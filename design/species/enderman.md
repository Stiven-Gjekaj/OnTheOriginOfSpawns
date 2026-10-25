# Enderman

This document defines the families and the genes of the enderman.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The enderman is a tall mob of the End. It has 40 health and does 7 melee
damage. It spawns in the dark in all three dimensions. It is neutral until a
player looks at its face or hurts it. It teleports up to 32 blocks away, and
it teleports away from each projectile. Water and rain hurt it. It picks up
some blocks, carries them, and puts them down. It attacks endermites. A
player with a carved pumpkin on the head does not make it angry.
Its genes fit the teleport, the stare, the carried block, the heavy fist,
and the fight against water. It shares no genes and no families with the
other species.

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

### Phasers

Phasers teleport to their target. Each teleport costs them some of their
body.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It teleports next to its target, one time each 6 seconds | 10% less maximum health |
| Tier 2 | It teleports next to its target, one time each 9 seconds | 15% less maximum health |
| Tier 1 | It teleports next to its target, one time each 12 seconds | 20% less maximum health |

### Gazers

Gazers hurt the mind of a player that looks at them. Their fists are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | A player that looks at its face gets Darkness for 4 seconds | 10% less damage |
| Tier 2 | A player that looks at its face gets Darkness for 3 seconds | 15% less damage |
| Tier 1 | A player that looks at its face gets Darkness for 2 seconds | 20% less damage |

### Haulers

Haulers throw the blocks that they carry. The weight makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It throws its block at a target 4 to 12 blocks away, one time each 8 seconds | 10% less speed |
| Tier 2 | It throws its block at a target 4 to 12 blocks away, one time each 10 seconds | 15% less speed |
| Tier 1 | It throws its block at a target 4 to 12 blocks away, one time each 12 seconds | 20% less speed |

### Bruisers

Bruisers hit hard. Each swing takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more melee damage | It attacks 10% slower |
| Tier 2 | 20% more melee damage | It attacks 15% slower |
| Tier 1 | 15% more melee damage | It attacks 20% slower |

### Rainwalkers

Rainwalkers resist water. Their body is thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 75% less damage from water and rain | 10% less maximum health |
| Tier 2 | 50% less damage from water and rain | 15% less maximum health |
| Tier 1 | 25% less damage from water and rain | 20% less maximum health |

## Pressures

The enderman uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `night`, `day`, `dry`, `wolf`, and
`shield`. The sun does not burn it, so it does not use `sunlight`. It needs
no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Phaser genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Far Step | Phaser | `unreached` | When it cannot reach its target, it teleports next to it, one time each 8 seconds | 10% less maximum health | Legs: purple sparks at the feet |
| Shadow Step | Phaser | `melee` | After it hits, it teleports behind its target, one time each 10 seconds | 10% less damage | Body: a dark shadow on the back |
| Pearl Heart | Phaser | `critical` | One time in its life, at under 25% health, it teleports away and heals 10 health | 4 less maximum health | Body: a pearl in the chest. This changes the model |
| Blast Step | Phaser | `explosion` | It teleports away from an explosion and takes no damage, one time each 20 seconds | 10% less maximum health | Arms: burn marks on the arms |
| Sudden Step | Phaser | `spotted` | Its first hit after a teleport does 30% more damage | 2 less armor | Head: purple eyes that glow brighter. This changes the model |
| Rain Step | Phaser, sub Rainwalker | `in_water` | In rain or water, it teleports to its target in place of a random place | 10% less maximum health | Legs: wet, dark legs |

### Gazer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Dread Gaze | Gazer | `spotted` | A player that looks at its face gets Darkness for 4 seconds | 10% less damage | Head: wide, white eyes |
| Chill Gaze | Gazer | `unreached` | A player that looks at its face gets Slowness II for 2 seconds | 10% less maximum health | Head: pale blue eyes |
| Mind Gaze | Gazer | `crowd` | A player that looks at its face gets Nausea for 4 seconds | 10% less maximum health | Head: spiral marks around the eyes |
| Far Gaze | Gazer | `alone` | It sees a stare from two times as far | 10% less speed | Head: a third eye on the forehead |
| Weak Gaze | Gazer, sub Bruiser | `melee` | A player that looks at its face gets Weakness for 4 seconds | 10% less speed | Head: gray, tired eyes |
| Hollow Eyes | Gazer | `shield` | A carved pumpkin does not stop its anger | 10% less damage | Head: empty, black eyes |

