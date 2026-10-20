# AUDIT_QUEUE.md — 監査の待ち行列（自動生成：tools/gen_tree_views.py。手で編集しない）

元データは docs/network_proposals.md の「⚠️ の裏取り」の表（V-番号）。技術ネットワーク担当（NET-03）が出典で確かめた結果を、あとで別の監査者にまとめて見てもらうための一覧。

## 監査する人への説明
- **このプロジェクト**：Minecraft の教育 MOD「Semiconductor Lab」。石器から EUV 露光装置・AI まで、本物の技術の歴史を遊びながら学ぶ。技術どうしのつながり（前提・別ルート・後で分かった科学）を「技術ネットワーク」として公開し、世界中の人に監査してもらう予定（docs/network.html・docs/node.html）
- **確かさの基準**：◎＝信頼できる出典2つ以上で一致／○＝1つで確認／△＝未確認・出典どうしが食い違う。**Wikipedia だけの場合は ○ 止まり**（この一覧では自動で ○ に下げ、「Wikipedia のみ」と書く）
- **見てほしいこと**：①年・場所・人物が正しいか ②因果関係（「A があったから B ができた」）が言いすぎていないか ③出典が主張を本当に支えているか（URL を開いて、その文が本当に書いてあるか）④「正しくは…」の訂正そのものが正しいか
- **結果の書き方**：項目ごとに「V-番号｜同意 / 修正 / 不明｜理由（1〜2文）｜出典 URL」。修正のときは正しい文も書く
- **優先順**：✖（訂正）→ △（未確認）→ ○（出典1つ・Wikipedia のみ）→ ◎（本線の年・人物、念のため）。どれも [10-16] AI までの道（本線）のノード

全 85 件（✖ 7・△ 27・○ 14・◎ 37）

## ✖ 訂正した項目（7）

| 番号 | ノード | 主張 | 今の判定 | NET-03 の結果 | 出典 | 特に確かめてほしい点 |
|---|---|---|---|---|---|---|
| V-07 | [0-10] 石灰焼成 | 石灰しっくいは紀元前9000〜7000年頃（ギョベクリ・テペ、チャタル・ヒュユク） | ✖（一部） | 正しくは「焼いた石灰は約1万2000年前のナトゥーフ文化（イスラエル、ナハル・エイン・ゲヴ II）が最古級、ギョベクリ・テペ（紀元前9600年頃〜）にも焼いた石灰のテラゾー床。レバントの先土器新石器B期（約1万年前）に広まる」。チャタル・ヒュユクの壁は主に泥灰土（マール）で例に向かない。年 -9000 はギョベクリ・テペを例にすれば合う | https://www.ncbi.nlm.nih.gov/pmc/articles/PMC10427327/ , https://en.wikipedia.org/wiki/G%C3%B6bekli_Tepe , https://discovery-pp.ucl.ac.uk/1521981/1/Camurcuoglu%26Siddall16.pdf | 訂正（「正しくは…」）が出典で本当に支えられているか |
| V-12 | [0-15] 窯 | 焼成レンガは紀元前3500年頃 | ✖ | 正しくは「最古の焼成レンガは中国・城頭山（大渓文化）の紀元前4400年頃」。紀元前3500年を残すなら「西アジアでは」と書く。出典は Wikipedia だけなので、直したあとも ⚠️ は残す | https://en.wikipedia.org/wiki/Brick , https://en.wikipedia.org/wiki/Chengtoushan | 訂正（「正しくは…」）が出典で本当に支えられているか |
| V-17 | [1-27] 鉛 | 年 -3500（古代の鉛製錬） | ✖ | 正しくは「方鉛鉱の処理は紀元前4400〜4300年（ルーマニア・ピエトレレ）、最古の鉛の像はアビドスの紀元前3800年頃」。年を -4000 前後（範囲つき）に | https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6457500/ , https://en.wikipedia.org/wiki/Metals_of_antiquity | 訂正（「正しくは…」）が出典で本当に支えられているか |
| V-23 | [2-09] 石けん | 紀元前2800年頃バビロニアの粘土板に処方 | ✖（一部） | 正しくは「紀元前2800年頃のバビロンの粘土の円筒から石けんのような物質（刻文は『灰を脂と煮た』）。処方の粘土板はもっと後（紀元前2500年頃とも2200年頃とも）」。年 -2800 はそのまま、粘土板の年代は ⚠️ を残す | https://en.wikipedia.org/wiki/Soap | 訂正（「正しくは…」）が出典で本当に支えられているか |
| V-28 | [6-04] 岩塩鉱 | 年 -1500 | ✖ | 正しくは「最古の岩塩採掘は紀元前4500年頃（アゼルバイジャン・ナヒチェヴァンのドゥズダグ、土器の型式による推定）。-1500 はハルシュタット（オーストリア）の青銅器時代の採掘（紀元前16〜15世紀）」。年を直すか「-1500＝ハルシュタット」と書き分ける | https://sciencedaily.com/releases/2010/11/101125201448.htm , https://www.nhm-wien.ac.at/hallstatt/en/salt_mine/bronze_age | 訂正（「正しくは…」）が出典で本当に支えられているか |
| V-44 | [1-21] 蛍石 | 年 1546（アグリコラ） | ✖ | 正しくは 1530年、アグリコラ『Bermannus』で融剤「fluores」として初めて活字に。1546年は『De Natura Fossilium』で、取り違えらしい | https://en.wikipedia.org/wiki/Fluorite , https://en.wikipedia.org/wiki/History_of_fluorine | 訂正（「正しくは…」）が出典で本当に支えられているか |
| V-53 | [7-05] 反射鏡 | 1932 ストロングのアルミ蒸着 | ✖ | 正しくは 1930年頃（カリフォルニア工科大）。1932〜37年はパロマー計画の期間、詳しい論文は1936年 | https://en.wikipedia.org/wiki/Silvering , https://ftp.astro.caltech.edu/palomar/audioguide/mirror_en.html | 訂正（「正しくは…」）が出典で本当に支えられているか |

