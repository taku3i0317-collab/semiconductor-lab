# IMPL3_LOG — 3人目の実装担当（IMP3-番号）の記録

担当：幹0（生存＝石器時代）と、その前提で未実装のノード。HANDOFF.md は触らず、ここに書く。
CHEMISTRY.md の番号は S1〜、REFERENCES.md は「# 生存」の節（S1〜）。幹1（[1-03] 銅・[1-30] 錫・[1-05] 青銅）は IMP2 が作業中なので触らない（できあがってから使う）。

## IMP3-01（2026-10-20）幹0 を背骨の先頭から
- ✅ [0-01] 打製石器（遊び方: 実験＋軽）：たたき石 hammerstone（砂利から 10%＝火打ち石が出なかったとき、Fabric の LootTableEvents.MODIFY_DROPS。作業台で戻る、item/HammerstoneItem）、フリントの剥片 flint_flake（剣扱いの小さな刃物、耐久 32）、黒曜石の剥片 obsidian_flake（鋭いがもろい、耐久 16）、ハンドアックス hand_axe（柄のない斧、耐久 80、速さ 3.0＝木の斧 2 と石の斧 4 の間）。修理タグ semiconductor-lab:knapping_stones（火打ち石・黒曜石）
  - 実験：たたき石で右クリック → もう片方の手の火打ち石は剥片2、黒曜石ブロックは剥片1（ブロックは残る、10tick の冷却）、丸石・花こう岩・砂岩などは「粒が粗くて刃にならない」と出るだけ（何も減らない）
  - 分け方（ユーザー承認）：MOD＝柄のない打製石器、バニラの石の道具（丸石＋棒）＝柄を付けた石器（着柄 約50万年前⚠️）
  - 年（ユーザー指示）：確実な最古＝オルドワン 約260万年前（ゴナ）、ロメクウィ 約330万年前は ⚠️、ハンドアックス（アシュール）は約170万年前から
  - GameTest knapping、CHEMISTRY S1〜S3、REFERENCES S1、TEST_QUEUE、tab 順（gen_tab_order.py）。テスト 95/95（IMP2 の変更を取り込んだ後）
  - 共通ファイルの変更：なし（Survival.java・SurvivalTests.java・item/HammerstoneItem.java・lang survival のみ）。tab の年はまだ tech_network.json の -3300000（ネットワーク担当が TECH_TREE から作り直すと -2600000 になる）
- ✅ [0-03] 土器（遊び方: 軽＋実験）：粘土玉×5 → 生の土器 raw_clay_pot →たき火30秒（野焼き）／かまど10秒 → 土器 earthenware_pot。生の土器＋水バケツ → 粘土玉5（戻る）、焼いた土器＋水 → 何もできない（不可逆な脱水）。塩田の煮つめ釜にも使える（salt_pan_earthenware、大釜版もそのまま）。GameTest pottery、CHEMISTRY S4〜S5、REFERENCES S2。テスト 97/97（IMP2 の青銅を取り込んだ後）
- ✅ [0-08] 木工（遊び方: 軽）：ハンドアックス＋砂＋水バケツ（戻る）＋棒 → 磨製石斧 polished_stone_axe（耐久 200、速さ 4.5）。砂なし・水なしは不可。ほぞ組みは へえ のみ。GameTest polished_stone_axe、CHEMISTRY S6、REFERENCES S3。テスト 99/99
- 取り込みで気づいたこと：REFERENCES の IMP2 の O10 が「生存」節の中に入ったので、消さずに光学の節の末尾へ移した。TECH_TREE の [0-02]・[0-03] はネットワーク担当の直しを残し、[0-03] は実装の記述＋直った元の記述にした
- ✅ [0-04] 農耕（遊び方: 実験、ユーザーが案A を選択）：野生のヒトツブコムギ wild_einkorn（草から 5%）→ 熟したまま放置で穂が落ちて芽に戻る、熟したうちに刈ると 10% で栽培種 einkorn の種。栽培種は落ちず、刈ると小麦（バニラ）＋種。block/EinkornCropBlock、作物のテクスチャは自作の仮。API_26X に作物・描画層・斧・MODIFY_DROPS を追記。GameTest einkorn_domestication、CHEMISTRY S7〜S8、REFERENCES S4。テスト 101/101
- REFERENCES の「生存」節を「# 第1章 ゲルマニウム」の直前へ移した（光学担当が「# 冶金と熱」の直前に足すたびに混ざるため。中身は同じ）
- ✅ [0-06] 織物（遊び方: 実験＋軽）：亜麻 flax（作物、草から 4%）→ 茎×2＋種。茎を水に投げこむと 1分で浸水した茎（mixin/ItemEntityRettingMixin → item/Retting。浮いて上下しても足元が水なら浸かっている扱い、水から出てもたまった時間は残る）。浸水した茎＋たたき石 → 繊維 → ×2 糸 → ×4 リネン。GameTest flax_to_linen（実際の tick、テスト中だけ浸水 20tick）、CHEMISTRY S9〜S11、REFERENCES S5。テスト 106/106
  - 共通ファイルの変更：src/main/resources/semiconductor-lab.mixins.json に "ItemEntityRettingMixin" を1行
