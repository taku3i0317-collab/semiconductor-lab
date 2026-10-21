"""つながりの候補を機械で集める（S-059）。**候補だけ**：入れる前に出典で確かめる（自動の候補だけでは入れない）。

1. Wikipedia（英語）：ノードの出典にある en.wikipedia.org の記事どうしのリンク。A の記事が B の記事にリンクし、A と B がどちらもノードなら候補
   （両方向にリンクがあれば強い候補）。記事の名前は転送（リダイレクト）をたどって正しい名前にそろえる
2. Wikidata：番号（QID、build/wikidata/qids.json、S-028）があるノードの関係 has part (P527)・made from material (P186)・uses (P2283)・
   based on (P144)・follows (P155)・followed by (P156)・part of (P361)・significant person は人なので使わない
API にはゆっくり（1秒に1回まで）、User-Agent を付ける。結果は build/links/candidates.json（git に入れない）
使い方：python tools/link_candidates.py
"""
import json
import re
import time
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "build" / "links"
UA = "semiconductor-lab-tech-network/1.0 (https://github.com/taku3i0317-collab/tech-network; educational CC0 dataset)"
last = [0.0]


def get(url, params):
    wait = 1.0 - (time.time() - last[0])
    if wait > 0:
        time.sleep(wait)
    full = url + "?" + urllib.parse.urlencode({**params, "format": "json", "maxlag": "5"})
    for k in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(full, headers={"User-Agent": UA}), timeout=30) as r:
                last[0] = time.time()
                d = json.loads(r.read().decode("utf-8"))
                if "error" in d and d["error"].get("code") == "maxlag":
                    time.sleep(5 * (k + 1)); continue
                return d
        except Exception:
            time.sleep(3 * (k + 1))
    last[0] = time.time()
    return {}