## △ 未確認のまま残った項目（27）

| 番号 | ノード | 主張 | 今の判定 | NET-03 の結果 | 出典 | 特に確かめてほしい点 |
|---|---|---|---|---|---|---|
| V-04 | [0-12] 木炭 | 製錬用の大量生産は紀元前4000年頃 | △ | この年を示す出典なし。最古の銅製錬（セルビア・ベロヴォデ）が紀元前5000年頃なので、「銅の製錬（紀元前5000年頃〜）とともに燃料として重要に」と書き直す案 | https://discovery.ucl.ac.uk/id/eprint/1470180/ , https://www.penn.museum/sites/expedition/fuel-for-the-metal-worker/ | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-06 | [0-08] 木工 | 年 -9000（磨製石斧の始まり） | △ | 第1弾では未確認。第2弾で調べる | — | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-16 | [0-07] 羊毛と紡錘車 | 羊毛用の品種は紀元前4000年頃 | △ | 紀元前6000年頃説から紀元前4000〜3500年の定説まで幅がある。⚠️ を残す | https://en.wikipedia.org/wiki/Sheep , https://publications.dainst.org/journals/efb/article/view/1945/6085 | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-21 | [1-43] アンチモン | 1540 ビリングッチョ『ピロテクニア』 | ◎（年と書名）／△（鉄で還元） | 1540 ヴェネツィア刊、アンチモンの取り出し方を書いた欧州最古の本は確認。「鉄で硫黄を奪う」方法が書いてあるかは未確認なので、その部分だけ ⚠️ を残す | https://en.wikipedia.org/wiki/De_la_pirotechnia , https://aimehq.org/themes/custom/aime/doclibrary/library/pirotechnia-of-vannoccio-biringuccio-1943.html | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-22 | [2-02] カリ | 年 -3000 | △（年を直す案） | -3000 を直接支える出典なし。シュメール（ギルス）の粘土板に約4500年前、羊毛を洗った記録 → -2500 にして ⚠️ を残す案 | https://en.wikipedia.org/wiki/Potash , https://www.earthdate.org/files/000/002/734/EarthDate_274_C.pdf | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-24 | [1-46] 珪石 | 年 -2500 | △ | 何の出来事の年か本文から読めない。水晶の利用は中石器時代の刃物や青銅器時代のビーズ・印章までさかのぼる。年が何を指すかを決めて書き直す | https://www.iranicaonline.org/articles/crystal-rock-bolur-bolur-e-kuhi/ | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-26 | [2-14] ソーダ石灰ガラス | 紀元前2500年頃メソポタミア（最初のガラス） | △ | コーニングガラス美術館は約4000年前（紀元前2000年頃）、Wikipedia などは紀元前2500年頃のビーズ。「紀元前2500〜2000年頃、メソポタミアのビーズ」として ⚠️ を残す | https://whatson.cmog.org/exhibitions-galleries/origins-glassmaking , https://en.wikipedia.org/wiki/History_of_glass | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-27 | [0-08] 木工 | 年 -9000（磨製石斧の始まり） | △ | 「始まり＝-9000」とする出典なし。旧石器にも例外的な磨製の例があり、中欧には紀元前6千年紀半ば。新石器の始まりという慣例の年として ⚠️ を残す | https://www.gibmuseum.gi/news/polished-neolithic-axe-from-gorhams-cave-85 , https://exarc.net/ark:/88735/10196 | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-30 | [1-06] 塊鉄炉 | 年 -1200 | △ | 「紀元前1200年頃アナトリア」と「紀元前1400年頃ヒッタイト」の両説。⚠️ を残す | https://www.tf.uni-kiel.de/matwis/amat/iss/kap_a/backbone/ra_3_1.html , https://www.chemeurope.com/en/encyclopedia/History_of_ferrous_metallurgy.html | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-31 | [1-06] 塊鉄炉 | ヒッタイト周辺 | △ | インド・アナトリア・カフカスのいずれかとする出典もあり、ヒッタイトに限るのは不確か | https://www.tf.uni-kiel.de/matwis/amat/iss/kap_a/backbone/ra_3_1.html , https://www.chemeurope.com/en/encyclopedia/History_of_ferrous_metallurgy.html | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-32 | [1-08] 鍛鉄 | 年 -1200 | △ | 根拠は [1-06] と同じで幅がある（紀元前1400〜1200年頃） | https://www.tf.uni-kiel.de/matwis/amat/iss/kap_a/backbone/ra_3_1.html , https://www.chemeurope.com/en/encyclopedia/History_of_ferrous_metallurgy.html | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-33 | [1-45] 水銀 | 年 -500 | △ | 水銀そのものはエジプト・クルナの墓（紀元前15〜16世紀）。文字の最古の取り出し方はテオフラストス（紀元前4世紀、辰砂を酢とこする方法で、焼く方法ではない）。-500 を支える出典なし | https://www.chemistryexplained.com/elements/L-P/Mercury.html , https://chemistryworld.com/news/botanists-chemists-and-historians-come-together-to-recreate-ancient-alchemy-of-making-mercury/4015894.article | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-36 | [2-16] 蒸留水 | 8〜9世紀ジャービル・イブン・ハイヤーンのアランビック | △（言い換え） | ジャービル文書は 850〜950年頃に書かれたとされ、一人の実在の人物かも議論がある。アランビックとの結びつきの出典なし。「9〜10世紀のジャービル文書」と言い換える案 | https://en.wikipedia.org/wiki/Jabir_ibn_Hayyan , https://www.nlm.nih.gov/hmd/arabic/alchemy10.html | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-40 | [2-43] ばん油 | 9〜10世紀のイスラム世界の錬金術師 | △ | ラーズィーが緑礬を乾留したとする二次資料はあるが、確かなのは鉱物の分類に入れたことだけ。ジャービルの名も伝承 | https://en.wikipedia.org/wiki/Vitriol , https://old.vscht.cz/chem_listy/common/article-vol_96-issue_12-page_997.html | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-47 | [5-01] 手押しピストンポンプ | 1657 マクデブルクで馬16頭 | △ | 1656年と1657年が混在（1657年はショットの本の年の可能性）、馬も16頭と30頭で食い違う | https://catalogue.museogalileo.it/indepth/MagdeburgHemispheres.html , https://en.wikipedia.org/wiki/Magdeburg_hemispheres | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-48 | [5-01] 手押しピストンポンプ | 半球の直径 約40cm | △ | 30〜50cm でばらつく（Wikipedia・ガリレオ博物館は約50cm） | https://en.wikipedia.org/wiki/Magdeburg_hemispheres , https://omeka-s.grinnell.edu/s/physicsmuseum/item/5200 | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-49 | [5-01] 手押しピストンポンプ | 到達圧力 約数百Pa | △ | 測定値の出典なし | — | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-50 | [7-03] レンズ研磨 | 17世紀からベンガラで研磨 | △ | 酸化鉄の研磨剤がのちに CeO2 に置き換わったことは確認。17世紀からの出典なし | https://cameo.mfa.org/wiki/Optical_rouge | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-57 | [1-40] クロム | 1898年頃に炭素を含まない Cr | △ | 製造は確認、1898年の裏づけなし（1898年は論文の年）。「1890年代後半」とぼかす案 | https://www.spektrum.de/lexikon/chemie/aluminothermisches-verfahren/344 | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-61 | [2-19] 過酸化水素 | 1908 ヴァイセンシュタインで電解法を工業化 | △ | 場所は正しい。Evonik の資料どうしで年が食い違い、「1908 建設・1910 生産開始」と書けば外せる（独立した2つ目の出典なし） | https://www.evonik.com/en/company/worldwide-locations/europe/austria/weissenstein.html , https://history.evonik.com/en/inventions/hydrogen-peroxide | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-69 | [7-02] 溶融石英 | 1839 ゴーダンが酸水素炎で石英を溶かした | △ | ゴーダンが溶かして糸にしたことは19世紀の記述で確認。「1839年」の原典は見つからない | https://www.biblio.liuc.it/opere_cattaneo/politecnico/pdf/FusioneDellAcidoSilicico.pdf , https://www.nature.com/articles/143826a0.pdf | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-71 | [2-44] 赤リン | 真空管のゲッター（1894 マリニャーニ） | △ | 導入1893・伊特許1894・米特許1895年4月で食い違う。「1893〜1895（伊特許1894）」と幅で | https://lamptech.co.uk/Documents/IN%20Getters.htm , https://forumeditrice.it/percorsi/storia-e-societa/varia/arturo-malignani.-con-il-futuro-negli-occhi/luomo-che-con-i-suoi-bulbi-bianchi-incanto-edison/file | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-72 | [1-07] 黒鉛 | 年 1850（19世紀からるつぼ材） | △ | 1850年の出来事は見つからない。黒鉛るつぼはディクソン1827年（米）、モーガン兄弟1856年（英）。年を1827年か「1827〜1856」に | https://en.wikipedia.org/wiki/Joseph_Dixon_(inventor) , https://www.britannica.com/biography/Joseph-Dixon | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-73 | [2-42] 酢酸 | 年 1850（木酢液→酢酸カルシウム→硫酸） | △ | 流れは確認、1850年の根拠なし（代表の年） | https://www.encyclopedia.com/science-and-technology/chemistry/organic-chemistry/acetic-acid , https://www.chemeurope.com/en/encyclopedia/Acetic_acid.html | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-76 | [5-12] 真空シール | 1962頃 バリアンのコンフラット | △ | ウィーラーが1960年にバリアンに入って開発、特許は1960年代初め。「1960年代初め」なら ○ | https://avs.org/awards/awards/awardee-interviews/william-r-wheeler/bio/ , https://en.wikipedia.org/wiki/Vacuum_flange | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-78 | [5-12] 真空シール | 年 1850（1850〜1937） | △ | すり合わせガラス＋グリースの1850年の根拠なし（終わりの1937年は O リング出願と合う） | — | 食い違い・未確認の点を、より確かな出典で決められるか |
| V-83 | [3-19] 副産物回収式コークス炉 | 1881頃 オットーとホフマンが実用化 | △ | オットー・ホフマン炉は確認、1881年との結びつきなし。「1881 ドイツで実用化（ヒュッセナーら）、まもなくオットー・ホフマン炉」 | https://www.gracesguide.co.uk/Otto-Hoffmann , https://gracesguide.co.uk/Albert_Hussener | 食い違い・未確認の点を、より確かな出典で決められるか |

