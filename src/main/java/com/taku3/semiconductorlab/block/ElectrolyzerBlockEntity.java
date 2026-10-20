package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 電解槽。左下のスロットに入れた物で2つのモードを切り替える（黒鉛電極・直流 5kW）。
 * <pre>
 * ① 水の電気分解 [6-01]：水バケツ ＋ 硫酸（電解質、消耗しない）→ 水素×2（陰極）＋ 酸素×1（陽極）
 *      2H2O → 2H2 + O2
 * ② クロルアルカリ電解 [6-05]：水バケツ ＋ 塩×2（消耗する）→ 水素×1（陰極）＋ 塩素×1（陽極）＋ 水酸化ナトリウム×2
 *      2NaCl + 2H2O → Cl2 + H2 + 2NaOH（1890年代〜）
 * </pre>
 * 陽極の出口（SLOT_O2）は、①では酸素、②では塩素が出る（同じ＋極で、水より塩化物イオンが先に電子を渡すため）。
 * ⚠️ 省略：本物の②は陽極側と陰極側を隔膜（アスベスト）・水銀・イオン交換膜で分けないと、塩素と NaOH が混ざって
 * 次亜塩素酸ナトリウム（漂白剤）になってしまう。ここでは隔膜の違いを省略（electrolyzer.placeholder）。
 */
public class ElectrolyzerBlockEntity extends ConsumerBlockEntity implements GasSource {
    /** ガスホルダーが吸う出口：陰極（H2）と陽極（O2／Cl2）。NaOH は液体なので吸わない */
    @Override public int[] gasOutputSlots() { return new int[] {SLOT_H2, SLOT_O2}; }
    public static final double WATTS = 5_000;
    // 水 2 mol を分解するのに要るエネルギー ≒ 2×286 kJ = 572 kJ。5kW なら約 115 秒
    public static final int TOTAL_TICKS = 20 * 115;
    // 塩素 1 mol あたり：工業の実績 約2.5 MWh/t-Cl2 ≒ 640 kJ/mol（理論は約 446 kJ/mol ＋電極・液の抵抗のロス）。5kW なら 128 秒
    public static final int BRINE_TICKS = 20 * 128;
    /** クロルアルカリ1回で使う塩（2NaCl） */
    public static final int SALT_PER_RUN = 2;
    public static final int SLOT_WATER = 0, SLOT_ACID = 1, SLOT_H2 = 2, SLOT_O2 = 3, SLOT_NAOH = 4, SLOT_COUNT = 5;
    /** GUI へ送るモード（DATA_COUNT 番）：0＝なし、1＝水（硫酸）、2＝塩水 */
    public static final int DATA_MODE = DATA_COUNT, MODE_NONE = 0, MODE_WATER = 1, MODE_BRINE = 2;

    /** 画面のスロットの並び（IMP-28：前の ElectrolyzerMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 1)
            .in(SLOT_WATER, 8, 17, s -> s.is(net.minecraft.world.item.Items.WATER_BUCKET))
            .in(SLOT_ACID, 8, 53, ElectrolyzerBlockEntity::isSecondInput)
            .out(SLOT_H2, 78, 17)
            .out(SLOT_O2, 78, 35)
            .out(SLOT_NAOH, 78, 53)
            .right("power", 17, 1, "gui.semiconductor-lab.electrolyzer.power").samples("5.0 kW")
            .right("stored", 27, 1, "gui.semiconductor-lab.electrolyzer.stored").samples("50 kJ")
            .right("hint", 38, 3, "gui.semiconductor-lab.electrolyzer.need_water", "gui.semiconductor-lab.electrolyzer.need_acid", "gui.semiconductor-lab.electrolyzer.need_salt", "gui.semiconductor-lab.electrolyzer.output_full", "gui.semiconductor-lab.electrolyzer.need_power", "gui.semiconductor-lab.electrolyzer.running")
            .bottom("mode", "gui.semiconductor-lab.electrolyzer.mode_brine", "gui.semiconductor-lab.electrolyzer.mode_water")
            .label("h2", 62, 21, 12, "H2").text("anode", 58, 39, 18, 1, "=O2", "=Cl2").label("naoh", 52, 57, 24, "NaOH")
            .build();

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(Gas.ELECTROLYZER.type, pos, state, SLOT_COUNT, 50_000, WATTS);
    }

    public static boolean isElectrolyte(ItemStack s) { return s.is(Chemistry.SULFURIC_ACID); }
    public static boolean isSalt(ItemStack s) { return s.is(Gas.SALT); }
    /** 左下のスロットに置けるもの（硫酸 or 塩） */
    public static boolean isSecondInput(ItemStack s) { return isElectrolyte(s) || isSalt(s); }

    public static int modeOf(ItemStack second) {
        if (isElectrolyte(second)) return MODE_WATER;
        if (isSalt(second)) return MODE_BRINE;
        return MODE_NONE;
    }
    public int mode() { return modeOf(container.getItem(SLOT_ACID)); }

    private ItemStack cathode() { return new ItemStack(Gas.HYDROGEN_GAS, mode() == MODE_BRINE ? 1 : 2); }
    private ItemStack anode() { return new ItemStack(mode() == MODE_BRINE ? Gas.CHLORINE_GAS : Gas.OXYGEN_GAS, 1); }
    private static ItemStack naoh() { return new ItemStack(Gas.SODIUM_HYDROXIDE, 2); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return mode() == MODE_BRINE ? BRINE_TICKS : TOTAL_TICKS; }

    @Override protected boolean canWork() {
        int m = mode();
        if (m == MODE_NONE) return false;                                // 純水は電気を通さない
        if (!container.getItem(SLOT_WATER).is(Items.WATER_BUCKET)) return false;
        if (m == MODE_BRINE && (container.getItem(SLOT_ACID).getCount() < SALT_PER_RUN || !canMerge(container.getItem(SLOT_NAOH), naoh()))) return false;
        return canMerge(container.getItem(SLOT_H2), cathode()) && canMerge(container.getItem(SLOT_O2), anode());
    }

    @Override protected void finishWork() {
        boolean brine = mode() == MODE_BRINE;
        ItemStack h2 = cathode(), an = anode();
        container.setItem(SLOT_WATER, new ItemStack(Items.BUCKET));    // 水を使い切った
        if (brine) {
            container.getItem(SLOT_ACID).shrink(SALT_PER_RUN);          // 塩は Cl2 と NaOH になって消える（硫酸は減らない）
            merge(SLOT_NAOH, naoh());                                    // 陰極の液に NaOH がたまる
        }
        merge(SLOT_H2, h2);   // 陰極（−）から水素
        merge(SLOT_O2, an);   // 陽極（＋）から酸素（水）／塩素（塩水）
        container.setChanged();
    }

    @Override protected int extraDataCount() { return 1; }
    @Override protected int extraData(int i) { return i == DATA_MODE ? mode() : 0; }

}
