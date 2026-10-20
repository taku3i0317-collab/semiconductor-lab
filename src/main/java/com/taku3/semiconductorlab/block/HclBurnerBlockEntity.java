package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 塩酸合成炉 [6-06]（酸水素バーナー [7-02] の派生。石英の火口で水素を塩素の中で燃やし、できた塩化水素を水に吸わせる）。電気は使わない。
 * <pre>
 *   H2 + Cl2 → 2HCl（燃焼、約2000℃の炎。反応熱で燃え続ける）
 *   HCl（気体）を水に吸収 → 塩酸
 *   水素 1mol ＋ 塩素 1mol ＋ 水 1/4 バケツ（5秒）→ 塩酸 ×1（HCl 2mol 入り、約8mol/L＝約25%）
 * </pre>
 * ガスは袋（1mol）・ボンベ（50mol）・隣のガスホルダーのどれからでも受け取り、BE の中（mol）に移す。
 * 水はバケツ1杯で塩酸4本分。空ボンベ・空バケツは右下の出口へ。
 * 水がない（水のスロットも空）ときは、吸わせずに乾いた塩化水素ガスのまま袋に詰める：H2 1mol ＋ Cl2 1mol → 袋詰め塩化水素 ×2（2mol）。
 * トリクロロシラン [1-16] は水と反応してしまうので、塩酸ではなくこの乾いたガスを使う（タスク22）。
 * スロット：0＝水素、1＝塩素、2＝水バケツ、3＝塩酸、4＝空ボンベ、5＝空バケツ
 */
