package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 塩田 [0-16]（揚げ浜式：海水を砂の上にまいて天日で濃くし、かん水を釜で煮詰める）。電気も燃料も使わない。
 * <ul>
 *   <li>海・浜バイオームにあり、横（東西南北）に水がある（＝海水をくめる）</li>
 *   <li>真上が空に開いていて、雨が降っていない（天日で乾かす）</li>
 * </ul>
 * の4つがそろうと時間で塩（NaCl）がたまり、塩 {@value #SALT_PER_BITTERN} 個ごとににがり（MgCl2）が1個たまる。
 * 右クリックでたまった分を受け取る。満杯（塩 {@value #MAX_SALT}・にがり {@value #MAX_BITTERN}）になると止まる。
 * ⚠️ 近似：煮詰め釜の燃料は省略（salt_pan.placeholder）。
 */
public class SaltPanBlockEntity extends BlockEntity {
    /** 塩1個にかかる時間（tick）。1分 */
    public static final int TICKS_PER_SALT = 20 * 60;
    /** 海水 1t から NaCl 約27kg・MgCl2 約3.8kg（物質量比で約11:1）。ゲームでは塩8個ごとににがり1個 */
    public static final int SALT_PER_BITTERN = 8;
    public static final int MAX_SALT = 64, MAX_BITTERN = 16;

    public enum Status { WORKING, NOT_SEA, NO_WATER, NO_SKY, RAINING, FULL }

    private int progress, salt, bittern, saltMade;
    /** テスト専用：バイオーム判定を「海」とみなす（GameTest の世界は平原なので） */
    private boolean forceSeaForTest;

    public SaltPanBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.SALT_PAN, pos, state); }

    public int getSalt() { return salt; }
    public int getBittern() { return bittern; }
    public int getProgress() { return progress; }
    public void setForceSeaForTest(boolean b) { forceSeaForTest = b; }

    /** 海・浜バイオームか（ここで「海水」を区別する。川・沼の水は淡水なので塩はとれない） */
    public static boolean isSeaBiome(Level level, BlockPos pos) {
        var b = level.getBiome(pos);
        return b.is(BiomeTags.IS_OCEAN) || b.is(BiomeTags.IS_BEACH);
    }

    public static boolean hasWaterBeside(Level level, BlockPos pos) {
        for (Direction d : Direction.Plane.HORIZONTAL)
            if (level.getFluidState(pos.relative(d)).is(FluidTags.WATER)) return true;
        return false;
    }

    /**
     * 真上に空が開いているか。高さマップ（その列でいちばん上の「動きをさえぎるブロック」の1つ上）が塩田のすぐ上なら空。
     * canSeeSky（空の明るさで判定）は光の計算が後から追いつくので、置いた直後の屋根に反応しない。高さマップはすぐ変わる。
     * 高さマップより下にあっても、日光を通すブロック（ガラス・バリアなど）は「空」とみなす（ガラス屋根の下でも日は当たる）。
     */
    public static boolean openSky(Level level, BlockPos pos) {
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        BlockPos.MutableBlockPos m = pos.mutable();
        for (int y = pos.getY() + 1; y < top; y++) {
            BlockState s = level.getBlockState(m.setY(y));
            if (!s.isAir() && !s.propagatesSkylightDown()) return false;
        }
        return true;
    }

    public Status status(Level level, BlockPos pos) {
        if (!forceSeaForTest && !isSeaBiome(level, pos)) return Status.NOT_SEA;
        if (!hasWaterBeside(level, pos)) return Status.NO_WATER;
        if (!openSky(level, pos)) return Status.NO_SKY;
        if (level.isRainingAt(pos.above())) return Status.RAINING;
        if (salt >= MAX_SALT || bittern >= MAX_BITTERN) return Status.FULL;
        return Status.WORKING;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SaltPanBlockEntity be) {
        if (level.isClientSide()) return;
        // 条件の確認は1秒に1回（毎 tick はバイオームを引かない）。進みは確認した時点で20 tick 分まとめて足す
        if (level.getGameTime() % 20 != 0) return;
        be.step(level, pos, 20);
    }

    /** 条件がそろっていれば ticks 分進める（GameTest からも呼ぶ） */
    public void step(Level level, BlockPos pos, int ticks) {
        if (status(level, pos) != Status.WORKING) return;   // 雨・夜露で止まっても進みは保つ（砂は乾いたまま）
        progress += ticks;
        while (progress >= TICKS_PER_SALT && salt < MAX_SALT && bittern < MAX_BITTERN) {
            progress -= TICKS_PER_SALT;
            salt++;
            // 煮詰めると NaCl が先に結晶し、MgCl2 は母液（にがり）に残る
            if (++saltMade % SALT_PER_BITTERN == 0) bittern++;
        }
        setChanged();
    }

    /** たまった塩とにがりをプレイヤーに渡す（持ちきれない分は足元に落とす）。渡した個数の合計を返す */
    public int collect(Player player) {
        int n = salt + bittern;
        give(player, new ItemStack(ModItems.SALT, salt));
        give(player, new ItemStack(ModItems.BITTERN, bittern));
        salt = 0;
        bittern = 0;
        setChanged();
        return n;
    }

    private void give(Player player, ItemStack s) {
        if (s.isEmpty()) return;
        if (!player.getInventory().add(s) && !s.isEmpty() && level != null) Block.popResource(level, worldPosition.above(), s);
    }

    /** 壊したら、たまっていた塩とにがりを落とす */
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            if (salt > 0) Block.popResource(level, pos, new ItemStack(ModItems.SALT, salt));
            if (bittern > 0) Block.popResource(level, pos, new ItemStack(ModItems.BITTERN, bittern));
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("Progress", progress); out.putInt("Salt", salt); out.putInt("Bittern", bittern); out.putInt("SaltMade", saltMade);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        progress = in.getIntOr("Progress", 0); salt = in.getIntOr("Salt", 0); bittern = in.getIntOr("Bittern", 0); saltMade = in.getIntOr("SaltMade", 0);
    }
}
