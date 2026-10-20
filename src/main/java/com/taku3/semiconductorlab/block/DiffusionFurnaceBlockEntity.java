package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.Dopant;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 拡散炉 [9-16]（1954 フラー：n型の板にホウ素をしみ込ませ、表面に薄い p型の層を作った）。電気炉 [4-12] の中に石英管 [7-02] を通し、
 * 板とドーパント源を入れて高温にすると、ドーパントの原子が表面から固体の中へ少しずつ入っていく（拡散、フィックの法則）。
 * シリコンの板 ×{@link #WAFERS_PER_RUN}（n型 か p型）＋ 反対の型のドーパント源 ×1 → p-n 接合の板 ×{@link #WAFERS_PER_RUN}。
 * n型の板には B2O3（ホウ素 → 表面が p型、1954 の電池と同じ向き）、p型の板には白リン（リン → 表面が n型、のちの電池の向き）。
 * 同じ型・ドーパントなし（真性）の板では接合ができないので動かない。1954 年は酸化膜のマスクなしで板全体に拡散した。
 * ⚠️ 温度（約1000℃）・電力（5kW）・時間（120秒、本物は何時間も）は出典で未確認のゲームの値（DECISIONS_PENDING Q25-6）。
 */
public class DiffusionFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 120;
    public static final int WAFERS_PER_RUN = 8, SOURCE_PER_RUN = 1;
    public static final int SLOT_WAFER = 0, SLOT_SOURCE = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    /** 画面のスロットの並び（IMP-28：前の DiffusionFurnaceMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, DiffusionFurnaceBlockEntity::isWafer)
            .in(SLOT_SOURCE, 8, 50, s -> com.taku3.semiconductorlab.Dopant.of(s) != null)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.diffusion_furnace.power").samples("500.0 kW")
            .right("need", 27, 1, "gui.semiconductor-lab.diffusion_furnace.need").samples("50 kW")
            .right("hint", 38, 3, "gui.semiconductor-lab.diffusion_furnace.need_input", "gui.semiconductor-lab.diffusion_furnace.need_power", "gui.semiconductor-lab.diffusion_furnace.running")
            .bottom("recipe", "gui.semiconductor-lab.diffusion_furnace.recipe")
            .build();

    public DiffusionFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.DIFFUSION_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isWafer(ItemStack s) { return s.is(Semiconductor.SILICON_WAFER); }
    public static Dopant baseOf(ItemStack wafer) { return Dopant.byId(wafer.getOrDefault(ModDataComponents.DOPANT, "")); }

    /** 板の型と反対の型のドーパント源なら true（n型の板＋ホウ素、p型の板＋リン） */
    public static boolean makesJunction(ItemStack wafer, ItemStack source) {
        Dopant base = baseOf(wafer), add = Dopant.of(source);
        return isWafer(wafer) && base != null && add != null && base.type != add.type;
    }

    /** p-n 接合の板：元の板の純度と、土台（板）のドーパントを受け継ぐ */
    public static ItemStack junction(ItemStack wafer, int count) {
        ItemStack s = new ItemStack(Semiconductor.PN_JUNCTION_WAFER, count);
        Double p = wafer.get(ModDataComponents.PURITY);
        if (p != null) s.set(ModDataComponents.PURITY, p);
        String d = wafer.get(ModDataComponents.DOPANT);
        if (d != null) s.set(ModDataComponents.DOPANT, d);
        return s;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack w = container.getItem(SLOT_WAFER), src = container.getItem(SLOT_SOURCE);
        return w.getCount() >= WAFERS_PER_RUN && src.getCount() >= SOURCE_PER_RUN && makesJunction(w, src)
                && canMerge(container.getItem(SLOT_OUT), junction(w, WAFERS_PER_RUN));
    }

    @Override protected void finishWork() {
        ItemStack out = junction(container.getItem(SLOT_WAFER), WAFERS_PER_RUN);
        container.getItem(SLOT_WAFER).shrink(WAFERS_PER_RUN);
        com.taku3.semiconductorlab.machine.Vessel.returnEmpty(level, worldPosition, container.getItem(SLOT_SOURCE), SOURCE_PER_RUN);   // 空の瓶は上に出る（IMP-33）
        container.getItem(SLOT_SOURCE).shrink(SOURCE_PER_RUN);
        merge(SLOT_OUT, out);
        container.setChanged();
    }

}
