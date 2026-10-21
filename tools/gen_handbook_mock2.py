"""「技術の手帳」の見本の作り直し（S-024：ユーザー「公式っぽいけど、ワクワクしない」）：ワクワクする方向の 3 案。決まるまで作らない（見本の絵だけ）
- 案1 handbook_mock_1.png「絵が主役＋次にやること」：ページの上半分に大きな絵（MOD のアイコン・機械の画面の絵をドット絵のまま拡大）、文は短く下に。
  右に「次にやること」カード（持ち物を見て次に作る物を1枚。要る物のアイコン、持っている物に ✓）。新しいページが開いたら手帳の角が光る
- 案2 handbook_mock_2.png「旅の地図の目次」：章を一本の道（石と火 → 電気 → シリコン → IC → 論理 → 微細化 → AI）に並べ、今いる所に自分の印、まだの章は霧（DESIGN の霧の地図とそろえる）
- 案3 handbook_mock_3.png「発見の手帳」：方眼のノート・お題のハンコの仕組みで「ここまで来た」の印・付箋・手書き風の矢印
色は B の docs/icons_e.json（ノードのアイコン）でよく使われる色から 22 色を取り出して使う（灰色のバニラの画面にしない）。キーで開く・タブ・開く決まり・中身の量は S-021 のまま
使い方：python tools/gen_handbook_mock2.py → docs/gui_preview/handbook_mock_1..3.png
"""
import base64, io, json, pathlib, random
from PIL import Image, ImageDraw, ImageFont

ROOT = pathlib.Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/semiconductor-lab/textures"
OUT = ROOT / "docs/gui_preview"
W, H = 360, 230
f = ImageFont.truetype("C:/Windows/Fonts/YuGothM.ttc", 10)
fs = ImageFont.truetype("C:/Windows/Fonts/YuGothM.ttc", 8)
fb = ImageFont.truetype("C:/Windows/Fonts/YuGothB.ttc", 12)
fbs = ImageFont.truetype("C:/Windows/Fonts/YuGothB.ttc", 10)


