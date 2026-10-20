package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * エッチング槽 [9-06]（IMP4-01）：鉛を張った槽にフッ酸を入れ、酸化膜つきの板を浸す。SiO2 + 6HF → H2SiF6 + 2H2O で酸化膜だけが溶け、
 * シリコンはほとんど溶けない（選択性）。膜が溶けきると表面が水をはじく（撥水）ので、終わりが目で分かる。電気・熱は使わない（室温）。
 * 酸化膜つきの板 ×{@link #WAFERS_PER_RUN} ＋ フッ酸 1（空の鉛の瓶は機械の上に戻る）→ シリコンの板 ×{@link #WAFERS_PER_RUN}（純度・ドーパントを受け継ぐ）。
 * 時間は膜の厚さ ÷ 溶ける速さ（ゲームでは 1秒に {@link #NM_PER_SECOND} nm。本物の薄めたフッ酸は 1分に数十〜100nm 程度）。
 * 加工中のウェハー（process_wafer、現像ずみ）を入れると、レジストのない所だけ膜が溶けて「窓」が開く（ChipOps.etchOxide、LITHO_DESIGN §5）。1回に1〜8枚。
 * 酸化膜つきの板（レジストなし）を入れると、膜を全部はがす。
 * リン酸（[9-25]）を入れると Al を削る：Al を一面に付けて現像した加工中のウェハー → レジストの残っている所だけ Al が残る（ChipOps.etchMetal）。
 * 2Al + 2H3PO4 → 2AlPO4 + 3H2。リン酸は酸化膜もシリコンもほとんど溶かさない。
 * 混酸（フッ酸＋硝酸、[9-17]）を入れるとシリコンそのものが溶ける：ろうで守った板 → ろうの下だけが台地に残ったメサの板。
 * スロット：0＝酸化膜つきの板、1＝フッ酸、2＝出口
 */
public class EtchTankBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_WAFER = 0, SLOT_HF = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    public static final int WAFERS_PER_RUN = 8;
    /** ゲームの溶ける速さ（nm/秒）。⚠️ 本物より速い（etch_tank.placeholder） */
    public static final int NM_PER_SECOND = 20;
    /** Al（約1µm）を削る時間。⚠️ ゲームの値（本物の温めたリン酸系で数分） */
    public static final int METAL_TICKS = 20 * 10;
    public static final int DATA_PROGRESS = 0, DATA_TOTAL = 1, DATA_COUNT = 2;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); EtchTankBlockEntity.this.setChanged(); }
    };
    private int progress, total = 1;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) { case DATA_PROGRESS -> progress; case DATA_TOTAL -> total; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, EtchTankBlockEntity::isInput)
            .in(SLOT_HF, 8, 50, EtchTankBlockEntity::isAcid)
            .out(SLOT_OUT, 78, 35)
            .right("hint", 17, 4, "gui.semiconductor-lab.etch_tank.need_input", "gui.semiconductor-lab.etch_tank.running", "gui.semiconductor-lab.etch_tank.out_full")
            .bottom("recipe", "gui.semiconductor-lab.etch_tank.recipe", "gui.semiconductor-lab.etch_tank.recipe_metal", "gui.semiconductor-lab.etch_tank.recipe_silicon")
            .build();

    public EtchTankBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.ETCH_TANK.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }

    /** 入れてよい物：酸化膜つきの板（全部はがす）か、窓を開けられる／Al を削れる加工中のウェハー */
    public static boolean isInput(ItemStack s) { return s.is(Semiconductor.OXIDIZED_WAFER) || s.is(Semiconductor.WAXED_WAFER) || ChipOps.canEtchOxide(s) || ChipOps.canEtchMetal(s); }
    /** 酸：フッ酸（酸化膜）かリン酸（Al） */
    public static boolean isAcid(ItemStack s) { return s.is(Chemistry.HYDROFLUORIC_ACID) || s.is(Chemistry.PHOSPHORIC_ACID) || s.is(Chemistry.SILICON_ETCH); }

    /** 1回の枚数：板は8枚ちょうど、加工中のウェハーは1〜8枚 */
    public static int batch(ItemStack w) {
        if (ChipOps.isProcessWafer(w) || w.is(Semiconductor.WAXED_WAFER)) return Math.min(w.getCount(), WAFERS_PER_RUN);
        return w.is(Semiconductor.OXIDIZED_WAFER) && w.getCount() >= WAFERS_PER_RUN ? WAFERS_PER_RUN : 0;
    }

    /** 出てくる物：フッ酸なら窓が開いたもの（板は膜をはがしたシリコンの板）、リン酸なら Al を削ったもの。できなければ EMPTY */
    public static ItemStack product(ItemStack w, int n, ItemStack acid) {
        // [9-17] 混酸（HF＋HNO3）はシリコンそのものを溶かす：ろうで守った点の下だけが台地（メサ）に残る
        if (acid.is(Chemistry.SILICON_ETCH)) return w.is(Semiconductor.WAXED_WAFER) ? new ItemStack(Semiconductor.MESA_WAFER, n) : ItemStack.EMPTY;
        if (w.is(Semiconductor.WAXED_WAFER)) return ItemStack.EMPTY;
        if (acid.is(Chemistry.PHOSPHORIC_ACID)) {
            if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
            ChipOps.Result r = ChipOps.etchMetal(w.copyWithCount(n)); return r.ok() ? r.out() : ItemStack.EMPTY;
        }
        if (!acid.is(Chemistry.HYDROFLUORIC_ACID)) return ItemStack.EMPTY;
        if (ChipOps.isProcessWafer(w)) { ChipOps.Result r = ChipOps.etchOxide(w.copyWithCount(n)); return r.ok() ? r.out() : ItemStack.EMPTY; }
        return stripped(w, n);
    }

    /** この厚さの膜が溶けきるまでの tick（最低 1秒） */
    public static int ticksFor(int oxideNm) { return Math.max(20, (int) Math.ceil(oxideNm * 20.0 / NM_PER_SECOND)); }

    /** 膜をはがした板：純度・ドーパントを受け継ぐ（膜の厚さは消える） */
    public static ItemStack stripped(ItemStack oxidized, int count) {
        ItemStack s = new ItemStack(Semiconductor.SILICON_WAFER, count);
        Double p = oxidized.get(ModDataComponents.PURITY);
        if (p != null) s.set(ModDataComponents.PURITY, p);
        String d = oxidized.get(ModDataComponents.DOPANT);
        if (d != null) s.set(ModDataComponents.DOPANT, d);
        return s;
    }

    private boolean canEtch() {
        ItemStack w = container.getItem(SLOT_WAFER), out = container.getItem(SLOT_OUT);
        int n = batch(w);
        if (n == 0 || !isAcid(container.getItem(SLOT_HF))) return false;
        ItemStack add = product(w, n, container.getItem(SLOT_HF));
        if (add.isEmpty()) return false;
        return out.isEmpty() || (ItemStack.isSameItemSameComponents(out, add) && out.getCount() + add.getCount() <= out.getMaxStackSize());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EtchTankBlockEntity be) {
        if (level.isClientSide()) return;
        if (!be.canEtch()) { if (be.progress != 0) { be.progress = 0; be.setChanged(); } return; }
        be.total = (be.container.getItem(SLOT_HF).is(Chemistry.PHOSPHORIC_ACID) || be.container.getItem(SLOT_HF).is(Chemistry.SILICON_ETCH)) ? METAL_TICKS : ticksFor(OxidationFurnaceBlockEntity.oxideOf(be.container.getItem(SLOT_WAFER)));
        if (++be.progress >= be.total) {
            be.progress = 0;
            int n = batch(be.container.getItem(SLOT_WAFER));
            ItemStack add = product(be.container.getItem(SLOT_WAFER), n, be.container.getItem(SLOT_HF));
            be.container.getItem(SLOT_WAFER).shrink(n);
            ItemStack hf = be.container.getItem(SLOT_HF);
            Vessel.returnEmpty(level, pos, hf, 1);   // 空の瓶（フッ酸＝鉛、リン酸＝ガラス）は上に戻る
            hf.shrink(1);
            ItemStack out = be.container.getItem(SLOT_OUT);
            if (out.isEmpty()) be.container.setItem(SLOT_OUT, add); else out.grow(add.getCount());
            be.container.setChanged();
        }
        be.setChanged();
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Progress", progress);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        progress = in.getIntOr("Progress", 0);
    }
}
