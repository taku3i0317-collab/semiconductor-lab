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
| W-3 | キャサリン・ブロジェット（1898〜1979） | 1938 ステアリン酸バリウムの単分子膜を44層重ね、ガラスの反射を打ち消す「見えないガラス」（特許 2,220,660、ラングミュア＝ブロジェット膜）（https://en.wikipedia.org/wiki/Katharine_Burr_Blodgett） | 技術ノード「反射防止膜」が無い。足す案：幹7（光学）、レンズ・投影レンズ [7-19] の前提か理解に。人物カードはノードができてから | 済み（O-015 H-04：人物カードを作った） |
| W-4 | グレース・ホッパー（1906〜1992） | 1952 A-0 システム（初期のコンパイラ。「世界初」とする言い方には諸説 ⚠️）、のち COBOL（https://en.wikipedia.org/wiki/A-0_System 、https://en.wikipedia.org/wiki/Grace_Hopper） | 技術ノード「コンパイラ（プログラミング言語）」が無い。足す案：幹10、[10-07] 計算機の後・[10-16] ニューラルネットの前（AI を動かすソフトの土台）。年と出典はノードを作るときに確かめる | 済み（O-015 H-02：人物カードを作った） |
| W-5 | リン・コンウェイ（1938〜2024） | 1979 カーヴァー・ミードと『Introduction to VLSI Systems』、拡大縮小できる設計ルールで IC 設計の教え方と進め方を変えた（https://en.wikipedia.org/wiki/Lynn_Conway 、https://computerhistory.org/blog/in-memoriam-lynn-conway-1938-2024/） | 技術ノード「VLSI 設計法（設計ルール）」が無い。足す案：幹10 か 9、[10-13] マイクロプロセッサの後・[B-11] AIチップの前 | 済み（O-015 H-03：人物カードを作った） |
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

### 第3弾（2026-10-20）本線の続き 20 ノード・27 件
| # | ノード | 主張 | 判定 | 結果 | 出典 |
|---|---|---|---|---|---|
| V-59 | [8-02] ねじ切りと互換性部品 | 互換性部品はブラン（1780年代）と米国の兵器工場（1820年代〜） | ◎ | ブランは1785年ヴァンセンヌで実演。ハーパーズ・フェリーのホール M1819 ライフル最初の1000丁が1824年、1826年の検査で部品を混ぜても組めた。「1824 ホール」を補うとよい | https://en.wikipedia.org/wiki/Honor%C3%A9_Blanc , https://www.nramuseum.org/the-museum/the-galleries/the-prospering-new-republic/case-34-the-mexican-war/us-hall-model-1819-breechloading-percussion-rifle.aspx |
| V-60 | [6-01] 電解槽 | 年 1800（ボルタ電堆で水を電気分解） | ◎ | 1800年5月、ニコルソンとカーライル | https://en.wikipedia.org/wiki/William_Nicholson_(chemist) , https://beeld.teylersmuseum.nl/digital_library/Emags/Van_Marum%27s_Scientific_Instruments/page_112.html |
| V-61 | [2-19] 過酸化水素 | 1908 ヴァイセンシュタインで電解法を工業化 | △ | 場所は正しい。Evonik の資料どうしで年が食い違い、「1908 建設・1910 生産開始」と書けば外せる（独立した2つ目の出典なし） | https://www.evonik.com/en/company/worldwide-locations/europe/austria/weissenstein.html , https://history.evonik.com/en/inventions/hydrogen-peroxide |
| V-62 | [1-24] ボーキサイト | 年 1821（ベルティエがレ・ボー近くで発見） | ◎ | 名前は村から。1847 beauxite、1861 bauxite | https://en.wikisource.org/wiki/1911_Encyclop%C3%A6dia_Britannica/Bauxite , https://www.earthmagazine.org/article/march-23-1821-bauxite-discovered/ |
| V-63 | [4-08] 電動モーター | 年 1821（1821〜1873） | ◎ | 1821年9月 ファラデーの電磁回転、1873年ウィーン万博でグラムの発電機がモーターとして回った。「最初のモーター」という呼び方は誇張という指摘あり | https://blog.sciencemuseum.org.uk/a-small-revolution-michael-faraday-and-the-origins-of-the-electric-motor/ , https://britannica.com/technology/Gramme-dynamo |
| V-64 | [3-12] 耐火れんが | 年 1822、珪石れんが（ディナス）は W・W・ヤング | ◎ | 1822年に特許（No.5047）、ディナスの珪石＋石灰約1%（Scottish Brick History はヤング説に「?」） | https://croydoncavingclub.org.uk/node/574 , https://www.scottishbrickhistory.co.uk/early-references-to-fire-brick-manufacture-in-the-uk/ |
| V-65 | [1-25] モナザイト | 年 1829 | ◎ | 1829年ブライトハウプトが命名、模式産地はウラルのイリメニ山地 | https://www.typmineral.uni-hamburg.de/tables/en/monazite-ce.html , https://en.wikipedia.org/wiki/Monazite |
| V-66 | [I-01] 電磁誘導 | ジョセフ・ヘンリーもほぼ同じころ独立に発見 | ○（ideas.md を直した） | 独立の発見は確か。ただし発表は1832年で、主な発見は自己誘導。ideas.md の本物を「1832年、ジョセフ・ヘンリーが独立に（とくに自己誘導を）見つけた」に直した | https://siarchives.si.edu/collections/siris_sic_12470 , https://en.wikipedia.org/wiki/Joseph_Henry |
| V-67 | [2-25] フェノール | クメン法は1944年ホック | ○ | 1944年ホック（ラングと共同）は正しい。1942年にソ連のウドリスとセルゲーエフが先に発明。「1942 ウドリスら、1944 ホックが独立に」 | https://en.wikipedia.org/wiki/Cumene_process , https://www.gophotonics.com/whitepapers/details/5319-ft-nir-analysis-of-the-hock-process-for-the-production-of-phenol-and-acetone |
| V-68 | [2-26] メタノール | 1923 BASF のピア・ミッタシュらが合成を工業化 | ◎ | 1923年7月ロイナで生産開始。触媒・圧力の数字は出典で食い違うので書かない | https://en.wikipedia.org/wiki/Alwin_Mittasch , https://www.deutsche-biographie.de/sfz95868.html |
| V-69 | [7-02] 溶融石英 | 1839 ゴーダンが酸水素炎で石英を溶かした | △ | ゴーダンが溶かして糸にしたことは19世紀の記述で確認。「1839年」の原典は見つからない | https://www.biblio.liuc.it/opere_cattaneo/politecnico/pdf/FusioneDellAcidoSilicico.pdf , https://www.nature.com/articles/143826a0.pdf |
| V-70 | [7-02] 溶融石英 | 1899 ヘレウス社が工業化 | ◎ | 1899年 Küch／ヘレウスが泡のない石英ガラス（部門の設立は1901年） | https://www.heraeus-group.com/en/about-heraeus/history , https://en.wikipedia.org/wiki/Heraeus |
| V-71 | [2-44] 赤リン | 真空管のゲッター（1894 マリニャーニ） | △ | 導入1893・伊特許1894・米特許1895年4月で食い違う。「1893〜1895（伊特許1894）」と幅で | https://lamptech.co.uk/Documents/IN%20Getters.htm , https://forumeditrice.it/percorsi/storia-e-societa/varia/arturo-malignani.-con-il-futuro-negli-occhi/luomo-che-con-i-suoi-bulbi-bianchi-incanto-edison/file |
| V-72 | [1-07] 黒鉛 | 年 1850（19世紀からるつぼ材） | △ | 1850年の出来事は見つからない。黒鉛るつぼはディクソン1827年（米）、モーガン兄弟1856年（英）。年を1827年か「1827〜1856」に | https://en.wikipedia.org/wiki/Joseph_Dixon_(inventor) , https://www.britannica.com/biography/Joseph-Dixon |
| V-73 | [2-42] 酢酸 | 年 1850（木酢液→酢酸カルシウム→硫酸） | △ | 流れは確認、1850年の根拠なし（代表の年） | https://www.encyclopedia.com/science-and-technology/chemistry/organic-chemistry/acetic-acid , https://www.chemeurope.com/en/encyclopedia/Acetic_acid.html |
| V-74 | [2-42] 酢酸 | 酢酸セルロースは無水酢酸＋硫酸触媒 | ◎ | 1865年シュッツェンベルガーが合成、今の工業法は硫酸などの触媒 | https://en.wikipedia.org/wiki/Cellulose_acetate , https://www.britannica.com/print/article/101663 |
| V-75 | [5-12] 真空シール | O リングは1937 クリステンセンの特許 | ○ | 1937年は米国の出願年（特許は1938〜39年）。「1937年出願」と書く | https://en.wikipedia.org/wiki/Niels_Christensen , https://www.inventionandtech.com/node/86858 |
| V-76 | [5-12] 真空シール | 1962頃 バリアンのコンフラット | △ | ウィーラーが1960年にバリアンに入って開発、特許は1960年代初め。「1960年代初め」なら ○ | https://avs.org/awards/awards/awardee-interviews/william-r-wheeler/bio/ , https://en.wikipedia.org/wiki/Vacuum_flange |
| V-77 | [5-12] 真空シール | 1910年代のジュメット線 | ○ | GE のフィンクのジュメット線が1913年から白金線に代わった。年を示すのは同じ電球史サイトだけ | https://lamptech.co.uk/Documents/People%20-%20Fink%20CG.htm , https://wirenet.org/the-book-of-terms/dumet-wire |
| V-78 | [5-12] 真空シール | 年 1850（1850〜1937） | △ | すり合わせガラス＋グリースの1850年の根拠なし（終わりの1937年は O リング出願と合う） | — |
| V-79 | [4-05] 自励式発電機 | 年 1866（1866〜67） | ◎ | ヴァーリーの出願1866年12月、ジーメンスとホイートストンの発表1867年 | https://en.wikipedia.org/wiki/Dynamo , https://www.gracesguide.co.uk/S._A._Varley |
| V-80 | [4-05] 自励式発電機 | ジーメンス・ホイートストンら、ほぼ同時 | ◎ | 「ら」にヴァーリーを名前で入れるとよい | https://www.nature.com/articles/145772c0.pdf , https://www.vde.com/de/geschichte/chronik/generatoren |
| V-81 | [4-01] 水車発電機 | 年 1878（初期の発電所は水力） | ◎ | 1878年 英国クラッグサイドでアームストロングが水力の電気でアーク灯。「世界初の水力発電所」とは言わない | https://www.nationaltrust.org.uk/visit/north-east/cragside/history-of-cragside , https://collection.sciencemuseumgroup.org.uk/people/ap29986 |
| V-82 | [3-19] 副産物回収式コークス炉 | 年 1881 | ○ | ドイツ最初の大きな副産物回収コークス工場が1881年5月（ヒュッセナー） | https://gracesguide.co.uk/Albert_Hussener , https://ausimm.com/publications/conference-proceedings/the-ausimm-proceedings-1928/modern-by-product-coke-oven-practice |
| V-83 | [3-19] 副産物回収式コークス炉 | 1881頃 オットーとホフマンが実用化 | △ | オットー・ホフマン炉は確認、1881年との結びつきなし。「1881 ドイツで実用化（ヒュッセナーら）、まもなくオットー・ホフマン炉」 | https://www.gracesguide.co.uk/Otto-Hoffmann , https://gracesguide.co.uk/Albert_Hussener |
| V-84 | [4-03] 銅ケーブル | 年 1882 | ◎ | 1882年9月4日パール街発電所、銅の導体を地下の管で配線 | https://ethw.org/Milestones:Pearl_Street_Station , https://ethw.org/w/images/a/ae/Edison_and_Pearl_Street,_Text,_031410.pdf |
| V-85 | [4-10] 変圧器 | 年 1885 | ◎ | 1885年ガンツ社のジペルノフスキー・ブラーティ・デーリが閉じた鉄心の変圧器（ZBD）を出願 | https://en.wikipedia.org/wiki/Ott%C3%B3_Bl%C3%A1thy , https://pp.bme.hu/ee/article/view/4637 |

まとめ（第3弾、27 件）：◎ 14・○ 5・△ 8・✖ 0。年を書き直す候補：[1-07] 1827（〜1856）、[2-44] 1893〜1895、[5-12] コンフラット「1960年代初め」・ジュメット線 1913、[2-19]「1908 建設・1910 生産」。

## DESIGN への提案
| # | 内容 | 理由 | 状態 |
|---|---|---|---|
| D-1 | ゲーム内でモノを作ったとき、その裏にいる人たちの肖像が並ぶ演出（例：画面の下を肖像が年代順に流れ、最後に「名前の残っていない、無数の人たち」のシルエットと「N 人の発明の上に、いま君が作った」） | ウェブの「人の壁」（NET-04）と同じ数え方・同じ肖像（network_meta.json）を使える。技術は一人ではなく大勢の積み重ねだと伝わる。モノ（種類: モノ・products.md）を作った節目だけにして、毎回は出さない（うるさくしない） | ✅ 反映（NET-06：DESIGN のゲーム内画面の構想に追記） |

### 第4弾（2026-10-20）本線の続き 20 ノード・38 件
| # | ノード | 主張 | 判定 | 結果 | 出典 |
|---|---|---|---|---|---|
| V-86 | [4-12] 電気炉 | 年 1890（黒鉛の抵抗加熱炉） | △ | 「最初の黒鉛抵抗炉」の年は特定できない。早い例はアチソンの黒鉛芯の炉（SiC 特許1893、炉の特許1896）とモアッサン1892。「1890年代」としてなら妥当 | https://en.wikipedia.org/wiki/Acheson_process , https://usgennet.org/usa/topic/preservation/science/inventions/chpt27.htm |
| V-87 | [5-16] ゲッター | 1890年代にマリニャーニが電球に赤リン（人物・材料） | ◎ | 2つの出典で一致 | https://lamptech.co.uk/Documents/IN%20Getters.htm , https://forumeditrice.it/percorsi/storia-e-societa/varia/arturo-malignani.-con-il-futuro-negli-occhi/luomo-che-con-i-suoi-bulbi-bianchi-incanto-edison/file |
| V-88 | [5-16] ゲッター | 年 1890 | △ | 1893（発明）・1894（伊特許）・1895（米特許 537,693）で分かれる。年は 1894 か「1893〜1895」に（V-71 と同じ） | 同上 |
| V-89 | [5-16] ゲッター | 初期の真空管は Mg、のちに Ba | △ | 「リン→Ba・Mg」とする愛好家サイトが中心。順番は確かめられない（1920年代初めには普及） | https://r-type.org/addtext/add007.htm , https://en.wikipedia.org/wiki/Getter |
| V-90 | [3-15] アーク炉 | 1802 ペトロフのアーク | ◎ | 1802年（発表1803年） | https://en.wikipedia.org/wiki/Electric_arc_furnace , https://www.ieap.uni-kiel.de/plasma/ag-kersten/sites/default/files/media/oeffentlichkeitsarbeit/gas_discharge_history/poster19thcentury.pdf |
| V-91 | [3-15] アーク炉 | 1808頃 デービーのアーク放電 | △ | 出典により1808〜1810年。「1808〜1810年」と幅で | https://en.wikipedia.org/wiki/Electric_arc_furnace , https://www.electrochem.org/category/ecs-classics |
| V-92 | [3-15] アーク炉 | 1892 モアッサンの電気炉、同年ウィルソンのカーバイド | ◎ | ウィルソンは1892年5月2日にアーク炉でカーバイド、モアッサンも同年 | https://www.biographi.ca/en/bio/willson_thomas_leopold_14E.html , https://dncr.nc.gov/blog/2016/05/02/discovery-calcium-carbide-process |
| V-93 | [3-15] アーク炉 | 1900頃 エルーの製鋼アーク炉 | ◎ | 1900年10月9日フランス・ラ・プラで操業（特許1899） | https://techniques-ingenieur.fr/en/resources/article/ti554/electric-steelworks-m7700/v1/history-of-the-arc-furnace-1 , https://en.wikipedia.org/wiki/Electric_arc_furnace |
| V-94 | [3-15] アーク炉 | モアッサンの炉は直流 | △ | 出典が見つからない | — |
| V-95 | [1-15] 金属シリコン | 年 1900、電気炉での工業生産は1900年頃〜 | ○ | 総説は「20世紀初めから電気炉で Si 金属と高 Si 合金の開発」。年の決め手はない | https://faculty.kfupm.edu.sa/RI/srehman/Projects/CER2280/RI%20projectLiteratureRev/760400.pdf , https://runeberg.org/sweden14/2/0319.html |
| V-96 | [6-06] 塩酸 | 年 1900（クロルアルカリの H2・Cl2 を燃やして HCl） | ○ | IARC モノグラフ：クロルアルカリの発達（20世紀初め）のあと塩素を水素中で燃やす合成が主流に。最初の工場の年は不明、「20世紀初め」なら妥当 | https://ncbi.nlm.nih.gov/books/NBK424680 |
| V-97 | [7-10] 水銀ランプ | 年 1901 クーパー・ヒューイットの低圧水銀灯 | ◎ | 1901年の発明・特許・発売で一致 | https://en.wikipedia.org/wiki/Peter_Cooper_Hewitt , https://www.britannica.com/print/article/264522 |
| V-98 | [7-10] 水銀ランプ | 1906 キューヒとレチンスキーの石英管高圧水銀灯 | ○ | 1906年 Annalen der Physik。シーメンスの発明とする年表もあり食い違う | https://www.lamptech.co.uk/Documents/M7%20Kuch%20%26%20Retschinsky.htm , https://identity.wellcomecollection.org/works/mw8ucqpn |
| V-99 | [4-11] 整流器 | 年 1904 | ✖ | 正しくは 1902：クーパー・ヒューイットの水銀整流器が最初の電力用の静止形整流器（フレミング二極管1904より2年早い）。1904 を残すなら「真空管の整流器の年」と書き分ける | https://en.wikipedia.org/wiki/Mercury-arc_valve , https://www.research.unipd.it/bitstream/11577/3271203/1/IEMPowerElectronics.pdf |
| V-100 | [5-02] 真空管 | 1914 ラングミュアのトリウム入り W フィラメント | ○ | 1914年7月に GE で特許出願（論文の年は未確認） | https://r-type.org/articles/art-249.htm |
| V-101 | [5-04] 油回転ポンプ | 年 1905 ゲーデの回転水銀ポンプ | ◎ | Leybold の社史・SVC の資料で一致 | https://www.leybold.com/en/about-us/history/our-founders , https://www.svc.org/clientuploads/directory/resource_library/02_551.pdf |
| V-102 | [5-04] 油回転ポンプ | 油で封じる回転ポンプ 1907頃 | ◎ | 1907年ゲーデ。「頃」は外してよい | https://www.svc.org/clientuploads/directory/resource_library/02_551.pdf , https://www.chemistryworld.com/opinion/gaedes-diffusion-pump/3004912.article |
| V-103 | [5-14] 真空蒸着 | 年 1912 ポールとプリングスハイム | ◎ | 1912年に真空で金属を蒸発させ鏡の膜。前史に1887ナールボルトがあるので「最初」とは書かない | https://en.wikipedia.org/wiki/Evaporation_(deposition) , https://articles.adsabs.harvard.edu/pdf/1936ApJ....83..401S |
| V-104 | [5-14] 真空蒸着 | 1930年代 ストロングが望遠鏡の鏡にアルミ蒸着 | ◎ | 1934年にはウィルソン山100インチ鏡も（V-53 の「1930年頃」と合う） | https://en.wikipedia.org/wiki/Evaporation_(deposition) , https://digital.archives.caltech.edu/collections/Images/10.12-48 |
| V-105 | [I-06] 偏析 | ガリバー 1913 の式 | ○ | Glicksman & Hills（2001）が「遅くとも1913年」。主著は1922年の本 | https://researchportal.hw.ac.uk/en/publications/non-equilibrium-segregation-during-alloy-solidification/ , https://en.wikipedia.org/wiki/Scheil_equation |
| V-106 | [I-06] 偏析 | シャイル 1942 の式 | ○ | Z. Metallkd. 1942, 34, 70-72（Wikipedia の文献表）。Springer の論文の引用は雑誌名が食い違う | https://en.wikipedia.org/wiki/Scheil_equation , https://link.springer.com/article/10.1007/BF00652037 |
| V-107 | [I-06] 偏析 | 1952 ベル研プファンが帯域精製 | ◎ | Pfann "Principles of Zone-Melting"（JOM 4, 747-753, 1952） | https://onemine.org/documents/institute-of-metals-division-principles-of-zone-melting , https://en.wikipedia.org/wiki/William_Gardner_Pfann |
| V-108 | [3-14] 誘導加熱炉 | 1900頃 キェリン（スウェーデン）の低周波誘導炉 | △ | 操業開始を1900年とする教科書と1903年とする社史。「1900〜1903頃」 | https://cdigital.cabu.uanl.mx/fg/11/1020061217/1020061217_022.pdf , https://abpinduction.com/en/?p=7473 |
| V-109 | [3-14] 誘導加熱炉 | 年 1916、ノースラップの高周波炉 | ✖ | 正しくは 1918年（プリンストン大で実演、論文1919年）。1916年の出典なし | https://foundrytradejournal.com/features/past-present-future-induction-technology , https://www.engineering.org.cn/en/top_journal_details/41479 |
| V-110 | [3-16] 真空炉 | 年 1920、工業化は1920年代〜 | △ | 1929年に Raytheon が最初の真空炉を発注（Ipsen 社史）だけ。「最初の真空炉は1950年代末〜60年代初め」とする資料もある | https://ipsenglobal.com/?p=10205 , https://asm.confex.com/asm/ht2011/webprogram/Paper28229.html |
| V-111 | [7-15] 紫外光学材料 | CaF2 はブリッジマン・ストックバーガー法（1920〜30年代） | ◎ | ブリッジマン1925、ストックバーガー1927〜、改良1935年頃 | https://en.wikipedia.org/wiki/Bridgman%E2%80%93Stockbarger_method , https://publicacions.iec.cat/repository/pdf/00000121\00000024.pdf |
| V-112 | [7-15] 紫外光学材料 | 合成石英は SiCl4 の火炎加水分解 | ◎ | 1934年コーニングのハイド（米国特許 2,272,342）。特許は「酸素の炎」で「酸水素炎」とは書いていない | https://en.wikipedia.org/wiki/James_Franklin_Hyde , https://optica-opn.org/home/articles/volume_21/issue_10/features/making_the_first_low-loss_optical_fibers |
| V-113 | [8-07] 除振と恒温 | 長さの標準温度20℃は1931年に国際採用 | ◎ | 1931年4月に CIPM が全会一致、ISO 1 は1975年 | https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4654601/ , https://en.wikipedia.org/wiki/ISO_1 |
| V-114 | [1-19] 希土類分離 | 年 1940（1940〜50）、スペディングのイオン交換・溶媒抽出 | ◎ | イオン交換は1940年代、1947年 JACS。溶媒抽出は別の流れなので分けて書く | https://en.wikipedia.org/wiki/Frank_Spedding , https://onemine.org/documents/extractive-metallurgy-division-the-separation-of-rare-earths-by-ion-exchange |
| V-115 | [1-19] 希土類分離 | ブキメラ（1980年代、三菱化成系）の残渣問題 | ◎ | 1982年操業、1985年提訴、1992年閉鎖 | https://phys.org/news/2012-06-toxic-legacy-malaysia-rare-earths-village.html , https://consumer.org.my/?p=559 |
| V-116 | [1-19] 希土類分離 | ライナス社の残渣保管問題は今も続く | ◎ | 2026年3月の決定で残渣の発生は2031年までに終える | https://www.malaymail.com/news/malaysia/2026/03/06/lynas-stays-another-decade-no-new-radioactive-dump-wlp-to-cease-by-2031/211209 , https://www.greenpeace.org/malaysia/press/63383/digging-deep-into-the-latest-developments-on-lynas-and-the-state-of-the-rare-earth-industry-in-malaysia |
| V-117 | [2-32] ポジ型フォトレジスト | 1940年代 カレ社のオスカー・ズース | ○ | Allresist は1940年、百度百科は1949年。独立した信頼できる出典2つがそろわない | https://www.allresist.com/forays-through-the-lithography-of-microelectronics-matthias-schirmer/ , https://baike.baidu.com/en/item/photoresist/1470078 |
| V-118 | [2-32] ポジ型フォトレジスト | 1970年代にシップレー社の AZ 系が IC の主力に | ○ | AZ-1350 は1965年発表。投影露光に移った「1970年代後半」にネガ型と入れ替わり。同じ館のページ2つ | https://www.shmj.or.jp/english/pdf/em/exhibi2437E.pdf , https://shmj.or.jp/english/pdf/em/exhibi2447E.pdf |
| V-119 | [2-32] ポジ型フォトレジスト | DNQ はナフタレン＋亜硝酸でジアゾ化 | △ | 合成の道すじの出典なし。単純化しすぎの可能性 | — |
| V-120 | [8-08] ボールねじ精密ステージ | 19世紀末に特許 | ◎ | 1898年 米国特許 601,451・610,044 | https://en.wikipedia.org/wiki/Ball_screw , https://www.machinedesign.com/mechanical-motion-systems/article/21834593/a-short-history-on-ball-screws |
| V-121 | [8-08] ボールねじ精密ステージ | 年 1940、GM がステアリングで量産 | ○ | 1940年代に自動車のステアリングで普及、GM サギノーの特許 US2267524（1939出願・1941登録）。量産開始の年は未確認 | https://en.wikipedia.org/wiki/Ball_screw , https://patents.google.com/patent/US2267524 |
| V-122 | [8-14] 鏡筒の精密組立 | 年 1950 | △ | 1950年代の心出しの手順の出典なし | https://spiedigitallibrary.org/conference-proceedings-of-spie/0381/0000/Centering-Optical-Lenses/10.1117/12.934838.full |
| V-123 | [1-16] トリクロロシラン合成・蒸留 | 年 1953 | △ | シーメンス法の特許 DE1061593 は1954年出願、ワッカーは1953年に超高純度 Si の研究開始。何の年として1953とするかを決める | https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/9382122 , https://cdn.ihs.com/Connect/Content/ChemWeek/pdf/Wacker.pdf |

まとめ（第4弾、38 件）：◎ 17・○ 9・△ 10・✖ 2（[4-11] 整流器は1902年、[3-14] ノースラップの高周波炉は1918年）。

