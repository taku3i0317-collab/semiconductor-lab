# タスク：決定反映 → 自動テスト基盤（実装モード）。段階ごとに commit & push。

## 0. 決定反映
- [3-08] 転炉：粘土突き固め内張り＝本物の初期ベッセマー炉の「ガニスター（珪質砂岩+粘土）酸性内張り」と同じ、と注記。🔧のまま。[3-12] 耐火れんがは次の実装候補
- [1-26] 鋼：CHEMISTRY と TECH_TREE に「バニラ鉄＝錬鉄（炭素≈0）なので本来ベッセマーは効かない。暫定で銑鉄の近似。[1-09] 高炉実装時に『銑鉄』アイテムを出し転炉は銑鉄のみ受け付ける。錬鉄+木炭→るつぼ鋼 [1-11] を正しい代替ルートとして追加予定」と明記
- [2-18] 綿→木材パルプ [15-01] のセルロースに変更。[8-18] 人工水晶 OK。ガスホルダー仕様 OK（1000mol・1袋ずつ）
- gen_tree_views.py 再実行

## 1. スモークテスト基盤
- Fabric Loom の gametest 実行（runGametest 相当）を build.gradle に設定。26.3 の Loom での正しい書き方を jar/ドキュメントで確認し API_26X.md に記録
- tools/test.ps1 を作成：`./gradlew.bat runGametest` を実行し、ログから `Caused by|Unbound|No key|Failed to parse|ERROR.*semiconductor-lab|GameTest.*(FAILED|PASSED)` を抽出して要約表示（401/authlib/Realms 除外）。終了コードで成否
- CLAUDE.md ルール3に「機械の動作に関わる変更なら tools/test.ps1 も実行」を追記

## 2. GameTest（fabric-gametest-api-v1 の API を javap で確認）
docs/TEST_QUEUE.md の未チェック項目のうち機械的に判定できるものを全部 GameTest に。最低限：
鉱石6種の feature 配置（対象ブロックだけ置換）／水車（流水で >0、水源のみ 0）／蒸気機関（石炭+水で 50kW、欠けると 0、バケツが戻る）／ケーブル3本で蓄積増・1%減／電解槽（H2×2 O2×1、電気なしで停止、硫酸不減）／ガスホルダー（吸引、爆鳴気で空）／転炉（鉄→鋼 20秒）／帯域精製炉（未精製→6N→9N→10N、9N+P→n型、6N+P 停止、11N 不可）／主要レシピが RecipeManager で解決／chapter1 進捗11個と guide/ が登録／バニラ要素 cinnabar・sulfur・sulfur_spike・potent_sulfur の実在をログ出力（失敗にしない）

## 3. 実行・記録
tools/test.ps1 を実行し docs/TEST_RESULTS.md に結果。TEST_QUEUE の各項目を「自動✅」「自動❌（原因）」「人間が見る（見た目・GUI操作感）」に分類して書き換え。明らかなバグは直してよい（何をどう直したか報告）

## 報告（40行以内）
「test.ps1 の使い方」「GameTest 数と PASSED/FAILED」「失敗の原因と直した点」「人間が見る項目」「バニラ要素の実在」「決めてほしいこと」