package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.Multiblock;
import com.taku3.semiconductorlab.machine.MultiblockPattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * マルチブロックのうすい形（ゴースト、O-035、DESIGN「入手・操作・組み立てを分かりやすく」）：見取り図の「形」ボタンで 30 秒、
 * 世界の中の置く場所に光の点を出す。白＝まだ置いていない所、赤＝ちがうブロックの所。全部の段（クライアントの世界のブロックで調べる）
 */
public final class MultiblockGhost {
    private MultiblockGhost() {}
    public static final int SECONDS = 30;
    private static final DustParticleOptions MISSING = new DustParticleOptions(0xFFFFFF, 1.2f), WRONG = new DustParticleOptions(0xFF3030, 1.5f);
    private static BlockPos target;
    private static long until;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(MultiblockGhost::tick);
    }

    /** 見せる・やめる（同じ制御ブロックでもう一度押すとやめる） */
    public static void toggle(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (pos.equals(target) && mc.level.getGameTime() < until) { target = null; return; }
        target = pos;
        until = mc.level.getGameTime() + SECONDS * 20L;
    }

    public static boolean showing(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && pos.equals(target) && mc.level.getGameTime() < until;
    }

    private static void tick(Minecraft mc) {
        if (target == null || mc.level == null || mc.level.getGameTime() >= until || mc.level.getGameTime() % 5 != 0) return;
        if (!(mc.level.getBlockEntity(target) instanceof Multiblock.Host h)) { target = null; return; }
        MultiblockPattern p = h.multiblock().pattern();
        BlockState s = mc.level.getBlockState(target);
        Direction facing = s.hasProperty(HorizontalDirectionalBlock.FACING) ? s.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        for (int y = 0; y < p.height(); y++) {
            int[][] st = Multiblock.status(mc.level, target, facing, p, y);
            for (int z = 0; z < p.depth(); z++) for (int x = 0; x < p.width(); x++) {
                int v = st[z][x];
                if (v != Multiblock.MISSING && v != Multiblock.WRONG) continue;
                BlockPos at = p.worldPos(target, facing, x, y, z);
                var opt = v == Multiblock.MISSING ? MISSING : WRONG;
                for (int k = 0; k < 4; k++)   // 四角の角のあたりに点（うすい形に見える）
                    mc.level.addParticle(opt, at.getX() + 0.15 + 0.7 * (k & 1), at.getY() + 0.5, at.getZ() + 0.15 + 0.7 * (k >> 1), 0, 0, 0);
            }
        }
    }
}
