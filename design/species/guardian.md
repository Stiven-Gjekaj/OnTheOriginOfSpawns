# Guardian

This document defines the families and the genes of the guardian.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The guardian is a fish with one eye that guards the ocean monuments. It has
30 health. It locks its eye on a target and charges a laser for about 4
seconds. Then the laser does 6 damage on Normal. The beam breaks if the
target goes out of its sight. When it does not swim, its spikes come out, and
a melee hit on it then does 2 damage to the attacker. It swims fast, and it
flops on land. It attacks players, squids, and axolotls. Its genes fit the
laser, the time of the charge, the spikes, the speed of its swim, and the
school. It shares no genes and no families with the other species. The elder
guardian is a boss, and it gets its genes in milestone 6.

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

### Beamers

Beamers have a hot laser. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its laser does 30% more damage | 10% less maximum health |
| Tier 2 | Its laser does 20% more damage | 15% less maximum health |
| Tier 1 | Its laser does 15% more damage | 20% less maximum health |

### Focusers

Focusers charge their laser fast. The laser is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its laser charges 30% faster | Its laser does 10% less damage |
| Tier 2 | Its laser charges 20% faster | Its laser does 15% less damage |
| Tier 1 | Its laser charges 15% faster | Its laser does 20% less damage |

### Spikebacks

Spikebacks have sharp spikes. They swim slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its spikes do 3 more damage | 10% less swim speed |
| Tier 2 | Its spikes do 2 more damage | 15% less swim speed |
| Tier 1 | Its spikes do 1 more damage | 20% less swim speed |

### Darters

Darters swim fast. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more swim speed | 10% more damage taken |
| Tier 2 | 20% more swim speed | 15% more damage taken |
| Tier 1 | 15% more swim speed | 20% more damage taken |

### Shoalers

Shoalers call their school. Alone, they are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When it sees a player, it calls the guardians in 16 blocks | Its laser does 10% less damage |
| Tier 2 | When it sees a player, it calls the guardians in 12 blocks | Its laser does 15% less damage |
| Tier 1 | When it sees a player, it calls the guardians in 8 blocks | Its laser does 20% less damage |

## Pressures

The guardian uses `projectile`, `melee`, `explosion`, `trident`, `boat`,
`in_water`, `on_land`, `unreached`, `spotted`, `alone`, `crowd`, `critical`,
and `player_death`. It lives in water, so it does not use `fire`,
`sunlight`, `night`, `day`, or `fall`. A golem seldom fights it, so it does
not use `golem`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Beamer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Deep Beam | Beamer | `unreached` | Its laser does 40% more damage | 10% less maximum health | Eye: a white, hot pupil |
| Tiring Gaze | Beamer | `melee` | Its laser gives Mining Fatigue II for 6 seconds | 10% less maximum health | Eye: a gray ring round the pupil |
| Hull Cutter | Beamer | `boat` | When its laser hits a player in a boat, the boat breaks | 10% less swim speed | Body: a dark red band round the body |
| Split Beam | Beamer | `crowd` | Its laser also hits a second target in 8 blocks of the first, with half the damage | 10% less maximum health | Eye: two pupils |
| Dying Glare | Beamer | `critical` | At under 30% health, its laser does 30% more damage | 10% less swim speed | Eye: red veins round the eye |
| Prism Flash | Beamer, sub Focuser | `spotted` | Its laser gives Blindness for 2 seconds | 10% less maximum health | Eye: a ring of many colors |

### Focuser genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Quick Focus | Focuser | `spotted` | Its laser charges 40% faster | 10% less maximum health | Eye: a narrow, slit pupil |
| Steady Lock | Focuser | `projectile` | The beam does not break when the target goes out of sight for 1 second or less | Its laser does 10% less damage | Eye: a gold ring round the eye |
| Close Snap | Focuser | `melee` | When its target is in 3 blocks, the laser fires at once | 10% less maximum health | Tail: a bright tail tip |
| Wide Eye | Focuser | `unreached` | Two times the follow range | 10% less swim speed | Eye: a large, wide eye. This changes the model |
| Kill Focus | Focuser | `player_death` | After it kills a player, its next laser charges at once | 10% less maximum health | Eye: a half-closed lid |
| Pulse Beam | Focuser, sub Beamer | `alone` | While its laser charges, the target takes 1 damage each second | 10% less maximum health | Body: glowing lines on the body |

