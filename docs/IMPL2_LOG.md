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
