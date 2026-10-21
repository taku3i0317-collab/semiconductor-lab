# IMPL2_LOG — 2人目の実装担当（IMP2-番号）の記録

担当：幹7（光学）と、その前提で未実装のノード。HANDOFF.md は触らず、ここに書く。
CHEMISTRY.md の行番号は IMP 側の #番号とぶつからないよう O1〜 を使う。REFERENCES.md は「# 光学」の節（O1〜）。

## IMP2-01（2026-10-20）光学の幹を露光（投影レンズ [7-19]）に近い順に
- 調べた結果：幹7 で「今すぐ作れる」のは [7-01] だけ。[7-03] レンズ研磨は研磨皿のピッチ（[2-23] 木タール or [2-20] コールタール）が未実装。[7-19] 投影レンズはさらに [7-09] 干渉計（← [7-05] 鏡・[5-14] 蒸着・[7-10] 水銀ランプ）と [8-14] が要る
- ✅ [7-01] 光学ガラス：フリント（砂5＋PbO1＋ソーダ1 → 調合原料5 → 溶鉱炉20秒）、クラウン（ソーダ石灰ガラス → 溶鉱炉20秒）。かまど・バニラのガラスでは不可。GameTest optical_glass、CHEMISTRY O1〜O3、REFERENCES O1、遊び方: 軽。テスト 83/83
  - 判断（小）：クラウンはソーダ石灰ガラスから（ノードの本物欄「クラウン＝ソーダ石灰・ホウケイ酸系」に沿う）。ホウケイ酸クラウンは [2-15] の後。フリントの融剤は本物はカリだが [2-02] 未実装なのでソーダ（placeholder に明記）
  - 確かさ：レイヴンズクロフト 1674 の鉛は議論あり（Historic England）→ △、lang に ⚠️
  - ROADMAP.md に幹7 の行がないのでチェックは付けていない。TECH_TREE_VIEW.md は gen_tree_views.py（ネットワーク担当）の生成物なので触っていない
- ✅ [2-23] 木材乾留（ユーザーの指示で [7-03] の前提として実装）：乾留炉 wood_retort（燃料式、block/WoodRetortBlockEntity、画面 WoodRetortScreen）、木酢液・木タール・ピッチ。GameTest wood_retort、CHEMISTRY W1〜W3、REFERENCES O2、遊び方: 重。テスト 85/85
  - 共通ファイルの変更：CommonTests.exportMachineRecipes に ChemistryTests.machineRecipes の1行（幹2 の機械が初めてなので）。SemiconductorLabClient に「幹2 化学」の段
  - へえ：ネアンデルタール人の白樺タール（カンピテッロ約20万年前）は二次資料で確認 → ○
- ✅ [2-02] カリ（ユーザーの指示）：木灰（たき火）→ 灰汁（水バケツ、バケツは戻る）→ 炭酸カリウム（かまど）。フリントの調合原料の融剤をソーダ→カリにして flint_glass_batch.placeholder を削除。GameTest potash、CHEMISTRY K1〜K3（O1 を更新）、REFERENCES O3、遊び方: 軽。テスト 86/86
  - 判断：KOH（苛性化）は 🔧 で保留。作業台は出力が1つで方解石の副産物を出せない（消石灰に craftRemainder を付けると耐火れんがなど他のレシピにも方解石が残ってしまう）。使い道が出たときに機械か反応ブロックで
- ✅ [7-03] レンズ研磨：研削（光学ガラス＋砂＋研削皿）→ 研磨（素材＋ベンガラ＋ピッチの研磨皿）→ クラウンレンズ（凸）・フリントレンズ（凹）。道具は item/ReusableToolItem（QuartzStillItem と同じしくみの共通クラス）で戻る。GameTest lens_grinding、CHEMISTRY O4〜O7、REFERENCES O4、遊び方: 軽。テスト 87/87
  - 判断（小）：研磨剤のベンガラは鉄の粒を焼く（クロクス・マルティス）。緑ばん（FeSO4）経由は硫酸鉄の材料が要るので見送り、placeholder に明記
- 次の一手（光学）：[7-04] 色消しレンズ・望遠鏡（前提 [7-01]✅・[7-03]✅ → 今すぐ作れる）。[7-06] プリズム分光も [7-01]・[7-03] で作れる。投影レンズ [7-19] は [7-09] 干渉計（← [7-05] 鏡・[5-14] 蒸着・[7-10] 水銀ランプ）と [8-14] が残る
- 保留：[2-02] の KOH（苛性化）🔧
- IMP-31 の creative_tab_order.txt に今回の17アイテムを追加（tools/gen_tab_order.py を実行。見つけてもらうため TECH_TREE の自分のノード行に「ID: …」を書いた）。合わせた状態で 88/88

## IMP2-01 続き（2026-10-20）
- ✅ [7-04] 色消しレンズ・屈折望遠鏡：クラウン凸＋フリント凹＋銅 → 色消しレンズ、色消し＋銅＋クラウン（縦）→ 屈折望遠鏡（ケプラー式）。右クリックでのぞける：item/TelescopeItem（SpyglassItem を継承）＋ mixin/PlayerScopingMixin（共通の mixins.json に1行）。GameTest telescope（モックのプレイヤーで isScoping を確認）、CHEMISTRY O8〜O9、REFERENCES O5、API_26X に javap 結果、遊び方: 作品。テスト 89/89
  - 判断（小）：単レンズの最初の望遠鏡（ソーダ石灰）・顕微鏡・縮小カメラは作らず、使う所（[3-11]・[7-11]・幹8）ができたときに
  - 人間の確認が要る：のぞいたときの拡大・のぞき穴の絵（TEST_QUEUE）
- 次の一手（光学）：[7-06] プリズム分光（前提 [7-01]✅ [7-03]✅）。[7-05] 反射鏡は段階1 [1-05] 青銅系 が要る
- ✅ [7-06] プリズム分光：プリズム（フリント＋研削・研磨、道具は戻る）、分光器（プリズム＋クラウンレンズ×2＋銅×3）。もう片方の手の試料をたき火・火で見る炎色反応（Na・K・Ca・B・Pb、Ge は紫外で見えない）、空に向けると太陽の暗線（item/SpectroscopeItem、チャットに表示）。GameTest spectroscope、CHEMISTRY O10〜O12、REFERENCES O6、遊び方: 実験。テスト 90/90
  - 判断（小）：プリズムはフリントだけ。分光器での純度確認（ppm の不純物）は、のちの分光ノードに回す
- 次の一手（光学）：[7-05] 反射鏡（段階1 は [1-05] 青銅系が要る）／[7-07] 回折格子（[7-06]✅・[7-05] or [1-03] 銅の針金）／[7-10] 水銀ランプ（[5-04]・[4-10] が要る）。投影レンズ [7-19] への残り：[7-09] 干渉計・[8-14]

## IMP2-01 続き：[7-07] の前提を作る（ユーザーの指示：[1-03]→[1-30]→[1-05]→[7-05]→[7-07]。[1-30] の前提 [1-22] 錫石も ❓ だったので足す）
- ✅ [1-03] 銅の製錬（孔雀石）：新しい鉱石 malachite_ore（浅い石）→ 孔雀石 → かまど/溶鉱炉 → 銅インゴット。共通ファイル ModWorldGen.RULES に1行。GameTest ore_malachite・copper_from_malachite（ore_biome_rules にも追加）、CHEMISTRY M1〜M2、REFERENCES O7。テスト 92/92
  - へえの訂正：ベロヴォデの製錬はマンガンを含む銅鉱石（きれいな孔雀石はビーズ）→ lang を合わせた
- ✅ [1-22] 錫石：新しい鉱石 cassiterite_ore（花崗岩だけ、鉄マンガン重石と同じ帯）→ 錫石。GameTest ore_cassiterite、CHEMISTRY M3。テスト 93/93（REFERENCES は [1-30] とまとめて O8）
- ✅ [1-30] スズ：錫石 → かまど/溶鉱炉 → tin_ingot（c:ingots/tin）。GameTest tin_from_cassiterite、CHEMISTRY M4、REFERENCES O8（[1-22] と共通）。テスト 94/94
- ✅ [1-05] 青銅：銅8＋スズ1 → 調合9 → かまど/溶鉱炉 → bronze_ingot（c:ingots/bronze）。GameTest bronze、CHEMISTRY M5〜M6、REFERENCES O9。テスト 95/95
- ✅ [7-05] 反射鏡 段階1 スペキュラム：銅2＋スズ1 → 合金 → 研削・研磨で鏡 → ニュートン式反射望遠鏡（TelescopeItem でのぞける）。段階2（銀鏡）・3（Al 蒸着）は前提待ちで ❓ のまま。GameTest speculum_mirror、CHEMISTRY O13〜O15、REFERENCES O10、遊び方: 作品。テスト 97/97
- ✅ [7-07] 回折格子（針金格子）：送りねじ2＋銅 → 針金格子（d = 101.6μm、フラウンホーファーの値）、分光器＋格子 → 格子分光器（SpectroscopeItem に gratingMicrons を足し、線ごとに回折角と λ = d·sinθ を表示）。GameTest grating、CHEMISTRY O16〜O17、REFERENCES O11、遊び方: 実験。テスト 98/98
  - 反射格子（スペキュラムに刻む）は [8-12] 刻線機の後
- 次の一手（光学）：[7-10] 水銀ランプ（[5-04]・[4-10] が要る）／[7-09] 干渉計（[7-05]✅ 段階1・[7-03]✅、半透明鏡 [5-14]・[7-10] が要る）／[7-05] 段階2 銀鏡（[1-32]・[6-11]・[2-21]）

## IMP2-01 続き：[7-10] 水銀ランプ（2026-10-20）
- 前提の確認：[1-45] 水銀・[7-02] 溶融石英・[1-28] W線は ✅。未実装は [5-04] 油回転ポンプ（1905）と [4-10] 変圧器（v0.4）
- 決定（ユーザーの指示で検討）：初期の水銀灯（クーパー・ヒューイット 1901、キューヒの石英灯 1903〜06）は直流で動いた → [4-10] は不要。排気は油回転ポンプより前の水銀ポンプ＝[5-15] シュプレンゲル ✅（封止台 0.1Pa）で代える。TECH_TREE の [7-10] の前提に書いた
- ✅ [7-10] 水銀ランプ：組立品（石英管＋水銀＋W線×2）→ 封止台 → 水銀灯の管 → 水銀灯ブロック（block/MercuryLampBlockEntity、ConsumerBlockEntity、直流 385W、LIT で明るさ15、画面は SimpleMachineScreen）。分光器で点灯中のランプを見ると Hg の線（格子分光器なら角度と波長）。GameTest mercury_lamp、CHEMISTRY O18〜O20、REFERENCES O12、遊び方: 作品＋実験。テスト 100/100
  - 共通の変更：block/SealingStationBlockEntity に sealedProduct()（真空管の組立品 → 真空管、水銀灯の組立品 → 水銀灯の管）。SealingStationScreen の判定もこれに。真空管の挙動は同じ（VacuumTests そのまま通る）
- 次の一手（光学 → 露光）：[7-09] 干渉計（半透明鏡 [5-14] 蒸着が要る）・[7-11] 銀塩フォトマスク（[B-02] 写真・[7-04]✅ 縮小カメラ）・[7-19] 投影レンズ（[7-09]・[8-14]）

