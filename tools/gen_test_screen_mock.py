"""テストの画面の見本（S-031 ユーザー「テストがマジでやりにくい」）→ docs/gui_preview/test_screen_mock.png。決まるまで作らない
キー1つ（クリエイティブ・コマンド有効の時だけ）で開く。左＝確かめる物の一覧（初めは「今日変わった物」、タブで全部＝HUMAN_CHECK_V1）、
右＝選んだ項目の「見ること」（アイコンつき1〜2行）と、ボタン：「ここに置く」（機械・材料・試験用の電源をその場に）・「速く ×10」・「OK」「おかしい」＋メモ。
結果は run/semilab_test_results.json に保存（F2 の写真の名前も）
"""
import pathlib
from PIL import Image, ImageDraw, ImageFont

ROOT = pathlib.Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/semiconductor-lab/textures"
W, H = 380, 236
f = ImageFont.truetype("C:/Windows/Fonts/YuGothM.ttc", 10)
fs = ImageFont.truetype("C:/Windows/Fonts/YuGothM.ttc", 8)
fb = ImageFont.truetype("C:/Windows/Fonts/YuGothB.ttc", 11)
img = Image.new("RGB", (W, H), (30, 32, 40))
d = ImageDraw.Draw(img)
INK, PAPER, OK, NG, ACC = (230, 232, 240), (44, 48, 60), (70, 170, 90), (210, 80, 70), (90, 140, 230)


def icon(name, x, y, size=16):
    for sub in ("item", "block"):
        for n in (name, name + "_front"):
            p = TEX / sub / f"{n}.png"
            if p.exists():
                im = Image.open(p).convert("RGBA").resize((size, size), Image.NEAREST)
                img.paste(im, (x, y), im)
                return


def button(x, y, w, h, text, col):
    d.rounded_rectangle([x, y, x + w, y + h], 3, fill=col)
    d.text((x + (w - d.textlength(text, font=f)) / 2, y + 3), text, font=f, fill=(255, 255, 255))


d.text((10, 6), "テスト（クリエイティブだけ・キー T）", font=fb, fill=INK)
for i, (t, on) in enumerate([("今日変わった物 6", True), ("全部 98", False), ("結果", False)]):
    x = 10 + i * 92
    d.rounded_rectangle([x, 22, x + 86, 38], 3, fill=ACC if on else PAPER)
    d.text((x + 8, 25), t, font=f, fill=INK)
d.rounded_rectangle([8, 44, 168, H - 8], 4, fill=PAPER)
rows = [("robot_arm", "ロボットの腕", "✓"), ("centrifugal_governor", "遠心調速機", "✓"), ("assembly_bench", "組立台の自動セット", ""),
        ("euv_scanner", "EUV のうすい形", "✗"), ("teach_pendant", "ペンダントで教える", ""), ("blast_furnace_hearth", "高炉の見取り図", "")]
for i, (ic, t, st) in enumerate(rows):
    y = 50 + i * 26
    if i == 2:
        d.rounded_rectangle([10, y - 3, 166, y + 21], 3, fill=(60, 70, 100))
    icon(ic, 14, y)
    d.text((36, y + 3), t, font=f, fill=INK)
    if st:
        d.text((150, y + 2), st, font=fb, fill=OK if st == "✓" else NG)
d.rounded_rectangle([176, 44, W - 8, H - 8], 4, fill=PAPER)
d.text((184, 50), "組立台の自動セット", font=fb, fill=INK)
d.text((184, 68), "見ること（1分）：", font=fs, fill=(170, 175, 190))
icon("transistor", 184, 82); d.text((204, 85), "→", font=f, fill=INK); icon("assembly_bench", 216, 82)
d.text((238, 85), "◀▶ で選ぶと部品が並ぶ", font=f, fill=INK)
d.text((184, 104), "足りない物は赤い枠。Shift で入るだけ", font=f, fill=INK)
button(184, 124, 88, 18, "ここに置く", ACC)
button(278, 124, 88, 18, "速く ×10", (120, 100, 170))
d.text((184, 148), "機械＋材料を持ち物に＋試験用の電源をつなぐ", font=fs, fill=(170, 175, 190))
button(184, 166, 60, 20, "OK", OK)
button(250, 166, 70, 20, "おかしい", NG)
d.rounded_rectangle([184, 192, W - 16, 210], 2, fill=(30, 32, 40))
d.text((190, 196), "メモ：Shift でも1回分だけ…", font=f, fill=(150, 155, 170))
d.text((184, 214), "保存：run/semilab_test_results.json（F2 の写真の名前も）", font=fs, fill=(150, 155, 170))
out = ROOT / "docs/gui_preview/test_screen_mock.png"
img.resize((W * 2, H * 2), Image.NEAREST).save(out)
print(out.relative_to(ROOT))
