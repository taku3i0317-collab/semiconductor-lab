# TEST_RESULTS.md — 自動テスト（GameTest）の結果

実行：`powershell -ExecutionPolicy Bypass -File tools\test.ps1`（中身は `./gradlew.bat runGameTest`。画面なしの専用サーバーで約20秒）
テストのコード：src/gametest/java/com/taku3/semiconductorlab/gametest/LabTests.java ／ 全ログ：build/gametest.log

## 2026-10-19 夜 タスク01（決定反映＋[3-12][1-27][1-28][6-03]）
**28 本中 28 本 PASSED、FAILED 0**（`All 28 required tests passed :)`）。途中で1回失敗→テスト側を修正（docs/tasks/FAILED.md）

| 追加したテスト | 確かめること | 結果 |
|---|---|---|
| gas_holder_full_stops | 満杯（1000mol）で電解槽からも「入」の袋からも吸わない、出口を空けると再開（テスト用の量設定口 setContentsForTest） | ✅ |
| refractory_brick | 未焼成は溶鉱炉でだけ耐火れんがになる（かまど不可）、材料＝砂＋粘土、転炉レシピが耐火れんがを使い粘土を使わない | ✅ |
| lead_from_galena | 方鉛鉱→一酸化鉛→鉛インゴット（かまど2回）、#c:ingots/lead・#c:ingots | ✅ |
| tungsten_hydrogen_furnace | 鉄マンガン重石→WO3、WO3＋H2×3＋3kW×30秒→W粉、水素なし・電気なし・H2不足で停止、W粉2＋H2＋10kW×20秒→焼結棒、粉不足で停止、3kWでは焼結が終わらない、LIT | ✅ |
| hydrogen_furnace_pulls_from_holder | 隣の水素ホルダーから 16 袋まで自動補充（本当に tick を進める） | ✅ |
| compressor_fills_cylinders | ホルダーなし／40mol／電気なし／空ボンベなしで停止、50mol→ボンベ1本（2480 tick・5kW）、水素・酸素、出口に別の種類があると止まる、LIT | ✅ |
| （更新）recipes_resolve | 33→43 レシピ | ✅ |
| （更新）block_drops | 機械に水素炉・圧縮機を追加 | ✅ |
| （更新）vanilla_elements_exist | 辰砂・硫黄の仲間をレジストリの実名で全部ログ（cinnabar, polished_cinnabar, cinnabar_bricks と _slab/_stairs/_wall、sulfur 系、sulfur_cube_bucket など） | ログのみ |

## 2026-10-19（Minecraft 26.3 / Loom 1.18.4 / fabric-gametest-api-v1 4.0.34）
**22 本中 22 本 PASSED、FAILED 0**（`All 22 required tests passed :)`。うち vanilla_elements_exist は失敗にしない「ログ専用」）

| テスト名 | 確かめること | 結果 |
|---|---|---|
| ore_sphalerite / ore_galena | 石・深層岩だけ置換、丸石は残る | ✅ |
| ore_graphite / ore_magnetite | 深層岩だけ置換、**石は残る** | ✅ |
| ore_wolframite | 花崗岩だけ置換、石は残る | ✅ |
| ore_iron_sand | 砂だけ置換、砂岩は残る | ✅ |
| block_drops | 鉱石6種：ツルハシ→原石、シルクタッチ→鉱石ブロック。機械7種は本体が落ちる | ✅（**修正後**。修正前は ❌） |
| tool_requirements | 鉱石4種は鉄ツルハシ必須、黒鉛は石以上、砂鉄はシャベル、水車は斧、機械はツルハシ・素手では落ちない | ✅ |
| water_wheel_flowing | 流水の横で 500 W | ✅ |
| water_wheel_still_water | 四方が水源だけなら 0 W | ✅ |
| steam_engine | 石炭+水で 50 kW、欠けると 0、空バケツが戻る、水1杯＝石炭2個、200 kJ で頭打ち | ✅ |
| cable_chain_fills | 蒸気機関→ケーブル3本、末端の蓄積が増える | ✅ |
| cable_loss_one_percent | 5000 J 送って 4950 J 届く（1%損失） | ✅ |
| electrolyzer | 電気なしで停止、2300 tick で H2×2・O2×1、硫酸不減、停電→再開で続きから、硫酸なし停止、出口満杯で停止、LIT | ✅ |
| gas_holder_pull_and_bang | 電解槽から H2 を吸う（O2 は吸わない）、出口に1袋、O2 を入れると爆鳴気で空・ブロック無事 | ✅ |
| bessemer_converter | 電気なしで停止、400 tick で鉄→鋼、金は不可、LIT、出口満杯で停止、炎の段階、#c:ingots/steel | ✅ |
| zone_refining_purity | 未精製→6N→9N→10N、10N で止まる（11N なし） | ✅ |
| zone_refining_doping | 9N+P→n型、9N+In→p型、6N+P→ドープしない | ✅ |
| recipes_resolve | 全33レシピが RecipeManager にある（JSON エラー・存在しないアイテムがない） | ✅ |
| advancements_and_functions | chapter1 進捗11個、guide/ 進捗11個、guide 関数8個 | ✅ |
| vanilla_elements_exist | バニラ要素の実在をログ出力（下） | ログのみ |

### バニラ要素の実在（26.3、ログ `[VANILLA]`）
| ID | ブロック | アイテム |
|---|---|---|
| minecraft:cinnabar | ある | ある |
| minecraft:sulfur | ある | ある |
| minecraft:sulfur_spike | ある | ある |
| minecraft:potent_sulfur | ある | ある |

### 見つかったバグと直したこと
1. **鉱石がシルクタッチ無しでも鉱石ブロックそのものを落としていた**（block_drops で ❌ を確認）
   - 原因：26.3 でルートテーブルの書式が変わった。`"conditions":[{"condition":…}]` → `"condition":{"type":…}`（またはバニラの述語 `"minecraft:tool/can_silk_touch"`）、`"functions":[{"function":…}]` → `"modifier":[{"type":…}]`。古い書式はエラーにならず**黙って無視**され、ログに WARN「Unreachable entry!」が出るだけだった
   - 修正：loot_table/blocks/*.json 13個を新書式に変換（鉱石6＋機械7。機械の survives_explosion 条件も無視されていた）。WARN も消えた
2. （テスト側）ガスホルダーの爆鳴気テストが時々失敗：爆発で空になったホルダーが、次の吸い込み（10tick ごと）で隣の電解槽に残っていた O2 を吸うため。MOD の不具合ではなく仕様どおり。テストで O2 を片付けてから爆発させるよう修正
3. （test.ps1）PowerShell の Set-Location は cmd のカレントフォルダに伝わらず gradlew.bat が見つからなかった → Start-Process -WorkingDirectory に変更

### 備考
- 機械のテストは BE の tick を手で N 回呼んで時間を早送りしている（電解 115 秒も一瞬）。水の流れ・ケーブル・ガスの吸い込みは本当に tick を進めている
- 鉱石は feature（形）を直接置いて確認。placed_feature の高さ・回数・バイオーム（浜辺だけの砂鉄など）は未確認＝ワールドを作って人間が見る
