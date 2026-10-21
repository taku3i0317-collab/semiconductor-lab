package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Communication;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 活版印刷機 [15-02]（IMP2 S-010、1450年ごろ グーテンベルク）。ぶどう搾り機のような木のねじの押し機で、インクを塗った活字の版を紙に押しつける。
 * <ol>
 * <li>組版：書いた本を持ち、反対の手に活字を持って右クリック → 本の文を写した「組んだ版」（本は戻る、活字は減らない）</li>
 * <li>刷る：白い本を持ち、反対の手に組んだ版を持って右クリック → 持ち物の印刷インクを1つ使って、同じ文の本が1冊（「原本の写し」）</li>
 * </ol>
 * 手で写すと1冊に何か月もかかった本が、同じ文で何冊でも作れる。⚠️ 紙を1枚ずつ刷って製本する手間は「白い本1冊」にまとめた
 */
public class PrintingPressBlock extends Block {
    private static final String K = "message.semiconductor-lab.printing_press.";

    public PrintingPressBlock(Properties properties) { super(properties); }

    /** 組版：書いた本の文を写した版。書いた本でなければ空 */
    public static ItemStack compose(ItemStack book) {
        WrittenBookContent c = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (c == null) return ItemStack.EMPTY;
        ItemStack form = new ItemStack(Communication.TYPESET_FORM);
        form.set(DataComponents.WRITTEN_BOOK_CONTENT, c);
        return form;
    }

    /** 刷る：版の文の本を1冊。写しの写し（世代 2 以上）は版にできても刷れない（バニラの決まり）→ 空 */
    public static ItemStack print(ItemStack form) {
        WrittenBookContent c = form.get(DataComponents.WRITTEN_BOOK_CONTENT);
        WrittenBookContent copy = c == null ? null : c.craftCopy();
        if (copy == null) return ItemStack.EMPTY;
        ItemStack out = new ItemStack(Items.WRITTEN_BOOK);
        out.set(DataComponents.WRITTEN_BOOK_CONTENT, copy);
        return out;
    }

    /** 持ち物の印刷インクを1つ使う。無ければ false */
    public static boolean useInk(Player p) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Chemistry.PRINTING_INK)) { s.shrink(1); return true; }
        }
        return false;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        boolean composing = stack.is(Items.WRITTEN_BOOK) && other.is(Communication.MOVABLE_TYPE);
        boolean printing = stack.is(Items.BOOK) && other.is(Communication.TYPESET_FORM);
        if (!composing && !printing) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable(K + "hint"));
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (composing) {
            player.getInventory().placeItemBackInInventory(compose(stack), net.minecraft.util.Prediction.SERVER_ONLY);
            player.sendOverlayMessage(Component.translatable(K + "composed"));
            return InteractionResult.SUCCESS;
        }
        ItemStack out = print(other);
        if (out.isEmpty()) { player.sendOverlayMessage(Component.translatable(K + "too_far")); return InteractionResult.SUCCESS; }
        if (!useInk(player)) { player.sendOverlayMessage(Component.translatable(K + "need_ink")); return InteractionResult.SUCCESS; }
        stack.shrink(1);
        player.getInventory().placeItemBackInInventory(out, net.minecraft.util.Prediction.SERVER_ONLY);
        player.sendOverlayMessage(Component.translatable(K + "printed"));
        return InteractionResult.SUCCESS;
    }
}
