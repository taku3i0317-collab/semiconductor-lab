#!/usr/bin/env python3
"""機械の GUI 背景（256x256 の左上 176x166）を作る。電解槽の背景と同じ色・書式。

使い方:  python tools/gen_gui.py <出力パス> x,y [x,y ...]
x,y は Menu の addSlot に渡す「アイテムの左上」の座標（枠はその1px外側に描く）。
プレイヤーのインベントリ（8,84 から 3x9 ＋ ホットバー 8,142）は自動で描く。
"""
import sys
from PIL import Image

BG, LIGHT, DARK, EDGE, SLOT = (198, 198, 198, 255), (255, 255, 255, 255), (85, 85, 85, 255), (55, 55, 55, 255), (139, 139, 139, 255)


def slot(px, x, y):
    for i in range(18):
        for j in range(18):
            c = SLOT
            if i == 0 or j == 0:
                c = EDGE
            elif i == 17 or j == 17:
                c = LIGHT
            px[x - 1 + i, y - 1 + j] = c


def main(out, *coords):
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    px = img.load()
    for x in range(176):
        for y in range(166):
            c = BG
            if x == 0 or y == 0:
                c = LIGHT
            elif x == 175 or y == 165:
                c = DARK
            px[x, y] = c
    for c in coords:
        x, y = map(int, c.split(","))
        slot(px, x, y)
    for r in range(3):
        for k in range(9):
            slot(px, 8 + k * 18, 84 + r * 18)
    for k in range(9):
        slot(px, 8 + k * 18, 142)
    img.save(out)
    print("wrote", out)


if __name__ == "__main__":
    main(*sys.argv[1:])
