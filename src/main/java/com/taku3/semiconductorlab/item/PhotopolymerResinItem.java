package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.HeliographPlateBlock;
import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 光で固まる樹脂 [2-45]：アクリルの単量体（グリセリンのトリアクリレート）＋光開始剤（ベンゾフェノン）＋アミン。
 * 紫外線を受けるとベンゾフェノンがアミンから水素をうばってラジカルを作り、アクリルの C=C が次々につながって（3本の手で）網になり、固まる。
 * 持ったまま日なた（昼・真上が空）にいると、スタックごと「固まった樹脂」に変わる（DNQ [2-57] と同じ判定）。日かげ・チェストでは固まらない。
 */
public class PhotopolymerResinItem extends Item {
    public static final int CHECK_TICKS = 40;

    public PhotopolymerResinItem(Properties properties) { super(properties); }

    /** 光で固める（テストからも呼ぶ）：同じ数の固まった樹脂をプレイヤーに渡し、このスタックを消す */
    public static void cure(ItemStack stack, Player p) {
        int n = stack.getCount();
        stack.setCount(0);
        ItemStack cured = new ItemStack(Chemistry.CURED_PHOTOPOLYMER, n);
        if (!p.getInventory().add(cured) && !cured.isEmpty())
            p.level().addFreshEntity(new ItemEntity(p.level(), p.getX(), p.getY(), p.getZ(), cured));
        p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.photopolymer_resin.cured"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % CHECK_TICKS != 0 || !(entity instanceof Player p)) return;
        if (level.isBrightOutside() && HeliographPlateBlock.openToSky(level, p.blockPosition().above())) cure(stack, p);
    }
}
