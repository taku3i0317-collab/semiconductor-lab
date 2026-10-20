package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 反射炉の画面。左上＝鉱石、左下＝燃料、間の縦バー＝残りの火、中央バー＝焙焼の進み、右 2x2＝主生成物・SO2・副産物 */
public class ReverberatoryFurnaceScreen extends MachineScreen {
    private static final int FLAME_X = 47, FLAME_Y = 36, FLAME_W = 10, FLAME_H = 14;
    private static final int BAR_X = 66, BAR_Y = 38, BAR_W = 32, BAR_H = 6;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.reverberatory_furnace.";

    public ReverberatoryFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        // 残りの火（下から減る）
        int fx = leftPos + FLAME_X, fy = topPos + FLAME_Y;
        g.fill(fx, fy, fx + FLAME_W, fy + FLAME_H, 0xFF3A3A40);
        int fh = menu.get(ReverberatoryFurnaceBlockEntity.DATA_BURN) * FLAME_H / Math.max(1, menu.get(ReverberatoryFurnaceBlockEntity.DATA_BURN_TOTAL));
        if (fh > 0) g.fill(fx, fy + FLAME_H - fh, fx + FLAME_W, fy + FLAME_H, 0xFFFF7A1E);
        // 焙焼の進み
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.get(ReverberatoryFurnaceBlockEntity.DATA_PROGRESS) * BAR_W / Math.max(1, menu.get(ReverberatoryFurnaceBlockEntity.DATA_TOTAL));
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFE8D24A);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は ReverberatoryFurnaceBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        if (id.equals("so2")) return Component.translatable("gui.semiconductor-lab.reverberatory_furnace.flue");   // 煙道のガス（SO2 か CO2）
        if (!id.equals("hint")) return null;
        ItemStack in = menu.getSlot(ReverberatoryFurnaceBlockEntity.SLOT_IN).getItem();
        ItemStack fuel = menu.getSlot(ReverberatoryFurnaceBlockEntity.SLOT_FUEL).getItem();
        String hint;
        if (in.isEmpty()) hint = "need_input";
        else if (menu.get(ReverberatoryFurnaceBlockEntity.DATA_BURN) <= 0 && fuel.isEmpty()) hint = "need_fuel";
        else if (menu.get(ReverberatoryFurnaceBlockEntity.DATA_PROGRESS) > 0) hint = "roasting";
        else hint = "out_full";   // 鉱石も燃料もあるのに進まない＝出口のどれかが満杯
        return Component.translatable(K + hint);
    }
}
