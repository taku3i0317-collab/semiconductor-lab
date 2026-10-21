package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 電気こんろ [3-24]（ニクロム線のヒーター、1906 マーシュの合金。IMP4-01、O-030 案A 仮決定）：ニクロム線に電流を流して赤く熱し（約 {@link #HEATER_C}℃ ⚠️）、
 * 上にのせた物を焼く。食べ物を焼く・あぶる（バニラの燻製器と同じレシピ、1個 {@link #TICKS} tick）。電気 {@link #WATTS} W。燃料も煙も出ない。
 * かまど（約1000℃）より低いので、鉱石は溶かせない。
 */
public class ElectricStoveBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int HEATER_C = 800, TICKS = 100;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_IN, 8, 35, s -> true)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.electric_stove.power").samples("0.5 kW")
            .right("heater", 28, 1, "gui.semiconductor-lab.electric_stove.heater").samples("800")
            .right("hint", 40, 3, "gui.semiconductor-lab.electric_stove.need_food", "gui.semiconductor-lab.electric_stove.output_full",
                    "gui.semiconductor-lab.electric_stove.need_power", "gui.semiconductor-lab.electric_stove.cooking")
            .bottom("recipe", "gui.semiconductor-lab.electric_stove.recipe")
            .build();

    public ElectricStoveBlockEntity(BlockPos pos, BlockState state) {
        super(Heat.ELECTRIC_STOVE.type, pos, state, SLOT_COUNT, 2_000, WATTS);
    }

    /** 焼くと何になるか（燻製器のレシピ。なければ EMPTY）。テストからも呼ぶ */
    public static ItemStack cooked(Level level, ItemStack in) {
        if (!(level instanceof ServerLevel sl) || in.isEmpty()) return ItemStack.EMPTY;
        var input = new SingleRecipeInput(in.copyWithCount(1));
        return sl.getServer().getRecipeManager().getRecipeFor(RecipeType.SMOKING, input, sl).map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        if (level == null) return false;
        ItemStack out = cooked(level, container.getItem(SLOT_IN));
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        if (level == null) return;
        ItemStack out = cooked(level, container.getItem(SLOT_IN));
        if (out.isEmpty()) return;
        container.getItem(SLOT_IN).shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
