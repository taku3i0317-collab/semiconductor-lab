# API_26X.md — Minecraft 26.3 / Fabric で javap により確認した API の形

ルール：javap で調べたら必ずここに追記する。推測で書かない。「？」は未確認。
調べ方：`javap -cp <jar> <クラス名> | Select-String <パターン>`（jar は .gradle\loom-cache 内の minecraft-common / clientOnly）

## 3年前から変わった点（まとめ）
- worldgen: configured_feature フォルダ/レジストリは feature に改名（26.1）。feature JSON は "config": {} の入れ子がなくなり、type と同じ階層に書く。targets の state は {"Name":…} ではなく文字列 "modid:block" で書く
- recipe / advancement / loot_table は単数形（1.21〜）
- loot_table（26.3）: "conditions":[{"condition":…}] は廃止 → "condition": {"type":…} の単数（または述語ID文字列 "minecraft:tool/can_silk_touch"）。"functions":[{"function":…}] は廃止 → "modifier": [{"type":…}]。古い書式はエラーにならず**黙って無視**され、ログに WARN「validation problem … Unreachable entry!」が出るだけ（シルクタッチ無しでも鉱石ブロックが落ちていた。GameTest blockDrops で検出・修正）
- items/ の client item 定義が必要（1.21.4〜）
- Fabric ItemTooltipCallback は引数4つ (stack, context, flag, lines)
- Screen.hasShiftDown() は廃止。Shift判定は Minecraft.getInstance().hasShiftDown()（Minecraft クラスのインスタンスメソッドに移動）
- Block の MapCodec / simpleCodec / codec() は廃止（26.x）。ブロッククラスに書かない
- I18n.exists() は廃止。翻訳キーの存在チェックは Language.getInstance().has(key)
- Player.displayClientMessage は廃止 → sendSystemMessage__ONEARG
- Level.isClientSide() はメソッド（フィールドではない）
- Level.setBlock(pos, state, 3) で動作（int 1つ版も存在）

## BlockEntity（#6 で確認・動作済み）
- 登録：`FabricBlockEntityTypeBuilder.create(BE::new, block).build()` → `Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type)`
- 保存：`saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`。`out.putInt` / `in.getIntOr(key, def)`／`out.putBoolean` / `in.getBooleanOr(key, def)`（javap 確認 2026-10 タスク04）
- アイテム：`ContainerHelper.saveAllItems(ValueOutput, NonNullList)` / `loadAllItems(ValueInput, NonNullList)`
- ticker：`EntityBlock.getTicker(Level, BlockState, BlockEntityType<T>)` → `BlockEntityTicker<T>.tick(Level, BlockPos, BlockState, T)`
- 右クリック：`useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)` / `useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)`
- 戻り値：`InteractionResult.SUCCESS / SUCCESS_SERVER / CONSUME / FAIL / PASS / TRY_WITH_EMPTY_HAND`
- 落とす：`Block.popResource(Level, BlockPos, ItemStack)`／`Containers.dropContents(Level, BlockPos, NonNullList)`
- 壊したとき：`BlockEntity.preRemoveSideEffects(BlockPos, BlockState)`（onRemove は廃止）

## Menu（#7 で確認）
- `AbstractContainerMenu(MenuType<?>, int containerId)`
- `addSlot(Slot)`／`Slot(Container, index, x, y)`／`addStandardInventorySlots(Container playerInv, x, y)`
- `addDataSlots(ContainerData)`／`SimpleContainerData(int)`／`ContainerData.get/set/getCount`
- `stillValid(ContainerLevelAccess, Player, Block)`／`ContainerLevelAccess.create(Level, BlockPos)`
- `quickMoveStack(Player, int)` 抽象／`moveItemStackTo(ItemStack, from, to, reverse)`
- 登録：`new MenuType<>(MenuSupplier, FeatureFlags.DEFAULT_FLAGS)`。`MenuSupplier.create(int, Inventory)`。Fabric の screen-handler-api は不要
- 開く：`Player.openMenu(MenuProvider)`。`MenuProvider.getDisplayName()` + `MenuConstructor.createMenu(int, Inventory, Player)`
- Block：`getMenuProvider(BlockState, Level, BlockPos)` をオーバーライド
- v0.2a #4 蒸気機関 GUI も上の形だけで実装（新しい API なし・コンパイル通過）。注意：ContainerData の同期は short 幅とされる（バニラ慣例。javap では未確認）ため、50000 W や 200000 J は 0.1kW / kJ 単位に縮めて送っている

## Screen（#7 で確認・一部未確認）
- `AbstractContainerScreen(T menu, Inventory, Component title)`／`init()`／`imageWidth`, `leftPos`, `topPos`, `titleLabelX/Y`, `inventoryLabelX/Y`
- 登録：`MenuScreens.register(MenuType, ScreenConstructor)`
- ★ `GuiGraphics` は廃止 → `GuiGraphicsExtractor`。描画システムは renderpearl
- ★ `renderBg` / `renderLabels` は廃止 → `extractRenderState(GuiGraphicsExtractor, int, int, float)`／`extractLabels(GuiGraphicsExtractor, int, int)`／`extractTooltip(...)`
- `RenderPipelines.GUI_TEXTURED` が存在（blit の引数に使う可能性）
- ？ GuiGraphicsExtractor の blit / 文字描画の関数名 → 下の調査ログ参照

## 爆発・音・パーティクル（[6-02] ガスホルダーで確認・使用）
- `Level.playSound(Entity, double x, double y, double z, Holder<SoundEvent>, SoundSource, float vol, float pitch)`（Holder 版。`SoundEvents.GENERIC_EXPLODE` は `Holder$Reference<SoundEvent>`）。SoundEvent 版の同じ形もある
- `ServerLevel.sendParticles(T extends ParticleOptions, x, y, z, int count, dx, dy, dz, double speed)`／`ParticleTypes.EXPLOSION`, `POOF` は SimpleParticleType
- `Level.explode(Entity, x, y, z, float power, Level$ExplosionInteraction)`（ほか boolean fire 付き・DamageSource 付きなど5形）。`ExplosionInteraction` = NONE / BLOCK / MOB / TNT / TRIGGER。NONE は「ブロックを壊さない」指定で、人へのダメージは別扱い（？未確認）。ガスホルダーでは人を傷つけないよう explode を使わず音＋パーティクルだけにした
- `ValueOutput.putString(String, String)`／`ValueInput.getStringOr(String, String)`
- `SoundSource.BLOCKS`、`Direction.values()`

