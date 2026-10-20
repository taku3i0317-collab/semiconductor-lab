package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * マスク（データコンポーネント mask、アイテム rubylith＝原図・photomask＝フォトマスク。LITHO_DESIGN §4.3）。
 * cells＝プレイヤーが描いた所（その物がある所）。透明・不透明の向きは ChipOps.expose が層の種類で決める。
 * design・index は「どの設計図の何枚目か」（表示用）。
 */
public record MaskData(int size, LayerKind kind, BitGrid cells, String design, int index) {
    public static final Codec<MaskData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(BitGrid.MIN, BitGrid.MAX).fieldOf("size").forGetter(MaskData::size),
            LayerKind.CODEC.fieldOf("kind").forGetter(MaskData::kind),
            BitGrid.CODEC.fieldOf("cells").forGetter(MaskData::cells),
            Codec.STRING.optionalFieldOf("design", "").forGetter(MaskData::design),
            Codec.INT.optionalFieldOf("index", 0).forGetter(MaskData::index)
    ).apply(i, MaskData::new));
}
