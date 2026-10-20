# TEST_QUEUE.md — テスト待ち行列（遠隔作業中はここに溜めて、後でまとめて消化）

**まず自動テスト**：`powershell -ExecutionPolicy Bypass -File tools\test.ps1`（GameTest 28本、約20秒、画面なし）。結果は docs/TEST_RESULTS.md。
ゲーム内テストの日は：`.\gradlew runClient` → ログの `Caused by|Unbound|No key|Failed to parse` を確認（JSON系は起動時にしか出ない）

分類（2026-10-19 に書き換え）
- **自動✅** … GameTest で確認済み（`(テスト名)`）。表示・見た目の部分だけ人間が見る
- **自動❌** … GameTest で失敗（原因を書く）。今は 0 件
- **人間** … 見た目・GUI の操作感・音・文字のはみ出しなど、人が見ないと分からないもの

