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
 * mixin/ItemEntityRettingMixin が、落ちているアイテムの tick ごとにここを呼ぶ。
 */
public final class Retting {
    /** 水に浸かっている時間（tick）。本物は川や池で1〜3週間 */
    public static final int RETTING_TICKS = 1200;

    /** 実際に使う時間（GameTest だけが短くする。ふだんは RETTING_TICKS） */
    public static int ticksForRetting = RETTING_TICKS;

    private Retting() {}

    /** 水に浸かっているか。水に浮いて上下にゆれている（足元が水）ときも浸かっているとみなす */
    public static boolean wet(ItemEntity e) {
        BlockPos pos = e.blockPosition();
        return e.isInWater() || e.level().getFluidState(pos).is(FluidTags.WATER) || e.level().getFluidState(pos.below()).is(FluidTags.WATER);
    }

    /** 1 tick 分すすめる。戻り値は新しい浸水時間（茎でなければ 0。水から出たら進まないが、たまった分は残る） */
    public static int tick(ItemEntity e, int soaked) {
        ItemStack st = e.getItem();
        if (!st.is(Survival.FLAX_STALKS)) return 0;
        if (!wet(e)) return soaked;
        if (++soaked < ticksForRetting) return soaked;
        e.setItem(new ItemStack(Survival.RETTED_FLAX, st.getCount()));
        e.level().playSound(null, e.blockPosition(), SoundEvents.BUBBLE_POP, SoundSource.NEUTRAL, 0.6f, 0.8f);
        return 0;
    }
}
