package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
	/**
	 * Hurts the entities near the rocket, as its blast does.
	 */
	@Invoker("dealExplosionDamage")
	void originofspawns$dealExplosionDamage(ServerLevel level);
}
