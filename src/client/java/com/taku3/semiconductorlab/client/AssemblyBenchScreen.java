package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity;
import com.taku3.semiconductorlab.machine.Assembly;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.*;

/**
 * 組立台の画面（IMP-33）。左上＝◀ ▶ で作る物を選ぶ、真ん中＝役目つきの部品のスロット（空いていれば薄い見本、足りなければ赤い枠）、
 * 右＝完成品と「組み立てる」。下の文＝さわっている（か、足りない）部品の役目。文字の位置は AssemblyBenchBlockEntity.LAYOUT
 */
public class AssemblyBenchScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.assembly_bench.";
    private Button assemble;

    public AssemblyBenchScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), b -> press(BUTTON_PREV)).bounds(leftPos + PREV_X, topPos + BTN_Y, BTN_W, BTN_W).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> press(BUTTON_NEXT)).bounds(leftPos + NEXT_X, topPos + BTN_Y, BTN_W, BTN_W).build());
        assemble = addRenderableWidget(Button.builder(Component.translatable(K + "assemble"), b -> press(BUTTON_ASSEMBLE))
                .bounds(leftPos + ASSEMBLE_X, topPos + ASSEMBLE_Y, ASSEMBLE_W, ASSEMBLE_H).build());
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private Assembly recipe() { return Assembly.get(menu.get(DATA_RECIPE)); }

    /** スロット i の部品がそろっているか（画面の見た目だけ。本当の判定はサーバー） */
    private boolean ok(Assembly a, int i) { return a.satisfied(i, menu.machineItem(i)); }

    private boolean allOk(Assembly a) {
        for (int i = 0; i < a.parts.size(); i++) if (!ok(a, i)) return false;
        return true;
    }

    @Override protected void containerTick() {
        super.containerTick();
        Assembly a = recipe();
        if (assemble != null) assemble.active = a != null && menu.get(DATA_PROGRESS) == 0 && allOk(a);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        Assembly a = recipe();
        if (a == null) return;
        for (int i = 0; i < a.parts.size(); i++) {
            int x = leftPos + PART_X + 18 * (i % 4), y = topPos + PART_Y + 18 * (i / 4);
            ItemStack in = menu.machineItem(i);
            if (in.isEmpty()) {   // 何を入れるかの薄い見本（数つき）
                ItemStack ghost = new ItemStack(a.parts.get(i).display(), a.parts.get(i).count());
                g.fakeItem(ghost, x, y);
                g.fill(x, y, x + 16, y + 16, 0xA08B8B8B);
            }
            if (!ok(a, i)) g.outline(x - 1, y - 1, 18, 18, 0xFFC03030);   // 足りない
        }
    }

    @Override
    protected Component text(String id) {
        Assembly a = recipe();
        if (a == null) return null;
        return switch (id) {
            case "index" -> Component.literal((menu.get(DATA_RECIPE) + 1) + "/" + Assembly.all().size());
            case "product" -> Component.translatable(a.resultKey());
            case "role" -> {
                int slot = hoveredSlot != null && hoveredSlot.getContainerSlot() < a.parts.size() && hoveredSlot.index < PARTS ? hoveredSlot.getContainerSlot() : -1;
                if (slot < 0) for (int i = 0; i < a.parts.size(); i++) if (!ok(a, i)) { slot = i; break; }
                yield slot < 0 ? null : Component.translatable(a.roleKey(a.parts.get(slot)));
            }
            case "hint" -> {
                String h;
                if (menu.get(DATA_PROGRESS) > 0) h = "assembling";
                else if (!allOk(a)) h = "need_parts";
                else if (!menu.machineItem(SLOT_OUT).isEmpty() && menu.machineItem(SLOT_OUT).getItem() != a.result) h = "out_full";
                else h = "ready";
                yield h.equals("assembling") ? Component.translatable(K + h, (menu.get(DATA_PROGRESS) * 100 / Math.max(1, menu.get(DATA_TOTAL))) + "%") : Component.translatable(K + h);
            }
            default -> null;
        };
    }
}