- IMP-32（docs の部品化）に合わせ、幹0 の CHEMISTRY・TEST_QUEUE の節は docs/chemistry/0_survival.md・docs/test_queue/0_survival.md に書く（docs_build.py --adopt で移した。取り込みで幹8 の表に紛れた S7・S8 の重複は幹8 の部品から外した）
- ✅ [0-07] 羊毛と紡錘車（遊び方: 実験＋軽、ユーザーの案）：紡錘車＝人類最古のはずみ車。item/SpindleItem（右クリックを押している間まわる）：紡錘（棒だけ）は 1秒で糸1本、紡錘車（土 or 石）つきの紡錘は 4秒で4本、材料比は繊維2 → 糸1 で同じ（物の量は保存、はずみ車が変えるのは1回の量）。羊毛ブロック → ほぐした羊毛×4 → 紡錘で毛糸。GameTest spindle_whorl、CHEMISTRY S12〜S14、REFERENCES S6、API_26X（Items.WOOL は ColorCollection、押し続ける道具）。テスト 111/111
  - 年：紡錘車の最古はナハル・エイン・ゲヴⅡ 約1万2000年前（2024年の1研究、⚠️）。羊毛用の羊は紀元前4000年ごろ（AUDIT V-16、⚠️）
  - ネットワーク担当への提案：docs/network_proposals.md「つながりの種類の提案」に E-2（はずみ車：紡錘車 → ろくろ → 蒸気機関・エンジン）

## 進め方の変更（2026-10-20 ユーザーの指示）
- 指示は INBOX.local.md（設計担当が書く、git に入らない）から。区切りごとに読み、「新しい」を上から実行 → 「実行中」→「完了（commit）」。無ければ Monitor で待つ（2時間変化なしで「待機終了」）。自分用のルールは CLAUDE.local.md（git に入らない。共通の CLAUDE.md は他の担当も読むので書かない）
- O-001（[0-07]）＝完了（aad49df）。O-002（幹0 の続き）＝実行中

## IMP3-01 続き（O-002）
- ✅ [0-18] ロープ（遊び方: 軽、ノードに書いてあるとおり）：リネンの糸×3 → ロープ（毛糸は不可）。出典：アブリ・デュ・マラの3本撚りのひも 約4万1000〜5万2000年前（Hardy ほか 2020）⚠️。年 -7000 は前提 [0-06] より古くなるので変えず、ネットワーク担当の判断に。GameTest rope、CHEMISTRY S15、REFERENCES S7。テスト 115/115

