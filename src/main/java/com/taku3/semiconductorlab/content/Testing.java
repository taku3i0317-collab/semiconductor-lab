package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.energy.TestPowerSourceBlockEntity;
import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 試験用（S-001 試験場、IMP3）。どの幹のノードでもない、ゲーム内の確認のための物だけ。クリエイティブ専用・レシピなし。
 * 試験場は データパックの関数 semiconductor-lab:testworld（tools/gen_testworld.py が作る、docs/TESTWORLD.md）。
 */
public final class Testing {
    private Testing() {}

    /** 試験用の無限電源：燃料なしで 1 MW を隣へ（機械の真下に置く） */
    public static final Machine<TestPowerSourceBlockEntity> TEST_POWER_SOURCE = Power.generator("test_power_source", TestPowerSourceBlockEntity::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(-1.0f, 3600000.0f).sound(SoundType.METAL)).build();

    static void init() {}
}
