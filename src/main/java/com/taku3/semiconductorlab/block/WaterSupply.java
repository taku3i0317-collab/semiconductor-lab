package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Construction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * 上水道 [12-06]・塩素消毒 [12-07] のしらべ方（IMP3、O-032・S-013）。
 * 蛇口からつながった管（鋳鉄管・砂ろ過の槽・塩素の注入器）をたどり、どこかが水源（水の源のブロック）にふれていれば水が来る。
 * 砂ろ過の槽を通ると「ろ過した水」、さらに薬の入った塩素の注入器があると「消毒した水」（にごったまま塩素を入れても、にごりは取れない）。
 * 下水道 [12-08]：水源（取り入れ口）の近く（{@link #CESSPIT_RANGE} ブロック以内）に、下水の管で遠くへ流していない汚水だめがあると、
 * 汚れが地面から水にしみこみ、水の質が1段下がる（1854 ブロード街の井戸は、すぐそばの汚水だめからしみた）。
 * 近似：管のどこを通ったかの順番は見ず、つながった管の中にあるかだけを見る。
 */
public final class WaterSupply {
    private WaterSupply() {}

    /** たどる管の数の上限（重くしない） */
    public static final int MAX_PIPES = 256;

    public enum Quality { NONE, MURKY, FILTERED, SAFE }

    /** 取り入れ口から、汚水だめを探す距離 */
    public static final int CESSPIT_RANGE = 6;
    /** 下水の管がこれより遠く（汚水だめから）までのびていれば、汚れは運び出された */
    public static final int SEWER_REACH = 16;

    /** 調べた結果：水の質と、使う塩素の注入器（無ければ null） */
    public record Result(Quality quality, BlockPos chlorinator) {}

    static boolean isPipe(BlockState s) {
        return s.is(Construction.CAST_IRON_PIPE) || s.is(Construction.SAND_FILTER) || s.is(Construction.CHLORINATOR);
    }

    public static Result trace(Level level, BlockPos tap) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        boolean water = false, filter = false;
        BlockPos intake = null;
        BlockPos chlor = null;
        for (Direction d : Direction.values()) {
            BlockPos n = tap.relative(d);
            if (isPipe(level.getBlockState(n)) && seen.add(n)) queue.add(n);
        }
        while (!queue.isEmpty() && seen.size() <= MAX_PIPES) {
            BlockPos at = queue.poll();
            BlockState s = level.getBlockState(at);
            if (s.is(Construction.SAND_FILTER)) filter = true;
            if (s.is(Construction.CHLORINATOR) && s.getValue(ChlorinatorBlock.DOSES) > 0) chlor = at;
            for (Direction d : Direction.values()) {
                BlockPos n = at.relative(d);
                var f = level.getFluidState(n);
                if (f.is(FluidTags.WATER) && f.isSource()) { water = true; if (intake == null) intake = n; }
                if (isPipe(level.getBlockState(n)) && seen.add(n)) queue.add(n);
            }
        }
        if (!water) return new Result(Quality.NONE, null);
        boolean sewage = nearCesspit(level, intake);
        if (!filter) return new Result(Quality.MURKY, null);
        if (chlor == null) return new Result(sewage ? Quality.MURKY : Quality.FILTERED, null);
        return sewage ? new Result(Quality.FILTERED, chlor) : new Result(Quality.SAFE, chlor);
    }

    /** 取り入れ口の近くに、下水道につないでいない汚水だめがあるか */
    public static boolean nearCesspit(Level level, BlockPos intake) {
        for (BlockPos q : BlockPos.betweenClosed(intake.offset(-CESSPIT_RANGE, -CESSPIT_RANGE, -CESSPIT_RANGE), intake.offset(CESSPIT_RANGE, CESSPIT_RANGE, CESSPIT_RANGE)))
            if (level.getBlockState(q).is(Construction.CESSPIT) && !drained(level, q.immutable())) return true;
        return false;
    }

    /** 汚水だめから下水の管をたどって、{@link #SEWER_REACH} ブロックより遠くまで流れていくか（＝汚れを町の外へ運び出している） */
    public static boolean drained(Level level, BlockPos cesspit) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(cesspit);
        queue.add(cesspit);
        while (!queue.isEmpty() && seen.size() <= MAX_PIPES) {
            BlockPos at = queue.poll();
            if (at.distManhattan(cesspit) > SEWER_REACH) return true;
            for (Direction d : Direction.values()) {
                BlockPos n = at.relative(d);
                if (level.getBlockState(n).is(Construction.SEWER_PIPE) && seen.add(n)) queue.add(n);
            }
        }
        return false;
    }
}