### 質問（遊び方の決定待ち。INBOX かユーザーの決定が来るまで作らない）
残りの幹0（前提 ✅）は、ノードに遊び方が書いてないので案を出す。おすすめは①
- Q3-1 [0-05] 豆（窒素固定）：①エンドウ（新作物）を植えて刈った後の耕地に、次に植えた麦が速く育つ（その場所に「根粒の窒素」を数回分ためる、輪作の実験）②エンドウ＋根を見る（刈ると「根粒のついた根」が出る、へえ用の軽）
- Q3-2 [0-09] 車輪：①ろくろ（土器の回転台、はずみ車 [0-07] の続き）を先に作り、ろくろで生の土器が粘土玉3つで作れる（手より少ない粘土）→ 木の車輪（板材＋磨製石斧）②木の車輪のアイテムだけ（[0-13] 荷車の進捗用）
- Q3-3 [0-14] 水運：決定どおりバニラのボートを本物として扱う。①「重い物は水で運ぶ」実験：ボートに乗ると荷物（重い石材を持っていても）遅くならない…はバニラの仕様を変えるので②バニラのボートにへえ①〜④を付けるだけ（遊びはバニラのボート）
- Q3-4 [0-17] 家畜化：①バニラの牛・羊・山羊に へえ を付け、野生の動物（オーロックス）は作らない ②「囲って育てると増える」を進捗（アドバンスメント）にする
- ✅ [0-05] 豆（O-003-1、遊び方: 実験）：エンドウ pea（草から 3%）が熟すと下の耕地が窒素の多い耕地 nitrogen_farmland（3回分）。上の作物は耕地のランダムtick ごと 50% でもう1段育つ、熟すたびに1回分使い3回でふつうの耕地。GameTest pea_rotation（同じtick 数で豆の後の麦 3段 vs 豆なし 0段）、CHEMISTRY S16〜S17、REFERENCES S8。テスト 119/119
  - 見つけた不具合を直した：MOD の作物（エンドウ・ヒトツブコムギ2種・亜麻）が minecraft:maintains_farmland に入っておらず、水が遠いと下の耕地が土に戻っていた → タグに追加（minecraft:crops にも）
  - 共通ファイル：data/minecraft/tags/block/mineable/shovel.json に nitrogen_farmland を1つ追加（既存の iron_sand・salt_pan はそのまま）
- ✅ [0-19] ろくろ（新ノード、O-003-2）＋ [0-09] 車輪：ろくろ potters_wheel（石3＋紡錘の棒＋板材3、block/PottersWheelBlock）で粘土玉3つ → 生の土器（手は5つ）。木の車輪＝板材3＋磨製石斧（戻るが耐久 −10、item/PolishedStoneAxeItem）。年：ろくろ 紀元前4500年ごろ⚠️（ククテニ・トリピリア）、車輪 紀元前3500〜3350（ウルク）・リュブリャナ 紀元前3130ごろ⚠️。TECH_TREE に [0-19] を追加、[0-09] の前提に [0-19]。network_proposals E-2 に「[0-19] 追加済み」と書き足した。GameTest potters_wheel_and_wheel、CHEMISTRY S18〜S19、REFERENCES S9。テスト 125/125
  - タブの並び：[0-19] は docs/tech_network.json にまだ無いので幹0 の最後（ネットワーク担当が作り直すと年の順になる）
  - 共通ファイル：data/minecraft/tags/block/mineable/axe.json に potters_wheel を1つ追加
- ✅ [0-14] 水運（O-004-1、案③、遊び方: 実験＋作品）：丸木舟 dugout_canoe（原木5＋磨製石斧、entity/DugoutCanoe＝ChestBoat 継承、荷物入れ27、client/SurvivalClient で BoatRenderer＋自前テクスチャ。テクスチャはバニラの透明部分の形だけを使い色は自前）。石材の荷 stone_load（石4＋ロープ）を持つと鈍化1〜4段（歩く速さ 0.10 → 0.04）、乗り物に乗ると鈍化なし。バニラのボートは変えていない。GameTest dugout_canoe、CHEMISTRY S20〜S21、REFERENCES S10。テスト 128/128
  - 共通ファイル：SemiconductorLabClient の「幹0」の段に SurvivalClient.register() を1行
  - 失敗から：レシピの category "transportation" は 26.3 に無く、全レシピの読み込みが止まった → misc（API_26X に追記）
- ✅ [0-17] 家畜化（O-004-2、②＋①、遊び方: 実験）：新しい動物は作らない。繁殖させた牛・羊・山羊の子に世代タグ semilab_gen_N（親の大きい方＋1、mixin/AnimalBreedingMixin → entity/Domestication）、最初の繁殖で進捗「家畜化のはじまり」、3世代目で「牛／羊／山羊の家畜化」（新しい進捗タブ domestication、説明がへえ）。バニラの牛・羊・山羊のスポーンエッグに へえ①〜④（lang の vanilla 部品）。GameTest domestication（本当の繁殖で mixin が世代を付けるのも確認）、CHEMISTRY S22、REFERENCES S11。テスト 131/131
  - 共通ファイル：mixins.json に "AnimalBreedingMixin" を1行
