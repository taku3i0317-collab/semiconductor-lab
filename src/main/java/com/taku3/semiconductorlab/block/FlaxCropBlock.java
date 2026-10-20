package com.taku3.semiconductorlab.block;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/**
 * 亜麻 [0-06]（Linum usitatissimum。茎の外側の皮の内側に長い繊維＝靱皮繊維がある）。
 * ふつうの作物と同じように育つ。種＝このブロックのアイテム（Reg.block が作る）。熟すと茎と種（亜麻仁）。
 */
public class FlaxCropBlock extends CropBlock {
    public FlaxCropBlock(Properties properties) { super(properties); }

    @Override
    protected ItemLike getBaseSeedId() { return this.asItem(); }
}
