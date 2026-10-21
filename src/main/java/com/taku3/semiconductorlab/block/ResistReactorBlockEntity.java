package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

/**
 * 化学増幅レジストの合成釜 [2-33]（1982 IBM の化学増幅レジスト。IMP4-01、O-016）。入れた物の組み合わせで5つの反応を切りかえる（表 {@link #RECIPES}）。
 * 樹脂（保護したポリヒドロキシスチレン）：
 *  ① フリース転位：フェノール＋無水酢酸＋塩化アルミニウム → 4-ヒドロキシアセトフェノン＋酢酸
 *  ② 還元と脱水：4-ヒドロキシアセトフェノン＋水素（ラネーニッケルの触媒、減らない）→ 4-ヒドロキシスチレン（水は捨てる）
 *  ③ 重合と保護：4-ヒドロキシスチレン 4 → 保護したポリヒドロキシスチレン（⚠️ 保護基 t-BOC の試薬は省略）
 * 光酸発生剤（トリフェニルスルホニウム ヘキサフルオロアンチモン酸塩、クリベロのオニウム塩）：
 *  ④ ベンゼン 2＋二酸化硫黄＋塩化アルミニウム → ジフェニルスルホキシド（⚠️ 本物は塩化チオニル）
 *  ⑤ ジフェニルスルホキシド＋ベンゼン＋アンチモン＋フッ化カリウム＋フッ酸 2 → 光酸発生剤（⚠️ SbF6⁻ の作り方は近似）。フッ酸の容器は上に戻る
 * ⚠️ 電力・時間・量はゲームの値（resist_reactor.placeholder）。
 */
