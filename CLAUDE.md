# Semiconductor Lab — Minecraft 26.3 / Fabric / Java 25

本物の半導体製造を遊びながら学ぶ教育MOD。1947年のトランジスタから EUV までを歴史順に実装する。
作者は Java 初心者。説明は日本語で、専門用語には短い補足を付ける。

## 最初に必ず読むファイル
- docs/ROADMAP.md … 全体計画と「今どこか」。各ステップのチェックボックスを更新する
- docs/DESIGN.md … 決定事項・化学ルール・データモデル・26.x の変更点メモ
- docs/CHEMISTRY.md … 全レシピの化学的根拠と「省略」ラベル。レシピを足したら必ず行を追加
- docs/API_26X.md … javap で確認済みの 26.3 API の形。ここにある形を使う
- docs/TEST_QUEUE.md … ゲーム内テスト待ち行列。実装後に項目を追記
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
   全部 PASSED を確認する。新しい機械や挙動を足したら src/gametest/java/.../LabTests.java にテストを足す
4. ゲーム内で確認が必要な項目は docs/TEST_QUEUE.md に追記する
5. 1ステップごとに `git add -A; git commit -m "..."`。push もする
6. 新しいバニラ要素（26.2 の硫黄など）をレシピに使う前に、名前の実在をユーザーに確認する
7. 既存の構造を踏襲する：アイテムは ModItems、ブロックは ModBlocks、BE は ModBlockEntities、
   機械は block/ パッケージ、電気は energy/ パッケージ、クライアント描画は src/client

## コードの慣習
- Mojang 公式マッピング（Yarn ではない）。Identifier.fromNamespaceAndPath / ResourceKey / setId(key) の登録パターン
- lang は en_us.json と ja_jp.json の両方を必ず更新。解説は item/block.<id>.note1（子ども・通常表示）.note2（学生・Shift）.note3（大人・ガイド）.note4（ガチ・深掘り）。旧 .note は .note2 扱い。仮は .placeholder
- 仮テクスチャは 16x16 PNG を自動生成でよい（System.Drawing）。本番差し替えは仕上げフェーズ
- 機械は ZoneRefiningFurnace（BE/Menu/Screen）を参考に。発電機は energy/GeneratorBlockEntity を継承

## TECH_TREE 駆動（最優先の進め方）
- docs/TECH_TREE.md のノードが「作業単位」。指示が「[X-NN] を実装して」なら、そのノードの
  前提/本物/入力出力/ゲーム/省略/へえ/テスト の各行をそのまま仕様として使う
- 前提ノードに ❓ や ⛔ が残っていたら実装せず、「前提 [..] が未実装」とユーザーに報告する
- 実装後：ノードの状態を ✅ に、CHEMISTRY.md に本物/省略の行、TEST_QUEUE.md にテスト行、
  lang の .note に「へえ」、ROADMAP のチェックを更新。commit メッセージに [X-NN] を入れる
- 「次に作れるノードを列挙して」と言われたら、前提が全部 ✅ の ❓ ノードを幹ごとに一覧にする

## 現在地
ツリー骨格完成（346ノード、背骨切れ0）。実装モード再開：TECH_TREE の『今すぐ作れる』（docs/TECH_TREE_VIEW.md §1）から背骨優先。
v0.2a 電気：#1 ケーブル ✅ #2 鉱石4つ ✅ #3 発電機 ✅ #4 蒸気機関GUI ✅ #5 電解槽[6-01] ✅ #5b ガスホルダー[6-02] ✅ #5c 鋼[1-26] ✅ → 次は [6-03]圧縮機（ほか [8-01]旋盤・[4-05]自励式発電機が解放済み）。
電気を使う機械は energy/ConsumerBlockEntity を継承（電解槽が見本）。
作業単位は TECH_TREE.md のノード。ROADMAP.md はノードの順番表。設計の判断に迷ったら実装せずユーザーに質問する。