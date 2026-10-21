package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.content.Transport;
import com.taku3.semiconductorlab.item.ChronometerItem;
import com.taku3.semiconductorlab.item.Navigation;
import com.taku3.semiconductorlab.item.SextantItem;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/**
 * GameTest：幹14 交通・運輸（IMP2 O-023 で作成）。共通の道具は {@link LabTestBase}。
 */
public class TransportTests extends LabTestBase {
    /** [14-02] 羅針盤（IMP2 S-010）：何もなければ北（くるい 0）を指し、近くに鉄のブロックがあると引かれてくるう。遠い鉄は効かない */
    @GameTest
    public void magnetic_compass(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "magnetic_compass", com.taku3.semiconductorlab.content.Power.MAGNET), "needle rubbed with a lodestone");
        var lv = h.getLevel();
        BlockPos at = h.absolutePos(p(3, 2, 3));
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.CompassItem.errorDeg(lv, at)) < 1, "no iron -> points north");
        h.assertValueEqual(com.taku3.semiconductorlab.item.CompassItem.arrow(com.taku3.semiconductorlab.item.CompassItem.needleDeg(lv, at), 180f), "↑", "facing north: arrow straight ahead");
        h.assertValueEqual(com.taku3.semiconductorlab.item.CompassItem.arrow(com.taku3.semiconductorlab.item.CompassItem.needleDeg(lv, at), 0f), "↓", "facing south: arrow behind");
        h.setBlock(p(4, 2, 3), net.minecraft.world.level.block.Blocks.IRON_BLOCK);   // 東どなりに鉄
        double err = com.taku3.semiconductorlab.item.CompassItem.errorDeg(lv, at);
        h.assertTrue(err > 20, "iron to the east pulls the needle east: " + err);
        h.setBlock(p(4, 2, 3), net.minecraft.world.level.block.Blocks.AIR);
        h.setBlock(p(3 + com.taku3.semiconductorlab.item.CompassItem.IRON_RANGE + 2, 2, 3), net.minecraft.world.level.block.Blocks.IRON_BLOCK);
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.CompassItem.errorDeg(lv, at)) < 1, "far iron does nothing");
        LOG.info("[LAB] magnetic compass [14-02]: points north, iron next to it turns the needle " + String.format(java.util.Locale.ROOT, "%.0f", err) + " deg");
        h.succeed();
    }

    /** [14-13] ジュラルミン（IMP2 S-010）：Al＋Cu＋Mg を焼入れ → 放っておくと硬くなる（時効硬化）。1日で HV 60 → 120 近く、持ち物で硬いジュラルミンに変わる */
    @GameTest(maxTicks = 100)
    public void duralumin(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "duralumin_charge", com.taku3.semiconductorlab.content.Metallurgy.ALUMINIUM_INGOT) && recipeUses(h, "duralumin_charge", com.taku3.semiconductorlab.content.Metallurgy.MAGNESIUM)
                && recipeUses(h, "quenched_duralumin", Transport.DURALUMIN_CHARGE), "Al + Cu + Mg, quenched in the furnace");
        h.assertTrue(com.taku3.semiconductorlab.item.QuenchedDuraluminItem.hardness(0) == com.taku3.semiconductorlab.item.QuenchedDuraluminItem.HV_SOFT && com.taku3.semiconductorlab.item.QuenchedDuraluminItem.hardness(com.taku3.semiconductorlab.item.QuenchedDuraluminItem.AGE_TICKS) > 110 && com.taku3.semiconductorlab.item.QuenchedDuraluminItem.hardness(com.taku3.semiconductorlab.item.QuenchedDuraluminItem.AGE_TICKS / 4) < com.taku3.semiconductorlab.item.QuenchedDuraluminItem.hardness(com.taku3.semiconductorlab.item.QuenchedDuraluminItem.AGE_TICKS), "hardens with time: " + com.taku3.semiconductorlab.item.QuenchedDuraluminItem.hardness(0) + " -> " + com.taku3.semiconductorlab.item.QuenchedDuraluminItem.hardness(com.taku3.semiconductorlab.item.QuenchedDuraluminItem.AGE_TICKS));
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack q = new ItemStack(Transport.QUENCHED_DURALUMIN, 3);
        net.minecraft.nbt.CompoundTag t = new net.minecraft.nbt.CompoundTag();
        t.putLong("QuenchedAt", h.getLevel().getGameTime() - com.taku3.semiconductorlab.item.QuenchedDuraluminItem.AGE_TICKS - 20);
        q.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(t));
        player.getInventory().setItem(5, q);
        var item = (com.taku3.semiconductorlab.item.QuenchedDuraluminItem) Transport.QUENCHED_DURALUMIN;
        h.assertTrue(item.tryAge(player, q, h.getLevel().getGameTime()), "a day has passed -> it turns");
        h.assertTrue(player.getInventory().getItem(5).is(Transport.DURALUMIN) && player.getInventory().getItem(5).getCount() == 3, "aged in the inventory -> 3 hard duralumin");
        ItemStack fresh = new ItemStack(Transport.QUENCHED_DURALUMIN);
        player.getInventory().setItem(6, fresh);
        h.assertFalse(item.tryAge(player, fresh, h.getLevel().getGameTime()), "just quenched: first records the time, stays soft");
        LOG.info("[LAB] duralumin [14-13]: quenched HV " + com.taku3.semiconductorlab.item.QuenchedDuraluminItem.HV_SOFT + " -> aged HV " + com.taku3.semiconductorlab.item.QuenchedDuraluminItem.hardness(com.taku3.semiconductorlab.item.QuenchedDuraluminItem.AGE_TICKS) + " after a day in the inventory");
        h.succeed();
    }

    /** [14-03] クロノメーター（IMP2 O-023）：東経30°（x=30000）で南中はグリニッジ 10時。六分儀＋クロノメーターで経度 30°E。狂った時計は経度もずれる */
    @GameTest
    public void chronometer_longitude(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "marine_chronometer", com.taku3.semiconductorlab.content.Metallurgy.CRUCIBLE_STEEL)
                && recipeUses(h, "marine_chronometer", com.taku3.semiconductorlab.content.Optics.RUBY_BOULE), "chronometer = crucible-steel spring + ruby jewels");
        BlockPos at = new BlockPos(30_000, 64, -10_000);   // 東経30°・北緯10°
        ItemStack clock = new ItemStack(Transport.MARINE_CHRONOMETER), sx = new ItemStack(Precision.SEXTANT);
        ChronometerItem.set(clock, 0, 0);
        int stage = 0;
        for (long t : new long[] {3_000, 3_600, 4_000, 4_400}) stage = SextantItem.sight(sx, h.getLevel(), at, t, clock, null);
        h.assertValueEqual(stage, 2, "noon passed");
        long maxTick = sx.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getLongOr("MaxTick", 0);
        double lon = SextantItem.longitudeFromNoon(ChronometerItem.readingHours(clock, maxTick));
        h.assertTrue(Math.abs(lon - 30) < 0.01, "local noon at 10:00 GMT -> 30 E, got " + lon);
        ItemStack bad = new ItemStack(Transport.MARINE_CHRONOMETER);
        ChronometerItem.set(bad, 0, 4);   // 1日に4秒進む
        long tenDays = 10 * Navigation.DAY_TICKS + 4_000;
        double err = ChronometerItem.errorSec(bad, tenDays);
        double badLon = SextantItem.longitudeFromNoon(ChronometerItem.readingHours(bad, tenDays));
        h.assertTrue(Math.abs(err - 40.67) < 0.1 && Math.abs(badLon - 30) > 0.15, "40 s fast after 10 days -> longitude off by " + Math.abs(badLon - 30) * 60 + "'");
        LOG.info("[LAB] chronometer [14-03]: noon at 10:00 GMT -> 30 E; a clock 40 s fast misplaces the ship by " + String.format(java.util.Locale.ROOT, "%.1f", Math.abs(badLon - 30) * 60) + " arcmin");
        h.succeed();
    }
    /** [14-22] ベルトコンベア（IMP4-01、O-029）：チェスト → 駆動 → ベルト（東2マス→北へ曲がる1マス）→ チェスト。1秒1マスで運び、終わりの先へ入れる。電気はベルト1マス 5W */
    @GameTest
    public void conveyorBelt(GameTestHelper h) {
        var E = net.minecraft.core.Direction.EAST; var Nd = net.minecraft.core.Direction.NORTH;
        var F = net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
        h.setBlock(p(1, 1, 3), net.minecraft.world.level.block.Blocks.CHEST);
        h.setBlock(p(2, 1, 3), Transport.CONVEYOR_DRIVE.block);
        h.setBlock(p(3, 1, 3), Transport.CONVEYOR_BELT.block.defaultBlockState().setValue(F, E));
        h.setBlock(p(4, 1, 3), Transport.CONVEYOR_BELT.block.defaultBlockState().setValue(F, E));
        h.setBlock(p(5, 1, 3), Transport.CONVEYOR_BELT.block.defaultBlockState().setValue(F, Nd));
        h.setBlock(p(5, 1, 2), net.minecraft.world.level.block.Blocks.CHEST);
        var src = (net.minecraft.world.level.block.entity.ChestBlockEntity) h.getBlockEntity(p(1, 1, 3), net.minecraft.world.level.block.entity.ChestBlockEntity.class);
        src.setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 10));
        src.setItem(1, new ItemStack(net.minecraft.world.item.Items.COPPER_INGOT, 5));
        BlockPos drive = h.absolutePos(p(2, 1, 3));
        var lv = h.getLevel();
        h.assertValueEqual(com.taku3.semiconductorlab.block.ConveyorDriveBlockEntity.belts(lv, drive).size(), 3, "3 belts, following the corner");
        for (int i = 0; i < 8; i++) com.taku3.semiconductorlab.block.ConveyorDriveBlockEntity.step(lv, drive);
        var dst = (net.minecraft.world.level.block.entity.ChestBlockEntity) h.getBlockEntity(p(5, 1, 2), net.minecraft.world.level.block.entity.ChestBlockEntity.class);
        int iron = 0, copper = 0;
        for (int i = 0; i < dst.getContainerSize(); i++) { var s = dst.getItem(i); if (s.is(net.minecraft.world.item.Items.IRON_INGOT)) iron += s.getCount(); if (s.is(net.minecraft.world.item.Items.COPPER_INGOT)) copper += s.getCount(); }
        h.assertTrue(iron == 10 && copper == 5 && src.isEmpty(), "carried to the far chest: iron " + iron + ", copper " + copper);
        h.assertTrue(recipeUses(h, "conveyor_drive", com.taku3.semiconductorlab.content.Power.ELECTRIC_MOTOR) && recipeUses(h, "conveyor_belt", com.taku3.semiconductorlab.content.Chemistry.VULCANIZED_RUBBER), "motor drive, rubber belt");
        h.assertValueEqual(com.taku3.semiconductorlab.block.ConveyorDriveBlockEntity.WATTS_PER_BELT, 5.0, "5 W per belt block");
        h.succeed();
    }

    /** [14-23] 天井の搬送 OHT（IMP4-01、O-029）：ロードポートの出す FOUP が25枚でいっぱい → 駆動が持ち上げ、レール3マスの先のロードポートの入れる口へ降ろす */
    @GameTest(maxTicks = 200)
    public void ohtRail(GameTestHelper h) {
        var LP = com.taku3.semiconductorlab.content.Semiconductor.LOAD_PORT;
        h.setBlock(p(1, 1, 1), LP.block);
        h.setBlock(p(4, 1, 1), LP.block);
        h.setBlock(p(1, 3, 1), Transport.OHT_DRIVE.block);
        for (int x = 2; x <= 4; x++) h.setBlock(p(x, 3, 1), Transport.OHT_RAIL);
        var a = (com.taku3.semiconductorlab.block.LoadPortBlockEntity) h.getBlockEntity(p(1, 1, 1), com.taku3.semiconductorlab.block.LoadPortBlockEntity.class);
        var b = (com.taku3.semiconductorlab.block.LoadPortBlockEntity) h.getBlockEntity(p(4, 1, 1), com.taku3.semiconductorlab.block.LoadPortBlockEntity.class);
        ItemStack foup = new ItemStack(com.taku3.semiconductorlab.content.Semiconductor.FOUP);
        com.taku3.semiconductorlab.item.FoupItem.add(foup, new ItemStack(com.taku3.semiconductorlab.content.Semiconductor.SILICON_WAFER, 25));
        a.getContainer().setItem(com.taku3.semiconductorlab.block.LoadPortBlockEntity.SLOT_OUT_FOUP, foup);
        BlockPos drive = h.absolutePos(p(1, 3, 1));
        h.assertTrue(com.taku3.semiconductorlab.block.OhtDriveBlockEntity.shouldSend(a), "a full FOUP is sent");
        var r = com.taku3.semiconductorlab.block.OhtDriveBlockEntity.route(h.getLevel(), drive);
        h.assertTrue(r != null && r.rails().size() == 3 && r.port().equals(h.absolutePos(p(4, 1, 1))), "route: 3 rails to the far load port");
        var d = (com.taku3.semiconductorlab.block.OhtDriveBlockEntity) h.getBlockEntity(p(1, 3, 1), com.taku3.semiconductorlab.block.OhtDriveBlockEntity.class);
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) d.getStorage()).insertRaw(4_000);
        h.assertTrue(recipeUses(h, "oht_drive", com.taku3.semiconductorlab.content.Power.ELECTRIC_MOTOR) && recipeUses(h, "oht_rail", com.taku3.semiconductorlab.content.Metallurgy.STEEL_INGOT), "motor drive, steel rail");
        h.succeedWhen(() -> {
            h.assertValueEqual(com.taku3.semiconductorlab.item.FoupItem.contents(b.getContainer().getItem(com.taku3.semiconductorlab.block.LoadPortBlockEntity.SLOT_IN_FOUP)).getCount(), 25, "25 wafers dropped at the far load port");
            h.assertTrue(a.getContainer().getItem(com.taku3.semiconductorlab.block.LoadPortBlockEntity.SLOT_OUT_FOUP).isEmpty(), "lifted from the first");
        });
    }

}
