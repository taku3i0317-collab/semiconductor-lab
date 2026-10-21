package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Control;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;

/**
 * GameTest：幹10 制御・計算（IMP4-01 O-011 で作った）。共通の道具は {@link LabTestBase}。
 */
public class ControlTests extends LabTestBase {

    /** [10-01] 電磁石：鉄の芯＋銅線（伸線 [1-58]）を巻く */
    @GameTest
    public void electromagnet(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "electromagnet", Items.IRON_INGOT) && recipeUses(h, "electromagnet", com.taku3.semiconductorlab.content.Metallurgy.COPPER_WIRE), "electromagnet = iron core + copper wire coil");
        h.assertTrue(new net.minecraft.world.item.ItemStack(Control.ELECTROMAGNET).getCount() == 1, "item exists");
        LOG.info("[LAB] electromagnet [10-01]: iron core + copper wire coil");
        h.succeed();
    }

    /**
     * [10-13] 4ビットのマイコン（S-014 Q25-5 案A）：組立台のレシピは cpu4 と rom_program に合格したチップ・水晶・ニキシー管など6種。
     * 合格していないチップは CPU にも ROM にもなれない。見本のプログラムは「LOAD 3・ADD 5・OUT・JUMP 6」で、出力の口は 8
     */
    @GameTest
    public void microcomputer(GameTestHelper h) {
        var a = com.taku3.semiconductorlab.machine.Assembly.of(com.taku3.semiconductorlab.content.Control.MICROCOMPUTER);
        h.assertTrue(a != null && a.parts.size() == 6, "microcomputer assembly with 6 roles");
        net.minecraft.world.item.ItemStack plain = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP);
        net.minecraft.world.item.ItemStack cpu = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP); cpu.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("cpu4"));
        net.minecraft.world.item.ItemStack rom = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP); rom.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("rom_program"));
        h.assertFalse(a.satisfied(0, plain), "a plain chip is not a CPU");
        h.assertTrue(a.satisfied(0, cpu) && !a.satisfied(0, rom), "the CPU slot takes a chip that passed cpu4");
        h.assertTrue(a.satisfied(1, rom), "the ROM slot takes a chip that passed rom_program");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Cpu4.listing(com.taku3.semiconductorlab.litho.Cpu4.ADD_3_5), "LOAD 3・ADD 5・OUT・JUMP 6", "program listing");
        h.succeed();
    }
}
