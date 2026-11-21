# Ravager

This document defines the families and the genes of the ravager.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The ravager is a large beast of the illagers. It comes only in raids, and an
illager can ride it. It has 100 health, does 12 melee damage, and resists 75%
of knockback. It breaks leaves in its way. When a shield blocks its hit, it is
stunned for 2 seconds with a 50% chance. After the stun, it roars. The roar
does 6 damage and knocks back the mobs in 4 blocks. Its genes fit the horns,
the run, the roar, the hide, and the rider. It shares no genes and no families
with the other species.

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

### Impalers

Impalers hit hard with their horns. They are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more melee damage | 10% less speed |
| Tier 2 | 20% more melee damage | 15% less speed |
| Tier 1 | 15% more melee damage | 20% less speed |

### Tramplers

Tramplers run fast. Their bodies are lean.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed | 10% less maximum health |
| Tier 2 | 20% more speed | 15% less maximum health |
| Tier 1 | 15% more speed | 20% less maximum health |

### Roarers

Roarers roar far. Their horns are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its roar reaches 8 blocks, not 4 | 10% less melee damage |
| Tier 2 | Its roar reaches 6 blocks, not 4 | 15% less melee damage |
| Tier 1 | Its roar reaches 5 blocks, not 4 | 20% less melee damage |

### Ironbacks

Ironbacks have a thick hide. A shield block stuns them easily.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor | A shield block stuns it with a 60% chance, not 50% |
| Tier 2 | 4 more armor | A shield block stuns it with a 70% chance, not 50% |
| Tier 1 | 3 more armor | A shield block stuns it with an 80% chance, not 50% |

### Warbeasts

Warbeasts guard their riders. Their bodies are lean.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its rider takes 30% less damage | 10% less maximum health |
| Tier 2 | Its rider takes 20% less damage | 15% less maximum health |
| Tier 1 | Its rider takes 15% less damage | 20% less maximum health |

## Pressures

