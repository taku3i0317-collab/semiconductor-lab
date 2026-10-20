package com.taku3.semiconductorlab.machine;

import com.taku3.semiconductorlab.content.Reg;
import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyApi;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 機械1台ぶんの登録をまとめたもの（IMP-28）：ブロック＋ブロックのアイテム＋BE の種類＋画面の種類＋電気の受け口。
 * 前は ModBlocks・ModBlockEntities・ModMenus・（電気の登録）に1行ずつ、さらに Menu クラスが要った。今は1行。
 * <pre>
 * // 電気で動くふつうの機械（向き＋点灯のブロック、画面、電気の受け口）
 * public static final Machine&lt;WaferSawBlockEntity&gt; WAFER_SAW =
 *         Machine.electric("wafer_saw", WaferSawBlockEntity::new, WaferSawBlockEntity.LAYOUT, Machine.metal(4.0f));
 * // 特別なブロック・tick・電気の受け口を持つ機械は builder で
 * public static final Machine&lt;SaltPanBlockEntity&gt; SALT_PAN = Machine.builder("salt_pan", SaltPanBlockEntity::new)
 *         .block(SaltPanBlock::new).ticker(SaltPanBlockEntity::tick).props(...).build();
 * </pre>
 * 使う側：ブロックは {@code X.block}、BE の種類は {@code X.type}（BE のコンストラクタの super に渡す）。
 */
public final class Machine<T extends BlockEntity> {
    private static final Map<BlockEntityType<?>, Machine<?>> BY_TYPE = new HashMap<>();
    private static final List<Machine<?>> ALL = new ArrayList<>();

    public final String name;
    public final Block block;
    public final BlockEntityType<T> type;
    /** 画面の形。null＝画面なし（水車・ケーブルなど） */
    public final MachineLayout layout;
    /** 画面の種類。null＝画面なし */
    public final MenuType<MachineMenu> menu;
    private final FabricBlockEntityTypeBuilder.Factory<T> factory;
    private final BlockEntityTicker<T> ticker;

    private Machine(Builder<T> b) {
        this.name = b.name;
        this.factory = b.factory;
        this.ticker = b.ticker;
        this.layout = b.layout;
        this.block = Reg.block(name, p -> b.block.apply(p, this), b.props);
        this.type = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id(name), FabricBlockEntityTypeBuilder.create(factory, block).build());
        this.menu = layout == null ? null : Registry.register(BuiltInRegistries.MENU, SemiconductorLab.id(name),
                new MenuType<>((id, inv) -> new MachineMenu(this, id, inv), FeatureFlags.DEFAULT_FLAGS));
        if (b.energy != null) EnergyApi.ENERGY.registerForBlockEntity(b.energy, type);
        BY_TYPE.put(type, this);
        ALL.add(this);
    }

    /** 電気で動くふつうの機械（ConsumerBlockEntity を継承した BE）。ブロックは {@link MachineBlock}、全面で電気を受け取る */
    public static <T extends ConsumerBlockEntity> Machine<T> electric(String name, FabricBlockEntityTypeBuilder.Factory<T> factory,
                                                                     MachineLayout layout, BlockBehaviour.Properties props) {
        return builder(name, factory).ticker(ConsumerBlockEntity::tick).layout(layout).energy((be, dir) -> be.getStorage()).props(props).build();
    }

    public static <T extends BlockEntity> Builder<T> builder(String name, FabricBlockEntityTypeBuilder.Factory<T> factory) {
        return new Builder<>(name, factory);
    }

    /** BE の種類から機械を引く（MachineBE が画面を作るときに使う） */
    public static Machine<?> of(BlockEntityType<?> type) { return BY_TYPE.get(type); }
    /** 登録した順の全部の機械（クリエイティブタブ・GameTest 用） */
    public static List<Machine<?>> all() { return Collections.unmodifiableList(ALL); }

    public T create(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { return factory.create(pos, state); }

    /** ブロックの getTicker から呼ぶ：この機械の BE なら毎 tick の処理を返す */
    @SuppressWarnings("unchecked")
    public <A extends BlockEntity> BlockEntityTicker<A> tickerFor(BlockEntityType<A> t) {
        return t == type && ticker != null ? (BlockEntityTicker<A>) ticker : null;
    }

    // よく使うブロックの性質（硬さ・音）。光り方は lit(...) で足す
    public static BlockBehaviour.Properties metal(float strength) {
        return BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).requiresCorrectToolForDrops().strength(strength).sound(net.minecraft.world.level.block.SoundType.METAL);
    }
    public static BlockBehaviour.Properties stone(float strength) {
        return BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).requiresCorrectToolForDrops().strength(strength).sound(net.minecraft.world.level.block.SoundType.STONE);
    }
    /** 動いている（LIT）間だけ明るさ light で光る */
    public static BlockBehaviour.Properties lit(BlockBehaviour.Properties p, int light) {
        return p.lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? light : 0);
    }

    public static final class Builder<T extends BlockEntity> {
        private final String name;
        private final FabricBlockEntityTypeBuilder.Factory<T> factory;
        private BiFunction<BlockBehaviour.Properties, Machine<T>, Block> block = MachineBlock::new;
        private BlockEntityTicker<T> ticker;
        private MachineLayout layout;
        private BiFunction<? super T, Direction, EnergyStorage> energy;
        private BlockBehaviour.Properties props;

        private Builder(String name, FabricBlockEntityTypeBuilder.Factory<T> factory) { this.name = name; this.factory = factory; }

        /** ブロックのクラス（既定は向き＋点灯の {@link MachineBlock}）。コンストラクタは (Properties, Machine) */
        public Builder<T> block(BiFunction<BlockBehaviour.Properties, Machine<T>, Block> f) { block = f; return this; }
        /** 毎 tick の処理（サーバー側でもクライアント側でも呼ばれるので、中で isClientSide を見る） */
        public Builder<T> ticker(BlockEntityTicker<T> t) { ticker = t; return this; }
        /** 画面の形（BE は MachineBE を実装する） */
        public Builder<T> layout(MachineLayout l) { layout = l; return this; }
        /** 電気の受け口（面ごとに違うなら dir を見る） */
        public Builder<T> energy(BiFunction<? super T, Direction, EnergyStorage> e) { energy = e; return this; }
        public Builder<T> props(BlockBehaviour.Properties p) { props = p; return this; }
        public Machine<T> build() { return new Machine<>(this); }
    }
}
