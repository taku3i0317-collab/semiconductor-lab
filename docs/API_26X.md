# API_26X.md — Minecraft 26.3 / Fabric で javap により確認した API の形

ルール：javap で調べたら必ずここに追記する。推測で書かない。「？」は未確認。
調べ方：`javap -cp <jar> <クラス名> | Select-String <パターン>`（jar は .gradle\loom-cache 内の minecraft-common / clientOnly）

## 3年前から変わった点（まとめ）
- worldgen: configured_feature フォルダ/レジストリは feature に改名（26.1）。feature JSON は "config": {} の入れ子がなくなり、type と同じ階層に書く。targets の state は {"Name":…} ではなく文字列 "modid:block" で書く
- recipe / advancement / loot_table は単数形（1.21〜）
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
- 保存：`saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`。`out.putInt` / `in.getIntOr(key, def)`
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

## Screen（#7 で確認・一部未確認）
- `AbstractContainerScreen(T menu, Inventory, Component title)`／`init()`／`imageWidth`, `leftPos`, `topPos`, `titleLabelX/Y`, `inventoryLabelX/Y`
- 登録：`MenuScreens.register(MenuType, ScreenConstructor)`
- ★ `GuiGraphics` は廃止 → `GuiGraphicsExtractor`。描画システムは renderpearl
- ★ `renderBg` / `renderLabels` は廃止 → `extractRenderState(GuiGraphicsExtractor, int, int, float)`／`extractLabels(GuiGraphicsExtractor, int, int)`／`extractTooltip(...)`
- `RenderPipelines.GUI_TEXTURED` が存在（blit の引数に使う可能性）
- ？ GuiGraphicsExtractor の blit / 文字描画の関数名 → 下の調査ログ参照

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
