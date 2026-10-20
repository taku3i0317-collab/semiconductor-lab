package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * マスク（データコンポーネント mask、アイテム rubylith＝原図・photomask＝フォトマスク。LITHO_DESIGN §4.3）。
 * cells＝プレイヤーが描いた所（その物がある所）。透明・不透明の向きは ChipOps.expose が層の種類で決める。
 * design・index は「どの設計図の何枚目か」（表示用）。
 */
public record MaskData(int size, LayerKind kind, BitGrid cells, String design, int index, int artworkMm, int reduction) {
    /**
     * 原図の1辺（mm）と縮小率。本物は大きな原図（1960年代はルビリス＝赤いフィルムを切る）を写真で縮小してマスクにした。
     * 今は製図台で材料なしに描く近似（⚠️ INBOX S-001）なので 0（原図なし）・1（縮小なし）。IMP2 の縮小カメラ [7-11] がつなぐときに入れる
     */
    public MaskData(int size, LayerKind kind, BitGrid cells, String design, int index) { this(size, kind, cells, design, index, 0, 1); }

    public static final Codec<MaskData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(BitGrid.MIN, BitGrid.MAX).fieldOf("size").forGetter(MaskData::size),
            LayerKind.CODEC.fieldOf("kind").forGetter(MaskData::kind),
            BitGrid.CODEC.fieldOf("cells").forGetter(MaskData::cells),
            Codec.STRING.optionalFieldOf("design", "").forGetter(MaskData::design),
            Codec.INT.optionalFieldOf("index", 0).forGetter(MaskData::index),
            Codec.INT.optionalFieldOf("artwork_mm", 0).forGetter(MaskData::artworkMm),
            Codec.INT.optionalFieldOf("reduction", 1).forGetter(MaskData::reduction)
    ).apply(i, MaskData::new));
}
