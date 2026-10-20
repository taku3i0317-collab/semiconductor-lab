package com.taku3.semiconductorlab.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 測量の1つ目の測点（[12-11] 三角測量、IMP2 O-007）。セオドライトに記録する：立った場所（x, z）と、目標をのぞいた向き（yaw、度）。
 */
public record SurveyStation(double x, double z, float yaw) {
    public static final Codec<SurveyStation> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("x").forGetter(SurveyStation::x),
            Codec.DOUBLE.fieldOf("z").forGetter(SurveyStation::z),
            Codec.FLOAT.fieldOf("yaw").forGetter(SurveyStation::yaw)
    ).apply(i, SurveyStation::new));
}
