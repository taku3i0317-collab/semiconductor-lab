package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 帯域精製炉の画面。左＝Ge、中央＝黒鉛ボート、右＝ドーパント。上＝電力、バーは溶融帯の進み具合 */
public class ZoneRefiningFurnaceScreen extends MachineScreen {
    private static final int BAR_X = 60, BAR_Y = 58, BAR_W = 56, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.refiner.";

    public ZoneRefiningFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        int color = menu.get(ZoneRefiningFurnaceBlockEntity.DATA_MODE) == ZoneRefiningFurnaceBlockEntity.MODE_DOPE ? 0xFF4FA3FF : 0xFFFF8C1E; // 青＝ドーピング、橙＝精製
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, color);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は ZoneRefiningFurnaceBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()), String.format("%.0f kW", ZoneRefiningFurnaceBlockEntity.WATTS / 1000));
            case "line" -> {
                int n = menu.get(ZoneRefiningFurnaceBlockEntity.DATA_NINES), cap = menu.get(ZoneRefiningFurnaceBlockEntity.DATA_BOAT_CAP);
                boolean working = menu.get(ZoneRefiningFurnaceBlockEntity.DATA_MODE) != ZoneRefiningFurnaceBlockEntity.MODE_IDLE;
                if (menu.getSlot(ZoneRefiningFurnaceBlockEntity.GE_SLOT).getItem().isEmpty()) yield Component.translatable(K + "hint");
                if (cap == 0) yield Component.translatable(K + "no_boat");
                if (working && menu.getKw() <= 0) yield Component.translatable(K + "need_power");
                if (menu.get(ZoneRefiningFurnaceBlockEntity.DATA_MODE) == ZoneRefiningFurnaceBlockEntity.MODE_DOPE) yield Component.translatable(K + "doping", n + "N");
                if (n >= 9 && !menu.getSlot(ZoneRefiningFurnaceBlockEntity.DOPANT_SLOT).getItem().isEmpty()) yield Component.translatable(K + "purity", n + "N", (n >= 10 ? "-" : "10N"));
                if (n >= 9 && n < 10 && cap < 10) yield Component.translatable(K + "clay_cap");
                if (n >= 9 && n < 10 && menu.get(ZoneRefiningFurnaceBlockEntity.DATA_ROUTE_CAP) < 10) yield Component.translatable(K + "route_cap");
                if (n >= 9 && n < 10) yield Component.translatable(K + "ready", n + "N");
                if (n >= 10) yield Component.translatable(K + "max", n + "N");
                // 決定（2026-10）：6N 以下＋ドーパントは「先に精製 → 9N になったら自動でドープ」。待っている理由を見せる
                if (!menu.getSlot(ZoneRefiningFurnaceBlockEntity.DOPANT_SLOT).getItem().isEmpty()) yield Component.translatable(K + "dopant_wait");
                String cur = n <= 0 ? Component.translatable(K + "unrefined").getString() : n + "N";
                yield Component.translatable(K + "purity", cur, (n <= 0 ? 6 : 9) + "N");
            }
            default -> null;
        };
    }
}
