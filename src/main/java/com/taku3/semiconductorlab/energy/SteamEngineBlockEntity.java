package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModBlockEntities;
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

/** 火力発電機（ボイラー＋蒸気機関＋ダイナモ）。石炭1個＝30秒 50kW。水バケツ1杯＝石炭2個分の蒸気。右クリックで GUI */
public class SteamEngineBlockEntity extends GeneratorBlockEntity implements MenuProvider {
    public static final double WATTS = 50_000;
    public static final int COAL_TICKS = 20 * 30;
    public static final int SLOT_COAL = 0, SLOT_WATER = 1;
    // GUI へ送る数値。同期は short（最大32767）なので、W は「0.1kW 単位」、J は「kJ 単位」に縮めて送る
    public static final int DATA_BURN = 0, DATA_BURN_TOTAL = 1, DATA_WATER = 2, DATA_DECI_KW = 3, DATA_KJ = 4, DATA_COUNT = 5;
    private final SimpleContainer container = new SimpleContainer(2) { @Override public void setChanged() { super.setChanged(); SteamEngineBlockEntity.this.setChanged(); } };
    private int burnTicks, waterUnits;   // waterUnits: 石炭何個分の水が残っているか
    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> COAL_TICKS; case DATA_WATER -> waterUnits;
            case DATA_DECI_KW -> (int) Math.round(getLastWatts() / 100); case DATA_KJ -> (int) (storage.getStoredJ() / 1000); default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public SteamEngineBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.STEAM_ENGINE, pos, state, 200_000, WATTS); }
    public SimpleContainer getContainer() { return container; }
    public int getBurnTicks() { return burnTicks; }
    public int getWaterUnits() { return waterUnits; }
    public static boolean isFuel(ItemStack s) { return s.is(Items.COAL) || s.is(Items.CHARCOAL); }

    @Override protected double produceWatts(Level level, BlockPos pos, BlockState state) {
        if (burnTicks <= 0) {
            ItemStack water = container.getItem(SLOT_WATER);
            if (waterUnits <= 0 && water.is(Items.WATER_BUCKET)) { container.setItem(SLOT_WATER, new ItemStack(Items.BUCKET)); waterUnits = 2; }
            ItemStack coal = container.getItem(SLOT_COAL);
            if (waterUnits > 0 && isFuel(coal)) { coal.shrink(1); container.setChanged(); waterUnits--; burnTicks = COAL_TICKS; }
        }
        if (burnTicks <= 0) return 0;
        burnTicks--;
        return WATTS;
    }
    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.steam_engine"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new SteamEngineMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition)); }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, container.getItems()); out.putInt("Burn", burnTicks); out.putInt("Water", waterUnits); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); container.getItems().replaceAll(s -> ItemStack.EMPTY); ContainerHelper.loadAllItems(in, container.getItems()); burnTicks = in.getIntOr("Burn", 0); waterUnits = in.getIntOr("Water", 0); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) net.minecraft.world.Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
}
