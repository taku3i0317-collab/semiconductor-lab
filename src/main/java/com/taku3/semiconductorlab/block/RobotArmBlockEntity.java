package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Control;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * [10-26] 産業用ロボット（O-037、Q36-2 案A 仮決定）：1961 ユニメート。覚えた「取る機械 → 置く機械」をくり返す腕。
 * <ul>
 * <li>教える：ティーチングペンダント（{@link com.taku3.semiconductorlab.item.TeachPendantItem}）で腕を右クリック → 取る機械 → 置く機械 の順に右クリック。
 *     しゃがんで腕を右クリックで忘れる</li>
 * <li>覚えられる組：{@link #BASE_STEPS}（本物は磁気ドラムに記憶 ⚠️）。スロットに自作のマイコン [10-14] を入れると {@link #MCU_STEPS}</li>
 * <li>動き：1 秒に1回、届く範囲（{@link #REACH} ブロック）の機械の出口から最大 {@link #GRAB} 個取って、相手の入口に入れる（Fabric Transfer API、machine/MachineItems）。
 *     ホッパーでつなげない向き・距離の機械どうしをつなぐ。電気 {@link #WATTS} W（本物は油圧で動いた ⚠️）</li>
 * </ul>
 */
public class RobotArmBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20, REACH = 3, GRAB = 16, BASE_STEPS = 1, MCU_STEPS = 8;
    public static final int SLOT_MCU = 0, SLOT_COUNT = 1;
    public static final int DATA_STEPS = DATA_COUNT, DATA_CAPACITY = DATA_COUNT + 1, DATA_TEACH = DATA_COUNT + 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT + 3)
            .in(SLOT_MCU, 8, 35, s -> s.is(Control.MICROCOMPUTER), 1)
            .standardTexts()
            .bottom("program", "gui.semiconductor-lab.robot_arm.program", "gui.semiconductor-lab.robot_arm.teaching_from", "gui.semiconductor-lab.robot_arm.teaching_to").samples("8", "8")
            .build();

    /** 覚えた組（取る所・置く所） */
    private final List<BlockPos[]> program = new ArrayList<>();
    private int next;
    /** 教えている途中：0＝いいえ・1＝取る機械を待つ・2＝置く機械を待つ */
    private int teachStep;
    private BlockPos teachFrom;

    public RobotArmBlockEntity(BlockPos pos, BlockState state) {
        super(Control.ROBOT_ARM.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 40, WATTS);
    }

    public int capacity() { return container.getItem(SLOT_MCU).is(Control.MICROCOMPUTER) ? MCU_STEPS : BASE_STEPS; }
    public List<BlockPos[]> program() { return List.copyOf(program); }
    public int teachStep() { return teachStep; }
    public boolean inReach(BlockPos p) { return !p.equals(worldPosition) && Math.max(Math.abs(p.getX() - worldPosition.getX()), Math.max(Math.abs(p.getY() - worldPosition.getY()), Math.abs(p.getZ() - worldPosition.getZ()))) <= REACH; }

    /** ペンダントで腕を右クリック：教え始める（しゃがむと忘れる） */
    public void startTeaching(Player p, boolean clear) {
        if (clear) { program.clear(); next = 0; teachStep = 0; setChanged(); msg(p, "cleared"); return; }
        teachStep = 1; teachFrom = null;
        msg(p, "teach_from");
    }

    /** ペンダントで機械を右クリック（教えている途中の腕に送られる）。使ったら true */
    public boolean teach(Player p, BlockPos at) {
        if (teachStep == 0) return false;
        if (!inReach(at)) { msg(p, "too_far"); return true; }
        if (ItemStorage.SIDED.find(level, at, Direction.UP) == null) { msg(p, "not_machine"); return true; }
        if (teachStep == 1) { teachFrom = at.immutable(); teachStep = 2; msg(p, "teach_to"); return true; }
        while (program.size() >= capacity()) program.remove(0);   // いっぱいなら古い組を忘れる
        program.add(new BlockPos[] {teachFrom, at.immutable()});
        teachStep = 0; teachFrom = null; setChanged();
        if (p != null) p.sendSystemMessage(Component.translatable("message.semiconductor-lab.robot_arm.learned", program.size(), capacity()));
        return true;
    }

    private static void msg(Player p, String k) { if (p != null) p.sendSystemMessage(Component.translatable("message.semiconductor-lab.robot_arm." + k)); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    /** i 番の組で今動かせる個数（試すだけ） */
    private long movable(int i, boolean commit) {
        BlockPos[] pair = program.get(i);
        var from = ItemStorage.SIDED.find(level, pair[0], Direction.DOWN);
        var to = ItemStorage.SIDED.find(level, pair[1], Direction.UP);
        if (from == null || to == null) return 0;
        try (Transaction tx = Transaction.openOuter()) {
            long n = StorageUtil.move(from, to, v -> true, GRAB, tx);
            if (commit) tx.commit();
            return n;
        }
    }

    @Override protected boolean canWork() {
        while (program.size() > capacity()) program.remove(0);   // マイコンを外したら最初の組だけ残す
        for (int k = 0; k < program.size(); k++) if (movable((next + k) % program.size(), false) > 0) return true;
        return false;
    }

    @Override protected void finishWork() {
        for (int k = 0; k < program.size(); k++) {
            int i = (next + k) % program.size();
            if (movable(i, true) > 0) { next = (i + 1) % program.size(); return; }
        }
    }

    @Override protected int extraDataCount() { return 3; }
    @Override protected int extraData(int i) {
        return switch (i) { case DATA_STEPS -> program.size(); case DATA_CAPACITY -> capacity(); case DATA_TEACH -> teachStep; default -> 0; };
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        int[] a = new int[program.size() * 6];   // 組ごとに 取る所 x,y,z・置く所 x,y,z
        for (int i = 0; i < program.size(); i++) for (int j = 0; j < 2; j++) {
            BlockPos q = program.get(i)[j];
            a[i * 6 + j * 3] = q.getX(); a[i * 6 + j * 3 + 1] = q.getY(); a[i * 6 + j * 3 + 2] = q.getZ();
        }
        out.putIntArray("Program", a);
        out.putInt("Next", next);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        program.clear();
        int[] a = in.getIntArray("Program").orElse(new int[0]);
        for (int i = 0; i + 5 < a.length; i += 6) program.add(new BlockPos[] {new BlockPos(a[i], a[i + 1], a[i + 2]), new BlockPos(a[i + 3], a[i + 4], a[i + 5])});
        next = in.getIntOr("Next", 0);
    }
}
