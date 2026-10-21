# わかりやすさの穴（自動生成：python tools/check_howto.py、S-026。手で直さない）

- ①入手の道が無いアイテム：**82** / 890
- ②「?」の説明（lang block.semiconductor-lab.<id>.howto）が無い機械：**113** / 113
- ③見取り図の無いマルチブロック：**0**

道の数え方：作業台・かまどのレシピ、機械・組立台のレシピ（docs/machine_recipes.json）、世界に生える鉱石とそのルート、ルートの書き換え（LootTableEvents）、例外の一覧（tools/check_howto.py の HOWTO_EXTRA）。ここに無いのに手に入る物は、HOWTO_EXTRA に理由を書いて足すか、machine_recipes の書き出しに足す

## ① 入手の道が無いアイテム（作った担当ごと：アイテムの JSON を最初に足した commit の頭 IMP2＝C・IMP3＝D・IMP4＝E・ほか＝A）
- **A**（10）：argon_cylinder（Gas）、calcium_carbide（Heat）、chip（Semiconductor）、chlorine_cylinder（Gas）、hydrogen_cylinder（Gas）、layout_sheet（Semiconductor）、nitrogen_cylinder（Gas）、oxygen_cylinder（Gas）、rubylith（Semiconductor）、stamp（Semiconductor）
- **C**（30）：aluminized_mirror（Optics）、carbon_dioxide_gas（Gas）、centered_lens_barrel（Precision）、chrome_blank（Optics）、chrome_photomask（Optics）、coated_crown_lens（Optics）、coated_flint_lens（Optics）、cultured_quartz（Precision）、duralumin（Transport）、exposed_dry_plate（Branches）、exposed_plate（Branches）、fluorine_cylinder（Gas）、i_line_filter（Optics）、ir_cut_filter（Optics）、krypton_cylinder（Gas）、krypton_gas（Gas）、mercury_lamp_tube（Optics）、molybdenum_trioxide（Metallurgy）、molybdenum（Metallurgy）、multilayer_mirror（Optics）、neon_cylinder（Gas）、neon_gas（Gas）、optical_fiber（Communication）、ruled_grating（Precision）、silicon_tetrachloride（Metallurgy）、steel_balls_ground（Precision）、steel_balls_precise（Precision）、surface_plate（Precision）、xenon_cylinder（Gas）、xenon_gas（Gas）
- **D**（30）：alkali_waste（Chemistry）、ammonia_gas（Gas）、carotene（Chemistry）、chlorophyll（Chemistry）、colcothar（Chemistry）、cured_photopolymer（Chemistry）、deionized_water（Chemistry）、einkorn（Survival）、ethylene_gas（Chemistry）、fermented_wash（Chemistry）、flax_stalks（Survival）、formalin（Chemistry）、green_vitriol（Chemistry）、heliograph（Chemistry）、indium_tin_coated_glass（Chemistry）、latex（Chemistry）、light_rare_earth_liquor（Metallurgy）、magnesium（Metallurgy）、nitrogen_farmland（Survival）、obsidian_flake（Survival）、pine_resin（Chemistry）、potassium_fluorozirconate（Metallurgy）、retted_flax（Survival）、roman_concrete（Construction）、spent_aqua_regia（Chemistry）、spent_lye（Chemistry）、tired_farmland（Agriculture）、vinegar（Agriculture）、wood_pulp（Communication）、woolen_yarn（Survival）
- **E**（12）：al_dotted_wafer（Semiconductor）、arsenic（Branches）、euv_mask_blank（Semiconductor）、euv_mask（Semiconductor）、gaasp_wafer（Branches）、gan_wafer（Branches）、mems_wafer（Semiconductor）、mesa_wafer（Semiconductor）、spent_bayer_liquor（Metallurgy）、tft_array_glass（Branches）、white_arsenic（Metallurgy）、yag_phosphor（Branches）

## ② 「?」の説明が無い機械（作った担当ごと）
- **A**（38）：acheson_furnace、air_separation_unit、arc_furnace、assembly_bench、bellows、bessemer_converter、blast_furnace_hearth、bloomery、compressor、copper_cable、crystal_puller、cz_puller、dicing_saw、diffusion_furnace、drafting_table、electric_furnace、electrolyzer、energy_port、gas_holder、gas_port、hcl_burner、high_voltage_line、hydrogen_furnace、item_port、oxyhydrogen_burner、probe_station、rectifier、reverberatory_furnace、salt_pan、sealing_station、siemens_reactor、sprengel_pump、transformer、trichlorosilane_reactor、vacuum_chamber、wafer_prober、wafer_saw、zone_refining_furnace
- **C**（23）：air_conditioner、bridgman_furnace、co2_laser、diffusion_pump、electron_microscope、euv_scanner、euv_source、evaporator、fluorine_cell、grinder、kibble_balance、lathe、mask_aligner、mercury_lamp、rare_gas_column、reduction_camera、refrigerator、rotary_pump、ruling_engine、scanner、stepper、turbo_pump、wood_retort
- **D**（10）：autoclave、bottle_machine、electroplating_bath、haber_bosch_reactor、high_pressure_reactor、lcd_cell、magnesium_cell、pyrolysis_furnace、solvay_tower、ultrapure_water_system
- **E**（42）：ald_reactor、ammonia_still、anneal_furnace、arc_lamp、bake_plate、bayer_digester、castner_cell、cleaning_bench、cmp_polisher、compound_furnace、copper_plater、cvd_furnace、developer_bench、dry_etcher、epitaxy_reactor、etch_tank、fluorination_reactor、fz_puller、gallium_cell、gas_retort、hafnium_chlorinator、hall_heroult_cell、inverter、ion_implanter、lead_retort、led_lamp、load_port、oil_diffusion_pump、oxidation_furnace、package_sealer、peroxide_cell、rca_bench、resist_reactor、resist_stripper、silane_generator、silicone_reactor、spin_coater、sputter、tantalum_refinery、wafer_polisher、white_led_lamp、wire_bonder

## ③ 見取り図の無いマルチブロック
- なし
