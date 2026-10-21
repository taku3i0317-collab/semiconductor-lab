"""各ノードに Wikidata の番号（QID）を付ける候補を探す（S-028）。

名前（日本語・英語）で Wikidata を検索し、ラベル（または別名）が名前と同じで、しかも年が合う物だけを採る。
年は Wikidata の「始まった時（P571）」「発見・発明の時（P575）」「その時（P585）」「出版の日（P577）」から。
年の合い方：ノードの年との差が 10 年まで（昔の物ほど広く：今からの年数の 10% まで）。年がどちらかに無い物は採らない（空のまま）。
Wikidata の API にはゆっくり（1秒に1回まで）頼み、User-Agent を付ける。結果は build/wikidata/qids.json
使い方：python tools/wikidata_match.py
"""
import json
import re
import time
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "build" / "wikidata"
UA = "semiconductor-lab-tech-network/1.0 (https://github.com/taku3i0317-collab/tech-network; educational CC0 dataset)"
API = "https://www.wikidata.org/w/api.php"
YEAR_PROPS = ["P571", "P575", "P585", "P577"]
last = [0.0]


def get(params):
    wait = 1.0 - (time.time() - last[0])
    if wait > 0:
        time.sleep(wait)
    url = API + "?" + urllib.parse.urlencode({**params, "format": "json", "maxlag": "5"})
    for k in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": UA}), timeout=30) as r:
                last[0] = time.time()
                d = json.loads(r.read().decode("utf-8"))
                if "error" in d and d["error"].get("code") == "maxlag":
                    time.sleep(5 * (k + 1)); continue
                return d
        except Exception:
            time.sleep(3 * (k + 1))
    last[0] = time.time()
    return {}


def clean(name):
    return re.sub(r"[（(].*?[）)]", "", name or "").strip()


def wd_years(ent):
    ys = []
    for p in YEAR_PROPS:
        for c in ent.get("claims", {}).get(p, []):
            v = c.get("mainsnak", {}).get("datavalue", {}).get("value", {})
            m = re.match(r"([+-]\d+)-", v.get("time", "")) if isinstance(v, dict) else None
            if m:
                ys.append(int(m.group(1)))
    return ys


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    data = json.loads((ROOT / "docs" / "tech_network.json").read_text(encoding="utf-8"))
    nodes = data["nodes"]
    res, done = {}, OUT / "qids.json"
    if done.exists():
        res = json.loads(done.read_text(encoding="utf-8")).get("nodes", {})
    for i, n in enumerate(nodes):
        if n["id"] in res:
            continue
        names = {("ja", clean(n.get("name"))), ("en", clean(n.get("name_en")))}
        cands = []
        for lang, q in names:
            if not q:
                continue
            d = get({"action": "wbsearchentities", "search": q, "language": lang, "uselang": lang, "limit": 5, "type": "item"})
            for s in d.get("search", []):
                labels = {s.get("label", "").lower()} | {a.lower() for a in s.get("aliases", [])} | {s.get("match", {}).get("text", "").lower()}
                if q.lower() in labels:
                    cands.append(s["id"])
        rec = {"qid": None, "reason": "no exact name match"}
        cands = list(dict.fromkeys(cands))[:6]
        if cands:
            ents = get({"action": "wbgetentities", "ids": "|".join(cands), "props": "claims|labels", "languages": "ja|en"}).get("entities", {})
            y = n.get("year")
            best = None
            for qid in cands:
                ys = wd_years(ents.get(qid, {}))
                if y is None or not ys:
                    continue
                tol = max(10, abs(2026 - y) * 0.1)
                diff = min(abs(v - y) for v in ys)
                if diff <= tol and (best is None or diff < best[1]):
                    best = (qid, diff, ys)
            if best:
                e = ents.get(best[0], {}).get("labels", {})
                rec = {"qid": best[0], "label": (e.get("ja") or e.get("en") or {}).get("value"), "wd_years": best[2], "year_diff": best[1], "reason": "name and year match"}
            else:
                rec = {"qid": None, "candidates": cands, "reason": "name matched but year did not (or no year)"}
        res[n["id"]] = rec
        if i % 20 == 0:
            done.write_text(json.dumps({"nodes": res}, ensure_ascii=False, indent=1), encoding="utf-8")
            print(f"{i}/{len(nodes)}", flush=True)
    matched = sum(1 for r in res.values() if r.get("qid"))
    namehit = sum(1 for r in res.values() if r.get("qid") or r.get("candidates"))
    done.write_text(json.dumps({"rule": "label or alias equals the node name (ja or en), and a Wikidata year (P571/P575/P585/P577) within max(10, 10% of age) years",
                                "matched": matched, "name_matched": namehit, "total": len(res), "nodes": res}, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"matched {matched} / {len(res)} (name only {namehit - matched})")


if __name__ == "__main__":
    main()
