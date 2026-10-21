"""AI の試験問題（ベンチマーク）の見本をグラフから自動で作る（S-028）。build/benchmark/benchmark.jsonl

問題に使うのは、出典が1つ以上あり、確かでない印（⚠️・△）が無いノードだけ（build/provenance.json の verification から）。
つながりは「requires（それが無いと作れない）」だけを使う（補った前提 added も含む）。
問題の種類（乱数の種は決めてあるので、何度作っても同じ）：
  not_required … 「〇〇を作るのに要らない物はどれ？」4択（要る物3つ＝3段までの近い祖先、要らない物1つ＝祖先でも子孫でもない、〇〇より古い物）
  which_first  … 「〇〇を作るのに、前に要るのはどちら？」2択（祖先と、関係の無い物）
  order_chain  … 「作る順に並べて」（前提の鎖 4つ。答えは前提から順）
各問題に根拠（使ったつながり）と、関わるノードの出典を付ける。
使い方：python tools/make_benchmark.py [数=200]
"""
import json
import random
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "build" / "tech-network" / "data"
OUT = ROOT / "build" / "benchmark"
N = int(sys.argv[1]) if len(sys.argv) > 1 else 200


def main():
    rnd = random.Random(20261021)
    nodes = {}
    for p in (SRC / "nodes").glob("*.json"):
        d = json.loads(p.read_text(encoding="utf-8")); nodes[d["id"]] = d["history"]
    prov = json.loads((ROOT / "build" / "provenance.json").read_text(encoding="utf-8"))
    edges = [e for e in json.loads((SRC / "edges.json").read_text(encoding="utf-8")) if e["kind"] == "requires"]
    par, kid = {}, {}
    for e in edges:
        par.setdefault(e["to"], set()).add(e["from"]); kid.setdefault(e["from"], set()).add(e["to"])

    import re
    def src_of(i):   # 出典：URL の欄、本文・へえの中の URL と「出典：…」、参考文献の根拠
        h = nodes[i]; t = " ".join([h.get("real") or "", h.get("hee1") or ""])
        out = list(h.get("sources", [])) + re.findall(r"https?://[^\s、）)」]+", t) + [m.strip() for m in re.findall(r"出典[:：]([^）\n]+)", t)]
        out += [r.get("basis") for r in h.get("refs", []) if r.get("basis")]
        return list(dict.fromkeys(x for x in out if x))[:3]
    def ok(i):
        v = (prov.get(i) or {}).get("verification") or {}
        h = nodes.get(i, {})
        return bool(v.get("sources")) and bool(src_of(i)) and not v.get("uncertain_mark") and v.get("certainty") != "△" and h.get("kind") != "idea" and h.get("year") is not None

    def walk(i, nxt):
        seen, st = set(), [i]
        while st:
            for v in nxt.get(st.pop(), ()):
                if v not in seen:
                    seen.add(v); st.append(v)
        return seen
    good = sorted(i for i in nodes if ok(i))
    anc = {i: walk(i, par) for i in good}
    def near(i, depth=3):   # 問題に使う「要る物」は3段までの近い祖先（遠すぎると問題として不自然）
        seen, fr = set(), {i}
        for _ in range(depth):
            fr = {p for u in fr for p in par.get(u, ()) if p not in seen}; seen |= fr
        return seen
    des = {i: walk(i, kid) for i in good}
    name = lambda i, l: nodes[i].get("name") if l == "ja" else (nodes[i].get("name_en") or nodes[i].get("name"))
    srcs = lambda ids: {i: src_of(i) for i in ids}
    qs, used = [], set()

    def unrelated(t, older=True):
        c = [x for x in good if x != t and x not in anc[t] and x not in des[t] and (not older or nodes[x]["year"] <= nodes[t]["year"])]
        return rnd.choice(c) if c else None

    targets = [t for t in good if len([a for a in near(t) if a in anc]) >= 3]
    rnd.shuffle(targets)
    per = N // 3
    for t in targets:
        if sum(1 for q in qs if q["type"] == "not_required") >= per:
            break
        a3 = rnd.sample(sorted(a for a in near(t) if a in anc), 3); x = unrelated(t)
        if not x:
            continue
        ch = a3 + [x]; rnd.shuffle(ch)
        qs.append({"type": "not_required", "target": t, "choices": ch, "answer": x,
                   "question_ja": f"「{name(t, 'ja')}」を作るのに要らない物はどれ？", "question_en": f"Which of these is NOT needed to make \"{name(t, 'en')}\"?",
                   "choices_ja": [name(c, "ja") for c in ch], "choices_en": [name(c, "en") for c in ch],
                   "evidence": {"needed": a3, "not_ancestor_nor_descendant": x}, "sources": srcs([t, *ch])})
    rnd.shuffle(targets)
    for t in targets:
        if sum(1 for q in qs if q["type"] == "which_first") >= per:
            break
        a = rnd.choice(sorted(x for x in near(t) if x in anc)); x = unrelated(t)
        if not x:
            continue
        ch = [a, x]; rnd.shuffle(ch)
        qs.append({"type": "which_first", "target": t, "choices": ch, "answer": a,
                   "question_ja": f"「{name(t, 'ja')}」を作るのに、前に要るのはどちら？", "question_en": f"Which one is needed before you can make \"{name(t, 'en')}\"?",
                   "choices_ja": [name(c, "ja") for c in ch], "choices_en": [name(c, "en") for c in ch],
                   "evidence": {"ancestor": a, "unrelated": x}, "sources": srcs([t, *ch])})
    rnd.shuffle(targets)
    for t in targets:
        if len(qs) >= N:
            break
        chain = [t]
        while len(chain) < 4:
            ps = sorted(p for p in par.get(chain[-1], ()) if p in anc and p not in chain)
            if not ps:
                break
            chain.append(rnd.choice(ps))
        if len(chain) < 4:
            continue
        order = list(reversed(chain)); key = tuple(order)
        if key in used:
            continue
        used.add(key)
        sh = order[:]; rnd.shuffle(sh)
        qs.append({"type": "order_chain", "target": t, "choices": sh, "answer": order,
                   "question_ja": "前に要る物から順に並べて：" + "・".join(name(c, "ja") for c in sh), "question_en": "Put these in order, prerequisite first: " + ", ".join(name(c, "en") for c in sh),
                   "choices_ja": [name(c, "ja") for c in sh], "choices_en": [name(c, "en") for c in sh],
                   "evidence": {"requires_chain": [[order[k], order[k + 1]] for k in range(3)]}, "sources": srcs(order)})
    for k, q in enumerate(qs, 1):
        q["id"] = f"TN-{k:03d}"
    OUT.mkdir(parents=True, exist_ok=True)
    with open(OUT / "benchmark.jsonl", "w", encoding="utf-8") as f:
        for q in qs:
            f.write(json.dumps({"id": q.pop("id"), **q}, ensure_ascii=False) + "\n")
    c = {}
    for q in qs:
        c[q["type"]] = c.get(q["type"], 0) + 1
    print(f"benchmark: {len(qs)} questions {c}, eligible nodes {len(good)} of {len(nodes)}")


if __name__ == "__main__":
    main()
