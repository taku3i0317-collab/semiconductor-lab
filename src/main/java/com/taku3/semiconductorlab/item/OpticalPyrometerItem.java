package com.taku3.semiconductorlab.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Map;

/**
 * 光高温計（消失フィラメント型）[3-11]（1901 ホルボルンとクルルバウム。根拠は 1900 プランクの放射則。IMP4-01、S-009）：
 * レンズで熱い物の像を作り、その前に電池で光らせた W のフィラメントを重ねる。電流を変えてフィラメントが像に溶けて見えなくなったときの電流から温度を読む。
 * 熱電対 [3-09] が溶けてしまう 1600℃ 超の炉（アーク炉・アチソン炉・引き上げ炉など）用。光っていない物（約 {@link #MIN_C}℃ より下）は暗くて測れない。
 * ⚠️ 温度は機械ごとの代表値（optical_pyrometer.placeholder）。
 */
public class OpticalPyrometerItem extends Item {
    public static final int MIN_C = 700;
    private static final String K = "item.semiconductor-lab.optical_pyrometer.";
    /** 動いて光っている MOD の炉の代表の温度（℃、⚠️） */
    private static final Map<String, Integer> MOD_FURNACE_C = Map.of(
            "arc_furnace", 2000, "acheson_furnace", 2600, "cz_puller", 1420, "fz_puller", 1420, "crystal_puller", 1420,
            "electric_furnace", 1500, "blast_furnace_hearth", 1500, "bloomery", 1200, "reverberatory_furnace", 1000, "epitaxy_reactor", 1200);

    public OpticalPyrometerItem(Properties properties) { super(properties); }

    /** 光って見える物の温度（℃）。光っていなければ 0 */
    public static int glowC(BlockState s) {
        boolean lit = s.hasProperty(BlockStateProperties.LIT) && s.getValue(BlockStateProperties.LIT);
        var id = BuiltInRegistries.BLOCK.getKey(s.getBlock());
        if (lit && id.getNamespace().equals("semiconductor-lab") && MOD_FURNACE_C.containsKey(id.getPath())) return MOD_FURNACE_C.get(id.getPath());
        int t = ThermocoupleItem.temperatureC(s);
        return t >= MIN_C ? t : 0;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!ctx.getLevel().isClientSide() && ctx.getPlayer() != null) {
            int c = glowC(ctx.getLevel().getBlockState(ctx.getClickedPos()));
            ctx.getPlayer().sendOverlayMessage(c == 0 ? Component.translatable(K + "dark", MIN_C) : Component.translatable(K + "reading", c));
        }
        return InteractionResult.SUCCESS;
    }
}
