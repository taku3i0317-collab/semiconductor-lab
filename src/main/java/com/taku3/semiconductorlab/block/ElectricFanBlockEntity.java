package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 電気のファン（扇風機）[4-18]（1882 ウィーラー。IMP4-01、O-030 案A 仮決定）：電動モーター [4-08] の軸に羽根を付け、正面へ風を送る。
 * 電気 {@link #WATTS} W ⚠️ の間、正面の {@link #REACH} マス（ふさがった所まで）の物・生き物・落ちている物を押す。
 * レッドストーンは使わない。
 */
public class ElectricFanBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 50, PUSH = 0.06;
    public static final int REACH = 5;

    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT + 1)
            .right("power", 17, 1, "gui.semiconductor-lab.electric_fan.power").samples("0.5 kW")
            .right("reach", 28, 1, "gui.semiconductor-lab.electric_fan.reach").samples("5")
            .right("hint", 40, 3, "gui.semiconductor-lab.electric_fan.need_power", "gui.semiconductor-lab.electric_fan.running")
            .bottom("recipe", "gui.semiconductor-lab.electric_fan.recipe")
            .build();

    private int reach;

    public ElectricFanBlockEntity(BlockPos pos, BlockState state) {
        super(Power.ELECTRIC_FAN.type, pos, state, 0, 1_000, WATTS);
    }

    /** 正面の向き */
    public Direction facing() {
        BlockState s = getBlockState();
        return s.hasProperty(HorizontalDirectionalBlock.FACING) ? s.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }

    /** 風が届くマスの数（正面の空いている所、ふさがったブロックの手前まで） */
    public static int reach(Level level, BlockPos pos, Direction d) {
        int n = 0;
        while (n < REACH && level.getBlockState(pos.relative(d, n + 1)).getCollisionShape(level, pos.relative(d, n + 1)).isEmpty()) n++;
        return n;
    }

    /** 1 tick 分、正面の物を押す（テストからも呼ぶ）。押した数 */
    public static int blow(Level level, BlockPos pos, Direction d) {
        int n = reach(level, pos, d);
        if (n == 0) return 0;
        BlockPos far = pos.relative(d, n);
        AABB box = new AABB(Vec3.atLowerCornerOf(pos.relative(d)), Vec3.atLowerCornerOf(far).add(1, 1, 1));
        int pushed = 0;
        for (Entity e : level.getEntitiesOfClass(Entity.class, box, e -> !e.isSpectator() && e.isPushable() || e instanceof net.minecraft.world.entity.item.ItemEntity)) {
            e.push(d.getStepX() * PUSH, 0, d.getStepZ() * PUSH);
            e.needsSync = true;   // 26.3：hurtMarked は needsSync に（プレイヤーにも動きを送る）
            pushed++;
        }
        return pushed;
    }

    @Override protected void beforeWork(Level level, BlockPos pos) { reach = reach(level, pos, facing()); }
    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 10; }
    @Override protected boolean canWork() { return true; }
    @Override protected void finishWork() {}

    @Override protected void serverTick(Level level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);
        if (getLastWatts() > 0) blow(level, pos, facing());
    }

    @Override protected int extraDataCount() { return 1; }
    @Override protected int extraData(int i) { return reach; }
}
