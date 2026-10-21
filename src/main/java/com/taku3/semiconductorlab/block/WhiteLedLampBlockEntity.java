package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Branches;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 白色 LED の明かり [B-20]（1996 日本の会社が青色 LED＋黄色の蛍光体 YAG:Ce で白色 LED を製品に。IMP4-01、S-007 案A）：
 * 青色 LED の光の一部を蛍光体が黄色に変え、青と黄色が混ざって白く見える。電気 {@link LedLampBlockEntity#WATTS} W で明るさ15（⚠️ ゲームの値）。
 */
public class WhiteLedLampBlockEntity extends LedLampBlockEntity {
    public WhiteLedLampBlockEntity(BlockPos pos, BlockState state) { super(Branches.WHITE_LED_LAMP.type, pos, state); }
}