### 第5弾（2026-10-20）本線の残り 23 ノード・46 件（本線はこれで一巡）
| # | ノード | 主張 | 判定 | 結果 | 出典 |
|---|---|---|---|---|---|
| V-124 | [1-17] シーメンス法 | 年 1953・特許1953年頃 | △ | ワッカーは1953年に研究開始、総説は1954年・1955年開発・1957年工場と分かれる。特許番号・出願日は未確認（V-123 は DE1061593 を1954年出願とする）。「1950年代前半」なら安全 | https://cdn.ihs.com/Connect/Content/ChemWeek/pdf/Wacker.pdf , https://www.sciencedirect.com/topics/engineering/polysilicon-production |
| V-125 | [9-08] CZ 法 | 1916 チョクラルスキー | ◎ | IEEE マイルストーンは1916年（論文受理1916年8月、出版1918年） | https://ethw.org/Milestones:Czochralski_Process , https://en.wikipedia.org/wiki/Czochralski_method |
| V-126 | [9-08] CZ 法 | ペンをインク壺でなく溶けたスズに浸した逸話 | ○ | 伝記サイトと Wikipedia。IEEE には無い。「〜と言われる」と書く | https://en.wikipedia.org/wiki/Czochralski_method , https://janczochralski.com/en/method/ |
| V-127 | [9-08] CZ 法 | 年 1954（Si は1950年代〜） | ✖ | 正しくは Si の引き上げは1951年にベル研のティールとビューラー（発表1952年）。1954年は TI の Si トランジスタの年。年を1951〜52に | https://spectrum.ieee.org/the-lost-history-of-the-transistor , https://www.nationalacademies.org/read/12473/chapter/52 |
| V-128 | [9-12] ウェハー切断 | 1954 ベル研の太陽電池で細長い板を切り出した | ○ | フラーが長い strip を切り出した（結晶から、とまでは書いていない） | https://ethw.org/Milestones:First_Practical_Photovoltaic_Solar_Cell |
| V-129 | [9-12] ウェハー切断 | 円盤刃（1950〜60年代）→1970年代〜内周刃 | ○ | Wikipedia「Wafering」と一致 | https://en.wikipedia.org/wiki/Wafering |
| V-130 | [9-12] ウェハー切断 | 1990年代〜マルチワイヤーソー | △ | Wikipedia は2000年代初め、NASA 報告は1980年前後に試作。「広まった」の定義で変わる | https://en.wikipedia.org/wiki/Wafering , https://ntrs.nasa.gov/citations/19820015789 |
| V-131 | [9-16] 熱拡散 | 年 1954、フラーが n型 Si にホウ素を拡散 | ◎ | ETHW・APS・CHM が一致 | https://ethw.org/Milestones:First_Practical_Photovoltaic_Solar_Cell , https://aps.org/apsnews/2009/04/bell-labs-silicon-solar-cell |
| V-132 | [9-16] 熱拡散 | 1954〜55 ベル研の拡散型トランジスタ | ◎ | 1954 リー（Ge）、1955年3月タネンバウムとトーマス（Si）。リーを名前に足す | https://www.computerhistory.org/siliconengine/diffusion-process-developed-for-transistors/ , https://spectrum.ieee.org/the-lost-history-of-the-transistor |
| V-133 | [9-28] イオン注入 | 1954 ショックレーの特許 | ◎ | US 2,787,564（1954年10月出願、1957年4月発行） | https://patents.google.com/patent/US2787564A/en , https://www2.avs.org/symposium1999/Papers/Paper_VT-TuA6.html |
| V-134 | [9-28] イオン注入 | 1970年代に量産技術へ | ○ | 量産で初の注入1962年、全製品に注入を使う工場は1970年（AVS） | https://www2.avs.org/symposium1999/Papers/Paper_VT-TuA6.html |
| V-135 | [9-11] ドーパント源 | 年 1955 | △ | ホウ素拡散は1954年初め（CHM）、POCl3 は1957年フロッシュとデリック。BBr3 の初出年は不明、SHMJ はホウ素源を B2H6 とする | https://www.computerhistory.org/siliconengine/diffusion-process-developed-for-transistors/ , https://www.shmj.or.jp/english/pdf/process/exhibi433E.pdf |
| V-136 | [10-09] トランジスタ論理 | 年 1956、MIT の TX-0 | ◎ | 1955年着工、1956年稼働。「MIT リンカーン研究所」と書くとより正確 | https://www.computerhistory.org/collections/catalog/102622467 , https://mitmuseum.mit.edu/collections/subject/computers-individual-computers-tx-0-computer-288 |
| V-137 | [9-06] 酸化膜エッチング | 年 1957 | ○ | 1957年ラスロップとナル（陸軍）が SiO2 に穴、ベル研の窓開けは1955年から。エッチング液が HF だったかは未確認 | https://www.computerhistory.org/siliconengine/photolithography-techniques-are-used-to-make-silicon-devices/ |
| V-138 | [9-21] 現像・ポストベーク | 年 1957 | ○ | 現像で窓を開ける工程はベル研で1955〜57年（Kodak KPR）。ポストベークの年は未確認 | https://www.computerhistory.org/siliconengine/photolithography-techniques-are-used-to-make-silicon-devices/ , https://www.shmj.or.jp/english/pdf/em/exhibi2415E.pdf |
| V-139 | [9-21] 現像・ポストベーク | 現代は金属を含まない TMAH | ◎ | 2.38% TMAH が標準（移行の年は未確認） | https://www.microchemicals.com/PRODUKTE/Produktinformationen/Fotochemikalien/Entwickler/ , https://www.fujifilm.com/us/en/business/semiconductor-materials/photoresist-ancillaries/other-developer |
| V-140 | [9-13] ラッピング・鏡面研磨 | 1960年代 ウォルシュとハーゾグ（モンサント）のコロイダルシリカ研磨 | ○ | US 3,170,273（1963年1月出願、1965年2月登録）。根拠は特許資料だけ | https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/4462188 |
| V-141 | [9-13] ラッピング・鏡面研磨 | 年 1960 | △ | 1960年の根拠なし。コロイダルシリカ研磨に合わせるなら1963年 | — |
| V-142 | [9-19] レジスト塗布 | 年 1960 | △ | スピンコートをレジストに使い始めた年の出典なし | — |
| V-143 | [10-11] 加算器・カウンタ・レジスタ | 1964 シャープ CS-10A はトランジスタ式 | ◎ | 世界初のオールトランジスタ電卓（Ge トランジスタ約530個）、IEEE マイルストーン | https://museum.ipsj.or.jp/en/heritage/CS-10A.html , https://ethw.org/Milestones:Pioneering_Work_on_Electronic_Calculators,_1964-1973 |
| V-144 | [10-11] 加算器・カウンタ・レジスタ | 1969〜 IC 化 | ◎ | 1969年シャープ QT-8D（MOS LSI 4チップ） | https://global.sharp/corporate/info/his/only_one/i_c/subwin/i_c3.html , https://oldcalculatormuseum.com/s-sharpqt8d.html |
| V-145 | [7-14] CO2 レーザー | 年 1964 パテル（ベル研） | ◎ | 1964年で一致（⚠️ なし、確認のみ） | https://lemelson.mit.edu/resources/kumar-patel , https://physics.aps.org/story/v26/st24 |
| V-146 | [7-14] CO2 レーザー | He を足すとさらに効率が上がる（1965〜） | ◎ | 翌年に He 添加で約100W・効率6〜7% | https://apps.dtic.mil/sti/tr/pdf/AD0757263.pdf , https://www.researching.cn/ArticlePdf/m00002/1966/3/2/1966-02-0009.pdf |
| V-147 | [7-12] クロムマスク | 年 1967 | △ | ULVAC の社史は「1960年代後半はまだハードマスクが商品化されず乳剤が主流」。「1960年代末〜70年代初め」に広げる | https://www.ulvac.co.jp:443/en/news/upload/ulvac69e-8.pdf |
| V-148 | [7-12] クロムマスク | Cr 約100nm | △ | 薄くて長持ちは特許で確認、100nm の裏づけは弱い | https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/4166148 |
| V-149 | [7-12] クロムマスク | Cr のエッチング液は硝酸セリウムアンモニウム | ◎ | ＋過塩素酸が標準（メーカー資料と特許） | https://microchemicals.com/dokumente/datenblaetter/tds/tf/en/tds_tf_technietch_cr01.pdf , https://patents.google.com/patent/US6162565 |
| V-150 | [2-18] 超純水 | 年 1970、1970年代から IC 工場で必須 | △ | 「超純水」の語は1970年代後半〜80年代初め、イオン交換は1950年代から。1970 の根拠なし（18.2 MΩ·cm は一致） | https://en.wikipedia.org/wiki/Milli-Q , https://www.shmj.or.jp/english/pdf/em/exhibi2414E.pdf |
| V-151 | [7-19] 投影レンズ | 年 1978 GCA の DSW | ○ | 日本半導体歴史館は発売1978年、Wikipedia は1975年。「発売の年」と書く | https://www.shmj.or.jp/english/pdf/em/exhibi2443E.pdf , https://en.wikipedia.org/wiki/Stepper |
| V-152 | [9-39] 縮小投影露光 | 1978 GCA DSW、1/10 縮小 | ○ | 1978年（発売）と 10:1 は一致、年は Wikipedia の1975年と食い違う | https://www.shmj.or.jp/english/pdf/em/exhibi2443E.pdf , https://terpconnect.umd.edu/~browns/stepspec.html |
| V-153 | [9-39] 縮小投影露光 | g線 | △ | g線とするのは解説記事1件、仕様ページは 4800 を i線とする（改造機の可能性）。g線と確認できたのは後継の 6300 | https://www.htx.com/uk-ua/news/a-brief-history-of-the-lithography-machine-how-a-beam-of-lig-WvqrSISl/ , https://www.cnfusers.cornell.edu/sites/default/files/Equipment-Resources/Stepper%20Training%20Manual20.pdf |
| V-154 | [9-39] 縮小投影露光 | 1990年代スキャナー | ○ | Wikipedia はステップ＆スキャンの登場を1990年（⚠️ なし、確認のみ） | https://en.wikipedia.org/wiki/Stepper |
| V-155 | [7-17] 多層膜ミラー | 1980年代 バービーとアンダーウッドらが Mo/Si を実証 | ✖ | 正しくは Mo/Si の実証は1985年バービー・ムロウカ・ヘトリック（Applied Optics 24, 883、反射率 最大約78%）。1981年のアンダーウッドとバービーは多層膜の軟X線鏡で、Mo/Si ではない | https://repository.exst.jaxa.jp/dspace/handle/a-is/392259 , https://www.nature.com/articles/294429a0.pdf |
| V-156 | [7-17] 多層膜ミラー | 年 1980 | △ | 多層膜の X線/EUV 鏡は1981年、Mo/Si に限るなら1985年。どちらかに | https://www.nature.com/articles/294429a0.pdf , https://als.lbl.gov/?p=57790 |
| V-157 | [8-09] リニアモーター・エアベアリングステージ | 年 1990 | △ | 1980年代の特許にすでにある。「1990年に露光装置へ」の出典なし（ASML PAS 5500 のステップ＆スキャンは1997年） | https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/6583597 , https://www.sec.gov/Archives/edgar/data/0000937966/000115697304000133/u47074e20vf.htm |
| V-158 | [7-16] 液浸光学 | 年 2006 | ○ | ASML 最初の液浸機 XT:1250i 発表は2003年12月、2007年1月には量産中。2006年ごろ量産開始（ASML の資料のみ） | https://www.sec.gov/Archives/edgar/data/937966/000114420407005498/exhibit99-2.htm , https://www.asml.com/en/company/stories/2023/how-immersion-lithography-saved-moores-law |
| V-159 | [7-16] 液浸光学 | 2002 TSMC が ArF の水液浸を提案 | ◎ | 2002年の SPIE 講演と Sematech のワークショップ | https://ece.osu.edu/news/2013/04/ece-alum-burn-lin-receives-ieee-jun-ichi-nishizawa-medal , https://nthu-en.site.nthu.edu.tw/p/404-1003-106872.php |
| V-160 | [7-16] 液浸光学 | 193nm での水の屈折率 1.44 | ◎ | 約1.44 | https://en.wikipedia.org/wiki/Immersion_lithography , https://rit.edu/~w-lithography/research/immersion/SPIE_5040_58_immersion.pdf |
| V-161 | [7-16] 液浸光学 | NA 1.35 | ○ | XT:1900i（2006年発表、2007年出荷）。ASML の発表のみ | https://asml.com/en/news/press-releases/2006/asml-expands-immersion-product-suite , https://www.asml.com/en/news/press-releases/2007/asml-ships-first-lithography-system |
| V-162 | [7-16] 液浸光学 | 157nm F2 計画は液浸のせいで中止 | △ | 2003年に「157nm は予備の技術」、中止の年の出典なし。「主流から外れた」と書く | https://www.eetimes.com/?p=1146356 |
| V-163 | [8-13] 液滴発生器 | 年 2010 | △ | サイマーの Sn 液滴＋CO2 レーザー光源は2006年から稼働、2007年に ASML が採用。2010 の決め手なし | https://sst.semiconductor-digest.com/?p=28597 , https://eejournal.com/article/20120302_uav |
| V-164 | [8-13] 液滴発生器 | 1880 キュリー兄弟が圧電効果を発見 | ◎ | 正しい。「電圧で伸び縮み」（逆圧電効果）は1881年リップマンの予言をキュリー兄弟が確認 | https://en.wikipedia.org/wiki/Jacques_Curie , https://www.aps.org/apsnews/2014/03/this-month-in-physics-history |
| V-165 | [8-13] 液滴発生器 | 1878 レイリーが液柱の分裂を解いた | ◎ | "On the Instability of Jets"（1878）。実験はプラトー（1873）が先 | https://zenodo.org/record/2095384 , https://www.princeton.edu/~stonelab/Teaching/Oren%20Breslouer%20559%20Final%20Report.pdf |
| V-166 | [9-40] EUV 露光装置 | 約180トン | ○ | 180トンと約150トンの両方（機種で違う）。「約150〜180トン」 | https://en.wikipedia.org/wiki/ASML_Holding , https://t3n.de/news/euv-lithografie-asml-400-millionen-dollar-1748711/ |
| V-167 | [9-40] EUV 露光装置 | 1台 約200億円 | △ | 定価は非公表。約2〜2.5億ドル、High-NA は約3.5〜4億ドル。「約200〜300億円（機種・為替による）」として ⚠️ を残す | https://en.wikipedia.org/wiki/ASML_Holding , https://finance.yahoo.com/news/explainer-250-million-asml-printer-050227041.html |
| V-168 | [9-38] GAA | 2022 サムスンが 3nm（MBCFET）で量産 | ◎ | 2022年6月30日に生産開始を発表 | https://at-web1.www.anandtech.com/show/17474/samsung-starts-3nm-production-the-gaafet-era-begins , https://www.eenewseurope.com/en/samsung-starts-3nm-chip-production-ahead-of-tsmc |
| V-169 | [9-38] GAA | 2025頃〜 TSMC・インテルも | ○ | TSMC N2 は2025年第4四半期量産（二次報道）。インテル 18A は予定の報道のみ | https://pausehardware.com/tsmc-n2-20000-wafers-mois/ , https://www.trendforce.com/news/2025/02/24/news-intel-to-begin-18a-tape-outs-in-1h25-reportedly-leading-tsmcs-2nm-launch/ |

まとめ（第5弾、46 件）：◎ 16・○ 14・△ 14・✖ 2（[9-08] Si の引き上げは1951年ティールとビューラー、[7-17] Mo/Si 多層膜は1985年バービーら）。

## ドキュメントの直し（実装担当へ）
| # | 内容 | 理由 | 状態 |
|---|---|---|---|
| R-1 | docs/README.md の「tech_network.js（tech_network.json と同じ中身の JS）」の行を、「tech_network_graph.js（図に要る軽いデータ・位置つき）／tech_network_detail.js（カード・詳細用の重いデータ）」に書き換える | NET-05 で tech_network.js をやめて2つに分けた（README はネットワーク担当の担当外） | ✅ 反映（NET-06） |
- （S-010、2026-10-21）DESIGN.md の「公開（tech-network）」（「ライセンスは CC BY 4.0 を候補」）と「長期ビジョン」（「CC BY 4.0 を候補」）は、ユーザーの決定「全部 CC0」と食い違う。ROADMAP は INBOX の指示で CC0 に直した。DESIGN は実装担当のファイルなので、CC0 に直してほしい

## NET-06 で見つかった、足すべき技術ノード（追加のノードの前提に要るのに無いもの）
| # | 足すべきノード | 使うところ | 本物（出典のある範囲） | 状態 |
|---|---|---|---|---|
| N-10 | 研削・焼入れ（鋼の球や刃を硬くして、砥石で正確な形に削る） | [8-20] 玉軸受（1883 フィッシャーの鋼球研削機） | フィッシャーは焼き入れた鋼球を研削で同じ大きさ・真球にした（Schaeffler「Impeccably round and smooth」） | 済み（O-009）：[8-31] |
| N-11 | 目盛りを刻む技術（手で目盛りを刻む → 1775年頃 ラムスデンの目盛り刻み機） | [8-21] 六分儀、[10-21] 計算尺 | 既存の [8-12] 刻線機（1882）は回折格子用で年が後。ラムスデンの年は出典で確かめてから ⚠️ | 済み（O-012）：[8-42]（スミソニアン・MacTutor で 1775 を確認） |
| N-12 | 超伝導（1911 オンネス） | [8-25] ワット天秤（ジョセフソン電圧標準は超伝導の素子） | 年・人物は出典で確かめてから | 済み（O-005）：[I-20] |
| N-13 | ジョセフソン効果（電圧を量子で決める） | [8-25] ワット天秤 | 年は出典で確かめてから | 済み（O-010）：[I-34] |
| N-14 | 量子ホール効果（抵抗を量子で決める） | [8-25] ワット天秤 | 年は出典で確かめてから（発見者は存命なので人物カードは作らない） | 済み（O-010）：[I-35] |

## つながりの種類の提案
| # | 内容 | 理由 | 状態 |
|---|---|---|---|
| E-1 | 「道具として使われた」線を作る（前提ではない：無くても作れたが、設計・計算に使われた） | [10-21] 計算尺は約350年、蒸気機関・橋などの設計の計算に使われたが、「計算尺が無いと作れなかった」とは言えない（出典なし）。今は前提にも理解にもつながず、本物欄に [B-08] 電卓に置き換えられたことだけ書いた | 済み（O-019）：線の種類「つくるのに使った道具」（tool）。[10-21] 計算尺 → [4-02] 蒸気機関（ソーホー工場の技術者用計算尺、Oughtred Society） |
| E-2 | 「はずみ車」のつながり：[0-07] 紡錘車 → ろくろ（土器の回転台、[0-09] 車輪の本物欄にある）→ [4-02] 蒸気機関のはずみ車・エンジン。足すなら「理解」の線（同じしくみ＝慣性モーメントで回転をならす）か、ノード「はずみ車」を幹8 に | 紡錘車は人類最古のはずみ車（Wikipedia「Spindle whorl」：回転の速さを保つ重り）。ナハル・エイン・ゲヴⅡの研究（2024 PLOS ONE、⚠️ 議論あり）は回る道具として車輪の考えの始まりの可能性を挙げる。ろくろ・蒸気機関のはずみ車の年は出典で確かめてから | 済み（O-019）：線の種類「同じしくみで回る」（same）。[0-07] 紡錘車 → [0-19] ろくろ → [4-02] 蒸気機関（Wikipedia「Flywheel」「Spindle whorl」）。**ろくろは TECH_TREE [0-19] として追加済み（IMP3-01 O-003、前提 [0-03]・[0-07]、[0-09] 車輪の前提）** |

## NET-06 の直しで生まれた年の逆転（直さずに報告だけ）
| # | 逆転 | 理由 | 状態 |
|---|---|---|---|
| Y-1 | [9-08] CZ 法 1951 ＜ 前提 [1-17] シーメンス法 1956、[9-09] FZ 法 1952 ＜ [1-17] 1956、[9-08] 1951 ＜ [9-11] ドーパント源 1955 | 年を本物どおりに直した結果（Si の引き上げは1951年、シーメンス法の特許は1956年出願）。本物ではシーメンス法より前に別の方法の Si で引き上げていた。ゲームでは多結晶 Si を前提にしたままでよいか、実装担当が判断 | 説明ずみ（A、2026-10-20：前提は変えず、本物の別の道（亜鉛還元の多結晶 Si など）を TECH_TREE に注で書いた。docs/tasks/LOG.md の O-009） |
| Y-2 | [4-11] 整流器 1902 ＜ 前提 [5-02] 真空管 1904 | 最初の電力用整流器はクーパー・ヒューイットの水銀整流器（1902）で真空管は要らない。前提を「[5-02] or 水銀整流器」にするか検討 | 報告（NET-06） |
| Y-3 | [2-09] 石けん -2800 ＜ 前提 [2-02] カリ -2500 | カリの年を出典のある -2500 にした結果。石けん（紀元前2800年頃の物質）より前の灰汁の利用は出典が無い | ✅ 解消（O-003：灰汁の最古級の記録は石けんと同じ紀元前2800年頃なので、[2-02] の年を -2800（-2800〜-2500）⚠️ に） |
| Y-4 | [9-41] パワー半導体とインバータ 1980 ⚠️ と [4-19] スイッチング電源 1970 ⚠️（S-002） | スイッチング電源は1960年代後半〜70年代にトランジスタ（[9-04]）で広まったので、[4-19] の前提は [9-41] にせず [9-04] にした。[9-41] の年（未調査）を調べるとき、MOSFET のパワー素子（1970年代後半）・IGBT（1980年代）と分けて書くか検討 | 報告だけ |

## 網羅チェックの候補（NET-07、2026-10-20）
ノーベル物理学賞・化学賞、IEEE Milestones（ETHW）、ASME ランドマーク、ACS 化学ランドマーク、国立科学博物館の未来技術遺産、日本機械学会の機械遺産、チューリング賞を、
ネットワーク（TECH_TREE・products・ideas）と突き合わせた。**既存ノードとつながるのに、まだ入っていない技術・発見だけ**。つながりの本数の多い順。足すかどうかは実装担当・ユーザーが決める（ネットワーク担当は足していない）
- つながりの種類：前提（候補が無いと既存ノードが作れない）／子（候補が既存ノードを前提にする）／説明する（なぜ動くかを説明）／理解される
- 「受賞しなかった重要な貢献者」は出典で分かったものだけ（「一人の天才はいない」方針）。存命の人は名前の後に（存命）
- 注意：nobelprize.org・acm.org は自動では開けなかった（403）。ノーベル物理学賞はノーベル財団の公式 API で、化学賞の年・授賞理由は調べ係の知識と検索で確かめた（化学賞は出典の欄を足すときに nobelprize.org で確かめ直す）

- 調べ係の注意：原子力発電の「熱効率約33%」「シッピングポート1957年」は出典未確認（⚠️）。研削盤は N-10（研削・焼入れ）と重なる。超伝導・ジョセフソン効果・量子ホール効果は N-12〜N-14 と同じ。未来技術遺産の登録番号は Wikipedia の一覧から（公式の台帳は自動では読めなかった）

