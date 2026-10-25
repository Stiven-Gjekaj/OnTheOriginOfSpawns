package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@Shadow
	@Final
	private @Nullable Entity source;

	/**
	 * The genes of the mob that shot a wind charge change the push of its blast. The game sends the same push to the
	 * client of a player, so the change acts on the stored push.
	 */
	@ModifyVariable(method = "hurtEntities", at = @At("STORE"), ordinal = 2)
	private Vec3 originofspawns$windPush(Vec3 knockback, @Local Entity entity) {
		Vec3 push = GeneBehaviors.windPush(source, entity, knockback);
		GeneBehaviors.windBlastEffects(source, entity, push);
		return push;
	}
}
