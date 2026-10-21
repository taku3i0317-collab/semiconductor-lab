"""docs/PITCH.md（未踏などの応募で使う要点、S-028）を作る。数は全部データから数える（手で書かない）。
使い方：python tools/make_pitch.py（build/provenance.json・build/benchmark・build/wikidata があれば、その数も入れる）
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def count_tests():
    n = 0
    for p in (ROOT / "src" / "gametest").rglob("*.java"):
        n += len(re.findall(r"@GameTest\b", p.read_text(encoding="utf-8", errors="ignore")))
    return n


def main():
    d = json.loads((ROOT / "docs" / "tech_network.json").read_text(encoding="utf-8"))
    nodes, edges = d["nodes"], d["edges"]
    kinds = {}
    for n in nodes:
        kinds[n["kind"]] = kinds.get(n["kind"], 0) + 1
    done = sum(1 for n in nodes if n.get("status") == "✅")
    det = (ROOT / "docs" / "tech_network_detail.js").read_text(encoding="utf-8")
    D = json.loads(det[det.index("{"):det.rindex("}") + 1])["nodes"]
    urls = set()
    for v in D.values():
        urls.update(v.get("sources", []))
        urls.update(re.findall(r"https?://[^\s、）)」]+", (v.get("real_full") or "") + (v.get("hee1") or "")))
    people = len({p["name"] for v in D.values() for p in v.get("people", [])})
    prov = json.loads((ROOT / "build" / "provenance.json").read_text(encoding="utf-8")) if (ROOT / "build" / "provenance.json").exists() else {}
    audited = sum(1 for v in prov.values() if (v.get("verification") or {}).get("audit_by_other_model"))
    with_src = sum(1 for v in prov.values() if (v.get("verification") or {}).get("sources"))
    bf = ROOT / "build" / "benchmark" / "benchmark.jsonl"
    bench = sum(1 for x in bf.read_text(encoding="utf-8").splitlines() if x.strip()) if bf.exists() else 0
    qf = ROOT / "build" / "wikidata" / "qids.json"
    q = json.loads(qf.read_text(encoding="utf-8")) if qf.exists() else {}
    qn = sum(1 for v in q.get("nodes", {}).values() if v.get("qid"))
    tests = count_tests()
    gl = json.loads((ROOT / "docs" / "glossary.json").read_text(encoding="utf-8"))
    text = f"""# PITCH — 技術ネットワークと Semiconductor Lab（応募で使う要点）

自動生成：tools/make_pitch.py（数はすべてデータから数えた。手で直さない）。誇張しない。本当のことだけ。

## 1 子どもの教材：遊んで学ぶ
- Minecraft の MOD「Semiconductor Lab」：石器から半導体・AI まで、本物の作り方の順に技術を作って遊ぶ。化学的に正しいことを最優先し、省いた所は「仮」としてゲームの中にも書く
- 技術の地図：**{len(nodes)} のノード**（技術 {kinds.get('tech', 0)}・モノ {kinds.get('product', 0)}・発見と考え方 {kinds.get('idea', 0)}）、**{len(edges)} のつながり**。うちゲームで作れる（✅）のは {done}
- 子ども向けの工夫：ふりがな、へえ①〜④（子ども・学生・大人・深掘り）、用語集 {len([k for k in gl if not k.startswith('_')])} 語、ドット絵のアイコン、「〇〇を作るには？」のミニムービー、スマホで見られる中心図、ホーム画面に置けるアプリ（電波が無くても見られる）
- ゲームの動きの自動テスト（GameTest）：{tests} 個

## 2 AI のための「確かめられた技術の地図」
- 前提のネットワーク：「何が無いと何が作れないか」（requires）と、「なぜ動くか」（understanding）・「考え方から生まれた」（enables）・部品（part）などを分けて持つ
- 出典と確かさ：出典の URL {len(urls)} 件。出典のあるノード {with_src}。確かさ ◎（信頼できる出典2つ以上）・○（1つ）・△（未確認、⚠️）。Wikipedia だけの物は ○ まで
- 出どころ（provenance）：ノードごとに、だれが（どの AI のモデル・どの担当）・いつ・どのタスクで書いたかを git の履歴から付けた。別の AI のモデルの監査を受けたノード {audited}（同意・修正・不明を1件ずつ）。**人の専門家の確認はまだ**（正直にそう書いている）
- Wikidata の番号：名前と年が両方合った物だけ {qn} ノード（合わない物は空）
- ベンチマークの見本：グラフから自動で作った問題 {bench} 問（出典があり確かでない印の無いノードだけから）
- 全部 CC0（パブリックドメイン）。公開：https://github.com/taku3i0317-collab/tech-network 、図：https://taku3i0317-collab.github.io/tech-network/

## 3 1人の人間と複数の AI で作る開発の方法
- 役割を分けた AI のセッション（設計の相談役・司令塔・実装担当・ネットワーク担当）が、受け取り箱（INBOX）と記録（LOG）で仕事を受け渡す。人はユーザーの決定（デザインの選択など）だけをする
- 品質の守り方：化学のレビュー、自動テスト、別のモデルによる監査、出典の確認、画面のはみ出しの自動検査。方法は docs/AI_WORKFLOW.md にまとめた

## 4 数（このファイルを作った時点）
| | 数 |
|---|---|
| ノード | {len(nodes)} |
| つながり | {len(edges)} |
| ゲームで作れるノード（✅） | {done} |
| 出典の URL | {len(urls)} |
| 人物（故人のみ、自作のドット絵の肖像） | {people} |
| 用語集 | {len([k for k in gl if not k.startswith('_')])} |
| GameTest | {tests} |
| 別のモデルの監査を受けたノード | {audited} |
| ベンチマーク | {bench} 問 |
| Wikidata の番号 | {qn} |

## 正直に書くこと（限界）
- 文の大部分は AI が書き、出典と別の AI のモデルで確かめた。専門家の確認はまだ
- 範囲はゲームに要る物（半導体・電気・化学）が中心で、ほかの分野はうすい
- 昔の技術の年はおおよそ。諸説ある物は ⚠️
"""
    (ROOT / "docs" / "PITCH.md").write_text(text, encoding="utf-8")
    print(f"PITCH.md: nodes {len(nodes)}, edges {len(edges)}, done {done}, urls {len(urls)}, tests {tests}, audited {audited}, bench {bench}, qids {qn}")


if __name__ == "__main__":
    main()
