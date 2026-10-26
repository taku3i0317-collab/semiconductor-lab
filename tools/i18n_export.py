"""翻訳の元の文を書き出す（S-063）：docs/i18n/source.json。形の説明は docs/i18n/README.md。

キー：<ノード番号>.<欄>（name・hee1〜hee4・real・play）、person.<ノード番号>.<何番目>.intro、term.<用語>.name・term.<用語>.def、ui.<名前>（画面の文）
値：{"ja": 日本語（正）, "en": 今の英語（あれば）}。日本語が無いキーは入れない。出典の書き込み（出典：URL）は外す
使い方：python tools/i18n_export.py
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "docs" / "i18n"

# 画面の文（物のページ・入口・章の一覧。tools/gen_node_pages.py・tools/gen_home.py の data-en と同じ）
UI = {
    "home": ("技術の地図", "Tech map"),
    "search_placeholder": ("さがす（物・技術・人の名前）", "Search (things, techniques, people)"),
    "film": ("▶ できるまで", "▶ How it was made"),
    "share": ("共有", "Share"),
    "how_to_get": ("どうやって手に入れるか", "How to get it"),
    "how_to_make": ("作り方", "How to make it"),
    "real_history": ("本当の歴史", "Real history"),
    "read_more": ("もっと読む（学生・大人・ガチ・人物・出典）", "Read more (students, adults, deep dive, people, sources)"),
    "connections": ("つながり", "Connections"),
    "needed": ("これを作るのに要る", "Needed to make this"),
    "made_possible": ("これで作れるようになった", "Made possible by this"),
    "ring_map": ("中心図で見る", "See the ring map"),
    "report": ("誤りを知らせる", "Report a mistake"),
    "year_map": ("年の図", "Year map"),
    "make_below": ("ゲームでは、下の「作り方」で作る", "In the game you make it as shown below."),
    "no_recipe": ("レシピは無い（拾う・掘る・ほかの方法）", "No recipe: found, mined or gathered"),
    "in_game_no_detail": ("ゲームで作れる（くわしい作り方はまだ）", "In the game (details not yet written)."),
    "not_in_game": ("ゲームにはまだ無い（本当の歴史だけ）", "Not in the game yet: real history only."),
    "view_3d": ("3D で見る", "View in 3D"),
    "machine_makes": ("この機械でできること", "What this machine makes"),
    "guides": ("道案内", "Guides"),
    "chapters": ("章から", "Chapters"),
    "more": ("ほかに", "More"),
    "see_map": ("地図で見る", "See the map"),
    "in_game": ("ゲームにある", "in the game"),
    "year_disputed": ("⚠️ 年は諸説", "⚠️ year disputed"),
    "origin": ("（始まり）", "(a starting point)"),
    "home_lead": ("石器から AI チップまで、物と、それを作るのに要る物。名前でさがすか、道案内でたどる。", "From stone tools to AI chips: things and what each one needs. Search by name, or follow a guide."),
    "guide_first_hour": ("はじめての1時間", "Your first hour"),
    "guide_ai_chip": ("AI チップまで", "All the way to an AI chip"),
    "machine_translation": ("機械訳（まちがいがあるかもしれません）", "Machine translation (may contain mistakes)"),
    "fix_translation": ("訳を直す", "Fix this translation"),
}


def js_obj(p):
    s = p.read_text(encoding="utf-8")
    return json.loads(s[s.index("{"):s.rindex("}") + 1])


def clean(t):
    t = re.sub(r"（[^（）]*出典[:：][^）]*）", "", t or "")
    return re.sub(r"\*\*|__|`", "", t).strip()


def main():
    G = js_obj(ROOT / "docs" / "tech_network_graph.js")
    D = js_obj(ROOT / "docs" / "tech_network_detail.js")["nodes"]
    gl = json.loads((ROOT / "docs" / "glossary.json").read_text(encoding="utf-8"))
    S = {}
    def put(k, ja, en=None):
        ja = clean(ja)
        if not ja:
            return
        S[k] = {"ja": ja}
        if en and clean(en):
            S[k]["en"] = clean(en)
    for n in G["nodes"]:
        i, d = n["id"], D.get(n["id"], {})
        put(f"{i}.name", n["name"], n.get("name_en"))
        put(f"{i}.hee1", d.get("hee1"), d.get("hee1_en"))
        ref = (d.get("refs") or [{}])[0] if d.get("refs") else {}
        for k in ("2", "3", "4"):
            put(f"{i}.hee{k}", (d.get("hee_game") or {}).get("note" + k) or ((ref.get("hee") or {}).get(k) if ref else "") or (d.get("hee_tree") or {}).get(k))
        put(f"{i}.real", d.get("real_full"))
        put(f"{i}.play", d.get("play"))
        for j, p in enumerate(d.get("people") or []):
            put(f"person.{i}.{j}.intro", p.get("intro"))
    for term, v in gl.items():
        if term.startswith("_"):
            continue
        put(f"term.{term}.name", term, v.get("en"))
        put(f"term.{term}.def", v.get("ja_def"), v.get("en_def"))
    for k, (ja, en) in UI.items():
        put(f"ui.{k}", ja, en)
    OUT.mkdir(parents=True, exist_ok=True)
    n_ja = len(S)
    n_en = sum(1 for v in S.values() if "en" in v)
    chars = sum(len(v["ja"]) for v in S.values())
    (OUT / "source.json").write_text(json.dumps({"_meta": {"what": "翻訳の元の文（日本語が正）。形の説明は docs/i18n/README.md", "generated_by": "tools/i18n_export.py",
                                                            "keys": n_ja, "with_english": n_en, "japanese_chars": chars}, "strings": S}, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"i18n source: {n_ja} keys ({n_en} with English), {chars} Japanese characters")


if __name__ == "__main__":
    main()
