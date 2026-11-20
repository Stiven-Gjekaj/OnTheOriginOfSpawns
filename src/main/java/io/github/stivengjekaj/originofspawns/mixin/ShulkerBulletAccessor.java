package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.projectile.ShulkerBullet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerBullet.class)
public interface ShulkerBulletAccessor {
	@Accessor("targetDeltaX")
	double originofspawns$targetDeltaX();

	@Accessor("targetDeltaY")
	double originofspawns$targetDeltaY();

	@Accessor("targetDeltaZ")
	double originofspawns$targetDeltaZ();
}
