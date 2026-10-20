# 試験場（S-001、IMP3）：自動生成 python tools/gen_testworld.py。手で直さない
# 使い方：新しいワールド（クリエイティブ・コマンド有効）の平らな所で /function semiconductor-lab:testworld
# 大きさ：東へ 122・南へ 109 ブロック（足元から）。機械の真下のピンクと黒は試験用の無限電源
fill ~-1 ~-1 ~-2 ~122 ~-1 ~5 minecraft:smooth_stone
fill ~-1 ~ ~-2 ~122 ~5 ~5 minecraft:air
fill ~-1 ~-1 ~6 ~122 ~-1 ~13 minecraft:smooth_stone
fill ~-1 ~ ~6 ~122 ~5 ~13 minecraft:air
fill ~-1 ~-1 ~14 ~122 ~-1 ~21 minecraft:smooth_stone
fill ~-1 ~ ~14 ~122 ~5 ~21 minecraft:air
fill ~-1 ~-1 ~22 ~122 ~-1 ~29 minecraft:smooth_stone
fill ~-1 ~ ~22 ~122 ~5 ~29 minecraft:air
fill ~-1 ~-1 ~30 ~122 ~-1 ~37 minecraft:smooth_stone
fill ~-1 ~ ~30 ~122 ~5 ~37 minecraft:air
fill ~-1 ~-1 ~38 ~122 ~-1 ~45 minecraft:smooth_stone
fill ~-1 ~ ~38 ~122 ~5 ~45 minecraft:air
fill ~-1 ~-1 ~46 ~122 ~-1 ~53 minecraft:smooth_stone
fill ~-1 ~ ~46 ~122 ~5 ~53 minecraft:air
fill ~-1 ~-1 ~54 ~122 ~-1 ~61 minecraft:smooth_stone
fill ~-1 ~ ~54 ~122 ~5 ~61 minecraft:air
fill ~-1 ~-1 ~62 ~122 ~-1 ~69 minecraft:smooth_stone
fill ~-1 ~ ~62 ~122 ~5 ~69 minecraft:air
fill ~-1 ~-1 ~70 ~122 ~-1 ~77 minecraft:smooth_stone
fill ~-1 ~ ~70 ~122 ~5 ~77 minecraft:air
fill ~-1 ~-1 ~78 ~122 ~-1 ~85 minecraft:smooth_stone
fill ~-1 ~ ~78 ~122 ~5 ~85 minecraft:air
fill ~-1 ~-1 ~86 ~122 ~-1 ~93 minecraft:smooth_stone
fill ~-1 ~ ~86 ~122 ~5 ~93 minecraft:air
fill ~-1 ~-1 ~94 ~122 ~-1 ~101 minecraft:smooth_stone
fill ~-1 ~ ~94 ~122 ~5 ~101 minecraft:air
fill ~-1 ~-1 ~102 ~122 ~-1 ~109 minecraft:smooth_stone
fill ~-1 ~ ~102 ~122 ~5 ~109 minecraft:air
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
setblock ~48 ~0 ~2 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vacuum_tube",count:16},{Slot:1b,id:"semiconductor-lab:induction_coil",count:16},{Slot:2b,id:"semiconductor-lab:leyden_jar",count:16},{Slot:3b,id:"semiconductor-lab:copper_cable",count:16},{Slot:4b,id:"minecraft:iron_ingot",count:16},{Slot:5b,id:"semiconductor-lab:junction_transistor",count:16},{Slot:6b,id:"semiconductor-lab:graphite",count:16},{Slot:7b,id:"semiconductor-lab:magnet",count:16},{Slot:8b,id:"semiconductor-lab:zinc_ingot",count:16},{Slot:9b,id:"semiconductor-lab:graphite_rod",count:16}]}
setblock ~0 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹1 冶金","↓ この列","",""]}}
setblock ~2 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-03] 銅の製錬","新しいワールドの浅い所","（y40〜110）で孔","雀石の鉱石が見つかるか"]}}
setblock ~2 ~0 ~10 semiconductor-lab:malachite_ore
setblock ~3 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:malachite",count:16}]}
setblock ~6 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-27] 鉛","一酸化鉛（黄色い粉）・","鉛インゴット（青灰色）","のテクスチャと名前。へ"]}}
setblock ~6 ~0 ~10 semiconductor-lab:galena_ore
setblock ~7 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:galena",count:16},{Slot:1b,id:"semiconductor-lab:litharge",count:16},{Slot:2b,id:"semiconductor-lab:lead_ingot",count:16},{Slot:3b,id:"semiconductor-lab:lead_bottle",count:16}]}
setblock ~10 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-22] 錫石 SnO2","花崗岩の多い所で錫石の","鉱石が見つかるか（鉄マ","ンガン重石と同じくらい"]}}
setblock ~10 ~0 ~10 semiconductor-lab:cassiterite_ore
setblock ~11 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cassiterite",count:16}]}
setblock ~14 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-30] 錫","スズのインゴットの見た","目・ツールチップ。レシ","ピ本に出るか"]}}
setblock ~14 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:tin_ingot",count:16}]}
setblock ~17 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-05] 青銅","青銅インゴットの見た目","（銅と見分けがつくか）",""]}}
setblock ~17 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bronze_charge",count:16},{Slot:1b,id:"semiconductor-lab:bronze_ingot",count:16}]}
setblock ~20 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-32] 銀","レシピ本に灰吹きの流れ","が出るか。銀の見た目",""]}}
setblock ~20 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bone_ash",count:16},{Slot:1b,id:"semiconductor-lab:cupel",count:16},{Slot:2b,id:"semiconductor-lab:cupel_charge",count:16},{Slot:3b,id:"semiconductor-lab:silver_nugget",count:16},{Slot:4b,id:"semiconductor-lab:silver_ingot",count:16}]}
setblock ~23 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-46] 珪石","新しいワールドで、平原","・森・山など砂漠以外で","も石英鉱脈（灰色に白い"]}}
setblock ~23 ~0 ~10 semiconductor-lab:quartz_vein
setblock ~24 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vein_quartz",count:16}]}
setblock ~27 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-06] 塊鉄炉","塊鉄炉の画面で温度計が","上がり、ふいごを踏むの","をやめると下がるか。赤"]}}
setblock ~27 ~0 ~10 semiconductor-lab:bloomery
setblock ~28 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:iron_bloom",count:16},{Slot:1b,id:"minecraft:raw_iron",count:16},{Slot:2b,id:"minecraft:charcoal",count:16}]}
setblock ~31 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-08] 鍛鉄","ブルームをかまどに入れ","ると赤く光るブルームが","出て、たき石と作業台に"]}}
setblock ~31 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hot_iron_bloom",count:16}]}
setblock ~34 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-47] 天然磁石","（人間の確認項目なし）","",""]}}
setblock ~34 ~0 ~10 semiconductor-lab:magnetite_ore
setblock ~35 ~0 ~10 semiconductor-lab:iron_sand
setblock ~36 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:magnetite",count:16}]}
setblock ~39 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-45] 水銀","真空計を持って容器に右","クリック → チャット","に「水銀柱の差 760"]}}
setblock ~39 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:iron_flask",count:16},{Slot:1b,id:"semiconductor-lab:cinnabar_dust",count:16},{Slot:2b,id:"semiconductor-lab:mercury",count:16},{Slot:3b,id:"semiconductor-lab:cinnabar_retort_charge",count:16}]}
setblock ~42 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-09] 高炉","炉床を置いて画面を開く","と、足りない場所に粒子","が出て「あと N か所"]}}
setblock ~42 ~0 ~10 semiconductor-lab:tuyere
setblock ~43 ~0 ~10 semiconductor-lab:blast_furnace_hearth
setblock ~44 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pig_iron",count:16},{Slot:1b,id:"minecraft:raw_iron",count:16},{Slot:2b,id:"minecraft:charcoal",count:16},{Slot:3b,id:"minecraft:calcite",count:16}]}
setblock ~47 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-13] 亜鉛","（人間の確認項目なし）","",""]}}
setblock ~47 ~0 ~10 semiconductor-lab:sphalerite_ore
setblock ~48 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sphalerite",count:16},{Slot:1b,id:"semiconductor-lab:zinc_ingot",count:16}]}
setblock ~51 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-21] 蛍石 CaF2","鉱石・蛍石の見た目（紫","と緑のつぶ）、石のツル","ハシで掘れて木では掘れ"]}}
setblock ~51 ~0 ~10 semiconductor-lab:fluorite_ore
setblock ~52 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:fluorite",count:16}]}
setblock ~55 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-10] コークス高炉","E のガス工場のレトル","トで作ったコークスを高","炉の燃料のスロットに入"]}}
setblock ~55 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coke",count:16}]}
setblock ~58 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-20] 白金","浜の砂鉄を掘ったときに","たまに出るか",""]}}
setblock ~58 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:platinum_grain",count:16}]}
setblock ~61 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-18] 白金精製","作業台で瓶がちゃんと手","元に戻るか、色（だいだ","いの液・黄色い粉・銀色"]}}
setblock ~61 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ammonium_chloroplatinate",count:16},{Slot:1b,id:"semiconductor-lab:platinum_sponge",count:16},{Slot:2b,id:"semiconductor-lab:platinum_compact",count:16},{Slot:3b,id:"semiconductor-lab:platinum_ingot",count:16}]}
setblock ~64 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-33] ホウ素","新しいワールドの砂漠の","地表近く（y56〜76","）でホウ砂の堆積（白い"]}}
setblock ~64 ~0 ~10 semiconductor-lab:borax_deposit
setblock ~65 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:borax",count:16},{Slot:1b,id:"semiconductor-lab:boric_acid",count:16},{Slot:2b,id:"semiconductor-lab:boron_trioxide",count:16}]}
setblock ~68 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-24] ボーキサイト","ジャングル・サバンナで","地表近くに見つかるか、","赤茶色の見た目、木のツ"]}}
setblock ~68 ~0 ~10 semiconductor-lab:bauxite_ore
setblock ~69 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:bauxite",count:16}]}
setblock ~72 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-07] 黒鉛","（人間の確認項目なし）","",""]}}
setblock ~72 ~0 ~10 semiconductor-lab:graphite_ore
setblock ~73 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:graphite",count:16}]}
setblock ~76 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-26] 鋼","見た目：正面に黒い炉口","、上面に内張り、下面に","羽口。置いた向きに正面"]}}
setblock ~76 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~76 ~0 ~10 semiconductor-lab:bessemer_converter
setblock ~77 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:steel_ingot",count:16},{Slot:1b,id:"semiconductor-lab:pig_iron",count:16}]}
setblock ~80 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-38] 氷晶石の合成","作業台で作ったとき鉛の","瓶が3本手元に戻る",""]}}
setblock ~80 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cryolite",count:16}]}
setblock ~83 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-29] アルミニウム","電解槽の GUI（左に","アルミナ・氷晶石・黒鉛","棒、オレンジのバー、浴"]}}
setblock ~83 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~83 ~0 ~10 semiconductor-lab:hall_heroult_cell
setblock ~84 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:aluminium_ingot",count:16},{Slot:1b,id:"semiconductor-lab:alumina",count:16},{Slot:2b,id:"semiconductor-lab:cryolite",count:16},{Slot:3b,id:"semiconductor-lab:graphite_rod",count:16}]}
setblock ~87 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-14] バイヤー法","溶解釜の GUI（左に","3つ、右に出口3つ、液","の残り回数・電力・状態"]}}
setblock ~87 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~87 ~0 ~10 semiconductor-lab:bayer_digester
setblock ~88 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:aluminium_hydroxide",count:16},{Slot:1b,id:"semiconductor-lab:alumina",count:16},{Slot:2b,id:"semiconductor-lab:red_mud",count:16},{Slot:3b,id:"semiconductor-lab:bauxite",count:16},{Slot:4b,id:"semiconductor-lab:sodium_hydroxide",count:16},{Slot:5b,id:"minecraft:water_bucket",count:1}]}
setblock ~91 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-15] 金属シリコン","アーク炉の GUI（左","に珪石・木炭・電極、青","白いバー、右に電力・必"]}}
setblock ~91 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:metallurgical_silicon",count:16}]}
setblock ~94 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-28] タングステン線","水素炉の見た目（正面の","窓が加熱中に橙色に光る","・置いた向き）、GUI"]}}
setblock ~94 ~0 ~10 semiconductor-lab:wolframite_ore
setblock ~95 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~95 ~0 ~10 semiconductor-lab:hydrogen_furnace
setblock ~96 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wolframite",count:16},{Slot:1b,id:"semiconductor-lab:wolframite_soda_charge",count:16},{Slot:2b,id:"semiconductor-lab:sodium_tungstate",count:16},{Slot:3b,id:"semiconductor-lab:tungstic_acid",count:16},{Slot:4b,id:"semiconductor-lab:tungsten_trioxide",count:16},{Slot:5b,id:"semiconductor-lab:tungsten_powder",count:16},{Slot:6b,id:"semiconductor-lab:tungsten_bar",count:16},{Slot:7b,id:"semiconductor-lab:tungsten_wire",count:16},{Slot:8b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:9b,id:"semiconductor-lab:germanium_dioxide",count:16}]}
setblock ~99 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-15] 紫外光学材料","ブリッジマン炉の画面（","左上に蛍石・左下にアル","ゴン、電力・状態の文字"]}}
setblock ~99 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:silicon_tetrachloride",count:16}]}
setblock ~102 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-01] 石炭灰からのゲ","（人間の確認項目なし）","",""]}}
setblock ~102 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coal_ash",count:16},{Slot:1b,id:"semiconductor-lab:germanium_concentrate",count:16},{Slot:2b,id:"semiconductor-lab:germanium_dioxide",count:16},{Slot:3b,id:"semiconductor-lab:raw_germanium",count:16}]}
setblock ~105 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-34] 四塩化ゲルマニ","（人間の確認項目なし）","",""]}}
setblock ~105 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:crude_germanium_tetrachloride",count:16},{Slot:1b,id:"semiconductor-lab:germanium_tetrachloride",count:16},{Slot:2b,id:"semiconductor-lab:high_purity_germanium_dioxide",count:16},{Slot:3b,id:"semiconductor-lab:quartz_still",count:1}]}
setblock ~108 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-02] ゾーンレベリン","（人間の確認項目なし）","",""]}}
setblock ~108 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:indium",count:16}]}
setblock ~111 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-16] トリクロロシラ","トリクロロシラン反応器","の GUI（左に Si","・HCl・蒸留するTC"]}}
setblock ~111 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~111 ~0 ~10 semiconductor-lab:trichlorosilane_reactor
setblock ~112 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrogen_chloride_gas",count:16},{Slot:1b,id:"semiconductor-lab:trichlorosilane",count:16},{Slot:2b,id:"semiconductor-lab:metallurgical_silicon",count:16}]}
setblock ~115 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-17] シーメンス法","シーメンス炉の GUI","（左に TCS・H2、","出口に多結晶シリコン・"]}}
setblock ~115 ~-1 ~10 semiconductor-lab:test_power_source
setblock ~115 ~0 ~10 semiconductor-lab:siemens_reactor
setblock ~116 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:polysilicon",count:16},{Slot:1b,id:"semiconductor-lab:trichlorosilane",count:16},{Slot:2b,id:"semiconductor-lab:hydrogen_gas",count:16}]}
setblock ~119 ~0 ~9 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] blast_furn","（人間の確認項目なし）","",""]}}
setblock ~119 ~0 ~10 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:blast_furnace_slag",count:16}]}
setblock ~0 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹2 化学","↓ この列","",""]}}
setblock ~2 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-02] カリ","たき火に原木を置けるか","、30秒で木灰が落ちる","か。レシピ本に灰汁・炭"]}}
setblock ~2 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wood_ash",count:16},{Slot:1b,id:"semiconductor-lab:lye",count:16},{Slot:2b,id:"semiconductor-lab:potash",count:16}]}
setblock ~5 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-03] 天然ソーダ","（人間の確認項目なし）","",""]}}
setblock ~5 ~0 ~18 semiconductor-lab:trona_deposit
setblock ~6 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:trona",count:16},{Slot:1b,id:"semiconductor-lab:sodium_carbonate",count:16}]}
setblock ~9 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-14] ソーダ石灰ガラ","（人間の確認項目なし）","",""]}}
setblock ~9 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:1b,id:"semiconductor-lab:glass_batch",count:16},{Slot:2b,id:"semiconductor-lab:soda_lime_glass",count:16}]}
setblock ~12 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-16] 蒸留水","作業台で蒸留器とバケツ","が手元に戻るか",""]}}
setblock ~12 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:distilled_water",count:16}]}
setblock ~15 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-23] 木材乾留","乾留炉の GUI（左に","原木・燃料、火の縦バー","、茶色のバー、右に木炭"]}}
setblock ~15 ~0 ~18 semiconductor-lab:wood_retort
setblock ~16 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wood_vinegar",count:16},{Slot:1b,id:"semiconductor-lab:wood_tar",count:16},{Slot:2b,id:"semiconductor-lab:pitch",count:16},{Slot:3b,id:"minecraft:oak_log",count:16},{Slot:4b,id:"minecraft:coal",count:16}]}
setblock ~19 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-04] 海藻灰ソーダ","たき火に乾燥昆布を置け","るか。ヨウ素の見た目",""]}}
setblock ~19 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:kelp_ash",count:16},{Slot:1b,id:"semiconductor-lab:kelp_liquor",count:16},{Slot:2b,id:"semiconductor-lab:iodine",count:16}]}
setblock ~22 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-10] フッ酸","鉛のレトルトの GUI","（上の段に蛍石・硫酸・","鉛の瓶、下に水・燃料、"]}}
setblock ~22 ~0 ~18 semiconductor-lab:lead_retort
setblock ~23 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrofluoric_acid",count:16},{Slot:1b,id:"semiconductor-lab:calcium_sulfate",count:16},{Slot:2b,id:"semiconductor-lab:fluorite",count:16},{Slot:3b,id:"semiconductor-lab:sulfuric_acid",count:16},{Slot:4b,id:"semiconductor-lab:lead_bottle",count:16},{Slot:5b,id:"minecraft:water_bucket",count:1},{Slot:6b,id:"minecraft:coal",count:16}]}
setblock ~26 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-20] コールタール","ガス工場のレトルトの ","GUI（左に石炭・燃料","、火の縦バー、黒い進み"]}}
setblock ~26 ~0 ~18 semiconductor-lab:gas_retort
setblock ~27 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coal_tar",count:16},{Slot:1b,id:"semiconductor-lab:ammoniacal_liquor",count:16},{Slot:2b,id:"minecraft:coal",count:16}]}
setblock ~30 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-18] 白金精製","作業台で瓶がちゃんと手","元に戻るか、色（だいだ","いの液・黄色い粉・銀色"]}}
setblock ~30 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ammonium_chloride",count:16},{Slot:1b,id:"semiconductor-lab:chloroplatinic_acid",count:16}]}
setblock ~33 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-19] 過酸化水素","電解槽の GUI（左に","硫酸・白金・水、2列目","に空の瓶、右に出口3つ"]}}
setblock ~33 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~33 ~0 ~18 semiconductor-lab:peroxide_cell
setblock ~34 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrogen_peroxide",count:16},{Slot:1b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:2b,id:"minecraft:water_bucket",count:1},{Slot:3b,id:"semiconductor-lab:sulfuric_acid",count:16},{Slot:4b,id:"semiconductor-lab:platinum_ingot",count:16}]}
setblock ~37 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-31] 光で固まる膜：","ヘリオグラフの板を日な","たに置き、数分で色が明","るくなるか（4段）。ガ"]}}
setblock ~37 ~0 ~18 semiconductor-lab:natural_asphalt
setblock ~38 ~0 ~18 semiconductor-lab:heliograph_plate
setblock ~39 ~0 ~18 semiconductor-lab:heliograph
setblock ~40 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:asphalt",count:16},{Slot:1b,id:"semiconductor-lab:pine_resin",count:16},{Slot:2b,id:"semiconductor-lab:turpentine",count:16},{Slot:3b,id:"semiconductor-lab:bitumen_varnish",count:16},{Slot:4b,id:"semiconductor-lab:pewter_plate",count:16}]}
setblock ~43 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-01] 硫酸","（人間の確認項目なし）","",""]}}
setblock ~43 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sulfur_powder",count:16},{Slot:1b,id:"semiconductor-lab:sulfur_dioxide",count:16},{Slot:2b,id:"semiconductor-lab:sulfuric_acid",count:16}]}
setblock ~46 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-26] メタノール","メタノールのツールチッ","プ（GHS の注意・「","飲まない」）と見た目"]}}
setblock ~46 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:limed_wood_liquor",count:16},{Slot:1b,id:"semiconductor-lab:methanol",count:16}]}
setblock ~49 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-29] ゴム","新しいワールドのジャン","グルにゴムの木が生えて","いるか（多すぎ・少なす"]}}
setblock ~49 ~0 ~18 semiconductor-lab:rubber_log
setblock ~50 ~0 ~18 semiconductor-lab:rubber_leaves
setblock ~51 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rubber_sapling",count:16},{Slot:1b,id:"semiconductor-lab:latex",count:16},{Slot:2b,id:"semiconductor-lab:raw_rubber",count:16},{Slot:3b,id:"semiconductor-lab:rubber_compound",count:16},{Slot:4b,id:"semiconductor-lab:vulcanized_rubber",count:16},{Slot:5b,id:"semiconductor-lab:cyclized_rubber_mix",count:16},{Slot:6b,id:"semiconductor-lab:cyclized_rubber",count:16}]}
setblock ~54 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-21] アンモニア水","アンモニア蒸留器の G","UI（左に4つ、右に出","口2つ、水の残り・電力"]}}
setblock ~54 ~-1 ~18 semiconductor-lab:test_power_source
setblock ~54 ~0 ~18 semiconductor-lab:ammonia_still
setblock ~55 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:ammonia_water",count:16},{Slot:1b,id:"semiconductor-lab:ammoniacal_liquor",count:16},{Slot:2b,id:"semiconductor-lab:slaked_lime",count:16},{Slot:3b,id:"semiconductor-lab:glass_bottle",count:16},{Slot:4b,id:"minecraft:water_bucket",count:1}]}
setblock ~58 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[2-27] ホルムアルデヒ","たき火の上に銅の網を置","き、メタノールを持って","右クリック → ホルマ"]}}
setblock ~58 ~0 ~18 semiconductor-lab:copper_gauze
setblock ~59 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:formalin",count:16}]}
setblock ~62 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-11] ドーパント源","ドーパントの付いた結晶","のツールチップ（p型は","赤・n型は青の文字、S"]}}
setblock ~62 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:phosphorus",count:16}]}
setblock ~65 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-25] 金属配線","真空蒸着装置に加工中の","ウェハーとアルミを入れ","たときの画面（入口に入"]}}
setblock ~65 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:phosphoric_acid",count:16}]}
setblock ~68 ~0 ~17 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-13] ラッピング・鏡","研磨機の GUI（左に","ウェハー・砥粒・シリカ","、銀色のバー、「やり直"]}}
setblock ~68 ~0 ~18 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:sodium_silicate",count:16},{Slot:1b,id:"semiconductor-lab:colloidal_silica",count:16}]}
setblock ~0 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹3 熱","↓ この列","",""]}}
setblock ~2 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-04] ふいご","ふいごを素手で右クリッ","クすると音と「送風 1","00%」が出て、1秒ほ"]}}
setblock ~2 ~0 ~26 semiconductor-lab:bellows
setblock ~6 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-06] 反射炉","（人間の確認項目なし）","",""]}}
setblock ~6 ~0 ~26 semiconductor-lab:reverberatory_furnace
setblock ~7 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:zinc_oxide",count:16},{Slot:1b,id:"semiconductor-lab:galena",count:16},{Slot:2b,id:"minecraft:coal",count:16},{Slot:3b,id:"semiconductor-lab:cinnabar_retort_charge",count:16},{Slot:4b,id:"semiconductor-lab:sphalerite",count:16}]}
setblock ~10 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-12] 耐火れんが","耐火れんが（未焼成）/","耐火れんが の名前・テ","クスチャ（白っぽい薄黄"]}}
setblock ~10 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:refractory_brick_blank",count:16},{Slot:1b,id:"semiconductor-lab:refractory_brick",count:16}]}
setblock ~13 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-15] アーク炉","アーク炉の GUI（左","に珪石・木炭・電極、青","白いバー、右に電力・必"]}}
setblock ~13 ~-1 ~26 semiconductor-lab:test_power_source
setblock ~13 ~0 ~26 semiconductor-lab:arc_furnace
setblock ~14 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vein_quartz",count:16},{Slot:1b,id:"minecraft:charcoal",count:16},{Slot:2b,id:"semiconductor-lab:graphite_rod",count:16}]}
setblock ~17 ~0 ~25 minecraft:oak_sign[rotation=8]{front_text:{messages:["[3-10] アチソン炉","GUI（左上＝炭素「C","」、左下＝砂「SiO2","」、中央＝進み具合バー"]}}
setblock ~17 ~-1 ~26 semiconductor-lab:test_power_source
setblock ~17 ~0 ~26 semiconductor-lab:acheson_furnace
setblock ~18 ~0 ~26 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:synthetic_graphite",count:16},{Slot:1b,id:"minecraft:coal",count:16},{Slot:2b,id:"minecraft:sand",count:16}]}
setblock ~0 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹4 電気・動力","↓ この列","",""]}}
setblock ~2 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[1-47] 天然磁石","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:magnet",count:16}]}
setblock ~5 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-08] 電動モーター","（人間の確認項目なし）","",""]}}
setblock ~5 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:electric_motor",count:16}]}
setblock ~8 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-04] ダイナモ・天然","（人間の確認項目なし）","",""]}}
setblock ~8 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:dynamo",count:16}]}
setblock ~11 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-07] 鉛蓄電池","（人間の確認項目なし）","",""]}}
setblock ~11 ~-1 ~34 semiconductor-lab:test_power_source
setblock ~11 ~0 ~34 semiconductor-lab:lead_acid_battery
setblock ~15 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-05] 自励式発電機","（人間の確認項目なし）","",""]}}
setblock ~15 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:self_excited_dynamo",count:16}]}
setblock ~18 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-01] 水車発電機","（人間の確認項目なし）","",""]}}
setblock ~18 ~0 ~34 semiconductor-lab:water_wheel
setblock ~22 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-14] 蒸気発電","（人間の確認項目なし）","",""]}}
setblock ~22 ~0 ~34 semiconductor-lab:steam_engine
setblock ~23 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"minecraft:coal",count:16},{Slot:1b,id:"minecraft:water_bucket",count:1}]}
setblock ~26 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-03] 銅ケーブル","（人間の確認項目なし）","",""]}}
setblock ~26 ~0 ~34 semiconductor-lab:copper_cable
setblock ~30 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-06] 蒸気タービン","（人間の確認項目なし）","",""]}}
setblock ~30 ~0 ~34 semiconductor-lab:steam_turbine
setblock ~31 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"minecraft:coal",count:16},{Slot:1b,id:"minecraft:water_bucket",count:1}]}
setblock ~34 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-12] 電気炉","電気炉の見た目（れんが","色、正面の窓が加熱中に","橙）、GUI（タイトル"]}}
setblock ~34 ~-1 ~34 semiconductor-lab:test_power_source
setblock ~34 ~0 ~34 semiconductor-lab:electric_furnace
setblock ~35 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:graphite_rod",count:16},{Slot:1b,id:"semiconductor-lab:graphite_boat_blank",count:16},{Slot:2b,id:"semiconductor-lab:refractory_brick_blank",count:16}]}
setblock ~38 ~0 ~33 minecraft:oak_sign[rotation=8]{front_text:{messages:["[4-09] 高周波発振器","レシピ本で帯域精製炉の","右下が「高周波発振器」","になっている。発振器・"]}}
setblock ~38 ~0 ~34 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:induction_coil",count:16},{Slot:1b,id:"semiconductor-lab:hf_oscillator",count:16},{Slot:2b,id:"semiconductor-lab:leyden_jar",count:16}]}
setblock ~0 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹5 真空","↓ この列","",""]}}
setblock ~2 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-01] 真空ポンプ →","容器の隣でポンプを素手","で右クリック → アク","ションバーに「シュッ…"]}}
setblock ~2 ~0 ~42 semiconductor-lab:hand_vacuum_pump
setblock ~6 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-11] 真空容器","（人間の確認項目なし）","",""]}}
setblock ~6 ~0 ~42 semiconductor-lab:vacuum_chamber
setblock ~10 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-15] シュプレンゲル","真空容器の横にポンプを","置き、水銀を持って右ク","リック（「水銀が足りな"]}}
setblock ~10 ~0 ~42 semiconductor-lab:sprengel_pump
setblock ~11 ~0 ~42 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mercury",count:16}]}
setblock ~14 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-10] 真空計","真空計を持って容器に右","クリック → チャット","に「水銀柱の差 760"]}}
setblock ~14 ~0 ~42 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mercury_vacuum_gauge",count:1}]}
setblock ~17 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-02] 真空管 → 幹","封止台を真空容器の隣に","置き、ケーブルで電気を","送る。GUI（タイトル"]}}
setblock ~17 ~-1 ~42 semiconductor-lab:test_power_source
setblock ~17 ~0 ~42 semiconductor-lab:sealing_station
setblock ~18 ~0 ~42 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vacuum_tube_assembly",count:16},{Slot:1b,id:"semiconductor-lab:vacuum_tube",count:16}]}
setblock ~21 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-04] 油回転ポンプ","真空容器｜拡散ポンプ｜","回転ポンプ と並べて電","気をつなぐ → 回転ポ"]}}
setblock ~21 ~0 ~42 semiconductor-lab:rotary_pump
setblock ~25 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-14] 真空蒸着","真空容器｜拡散ポンプ｜","回転ポンプ の容器に蒸","着装置もとなりに置いて"]}}
setblock ~25 ~0 ~42 semiconductor-lab:evaporator
setblock ~29 ~0 ~41 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-05] 水銀拡散ポンプ","真空容器｜拡散ポンプ｜","回転ポンプ と並べて電","気をつなぐ → 回転ポ"]}}
setblock ~29 ~0 ~42 semiconductor-lab:diffusion_pump
setblock ~0 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹6 ガス・電解","↓ この列","",""]}}
setblock ~2 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-04] 岩塩鉱","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~50 semiconductor-lab:rock_salt_ore
setblock ~3 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rock_salt",count:16},{Slot:1b,id:"semiconductor-lab:salt",count:16}]}
setblock ~6 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-16] 天然硝石の採取","新しいワールドの鍾乳石","の洞窟で硝石土が見つか","るか、多すぎないか。見"]}}
setblock ~6 ~0 ~50 semiconductor-lab:nitre_earth_block
setblock ~7 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nitre_earth",count:16},{Slot:1b,id:"semiconductor-lab:nitre_liquor",count:16},{Slot:2b,id:"semiconductor-lab:saltpeter",count:16}]}
setblock ~10 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-11] 硝酸","作業台で蒸留器・空き瓶","がちゃんと手元に戻るか","（シフトクリックでまと"]}}
setblock ~10 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nitric_acid",count:16},{Slot:1b,id:"semiconductor-lab:silver_nitrate",count:16}]}
setblock ~13 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-01] 電解槽","右クリックで GUI（","タイトル「電解槽」、背","景のスロット枠と実際の"]}}
setblock ~13 ~-1 ~50 semiconductor-lab:test_power_source
setblock ~13 ~0 ~50 semiconductor-lab:electrolyzer
setblock ~14 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:1b,id:"semiconductor-lab:oxygen_gas",count:16},{Slot:2b,id:"minecraft:water_bucket",count:1},{Slot:3b,id:"semiconductor-lab:sulfuric_acid",count:16},{Slot:4b,id:"semiconductor-lab:salt",count:16}]}
setblock ~17 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-02] ガスホルダー","見た目：側面（上が鉄の","ベル・下が青い水）、上","面、下面、インベントリ"]}}
setblock ~17 ~0 ~50 semiconductor-lab:gas_holder
setblock ~21 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-03] 圧縮機","圧縮機の見た目（正面の","窓が動作中に青）、GU","I（タイトル「圧縮機」"]}}
setblock ~21 ~-1 ~50 semiconductor-lab:test_power_source
setblock ~21 ~0 ~50 semiconductor-lab:compressor
setblock ~22 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:empty_gas_cylinder",count:16},{Slot:1b,id:"semiconductor-lab:hydrogen_cylinder",count:16},{Slot:2b,id:"semiconductor-lab:oxygen_cylinder",count:16}]}
setblock ~25 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-05] クロルアルカリ","（人間の確認項目なし）","",""]}}
setblock ~25 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:chlorine_gas",count:16},{Slot:1b,id:"semiconductor-lab:chlorine_cylinder",count:16},{Slot:2b,id:"semiconductor-lab:sodium_hydroxide",count:16}]}
setblock ~28 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-07] 空気分離","空気分離装置の GUI","（左に縦の青いバーが下","からたまる、出口3つに"]}}
setblock ~28 ~-1 ~50 semiconductor-lab:test_power_source
setblock ~28 ~0 ~50 semiconductor-lab:air_separation_unit
setblock ~29 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:nitrogen_gas",count:16},{Slot:1b,id:"semiconductor-lab:argon_gas",count:16},{Slot:2b,id:"semiconductor-lab:nitrogen_cylinder",count:16},{Slot:3b,id:"semiconductor-lab:argon_cylinder",count:16}]}
setblock ~32 ~0 ~49 minecraft:oak_sign[rotation=8]{front_text:{messages:["[6-06] 塩酸","（人間の確認項目なし）","",""]}}
setblock ~32 ~0 ~50 semiconductor-lab:hcl_burner
setblock ~33 ~0 ~50 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hydrochloric_acid",count:16},{Slot:1b,id:"semiconductor-lab:hydrogen_gas",count:16},{Slot:2b,id:"semiconductor-lab:chlorine_gas",count:16},{Slot:3b,id:"minecraft:water_bucket",count:1},{Slot:4b,id:"semiconductor-lab:glass_bottle",count:16}]}
setblock ~0 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹7 光学","↓ この列","",""]}}
setblock ~2 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-04] 屈折望遠鏡と色","屈折望遠鏡を右クリック","で押している間、バニラ","の望遠鏡と同じように視"]}}
setblock ~2 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:achromatic_lens",count:16},{Slot:1b,id:"semiconductor-lab:refracting_telescope",count:1}]}
setblock ~5 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-03] レンズ研磨","作業台で研削皿・研磨皿","が手元に残るか（シフト","クリックでまとめて作っ"]}}
setblock ~5 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rouge",count:16},{Slot:1b,id:"semiconductor-lab:grinding_tool",count:1},{Slot:2b,id:"semiconductor-lab:pitch_lap",count:1},{Slot:3b,id:"semiconductor-lab:crown_lens_blank",count:16},{Slot:4b,id:"semiconductor-lab:flint_lens_blank",count:16},{Slot:5b,id:"semiconductor-lab:crown_lens",count:16},{Slot:6b,id:"semiconductor-lab:flint_lens",count:16}]}
setblock ~8 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-06] プリズム分光","塩を左手に持って火のつ","いたたき火を右クリック"," → チャットに「ナト"]}}
setblock ~8 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:prism",count:16},{Slot:1b,id:"semiconductor-lab:spectroscope",count:1}]}
setblock ~11 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-05] 反射鏡","反射望遠鏡を右クリック","で押している間に拡大・","のぞき穴が出るか。合金"]}}
setblock ~11 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:speculum_charge",count:16},{Slot:1b,id:"semiconductor-lab:speculum_ingot",count:16},{Slot:2b,id:"semiconductor-lab:speculum_mirror",count:16},{Slot:3b,id:"semiconductor-lab:reflecting_telescope",count:1}]}
setblock ~14 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-01] 光学ガラス","レシピ本に3つのレシピ","が出るか（一酸化鉛・調","合原料・ソーダ石灰ガラ"]}}
setblock ~14 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:flint_glass_batch",count:16},{Slot:1b,id:"semiconductor-lab:flint_glass",count:16},{Slot:2b,id:"semiconductor-lab:crown_glass",count:16}]}
setblock ~17 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-07] 回折格子","格子分光器で塩を炎に ","→ D線2本の角度と波","長が出るか、昼の空 →"]}}
setblock ~17 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:wire_grating",count:16},{Slot:1b,id:"semiconductor-lab:grating_spectroscope",count:1}]}
setblock ~20 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-02] 溶融石英","（人間の確認項目なし）","",""]}}
setblock ~20 ~0 ~58 semiconductor-lab:oxyhydrogen_burner
setblock ~21 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:fused_quartz",count:16},{Slot:1b,id:"semiconductor-lab:quartz_tube",count:16},{Slot:2b,id:"semiconductor-lab:silicon_tetrachloride",count:16},{Slot:3b,id:"semiconductor-lab:hydrogen_cylinder",count:16},{Slot:4b,id:"semiconductor-lab:oxygen_cylinder",count:16},{Slot:5b,id:"semiconductor-lab:vein_quartz",count:16}]}
setblock ~24 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-09] 干渉計","点灯中の水銀灯を干渉計","で右クリック → 縞の","本数と長さの3行がチャ"]}}
setblock ~24 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:interferometer",count:1}]}
setblock ~27 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-10] 水銀ランプ","水銀灯をケーブルにつな","いで光るか（前面の絵・","明るさ）、右クリックの"]}}
setblock ~27 ~0 ~58 semiconductor-lab:mercury_lamp
setblock ~28 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:mercury_lamp_assembly",count:16},{Slot:1b,id:"semiconductor-lab:mercury_lamp_tube",count:16}]}
setblock ~31 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[5-14] 真空蒸着","真空容器｜拡散ポンプ｜","回転ポンプ の容器に蒸","着装置もとなりに置いて"]}}
setblock ~31 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:half_silvered_mirror",count:16}]}
setblock ~34 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-15] 紫外光学材料","ブリッジマン炉の画面（","左上に蛍石・左下にアル","ゴン、電力・状態の文字"]}}
setblock ~34 ~-1 ~58 semiconductor-lab:test_power_source
setblock ~34 ~0 ~58 semiconductor-lab:bridgman_furnace
setblock ~35 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:synthetic_quartz",count:16},{Slot:1b,id:"semiconductor-lab:caf2_crystal",count:16},{Slot:2b,id:"semiconductor-lab:caf2_lens",count:16},{Slot:3b,id:"semiconductor-lab:fluorite",count:16},{Slot:4b,id:"semiconductor-lab:argon_gas",count:16}]}
setblock ~38 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-22] 反射防止膜","蒸着装置の画面に蛍石と","レンズが入るか（シフト","クリックも）。膜付きレ"]}}
setblock ~38 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:coated_crown_lens",count:16},{Slot:1b,id:"semiconductor-lab:coated_flint_lens",count:16}]}
setblock ~41 ~0 ~57 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-11] 銀塩フォトマス","レシピ本に乾板までの流","れが出るか。アイテムの","見た目"]}}
setblock ~41 ~-1 ~58 semiconductor-lab:test_power_source
setblock ~41 ~0 ~58 semiconductor-lab:reduction_camera
setblock ~42 ~0 ~58 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:hide_broth",count:16},{Slot:1b,id:"semiconductor-lab:gelatin",count:16},{Slot:2b,id:"semiconductor-lab:photo_emulsion",count:16},{Slot:3b,id:"semiconductor-lab:photo_plate",count:16},{Slot:4b,id:"semiconductor-lab:ferrous_sulfate",count:16},{Slot:5b,id:"semiconductor-lab:hypo_liquor",count:16},{Slot:6b,id:"semiconductor-lab:sodium_thiosulfate",count:16},{Slot:7b,id:"semiconductor-lab:photomask",count:1},{Slot:8b,id:"semiconductor-lab:rubylith",count:1}]}
setblock ~0 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹8 精密","↓ この列","",""]}}
setblock ~2 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-04] ノギスとマイク","旋盤で作ったローター・","規格ねじを左手、ノギス","／マイクロメータを右手"]}}
setblock ~2 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:vernier_caliper",count:1},{Slot:1b,id:"semiconductor-lab:micrometer",count:1}]}
setblock ~5 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-10] 光学顕微鏡","フォトマスクを左手、顕","微鏡を右手で右クリック"," → チャットに NA"]}}
setblock ~5 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:simple_microscope",count:1},{Slot:1b,id:"semiconductor-lab:compound_microscope",count:1}]}
setblock ~8 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-02] ねじ切りと互換","旋盤の画面に「親ねじ」","の入口（左下）が増えた","。シフトクリックで送り"]}}
setblock ~8 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:lead_screw",count:16},{Slot:1b,id:"semiconductor-lab:standard_screw",count:16}]}
setblock ~11 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-01] 旋盤","旋盤の画面（左に入口・","右に出口・電力の文字）","。水銀灯の画面も同じ書"]}}
setblock ~11 ~-1 ~66 semiconductor-lab:test_power_source
setblock ~11 ~0 ~66 semiconductor-lab:lathe
setblock ~12 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pump_rotor",count:16},{Slot:1b,id:"semiconductor-lab:steel_ingot",count:16},{Slot:2b,id:"semiconductor-lab:lead_screw",count:16}]}
setblock ~15 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-03] 平面と定盤","鋳鉄の板を3枚作り、利","き手と左手に1枚ずつ持","って右クリック → チ"]}}
setblock ~15 ~0 ~66 semiconductor-lab:surface_plate
setblock ~16 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:cast_iron_plate",count:1}]}
setblock ~19 ~0 ~65 minecraft:oak_sign[rotation=8]{front_text:{messages:["[8-06] 光波干渉測長","（人間の確認項目なし）","",""]}}
setblock ~19 ~0 ~66 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:metre_bar",count:1}]}
setblock ~0 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹9 半導体（Ge・Si）","↓ この列","",""]}}
setblock ~2 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-03] 点接触トランジ","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:transistor",count:16}]}
setblock ~5 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-07] Ge 単結晶の","引き上げ機の GUI（","左に Ge・種・水素、","中央バー、出口、右に電"]}}
setblock ~5 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~5 ~0 ~74 semiconductor-lab:crystal_puller
setblock ~6 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:germanium_seed_crystal",count:16},{Slot:1b,id:"semiconductor-lab:germanium_single_crystal",count:16},{Slot:2b,id:"semiconductor-lab:tungsten_wire",count:16},{Slot:3b,id:"semiconductor-lab:refined_germanium",count:16},{Slot:4b,id:"semiconductor-lab:hydrogen_gas",count:16}]}
setblock ~9 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-08] CZ 法","CZ 引き上げ機の G","UI（左に多結晶Si・","種・ドーパント、2列目"]}}
setblock ~9 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~9 ~0 ~74 semiconductor-lab:cz_puller
setblock ~10 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:quartz_crucible",count:16},{Slot:1b,id:"semiconductor-lab:silicon_seed_crystal",count:16},{Slot:2b,id:"semiconductor-lab:silicon_single_crystal",count:16},{Slot:3b,id:"semiconductor-lab:polysilicon",count:16},{Slot:4b,id:"semiconductor-lab:argon_gas",count:16},{Slot:5b,id:"semiconductor-lab:boron_trioxide",count:16}]}
setblock ~13 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-01] Ge 帯域精製","（人間の確認項目なし）","",""]}}
setblock ~13 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~13 ~0 ~74 semiconductor-lab:zone_refining_furnace
setblock ~14 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:refined_germanium",count:16},{Slot:1b,id:"semiconductor-lab:graphite_boat_blank",count:16},{Slot:2b,id:"semiconductor-lab:graphite_boat",count:16},{Slot:3b,id:"semiconductor-lab:synthetic_graphite_boat",count:16},{Slot:4b,id:"semiconductor-lab:raw_germanium",count:16},{Slot:5b,id:"semiconductor-lab:phosphorus",count:16},{Slot:6b,id:"semiconductor-lab:indium",count:16}]}
setblock ~17 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-02] ゾーンレベリン","（人間の確認項目なし）","",""]}}
setblock ~17 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:n_type_germanium",count:16},{Slot:1b,id:"semiconductor-lab:p_type_germanium",count:16}]}
setblock ~20 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-04] 合金接合トラン","（人間の確認項目なし）","",""]}}
setblock ~20 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:junction_transistor_blank",count:16},{Slot:1b,id:"semiconductor-lab:junction_transistor",count:16}]}
setblock ~23 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-12] ウェハー切断","ウェハー切断機の GU","I（左に結晶・刃、銀色","のバー）。板のツールチ"]}}
setblock ~23 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~23 ~0 ~74 semiconductor-lab:wafer_saw
setblock ~24 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:diamond_saw_blade",count:16},{Slot:1b,id:"semiconductor-lab:silicon_wafer",count:16},{Slot:2b,id:"semiconductor-lab:silicon_single_crystal",count:16}]}
setblock ~27 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-16] 熱拡散","拡散炉の GUI（左に","板・ドーパント、オレン","ジのバー、反応式）。p"]}}
setblock ~27 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~27 ~0 ~74 semiconductor-lab:diffusion_furnace
setblock ~28 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:pn_junction_wafer",count:16},{Slot:1b,id:"semiconductor-lab:silicon_wafer",count:16},{Slot:2b,id:"semiconductor-lab:boron_trioxide",count:16}]}
setblock ~31 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-15] 熱酸化","酸化炉の GUI（左に","板・酸素か水、右に出口","2つ、オレンジのバー、"]}}
setblock ~31 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~31 ~0 ~74 semiconductor-lab:oxidation_furnace
setblock ~32 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:oxidized_wafer",count:16},{Slot:1b,id:"semiconductor-lab:silicon_wafer",count:16},{Slot:2b,id:"semiconductor-lab:oxygen_gas",count:16},{Slot:3b,id:"minecraft:water_bucket",count:1}]}
setblock ~35 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-06] 酸化膜エッチン","エッチング槽の GUI","（左に板・フッ酸、水色","のバー、右に状態、下に"]}}
setblock ~35 ~0 ~74 semiconductor-lab:etch_tank
setblock ~36 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:process_wafer",count:16},{Slot:1b,id:"semiconductor-lab:oxidized_wafer",count:16},{Slot:2b,id:"semiconductor-lab:hydrofluoric_acid",count:16}]}
setblock ~39 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[7-11] 銀塩フォトマス","レシピ本に乾板までの流","れが出るか。アイテムの","見た目"]}}
setblock ~39 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:rubylith",count:1}]}
setblock ~42 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[9-13] ラッピング・鏡","研磨機の GUI（左に","ウェハー・砥粒・シリカ","、銀色のバー、「やり直"]}}
setblock ~42 ~0 ~74 semiconductor-lab:wafer_polisher
setblock ~46 ~0 ~73 minecraft:oak_sign[rotation=8]{front_text:{messages:["[-] layout_she","（人間の確認項目なし）","",""]}}
setblock ~46 ~0 ~74 semiconductor-lab:drafting_table
setblock ~47 ~0 ~74 semiconductor-lab:probe_station
setblock ~48 ~-1 ~74 semiconductor-lab:test_power_source
setblock ~48 ~0 ~74 semiconductor-lab:cleaning_bench
setblock ~49 ~0 ~74 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:layout_sheet",count:1},{Slot:1b,id:"semiconductor-lab:process_wafer",count:16},{Slot:2b,id:"semiconductor-lab:nitric_acid",count:16},{Slot:3b,id:"semiconductor-lab:hydrofluoric_acid",count:16},{Slot:4b,id:"semiconductor-lab:distilled_water",count:16}]}
setblock ~0 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹12 建設・測量","↓ この列","",""]}}
setblock ~2 ~0 ~81 minecraft:oak_sign[rotation=8]{front_text:{messages:["[12-11] 測量","セオドライトで遠くの木","や塔をのぞいて離す →"," チャットに方位。横に"]}}
setblock ~2 ~0 ~82 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:theodolite",count:1}]}
setblock ~0 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["幹15 通信","↓ この列","",""]}}
setblock ~2 ~0 ~89 minecraft:oak_sign[rotation=8]{front_text:{messages:["[15-10] トランジスタ","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~90 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:transistor_radio",count:1}]}
setblock ~0 ~0 ~97 minecraft:oak_sign[rotation=8]{front_text:{messages:["枝 製品","↓ この列","",""]}}
setblock ~2 ~0 ~97 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-02] 銀塩写真 🌿✅","（人間の確認項目なし）","",""]}}
setblock ~2 ~0 ~98 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:silvered_plate",count:16},{Slot:1b,id:"semiconductor-lab:daguerreotype_plate",count:16},{Slot:2b,id:"semiconductor-lab:camera",count:1},{Slot:3b,id:"semiconductor-lab:exposed_plate",count:1},{Slot:4b,id:"semiconductor-lab:developed_plate",count:1},{Slot:5b,id:"semiconductor-lab:daguerreotype",count:1},{Slot:6b,id:"semiconductor-lab:exposed_dry_plate",count:1},{Slot:7b,id:"semiconductor-lab:developed_dry_plate",count:1},{Slot:8b,id:"semiconductor-lab:negative",count:1},{Slot:9b,id:"semiconductor-lab:printing_paper",count:16},{Slot:10b,id:"semiconductor-lab:print",count:16}]}
setblock ~5 ~0 ~97 minecraft:oak_sign[rotation=8]{front_text:{messages:["[B-12] 太陽電池","通しプレイ（多結晶Si"," → CZ（リンで n","型）→ 切断 → 拡散"]}}
setblock ~5 ~0 ~98 semiconductor-lab:solar_cell
setblock ~0 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["本物の発電","↓ この列","",""]}}
setblock ~2 ~0 ~105 minecraft:oak_sign[rotation=8]{front_text:{messages:["蒸気機関で発電","石炭と水バケツ","を入れると","電気炉が動く"]}}
setblock ~2 ~0 ~106 semiconductor-lab:steam_engine
setblock ~2 ~0 ~107 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"minecraft:coal",count:32},{Slot:1b,id:"minecraft:water_bucket",count:1},{Slot:2b,id:"minecraft:water_bucket",count:1},{Slot:3b,id:"minecraft:water_bucket",count:1},{Slot:4b,id:"minecraft:water_bucket",count:1}]}
setblock ~3 ~0 ~106 semiconductor-lab:copper_cable
setblock ~4 ~0 ~106 semiconductor-lab:copper_cable
setblock ~5 ~0 ~106 semiconductor-lab:copper_cable
setblock ~6 ~0 ~106 semiconductor-lab:electric_furnace
setblock ~7 ~0 ~106 minecraft:chest[facing=north]{Items:[{Slot:0b,id:"semiconductor-lab:refractory_brick_blank",count:16}]}
execute if entity @s run tellraw @s {"text":"試験場ができた：列ごとに幹、看板に「何を確かめるか」。全文は docs/TESTWORLD.md","color":"green"}