## 調査ログ（生の javap 出力）
### GuiGraphicsExtractor（2026-10-19）
`` 
public net.minecraft.client.gui.GuiGraphicsExtractor(net.minecraft.client.Minecraft, net.minecraft.client.renderer.state.gui.GuiRenderState, int, int);
public void fill(int, int, int, int, int);
public void fill(com.mojang.renderpearl.api.pipeline.RenderPipeline, int, int, int, int, int);
public void fillGradient(int, int, int, int, int, int);
public void fill(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.client.gui.render.TextureSetup, int, int, int, int);
public void textHighlight(int, int, int, int, boolean);
public void text(net.minecraft.client.gui.Font, java.lang.String, int, int, int);
public void text(net.minecraft.client.gui.Font, java.lang.String, int, int, int, boolean);
public void text(net.minecraft.client.gui.Font, net.minecraft.util.FormattedCharSequence, int, int, int);
public void text(net.minecraft.client.gui.Font, net.minecraft.util.FormattedCharSequence, int, int, int, boolean);
public void text(net.minecraft.client.gui.Font, net.minecraft.network.chat.Component, int, int, int);
public void text(net.minecraft.client.gui.Font, net.minecraft.network.chat.Component, int, int, int, boolean);
public void centeredText(net.minecraft.client.gui.Font, java.lang.String, int, int, int);
public void centeredText(net.minecraft.client.gui.Font, net.minecraft.network.chat.Component, int, int, int);
public void centeredText(net.minecraft.client.gui.Font, net.minecraft.util.FormattedCharSequence, int, int, int);
public int textWithWordWrap(net.minecraft.client.gui.Font, net.minecraft.network.chat.FormattedText, int, int, int, int);
public int textWithWordWrap(net.minecraft.client.gui.Font, net.minecraft.network.chat.FormattedText, int, int, int, int, boolean);
public void textWithBackdrop(net.minecraft.client.gui.Font, net.minecraft.network.chat.Component, int, int, int, int);
public void blit(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, float, float, int, int, int, int, int);
public void blit(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, float, float, int, int, int, int);
public void blit(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, float, float, int, int, int, int, int, int);
public void blit(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, float, float, int, int, int, int, int, int, int);
public void blit(net.minecraft.resources.Identifier, int, int, int, int, float, float, float, float);
public void blit(com.mojang.renderpearl.api.textures.GpuTextureView, com.mojang.renderpearl.api.textures.GpuSampler, int, int, int, int, float, float, float, float);
public void blitSprite(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, int, int);
public void blitSprite(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, int, int, float);
public void blitSprite(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, int, int, int);
public void blitSprite(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, int, int, int, int, int, int);
public void blitSprite(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.resources.Identifier, int, int, int, int, int, int, int, int, int);
public void blitSprite(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.client.renderer.texture.TextureAtlasSprite, int, int, int, int);
public void blitSprite(com.mojang.renderpearl.api.pipeline.RenderPipeline, net.minecraft.client.renderer.texture.TextureAtlasSprite, int, int, int, int, int);
public void itemDecorations(net.minecraft.client.gui.Font, net.minecraft.world.item.ItemStack, int, int);
public void itemDecorations(net.minecraft.client.gui.Font, net.minecraft.world.item.ItemStack, int, int, java.lang.String);
public void setTooltipForNextFrame(net.minecraft.network.chat.Component, int, int);
public void setTooltipForNextFrame(net.minecraft.client.gui.Font, net.minecraft.world.item.ItemStack, int, int);
public void setTooltipForNextFrame(net.minecraft.client.gui.Font, java.util.List<net.minecraft.network.chat.Component>, java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent>, int, int);
public void setTooltipForNextFrame(net.minecraft.client.gui.Font, java.util.List<net.minecraft.network.chat.Component>, java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent>, int, int, net.minecraft.resources.Identifier);
public void setTooltipForNextFrame(net.minecraft.client.gui.Font, java.util.List<net.minecraft.network.chat.Component>, java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent>, int, int, net.minecraft.resources.Identifier, boolean);
public void setTooltipForNextFrame(net.minecraft.client.gui.Font, java.util.List<net.minecraft.util.FormattedCharSequence>, java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent>, net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner, int, int, boolean, net.minecraft.resources.Identifier);
public void setTooltipForNextFrame(net.minecraft.client.gui.Font, net.minecraft.network.chat.Component, int, int);
``
### AbstractContainerScreen の extract 系・submit 系
`` 
public static final net.minecraft.resources.Identifier INVENTORY_LOCATION;
protected static final int BACKGROUND_TEXTURE_WIDTH;
protected static final int BACKGROUND_TEXTURE_HEIGHT;
protected static final int DEFAULT_IMAGE_WIDTH;
protected static final int DEFAULT_IMAGE_HEIGHT;
protected final int imageWidth;
protected final int imageHeight;
protected int titleLabelX;
protected int titleLabelY;
protected int inventoryLabelX;
protected int inventoryLabelY;
protected final T menu;
protected final net.minecraft.network.chat.Component playerInventoryTitle;
protected net.minecraft.world.inventory.Slot hoveredSlot;
protected int leftPos;
protected int topPos;
protected final java.util.Set<net.minecraft.world.inventory.Slot> quickCraftSlots;
protected boolean isQuickCrafting;
protected void init();
protected void addItemSlotMouseAction(net.minecraft.client.gui.ItemSlotMouseAction);
public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, float);
public void extractContents(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, float);
public void extractCarriedItem(net.minecraft.client.gui.GuiGraphicsExtractor, int, int);
protected void extractSlots(net.minecraft.client.gui.GuiGraphicsExtractor, int, int);
protected void extractTooltip(net.minecraft.client.gui.GuiGraphicsExtractor, int, int);
protected java.util.List<net.minecraft.network.chat.Component> getTooltipFromContainerItem(net.minecraft.world.item.ItemStack);
protected void extractLabels(net.minecraft.client.gui.GuiGraphicsExtractor, int, int);
protected void extractSlot(net.minecraft.client.gui.GuiGraphicsExtractor, net.minecraft.world.inventory.Slot, int, int);
protected boolean hasClickedOutside(double, double, int, int);
protected boolean isHovering(int, int, int, int, double, double);
protected void slotClicked(net.minecraft.world.inventory.Slot, int, int, net.minecraft.world.inventory.ContainerInput);
protected void onMouseClickAction(net.minecraft.world.inventory.Slot, net.minecraft.world.inventory.ContainerInput);
protected void handleSlotStateChanged(int, int, boolean);
protected boolean checkHotbarKeyPressed(net.minecraft.client.input.KeyEvent);
protected void containerTick();
``

## 先行調査（2026-10-19）

