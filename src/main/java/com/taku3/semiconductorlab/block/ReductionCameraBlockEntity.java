package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 縮小カメラ [7-11]（IMP2-03）：1960年ごろのエマルジョンマスクの作り方。大きく描いた原図（rubylith）を照明して、
 * 色消しレンズで小さく縮めて感光乾板に写し、現像（硫酸鉄(II)）・定着（ハイポ）してフォトマスクにする。
 * 原図は減らない（同じ原図から何枚でもマスクを作れる＝写真のネガと同じ）。
 * 写すのは原図の成分 semiconductor-lab:mask（LITHO_DESIGN §4.3 の MaskData）そのもの。中身は見ない（litho の形が変わっても動く）。
 */
public class ReductionCameraBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 30;
    public static final int SLOT_ORIGINAL = 0, SLOT_PLATE = 1, SLOT_DEVELOPER = 2, SLOT_FIXER = 3, SLOT_OUT = 4, SLOT_COUNT = 5;

    /** 原図として使える物：mask の成分を持ち、フォトマスクそのものではない物（原図 rubylith は A が登録） */
    public static boolean isOriginal(ItemStack s) { return !s.isEmpty() && s.has(ModDataComponents.MASK) && !s.is(Optics.PHOTOMASK) && !s.is(Optics.CHROME_PHOTOMASK); }

    /** [7-12] 乳剤（銀塩）で描ける一番細かい方眼。乳剤の粒（数µm）より細かい線は描けない（IMP2 O-015）。クロムは制限なし */
    public static final int EMULSION_MAX_GRID = 12;

    public static boolean isPlate(ItemStack s) { return s.is(Optics.PHOTO_PLATE) || s.is(Optics.CHROME_BLANK); }
    public static boolean isDeveloper(ItemStack s) { return s.is(Optics.FERROUS_SULFATE) || s.is(com.taku3.semiconductorlab.content.Chemistry.POSITIVE_RESIST); }
    public static boolean isFixer(ItemStack s) { return s.is(Optics.SODIUM_THIOSULFATE) || s.is(com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID); }

    /** 板に合う薬：乾板＝硫酸鉄(II)＋ハイポ、クロムのブランク＝ポジ型レジスト＋塩酸（Cr を溶かす） */
    public static boolean chemicalsMatch(ItemStack plate, ItemStack dev, ItemStack fix) {
        if (plate.is(Optics.PHOTO_PLATE)) return dev.is(Optics.FERROUS_SULFATE) && fix.is(Optics.SODIUM_THIOSULFATE);
        if (plate.is(Optics.CHROME_BLANK)) return dev.is(com.taku3.semiconductorlab.content.Chemistry.POSITIVE_RESIST) && fix.is(com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID);
        return false;
    }

    /** その板で、その原図の細かさが描けるか */
    public static boolean fineEnough(ItemStack plate, ItemStack original) {
        var m = original.get(ModDataComponents.MASK);
        return m != null && (!plate.is(Optics.PHOTO_PLATE) || m.size() <= EMULSION_MAX_GRID);
    }

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_ORIGINAL, 8, 17, ReductionCameraBlockEntity::isOriginal)
            .in(SLOT_PLATE, 8, 35, ReductionCameraBlockEntity::isPlate)
            .in(SLOT_DEVELOPER, 8, 53, ReductionCameraBlockEntity::isDeveloper)
            .in(SLOT_FIXER, 30, 53, ReductionCameraBlockEntity::isFixer)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.reduction_camera.power").samples("1.0 kW")
            .right("hint", 30, 3, "gui.semiconductor-lab.reduction_camera.need_original", "gui.semiconductor-lab.reduction_camera.need_plate",
                    "gui.semiconductor-lab.reduction_camera.need_chemicals", "gui.semiconductor-lab.reduction_camera.too_fine", "gui.semiconductor-lab.reduction_camera.output_full",
                    "gui.semiconductor-lab.reduction_camera.need_power", "gui.semiconductor-lab.reduction_camera.running")
            .build();

    public ReductionCameraBlockEntity(BlockPos pos, BlockState state) {
        super(Optics.REDUCTION_CAMERA.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** できるフォトマスク（原図の mask 成分を写す） */
    public static ItemStack photomaskOf(ItemStack original) { return photomaskOf(original, new ItemStack(Optics.PHOTO_PLATE)); }

    /** できるフォトマスク：乾板なら銀塩マスク、クロムのブランクならクロムマスク */
    public static ItemStack photomaskOf(ItemStack original, ItemStack plate) {
        ItemStack m = new ItemStack(plate.is(Optics.CHROME_BLANK) ? Optics.CHROME_PHOTOMASK : Optics.PHOTOMASK);
        m.set(ModDataComponents.MASK, original.get(ModDataComponents.MASK));
        return m;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack plate = container.getItem(SLOT_PLATE), original = container.getItem(SLOT_ORIGINAL);
        return isOriginal(original) && isPlate(plate) && chemicalsMatch(plate, container.getItem(SLOT_DEVELOPER), container.getItem(SLOT_FIXER))
                && fineEnough(plate, original) && container.getItem(SLOT_OUT).isEmpty();
    }

    @Override protected void finishWork() {
        container.setItem(SLOT_OUT, photomaskOf(container.getItem(SLOT_ORIGINAL), container.getItem(SLOT_PLATE)));   // 原図は減らない
        container.getItem(SLOT_PLATE).shrink(1);
        use(SLOT_DEVELOPER);
        use(SLOT_FIXER);
        container.setChanged();
    }

    /** 薬を1つ使う。瓶入り（レジスト・塩酸）なら空き瓶を機械の上に落とす */
    private void use(int slot) {
        ItemStack s = container.getItem(slot);
        var rem = s.getItem().getCraftingRemainder();
        s.shrink(1);
        if (rem != null && level != null)
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), rem.create());
    }
}