def js_obj(p):
    s = p.read_text(encoding="utf-8")
    return json.loads(s[s.index("{"):s.rindex("}") + 1])


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    G = js_obj(ROOT / "docs" / "tech_network_graph.js")
    D = js_obj(ROOT / "docs" / "tech_network_detail.js")["nodes"]
    have = {(f, t) for f, t, k in G["edges"]} | {(t, f) for f, t, k in G["edges"]}
    # ノード → 英語の Wikipedia の記事の名前（出典の URL から、最初の1つ）
    title = {}
    for n in G["nodes"]:
        d = D.get(n["id"], {})
        urls = list(d.get("sources") or []) + [r.get("url") for r in d.get("refs", []) if r.get("url")] + re.findall(r"https?://en\.wikipedia\.org/wiki/[^\s、）)」]+", (d.get("real_full") or "") + (d.get("hee1") or ""))
        for u in urls:
            m = re.match(r"https?://en\.wikipedia\.org/wiki/([^#?]+)", str(u))
            if m:
                title[n["id"]] = urllib.parse.unquote(m.group(1)).replace("_", " ")
                break
    # 出典に Wikipedia が無いノードは、英語の名前を記事の名前として試す（あとで、無い記事・曖昧さ回避は外す）
    guessed = set()
    for n in G["nodes"]:
        if n["id"] not in title and n.get("name_en"):
            title[n["id"]] = re.sub(r"\s*\(.*?\)", "", n["name_en"]).strip()
            guessed.add(n["id"])
    # 転送をたどって正しい名前に（50 ずつ）。無い記事・曖昧さ回避のページは外す
    canon, missing = {}, set()
    names = sorted(set(title.values()))
    for i in range(0, len(names), 50):
        d = get("https://en.wikipedia.org/w/api.php", {"action": "query", "titles": "|".join(names[i:i + 50]), "redirects": 1, "prop": "pageprops", "ppprop": "disambiguation"})
        q = d.get("query", {})
        for pg in q.get("pages", {}).values():
            if "missing" in pg or "disambiguation" in (pg.get("pageprops") or {}):
                missing.add(pg.get("title"))
        nm = {x["from"]: x["to"] for x in q.get("normalized", [])}
        rd = {x["from"]: x["to"] for x in q.get("redirects", [])}
        for t in names[i:i + 50]:
            t2 = nm.get(t, t)
            canon[t] = rd.get(t2, t2)
    node_of = {}
    for nid, t in title.items():
        c = canon.get(t, t)
        if c in missing:
            continue
        node_of.setdefault(c, []).append(nid)
    (OUT / "titles.json").write_text(json.dumps({"guessed_from_name_en": sorted(guessed), "articles": node_of}, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"wikipedia titles: {len(title)} nodes -> {len(node_of)} articles", flush=True)
    # 記事のリンク（記事ごとに、ほかのノードの記事へのリンクだけ残す）
    links = {}
    arts = sorted(node_of)
    for i, a in enumerate(arts):
        out, cont = set(), {}
        for _ in range(10):
            d = get("https://en.wikipedia.org/w/api.php", {"action": "query", "titles": a, "prop": "links", "plnamespace": 0, "pllimit": "max", "redirects": 1, **cont})
            for p in d.get("query", {}).get("pages", {}).values():
                for l in p.get("links", []):
                    out.add(l["title"])
            if "continue" in d:
                cont = d["continue"]
            else:
                break
        links[a] = sorted(x for x in out if x in node_of and x != a)
        if i % 25 == 0:
            print(f"links {i}/{len(arts)}", flush=True)
            (OUT / "wikipedia_links.json").write_text(json.dumps(links, ensure_ascii=False), encoding="utf-8")
    (OUT / "wikipedia_links.json").write_text(json.dumps(links, ensure_ascii=False), encoding="utf-8")
    # ほかのノードの記事の名前を転送先でも引けるように（リンクは転送前の名前で出ることがある）
    cands = {}
    def add(a, b, why):
        if a == b or (a, b) in have:
            return
        k = tuple(sorted((a, b)))
        c = cands.setdefault(k, {"a": k[0], "b": k[1], "why": []})
        if why not in c["why"]:
            c["why"].append(why)
    for a, outs in links.items():
        for b in outs:
            for na in node_of[a]:
                for nb in node_of[b]:
                    add(na, nb, f"wikipedia: {a} → {b}")
    # Wikidata
    qf = ROOT / "build" / "wikidata" / "qids.json"
    qid = {k: v["qid"] for k, v in (json.loads(qf.read_text(encoding="utf-8")).get("nodes", {}) if qf.exists() else {}).items() if v.get("qid")}
    node_by_q = {}
    for k, q in qid.items():
        node_by_q.setdefault(q, []).append(k)
    PROPS = {"P527": "has part", "P186": "made from material", "P2283": "uses", "P144": "based on", "P155": "follows", "P156": "followed by", "P361": "part of"}
    qs = sorted(set(qid.values()))
    for i in range(0, len(qs), 50):
        ents = get("https://www.wikidata.org/w/api.php", {"action": "wbgetentities", "ids": "|".join(qs[i:i + 50]), "props": "claims"}).get("entities", {})
        for q, e in ents.items():
            for p, label in PROPS.items():
                for c in e.get("claims", {}).get(p, []):
                    v = c.get("mainsnak", {}).get("datavalue", {}).get("value", {})
                    tq = v.get("id") if isinstance(v, dict) else None
                    for na in node_by_q.get(q, []):
                        for nb in node_by_q.get(tq, []):
                            add(na, nb, f"wikidata: {na} {label} {nb}")
    for c in cands.values():
        c["mutual"] = sum(1 for w in c["why"] if w.startswith("wikipedia")) >= 2
        c["wikidata"] = any(w.startswith("wikidata") for w in c["why"])
    res = sorted(cands.values(), key=lambda c: (-c["wikidata"], -c["mutual"], c["a"], c["b"]))
    (OUT / "candidates.json").write_text(json.dumps({"rule": "candidates only; verify with sources before adding", "count": len(res), "mutual": sum(c["mutual"] for c in res), "wikidata": sum(c["wikidata"] for c in res), "candidates": res}, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"candidates {len(res)} (mutual wikipedia links {sum(c['mutual'] for c in res)}, wikidata {sum(c['wikidata'] for c in res)})")


if __name__ == "__main__":
    main()
