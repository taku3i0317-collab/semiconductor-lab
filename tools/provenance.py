"""各ノードの出どころ（provenance）と確かめ方（S-028）。build/provenance.json に書く。

出どころ：git の履歴から、ノードの見出し「### [番号]」を最初に足したコミット（日付・作者・タスクの番号・担当・AI のモデル名）。
  担当はタスクの番号の頭から：IMP・タスク＝実装担当（A）、NET・S-・O-＝ネットワーク担当（B）。2026-10-20 より前（担当を分ける前）は1つのセッション。そのほか＝不明。
  AI のモデル名はコミットの「Co-Authored-By: Claude …」から（無ければ空）。
確かめ方：出典の数、確かさ（REFERENCES の ◎○△）、別のモデルの監査（AUDIT_RESULTS の V- の行：同意・修正・不明）、人の目（今は無し）。
使い方：python tools/provenance.py
"""
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FILES = ["docs/TECH_TREE.md", "docs/products.md", "docs/ideas.md"]


def role(subject, date=""):
    s = subject.strip()
    if date and date < "2026-10-20" and not re.match(r"(IMP|NET|S-\d|O-\d)", s):
        return "single session (before the A/B split on 2026-10-20)"
    if re.match(r"(IMP|タスク|task)", s, re.I):
        return "A (implementation)"
    if re.match(r"(NET|S-\d|O-\d|N-\d)", s):
        return "B (technology network)"
    return "unknown"


def main():
    p = subprocess.run(["git", "log", "--reverse", "--format=@@COMMIT\x1f%H\x1f%an\x1f%aI\x1f%s\x1f%b\x1e", "-p", "-U0", "--", *FILES],
                       cwd=ROOT, capture_output=True, text=True, encoding="utf-8", errors="replace").stdout
    first = {}
    cur = None
    for chunk in p.split("@@COMMIT\x1f")[1:]:
        head, _, diff = chunk.partition("\x1e")
        h, an, date, subj, body = (head.split("\x1f") + [""] * 5)[:5]
        model = re.findall(r"Co-Authored-By:\s*(Claude[^<\n]*)", body)
        cur = {"commit": h[:10], "date": date[:10], "author": an, "task": (re.match(r"\S+", subj) or [""])[0], "role": role(subj, date[:10]),
               "ai_model": model[0].strip() if model else None}
        for m in re.finditer(r"^\+### \[((?:\d+|B|P|I)-\d+[a-z]?)\]", diff, re.M):
            first.setdefault(m.group(1), cur)
    data = json.loads((ROOT / "docs" / "tech_network.json").read_text(encoding="utf-8"))
    det = (ROOT / "docs" / "tech_network_detail.js").read_text(encoding="utf-8")
    D = json.loads(det[det.index("{"):det.rindex("}") + 1])["nodes"]
    # 別のモデルの監査：docs/AUDIT_RESULTS.md の行（V-番号｜[ノード]｜同意・修正・不明）。監査を待っているだけ（AUDIT_QUEUE）の物は数えない
    res = (ROOT / "docs" / "AUDIT_RESULTS.md").read_text(encoding="utf-8") if (ROOT / "docs" / "AUDIT_RESULTS.md").exists() else ""
    audits = {}
    for m in re.finditer(r"^\| (V-\d+) \| \[((?:\d+|B|P|I)-\d+[a-z]?)\][^|]*\| (同意|修正|不明) \|", res, re.M):
        audits.setdefault(m.group(2), []).append({"item": m.group(1), "verdict": {"同意": "agree", "修正": "corrected", "不明": "unclear"}[m.group(3)]})
    who = (re.search(r"監査者：([^。\n]*)", res) or [None, None])[1]
    out = {}
    for n in data["nodes"]:
        i, d = n["id"], D.get(n["id"], {})
        cert = " ".join(r.get("certainty", "") for r in d.get("refs", []))
        text = " ".join([d.get("real_full", ""), d.get("hee1", "")])
        nsrc = len(d.get("sources", [])) + len(re.findall(r"出典[:：]", text)) + len(d.get("refs", []))
        out[i] = {"added": first.get(i), "verification": {
            "sources": nsrc,
            "certainty": "◎" if "◎" in cert and "△" not in cert else "○" if "○" in cert or "◎" in cert else "△" if "△" in cert else None,
            "uncertain_mark": bool(n.get("year_uncertain")) or "⚠" in text,
            "audit_by_other_model": {"by": who, "date": "2026-10-20", "items": audits[i]} if i in audits else None,
            "human_reviewed": False, "expert_reviewed": False}}
    (ROOT / "build").mkdir(exist_ok=True)
    (ROOT / "build" / "provenance.json").write_text(json.dumps(out, ensure_ascii=False, indent=1), encoding="utf-8")
    roles, models = {}, {}
    for v in out.values():
        a = v["added"] or {}
        roles[a.get("role", "not found")] = roles.get(a.get("role", "not found"), 0) + 1
        models[a.get("ai_model") or "none recorded"] = models.get(a.get("ai_model") or "none recorded", 0) + 1
    print(json.dumps({"nodes": len(out), "roles": roles, "models": models,
                      "with_source": sum(1 for v in out.values() if v["verification"]["sources"]),
                      "audited": sum(1 for v in out.values() if v["verification"]["audit_by_other_model"])}, ensure_ascii=False))


if __name__ == "__main__":
    main()
