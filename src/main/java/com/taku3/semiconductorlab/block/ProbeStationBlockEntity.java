package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.litho.ChipModel;
import com.taku3.semiconductorlab.litho.ChipState;
import com.taku3.semiconductorlab.litho.Discoveries;
import com.taku3.semiconductorlab.litho.Netlist;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 検査台（プローブ、O-002、LITHO_DESIGN §6・§7）：焼き上がったウェハーを置き、パッドに 0 V／5 V／つながない を決めて
 * 電流の流れ方を見る。「調べる」でつながり方を判定し、歴史上の発明と同じなら知らせて「発見」の進捗を開く。
 * 正解は教えない：部品の名前が出るのは調べたあとだけ。電流の計算は画面（クライアント）が同じ Circuit で行う。
 * 省略：測定の電源（電池・電源装置）と針の位置合わせ（[8-08]）。
 */
public class ProbeStationBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_WAFER = 0, SLOT_COUNT = 1;
    /** パッドの数の上限（v0.3 の 8×8 には十分） */
    public static final int MAX_PADS = 12;
    /** パッドの状態：つながない・0 V・5 V */
    public static final int OPEN = 0, GROUND = 1, VCC = 2;
    /** 数値：0〜11＝パッドの状態、12＝このチップで見つかった発見（ビット）、13＝調べたか */
    public static final int DATA_FOUND = MAX_PADS, DATA_PROBED = MAX_PADS + 1, DATA_COUNT = MAX_PADS + 2;
    /** ボタン：パッド i の状態を順に変える（0〜11）、調べる */
    public static final int BTN_PROBE = 20;

    public static final int H = 222, CHIP_X = 8, CHIP_Y = 18, CHIP_W = 80, RIGHT_X = 94, PROBE_Y = 84, WAFER_X = 96, WAFER_Y = 104;

    public static final MachineLayout LAYOUT;
    static {
        var b = MachineLayout.builder(DATA_COUNT).height(H)
                .in(SLOT_WAFER, WAFER_X, WAFER_Y, s -> s.is(Semiconductor.PROCESS_WAFER) || s.is(Semiconductor.CHIP))   // 割ったチップも調べられる（[9-32]）
                .area("chip", CHIP_X, CHIP_Y, CHIP_W, CHIP_W)
                .area("legend", CHIP_X, CHIP_Y + CHIP_W + 4, CHIP_W, 20)
                .area("probe_button", RIGHT_X, PROBE_Y, 78, 14)
                .text("pad", RIGHT_X, 18, 78, 1, "gui.semiconductor-lab.probe_station.pad").samples("12", "5 V").fit()
                .text("amps", RIGHT_X, 28, 78, 1, "gui.semiconductor-lab.probe_station.amps").samples("-123.4 mA").fit();
        for (int i = 0; i < Discoveries.all().size(); i++) b.lazyText("found" + i, RIGHT_X, 38 + 9 * i, 78, 1, () -> Discoveries.all().stream().map(Discoveries.Discovery::langKey).toList()).fit();
        LAYOUT = b.text("dirt", WAFER_X + 22, WAFER_Y + 4, 54, 1, "gui.semiconductor-lab.probe_station.dirt").samples("200").fit()
                .text("hint", RIGHT_X, 122, 78, 1, MachineLayout.keys("gui.semiconductor-lab.probe_station.", "need_wafer", "set_pads", "nothing")).fit()
                .build();
    }

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); ProbeStationBlockEntity.this.changed(); }
    };
    private final int[] pads = new int[MAX_PADS];
    private int found, probed;
    /** パッドの状態を合わせたウェハーの中身（変わったらパッドを元に戻す） */
    private ChipState lastState;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return i < MAX_PADS ? pads[i] : i == DATA_FOUND ? found : i == DATA_PROBED ? probed : 0; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public ProbeStationBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.PROBE_STATION.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public int padState(int i) { return pads[i]; }
    public int found() { return found; }

    private void changed() {
        ChipState s = container.getItem(SLOT_WAFER).get(ModDataComponents.CHIP_STATE);
        if (!java.util.Objects.equals(s, lastState)) {   // ちがうウェハー：パッドと結果を元に戻す
            lastState = s;
            java.util.Arrays.fill(pads, OPEN);
            found = 0; probed = 0;
        }
        setChanged();
    }

    /** 置いてあるウェハーの回路（なければ null） */
    public Netlist netlist() { return netlistOf(container.getItem(SLOT_WAFER)); }

    public static Netlist netlistOf(ItemStack w) {
        ChipState s = w.get(ModDataComponents.CHIP_STATE);
        ChipModel m = s == null ? null : ChipModel.of(s);
        return m == null ? null : Netlist.extract(m);
    }

    /** パッドの状態 → 線の番号ごとの電圧（つながないパッドは入れない）。画面と同じ */
    public static Map<Integer, Double> drive(Netlist nl, java.util.function.IntUnaryOperator padState) {
        Map<Integer, Double> d = new HashMap<>();
        List<Netlist.Net> p = nl.pads();
        for (int i = 0; i < p.size() && i < MAX_PADS; i++) {
            int st = padState.applyAsInt(i);
            if (st == GROUND) d.put(p.get(i).id(), 0.0);
            else if (st == VCC) d.put(p.get(i).id(), com.taku3.semiconductorlab.litho.Circuit.VCC);
        }
        return d;
    }

    @Override public boolean onButton(Player player, int id) {
        Netlist nl = netlist();
        if (nl == null) return false;
        if (id >= 0 && id < MAX_PADS && id < nl.pads().size()) {
            pads[id] = (pads[id] + 1) % 3;
            setChanged();
            return true;
        }
        if (id == BTN_PROBE) {
            double leakScale = level == null ? 1.0 : 0.5 + level.getRandom().nextDouble();   // 汚れの漏れは毎回少しちがう（S-003）
            List<Discoveries.Discovery> hits = Discoveries.match(nl, leakScale);
            int mask = 0;
            for (Discoveries.Discovery d : hits) mask |= 1 << Discoveries.all().indexOf(d);
            found = mask;
            probed = 1;
            setChanged();
            if (player instanceof ServerPlayer sp && level != null && level.getServer() != null) {
                for (Discoveries.Discovery d : hits) {
                    AdvancementHolder adv = level.getServer().getAdvancements().get(SemiconductorLab.id("discovery/" + d.id()));
                    if (adv == null || sp.getAdvancements().getOrStartProgress(adv).isDone()) continue;
                    sp.getAdvancements().award(adv, "found");
                    sp.sendSystemMessage(Component.translatable("litho.semiconductor-lab.found", d.year(), Component.translatable(d.langKey())));
                }
            }
            return true;
        }
        return false;
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putIntArray("Pads", pads.clone()); out.putInt("Found", found); out.putInt("Probed", probed);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        int[] p = in.getIntArray("Pads").orElse(new int[0]);
        for (int i = 0; i < MAX_PADS; i++) pads[i] = i < p.length ? p[i] : OPEN;
        found = in.getIntOr("Found", 0); probed = in.getIntOr("Probed", 0);
        lastState = container.getItem(SLOT_WAFER).get(ModDataComponents.CHIP_STATE);
    }
}
