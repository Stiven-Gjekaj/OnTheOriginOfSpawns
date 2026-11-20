# Zombie nautilus

This document defines the families and the genes of the zombie nautilus.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The zombie nautilus is an undead nautilus of the ocean. It has 15 health. When
a drowned with a trident spawns in a natural way, it rides a zombie nautilus
one time in 2, but not in a river. The zombie nautilus swims faster than a
nautilus, and it charges at its target for 3 damage, about one time each 4
seconds. It resists a part of each push. It has a temperate and a warm
variant. It cannot breed, and it has no babies. Its genes fit its charge, its
shell, its speed in water, the guard of its rider, and ink. It shares no
genes and no families with the other species.

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

### Rammers

Rammers charge hard. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its charge does 30% more damage | 10% less maximum health |
| Tier 2 | Its charge does 20% more damage | 15% less maximum health |
| Tier 1 | Its charge does 15% more damage | 20% less maximum health |

### Whorlbacks

Whorlbacks have thick shells. They swim slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 4 more armor | 10% less speed |
| Tier 2 | 3 more armor | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Jetters

Jetters swim fast. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed | 10% more damage taken |
| Tier 2 | 20% more speed | 15% more damage taken |
| Tier 1 | 15% more speed | 20% more damage taken |

### Tidebearers

Tidebearers guard their riders. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its riders take 30% less damage | 10% less maximum health |
| Tier 2 | Its riders take 20% less damage | 15% less maximum health |
| Tier 1 | Its riders take 15% less damage | 20% less maximum health |

### Inkers

Inkers squirt ink when they are hurt. Their charge is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When something hurts it, the players in 4 blocks get Blindness for 4 seconds, one time each 10 seconds | Its charge does 10% less damage |
| Tier 2 | When something hurts it, the players in 4 blocks get Blindness for 3 seconds, one time each 10 seconds | Its charge does 15% less damage |
| Tier 1 | When something hurts it, the players in 4 blocks get Blindness for 2 seconds, one time each 10 seconds | Its charge does 20% less damage |

## Pressures

The zombie nautilus uses `melee`, `projectile`, `trident`, `explosion`,
`boat`, `shield`, `in_water`, `on_land`, `unreached`, `spotted`, `alone`,
`crowd`, `critical`, `sunlight`, and `player_death`. The sun burns it out of
water, as it burns a drowned. It lives in the ocean, so it does not use
`fire`, `night`, `day`, or `fall`. A golem seldom fights it, so it does not
use `golem`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Rammer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Deep Ram | Rammer | `unreached` | Its charge does 40% more damage | 10% less maximum health | Shell: a sharp, pointed front edge. This changes the model |
| Quick Ram | Rammer | `spotted` | It charges 50% more often | 10% less maximum health | Body: narrow, pale eyes |
| Hull Ram | Rammer | `boat` | Its charge breaks the boat of the target | 10% less speed | Shell: a dark red rim |
| Shield Ram | Rammer | `shield` | Its charge stops a shield for 4 seconds | 10% less speed | Shell: gray scrape marks |
| Breath Ram | Rammer | `in_water` | Its charge takes 3 seconds of air from the target | 10% less maximum health | Tentacles: blue tips |
| Tide Charge | Rammer, sub Jetter | `crowd` | Its charge pushes the target back two times as far | 10% less maximum health | Tentacles: long, thin tentacles. This changes the model |

### Whorlback genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Layered Shell | Whorlback | `projectile` | 30% less damage from projectiles | 10% less speed | Shell: thick, layered ridges |
| Trident Shell | Whorlback | `trident` | Half the damage from tridents | 10% less speed | Shell: steel bands round the shell |
| Coral Crust | Whorlback | `explosion` | Half the damage from explosions | 10% less speed | Shell: pink coral patches |
| Barnacle Hide | Whorlback | `melee` | When a melee hit hurts it, the attacker takes 2 damage | 10% less speed | Shell: rows of barnacles |
| Sealed Shell | Whorlback | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Shell: a bright scar across the shell |
| Spiral Rest | Whorlback, sub Tidebearer | `alone` | It heals 1 health each 3 seconds out of combat | 10% less speed | Shell: a pale spiral line |