### Hauler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Block Throw | Hauler | `unreached` | It throws its block at a target 4 to 12 blocks away, one time each 8 seconds. The block does 4 damage | 10% less maximum health | Arms: strong, thick hands |
| Heavy Block | Hauler | `projectile` | Its thrown block does two times the damage | It throws 10% less often | Arms: stone dust on the arms |
| Stone Grip | Hauler | `golem` | It also picks up stone, cobblestone, and deepslate | 10% less speed | Arms: gray stone on the hands |
| Shield Breaker | Hauler | `shield` | Its thrown block stops a shield for 5 seconds | 10% less speed | Body: cracks on the chest |
| Burden | Hauler | `melee` | 4 more armor while it carries a block | 10% less speed | Body: a strap across the chest |
| Dust Throw | Hauler, sub Gazer | `spotted` | Its thrown block gives Blindness for 2 seconds | 10% less damage | Arms: sand on the hands |

### Bruiser genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Heavy Fist | Bruiser | `melee` | 20% more melee damage | It attacks 15% slower | Arms: large, dark fists. This changes the model |
| Void Punch | Bruiser | `critical` | Each hit has a 20% chance to teleport the target up to 8 blocks away | 10% less damage | Arms: purple veins |
| Ender Rage | Bruiser | `crowd` | 30% more speed while its health is under half | 10% less maximum health | Head: an open mouth |
| Stone Skin | Bruiser | `explosion` | 30% less damage from explosions | 10% less speed | Body: gray plates on the body |
| Wide Swing | Bruiser | `fall` | Its hits knock the target back two times as far | It attacks 10% slower | Arms: long, thin arms with scars |
| Long Reach | Bruiser, sub Phaser | `unreached` | It leaps at its target from 4 to 8 blocks away | 10% less maximum health | Legs: bent, strong legs |

### Rainwalker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Dry Skin | Rainwalker | `in_water` | 50% less damage from water and rain | 10% less speed | Body: a dry, cracked skin |
| Storm Stride | Rainwalker | `dry` | 30% more speed in the rain | 10% less maximum health | Legs: blue stripes |
| Splash Proof | Rainwalker | `projectile` | Splash potions of water do not hurt it | 10% less maximum health | Head: blue drops on the head |
| Wet Fury | Rainwalker | `melee` | 20% more damage in rain or water | 10% less maximum health | Arms: blue lines on the arms |
| Ice Veins | Rainwalker | `fire` | 50% less fire damage | 10% less speed | Body: pale blue veins |
| Mud Hands | Rainwalker, sub Hauler | `spotted` | In the rain, its thrown block gives Slowness for 3 seconds | 10% less damage | Arms: mud on the hands |

## Gene details

| Gene | Rules |
|---|---|
| Phasers, Far Step, Shadow Step, Rain Step | The teleport goes to a free place near the target. If it finds no free place, it does not teleport, and the cooldown does not start |
| Gazers, Dread Gaze, Chill Gaze, Mind Gaze, Weak Gaze | "Looks at its face" is the vanilla stare test. A carved pumpkin stops the effect, except with Hollow Eyes |
| Haulers, Block Throw | The thrown block is a falling block. It puts down the block where it lands, if the `mobGriefing` gamerule is on. Else it drops nothing. It throws only while it carries a block |
| Heavy Block, Shield Breaker, Dust Throw, Mud Hands | They change the thrown block of Haulers or Block Throw. Alone they do nothing |
| Stone Grip | It obeys the `mobGriefing` gamerule |
| Bruisers, Heavy Fist, Wide Swing | "Attacks slower" changes the time between two melee attacks |
| Ender Rage | "Its health is under half" is the `wounded` state |
| Long Reach | It works as the zombie Lunger |
| Wide Swing | It works as the vindicator Chop |
| Rainwalkers, Dry Skin | They change the damage from water, rain, and splash potions of water |
| Void Punch | It works as a chorus fruit on the target |

## Equipment

- The enderman holds no weapon. It carries one block, as in vanilla.
- When it dies, it drops its block and its ender pearl, as in vanilla.

