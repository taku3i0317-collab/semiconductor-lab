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

import java.util.function.Supplier;

/**
 * 磁気録音機（テレグラフォン）[15-12]（IMP2 S-010、1898 ポールセン）。電話の声の電流を電磁石に流し、走る鋼の線を声に合わせて磁化して記録する。
 * 読むときは、磁化した線が電磁石のそばを通ると電流が生まれ、電話から声が戻る。
 * <ul>
 * <li>もう片方の手に「何も記録していない」鋼線の巻き（{@value #WIRE_CHARS} 文字まで）か磁気テープ（1928 プフロイマー、{@value #TAPE_CHARS} 文字まで）を持ってチャットで話すと記録</li>
 * <li>記録した物を持って右クリックで再生。蓄音機の錫はく [15-05] とちがい、何度聞いても消えない（溝ではなく磁気）</li>
 * <li>しゃがんで右クリックで消す（強い磁気で向きをそろえ直す）→ また記録できる</li>
 * </ul>
 */
public class TelegraphoneItem extends Item {
    public static final int WIRE_CHARS = 40, TAPE_CHARS = 160;
    private static final String K = "item.semiconductor-lab.telegraphone.";
    private final Supplier<Item> wire, tape;

    public TelegraphoneItem(Properties properties, Supplier<Item> wire, Supplier<Item> tape) { super(properties); this.wire = wire; this.tape = tape; }

    /** 記録できる物なら、その文字数の上限（でなければ 0） */
    public int capacity(ItemStack s) { return s.is(wire.get()) ? WIRE_CHARS : s.is(tape.get()) ? TAPE_CHARS : 0; }

    public static String recording(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? "" : d.copyTag().getStringOr("Rec", "");
    }

    /** 記録する（上限で切る）。上限を超えて切ったら true */
    public boolean record(ItemStack medium, String text) {
        int cap = capacity(medium);
        CompoundTag t = new CompoundTag();
        t.putString("Rec", text.length() > cap ? text.substring(0, cap) : text);
        medium.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return text.length() > cap;
    }

    public static void erase(ItemStack medium) { medium.remove(DataComponents.CUSTOM_DATA); }

    /** チャットを、録音機と何も記録していない鋼線・テープを両手に持っている人の物に記録する */
    public static void onChat(String text, ServerPlayer p) {
        ItemStack a = p.getMainHandItem(), b = p.getOffhandItem();
        TelegraphoneItem t = a.getItem() instanceof TelegraphoneItem x ? x : b.getItem() instanceof TelegraphoneItem y ? y : null;
        if (t == null) return;
        ItemStack medium = a.getItem() instanceof TelegraphoneItem ? b : a;
        if (t.capacity(medium) == 0 || !recording(medium).isEmpty()) return;
        boolean cut = t.record(medium, text);
        p.sendSystemMessage(Component.translatable(K + (cut ? "recorded_cut" : "recorded"), t.capacity(medium)));
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack medium = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (capacity(medium) == 0) { p.sendSystemMessage(Component.translatable(K + "hint")); return InteractionResult.SUCCESS; }
        if (p.isShiftKeyDown()) { erase(medium); p.sendSystemMessage(Component.translatable(K + "erased")); return InteractionResult.SUCCESS; }
        String rec = recording(medium);
        p.sendSystemMessage(rec.isEmpty() ? Component.translatable(K + "blank") : Component.translatable(K + "play", rec));
        return InteractionResult.SUCCESS;
    }
}
