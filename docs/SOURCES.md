# SOURCES.md — 出典と要確認リスト

## 一次・標準的な資料
- USGS Mineral Commodity Summaries（毎年）: Germanium / Indium / Graphite の項（産源・副産物回収）
- Pfann, W.G. (1952) "Principles of Zone-Melting", Trans. AIME 194 ／ Pfann (1958) *Zone Melting*（帯域精製・ゾーンレベリング）
- Riordan & Hoddeson (1997) *Crystal Fire*（トランジスタ史：点接触1947・接合型1951・合金型1952）
- Computer History Museum "The Silicon Engine" 年表（無料）
- 高校〜大学初年の無機化学教科書（接触法・ヴェーラー法・亜鉛の湿式精錬）
- 薬理凶室『アリエナイ理科ノ教科書』シリーズ（硫酸・リンの歴史的製法の参考）

## 要確認（本文に使う前に確認する）
- [ ] Ge帯域精製の到達純度（10N 前後？）→ 11N 表記を修正
- [ ] 初期Geトランジスタの実際のn型ドーパント（Sb/As が主か）
- [ ] 層流クリーンルームの発明年（Whitfield, Sandia, 1960-62?）
- [ ] 1954 Regency TR-1 の構成（NPN成長接合型×4）／1955 Sony TR-55
- [ ] Ge濃縮石炭の産地・含有量（雲南省ほか）／閃亜鉛鉱中の Ge・In 含有率
- [ ] CP-4 エッチング液の組成（HNO3/HF/CH3COOH）
- [ ] 硫酸浸出でのGe回収の妥当性（塩酸ルートとの差）
- [ ] クーリッジの延性タングステンの年（研究は1906〜、成功1909〜1910、GE の電球発売1910〜1911、特許1913）。ゲーム内は「1909〜1910」と表記
- [ ] 1783 エルヤル兄弟（W の単離）・1904 ユストとハナマン（押し出し W フィラメント）・ディナスれんが 1822 W.W.ヤング
## 技術ツリーの参考文献
- Lewis Dartnell *The Knowledge* (2014) 邦訳『この世界が消えたあとの科学文明のつくりかた』: 文明再建の最短ルート、ボトルネック技術。幹1〜4の照合に使う
- Ryan North *How to Invent Everything* (2018) 邦訳『ゼロからつくる科学文明』: 技術依存チャート、各技術の「本当はいつ可能だったか」
- Thomas Thwaites *The Toaster Project* (2011): 鉱石からの手作りの現実
- James Burke *Connections* (1978): 技術史のつながり
- 注: いずれも露光装置・半導体製造までは扱わない。幹5〜9は本MOD独自

## [5-15] シュプレンゲル水銀ポンプ（タスク12、2026-10-20）
- H. Sprengel, "Researches on the vacuum", J. Chem. Soc. 18 (1865) 9–21：原論文。0.5L の容器を約20分で排気
- Wikipedia "Sprengel pump"（https://en.wikipedia.org/wiki/Sprengel_pump）：「1 mPa 未満」と書くが要出典の印あり。エジソン・スワンの電球、クルックスの直列使用。出典に S. P. Thompson *The Development of the Mercurial Air-pump* (1888)、F. Jehl *Menlo Park Reminiscences* (1937)
- A. Sella, "Classic Kit: Sprengel pump", Chemistry World (2008)（https://www.chemistryworld.com/opinion/classic-kit-sprengel-pump/3004941.article）：シュプレンゲル本人は 10⁻³ Torr 程度と見積もった（測る手段なし）。マクラウド計（1874）以後 10⁻⁶ Torr 級。エジソンは数台で 10⁻³ Torr
- ゲームの値：到達 0.05Pa（≈4×10⁻⁴ Torr、1台・短時間として上の2つのあいだ）、封止 0.1Pa（≈7.5×10⁻⁴ Torr、初期の軟真空管）
- ❓ 要確認：エジソンの電球1個あたりの排気時間、オーディオン（1906）の実際の封入圧

