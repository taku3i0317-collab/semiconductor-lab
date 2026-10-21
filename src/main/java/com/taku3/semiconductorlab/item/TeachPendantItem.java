package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.RobotArmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * [10-26] ティーチングペンダント（O-037）：ロボットの腕に動きを教える道具。
 * 腕を右クリック → 教え始める（しゃがむと忘れる）。次に右クリックした機械が「取る所」、その次が「置く所」（教えている途中の、届く範囲の腕に送る）
 */
public class TeachPendantItem extends Item {
    public TeachPendantItem(Properties p) { super(p); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos at = ctx.getClickedPos();
        Player p = ctx.getPlayer();
        if (level.getBlockEntity(at) instanceof RobotArmBlockEntity arm) {
            if (!level.isClientSide()) arm.startTeaching(p, p != null && p.isShiftKeyDown());
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        int r = RobotArmBlockEntity.REACH;
        for (BlockPos q : BlockPos.betweenClosed(at.offset(-r, -r, -r), at.offset(r, r, r)))
            if (level.getBlockEntity(q) instanceof RobotArmBlockEntity arm && arm.teachStep() > 0 && arm.teach(p, at)) return InteractionResult.SUCCESS;
        return InteractionResult.PASS;
    }
}
