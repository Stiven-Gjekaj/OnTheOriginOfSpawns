package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Lets the genes of a shulker change its attack.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Shulker$ShulkerAttackGoal")
public abstract class ShulkerAttackGoalMixin {
	@Shadow
	private int attackTime;

	/**
	 * After each shot, the throw rate genes of the shulker change the wait to the next shot. The vanilla wait is 1 to 5.5
	 * seconds.
	 */
	@ModifyArg(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private Entity originofspawns$afterShot(Entity bullet) {
		if (bullet instanceof ShulkerBullet shot && shot.getOwner() instanceof Shulker shulker) {
			this.attackTime = GeneBehaviors.scaleThrowInterval(shulker, this.attackTime);
		}
		return bullet;
	}
}
