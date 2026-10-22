# Vindicator

This document defines the families and the genes of the vindicator.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The vindicator is an illager with an iron axe. It has 24 health. It lives in
woodland mansions and comes in raids, where it breaks doors. Its arms are
crossed until it attacks. A vindicator with the name Johnny attacks all mobs.
Its genes fit the heavy blow of the axe, the rage of the berserker, the siege
of the sapper, the raid on the village, and the guard of the mansion. It
shares no genes and no families with the other species.

## Summary

| Item | Number |
|---|---|
| Major families | 5 |
| Genes | 30, 6 for each major family |
| Genes with a sub tag | 5 |
| Minor families | 20 |
| Pure families | 5 |
| Hybrids | 10 |

## Major families

Each major family has one big buff and one big debuff, at 3 tiers.
A higher tier makes the buff stronger and the debuff weaker.

### Butchers

Butchers swing the axe with full force. Each swing takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more melee damage | It attacks 10% slower |
| Tier 2 | 20% more melee damage | It attacks 15% slower |
| Tier 1 | 15% more melee damage | It attacks 20% slower |

### Berserkers

Berserkers fight harder when they bleed. They do not guard themselves.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 40% more damage while its health is under half | 10% more damage taken |
| Tier 2 | 30% more damage while its health is under half | 15% more damage taken |
| Tier 1 | 20% more damage while its health is under half | 20% more damage taken |

### Sappers

Sappers break through doors and walls. The work makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It breaks doors and weak blocks in its way in 3 seconds | 10% less speed |
| Tier 2 | It breaks doors and weak blocks in its way in 6 seconds | 15% less speed |
| Tier 1 | It breaks doors and weak blocks in its way in 10 seconds | 20% less speed |

### Ransackers

Ransackers hunt the village. Against a player, they are weaker.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 50% more damage to villagers and golems | 10% less damage to players |
| Tier 2 | 40% more damage to villagers and golems | 15% less damage to players |
| Tier 1 | 30% more damage to villagers and golems | 20% less damage to players |

### Guardsmen

Guardsmen block hits with the axe. Their guard makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It blocks one melee hit each 6 seconds | 10% less speed |
| Tier 2 | It blocks one melee hit each 9 seconds | 15% less speed |
| Tier 1 | It blocks one melee hit each 12 seconds | 20% less speed |

## Pressures

The vindicator uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `night`, `day`, `wolf`, `shield`, and
`raid`. The sun does not burn it, so it does not use `sunlight`. It needs no
new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Butcher genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Heavy Swing | Butcher | `melee` | 20% more melee damage | It attacks 15% slower | Arms: thick, scarred forearms |
| Sunder | Butcher, sub Sapper | `shield` | Its hits damage the armor of a player two times as much | 10% less speed | Hands: iron knuckles |
| Gash | Butcher | `unreached` | Its hits give Slowness II for 2 seconds | 10% less damage | Body: a bloody apron. This changes the model |
| Hatchet | Butcher | `projectile` | Each 8 seconds, it throws a hatchet at a player 4 to 10 blocks away. The hatchet gives Slowness for 3 seconds | 10% less maximum health | Back: a row of hatchets on the belt. This changes the model |
| Chop | Butcher | `fall` | Its hits knock the target back two times as far | It attacks 10% slower | Arms: rolled-up sleeves |
| Decapitator | Butcher | `critical` | 30% more damage to a player under half health | 10% less damage to a player over half health | Head: a black hood with eye holes. This changes the model |

### Berserker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Bloodlust | Berserker | `melee` | Each hit heals it 1 health | 2 less armor | Head: red war paint |
| Rage | Berserker | `crowd` | 30% more speed while its health is under half | 10% less maximum health | Head: bared teeth |
| Death Wish | Berserker, sub Guardsman | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Body: many old scars |
| Howl | Berserker | `alone` | Players in 6 blocks get Slowness for 3 seconds, each 15 seconds | Players hear it from two times farther | Head: a wide, open mouth |
| Fury | Berserker | `spotted` | It attacks 20% faster | 10% less maximum health | Arms: red veins |
| Thick Blood | Berserker | `fire` | It heals 1 health each 3 seconds out of combat | 25% more fire damage | Body: dark red cloth |

