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
- ✅ [1-20] 砂白金：砂鉄のルートテーブルに約3%の砂白金（シルクタッチなし）。block_drops テストは砂鉄だけ砂白金も許す形に。GameTest placer_platinum、CHEMISTRY F24、REFERENCES F12、遊び方: 軽
- ✅ [1-18] 白金の精製：塩化アンモニウム、王水で塩化白金酸、沈殿、海綿、圧粉、溶鉱炉でインゴット（作業台・かまど・溶鉱炉、硝酸は IMP2 の物）。GameTest platinum_refining、CHEMISTRY F25〜F29、REFERENCES F13、遊び方: 軽
  - 注意（全担当向け）：Metallurgy に Vessel.GLASS_BOTTLE 入りの物を置くと、Chemistry が先に初期化されて Chemistry.HYDROFLUORIC_ACID の鉛の瓶（Metallurgy.LEAD_BOTTLE）が null になり、GameTest vessels が落ちた。中身入りの液は Chemistry に置く
- ✅ [2-19] 過酸化水素：電解槽 peroxide_cell（block/PeroxideCellBlockEntity、画面 PeroxideCellScreen、3kW）。白金の陽極＋硫酸（8回で1）＋水（4回で1）＋空の瓶 → H2O2（ガラス瓶）＋水素。TECH_TREE の前提に [1-18] 白金を足した。GameTest peroxide_cell、CHEMISTRY F30、REFERENCES F14、遊び方: 重

## O-005（2026-10-20）[9-14] 洗浄
- ✅ [2-16] 蒸留水（段階1 のすすぎ）：作業台で 水バケツ＋石英の蒸留器＋瓶3 → 蒸留水3。GameTest distilled_water、CHEMISTRY F31、REFERENCES F15、遊び方: 軽
- 出典で確かめた：Kern 1990（J. Electrochem. Soc. 137, 1887）は 1950年代初めからの洗浄の歴史を扱い、RCA 以前はクロム酸混液などで汚染・廃液の問題があったと書く。日本半導体歴史館は、イオン交換の超純水が 1950年代に始まった半導体工業を支えたと書く（樹脂 1935、工業化 1938、スチレン系 1944）
- 結論（TECH_TREE [9-14] に書いた）：段階1＝1950年代の酸洗浄（硝酸で煮る → フッ酸 → 水すすぎ。水はまず蒸留水、[2-17] ができたら脱イオン水）、段階2＝RCA（SC-1・SC-2、脱イオン水）。[9-19] の前提は段階1
- **判断待ち（A と設計担当へ）**：洗浄の効き目をチップのデータにどう残すか。案：ChipState に「洗った」印（または汚れの度合い）を足し、ChipOps.coat（と拡散・酸化の前）がそれを条件にする／削る・はがす・拡散のあとは印が消える（本物も炉に入れる前に毎回洗う）。印を足すのは A の litho のファイルなので A に頼んだ。返事が来るまで洗浄の機械は作らない（ほかの項目を先に進める）
- A と合意：ChipState に int contamination（操作ごとに +1、ChipOps.clean で 0）。失敗にはしない（ツールチップと v0.5 の歩留まりへ）。硬い条件にするかの質問は A が LOG・Discord に出す。A が litho に入れたら、E が洗浄槽（段階1：硝酸＋フッ酸＋蒸留水 → ChipOps.clean）を作る。それまで待機（INBOX は6時間待つ、ユーザーの指示）
- ✅ [9-14] 洗浄 段階1：洗浄槽 cleaning_bench（block/CleaningBenchBlockEntity、画面 CleaningBenchScreen、1kW）。加工中のウェハー＋硝酸＋フッ酸＋蒸留水 → ChipOps.clean(w, 0)。GameTest cleaning_bench（NOT の例の5層ごとに洗っても4つの発見）、CHEMISTRY F32、REFERENCES F16。段階2（RCA）は [2-17] 脱イオン水（[2-28] フェノール樹脂＝C のレジストの鎖）の後

## O-007（2026-10-20）IC の工程の範囲で今すぐ作れるノード
- O-006（レジスト・塗布・現像は E、材料は D）は材料待ち（環化ゴムはあるが感光剤ビスアジド・キシレンがまだ、ノボラックもまだ）。O-007 を先に
- ✅ [9-17] メサトランジスタ：蒸着装置（C）の分岐に「p-n 接合の板＋アルミ → Al の点の板」を足した（自分が足した分岐の中、既存は変えない）。かまどで合金、ハニカムでろう、混酸（フッ酸＋硝酸、鉛の瓶）でエッチング槽、金の粒で4個。item/MesaTransistorItem：持ち物の中で β が下がる（inventoryTick、データコンポーネント current_gain）。API_26X に inventoryTick・appendHoverText・transmuteCopy を記録。GameTest mesa_transistor、CHEMISTRY F33〜F36、REFERENCES F17、遊び方: 実験
- ✅ [9-09] FZ 法：FZ 炉 fz_puller（block/FzPullerBlockEntity、画面 FzPullerScreen、20kW）。るつぼなし、酸素 0.05 ppma（新しいデータコンポーネント oxygen_ppma、ウェハー切断で受け継ぐ＝WaferSawBlockEntity に2行）。ツールチップ（PurityTooltip）に酸素。年と人を出典で確かめ ⚠️ を外した。GameTest fz_puller、CHEMISTRY F37、REFERENCES F18、遊び方: 重

## O-006（2026-10-20）レジストと塗る・焼く工程（E の担当に確定）
- 材料（D）：ノボラック・DNQ・キシレン・環化ゴムはそろった。ネガ型の感光剤ビスアジドは D の判断待ち（Q3-6）→ ネガ型 [2-30] は止めて、ポジ型から
- ✅ [2-32] ポジ型レジスト：作業台で ノボラック＋DNQ＋キシレン＋瓶 → positive_resist（ガラス瓶、Chemistry の containedItem＝Reg.contained と同じことを専用クラスで）。日なたで感光（item/PositiveResistItem、D の DnqItem と同じしくみ）
- ✅ [9-19] レジスト塗布機 spin_coater（block/SpinCoaterBlockEntity、画面 SpinCoaterScreen、500W）：1〜8枚＋レジスト → ChipOps.coat。GameTest spin_coater（NOT の例の全部の塗布を塗布機で → 4つの発見）、CHEMISTRY F38〜F39、REFERENCES F19
- **止まっている所（判断・前提待ち）**：[9-21] 現像は前提 [9-20] 露光が未実装。[9-20] 密着露光（「C か E」）は前提 [8-17] アライメント（← [8-10][8-08][8-06]、幹8＝IMP2）が ❓。LITHO_DESIGN では v0.3 は合わせのずれなし。案：①[9-20] の前提から [8-17] を外し「顕微鏡で手で合わせる」1960年の形にする（ずれは v0.5）②IMP2 が [8-17] を作るのを待つ。ユーザー（設計担当）の判断を LOG と Discord で聞く。その間は ③現像液（薄い NaOH 水溶液）だけ先に作る、などで止まらない
- O-008 を読んだ：[9-20] 密着露光は C、E は塗布・現像・はがし。前の質問（[8-17] を外すか）は C の判断になったので取り下げ
- ✅ レジストはがし（LITHO §10 の 8）：はがし槽 resist_stripper（block/ResistStripperBlockEntity、画面 ResistStripperScreen、500W）。硫酸＋過酸化水素 → ChipOps.strip。GameTest resist_stripper、CHEMISTRY F40。現像 [9-21] は C の [9-20] の後（現像液は薄い NaOH の予定）
- 待機（18:29〜）：現像 [9-21] は C の露光 [9-20] の push 待ち、ネガ型 [2-30] は D のビスアジド（Q3-6）待ち。INBOX は6時間待つ
- C の [9-20] マスクアライナーが push された → ✅ [9-21] 現像：現像液（NaOH＋水＋瓶3 → 3）、現像槽 developer_bench（block/DeveloperBenchBlockEntity、画面 DeveloperBenchScreen、300W）→ ChipOps.develop。GameTest developer_bench（塗布機・現像槽・はがし槽を通して NOT の例 → 4つの発見）、CHEMISTRY F41〜F42、REFERENCES F20、遊び方: 重。LITHO §10 の E の分（4 塗布・6 現像・8 はがし）はそろった
- A（O-010）が v03_chain を全部実機にし、発見に monolithic_ic を足した（NOT の例は5つ）。テストは Discoveries.all().size() で数える

