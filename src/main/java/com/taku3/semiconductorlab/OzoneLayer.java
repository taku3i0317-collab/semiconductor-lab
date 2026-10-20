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
 * ⚠️ プレイヤーへの影響（紫外線の害）はまだ付けない（IMPL3_LOG に案、ユーザーの決定待ち）。
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

    /** テスト用：はじめの量にもどす */
    public void resetForTest(ServerLevel level) { dobson = START_DU; last = level.getGameTime(); setDirty(); }
}