- ✅ [0-13] 荷車（O-002 の続き、決定どおり進捗のみ）：進捗 domestication/cart（「牛の家畜化」の下、木の車輪を手に入れると達成、説明がへえ①）。テストは domestication に1つ足した
- 幹0 は [0-01]〜[0-19] の全ノードが ✅（INBOX O-001〜O-004 完了）。INBOX の新しい指示を待つ（2026-10-20 16:20〜）

## S-001 試験場（相談役から、ユーザーの提案）
- 途中でメモリ不足で止まった → 再開（git status で途中の変更が無事なのを確認、ロックファイルなし、pull --rebase）
- ✅ 関数 semiconductor-lab:testworld（tools/gen_testworld.py が作る）：幹ごとに1列、ノードごとに1区画（ブロック・機械＋チェスト＋看板）。電気を使う機械の真下に「試験用の無限電源」（content/Testing.java＋energy/TestPowerSourceBlockEntity、1 MW、クリエイティブ専用・レシピなし・壊れない、名前に「試験用」）。最後の列は本物の発電（蒸気機関 → 銅ケーブル → 電気炉）。98×101 ブロック、311 個
- 区画は タブの並び（幹・ノード）、機械と材料は machine_recipes.json、看板は TEST_QUEUE の部品の「人間：」の最初の項目（全文は docs/TESTWORLD.md の自動生成の一覧）。チェストの個数は Java の stacksTo・斧・剣から（重ねられない物は1個）
- GameTest testworld（SurvivalTests）：テストの場所から 2000 ブロック離れた所に建て（上に建てると塩田のテストの空をふさいで落ちた）、311 ブロックがすべて計画どおり。テスト 138/138
- 共通ファイルの変更（S-001 の指示の範囲＋登録に要る最小限）：content/Content.java に Testing.init() の1行、lang の common 部品に試験用電源の6キー、CLAUDE.md の手順に gen_testworld.py の1行、docs/test_queue/general.md に1節、API_26X に2行
- 決めたこと（小）：「章」は幹ごとの列にした（石器時代＝幹0、ゲルマニウム・シリコン＝幹9 など。v0.3 の本線の物は各幹の列に入る）。作物はブロックにせず種としてチェストへ

## O-005 担当の変更：幹2 の有機・高分子（2026-10-20 ユーザーの決定）
- ユーザー不在の間の決まり：INBOX の待ちは6時間、許可の確認が出る操作と判断が要る件は LOG と Discord に書いて飛ばす（CLAUDE.local.md に書いた）
- ✅ [2-31] アスファルト（ヘリオグラフ、遊び方: 実験）：天然アスファルト natural_asphalt（荒野・砂漠の地表近く、ModWorldGen に1行）→ 瀝青。剥片でトウヒ → 松やに → かまど → テレビン油（item/FlakeItem：[0-01] の剥片に使い道を足した）。瀝青＋テレビン油 → 感光液、スズ2＋鉛1 → ピューター板3、感光液＋板 → ヘリオグラフの板（block/HeliographPlateBlock）：昼に板の真上が空まで空気かガラスならランダムtick で露光（4段）、テレビン油で洗うと露光しきれば像・足りなければ全部溶けて板が戻る。GameTest heliograph・ore_natural_asphalt、CHEMISTRY H1〜H4（docs/chemistry/2_chemistry.md）、REFERENCES C1（新しい節「化学・有機と高分子（IMP3）」）。テスト 154/154
  - 決めたこと（小）：洗う溶剤はニエプスのラベンダー油の代わりにテレビン油（ラベンダーがバニラに無いため。INBOX の「ラベンダー油など」の範囲、placeholder に明記）。像の形は決まった絵
  - 失敗から：日なたの判定を空の光の値でしたら、テストでは光の計算がまだ追いつかず、石の下でも固まった → 板の真上を空まで調べる（propagatesSkylightDown）に変えた
  - 共通ファイル：ModWorldGen に1行、MetallurgyTests.ore_biome_rules に期待値1行、mineable/pickaxe タグに1つ
  - Gradle のデーモンが1回消えた（ほかの担当のビルドと重なってメモリ不足とみられる）→ 回し直して通った
