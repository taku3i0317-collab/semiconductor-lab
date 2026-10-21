package com.taku3.semiconductorlab;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * 世界のオゾン層（[2-48] フロン・[I-17] オゾン層の破壊＝失敗の記録、IMP3 O-019）。世界に1つの数値（ドブソン単位 DU）を保存する。
 * はじめは {@link #START_DU}。フロンが空にもれる（{@link #leak}）と減り、もれなければ1日（24000 tick）に {@link #RECOVERY_DU_PER_DAY} ずつ戻る（モントリオール議定書のあとの回復）。
 * {@link #HOLE_DU} より下が「オゾンホール」の目安。ドブソン計（DobsonMeterItem）で読む。
 * オゾンホール（{@link #HOLE_DU} 未満）のあいだ、日なた（昼・真上が空・雨でない）のプレイヤーに、{@link #CHECK_TICKS} ごとに {@link #SUNBURN_CHANCE} の確率で
 * 短い「日焼け」（弱体化 {@link #SUNBURN_TICKS}）。地形は壊さない。フロンをやめればオゾンは時間で戻り、日焼けもなくなる（決定 Q3-11 ②、DESIGN 6）。
 * 保存：SavedDataType。DataFixTypes は null にできない（読みこみで update が呼ばれる）ので、中身を書きかえない SAVED_DATA_COMMAND_STORAGE を使う（API_26X）。
 */
public class OzoneLayer extends SavedData {
    public static final double START_DU = 300, HOLE_DU = 220, MIN_DU = 100, RECOVERY_DU_PER_DAY = 1.0;
    public static final double LEAK_DU = 3.0;

    public static final Codec<OzoneLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("dobson").forGetter(o -> o.dobson),
            Codec.LONG.fieldOf("last").forGetter(o -> o.last)).apply(i, OzoneLayer::new));

    public static final SavedDataType<OzoneLayer> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("semiconductor-lab", "ozone_layer"), OzoneLayer::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private double dobson;
    private long last;

    public OzoneLayer() { this(START_DU, -1); }
    public OzoneLayer(double dobson, long last) { this.dobson = dobson; this.last = last; }

    /** 世界のオゾン層（オーバーワールドに保存） */
    public static OzoneLayer get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** 今のオゾン量（DU）。前に見たときからの回復を足す */
    public double dobson(ServerLevel level) {
        long now = level.getGameTime();
        if (last >= 0 && now > last) dobson = Math.min(START_DU, dobson + (now - last) * RECOVERY_DU_PER_DAY / 24000.0);
        last = now;
        setDirty();
        return dobson;
    }

    /** フロンが空にもれた：オゾンが du 減る */
    public void leak(ServerLevel level, double du) {
        dobson(level);
        dobson = Math.max(MIN_DU, dobson - du);
        setDirty();
    }

    public static final int CHECK_TICKS = 200, SUNBURN_TICKS = 20 * 10;
    public static final float SUNBURN_CHANCE = 1f / 3f;

    /** 日焼けするか：オゾンホールで、日なたにいるとき */
    public static boolean sunburnRisk(double du, boolean sunlit) { return du < HOLE_DU && sunlit; }

    /** 日焼けさせる（テストからも呼ぶ）：短い弱体化とメッセージ */
    public static void sunburn(net.minecraft.world.entity.player.Player p) {
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, SUNBURN_TICKS, 0, false, false, true));
        p.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.semiconductor-lab.ozone.sunburn"));
    }

    /** サーバーの tick から呼ぶ：オーバーワールドの日なたのプレイヤーを調べる */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        ServerLevel lv = server.overworld();
        if (lv.getGameTime() % CHECK_TICKS != 0 || !lv.isBrightOutside()) return;
        double du = get(lv).dobson(lv);
        if (du >= HOLE_DU) return;
        for (var p : lv.players()) {
            net.minecraft.core.BlockPos head = p.blockPosition().above();
            boolean sunlit = !lv.isRainingAt(head) && com.taku3.semiconductorlab.block.HeliographPlateBlock.openToSky(lv, head);
            if (sunburnRisk(du, sunlit) && lv.getRandom().nextFloat() < SUNBURN_CHANCE) sunburn(p);
        }
    }

    /** テスト用：はじめの量にもどす */
    public void resetForTest(ServerLevel level) { dobson = START_DU; last = level.getGameTime(); setDirty(); }
    /** テスト用：値を決める */
    public void setForTest(ServerLevel level, double du) { dobson = du; last = level.getGameTime(); setDirty(); }
}
