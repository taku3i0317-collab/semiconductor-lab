# 05：v0.2a の締め（化学レビュー・ガイド・へえ肉付け・tag）
## 1. 化学レビュー：CHEMISTRY.md の v0.2a 全行（#24〜）を点検し、⚠️省略の記述が揃っているか、反応式の誤りがないかを確認。疑問点は DECISIONS_PENDING へ
## 2. ガイド「電気」章：第1章ガイドブックと同じ方式（mcfunction+翻訳）で第2の本「電気」（8〜10ページ：水車→蒸気→ダイナモ→ケーブル→電解→真空管→電気炉→黒鉛）。ガイドタブに guide2/ を追加し、各機械の進捗で解放
## 3. へえ②〜④の肉付け：v0.1〜v0.2a で実装済みの全アイテム・ブロック（約60）に .note1〜.note4 を lang（en/ja）に追加。NoteTooltip が4段階を正しく表示（note1常時、note2 Shift、note3/4 は「🔒…」と冒頭だけ表示しガイドタブで全文）。TECH_TREE の該当ノードにも へえ①〜④ を転記
## 4. ROADMAP の v0.2a を完了に、v0.2b の冒頭を「今すぐ作れる」ノードで更新。gen_tree_views 再実行。git tag v0.2a。./gradlew.bat build で jar を作り build/libs のファイル名を LOG に
## 共通ルール（夜間バッチ・無人）
- 判断に迷っても止まらない。docs/tasks/DECISIONS_PENDING.md に「質問・自分の仮決定・理由」を追記し、保守的な既定（仮ラベル .placeholder／近似の明記）で進む
- 各ノードに GameTest を最低1本。最後に powershell -ExecutionPolicy Bypass -File tools\test.ps1 を実行。失敗なら原因を docs/tasks/FAILED.md に書き、直せるなら直す。2回失敗したらそのノードは revert して次へ
- test.ps1 が通った状態でのみ commit & push。ノードごとに commit
- 終わったら TECH_TREE ノード✅、CHEMISTRY/TEST_QUEUE/API_26X/ROADMAP 更新、gen_tree_views.py 再実行
- 最後に docs/tasks/LOG.md に「タスク名／作ったもの／テスト結果／解放されたノード／保留した判断」を15行以内で追記