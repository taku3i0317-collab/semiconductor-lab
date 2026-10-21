"""v1.0 の全体テストの確認表（O-038、v1.0 #8 の準備）→ docs/HUMAN_CHECK_V1.md（手で直さない）
石器から AI チップまでの通しの手順を、ガイドの巻とページの順（第1巻 → 第8巻 ＝ ROADMAP の版の順）に並べる。
1行＝1ページ：開く物・作り方（assets/howto_get.json）・試験場での置き場所（testworld.mcfunction の setblock、x,z は試験場の左上から）。
各巻の最後に、その巻の「人間の確認」（docs/test_queue の「- [ ] 人間：」）の数。
使い方：python tools/gen_v1_check.py（先に tools/test.ps1 → python tools/check_howto.py → python tools/gen_testworld.py）
"""
import json, pathlib, re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
NS = "semiconductor-lab"
ADV = RES / f"data/{NS}/advancement"


def load(p):
    return json.loads(p.read_text(encoding="utf-8"))


def main():
    ja = load(RES / f"assets/{NS}/lang/ja_jp.json")
    ways = load(RES / f"assets/{NS}/howto_get.json")["ways"]
    where = {}
    for line in (RES / f"data/{NS}/function/testworld.mcfunction").read_text(encoding="utf-8").splitlines():
        m = re.match(r"setblock ~(-?\d+) ~(-?\d+) ~(-?\d+) (\S+)", line)
        if not m:
            continue
        x, y, z, rest = m.groups()
        for i in re.findall(rf'{NS}:([a-z0-9_]+)', rest):
            where.setdefault(i, f"({x}, {z})" + ("" if rest.startswith(NS) else " の箱"))

    def name(i):
        return ja.get(f"item.{NS}.{i}") or ja.get(f"block.{NS}.{i}") or i

    def how(i):
        out = []
        for w in ways.get(i, []):
            out.append(name(w[8:]) if w.startswith("machine:") else ja.get(f"tooltip.{NS}.howto_get.{w}", w))
        return "・".join(out[:3]) or "—"

    vols = sorted([d for d in ADV.iterdir() if re.fullmatch(r"guide\d*", d.name)], key=lambda d: int(d.name[5:] or 1))
    vols = [ADV / "domestication", ADV / "chapter1"] + vols   # 石器・農耕のころ（家畜化）と第1章の進捗（第1巻のページは本の関数で開くので、章の進捗で通す）
    L = ["# v1.0 全体テストの確認表（自動生成：python tools/gen_v1_check.py、O-038。手で直さない）", "",
         "石器から AI チップまで、ガイドの巻とページの順に1つずつ。試験場は `/function semiconductor-lab:testworld`（docs/TESTWORLD.md）。",
         "置き場所の (x, z) は試験場の左上からのずれ。**「通しで遊ぶ」と「試験場で確かめる」の両方**：通しはサバイバルで最初から、試験場はクリエイティブで区画ごと。", "",
         "共通の確認（全部の巻で）：", "- [ ] アイテムのツールチップに「作り方：〇〇」がある", "- [ ] 機械の画面の右上の「?」で使い方とレシピ",
         "- [ ] ページが開くとトースト、ガイドの本の文と進捗の画面の文が同じ", ""]
    tq = {}
    for p in (ROOT / "docs/test_queue").glob("*.md"):
        tq[p.stem] = sum(1 for l in p.read_text(encoding="utf-8").splitlines() if l.startswith("- [ ] 人間"))
    total = 0
    for d in vols:
        root = load(d / "root.json")
        title = ja.get(root["display"]["title"]["translate"], d.name)
        L += [f"## {title}（{d.name}）", ""]
        if d.name.startswith("guide"):
            pages = sorted([p for p in d.glob("p*.json")], key=lambda p: int(re.sub(r"[^0-9]", "", p.stem) or 0))
        else:   # 進捗のつながり（親 → 子）の順
            kids = {}
            for p in d.glob("*.json"):
                par = load(p).get("parent", "")
                kids.setdefault(par.split("/")[-1] if par.startswith(f"{NS}:{d.name}/") else None, []).append(p)
            pages, todo = [], sorted(kids.get("root", []))
            while todo:
                p = todo.pop(0)
                pages.append(p)
                todo = sorted(kids.get(p.stem, [])) + todo
        for p in [d / "root.json"] + pages:
            a = load(p)
            items = sorted(set(re.findall(rf'"items":\s*"{NS}:([a-z0-9_]+)"', json.dumps(a.get("criteria", {})))))
            t = ja.get(a["display"]["title"]["translate"], p.stem) if p.stem != "root" else "（巻を開く）"
            if not items:
                L.append(f"- [ ] {t}：（入った時・本の関数で開く）")
                continue
            for i in items:
                L.append(f"- [ ] {t}：**{name(i)}**（作り方：{how(i)}／試験場 {where.get(i, '—')}）")
                total += 1
        L.append("")
    L += ["## 幹ごとの「人間の確認」（docs/test_queue、TEST_QUEUE.md）", ""]
    L += [f"- {k}：{v} 件" for k, v in sorted(tq.items()) if v]
    L += ["", "## 公開の前に（ユーザーの決定を待つ）", "", "- [ ] v1.0 公開・tag v1.0・jar はユーザーの決定のあと（O-038）"]
    (ROOT / "docs/HUMAN_CHECK_V1.md").write_text("\n".join(L) + "\n", encoding="utf-8")
    print(f"HUMAN_CHECK_V1.md: {len(vols)} volumes, {total} steps")


if __name__ == "__main__":
    main()