### Sapper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Door Splitter | Sapper | `unreached` | It breaks a door in 2 seconds | 10% less speed | Arms: wood splinters on the sleeves |
| Shatterer | Sapper | `alone` | Its hits on blocks also break glass and fences | 10% less damage | Hands: heavy gloves |
| Torchbearer | Sapper, sub Ransacker | `fire` | Each hit has a 30% chance to set the target on fire for 4 seconds | 25% more fire damage | Back: a torch on the back. This changes the model |
| Climber | Sapper | `fall` | It climbs walls | 10% less maximum health | Hands: climbing hooks |
| Battering | Sapper | `golem` | Full knockback resistance | 10% less speed | Body: a padded gambeson |
| Netter | Sapper | `spotted` | Its hits put a cobweb at the target with a 15% chance | 10% less damage | Body: a coil of rope. This changes the model |

### Ransacker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Village Bane | Ransacker | `crowd` | Two times the damage to villagers | 10% less damage to players | Head: a torn villager hat. This changes the model |
| Golem Smasher | Ransacker | `golem` | Two times the damage to iron golems and snow golems | 10% less speed | Hands: iron-gray fist wraps |
| Looting | Ransacker | `alone` | It picks up items | 10% less speed | Back: a sack of loot. This changes the model |
| Ruthless | Ransacker, sub Butcher | `critical` | Its first hit on a player does 30% more damage | 2 less armor | Head: a cold, narrow stare |
| Hunting Call | Ransacker | `spotted` | When it sees a player, it calls the vindicators in 24 blocks | Players hear it from two times farther | Body: a horn on a cord |
| Pillage Run | Ransacker | `projectile` | 20% more speed | 10% less maximum health | Legs: muddy boots |

### Guardsman genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Deflect | Guardsman | `melee` | It blocks one melee hit each 8 seconds | 10% less damage | Arms: a steel bracer. This changes the model |
| Bodyguard | Guardsman | `crowd` | It shares the damage that it takes with the vindicators in 8 blocks | 10% less speed | Body: a gray tabard |
| Watchman | Guardsman | `spotted` | It sees invisible players in 16 blocks | 10% less speed | Body: a lantern at the belt. This changes the model |
| Stand Ground | Guardsman | `explosion` | Full knockback resistance | 10% less speed | Legs: heavy greaves |
| Bracer | Guardsman, sub Berserker | `projectile` | 30% less damage from projectiles | 15% less speed | Arms: thick leather bracers |
| Alarm Bell | Guardsman | `alone` | When something hurts it, it calls the vindicators in 16 blocks | 10% less damage | Body: a small bell on the belt |

## Gene details

| Gene | Rules |
|---|---|
| Butchers, Heavy Swing, Fury, Chop | "Attacks slower" or "faster" changes the time between two melee attacks |
| Berserkers, Rage | "Its health is under half" is the `wounded` state |
| Sappers, Door Splitter, Shatterer | They work as the zombie Siege. They obey the `mobGriefing` gamerule. "Weak blocks" are the blocks in the tag `#originofspawns:siege_breakable` |
| Ransackers, Village Bane | "Villagers" are the villager and the wandering trader. "Golems" are the iron golem and the snow golem |
| Guardsmen, Deflect | They work as the wither skeleton Parry |
| Hatchet | It works as the husk Sandblast, with Slowness in place of Blindness. The hatchet makes no item |
| Decapitator | The buff and the debuff count only while the target of the vindicator is a player |
| Thick Blood | "Out of combat" means 5 seconds with no hit |
| Netter | It works as the spider Weavers, with the same cobwebs |
| Looting | It works as the pillager Plunder |
| Ruthless | It works as the spider Trapdoors |
| Bodyguard | It works as the spider Hive Mind |