## IMP2-01 続き：[5-05] → [5-14] → [7-09]（ユーザー指示：年の順が守れる道のうち前提が少ないほう＝案A）
- 前提の確認：[5-05] は [5-04] 油回転ポンプ（← [8-01] 旋盤）と [2-15] ホウケイ酸（← [3-04] ふいご）が ❓。[5-14] は蒸着する金属（Ag [1-32] ❓・Al [1-29] ❓…）が要る。[7-09] は [5-14] が要る
- 道：[1-32] 銀 → [8-01] 旋盤 → [5-04]（段階① 水銀封止＝ゲーデの回転水銀ポンプ 1905）→ [5-05]（ゲーデ 1915：ガラスと鉄、水銀、ヒーター。[2-15] は使わない）→ [5-14]（熱した W 線で銀を蒸発）→ [7-09]
- ✅ [1-32] 銀（灰吹法）：骨灰の皿＋鉛8 → 溶鉱炉 → 銀の粒。GameTest silver_cupellation、CHEMISTRY M7〜M9、REFERENCES O13。テスト 106/106
- ✅ [8-01] 旋盤：電気 750W、鋼 → ポンプのローター。新しく PrecisionTests.java（gametest の fabric.mod.json と CommonTests.exportMachineRecipes に1行ずつ）。GameTest lathe、CHEMISTRY P1、REFERENCES O14。テスト 107/107
  - IMP-30 の MachineLayout.standardTexts() は見本の文 "Power 500.0 kW" が 72px に入らず gui_layout テストで落ちる → 自分の2台（旋盤・水銀灯）は同じ置き場所で見本を 0.8kW・0.4kW にして自分で書いた。standardTexts 本体は触っていない（IMP 側の判断）
- ✅ [5-04] 回転ポンプ（段階① 水銀封止）＋ [5-05] 水銀拡散ポンプ（1つの commit：回転ポンプが拡散ポンプ越しに容器を引くので互いに参照）。block/RotaryPumpBlockEntity・DiffusionPumpBlockEntity、画面 client/PumpScreen（容器の圧力と状態）。真空容器｜拡散｜回転 で 1気圧 → 1Pa → 約0.002Pa。GameTest rotary_and_diffusion_pumps、CHEMISTRY V1〜V3、REFERENCES O15。テスト 108/108
  - 決定（ユーザー指示「1915年ごろの作り方：ガラス製・水銀・ヒーター」）：ゲーデのポンプは「ガラスと鉄」でガラスの種類は資料に無い → [2-15] ホウケイ酸を作らず [2-14] ソーダ石灰ガラス＋鉄の釜⚠️。加熱は電気のヒーター⚠️。TECH_TREE [5-05] に理由
  - gui.semiconductor-lab.pump.* は lang の common 部品に入った（lang_add の自動振り分け）
- ✅ [5-14] 真空蒸着（熱した W 線で銀を蒸発、いちばん古い方法）：block/EvaporatorBlockEntity・client/EvaporatorScreen。隣の容器 0.01Pa 以下（拡散ポンプが要る、シュプレンゲル 0.05Pa では不可）、クラウンガラス＋銀 → 半透明鏡。組み合わせは EvaporatorBlockEntity.recipe に1行ずつ足す。GameTest vacuum_evaporation、CHEMISTRY V4〜V5、REFERENCES O16。テスト 109/109
  - IMP-32 から CHEMISTRY.md・TEST_QUEUE.md は部品（docs/chemistry/・docs/test_queue/）に書いて tools/docs_build.py で組み立てる。[5-04][5-05] の節は --adopt で部品へ移した
- ✅ [7-09] マイケルソン干渉計：item/InterferometerItem。点灯中の水銀灯の緑線 546.1nm で、ねじ1回転 0.5mm → 縞 1831 本 → 0.5000mm。炎では縞が消える。GameTest interferometer、CHEMISTRY O21、REFERENCES O17、遊び方: 実験。テスト 110/110
  - 年の注意⚠️：マイケルソン（1881）の半透明鏡は化学の銀めっき。ゲームでは [5-14] 真空蒸着（1912〜）で作るので年が逆。[7-05] 段階2（銀鏡反応、[6-11] 硝酸・[2-21] アンモニアが要る）ができたら、そちらでも作れるようにする。TECH_TREE [7-09] に書いた
- 次の一手（露光へ）：[7-11] 銀塩フォトマスク（[B-02] 写真＝銀✅・ゼラチン、[7-04]✅ 縮小カメラ）／[7-19] 投影レンズ（[7-09]✅・[8-14] 鏡筒の精密組立）／[7-22] 反射防止膜（蒸着✅）

## IMP2-01 続き：INBOX O-001（2026-10-20）光学の幹で露光の本線に向けて
- 前提を調べた：[7-04] は ✅ 済み。[7-11] 銀塩フォトマスクへは [B-02] 写真（[B-02a] ダゲレオタイプ ← ヨウ素 [2-04]、[B-02b] 湿板 ← 硝酸 [6-11] ← 天然硝石 [6-16]）とゼラチンが要る。[7-19] 投影レンズは [8-14] 鏡筒の組立（← [8-04]・[8-10]・[8-07]）が要る。[1-21] 蛍石は IMP4 が ✅ にしていた
- ✅ [7-22] 反射防止膜：蒸着装置の組み合わせを表（EvaporatorBlockEntity.RECIPES）にして、レンズ＋蛍石（CaF2）→ 反射防止膜付きレンズ。GameTest anti_reflection_coating、CHEMISTRY O22、REFERENCES O18。テスト 115/115
- ⏸ O-001 の残り（[B-02] 写真 → [7-11] 銀塩フォトマスク）は実装せずに質問（2026-10-20）。理由：
  1. [7-11] のマスクの持ち方（DESIGN のデータモデル photomask：design / min_feature_nm / durability）は、DESIGN「露光装置の詳細設計（第3章の前に作る）」が未決。IMP・IMP4 の v0.3 本線（露光・IC 工程）と直接ぶつかる
  2. [B-02] 写真（🌿モノ）は遊び方（カメラ・撮影・現像）の設計が要る（ユーザーが決める物）
  3. 道の途中の [6-16] 天然硝石 → [6-11] 硝酸は IMP4 のエッチング（CP-4 など）でも使う薬品。重複を避けたい
  - **Q-IMP2-1**：[6-16]→[6-11] 硝酸（＋硝酸銀）を IMP2 が作ってよいか、IMP4 が作るか
  - **Q-IMP2-2**：[B-02a] ダゲレオタイプ・[B-02b] 湿板の遊び方（例：カメラのアイテムで景色を写すと「写真」アイテムに、湿板はガラスのネガ）を IMP2 で設計してよいか
  - **Q-IMP2-3**：[7-11] 銀塩マスクの表し方（原図＝ルビリスをどう描くか、パターンの持ち方）は露光装置の設計と一緒に決めるか
  - 回答が来るまで INBOX を待つ。そのあいだにできる光学の候補：[7-15] 紫外光学材料（CaF2 結晶 ← [1-21]✅・[3-14]✅ 誘導炉、合成石英 ← SiCl4 [1-16] 系✅？・[6-01]✅）、[7-05] 段階2 銀鏡（[6-11]・[2-21] が要る）

## IMP2-02（INBOX O-002、2026-10-20）Q-IMP2-1〜3 の答え：硝酸と硝酸銀は IMP2、写真は案を出す、[7-11] は待つ
- ✅ [6-16] 天然硝石：新しいブロック nitre_earth_block（鍾乳石の洞窟の鍾乳石ブロックを置き換える。マンモスケーブの洞窟採掘に合わせた）→ 硝石土×4＋木灰＋水 → 浸出液 → かまど → 硝石 KNO3。ModWorldGen.RULES に1行、ore_biome_rules（MetallurgyTests）に期待を1行。GameTest ore_nitre_earth・saltpeter_from_nitre_earth、CHEMISTRY G1〜G2、REFERENCES O19。テスト 119/119
  - API の注意：26.3 の loot_table は "functions" ではなく "modifier":[{"type":…}]（API_26X.md にもう書いてあった。set_count も同じ）
- ✅ [6-11] 硝酸・硝酸銀：硝石＋硫酸（瓶）＋空き瓶＋石英の蒸留器 → 硝酸（Reg.contained ガラス瓶、Vessel.ALLOWED に1行＝共通ファイル）。銀＋硝酸 → 硝酸銀。年は監査 V-43 に合わせた。IMP4 はこの硝酸を使う（INBOX O-002）。GameTest nitric_acid_and_silver_nitrate、CHEMISTRY G3〜G4、REFERENCES O20。テスト 121/121
- 📝 **写真 [B-02a] ダゲレオタイプ・[B-02b] 湿板の遊び方の案（Q-IMP2-2、ユーザーが選ぶまで実装しない）**
  - 共通の本物の流れ：ダゲレオタイプ（1839）＝銀めっきの銅板をヨウ素の蒸気で AgI に → カメラで露光（数分）→ 水銀の蒸気で現像 → 食塩水で定着 → 1枚きり（ネガなし）。湿板（1851）＝ガラスにヨウ化物入りコロジオン → 硝酸銀の浴で AgI → ぬれているうちに露光（数秒）・現像 → ガラスのネガ → 紙に何枚も焼ける
  - **案A「カメラで景色を写す」**：カメラ（レンズ [7-04] ＋箱）を持って右クリック長押し（ダゲレオタイプは長く、湿板は短く）→ 見ている所を地図のような絵にした「写真」アイテム（額縁に飾れる）。湿板はガラスのネガ → 印画紙に何枚でも焼ける。
    - 良い所：いちばん楽しい・目で見て分かる（「露光時間が短くなった」が体験できる）。
    - 気になる所：絵を作るクライアント側の作業が大きい（バニラの地図の描き方を流用しても数日かかる）。v0.3 の本線より後回しになりやすい
  - **案B「工程を化学で体験、写真の中身はデータ」**：感光板（ヨウ素の蒸気・硝酸銀の浴）→ 露光（感光板を置いて日光に当てる。時間で露光の量が決まる：短いと「うすい」、長すぎると「黒い」）→ 現像（水銀の蒸気／鉄の現像液）→ 定着（食塩水）。写真アイテムには「撮った場所・時刻・露光の良さ」を記録してツールチップに出す（絵はなし）。湿板はネガ → 何枚でも焼ける。
    - 良い所：軽い（今の機械・作業台の作り方で数ノード分）、化学の正しさが中心、露光時間の歴史（数分 → 数秒）が数字で分かる。
    - 気になる所：絵が出ないので見た目は地味
  - **案C「ネガ → 何枚も焼く＝マスク転写の練習」**：写真の中身を「原図」にする。作業台で描いた原図（例：3×3 の明暗）を湿板のネガに縮小して写し、印画紙に何枚でも焼く。これがそのまま [7-11] 銀塩マスク・露光の前段になる。
    - 良い所：v0.3 の本線（マスク・露光）に直結、ムダが少ない。
    - 気になる所：露光装置とマスクの設計（Q-IMP2-3）が決まらないと形が決まらない。景色の写真としての楽しさは少ない
  - おすすめ：案B を先に（今すぐ作れる・軽い）、マスクの設計が決まったら案C の「原図を写す」を足す。案A は仕上げフェーズの楽しみとして後日
  - 前提で足りない物：ヨウ素（[2-04] 海藻灰 → ヨウ素、1811 クールトア）。コロジオン（ニトロセルロース＝綿＋硝酸＋硫酸）は綿がないので仮⚠️（ノード仕様どおり）。臭化物（乾板 1871）は臭素が未実装
