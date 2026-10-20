package com.taku3.semiconductorlab.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * 丸木舟 [0-14]（1本の丸太をくりぬいた舟）。荷物入れ（27マス）つきの舟：バニラのチェスト付きボートと同じ動き。
 * 重い荷（{@link com.taku3.semiconductorlab.item.HeavyLoadItem}）を積んでも、水に浮かぶので速さは変わらない。
 * 陸で同じ荷を持って歩くと遅くなる＝「重い物は水で運ぶ」を比べる実験。バニラのボートは変えない。
 */
public class DugoutCanoe extends ChestBoat {
    public DugoutCanoe(EntityType<? extends ChestBoat> type, Level level, Supplier<Item> dropItem) {
        super(type, level, dropItem);
    }
}
