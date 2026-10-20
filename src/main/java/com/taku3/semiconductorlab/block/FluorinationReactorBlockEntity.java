package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * フッ素化の反応器 [9-27] の前段（IMP4-01、O-012）：炭素をフッ素の中で燃やすと四フッ化炭素 CF4 ができる（C + 2F2 → CF4。1886 モアッサンがフッ素を取り出したとき、炭素が燃えるのを見た）。
 * フッ素に強いニッケルの管で、冷やしながら少しずつ反応させる（電気は冷却と制御 {@link #WATTS}）。
 * 黒鉛 1 ＋ フッ素のボンベ 1 → CF4 {@link #CF4_PER_RUN} 袋 ＋ 空のボンベ。CF4 はとても安定で毒が少なく、プラズマの中でだけフッ素を放す＝ドライエッチングのガス。
 * ⚠️ 量はゲームの値（ボンベ1本で 8袋）。本物の工業の CF4 は HF とクロロカーボンからも作る（fluorination_reactor.placeholder）。
 */
public class FluorinationReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int TICKS = 20 * 10;
    public static final int CF4_PER_RUN = 8;
    public static final int SLOT_CARBON = 0, SLOT_F2 = 1, SLOT_OUT = 2, SLOT_CYL = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_CARBON, 8, 26, FluorinationReactorBlockEntity::isCarbon)
            .in(SLOT_F2, 8, 50, s -> s.is(Gas.FLUORINE_CYLINDER))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_CYL, 78, 50)
            .right("power", 17, 1, "gui.semiconductor-lab.fluorination_reactor.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.fluorination_reactor.need_input", "gui.semiconductor-lab.fluorination_reactor.need_power", "gui.semiconductor-lab.fluorination_reactor.running")
            .bottom("recipe", "gui.semiconductor-lab.fluorination_reactor.recipe")
            .build();

    public FluorinationReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.FLUORINATION_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isCarbon(ItemStack s) { return s.is(Metallurgy.GRAPHITE) || s.is(Heat.SYNTHETIC_GRAPHITE); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return isCarbon(container.getItem(SLOT_CARBON)) && container.getItem(SLOT_F2).is(Gas.FLUORINE_CYLINDER)
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Semiconductor.CARBON_TETRAFLUORIDE, CF4_PER_RUN))
                && canMerge(container.getItem(SLOT_CYL), new ItemStack(Gas.EMPTY_GAS_CYLINDER));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_CARBON).shrink(1);
        container.getItem(SLOT_F2).shrink(1);
        merge(SLOT_OUT, new ItemStack(Semiconductor.CARBON_TETRAFLUORIDE, CF4_PER_RUN));
        merge(SLOT_CYL, new ItemStack(Gas.EMPTY_GAS_CYLINDER));
        container.setChanged();
    }
}