| # | 候補 | 種類 | 年 | どのリスト | つながる既存ノード（本数） | 人物 | 受賞しなかった貢献者 | 出典 |
|---|---|---|---|---|---|---|---|---|
| C-01 | フェライト（酸化物の磁性体） **済み（O-005）：[1-53]** | 技術 | 1930（1930 発明（特許出願1930・登録1932）、1937ごろ TDK が磁心を製品化） | 未来技術遺産 00053（初期のフェライト磁心、2010）・未来技術遺産 00099（パラメトロン電子計算機 HIPAC MK-1、2012）・IEEE Milestone: Development of Ferrite Materials and Their Applications, 1930-1945・ノーベル物理学賞 1970（ネール。アルヴェーンと同時） | 12 本<br>前提：[10-08] 磁気コアメモリ（コアメモリの小さな輪はフェライト（TECH_TREE でも前提に書いてあるがノードがない））<br>子：[1-13] 亜鉛（亜鉛など金属の酸化物を鉄の酸化物と焼き固めて作る（Mn-Zn・Ni-Zn フェライト））<br>子：[4-10] 変圧器（高い周波数でも渦電流が流れにくい＝高周波トランス・コイルの鉄心）<br>子：[15-12] 磁気記録（磁気ヘッドの材料・磁気テープの磁性粉の仲間）<br>子：[10-05] ブール代数とスイッチ回路（発振の位相（0 か 180度か）を 0/1 にしてブール代数の多数決論理を作る）<br>子：[4-09] 高周波発振器（高周波発振器で励振（ポンプ）してはじめて動く）<br>説明する：[10-07] 真空管フリップフロップと計算機（真空管・トランジスタ以外でも計算機が作れる＝論理は素子を選ばない、の実例）<br>子：[10-08] 磁気コアメモリ（コアメモリと同じフェライトの輪を使う）<br>子：[15-09] 真空管ラジオ（最初の用途は無線機の発振・混合・中間周波トランス（IEEE の銘板））<br>説明する：[10-08] 磁気コアメモリ（コアメモリのフェライトが磁石になる理由）<br>説明する：[15-12] 磁気記録（磁気テープの酸化鉄粉（γ-Fe2O3 など）の磁性）<br>説明する：[1-47] 天然磁石（天然磁石（磁鉄鉱 Fe3O4）もフェリ磁性） | 加藤与五郎・武井武（東京工業大学）、事業化は齋藤憲三（TDK、1935） ／ 後藤英一（東京大学の大学院生、1954）。HIPAC MK-1 は日立、MUSASINO-1 は電電公社 ／ 加藤与五郎、武井武（東京工業大学） ／ ルイ・ネール | フォン・ノイマンが同じ頃に独立に同じ原理（パラメトリック発振で 0/1）を特許出願（1954、米国特許 2,815,488） ／ フェライトそのものは1930年に加藤与五郎と武井武が発明（既存ノード [10-08] に記載） | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://www.titech.ac.jp/news/2015/029551 , https://en.wikipedia.org/wiki/Ferrite_(magnet) , https://en.wikipedia.org/wiki/Parametron |
| C-02 | 超伝導の発見（network_proposals N-12 の確認） **済み（O-005）：[I-20]（N-12 も同じ物）** | 発見 | 1911（1911年4月8日、水銀の電気抵抗が約3K で『ほぼゼロ』） | IEEE Milestone: Discovery of Superconductivity, 1911・ノーベル物理学賞 1913（カメルリング・オネス）・ノーベル物理学賞 1972（バーディーン、クーパー、シュリーファー）・ノーベル物理学賞 1978（カピッツァ）・ノーベル物理学賞 1987（ベドノルツ、ミュラー）・ノーベル物理学賞 2025（クラーク、デヴォレ、マルティニス。3人とも存命） | 10 本<br>子：[6-15] ヘリウム 回避（1908 オネスがヘリウムを液化したので 4K 以下まで冷やせた）<br>子：[1-45] 水銀（最初に超伝導になった金属は水銀（純度を上げやすかったため））<br>前提：[8-25] ワット天秤（ワット天秤の電圧標準（ジョセフソン素子）は超伝導で動く（既提案 N-12〜N-14））<br>子：[6-07] 空気分離（リンデ法などの気体液化（圧縮→膨張で冷やす）の延長）<br>子：[5-11] 真空容器（液体ヘリウムは真空断熱の容器（デュワー瓶）で保つ）<br>前提：[5-09] クライオポンプ → 液体窒素コールドトラップ（クライオポンプはヘリウム冷凍機で冷やす（要確認 ⚠️））<br>子：[9-19] レジスト塗布（ジョセフソン接合の回路はリソグラフィーで作る）<br>子：[5-14] 真空蒸着（アルミの薄膜を真空蒸着して接合を作る）<br>子：[8-25] ワット天秤（同じジョセフソン接合（N-13）の上に立つ）<br>子：[10-13] マイクロプロセッサ（計算機の次の形（量子コンピュータ）の部品。ネットワークに量子計算のノードはない） | ヘイケ・カメルリング・オネス（ライデン、1913 ノーベル物理学賞） ／ カメルリング・オネス、カピッツァ（ヘリウム液化機・膨張タービン）、バーディーン（トランジスタと2度目の受賞） ／ ジョン・クラーク、ミシェル・デヴォレ、ジョン・マルティニス（いずれも存命） | ヒレス・ホルスト（測定を担当）、コルネリス・ドルスマン、ヘリット・ヤン・フリム（液体ヘリウム装置）。IEEE の銘板は3人の名を挙げる ／ 1911年の発見はドルスマン、フリム（低温装置の技師）、ホルスト（検流計の測定。のちフィリップス研究所の所長）とのチーム（IEEE マイルストーン）。デュワーは真空断熱びん（デュワー瓶）と水素の液化（1898）でノーベル賞なし（要確認 ⚠️） ／ 理論はアンソニー・レゲット（2003年に別の業績で受賞）らの巨視的量子現象の提案（要確認 ⚠️） | https://ethw.org/Milestones:Discovery_of_Superconductivity,_1911 , https://www.nobelprize.org/prizes/physics/1913/summary/ , https://www.nobelprize.org/prizes/physics/1978/summary/ , https://www.nobelprize.org/prizes/physics/1987/summary/ |
| C-03 | 電離説と電極電位（イオンが電気を運ぶ） **済み（O-005）：[I-21]** | 発見 | 1887（アレニウス 1884 学位論文・1887 電離説、ネルンストの式 1889） | ノーベル化学賞 1903（アレニウス）・ノーベル化学賞 1909（オストワルト、一部は電離説の擁護と希釈律） | 6 本<br>説明する：[6-01] 電解槽（水に電気が流れて分解されるのは、溶けた物質がイオンに分かれて電荷を運ぶから）<br>説明する：[6-05] クロルアルカリ電解（食塩水で Cl- が陽極へ、Na+ が陰極へ動く理由）<br>説明する：[4-13] ボルタ電池（電池の電圧は電極電位の差（ネルンストの式）で決まる）<br>説明する：[4-07] 鉛蓄電池（鉛蓄電池の約2Vと、硫酸の濃さで電圧が変わる理由（ネルンストの式））<br>説明する：[2-18] 超純水（超純水の純度は比抵抗 18.2 MΩ·cm で測る。残ったイオンが電気を運ぶから）<br>説明する：[2-17] イオン交換（イオン交換樹脂が取り除くのは、水中でイオンに分かれた不純物） | アレニウス（電離説）、オストワルト（希釈律）、ネルンスト（電極電位の式 1889。ネルンストの受賞 1920 は熱定理のため） | コールラウシュ（電解質の導電率を精密測定し電離説の土台を作った。ノーベル賞なし） | https://www.nobelprize.org/prizes/chemistry/1903/summary/ , https://www.nobelprize.org/prizes/chemistry/1909/summary/ , https://www.nobelprize.org/prizes/chemistry/1920/summary/ |
| C-04 | インバー（ほとんど伸び縮みしない鉄ニッケル合金） **済み（O-005）：[1-54]（技術ノードに）** | 発見 | 1896（1896 発見（国際度量衡局 BIPM）） | ノーベル物理学賞 1920（ギヨーム） | 6 本<br>前提：[1-37] ニッケル（Fe 約64%・Ni 約36% の合金（ニッケルが要る））<br>子：[1-26] 鋼（鋼・ニッケル鋼の研究から）<br>子：[8-22] メートル原器（メートル原器の安い写し・測量用の基線尺として生まれた）<br>前提：[12-11] 測量（インバーの巻尺・基線尺で測量の精度が上がった（要確認 ⚠️：年））<br>子：[14-03] 経度とクロノメーター（温度で狂わない時計のてんぷ（ギヨームてんぷ、エリンバー））<br>子：[8-07] 除振と恒温（温度による伸び（鋼 11µm/m/℃）を小さくする別の解き方。超低膨張ガラス [7-21] と同じ考え） | シャルル・エドゥアール・ギヨーム（BIPM） | — | https://www.nobelprize.org/prizes/physics/1920/summary/ |
| C-05 | 触媒と表面化学（気体は固体の表面で反応する） **済み（O-005）：[I-22]** | 発見 | 1916（オストワルトの触媒の定義 1894、ラングミュアの吸着式 1916〜18、エルトルの表面反応解明 1970〜80年代） | ノーベル化学賞 1909（オストワルト）・ノーベル化学賞 1932（ラングミュア）・ノーベル化学賞 2007（エルトル） | 6 本<br>説明する：[6-09] ハーバー・ボッシュ法（鉄触媒の表面で N2 が原子に割れる段階が律速（エルトル））<br>説明する：[6-13] 接触法硫酸（白金・五酸化バナジウム触媒は平衡を変えず速さだけ上げる）<br>説明する：[5-16] ゲッター（ゲッターが残りの気体を表面に吸い付けて捕まえるしくみ＝吸着）<br>子：[B-01] 白熱電球（ラングミュアの表面研究はタングステン電球の寿命の研究から生まれた（1913 ガス入り電球も））<br>説明する：[9-30] エピタキシーと CVD（CVD の膜は、気体分子がウェハー表面に吸着して反応してできる）<br>子：[5-07] ターボ分子ポンプ（エルトル流の清浄表面の研究には超高真空（ターボ分子ポンプ）が要る） | オストワルト（触媒＝速さを変えるが平衡は変えない）、ラングミュア（GE 研究所、単分子層吸着）、エルトル（超高真空で鉄触媒上のアンモニア合成の各段階を解明、存命） | ベルセリウス（1835 に『触媒』という言葉と概念を作った。ノーベル賞創設前） | https://www.nobelprize.org/prizes/chemistry/1909/summary/ , https://www.nobelprize.org/prizes/chemistry/1932/summary/ , https://www.nobelprize.org/prizes/chemistry/2007/summary/ |
| C-06 | 高分子説（ポリマーは長い鎖の巨大分子） **済み（O-005）：[I-23]** | 発見 | 1920（シュタウディンガー 1920 論文〜1920年代の論争、カロザースの縮合重合で実証 1929〜） | ノーベル化学賞 1953（シュタウディンガー）・ノーベル化学賞 1974（フローリー）・ACS 化学遺産 1999（シュタウディンガーの高分子研究）・ACS 化学遺産 2000（デュポンのカロザースの高分子研究） | 6 本<br>説明する：[2-28] フェノール樹脂（ベークライトが硬いのは鎖が網目に橋かけされた巨大分子だから）<br>説明する：[2-37] ポリエチレン（ポリエチレン＝エチレンが何万個もつながった鎖）<br>説明する：[2-29] ゴム（ゴムの伸び縮みと加硫（硫黄で鎖を橋かけ））<br>説明する：[2-30] ネガ型フォトレジスト（ネガ型レジストは光で鎖が橋かけされて溶けなくなる）<br>説明する：[2-33] 化学増幅レジスト（化学増幅レジストは鎖の側基が酸で外れて溶けるようになる）<br>説明する：[2-41] エポキシ樹脂（エポキシの硬化＝橋かけで網目の巨大分子になる） | シュタウディンガー（巨大分子説）、フローリー（高分子の物理化学） | カロザース（デュポン。分子量を設計どおりに作り高分子説を実証、1937 死去で受賞なし。ACS 化学遺産 2000） | https://www.nobelprize.org/prizes/chemistry/1953/summary/ , https://www.nobelprize.org/prizes/chemistry/1974/summary/ , https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks |
| C-07 | 原子時計 **済み（O-006）：[8-30]** | 技術 | 1948（1948年8月12日 NBS のアンモニア時計が初運転（公開は1949年1月）。1955 エッセンとパリーのセシウム時計（英 NPL）。1967 秒の定義がセシウムに） | IEEE Milestone: First Atomic Clock, 1948・ノーベル物理学賞 1944（ラビ）・ノーベル物理学賞 1989（ラムゼー、デーメルト、パウル）・ノーベル物理学賞 1997（朱棣文、コーエン＝タヌージ、フィリップス：レーザー冷却） | 6 本<br>子：[8-15] 水晶振動子（水晶発振器の振動数をアンモニア分子の吸収線（23.8GHz）に合わせ続ける仕組み）<br>前提：[B-17] GPS（GPS 衛星は原子時計の時刻を送る（ノードの前提にも『衛星には原子時計』とある））<br>前提：[8-24] 国際単位系（1967年から『秒』はセシウム原子の振動で決まる）<br>前提：[I-09] 単位を自然の定数で決める（単位を自然の定数で決める考えの最初の実例（秒））<br>子：[8-24] 国際単位系（1967 年に SI の秒をセシウムで定義し直した）<br>子：[14-03] 経度とクロノメーター（時計で経度・位置を知る考えの延長） | ハロルド・ライオンズ（米国標準局 NBS）、ルイス・エッセン、ジャック・パリー（セシウム、1955） ／ イジドール・ラビ、ノーマン・ラムゼー | イジドール・ラビ（分子線の共鳴法、1945 に原子で時計を作ることを提案。1944 ノーベル物理学賞） ／ ルイ・エッセンとジャック・パリー（英国 NPL）が1955年に最初の実用的なセシウム時計を作った（水晶時計を校正する方式）。最初の原子時計はハロルド・ライオンズ（米 NBS、アンモニア、1948〜49） | https://ethw.org/Milestones:First_Atomic_Clock,_1948 , https://tf.nist.gov/general/pdf/2535.pdf , https://en.wikipedia.org/wiki/Atomic_clock , https://www.nobelprize.org/prizes/physics/1989/summary/ |
| C-08 | 酸素の発見と燃焼の理論（化学革命） **済み（O-006）：[I-24]** | 発見 | 1774（シェーレ 1771〜72 に得たが発表 1777、プリーストリー 1774、ラボアジエ『化学原論』1789） | ACS 化学遺産 1994（プリーストリーの家）・ACS 化学遺産 1999（ラボアジエの化学革命）・ACS 化学遺産 2000（ボーウッド・ハウス＝酸素発見の地） | 5 本<br>説明する：[3-01] たき火（火が燃えるのは燃料が空気中の酸素と結びつくから（フロギストン説の否定））<br>説明する：[1-03] 銅の製錬（製錬＝鉱石（酸化物）から炭素が酸素を奪う『還元』）<br>前提：[13-15] 医療用酸素（酸素が何か分からなければ医療用酸素はない）<br>前提：[I-03] 周期表（ラボアジエの『元素』の定義と元素表が周期表の出発点）<br>子：[1-45] 水銀（プリーストリーは酸化水銀（水銀）を加熱して酸素を得た） | プリーストリー（酸化水銀を加熱して酸素、1774）、ラボアジエ（燃焼＝酸素との結合、質量保存、元素の表） | シェーレ（プリーストリーより先に酸素を得たが発表が遅れた） | https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks , https://www.acs.org/education/whatischemistry/landmarks/landmarksdirectory.html |
| C-09 | 誘導コイル（電池から高い電圧の火花を作る） **済み（O-006）：[4-27]（[15-06] 電波の発見・[B-03] X線管の前提に）** | 技術 | 1836（1834 着手、1836 カランが最初の誘導コイル（一次・二次が別の巻線＋自動の断続器）。1851 ルームコルフが商品化） | IEEE Milestone: Callan's Pioneering Contributions to Electrical Science and Technology, 1836 | 5 本<br>子：[I-01] 電磁誘導（電磁誘導（1831）を使い、電流を急に切ると二次側に高い電圧が出る）<br>子：[10-01] 電磁石（鉄心に巻いた電磁石が土台。断続器も電磁石で動く）<br>前提：[15-06] 電波の発見（ヘルツの電波の実験の火花は誘導コイルで作った（ノードの前提に『[10-01]（誘導コイル）』と書かれているがノードが無い）<br>前提：[B-03] X線管（レントゲンが X線を見つけた放電管は誘導コイルの高電圧で動かした）<br>前提：[15-07] 無線電信（初期の無線電信の火花送信機） | ニコラス・カラン（メイヌース） | チャールズ・グラフトン・ペイジ（米国、1836 ごろ独立に）、ハインリッヒ・ルームコルフ（1851 改良・商品化。実験室に広めた） | https://ethw.org/Milestones:Callan%27s_Pioneering_Contributions_to_Electrical_Science_and_Technology,_1836 , https://en.wikipedia.org/wiki/Induction_coil |
| C-10 | 電子の発見と電気素量（陰極線の正体） **済み（O-006）：[I-25]** | 発見 | 1897（トムソンの比電荷測定 1897、ミリカンの油滴実験 1909〜1913） | ノーベル物理学賞 1905（レーナルト：陰極線）・ノーベル物理学賞 1906（J. J. トムソン：気体の電気伝導）・ノーベル物理学賞 1923（ミリカン：電気素量） | 5 本<br>説明する：[5-02] 真空管（真空管の中を流れているのが電子だと分かった）<br>説明する：[B-03] X線管（X線は高速の電子が W に当たって止まるときに出る）<br>前提：[8-11] 電子顕微鏡（電子顕微鏡は電子ビームを電磁レンズで曲げる）<br>子：[5-10] 真空計（陰極線の実験には真空計・高い真空が要った）<br>説明する：[I-09] 単位を自然の定数で決める（2019 年の SI では電気素量 e の値を固定してアンペアを定義） | J. J. トムソン（1897）、レーナルト、ミリカン（1909〜1913） | 陰極線の研究ではヒットルフ、クルックスが先行。同じ1897年にヴィーヘルトも比電荷を測った（要確認 ⚠️）。ミリカンの油滴実験は学生ハーヴィー・フレッチャーの寄与が大きいとされる（要確認 ⚠️） | https://www.nobelprize.org/prizes/physics/1906/summary/ , https://www.nobelprize.org/prizes/physics/1923/summary/ , https://www.nobelprize.org/prizes/physics/1905/summary/ |
| C-11 | 熱電子放出（リチャードソンの法則） **済み（O-006）：[I-26]** | 発見 | 1901（エジソン効果 1880〜1883、リチャードソンの法則 1901〜1903、賞は 1928） | ノーベル物理学賞 1928（O. W. リチャードソン） | 5 本<br>説明する：[5-02] 真空管（熱した陰極から電子が出るので真空管が動く）<br>子：[B-01] 白熱電球（白熱電球の中で見つかったエジソン効果が出発点）<br>前提：[B-03] X線管（クーリッジの熱陰極 X線管（1913）は熱電子放出を使う）<br>前提：[8-11] 電子顕微鏡（電子顕微鏡の W 電子銃）<br>説明する：[1-28] タングステン線（なぜ陰極に W（高温に耐える）を使うか） | オーエン・リチャードソン | エジソンが1880〜1883年に電球の中で一方向の電流（エジソン効果、1883年特許）を見つけた。ラングミュア（GE、のち1932年ノーベル化学賞＝表面化学）は高真空で熱電子放出が安定することを示し、クーリッジの熱陰極 X 線管（1913）につながった | https://www.nobelprize.org/prizes/physics/1928/summary/ , https://technav.ieee.org/topic/thermionic-emission/ , https://www.auntminnieeurope.com/clinical-news/article/15645646/reflections-on-the-incredible-life-of-william-david-coolidge |
| C-12 | 光電効果と光子（光はつぶでもある） **済み（O-006）：[I-27]** | 発見 | 1905（ヘルツの発見 1887、ハルヴァックス 1888、レーナルト 1902、アインシュタインの説明 1905、ミリカンの検証 1914〜1916） | ノーベル物理学賞 1921（アインシュタイン）・ノーベル物理学賞 1923（ミリカン） | 5 本<br>説明する：[B-12] 太陽電池（光のつぶ1個のエネルギーが足りれば電子が動く＝太陽電池）<br>子：[15-06] 電波の発見（ヘルツは電波の実験中に光電効果を見つけた）<br>前提：[7-13] レーザー原理（光のつぶの考えが1917年の誘導放出（レーザーの原理）につながる）<br>説明する：[2-33] 化学増幅レジスト（短い波長ほど光子のエネルギーが大きく、レジストの化学反応を起こせる）<br>前提：[15-11] テレビ（テレビのカメラ（撮像管）は光電面で光を電子に変える） | アインシュタイン（1905）、ミリカン（実験で確かめた） | ハインリヒ・ヘルツが1887年に紫外線で火花が飛びやすくなることを発見（1894年に死去）。ハルヴァックス（1888、負に帯電した亜鉛板が紫外線で電荷を失う）。レーナルトは1902年に電子のエネルギーが光の強さでなく波長で決まることを示した | https://www.nobelprize.org/prizes/physics/1921/summary/ , https://www.nobelprize.org/prizes/physics/1923/summary/ , https://galileo.phys.virginia.edu/classes/252/photoelectric_effect.html |
| C-13 | 溶解アセチレンと灯台の自動点滅器（AGA） **済み（O-008）：[B-23]** | 技術 | 1906（アセトンへの溶解 1896（クロードとエス）、ダレンのアガマッサン 1906、太陽弁 1907） | ノーベル物理学賞 1912（ダレン） | 5 本<br>子：[B-14] アセチレンランプ（カーバイドから作るアセチレンをボンベで安全に運ぶ）<br>子：[2-40] アセトン（ボンベの中はアセトンにアセチレンを溶かす）<br>子：[3-13] 断熱材（詰め物（アガマッサン）は珪藻土・石綿など）<br>子：[10-04] フィードバック制御（太陽弁＝光で自動にガスを止める制御）<br>子：[7-02] 溶融石英（溶解アセチレンのボンベで酸素アセチレン炎（溶接・切断）が広まった（要確認 ⚠️）） | グスタフ・ダレン（AGA 社。1912年にアセチレンの爆発で失明） | ジョルジュ・クロードとアルベール・エスが1896年にアセチレンがアセトンに大量に溶けて安全になることを発見（ノーベル財団の授賞演説、Wikipedia「Agamassan」） | https://www.nobelprize.org/prizes/physics/1912/ceremony-speech/ , https://en.wikipedia.org/wiki/Agamassan , https://www.lindahall.org/about/news/scientist-of-the-day/gustaf-dalen/ |
| C-14 | X線回折と結晶構造解析 **済み（O-008）：[I-28]** | 発見 | 1912（ラウエの回折 1912、ブラッグ父子の構造解析 1913） | ノーベル物理学賞 1914（ラウエ）・ノーベル物理学賞 1915（W. H. ブラッグ、W. L. ブラッグ） | 5 本<br>子：[B-03] X線管（X線管がないと実験できない）<br>前提：[9-08] CZ 法（単結晶インゴットの結晶方位を X 線で決めてから切る（要確認 ⚠️：工業利用の開始年））<br>前提：[8-18] 人工水晶（水晶振動子は決まった角度（AT カットなど）で切る。方位は X 線で測る（要確認 ⚠️））<br>説明する：[7-07] 回折格子（結晶は原子の並びでできた『回折格子』として働く）<br>説明する：[I-06] 偏析（結晶の中で原子がどう並ぶかを見る手段） | マックス・フォン・ラウエ、ウィリアム・ヘンリー・ブラッグ、ウィリアム・ローレンス・ブラッグ | ラウエの実験を実際に行ったのはフリードリッヒとクニッピング（1912、要確認 ⚠️） | https://www.nobelprize.org/prizes/physics/1914/summary/ , https://www.nobelprize.org/prizes/physics/1915/summary/ |
| C-15 | 固体のバンド理論（量子力学・パウリの排他原理から、なぜ半導体があるか） **済み（O-008）：[I-29]** | 発見 | 1931（量子力学 1925〜1926、ブロッホの結晶中の電子 1928、ウィルソンの半導体理論 1931。ノーベル賞はバンド理論そのものではなく、土台の量子力学（1932・1933）と排他原理（1945）） | ノーベル物理学賞 1932（ハイゼンベルク）・ノーベル物理学賞 1933（シュレーディンガー、ディラック）・ノーベル物理学賞 1945（パウリ） | 5 本<br>説明する：[9-03] 点接触トランジスタ（なぜ n型 Ge に点接触で増幅が起きるか（電子と正孔））<br>説明する：[9-11] ドーパント源（ドーパント（P・B）がなぜ電子・正孔を作るか）<br>説明する：[B-12] 太陽電池（バンドギャップより大きい光のエネルギーで p-n 接合が発電する）<br>説明する：[9-05] Geダイオード（点接触ダイオードの整流が起きる理由）<br>子：[I-03] 周期表（周期表の殻構造（排他原理）を固体に広げた考え） | ハイゼンベルク、シュレーディンガー、ディラック、パウリ（受賞）。バンド理論はブロッホ（1928）、パイエルス、A. H. ウィルソン（1931『電子半導体の理論』） | アラン・ヘリーズ・ウィルソンは1931年に導体・半導体・絶縁体の違いをバンドで説明したがノーベル賞なし（Computer History Museum）。ブロッホのノーベル賞（1952）は NMR で、バンド理論ではない | https://www.nobelprize.org/prizes/physics/1932/summary/ , https://www.nobelprize.org/prizes/physics/1945/summary/ , https://www.computerhistory.org/siliconengine/the-theory-of-electronic-semi-conductors-is-published/ , https://en.wikipedia.org/wiki/Alan_Herries_Wilson |
| C-16 | 加速器（コッククロフト・ウォルトン回路とサイクロトロン） **済み（O-008）：[9-47]（[9-28] イオン注入の前提に）** | 技術 | 1932（サイクロトロン 1930〜31、コッククロフトとウォルトンの Li 分裂 1932） | ノーベル物理学賞 1939（ローレンス）・ノーベル物理学賞 1951（コッククロフト、ウォルトン） | 5 本<br>子：[4-10] 変圧器（変圧器の交流を何段も重ねて数十万V に）<br>子：[4-11] 整流器（整流器（ダイオード）とコンデンサの梯子で高電圧を作る）<br>前提：[9-28] イオン注入（イオン注入装置はイオンを数十〜数百kV で加速する加速器そのもの）<br>子：[5-05] 水銀拡散ポンプ（粒子を飛ばす管は高真空が要る）<br>子：[10-01] 電磁石（サイクロトロンは大きな電磁石で粒子を回す） | アーネスト・ローレンス、ジョン・コッククロフト、アーネスト・ウォルトン | ハインリヒ・グライナッハーが1920年に倍電圧回路（同じ回路）を考えた（ベルン大学）。ロルフ・ヴィデレーの線形加速器（1928）の論文を読んでローレンスがサイクロトロンを思いついた（DESY のヴィデレー伝、CERN 講義） | https://www.nobelprize.org/prizes/physics/1939/summary/ , https://www.nobelprize.org/prizes/physics/1951/summary/ , https://www.phinst.unibe.ch/e54890/e291084/e291090/greinacher_leben_ger.pdf , https://www-library.desy.de/elbooks/wideroe/WiE-CHRO.htm |
| C-17 | プログラム内蔵方式（プログラムを電子の記憶に入れて動く計算機） **済み（O-008）：[10-31]（[10-18] コンパイラの前提に）** | 技術 | 1948（1948年6月21日 マンチェスター「ベビー」が最初のプログラムを実行。1949 EDSAC（ケンブリッジ）が定常運用。「世界初のプログラム内蔵式」は定義しだいで諸説（IEEE も言い方を避けた）） | IEEE Milestone: Manchester University "Baby" Computer and its Derivatives, 1948-1951・ACMチューリング賞 1967（モーリス・ウィルクス：EDSAC） | 5 本<br>子：[10-07] 真空管フリップフロップと計算機（真空管の計算機（ENIAC など）は配線でプログラムを変えていた。それを記憶の中の数に置き換えた）<br>子：[15-11] テレビ（ベビーの記憶はブラウン管（ウィリアムズ＝キルバーン管）の画面の電荷。ベビーはこの記憶を試すために作られた）<br>子：[I-08] プログラム（『プログラム』という考えを、取り替えられるデータとして機械の中に入れた）<br>前提：[10-18] コンパイラ（コンパイラは『プログラムを作るプログラム』。プログラムが記憶の中のデータでないと成り立たない（A-0 は UNIVAC ）<br>前提：[10-13] マイクロプロセッサ（マイクロプロセッサは、記憶から命令を読んで実行するプログラム内蔵式の CPU を1チップにしたもの） | フレデリック・ウィリアムズ、トム・キルバーン、ジェフ・トゥーティル（ベビー、1948）、モーリス・ウィルクス（EDSAC 1949） | フォン・ノイマンの『EDVAC 報告書の第一草稿』（1945）、エッカートとモークリー（EDVAC の構想）、チューリングの ACE 設計（1945）。1948年には ENIAC も改造されてプログラム内蔵式で動いた（ETHW の専門家レビュー） | https://ethw.org/Milestones:Manchester_University_%22Baby%22_Computer_and_its_Derivatives,_1948-1951 , https://en.wikipedia.org/wiki/Turing_Award |
| C-18 | 配位重合触媒（チーグラー・ナッタ触媒、フィリップス触媒） **済み（O-008）：[2-50]** | 技術 | 1953（フィリップスのホーガンとバンクス 1951（Cr 触媒、高密度PE・ポリプロピレン）、チーグラー 1953（Ti-Al 触媒）、ナッタ 1954（立体規則性PP）） | ノーベル化学賞 1963（チーグラー、ナッタ）・ACS 化学遺産 1999（ポリプロピレンと高密度ポリエチレン＝フィリップス） | 5 本<br>子：[2-37] ポリエチレン（高圧法ポリエチレン（1933）の次：常圧近くで硬い高密度PEが作れる）<br>子：[2-36] エチレン（原料はエチレン・プロピレン）<br>子：[1-41] チタン（チーグラー触媒は四塩化チタン TiCl4）<br>子：[1-40] クロム（フィリップス触媒は酸化クロム）<br>前提：[2-29] ゴム（合成ポリイソプレン（天然ゴムと同じ形）はこの触媒で1950年代に可能になった（2-29 本文に記載）） | チーグラー、ナッタ | ホーガンとバンクス（フィリップス石油。1951 に酸化クロム触媒で結晶性PPと低圧の高密度PE。ノーベル賞なし、ACS 化学遺産） | https://www.nobelprize.org/prizes/chemistry/1963/summary/ , https://www.acs.org/education/whatischemistry/landmarks/polypropylene.html |
| C-19 | 希ガス化合物（キセノンも反応する） **済み（O-009）：[I-30]** | 発見 | 1962（バートレット 1962 Xe+PtF6、同年 XeF2・XeF4。XeF2 によるシリコンのエッチング 1979（ウィンターズとコバーン）） | ACS 化学遺産 2006（バートレットの希ガス反応） | 5 本<br>子：[6-08] 希ガス精製（精製したキセノンが要る）<br>子：[6-14] フッ素ガス（相手は六フッ化白金＝フッ素ガスで作る）<br>子：[1-20] 白金（PtF6 の白金）<br>説明する：[7-20] エキシマレーザー（希ガスもハロゲンと結合しうる例。ただしエキシマレーザーの KrF・ArF は励起状態だけの結合で、バートレットの安定化合）<br>子：[9-27] ドライエッチング（XeF2 ガスはプラズマなしでシリコンを削るドライエッチング（MEMS の犠牲層除去）。9-27 の技術を前提にした派生） | ニール・バートレット（ブリティッシュ・コロンビア大学） | — | https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks , https://en.wikipedia.org/wiki/Xenon_hexafluoroplatinate , https://en.wikipedia.org/wiki/Vapor_etching |
| C-20 | 半導体ヘテロ構造（ダブルヘテロ接合レーザー・HEMT） **済み（O-009）：[9-48]** | 技術 | 1963（クレーマーとアルフェロフの提案 1963、室温連続発振のレーザー 1970） | ノーベル物理学賞 2000（アルフェロフ、クレーマー。キルビーは集積回路で同時受賞） | 5 本<br>子：[9-30] エピタキシーと CVD（薄い層を重ねるエピタキシー（液相・MBE）がないと作れない）<br>前提：[15-13] 光ファイバー（光ファイバー通信の光源＝室温で連続発振する半導体レーザー）<br>前提：[B-07] LED（明るい LED（青色 GaN も）はダブルヘテロ構造で光を閉じこめる）<br>前提：[B-10] スマホ（スマホの高周波回路（HEMT・HBT））<br>子：[1-44] ガリウム（GaAs/AlGaAs はガリウムが前提） | ジョレス・アルフェロフ、ハーバート・クレーマー | 林厳雄とモートン・パニッシュ（ベル研）が1970年に室温連続発振の半導体レーザーを独立に達成（アルフェロフ組が約1か月早く発表。3人は2001年京都賞）。三村高志（富士通）が1979年にヘテロ接合の HEMT を発明（IEEE マイルストーン、存命） | https://www.nobelprize.org/prizes/physics/2000/summary/ , https://shmj.or.jp/shimura/shimura_E/ssis_shimura3_11E.html , https://www.kyotoprize.org/en/laureates/morton_b_panish/ , https://ethw.org/Milestones:High_Electron_Mobility_Transistor,_HEMT,_1979 |
| C-21 | 液晶ディスプレイ（LCD） **済み（O-009）：既存の [B-18] 液晶・[B-19] TFT（RCA 1968・EL-805 1973 を [B-18] の本物欄へ）** | 技術 | 1968（1888 ライニッツァー・1889 レーマンが液晶を発見、1964〜68 RCA の動的散乱型、1971 ねじれネマティック（TN）、1973 シャープ EL-805 電卓、1979 アモルファス Si の TFT） | 未来技術遺産 00107（液晶表示電卓 EL-805、2012）・未来技術遺産 00267（液晶デジタルクオーツ腕時計、2019）・IEEE Milestone: Liquid Crystal Display, 1968・IEEE Milestone: Liquid Crystals, 1889・IEEE Milestone: Amorphous Silicon Thin Film Transistor Switches for LCDs, 1979・IEEE Milestone: Sharp 14-inch TFT-LCD for TV, 1988 | 5 本<br>子：[B-06] 偏光フィルム（TN 液晶は2枚の偏光板で光を止めたり通したりする＝偏光フィルムが無いと作れない）<br>前提：[B-08] 電卓（EL-805（1973）で電卓が電池で長く動く薄型に）<br>子：[9-24] CMOS（EL-805 は CMOS と液晶の組み合わせで消費電力を下げた）<br>前提：[B-10] スマホ（スマホの画面（今は有機ELもあるが、普及は液晶から））<br>子：[9-23] MOSFET（大きな画面は画素1つずつに薄膜トランジスタ（MOS 型の TFT）を付けて動かす） | ライニッツァー（1888 発見）、ハイルマイヤー（RCA、1968）、シャット＆ヘルフリッヒ（1971 TN）、ファーガソン（1971 TN、独立）、シャープ（EL-805） ／ ジョージ・ハイルマイヤー、ルイス・ザノーニ、ルシアン・バートン（RCA）、オットー・レーマン（1889）、シャット＝ヘルフリッヒ（TN、1971）、スピアとル・コンバー（a-Si TFT、ダンディー大） | TN 方式はシャット・ヘルフリッヒとファーガソンがほぼ同時に独立発明（特許争い） ／ フリードリヒ・ライニッツァー（1888、植物学者として最初に液晶状態を観察）、ジェームズ・ファーガソン（TN を米国で独立に開発） | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Liquid-crystal_display , https://en.wikipedia.org/wiki/Twisted_nematic_field_effect , https://ethw.org/Milestones:Liquid_Crystal_Display,_1968 |
| C-22 | CCD（電荷結合素子）と半導体の撮像素子 **済み（O-009）：既存の [9-42] 画像センサー（1971 の撮像への利用を本物欄へ）** | 技術 | 1969（1969年10月の発明（ベル研）、撮像は1971） | ノーベル物理学賞 2009（ボイル、スミス。カオは光ファイバーで同時受賞） | 5 本<br>子：[9-23] MOSFET（MOS（酸化膜の上の電極）のコンデンサを並べた構造）<br>子：[9-18] プレーナ技術（プレーナ技術で作る）<br>子：[10-12] DRAM（最初はメモリ（電荷をバケツリレーで運ぶ）として考えられた）<br>前提：[B-10] スマホ（スマホのカメラ（今は CMOS センサーだが同じく半導体の撮像素子））<br>子：[B-02] 銀塩写真（フィルム写真を置きかえた） | ウィラード・ボイル、ジョージ・E・スミス | マイケル・トンプセット（ベル研）が1971年に CCD を撮像に使う方法を示し特許を取った。ボイルとスミスの CCD は当初メモリ用だった（IEEE Spectrum、ETHW） | https://www.nobelprize.org/prizes/physics/2009/summary/ , https://spectrum.ieee.org/nobel-controversy-former-bell-labs-employee-says-he-invented-the-ccd-imager , https://ethw.org/Michael_Tompsett |
| C-23 | 研削盤（砥石で削る精密仕上げ） **済み（O-009）：[8-31]（N-10 も同じ物。[8-20] 玉軸受の前提に）** | 技術 | 1876（1876 ブラウン＆シャープの万能研削盤、1930 国産初の歯車研削盤、1953 平面研削盤 PSG-6B、1956 GPB 円筒研削盤） | 機械遺産 No.059（大隈式非真円平軸受と GPB 形円筒研削盤）・機械遺産 No.114（平面研削盤 PSG-6B）・機械遺産 No.106（平歯車研削盤 ASG-2形） | 4 本<br>前提：[8-20] 玉軸受（玉軸受の転動面と鋼球は焼入れ後に研削（機械遺産 No.059 の解説））<br>前提：[8-08] ボールねじ精密ステージ（ボールねじのねじ溝の仕上げ）<br>子：[3-10] アチソン炉（砥石の粒はアチソン炉の炭化ケイ素（カーボランダム））<br>子：[8-01] 旋盤（旋盤では削れない焼入れ鋼を 1/1000mm で仕上げる） | ジョセフ・ブラウン（ブラウン＆シャープ）、長岡振吉（大隈鐵工所）、岡本覚三郎（岡本工作機械） | — | https://www.jsme.or.jp/kikaiisan/heritage_059_jp.html , https://www.jsme.or.jp/kikaiisan/heritage_114_jp.html , https://www.jsme.or.jp/kikaiisan/heritage_106_jp.html , https://en.wikipedia.org/wiki/Grinding_machine |
| C-24 | 乾電池（マンガン乾電池） **済み（O-009）：[4-28]** | 技術 | 1896（ルクランシェ電池（湿式）1866、ガスナーの乾電池 1886、屋井先蔵 1887頃、コロンビア乾電池 1896） | ACS 化学遺産 2005（コロンビア乾電池） | 4 本<br>子：[4-13] ボルタ電池（ボルタ電池の次：液をペーストにしてこぼれない電池）<br>子：[1-13] 亜鉛（負極は亜鉛の缶）<br>子：[1-07] 黒鉛（正極の集電体は炭素棒）<br>子：[2-21] アンモニア水（電解液は塩化アンモニウム） | ナショナル・カーボン社（今のエナジャイザー） | ルクランシェ（1866 亜鉛・MnO2・塩化アンモニウムの電池）、ガスナー（1886 ペースト状の乾電池）、屋井先蔵（1887頃 日本で独自に乾電池。年は資料で揺れる ⚠️） | https://www.acs.org/education/whatischemistry/landmarks/drycellbattery.html |
| C-25 | 歯切り盤（ホブ盤）と精密歯車 **済み：[8-32]（O-010）** | 技術 | 1897（1897 プファウターのホブ盤、1943 最初のマスターウォームホイール、1953 HRS-500） | 機械遺産 No.034（親歯車ホブ盤 HRS-500 のマスターウォームホイール）・機械遺産 No.106（平歯車研削盤 ASG-2形） | 4 本<br>前提：[4-06] 蒸気タービン（船のタービンの大きな減速歯車を切る（機械遺産 No.034 の解説））<br>前提：[14-11] 自動車（自動車の変速機の歯車）<br>子：[8-01] 旋盤（ねじ（ウォーム）と歯車を回しながら削る＝旋盤とねじ切りの発展）<br>説明する：[I-11] 基準なしに正確さを生む（親歯車で歯車を切り、より正確な親歯車を作り直して精度を上げた（7代目で最大累積ピッチ誤差4µm）） | ヘルマン・プファウター（独）、芝浦機械製作所（HRS-500） | — | https://www.jsme.or.jp/kikaiisan/heritage_034_jp.html , https://en.wikipedia.org/wiki/Hobbing |
| C-26 | 熱放射の法則とエネルギー量子（ウィーンの法則・プランクの放射則） **済み：[I-31]（O-010）** | 発見 | 1900（ウィーンの変位則 1893・放射則 1896、プランク 1900） | ノーベル物理学賞 1911（ウィーン）・ノーベル物理学賞 1918（プランク） | 4 本<br>説明する：[3-11] 光高温計（光高温計は熱い物の色（明るさ）から温度を読む＝プランクの放射則）<br>説明する：[B-01] 白熱電球（白熱電球の光の大部分が赤外線になる理由）<br>前提：[I-09] 単位を自然の定数で決める（プランク定数 h が 2019 年のキログラムの定義の柱）<br>子：[I-02] 熱力学（熱力学の上に立つ） | ヴィルヘルム・ウィーン、マックス・プランク | ベルリンの帝国物理工学研究所（PTR）のルンマーとプリングスハイム、ルーベンスとクルルバウムの精密測定が理論を決めた（要確認 ⚠️）。キルヒホッフ（1859 黒体の考え） | https://www.nobelprize.org/prizes/physics/1911/summary/ , https://www.nobelprize.org/prizes/physics/1918/summary/ |
| C-27 | フランク・ヘルツの実験（電子が原子にぶつかってエネルギーを渡す） **済み：[I-32]（O-010）** | 発見 | 1914（1914（水銀蒸気、4.9V）） | ノーベル物理学賞 1925（フランク、G. ヘルツ） | 4 本<br>説明する：[7-10] 水銀ランプ（水銀ランプは電子が水銀原子にぶつかって決まった線（g線・i線）を出す）<br>子：[1-45] 水銀（実験は水銀蒸気で行った）<br>説明する：[I-07] 電気アークのしくみ（放電・アークの中で電子が原子を励起・電離するしくみ）<br>子：[5-02] 真空管（熱陰極と格子のある真空管型の装置） | ジェームス・フランク、グスタフ・ヘルツ | — | https://www.nobelprize.org/prizes/physics/1925/summary/ |
| C-28 | X線で結晶の並びを読む（X線結晶解析・粉末法） **済み：[I-28] と同じ物（粉末法 1916 を本物欄へ）（O-010）** | 技術 | 1916（ラウエ 1912、ブラッグ父子 1913（物理学賞 1914・1915）、デバイ＝シェラー粉末法 1916、構造決定法 1950年代） | ノーベル化学賞 1936（デバイ）・ノーベル化学賞 1964（ホジキン）・ノーベル化学賞 1985（ハウプトマン、カール） | 4 本<br>子：[B-03] X線管（X 線管が要る）<br>前提：[9-08] CZ 法（種結晶とインゴットの結晶方位（<100> など）は X 線で決めて切る）<br>前提：[8-18] 人工水晶（水晶振動子の AT カット（温度で周波数が変わらない角度）は X 線で方位を測って切る）<br>説明する：[7-17] 多層膜ミラー（多層膜ミラーの反射は結晶の X 線回折と同じブラッグの条件） | デバイ、シェラー、ホジキン、ハウプトマン、カール | ハル（GE、1917 に粉末法を独立に発明。受賞なし） | https://www.nobelprize.org/prizes/chemistry/1936/summary/ , https://www.nobelprize.org/prizes/chemistry/1985/summary/ |
| C-29 | 質量分析（原子・分子を重さでふるい分ける） **済み：[8-33]（[9-28] イオン注入の前提に）（O-010）** | 技術 | 1919（J.J.トムソン 1913 ネオンの同位体、デンプスター 1918、アストン 1919 質量分析器、田中耕一 1987 ソフトレーザー脱離） | ノーベル化学賞 1922（アストン）・ノーベル化学賞 2002（フェン、田中耕一）・ACS 化学遺産 2019（ガスクロマトグラフ質量分析） | 4 本<br>前提：[9-28] イオン注入（イオン注入装置は磁石でイオンを質量ごとに曲げ、欲しい B+ や P+ だけを選ぶ（質量分析器そのもの））<br>説明する：[I-03] 周期表（塩素の原子量が 35.5 と半端なのは同位体 35 と 37 が混ざるから）<br>前提：[5-12] 真空シール（現代の真空シールはヘリウムリーク検出器（He だけを数える質量分析器）で漏れを確かめる）<br>子：[6-15] ヘリウム 回避（リーク検出にはヘリウムを使う） | アストン（同位体と整数則）、田中耕一（存命、島津製作所）、フェン | J.J.トムソン（陽極線の放物線法 1913。受賞 1906 物理は電子のため）、デンプスター（1918 磁場型質量分析計。受賞なし） | https://www.nobelprize.org/prizes/chemistry/1922/summary/ , https://www.nobelprize.org/prizes/chemistry/2002/summary/ , https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks |
| C-30 | 八木・宇田アンテナ **済み：[15-27]（O-010）** | 技術 | 1926（1926 論文、未来技術遺産の実物は1930） | 未来技術遺産 00210（八木・宇田アンテナ、2016）・IEEE Milestone: Directive Short Wave Antenna, 1924 | 4 本<br>子：[15-07] 無線電信（無線の電波を一方向に強く送る・受ける）<br>前提：[15-11] テレビ（テレビ放送を家の屋根で受ける標準のアンテナ）<br>説明される：[15-06] 電波の発見（導体の長さと間隔で電波が強め合う＝電磁波の干渉）<br>子：[15-06] 電波の発見（電波を一方向に集める。電磁波が金属の棒で反射・誘導される性質を使う） | 宇田新太郎（実験・設計の中心）、八木秀次（東北帝大） ／ 八木秀次、宇田新太郎（東北帝国大学） | 宇田新太郎の貢献が大きいのに長く「八木アンテナ」と呼ばれた（出典: Wikipedia Yagi–Uda antenna） ／ 宇田新太郎（実際の実験と最初の発表は宇田。英語論文と特許が八木の名だったため、海外では『八木アンテナ』と呼ばれる） | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Yagi%E2%80%93Uda_antenna , https://ethw.org/Milestones:Directive_Short_Wave_Antenna,_1924 , https://ethw.org/Yagi_Antenna |
| C-31 | ナイロン（最初の完全合成繊維） **済み：[2-51]（O-010）** | 技術 | 1935（1935-02 ナイロン66 合成、1938 発表、1939 ストッキング） | ACS 化学遺産 1995（ナイロン） | 4 本<br>子：[2-24] ベンゼン（原料のアジピン酸はベンゼン→シクロヘキサン経由）<br>子：[2-21] アンモニア水（もう一方の原料ヘキサメチレンジアミンは窒素をアンモニアからもらう）<br>子：[6-09] ハーバー・ボッシュ法（大量のアンモニアはハーバー・ボッシュ法から）<br>子：[0-06] 織物（絹・綿の織物の代わりの合成繊維） | カロザース、ジュリアン・ヒル（デュポン） | ジュリアン・ヒル（1930 にポリエステルの糸を引いた実験者） | https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks , https://www.acs.org/education/whatischemistry/landmarks/landmarksdirectory.html |
| C-32 | ガスタービンとジェットエンジン **済み：[4-29]（O-010）** | 技術 | 1939（1937 ホイットルの試運転、1939 ハインケル He178 初飛行・ノイシャテルの発電用ガスタービン、1945 ネ20） | ASME Landmark #135（Neuchâtel Gas Turbine）・機械遺産 No.007（FJR710 ジェットエンジン）・未来技術遺産 00338（ネ20）・00126（国産発電用1号ガスタービン）ほか | 4 本<br>子：[4-06] 蒸気タービン（蒸気タービンの羽根と回転体の技術を、燃えたガスで回す）<br>子：[6-03] 圧縮機（空気を圧縮機で押し込んでから燃やす（圧縮機が出力の多くを使う））<br>理解される：[I-02] 熱力学（効率はブレイトン・サイクル＝熱力学で決まる）<br>子：[1-37] ニッケル（高温の羽根はニッケル基の超合金） | フランク・ホイットル、ハンス・フォン・オハイン、ブラウン・ボベリ（受入れ試験はストドラ）、ネ20 は種子島時休・永野治ら | ホイットルとフォン・オハインは互いを知らずに独立に発明 | https://www.asme.org/about-asme/engineering-history/landmarks/135-neuchatel-gas-turbine , https://www.jsme.or.jp/kikaiisan/heritage_007_jp.html , https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Jet_engine |
| C-33 | NC（数値制御）工作機械 **済み：[8-34]（[P-04] 3Dプリンターの前提に）（O-010）** | 技術 | 1952（1949 パーソンズの構想、1952 MIT が NC フライス盤を公開、1956 日本初の NC（富士通）、1968 NC 旋盤 MTC） | 機械遺産 No.126（米国輸出を果たした NC 旋盤 MTC-2500R） | 4 本<br>子：[8-01] 旋盤（旋盤・フライス盤の刃の動きを数字（紙テープ）で指令）<br>子：[8-08] ボールねじ精密ステージ（TECH_TREE [8-08] の本文どおり、ボールねじと組み合わせて µm 級の位置決め）<br>子：[10-04] フィードバック制御（サーボ（位置のフィードバック）で軸を動かす）<br>前提：[P-04] 3Dプリンター（3D プリンターの動きは NC と同じ G コードの位置指令） | ジョン・パーソンズとフランク・ステューレン（構想）、MIT サーボ機構研究所（ゴードン・ブラウン、ジェームズ・マクドノー）、稲葉清右衛門（富士通→ファナック） | 構想を出したパーソンズは MIT に開発の主導権を取られ、長く名前が出なかった（出典: Wikipedia John T. Parsons） | https://www.jsme.or.jp/kikaiisan/heritage_126_jp.html , https://en.wikipedia.org/wiki/Numerical_control , https://en.wikipedia.org/wiki/John_T._Parsons |
| C-34 | 合成ダイヤモンド（高温高圧法） **済み：[1-55]（C-35 と1つに）（O-010）** | 技術 | 1954（1954年12月16日 GE で再現性のある合成に成功） | ASME Landmark #198（GE's Ultra High Pressure Apparatus for the Production of Diamonds） | 4 本<br>子：[1-07] 黒鉛（原料は黒鉛。55kbar・1500℃で炭素の並びを変える）<br>子：[1-37] ニッケル（鉄・ニッケルなど金属を溶媒（触媒）にすると変わりやすい）<br>理解される：[I-02] 熱力学（高圧ではダイヤの方が安定＝熱力学の相図）<br>前提：[9-32] ダイシング（ダイシング・ウェハー切断の刃の砥粒（天然でもできるが、量と値段で合成が前提）⚠️ ゲームはバニラのダイヤを使用中＝化学ル） | トレーシー・ホール、フランシス・バンディ、ハーバート・ストロング、ロバート・ウェントーフ（GE） | スウェーデンの ASEA（フォン・プラーテン、ルンドブラッドら）が1953年2月に合成していたが公表しなかった（出典: Wikipedia Synthetic diamond） | https://www.asme.org/about-asme/engineering-history/landmarks/198-ge-s-ultra-high-pressure-apparatus , https://en.wikipedia.org/wiki/Synthetic_diamond |
| C-35 | 超高圧と合成ダイヤモンド **済み：[1-55]（C-34 と同じ物）（O-010）** | 技術 | 1954（ブリッジマンの超高圧装置 1900年代〜、ASEA の合成 1953年2月（非公表）、GE の合成 1954年12月） | ノーベル物理学賞 1946（ブリッジマン） | 4 本<br>子：[1-07] 黒鉛（黒鉛を高温高圧でダイヤモンドに変える）<br>前提：[9-32] ダイシング（ダイシングのダイヤモンド刃（工業用ダイヤは合成が主））<br>前提：[9-12] ウェハー切断（ウェハー切断のダイヤモンドの刃）<br>前提：[9-13] ラッピング・鏡面研磨（研磨のダイヤモンド砥粒（要確認 ⚠️）） | パーシー・ブリッジマン | GE のトレイシー・ホール（ベルト型プレス、1954）、ストロング、ウェントーフ。ASEA（スウェーデン）のフォン・プラーテンとケンペが1953年に先に合成していたが公表しなかった（GIA、Wikipedia） | https://www.nobelprize.org/prizes/physics/1946/summary/ , https://www.gia.edu/gia-news-research-synthetic-diamonds-dark-industrials-bright-gems , https://en.wikipedia.org/wiki/Synthetic_diamond |
| C-36 | エサキダイオード（半導体のトンネル効果） **済み：[I-33]（存命の人の名前を出さないため、ノード名はトンネルダイオード）（O-010）** | 発見 | 1957（1957年7〜8月、東京通信工業（今のソニー）） | ノーベル物理学賞 1973（江崎玲於奈、存命とされる ⚠️：2026年時点で101歳、要確認） | 4 本<br>子：[9-04] 合金接合トランジスタ（ソニーが作っていた Ge の合金接合（不純物を濃くした接合）を調べて見つかった）<br>子：[9-01] Ge 帯域精製（不純物の量を正確に制御した Ge が前提）<br>説明する：[8-16] 走査プローブ顕微鏡（走査トンネル顕微鏡も同じトンネル効果で電流が流れる）<br>説明する：[9-38] GAA（微細なトランジスタで漏れ電流が増える理由の一つ（トンネル）） | 江崎玲於奈 | 黒瀬百合子・鈴木隆が共同発明者とする出典がある一方、江崎だけとする出典もある（Wikipedia と半導体産業人協会で食い違い ⚠️）。1958年のブリュッセル会議でショックレーが紹介して有名に | https://www.nobelprize.org/prizes/physics/1973/summary/ , https://shmj.or.jp/innovation50/english/detail_D01E.htm , https://en.wikipedia.org/wiki/Tunnel_diode , https://en.wikipedia.org/wiki/Leo_Esaki |
| C-37 | 産業用ロボット **済み：既存の [10-26] 産業用ロボット（川崎ユニメート 1969・MOTOMAN-L10 1977・SCARA 1978 を本物欄へ）（O-010）** | 技術 | 1961（1954 デボルの特許出願、1961 ユニメートが GM で稼働、1969 川崎ユニメート、1977 全電気式 MOTOMAN-L10、1978 SCARA） | 機械遺産 No.076（全電気式産業用ロボット モートマン-L10）・未来技術遺産 00064（川崎ユニメート2000型）・00041（MOTOMAN-L10）・00021（SCARA 試作機） | 4 本<br>子：[10-04] フィードバック制御（腕の位置を測って目標に合わせるフィードバック制御）<br>子：[4-08] 電動モーター（MOTOMAN-L10 は油圧をやめてモーターで全関節を動かした）<br>子：[15-12] 磁気記録（最初のユニメートは動きの手順を磁気ドラムに記憶した）<br>子：[10-13] マイクロプロセッサ（今のロボットはマイクロプロセッサで制御） | ジョージ・デボル、ジョセフ・エンゲルバーガー（ユニメーション）、安川電機、牧野洋（山梨大、SCARA） | — | https://www.jsme.or.jp/kikaiisan/heritage_076_jp.html , https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Unimate |
| C-38 | 半導体レーザー **済み：[7-25]（[15-13] 光ファイバーの光源・[9-48] ヘテロ構造の前提に）（O-010）** | 技術 | 1962（1962年秋、GE（2チーム）・IBM・MIT リンカーン研究所が独立にほぼ同時に発表。室温での連続発振は1970） | IEEE Milestone: Semiconductor Laser, 1962 | 4 本<br>子：[7-13] レーザー原理（1960年のレーザーの原理を、ガリウムヒ素の p-n 接合に電流を流すだけで実現した）<br>子：[B-07] LED（同じ GaAs 系の p-n 接合の発光。LED に光をはね返す鏡（結晶の割れた面）を付けたもの）<br>子：[1-44] ガリウム（材料はガリウムヒ素）<br>前提：[15-13] 光ファイバー（光ファイバー通信の光源（ノードの前提にも『LED or 半導体レーザー』とある）） | ロバート・N・ホール（GE スケネクタディ）、ニック・ホロニアック（GE シラキュース、見える光）、マーシャル・ネイサン（IBM）、キーズとクイスト（リンカーン研） | 林巌雄とモートン・パニッシュ（ベル研、1970 室温連続発振）、アルフョーロフとクレーマー（ヘテロ構造、2000 ノーベル物理学賞） | https://ethw.org/Milestones:Semiconductor_Laser,_1962 , https://en.wikipedia.org/wiki/Laser_diode |
| C-39 | ジョセフソン効果（電圧を量子で決める） **済み：[I-34]（N-13 も同じ物。予言した人は存命なので名前を出さず、ノード名は「超伝導のトンネル効果」）（O-010）** | 発見 | 1962（予言 1962、観測 1963。network_proposals N-13 の提案と同じもの） | ノーベル物理学賞 1973（ジョセフソン（存命）、ジエーバー、江崎と共同受賞） | 4 本<br>前提：[8-25] ワット天秤（ワット天秤の電圧測定はジョセフソン電圧標準）<br>前提：[I-09] 単位を自然の定数で決める（電圧が周波数とプランク定数で決まる＝定数で単位を作る）<br>子：[8-15] 水晶振動子（電圧標準はマイクロ波の周波数（周波数標準）とくらべる）<br>子：[5-14] 真空蒸着（接合は薄い金属膜を真空蒸着して作る） | ブライアン・ジョセフソン（存命）。超伝導体のトンネルはアイヴァー・ジエーバー（1960） | フィリップ・アンダーソンとジョン・ローウェル（ベル研）が1963年1月に初めて観測（アンダーソンは1977年に別の業績で受賞）。交流ジョセフソン効果はシャピロが同年に観測 | https://www.nobelprize.org/prizes/physics/1973/summary/ , https://en.wikipedia.org/wiki/Josephson_effect , https://physics.umd.edu/courses/Phys798C/AnlageSpring24/Rowell_JJ_Observation_ASC_2012.pdf |
| C-40 | CCD イメージセンサー **済み：既存の [9-42] 画像センサー（1985 の CCD ビデオカメラを本物欄へ）（O-010）** | 技術 | 1969（1969 発明、1985 CCD 8ミリビデオカメラ） | 未来技術遺産 00161（8ミリビデオカメラ CCD-V8、2014）ほか CCD-TR55・CCD-VX1・未来技術遺産 00212（CMOS センサーのデジカメ アレグレット PDR-2） | 4 本<br>子：[9-23] MOSFET（MOS キャパシタの列に電荷を貯めてバケツリレーで送る＝MOS 構造が前提）<br>子：[9-18] プレーナ技術（プレーナ技術の酸化膜・電極で作る）<br>前提：[B-10] スマホ（スマホのカメラ（今は CMOS センサー。CCD と同じ光電変換の考え方））<br>理解される：[B-02] 銀塩写真（フィルムの銀塩写真を電子の写真に置きかえた） | ボイルとスミス（ベル研、1969、2009 ノーベル物理学賞）、ソニーの越智成之ら（実用化） | マイケル・トンプセット（ベル研）が最初の CCD 撮像素子を作ったがノーベル賞から外れた（出典: Wikipedia Charge-coupled device） | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Charge-coupled_device |
| C-41 | クオーツ腕時計 **済み：[8-35]（O-010）** | 技術 | 1969（1927 水晶時計（ベル研）、1969 セイコー クオーツアストロン35SQ） | 機械遺産 No.066（時代を画した国産腕時計）・未来技術遺産 00258（セイコー クオーツアストロン35SQ、2018） | 4 本<br>子：[8-15] 水晶振動子（水晶振動子（32768Hz）を腕に入る大きさに）<br>子：[9-22] 集積回路（水晶の振動を数えて1秒にするのは時計用 IC（機械遺産の解説に明記））<br>子：[4-08] 電動モーター（針は小さなステップモーターで動かす）<br>子：[9-24] CMOS（1970年代のクオーツ時計は CMOS で電池を長持ちさせた ⚠️ CMOS 採用の年は要確認） | 諏訪精工舎（セイコー）の開発チーム。水晶時計はマリソンとホートン（ベル研、1927） | — | https://www.jsme.or.jp/kikaiisan/heritage_066_jp.html , https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Seiko_Astron |
| C-42 | フロン（CFC）とオゾン層の破壊 **済み：既存の [I-17] フロンとオゾン層（1971 ラブロックの測定を本物欄へ）（O-010）** | 発見 | 1974（ミジリーのフロン冷媒 1928、ラブロックの大気中測定 1971、モリーナとローランド 1974、モントリオール議定書 1987） | ノーベル化学賞 1995（クルッツェン、モリーナ、ローランド）・ACS 化学遺産 2017（フロンとオゾン層破壊） | 4 本<br>子：[11-13] 冷蔵・冷凍（フロンはアンモニアや SO2 に代わる安全な冷蔵庫の冷媒として作られた）<br>子：[12-12] 換気・空調（エアコンの冷媒）<br>子：[2-10] フッ酸（フロンは四塩化炭素＋フッ酸で作る（スワーツ反応））<br>子：[1-43] アンチモン（スワーツ反応の触媒はアンチモンのハロゲン化物） | クルッツェン、モリーナ、ローランド | ミジリー（フロンを冷媒として発明 1928）、ラブロック（電子捕獲検出器で大気中のフロンを初めて測定） | https://www.nobelprize.org/prizes/chemistry/1995/summary/ , https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks |
| C-43 | 量子ホール効果（抵抗を量子で決める） **済み：[I-35]（N-14 も同じ物）（O-010）** | 発見 | 1980（1980年2月4〜5日の夜、グルノーブル強磁場研究所。network_proposals N-14 の提案と同じもの） | ノーベル物理学賞 1985（フォン・クリッツィング、存命） | 4 本<br>子：[9-23] MOSFET（見つかったのは Si MOSFET の反転層（2次元の電子）の中）<br>前提：[8-25] ワット天秤（ワット天秤は抵抗を量子ホール効果で正確に測る）<br>前提：[I-09] 単位を自然の定数で決める（2019 年の SI でプランク定数と e を固定する根拠の一つ）<br>子：[6-07] 空気分離（極低温（液体ヘリウム）が必要） | クラウス・フォン・クリッツィング（存命）。論文の共著者はドルダ（シーメンス、試料）とペッパー（ケンブリッジ） | 安藤恒也・松本幸夫・植村泰忠（東大）が1975年に e²/h の整数倍を理論で示した。1975〜1981年に川路紳治らの日本の実験グループが平らな部分を観測していたが精度は調べなかった（ノーベル財団の発表文） | https://www.nobelprize.org/prizes/physics/1985/press-release/ , https://www.europhysicsnews.org/articles/epn/pdf/1985/11/epn19851611p1.pdf |
| C-44 | 光ディスク（CD） **済み：[15-28]（O-010）** | 技術 | 1982（1979 規格の試作、1982 CD プレーヤー発売） | 未来技術遺産 00270（世界初 CD プレーヤー CDP-101、2019）・未来技術遺産 00201〜00203（レーザーディスク、2015） | 4 本<br>子：[7-13] レーザー原理（半導体レーザーの光で 1µm ほどのピットを読む）<br>説明される：[15-14] 情報理論（傷があっても音が出るのは誤り訂正符号（リード・ソロモン符号）＝情報理論）<br>子：[9-22] 集積回路（信号処理・復号は LSI（未来技術遺産の MPEG2 デコーダ LSI も））<br>前提：[B-09] ゲーム機（プレイステーション（1994）はゲームを CD-ROM で配った） | フィリップス（ルー・オッテンス、キース・スハウハメル・イミンク＝存命）とソニー（土井利忠ら）の共同規格 | — | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Compact_disc |
| C-45 | ホップフィールド・ネットワークとボルツマンマシン（物理から生まれた機械学習） **済み：[I-36]（受賞者は存命なので名前を出さない）（O-010）** | 発見 | 1982（ホップフィールド 1982、ボルツマンマシン 1983〜1985） | ノーベル物理学賞 2024（ホップフィールド（存命）、ヒントン（存命）） | 4 本<br>子：[I-05] パーセプトロン（パーセプトロンの人工ニューロンを、たがいにつないだ網に広げた）<br>前提：[10-16] ニューラルネット（ヒントンの研究（ボルツマンマシン・誤差逆伝播法・深い網の事前学習）が今の深層学習につながる）<br>子：[I-02] 熱力学（エネルギーを下げる・ボルツマン分布＝統計力学（熱力学）の道具を使う）<br>子：[B-11] AIチップ（大きな網を動かすには AI チップの計算力が要る） | ジョン・ホップフィールド（存命）、ジェフリー・ヒントン（存命）。ボルツマンマシンはセジュノウスキー（存命）と共同 | 甘利俊一（1972、学習する再帰型ネットワークを発表、存命）、中野馨（1972、連想記憶）。リトル（1974）（BCAM の解説、Wikipedia） | https://www.nobelprize.org/prizes/physics/2024/summary/ , https://en.wikipedia.org/wiki/Hopfield_network , https://en.wikipedia.org/wiki/Shun%27ichi_Amari , https://bcamath.org/bcam-smrnn-seminar-reading-group-john-hopfields-1982-paper-and-colloquium-the-2024-physics-nobel |
| C-46 | 浮動小数点演算の標準（IEEE 754） **済み：[10-32]（[B-11] AI チップの前提に）（O-010）** | 技術 | 1985（1977 小委員会、1978 カハンらの原案、1980 インテル 8087 が最初の実装、1985 承認） | IEEE Milestone: IEEE Standard 754 for Binary Floating-Point Arithmetic, 1985・IEEE Milestone: Intel 8087 Math Coprocessor, 1980・ACMチューリング賞 1989（ウィリアム・カハン） | 4 本<br>子：[10-11] 加算器・カウンタ・レジスタ（加算器・レジスタの上に『指数と仮数』の規則を決め、小数を同じ答えで計算できるようにした）<br>子：[10-06] リレー計算機（1941 ツーゼの Z3 がすでに2進の浮動小数点を使っていた。754 はその考えを全部の計算機で共通にした）<br>子：[10-13] マイクロプロセッサ（最初の実装はマイクロプロセッサ 8086 の相棒チップ 8087）<br>前提：[B-11] AIチップ（AI チップは半精度（16ビット）などの浮動小数点で掛け算・足し算をする。半精度は 754 の2008年版で決まった形） | ウィリアム・カハン、ジェローム・クーネン、ハロルド・ストーン（カリフォルニア大バークレー）、ジョン・パーマー（インテル） | ジョン・パーマー（インテル）が1976年にカハンに声をかけたのが始まり。インテルの役割をどう書くかは資料で差がある（ETHW） | https://ethw.org/Milestones:IEEE_Standard_754_for_Binary_Floating-Point_Arithmetic,_1985 , https://ethw.org/Milestones:Intel_8087_Math_Coprocessor,_1980 , https://math.berkeley.edu/node/3452 |
| C-47 | 巨大磁気抵抗（GMR）とハードディスクの読み取りヘッド **済み：[I-37]（O-010）** | 発見 | 1988（発見 1988、IBM の製品ヘッド 1997） | ノーベル物理学賞 2007（フェール（存命）、グリュンベルク） | 4 本<br>子：[15-12] 磁気記録（磁気記録の読み取りを何十倍も細かくした）<br>子：[9-26] スパッタリング（IBM はスパッタリングでナノメートルの多層膜を作った）<br>前提：[B-16] パソコン（パソコンの大容量ハードディスク）<br>子：[10-01] 電磁石（磁性と電流の関係（電磁石からつながる）） | アルベール・フェール（存命）、ペーター・グリュンベルク | スチュアート・パーキン（IBM、存命）らがスパッタリングで多層膜を量産的に作り、スピンバルブのヘッド（1997年に製品化）にした（Physics Today 2007） | https://www.nobelprize.org/prizes/physics/2007/summary/ , https://aip.brightspotcdn.com/PTO.v60.i12.14_1.online.pdf , https://en.wikipedia.org/wiki/Stuart_Parkin |
| C-48 | 蒸気ハンマー **済み：[1-56]（O-010）** | 技術 | 1842（1839 ナスミスの設計、1840ごろ ル・クルーゾで最初の実機、1842 ナスミスの特許、1865 横須賀へ輸入） | ASME Landmark #69（Creusot Steam Hammer、1876）・機械遺産 No.058（旧横須賀製鉄所 スチームハンマー） | 3 本<br>子：[4-02] 蒸気機関（蒸気の力でハンマーを持ち上げ・打ち下ろす）<br>子：[1-08] 鍛鉄（人の腕では打てない大きな鍛鉄・鋼の塊を鍛える）<br>子：[1-26] 鋼（大きな鋼の軸・ピストン棒の鍛造（ASME：打撃100トン）） | ジェームズ・ナスミス、シュナイダー社（ル・クルーゾ）のフランソワ・ブルドン | ナスミスの図を見たブルドンが先に作った（先後に争いあり。出典: Wikipedia Steam hammer） | https://www.asme.org/about-asme/engineering-history/landmarks/69-creusot-steam-hammer , https://www.jsme.or.jp/kikaiisan/heritage_058_jp.html , https://en.wikipedia.org/wiki/Steam_hammer |
| C-49 | ファン・デル・ワールスの状態方程式（本物の気体と液化） **済み：[I-38]（O-011）** | 発見 | 1873（博士論文 1873） | ノーベル物理学賞 1910（ファン・デル・ワールス） | 3 本<br>説明する：[6-07] 空気分離（気体はなぜ・どこまで冷やすと液体になるか（臨界温度）。オネスはこの式でヘリウムの液化を計画した）<br>説明する：[6-03] 圧縮機（圧縮した気体が理想気体からずれる）<br>子：[I-02] 熱力学（熱力学・気体分子運動論の上に立つ） | ヨハネス・ディーデリク・ファン・デル・ワールス | 臨界点の実験はアンドルーズ（1869、CO2）（要確認 ⚠️） | https://www.nobelprize.org/prizes/physics/1910/summary/ |
| C-50 | 化学平衡と化学熱力学（圧力・温度で反応の行き先が決まる） **済み：[I-39]（O-011）** | 発見 | 1884（ギブズ 1876、ルシャトリエ 1884、ファント・ホッフ 1884『化学動力学の研究』、ネルンスト熱定理 1906） | ノーベル化学賞 1901（ファント・ホッフ）・ノーベル化学賞 1920（ネルンスト） | 3 本<br>説明する：[6-09] ハーバー・ボッシュ法（なぜ高圧（約200気圧）で、なぜ温度は高すぎない約450℃なのか＝平衡と速さの妥協）<br>説明する：[6-13] 接触法硫酸（SO2→SO3 は熱を出す反応なので低温ほど平衡は有利、でも遅い。約450℃の妥協）<br>理解される：[I-02] 熱力学（化学平衡は熱力学（エントロピー・自由エネルギー）を化学反応に当てはめたもの） | ファント・ホッフ（平衡定数の温度依存）、ネルンスト（熱定理＝第三法則。アンモニアの平衡も測った） | ギブズ（自由エネルギー、1876〜78。ノーベル賞創設前に死去）、ルシャトリエ（平衡移動の原理、受賞なし） | https://www.nobelprize.org/prizes/chemistry/1901/summary/ , https://www.nobelprize.org/prizes/chemistry/1920/summary/ |
| C-51 | 回転磁界と誘導電動機・三相交流送電 **済み：回転磁界と誘導電動機は既存の [4-22]、三相送電を [4-30] に（O-011）** | 技術 | 1888（1885〜88 フェラリスの回転磁界と二相モーター、1888 テスラの特許、1891 ラウフェン〜フランクフルト 約175km の三相送電） | IEEE Milestone: Rotating Fields and Early Induction Motors, 1885-1888・IEEE Milestone: 三相交流の長距離送電（ラウフェン〜フランクフルト）, 1891・IEEE Milestone: Folsom Powerhouse, 1895 | 3 本<br>子：[4-10] 変圧器（変圧器で電圧を上げられる交流だから遠くへ送れた（ラウフェン〜フランクフルトは変圧器で高電圧にした））<br>子：[4-08] 電動モーター（整流子（ブラシ）の要らないモーター。直流モーターの次の形）<br>前提：[9-41] パワー半導体とインバータ（インバータが回す『モーター』はほとんどが交流の誘導電動機・同期電動機。回転磁界の周波数を変えて速さを変える） | ガリレオ・フェラリス（トリノ）、ニコラ・テスラ（1888 独立）、ミハイル・ドリヴォ＝ドブロヴォルスキー（AEG、三相の誘導電動機と送電、1891）、オスカー・フォン・ミラー | フェラリスは特許を取らず公開した。三相方式の実用化はドリヴォ＝ドブロヴォルスキーの功績が大きいが、日本の教科書ではテスラの名だけが出やすい | https://ethw.org/Milestones:Rotating_Fields_and_Early_Induction_Motors,_1885-1888 , https://en.wikipedia.org/wiki/Mikhail_Dolivo-Dobrovolsky , https://ethw.org/Milestones:List_of_IEEE_Milestones |
| C-52 | 放射能と元素の変換（α線＝ヘリウム） **済み：[I-40]（O-011）** | 発見 | 1903（ベクレル 1896、キュリー夫妻 1898、ラザフォードとソディの変換説 1902〜03、α＝He の証明 1908〜09、天然ガス中のヘリウム 1905（カンザス）） | ノーベル化学賞 1908（ラザフォード）・ノーベル化学賞 1911（マリー・キュリー）・ノーベル化学賞 1921（ソディ）・ACS 化学遺産 2000（天然ガス中のヘリウム発見） | 3 本<br>説明する：[6-15] ヘリウム 回避（天然ガスにヘリウムがあるのは、地下のウラン・トリウムの α 崩壊で何億年もかけて溜まったから）<br>子：[B-03] X線管（ベクレルは X 線の発見（1895）に刺激されて放射能を見つけた）<br>説明する：[I-03] 周期表（元素は変わらないという考えが崩れ、原子番号・同位体へ） | ラザフォード、ソディ、マリー・キュリー、ラムゼー（1903 ラジウムからヘリウムが出ることをソディと確認） | ロイズ（1909 ラザフォードと α 粒子がヘリウムになることを分光で示した学生）、ケーディとマクファーランド（1905 カンザスの天然ガスでヘリウムを発見、ACS 化学遺産） | https://www.nobelprize.org/prizes/chemistry/1908/summary/ , https://www.nobelprize.org/prizes/chemistry/1921/summary/ , https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks |
| C-53 | 傾斜パッド式スラスト軸受（油膜で支える） **済み：[8-36]（O-011）** | 技術 | 1912（1907 特許出願・1910 登録、1912 ホルトウッド水力発電所に初設置） | ASME Landmark #123（Kingsbury Thrust Bearing） | 3 本<br>前提：[4-01] 水車発電機（大きな立て軸の水車発電機の重さと水の押す力を、くさび形の油膜で支える）<br>前提：[4-06] 蒸気タービン（タービンの軸が押される力（スラスト）を受ける）<br>理解される：[8-20] 玉軸受（玉で転がす軸受と、油膜で浮かせる軸受は軸受の2つの道） | アルバート・キングスベリー（米）、A.G.M. ミッチェル（豪、独立に発明） | ミッチェルは同じ原理を独立に発明（ASME の解説に明記）。理論の土台はレイノルズの潤滑理論（1886） | https://www.asme.org/about-asme/engineering-history/landmarks/123-kingsbury-thrust-bearing , https://en.wikipedia.org/wiki/Fluid_bearing |
| C-54 | ボラン（ホウ素の水素化物、ジボラン B2H6） **済み：[2-52]（実装済みのドーパント源 [9-11] への線は引かず本物欄で）（O-011）** | 発見 | 1912（シュトックの合成 1912〜、リプスコムの三中心結合の構造解明 1950年代） | ノーベル化学賞 1976（リプスコム） | 3 本<br>子：[1-33] ホウ素（ホウ素化合物から作る）<br>前提：[9-11] ドーパント源（ジボランは p 型ドーパントのガス源（拡散・CVD 中のドーピング））<br>前提：[9-30] エピタキシーと CVD（ホウ素入りエピ膜・BPSG 膜の CVD 原料） | リプスコム | アルフレート・シュトック（1912〜 ボランを初めて作り、空気を嫌う気体を扱うガラス真空ラインを開発。受賞なし） | https://www.nobelprize.org/prizes/chemistry/1976/summary/ |
| C-55 | 連鎖反応（爆発のしくみ） **済み：[I-41]（O-011）** | 発見 | 1913（ボーデンシュタイン 1913 H2+Cl2 の連鎖、ネルンスト 1918 原子連鎖、セミョーノフ・ヒンシェルウッドの分岐連鎖 1920年代末） | ノーベル化学賞 1956（ヒンシェルウッド、セミョーノフ） | 3 本<br>説明する：[6-06] 塩酸（H2 と Cl2 を混ぜて光を当てると爆発する（Cl 原子の連鎖）。だから塩酸合成炉は混ぜずに炎で燃やす）<br>説明する：[7-02] 溶融石英（酸水素の爆鳴気がなぜ一気に爆発するか（分岐連鎖））<br>説明する：[14-09] 内燃機関（エンジンのノッキング＝燃え残りの自己着火） | セミョーノフ、ヒンシェルウッド | ボーデンシュタイン（連鎖反応の概念 1913、受賞なし） | https://www.nobelprize.org/prizes/chemistry/1956/summary/ |
| C-56 | 特性X線と元素の X 線分光（モーズリーの法則） **済み：[I-42]（O-011）** | 発見 | 1913（バークラの特性X線 1906〜1911、モーズリー 1913〜1914、シーグバーンの X線分光 1920年代） | ノーベル物理学賞 1917（バークラ）・ノーベル物理学賞 1924（M. シーグバーン） | 3 本<br>説明する：[I-03] 周期表（周期表の順番は原子量ではなく原子番号（核の電荷））<br>子：[B-03] X線管（X線管で元素ごとの X 線を出して測る）<br>説明する：[1-19] 希土類分離（希土類の未発見元素の席（61番など）が分かり、分離の確認に使われた（要確認 ⚠️）） | チャールズ・バークラ、マンネ・シーグバーン | ヘンリー・モーズリーが1913年に元素の特性X線の振動数が原子番号で決まることを示し、周期表の順番を原子番号で決めた。1915年ガリポリで戦死し受賞できなかった（要確認 ⚠️：出典を足す） | https://www.nobelprize.org/prizes/physics/1917/summary/ , https://www.nobelprize.org/prizes/physics/1924/summary/ |
| C-57 | フェルミ＝ディラック統計（半導体の中の電子の数え方） **済み：[I-43]（O-011）** | 発見 | 1926（1924〜1926（フェルミの論文は1926年。ディラックも1926年に独立に発表）） | IEEE Milestone: Enrico Fermi's Major Contribution to Semiconductor Statistics, 1924-1926 | 3 本<br>説明する：[9-05] Geダイオード（なぜ p 型と n 型の境目で電流が一方向にしか流れないか（電子とホールの数がエネルギーでどう決まるか））<br>説明する：[9-03] 点接触トランジスタ（点接触トランジスタで、少しの不純物・電圧で流れる電子の数が大きく変わる理由）<br>説明する：[9-11] ドーパント源（ドーパントをごく少し入れるだけで電気の通りやすさが何桁も変わる理由（電子が何個まで1つの状態に入れるかの規則）） | エンリコ・フェルミ（フィレンツェ、1926）、ポール・ディラック（1926、粒子に名前を付けた）、ゾンマーフェルト（1927、金属の電子に応用） | パスクアル・ヨルダンが1925年に同じ統計にたどり着いていたが、すぐには発表されなかった（ETHW 'On the origin of Fermi-Dirac statistics'） | https://ethw.org/Milestones:Enrico_Fermi%27s_Major_Contribution_to_Semiconductor_Statistics,_1924-1926 , https://ethw.org/On_the_origin_of_Fermi-Dirac_statistics |
| C-58 | 電子の波動性と電子回折 **済み：[I-44]（O-011）** | 発見 | 1927（ド・ブロイの仮説 1924、デヴィッソン＝ガーマーと G. P. トムソンの実験 1927、菊池正士 1928） | ノーベル物理学賞 1929（ド・ブロイ）・ノーベル物理学賞 1937（デヴィッソン、G. P. トムソン） | 3 本<br>説明する：[8-11] 電子顕微鏡（電子の波長は光の数万分の1なので、電子顕微鏡は細かく見える）<br>説明する：[I-04] 光の解像度の限界（解像度の限界は波長で決まる（電子でも同じ））<br>前提：[9-30] エピタキシーと CVD（エピタキシー・MBE で結晶の表面を見る電子回折（RHEED）（要確認 ⚠️：導入年）） | ルイ・ド・ブロイ、クリントン・デヴィッソン、ジョージ・パジェット・トムソン | レスター・ガーマー（デヴィッソンの共同実験者）。菊池正士（理研）が1928年に雲母・方解石で電子線回折の像（菊池線）を撮り説明した（日本学士院紀要 1928） | https://www.nobelprize.org/prizes/physics/1929/summary/ , https://www.nobelprize.org/prizes/physics/1937/summary/ , https://www.jstage.jst.go.jp/article/pjab1912/4/8/4_8_471/_article , https://en.wikipedia.org/wiki/Seishi_Kikuchi |
| C-59 | 化学結合の量子論（共有結合・電気陰性度・混成軌道） **済み：[I-45]（O-011）** | 発見 | 1931（ハイトラー＝ロンドン 1927、ポーリング 1931 混成軌道・1932 電気陰性度、マリケンの分子軌道 1928〜32） | ノーベル化学賞 1954（ポーリング）・ノーベル化学賞 1966（マリケン） | 3 本<br>説明する：[9-11] ドーパント源（Si は4本の手（sp3）でダイヤモンド型に並ぶ。手が5本の P は電子が1個余り n 型、3本の B は1個足りず p）<br>説明する：[6-14] フッ素ガス（フッ素は電気陰性度が最大（4.0）なので何とでも激しく反応する）<br>説明する：[I-03] 周期表（周期表の族ごとの『手の数』（原子価）の理由） | ポーリング、マリケン | ハイトラーとロンドン（1927 水素分子の共有結合を量子力学で初めて説明、受賞なし）、フント（分子軌道法の共同創始者、受賞なし） | https://www.nobelprize.org/prizes/chemistry/1954/summary/ , https://www.nobelprize.org/prizes/chemistry/1966/summary/ |
| C-60 | 接触分解（触媒で重い油をガソリンに） **済み：[2-53]（O-011）** | 技術 | 1937（ウードリー法 1936〜37（サン石油）、流動接触分解 1942（エッソ、ベイトンルージュ）） | ACS 化学遺産 1996（ウードリー法）・ACS 化学遺産 1998（流動床反応器） | 3 本<br>子：[2-35] 原油と蒸留（原油の蒸留で残る重い油を、触媒（活性白土・シリカアルミナ）で割ってガソリンにする）<br>子：[14-09] 内燃機関（ノッキングしにくい高オクタン価ガソリンの需要から生まれた）<br>子：[1-14] バイヤー法（合成触媒はシリカ＋アルミナ） | ウジェーヌ・ウードリー | 流動接触分解はエッソ・MIT（ルイスとギリランド）ら多数のチーム | https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks , https://www.acs.org/education/whatischemistry/landmarks/landmarksdirectory.html |
| C-61 | 放電加工 **済み：[8-37]（O-011）** | 技術 | 1943（1943 ラザレンコ夫妻（ソ連）、1954 ジャパックス（日本）、1970年代 ワイヤ放電・NC 化） | 未来技術遺産 00373〜00376（形彫・ワイヤ放電加工機、2024） | 3 本<br>理解される：[I-07] 電気アークのしくみ（電極と金属のすき間の火花が表面を少しずつ溶かして飛ばす＝アーク・放電のしくみ）<br>子：[8-01] 旋盤（刃では削れない焼入れ鋼・超硬合金を形どおりに加工（金型））<br>子：[9-41] パワー半導体とインバータ（後の加工電源はトランジスタで火花のパルスを作る） | ボリス・ラザレンコとナタリア・ラザレンコ、井上潔（ジャパックス） | — | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Electrical_discharge_machining |
| C-62 | ホログラフィー **済み：[7-26]（O-011）** | 技術 | 1947（1947 着想・特許出願、1948 Nature、1962〜64 レーザーで実用（リース＆ウパトニークス、デニシュク）） | IEEE Milestone: Invention of Holography, 1947 | 3 本<br>子：[8-11] 電子顕微鏡（電子顕微鏡の解像度を上げるための『波面の再生』として考えられた）<br>子：[7-09] 干渉計（光の干渉縞に、波の強さだけでなく位置（位相）まで記録する）<br>子：[7-13] レーザー原理（実用はそろった光（レーザー）を待った（IEEE の銘板）） | デニス・ガボール（BTH 社ラグビー研究所、1971 ノーベル物理学賞） | エメット・リースとユリス・ウパトニークス（1962〜64 レーザーで立体像）、ユーリ・デニシュク（1962 反射型ホログラム） | https://ethw.org/Milestones:Invention_of_Holography,_1947 , https://en.wikipedia.org/wiki/Holography |
| C-63 | 誤り訂正符号（ハミング符号） **済み：[15-29]（O-011）** | 技術 | 1950（1947ごろ着想、1950 論文『誤りを見つけ、直す符号』） | ACMチューリング賞 1968（リチャード・ハミング） | 3 本<br>子：[10-06] リレー計算機（週末にベル研のリレー計算機が誤りを見つけると仕事を捨ててしまい、ハミングが『見つけられるなら直せるはず』と考えた）<br>子：[15-14] 情報理論（シャノンが『雑音があっても誤りなく送れる』と示した限界に近づく具体的な方法の最初）<br>前提：[B-10] スマホ（スマホのフラッシュメモリや無線は、誤り訂正なしでは使えないほど誤りが出る） | リチャード・ハミング（ベル研） | マルセル・ゴレイ（1949 ゴレイ符号）、シャノン（1948 の論文でハミングの符号を紹介） | https://history.computer.org/pioneers/hamming.html , https://en.wikipedia.org/wiki/Hamming_code , https://en.wikipedia.org/wiki/Turing_Award |
| C-64 | 原子力発電 **済み：[4-31]（事故と使用済み燃料の課題も本物欄に。兵器の話・作り方は書いていない）（O-011）** | 技術 | 1951（1951年12月20日 EBR-I で初の発電、1957 シッピングポート（初の本格的な商用規模）） | ASME Landmark #39（Experimental Breeder Reactor I）・ASME Landmark #47（Shippingport Nuclear Power Station） | 3 本<br>子：[4-14] 蒸気発電（核分裂の熱で水を沸かし、あとは蒸気発電と同じ）<br>子：[4-06] 蒸気タービン（蒸気タービンで発電機を回す）<br>理解される：[I-02] 熱力学（熱効率は熱力学（蒸気の温度が低めなので約33%）） | ウォルター・ジン（設計、アルゴンヌ）、エンリコ・フェルミ（支援）、シッピングポートはハイマン・リッコーバーの海軍炉グループ | — | https://www.asme.org/about-asme/engineering-history/landmarks/39-Experimental-Breeder-Reactor-I , https://www.asme.org/about-asme/engineering-history/landmarks/47-Shippingport-Nuclear-Power-Station |
| C-65 | 純酸素上吹き転炉（LD 転炉） **済み：既存の [1-48] 純酸素転炉（S-001）（O-011）** | 技術 | 1952（1952 リンツで操業、1955 米国に導入（ASME の展示炉）） | ASME Landmark #104（Basic-Oxygen Steel Making Vessel） | 3 本<br>子：[3-08] 転炉（ベッセマー転炉の空気を純酸素に（窒素が鋼に入らない））<br>前提：[6-07] 空気分離（大量の安い酸素＝空気分離（深冷分離）が無いと成り立たない）<br>子：[1-26] 鋼（今の鋼の大部分を作る方法。平炉の約3倍の速さ（ASME）） | VÖEST（リンツ）・アルピネ（ドナヴィッツ）の技術者 | スイスのローベルト・ドゥラーが1948〜49年に純酸素の吹き込み実験を先に行った（出典: Wikipedia Basic oxygen steelmaking） | https://www.asme.org/about-asme/engineering-history/landmarks/104-basic-oxygen-steel-making-vessel , https://en.wikipedia.org/wiki/Basic_oxygen_steelmaking |
| C-66 | クロマトグラフィー（ガスクロ）＝混ざり物をふるい分けて量る **済み：[2-54]（O-011）** | 技術 | 1952（ツヴェット 1906 植物色素、マーティンとシング分配クロマト 1941、ジェームズとマーティンのガスクロ 1952） | ノーベル化学賞 1952（マーティン、シング）・ACS 化学遺産 2019（ガスクロマトグラフ質量分析） | 3 本<br>前提：[1-16] トリクロロシラン合成・蒸留（トリクロロシランを蒸留して 9N 以上にするには、残る不純物を量る方法が要る（工業ではガスクロ））<br>前提：[6-08] 希ガス精製（希ガスの純度を確かめる）<br>説明する：[2-35] 原油と蒸留（原油の成分を一つずつ分けて見せる（石油化学の分析の基本）） | マーティン、シング | ツヴェット（1906 発明、名付け親。受賞なし）、アンソニー・ジェームズ（1952 ガスクロの共同開発者、受賞なし） | https://www.nobelprize.org/prizes/chemistry/1952/summary/ , https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks |
| C-67 | 高圧直流送電（HVDC） **済み：[4-32]（O-011）** | 技術 | 1954（1954 ゴットランド島の連系（20MW・100kV・海底ケーブル96km）。1970 サイリスタのバルブを追加） | IEEE Milestone: Gotland High Voltage Direct Current Link, 1954 | 3 本<br>子：[4-11] 整流器（交流→直流→交流の変換は大型の水銀整流器（水銀アークバルブ））<br>子：[15-03] 海底ケーブル（世界初の直流用の海底ケーブル（油をしみこませた紙の絶縁））<br>子：[4-10] 変圧器（変換所の両側で変圧器を使って電圧を合わせる） | ウノ・ラム（ASEA、水銀アークバルブを25年かけて開発） | — | https://ethw.org/Milestones:Gotland_High_Voltage_Direct_Current_Link,_1954 , https://ieeemilestones.ethw.org/w/images/8/81/Milestone_Reference_4-Uno_Lamm_the_father_of_HVdc_transmission.pdf |
| C-68 | 磁気ディスク装置（ハードディスク・フロッピー） **済み：[10-33]（O-011）** | 技術 | 1956（1956 IBM 350 RAMAC、1971 フロッピー、1980ごろ 3.5インチ） | ASME Landmark #90（IBM 350 RAMAC Disk File）・未来技術遺産 00341（3.5インチ・マイクロフロッピーディスク、2022） | 3 本<br>子：[15-12] 磁気記録（磁気記録を回る円盤に＝好きな場所をすぐ読める（ランダムアクセス））<br>前提：[B-16] パソコン（パソコンのプログラム・データの保存（フロッピー→ハードディスク））<br>子：[10-14] マイクロコンピュータ（マイコンの外部記憶） | IBM サンノゼ研究所（レイノルド・ジョンソンのチーム）、フロッピーは IBM（アラン・シュガートら）、3.5インチはソニー | — | https://www.asme.org/about-asme/engineering-history/landmarks/90-IBM-350-RAMAC-Disk-File , https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/IBM_350 |
| C-69 | サイリスタ（SCR：大電流をオン・オフする半導体スイッチ） **済み：[9-49]（[9-41] パワー半導体の前提に）（O-011）** | 技術 | 1957（1954〜55 ベル研の p-n-p-n スイッチ、1957 GE が3端子の SCR を発売（最初の試作は1957年7月末、300V・7A）） | IEEE Milestone: SCR/Thyristor, 1957 | 3 本<br>子：[9-16] 熱拡散（シリコンに熱拡散で p・n・p・n の4層を作る）<br>子：[4-11] 整流器（水銀整流器・サイラトロン（ガス入りの管）を固体の素子に置き換えた）<br>前提：[9-41] パワー半導体とインバータ（電車・電力のインバータは最初サイリスタで作られ、GTO・IGCT はその子孫（IEEE の銘板）） | GE の開発チーム（ゴードン・ホール、フランク・W・ガッツウィラーら） | ショックレー（p-n-p-n の考え）、ベル研のモル・タネンバウム・ゴールディ・ホロニアック（1956 シリコン p-n-p-n スイッチ。ホロニアック 2001 の論文） | https://ethw.org/Milestones:SCR/Thyristor,_1957 , https://ieeemilestones.ethw.org/w/images/d/dd/Milestone_Reference_2-The_Silicon_pnpn_Switch_Holonyak_2001.pdf |
| C-70 | 炭素繊維 **済み：[1-57]（O-011）** | 技術 | 1961（ベーコン（ユニオン・カーバイド）1958 レーヨン系高性能、進藤昭男 1961 PAN系） | ACS 化学遺産 2003（高性能炭素繊維） | 3 本<br>子：[1-07] 黒鉛（繊維の中身は黒鉛の層）<br>子：[3-10] アチソン炉（黒鉛化の高温処理は人造黒鉛（アチソン炉）の技術の延長）<br>前提：[4-15] リチウムイオン電池（吉野はポリアセチレンから炭素材料（旭化成の炭素繊維系）の負極に切り替えて実用化に近づいた） | ロジャー・ベーコン（1958 黒鉛ウィスカー） | 進藤昭男（大阪工業技術試験所、1961 PAN 系炭素繊維＝今の主流） | https://en.wikipedia.org/wiki/National_Historic_Chemical_Landmarks , https://asianews.network/?p=6491 |
| C-71 | 計算化学（密度汎関数理論 DFT） **済み：[I-46]（共著者の存命の人は名前を出さない）（O-011）** | 技術 | 1965（ホーエンベルク＝コーン 1964、コーン＝シャム 1965、ポープルの Gaussian 1970） | ノーベル化学賞 1998（コーン、ポープル） | 3 本<br>子：[10-13] マイクロプロセッサ（電子の式を計算機で解く）<br>子：[10-18] コンパイラ（Gaussian などは FORTRAN のプログラム）<br>説明する：[I-03] 周期表（原子・分子の性質を電子密度から計算で出せる） | コーン、ポープル | ピエール・ホーエンベルク（1964 定理の共著者、受賞なし）、沈呂九 L. J. Sham（存命、1965 コーン＝シャム方程式の共著者、受賞なし） | https://www.nobelprize.org/prizes/chemistry/1998/summary/ |
| C-72 | オペレーティングシステム（時分割と UNIX） **済み：[10-34]（存命の人は名前を出さない）（O-011）** | 技術 | 1969（1961 CTSS（MIT、時分割）、1969 UNIX（ベル研）、1973 UNIX を C 言語で書き直す） | ACMチューリング賞 1990（コルバト：時分割）・ACMチューリング賞 1983（トンプソンとリッチー：UNIX） | 3 本<br>子：[10-18] コンパイラ（UNIX は1973年に高級言語 C で書き直され、ほかの計算機に移せるようになった）<br>子：[10-07] 真空管フリップフロップと計算機（1台の高価な計算機を多くの人で順番に細かく使う（時分割））<br>前提：[B-10] スマホ（スマホの OS（iOS・Android）は UNIX 系の流れ） | フェルナンド・コルバト（CTSS）、ケン・トンプソン（存命）、デニス・リッチー（UNIX・C 言語） | マージョリー・ダゲットとロバート・デイリー（CTSS の共同開発）、ダグ・マキルロイ（パイプ）、ジョー・オサンナ（UNIX） | https://en.wikipedia.org/wiki/Turing_Award , https://en.wikipedia.org/wiki/Compatible_Time-Sharing_System , https://en.wikipedia.org/wiki/Unix |
| C-73 | 回路シミュレーター SPICE **済み：[10-35]（O-011）** | 技術 | 1971（1969〜70 バークレーの授業の課題（CANCER）から、1971 公開、1973 論文） | IEEE Milestone: SPICE (Simulation Program with Integrated Circuit Emphasis), 1969-1970 | 3 本<br>子：[9-22] 集積回路（IC は作ってから直せないので、作る前に計算機の中で回路を動かして確かめる必要があった（名前が『IC を重視』））<br>子：[9-23] MOSFET（トランジスタ（MOSFET など）の電気のふるまいを式（デバイスモデル）にして組み込んだ）<br>子：[10-17] CAD（計算機で設計する道具（CAD）の電子回路版） | ローレンス・ネーゲル、ドナルド・ペダーソン、ロナルド・ローラー | — | https://ethw.org/Milestones:SPICE_(Simulation_Program_with_Integrated_Circuit_Emphasis),_1969-1970 |
| C-74 | 浮遊ゲートの不揮発メモリ（EPROM → EEPROM → フラッシュメモリ） **済み：既存の [10-23] フラッシュメモリ（浮遊ゲート 1967・EPROM 1971・EEPROM を本物欄へ）（O-011）** | 技術 | 1976（1967 カーンとジー（ベル研）が浮遊ゲートを提案、1971 インテル EPROM（紫外線で消す）、1976〜78 ヒューズ社の薄い酸化膜の EEPROM、1980 東芝で発明・1984 IEDM でフラッシュ発表） | IEEE Milestone: The Floating Gate EEPROM, 1976-1978 | 3 本<br>子：[9-23] MOSFET（MOSFET のゲートの下にもう1枚、電気の通らない島（浮遊ゲート）を入れて電子を閉じ込める）<br>子：[9-15] 熱酸化（書き込み・消去は約10nm の薄い熱酸化膜を電子がトンネルで通り抜けることで行う）<br>前提：[B-10] スマホ（スマホの写真やアプリを電源を切っても覚えておく記憶はフラッシュメモリ） | ダヴ・フローマン＝ベンチコウスキー（EPROM 1971、存命かは未確認）、エリ・ハラリ（EEPROM 1976、存命）、ジョージ・パーレゴス（インテル EEPROM 1978）、舛岡富士雄（フラッシュ、存命） | ダウォン・カーンとサイモン・ジー（1967 浮遊ゲートの提案）、『フラッシュ』の名付け親は舛岡の同僚 有泉昭二（東芝による） | https://ethw.org/Milestones:The_Floating_Gate_EEPROM,_1976_-_1978 , https://www.computerhistory.org/storageengine/reusable-semiconductor-rom-introduced/ , https://shmj.or.jp/english/pdf/ic/exhibi741E.pdf , https://en.wikipedia.org/wiki/Fujio_Masuoka |
| C-75 | 公開鍵暗号 **済み：[15-30]（考えた人の多くが存命なので方法の名前だけ）（O-011）** | 技術 | 1976（1969〜75 英 GCHQ で秘密に発明（1997 まで非公開）、1976 ディフィーとヘルマンの鍵交換、1977 RSA） | IEEE Milestone: Invention of Public-key Cryptography, 1969-1975・ACMチューリング賞 2015（ディフィーとヘルマン）・ACMチューリング賞 2002（リヴェスト・シャミア・エーデルマン） | 3 本<br>子：[15-15] コンピュータネットワーク（会ったことのない相手と網の上で秘密に話すには、先に鍵を渡す方法が要った）<br>子：[15-14] 情報理論（シャノンの情報理論（1949 の秘密通信の理論）が暗号を数学にした）<br>前提：[B-10] スマホ（スマホの通信・支払いの暗号（HTTPS など）は公開鍵で鍵を決める） | ホイットフィールド・ディフィー、マーティン・ヘルマン（存命）、ロナルド・リヴェスト、アディ・シャミア、レオナルド・エーデルマン（存命） | ジェームズ・エリス（1969 考え）、クリフォード・コックス（1974 方法、RSA と同じ形）、マルコム・ウィリアムソン（1975、ディフィー＝ヘルマンと同じ鍵交換）は GCHQ で秘密だったためチューリング賞なし（IEEE Milestone が功績を記録）。ラルフ・マークル（存命、公開鍵の考えを独立に） | https://ethw.org/Milestones:Invention_of_Public-key_Cryptography,_1969_-_1975 , https://www.gchq.gov.uk/person/malcolm-williamson , https://en.wikipedia.org/wiki/Turing_Award |
| C-76 | 導電性高分子（電気を通すプラスチック） **済み：[2-55]（存命の受賞者は名前を出さない）（O-011）** | 発見 | 1977（白川がポリアセチレン膜 1967〜（触媒を誤って1000倍）、ヨウ素ドーピングで金属並みの導電 1977） | ノーベル化学賞 2000（ヒーガー、マクダイアミッド、白川英樹） | 3 本<br>子：[B-14] アセチレンランプ（原料のアセチレンはカーバイド（炭化カルシウム）から）<br>前提：[4-15] リチウムイオン電池（吉野彰のリチウムイオン電池研究は 1981〜83 年のポリアセチレン負極から始まった（後に炭素へ。最終製品には使われない）<br>理解される：[9-11] ドーパント源（高分子への『ドーピング』も、半導体のドーピングと同じく電荷の運び手を足す考え方） | 白川英樹（存命）、ヒーガー（存命）、マクダイアミッド | — | https://www.nobelprize.org/prizes/chemistry/2000/summary/ , https://www.wipo.int/wipo_magazine/en/2020/03/article_0004.html |
| C-77 | 薄膜トランジスタ（TFT：アモルファスSi・IGZO） **済み：既存の [B-19] TFT 液晶（IGZO 2004 を本物欄へ）（O-011）** | 技術 | 1979（1962 最初の TFT（CdS）、1979 a-Si TFT、2004 透明アモルファス IGZO TFT） | 未来技術遺産 00377（フレキシブル透明アモルファス IGZO 薄膜トランジスタ、2024） | 3 本<br>子：[9-23] MOSFET（ゲートの電圧で膜の電流を切り替える＝MOSFET と同じ電界効果を、ガラスの上の薄い膜で）<br>子：[9-30] エピタキシーと CVD（a-Si や IGZO の膜は CVD・スパッタで広いガラスに付ける）<br>前提：[B-10] スマホ（画面の1画素ずつにスイッチ（アクティブマトリクス）） | ワイマー（RCA、1962）、スピアとルカンバー（ダンディー大、1979 a-Si）、細野秀雄（存命）・野村研二ら（東工大、2004 IGZO） | — | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Thin-film_transistor |
| C-78 | RISC（命令を単純にした CPU の設計） **済み：[10-36]（O-011）** | 技術 | 1981（1975〜80 IBM 801（コック）、1980〜82 バークレーの RISC-I（1981 完成）、同じころスタンフォードの MIPS） | IEEE Milestone: First RISC (Reduced Instruction-Set Computing) Microprocessor, 1980-1982・ACMチューリング賞 2017（ヘネシーとパターソン）・ACMチューリング賞 1987（ジョン・コック） | 3 本<br>子：[10-13] マイクロプロセッサ（マイクロプロセッサの命令を減らして単純にし、空いたトランジスタをレジスタとパイプラインに回した）<br>子：[10-18] コンパイラ（複雑な命令は人ではなくコンパイラが単純な命令の組み合わせで作る、という前提の設計）<br>前提：[B-10] スマホ（スマホの CPU（ARM）は RISC の流れ。IEEE の銘板もゲーム機・電話・タブレットへの影響を書く） | デイヴィッド・パターソン、カルロ・セカン（バークレー、存命）、ジョン・ヘネシー（スタンフォード MIPS、存命）、ジョン・コック（IBM 801） | RISC-I を設計した大学院生たち（IEEE の銘板は『学生が設計し作った』と書く） | https://ethw.org/Milestones:First_RISC_(Reduced_Instruction-Set_Computing)_Microprocessor_1980-1982 , https://en.wikipedia.org/wiki/Turing_Award |
| C-79 | 量子ドット（大きさで色が変わる半導体の粒） **済み：[I-47]（受賞者は存命なので名前を出さない）（O-011）** | 発見 | 1981（エキモフ 1981 ガラス中の CuCl 微粒子、エフロスの理論 1982、ブルース 1983 溶液中、バウェンディ 1993 合成法） | ノーベル化学賞 2023（バウェンディ、ブルース、エキモフ、いずれも存命） | 3 本<br>子：[2-14] ソーダ石灰ガラス（最初の量子ドットは色ガラスの中の微粒子（エキモフ））<br>子：[7-01] 光学ガラス（色ガラスフィルター（CdS・CdSe 入り）の色の研究から）<br>子：[B-07] LED（QLED テレビは青色 LED の光を量子ドットで赤・緑に変える） | エキモフ（存命）、ブルース（存命）、バウェンディ（存命） | アレクサンドル・エフロス（存命。1982 に量子サイズ効果の理論、受賞なし ⚠️ 出典は二次資料） | https://www.nobelprize.org/prizes/chemistry/2023/summary/ , https://www.soci.org/news/2023/10/quantum-dot-pioneers-claim-2023-nobel-prize-in-chemistry , https://en.wikipedia.org/wiki/Quantum_dot |
| C-80 | クライオ電子顕微鏡 **済み：[8-38]（同上）（O-011）** | 技術 | 1984（ヘンダーソン 1975・1990、フランクの画像解析 1975〜86、デュボシェのガラス状の氷 1982〜84） | ノーベル化学賞 2017（デュボシェ、フランク、ヘンダーソン、いずれも存命） | 3 本<br>子：[8-11] 電子顕微鏡（電子顕微鏡で凍らせた試料を見る）<br>子：[5-09] クライオポンプ → 液体窒素コールドトラップ（液体窒素で冷やした液体エタンで一瞬で凍らせ、液体窒素温度で観察）<br>子：[10-13] マイクロプロセッサ（何十万枚の像を計算機で平均して立体に組む） | デュボシェ（存命）、フランク（存命）、ヘンダーソン（存命） | ルスカ（電子顕微鏡の発明者、物理学賞 1986） | https://www.nobelprize.org/prizes/chemistry/2017/summary/ |
| C-81 | タンパク質の形を AI で予測（AlphaFold） **済み：[10-37]（同上）（O-011）** | 技術 | 2020（CASP 1994〜、AlphaFold 2018、AlphaFold2 2020、Baker の設計 2003〜） | ノーベル化学賞 2024（ベイカー、ハサビス、ジャンパー、いずれも存命） | 3 本<br>子：[10-16] ニューラルネット（深層ニューラルネット（注意機構）で学習）<br>子：[B-11] AIチップ（学習は AI チップ（TPU）の上で）<br>子：[10-13] マイクロプロセッサ（計算機なしにはできない） | ハサビス（存命）、ジャンパー（存命）、ベイカー（存命、タンパク質の設計） | アンフィンセン（1972 化学賞：アミノ酸の並びが形を決める＝予測の前提）、ジョン・モールト（存命。予測コンテスト CASP の創設、受賞なし）、PDB に構造を登録した世界中の結晶学者 | https://www.nobelprize.org/prizes/chemistry/2024/summary/ , https://www.nobelprize.org/prizes/chemistry/1972/summary/ |
| C-82 | 水車タービン（フランシス・ペルトン・カプラン） **済み：[4-33]（実装済みの [4-01] への線は引かず本物欄で）（O-011）** | 技術 | 1827（1827 フルネロン、1849 フランシス、1880ごろ ペルトン、1913 カプランの特許・1929 米国で稼働） | ASME Landmark #157（Pelton Waterwheel Collection）・ASME Landmark #57（Kaplan Turbine）・ASME Landmark #5（Boyden Hydraulic Turbines） | 2 本<br>前提：[4-01] 水車発電機（水力発電は昔の水車でなく速く回るタービン水車で発電機を回す）<br>理解される：[4-06] 蒸気タービン（羽根に流体を当てて回す同じ考え（速度三角形）） | ブノワ・フルネロン、ジェームズ・フランシス、レスター・ペルトン、ヴィクトール・カプラン | — | https://www.asme.org/about-asme/engineering-history/landmarks/57-Kaplan-Turbine , https://www.asme.org/about-asme/engineering-history/landmarks/157-Pelton-Waterwheel-Collection , https://en.wikipedia.org/wiki/Water_turbine |
| C-83 | ガラスの自動成形機（製びん機・リボンマシン） **済み：[2-56]（O-011）** | 技術 | 1903（1903 オーエンスの全自動製びん機（AR 型 1912）、1926 コーニングのリボンマシン） | ASME Landmark #86（Owens AR Bottle Machine）・ASME Landmark #81（Corning Ribbon Machine） | 2 本<br>子：[2-14] ソーダ石灰ガラス（溶けたソーダ石灰ガラスを機械で吸い上げ・型で吹く（手吹きを置きかえ））<br>前提：[B-01] 白熱電球（電球のガラス球を毎分2000個（リボンマシン）＝電球が安くなった） | マイケル・オーエンス（資金はエドワード・リビー）、ウィリアム・ウッズとデビッド・グレイ（コーニング） | — | https://www.asme.org/about-asme/engineering-history/landmarks/86-Owens-AR-Bottle-Machine , https://www.asme.org/about-asme/engineering-history/landmarks/81-corning-ribbon-machine |
| C-84 | オンサーガーの相反定理（熱と電気の『あいこ』） **済み：[I-48]（O-011）** | 発見 | 1931（ケルビンの熱電関係式 1854、オンサーガー 1931） | ノーベル化学賞 1968（オンサーガー） | 2 本<br>説明する：[3-09] 熱電対（熱電対の起電力（ゼーベック）と電流で熱を運ぶ効果（ペルチェ）が同じ係数でつながる理由）<br>理解される：[I-02] 熱力学（平衡から少しずれた熱力学（非平衡熱力学）） | オンサーガー | ケルビン卿（1854 ゼーベック係数とペルチェ係数の関係を予想。ノーベル賞創設前） | https://www.nobelprize.org/prizes/chemistry/1968/summary/ |
| C-85 | 回転ヘッド式ビデオテープ（VTR・VHS） **済み：[15-31]（O-011）** | 技術 | 1956（1956 アンペックス VRX-1000、1959 ヘリカルスキャン試作、1976 VHS） | 未来技術遺産 00020（VHS 方式家庭用ビデオ HR-3300）・00058（2インチビデオテープ試作品） | 2 本<br>子：[15-12] 磁気記録（磁気記録のヘッドを回して、テープを速く動かさずに映像の速い信号を書く）<br>子：[15-11] テレビ（テレビ番組を録って後で見る） | アンペックス（チャールズ・ギンズバーグ、レイ・ドルビーら）、沢崎憲一（東芝、ヘリカルスキャン）、高野鎮雄・白石勇磨（日本ビクター、VHS） | — | https://ja.wikipedia.org/wiki/%E9%87%8D%E8%A6%81%E7%A7%91%E5%AD%A6%E6%8A%80%E8%A1%93%E5%8F%B2%E8%B3%87%E6%96%99 , https://en.wikipedia.org/wiki/Videotape , https://en.wikipedia.org/wiki/VHS |
| C-86 | 人工知能という分野（ダートマス会議と記号処理の AI） **済み：[I-49]（O-011）** | 発見 | 1956（1955年8月 提案書（『人工知能』という言葉の始まり）、1956年夏 ダートマスの研究会、1955〜56 ロジック・セオリスト） | ACMチューリング賞 1969（ミンスキー）・ACMチューリング賞 1971（マッカーシー）・ACMチューリング賞 1975（ニューウェルとサイモン） | 2 本<br>子：[10-07] 真空管フリップフロップと計算機（電子計算機が無いと『考える機械』を試せない（ロジック・セオリストは JOHNNIAC で動いた））<br>子：[10-05] ブール代数とスイッチ回路（記号の論理で定理を証明する。ブール代数・論理の機械化の続き） | ジョン・マッカーシー、マーヴィン・ミンスキー、ナサニエル・ロチェスター、クロード・シャノン（提案者）、アレン・ニューウェル、ハーバート・サイモン | クリフ・ショー（ロジック・セオリストのプログラムを書いた。チューリング賞は受けていない） | https://ojs.aaai.org/index.php/aimagazine/article/view/1904 , https://en.wikipedia.org/wiki/Dartmouth_workshop , https://en.wikipedia.org/wiki/Turing_Award |
| C-87 | ムーアの法則 **済み：[I-50]（名付けた人は存命なので名前を出さない）（O-011）** | 発見 | 1965（1965 論文（毎年2倍）、1975 2年で2倍に修正） | IEEE Milestone: Moore's Law, 1965 | 2 本<br>子：[9-22] 集積回路（集積回路の部品数が毎年2倍になる、という IC 数年分の実績から出た見通し）<br>子：[9-18] プレーナ技術（プレーナ技術で同じ写真の工程を繰り返せるから、小さくするほど1個あたりが安くなった） | ゴードン・ムーア（フェアチャイルド） | カーヴァー・ミード（『ムーアの法則』と名付けた） | https://ethw.org/Milestones:List_of_IEEE_Milestones , https://en.wikipedia.org/wiki/Moore%27s_law |
| C-88 | 強化学習（ごほうびから学ぶ AI） **済み：既存の [10-28] 強化学習（S-005。ベルマン 1957・Q 学習 1989・1992 のバックギャモンを本物欄へ）（O-011）** | 技術 | 1983（1980年代から一連の論文（1983 アクター・クリティック、1988 TD 学習）、1998 教科書『強化学習』） | ACMチューリング賞 2024（バートーとサットン、2025年3月発表） | 2 本<br>子：[10-04] フィードバック制御（最適制御（どう操作すれば一番よいか）の数学を、モデルが分からなくても試しながら解くようにした）<br>子：[10-16] ニューラルネット（学んだ価値や方策をニューラルネットで表す（ACM の授賞理由にある）） | アンドリュー・バートー（存命）、リチャード・サットン（存命） | リチャード・ベルマン（1957 動的計画法）、ハリー・クロップ（ごほうびで学ぶ機械の考え）、クリス・ワトキンス（1989 Q 学習）、ジェラルド・テサウロ（1992 TD-Gammon） | https://awards.acm.org/about/2024-turing , https://www.acm.org/media-center/2025/march/turing-award-2024 |