## O-009（2026-10-20）ネガ型は KPR（ユーザーの決定）
- ビスアジドは作らない（作り方をレシピにもデータにも入れない）。ネガ型 [2-30] は KPR＝ポリケイ皮酸ビニル（材料は D、今はケイ皮酸・塩化シンナモイルまで）
- 環化ゴム（D の物）の扱い（E の判断）：レジストのレシピには使わない（KTFR はビスアジドが要るため）。[2-30] の本物欄・note で「1957〜 の KTFR の土台だった」と触れるだけにする。環化ゴムそのものの使い道は D に任せる
- ChipOps はポジ型だけなので、A にネガ型の扱い（レジストの型 tone、coat(w, tone)、ネガ型はキシレンで現像）を頼んだ。D の材料と A の返事が来たら、ネガ型のレジスト・塗布機・現像槽を合わせ、v03_chain のネガ型版で NOT を確かめる
- ✅ 現像槽のネガ型：A の ChipOps（tone・develop(w, Developer)）に合わせ、現像液にキシレン（D の物、有機溶剤）を受け付ける。型と合わなければ「現像液が型と合わない」。GameTest developer_bench_negative。ネガ型レジスト（KPR）の液と塗布機の対応は D のポリケイ皮酸ビニル待ち
- ✅ [2-30] ネガ型レジスト KPR：作業台で ポリケイ皮酸ビニル（D）＋キシレン＋瓶 → negative_resist（日なたで感光）。塗布機が KPR ならネガ型で塗る（ChipOps.coat NEGATIVE）。GameTest v03_chain_negative（A の v03_chain を写し、レジストと現像液だけ KPR・キシレンに）で NOT が見つかる。CHEMISTRY F43、REFERENCES F21。O-009 完了
- A（O-014）の依頼：フッ酸の判定を Chemistry.HF_TAG に（洗浄槽・エッチング槽・cryolite.json・silicon_etch.json）。鉛のレトルトは空の PE の瓶も受け付けて hydrofluoric_acid_pe を詰める（瓶で決まる）。GameTest lead_retort_hf に PE の確認を追加
- ✅ [9-14] 段階2 RCA 洗浄台 rca_bench（block/RcaBenchBlockEntity、画面 RcaBenchScreen、1.5kW）：NH3水＋HCl＋H2O2 2＋脱イオン水（D の [2-17]）か超純水 → ChipOps.clean(w, 0)。段階1 の洗浄槽のすすぎに脱イオン水・超純水も。[9-14] を ✅ に。GameTest rca_bench、CHEMISTRY F44

## O-010（2026-10-20）RCA の差と後工程
- ✅ 1. [9-14] 段階2 は前の push で ✅。O-010 の「落ち方の差」：段階1 の洗浄槽は ChipOps.clean(w, 1)（汚れ 1 まで＝金属が残る）、RCA は clean(w, 0)。A の v03Chain の「洗浄で 0」の行を「段階1 で 1 以下」に直した（A に連絡）。GameTest rca_bench に差の確認
- **判断待ち（O-010 の 2）**：[9-33] ワイヤーボンディングの前提 [1-58] 伸線は TECH_TREE で「MC: 未定（実装の前に相談）」。案：
  ① [1-58] を作る：引き板（鉄インゴット3＋火打ち石、穴が段々に小さくなる鉄の板、作業台で戻る道具）＋金インゴット → 金線8。ボンディング用の細い線（直径約25µm）はダイヤモンドのダイス（ダイヤモンド＋鉄、戻る道具）でもう一度引く → 金のボンディング線。電線（銅線）にも使える
  ② [1-58] は作らず、[9-33] はバニラの金の粒をそのまま線として使う（INBOX O-010 の書き方「金（バニラの金）」どおり。省略として placeholder に書く）
  おすすめは ①（化学ルール1：金の粒は線ではない）。決まるまで [9-33]・[9-34] は止め、INBOX を待つ
  - 後工程の設計案（決まったら）：[9-33] ボンダー（チップ＋パッドの数だけ金線＋ヘッダー（鋼＋ガラスの封着）→ 線をつないだチップ、CHIP_STATE を写す）、[9-34] 封止機（金属缶＋乾いた窒素 → 金属缶のチップ／エポキシ成形材（D）→ プラスチックのチップ、1960年代後半）

## S-001（2026-10-20）[1-58] 伸線 → [9-33] → [9-34]（答え①）
- ✅ [1-58] 伸線：引き板・ダイヤモンドのダイス（ReusableToolItem）、金線・金のボンディング線・銅線。GameTest wire_drawing、CHEMISTRY F45、REFERENCES F22、DECISIONS に1行
- ✅ [9-33] ワイヤーボンディング：ヘッダー（鋼＋ガラス＋銅線）、ボンダー wire_bonder（block/WireBonderBlockEntity、画面 WireBonderScreen、200W）。パッドの数は A の Netlist で数える（読むだけ）、CHIP_STATE は transmuteCopy で写す。GameTest wire_bonder、CHEMISTRY F46、REFERENCES F23
- ✅ [9-34] 封止：金属缶、封止機 package_sealer（block/PackageSealerBlockEntity、画面 PackageSealerScreen、500W）：缶＋窒素 → 金属缶の IC、エポキシ成形材（D）→ プラスチックの IC。CHIP_STATE を写す。GameTest package_sealer、CHEMISTRY F47、REFERENCES F24。S-001 完了

