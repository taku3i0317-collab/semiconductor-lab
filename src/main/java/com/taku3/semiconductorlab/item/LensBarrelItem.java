package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.Vibration;
import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 鏡筒 [8-14] 精密組立（心出し、IMP2 O-010）。中のレンズ {@value #LENSES} 枚それぞれの「心のずれ」（光軸からのずれ、0.1µm 単位）を成分 LENS_DECENTER で持つ。
 * 利き手に鏡筒、もう片方の手に顕微鏡 [8-10] を持って右クリック → 鏡筒を回して像の揺れを見て、いちばんずれたレンズを軽くたたいて寄せる（ずれが 4割に）。
 * 見分けられるのは「顕微鏡の分解能」と「床の揺れ」[8-07] より大きいずれだけ。全部のレンズが {@value #CENTERED_TENTHS}/10 µm 以下になると心出し済みの鏡筒になる。
 * 単レンズ顕微鏡（分解能 約1.4µm）では 1µm まで見えず、仕上がらない。
 */
public class LensBarrelItem extends Item {
    public static final int LENSES = 4;
    /** 組んだままのずれ（0.1µm 単位）＝ 20〜80µm */
    public static final int RAW_MIN = 200, RAW_MAX = 800;
    /** 心出し済み＝全部 1µm 以下 */
    public static final int CENTERED_TENTHS = 10;
    /** 1回たたくと残るずれの割合 */
    public static final double TAP_KEEP = 0.4;
    private static final String K = "item.semiconductor-lab.lens_barrel.";

    public LensBarrelItem(Properties properties) { super(properties); }

    public static List<Integer> decenters(ItemStack s, net.minecraft.util.RandomSource rnd) {
        List<Integer> d = s.get(Precision.LENS_DECENTER);
        if (d != null) return new ArrayList<>(d);
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i < LENSES; i++) r.add(rnd.nextIntBetweenInclusive(RAW_MIN, RAW_MAX));
        return r;
    }

    /** 見分けられる一番小さいずれ（0.1µm 単位）：顕微鏡の分解能と床の揺れの大きいほう */
    public static int seeLimit(double na, double vibrationNm) {
        return (int) Math.ceil(Math.max(MicroscopeItem.resolutionNm(na), vibrationNm) / 100.0);
    }

    /** 1回の心出し：いちばんずれたレンズを、見分けられる所まで寄せる。たたいたレンズの番号（0〜）、見えなければ -1 */
    public static int tap(List<Integer> d, int limit) {
        int i = 0;
        for (int k = 1; k < d.size(); k++) if (d.get(k) > d.get(i)) i = k;
        if (d.get(i) <= limit) return -1;
        d.set(i, Math.max(limit, (int) Math.round(d.get(i) * TAP_KEEP)));
        return i;
    }

    public static boolean centered(List<Integer> d) { return d.stream().allMatch(v -> v <= CENTERED_TENTHS); }

    private static String um(int tenths) { return String.format("%.1f", tenths / 10.0); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack stack = p.getItemInHand(hand);
        if (!(p.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof MicroscopeItem m)) {
            if (!level.isClientSide()) p.sendSystemMessage(Component.translatable(K + "hold"));
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        List<Integer> d = decenters(stack, level.getRandom());
        double vib = Vibration.nm(level, p.blockPosition(), p);
        int limit = seeLimit(m.na, vib);
        int worst = d.stream().max(Integer::compare).orElse(0);
        p.sendSystemMessage(Component.translatable(K + "wobble", um(2 * worst)));
        int i = tap(d, limit);
        if (i < 0) p.sendSystemMessage(Component.translatable(K + "cannot_see", um(limit),
                String.format("%.2f", MicroscopeItem.resolutionNm(m.na) / 1000), String.format("%.2f", vib / 1000)));
        else p.sendSystemMessage(Component.translatable(K + "tapped", i + 1, um(d.get(i))));
        ItemStack out = new ItemStack(centered(d) ? Precision.CENTERED_LENS_BARREL : Precision.LENS_BARREL);
        out.set(Precision.LENS_DECENTER, List.copyOf(d));
        p.setItemInHand(hand, out);
        if (centered(d)) p.sendSystemMessage(Component.translatable(K + "done"));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        List<Integer> d = stack.get(Precision.LENS_DECENTER);
        if (d == null) { out.accept(Component.translatable(K + "unknown")); return; }
        out.accept(Component.translatable(K + "decenter", String.join(" / ", d.stream().map(LensBarrelItem::um).toList())));
    }
}
