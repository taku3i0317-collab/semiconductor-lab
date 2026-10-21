package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.litho.Cpu4;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 4ビットのマイコン（[10-13]、S-014 Q25-5 案A：MCS-4 のように数枚のチップを組立台で1枚の板に）：
 * 4ビット CPU のお題 cpu4 に合格したチップ＋ROM のお題 rom_program に合格したチップ＋水晶の振動子（クロック）＋ニキシー管。
 * 右クリックで ROM のプログラム（3＋5）を動かし、出力の口に出た数をチャットに出す（計算は見本の {@link Cpu4}：組んだチップはお題で確かめ済み）
 */
public class MicrocomputerItem extends Item {
    public MicrocomputerItem(Properties p) { super(p); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            var out = Cpu4.run(Cpu4.ADD_3_5, Cpu4.CYCLES);
            p.sendSystemMessage(Component.translatable("message.semiconductor-lab.microcomputer.run", Cpu4.listing(Cpu4.ADD_3_5),
                    out.isEmpty() ? "-" : out.get(out.size() - 1).toString()));
        }
        return InteractionResult.SUCCESS;
    }
}
