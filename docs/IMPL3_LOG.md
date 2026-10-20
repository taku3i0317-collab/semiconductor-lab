# IMPL3_LOG — 3人目の実装担当（IMP3-番号）の記録

担当：幹0（生存＝石器時代）と、その前提で未実装のノード。HANDOFF.md は触らず、ここに書く。
CHEMISTRY.md の番号は S1〜、REFERENCES.md は「# 生存」の節（S1〜）。幹1（[1-03] 銅・[1-30] 錫・[1-05] 青銅）は IMP2 が作業中なので触らない（できあがってから使う）。

## IMP3-01（2026-10-20）幹0 を背骨の先頭から
- ✅ [0-01] 打製石器（遊び方: 実験＋軽）：たたき石 hammerstone（砂利から 10%＝火打ち石が出なかったとき、Fabric の LootTableEvents.MODIFY_DROPS。作業台で戻る、item/HammerstoneItem）、フリントの剥片 flint_flake（剣扱いの小さな刃物、耐久 32）、黒曜石の剥片 obsidian_flake（鋭いがもろい、耐久 16）、ハンドアックス hand_axe（柄のない斧、耐久 80、速さ 3.0＝木の斧 2 と石の斧 4 の間）。修理タグ semiconductor-lab:knapping_stones（火打ち石・黒曜石）
  - 実験：たたき石で右クリック → もう片方の手の火打ち石は剥片2、黒曜石ブロックは剥片1（ブロックは残る、10tick の冷却）、丸石・花こう岩・砂岩などは「粒が粗くて刃にならない」と出るだけ（何も減らない）
  - 分け方（ユーザー承認）：MOD＝柄のない打製石器、バニラの石の道具（丸石＋棒）＝柄を付けた石器（着柄 約50万年前⚠️）
  - 年（ユーザー指示）：確実な最古＝オルドワン 約260万年前（ゴナ）、ロメクウィ 約330万年前は ⚠️、ハンドアックス（アシュール）は約170万年前から
  - GameTest knapping、CHEMISTRY S1〜S3、REFERENCES S1、TEST_QUEUE、tab 順（gen_tab_order.py）。テスト 95/95（IMP2 の変更を取り込んだ後）
  - 共通ファイルの変更：なし（Survival.java・SurvivalTests.java・item/HammerstoneItem.java・lang survival のみ）。tab の年はまだ tech_network.json の -3300000（ネットワーク担当が TECH_TREE から作り直すと -2600000 になる）
- ✅ [0-03] 土器（遊び方: 軽＋実験）：粘土玉×5 → 生の土器 raw_clay_pot →たき火30秒（野焼き）／かまど10秒 → 土器 earthenware_pot。生の土器＋水バケツ → 粘土玉5（戻る）、焼いた土器＋水 → 何もできない（不可逆な脱水）。塩田の煮つめ釜にも使える（salt_pan_earthenware、大釜版もそのまま）。GameTest pottery、CHEMISTRY S4〜S5、REFERENCES S2。テスト 97/97（IMP2 の青銅を取り込んだ後）
- ✅ [0-08] 木工（遊び方: 軽）：ハンドアックス＋砂＋水バケツ（戻る）＋棒 → 磨製石斧 polished_stone_axe（耐久 200、速さ 4.5）。砂なし・水なしは不可。ほぞ組みは へえ のみ。GameTest polished_stone_axe、CHEMISTRY S6、REFERENCES S3。テスト 99/99
- 取り込みで気づいたこと：REFERENCES の IMP2 の O10 が「生存」節の中に入ったので、消さずに光学の節の末尾へ移した。TECH_TREE の [0-02]・[0-03] はネットワーク担当の直しを残し、[0-03] は実装の記述＋直った元の記述にした
