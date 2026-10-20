package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 方眼1枚（size×size マス、1マス1ビット）。マス (x, y) は bits の (y*size + x) ビット目（LITHO_DESIGN §4.1）。
 * 書きかえると新しい BitGrid を返す（データコンポーネントの中身は変えてはいけないので）。
 */
public record BitGrid(int size, List<Long> bits) {
    public static final int MIN = 4, MAX = 72;

    public static final Codec<BitGrid> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(MIN, MAX).fieldOf("size").forGetter(BitGrid::size),
            Codec.LONG.listOf().fieldOf("bits").forGetter(BitGrid::bits)
    ).apply(i, BitGrid::new));

    public BitGrid {
        bits = List.copyOf(bits);
    }

    public static BitGrid empty(int size) {
        List<Long> b = new ArrayList<>();
        for (int i = 0; i < words(size); i++) b.add(0L);
        return new BitGrid(size, b);
    }

    /** 文字の絵から作る（テスト・例用）。'#' か 'X' が塗ったマス。行の数＝size */
    public static BitGrid parse(String... rows) {
        BitGrid g = empty(rows.length);
        for (int y = 0; y < rows.length; y++)
            for (int x = 0; x < rows[y].length() && x < rows.length; x++)
                if (rows[y].charAt(x) == '#' || rows[y].charAt(x) == 'X') g = g.with(x, y, true);
        return g;
    }

    private static int words(int size) { return (size * size + 63) / 64; }

    public boolean inside(int x, int y) { return x >= 0 && y >= 0 && x < size && y < size; }

    public boolean get(int x, int y) {
        if (!inside(x, y)) return false;
        int k = y * size + x;
        return k / 64 < bits.size() && (bits.get(k / 64) >>> (k % 64) & 1L) != 0;
    }

    public BitGrid with(int x, int y, boolean on) {
        if (!inside(x, y)) return this;
        int k = y * size + x;
        List<Long> b = new ArrayList<>(bits);
        while (b.size() < words(size)) b.add(0L);
        long w = b.get(k / 64);
        b.set(k / 64, on ? w | (1L << (k % 64)) : w & ~(1L << (k % 64)));
        return new BitGrid(size, b);
    }

    public int count() {
        int n = 0;
        for (long w : bits) n += Long.bitCount(w);
        return n;
    }

    public boolean isEmpty() { return count() == 0; }

    /** 塗っていないマスを塗り、塗ったマスを消す */
    public BitGrid invert() {
        BitGrid g = empty(size);
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) if (!get(x, y)) g = g.with(x, y, true);
        return g;
    }
}