## ○ 出典1つ・Wikipedia のみの項目（14）

| 番号 | ノード | 主張 | 今の判定 | NET-03 の結果 | 出典 | 特に確かめてほしい点 |
|---|---|---|---|---|---|---|
| V-09 | [0-06] 織物 | 亜麻布は約9000年前（ナハル・ヘマル） | ○（年を直す） | 場所は正しい。年代は紀元前8210〜7300年で「約1万年前」が近い。年 -7000 → -8000 の候補 | https://pmc.ncbi.nlm.nih.gov/articles/PMC4977467/ | 2つ目の独立した出典があるか |
| V-19 | [1-22] 錫石 | 年 -3000（錫の利用の始まり） | ○ | 錫青銅の最初の証拠は紀元前3000年頃の近東・バルカン。どこで錫を掘ったかは未解明 | https://en.wikipedia.org/wiki/Tin_mine , https://research.brighton.ac.uk/en/publications/beaker-and-early-bronze-age-tin-exploitation-in-cornwall-cassiter/ | 2つ目の独立した出典があるか |
| V-25 | [2-03] 天然ソーダ | ナトロンでミイラ作り・年 -2500 | ○（年を直す） | 最初の証拠は古王国第4王朝（紀元前2600年頃、ヘテプヘレス1世の内臓箱）。「第4王朝（紀元前2600年頃）」と書けば ○、年 -2600 | https://www.spurlock.illinois.edu/exhibits/online/mummification/history2.html , https://giza.fas.harvard.edu/objects/54956/full/ | 2つ目の独立した出典があるか |
| V-35 | [2-16] 蒸留水 | 年 200（100〜300） | ○ | マリアの年代と合う（マリアの年代に頼った値） | https://en.wikipedia.org/wiki/Mary_the_Jewess | Wikipedia 以外の独立した出典で確かめられるか |
| V-38 | [6-16] 天然硝石の採取 | 中国の火薬は9世紀頃 | ○ | 808年の道教の文書が最古の処方、1044年『武経総要』がまとまった処方の最古。出典は Wikipedia 系のみ | https://en.wikipedia.org/wiki/History_of_gunpowder | Wikipedia 以外の独立した出典で確かめられるか |
| V-39 | [6-16] 天然硝石の採取 | 年 1250、ヨーロッパは13世紀から | ○ | 1267年ロジャー・ベーコンの著作に処方。欧州で硝石を採った記録そのものは未確認 | https://en.wikipedia.org/wiki/History_of_gunpowder , https://books.rsc.org/books/monograph/149/chapter/80602/Historical-Introduction | 2つ目の独立した出典があるか |
| V-42 | [6-11] 硝酸 | 年 1300（硝石＋ばん類） | ○ | 偽ゲーベル『Summa perfectionis』（1310年少し前）が最初期の記述 | https://en.wikipedia.org/wiki/Pseudo-Geber , https://old.vscht.cz/chem_listy/common/article-vol_96-issue_12-page_997.html | 2つ目の独立した出典があるか |
| V-43 | [6-11] 硝酸 | 1648 グラウバー法 | ○ | 硝石＋ばん油で大量に作った（最初の人ではない） | https://www.cavemanchemistry.com/cavebook/chsaltpeter.html | 2つ目の独立した出典があるか |
| V-52 | [2-23] 木材乾留 | 木酢液は酢酸数%・メタノール1〜3% | ○ | 45樹種の実測で酢酸1.2〜9.5%・メタノール0.2〜3.8%。「〜4%程度」とすると安全 | https://doaj.org/article/3084c45384e54c2c934c253895540324 , https://www.fao.org/4/X5328E/x5328e0d.htm | 2つ目の独立した出典があるか |
| V-66 | [I-01] 電磁誘導 | ジョセフ・ヘンリーもほぼ同じころ独立に発見 | ○（ideas.md を直した） | 独立の発見は確か。ただし発表は1832年で、主な発見は自己誘導。ideas.md の本物を「1832年、ジョセフ・ヘンリーが独立に（とくに自己誘導を）見つけた」に直した | https://siarchives.si.edu/collections/siris_sic_12470 , https://en.wikipedia.org/wiki/Joseph_Henry | 2つ目の独立した出典があるか |
| V-67 | [2-25] フェノール | クメン法は1944年ホック | ○ | 1944年ホック（ラングと共同）は正しい。1942年にソ連のウドリスとセルゲーエフが先に発明。「1942 ウドリスら、1944 ホックが独立に」 | https://en.wikipedia.org/wiki/Cumene_process , https://www.gophotonics.com/whitepapers/details/5319-ft-nir-analysis-of-the-hock-process-for-the-production-of-phenol-and-acetone | 2つ目の独立した出典があるか |
| V-75 | [5-12] 真空シール | O リングは1937 クリステンセンの特許 | ○ | 1937年は米国の出願年（特許は1938〜39年）。「1937年出願」と書く | https://en.wikipedia.org/wiki/Niels_Christensen , https://www.inventionandtech.com/node/86858 | 2つ目の独立した出典があるか |
| V-77 | [5-12] 真空シール | 1910年代のジュメット線 | ○ | GE のフィンクのジュメット線が1913年から白金線に代わった。年を示すのは同じ電球史サイトだけ | https://lamptech.co.uk/Documents/People%20-%20Fink%20CG.htm , https://wirenet.org/the-book-of-terms/dumet-wire | 2つ目の独立した出典があるか |
| V-82 | [3-19] 副産物回収式コークス炉 | 年 1881 | ○ | ドイツ最初の大きな副産物回収コークス工場が1881年5月（ヒュッセナー） | https://gracesguide.co.uk/Albert_Hussener , https://ausimm.com/publications/conference-proceedings/the-ausimm-proceedings-1928/modern-by-product-coke-oven-practice | 2つ目の独立した出典があるか |