## Conversion

- The enderman does not convert to a different species, and no species
  converts to the enderman.

## Babies and jockeys

- Endermen have no babies in vanilla. They do not get a gene for them.
- An enderman does not ride a mob in vanilla. It does not get a gene for
  this.

## Body

- The enderman has the enderman model: a long head, a thin body, and long
  arms and legs. Its eyes glow in the dark. Its texture is 64 x 32.
- It needs a new region map, `textures/region/enderman.png`.
- The gene models use the enderman bones: `head`, `body`, `right_arm`,
  `left_arm`, `right_leg`, and `left_leg`. Check them against the vanilla
  model, as for the witch.
- The marks on the eyes glow, as the vanilla eyes do.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Staring Phasers | Phaser | Gazer | Teleports and stares |
| Block Phasers | Phaser | Hauler | Teleports and throws |
| Strike Phasers | Phaser | Bruiser | Teleports and hits hard |
| Storm Phasers | Phaser | Rainwalker | Teleports in the rain |
| Haunting Gazers | Gazer | Phaser | A stare that follows |
| Rock Gazers | Gazer | Hauler | A stare and a thrown rock |
| Cruel Gazers | Gazer | Bruiser | A stare and a fist |
| Rain Gazers | Gazer | Rainwalker | A stare in the rain |
| Blink Haulers | Hauler | Phaser | Throws, then teleports |
| Dread Haulers | Hauler | Gazer | Throws at a dazed player |
| Rock Smashers | Hauler | Bruiser | Throws and hits hard |
| Mud Haulers | Hauler | Rainwalker | Throws in the rain |
| Void Brawlers | Bruiser | Phaser | Fists that teleport |
| Grim Brawlers | Bruiser | Gazer | Fists and a stare |
| Boulder Brawlers | Bruiser | Hauler | Fists and rocks |
| Storm Brawlers | Bruiser | Rainwalker | Fists in the rain |
| Tide Phasers | Rainwalker | Phaser | Rain and teleports |
| Tide Gazers | Rainwalker | Gazer | Rain and a stare |
| Tide Haulers | Rainwalker | Hauler | Rain and rocks |
| Tide Brawlers | Rainwalker | Bruiser | Rain and fists |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Phasers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Void Watchers | Gazer and Phaser | A stare that does not let go |
| Rift Throwers | Hauler and Phaser | Rocks from all sides |
| Void Fighters | Bruiser and Phaser | A fist from nowhere |
| Storm Shades | Phaser and Rainwalker | A shade in the storm |
| Stone Watchers | Gazer and Hauler | A stare and a rock |
| Dread Fists | Bruiser and Gazer | A stare and a fist |
| Storm Seers | Gazer and Rainwalker | A stare in the storm |
| Rock Giants | Bruiser and Hauler | Rocks and fists |
| Flood Haulers | Hauler and Rainwalker | Rocks in the rain |
| Storm Titans | Bruiser and Rainwalker | Fists in the storm |

## Neutral

A neutral enderman has no genes, or no family points.
It is a plain enderman, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| An enderman region map, and the gene textures and gene models on the enderman model, with glowing marks on the eyes | All visuals |
| A teleport next to the target, with a cooldown and a state | Phasers, Far Step, Rain Step |
| A teleport behind the target after a hit | Shadow Step |
| A one-time teleport and heal at low health | Pearl Heart |
| A teleport away from an explosion, with no damage | Blast Step |
| More damage on the first hit after a teleport | Sudden Step |
| Effects on a player that stares at it, and a stare range | Gazers, Dread Gaze, Chill Gaze, Mind Gaze, Weak Gaze, Far Gaze |
| A stare that a carved pumpkin does not stop | Hollow Eyes |
| A throw of the carried block, with damage, effects, and a shield stop | Haulers, Block Throw, Heavy Block, Shield Breaker, Dust Throw, Mud Hands |
| More blocks that it can pick up | Stone Grip |
| Armor while it carries a block | Burden |
| A hit that teleports the target | Void Punch |
| Less damage from water, rain, and splash potions of water | Rainwalkers, Dry Skin, Splash Proof |
| A check that the reused effects work on an enderman: attack interval, knockback, leap, speed when, damage dealt when, damage taken, and attributes | Many genes |