## [6-07] 空気分離（タスク12、2026-10-20）
- Linde Engineering「Air separation plants」（https://www.linde-engineering.com/products-and-services/process-plants/air-separation-plants）：1902 最初の空気分離装置（単塔で O2）、1904 N2、アルゴンの精留
- 「Oxygen Specific Power Consumption Comparison for Air Separation Units」（ResearchGate 287708500）：理論最小 0.33 kWh/m³(O2)、実機は 0.46 kWh/m³ 以上 → ゲームの電力に使用
- 乾燥空気の組成（N2 78.08・O2 20.95・Ar 0.93・CO2 0.04 vol%）：標準的な値
- ❓ 要確認：1900〜1910年代のリンデ装置の電力（kWh/m³ O2）、アルゴンを工業的に取り出し始めた年

## [1-46] 珪石（石英鉱脈）（タスク13、2026-10-20）
- USGS Mineral Commodity Summaries 2025「Quartz (high-purity)」（https://pubs.usgs.gov/periodicals/mcs2025/mcs2025-quartz.pdf）：スプルースパイン（ノースカロライナ）の高純度石英はペグマタイト由来、長石・雲母と同時に採掘
- USGS Mineral Commodity Summaries 2026「Quartz」（https://pubs.usgs.gov/periodicals/mcs2026/mcs2026-quartz.pdf）：高純度石英＝不純物 100ppm 未満（超高純度は 10ppm 未満）。用途：電子材料・光ファイバー・シリコン単結晶用の石英るつぼ・ランプ管
- 脈石英を高純度石英の原料として評価した研究：Fenkina-Lampi 鉱床（カレリア）の脈石英の選鉱（https://zrmo.org/archives/2021/issue-6/mineral-processing-of-vein-quartz-from-the-fenkina-lampi-deposit-karelia.html）、ペシャワール盆地の石英脈（https://ejm.copernicus.org/articles/37/151/2025/）
- 地殻の鉱物組成（石英 約12%、長石に次ぐ）：鉱物学の標準的な値
- ゲームの値（y−56〜96・大きさ12・10回）は「どこにでもあるが掘りに行く必要がある」量として決めた（産状の厳密な再現ではない）

## [9-07] Ge 単結晶の引き上げ（タスク14、2026-10-20）
- G. K. Teal, J. B. Little, Phys. Rev. 78, 647 (1950)：原論文（ゲルマニウム単結晶の成長）。本文は未確認 ❓
- E. E. Haller「Germanium: From Its Discovery to SiGe Devices」（https://www.osti.gov/servlets/purl/922705）：ティールが「多結晶で十分」という反対を1年以上かけて説得、1950 年に報告、チョクラルスキーの考えに沿った装置。超高純度 Ge の引き上げでは純水素の雰囲気だけがよい結果、黒鉛のサセプターを高周波で加熱
- arXiv 2511.20879（高純度 Ge の偏析係数）：回転 3rpm、引き上げ 40mm/h（ネッキング）→ 20mm/h（本体）
- arXiv 1907.05067（Ge 単結晶、直径45mm）：回転 15〜20rpm、引き上げ 20〜25mm/h
- 特許 US2703296（ベル研、ゲルマニウムの種から棒状の結晶を引き上げる）（https://patents.google.com/patent/US2703296）
- ゲームの値：1本 60秒（本物は数時間）、3kW（帯域精製炉の発振器と同じ）、H2 1袋/回。❓ 1950年の装置の引き上げ速度・回転数は原論文で要確認