## 網羅チェックの候補：製鉄・金属加工（S-001、2026-10-20）
鉄鋼・非鉄金属・金属加工の教科書的な工程の一覧（焼結・高炉・直接還元・パドル・平炉・転炉・電炉・二次精錬・連続鋳造・圧延・鍛造・伸線・押出し・熱処理・溶接・粉末冶金・めっき・電解精錬・クロール法など）と、ASME ランドマーク・ノーベル化学賞などを突き合わせた。**既存ノードとつながるのに、まだ入っていないものだけ**。つながりの本数の多い順。足していない（実装担当・ユーザーが決める）
- 今回のノード追加（[1-48]〜[1-52]・[4-17]）と、既に提案済みの物（蒸気ハンマー C-48・インバー C-04・放電加工 C-61・研削と焼入れ N-10）は除いた。「熱処理」は N-10 と一部重なる
- 注意：年・人物は Wikipedia などで知られた事実が中心で、全部を公式の資料で確かめたわけではない（はっきりしない物は ⚠️）。未来技術遺産・IEEE マイルストーンには金属関係の確かな登録が見つからなかった

| # | 候補 | 種類 | 年 | どのリスト | つながる既存ノード（本数） | 人物 | 受賞・登録されなかった貢献者 | 出典 |
|---|---|---|---|---|---|---|---|---|
| M-01 | 伸線（ダイスで線を引く） **済み：[1-58]（[15-03] 海底ケーブルの前提に）（O-012）** | 技術 | 1350（古代は手引き。14世紀ニュルンベルクで水車駆動の伸線、1819 ダイヤモンドダイス⚠️、1893 ローブリングの80トン撚り線機） | ASME Landmark #139 Roebling 80-ton Wire Rope Machine（1893） | 6 本<br>子：[4-03] 銅ケーブル（銅ケーブルは銅の棒をダイスで引いて作る）<br>子：[1-28] タングステン線（クーリッジの延性W線は焼結棒を熱間で引き延ばして作る）<br>子：[9-33] ワイヤーボンディング（ワイヤーボンディングの直径約25µmの金線・アルミ線）<br>子：[15-03] 海底ケーブル（海底ケーブルの銅心線と鋼線の外装）<br>子：[12-10] 橋（鋼線を束ねた吊り橋のケーブル（ブルックリン橋 1883））<br>前提：[1-08] 鍛鉄（最初の鉄線は延びる錬鉄から引いた） | ジョン・A・ローブリング（ワイヤロープ・吊り橋） | — | https://en.wikipedia.org/wiki/Wire_drawing , https://www.asme.org/about-asme/engineering-history/landmarks |
| M-02 | 鉄−炭素状態図と金属組織学（顕微鏡で鋼を見る） **済み：[I-51]（O-012）** | 発見 | 1863（1863 ソービーが磨いて腐食した鋼を顕微鏡で観察、1897 ロバーツ＝オーステンが Fe-C 状態図、1900 バッキウス・ローズボームが相律で整理） | 工程の一覧 | 6 本<br>説明する：[1-26] 鋼（炭素0.1〜2%が『鋼』になる理由（オーステナイト→パーライト/マルテンサイト））<br>説明する：[1-09] 高炉（C約4%で融点が約1150℃に下がる（共晶点）から高炉の鉄が液体で出る）<br>説明する：[1-08] 鍛鉄（錬鉄（C<0.1%）が柔らかく、焼入れで硬くならない理由）<br>説明する：[1-11] るつぼ鋼（るつぼで炭素量をそろえると性質がそろう理由）<br>前提：[8-10] 光学顕微鏡（反射光の光学顕微鏡で研磨面を見る）<br>前提：[3-09] 熱電対（ル・シャトリエの白金熱電対で冷却曲線（変態点）を測った） | ヘンリー・クリフトン・ソービー、ウィリアム・ロバーツ＝オーステン、ヘンドリク・バッキウス・ローズボーム、アドルフ・マルテンス（マルテンサイトの名の由来） | フロリス・オスモンド（1880年代、熱電対で冷却曲線を測り鉄の変態点を見つけた） | https://en.wikipedia.org/wiki/Henry_Clifton_Sorby , https://en.wikipedia.org/wiki/William_Chandler_Roberts-Austen , https://en.wikipedia.org/wiki/Iron%E2%80%93carbon_phase_diagram |
| M-03 | アーク溶接（炭素アーク→金属棒→被覆アーク溶接棒） **済み：[8-39]（O-012）** | 技術 | 1881（1881 ベナルドス（炭素電極、パリ電気博で発表、特許1887）、1888 スラヴャノフ（金属電極）、1904〜07 シェルベリ（被覆溶接棒、ESAB）。1926 ラングミュアの原子水素溶接も） | ASME Landmark #44 Fusion-welded Test Boiler Drum（1930） | 6 本<br>理解される：[I-07] 電気アークのしくみ（電気アークのしくみ（エアトン 1899）が溶接アークの安定を説明する）<br>前提：[4-16] アーク灯（アーク灯の炭素アークをそのまま金属の溶融に使ったのが始まり）<br>前提：[4-05] 自励式発電機（大電流の直流を連続で出す自励式発電機がないとアークを保てない）<br>子：[12-04] 鉄骨構造（リベットから溶接へ。溶接鉄骨・溶接船体（リバティ船））<br>子：[5-11] 真空容器（金属の真空容器は溶接で組む（本文にも『溶接容器・溶接部のリーク』とある））<br>理解される：[3-15] アーク炉（同じ『電極と金属の間のアーク』で熱を作る兄弟技術） | ニコライ・ベナルドス、ニコライ・スラヴャノフ、オスカー・シェルベリ、アーヴィング・ラングミュア（原子水素溶接。ノーベル化学賞1932は表面化学で、溶接ではない） | スタニスワフ・オルシェフスキ（ベナルドスの共同研究者・出資者） | https://en.wikipedia.org/wiki/Arc_welding , https://en.wikipedia.org/wiki/Nikolay_Benardos , https://www.asme.org/about-asme/engineering-history/landmarks |
| M-04 | パドル法（攪錬法） **済み：[1-59]（O-012）** | 技術 | 1784（1783 圧延の特許、1784 ヘンリー・コートの反射炉での攪拌（パドル）特許。1830年代 ホールの湿式パドル） | 工程の一覧 | 5 本<br>前提：[1-09] 高炉（原料は高炉の銑鉄）<br>前提：[3-06] 反射炉（燃料と鉄を分ける反射炉で、石炭の硫黄を鉄に入れずに炭素を燃やす）<br>子：[1-08] 鍛鉄（鍛鉄（錬鉄）を大量に作る方法になった（塊鉄炉の後継））<br>子：[1-51] 圧延（コートはパドルの錬鉄を溝付きロールで圧延した）<br>子：[12-04] 鉄骨構造（エッフェル塔はパドル錬鉄） | ヘンリー・コート | ピーター・オニオンズ（同時期に似た方法の特許）、ジョセフ・ホール（1830年代の湿式パドル＝酸化鉄の炉床で収率を上げた） | https://en.wikipedia.org/wiki/Puddling_(metallurgy) , https://en.wikipedia.org/wiki/Henry_Cort |
| M-05 | 電気めっき **済み：既存の [2-12] 銅めっき（ヤコビ 1838・エルキントン 1840 を本物欄へ）（O-012）** | 技術 | 1840（1838 ヤコビの電鋳（ガルヴァノプラスティ）、1840 エルキントン兄弟がシアン化物浴の銀めっき特許（ジョン・ライトの浴）） | 工程の一覧 | 5 本<br>前提：[4-13] ボルタ電池（最初はボルタ電池の電流で金属を析出させた）<br>理解される：[6-01] 電解槽（電気分解の陰極側をそのまま使う）<br>子：[9-36] 銅配線（銅配線（IBM 1997）のダマシン法は溝に銅を電気めっきで埋める）<br>子：[1-32] 銀（銀めっき（エルキントンの銀器））<br>子：[1-40] クロム（クロムめっき（1920年代）） | モーリッツ・フォン・ヤコビ、ジョージ・エルキントンとヘンリー・エルキントン | ジョン・ライト（バーミンガムの外科医、シアン化カリウム浴を見つけた）、ルイージ・ブルニャテッリ（1805 金めっき） | https://en.wikipedia.org/wiki/Electroplating , https://en.wikipedia.org/wiki/Moritz_von_Jacobi |
| M-06 | 銅の電解精錬 **済み：[1-60]（O-012）** | 技術 | 1869（1865 ジェームズ・エルキントンの特許、1869 ウェールズ・パンブレーで最初の工場。大規模化は発電機（1870年代）以降） | 工程の一覧 | 5 本<br>子：[1-04] 銅の製錬（転炉で作った粗銅（約99%）を陽極にして 99.99% に）<br>前提：[4-03] 銅ケーブル（電線の銅は不純物が少しでもあると電気抵抗が大きく増える。電解銅が電線の標準に）<br>前提：[4-05] 自励式発電機（安い大電流の直流（ダイナモ）で初めて工業化）<br>理解される：[6-01] 電解槽（電解槽と同じ電気分解（陽極で溶け、陰極に析出））<br>子：[1-32] 銀（陽極泥から銀・金・セレン・テルルを回収） | ジェームズ・バルフォア・エルキントン | — | https://en.wikipedia.org/wiki/Copper_extraction#Electrorefining , https://en.wikipedia.org/wiki/James_Balfour_Elkington |
| M-07 | トーマス法（塩基性転炉）と塩基性内張り **済み：[1-61]（O-012）** | 技術 | 1878（1877〜78 シドニー・ギルクリスト・トーマスと従兄弟パーシー・ギルクリスト。1879 ミドルズブラで公開） | 工程の一覧 | 5 本<br>子：[3-08] 転炉（ベッセマー転炉の内張りを塩基性（ドロマイト）にした（本文に記述はあるが未ノード））<br>前提：[3-12] 耐火れんが（焼いたドロマイト・マグネシアの塩基性れんが）<br>子：[1-12] 平炉（平炉にも塩基性の炉床が使われ、リンの多い鉱石が使えるように）<br>子：[11-06] 過リン酸石灰（リンを吸ったスラグ（トーマスりん肥）が肥料に）<br>前提：[0-10] 石灰焼成（石灰を入れてリンを P2O5 としてスラグへ） | シドニー・ギルクリスト・トーマス、パーシー・ギルクリスト | — | https://en.wikipedia.org/wiki/Gilchrist%E2%80%93Thomas_process , https://en.wikipedia.org/wiki/Sidney_Gilchrist_Thomas |
| M-08 | ガス溶接・ガス切断（酸素アセチレン） **済み：[8-40]（O-012）** | 技術 | 1903（1895 ル・シャトリエがアセチレンと酸素の炎が約3100℃と報告、1903 フーシェとピカールがトーチを開発） | 工程の一覧 | 5 本<br>前提：[B-14] アセチレンランプ（カーバイドから作るアセチレン）<br>前提：[6-07] 空気分離（空気分離（リンデ 1895）で純酸素が安く手に入ったから）<br>前提：[3-15] アーク炉（炭化カルシウムはアーク炉で作る（ウィルソン 1892））<br>理解される：[7-02] 溶融石英（酸水素バーナーの兄弟。水素より高温の炎）<br>子：[12-04] 鉄骨構造（鉄骨・船の切断と解体） | エドモン・フーシェ、シャルル・ピカール、アンリ・ル・シャトリエ | — | https://en.wikipedia.org/wiki/Oxy-fuel_welding_and_cutting , https://en.wikipedia.org/wiki/Edmond_Fouch%C3%A9 |
| M-09 | ステンレス鋼 **済み：[1-62]（O-012）** | 技術 | 1913（1912 クルップのシュトラウスとマウラーがオーステナイト系（Fe-Cr-Ni）の特許、1913 ブレアリー（シェフィールド）がマルテンサイト系13Cr を『さびない鋼』として） | 工程の一覧 | 5 本<br>前提：[1-40] クロム（Cr 約12%以上で表面に薄い Cr2O3 膜ができてさびなくなる）<br>前提：[1-37] ニッケル（Ni を8%入れるとオーステナイト（磁石につかない・粘る）になる（18-8））<br>子：[5-11] 真空容器（超高真空の容器は気体が出にくいステンレス（本文にあるが未ノード））<br>子：[2-18] 超純水（超純水・薬液の配管（電解研磨したステンレス））<br>子：[1-41] チタン（クロール法の反応容器はステンレス製） | ハリー・ブレアリー、ベンノ・シュトラウス、エドゥアルト・マウラー | レオン・ギエ（1904〜06、Fe-Cr・Fe-Cr-Ni 合金の組織を研究）、フィリップ・モンナルツ（1911、Cr約12%以上で不動態化することを示した） | https://en.wikipedia.org/wiki/Stainless_steel , https://en.wikipedia.org/wiki/Harry_Brearley |
| M-10 | 転位論（金属はなぜ曲がり、たたくと硬くなるか）と無転位結晶 **済み：[I-52]（ダッシュ法の人名は生死が確かめられないので出さない）（O-012）** | 発見 | 1934（1934 テイラー・オロワン・ポラニーが独立に。1956 電子顕微鏡で直接観察（ハーシュら）。1958〜59 ダッシュのネッキングで無転位 Si） | 工程の一覧 | 5 本<br>説明する：[1-51] 圧延（冷間圧延で硬くなる（加工硬化）のは転位がからみ合うから）<br>説明する：[1-08] 鍛鉄（金属が割れずにたたいて延びる理由）<br>子：[9-08] CZ 法（CZ 法の種結晶の首を細く絞って転位を逃がす（ダッシュ法）＝無転位 Si ウェハー）<br>前提：[8-11] 電子顕微鏡（電子顕微鏡で転位が初めて直接見えた）<br>説明する：[14-13] ジュラルミン（時効硬化は析出物が転位の動きを止めるから） | G・I・テイラー、エゴン・オロワン、マイケル・ポラニー、ウィリアム・C・ダッシュ（GE、ネッキング法） | ヴィト・ヴォルテラ（1907 転位の弾性論）、ピーター・ハーシュら（1956 電子顕微鏡で初観察） | https://en.wikipedia.org/wiki/Dislocation , https://en.wikipedia.org/wiki/Dash_necking |
| M-11 | マンガン添加（フェロマンガン・スピーゲル）とハドフィールド鋼 **済み：[1-63]（[14-06] 鋼のレールの前提に）（O-012）** | 技術 | 1856（1856 マシェットがスピーゲル（Mn入り銑鉄）を加えてベッセマー鋼の脆さを解決、1882 ハドフィールドの高マンガン鋼） | 工程の一覧 | 4 本<br>前提：[1-26] 鋼（Mn が溶けた酸素と硫黄を除き、ベッセマー鋼が熱間で割れなくなった）<br>子：[14-06] 鋼のレール（鋼のレール（1857 マシェット））<br>理解される：[1-15] 金属シリコン（フェロシリコンと同じフェロアロイ（脱酸剤）。今はアーク炉で作る）<br>子：[3-15] アーク炉（フェロマンガン・フェロクロムは電気炉で作られるようになった） | ロバート・マシェット、ロバート・ハドフィールド | マシェットは特許更新料を払えず報酬を得られなかった（のちベッセマーが年金を出した） | https://en.wikipedia.org/wiki/Robert_Forester_Mushet , https://en.wikipedia.org/wiki/Mangalloy |
| M-12 | 金の製錬（アマルガム法・青化法） **済み：[1-64]（[9-33] の「金（バニラの金インゴット）」は設計の判断なので線は引かず、細線を [1-58] 伸線へ）（O-012）** | 技術 | 1887（アマルガム法は古代〜16世紀（パティオ法 1554）、1887 マッカーサー・フォレスト法（シアン化法）） | 工程の一覧 | 4 本<br>前提：[9-33] ワイヤーボンディング（ワイヤーボンディングの金線（ネットワークに『金』そのものがない））<br>前提：[1-45] 水銀（水銀アマルガムで金・銀を集める）<br>理解される：[2-22] 王水（王水が金を溶かす理由（錯イオン）はシアン化法と同じ考え）<br>理解される：[1-32] 銀（灰吹きで銀と一緒に金も取る） | ジョン・スチュワート・マッカーサー、ロバート・フォレスト、ウィリアム・フォレスト、バルトロメ・デ・メディナ（パティオ法） | — | https://en.wikipedia.org/wiki/MacArthur%E2%80%93Forrest_process , https://en.wikipedia.org/wiki/Patio_process |
| M-13 | 高速度鋼（ハイス） **済み：[1-65]（[1-28] タングステン線は年が 1910 なので前提にせず本物欄で）（O-012）** | 技術 | 1900（1868 マシェットの自硬鋼（W入り）、1898〜1900 テイラーとホワイトの熱処理、1900 パリ万博で赤熱しても切れる刃物を実演） | 工程の一覧 | 4 本<br>前提：[1-28] タングステン線（W 約18%（18-4-1 型））<br>前提：[1-40] クロム（Cr 約4%）<br>子：[8-01] 旋盤（旋盤の切削速度が数倍に。工作機械が作り直された）<br>子：[1-31] モリブデン製錬（1930年代 W の代わりに Mo を使うモリブデンハイス） | フレデリック・W・テイラー、マンセル・ホワイト | ロバート・マシェット（1868 タングステン入りの『自硬鋼』、ハイスの前身） | https://en.wikipedia.org/wiki/High-speed_steel , https://en.wikipedia.org/wiki/Robert_Forester_Mushet |
| M-14 | タンタル（延性タンタルとバリア膜） **済み：[1-66]（[9-36] 銅配線の前提に）（O-012）** | 技術 | 1903（1802 エーケベリが発見、1903 ボルトン（ジーメンス・ハルスケ）が延性タンタルを作り1905 タンタル電球） | 工程の一覧 | 4 本<br>子：[B-01] 白熱電球（炭素→タンタル→タングステンと白熱電球のフィラメントが進んだ）<br>理解される：[1-28] タングステン線（W線の直前の高融点金属フィラメント）<br>子：[9-36] 銅配線（銅配線では Ta/TaN の膜で銅がシリコンへ拡散するのを止める）<br>前提：[3-16] 真空炉（真空中で溶かして精製する） | ヴェルナー・フォン・ボルトン、アンデシュ・エーケベリ | — | https://en.wikipedia.org/wiki/Tantalum , https://en.wikipedia.org/wiki/Werner_von_Bolton |
| M-15 | 浮遊選鉱（浮選） **済み：[1-67]（O-012）** | 技術 | 1905（1877 ベッセル兄弟（黒鉛）、1898 エルモア（油浮選）、1905 ミネラルズ・セパレーション社の泡浮選（ブロークンヒル）） | 工程の一覧 | 4 本<br>前提：[1-04] 銅の製錬（低品位の黄銅鉱（Cu 1%未満）を精鉱にしないと製錬できない）<br>前提：[1-13] 亜鉛（閃亜鉛鉱と方鉛鉱を分ける）<br>前提：[1-23] 輝水鉛鉱 MoS2（輝水鉛鉱は浮選で精鉱にする）<br>前提：[1-27] 鉛（方鉛鉱の選鉱） | フランシス・エルモア、ミネラルズ・セパレーション社（サルマン、ピカード、バロット） | キャリー・エヴァーソン（1886 酸と油で硫化鉱が浮くことを見つけたとされる⚠️） | https://en.wikipedia.org/wiki/Froth_flotation |
| M-16 | 永久磁石鋼（KS鋼・MK鋼→アルニコ） **済み：[1-68]（O-012）** | 技術 | 1917（1917 本多光太郎と高木弘の KS鋼（1916 とする資料もある⚠️）、1931 三島徳七の MK鋼（Fe-Ni-Al）、1934 新KS鋼） | 工程の一覧 | 4 本<br>子：[1-47] 天然磁石（天然磁石より何倍も強い人工の磁石）<br>理解される：[1-35] ネオジム磁石（ネオジム磁石（1982 佐川眞人、存命）へ続く日本の磁石の系譜）<br>子：[4-04] ダイナモ・天然磁石（磁石発電機（マグネトー）・スピーカー・計器）<br>前提：[1-37] ニッケル（MK鋼は Ni が要る） | 本多光太郎、高木弘、三島徳七 | — | https://en.wikipedia.org/wiki/Kotaro_Honda , https://kotobank.jp/word/MK%E9%8B%BC-37425 , https://www.jpo.go.jp/introduction/rekishi/document/10hatsumeika/10mishima.pdf |
| M-17 | 浸炭・窒化（表面硬化） **済み：[1-69]（O-012）** | 技術 | 1923（浸炭は古代から。1913 マクレット（米）のアンモニア窒化特許、1923 フライ（クルップ）の窒化鋼） | 工程の一覧 | 4 本<br>前提：[6-09] ハーバー・ボッシュ法（窒化はアンモニアガスで表面に窒素を入れる）<br>理解される：[9-16] 熱拡散（表面から原子をしみ込ませる『拡散』（フィックの法則）は半導体の熱拡散と同じ）<br>子：[8-20] 玉軸受（玉軸受・歯車の表面だけ硬く、芯は粘く）<br>子：[14-09] 内燃機関（エンジンのクランク軸・カム） | アドルフ・フライ、アドルフ・マクレット | — | https://en.wikipedia.org/wiki/Nitriding , https://en.wikipedia.org/wiki/Carburizing |
| M-18 | ジルコニウム・ハフニウム（ヨウ化物法と分離） **済み：[1-70]（[9-37] FinFET の前提に）（O-012）** | 技術 | 1925（1923 コスターとヘヴェシーがX線分光でハフニウム発見、1925 ファン・アルケルとデ・ブールのヨウ化物法、1945〜 クロール法をZrに） | 工程の一覧 | 4 本<br>子：[9-37] FinFET（45nm（2007 インテル）から HfO2 の high-k ゲート絶縁膜）<br>理解される：[1-41] チタン（Zr も TiCl4 と同じくクロール法で作る）<br>理解される：[1-28] タングステン線（ヨウ化物法は熱したW線の上に金属を析出させる）<br>前提：[2-17] イオン交換（Zr と Hf は性質が似すぎて、溶媒抽出・イオン交換でないと分けられない） | ディルク・コスター、ゲオルク・ド・ヘヴェシー（ノーベル化学賞1943 は放射性トレーサーで、Hf ではない）、アントン・エドゥアルト・ファン・アルケル、ヤン・ヘンドリク・デ・ブール | — | https://en.wikipedia.org/wiki/Hafnium , https://en.wikipedia.org/wiki/Crystal_bar_process |
| M-19 | 熱処理（焼入れ・焼戻し・焼なまし）とマルテンサイト変態 **済み：[1-71]（O-012）** | 技術 | 1930（焼入れは古代から。1890年代 マルテンス・オスモンドが組織を命名、1930 ベインとダヴェンポートの恒温変態（TTT）図） | 工程の一覧 | 4 本<br>子：[8-05] ゲージブロック（ゲージブロックは焼入れ後に寸法を安定させる処理が要る）<br>子：[1-11] るつぼ鋼（るつぼ鋼の刃物・ばね）<br>理解される：[9-29] アニール（焼なまし＝熱で原子を動かして欠陥を直す、のは半導体のアニールと同じ考え）<br>理解される：[14-13] ジュラルミン（焼入れ後の時効も同じ熱処理の考え方） | エドガー・ベイン、エドマンド・ダヴェンポート、フロリス・オスモンド | — | https://en.wikipedia.org/wiki/Quenching , https://en.wikipedia.org/wiki/Isothermal_transformation_diagram |
| M-20 | 粉末冶金と熱間静水圧プレス（HIP） **済み：[1-72]（[1-35] ネオジム磁石の前提に）（O-012）** | 技術 | 1955（1820年代 ウォラストンの白金、1910 クーリッジのW、1955 バテル研究所の HIP） | ASME Landmark #103 First Hot Isostatic Processing Vessels（1955） | 4 本<br>理解される：[1-28] タングステン線（W 線は粉を固めて焼く（本文の『粉末冶金』））<br>理解される：[1-31] モリブデン製錬（Mo も同じ焼結）<br>理解される：[1-18] 白金精製（ウォラストンの白金）<br>子：[1-35] ネオジム磁石（ネオジム磁石は焼結で作る） | ウィリアム・ハイド・ウォラストン、ウィリアム・D・クーリッジ、バテル研究所（ソー、ウォーレン、ボイヤー） | — | https://en.wikipedia.org/wiki/Powder_metallurgy , https://en.wikipedia.org/wiki/Hot_isostatic_pressing , https://www.asme.org/about-asme/engineering-history/landmarks |
| M-21 | めっき鋼板（トタン・ブリキ） **済み：[1-73]（O-012）** | 技術 | 1837（ブリキは17世紀ボヘミア・1720年代ウェールズ、1837 ソレルの溶融亜鉛めっき特許） | 工程の一覧 | 3 本<br>前提：[1-13] 亜鉛（亜鉛が先にさびて鉄を守る（犠牲防食））<br>前提：[1-30] 錫（ブリキは鋼板に錫）<br>子：[11-12] 缶詰（缶詰のブリキ缶（本文に記述あり）） | スタニスラス・ソレル | — | https://en.wikipedia.org/wiki/Hot-dip_galvanization , https://en.wikipedia.org/wiki/Tinplate |
| M-22 | 超硬合金（炭化タングステン） **済み：[1-74]（O-012）** | 技術 | 1923（1923 シュレーター（オスラム）が WC-Co の焼結、1926 クルップが『ヴィディア』として発売） | 工程の一覧 | 3 本<br>前提：[1-28] タングステン線（電球のW線を作るための W 粉末冶金・線引きダイスの研究から生まれた）<br>子：[8-01] 旋盤（旋盤の刃物（ハイスよりさらに高速））<br>理解される：[3-15] アーク炉（炭化物（SiC・CaC2）をアーク炉で作る仲間） | カール・シュレーター、ハインリッヒ・バウムハウアー（オスラム） | — | https://en.wikipedia.org/wiki/Cemented_carbide , https://en.wikipedia.org/wiki/Tungsten_carbide |
| M-23 | 鍛造と水圧鍛造プレス **済み：[8-41]（O-012）** | 技術 | 1954（1795 ブラマーの水圧プレス、1893 ベスレヘムの1万4千トン、1954〜55 米空軍『重プレス計画』の5万トン（アルコア、ワイマン・ゴードン）） | ASME Landmark #71 ALCOA 50,000-ton Hydraulic Forging Press（1954）・ASME Landmark #89 Wyman-Gordon 50,000-ton Hydraulic Forging Press（1955） | 3 本<br>前提：[1-29] アルミニウム（アルミ合金の大型型鍛造部品）<br>子：[14-12] 飛行機（ジェット機の主翼桁・脚を一体鍛造）<br>子：[4-06] 蒸気タービン（タービンの軸・円板は大型鍛造品） | ジョセフ・ブラマー | — | https://en.wikipedia.org/wiki/Heavy_Press_Program , https://www.asme.org/about-asme/engineering-history/landmarks |
| M-24 | 真空脱ガス（二次精錬） **済み：[1-75]（O-012）** | 技術 | 1959（1956 DH 法（ドルトムント・ヘルデ）、1959 RH 法（ルールシュタール・ヘレウス）） | 工程の一覧 | 3 本<br>子：[1-26] 鋼（溶けた鋼から水素・窒素を真空で抜く（水素割れを防ぐ））<br>理解される：[3-16] 真空炉（真空で気体が出る反応を進める同じ考え）<br>前提：[5-04] 油回転ポンプ（大量の気体を抜く真空ポンプ（実際は蒸気エジェクタ中心⚠️）） | ルールシュタール社とヘレウス社 | — | https://en.wikipedia.org/wiki/Vacuum_degassing , https://en.wikipedia.org/wiki/Secondary_steelmaking |
| M-25 | 直接還元製鉄（ガス・水素で鉄鉱石を還元） **済み：[1-76]（O-012）** | 技術 | 1969（1957 HYL 法（メキシコ）、1969 ミドレックス法。2020年代 水素直接還元の試験） | 工程の一覧 | 3 本<br>理解される：[1-09] 高炉（高炉を使わず、溶かさずに固体のまま還元（塊鉄炉と同じ考え））<br>前提：[6-01] 電解槽（水素直接還元は電気分解の水素を使う）<br>子：[3-15] アーク炉（還元鉄をアーク炉で溶かして鋼に） | HYLSA 社、ミドレックス社 | — | https://en.wikipedia.org/wiki/Direct_reduced_iron |