- ✅ [2-29] ゴム（遊び方: 実験＋軽、決定どおりジャングルに新しい木）：ゴムの木（丸太・葉・苗木、木の生成 rubber_tree／rubber_trees）。剥片で幹 → ラテックス、＋木酢液 → 生ゴム、＋硫黄 → 焼く → 加硫ゴム。生ゴムは暑い所（気温 0.95 以上）で「べたついた生ゴム」、寒い所（0.15 以下）で「硬くなった生ゴム」に名前が変わる（item/RawRubberItem、ITEM_NAME の翻訳キー）。加硫ゴムは変わらない。GameTest rubber、CHEMISTRY R1〜R3、REFERENCES C2。テスト 160/160
  - 共通ファイル：ModWorldGen の OreRule に「生成の段階」を任意で足した（2引数の書き方はそのまま鉱石の段階）＋ゴムの木の1行、MetallurgyTests.ore_biome_rules に期待値1行、minecraft のタグ（logs・logs_that_burn・leaves・saplings・mineable/axe・mineable/hoe、item 側も）に足した
  - 分かったこと：GameTest の世界では木の成長（TreeGrower.growTree）がバニラのジャングルの木でも false になる → 木が育つかは人間の確認に回した（TEST_QUEUE）
- ✅ 環化ゴム（O-005-3、[2-29] の続き。TECH_TREE に専用ノードが無いので [2-29] の行に書いた）：生ゴム＋硫酸＋テレビン油 → 環化前の液 →かまど → 環化ゴム。加硫ゴムは不可（溶けない）。出典：Harries 1919 の硫酸法（スリランカ国立科学財団の資料）、KTFR は [2-30] の本物欄。感光剤を混ぜるのは担当 E。CHEMISTRY R4、REFERENCES C3。テスト 163/163

## O-006 フェノール樹脂までの鎖
- ✅ [2-26] メタノール（遊び方: 軽）：木酢液4＋消石灰 → 石灰を入れた木酢液4 →かまど → メタノール（石灰なしは不可）。GHS を note2 に。GameTest methanol、CHEMISTRY P1、REFERENCES C4。テスト 165/165
- ✅ [2-27] ホルムアルデヒド（遊び方: 実験）：銅の網（銅4、block/CopperGauzeBlock）を火のついたたき火の上に置き、メタノールで右クリック → ホルマリン。網は減らない（触媒）、冷たい・火の消えたたき火では反応しない。GameTest formaldehyde、CHEMISTRY P2、REFERENCES C5。テスト 166/166
- ✅ [2-25] フェノール（遊び方: 軽）：コールタール →かまど→ 中油、＋NaOH → フェノキシド液、＋硫酸 → フェノール（瓶は戻る）。中油にいきなり酸は不可。GameTest phenol、CHEMISTRY P3、REFERENCES C6。ツールチップ検査で「省略。」だけの行が Shift なしで出たので言い回しを変えた。テスト 167/167
- ✅ [2-28] フェノール樹脂（遊び方: 軽＋実験）：フェノール2＋ホルマリン＋塩酸 →かまど → ノボラック（[2-32] の主成分、熱で溶ける）。フェノール＋ホルマリン2＋NaOH →かまど → ベークライト（かまどで溶けない）。触媒なしは不可。GameTest phenolic_resin、CHEMISTRY P4〜P5、REFERENCES C7。テスト 170/170
- O-006 完了（[2-26] [2-27] [2-25] [2-28]）

### 判断待ち（ユーザー・設計担当の決定が来るまで、今の形のまま）
- Q3-5 [2-28] 加圧加熱釜：TECH_TREE の MC入手に「加圧釜ブロック」とある。今はかまどで作った（ノボラックは大気圧で作れる。ベークライトの加圧成形＝ベークライザー 1909 だけが加圧）。①今のまま（かまど、placeholder に明記）②ベークライザーを機械（電気か燃料で加熱＋圧力の表示）として作り、ベークライトだけを機械に移す。おすすめは①（ノボラックが本線で、加圧は本線に要らない）

## O-007 レジストの残りの材料（E がレジストにまとめる前の物）
- ✅ [2-24] ベンゼン・キシレン（遊び方: 実験＋軽）：コールタールをたき火 → 軽油（かまど → 中油）、軽油をたき火 → ベンゼン、かまど → キシレン（温度で分ける分留の実験）。GHS（ベンゼン発がん性1A）。GameTest benzene_xylene、CHEMISTRY P6、REFERENCES C8。テスト 172/172
  - **E へ**：キシレン semiconductor-lab:xylene ができた（ネガ型の溶剤・現像液 [9-21]）。ベンゼン semiconductor-lab:benzene も
