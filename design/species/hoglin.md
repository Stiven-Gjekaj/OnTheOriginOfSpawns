# Hoglin

This document defines the families and the genes of the hoglin.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The hoglin is a large boar of the crimson forests. It has 40 health and does
6 melee damage. Each hit throws the target up and back. It attacks players
on sight. It runs from warped fungus, nether portals, and respawn anchors.
Players can breed it with crimson fungus, and 20% spawn as babies. In the
Overworld, it turns into a zoglin after 15 seconds. Piglins hunt it.
Its genes fit the tusks, the charge, the thick hide, the herd, and its
fears. It shares no genes and no families with the other species.

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

### Tuskers

Tuskers throw their prey high. Each throw takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits throw the target 50% higher and farther | It attacks 10% slower |
| Tier 2 | Its hits throw the target 30% higher and farther | It attacks 15% slower |
| Tier 1 | Its hits throw the target 20% higher and farther | It attacks 20% slower |

### Chargers

Chargers run at their prey. They tire fast.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed while it chases a player farther than 4 blocks | 10% less maximum health |
| Tier 2 | 20% more speed while it chases a player farther than 4 blocks | 15% less maximum health |
| Tier 1 | 15% more speed while it chases a player farther than 4 blocks | 20% less maximum health |

### Thickhides

Thickhides have a hide like stone. The hide is heavy.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor | 10% less speed |
| Tier 2 | 4 more armor | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Herdkin

Herdkin keep the herd together. Alone, they are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When something hurts it, the hoglins in 16 blocks attack the attacker | 10% less damage |
| Tier 2 | When something hurts it, the hoglins in 12 blocks attack the attacker | 15% less damage |
| Tier 1 | When something hurts it, the hoglins in 8 blocks attack the attacker | 20% less damage |

### Fearless

Fearless do not run. They pay for it with their health.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It does not run from warped fungus, nether portals, or respawn anchors | 10% less maximum health |
| Tier 2 | It does not run from warped fungus or nether portals | 15% less maximum health |
| Tier 1 | It does not run from warped fungus | 20% less maximum health |

## Pressures

The hoglin uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `wolf`, and `shield`. It lives in
the Nether, so it does not use `sunlight`, `night`, or `day`. It needs no
new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Tusker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Tusks | Tusker | `melee` | 20% more melee damage | It attacks 10% slower | Head: long, curved tusks. This changes the model |
| Sky Toss | Tusker | `fall` | Its hits throw the target two times as high | 10% less maximum health | Head: an upturned snout |
| Gore | Tusker | `critical` | Its hits give Weakness for 3 seconds | 10% less damage | Head: blood-red tusk tips |
| Shield Hook | Tusker | `shield` | Its hits stop a shield for 4 seconds | 10% less speed | Head: hooked tusks. This changes the model |
| Golem Toss | Tusker | `golem` | Two times the damage to golems | 10% less speed | Head: chipped, cracked tusks |
| Ram | Tusker, sub Charger | `unreached` | Its first hit after a run of 6 blocks does 30% more damage | 2 less armor | Head: a flat, hard brow |

### Charger genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Stampede | Charger | `crowd` | 20% more speed with 2 or more hoglins near it | 10% less speed alone | Legs: dust on the legs |
| Long Run | Charger | `unreached` | Two times the follow range | 10% less maximum health | Legs: long, thin legs |
| Hoof Leap | Charger | `fall` | It leaps at its target from 3 to 6 blocks away | 10% less maximum health | Legs: strong back legs |
| Sidestep Hoof | Charger | `projectile` | It steps aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Body: a low, lean body |
| Wild Run | Charger | `spotted` | 20% more speed | 10% more damage taken | Body: a stripe of wild bristles |
| Dust Kick | Charger, sub Tusker | `melee` | Its hits give Blindness for 2 seconds | 10% less damage | Legs: dirty hooves |

