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