## O-011（2026-10-20）イオン注入・アニール・スパッタ
- 前提を調べた：[9-28] は [9-47] 加速器・[10-01] 電磁石・[8-33] 質量分析が ❓。[9-26] は [5-06] 油拡散ポンプ（幹5＝C）が ❓。A に [8-33] の担当とイオン注入の ChipOps（implant・anneal）を相談中
- ✅ [10-01] 電磁石：幹10 のファイル content/Control.java を作った（Content.java・lang_build の ORDER・gametest の fabric.mod.json に1行、ControlTests）。鉄＋銅線8 → 電磁石。CHEMISTRY F48、REFERENCES F25
- ✅ [9-47] 加速器の部品：コッククロフト・ウォルトン回路・加速管（部品アイテム、イオン注入装置に組みこむ）。サイクロトロンは作らない。GameTest accelerator_parts、REFERENCES F26。[8-33]・[5-06] は C に相談中（A の答え：幹8 は C か司令塔に）。イオン注入の ChipOps は A が入れた（implant・anneal）
- C の答え：[8-33]・[5-06] とも E が作る（Precision.java・Vacuum.java に追記してよい）
- ✅ [8-33] 質量分析器（item/MassSpectrometerItem、Precision.java の末尾に1行）：試料の同位体と天然存在比。GameTest mass_spectrometer（PrecisionTests）、CHEMISTRY F49、REFERENCES F27
- ✅ [9-28] イオン注入装置 ion_implanter（block/IonImplanterBlockEntity、画面 IonImplanterScreen、5kW）：A の ChipOps.implant。隣の真空容器 0.01Pa 以下。GameTest ion_implanter、CHEMISTRY F50、REFERENCES F28
- ✅ [9-29] アニール炉 anneal_furnace（block/AnnealFurnaceBlockEntity、画面 AnnealFurnaceScreen、3kW、N2/Ar をホルダーから）：A の ChipOps.anneal。注入しただけでは層が同じ。GameTest anneal_furnace、CHEMISTRY F51、REFERENCES F29
- ✅ [5-04]③ シリコーン油（block/SiliconeReactorBlockEntity、3つの反応）・[5-06] 油拡散ポンプ（block/OilDiffusionPumpBlockEntity、水銀拡散ポンプを継承：C の了承どおり DiffusionPumpBlockEntity に protected の入口1つだけ追加。RotaryPump・Turbo は instanceof のまま変更なし）。GameTest silicone_oil・oil_diffusion_pump、CHEMISTRY F52〜F53、REFERENCES F30
- ✅ [9-26] スパッタ装置 sputter（block/SputterBlockEntity、画面 SputterScreen、5kW、Ar をホルダーから）：A の答え（4bdc43ad）で案②：ChipOps.depositMetal(w, true)＝conformalMetal の印、回路は蒸着と同じ（v0.5 の歩留まりで使う）。GameTest sputter、CHEMISTRY F54、REFERENCES F31
- O-011 完了（2ddd5452 まで、274/274）。次は INBOX の新しい項目を待つ
- O-012 開始。A へ相談（SendMessage）：[9-27] の液とドライの差（案α：方眼 36 以上で液は窓が1マス太る／案β：記録だけ）、多層化の案（A＝作らない／B＝METAL2＋VIA の最小2層／C＝一般化）
- ✅ [9-27] フッ素化の反応器（黒鉛＋F2 → CF4 8袋）・ドライエッチング装置（block/DryEtcherBlockEntity、3kW、0.1Pa 以下、A の答えで案α：ChipOps.etchOxide(w, true)、液は方眼 36 以上で1マス太る）。GameTest dry_etcher、CHEMISTRY F55〜F56、REFERENCES F32
- **O-012 の2 多層化：ユーザーの決定待ち（作らない）**。A と相談した案：
  - 案A：作らない（v0.4 も金属1層のまま。2層目は v0.5 の CMP の後）
  - 案B：最小の2層（A の見立てでは自然）。LayerKind に VIA・METAL2 を足し、層間膜（ChipOps.depositInterlayer）→ VIA の窓 → METAL2 → 削る。Netlist は METAL と METAL2 が VIA でつながる、を足すだけ。判定・お題・ハンコはそのまま。製図台の層の並びは Q21-3（工程ごとの切りかえ）と同じ仕組みで2層を足す。手間：A が ChipOps・Netlist・ChipModel に半日ほど、E が層間膜（CVD の SiO2）の機械
  - 案C：層を一般化（METAL[n]・VIA[n]）。A の見立てでは v0.5 まで要らない
  - 年：2層の金属配線は 1960年代の終わりから ⚠️（出典で確かめる）
  - Discord でユーザーに質問した。答えが来たら、ChipOps・Netlist は A、層間膜の機械と VIA のエッチングは E
- 待機終了（2026-10-21 05:29、INBOX が6時間変化なし。O-012 の2 多層化はユーザーの決定待ちのまま）
- S-002 開始（ユーザーの決定：多層化＝案B）。A へ分担の相談（SendMessage）：A＝ChipOps.depositInterlayer・VIA・METAL2・Netlist・2層の例、E＝CVD 炉（SiH4 + O2 → SiO2、約400℃）とシラン
- ✅ [9-30] の一部：シランの発生器（block/SilaneGeneratorBlockEntity、Mg2Si → SiH4）。GameTest silane_generator、CHEMISTRY F57。CVD 炉は A の depositInterlayer を待つ
- ✅ [9-30] CVD 炉（block/CvdFurnaceBlockEntity、2kW、ChipOps.depositInterlayer）と2層配線の通し（GameTest v03_chain_two_metal：CVD → ドライで VIA → スパッタで2層目 → METAL2、パッドが同じ線）。CHEMISTRY F58、REFERENCES F33。2層配線の年 1970年代は △
- S-002 の1 完了（CVD 炉・2層配線、289/289）。ROADMAP v0.4 #3・#4 にチェック。S-002 の2：v0.5 #2 [9-31] CMP の ChipState 側（段差 steps と planarize、DOF との組）を A に相談中
- ✅ [9-31] CMP 装置（block/CmpPolisherBlockEntity、2kW、A の ChipOps.planarize e2da4075）。GameTest cmp_polisher、CHEMISTRY F59、REFERENCES F34、ROADMAP v0.5 #2
- S-002 完了（6310b435、292/292）。v0.5 で E の範囲の残り：#7 FinFET は前提 [1-70] ハフニウム・[7-16] 液浸が ❓、[9-36] 銅配線は [1-66] タンタルが ❓。ほかは A・光学の担当。INBOX を待つ
- O-013 開始。1＝[9-31] CMP は S-002 で済（6310b435）。2＝[9-36] 銅配線：**前提 [1-66] タンタルが ❓（TECH_TREE の MC 欄「未定（実装の前に相談）」）→ ユーザーに質問（Discord）、作らない**。案：
  - 案A：タンタルを本物どおり作る（新しい鉱石＝タンタル石（コルンブ石）をペグマタイトに生成 → フッ酸で溶かし K2TaF7（1866 マリニャック法、フッ酸・フッ化カリウムは既にある）→ ナトリウムで還元）。鉱石を足すので worldgen（幹1）に触れる
  - 案B：バリア膜を窒化チタン TiN で近似（チタンは IMP3 で済）。⚠️ 銅配線のバリアは本物は Ta/TaN が主流なので「仮」になる
  - 案C：銅配線は後回し（タンタルの扱いが決まるまで）
  3＝[9-30] エピタキシー・窒化膜を先に進める（A に ChipOps 側を相談中）
