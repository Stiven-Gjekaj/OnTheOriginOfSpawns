# Shulker

This document defines the families and the genes of the shulker.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The shulker is a box that guards the End cities. It has 30 health. It sits on
a block and opens its lid to shoot. Its bullet follows its target, does 4
damage, and gives Levitation for 10 seconds. While it is closed, it has 20
armor, and arrows do not hurt it. When something hurts it under half health,
it teleports with a 25% chance. When a shulker bullet hits an open shulker,
it teleports, and a new shulker can split off it. The shulker is a rare mob.
Thus it has 15 genes, not 30, as [DESIGN.md](../DESIGN.md) says. Its genes
fit the bullet, the lift, the shell, the teleport, and the split. It shares no
genes and no families with the other species.

## Summary

| Item | Number |
|---|---|
| Major families | 5 |
| Genes | 15, 3 for each major family |
| Genes with a sub tag | 5 |
| Minor families | 20 |
| Pure families | 5 |
| Hybrids | 10 |

## Major families

Each major family has one big buff and one big debuff, at 3 tiers.
A higher tier makes the buff stronger and the debuff weaker.

### Gunners

Gunners shoot hard bullets. Their shells are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its bullets do 30% more damage | 10% less maximum health |
| Tier 2 | Its bullets do 20% more damage | 15% less maximum health |
| Tier 1 | Its bullets do 15% more damage | 20% less maximum health |

### Lifters

Lifters lift their prey high. Their bullets are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its bullets give Levitation for 20 seconds | Its bullets do 10% less damage |
| Tier 2 | Its bullets give Levitation for 15 seconds | Its bullets do 15% less damage |
| Tier 1 | Its bullets give Levitation for 13 seconds | Its bullets do 20% less damage |

### Bunkers

Bunkers keep a hard shell, also while open. They shoot slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | While it is open, it has 8 armor | It shoots 10% slower |
| Tier 2 | While it is open, it has 4 armor | It shoots 15% slower |
| Tier 1 | While it is open, it has 3 armor | It shoots 20% slower |

### Shifters

Shifters teleport at any health. Their shells close badly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When something hurts it, it teleports with a 50% chance, at any health | 4 less armor while it is closed |
| Tier 2 | When something hurts it, it teleports with a 35% chance, at any health | 6 less armor while it is closed |
| Tier 1 | When something hurts it, it teleports with a 30% chance, at any health | 8 less armor while it is closed |

### Budders

Budders split off new shulkers. Each one is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When something hurts it, a new shulker splits off it with a 10% chance | 10% less maximum health |
| Tier 2 | When something hurts it, a new shulker splits off it with a 5% chance | 15% less maximum health |
| Tier 1 | When something hurts it, a new shulker splits off it with a 4% chance | 20% less maximum health |

## Pressures

The shulker uses `projectile`, `melee`, `explosion`, `unreached`, `spotted`,
`alone`, `crowd`, `critical`, and `player_death`. It lives in the End, so it
does not use `fire`, `sunlight`, `night`, `day`, or the biome pressures. It
does not move, so it does not use `fall` or `in_water`. It needs no new
pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Gunner genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Hot Shot | Gunner | `unreached` | Its bullets do 2 more damage | 10% less maximum health | Head: an orange head |
| Double Shot | Gunner | `spotted` | Each third attack shoots 2 bullets | 10% less maximum health | Lid: two dark rings on the lid |
| Seeker Shot | Gunner, sub Lifter | `projectile` | Its bullets fly 30% faster | 10% less maximum health | Head: pale yellow eyes |

### Lifter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| High Lift | Lifter | `critical` | Its bullets give Levitation for 20 seconds | Its bullets do 10% less damage | Lid: white spots on the lid |
| Heavy Air | Lifter | `melee` | Its bullets also give Weakness for 6 seconds | Its bullets do 10% less damage | Head: a gray head |
| Steep Lift | Lifter, sub Gunner | `alone` | Its bullets give Levitation II, not Levitation I | Its bullets do 10% less damage | Base: light blue bands on the base |

### Bunker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Thick Lid | Bunker | `projectile` | 30% less damage from projectiles while it is open | It shoots 10% slower | Lid: a thick, dark rim on the lid |
| Blast Lid | Bunker | `explosion` | Half the damage from explosions | It shoots 10% slower | Base: sooty patches on the base |
| Snap Lid | Bunker, sub Gunner | `melee` | When a player is next to it, it snaps its lid on the player for 4 damage, one time each 3 seconds | It shoots 10% slower | Lid: teeth on the edge of the lid |

### Shifter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Quick Blink | Shifter | `critical` | When something hurts it, it teleports with a 50% chance, at any health | 10% less maximum health | Base: purple sparks on the base |
| Sight Blink | Shifter | `unreached` | When it cannot see its target for 5 seconds, it teleports to a place that sees the target | 10% less maximum health | Head: wide, pale eyes |
| Blink Shut | Shifter, sub Bunker | `spotted` | After it teleports, it stays closed for 3 seconds | 10% less maximum health | Lid: a purple seam on the lid |

### Budder genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Split Reach | Budder | `crowd` | Its split works with up to 8 shulkers near it, not 5 | 10% less maximum health | Base: small buds on the base |
| Heir Shell | Budder | `alone` | A shulker that splits off it gets its genes, not the genes of the pool | 10% less maximum health | Lid: a pale cross on the lid |
| Scatter Bud | Budder, sub Shifter | `player_death` | A shulker that splits off it teleports at once to a free place in 8 blocks | 10% less maximum health | Head: a pink head |

