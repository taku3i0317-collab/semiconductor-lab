# 11：引き継ぎ用ドキュメント整備（コードは触らない。docs/ と CLAUDE.md のみ）
目的：このプロジェクトを初めて見るAI（claude.ai の Project）や人が、docs だけで現状と運用を5分で把握できるようにする。

## 1. docs/README.md（入口）
各ファイルの役割を1行ずつ、推奨の読む順番（HANDOFF→DESIGN→TECH_TREE_VIEW→ROADMAP→CHEMISTRY→…）、「質問別にどこを見るか」表（例：なぜこの材料？→CHEMISTRY／次に何を作る？→HANDOFF／APIの形→API_26X）

## 2. docs/HANDOFF.md（現在地1枚・毎バッチ末尾で更新する運用にする）
- 3行の現状／MODの概要（1段落）／完成した章と tag／GameTest 本数／ツリー進捗%（gen_tree_views の数字）／「今すぐ作れる」上位10／残っている「仮」一覧（.placeholder を全検索して列挙）／未決の判断（DECISIONS_PENDING から未採用のみ）／直近3タスクの要約／次の一手（推奨順）／費用実績（LOG から集計）
- agent_queue.py の各タスク末尾に「HANDOFF.md を更新する」を共通ルールとして CLAUDE.md に追記

## 3. docs/WORKFLOW.md（運用）
- 3者の役割（ユーザー＝判断／設計チャット＝科学レビューとタスク作成／Claude Code・Agent SDK＝実装）
- ツール一覧：agent.py（対話）、agent_queue.py（キュー実行、予算上限）、デスクトップの .bat 2つ、tools/test.ps1、tools/gen_tree_views.py、tools/new_machine.py、lang_add.py、gen_png.py、gen_gui.py。それぞれ「何をすると何が起きるか」
- タスクの書き方（docs/tasks/next.md または queue/NN_*.md、共通ルール、報告の形）、再開のしかた（途中で止まったら）、衝突ルール（バッチ中は git/runClient を触らない）、費用の目安（実測：機械1台 $1〜2、4ノード $4〜6）
- 人間テストのやり方（runClient、TEST_QUEUE の「人間が見る」項目）

## 4. docs/GLOSSARY.md（用語集）
仮（.placeholder）／近似／省略の違いと使い分け、✅🔧❓⛔🌿、ノードID [幹-番号]、背骨と枝、へえ①〜④と lang キー、J/W/kW と 1W=0.05J/tick、純度 N 表記、データコンポーネント、BE/Menu/Screen、GameTest、ConsumerBlockEntity/GeneratorBlockEntity、ガスホルダー mol、化学ルール1〜7（DESIGN から転記）

## 5. DESIGN.md の再構成 ＋ docs/DECISIONS.md 新設
- DESIGN.md を章立てに整理：1 コンセプト／2 基本情報／3 化学ルール／4 技術ツリーの範囲と進め方／5 データモデル／6 説明4段階と遊びの要素・報酬ルール／7 電気の設計／8 26.x で変わった点／9 変更履歴。内容は削らず移動のみ。古い「未決定」は DECISIONS_PENDING と突き合わせ、解決済みなら消す
- docs/DECISIONS.md：DECISIONS_PENDING で「採用」になった項目を「日付／ノード／決定／理由」の表に移す。PENDING には未採用だけ残す
- CHEMISTRY.md の冒頭に「ノードID→行番号」の索引を追加

## 6. CLAUDE.md
必読リストを README.md と HANDOFF.md 先頭に変更。「各タスクの最後に HANDOFF.md を更新」を共通ルールに追加。現在地の記述は HANDOFF.md を参照する形に短縮

commit & push。LOG.md に追記。報告：作成・変更したファイル一覧と、HANDOFF.md の先頭20行。