- ✅ [7-15] 紫外光学材料の CaF2 の半分：ブリッジマン炉（block/BridgmanFurnaceBlockEntity、3kW、蛍石4＋Ar → CaF2 単結晶）→ CaF2 レンズ。GameTest bridgman_ca_f2、CHEMISTRY O23〜O24、REFERENCES O21。テスト 125/125
  - **Q-IMP2-4**：合成石英（SiCl4 を酸水素炎で加水分解、ハイド 1934）は SiCl4 がまだ無いので保留。SiCl4 は [9-30] エピタキシー（IMP の本線）でも使う。①トリクロロシラン反応器 [1-16] の副産物として出す（本物も SiCl4 ができる）か、②金属シリコン＋塩素の直接塩素化の機械を作るか、③IMP 側で作るのを待つか。決まれば酸水素バーナー [7-02] に SiCl4 の入口を足して合成石英にする

## IMP2-03（INBOX O-003、2026-10-20）フォトマスク（物としての原板）と露光の光学
- A と分担を確認（cross-session）：rubylith（原図）と ExposureTool は A の litho、photomask アイテムと [7-11] 縮小カメラは IMP2（rubylith の成分 ModDataComponents.MASK をそのまま写す）。密着露光でマスクが傷む回数も photomask 側で決める。ExposureTool の λ・CD・N を出典に合わせて直すのは IMP2 でよい（直したら A に知らせる）。光源・投影レンズのノード・REFERENCES の CD/NA・ガイドの「アッベの限界」は IMP2
- SiCl4（Q-IMP2-4）：A は作る予定なし。作るならトリクロロシラン反応器 [1-16] に副産物の出口を足すのが自然（A の意見）。誰が作るかは設計担当へ
- ✅ [7-11] の材料（litho のデータに依存しない化学）：ゼラチン・塩化銀ゼラチン乳剤・感光乾板・硫酸鉄(II) 現像液・チオ硫酸ナトリウム定着液。GameTest photo_plate_chemistry、CHEMISTRY O25〜O28、REFERENCES O22。テスト 128/128。縮小カメラと photomask は A の litho の push を待って作る
- 📝 REFERENCES O23：露光の方法と CD（LITHO_DESIGN §3 の出典）。マイクラライン 1973・DSW4800 1978 は ○、各 CD の値は △（製品資料で未確認）。ExposureTool の値は今のまま（変える根拠がまだ無い）
- ✅ [7-11] 銀塩フォトマスク：縮小カメラ（block/ReductionCameraBlockEntity、画面 ReductionCameraScreen、1kW）。原図（mask 成分を持つ物。rubylith は A が登録）＋乾板＋現像液＋定着液 → フォトマスク（mask 成分をそのまま写す、原図は減らない）。photomask は耐久10（密着露光の機械が damage で傷める）。テストで ChipOps.expose(coated, mask, CONTACT_G) が通るのを確認。GameTest reduction_camera_photomask、CHEMISTRY O29〜O30、REFERENCES O24。テスト 136/136

## IMP2-04（INBOX O-004、2026-10-20 再開後）[8-10] 光学顕微鏡
- ✅ [8-10]：単レンズ（NA 0.2）・複式（NA 0.9）の顕微鏡（item/MicroscopeItem）。持ち替えた手のマスク・原図・設計図・ウェハーの方眼の1マス（ExposureTool の CD）と、アッベの d ＝ 550nm ÷ 2NA をくらべて見える／ぼやける＋フォトマスクの傷の数。A のデータ（mask・chip_design・chip_state の size）は読むだけ、A のファイルは触っていない。遊び方: 実験。GameTest microscope、CHEMISTRY P2、REFERENCES O25。テスト 138/138

## IMP2-05（INBOX O-005）[8-02] → [8-04]
- ✅ [8-02] ねじ切りと互換性部品：旋盤（LatheBlockEntity、自分の [8-01]）に親ねじの入口。送りねじ（減らない）＋鋼 → 規格ねじ×4。削った部品に寸法の誤差の成分 Precision.PART_ERROR_UM（Precision.java で登録＝共通の ModDataComponents は触っていない）。誤差は旋盤ごとのくせ（±25µm、はじめて削るときに決まり保存）。GameTest lathe、CHEMISTRY P3、REFERENCES O26。テスト 139/139
- ✅ [8-04] ノギスとマイクロメータ：年の順に2段（ノギス 1631＝青銅だけ、マイクロメータ 1848＝規格ねじ [8-02] の後）。item/GaugeItem：旋盤の部品の寸法を読みの細かさに丸めて表示、等級 A ≤10µm／B ≤20µm／C はマイクロメータでだけ決まる。マイクロメータのねじ 0.5mm は干渉計 [7-09] とそろえた。遊び方: 実験。GameTest calipers_and_micrometer、CHEMISTRY P4、REFERENCES O27。テスト 143/143
  - 等級を使う物（[8-08] ボールねじステージ・[8-14] 鏡筒の組立の材料に「等級 A の部品」を求める、など）は後日。決めるのはその時の指示で

## S-001（相談役から、Q-IMP2-4 の答え）SiCl4 と合成石英
- ✅ トリクロロシラン反応器（A が昔作った機械）に副産物 SiCl4 の出口を足しただけ（SLOT_SICL4、合成4回ごとに1つ、合成回数を保存）。合成・蒸留・既存のテストは前と同じ（trichlorosilane_reactor も PASS）。SiCl4 は Metallurgy.SILICON_TETRACHLORIDE（[9-30] で IMP4 も使える共通の素材）
- ✅ [7-15] の合成石英：酸水素バーナー（自分の [7-02]）に SiCl4 の入口 → 合成石英（ハイド 1934）。[7-15] は ✅ に。GameTest synthetic_quartz、CHEMISTRY O31〜O32、REFERENCES O28。テスト 144/144

## S-002（相談役から。写真は案B に決定）
- ✅ [2-04] 海藻灰ソーダとヨウ素（[B-02a] の前提）：乾燥昆布 → たき火 → 海藻灰 → 浸出液 → かまどでソーダ、または硫酸でヨウ素。GameTest kelp_ash_and_iodine、CHEMISTRY C1〜C3、REFERENCES O29。テスト 147/147
- ✅ [B-02a] ダゲレオタイプ（案B）：銀めっき板＋ヨウ素 → 感光板、暗箱カメラ（item/CameraItem：押している時間×光で露光の量）→ 露光した板 → 水銀で現像 → 塩かハイポで定着。写真の中身は成分 Branches.PHOTO（item/PhotoData：場所・時刻・露光の量、PhotoItem のツールチップ）。現像・定着は crafting_transmute で成分を引き継ぐ。TECH_TREE の [B-02] と ROADMAP の仕上げに「案A は後で」。GameTest daguerreotype、CHEMISTRY B1〜B3、REFERENCES O30、API_26X に javap の結果。テスト 150/150
  - **Q-IMP2-5（質問）→ 答え：③（2026-10-20 ユーザーの決定、INBOX O-008。ゼラチン乾板で実装）**：湿板 [B-02b] のコロジオンはニトロセルロース（綿＋硝酸＋硫酸）で、綿火薬の仲間。DESIGN「危ない技術の扱い」（爆発物の配合は作らない）とぶつかるので、作り方を決めずに止めた。案：①硝化の工程を作らず「写真用コロジオン（仮⚠️）」を別の手に入れ方にする（例：決まるまで湿板は作らない）、②紙のセルロース＋硝酸＋硫酸で作る（材料の名前だけ・量や温度は書かない）を許す、③湿板を飛ばし、ゼラチン乾板（[7-11] で作った乳剤）で「ネガ → 何枚も焼ける」を体験する（年は 1871 で、湿板 1851 より後）
- ✅ O-007 [12-11] 測量（三角測量）：幹12 の最初のノードなので content/Construction.java・lang 部品 construction・ConstructionTests を作成（Content.java・lang_build.py の ORDER・gen_testworld.py の幹名に1行ずつ）。セオドライト（item/TheodoliteItem、TelescopeItem を継承してのぞける）：のぞいて離すと向きを 1分まで読む → 測点A（成分 survey_station）→ 横に 3m 以上 → 測点B で正弦定理の距離と目標の場所、のぞいた先のブロックまでの実際の距離とずれ。⚠️ 底辺はそのままの長さ。GameTest triangulation、CHEMISTRY K1〜K2、REFERENCES O31。テスト 153/153
- ✅ O-007 [8-22] メートル原器：metre_bar（白金インゴット×3＋セオドライト。セオドライトは作業台で戻る＝getCraftingRemainder）、item/MetreBarItem：ブロックを右クリックで印（成分 metre_mark）→ 別のブロックで長さを m・尺・トワーズで。[8-06] 光波干渉測長の行に「波長で測る相手＝metre_bar」と書いた。GameTest metre_bar、CHEMISTRY M1〜M2、REFERENCES O32。テスト 156/156
  - O-007 の残り [8-03] 定盤は、A の銑鉄 [1-09]（高炉）ができてから