## ◎ 本線の年・人物（念のため）（37）

| 番号 | ノード | 主張 | 今の判定 | NET-03 の結果 | 出典 | 特に確かめてほしい点 |
|---|---|---|---|---|---|---|
| V-01 | [0-02] 火起こし | 年 -1000000、火の利用は約100万年前（ワンダーワーク洞窟） | ◎ | ⚠️ を外してよい。ただし証拠は「火を使った」までで「火をおこした」ではないので、文は「火の利用」のままに | https://pmc.ncbi.nlm.nih.gov/articles/PMC3356665 , https://www.bu.edu/articles/2012/playing-with-fire-2/ , https://www.sciencenews.org/article/ashes-oldest-controlled-fire | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-02 | [3-01] たき火 | 年 -1000000、火の利用は約100万年前 | ◎ | 同上。「炉を使い続けた」とまでは言えない | https://pmc.ncbi.nlm.nih.gov/articles/PMC3356665 , https://www.sciencenews.org/article/ashes-oldest-controlled-fire | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-03 | [0-12] 木炭 | 洞窟壁画の顔料として約3万年前（年 -30000） | ◎ | ショーヴェ洞窟の木炭画が約3万〜3万2000年前。燃えさしの炭で、炭焼きで作った炭ではない | https://www.scientificamerican.com/article/scientists-confirm-great , https://whc.unesco.org/document/152648 | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-05 | [0-08] 木工 | ほぞ組みの木製井戸枠 約7000年前（ドイツ） | ◎ | ライプツィヒ近郊の線帯文土器文化の井戸、オーク材は年輪で紀元前5206〜5098年 | https://www.sciencedaily.com/releases/2012/12/121220080130.htm , https://kommunikation.uni-freiburg.de/pm-en/press-releases-2012/pm.2012-12-19.366-en | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-08 | [0-06] 織物 | 亜麻繊維の痕跡は約3万年前（ズズアナ洞窟） | ◎ | Science 2009（Kvavadze ら）、約3.6万〜3.1万年前 | https://dash.harvard.edu/handle/1/4270521 , https://www.npr.org/2009/09/10/112726804/these-vintage-threads-are-30-000-years-old | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-10 | [0-11] 消石灰・漆喰 | 年 -7000 | ◎ | アイン・ガザルの石灰しっくい像（紀元前7200〜6250年、スミソニアン：紀元前6500年頃） | https://en.wikipedia.org/wiki/Ayn_Ghazal_statues , https://mci.si.edu/ain-ghazal-statues | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-11 | [0-15] 窯 | 年 -6000（最古の窯） | ◎ | イラク・ヤリム・テペ（ハッスーナ文化）、紀元前6000年頃 | https://en.wikipedia.org/wiki/Pit_fired_pottery , https://www.guinnessworldrecords.com/world-records/first-pottery-kilns | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-13 | [0-16] 塩田 | 年 -6000（製塩の始まり） | ◎ | ルーマニア・ポイアナ・スラティネイで紀元前6050〜5500年に塩泉の水を土器で煮詰めた（Antiquity 2005） | https://www.antiquity.ac.uk/projgall/weller306/ , https://en.wikipedia.org/wiki/Briquetage | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-14 | [11-10] 酒と酢 | 年 -6000（酒の始まり） | ◎（範囲を広げる案） | ジョージアのワイン 紀元前6000〜5800年（PNAS 2017）。中国・賈湖の発酵酒は紀元前7000年頃なので、範囲の始まりを -7000 にする候補 | https://www.ift.org/news-and-publications/scientific-journals/news/2017/november/21/earliest-evidence-of-winemaking-dates-to-about-6000-bc , https://nationalgeographic.com/history/article/oldest-winemaking-grapes-georgia-archaeology | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-15 | [11-10] 酒と酢 | 1864 パスツールが酢は酢酸菌が作ると示した | ◎ | 1864年の論文で Mycoderma aceti。1861〜62 にも発表、本『酢の研究』は1868 | https://collections.nlm.nih.gov/ocr/nlm:nlmuid-101237604-bk , https://lpsn.dsmz.de/species/mycoderma-aceti | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-18 | [1-05] 青銅 | 青銅より前はヒ素銅 | ◎ | 近東・イラン高原では銅器時代（紀元前4500〜3000年頃）の主な金属がヒ素銅。セルビア・プロチュニクに紀元前4650年頃の錫青銅の箔があるので「近東では」と付けると正確 | https://journals.ut.ac.ir/article_82711.html , https://discovery-pp.ucl.ac.uk/id/eprint/1529309 | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-20 | [1-43] アンチモン | 年 -3000（輝安鉱のアイシャドウ） | ◎ | 輝安鉱は少なくとも紀元前3000年からコールに使われた | https://en.wikipedia.org/wiki/Stibnite , https://www.chemeurope.com/en/encyclopedia/Antimony.html | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-29 | [3-04] ふいご | 年 -1450、レクミラの墓の壁画（紀元前15世紀頃） | ◎ | メトロポリタン美術館：TT100 は紀元前1479〜1425年頃、足踏みふいごで銅を溶かす場面 | https://www.metmuseum.org/art/collection/search/544617 , https://library.biblicalarchaeology.org/images/bsba100404510ljpg/ | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-34 | [2-16] 蒸留水 | アレクサンドリアのマリア（1〜3世紀）の装置 | ◎ | 三本腕の蒸留器（トリビコス）。伝えたのはゾシモス（300年頃）で、発明者かは不明と書くとよい | https://en.wikipedia.org/wiki/Mary_the_Jewess , https://4kyws.ua.edu/HEBRAEA.html | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-37 | [1-13] 亜鉛 | 年 1150、インド・ザワールで12世紀頃 | ◎ | 12世紀〜19世紀初めに操業（坑道の木材の年代はさらに古い） | https://mci.si.edu/node/1187917 , https://esamskriti.com/e/Culture/Indian-Culture/Zinc-Mining-In-Zawar-Region-Of-Rajasthan~Interview-With-Dr.-Paul-T.-Craddock-1.aspx | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-41 | [2-43] ばん油 | 13世紀頃ヨーロッパ・年 1300 | ◎ | ヴァンサン・ド・ボーヴェ、偽ゲーベル『完全大全』などに言及 | https://en.wikipedia.org/wiki/Vitriol , https://old.vscht.cz/chem_listy/common/article-vol_96-issue_12-page_997.html | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-45 | [5-01] 手押しピストンポンプ | 1650頃 ゲーリケがポンプ製作 | ◎ | 1650年（1655年頃とする資料もある） | https://en.wikipedia.org/wiki/Otto_von_Guericke , https://pages.mtu.edu/~pcharles/SCIHISTORY/Guericke.html | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-46 | [5-01] 手押しピストンポンプ | 1654 レーゲンスブルクの公開実験 | ◎ | 帝国議会、皇帝フェルディナント3世の前 | https://en.wikipedia.org/wiki/Magdeburg_hemispheres , https://physicstoday.aip.org/backscatter/magdeburg-hemispheres | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-51 | [2-23] 木材乾留 | 年 1661 ボイルが木精を記述 | ◎ | ツゲ材の乾留で「spirit of box」（原典は未確認） | https://graphsearch.epfl.ch/en/concept/19712 , https://kids.britannica.com/students/article/methanol/275810 | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-54 | [1-20] 白金 | 年 1748（ウジョアの記述） | ◎ | 1748年『Relación Histórica』。ウッドが1741年に試料を英国へ持ち込んでいるので「最初の記述」と書く | https://wpd.ugr.es/~gervilla/wp-content/uploads/2022/01/Edad-del-platino-en-Espana.pdf , https://technology.matthey.com/content/journals/10.1595/003214092X3614047 | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-55 | [1-23] 輝水鉛鉱 | 年 1778（シェーレ） | ◎ | 黒鉛でも鉛の鉱石でもなくモリブデンの硫化物と示した | https://www.chemicool.com/elements/molybdenum.html , https://winter.group.shef.ac.uk/webelements/molybdenum/history.html | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-56 | [1-40] クロム | 1895 ゴルトシュミットのテルミット法の特許 | ◎ | DRP 96317（1895年3月13日） | https://en.wikipedia.org/wiki/Hans_Goldschmidt , https://www.goldschmidt.com/en/goldschmidt-group/history/ | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-58 | [2-07] 重クロム酸カリウム | 6価クロムは発がん性 | ◎ | IARC グループ1（第49巻 1990、第100C巻 2012） | https://www.carexcanada.ca/?p=9477 , https://insst.es/documents/d/portal-insst/sheet-n-7-chromium-vi-ano-2023 | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-59 | [8-02] ねじ切りと互換性部品 | 互換性部品はブラン（1780年代）と米国の兵器工場（1820年代〜） | ◎ | ブランは1785年ヴァンセンヌで実演。ハーパーズ・フェリーのホール M1819 ライフル最初の1000丁が1824年、1826年の検査で部品を混ぜても組めた。「1824 ホール」を補うとよい | https://en.wikipedia.org/wiki/Honor%C3%A9_Blanc , https://www.nramuseum.org/the-museum/the-galleries/the-prospering-new-republic/case-34-the-mexican-war/us-hall-model-1819-breechloading-percussion-rifle.aspx | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-60 | [6-01] 電解槽 | 年 1800（ボルタ電堆で水を電気分解） | ◎ | 1800年5月、ニコルソンとカーライル | https://en.wikipedia.org/wiki/William_Nicholson_(chemist) , https://beeld.teylersmuseum.nl/digital_library/Emags/Van_Marum%27s_Scientific_Instruments/page_112.html | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-62 | [1-24] ボーキサイト | 年 1821（ベルティエがレ・ボー近くで発見） | ◎ | 名前は村から。1847 beauxite、1861 bauxite | https://en.wikisource.org/wiki/1911_Encyclop%C3%A6dia_Britannica/Bauxite , https://www.earthmagazine.org/article/march-23-1821-bauxite-discovered/ | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-63 | [4-08] 電動モーター | 年 1821（1821〜1873） | ◎ | 1821年9月 ファラデーの電磁回転、1873年ウィーン万博でグラムの発電機がモーターとして回った。「最初のモーター」という呼び方は誇張という指摘あり | https://blog.sciencemuseum.org.uk/a-small-revolution-michael-faraday-and-the-origins-of-the-electric-motor/ , https://britannica.com/technology/Gramme-dynamo | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-64 | [3-12] 耐火れんが | 年 1822、珪石れんが（ディナス）は W・W・ヤング | ◎ | 1822年に特許（No.5047）、ディナスの珪石＋石灰約1%（Scottish Brick History はヤング説に「?」） | https://croydoncavingclub.org.uk/node/574 , https://www.scottishbrickhistory.co.uk/early-references-to-fire-brick-manufacture-in-the-uk/ | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-65 | [1-25] モナザイト | 年 1829 | ◎ | 1829年ブライトハウプトが命名、模式産地はウラルのイリメニ山地 | https://www.typmineral.uni-hamburg.de/tables/en/monazite-ce.html , https://en.wikipedia.org/wiki/Monazite | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-68 | [2-26] メタノール | 1923 BASF のピア・ミッタシュらが合成を工業化 | ◎ | 1923年7月ロイナで生産開始。触媒・圧力の数字は出典で食い違うので書かない | https://en.wikipedia.org/wiki/Alwin_Mittasch , https://www.deutsche-biographie.de/sfz95868.html | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-70 | [7-02] 溶融石英 | 1899 ヘレウス社が工業化 | ◎ | 1899年 Küch／ヘレウスが泡のない石英ガラス（部門の設立は1901年） | https://www.heraeus-group.com/en/about-heraeus/history , https://en.wikipedia.org/wiki/Heraeus | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-74 | [2-42] 酢酸 | 酢酸セルロースは無水酢酸＋硫酸触媒 | ◎ | 1865年シュッツェンベルガーが合成、今の工業法は硫酸などの触媒 | https://en.wikipedia.org/wiki/Cellulose_acetate , https://www.britannica.com/print/article/101663 | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-79 | [4-05] 自励式発電機 | 年 1866（1866〜67） | ◎ | ヴァーリーの出願1866年12月、ジーメンスとホイートストンの発表1867年 | https://en.wikipedia.org/wiki/Dynamo , https://www.gracesguide.co.uk/S._A._Varley | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-80 | [4-05] 自励式発電機 | ジーメンス・ホイートストンら、ほぼ同時 | ◎ | 「ら」にヴァーリーを名前で入れるとよい | https://www.nature.com/articles/145772c0.pdf , https://www.vde.com/de/geschichte/chronik/generatoren | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-81 | [4-01] 水車発電機 | 年 1878（初期の発電所は水力） | ◎ | 1878年 英国クラッグサイドでアームストロングが水力の電気でアーク灯。「世界初の水力発電所」とは言わない | https://www.nationaltrust.org.uk/visit/north-east/cragside/history-of-cragside , https://collection.sciencemuseumgroup.org.uk/people/ap29986 | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-84 | [4-03] 銅ケーブル | 年 1882 | ◎ | 1882年9月4日パール街発電所、銅の導体を地下の管で配線 | https://ethw.org/Milestones:Pearl_Street_Station , https://ethw.org/w/images/a/ae/Edison_and_Pearl_Street | 出典が主張（年・人物・場所）をそのまま支えているか |
| V-85 | [4-10] 変圧器 | 年 1885 | ◎ | 1885年ガンツ社のジペルノフスキー・ブラーティ・デーリが閉じた鉄心の変圧器（ZBD）を出願 | https://en.wikipedia.org/wiki/Ott%C3%B3_Bl%C3%A1thy , https://pp.bme.hu/ee/article/view/4637 | 出典が主張（年・人物・場所）をそのまま支えているか |
