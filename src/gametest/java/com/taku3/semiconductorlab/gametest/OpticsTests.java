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
 * GameTest：幹7 光学（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class OpticsTests extends LabTestBase {
    @GameTest
    public void oxyhydrogenFusedQuartz(GameTestHelper h) {
        int T = OxyhydrogenBurnerBlockEntity.MELT_TICKS;
        int IN = OxyhydrogenBurnerBlockEntity.SLOT_IN, H2 = OxyhydrogenBurnerBlockEntity.SLOT_H2, O2 = OxyhydrogenBurnerBlockEntity.SLOT_O2;
        int OUT = OxyhydrogenBurnerBlockEntity.SLOT_OUT, EMPTY = OxyhydrogenBurnerBlockEntity.SLOT_EMPTY;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Optics.OXYHYDROGEN_BURNER.block);
        OxyhydrogenBurnerBlockEntity be = h.getBlockEntity(pos, OxyhydrogenBurnerBlockEntity.class);
        var c = be.getContainer();

        // かまどではもう溶けない（仮レシピ #19 は削除）
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.VEIN_QUARTZ).isEmpty(), "quartz must not melt in a furnace (~1000 C < 1713 C)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.VEIN_QUARTZ).isEmpty(), "nor in a blast furnace");
        // タスク13：入力は珪石 [1-46]。ネザークォーツ（現実に無い素材）は受け付けない
        h.assertTrue(OxyhydrogenBurnerBlockEntity.isInput(new ItemStack(Metallurgy.VEIN_QUARTZ)), "the burner melts vein quartz");
        h.assertFalse(OxyhydrogenBurnerBlockEntity.isInput(new ItemStack(Items.QUARTZ)), "nether quartz is not accepted (chemistry rule 1)");

        // ボンベなし：動かない
        c.setItem(IN, new ItemStack(Metallurgy.VEIN_QUARTZ, 30));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no cylinders -> no melting");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 水素だけ：動かない（酸素がないと燃えない）。水素ボンベはつながない（無駄に開けない）
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_CYLINDER, 1));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "hydrogen only -> no flame");
        // 酸素だけ：動かない
        c.setItem(H2, ItemStack.EMPTY);
        c.setItem(O2, new ItemStack(Gas.OXYGEN_CYLINDER, 1));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "oxygen only -> no flame");

        // 両方：10秒で溶融石英1個、空ボンベ2本が出る
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_CYLINDER, 2));
        tickBurner(h, pos, be, T - 1);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickBurner(h, pos, be, 1);
        h.assertTrue(c.getItem(OUT).is(Optics.FUSED_QUARTZ), "quartz -> fused quartz");
        h.assertValueEqual(c.getItem(EMPTY).getCount(), 2, "one H2 and one O2 cylinder connected -> 2 empties");
        h.assertValueEqual(be.getH2(), OxyhydrogenBurnerBlockEntity.MOL_PER_CYLINDER - OxyhydrogenBurnerBlockEntity.H2_PER, "H2 46 mol left");
        h.assertValueEqual(be.getO2(), OxyhydrogenBurnerBlockEntity.MOL_PER_CYLINDER - OxyhydrogenBurnerBlockEntity.O2_PER, "O2 48 mol left");
        // 水素ボンベ1本＝12個まで（50/4）、13個目で2本目をつなぐ
        tickBurner(h, pos, be, T * 11);
        h.assertValueEqual(c.getItem(OUT).getCount(), 12, "12 fused quartz from the first H2 cylinder");
        h.assertValueEqual(c.getItem(H2).getCount(), 1, "2nd H2 cylinder still unopened");
        tickBurner(h, pos, be, T);
        h.assertValueEqual(c.getItem(OUT).getCount(), 13, "13th quartz");
        h.assertTrue(c.getItem(H2).isEmpty(), "2nd H2 cylinder connected");
        h.assertValueEqual(c.getItem(EMPTY).getCount(), 3, "3 empty cylinders");
        h.assertValueEqual(be.getO2(), OxyhydrogenBurnerBlockEntity.MOL_PER_CYLINDER - 13 * OxyhydrogenBurnerBlockEntity.O2_PER, "O2 used at 2 mol each (2H2 + O2)");
        // 溶融石英 → 石英管のルートは残る
        h.assertTrue(recipeUses(h, "quartz_tube", Optics.FUSED_QUARTZ), "fused quartz -> quartz tube");
        LOG.info("[LAB] oxyhydrogen burner: no gas -> 0, H2+O2 -> fused quartz in {} ticks, 12.5 per H2 cylinder", T);
        h.succeed();
    }

    /** [7-01] 光学ガラス（IMP2-01）：フリント＝砂5＋PbO1＋カリ1 →調合原料5 →溶鉱炉→ フリント。クラウン＝ソーダ石灰ガラス →溶鉱炉（かき混ぜ）→ クラウン */
    @GameTest
    public void opticalGlass(GameTestHelper h) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("flint_glass_batch"));
        var r = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var ings = r.placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Items.SAND))).count(), 5L, "5 sand (SiO2 ~49%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Metallurgy.LITHARGE))).count(), 1L, "1 litharge (PbO ~36%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Chemistry.POTASH))).count(), 1L, "1 potash (K2O ~15%, as Ravenscroft)");
        h.assertFalse(recipeUses(h, "flint_glass_batch", Chemistry.SODIUM_CARBONATE), "not soda: real lead glass used potash");
        h.assertValueEqual(ings.size(), 7, "nothing else in the batch");
        // 溶鉱炉（長く溶かしてかき混ぜる）でだけ光学ガラスになる。かまどでは泡と脈理が残る＝作れない
        var BL = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        var SM = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(cook(h, BL, Optics.FLINT_GLASS_BATCH).is(Optics.FLINT_GLASS), "flint batch -> blast furnace -> flint glass");
        h.assertTrue(cook(h, BL, Chemistry.SODA_LIME_GLASS).is(Optics.CROWN_GLASS), "soda-lime glass -> blast furnace (stirred) -> crown glass");
        h.assertTrue(cook(h, SM, Optics.FLINT_GLASS_BATCH).isEmpty(), "no optical flint from a plain furnace");
        h.assertTrue(cook(h, SM, Chemistry.SODA_LIME_GLASS).isEmpty(), "no optical crown from a plain furnace");
        // バニラのガラスは光学ガラスにならない（化学ルール1：近似のガラスから光学ガラスを作らない）
        h.assertTrue(cook(h, BL, Items.GLASS).isEmpty(), "vanilla glass is not optical glass");
        for (String id : new String[] {"flint_glass", "crown_glass"}) {
            var rk = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(id));
            h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(rk).isPresent(), "recipe " + id);
        }
        LOG.info("[LAB] optical glass: 5 sand + PbO + potash -> 5 flint batch -> blast -> flint; soda-lime glass -> blast -> crown");
        h.succeed();
    }

    /** 作業台で並べたときの {出力, 戻る物} */
    private static ItemStack[] craft(GameTestHelper h, net.minecraft.world.item.Item... items) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) list.add(i < items.length && items[i] != null ? new ItemStack(items[i]) : ItemStack.EMPTY);
        var input = net.minecraft.world.item.crafting.CraftingInput.ofPositioned(3, 3, list).input();   // 空きを詰める（作業台と同じ）
        var r = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, h.getLevel());
        if (r.isEmpty()) return new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};
        ItemStack back = r.get().value().getRemainingItems(input).stream().filter(s -> !s.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
        return new ItemStack[] {r.get().value().assemble(input), back};
    }

    /** [7-03] レンズ研磨（IMP2-01）：光学ガラス＋砂＋研削皿 → すりガラスの素材、素材＋ベンガラ＋ピッチの研磨皿 → レンズ。道具は戻る */
    @GameTest
    public void lensGrinding(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.IRON_NUGGET).is(Optics.ROUGE), "iron roasted in air -> rouge Fe2O3");
        h.assertTrue(craft(h, Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT)[0].is(Optics.GRINDING_TOOL), "3 iron -> cast iron grinding tool");
        h.assertTrue(craft(h, Chemistry.PITCH, Chemistry.PITCH, Chemistry.PITCH, Items.IRON_INGOT)[0].is(Optics.PITCH_LAP), "pitch on an iron dish -> pitch lap");
        net.minecraft.world.item.Item[][] g = {{Optics.CROWN_GLASS, Optics.CROWN_LENS_BLANK, Optics.CROWN_LENS}, {Optics.FLINT_GLASS, Optics.FLINT_LENS_BLANK, Optics.FLINT_LENS}};
        for (var row : g) {
            var ground = craft(h, row[0], Items.SAND, Optics.GRINDING_TOOL);
            h.assertTrue(ground[0].is(row[1]), row[0] + " + sand + grinding tool -> ground blank");
            h.assertTrue(ground[1].is(Optics.GRINDING_TOOL), "grinding tool comes back");
            var pol = craft(h, row[1], Optics.ROUGE, Optics.PITCH_LAP);
            h.assertTrue(pol[0].is(row[2]), row[1] + " + rouge + pitch lap -> lens");
            h.assertTrue(pol[1].is(Optics.PITCH_LAP), "pitch lap comes back");
            // 段を飛ばせない：研削せずに磨けない、磨き粉なし・皿なしでは磨けない
            h.assertTrue(craft(h, row[0], Optics.ROUGE, Optics.PITCH_LAP)[0].isEmpty(), "unground glass cannot be polished");
            h.assertTrue(craft(h, row[1], Items.SAND, Optics.PITCH_LAP)[0].isEmpty(), "sand does not polish (scratches)");
            h.assertTrue(craft(h, row[1], Optics.ROUGE)[0].isEmpty(), "no lap -> no polish");
        }
        // バニラのガラス・ソーダ石灰ガラスからはレンズにしない（光学ガラス [7-01] だけ）
        h.assertTrue(craft(h, Items.GLASS, Items.SAND, Optics.GRINDING_TOOL)[0].isEmpty(), "vanilla glass is not lens glass");
        h.assertTrue(craft(h, Chemistry.SODA_LIME_GLASS, Items.SAND, Optics.GRINDING_TOOL)[0].isEmpty(), "soda-lime glass must be remelted to crown first");
        LOG.info("[LAB] lens grinding [7-03]: glass + sand + grinding tool -> blank; blank + rouge + pitch lap -> crown/flint lens; tools come back");
        h.succeed();
    }

    /** [7-04] 色消しレンズ・屈折望遠鏡（IMP2-01）：クラウン凸＋フリント凹＋銅 → 色消し、色消し＋銅＋クラウン → 望遠鏡。使っている間はのぞいている（isScoping） */
    @GameTest
    public void telescope(GameTestHelper h) {
        h.assertTrue(craft(h, Optics.CROWN_LENS, Optics.FLINT_LENS, Items.COPPER_INGOT)[0].is(Optics.ACHROMATIC_LENS), "crown + flint + cell -> achromatic lens");
        h.assertTrue(craft(h, Optics.CROWN_LENS, Optics.CROWN_LENS, Items.COPPER_INGOT)[0].isEmpty(), "two crown lenses are not achromatic");
        // 縦に 対物（色消し）・筒（銅）・接眼（クラウン）
        var top = craft(h, Optics.ACHROMATIC_LENS, null, null, Items.COPPER_INGOT, null, null, Optics.CROWN_LENS);
        h.assertTrue(top[0].is(Optics.REFRACTING_TELESCOPE), "achromat over copper tube over crown eyepiece -> telescope");
        h.assertTrue(craft(h, Optics.CROWN_LENS, null, null, Items.COPPER_INGOT, null, null, Optics.CROWN_LENS)[0].isEmpty(), "no achromat -> no telescope");

        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Optics.REFRACTING_TELESCOPE));
        h.assertFalse(player.isScoping(), "not scoping before use");
        player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(player.isUsingItem(), "telescope can be used (held)");
        h.assertTrue(player.isScoping(), "using the telescope = scoping (zoom, scope overlay)");
        player.stopUsingItem();
        h.assertFalse(player.isScoping(), "released -> not scoping");
        // バニラの望遠鏡は今までどおり
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.SPYGLASS));
        player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(player.isScoping(), "vanilla spyglass still scopes");
        LOG.info("[LAB] telescope [7-04]: crown + flint -> achromat; achromat + tube + eyepiece -> telescope; using it scopes like a spyglass");
        h.succeed();
    }

    /** [7-06] プリズム分光（IMP2-01）：プリズム・分光器のレシピ、炎の判定、試料ごとの元素の線、たき火に使うと反応・石には反応しない */
    @GameTest
    public void spectroscope(GameTestHelper h) {
        var SP = com.taku3.semiconductorlab.item.SpectroscopeItem.class;
        var prism = craft(h, Optics.FLINT_GLASS, Items.SAND, Optics.GRINDING_TOOL, Optics.ROUGE, Optics.PITCH_LAP);
        h.assertTrue(prism[0].is(Optics.PRISM), "flint glass ground and polished -> prism");
        h.assertTrue(craft(h, Optics.CROWN_GLASS, Items.SAND, Optics.GRINDING_TOOL, Optics.ROUGE, Optics.PITCH_LAP)[0].isEmpty(), "prism is flint (high dispersion), not crown");
        h.assertTrue(craft(h, Optics.CROWN_LENS, Items.COPPER_INGOT, Optics.CROWN_LENS, Items.COPPER_INGOT, Optics.PRISM, Items.COPPER_INGOT)[0].is(Optics.SPECTROSCOPE),
                "slit/collimator lens + prism + telescope lens -> spectroscope");

        // 炎の判定：火のついたたき火・火だけ
        BlockState camp = Blocks.CAMPFIRE.defaultBlockState();
        h.assertTrue(com.taku3.semiconductorlab.item.SpectroscopeItem.isFlame(camp.setValue(BlockStateProperties.LIT, true)), "lit campfire is a flame");
        h.assertFalse(com.taku3.semiconductorlab.item.SpectroscopeItem.isFlame(camp.setValue(BlockStateProperties.LIT, false)), "unlit campfire is not");
        h.assertTrue(com.taku3.semiconductorlab.item.SpectroscopeItem.isFlame(Blocks.FIRE.defaultBlockState()), "fire is a flame");
        h.assertFalse(com.taku3.semiconductorlab.item.SpectroscopeItem.isFlame(Blocks.STONE.defaultBlockState()), "stone is not");

        // 元素の線（炎色反応）
        java.util.function.Function<net.minecraft.world.item.Item, List<String>> el = it -> com.taku3.semiconductorlab.item.SpectroscopeItem.elementsOf(new ItemStack(it));
        h.assertValueEqual(el.apply(com.taku3.semiconductorlab.content.Gas.SALT), List.of("na"), "NaCl -> Na D lines");
        h.assertValueEqual(el.apply(Chemistry.POTASH), List.of("k"), "K2CO3 -> K");
        h.assertTrue(el.apply(Chemistry.WOOD_ASH).containsAll(List.of("k", "na", "ca")), "wood ash -> K, Ca and some Na");
        h.assertValueEqual(el.apply(Survival.SLAKED_LIME), List.of("ca"), "Ca(OH)2 -> Ca");
        h.assertValueEqual(el.apply(Metallurgy.BORIC_ACID), List.of("b"), "H3BO3 -> green B");
        h.assertValueEqual(el.apply(Metallurgy.BORAX), List.of("b", "na"), "borax Na2B4O7 -> B + Na");
        h.assertValueEqual(el.apply(Metallurgy.LITHARGE), List.of("pb"), "PbO -> Pb");
        h.assertValueEqual(el.apply(Metallurgy.GERMANIUM_DIOXIDE), List.of("ge"), "GeO2 -> Ge (UV, invisible)");
        h.assertTrue(el.apply(Items.STONE).isEmpty(), "stone: no lines");
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpectroscopeItem.flameReport(ItemStack.EMPTY), List.of("flame_only", "hold_sample"), "no sample -> continuous spectrum");
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpectroscopeItem.flameReport(new ItemStack(Items.STONE)), List.of("none"), "non-emitting sample");
        // lang に全部の行がある
        var ja = langJson("ja_jp");
        for (String k : new String[] {"flame", "flame_with", "flame_only", "hold_sample", "none", "sun", "sun_lines", "no_sun", "line.na", "line.k", "line.ca", "line.b", "line.pb", "line.ge"})
            h.assertTrue(ja.has("item.semiconductor-lab.spectroscope." + k), "lang spectroscope." + k);

        // 実際に使う：火のついたたき火には反応（SUCCESS）、石には反応しない（PASS → 空を見る）
        BlockPos fire = p(1, 1, 1), stone = p(3, 1, 1);
        h.setBlock(fire, camp.setValue(BlockStateProperties.LIT, true));
        h.setBlock(stone, Blocks.STONE);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack scope = new ItemStack(Optics.SPECTROSCOPE);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, scope);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(com.taku3.semiconductorlab.content.Gas.SALT));
        for (BlockPos rel : new BlockPos[] {fire, stone}) {
            BlockPos a = h.absolutePos(rel);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(a), net.minecraft.core.Direction.UP, a, false);
            var r = scope.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND, scope, hit));
            if (rel == fire) h.assertTrue(r.consumesAction(), "spectroscope on a lit campfire -> reads the flame");
            else h.assertFalse(r.consumesAction(), "spectroscope on stone -> passes to looking at the sky");
        }
        LOG.info("[LAB] spectroscope [7-06]: flint prism + 2 crown lenses + copper; flame test Na/K/Ca/B/Pb (Ge in UV); sun -> Fraunhofer lines");
        h.succeed();
    }

    /** [7-05] 反射鏡 段階1 スペキュラム（IMP2-01）：銅2＋スズ1 → 調合3 →かまど→ 合金 → 研削・研磨で鏡 → ニュートン式反射望遠鏡（のぞける） */
    @GameTest
    public void speculumMirror(GameTestHelper h) {
        h.assertTrue(craft(h, Items.COPPER_INGOT, Items.COPPER_INGOT, Metallurgy.TIN_INGOT)[0].is(Optics.SPECULUM_CHARGE), "2 Cu + 1 Sn -> speculum charge");
        h.assertValueEqual(craft(h, Items.COPPER_INGOT, Items.COPPER_INGOT, Metallurgy.TIN_INGOT)[0].getCount(), 3, "3 charges");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Optics.SPECULUM_CHARGE).is(Optics.SPECULUM_INGOT), "melt -> speculum metal");
        var m = craft(h, Optics.SPECULUM_INGOT, Items.SAND, Optics.GRINDING_TOOL, Optics.ROUGE, Optics.PITCH_LAP);
        h.assertTrue(m[0].is(Optics.SPECULUM_MIRROR), "ground and polished -> mirror");
        h.assertTrue(craft(h, Metallurgy.BRONZE_INGOT, Items.SAND, Optics.GRINDING_TOOL, Optics.ROUGE, Optics.PITCH_LAP)[0].isEmpty(), "ordinary bronze (11% Sn) is not mirror metal");
        h.assertTrue(craft(h, Items.COPPER_INGOT, Optics.CROWN_LENS, null, Items.COPPER_INGOT, null, null, Optics.SPECULUM_MIRROR)[0].is(Optics.REFLECTING_TELESCOPE),
                "mirror at the bottom, tube, eyepiece at the side -> Newtonian telescope");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Optics.REFLECTING_TELESCOPE));
        player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(player.isScoping(), "reflecting telescope scopes");
        LOG.info("[LAB] speculum [7-05]: 2 Cu + Sn -> speculum metal -> polished mirror -> Newtonian telescope (scopes)");
        h.succeed();
    }

    /** [7-07] 回折格子（IMP2-01）：送りねじ2＋銅 → 針金格子（d = 101.6μm）、分光器＋格子 → 格子分光器。sinθ = λ/d で角度から波長 */
    @GameTest
    public void grating(GameTestHelper h) {
        h.assertTrue(craft(h, com.taku3.semiconductorlab.content.Precision.LEAD_SCREW, com.taku3.semiconductorlab.content.Precision.LEAD_SCREW, Items.COPPER_INGOT)[0].is(Optics.WIRE_GRATING),
                "copper wire wound on two screws -> wire grating");
        h.assertTrue(craft(h, Optics.SPECTROSCOPE, Optics.WIRE_GRATING)[0].is(Optics.GRATING_SPECTROSCOPE), "spectroscope + grating -> grating spectroscope");
        var g = (com.taku3.semiconductorlab.item.SpectroscopeItem) Optics.GRATING_SPECTROSCOPE;
        var prism = (com.taku3.semiconductorlab.item.SpectroscopeItem) Optics.SPECTROSCOPE;
        h.assertTrue(g.gratingMicrons() == 101.6 && prism.gratingMicrons() == 0, "grating d = 0.004 in = 101.6 um; prism has none");
        // Na D 589.0nm：sinθ = 0.5890/101.6 → θ ≈ 0.332°（約 19.9′）。角度から戻すと 589.0nm
        double th = com.taku3.semiconductorlab.item.SpectroscopeItem.angleDeg(589.0, 101.6);
        h.assertTrue(Math.abs(th - 0.3322) < 0.0005, "Na D first-order angle ~0.332 deg, got " + th);
        h.assertTrue(Math.abs(101.6e3 * Math.sin(Math.toRadians(th)) - 589.0) < 1e-6, "lambda = d sin(theta) gives back 589.0 nm");
        h.assertTrue(com.taku3.semiconductorlab.item.SpectroscopeItem.angleDeg(656.3, 101.6) > th, "red (C, 656 nm) bends more than yellow (opposite to a prism)");
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpectroscopeItem.measurableNm("ge").length, 0, "Ge line is UV -> no angle shown");
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpectroscopeItem.measurableNm("sun").length, 6, "C, D, b1, F, H, K");
        h.assertTrue(langJson("ja_jp").has("item.semiconductor-lab.grating_spectroscope.measure"), "lang measure line");
        LOG.info("[LAB] grating [7-07]: wire grating d=101.6 um; Na D at {} deg; spectroscope + grating -> measures wavelengths", String.format("%.4f", th));
        h.succeed();
    }

    /** [7-10] 水銀灯（IMP2-01）：組立品 →封止台（0.1Pa 以下だけ）→ 管 → ランプ。直流 385W で点灯（LIT）、電気なしで消える。分光器で Hg の線 */
    @GameTest
    public void mercuryLamp(GameTestHelper h) {
        h.assertTrue(craft(h, Optics.QUARTZ_TUBE, Metallurgy.MERCURY, Metallurgy.TUNGSTEN_WIRE, Metallurgy.TUNGSTEN_WIRE)[0].is(Optics.MERCURY_LAMP_ASSEMBLY),
                "quartz tube + mercury + 2 W electrodes -> lamp assembly");
        h.assertTrue(craft(h, Chemistry.SODA_LIME_GLASS, Metallurgy.MERCURY, Metallurgy.TUNGSTEN_WIRE, Metallurgy.TUNGSTEN_WIRE)[0].isEmpty(), "needs a quartz tube (UV)");
        // 封止台：大気圧・1kPa では封じない、0.08Pa で管になる
        var SS = com.taku3.semiconductorlab.block.SealingStationBlockEntity.class;
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), sp = p(2, 1, 1);
        h.setBlock(cp, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(sp, com.taku3.semiconductorlab.content.Vacuum.SEALING_STATION.block);
        var c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        var st = h.getBlockEntity(sp, SS);
        int IN = com.taku3.semiconductorlab.block.SealingStationBlockEntity.SLOT_IN, OUT = com.taku3.semiconductorlab.block.SealingStationBlockEntity.SLOT_OUT;
        int T = com.taku3.semiconductorlab.block.SealingStationBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SealingStationBlockEntity.WATTS);
        st.getContainer().setItem(IN, new ItemStack(Optics.MERCURY_LAMP_ASSEMBLY));
        c.setPressure(1000);
        tickConsumer(h, sp, st, T + 20, feed);
        h.assertTrue(st.getContainer().getItem(OUT).isEmpty(), "1 kPa: air left -> not sealed");
        c.setPressure(0.08);
        tickConsumer(h, sp, st, T, feed);
        h.assertTrue(st.getContainer().getItem(OUT).is(Optics.MERCURY_LAMP_TUBE), "0.08 Pa -> sealed mercury lamp tube");
        h.assertTrue(com.taku3.semiconductorlab.block.SealingStationBlockEntity.sealedProduct(new ItemStack(com.taku3.semiconductorlab.content.Vacuum.VACUUM_TUBE_ASSEMBLY))
                .is(com.taku3.semiconductorlab.content.Vacuum.VACUUM_TUBE), "vacuum tubes still seal as before");
        h.assertTrue(craft(h, Optics.MERCURY_LAMP_TUBE, Items.IRON_INGOT, Items.IRON_INGOT, com.taku3.semiconductorlab.content.Power.COPPER_CABLE.block.asItem())[0].is(Optics.MERCURY_LAMP.block.asItem()),
                "tube + iron + cable -> lamp block");

        // 点灯：直流 385W があれば LIT（明るさ15）、なければ消える
        BlockPos lp = p(4, 1, 4);
        h.setBlock(lp, Optics.MERCURY_LAMP.block);
        var lamp = h.getBlockEntity(lp, com.taku3.semiconductorlab.block.MercuryLampBlockEntity.class);
        tickConsumer(h, lp, lamp, 5, 0);
        h.assertBlockProperty(lp, BlockStateProperties.LIT, false);
        tickConsumer(h, lp, lamp, 5, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.MercuryLampBlockEntity.WATTS));
        h.assertBlockProperty(lp, BlockStateProperties.LIT, true);
        h.assertValueEqual(h.getBlockState(lp).getLightEmission(), 15, "lit lamp light level 15");
        h.assertTrue(Math.abs(lamp.getLastWatts() - 385) < 1e-6, "385 W (110 V x 3.5 A)");
        // 分光器を点灯中のランプに：反応する（SUCCESS）
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack scope = new ItemStack(Optics.GRATING_SPECTROSCOPE);
        BlockPos a = h.absolutePos(lp);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(a), net.minecraft.core.Direction.UP, a, false);
        h.assertTrue(scope.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND, scope, hit)).consumesAction(),
                "spectroscope reads the lit lamp");
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpectroscopeItem.measurableNm("hg").length, 5, "5 visible Hg lines (i-line is UV)");
        // 電気が切れたら消える
        tickConsumer(h, lp, lamp, 200, 0);
        h.assertBlockProperty(lp, BlockStateProperties.LIT, false);
        LOG.info("[LAB] mercury lamp [7-10]: assembly sealed at 0.08 Pa -> tube -> lamp; DC 385 W -> lit (15); spectroscope shows Hg g/h lines");
        h.succeed();
    }

    /** [7-09] マイケルソン干渉計（IMP2-01）：半透明鏡＋鏡2＋送りねじ＋鉄2。水銀の緑 546.1nm、ねじ1回転 0.5mm で縞 1831 本 → 0.5000mm。炎では縞が消える */
    @GameTest
    public void interferometer(GameTestHelper h) {
        h.assertTrue(craft(h, Optics.HALF_SILVERED_MIRROR, Optics.SPECULUM_MIRROR, Optics.SPECULUM_MIRROR, com.taku3.semiconductorlab.content.Precision.LEAD_SCREW, Items.IRON_INGOT, Items.IRON_INGOT)[0].is(Optics.INTERFEROMETER),
                "beam splitter + 2 mirrors + lead screw + base -> interferometer");
        h.assertTrue(craft(h, Optics.CROWN_GLASS, Optics.SPECULUM_MIRROR, Optics.SPECULUM_MIRROR, com.taku3.semiconductorlab.content.Precision.LEAD_SCREW, Items.IRON_INGOT, Items.IRON_INGOT)[0].isEmpty(),
                "plain glass does not split the beam");
        double n = com.taku3.semiconductorlab.item.InterferometerItem.fringes(0.5, 546.1);
        h.assertTrue(Math.abs(n - 1831.17) < 0.01, "2 x 0.5 mm / 546.1 nm = 1831.2 fringes, got " + n);
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.InterferometerItem.lengthMm(1831, 546.1) - 0.49996) < 1e-4, "1831 fringes x 546.1/2 nm = 0.5000 mm");
        // 点灯中の水銀灯：反応、炎：反応（縞が消えると出る）、石：PASS
        BlockPos lp = p(1, 1, 1), fp = p(3, 1, 1), sp = p(5, 1, 1);
        h.setBlock(lp, Optics.MERCURY_LAMP.block);
        var lamp = h.getBlockEntity(lp, com.taku3.semiconductorlab.block.MercuryLampBlockEntity.class);
        tickConsumer(h, lp, lamp, 5, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.MercuryLampBlockEntity.WATTS));
        h.setBlock(fp, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, true));
        h.setBlock(sp, Blocks.STONE);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack it = new ItemStack(Optics.INTERFEROMETER);
        boolean[] want = {true, true, false};
        BlockPos[] at = {lp, fp, sp};
        for (int i = 0; i < 3; i++) {
            BlockPos a2 = h.absolutePos(at[i]);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(a2), net.minecraft.core.Direction.UP, a2, false);
            boolean r = it.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND, it, hit)).consumesAction();
            h.assertValueEqual(r, want[i], "use on " + at[i]);
        }
        var ja = langJson("ja_jp");
        for (String k : new String[] {"lamp", "count", "length", "white", "hint"}) h.assertTrue(ja.has("item.semiconductor-lab.interferometer." + k), "lang " + k);
        LOG.info("[LAB] interferometer [7-09]: Hg green 546.1 nm, screw 0.5 mm -> 1831 fringes -> 0.5000 mm; flame -> fringes wash out");
        h.succeed();
    }

    /** [7-22] 反射防止膜（IMP2-01）：蒸着装置でレンズ＋蛍石 CaF2 → 反射防止膜付きレンズ。1/4 波長の膜で反射 約4.3% → 約2.2%（式で確かめる） */
    @GameTest
    public void antiReflectionCoating(GameTestHelper h) {
        var E = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.class;
        var r1 = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.recipe(new ItemStack(Optics.CROWN_LENS), new ItemStack(Metallurgy.FLUORITE));
        var r2 = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.recipe(new ItemStack(Optics.FLINT_LENS), new ItemStack(Metallurgy.FLUORITE));
        h.assertTrue(r1 != null && r1.product() == Optics.COATED_CROWN_LENS, "crown lens + CaF2 -> coated crown lens");
        h.assertTrue(r2 != null && r2.product() == Optics.COATED_FLINT_LENS, "flint lens + CaF2 -> coated flint lens");
        h.assertTrue(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.recipe(new ItemStack(Optics.CROWN_GLASS), new ItemStack(Metallurgy.SILVER_NUGGET)).product() == Optics.HALF_SILVERED_MIRROR, "half mirror still");
        // 物理の確かめ：ガラス n=1.52 の裸の面 R0、CaF2 n=1.43 を 1/4 波長付けた面 R1（空気 n=1）
        double ng = 1.52, nf = 1.43;
        double r0 = Math.pow((ng - 1) / (ng + 1), 2), rq = Math.pow((ng - nf * nf) / (ng + nf * nf), 2);
        h.assertTrue(Math.abs(r0 - 0.0426) < 0.001 && Math.abs(rq - 0.0217) < 0.001, "R: bare " + r0 + " -> coated " + rq);
        // 機械で：0.004Pa でコートができる
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), ep = p(2, 1, 1);
        h.setBlock(cp, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(ep, com.taku3.semiconductorlab.content.Vacuum.EVAPORATOR.block);
        var c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        var e = h.getBlockEntity(ep, E);
        e.getContainer().setItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_SUBSTRATE, new ItemStack(Optics.CROWN_LENS));
        e.getContainer().setItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_METAL, new ItemStack(Metallurgy.FLUORITE));
        c.setPressure(0.004);
        tickConsumer(h, ep, e, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS));
        h.assertTrue(e.getContainer().getItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_OUT).is(Optics.COATED_CROWN_LENS), "evaporator coats the lens");
        LOG.info("[LAB] anti-reflection [7-22]: lens + CaF2 (quarter wave) -> coated lens; R {} -> {}", String.format("%.3f", r0), String.format("%.3f", rq));
        h.succeed();
    }

    /** [7-15] 紫外光学材料・CaF2（IMP2-02）：ブリッジマン炉で蛍石4＋アルゴン → CaF2 単結晶（アルゴンなし・3個・電力半分では不可）→ 削って磨いて CaF2 レンズ */
    @GameTest
    public void bridgmanCaF2(GameTestHelper h) {
        var B = com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.class;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Optics.BRIDGMAN_FURNACE.block);
        var be = h.getBlockEntity(pos, B);
        var c = be.getContainer();
        int IN = com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.SLOT_IN, AR = com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.SLOT_AR, OUT = com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.SLOT_OUT;
        int T = com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.TICKS;
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.WATTS);
        c.setItem(IN, new ItemStack(Metallurgy.FLUORITE, 4));
        tickConsumer(h, pos, be, T + 5, full);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no argon -> oxygen would cloud the crystal -> wait");
        c.setItem(AR, new ItemStack(com.taku3.semiconductorlab.content.Gas.ARGON_GAS, 1));
        c.setItem(IN, new ItemStack(Metallurgy.FLUORITE, 3));
        tickConsumer(h, pos, be, T + 5, full);
        h.assertTrue(c.getItem(OUT).isEmpty(), "3 fluorite is not enough");
        c.setItem(IN, new ItemStack(Metallurgy.FLUORITE, 4));
        tickConsumer(h, pos, be, T, full / 2);
        h.assertTrue(c.getItem(OUT).isEmpty(), "half power -> no progress");
        tickConsumer(h, pos, be, T, full);
        h.assertTrue(c.getItem(OUT).is(Optics.CAF2_CRYSTAL), "4 fluorite + Ar -> CaF2 single crystal");
        h.assertTrue(c.getItem(AR).isEmpty() && c.getItem(IN).isEmpty(), "argon and fluorite used");
        h.assertTrue(craft(h, Optics.CAF2_CRYSTAL, Items.SAND, Optics.GRINDING_TOOL, Optics.ROUGE, Optics.PITCH_LAP)[0].is(Optics.CAF2_LENS), "ground and polished -> CaF2 lens");
        h.assertTrue(craft(h, Metallurgy.FLUORITE, Items.SAND, Optics.GRINDING_TOOL, Optics.ROUGE, Optics.PITCH_LAP)[0].isEmpty(), "raw fluorite (cloudy, cleaved) is not a lens");
        LOG.info("[LAB] CaF2 [7-15]: Bridgman furnace 4 fluorite + Ar -> CaF2 crystal (3 kW x 90 s) -> CaF2 lens");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Optics.BRIDGMAN_FURNACE.block, "CaF2 の単結晶を育てる", L(io(Metallurgy.FLUORITE, 4), io(com.taku3.semiconductorlab.content.Gas.ARGON_GAS, 1)),
            L(io(Optics.CAF2_CRYSTAL, 1)), com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.BridgmanFurnaceBlockEntity.TICKS / 20.0, "アルゴンの中で溶かし（1418℃）、ゆっくり下げて下から結晶化");
        rec(a, Optics.OXYHYDROGEN_BURNER.block, "石英を溶かす", L(io(Metallurgy.VEIN_QUARTZ, 1), mol(Gas.HYDROGEN_CYLINDER, OxyhydrogenBurnerBlockEntity.H2_PER), mol(Gas.OXYGEN_CYLINDER, OxyhydrogenBurnerBlockEntity.O2_PER)),
            L(io(Optics.FUSED_QUARTZ, 1)), 0, OxyhydrogenBurnerBlockEntity.MELT_TICKS / 20.0, "2H2 + O2 → 2H2O（約2800℃）。電気なし");
    }
}
