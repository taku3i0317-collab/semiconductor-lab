package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.content.Survival;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;

/** 幹0 生存のクライアント側（IMP3-01）：丸木舟 [0-14] の描画。テクスチャは textures/entity/chest_boat/dugout_canoe.png */
public final class SurvivalClient {
    public static final ModelLayerLocation DUGOUT_CANOE_LAYER = new ModelLayerLocation(SemiconductorLab.id("chest_boat/dugout_canoe"), "main");

    private SurvivalClient() {}

    public static void register() {
        ModelLayerRegistry.registerModelLayer(DUGOUT_CANOE_LAYER, BoatModel::createChestBoatModel);
        EntityRendererRegistry.register(Survival.DUGOUT_CANOE_ENTITY, ctx -> new BoatRenderer(ctx, DUGOUT_CANOE_LAYER));
    }
}