def palette(n=22):
    """B の icons_e のアイコンで多い色（似た色はまとめる）"""
    p = ROOT.parent / "semilab-network/docs/icons_e.json"
    counts = {}
    if p.exists():
        for v in json.loads(p.read_text(encoding="utf-8")).values():
            im = Image.open(io.BytesIO(base64.b64decode(v.split(",", 1)[1]))).convert("RGBA")
            for r, g, b, a in im.get_flattened_data():
                if a > 128:
                    k = (r // 24 * 24 + 12, g // 24 * 24 + 12, b // 24 * 24 + 12)
                    counts[k] = counts.get(k, 0) + 1
    out = []
    for c, _ in sorted(counts.items(), key=lambda kv: -kv[1]):
        if sum(abs(a - b) for a, b in zip(c, (128, 128, 128))) < 24:   # 灰色はとばす
            continue
        if all(sum(abs(a - b) for a, b in zip(c, o)) > 30 for o in out):
            out.append(c)
        if len(out) == n:
            break
    return out or [(200, 80, 60), (230, 170, 60), (90, 160, 90), (70, 120, 200), (150, 90, 170)]


PAL = palette()
VIVID = sorted(PAL, key=lambda c: -(max(c) - min(c)))[:8]   # 鮮やかな色から（暗い色ばかりだと楽しくない）


def warm(i):
    return VIVID[i % len(VIVID)]


def icon(name, size):
    for d in ("item", "block"):
        p = TEX / d / f"{name}.png"
        if p.exists():
            return Image.open(p).convert("RGBA").resize((size, size), Image.NEAREST)
    return Image.new("RGBA", (size, size), (200, 0, 200, 255))


def paste(img, name, x, y, size):
    im = icon(name, size)
    img.paste(im, (x, y), im)


def tabs(d, cur, bg, ink):
    for i, name in enumerate(["ガイド", "中心図", "お題"]):
        x = 14 + i * 64
        on = i == cur
        d.rounded_rectangle([x, 6 if on else 10, x + 58, 30], 5, fill=bg if on else tuple(max(0, c - 40) for c in bg))
        d.text((x + 13, 11 if on else 14), name, font=fbs if on else f, fill=ink)


def mock1():
    """案1：絵が主役＋次にやることカード"""
    paper, ink, edge = (250, 240, 214), (60, 44, 30), (120, 80, 50)
    img = Image.new("RGB", (W, H), (34, 30, 40))
    d = ImageDraw.Draw(img)
    tabs(d, 0, paper, ink)
    d.rounded_rectangle([8, 26, 246, H - 8], 6, fill=paper, outline=edge, width=2)
    d.polygon([(246 - 16, 26), (246, 26), (246, 42)], fill=(255, 220, 90))   # 新しいページの印（角が光る）
    d.rectangle([16, 34, 238, 132], fill=(36, 40, 60))   # 大きな絵（上半分）
    for k in range(18):   # 光の筋（マスクを通る光）
        x = 40 + k * 10
        d.line([(x, 38), (x + (k - 9) * 2, 92)], fill=(140, 120, 255), width=1)
    paste(img, "photomask", 92, 66, 64)
    paste(img, "oxidized_wafer", 160, 82, 48)
    d.text((22, 116), "光 → マスク → ウェハー", font=fbs, fill=(255, 240, 160))
    d.text((18, 138), "窓を開ける", font=fb, fill=ink)
    d.text((18, 156), "光の当たった所だけ、膜に窓が開く。", font=f, fill=ink)
    d.text((18, 170), "窓からだけリンやホウ素がしみ込む。", font=f, fill=ink)
    d.text((18, 190), "1959 ホーニ「プレーナ法」", font=fs, fill=edge)
    d.text((18, 206), "◀    第4巻  2 / 10    ▶", font=f, fill=edge)
    # 次にやることカード
    c = warm(1)
    d.rounded_rectangle([254, 26, W - 8, H - 8], 6, fill=(255, 250, 235), outline=c, width=3)
    d.rectangle([254, 26, W - 8, 46], fill=c)
    d.text((262, 29), "次にやること", font=fbs, fill=(255, 255, 255))
    paste(img, "photomask", 280, 52, 48)
    d.text((266, 104), "フォトマスク", font=fbs, fill=ink)
    need = [("quartz_blank", True), ("photo_emulsion", True), ("photo_plate", False)]
    for i, (n, ok) in enumerate(need):
        y = 124 + i * 26
        paste(img, n, 266, y, 20)
        d.text((292, y + 3), "✓ 持っている" if ok else "まだ", font=fs, fill=(40, 140, 60) if ok else (190, 60, 50))
    d.text((264, 204), "作り方 ▶ 第4巻 1", font=fs, fill=edge)
    return img


def mock2():
    """案2：旅の地図の目次（霧の地図とそろえる）"""
    img = Image.new("RGB", (W, H), (30, 46, 40))
    d = ImageDraw.Draw(img)
    tabs(d, 0, (236, 222, 190), (60, 44, 30))
    d.rounded_rectangle([8, 26, W - 8, H - 8], 6, fill=(226, 212, 176), outline=(120, 90, 60), width=2)
    rnd = random.Random(3)
    for _ in range(260):   # 紙の点
        x, y = rnd.randint(12, W - 12), rnd.randint(30, H - 12)
        d.point((x, y), fill=(210, 194, 156))
    stops = [("石と火", "quicklime", 30, 168), ("電気", "copper_cable", 78, 132), ("シリコン", "polysilicon", 130, 160), ("IC", "chip", 180, 110),
             ("論理", "transistor", 226, 150), ("微細化", "photomask", 270, 96), ("AI", "ai_chip", 320, 60)]
    here = 3
    for i in range(len(stops) - 1):   # 道（点線、まだの所はうすく）
        (_, _, x0, y0), (_, _, x1, y1) = stops[i], stops[i + 1]
        for t in range(0, 20):
            if t % 2 == 0:
                a, b = t / 20, (t + 1) / 20
                d.line([(x0 + (x1 - x0) * a + 10, y0 + (y1 - y0) * a + 10), (x0 + (x1 - x0) * b + 10, y0 + (y1 - y0) * b + 10)],
                       fill=(150, 90, 50) if i < here else (190, 176, 140), width=3)
    for i, (name, ic, x, y) in enumerate(stops):
        col = warm(i + 2)
        if i <= here:
            d.ellipse([x - 4, y - 4, x + 24, y + 24], fill=col, outline=(80, 50, 30), width=2)
            paste(img, ic, x + 2, y + 2, 16)
            d.text((x - 4, y + 27), name, font=fbs, fill=(60, 40, 25))
        else:   # 霧
            fog = Image.new("RGBA", (44, 40), (240, 240, 245, 0))
            fd = ImageDraw.Draw(fog)
            for k in range(7):
                fd.ellipse([rnd.randint(0, 20), rnd.randint(0, 18), rnd.randint(24, 44), rnd.randint(22, 40)], fill=(245, 245, 250, 150))
            img.paste(fog, (x - 10, y - 10), fog)
            d.text((x + 4, y + 2), "?", font=fb, fill=(150, 150, 160))
    hx, hy = stops[here][2], stops[here][3]   # 自分の印（旗）
    d.line([(hx + 26, hy - 20), (hx + 26, hy + 6)], fill=(60, 40, 25), width=2)
    d.polygon([(hx + 27, hy - 20), (hx + 42, hy - 15), (hx + 27, hy - 10)], fill=(220, 50, 50))
    d.text((hx + 30, hy - 34), "いまここ", font=fs, fill=(200, 40, 40))
    d.text((16, 34), "技術の旅（第4巻 IC まで来た）", font=fb, fill=(60, 40, 25))
    d.text((16, 52), "道の石をクリック → その章へ", font=fs, fill=(110, 80, 50))
    return img


def mock3():
    """案3：発見の手帳（方眼・ハンコ・付箋）"""
    paper = (252, 250, 240)
    img = Image.new("RGB", (W, H), (44, 36, 30))
    d = ImageDraw.Draw(img)
    tabs(d, 2, paper, (40, 40, 60))
    d.rectangle([8, 26, W - 8, H - 8], fill=paper)
    for x in range(8, W - 8, 8):
        d.line([(x, 26), (x, H - 8)], fill=(214, 228, 240))
    for y in range(26, H - 8, 8):
        d.line([(8, y), (W - 8, y)], fill=(214, 228, 240))
    d.line([(180, 26), (180, H - 8)], fill=(200, 160, 160), width=2)   # 見開きの真ん中
    for i in range(6):   # リングのとじ
        d.ellipse([175, 38 + i * 30, 185, 48 + i * 30], outline=(120, 120, 130), width=2)
    d.text((18, 34), "発見メモ No.12", font=fb, fill=(40, 50, 90))
    d.text((18, 54), "ゲルマニウムに針を2本 →", font=f, fill=(40, 50, 90))
    d.text((18, 68), "電気が「増えた」！", font=f, fill=(40, 50, 90))
    paste(img, "transistor", 30, 88, 48)
    d.line([(84, 110), (120, 100)], fill=(200, 60, 60), width=2)
    d.polygon([(120, 100), (112, 98), (116, 106)], fill=(200, 60, 60))
    d.text((122, 92), "これ！", font=fbs, fill=(200, 60, 60))
    d.text((18, 150), "1947 ベル研", font=fs, fill=(90, 90, 120))
    # ハンコ（お題のハンコの仕組みで「ここまで来た」）
    for i, (name, done) in enumerate([("石と火", True), ("電気", True), ("シリコン", True), ("IC", True), ("論理", False), ("微細化", False)]):
        cx, cy = 210 + (i % 3) * 46, 60 + (i // 3) * 54
        col = warm(i) if done else (205, 205, 205)
        d.ellipse([cx - 18, cy - 18, cx + 18, cy + 18], outline=col, width=3)
        ff = fbs if len(name) <= 2 else fs
        d.text((cx - len(name) * (5 if len(name) <= 2 else 4), cy - 6), name, font=ff, fill=col)
        if done:
            d.text((cx + 8, cy + 10), "✓", font=fs, fill=col)
    d.text((196, 22 + 8), "ここまで来たハンコ", font=fbs, fill=(40, 50, 90))
    # 付箋
    note = Image.new("RGBA", (84, 54), warm(5) + (235,))
    nd = ImageDraw.Draw(note)
    nd.text((6, 6), "次：検査台", font=fbs, fill=(40, 30, 20))
    nd.text((6, 24), "要る物 2/3 ✓", font=fs, fill=(40, 30, 20))
    nd.text((6, 36), "キー H で開く", font=fs, fill=(40, 30, 20))
    note = note.rotate(-6, expand=True, resample=Image.NEAREST)
    img.paste(note, (250, 156), note)
    d.polygon([(W - 24, H - 8), (W - 8, H - 8), (W - 8, H - 24)], fill=(255, 220, 90))   # 角が光る
    return img


if __name__ == "__main__":
    for i, fn in enumerate((mock1, mock2, mock3), 1):
        p = OUT / f"handbook_mock_{i}.png"
        fn().resize((W * 2, H * 2), Image.NEAREST).save(p)
        print(p.relative_to(ROOT))
    print("palette", len(PAL))