### #7 AbstractFurnaceScreen が持つメソッド
`` 
public abstract class net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen<T extends net.minecraft.world.inventory.AbstractFurnaceMenu> extends net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen<T> {
public net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen(T, net.minecraft.world.entity.player.Inventory, net.minecraft.network.chat.Component, net.minecraft.network.chat.Component, net.minecraft.resources.Identifier, net.minecraft.resources.Identifier, net.minecraft.resources.Identifier, java.util.List<net.minecraft.client.gui.screens.recipebook.RecipeBookComponent$TabInfo>);
public void init();
protected net.minecraft.client.gui.navigation.ScreenPosition getRecipeBookButtonPosition();
public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, float);
``

### #7 AbstractRecipeBookScreen（かまどの親）
`` 
protected void init();
protected abstract net.minecraft.client.gui.navigation.ScreenPosition getRecipeBookButtonPosition();
protected void onRecipeBookButtonClick();
public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, float);
protected void extractSlots(net.minecraft.client.gui.GuiGraphicsExtractor, int, int);
protected boolean isBiggerResultSlot();
protected boolean isHovering(int, int, int, int, double, double);
protected boolean hasClickedOutside(double, double, int, int);
protected void slotClicked(net.minecraft.world.inventory.Slot, int, int, net.minecraft.world.inventory.ContainerInput);
``

### #7 Screen の extract/background 系
`` 
public static final net.minecraft.resources.Identifier MENU_BACKGROUND;
public final void extractRenderStateWithTooltipAndSubtitles(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, float);
public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, float);
protected void init();
public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, float);
protected void extractBlurredBackground(net.minecraft.client.gui.GuiGraphicsExtractor);
protected void extractPanorama(net.minecraft.client.gui.GuiGraphicsExtractor, float);
protected void extractMenuBackground(net.minecraft.client.gui.GuiGraphicsExtractor);
protected void extractMenuBackground(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, int, int);
public static void extractMenuBackgroundTexture(net.minecraft.client.gui.GuiGraphicsExtractor, net.minecraft.resources.Identifier, int, int, float, float, int, int);
public void extractTransparentBackground(net.minecraft.client.gui.GuiGraphicsExtractor);
public net.minecraft.sounds.Music getBackgroundMusic();
``

