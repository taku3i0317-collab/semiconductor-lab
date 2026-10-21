package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * 穴あきテープ [8-34]（IMP2 O-038 案A）。紙のテープに穴の並びで数字（刃物の位置）を記録する。
 * 白いテープを持ち物に入れたままマイクロメータ [8-04] で旋盤の部品を測ると、その部品の形がテープに記録される（白いテープ1本が記録したテープになる）。
 * NC 旋盤で {@value #RUNS} 回読むと穴が傷んで読めなくなる（⚠️ ゲームの値）
 */
public class PunchedTapeItem extends Item {
    public static final int RUNS = 64;
    private static final String K = "item.semiconductor-lab.punched_tape.";

    public PunchedTapeItem(Properties properties) { super(properties); }

    private static CompoundTag tag(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? new CompoundTag() : d.copyTag();
    }

    /** 記録した部品の ID の名前（"pump_rotor"・"standard_screw"）。白いテープは "" */
    public static String part(ItemStack s) { return s.is(Precision.PUNCHED_TAPE) ? tag(s).getStringOr("Part", "") : ""; }
    public static double mm(ItemStack s) { return tag(s).getDoubleOr("Mm", 0); }
    public static int used(ItemStack s) { return tag(s).getIntOr("Used", 0); }

    public static boolean isRecorded(ItemStack s) { return !part(s).isEmpty(); }
    public static boolean usable(ItemStack s) { return isRecorded(s) && used(s) < RUNS; }

    /** 部品の形をテープに記録した1本（部品が測れない物なら EMPTY） */
    public static ItemStack record(ItemStack part) {
        double mm = GaugeItem.nominalMm(part);
        if (mm <= 0) return ItemStack.EMPTY;
        ItemStack tape = new ItemStack(Precision.PUNCHED_TAPE);
        CompoundTag t = new CompoundTag();
        t.putString("Part", BuiltInRegistries.ITEM.getKey(part.getItem()).getPath());
        t.putDouble("Mm", mm);
        t.putInt("Used", 0);
        tape.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return tape;
    }

    /** 1回読んだ（穴が少し傷む） */
    public static void wear(ItemStack s) {
        if (!isRecorded(s)) return;
        CompoundTag t = tag(s);
        t.putInt("Used", t.getIntOr("Used", 0) + 1);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** マイクロメータで測ったときに呼ぶ：持ち物の白いテープ1本を、この部品を記録したテープにする */
    public static boolean recordFromInventory(Player p, ItemStack part) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Precision.PUNCHED_TAPE) && !isRecorded(s)) {
                ItemStack rec = record(part);
                if (rec.isEmpty()) return false;
                s.shrink(1);
                inv.placeItemBackInInventory(rec, net.minecraft.util.Prediction.SERVER_ONLY);
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        if (!isRecorded(stack)) { out.accept(Component.translatable(K + "blank")); return; }
        out.accept(Component.translatable(K + "recorded", Component.translatable("item.semiconductor-lab." + part(stack)),
                String.format(Locale.ROOT, "%.2f", mm(stack)), Math.max(0, RUNS - used(stack))));
    }
}