## Equipment

- A vindicator keeps its iron axe. The axe is its natural weapon.
- Mobs with genes do not spawn with random armor. A vindicator with genes has
  no random enchantment on its axe from its spawn.
- In a raid, the vanilla raid enchantment stays: Sharpness.
- A vindicator cannot be the leader of a raid wave. Only a pillager with a
  Captain gene can be, as the pillager design says.
- The name Johnny works as in vanilla.

## Conversion

- The vindicator does not convert to a different species, and no species
  converts to the vindicator.

## Babies and jockeys

- Vindicators have no babies in vanilla. They do not get a gene for them.
- A vindicator can ride a ravager in a raid, as in vanilla. It does not get a
  gene for this.

## Body

- The vindicator has the illager model, with crossed arms when it does not
  attack. Its texture is 64 x 64.
- It uses the illager region map, `textures/region/illager.png`.
- The gene models use the illager bones. A model on an arm shows only while
  the arms are apart, when it attacks.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Blood Axes | Butcher | Berserker | Heavy blows in a rage |
| Wreckers | Butcher | Sapper | Heavy blows that break doors |
| Raze Axes | Butcher | Ransacker | Heavy blows on the village |
| Headsmen | Butcher | Guardsman | Heavy blows behind a guard |
| Mad Axes | Berserker | Butcher | Rage with heavy blows |
| Door Bashers | Berserker | Sapper | Rage that breaks doors |
| Village Furies | Berserker | Ransacker | Rage on the village |
| Iron Wills | Berserker | Guardsman | Rage that holds the line |
| Breach Cutters | Sapper | Butcher | Sieges with heavy blows |
| Mad Sappers | Sapper | Berserker | Sieges in a rage |
| Gatecrashers | Sapper | Ransacker | Sieges on the village |
| Siege Guards | Sapper | Guardsman | Guarded sieges |
| Reavers | Ransacker | Butcher | Heavy blows on the village |
| Marauders | Ransacker | Berserker | A raging attack on the village |
| Sackers | Ransacker | Sapper | A village that falls |
| Raid Wardens | Ransacker | Guardsman | Guarded attacks on the village |
| Halberdiers | Guardsman | Butcher | A guard with heavy blows |
| Stalwarts | Guardsman | Berserker | A guard that does not fall |
| Sentry Sappers | Guardsman | Sapper | A guard that breaks in |
| Manor Guards | Guardsman | Ransacker | The guards of the mansion |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Butchers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Axe Maniacs | Berserker and Butcher | Heavy blows in a rage |
| Demolishers | Butcher and Sapper | Heavy blows that break walls |
| Village Butchers | Butcher and Ransacker | Heavy blows on the village |
| Champions | Butcher and Guardsman | Heavy blows behind a guard |
| Rampagers | Berserker and Sapper | A rage that breaks walls |
| Village Burners | Berserker and Ransacker | A rage on the village |
| Bloodguards | Berserker and Guardsman | A guard in a rage |
| Siege Raiders | Ransacker and Sapper | A siege on the village |
| Fortress Breakers | Guardsman and Sapper | A guarded siege |
| Mansion Lords | Guardsman and Ransacker | The masters of the mansion |

## Neutral

A neutral vindicator has no genes, or no family points.
It is a plain vindicator, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the vindicator | All visuals |
| A melee knockback change | Chop |
| More damage to villagers, with a new entity tag for the villager and the wandering trader, and the snow golem in the golem tag | Ransackers, Village Bane, Golem Smasher |
| Less damage to players only | Ransackers |
| A siege that also breaks glass and fences | Shatterer |
| A check that the reused effects work on a vindicator: throw, parry, siege, web on hit, heal on hit, pick up items, first strike, share damage, calls, climb, and the wounded state | Many genes |
