package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.SaltPanBlockEntity;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 分光器 [7-06]（IMP2-01）：プリズム＋コリメーター（細い隙間とレンズ）＋のぞく望遠鏡（キルヒホッフとブンゼン 1859）。
 * <ul>
 *   <li>火のついたたき火・火に向けて右クリック：もう片方の手に持った試料が炎の中で出す元素の線（炎色反応）をチャットに出す</li>
 *   <li>昼に空の下で空中を右クリック：太陽の連続スペクトルと暗線（フラウンホーファー線）</li>
 * </ul>
 * 線の波長は NIST の原子スペクトルの値（可視で強い線だけ）。どの試料にどの元素が入っているかは {@link #elementsOf} に1行ずつ。
 * [7-07] 回折格子の分光器（IMP2-01）は同じクラスで gratingMicrons &gt; 0：線ごとに 1次の回折角 θ と λ = d·sinθ も出す（波長を測れる）。
 */
public class SpectroscopeItem extends Item {
    private static final String K = "item.semiconductor-lab.spectroscope.";

    /** 回折格子の溝（針金）の間隔 d [μm]。0 ならプリズム（角度・波長は出さない） */
    private final double gratingMicrons;

    public SpectroscopeItem(Properties properties) { this(properties, 0); }
    public SpectroscopeItem(Properties properties, double gratingMicrons) { super(properties); this.gratingMicrons = gratingMicrons; }

    public double gratingMicrons() { return gratingMicrons; }

    /** 1次の回折角 [度]：sinθ = λ / d */
    public static double angleDeg(double nm, double dMicron) { return Math.toDegrees(Math.asin(nm * 1e-3 / dMicron)); }

    /** 角度を測れる（目に見える 380〜780nm の、原子の細い）線の波長 [nm]。帯（CaOH・BO2）や紫外の線は入れない */
    public static double[] measurableNm(String key) {
        return switch (key) {
            case "na" -> new double[] {589.0, 589.6};
            case "k" -> new double[] {766.5, 769.9, 404.4};
            case "ca" -> new double[] {422.7};
            case "pb" -> new double[] {405.8};
            case "sun" -> new double[] {656.3, 589.0, 518.4, 486.1, 396.8, 393.4};   // C・D・b1・F・H・K
            default -> new double[0];
        };
    }

    /** 格子の分光器なら、key の線ごとに「回折角 → 波長」を1行ずつ送る */
    private void sendMeasure(Player p, String key) {
        if (gratingMicrons <= 0) return;
        for (double nm : measurableNm(key)) {
            double deg = angleDeg(nm, gratingMicrons);
            double measured = gratingMicrons * 1e3 * Math.sin(Math.toRadians(deg));
            p.sendSystemMessage(Component.translatable("item.semiconductor-lab.grating_spectroscope.measure",
                    String.format(java.util.Locale.ROOT, "%.4f", deg), String.format(java.util.Locale.ROOT, "%.1f", deg * 60),
                    String.format(java.util.Locale.ROOT, "%.1f", measured), String.format(java.util.Locale.ROOT, "%.1f", gratingMicrons)));
        }
    }

    /** 炎として扱うブロック（火のついたたき火・火・魂の火） */
    public static boolean isFlame(BlockState s) {
        return CampfireBlock.isLitCampfire(s) || s.getBlock() instanceof BaseFireBlock;
    }

    /** 試料を炎に入れたときに線が出る元素（lang の spectroscope.line.&lt;記号&gt;）。光らない物は空 */
    public static List<String> elementsOf(ItemStack s) {
        List<String> e = new ArrayList<>();
        if (s.is(Gas.SALT) || s.is(Gas.ROCK_SALT) || s.is(Chemistry.SODIUM_CARBONATE) || s.is(Chemistry.TRONA) || s.is(Gas.SODIUM_HYDROXIDE)) e.add("na");
        else if (s.is(Chemistry.POTASH) || s.is(Chemistry.LYE)) e.add("k");
        else if (s.is(Chemistry.WOOD_ASH)) { e.add("k"); e.add("ca"); e.add("na"); }   // 木灰には Na も少し入り、黄色が K の紫を隠す
        else if (s.is(Survival.QUICKLIME) || s.is(Survival.SLAKED_LIME)) e.add("ca");
        else if (s.is(Metallurgy.BORAX)) { e.add("b"); e.add("na"); }                    // ホウ砂 Na2B4O7
        else if (s.is(Metallurgy.BORIC_ACID) || s.is(Metallurgy.BORON_TRIOXIDE)) e.add("b");
        else if (s.is(Metallurgy.LITHARGE)) e.add("pb");
        else if (s.is(Metallurgy.GERMANIUM_DIOXIDE) || s.is(Metallurgy.HIGH_PURITY_GERMANIUM_DIOXIDE)
                || s.is(Metallurgy.GERMANIUM_TETRACHLORIDE) || s.is(Metallurgy.CRUDE_GERMANIUM_TETRACHLORIDE)) e.add("ge");
        return e;
    }

    /** 炎を見たときのチャットの行（lang キーの末尾）。試料なし＝炎だけの連続スペクトル */
    public static List<String> flameReport(ItemStack sample) {
        List<String> out = new ArrayList<>();
        if (sample.isEmpty()) { out.add("flame_only"); out.add("hold_sample"); return out; }
        List<String> e = elementsOf(sample);
        if (e.isEmpty()) out.add("none");
        else for (String x : e) out.add("line." + x);
        return out;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!isFlame(level.getBlockState(ctx.getClickedPos()))) return InteractionResult.PASS;   // 炎でなければ use()（空を見る）へ
        Player p = ctx.getPlayer();
        if (!level.isClientSide() && p != null) {
            InteractionHand other = ctx.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack sample = p.getItemInHand(other);
            p.sendSystemMessage(sample.isEmpty() ? Component.translatable(K + "flame")
                    : Component.translatable(K + "flame_with", sample.getHoverName()));
            for (String key : flameReport(sample)) p.sendSystemMessage(Component.translatable(K + key));
            for (String e : elementsOf(sample)) sendMeasure(p, e);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            boolean sun = level.dimension() == Level.OVERWORLD && level.isBrightOutside()
                    && SaltPanBlockEntity.openSky(level, p.blockPosition().above());
            p.sendSystemMessage(Component.translatable(K + (sun ? "sun" : "no_sun")));
            if (sun) { p.sendSystemMessage(Component.translatable(K + "sun_lines")); sendMeasure(p, "sun"); }
        }
        return InteractionResult.SUCCESS;
    }
}
