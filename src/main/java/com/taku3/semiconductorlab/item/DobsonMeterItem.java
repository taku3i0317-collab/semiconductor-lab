package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.OzoneLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * ドブソン分光光度計 [2-48]（1924 ドブソン）：太陽の紫外線の2つの波長の強さをくらべて、空の上のオゾンの量（ドブソン単位 DU）をはかる。
 * 右クリックで世界のオゾン量（OzoneLayer）を表示する。220 DU より下はオゾンホールの目安。
 */
public class DobsonMeterItem extends Item {
    public DobsonMeterItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level instanceof ServerLevel sl) {
            double du = OzoneLayer.get(sl).dobson(sl);
            p.sendOverlayMessage(Component.translatable(du < OzoneLayer.HOLE_DU ? "message.semiconductor-lab.dobson_meter.hole" : "message.semiconductor-lab.dobson_meter.reading",
                    String.format("%.0f", du)));
        }
        return InteractionResult.SUCCESS;
    }
}
