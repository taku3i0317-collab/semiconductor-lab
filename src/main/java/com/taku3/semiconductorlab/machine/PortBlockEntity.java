package com.taku3.semiconductorlab.machine;

import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.EnergyApi;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * マルチブロックの口（v1.0 #1、O-034 Q33-2）：形の「囲い」の場所に置ける。組み上がると制御ブロックにつながる（{@link Multiblock#recheck} が link する）。
 * 電気の口＝制御ブロックの電気の入れ物を返す（EnergyApi）、物の口＝{@link Multiblock.ItemPort}（E のロードポートがそのまま使える）、ガスの口＝{@link Multiblock#extractGas}
 */
public class PortBlockEntity extends BlockEntity implements Multiblock.ItemPort {
    public enum Kind { ENERGY, ITEM, GAS }
    private final Kind kind;
    private BlockPos controller;

    public PortBlockEntity(Kind kind, BlockPos pos, BlockState state) {
        super(switch (kind) { case ENERGY -> Semiconductor.ENERGY_PORT.type; case ITEM -> Semiconductor.ITEM_PORT.type; case GAS -> Semiconductor.GAS_PORT.type; }, pos, state);
        this.kind = kind;
    }

    public Kind kind() { return kind; }
    public BlockPos controllerPos() { return controller; }
    void link(BlockPos pos) { if (!pos.equals(controller)) { controller = pos; setChanged(); } }

    /** つながった制御ブロック（組み上がっていなければ null） */
    private Multiblock.Host host() {
        if (level == null || controller == null || !(level.getBlockEntity(controller) instanceof Multiblock.Host h) || !h.multiblock().formed()) return null;
        return h;
    }

    @Override public MachineBE controller() { return kind == Kind.ITEM && host() instanceof MachineBE m ? m : null; }

    /** 電気の口：制御ブロックの電気の入れ物（組み上がっていなければ null） */
    public EnergyStorage energy(Direction side) {
        if (kind != Kind.ENERGY || host() == null) return null;
        return EnergyApi.ENERGY.find(level, controller, side);
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); if (controller != null) out.putLong("Controller", controller.asLong()); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); long c = in.getLongOr("Controller", Long.MIN_VALUE); controller = c == Long.MIN_VALUE ? null : BlockPos.of(c); }
}
