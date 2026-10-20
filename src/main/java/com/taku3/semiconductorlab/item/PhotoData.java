package com.taku3.semiconductorlab.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 写真の中身（[B-02] 銀塩写真、IMP2、S-002 案B）。絵ではなく「撮った場所・時刻・写りの良さ」を記録する。
 * exposure＝ちょうどよい露光を 100 とした露光の量（％）。kind＝"daguerreotype"（銀板）か "wet_plate"（湿板）。
 */
public record PhotoData(String place, long dayTime, int exposure, String kind) {
    public static final Codec<PhotoData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("place").forGetter(PhotoData::place),
            Codec.LONG.fieldOf("day_time").forGetter(PhotoData::dayTime),
            Codec.INT.fieldOf("exposure").forGetter(PhotoData::exposure),
            Codec.STRING.optionalFieldOf("kind", "daguerreotype").forGetter(PhotoData::kind)
    ).apply(i, PhotoData::new));

    /** 写り：うすい（露光不足）／ちょうどよい／黒い（露光オーバー）。露光の量が 50〜200％ならちょうどよい */
    public String quality() { return exposure < 50 ? "thin" : exposure > 200 ? "dark" : "good"; }
}
