# わかりやすさの穴（自動生成：python tools/check_howto.py、S-026。手で直さない）

- ①入手の道が無いアイテム：**31** / 900
- ②「?」の説明が無い機械（画面のある機械のうち、手書きの lang block.semiconductor-lab.<id>.howto も、自動のレシピの一覧＝machine_recipes も無い物）：**17** / 101
- ③見取り図の無いマルチブロック：**0**

道の数え方：作業台・かまどのレシピ、機械・組立台のレシピ（docs/machine_recipes.json）、世界に生える鉱石とそのルート、ルートの書き換え（LootTableEvents）、例外の一覧（tools/check_howto.py の HOWTO_EXTRA）。ここに無いのに手に入る物は、HOWTO_EXTRA に理由を書いて足すか、machine_recipes の書き出しに足す

## ① 入手の道が無いアイテム（作った担当ごと：アイテムの JSON を最初に足した commit の頭 IMP2＝C・IMP3＝D・IMP4＝E・ほか＝A）
- **C**（30）：aluminized_mirror（Optics）、carbon_dioxide_gas（Gas）、centered_lens_barrel（Precision）、chrome_blank（Optics）、chrome_photomask（Optics）、coated_crown_lens（Optics）、coated_flint_lens（Optics）、cultured_quartz（Precision）、duralumin（Transport）、exposed_dry_plate（Branches）、exposed_plate（Branches）、fluorine_cylinder（Gas）、i_line_filter（Optics）、ir_cut_filter（Optics）、krypton_cylinder（Gas）、krypton_gas（Gas）、mercury_lamp_tube（Optics）、molybdenum_trioxide（Metallurgy）、molybdenum（Metallurgy）、multilayer_mirror（Optics）、neon_cylinder（Gas）、neon_gas（Gas）、optical_fiber（Communication）、ruled_grating（Precision）、silicon_tetrachloride（Metallurgy）、steel_balls_ground（Precision）、steel_balls_precise（Precision）、surface_plate（Precision）、xenon_cylinder（Gas）、xenon_gas（Gas）
- **E**（1）：oxidized_wafer（Semiconductor）

## ② 「?」の説明が無い機械（作った担当ごと）
- **C**（17）：air_conditioner、diffusion_pump、electron_microscope、euv_scanner、euv_source、fluorine_cell、grinder、kibble_balance、mask_aligner、mercury_lamp、rare_gas_column、refrigerator、rotary_pump、ruling_engine、scanner、stepper、turbo_pump

## ③ 見取り図の無いマルチブロック
- なし
