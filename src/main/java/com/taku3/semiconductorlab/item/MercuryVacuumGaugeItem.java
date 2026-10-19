package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.VacuumChamberBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * 水銀真空計 [5-10]（片側を閉じた U字管の水銀マノメーター。原理はトリチェリ 1643 の水銀柱）。
 * 真空容器に当てて右クリックすると、閉じた側（ほぼ真空）と容器側の水銀面の高さの差＝容器の圧力（mmHg）を読む。
 * 差が 1mm 未満（約133Pa 未満）は目で読めない（マクラウド真空計 1874・ピラニ真空計 1906 の出番）。
 */
public class MercuryVacuumGaugeItem extends Item {
    /** 目で読める最小の差（mm） */
    public static final double MIN_READABLE_MM = 1.0;

    public MercuryVacuumGaugeItem(Properties props) { super(props); }

    /** Pa → 水銀柱の高さの差（mm）。1mmHg ＝ 133.322 Pa */
    public static double mmHg(double pa) { return pa / VacuumChamberBlockEntity.PA_PER_MMHG; }

    /** 読み取り結果の文（テストからも呼ぶ） */
    public static Component reading(double pa) {
        double mm = mmHg(pa);
        if (mm < MIN_READABLE_MM) return Component.translatable("message.semiconductor-lab.mercury_vacuum_gauge.below", VacuumChamberBlockEntity.formatPa(VacuumChamberBlockEntity.PA_PER_MMHG));
        return Component.translatable("message.semiconductor-lab.mercury_vacuum_gauge.reading", String.format("%.0f", mm), VacuumChamberBlockEntity.formatPa(pa));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof VacuumChamberBlockEntity c)) return InteractionResult.PASS;
        Player player = ctx.getPlayer();
        if (!ctx.getLevel().isClientSide() && player != null) player.sendSystemMessage(reading(c.getPressure()));
        return InteractionResult.SUCCESS;
    }
}
