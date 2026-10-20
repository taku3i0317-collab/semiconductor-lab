package com.taku3.semiconductorlab.mixin;

import com.taku3.semiconductorlab.item.Retting;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** [0-06] 水に落ちている亜麻の茎を浸水させる（中身は item/Retting） */
@Mixin(ItemEntity.class)
public abstract class ItemEntityRettingMixin {
	@Unique private int semilab$soaked;

	@Inject(method = "tick", at = @At("TAIL"))
	private void semilab$retting(CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (self.isRemoved() || self.level().isClientSide()) return;
		semilab$soaked = Retting.tick(self, semilab$soaked);
	}
}
