package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.Purity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 帯域精製炉の中身。1パス（30秒）ごとに純度が1段上がる：未精製 → 6N → 9N → 11N */
public class ZoneRefiningFurnaceBlockEntity extends BlockEntity {
    public static final int TOTAL_TICKS = 20 * 30;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int progress;

    public ZoneRefiningFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZONE_REFINING_FURNACE, pos, state);
    }

    public ItemStack getItem() { return items.get(0); }
    public void setItem(ItemStack s) { items.set(0, s); progress = 0; setChanged(); }
    public int getProgress() { return progress; }

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

    public boolean canRefine() {
        ItemStack s = getItem();
        return !s.isEmpty() && isGermanium(s) && nextPurity(s) > 0;
    }

    /** 毎tick呼ばれる（サーバー側だけ処理する） */
    public static void tick(Level level, BlockPos pos, BlockState state, ZoneRefiningFurnaceBlockEntity be) {
        if (level.isClientSide()) return;
        boolean working = be.canRefine();
        if (working) {
            be.progress++;
            if (be.progress >= TOTAL_TICKS) {
                ItemStack in = be.getItem();
                ItemStack out = new ItemStack(ModItems.REFINED_GERMANIUM, in.getCount());
                out.set(ModDataComponents.PURITY, nextPurity(in));
                be.items.set(0, out);
                be.progress = 0;
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

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);
        out.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, items);
        progress = in.getIntOr("Progress", 0);
    }
}