- ✅ O-008 [B-02b]（Q-IMP2-5 の答え：③）：湿板は作らず、ゼラチン乾板でネガ → 何枚も焼ける。カメラは [7-11] の感光乾板も使える（1秒、CameraItem.DRY_TARGET_TICKS）→ 露光した乾板 →＋硫酸鉄(II) → 現像した乾板 →＋ハイポ → ネガ（item/NegativeItem：作業台で中身ごと戻る）。紙×4＋塩＋硝酸銀 → 印画紙×4。ネガ＋印画紙 → 紙焼き。コロジオンの作り方はどこにも書かない（DESIGN ③）、湿板は へえ で紹介だけ。TECH_TREE [B-02b] に決定と理由（2026-10-20）。GameTest dry_plate_negative、CHEMISTRY B4〜B7、REFERENCES O33。テスト 162/162
- ✅ O-009 [8-03] 平面と定盤：銑鉄×4＋砂 → 鋳鉄の板 cast_iron_plate（item/CastPlateItem、成分 plate_curve＝そり 0.1µm 単位、最初のすり合わせで ±20〜60µm）。両手に板＋持ち物のルージュ1つで右クリック → すり合わせ（合わない分の 80％を半分ずつ）。2枚だけでは凸凹のまま合う、3枚を順に → 1µm 以下で定盤 surface_plate（ブロック）。「精密な機械の隣に定盤」の条件は、それを使う機械のときに。GameTest surface_plates、CHEMISTRY P1〜P2、REFERENCES O34、API_26X（Inventory.findSlotMatchingItem・removeItem・Abilities.instabuild）。テスト 163/163
- ✅ [8-07] 除振（O-009 のあと IMPL2_LOG の次の一手、本線 [8-14]→[7-19] の前提）：除振台 isolation_table（定盤＋加硫ゴム）。block/Vibration：動く生き物・トロッコ・動くピストンから床の揺れ（nm）、真下が除振台なら 1/20、縞のゆれ＝2×揺れ÷波長。干渉計 [7-09] が「床の揺れ → 縞のゆれ ±本（±0.5 以上は数えられない）」を出す。露光装置（A・E）も Vibration.nm(level, pos, 本人) を使える。⚠️ 空気ばねと恒温（空調 [12-12]）はまだ。API_26X：EntityType の定数は EntityTypes へ。GameTest vibration_isolation、CHEMISTRY P3、REFERENCES O35。テスト 165/165
- ✅ O-010 1. [8-14] 鏡筒の精密組立（心出し）：鏡筒 lens_barrel（色消しレンズ×4＋鋼×2＋規格ねじ×2、成分 lens_decenter＝4枚のずれ）。利き手に鏡筒・左手に顕微鏡で右クリック → いちばんずれたレンズを 4割に（item/LensBarrelItem）。見える限界＝顕微鏡の分解能と足元の揺れ（[8-07] Vibration）。全部 1µm 以下で centered_lens_barrel（[7-19] の材料）。GameTest lens_centering、CHEMISTRY P4、REFERENCES O36。テスト 168/168
- ✅ O-010 2. [7-19] 投影レンズ（部品まで。露光装置 [9-39] は担当を質問中）：心出し済みの鏡筒＋反射防止膜のクラウンレンズ（crafting_transmute でずれを引き継ぐ）→ projection_lens（item/ProjectionLensItem）。g線 λ は A の ExposureTool.STEPPER_G を読むだけ（A の litho は変えていない）、1/5、NA 0.35⚠️、W＝ずれ合計 µm×0.01、k1＝0.6＋2W、CD＝k1·λ/NA。ProjectionLensItem.cdNm(物)・meetsStepperG(物) を露光装置の担当が使える。MaskData の縮小率（LITHO §9.2）と合わせるなら A と相談。GameTest projection_lens、CHEMISTRY O33、REFERENCES O37。テスト 175/175
- ✅ O-011 [9-20] マスクアライナー（担当 C）：block/MaskAlignerBlockEntity・client/MaskAlignerScreen（ボタンですき間 密着→20µm→80µm）、Semiconductor.MASK_ALIGNER（content/Semiconductor に1行）。レジストを塗った板＋フォトマスク → ChipOps.expose(w, mask, CONTACT_G)（A の litho は読むだけ）。密着はマスクが板1枚ごとに耐久1、近接は傷まずぼけ √(λg)、ぼけ＞CD/2 は焼けない。SemiconductorTests の v03Chain の露光を実機に置き換え（NOT が見つかる）＋ mask_aligner のテスト。塗布 [9-19] は E の機械ができたらつなぐ（今は ChipOps.coat）。CHEMISTRY X1、REFERENCES O38。テスト 178/178
- ✅ O-010 3. [1-11] るつぼ鋼：raw_crucible（粘土×5＋耐火れんが）→溶鉱炉→ clay_crucible、＋鍛鉄×2＋木炭 → crucible_charge →溶鉱炉→ crucible_steel×2（content/Metallurgy に4行）。⚠️ ウーツ式（鍛鉄＋木炭を一緒に）・バニラの溶鉱炉・るつぼは1回。GameTest crucible_steel（MetallurgyTests）、CHEMISTRY CS1〜CS2、REFERENCES O39。テスト 183/183
- ✅ O-010 3. [8-05] ゲージブロック：gauge_block_set（るつぼ鋼×4＋水バケツ＋ルージュ、item/GaugeBlockSetItem、111個）。空中で右クリック → 目標の長さの組み合わせ（GaugeBlockSetItem.combine）。左手にマイクロメータで旋盤を右クリック → くせを ±1µm に（LatheBlockEntity.calibrate を足した）。GameTest gauge_blocks、CHEMISTRY P5、REFERENCES O40。テスト 185/185。O-010 はこれで全部
- ✅ O-012 1. [8-06] 光波干渉測長：干渉計 [7-09] で点灯中の水銀灯を右クリックするとき、もう片方の手のゲージブロック（ランダムな1個を縞の本数で測り、表示とのずれ ±0.2µm）・メートル原器（1m＝1,831,166 波長）を光で測る。揺れていると測れない。InterferometerItem に wavelengths・GAUGE_ERR_NM。GameTest light_wave_length、CHEMISTRY P6、REFERENCES O41。テスト 190/190
- ✅ O-012 2. [7-05] 段階2 銀鏡反応：クラウンガラス＋硝酸銀＋アンモニア水＋砂糖 → 半透明鏡（half_silvered_mirror_chemical）、＋もう一度 → 銀鏡 silvered_mirror。銀鏡で反射望遠鏡（フーコー 1857）・干渉計（マイケルソン 1881）の別のレシピ。TECH_TREE [7-09] の年の ⚠️ を解いた（蒸着の道も残す）。GameTest silvering、CHEMISTRY O34、REFERENCES O42。テスト 191/191。O-012 の 1・2 はこれで全部（[8-20]・[8-08] はユーザーの答え待ち）
- ✅ O-013 [8-31] 研削盤・[8-20] 玉軸受（ユーザーの決定：①に一工夫）：block/GrinderBlockEntity（鋼球＋砥石＋測る道具 → 研削した／そろえた鋼球）、item/BallBearingItem（段階1〜3、回る機械を右クリックで入る、前の物は返る）。A と合意して energy/ConsumerBlockEntity に入口：BEARING_MACHINES（旋盤・圧縮機・ウェハー切断機・研磨機・レジスト塗布機・CZ 引き上げ機）、bearingGrade（保存 "Bearing"、無ければ0）、drawWatts＝workWatts×倍率（仕事の時間は同じ）、壊すと軸受が落ちる。⚠️ 0.95・0.85・0.70 はゲームの値。GameTest ball_bearings、CHEMISTRY P7〜P8、REFERENCES O43。テスト 195/195（machineMenus・cable_network を含む全部）
- ✅ A の CHEMISTRY v0.3 レビュー（O-011 ③）の3件を直した：2_chemistry C1 バリラ＝陸の塩生植物・ケルプはソーダ数%（✅→⚠️、kelp_ash.placeholder と REFERENCES O29 も）、8_precision P6 の波の数を「約183万（546.1nm に丸めた値）」に、1_metallurgy M4 と tin_ingot.placeholder を「CO/CO2」に
- ✅ O-014 1. [8-08] ボールねじ：ball_screw（送りねじ＋鋼＋鋼球、段階は bearing_grade を使う、item/BallScrewItem）。旋盤の親ねじの入口に入る（LatheBlockEntity.isMasterScrew）：部品の誤差 ×1/0.5/0.25/0.1、drawWatts も下がる。送りの細かさ feedUm 10/5/2/1µm は [8-17] で使う。GameTest ball_screw、CHEMISTRY P9、REFERENCES O44。テスト 196/196
  - **A へ**：[8-08] ができたので、[9-32] ダイシングの段階2（円盤刃）の前提がそろった
- ✅ O-014 2. [8-17] アライメント：A が ChipState.overlayNm・ChipOps.expose(…, overlayNm) を入れた（fa977e6a）ので、マスクアライナーに台のねじの入口 SLOT_STAGE（送りねじ・ボールねじ）と overlayNm（送り・顕微鏡 0.31µm・床の揺れの最大）を足し、ずれ付きで expose。画面に「ずれ ○µm」。v03Chain は台なし（20µm）のまま NOT が見つかる。GameTest alignment、CHEMISTRY P10、REFERENCES O45。テスト 198/198
- ✅ O-015 1・2. [1-40] クロム・[2-07] 重クロム酸カリウム：新鉱石 chromite_ore（深層岩だけ Y−64〜0、ModWorldGen.RULES・ore_biome_rules・oreChromite に1行ずつ）。焙焼の仕込み →溶鉱炉→ クロム酸ナトリウム → ＋硫酸＋炭酸カリウム → 重クロム酸カリウム（Chemistry に1行）→かまど→ Cr2O3 → ＋アルミ×2＋るつぼ →溶鉱炉→ クロム×2。GHS（6価クロム）と1975 の六価クロム鉱さい問題を教訓として note4 に。工程が作業台・炉だけなので、その場の機械の「事故」はなし（機械にする時に考える）。GameTest chromium・oreChromite、CHEMISTRY CR1〜CR4、REFERENCES O46。テスト 203/203
  - **D へ**：[2-07] 重クロム酸カリウムは C が作った（Chemistry.POTASSIUM_DICHROMATE、recipe potassium_dichromate）。触らないでください。重クロム酸ゼラチン [2-31] で使うときはこのアイテムをそのまま使えます
- ✅ O-015 3. [7-12] クロムマスク：蒸着の組み合わせに 合成石英＋クロム → chrome_blank（EvaporatorBlockEntity.RECIPES に1行）。縮小カメラ（ReductionCameraBlockEntity）：乾板の入口にブランク、薬はレジスト＋塩酸 → chrome_photomask（耐久100）、乳剤は方眼12まで（EMULSION_MAX_GRID、画面に「乳剤では細かすぎる」）、瓶は機械の上に落とす。マスクアライナーの isMask に追加。photomask の形・A の litho はそのまま。GameTest chrome_mask、CHEMISTRY O35、REFERENCES O47。テスト 207/207。O-015 はこれで全部

## S-003（2026-10-20）[9-39] 縮小投影露光の担当
- [9-39] は C が担当（ユーザーの答え「おすすめで」、S-003）
- 前提の確認：[9-20]✅・[7-19]✅（部品）・[8-08]✅・光源 [7-10]✅ はそろった。未実装：**[12-14] 層流クリーンルーム**（← [12-13] HEPA ← [12-12] 空調 ← [11-13] 冷凍機）と **[10-15] 露光装置の制御**（← [10-14] マイコン ← [10-13] CPU。「密着露光で作った IC → CPU → 制御 → ステッパー」のブートストラップ）
- **Q-IMP2-6（質問）**：[10-15] は自作 CPU が前提で、ゲームの大きな山（第3〜4章の目標）なので、C が仮で代わりを作るのは避けたい。案：①[10-15] ができるまで [9-39] は作らない（クリーンルームの道だけ先に作る）、②[9-39] を「制御は人の手（ハンドル）で1チップずつ送る」近似で先に作り、[10-15] ができたら自動送り（速さ）を足す（⚠️ を TECH_TREE とゲームに）、③[10-13]〜[10-15] の担当を決めてから
- 答えが来るまで：[9-39] の前提のうち C の範囲で作れるクリーンルームの道（[11-13] 冷凍機 → [12-12] 空調 → [12-13] HEPA → [12-14] クリーンルーム）を順に作る。[12-14] の「クリーンルーム判定」（ROADMAP v0.5 #5、第5章の前提）は A の歩留まりとつながるので、作る前に A に形を確かめる
- ✅ S-003 の前提 [11-13] 冷凍機：幹11 の最初のノードなので content/Agriculture.java・lang 部品 agriculture・AgricultureTests を作成（Content.java・lang_build.py の ORDER・gen_testworld.py の幹名・gametest の fabric.mod.json に1行ずつ）。block/RefrigeratorBlockEntity：冷媒アンモニア水（減らない）＋水バケツ → 氷＋バケツ、1kW・20秒。GameTest refrigerator、CHEMISTRY A1、REFERENCES O48。テスト 211/211
- ✅ S-003 の前提 [12-12] 空調：block/AirConditionerBlockEntity（2kW 連続、材料なし）。8 ブロック以内で動いていると温度のゆれ ±3℃ → ±0.1℃（swingC・thermalNm）。MaskAlignerBlockEntity.overlayNm に温度のずれを足した（空調なしだと台が 3.3µm 伸び縮み、ボールねじ段階3 でも 3.3µm）＝ [8-07] の恒温の残りが解けた。alignment テストの期待値を 3.3µm に直した。GameTest air_conditioning、CHEMISTRY K3、REFERENCES O49。テスト 213/213
- ✅ S-003 の前提 [12-13] HEPA フィルター：platinum_bushing（ReusableToolItem）→ glass_fiber → hepa_filter（content/Construction）。空調機にフィルターの入口（AirConditionerBlockEntity.SLOT_FILTER、filtered()）。ソーダ石灰ガラスの溶鉱炉はクラウンガラスに使われていたので、白金のブッシングの道にした。GameTest hepa_filter、CHEMISTRY K4、REFERENCES O50。テスト 214/214
  - [12-14] クリーンルーム判定の形は A に相談中（返事が来るまで作らない）
