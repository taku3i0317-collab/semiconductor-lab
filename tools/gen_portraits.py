#!/usr/bin/env python3
"""人物カードのドット絵の肖像（32x32、顔が分かる大きさ。ネットワーク図の丸の中のアイコンは 16x16 のまま）を作り、docs/network_meta.json の people に portrait（data URI）として入れる。

肖像は自作の記号的なドット絵（髪・ひげ・帽子・めがね・服の色などの特徴だけ）。写真や絵の写しではない。
存命の人は載せない（DESIGN「遊びの要素」）。人物を足したら TRAITS に1行足して再実行し、そのあと gen_tree_views.py を実行する。
使い方:  python tools/gen_portraits.py
"""
import base64
import io
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / "docs/network_meta.json"

SKIN = {"a": (236, 196, 160), "b": (224, 178, 138), "c": (200, 150, 110)}
HAIR = {"black": (40, 34, 30), "brown": (110, 70, 40), "dark": (70, 50, 35), "grey": (150, 150, 150),
        "white": (225, 225, 225), "blond": (200, 160, 90), "red": (150, 70, 40)}
CLOTH = {"navy": (40, 50, 90), "black": (35, 35, 40), "brown": (100, 70, 45), "red": (140, 40, 40), "green": (50, 90, 60),
         "grey": (110, 110, 120), "blue": (60, 100, 160), "white": (220, 220, 215), "purple": (90, 60, 110)}

