# network_proposals.md — 技術ネットワーク担当から実装担当への提案

技術ネットワーク担当（NET-番号）は TECH_TREE.md・src・lang を触らない。直したいこと・足りない技術ノードはここに書く。
判断が出たら「状態」に書く（✅ 採用 / ✖ 不採用 / 提案）。TECH_TREE への反映は実装担当（IMP-番号）が行う。

## 足すべき技術ノード（products.md・ideas.md の前提に要るのに無いもの）
| # | 足すべきノード | 使うところ | 本物（出典のある範囲） | 状態 |
|---|---|---|---|---|
| N-1 | 滑車（複滑車） | [P-01] クレーン | 複滑車のクレーンはウィトルウィウス・ヘロンが記述（https://en.wikipedia.org/wiki/Crane_(machine)） | ✅ 追加（2026-10-20 決定、反映は IMP-24）。**反映済み（IMP-24、TECH_TREE [8-19] ❓）** |
| N-2 | ロープ（綱） | [P-01] クレーン | 年・出典は未調査 ⚠️。[0-06] 織物 の近くに置く案 | ✅ 追加（IMP-24）。**反映済み（IMP-24、TECH_TREE [0-18] ❓）** |
| N-3 | リチウムイオン電池 | [P-03] EV、[B-10] スマホ（TECH_TREE の本文に「1985 吉野彰の原型、1991 …」とある） | 年は TECH_TREE [1-42] の用途欄を出典で確かめてから | ✅ 追加（IMP-24）。**反映済み（IMP-24、TECH_TREE [4-15] ❓）** |
| N-4 | パワー半導体（IGBT・パワー MOSFET） | [P-03] EV | 年・出典は未調査 ⚠️。前提は [9-23] MOSFET の先 | ✅ N-5 と1つにまとめて追加（IMP-24）。**反映済み（IMP-24、TECH_TREE [9-41] ❓）** |
| N-5 | インバータ（直流→交流、回転の速さを変える） | [P-03] EV | 年・出典は未調査 ⚠️。前提は N-4 と [4-08] | ✅ N-4 と1つにまとめて追加（IMP-24）。**反映済み（IMP-24、TECH_TREE [9-41] ❓）** |
| N-6 | 光で固まる樹脂（光重合・光硬化樹脂） | [P-04] 3Dプリンター | 1981 小玉秀男・1984 ハルの光造形で使用（ASME の資料）。[2-30] ネガ型レジストの近く | ✅ 追加（IMP-24）。**反映済み（IMP-24、TECH_TREE [2-45] ❓）** |
| N-7 | CAD（コンピュータで形を描く） | [P-04] 3Dプリンター | 年・出典は未調査 ⚠️。前提は [10-14] | ✅ 追加（IMP-24）。**反映済み（IMP-24、TECH_TREE [10-17] ❓）** |
| N-8 | 解析機関（バベッジ、設計だけで作られなかった計算機） | [I-08] プログラム（エイダ・ラブレス） | 1843 ラブレスが論文を英訳して注を付けた（https://en.wikipedia.org/wiki/Note_G）。設計の年は出典で確かめてから | 提案（NET-02）。**反映済み（IMP-24、TECH_TREE [10-20] ❓）** |
| N-9 | アーク灯（炭素のアークの明かり） | [I-07] 電気アークのしくみ（ハータ・エアトン） | エアトンが研究したのは炭素のアーク灯のシュー音（https://en.wikipedia.org/wiki/Hertha_Ayrton）。アーク灯そのものの年は出典で確かめてから | 提案（NET-02）。**反映済み（IMP-24、TECH_TREE [4-16] ❓）** |

