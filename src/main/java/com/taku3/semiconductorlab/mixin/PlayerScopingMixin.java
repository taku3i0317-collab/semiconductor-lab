package com.taku3.semiconductorlab.mixin;

import com.taku3.semiconductorlab.item.TelescopeItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** [7-04] 屈折望遠鏡（TelescopeItem）を使っている間も「のぞいている」ことにする（バニラは Items.SPYGLASS だけ） */
@Mixin(Player.class)
public abstract class PlayerScopingMixin {
	@Inject(method = "isScoping", at = @At("RETURN"), cancellable = true)
	private void semilab$telescope(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) return;
		Player self = (Player) (Object) this;
		if (self.isUsingItem() && self.getUseItem().getItem() instanceof TelescopeItem) cir.setReturnValue(true);
	}
}
