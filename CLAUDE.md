# Semiconductor Lab — Minecraft 26.3 / Fabric / Java 25

本物の半導体製造を遊びながら学ぶ教育MOD。1947年のトランジスタから EUV までを歴史順に実装する。
作者は Java 初心者。説明は日本語で、専門用語には短い補足を付ける。

## 最初に必ず読むファイル
- docs/README.md … docs の入口（各ファイルの役割と読む順番）
- docs/HANDOFF.md … 現在地1枚。**各タスクの最後に HANDOFF.md を更新する**（数字・今すぐ作れる・仮・未決・次の一手）
- docs/REFERENCES.md … 参考文献のまとめ（答え→へえ①〜④→確かさ→根拠）
- docs/ROADMAP.md … 全体計画と「今どこか」。各ステップのチェックボックスを更新する
- docs/DESIGN.md … 決定事項・化学ルール・データモデル・26.x の変更点メモ
- docs/CHEMISTRY.md … 全レシピの化学的根拠と「省略」ラベル。レシピを足したら必ず行を追加（**書くのは docs/chemistry/<幹>.md**。CHEMISTRY.md は組み立て結果、IMP-32）
- docs/API_26X.md … javap で確認済みの 26.3 API の形。ここにある形を使う
- docs/TEST_QUEUE.md … ゲーム内テスト待ち行列。実装後に項目を追記（**書くのは docs/test_queue/<幹>.md**。TEST_QUEUE.md は組み立て結果、IMP-32）
- docs/SOURCES.md … 出典と要確認リスト
- docs/TECH_TREE.md … 依存関係の技術ツリー。新しい機械・材料を足すときは対応ノードを追記し、前提が未実装なら「仮」にせずユーザーに相談

## 絶対ルール
1. 化学的整合性が最優先。バニラ素材で別の物を代用しない。未実装は「仮」として
   ゲーム内ツールチップ（lang の *.placeholder キー）と CHEMISTRY.md に明記する
2. 26.x の API は推測で書かない。.gradle/loom-cache 内の minecraft-common / clientOnly jar を
   `javap -cp <jar> <クラス>` で確認してから書き、結果を docs/API_26X.md に追記する
   （GuiGraphics は廃止→GuiGraphicsExtractor、Block の CODEC 廃止、configured_feature→feature など既知の変更あり）
3. 変更後は `./gradlew.bat compileJava compileClientJava` で必ずコンパイル確認。runClient は実行しない（ユーザーが遠隔作業中のため）
   機械の動作・レシピ・ルートテーブル・worldgen など「データやロジック」に関わる変更なら `tools/test.ps1`（GameTest。画面なしの専用サーバー、約20秒）も実行し、
   全部 PASSED を確認する。新しい機械や挙動を足したら src/gametest/java/.../<幹>Tests.java（MetallurgyTests など、共通の道具は LabTestBase）にテストを足す
   **画面（MachineLayout の文字・スロット）・lang の画面の文・ツールチップを変えたら、必ず test.ps1 で画面の検査（GameTest gui_layout・tooltip_text、ゲームなし）を通す**。
   画面の文字は MachineLayout にデータで書き、画面のコードでは直接描かない（DESIGN「画面の文字と自動チェック」）。設計図は docs/gui_preview/
