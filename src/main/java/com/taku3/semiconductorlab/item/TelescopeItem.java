package com.taku3.semiconductorlab.item;

import net.minecraft.world.item.SpyglassItem;

/**
 * 屈折望遠鏡 [7-04]（IMP2-01）。使い方はバニラの望遠鏡と同じ（右クリックを押している間のぞく）。
 * バニラは Player.isScoping() が Items.SPYGLASS しか見ないので、mixin/PlayerScopingMixin で「このクラスの道具ものぞける」ようにしている
 * （のぞき穴の絵・視野の拡大・マウスの感度は全部 isScoping() を通る。26.3 の Hud・AbstractClientPlayer・MouseHandler を javap で確認）。
 */
public class TelescopeItem extends SpyglassItem {
    public TelescopeItem(Properties properties) { super(properties); }
}
