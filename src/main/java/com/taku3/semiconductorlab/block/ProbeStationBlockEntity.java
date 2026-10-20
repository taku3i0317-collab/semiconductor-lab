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
    /** パッドの状態：つながない・0 V・5 V・入力A・入力B・出力1・出力2（v0.3b のお題、S-005） */
    public static final int OPEN = 0, GROUND = 1, VCC = 2, IN_A = 3, IN_B = 4, OUT_1 = 5, OUT_2 = 6, PAD_STATES = 7;
    /** 数値：0〜11＝パッドの状態、12＝このチップで見つかった発見（ビット）、13＝調べたか、14＝お題の番号、15＝お題の結果（{@link #encodeResult}） */
    public static final int DATA_FOUND = MAX_PADS, DATA_PROBED = MAX_PADS + 1, DATA_CHALLENGE = MAX_PADS + 2, DATA_RESULT = MAX_PADS + 3, DATA_COUNT = MAX_PADS + 4;
    /** ボタン：パッド i の状態を順に変える（0〜11）、調べる、次のお題 */
    public static final int BTN_PROBE = 20, BTN_CHALLENGE = 21, BTN_HINT = 22;
    /** ヒントの段（①どう動けばいいか ②大事な考え方 ③形の一部 ④歴史の本物の回路図） */
    public static final int HINTS = 4;
    /** お題の結果の数値：0＝まだ、1＝パッドの役目が足りない、2 以上＝2 + 合った行×16 + 全部の行 */
    public static final int RESULT_NONE = 0, RESULT_MISSING = 1;
    public static int encodeResult(com.taku3.semiconductorlab.litho.Challenge.Result r) { return r.missingRoles() ? RESULT_MISSING : 2 + r.rowsOk() * 16 + r.rows(); }

    public static final int H = 256, CHIP_X = 8, CHIP_Y = 18, CHIP_W = 80, RIGHT_X = 94, PROBE_Y = 84, WAFER_X = 96, WAFER_Y = 104;

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
                // お題（v0.3b、S-005）：左下にボタン、その右に動きの説明（2行）、下に結果
                .area("challenge_button", CHIP_X, 134, 16, 14)
                .lazyText("challenge", CHIP_X + 20, 134, 144, 2, () -> com.taku3.semiconductorlab.litho.Challenge.all().stream().map(com.taku3.semiconductorlab.litho.Challenge.Def::goalKey).toList()).fit()
                .area("hint_button", CHIP_X, 150, 16, 12)
                .text("result", CHIP_X + 20, 153, 144, 1, MachineLayout.keys("gui.semiconductor-lab.probe_station.", "result_none", "result_missing", "result_rows", "result_pass")).samples("4", "4").fit()
                .build();
    }

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); ProbeStationBlockEntity.this.changed(); }
    };
    private final int[] pads = new int[MAX_PADS];
    private int found, probed, challenge, result;
    /** 今のお題で開いたヒントの段（0＝まだ）。お題を変えると 0 */
    private int hintLevel;
    /** パッドの状態を合わせたウェハーの中身（変わったらパッドを元に戻す） */
    private ChipState lastState;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return i < MAX_PADS ? pads[i] : i == DATA_FOUND ? found : i == DATA_PROBED ? probed : i == DATA_CHALLENGE ? challenge : i == DATA_RESULT ? result : 0; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public ProbeStationBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.PROBE_STATION.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public int padState(int i) { return pads[i]; }
    public int found() { return found; }
    public int challenge() { return challenge; }
    public int result() { return result; }
    public int hintLevel() { return hintLevel; }

    private void changed() {
        ChipState s = container.getItem(SLOT_WAFER).get(ModDataComponents.CHIP_STATE);
        if (!java.util.Objects.equals(s, lastState)) {   // ちがうウェハー：パッドと結果を元に戻す
            lastState = s;
            java.util.Arrays.fill(pads, OPEN);
            found = 0; probed = 0; result = RESULT_NONE;
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
            if (st == GROUND || st == IN_A || st == IN_B) d.put(p.get(i).id(), 0.0);   // 画面では入力は 0 V で見せる
            else if (st == VCC) d.put(p.get(i).id(), com.taku3.semiconductorlab.litho.Circuit.VCC);
        }
        return d;
    }

    /** パッドの状態 → お題の役目（線の番号 → 役目） */
    public static Map<Integer, com.taku3.semiconductorlab.litho.Circuit.Role> roles(Netlist nl, java.util.function.IntUnaryOperator padState) {
        Map<Integer, com.taku3.semiconductorlab.litho.Circuit.Role> r = new HashMap<>();
        List<Netlist.Net> p = nl.pads();
        for (int i = 0; i < p.size() && i < MAX_PADS; i++) {
            var role = switch (padState.applyAsInt(i)) {
                case GROUND -> com.taku3.semiconductorlab.litho.Circuit.Role.GROUND;
                case VCC -> com.taku3.semiconductorlab.litho.Circuit.Role.VCC;
                case IN_A -> com.taku3.semiconductorlab.litho.Circuit.Role.IN_A;
                case IN_B -> com.taku3.semiconductorlab.litho.Circuit.Role.IN_B;
                case OUT_1 -> com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1;
                case OUT_2 -> com.taku3.semiconductorlab.litho.Circuit.Role.OUT_2;
                default -> null;
            };
            if (role != null) r.put(p.get(i).id(), role);
        }
        return r;
    }

    /** チップ（加工中のウェハー）からハンコを作る：層を設計図に戻し、描いてある所で切り抜く。名前＝お題の id */
    public static ItemStack stampOf(ItemStack wafer, String name) {
        ChipState s = wafer.get(ModDataComponents.CHIP_STATE);
        if (s == null || !s.sized()) return ItemStack.EMPTY;
        ItemStack st = new ItemStack(Semiconductor.STAMP);
        st.set(ModDataComponents.CHIP_DESIGN, com.taku3.semiconductorlab.litho.ChipDesign.fromState(s, name).crop());
        return st;
    }

    /** お題 i は開いているか（最初のお題、または前のお題の進捗 challenge/&lt;前&gt; が済んでいる） */
    public static boolean unlocked(ServerPlayer sp, int i) {
        if (i <= 0) return true;
        var prev = com.taku3.semiconductorlab.litho.Challenge.byIndex(i - 1);
        AdvancementHolder adv = prev == null || sp.level().getServer() == null ? null : sp.level().getServer().getAdvancements().get(SemiconductorLab.id("challenge/" + prev.id()));
        return adv != null && sp.getAdvancements().getOrStartProgress(adv).isDone();
    }

    @Override public boolean onButton(Player player, int id) {
        Netlist nl = netlist();
        if (nl == null) return false;
        if (id >= 0 && id < MAX_PADS && id < nl.pads().size()) {
            pads[id] = (pads[id] + 1) % PAD_STATES;
            setChanged();
            return true;
        }
        if (id == BTN_CHALLENGE) {   // 次の開いているお題（なければ最初へ）
            int n = com.taku3.semiconductorlab.litho.Challenge.all().size();
            int next = (challenge + 1) % n;
            if (player instanceof ServerPlayer sp && !unlocked(sp, next)) next = 0;
            challenge = next;
            result = RESULT_NONE;
            hintLevel = 0;
            setChanged();
            return true;
        }
        if (id == BTN_HINT) {   // ヒントを1段ずつ自分で開く（§11.4）。開いたことは進捗 hint/<id> に残る（自力の発見ではなくなる）
            var def = com.taku3.semiconductorlab.litho.Challenge.byIndex(challenge);
            if (def == null || hintLevel >= HINTS) return false;
            hintLevel++;
            if (player instanceof ServerPlayer sp && level != null && level.getServer() != null) {
                sp.sendSystemMessage(Component.translatable(def.langKey() + ".hint" + hintLevel));
                AdvancementHolder used = level.getServer().getAdvancements().get(SemiconductorLab.id("hint/" + def.id()));
                if (used != null && !sp.getAdvancements().getOrStartProgress(used).isDone()) sp.getAdvancements().award(used, "opened");
            }
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
            // お題（v0.3b）：今のお題を真理値表で判定
            var def = com.taku3.semiconductorlab.litho.Challenge.byIndex(challenge);
            if (def != null) {
                var roleMap = roles(nl, i -> pads[i]);
                var r = com.taku3.semiconductorlab.litho.Challenge.check(def, nl, roleMap, leakScale);
                result = encodeResult(r);
                setChanged();
                if (!r.passed() && player instanceof ServerPlayer sp0)   // あと少しの手がかり（§11.2、正解の形は言わない）
                    sp0.sendSystemMessage(Component.translatable("litho.semiconductor-lab.near." + com.taku3.semiconductorlab.litho.NearMiss.diagnose(def, nl, roleMap, r, leakScale)));
                if (r.passed() && player instanceof ServerPlayer sp && level != null && level.getServer() != null) {
                    AdvancementHolder adv = level.getServer().getAdvancements().get(SemiconductorLab.id("challenge/" + def.id()));
                    if (adv != null && !sp.getAdvancements().getOrStartProgress(adv).isDone()) {
                        sp.getAdvancements().award(adv, "solved");
                        sp.sendSystemMessage(Component.translatable("litho.semiconductor-lab.challenge.solved", Component.translatable(def.langKey())));
                        AdvancementHolder used = level.getServer().getAdvancements().get(SemiconductorLab.id("hint/" + def.id()));
                        boolean self = used == null || !sp.getAdvancements().getOrStartProgress(used).isDone();
                        if (self) {   // ヒントを見ずに解いた＝自力で発見（§11.4）
                            AdvancementHolder own = level.getServer().getAdvancements().get(SemiconductorLab.id("self/" + def.id()));
                            if (own != null) sp.getAdvancements().award(own, "self");
                            sp.sendSystemMessage(Component.translatable("litho.semiconductor-lab.challenge.self", Component.translatable(def.langKey())));
                        }
                        sp.sendSystemMessage(Component.translatable(def.langKey() + ".history"));   // 歴史とつなぐ（§11.5）
                        ItemStack stamp = stampOf(container.getItem(SLOT_WAFER), def.id());   // 見つけた物はハンコになる（§11.3）
                        if (!stamp.isEmpty() && !sp.getInventory().add(stamp)) net.minecraft.world.level.block.Block.popResource(level, worldPosition.above(), stamp);
                    }
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
        out.putIntArray("Pads", pads.clone()); out.putInt("Found", found); out.putInt("Probed", probed); out.putInt("Challenge", challenge); out.putInt("Result", result); out.putInt("HintLevel", hintLevel);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        int[] p = in.getIntArray("Pads").orElse(new int[0]);
        for (int i = 0; i < MAX_PADS; i++) pads[i] = i < p.length ? p[i] : OPEN;
        found = in.getIntOr("Found", 0); probed = in.getIntOr("Probed", 0); challenge = in.getIntOr("Challenge", 0); result = in.getIntOr("Result", 0); hintLevel = in.getIntOr("HintLevel", 0);
        lastState = container.getItem(SLOT_WAFER).get(ModDataComponents.CHIP_STATE);
    }
}