- ✅ [2-57] DNQ 感光剤（新ノード、遊び方: 実験＋軽）：中油＋氷 → ナフタレン → ＋硫酸 → ナフタレンスルホン酸 → ＋NaOH×2 → 焼く → 2-ナフトール → ＋硫酸＋安水＋SO2 → トビアス酸。硝石＋鉛 → 焼く → 亜硝酸カリウム（昔の方法）。トビアス酸＋亜硝酸カリウム＋塩酸＋氷 → DNQ。DNQ は日なたで持つと光で分解（item/DnqItem、名前が変わり戻らない）。量・温度・条件は書いていない（DESIGN）。GameTest dnq、CHEMISTRY P7〜P9、REFERENCES C9。テスト 175/175
  - **E へ**：DNQ semiconductor-lab:dnq ができた（[2-32] でノボラック semiconductor-lab:novolac と混ぜる）。日なたで持ち歩くと名前が「光で分解した DNQ」に変わる（ITEM_NAME の翻訳キー item.semiconductor-lab.dnq.decomposed、判定は DnqItem.decomposed(stack)）。レシピで分解した物をはじくなら、この判定を使える
  - **C へ**：亜硝酸カリウム semiconductor-lab:potassium_nitrite を作った（硝石＋鉛を焼く）。硝酸・亜硝酸の系で使えるならどうぞ
  - タブの並び：[2-57] は tech_network.json にまだ無いので幹2 の最後（ネットワーク担当が作り直すと年の順）

### 判断待ち（ビスアジド）
- Q3-6 ネガ型の感光剤（ビスアジド、KTFR 用）：本物は BAC-M＝2,6-ビス(4-アジドベンジリデン)-4-メチルシクロヘキサノン（米国特許 2,940,853 の系統、環化ゴム＋ビスアジドの特許 4268603 など）。作り方は 4-アミノベンズアルデヒド＋4-メチルシクロヘキサノン（アルカリでアルドール縮合）→ ジアゾ化 → アジ化ナトリウム。前提がまだ無い：金属ナトリウム（溶けた NaCl の電気分解、1924 ダウンズ）→ アジ化ナトリウム（1892 ヴィスリツェヌス：ナトリウムアミド＋N2O）、トルエンのニトロ化（4-アミノベンズアルデヒド）、p-クレゾールの水素化（4-メチルシクロヘキサノン）。**作らずに止めた**。案：①前提（金属ナトリウム・アジ化ナトリウム・トルエンとニトロ化）をノードとして先に作り、そのあとビスアジド（おすすめ。危ない物なので DESIGN どおり量・温度は書かず、GHS と「事故」で扱う）②ネガ型は KTFR でなく KPR（ポリケイ皮酸ビニル、光で [2+2] 二量化、アジド不要）を先にする：ケイ皮酸（ベンズアルデヒド＋無水酢酸、1868 パーキン）とポリビニルアルコールが要る ③ビスアジドを「仮」で置く（化学ルール1に反するのでおすすめしない）

## O-008 幹2 の続き（本線に近い順）
- ✅ [2-17] イオン交換（遊び方: 実験）：ベークライト＋硫酸 → 陽イオン交換樹脂、ノボラック＋安水＋ホルマリン → 陰イオン交換樹脂、ガラス6＋両方 → イオン交換塔（block/IonExchangeColumnBlock）。蒸留水（E の [2-16]）で右クリック → 脱イオン水 semiconductor-lab:deionized_water（8回で飽和）、硫酸・NaOH で別々に再生。GameTest ion_exchange、CHEMISTRY P10、REFERENCES C10。テスト 179/179
  - **E へ**：脱イオン水ができた（[2-18] 超純水・[9-14] RCA 洗浄に使える）
- ✅ [2-42] 酢酸（遊び方: 実験＋軽）：石灰を入れた木酢液をたき火 → 酢酸カルシウム（かまど → メタノール）、＋硫酸 → 氷酢酸2（寒い所で凍る、item/GlacialAceticAcidItem）。GameTest acetic_acid、CHEMISTRY P11、REFERENCES C11。テスト 180/180
