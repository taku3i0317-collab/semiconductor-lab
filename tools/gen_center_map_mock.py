"""S-016 中心図の見本（絵の案）：TECH_TREE.md の前提から、作りたいモノを真ん中に、内側の輪＝すぐ要る物、外へ昔の前提。✅＝太い枠、それ以外＝細い枠"""
import re, math, sys, pathlib
from PIL import Image, ImageDraw, ImageFont

CENTER = sys.argv[1] if len(sys.argv) > 1 else "10-13"
OUT = sys.argv[2] if len(sys.argv) > 2 else "docs/gui_preview/center_map_mock.png"
txt = pathlib.Path("docs/TECH_TREE.md").read_text(encoding="utf-8")
nodes = {}
for line in txt.split("\n"):
    m = re.match(r"### \[([0-9B]+-[0-9a-z]+)\] (.+?)(✅|❓|🔧|⛔)", line)
    if not m: continue
    nid, title, st = m.group(1), m.group(2).strip(), m.group(3)
    pre = re.search(r"前提: ([^／]*)", line)
    req = re.findall(r"\[([0-9B]+-[0-9a-z]+)\]", pre.group(1)) if pre else []
    year = re.search(r"年: (-?\d+)", line)
    nodes[nid] = (title.replace("🌿", "").strip(), st, req, int(year.group(1)) if year else None)

rings, seen = [[CENTER]], {CENTER}
for _ in range(3):
    nxt = []
    for n in rings[-1]:
        for r in nodes.get(n, ("", "", [], None))[2]:
            if r in nodes and r not in seen: seen.add(r); nxt.append(r)
    if not nxt: break
    rings.append(nxt[:[0, 6, 8, 10][len(rings)]])

W = H = 680
img = Image.new("RGB", (W, H), (198, 198, 198))
d = ImageDraw.Draw(img)
font = ImageFont.truetype("C:/Windows/Fonts/YuGothM.ttc", 11)
fontb = ImageFont.truetype("C:/Windows/Fonts/YuGothB.ttc", 13)
d.rectangle([0, 0, W - 1, H - 1], outline=(55, 55, 55), width=3)
d.text((12, 8), f"中心図：{nodes[CENTER][0]}（[{CENTER}]）", font=fontb, fill=(64, 64, 64))
d.text((12, H - 22), "太い枠＝もう作れる　細い枠＝まだ　押すとその物が真ん中に　外の輪ほど昔の前提", font=font, fill=(80, 80, 80))
cx, cy = W // 2, H // 2 + 4
pos = {}
for k, ring in enumerate(rings):
    rad = [0, 105, 195, 285][k]
    for i, n in enumerate(ring):
        a = -math.pi / 2 + 2 * math.pi * (i + 0.5 * (k % 2)) / max(1, len(ring))
        pos[n] = (cx + rad * math.cos(a), cy + rad * math.sin(a))
for k, ring in enumerate(rings[1:], 1):
    r = [0, 105, 195, 285][k]; d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(175, 175, 175))
for n, (x, y) in pos.items():   # 前提の線
    for r in nodes[n][2]:
        if r in pos: d.line([pos[r], (x, y)], fill=(120, 120, 140), width=1)
for n, (x, y) in pos.items():
    title, st, _, year = nodes[n]
    title = title.split("（")[0]
    label = title[:7] + ("…" if len(title) > 7 else "")
    w = 92 if n != CENTER else 130
    box = [x - w / 2, y - 16, x + w / 2, y + 16]
    d.rectangle(box, fill=(139, 139, 139) if st != "✅" else (110, 110, 110), outline=(255, 255, 160) if n == CENTER else (30, 30, 30), width=3 if st == "✅" else 1)
    d.text((box[0] + 4, box[1] + 2), label, font=fontb if n == CENTER else font, fill=(255, 255, 255))
    d.text((box[0] + 4, box[1] + 17), (str(year) if year is not None else "") + (" ✓" if st == "✅" else ""), font=font, fill=(230, 230, 160))
img.save(OUT)
print(OUT, [len(r) for r in rings])
