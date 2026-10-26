"""翻訳を取りこむ（S-064）。翻訳係のファイル（<言語>.json・<言語>-2.json…、docs/i18n/README.md の形）を
docs/i18n/<言語>.json（同じ言語は合体）に、ページが読む docs/lang/<言語>.json（キー → 文だけ、source.json にあるキーだけ）を作る。
使い方：python tools/i18n_import.py <翻訳係のフォルダ>
"""
import glob
import json
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
NAMES = {"en": "English", "zh-Hans": "中文（简体）", "es": "Español", "hi": "हिन्दी", "ar": "العربية", "pt": "Português", "fr": "Français", "ru": "Русский", "id": "Bahasa Indonesia", "ko": "한국어"}


def main():
    src_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("C:/dev/notes/work/翻訳係")
    source = json.loads((ROOT / "docs" / "i18n" / "source.json").read_text(encoding="utf-8"))["strings"]
    langs = {}
    for f in sorted(glob.glob(str(src_dir / "*.json"))):
        base = os.path.basename(f)[:-5]
        lang = re.sub(r"-\d+$", "", base)
        try:
            d = json.loads(Path(f).read_text(encoding="utf-8"))
        except Exception as e:
            print("skip (not JSON):", base, e)
            continue
        L = langs.setdefault(lang, {"_meta": dict(d.get("_meta") or {}), "strings": {}})
        for k, v in (d.get("strings") or {}).items():
            if isinstance(v, str) and v.strip() and k in source:
                L["strings"][k] = v.strip()
    (ROOT / "docs" / "i18n").mkdir(exist_ok=True)
    (ROOT / "docs" / "lang").mkdir(exist_ok=True)
    # S-062・S-064：翻訳係のメモにも存命かもしれない人の名前が入ることがある（network_meta の living_names で置きかえ）
    ln = sorted(((json.loads((ROOT / "docs" / "network_meta.json").read_text(encoding="utf-8")).get("living_names") or {}).get("map") or {}).items(), key=lambda kv: -len(kv[0]))
    for L in langs.values():
        for f in ("notes", "translator"):
            if isinstance(L["_meta"].get(f), str):
                for a, b in ln:
                    L["_meta"][f] = L["_meta"][f].replace(a, b)
    index = []
    for lang, L in sorted(langs.items()):
        m = L["_meta"]
        m.setdefault("lang", lang)
        m.setdefault("name", NAMES.get(lang, lang))
        m.setdefault("dir", "rtl" if lang in ("ar", "he", "fa", "ur") else "ltr")
        m.setdefault("machine", True)
        m["keys"] = len(L["strings"])
        m["coverage"] = round(len(L["strings"]) / len(source) * 100, 1)
        (ROOT / "docs" / "i18n" / f"{lang}.json").write_text(json.dumps(L, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
        (ROOT / "docs" / "lang" / f"{lang}.json").write_text(json.dumps({"_meta": {k: m[k] for k in ("lang", "name", "dir", "machine")}, "s": L["strings"]}, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
        index.append({"lang": lang, "name": m["name"], "dir": m["dir"], "machine": m["machine"], "coverage": m["coverage"]})
        print(f"{lang}: {m['keys']} keys ({m['coverage']}%), dir={m['dir']}, machine={m['machine']}")
    (ROOT / "docs" / "lang" / "index.json").write_text(json.dumps(index, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