### Spikeback genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Spikes | Spikeback | `melee` | Its spikes do 2 more damage | 10% less swim speed | Spikes: long spikes. This changes the model |
| Trident Plate | Spikeback | `trident` | Half the damage from tridents | 10% less swim speed | Body: thick, steel blue plates |
| Coral Shell | Spikeback | `projectile` | 30% less damage from projectiles | 10% less swim speed | Body: pink coral plates |
| Blast Plate | Spikeback | `explosion` | Half the damage from explosions | 10% less swim speed | Body: dark, sooty plates |
| Venom Spikes | Spikeback | `critical` | Its spikes give Poison for 3 seconds | 10% less maximum health | Spikes: green tips |
| Spike Burst | Spikeback, sub Darter | `crowd` | When it dies, its spikes fly out and do 4 damage to the players in 3 blocks | 10% less maximum health | Spikes: red tips |

### Darter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Fast Fin | Darter | `unreached` | 20% more swim speed | 10% less maximum health | Tail: a wide tail fin. This changes the model |
| Dodge Fin | Darter | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Tail: torn fin edges |
| Shore Hop | Darter | `on_land` | On land, it hops to the nearest water in 8 blocks | 10% less maximum health | Body: wet, dark patches |
| Slick Hide | Darter | `trident` | It moves aside from a thrown trident, one time each 8 seconds | 10% less swim speed | Body: smooth, pale scales |
| Deep Flight | Darter | `in_water` | At under 30% health, it swims away fast for 4 seconds | 10% less maximum health | Tail: a dark tail |
| Ram Fin | Darter, sub Spikeback | `melee` | It rams its target for 4 damage, one time each 5 seconds | 10% less maximum health | Body: a hard, gray brow on the body |

### Shoaler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| School Call | Shoaler | `spotted` | When it sees a player, it calls the guardians in 24 blocks | Players hear it from two times farther | Body: rows of small spots |
| Shoal Ward | Shoaler | `crowd` | 20% less damage taken with 2 or more guardians near it | 20% more damage taken alone | Body: blue bands |
| Pack Gaze | Shoaler | `melee` | Its laser does 20% more damage with 2 or more guardians near it | Its laser does 20% less damage alone | Eye: a blue rim round the eye |
| Alarm Pulse | Shoaler | `alone` | When something hurts it, it calls the guardians in 32 blocks | Players hear it from two times farther | Tail: ripple lines |
| Kin Grieving | Shoaler | `player_death` | When a guardian dies in 16 blocks, it heals 4 health | 10% less maximum health | Eye: a sad, drooping lid |
| Guide Beam | Shoaler, sub Focuser | `unreached` | When it locks on a target, the guardians in 16 blocks target the same mob | 10% less maximum health | Spikes: blue tips |

## Gene details

| Gene | Rules |
|---|---|
| Beamers, Deep Beam | The damage is the damage of the laser when it fires |
| Focusers, Quick Focus | The vanilla charge takes 80 ticks. 30% faster takes about 61 ticks |
| Spikebacks, Long Spikes | The vanilla spikes do 2 damage to a melee attacker while the spikes are out |
| Darters, Fast Fin | "Swim speed" is the speed of the guardian in water |
| Tiring Gaze | The effect goes to each target that the laser hits |
| Hull Cutter | The boat drops as an item, and the player falls into the water |
| Split Beam | The second target must be a target that the guardian attacks in vanilla |
| Dying Glare | It works as the zombified piglin Wrath, on the laser |
| Steady Lock | In vanilla, the beam breaks at once when the target goes out of sight |
| Close Snap | The laser does its full damage |
| Wide Eye | The vanilla follow range is 16 blocks |
| Pulse Beam | The damage is magic damage, as the laser |
| Trident Plate | The trident tag holds a thrown trident and a melee hit with a trident |
| Venom Spikes | The effect goes to each attacker that the spikes hurt |
| Spike Burst | It does no damage to other guardians |
| Dodge Fin, Slick Hide | They work as the pillager Evasion, in water |
| Shore Hop | It hops one time each second while it is on land |
| Deep Flight | It works as the blaze Smoke Dash |
| Ram Fin | The ram knocks the target back a little |
| School Call | It works as the pillager War Horn |
| Shoal Ward, Pack Gaze | They work as the pillager Formation |
| Alarm Pulse | It works as the vindicator Alarm Bell |
| Kin Grieving | It works as the stray Mourner |
| Guide Beam | It works as the phantom Swarm Dive |