### Thickhide genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rock Hide | Thickhide | `melee` | 4 more armor | 10% less speed | Body: gray, stony patches |
| Bristle Mat | Thickhide | `projectile` | 30% less damage from projectiles | 10% less speed | Body: thick, long bristles. This changes the model |
| Blast Hide | Thickhide | `explosion` | Half the damage from explosions | 10% less speed | Body: a scorched hide |
| Root Feet | Thickhide | `golem` | Full knockback resistance | 10% less speed | Legs: thick, dark hooves |
| Scar Hide | Thickhide | `critical` | 6 more armor while its health is under half | 10% less maximum health | Body: old scars |
| Barbed Hide | Thickhide, sub Herdkin | `crowd` | An attacker that hits it in melee takes 1 damage | 10% less maximum health | Body: sharp bristles on the back. This changes the model |

### Herdkin genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Herd Call | Herdkin | `spotted` | When it sees a player, it calls the hoglins in 24 blocks | Players hear it from two times farther | Head: a wide, open snout |
| Close Ranks | Herdkin | `projectile` | 20% less damage taken with 2 or more hoglins near it | 20% more damage taken alone | Body: a thick mane |
| Big Litter | Herdkin | `alone` | When it spawns, 1 more hoglin spawns with it | 10% less maximum health | Body: a round belly |
| Mourning Herd | Herdkin | `critical` | When a hoglin dies in 16 blocks, it gets Strength for 10 seconds | 10% less maximum health | Head: dark, wet eyes |
| Herd Rally | Herdkin | `melee` | 5% more damage for each hoglin in 8 blocks, up to 20% | 2 less armor | Head: red ear tags |
| Fungus Guard | Herdkin, sub Fearless | `wolf` | The hoglins in 8 blocks do not run from warped fungus | 10% less speed | Body: red crimson moss on the back |

### Fearless genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Warp Eater | Fearless | `shield` | It does not run from warped fungus, and it eats warped fungus items on the ground | 10% less maximum health | Head: blue fungus stains on the snout |
| Portal Walker | Fearless | `alone` | It does not run from nether portals | 10% less maximum health | Body: purple sparks on the hide |
| Anchor Brave | Fearless | `player_death` | It does not run from respawn anchors | 10% less maximum health | Head: a glowing purple eye |
| Steady Nerves | Fearless | `crowd` | It does not run from piglins, and it attacks them | 10% less speed | Head: a calm, steady look |
| Overworld Hide | Fearless | `on_land` | In the Overworld, it turns into a zoglin after 60 seconds, not 15 | 10% less maximum health | Body: a pale, gray hide |
| Bold Charge | Fearless, sub Charger | `unreached` | 20% more speed while it chases a player that holds warped fungus | 10% less maximum health | Legs: blue stripes |

## Gene details

| Gene | Rules |
|---|---|
| Tuskers, Sky Toss | They change the throw of the vanilla hoglin hit. The vanilla throw is up and back |
| Tuskers, Long Tusks | "Attacks slower" changes the time between two melee attacks |
| Chargers | It counts while its target is a player farther than 4 blocks |
| Herdkin | It works as the cave spider Brood Call, with hoglins |
| Fearless, Warp Eater, Portal Walker, Anchor Brave | They stop the vanilla fears of the hoglin |
| Gore | It gives the effect on each hit |
| Shield Hook | It works as the slime Shield Melt |
| Golem Toss | "Golems" are the iron golem and the snow golem |
| Ram | It works as the spider Jumpers, after a run in place of a leap |
| Stampede, Close Ranks | They work as the pillager Formation |
| Hoof Leap | It works as the zombie Lunger |
| Sidestep Hoof | It works as the pillager Evasion |
| Barbed Hide | It works as the bogged Thornwrap |
| Herd Call | It works as the pillager War Horn |
| Big Litter | It works as the cave spider Egg Layer, with genes from the pool. The extra hoglin is a baby with a 50% chance |
| Mourning Herd | It works as the pillager Grudge, on itself |
| Herd Rally | It works as the pillager Rally Cry |
| Fungus Guard | The hoglins in the radius act as if they had Warp Eater, while they are near it |
| Steady Nerves | Piglins still hunt it |
| Overworld Hide | It changes the vanilla time of 15 seconds |

## Equipment

- The hoglin holds nothing. It drops its vanilla items.
- Players can put no saddle or armor on it.

## Conversion