public class HclBurnerBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_H2 = 0, SLOT_CL2 = 1, SLOT_WATER = 2, SLOT_OUT = 3, SLOT_EMPTY = 4, SLOT_BUCKET = 5, SLOT_COUNT = 6;
    public static final int BURN_TICKS = 100;
    /** 塩酸1本あたり：H2 1mol ＋ Cl2 1mol → HCl 2mol。水バケツ1杯（1L）で4本＝HCl 8mol/L */
    public static final int H2_PER = 1, CL2_PER = 1, ACID_PER_BUCKET = 4;
    public static final int MOL_PER_CYLINDER = CompressorBlockEntity.MOL_PER_CYLINDER;
    /** 隣のホルダーから一度にもらう量と間隔 */
    private static final int PULL_MOL = 8, PULL_INTERVAL = 10;
    public static final int DATA_PROGRESS = 0, DATA_TOTAL = 1, DATA_H2 = 2, DATA_CL2 = 3, DATA_WATER = 4, DATA_COUNT = 5;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); HclBurnerBlockEntity.this.setChanged(); }
    };
    private int progress, h2Mol, cl2Mol, water;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_PROGRESS -> progress; case DATA_TOTAL -> BURN_TICKS; case DATA_H2 -> h2Mol; case DATA_CL2 -> cl2Mol; case DATA_WATER -> water; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    /** 画面のスロットの並び（IMP-28：前の HclBurnerMenu の代わり） */
    /** 画面の中のスロットの位置（画面の絵と合わせる） */
    public static final int H2_X = 26, CL2_X = 62, GAS_Y = 53, WATER_X = 44, WATER_Y = 17, OUT_X = 116, OUT_Y = 35, EMPTY_X = 134, BUCKET_X = 152, EMPTIES_Y = 59;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_H2, H2_X, GAS_Y, HclBurnerBlockEntity::isHydrogen)
            .in(SLOT_CL2, CL2_X, GAS_Y, HclBurnerBlockEntity::isChlorine)
            .in(SLOT_WATER, WATER_X, WATER_Y, s -> s.is(net.minecraft.world.item.Items.WATER_BUCKET))
            .out(SLOT_OUT, OUT_X, OUT_Y)
            .out(SLOT_EMPTY, EMPTY_X, EMPTIES_Y)
            .out(SLOT_BUCKET, BUCKET_X, EMPTIES_Y)
            .text("h2", H2_X - 2, GAS_Y - 10, 34, 1, "=H2 00")
            .text("cl2", CL2_X - 2, GAS_Y - 10, 34, 1, "=Cl2 00")
            .text("water", WATER_X + 20, WATER_Y + 1, 108, 1, "gui.semiconductor-lab.hcl_burner.water").samples("4")
            .text("hint", 82, 53, 48, 2, "gui.semiconductor-lab.hcl_burner.burning", "gui.semiconductor-lab.hcl_burner.need_water", "gui.semiconductor-lab.hcl_burner.need_h2", "gui.semiconductor-lab.hcl_burner.need_cl2", "gui.semiconductor-lab.hcl_burner.out_full").fit()
            .build();

    public HclBurnerBlockEntity(BlockPos pos, BlockState state) { super(Gas.HCL_BURNER.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public net.minecraft.world.inventory.ContainerData getMenuData() { return data; }
    public int getH2() { return h2Mol; }
    public int getCl2() { return cl2Mol; }
    public int getWater() { return water; }

    public static boolean isHydrogen(ItemStack s) { return s.is(Gas.HYDROGEN_GAS) || s.is(Gas.HYDROGEN_CYLINDER); }
    public static boolean isChlorine(ItemStack s) { return s.is(Gas.CHLORINE_GAS) || s.is(Gas.CHLORINE_CYLINDER); }

    private static boolean canMerge(ItemStack out, ItemStack add) {
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(out, add) && out.getCount() + add.getCount() <= out.getMaxStackSize();
    }
    private void merge(int slot, ItemStack add) {
        ItemStack out = container.getItem(slot);
        if (out.isEmpty()) container.setItem(slot, add.copy());
        else { out.grow(add.getCount()); container.setChanged(); }
    }

    /** スロットにガスがあるか（袋 or ボンベ） */
    private boolean hasGasItem(int slot, Item bag, Item cylinder) {
        ItemStack s = container.getItem(slot);
        return s.is(bag) || (s.is(cylinder) && canMerge(container.getItem(SLOT_EMPTY), new ItemStack(Gas.EMPTY_GAS_CYLINDER)));
    }

    /** スロットのガスを1つ開ける（袋は 1mol、ボンベは 50mol で空ボンベが出る）。開けた mol を返す */
    private int open(int slot, Item bag, Item cylinder) {
        ItemStack s = container.getItem(slot);
        if (s.is(bag)) { s.shrink(1); container.setChanged(); return 1; }
        if (s.is(cylinder) && canMerge(container.getItem(SLOT_EMPTY), new ItemStack(Gas.EMPTY_GAS_CYLINDER))) {
            s.shrink(1);
            merge(SLOT_EMPTY, new ItemStack(Gas.EMPTY_GAS_CYLINDER));
            return MOL_PER_CYLINDER;
        }
        return 0;
    }

    /** 隣のガスホルダーから、足りないガスをもらう */
    private void pullFromHolders(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            if (h2Mol < H2_PER) h2Mol += holder.extract(GasType.HYDROGEN, PULL_MOL);
            if (cl2Mol < CL2_PER) cl2Mol += holder.extract(GasType.CHLORINE, PULL_MOL);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HclBurnerBlockEntity be) {
        if (level.isClientSide()) return;
        if (level.getGameTime() % PULL_INTERVAL == 0) be.pullFromHolders(level, pos);
        // 水：足りなければバケツを1杯あける（空バケツの出口に空きがあるとき）
        if (be.water <= 0 && be.container.getItem(SLOT_WATER).is(Items.WATER_BUCKET) && canMerge(be.container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET))) {
            be.container.getItem(SLOT_WATER).shrink(1);
            be.merge(SLOT_BUCKET, new ItemStack(Items.BUCKET));
            be.water += ACID_PER_BUCKET;
            be.setChanged();
        }
        // 水があれば塩酸、水がなければ乾いた塩化水素ガス（2mol）
        boolean dry = be.water <= 0 && !be.container.getItem(SLOT_WATER).is(Items.WATER_BUCKET);
        ItemStack product = dry ? new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, 2) : new ItemStack(Gas.HYDROCHLORIC_ACID);
        boolean can = (dry || be.water > 0) && canMerge(be.container.getItem(SLOT_OUT), product);
        // 両方のガスがそろうときだけ開ける（片方だけでは炎が立たない）
        boolean h2Ready = be.h2Mol >= H2_PER || be.hasGasItem(SLOT_H2, Gas.HYDROGEN_GAS, Gas.HYDROGEN_CYLINDER);
        boolean cl2Ready = be.cl2Mol >= CL2_PER || be.hasGasItem(SLOT_CL2, Gas.CHLORINE_GAS, Gas.CHLORINE_CYLINDER);
        if (can && h2Ready && cl2Ready) {
            if (be.h2Mol < H2_PER) be.h2Mol += be.open(SLOT_H2, Gas.HYDROGEN_GAS, Gas.HYDROGEN_CYLINDER);
            if (be.cl2Mol < CL2_PER) be.cl2Mol += be.open(SLOT_CL2, Gas.CHLORINE_GAS, Gas.CHLORINE_CYLINDER);
        }
        boolean burning = can && be.h2Mol >= H2_PER && be.cl2Mol >= CL2_PER;
        if (burning) {
            if (++be.progress >= BURN_TICKS) {
                be.progress = 0;
                be.h2Mol -= H2_PER;    // H2 + Cl2 → 2HCl
                be.cl2Mol -= CL2_PER;
                if (!dry) be.water--;  // HCl を水に吸わせる（乾いたガスのときは水を使わない）
                be.merge(SLOT_OUT, product);
            }
            be.setChanged();
        } else if (be.progress != 0) { be.progress = 0; be.setChanged(); }
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != burning)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, burning), 3);
    }

    /** 壊すと中身は落ちる。中のガスと水は逃げる */
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Progress", progress); out.putInt("H2", h2Mol); out.putInt("Cl2", cl2Mol); out.putInt("Water", water);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        progress = in.getIntOr("Progress", 0); h2Mol = in.getIntOr("H2", 0); cl2Mol = in.getIntOr("Cl2", 0); water = in.getIntOr("Water", 0);
    }
}
