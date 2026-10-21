package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;

/**
 * 層の種類（LITHO_DESIGN §2）。プレイヤーはどの層でも「その物がある所」を描く。
 * window＝酸化膜に窓を開ける層（描いた所＝マスクの透明な所）、そうでない層（金属）は描いた所＝マスクの不透明な所。
 */
public enum LayerKind {
    /** n型の拡散（リン） */
    DIFF_N("diff_n", true, 'n'),
    /** p型の拡散（ホウ素） */
    DIFF_P("diff_p", true, 'p'),
    /** 金属と半導体をつなぐ窓 */
    CONTACT("contact", true, ' '),
    /** アルミの配線（1層目） */
    METAL("metal", false, ' '),
    /** MOS のゲートの薄い酸化膜（S-011 Q21-1 案A）：厚い酸化膜を削り、薄く（約 0.1 µm）育て直す所。上の金属がゲートになる */
    GATE_OX("gate_ox", true, ' '),
    /** 層間膜の窓（S-011、2層目の金属）：1層目と2層目の金属をつなぐ所 */
    VIA("via", true, ' '),
    /** アルミの配線（2層目、S-011） */
    METAL2("metal2", false, ' ');

    public final String id;
    /** 窓を開ける層か（ポジ型レジストでマスクの向きが決まる） */
    public final boolean window;
    /** 拡散なら型（'n'/'p'）、それ以外は ' ' */
    public final char dopes;

    LayerKind(String id, boolean window, char dopes) { this.id = id; this.window = window; this.dopes = dopes; }

    public boolean isDiffusion() { return dopes != ' '; }

    public static LayerKind byId(String id) {
        for (LayerKind k : values()) if (k.id.equals(id)) return k;
        return null;
    }

    public static final Codec<LayerKind> CODEC = Codec.STRING.xmap(LayerKind::byId, k -> k.id);

    /** lang のキー（層の名前） */
    public String langKey() { return "litho.semiconductor-lab.layer." + id; }
}
