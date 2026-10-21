package com.taku3.semiconductorlab.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.taku3.semiconductorlab.machine.Multiblock;
import com.taku3.semiconductorlab.machine.MultiblockPattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * マルチブロックのうすい形（ゴースト、O-035 → S-029 ユーザーの決定）：置く場所に**半透明のブロック**（置くブロックの絵）を描く。
 * まだの所だけ。ちがうブロックを置いた所は赤い枠。光の点はやめた。
 * 出す・消す：制御ブロックを見てキー（初期 G、設定で変えられる）、または見取り図の「形」ボタン。組み上がったら自動で消える。
 * 重くしない：形は 10 tick に1回だけ調べ直し（描くのは覚えた一覧だけ）、カメラから 48 ブロックより遠いマスは描かない
 */
public final class MultiblockGhost {
    private MultiblockGhost() {}
    private static final double RANGE = 48;
    private static final int ALPHA = 0x80, LIGHT = 0xF000F0;
    private record Cell(BlockPos pos, BlockState show, boolean wrong) {}

    private static KeyMapping key;
    private static BlockPos target;
    private static List<Cell> cells = List.of();

    public static void register() {
        key = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.semiconductor-lab.ghost", InputConstants.Type.KEYBOARD, 71, KeyMapping.Category.MISC));   // 71＝G
        ClientTickEvents.END_CLIENT_TICK.register(MultiblockGhost::tick);
        LevelRenderEvents.COLLECT_SUBMITS.register(ctx -> {
            if (target == null || cells.isEmpty()) return;
            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack ps = ctx.poseStack();
            var mc = Minecraft.getInstance();
            var models = mc.getModelManager().getBlockStateModelSet();
            for (Cell c : cells) {
                if (Vec3.atCenterOf(c.pos).distanceToSqr(cam) > RANGE * RANGE) continue;
                ps.pushPose();
                ps.translate(c.pos.getX() - cam.x, c.pos.getY() - cam.y, c.pos.getZ() - cam.z);
                if (c.wrong) {
                    ctx.submitNodeCollector().submitCustomGeometry(ps, RenderTypes.LINES, (pose, vc) -> frame(pose, vc));
                } else {
                    TextureAtlasSprite sp = models.getParticleMaterial(c.show).sprite();
                    ctx.submitNodeCollector().submitCustomGeometry(ps, RenderTypes.entityTranslucent(sp.atlasLocation()), (pose, vc) -> cube(pose, vc, sp));
                }
                ps.popPose();
            }
        });
    }

    /** 見せる・やめる（見取り図の「形」ボタン、キー） */
    public static void toggle(BlockPos pos) {
        if (pos.equals(target)) { target = null; cells = List.of(); return; }
        target = pos;
        refresh(Minecraft.getInstance());
    }

    public static boolean showing(BlockPos pos) { return pos.equals(target); }

    private static void tick(Minecraft mc) {
        while (key != null && key.consumeClick()) {
            if (target != null) toggle(target);
            else if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK && mc.level != null
                    && mc.level.getBlockEntity(((BlockHitResult) mc.hitResult).getBlockPos()) instanceof Multiblock.Host)
                toggle(((BlockHitResult) mc.hitResult).getBlockPos());
        }
        if (target != null && mc.level != null && mc.level.getGameTime() % 10 == 0) refresh(mc);
    }

    /** 形を調べ直して、描くマスの一覧を作る（組み上がっていたら消す） */
    private static void refresh(Minecraft mc) {
        if (mc.level == null || target == null || !(mc.level.getBlockEntity(target) instanceof Multiblock.Host h)) { target = null; cells = List.of(); return; }
        MultiblockPattern p = h.multiblock().pattern();
        BlockState s = mc.level.getBlockState(target);
        Direction facing = s.hasProperty(HorizontalDirectionalBlock.FACING) ? s.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        List<Cell> out = new ArrayList<>();
        for (int y = 0; y < p.height(); y++) {
            int[][] st = Multiblock.status(mc.level, target, facing, p, y);
            for (int z = 0; z < p.depth(); z++) for (int x = 0; x < p.width(); x++) {
                int v = st[z][x];
                if (v != Multiblock.MISSING && v != Multiblock.WRONG) continue;
                var cell = p.cell(p.layers().get(y)[z].charAt(x));
                BlockState show = cell != null && cell.block() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(cell.block())).defaultBlockState() : Blocks.GLASS.defaultBlockState();
                out.add(new Cell(p.worldPos(target, facing, x, y, z), show, v == Multiblock.WRONG));
            }
        }
        if (out.isEmpty()) { target = null; cells = List.of(); return; }   // 組み上がった
        cells = out;
    }

    /** 少し小さい半透明の立方体（置くブロックの絵） */
    private static void cube(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sp) {
        float a = 0.06f, b = 0.94f, u0 = sp.getU0(), u1 = sp.getU1(), v0 = sp.getV0(), v1 = sp.getV1();
        int col = (ALPHA << 24) | 0xFFFFFF;
        float[][] faces = {   // 4 頂点（x,y,z）×面ごと と 法線
                {a, a, a, b, a, a, b, a, b, a, a, b, 0, -1, 0}, {a, b, b, b, b, b, b, b, a, a, b, a, 0, 1, 0},
                {a, a, a, a, b, a, b, b, a, b, a, a, 0, 0, -1}, {b, a, b, b, b, b, a, b, b, a, a, b, 0, 0, 1},
                {a, a, b, a, b, b, a, b, a, a, a, a, -1, 0, 0}, {b, a, a, b, b, a, b, b, b, b, a, b, 1, 0, 0}};
        float[][] uv = {{u0, v1}, {u0, v0}, {u1, v0}, {u1, v1}};
        for (float[] f : faces)
            for (int i = 0; i < 4; i++)
                vc.addVertex(pose, f[i * 3], f[i * 3 + 1], f[i * 3 + 2]).setColor(col).setUv(uv[i][0], uv[i][1])
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LIGHT).setNormal(pose, f[12], f[13], f[14]);
    }

    /** 赤い枠（ちがうブロックの所） */
    private static void frame(PoseStack.Pose pose, VertexConsumer vc) {
        float a = -0.01f, b = 1.01f;
        float[][] e = {{a, a, a, b, a, a}, {a, a, b, b, a, b}, {a, b, a, b, b, a}, {a, b, b, b, b, b},
                {a, a, a, a, b, a}, {b, a, a, b, b, a}, {a, a, b, a, b, b}, {b, a, b, b, b, b},
                {a, a, a, a, a, b}, {b, a, a, b, a, b}, {a, b, a, a, b, b}, {b, b, a, b, b, b}};
        for (float[] l : e) {
            float nx = l[3] - l[0], ny = l[4] - l[1], nz = l[5] - l[2];
            vc.addVertex(pose, l[0], l[1], l[2]).setColor(0xFFFF3030).setNormal(pose, nx, ny, nz).setLineWidth(3f);
            vc.addVertex(pose, l[3], l[4], l[5]).setColor(0xFFFF3030).setNormal(pose, nx, ny, nz).setLineWidth(3f);
        }
    }
}
