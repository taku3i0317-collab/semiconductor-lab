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
