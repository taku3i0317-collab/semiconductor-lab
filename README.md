# Semiconductor Lab（セミコンダクター・ラボ）

**日本語** ・ [English](#english)

## これは何？

遊びながら「文明がどうやってできてきたか」を学ぶ、Minecraft の教育 MOD です。
石を打ち欠いた道具・火・土器から始まり、冶金・化学・電気・真空・光学を一歩ずつ本物どおりに積み上げて、
トランジスタ、集積回路、EUV 露光装置、そして自分で作ったチップの上で動く小さな AI まで進みます。
レシピは本物の化学・物理に合わせ、作れるようになった技術ごとに「へえ」と思える本当の話（年・人物・出典つき）が読めます。

もう一つの柱が **技術ネットワーク** です。技術を「ツリー（木）」ではなく「網」として、
「何が無いと何が作れないか」「どの考え方がどの技術を説明するか」を、年・人物・出典つきで約400個のノードにまとめています。

- **技術ネットワーク図**：https://taku3i0317-collab.github.io/semiconductor-lab/network.html
  （点＝技術・モノ・発見、横＝年。押すとカード、「詳しく見る」で1ノードのページ。スマホでも見られます）
- データ：`docs/TECH_TREE.md`（技術）・`docs/products.md`（モノ）・`docs/ideas.md`（発見・考え方）・`docs/REFERENCES.md`（参考文献）。
  図とノードのページの「English」ボタンで英語に切り替えられる（ノードの名前とへえ①。ほかは日本語のまま）。
  機械が読めるまとめは `docs/tech_network.json`（線の種類 kind：requires＝前提、understanding＝なぜ動くかの説明、enables＝考え方から生まれた、part＝部品、tool＝つくるのに使った道具、same＝同じしくみ）
- このプロジェクトの作り方（AI チームの使い方：相談役＋司令塔＋作業員）：[`docs/AI_WORKFLOW.md`](docs/AI_WORKFLOW.md)

## 遊び方

1. Minecraft **26.3** と [Fabric Loader](https://fabricmc.net/use/)（0.19.5 以上）、[Fabric API](https://modrinth.com/mod/fabric-api) を入れる（Java 25）
2. この MOD の jar を `mods` フォルダに入れる（jar はリポジトリを取ってきて `gradlew build` で `build/libs/` にできます）
3. ゲームの中のガイド本と、進捗画面の「図鑑」から始めてください。アイテムの説明は Shift で詳しく、ガイドでさらに深く読めます

## 誤りを見つけたら

年・人物・つながり・出典の誤りの指摘は大歓迎です。[Issue](https://github.com/taku3i0317-collab/semiconductor-lab/issues/new/choose) から、ひな形を選んでください：

- **年・人物の誤り**
- **前提（つながり）の誤り**
- **出典の追加**

技術ネットワーク図のノードのページ（`node.html`）の「誤りを報告」ボタンからは、ノード番号が入った状態で開きます。
確かさの印：◎ 信頼できる出典2つ以上で一致／○ 1つで確認／△・⚠️ 未確認・諸説あり。

## 影響を受けた作品
科学の出典（事実の裏付け）とは別に、このプロジェクトが影響を受けた人・作品です。
- 亜留間次郎さん（薬理凶室）— 配信・動画、『アリエナイ医学事典』（三才ブックス）ほか。いちばん影響を受けました。

## ライセンス

**全部 CC0 1.0（パブリックドメイン）です。** コード・データ・文章・画像（テクスチャ・肖像のドット絵を含む）のすべてを、
誰でも、何にでも、自由に、名前を書かずに使ってかまいません（[LICENSE](LICENSE)）。
ただし、技術ネットワーク図が読み込むライブラリ d3（cdnjs から読み込み、このリポジトリには含まない）は、それぞれのライセンスに従います。

この MOD は Minecraft の公式の製品ではなく、Mojang・Microsoft とは関係ありません。

---

<a id="english"></a>
# Semiconductor Lab (English)

[日本語](#semiconductor-labセミコンダクターラボ) ・ **English**

## What is this?

An educational Minecraft mod for learning how civilization was built, by playing.
You start with knapped stone tools, fire and pottery, then climb through metallurgy, chemistry, electricity, vacuum and optics step by step, the way it really happened,
up to transistors, integrated circuits, EUV lithography, and finally a small AI running on a chip you made yourself.
Recipes follow real chemistry and physics, and each technology you unlock comes with true stories (with years, people and sources).

The second pillar is the **Technology Network**: technology as a web, not a tree.
About 400 nodes record what cannot be made without what, and which ideas explain which technologies, with years, people and sources.

- **Technology Network map**: https://taku3i0317-collab.github.io/semiconductor-lab/network.html
  (dots are technologies, products and discoveries; the horizontal axis is time. Click for a card, "詳しく見る" for a full node page. Works on phones.)
- Data: `docs/TECH_TREE.md` (technologies), `docs/products.md` (products), `docs/ideas.md` (discoveries and ideas), `docs/REFERENCES.md` (references).
  The map and node pages have an **English** button (node names and the first fun fact are translated; other text is still Japanese). A machine-readable bundle is `docs/tech_network.json` (edge kinds: requires, understanding, enables, part, tool = a tool used in designing it, same = the same mechanism). (The texts are mostly in Japanese; in-game item descriptions also exist in English.)
- How this project is built (an AI team: advisor + conductor + workers): [`docs/AI_WORKFLOW.md`](docs/AI_WORKFLOW.md) (Japanese, with an English summary)

## How to play

1. Install Minecraft **26.3**, [Fabric Loader](https://fabricmc.net/use/) (0.19.5 or later) and [Fabric API](https://modrinth.com/mod/fabric-api) (Java 25)
2. Put the mod jar into your `mods` folder (build it from this repository with `gradlew build`; it appears in `build/libs/`)
3. Start with the in-game guide book and the "Zukan" (encyclopedia) tab in the advancements screen. Hold Shift on items for more detail

## Found a mistake?

Corrections to years, people, connections and sources are very welcome. Open an [issue](https://github.com/taku3i0317-collab/semiconductor-lab/issues/new/choose) and pick a template:

- **Wrong year or person**
- **Wrong prerequisite (connection)**
- **Add a source**

The "誤りを報告" (report an error) buttons on each node page (`node.html`) open the template with the node number already filled in.
Certainty marks: ◎ confirmed by two or more reliable sources / ○ one source / △ or ⚠️ unconfirmed or disputed.

## Inspirations
Separate from the scientific sources, these are the people and works that inspired this project.
- Jiro Aruma (Yakuri Kyoshitsu) — streams and videos, "Arienai Igaku Jiten" (Sansai Books) and more. Our biggest influence.

## License

**Everything is CC0 1.0 (public domain).** Code, data, text and images (including textures and the pixel-art portraits)
may be used by anyone, for anything, freely, without attribution ([LICENSE](LICENSE)).
The d3 library that the network map loads from cdnjs is not part of this repository and keeps its own license.

This mod is not an official Minecraft product and is not affiliated with Mojang or Microsoft.
