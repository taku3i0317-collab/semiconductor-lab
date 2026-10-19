package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 帯域精製炉。精製（未精製→6N→9N→10N）と、ゾーンレベリング（9N以上＋ドーパント→n型/p型） */
public class ZoneRefiningFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TOTAL_TICKS = 20 * 30;
    public static final int GE_SLOT = 0, DOPANT_SLOT = 1;
    public static final int DATA_PROGRESS = 0, DATA_TOTAL = 1, DATA_NINES = 2, DATA_MODE = 3;
    public static final int MODE_IDLE = 0, MODE_REFINE = 1, MODE_DOPE = 2;

    private final SimpleContainer container = new SimpleContainer(2) {
        @Override public void setChanged() { super.setChanged(); progress = 0; ZoneRefiningFurnaceBlockEntity.this.setChanged(); }
    };
    private int progress;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) { case DATA_PROGRESS -> progress; case DATA_TOTAL -> TOTAL_TICKS; case DATA_NINES -> currentNines(); case DATA_MODE -> mode(); default -> 0; }; }
        @Override public void set(int i, int v) { if (i == DATA_PROGRESS) progress = v; }
        @Override public int getCount() { return 4; }
    };

    public ZoneRefiningFurnaceBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ZONE_REFINING_FURNACE, pos, state); }
    public SimpleContainer getContainer() { return container; }

    public static boolean isGermanium(ItemStack s) { return s.is(ModItems.RAW_GERMANIUM) || s.is(ModItems.REFINED_GERMANIUM); }
    public static boolean isDopant(ItemStack s) { return s.is(ModItems.PHOSPHORUS) || s.is(ModItems.INDIUM); }
    public static boolean isTransistorGrade(ItemStack s) { Double p = s.get(ModDataComponents.PURITY); return s.is(ModItems.REFINED_GERMANIUM) && p != null && p >= Purity.NINE_N; }

    /** 次の純度段階。上がらないなら -1 */
    public static double nextPurity(ItemStack s) {
        if (s.is(ModItems.RAW_GERMANIUM)) return Purity.SIX_N;
        Double p = s.get(ModDataComponents.PURITY);
        if (p == null) return -1;
        if (p < Purity.NINE_N) return Purity.NINE_N;
        if (p < Purity.TEN_N) return Purity.TEN_N;
        return -1;
    }
    private int currentNines() { ItemStack s = container.getItem(GE_SLOT); if (s.isEmpty()) return -1; Double p = s.get(ModDataComponents.PURITY); return p == null ? 0 : Purity.nines(p); }
    private int mode() {
        ItemStack ge = container.getItem(GE_SLOT), dp = container.getItem(DOPANT_SLOT);
        if (ge.isEmpty()) return MODE_IDLE;
        if (isTransistorGrade(ge) && isDopant(dp)) return MODE_DOPE;
        if (isGermanium(ge) && nextPurity(ge) > 0) return MODE_REFINE;
        return MODE_IDLE;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ZoneRefiningFurnaceBlockEntity be) {
        if (level.isClientSide()) return;
        int mode = be.mode();
        if (mode != MODE_IDLE) {
            be.progress++;
            if (be.progress >= TOTAL_TICKS) {
                ItemStack ge = be.container.getItem(GE_SLOT);
                if (mode == MODE_REFINE) {
                    ItemStack out = new ItemStack(ModItems.REFINED_GERMANIUM, ge.getCount());
                    out.set(ModDataComponents.PURITY, nextPurity(ge));
                    be.container.setItem(GE_SLOT, out);
                } else {
                    ItemStack dp = be.container.getItem(DOPANT_SLOT);
                    ItemStack out = new ItemStack(dp.is(ModItems.PHOSPHORUS) ? ModItems.N_TYPE_GERMANIUM : ModItems.P_TYPE_GERMANIUM, ge.getCount());
                    dp.shrink(1);
                    be.container.setItem(DOPANT_SLOT, dp.isEmpty() ? ItemStack.EMPTY : dp);
                    be.container.setItem(GE_SLOT, out);
                }
            }
            be.setChanged();
        } else if (be.progress != 0) { be.progress = 0; be.setChanged(); }
        boolean lit = mode != MODE_IDLE;
        if (state.getValue(ZoneRefiningFurnaceBlock.LIT) != lit) level.setBlock(pos, state.setValue(ZoneRefiningFurnaceBlock.LIT, lit), 3);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.zone_refining_furnace"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new ZoneRefiningFurnaceMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition)); }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, container.getItems()); out.putInt("Progress", progress); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); container.getItems().replaceAll(s -> ItemStack.EMPTY); ContainerHelper.loadAllItems(in, container.getItems()); progress = in.getIntOr("Progress", 0); }
}