package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LargeFireball.class)
public interface LargeFireballAccessor {
	/**
	 * Makes the fireball hit now, as when it touches a block or an entity.
	 */
	@Invoker("onHit")
	void originofspawns$onHit(HitResult hit);
}