- In the Overworld, a hoglin turns into a zoglin after 15 seconds. The zoglin
  gets new genes from its own pool. It keeps none of the genes of the
  hoglin.

## Babies and jockeys

- The vanilla babies stay: 20% of the hoglins spawn as babies, and players
  can breed hoglins. A baby from breeding gets genes from the pool, as a new
  mob. The breeding of mobs comes in milestone 6.
- A baby piglin can ride a hoglin, as in vanilla. It does not get a gene for
  this.

## Body

- The hoglin has the hoglin model: a large head with tusks and ears, a body
  with a mane, and four legs. Its texture is 128 x 64.
- It needs a new region map, `textures/region/hoglin.png`. The zoglin uses
  the same map.
- The gene models use the hoglin bones. Check them against the vanilla
  model, as for the witch, also the tusks and the mane.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Rushing Tuskers | Tusker | Charger | A throw at the end of a run |
| Armored Tuskers | Tusker | Thickhide | A throw from a heavy body |
| Herd Tuskers | Tusker | Herdkin | A throw in a herd |
| Bold Tuskers | Tusker | Fearless | A throw that does not stop |
| Goring Runners | Charger | Tusker | A run with a hard throw |
| Heavy Runners | Charger | Thickhide | A heavy run |
| Herd Runners | Charger | Herdkin | A run in a herd |
| Bold Runners | Charger | Fearless | A run with no fear |
| Tusked Walls | Thickhide | Tusker | A wall with tusks |
| Rolling Walls | Thickhide | Charger | A wall that runs |
| Herd Walls | Thickhide | Herdkin | A wall in a herd |
| Bold Walls | Thickhide | Fearless | A wall with no fear |
| Tusk Herds | Herdkin | Tusker | A herd with tusks |
| Running Herds | Herdkin | Charger | A herd that runs |
| Shielded Herds | Herdkin | Thickhide | A herd behind a hide |
| Bold Herds | Herdkin | Fearless | A herd with no fear |
| Wild Tuskers | Fearless | Tusker | No fear and long tusks |
| Wild Runners | Fearless | Charger | No fear and a fast run |
| Wild Walls | Fearless | Thickhide | No fear and a thick hide |
| Wild Herds | Fearless | Herdkin | No fear in a herd |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Tuskers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Battering Rams | Charger and Tusker | A run with a throw |
| Iron Boars | Thickhide and Tusker | A heavy throw |
| Tusk Clans | Herdkin and Tusker | A clan with tusks |
| Mad Boars | Fearless and Tusker | A throw with no fear |
| Siege Boars | Charger and Thickhide | A heavy run |
| Stampede Clans | Charger and Herdkin | A clan that runs |
| Reckless Runners | Charger and Fearless | A run with no fear |
| Stone Clans | Herdkin and Thickhide | A clan behind a hide |
| Iron Hogs | Fearless and Thickhide | A wall with no fear |
| Wild Clans | Fearless and Herdkin | A clan with no fear |

## Neutral

A neutral hoglin has no genes, or no family points.
It is a plain hoglin, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A hoglin region map, and the gene textures and gene models on the hoglin model | All visuals |
| The throw of the hoglin hit, higher and farther | Tuskers, Sky Toss |
| Speed while it chases a player farther than a distance | Chargers |
| A call when something hurts it, to attack the attacker | Herdkin |
| The fears of the hoglin, one by one, and for the hoglins near it | Fearless, Warp Eater, Portal Walker, Anchor Brave, Fungus Guard |
| Eating warped fungus items | Warp Eater |
| No fear of piglins, and an attack on them | Steady Nerves |
| The time of the Overworld conversion | Overworld Hide |
| A first hit after a run | Ram |
| Speed while its target holds warped fungus | Bold Charge |
| Thorns damage on melee attackers | Barbed Hide |
| A brood with babies | Big Litter |
| An effect on itself when a hoglin dies near it | Mourning Herd |
| A check that the reused effects work on a hoglin: effect on hit, shield break, damage to, leap, sidestep, call on sight, brood, rally, damage when grouped and alone, attributes, attribute when, and the damage taken effects | Many genes |
