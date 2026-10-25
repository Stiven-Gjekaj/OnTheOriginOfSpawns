# Breeze

This document defines the families and the genes of the breeze.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The breeze is a mob of wind from the trial chambers. It has 30 health. It
jumps far and fast. It shoots wind charges that push the target back and do
1 damage. A projectile does not hurt it, because the wind turns the
projectile away. It does not take fall damage. A trial spawner makes it. It
drops breeze rods. Its genes fit its wind charges, its guard, its jumps, the
speed of its wind, and storms. It shares no genes and no families with the
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

### Gusters

Gusters shoot strong wind charges. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its wind charges push 50% farther | 10% less maximum health |
| Tier 2 | Its wind charges push 30% farther | 15% less maximum health |
| Tier 1 | Its wind charges push 20% farther | 20% less maximum health |

### Deflectors

Deflectors guard well. They move slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% less damage from melee hits | 10% less movement speed |
| Tier 2 | 20% less damage from melee hits | 15% less movement speed |
| Tier 1 | 15% less damage from melee hits | 20% less movement speed |

### Bounders

Bounders jump far. Their wind charges are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its jumps go 50% farther | Its wind charges push 10% less far |
| Tier 2 | Its jumps go 30% farther | Its wind charges push 15% less far |
| Tier 1 | Its jumps go 20% farther | Its wind charges push 20% less far |

### Whirlers

Whirlers shoot often. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It shoots 30% more often | 10% more damage taken |
| Tier 2 | It shoots 20% more often | 15% more damage taken |
| Tier 1 | It shoots 15% more often | 20% more damage taken |

### Tempests

Tempests make wind that hurts. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its wind charges do 3 more damage | 10% less maximum health |
| Tier 2 | Its wind charges do 2 more damage | 15% less maximum health |
| Tier 1 | Its wind charges do 1 more damage | 20% less maximum health |

## Pressures

The breeze uses `melee`, `explosion`, `unreached`, `spotted`, `alone`,
`crowd`, `critical`, `golem`, `player_death`, `shield`, `fire`, and
`in_water`. A projectile does not hurt it, so it does not use `projectile`.
It does not take fall damage, so it does not use `fall`. It lives in the
trial chambers, under the ground, so it does not use `sunlight`, `night`, or
`day`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Guster genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Hard Gust | Guster | `melee` | Its wind charges push two times farther | 10% less maximum health | Head: a wide, open mouth |
| Lift Gust | Guster | `unreached` | Its wind charges throw the target up 4 blocks | 10% less maximum health | Rods: pale wind curls round the rods. This changes the model |
| Shield Gust | Guster | `shield` | Its wind charges stop a shield for 3 seconds | 10% less movement speed | Head: a sharp, blue brow |
| Golem Gust | Guster | `golem` | Its wind charges push golems two times farther | 10% less maximum health | Wind: gray, heavy wind |
| Disarm Gust | Guster | `spotted` | Its wind charges make a player drop the item in the off hand, one time each 20 seconds | 10% less maximum health | Head: narrow, white eyes |
| Wide Gust | Guster, sub Tempest | `crowd` | Its wind charges have a 50% larger blast | 10% less movement speed | Wind: wide, spread wind. This changes the model |

### Deflector genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Wind Mirror | Deflector | `unreached` | A projectile that it sends back does two times the damage, and it flies straight at the shooter | 10% less maximum health | Rods: silver, bright rods |
| Gale Guard | Deflector | `melee` | When a melee hit hurts it, it pushes the attacker back | 10% less movement speed | Wind: a thick ring of wind |
| Blast Calm | Deflector | `explosion` | Half the damage from explosions | 10% less movement speed | Head: a calm, closed mouth |
| Fire Shear | Deflector | `fire` | Fire and lava do half the damage | 10% less maximum health | Rods: orange, burnt tips |
| Steel Core | Deflector | `golem` | 30% less damage from golems | 10% less movement speed | Head: a gray, iron brow |
| Last Draft | Deflector, sub Bounder | `critical` | At under 30% health, it jumps far away from the target | 10% less maximum health | Rods: rods that point down |