## CC0 の点検：丸写しの検査（NET-08 の 4・O-002、2026-10-20）
- 突き合わせた物：日本語版 Wikipedia（ノードの名前 390 件で検索 → 記事 223 本）・英語版 Wikipedia（リポジトリの出典に出てくる記事 182 件 → 本文が取れた 128 本）と、自分たちの文（TECH_TREE・products・ideas の本物欄とへえ①〜④、REFERENCES の各項目、lang の部品の文：日本語 1511・英語 815）
- 基準：日本語は空白を除いて 30 字以上（数字・記号・化学式だけの一致は数えない）、英語は 10 語以上の連続した一致
- **結果：一致は 0 件**。丸写しは見つからなかった。検査のしかたが正しく動くことは、記事の文をわざと混ぜた文で確かめた（日本語・英語とも検出できた）
- 限界：Wikipedia 以外（本・論文・博物館のページ）とは比べていない。翻訳した文や言い換えは、この方法では見つからない
| # | ノード | ファイル | 一致した文の頭10字 | 元の記事 | 状態 |
|---|---|---|---|---|---|
| — | （一致なし） | — | — | — | — |

## 今日足した物の自己監査（O-013、2026-10-20）
S-001〜O-012 で足した 203 項目のうち、出典が1つだけ・Wikipedia だけの 62 項目に、Wikipedia 以外の確かな出典（学会・公的機関・博物館・大学・ノーベル財団など）を1つずつ当てた。新しい出典は各ノードの出典に足した。
注意：調べ係の報告では、一部の出典は覚えている事実をもとに名前を書いたもので、ページを開いて確かめていない（ok の13件ほど）。次の監査でリンクが生きているか確かめる