- ✅ [9-30] エピタキシャル炉（block/EpitaxyReactorBlockEntity、10kW、データ部品 epi（A、"n"／"p"。塗るとき substrate＝エピ層の型）、酸化炉で受け継ぐ）。GameTest epitaxy_reactor、CHEMISTRY F60、REFERENCES F35。ChipState への反映（基板の型）は A に相談中
- ✅ [9-30] エピは A の部品 EPI（"n"/"p"、d5814a71）に合わせた（自分の EPI_LAYER は消した）。窒化膜：CVD 炉に NH3 → ChipOps.passivate。CHEMISTRY F61、REFERENCES F36。[9-30] ✅
- O-014 開始：[9-37] FinFET は「案と REFERENCES まで、作らない」。前提の状態：[9-24] CMOS ✅（A）・[9-27] ✅・[9-30] ✅・[7-16] 液浸 ✅、**[1-70] ハフニウム（high-k）は ❓**（FinFET の量産は high-k＋金属ゲートと組。作るときは [1-70] が先か、SiO2 ゲートで近似の「仮」か、も選ぶ）
- **FinFET の案（A に相談、ユーザーに Discord で質問）**：
  - 案A（判定に入れる・おすすめ候補）：細い方眼（例：ARF_IMMERSION 104 以上＝CD 約40nm 以下）の平らな MOS は、ゲートの下を電流が漏れる（短チャネル効果）＝ A の Circuit の漏れ道（今は汚れだけ）にゲートの漏れを足す → NOT などのお題が不合格・歩留まりが下がる。FinFET の工程（FIN の層：ドライエッチングで細い板を立てる。液のエッチングは断る → 酸化膜でうめて CMP → 少し削って頭を出す → ゲートが3方向から囲む）で漏れが消える。手間：A＝LayerKind FIN・ChipModel・Circuit、E＝フィンの工程（既存のドライエッチング・CVD・CMP の分岐）
  - 案B（記録だけ）：FIN の層と ChipState の印だけ足し、判定は変えない（スパッタ・CMP と同じ形）。手軽だが「漏れが減る」体験はない
  - 案C（後回し）：v1.0 の GAA（[9-38]）と一緒に設計する
  - 本物：1999 カリフォルニア大学バークレー校のチームが発表（米国の研究資金）、2011 インテルが 22nm で量産（トライゲート）、2015〜 TSMC・サムスンも。REFERENCES F37
- S-003 開始（ユーザーの決定「おすすめで」：タンタル＝案A、FinFET＝案A）。A に知らせ（worldgen 幹1 に tantalite_ore）と銅配線・FinFET の分担を相談（SendMessage）
- ✅ [1-66] タンタル：tantalite_ore（ModWorldGen.RULES に1行）、カストナーの電解槽（金属ナトリウム、前提として）、タンタルの精製槽（マリニャック法・ナトリウム還元）。Metallurgy.java に追記。GameTest ore_tantalite・tantalum、CHEMISTRY F62〜F64、REFERENCES F38
- A の答え（S-003）：銅＝デュアルダマシン（層間膜 → etchOxide 1回目＝ビアの穴、2回目＝溝 → depositBarrier（Ta）→ plateCopper（バリアなしは汚れ +30）→ planarize で溝の中だけ残す＝METAL2＋VIA）。FinFET＝平らな MOS は方眼が ARF_IMMERSION の CD より細いと漏れ、チャネルが FIN の上なら漏れない。A が litho に入れたら E の機械をつなぐ
- **ユーザーに質問（Discord）**：FinFET の high-k。本物の FinFET（2011）は high-k（ハフニウムの酸化物）＋金属ゲートと組。[1-70] ハフニウムは ❓。案①[1-70] を先に作る／案②ゲートは SiO2 のまま「仮」（漏れの判定は SiO2 でも成り立つ。high-k はゲートの漏れの話で短チャネルの漏れとは別）。答えまで FinFET の high-k の部分は作らない（フィンの工程は進めてよいかも合わせて聞いた）
- ✅ [9-36] 銅配線：スパッタに Ta の的（depositBarrier）、ウェハーの銅めっき装置（block/CopperPlaterBlockEntity、plateCopper）、CMP は既存。A の 1e6f067d。GameTest copper_wiring、CHEMISTRY F65、REFERENCES F39
- ✅ [9-37] FinFET の E の分：ドライエッチング装置に Cl2 → ChipOps.formFins（A の 76d68bcd）。GameTest fin_etch、CHEMISTRY F66、REFERENCES F37 更新。STI は省略、high-k はユーザーの決定待ち
- O-015 開始。[9-49] の MC 欄は「未定（実装の前に相談）」だったが、INBOX の指示どおり作る。いちばん軽い形＝インバータの部品にした（判断の記録）。拡散炉への分岐は A の了承
- ✅ [9-49] サイリスタ（拡散炉の分岐：n の p-n 接合＋リン → thyristor_wafer、作業台で thyristor ×4）・[9-41] インバータ（energy/InverterBlockEntity、DC→AC 95%、網の決まりは A に確認）。GameTest thyristor_inverter、CHEMISTRY F67、REFERENCES F40。[4-26] は前提がそろってから
- S-004 の1：FinFET のゲートは SiO2 の「仮」と明記（TECH_TREE・CHEMISTRY F66・REFERENCES F37・dry_etcher.placeholder・DECISIONS）。[1-70]（D）ができたら HfO2 に入れ替え
- **S-004 の2 は質問（Discord）**：「v0.6 に進んでよい（ROADMAP の順）」とあるが、ROADMAP に v0.6 の章がない（v0.5 の次は v1.0 EUV）。v1.0 のことか、ほかの意味かを確かめるまで、新しい章には入らない
- O-016（訂正：v0.6＝v1.0 EUV）を読んだ。分担：C が [7-17] 多層膜ミラー → [8-13] 液滴発生器 → [7-18] EUV 光源（sputter に Mo/Si の的の分岐を C が足す）、E が [2-33] 化学増幅レジスト（A に litho の形を相談中）。[9-38] GAA は [7-18] 待ち
- [2-33] の材料の道（案、出典で確かめながら）：ポリヒドロキシスチレン＝フェノール＋無水酢酸 → 酢酸フェニル →（AlCl3、フリース転位）→ 4-ヒドロキシアセトフェノン → 水素化 → 脱水 → 4-ヒドロキシスチレン → 重合。保護基（t-BOC）は ⚠️ 近似を検討。光酸発生剤＝トリフェニルスルホニウム塩（1982 の IBM は Ph3S⁺ SbF6⁻、クリベロのオニウム塩）：アンチモン（IMP3）＋フッ酸で SbF6⁻
- ✅ high-k（S-004 の「あとで①」）：D の HfO2 → ハフニウムの塩素化炉（HfCl4）→ ALD 装置（ChipOps.highK、A）。GameTest high_k_gate、CHEMISTRY F68〜F69、REFERENCES F41
- ✅ [2-33] 化学増幅レジスト：合成釜（block/ResistReactorBlockEntity、5つの反応の表）・ホットプレート（block/BakePlateBlockEntity、ChipOps.bake）・塗布機がレジストの種類を記録（DNQ/KPR/CAR、A の 55cfe13b）。GameTest chemically_amplified_chain、CHEMISTRY F70〜F72、REFERENCES F42
- C から（O-025 仮決定）：CleanRoom.dustAt を E の工程の機械で ChipOps.addDust に使う入口ができた（576eca44）。E の INBOX かユーザーの決定が来るまで入れない（候補：塗布機・CVD 炉・スパッタ・CMP・めっき・ALD）
- S-005（ユーザーの決定：v0.5 を全部終えてから v1.0）：v1.0 はユーザーの決定で後回し。E は v1.0 のノードを始めていない（[2-33] 化学増幅レジストは TECH_TREE で「v0.4〜、KrF/ArF 用」、[9-38] GAA は未着手。C の [7-17] 以降は C の判断）。**待機中（v0.4・v0.5 の残り待ち）**。E の候補：v0.5 #7 FinFET の残り（STI のうめ・CMP・頭出し）、#4 歩留まり（A と）
- S-006：[2-33] は E の版が正。D がホスゲンを使わない部分（PHS 樹脂の本物の道・光酸発生剤の材料）を ⚠️ の所に入れに来る → D と分担を相談（SendMessage）。t-BOC の試薬は省略のまま
- **D へ（S-006 の分担の案。D のセッションが今つながらないので LOG に）**：合成釜 block/ResistReactorBlockEntity の RECIPES 表（Recipe 1行＝材料4つまで・触媒・出る物2つ）の ⚠️ の行を、D が本物の材料（塩化チオニル・KSbF6・4-アセトキシスチレン・PGMEA など、D の幹のファイルでアイテムを作る）に書きかえてよい。触るのは表の行と Chemistry 側のアイテムの1行だけ。行を変えたら SemiconductorTests.chemicallyAmplifiedChain の材料も合わせて直し、test.ps1 全 PASS。本物にした所は lang の .placeholder（protected_phs・diphenyl_sulfoxide・photoacid_generator・chemically_amplified_resist・resist_reactor）と CHEMISTRY F70〜F72 の ⚠️ も外す。t-BOC の試薬は省略のまま。違う形がよければ D の LOG か INBOX で
- ✅ O-017：1＝E の工程の機械6台（塗布機・CVD 炉・スパッタ・CMP・めっき・ALD）の出口でほこり（ChipOps.addDust(CleanRoom.dustAt)）、GameTest process_dust_outside。2＝FinFET の high-k は S-004 で済（DECISIONS に入れ替え済みの行）
- O-018 開始。[9-42]・[9-43] とも TECH_TREE の MC 欄が「未定（実装の前に相談）」なので、遊び方の案を書いて仮決定を待つ（作らない）。[B-19] は D の [B-18] 液晶が ❓ のまま＝待つ
- **[9-42] 画像センサーの案**：
  - 案A（おすすめ、C の指摘で直した：DESIGN「レッドストーンは使わない」なのでレッドストーンの出力はやめる）：画像センサーの素子 image_sensor（アイテム）。作り方＝p-n 接合の板（フォトダイオード）を切った素子＋MOS のチップ（読み出しの増幅、A の litho で合格した NMOS／CMOS のチップ）＋アルミ配線。手に持つと、いる場所の光（空の光・ブロックの光）を「1画素にたまった電子の数」として画面の下に出す（暗いと少なく、ノイズで数がばらつく）。C の [9-45] ベイヤーのカラーフィルターを「素子＋フィルター → 色の分かる素子」で載せ、[P-17] スマホのカメラ（光学は C）の部品になる。litho は今のチップを使うだけ＝A の変更なし
  - 案B：アイテムのカメラ：右クリックで、見ている方向の 8×8 の明るさを「写真」（地図のような絵）にする。体験は大きいが、描画（client）の手間が大きい
  - 案C：部品（アイテム）だけ作り、[P-17] で使う
