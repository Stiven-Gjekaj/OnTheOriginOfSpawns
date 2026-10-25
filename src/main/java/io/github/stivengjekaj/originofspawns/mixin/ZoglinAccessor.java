package io.github.stivengjekaj.originofspawns.mixin;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zoglin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Zoglin.class)
public interface ZoglinAccessor {
	@Invoker("findNearestValidAttackTarget")
	static Optional<? extends LivingEntity> originofspawns$findNearestValidAttackTarget(ServerLevel level, Mob mob) {
		throw new AssertionError();
	}
}
