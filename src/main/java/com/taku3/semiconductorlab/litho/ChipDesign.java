package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 設計図（データコンポーネント chip_design、アイテム layout_sheet）：全部の層を工程の順に（LITHO_DESIGN §4.2）。
 * 製図台で描く。ChipModel.of(design) が「全部うまくいったら」の予想を作る。
 */
public record ChipDesign(int size, String name, List<Layer> layers) {
    /** 1つの層（工程の順に並ぶ） */
    public record Layer(LayerKind kind, BitGrid cells) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(i -> i.group(
                LayerKind.CODEC.fieldOf("kind").forGetter(Layer::kind),
                BitGrid.CODEC.fieldOf("cells").forGetter(Layer::cells)
        ).apply(i, Layer::new));
    }

    public static final Codec<ChipDesign> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(BitGrid.MIN, BitGrid.MAX).fieldOf("size").forGetter(ChipDesign::size),
            Codec.STRING.optionalFieldOf("name", "").forGetter(ChipDesign::name),
            Layer.CODEC.listOf().fieldOf("layers").forGetter(ChipDesign::layers)
    ).apply(i, ChipDesign::new));

    public ChipDesign {
        layers = List.copyOf(layers);
    }

    /** 層 i を書きかえた設計図 */
    public ChipDesign withLayer(int i, BitGrid cells) {
        List<Layer> l = new ArrayList<>(layers);
        l.set(i, new Layer(l.get(i).kind(), cells));
        return new ChipDesign(size, name, l);
    }

    /** 層 i のマスク（原図・フォトマスクに入れる中身） */
    public MaskData mask(int i) { return new MaskData(size, layers.get(i).kind(), layers.get(i).cells(), name, i); }
}
