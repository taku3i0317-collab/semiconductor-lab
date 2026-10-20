# docs/README.md — docs の入口

このフォルダだけで、Semiconductor Lab（本物の半導体製造を遊びながら学ぶ Minecraft 26.3 / Fabric MOD）の現状と決まりがわかるようにしてある。

## 読む順番
1. HANDOFF.md（現在地1枚）
2. DESIGN.md（決定事項・化学ルール）
3. TECH_TREE_VIEW.md（ツリーの進捗と「今すぐ作れる」）
4. ROADMAP.md（ノードの順番表）
5. CHEMISTRY.md（レシピの化学的根拠）
6. 必要に応じて API_26X.md・TEST_QUEUE.md・tasks/

## 各ファイルの役割
| ファイル | 役割 |
|---|---|
| HANDOFF.md | 現在地1枚：章と tag、テスト本数、ツリー進捗、残りの仮、未決の判断、次の一手。各タスクの最後に更新する |
| DESIGN.md | 設計書：コンセプト・化学ルール・データモデル・電気の設計・26.x で変わった点 |
| ROADMAP.md | 全体計画とバージョン（v0.1 / v0.2a / v0.2b…）ごとのチェックリスト |
| TECH_TREE.md | 技術ツリー本体（347ノード）。ノード＝作業単位（前提/本物/入出力/ゲーム/省略/へえ/テスト） |
| TECH_TREE_VIEW.md | TECH_TREE から自動生成した進捗表・背骨・依存図（tools/gen_tree_views.py。手で編集しない） |
| network.html | 技術ネットワーク図（ブラウザで開く。点＝技術、横＝年、縦＝幹。クリックでカード）。データは tech_network.js |
| network_meta.json | ネットワーク図の見た目（ノード→アイコンのテクスチャ、幹の記号、モノの絵文字、人物＝シルエット＋名前＋ゆかりの道具）。手で編集する |
| tech_network.js | tech_network.json と同じ中身の JS（network.html をファイルから直接開いても読めるように） |
| tech_network.json | 技術ネットワークのデータ（ノード・年・つながり・子孫の数・年の逆転）。gen_tree_views.py が書き出す。ネットワーク図（②）の元データ |
| TECH_TREE_INSPECTION.md | ツリーの検査結果（背骨の切れ・幹ごとのノード数など） |
| CHEMISTRY.md | 全レシピの本物の反応・工程と「省略／近似／仮」の判定（#番号つき） |
| API_26X.md | javap で確認した Minecraft 26.3 API の形。コードはここにある形で書く |
| SOURCES.md | 出典と要確認リスト |
| REFERENCES.md | 参考文献のまとめ（答え→へえ①〜④→確かさ→根拠）。v0.2b シリコンの試作版 |
| TEST_QUEUE.md | テスト待ち行列：自動✅（GameTest）と、人間がゲーム内で見る項目 |
| TEST_RESULTS.md | 自動テスト（GameTest）の実行方法と結果 |
| tasks/queue/ | これからやるタスク（NN_*.md）。tasks/done/ は終わったタスク |
| tasks/next.md | 単発のタスク指示 |
| tasks/LOG.md | タスクごとの作業記録（作ったもの・テスト・解放ノード・保留判断・費用） |
| DECISIONS.md | 決定済みの判断の表（日付／ノード／決定） |
| tasks/DECISIONS_PENDING.md | まだ決まっていない判断だけ（質問・仮決定・理由）。決まったら DECISIONS.md へ移す |
| tasks/FAILED.md | 失敗の記録と直し方 |
| tasks/night_output.txt | 夜間バッチの出力ログ（生ログ） |

## 質問別：どこを見るか
| 知りたいこと | 見る所 |
|---|---|
| 今どこ？次に何を作る？ | HANDOFF.md → TECH_TREE_VIEW.md §1 |
| なぜこの材料・この反応？ | CHEMISTRY.md |
| ノードの仕様 | TECH_TREE.md |
| API の書き方 | API_26X.md |
| 何がゲーム内確認待ち？ | TEST_QUEUE.md |
| 決まったこと・決まっていないこと | DECISIONS.md ／ tasks/DECISIONS_PENDING.md |
| 前のタスクで何をした？ | tasks/LOG.md |
