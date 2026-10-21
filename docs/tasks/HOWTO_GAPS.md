# わかりやすさの穴（自動生成：python tools/check_howto.py、S-026。手で直さない）

- ①入手の道が無いアイテム：**0** / 921
- ②「?」の説明が無い機械（画面のある機械のうち、手書きの lang block.semiconductor-lab.<id>.howto も、自動のレシピの一覧＝machine_recipes も無い物）：**0** / 103
- ①入手の道が無いアイテム：**1** / 923
- ②「?」の説明が無い機械（画面のある機械のうち、手書きの lang block.semiconductor-lab.<id>.howto も、自動のレシピの一覧＝machine_recipes も無い物）：**0** / 104
- ③見取り図の無いマルチブロック：**0**

道の数え方：作業台・かまどのレシピ、機械・組立台のレシピ（docs/machine_recipes.json）、世界に生える鉱石とそのルート、ルートの書き換え（LootTableEvents）、例外の一覧（tools/check_howto.py の HOWTO_EXTRA）。ここに無いのに手に入る物は、HOWTO_EXTRA に理由を書いて足すか、machine_recipes の書き出しに足す

## ① 入手の道が無いアイテム（作った担当ごと：アイテムの JSON を最初に足した commit の頭 IMP2＝C・IMP3＝D・IMP4＝E・ほか＝A）

## ② 「?」の説明が無い機械（作った担当ごと）

## ③ 見取り図の無いマルチブロック
- なし