- **[9-43] MEMS（加速度センサー）の案**：
  - 案A（おすすめ）：加速度センサー mems_accelerometer（アイテム）。作り方＝シリコンの板 → ドライエッチング装置の深堀りのモード（Cl2、本物は 1994 ボッシュ法の SF6／C4F8 ⚠️）で「ばねとおもり」を削り出した板 → 切って素子 → 読み出しのチップと封止。手に持つと、画面の下に今の加速度を g で出す（立ち止まり 1.00g、落ちている間 0g、着地の瞬間に大きく、走る・乗り物で変わる）。傾け（向き）も出す。[P-18] の部品
  - 案B：ブロック：振動（近くの足音・ピストン）で信号を出す地震計
  - 案C：部品だけ
- C の指摘（DESIGN 404行「レッドストーンは使わない」）で [9-42] 案A のレッドストーン出力をやめ、手に持って数を見るアイテムに直した。C の [9-45] フィルターは「素子＋フィルター」のレシピで載せる形で合意
- ✅ A の焦点深度（O-030）に合わせ GameTest cmp_rescues_depth_of_focus：CVD → 塗布機（CAR）→ KrF は defocus、CMP 装置＋洗浄 → 焼ける
- ✅ O-019（仮決定の案A）：[9-42] 画像センサーの素子（item/ImageSensorItem）・[9-43] MEMS 加速度センサー（item/MemsAccelerometerItem、ドライエッチング装置に素の板＋Cl2 → MEMS の板）。どちらも組立台（Semiconductor.assemblies、Content に1行）。GameTest sensors、CHEMISTRY F73〜F74、REFERENCES F43、DECISIONS 2行
- ✅ [1-44] ガリウム：バイヤー法の溶解釜に使い終わった液のスロット（SLOT_SPENT）、ガリウムの電解槽（block/GalliumCellBlockEntity）。GameTest gallium、CHEMISTRY F75、REFERENCES F44
- ✅ v0.5 #4 歩留まり（A の 7c135282）：E の工程の機械6台の出口のほこりに、ChipOps.inRoom(CleanRoom.defectDensity) も足した（部屋の欠陥の密度を残す。失敗にはしない）
- **O-020 の案（遊び方・前提の選択。仮決定を待つ）**：
  - [B-07] LED：前提 [9-11] のヒ素が未実装（B・P だけ）。案A（おすすめ）：GaAsP の赤（1962）を本物どおり。ヒ素は金属の鉱石を焼いた煙道のちり（白ヒ素 As2O3）を集めて、炭素で還元（昔の方法）。反射炉（A）の煙道の副産物に1行が要る。毒（GHS：どくろ・健康有害性・環境）、工業の材料としてだけ。LED の素子（作業台かエピタキシャル炉の分岐で GaAsP の層 → 素子）は置くと光るブロック（電気 0.1W ほど、明るさ 7⚠️）。案B：ヒ素を使わない GaP の赤・緑の LED（1960年代後半）から始める（年はやや後、INBOX の「GaAsP の赤から」とはちがう）。案C：ヒ素は今は作らず LED は後
  - [B-20] 白色 LED バックライト：MC 未定。案A（おすすめ）：青色 LED（GaN、窒化ガリウム：ガリウム＋アンモニアのエピ ⚠️）＋黄色の蛍光体（YAG:Ce＝イットリウム・アルミ・セリウム ❓ 前提が要る）→ 白色 LED のブロック（明るさ 15）と、スマホの部品のバックライト。蛍光体の前提（Y・Ce）が無いので、先に前提を相談。案B：部品だけ
  - [15-18] 静電容量式タッチパネル：MC 未定。案A（おすすめ）：部品 touch_panel（ITO ガラス＋銅の電極＋NOT に合格したチップ）。手に持って右クリックすると「指の静電容量の変化」を読み、近くのプレイヤー・動物（体に電気をためられる物）に向けると反応、棒・ブロックでは反応しない（静電容量式の特徴）。スマホの部品 [P-?]。案B：部品だけ
