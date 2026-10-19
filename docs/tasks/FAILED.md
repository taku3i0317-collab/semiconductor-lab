# FAILED.md — 夜間バッチで test.ps1 が失敗したときの記録

## タスク01 [1-28]（2026-10-19 23:37）
- 失敗：`tungsten_hydrogen_furnace` — "no power -> no reduction"
- 原因：テスト側。直前の「水素なし」段階で毎 tick 電気を入れていたため、内部バッファに約 9 万 J 残り、「電気なし」段階でもバッファの電気で還元が進んだ（MOD は仕様どおり）
- 修正：「電気なし」段階の前にバッファを空にする（SimpleEnergyStorage.extractRaw）。1回目の修正で解決