## Gene details

| Gene | Rules |
|---|---|
| Gunners, Hot Shot | The vanilla bullet does 4 damage |
| Double Shot | The two bullets follow the same target |
| Seeker Shot | A faster bullet is harder to hit with a sword or an arrow |
| Lifters, High Lift | The vanilla Levitation from a bullet lasts 10 seconds |
| Steep Lift | Levitation II lifts the player two times as fast |
| Bunkers, Thick Lid | While it is closed, it has 20 armor, and arrows do not hurt it, as in vanilla |
| Snap Lid | It snaps only while it is open, and not while it shoots |
| Shifters, Quick Blink | In vanilla, it teleports only under half health, with a 25% chance. The teleport is the vanilla teleport, in 8 blocks |
| Sight Blink | It teleports in 8 blocks, as in vanilla |
| Budders | The split works as the vanilla split from a bullet. The new shulker has the genes of the pool |
| Split Reach | In vanilla, the chance of a split goes down with each shulker in 8 blocks, and it stops at 5 |
| Heir Shell | This also works for the vanilla split from a bullet |

## Equipment

- The shulker holds nothing. It drops its vanilla items.

## Conversion

- The shulker does not convert to a different species, and no species
  converts to the shulker.

## Babies and jockeys

- The shulker has no babies. A split is not a baby. It does not get a gene
  for babies.
- A shulker does not ride a mob. It does not get a gene for this.

## Spawning

- A shulker that spawns in an End city gets genes as a natural spawn.
- A shulker that splits off another shulker gets the genes of the pool,
  unless the other shulker has Heir Shell.

## Body

- The shulker has the shulker model: a lid, a base, and a head. Its texture
  is 64 x 64.
- A shulker has one of 17 colors. The marks must be easy to see on each
  color.
- It needs a new region map, `textures/region/shulker.png`. The design uses
  the regions lid, base, and head.
- No gene changes the model.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Lifting Gunners | Gunner | Lifter | A hard bullet and a long lift |
| Walled Gunners | Gunner | Bunker | A hard bullet and a hard shell |
| Shifting Gunners | Gunner | Shifter | A hard bullet and a quick teleport |
| Budding Gunners | Gunner | Budder | A hard bullet and a split |
| Armed Lifters | Lifter | Gunner | A long lift and a hard bullet |
| Walled Lifters | Lifter | Bunker | A long lift and a hard shell |
| Shifting Lifters | Lifter | Shifter | A long lift and a quick teleport |
| Budding Lifters | Lifter | Budder | A long lift and a split |
| Armed Bunkers | Bunker | Gunner | A hard shell and a hard bullet |
| Lifting Bunkers | Bunker | Lifter | A hard shell and a long lift |
| Shifting Bunkers | Bunker | Shifter | A hard shell and a quick teleport |
| Budding Bunkers | Bunker | Budder | A hard shell and a split |
| Armed Shifters | Shifter | Gunner | A quick teleport and a hard bullet |
| Lifting Shifters | Shifter | Lifter | A quick teleport and a long lift |
| Walled Shifters | Shifter | Bunker | A quick teleport and a hard shell |
| Budding Shifters | Shifter | Budder | A quick teleport and a split |
| Armed Budders | Budder | Gunner | A split and a hard bullet |
| Lifting Budders | Budder | Lifter | A split and a long lift |
| Walled Budders | Budder | Bunker | A split and a hard shell |
| Shifting Budders | Budder | Shifter | A split and a quick teleport |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Gunners".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Sky Turrets | Gunner and Lifter | A hard bullet and a long lift |
| Pillboxes | Gunner and Bunker | A hard bullet and a hard shell |
| Phase Turrets | Gunner and Shifter | A hard bullet and a quick teleport |
| Battery Shells | Gunner and Budder | A hard bullet and a split |
| Float Walls | Lifter and Bunker | A long lift and a hard shell |
| Drift Blinkers | Lifter and Shifter | A long lift and a quick teleport |
| Cloud Buds | Lifter and Budder | A long lift and a split |
| Vault Doors | Bunker and Shifter | A hard shell and a quick teleport |
| Shell Hoards | Bunker and Budder | A hard shell and a split |
| Warp Broods | Shifter and Budder | A quick teleport and a split |

## Neutral

A neutral shulker has no genes, or no family points.
It is a plain shulker, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A shulker region map, and the gene textures on the shulker model, for each of the 17 colors | All visuals |
| The damage, the speed, and the effects of a bullet, and two bullets in one attack | Gunners, Lifters, Hot Shot, Double Shot, Seeker Shot, High Lift, Heavy Air, Steep Lift |
| The time between two attacks | Bunkers and the downsides |
| Armor while it is open and while it is closed, and less projectile damage while it is open | Bunkers, Shifters, Thick Lid |
| A snap of the lid on a player next to it | Snap Lid |
| The chance of a teleport at any health, a teleport to a place that sees the target, and a closed shell after a teleport | Shifters, Quick Blink, Sight Blink, Blink Shut |
| A split when something hurts it, the limit of the split, the genes of the split, and a teleport of the split | Budders, Split Reach, Heir Shell, Scatter Bud |
| A check that the damage taken effects work on a shulker | Blast Lid |
