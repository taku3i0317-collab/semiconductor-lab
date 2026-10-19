# TEST_QUEUE.md — ゲーム内テスト待ち行列（遠隔作業中はここに溜めて、後でまとめて消化）

テスト日の最初に：`.\gradlew runClient` → ログの `Caused by|Unbound|No key|Failed to parse` を確認（JSON系は起動時にしか出ない）

## v0.2a #1 ケーブル ✅（確認済み：置ける・右クリック表示）

## v0.2a #2 鉱石4つ
- [ ] `/tp @s ~ -30 ~` → `/place feature semiconductor-lab:magnetite_ore` 深層岩の中に出る
- [ ] 花崗岩を置いて `/place feature semiconductor-lab:wolframite_ore` 花崗岩だけ置換
- [ ] 浜辺の砂で `/place feature semiconductor-lab:iron_sand` 黒い砂鉄
- [ ] `/place feature semiconductor-lab:galena_ore`
- [ ] 砂鉄をシャベル → 磁鉄鉱（原石）。鉱石は鉄ツルハシ必須
- [ ] 各鉱石の Shift 解説