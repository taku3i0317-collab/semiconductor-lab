package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
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
 * ガス工場のレトルト [2-20]（IMP4-01）：耐火粘土の釜（レトルト）に石炭を入れ、外から約1000℃に熱する（空気は入れない＝乾留）。
 * 1792 マードックの石炭ガス灯、1812 ロンドンのガス会社。石炭ガスを取るのが目的で、釜にはコークスが残り、
 * ガスを冷やすと黒いコールタールと、アンモニアを含む水（安水）がたまる。タールは1850年頃から化学工業の原料になった。電気は使わない。
 * <pre>
 *   石炭 4個 → コークス 3（重さで約7割が残る）
 *            ＋ 4個ごとに 安水 1（アンモニア 約1〜2% の水）
 *            ＋ 8個ごとに コールタール 1（重さで約5%）
 * </pre>
 * 割合はガス工場の典型値（石炭1トンから コークス 約700kg、タール 約50kg、安水 約100L、ガス 約300m³）を丸めたもの。
 * ⚠️ 石炭ガスは回収しない（本物の目的は照明用のガス。gas_retort.placeholder）。燃料は石炭・木炭・コークス（本物はコークスで熱した）。
 * スロット：0＝石炭、1＝燃料、2＝コークス、3＝コールタール、4＝安水
 */
public class GasRetortBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_IN = 0, SLOT_FUEL = 1, SLOT_COKE = 2, SLOT_TAR = 3, SLOT_LIQUOR = 4, SLOT_COUNT = 5;
    /** 石炭1個を乾留する時間（10秒） */
    public static final int DISTILL_TICKS = 200;
    /** 燃料1個が燃える時間（かまどと同じ＝8個分） */
    public static final int FUEL_TICKS = 1600;
    /** 何個ごとに安水・タールが1個出るか（コークスは4個に3個） */
    public static final int LIQUOR_EVERY = 4, TAR_EVERY = 8;
    public static final int DATA_BURN = 0, DATA_BURN_TOTAL = 1, DATA_PROGRESS = 2, DATA_TOTAL = 3, DATA_COUNT = 4;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); GasRetortBlockEntity.this.setChanged(); }
    };
    private int burnTicks, progress;
    /** 乾留した石炭の数（コークス・安水・タールの割合を決める。保存する） */
    private int coalDone;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> FUEL_TICKS;
            case DATA_PROGRESS -> progress; case DATA_TOTAL -> DISTILL_TICKS; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    /** 画面の中のスロットの位置（画面の絵と合わせる。反射炉と同じ並び） */
    public static final int OUT_X = 104, OUT_Y = 22;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_IN, 44, 17, GasRetortBlockEntity::isInput)
            .in(SLOT_FUEL, 44, 53, GasRetortBlockEntity::isFuel)
            .out(SLOT_COKE, OUT_X, OUT_Y)
            .out(SLOT_TAR, OUT_X + 22, OUT_Y)
            .out(SLOT_LIQUOR, OUT_X, OUT_Y + 22)
            .text("hint", 64, 64, 108, 2, MachineLayout.keys("gui.semiconductor-lab.gas_retort.", "need_input", "need_fuel", "running", "out_full"))
            .build();

    public GasRetortBlockEntity(BlockPos pos, BlockState state) { super(Chemistry.GAS_RETORT.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public int getCoalDone() { return coalDone; }

    public static boolean isFuel(ItemStack s) { return s.is(Items.COAL) || s.is(Items.CHARCOAL) || s.is(Metallurgy.COKE); }
    /** 乾留するのは石炭だけ（木炭はもう乾留ずみ） */
    public static boolean isInput(ItemStack s) { return s.is(Items.COAL); }

    private static boolean canMerge(ItemStack out, ItemStack add) {
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(out, add) && out.getCount() + add.getCount() <= out.getMaxStackSize();
    }
    private void merge(int slot, ItemStack add) {
        ItemStack out = container.getItem(slot);
        if (out.isEmpty()) container.setItem(slot, add.copy());
        else { out.grow(add.getCount()); container.setChanged(); }
    }

    /** 石炭があり、3つの出口すべてに空きがあるか */
    private boolean canDistill() {
        return isInput(container.getItem(SLOT_IN))
                && canMerge(container.getItem(SLOT_COKE), new ItemStack(Metallurgy.COKE))
                && canMerge(container.getItem(SLOT_TAR), new ItemStack(Chemistry.COAL_TAR))
                && canMerge(container.getItem(SLOT_LIQUOR), new ItemStack(Chemistry.AMMONIACAL_LIQUOR));
    }

    private void finishDistill() {
        container.getItem(SLOT_IN).shrink(1);
        coalDone++;
        if (coalDone % 4 != 0) merge(SLOT_COKE, new ItemStack(Metallurgy.COKE));   // 4個に3個（4個目の分はガスとタールになった）
        if (coalDone % LIQUOR_EVERY == 0) merge(SLOT_LIQUOR, new ItemStack(Chemistry.AMMONIACAL_LIQUOR));
        if (coalDone % TAR_EVERY == 0) merge(SLOT_TAR, new ItemStack(Chemistry.COAL_TAR));
        container.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GasRetortBlockEntity be) {
        if (level.isClientSide()) return;
        boolean can = be.canDistill();
        boolean changed = false;
        // 火が消えていて、乾留する石炭があれば燃料を1個くべる（空焚きはしない）
        if (be.burnTicks <= 0 && can) {
            ItemStack fuel = be.container.getItem(SLOT_FUEL);
            if (isFuel(fuel)) { fuel.shrink(1); be.burnTicks = FUEL_TICKS; be.container.setChanged(); changed = true; }
        }
        if (be.burnTicks > 0) {
            be.burnTicks--;
            changed = true;
            if (can) {
                if (++be.progress >= DISTILL_TICKS) { be.progress = 0; be.finishDistill(); }
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
        out.putInt("Burn", burnTicks); out.putInt("Progress", progress); out.putInt("CoalDone", coalDone);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        burnTicks = in.getIntOr("Burn", 0); progress = in.getIntOr("Progress", 0); coalDone = in.getIntOr("CoalDone", 0);
    }
}
