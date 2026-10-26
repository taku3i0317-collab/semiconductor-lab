package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Survival;
import com.taku3.semiconductorlab.machine.Assembly;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 組立台（IMP-33、DESIGN「組立台」＝作り方の3段の2段目）。木の作業机＋万力。電気なし。
 * 画面の左で作る物を選び（◀ ▶）、右の「役目つきのスロット」に部品を入れ、全部そろったら「組み立てる」。
 * スロットにはその役目の部品しか入らない（MachineLayout.Ctx：選んだ製品は画面の数値 DATA_RECIPE で同期）。
 * ボタンは vanilla の clickMenuButton（0＝前の製品、1＝次の製品、2＝組み立てる）。押したらサーバーでもう一度確かめる。
 * スロット：0〜7＝部品（役目の順）、8＝完成品
 */
public class AssemblyBenchBlockEntity extends BlockEntity implements MachineBE {
    public static final int PARTS = Assembly.MAX_PARTS, SLOT_OUT = PARTS, SLOT_COUNT = PARTS + 1;
    public static final int BUTTON_PREV = 0, BUTTON_NEXT = 1, BUTTON_ASSEMBLE = 2, BUTTON_FILL = 3, BUTTON_FILL_MAX = 4;   // 3・4：持ち物から部品を自動でそろえる（S-030、4＝シフト：入るだけ）
    public static final int DATA_RECIPE = 0, DATA_PROGRESS = 1, DATA_TOTAL = 2, DATA_COUNT = 3;

    /** 画面の中の位置：部品は 4×2 のマス、完成品は右、ボタンは画面のクラス（AssemblyBenchScreen）が同じ数字で置く */
    public static final int PART_X = 66, PART_Y = 17, OUT_X = 152, OUT_Y = 17;
    public static final int BTN_Y = 16, PREV_X = 8, NEXT_X = 48, BTN_W = 12, ASSEMBLE_X = 142, ASSEMBLE_Y = 36, ASSEMBLE_W = 30, ASSEMBLE_H = 14;

