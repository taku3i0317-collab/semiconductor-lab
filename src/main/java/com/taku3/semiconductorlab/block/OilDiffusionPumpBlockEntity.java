package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Vacuum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 油拡散ポンプ [5-06]（1928 バーチが低蒸気圧の油で水銀を置きかえ、1930年代 ヒックマンの分留型。IMP4-01、O-011）：
 * しくみは水銀拡散ポンプ [5-05] と同じ（油を沸かし、蒸気の噴流で気体分子をたたき落として前段の回転ポンプへ送る）。
 * 水銀より毒が少なく、鋼の胴で大きく作れる＝速く引ける（{@link #RATIO}）・到達圧力も低い（{@link #LIMIT_PA}）。
 * 油はシリコーン油（[5-04] の③、ロチョウの直接法）。前段・状態・画面は水銀拡散ポンプと共通（継承）。
 * 油の蒸気も少し容器に戻る（バックストリーミング）＝ TurboPumpBlockEntity.backstreams がそのまま当てはまる。
 * ⚠️ 本物の拡散ポンプ油はフェニル基の付いたシリコーン（DC-704 など）や石油の油（アピエゾン）。ここでは段階③のシリコーン油1種で近似（oil_diffusion_pump.placeholder）。
 */
public class OilDiffusionPumpBlockEntity extends DiffusionPumpBlockEntity {
    public static final double WATTS = 1_000;
    /** 到達圧力（Pa）。油拡散ポンプは 10⁻⁵〜10⁻⁶Pa 級 */
    public static final double LIMIT_PA = 1e-5;
    /** 1秒ごとに残る割合（水銀拡散ポンプは 0.5）。口が大きいので速い */
    public static final double RATIO = 0.3;

    public OilDiffusionPumpBlockEntity(BlockPos pos, BlockState state) {
        super(Vacuum.OIL_DIFFUSION_PUMP.type, pos, state, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }

    @Override protected void finishWork() {
        VacuumChamberBlockEntity c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        if (c != null) c.pumpStroke(LIMIT_PA, RATIO);
    }
}
