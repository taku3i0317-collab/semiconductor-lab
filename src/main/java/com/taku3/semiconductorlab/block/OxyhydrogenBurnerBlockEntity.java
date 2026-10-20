package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 酸水素バーナー [7-02]（ヘアの吹管 1801 → ゴーダンが 1839 年に石英を溶かした）。電気は使わない。
 * 水素ボンベと酸素ボンベ（[6-03] 圧縮機、1本 50mol）をつなぎ、2H2 + O2 → 2H2O の炎（約2800℃）で
 * 珪石 [1-46]（石英鉱脈の SiO2、融点 約1713℃）を溶かして溶融石英にする。
 * <pre>
 *   珪石 ×1 ＋ H2 4mol ＋ O2 2mol（10秒） → 溶融石英 ×1
 *   水素ボンベ1本＝石英 12.5個分、酸素ボンベ1本＝25個分（H2:O2 = 2:1 の量論比）
 * </pre>
 * ボンベは中のガスが足りなくなったときに1本ずつ「つなぎ」、ガスは BE の中（mol）に移る。空ボンベは右下の出口へ。
 * スロット：0＝石英、1＝水素ボンベ、2＝酸素ボンベ、3＝溶融石英、4＝空ボンベ
 */
public class OxyhydrogenBurnerBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_IN = 0, SLOT_H2 = 1, SLOT_O2 = 2, SLOT_OUT = 3, SLOT_EMPTY = 4, SLOT_COUNT = 5;
    /** 1個を溶かす時間（10秒） */
    public static final int MELT_TICKS = 200;
    /** 1個あたりのガス（mol）。熱の大半は逃げる（炎の効率 約1割）として、4mol×286kJ ≈ 1.1MJ で 約0.5kg の石英を 2000℃へ */
    public static final int H2_PER = 4, O2_PER = 2;
    /** ボンベ1本の中身（圧縮機 [6-03] と同じ 50mol） */
    public static final int MOL_PER_CYLINDER = CompressorBlockEntity.MOL_PER_CYLINDER;
    public static final int DATA_PROGRESS = 0, DATA_TOTAL = 1, DATA_H2 = 2, DATA_O2 = 3, DATA_COUNT = 4;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); OxyhydrogenBurnerBlockEntity.this.setChanged(); }
    };
    private int progress;
    /** つないだボンベから移したガス（mol、保存する） */
    private int h2Mol, o2Mol;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_PROGRESS -> progress; case DATA_TOTAL -> MELT_TICKS; case DATA_H2 -> h2Mol; case DATA_O2 -> o2Mol; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    /** 画面のスロットの並び（IMP-28：前の OxyhydrogenBurnerMenu の代わり） */
    /** 画面の中のスロットの位置（画面の絵と合わせる） */
    public static final int IN_X = 44, IN_Y = 17, H2_X = 26, O2_X = 62, GAS_Y = 53, OUT_X = 116, OUT_Y = 35, EMPTY_X = 148, EMPTY_Y = 53;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_IN, IN_X, IN_Y, OxyhydrogenBurnerBlockEntity::isInput)
            .in(SLOT_H2, H2_X, GAS_Y, s -> s.is(com.taku3.semiconductorlab.content.Gas.HYDROGEN_CYLINDER))
            .in(SLOT_O2, O2_X, GAS_Y, s -> s.is(com.taku3.semiconductorlab.content.Gas.OXYGEN_CYLINDER))
            .out(SLOT_OUT, OUT_X, OUT_Y)
            .out(SLOT_EMPTY, EMPTY_X, EMPTY_Y)
            .text("h2", H2_X - 2, GAS_Y - 10, 34, 1, "=H2 00")
            .text("o2", O2_X - 2, GAS_Y - 10, 34, 1, "=O2 00")
            .text("hint", 64, 20, 108, 1, "gui.semiconductor-lab.oxyhydrogen_burner.need_input", "gui.semiconductor-lab.oxyhydrogen_burner.melting", "gui.semiconductor-lab.oxyhydrogen_burner.need_h2", "gui.semiconductor-lab.oxyhydrogen_burner.need_o2", "gui.semiconductor-lab.oxyhydrogen_burner.out_full")
            .build();

    public OxyhydrogenBurnerBlockEntity(BlockPos pos, BlockState state) { super(Optics.OXYHYDROGEN_BURNER.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public net.minecraft.world.inventory.ContainerData getMenuData() { return data; }
    public int getH2() { return h2Mol; }
    public int getO2() { return o2Mol; }

    /** 入力は珪石 [1-46]（石英鉱脈の SiO2）。ネザークォーツは現実に無い素材なので受け付けない（化学ルール1、タスク13） */
    public static boolean isInput(ItemStack s) { return !productOf(s).isEmpty(); }

    /** 炎に入れるとできる物：珪石 → 溶融石英、[7-15]（IMP2、S-001）SiCl4 → 合成石英（炎の中の水で加水分解 SiCl4 + 2H2O → SiO2 + 4HCl、ハイド 1934） */
    public static ItemStack productOf(ItemStack in) {
        if (in.is(Metallurgy.VEIN_QUARTZ)) return new ItemStack(Optics.FUSED_QUARTZ);
        if (in.is(Metallurgy.SILICON_TETRACHLORIDE)) return new ItemStack(Optics.SYNTHETIC_QUARTZ);
        if (in.is(Optics.RUBY_FEED)) return new ItemStack(Optics.RUBY_BOULE);
        if (in.is(Optics.ULE_FEED)) return new ItemStack(Optics.ULE_GLASS);   // [7-21] SiCl4＋TiCl4 を炎で加水分解 → SiO2・TiO2 のガラス（ULE）   // [7-13] ベルヌーイ法 1902：粉を炎に落として溶かし、下の種の上に積もらせて単結晶に
        return ItemStack.EMPTY;
    }

    private static boolean canMerge(ItemStack out, ItemStack add) {
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(out, add) && out.getCount() + add.getCount() <= out.getMaxStackSize();
    }
    private void merge(int slot, ItemStack add) {
        ItemStack out = container.getItem(slot);
        if (out.isEmpty()) container.setItem(slot, add.copy());
        else { out.grow(add.getCount()); container.setChanged(); }
    }

    /** ガスが足りなければボンベを1本つなぐ（空ボンベの出口に空きがあるときだけ） */
    private void connect(int slot, net.minecraft.world.item.Item full, boolean hydrogen) {
        ItemStack cyl = container.getItem(slot);
        if (!cyl.is(full)) return;
        if (!canMerge(container.getItem(SLOT_EMPTY), new ItemStack(Gas.EMPTY_GAS_CYLINDER))) return;
        cyl.shrink(1);
        merge(SLOT_EMPTY, new ItemStack(Gas.EMPTY_GAS_CYLINDER));
        if (hydrogen) h2Mol += MOL_PER_CYLINDER; else o2Mol += MOL_PER_CYLINDER;
        container.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OxyhydrogenBurnerBlockEntity be) {
        if (level.isClientSide()) return;
        boolean can = isInput(be.container.getItem(SLOT_IN)) && canMerge(be.container.getItem(SLOT_OUT), productOf(be.container.getItem(SLOT_IN)));
        // 両方のガスがそろうときだけボンベを開ける（片方だけ開けても炎は出ない）
        boolean h2Ready = be.h2Mol >= H2_PER || be.container.getItem(SLOT_H2).is(Gas.HYDROGEN_CYLINDER);
        boolean o2Ready = be.o2Mol >= O2_PER || be.container.getItem(SLOT_O2).is(Gas.OXYGEN_CYLINDER);
        if (can && h2Ready && o2Ready) {
            if (be.h2Mol < H2_PER) be.connect(SLOT_H2, Gas.HYDROGEN_CYLINDER, true);
            if (be.o2Mol < O2_PER) be.connect(SLOT_O2, Gas.OXYGEN_CYLINDER, false);
        }
        boolean burning = can && be.h2Mol >= H2_PER && be.o2Mol >= O2_PER;
        if (burning) {
            if (++be.progress >= MELT_TICKS) {
                be.progress = 0;
                be.h2Mol -= H2_PER;   // 2H2 + O2 → 2H2O（水蒸気になって出ていく）
                be.o2Mol -= O2_PER;
                ItemStack made = productOf(be.container.getItem(SLOT_IN));
                be.container.getItem(SLOT_IN).shrink(1);
                be.merge(SLOT_OUT, made);
                be.container.setChanged();
            }
            be.setChanged();
        } else if (be.progress != 0) { be.progress = 0; be.setChanged(); }   // 炎が消えたら溶けかけは固まる
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != burning)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, burning), 3);
    }

    /** 壊すと中身は落ちる。つないだボンベのガスは逃げる（ボンベに戻らない） */
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Progress", progress); out.putInt("H2", h2Mol); out.putInt("O2", o2Mol);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        progress = in.getIntOr("Progress", 0); h2Mol = in.getIntOr("H2", 0); o2Mol = in.getIntOr("O2", 0);
    }
}
