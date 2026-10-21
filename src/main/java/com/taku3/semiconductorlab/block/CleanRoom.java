package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Construction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * 層流クリーンルーム [12-14]（IMP2 S-003、1962 ホイットフィールド）。A と決めた形：{@link #classAt} が等級を返し、
 * マスクアライナー・ステッパーが露光のあとに ChipOps.addDust(w, {@link Grade#dust}) を呼ぶ（失敗にはしない、洗浄台で落ちる）。
 * <p>判定：pos の上の空気から部屋の中を数えて（{@value #MAX_AIR} マスまで、外につながっていたら部屋ではない）、
 * 天井（部屋の空気のすぐ上のブロック）のうち HEPA の天井の割合と、部屋の中（{@link AirConditionerBlockEntity#RANGE} 以内）に
 * HEPA フィルター入りの空調機が動いているかで決める。</p>
 * <ul>
 * <li>クラス100 相当：天井の {@value #CLASS100_COVER}％以上が HEPA（天井全体から床へ一方向に流す＝層流）</li>
 * <li>クラス1000 相当：天井に HEPA が {@value #CLASS1000_COVER}％以上</li>
 * <li>それ以外・空調なし・囲まれていない：外（NONE）</li>
 * </ul>
 * ⚠️ 等級の境目（割合）はゲームの値。本物の等級は 1 立方フィートあたりの 0.5µm 以上の粒の数（米国連邦規格 209、1963）。
 */
public final class CleanRoom {
    private CleanRoom() {}

    public static final int MAX_AIR = 512;
    public static final int CLASS100_COVER = 80, CLASS1000_COVER = 25;

    /**
     * 部屋の等級と、露光のあとに足す汚れ（A の目安：外 +2、クラス1000 +1、クラス100 0）、
     * v0.5 #4 歩留まりの欠陥の密度 D（個/cm²、IMP2 O-025）。⚠️ ゲームの代表値：1970年代の工場で D は数個/cm²、
     * クリーンルームが良くなるほど空気中の粒（＝チップに落ちる欠陥）が減り、等級が1つ上がる（粒が1/10）と D もおよそ下がる
     */
    public enum Grade {
        NONE(2, 5.0), CLASS_1000(1, 1.0), CLASS_100(0, 0.2);
        public final int dust;
        /** 欠陥の密度 D（個/cm²） */
        public final double defectsPerCm2;
        Grade(int dust, double defectsPerCm2) { this.dust = dust; this.defectsPerCm2 = defectsPerCm2; }
    }

    /** v0.5 #4（IMP2 O-025）：その場所の欠陥の密度 D（個/cm²）。歩留まりの式（A の litho）に入れる入口 */
    public static double defectDensity(Level level, BlockPos pos) { return classAt(level, pos).defectsPerCm2; }

    /** 歩留まりの目安（ポアソンのモデル）Y ＝ e^(−D×A)。A＝チップの面積（cm²）。式をチップにどう効かせるかは A の litho */
    public static double poissonYield(double defectsPerCm2, double areaCm2) { return Math.exp(-defectsPerCm2 * areaCm2); }

    /** ①（IMP2 O-025）：露光以外の工程（拡散炉・CVD・CMP など）が、仕上げたあと ChipOps.addDust(w, dustAt(level, pos)) で使う入口 */
    public static int dustAt(Level level, BlockPos pos) { return classAt(level, pos).dust; }

    public static Grade classAt(Level level, BlockPos pos) {
        Set<BlockPos> air = room(level, pos.above());
        if (air == null) return Grade.NONE;
        int ceiling = 0, hepa = 0;
        for (BlockPos a : air) {
            BlockPos up = a.above();
            if (air.contains(up)) continue;
            ceiling++;
            if (level.getBlockState(up).is(Construction.HEPA_CEILING)) hepa++;
        }
        if (ceiling == 0 || hepa == 0 || !filteredAir(level, pos)) return Grade.NONE;
        int cover = hepa * 100 / ceiling;
        if (cover >= CLASS100_COVER) return Grade.CLASS_100;
        if (cover >= CLASS1000_COVER) return Grade.CLASS_1000;
        return Grade.NONE;
    }

    /** start から空気をたどった部屋の中。外につながる（MAX_AIR を超える）なら null */
    static Set<BlockPos> room(Level level, BlockPos start) {
        if (!level.getBlockState(start).isAir()) return null;
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        seen.add(start.immutable()); q.add(start.immutable());
        while (!q.isEmpty()) {
            BlockPos c = q.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = c.relative(d);
                if (seen.contains(n) || !level.getBlockState(n).isAir()) continue;
                if (seen.size() >= MAX_AIR) return null;
                seen.add(n); q.add(n);
            }
        }
        return seen;
    }

    /** 近くに、HEPA フィルター入りで動いている空調機があるか */
    static boolean filteredAir(Level level, BlockPos pos) {
        int r = AirConditionerBlockEntity.RANGE;
        for (BlockPos q : BlockPos.betweenClosed(pos.offset(-r, -r, -r), pos.offset(r, r, r)))
            if (level.getBlockEntity(q) instanceof AirConditionerBlockEntity ac && ac.getLastWatts() > 0 && ac.filtered()) return true;
        return false;
    }
}