### #7 ContainerInput / quickMove 周り
`` 
public final class net.minecraft.world.inventory.ContainerInput extends java.lang.Enum<net.minecraft.world.inventory.ContainerInput> {
public static final net.minecraft.world.inventory.ContainerInput PICKUP;
public static final net.minecraft.world.inventory.ContainerInput QUICK_MOVE;
public static final net.minecraft.world.inventory.ContainerInput SWAP;
public static final net.minecraft.world.inventory.ContainerInput CLONE;
public static final net.minecraft.world.inventory.ContainerInput THROW;
public static final net.minecraft.world.inventory.ContainerInput QUICK_CRAFT;
public static final net.minecraft.world.inventory.ContainerInput PICKUP_ALL;
public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, net.minecraft.world.inventory.ContainerInput> STREAM_CODEC;
public static net.minecraft.world.inventory.ContainerInput[] values();
public static net.minecraft.world.inventory.ContainerInput valueOf(java.lang.String);
public int id();
``

### #8 DataComponents の BOOK 系
`` 
public static final net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.WritableBookContent> WRITABLE_BOOK_CONTENT;
public static final net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.WrittenBookContent> WRITTEN_BOOK_CONTENT;
``

### #8 WrittenBookContent
`` 
public final class net.minecraft.world.item.component.WrittenBookContent extends java.lang.Record implements net.minecraft.world.item.component.BookContent<net.minecraft.network.chat.Component, net.minecraft.world.item.component.WrittenBookContent>, net.minecraft.world.item.component.TooltipProvider {
public static final net.minecraft.world.item.component.WrittenBookContent EMPTY;
public static final int PAGE_LENGTH;
public static final int TITLE_LENGTH;
public static final int TITLE_MAX_LENGTH;
public static final int MAX_GENERATION;
public static final com.mojang.serialization.Codec<net.minecraft.network.chat.Component> CONTENT_CODEC;
public static final com.mojang.serialization.Codec<java.util.List<net.minecraft.server.network.Filterable<net.minecraft.network.chat.Component>>> PAGES_CODEC;
public static final com.mojang.serialization.Codec<net.minecraft.world.item.component.WrittenBookContent> CODEC;
public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, net.minecraft.world.item.component.WrittenBookContent> STREAM_CODEC;
public net.minecraft.world.item.component.WrittenBookContent(net.minecraft.server.network.Filterable<java.lang.String>, java.lang.String, int, java.util.List<net.minecraft.server.network.Filterable<net.minecraft.network.chat.Component>>, boolean);
public static com.mojang.serialization.Codec<java.util.List<net.minecraft.server.network.Filterable<net.minecraft.network.chat.Component>>> pagesCodec(com.mojang.serialization.Codec<net.minecraft.network.chat.Component>);
public net.minecraft.world.item.component.WrittenBookContent craftCopy();
public static boolean resolveForItem(net.minecraft.world.item.ItemStack, net.minecraft.network.chat.ResolutionContext, net.minecraft.core.HolderLookup$Provider);
public net.minecraft.world.item.component.WrittenBookContent resolve(net.minecraft.network.chat.ResolutionContext, net.minecraft.core.HolderLookup$Provider);
public net.minecraft.world.item.component.WrittenBookContent markResolved();
public java.util.List<net.minecraft.network.chat.Component> getPages(boolean);
public net.minecraft.world.item.component.WrittenBookContent withReplacedPages(java.util.List<net.minecraft.server.network.Filterable<net.minecraft.network.chat.Component>>);
public void addToTooltip(net.minecraft.world.item.Item$TooltipContext, java.util.function.Consumer<net.minecraft.network.chat.Component>, net.minecraft.world.item.TooltipFlag, net.minecraft.core.component.DataComponentGetter);
public final java.lang.String toString();
public final int hashCode();
public final boolean equals(java.lang.Object);
public net.minecraft.server.network.Filterable<java.lang.String> title();
public java.lang.String author();
public int generation();
public java.util.List<net.minecraft.server.network.Filterable<net.minecraft.network.chat.Component>> pages();
public boolean resolved();
public java.lang.Object withReplacedPages(java.util.List);
``

### #8 Filterable
`` 
public final class net.minecraft.server.network.Filterable<T> extends java.lang.Record {
public net.minecraft.server.network.Filterable(T, java.util.Optional<T>);
public static <T> com.mojang.serialization.Codec<net.minecraft.server.network.Filterable<T>> codec(com.mojang.serialization.Codec<T>);
public static <B extends io.netty.buffer.ByteBuf, T> net.minecraft.network.codec.StreamCodec<B, net.minecraft.server.network.Filterable<T>> streamCodec(net.minecraft.network.codec.StreamCodec<B, T>);
public static <T> net.minecraft.server.network.Filterable<T> passThrough(T);
public static net.minecraft.server.network.Filterable<java.lang.String> from(net.minecraft.server.network.FilteredText);
public T get(boolean);
public <U> net.minecraft.server.network.Filterable<U> map(java.util.function.Function<T, U>);
public <U> java.util.Optional<net.minecraft.server.network.Filterable<U>> resolve(java.util.function.Function<T, java.util.Optional<U>>);
public final java.lang.String toString();
public final int hashCode();
public final boolean equals(java.lang.Object);
public T raw();
public java.util.Optional<T> filtered();
``

### #8 ServerPlayConnectionEvents（初回参加で本を渡す）
`` 
public static final net.fabricmc.fabric.api.event.Event<net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents$Join> JOIN;
public static final net.fabricmc.fabric.api.event.Event<net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents$Disconnect> DISCONNECT;
``

### #8 ServerPlayerEvents
`` 
public static final net.fabricmc.fabric.api.event.Event<net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents$CopyFrom> COPY_FROM;
public static final net.fabricmc.fabric.api.event.Event<net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents$AfterRespawn> AFTER_RESPAWN;
public static final net.fabricmc.fabric.api.event.Event<net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents$Join> JOIN;
public static final net.fabricmc.fabric.api.event.Event<net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents$Leave> LEAVE;
public static final net.fabricmc.fabric.api.event.Event<net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents$AllowDeath> ALLOW_DEATH;
``

### v0.1.1 BlockBehaviour のレッドストーン系
`` 
protected void neighborChanged(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.level.block.Block, net.minecraft.world.level.redstone.Orientation, boolean);
protected boolean hasAnalogOutputSignal(net.minecraft.world.level.block.state.BlockState);
protected int getAnalogOutputSignal(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.core.Direction);
protected boolean isSignalSource(net.minecraft.world.level.block.state.BlockState);
protected int getSignal(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.core.Direction);
protected int ownSignal(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos);
protected int getDirectSignal(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.core.Direction);
``

### v0.1.1 RedstoneTorchBlock（参考：出力側）
`` 
public class net.minecraft.world.level.block.RedstoneTorchBlock extends net.minecraft.world.level.block.BaseTorchBlock {
public static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT;
public static final int RECENT_TOGGLE_TIMER;
public static final int MAX_RECENT_TOGGLES;
public static final int RESTART_DELAY;
public net.minecraft.world.level.block.RedstoneTorchBlock(net.minecraft.world.level.block.state.BlockBehaviour$Properties);
protected void onPlace(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, boolean);
protected void affectNeighborsAfterRemoval(net.minecraft.world.level.block.state.BlockState, net.minecraft.server.level.ServerLevel, net.minecraft.core.BlockPos, boolean);
protected boolean hasNeighborSignal(net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState);
protected void tick(net.minecraft.world.level.block.state.BlockState, net.minecraft.server.level.ServerLevel, net.minecraft.core.BlockPos, net.minecraft.util.RandomSource);
protected void neighborChanged(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.level.block.Block, net.minecraft.world.level.redstone.Orientation, boolean);
protected int getDirectSignal(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.core.Direction);
protected boolean isSignalSource(net.minecraft.world.level.block.state.BlockState);
protected int ownSignal(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos);
protected int getSignal(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.core.Direction);
public void animateTick(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.util.RandomSource);
protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition$Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState>);
protected net.minecraft.world.level.redstone.Orientation randomOrientation(net.minecraft.world.level.Level, net.minecraft.world.level.block.state.BlockState);
``

### v0.2a fabric-transfer-api-v1 のクラス一覧
`` 

``

### v0.2a BlockApiLookup
`` 
public static <A, C> net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup<A, C> get(net.minecraft.resources.Identifier, java.lang.Class<A>, java.lang.Class<C>);
public default A find(net.minecraft.world.level.Level, net.minecraft.core.BlockPos, C);
public abstract A find(net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.block.entity.BlockEntity, C);
public abstract void registerSelf(net.minecraft.world.level.block.entity.BlockEntityType<?>...);
public abstract void registerForBlocks(net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup$BlockApiProvider<A, C>, net.minecraft.world.level.block.Block...);
public default <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerForBlockEntity(java.util.function.BiFunction<? super T, C, A>, net.minecraft.world.level.block.entity.BlockEntityType<T>);
public abstract void registerForBlockEntities(net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup$BlockEntityApiProvider<A, C>, net.minecraft.world.level.block.entity.BlockEntityType<?>...);
public abstract void registerFallback(net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup$BlockApiProvider<A, C>);
``

### v0.2b Recipe
`` 
public abstract boolean matches(T, net.minecraft.world.level.Level);
public abstract net.minecraft.world.item.ItemStack assemble(T);
public default boolean isSpecial();
public abstract boolean showNotification();
public abstract java.lang.String group();
public abstract net.minecraft.world.item.crafting.RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<T>> getSerializer();
public abstract net.minecraft.world.item.crafting.RecipeType<? extends net.minecraft.world.item.crafting.Recipe<T>> getType();
public abstract net.minecraft.world.item.crafting.PlacementInfo placementInfo();
public default java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display();
public abstract net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory();
``

### v0.2b RecipeType / RecipeSerializer
`` 
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.CraftingRecipe> CRAFTING;
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.SmeltingRecipe> SMELTING;
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.BlastingRecipe> BLASTING;
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.SmokingRecipe> SMOKING;
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.CampfireCookingRecipe> CAMPFIRE_COOKING;
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.StonecutterRecipe> STONECUTTING;
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.SmithingRecipe> SMITHING;
public static final net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.BrewingRecipe> BREWING;
public static <T extends net.minecraft.world.item.crafting.Recipe<?>> net.minecraft.world.item.crafting.RecipeType<T> register(java.lang.String);
static {};
public net.minecraft.world.item.crafting.RecipeSerializer(com.mojang.serialization.MapCodec<T>, net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T>);
public com.mojang.serialization.MapCodec<T> codec();
public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec();
``

### v0.2b RecipeManager / RecipeInput
`` 
public <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>> java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<T>> getRecipeFor(net.minecraft.world.item.crafting.RecipeType<T>, I, net.minecraft.world.level.Level, net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>>);
public <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>> java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<T>> getRecipeFor(net.minecraft.world.item.crafting.RecipeType<T>, I, net.minecraft.world.level.Level, net.minecraft.world.item.crafting.RecipeHolder<T>);
public <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>> java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<T>> getRecipeFor(net.minecraft.world.item.crafting.RecipeType<T>, I, net.minecraft.world.level.Level);
public java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> byKey(net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>>);
public net.minecraft.world.item.crafting.SelectableRecipe$SingleInputSet<net.minecraft.world.item.crafting.StonecutterRecipe> getSynchronizedStonecutterRecipes();
public net.minecraft.world.item.crafting.SelectableRecipe$SingleInputSet<net.minecraft.world.item.crafting.StonecutterRecipe> stonecutterRecipes();
public java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<?>> getRecipes();
public java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<?>> getLearnableRecipes();
public final class net.minecraft.world.item.crafting.SingleRecipeInput extends java.lang.Record implements net.minecraft.world.item.crafting.RecipeInput {
public net.minecraft.world.item.crafting.SingleRecipeInput(net.minecraft.world.item.ItemStack);
public net.minecraft.world.item.ItemStack getItem(int);
public int size();
public final java.lang.String toString();
public final int hashCode();
public final boolean equals(java.lang.Object);
public net.minecraft.world.item.ItemStack item();
``

### v0.2b AbstractFurnaceBlockEntity（参考：バニラ機械の実装）
`` 
public abstract class net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity extends net.minecraft.world.level.block.entity.BaseContainerBlockEntity implements net.minecraft.world.WorldlyContainer,net.minecraft.world.inventory.StackedContentsCompatible,net.minecraft.world.inventory.RecipeCraftingHolder {
protected static final int SLOT_INPUT;
protected static final int SLOT_FUEL;
protected static final int SLOT_RESULT;
public static final int DATA_LIT_TIME;
public static final int DATA_LIT_DURATION;
public static final int DATA_COOKING_PROGRESS;
public static final int DATA_COOKING_TOTAL_TIME;
public static final int NUM_DATA_VALUES;
public static final int BURN_TIME_STANDARD;
public static final int BURN_COOL_SPEED;
protected net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items;
protected final net.minecraft.world.inventory.ContainerData dataAccess;
protected net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, net.minecraft.world.item.crafting.RecipeType<? extends net.minecraft.world.item.crafting.AbstractCookingRecipe>);
protected void loadAdditional(net.minecraft.world.level.storage.ValueInput);
protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput);
public static void serverTick(net.minecraft.server.level.ServerLevel, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity);
protected int getBurnDuration(net.minecraft.server.level.ServerLevel, net.minecraft.world.item.ItemStack);
protected float getSpeedMultiplier(net.minecraft.server.level.ServerLevel, net.minecraft.world.item.ItemStack);
public int[] getSlotsForFace(net.minecraft.core.Direction);
public boolean canPlaceItemThroughFace(int, net.minecraft.world.item.ItemStack, net.minecraft.core.Direction);
public boolean canTakeItemThroughFace(int, net.minecraft.world.item.ItemStack, net.minecraft.core.Direction);
public int getContainerSize();
protected net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> getItems();
protected void setItems(net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack>);
public void setItem(int, net.minecraft.world.item.ItemStack);
public boolean canPlaceItem(int, net.minecraft.world.item.ItemStack);
public void setRecipeUsed(net.minecraft.world.item.crafting.RecipeHolder<?>);
public net.minecraft.world.item.crafting.RecipeHolder<?> getRecipeUsed();
public void awardUsedRecipes(net.minecraft.world.entity.player.Player, java.util.List<net.minecraft.world.item.ItemStack>);
``

### v0.2b 流体 FlowingFluid / BucketItem
`` 
public abstract class net.minecraft.world.level.material.FlowingFluid extends net.minecraft.world.level.material.Fluid {
public abstract net.minecraft.world.level.material.Fluid getFlowing();
public abstract net.minecraft.world.level.material.Fluid getSource();
protected abstract boolean canConvertToSource(net.minecraft.server.level.ServerLevel);
protected abstract void beforeDestroyingBlock(net.minecraft.world.level.LevelAccessor, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState);
protected abstract int getSlopeFindDistance(net.minecraft.world.level.LevelReader);
protected abstract int getDropOff(net.minecraft.world.level.LevelReader);
public abstract int getAmount(net.minecraft.world.level.material.FluidState);
public net.minecraft.world.item.BucketItem(net.minecraft.world.level.material.Fluid, net.minecraft.world.item.Item$Properties);
``

## Screen（#7 で確定・動作確認後に✅）
- 背景：`extractBackground(GuiGraphicsExtractor g, int, int, float)` をオーバーライド → `g.blit(RenderPipelines.GUI_TEXTURED, Identifier, x, y, u, v, w, h, 256, 256)`
- 塗り：`g.fill(x1, y1, x2, y2, argb)`／文字：`g.text(font, Component, x, y, argb, shadow)`
- ラベル：`extractLabels(GuiGraphicsExtractor, int, int)`
- 登録：`MenuScreens.register(ModMenus.X, Screen::new)`（ClientModInitializer 内）
- 右クリックで開く：Block で `getMenuProvider` ＋ `useWithoutItem` → `player.openMenu(be)`。BlockEntity が `MenuProvider` を実装
- SimpleContainer.addListener は廃止 → 匿名サブクラスで setChanged() を上書きする
- 本を渡す：loot table の set_components で written_book_content は動かず（形不明）。進捗 rewards.function → mcfunction の give コマンド（SNBT: pages:[{translate:...}]）で動作。コマンド内の文章は JSON 文字列ではなく SNBT 形式

## v0.2a 先行調査（2026-10-19）

### BlockApiLookup
`` 
public static <A, C> net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup<A, C> get(net.minecraft.resources.Identifier, java.lang.Class<A>, java.lang.Class<C>);
public default A find(net.minecraft.world.level.Level, net.minecraft.core.BlockPos, C);
public abstract A find(net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.block.entity.BlockEntity, C);
public abstract void registerSelf(net.minecraft.world.level.block.entity.BlockEntityType<?>...);
public abstract void registerForBlocks(net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup$BlockApiProvider<A, C>, net.minecraft.world.level.block.Block...);
public default <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerForBlockEntity(java.util.function.BiFunction<? super T, C, A>, net.minecraft.world.level.block.entity.BlockEntityType<T>);
public abstract void registerForBlockEntities(net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup$BlockEntityApiProvider<A, C>, net.minecraft.world.level.block.entity.BlockEntityType<?>...);
public abstract void registerFallback(net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup$BlockApiProvider<A, C>);
``

### Level 近隣アクセス
`` 
public net.minecraft.world.level.block.state.BlockState getBlockState(net.minecraft.core.BlockPos);
public net.minecraft.world.level.material.FluidState getFluidState(net.minecraft.core.BlockPos);
public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(net.minecraft.core.BlockPos);
public boolean isLoaded(net.minecraft.core.BlockPos);
``

### Direction
`` 
public static final net.minecraft.core.Direction DOWN;
public static final net.minecraft.core.Direction UP;
public static final net.minecraft.core.Direction NORTH;
public static final net.minecraft.core.Direction SOUTH;
public static final net.minecraft.core.Direction WEST;
public static final net.minecraft.core.Direction EAST;
public static final net.minecraft.util.StringRepresentable$EnumCodec<net.minecraft.core.Direction> CODEC;
public static final com.mojang.serialization.Codec<net.minecraft.core.Direction> VERTICAL_CODEC;
public static final java.util.function.IntFunction<net.minecraft.core.Direction> BY_ID;
public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, net.minecraft.core.Direction> STREAM_CODEC;
public static final com.mojang.serialization.Codec<net.minecraft.core.Direction> LEGACY_ID_CODEC;
public static final com.mojang.serialization.Codec<net.minecraft.core.Direction> LEGACY_ID_CODEC_2D;
public static net.minecraft.core.Direction[] values();
public net.minecraft.core.Direction getOpposite();
``

### BlockPos.relative
`` 
public net.minecraft.core.BlockPos above();
public net.minecraft.core.BlockPos above(int);
public net.minecraft.core.BlockPos below();
public net.minecraft.core.BlockPos below(int);
public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction);
public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction, int);
public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction$Axis, int);
public net.minecraft.core.Vec3i relative(net.minecraft.core.Direction$Axis, int);
public net.minecraft.core.Vec3i relative(net.minecraft.core.Direction, int);
public net.minecraft.core.Vec3i relative(net.minecraft.core.Direction);
public net.minecraft.core.Vec3i below(int);
public net.minecraft.core.Vec3i below();
public net.minecraft.core.Vec3i above(int);
public net.minecraft.core.Vec3i above();
``

### FluidState
`` 
public boolean isSource();
public boolean isSourceOfType(net.minecraft.world.level.material.Fluid);
public boolean isEmpty();
public float getHeight(net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos);
public float getHeightForCamera(net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos);
public int getAmount();
public net.minecraft.world.phys.Vec3 getFlow(net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos);
``

### Fluids
`` 
public static final net.minecraft.world.level.material.Fluid EMPTY;
public static final net.minecraft.world.level.material.FlowingFluid FLOWING_WATER;
public static final net.minecraft.world.level.material.FlowingFluid WATER;
public static final net.minecraft.world.level.material.FlowingFluid FLOWING_LAVA;
public static final net.minecraft.world.level.material.FlowingFluid LAVA;
``

### RuleTest の種類（predicate_type 名の候補）
`` 
NotRuleTest.class
TagMatchTest.class
BlockStateMatchTest.class
AlwaysTrueTest.class
RandomBlockStateMatchTest.class
HeightMatchTest.class
BlockMatchTest.class
PosAlwaysTrueTest.class
AnyOfRuleTest.class
LinearPosTest.class
AxisAlignedLinearPosTest.class
RuleTest.class
PosRuleTest.class
AllOfRuleTest.class
RandomBlockMatchTest.class
``

### バニラ ore_quartz.json（block_match の見本）
`` 
{
  "type": "minecraft:ore",
  "discard_chance_on_air_exposure": 0.0,
  "size": 14,
  "targets": [
    {
      "state": "minecraft:nether_quartz_ore",
      "target": {
        "block": "minecraft:netherrack",
        "predicate_type": "minecraft:block_match"
      }
    }
  ]
}
``

### バニラ disk_sand or ore_gravel（砂の置換の見本）
`` 
{
  "type": "minecraft:ore",
  "discard_chance_on_air_exposure": 0.0,
  "size": 33,
  "targets": [
    {
      "state": "minecraft:gravel",
      "target": {
        "predicate_type": "minecraft:tag_match",
        "tag": "minecraft:base_stone_overworld"
      }
    }
  ]
}
``

### BiomeSelectors
`` 
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> all();
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> vanilla();
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> foundInOverworld();
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> foundInTheNether();
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> foundInTheEnd();
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> tag(net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome>);
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> excludeByKey(net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>...);
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> excludeByKey(java.util.Collection<net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>>);
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> includeByKey(net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>...);
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> includeByKey(java.util.Collection<net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>>);
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> spawnsOneOf(net.minecraft.world.entity.EntityType<?>...);
public static java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> spawnsOneOf(java.util.Set<net.minecraft.world.entity.EntityType<?>>);
``

### BiomeTags（浜辺）
`` 
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> IS_DEEP_OCEAN;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> IS_OCEAN;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> IS_BEACH;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> IS_RIVER;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> HAS_OCEAN_MONUMENT;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> HAS_OCEAN_RUIN_COLD;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> HAS_OCEAN_RUIN_WARM;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> HAS_RUINED_PORTAL_OCEAN;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> HAS_SHIPWRECK_BEACHED;
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> REQUIRED_OCEAN_MONUMENT_SURROUNDING;
``

### BucketItem / Items.WATER_BUCKET
`` 
public net.minecraft.world.item.BucketItem(net.minecraft.world.level.material.Fluid, net.minecraft.world.item.Item$Properties);
public net.minecraft.world.level.ClipContext$Fluid getFluidContext();
public static final net.minecraft.world.item.Item BUCKET;
public static final net.minecraft.world.item.Item WATER_BUCKET;
public static final net.minecraft.world.item.Item LAVA_BUCKET;
public static final net.minecraft.world.item.Item POWDER_SNOW_BUCKET;
public static final net.minecraft.world.item.Item MILK_BUCKET;
public static final net.minecraft.world.item.Item PUFFERFISH_BUCKET;
public static final net.minecraft.world.item.Item SALMON_BUCKET;
public static final net.minecraft.world.item.Item COD_BUCKET;
public static final net.minecraft.world.item.Item TROPICAL_FISH_BUCKET;
public static final net.minecraft.world.item.Item AXOLOTL_BUCKET;
public static final net.minecraft.world.item.Item SULFUR_CUBE_BUCKET;
public static final net.minecraft.world.item.Item TADPOLE_BUCKET;
``
- Player.displayClientMessage(Component, boolean) は廃止 → player.sendSystemMessage(Component)（1引数、チャット表示）

### [6-01] 電解槽で使った API（javap 確認済み）
``
// StateHolder（BlockState の親）
public boolean hasProperty(net.minecraft.world.level.block.state.properties.Property<?>);
public <T extends Comparable<T>, V extends T> S setValue(Property<T>, V);
// ItemStack
public static boolean isSameItemSameComponents(ItemStack, ItemStack);
public void grow(int); public void shrink(int);
// ItemStack implements ItemInstance … getMaxStackSize() は ItemInstance の default メソッドに移動（ItemStack 自体の宣言にはない）
public default int getMaxStackSize();
// Containers
public static void dropContents(Level, BlockPos, NonNullList<ItemStack>);
``

## 自動テスト（GameTest）— 2026-10-19 javap / ソースで確認、実際に動作
### Loom 1.18.4（instrumented-fabric-loom-1.18.4.jar）
``
// build.gradle で使う拡張：fabricApi
public interface net.fabricmc.loom.api.fabricapi.FabricApiExtension {
  void configureTests(); void configureTests(Action<GameTestSettings>);
  void configureDataGeneration(...);
}
public interface net.fabricmc.loom.api.fabricapi.GameTestSettings {
  Property<Boolean> getCreateSourceSet();       // 既定 false。true で src/gametest/ ソースセットを作る
  Property<String>  getModId();                  // テスト用 MOD の id（src/gametest/resources/fabric.mod.json と一致させる）
  Property<Boolean> getEnableGameTests();        // 既定 true → run 設定 "gameTest" ＝ タスク runGameTest。check タスクにもぶら下がる
  Property<Boolean> getEnableClientGameTests();  // 既定 true → runClientGameTest（画面が開くので false にした）
  Property<Boolean> getEula();                   // 既定 false。true で eula.txt を自動作成
  Property<Boolean> getClearRunDirectory();      // 既定 true（build/run/gameTest を毎回消す）
  Property<String>  getUsername();               // 既定 "Player0"
}
// FabricApiTesting の定数：run 名 "gameTest"、runDir "build/run/gameTest"、vmArg -Dfabric-api.gametest
``
- タスク名は **runGameTest**（G と T が大文字。runGametest ではない）

### fabric-gametest-api-v1 4.0.34（sources jar で確認）
``
@interface net.fabricmc.fabric.api.gametest.v1.GameTest {
  String environment() default "minecraft:default"; String structure() default "fabric-gametest-api-v1:empty"; // 8x8 の空き
  int maxTicks() default 20; int setupTicks() default 0; boolean required() default true;
  boolean skyAccess() default false; int padding() default 1; int maxAttempts() default 1; ...
}
// メソッドは public・非 static・void・引数 GameTestHelper 1つ。クラスは fabric.mod.json の "fabric-gametest" エントリポイントに書く
// テスト名 = <modid>:<クラス名_メソッド名 のスネークケース>（例 semiconductor-lab-gametest:lab_tests_block_drops）
``
### net.minecraft.gametest.framework.GameTestHelper（26.3）
``
ServerLevel getLevel(); BlockPos absolutePos(BlockPos);  <T extends BlockEntity> T getBlockEntity(BlockPos, Class<T>);
void setBlock(BlockPos, Block); void setBlock(BlockPos, BlockState); BlockState getBlockState(BlockPos);
void succeed(); void succeedWhen(Runnable); void runAfterDelay(long, Runnable); void runAtTickTime(long, Runnable);
void fail(String); void assertTrue(boolean, String); void assertFalse(boolean, String);
<N> void assertValueEqual(N, N, String); <T extends Comparable<T>> void assertBlockProperty(BlockPos, Property<T>, T);
void assertBlockPresent(Block, BlockPos); Player makeMockPlayer(GameType); long getTick();
``
### テストで使ったその他の API
``
// Registries.FEATURE は Registry<Feature>（旧 ConfiguredFeature が Feature に統合。Feature はインターフェース）
boolean Feature.place(WorldGenLevel, ChunkGenerator, RandomSource, BlockPos);
ServerChunkCache ServerLevel.getChunkSource(); ChunkGenerator ServerChunkCache.getGenerator();
<E> Registry<E> RegistryAccess.lookupOrThrow(ResourceKey<? extends Registry<? extends E>>);
T Registry.getValue(Identifier); boolean Registry.containsKey(Identifier);
Holder.Reference<T> HolderGetter.getOrThrow(ResourceKey<T>);
// レシピ・進捗・関数
Optional<RecipeHolder<?>> RecipeManager.byKey(ResourceKey<Recipe<?>>);   // ResourceKey.create(Registries.RECIPE, id)
AdvancementHolder ServerAdvancementManager.get(Identifier);  Collection<AdvancementHolder> getAllAdvancements();
Optional<CommandFunction<CommandSourceStack>> ServerFunctionManager.get(Identifier);
// ドロップ
static List<ItemStack> Block.getDrops(BlockState, ServerLevel, BlockPos, BlockEntity, Entity, ItemInstance);
void ItemStack.enchant(Holder<Enchantment>, int);   ResourceKey<Enchantment> Enchantments.SILK_TOUCH;
TagKey<Block> BlockTags.STONE_ORE_REPLACEABLES / DEEPSLATE_ORE_REPLACEABLES;
static RandomSource RandomSource.create(long);
``

### タスク01（2026-10-19 javap で確認・GameTest で使用）
``
// net.minecraft.world.item.crafting.PlacementInfo
public java.util.List<net.minecraft.world.item.crafting.Ingredient> ingredients();
public boolean isImpossibleToPlace();
// net.minecraft.world.item.crafting.Ingredient
public boolean test(net.minecraft.world.item.ItemStack);
// 使い方（LabTests.recipeUses）: recipeManager.byKey(key).map(r -> r.value().placementInfo().ingredients().stream().anyMatch(i -> i.test(stack)))
// かまど系の出力（LabTests.cook）: recipeManager.getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(stack), level).map(r -> r.value().assemble(input))
// Registry.keySet() → Set<Identifier>（BuiltInRegistries.ITEM.keySet() で全アイテム ID を列挙できた）
``


### タスク02（2026-10-19 javap で確認）
``
// net.minecraft.world.entity.player.Player
public void sendOverlayMessage(net.minecraft.network.chat.Component);   // アクションバー（ホットバーの上）に出す。sendSystemMessage はチャット
public void causeFoodExhaustion(float);                                   // 人力の機械でお腹を減らす
// net.minecraft.world.level.storage.ValueOutput / ValueInput
public abstract void putDouble(String, double);  public abstract double getDoubleOr(String, double);
// net.minecraft.world.item.Item
public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext);   // ブロックに向けて右クリック
// net.minecraft.world.item.context.UseOnContext: getLevel() getClickedPos() getPlayer() getItemInHand() getHand() sendOverlayMessage(Component)
// net.minecraft.world.level.Level
public void playSound(Entity, BlockPos, SoundEvent, SoundSource, float, float);   // SoundEvents.PISTON_CONTRACT 実在
// net.minecraft.world.item.Items: COPPER_NUGGET, IRON_NUGGET, GLASS_BOTTLE, LEATHER, CINNABAR, GLASS_PANE 実在
``