- ✅ ドライエッチング装置にもほこり＋欠陥の密度（A の頼み、O-017 の「など」の範囲）
- ✅ [15-18] タッチパネル（item/TouchPanelItem、Communication.java に1行と組立台のレシピ）。GameTest touch_panel、REFERENCES F45、DECISIONS。[B-07]・[B-20] はユーザーの答え待ち
- D と [2-33] の分担を決めた（S-008）：D が (a) 樹脂を 4-アセトキシスチレン経由の本物の道、(b) SbF6⁻ を KSbF6（Sb2O3＋KF＋HF＋H2O2）に。D が合成釜の RECIPES の行・Chemistry のアイテム・chemicallyAmplifiedChain のテスト・placeholder／F70〜F71 の ⚠️ を直す。E は今そのファイルを触らない
- A のスマホ [B-10] の組立台（Branches.TFT_ID）：E の [B-19] は ID tft_lcd、[B-20] は led_backlight で作る（作ったら A に知らせる）
- 存命の人の名前（O-022 のきまり、D の指摘）：[2-33] の F72・REFERENCES F42・TECH_TREE・lang note4 から伊藤・ウィルソン・フレシェを消し「IBM の研究者たち」に
- ✅ [B-07] 赤い LED（S-007 案A）：白ヒ素（A の反射炉、黄鉄鉱4個ごと）→ 化合物半導体の炉（block/CompoundFurnaceBlockEntity）でヒ素・GaAsP → 作業台で red_led → LED の明かり（block/LedLampBlockEntity、2W・明るさ7）。Branches.java に追記。GameTest red_led、CHEMISTRY B-L1〜L2、REFERENCES F46
- O-022：D の [B-18] 液晶ができたので [B-19] TFT 液晶へ。TECH_TREE の MC 欄が「未定」なので案（仮決定を待つ）：
  - 案A（おすすめ）：スマホの部品 tft_lcd（A の組立台の ID どおり）。作り方＝①TFT のガラス tft_array_glass：CVD 炉の新しいモード「ITO ガラス＋シラン（＋水素）→ 水素を含むアモルファスシリコンの膜」（1979 ダンディー大学の a-Si:H の TFT、本物はプラズマ CVD ⚠️）→ 露光・エッチングは省略（⚠️ 画素の並びは作らない）②組立台：TFT のガラス＋こすった ITO ガラス（D）＋MBBA（D）＋偏光フィルム2（D）＋NOT に合格したチップ（駆動）→ tft_lcd。手に持つと「画素ごとのトランジスタが電気をためて、次に書きかえるまで明るさを保つ」を一言で出す（説明だけ、表示の絵はなし）
  - 案B：D の液晶セル（ブロック）の上位のブロック（4×4 の画素を順に書きかえて模様を出す）。体験は大きいが描画の手間が大きい
  - 案C：部品 tft_lcd を作るだけ（手に持っても何もしない）
- ✅ [B-20] 白色 LED（S-007 案A、D の [1-19] Y2O3・CeO2）：化合物半導体の炉に GaN・蛍光体のモード、作業台で blue_led → white_led → white_led_lamp（明るさ15）・led_backlight（A の ID）。GameTest white_led、CHEMISTRY B-L3〜L4、REFERENCES F47
- ✅ [B-19] TFT 液晶（O-024 仮決定の案A）：CVD 炉に TFT モード（ITO＋シラン＋H2 → tft_array_glass）、組立台で tft_lcd（item/TftLcdItem）。GameTest tft_lcd、CHEMISTRY B-L5、REFERENCES F48、DECISIONS
- S-008 開始（ユーザーの決定「v1.0 を始める」）：[9-38] GAA と EUV のマスク・レジストの塗布現像。前提 [9-37]・[9-30]・[7-18] は ✅。分担の相談を出した：A（GAA の ChipOps＝超格子の記録・releaseSheets・Circuit の漏れ、EUV の MaskData に反射型の印・[9-40] の担当）、C（多層膜ミラーをマスクブランクに、[9-40]）、D（EUV のレジストの材料）。E の分：エピタキシャル炉の Si/SiGe 交互のモード、SiGe を選んで溶かす工程、スパッタの Ta＋N2（TaN の吸収体）、マスクブランク、塗布機・現像槽の EUV レジスト
- A の答え（S-008）：SUPERLATTICE（int、A が作る）・releaseSheets・方眼 gridFor(16nm)≈136 以上は FIN でも漏れ GAA なら漏れない、MaskData に reflective（E のマスクの板の工程で印）・EUV は reflective でないと transmissive_mask、RESIST_EUV（CAR と同じ扱い）。A は S-019 のあとに入れる → E はそれ待ち（データ部品を重ねて作らない）
- ✅ EUV のマスクの板（S-008、C と分担）：スパッタで 多層膜ミラー（40対以上）＋Ta → euv_mask_blank、euv_mask の ID を登録（模様付けは C の縮小カメラ、吸収体を削るのは塩素のプラズマ）。GameTest euv_mask_blank
- D の euv_resist（e2c4ad86、ポジ型・アルカリ現像、クエンチャー入り）ができた。塗布機へのつなぎは A の RESIST_EUV 待ち。C は [9-40] EUV 露光装置と、縮小カメラに euv_mask_blank＋ポジ型レジスト＋塩素 → euv_mask の分岐を作る
- ✅ [9-38] GAA（A の受け口）：エピタキシャル炉に GeCl4 のモード（superlattice 3）、酸化炉で受け継ぐ、ドライエッチング装置に HCl（releaseSheets）。塗布機に EUV レジスト（RESIST_EUV）。GameTest gaa_chain、CHEMISTRY F76、REFERENCES F49。マスクの反射型の印は C の縮小カメラで reflectiveCopy()（C に伝えた）
- ✅ v1.0 の通しの GameTest gaa_with_euv：Si/SiGe → EUV レジスト（塗布機）→ C の縮小カメラの反射型マスク → EUV 露光 → ホットプレート → 現像 → フィン → HCl → GAA（方眼 148）
- **O-025 v1.0 #4 自動搬送の案**（作らない。司令塔 §4b か、ユーザーに）。今の様子：工程の機械（ConsumerBlockEntity）の中身は SimpleContainer で、ホッパー・Fabric の ItemStorage には出していない＝ウェハーは全部手で運ぶ。DESIGN 337行のマルチブロックの「物の口（ホッパーとつながる）」は A の v1.0 #1
  - 案A（おすすめ）：**FOUP と装置の前のロボット**（本物：1990年代末〜 300mm の FOUP ⚠️、装置ごとのロードポートとウェハー搬送ロボット＝EFEM）。①FOUP（アイテム）：同じ種類のウェハーを25枚まで入れて持ち運べる箱（中身はデータ部品、開けずに運ぶ＝ほこりが付かない：クリーンルームの外を運んでも汚れが増えない、の効き目）②ロードポート（ブロック、電気 50W 程度）：工程の機械の隣に置き、FOUP を入れると、その機械のウェハーの入口に1回分ずつ入れ、出口のウェハーを FOUP に戻す（同じ種類だけ）。機械どうしの「次へ運ぶ」は人が FOUP を持ち替える。電気で動き、レッドストーンは使わない。E の工程の機械（共通の ConsumerBlockEntity の中身）に触るだけで、A のマルチブロックとは別にできる
  - 案B：**天井を走る搬送（AMHS）**：レールのブロックの上を台車（エンティティ）が FOUP を運び、ロードポートどうしを結ぶ。見た目は大きいが、エンティティの動き・経路・描画の手間が大きい。A のマルチブロックの「物の口」とつなぐ所は A にお願い
  - 案C：**機械の物の口だけ**：工程の機械に Fabric の ItemStorage（ホッパーで出し入れ）を足す。いちばん軽いが、バニラのホッパーに頼る（本物の搬送の学びは少ない）
  - A にお願いする所：案A・B とも、マルチブロック（v1.0 #1）の「物の口」と FOUP／ロードポートの受け渡しの形をそろえる
