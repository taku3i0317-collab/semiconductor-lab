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
- ✅ [9-06] 酸化膜エッチング：エッチング槽 etch_tank（block/EtchTankBlockEntity、画面 EtchTankScreen、室温・電気なし）。酸化膜つきの板8＋フッ酸 → シリコンの板8、時間は膜の厚さ÷20nm/秒、鉛の瓶が上に戻る。窓開けは露光の後。GameTest etch_tank、CHEMISTRY F11、REFERENCES F4、遊び方: 軽
- A（IMP）と相談：LITHO_DESIGN §8 の未定のうち [9-25] Al 配線（とその前提の鎖）と [9-13] 研磨（reclaim）は E（IMP4）が担当。エッチング槽・酸化炉は ChipOps（etchOxide・reoxidize）が push されたらつなぐ。レジスト（[2-30]/[2-32]）は C（IMP2）
- ✅ [1-24] ボーキサイト：鉱石 bauxite_ore（ジャングル・サバンナ、Y 56〜90 の土・石）→ ボーキサイト。ModWorldGen.RULES に1行。GameTest ore_bauxite・ore_biome_rules、CHEMISTRY F12、REFERENCES F5、遊び方: 軽
- ✅ [1-14] バイヤー法：溶解釜 bayer_digester（block/BayerDigesterBlockEntity、画面 BayerDigesterScreen、2kW）。ボーキサイト2 → Al(OH)3＋赤泥、NaOH＋水の液で4回、Al(OH)3 →かまど→ アルミナ。GameTest bayer_process、CHEMISTRY F13〜F14、REFERENCES F6、遊び方: 重
- ✅ [1-38] 氷晶石の合成：作業台で フッ酸3＋Al(OH)3＋NaOH3 → 氷晶石（鉛の瓶3本が戻る）。GameTest cryolite_synthesis、CHEMISTRY F15、REFERENCES F7、遊び方: 軽
- ✅ [1-29] アルミニウム：ホール・エルー電解槽 hall_heroult_cell（block/HallHeroultCellBlockEntity、画面 HallHeroultCellScreen、100kW）。アルミナ2 → アルミ1（15kWh）、氷晶石1で16回・黒鉛棒1で2回。c:ingots/aluminum。GameTest hall_heroult、CHEMISTRY F16、REFERENCES F8、遊び方: 重
- ✅ 露光の工程へのつなぎ（A の litho/ChipOps）：エッチング槽に現像ずみの process_wafer（1〜8枚）→ ChipOps.etchOxide で窓開け、酸化炉に窓の開いた process_wafer → ChipOps.reoxidize で閉じ直し＋膜が育つ。GameTest etch_and_reoxidize_windows

## O-003（2026-10-20）IC の工程で層を作る部分（INBOX、LITHO_DESIGN）
- O-001 は完了（2244e9c まで）、O-002 は作業なし（硝酸は IMP2 の Gas.NITRIC_ACID を使う）
- ✅ [9-25] 金属配線：①真空蒸着装置（IMP2 の EvaporatorBlockEntity に分岐を追記：process_wafer 1〜8枚＋アルミ1 → ChipOps.depositMetal。RECIPES と既存の動きはそのまま。C が不在だったので A に了承をもらって追記）②リン酸（骨灰1＋硫酸3＋瓶2 → 2）③エッチング槽がリン酸で ChipOps.etchMetal。GameTest aluminium_wiring（NOT の例が蒸着装置・槽を通って4つの発見）、CHEMISTRY F17〜F19、REFERENCES F9、遊び方: 重
- ✅ [9-13] ラッピング・鏡面研磨：研磨機 wafer_polisher（block/WaferPolisherBlockEntity、画面 WaferPolisherScreen、1kW）。加工中のウェハー＋砥粒（アルミナ・ベンガラ）＋コロイダルシリカ → ChipOps.reclaim（2回まで）。水ガラス・コロイダルシリカ（作業台）。GameTest wafer_polisher、CHEMISTRY F20〜F22、REFERENCES F10、遊び方: 軽
- 報告：自分用のルールを CLAUDE.local.md に書こうとしたが、権限で止められた（INBOX の「CLAUDE.md の自分用のルールにも書く」は未達。ルールの中身はこの LOG の冒頭と INBOX どおりに守っている）

## O-004（2026-10-20）[9-14] 洗浄の前提の薬品
- 調べた結果：[2-19] 過酸化水素の電解法（1908 ヴァイセンシュタイン）は、硫酸を白金の陽極で電解して過硫酸にする（高い電流密度で O2 より先に S2O8 ができるのは白金だけ）。白金 [1-20][1-18] が未実装なので、前提から作る：[1-20] 砂白金 → [1-18] 王水で精製（塩酸✅・硝酸✅ IMP2・塩化アンモニウム＝[2-21]＋塩酸）→ [2-19]。TECH_TREE の [2-19] の前提に白金を足す
- ✅ [2-21] アンモニア水：アンモニア蒸留器 ammonia_still（block/AmmoniaStillBlockEntity、画面 AmmoniaStillScreen、1kW）。安水4＋消石灰＋瓶 → アンモニア水（ガラス瓶）、水バケツ1杯で4回。GameTest ammonia_still、CHEMISTRY F23、REFERENCES F11、遊び方: 軽
