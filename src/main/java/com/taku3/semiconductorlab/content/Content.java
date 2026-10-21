package com.taku3.semiconductorlab.content;

/**
 * 幹ごとのファイルを全部読み込む（IMP-28）。新しい幹のファイルを作ったら、ここに1行足す。
 * 1つのノードを足すときは、そのノードの幹のファイル（と lang の src/main/lang/&lt;幹&gt;/、テストの gametest/&lt;幹&gt;Tests）だけを書き換える。
 * <pre>
 * 幹0 Survival 生存・道具     幹1 Metallurgy 冶金      幹2 Chemistry 化学       幹3 Heat 熱（炉と温度）
 * 幹4 Power 動力・電気        幹5 Vacuum 真空          幹6 Gas ガス・電解       幹7 Optics 光学
 * 幹8 Precision 精密機械・計測  幹9 Semiconductor 半導体プロセス  幹10 Control 制御・計算
 * 幹11 Agriculture 農業・食品  幹12 Construction 建設・測量  幹14 Transport 交通・運輸
 * 幹15 Communication 通信・情報  枝🌿 Branches（[B-..] など、EUV の前提にならない製品）
 * </pre>
 * 幹10〜14 のファイルは、その幹の最初のノードを実装するときに作る（中身が空のファイルは置かない）。
 */
public final class Content {
    private Content() {}

    public static void initialize() {
        // 読み込む順＝クリエイティブタブに並ぶ順（幹の番号順）
        Survival.init();
        Metallurgy.init();
        Chemistry.init();
        Heat.init();
        Power.init();
        Vacuum.init();
        Gas.init();
        Optics.init();
        Precision.init();
        Semiconductor.init();
        Control.init();   // 幹10（IMP4-01 O-011、[10-01] 電磁石）
        Agriculture.init();   // 幹11（IMP2 S-003）
        Construction.init();   // 幹12（IMP2 O-007）
        Transport.init();   // 幹14（IMP2 O-023、[14-03] クロノメーター）
        Communication.init();
        Branches.init();
        Testing.init();   // S-001 試験場の試験用ブロック（IMP3）
        // 組立台のレシピ（IMP-33）：ほかの幹のアイテムを使うので、全部の幹を読み込んでから。製品の幹のファイルに書く
        Power.assemblies();
        Communication.assemblies();
        Branches.assemblies();   // [B-08] 電卓（S-010）
        Reg.registerTabs();
    }
}
