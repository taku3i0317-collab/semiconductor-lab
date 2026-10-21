package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 封止台 [5-02]（真空管の排気・封じ切り台）。隣の真空容器を「排気マニホールド」として使う：
 * 真空管の組立品（未排気）を入れ、容器の圧力が {@link #SEAL_MAX_PA} 以下のあいだだけ、
 * 500W×10秒（フィラメントに電流を流して中のガスを追い出す＝ガス出し）→ 管の首を焼き切って封じる → 三極真空管。
 * 1本封じるたびに、次の管を付け替えるときの空気が入り、容器の圧力が少し上がる（大気圧との差の {@link #VENT_FRACTION}）。
 * 閾値は初期の軟真空管（オーディオン 1906）の 0.1Pa（約 10⁻³ Torr）。手押しポンプ（1kPa まで）では届かず、
 * シュプレンゲル水銀ポンプ[5-15]（0.05Pa まで）が要る。高真空管（10⁻⁴Pa、ラングミュア 1913）は拡散ポンプ[5-05]で。
 * ゲッター [5-16]（IMP4-01、O-030 案A、A の同意あり）：下のスロットにマグネシウムのゲッターを入れると、封じた後にゲッターが残りのガスを吸うので、
 * 容器が {@link #GETTER_SEAL_MAX_PA}（1Pa ⚠️）まででも封じられる（1本ごとにゲッター1個を使う）。ゲッターが無いときは今までどおり。
 */
public class SealingStationBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int TICKS = 20 * 10;
    /** 封止してよい容器の圧力の上限（Pa）。軟真空 0.1Pa ≈ 7.5×10⁻⁴ Torr（2026-10 タスク12 で 1.5kPa の仮から変更） */
    public static final double SEAL_MAX_PA = 0.1;
    /** 1本封じるごとに戻る空気の割合 */
    public static final double VENT_FRACTION = 0.05;
    /** ゲッター [5-16] があるときの上限（Pa ⚠️ ゲームの値）：封じた後にゲッターが残りのガスを吸い、中は 1/10 になる */
    public static final double GETTER_SEAL_MAX_PA = 1.0;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_GETTER = 2, SLOT_COUNT = 3;
    /** GUI へ送る追加の数値：隣の容器の圧力（{@link #encodePa}） */
    public static final int DATA_PRESSURE = ConsumerBlockEntity.DATA_COUNT, MENU_DATA_COUNT = ConsumerBlockEntity.DATA_COUNT + 1;

    private final ContainerData menuData = new ContainerData() {
        @Override public int get(int i) {
            if (i == DATA_PRESSURE) { VacuumChamberBlockEntity c = chamber(); return c == null ? 0 : encodePa(c.getPressure()); }
            return data.get(i);
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return MENU_DATA_COUNT; }
    };

    /** 画面のスロットの並び（IMP-28：前の SealingStationMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(MENU_DATA_COUNT)
            .in(SLOT_IN, 8, 35, s -> !sealedProduct(s).isEmpty())
            .out(SLOT_OUT, 78, 35)
            .in(SLOT_GETTER, 30, 52, s -> s.is(Vacuum.MAGNESIUM_GETTER))
            .right("power", 17, 1, "gui.semiconductor-lab.sealing_station.power").samples("0.5 kW")
            .right("pressure", 27, 1, "gui.semiconductor-lab.sealing_station.pressure").samples("101 kPa").fit()
            .right("target", 37, 1, "gui.semiconductor-lab.sealing_station.target").samples("0.1 Pa")
            .right("hint", 48, 2, "gui.semiconductor-lab.sealing_station.need_input", "gui.semiconductor-lab.sealing_station.need_chamber", "gui.semiconductor-lab.sealing_station.need_vacuum", "gui.semiconductor-lab.sealing_station.output_full", "gui.semiconductor-lab.sealing_station.need_power", "gui.semiconductor-lab.sealing_station.sealing")
            .build();

    public SealingStationBlockEntity(BlockPos pos, BlockState state) {
        super(Vacuum.SEALING_STATION.type, pos, state, SLOT_COUNT, 20_000, 2_000);
    }

    /**
     * 圧力を GUI 同期用の整数にする（同期は short までなので Pa のままだと 101325 が入らない）。
     * 0＝容器なし、それ以外は round(log10(Pa)×1000)+10000（1mPa〜1気圧が 7000〜15006 に入る）
     */
    public static int encodePa(double pa) { return (int) Math.round(Math.log10(Math.max(pa, 1e-3)) * 1000) + 10_000; }
    /** encodePa の逆。容器なしは -1 */
    public static double decodePa(int v) { return v <= 0 ? -1 : Math.pow(10, (v - 10_000) / 1000.0); }

    @Override public ContainerData getMenuData() { return menuData; }

    private VacuumChamberBlockEntity chamber() { return level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    /**
     * 封じると何になるか（封じられない物は EMPTY）。真空管の組立品 → 三極真空管、
     * IMP2-01：水銀灯の組立品 [7-10] → 水銀灯の管（空気を抜いて封じる。水銀の蒸気だけが残る）
     */
    public static ItemStack sealedProduct(ItemStack in) {
        if (in.is(Vacuum.VACUUM_TUBE_ASSEMBLY)) return new ItemStack(Vacuum.VACUUM_TUBE);
        if (in.is(com.taku3.semiconductorlab.content.Optics.MERCURY_LAMP_ASSEMBLY)) return new ItemStack(com.taku3.semiconductorlab.content.Optics.MERCURY_LAMP_TUBE);
        return ItemStack.EMPTY;
    }

    /** ゲッターがあるか */
    public static boolean hasGetter(ItemStack s) { return s.is(Vacuum.MAGNESIUM_GETTER); }
    /** 封じてよい容器の圧力の上限（ゲッターがあれば 1Pa、なければ 0.1Pa） */
    public static double sealMaxPa(ItemStack getter) { return hasGetter(getter) ? GETTER_SEAL_MAX_PA : SEAL_MAX_PA; }

    @Override protected boolean canWork() {
        ItemStack out = sealedProduct(container.getItem(SLOT_IN));
        if (out.isEmpty()) return false;
        VacuumChamberBlockEntity c = chamber();
        return c != null && c.getPressure() <= sealMaxPa(container.getItem(SLOT_GETTER)) && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = sealedProduct(container.getItem(SLOT_IN));
        container.getItem(SLOT_IN).shrink(1);
        if (hasGetter(container.getItem(SLOT_GETTER))) container.getItem(SLOT_GETTER).shrink(1);   // ゲッターは管の中に残る（銀色の膜）
        merge(SLOT_OUT, out);
        container.setChanged();
        VacuumChamberBlockEntity c = chamber();
        if (c != null) c.vent(VENT_FRACTION);
    }

}
