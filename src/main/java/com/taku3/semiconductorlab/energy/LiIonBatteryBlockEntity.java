package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * リチウムイオン電池 [4-15]（1985 吉野彰の原型、1991 ソニーが量産。IMP3 S-006、決定 Q3-13 ②）。
 * 充電でリチウムイオンが正極から出て、電解液を通って黒鉛の層のすき間に入る。放電でもとにもどる。
 * 正極はリン酸鉄リチウム LiFePO4（1997 グッドイナフの研究室 ⚠️ 1991 の量産品は LiCoO2。コバルトがまだ無いので、今の材料で作れる LFP にした）。
 * ゲーム：容量 4MJ（鉛蓄電池の4倍）、充電・放電とも 20kW、充電の効率 95%（鉛蓄電池は 85%）。⚠️ 数値はゲームの値。
 * 充放電・残量の保存などは親の {@link RechargeableBatteryBlockEntity}（A、S-011 の3）。
 */
public class LiIonBatteryBlockEntity extends RechargeableBatteryBlockEntity {
    public static final Spec SPEC = new Spec(4_000_000, 20_000, 95);

    public LiIonBatteryBlockEntity(BlockPos pos, BlockState state) { super(Power.LITHIUM_ION_BATTERY.type, pos, state, SPEC); }
}