- ✅ S-003 の前提 [12-14] 層流クリーンルーム（A と合意：等級は C、汚れの反映は A の ChipOps.addDust）：hepa_ceiling（ブロック）、block/CleanRoom.classAt（部屋をたどる・天井の HEPA の割合・HEPA 入りの空調）。マスクアライナーが露光のあとに等級の汚れ（2/1/0）を足し、画面に部屋の等級。v03Chain の汚れは 14 → 24 になったが洗浄台で落ちて NOT が見つかる。GameTest clean_room、CHEMISTRY K5、REFERENCES O51。テスト 220/220
  - [9-39] の前提で残るのは [10-15] 制御だけ（Q-IMP2-6 の答え待ち）
- ✅ S-004（Q-IMP2-6 の答え②）[9-39] ステッパー段階1：block/StepperBlockEntity・client/StepperScreen、Semiconductor.STEPPER（1行）。ボタンで1チップずつ4回送って焼く（手で送る）、ChipOps.expose(STEPPER_G＝方眼16)、レンズの CD が届かないと不可、マスクは傷まない、重ね合わせ（MaskAlignerBlockEntity.overlayNm）と汚れ（CleanRoom）はアライナーと同じ。段階2（[10-15] の自動送り）は後。GameTest stepper、CHEMISTRY X2、REFERENCES O52。テスト 233/233
- ✅ A の S-008（方眼の係数 8→16：密着16・投影24・g線ステッパー36…）に合わせた：乳剤（銀塩）マスクの上限 EMULSION_MAX_GRID を ExposureTool.PROJECTION_1X.grid（今 24）から読むように（数字を直書きしない）、chrome_mask テストをステッパーの方眼に、lang・TECH_TREE・CHEMISTRY・TEST_QUEUE の方眼の数字（12/16/8 → 24/36/16）を直した。stepper テストは A が直してくれた
- ✅ O-016 1. [8-15] 水晶振動子：rock_crystal（石英鉱脈の追加の pool 4％、Metallurgy に1行・loot に1つ）、diamond_saw（戻る）、quartz_blank（item/QuartzBlankItem：成分 blank_thickness_um、ルージュで 20µm ずつ）、crystal_resonator（item/CrystalResonatorItem：振動数・膜厚計）。EvaporatorBlockEntity に蒸着した回数 runs（保存 "Runs"）。GameTest crystal_resonator、CHEMISTRY P11、REFERENCES O53。テスト 238/238
  - **使い方（のちの計算機 [10-] の時計へ）**：CrystalResonatorItem.frequencyHz(ItemStack) で振動数（Hz）。厚さは QuartzBlankItem.thicknessUm（成分 semiconductor-lab:blank_thickness_um、無ければ 500µm）。f＝1661kHz·mm÷厚さ。時計の機械は振動子をスロットに入れて、この値を tick あたりの数に直せばよい
- ✅ O-016 2. [8-18] 人工水晶：hydrothermal_charge・cultured_quartz（content/Precision）、D の AutoclaveBlockEntity.RECIPES に static で1行（280℃・15気圧・40秒、圧力不足は仕込みが戻る）。quartz_blank_cultured。⚠️ 本物は 350℃・1000気圧以上（釜の上限 300℃・20気圧に合わせた）。GameTest cultured_quartz、CHEMISTRY P12、REFERENCES O54。テスト 239/239
  - **D へ**：AutoclaveBlockEntity.RECIPES に "cultured_quartz" を1行足しました（content/Precision の static、D のファイルは触っていない）
- ✅ O-016 3. [8-12] 刻線機：block/RulingEngineBlockEntity（鏡＋ダイヤモンド＋送りねじ → ruled_grating、刻み終わりの揺れ・温度でゴースト＝成分 grating_ghosts）、ruled_spectroscope（transmute でゴーストを引き継ぐ）。SpectroscopeItem に溝の総数（分解能）を足し、二重線（D 線・水銀の黄）が分かれるか・ゴーストのメッセージ（針金の格子は WIRE_GRATING_LINES＝246）。GameTest ruling_engine、CHEMISTRY P13、REFERENCES O55。テスト 241/241。O-016 はこれで全部
- ✅ O-017 1. ステッパーの光源 i線：ProjectionLensItem をレンズごとの ExposureTool・NA に（g線 NA0.35・i線 NA0.7）、meetsTool。Optics に projection_lens_i（鏡筒＋CaF2 レンズ）・i_line_filter（蒸着：合成石英＋蛍石×2、EvaporatorBlockEntity.RECIPES に1行）。StepperBlockEntity にフィルターの入口（SLOT_FILTER）、toolFor／lensMatches。A の ExposureTool.STEPPER_I は読むだけ。GameTest stepper_i_line、CHEMISTRY X3、REFERENCES O56。テスト 244/244
- ✅ O-017 2. [8-11] 電子顕微鏡：block/ElectronMicroscopeBlockEntity・client/ElectronMicroscopeScreen（2kW、隣の真空容器 0.01Pa 以下で電子線、入口の見る物を光学顕微鏡と並べて「電子顕微鏡でだけ見える」）。MicroscopeItem.cellNm の表にない方眼の逆算を A の係数 16 に（microscope テストも）。GameTest electron_microscope、CHEMISTRY P14、REFERENCES O57。テスト 246/246
- ✅ O-017 3. [5-07] ターボ分子ポンプ：block/TurboPumpBlockEntity（玉軸受 段階2以上・前段の回転ポンプ・10Pa 以下で 10⁻⁶Pa）、Vacuum.TURBO_PUMP、PumpScreen に no_bearing。回転ポンプの相手にターボを足した（RotaryPumpBlockEntity.target）。ConsumerBlockEntity.BEARING_MACHINES に turbo_pump（A に伝える）。蒸着装置は隣の容器に動く拡散ポンプがあると Al の膜にほこり+1（TurboPumpBlockEntity.backstreams、A の ChipOps.addDust）。GameTest turbo_pump、CHEMISTRY V6、REFERENCES O58。テスト 248/248。O-017 はこれで全部
- ✅ O-018 1. [6-08] 希ガス：block/RareGasColumnBlockEntity（GasSource、酸素1＋窒素1 → Kr/Xe/Ne を mmol で貯める）、Gas に NEON/KRYPTON/XENON の袋とボンベ、GasType に3つ足した（ガスホルダーの色は default のまま）。GameTest rare_gases、CHEMISTRY G5、REFERENCES O59。テスト 252/252
- ✅ O-018 2. [6-14] フッ素：Chemistry に potassium_fluoride、Gas に fluorine_cylinder（袋は作らない＝ゴムを侵す、鋼のボンベに直接）と FLUORINE_CELL（block/FluorineCellBlockEntity・client/FluorineCellScreen）。事故は DESIGN⑥ どおり（水素が逃げないと混ざってフッ素を失い止まる、地形は壊れない）。Vessel の相性表は変えていない（ボンベの容器は鋼）。GameTest fluorine_cell、CHEMISTRY G6、REFERENCES O60。テスト 253/253
  - E からの相談（[8-33] 質量分析・[5-06] 油拡散ポンプ）：どちらも E が作ってよいと返事した（C は O-018 で手がいっぱい）
- ✅ O-018 3. [7-20] KrF エキシマレーザー：Optics に krf_laser（耐久＝ガス 64）・projection_lens_krf（NA 0.8・k1 0.35、ProjectionLensItem に k1Base を足した）。ステッパーの光源の入口に KrF レーザー（ExposureTool.KRF、方眼68、1チップでガス1）。詰め替え krf_laser_refill。GameTest stepper_krf、CHEMISTRY O36、REFERENCES O61。テスト 260/260。O-018 はこれで全部（ArF・液浸・[7-13] ルビーレーザーは後）
- ✅ O-019 1. [7-13] ルビーレーザー：ruby_feed（アルミナ＋Cr2O3）→酸水素バーナー → ruby_boule → ＋銀の鏡×2 → ruby_rod。xenon_flashlamp（石英管＋Xe＋W線）。ruby_laser（item/RubyLaserItem、耐久＝ランプ 100）：1発で持ち物の鉛蓄電池から 1kJ。干渉計をもう片方の手に → 水銀灯とコヒーレンスの比べっこ。ガラス・氷・金属ブロックに当てると反射の事故（本人に盲目4秒）。GameTest ruby_laser、CHEMISTRY O37〜39、REFERENCES O62
- ✅ A の S-010 の依頼：ステッパーを ChipOps.exposeAtCd（レンズの CD → 方眼）に。lensOk はレンズの有無だけ、画面の「ぼけ」はマスクの方眼 > gridFor(CD)。stepper_krf に「心出しの良いレンズほど方眼が細かい」を足した
- ✅ O-019 2. [7-14] CO2 レーザー：反射炉で方解石 → 生石灰＋CO2（Gas.CARBON_DIOXIDE_GAS、煙道のスロット。A の反射炉に1行と表示「煙道」を足した）。岩塩の窓 salt_window（耐久64）。CO2 レーザー（block/Co2LaserBlockEntity、1kW、画面 Co2LaserScreen）：CO2＋N2 の袋で 8 回、N2 なしは 15W で溶かせない、N2 ありで 60W → 珪石 → 溶融石英。ガラス・石英の窓は割れる。GameTest co2_laser、CHEMISTRY O40〜42、REFERENCES O63
- ✅ O-019 3. [7-21] 超低膨張ガラス：ule_feed（SiCl4×8＋TiCl4×1、[1-41] IMP3 の四塩化チタン）→ 酸水素バーナー → ule_glass。干渉計（InterferometerItem.expansionPpm・thermalNm）：もう片方の手に ULE・石英・ガラス・鋼・鉄・銅 → 100mm の棒の熱膨張を縞で（空調の ±0.1℃ ともつながる）。インバー [1-54] はまだないので比べる相手は鋼。GameTest ule_glass、CHEMISTRY O43〜44、REFERENCES O64。O-019 はこれで全部
- ✅ O-020 1. [7-05] 段階3 アルミ蒸着の鏡：蒸着の表に クラウンガラス＋アルミ → aluminized_mirror。item/MirrorItem（スペキュラム・銀・アルミの鏡に）：水銀灯で 546/365/254nm の反射率、空中で曇り方。反射望遠鏡のレシピにアルミ版。GameTest aluminized_mirror、CHEMISTRY O45〜46、REFERENCES O65
- ✅ O-020 2. [8-23] キログラム原器：kilogram_prototype（白金インゴット＋ベンガラ、item/KilogramPrototypeItem、値はバニラの custom_data：磨きのずれ ±50µg・汚れの速さ 0〜3µg/日・作った日）。2つ持って右クリック → 差 µg。イリジウムは TECH_TREE にも MOD にもないので白金だけ（1799 の形、⚠️ placeholder）。**提案（判断待ち）**：イリジウムは白金を王水に溶かした残り（オスミリジウム、テナント 1803）から取れる。王水 spent_aqua_regia の工程に「溶け残り」を足して [1-xx] イリジウムのノードにすれば、1889 の Pt-Ir 原器にできる。GameTest kilogram_prototype、CHEMISTRY P15、REFERENCES O66
- ⏸ O-020 3. [8-24] 国際単位系・[8-25] ワット天秤：**判断待ちで止める**（INBOX「決められなければ案を書いて止まる」）。理由と案：
  - [8-24]：TECH_TREE の MC が「未定（実装の前に相談）」。**案A（おすすめ）**：1960 の SI と同時にメートルが「クリプトン86 の橙の線 605.78nm の 1,650,763.73 波長」になったので、クリプトン86 ランプ（石英管＋クリプトン[6-08]＋W線。Kr-86 は質量分析[8-33]で濃くする⚠️）を干渉計[7-09]の光源にして、メートル原器なしで 1m を測れるようにする（原器 → 光の波長への移り変わりを体験）。SI の7つの単位と接頭語は図鑑・ガイドで。**案B**：SI の表のアイテムだけ（見るだけになるので不可と思う）
  - [8-25]：前提の超伝導 [I-20]・電圧の量子標準 [I-34]（ジョセフソン）は液体ヘリウム（約4K）が要るが、[6-15] は「ヘリウム 回避（実装しない）」の決定でぶつかる。**案A**：量子標準なしの古典版。電圧はウェストン標準電池（1893、カドミウム⚠️毒性）、抵抗は標準抵抗、速度はルビーではなく連続のレーザー（He-Ne は He なので不可 → 案の中で要相談）を干渉計で。コイル＋磁石＋真空[5-07]のワット天秤ブロックで、キログラム原器[8-23]の質量を電気から測る（精度は 10⁻⁶ ほど）。**案B**：[6-15] の決定を見直して He・超伝導・ジョセフソンをノード化してから。**案C**：[8-25] は図鑑と [I-09]（2019 の定義し直し）の説明に回し、ゲームは [8-24] 案A まで
  - 決まったら INBOX に書いてください。それまで O-020 の 3 は手を付けない