4. ゲーム内で確認が必要な項目は docs/test_queue/<幹>.md に追記する（TEST_QUEUE.md に組み立てられる）
   **部品ファイルの決まり（全担当共通、IMP-32）**：追記の多い docs は幹ごとの部品に書き、スクリプトで1つに組み立てる。組み立て結果は手で直さない
   - docs/chemistry/<幹>.md → docs/CHEMISTRY.md、docs/test_queue/<幹>.md → docs/TEST_QUEUE.md（`python tools/docs_build.py`）
   - src/main/lang/<幹>/ → assets/.../lang/*.json（`python tools/lang_build.py`、`tools/lang_add.py` で足す）
   - <幹>＝ノード番号の頭：0_survival・1_metallurgy・2_chemistry・3_heat・4_power・5_vacuum・6_gas・7_optics・8_precision・9_semiconductor・10_control・11_agriculture・12_construction・13_medicine・14_transport・15_communication・B_branches。ノードでない節は general.md
   - 節は「## [幹-番号] 題（担当）」で、その幹の部品の末尾に足す。test.ps1 が最初に --check で組み立て結果と部品が同じかを確かめる。組み立て結果がぶつかったら、スクリプトを実行し直せばよい
5. 1ステップごとに `git add -A; git commit -m "..."`。push もする
6. 新しいバニラ要素（26.2 の硫黄など）をレシピに使う前に、名前の実在をユーザーに確認する
7. 既存の構造を踏襲する（IMP-28 から**幹ごとに分ける**）：アイテム・ブロック・機械（Machine 1行）の登録は content/<幹>.java
   （Survival＝幹0、Metallurgy＝1、Chemistry＝2、Heat＝3、Power＝4、Vacuum＝5、Gas＝6、Optics＝7、Precision＝8、Semiconductor＝9、
   Communication＝15、Branches＝枝🌿。まだない幹は最初のノードのときに作り content/Content.java に1行）、lang は src/main/lang/<幹>/、
   テストは gametest/<幹>Tests.java。ノードの幹＝番号の頭（[9-08] なら Semiconductor）。1ノードで触るのはその幹のファイルだけ。
   機械は block/、電気は energy/、機械の共通部分は machine/、クライアント描画は src/client

## 担当の分け方（2026-10-20〜）
- **共通ルール：タスク番号**は、実装担当（このフォルダ）＝ **IMP-番号**、ネットワーク担当（semilab-network）＝ **NET-番号**。commit メッセージ・LOG・Discord の通知・報告の最初の行に付ける。これまでのタスク22・23 は IMP-22・IMP-23 と読む
- ネットワーク担当の進み具合は docs/NETWORK_LOG.md（ネットワーク担当が書く）
- **このフォルダ（IMP）の指示の受け取り方（2026-10-20〜、INBOX）**：指示は、このフォルダの INBOX.local.md（git に入らない、.git/info/exclude）に設計担当（claude.ai のチャット）が直接書く
  - 作業の区切りがつくたびに INBOX.local.md を読む。状態が「新しい」の項目を上から実行し、始めるときに「実行中」、終わったら「完了（commit ハッシュ）」に書き換える
  - 新しい項目が無ければ止まらずに、Monitor で INBOX.local.md の変化を待つ（更新時刻か「状態: 新しい」を数分おきに確かめる until ループ）。変わったら読んで実行。6時間変化が無ければ docs/tasks/LOG.md に「待機終了」と書いて止まる（2026-10-20 ユーザー：2時間→6時間）
  - 報告は今までどおり docs/tasks/LOG.md と Discord に（ユーザーへのコピペは要らない）
  - INBOX の内容がこれまでの指示や DESIGN とぶつかるときは実行せず、LOG と Discord で質問して INBOX の変化を待つ
  - ユーザーが決める物（デザインの選択など）は、INBOX かユーザー本人の決定が来るまで適用しない
  - 止まらずに進める：許可の確認が出そうな操作（.claude/settings.json の許可に無いコマンド）は避けて、許可されている方法でやる。どうしても必要な件・判断が要る件は LOG と Discord に書いて置いておき、次の項目へ進む（確認を出したまま待たない）
- **危ない技術（武器・爆発物・毒・事故・公害）の扱いは DESIGN「危ない技術の扱い」の節に従う（全担当共通）**
- **会社・仕組みの扱いは DESIGN「会社・仕組みの扱い」の節に従う（全担当共通）**
- docs/network.html・docs/node.html・tools/gen_tree_views.py・docs/network_meta.json・products.md・ideas.md・tools/gen_portraits.py は、別フォルダ C:/Users/taku3/mods/semilab-network（ネットワーク担当）の担当。このフォルダ（A＝MOD 本体）ではこれらを触らない
- A が TECH_TREE.md・REFERENCES.md・lang・machine_recipes.json（GameTest が書き出す）を変えると、ネットワーク担当がそれを読んで図を作り直す。図の作り直し（gen_tree_views.py の実行）もネットワーク担当

## コードの慣習
- Mojang 公式マッピング（Yarn ではない）。Identifier.fromNamespaceAndPath / ResourceKey / setId(key) の登録パターン
- lang は en_us と ja_jp の両方を必ず更新。書くのは部品 src/main/lang/<部品>/en_us.json・ja_jp.json（tools/lang_add.py が ID から幹の部品へ自動で振り分ける）。ゲームが読む assets/.../lang/*.json は tools/lang_build.py が作る（手で直さない、test.ps1 が --check で確かめる）。解説は item/block.<id>.note1（子ども・通常表示）.note2（学生・Shift）.note3（大人・ガイド）.note4（ガチ・深掘り）。旧 .note は .note2 扱い。仮は .placeholder
- 仮テクスチャは 16x16 PNG を自動生成でよい（System.Drawing）。本番差し替えは仕上げフェーズ
- 機械は ZoneRefiningFurnace（BE/Menu/Screen）を参考に。発電機は energy/GeneratorBlockEntity を継承

## TECH_TREE 駆動（最優先の進め方）
- docs/TECH_TREE.md のノードが「作業単位」。指示が「[X-NN] を実装して」なら、そのノードの
  前提/本物/入力出力/ゲーム/省略/へえ/テスト の各行をそのまま仕様として使う
- 前提ノードに ❓ や ⛔ が残っていたら実装せず、「前提 [..] が未実装」とユーザーに報告する
- 実装後：ノードの状態を ✅ に、docs/chemistry/<幹>.md に本物/省略の行、docs/test_queue/<幹>.md にテスト行（どちらも python tools/docs_build.py で組み立て）、
  lang の .note に「へえ」、ROADMAP のチェックを更新。commit メッセージに [X-NN] を入れる
- ノードを実装したら docs/REFERENCES.md に同じ形式（答え→へえ①〜④→確かさ◎○△→根拠リンク）で1項目を足す（既にあれば更新）。△ の内容はゲーム内（lang の note）でも ⚠️ を付ける
- ユーザーから指示されたタスクが終わったら、毎回 Discord の Webhook に完了通知を送る（大きな見出し「# ✅🔔 作業完了 🔔✅」・色付きの埋め込み（embeds、成功＝緑 3066993）・読み上げ（"tts": true）。テストが失敗して止まったときも「# ❌ 停止」・赤 15158332 で送る）。@everyone は付けない。URL はリポジトリ直下の .discord_webhook（git に入らない、.gitignore 済み）から読む。URL はこのファイルにもチャットにも書かない。JSON は Write でファイルにしてから `python tools/discord_notify.py <JSON>` で送る（.claude/settings.json で聞かずに実行できる。curl は毎回聞かれる）
- 「次に作れるノードを列挙して」と言われたら、前提が全部 ✅ の ❓ ノードを幹ごとに一覧にする

## 現在地
ツリー骨格完成（346ノード、背骨切れ0）。実装モード再開：TECH_TREE の『今すぐ作れる』（docs/TECH_TREE_VIEW.md §1）から背骨優先。
v0.2a 電気：#1 ケーブル ✅ #2 鉱石4つ ✅ #3 発電機 ✅ #4 蒸気機関GUI ✅ #5 電解槽[6-01] ✅ #5b ガスホルダー[6-02] ✅ #5c 鋼[1-26] ✅ #5d 圧縮機[6-03] ✅、耐火れんが[3-12]・鉛[1-27]・W線[1-28]（水素炉）✅ #7 真空→真空管→高周波発振器（[5-01]手押しポンプ・[5-11]真空容器・[1-45]水銀・[5-10]水銀真空計・[5-02]三極真空管/封止台・[4-09]高周波発振器）✅ → 第1章の仮①（hf_power_supply）は消えた。#8 [4-12]電気炉・[3-10]アチソン炉（500kW、温度モデル）→人造黒鉛ボート（10N）✅、#9 帯域精製炉の電力駆動（3kW、ボート専用スロット）✅ → 第1章の仮②（粘土黒鉛ボート）も消えた。#10 タスク04：[4-05]自励式ダイナモ（積み替えで2倍）・[4-06]蒸気タービン（500kW/30%、SteamEngineBlockEntity を継承、GUI 共通）・[4-07]鉛蓄電池（1MJ、正面放電）✅。#11 タスク06：電気をネットワーク方式に（energy/CableNetwork：ケーブルは貯めない導体、最長経路×1%、網あたり 1MW で按分、発電→機械＞電池放電＞電池充電）、鉛蓄電池は全面充放電＋残量をデータコンポーネント battery_energy に保存 ✅。#12 タスク07：[0-10]生石灰・[0-11]消石灰（耐火れんがの結合材は石灰に）、[3-06]反射炉＝焙焼炉（block/ReverberatoryFurnaceBlockEntity：燃料式、鉱石→酸化物/金属＋SO2、閃亜鉛鉱は Ge精鉱/In を副産物スロットへ）✅。#13 タスク08：[7-02]酸水素バーナー（block/OxyhydrogenBurnerBlockEntity：H2/O2 ボンベ式、電気なし）→溶融石英の仮が消えた、[2-03]トロナ堆積（砂漠のみ）→炭酸ナトリウム、[2-14]ソーダ石灰ガラス（真空容器・真空計・ライデン瓶は MOD ガラス必須）✅。#14 タスク09：[6-04]岩塩鉱・[0-16]塩田（塩＋にがり）、[6-05]クロルアルカリ（電解槽の塩水モード、塩素はホルダー/圧縮機/黄ボンベ対応）、[6-06]塩酸合成炉（block/HclBurnerBlockEntity）、WO3 本物化（ソーダ融解→塩酸）、[1-34]GeCl4 蒸留（石英の蒸留器＝作業台で戻る道具、データコンポーネント chloride_route：帯域精製の上限は GeCl4 経由 10N／硫酸経由 9N）✅。#15 タスク10：v0.2a の締め（化学レビュー、ガイド第2巻「電気」guide2/、全アイテムの へえ①〜④＋図鑑タブ zukan/ は tools/gen_zukan.py で生成）→ **tag v0.2a** ✅。#16 タスク12：判断を docs/DECISIONS.md へ一括採用、[5-15] シュプレンゲル水銀ポンプ（block/SprengelPumpBlockEntity、封止 0.1Pa）・[6-07] 空気分離（block/AirSeparationBlockEntity、GasType に N2/Ar、ガスを出す機械は block/GasSource）・[1-33] ホウ素（ホウ砂→ホウ酸→B2O3）・[3-14] 誘導加熱炉＝帯域精製炉 ✅。[1-16] トリクロロシラン（block/TrichlorosilaneReactorBlockEntity、合成＋蒸留）・[1-17] シーメンス炉（block/SiemensReactorBlockEntity）✅。[9-11] ドーパント源（Dopant、B・P のみ 🔧）・[9-08] CZ 引き上げ機（block/CzPullerBlockEntity）✅。[9-12] ウェハー切断機・[9-16] 拡散炉・[B-12] 太陽電池（energy/SolarCellBlockEntity、60W）✅（IMP-25、1954年の作り方）。v0.2b のゴールに到達。次は v0.2b の締め。解放済み：[8-01]旋盤・[4-10]変圧器・[14-14]電気鉄道・[6-07]空気分離・[1-32]銀・[3-13]断熱材・[3-14]誘導加熱炉・[4-11]整流器・[0-03]土器・[0-12]木炭・[2-02]カリ・[2-04]海藻灰ソーダ・[3-01]たき火・[12-01]ローマンコンクリート・[5-03]水銀柱・[5-15]シュプレンゲルポンプ・[13-13]体温計・[11-11]塩蔵。
真空容器の圧力は VacuumChamberBlockEntity（findAdjacent で隣を探す）。真空管の封止圧は SealingStationBlockEntity.SEAL_MAX_PA（0.1Pa＝軟真空。手押しは 1kPa、シュプレンゲルは 0.05Pa まで）。
電気を使う機械は energy/ConsumerBlockEntity を継承（電解槽が見本。温度など独自の tick は serverTick を上書き＝アチソン炉、GUI 数値の追加は extraData）。機械ブロックの JSON 一式は tools/new_machine.py。新しい機械の手順は DESIGN「機械の共通化」：BE に LAYOUT（スロットの並び）を書き、`Machine.electric(...)` の1行で登録（ブロック machine/MachineBlock＝向き＋LIT＋GUI、画面は書かなければ SimpleMachineScreen）。隣のガスホルダーからガスをもらうのは GasHolderBlockEntity.extract()。
夜間バッチの保留判断は docs/tasks/DECISIONS_PENDING.md、失敗記録は docs/tasks/FAILED.md。lang の追加は tools/lang_add.py、アイテム・ブロックを足したら python tools/gen_tab_order.py（専用タブ「Semiconductor Lab」の並び＝幹の順→年の順。忘れると GameTest creative_tab が落ちる、IMP-31）、機械やアイテムを足したら python tools/gen_testworld.py（試験場の関数 /function semiconductor-lab:testworld を作り直す。docs/TESTWORLD.md、S-001）、仮テクスチャは tools/gen_png.py・GUI 背景は tools/gen_gui.py。
作業単位は TECH_TREE.md のノード。ROADMAP.md はノードの順番表。設計の判断に迷ったら実装せずユーザーに質問する。