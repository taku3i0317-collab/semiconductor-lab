# 試験場（S-001、IMP3）：自動生成 python tools/gen_testworld.py。手で直さない
# 使い方：新しいワールド（クリエイティブ・コマンド有効）の平らな所で /function semiconductor-lab:testworld
# 大きさ：東へ 216・南へ 125 ブロック（足元から）。機械の真下のピンクと黒は試験用の無限電源
fill ~-1 ~-1 ~-2 ~216 ~-1 ~5 minecraft:smooth_stone
fill ~-1 ~ ~-2 ~216 ~5 ~5 minecraft:air
fill ~-1 ~-1 ~6 ~216 ~-1 ~13 minecraft:smooth_stone
fill ~-1 ~ ~6 ~216 ~5 ~13 minecraft:air
fill ~-1 ~-1 ~14 ~216 ~-1 ~21 minecraft:smooth_stone
fill ~-1 ~ ~14 ~216 ~5 ~21 minecraft:air
fill ~-1 ~-1 ~22 ~216 ~-1 ~29 minecraft:smooth_stone
fill ~-1 ~ ~22 ~216 ~5 ~29 minecraft:air
fill ~-1 ~-1 ~30 ~216 ~-1 ~37 minecraft:smooth_stone
fill ~-1 ~ ~30 ~216 ~5 ~37 minecraft:air
fill ~-1 ~-1 ~38 ~216 ~-1 ~45 minecraft:smooth_stone
fill ~-1 ~ ~38 ~216 ~5 ~45 minecraft:air
fill ~-1 ~-1 ~46 ~216 ~-1 ~53 minecraft:smooth_stone
fill ~-1 ~ ~46 ~216 ~5 ~53 minecraft:air
fill ~-1 ~-1 ~54 ~216 ~-1 ~61 minecraft:smooth_stone
fill ~-1 ~ ~54 ~216 ~5 ~61 minecraft:air
fill ~-1 ~-1 ~62 ~216 ~-1 ~69 minecraft:smooth_stone
fill ~-1 ~ ~62 ~216 ~5 ~69 minecraft:air
fill ~-1 ~-1 ~70 ~216 ~-1 ~77 minecraft:smooth_stone
fill ~-1 ~ ~70 ~216 ~5 ~77 minecraft:air
fill ~-1 ~-1 ~78 ~216 ~-1 ~85 minecraft:smooth_stone
fill ~-1 ~ ~78 ~216 ~5 ~85 minecraft:air
fill ~-1 ~-1 ~86 ~216 ~-1 ~93 minecraft:smooth_stone
fill ~-1 ~ ~86 ~216 ~5 ~93 minecraft:air
fill ~-1 ~-1 ~94 ~216 ~-1 ~101 minecraft:smooth_stone
fill ~-1 ~ ~94 ~216 ~5 ~101 minecraft:air
fill ~-1 ~-1 ~102 ~216 ~-1 ~109 minecraft:smooth_stone
fill ~-1 ~ ~102 ~216 ~5 ~109 minecraft:air
fill ~-1 ~-1 ~110 ~216 ~-1 ~117 minecraft:smooth_stone
fill ~-1 ~ ~110 ~216 ~5 ~117 minecraft:air
fill ~-1 ~-1 ~118 ~216 ~-1 ~125 minecraft:smooth_stone
fill ~-1 ~ ~118 ~216 ~5 ~125 minecraft:air
setblock ~0 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹0 生存（石器時代）","↓ この列","",""]}}
setblock ~2 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-01] 打製石器","右クリックでの打ち欠き","（メインにたたき石・オ","フハンドに火打ち石／逆"]}}
setblock ~2 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hammerstone",count:1},{Slot:1b,id:"semiconductor-lab:flint_flake",count:1},{Slot:2b,id:"semiconductor-lab:obsidian_flake",count:1},{Slot:3b,id:"semiconductor-lab:hand_axe",count:1}]}
setblock ~5 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-18] ロープ","ロープの見た目・ツール","チップ",""]}}
setblock ~5 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rope",count:16}]}
setblock ~8 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-03] 土器","たき火の上に生の土器を","置いて30秒で土器にな","るか（見た目）。2つの"]}}
setblock ~8 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:raw_clay_pot",count:16},{Slot:1b,id:"semiconductor-lab:earthenware_pot",count:16}]}
setblock ~11 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-04] 農耕","草を壊して種が出るか、","耕した土に植えて育つか","、熟した野生種を放って"]}}
setblock ~11 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wild_einkorn",count:16},{Slot:1b,id:"semiconductor-lab:einkorn",count:16}]}
setblock ~14 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-05] 豆","豆を育てた後の耕地と、","豆なしの耕地に小麦を並","べて植え、豆の後の方が"]}}
setblock ~14 ~0 ~2 semiconductor-lab:nitrogen_farmland
setblock ~15 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pea",count:16}]}
setblock ~18 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-08] 木工","磨製石斧で木を切る速さ","（石の斧と比べて）、見","た目（柄の向き）、ツー"]}}
setblock ~18 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:polished_stone_axe",count:1}]}
setblock ~21 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-10] 石灰焼成","（人間の確認項目なし）","",""]}}
setblock ~21 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:quicklime",count:16}]}
setblock ~24 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-06] 織物","亜麻の茎を川や池に投げ","こんで約1分で「浸水し","た亜麻の茎」に変わるか"]}}
setblock ~24 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:flax_stalks",count:16},{Slot:1b,id:"semiconductor-lab:retted_flax",count:16},{Slot:2b,id:"semiconductor-lab:flax_fiber",count:16},{Slot:3b,id:"semiconductor-lab:linen_thread",count:16},{Slot:4b,id:"semiconductor-lab:linen_cloth",count:16},{Slot:5b,id:"semiconductor-lab:flax",count:16}]}
setblock ~27 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-14] 水運","石材の荷を4つ持って陸","を歩くのと、丸木舟に積","んで（または持ったまま"]}}
setblock ~27 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:dugout_canoe",count:1},{Slot:1b,id:"semiconductor-lab:stone_load",count:16}]}
setblock ~30 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-11] 消石灰・漆喰","（人間の確認項目なし）","",""]}}
setblock ~30 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:slaked_lime",count:16}]}
setblock ~33 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-16] 塩田","（人間の確認項目なし）","",""]}}
setblock ~33 ~0 ~2 semiconductor-lab:salt_pan
setblock ~34 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bittern",count:16}]}
setblock ~37 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-19] ろくろ","ろくろを置いて、粘土玉","を持って右クリックする","と生の土器ができるか（"]}}
setblock ~37 ~0 ~2 semiconductor-lab:potters_wheel
setblock ~41 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-07] 羊毛と紡錘車","もう片方の手に繊維を持","って右クリックを押し続","けると、棒だけは1秒・"]}}
setblock ~41 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:raw_clay_whorl",count:16},{Slot:1b,id:"semiconductor-lab:earthenware_whorl",count:16},{Slot:2b,id:"semiconductor-lab:stone_whorl",count:16},{Slot:3b,id:"semiconductor-lab:spindle_shaft",count:1},{Slot:4b,id:"semiconductor-lab:spindle",count:1},{Slot:5b,id:"semiconductor-lab:carded_wool",count:16},{Slot:6b,id:"semiconductor-lab:woolen_yarn",count:16}]}
setblock ~44 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[0-09] 車輪","ろくろを置いて、粘土玉","を持って右クリックする","と生の土器ができるか（"]}}
setblock ~44 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wooden_wheel",count:16}]}
setblock ~47 ~0 ~1 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] assembly_b","（人間の確認項目なし）","",""]}}
setblock ~47 ~0 ~2 semiconductor-lab:assembly_bench
setblock ~48 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vacuum_tube",count:16},{Slot:1b,id:"semiconductor-lab:induction_coil",count:16},{Slot:2b,id:"semiconductor-lab:leyden_jar",count:16},{Slot:3b,id:"semiconductor-lab:copper_cable",count:16},{Slot:4b,id:"minecraft:iron_ingot",count:16},{Slot:5b,id:"semiconductor-lab:ito_glass",count:16},{Slot:6b,id:"minecraft:copper_ingot",count:16},{Slot:7b,id:"semiconductor-lab:chip",count:16},{Slot:8b,id:"semiconductor-lab:magnet",count:16},{Slot:9b,id:"minecraft:oak_planks",count:16},{Slot:10b,id:"semiconductor-lab:junction_transistor",count:16},{Slot:11b,id:"semiconductor-lab:graphite",count:16},{Slot:12b,id:"semiconductor-lab:zinc_ingot",count:16},{Slot:13b,id:"semiconductor-lab:graphite_rod",count:16},{Slot:14b,id:"semiconductor-lab:tft_array_glass",count:16},{Slot:15b,id:"semiconductor-lab:rubbed_ito_glass",count:16},{Slot:16b,id:"semiconductor-lab:mbba",count:16},{Slot:17b,id:"semiconductor-lab:polarizing_film",count:16},{Slot:18b,id:"semiconductor-lab:nixie_tube",count:16},{Slot:19b,id:"semiconductor-lab:key_switch",count:16},{Slot:20b,id:"semiconductor-lab:math_coprocessor",count:16},{Slot:21b,id:"minecraft:lever",count:16},{Slot:22b,id:"semiconductor-lab:camera_module",count:1},{Slot:23b,id:"semiconductor-lab:mems_accelerometer",count:1},{Slot:24b,id:"semiconductor-lab:touch_panel",count:1},{Slot:25b,id:"semiconductor-lab:tft_lcd",count:16},{Slot:26b,id:"semiconductor-lab:led_backlight",count:16}]}
setblock ~0 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹1 冶金","↓ この列","",""]}}
setblock ~2 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-02] 自然銅","銅の鉱石を何十個か掘る","と自然銅が出る",""]}}
setblock ~2 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:native_copper",count:16}]}
setblock ~5 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-03] 銅の製錬","新しいワールドの浅い所","（y40〜110）で孔","雀石の鉱石が見つかるか"]}}
setblock ~5 ~0 ~10 semiconductor-lab:malachite_ore
setblock ~6 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:malachite",count:16}]}
setblock ~9 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-27] 鉛","一酸化鉛（黄色い粉）・","鉛インゴット（青灰色）","のテクスチャと名前。へ"]}}
setblock ~9 ~0 ~10 semiconductor-lab:galena_ore
setblock ~10 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:galena",count:16},{Slot:1b,id:"semiconductor-lab:litharge",count:16},{Slot:2b,id:"semiconductor-lab:lead_ingot",count:16},{Slot:3b,id:"semiconductor-lab:lead_bottle",count:16}]}
setblock ~13 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-22] 錫石 SnO2","花崗岩の多い所で錫石の","鉱石が見つかるか（鉄マ","ンガン重石と同じくらい"]}}
setblock ~13 ~0 ~10 semiconductor-lab:cassiterite_ore
setblock ~14 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cassiterite",count:16}]}
setblock ~17 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-30] 錫","スズのインゴットの見た","目・ツールチップ。レシ","ピ本に出るか"]}}
setblock ~17 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tin_ingot",count:16}]}
setblock ~20 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-05] 青銅","青銅インゴットの見た目","（銅と見分けがつくか）",""]}}
setblock ~20 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bronze_charge",count:16},{Slot:1b,id:"semiconductor-lab:bronze_ingot",count:16}]}
setblock ~23 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-32] 銀","レシピ本に灰吹きの流れ","が出るか。銀の見た目",""]}}
setblock ~23 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bone_ash",count:16},{Slot:1b,id:"semiconductor-lab:cupel",count:16},{Slot:2b,id:"semiconductor-lab:cupel_charge",count:16},{Slot:3b,id:"semiconductor-lab:silver_nugget",count:16},{Slot:4b,id:"semiconductor-lab:silver_ingot",count:16}]}
setblock ~26 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-43] アンチモン","輝安鉱の見た目（方鉛鉱","と見分けられるか）、掘","ったときの落とし物"]}}
setblock ~26 ~0 ~10 semiconductor-lab:stibnite_ore
setblock ~27 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:stibnite",count:16},{Slot:1b,id:"semiconductor-lab:antimony_trioxide",count:16},{Slot:2b,id:"semiconductor-lab:antimony",count:16}]}
setblock ~30 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-46] 珪石","新しいワールドで、平原","・森・山など砂漠以外で","も石英鉱脈（灰色に白い"]}}
setblock ~30 ~0 ~10 semiconductor-lab:quartz_vein
setblock ~31 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vein_quartz",count:16}]}
setblock ~34 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-06] 塊鉄炉","塊鉄炉の画面で温度計が","上がり、ふいごを踏むの","をやめると下がるか。赤"]}}
setblock ~34 ~0 ~10 semiconductor-lab:bloomery
setblock ~35 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:iron_bloom",count:16},{Slot:1b,id:"minecraft:raw_iron",count:16},{Slot:2b,id:"minecraft:charcoal",count:16}]}
setblock ~38 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-08] 鍛鉄","ブルームをかまどに入れ","ると赤く光るブルームが","出て、たき石と作業台に"]}}
setblock ~38 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hot_iron_bloom",count:16}]}
setblock ~41 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-47] 天然磁石","（人間の確認項目なし）","",""]}}
setblock ~41 ~0 ~10 semiconductor-lab:magnetite_ore
setblock ~42 ~0 ~10 semiconductor-lab:iron_sand
setblock ~43 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:magnetite",count:16}]}
setblock ~46 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-45] 水銀","真空計を持って容器に右","クリック → チャット","に「水銀柱の差 760"]}}
setblock ~46 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:iron_flask",count:16},{Slot:1b,id:"semiconductor-lab:cinnabar_dust",count:16},{Slot:2b,id:"semiconductor-lab:mercury",count:16},{Slot:3b,id:"semiconductor-lab:cinnabar_retort_charge",count:16}]}
setblock ~49 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-09] 高炉","炉床を置いて画面を開く","と、足りない場所に粒子","が出て「あと N か所"]}}
setblock ~49 ~0 ~10 semiconductor-lab:tuyere
setblock ~50 ~0 ~10 semiconductor-lab:blast_furnace_hearth
setblock ~51 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pig_iron",count:16},{Slot:1b,id:"minecraft:raw_iron",count:16},{Slot:2b,id:"minecraft:charcoal",count:16},{Slot:3b,id:"minecraft:calcite",count:16}]}
setblock ~54 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-13] 亜鉛","（人間の確認項目なし）","",""]}}
setblock ~54 ~0 ~10 semiconductor-lab:sphalerite_ore
setblock ~55 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sphalerite",count:16},{Slot:1b,id:"semiconductor-lab:zinc_ingot",count:16}]}
setblock ~58 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-58] 伸線","作業台で引き板・ダイス","が手元に戻るか",""]}}
setblock ~58 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:draw_plate",count:1},{Slot:1b,id:"semiconductor-lab:diamond_die",count:1},{Slot:2b,id:"semiconductor-lab:gold_wire",count:16},{Slot:3b,id:"semiconductor-lab:gold_bonding_wire",count:16},{Slot:4b,id:"semiconductor-lab:copper_wire",count:16}]}
setblock ~61 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-21] 蛍石 CaF2","鉱石・蛍石の見た目（紫","と緑のつぶ）、石のツル","ハシで掘れて木では掘れ"]}}
setblock ~61 ~0 ~10 semiconductor-lab:fluorite_ore
setblock ~62 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:fluorite",count:16}]}
setblock ~65 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-10] コークス高炉","E のガス工場のレトル","トで作ったコークスを高","炉の燃料のスロットに入"]}}
setblock ~65 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coke",count:16}]}
setblock ~68 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-11] るつぼ鋼","作業台と溶鉱炉で るつ","ぼ → 仕込み → る","つぼ鋼 の順に作れるか"]}}
setblock ~68 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:raw_crucible",count:16},{Slot:1b,id:"semiconductor-lab:clay_crucible",count:16},{Slot:2b,id:"semiconductor-lab:crucible_charge",count:16},{Slot:3b,id:"semiconductor-lab:crucible_steel",count:16}]}
setblock ~71 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-20] 白金","浜の砂鉄を掘ったときに","たまに出るか",""]}}
setblock ~71 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:platinum_grain",count:16}]}
setblock ~74 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-37] ニッケル","ペントランド鉱の見た目","（深層岩の中）、ニッケ","ルカルボニルを持ち歩く"]}}
setblock ~74 ~0 ~10 semiconductor-lab:pentlandite_ore
setblock ~75 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nickel",count:16}]}
setblock ~78 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-23] 輝水鉛鉱 Mo","（人間の確認項目なし）","",""]}}
setblock ~78 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:molybdenite",count:16},{Slot:1b,id:"semiconductor-lab:molybdenum",count:16}]}
setblock ~81 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-31] モリブデン製錬","銅の鉱石をたくさん掘る","とたまに輝水鉛鉱が出る","か（シルクタッチでは出"]}}
setblock ~81 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:molybdenum_trioxide",count:16}]}
setblock ~84 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-40] クロム","深い所（Y 0 より下","）の深層岩でクロム鉄鉱","の鉱石が見つかるか（鉄"]}}
setblock ~84 ~0 ~10 semiconductor-lab:chromite_ore
setblock ~85 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:chromite",count:16},{Slot:1b,id:"semiconductor-lab:chromite_roast_charge",count:16},{Slot:2b,id:"semiconductor-lab:sodium_chromate",count:16},{Slot:3b,id:"semiconductor-lab:chromium_oxide",count:16},{Slot:4b,id:"semiconductor-lab:thermite_charge",count:16},{Slot:5b,id:"semiconductor-lab:chromium",count:16}]}
setblock ~88 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-18] 白金精製","作業台で瓶がちゃんと手","元に戻るか、色（だいだ","いの液・黄色い粉・銀色"]}}
setblock ~88 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ammonium_chloroplatinate",count:16},{Slot:1b,id:"semiconductor-lab:platinum_sponge",count:16},{Slot:2b,id:"semiconductor-lab:platinum_compact",count:16},{Slot:3b,id:"semiconductor-lab:platinum_ingot",count:16}]}
setblock ~91 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-66] タンタル","花崗岩の中でタンタル石","の鉱石を見つける（まれ","）、2台の GUI"]}}
setblock ~91 ~0 ~10 semiconductor-lab:tantalite_ore
setblock ~92 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~92 ~0 ~10 semiconductor-lab:castner_cell
setblock ~93 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~93 ~0 ~10 semiconductor-lab:tantalum_refinery
setblock ~94 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sodium",count:16},{Slot:1b,id:"semiconductor-lab:tantalite",count:16},{Slot:2b,id:"semiconductor-lab:potassium_fluorotantalate",count:16},{Slot:3b,id:"semiconductor-lab:tantalum",count:16},{Slot:4b,id:"semiconductor-lab:sodium_hydroxide",count:16},{Slot:5b,id:"semiconductor-lab:iron_flask",count:16},{Slot:6b,id:"semiconductor-lab:hydrofluoric_acid_pe",count:16},{Slot:7b,id:"semiconductor-lab:potassium_fluoride",count:16}]}
setblock ~97 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-33] ホウ素","新しいワールドの砂漠の","地表近く（y56〜76","）でホウ砂の堆積（白い"]}}
setblock ~97 ~0 ~10 semiconductor-lab:borax_deposit
setblock ~98 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:borax",count:16},{Slot:1b,id:"semiconductor-lab:boric_acid",count:16},{Slot:2b,id:"semiconductor-lab:boron_trioxide",count:16}]}
setblock ~101 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-42] リチウム","花崗岩の中にリチア輝石","の鉱石が見つかるか（ま","れ）、見た目"]}}
setblock ~101 ~0 ~10 semiconductor-lab:spodumene_ore
setblock ~102 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:spodumene",count:16},{Slot:1b,id:"semiconductor-lab:beta_spodumene",count:16},{Slot:2b,id:"semiconductor-lab:lithium_sulfate",count:16},{Slot:3b,id:"semiconductor-lab:lithium_carbonate",count:16}]}
setblock ~105 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-24] ボーキサイト","ジャングル・サバンナで","地表近くに見つかるか、","赤茶色の見た目、木のツ"]}}
setblock ~105 ~0 ~10 semiconductor-lab:bauxite_ore
setblock ~106 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bauxite",count:16}]}
setblock ~109 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-07] 黒鉛","（人間の確認項目なし）","",""]}}
setblock ~109 ~0 ~10 semiconductor-lab:graphite_ore
setblock ~110 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:graphite",count:16}]}
setblock ~113 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-26] 鋼","見た目：正面に黒い炉口","、上面に内張り、下面に","羽口。置いた向きに正面"]}}
setblock ~113 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~113 ~0 ~10 semiconductor-lab:bessemer_converter
setblock ~114 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:steel_ingot",count:16},{Slot:1b,id:"semiconductor-lab:pig_iron",count:16}]}
setblock ~117 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-44] ガリウム","バイヤー法の溶解釜の右","下のスロット、ガリウム","の電解槽の GUI"]}}
setblock ~117 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~117 ~0 ~10 semiconductor-lab:gallium_cell
setblock ~118 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:spent_bayer_liquor",count:16},{Slot:1b,id:"semiconductor-lab:gallium",count:16},{Slot:2b,id:"semiconductor-lab:polyethylene_bottle",count:16}]}
setblock ~121 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-38] 氷晶石の合成","作業台で作ったとき鉛の","瓶が3本手元に戻る",""]}}
setblock ~121 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cryolite",count:16}]}
setblock ~124 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-29] アルミニウム","電解槽の GUI（左に","アルミナ・氷晶石・黒鉛","棒、オレンジのバー、浴"]}}
setblock ~124 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~124 ~0 ~10 semiconductor-lab:hall_heroult_cell
setblock ~125 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:aluminium_ingot",count:16},{Slot:1b,id:"semiconductor-lab:alumina",count:16},{Slot:2b,id:"semiconductor-lab:cryolite",count:16},{Slot:3b,id:"semiconductor-lab:graphite_rod",count:16}]}
setblock ~128 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-14] バイヤー法","溶解釜の GUI（左に","3つ、右に出口3つ、液","の残り回数・電力・状態"]}}
setblock ~128 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~128 ~0 ~10 semiconductor-lab:bayer_digester
setblock ~129 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:aluminium_hydroxide",count:16},{Slot:1b,id:"semiconductor-lab:alumina",count:16},{Slot:2b,id:"semiconductor-lab:red_mud",count:16},{Slot:3b,id:"semiconductor-lab:bauxite",count:16},{Slot:4b,id:"semiconductor-lab:sodium_hydroxide",count:16},{Slot:5b,id:"minecraft:water_bucket",count:1}]}
setblock ~132 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-36] 単体ホウ素","（人間の確認項目なし）","",""]}}
setblock ~132 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:boron_magnesium_mix",count:16},{Slot:1b,id:"semiconductor-lab:crude_boron",count:16},{Slot:2b,id:"semiconductor-lab:boron",count:16}]}
setblock ~135 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-28] 乾電池","軟マンガン鉱の鉱石が浅","い石の中に見つかるか（","黒い粒の見た目）。電池"]}}
setblock ~135 ~0 ~10 semiconductor-lab:pyrolusite_ore
setblock ~136 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pyrolusite",count:16}]}
setblock ~139 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-15] 金属シリコン","アーク炉の GUI（左","に珪石・木炭・電極、青","白いバー、右に電力・必"]}}
setblock ~139 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:metallurgical_silicon",count:16}]}
setblock ~142 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-28] タングステン線","水素炉の見た目（正面の","窓が加熱中に橙色に光る","・置いた向き）、GUI"]}}
setblock ~142 ~0 ~10 semiconductor-lab:wolframite_ore
setblock ~143 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~143 ~0 ~10 semiconductor-lab:hydrogen_furnace
setblock ~144 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wolframite",count:16},{Slot:1b,id:"semiconductor-lab:wolframite_soda_charge",count:16},{Slot:2b,id:"semiconductor-lab:sodium_tungstate",count:16},{Slot:3b,id:"semiconductor-lab:tungstic_acid",count:16},{Slot:4b,id:"semiconductor-lab:tungsten_trioxide",count:16},{Slot:5b,id:"semiconductor-lab:tungsten_powder",count:16},{Slot:6b,id:"semiconductor-lab:tungsten_bar",count:16},{Slot:7b,id:"semiconductor-lab:tungsten_wire",count:16},{Slot:8b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:9b,id:"semiconductor-lab:germanium_dioxide",count:16}]}
setblock ~147 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-15] 紫外光学材料","ブリッジマン炉の画面（","左上に蛍石・左下にアル","ゴン、電力・状態の文字"]}}
setblock ~147 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:silicon_tetrachloride",count:16}]}
setblock ~150 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-15] 水晶振動子","石英鉱脈を掘って水晶を","集め、のこぎりで水晶片","、右クリックで磨いて振"]}}
setblock ~150 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rock_crystal",count:16}]}
setblock ~153 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-70] ジルコニウム・","砂鉄を掘ってまれにジル","コンが出るか、分別結晶","で作業台に K2ZrF"]}}
setblock ~153 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:zircon",count:16},{Slot:1b,id:"semiconductor-lab:crude_zirconia",count:16},{Slot:2b,id:"semiconductor-lab:potassium_fluorozirconate",count:16},{Slot:3b,id:"semiconductor-lab:crude_fluorozirconate",count:16},{Slot:4b,id:"semiconductor-lab:potassium_fluorohafnate",count:16},{Slot:5b,id:"semiconductor-lab:hafnium_hydroxide",count:16},{Slot:6b,id:"semiconductor-lab:hafnium_oxide",count:16},{Slot:7b,id:"semiconductor-lab:crude_zirconium",count:16},{Slot:8b,id:"semiconductor-lab:crude_hafnium",count:16},{Slot:9b,id:"semiconductor-lab:zirconium_bar",count:16},{Slot:10b,id:"semiconductor-lab:hafnium_bar",count:16}]}
setblock ~156 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-01] 石炭灰からのゲ","（人間の確認項目なし）","",""]}}
setblock ~156 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coal_ash",count:16},{Slot:1b,id:"semiconductor-lab:germanium_concentrate",count:16},{Slot:2b,id:"semiconductor-lab:germanium_dioxide",count:16},{Slot:3b,id:"semiconductor-lab:raw_germanium",count:16}]}
setblock ~159 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-41] チタン","砂鉄を掘って時々チタン","鉄鉱が出るか、6つのア","イテムのツールチップ"]}}
setblock ~159 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ilmenite",count:16},{Slot:1b,id:"semiconductor-lab:titanium_tetrachloride",count:16},{Slot:2b,id:"semiconductor-lab:titanium_sponge",count:16}]}
setblock ~162 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-19] 希土類分離","ジャングル・サバンナで","イオン吸着型の粘土が見","つかるか（シャベル）、"]}}
setblock ~162 ~0 ~10 semiconductor-lab:bastnaesite_ore
setblock ~163 ~0 ~10 semiconductor-lab:ion_adsorption_clay
setblock ~164 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bastnaesite",count:16},{Slot:1b,id:"semiconductor-lab:cerium_oxide",count:16},{Slot:2b,id:"semiconductor-lab:yttrium_oxide",count:16}]}
setblock ~167 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-39] マグネシウム","電解槽の画面（ふつうの","画面）と見た目、4つの","アイテムのツールチップ"]}}
setblock ~167 ~0 ~10 semiconductor-lab:magnesium_cell
setblock ~168 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:magnesium",count:16}]}
setblock ~171 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-34] 四塩化ゲルマニ","（人間の確認項目なし）","",""]}}
setblock ~171 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:crude_germanium_tetrachloride",count:16},{Slot:1b,id:"semiconductor-lab:germanium_tetrachloride",count:16},{Slot:2b,id:"semiconductor-lab:high_purity_germanium_dioxide",count:16},{Slot:3b,id:"semiconductor-lab:quartz_still",count:1}]}
setblock ~174 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-02] ゾーンレベリン","（人間の確認項目なし）","",""]}}
setblock ~174 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:indium",count:16}]}
setblock ~177 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-16] トリクロロシラ","トリクロロシラン反応器","の GUI（左に Si","・HCl・蒸留するTC"]}}
setblock ~177 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~177 ~0 ~10 semiconductor-lab:trichlorosilane_reactor
setblock ~178 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrogen_chloride_gas",count:16},{Slot:1b,id:"semiconductor-lab:trichlorosilane",count:16},{Slot:2b,id:"semiconductor-lab:metallurgical_silicon",count:16}]}
setblock ~181 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-72] 粉末冶金と熱間","電気炉に鉄の圧粉体を入","れて歯車が出るか、型が","作業台に戻るか"]}}
setblock ~181 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:steel_die",count:1},{Slot:1b,id:"semiconductor-lab:iron_powder",count:16},{Slot:2b,id:"semiconductor-lab:iron_compact",count:16},{Slot:3b,id:"semiconductor-lab:sintered_gear",count:16}]}
setblock ~184 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-17] シーメンス法","シーメンス炉の GUI","（左に TCS・H2、","出口に多結晶シリコン・"]}}
setblock ~184 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~184 ~0 ~10 semiconductor-lab:siemens_reactor
setblock ~185 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:polysilicon",count:16},{Slot:1b,id:"semiconductor-lab:trichlorosilane",count:16},{Slot:2b,id:"semiconductor-lab:hydrogen_gas",count:16}]}
setblock ~188 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-07] LED 🌿✅⚠","LED の明かりを置い","て電気を流すと赤く光る","（明るさ7）、化合物半"]}}
setblock ~188 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:white_arsenic",count:16}]}
setblock ~191 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-25] ワット天秤","ワット天秤の画面（電力","・結果・±・ヒントの順","）。空調のそばと真空容"]}}
setblock ~191 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cadmium",count:16}]}
setblock ~194 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-35] ネオジム磁石","酸化セリウムを作ったあ","と作業台に液が残るか、","電気炉に仕込み・圧粉体"]}}
setblock ~194 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:light_rare_earth_oxide",count:16},{Slot:1b,id:"semiconductor-lab:neodymium_oxide",count:16},{Slot:2b,id:"semiconductor-lab:neodymium_fluoride",count:16},{Slot:3b,id:"semiconductor-lab:neodymium",count:16},{Slot:4b,id:"semiconductor-lab:ferroboron",count:16},{Slot:5b,id:"semiconductor-lab:ndfeb_alloy",count:16},{Slot:6b,id:"semiconductor-lab:ndfeb_powder",count:16},{Slot:7b,id:"semiconductor-lab:ndfeb_compact",count:16},{Slot:8b,id:"semiconductor-lab:sintered_ndfeb",count:16},{Slot:9b,id:"semiconductor-lab:neodymium_magnet",count:16},{Slot:10b,id:"semiconductor-lab:light_rare_earth_liquor",count:16}]}
setblock ~197 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] pentlandit","（人間の確認項目なし）","",""]}}
setblock ~197 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pentlandite",count:16},{Slot:1b,id:"semiconductor-lab:matte_charge",count:16},{Slot:2b,id:"semiconductor-lab:nickel_matte",count:16},{Slot:3b,id:"semiconductor-lab:nickel_oxide",count:16},{Slot:4b,id:"semiconductor-lab:crude_nickel",count:16},{Slot:5b,id:"semiconductor-lab:carbon_monoxide_gas",count:16},{Slot:6b,id:"semiconductor-lab:nickel_carbonyl_gas",count:16},{Slot:7b,id:"semiconductor-lab:bittern_under_hcl",count:16},{Slot:8b,id:"semiconductor-lab:anhydrous_magnesium_chloride",count:16},{Slot:9b,id:"semiconductor-lab:magnesium_oxide",count:16},{Slot:10b,id:"semiconductor-lab:light_rare_earth_oxalate",count:16},{Slot:11b,id:"semiconductor-lab:neodymium_electrolysis_charge",count:16},{Slot:12b,id:"semiconductor-lab:ferroboron_charge",count:16},{Slot:13b,id:"semiconductor-lab:magnetizing_yoke",count:1},{Slot:14b,id:"semiconductor-lab:roasted_bastnaesite",count:16},{Slot:15b,id:"semiconductor-lab:oxalic_acid",count:16},{Slot:16b,id:"semiconductor-lab:rare_earth_leachate",count:16},{Slot:17b,id:"semiconductor-lab:rare_earth_oxalate",count:16},{Slot:18b,id:"semiconductor-lab:mixed_rare_earth_oxide",count:16},{Slot:19b,id:"semiconductor-lab:vanadium_magnetite",count:16},{Slot:20b,id:"semiconductor-lab:vanadium_roast_charge",count:16},{Slot:21b,id:"semiconductor-lab:vanadium_calcine",count:16},{Slot:22b,id:"semiconductor-lab:sodium_vanadate_solution",count:16},{Slot:23b,id:"semiconductor-lab:ammonium_metavanadate",count:16},{Slot:24b,id:"semiconductor-lab:vanadium_pentoxide",count:16},{Slot:25b,id:"semiconductor-lab:crude_titanium_tetrachloride",count:16},{Slot:26b,id:"semiconductor-lab:kroll_charge",count:16}]}
setblock ~0 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹2 化学","↓ この列","",""]}}
setblock ~2 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-02] カリ","たき火に原木を置けるか","、30秒で木灰が落ちる","か。レシピ本に灰汁・炭"]}}
setblock ~2 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wood_ash",count:16},{Slot:1b,id:"semiconductor-lab:lye",count:16},{Slot:2b,id:"semiconductor-lab:potash",count:16}]}
setblock ~5 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-09] 石けん","牛・豚・羊を倒して獣脂","が出るか、石けんを作っ","たとき作業台に「グリセ"]}}
setblock ~5 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tallow",count:16},{Slot:1b,id:"semiconductor-lab:soap_batch",count:16},{Slot:2b,id:"semiconductor-lab:spent_lye",count:16},{Slot:3b,id:"semiconductor-lab:saponified_tallow",count:16},{Slot:4b,id:"semiconductor-lab:soap",count:16},{Slot:5b,id:"semiconductor-lab:glycerin",count:16}]}
setblock ~8 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-03] 天然ソーダ","（人間の確認項目なし）","",""]}}
setblock ~8 ~0 ~18 semiconductor-lab:trona_deposit
setblock ~9 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:trona",count:16},{Slot:1b,id:"semiconductor-lab:sodium_carbonate",count:16}]}
setblock ~12 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-14] ソーダ石灰ガラ","（人間の確認項目なし）","",""]}}
setblock ~12 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:1b,id:"semiconductor-lab:glass_batch",count:16},{Slot:2b,id:"semiconductor-lab:soda_lime_glass",count:16}]}
setblock ~15 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-16] 蒸留水","作業台で蒸留器とバケツ","が手元に戻るか",""]}}
setblock ~15 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:distilled_water",count:16}]}
setblock ~18 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-22] 王水","王水を持ち歩くと数分で","「力を失った王水」に変","わり、メッセージが出る"]}}
setblock ~18 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:aqua_regia",count:16},{Slot:1b,id:"semiconductor-lab:spent_aqua_regia",count:16},{Slot:2b,id:"semiconductor-lab:chloroauric_acid",count:16},{Slot:3b,id:"semiconductor-lab:gold_powder",count:16}]}
setblock ~21 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-43] ばん油","黄鉄鉱の見た目（金色）","、水に投げて約2分で緑","礬になるか、作業台でベ"]}}
setblock ~21 ~0 ~18 semiconductor-lab:pyrite_ore
setblock ~22 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pyrite",count:16},{Slot:1b,id:"semiconductor-lab:colcothar",count:16},{Slot:2b,id:"semiconductor-lab:green_vitriol",count:16}]}
setblock ~25 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-23] 木材乾留","乾留炉の GUI（左に","原木・燃料、火の縦バー","、茶色のバー、右に木炭"]}}
setblock ~25 ~0 ~18 semiconductor-lab:wood_retort
setblock ~26 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wood_vinegar",count:16},{Slot:1b,id:"semiconductor-lab:wood_tar",count:16},{Slot:2b,id:"semiconductor-lab:pitch",count:16},{Slot:3b,id:"minecraft:oak_log",count:16},{Slot:4b,id:"minecraft:coal",count:16}]}
setblock ~29 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-04] 海藻灰ソーダ","たき火に乾燥昆布を置け","るか。ヨウ素の見た目",""]}}
setblock ~29 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:kelp_ash",count:16},{Slot:1b,id:"semiconductor-lab:kelp_liquor",count:16},{Slot:2b,id:"semiconductor-lab:iodine",count:16}]}
setblock ~32 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-10] フッ酸","鉛のレトルトの GUI","（上の段に蛍石・硫酸・","鉛の瓶、下に水・燃料、"]}}
setblock ~32 ~0 ~18 semiconductor-lab:lead_retort
setblock ~33 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrofluoric_acid",count:16},{Slot:1b,id:"semiconductor-lab:calcium_sulfate",count:16},{Slot:2b,id:"semiconductor-lab:fluorite",count:16},{Slot:3b,id:"semiconductor-lab:sulfuric_acid",count:16},{Slot:4b,id:"semiconductor-lab:lead_bottle",count:16},{Slot:5b,id:"minecraft:water_bucket",count:1},{Slot:6b,id:"minecraft:coal",count:16}]}
setblock ~36 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-11] 硫化水素と硫黄","H2S を持ち歩くと時","々吐き気とメッセージが","出るか（約2分に1回）"]}}
setblock ~36 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:alkali_waste",count:16},{Slot:1b,id:"semiconductor-lab:hydrogen_sulfide_gas",count:16}]}
setblock ~39 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-05] ルブラン法","吸収塔のレシピで芒硝が","作業台に残り、水バケツ","の空のバケツも戻るか。"]}}
setblock ~39 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:salt_cake",count:16},{Slot:1b,id:"semiconductor-lab:salt_cake_charge",count:16},{Slot:2b,id:"semiconductor-lab:black_ash",count:16}]}
setblock ~42 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-20] コールタール","ガス工場のレトルトの ","GUI（左に石炭・燃料","、火の縦バー、黒い進み"]}}
setblock ~42 ~0 ~18 semiconductor-lab:gas_retort
setblock ~43 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coal_tar",count:16},{Slot:1b,id:"semiconductor-lab:ammoniacal_liquor",count:16},{Slot:2b,id:"minecraft:coal",count:16}]}
setblock ~46 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-36] エチレン","触媒管の見た目（側面＝","鉄の管、上下＝白いアル","ミナ）、エチレンの袋の"]}}
setblock ~46 ~0 ~18 semiconductor-lab:alumina_catalyst_tube
setblock ~47 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ethylene_gas",count:16}]}
setblock ~50 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-07] 重クロム酸カリ","深い所（Y 0 より下","）の深層岩でクロム鉄鉱","の鉱石が見つかるか（鉄"]}}
setblock ~50 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:potassium_dichromate",count:16}]}
setblock ~53 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-18] 白金精製","作業台で瓶がちゃんと手","元に戻るか、色（だいだ","いの液・黄色い粉・銀色"]}}
setblock ~53 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ammonium_chloride",count:16},{Slot:1b,id:"semiconductor-lab:chloroplatinic_acid",count:16}]}
setblock ~56 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-19] 過酸化水素","電解槽の GUI（左に","硫酸・白金・水、2列目","に空の瓶、右に出口3つ"]}}
setblock ~56 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~56 ~0 ~18 semiconductor-lab:peroxide_cell
setblock ~57 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrogen_peroxide",count:16},{Slot:1b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:2b,id:"minecraft:water_bucket",count:1},{Slot:3b,id:"semiconductor-lab:sulfuric_acid",count:16},{Slot:4b,id:"semiconductor-lab:platinum_ingot",count:16}]}
setblock ~60 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-24] ベンゼン","コールタールをたき火に","置くと30秒で軽油が落","ちるか、軽油をたき火 "]}}
setblock ~60 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:light_oil",count:16},{Slot:1b,id:"semiconductor-lab:benzene",count:16},{Slot:2b,id:"semiconductor-lab:xylene",count:16}]}
setblock ~63 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-31] 光で固まる膜：","ヘリオグラフの板を日な","たに置き、数分で色が明","るくなるか（4段）。ガ"]}}
setblock ~63 ~0 ~18 semiconductor-lab:natural_asphalt
setblock ~64 ~0 ~18 semiconductor-lab:heliograph_plate
setblock ~65 ~0 ~18 semiconductor-lab:heliograph
setblock ~66 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:asphalt",count:16},{Slot:1b,id:"semiconductor-lab:pine_resin",count:16},{Slot:2b,id:"semiconductor-lab:turpentine",count:16},{Slot:3b,id:"semiconductor-lab:bitumen_varnish",count:16},{Slot:4b,id:"semiconductor-lab:pewter_plate",count:16}]}
setblock ~69 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-01] 硫酸","（人間の確認項目なし）","",""]}}
setblock ~69 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sulfur_powder",count:16},{Slot:1b,id:"semiconductor-lab:sulfur_dioxide",count:16},{Slot:2b,id:"semiconductor-lab:sulfuric_acid",count:16}]}
setblock ~72 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-26] メタノール","メタノールのツールチッ","プ（GHS の注意・「","飲まない」）と見た目"]}}
setblock ~72 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:limed_wood_liquor",count:16},{Slot:1b,id:"semiconductor-lab:methanol",count:16}]}
setblock ~75 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-25] フェノール","3つのアイテムの見た目","とツールチップ（GHS"," の注意）、レシピ本に"]}}
setblock ~75 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:carbolic_oil",count:16},{Slot:1b,id:"semiconductor-lab:sodium_phenolate",count:16},{Slot:2b,id:"semiconductor-lab:phenol",count:16}]}
setblock ~78 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-22] 蒸気圧縮の冷凍","冷凍機を置いて、エーテ","ル・水を入れ、素手で右","クリックを16回、音と"]}}
setblock ~78 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:diethyl_ether",count:16}]}
setblock ~81 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-12] 硫酸塩","めっき槽の画面（電流・","電気・銅の重さが比例し","て増えるか、約97秒で"]}}
setblock ~81 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~81 ~0 ~18 semiconductor-lab:electroplating_bath
setblock ~82 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:copper_sulfate",count:16},{Slot:1b,id:"semiconductor-lab:graphite_wax_mould",count:16},{Slot:2b,id:"semiconductor-lab:copper_electrotype",count:16},{Slot:3b,id:"minecraft:copper_ingot",count:16}]}
setblock ~85 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-29] ゴム","新しいワールドのジャン","グルにゴムの木が生えて","いるか（多すぎ・少なす"]}}
setblock ~85 ~0 ~18 semiconductor-lab:rubber_log
setblock ~86 ~0 ~18 semiconductor-lab:rubber_leaves
setblock ~87 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rubber_sapling",count:16},{Slot:1b,id:"semiconductor-lab:latex",count:16},{Slot:2b,id:"semiconductor-lab:raw_rubber",count:16},{Slot:3b,id:"semiconductor-lab:rubber_compound",count:16},{Slot:4b,id:"semiconductor-lab:vulcanized_rubber",count:16},{Slot:5b,id:"semiconductor-lab:cyclized_rubber_mix",count:16},{Slot:6b,id:"semiconductor-lab:cyclized_rubber",count:16}]}
setblock ~90 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-44] 赤リン","3つのアイテムの見た目","とツールチップ（GHS","・リン顎の教訓）、白リ"]}}
setblock ~90 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:red_phosphorus",count:16},{Slot:1b,id:"semiconductor-lab:phosphorus_pentoxide",count:16}]}
setblock ~93 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-21] アンモニア水","アンモニア蒸留器の G","UI（左に4つ、右に出","口2つ、水の残り・電力"]}}
setblock ~93 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~93 ~0 ~18 semiconductor-lab:ammonia_still
setblock ~94 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ammonia_water",count:16},{Slot:1b,id:"semiconductor-lab:ammoniacal_liquor",count:16},{Slot:2b,id:"semiconductor-lab:slaked_lime",count:16},{Slot:3b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:4b,id:"minecraft:water_bucket",count:1}]}
setblock ~97 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-42] 酢酸","氷酢酸を持って雪の地方","へ行くと「凍った氷酢酸","」になりアクションバー"]}}
setblock ~97 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:calcium_acetate",count:16},{Slot:1b,id:"semiconductor-lab:acetic_acid",count:16}]}
setblock ~100 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-58] 無水酢酸","10個のアイテムの見た","目とツールチップ（GH","S）、白リンの水の瓶・"]}}
setblock ~100 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:phosphorus_trichloride",count:16},{Slot:1b,id:"semiconductor-lab:acetyl_chloride",count:16},{Slot:2b,id:"semiconductor-lab:sodium_acetate",count:16},{Slot:3b,id:"semiconductor-lab:acetic_anhydride",count:16},{Slot:4b,id:"semiconductor-lab:cinnamic_acid",count:16}]}
setblock ~103 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-39] エタノール","かめを置いて仕込み、数","分で泡が出て「できあが","り」になるか（ランダム"]}}
setblock ~103 ~0 ~18 semiconductor-lab:fermentation_crock
setblock ~104 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wild_yeast",count:16},{Slot:1b,id:"semiconductor-lab:sugar_wash",count:16},{Slot:2b,id:"semiconductor-lab:fermented_wash",count:16},{Slot:3b,id:"semiconductor-lab:ethanol",count:16}]}
setblock ~107 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-27] ホルムアルデヒ","たき火の上に銅の網を置","き、メタノールを持って","右クリック → ホルマ"]}}
setblock ~107 ~0 ~18 semiconductor-lab:copper_gauze
setblock ~108 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:formalin",count:16}]}
setblock ~111 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-06] ソルベー法","ソルベー塔の画面（NH","3（循環）の数と「補給","まで」の回数）、見た目"]}}
setblock ~111 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~111 ~0 ~18 semiconductor-lab:solvay_tower
setblock ~112 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:salt",count:16},{Slot:1b,id:"minecraft:calcite",count:16},{Slot:2b,id:"semiconductor-lab:ammonia_water",count:16}]}
setblock ~115 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-59] ケイ皮酸","10個のアイテムの見た","目とツールチップ（GH","S）、白リンの水の瓶・"]}}
setblock ~115 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:toluene",count:16},{Slot:1b,id:"semiconductor-lab:benzal_chloride",count:16},{Slot:2b,id:"semiconductor-lab:benzaldehyde",count:16},{Slot:3b,id:"semiconductor-lab:perkin_mix",count:16},{Slot:4b,id:"semiconductor-lab:cinnamoyl_chloride",count:16}]}
setblock ~118 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-14] フッ素ガス","電解槽に KF・黒鉛・","フッ酸・空のボンベを入","れてフッ素のボンベがで"]}}
setblock ~118 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:potassium_fluoride",count:16}]}
setblock ~121 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-15] ホウケイ酸ガラ","たき火にソーダ石灰ガラ","スをのせて割れる様子、","ホウケイ酸ガラスはのせ"]}}
setblock ~121 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:borosilicate_batch",count:16},{Slot:1b,id:"semiconductor-lab:borosilicate_glass",count:16},{Slot:2b,id:"semiconductor-lab:cracked_glass",count:16}]}
setblock ~124 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-56] ガラスの自動成","製びん機の画面（ふつう","の画面）、見た目、ホッ","パーでガラスを入れ続け"]}}
setblock ~124 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~124 ~0 ~18 semiconductor-lab:bottle_machine
setblock ~125 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:soda_lime_glass",count:16}]}
setblock ~128 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-28] フェノール樹脂","ノボラック・ベークライ","トの見た目とツールチッ","プ、塩酸の瓶が戻るか、"]}}
setblock ~128 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~128 ~0 ~18 semiconductor-lab:autoclave
setblock ~129 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:novolac_mix",count:16},{Slot:1b,id:"semiconductor-lab:novolac",count:16},{Slot:2b,id:"semiconductor-lab:resol_mix",count:16},{Slot:3b,id:"semiconductor-lab:bakelite",count:16},{Slot:4b,id:"semiconductor-lab:foamy_bakelite",count:16},{Slot:5b,id:"minecraft:water_bucket",count:1},{Slot:6b,id:"semiconductor-lab:tetrafluoroethylene_gas",count:16},{Slot:7b,id:"semiconductor-lab:nylon_salt",count:16}]}
setblock ~132 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-40] アセトン","アセトンの見た目とツー","ルチップ（GHS）",""]}}
setblock ~132 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:acetone",count:16}]}
setblock ~135 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-60] ポリビニルアル","6つのアイテムの見た目","とツールチップ（GHS","、水俣病の教訓の書き方"]}}
setblock ~135 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:acetylene_gas",count:16},{Slot:1b,id:"semiconductor-lab:mercuric_sulfate",count:1},{Slot:2b,id:"semiconductor-lab:vinyl_acetate",count:16},{Slot:3b,id:"semiconductor-lab:polyvinyl_acetate",count:16},{Slot:4b,id:"semiconductor-lab:polyvinyl_alcohol",count:16},{Slot:5b,id:"semiconductor-lab:polyvinyl_cinnamate",count:16}]}
setblock ~138 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-48] 冷媒フロン","ドブソン計でオゾンを読","む → フロンの冷凍機","で氷をたくさん作る →"]}}
setblock ~138 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cfc12_gas",count:16},{Slot:1b,id:"semiconductor-lab:dobson_meter",count:1},{Slot:2b,id:"semiconductor-lab:hfc23_gas",count:16}]}
setblock ~141 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-37] ポリエチレン","反応釜の画面（ふつうの","画面：電力・蓄積・状態","）、見た目、レシピ（鋼"]}}
setblock ~141 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~141 ~0 ~18 semiconductor-lab:high_pressure_reactor
setblock ~142 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:polyethylene",count:16},{Slot:1b,id:"semiconductor-lab:ethylene_gas",count:16},{Slot:2b,id:"semiconductor-lab:oxygen_gas",count:16}]}
setblock ~145 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-17] イオン交換","イオン交換塔に蒸留水を","持って右クリック → ","脱イオン水ができ、アク"]}}
setblock ~145 ~0 ~18 semiconductor-lab:ion_exchange_column
setblock ~146 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cation_exchange_resin",count:16},{Slot:1b,id:"semiconductor-lab:anion_exchange_resin",count:16},{Slot:2b,id:"semiconductor-lab:deionized_water",count:16}]}
setblock ~149 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-51] ナイロン","加圧加熱釜が 270℃"," まで温まるか（10k","W で約300℃に落ち"]}}
setblock ~149 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:raney_nickel",count:1}]}
setblock ~152 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-41] エポキシ樹脂","6つのアイテムの見た目","とツールチップ（GHS","）、塩酸の瓶が戻るか、"]}}
setblock ~152 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bisphenol_a",count:16},{Slot:1b,id:"semiconductor-lab:dichlorohydrin",count:16},{Slot:2b,id:"semiconductor-lab:epichlorohydrin",count:16},{Slot:3b,id:"semiconductor-lab:epoxy_resin",count:16},{Slot:4b,id:"semiconductor-lab:epoxy_molding_compound",count:16},{Slot:5b,id:"semiconductor-lab:cured_epoxy",count:16}]}
setblock ~155 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-38] PTFE","さらし粉・クロロホルム","の見た目とツールチップ","（GHS）、エタノール"]}}
setblock ~155 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~155 ~0 ~18 semiconductor-lab:pyrolysis_furnace
setblock ~156 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:chloroform",count:16},{Slot:1b,id:"semiconductor-lab:antimony_pentachloride",count:1},{Slot:2b,id:"semiconductor-lab:chlorodifluoromethane_gas",count:16},{Slot:3b,id:"semiconductor-lab:tetrafluoroethylene_gas",count:16},{Slot:4b,id:"semiconductor-lab:ptfe",count:16}]}
setblock ~159 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-32] ポジ型フォトレ","日なたでレジストを持ち","歩くと名前が「感光して","しまったレジスト」に変"]}}
setblock ~159 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:positive_resist",count:16}]}
setblock ~162 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-57] ジアゾナフトキ","DNQ を持って昼の屋","外に出ると、2秒ほどで","名前が「光で分解した "]}}
setblock ~162 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:naphthalene",count:16},{Slot:1b,id:"semiconductor-lab:naphthalenesulfonic_acid",count:16},{Slot:2b,id:"semiconductor-lab:naphthol_melt",count:16},{Slot:3b,id:"semiconductor-lab:naphthol",count:16},{Slot:4b,id:"semiconductor-lab:tobias_acid",count:16},{Slot:5b,id:"semiconductor-lab:nitrite_mix",count:16},{Slot:6b,id:"semiconductor-lab:potassium_nitrite",count:16},{Slot:7b,id:"semiconductor-lab:dnq",count:16}]}
setblock ~165 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-54] クロマトグラフ","柱の見た目（しみこむと","色の帯が見えるか）、メ","ッセージ（帯の順）"]}}
setblock ~165 ~0 ~18 semiconductor-lab:chromatography_column
setblock ~166 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:leaf_pigment_extract",count:16},{Slot:1b,id:"semiconductor-lab:carotene",count:16},{Slot:2b,id:"semiconductor-lab:chlorophyll",count:16}]}
setblock ~169 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-50] 配位重合触媒","3つのアイテムのツール","チップ（GHS・高圧法","との違い）、触媒が作業"]}}
setblock ~169 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hdpe",count:16}]}
setblock ~172 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-30] ネガ型フォトレ","KPR を塗った後、現","像槽に薄い NaOH ","を入れると「現像液が型"]}}
setblock ~172 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:negative_resist",count:16}]}
setblock ~175 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-46] 透明な電極 I","蒸着装置（0.01Pa"," 以下）で本当に作れる","か、見た目（銀色 → "]}}
setblock ~175 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:indium_tin_alloy",count:16},{Slot:1b,id:"semiconductor-lab:ito_glass",count:16}]}
setblock ~178 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-11] ドーパント源","ドーパントの付いた結晶","のツールチップ（p型は","赤・n型は青の文字、S"]}}
setblock ~178 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:phosphorus",count:16}]}
setblock ~181 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-17] メサトランジス","メサトランジスタを持ち","歩くと1分ごとにツール","チップの β が下がる"]}}
setblock ~181 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:silicon_etch",count:16}]}
setblock ~184 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-21] 現像・ポストベ","C のマスクアライナー","で露光したウェハーが現","像槽に入るか、現像槽の"]}}
setblock ~184 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:developer",count:16}]}
setblock ~187 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-25] 金属配線","真空蒸着装置に加工中の","ウェハーとアルミを入れ","たときの画面（入口に入"]}}
setblock ~187 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:phosphoric_acid",count:16}]}
setblock ~190 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-47] 化学強化ガラス","作業台でたたき石＋ガラ","スを並べて割れるか、強","化ガラスでは何も出ない"]}}
setblock ~190 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:aluminosilicate_glass",count:16},{Slot:1b,id:"semiconductor-lab:strengthened_glass",count:16}]}
setblock ~193 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-13] ラッピング・鏡","研磨機の GUI（左に","ウェハー・砥粒・シリカ","、銀色のバー、「やり直"]}}
setblock ~193 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sodium_silicate",count:16},{Slot:1b,id:"semiconductor-lab:colloidal_silica",count:16}]}
setblock ~196 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-18] 超純水","装置の画面（ふつうの画","面）と見た目、PE の","瓶が作業台で戻るか、ツ"]}}
setblock ~196 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~196 ~0 ~18 semiconductor-lab:ultrapure_water_system
setblock ~197 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cellulose_acetate",count:16},{Slot:1b,id:"semiconductor-lab:ro_membrane",count:16},{Slot:2b,id:"semiconductor-lab:ultrapure_water",count:16},{Slot:3b,id:"semiconductor-lab:deionized_water",count:16},{Slot:4b,id:"semiconductor-lab:polyethylene_bottle",count:16}]}
setblock ~200 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-45] ベイヤー配列の","作業台で 緑・赤／青・","緑 に並べて4枚できる","か。画像センサーへの組"]}}
setblock ~200 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:copper_phthalocyanine",count:16}]}
setblock ~203 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-55] 導電性高分子","テスターを利き手、調べ","る物を反対の手に持って","右クリック → メッセ"]}}
setblock ~203 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:polyacetylene_film",count:16},{Slot:1b,id:"semiconductor-lab:doped_polyacetylene",count:16},{Slot:2b,id:"semiconductor-lab:continuity_tester",count:1}]}
setblock ~206 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-45] 光で固まる樹脂","樹脂を持って日なたに出","ると数秒で固まり、メッ","セージが出るか、日かげ"]}}
setblock ~206 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:photopolymer_resin",count:16},{Slot:1b,id:"semiconductor-lab:cured_photopolymer",count:16}]}
setblock ~209 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-33] 化学増幅レジス","合成釜の画面（反応ごと","の文・「下の置き場の物","が要る」）、ホットプレ"]}}
setblock ~209 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~209 ~0 ~18 semiconductor-lab:resist_reactor
setblock ~210 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydroxyacetophenone",count:16},{Slot:1b,id:"semiconductor-lab:acetoxyphenyl_ethanol",count:16},{Slot:2b,id:"semiconductor-lab:acetoxystyrene",count:16},{Slot:3b,id:"semiconductor-lab:poly_acetoxystyrene",count:16},{Slot:4b,id:"semiconductor-lab:polyhydroxystyrene",count:16},{Slot:5b,id:"semiconductor-lab:protected_phs",count:16},{Slot:6b,id:"semiconductor-lab:diphenyl_sulfoxide",count:16},{Slot:7b,id:"semiconductor-lab:photoacid_generator",count:16},{Slot:8b,id:"semiconductor-lab:potassium_hexafluoroantimonate",count:16},{Slot:9b,id:"semiconductor-lab:chemically_amplified_resist",count:16},{Slot:10b,id:"semiconductor-lab:tributylamine",count:16},{Slot:11b,id:"semiconductor-lab:euv_resist",count:16},{Slot:12b,id:"semiconductor-lab:phenol",count:16},{Slot:13b,id:"semiconductor-lab:acetic_anhydride",count:16},{Slot:14b,id:"semiconductor-lab:aluminium_chloride",count:16},{Slot:15b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:16b,id:"semiconductor-lab:raney_nickel",count:1},{Slot:17b,id:"semiconductor-lab:ammonia_water",count:16},{Slot:18b,id:"semiconductor-lab:methanol",count:16},{Slot:19b,id:"semiconductor-lab:benzene",count:16},{Slot:20b,id:"semiconductor-lab:sulfur_dioxide",count:16},{Slot:21b,id:"semiconductor-lab:antimony_trioxide",count:16},{Slot:22b,id:"semiconductor-lab:potassium_fluoride",count:16},{Slot:23b,id:"semiconductor-lab:hydrofluoric_acid_pe",count:16},{Slot:24b,id:"semiconductor-lab:hydrogen_peroxide",count:16}]}
setblock ~213 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] polyethyle","（人間の確認項目なし）","",""]}}
setblock ~213 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:polyethylene_bottle",count:16},{Slot:1b,id:"semiconductor-lab:black_ash_charge",count:16},{Slot:2b,id:"semiconductor-lab:aluminosilicate_batch",count:16},{Slot:3b,id:"semiconductor-lab:glass_in_salt_bath",count:16},{Slot:4b,id:"semiconductor-lab:indium_tin_coated_glass",count:16},{Slot:5b,id:"semiconductor-lab:phosphorus_under_nitrogen",count:16},{Slot:6b,id:"semiconductor-lab:raney_alloy",count:16},{Slot:7b,id:"semiconductor-lab:cyclohexanol",count:16},{Slot:8b,id:"semiconductor-lab:adipic_acid",count:16},{Slot:9b,id:"semiconductor-lab:ammonium_adipate",count:16},{Slot:10b,id:"semiconductor-lab:adiponitrile",count:16},{Slot:11b,id:"semiconductor-lab:hexamethylenediamine",count:16},{Slot:12b,id:"semiconductor-lab:nylon_salt",count:16},{Slot:13b,id:"semiconductor-lab:nylon_66",count:16},{Slot:14b,id:"semiconductor-lab:nylon_fiber",count:16},{Slot:15b,id:"semiconductor-lab:acrylic_acid",count:16},{Slot:16b,id:"semiconductor-lab:glycerol_triacrylate",count:16},{Slot:17b,id:"semiconductor-lab:aluminium_chloride",count:16},{Slot:18b,id:"semiconductor-lab:carbon_tetrachloride",count:16},{Slot:19b,id:"semiconductor-lab:benzophenone",count:16},{Slot:20b,id:"semiconductor-lab:triethylaluminium",count:16},{Slot:21b,id:"semiconductor-lab:ziegler_natta_catalyst",count:1},{Slot:22b,id:"semiconductor-lab:hydrofluoric_acid_pe",count:16},{Slot:23b,id:"semiconductor-lab:bleaching_powder",count:16},{Slot:24b,id:"semiconductor-lab:acetaldehyde",count:16},{Slot:25b,id:"semiconductor-lab:crotonaldehyde",count:16},{Slot:26b,id:"semiconductor-lab:butanol",count:16}]}
setblock ~0 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹3 熱","↓ この列","",""]}}
setblock ~2 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-20] 耐火粘土","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:fire_clay",count:16},{Slot:1b,id:"semiconductor-lab:fire_clay_crucible",count:16}]}
setblock ~5 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-04] ふいご","ふいごを素手で右クリッ","クすると音と「送風 1","00%」が出て、1秒ほ"]}}
setblock ~5 ~0 ~26 semiconductor-lab:bellows
setblock ~9 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-06] 反射炉","（人間の確認項目なし）","",""]}}
setblock ~9 ~0 ~26 semiconductor-lab:reverberatory_furnace
setblock ~10 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:zinc_oxide",count:16},{Slot:1b,id:"semiconductor-lab:galena",count:16},{Slot:2b,id:"minecraft:coal",count:16},{Slot:3b,id:"semiconductor-lab:cinnabar_retort_charge",count:16},{Slot:4b,id:"semiconductor-lab:sphalerite",count:16}]}
setblock ~13 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-12] 耐火れんが","耐火れんが（未焼成）/","耐火れんが の名前・テ","クスチャ（白っぽい薄黄"]}}
setblock ~13 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:refractory_brick_blank",count:16},{Slot:1b,id:"semiconductor-lab:refractory_brick",count:16}]}
setblock ~16 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-22] 蒸気圧縮の冷凍","冷凍機を置いて、エーテ","ル・水を入れ、素手で右","クリックを16回、音と"]}}
setblock ~16 ~0 ~26 semiconductor-lab:perkins_ice_machine
setblock ~20 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-13] 断熱材","（人間の確認項目なし）","",""]}}
setblock ~20 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rock_wool",count:16}]}
setblock ~23 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-25] 炭化カルシウム","アーク炉の入口1に生石","灰を入れると約22秒で","カーバイドができ、画面"]}}
setblock ~23 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:calcium_carbide",count:16}]}
setblock ~26 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-15] アーク炉","アーク炉の GUI（左","に珪石・木炭・電極、青","白いバー、右に電力・必"]}}
setblock ~26 ~-1 ~26 semiconductor-lab:test_power_source
setblock ~26 ~0 ~26 semiconductor-lab:arc_furnace
setblock ~27 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vein_quartz",count:16},{Slot:1b,id:"minecraft:charcoal",count:16},{Slot:2b,id:"semiconductor-lab:graphite_rod",count:16}]}
setblock ~30 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-10] アチソン炉","GUI（左上＝炭素「C","」、左下＝砂「SiO2","」、中央＝進み具合バー"]}}
setblock ~30 ~-1 ~26 semiconductor-lab:test_power_source
setblock ~30 ~0 ~26 semiconductor-lab:acheson_furnace
setblock ~31 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:synthetic_graphite",count:16},{Slot:1b,id:"minecraft:coal",count:16},{Slot:2b,id:"minecraft:sand",count:16}]}
setblock ~0 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹4 電気・動力","↓ この列","",""]}}
setblock ~2 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-47] 天然磁石","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:magnet",count:16}]}
setblock ~5 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-13] ボルタ電池","画面（出力・亜鉛の残り","・分極の知らせ）、銅ケ","ーブルで機械（電解槽な"]}}
setblock ~5 ~0 ~34 semiconductor-lab:voltaic_pile
setblock ~9 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-16] アーク灯","置くとまわりが明るい、","棒がなくなると消える",""]}}
setblock ~9 ~0 ~34 semiconductor-lab:arc_lamp
setblock ~13 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-08] 電動モーター","（人間の確認項目なし）","",""]}}
setblock ~13 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:electric_motor",count:16}]}
setblock ~16 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-04] ダイナモ・天然","（人間の確認項目なし）","",""]}}
setblock ~16 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:dynamo",count:16}]}
setblock ~19 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-36] ダニエル電池","画面（出力・亜鉛の残り","・分極の知らせ）、銅ケ","ーブルで機械（電解槽な"]}}
setblock ~19 ~0 ~34 semiconductor-lab:daniell_cell
setblock ~23 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-07] 鉛蓄電池","（人間の確認項目なし）","",""]}}
setblock ~23 ~-1 ~34 semiconductor-lab:test_power_source
setblock ~23 ~0 ~34 semiconductor-lab:lead_acid_battery
setblock ~27 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-05] 自励式発電機","（人間の確認項目なし）","",""]}}
setblock ~27 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:self_excited_dynamo",count:16}]}
setblock ~30 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-01] 水車発電機","（人間の確認項目なし）","",""]}}
setblock ~30 ~0 ~34 semiconductor-lab:water_wheel
setblock ~34 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-34] 交流発電機","タービンに交流発電機を","持って右クリック（メッ","セージ、自励式ダイナモ"]}}
setblock ~34 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:alternator",count:16}]}
setblock ~37 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-14] 蒸気発電","（人間の確認項目なし）","",""]}}
setblock ~37 ~0 ~34 semiconductor-lab:steam_engine
setblock ~38 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"minecraft:coal",count:16},{Slot:1b,id:"minecraft:water_bucket",count:1}]}
setblock ~41 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-03] 銅ケーブル","（人間の確認項目なし）","",""]}}
setblock ~41 ~0 ~34 semiconductor-lab:copper_cable
setblock ~45 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-06] 蒸気タービン","（人間の確認項目なし）","",""]}}
setblock ~45 ~0 ~34 semiconductor-lab:steam_turbine
setblock ~46 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"minecraft:coal",count:16},{Slot:1b,id:"minecraft:water_bucket",count:1}]}
setblock ~49 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-10] 変圧器","タービンに交流発電機を","持って右クリック（メッ","セージ、自励式ダイナモ"]}}
setblock ~49 ~0 ~34 semiconductor-lab:transformer
setblock ~53 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-35] 高圧送電","タービンに交流発電機を","持って右クリック（メッ","セージ、自励式ダイナモ"]}}
setblock ~53 ~0 ~34 semiconductor-lab:high_voltage_line
setblock ~57 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-12] 電気炉","電気炉の見た目（れんが","色、正面の窓が加熱中に","橙）、GUI（タイトル"]}}
setblock ~57 ~-1 ~34 semiconductor-lab:test_power_source
setblock ~57 ~0 ~34 semiconductor-lab:electric_furnace
setblock ~58 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:graphite_rod",count:16},{Slot:1b,id:"semiconductor-lab:graphite_boat_blank",count:16},{Slot:2b,id:"semiconductor-lab:iron_compact",count:16},{Slot:3b,id:"semiconductor-lab:ndfeb_compact",count:16},{Slot:4b,id:"semiconductor-lab:neodymium_electrolysis_charge",count:16},{Slot:5b,id:"semiconductor-lab:refractory_brick_blank",count:16}]}
setblock ~61 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-28] 乾電池","軟マンガン鉱の鉱石が浅","い石の中に見つかるか（","黒い粒の見た目）。電池"]}}
setblock ~61 ~0 ~34 semiconductor-lab:dry_cell_holder
setblock ~62 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:dry_cell",count:16}]}
setblock ~65 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-11] 整流器","タービンに交流発電機を","持って右クリック（メッ","セージ、自励式ダイナモ"]}}
setblock ~65 ~0 ~34 semiconductor-lab:rectifier
setblock ~69 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-09] 高周波発振器","レシピ本で帯域精製炉の","右下が「高周波発振器」","になっている。発振器・"]}}
setblock ~69 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:induction_coil",count:16},{Slot:1b,id:"semiconductor-lab:hf_oscillator",count:16},{Slot:2b,id:"semiconductor-lab:leyden_jar",count:16}]}
setblock ~72 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-15] リチウムイオン","右クリックの表示（「リ","チウムイオン電池: …","」）、放電中に光るか、"]}}
setblock ~72 ~0 ~34 semiconductor-lab:lithium_ion_battery
setblock ~76 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] stepping_m","（人間の確認項目なし）","",""]}}
setblock ~76 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:stepping_motor",count:16},{Slot:1b,id:"semiconductor-lab:lfp_charge",count:16},{Slot:2b,id:"semiconductor-lab:lithium_iron_phosphate",count:16},{Slot:3b,id:"semiconductor-lab:lithium_fluoride",count:16},{Slot:4b,id:"semiconductor-lab:lithium_hexafluorophosphate",count:16}]}
setblock ~0 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹5 真空","↓ この列","",""]}}
setblock ~2 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-03] 水銀柱と圧力の","山の上と谷で右クリック","して値がちがう",""]}}
setblock ~2 ~0 ~42 semiconductor-lab:mercury_barometer
setblock ~6 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-01] 真空ポンプ →","容器の隣でポンプを素手","で右クリック → アク","ションバーに「シュッ…"]}}
setblock ~6 ~0 ~42 semiconductor-lab:hand_vacuum_pump
setblock ~10 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-11] 真空容器","（人間の確認項目なし）","",""]}}
setblock ~10 ~0 ~42 semiconductor-lab:vacuum_chamber
setblock ~14 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-15] シュプレンゲル","真空容器の横にポンプを","置き、水銀を持って右ク","リック（「水銀が足りな"]}}
setblock ~14 ~0 ~42 semiconductor-lab:sprengel_pump
setblock ~15 ~0 ~42 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mercury",count:16}]}
setblock ~18 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-10] 真空計","真空計を持って容器に右","クリック → チャット","に「水銀柱の差 760"]}}
setblock ~18 ~0 ~42 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mercury_vacuum_gauge",count:1}]}
setblock ~21 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-02] 真空管 → 幹","封止台を真空容器の隣に","置き、ケーブルで電気を","送る。GUI（タイトル"]}}
setblock ~21 ~-1 ~42 semiconductor-lab:test_power_source
setblock ~21 ~0 ~42 semiconductor-lab:sealing_station
setblock ~22 ~0 ~42 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vacuum_tube_assembly",count:16},{Slot:1b,id:"semiconductor-lab:vacuum_tube",count:16}]}
setblock ~25 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-04] 油回転ポンプ","真空容器｜拡散ポンプ｜","回転ポンプ と並べて電","気をつなぐ → 回転ポ"]}}
setblock ~25 ~0 ~42 semiconductor-lab:rotary_pump
setblock ~29 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-05] 水銀拡散ポンプ","真空容器｜拡散ポンプ｜","回転ポンプ と並べて電","気をつなぐ → 回転ポ"]}}
setblock ~29 ~0 ~42 semiconductor-lab:diffusion_pump
setblock ~33 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-06] 油拡散ポンプ","反応器の GUI（反応","ごとの文・足りない触媒","の文）、油拡散ポンプの"]}}
setblock ~33 ~-1 ~42 semiconductor-lab:test_power_source
setblock ~33 ~0 ~42 semiconductor-lab:silicone_reactor
setblock ~34 ~-1 ~42 semiconductor-lab:test_power_source
setblock ~34 ~0 ~42 semiconductor-lab:oil_diffusion_pump
setblock ~35 ~0 ~42 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:methyl_chloride",count:16},{Slot:1b,id:"semiconductor-lab:dimethyldichlorosilane",count:16},{Slot:2b,id:"semiconductor-lab:silicone_oil",count:16},{Slot:3b,id:"semiconductor-lab:methanol",count:16},{Slot:4b,id:"semiconductor-lab:hydrogen_chloride_gas",count:16},{Slot:5b,id:"semiconductor-lab:alumina",count:16},{Slot:6b,id:"semiconductor-lab:metallurgical_silicon",count:16},{Slot:7b,id:"minecraft:copper_ingot",count:16},{Slot:8b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:9b,id:"semiconductor-lab:distilled_water",count:16},{Slot:10b,id:"Block{semiconductor-lab:rotary_pump}",count:16}]}
setblock ~38 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-07] ターボ分子ポン","ターボ分子ポンプを真空","容器と回転ポンプの間に","置き、研削した玉の玉軸"]}}
setblock ~38 ~0 ~42 semiconductor-lab:turbo_pump
setblock ~42 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-25] 金属配線","真空蒸着装置に加工中の","ウェハーとアルミを入れ","たときの画面（入口に入"]}}
setblock ~42 ~0 ~42 semiconductor-lab:evaporator
setblock ~0 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹6 ガス・電解","↓ この列","",""]}}
setblock ~2 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-04] 岩塩鉱","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~50 semiconductor-lab:rock_salt_ore
setblock ~3 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rock_salt",count:16},{Slot:1b,id:"semiconductor-lab:salt",count:16}]}
setblock ~6 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-16] 天然硝石の採取","新しいワールドの鍾乳石","の洞窟で硝石土が見つか","るか、多すぎないか。見"]}}
setblock ~6 ~0 ~50 semiconductor-lab:nitre_earth_block
setblock ~7 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nitre_earth",count:16},{Slot:1b,id:"semiconductor-lab:nitre_liquor",count:16},{Slot:2b,id:"semiconductor-lab:saltpeter",count:16}]}
setblock ~10 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-11] 硝酸","作業台で蒸留器・空き瓶","がちゃんと手元に戻るか","（シフトクリックでまと"]}}
setblock ~10 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nitric_acid",count:16},{Slot:1b,id:"semiconductor-lab:silver_nitrate",count:16}]}
setblock ~13 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-10] 硝石床","置いて待つと硝石土にな","るか（どのくらいの時間","か）、見た目"]}}
setblock ~13 ~0 ~50 semiconductor-lab:nitre_bed
setblock ~17 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-01] 電解槽","右クリックで GUI（","タイトル「電解槽」、背","景のスロット枠と実際の"]}}
setblock ~17 ~-1 ~50 semiconductor-lab:test_power_source
setblock ~17 ~0 ~50 semiconductor-lab:electrolyzer
setblock ~18 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:1b,id:"semiconductor-lab:oxygen_gas",count:16},{Slot:2b,id:"minecraft:water_bucket",count:1},{Slot:3b,id:"semiconductor-lab:sulfuric_acid",count:16},{Slot:4b,id:"semiconductor-lab:salt",count:16}]}
setblock ~21 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-02] ガスホルダー","見た目：側面（上が鉄の","ベル・下が青い水）、上","面、下面、インベントリ"]}}
setblock ~21 ~0 ~50 semiconductor-lab:gas_holder
setblock ~25 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-13] 接触法硫酸","レシピ本で SO3 →"," 発煙硫酸 → 硫酸2"," がたどれるか、容器の"]}}
setblock ~25 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:platinum_gauze",count:1},{Slot:1b,id:"semiconductor-lab:sulfur_trioxide",count:16},{Slot:2b,id:"semiconductor-lab:oleum",count:16}]}
setblock ~28 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-03] 圧縮機","圧縮機の見た目（正面の","窓が動作中に青）、GU","I（タイトル「圧縮機」"]}}
setblock ~28 ~-1 ~50 semiconductor-lab:test_power_source
setblock ~28 ~0 ~50 semiconductor-lab:compressor
setblock ~29 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:empty_gas_cylinder",count:16},{Slot:1b,id:"semiconductor-lab:hydrogen_cylinder",count:16},{Slot:2b,id:"semiconductor-lab:oxygen_cylinder",count:16}]}
setblock ~32 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-14] フッ素ガス","電解槽に KF・黒鉛・","フッ酸・空のボンベを入","れてフッ素のボンベがで"]}}
setblock ~32 ~0 ~50 semiconductor-lab:fluorine_cell
setblock ~33 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:fluorine_cylinder",count:16}]}
setblock ~36 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-05] クロルアルカリ","（人間の確認項目なし）","",""]}}
setblock ~36 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:chlorine_gas",count:16},{Slot:1b,id:"semiconductor-lab:chlorine_cylinder",count:16},{Slot:2b,id:"semiconductor-lab:sodium_hydroxide",count:16}]}
setblock ~39 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-07] 空気分離","空気分離装置の GUI","（左に縦の青いバーが下","からたまる、出口3つに"]}}
setblock ~39 ~-1 ~50 semiconductor-lab:test_power_source
setblock ~39 ~0 ~50 semiconductor-lab:air_separation_unit
setblock ~40 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nitrogen_gas",count:16},{Slot:1b,id:"semiconductor-lab:argon_gas",count:16},{Slot:2b,id:"semiconductor-lab:nitrogen_cylinder",count:16},{Slot:3b,id:"semiconductor-lab:argon_cylinder",count:16}]}
setblock ~43 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-08] 希ガス精製","空気分離装置の酸素・窒","素の袋を希ガスの精留塔","に入れて、Kr・Ne "]}}
setblock ~43 ~0 ~50 semiconductor-lab:rare_gas_column
setblock ~44 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:neon_gas",count:16},{Slot:1b,id:"semiconductor-lab:krypton_gas",count:16},{Slot:2b,id:"semiconductor-lab:xenon_gas",count:16},{Slot:3b,id:"semiconductor-lab:neon_cylinder",count:16},{Slot:4b,id:"semiconductor-lab:krypton_cylinder",count:16},{Slot:5b,id:"semiconductor-lab:xenon_cylinder",count:16}]}
setblock ~47 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-06] 塩酸","（人間の確認項目なし）","",""]}}
setblock ~47 ~0 ~50 semiconductor-lab:hcl_burner
setblock ~48 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrochloric_acid",count:16},{Slot:1b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:2b,id:"semiconductor-lab:chlorine_gas",count:16},{Slot:3b,id:"minecraft:water_bucket",count:1},{Slot:4b,id:"semiconductor-lab:glass_bottle",count:16}]}
setblock ~51 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-09] ハーバー・ボッ","反応塔の画面（ふつうの","画面）、肥料を耕地に使","ったときの音とメッセー"]}}
setblock ~51 ~0 ~50 semiconductor-lab:haber_bosch_reactor
setblock ~52 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:iron_catalyst",count:1},{Slot:1b,id:"semiconductor-lab:ammonia_gas",count:16},{Slot:2b,id:"semiconductor-lab:ammonium_sulfate",count:16}]}
setblock ~55 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-14] CO2 レーザ","反射炉の煙道の表示（「","煙道」）、方解石で C","O2 の袋が出るか。C"]}}
setblock ~55 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:carbon_dioxide_gas",count:16}]}
setblock ~0 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹7 光学","↓ この列","",""]}}
setblock ~2 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-04] 屈折望遠鏡と色","屈折望遠鏡を右クリック","で押している間、バニラ","の望遠鏡と同じように視"]}}
setblock ~2 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:achromatic_lens",count:16},{Slot:1b,id:"semiconductor-lab:refracting_telescope",count:1}]}
setblock ~5 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-03] レンズ研磨","作業台で研削皿・研磨皿","が手元に残るか（シフト","クリックでまとめて作っ"]}}
setblock ~5 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rouge",count:16},{Slot:1b,id:"semiconductor-lab:grinding_tool",count:1},{Slot:2b,id:"semiconductor-lab:pitch_lap",count:1},{Slot:3b,id:"semiconductor-lab:crown_lens_blank",count:16},{Slot:4b,id:"semiconductor-lab:flint_lens_blank",count:16},{Slot:5b,id:"semiconductor-lab:crown_lens",count:16},{Slot:6b,id:"semiconductor-lab:flint_lens",count:16},{Slot:7b,id:"semiconductor-lab:camera_lens",count:16},{Slot:8b,id:"semiconductor-lab:ir_cut_filter",count:16}]}
setblock ~8 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-06] プリズム分光","塩を左手に持って火のつ","いたたき火を右クリック"," → チャットに「ナト"]}}
setblock ~8 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:prism",count:16},{Slot:1b,id:"semiconductor-lab:spectroscope",count:1}]}
setblock ~11 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-05] 反射鏡","反射望遠鏡を右クリック","で押している間に拡大・","のぞき穴が出るか。合金"]}}
setblock ~11 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:speculum_charge",count:16},{Slot:1b,id:"semiconductor-lab:speculum_ingot",count:16},{Slot:2b,id:"semiconductor-lab:speculum_mirror",count:16},{Slot:3b,id:"semiconductor-lab:reflecting_telescope",count:1},{Slot:4b,id:"semiconductor-lab:silvered_mirror",count:16},{Slot:5b,id:"semiconductor-lab:aluminized_mirror",count:16}]}
setblock ~14 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-01] 光学ガラス","レシピ本に3つのレシピ","が出るか（一酸化鉛・調","合原料・ソーダ石灰ガラ"]}}
setblock ~14 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:flint_glass_batch",count:16},{Slot:1b,id:"semiconductor-lab:flint_glass",count:16},{Slot:2b,id:"semiconductor-lab:crown_glass",count:16}]}
setblock ~17 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-07] 回折格子","格子分光器で塩を炎に ","→ D線2本の角度と波","長が出るか、昼の空 →"]}}
setblock ~17 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wire_grating",count:16},{Slot:1b,id:"semiconductor-lab:grating_spectroscope",count:1}]}
setblock ~20 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-02] 溶融石英","（人間の確認項目なし）","",""]}}
setblock ~20 ~0 ~58 semiconductor-lab:oxyhydrogen_burner
setblock ~21 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:fused_quartz",count:16},{Slot:1b,id:"semiconductor-lab:quartz_tube",count:16},{Slot:2b,id:"semiconductor-lab:silicon_tetrachloride",count:16},{Slot:3b,id:"semiconductor-lab:hydrogen_cylinder",count:16},{Slot:4b,id:"semiconductor-lab:oxygen_cylinder",count:16},{Slot:5b,id:"semiconductor-lab:ruby_feed",count:16},{Slot:6b,id:"semiconductor-lab:ule_feed",count:16},{Slot:7b,id:"semiconductor-lab:vein_quartz",count:16}]}
setblock ~24 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] photo_plat","（人間の確認項目なし）","",""]}}
setblock ~24 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:photo_plate",count:16}]}
setblock ~27 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-09] 干渉計","点灯中の水銀灯を干渉計","で右クリック → 縞の","本数と長さの3行がチャ"]}}
setblock ~27 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:interferometer",count:1}]}
setblock ~30 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-10] 水銀ランプ","水銀灯をケーブルにつな","いで光るか（前面の絵・","明るさ）、右クリックの"]}}
setblock ~30 ~0 ~58 semiconductor-lab:mercury_lamp
setblock ~31 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mercury_lamp_assembly",count:16},{Slot:1b,id:"semiconductor-lab:mercury_lamp_tube",count:16}]}
setblock ~34 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-14] 真空蒸着","真空容器｜拡散ポンプ｜","回転ポンプ の容器に蒸","着装置もとなりに置いて"]}}
setblock ~34 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:half_silvered_mirror",count:16}]}
setblock ~37 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-15] 紫外光学材料","ブリッジマン炉の画面（","左上に蛍石・左下にアル","ゴン、電力・状態の文字"]}}
setblock ~37 ~-1 ~58 semiconductor-lab:test_power_source
setblock ~37 ~0 ~58 semiconductor-lab:bridgman_furnace
setblock ~38 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:synthetic_quartz",count:16},{Slot:1b,id:"semiconductor-lab:caf2_crystal",count:16},{Slot:2b,id:"semiconductor-lab:caf2_lens",count:16},{Slot:3b,id:"semiconductor-lab:fluorite",count:16},{Slot:4b,id:"semiconductor-lab:argon_gas",count:16}]}
setblock ~41 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-22] 反射防止膜","蒸着装置の画面に蛍石と","レンズが入るか（シフト","クリックも）。膜付きレ"]}}
setblock ~41 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coated_crown_lens",count:16},{Slot:1b,id:"semiconductor-lab:coated_flint_lens",count:16}]}
setblock ~44 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-11] 銀塩フォトマス","レシピ本に乾板までの流","れが出るか。アイテムの","見た目"]}}
setblock ~44 ~-1 ~58 semiconductor-lab:test_power_source
setblock ~44 ~0 ~58 semiconductor-lab:reduction_camera
setblock ~45 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hide_broth",count:16},{Slot:1b,id:"semiconductor-lab:gelatin",count:16},{Slot:2b,id:"semiconductor-lab:photo_emulsion",count:16},{Slot:3b,id:"semiconductor-lab:ferrous_sulfate",count:16},{Slot:4b,id:"semiconductor-lab:hypo_liquor",count:16},{Slot:5b,id:"semiconductor-lab:sodium_thiosulfate",count:16},{Slot:6b,id:"semiconductor-lab:photomask",count:1},{Slot:7b,id:"semiconductor-lab:rubylith",count:1},{Slot:8b,id:"semiconductor-lab:photo_plate",count:16}]}
setblock ~48 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-13] レーザー原理","ルビー→棒→レーザーを","作り、鉛蓄電池を持って","右クリック。干渉計をも"]}}
setblock ~48 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ruby_feed",count:16},{Slot:1b,id:"semiconductor-lab:ruby_boule",count:16},{Slot:2b,id:"semiconductor-lab:ruby_rod",count:1},{Slot:3b,id:"semiconductor-lab:xenon_flashlamp",count:16},{Slot:4b,id:"semiconductor-lab:ruby_laser",count:1}]}
setblock ~51 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-21] 超低膨張ガラス","干渉計を点灯中の水銀灯","に使い、もう片方の手に"," ULE・石英・鋼を持"]}}
setblock ~51 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ule_feed",count:16},{Slot:1b,id:"semiconductor-lab:ule_glass",count:16}]}
setblock ~54 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-14] CO2 レーザ","反射炉の煙道の表示（「","煙道」）、方解石で C","O2 の袋が出るか。C"]}}
setblock ~54 ~-1 ~58 semiconductor-lab:test_power_source
setblock ~54 ~0 ~58 semiconductor-lab:co2_laser
setblock ~55 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:salt_window",count:1},{Slot:1b,id:"semiconductor-lab:vein_quartz",count:16}]}
setblock ~58 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-12] クロムマスク","真空蒸着装置で合成石英","＋クロム → ブランク","。縮小カメラにブランク"]}}
setblock ~58 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:chrome_blank",count:16},{Slot:1b,id:"semiconductor-lab:chrome_photomask",count:1}]}
setblock ~61 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-20] エキシマレーザ","KrF レーザーと K","rF の投影レンズを作","り、ステッパーで方眼6"]}}
setblock ~61 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:krf_laser",count:1},{Slot:1b,id:"semiconductor-lab:arf_laser",count:1},{Slot:2b,id:"semiconductor-lab:projection_lens_arf",count:1},{Slot:3b,id:"semiconductor-lab:projection_lens_krf",count:1}]}
setblock ~64 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-19] 投影レンズ","投影レンズで点灯中の水","銀灯を右クリック → ","チャットのテスト露光（"]}}
setblock ~64 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:projection_lens",count:1}]}
setblock ~67 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-39] 縮小投影露光","ステッパーに電気をつな","ぎ、塗った板・クロムマ","スク・投影レンズ（・ボ"]}}
setblock ~67 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:projection_lens_i",count:1},{Slot:1b,id:"semiconductor-lab:i_line_filter",count:1},{Slot:2b,id:"semiconductor-lab:annular_aperture",count:1},{Slot:3b,id:"semiconductor-lab:quadrupole_aperture",count:1},{Slot:4b,id:"semiconductor-lab:phase_shift_mask",count:1}]}
setblock ~70 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-17] 多層膜ミラー","スパッタ装置の入口に ","ULE ガラス・的の入","口に Mo/Si の的"]}}
setblock ~70 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mo_si_target",count:16},{Slot:1b,id:"semiconductor-lab:multilayer_mirror",count:1}]}
setblock ~73 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-16] 液浸光学","ステッパーの右下の超純","水の入口（ヒント「液浸","の超純水を入れて」）、"]}}
setblock ~73 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:projection_lens_immersion",count:1}]}
setblock ~76 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-18] EUV 光源","EUV 光源の画面（電","力・EUV・レーザーの","台数・ヒント）、レーザ"]}}
setblock ~76 ~0 ~58 semiconductor-lab:euv_source
setblock ~80 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-40] EUV 露光装","EUV 光源・真空容器","を隣に置いて、画面の「","EUV ○mW」とヒン"]}}
setblock ~80 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:euv_mirror_set",count:1},{Slot:1b,id:"semiconductor-lab:euv_projection_optics",count:1},{Slot:2b,id:"semiconductor-lab:euv_high_na_optics",count:1}]}
setblock ~0 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹8 精密","↓ この列","",""]}}
setblock ~2 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-19] 滑車","ブロックを右クリックし","て力と綱の長さの文",""]}}
setblock ~2 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:simple_pulley",count:16},{Slot:1b,id:"semiconductor-lab:compound_pulley",count:16}]}
setblock ~5 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-43] 機械式時計と輪","組立台で腕時計を組み、","右クリックで巻いて手に","持つと画面の下に「⌚ "]}}
setblock ~5 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:gear_train",count:16},{Slot:1b,id:"semiconductor-lab:mechanical_watch",count:1}]}
setblock ~8 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-44] ぜんまい","（人間の確認項目なし）","",""]}}
setblock ~8 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mainspring",count:16}]}
setblock ~11 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-04] ノギスとマイク","旋盤で作ったローター・","規格ねじを左手、ノギス","／マイクロメータを右手"]}}
setblock ~11 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vernier_caliper",count:1},{Slot:1b,id:"semiconductor-lab:micrometer",count:1}]}
setblock ~14 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-10] 光学顕微鏡","フォトマスクを左手、顕","微鏡を右手で右クリック"," → チャットに NA"]}}
setblock ~14 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:simple_microscope",count:1},{Slot:1b,id:"semiconductor-lab:compound_microscope",count:1}]}
setblock ~17 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-45] てんぷとひげぜ","（人間の確認項目なし）","",""]}}
setblock ~17 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:balance_wheel",count:16}]}
setblock ~20 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-47] 宝石の軸受","（人間の確認項目なし）","",""]}}
setblock ~20 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:jewel_bearing",count:16}]}
setblock ~23 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-46] レバー脱進機","（人間の確認項目なし）","",""]}}
setblock ~23 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:lever_escapement",count:16}]}
setblock ~26 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-21] 六分儀","外で昼前（時刻 500","0 ごろ）から何回か右","クリックして南中の文が"]}}
setblock ~26 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sextant",count:1}]}
setblock ~29 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-02] ねじ切りと互換","旋盤の画面に「親ねじ」","の入口（左下）が増えた","。シフトクリックで送り"]}}
setblock ~29 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:lead_screw",count:16},{Slot:1b,id:"semiconductor-lab:standard_screw",count:16}]}
setblock ~32 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-01] 旋盤","旋盤の画面（左に入口・","右に出口・電力の文字）","。水銀灯の画面も同じ書"]}}
setblock ~32 ~-1 ~66 semiconductor-lab:test_power_source
setblock ~32 ~0 ~66 semiconductor-lab:lathe
setblock ~33 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pump_rotor",count:16},{Slot:1b,id:"semiconductor-lab:steel_ingot",count:16},{Slot:2b,id:"semiconductor-lab:lead_screw",count:16}]}
setblock ~36 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-22] メートル原器","作業台でセオドライトが","戻るか。ブロックを2つ","右クリックしてチャット"]}}
setblock ~36 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:metre_bar",count:1}]}
setblock ~39 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-03] 平面と定盤","鋳鉄の板を3枚作り、利","き手と左手に1枚ずつ持","って右クリック → チ"]}}
setblock ~39 ~0 ~66 semiconductor-lab:surface_plate
setblock ~40 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cast_iron_plate",count:1}]}
setblock ~43 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-31] 研削盤","研削盤に鋼球・砥石・マ","イクロメータ／ゲージブ","ロックを入れて動かす。"]}}
setblock ~43 ~0 ~66 semiconductor-lab:grinder
setblock ~47 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-12] 刻線機","刻線機にスペキュラム鏡","・ダイヤモンド・送りね","じを入れて格子を作り、"]}}
setblock ~47 ~0 ~66 semiconductor-lab:ruling_engine
setblock ~48 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ruled_grating",count:1},{Slot:1b,id:"semiconductor-lab:ruled_spectroscope",count:1}]}
setblock ~51 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-20] 玉軸受","研削盤に鋼球・砥石・マ","イクロメータ／ゲージブ","ロックを入れて動かす。"]}}
setblock ~51 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:steel_balls_rough",count:16},{Slot:1b,id:"semiconductor-lab:raw_grinding_wheel",count:16},{Slot:2b,id:"semiconductor-lab:grinding_wheel",count:1},{Slot:3b,id:"semiconductor-lab:steel_balls_ground",count:16},{Slot:4b,id:"semiconductor-lab:steel_balls_precise",count:16},{Slot:5b,id:"semiconductor-lab:ball_bearing",count:16}]}
setblock ~54 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-23] キログラム原器","原器を2つ作り、1つだ","け／2つ持って右クリッ","クの文。何日か寝て進め"]}}
setblock ~54 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:kilogram_prototype",count:1}]}
setblock ~57 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-05] ゲージブロック","ゲージブロックのセット","を空中で右クリック →"," チャットの組み合わせ"]}}
setblock ~57 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:gauge_block_set",count:1}]}
setblock ~60 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-33] 質量分析","もう片方の手に試料を持","って右クリックしたとき","のチャットの表示"]}}
setblock ~60 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mass_spectrometer",count:1}]}
setblock ~63 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-15] 水晶振動子","石英鉱脈を掘って水晶を","集め、のこぎりで水晶片","、右クリックで磨いて振"]}}
setblock ~63 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:diamond_saw",count:1},{Slot:1b,id:"semiconductor-lab:quartz_blank",count:1},{Slot:2b,id:"semiconductor-lab:crystal_resonator",count:1}]}
setblock ~66 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-48] 自動巻き","組立台で腕時計を組み、","右クリックで巻いて手に","持つと画面の下に「⌚ "]}}
setblock ~66 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:automatic_rotor",count:16},{Slot:1b,id:"semiconductor-lab:automatic_watch",count:1}]}
setblock ~69 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-07] 除振と恒温","点灯中の水銀灯を干渉計","で右クリック → チャ","ットに「床の揺れ・縞の"]}}
setblock ~69 ~0 ~66 semiconductor-lab:isolation_table
setblock ~73 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-11] 電子顕微鏡","電子顕微鏡の隣に真空容","器を置き、拡散ポンプで"," 0.01Pa 以下に"]}}
setblock ~73 ~0 ~66 semiconductor-lab:electron_microscope
setblock ~77 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-08] ボールねじ精密","ボールねじを旋盤の親ね","じの入口に入れて規格ね","じを作り、マイクロメー"]}}
setblock ~77 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ball_screw",count:1}]}
setblock ~80 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-18] 人工水晶","加圧加熱釜に仕込みと水","バケツを入れ、温まって","から人工水晶ができるか"]}}
setblock ~80 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrothermal_charge",count:16},{Slot:1b,id:"semiconductor-lab:cultured_quartz",count:16}]}
setblock ~83 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-14] 鏡筒の精密組立","鏡筒を利き手、複式顕微","鏡を左手で右クリックを","何回か → チャットの"]}}
setblock ~83 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:lens_barrel",count:1},{Slot:1b,id:"semiconductor-lab:centered_lens_barrel",count:1}]}
setblock ~86 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-24] 国際単位系","干渉計とクリプトン86"," ランプを両手に持ち、","ブロック2つを右クリッ"]}}
setblock ~86 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:krypton_lamp",count:1}]}
setblock ~89 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-35] クオーツ腕時計","腕時計とクロノメーター","を両手に持って右クリッ","ク → 2つの差が出る"]}}
setblock ~89 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:quartz_watch",count:1}]}
setblock ~92 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-25] ワット天秤","ワット天秤の画面（電力","・結果・±・ヒントの順","）。空調のそばと真空容"]}}
setblock ~92 ~0 ~66 semiconductor-lab:kibble_balance
setblock ~93 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:weston_cell",count:1},{Slot:1b,id:"semiconductor-lab:standard_resistor",count:1}]}
setblock ~96 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-13] 液滴発生器","右クリックの文","",""]}}
setblock ~96 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:droplet_generator",count:1}]}
setblock ~0 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹9 半導体（Ge・Si）","↓ この列","",""]}}
setblock ~2 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-47] 加速器","作業台で作れるか、見た","目",""]}}
setblock ~2 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cw_multiplier",count:16},{Slot:1b,id:"semiconductor-lab:accelerator_tube",count:16}]}
setblock ~5 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-05] Geダイオード","（人間の確認項目なし）","",""]}}
setblock ~5 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:point_contact_diode",count:16}]}
setblock ~8 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-03] 点接触トランジ","（人間の確認項目なし）","",""]}}
setblock ~8 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:transistor",count:16}]}
setblock ~11 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-07] Ge 単結晶の","引き上げ機の GUI（","左に Ge・種・水素、","中央バー、出口、右に電"]}}
setblock ~11 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~11 ~0 ~74 semiconductor-lab:crystal_puller
setblock ~12 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:germanium_seed_crystal",count:16},{Slot:1b,id:"semiconductor-lab:germanium_single_crystal",count:16},{Slot:2b,id:"semiconductor-lab:tungsten_wire",count:16},{Slot:3b,id:"semiconductor-lab:refined_germanium",count:16},{Slot:4b,id:"semiconductor-lab:hydrogen_gas",count:16}]}
setblock ~15 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-08] CZ 法","CZ 引き上げ機の G","UI（左に多結晶Si・","種・ドーパント、2列目"]}}
setblock ~15 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~15 ~0 ~74 semiconductor-lab:cz_puller
setblock ~16 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:quartz_crucible",count:16},{Slot:1b,id:"semiconductor-lab:silicon_seed_crystal",count:16},{Slot:2b,id:"semiconductor-lab:silicon_single_crystal",count:16},{Slot:3b,id:"semiconductor-lab:polysilicon",count:16},{Slot:4b,id:"semiconductor-lab:argon_gas",count:16},{Slot:5b,id:"semiconductor-lab:boron_trioxide",count:16}]}
setblock ~19 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-01] Ge 帯域精製","（人間の確認項目なし）","",""]}}
setblock ~19 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~19 ~0 ~74 semiconductor-lab:zone_refining_furnace
setblock ~20 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:refined_germanium",count:16},{Slot:1b,id:"semiconductor-lab:graphite_boat_blank",count:16},{Slot:2b,id:"semiconductor-lab:graphite_boat",count:16},{Slot:3b,id:"semiconductor-lab:synthetic_graphite_boat",count:16},{Slot:4b,id:"semiconductor-lab:raw_germanium",count:16},{Slot:5b,id:"semiconductor-lab:phosphorus",count:16},{Slot:6b,id:"semiconductor-lab:indium",count:16}]}
setblock ~23 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-02] ゾーンレベリン","（人間の確認項目なし）","",""]}}
setblock ~23 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:n_type_germanium",count:16},{Slot:1b,id:"semiconductor-lab:p_type_germanium",count:16}]}
setblock ~26 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-04] 合金接合トラン","（人間の確認項目なし）","",""]}}
setblock ~26 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:junction_transistor_blank",count:16},{Slot:1b,id:"semiconductor-lab:junction_transistor",count:16}]}
setblock ~29 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-09] FZ 法","FZ 炉の GUI、単","結晶・板のツールチップ","（FZ は「酸素 0."]}}
setblock ~29 ~0 ~74 semiconductor-lab:fz_puller
setblock ~33 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-12] ウェハー切断","ウェハー切断機の GU","I（左に結晶・刃、銀色","のバー）。板のツールチ"]}}
setblock ~33 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~33 ~0 ~74 semiconductor-lab:wafer_saw
setblock ~34 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:diamond_saw_blade",count:16},{Slot:1b,id:"semiconductor-lab:silicon_wafer",count:16},{Slot:2b,id:"semiconductor-lab:silicon_single_crystal",count:16}]}
setblock ~37 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-16] 熱拡散","拡散炉の GUI（左に","板・ドーパント、オレン","ジのバー、反応式）。p"]}}
setblock ~37 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~37 ~0 ~74 semiconductor-lab:diffusion_furnace
setblock ~38 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pn_junction_wafer",count:16},{Slot:1b,id:"semiconductor-lab:silicon_wafer",count:16},{Slot:2b,id:"semiconductor-lab:boron_trioxide",count:16},{Slot:3b,id:"semiconductor-lab:phosphorus",count:16}]}
setblock ~41 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-28] イオン注入","注入装置の GUI（「","隣の真空容器を 0.0","1Pa 以下に」「窓も"]}}
setblock ~41 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~41 ~0 ~74 semiconductor-lab:ion_implanter
setblock ~42 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:boron_trioxide",count:16}]}
setblock ~45 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-15] 熱酸化","酸化炉の GUI（左に","板・酸素か水、右に出口","2つ、オレンジのバー、"]}}
setblock ~45 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~45 ~0 ~74 semiconductor-lab:oxidation_furnace
setblock ~46 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:oxidized_wafer",count:16},{Slot:1b,id:"semiconductor-lab:silicon_wafer",count:16},{Slot:2b,id:"semiconductor-lab:oxygen_gas",count:16},{Slot:3b,id:"minecraft:water_bucket",count:1}]}
setblock ~49 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-20] 密着・近接露光","マスクアライナーを置い","て電気をつなぎ、レジス","トを塗った板とフォトマ"]}}
setblock ~49 ~0 ~74 semiconductor-lab:mask_aligner
setblock ~53 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-49] サイリスタ","インバータの向き（後ろ","＝直流、前＝交流）、前","と後ろを同じ網にすると"]}}
setblock ~53 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:thyristor_wafer",count:16},{Slot:1b,id:"semiconductor-lab:thyristor",count:16}]}
setblock ~56 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-17] メサトランジス","メサトランジスタを持ち","歩くと1分ごとにツール","チップの β が下がる"]}}
setblock ~56 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:al_dotted_wafer",count:16},{Slot:1b,id:"semiconductor-lab:alloyed_wafer",count:16},{Slot:2b,id:"semiconductor-lab:waxed_wafer",count:16},{Slot:3b,id:"semiconductor-lab:mesa_wafer",count:16},{Slot:4b,id:"semiconductor-lab:mesa_transistor",count:16}]}
setblock ~59 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-06] 酸化膜エッチン","エッチング槽の GUI","（左に板・フッ酸、水色","のバー、右に状態、下に"]}}
setblock ~59 ~0 ~74 semiconductor-lab:etch_tank
setblock ~60 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:oxidized_wafer",count:16},{Slot:2b,id:"semiconductor-lab:hydrofluoric_acid",count:16}]}
setblock ~63 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-33] ワイヤーボンデ","ボンダーの GUI（パ","ッドの数の表示、「金の","線が足りない」「パッド"]}}
setblock ~63 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~63 ~0 ~74 semiconductor-lab:wire_bonder
setblock ~64 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:chip_header",count:16},{Slot:1b,id:"semiconductor-lab:bonded_chip",count:16},{Slot:2b,id:"semiconductor-lab:chip",count:16},{Slot:3b,id:"semiconductor-lab:gold_bonding_wire",count:16}]}
setblock ~67 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-21] 現像・ポストベ","C のマスクアライナー","で露光したウェハーが現","像槽に入るか、現像槽の"]}}
setblock ~67 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~67 ~0 ~74 semiconductor-lab:resist_stripper
setblock ~68 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~68 ~0 ~74 semiconductor-lab:developer_bench
setblock ~69 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:sulfuric_acid",count:16},{Slot:2b,id:"semiconductor-lab:hydrogen_peroxide",count:16},{Slot:3b,id:"semiconductor-lab:developer",count:16}]}
setblock ~72 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-22] 集積回路","検査台で NOT の例","を「調べる」と、右の欄","に5行（最後まで読める"]}}
setblock ~72 ~0 ~74 semiconductor-lab:probe_station
setblock ~76 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-30] エピタキシーと","シランの発生器の GU","I（反応ごとの文・「空","のボンベが要る」）、塩"]}}
setblock ~76 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~76 ~0 ~74 semiconductor-lab:silane_generator
setblock ~77 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~77 ~0 ~74 semiconductor-lab:cvd_furnace
setblock ~78 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~78 ~0 ~74 semiconductor-lab:epitaxy_reactor
setblock ~79 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:magnesium_silicide",count:16},{Slot:1b,id:"semiconductor-lab:silane_cylinder",count:16},{Slot:2b,id:"semiconductor-lab:magnesium",count:16},{Slot:3b,id:"semiconductor-lab:metallurgical_silicon",count:16},{Slot:4b,id:"semiconductor-lab:hydrochloric_acid",count:16},{Slot:5b,id:"semiconductor-lab:empty_gas_cylinder",count:16},{Slot:6b,id:"semiconductor-lab:process_wafer",count:16},{Slot:7b,id:"semiconductor-lab:oxygen_gas",count:16},{Slot:8b,id:"semiconductor-lab:ammonia_gas",count:16},{Slot:9b,id:"semiconductor-lab:silicon_wafer",count:16},{Slot:10b,id:"semiconductor-lab:silicon_tetrachloride",count:16},{Slot:11b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:12b,id:"semiconductor-lab:boron_trioxide",count:16}]}
setblock ~82 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-32] ダイシング","けがき針を持ち、もう片","方の手に仕上がったウェ","ハー → 使うとチップ"]}}
setblock ~82 ~0 ~74 semiconductor-lab:dicing_saw
setblock ~83 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:diamond_scribe",count:1},{Slot:1b,id:"semiconductor-lab:chip",count:16},{Slot:2b,id:"semiconductor-lab:diamond_blade",count:1}]}
setblock ~86 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-34] 封止","封止機の GUI（「缶","には窒素が必要」、缶と","エポキシで反応式が切り"]}}
setblock ~86 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~86 ~0 ~74 semiconductor-lab:package_sealer
setblock ~87 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:can_cap",count:16},{Slot:1b,id:"semiconductor-lab:can_packaged_chip",count:16},{Slot:2b,id:"semiconductor-lab:plastic_packaged_chip",count:16},{Slot:3b,id:"semiconductor-lab:bonded_chip",count:16},{Slot:4b,id:"semiconductor-lab:nitrogen_gas",count:16},{Slot:5b,id:"semiconductor-lab:epoxy_molding_compound",count:16}]}
setblock ~90 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-11] 銀塩フォトマス","レシピ本に乾板までの流","れが出るか。アイテムの","見た目"]}}
setblock ~90 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rubylith",count:1}]}
setblock ~93 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-19] レジスト塗布","日なたでレジストを持ち","歩くと名前が「感光して","しまったレジスト」に変"]}}
setblock ~93 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~93 ~0 ~74 semiconductor-lab:spin_coater
setblock ~94 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:oxidized_wafer",count:16},{Slot:1b,id:"semiconductor-lab:positive_resist",count:16}]}
setblock ~97 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-35] テスト","プローバーにウェハーと","ボールねじ → 4秒で","「プローバー済み：n "]}}
setblock ~97 ~0 ~74 semiconductor-lab:wafer_prober
setblock ~101 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-13] ラッピング・鏡","研磨機の GUI（左に","ウェハー・砥粒・シリカ","、銀色のバー、「やり直"]}}
setblock ~101 ~0 ~74 semiconductor-lab:wafer_polisher
setblock ~105 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-14] RCA 洗浄","洗浄槽の GUI（左に","ウェハー、2列目に硝酸","・フッ酸・蒸留水、水色"]}}
setblock ~105 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~105 ~0 ~74 semiconductor-lab:cleaning_bench
setblock ~106 ~0 ~74 semiconductor-lab:rca_bench
setblock ~107 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:nitric_acid",count:16},{Slot:2b,id:"semiconductor-lab:hydrofluoric_acid",count:16},{Slot:3b,id:"semiconductor-lab:distilled_water",count:16}]}
setblock ~110 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-42] 画像センサー","画像センサーを持って昼","・夜・洞窟・たいまつの","近くで数が変わる、加速"]}}
setblock ~110 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:image_sensor",count:1}]}
setblock ~113 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-27] ドライエッチン","2台の GUI（「隣の","真空容器を 0.1Pa"," 以下に」「現像したレ"]}}
setblock ~113 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~113 ~0 ~74 semiconductor-lab:fluorination_reactor
setblock ~114 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~114 ~0 ~74 semiconductor-lab:dry_etcher
setblock ~115 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:carbon_tetrafluoride",count:16},{Slot:1b,id:"semiconductor-lab:graphite",count:16},{Slot:2b,id:"semiconductor-lab:fluorine_cylinder",count:16},{Slot:3b,id:"semiconductor-lab:process_wafer",count:16},{Slot:4b,id:"semiconductor-lab:chlorine_gas",count:16}]}
setblock ~118 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-26] スパッタリング","スパッタ装置の GUI","（「隣の真空容器を 0",".01Pa 以下に」）"]}}
setblock ~118 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~118 ~0 ~74 semiconductor-lab:sputter
setblock ~119 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:aluminium_ingot",count:16},{Slot:2b,id:"semiconductor-lab:argon_gas",count:16},{Slot:3b,id:"semiconductor-lab:tantalum",count:16}]}
setblock ~122 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-45] ベイヤー配列の","作業台で 緑・赤／青・","緑 に並べて4枚できる","か。画像センサーへの組"]}}
setblock ~122 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:red_color_resist",count:16},{Slot:1b,id:"semiconductor-lab:green_color_resist",count:16},{Slot:2b,id:"semiconductor-lab:blue_color_resist",count:16},{Slot:3b,id:"semiconductor-lab:bayer_filter",count:16},{Slot:4b,id:"semiconductor-lab:color_image_sensor",count:1}]}
setblock ~125 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-39] 縮小投影露光","ステッパーに電気をつな","ぎ、塗った板・クロムマ","スク・投影レンズ（・ボ"]}}
setblock ~125 ~0 ~74 semiconductor-lab:stepper
setblock ~126 ~0 ~74 semiconductor-lab:scanner
setblock ~130 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-29] アニール","アニール炉の GUI（","「レジストを先にはがす","」「注入した層がない」"]}}
setblock ~130 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~130 ~0 ~74 semiconductor-lab:anneal_furnace
setblock ~131 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:nitrogen_gas",count:16}]}
setblock ~134 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-31] CMP","CMP 装置の GUI","（「層間膜がない」「も","う平ら」など）、瓶が上"]}}
setblock ~134 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~134 ~0 ~74 semiconductor-lab:cmp_polisher
setblock ~135 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:colloidal_silica",count:16}]}
setblock ~138 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-41] パワー半導体と","インバータの向き（後ろ","＝直流、前＝交流）、前","と後ろを同じ網にすると"]}}
setblock ~138 ~0 ~74 semiconductor-lab:inverter
setblock ~142 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-33] 化学増幅レジス","合成釜の画面（反応ごと","の文・「下の置き場の物","が要る」）、ホットプレ"]}}
setblock ~142 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~142 ~0 ~74 semiconductor-lab:bake_plate
setblock ~143 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16}]}
setblock ~146 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-43] MEMS","画像センサーを持って昼","・夜・洞窟・たいまつの","近くで数が変わる、加速"]}}
setblock ~146 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mems_wafer",count:16},{Slot:1b,id:"semiconductor-lab:mems_accelerometer",count:1}]}
setblock ~149 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-36] 銅配線","めっき装置の GUI（","「⚠ バリア膜なし」）","、スパッタに Ta の"]}}
setblock ~149 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~149 ~0 ~74 semiconductor-lab:copper_plater
setblock ~150 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:copper_sulfate",count:16},{Slot:2b,id:"minecraft:copper_ingot",count:16}]}
setblock ~153 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-37] FinFET","ドライエッチング装置に"," Cl2 を入れたとき","「Cl2 でフィンを立"]}}
setblock ~153 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~153 ~0 ~74 semiconductor-lab:hafnium_chlorinator
setblock ~154 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~154 ~0 ~74 semiconductor-lab:ald_reactor
setblock ~155 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hafnium_oxide",count:16},{Slot:1b,id:"semiconductor-lab:coke",count:16},{Slot:2b,id:"semiconductor-lab:chlorine_gas",count:16},{Slot:3b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:4b,id:"semiconductor-lab:process_wafer",count:16},{Slot:5b,id:"semiconductor-lab:hafnium_tetrachloride",count:16},{Slot:6b,id:"semiconductor-lab:distilled_water",count:16}]}
setblock ~158 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-40] EUV 露光装","EUV 光源・真空容器","を隣に置いて、画面の「","EUV ○mW」とヒン"]}}
setblock ~158 ~0 ~74 semiconductor-lab:euv_scanner
setblock ~159 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:euv_mask",count:1}]}
setblock ~162 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] euv_mask_b","（人間の確認項目なし）","",""]}}
setblock ~162 ~0 ~74 semiconductor-lab:energy_port
setblock ~163 ~0 ~74 semiconductor-lab:item_port
setblock ~164 ~0 ~74 semiconductor-lab:gas_port
setblock ~165 ~0 ~74 semiconductor-lab:drafting_table
setblock ~166 ~0 ~74 semiconductor-lab:load_port
setblock ~167 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:euv_mask_blank",count:1},{Slot:1b,id:"semiconductor-lab:foup",count:1},{Slot:2b,id:"semiconductor-lab:hafnium_tetrachloride",count:16},{Slot:3b,id:"semiconductor-lab:stamp",count:1},{Slot:4b,id:"semiconductor-lab:layout_sheet",count:1}]}
setblock ~0 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹11 農業・食品","↓ この列","",""]}}
setblock ~2 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-08] 製粉","食べられるか・満腹度、","見た目、レシピ本に出る","か"]}}
setblock ~2 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:quern",count:1},{Slot:1b,id:"semiconductor-lab:flour",count:16}]}
setblock ~5 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-10] 酒と酢","かめに酒を入れて数分で","酢になるか、殺菌した酒","のメッセージ"]}}
setblock ~5 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vinegar",count:16}]}
setblock ~8 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-01] 灌漑","砂漠で水をひいた畑で麦","を何回か収穫して、塩の","耕地（白い粒）になるか"]}}
setblock ~8 ~0 ~82 semiconductor-lab:salty_farmland
setblock ~12 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-09] パン","食べられるか・満腹度、","見た目、レシピ本に出る","か"]}}
setblock ~12 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bread_dough",count:16},{Slot:1b,id:"semiconductor-lab:leavened_bread",count:16}]}
setblock ~15 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-02] 犂","草の地面に使って 3×","3 が耕地になるか、す","り減るか"]}}
setblock ~15 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:plow",count:1}]}
setblock ~18 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-11] 塩蔵・乾燥・","食べられるか・満腹度、","見た目、レシピ本に出る","か"]}}
setblock ~18 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:salted_meat",count:16},{Slot:1b,id:"semiconductor-lab:smoked_meat",count:16}]}
setblock ~21 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-14] 砂糖","たき火で汁から黒糖がで","きるか、見た目（茶色 ","→ 白）"]}}
setblock ~21 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cane_juice",count:16},{Slot:1b,id:"semiconductor-lab:beet_juice",count:16},{Slot:2b,id:"semiconductor-lab:raw_sugar",count:16},{Slot:3b,id:"semiconductor-lab:bone_char",count:16}]}
setblock ~24 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-03] 輪作","実際に同じ畑で小麦を3","回収穫して3回目から1","つ少ないか（MODIF"]}}
setblock ~24 ~0 ~82 semiconductor-lab:tired_farmland
setblock ~28 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-12] 缶詰","缶詰を食べられるか（満","腹度 9）、見た目（缶","・ボルドー液の青）、レ"]}}
setblock ~28 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tin_can",count:16},{Slot:1b,id:"semiconductor-lab:sealed_can",count:16},{Slot:2b,id:"semiconductor-lab:canned_meat",count:16}]}
setblock ~31 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-06] 過リン酸石灰","実際に麦・ニンジンを収","穫して1つ多いか（MO","DIFY_DROPS "]}}
setblock ~31 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:superphosphate",count:16}]}
setblock ~34 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-15] 低温殺菌","かめに酒を入れて数分で","酢になるか、殺菌した酒","のメッセージ"]}}
setblock ~34 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pasteurized_wine",count:16}]}
setblock ~37 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-13] 冷蔵・冷凍","冷凍機に電気をつなぎ、","アンモニア水と水バケツ","を入れて氷ができるか。"]}}
setblock ~37 ~0 ~82 semiconductor-lab:refrigerator
setblock ~41 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-16] ボルドー液","缶詰を食べられるか（満","腹度 9）、見た目（缶","・ボルドー液の青）、レ"]}}
setblock ~41 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bordeaux_mixture",count:16}]}
setblock ~44 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[11-07] 窒素肥料","実際に麦・ニンジンを収","穫して1つ多いか（MO","DIFY_DROPS "]}}
setblock ~44 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ammonium_nitrate",count:16}]}
setblock ~0 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹12 建設・測量","↓ この列","",""]}}
setblock ~2 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-01] ローマンコン","生コンを水の中に置いて","固まるか、砂のように落","ちるか、見た目"]}}
setblock ~2 ~0 ~90 semiconductor-lab:roman_concrete
setblock ~3 ~0 ~90 semiconductor-lab:roman_concrete_mix
setblock ~7 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-11] 測量","セオドライトで遠くの木","や塔をのぞいて離す →"," チャットに方位。横に"]}}
setblock ~7 ~0 ~90 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:theodolite",count:1}]}
setblock ~10 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-04] 鉄骨構造","縦・横に置いたときの見","た目（H の断面）",""]}}
setblock ~10 ~0 ~90 semiconductor-lab:steel_beam
setblock ~14 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-09] 道路","上を歩くと速くなるか、","見た目",""]}}
setblock ~14 ~0 ~90 semiconductor-lab:macadam
setblock ~15 ~0 ~90 semiconductor-lab:tarmac
setblock ~19 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-02] ポルトランド","レシピ本でセメントから","コンクリートパウダーが","作れるか"]}}
setblock ~19 ~0 ~90 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cement_raw_meal",count:16},{Slot:1b,id:"semiconductor-lab:cement_clinker",count:16},{Slot:2b,id:"semiconductor-lab:portland_cement",count:16}]}
setblock ~22 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-03] 鉄筋コンクリ","TNT の爆発でふつう","のコンクリートより残る","か"]}}
setblock ~22 ~0 ~90 semiconductor-lab:reinforced_concrete
setblock ~26 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-12] 換気・空調","マスクアライナーの近く","（8 ブロック以内）に","空調機を置いて電気を流"]}}
setblock ~26 ~0 ~90 semiconductor-lab:air_conditioner
setblock ~30 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-13] HEPA フ","ブッシングでガラス繊維"," → HEPA フィル","ター → 空調機の左上"]}}
setblock ~30 ~0 ~90 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:platinum_bushing",count:1},{Slot:1b,id:"semiconductor-lab:glass_fiber",count:16},{Slot:2b,id:"semiconductor-lab:hepa_filter",count:1}]}
setblock ~33 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-05] 板ガラス","レシピ本で MOD の","ガラスからガラス板が作","れるか"]}}
setblock ~33 ~0 ~90 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tin_bath",count:1}]}
setblock ~36 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-14] 層流クリーン","石で部屋を囲み、天井に"," HEPA の天井を並","べ、中に HEPA 入"]}}
setblock ~36 ~0 ~90 semiconductor-lab:hepa_ceiling
setblock ~0 ~0 ~97 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹14 交通・運輸","↓ この列","",""]}}
setblock ~2 ~0 ~97 minecraft:oak_sign[rotation=8]{front_text:{messages:["[14-03] 経度とクロノ","クロノメーターを持って","六分儀で南中を測ると経","度の文が出るか。x を"]}}
setblock ~2 ~0 ~98 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:marine_chronometer",count:1}]}
setblock ~5 ~0 ~97 minecraft:oak_sign[rotation=8]{front_text:{messages:["[14-13] ジュラルミン","焼入れしたジュラルミン","を持って1日待つと硬い","ジュラルミンに変わるか"]}}
setblock ~5 ~0 ~98 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:duralumin_charge",count:16},{Slot:1b,id:"semiconductor-lab:duralumin",count:16},{Slot:2b,id:"semiconductor-lab:quenched_duralumin",count:16}]}
setblock ~0 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹15 通信","↓ この列","",""]}}
setblock ~2 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-01] 紙","3つのアイテムの見た目","とツールチップ、パルプ","から紙ができるか（レシ"]}}
setblock ~2 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sulfite_liquor",count:16},{Slot:1b,id:"semiconductor-lab:pulp_charge",count:16},{Slot:2b,id:"semiconductor-lab:wood_pulp",count:16}]}
setblock ~5 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-04] 電話","（人間の確認項目なし）","",""]}}
setblock ~5 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:telephone",count:1}]}
setblock ~8 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-05] 蓄音機","蓄音機と白い筒を両手に","持ってチャット → 「","録音した」、右クリック"]}}
setblock ~8 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tin_foil_cylinder",count:1},{Slot:1b,id:"semiconductor-lab:phonograph",count:1}]}
setblock ~11 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-09] 真空管ラジオ","組立台で作れるか、鉛蓄","電池を持って右クリック","の文"]}}
setblock ~11 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tube_radio",count:1}]}
setblock ~14 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-10] トランジスタ","（人間の確認項目なし）","",""]}}
setblock ~14 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:transistor_radio",count:1}]}
setblock ~17 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-18] 静電容量式タ","右クリックで、人・動物","には数が出て、ブロック","・空では「反応なし」"]}}
setblock ~17 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:touch_panel",count:1}]}
setblock ~20 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-13] 光ファイバー","ファイバーでブロックを","2つ右クリックして届く","光の％、合成石英と天然"]}}
setblock ~20 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:fiber_preform",count:16},{Slot:1b,id:"semiconductor-lab:fiber_preform_natural",count:16},{Slot:2b,id:"semiconductor-lab:optical_fiber",count:16}]}
setblock ~0 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["枝 製品","↓ この列","",""]}}
setblock ~2 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-02] 銀塩写真 🌿✅","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:silvered_plate",count:16},{Slot:1b,id:"semiconductor-lab:daguerreotype_plate",count:16},{Slot:2b,id:"semiconductor-lab:exposed_plate",count:1},{Slot:3b,id:"semiconductor-lab:developed_plate",count:1},{Slot:4b,id:"semiconductor-lab:daguerreotype",count:1},{Slot:5b,id:"semiconductor-lab:exposed_dry_plate",count:1},{Slot:6b,id:"semiconductor-lab:developed_dry_plate",count:1},{Slot:7b,id:"semiconductor-lab:negative",count:1},{Slot:8b,id:"semiconductor-lab:printing_paper",count:16},{Slot:9b,id:"semiconductor-lab:print",count:16}]}
setblock ~5 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] camera","（人間の確認項目なし）","",""]}}
setblock ~5 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:camera",count:1}]}
setblock ~8 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-14] アセチレンラン","ランプの見た目（火のつ","いた・消えた）、夜に置","いて明るさ、数分で消え"]}}
setblock ~8 ~0 ~114 semiconductor-lab:carbide_lamp
setblock ~12 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-06] 偏光フィルム ","両手に持って右クリック","したときのメッセージ、","1枚だけのときのメッセ"]}}
setblock ~12 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pva_film",count:16},{Slot:1b,id:"semiconductor-lab:potassium_iodide",count:16},{Slot:2b,id:"semiconductor-lab:polarizing_film",count:16}]}
setblock ~15 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-12] 太陽電池","通しプレイ（多結晶Si"," → CZ（リンで n","型）→ 切断 → 拡散"]}}
setblock ~15 ~0 ~114 semiconductor-lab:solar_cell
setblock ~19 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-24] ニキシー管 🌿","合格したチップ（全加算","器・レジスタ×2・カウ","ンタ）とキー・ニキシー"]}}
setblock ~19 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nixie_tube",count:16}]}
setblock ~22 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-07] LED 🌿✅⚠","LED の明かりを置い","て電気を流すと赤く光る","（明るさ7）、化合物半"]}}
setblock ~22 ~0 ~114 semiconductor-lab:compound_furnace
setblock ~23 ~0 ~114 semiconductor-lab:led_lamp
setblock ~24 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:arsenic",count:16},{Slot:1b,id:"semiconductor-lab:gaasp_wafer",count:16},{Slot:2b,id:"semiconductor-lab:red_led",count:16}]}
setblock ~27 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-08] 電卓","合格したチップ（全加算","器・レジスタ×2・カウ","ンタ）とキー・ニキシー"]}}
setblock ~27 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:key_switch",count:16},{Slot:1b,id:"semiconductor-lab:calculator",count:1}]}
setblock ~30 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-18] 液晶ディスプレ","銅ケーブルで電気をつな","ぐと正面が暗くなり、切","ると明るくもどるか（見"]}}
setblock ~30 ~0 ~114 semiconductor-lab:lcd_cell
setblock ~31 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mbba",count:16},{Slot:1b,id:"semiconductor-lab:rubbed_ito_glass",count:16}]}
setblock ~34 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-09] ゲーム機","組立台でゲーム機を組み","、右クリックで卓球（左","の板がマウスに付いてく"]}}
setblock ~34 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:game_console",count:1}]}
setblock ~37 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-45] ベイヤー配列の","作業台で 緑・赤／青・","緑 に並べて4枚できる","か。画像センサーへの組"]}}
setblock ~37 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:camera_module",count:1}]}
setblock ~40 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-19] TFT 液晶","TFT 液晶を手に持つ","と画面の下に一言、スマ","ホの組立台に tft_"]}}
setblock ~40 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tft_array_glass",count:16}]}
setblock ~43 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-20] 白色 LED ","白色 LED の明かり","（明るさ15）、LED"," の明かり（赤、明るさ"]}}
setblock ~43 ~0 ~114 semiconductor-lab:white_led_lamp
setblock ~44 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:gan_wafer",count:16},{Slot:1b,id:"semiconductor-lab:blue_led",count:16},{Slot:2b,id:"semiconductor-lab:yag_phosphor",count:16},{Slot:3b,id:"semiconductor-lab:white_led",count:16}]}
setblock ~47 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-10] スマホ","組立台でスマホを組み（","マイコン mcu4・カ","メラ・加速度計・タッチ"]}}
setblock ~47 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tft_lcd",count:16},{Slot:1b,id:"semiconductor-lab:led_backlight",count:16},{Slot:2b,id:"semiconductor-lab:smartphone",count:1}]}
setblock ~50 ~0 ~113 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-11] AIチップ","組立台で AI チップ","を組み、右クリックで「","見本の 7 を読んだ "]}}
setblock ~50 ~0 ~114 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ai_chip",count:1}]}
setblock ~0 ~0 ~121 minecraft:oak_sign[rotation=8]{front_text:{messages:["本物の発電","↓ この列","",""]}}
setblock ~2 ~0 ~121 minecraft:oak_sign[rotation=8]{front_text:{messages:["蒸気機関で発電","石炭と水バケツ","を入れると","電気炉が動く"]}}
setblock ~2 ~0 ~122 semiconductor-lab:steam_engine
setblock ~2 ~0 ~123 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"minecraft:coal",count:32},{Slot:1b,id:"minecraft:water_bucket",count:1},{Slot:2b,id:"minecraft:water_bucket",count:1},{Slot:3b,id:"minecraft:water_bucket",count:1},{Slot:4b,id:"minecraft:water_bucket",count:1}]}
setblock ~3 ~0 ~122 semiconductor-lab:copper_cable
setblock ~4 ~0 ~122 semiconductor-lab:copper_cable
setblock ~5 ~0 ~122 semiconductor-lab:copper_cable
setblock ~6 ~0 ~122 semiconductor-lab:electric_furnace
setblock ~7 ~0 ~122 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:refractory_brick_blank",count:16}]}
execute if entity @s run tellraw @s {"text":"試験場ができた：列ごとに幹、看板に「何を確かめるか」。全文は docs/TESTWORLD.md","color":"green"}
