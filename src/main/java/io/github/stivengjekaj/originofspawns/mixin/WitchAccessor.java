package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Witch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Witch.class)
public interface WitchAccessor {
	/**
	 * Sets the ticks of the drink that are left.
	 */
	@Accessor("usingTime")
	void originofspawns$setUsingTime(int ticks);

	/**
	 * Returns the speed modifier that slows a witch while it drinks.
	 */
	@Accessor("SPEED_MODIFIER_DRINKING")
	static AttributeModifier originofspawns$drinkingModifier() {
		throw new AssertionError();
	}
}
