package com.taku3.semiconductorlab.energy;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** 「このブロックのこの面に電気の受け口はあるか」を調べる登録簿（Fabric 標準の仕組み） */
public final class EnergyApi {
    public static final BlockApiLookup<EnergyStorage, Direction> ENERGY =
            BlockApiLookup.get(Identifier.fromNamespaceAndPath("semiconductor-lab", "energy"), EnergyStorage.class, Direction.class);
    private EnergyApi() {}
}