### Jetter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Jet Fin | Jetter | `unreached` | 20% more speed | 10% less maximum health | Tentacles: wide, flat tentacles |
| Dodge Jet | Jetter | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Body: wide, alert eyes |
| Shore Crawl | Jetter | `on_land` | On land, it crawls to the nearest water in 8 blocks | 10% less maximum health | Body: wet, dark skin |
| Deep Escape | Jetter | `critical` | At under 30% health, it swims away fast for 4 seconds | 10% less maximum health | Tentacles: torn tentacles |
| Sun Shell | Jetter | `sunlight` | The sun burns it only one time each 4 seconds, not each second | 10% less maximum health | Shell: a dark, sun-baked shell |
| Wake Jet | Jetter, sub Rammer | `spotted` | After a charge, it gets Speed for 3 seconds | 10% less maximum health | Tentacles: white wake marks |

### Tidebearer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Shell Wall | Tidebearer | `projectile` | Its riders take 30% less damage from projectiles | 10% less maximum health | Shell: a high shell rim. This changes the model |
| Tide Shield | Tidebearer | `melee` | Its riders take 20% less melee damage | 10% less speed | Shell: a broad, flat shell edge |
| Trident Guard | Tidebearer | `trident` | Its riders take half the damage from tridents | 10% less speed | Shell: a silver ring |
| Kin Current | Tidebearer | `critical` | When a rider is under half health, the rider heals 4 health at once, one time each 30 seconds | 10% less maximum health | Body: a green mark between the eyes |
| Last Current | Tidebearer | `player_death` | When it dies, its riders get Resistance for 10 seconds | 10% less maximum health | Tentacles: pale tips |
| Ink Veil | Tidebearer, sub Inker | `spotted` | Its riders are near invisible in the dark | 10% less maximum health | Shell: black ink stains |

### Inker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Thick Ink | Inker | `melee` | Its ink also gives Slowness for 3 seconds | 10% less maximum health | Body: black ink round the eyes |
| Wide Ink | Inker | `crowd` | Its ink reaches two times as far | 10% less speed | Tentacles: black tips |
| Sour Ink | Inker | `unreached` | Its ink also gives Nausea for 4 seconds | 10% less maximum health | Body: purple spots |
| Ink Screen | Inker | `projectile` | After a projectile hurts it, it is near invisible for 4 seconds | 10% less maximum health | Body: dark, smoky patches |
| Quick Ink | Inker | `alone` | It squirts its ink two times as often | 10% less maximum health | Tentacles: purple rings |
| Ink Rush | Inker, sub Jetter | `explosion` | After it squirts ink, it gets Speed for 4 seconds | 10% less maximum health | Tentacles: streaked tentacles |

## Gene details

| Gene | Rules |
|---|---|
| Rammers, Deep Ram | "Charge" is the vanilla charge attack of the zombie nautilus. A vanilla zombie nautilus charges only a pufferfish, or an entity that hurt it while in water |
| Rammer genes | A zombie nautilus with Rammer family points or a Rammer gene also charges at the target of its rider, in water and on land |
| Sun Shell | It works as the phantom Sun Shade |
| Quick Ram | The vanilla charge waits 80 ticks between charges |
| Hull Ram | It works as the guardian Hull Cutter |
| Shield Ram | It works as the slime Shield Melt |
| Breath Ram | It works only in water. It does not hurt a target that breathes water |
| Tide Charge | It works as the push of the hoglin Tuskers |
| Whorlbacks | "Speed" is the speed of the zombie nautilus in water and on land |
| Barnacle Hide | It works as the hoglin Barbed Hide |
| Sealed Shell | It works as the vindicator Death Wish |
| Spiral Rest | It works as the vindicator Thick Blood |
| Dodge Jet | It works as the pillager Evasion, in water |
| Shore Crawl | It works as the guardian Shore Hop |
| Deep Escape | It works as the blaze Smoke Dash |
| Wake Jet | It uses the self effect after a hit |
| Tidebearers | "Its riders" are the mobs that ride it. A player that rides it does not count |
| Kin Current | It checks each rider. The riders are undead, and the undead ignore Regeneration, so it heals at once |
| Last Current | It works as the pillager Grudge, on its riders only |
| Ink Veil | It works as the creeper Gloom, on the riders |
| Inkers | They work as the evoker Smoke Veil, with an ink cloud and Blindness |
| Thick Ink, Sour Ink | The extra effect goes to each player that the ink reaches |
| Ink Screen | It works as the creeper Gloom for 4 seconds |

## Equipment

- The zombie nautilus holds nothing. It drops its vanilla items.
- A drowned that rides a zombie nautilus keeps its trident, as a drowned does.

## Conversion

- The zombie nautilus does not convert to a different species, and no species
  converts to the zombie nautilus.

## Babies and jockeys

