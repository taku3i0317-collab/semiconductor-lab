package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
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
 * シーメンス炉 [1-17]（1950年代 シーメンス社）。ベル型の容器の中で、電気を流して約1100℃にした細いシリコン棒に
 * トリクロロシランと水素を吹きつける：SiHCl3 + H2 → Si + 3HCl。シリコンだけが棒に積もって棒が太る。
 * トリクロロシラン ×1 ＋ 水素 ×1 → 多結晶シリコン ×1（純度＝トリクロロシランの純度）＋ 塩化水素 ×3（[1-16] に戻せる）。
 * 電力量 90kWh/kg（米国特許 US 8,398,946 の比較例。現代の炉はもっと少ない）＝ 500kW × 648秒。
 * 蒸留が 9N に届かないトリクロロシランは入れられない（電子グレード 9N〜11N だけを作る）。
 * ⚠️ 量は記号的（Q22-1）。省略：最初に立てる細いシリコン棒（種棒）と、棒を初めに温める仕組み（冷えたシリコンは電気を通しにくい）。
 */
public class SiemensReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500_000;
    /** 90 kWh = 324 MJ ÷ 500 kW = 648 秒 */
    public static final int TICKS = 12960;
    public static final int TCS_PER_RUN = 1, H2_PER_RUN = 1, HCL_OUT = 3;
    public static final int MIN_NINES = 9;
    public static final int SLOT_TCS = 0, SLOT_H2 = 1, SLOT_OUT = 2, SLOT_HCL = 3, SLOT_COUNT = 4;

    /** 画面のスロットの並び（IMP-28：前の SiemensReactorMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_TCS, 8, 26, SiemensReactorBlockEntity::isTcs)
            .in(SLOT_H2, 8, 50, SiemensReactorBlockEntity::isHydrogen)
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_HCL, 78, 50)
            .build();

    public SiemensReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.SIEMENS_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isTcs(ItemStack s) { return s.is(Metallurgy.TRICHLOROSILANE); }
    public static boolean isHydrogen(ItemStack s) { return s.is(Gas.HYDROGEN_GAS); }
    /** 9N 以上に蒸留したトリクロロシランだけ */
    public static boolean pureEnough(ItemStack s) { return isTcs(s) && Purity.nines(TrichlorosilaneReactorBlockEntity.purityOf(s)) >= MIN_NINES; }

    public static ItemStack polysilicon(double purity) {
        ItemStack s = new ItemStack(Metallurgy.POLYSILICON);
        s.set(ModDataComponents.PURITY, purity);
        return s;
    }
    private ItemStack product() { return polysilicon(TrichlorosilaneReactorBlockEntity.purityOf(container.getItem(SLOT_TCS))); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack t = container.getItem(SLOT_TCS);
        return pureEnough(t) && t.getCount() >= TCS_PER_RUN
                && isHydrogen(container.getItem(SLOT_H2)) && container.getItem(SLOT_H2).getCount() >= H2_PER_RUN
                && canMerge(container.getItem(SLOT_OUT), product())
                && canMerge(container.getItem(SLOT_HCL), new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, HCL_OUT));
    }

    @Override protected void finishWork() {
        ItemStack si = product();
        container.getItem(SLOT_TCS).shrink(TCS_PER_RUN);
        container.getItem(SLOT_H2).shrink(H2_PER_RUN);
        merge(SLOT_OUT, si);
        merge(SLOT_HCL, new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, HCL_OUT));
        container.setChanged();
    }

}
