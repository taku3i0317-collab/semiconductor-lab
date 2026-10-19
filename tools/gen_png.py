#!/usr/bin/env python3
"""仮テクスチャ（16x16）を作る。本番の絵は仕上げフェーズで差し替える。

使い方:  python tools/gen_png.py <種類> <色 #rrggbb> <出力パス> [第2色]
種類: brick（れんが）/ ingot（インゴット）/ powder（粉）/ lump（塊）/ wire（巻いた線）/ bar（棒）
      cylinder（ボンベ。第2色＝肩の色）/ machine_side / machine_front / machine_top（機械の面。第2色＝窓・アクセント）
"""
import random
import sys
from PIL import Image


def hexc(s):
    s = s.lstrip("#")
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c) + (255,)


def main(kind, color, out, color2=None):
    c = hexc(color)
    c2 = hexc(color2) if color2 else c
    rnd = random.Random(hash((kind, color)) & 0xFFFF)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    def put(x, y, col):
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = col

    if kind == "brick":                       # れんがを積んだ1個（アイテム）
        for y in range(4, 12):
            for x in range(2, 14):
                f = 0.9 + rnd.random() * 0.2
                if y in (4, 11) or x in (2, 13):
                    f *= 0.7
                put(x, y, shade(c, f))
        for x in range(3, 13):
            put(x, 5, shade(c, 1.2))
    elif kind == "ingot":
        for y in range(5, 12):
            inset = max(0, 7 - y) if y < 7 else 0
            for x in range(2 + inset, 14 - inset):
                f = 1.15 if y == 5 or y == 6 else (0.75 if y == 11 else 1.0)
                put(x, y, shade(c, f * (0.95 + rnd.random() * 0.1)))
    elif kind == "powder":                    # 小さな山
        for y in range(7, 14):
            half = (y - 6) + 1
            for x in range(8 - half, 8 + half):
                if rnd.random() < 0.9:
                    put(x, y, shade(c, 0.75 + rnd.random() * 0.5))
    elif kind == "lump":
        for y in range(3, 13):
            for x in range(3, 13):
                if (x - 7.5) ** 2 + (y - 8) ** 2 < 24 + rnd.random() * 6:
                    put(x, y, shade(c, 0.7 + rnd.random() * 0.5))
    elif kind == "wire":                      # 巻いた細線（輪を3つ）
        for r, f in ((6, 0.8), (4, 1.0), (2, 1.2)):
            for a in range(0, 360, 6):
                import math
                put(int(8 + r * math.cos(math.radians(a))), int(8 + r * math.sin(math.radians(a))), shade(c, f))
    elif kind == "bar":                       # 斜めの棒
        for i in range(2, 14):
            for w in range(-1, 2):
                put(i, 15 - i + w, shade(c, 1.0 + 0.15 * w))
    elif kind == "cylinder":                  # 立てたボンベ（肩とバルブは第2色）
        for y in range(4, 15):
            for x in range(5, 11):
                f = 1.2 if x == 6 else (0.7 if x == 10 else 1.0)
                put(x, y, shade(c, f))
        for x in range(6, 10):
            put(x, 3, shade(c2, 1.0))
            put(x, 4, shade(c2, 0.9))
        put(7, 2, shade((90, 90, 90), 1.0)); put(8, 2, shade((90, 90, 90), 1.0))
        put(7, 1, shade((60, 60, 60), 1.0)); put(8, 1, shade((60, 60, 60), 1.0))
    elif kind.startswith("machine"):
        for y in range(16):
            for x in range(16):
                f = 0.9 + rnd.random() * 0.15
                if x in (0, 15) or y in (0, 15):
                    f = 0.6
                put(x, y, shade(c, f))
        if kind == "machine_front":           # 窓（第2色）
            for y in range(5, 11):
                for x in range(4, 12):
                    put(x, y, shade(c2, 0.9 + rnd.random() * 0.2))
        elif kind == "machine_top":           # 丸い口
            for y in range(16):
                for x in range(16):
                    if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 12:
                        put(x, y, shade(c2, 0.8))
        else:                                 # 横：ボルト
            for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
                put(x, y, shade(c, 1.3))
    else:
        sys.exit(f"unknown kind {kind}")
    img.save(out)
    print("wrote", out)


if __name__ == "__main__":
    main(*sys.argv[1:])