# 名前 → 特徴。skin / hair（色）/ style（short・bald・side・long・wig・curly・tied）/ beard（none・full・moustache・sideburns・goatee）
# hat（none・official＝中国の官帽・futou・tophat・cap・beret）/ glasses / cloth / tie（white＝白い襟、red など）
TRAITS = {
    "蔡倫": dict(skin="b", hair="black", style="tied", beard="none", hat="official", cloth="red"),
    "グーテンベルク": dict(skin="a", hair="brown", style="long", beard="full", hat="beret", cloth="brown"),
    "沈括": dict(skin="b", hair="black", style="tied", beard="goatee", hat="futou", cloth="blue"),
    "リッペルハイ": dict(skin="a", hair="brown", style="short", beard="goatee", hat="none", cloth="black", tie="white"),
    "ガリレオ": dict(skin="a", hair="red", style="side", beard="full", hat="none", cloth="black", tie="white"),
    "トリチェリ": dict(skin="a", hair="dark", style="long", beard="moustache", hat="none", cloth="black", tie="white"),
    "ゲーリケ": dict(skin="a", hair="grey", style="long", beard="moustache", hat="none", cloth="black", tie="white"),
    "ニューコメン": dict(skin="a", hair="white", style="wig", beard="none", hat="none", cloth="brown"),
    "ワット": dict(skin="a", hair="grey", style="side", beard="none", hat="none", cloth="black", tie="white"),
    "エイブラハム・ダービー1世": dict(skin="a", hair="white", style="wig", beard="none", hat="none", cloth="grey"),
    "ハンツマン": dict(skin="a", hair="white", style="wig", beard="none", hat="none", cloth="brown"),
    "モーズリー": dict(skin="a", hair="brown", style="short", beard="sideburns", hat="none", cloth="navy", tie="white"),
    "ヴォルタ": dict(skin="a", hair="grey", style="curly", beard="none", hat="none", cloth="black", tie="white"),
    "エルステッド": dict(skin="a", hair="brown", style="curly", beard="none", hat="none", cloth="navy", tie="white"),
    "ファラデー": dict(skin="a", hair="grey", style="curly", beard="sideburns", hat="none", cloth="black", tie="white"),
    "トレビシック": dict(skin="a", hair="dark", style="short", beard="sideburns", hat="none", cloth="green", tie="white"),
    "モールス": dict(skin="a", hair="white", style="side", beard="full", hat="none", cloth="black", tie="white"),
    "ダゲール": dict(skin="a", hair="dark", style="curly", beard="moustache", hat="none", cloth="black", tie="white"),
    "ベッセマー": dict(skin="a", hair="grey", style="side", beard="sideburns", hat="none", cloth="black", tie="white"),
    "プランテ": dict(skin="a", hair="dark", style="short", beard="full", hat="none", cloth="black", tie="white"),
    "シュプレンゲル": dict(skin="a", hair="dark", style="side", beard="full", hat="none", cloth="grey", tie="white"),
    "ベル": dict(skin="a", hair="white", style="short", beard="full", hat="none", cloth="black", tie="white"),
    "エジソン": dict(skin="a", hair="grey", style="short", beard="none", hat="none", cloth="black", tie="white"),
    "スワン": dict(skin="a", hair="white", style="side", beard="full", hat="none", cloth="black", tie="white"),
    "ホール": dict(skin="a", hair="dark", style="short", beard="moustache", hat="none", cloth="navy", tie="white"),
    "エルー": dict(skin="a", hair="dark", style="short", beard="full", hat="none", cloth="black", tie="white"),
    "アチソン": dict(skin="a", hair="white", style="side", beard="moustache", hat="none", cloth="grey", tie="white"),
    "リンデ": dict(skin="a", hair="white", style="side", beard="full", hat="none", cloth="black", tie="white"),
    "レントゲン": dict(skin="a", hair="dark", style="short", beard="full", hat="none", cloth="black", tie="white"),
    "マルコーニ": dict(skin="a", hair="dark", style="short", beard="none", hat="none", cloth="navy", tie="white"),
    "フレミング": dict(skin="a", hair="white", style="bald", beard="full", hat="none", cloth="black", tie="white"),
    "ド・フォレスト": dict(skin="a", hair="dark", style="short", beard="moustache", hat="none", cloth="grey", tie="red"),
    "ハーバー": dict(skin="a", hair="dark", style="bald", beard="moustache", hat="none", cloth="black", tie="white", glasses=True),
    "ボッシュ": dict(skin="a", hair="dark", style="short", beard="moustache", hat="none", cloth="grey", tie="white"),
    "ライト兄弟": dict(skin="a", hair="dark", style="bald", beard="moustache", hat="cap", cloth="brown", tie="white"),
    "チョクラルスキー": dict(skin="a", hair="dark", style="side", beard="moustache", hat="none", cloth="navy", tie="red"),
    "フレミング（アレクサンダー）": dict(skin="a", hair="grey", style="short", beard="none", hat="none", cloth="white", tie="red"),
    "バーディーン": dict(skin="a", hair="dark", style="side", beard="none", hat="none", cloth="grey", tie="blue"),
    "ブラッテン": dict(skin="a", hair="dark", style="short", beard="none", hat="none", cloth="brown", tie="red", glasses=True),
    "シャノン": dict(skin="a", hair="dark", style="short", beard="none", hat="none", cloth="grey", tie="navy"),
    "ティール": dict(skin="a", hair="brown", style="short", beard="none", hat="none", cloth="navy", tie="red", glasses=True),
    "リトル": dict(skin="a", hair="dark", style="short", beard="none", hat="none", cloth="grey", tie="green"),
    "プファン": dict(skin="a", hair="dark", style="short", beard="none", hat="none", cloth="white", tie="navy", glasses=True),
    "キルビー": dict(skin="a", hair="dark", style="side", beard="none", hat="none", cloth="navy", tie="red", glasses=True),
    "ノイス": dict(skin="a", hair="dark", style="short", beard="none", hat="none", cloth="grey", tie="navy"),
    "マカロック": dict(skin="a", hair="white", style="side", beard="full", hat="none", cloth="brown", tie="white"),
    "ピッツ": dict(skin="a", hair="dark", style="short", beard="none", hat="none", cloth="grey", tie="navy", glasses=True),
    "ローゼンブラット": dict(skin="a", hair="dark", style="curly", beard="none", hat="none", cloth="navy", tie="white"),
}


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def draw(t):
    """32x32 の肖像。顔（目・まゆ・鼻・口）が分かる大きさ。最後に外側へ1px の輪郭を付ける"""
    W = 32
    img = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    px = img.load()

    def put(x, y, c):
        if 0 <= x < W and 0 <= y < W:
            px[x, y] = tuple(c[:3]) + (255,)

    def rect(x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                put(x, y, c)

    skin, hair, cloth = SKIN[t["skin"]], HAIR[t["hair"]], CLOTH[t["cloth"]]
    dark_skin, hair_hi = shade(skin, 0.85), shade(hair, 1.25)
    cx, cy, rx, ry = 15.5, 14.5, 7.2, 9.0

    def in_head(x, y):
        return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0

    st, b, h = t["style"], t["beard"], t["hat"]
    # 長い髪・かつらは顔より後ろ（先に描く）
    if st == "long":
        rect(7, 9, 9, 27, hair); rect(22, 9, 24, 27, hair)
    if st == "wig":
        for y in range(9, 24):
            for x in (5, 6, 7, 8, 23, 24, 25, 26):
                if (x + y) % 5 != 0: put(x, y, hair if (y // 3) % 2 else shade(hair, 0.9))
    # 服と肩（24〜31段目）
    for y in range(24, 32):
        half = min(15, 7 + (y - 24) * 2)
        for x in range(16 - half, 16 + half):
            put(x, y, cloth)
    for y in range(25, 32):   # えり（左右の折り返しを少し暗く）
        put(16 - 3 - (y - 25) // 2, y, shade(cloth, 0.7)); put(15 + 3 + (y - 25) // 2, y, shade(cloth, 0.7))
    rect(14, 22, 17, 25, dark_skin)   # 首
    tie = t.get("tie")
    if tie == "white":   # 白い襟巻き（クラバット）
        rect(12, 24, 19, 26, (235, 235, 230)); rect(14, 27, 17, 28, (235, 235, 230))
    elif tie:
        for y in range(24, 32):
            w = (y - 24) // 2 + 1
            for x in range(16 - w, 16 + w): put(x, y, (235, 235, 230))
        rect(15, 25, 16, 31, CLOTH.get(tie, (140, 40, 40))); put(15, 25, shade(CLOTH.get(tie, (140, 40, 40)), 0.8))
    # 顔
    for y in range(4, 25):
        for x in range(6, 26):
            if in_head(x, y):
                edge = not (in_head(x - 1, y) and in_head(x + 1, y))
                put(x, y, dark_skin if edge or x > cx + 4.5 else skin)
    rect(7, 13, 8, 17, dark_skin); rect(23, 13, 24, 17, dark_skin)   # 耳
    put(8, 15, shade(skin, 0.7)); put(23, 15, shade(skin, 0.7))
    # まゆ・目
    rect(11, 12, 13, 12, hair if st != "bald" or b != "none" else shade(hair, 0.9))
    rect(18, 12, 20, 12, hair if st != "bald" or b != "none" else shade(hair, 0.9))
    for ex in (11, 18):
        put(ex, 14, (245, 245, 245)); put(ex + 1, 14, (35, 30, 30)); put(ex + 2, 14, (245, 245, 245))
        put(ex + 1, 15, shade(skin, 0.9))
    # 鼻・口
    put(16, 15, dark_skin); put(16, 16, dark_skin); put(16, 17, shade(skin, 0.75)); put(15, 18, shade(skin, 0.8)); put(16, 18, shade(skin, 0.8))
    rect(14, 20, 17, 20, (175, 95, 85))
    put(13, 17, shade(skin, 1.04)); put(19, 17, shade(skin, 1.04))   # ほお
    # 髪
    if st == "bald":
        rect(8, 10, 9, 15, hair); rect(22, 10, 23, 15, hair)
        put(14, 7, shade(skin, 1.08)); put(15, 7, shade(skin, 1.08))   # 頭のつや
    else:
        for y in range(3, 10):
            for x in range(7, 25):
                if ((x - cx) / 8.5) ** 2 + ((y - 9.5) / 6.5) ** 2 <= 1.0:
                    put(x, y, hair)
        if st == "side":   # 後退した額
            for y in range(4, 10):
                for x in range(11, 21):
                    if ((x - cx) / 5) ** 2 + ((y - 10) / 5) ** 2 <= 1.0: put(x, y, skin)
            rect(8, 8, 9, 14, hair); rect(22, 8, 23, 14, hair)
        else:
            rect(8, 9, 9, 12, hair); rect(22, 9, 23, 12, hair)
            for x in range(10, 22, 3): put(x, 10, hair)   # 前髪
        if st == "curly":
            for x in range(9, 23, 3): rect(x, 2, x + 1, 4, hair)   # くせ毛のでこぼこ
            rect(6, 6, 7, 13, hair); rect(24, 6, 25, 13, hair)
        if st == "tied":   # まげ
            rect(13, 0, 18, 3, hair)
        if st == "wig":
            rect(7, 3, 24, 6, hair)
        for x in range(10, 16): put(x, 5, hair_hi)   # 髪のつや
    # ひげ
    if b in ("full", "moustache", "goatee"):
        rect(13, 19, 18, 19, hair); put(12, 20, hair); put(19, 20, hair)
    if b == "full":
        for y in range(16, 27):
            for x in range(8, 24):
                if in_head(x, y) and (y >= 20 or x <= 10 or x >= 21):
                    put(x, y, hair if (x + y) % 7 else shade(hair, 0.85))
        rect(14, 20, 17, 20, (150, 80, 70))
        rect(12, 25, 19, 26, hair)
    if b == "goatee":
        rect(14, 21, 17, 25, hair)
    if b == "sideburns":
        rect(8, 11, 9, 20, hair); rect(22, 11, 23, 20, hair)
    if t.get("glasses"):
        g = (55, 55, 65)
        for ex in (10, 17):
            rect(ex, 13, ex + 4, 13, g); rect(ex, 15, ex + 4, 15, g); put(ex, 14, g); put(ex + 4, 14, g)
        put(15, 14, g); put(16, 14, g)
    # 帽子
    K = (25, 25, 30)
    if h == "official":   # 漢の冠
        rect(9, 3, 22, 7, K); rect(11, 0, 20, 2, K); rect(15, 0, 16, 0, (80, 80, 90))
    if h == "futou":      # 宋の幞頭（横に長い羽）
        rect(8, 2, 23, 7, K); rect(0, 6, 31, 7, K)
    if h == "beret":
        rect(6, 2, 24, 6, (95, 40, 40)); rect(9, 0, 22, 1, (95, 40, 40)); rect(7, 7, 23, 7, (70, 30, 30))
    if h == "cap":
        rect(8, 3, 23, 7, (120, 110, 95)); rect(5, 8, 22, 8, (90, 80, 70))
    # 輪郭：透明に接する点を暗く
    src = img.copy().load()
    for y in range(W):
        for x in range(W):
            if src[x, y][3] and any(not (0 <= x + dx < W and 0 <= y + dy < W) or src[x + dx, y + dy][3] == 0
                                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(x, y, shade(src[x, y], 0.6))
    return img


def data_uri(img):
    b = io.BytesIO()
    img.save(b, format="PNG", optimize=True)
    return "data:image/png;base64," + base64.b64encode(b.getvalue()).decode()


def main():
    meta = json.loads(META.read_text(encoding="utf-8"))
    missing = []
    for k, lst in meta["people"].items():
        if k.startswith("_"):
            continue
        for p in lst:
            t = TRAITS.get(p["name"])
            if not t:
                missing.append(p["name"]); continue
            p["portrait"] = data_uri(draw(t))
    meta["people"]["_note"] = ("人物は自作のドット絵の肖像（32x32、髪・ひげ・帽子・めがね・服などの特徴だけの記号的な絵。写真や絵画の写しではない）＋名前＋ゆかりの道具。"
                               "存命の人は載せない（DESIGN「遊びの要素」）。32x32。肖像は tools/gen_portraits.py が作る。名前と年は TECH_TREE の本物欄から")
    META.write_text(json.dumps(meta, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    # 一覧の確認用シート
    names = [p["name"] for k, lst in meta["people"].items() if not k.startswith("_") for p in lst if p["name"] in TRAITS]
    cols = 10
    sheet = Image.new("RGBA", (cols * 34, (len(names) + cols - 1) // cols * 34), (255, 255, 255, 255))
    for i, n in enumerate(names):
        sheet.paste(draw(TRAITS[n]), (i % cols * 34 + 1, i // cols * 34 + 1))
    sheet.resize((sheet.width * 3, sheet.height * 3), Image.NEAREST).save(ROOT / "build/portraits_sheet.png")
    print(f"portraits: {len(names)}, missing: {missing}")
    if missing:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