- ✅ v1.0 #4 自動搬送（O-026 仮決定の案A）：FOUP（item/FoupItem、データ部品 foup_contents）・ロードポート（block/LoadPortBlockEntity、Machine.of(type).layout の入口・出口を読む、A の了承）。GameTest load_port、ROADMAP v1.0 #4、DECISIONS
- ✅ S-009 [1-36] 単体ホウ素（混ぜ物 → 溶鉱炉 → 塩酸）。GameTest elemental_boron、REFERENCES F50
- ✅ S-009 [9-05] 点接触ダイオード（作業台）。GameTest point_contact_diode、REFERENCES F51
- ✅ S-009 [4-16] アーク灯（block/ArcLampBlockEntity、450W・明るさ15・黒鉛が減る）。GameTest arc_lamp、REFERENCES F52
- ✅ S-009 [5-03] 水銀柱の気圧計（block/MercuryBarometerBlock、右クリックで高さの気圧）。GameTest mercury_barometer、REFERENCES F53
- ✅ S-009 [1-47] 天然磁石：v0.2a の磁石アイテム（磁鉄鉱4）がそのまま当てはまるので ✅ に（新しいアイテムなし）。GameTest natural_magnet、REFERENCES F54
- ✅ S-009 [1-02] 自然銅（銅の鉱石から 5%、かまどで銅）。GameTest native_copper、REFERENCES F55
- ✅ S-009 [3-20] 耐火粘土・耐火粘土のるつぼ（作業台）。GameTest fire_clay、REFERENCES F56
- ✅ ロードポート × マルチブロックの物の口（A の O-034）：隣が Multiblock.ItemPort なら組み上がった本体を機械として見る。GameTest load_port_on_item_port
- ✅ S-009 [3-01] たき火：バニラのたき火がそのまま当てはまるので ✅ に。GameTest campfire、REFERENCES F57
- ✅ S-009 [3-13] ロックウール（溶鉱炉で玄武岩、石綿は作らない＝危ない物は歴史と警告だけ）。GameTest rock_wool、REFERENCES F58
- ✅ S-009 [3-09] 熱電対（item/ThermocoupleItem、右クリックで温度と電圧、銅＋鉄は700℃ まで）。GameTest thermocouple、REFERENCES F59
- ✅ O-027：E の分 0（入手の道 12＝機械のコードが作る物を machine_recipes に書き出し〔A の反射炉の白ヒ素・C の縮小カメラの EUV マスクも E の行で〕、「?」の howto を E の機械42台に）。check_howto: no way 71 / no help 71（E は 0）
- **O-028 の案（量産の道具、作る前に。司令塔 §4b か、ユーザーに）**：A の Transfer API（machine/MachineItems：画面のある機械は入口・出口が ItemStorage）を使い、レッドストーンは使わない
  - 1 ベルトコンベア（1913 フォード、ハイランドパーク ⚠️）：
    - 案A（おすすめ）：ベルト（向きのあるブロック、1マスに物1スタック、上に物が乗って見える）＋ベルトの駆動（モーター [4-08] の付いた頭のブロック、電気＝ベルト1マスあたり約 5W ⚠️）。駆動の後ろの機械の出口から取り（ItemStorage）、ベルトを1秒に1マスずつ運んで、終わりの前の機械の入口に入れる。駆動が電気をもらっている間だけ動く。ホッパーより遠く（ベルトの長さぶん）運べる、が体験
    - 案B：ベルト1マスずつが電気をもらう（ケーブルを全部のベルトにつなぐ手間）
    - 案C：物をエンティティとしてベルトの上を流す（見た目はよいが、エンティティの動き・拾い方の手間とラグ）
  - 2 天井の搬送 OHT（1990年代の 300mm 工場 ⚠️）：
    - 案A（おすすめ）：天井のレール（ブロック）でロードポートどうしを結び、出す FOUP がいっぱい（か機械が止まった）とき、レールの先の別のロードポートの「入れる」口へ FOUP を送る（レールの長さに比例した時間、台車は描かず、レールに沿って光が動く程度 ⚠️）。電気はレールの頭の駆動で
    - 案B：台車（エンティティ）がレールの上を走って見える（見た目は大きいが、描画と経路の手間）
    - 案C：OHT は作らず、ベルトコンベアで FOUP も運べるようにする
