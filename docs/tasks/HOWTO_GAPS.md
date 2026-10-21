# わかりやすさの穴（自動生成：python tools/check_howto.py、S-026。手で直さない）

- ①入手の道が無いアイテム：**82** / 887
- ②「?」の説明（lang block.semiconductor-lab.<id>.howto）が無い機械：**113** / 113
- ③見取り図の無いマルチブロック：**0**

道の数え方：作業台・かまどのレシピ、機械・組立台のレシピ（docs/machine_recipes.json）、世界に生える鉱石とそのルート、ルートの書き換え（LootTableEvents）、例外の一覧（tools/check_howto.py の HOWTO_EXTRA）。ここに無いのに手に入る物は、HOWTO_EXTRA に理由を書いて足すか、machine_recipes の書き出しに足す

## ① 入手の道が無いアイテム（担当ごと）
- **A**（32）：al_dotted_wafer（Semiconductor）、ammonia_gas（Gas）、argon_cylinder（Gas）、arsenic（Branches）、carbon_dioxide_gas（Gas）、chip（Semiconductor）、chlorine_cylinder（Gas）、euv_mask_blank（Semiconductor）、euv_mask（Semiconductor）、exposed_dry_plate（Branches）、exposed_plate（Branches）、fluorine_cylinder（Gas）、gaasp_wafer（Branches）、gan_wafer（Branches）、hydrogen_cylinder（Gas）、krypton_cylinder（Gas）、krypton_gas（Gas）、layout_sheet（Semiconductor）、mems_wafer（Semiconductor）、mesa_wafer（Semiconductor）、neon_cylinder（Gas）、neon_gas（Gas）、nitrogen_cylinder（Gas）、optical_fiber（Communication）、oxygen_cylinder（Gas）、rubylith（Semiconductor）、stamp（Semiconductor）、tft_array_glass（Branches）、wood_pulp（Communication）、xenon_cylinder（Gas）、xenon_gas（Gas）、yag_phosphor（Branches）
- **C**（9）：aluminized_mirror（Optics）、chrome_blank（Optics）、chrome_photomask（Optics）、coated_crown_lens（Optics）、coated_flint_lens（Optics）、i_line_filter（Optics）、ir_cut_filter（Optics）、mercury_lamp_tube（Optics）、multilayer_mirror（Optics）
- **D**（34）：alkali_waste（Chemistry）、calcium_carbide（Heat）、carotene（Chemistry）、chlorophyll（Chemistry）、colcothar（Chemistry）、cured_photopolymer（Chemistry）、deionized_water（Chemistry）、einkorn（Survival）、ethylene_gas（Chemistry）、fermented_wash（Chemistry）、flax_stalks（Survival）、formalin（Chemistry）、green_vitriol（Chemistry）、heliograph（Chemistry）、indium_tin_coated_glass（Chemistry）、latex（Chemistry）、light_rare_earth_liquor（Metallurgy）、magnesium（Metallurgy）、molybdenum_trioxide（Metallurgy）、molybdenum（Metallurgy）、nitrogen_farmland（Survival）、obsidian_flake（Survival）、pine_resin（Chemistry）、potassium_fluorozirconate（Metallurgy）、retted_flax（Survival）、roman_concrete（Construction）、silicon_tetrachloride（Metallurgy）、spent_aqua_regia（Chemistry）、spent_bayer_liquor（Metallurgy）、spent_lye（Chemistry）、tired_farmland（Agriculture）、vinegar（Agriculture）、white_arsenic（Metallurgy）、woolen_yarn（Survival）
- **E**（7）：centered_lens_barrel（Precision）、cultured_quartz（Precision）、duralumin（Transport）、ruled_grating（Precision）、steel_balls_ground（Precision）、steel_balls_precise（Precision）、surface_plate（Precision）

## ② 「?」の説明が無い機械（担当ごと）
- **A**（69）：air_separation_unit、ald_reactor、anneal_furnace、arc_lamp、bake_plate、cleaning_bench、cmp_polisher、compound_furnace、compressor、copper_cable、copper_plater、crystal_puller、cvd_furnace、cz_puller、developer_bench、dicing_saw、diffusion_furnace、diffusion_pump、drafting_table、dry_etcher、electric_furnace、electrolyzer、energy_port、epitaxy_reactor、etch_tank、euv_scanner、evaporator、fluorination_reactor、fluorine_cell、fz_puller、gas_holder、gas_port、haber_bosch_reactor、hafnium_chlorinator、hcl_burner、high_voltage_line、inverter、ion_implanter、item_port、lcd_cell、led_lamp、load_port、mask_aligner、oil_diffusion_pump、oxidation_furnace、package_sealer、probe_station、rare_gas_column、rca_bench、rectifier、resist_stripper、rotary_pump、scanner、sealing_station、silane_generator、silicone_reactor、spin_coater、sprengel_pump、sputter、stepper、transformer、turbo_pump、vacuum_chamber、wafer_polisher、wafer_prober、wafer_saw、white_led_lamp、wire_bonder、zone_refining_furnace
- **C**（6）：bridgman_furnace、co2_laser、euv_source、mercury_lamp、oxyhydrogen_burner、reduction_camera
- **D**（33）：acheson_furnace、air_conditioner、ammonia_still、arc_furnace、assembly_bench、autoclave、bayer_digester、bellows、bessemer_converter、blast_furnace_hearth、bloomery、bottle_machine、castner_cell、electroplating_bath、gallium_cell、gas_retort、hall_heroult_cell、high_pressure_reactor、hydrogen_furnace、lead_retort、magnesium_cell、peroxide_cell、pyrolysis_furnace、refrigerator、resist_reactor、reverberatory_furnace、salt_pan、siemens_reactor、solvay_tower、tantalum_refinery、trichlorosilane_reactor、ultrapure_water_system、wood_retort
- **E**（5）：electron_microscope、grinder、kibble_balance、lathe、ruling_engine

## ③ 見取り図の無いマルチブロック
- なし