| # | ノード | 結果 | 内容 |
|---|---|---|---|
| A-01 | [1-64] | 直した | パティオ法は銀の製錬法（塩・マジストラル＝硫酸銅を使う）。金のアマルガム法とは別；年は1554・1556・1557の説がある |
| A-02 | [8-38] | 直した | ノーベル財団の発表では像の処理法の開発は1975〜1986年（1990年は別の受賞者が原子に近い分解能の像を出した年） |
| A-03 | [I-18] | 直した | Coandă-1910 の試験飛行の話は本人の後年の証言で、内容が年ごとに変わり、当時の資料と合わない |
| A-04 | [1-69] | 直した | Pye（ASM）によるとマクレットの出願は1908年、特許が認められたのが1913年 |
| A-05 | [1-73] | 直した | ブリキはボヘミアの独占（13〜14世紀から）で、17世紀（1620年ごろ）はザクセンへ広まった時期（1911年ブリタニカ） |
| A-06 | [7-24] | 直した | 1978年のポラロイド SX-70 ソナー（折りたたみ式のインスタント一眼レフ、超音波 AF）がもっと早い。ME F は「35ミリ一眼レフで最初」 |
| A-07 | [P-14] | 直した | 今の DRAM の書きなおし（リフレッシュ）の間隔は標準で 64ミリ秒＝1秒に約16回（高温時 32ミリ秒で約31回）。「何十回」は少し多い。本物欄・へえ①の両方 |
| A-08 | [2-52] | 直した | B-H-B の三中心結合はリプスコム本人のノーベル講演でもロンゲ＝ヒギンズ（1943年、ベルと）が最初に示したとしている。リプスコムの功績はそれを B-B-B 結合などに広げ多くのボランの構造を解いたこと |
| A-09 | [I-14] | 直した | 1841年は論文での提案の年。国中に広まったのは鉄道会社の採用のあとで、正式な British Standard になるのはさらに後（1881年ごろ）。「1841年に初めてそろった」は言い過ぎ |
| A-10 | [I-48] | 直した | 1854年の時点ではまだウィリアム・トムソン。ケルビン卿になるのは1892年 |
| A-11 | [4-25] | ⚠️ | クリエジェ車の回生（1904年の実車に回生ブレーキの位置あり、博物館で確認）。1894/1897年の開始年と、レイワースの『1903年の特許』は確かな出典で確かめられない（Grace's Guide では1904年に Raworth Traction Patents 社を登録、ロ |
| A-12 | [P-19] | ⚠️ | 年 1980 を確かな出典で確かめられない。確かめられたのは1980年代後半の Motorola の特許まで。年に ⚠️ を付けるべき |
| A-13 | [1-50] | ⚠️ | ユングハンスの1930年代の研究と、鋼での工業化が1950年代（CONCAST）なのは確か。最初の鋼の工場の名前と年は確かな出典で確かめられなかった（1950年ごろのユングハンスの試験機、1950年代前半の英・加・独の工場など説が分かれる）。今の ⚠️ はそのまま残す |
| A-14 | 存命の人の名前 | 直した | 今日の追加の本文から 18 か所の名前を外した（O-013 第1弾）。実装側の [B-07]・[10-19] は報告だけ |
| A-15 | つながり | そのまま | 自動チェック（--check のつながりの検査）で無い番号は [B-05] 本文の [7-08] の1件だけ（以前からの物）。同じ物の候補 24 組は技術と部品のモノの組など意図した物。年の逆転 87 件は増えていない |
| A-16 | ok の 49 項目 | そのまま | [1-54] [1-61] [1-67] [1-71] [1-75] [2-56] [15-30] [10-35] [I-36] [I-43] [I-51] [P-07] [P-10] [P-13] [P-16] [P-22] [4-28] [1-59] [1-62] [1-65] [1-76] [8-40] [10-32] [10-37] [I-30] [I-38] [I-46] [I-52] [P-08] [P-11] [P-17] [P-20] [P-23] [1-60] [1-63] [1-66] [1-70] [1-74] [8-27] [9-49] [10-34] [I-32] [I-41] [P-06] [P-09] [P-12] [P-15] [P-18] [P-21] |

### 出典のリンクの確認（O-014、2026-10-20）
今日足した物の出典 URL 186 本を自動で開いた：開けた 168・403 13・404 8（うち3本は自分の抜き出しの切れ目で、本当は生きていた）・接続できない 6。ページの中に年があるかの自動の確かめは、ページが JavaScript や PDF のことが多く当てにならないので使わなかった

| # | ノード | 結果 | 内容 |
|---|---|---|---|
| A-17 | [1-69] [1-70] | 直した | ASM International のページが 404 → URL を外した（[1-69] は本の名前を残す）。ほかの出典はそのまま |
| A-18 | [I-52] | 直した | Wikipedia「Dash necking」が 404 → 「Necking (engineering)」へ（インドの学術誌の論文はそのまま） |
| A-19 | [P-21] | 直した | gps.gov/systems/gps/ が 404 → gps.gov のトップへ |
| A-20 | [P-25] | 直した | The Henry Ford の資料ページが 404 → IEEE Spectrum「Unimation robot」へ |
| A-21 | [1-67] [1-76] [I-13] [I-51] | 直した | 出典の URL が出版社・団体・目次のトップページだけで、年や人を直接は支えていなかった → URL を外した（本の名前は残す）。[I-51] は Wikipedia だけになったので年に ⚠️ |
| A-22 | 13 か所（Britannica・ACM・Smithsonian・Analog Devices・Horton など） | そのまま | 403（自動の取得を断るサイト）。人がブラウザで開けば読めるはずで、自動では確かめられない |
| A-23 | 6 か所（deutsche-biographie・Arm・Princeton・Virginia 大・Intel・Elevator Museum） | そのまま | 自動では接続できなかった（証明書・接続の制限）。回り道はしていない |
| A-24 | 生成器の URL の切れ | 直した | tools/gen_tree_views.py が出典の URL をカンマとかっこで切っていた（IEEE マイルストーンの「,_1911」・Wikipedia の「(computer)」など）→ 切らないように直した。node.html のリンクが正しくなる |
| A-25 | URL の形の自動チェック | 足した | --check に「odd urls」（日本語の文字・全角かっこが混ざった URL）を足した（オフライン）。今は 0 件 |

## 人物の偏りの点検（O-015、2026-10-20。NET-02・DECISIONS_PENDING Q26-1 の続き）
今日（S-001〜O-014）でノード・発見・モノは約 330 から 604 に増えたが、人物カードは 60 人のまま増えていなかった（人物のいるノードの割合が下がっていた）。今日のノードの本物欄に名前のある、亡くなったことが確かめられる人を足した。存命の人・生死が確かめられない人（例：ナタリア・ラザレンコ）は足さない。数を合わせるための人物は作らない

| | 前 | 後 |
|---|---|---|
| 人物 | 60 | 73 |
| 女性 | 2 | 6 |
| 西洋以外 | 12 | 21 |

