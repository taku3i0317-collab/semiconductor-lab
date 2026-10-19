package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.Purity;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 帯域精製炉の中身。1パス（30秒）ごとに純度が1段上がる：未精製 → 6N → 9N → 11N */
public class ZoneRefiningFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TOTAL_TICKS = 20 * 30;
    public static final int SLOT = 0;
    public static final int DATA_PROGRESS = 0, DATA_TOTAL = 1, DATA_NINES = 2;

    private final SimpleContainer container = new SimpleContainer(1) {
        @Override public void setChanged() {
            super.setChanged();
            progress = 0;                                  // 中身が変わったら最初から
            ZoneRefiningFurnaceBlockEntity.this.setChanged();
        }
    };
    private int progress;

    /** 画面に送る数値：進み具合・合計・今の純度のN数 */
    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case DATA_PROGRESS -> progress;
                case DATA_TOTAL -> TOTAL_TICKS;
                case DATA_NINES -> currentNines();
                default -> 0;
            };
        }
        @Override public void set(int i, int v) { if (i == DATA_PROGRESS) progress = v; }
        @Override public int getCount() { return 3; }
    };

    public ZoneRefiningFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZONE_REFINING_FURNACE, pos, state);
    }

    public SimpleContainer getContainer() { return container; }
    public ItemStack getItem() { return container.getItem(SLOT); }

    public static boolean isGermanium(ItemStack s) {
        return s.is(ModItems.RAW_GERMANIUM) || s.is(ModItems.REFINED_GERMANIUM);
    }

    /** 次の純度段階。これ以上上がらないなら -1 */
    public static double nextPurity(ItemStack s) {
        if (s.is(ModItems.RAW_GERMANIUM)) return Purity.SIX_N;
        Double p = s.get(ModDataComponents.PURITY);
        if (p == null) return -1;
        if (p < Purity.NINE_N) return Purity.NINE_N;
        if (p < Purity.ELEVEN_N) return Purity.ELEVEN_N;
        return -1;
    }

    private int currentNines() {
        ItemStack s = getItem();
        if (s.isEmpty()) return -1;
        Double p = s.get(ModDataComponents.PURITY);
        return p == null ? 0 : Purity.nines(p);
    }

    public boolean canRefine() {
        ItemStack s = getItem();
        return !s.isEmpty() && isGermanium(s) && nextPurity(s) > 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ZoneRefiningFurnaceBlockEntity be) {
        if (level.isClientSide()) return;
        boolean working = be.canRefine();
        if (working) {
            be.progress++;
            if (be.progress >= TOTAL_TICKS) {
                ItemStack in = be.getItem();
                ItemStack out = new ItemStack(ModItems.REFINED_GERMANIUM, in.getCount());
                out.set(ModDataComponents.PURITY, nextPurity(in));
                be.container.setItem(SLOT, out);   // listener が progress を 0 に戻す
            }
            be.setChanged();
        } else if (be.progress != 0) {
            be.progress = 0;
            be.setChanged();
        }
        if (state.getValue(ZoneRefiningFurnaceBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(ZoneRefiningFurnaceBlock.LIT, working), 3);
        }
    }

    /** 壊されたとき中身を落とす */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) Containers.dropContents(level, pos, container.getItems());
        super.preRemoveSideEffects(pos, state);
    }

    // ---- MenuProvider（画面を開くための情報）----
    @Override
    public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.zone_refining_furnace"); }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ZoneRefiningFurnaceMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().set(SLOT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        progress = in.getIntOr("Progress", 0);
    }
}