- 待機終了（2026-10-21 05:23）：O-020 の到着（10-20 23:21 ごろ）から6時間、INBOX に変化なし。O-020 の 3（[8-24]・[8-25]）は判断待ちのまま
- ✅ S-005 1. [8-24] 国際単位系（案A）：krypton_lamp（石英管＋Kr＋W線×2、耐久64）。干渉計＋反対の手にランプ → ブロック2つで距離をクリプトンの波の数で（InterferometerItem.measureWithKrypton、印は custom_data）。1m で「1,650,763.73 波長」。空中で SI の説明。SI の7単位・接頭語は note3・REFERENCES。GameTest krypton_metre、CHEMISTRY P16、REFERENCES O67
- ✅ S-005 2. [8-25] ワット天秤（案A 古典版、ヘリウム回避のまま）：cadmium（Metallurgy：酸化亜鉛×6＋木炭×2＋石英の蒸留器、亜鉛の副産物。**ネットワーク担当へ：カドミウムのノードがまだない**）、weston_cell・standard_resistor（Precision）、kibble_balance（block/KibbleBalanceBlockEntity・KibbleBalanceScreen、200W・30秒）：原器のずれ±あいまいさ（空調・真空容器で減る、最良 約4.6mg で 50µg は見えない＝量子標準は説明だけ）。イタイイタイ病を cadmium.note4・REFERENCES に。GameTest kibble_balance、CHEMISTRY P17〜19、REFERENCES O68
- ✅ S-005 3（v0.5、C の範囲）：[7-20] ArF（arf_laser・projection_lens_arf NA 0.85）と [7-16] 液浸（projection_lens_immersion NA 1.35、ステッパーに超純水の入口 SLOT_WATER、1チップで1本）。**直した不具合**：ステッパーのレンズの入口が g線のレンズしか受けなかった（i線・KrF のレンズが手で入らなかった）→ どの投影レンズも受ける。v0.5 の 1 焦点深度・2 CMP・4 歩留まり は litho の中心（A）の設計が要るので手を付けていない。GameTest stepper_arf_immersion、CHEMISTRY O47〜48、REFERENCES O69
- ✅ O-021：1 ArF・3 液浸は S-005 で済み（b3093036）。2 スキャナー：block/ScannerBlockEntity（StepperBlockEntity を継承、入口にマスクの台のねじ SLOT_MASK_STAGE）、client/ScannerScreen。CD はスリットで収差の効きが 0.5 倍（ProjectionLensItem.cdNm(lens, aberration)）、重ね合わせは板の台＋マスクの台÷4。ステッパーは lensCd()・stagesReady() で上書きできる形に。存命の人の名前（CO2 レーザー・液浸）を lang と REFERENCES から外した。GameTest scanner、CHEMISTRY X4、REFERENCES O70
- 🔧 O-022 1. マルチパターニング（機械の側だけ）：縮小カメラ[7-11]に「分ける」ボタン（1 → 2 → 1、ReductionCameraBlockEntity.partOf：列の番号の偶数・奇数で 1/2・2/2 のマスク、design の末尾に [1/2]）。2枚を重ねると元どおり、1枚ずつはとなりの列に光がない（ピッチ2倍）。GameTest multipatterning_split、REFERENCES O71。**A に相談中**：exposeAtCd で、となりの列に光がないマスクを gridFor(cd)×2 の方眼まで焼けるとしてよいか（MaskData に旗は要らない案）・2枚の間のずれを歩留まり（#4）でどう使うか。今の litho は同じ層の窓が重なっていくので、判定が入れば LELE（焼く→エッチ→焼く→エッチ）はそのまま遊べる
- ❓ **ユーザーに聞くこと（O-022 1）**：何回まで分けるか。**案A（おすすめ）**：今は2枚（LELE、ArF 液浸の時代）まで。3〜4枚（LELELE・自己整合の SADP/SAQP）は v1.0 の前に足す。**案B**：今4枚まで（SAQP は線の両側に膜をつける工程＝CVD・エッチングの機械が要る）
- 📝 O-022 2. クリーンルーム（グレード制）の今の形（作らずにまとめ）：[12-14] CleanRoom.classAt は、機械の上の空気から囲まれた部屋（512 マスまで）をたどり、天井の HEPA の天井の割合（80%↑＝クラス100、25%↑＝クラス1000）と HEPA フィルター入りで動く空調機[12-12]で等級を決める。効くのは露光の機械（マスクアライナー・ステッパー・スキャナー）だけで、露光のあとの汚れ 外＋2・クラス1000＋1・クラス100 0（ChipOps.addDust、失敗にはせず検査台の漏れ電流に効き、洗浄台で落ちる）。画面に部屋の等級が出る。**足りない所の案（判断待ち）**：① 汚れが入る工程を露光以外にも（拡散炉・CVD・CMP など A・E の機械の担当と相談）② v0.5 #4 歩留まり Y≒e^(−D×A) の D を部屋の等級から（A の litho と一緒に）③ もっと上の等級（クラス10・1、1980年代のミニエンバイロメント・密閉した運び箱）④ 人の出入り：防塵服を着ていないと汚れ＋1 ⑤ 部屋の大きさの上限 512 マス（大きな工場が作れない）を上げるか。**おすすめは ②（歩留まりで等級に意味が出る）→ ① の順**
- 📝 B（ネットワーク担当）へ：カドミウム cadmium（Metallurgy、酸化亜鉛の蒸留、シュトロマイヤー 1817）を [8-25] の前提として作ったが、TECH_TREE にノードがない
- ✅ S-006：マルチパターニングは2枚まで（決定）を DECISIONS に。作ってある形（MAX_SPLIT=2）のまま。A との相談（gridFor×2・ずれと歩留まり）は続ける。クリーンルームの案（O-022 2）はまだ判断待ち
- ✅ O-023 1. [8-21] 六分儀：sextant（item/SextantItem、Navigation＝世界の緯度・経度・グリニッジ時刻・太陽の高さ）。昼前から何回か測り、下がり始めたら 緯度＝90°−南中高度。もう片方の手に時計があれば経度（[14-03] で）。⚠️ 季節なし・1°＝1000 ブロック。GameTest sextant_noon_sight、CHEMISTRY P20、REFERENCES O72。API_26X：空が見えるかは BlockAndLightGetter.canSeeSky
- ✅ O-023 2. [14-03] 経度とクロノメーター：幹14 のファイル content/Transport.java を作った（Content・lang_build の ORDER・gen_testworld・fabric.mod.json に1行ずつ、TransportTests）。marine_chronometer（item/ChronometerItem、1日の狂い ±0.5秒⚠️、custom_data）。六分儀で南中を測るときもう片方の手に持つと経度。GameTest chronometer_longitude、CHEMISTRY T1、REFERENCES O73
- ✅ O-023 3. [8-35] クオーツ腕時計：quartz_watch（ChronometerItem、1日 ±0.2秒⚠️）。前提の [4-17] ステッピングモーターは TECH_TREE で遊び方が未定なので、時計用の部品 stepping_motor だけ作った（Power、電磁石＋磁石＋鉄）。時計を両手に持つと2つの差。GameTest quartz_watch、CHEMISTRY P21〜22、REFERENCES O74。O-023 はこれで全部
- ✅ O-022 1 の続き：A が exposeAtCd に分けたマスクの判定を入れた（4e30fd22：名前の [1/2]・[2/2]＋横どなりに描いた所なし → 方眼の2倍まで、上限148）。縮小カメラはそのまま合う。ステッパーの画面の「ぼけ」は exposeAtCd の理由（too_fine）で出すように直した。GameTest stepper_lele。2枚目のずれ（線の太さのばらつき）は歩留まり #4 で A が決める
- ✅ S-007（v0.6 に進んでよい）1. 前提 [1-23] 輝水鉛鉱・[1-31] モリブデン：銅の鉱石から 5%（Metallurgy.init の MODIFY_DROPS）、反射炉で MoO3＋SO2（A の反射炉に1行）、水素炉に REDUCE_MO（A の水素炉に1モード・画面に1行）。GameTest molybdenum、CHEMISTRY MO1〜2、REFERENCES O75
- ✅ S-007 2. [7-17] 多層膜ミラー：mo_si_target（Mo＋多結晶シリコン）、multilayer_mirror（item/MultilayerMirrorItem、対の数は custom_data、R＝0.70·tanh²(N/15)⚠️）。E のスパッタ装置に分岐を足した（E と合意：入口に ULE・途中の鏡、的に Mo/Si、product の頭で mirrorProduct）。ULE[7-21] が基板（決定どおり溶融石英の近似から置き換え）。GameTest multilayer_mirror、CHEMISTRY O49、REFERENCES O76
- ✅ S-007 3. [8-13] 液滴発生器：droplet_generator（item/DropletGeneratorItem、TECH_TREE どおり [7-18] の部品）。右クリックでレイリーの滴の大きさ・間隔・数。GameTest droplet_generator、CHEMISTRY P23、REFERENCES O77
- ✅ S-007 4. [7-18] EUV 光源：euv_source（block/EuvSourceBlockEntity・client/EuvSourceScreen、2kW）。隣の CO2 レーザーを何台でも駆動（Co2LaserBlockEntity.drive：窓・ガス1回・電気を使って赤外線の W を返す）、EUV＝合計×5%×集光鏡の反射率。水素なしは集光鏡にスズ（MultilayerMirrorItem.tinNm、反射 ×0.9/nm）。GameTest euv_source、CHEMISTRY O50、REFERENCES O78。S-007（v0.6 の C の範囲：Mo → 多層膜 → 液滴 → 光源）はここまで。EUV 露光装置 [9-40]（マルチブロック）は A の v1.0 #1 待ち
- ⏸ S-008（ユーザーの決定 10:57）：v1.0（EUV）はユーザーの決定で後回し。S-008 を読む前に S-007 として [1-23][1-31] Mo・[7-17] 多層膜・[8-13] 液滴・[7-18] EUV 光源まで push 済み（06f1db37、テスト 332/332）。ここで区切り、v1.0 のノードはこれ以上進めない
- 待機中（v0.4・v0.5 の残り待ち）。C の候補：v0.5 #5 クリーンルームのグレード制の残り（O-022 2 の案、判断待ち）・#6 マルチパターニング（A の判定は 4e30fd22 で入り、ステッパー側も済み 44d600a6。2枚目のずれは歩留まり #4 で A が決める）
- ✅ O-025（仮決定：おすすめ ②→①）：CleanRoom.Grade に欠陥の密度 D（外 5・クラス1000 1・クラス100 0.2 個/cm²⚠️）、入口 CleanRoom.defectDensity(level,pos)・poissonYield(D,A)・dustAt(level,pos)。DECISIONS に1行。式への組み込み（v0.5 #4）は A に、露光以外の工程（拡散炉・CVD・CMP など）の dustAt は A・E にそれぞれ頼んだ（C は他の担当の機械を触らない）。GameTest clean_room_yield、CHEMISTRY CR1〜2、REFERENCES O79
- 📝 O-026 v0.4「5. k1 改善の研究要素 ❓」の案（作っていない。決まるまで待つ）
  - **本物（年・出典は ⚠️ 要確認のものあり）**：k1 は「レンズの CD＝k1·λ/NA」の係数。理論の下限は 0.25（線と間のくり返し）。下げた主な技術：
    ① **位相シフトマスク**（1982 年、IBM の研究者が提案 ⚠️。となりの透明部分の光の位相を半波長ずらすと、境目で光が打ち消し合ってコントラストが上がる。石英を削る・薄い膜で位相をずらす）
    ② **変形照明（斜め入射：輪帯・四重極）**（1990年代はじめに実用化 ⚠️。マスクに光を斜めに当てると、回折した光の片方だけでも像ができ、細かいくり返しが写る）
    ③ **OPC（光近接効果補正）**（1990年代半ばから。写るとゆがむ形を、計算でマスクの形を先にゆがめておいて直す。計算機が要る）
    本物の世代の目安：g線・i線のころ k1 0.6〜0.8 → KrF で ①②により 0.4 前後 → ArF 液浸で ①②③と多重露光で 0.3 前後
  - **今のゲームとの関係**：投影レンズの k1 の土台（ProjectionLensItem.k1Base）は g線・i線 0.6、KrF・ArF 0.35、液浸 0.3 にしてあり、KrF から先は「解像度を上げる工夫」を最初から入れた近似（⚠️）。CD はステッパー・スキャナーの lensCd() で計算し、A の ExposureTool.gridFor(CD) で方眼になる（A の Q20-1 の式）
  - **案A（おすすめ）：工夫を「物」として足す**。土台の k1 を全部 0.6 にそろえ、工夫ごとに k1 を下げる（下限 0.25）
    - 変形照明：ステッパー・スキャナーに「照明の絞り」の入口を足し、輪帯の絞り（−0.1）・四重極の絞り（−0.15）を入れる（真空蒸着装置か旋盤で作る金属の板）。C だけで作れる
    - 位相シフトマスク：縮小カメラでクロムのブランク＋「位相の膜（フッ酸で石英を削る or 薄い膜）」→ 位相シフトマスク（−0.15）。**A にお願いする所**：MaskData に「位相シフト」の印（分けたマスクの " [1/2]" と同じく design の末尾の印でもよい）と、exposeAtCd がそれを読むか、C がステッパー側で k1 を下げて CD を出すか（後者なら A の変更は印の読み方だけ）
    - OPC：計算機（幹10）ができてから（今回はしない）
    - 遊び：KrF の線を前と同じ細さにするには、絞りと位相シフトマスクを両方そろえる＝工夫を足すほど方眼が細かくなるのが見える。画面に「k1 0.60 − 絞り 0.15 − 位相 0.15 ＝ 0.30」と出す
  - **案B：レンズに工夫を付ける（作業台）**：投影レンズ＋輪帯の絞り → k1 の低いレンズ、のように道具で k1 を下げる。作りやすいが、本物（照明とマスクの工夫で、レンズそのものは同じ）とずれる
  - **案C：研究の点数**：テスト用の露光（干渉計・電子顕微鏡で線を見る）をくり返すと研究が進み、k1 が段階的に下がる。「研究要素」の言葉には近いが、何を作ったかが見えにくく、化学ルール1（本物の部品で）から遠い
  - **おすすめ：案A**（本物の順番どおり：変形照明 → 位相シフトマスク、OPC は計算機のあと）。今の KrF・ArF・液浸の k1 0.35/0.3 を 0.6 に戻すので、前のテスト（stepper_krf・stepper_arf_immersion・scanner・stepper_lele）の方眼の数は直す
