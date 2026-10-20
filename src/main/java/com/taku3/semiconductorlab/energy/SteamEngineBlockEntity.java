package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.ContainerHelper;

/**
 * 火力発電機（ボイラー＋蒸気機関＋ダイナモ）。石炭1個＝30秒 50kW。水バケツ1杯＝石炭2個分の蒸気。右クリックで GUI。
 * エネルギーの勘定（ゲームの約束）：石炭1個の熱＝30MJ（本物の石炭 約1kg 分）。蒸気機関は 50kW×30秒＝1.5MJ＝熱効率5%。
 * 自励式ダイナモ [4-05] を付けると 100kW になるが、石炭も2倍の速さ（15秒）で燃える＝石炭1個の電気は同じ 1.5MJ。
 * 蒸気タービン [4-06] はこのクラスを継承し、出力と燃焼時間だけ変える（30%＝石炭1個 9MJ）。
 */
public class SteamEngineBlockEntity extends GeneratorBlockEntity implements MachineBE {
    public static final double WATTS = 50_000;
    public static final int COAL_TICKS = 20 * 30;
    public static final double COAL_HEAT_J = 30_000_000;   // 石炭1個の熱（ゲームの約束）
    public static final int SLOT_COAL = 0, SLOT_WATER = 1;
    // GUI へ送る数値。同期は short（最大32767）なので、W は「0.1kW 単位」、J は「kJ 単位」に縮めて送る
    public static final int DATA_BURN = 0, DATA_BURN_TOTAL = 1, DATA_WATER = 2, DATA_DECI_KW = 3, DATA_KJ = 4, DATA_COUNT = 5;
    private final SimpleContainer container = new SimpleContainer(2) { @Override public void setChanged() { super.setChanged(); SteamEngineBlockEntity.this.setChanged(); } };
    private int burnTicks, waterUnits;
    // waterUnits: 石炭何個分の水が残っているか。feedNow: 隣の水源から給水できたか（タービンだけ。GUI には水 = -1 で送る）
    private boolean feedNow;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> coalTicks; case DATA_WATER -> feedNow ? -1 : waterUnits;
            case DATA_DECI_KW -> (int) Math.round(getLastWatts() / 100); case DATA_KJ -> (int) (storage.getStoredJ() / 1000); default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    private final double watts;
    private final int coalTicks;

    /** 画面のスロットの並び（IMP-28：前の SteamEngineMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_COAL, 56, 53, SteamEngineBlockEntity::isFuel)
            .in(SLOT_WATER, 56, 17, s -> s.is(net.minecraft.world.item.Items.WATER_BUCKET))
            .texture("steam_engine")
            .text("output", 82, 17, 90, 1, "gui.semiconductor-lab.steam_engine.output").samples("500.0 kW")
            .text("stored", 82, 27, 90, 1, "gui.semiconductor-lab.steam_engine.stored").samples("50 kJ")
            .text("water", 82, 37, 90, 2, "gui.semiconductor-lab.steam_engine.water_intake", "gui.semiconductor-lab.steam_engine.water").samples("8")
            .text("hint", 82, 58, 90, 2, "gui.semiconductor-lab.steam_engine.running", "gui.semiconductor-lab.steam_engine.need_water", "gui.semiconductor-lab.steam_engine.need_coal")
            .build();

    public SteamEngineBlockEntity(BlockPos pos, BlockState state) { this(Power.STEAM_ENGINE.type, pos, state, 200_000, WATTS, COAL_TICKS); }
    protected SteamEngineBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state, long bufferJ, double watts, int coalTicks) {
        super(type, pos, state, bufferJ, watts * 2);   // ×2 は自励式ダイナモの分
        this.watts = watts; this.coalTicks = coalTicks;
    }
    @Override public boolean supportsDynamoUpgrade() { return true; }
    /** 石炭1個が燃え尽きるまでの tick（自励式なら半分） */
    public int coalTicks() { return coalTicks; }
    /** 燃えている間の出力（W）。自励式なら2倍 */
    public double ratedWatts() { return watts * dynamoFactor(); }
    /** 熱効率：石炭1個の電気 ÷ 石炭1個の熱 */
    public double thermalEfficiency() { return watts * coalTicks / 20.0 / COAL_HEAT_J; }
    /** ボイラーに水が来ているか（バケツの水が残っている以外に）。タービンは隣の水源からも取る */
    protected boolean hasFeedWater(Level level, BlockPos pos) { return false; }
    @Override public SimpleContainer getContainer() { return container; }
    @Override public net.minecraft.world.inventory.ContainerData getMenuData() { return data; }
    public int getBurnTicks() { return burnTicks; }
    public int getWaterUnits() { return waterUnits; }
    public static boolean isFuel(ItemStack s) { return s.is(Items.COAL) || s.is(Items.CHARCOAL); }

    @Override protected double produceWatts(Level level, BlockPos pos, BlockState state) {
        if (burnTicks <= 0) {
            boolean feed = feedNow = hasFeedWater(level, pos);
            ItemStack water = container.getItem(SLOT_WATER);
            if (!feed && waterUnits <= 0 && water.is(Items.WATER_BUCKET)) { container.setItem(SLOT_WATER, new ItemStack(Items.BUCKET)); waterUnits = 2; }
            ItemStack coal = container.getItem(SLOT_COAL);
            if ((feed || waterUnits > 0) && isFuel(coal)) { coal.shrink(1); container.setChanged(); if (!feed) waterUnits--; burnTicks = coalTicks; }
        }
        if (burnTicks <= 0) return 0;
        burnTicks = Math.max(0, burnTicks - dynamoFactor());   // 自励式：2倍の出力＝2倍の速さで石炭を使う
        return ratedWatts();
    }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, container.getItems()); out.putInt("Burn", burnTicks); out.putInt("Water", waterUnits); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); container.getItems().replaceAll(s -> ItemStack.EMPTY); ContainerHelper.loadAllItems(in, container.getItems()); burnTicks = in.getIntOr("Burn", 0); waterUnits = in.getIntOr("Water", 0); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) net.minecraft.world.Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
}
