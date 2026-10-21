"""「技術の手帳」の見本（絵の案、S-021 案A）：docs/gui_preview/handbook_mock.png
キーで開く画面（アイテム不要）。上にタブ3つ（ガイド・中心図・お題）、左に巻とページの一覧（まだの所は鍵で灰色）、右にページの中身。
色はバニラの画面と同じ灰色（MachineScreen の決まり）、文字は MachineLayout のデータで書く想定
"""
import pathlib
from PIL import Image, ImageDraw, ImageFont

W, H = 360, 230
GRAY, DARK, LIGHT, TEXT, LOCK = (198, 198, 198), (85, 85, 85), (255, 255, 255), (64, 64, 64), (150, 150, 150)
f = ImageFont.truetype("C:/Windows/Fonts/YuGothM.ttc", 10)
fb = ImageFont.truetype("C:/Windows/Fonts/YuGothB.ttc", 11)
img = Image.new("RGB", (W, H), (40, 40, 40))
d = ImageDraw.Draw(img)


def panel(x0, y0, x1, y1, fill=GRAY):
    d.rectangle([x0, y0, x1, y1], fill=fill)
    d.line([x0, y0, x1, y0], fill=LIGHT); d.line([x0, y0, x0, y1], fill=LIGHT)
    d.line([x0, y1, x1, y1], fill=DARK); d.line([x1, y0, x1, y1], fill=DARK)


panel(8, 26, W - 9, H - 9)
for i, (name, on) in enumerate([("ガイド", True), ("中心図", False), ("お題", False)]):   # タブ
    x = 12 + i * 62
    panel(x, 8 if on else 12, x + 58, 27, GRAY if on else (170, 170, 170))
    d.text((x + 12, 12 if on else 15), name, font=fb if on else f, fill=TEXT)
panel(14, 32, 120, H - 15, (220, 220, 220))   # 巻とページの一覧
rows = [("第1巻 石と火", True), ("第2巻 電気", True), ("第3巻 シリコン", True), ("第4巻 IC", True), ("  1. 写真の技術", True), ("  2. 窓を開ける", True),
        ("  3. 拡散と配線", True), ("  4. 検査台", False), ("第5巻 論理回路", True), ("第6巻 微細化", False), ("第7巻 極限の微細化", False)]
for i, (r, ok) in enumerate(rows):
    y = 36 + i * 15
    if i == 5: d.rectangle([16, y - 1, 118, y + 12], fill=(160, 200, 255))
    d.text((18, y), ("" if ok else "🔒") + r, font=f, fill=TEXT if ok else LOCK)
panel(126, 32, W - 15, H - 15, (240, 236, 220))   # ページの中身（本の紙の色）
page = ["2. 窓を開ける", "", "酸化膜（SiO2）にレジストを塗り、", "マスクを通して光を当てる。", "現像すると光の当たった所が溶け、",
        "フッ酸で酸化膜に窓が開く。", "", "この窓からだけ、拡散でリンや", "ホウ素がしみ込む（プレーナ法、", "1959 ホーニ）。", "", "◀ 前    第4巻 2/10    次 ▶"]
for i, line in enumerate(page):
    d.text((134, 38 + i * 14), line, font=fb if i == 0 else f, fill=TEXT)
out = pathlib.Path("docs/gui_preview/handbook_mock.png")
img.resize((W * 2, H * 2), Image.NEAREST).save(out)
print(out)
