package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 鉛のレトルト [2-10]（IMP4-01）：蛍石を硫酸といっしょに鉛の釜で約200〜250℃に熱すると、CaF2 + H2SO4 → CaSO4 + 2HF↑。
 * 出てきたフッ化水素の気体を、鉛の受け器の水に吸わせるとフッ酸になる（1771 シェーレ。ガラスのレトルトが侵食されて性質に気づいた）。
 * フッ酸はガラス（SiO2）を溶かすので、釜・受け器・瓶は鉛（のちにろう・ガタパーチャ・ポリエチレン）。空のポリエチレンの瓶を入れればポリエチレンの瓶入りになる。電気は使わない（燃料式）。
 * <pre>
 *   蛍石 1 ＋ 硫酸 1（ガラス瓶は機械の上に戻る）＋ 空の鉛の瓶 1 → フッ酸（鉛の瓶入り）1 ＋ 石こう（硫酸カルシウム）1
 *   吸収用の水：水バケツ 1 で {@link #RUNS_PER_WATER} 回（空のバケツが出口に戻る）
 * </pre>
 * ⚠️ 量は記号的（本物は蛍石 1mol から HF 2mol）。濃度（本物は約40〜70%）と吸収塔は省略（lead_retort.placeholder）。
 * スロット：0＝蛍石、1＝硫酸、2＝空の鉛の瓶、3＝水、4＝燃料、5＝フッ酸、6＝石こう、7＝空のバケツ
 */
public class HfRetortBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_FLUORITE = 0, SLOT_ACID = 1, SLOT_BOTTLE = 2, SLOT_WATER = 3, SLOT_FUEL = 4,
            SLOT_HF = 5, SLOT_GYPSUM = 6, SLOT_BUCKET = 7, SLOT_COUNT = 8;
    /** 1回の時間（10秒） */
    public static final int REACT_TICKS = 200;
    /** 燃料1個が燃える時間（かまどと同じ＝8回分） */
    public static final int FUEL_TICKS = 1600;
    /** 水バケツ1杯で何回ぶんの HF を吸わせるか */
    public static final int RUNS_PER_WATER = 4;
    public static final int DATA_BURN = 0, DATA_BURN_TOTAL = 1, DATA_PROGRESS = 2, DATA_TOTAL = 3, DATA_WATER = 4, DATA_COUNT = 5;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); HfRetortBlockEntity.this.setChanged(); }
    };
    private int burnTicks, progress;
    /** 受け器に残っている水（あと何回ぶん吸えるか。保存する） */
    private int water;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> FUEL_TICKS;
            case DATA_PROGRESS -> progress; case DATA_TOTAL -> REACT_TICKS; case DATA_WATER -> water; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public static final int OUT_X = 104, OUT_Y = 22;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_FLUORITE, 8, 17, s -> s.is(Metallurgy.FLUORITE))
            .in(SLOT_ACID, 26, 17, s -> s.is(Chemistry.SULFURIC_ACID))
            .in(SLOT_BOTTLE, 44, 17, HfRetortBlockEntity::isBottle)
            .in(SLOT_WATER, 8, 53, s -> s.is(Items.WATER_BUCKET))
            .in(SLOT_FUEL, 44, 53, HfRetortBlockEntity::isFuel)
            .out(SLOT_HF, OUT_X, OUT_Y)
            .out(SLOT_GYPSUM, OUT_X + 22, OUT_Y)
            .out(SLOT_BUCKET, OUT_X + 44, OUT_Y)
            .text("hint", 64, 64, 108, 2, MachineLayout.keys("gui.semiconductor-lab.lead_retort.", "need_input", "need_bottle", "need_water", "need_fuel", "running", "out_full"))
            .build();

    public HfRetortBlockEntity(BlockPos pos, BlockState state) { super(Chemistry.LEAD_RETORT.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public int getWater() { return water; }

    /** 空の瓶：鉛かポリエチレン（ガラスは溶ける） */
    public static boolean isBottle(ItemStack s) { return s.is(Metallurgy.LEAD_BOTTLE) || s.is(Chemistry.POLYETHYLENE_BOTTLE); }
    /** 瓶に合わせたフッ酸（鉛の瓶 → hydrofluoric_acid、PE の瓶 → hydrofluoric_acid_pe） */
    public static ItemStack hfFor(ItemStack bottle) { return new ItemStack(bottle.is(Chemistry.POLYETHYLENE_BOTTLE) ? Chemistry.HYDROFLUORIC_ACID_PE : Chemistry.HYDROFLUORIC_ACID); }

    public static boolean isFuel(ItemStack s) { return s.is(Items.COAL) || s.is(Items.CHARCOAL) || s.is(Metallurgy.COKE); }

    private static boolean canMerge(ItemStack out, ItemStack add) {
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(out, add) && out.getCount() + add.getCount() <= out.getMaxStackSize();
    }
    private void merge(int slot, ItemStack add) {
        ItemStack out = container.getItem(slot);
        if (out.isEmpty()) container.setItem(slot, add.copy());
        else { out.grow(add.getCount()); container.setChanged(); }
    }

    /** 材料がそろい、出口に空きがあるか（水は受け器に残っているか、バケツを空けて足せるか） */
    private boolean canReact() {
        if (!container.getItem(SLOT_FLUORITE).is(Metallurgy.FLUORITE) || !container.getItem(SLOT_ACID).is(Chemistry.SULFURIC_ACID)
                || !isBottle(container.getItem(SLOT_BOTTLE))) return false;
        if (water <= 0 && !(container.getItem(SLOT_WATER).is(Items.WATER_BUCKET) && canMerge(container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET)))) return false;
        return canMerge(container.getItem(SLOT_HF), hfFor(container.getItem(SLOT_BOTTLE)))
                && canMerge(container.getItem(SLOT_GYPSUM), new ItemStack(Chemistry.CALCIUM_SULFATE));
    }

    private void finishReaction(Level level, BlockPos pos) {
        if (water <= 0) {   // 受け器に水を足す（空のバケツは出口へ）
            container.getItem(SLOT_WATER).shrink(1);
            merge(SLOT_BUCKET, new ItemStack(Items.BUCKET));
            water = RUNS_PER_WATER;
        }
        water--;
        container.getItem(SLOT_FLUORITE).shrink(1);
        ItemStack acid = container.getItem(SLOT_ACID);
        Vessel.returnEmpty(level, pos, acid, 1);   // 硫酸のガラス瓶は機械の上に戻る
        acid.shrink(1);
        ItemStack hf = hfFor(container.getItem(SLOT_BOTTLE));
        container.getItem(SLOT_BOTTLE).shrink(1);
        merge(SLOT_HF, hf);
        merge(SLOT_GYPSUM, new ItemStack(Chemistry.CALCIUM_SULFATE));
        container.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HfRetortBlockEntity be) {
        if (level.isClientSide()) return;
        boolean can = be.canReact();
        boolean changed = false;
        // 火が消えていて、反応させる物があれば燃料を1個くべる（空焚きはしない）
        if (be.burnTicks <= 0 && can) {
            ItemStack fuel = be.container.getItem(SLOT_FUEL);
            if (isFuel(fuel)) { fuel.shrink(1); be.burnTicks = FUEL_TICKS; be.container.setChanged(); changed = true; }
        }
        if (be.burnTicks > 0) {
            be.burnTicks--;
            changed = true;
            if (can) {
                if (++be.progress >= REACT_TICKS) { be.progress = 0; be.finishReaction(level, pos); }
            } else if (be.progress != 0) be.progress = 0;
        } else if (be.progress != 0) { be.progress = 0; changed = true; }
        if (changed) be.setChanged();
        boolean lit = be.burnTicks > 0;
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), 3);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Burn", burnTicks); out.putInt("Progress", progress); out.putInt("Water", water);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        burnTicks = in.getIntOr("Burn", 0); progress = in.getIntOr("Progress", 0); water = in.getIntOr("Water", 0);
    }
}
