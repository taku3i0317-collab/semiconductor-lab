"""へえ①（子ども向けの一言）の末尾に混ざった出典メモ・URL・編集メモ・余分な「）」を外す（S-064）。
翻訳係が見つけた元の文の傷のうち、機械的に外せる物。元の TECH_TREE の文は担当が直す（network_proposals の S-064）。直ったら要らない
使い方：from text_tail import tail（gen_tree_views.py・i18n_export.py）
"""
import re

TAIL = re.compile(
    r"\s*(?:（\s*)?https?://"
    r"|(?:が作った)?\s*、?\s*(?:Wikipedia|Britannica「|International Orthopaedics|Nature \d|DTIC「|CERN「|Google Patents)"
    r"|\s*\(Added in |\s*\(Sources?:|Urmson ほか|「LiDAR history」|「Ambroise Paré」|ペッツバールの伝記"
    r"|\s*CHEMISTRY #\d+|\s*（IMP-\d+[^）]*）|\s*（枝 \[[^\]]*\] 候補）|\s*（\[[^\]]+\] から移動）|\s*（第\d+章 [^）]*の伏線）"
)


def tail(s):
    if not isinstance(s, str):
        return s
    m = TAIL.search(s)
    if m and m.start() > 8:
        s = s[:m.start()]
    s = s.rstrip(" 、")
    while (s.endswith("）") and s.count("）") > s.count("（")) or (s.endswith(")") and s.count(")") > s.count("(")):
        s = s[:-1]
    return s
