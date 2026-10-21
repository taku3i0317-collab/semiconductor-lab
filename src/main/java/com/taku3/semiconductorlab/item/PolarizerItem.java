package com.taku3.semiconductorlab.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * 偏光フィルム [B-06]（ランドの H シート 1938。IMP3、O-023）。実験：両手に1枚ずつ持って右クリックすると、利き手の1枚を {@link #STEP} 度まわし、
 * 2枚を通る光の明るさを出す（マリュスの法則 1809：明るさ ∝ cos²θ）。1枚だけなら、ふつうの光（いろいろな向きにゆれる）の半分が通る。
 * 角度はアイテムの CUSTOM_DATA「Angle」（0〜179）。⚠️ 理想のフィルム（本物の1枚は約40%、2枚を直角にしても少しもれる）。
 */
public class PolarizerItem extends Item {
    public static final int STEP = 15;

    public PolarizerItem(Properties p) { super(p); }

    public static int angle(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : d.copyTag().getIntOr("Angle", 0);
    }

    public static void setAngle(ItemStack s, int deg) {
        CompoundTag t = new CompoundTag();
        t.putInt("Angle", Math.floorMod(deg, 180));
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** ふつうの光を 100 としたときの明るさ（％）：1枚で 50、2枚で 50 cos²(角度の差) */
    public static double brightness(int filters, int angleDiffDeg) {
        if (filters <= 0) return 100;
        if (filters == 1) return 50;
        double c = Math.cos(Math.toRadians(angleDiffDeg));
        return 50 * c * c;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack main = p.getItemInHand(hand);
        ItemStack other = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!(other.getItem() instanceof PolarizerItem)) {
            p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.polarizing_film.one", String.format("%.0f", brightness(1, 0))));
            return InteractionResult.SUCCESS;
        }
        setAngle(main, angle(main) + STEP);
        int diff = Math.floorMod(angle(main) - angle(other), 180);
        p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.polarizing_film.two", diff, String.format("%.0f", brightness(2, diff))));
        return InteractionResult.SUCCESS;
    }
}