## Equipment

- The guardian holds nothing. It drops its vanilla items.

## Conversion

- The guardian does not convert to a different species, and no species
  converts to the guardian.

## Babies and jockeys

- The guardian has no babies. It does not get a gene for them.
- A guardian does not ride a mob. It does not get a gene for this.

## Spawning

- A guardian that spawns in an ocean monument gets genes as a natural spawn.

## Body

- The guardian has the guardian model: a body cube, one eye, three tail
  parts, and twelve spikes. Its texture is 64 x 64.
- It needs a new region map, `textures/region/guardian.png`. The design uses
  the regions body, eye, tail, and spikes.
- The gene models use the guardian bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Focused Beamers | Beamer | Focuser | A hot laser, charged fast |
| Spiked Beamers | Beamer | Spikeback | A hot laser and sharp spikes |
| Darting Beamers | Beamer | Darter | A hot laser and a fast swim |
| Shoal Beamers | Beamer | Shoaler | A hot laser in a school |
| Bright Focusers | Focuser | Beamer | A fast charge of a hot laser |
| Spiked Focusers | Focuser | Spikeback | A fast charge and sharp spikes |
| Darting Focusers | Focuser | Darter | A fast charge and a fast swim |
| Shoal Focusers | Focuser | Shoaler | A fast charge in a school |
| Bright Spikebacks | Spikeback | Beamer | Sharp spikes and a hot laser |
| Focused Spikebacks | Spikeback | Focuser | Sharp spikes and a fast charge |
| Darting Spikebacks | Spikeback | Darter | Sharp spikes and a fast swim |
| Shoal Spikebacks | Spikeback | Shoaler | Sharp spikes in a school |
| Bright Darters | Darter | Beamer | A fast swim and a hot laser |
| Focused Darters | Darter | Focuser | A fast swim and a fast charge |
| Spiked Darters | Darter | Spikeback | A fast swim and sharp spikes |
| Shoal Darters | Darter | Shoaler | A fast swim in a school |
| Bright Shoalers | Shoaler | Beamer | A school with a hot laser |
| Focused Shoalers | Shoaler | Focuser | A school with a fast charge |
| Spiked Shoalers | Shoaler | Spikeback | A school with sharp spikes |
| Darting Shoalers | Shoaler | Darter | A school with a fast swim |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Beamers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Sun Lances | Beamer and Focuser | A hot laser, charged fast |
| Spined Lights | Beamer and Spikeback | A hot laser and sharp spikes |
| Swift Beams | Beamer and Darter | A hot laser and a fast swim |
| Beacon Shoals | Beamer and Shoaler | A hot laser in a school |
| Needle Eyes | Focuser and Spikeback | A fast charge and sharp spikes |
| Darting Gazes | Darter and Focuser | A fast charge and a fast swim |
| Watch Shoals | Focuser and Shoaler | A fast charge in a school |
| Urchins | Darter and Spikeback | Sharp spikes and a fast swim |
| Spine Shoals | Shoaler and Spikeback | Sharp spikes in a school |
| Tide Racers | Darter and Shoaler | A fast swim in a school |

## Neutral

A neutral guardian has no genes, or no family points.
It is a plain guardian, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A guardian region map, and the gene textures and gene models on the guardian model | All visuals |
| The damage of the laser, also by state and by group | Beamers, Deep Beam, Dying Glare, Pack Gaze, and the downsides |
| The charge time of the laser, a charge at once at close range, and a charge at once after a kill | Focusers, Quick Focus, Close Snap, Kill Focus |
| An effect from the laser | Tiring Gaze, Prism Flash |
| A boat that breaks when the laser hits | Hull Cutter |
| A second target of the laser | Split Beam |
| A beam that does not break at once out of sight | Steady Lock |
| Damage to the target while the laser charges | Pulse Beam |
| The damage of the spikes, and an effect from the spikes | Spikebacks, Long Spikes, Venom Spikes |
| Spikes that fly out at death | Spike Burst |
| The swim speed of the guardian | Darters, Fast Fin, and the downsides |
| A sidestep in water, also from a thrown trident | Dodge Fin, Slick Hide |
| A hop to water on land | Shore Hop |
| A ram at the target | Ram Fin |
| A call of the school to its locked target | Guide Beam |
| A check that the reused effects work on a guardian: calls, groups, heal on ally death, flee, follow range, and the damage taken effects | Many genes |
