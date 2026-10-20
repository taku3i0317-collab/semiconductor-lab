package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 亜麻の浸水（レッティング）[0-06]。水の中に落ちている「亜麻の茎」は、浸かっている時間が RETTING_TICKS たまると
 * 「浸水した亜麻の茎」になる（水の中の微生物が、繊維どうしをくっつけているペクチンを分解する）。陸の上では変わらない。
 * 黄鉄鉱 [2-43] も同じしくみで、水の中に WEATHERING_TICKS 置くと風化して緑礬になる。
 * mixin/ItemEntityRettingMixin が、落ちているアイテムの tick ごとにここを呼ぶ。
 */
public final class Retting {
    /** 水に浸かっている時間（tick）。本物は川や池で1〜3週間 */
    public static final int RETTING_TICKS = 1200;

    /** 実際に使う時間（GameTest だけが短くする。ふだんは RETTING_TICKS） */
    public static int ticksForRetting = RETTING_TICKS;

    /** [2-43] 黄鉄鉱が水の中で風化して緑礬になるまでの時間（tick）。本物の工場は何年も雨にさらした */
    public static final int WEATHERING_TICKS = 2400;
    public static int ticksForWeathering = WEATHERING_TICKS;

    private Retting() {}

    /** 水に浸かっているか。水に浮いて上下にゆれている（足元が水）ときも浸かっているとみなす */
    public static boolean wet(ItemEntity e) {
        BlockPos pos = e.blockPosition();
        return e.isInWater() || e.level().getFluidState(pos).is(FluidTags.WATER) || e.level().getFluidState(pos.below()).is(FluidTags.WATER);
    }

    /** 1 tick 分すすめる。戻り値は新しい浸水時間（茎でなければ 0。水から出たら進まないが、たまった分は残る） */
    public static int tick(ItemEntity e, int soaked) {
        ItemStack st = e.getItem();
        boolean pyrite = st.is(com.taku3.semiconductorlab.content.Chemistry.PYRITE);
        if (!st.is(Survival.FLAX_STALKS) && !pyrite) return 0;
        if (!wet(e)) return soaked;
        if (++soaked < (pyrite ? ticksForWeathering : ticksForRetting)) return soaked;
        // [2-43] 黄鉄鉱は水と空気で風化して緑礬になる（2FeS2 + 7O2 + 2H2O → 2FeSO4 + 2H2SO4、コッパラス工場の近似）
        e.setItem(new ItemStack(pyrite ? com.taku3.semiconductorlab.content.Chemistry.GREEN_VITRIOL : Survival.RETTED_FLAX, st.getCount()));
        e.level().playSound(null, e.blockPosition(), SoundEvents.BUBBLE_POP, SoundSource.NEUTRAL, 0.6f, 0.8f);
        return 0;
    }
}
