package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 反射炉 [3-06] を「焙焼炉」として使う（石炭・木炭を燃やす。電気は使わない）。
 * 燃焼室と炉床が分かれていて、炎の熱だけが天井で反射して鉱石に当たる＝燃料の灰・硫黄が鉱石に混ざらない。
 * 硫化鉱を溶かさずに空気中で焼き（焙焼、約600〜800℃）、硫黄を SO2 として煙道から集める。
 * <pre>
 *   方鉛鉱       → 一酸化鉛 ＋ SO2        2PbS + 3O2 → 2PbO + 2SO2
 *   辰砂の粉     → 水銀     ＋ SO2        HgS + O2 → Hg + SO2（水銀は蒸気で出て冷えて液体に）
 *   閃亜鉛鉱     → 酸化亜鉛 ＋ SO2        2ZnS + 3O2 → 2ZnO + 2SO2
 *                  ＋ 4個ごとに Ge精鉱、8個ごとにインジウム（副産物スロット。#16 の「作業台で分割」を置換）
 * </pre>
 * スロット：0＝鉱石、1＝燃料、2＝主生成物、3＝SO2、4＝副産物（Ge精鉱）、5＝副産物（インジウム）
 */
public class ReverberatoryFurnaceBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_IN = 0, SLOT_FUEL = 1, SLOT_OUT = 2, SLOT_SO2 = 3, SLOT_BY_GE = 4, SLOT_BY_IN = 5, SLOT_COUNT = 6;
    /** 1個を焼く時間（かまどと同じ 10秒） */
    public static final int ROAST_TICKS = 200;
    /** 石炭・木炭1個が燃える時間（かまどと同じ＝8個分） */
    public static final int FUEL_TICKS = 1600;
    /** 閃亜鉛鉱を何個焼くごとに副産物が1個出るか（作業台の旧レシピ ×4・×8 と同じ割合） */
    public static final int GE_EVERY = 4, IN_EVERY = 8;
    public static final int DATA_BURN = 0, DATA_BURN_TOTAL = 1, DATA_PROGRESS = 2, DATA_TOTAL = 3, DATA_COUNT = 4;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); ReverberatoryFurnaceBlockEntity.this.setChanged(); }
    };
    private int burnTicks, progress;
    /** 焼いた閃亜鉛鉱の数（副産物の割合を決める。保存する） */
    private int sphaleriteRoasted;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> FUEL_TICKS;
            case DATA_PROGRESS -> progress; case DATA_TOTAL -> ROAST_TICKS; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    /** 画面のスロットの並び（IMP-28：前の ReverberatoryFurnaceMenu の代わり） */
    /** 画面の中のスロットの位置（画面の絵と合わせる） */
    public static final int OUT_X = 104, OUT_Y = 22;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_IN, 44, 17, ReverberatoryFurnaceBlockEntity::isInput)
            .in(SLOT_FUEL, 44, 53, ReverberatoryFurnaceBlockEntity::isFuel)
            .out(SLOT_OUT, OUT_X, OUT_Y)
            .out(SLOT_SO2, OUT_X + 22, OUT_Y)
            .out(SLOT_BY_GE, OUT_X, OUT_Y + 22)
            .out(SLOT_BY_IN, OUT_X + 22, OUT_Y + 22)
            .text("hint", 64, 64, 108, 2, "gui.semiconductor-lab.reverberatory_furnace.need_input", "gui.semiconductor-lab.reverberatory_furnace.need_fuel", "gui.semiconductor-lab.reverberatory_furnace.roasting", "gui.semiconductor-lab.reverberatory_furnace.out_full")
            .label("so2", OUT_X + 22 + 18, OUT_Y + 4, 18, "SO2")
            .build();

    public ReverberatoryFurnaceBlockEntity(BlockPos pos, BlockState state) { super(Heat.REVERBERATORY_FURNACE.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public net.minecraft.world.inventory.ContainerData getMenuData() { return data; }
    public int getSphaleriteRoasted() { return sphaleriteRoasted; }

    public static boolean isFuel(ItemStack s) { return s.is(Items.COAL) || s.is(Items.CHARCOAL); }
    public static boolean isInput(ItemStack s) { return !mainProduct(s).isEmpty(); }

    /** 焙焼の主生成物（金属酸化物 or 金属）。焼けない物は EMPTY */
    public static ItemStack mainProduct(ItemStack in) {
        Item out = null;
        if (in.is(Metallurgy.GALENA)) out = Metallurgy.LITHARGE;
        else if (in.is(Metallurgy.CINNABAR_RETORT_CHARGE)) out = Metallurgy.MERCURY;   // 辰砂＋空の鉄のフラスコ（受け器）→ 水銀入りのフラスコ（IMP-33）
        else if (in.is(Metallurgy.SPHALERITE)) out = Heat.ZINC_OXIDE;
        return out == null ? ItemStack.EMPTY : new ItemStack(out);
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

    /** 鉱石があり、主生成物・SO2・（閃亜鉛鉱なら）副産物の出口すべてに空きがあるか */
    private boolean canRoast() {
        ItemStack in = container.getItem(SLOT_IN);
        ItemStack main = mainProduct(in);
        if (main.isEmpty()) return false;
        if (!canMerge(container.getItem(SLOT_OUT), main)) return false;
        if (!canMerge(container.getItem(SLOT_SO2), new ItemStack(Chemistry.SULFUR_DIOXIDE))) return false;
        if (in.is(Metallurgy.SPHALERITE)) {
            return canMerge(container.getItem(SLOT_BY_GE), new ItemStack(Metallurgy.GERMANIUM_CONCENTRATE))
                    && canMerge(container.getItem(SLOT_BY_IN), new ItemStack(Metallurgy.INDIUM));
        }
        return true;
    }

    private void finishRoast() {
        ItemStack in = container.getItem(SLOT_IN);
        ItemStack main = mainProduct(in);
        boolean sphalerite = in.is(Metallurgy.SPHALERITE);
        in.shrink(1);
        merge(SLOT_OUT, main);
        merge(SLOT_SO2, new ItemStack(Chemistry.SULFUR_DIOXIDE));   // 硫黄1つ分 → SO2 1つ（煙道で集める）
        if (sphalerite) {
            sphaleriteRoasted++;
            if (sphaleriteRoasted % GE_EVERY == 0) merge(SLOT_BY_GE, new ItemStack(Metallurgy.GERMANIUM_CONCENTRATE));
            if (sphaleriteRoasted % IN_EVERY == 0) merge(SLOT_BY_IN, new ItemStack(Metallurgy.INDIUM));
        }
        container.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ReverberatoryFurnaceBlockEntity be) {
        if (level.isClientSide()) return;
        boolean can = be.canRoast();
        boolean changed = false;
        // 火が消えていて、焼く物があれば燃料を1個くべる（かまどと同じ。空焚きはしない）
        if (be.burnTicks <= 0 && can) {
            ItemStack fuel = be.container.getItem(SLOT_FUEL);
            if (isFuel(fuel)) { fuel.shrink(1); be.burnTicks = FUEL_TICKS; be.container.setChanged(); changed = true; }
        }
        if (be.burnTicks > 0) {
            be.burnTicks--;
            changed = true;
            if (can) {
                if (++be.progress >= ROAST_TICKS) { be.progress = 0; be.finishRoast(); }
            } else if (be.progress != 0) be.progress = 0;
        } else if (be.progress != 0) { be.progress = 0; changed = true; }   // 火が消えたら焼きかけは戻る（かまどは徐々に戻るが簡略化）
        if (changed) be.setChanged();
        boolean lit = be.burnTicks > 0;
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), 3);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Burn", burnTicks); out.putInt("Progress", progress); out.putInt("SphaleriteRoasted", sphaleriteRoasted);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        burnTicks = in.getIntOr("Burn", 0); progress = in.getIntOr("Progress", 0); sphaleriteRoasted = in.getIntOr("SphaleriteRoasted", 0);
    }
}
