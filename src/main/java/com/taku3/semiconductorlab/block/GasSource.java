package com.taku3.semiconductorlab.block;

import net.minecraft.world.SimpleContainer;

/** 袋詰めガスを出口スロットに出す機械（電解槽・空気分離装置）。隣のガスホルダーがここから吸う */
public interface GasSource {
    /** ガスの袋が出てくるスロット番号 */
    int[] gasOutputSlots();
    SimpleContainer getContainer();
}
