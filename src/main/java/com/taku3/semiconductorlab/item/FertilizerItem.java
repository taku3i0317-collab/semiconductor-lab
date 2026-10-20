package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.NitrogenFarmlandBlock;
import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 化学肥料（硫酸アンモニウム）[6-09]。耕した土（耕地）に使うと、窒素の多い耕地（[0-05] エンドウと同じ、3回分）になる。
 * 窒素の多い耕地にさらに使うと、3回分にもどす。マメの根粒菌が空気の窒素を土に入れるのを、工場でしたのと同じ。
 */
public class FertilizerItem extends Item {
    public FertilizerItem(Properties properties) { super(properties); }

    /** 耕地を肥やす（テストからも呼ぶ）。できたら true */
    public static boolean fertilize(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        BlockState rich = Survival.NITROGEN_FARMLAND.defaultBlockState().setValue(NitrogenFarmlandBlock.NITROGEN, NitrogenFarmlandBlock.MAX_NITROGEN);
        if (s.is(Blocks.FARMLAND)) {
            rich = rich.setValue(FarmlandBlock.MOISTURE, s.getValue(FarmlandBlock.MOISTURE));
        } else if (s.is(Survival.NITROGEN_FARMLAND)) {
            if (s.getValue(NitrogenFarmlandBlock.NITROGEN) >= NitrogenFarmlandBlock.MAX_NITROGEN) return false;
            rich = s.setValue(NitrogenFarmlandBlock.NITROGEN, NitrogenFarmlandBlock.MAX_NITROGEN);
        } else return false;
        level.setBlock(pos, rich, 3);
        level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.6f, 1.0f);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!fertilize(level, ctx.getClickedPos())) return InteractionResult.PASS;
        ctx.getItemInHand().shrink(1);
        if (ctx.getPlayer() != null) ctx.getPlayer().sendOverlayMessage(Component.translatable("message.semiconductor-lab.ammonium_sulfate.fertilized"));
        return InteractionResult.SUCCESS;
    }
}
