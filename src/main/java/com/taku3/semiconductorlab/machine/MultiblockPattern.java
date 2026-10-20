package com.taku3.semiconductorlab.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * マルチブロックの形と判定（IMP-33 ③、DESIGN「マルチブロック」）。形は文字の絵で、**下の段から**書く（vanilla の BlockPatternBuilder と同じ書き方）。
 * 1段＝何行かの文字（行＝手前から奥、文字＝左から右）。制御ブロックが北を向いているときの形で書き、4方向は自動で回す。
 * <pre>
 * MultiblockPattern.builder()
 *         .layer("BBB", "BCB", "BBB")              // 1段目（C＝制御ブロック）
 *         .layer("BTB", "T T", "BTB")              // 2段目
 *         .where('B', LINING_BLOCK, "lining")      // 名前 → lang multiblock.semiconductor-lab.lining
 *         .controller('C').build();
 * </pre>
 * 「 」（空白）は空気でなければならない所（炉の中）、「?」は何でもよい所。
 * vanilla の BlockPattern.matches は合う／合わないしか返さないので、自前で「どこが足りないか」まで返す。
 */
public final class MultiblockPattern {
    /** 文字1つの条件と名前（lang multiblock.semiconductor-lab.&lt;name&gt;） */
    public record Cell(char symbol, Predicate<BlockState> ok, String name) {}

    /** 足りない・違う場所：位置・期待する名前・今のブロック（空気なら missing） */
    public record Problem(BlockPos pos, String expected, BlockState actual, boolean missing) {}

    /** 判定の結果：組み上がったか・問題の一覧・名前ごとの足りない数 */
    public record Result(boolean formed, List<Problem> problems) {
        public Map<String, Integer> missingCounts() {
            Map<String, Integer> m = new HashMap<>();
            for (Problem p : problems) m.merge(p.expected(), 1, Integer::sum);
            return m;
        }
    }

    public static final char AIR = ' ', ANY = '?';

    private final List<String[]> layers;
    private final Map<Character, Cell> cells;
    private final char controller;
    private final int cx, cy, cz;

    private MultiblockPattern(List<String[]> layers, Map<Character, Cell> cells, char controller) {
        this.layers = List.copyOf(layers);
        this.cells = Map.copyOf(cells);
        this.controller = controller;
        int[] at = null;
        for (int y = 0; y < layers.size(); y++)
            for (int z = 0; z < layers.get(y).length; z++) {
                int x = layers.get(y)[z].indexOf(controller);
                if (x >= 0) { if (at != null) throw new IllegalStateException("two controllers"); at = new int[] {x, y, z}; }
            }
        if (at == null) throw new IllegalStateException("no controller '" + controller + "'");
        cx = at[0]; cy = at[1]; cz = at[2];
    }

    public static Builder builder() { return new Builder(); }

    /** 段の数・行の数（奥行き）・幅 */
    public int height() { return layers.size(); }
    public int depth() { return layers.get(0).length; }
    public int width() { return layers.get(0)[0].length(); }
    public List<String[]> layers() { return layers; }
    public Cell cell(char c) { return cells.get(c); }

    /**
     * 形の中の位置 (x, y, z) の、世界での位置。制御ブロックの正面＝facing。
     * 北向きの形を、(x, z) の向きだけ回す（東＝時計回りに 90°）
     */
    public BlockPos worldPos(BlockPos controllerPos, Direction facing, int x, int y, int z) {
        int lx = x - cx, lz = z - cz, dx, dz;
        switch (facing) {
            case EAST -> { dx = -lz; dz = lx; }
            case SOUTH -> { dx = -lx; dz = -lz; }
            case WEST -> { dx = lz; dz = -lx; }
            default -> { dx = lx; dz = lz; }
        }
        return controllerPos.offset(dx, y - cy, dz);
    }

    /** 組み上がっているかを調べる（読み込まれていない場所があれば組み上がっていない） */
    public Result check(Level level, BlockPos controllerPos, Direction facing) {
        List<Problem> problems = new ArrayList<>();
        for (int y = 0; y < layers.size(); y++)
            for (int z = 0; z < layers.get(y).length; z++) {
                String row = layers.get(y)[z];
                for (int x = 0; x < row.length(); x++) {
                    char c = row.charAt(x);
                    if (c == controller || c == ANY) continue;
                    BlockPos p = worldPos(controllerPos, facing, x, y, z);
                    if (!level.isLoaded(p)) return new Result(false, List.of(new Problem(p, "unloaded", null, true)));
                    BlockState s = level.getBlockState(p);
                    if (c == AIR) {
                        if (!s.isAir()) problems.add(new Problem(p, "inside", s, false));
                        continue;
                    }
                    Cell cell = cells.get(c);
                    if (!cell.ok().test(s)) problems.add(new Problem(p, cell.name(), s, s.isAir()));
                }
            }
        return new Result(problems.isEmpty(), problems);
    }

    /** 足りない・違う場所に粒子を出す（素手で右クリックしたとき。DESIGN「見せる」2） */
    public static void showProblems(ServerLevel level, Result r) {
        for (Problem p : r.problems())
            level.sendParticles(p.missing() ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER,
                    p.pos().getX() + 0.5, p.pos().getY() + 0.5, p.pos().getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
    }

    public static final class Builder {
        private final List<String[]> layers = new ArrayList<>();
        private final Map<Character, Cell> cells = new HashMap<>();
        private char controller = 'C';

        /** 1段（下から順に）。行＝手前（正面）から奥へ */
        public Builder layer(String... rows) {
            if (!layers.isEmpty() && (rows.length != layers.get(0).length || rows[0].length() != layers.get(0)[0].length()))
                throw new IllegalStateException("all layers must have the same size");
            layers.add(rows.clone());
            return this;
        }
        public Builder where(char c, Predicate<BlockState> ok, String name) { cells.put(c, new Cell(c, ok, name)); return this; }
        public Builder where(char c, Block block, String name) { return where(c, s -> s.is(block), name); }
        public Builder controller(char c) { controller = c; return this; }

        public MultiblockPattern build() {
            for (String[] l : layers) for (String row : l) for (char c : row.toCharArray())
                if (c != controller && c != AIR && c != ANY && !cells.containsKey(c)) throw new IllegalStateException("no block for '" + c + "'");
            return new MultiblockPattern(layers, cells, controller);
        }
    }
}
