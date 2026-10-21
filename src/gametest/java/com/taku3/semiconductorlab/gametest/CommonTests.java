package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Branches;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.content.Survival;
import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.block.BessemerConverterBlockEntity;
import com.taku3.semiconductorlab.block.CompressorBlockEntity;
import com.taku3.semiconductorlab.block.ElectrolyzerBlockEntity;
import com.taku3.semiconductorlab.block.ElectricFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.AchesonFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.GasHolderBlockEntity;
import com.taku3.semiconductorlab.block.GasType;
import com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.HandVacuumPumpBlock;
import com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity;
import com.taku3.semiconductorlab.block.VacuumChamberBlockEntity;
import com.taku3.semiconductorlab.block.SealingStationBlockEntity;
import com.taku3.semiconductorlab.item.MercuryVacuumGaugeItem;
import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.HclBurnerBlockEntity;
import com.taku3.semiconductorlab.block.SaltPanBlockEntity;
import com.taku3.semiconductorlab.block.SprengelPumpBlockEntity;
import com.taku3.semiconductorlab.block.AirSeparationBlockEntity;
import com.taku3.semiconductorlab.block.CrystalPullerBlockEntity;
import com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity;
import com.taku3.semiconductorlab.energy.CableBlockEntity;
import com.taku3.semiconductorlab.energy.CableNetwork;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.energy.GeneratorBlockEntity;
import com.taku3.semiconductorlab.energy.SteamEngineBlockEntity;
import com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity;
import com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyApi;
import com.taku3.semiconductorlab.energy.WaterWheelBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import net.minecraft.tags.BiomeTags;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * GameTest：MOD 全体（レシピ・進捗・ガイド・図鑑・lang・機械の画面・レシピの書き出し）（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class CommonTests extends LabTestBase {
    // =====================================================================
    // マルチブロックの形と判定（IMP-33 ③、DESIGN「マルチブロック」）
    // =====================================================================

    /**
     * 形の判定：3×3×4 の見本（石れんがの壁・ガラスの窓・中は空気）を4方向に置くと、どの向きでも組み上がる。
     * 1個こわす → その場所だけ「足りない」、ちがうブロック → 「ちがう」、中に物 → 「中」。足りない数を名前ごとに数える
     */
    @GameTest(maxTicks = 100)
    public void multiblockPattern(GameTestHelper h) {
        var pat = com.taku3.semiconductorlab.machine.MultiblockPattern.builder()
                .layer("BBB", "BCB", "BBB")
                .layer("BTB", "T T", "BTB")
                .layer("BBB", "B B", "BBB")
                .layer("BBB", "B B", "BBB")
                .where('B', net.minecraft.world.level.block.Blocks.STONE_BRICKS, "lining")
                .where('T', net.minecraft.world.level.block.Blocks.GLASS, "tuyere")
                .controller('C').build();
        var lv = h.getLevel();
        int i = 0;
        for (var facing : new net.minecraft.core.Direction[] {net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.WEST}) {
            BlockPos ctl = h.absolutePos(p(4, 1, 4));   // 同じ場所で向きだけ変える（テストの場所は 8×8×8）
            i++;
            for (BlockPos q : BlockPos.betweenClosed(ctl.offset(-2, 0, -2), ctl.offset(2, 3, 2))) lv.setBlockAndUpdate(q, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            for (int y = 0; y < pat.height(); y++) for (int z = 0; z < pat.depth(); z++) for (int x = 0; x < pat.width(); x++) {
                char c = pat.layers().get(y)[z].charAt(x);
                var at = pat.worldPos(ctl, facing, x, y, z);
                var state = c == 'B' ? net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState()
                        : c == 'T' ? net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState()
                        : c == 'C' ? net.minecraft.world.level.block.Blocks.FURNACE.defaultBlockState()
                        : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                lv.setBlockAndUpdate(at, state);
            }
            var r = pat.check(lv, ctl, facing);
            h.assertTrue(r.formed(), facing + ": formed, problems " + r.problems());
            // 2段目の羽口（左）をこわす → 1か所「足りない」
            var tuyere = pat.worldPos(ctl, facing, 0, 1, 1);
            lv.setBlockAndUpdate(tuyere, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            r = pat.check(lv, ctl, facing);
            h.assertTrue(!r.formed() && r.problems().size() == 1 && r.problems().get(0).missing() && r.problems().get(0).pos().equals(tuyere)
                    && r.missingCounts().get("tuyere") == 1, facing + ": one missing tuyere at the right place, got " + r.problems());
            // ちがうブロック、中に物
            lv.setBlockAndUpdate(tuyere, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
            lv.setBlockAndUpdate(pat.worldPos(ctl, facing, 1, 2, 1), net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
            r = pat.check(lv, ctl, facing);
            h.assertTrue(r.problems().size() == 2 && r.problems().stream().noneMatch(com.taku3.semiconductorlab.machine.MultiblockPattern.Problem::missing)
                    && r.missingCounts().containsKey("inside"), facing + ": wrong block + something inside, got " + r.problems());
        }
        LOG.info("[LAB] multiblock pattern: 3x3x4 sample formed in all 4 directions; missing / wrong / inside reported per position");
        h.succeed();
    }

    @GameTest
    public void recipesResolve(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        for (String r : RECIPES) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(r));
            if (server.getRecipeManager().byKey(key).isEmpty()) missing.add(r);
        }
        h.assertTrue(missing.isEmpty(), "recipes not loaded (JSON error or missing item?): " + missing);
        LOG.info("[LAB] recipes: all {} resolved in RecipeManager", RECIPES.length);
        h.succeed();
    }

    // =====================================================================
    // 機械の共通化（IMP-28）：Machine 1行の登録・共通の画面 MachineMenu
    // =====================================================================

    /**
     * 画面を持つ全部の機械で、サーバーの画面（BE から作る）とクライアントの画面（MachineLayout だけから作る）の
     * スロットの数・数値の数がそろっているか。ずれると画面の数字が化けたり、つないだときに落ちたりする。
     * シフトクリックで入口に入ること・出口には入らないことも確かめる。
     */
    @GameTest
    public void machineMenus(GameTestHelper h) {
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos fp = p(1, 1, 1);
        int checked = 0;
        for (var m : com.taku3.semiconductorlab.machine.Machine.all()) {
            h.assertTrue(BuiltInRegistries.BLOCK.getKey(m.block).getPath().equals(m.name), "block id = machine name: " + m.name);
            h.assertTrue(m.type.isValid(m.block.defaultBlockState()), "BE type accepts its block: " + m.name);
            h.assertTrue(com.taku3.semiconductorlab.machine.Machine.of(m.type) == m, "lookup by BE type: " + m.name);
            if (m.menu == null) continue;
            h.setBlock(fp, m.block);
            var be = h.getBlockEntity(fp, net.minecraft.world.level.block.entity.BlockEntity.class);
            h.assertTrue(be instanceof com.taku3.semiconductorlab.machine.MachineBE, "BE has a screen: " + m.name);
            var mbe = (com.taku3.semiconductorlab.machine.MachineBE) be;
            var server = (com.taku3.semiconductorlab.machine.MachineMenu) mbe.createMenu(1, player.getInventory(), player);
            var client = new com.taku3.semiconductorlab.machine.MachineMenu(m, 1, player.getInventory());
            int machineSlots = m.layout.slots().size();
            h.assertValueEqual(server.slots.size(), machineSlots + 36, "server slots: " + m.name);
            h.assertValueEqual(client.slots.size(), server.slots.size(), "client slots = server slots: " + m.name);
            h.assertValueEqual(m.layout.dataCount(), mbe.getMenuData().getCount(), "synced numbers (client = BE): " + m.name);
            h.assertTrue(mbe.getContainer().getContainerSize() >= m.layout.containerSize(), "container big enough: " + m.name);
            h.assertTrue(mbe.getDisplayName().getString().equals(m.block.getName().getString()), "title = block name: " + m.name);
            for (int i = 0; i < machineSlots; i++) {
                var s = m.layout.slots().get(i);
                h.assertValueEqual(server.getSlot(i).getContainerSlot(), s.index(), "slot order: " + m.name + " #" + i);
                if (s.isOutput()) h.assertFalse(server.getSlot(i).mayPlace(new ItemStack(Items.DIRT)), "output takes nothing: " + m.name);
            }
            h.setBlock(fp, Blocks.AIR);
            checked++;
        }
        // シフトクリック：持ち物の材料 → 入れられる最初の入口。出口には入らない
        h.setBlock(fp, Power.ELECTRIC_FURNACE.block);
        var ef = h.getBlockEntity(fp, ElectricFurnaceBlockEntity.class);
        var menu = (com.taku3.semiconductorlab.machine.MachineMenu) ef.createMenu(1, player.getInventory(), player);
        player.getInventory().setItem(0, new ItemStack(Heat.REFRACTORY_BRICK_BLANK, 5));
        int invSlot = -1;
        for (int i = 0; i < menu.slots.size(); i++) if (menu.getSlot(i).container == player.getInventory() && menu.getSlot(i).getContainerSlot() == 0) invSlot = i;
        menu.quickMoveStack(player, invSlot);
        h.assertValueEqual(ef.getContainer().getItem(ElectricFurnaceBlockEntity.SLOT_IN).getCount(), 5, "shift-click puts the blank into the input");
        player.getInventory().setItem(0, new ItemStack(Items.DIRT, 3));
        menu.quickMoveStack(player, invSlot);
        h.assertTrue(ef.getContainer().getItem(ElectricFurnaceBlockEntity.SLOT_OUT).isEmpty(), "dirt does not go into the output");
        h.assertValueEqual(player.getInventory().getItem(0).getCount(), 3, "dirt stays in the inventory");
        h.assertTrue(com.taku3.semiconductorlab.machine.Machine.all().size() >= 29, "machines registered: " + com.taku3.semiconductorlab.machine.Machine.all().size());
        LOG.info("[LAB] machine menus: {} machines with a screen, {} machines in all (IMP-28)", checked, com.taku3.semiconductorlab.machine.Machine.all().size());
        h.succeed();
    }

    /**
     * 画面のスロットの位置（IMP-30 ①）。全部の機械の MachineLayout で：
     * スロットの枠（18×18、左上は (x-1, y-1)）が画面（176×166）の内側の縁（4px）より内にある／題名の帯（y &lt; 16）にかからない／
     * プレイヤーの持ち物（y ≥ 83）と「持ち物」の文字（左下 x 7〜77・y 71〜81）に重ならない／ほかのスロットと重ならない。
     * 文字の幅・バーとの重なりは画面を描かないと分からないので、クライアントのテスト（ScreenChecks）で見る。
     */
    @GameTest
    public void machineSlotLayout(GameTestHelper h) {
        final int W = 176, EDGE = 4, TITLE_BOTTOM = 16;
        List<String> bad = new ArrayList<>();
        int n = 0;
        for (var m : com.taku3.semiconductorlab.machine.Machine.all()) {
            if (m.layout == null) continue;
            var ss = m.layout.slots();
            final int INV_TOP = m.layout.height() - 83, LABEL_Y = m.layout.height() - 95;   // 画面の高さはレイアウトのデータ（ふつうは 166）
            for (int i = 0; i < ss.size(); i++) {
                var s = ss.get(i);
                int x0 = s.x() - 1, y0 = s.y() - 1, x1 = x0 + 18, y1 = y0 + 18;
                String id = m.name + " slot#" + s.index() + " (" + s.x() + "," + s.y() + ")";
                if (x0 < EDGE || x1 > W - EDGE) bad.add(id + " sticks out left/right");
                if (y0 < TITLE_BOTTOM) bad.add(id + " overlaps the title band");
                if (y1 > INV_TOP) bad.add(id + " overlaps the player inventory");
                if (x0 < 77 && x1 > 7 && y0 < LABEL_Y + 10 && y1 > LABEL_Y) bad.add(id + " overlaps the inventory label");
                for (int j = i + 1; j < ss.size(); j++) {
                    var t = ss.get(j);
                    if (Math.abs(s.x() - t.x()) < 18 && Math.abs(s.y() - t.y()) < 18) bad.add(id + " overlaps slot#" + t.index() + " (" + t.x() + "," + t.y() + ")");
                }
                n++;
            }
        }
        LOG.info("[LAB] slot layout: {} slots checked, {} problems (IMP-30)", n, bad.size());
        bad.forEach(b -> LOG.warn("[LAB] slot layout: {}", b));
        h.assertTrue(bad.isEmpty(), "slot layout problems: " + bad);
        h.succeed();
    }

    /**
     * 専用のクリエイティブタブ（IMP-31）：登録されている／MOD のアイテムが全部（ブロックのアイテムも）並ぶ／
     * 並びの表（creative_tab_order.txt）に全部が載っていて、もう無い ID が残っていない（足したら python tools/gen_tab_order.py）
     */
    @GameTest
    public void creativeTab(GameTestHelper h) {
        h.assertTrue(BuiltInRegistries.CREATIVE_MODE_TAB.getValue(com.taku3.semiconductorlab.content.Reg.TAB) != null, "tab Semiconductor Lab is registered");
        java.util.Set<String> mod = new java.util.TreeSet<>();
        for (var it : BuiltInRegistries.ITEM) { var id = BuiltInRegistries.ITEM.getKey(it); if (id.getNamespace().equals(SemiconductorLab.MOD_ID)) mod.add(id.getPath()); }
        var order = com.taku3.semiconductorlab.content.Reg.tabOrder();
        java.util.Set<String> inTab = new java.util.TreeSet<>();
        for (var it : order) inTab.add(BuiltInRegistries.ITEM.getKey(it).getPath());
        h.assertTrue(inTab.equals(mod), "every MOD item is in the tab; missing " + new java.util.TreeSet<>(mod) {{ removeAll(inTab); }});
        java.util.List<String> listed = new java.util.ArrayList<>();
        try (var in = com.taku3.semiconductorlab.content.Reg.class.getResourceAsStream("/assets/semiconductor-lab/creative_tab_order.txt")) {
            h.assertTrue(in != null, "creative_tab_order.txt exists");
            for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\n")) { String id = line.split("#")[0].trim(); if (!id.isEmpty()) listed.add(id); }
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        var notListed = new java.util.TreeSet<>(mod); notListed.removeAll(listed);
        var stale = new java.util.TreeSet<>(listed); stale.removeAll(mod);
        h.assertTrue(notListed.isEmpty(), "not in creative_tab_order.txt (run python tools/gen_tab_order.py): " + notListed);
        h.assertTrue(stale.isEmpty(), "creative_tab_order.txt has ids that no longer exist: " + stale);
        for (int i = 0; i < listed.size(); i++)
            h.assertValueEqual(BuiltInRegistries.ITEM.getKey(order.get(i)).getPath(), listed.get(i), "tab order follows the table at #" + i);
        LOG.info("[LAB] creative tab: {} items, first {} … last {} (IMP-31)", order.size(), listed.get(0), listed.get(listed.size() - 1));
        h.succeed();
    }

    @GameTest
    public void exportMachineRecipes(GameTestHelper h) {
        var a = new com.google.gson.JsonArray();
        // 幹ごとのテストクラスが自分の機械の分を足す（IMP-28）。並びは機械の ID 順（同じ機械の中は書いた順）に固定する
        SurvivalTests.machineRecipes(a);
        MetallurgyTests.machineRecipes(a);
        ChemistryTests.machineRecipes(a);
        HeatTests.machineRecipes(a);
        PowerTests.machineRecipes(a);
        VacuumTests.machineRecipes(a);
        GasTests.machineRecipes(a);
        OpticsTests.machineRecipes(a);
        PrecisionTests.machineRecipes(a);
        SemiconductorTests.machineRecipes(a);
        var sorted = new java.util.ArrayList<com.google.gson.JsonElement>();
        a.forEach(sorted::add);
        sorted.sort(java.util.Comparator.comparing(e -> e.getAsJsonObject().get("machine").getAsString()));   // List.sort は安定（同じ機械の中の順は変わらない）
        a = new com.google.gson.JsonArray();
        for (var e : sorted) a.add(e);

        var root = new com.google.gson.JsonObject();
        root.addProperty("_note", "自動生成：GameTest exportMachineRecipes（tools/test.ps1 で更新）。各機械の定数とアイテムの登録から作る。watts が負＝発電、0＝電気なし。"
            + "量：count（1回あたり）・unit・per_runs（N回で count 個へる）・consumed=false（減らない）・max（1回の上限）・alt（代わり）・cond（条件）");
        root.add("recipes", a);
        java.nio.file.Path dir = java.nio.file.Paths.get("").toAbsolutePath();
        while (dir != null && !java.nio.file.Files.exists(dir.resolve("docs").resolve("TECH_TREE.md"))) dir = dir.getParent();
        h.assertTrue(dir != null, "project root (docs/TECH_TREE.md) not found from " + java.nio.file.Paths.get("").toAbsolutePath());
        try {
            java.nio.file.Files.writeString(dir.resolve("docs").resolve("machine_recipes.json"),
                    new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n", java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        // 量は全部数字の項目になっている（説明文の量がない）
        for (var r : a) for (String side : new String[] {"inputs", "outputs"})
            for (var x : r.getAsJsonObject().getAsJsonArray(side)) h.assertTrue(x.getAsJsonObject().get("count").isJsonPrimitive(), "every amount is a number");
        h.assertTrue(a.size() >= 25, "machine recipes: " + a.size());
        LOG.info("[LAB] machine recipes: {} entries written to docs/machine_recipes.json", a.size());
        h.succeed();
    }

    // =====================================================================
    // レッドストーンは使わない（DESIGN、2026-10-20）・トランジスタラジオの本物の部品
    // =====================================================================

    /** MOD のレシピに、レッドストーン（粉・ブロック・たいまつ・反復装置・比較装置・ランプ）が1つも入っていない */
    @GameTest
    public void noRedstoneInRecipes(GameTestHelper h) {
        net.minecraft.world.item.Item[] rs = {Items.REDSTONE, Items.REDSTONE_BLOCK, Items.REDSTONE_TORCH, Items.REPEATER, Items.COMPARATOR, Items.REDSTONE_LAMP};
        java.util.List<String> bad = new java.util.ArrayList<>();
        int n = 0;
        for (var r : h.getLevel().getServer().getRecipeManager().getRecipes()) {
            if (!r.id().identifier().getNamespace().equals(SemiconductorLab.MOD_ID)) continue;
            n++;
            for (var ing : r.value().placementInfo().ingredients())
                for (var it : rs) if (ing.test(new ItemStack(it))) bad.add(r.id().identifier().getPath() + ":" + it);
        }
        h.assertTrue(n > 50, "MOD recipes found: " + n);
        h.assertTrue(bad.isEmpty(), "redstone must not be a material: " + bad);
        LOG.info("[LAB] no redstone in {} MOD recipes", n);
        h.succeed();
    }

    @GameTest
    public void advancementsAndFunctions(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        for (String a : CHAPTER1) if (server.getAdvancements().get(SemiconductorLab.id("chapter1/" + a)) == null) missing.add("advancement chapter1/" + a);
        for (String a : GUIDE_ADV) if (server.getAdvancements().get(SemiconductorLab.id("guide/" + a)) == null) missing.add("advancement guide/" + a);
        for (String f : GUIDE_FN) if (server.getFunctions().get(SemiconductorLab.id(f)).isEmpty()) missing.add("function " + f);
        long ch1 = server.getAdvancements().getAllAdvancements().stream()
                .filter(x -> x.id().getNamespace().equals(SemiconductorLab.MOD_ID) && x.id().getPath().startsWith("chapter1/")).count();
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        h.assertValueEqual(ch1, 11L, "number of chapter1 advancements");
        LOG.info("[LAB] advancements: chapter1 = {}, guide = {}, functions = {} all registered", ch1, GUIDE_ADV.length, GUIDE_FN.length);
        h.succeed();
    }

    /** guide2/ の11個と give_guidebook2 が登録され、ルートの報酬が本を渡す関数になっている */
    @GameTest
    public void guide2Electricity(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        for (String a : GUIDE2_ADV) if (server.getAdvancements().get(SemiconductorLab.id("guide2/" + a)) == null) missing.add("guide2/" + a);
        if (server.getFunctions().get(SemiconductorLab.id("give_guidebook2")).isEmpty()) missing.add("function give_guidebook2");
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        long n = server.getAdvancements().getAllAdvancements().stream()
                .filter(x -> x.id().getNamespace().equals(SemiconductorLab.MOD_ID) && x.id().getPath().startsWith("guide2/")).count();
        h.assertValueEqual(n, (long) GUIDE2_ADV.length, "number of guide2 advancements");
        LOG.info("[LAB] guide2 (electricity): {} advancements + give_guidebook2 registered", n);
        h.succeed();
    }

    /** O-024：ガイド第6巻「微細化」（guide6/）の13個と give_guidebook6。ページを開く物がみな登録されている */
    @GameTest
    public void guide6Shrinking(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        String[] adv = {"root", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10", "p11", "p12"};
        List<String> missing = new ArrayList<>();
        for (String a : adv) if (server.getAdvancements().get(SemiconductorLab.id("guide6/" + a)) == null) missing.add("guide6/" + a);
        if (server.getFunctions().get(SemiconductorLab.id("give_guidebook6")).isEmpty()) missing.add("function give_guidebook6");
        for (String id : new String[] {"cmp_polisher", "drafting_table", "dry_etcher", "i_line_filter", "ion_implanter", "probe_station", "projection_lens", "projection_lens_krf", "rectifier", "stepper", "transformer"})
            if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(SemiconductorLab.id(id))) missing.add("item " + id);
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        LOG.info("[LAB] guide6 (shrinking): {} advancements + give_guidebook6", adv.length);
        h.succeed();
    }

    /** O-028：ガイド第7巻「極限の微細化」（guide7/）の10個と give_guidebook7。ページを開く物がみな登録されている */
    @GameTest
    public void guide7Extreme(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        String[] adv = {"root", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};
        List<String> missing = new ArrayList<>();
        for (String a : adv) if (server.getAdvancements().get(SemiconductorLab.id("guide7/" + a)) == null) missing.add("guide7/" + a);
        if (server.getFunctions().get(SemiconductorLab.id("give_guidebook7")).isEmpty()) missing.add("function give_guidebook7");
        for (String id : new String[] {"ald_reactor", "chemically_amplified_resist", "cmp_polisher", "dry_etcher", "electroplating_bath", "hepa_filter", "projection_lens_immersion", "scanner", "wafer_prober"})
            if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(SemiconductorLab.id(id))) missing.add("item " + id);
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        LOG.info("[LAB] guide7 (extreme): {} advancements + give_guidebook7", adv.length);
        h.succeed();
    }

    /** O-019：ガイド第5巻「論理回路」（guide5/）の11個と give_guidebook5、進捗のつながり（NOT → 最初のハンコ → NOR） */
    @GameTest
    public void guide5Logic(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        String[] adv = {"root", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};
        List<String> missing = new ArrayList<>();
        for (String a : adv) if (server.getAdvancements().get(SemiconductorLab.id("guide5/" + a)) == null) missing.add("guide5/" + a);
        if (server.getFunctions().get(SemiconductorLab.id("give_guidebook5")).isEmpty()) missing.add("function give_guidebook5");
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        var nor = server.getAdvancements().get(SemiconductorLab.id("challenge/nor"));
        h.assertTrue(nor != null && nor.value().parent().map(p -> p.getPath().equals("challenge/first_stamp")).orElse(false), "NOR comes after the first stamp");
        LOG.info("[LAB] guide5 (logic): {} advancements + give_guidebook5, challenge chain NOT -> first stamp -> NOR", adv.length);
        h.succeed();
    }

    /** O-011：ガイド第4巻「IC」（guide4/）の11個と give_guidebook4 が登録されている。ページは en・ja の両方にある */
    @GameTest
    public void guide4Ic(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        String[] adv = {"root", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};
        List<String> missing = new ArrayList<>();
        for (String a : adv) if (server.getAdvancements().get(SemiconductorLab.id("guide4/" + a)) == null) missing.add("guide4/" + a);
        if (server.getFunctions().get(SemiconductorLab.id("give_guidebook4")).isEmpty()) missing.add("function give_guidebook4");
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        long n = server.getAdvancements().getAllAdvancements().stream()
                .filter(x -> x.id().getNamespace().equals(SemiconductorLab.MOD_ID) && x.id().getPath().startsWith("guide4/")).count();
        h.assertValueEqual(n, (long) adv.length, "number of guide4 advancements");
        LOG.info("[LAB] guide4 (ICs): {} advancements + give_guidebook4 registered", n);
        h.succeed();
    }

    /** IMP-26：ガイド第3巻「シリコン」（guide3/）の10個と give_guidebook3 が登録されている */
    @GameTest
    public void guide3Silicon(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        for (String a : GUIDE3_ADV) if (server.getAdvancements().get(SemiconductorLab.id("guide3/" + a)) == null) missing.add("guide3/" + a);
        if (server.getFunctions().get(SemiconductorLab.id("give_guidebook3")).isEmpty()) missing.add("function give_guidebook3");
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        long n = server.getAdvancements().getAllAdvancements().stream()
                .filter(x -> x.id().getNamespace().equals(SemiconductorLab.MOD_ID) && x.id().getPath().startsWith("guide3/")).count();
        h.assertValueEqual(n, (long) GUIDE3_ADV.length, "number of guide3 advancements");
        LOG.info("[LAB] guide3 (silicon): {} advancements + give_guidebook3 registered", n);
        h.succeed();
    }

    /** MOD の全アイテムに へえ①〜④（en/ja）があり、図鑑タブ zukan/<id> が全部ある */
    @GameTest
    public void notesAndZukanComplete(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        var en = langJson("en_us");
        var ja = langJson("ja_jp");
        List<String> missing = new ArrayList<>();
        int items = 0;
        for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if (!key.getNamespace().equals(SemiconductorLab.MOD_ID)) continue;
            items++;
            String base = (en.has("item." + key.getNamespace() + "." + key.getPath()) ? "item." : "block.") + key.getNamespace() + "." + key.getPath();
            for (var lang : List.of(en, ja)) {
                for (String s : new String[] {"note1", "note3", "note4"}) if (!lang.has(base + "." + s)) missing.add(base + "." + s);
                if (!lang.has(base + ".note2") && !lang.has(base + ".note")) missing.add(base + ".note2");
            }
            if (server.getAdvancements().get(SemiconductorLab.id("zukan/" + key.getPath())) == null) missing.add("zukan/" + key.getPath());
        }
        h.assertTrue(missing.isEmpty(), "missing (" + missing.size() + "): " + missing);
        LOG.info("[LAB] notes: all {} items have note1-4 in en/ja and a zukan entry", items);
        h.succeed();
    }

    /** 26.2 以降のバニラ要素が実在するかをログに出すだけ（失敗にしない） */
    @GameTest(required = false)
    public void vanillaElementsExist(GameTestHelper h) {
        for (String n : new String[] {"cinnabar", "sulfur", "sulfur_spike", "potent_sulfur"}) {
            Identifier id = Identifier.withDefaultNamespace(n);
            LOG.info("[LAB] [VANILLA] minecraft:{} block={} item={}", n, BuiltInRegistries.BLOCK.containsKey(id), BuiltInRegistries.ITEM.containsKey(id));
        }
        // 辰砂・硫黄の仲間（建材の派生など）を、レジストリにある実名で全部出す（[1-45] の「ゲーム」行の根拠）
        BuiltInRegistries.ITEM.keySet().stream().filter(i -> i.getNamespace().equals("minecraft"))
                .filter(i -> i.getPath().contains("cinnabar") || i.getPath().contains("sulfur")).sorted()
                .forEach(i -> LOG.info("[LAB] [VANILLA] registry item {} (block={})", i, BuiltInRegistries.BLOCK.containsKey(i)));
        h.succeed();
    }

    // =====================================================================
    // 容器（IMP-33、DESIGN「容器のしくみ」）
    // =====================================================================

    /**
     * 中身入りのアイテムは容器が1つ決まっていて、作業台で使うと空の容器が戻り、重なる数は容器の数。
     * 相性表（Vessel.ALLOWED：フッ酸×ガラス瓶はだめ、など）に合っているか。機械で使ったら空の容器が上に出るか。
     */
    @GameTest
    public void vessels(GameTestHelper h) {
        List<String> problems = new ArrayList<>();
        var all = com.taku3.semiconductorlab.machine.Vessel.all();
        for (var e : all.entrySet()) {
            var item = e.getKey(); var v = e.getValue();
            String id = BuiltInRegistries.ITEM.getKey(item).getPath();
            var rem = item.getCraftingRemainder();
            var remItem = rem == null ? null : rem.create().getItem();
            if (v.empty() != null && remItem != v.empty()) problems.add(id + ": crafting remainder " + remItem + " != " + v.empty());
            if (v.empty() == null && remItem != null) problems.add(id + ": bag must not leave a remainder");
            if (item.getDefaultMaxStackSize() != v.stack) problems.add(id + ": stack " + item.getDefaultMaxStackSize() + " != " + v.stack);
            var ok = com.taku3.semiconductorlab.machine.Vessel.ALLOWED.get(id);
            if (ok != null && !ok.contains(v)) problems.add(id + ": " + v + " is not allowed (" + ok + ")");
        }
        h.assertTrue(problems.isEmpty(), "vessel problems: " + problems);
        h.assertTrue(all.size() >= 16, "gas bags, cylinders, acids, phosphorus, mercury are all in containers: " + all.size());
        h.assertTrue(all.get(Chemistry.PHOSPHORUS) == com.taku3.semiconductorlab.machine.Vessel.WATER_JAR, "white phosphorus is kept under water");
        h.assertTrue(all.get(Metallurgy.MERCURY) == com.taku3.semiconductorlab.machine.Vessel.IRON_FLASK, "mercury in an iron flask");
        h.assertTrue(!com.taku3.semiconductorlab.machine.Vessel.ALLOWED.get("hydrofluoric_acid").contains(com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE), "HF dissolves glass");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.ALLOWED.get("hydrofluoric_acid").contains(com.taku3.semiconductorlab.machine.Vessel.POLYETHYLENE_BOTTLE)
                && com.taku3.semiconductorlab.machine.Vessel.POLYETHYLENE_BOTTLE.empty() == Chemistry.POLYETHYLENE_BOTTLE && recipeUses(h, "polyethylene_bottle", Chemistry.POLYETHYLENE), "HF may go in a polyethylene bottle (S-006)");
        h.assertTrue(all.get(Chemistry.HYDROFLUORIC_ACID_PE) == com.taku3.semiconductorlab.machine.Vessel.POLYETHYLENE_BOTTLE
                && new ItemStack(Chemistry.HYDROFLUORIC_ACID_PE).is(Chemistry.HF_TAG) && new ItemStack(Chemistry.HYDROFLUORIC_ACID).is(Chemistry.HF_TAG)
                && recipeUses(h, "hydrofluoric_acid_pe", Chemistry.POLYETHYLENE_BOTTLE), "HF in a PE bottle is its own item; the tag holds both (O-014)");
        h.assertTrue(recipeUses(h, "cinnabar_retort_charge", Metallurgy.IRON_FLASK) && recipeUses(h, "sulfuric_acid", Chemistry.GLASS_BOTTLE)
                && recipeUses(h, "phosphorus", Chemistry.GLASS_BOTTLE), "filling recipes use the empty container");
        // 機械で使う：空の容器が機械の上に出る（袋は何も出ない）
        BlockPos pos = p(1, 1, 1);
        com.taku3.semiconductorlab.machine.Vessel.returnEmpty(h.getLevel(), h.absolutePos(pos), new ItemStack(Chemistry.PHOSPHORUS), 1);
        com.taku3.semiconductorlab.machine.Vessel.returnEmpty(h.getLevel(), h.absolutePos(pos), new ItemStack(Gas.HYDROGEN_GAS), 1);
        h.assertItemEntityPresent(Chemistry.GLASS_BOTTLE, pos.above(), 1.5);
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.emptyOf(new ItemStack(Gas.HYDROGEN_GAS)).isEmpty(), "bags are not returned");
        LOG.info("[LAB] vessels: {} filled items checked (remainder, stack size, compatibility table)", all.size());
        h.succeed();
    }

    /**
     * S-023：マルチブロックの形をウィキのために書き出す（docs/multiblocks.json、tools/wiki_export.py がまとめる）。下の層から、文字の絵と、文字ごとのブロックの ID・名前（lang）。
     * 新しいマルチブロックを作ったらここに1行足す
     */
    @GameTest
    public void exportMultiblocks(GameTestHelper h) {
        var all = new java.util.LinkedHashMap<String, com.taku3.semiconductorlab.machine.MultiblockPattern>();
        all.put("semiconductor-lab:blast_furnace", com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.shape());
        var root = new com.google.gson.JsonObject();
        root.addProperty("_note", "自動生成：GameTest exportMultiblocks（tools/test.ps1 で更新）。layers は下の層から、各層は奥から手前の行。' '＝空気、'?'＝何でも、controller＝制御ブロックの文字");
        var list = new com.google.gson.JsonArray();
        for (var e : all.entrySet()) {
            var p = e.getValue();
            var o = new com.google.gson.JsonObject();
            o.addProperty("controller_block", e.getKey());
            o.addProperty("controller", String.valueOf(p.controller()));
            o.addProperty("width", p.width()); o.addProperty("depth", p.depth()); o.addProperty("height", p.height());
            var layers = new com.google.gson.JsonArray();
            for (String[] layer : p.layers()) { var l = new com.google.gson.JsonArray(); for (String row : layer) l.add(row); layers.add(l); }
            o.add("layers", layers);
            var legend = new com.google.gson.JsonObject();
            for (String[] layer : p.layers()) for (String row : layer) for (char c : row.toCharArray()) {
                var cell = p.cell(c);
                if (cell == null || legend.has(String.valueOf(c))) continue;
                var cj = new com.google.gson.JsonObject();
                cj.addProperty("name", cell.name());
                cj.addProperty("lang", "multiblock.semiconductor-lab." + cell.name());
                if (cell.block() != null) cj.addProperty("block", cell.block());
                legend.add(String.valueOf(c), cj);
            }
            o.add("legend", legend);
            list.add(o);
        }
        root.add("multiblocks", list);
        java.nio.file.Path dir = java.nio.file.Paths.get("").toAbsolutePath();
        while (dir != null && !java.nio.file.Files.exists(dir.resolve("docs").resolve("TECH_TREE.md"))) dir = dir.getParent();
        h.assertTrue(dir != null, "project root not found");
        try { java.nio.file.Files.writeString(dir.resolve("docs").resolve("multiblocks.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n", java.nio.charset.StandardCharsets.UTF_8); }
        catch (java.io.IOException ex) { h.fail("write failed: " + ex.getMessage()); }
        h.assertTrue(list.size() >= 1 && list.get(0).getAsJsonObject().getAsJsonObject("legend").has("B"), "blast furnace shape with its legend");
        h.succeed();
    }
}
