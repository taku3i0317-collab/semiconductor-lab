package com.taku3.semiconductorlab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.taku3.semiconductorlab.block.ConveyorBeltBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/** ベルトコンベアのベルト [14-22] の上に、乗っている物を寝かせて描く（IMP4-01。たき火の CampfireRenderer と同じ形：26.3 は描く前に状態を写す） */
public class ConveyorBeltRenderer implements BlockEntityRenderer<ConveyorBeltBlockEntity, ConveyorBeltRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
    }

    private final ItemModelResolver items;

    public ConveyorBeltRenderer(BlockEntityRendererProvider.Context ctx) { items = ctx.itemModelResolver(); }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(ConveyorBeltBlockEntity be, State s, float partial, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partial, camera, crumbling);
        items.updateForTopItem(s.item, be.getItem(), ItemDisplayContext.FIXED, be.getLevel(), null, (int) be.getBlockPos().asLong());
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (s.item.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5f, 0.27f, 0.5f);
        pose.rotateDegrees(Axis.XP, 90f);
        pose.scale(0.5f, 0.5f, 0.5f);
        s.item.submit(pose, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
