package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 手回しの冷凍機 [3-22]（1834 パーキンスの特許、閉じた回路の蒸気圧縮）。冷媒を入れて、水バケツで水を入れ、素手で右クリックしてハンドルを回す：
 * 圧縮（ポンプ）→ 放熱（銅の管で冷えて液に）→ 膨張弁（圧力が下がる）→ 蒸発（気化熱で水から熱をうばう）→ また圧縮。{@link #CRANKS} 回で氷1。
 * 冷媒がないと冷えない（実験）。冷媒：{@link #ETHER}＝ジエチルエーテル（1834 と同じ。燃えやすいので、作るたびに少しの確率でもれて失う＝事故）。
 * 冷凍機は「冷たさを作る」のでなく、中の熱を外へくみ出す熱のポンプ。
 */
public class PerkinsIceMachineBlock extends Block {
    public static final int NONE = 0, ETHER = 1, MAX_REFRIGERANT = 2;
    public static final int CRANKS = 16;
    public static final float ETHER_LEAK_CHANCE = 1f / 8f;
    public static final IntegerProperty REFRIGERANT = IntegerProperty.create("refrigerant", 0, MAX_REFRIGERANT);
    public static final BooleanProperty WATER = BooleanProperty.create("water");
    public static final IntegerProperty CRANK = IntegerProperty.create("crank", 0, CRANKS - 1);

    public PerkinsIceMachineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(REFRIGERANT, NONE).setValue(WATER, false).setValue(CRANK, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(REFRIGERANT, WATER, CRANK); }

    /** 手に持った物を冷媒として入れられるなら、その番号（入れられなければ NONE） */
    public static int refrigerantOf(ItemStack s) {
        if (s.is(Chemistry.DIETHYL_ETHER)) return ETHER;
        return NONE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (refrigerantOf(stack) == NONE && !stack.is(Items.WATER_BUCKET)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) fill(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) crank(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    /** 冷媒か水を入れる（テストからも呼ぶ） */
    public static boolean fill(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockState s = level.getBlockState(pos);
        if (stack.is(Items.WATER_BUCKET)) {
            if (s.getValue(WATER)) return false;
            stack.shrink(1);
            give(level, pos, player, new ItemStack(Items.BUCKET));
            level.setBlock(pos, s.setValue(WATER, true), 3);
            return true;
        }
        int r = refrigerantOf(stack);
        if (r == NONE || s.getValue(REFRIGERANT) != NONE) return false;
        ItemStack rem = stack.getItem().getCraftingRemainder(stack) == null ? ItemStack.EMPTY : stack.getItem().getCraftingRemainder(stack).create();
        stack.shrink(1);
        give(level, pos, player, rem);
        level.setBlock(pos, s.setValue(REFRIGERANT, r), 3);
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.perkins_ice_machine.charged"));
        return true;
    }

    /** ハンドルを1回まわす（テストからも呼ぶ）。CRANKS 回で氷を出し、true */
    public static boolean crank(Level level, BlockPos pos, Player player) {
        BlockState s = level.getBlockState(pos);
        if (s.getValue(REFRIGERANT) == NONE) { player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.perkins_ice_machine.no_refrigerant")); return false; }
        if (!s.getValue(WATER)) { player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.perkins_ice_machine.no_water")); return false; }
        int c = s.getValue(CRANK) + 1;
        level.playSound(null, pos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.4f, 0.8f + c * 0.02f);
        if (c < CRANKS) { level.setBlock(pos, s.setValue(CRANK, c), 3); return false; }
        give(level, pos, player, new ItemStack(Items.ICE));
        int r = s.getValue(REFRIGERANT);
        if (r == ETHER && level.getRandom().nextFloat() < ETHER_LEAK_CHANCE) {
            r = NONE;
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.perkins_ice_machine.ether_leak"));
        } else player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.perkins_ice_machine.ice"));
        afterIce(level, pos, player, s.getValue(REFRIGERANT));
        level.setBlock(pos, s.setValue(CRANK, 0).setValue(WATER, false).setValue(REFRIGERANT, r), 3);
        return true;
    }

    /** 氷を1つ作ったあとの冷媒ごとのできごと（[2-48] フロンで使う） */
    static void afterIce(Level level, BlockPos pos, Player player, int refrigerant) {}

    private static void give(Level level, BlockPos pos, Player player, ItemStack out) {
        if (out.isEmpty()) return;
        if (!player.getInventory().add(out) && !out.isEmpty())
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, out));
    }
}
