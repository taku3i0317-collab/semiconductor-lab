# 10：v0.2a の締め（化学レビュー・ガイド「電気」・へえ肉付け・tag）
## 1. 化学レビュー：CHEMISTRY.md の v0.2a 全行（#24〜）を点検。反応式の誤り、⚠️省略の記述漏れ、仮の残りを一覧化して DECISIONS_PENDING へ
## 2. ガイド「電気」：第1章と同じ方式（mcfunction＋翻訳）で第2の本（8〜10ページ：水車→蒸気→ダイナモ→ケーブル→電解→真空管→電気炉→黒鉛→塩素）。ガイドタブ guide2/ を追加し各機械の進捗で解放
## 3. へえ②〜④の肉付け：v0.1〜v0.2a の実装済み全アイテム・ブロックに .note1〜.note4（en/ja）。NoteTooltip：note1 常時、note2 Shift、note3/4 は「🔒…」で冒頭のみ、ガイドタブで全文。TECH_TREE 該当ノードに転記
## 4. ROADMAP の v0.2a を完了に、v0.2b 冒頭を「今すぐ作れる」で更新。git tag v0.2a。./gradlew.bat build で jar を作り build/libs のファイル名を LOG に
## 共通ルール（無人バッチ）
- 判断に迷っても止まらない。DECISIONS_PENDING.md に「質問・仮決定・理由」を追記し、保守的な既定（.placeholder／近似明記）で進む
- 各ノードに GameTest 最低1本。最後に powershell -ExecutionPolicy Bypass -File tools\test.ps1。失敗は FAILED.md に書き、2回失敗なら revert して次へ
- test.ps1 が通った状態でのみ commit & push（ノードごと）。TECH_TREE ✅、CHEMISTRY/TEST_QUEUE/API_26X/ROADMAP 更新、gen_tree_views.py 再実行
- 最後に docs/tasks/LOG.md に15行以内で追記（作ったもの／テスト／解放ノード／保留判断）