#!/usr/bin/env python3
"""ネットワーク図の本番のアイコン（S-008、案 E「色付き」）を作る。

決まり（docs/DESIGN_SYSTEM.md「アイコン」）：
- 16x16 のドット絵。色は素材を表す：11 素材 × 濃い・薄い ＝ 22 色（PALETTE）。1つのアイコンは2素材まで
- 画面の意味（作れる／まだ・モノ・技術）はアイコンの色ではなく丸の枠で見せる（network.html が描く）
- ゲームのテクスチャがあるノードは、そのテクスチャをこのパレットに置きかえて作る（見た目を大きく変えない）
- テクスチャが無いノードは docs/icon_glyphs.json の形（16 行 × 16 文字。'.' 空き・'#' 形・'+' 中の線）と素材2つから作る

出力：docs/icons_e.json（ノード番号 → PNG の data URI）と、確認用の一覧 build/icons_e_sheet.png
使い方:  python tools/gen_icons.py   （そのあと python tools/gen_tree_views.py）
"""
import base64
import io
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / "docs" / "network_meta.json"
GLYPHS = ROOT / "docs" / "icon_glyphs.json"
OUT = ROOT / "docs" / "icons_e.json"
TEX = ROOT / "src" / "main" / "resources" / "assets" / "semiconductor-lab" / "textures"

# 素材 → (濃い, 薄い)。docs/design_options/palette.png と同じ
PALETTE = {
    "石": ((88, 86, 82), (160, 156, 148)), "鉄": ((58, 64, 76), (132, 140, 152)), "銅": ((138, 68, 38), (212, 132, 82)),
    "金": ((150, 110, 20), (232, 192, 72)), "ガラス": ((56, 118, 138), (172, 216, 226)), "木": ((98, 62, 34), (172, 122, 72)),
    "土": ((148, 78, 48), (208, 142, 96)), "布": ((68, 78, 138), (152, 162, 212)), "樹脂": ((38, 38, 42), (112, 112, 120)),
    "シリコン": ((58, 58, 108), (122, 122, 182)), "電気": ((196, 150, 0), (255, 226, 84)),
}
COLORS = [(m, t, c) for m, pair in PALETTE.items() for t, c in enumerate(pair)]   # (素材, 0=濃い/1=薄い, 色)


def nearest(rgb, allowed=None):
    best = None
    for m, t, c in COLORS:
        if allowed and m not in allowed:
            continue
        d = sum((a - b) ** 2 for a, b in zip(rgb, c))
        if best is None or d < best[0]:
            best = (d, m, t, c)
    return best[1], best[2], best[3]


def from_texture(path):
    """ゲームのテクスチャ → パレットの2素材に置きかえ、ふちを濃い色にする"""
    img = Image.open(path).convert("RGBA")
    if img.size != (16, 16):
        img = img.resize((16, 16), Image.NEAREST)
    px = img.load()
    opaque = [(x, y) for y in range(16) for x in range(16) if px[x, y][3] >= 128]
    count = {}
    for x, y in opaque:
        m, _, _ = nearest(px[x, y][:3])
        count[m] = count.get(m, 0) + 1
    keep = sorted(count, key=lambda k: -count[k])[:2]
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    po = out.load()
    on = lambda x, y: 0 <= x < 16 and 0 <= y < 16 and px[x, y][3] >= 128
    for x, y in opaque:
        m, t, c = nearest(px[x, y][:3], keep)
        edge = not all(on(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        po[x, y] = PALETTE[m][0] + (255,) if edge else c + (255,)
    return out


def from_glyph(rows, mat):
    """形（'#' と '+'）と素材2つ → アイコン。'#' は薄い色でふちは濃い色、'+' は2つ目の素材の濃い色（電気は光る黄、まわりが空きなら濃い黄）"""
    a, b = mat
    m = [(r + "." * 16)[:16] for r in (rows + ["." * 16] * 16)[:16]]
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    po = out.load()
    on = lambda x, y: 0 <= x < 16 and 0 <= y < 16 and m[y][x] in "#+"
    plus = lambda x, y: 0 <= x < 16 and 0 <= y < 16 and m[y][x] == "+"
    for y in range(16):
        for x in range(16):
            c = m[y][x]
            if c == ".":
                continue
            edge = not all(on(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if c == "+":
                if b == "電気":   # 光る線：まわりも光る線なら薄い黄、細い所は濃い黄（白い地でも見える）
                    inner = sum(plus(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))) >= 3
                    po[x, y] = PALETTE[b][1 if inner else 0] + (255,)
                else:
                    po[x, y] = PALETTE[b][0] + (255,)
            else:
                po[x, y] = PALETTE[a][0 if edge else 1] + (255,)
    return out


def data_uri(img):
    b = io.BytesIO()
    img.save(b, format="PNG", optimize=True)
    return "data:image/png;base64," + base64.b64encode(b.getvalue()).decode()


def main():
    meta = json.loads(META.read_text(encoding="utf-8"))
    icons = {k: v for k, v in meta["icons"].items() if not k.startswith("_")}
    glyphs = {k: v for k, v in json.loads(GLYPHS.read_text(encoding="utf-8")).items() if not k.startswith("_")} if GLYPHS.exists() else {}
    out, imgs, missing = {}, {}, []
    for nid, rel in icons.items():
        p = TEX / (rel + ".png")
        if p.exists():
            imgs[nid] = from_texture(p)
        else:
            missing.append(rel)
    for nid, g in glyphs.items():
        if nid not in imgs:   # テクスチャがある物はテクスチャを優先（ゲームと見た目をそろえる）
            imgs[nid] = from_glyph(g["glyph"], g["mat"])
    for nid, im in imgs.items():
        out[nid] = data_uri(im)
    OUT.write_text(json.dumps(dict(sorted(out.items())), ensure_ascii=False, indent=0) + "\n", encoding="utf-8")
    # 確認用の一覧（4倍）
    ids = sorted(imgs)
    cols = 25
    sheet = Image.new("RGBA", (cols * 68, (len(ids) + cols - 1) // cols * 68), (250, 247, 240, 255))
    for i, nid in enumerate(ids):
        big = imgs[nid].resize((64, 64), Image.NEAREST)
        sheet.paste(big, (i % cols * 68 + 2, i // cols * 68 + 2), big)
    (ROOT / "build").mkdir(exist_ok=True)
    sheet.save(ROOT / "build" / "icons_e_sheet.png")
    print(f"icons: {len(out)}（テクスチャ {len([i for i in icons if i in imgs])}・形 {len(out) - len([i for i in icons if i in imgs])}）, missing textures: {missing}")


if __name__ == "__main__":
    main()