### Bounder genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sky Hop | Bounder | `unreached` | Its jumps go 50% higher | Its wind charges push 10% less far | Rods: long, thin rods. This changes the model |
| Fast Spring | Bounder | `spotted` | 30% less time between two jumps | 10% less maximum health | Head: wide, bright eyes |
| Crash Down | Bounder | `crowd` | When it lands from a jump, the mobs and players in 3 blocks take 3 damage | 10% less maximum health | Wind: a ring of dust at the base |
| Side Hop | Bounder | `melee` | It jumps aside from a melee hit, one time each 8 seconds | 10% less maximum health | Rods: rods that lean out |
| Swim Hop | Bounder | `in_water` | It jumps out of water at full speed | 10% less movement speed | Wind: blue, wet wind |
| Wind Ride | Bounder, sub Whirler | `alone` | It shoots a wind charge in the air at the top of each jump | Its jumps go 10% less far | Head: a mark of three lines |

### Whirler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rapid Wind | Whirler | `spotted` | It shoots 30% more often | 10% less maximum health | Wind: fast, thin streaks |
| Twin Charge | Whirler | `unreached` | It shoots two wind charges at once, a little apart | It shoots 10% less often | Head: two small mouths. This changes the model |
| Spin Up | Whirler | `melee` | Each hit that it takes makes it shoot 10% more often, up to 40%, for 10 seconds | 10% less maximum health | Rods: rods with a spiral mark |
| True Aim | Whirler | `alone` | Its wind charges fly straight at the target, with no spread | 10% less movement speed | Head: a blue mark between the eyes |
| Fast Charge | Whirler | `crowd` | Its wind charges fly 50% faster | 10% less maximum health | Wind: a pointed tip of wind |
| Eye of Storm | Whirler, sub Deflector | `player_death` | Each 10 seconds, it shoots a wind charge at each player in 16 blocks | 10% less maximum health | Head: a dark ring round the eyes |

### Tempest genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Cut Wind | Tempest | `melee` | Its wind charges do 4 more damage | 10% less maximum health | Wind: sharp, white edges |
| Chill Wind | Tempest | `spotted` | Its wind charges give Slowness for 3 seconds | 10% less movement speed | Wind: pale blue wind |
| Dizzy Wind | Tempest | `unreached` | Its wind charges give Nausea for 4 seconds | 10% less maximum health | Head: spiral eyes |
| Choke Wind | Tempest | `crowd` | Its wind charges give Weakness for 4 seconds | 10% less maximum health | Head: a gray, tight mouth |
| Thunder Call | Tempest | `player_death` | Each 30 seconds, lightning hits a player in 16 blocks that it can see the sky above | 10% less maximum health | Rods: yellow, charged tips |
| Storm Gale | Tempest, sub Guster | `critical` | At under 30% health, it makes a wind blast that pushes the mobs and players in 6 blocks away | 4 less maximum health | Wind: a dark, storm gray wind |

## Gene details

| Gene | Rules |
|---|---|
| Gusters, Hard Gust | "Push" is the knockback of the wind charge blast |
| Deflectors | A melee hit is a hit from a mob or a player at close range |
| Bounders, Sky Hop | "Jumps" is the long jump of the breeze |
| Whirlers, Rapid Wind | "Shoots more often" changes the time between two wind charges |
| Tempests, Cut Wind | The vanilla wind charge of the breeze does 1 damage |
| Lift Gust | It adds an upward push to the target that the wind charge hits |
| Shield Gust | It works as the slime Shield Melt |
| Golem Gust, Steel Core | "Golems" are the iron golem and the snow golem |
| Disarm Gust | The item falls to the ground near the player |
| Wide Gust | The blast is wider, but the push does not change |
| Wind Mirror | A vanilla breeze already sends each projectile back, at half speed and a little off the line |
| Gale Guard | The push is as strong as a vanilla wind charge, on the attacker only |
| Last Draft | It jumps one time in its life |
| Fast Spring | "Time between two jumps" is the rest after each jump |
| Crash Down | It does no damage to other breezes |
| Side Hop | It works as the pillager Evasion |
| Swim Hop | The vanilla breeze is slow in water |
| Twin Charge | The second wind charge follows the rules of the first |
| Spin Up | A new hit starts the 10 seconds again |
| True Aim | It works as the skeleton arrow spread at `0` |
| Eye of Storm | It works as the evoker Fang Line, with wind charges |
| Chill Wind, Dizzy Wind, Choke Wind | The effect goes to each mob or player that the blast pushes |
| Thunder Call | The lightning does the vanilla damage. It hits only a player under an open sky |
| Storm Gale | It works one time in its life |

## Equipment

- The breeze holds nothing. It drops its vanilla items.

## Conversion