- ✅ O-027（仮決定：O-026 の案A）：k1 の土台を全部 0.6 に（KrF・ArF は 0.35、液浸は 0.3 だった）。輪帯の絞り（−0.10）・四重極の絞り（−0.15）をステッパー・スキャナーの絞りの入口（SLOT_APERTURE、スキャナーのマスクの台は 8 番に）に、位相シフトマスク（クロムマスク＋フッ酸、別のアイテムなので A の MaskData は変えない＝ステッパー側で k1 を下げる形）。下限 0.25。ボタンを下（y=84）に動かし、その下に「k1 0.60-0.30=0.30」。ProjectionLensItem.designK1・cdNm(lens, 収差, 工夫)・meetsToolWithTricks。
  **方眼の数（前 → 後）**：stepper_krf：良いレンズだけで CD 118nm（方眼72）→ 196nm（方眼60）。四重極＋位相シフトマスクで約102nm（方眼72）＝テストの方眼68 が焼ける（絞りなしでは焼けない、を足した）／stepper_arf_immersion：乾 87nm 方眼76・水 47nm 方眼96 → 乾 144nm 方眼64・水 90nm 方眼76／scanner：CD 116→98nm・方眼72→76 → 173→154nm・方眼60→64／stepper_lele：レンズの方眼76・原図100 → レンズの方眼64・原図88。GameTest k1_tricks、CHEMISTRY O51〜52、REFERENCES O80
- ✅ O-028（光学の側）：ProjectionLensItem.dofNm（DOF＝k2·λ/NA²、k2 0.8⚠️、液浸は ×1.44）、ツールチップ「焦点深度」、ステッパー・スキャナーの左下に DOF（DATA_DOF）。g 2.85・i 0.60・KrF 0.31・ArF 0.21・液浸 0.12µm。GameTest depth_of_focus、CHEMISTRY O53、REFERENCES O81。
- ❓ O-028 遊び方の案（判断待ち。段差との判定は A の litho、A に相談中）：今の ChipState.steps()（CMP のあとに重ねた層の数）× 1段の高さ（A が決める）＝表面の段差。**案A（おすすめ）**：段差 ＞ DOF なら焼けない（exposeAtCd が "defocus"、画面は「段差が焦点深度より大きい：CMP で平らに」）。わかりやすく、CMP（E）の意味がはっきり出る。**案B**：焼けるが、はみ出した分だけ歩留まりが下がる（v0.5 #4、A の歩留まりと一緒に）。**案C**：はみ出すとぼけて CD が太くなる（方眼が粗くなる）。本物は B（ぼけた所が不良になる）に近いが、遊びは A がわかりやすい
- ⏸ O-029 1. [9-45] ベイヤー配列のカラーフィルター：前提 [9-42] 画像センサー（E）がまだ ❓（E の IMPL4_LOG 141〜145 行は案のまま、仮決定待ち）なので待つ。できたら、赤・緑・青のフィルター（色素は D に頼むか、[7-..] の干渉フィルターの膜で近似）を 2×2 の「緑2・赤1・青1」に並べる形で作る
- 📝 A の答え（O-028 の続き）：1段の高さ ChipOps.STEP_NM＝80nm⚠️、段差＝steps()×80。exposeAtCd(w, mask, tool, cd, overlay, dofNm) を足して 段差＞DOF なら "defocus"（今の5引数の exposeAtCd は焦点深度を見ない）。KrF（310nm）は段3まで、液浸（122nm）は段1まで。遊び方の仮決定が A の INBOX に来たら S-014 のあとで入れる。C はそのとき、ステッパー・スキャナーの呼び出しを6引数にしてヒント「段差が焦点深度より大きい：CMP で平らに」を足す
- ❓ O-029 2. スマホのカメラ [P-17] の光学（案だけ。モノ [P-] は products.md＝B の物）：
  - **本物**：厚さ数 mm に収めるため、焦点距離 約4mm・明るさ F2 ほどの小さなレンズ（プラスチックやガラスを型で押して作る非球面レンズを数枚）＋赤外線カットのフィルター（薄い膜の干渉フィルター）＋ベイヤーのカラーフィルター＋CMOS 画像センサー（1993〜、画素 約1µm）。画素がとても小さいので、レンズの回折（エアリーの円の直径 ≒ 2.44·λ·F 値、緑で F2 なら約2.7µm）が画素より大きくなる＝レンズを良くしても限りがある
  - **案A（おすすめ）**：カメラモジュール camera_module（アイテム）＝小さなレンズ（[7-03] のクラウンレンズを小さく磨いたもの）＋赤外線カットのフィルター（真空蒸着の膜、i線フィルターと同じ作り方）＋ベイヤーのフィルター [9-45]＋画像センサー [9-42]（E）。右クリックで、C が作った写真の仕組み（[B] の乾板カメラ・PhotoItem）を使って「すぐ見られる写真」を撮る＝乾板（現像・定着が要る、露光 1秒〜）とくらべて、その場でカラーの写真になる。画面に「F2・画素 1µm・回折の円 2.7µm」を出し、回折の限りを体験。スマホ [B-10] の部品にもなる
  - **案B**：部品だけ（組立台でスマホに組み込む）。作りやすいが、遊びが少ない
  - **案C**：ブロックのカメラ（ウェブカメラ）で、見ている方向を画面に絵として出す。体験は大きいが、描画（client）の手間が大きい
  - おすすめ：**案A**（今ある乾板カメラ・写真の仕組みとつながり、「化学の写真 → 電子の写真」の移り変わりが見える）。E の [9-42] の形（素子のアイテムか、ブロックか）が決まってから組み立て方を合わせる