The ravager uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`, `shield`, and
`player_death`. It comes only in raids, so `raid` is true for each death,
and the ravager does not use it. It does not burn in the sun, so it does not
use `sunlight`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Impaler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Horn Gore | Impaler | `melee` | 20% more melee damage | 10% less speed | Horns: dark horn tips |
| Shield Crush | Impaler | `shield` | Its hits disable a shield for 5 seconds, as an axe does | 10% less speed | Horns: three pale notches on the upper edge of each horn. This changes the model |
| Golem Crusher | Impaler | `golem` | Its hits do 50% more damage to iron golems | 10% less speed | Head: an iron gray, riveted brow |
| Gore Rage | Impaler | `critical` | At under half health, 30% more melee damage | 10% less speed | Head: red veins on the sides over and behind the eyes |
| Sweep Horns | Impaler | `crowd` | Its hits also do half damage to the mobs in 2 blocks of the target | 10% less speed | Horns: long horns. This changes the model |
| Ram Rush | Impaler, sub Trampler | `spotted` | Its first hit after a run of 6 blocks or more does 50% more damage | 10% less speed | Head: a pale stripe down the top of the head and the forehead |

### Trampler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Wide Stride | Trampler | `unreached` | 20% more speed | 10% less maximum health | Legs: pale stripes on the legs |
| Fence Breaker | Trampler | `projectile` | It breaks fences, fence gates, and glass in its way | 10% less maximum health | Head: scars on the snout and between the eyes |
| Sure Foot | Trampler | `fall` | Half the fall damage | 10% less maximum health | Legs: dark hooves |
| Blood Run | Trampler | `player_death` | After it kills a player, 30% more speed for 10 seconds | 10% less maximum health | Body: red claw marks on the front flanks |
| Lone Charge | Trampler | `alone` | It charges at its target from 8 blocks or more, one time each 8 seconds. The charge does 6 damage and knocks the target back | 10% less maximum health | Neck: a crest of dark hair between the head and the body. This changes the model |
| Rider Rush | Trampler, sub Warbeast | `crowd` | With a rider, 20% more speed | 10% less maximum health | Body: a brown strap with brass buckles round the body. This changes the model |

### Roarer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Loud Roar | Roarer | `shield` | Its roar reaches 6 blocks | 10% less melee damage | Mouth: a pale, shut mouth and pale lips |
| Fear Roar | Roarer | `melee` | Its roar also gives Slowness II for 4 seconds | 10% less melee damage | Mouth: two short, thick, pale tusks at the corners of the mouth. This changes the model |
| Rage Roar | Roarer | `projectile` | When a projectile hurts it, it roars, one time each 10 seconds | 10% less melee damage | Head: orange marks on the back of the sides of the head |
| Victory Roar | Roarer | `player_death` | After it kills a player, it roars and heals 10 health | 10% less melee damage | Head: gold rings round the eyes |
| Lone Roar | Roarer | `alone` | With no raider in 8 blocks, its roar does 50% more damage | 10% less melee damage | Head: gray scars on the back of the sides of the head |
| War Roar | Roarer, sub Warbeast | `crowd` | Its roar gives the raiders in 8 blocks Strength I for 5 seconds | 10% less melee damage | Mouth: dark red lips and jaw |

### Ironback genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Plate Hide | Ironback | `melee` | 4 more armor | 10% less speed | Legs: riveted iron plates on the tops of the legs |
| Arrow Hide | Ironback | `projectile` | 30% less damage from projectiles | 10% less speed | Body: thick, dark patches on the back flanks |
| Blast Pelt | Ironback | `explosion` | Half the damage from explosions | 10% less speed | Body: soot and ash on the rump |
| Ember Pelt | Ironback | `fire` | Half the damage from fire and lava | 10% less speed | Legs: dark red legs |
| Steady Stance | Ironback | `shield` | A shield block never stuns it | It does not roar | Legs: a wide, pale pad round each foot. This changes the model |
| Spiked Hide | Ironback, sub Impaler | `critical` | A melee attacker takes 2 damage | 10% less speed | Body: two rows of short, dark spikes on the back. This changes the model |

### Warbeast genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Saddle Guard | Warbeast | `spotted` | Its rider takes 30% less damage | 10% less maximum health | Body: a dark blue gray saddle cloth with pale stitches |
| Escort Rider | Warbeast | `alone` | In a raid, it always spawns with a rider | 10% less maximum health | Neck: a thick iron ring round the neck. This changes the model |
| Rider Heal | Warbeast | `critical` | When its rider hurts a target, it heals 4 health | 10% less maximum health | Head: green marks on the brow |
| Horde Hide | Warbeast | `crowd` | 20% less damage taken with 2 or more raiders in 8 blocks | 20% more damage taken alone | Body: bands of dark blue gray, dark green, and beige |
| Raid Scent | Warbeast | `unreached` | In a raid, it targets the nearest player in 48 blocks | 10% less maximum health | Head: wide, dark nostrils with flared rims. This changes the model |
| Twin Strike | Warbeast, sub Impaler | `melee` | When it hits a target, its rider also attacks that target | 10% less maximum health | Horns: banded horns and a pale ring round the root of each horn |

## Gene details

| Gene | Rules |
|---|---|
| Impalers, Horn Gore | The vanilla hit does 12 damage on Normal |
| Shield Crush | It works as the vindicator axe on a shield |
| Gore Rage | It works as the zombified piglin Wrath |
| Sweep Horns | The other targets must be targets that the ravager attacks in vanilla. Raiders take no damage |
| Ram Rush | The run must be toward the target, with no stop |
| Fence Breaker | It breaks these blocks as it breaks leaves in vanilla. It needs the mobGriefing game rule |
| Lone Charge | The charge stops at a wall |
| Rider Rush | The rider is any raider on its back |
| Roarers, Loud Roar | The vanilla roar reaches 4 blocks |
| Rage Roar | The roar does not need a stun first |
| Victory Roar | The heal comes at once |
| Lone Roar | It counts the raiders in 8 blocks, as the pillager Formation does |
| War Roar | Each raider gets the effect one time for each roar |
| Ironbacks | The vanilla stun lasts 2 seconds. After the stun, it roars |
| Steady Stance | It does not roar after a shield block, because it is not stunned. Rage Roar and Victory Roar still work |
| Spiked Hide | It works as the guardian spikes, at all times |
| Warbeasts, Saddle Guard | The damage taken by its rider goes down |
| Escort Rider | The rider is a pillager, as in the vanilla raid waves |
| Rider Heal | The heal comes at once, one time each 4 seconds |
| Horde Hide | It works as the pillager Formation, with raiders |
| Raid Scent | The vanilla follow range is 32 blocks |
| Twin Strike | The rider attacks one time for each hit of the ravager |

## Equipment

- The ravager holds nothing. It drops its vanilla items.

## Conversion

- The ravager does not convert to a different species, and no species
  converts to the ravager.

## Babies and jockeys

- The ravager has no babies. It does not get a gene for them.
- A pillager, a vindicator, or an evoker can ride a ravager, as in vanilla.
  The rider keeps its own genes. The Warbeast genes act on the rider.

## Spawning

- A ravager that spawns in a raid gets genes as a natural spawn.

## Body

- The ravager has the ravager model: a head with a mouth and two horns, a
  neck, a body, and four legs. Its texture is 128 x 128.
- It needs a new region map, `textures/region/ravager.png`. The design uses
  the regions head, horns, mouth, neck, body, and legs.
- The neck does not show while the ravager stands. The head covers its
  front, and the body covers its back. So the two neck genes are models,
  and Rage Roar and Lone Roar mark the back of the sides of the head.
- The legs stand up past the back and hide most of the flanks. So Plate
  Hide puts its plates on the tops of the legs.
- The mouth stays shut. No gene opens it.
- Nine genes change the model: Shield Crush, Sweep Horns, Lone Charge,
  Rider Rush, Fear Roar, Steady Stance, Spiked Hide, Escort Rider, and Raid
  Scent. The eyes stay vanilla.
- The gene models use the ravager bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Trampling Impalers | Impaler | Trampler | Hard horns and a fast run |
| Roaring Impalers | Impaler | Roarer | Hard horns and a wide roar |
| Plated Impalers | Impaler | Ironback | Hard horns and a thick hide |
| Saddled Impalers | Impaler | Warbeast | Hard horns and a guarded rider |
| Impaling Tramplers | Trampler | Impaler | A fast run and hard horns |
| Roaring Tramplers | Trampler | Roarer | A fast run and a wide roar |
| Plated Tramplers | Trampler | Ironback | A fast run and a thick hide |
| Saddled Tramplers | Trampler | Warbeast | A fast run and a guarded rider |
| Impaling Roarers | Roarer | Impaler | A wide roar and hard horns |
| Trampling Roarers | Roarer | Trampler | A wide roar and a fast run |
| Plated Roarers | Roarer | Ironback | A wide roar and a thick hide |
| Saddled Roarers | Roarer | Warbeast | A wide roar and a guarded rider |
| Impaling Ironbacks | Ironback | Impaler | A thick hide and hard horns |
| Trampling Ironbacks | Ironback | Trampler | A thick hide and a fast run |
| Roaring Ironbacks | Ironback | Roarer | A thick hide and a wide roar |
| Saddled Ironbacks | Ironback | Warbeast | A thick hide and a guarded rider |
| Impaling Warbeasts | Warbeast | Impaler | A guarded rider and hard horns |
| Trampling Warbeasts | Warbeast | Trampler | A guarded rider and a fast run |
| Roaring Warbeasts | Warbeast | Roarer | A guarded rider and a wide roar |
| Plated Warbeasts | Warbeast | Ironback | A guarded rider and a thick hide |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Impalers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Gate Smashers | Impaler and Trampler | Hard horns and a fast run |
| Bellow Horns | Impaler and Roarer | Hard horns and a wide roar |
| Iron Rams | Impaler and Ironback | Hard horns and a thick hide |
| War Tusks | Impaler and Warbeast | Hard horns and a guarded rider |
| Howl Runners | Trampler and Roarer | A fast run and a wide roar |
| Siege Engines | Trampler and Ironback | A fast run and a thick hide |
| Raid Sprinters | Trampler and Warbeast | A fast run and a guarded rider |
| Rock Bellows | Roarer and Ironback | A wide roar and a thick hide |
| Horn Callers | Roarer and Warbeast | A wide roar and a guarded rider |
| War Wagons | Ironback and Warbeast | A thick hide and a guarded rider |

## Neutral

A neutral ravager has no genes, or no family points.
It is a plain ravager, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A ravager region map, and the gene textures and gene models on the ravager model | All visuals |
| The range, the damage, and the effects of the roar, a roar without a stun, and no roar | Roarers, Loud Roar, Fear Roar, Rage Roar, Victory Roar, Lone Roar, War Roar, Steady Stance |
| The chance of a stun from a shield block | Ironbacks, Steady Stance |
| A shield disable from a hit | Shield Crush |
| More damage to iron golems, and a sweep hit | Golem Crusher, Sweep Horns |
| A first hit with more damage after a run, and a charge at the target | Ram Rush, Lone Charge |
| A break of fences, fence gates, and glass | Fence Breaker |
| Less damage taken by its rider, a heal when its rider hurts a target, and an attack of the rider with its hit | Warbeasts, Saddle Guard, Rider Heal, Twin Strike |
| Speed with a rider | Rider Rush |
| A rider at each spawn in a raid, and a long follow range in a raid | Escort Rider, Raid Scent |
| A check that the reused effects work on a ravager: groups, Wrath, thorns, fall damage, and the damage taken effects | Many genes |
