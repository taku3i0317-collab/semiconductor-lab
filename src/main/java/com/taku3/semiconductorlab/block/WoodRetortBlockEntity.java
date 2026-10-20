package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 乾留炉 [2-23]（IMP2-01）：鉄のレトルト（ふたつきの釜）に原木を入れ、外から石炭・木炭で熱する（約400〜500℃、空気は入れない）。
 * 出てくる煙を銅の管で冷やすと、水っぽい木酢液と、重い木タールに分かれてたまる。釜の中には木炭が残る。電気は使わない。
 * <pre>
 *   原木 1本 → 木炭 1（かまどと同じ）
 *            ＋ 2本ごとに 木酢液 1（酢酸 CH3COOH・メタノールを含む水）
 *            ＋ 4本ごとに 木タール 1
 * </pre>
 * 割合は FAO の広葉樹の値（乾いた木1トンから 木炭 約300kg、酢酸 50kg・メタノール 16kg を含む水の多い液、タール 約240kg）を、
 * 「1本＝木炭1」に合わせて丸めたもの。燃えるガス（CO・メタン）は燃やして捨てる（省略）。
 * スロット：0＝原木、1＝燃料、2＝木炭、3＝木酢液、4＝木タール
 */
public class WoodRetortBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_IN = 0, SLOT_FUEL = 1, SLOT_CHARCOAL = 2, SLOT_VINEGAR = 3, SLOT_TAR = 4, SLOT_COUNT = 5;
    /** 原木1本を乾留する時間（かまどと同じ 10秒） */
    public static final int DISTILL_TICKS = 200;
    /** 石炭・木炭1個が燃える時間（かまどと同じ＝8本分） */
    public static final int FUEL_TICKS = 1600;
    /** 何本ごとに木酢液・木タールが1個出るか */
    public static final int VINEGAR_EVERY = 2, TAR_EVERY = 4;
    public static final int DATA_BURN = 0, DATA_BURN_TOTAL = 1, DATA_PROGRESS = 2, DATA_TOTAL = 3, DATA_COUNT = 4;

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); WoodRetortBlockEntity.this.setChanged(); }
    };
    private int burnTicks, progress;
    /** 乾留した原木の数（木酢液・タールの割合を決める。保存する） */
    private int logsDone;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> FUEL_TICKS;
            case DATA_PROGRESS -> progress; case DATA_TOTAL -> DISTILL_TICKS; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    /** 画面の中のスロットの位置（画面の絵と合わせる。反射炉と同じ並び） */
    public static final int OUT_X = 104, OUT_Y = 22;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_IN, 44, 17, WoodRetortBlockEntity::isInput)
            .in(SLOT_FUEL, 44, 53, WoodRetortBlockEntity::isFuel)
            .out(SLOT_CHARCOAL, OUT_X, OUT_Y)
            .out(SLOT_VINEGAR, OUT_X + 22, OUT_Y)
            .out(SLOT_TAR, OUT_X, OUT_Y + 22)
            .build();

    public WoodRetortBlockEntity(BlockPos pos, BlockState state) { super(Chemistry.WOOD_RETORT.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public int getLogsDone() { return logsDone; }

    public static boolean isFuel(ItemStack s) { return s.is(Items.COAL) || s.is(Items.CHARCOAL); }
    /** 燃える原木（ネザーの幹はキノコなので木ではない＝ LOGS_THAT_BURN） */
    public static boolean isInput(ItemStack s) { return s.is(ItemTags.LOGS_THAT_BURN); }

    private static boolean canMerge(ItemStack out, ItemStack add) {
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(out, add) && out.getCount() + add.getCount() <= out.getMaxStackSize();
    }
    private void merge(int slot, ItemStack add) {
        ItemStack out = container.getItem(slot);
        if (out.isEmpty()) container.setItem(slot, add.copy());
        else { out.grow(add.getCount()); container.setChanged(); }
    }

    /** 原木があり、3つの出口すべてに空きがあるか */
    private boolean canDistill() {
        return isInput(container.getItem(SLOT_IN))
                && canMerge(container.getItem(SLOT_CHARCOAL), new ItemStack(Items.CHARCOAL))
                && canMerge(container.getItem(SLOT_VINEGAR), new ItemStack(Chemistry.WOOD_VINEGAR))
                && canMerge(container.getItem(SLOT_TAR), new ItemStack(Chemistry.WOOD_TAR));
    }

    private void finishDistill() {
        container.getItem(SLOT_IN).shrink(1);
        merge(SLOT_CHARCOAL, new ItemStack(Items.CHARCOAL));
        logsDone++;
        if (logsDone % VINEGAR_EVERY == 0) merge(SLOT_VINEGAR, new ItemStack(Chemistry.WOOD_VINEGAR));
        if (logsDone % TAR_EVERY == 0) merge(SLOT_TAR, new ItemStack(Chemistry.WOOD_TAR));
        container.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WoodRetortBlockEntity be) {
        if (level.isClientSide()) return;
        boolean can = be.canDistill();
        boolean changed = false;
        // 火が消えていて、乾留する物があれば燃料を1個くべる（空焚きはしない）
        if (be.burnTicks <= 0 && can) {
            ItemStack fuel = be.container.getItem(SLOT_FUEL);
            if (isFuel(fuel)) { fuel.shrink(1); be.burnTicks = FUEL_TICKS; be.container.setChanged(); changed = true; }
        }
        if (be.burnTicks > 0) {
            be.burnTicks--;
            changed = true;
            if (can) {
                if (++be.progress >= DISTILL_TICKS) { be.progress = 0; be.finishDistill(); }
            } else if (be.progress != 0) be.progress = 0;
        } else if (be.progress != 0) { be.progress = 0; changed = true; }
        if (changed) be.setChanged();
        boolean lit = be.burnTicks > 0;
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), 3);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Burn", burnTicks); out.putInt("Progress", progress); out.putInt("LogsDone", logsDone);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        burnTicks = in.getIntOr("Burn", 0); progress = in.getIntOr("Progress", 0); logsDone = in.getIntOr("LogsDone", 0);
    }
}
