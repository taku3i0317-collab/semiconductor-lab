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
| network.html | 技術ネットワーク図（ブラウザで開く。点＝技術、横＝年、縦＝幹。クリックでカード）。データは tech_network_graph.js（図）と tech_network_detail.js（カード） |
| node.html | ノード詳細ページ（node.html#9-07 のように番号で開く）：へえ①〜④・本物・人物・確かさと出典・ゲームでの作り方・ここにたどり着く道のり・影響・時を超えたつながり・誤りの報告 |
| machine_recipes.json | 機械のレシピ（入力・出力・電力・時間）。GameTest exportMachineRecipes が tools/test.ps1 のたびに書き出す（手で編集しない） |
| （担当） | network.html・node.html・gen_tree_views.py・network_meta.json・products.md・ideas.md・gen_portraits.py は semilab-network（ネットワーク担当）が触る。このフォルダでは触らない |
| network_meta.json | ネットワーク図の見た目（ノード→アイコンのテクスチャ、幹の記号、モノの絵文字、人物＝ドット絵の肖像（tools/gen_portraits.py）＋名前＋ゆかりの道具）。手で編集する |
| tech_network_graph.js | 図に要る軽いデータ（番号・名前・年・幹・状態・種類・アイコン・位置・つながり・子孫の数）。位置は gen_tree_views.py が先に計算。network.html が最初に読む（ファイルを直接開いても読めるように JS） |
| tech_network_detail.js | カード・詳細用の重いデータ（本物・へえ・出典・レシピ・人物）。network.html はカードを開くか検索したときに読み、node.html は最初から読む |
| tech_network.json | 技術ネットワークのデータ（ノード・年・つながり・子孫の数・年の逆転）。gen_tree_views.py が書き出す。ネットワーク図（②）の元データ |
| furigana.js・furigana_dict.js・furigana_fix.json | ふりがな（S-012）。ページの「ふりがな」ボタンでオン・オフ（設定は tn-furi）。辞書 furigana_dict.js は `python tools/gen_furigana.py`（pykakasi。-I なし）で作る。読みの誤りは furigana_fix.json に「言葉: 読み」で足す（表が優先） |
| linebreak.js・vendor/budoux-ja.min.js | 日本語の折り返し（S-016）。言葉の途中で改行しないように、文節の切れ目でだけ折り返す。BudouX 0.10.0（Google）は **Apache License 2.0**（vendor/LICENSE-budoux.txt。ここだけ CC0 ではない） |
| ui_glyphs.json | 画面の印（✅ ❓ ⚠️ など）を置き換えるドット絵の形（S-016、見本だけ）。tools/gen_ui_icons.py が mockups/S016_emoji.html を作る |
| mockups/ | 見せ方の案・見本（S-012 図の見せ方、S-013 書体、S-014・S-015 前後の写真、S-016 絵文字と小さな動き） |
| （道具）tools/check_layout.mjs | 画面のはみ出しの自動検査（S-016）。5つの画面の大きさ × 7 ページで、横スクロール・はみ出す要素が無いかを調べ、写真を残す。`node tools/check_layout.mjs build/layout`（docs をポート 8791 で出しておく） |
| TECH_TREE_INSPECTION.md | ツリーの検査結果（背骨の切れ・幹ごとのノード数など） |
| CHEMISTRY.md | 全レシピの本物の反応・工程と「省略／近似／仮」の判定（#番号つき）。組み立て結果：書くのは chemistry/<幹>.md（tools/docs_build.py） |
| HUMAN_CHECK_TODAY.md | 今日足された「人間の確認」を試験場の区画の順に並べた手順書（`python tools/gen_human_check.py [日付]` が作る。v0.3 の本線は通しの1つの手順） |
| LITHO_DESIGN.md | 露光とマスク（v0.3 の中心）の仕様：チップ・マスク・層のデータの形、工程の関数（ChipOps）、判定と回路の計算、C・E がつなぐ所 |
| API_26X.md | javap で確認した Minecraft 26.3 API の形。コードはここにある形で書く |
| SOURCES.md | 出典と要確認リスト |
| REFERENCES.md | 参考文献のまとめ（答え→へえ①〜④→確かさ→根拠）。v0.2b シリコンの試作版 |
| TEST_QUEUE.md | テスト待ち行列：自動✅（GameTest）と、人間がゲーム内で見る項目。組み立て結果：書くのは test_queue/<幹>.md |
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