| # | 人 | ノード | 結果 |
|---|---|---|---|
| H-01 | マリー・キュリー | [I-40] 放射能 | 足した（女性） |
| H-02 | グレース・ホッパー | [10-18] コンパイラ | 足した（女性。NET-02 の提案 W-4 がノードができて実現） |
| H-03 | リン・コンウェイ | [10-19] VLSI 設計法 | 足した（女性。W-5） |
| H-04 | キャサリン・ブロジェット | [7-22] 反射防止膜 | 足した（女性。W-3） |
| H-05 | 加藤与五郎・武井武 | [1-53] フェライト | 足した |
| H-06 | 宇田新太郎・八木秀次 | [15-27] 八木・宇田アンテナ | 足した |
| H-07 | 本多光太郎・三島徳七 | [1-68] 永久磁石鋼 | 足した |
| H-08 | 菊池正士 | [I-44] 電子回折 | 足した |
| H-09 | 林厳雄 | [9-48] 半導体ヘテロ構造 | 足した |
| H-10 | 加藤一郎 | [P-24] 人型ロボット（WABOT-1） | 足した |
| H-11 | ナタリア・ラザレンコ | [8-37] 放電加工 | 足さない（生死が出典で確かめられない） |
| H-12 | パーキンス [3-22]・ミジリー [2-48]・ウェンテ [15-23]・オーチス [12-16]・テスラとフェラリス [4-22]・ショールズ [8-26]・ホイットル [4-29]・カロザース [2-51]・ガボール [7-26]・ハミング [15-29]・ダレン [B-23]・ローレンス [9-47]・ナスミス [1-56]・コート [1-59]・ラムスデン [8-42]・ラザフォード [I-40]・オンサーガー [I-48] ほか | 今日のノード | 済み（O-016）：18人を人物カードに（ウェンテ・ガボール・ハミングほか全員、亡くなったことを確認） |

## 今日足した物の監査の準備（O-020 ⑥、2026-10-20）
S-001〜O-012 で足した物のうち、⚠️ が残る物（△）と、出典が1つだけ・Wikipedia だけの物（○）。AUDIT_QUEUE.md に入る

| # | ノード | 主張 | 判定 | 結果 | 出典 |
|---|---|---|---|---|---|
| V-170 | [4-17] ステッピングモーター | 年 1919（1919〜1950） ⚠️ | △ | ⚠️ が残る：### [4-17] ステッピングモーター ❓  年: 1919（1919〜1950） ⚠️ | （出典名のみ：本文の出典欄） |
| V-171 | [4-19] スイッチング電源 | 年 1970（1960年代後半〜1977） ⚠️ | △ | ⚠️ が残る：### [4-19] スイッチング電源 ❓  年: 1970（1960年代後半〜1977） ⚠️ | （出典名のみ：本文の出典欄） |
| V-172 | [4-20] 振動モーター（片寄ったおもり） | 年 1980（1970年代〜1990年代） ⚠️ | △ | ⚠️ が残る：[4-20] 振動モーター（片寄ったおもり） ❓  年: 1980（1970年代〜1990年代） ⚠️ | （出典名のみ：本文の出典欄） |
| V-173 | [4-23] 横流ファン（クロスフローファン） | 年 1893（1891〜1893） ⚠️ | △ | ⚠️ が残る：### [4-23] 横流ファン（クロスフローファン） ❓  年: 1893（1891〜1893） ⚠️ | （出典名のみ：本文の出典欄） |
| V-174 | [4-25] 回生ブレーキ | 年 1897（1890年代〜1908） ⚠️ | △ | ⚠️ が残る：### [4-25] 回生ブレーキ ❓  年: 1897（1890年代〜1908） ⚠️（O-013 で確認：warn） | https://collection.sciencemuseumgroup.org.uk/objects/co8038670/electric-brougham-motor-car-electric |
| V-175 | [4-28] 乾電池（マンガン乾電池） | 年 1896（1866〜1896） | △ | ⚠️ が残る：日本でも屋井先蔵が1887年ごろ独自に乾電池を作ったとされる（年は資料で揺れる ⚠️）（O-013 で確認：ok） | https://www.acs.org/education/whatischemistry/landmarks/drycellbattery.html |
| V-176 | [4-31] 原子力発電 | 年 1951（1951〜1957） | △ | ⚠️ が残る：へえ①: 最初の原子力発電がともしたのは、4つの電球だった ⚠️（O-011 で追加 | （出典名のみ：本文の出典欄） |
| V-177 | [1-50] 連続鋳造 | 年 1950（1930〜1960） ⚠️ | △ | ⚠️ が残る：### [1-50] 連続鋳造 ❓  年: 1950（1930〜1960） ⚠️（O-013 で確認：warn） | https://www.mdpi.com/2075-4701/12/5/862 |
| V-178 | [1-52] 押出し（アルミの形材） | 年 1894（1894〜1904） ⚠️ | △ | ⚠️ が残る：### [1-52] 押出し（アルミの形材） ❓  年: 1894（1894〜1904） ⚠️ | （出典名のみ：本文の出典欄） |
| V-179 | [1-57] 炭素繊維 | 年 1961（1958〜1961） | △ | ⚠️ が残る：へえ①: 炭素繊維は、鉄の4分の1ほどの重さで、鉄より強い ⚠️（O-011 で追加 | （出典名のみ：本文の出典欄） |
| V-180 | [1-58] 伸線（ダイスで線を引く） | 年 1350（古代〜1893） ⚠️ | △ | ⚠️ が残る：### [1-58] 伸線（ダイスで線を引く） ❓  年: 1350（古代〜1893） ⚠️ | （出典名のみ：本文の出典欄） |
| V-181 | [1-64] 金の製錬（アマルガム法・青化法） | 年 1887（1554〜1887） | △ | ⚠️ が残る：1554年ごろ（1554〜57年と資料で揺れる ⚠️）、メキシコのパチュ（O-013 で確認：fix） | https://miningfoundationsw.org/Bartolome_de_Medina |
| V-182 | [1-68] 永久磁石鋼（KS鋼・MK鋼） | 年 1917（1916〜1934） ⚠️ | △ | ⚠️ が残る：### [1-68] 永久磁石鋼（KS鋼・MK鋼） ❓  年: 1917（1916〜1934） ⚠️ | （出典名のみ：本文の出典欄） |
| V-183 | [1-70] ジルコニウム・ハフニウム（ヨウ化物法と分離） | 年 1925（1923〜1945） | ○ | 出典が1つだけ（O-013 で確認：ok） | （出典名のみ：本文の出典欄） |
| V-184 | [2-46] 透明な電極 ITO（酸化インジウムスズ） | 年 1954（1907〜1970年代） ⚠️ | △ | ⚠️ が残る：-46] 透明な電極 ITO（酸化インジウムスズ） ❓  年: 1954（1907〜1970年代） ⚠️ | （出典名のみ：本文の出典欄） |
| V-185 | [7-23] LiDAR（レーザーで距離を測る） | 年 1963（1963〜2007） ⚠️ | △ | ⚠️ が残る：## [7-23] LiDAR（レーザーで距離を測る） ❓  年: 1963（1963〜2007） ⚠️ | （出典名のみ：本文の出典欄） |
| V-186 | [8-40] ガス溶接・ガス切断（酸素アセチレン） | 年 1903（1895〜1903） | △ | ⚠️ が残る：へえ①: 酸素とアセチレンの炎は約3100℃で、鉄をバターのように切れる ⚠️（O-012 で追加（O-013 で確認：ok） | https://www.lindedirect.com/blogs/industrial-guides/industrial-guides/2024/01/30/a-look-at-the-history-of-welding |
| V-187 | [8-41] 水圧鍛造プレス（大型の鍛造） | 年 1954（1795〜1955） | △ | ⚠️ が残る：へえ①: 5万トンのプレスは、何階建てのビルほどの大きさがある ⚠️（O-012 で追加 | （出典名のみ：本文の出典欄） |
| V-188 | [9-46] 人を感じるセンサー（焦電型赤外線センサー） | 年 1970（1960年代〜1970年代） ⚠️ | △ | ⚠️ が残る：] 人を感じるセンサー（焦電型赤外線センサー） ❓  年: 1970（1960年代〜1970年代） ⚠️ | （出典名のみ：本文の出典欄） |
| V-189 | [15-22] アンテナ | 年 1888（1888〜1998） | △ | ⚠️ が残る：伸ばす棒のアンテナから本体の中の板（逆F アンテナ）に変わった（内蔵の最初の機種の年は資料で揺れる ⚠️） | （出典名のみ：本文の出典欄） |
| V-190 | [I-16] サプライチェーン（部品がたくさんの所から集まる） | 年 1982 ⚠️ | △ | ⚠️ が残る：### [I-16] サプライチェーン（部品がたくさんの所から集まる） ❓  年: 1982 ⚠️ | https://en.wikipedia.org/wiki/Supply_chain_management , https://supplychaindigital.com/digital-supply-chain/keith-oliver-the-man-who-gave-us-supply-chain-management |
| V-191 | [I-18] コアンダ効果（流れが曲面にくっつく） | 年 1934（1800〜1936） | △ | ⚠️ が残る：飛行機で排気が機体に引き寄せられるのに気づいたと後に語り（この飛行機が本当に飛んだかは疑われている ⚠️）、1934年にフラ（O-013 で確認：fix） | https://en.wikipedia.org/wiki/Coand%C4%83_effect , https://en.wikipedia.org/wiki/Henri_Coand%C4%83 , https://www.airspacemag.com/history-of-flight/coandas-claim-73647227/ |
| V-192 | [I-32] フランク・ヘルツの実験（電子が原子にエネルギーを渡す） | 年 1914 | ○ | 出典が1つだけ（O-013 で確認：ok） | https://www.nobelprize.org/prizes/physics/1925/summary/ |
| V-193 | [I-33] 半導体のトンネル効果（トンネルダイオード） | 年 1957 | △ | ⚠️ が残る：共同で発見した人の範囲は資料で分かれる ⚠️ | https://www.nobelprize.org/prizes/physics/1973/summary/ , https://shmj.or.jp/innovation50/english/detail_D01E.htm , https://en.wikipedia.org/wiki/Tunnel_diode |
| V-194 | [I-42] 特性X線とモーズリーの法則（元素の順番は原子番号） | 年 1913（1906〜1924） | △ | ⚠️ が残る：モーズリーは1915年にガリポリの戦いで戦死した ⚠️ | https://www.nobelprize.org/prizes/physics/1917/summary/ , https://www.nobelprize.org/prizes/physics/1924/summary/ |
| V-195 | [I-51] 鉄と炭素の状態図と金属組織学（顕微鏡で鋼を見る） | 年 1863（1863〜1900） ⚠️ | △ | ⚠️ が残る：1] 鉄と炭素の状態図と金属組織学（顕微鏡で鋼を見る） ❓  年: 1863（1863〜1900） ⚠️（O-013 で確認：ok） | https://en.wikipedia.org/wiki/Henry_Clifton_Sorby , https://en.wikipedia.org/wiki/William_Chandler_Roberts-Austen |
| V-196 | [P-19] 振動モーター | 年 1980（1970年代〜1990年代） ⚠️ | △ | ⚠️ が残る：### [P-19] 振動モーター ❓  年: 1980（1970年代〜1990年代） ⚠️（O-013 で確認：warn） | https://en.wikipedia.org/wiki/Vibrating_alert , https://patents.google.com/patent/US4864276 |
| V-197 | [P-21] 通信チップとアンテナ | 年 1999（1979〜2008） | ○ | 出典が1つだけ（O-013 で確認：ok） | https://www.gps.gov/ |
| V-198 | [8-34] NC 旋盤 | 機械遺産の NC 旋盤は 1970（今の年を確かめる）（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-199 | [1-17] シーメンス法 | DE 1061593 は装置の特許（方法の特許かどうか、年）（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-200 | [2-41] エポキシ | 年を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-201 | [5-05] 水銀拡散ポンプ | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-202 | [5-08] | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-203 | [7-19] | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-204 | [7-21] 超低膨張ガラス | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-205 | [9-04] 合金接合トランジスタ | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-206 | [9-17] メサトランジスタ | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-207 | [9-23] MOSFET | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-208 | [9-37] FinFET | 年の幅を確かめる（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |
| V-209 | [11-06] 過リン酸石灰 | ローズ（Lawes）の名前・年（S-062、出典点検係 2026-10-26） | △ | 裏取り待ち（AUDIT_QUEUE で回す） | — |

## よく出るがリンクの無い言葉（S-009、2026-10-20）
本物欄・へえの中でよく出るのに、ノードの名前・別名・人物・用語集のどれにも無い言葉（出てくる回数の多い順、上位60から人名・年・出典の言葉を除いた物）。別名・用語集・新しいノードのどれにするかは判断が要るので提案だけ（まだ足していない）

| # | 言葉 | 回数 | 案 |
|---|---|---|---|
| L-01 | 電気 | 122 | 用語集に足す（基本の言葉） |
| L-02 | 金属 | 88 | 用語集に足す（基本の言葉） |
| L-03 | 空気 | 78 | 用語集に足す（基本の言葉） |
| L-04 | ガラス | 57 | [2-14] ソーダ石灰ガラス の別名 |
| L-05 | チップ | 46 | [9-22] 集積回路 の別名 |
| L-06 | トランジスタ | 41 | [9-03] 点接触トランジスタ（最初のトランジスタ）の別名 |
| L-07 | モーター | 38 | [4-08] 電動モーター の別名 |
| L-08 | 計算 | 38 | 用語集に足す（基本の言葉） |
| L-09 | 表面 | 34 | 用語集に足す（基本の言葉） |
| L-10 | 炭素 | 33 | 用語集に足す（基本の言葉） |
| L-11 | 画面 | 29 | 用語集に足す（基本の言葉） |
| L-12 | シリコン | 28 | [1-15] 金属シリコン か [9-08] CZ 法の別名（どちらにするか判断が要る） |
| L-13 | 機械 | 27 | 用語集に足す（基本の言葉） |
| L-14 | 酸素 | 24 | 用語集に足す（基本の言葉） |
| L-15 | 電球 | 23 | [B-01] 白熱電球 の別名 |
| L-16 | レンズ | 22 | [7-03] レンズ研磨 の別名 |
| L-17 | アーク | 22 | [I-07] 電気アークのしくみ の別名 |
| L-18 | レーザー | 22 | [7-13] レーザー原理 の別名 |
| L-19 | 装置 | 21 | 用語集に足す（基本の言葉） |
| L-20 | 設計 | 21 | 用語集に足す（基本の言葉） |
| L-21 | 直流 | 20 | 用語集に足す（基本の言葉） |
| L-22 | 電波 | 20 | 用語集に足す（基本の言葉） |
| L-23 | コイル | 19 | 用語集 |
| L-24 | 論文 | 19 | 用語集に足す（基本の言葉） |
| L-25 | 模様 | 19 | 用語集に足す（基本の言葉） |
| L-26 | 量産 | 18 | 用語集に足す（基本の言葉） |
| L-27 | 電極 | 18 | 用語集 |
| L-28 | 加熱 | 17 | 用語集に足す（基本の言葉） |
| L-29 | 燃料 | 17 | 用語集に足す（基本の言葉） |
| L-30 | 実験 | 17 | 用語集に足す（基本の言葉） |
| L-31 | 記録 | 17 | 用語集に足す（基本の言葉） |
| L-32 | 計算機 | 17 | [10-31] プログラム内蔵方式 の別名（判断が要る） |
| L-33 | 蒸気 | 16 | [4-02] 蒸気機関 の別名にはしない（意味が広い）→ 用語集 |
| L-34 | 歯車 | 16 | 新しいノード「歯車」の候補（[8-32] ホブ盤はその作り方） |
| L-35 | コンピュータ | 15 | [B-16] パソコン か [10-31] の別名（判断が要る） |
| L-36 | マスク | 15 | [7-11] 銀塩フォトマスク の別名 |
| L-37 | センサー | 15 | 用語集（センサー＝感じる部品） |
| L-38 | ロボット | 15 | [10-26] 産業用ロボット の別名 |
| L-39 | 硫黄 | 15 | 用語集に足す（基本の言葉） |
| L-40 | 性質 | 15 | 用語集に足す（基本の言葉） |
| L-41 | 交流 | 15 | 用語集に足す（基本の言葉） |
| L-42 | ウェハー | 14 | [9-12] ウェハー切断 の別名 |
| L-43 | タングステン | 14 | [1-28] タングステン線 の別名 |
| L-44 | アンモニア | 14 | [2-21] アンモニア水 か [6-09] の別名（判断が要る） |
| L-45 | カメラ | 14 | [P-27] デジタルカメラ の別名 |
| L-46 | 鉱石 | 14 | 用語集 |
| L-47 | 火花 | 14 | 用語集に足す（基本の言葉） |
| L-48 | 電線 | 14 | [4-03] 銅ケーブル の別名 |
| L-49 | 水素 | 14 | [6-01] 電解槽（水素）の別名にするか用語集（判断が要る） |

## A からの提案：v0.3b「回路の発見」の発見・考え方のノード（2026-10-20、A＝IMP から。DESIGN「回路の発見（v0.3b の中心）」・LITHO_DESIGN §11）
A（S-005、ユーザーの決定）からの依頼を司令塔へ上げる。足すかどうか・番号・つなぎ方は INBOX で決まってから。年・人物はまだ出典で確かめていない（足すときに確かめる）

| # | 足すノード（発見・考え方） | つなぐ先 | 中身（A の設計から） | 重なり・注意 | 状態 |
|---|---|---|---|---|---|
| N-15 | 抽象化（階層設計）：小さな回路をまとめて1つの記号にし、それを組み合わせて大きな回路を作る考え方 | 説明する：[10-19] VLSI 設計法（ミードとコンウェイの階層設計）、[10-11] 加算器・カウンタ・レジスタ、[B-08] 電卓 | ゲームの「ハンコは記号になる」：トランジスタ → NOT → AND → 足し算と大きな記号へ。記号を押すと一段下の中身が見える | ネットワークに同じ考えのノードは無い。[I-13] 設計と製造の分業とは別（あちらは会社・工場の分け方） | 済み（O-023）：[I-53] |
| N-16 | 回路記号：部品を決まった記号で描き、回路図で考える方法 | 説明する：[10-19] VLSI 設計法、[10-35] 回路シミュレーター SPICE。前提になる候補：[9-22] 集積回路の設計 | ゲームの製図台で、描いたトランジスタの形がハンコにすると回路記号になる | 規格（記号の国際規格など）の年は出典で確かめる ⚠️ | 済み（O-023）：[I-54]（いちばん古い規格の年は ⚠️） |
| N-17 | 論理ゲート（NOT・AND・OR を部品として組む考え方） | 説明する：[10-09] トランジスタ論理・[10-10] IC 論理・[10-11] 加算器。前提：[10-05] ブール代数とスイッチ回路 | お題「入れた電気と逆の信号が出る回路を作れ」（NOT）・「2つとも電気が来たときだけ光る」（AND） | **[10-05] ブール代数とスイッチ回路（種類: 発見）と大きく重なる**。新しいノードにするか、[10-05] に「論理ゲート」の別名・説明を足すかの判断が要る | 済み（O-023）：新しいノードにせず、既存の [10-05] ブール代数とスイッチ回路の別名「論理ゲート」「論理回路」に（network_meta.json の aliases。文の中で [10-05] へリンクされる） |

## S-025 前提の書きもれ（根の点検、2026-10-21）
根（前提が1つも無いノード）51 を全部見た。自然から得る物（鉱石・岩塩など）・最初の道具（打製石器）・考え方と発見のどれでもない 16 ノードは、本当は前提がある（本物の欄にもう書いてある物が多い）。
**ネットワークの側では network_meta.json の added_requires で補った**（図と数に入る。根 51 → 35）。**担当の方へ**：TECH_TREE の前提の欄に足してもらえれば、補いは自動で重ならない（gen_tree_views.py が同じ前提を2回足さない）。どれも前提の方が年が古いので、年の逆転は出ない。

| R | ノード | 足す前提 | 理由 | 担当 | 状態 |
|---|---|---|---|---|---|
| R-1 | [0-18] ロープ | [0-01]打製石器 | 最古の撚りひもは、石器にくっついた形で見つかった（樹皮の内側の繊維）（https://www.nature.com/articles/s41598-020-61839-w） | 幹0 | ネットワークで補った・TECH_TREE は未 |
| R-2 | [1-27] 鉛 | [3-01]たき火 | 方鉛鉱を焼いて（焙焼）、炭で還元する＝火が要る（本物の欄の反応式） | 幹1 | ネットワークで補った・TECH_TREE は未 |
| R-3 | [1-43] アンチモン | [3-01]たき火 | 輝安鉱を熱で溶け出させる・焼いて炭で還元する＝火が要る（本物の欄） | 幹1 | ネットワークで補った・TECH_TREE は未 |
| R-4 | [1-67] 浮遊選鉱 | [2-35]原油と蒸留 | 1898年からの浮選は油を使って鉱物の粒を泡に付ける（本物の欄） | 幹1 | ネットワークで補った・TECH_TREE は未 |
| R-5 | [1-76] 直接還元製鉄 | [2-35]原油と蒸留 | 天然ガスから作った水素と一酸化炭素で鉄鉱石を還元する（本物の欄） | 幹1 | ネットワークで補った・TECH_TREE は未 |
| R-6 | [2-01] 硫酸 | [1-20]白金 | 接触法（1831年フィリップスの特許）は白金を触媒に使った（https://www.gracesguide.co.uk/Peregrine_Phillips） | 幹2 | ネットワークで補った・TECH_TREE は未 |
| R-7 | [2-16] 蒸留水 | [0-03]土器、[3-01]たき火 | 蒸留器（三本腕の蒸留器）は焼き物と火で作り、熱する／水を熱して湯気にし、冷やして集める（本物の欄） | 幹2 | ネットワークで補った・TECH_TREE は未 |
| R-8 | [2-29] ゴム | [2-11]硫化水素と硫黄回収 | 1839年のグッドイヤーの加硫は硫黄で鎖を橋かけする（本物の欄） | 幹2 | ネットワークで補った・TECH_TREE は未 |
| R-9 | [2-35] 原油と蒸留 | [2-16]蒸留水、[4-02]蒸気機関 | 原油を常圧蒸留で分ける（本物の欄）／1859年のドレークの油井は蒸気機関で掘った（https://www.asme.org/topics-resources/content/drake’s-oil-well-started-a-revolution） | 幹2 | ネットワークで補った・TECH_TREE は未 |
| R-10 | [2-54] クロマトグラフィー | [2-14]ソーダ石灰ガラス | 細い管（ガラスの管）に粉をつめて混ざった物を通す（本物の欄） | 幹2 | ネットワークで補った・TECH_TREE は未 |
| R-11 | [4-03] 銅ケーブル | [1-03]銅の製錬（孔雀石） | 銅の導体（本物の欄） | 幹4 | ネットワークで補った・TECH_TREE は未 |
| R-12 | [4-08] 電動モーター | [4-13]ボルタ電池、[1-03]銅の製錬（孔雀石） | 1821年のファラデーの電磁回転は、ボルタの電池の電流で動かした（https://spectrum.ieee.org/200-years-ago-faraday-invented-the-electric-motor）／電流を流す銅の線 | 幹4 | ネットワークで補った・TECH_TREE は未 |
| R-13 | [6-02] ガスホルダー | [1-09]高炉（銑鉄） | 水を張った槽に鉄のベルをかぶせる（本物の欄） | 幹6 | ネットワークで補った・TECH_TREE は未 |
| R-14 | [6-10] 硝石床 | [11-05]堆肥、[0-10]石灰焼成 | 有機物（堆肥）に石灰をまぜて数か月積む（本物の欄）／有機物＋石灰（本物の欄） | 幹6 | ネットワークで補った・TECH_TREE は未 |
| R-15 | [6-15] ヘリウム 回避 | [2-35]原油と蒸留、[6-03]圧縮機 | 天然ガスに数%ふくまれる物を取り出す（本物の欄）／液化して分ける（本物の欄） | 幹6 | ネットワークで補った・TECH_TREE は未 |
| R-16 | [7-10] 水銀ランプ | [1-45]水銀、[2-14]ソーダ石灰ガラス、[4-14]蒸気発電 | 水銀の原子の光（本物の欄）／ガラス・石英の管に封じる（本物の欄）／電気で放電させて光らせる | 幹7 | ネットワークで補った・TECH_TREE は未 |

残した根（35）：考え方・発見 20、最初の道具 1（打製石器）、自然から得る物 13（黒鉛・白金・蛍石・錫石・輝水鉛鉱・ボーキサイト・モナザイト・珪石・天然磁石・天然ソーダ・岩塩鉱・天然硝石の採取・タンタル〔鉱物から見つけた元素〕）、確かめ中 1（[1-01] 石炭灰からのゲルマニウム：本物の欄が空。石炭を燃やした灰から取るので「火」が前提か、担当に相談）

### S-025 の2 自然の恵みを根として見せる（案。決まったら担当がノードにする）
今は植物・木・水・空気・火などは、ノードではなく文の中にだけある。根として見せるなら、幹0（サバイバル）につながる「自然の恵み」の小さなノードを足す案：
- 木（[0-01]・[0-12]木炭・[2-29]ゴムの木の前）、植物の繊維（[0-18]ロープ・[0-06]織物の前）、水（[2-16]蒸留水・[11-01]灌漑の前）、空気（[6-07]空気分離の前）、石炭（[3-01]たき火・[1-01]の前）、動物（骨・皮、[0-01] のころの道具）
- 年は「いつもある」（年なし）として、図では左のはしにまとめる。ゲームではバニラの素材（木・水・羊毛など）なので、MOD の作り方は「自然にある」
- 入れると根が「自然の恵み」6 ほどにまとまり、ムービーの「たくさんの始まり」も分かりやすくなる。入れるかどうかはユーザーと担当で決める

## S-031 前提の点検：材料・道具や機械・知恵（2026-10-21）
各ノードの前提（requires・enables・part・understanding）を「材料（何から作る）＝冶金・化学・ガスの幹の技術」「道具や機械（何で作る）＝そのほかの技術とモノ」「知恵（何が分かっていたから）＝発見・考え方」に分けた（データから自動。決まりで分けるので、あいまいな物はある）。
- 全体（考え方を除く 571 ノード）：材料が無い 260・道具が無い 136・知恵が無い 473
- AI までの道（[10-16] の祖先 272）のうち、どれかが空のノード 210（知恵だけが空 56、材料か道具が空 154）
- **ネットワークの側で補った（S-031、出典つき、added_requires）**：鋼 [1-26] ← 高炉（銑鉄）[1-09]／MOSFET [9-23] ← CZ 法 [9-08]（単結晶のシリコン）／CO2 レーザー [7-14] ← レーザー原理 [7-13]／マイクロコンピュータ [10-14] ← DRAM [10-12]
- 知恵が空のノードがとても多いのは、「なぜ動くか」の知恵（understanding）をまだ少ししか書いていないため。担当の方へ：本物の欄で使っている原理（例：MOSFET ← 固体のバンド理論 [I-29]）を「理解」の欄に足してもらえれば、中心図の知恵が増える

### AI までの道のノードで、材料か道具が前提に無い物（担当の方へ：本物どおりに足す前提の候補を、出典つきで）

| ノード | 空の物 |
|---|---|
| [0-01] 打製石器 | 材料・道具・知恵 |
| [0-02] 火起こし | 材料・知恵 |
| [0-03] 土器 | 材料・知恵 |
| [0-04] 農耕 | 材料・知恵 |
| [0-06] 織物 | 材料・知恵 |
| [0-07] 羊毛と紡錘車 | 材料・知恵 |
| [0-08] 木工 | 材料・知恵 |
| [0-10] 石灰焼成 | 材料・知恵 |
| [0-11] 消石灰・漆喰 | 材料・知恵 |
| [0-12] 木炭 | 材料・知恵 |
| [0-15] 窯 | 材料・知恵 |
| [0-16] 塩田 | 材料・知恵 |
| [1-01] 石炭灰からのゲルマニウム | 材料・道具 |
| [1-03] 銅の製錬（孔雀石） | 材料 |
| [1-04] 銅の製錬（黄銅鉱） | 道具・知恵 |
| [1-05] 青銅 | 道具・知恵 |
| [1-06] 塊鉄炉 | 材料・知恵 |
| [1-07] 黒鉛 | 材料・道具・知恵 |
| [1-08] 鍛鉄 | 道具 |
| [1-09] 高炉（銑鉄） | 道具 |
| [1-10] コークス高炉 | 道具・知恵 |
| [1-13] 亜鉛 | 材料・知恵 |
| [1-14] バイヤー法 | 道具・知恵 |
| [1-16] トリクロロシラン合成・蒸留 | 道具・知恵 |
| [1-17] シーメンス法 | 道具・知恵 |
| [1-18] 白金精製 | 道具・知恵 |
| [1-19] 希土類分離 | 道具・知恵 |
| [1-20] 白金 | 材料・道具・知恵 |
| [1-21] 蛍石 CaF2 | 材料・道具・知恵 |
| [1-22] 錫石 SnO2 | 材料・道具・知恵 |
| [1-23] 輝水鉛鉱 MoS2 | 材料・道具・知恵 |
| [1-24] ボーキサイト | 材料・道具・知恵 |
| [1-25] モナザイト | 材料・道具・知恵 |
| [1-27] 鉛 | 材料・知恵 |
| [1-28] タングステン線 | 道具 |
| [1-29] アルミニウム | 道具・知恵 |
| [1-30] 錫 | 道具・知恵 |
| [1-31] モリブデン製錬 | 道具・知恵 |
| [1-32] 銀 | 道具・知恵 |
| [1-33] ホウ素 | 道具・知恵 |
| [1-35] ネオジム磁石 | 道具・知恵 |
| [1-36] 単体ホウ素 | 道具・知恵 |
| [1-37] ニッケル | 道具・知恵 |
| [1-43] アンチモン | 材料・知恵 |
| [1-45] 水銀 | 材料・知恵 |
| [1-46] 珪石 | 材料・道具・知恵 |
| [1-70] ジルコニウム・ハフニウム | 道具・知恵 |
| [1-72] 粉末冶金と熱間静水圧プレス | 道具・知恵 |
| [10-02] リレー | 材料・知恵 |
| [10-04] フィードバック制御 | 材料・知恵 |
| [10-07] 真空管フリップフロップと計算機 | 材料・知恵 |
| [10-09] トランジスタ論理 | 材料 |
| [10-10] IC 論理 | 材料・知恵 |
| [10-11] 加算器・カウンタ・レジスタ | 材料 |
| [10-12] DRAM | 材料・知恵 |
| [10-13] マイクロプロセッサ | 材料・知恵 |
| [10-14] マイクロコンピュータ | 材料・知恵 |
| [10-15] 露光装置の制御 | 材料・知恵 |
| [10-31] プログラム内蔵方式 | 材料・知恵 |
| [10-32] 浮動小数点の標準 | 材料・知恵 |
| [11-05] 堆肥 | 材料・知恵 |
| [12-11] 測量 | 材料・知恵 |
| [12-12] 換気・空調 | 材料・知恵 |
| [12-14] 層流クリーンルーム | 材料・知恵 |
| [15-01] 紙 | 材料・知恵 |
| [15-06] 電波の発見 | 材料・知恵 |
| [15-11] テレビ | 材料 |
| [2-01] 硫酸 | 道具・知恵 |
| [2-02] カリ | 材料・知恵 |
| [2-03] 天然ソーダ | 材料・道具・知恵 |
| [2-04] 海藻灰ソーダ | 材料・知恵 |
| [2-07] 重クロム酸カリウム | 道具・知恵 |
| [2-09] 石けん | 道具・知恵 |
| [2-10] フッ酸 | 道具・知恵 |
| [2-11] 硫化水素と硫黄回収 | 道具・知恵 |
| [2-16] 蒸留水 | 材料・知恵 |
| [2-17] イオン交換 | 道具 |
| [2-19] 過酸化水素 | 道具・知恵 |
| [2-21] アンモニア水 | 道具・知恵 |
| [2-23] 木材乾留 | 材料・知恵 |
| [2-24] ベンゼン | 道具・知恵 |
| [2-25] フェノール | 道具・知恵 |
| [2-26] メタノール | 道具・知恵 |
| [2-27] ホルムアルデヒド | 道具・知恵 |
| [2-28] フェノール樹脂 | 道具 |
| [2-29] ゴム | 道具 |
| [2-30] ネガ型フォトレジスト | 道具 |
| [2-32] ポジ型フォトレジスト | 道具・知恵 |
| [2-38] PTFE | 道具・知恵 |
| [2-39] エタノール | 道具・知恵 |
| [2-43] ばん油 | 材料・知恵 |
| [2-44] 赤リン | 材料・知恵 |
| [2-58] 無水酢酸 | 道具・知恵 |
| [2-61] ポリケイ皮酸ビニル | 道具・知恵 |
| [3-01] たき火 | 材料 |
| [3-04] ふいご | 材料・知恵 |
| [3-12] 耐火れんが | 材料・知恵 |
| [3-14] 誘導加熱炉 | 材料 |
| [3-16] 真空炉 | 材料・知恵 |
| [4-01] 水車発電機 | 材料 |
| [4-02] 蒸気機関 | 道具 |
| [4-03] 銅ケーブル | 道具・知恵 |
| [4-04] ダイナモ・天然磁石 | 材料・道具 |
| [4-09] 高周波発振器 | 材料・知恵 |
| [4-10] 変圧器 | 材料・知恵 |
| [4-11] 整流器 | 材料・知恵 |
| [4-14] 蒸気発電 | 材料 |
| [4-27] 誘導コイル | 材料・知恵 |
| [5-03] 水銀柱と圧力の単位 | 道具・知恵 |
| [5-06] 油拡散ポンプ | 材料・知恵 |
| [5-11] 真空容器 | 道具・知恵 |
| [5-12] 真空シール | 道具・知恵 |
| [5-15] シュプレンゲル水銀ポンプ | 道具・知恵 |
| [5-16] ゲッター | 道具 |
| [6-02] ガスホルダー | 道具・知恵 |
| [6-04] 岩塩鉱 | 材料・道具・知恵 |
| [6-06] 塩酸 | 道具 |
| [6-07] 空気分離 | 道具 |
| [6-08] 希ガス精製 | 道具・知恵 |
| [6-09] ハーバー・ボッシュ法 | 道具 |
| [6-10] 硝石床 | 材料・知恵 |
| [6-11] 硝酸 | 道具・知恵 |
| [6-12] 鉛室法硫酸 | 道具・知恵 |
| [6-16] 天然硝石の採取 | 材料・道具・知恵 |
| [7-01] 光学ガラス | 道具・知恵 |
| [7-09] 干渉計 | 材料・知恵 |
| [7-11] 銀塩フォトマスク | 材料・知恵 |
| [7-19] 投影レンズ | 材料・知恵 |
| [8-01] 旋盤 | 道具・知恵 |
| [8-06] 光波干渉測長 | 材料・知恵 |
| [8-08] ボールねじ精密ステージ | 材料・知恵 |
| [8-10] 光学顕微鏡 | 材料・知恵 |
| [8-14] 鏡筒の精密組立 | 材料・知恵 |
| [8-17] アライメント | 材料・知恵 |
| [8-33] 質量分析 | 材料・知恵 |
| [9-01] Ge 帯域精製 | 道具 |
| [9-02] ゾーンレベリング | 材料 |
| [9-04] 合金接合トランジスタ | 材料・知恵 |
| [9-06] 酸化膜エッチング | 道具・知恵 |
| [9-11] ドーパント源 | 道具 |
| [9-14] RCA 洗浄 | 道具・知恵 |
| [9-16] 熱拡散 | 材料・知恵 |
| [9-17] メサトランジスタ | 材料・知恵 |
| [9-18] プレーナ技術 | 材料・知恵 |
| [9-20] 密着・近接露光 | 材料・知恵 |
| [9-22] 集積回路 | 材料・知恵 |
| [9-23] MOSFET | 材料・知恵 |
| [9-24] CMOS | 材料・知恵 |
| [9-28] イオン注入 | 材料・知恵 |
| [9-39] 縮小投影露光 | 材料 |
| [9-47] 加速器 | 材料・知恵 |
| [B-02] 銀塩写真 | 材料・知恵 |
| [B-02a] ダゲレオタイプ | 道具・知恵 |
| [B-11] AIチップ | 材料・知恵 |