## 女性の人物（NET-02、出典で確かめた結果）
本物の技術・発見に関わり、ネットワークにつながる場所がある人だけを書く。数を合わせるための追加はしない。存命の人は載せない（6人とも亡くなっている）。
| # | 人 | 何をしたか（出典） | ネットワークでの扱い | 状態 |
|---|---|---|---|---|
| W-1 | ハータ・エアトン（1854〜1923） | 炭素アークのシュー音は炭素と酸素の反応が原因と示した。1899 IEE で発表・初の女性会員、1902『The Electric Arc』、1906 ヒューズ・メダル（https://en.wikipedia.org/wiki/Hertha_Ayrton） | ideas.md に [I-07] として追加済み（[3-15] アーク炉を説明する）。人物カードあり | ✅ 追加（NET-02） |
| W-2 | エイダ・ラブレス（1815〜1852） | 1843 解析機関の論文の英訳と注。注G のベルヌーイ数の手順表（どこまで本人が書いたかは諸説 ⚠️）（https://en.wikipedia.org/wiki/Note_G） | ideas.md に [I-08] として追加済み（[10-06] プログラム式の計算機の考え方）。人物カードあり | ✅ 追加（NET-02） |
| W-3 | キャサリン・ブロジェット（1898〜1979） | 1938 ステアリン酸バリウムの単分子膜を44層重ね、ガラスの反射を打ち消す「見えないガラス」（特許 2,220,660、ラングミュア＝ブロジェット膜）（https://en.wikipedia.org/wiki/Katharine_Burr_Blodgett） | 技術ノード「反射防止膜」が無い。足す案：幹7（光学）、レンズ・投影レンズ [7-19] の前提か理解に。人物カードはノードができてから | 提案。**反映済み（IMP-24、TECH_TREE [7-22] ❓）** |
| W-4 | グレース・ホッパー（1906〜1992） | 1952 A-0 システム（初期のコンパイラ。「世界初」とする言い方には諸説 ⚠️）、のち COBOL（https://en.wikipedia.org/wiki/A-0_System 、https://en.wikipedia.org/wiki/Grace_Hopper） | 技術ノード「コンパイラ（プログラミング言語）」が無い。足す案：幹10、[10-07] 計算機の後・[10-16] ニューラルネットの前（AI を動かすソフトの土台）。年と出典はノードを作るときに確かめる | 提案。**反映済み（IMP-24、TECH_TREE [10-18] ❓）** |
| W-5 | リン・コンウェイ（1938〜2024） | 1979 カーヴァー・ミードと『Introduction to VLSI Systems』、拡大縮小できる設計ルールで IC 設計の教え方と進め方を変えた（https://en.wikipedia.org/wiki/Lynn_Conway 、https://computerhistory.org/blog/in-memoriam-lynn-conway-1938-2024/） | 技術ノード「VLSI 設計法（設計ルール）」が無い。足す案：幹10 か 9、[10-13] マイクロプロセッサの後・[B-11] AIチップの前 | 提案。**反映済み（IMP-24、TECH_TREE [10-19] ❓）** |
| W-6 | ヘディ・ラマー（1914〜2000） | 1942 ジョージ・アンタイルと周波数ホッピングの特許（2,292,387、https://en.wikipedia.org/wiki/Hedy_Lamarr）。Wi-Fi・Bluetooth の元とする話は出典によって食い違い、より古い特許もある ⚠️ | 見送り：いまのネットワークに直接つながるノードが無い（[B-17] GPS は周波数ホッピングではない）。通信の幹に「スペクトラム拡散」ノードを作るときに改めて検討 | ✖ 見送り（NET-02） |

## TECH_TREE の直し（提案）
| # | 内容 | 理由 | 状態 |
|---|---|---|---|
| T-1 | [10-05] ブール代数とスイッチ回路 に「種類: 発見」を付ける（または ideas.md の I- ノードに移す） | 考え方のノード。図でひし形にしたい。生成器は TECH_TREE の「種類: 発見」もひし形にする | ✅ 採用（IMP-24）。**反映済み（IMP-24、TECH_TREE [10-05] ❓）** |
| T-2 | [15-14] 情報理論 に「種類: 発見」を付ける（同上） | 「解説のみ」のノードで、作るモノではない | ✅ 採用（IMP-24）。**反映済み（IMP-24、TECH_TREE [15-14] ❓）** |
| T-3 | [14-15] ロケット（年 1903＝ツィオルコフスキーのロケット方程式）を「ロケットの原理（発見）」と分けるか検討 | 年が方程式（考え方）の年で、モノの液体燃料ロケット 1926 は [P-02] にした | ✅ 採用（IMP-24）。**反映済み（IMP-24、TECH_TREE [14-15] ❓）** |
| T-4 | [9-01] Ge 帯域精製 の「理解:」に偏析の考え方を、[9-39]・[9-40] にアッベの限界を書くかどうか | 今は ideas.md 側の「前提になる／説明する」だけでつないでいる | ✖ 不採用：理解のつながりは ideas.md だけに置く |

