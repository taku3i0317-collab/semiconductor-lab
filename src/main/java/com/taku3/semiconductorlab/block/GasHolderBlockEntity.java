package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ガスホルダー [6-02]（低圧貯蔵）。19世紀のガス灯用の水封式ガスタンク：
 * 水を張った槽に、底のない鉄の「ふた（ベル）」をかぶせ、ガスが入るとベルが浮き上がる。水がふたのすき間をふさぐので漏れない。
 * 電気は使わない。1種類のガスだけを貯める（最初に入れたガスで種類が決まり、空になると解除）。
 * - 隣（6方向）の電解槽の出口から、同じ種類のガスを自動で受け取る
 * - 左のスロットに袋詰めガスを入れると吸い込む。右のスロットに1袋ずつ出てくる
 * - 水素と酸素を混ぜると爆鳴気で小さく爆発し、中身が空になる（ブロックは壊れない）
 */
public class GasHolderBlockEntity extends BlockEntity implements MenuProvider {
    /** 容量（mol）。25℃・1気圧で 1mol ≈ 24.5 L なので 1000mol ≈ 24.5 m³（小さな町工場のホルダー程度） */
    public static final int CAPACITY = 1000;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_COUNT = 2;
    public static final int DATA_TYPE = 0, DATA_AMOUNT = 1, DATA_BANG = 2, DATA_COUNT = 3;
    /** 爆発のあと「爆鳴気！」を GUI に出しておく時間（tick） */
    private static final int BANG_TICKS = 100;
    private static final int PULL_INTERVAL = 10;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); GasHolderBlockEntity.this.setChanged(); }
    };
    private GasType type = GasType.NONE;
    private int amount;
    private int bang;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_TYPE -> type.ordinal(); case DATA_AMOUNT -> amount; case DATA_BANG -> bang; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public GasHolderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAS_HOLDER, pos, state);
    }

    public GasType getGasType() { return type; }
    public int getAmount() { return amount; }

    /** このガスを受け入れられるか（空、または同じ種類で満杯でない） */
    private boolean accepts(GasType g) {
        return g != GasType.NONE && amount < CAPACITY && (amount == 0 || type == g);
    }

    private void add(GasType g, int n) {
        type = g;
        amount += n;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GasHolderBlockEntity be) {
        if (level.isClientSide()) return;
        if (be.bang > 0) be.bang--;
        be.handleInput(level, pos);
        if (level.getGameTime() % PULL_INTERVAL == 0) be.pullFromNeighbors(level, pos);
        be.fillOutput();
    }

    /** 左のスロットの袋詰めガスを1tickに1袋ずつ吸い込む */
    private void handleInput(Level level, BlockPos pos) {
        ItemStack in = container.getItem(SLOT_IN);
        GasType g = GasType.of(in);
        if (g == GasType.NONE) return;
        if (amount > 0 && type != g && GasType.explosive(type, g)) {
            in.shrink(1);
            container.setChanged();
            explode(level, pos);
        } else if (accepts(g)) {
            in.shrink(1);
            container.setChanged();
            add(g, 1);
        }
    }

    /**
     * 爆鳴気：水素2：酸素1 が理想だが、水素が空気中で約4〜75%（酸素中ならさらに広い）あれば火花で燃え広がる。
     * ゲームでは音と煙だけ出して中身を空にする。人やブロックは傷つけない（教材として「危ない」を体験させる範囲）。
     */
    private void explode(Level level, BlockPos pos) {
        amount = 0;
        type = GasType.NONE;
        bang = BANG_TICKS;
        setChanged();
        double x = pos.getX() + 0.5, y = pos.getY() + 1.0, z = pos.getZ() + 0.5;
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.6f, 1.6f);
        if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
    }

    /** 隣の電解槽の出口（水素・酸素スロット）から、受け入れられるガスを全部もらう */
    private void pullFromNeighbors(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (!(level.getBlockEntity(pos.relative(d)) instanceof ElectrolyzerBlockEntity el)) continue;
            SimpleContainer c = el.getContainer();
            for (int slot : new int[] {ElectrolyzerBlockEntity.SLOT_H2, ElectrolyzerBlockEntity.SLOT_O2}) {
                ItemStack s = c.getItem(slot);
                GasType g = GasType.of(s);
                if (!accepts(g)) continue;
                int n = Math.min(s.getCount(), CAPACITY - amount);
                c.removeItem(slot, n);
                add(g, n);
            }
        }
    }

    /** 右のスロットが空なら1袋だけ詰めて出す（取ると次の袋が出てくる） */
    private void fillOutput() {
        if (amount <= 0 || !container.getItem(SLOT_OUT).isEmpty()) return;
        container.setItem(SLOT_OUT, new ItemStack(type.bagItem(), 1));
        amount--;
        if (amount == 0) type = GasType.NONE;
        setChanged();
    }

    public SimpleContainer getContainer() { return container; }

    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.gas_holder"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new GasHolderMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition));
    }

    // 壊したときは袋だけ落ちる。中のガスは逃げる（本物も低圧なので、こわれたら空気に混ざって散る）
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putString("Gas", type.name());
        out.putInt("Amount", amount);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        type = GasType.byName(in.getStringOr("Gas", "NONE"));
        amount = in.getIntOr("Amount", 0);
        if (amount <= 0) { amount = 0; type = GasType.NONE; }
    }
}
