package com.taku3.semiconductorlab.item;

/**
 * ゲーム機の中の卓球（[B-09]、S-017 Q27 案A ポン型）：1972 アタリのポンと同じく、玉と板の位置をカウンタで数え、ぶつかりを比べる回路で決める（ここでは Java ⚠️、
 * 組んだチップ＝カウンタ・全加算器・レジスタはお題で確かめ済み）。左がプレイヤー、右がかんたんな自動。先に {@link #WIN} 点で勝ち。
 * 座標はコートのマス（横 {@link #W}・縦 {@link #H}）。1回の {@link #step} ＝ 1 tick
 */
public final class PongLogic {
    public static final int W = 128, H = 80, PAD = 16, BALL = 3, PAD_X_LEFT = 4, PAD_X_RIGHT = W - 6, WIN = 9;
    /** 板の速さ（プレイヤー・自動）。自動はプレイヤーより遅い＝勝てる */
    static final int PLAYER_SPEED = 4, AI_SPEED = 2;

    public int left = (H - PAD) / 2, right = (H - PAD) / 2, bx, by, vx, vy, scoreLeft, scoreRight;
    private int serves;

    public PongLogic() { serve(1); }

    /** 真ん中から玉を出す（dir＝+1 右へ、−1 左へ）。縦の向きは交互 */
    void serve(int dir) {
        bx = W / 2; by = H / 2;
        vx = 2 * dir; vy = (serves++ % 2 == 0) ? 1 : -1;
    }

    public boolean over() { return scoreLeft >= WIN || scoreRight >= WIN; }

    /** 1 tick 進める。playerTarget＝プレイヤーの板の上の端を合わせたい位置（マウス） */
    public void step(int playerTarget) {
        if (over()) return;
        left += Math.max(-PLAYER_SPEED, Math.min(PLAYER_SPEED, clamp(playerTarget) - left));
        int aiTarget = clamp(by - PAD / 2);
        right += Math.max(-AI_SPEED, Math.min(AI_SPEED, aiTarget - right));
        bx += vx; by += vy;
        if (by <= 0) { by = 0; vy = Math.abs(vy); }                      // 上の壁
        if (by + BALL >= H) { by = H - BALL; vy = -Math.abs(vy); }       // 下の壁
        if (vx < 0 && bx <= PAD_X_LEFT + 2 && bx > PAD_X_LEFT - 2 && hits(left)) bounce(left, 1);
        if (vx > 0 && bx + BALL >= PAD_X_RIGHT && bx + BALL < PAD_X_RIGHT + 4 && hits(right)) bounce(right, -1);
        if (bx < 0) { scoreRight++; serve(-1); }
        if (bx + BALL > W) { scoreLeft++; serve(1); }
    }

    private boolean hits(int pad) { return by + BALL > pad && by < pad + PAD; }

    /** 板に当たったら向きを変え、当たった所（板の端ほど）で縦の速さを変える（本物のポンも板を8つに分けて角度を変えた） */
    private void bounce(int pad, int dir) {
        vx = dir * Math.min(4, Math.abs(vx) + 0);
        int off = (by + BALL / 2) - (pad + PAD / 2);
        vy = Math.max(-3, Math.min(3, off / 3));
        if (vy == 0) vy = off >= 0 ? 1 : -1;
    }

    private static int clamp(int y) { return Math.max(0, Math.min(H - PAD, y)); }
}
