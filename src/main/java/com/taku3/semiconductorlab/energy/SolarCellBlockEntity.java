package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModMachines;
import com.taku3.semiconductorlab.block.SaltPanBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 太陽電池 [B-12]（1954 ベル研のチェイピン・フラー・ピアソン、効率約6%）。p-n 接合の板を並べたパネル。
 * 真上に空が開いていて（ガラスは通す）、昼なら {@link #WATTS} W を出す。雨（曇り空）の日は、雲を通った散らばった光（散乱光）で晴れの約2割（{@link #RAIN_FACTOR}）。夜・屋根の下は 0。
 * 60W の根拠：真昼の日光 約1000W/m² × 効率 6% × 1ブロック（1m²）＝ 60W。1954 年の発表「1平方ヤード（0.84m²）あたり約50W」とも合う（ETHW）。
 * ⚠️ 近似：太陽の高さ・雲の厚さで細かく変えない（昼か夜か、雨かだけ）。雨の日の2割は目安（厚い雲では1割以下、薄い雲ではもっと多い ⚠️）。
 */
public class SolarCellBlockEntity extends GeneratorBlockEntity {
    public static final double SUNLIGHT_W_PER_M2 = 1000, EFFICIENCY = 0.06;
    public static final double WATTS = SUNLIGHT_W_PER_M2 * EFFICIENCY;
    /** 雨（曇り空）の日の散乱光：晴れの約2割 */
    public static final double RAIN_FACTOR = 0.2;

    public SolarCellBlockEntity(BlockPos pos, BlockState state) { super(ModMachines.SOLAR_CELL.type, pos, state, 2_000, WATTS); }

    /** 空・昼・雨から出力を決める（GameTest からも呼ぶ） */
    public static double watts(boolean openSky, boolean bright, boolean raining) {
        if (!openSky || !bright) return 0;
        return raining ? WATTS * RAIN_FACTOR : WATTS;
    }

    @Override protected double produceWatts(Level level, BlockPos pos, BlockState state) {
        return watts(SaltPanBlockEntity.openSky(level, pos), level.isBrightOutside(), level.isRainingAt(pos.above()));
    }
}
