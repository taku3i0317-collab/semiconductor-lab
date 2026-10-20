package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipModel;
import com.taku3.semiconductorlab.litho.ChipState;
import com.taku3.semiconductorlab.litho.Netlist;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ワイヤーボンダー [9-33]（1957 ベル研の熱圧着ボンディング ⚠️。IMP4-01、S-001）：チップをヘッダー（足の付いた台）に載せ、温めた台の上で
 * 金の細い線（約25µm）をチップのアルミのパッドと足に押しつけてくっつける（熱＋圧力）。パッド1つに線1本。
 * チップ 1〜8個 ＋ 金のボンディング線（チップ1個あたりパッドの数だけ）＋ ヘッダー（チップ1個に1つ）→ 線をつないだチップ（チップの中身 CHIP_STATE を写す）。
 * パッド（チップのふちにかかった配線）の数は A の Netlist で数える（読むだけ）。パッドのないチップはつなげない。
 * ⚠️ 電力（200W：台の熱）・時間（15秒）はゲームの値。のちの超音波ボンディングは表さない（wire_bonder.placeholder）。
 */
public class WireBonderBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 200;
    public static final int TICKS = 20 * 15;
    public static final int CHIPS_PER_RUN = 8;
    public static final int DATA_PADS = DATA_COUNT;
    public static final int SLOT_CHIP = 0, SLOT_WIRE = 1, SLOT_HEADER = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 1)
            .in(SLOT_CHIP, 8, 17, s -> s.is(Semiconductor.CHIP))
            .in(SLOT_WIRE, 8, 35, s -> s.is(Metallurgy.GOLD_BONDING_WIRE))
            .in(SLOT_HEADER, 8, 53, s -> s.is(Semiconductor.CHIP_HEADER))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.wire_bonder.power").samples("500.0 kW")
            .right("pads", 27, 1, "gui.semiconductor-lab.wire_bonder.pads").samples("16")
            .right("hint", 38, 3, "gui.semiconductor-lab.wire_bonder.need_input", "gui.semiconductor-lab.wire_bonder.no_pads",
                    "gui.semiconductor-lab.wire_bonder.need_wire", "gui.semiconductor-lab.wire_bonder.need_power", "gui.semiconductor-lab.wire_bonder.running")
            .bottom("recipe", "gui.semiconductor-lab.wire_bonder.recipe")
            .build();

    public WireBonderBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.WIRE_BONDER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** チップのパッドの数（チップの中身がない・配線がないなら 0） */
    public static int padsOf(ItemStack chip) {
        ChipState s = chip.get(ModDataComponents.CHIP_STATE);
        if (s == null) return 0;
        ChipModel m = ChipModel.of(s);
        return m == null ? 0 : Netlist.extract(m).pads().size();
    }

    /** 1回につなぐチップの数（チップ・ヘッダー・線が足りる分だけ、最大8） */
    public static int batch(ItemStack chip, ItemStack wire, ItemStack header) {
        int pads = padsOf(chip);
        if (pads == 0 || !chip.is(Semiconductor.CHIP) || !wire.is(Metallurgy.GOLD_BONDING_WIRE) || !header.is(Semiconductor.CHIP_HEADER)) return 0;
        return Math.min(Math.min(chip.getCount(), CHIPS_PER_RUN), Math.min(header.getCount(), wire.getCount() / pads));
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    private ItemStack product() {
        int n = batch(container.getItem(SLOT_CHIP), container.getItem(SLOT_WIRE), container.getItem(SLOT_HEADER));
        return n == 0 ? ItemStack.EMPTY : container.getItem(SLOT_CHIP).transmuteCopy(Semiconductor.BONDED_CHIP, n);
    }

    @Override protected boolean canWork() {
        ItemStack out = product();
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product();
        int n = out.getCount(), pads = padsOf(container.getItem(SLOT_CHIP));
        container.getItem(SLOT_CHIP).shrink(n);
        container.getItem(SLOT_WIRE).shrink(n * pads);
        container.getItem(SLOT_HEADER).shrink(n);
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 画面に出すパッドの数 */
    @Override protected int extraDataCount() { return 1; }
    @Override protected int extraData(int i) { return i == DATA_PADS ? padsOf(container.getItem(SLOT_CHIP)) : 0; }
}
