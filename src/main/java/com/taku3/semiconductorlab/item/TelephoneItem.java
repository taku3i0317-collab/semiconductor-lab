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
 * 電話 [15-04]（IMP2 S-010、1876 ベル、1877〜78 炭素マイク）。炭の粒のマイクで声を電流の強弱にし、電磁石の受話器で振動板をふるわせて声に戻す。
 * 2台の電話を両手に持って右クリックすると、同じ回線（Line）に結ばれる。電話を手に持ってチャットで話すと、同じ回線の電話を持っている人に届く。
 * ⚠️ 2台をつなぐ電線は省略（どこにいても届く）。
 */
public class TelephoneItem extends Item {
    private static final String K = "item.semiconductor-lab.telephone.";

    public TelephoneItem(Properties properties) { super(properties); }

    public static long line(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : d.copyTag().getLongOr("Line", 0);
    }

    public static void setLine(ItemStack s, long line) {
        CompoundTag t = new CompoundTag();
        t.putLong("Line", line);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    public static boolean sameLine(ItemStack a, ItemStack b) { return a.getItem() instanceof TelephoneItem && b.getItem() instanceof TelephoneItem && line(a) != 0 && line(a) == line(b); }

    /** 手に持っている電話の回線（なければ 0） */
    public static long lineOf(Player p) {
        for (InteractionHand h : InteractionHand.values()) {
            ItemStack s = p.getItemInHand(h);
            if (s.getItem() instanceof TelephoneItem && line(s) != 0) return line(s);
        }
        return 0;
    }

    /** チャットを、同じ回線の電話を持っているほかの人に届ける */
    public static void relay(String text, ServerPlayer sender) {
        long l = lineOf(sender);
        if (l == 0 || sender.level().getServer() == null) return;
        for (ServerPlayer p : sender.level().getServer().getPlayerList().getPlayers())
            if (p != sender && lineOf(p) == l) p.sendSystemMessage(Component.translatable(K + "voice", sender.getName(), text));
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack a = p.getItemInHand(hand), b = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (b.getItem() instanceof TelephoneItem) {
            long l = level.getRandom().nextLong() | 1;
            setLine(a, l); setLine(b, l);
            p.sendSystemMessage(Component.translatable(K + "linked"));
        } else p.sendSystemMessage(Component.translatable(line(a) == 0 ? K + "unlinked" : K + "hint"));
        return InteractionResult.SUCCESS;
    }
}