## ⚠️ の裏取り（NET-03：公開前の自己監査）
TECH_TREE の ⚠️ が付いた年・人物を、出典で1つずつ確かめた結果。古い順・本線（[10-16] AI までの道）を先にする。TECH_TREE の ⚠️ を外す・年を直すのは実装担当。
判定：◎ ⚠️ を外してよい（信頼できる出典2つ以上）／○ ○ にしてよい（出典1つ）／△ ⚠️ を残す（理由つき）／✖ 間違い（「正しくは…」）
対象：⚠️ のあるノード 145（うち本線 102）。第1弾は本線の古い順に 20 ノード・26 件。

### 第1弾（2026-10-20）
| # | ノード | 主張 | 判定 | 結果 | 出典 |
|---|---|---|---|---|---|
| V-01 | [0-02] 火起こし | 年 -1000000、火の利用は約100万年前（ワンダーワーク洞窟） | ◎ | ⚠️ を外してよい。ただし証拠は「火を使った」までで「火をおこした」ではないので、文は「火の利用」のままに | https://pmc.ncbi.nlm.nih.gov/articles/PMC3356665 , https://www.bu.edu/articles/2012/playing-with-fire-2/ , https://www.sciencenews.org/article/ashes-oldest-controlled-fire |
| V-02 | [3-01] たき火 | 年 -1000000、火の利用は約100万年前 | ◎ | 同上。「炉を使い続けた」とまでは言えない | https://pmc.ncbi.nlm.nih.gov/articles/PMC3356665 , https://www.sciencenews.org/article/ashes-oldest-controlled-fire |
| V-03 | [0-12] 木炭 | 洞窟壁画の顔料として約3万年前（年 -30000） | ◎ | ショーヴェ洞窟の木炭画が約3万〜3万2000年前。燃えさしの炭で、炭焼きで作った炭ではない | https://www.scientificamerican.com/article/scientists-confirm-great , https://whc.unesco.org/document/152648 |
| V-04 | [0-12] 木炭 | 製錬用の大量生産は紀元前4000年頃 | △ | この年を示す出典なし。最古の銅製錬（セルビア・ベロヴォデ）が紀元前5000年頃なので、「銅の製錬（紀元前5000年頃〜）とともに燃料として重要に」と書き直す案 | https://discovery.ucl.ac.uk/id/eprint/1470180/ , https://www.penn.museum/sites/expedition/fuel-for-the-metal-worker/ |
| V-05 | [0-08] 木工 | ほぞ組みの木製井戸枠 約7000年前（ドイツ） | ◎ | ライプツィヒ近郊の線帯文土器文化の井戸、オーク材は年輪で紀元前5206〜5098年 | https://www.sciencedaily.com/releases/2012/12/121220080130.htm , https://kommunikation.uni-freiburg.de/pm-en/press-releases-2012/pm.2012-12-19.366-en |
| V-06 | [0-08] 木工 | 年 -9000（磨製石斧の始まり） | △ | 第1弾では未確認。第2弾で調べる | — |
| V-07 | [0-10] 石灰焼成 | 石灰しっくいは紀元前9000〜7000年頃（ギョベクリ・テペ、チャタル・ヒュユク） | ✖（一部） | 正しくは「焼いた石灰は約1万2000年前のナトゥーフ文化（イスラエル、ナハル・エイン・ゲヴ II）が最古級、ギョベクリ・テペ（紀元前9600年頃〜）にも焼いた石灰のテラゾー床。レバントの先土器新石器B期（約1万年前）に広まる」。チャタル・ヒュユクの壁は主に泥灰土（マール）で例に向かない。年 -9000 はギョベクリ・テペを例にすれば合う | https://www.ncbi.nlm.nih.gov/pmc/articles/PMC10427327/ , https://en.wikipedia.org/wiki/G%C3%B6bekli_Tepe , https://discovery-pp.ucl.ac.uk/1521981/1/Camurcuoglu%26Siddall16.pdf |
| V-08 | [0-06] 織物 | 亜麻繊維の痕跡は約3万年前（ズズアナ洞窟） | ◎ | Science 2009（Kvavadze ら）、約3.6万〜3.1万年前 | https://dash.harvard.edu/handle/1/4270521 , https://www.npr.org/2009/09/10/112726804/these-vintage-threads-are-30-000-years-old |
| V-09 | [0-06] 織物 | 亜麻布は約9000年前（ナハル・ヘマル） | ○（年を直す） | 場所は正しい。年代は紀元前8210〜7300年で「約1万年前」が近い。年 -7000 → -8000 の候補 | https://pmc.ncbi.nlm.nih.gov/articles/PMC4977467/ |
| V-10 | [0-11] 消石灰・漆喰 | 年 -7000 | ◎ | アイン・ガザルの石灰しっくい像（紀元前7200〜6250年、スミソニアン：紀元前6500年頃） | https://en.wikipedia.org/wiki/Ayn_Ghazal_statues , https://mci.si.edu/ain-ghazal-statues |
| V-11 | [0-15] 窯 | 年 -6000（最古の窯） | ◎ | イラク・ヤリム・テペ（ハッスーナ文化）、紀元前6000年頃 | https://en.wikipedia.org/wiki/Pit_fired_pottery , https://www.guinnessworldrecords.com/world-records/first-pottery-kilns |
| V-12 | [0-15] 窯 | 焼成レンガは紀元前3500年頃 | ✖ | 正しくは「最古の焼成レンガは中国・城頭山（大渓文化）の紀元前4400年頃」。紀元前3500年を残すなら「西アジアでは」と書く。出典は Wikipedia だけなので、直したあとも ⚠️ は残す | https://en.wikipedia.org/wiki/Brick , https://en.wikipedia.org/wiki/Chengtoushan |
| V-13 | [0-16] 塩田 | 年 -6000（製塩の始まり） | ◎ | ルーマニア・ポイアナ・スラティネイで紀元前6050〜5500年に塩泉の水を土器で煮詰めた（Antiquity 2005） | https://www.antiquity.ac.uk/projgall/weller306/ , https://en.wikipedia.org/wiki/Briquetage |
| V-14 | [11-10] 酒と酢 | 年 -6000（酒の始まり） | ◎（範囲を広げる案） | ジョージアのワイン 紀元前6000〜5800年（PNAS 2017）。中国・賈湖の発酵酒は紀元前7000年頃なので、範囲の始まりを -7000 にする候補 | https://www.ift.org/news-and-publications/scientific-journals/news/2017/november/21/earliest-evidence-of-winemaking-dates-to-about-6000-bc , https://nationalgeographic.com/history/article/oldest-winemaking-grapes-georgia-archaeology |
| V-15 | [11-10] 酒と酢 | 1864 パスツールが酢は酢酸菌が作ると示した | ◎ | 1864年の論文で Mycoderma aceti。1861〜62 にも発表、本『酢の研究』は1868 | https://collections.nlm.nih.gov/ocr/nlm:nlmuid-101237604-bk , https://lpsn.dsmz.de/species/mycoderma-aceti |
| V-16 | [0-07] 羊毛と紡錘車 | 羊毛用の品種は紀元前4000年頃 | △ | 紀元前6000年頃説から紀元前4000〜3500年の定説まで幅がある。⚠️ を残す | https://en.wikipedia.org/wiki/Sheep , https://publications.dainst.org/journals/efb/article/view/1945/6085 |
| V-17 | [1-27] 鉛 | 年 -3500（古代の鉛製錬） | ✖ | 正しくは「方鉛鉱の処理は紀元前4400〜4300年（ルーマニア・ピエトレレ）、最古の鉛の像はアビドスの紀元前3800年頃」。年を -4000 前後（範囲つき）に | https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6457500/ , https://en.wikipedia.org/wiki/Metals_of_antiquity |
| V-18 | [1-05] 青銅 | 青銅より前はヒ素銅 | ◎ | 近東・イラン高原では銅器時代（紀元前4500〜3000年頃）の主な金属がヒ素銅。セルビア・プロチュニクに紀元前4650年頃の錫青銅の箔があるので「近東では」と付けると正確 | https://journals.ut.ac.ir/article_82711.html , https://discovery-pp.ucl.ac.uk/id/eprint/1529309 |
| V-19 | [1-22] 錫石 | 年 -3000（錫の利用の始まり） | ○ | 錫青銅の最初の証拠は紀元前3000年頃の近東・バルカン。どこで錫を掘ったかは未解明 | https://en.wikipedia.org/wiki/Tin_mine , https://research.brighton.ac.uk/en/publications/beaker-and-early-bronze-age-tin-exploitation-in-cornwall-cassiter/ |
| V-20 | [1-43] アンチモン | 年 -3000（輝安鉱のアイシャドウ） | ◎ | 輝安鉱は少なくとも紀元前3000年からコールに使われた | https://en.wikipedia.org/wiki/Stibnite , https://www.chemeurope.com/en/encyclopedia/Antimony.html |
| V-21 | [1-43] アンチモン | 1540 ビリングッチョ『ピロテクニア』 | ◎（年と書名）／△（鉄で還元） | 1540 ヴェネツィア刊、アンチモンの取り出し方を書いた欧州最古の本は確認。「鉄で硫黄を奪う」方法が書いてあるかは未確認なので、その部分だけ ⚠️ を残す | https://en.wikipedia.org/wiki/De_la_pirotechnia , https://aimehq.org/themes/custom/aime/doclibrary/library/pirotechnia-of-vannoccio-biringuccio-1943.html |
| V-22 | [2-02] カリ | 年 -3000 | △（年を直す案） | -3000 を直接支える出典なし。シュメール（ギルス）の粘土板に約4500年前、羊毛を洗った記録 → -2500 にして ⚠️ を残す案 | https://en.wikipedia.org/wiki/Potash , https://www.earthdate.org/files/000/002/734/EarthDate_274_C.pdf |
| V-23 | [2-09] 石けん | 紀元前2800年頃バビロニアの粘土板に処方 | ✖（一部） | 正しくは「紀元前2800年頃のバビロンの粘土の円筒から石けんのような物質（刻文は『灰を脂と煮た』）。処方の粘土板はもっと後（紀元前2500年頃とも2200年頃とも）」。年 -2800 はそのまま、粘土板の年代は ⚠️ を残す | https://en.wikipedia.org/wiki/Soap |
| V-24 | [1-46] 珪石 | 年 -2500 | △ | 何の出来事の年か本文から読めない。水晶の利用は中石器時代の刃物や青銅器時代のビーズ・印章までさかのぼる。年が何を指すかを決めて書き直す | https://www.iranicaonline.org/articles/crystal-rock-bolur-bolur-e-kuhi/ |
| V-25 | [2-03] 天然ソーダ | ナトロンでミイラ作り・年 -2500 | ○（年を直す） | 最初の証拠は古王国第4王朝（紀元前2600年頃、ヘテプヘレス1世の内臓箱）。「第4王朝（紀元前2600年頃）」と書けば ○、年 -2600 | https://www.spurlock.illinois.edu/exhibits/online/mummification/history2.html , https://giza.fas.harvard.edu/objects/54956/full/ |
| V-26 | [2-14] ソーダ石灰ガラス | 紀元前2500年頃メソポタミア（最初のガラス） | △ | コーニングガラス美術館は約4000年前（紀元前2000年頃）、Wikipedia などは紀元前2500年頃のビーズ。「紀元前2500〜2000年頃、メソポタミアのビーズ」として ⚠️ を残す | https://whatson.cmog.org/exhibitions-galleries/origins-glassmaking , https://en.wikipedia.org/wiki/History_of_glass |

