package com.taku3.semiconductorlab.machine;

import com.taku3.semiconductorlab.SemiconductorLab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * マルチブロックの土台（v1.0 #1、O-034 Q33-2）：制御ブロックの BE が1つ持つ部品（高炉・EUV 露光装置など、親クラスがちがっても使える）。
 * <ul>
 * <li>形の判定：{@link #CHECK_EVERY} tick に1回（{@link #tick}）、テストからは {@link #recheck}。組み上がっていないと host は止まる（中身は残る）</li>
 * <li>口ブロック（{@link PortBlockEntity}：電気・物・ガス）：形の中の「囲い」の場所に置ける。組み上がると口が制御ブロックにつながる</li>
 * <li>はじめて組み上がった瞬間：音・近くの人にメッセージ・進捗「はじめての大きな装置」（1回だけ、保存する）</li>
 * <li>見取り図：{@link #status} がマスごとの 合っている／足りない／ちがう を返す（client の MultiblockScreen がしゃがんで右クリックで出す）</li>
 * </ul>
 */
public final class Multiblock {
    /** 制御ブロックの BE が実装する */
    public interface Host { Multiblock multiblock(); }
    /** 物の口：つながった制御ブロック（E のロードポートは、となりがこれなら controller() に入れる・出す） */
    public interface ItemPort { MachineBE controller(); }

    public static final int CHECK_EVERY = 20;
    /** マスの状態（見取り図の色）：合っている・足りない（空気）・ちがうブロック・何でもよい */
    public static final int OK = 0, MISSING = 1, WRONG = 2, FREE = 3;
    /** しゃがんで右クリックで見取り図を開く（クライアントの初期化で入れる） */
    public static java.util.function.Consumer<BlockPos> openView = p -> {};

    private final Supplier<MultiblockPattern> pattern;
    private boolean formed, announced;
    private MultiblockPattern.Result last;
    private final List<BlockPos> ports = new ArrayList<>();

    public Multiblock(Supplier<MultiblockPattern> pattern) { this.pattern = pattern; }

    public MultiblockPattern pattern() { return pattern.get(); }
    public boolean formed() { return formed; }
    public boolean announced() { return announced; }
    public MultiblockPattern.Result last() { return last; }
    public List<BlockPos> ports() { return List.copyOf(ports); }

    /** 20 tick に1回（またはまだ調べていないとき）調べる */
    public void tick(Level level, BlockPos pos, Direction facing) {
        if (level.getGameTime() % CHECK_EVERY == 0 || last == null) recheck(level, pos, facing);
    }

    /** 形を調べ直す。組み上がっていれば口をつなぎ、はじめてなら知らせる */
    public MultiblockPattern.Result recheck(Level level, BlockPos pos, Direction facing) {
        if (level == null) return null;
        MultiblockPattern p = pattern.get();
        last = p.check(level, pos, facing);
        formed = last.formed();
        ports.clear();
        if (formed) {
            for (int y = 0; y < p.height(); y++) for (int z = 0; z < p.depth(); z++) for (int x = 0; x < p.width(); x++) {
                BlockPos at = p.worldPos(pos, facing, x, y, z);
                if (level.getBlockEntity(at) instanceof PortBlockEntity port) { port.link(pos); ports.add(at); }
            }
            if (!announced && level instanceof ServerLevel sl) { announced = true; announce(sl, pos); }
        }
        return last;
    }

    private static void announce(ServerLevel sl, BlockPos pos) {
        sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1f);
        Component name = sl.getBlockState(pos).getBlock().getName();
        var adv = sl.getServer().getAdvancements().get(SemiconductorLab.id("multiblock/formed"));
        for (ServerPlayer p : sl.players()) {
            if (p.blockPosition().distSqr(pos) > 24 * 24) continue;
            p.sendSystemMessage(Component.translatable("multiblock.semiconductor-lab.formed", name));
            if (adv != null) p.getAdvancements().award(adv, "formed");
        }
    }

    /** 見取り図：段 y の各マスの状態（[z][x]、{@link #OK}・{@link #MISSING}・{@link #WRONG}・{@link #FREE}） */
    public static int[][] status(Level level, BlockPos pos, Direction facing, MultiblockPattern p, int y) {
        int[][] out = new int[p.depth()][p.width()];
        for (int z = 0; z < p.depth(); z++) for (int x = 0; x < p.width(); x++) {
            char c = p.layers().get(y)[z].charAt(x);
            BlockState s = level.getBlockState(p.worldPos(pos, facing, x, y, z));
            if (c == MultiblockPattern.ANY || c == p.controller()) out[z][x] = c == p.controller() ? OK : FREE;
            else if (c == MultiblockPattern.AIR) out[z][x] = s.isAir() ? OK : WRONG;
            else { var cell = p.cell(c); out[z][x] = cell != null && cell.ok().test(s) ? OK : s.isAir() ? MISSING : WRONG; }
        }
        return out;
    }

    /** ガスの口のとなりのガスホルダーから取り出す（口が無ければ 0） */
    public int extractGas(Level level, com.taku3.semiconductorlab.block.GasType g, int max) {
        int got = 0;
        for (BlockPos port : ports) {
            if (!(level.getBlockEntity(port) instanceof PortBlockEntity pe) || pe.kind() != PortBlockEntity.Kind.GAS) continue;
            for (Direction d : Direction.values()) {
                if (got >= max) return got;
                if (level.getBlockEntity(port.relative(d)) instanceof com.taku3.semiconductorlab.block.GasHolderBlockEntity h) got += h.extract(g, max - got);
            }
        }
        return got;
    }

    public void save(ValueOutput out) { out.putBoolean("MultiblockAnnounced", announced); }
    public void load(ValueInput in) { announced = in.getBooleanOr("MultiblockAnnounced", false); }
}
