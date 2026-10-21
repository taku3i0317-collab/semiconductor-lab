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
 * [11-07] 硝酸アンモニウムも窒素肥料、[11-06] 過リン酸石灰はリン肥料（{@link Nutrient#PHOSPHORUS}：耕地にリン3回分、収穫が増える）（IMP3、O-020）。
 */
public class FertilizerItem extends Item {
    /** 何を土に入れる肥料か */
    public enum Nutrient { NITROGEN, PHOSPHORUS }
    private final Nutrient nutrient;

    public FertilizerItem(Properties properties) { this(properties, Nutrient.NITROGEN); }
    public FertilizerItem(Properties properties, Nutrient nutrient) { super(properties); this.nutrient = nutrient; }

    /** 窒素で耕地を肥やす（テストからも呼ぶ）。できたら true */
    public static boolean fertilize(Level level, BlockPos pos) { return fertilize(level, pos, Nutrient.NITROGEN); }

    /** 耕地を肥やす（テストからも呼ぶ）。ふつうの耕地は「肥えた耕地」になる。できたら true */
    public static boolean fertilize(Level level, BlockPos pos, Nutrient nutrient) {
        var prop = nutrient == Nutrient.NITROGEN ? NitrogenFarmlandBlock.NITROGEN : NitrogenFarmlandBlock.PHOSPHORUS;
        int max = nutrient == Nutrient.NITROGEN ? NitrogenFarmlandBlock.MAX_NITROGEN : NitrogenFarmlandBlock.MAX_PHOSPHORUS;
        BlockState s = level.getBlockState(pos);
        BlockState rich;
        if (s.is(Blocks.FARMLAND)) {
            rich = Survival.NITROGEN_FARMLAND.defaultBlockState().setValue(NitrogenFarmlandBlock.NITROGEN, 0).setValue(NitrogenFarmlandBlock.PHOSPHORUS, 0)
                    .setValue(prop, max).setValue(FarmlandBlock.MOISTURE, s.getValue(FarmlandBlock.MOISTURE));
        } else if (s.is(Survival.NITROGEN_FARMLAND)) {
            if (s.getValue(prop) >= max) return false;
            rich = s.setValue(prop, max);
        } else return false;
        level.setBlock(pos, rich, 3);
        level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.6f, 1.0f);
        return true;
    }

    /** [11-05] 堆肥で耕地を肥やす（コンポスターの骨粉、テストからも呼ぶ）：窒素もリンも1回分ずつ（化学肥料の 1/3）。もう両方いっぱいなら false */
    public static final int COMPOST_SHARE = 1;
    public static boolean compost(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        BlockState rich;
        if (s.is(Blocks.FARMLAND)) {
            rich = Survival.NITROGEN_FARMLAND.defaultBlockState().setValue(NitrogenFarmlandBlock.NITROGEN, COMPOST_SHARE).setValue(NitrogenFarmlandBlock.PHOSPHORUS, COMPOST_SHARE)
                    .setValue(FarmlandBlock.MOISTURE, s.getValue(FarmlandBlock.MOISTURE));
        } else if (s.is(Survival.NITROGEN_FARMLAND)) {
            int n = s.getValue(NitrogenFarmlandBlock.NITROGEN), p = s.getValue(NitrogenFarmlandBlock.PHOSPHORUS);
            if (n >= NitrogenFarmlandBlock.MAX_NITROGEN && p >= NitrogenFarmlandBlock.MAX_PHOSPHORUS) return false;
            rich = s.setValue(NitrogenFarmlandBlock.NITROGEN, Math.min(NitrogenFarmlandBlock.MAX_NITROGEN, n + COMPOST_SHARE))
                    .setValue(NitrogenFarmlandBlock.PHOSPHORUS, Math.min(NitrogenFarmlandBlock.MAX_PHOSPHORUS, p + COMPOST_SHARE));
        } else return false;
        level.setBlock(pos, rich, 3);
        level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.6f, 0.8f);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!fertilize(level, ctx.getClickedPos(), nutrient)) return InteractionResult.PASS;
        ctx.getItemInHand().shrink(1);
        if (ctx.getPlayer() != null) ctx.getPlayer().sendOverlayMessage(Component.translatable(nutrient == Nutrient.NITROGEN
                ? "message.semiconductor-lab.ammonium_sulfate.fertilized" : "message.semiconductor-lab.superphosphate.fertilized"));
        return InteractionResult.SUCCESS;
    }
}