## [3-15] アーク炉・[1-15] 金属シリコン（タスク15、2026-10-20）
- docs/REFERENCES.md §1（USGS シリコン統計 https://www.usgs.gov/centers/national-minerals-information-center/silicon-statistics-and-information 、PV-Manufacturing.org https://pv-manufacturing.org/silicon-production/）：SiO2 + 2C → Si + 2CO、約2000℃、98〜99%、1kg あたり 10kWh 以上
- ゲームの値：12kWh/kg（現代の炉の 11〜13kWh の中ほど）、500kW、電極は8回で1本
- ❓ 要確認：モアッサン（1892）の電気炉は直流ダイナモで動いたか、電気炉による金属シリコンの工業生産が始まった年（1900年ごろ）

## [1-16] トリクロロシラン・[1-17] シーメンス法（タスク22、2026-10-20）
- PV-Manufacturing.org「Silicon production」https://pv-manufacturing.org/silicon-production/ ：Si + 3HCl → SiHCl3 + H2（約300℃、流動層）、蒸留、ベル型反応炉で約1100℃の細い棒に析出、HCl の再利用
- Fraunhofer ISE（ポリシリコン製造の解説・報告）：TCS 合成は約300℃の流動層、析出は約1100℃のベル型反応炉
- 米国特許 US 8,398,946（多結晶シリコンの製造、比較例）：シーメンス炉 1030℃前後・析出速度 約1.2mm/時・電力 約90kWh/kg → ゲームの 500kW×648秒＝90kWh/kg の根拠。現代の炉は 40〜60kWh/kg まで下がった（⚠️ 要確認）
- 沸点（化学の標準値）：SiHCl3 31.8℃、BCl3 12.6℃、PCl3 76℃、SiCl4 57.6℃
- 要確認：シーメンス社の最初の特許（1950年代、年と番号）

## [9-11] ドーパント源・[9-08] CZ 法（タスク23、2026-10-20）
- 米国特許 US 5,073,229（Semiconductor crystal pulling method）https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/5073229 ：Si 中の偏析係数 P 0.35、B 0.80
- キール大学 材料科学（H. Föll）「Segregation」https://www.tf.uni-kiel.de/matwis/amat/iss/kap_6/illustr/s6_2_2.html ：多くのドーパントは k<1（融液に押し出される）、B は 1 に近く偏析が小さい。石英るつぼが少しずつ溶けて融液の酸素が増える
- 国立台湾大学の論文（太陽電池用 CZ のホットゾーン改良）https://scholars.lib.ntu.edu.tw/bitstream/123456789/64459/1/35.pdf ：電力 59.1 → 17.4 kWh/kg、引き上げ速度 0.8 → 1.32 mm/分、Ar 93.3 → 27.1 立方フィート/kg（⚠️ 要確認：原文の条件）
- 引き上げ機の仕様例（ロシアのメーカー、250mm 用）https://en.gkmp32.com/rost/ustanovki-metodom-Chohralskogo/ ：引き上げ 0.1〜8 mm/分、種の回転 1〜30rpm、るつぼの回転 1〜20rpm、ヒーター最大 150kW
- Si の融点 1414℃（標準値）。融液は約1420℃に保つ

## [9-12] ウェハー切断・[9-16] 熱拡散・[B-12] 太陽電池（IMP-25、2026-10-20）
- ETHW「Milestones: First Practical Photovoltaic Solar Cell」https://ethw.org/Milestones:First_Practical_Photovoltaic_Solar_Cell （John Perlin『From Space to Earth』3章の要約）：1954年、結晶から細長い板を切る、ヒ素で n型、ホウ素で薄い p型の表面層、光る表面の反射をくもった樹脂の膜で減らす、効率約6%、1平方ヤードあたり約50W。リチウムで作った最初の電池は接合が動いて不安定だった。フラーは以前リンを蒸着した試料も作った
- PVEducation「Early Silicon Cells」https://www.pveducation.org/pvcdrom/manufacturing-si-cells/early-silicon-cells ：1954年の電池は結晶成長と拡散による接合で作られ、電極は2つとも裏側、効率6%。原論文 Chapin, Fuller, Pearson, J. Appl. Phys. 25, 676 (1954)
- 要確認：1954年の電極の金属・拡散の温度と時間（原論文は有料で未確認）
