package com.taku3.semiconductorlab.mixin;

import com.taku3.semiconductorlab.entity.Domestication;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** [0-17] 家畜化：繁殖で生まれた子に世代を付ける（中身は entity/Domestication） */
@Mixin(Animal.class)
public abstract class AnimalBreedingMixin {
	@Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
	private void semilab$domestication(ServerLevel level, Animal partner, AgeableMob child, CallbackInfo ci) {
		Animal self = (Animal) (Object) this;
		ServerPlayer player = self.getLoveCause() != null ? self.getLoveCause() : partner.getLoveCause();
		Domestication.onBred(level, self, partner, child, player);
	}
}
