#!/usr/bin/env python3
"""人物カードのドット絵の肖像（16x16）を作り、docs/network_meta.json の people に portrait（data URI）として入れる。

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
    W = 16
    img = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    px = img.load()

    def put(x, y, c):
        if 0 <= x < W and 0 <= y < W:
            px[x, y] = tuple(c) + (255,)

    skin, hair, cloth = SKIN[t["skin"]], HAIR[t["hair"]], CLOTH[t["cloth"]]
    # 肩と服（12〜15段目）
    for y in range(12, 16):
        for x in range(2 + max(0, 13 - y), 14 - max(0, 13 - y)):
            put(x, y, cloth if x not in (2, 13) else shade(cloth, 0.75))
    put(7, 12, skin); put(8, 12, skin)   # 首
    if t.get("tie"):
        tc = CLOTH.get(t["tie"], (230, 230, 225))
        if t["tie"] == "white":
            for x in (6, 7, 8, 9): put(x, 12, tc)
            put(7, 13, tc); put(8, 13, tc)
        else:
            put(6, 12, (230, 230, 225)); put(9, 12, (230, 230, 225))
            for y in (13, 14, 15): put(7, y, tc); put(8, y, tc)
    # 顔（4〜11段目、幅 8）
    for y in range(4, 12):
        x0, x1 = (5, 10) if y in (4, 11) else (4, 11)
        for x in range(x0, x1 + 1):
            put(x, y, skin)
    put(3, 7, shade(skin, 0.9)); put(12, 7, shade(skin, 0.9))   # 耳
    # 目・鼻・口
    eye = (30, 30, 35)
    put(6, 7, eye); put(9, 7, eye)
    put(7, 9, shade(skin, 0.82)); put(8, 9, shade(skin, 0.82))
    if t["beard"] not in ("full", "moustache"):
        put(7, 10, (170, 90, 80)); put(8, 10, (170, 90, 80))
    if t.get("glasses"):
        g = (60, 60, 70)
        for x in (5, 7, 8, 10): put(x, 7, g)
        put(6, 6, g); put(9, 6, g)
    # 髪
    st = t["style"]
    if st == "bald":
        for x in (4, 11): put(x, 5, hair); put(x, 6, hair)
    else:
        for x in range(5, 11): put(x, 3, hair)
        for x in range(4, 12): put(x, 4, hair)
        if st != "side":
            for x in (4, 5, 10, 11): put(x, 5, hair)
        else:
            for x in (4, 11): put(x, 5, hair); put(x, 6, hair)
            for x in range(6, 10): put(x, 4, skin)   # 後退した額
    if st in ("long", "wig", "curly"):
        for y in range(5, 12 if st != "curly" else 9):
            put(3, y, hair); put(12, y, hair)
            if st == "wig": put(2, y, shade(hair, 0.9)); put(13, y, shade(hair, 0.9))
        if st == "curly":
            for x in (4, 6, 9, 11): put(x, 2, hair)
    if st == "tied":
        put(7, 2, hair); put(8, 2, hair); put(7, 1, hair); put(8, 1, hair)
    # ひげ
    b = t["beard"]
    if b in ("full", "moustache"):
        for x in range(6, 10): put(x, 10, hair)
    if b == "full":
        for x in range(5, 11): put(x, 11, hair)
        for y in (8, 9, 10): put(4, y, hair); put(11, y, hair)
        for x in range(6, 10): put(x, 12, hair)
    if b == "goatee":
        put(7, 11, hair); put(8, 11, hair); put(7, 12, hair); put(8, 12, hair)
        put(6, 10, hair); put(9, 10, hair)
    if b == "sideburns":
        for y in (6, 7, 8, 9): put(4, y, hair); put(11, y, hair)
    # 帽子
    h = t["hat"]
    if h == "official":   # 漢の官帽（黒い冠）
        for x in range(5, 11): put(x, 2, (25, 25, 30)); put(x, 3, (25, 25, 30))
        for x in range(6, 10): put(x, 1, (25, 25, 30))
        put(7, 0, (25, 25, 30)); put(8, 0, (25, 25, 30))
    if h == "futou":      # 宋の幞頭（横に長い羽）
        for x in range(4, 12): put(x, 2, (25, 25, 30)); put(x, 3, (25, 25, 30))
        for x in (0, 1, 2, 3, 12, 13, 14, 15): put(x, 3, (25, 25, 30))
    if h == "beret":
        for x in range(3, 13): put(x, 2, (60, 30, 30))
        for x in range(4, 12): put(x, 1, (60, 30, 30))
        for x in range(4, 12): put(x, 3, (80, 40, 40))
    if h == "cap":
        for x in range(4, 12): put(x, 2, (120, 110, 95)); put(x, 3, (120, 110, 95))
        for x in range(3, 14): put(x, 4, (95, 85, 75))
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
    meta["people"]["_note"] = ("人物は自作のドット絵の肖像（16x16、髪・ひげ・帽子・めがね・服などの特徴だけの記号的な絵。写真や絵画の写しではない）＋名前＋ゆかりの道具。"
                               "存命の人は載せない（DESIGN「遊びの要素」）。肖像は tools/gen_portraits.py が作る。名前と年は TECH_TREE の本物欄から")
    META.write_text(json.dumps(meta, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    # 一覧の確認用シート
    names = [p["name"] for k, lst in meta["people"].items() if not k.startswith("_") for p in lst if p["name"] in TRAITS]
    sheet = Image.new("RGBA", (len(names) * 18, 18), (255, 255, 255, 255))
    for i, n in enumerate(names):
        sheet.paste(draw(TRAITS[n]), (i * 18 + 1, 1))
    sheet.resize((sheet.width * 4, sheet.height * 4), Image.NEAREST).save(ROOT / "build/portraits_sheet.png")
    print(f"portraits: {len(names)}, missing: {missing}")
    if missing:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