- ✅ S-009 [3-11] 光高温計（item/OpticalPyrometerItem、光っている物の温度）。GameTest optical_pyrometer、REFERENCES F60
- ✅ O-029-1 [14-22] ベルトコンベア＋駆動（block/ConveyorBeltBlock・ConveyorDriveBlockEntity、client/ConveyorBeltRenderer、Transfer API で機械・チェストどうしを遠くまで）。GameTest conveyor_belt、REFERENCES F61
- ✅ O-029-2 [14-23] 天井の搬送 OHT（block/OhtRailBlock・OhtDriveBlockEntity：いっぱいの FOUP をレールの先のロードポートへ、光が動く）。GameTest oht_rail、REFERENCES F62。O-029 完了
- ✅ S-009 [3-02] かまど（バニラ furnace の解説・≈1000℃ に統一：熱電対・光高温計の値 600→1000℃）。GameTest furnace_temperature、REFERENCES F63
- ✅ S-009 [3-05] 高炉の扱い（バニラ溶鉱炉＝高温の窯 ≈1450℃ の解説、熱電対・光高温計の値 1200→1450℃）。GameTest blast_furnace_temperature、REFERENCES F64
- **S-009 の案（遊び方が決まっていない／今ある物を変えるので飛ばした。司令塔 §4b か、ユーザーに）**
  - [5-16] ゲッター（Mg）：案A（おすすめ）封止台にゲッターのスロットを足し、入れると封止の圧が 1/10（0.1→0.01Pa）、真空管が長もち ⚠️（A の封止台を変えるので A の同意が要る）／案B 真空管のレシピの材料にする（Mg が真空管より先に要る＝進み方が変わる）／案C 解説だけ
  - [1-13] 亜鉛の蒸留：案A（おすすめ）酸化亜鉛＋木炭＋土器の「蒸留のかめ」→かまど→亜鉛2（今の ZnO→亜鉛1 より多い）。閃亜鉛鉱を直接焼く近道（#15 仮、HeatTests が確かめている）は残す／案B 近道を消して焙焼→蒸留を必須に（亜鉛を使う乾電池などが後ろにずれる）
  - [1-04] 銅の製錬（硫化鉱）：案A 反射炉で原石の銅→マット＋SO2、転炉で空気を吹いて粗銅＋スラグ（A の反射炉・転炉を変える）／案B 解説だけ（バニラかまど＝るつぼ炉の近似の決定のまま）。おすすめ B（今の決定を変えない）
  - [3-07] るつぼ炉：案A（おすすめ）黒鉛るつぼを入れる燃料式の小さな炉（2スロット、少量を汚さず溶かす：るつぼ鋼・光学ガラス）、るつぼ鋼の今の作り方は残す／案B るつぼ鋼をこの炉だけにする
  - [3-24] ニクロム線ヒーター：案A（おすすめ）ニッケル4＋クロム → ニクロム線、電気こんろ（ブロック、500W、上に置いた物を焼く＝電気のかまど 約800℃）／案B 機械の部品だけ（電気炉の材料に）
  - [4-18] 扇風機：案A（おすすめ）モーター＋羽根のブロック、正面の数マスの物・エンティティを押す＋機械の冷却（隣の機械の速さ +10%）⚠️／案B 部品（冷却ファン）だけ
  - [9-46] 人を感じるセンサー：案A（おすすめ）近くで動く人がいると隣の明かり（LED の明かりなど）を点ける（レッドストーンは使わない：明かりに直接）／案B 部品だけ（スマホ・自動ドアの材料）
- ✅ O-030 [1-04] 銅の製錬＝案B（バニラの銅の原石に解説だけ、かまどの道は近似のまま）。GameTest copper_smelting、REFERENCES F65
- ✅ O-030 [1-13] 亜鉛の蒸留＝案A（蒸留のかめ zinc_retort_charge → かまど → 亜鉛2、近道は残す）。GameTest zinc_retort、REFERENCES F66
- ✅ O-030 [5-16] ゲッター＝案A（magnesium_getter、封止台の下のスロットで封止の圧 1Pa まで ⚠️、A の同意あり。封止台の howto も）。GameTest getter_seal、REFERENCES F67
- ✅ O-030 [3-24] ニクロム線ヒーター＝案A（nichrome_wire、電気こんろ electric_stove 500W・約800℃・燻製器のレシピ）。GameTest electric_stove、REFERENCES F68
- ✅ O-030 [9-46] 人を感じるセンサー＝案A（pir_sensor、5マスで動く生き物 → 10秒光る、隣の LED の明かりはその間だけ）。GameTest pir_sensor、REFERENCES F69
- ✅ O-030 [3-07] るつぼ炉＝案A（crucible_furnace、コークス＋ふいごで 1600℃、るつぼ鋼2＋るつぼが戻る・3回目で割れる、フリントガラスも）。GameTest crucible_furnace、REFERENCES F70
- ✅ O-030 [4-18] 扇風機＝案A（electric_fan、50W、正面5マスを押す）。**「隣の機械の冷却 +10%」は入れず説明だけ**：A の判断（冷やすと遅くなる機械がある・電気の勘定が合わない・出口満杯で物が消えるおそれ）。効果が要るなら、温度を持つ機械を作るときに「過熱で止まりにくくする」形で司令塔に相談。GameTest electric_fan、REFERENCES F71。O-030 完了
- 待機終了（2026-10-22 02:14）：INBOX が6時間変わらなかった。O-028〜O-030・S-009（軽いノード13個）まで完了。続きは INBOX の新しい項目から
- **S-009 の案・その2（2026-10-26、どれも「MC 未定」なので作る前に。おすすめが1つなら司令塔が §4b で仮決定）**
  - [9-44] サーミスタ：案A（おすすめ）手に持つ温度計の部品（酸化物の焼き物＋銅線）。右クリックで温度と抵抗値（温度が上がると抵抗が下がる、−50〜300℃）。熱電対より低い温度用／案B 部品だけ（スマホ・電気こんろの温度調節の材料）
  - [1-54] インバー：案A（おすすめ）鉄2＋ニッケル → インバー。持つと「1m の棒が 100℃ で何µm 伸びるか」を鋼と比べる（鋼 1200µm・インバー 120µm）。精密の部品（ゲージブロック台・時計の振り子）の材料にも／案B 材料だけ
  - [1-60] 銅の電解精錬：案A（おすすめ）電気めっき槽に「精錬」モード：粗銅（今の銅インゴット）＋硫酸銅 → 電気銅（99.99%、純度のデータ）、陽極の泥から少し銀・金 ⚠️。銅線・銅ケーブルに電気銅を使うと損失が少ない、は説明だけ／案B 新しい槽を作る
  - [1-53] フェライト：案A（おすすめ）酸化鉄＋酸化亜鉛＋酸化マンガン → 焼く → フェライトの芯。変圧器・高周波の部品の材料（今ある物は変えない）／案B 説明だけ
  - [1-65] 高速度鋼：案A（おすすめ）鋼＋タングステン＋クロム → るつぼ炉 → ハイスの刃。旋盤に付けると速く削れる（旋盤の時間 −30% ⚠️、C の旋盤なので C の同意が要る）／案B 材料だけ
  - [1-71] 熱処理：案A（おすすめ）焼入れ（かまど→水）・焼戻し（低温）・焼なまし（ゆっくり冷ます）を鋼のデータ（硬さ HV）で：持ち物で時間がたつジュラルミン（C の [14-13]）と同じ形／案B 説明だけ
  - [1-55] 合成ダイヤモンド：案A（おすすめ）高圧の反応器（D の [2-xx] 高圧反応器があれば）で黒鉛＋ニッケル → 工業用ダイヤの粉 → ダイヤの刃（けがき針・ダイシングソーの材料に）。⚠️ 5万気圧はゲームの数で／案B 説明だけ
  - [1-68] 永久磁石鋼（KS鋼）：案A（おすすめ）鋼＋コバルト（無ければニッケル）＋タングステン → 焼入れ → 磁石（今の磁石 [4-04] より強い：発電機の出力 +20% ⚠️ は A の同意が要る）／案B 材料と説明だけ。おすすめは B（速さ・出力を変えるのは A が慎重）
  - [1-64] 金の製錬：⚠️ 水銀のアマルガム法・青化法（シアン化物）は毒。DESIGN「危ない技術」に合わせて **解説だけ**（おすすめ）。金そのものは今ある分のまま
  - [1-73] めっき鋼板：案A（おすすめ）鉄板＋亜鉛（溶かした亜鉛にくぐらせる）→ トタン、鉄板＋錫 → ブリキ。雨に当てるとさびの進み方がちがう実験（鉄だけはさびる、トタンは亜鉛が先にさびる）／案B 材料だけ
- ✅ S-009 [1-54] インバー（invar、右クリックで 1m・100℃ の伸びを鋼とくらべる）。GameTest invar、REFERENCES F72