- ✅ O-030（仮決定：O-028・O-029 の案A）：DECISIONS に2行。1 焦点深度：A に決定を知らせた（ChipOps.STEP_NM＝80、6引数の exposeAtCd で "defocus"、材料は失わない）。入ったらステッパー・スキャナーを6引数にしてヒントを足す。2 カメラ：組み立ては E の [9-42] 待ち。先に光学の部品だけ：camera_lens（クラウンレンズ＋ベンガラ → 4）、ir_cut_filter（蒸着：クラウンガラス＋蛍石×3）。GameTest camera_optics_parts、CHEMISTRY O54
- ✅ O-030 の続き：A が焦点深度の判定を入れた（ChipOps.STEP_NM＝80、6引数の exposeAtCd）。ステッパー・スキャナーを6引数（dofNm＝レンズの DOF）にして、画面のヒント defocus「段差が焦点深度より大きい：CMP で平らに」。GameTest stepper_defocus
- ✅ O-031（[9-45] の C の側）：red/green/blue_color_resist（ポジ型レジスト＋ベンガラ／酸化クロム／硫酸銅⚠️）、bayer_filter（作業台の 2×2 に 緑・赤／青・緑 → 4）。画像センサーへの組み立て（素子＋フィルター → 色の分かる素子）とカメラモジュールは E の image_sensor の ID が決まってから。GameTest bayer_filter、CHEMISTRY X5、REFERENCES O82。D へ：青の顔料（銅フタロシアニン）ができたら置き換えたい
- ✅ O-031 の続き（E の image_sensor ができた）：color_image_sensor（Semiconductor、E の ImageSensorItem で登録＝同じ表示、image_sensor＋bayer_filter）、camera_module（Branches、item/CameraModuleItem：小さなレンズ×2＋赤外線カット膜＋色の分かる画像センサー＋IC＋乾電池、右クリックで PhotoData の種類 digital の写真をすぐ、自動露出、F2・画素1µm・回折の円 2.7µm）。O-029・O-030 のカメラはこれで全部。GameTest camera_module、CHEMISTRY CAM1
- ✅ A の頼み（歩留まり v0.5 #4、A 7c135282）：マスクアライナー・ステッパー・スキャナーの仕上げで ChipOps.inRoom(ChipOps.addDust(w, CleanRoom.dustAt), CleanRoom.defectDensity) を呼ぶ（チップに「いちばん悪い部屋」の D が残り、ウェハープローバーで e^(−D·A) の確率で良品）。stepper_arf_immersion に D の確かめを足した
- ✅ O-033 [15-13] 光ファイバー（幹15 は担当がいないので C の光学の前提として）：fiber_preform（合成石英×4＋GeO2）・fiber_preform_natural（溶融石英×4＋GeO2）→酸水素バーナー → optical_fiber×8（item/OpticalFiberItem、custom_data に dB/km：20 と 1000）。ブロック2つで届く光の％、臨界角 85.3°。光源（[B-07] LED・[7-25] 半導体レーザー）はまだないので光源なし⚠️。GameTest optical_fiber、CHEMISTRY FO1、REFERENCES O83
- ✅ S-009（v1.0 の C の分）：1 光源・2 多層膜は S-007 で済み（06f1db37・c1955c42）。3 EUV 露光装置 [9-40]：block/EuvScannerBlockEntity（ScannerBlockEntity を継承、ステッパーに lightReady・maskOk の上書き口を足した）、client/EuvScannerScreen。euv_mirror_set（多層膜×6）、euv_projection_optics（心出し済みの鏡筒＋鏡の組、ProjectionLensItem(EUV, NA 0.33)）。隣の EUV 光源（EuvSourceBlockEntity.ready）・真空容器 1Pa、マスクは E の euv_mask（ID の名前で判定）。鏡10枚＋マスクで 1.7%、1チップ 0.2J。GameTest euv_scanner、CHEMISTRY X6、REFERENCES O84
- ✅ S-009 の続き：縮小カメラに E の euv_mask_blank の分岐（ポジ型レジスト＋塩素 Gas.CHLORINE_GAS で吸収体を削る → Semiconductor.EUV_MASK、MASK は A の reflectiveCopy で反射型）。euv_scanner の通しのテスト（EUV のレジスト ChipOps.RESIST_EUV で1チップ焼ける）。A の litho：EUV は反射型のマスクだけ、レジストは EUV か化学増幅
- ❓ O-034 v1.0「6. High-NA EUV（NA 0.55）」の案（作っていない。決まるまで待つ）
  - **本物（⚠️ 年は出典で要確認）**：NA を 0.33 → 0.55 に上げた EUV 露光装置（ASML の EXE 系。最初の装置の出荷は 2023年末ごろ⚠️、量産向けは 2025年ごろ⚠️）。NA が大きいと鏡の角度が大きくなり、マスクに当たる光の角度の都合で、縦と横で縮小の倍率がちがう「アナモルフィック」の投影光学（片方 1/4・もう片方 1/8）にした。そのため1回に焼ける範囲（ショット）が今の半分（26×16.5mm）になり、大きなチップは2回に分けてつなぐ（スティッチング）。焦点深度は NA の2乗で浅くなる（k2·λ/NA² で約 36nm）。線は約 8nm の半ピッチまで
  - **今のゲームとの関係**：EUV の投影光学（NA 0.33、k1 0.6）で CD 26nm・方眼116。焦点深度 99nm（段1 まで）。A の方眼の式 gridFor(CD) と BitGrid の上限 148
  - **案A（おすすめ）：High-NA の投影光学を「部品」として足す**。EUV の鏡の組＋心出し済みの鏡筒（より精密：心出しのずれが小さくないと作れない）→ High-NA の投影光学（NA 0.55、アナモルフィック）。EUV 露光装置のレンズの入口に入れると：
    - CD が細かくなる（k1 0.6 で 約15nm）
    - アナモルフィックで1ショットが半分＝1チップの送りが2倍（STEPS 4 → 8）＝時間が2倍（光源の強さがもっと大事に）
    - 焦点深度 約36nm＝段差が1段（80nm）でも焼けない＝毎層 CMP が要る
    - **A にお願いする所**：方眼の上限（BitGrid.MAX＝148）を超える細かさになるので、High-NA で 148 を超えた分をどう扱うか（上限を上げる／148 で止める）
  - **案B：NA だけ上げる**（今の投影光学の NA を 0.55 にした別の部品、アナモルフィック・半分のショットなし）。作りやすいが、High-NA ならではの「半分のショット・つなぎ」の苦労が出ない
  - **案C：マルチブロックの High-NA 装置**（v1.0 #1 の A のマルチブロックの土台ができてから）。本物の大きさ（約 150 トン、2階建て）が見えるが、土台待ち
  - **おすすめ：案A**（光学の部品として作れて、細かさ・時間・焦点深度の3つの代償が体験できる）。存命の人の名前は出さない
- ✅ D の銅フタロシアニン（c257779f）で blue_color_resist のレシピを置き換えた（硫酸銅 → copper_phthalocyanine）。lang の note・placeholder、bayer_filter の placeholder、GameTest bayer_filter を直した。CHEMISTRY X7
- ✅ O-035（仮決定：O-034 の案A）：euv_high_na_optics（EUV の投影光学＋鏡の組、ProjectionLensItem(EUV, NA 0.55)）。EuvScannerBlockEntity：isHighNa・opticsUsable（心出しのずれ合計 1.6µm 以下）・steps()＝8（ステッパーに steps() の上書き口を足した）。焦点深度 36nm は今の式のまま。CD 16nm・方眼136。方眼の上限 148 は今のまま（A に確かめた）。DECISIONS に1行。GameTest euv_high_na、CHEMISTRY X8、REFERENCES O85
- ✅ S-010 1. [8-19] 滑車：simple_pulley・compound_pulley（item/PulleyItem）。ブロックを持ち上げる力と綱の長さ、人の力と比べる。GameTest pulley、CHEMISTRY P24、REFERENCES O86
- ✅ S-010 2. [14-13] ジュラルミン：duralumin_charge →かまど → quenched_duralumin（item/QuenchedDuraluminItem：持ち物で1日たつと duralumin に、右クリックで硬さ）。GameTest duralumin、CHEMISTRY T2、REFERENCES O87
- ✅ S-010 3. [15-09] 真空管ラジオ：tube_radio（組立台、item/TubeRadioItem：聞くたびに鉛蓄電池から 400J）。GameTest tube_radio、CHEMISTRY CO1、REFERENCES O88
- ✅ S-010 4. [15-04] 電話：telephone（item/TelephoneItem、両手で結ぶ、持ってチャット → ServerMessageEvents.CHAT_MESSAGE で同じ回線へ）。GameTest telephone、CHEMISTRY CO2、REFERENCES O89
- ✅ S-011 機械式腕時計：ノード [8-43] 輪列・[8-44] ぜんまい・[8-45] てんぷとひげぜんまい・[8-46] レバー脱進機・[8-47] 宝石の軸受・[8-48] 自動巻き を TECH_TREE に足して実装（番号は B に相談中、仮）。製品 [P-38] 機械式腕時計（組立台、item/MechanicalWatchItem：巻く・チクタク・2日で止まる・±10秒/日、ChronometerItem を継承）、自動巻きの腕時計。ラジウム・ガールズは note4・REFERENCES に教訓として（作り方は入れない）。エリンバー・振り子時計・知恵のノード（ガリレオ・フック）は B に頼んだ。GameTest mechanical_watch、CHEMISTRY P25、REFERENCES O90
- ✅ S-010 5. [15-05] 蓄音機：tin_foil_cylinder・phonograph（item/PhonographItem、チャットで録音・右クリックで再生・3回で消える）。GameTest phonograph、CHEMISTRY CO3、REFERENCES O91
- ✅ O-034 C の分（Q33-1 案A・Q33-3）：EUV 露光装置をマルチブロックに（A の machine/Multiblock）。5×3×3、部品ブロック euv_casing・euv_illuminator・euv_reticle_stage・euv_projection_box・euv_wafer_stage（＋既存の EUV 光源・真空容器）、クラス100 の部屋の中でだけ組み上がる（部屋は制御ブロックの正面で調べる＝StepperBlockEntity.roomPos）。画面のヒントに「形ができていない」「クラス100の部屋が要る」。GameTest euv_scanner を形を組む試験に、exportMultiblocks に1行。CHEMISTRY X9。⚠️ Q33-4＝b（組み上がると表の模様がつながる絵）はまだ：A の土台に見た目の仕組みが入ったら合わせる。High-NA の大きい形もまだ。材料（ステンレス・セラミックス板・O リング）は D に頼んだ
- ✅ O-036（わかりやすさの穴、C の分）：①入手の道 30→0（機械のコードが作る22品と希ガスのボンベ3つを check_howto.py の EXTRA_WAYS に、手で作る5品＝心出しの鏡筒・定盤・ジュラルミン・露光した板2つを HOWTO_EXTRA に）。②「?」の説明 17→0（自分の機械 17 台の .howto を ja/en で）。残りの 13 品・8 台は E の分
- ✅ O-034 Q33-4＝b：EUV の部品5つを machine/MultiblockPartBlock（A の Multiblock.FORMED を持つ）に。組み上がると全部が同じ白い板＋横に通る紺の帯の絵（euv_formed_side/top）になり、帯がつながって1台の大きな機械に見える。GameTest euv_scanner で囲いが formed=true になるのを確かめる
- ⏸ O-037 [8-34] NC 工作機械：保留。前提 [10-04] フィードバック制御（A の幹10）がまだ ❓ なので、CLAUDE.md の決まりどおり実装していない。質問：①A が先に [10-04] を作るか、②[10-04] を前提から外して（サーボの位置のフィードバックはノードの中で表す）C が作るか。
  - 遊び方の案（決まったらすぐ作れるように）：**案A（おすすめ）** NC 旋盤 nc_lathe：①手の旋盤で削った部品をマイクロメータで測ると、寸法が「穴あきテープ」（紙＋その寸法）に記録される（プログラムを作る）。②NC 旋盤にテープとボールねじを入れると、鋼が来るたびに同じ部品を自動でくり返し削る。誤差は旋盤の「くせ」±18µm でなく、ボールねじの送り＋サーボの補正で ±2µm、しかも全部がそろう。③ホッパー・ベルトコンベア（E）から鋼を入れ、出口から出す（Transfer API、レッドストーンなし）。テープは何度でも使えるが、長く使うと穴が傷んで読み違える（⚠️ ゲームの値）。**案B** テープを画面のボタンで寸法を打ち込んで作る（手順を自分で書く遊び）。案A は「今ある精密の道具の続き」で覚えることが少ない
- ✅ S-010 6. [15-12] 磁気記録：steel_wire_spool・magnetic_tape・telegraphone（item/TelegraphoneItem、チャットで記録・右クリックで再生・消えない・しゃがんで消す）。GameTest magnetic_recording、CHEMISTRY CO4、REFERENCES O92
- ⏸ O-037 続き：A が [10-04] を ✅ に（ab8fff85、サーボはノードの中で表してよい）。前提の問題は解消。残りは遊び方（上の 案A／案B）の判断だけ。おすすめは 案A