    public static final MachineLayout LAYOUT;
    static {
        var b = MachineLayout.builder(DATA_COUNT);
        for (int i = 0; i < PARTS; i++) {
            final int slot = i;
            b.in(i, PART_X + 18 * (i % 4), PART_Y + 18 * (i / 4), AssemblyBenchBlockEntity::isAnyPart, new MachineLayout.Ctx() {
                @Override public boolean accepts(java.util.function.IntUnaryOperator data, ItemStack s) {
                    Assembly a = Assembly.get(data.applyAsInt(DATA_RECIPE));
                    return a != null && a.part(slot) != null && a.part(slot).ok().test(s);
                }
                @Override public boolean active(java.util.function.IntUnaryOperator data) {
                    Assembly a = Assembly.get(data.applyAsInt(DATA_RECIPE));
                    return a != null && a.part(slot) != null;
                }
            });
        }
        LAYOUT = b.out(SLOT_OUT, OUT_X, OUT_Y)
                .center("index", PREV_X + BTN_W, BTN_Y + 2, NEXT_X - PREV_X - BTN_W, 1, "=1/2")
                .lazyText("product", 8, 31, 54, 2, Assembly::resultKeys).fit()
                .lazyText("role", 8, 53, 164, 2, Assembly::roleKeys)
                .bottom("hint", MachineLayout.keys("gui.semiconductor-lab.assembly_bench.", "need_parts", "ready", "assembling", "out_full")).samples("100%")
                .build();
    }

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); AssemblyBenchBlockEntity.this.setChanged(); }
    };
    private int recipe, progress;
    private boolean working;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_RECIPE -> recipe; case DATA_PROGRESS -> progress; case DATA_TOTAL -> current() == null ? 1 : current().ticks; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public AssemblyBenchBlockEntity(BlockPos pos, BlockState state) { super(Survival.ASSEMBLY_BENCH.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public Assembly current() { return Assembly.get(recipe); }
    public int getProgress() { return progress; }
    public boolean isWorking() { return working; }

    /** どれかの組立レシピの部品か（シフトクリックの入口さがし用。本当の条件は役目ごと） */
    public static boolean isAnyPart(ItemStack s) {
        for (Assembly a : Assembly.all()) for (Assembly.Part p : a.parts) if (p.ok().test(s)) return true;
        return false;
    }

    /** 選んだ製品の部品が全部そろい、完成品の出口に空きがあるか */
    public boolean ready() {
        Assembly a = current();
        if (a == null) return false;
        for (int i = 0; i < a.parts.size(); i++) if (!a.satisfied(i, container.getItem(i))) return false;
        for (int i = a.parts.size(); i < PARTS; i++) if (!container.getItem(i).isEmpty()) return false;
        ItemStack out = container.getItem(SLOT_OUT), r = new ItemStack(a.result);
        return out.isEmpty() || (ItemStack.isSameItemSameComponents(out, r) && out.getCount() < out.getMaxStackSize());
    }

    @Override public boolean onButton(Player player, int id) {
        int n = Assembly.all().size();
        if (n == 0) return false;
        if (id == BUTTON_PREV || id == BUTTON_NEXT) {
            if (working) return false;
            giveBackParts(player);   // 前の製品の部品は持ち物へ戻す
            recipe = Math.floorMod(recipe + (id == BUTTON_NEXT ? 1 : -1), n);
            setChanged();
            return true;
        }
        if ((id == BUTTON_FILL || id == BUTTON_FILL_MAX) && !working) { fill(player, id == BUTTON_FILL_MAX); return true; }
        if (id == BUTTON_ASSEMBLE && !working && ready()) {
            working = true;
            progress = 0;
            setChanged();
            return true;
        }
        return false;
    }

    /**
     * 持ち物から、選んだ製品の部品を自動で置く（S-030 ユーザー「いちいちクリックでそろえるのが大変すぎる」）。
     * max＝false：1回分（役目ごとの数）、true：入るだけ（1回分の数の倍数）。ちがう物が入っていたら持ち物へ戻す。足りない物はそのまま（画面で赤い枠）
     */
    public void fill(Player player, boolean max) {
        Assembly a = current();
        if (a == null || player == null) return;
        var inv = player.getInventory();
        for (int i = 0; i < a.parts.size(); i++) {
            Assembly.Part part = a.parts.get(i);
            ItemStack in = container.getItem(i);
            if (!in.isEmpty() && !part.ok().test(in)) {   // ちがう物は戻す
                ItemStack back = container.removeItemNoUpdate(i);
                if (!inv.add(back)) Block.popResource(level, worldPosition.above(), back);
                in = ItemStack.EMPTY;
            }
            for (int k = 0; k < inv.getContainerSize(); k++) {
                ItemStack src = inv.getItem(k);
                if (src.isEmpty() || !part.ok().test(src) || (!in.isEmpty() && !ItemStack.isSameItemSameComponents(in, src))) continue;
                int cap = Math.min(src.getMaxStackSize(), container.getMaxStackSize());
                int target = max ? Math.max(part.count(), cap / part.count() * part.count()) : part.count();
                int need = target - in.getCount();
                if (need <= 0) break;
                ItemStack moved = src.split(Math.min(need, src.getCount()));
                if (in.isEmpty()) { in = moved; container.setItem(i, in); } else in.grow(moved.getCount());
            }
        }
        container.setChanged();
        inv.setChanged();
    }

    private void giveBackParts(Player player) {
        for (int i = 0; i < PARTS; i++) {
            ItemStack s = container.removeItemNoUpdate(i);
            if (!s.isEmpty() && !player.getInventory().add(s)) Block.popResource(level, worldPosition.above(), s);
        }
        container.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AssemblyBenchBlockEntity be) {
        if (level.isClientSide()) return;
        if (be.working) {
            Assembly a = be.current();
            if (a == null || !be.ready()) { be.working = false; be.progress = 0; }   // 途中で部品を抜いたらやめる
            else if (++be.progress >= a.ticks) {
                for (int i = 0; i < a.parts.size(); i++) be.container.getItem(i).shrink(a.parts.get(i).count());
                ItemStack out = be.container.getItem(SLOT_OUT);
                if (out.isEmpty()) be.container.setItem(SLOT_OUT, new ItemStack(a.result)); else out.grow(1);
                be.container.setChanged();
                be.working = false;
                be.progress = 0;
            }
            be.setChanged();
        }
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != be.working)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, be.working), 3);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Recipe", recipe); out.putInt("Progress", progress); out.putBoolean("Working", working);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        recipe = in.getIntOr("Recipe", 0); progress = in.getIntOr("Progress", 0); working = in.getBooleanOr("Working", false);
    }
}
