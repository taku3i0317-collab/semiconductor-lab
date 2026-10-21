package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * マスク（データコンポーネント mask、アイテム rubylith＝原図・photomask＝フォトマスク。LITHO_DESIGN §4.3）。
 * cells＝プレイヤーが描いた所（その物がある所）。透明・不透明の向きは ChipOps.expose が層の種類で決める。
 * design・index は「どの設計図の何枚目か」（表示用）。
 */
public record MaskData(int size, LayerKind kind, BitGrid cells, String design, int index, int artworkMm, int reduction, java.util.List<Part> parts, boolean reflective) {
    /** 押したハンコの記録なし（前からの呼び方）。parts＝設計図に押したハンコ（v0.3b、チップがハンコになるとき中身をのぞける） */
    public MaskData(int size, LayerKind kind, BitGrid cells, String design, int index, int artworkMm, int reduction) { this(size, kind, cells, design, index, artworkMm, reduction, java.util.List.of()); }
    public MaskData { parts = java.util.List.copyOf(parts); }
    /** 透過型（前からの呼び方） */
    public MaskData(int size, LayerKind kind, BitGrid cells, String design, int index, int artworkMm, int reduction, java.util.List<Part> parts) { this(size, kind, cells, design, index, artworkMm, reduction, parts, false); }
    /** EUV の反射型のマスク（[9-40]、E のマスクの板：多層膜＋TaN の吸収体、E と決めた形）にしたもの */
    public MaskData reflectiveCopy() { return new MaskData(size, kind, cells, design, index, artworkMm, reduction, parts, true); }

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
            Codec.INT.optionalFieldOf("reduction", 1).forGetter(MaskData::reduction),
            Part.CODEC.listOf().optionalFieldOf("parts", java.util.List.of()).forGetter(MaskData::parts),
            Codec.BOOL.optionalFieldOf("reflective", false).forGetter(MaskData::reflective)
    ).apply(i, MaskData::new));
}
