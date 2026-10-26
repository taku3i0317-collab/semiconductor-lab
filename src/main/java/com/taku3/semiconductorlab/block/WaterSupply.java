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
 * 近似：管のどこを通ったかの順番は見ず、つながった管の中にあるかだけを見る。
 */
public final class WaterSupply {
    private WaterSupply() {}

    /** たどる管の数の上限（重くしない） */
    public static final int MAX_PIPES = 256;

    public enum Quality { NONE, MURKY, FILTERED, SAFE }

    /** 調べた結果：水の質と、使う塩素の注入器（無ければ null） */
    public record Result(Quality quality, BlockPos chlorinator) {}

    static boolean isPipe(BlockState s) {
        return s.is(Construction.CAST_IRON_PIPE) || s.is(Construction.SAND_FILTER) || s.is(Construction.CHLORINATOR);
    }

    public static Result trace(Level level, BlockPos tap) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        boolean water = false, filter = false;
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
                if (f.is(FluidTags.WATER) && f.isSource()) water = true;
                if (isPipe(level.getBlockState(n)) && seen.add(n)) queue.add(n);
            }
        }
        if (!water) return new Result(Quality.NONE, null);
        if (!filter) return new Result(Quality.MURKY, null);
        return chlor != null ? new Result(Quality.SAFE, chlor) : new Result(Quality.FILTERED, null);
    }
}
