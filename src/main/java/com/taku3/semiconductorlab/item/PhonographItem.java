package com.taku3.semiconductorlab.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * 蓄音機 [15-05]（IMP2 S-010、1877 エジソン）。声で振動板をふるわせ、針で回る円筒の錫はくに溝を刻む。溝を針でなぞると声に戻る。
 * もう片方の手に「録音していない錫はくの筒」を持ってチャットで話すと、その文が録音される。録音した筒を持って右クリックで再生。
 * 錫はくは柔らかいので {@value #PLAYS} 回ほどで溝がつぶれて聞こえなくなる（本物もそうだった）。
 */
public class PhonographItem extends Item {
    public static final int PLAYS = 3;
    private static final String K = "item.semiconductor-lab.phonograph.";
    private final java.util.function.Supplier<Item> cylinder;

    public PhonographItem(Properties properties, java.util.function.Supplier<Item> cylinder) { super(properties); this.cylinder = cylinder; }

    public static String recording(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? "" : d.copyTag().getStringOr("Rec", "");
    }

    public static int played(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : d.copyTag().getIntOr("Played", 0);
    }

    public static void record(ItemStack s, String text) {
        CompoundTag t = new CompoundTag();
        t.putString("Rec", text.length() > 80 ? text.substring(0, 80) : text);
        t.putInt("Played", 0);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** チャットを、蓄音機と白い錫はくの筒を両手に持っている人の筒に録音する */
    public static void onChat(String text, ServerPlayer p) {
        ItemStack a = p.getMainHandItem(), b = p.getOffhandItem();
        ItemStack foil = a.getItem() instanceof PhonographItem ? b : b.getItem() instanceof PhonographItem ? a : ItemStack.EMPTY;
        PhonographItem ph = (PhonographItem) (a.getItem() instanceof PhonographItem ? a.getItem() : b.getItem() instanceof PhonographItem ? b.getItem() : null);
        if (ph == null || !foil.is(ph.cylinder.get()) || !recording(foil).isEmpty()) return;
        record(foil, text);
        p.sendSystemMessage(Component.translatable(K + "recorded"));
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack foil = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!foil.is(cylinder.get())) { p.sendSystemMessage(Component.translatable(K + "hint")); return InteractionResult.SUCCESS; }
        String rec = recording(foil);
        if (rec.isEmpty()) { p.sendSystemMessage(Component.translatable(K + "blank")); return InteractionResult.SUCCESS; }
        int n = played(foil) + 1;
        if (n > PLAYS) { p.sendSystemMessage(Component.translatable(K + "worn")); return InteractionResult.SUCCESS; }
        CompoundTag t = foil.get(DataComponents.CUSTOM_DATA).copyTag();
        t.putInt("Played", n);
        foil.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        p.sendSystemMessage(Component.translatable(K + (n < PLAYS ? "play" : "play_faint"), rec, n, PLAYS));
        return InteractionResult.SUCCESS;
    }
}
