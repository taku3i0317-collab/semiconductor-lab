"""Hugging Face のデータセットを作る（S-028）。build/huggingface/ に書く（git に入れない。公開はユーザーのアカウントで）。

元：build/tech-network/data（tools/export_tech_network.py の書き出し）、build/provenance.json（tools/provenance.py）、
    build/wikidata/qids.json（tools/wikidata_match.py、無ければ空）、build/benchmark/benchmark.jsonl（tools/make_benchmark.py、無ければ出さない）
出す物（設定 configs）：
  default … data/nodes.jsonl・.parquet（本当の歴史だけ）、data/edges.*、data/glossary.*
  game    … data/nodes_game.jsonl・.parquet（MOD での作り方。歴史とは別）
  benchmark … data/benchmark.jsonl・.parquet
使い方：python tools/make_hf_dataset.py（pyarrow が要る）
"""
import json
import re
import shutil
from pathlib import Path

import pyarrow as pa
import pyarrow.parquet as pq

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "build" / "tech-network" / "data"
OUT = ROOT / "build" / "huggingface"
REPO = "https://github.com/taku3i0317-collab/tech-network"


def jl(path, rows):
    with open(path, "w", encoding="utf-8") as f:
        for r in rows:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")
    # Parquet：入れ子の物は JSON の文字列にして、型をそろえる
    flat = [{k: (json.dumps(v, ensure_ascii=False) if isinstance(v, (dict, list)) else v) for k, v in r.items()} for r in rows]
    pq.write_table(pa.Table.from_pylist(flat), path.with_suffix(".parquet"))


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    (OUT / "data").mkdir(parents=True)
    prov = json.loads((ROOT / "build" / "provenance.json").read_text(encoding="utf-8"))
    qf = ROOT / "build" / "wikidata" / "qids.json"
    qids = json.loads(qf.read_text(encoding="utf-8")).get("nodes", {}) if qf.exists() else {}
    nodes, game = [], []
    for p in sorted((SRC / "nodes").glob("*.json")):
        d = json.loads(p.read_text(encoding="utf-8"))
        h, g = d["history"], d.get("game", {})
        q = qids.get(d["id"], {})
        pv = prov.get(d["id"], {})
        nodes.append({
            "id": d["id"], "name_ja": h.get("name"), "name_en": h.get("name_en"), "kind": h.get("kind"),
            "branch": h.get("trunk_name"), "year": h.get("year"), "year_range": h.get("year_range"), "year_uncertain": bool(h.get("year_uncertain")),
            "history_ja": h.get("real"), "fun_fact_ja": h.get("hee1"), "fun_fact_en": h.get("hee1_en"),
            "sources": h.get("sources", []),
            "references": [{"title": r.get("title"), "certainty": r.get("certainty"), "basis": r.get("basis")} for r in h.get("refs", [])],
            "people": [{"name": x.get("name"), "year": x.get("year")} for x in h.get("people", [])],
            "requires": h.get("requires", []), "parts": h.get("parts", []), "materials": h.get("materials", []),
            "wikidata_qid": q.get("qid"),
            "provenance": pv.get("added"), "verification": pv.get("verification"),
        })
        game.append({"id": d["id"], **{k: g.get(k) for k in ("status", "buildable_now", "play", "missing", "machine_recipes", "game_text")}})
    edges = json.loads((SRC / "edges.json").read_text(encoding="utf-8"))
    gl = json.loads((SRC / "glossary.json").read_text(encoding="utf-8"))
    glossary = [{"term_ja": k, "term_en": v.get("en"), "definition_ja": v.get("ja_def"), "definition_en": v.get("en_def")} for k, v in gl.items() if not k.startswith("_")]
    jl(OUT / "data" / "nodes.jsonl", nodes)
    jl(OUT / "data" / "nodes_game.jsonl", game)
    jl(OUT / "data" / "edges.jsonl", edges)
    jl(OUT / "data" / "glossary.jsonl", glossary)
    bf = ROOT / "build" / "benchmark" / "benchmark.jsonl"
    nb = 0
    if bf.exists():
        rows = [json.loads(x) for x in bf.read_text(encoding="utf-8").splitlines() if x.strip()]
        jl(OUT / "data" / "benchmark.jsonl", rows); nb = len(rows)
    # 数（カードに書く。手で書かない）
    kinds = {}
    for n in nodes:
        kinds[n["kind"]] = kinds.get(n["kind"], 0) + 1
    ekinds = {}
    for e in edges:
        ekinds[e["kind"]] = ekinds.get(e["kind"], 0) + 1
    with_src = sum(1 for n in nodes if (n["verification"] or {}).get("sources"))
    audited = sum(1 for n in nodes if (n["verification"] or {}).get("audit_by_other_model"))
    uncertain = sum(1 for n in nodes if n["year_uncertain"] or (n["verification"] or {}).get("uncertain_mark"))
    wq = sum(1 for n in nodes if n["wikidata_qid"])
    models = {}
    for n in nodes:
        m = (n["provenance"] or {}).get("ai_model") or "not recorded"
        models[m] = models.get(m, 0) + 1
    card = f"""---
license: cc0-1.0
language:
- ja
- en
pretty_name: Technology Network (verified prerequisites of technologies)
size_categories:
- n<1K
task_categories:
- question-answering
- graph-ml
tags:
- history-of-technology
- knowledge-graph
- education
- provenance
configs:
- config_name: default
  data_files:
  - split: nodes
    path: data/nodes.parquet
  - split: edges
    path: data/edges.parquet
  - split: glossary
    path: data/glossary.parquet
- config_name: game
  data_files:
  - split: nodes
    path: data/nodes_game.parquet
{"- config_name: benchmark" + chr(10) + "  data_files:" + chr(10) + "  - split: test" + chr(10) + "    path: data/benchmark.parquet" if nb else ""}
---

# Technology Network / 技術ネットワーク

A map of **which technologies, products and discoveries are needed to make which**, from stone tools to AI, with sources, a certainty mark and the provenance of every entry.
石器から AI まで、技術・モノ・発見が「何が無いと何が作れないか」でつながる地図。1つ1つに出典・確かさ・出どころ（だれが書き、どう確かめたか）を付けた。

- Source repository / 元のリポジトリ: {REPO} (CC0). Interactive map / 図: https://taku3i0317-collab.github.io/tech-network/
- Made for an educational Minecraft mod (Semiconductor Lab). The `default` config is **real history only**; how things are made in the game is in the separate `game` config.

## Contents / 中身 (counted from the data)

| | count |
|---|---|
| nodes | {len(nodes)} ({", ".join(f"{k} {v}" for k, v in sorted(kinds.items()))}) |
| edges | {len(edges)} ({", ".join(f"{k} {v}" for k, v in sorted(ekinds.items()))}) |
| glossary terms | {len(glossary)} |
| nodes with at least one source | {with_src} |
| nodes audited by a second AI model | {audited} |
| nodes marked uncertain (⚠️) | {uncertain} |
| nodes with a Wikidata QID (name and year both match) | {wq} |
{f"| benchmark questions | {nb} |" if nb else ""}

Edge kinds: `requires` (cannot be made without), `understanding` (explains why it works), `enables` (an idea that made it possible), `part` (a component), `tool` (used to design or make it, not required), `same` (same mechanism).

## How it was made — please read / どう作ったか（正直に）

- **Most entries were written by AI** ({", ".join(f"{k}: {v}" for k, v in sorted(models.items(), key=lambda x: -x[1]))} by the first commit that added each node), in a project run by one person with several AI sessions. 大部分は AI が書いた。
- Facts were checked against sources (museums, standards bodies, patents, papers, encyclopedias; Wikipedia alone counts as at most ○). Certainty: ◎ two reliable sources agree, ○ one, △ unverified (⚠️).
- {audited} nodes were **audited by a second AI model** (Claude Fable 5.1, 2026-10-20: agree / corrected / unclear per item, see `verification.audit_by_other_model`).
- **No human expert has reviewed the data yet** (`verification.human_reviewed` and `expert_reviewed` are false everywhere). Entries reviewed by people later will get those marks.
- Prerequisites are a teaching simplification: "requires" means "historically needed to make it", chosen with sources, not a complete bill of materials.

## Fields (nodes)

`id`, `name_ja`, `name_en`, `kind` (tech / product / idea), `branch`, `year`, `year_range`, `year_uncertain`, `history_ja`, `fun_fact_ja`, `fun_fact_en`, `sources` (URLs), `references` (certainty and basis), `people` (deceased people only), `requires` (list of alternatives; each alternative is a list of node ids), `parts`, `materials`, `wikidata_qid`, `provenance` (commit, date, task, role, AI model), `verification` (source count, certainty, uncertain mark, audit by another model, human review).
In Parquet files, nested fields are JSON strings.

## Example / 使い方の例

```python
from datasets import load_dataset
nodes = load_dataset("<user>/tech-network", split="nodes")
edges = load_dataset("<user>/tech-network", split="edges")
# everything needed (transitively) to make a smartphone [B-10]
import collections
parents = collections.defaultdict(list)
for e in edges:
    if e["kind"] in ("requires", "enables", "part"):
        parents[e["to"]].append(e["from"])
need, stack = set(), ["B-10"]
while stack:
    for p in parents[stack.pop()]:
        if p not in need:
            need.add(p); stack.append(p)
print(len(need))
```

## Limits / 限界

- Coverage is centred on what the game needs (semiconductors, electricity, chemistry); many fields of technology are thin or missing.
- Years of ancient technologies are approximate; disputed years are marked ⚠️.
- Japanese is the main language; English names and short fun facts exist for every node, long texts are Japanese.
- Wikidata QIDs are given only when the label and a year both match; most nodes have none.

## License

CC0 1.0. Links to third-party images in the source repository are references only and keep their own licenses.
"""
    (OUT / "README.md").write_text(card, encoding="utf-8")
    print(f"wrote {OUT}: nodes {len(nodes)}, edges {len(edges)}, glossary {len(glossary)}, benchmark {nb}, qids {wq}, audited {audited}")


if __name__ == "__main__":
    main()
