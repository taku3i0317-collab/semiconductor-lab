# IMPL4_LOG — 5人目の実装担当（IMP4-番号）の記録

担当：本線 v0.3（IC の工程）と、その前提で未実装のノード。HANDOFF.md は触らず、ここに書く。
CHEMISTRY の行番号は F1〜（docs/chemistry/<幹>.md）。REFERENCES は第2章の節（8 など）を更新、または v0.3 の節に足す。

## IMP4-01（2026-10-20）v0.3 の本線を前提がそろうノードから
- 調べた結果：[9-15] 熱酸化の前提 [9-13] 鏡面研磨（1963）は 1955 より後＝年の逆転。前提から外して [9-12] 切った板にした（TECH_TREE に「前提の訂正」と明記）
- 容器：IMP（A）に確認 → lead_bottle と容器のしくみ（machine/Vessel・Reg.contained）は IMP-33 で IMP が作る。[2-10] フッ酸はその push を待つ。それまで容器の要らないノードを進める
- ✅ [9-15] 熱酸化：酸化炉 oxidation_furnace（block/OxidationFurnaceBlockEntity、画面 OxidationFurnaceScreen、5kW×120秒）。板8＋O2（ドライ、隣のガスホルダーから補充）か水バケツ（ウェット、空のバケツが戻る）→ 酸化膜つきの板8。膜の厚さはデータコンポーネント oxide_nm（Deal–Grove、1100℃：ドライ約125nm・ウェット約660nm、続けて焼くと続きから）。ツールチップに膜の厚さと色（干渉の色見本）。GameTest oxidation_furnace、CHEMISTRY F1〜F4、REFERENCES 8（更新）、遊び方: 重
  - 共通ファイルの変更（1〜数行）：ModDataComponents に OXIDE_NM、client/PurityTooltip に酸化膜の行、SemiconductorLabClient に screen の1行
- ✅ [1-21] 蛍石（[2-10] の前提）：鉱石 fluorite_ore（方鉛鉱と同じ帯、方鉛鉱より少ない、石のツルハシ以上）→ 蛍石。ModWorldGen.RULES に1行（共通ファイル）。GameTest ore_fluorite・ore_biome_rules、CHEMISTRY F5、REFERENCES F1（新しい節「IC の工程（v0.3）」）、遊び方: 軽
- ✅ [2-20] コールタール（レジストの材料の入口）：ガス工場のレトルト gas_retort（block/GasRetortBlockEntity、乾留炉と同じ形、燃料式）。石炭8 → コークス6・安水2・コールタール1。コークス（[1-10] のアイテム）は content/Metallurgy に先に置いた（高炉の使い道は IMP の [1-09] の後）。GameTest gas_retort、CHEMISTRY F6〜F8、REFERENCES F2、遊び方: 重
  - 共通ファイル：SemiconductorLabClient に screen の1行
- ✅ [2-10] フッ酸（IMP-33 の容器を使用）：鉛のレトルト lead_retort（block/HfRetortBlockEntity、画面 HfRetortScreen、燃料式）。蛍石＋硫酸＋空の鉛の瓶 → フッ酸（Reg.contained で鉛の瓶）＋石こう、水バケツ1杯で4回、硫酸のガラス瓶は Vessel.returnEmpty で上に出る。GameTest lead_retort_hf、CHEMISTRY F9〜F10、REFERENCES F3、遊び方: 重