- The breeze does not convert to a different species, and no species
  converts to the breeze.

## Babies and jockeys

- The breeze has no babies. It does not get a gene for them.
- A breeze does not ride a mob. It does not get a gene for this.

## Spawners

- A breeze from a trial spawner gets genes as a natural spawn. An ominous
  trial spawner does not change its genes.

## Body

- The breeze has the breeze model: a head, three rods, and a wind body
  below. The wind body is a second texture that moves. Its texture is 32 x
  32, and the wind texture is 128 x 128.
- It needs a new region map, `textures/region/breeze.png`. The design uses
  the regions head, rods, and wind. A wind mark goes on the wind texture.
- The gene models use the breeze bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Guarded Gusters | Guster | Deflector | Strong pushes and a firm guard |
| Bounding Gusters | Guster | Bounder | Strong pushes and far jumps |
| Whirling Gusters | Guster | Whirler | Strong pushes, shot fast |
| Stormy Gusters | Guster | Tempest | Strong pushes that hurt |
| Gusting Deflectors | Deflector | Guster | A firm guard and strong pushes |
| Bounding Deflectors | Deflector | Bounder | A firm guard and far jumps |
| Whirling Deflectors | Deflector | Whirler | A firm guard and fast fire |
| Stormy Deflectors | Deflector | Tempest | A firm guard and wind that hurts |
| Gusting Bounders | Bounder | Guster | Far jumps and strong pushes |
| Guarded Bounders | Bounder | Deflector | Far jumps and a firm guard |
| Whirling Bounders | Bounder | Whirler | Far jumps and fast fire |
| Stormy Bounders | Bounder | Tempest | Far jumps and wind that hurts |
| Gusting Whirlers | Whirler | Guster | Fast fire and strong pushes |
| Guarded Whirlers | Whirler | Deflector | Fast fire and a firm guard |
| Bounding Whirlers | Whirler | Bounder | Fast fire and far jumps |
| Stormy Whirlers | Whirler | Tempest | Fast fire that hurts |
| Gusting Tempests | Tempest | Guster | Wind that hurts and pushes far |
| Guarded Tempests | Tempest | Deflector | Wind that hurts and a firm guard |
| Bounding Tempests | Tempest | Bounder | Wind that hurts and far jumps |
| Whirling Tempests | Tempest | Whirler | Wind that hurts, shot fast |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Gusters".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Wind Walls | Deflector and Guster | Strong pushes and a firm guard |
| Sky Gusts | Bounder and Guster | Strong pushes and far jumps |
| Cyclones | Guster and Whirler | Strong pushes, shot fast |
| Hurricanes | Guster and Tempest | Strong pushes that hurt |
| Spring Guards | Bounder and Deflector | A firm guard and far jumps |
| Spinning Wards | Deflector and Whirler | A firm guard and fast fire |
| Calm Storms | Deflector and Tempest | A firm guard and wind that hurts |
| Whirlwinds | Bounder and Whirler | Far jumps and fast fire |
| Squall Leapers | Bounder and Tempest | Far jumps and wind that hurts |
| Twisters | Tempest and Whirler | Fast fire that hurts |

## Neutral

A neutral breeze has no genes, or no family points.
It is a plain breeze, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A breeze region map, and the gene textures and gene models on the breeze model, with the wind texture | All visuals |
| The push, the damage, the blast size, and the speed of its wind charges | Gusters, Tempests, Hard Gust, Wide Gust, Cut Wind, Fast Charge |
| An upward push from its wind charges | Lift Gust |
| A shield stop, more golem push, and an off hand drop from its wind charges | Shield Gust, Golem Gust, Disarm Gust |
| An effect from its wind charges | Chill Wind, Dizzy Wind, Choke Wind |
| The length, the height, and the rest time of its jumps | Bounders, Sky Hop, Fast Spring, and the downsides |
| Damage when it lands, a jump aside, and a jump out of water | Crash Down, Side Hop, Swim Hop |
| A wind charge at the top of a jump | Wind Ride |
| The time between its wind charges, and two at once | Whirlers, Rapid Wind, Twin Charge |
| No spread of its wind charges | True Aim |
| A wind blast round it | Storm Gale |
| More damage from a projectile that it sends back, and a straight path to the shooter | Wind Mirror |
| A check that the reused effects work on a breeze: rebound, push on hurt, flee, stacks, fang line, lightning, sidestep, and the damage taken effects | Many genes |
