package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * 水晶片（AT カット）[8-15]（IMP2 O-016）。厚さ（成分 BLANK_THICKNESS_UM、µm）で振動数が決まる：f ＝ {@value #N_KHZ_MM} kHz·mm ÷ 厚さ。
 * 利き手に持って右クリック → 持ち物のルージュを1つ使って {@value #LAP_UM}µm 薄く磨く（振動数が上がる）。目標の振動数に近づけるのが遊び（本物も磨いて合わせた）。
 */
public class QuartzBlankItem extends Item {
    /** AT カットの周波数定数（kHz·mm） */
    public static final double N_KHZ_MM = 1661;
    public static final int START_UM = 500, LAP_UM = 20, MIN_UM = 100;
    protected static final String K = "item.semiconductor-lab.quartz_blank.";

    public QuartzBlankItem(Properties properties) { super(properties); }

    public static int thicknessUm(ItemStack s) { return s.getOrDefault(Precision.BLANK_THICKNESS_UM, START_UM); }

    /** 振動数（MHz） */
    public static double freqMHz(int thicknessUm) { return N_KHZ_MM / thicknessUm; }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        ItemStack s = p.getItemInHand(hand);
        int t = thicknessUm(s);
        if (t - LAP_UM < MIN_UM) {
            if (!level.isClientSide()) p.sendSystemMessage(Component.translatable(K + "too_thin"));
            return InteractionResult.FAIL;
        }
        int slot = p.getInventory().findSlotMatchingItem(new ItemStack(Optics.ROUGE));
        if (slot < 0 && !p.getAbilities().instabuild) {
            if (!level.isClientSide()) p.sendSystemMessage(Component.translatable("item.semiconductor-lab.cast_iron_plate.no_rouge"));
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (slot >= 0 && !p.getAbilities().instabuild) p.getInventory().removeItem(slot, 1);
        s.set(Precision.BLANK_THICKNESS_UM, t - LAP_UM);
        p.sendSystemMessage(Component.translatable(K + "lapped", t - LAP_UM, String.format(Locale.ROOT, "%.3f", freqMHz(t - LAP_UM))));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        int t = thicknessUm(stack);
        out.accept(Component.translatable(K + "spec", t, String.format(Locale.ROOT, "%.3f", freqMHz(t))));
    }
}