## タスク03（2026-10-20）
新しい 26.3 API は使っていない（ConsumerBlockEntity・ConsumerMachineBlock・ContainerData など既存の形のみ）。
ConsumerBlockEntity に serverTick（tick 本体の上書き）・extraDataCount/extraData（GUI へ送る数値の追加）・setLit を足した（MOD 内の仕組み）。

## タスク06（2026-10-20 javap で確認）
``
// net.minecraft.world.level.block.entity.BlockEntity
public void setLevel(Level);  public void setRemoved();  public void clearRemoved();
protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter);   // アイテム → BE（置いたとき）
protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap$Builder);   // BE → アイテム（壊したとき、copy_components で）
public void removeComponentsFromTag(ValueOutput);   // コンポーネントに移した値を NBT から消す（out.discard("キー")）
public final void applyComponentsFromItemStack(ItemStack);   // テストで「置き直し」を再現できる
// net.minecraft.core.component.DataComponentGetter: <T> T get(DataComponentType<? extends T>)
// net.minecraft.core.component.DataComponentMap$Builder: <T> Builder set(DataComponentType<T>, T)
// net.minecraft.world.level.storage.ValueOutput: void discard(String)
// net.minecraft.network.codec.ByteBufCodecs: LONG, VAR_LONG
// net.minecraft.world.level.block.state.BlockBehaviour
protected void onPlace(BlockState, Level, BlockPos, BlockState old, boolean movedByPiston);
protected void neighborChanged(BlockState, Level, BlockPos, Block, net.minecraft.world.level.redstone.Orientation, boolean);
``
- loot_table の copy_components（バニラ shulker_box.json で確認）: `"modifier": {"type":"minecraft:copy_components","source":"block_entity","include":["semiconductor-lab:battery_energy"]}`（modifier は配列でなく1つのオブジェクトでも可）

