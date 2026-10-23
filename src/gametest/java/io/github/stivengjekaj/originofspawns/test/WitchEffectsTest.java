package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.mixin.ProjectileAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the witch genes use: the thrown potions, the drinks, and the heal throw. It also tests that
 * the reused effects work on a witch.
 */
public class WitchEffectsTest {
	@GameTest
	public void longBrewMakesTheThrownEffectsLastLonger(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(2, 1, 2));
		Witch witch = witchWith(helper, new BlockPos(2, 1, 5), "test_long_brew");
		ItemStack poison = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON);
		helper.assertValueEqual(GeneBehaviors.brew(plain, poison).getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F),
				1.0F, "a plain witch does not change the time");
		helper.assertValueEqual(GeneBehaviors.brew(witch, poison).getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F),
				2.0F, "the effects last two times as long");
		helper.succeed();
	}

	@GameTest
	public void strongBrewMakesTheEffectsWithATimeOneLevelStronger(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2), "test_strong_brew");
		helper.assertValueEqual(effectsOf(GeneBehaviors.brew(witch, splash(Potions.POISON))).getFirst().getAmplifier(), 1,
				"Poison I becomes Poison II");
		helper.assertValueEqual(effectsOf(GeneBehaviors.brew(witch, splash(Potions.HARMING))).getFirst().getAmplifier(), 0,
				"Harming does not change");
		helper.succeed();
	}

	@GameTest
	public void blindingBrewThrowsBlindnessInPlaceOfSlowness(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2), "test_blinding_brew");
		List<MobEffectInstance> effects = effectsOf(GeneBehaviors.brew(witch, splash(Potions.SLOWNESS)));
		helper.assertValueEqual(effects.size(), 1, "one effect");
		helper.assertTrue(is(effects.getFirst(), MobEffects.BLINDNESS), "Blindness takes the place of Slowness");
		helper.assertValueEqual(effects.getFirst().getDuration(), 100, "for 5 seconds");
		helper.assertTrue(is(effectsOf(GeneBehaviors.brew(witch, splash(Potions.POISON))).getFirst(), MobEffects.POISON),
				"Poison does not change");
		helper.succeed();
	}

	@GameTest
	public void witherBrewAddsWitherToHarming(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2), "test_wither_brew");
		List<MobEffectInstance> effects = effectsOf(GeneBehaviors.brew(witch, splash(Potions.HARMING)));
		helper.assertValueEqual(effects.size(), 2, "Harming and Wither");
		helper.assertTrue(is(effects.getFirst(), MobEffects.INSTANT_DAMAGE), "the potion still harms");
		helper.assertTrue(is(effects.get(1), MobEffects.WITHER) && effects.get(1).getDuration() == 60,
				"Wither for 3 seconds");
		helper.assertValueEqual(effectsOf(GeneBehaviors.brew(witch, splash(Potions.POISON))).size(), 1,
				"Poison gets no Wither");
		helper.succeed();
	}

	@GameTest
	public void lingeringBrewThrowsALingeringPotionButNotAtARaider(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(2, 1, 1));
		Witch witch = witchWith(helper, new BlockPos(2, 1, 4), "test_lingering_brew");
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1));
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4));
		plain.performRangedAttack(plainTarget, 1.0F);
		AbstractThrownPotion plainPotion = potionOf(helper, plain);
		helper.assertFalse(plainPotion instanceof ThrownLingeringPotion, "a plain witch throws a splash potion");
		helper.assertFalse(plainPotion.hasAttached(ModAttachments.GENE_POTION), "a plain witch does not mark it");
		plainPotion.discard();
		witch.performRangedAttack(target, 1.0F);
		AbstractThrownPotion potion = potionOf(helper, witch);
		helper.assertTrue(potion instanceof ThrownLingeringPotion, "the witch throws a lingering potion");
		helper.assertTrue(potion.hasAttached(ModAttachments.GENE_POTION), "the potion carries the mark of the genes");
		potion.discard();
		Pillager raider = PillagerEffectsTest.pillagerWith(helper, new BlockPos(5, 1, 6));
		witch.performRangedAttack(raider, 1.0F);
		AbstractThrownPotion heal = potionOf(helper, witch);
		helper.assertFalse(heal instanceof ThrownLingeringPotion, "the heal of a raider stays a splash potion");
		helper.assertFalse(heal.hasAttached(ModAttachments.GENE_POTION), "the heal has no mark");
		heal.discard();
		helper.succeed();
	}

	@GameTest
	public void doubleThrowThrowsTwoPotionsALittleApart(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 3), "test_double_throw");
		// With no aim spread, only the angle of the gene parts the potions.
		GeneBehaviors.throwPotion(witch, helper.getLevel(), splash(Potions.POISON), 1.0, 0.2, 0.0, 0.75F, 0.0F);
		List<AbstractThrownPotion> potions = helper.getLevel().getEntitiesOfClass(AbstractThrownPotion.class,
				witch.getBoundingBox().inflate(4.0), potion -> potion.getOwner() == witch);
		helper.assertValueEqual(potions.size(), 2, "the witch throws two potions");
		Vec3 one = potions.get(0).getDeltaMovement();
		Vec3 two = potions.get(1).getDeltaMovement();
		double apart = Math.toDegrees(Math.abs(Math.atan2(one.z, one.x) - Math.atan2(two.z, two.x)));
		potions.forEach(AbstractThrownPotion::discard);
		helper.assertTrue(Math.abs(apart - 10.0) < 0.5, "the potions fly 10 degrees apart, " + apart);
		helper.succeed();
	}

	@GameTest
	public void wideSplashReachesFartherAndMakesAWiderCloud(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(1, 1, 1));
		Witch witch = witchWith(helper, new BlockPos(1, 1, 4), "test_wide_splash");
		// The two cases use one place, one after the other, so that the wide splash does not reach the first pig.
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		splashAt(helper, plain, new BlockPos(1, 1, 1), Items.SPLASH_POTION);
		helper.assertFalse(plainTarget.hasEffect(MobEffects.POISON), "a plain splash does not reach 5 blocks");
		plainTarget.discard();
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		splashAt(helper, witch, new BlockPos(1, 1, 1), Items.SPLASH_POTION);
		helper.assertTrue(target.hasEffect(MobEffects.POISON), "a wide splash reaches 5 blocks");
		target.discard();
		splashAt(helper, plain, new BlockPos(2, 1, 2), Items.LINGERING_POTION);
		splashAt(helper, witch, new BlockPos(2, 1, 5), Items.LINGERING_POTION);
		List<AreaEffectCloud> clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class,
				new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8.0));
		helper.assertValueEqual(clouds.stream().filter(cloud -> cloud.getOwner() == plain).findFirst().orElseThrow()
				.getRadius(), 3.0F, "a plain cloud starts at 3 blocks");
		helper.assertValueEqual(clouds.stream().filter(cloud -> cloud.getOwner() == witch).findFirst().orElseThrow()
				.getRadius(), 4.5F, "a wide cloud starts at 4.5 blocks");
		clouds.forEach(AreaEffectCloud::discard);
		helper.succeed();
	}

	/**
	 * Breaks a potion of Poison of the witch on the ground at the place.
	 */
	static void splashAt(GameTestHelper helper, Witch witch, BlockPos pos, net.minecraft.world.item.Item item) {
		ItemStack stack = PotionContents.createItemStack(item, Potions.POISON);
		AbstractThrownPotion potion = item == Items.LINGERING_POTION
				? new ThrownLingeringPotion(helper.getLevel(), witch, stack)
				: new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(helper.getLevel(),
						witch, stack);
		Vec3 place = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
		potion.setPos(place);
		((ProjectileAccessor) potion).originofspawns$onHit(
				new BlockHitResult(place, Direction.UP, helper.absolutePos(pos.below()), false));
	}

	static ItemStack splash(Holder<net.minecraft.world.item.alchemy.Potion> potion) {
		return PotionContents.createItemStack(Items.SPLASH_POTION, potion);
	}

	static List<MobEffectInstance> effectsOf(ItemStack stack) {
		List<MobEffectInstance> effects = new java.util.ArrayList<>();
		stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().forEach(effects::add);
		return effects;
	}

	static boolean is(MobEffectInstance instance, Holder<MobEffect> effect) {
		return instance.getEffect().value() == effect.value();
	}

	/**
	 * Returns the one potion that the witch threw. The test discards it before it flies out of the test area.
	 */
	static AbstractThrownPotion potionOf(GameTestHelper helper, Witch witch) {
		List<AbstractThrownPotion> potions = helper.getLevel().getEntitiesOfClass(AbstractThrownPotion.class,
				witch.getBoundingBox().inflate(4.0), potion -> potion.getOwner() == witch && !potion.isRemoved());
		helper.assertValueEqual(potions.size(), 1, "the witch threw one potion");
		return potions.getFirst();
	}

	static Witch witchWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Witch witch = EntityTypes.WITCH.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		witch.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		witch.setNoAi(true);
		witch.setPersistenceRequired();
		witch.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(witch);
		return witch;
	}
}