public class ResistReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_500;
    public static final int TICKS = 20 * 15;
    public static final int SLOT_IN0 = 0, SLOT_IN3 = 3, SLOT_CAT = 4, SLOT_OUT = 5, SLOT_OUT2 = 6, SLOT_COUNT = 7;

    /** 材料：アイテムかタグ、と個数 */
    public record Ing(Predicate<ItemStack> test, int count, Item shown) {
        static Ing of(Item i, int n) { return new Ing(s -> s.is(i), n, i); }
        static Ing tag(TagKey<Item> t, int n, Item shown) { return new Ing(s -> s.is(t), n, shown); }
    }
    /** 反応1つ：材料（4つまで）・触媒（減らない、なくてよい）・出る物2つまで・画面の文の鍵の末尾 */
    public record Recipe(String key, List<Ing> in, Item catalyst, Item out, int outCount, Item out2, int out2Count) {}

    public static final List<Recipe> RECIPES = List.of(
            new Recipe("fries", List.of(Ing.of(Chemistry.PHENOL, 1), Ing.of(Chemistry.ACETIC_ANHYDRIDE, 1), Ing.of(Chemistry.ALUMINIUM_CHLORIDE, 1)), null,
                    Chemistry.HYDROXYACETOPHENONE, 1, Chemistry.ACETIC_ACID, 1),
            new Recipe("reduce", List.of(Ing.of(Chemistry.HYDROXYACETOPHENONE, 1), Ing.of(Gas.HYDROGEN_GAS, 1)), Chemistry.RANEY_NICKEL,
                    Chemistry.HYDROXYSTYRENE, 1, null, 0),
            new Recipe("polymer", List.of(Ing.of(Chemistry.HYDROXYSTYRENE, 4)), null, Chemistry.PROTECTED_PHS, 1, null, 0),
            new Recipe("sulfoxide", List.of(Ing.of(Chemistry.BENZENE, 2), Ing.of(Chemistry.SULFUR_DIOXIDE, 1), Ing.of(Chemistry.ALUMINIUM_CHLORIDE, 1)), null,
                    Chemistry.DIPHENYL_SULFOXIDE, 1, null, 0),
            new Recipe("pag", List.of(Ing.of(Chemistry.DIPHENYL_SULFOXIDE, 1), Ing.of(Chemistry.BENZENE, 1), Ing.of(Metallurgy.ANTIMONY, 1), Ing.tag(Chemistry.HF_TAG, 2, Chemistry.HYDROFLUORIC_ACID_PE)),
                    Chemistry.POTASSIUM_FLUORIDE, Chemistry.PHOTOACID_GENERATOR, 1, null, 0));

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(0, 8, 17, ResistReactorBlockEntity::isIngredient).in(1, 26, 17, ResistReactorBlockEntity::isIngredient)
            .in(2, 8, 35, ResistReactorBlockEntity::isIngredient).in(3, 26, 35, ResistReactorBlockEntity::isIngredient)
            .in(SLOT_CAT, 8, 53, s -> s.is(Chemistry.RANEY_NICKEL) || s.is(Chemistry.POTASSIUM_FLUORIDE))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_OUT2, 78, 46)
            .right("power", 17, 1, "gui.semiconductor-lab.resist_reactor.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.resist_reactor.need_input", "gui.semiconductor-lab.resist_reactor.need_catalyst", "gui.semiconductor-lab.resist_reactor.need_power",
                    "gui.semiconductor-lab.resist_reactor.fries", "gui.semiconductor-lab.resist_reactor.reduce", "gui.semiconductor-lab.resist_reactor.polymer",
                    "gui.semiconductor-lab.resist_reactor.sulfoxide", "gui.semiconductor-lab.resist_reactor.pag")
            .bottom("recipe", "gui.semiconductor-lab.resist_reactor.recipe")
            .build();

    public ResistReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.RESIST_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isIngredient(ItemStack s) {
        for (Recipe r : RECIPES) for (Ing i : r.in()) if (i.test().test(s)) return true;
        return false;
    }

    /** 4つの入口にある物で作れる反応（材料がそろっていなければ null）。slotsOut[k]＝材料 k を取る入口 */
    public static Recipe match(ItemStack[] in, int[] slotsOut) {
        for (Recipe r : RECIPES) {
            boolean[] used = new boolean[in.length];
            boolean ok = true;
            for (int k = 0; k < r.in().size() && ok; k++) {
                Ing ing = r.in().get(k);
                int found = -1;
                for (int j = 0; j < in.length; j++) if (!used[j] && ing.test().test(in[j]) && in[j].getCount() >= ing.count()) { found = j; break; }
                if (found < 0) ok = false; else { used[found] = true; if (slotsOut != null) slotsOut[k] = found; }
            }
            if (!ok) continue;
            int filled = 0; for (ItemStack s : in) if (!s.isEmpty()) filled++;
            if (filled == r.in().size()) return r;   // よけいな物が入っていたら動かない（混ぜ物）
        }
        return null;
    }

    private ItemStack[] inputs() {
        ItemStack[] a = new ItemStack[4];
        for (int i = 0; i < 4; i++) a[i] = container.getItem(SLOT_IN0 + i);
        return a;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        Recipe r = match(inputs(), null);
        if (r == null) return false;
        if (r.catalyst() != null && !container.getItem(SLOT_CAT).is(r.catalyst())) return false;
        return canMerge(container.getItem(SLOT_OUT), new ItemStack(r.out(), r.outCount()))
                && (r.out2() == null || canMerge(container.getItem(SLOT_OUT2), new ItemStack(r.out2(), r.out2Count())));
    }

    @Override protected void finishWork() {
        int[] at = new int[4];
        Recipe r = match(inputs(), at);
        if (r == null) return;
        for (int k = 0; k < r.in().size(); k++) {
            ItemStack s = container.getItem(SLOT_IN0 + at[k]);
            Vessel.returnEmpty(level, worldPosition, s, r.in().get(k).count());   // フッ酸の容器などは上に戻る（容器のない物は何もしない）
            s.shrink(r.in().get(k).count());
        }
        if (r.key().equals("pag")) container.getItem(SLOT_CAT).shrink(1);   // ⑤ のフッ化カリウムは材料（触媒の置き場に入れる）
        merge(SLOT_OUT, new ItemStack(r.out(), r.outCount()));
        if (r.out2() != null) merge(SLOT_OUT2, new ItemStack(r.out2(), r.out2Count()));
        container.setChanged();
    }
}
