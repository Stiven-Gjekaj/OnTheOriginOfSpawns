package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.evolution.goal.LeapGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Checks that the reused effects work on a species. Each check builds its own mobs with the maker of the species and
 * the test genes, and it keeps them inside the test area. A species test calls the checks that its design lists.
 */
final class ReusedEffects {
	/**
	 * Makes a mob of the species with the test genes and no AI.
	 */
	interface Maker {
		Mob at(GameTestHelper helper, BlockPos pos, String... genes);
	}

	private ReusedEffects() {
	}

	static void armorBreak(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 1), "test_black_rot");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		GeneCombat.breakArmor(mob, player, 4.0F, false);
		helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 1, "armor break wears the armor");
	}

	static void allyAura(GameTestHelper helper, Maker maker) {
		Mob leader = maker.at(helper, new BlockPos(1, 1, 1), "test_ally_speed");
		Mob ally = maker.at(helper, new BlockPos(3, 1, 1));
		leader.tickCount = 20;
		GeneBehaviors.tick(leader, helper.getLevel());
		helper.assertTrue(ally.hasEffect(MobEffects.SPEED), "the aura gives the ally Speed");
	}

	static void deathCloud(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_cloud");
		GeneCombat.onDeath(mob, helper.getLevel());
		List<AreaEffectCloud> clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class,
				mob.getBoundingBox().inflate(1.0));
		helper.assertValueEqual(clouds.size(), 1, "the dead mob leaves a cloud");
		clouds.forEach(AreaEffectCloud::discard);
	}

	static void damageToGolems(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(5, 1, 5), "test_golem_bane");
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(1, 1, 1));
		helper.assertValueEqual(GeneCombat.modifyDamage(golem, helper.getLevel().damageSources().mobAttack(mob), 4.0F),
				6.0F, "1.5 times the damage to a golem");
	}

	static void damageTaken(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_evoker_wards");
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(mob, helper.getLevel().damageSources().inFire(), 4.0F)
				- 2.2F) < 0.001F, "fire does half the damage, times 1.1");
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(mob, helper.getLevel().damageSources().generic(), 4.0F)
				- 4.4F) < 0.001F, "other damage is 1.1 times as much");
	}

	static void cleave(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 1), "test_cleave");
		ServerPlayer target = CreeperEffectsTest.survivalPlayer(helper).player();
		ServerPlayer near = CreeperEffectsTest.survivalPlayer(helper).player();
		target.snapTo(helper.absoluteVec(new Vec3(3.5, 1.0, 1.5)));
		near.snapTo(helper.absoluteVec(new Vec3(4.5, 1.0, 1.5)));
		float health = near.getHealth();
		GeneCombat.onHit(mob, target);
		helper.assertTrue(near.getHealth() < health, "the cleave hurts the near player");
	}

	static void parry(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_deflect");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		var hit = helper.getLevel().damageSources().mobAttack(pig);
		helper.assertTrue(GeneCombat.parries(mob, hit), "the mob parries the first hit");
		helper.assertFalse(GeneCombat.parries(mob, hit), "no parry in the cooldown");
	}

	static void firstStrike(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_ambush_bite");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		var hit = helper.getLevel().damageSources().mobAttack(mob);
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(player, hit, 10.0F) - 15.0F) < 0.001F, "the first hit");
		helper.assertValueEqual(GeneCombat.modifyDamage(player, hit, 10.0F), 10.0F, "the next hit");
	}

	static void leap(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 4));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		mob.setTarget(pig);
		mob.setOnGround(true);
		LeapGoal goal = new LeapGoal(mob, new GeneEffect.Leap(3.0F, 6.0F, 5.0F, 1.0F));
		helper.assertTrue(goal.canUse(), "the mob can leap at the pig");
		goal.start();
		helper.assertTrue(mob.getDeltaMovement().x > 0.5 && mob.getDeltaMovement().y > 0.0, "the mob leaps at the pig");
	}

	static void sidestep(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 1));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		player.setPos(mob.position().add(0.0, 0.0, 5.0));
		CreeperEffectsTest.face(player, 180.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		player.startUsingItem(InteractionHand.MAIN_HAND);
		Vec3 before = mob.position();
		helper.assertTrue(GeneBehaviors.sidestep(mob, helper.getLevel(), new GeneEffect.Sidestep(12.0F, 3.0F, 10.0F, true)),
				"the mob steps aside from the aimed bow");
		helper.assertValueEqual(Math.round(Math.abs(mob.getX() - before.x)), 3L, "the side distance");
	}

	static void callOnHurt(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 4), "test_alarm_bell");
		Mob ally = maker.at(helper, new BlockPos(4, 1, 4));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		GeneCombat.onHurt(mob, helper.getLevel().damageSources().mobAttack(pig));
		helper.assertTrue(ally.getTarget() == pig, "the call makes the ally attack the attacker");
	}

	static void effectOnHit(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_slime_hits");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		GeneCombat.onHit(mob, pig);
		helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "the hit gives Slowness");
	}

	static void igniteTarget(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_flame_touch");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		GeneCombat.onHit(mob, pig);
		helper.assertValueEqual(pig.getRemainingFireTicks(), 100, "the hit burns the target for 5 seconds");
	}

	static void meleeDisarm(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_sword_slap");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		GeneCombat.onHit(mob, player);
		helper.assertTrue(player.getMainHandItem().isEmpty(), "a melee hit makes the player drop the sword");
	}

	static void translucent(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_always_translucent");
		helper.assertTrue(GeneEffects.translucentOpacity(mob).isPresent(), "the mob is translucent");
	}

	static void seeInvisible(GameTestHelper helper, Maker maker) {
		Mob sees = maker.at(helper, new BlockPos(1, 1, 1), "test_blind_hunter");
		Mob plain = maker.at(helper, new BlockPos(1, 1, 5));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 1, 3))));
		player.setInvisible(true);
		helper.assertValueEqual(GeneBehaviors.visibility(player, sees, 0.1), 1.0, "the mob sees the invisible player");
		helper.assertValueEqual(GeneBehaviors.visibility(player, plain, 0.1), 0.1, "a plain mob does not");
	}

	static void groupedAndAlone(GameTestHelper helper, Maker maker) {
		// The alone state counts the mobs in 8 blocks, so the lone mob is the only one at first.
		Mob lone = maker.at(helper, new BlockPos(7, 1, 7), "test_alone_weak");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		helper.assertValueEqual(GeneCombat.modifyDamage(player, helper.getLevel().damageSources().mobAttack(lone), 10.0F),
				5.0F, "half the damage alone");
		lone.discard();
		Mob pack = maker.at(helper, new BlockPos(1, 1, 1), "test_pack_bite");
		helper.assertValueEqual(GeneCombat.modifyDamage(player, helper.getLevel().damageSources().mobAttack(pack), 10.0F),
				10.0F, "no extra damage with no ally near");
		maker.at(helper, new BlockPos(2, 1, 1));
		maker.at(helper, new BlockPos(1, 1, 2));
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(player, helper.getLevel().damageSources().mobAttack(pack), 10.0F)
				- 12.0F) < 0.001F, "20% more damage in a group");
	}

	static void callOnSight(GameTestHelper helper, Maker maker) {
		Mob caller = maker.at(helper, new BlockPos(1, 1, 4), "test_call_sight");
		Mob ally = maker.at(helper, new BlockPos(6, 1, 4));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 1))));
		caller.setTarget(player);
		caller.tickCount = 20;
		GeneBehaviors.tick(caller, helper.getLevel());
		helper.assertTrue(ally.getTarget() == player, "the call makes the ally target the player");
	}

	static void deathEffectOnAllies(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 1), "test_death_allies");
		Mob ally = maker.at(helper, new BlockPos(3, 1, 1));
		GeneCombat.onDeath(mob, helper.getLevel());
		helper.assertTrue(ally.hasEffect(MobEffects.RESISTANCE), "the dead mob gives its ally Resistance");
	}

	static void rally(GameTestHelper helper, Maker maker, String gene) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), gene);
		maker.at(helper, new BlockPos(2, 1, 4));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(pig, helper.getLevel().damageSources().mobAttack(mob), 10.0F)
				- 10.5F) < 0.001F, "5% more damage for the one ally");
	}

	static void throwEffect(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 1), "test_hatchet");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 1));
		mob.setTarget(pig);
		var hatchet = new GeneEffect.ThrowEffect(MobEffects.SLOWNESS, 3.0F, 0, 10.0F, 8.0F, 4.0F);
		helper.assertTrue(GeneBehaviors.throwEffect(mob, helper.getLevel(), hatchet), "the mob throws at the target");
		helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "the throw gives Slowness");
	}

	static void knockback(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 4), "test_chop");
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		GeneCombat.onHit(mob, target);
		helper.assertTrue(Math.abs(target.getDeltaMovement().x - 0.4) < 0.001, "the hit adds one more push away");
	}

	static void healOnHit(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(1, 1, 4), "test_bloodlust");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		mob.setHealth(10.0F);
		GeneCombat.onHit(mob, pig);
		helper.assertValueEqual(mob.getHealth(), 11.0F, "each hit heals 1 health");
	}

	static void healOnce(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_pearl");
		mob.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), mob.getMaxHealth() * 0.8F);
		helper.assertValueEqual(mob.getHealth(), mob.getMaxHealth(), "the first heal to full health");
	}

	/**
	 * Waits until the regeneration heals a mob that is out of combat.
	 */
	static void regenerate(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_regen");
		mob.setHealth(mob.getMaxHealth() / 2.0F);
		float start = mob.getHealth();
		helper.succeedWhen(() -> helper.assertTrue(mob.getHealth() > start, "the mob heals out of combat"));
	}

	static void thorns(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_coral");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		float health = pig.getHealth();
		GeneCombat.onHurt(mob, helper.getLevel().damageSources().mobAttack(pig));
		helper.assertValueEqual(pig.getHealth(), health - 1.0F, "a melee attacker takes 1 damage");
	}

	static void armorAttribute(GameTestHelper helper, Maker maker) {
		Mob plain = maker.at(helper, new BlockPos(1, 1, 1));
		Mob skin = maker.at(helper, new BlockPos(5, 1, 5), "test_basalt_skin");
		// The game puts the attribute genes on a new mob when it rolls the genes. The test mob gets its genes direct.
		GeneEffects.applyAttributes(skin, GeneEffects.sources(helper.getLevel().registryAccess(),
				skin.getAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.GENES)));
		helper.assertValueEqual(skin.getArmorValue(), plain.getArmorValue() + 4, "the gene adds 4 armor");
	}

	/**
	 * Waits until the attribute when gene gives a wounded mob 6 more armor.
	 */
	static void armorWhenWounded(GameTestHelper helper, Maker maker) {
		Mob plain = maker.at(helper, new BlockPos(1, 1, 1));
		Mob crust = maker.at(helper, new BlockPos(5, 1, 5), "test_crust");
		helper.onEachTick(() -> crust.setHealth(crust.getMaxHealth() * 0.3F));
		helper.succeedWhen(() -> helper.assertValueEqual(crust.getArmorValue(), plain.getArmorValue() + 6,
				"a wounded mob has 6 more armor"));
	}

	/**
	 * Waits until the speed when gene of a wounded mob makes it 30% faster.
	 */
	static void speedWhenWounded(GameTestHelper helper, Maker maker) {
		Mob mob = maker.at(helper, new BlockPos(4, 1, 4), "test_escape_artist");
		helper.onEachTick(() -> mob.setHealth(mob.getMaxHealth() * 0.3F));
		helper.succeedWhen(() -> {
			var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
			helper.assertTrue(Math.abs(speed.getValue() - speed.getBaseValue() * 1.3) < 0.0001,
					"a wounded mob is 30% faster");
		});
	}
}
