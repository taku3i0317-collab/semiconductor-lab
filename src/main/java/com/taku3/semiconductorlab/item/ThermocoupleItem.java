package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 熱電対 [3-09]（1821 ゼーベック：2種の金属の接点に温度差があると電圧が出る。IMP4-01、S-009）：銅と鉄の線の先を合わせた温度計。
 * ブロックを右クリックすると、そこの温度と、出た電圧（mV）を出す。銅＋鉄は約 {@link #MAX_C}℃ まで（それより熱いと銅が酸化して測れない）。
 * 温度の値はブロックの種類と火が付いているかで決める代表値 ⚠️。高い温度用の白金−白金ロジウム（1886 ル・シャトリエ）はロジウムが無いので作らない。
 */
public class ThermocoupleItem extends Item {
    public static final int MAX_C = 700;
    /** 火の入ったかまど（バニラ furnace）の温度。[3-02] の決定：≈1000℃ と固定（銅の融点 1085℃ より上はふいご [3-04]） */
    public static final int FURNACE_C = 1000;
    /** 火の入ったバニラの溶鉱炉（blast_furnace）。[3-05] の決定：高温の窯 ≈1450℃（本物の高炉はマルチブロック [1-09]） */
    public static final int BLAST_FURNACE_C = 1450;
    /** 銅−鉄の熱起電力（µV/℃、0〜数百℃ の平均 ⚠️） */
    public static final double MICROVOLT_PER_C = 13.0;
    private static final String K = "item.semiconductor-lab.thermocouple.";

    public ThermocoupleItem(Properties properties) { super(properties); }

    /** ブロックの代表の温度（℃）。わからなければ 20（部屋の温度） */
    public static int temperatureC(BlockState s) {
        boolean lit = s.hasProperty(BlockStateProperties.LIT) && s.getValue(BlockStateProperties.LIT);
        if (s.is(Blocks.LAVA) || s.is(Blocks.LAVA_CAULDRON)) return 1150;
        if (s.is(Blocks.MAGMA_BLOCK)) return 900;
        if (s.is(Blocks.FIRE) || s.is(Blocks.SOUL_FIRE)) return 800;
        if ((s.is(Blocks.FURNACE) || s.is(Blocks.BLAST_FURNACE) || s.is(Blocks.SMOKER)) && lit) return s.is(Blocks.BLAST_FURNACE) ? BLAST_FURNACE_C : s.is(Blocks.FURNACE) ? FURNACE_C : 250;   // かまど ≈1000℃ は [3-02] の決定
        if ((s.is(Blocks.CAMPFIRE) || s.is(Blocks.SOUL_CAMPFIRE)) && lit) return 650;
        if (s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH)) return 500;
        if (s.is(Blocks.ICE) || s.is(Blocks.PACKED_ICE) || s.is(Blocks.BLUE_ICE) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.POWDER_SNOW)) return 0;
        if (s.is(Blocks.WATER) || s.is(Blocks.WATER_CAULDRON)) return 15;
        if (lit) return 300;   // MOD の機械など、動いて光っている物（⚠️ ざっくり）
        return 20;
    }

    /** 冷たい側（20℃）とのちがいで出る電圧（mV） */
    public static double millivolts(int c) { return (c - 20) * MICROVOLT_PER_C / 1000.0; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!ctx.getLevel().isClientSide() && ctx.getPlayer() != null) {
            int c = temperatureC(ctx.getLevel().getBlockState(ctx.getClickedPos()));
            ctx.getPlayer().sendOverlayMessage(c > MAX_C ? Component.translatable(K + "over", MAX_C)
                    : Component.translatable(K + "reading", c, String.format(java.util.Locale.ROOT, "%.2f", millivolts(c))));
        }
        return InteractionResult.SUCCESS;
    }
}
