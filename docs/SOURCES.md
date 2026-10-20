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
