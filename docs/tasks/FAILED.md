# FAILED.md — 夜間バッチで test.ps1 が失敗したときの記録

## タスク01 [1-28]（2026-10-19 23:37）
- 失敗：`tungsten_hydrogen_furnace` — "no power -> no reduction"
- 原因：テスト側。直前の「水素なし」段階で毎 tick 電気を入れていたため、内部バッファに約 9 万 J 残り、「電気なし」段階でもバッファの電気で還元が進んだ（MOD は仕様どおり）
- 修正：「電気なし」段階の前にバッファを空にする（SimpleEnergyStorage.extractRaw）。1回目の修正で解決

## タスク06 ケーブルのネットワーク方式（2026-10-20 07:34）
- 失敗1：`battery_on_network`・`cable_ten_hops500k_w` — 同じ tick の中で隣の発電機を外しても、網のキャッシュが外した発電機を覚えていた（MOD 側の不具合）。修正：CableBlock.neighborChanged でもキャッシュを捨てる
- 失敗2：`cable_share_over_limit` — 取り出し量が 50000 でなく 49998（按分の切り捨て）。テスト側を ±10J 許容に
- 失敗3：`cable_ten_hops500k_w` の「1台で 450kW」— タービン内部のバッファ（1MJ）が不足を補っていた（仕様どおり）。テスト側で 1000 tick 待つように。3回目の実行で 43/43 PASSED


## タスク09 [0-16] 塩田（2026-10-20 08:10）
- 失敗1：`salt_pan` — "no sky -> no salt"。canSeeSky（空の明るさ）は光の計算が遅れるので、置いた直後の石の屋根に反応しなかった（MOD 側）。修正：高さマップで判定
- 失敗2：`salt_pan` — "1 salt per minute"。GameTest の場の天井がバリアで、高さマップがバリアの上になっていた。修正：日光を通すブロック（バリア・ガラス）は空とみなす（propagatesSkylightDown）。3回目で 52/52 PASSED

## タスク09 [6-06] 塩酸合成炉（2026-10-20 08:21）
- 失敗：`hcl_burner` — "Didn't succeed or fail within 20 ticks"。テスト側：runAfterDelay(25) なのに maxTicks の指定を忘れた（既定 20）。修正：maxTicks = 80
