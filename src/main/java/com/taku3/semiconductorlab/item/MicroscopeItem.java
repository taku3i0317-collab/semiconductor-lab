package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.litho.ExposureTool;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * 光学顕微鏡 [8-10]（IMP2-04）。もう片方の手に持ったマスク・原図・設計図・加工中のウェハーの「1マスの細さ」と、
 * この顕微鏡で見分けられる一番細い線（アッベ 1873：d ＝ λ ÷ 2NA）をくらべる。1マスが d より太ければ見える、細ければぼやける。
 * 光は白い光のまん中 {@link #LIGHT_NM}。1マスの細さは LITHO_DESIGN §3 の表（ExposureTool）から。A のデータ（size）を読むだけ。
 */
public class MicroscopeItem extends Item {
    /** 白い光（目がいちばん感じる緑のあたり） */
    public static final double LIGHT_NM = 550;
    private static final String K = "item.semiconductor-lab.microscope.";

    /** 対物レンズの開口数 NA（レンズが光を受ける角度の広さ。空気中では 1 より小さい） */
    public final double na;

    public MicroscopeItem(Properties properties, double na) { super(properties); this.na = na; }

    /** 見分けられる一番細い線（nm）：アッベの d ＝ λ ÷ 2NA */
    public static double resolutionNm(double na) { return LIGHT_NM / (2 * na); }

    /** 方眼の1辺のマス数から、1マスの細さ（nm）。表にあればその値、なければ N＝8×(10µm÷CD)^(1/3) を逆に解く */
    public static double cellNm(int size) {
        for (ExposureTool t : ExposureTool.values()) if (t.grid == size) return t.cdNm;
        return 10_000.0 * Math.pow(8.0 / size, 3);
    }

    /** 見る物の方眼の大きさ（マスク・設計図・加工中のウェハー）。見る物でなければ 0 */
    public static int sizeOf(ItemStack s) {
        if (s.has(ModDataComponents.MASK)) return s.get(ModDataComponents.MASK).size();
        if (s.has(ModDataComponents.CHIP_DESIGN)) return s.get(ModDataComponents.CHIP_DESIGN).size();
        if (s.has(ModDataComponents.CHIP_STATE)) return s.get(ModDataComponents.CHIP_STATE).size();
        return 0;
    }

    /** この顕微鏡でその方眼の線が見えるか */
    public boolean canResolve(int size) { return size > 0 && cellNm(size) >= resolutionNm(na); }

    private static String um(double nm) {
        return nm >= 1000 ? String.format(Locale.ROOT, "%.1f µm", nm / 1000) : String.format(Locale.ROOT, "%.0f nm", nm);
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack target = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        int size = sizeOf(target);
        p.sendSystemMessage(Component.translatable(K + "limit", String.format(Locale.ROOT, "%.2f", na), um(resolutionNm(na))));
        if (size <= 0) {
            p.sendSystemMessage(Component.translatable(K + (target.isEmpty() ? "hold" : "nothing")));
            return InteractionResult.SUCCESS;
        }
        p.sendSystemMessage(Component.translatable(K + "cell", target.getHoverName(), size, size, um(cellNm(size))));
        p.sendSystemMessage(Component.translatable(K + (canResolve(size) ? "sharp" : "blurred")));
        if (target.isDamageableItem() && target.getDamageValue() > 0)
            p.sendSystemMessage(Component.translatable(K + "scratches", target.getDamageValue(), target.getMaxDamage()));
        return InteractionResult.SUCCESS;
    }
}