## タスク07（2026-10-20）
- 新しい API はなし。反射炉は既存の形だけで書いた（HorizontalDirectionalBlock＋EntityBlock＝ConsumerMachineBlock と同じ、SimpleContainer / ContainerData / ValueInput.getIntOr / ValueOutput.putInt / preRemoveSideEffects＝ConsumerBlockEntity と同じ）
- Items.CALCITE / Items.TUFF（GameTest で使用、コンパイルで実在確認）

### タスク08 で確認（javap、26.3）
``
net.minecraft.world.level.biome.Biomes:  public static final ResourceKey<Biome> DESERT;   （PLAINS も同様）
BiomeSelectors.includeByKey(ResourceKey<Biome>...)   … fabric-biome-api-v1 20.1.0
net.minecraft.world.level.biome.Biome:  public BiomeGenerationSettings getGenerationSettings();
net.minecraft.world.level.biome.BiomeGenerationSettings:  public List<HolderSet<PlacedFeature>> features();   （BiomeModifications の追加はサーバー起動後ここに反映される＝GameTest で確認済み）
net.minecraft.resources.ResourceKey:  public Identifier identifier();   （旧 location()）
net.minecraft.world.item.ItemStack:  public void enchant(Holder<Enchantment>, int);
``


### タスク09 で確認（javap、26.3）
``
net.minecraft.world.level.BlockAndLightGetter:  public default boolean canSeeSky(BlockPos);   （空の明るさで判定＝光の計算が後から追いつくので、置いた直後の屋根に反応しない。塩田は高さマップを使う）
net.minecraft.world.level.LevelReader:  public abstract int getHeight(Heightmap.Types, int x, int z);  ／ Heightmap.Types.MOTION_BLOCKING
net.minecraft.world.level.LevelReader:  public default Holder<Biome> getBiome(BlockPos);   （Holder.is(TagKey) で BiomeTags.IS_OCEAN / IS_BEACH / IS_BADLANDS）
net.minecraft.world.level.Level:  public boolean isRainingAt(BlockPos);  public boolean isRaining();  public boolean isBrightOutside();
BlockBehaviour.BlockStateBase:  public boolean propagatesSkylightDown();   （バリア・ガラスは true。GameTest の天井はバリア）
net.minecraft.world.item.Item$Properties:  craftRemainder(Item) / craftRemainder(ItemStackTemplate)   ／ Item.getCraftingRemainder() は ItemStackTemplate
net.minecraft.world.Container:  public default int countItem(Item);   （Inventory でも使える）
GameTestHelper:  public Player makeMockPlayer(GameType);
Player:  public void sendOverlayMessage(Component);   （既存の手押しポンプと同じ）
``
- GameTest の世界のバイオームは minecraft:desert だった（2026-10-20 のログ）。テスト場の上（y+7 付近）にバリアの天井がある
- 自分自身を返す「作業台で使っても戻る道具」：`Item.Properties.craftRemainder(Item)` は登録前の自分を渡せないので、Fabric の `net.fabricmc.fabric.api.item.v1.FabricItem`（Item にインターフェース注入済み、fabric-item-api-v1 14.7.1）の `default ItemStackTemplate getCraftingRemainder(ItemStack)` を上書きする（item/QuartzStillItem）。GameTest で `CraftingRecipe.getRemainingItems(CraftingInput)` に反映されることを確認（ge_cl4_route）
``
net.minecraft.world.item.ItemStackTemplate:  public ItemStackTemplate(Item);  (Item,int) / (Item, DataComponentPatch) / (Holder<Item>, int, DataComponentPatch)；create()
net.minecraft.world.item.crafting.CraftingRecipe:  public default NonNullList<ItemStack> getRemainingItems(CraftingInput);
net.minecraft.world.item.crafting.CraftingInput:  public static CraftingInput of(int w, int h, List<ItemStack>);
``

## タスク12（2026-10-20）
- GameTestHelper（javap 確認）：`destroyBlock(BlockPos)`（相対座標、ドロップあり）／`assertItemEntityPresent(Item, BlockPos, double)`／`assertItemEntityCountIs(Item, BlockPos, double, int)`
- RecipeManager（javap 確認、タスク13）：`getRecipes()` → `Collection<RecipeHolder<?>>`、`RecipeHolder.id()` は `ResourceKey<Recipe<?>>`（`.identifier()` で ID）
