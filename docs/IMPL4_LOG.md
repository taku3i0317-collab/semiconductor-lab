# IMPL4_LOG — 5人目の実装担当（IMP4-番号）の記録

担当：本線 v0.3（IC の工程）と、その前提で未実装のノード。HANDOFF.md は触らず、ここに書く。
CHEMISTRY の行番号は F1〜（docs/chemistry/<幹>.md）。REFERENCES は第2章の節（8 など）を更新、または v0.3 の節に足す。

## IMP4-01（2026-10-20）v0.3 の本線を前提がそろうノードから
- 調べた結果：[9-15] 熱酸化の前提 [9-13] 鏡面研磨（1963）は 1955 より後＝年の逆転。前提から外して [9-12] 切った板にした（TECH_TREE に「前提の訂正」と明記）
- 容器：IMP（A）に確認 → lead_bottle と容器のしくみ（machine/Vessel・Reg.contained）は IMP-33 で IMP が作る。[2-10] フッ酸はその push を待つ。それまで容器の要らないノードを進める
- ✅ [9-15] 熱酸化：酸化炉 oxidation_furnace（block/OxidationFurnaceBlockEntity、画面 OxidationFurnaceScreen、5kW×120秒）。板8＋O2（ドライ、隣のガスホルダーから補充）か水バケツ（ウェット、空のバケツが戻る）→ 酸化膜つきの板8。膜の厚さはデータコンポーネント oxide_nm（Deal–Grove、1100℃：ドライ約125nm・ウェット約660nm、続けて焼くと続きから）。ツールチップに膜の厚さと色（干渉の色見本）。GameTest oxidation_furnace、CHEMISTRY F1〜F4、REFERENCES 8（更新）、遊び方: 重
  - 共通ファイルの変更（1〜数行）：ModDataComponents に OXIDE_NM、client/PurityTooltip に酸化膜の行、SemiconductorLabClient に screen の1行
