package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Projectile.class)
public interface ProjectileAccessor {
	/**
	 * Lets the projectile hit as if it flew into the hit.
	 */
	@Invoker("onHit")
	void originofspawns$onHit(HitResult hit);
}