- The zombie nautilus has no babies. It does not get a gene for them.
- The drowned on its back keeps its own genes. The zombie nautilus does not
  change them, except through the Tidebearer genes.

## Spawning

- A zombie nautilus that spawns under a drowned gets genes as a natural spawn.

## Body

- The zombie nautilus has the nautilus model and a temperate and a warm
  texture. Both are 128 x 128 and share one layout, so one region map serves
  both, and each mark goes on both. The warm model adds flat corals, which are
  not a region.
- It needs a new region map, `textures/region/zombie_nautilus.png`. The design
  uses the regions shell, body, and tentacles.
- The model has no tentacle bones. The "tentacles" are the three mouth parts:
  `upper_mouth`, `inner_mouth`, and `lower_mouth`. They are small, so most
  tentacle genes are small gene models, each in its own place.
- The bones are `shell`, `upper_mouth`, `inner_mouth`, and `lower_mouth`. The
  coil of the shell is the front as it swims, and the mouth trails.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Shelled Rammers | Rammer | Whorlback | Hard charges from a thick shell |
| Jetting Rammers | Rammer | Jetter | Hard charges at speed |
| Bearing Rammers | Rammer | Tidebearer | Hard charges that guard the rider |
| Inking Rammers | Rammer | Inker | Hard charges and ink |
| Ramming Whorlbacks | Whorlback | Rammer | A thick shell and hard charges |
| Jetting Whorlbacks | Whorlback | Jetter | A thick shell at speed |
| Bearing Whorlbacks | Whorlback | Tidebearer | A thick shell that guards the rider |
| Inking Whorlbacks | Whorlback | Inker | A thick shell and ink |
| Ramming Jetters | Jetter | Rammer | Speed and hard charges |
| Shelled Jetters | Jetter | Whorlback | Speed and a thick shell |
| Bearing Jetters | Jetter | Tidebearer | Speed that guards the rider |
| Inking Jetters | Jetter | Inker | Speed and ink |
| Ramming Tidebearers | Tidebearer | Rammer | A guard and hard charges |
| Shelled Tidebearers | Tidebearer | Whorlback | A guard and a thick shell |
| Jetting Tidebearers | Tidebearer | Jetter | A guard at speed |
| Inking Tidebearers | Tidebearer | Inker | A guard and ink |
| Ramming Inkers | Inker | Rammer | Ink and hard charges |
| Shelled Inkers | Inker | Whorlback | Ink and a thick shell |
| Jetting Inkers | Inker | Jetter | Ink at speed |
| Bearing Inkers | Inker | Tidebearer | Ink that guards the rider |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Rammers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Battering Shells | Rammer and Whorlback | Hard charges from a thick shell |
| Torpedoes | Jetter and Rammer | Hard charges at speed |
| War Galleys | Rammer and Tidebearer | Hard charges that guard the rider |
| Murk Rams | Inker and Rammer | Hard charges and ink |
| Swift Shells | Jetter and Whorlback | A thick shell at speed |
| Shell Fortresses | Whorlback and Tidebearer | A thick shell that guards the rider |
| Ink Shells | Inker and Whorlback | A thick shell and ink |
| Tide Couriers | Jetter and Tidebearer | Speed that guards the rider |
| Squid Runners | Inker and Jetter | Speed and ink |
| Murk Bearers | Inker and Tidebearer | Ink that guards the rider |

## Neutral

A neutral zombie nautilus has no genes, or no family points.
It is a plain zombie nautilus, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A zombie nautilus region map, and the gene textures and gene models on the nautilus model, with both variant textures | All visuals |
| A charge at the target of its rider | Rammers and the Rammer genes |
| The damage, the time between, and the push of its charge | Rammers, Deep Ram, Quick Ram, Tide Charge, and the downsides |
| A boat break, a shield stop, and an air loss from its charge | Hull Ram, Shield Ram, Breath Ram |
| A self effect after a charge, and after an ink squirt | Wake Jet, Ink Rush |
| Less damage to the riders, also by damage type | Tidebearers, Shell Wall, Tide Shield, Trident Guard |
| An effect on a rider at low health, an effect on the riders at death, and a translucent rider | Kin Current, Last Current, Ink Veil |
| More effects, more reach, and a shorter cooldown on the ink | Thick Ink, Wide Ink, Sour Ink, Quick Ink |
| A translucent time after a projectile hit | Ink Screen |
| A check that the reused effects work on a zombie nautilus: armor, thorns, heal once, regenerate, sidestep in water, a crawl to water, flee, sun shade, effect on hurt with ink, and the damage taken effects | Many genes |