## S-035 中心図の粒度（輪の意味）の迷う物（2026-10-21）
輪：1 製品・部品／2 素子・装置／3 工程・機械・道具（発見・考え方もここ）／4 材料／5 原点（前提の無い物）。モノは1、発見は3、そのほかは幹の決まり（network_meta.json の granularity）。手で直した物 51
- 数：輪1 73・輪2 163・輪3 235・輪4 159（原点は中心図ごとに決まる）
- 幹の決まりのまま入っている、迷う幹（動力・真空・光学・半導体・制御・輸送・通信・枝）の物 167。下の一覧で違う物があれば network_meta.json の granularity.nodes に番号と輪を書く
- 気になる点：輪1 には、そのモノの部品だけでなく、祖先のモノ（紙・電話・青銅など）も入る（モノは1、の決まりのため）

| ノード | 今の輪 |
|---|---|
| [4-01] 水車発電機 | 2 |
| [4-03] 銅ケーブル | 2 |
| [4-04] ダイナモ・天然磁石 | 2 |
| [4-05] 自励式発電機 | 2 |
| [4-06] 蒸気タービン | 2 |
| [4-07] 鉛蓄電池 | 2 |
| [4-08] 電動モーター | 2 |
| [4-09] 高周波発振器 | 2 |
| [4-10] 変圧器 | 2 |
| [4-11] 整流器 | 2 |
| [4-12] 電気炉 | 2 |
| [4-13] ボルタ電池 | 2 |
| [4-15] リチウムイオン電池 | 2 |
| [4-16] アーク灯 | 2 |
| [4-17] ステッピングモーター | 2 |
| [4-18] 電気のファン | 2 |
| [4-19] スイッチング電源 | 2 |
| [4-20] 振動モーター | 2 |
| [4-21] ブラシレス DC モーター | 2 |
| [4-22] 誘導モーター | 2 |
| [4-23] 横流ファン | 2 |
| [4-24] 羽根のない扇風機 | 2 |
| [4-25] 回生ブレーキ | 2 |
| [4-26] EV の急速充電 | 2 |
| [4-27] 誘導コイル | 2 |
| [4-28] 乾電池 | 2 |
| [4-29] ガスタービンとジェットエンジン | 2 |
| [4-30] 三相交流送電 | 2 |
| [4-31] 原子力発電 | 2 |
| [4-32] 高圧直流送電 | 2 |
| [4-33] 水車タービン | 2 |
| [4-34] 交流発電機 | 2 |
| [4-35] 高圧送電 | 2 |
| [4-36] ダニエル電池 | 2 |
| [4-37] NaS 電池 | 2 |
| [5-01] 手押しピストンポンプ | 3 |
| [5-03] 水銀柱と圧力の単位 | 3 |
| [5-04] 油回転ポンプ | 3 |
| [5-05] 水銀拡散ポンプ | 3 |
| [5-06] 油拡散ポンプ | 3 |
| [5-07] ターボ分子ポンプ | 3 |
| [5-08] イオンポンプ | 3 |
| [5-09] クライオポンプ → 液体窒素コールドトラップ | 3 |
| [5-11] 真空容器 | 3 |
| [5-12] 真空シール | 3 |
| [5-14] 真空蒸着 | 3 |
| [5-15] シュプレンゲル水銀ポンプ | 3 |
| [9-01] Ge 帯域精製 | 3 |
| [9-02] ゾーンレベリング | 3 |
| [9-06] 酸化膜エッチング | 3 |
| [9-07] Ge 単結晶の引き上げ | 3 |
| [9-08] CZ 法 | 3 |
| [9-09] FZ 法 | 3 |
| [9-12] ウェハー切断 | 3 |
| [9-13] ラッピング・鏡面研磨 | 3 |
| [9-14] RCA 洗浄 | 3 |
| [9-15] 熱酸化 | 3 |
| [9-16] 熱拡散 | 3 |
| [9-18] プレーナ技術 | 3 |
| [9-19] レジスト塗布 | 3 |
| [9-20] 密着・近接露光 | 3 |
| [9-21] 現像・ポストベーク | 3 |
| [9-25] 金属配線 | 3 |
| [9-26] スパッタリング | 3 |
| [9-27] ドライエッチング | 3 |
| [9-28] イオン注入 | 3 |
| [9-29] アニール | 3 |
| [9-30] エピタキシーと CVD | 3 |
| [9-31] CMP | 3 |
| [9-32] ダイシング | 3 |
| [9-33] ワイヤーボンディング | 3 |
| [9-34] 封止 | 3 |
| [9-35] テスト | 3 |
| [9-36] 銅配線 | 3 |
| [9-39] 縮小投影露光 | 3 |
| [10-01] 電磁石 | 2 |
| [10-02] リレー | 2 |
| [10-04] フィードバック制御 | 2 |
| [10-06] リレー計算機 | 2 |
| [10-07] 真空管フリップフロップと計算機 | 2 |
| [10-08] 磁気コアメモリ | 2 |
| [10-09] トランジスタ論理 | 2 |
| [10-10] IC 論理 | 2 |
| [10-11] 加算器・カウンタ・レジスタ | 2 |
| [10-12] DRAM | 2 |
| [10-13] マイクロプロセッサ | 2 |
| [10-14] マイクロコンピュータ | 2 |
| [10-15] 露光装置の制御 | 2 |
| [10-17] CAD | 2 |
| [10-18] コンパイラ | 2 |
| [10-19] VLSI 設計法 | 2 |
| [10-20] 解析機関 | 2 |
| [10-21] 計算尺 | 2 |
| [10-22] キーボード | 2 |
| [10-23] フラッシュメモリ | 2 |
| [10-24] SSD | 2 |
| [10-25] GPU | 2 |
| [10-26] 産業用ロボット | 2 |
| [10-27] サーボ機構 | 2 |
| [10-28] 強化学習 | 2 |
| [10-29] Transformer と大きな AI | 2 |
| [10-30] 自分の位置と地図を同時に作る計算 | 2 |
| [10-31] プログラム内蔵方式 | 2 |
| [10-32] 浮動小数点の標準 | 2 |
| [10-33] 磁気ディスク装置 | 2 |
| [10-34] オペレーティングシステム | 2 |
| [10-35] 回路シミュレーター | 2 |
| [10-36] RISC | 2 |
| [10-37] たんぱく質の形を AI で予測する | 2 |
| [10-38] ストレージ管のグラフィック端末 | 2 |
| [10-39] BASIC で動く小さなマイコンの板 | 2 |
| [14-01] 運河と閘門 | 2 |
| [14-03] 経度とクロノメーター | 2 |
| [14-06] 鋼のレール | 2 |
| [14-07] 空気入りタイヤ | 2 |
| [14-08] 自転車 | 2 |
| [14-09] 内燃機関 | 2 |
| [14-10] ディーゼル機関 | 2 |
| [14-13] ジュラルミン | 2 |
| [14-14] 電気鉄道 | 2 |
| [14-16] 自動運転 | 2 |
| [14-17] 空気ブレーキ | 2 |
| [14-18] ドラムブレーキとディスクブレーキ | 2 |
| [14-19] 自動変速機 | 2 |
| [14-20] ターボチャージャー | 2 |
| [14-21] E6B フライトコンピューター | 2 |
| [15-03] 海底ケーブル | 2 |
| [15-06] 電波の発見 | 2 |
| [15-07] 無線電信 | 2 |
| [15-08] 鉱石ラジオ | 2 |
| [15-12] 磁気記録 | 2 |
| [15-13] 光ファイバー | 2 |
| [15-15] コンピュータネットワーク | 2 |
| [15-16] USB | 2 |
| [15-17] Wi-Fi | 2 |
| [15-18] 静電容量式タッチパネル | 2 |
| [15-19] スピーカー | 2 |
| [15-20] 携帯電話の電波 | 2 |
| [15-21] Bluetooth | 2 |
| [15-22] アンテナ | 2 |
| [15-23] コンデンサーマイク | 2 |
| [15-24] ダイナミックマイク | 2 |
| [15-25] エレクトレットコンデンサーマイク | 2 |
| [15-26] MEMS マイク | 2 |
| [15-27] 八木・宇田アンテナ | 2 |
| [15-28] 光ディスク | 2 |
| [15-29] 誤り訂正符号 | 2 |
| [15-30] 公開鍵暗号 | 2 |
| [15-31] 回転ヘッド式ビデオテープ | 2 |
| [15-32] 腕木通信 | 2 |
| [15-33] ヘルシュライバー | 2 |
| [B-02a] ダゲレオタイプ | 2 |
| [B-02b] 湿板写真 → ゼラチン乾板 | 2 |
| [B-03] X線管 | 2 |
| [B-05] 偏光 | 2 |
| [B-06] 偏光フィルム | 2 |
| [B-11] AIチップ | 2 |
| [B-13] モーブ | 2 |
| [B-14] アセチレンランプ | 2 |
| [B-15] 豆腐 | 2 |
| [B-18] 液晶ディスプレイ | 2 |
| [B-19] TFT 液晶 | 2 |
| [B-20] 白色 LED バックライト | 2 |
| [B-21] ボールマウス | 2 |
| [B-22] 光学式マウス | 2 |
| [B-23] 溶解アセチレンと灯台の自動点滅器 | 2 |
| [B-24] ニキシー管 | 2 |

## S-036 製品・枝の前提の点検と、考え方の前提の書き方（2026-10-21）
- 製品（P-）・枝（B-）63 のうち、前提（requires・part・enables）が3つ以下 35。ほとんどは「モノのカードが技術ノード1つを指す」形（例：メモリ ← DRAM）で、本物の前提はその技術ノードの側にある。本当に足りなかった物を直した：
  - [P-02] 液体燃料ロケット（products.md を直した）：鋼・アルミニウム（スミソニアンの所蔵品の材料）、蒸気タービン（ド・ラバルのノズル、NASA）
  - [P-01] クレーン（products.md を直した）：ロープ
  - [B-14] アセチレンランプ ← [0-10] 石灰（カーバイドは石灰とコークスから、ACS）、[B-15] 豆腐 ← [3-01] たき火（煮る）：ネットワークの側で補った。**担当の方へ**：TECH_TREE の前提に足してください
- 考え方・発見の前提に材料が入っている物 8。[14-15] ロケット方程式だけが本当におかしい（式に液体酸素・エタノール・精密加工は要らない）→ **ネットワークの側で外し（dropped_requires）、[I-10] 対数を前提に足した**。**担当の方へ**：TECH_TREE の [14-15] の前提を「[I-10] 対数」に直してください（液体酸素などは [P-02] 液体燃料ロケットの前提にした）
  - ほかの7つ（超伝導の発見 ← 水銀・ヘリウム、酸素の発見 ← 水銀、フランク・ヘルツの実験 ← 水銀、希ガス化合物 ← 希ガス・フッ素・白金、電気アークのしくみ ← 黒鉛、フロンとオゾン層 ← 冷媒フロン、量子ドット ← ガラス）は、発見に使った実験の材料なので、そのままでよい

## S-037 子孫（これが生んだ物）が0の考え方・技術（2026-10-21）
- [I-50] ムーアの法則：前提（理解のつながり）208 はあるが、ムーアの法則から出るつながりが無い。本物では、微細化の計画（ロードマップ）を通して [9-39] 縮小投影露光・[9-40] EUV 露光装置・[10-13] マイクロプロセッサなどの開発の目標になった → 「前提になる」か「理解」で足す案（出典を探してから）
- [9-03] 点接触トランジスタ：子孫が無い。[9-04] 合金接合トランジスタ・[9-10] 成長接合トランジスタの前提にしてよいか、担当に相談

## S-047（2026-10-21）ソーダ石灰ガラス [2-14] の前提に珪石（砂）[1-46]
- ふつうのガラスの重さの 70〜74% はシリカ（砂・石英）。今の前提はソーダ（天然ソーダ・海藻灰・ルブラン法）と石灰だけで、主な原料の砂が無い
- 出典：https://en.wikipedia.org/wiki/Soda%E2%80%93lime_glass
- ネットワークの側では network_meta.json の added_requires で足した（「あ！」の「画面のガラスは砂から」に使う）。TECH_TREE の [2-14] の前提に [1-46] を足してほしい

## S-059（2026-10-21）つながりを本物どおりに増やす：TECH_TREE に入れてほしい前提・部品（幹ごと）
ネットワークの側では network_meta.json（added_requires・extra_links）で先に入れた。出典を開いて確かめた物だけ（◎○）。
- **幹10**：[10-13] マイクロプロセッサ ← [9-22] 集積回路（前提、◎ Wikipedia・Computer History Museum）
- **枝B**：[B-09] ゲーム機 ← [15-11] テレビ（前提：最初の家庭用ゲーム機 Magnavox Odyssey はテレビにつないだ、○）／[B-16] パソコン の部品に [B-21] ボールマウス（○）
- **幹14**：[14-11] 自動車 の部品に [0-09] 車輪・[4-07] 鉛蓄電池（始動用、○）
- 前提でも部品でもない新しい種類（改良・置き換え・需要・道具）は、図の側だけで持つ（TECH_TREE には入れなくてよい）

## S-059 途中3（2026-10-21）：今あるつながりで直したほうがよさそうな物と、足りないノード（調べた時に見つかった）
- **年が合わない今の前提**（TECH_TREE）：
  - [B-08] 電卓（1964 シャープ CS-10A）の前提に [9-22] 集積回路・[9-24] CMOS → 1964年の電卓はばらのゲルマニウムトランジスタ。IC・CMOS の電卓はあと（別の物か、年の欄を見直す）
  - [15-10] トランジスタラジオ（1954 TR-1）の前提が [9-04] 合金接合 → TR-1 は TI の成長接合トランジスタ（[9-10]）。ネットワークの側で [9-10] を足した
  - [9-03] 点接触トランジスタ（1947）の前提に [9-02] ゾーンレベリング（1952）→ 年が逆
  - [P-07] モニター（1996）の部品に [B-20] 白色 LED のバックライト → 1996年の液晶のモニターは冷陰極管（CCFL）
- **足りないノードの案**（出典つき、作るかは担当と相談役）：
  - たたき石と 火打ち石（割りやすい石）（Hammerstone and flint/chert (knappable stone)）：0-01 のため。Knapped tools are made by striking flint or chert with a hammerstone; no earlier node exists in the list.　出典 https://en.wikipedia.org/wiki/Hand_axe
  - プラスチックの 非球面レンズ（Molded plastic aspheric lenses）：P-17 のため。Phone camera lenses are made of many molded plastic aspheric elements, not ground glass.　出典 https://en.wikipedia.org/wiki/Camera_phone
  - アルミ電解コンデンサ（Aluminium electrolytic capacitor）：P-11 のため。Smooths the rectified DC in power supplies and switched-mode supplies; a main component of every PSU.　出典 https://en.wikipedia.org/wiki/Aluminum_electrolytic_capacitor
  - コバルト酸リチウム正極（Lithium cobalt oxide cathode）：P-22 のため。Goodenough's 1980 cathode used in the first commercial Li-ion battery; no cobalt or LiCoO2 node exists.　出典 https://en.wikipedia.org/wiki/Lithium-ion_battery
  - シリンダー中ぐり盤（Cylinder boring machine (Wilkinson, 1774)）：4-02 のため。ワットの1776年の機関は、ウィルキンソンが精密にくりぬいたシリンダーで初めて実用になった。旋盤[8-01]（1797）とは別で、年も前。　出典 https://en.wikipedia.org/wiki/Watt_steam_engine

## S-059 途中4（2026-10-21）：本線（AI までの道）を調べて見つかった直したほうがよい所
- ネットワークの側で外した物（dropped_requires）：[3-18] 蓄熱式炉（1856）← [3-19] 副産物回収式コークス炉（1881）＝年が逆。要るのは石炭ガス。逆向き（ホフマンのコークス炉がジーメンスの蓄熱室を使った、◎）を足した
- **今の前提で、年が逆か出典の無い物**（TECH_TREE を確かめてほしい）：
  - [2-08] 苛性ソーダ → [2-09] 石けん：Caustic soda node is 1892, soap is -2800 (anachronism). Babylonian soap used fat + wood ash (potash, already linked).
  - [2-01] 硫酸 → [1-33] ホウ素：Sulfuric acid node (contact process) is 1831, boron 1808 (anachronism). Boron came from borax/boric acid reduced with potassium; if an acid link is wanted, 2-43 oil of vitriol (1300) fits the year.
  - [6-05] クロルアルカリ電解 → [2-58] 無水酢酸：Chlor-alkali electrolysis is 1890, acetic anhydride 1852 (anachronism). Gerhardt used benzoyl chloride (missing node).
  - [1-39] マグネシウム → [5-16] ゲッター：Magnesium node is 1941, getter 1894 (anachronism). The first getter (Malignani) was phosphorus; Wikipedia Getter says phosphorus was used before metallic getters.
  - [2-11] 硫化水素と硫黄回収 → [2-29] ゴム：Year is fine, but 2-11 is 'H2S and sulfur recovery'; vulcanization really needs plain sulfur (missing node).
  - [1-26] 鋼 → [8-01] 旋盤：Steel (Bessemer) is 1856, after Maudslay's lathe (1797); crucible steel 1-11 already covers it.
  - [5-02] 真空管 → [4-11] 整流器：Vacuum tube (1904) is after the rectifier node year 1902 (Cooper Hewitt mercury-arc rectifier).
  - [9-05] Geダイオード → [4-11] 整流器：Ge diode (1940) is after 1902; if 4-11 is meant to be the 1902 mercury-arc rectifier this is anachronistic.
  - [6-05] クロルアルカリ電解 → [1-14] バイヤー法：Chlor-alkali electrolysis node is 1890, after the Bayer process (1888); NaOH then came from causticizing soda ash with slaked lime.
  - [1-10] コークス高炉 → [1-37] ニッケル：Cronstedt isolated nickel (1751) from nickeline from a cobalt mine; no source ties it to the coke blast furnace.
  - [0-10] 石灰焼成 → [2-02] カリ：Potash is made by leaching wood ash; lime is only needed to make caustic potash, not potash itself (not verified as wrong, just unsupported).
  - [8-04] ノギスとマイクロメータ → [12-11] 測量：Year order: calipers/micrometers 1631 is after surveying 1615 (Snellius used a quadrant).
  - [7-04] 屈折望遠鏡と色消しレンズ → [12-11] 測量：Snellius' 1615 survey is documented with a quadrant only; telescopic sights in surveying came later (Picard 1669-70). Weak as a prerequisite at 1615.
  - [6-01] 電解槽 → [1-31] モリブデン製錬：Year order: electrolyzer 1800 is after molybdenum 1781; Hjelm reduced the oxide with carbon, not electrolysis.
  - [6-09] ハーバー・ボッシュ法 → [2-21] アンモニア水：Year order: Haber-Bosch 1913 is after ammonia water 1850; 19th-century ammonia water came from gasworks liquor and hartshorn distillation.
  - [1-39] マグネシウム → [1-36] 単体ホウ素：Year order: magnesium node is 1941 but elemental boron 1892; metallic Mg existed from 1808 (Davy) / 1831 (Bussy), so the Mg node year looks wrong rather than the link.
- **足りないノードの案**（出典つき。作るかは担当と相談役）：
  - 辰砂（Cinnabar (HgS)）：1-45 のため。Mercury is retorted (distilled) from cinnabar, its main ore.　出典 https://en.wikipedia.org/wiki/Cinnabar
  - 閃亜鉛鉱（亜鉛の鉱石）（Sphalerite / zinc ore）：1-13 のため。Zawar zinc was distilled from roasted ore with charcoal in clay retorts.　出典 https://www.theweek.in/theweek/statescan/2021/12/16/the-lost-city-of-zawar.html
  - なめし革（Leather / hide）：3-04 のため。Pot bellows were ceramic pots covered with a loose leather hide.　出典 https://en.wikipedia.org/wiki/Bellows
  - 石炭とコークスづくり（Coal and coke making）：1-10 のため。Darby's 1709 furnace used coke ('charked' coal) from Shropshire coal; no coal/coke node exists before 3-19 (1881).　出典 https://en.wikipedia.org/wiki/Abraham_Darby_I
  - 水車（動力）（Water wheel）：1-10 のため。The Coalbrookdale furnace blast came from a waterwheel driving bellows; 4-33 water turbine is 1827.　出典 https://en.wikipedia.org/wiki/Coalbrookdale
  - 風車と遠心調速機（Windmill and Huygens' centrifugal governor）：10-04 のため。The centrifugal governor regulated windmills and water wheels a century before Watt adapted it in 1788.　出典 https://en.wikipedia.org/wiki/Centrifugal_governor
  - 金属カリウム（Potassium metal）：1-33 のため。Boron was isolated in 1808 by heating borax/boric acid with potassium metal (Davy made potassium by electrolysis in 1807　出典 https://periodic-table.rsc.org/element/5/boron
  - ホウ砂（Borax）：1-33 のため。Starting material for boron isolation (known as glaze/flux long before).　出典 https://en.wikipedia.org/wiki/Boron
  - 硫黄（Sulfur）：2-01 のため。The contact process starts by burning sulfur to SO2; 2-11 (sulfur recovery, 1777) is not the same thing.　出典 https://en.wikipedia.org/wiki/Contact_process
  - 天然ゴム（Natural rubber (latex)）：2-29 のため。Goodyear vulcanized India rubber with sulfur; the raw rubber has no node.　出典 https://en.wikipedia.org/wiki/Charles_Goodyear
  - 塩化ベンゾイル（Benzoyl chloride）：2-58 のため。Gerhardt made acetic anhydride from potassium acetate and benzoyl chloride (1852).　出典 https://en.wikipedia.org/wiki/Acetic_anhydride
  - インジウム（Indium）：9-04 のため。Indium beads formed the emitter/collector of PNP alloy-junction transistors (indium discovered 1863).　出典 https://en.wikipedia.org/wiki/Indium
  - ヨウ素（Iodine）：1-70 のため。The van Arkel-de Boer crystal-bar process decomposes Hf/Zr iodide on a hot tungsten filament.　出典 https://en.wikipedia.org/wiki/Hafnium
  - 充てん材（木粉・石綿）（Filler (wood flour / asbestos)）：2-28 のため。Moulded Bakelite was mixed with wood flour or asbestos filler.　出典 https://en.wikipedia.org/wiki/Bakelite
  - 白リン（White phosphorus）：2-44 のため。Red phosphorus was first made (Schrötter 1847) by heating or illuminating white phosphorus (Brand 1669); no phosphorus n　出典 https://en.wikipedia.org/wiki/Red_phosphorus
  - ジュール・トムソン効果（Joule–Thomson effect）：6-07 のため。The Hampson–Linde cycle relies solely on the Joule–Thomson effect plus a countercurrent heat exchanger.　出典 https://en.wikipedia.org/wiki/Hampson%E2%80%93Linde_cycle
  - 光酸発生剤（オニウム塩）（Onium-salt photoacid generators）：2-33 のため。Chemically amplified resists need an acid released by light; triarylsulfonium/diaryliodonium salts (Crivello, GE, late 1　出典 https://acswebcontent.acs.org/prfar/2007/REPORTS/P7914.HTM
  - ガタパーチャ（電線の被覆）（Gutta-percha cable insulation）：4-03 のため。19th-century telegraph and submarine cables were insulated with gutta-percha; no insulation node exists for copper cable　出典 https://en.wikipedia.org/wiki/Gutta-percha
  - エーテル（ジエチルエーテル）（Diethyl ether）：3-22 のため。Perkins' 1834 vapor-compression machine used ether as its working fluid.　出典 https://en.wikipedia.org/wiki/Icemaker
  - 測量用の四分儀（Surveying quadrant）：12-11 のため。Snellius' 1615 triangulation used a large quadrant to measure angles (now in Museum Boerhaave).　出典 https://en.wikipedia.org/wiki/Willebrord_Snellius
  - ボルダの反復円（Borda repeating circle）：8-22 のため。Instrument used by Delambre and Mechain for the Dunkirk-Barcelona meridian survey that fixed the metre.　出典 https://en.wikipedia.org/wiki/Repeating_circle
  - ガイスラー管（放電管）（Geissler tube (gas discharge tube)）：6-08 のため。Ramsay and Travers identified neon by its red glow in a Geissler tube (Geissler 1857).　出典 https://en.wikipedia.org/wiki/Neon_sign
  - 砥石車（研削といし）（Bonded abrasive grinding wheel）：8-31 のため。The abrasive wheel is the cutting tool of a grinding machine; no abrasive node exists.　出典 https://en.wikipedia.org/wiki/Grinding_machine
  - 浮動小数点コプロセッサ 8087（Intel 8087 floating-point coprocessor）：10-32 のため。First IC implementing the draft of IEEE 754 (1980); Kahan's draft was written for it.　出典 https://en.wikipedia.org/wiki/IEEE_754-1985

## S-061（2026-10-26）物（ノード）の決まっていないアイテム 82 の入れ先
- ウェブを「1つの物に1つのページ」にした（S-061）。アイテムは、その物のページの「作り方」にのる。ノードの決まっていない 82 は前のウィキのページのまま
- 入れ先の案（自動。レシピでいっしょに出る物のノードを数えた。64 に案、18 は案なし）：docs/mockups/S061_unassigned_items.md。**決めるのは A とユーザー**。決まったら wiki_export.json のアイテムの node に入れてもらえれば、B が作り直す

## S-062（2026-10-26）存命の人の名前・作り方の書きすぎ（出典点検係の点検から）
- **ネットワークの側で直した**（TECH_TREE・products.md・network_meta）：存命と見られる人の名前を「〇〇の研究者たち」などに（[2-45] 小玉秀男・ハル、[9-37] 胡正明、[10-13] 嶋正利・ファジン・ホフ（メイザーは故人だが一緒に「技術者たち」に）、[B-07] 中村修二・天野（赤崎は故人：人物の紹介の「天野浩とともに」を「研究の仲間とともに」に）、[10-16] ヒントン・ウィリアムズ（生死不明は外す）・ルカン、[10-19] カーヴァー・ミード、[P-04] 小玉秀男・ハル）。[4-15] 吉野彰は D の S-014 で
- 作り方の書きすぎ：[B-02b] の元の記述（コロジオンの工程と、硝化セルロース＝綿＋硝酸＋硫酸）を外して歴史だけに。[13-03] の前提の作り方の道（エタノール＋硫酸でエーテル、硝酸アンモニウムの加熱で N2O）を、ノードの番号だけに。[2-38] の前提のクロロホルムの作り方も番号だけに
- **担当（A・D・E）へ**：
  - ゲームの言葉（src/main/lang）と REFERENCES.md にも同じ存命の人の名前がある（嶋正利〔common〕・ルカン〔branches〕・小玉秀男・Hull・Kodama〔chemistry〕、REFERENCES のヒントン・ルカン・小玉・ハル・嶋）。同じように直してほしい
  - 実装の書き方（ゲームのレシピの説明）で、作り方がくわしすぎないか見直してほしい所：[2-45] ニッケルカルボニルを使う道、[2-57] 硝石＋鉛で亜硝酸カリウム、[2-33] HF・KSbF6 の道、[B-07] 黄鉄鉱を焼いて白ヒ素、[2-64] HF・塩素のもれる事故の道。ゲームの都合と DESIGN「危ない技術の扱い」で、担当が判断
- 年・人のずれ 12 は上の V-198〜V-209 に足した（AUDIT_QUEUE で回す）


## S-064（2026-10-26）翻訳係が見つけた元の文の傷（TECH_TREE の担当へ）と、ネットワークまとめ係の足りない要素（監査待ち）
**1. 元の文の傷**（source.json の ja ＝ TECH_TREE のへえ①など。10 言語の翻訳係が同じ所を指摘）
- 公開側はもう外した：tools/text_tail.py が、へえ①の末尾の **出典メモ・URL・作者名・編集メモ・余分な「）」** を書き出しのときに外す（65 件。地図・物のページ・source.json）。Wikipedia の編集者の名前（存命かもしれない人）は living_names で「Wikipedia の編集者」に。**元の TECH_TREE の文は担当が直してほしい**（直れば text_tail は要らない）
  - 余分な「）」：1-50・1-54・1-59〜1-67・1-71・1-73〜1-76・2-52・4-25・8-38・8-40・9-49・10-32・10-34・10-37・15-19
  - 出典メモ・URL・作者名：2-56・2-63・2-64・4-28・4-37・7-23・7-27・7-28・8-27・9-43・10-28・10-35・10-38・10-39・12-18・13-16・14-16・14-21・15-18・15-30・15-32・15-33（本当の歴史 1-37・1-62・13-03・15-11・B-05 にも作者名）
  - 編集メモ：0-18・2-24・2-45・3-14（CHEMISTRY #142）・4-15・4-16・7-22・8-19・9-41・10-17〜10-20・14-15・B-02・B-05
- **機械では直せない物（担当が書き直す）**
  - 途中で切れている：10-05.hee1（「はい」だけ）・I-54.name（「…回路図で考え」）
  - へえ①に①〜④がまるごと：1-26・6-02
  - 同じ文：0-09（車輪）と 0-19（ろくろ）／P-31＝11-17・P-33＝4-24・P-34＝12-16・P-37＝14-20
  - P-37（路線バス）のへえ①がターボの説明で、名前と合わない
  - 長すぎて訳せない：2-62.hee1
  - 名前に作業メモ：3-05.name「（バニラ溶鉱炉の扱い）」・6-15.name「回避」
  - 内容の確認：8-15 と 8-35 のへえ①がほぼ同じ／1-37「ニッケルは一酸化炭素と気体になり」の言い方
- 直ったら：ネットワーク担当が gen_tree_views → i18n_export。変わったキーだけ翻訳係がもう一度訳す

**2. 足りない要素とつながり**（ネットワークまとめ係、16 人の調べを幹をまたいで1つに。新しい要素 194・つながり 107、うち書きもれ 要素 4・線 31）
- 表は **docs/audit/S064_network_gaps_2026-10-26.md**（出どころ・⚠️・W・優先つき。N087 ディーコン法の反応温度は外した）。出典点検係が裏を取ってから、TECH_TREE の担当が採るかを決める（ネットワーク担当は勝手に足さない）
- **① AI までの本線の年の穴**（先に）：N001 シリコンゲート 1968・N002 LOCOS 1970・N003 Al-Cu 配線 1970・N004 デナードのスケーリング則 1974・N005 ALD 1974・N006 ひずみ Si 2003・N007 HKMG 2007・N008 マルチパターニング ⚠️・N009 フリップチップ 1964・N010 TSV/HBM ⚠️・N011 p-n 接合の発見 1940・N012 四探針法 1954・N013 パンチカード・N014 微分解析機 1931・N015 勾配降下法・N016 誤差逆伝播 1986・N017 CNN 1989・N018〜N047（A/D 変換・HDL・カルマンフィルタ・PLC・収差論・OPC・EUV 初期研究・ペリクル・ロードロック・MFC・HMDS・BHF・TMAH・IPA・シリコーン ほか）
- **② 書きもれ**（レシピや本文で使うのに前提の線が無い）：要素 N048 ライデン瓶・N005 ALD・N044 BHF・N067 カドミウム、線 L001〜L031（例：[2-37]→[2-18]、[7-10]→[2-18]、[6-05]→[9-37]、[7-22]→[7-19]、[5-14]→[8-15]、[2-64]→[5-12] など）
- 注意（表の最後）：年が割れる物（ケイ素鋼・熱風炉・回転窯 ほか）、Wikipedia だけの物（幹1・幹14/15 に多い）、危ない物は作り方を書かない、I- 番号（発見）にするか迷う物、json の確認を頼まれた物（L012・L013・L026・L031・L083・L106 → ネットワーク担当が次の時間に見る）
