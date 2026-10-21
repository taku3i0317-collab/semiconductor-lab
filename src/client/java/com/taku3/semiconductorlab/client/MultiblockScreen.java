package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.Multiblock;
import com.taku3.semiconductorlab.machine.MultiblockPattern;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * マルチブロックの層ごとの見取り図（v1.0 #1、O-034 Q33-2、DESIGN「見せる」1）：制御ブロックをしゃがんで右クリック。
 * 上下のボタンで段を選ぶ。マス＝緑 合っている・赤 足りない・黄 ちがうブロック・灰 何でもよい。マスにマウスを乗せると名前（lang multiblock.semiconductor-lab.&lt;名前&gt;）。
 * クライアントの世界のブロックで調べる（サーバーと通信しない）
 */
public class MultiblockScreen extends Screen {
    private static final int CELL = 16, OK = 0xFF40B040, MISSING = 0xFFD04040, WRONG = 0xFFE0C030, FREE = 0xFF707070, BG = 0xFF2A2A30;
    private final BlockPos pos;
    private int layer;

    public MultiblockScreen(BlockPos pos) { super(Component.translatable("multiblock.semiconductor-lab.view")); this.pos = pos; }

    private MultiblockPattern pattern() {
        return minecraft != null && minecraft.level != null && minecraft.level.getBlockEntity(pos) instanceof Multiblock.Host h ? h.multiblock().pattern() : null;
    }
    private Direction facing() {
        BlockState s = minecraft.level.getBlockState(pos);
        return s.hasProperty(HorizontalDirectionalBlock.FACING) ? s.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }
    private int left(MultiblockPattern p) { return (width - p.width() * CELL) / 2; }
    private int top(MultiblockPattern p) { return (height - p.depth() * CELL) / 2; }

    @Override protected void init() {
        MultiblockPattern p = pattern();
        if (p == null) return;
        int x = left(p) + p.width() * CELL + 8, y = top(p);
        addRenderableWidget(Button.builder(Component.literal("▲"), b -> layer = Math.min(p.height() - 1, layer + 1)).bounds(x, y, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("▼"), b -> layer = Math.max(0, layer - 1)).bounds(x, y + 24, 20, 20).build());
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        MultiblockPattern p = pattern();
        if (p == null) { onClose(); return; }
        int l = left(p), t = top(p);
        g.fill(l - 6, t - 6, l + p.width() * CELL + 6, t + p.depth() * CELL + 6, BG);
        int[][] st = Multiblock.status(minecraft.level, pos, facing(), p, layer);
        for (int z = 0; z < p.depth(); z++) for (int x = 0; x < p.width(); x++) {
            int c = switch (st[z][x]) { case Multiblock.OK -> OK; case Multiblock.MISSING -> MISSING; case Multiblock.WRONG -> WRONG; default -> FREE; };
            g.fill(l + x * CELL, t + z * CELL, l + (x + 1) * CELL - 1, t + (z + 1) * CELL - 1, c);
        }
        for (int k = 0; k < p.height(); k++)   // 段の目印（左に、今の段は明るく）
            g.fill(l - 14, t + (p.height() - 1 - k) * 6, l - 9, t + (p.height() - 1 - k) * 6 + 4, k == layer ? 0xFFFFFFFF : 0xFF606060);
        int cx = (mouseX - l) / CELL, cz = (mouseY - t) / CELL;
        if (mouseX >= l && mouseY >= t && cx < p.width() && cz < p.depth()) {
            char ch = p.layers().get(layer)[cz].charAt(cx);
            var cell = p.cell(ch);
            String key = ch == p.controller() ? "multiblock.semiconductor-lab.controller" : ch == MultiblockPattern.AIR ? "multiblock.semiconductor-lab.inside" : cell == null ? null : "multiblock.semiconductor-lab." + cell.name();
            if (key != null) g.setTooltipForNextFrame(Component.translatable(key), mouseX, mouseY);
        }
    }
}
