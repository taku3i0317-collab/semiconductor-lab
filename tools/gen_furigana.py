"""ふりがなの辞書を作る（S-012 決定1）。

ページに出る日本語（tech_network_graph.js・tech_network_detail.js・glossary.js・docs/*.html）から漢字を含む言葉を集め、
pykakasi（辞書で読みを付ける道具）で読みを付けて docs/furigana_dict.js に書き出す。
読みの誤り（固有名詞・専門語・「人」など）は docs/furigana_fix.json の表で手で直す（表が優先）。

ページ側は docs/furigana.js が「ふりがな」ボタンでオン・オフし、オンのときだけ辞書を読み込んで <ruby> を付ける。

実行：python tools/gen_furigana.py   （pykakasi は pip --user で入れたので、python -I では動かない）
"""
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

import pykakasi

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
SOURCES = ["tech_network_graph.js", "tech_network_detail.js", "glossary.js"]
KANJI = re.compile(r"[一-龥々〆ヵヶ]")
RUN = re.compile(r"[一-龥々〆ヵヶぁ-んァ-ヶー]+")
KANA = re.compile(r"[ぁ-ん]")


def hira(s):
    """カタカナをひらがなに（送りがなの比べ合わせ用）"""
    return "".join(chr(ord(c) - 0x60) if "ァ" <= c <= "ヶ" else c for c in s)


def split_kana(orig, read):
    """前後の送りがな（読みと同じかな）を外す。返り値：(前のかなの数, 漢字の部分の読み, 後のかなの数)"""
    pre = 0
    while pre < len(orig) and not KANJI.match(orig[pre]) and pre < len(read) and hira(orig[pre]) == read[pre]:
        pre += 1
    suf = 0
    while suf < len(orig) - pre and not KANJI.match(orig[-1 - suf]) and suf < len(read) - pre and hira(orig[-1 - suf]) == read[-1 - suf]:
        suf += 1
    return pre, read[pre:len(read) - suf], suf


def collect_text():
    texts = []
    for name in SOURCES:
        p = DOCS / name
        if p.exists():
            texts.append(p.read_text(encoding="utf-8"))
    for p in DOCS.glob("*.html"):
        texts.append(p.read_text(encoding="utf-8"))
    return texts


def main():
    kks = pykakasi.kakasi()
    counts = defaultdict(Counter)
    seen_runs = set()
    for t in collect_text():
        for run in RUN.findall(t):
            if not KANJI.search(run) or run in seen_runs:
                continue
            seen_runs.add(run)
            for seg in kks.convert(run):
                o, r = seg["orig"], seg["hira"]
                if KANJI.search(o) and r and r != o:
                    counts[o][r] += 1
    fix = json.loads((DOCS / "furigana_fix.json").read_text(encoding="utf-8"))
    fix = {k: v for k, v in fix.items() if not k.startswith("_")}
    out = {}
    for o, c in counts.items():
        out[o] = c.most_common(1)[0][0]
    out.update(fix)
    # 値は [前のかなの数, 読み, 後のかなの数]（送りがなにはふりがなを付けない）
    table = {}
    for o, r in sorted(out.items()):
        pre, mid, suf = split_kana(o, r)
        table[o] = [pre, mid, suf] if (pre or suf) else mid
    body = json.dumps(table, ensure_ascii=False, separators=(",", ":"))
    (DOCS / "furigana_dict.js").write_text(
        "// 自動生成：tools/gen_furigana.py（pykakasi＋docs/furigana_fix.json）。手で直さない\n"
        f"window.FURI_DICT = {body};\n", encoding="utf-8")
    print(f"furigana: {len(table)} words ({len(fix)} hand-fixed), {len(body) // 1024} KB")


if __name__ == "__main__":
    main()