まとめ（第1弾、26 件）：◎ 12・○ 3・△ 6・✖ 4・◎と△の混在 1（[1-43] ビリングッチョ）（[0-10] 石灰の例、[0-15] 焼成レンガ、[1-27] 鉛の年、[2-09] 石けんの粘土板）。年を直す候補：[0-06] -8000、[1-27] -4000 前後、[2-03] -2600、[2-02] -2500、[11-10] 範囲 -7000〜。

### 第2弾（2026-10-20）本線の続き 20 ノード・32 件
| # | ノード | 主張 | 判定 | 結果 | 出典 |
|---|---|---|---|---|---|
| V-27 | [0-08] 木工 | 年 -9000（磨製石斧の始まり） | △ | 「始まり＝-9000」とする出典なし。旧石器にも例外的な磨製の例があり、中欧には紀元前6千年紀半ば。新石器の始まりという慣例の年として ⚠️ を残す | https://www.gibmuseum.gi/news/polished-neolithic-axe-from-gorhams-cave-85 , https://exarc.net/ark:/88735/10196 |
| V-28 | [6-04] 岩塩鉱 | 年 -1500 | ✖ | 正しくは「最古の岩塩採掘は紀元前4500年頃（アゼルバイジャン・ナヒチェヴァンのドゥズダグ、土器の型式による推定）。-1500 はハルシュタット（オーストリア）の青銅器時代の採掘（紀元前16〜15世紀）」。年を直すか「-1500＝ハルシュタット」と書き分ける | https://sciencedaily.com/releases/2010/11/101125201448.htm , https://www.nhm-wien.ac.at/hallstatt/en/salt_mine/bronze_age |
| V-29 | [3-04] ふいご | 年 -1450、レクミラの墓の壁画（紀元前15世紀頃） | ◎ | メトロポリタン美術館：TT100 は紀元前1479〜1425年頃、足踏みふいごで銅を溶かす場面 | https://www.metmuseum.org/art/collection/search/544617 , https://library.biblicalarchaeology.org/images/bsba100404510ljpg/ |
| V-30 | [1-06] 塊鉄炉 | 年 -1200 | △ | 「紀元前1200年頃アナトリア」と「紀元前1400年頃ヒッタイト」の両説。⚠️ を残す | https://www.tf.uni-kiel.de/matwis/amat/iss/kap_a/backbone/ra_3_1.html , https://www.chemeurope.com/en/encyclopedia/History_of_ferrous_metallurgy.html |
| V-31 | [1-06] 塊鉄炉 | ヒッタイト周辺 | △ | インド・アナトリア・カフカスのいずれかとする出典もあり、ヒッタイトに限るのは不確か | 同上 |
| V-32 | [1-08] 鍛鉄 | 年 -1200 | △ | 根拠は [1-06] と同じで幅がある（紀元前1400〜1200年頃） | 同上 |
| V-33 | [1-45] 水銀 | 年 -500 | △ | 水銀そのものはエジプト・クルナの墓（紀元前15〜16世紀）。文字の最古の取り出し方はテオフラストス（紀元前4世紀、辰砂を酢とこする方法で、焼く方法ではない）。-500 を支える出典なし | https://www.chemistryexplained.com/elements/L-P/Mercury.html , https://chemistryworld.com/news/botanists-chemists-and-historians-come-together-to-recreate-ancient-alchemy-of-making-mercury/4015894.article |
| V-34 | [2-16] 蒸留水 | アレクサンドリアのマリア（1〜3世紀）の装置 | ◎ | 三本腕の蒸留器（トリビコス）。伝えたのはゾシモス（300年頃）で、発明者かは不明と書くとよい | https://en.wikipedia.org/wiki/Mary_the_Jewess , https://4kyws.ua.edu/HEBRAEA.html |
| V-35 | [2-16] 蒸留水 | 年 200（100〜300） | ○ | マリアの年代と合う（マリアの年代に頼った値） | https://en.wikipedia.org/wiki/Mary_the_Jewess |
| V-36 | [2-16] 蒸留水 | 8〜9世紀ジャービル・イブン・ハイヤーンのアランビック | △（言い換え） | ジャービル文書は 850〜950年頃に書かれたとされ、一人の実在の人物かも議論がある。アランビックとの結びつきの出典なし。「9〜10世紀のジャービル文書」と言い換える案 | https://en.wikipedia.org/wiki/Jabir_ibn_Hayyan , https://www.nlm.nih.gov/hmd/arabic/alchemy10.html |
| V-37 | [1-13] 亜鉛 | 年 1150、インド・ザワールで12世紀頃 | ◎ | 12世紀〜19世紀初めに操業（坑道の木材の年代はさらに古い） | https://mci.si.edu/node/1187917 , https://esamskriti.com/e/Culture/Indian-Culture/Zinc-Mining-In-Zawar-Region-Of-Rajasthan~Interview-With-Dr.-Paul-T.-Craddock-1.aspx |
| V-38 | [6-16] 天然硝石の採取 | 中国の火薬は9世紀頃 | ○ | 808年の道教の文書が最古の処方、1044年『武経総要』がまとまった処方の最古。出典は Wikipedia 系のみ | https://en.wikipedia.org/wiki/History_of_gunpowder |
| V-39 | [6-16] 天然硝石の採取 | 年 1250、ヨーロッパは13世紀から | ○ | 1267年ロジャー・ベーコンの著作に処方。欧州で硝石を採った記録そのものは未確認 | https://en.wikipedia.org/wiki/History_of_gunpowder , https://books.rsc.org/books/monograph/149/chapter/80602/Historical-Introduction |
| V-40 | [2-43] ばん油 | 9〜10世紀のイスラム世界の錬金術師 | △ | ラーズィーが緑礬を乾留したとする二次資料はあるが、確かなのは鉱物の分類に入れたことだけ。ジャービルの名も伝承 | https://en.wikipedia.org/wiki/Vitriol , https://old.vscht.cz/chem_listy/common/article-vol_96-issue_12-page_997.html |
| V-41 | [2-43] ばん油 | 13世紀頃ヨーロッパ・年 1300 | ◎ | ヴァンサン・ド・ボーヴェ、偽ゲーベル『完全大全』などに言及 | 同上 |
| V-42 | [6-11] 硝酸 | 年 1300（硝石＋ばん類） | ○ | 偽ゲーベル『Summa perfectionis』（1310年少し前）が最初期の記述 | https://en.wikipedia.org/wiki/Pseudo-Geber , https://old.vscht.cz/chem_listy/common/article-vol_96-issue_12-page_997.html |
| V-43 | [6-11] 硝酸 | 1648 グラウバー法 | ○ | 硝石＋ばん油で大量に作った（最初の人ではない） | https://www.cavemanchemistry.com/cavebook/chsaltpeter.html |
| V-44 | [1-21] 蛍石 | 年 1546（アグリコラ） | ✖ | 正しくは 1530年、アグリコラ『Bermannus』で融剤「fluores」として初めて活字に。1546年は『De Natura Fossilium』で、取り違えらしい | https://en.wikipedia.org/wiki/Fluorite , https://en.wikipedia.org/wiki/History_of_fluorine |
| V-45 | [5-01] 手押しピストンポンプ | 1650頃 ゲーリケがポンプ製作 | ◎ | 1650年（1655年頃とする資料もある） | https://en.wikipedia.org/wiki/Otto_von_Guericke , https://pages.mtu.edu/~pcharles/SCIHISTORY/Guericke.html |
| V-46 | [5-01] 手押しピストンポンプ | 1654 レーゲンスブルクの公開実験 | ◎ | 帝国議会、皇帝フェルディナント3世の前 | https://en.wikipedia.org/wiki/Magdeburg_hemispheres , https://physicstoday.aip.org/backscatter/magdeburg-hemispheres |
| V-47 | [5-01] 手押しピストンポンプ | 1657 マクデブルクで馬16頭 | △ | 1656年と1657年が混在（1657年はショットの本の年の可能性）、馬も16頭と30頭で食い違う | https://catalogue.museogalileo.it/indepth/MagdeburgHemispheres.html , https://en.wikipedia.org/wiki/Magdeburg_hemispheres |
| V-48 | [5-01] 手押しピストンポンプ | 半球の直径 約40cm | △ | 30〜50cm でばらつく（Wikipedia・ガリレオ博物館は約50cm） | https://en.wikipedia.org/wiki/Magdeburg_hemispheres , https://omeka-s.grinnell.edu/s/physicsmuseum/item/5200 |
| V-49 | [5-01] 手押しピストンポンプ | 到達圧力 約数百Pa | △ | 測定値の出典なし | — |
| V-50 | [7-03] レンズ研磨 | 17世紀からベンガラで研磨 | △ | 酸化鉄の研磨剤がのちに CeO2 に置き換わったことは確認。17世紀からの出典なし | https://cameo.mfa.org/wiki/Optical_rouge |
| V-51 | [2-23] 木材乾留 | 年 1661 ボイルが木精を記述 | ◎ | ツゲ材の乾留で「spirit of box」（原典は未確認） | https://graphsearch.epfl.ch/en/concept/19712 , https://kids.britannica.com/students/article/methanol/275810 |
| V-52 | [2-23] 木材乾留 | 木酢液は酢酸数%・メタノール1〜3% | ○ | 45樹種の実測で酢酸1.2〜9.5%・メタノール0.2〜3.8%。「〜4%程度」とすると安全 | https://doaj.org/article/3084c45384e54c2c934c253895540324 , https://www.fao.org/4/X5328E/x5328e0d.htm |
| V-53 | [7-05] 反射鏡 | 1932 ストロングのアルミ蒸着 | ✖ | 正しくは 1930年頃（カリフォルニア工科大）。1932〜37年はパロマー計画の期間、詳しい論文は1936年 | https://en.wikipedia.org/wiki/Silvering , https://ftp.astro.caltech.edu/palomar/audioguide/mirror_en.html |
| V-54 | [1-20] 白金 | 年 1748（ウジョアの記述） | ◎ | 1748年『Relación Histórica』。ウッドが1741年に試料を英国へ持ち込んでいるので「最初の記述」と書く | https://wpd.ugr.es/~gervilla/wp-content/uploads/2022/01/Edad-del-platino-en-Espana.pdf , https://technology.matthey.com/content/journals/10.1595/003214092X3614047 |
| V-55 | [1-23] 輝水鉛鉱 | 年 1778（シェーレ） | ◎ | 黒鉛でも鉛の鉱石でもなくモリブデンの硫化物と示した | https://www.chemicool.com/elements/molybdenum.html , https://winter.group.shef.ac.uk/webelements/molybdenum/history.html |
| V-56 | [1-40] クロム | 1895 ゴルトシュミットのテルミット法の特許 | ◎ | DRP 96317（1895年3月13日） | https://en.wikipedia.org/wiki/Hans_Goldschmidt , https://www.goldschmidt.com/en/goldschmidt-group/history/ |
| V-57 | [1-40] クロム | 1898年頃に炭素を含まない Cr | △ | 製造は確認、1898年の裏づけなし（1898年は論文の年）。「1890年代後半」とぼかす案 | https://www.spektrum.de/lexikon/chemie/aluminothermisches-verfahren/344 |
| V-58 | [2-07] 重クロム酸カリウム | 6価クロムは発がん性 | ◎ | IARC グループ1（第49巻 1990、第100C巻 2012） | https://www.carexcanada.ca/?p=9477 , https://insst.es/documents/d/portal-insst/sheet-n-7-chromium-vi-ano-2023 |

まとめ（第2弾、32 件）：◎ 11・○ 6・△ 12・✖ 3（[6-04] 最古の岩塩採掘、[1-21] 蛍石は1530年、[7-05] アルミ蒸着は1930年頃）。
ネットワーク担当のファイルで直したもの：network_meta.json のジャービルの人物カード（V-36 に合わせて「9〜10世紀のジャービル文書」、道具をアランビックから錬金術の文書に）。
