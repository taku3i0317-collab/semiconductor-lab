package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 押したハンコ（v0.3b、S-005、LITHO_DESIGN §11.3）：設計図のどこ（x, y＝左上のマス）に、どのハンコ（stamp＝切り抜いた設計図。中にさらにハンコがあってよい）を押したか。
 * 判定はいつも一番下のマスクの絵（設計図の層そのもの）から行う。Part は「記号」として見せ、中身をのぞくための記録。
 */
public record Part(ChipDesign stamp, int x, int y) {
    /** ChipDesign の中で使う（ハンコの中のハンコ＝入れ子） */
    static Codec<Part> codec(Codec<ChipDesign> design) {
        return RecordCodecBuilder.create(i -> i.group(
                design.fieldOf("stamp").forGetter(Part::stamp),
                Codec.INT.fieldOf("x").forGetter(Part::x),
                Codec.INT.fieldOf("y").forGetter(Part::y)
        ).apply(i, Part::new));
    }

    /** MaskData・ChipState が使う */
    public static final Codec<Part> CODEC = Codec.lazyInitialized(() -> codec(ChipDesign.CODEC));

    /** 記号の名前（ハンコの名前＝お題の id。lang litho.semiconductor-lab.challenge.&lt;name&gt;） */
    public String name() { return stamp.name(); }
}
