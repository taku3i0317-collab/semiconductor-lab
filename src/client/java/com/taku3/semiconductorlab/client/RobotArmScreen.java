package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.RobotArmBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** [10-26] ロボットの腕の画面：ふつうの画面＋下に「覚えた動き 1/8」（教えている途中なら次に右クリックする所） */
public class RobotArmScreen extends SimpleMachineScreen {
    private static final String K = "gui.semiconductor-lab.robot_arm.";

    public RobotArmScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        if (!id.equals("program")) return super.text(id);
        int teach = menu.get(RobotArmBlockEntity.DATA_TEACH);
        if (teach == 1) return Component.translatable(K + "teaching_from");
        if (teach == 2) return Component.translatable(K + "teaching_to");
        return Component.translatable(K + "program", menu.get(RobotArmBlockEntity.DATA_STEPS), menu.get(RobotArmBlockEntity.DATA_CAPACITY));
    }
}
