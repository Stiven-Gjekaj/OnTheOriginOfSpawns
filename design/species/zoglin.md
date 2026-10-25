# Zoglin

This document defines the families and the genes of the zoglin.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The zoglin is an undead hoglin. A hoglin in the Overworld turns into one
after 15 seconds. It has 40 health and does 6 melee damage, and each hit
throws the target up and back. Fire and lava do not hurt it. It attacks all
mobs near it, except creepers and other zoglins. It fears nothing, and it
does not breed. A baby hoglin turns into a baby zoglin.
Its genes fit the gore of its tusks, the mad rush, the rot, the bones that
show through its hide, and the madness that makes it attack all. It shares
no genes and no families with the other species, also not with the hoglin.

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

### Gorers

Gorers drive their tusks deep. Each thrust takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more melee damage | It attacks 10% slower |
| Tier 2 | 20% more melee damage | It attacks 15% slower |
| Tier 1 | 15% more melee damage | It attacks 20% slower |

### Stampeders

Stampeders rush at all they see. Their rotten legs are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 20% more speed | 10% less maximum health |
| Tier 2 | 15% more speed | 15% less maximum health |
| Tier 1 | 10% more speed | 20% less maximum health |

### Rotters

Rotters spread rot with each hit. Their hits are soft.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits give Wither for 3 seconds | 10% less damage |
| Tier 2 | Its hits give Wither for 2 seconds | 15% less damage |
| Tier 1 | Its hits give Wither for 1 second | 20% less damage |

### Bonebacks

Bonebacks grow bone plates through the hide. The bone is heavy.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor | 10% less speed |
| Tier 2 | 4 more armor | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Maddened

Maddened lose all control. They attack players first, but they cannot stop.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It targets the nearest player before other mobs, in 24 blocks | 10% more damage taken |
| Tier 2 | It targets the nearest player before other mobs, in 20 blocks | 15% more damage taken |
| Tier 1 | It targets the nearest player before other mobs, in 16 blocks | 20% more damage taken |

## Pressures

The zoglin uses `projectile`, `melee`, `explosion`, `fall`, `unreached`,
`spotted`, `alone`, `crowd`, `critical`, `golem`, `player_death`,
`in_water`, `on_land`, `smite`, `wolf`, and `shield`. Fire does not hurt it,
so it does not use `fire`. The sun does not burn it, so it does not use
`sunlight`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Gorer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Deep Gore | Gorer | `melee` | 20% more melee damage | It attacks 15% slower | Head: long, broken tusks. This changes the model |
| High Toss | Gorer | `fall` | Its hits throw the target two times as high | 10% less maximum health | Head: tusks that point up |
| Bleed Out | Gorer | `critical` | 30% more damage to a player under half health | 10% less damage to a player over half health | Head: red, wet tusks |
| Hook Tusk | Gorer | `shield` | Its hits stop a shield for 4 seconds | 10% less speed | Head: hooked, bone tusks. This changes the model |
| Golem Gore | Gorer | `golem` | Two times the damage to golems | 10% less speed | Head: cracked tusks |
| Grim Charge | Gorer, sub Stampeder | `unreached` | Its first hit after a run of 6 blocks does 30% more damage | 2 less armor | Head: a cracked, flat brow |

### Stampeder genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Dead Run | Stampeder | `unreached` | 20% more speed | 10% less maximum health | Legs: thin, rotten legs |
| Hunt Sense | Stampeder | `alone` | Two times the follow range | 10% less speed | Head: a wide, rotten snout |
| Grave Leap | Stampeder | `fall` | It leaps at its target from 3 to 6 blocks away | 10% less maximum health | Legs: bent back legs with bones |
| Dodge Rot | Stampeder | `projectile` | It steps aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Body: a twisted, low body |
| Rot Rush | Stampeder | `crowd` | 20% more speed with 2 or more zoglins near it | 10% less speed alone | Legs: torn hide at the knees |
| Mire Trample | Stampeder, sub Rotter | `melee` | Its hits give Slowness for 2 seconds | 10% less damage | Legs: mud and rot on the hooves |

### Rotter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Wither Tusk | Rotter | `melee` | Its hits give Wither for 2 seconds | 10% less damage | Head: black tusk tips |
| Plague Breath | Rotter | `crowd` | Players in 3 blocks get Hunger | 10% less speed | Head: green fumes at the snout. This changes the model |
| Rot Cloud | Rotter | `critical` | When it dies, it leaves a cloud of Wither | 10% less maximum health | Body: a swollen, green belly |
| Grave Stench | Rotter | `spotted` | Players in 6 blocks get Nausea for 3 seconds, each 15 seconds | Players hear it from two times farther | Body: green, rotten patches |
| Corpse Feeder | Rotter | `player_death` | Each hit heals it 1 health | 2 less armor | Head: a torn, open jaw |
| Bone Rot | Rotter, sub Boneback | `explosion` | Half the damage from explosions | 10% less speed | Body: rotten bones in the hide |

### Boneback genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Bone Plates | Boneback | `melee` | 4 more armor | 10% less speed | Body: white bone plates. This changes the model |
| Rib Cage | Boneback | `projectile` | 30% less damage from projectiles | 10% less speed | Body: ribs through the hide |
| Bone Brace | Boneback | `golem` | Full knockback resistance | 10% less speed | Legs: thick bone on the legs |
| Skull Crest | Boneback | `critical` | 6 more armor while its health is under half | 10% less maximum health | Head: a bone crest on the head. This changes the model |
| Holy Bane | Boneback | `smite` | Smite does half the extra damage | 10% less maximum health | Body: dark runes on the bones |
| Spine Spikes | Boneback, sub Maddened | `crowd` | An attacker that hits it in melee takes 1 damage | 10% less maximum health | Body: bone spikes on the back. This changes the model |

### Maddened genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Player Hate | Maddened | `spotted` | It targets the nearest player before other mobs, in 24 blocks | 10% more damage taken | Head: white, mad eyes |
| Blind Fury | Maddened | `crowd` | It attacks 20% faster | 10% less maximum health | Head: red, bulging eyes |
| Mad Squeal | Maddened | `alone` | When something hurts it, it calls the zoglins in 24 blocks | Players hear it from two times farther | Head: a wide, screaming snout |
| Berserk Rot | Maddened | `critical` | 30% more damage while its health is under half | 10% less maximum health | Body: red, open wounds |
| Creeper Hunter | Maddened | `explosion` | It also attacks creepers, and creepers run from it | 10% less maximum health | Head: scorched, black ears |
| Rabid Bite | Maddened, sub Gorer | `melee` | Its hits give Weakness for 3 seconds | 10% less damage | Head: a foaming mouth |

## Gene details

| Gene | Rules |
|---|---|
| Gorers, Deep Gore, Blind Fury | "Attacks slower" or "faster" changes the time between two melee attacks |
| High Toss | It works as the hoglin Sky Toss |
| Bleed Out | It works as the vindicator Decapitator |
| Hook Tusk | It works as the slime Shield Melt |
| Golem Gore | "Golems" are the iron golem and the snow golem |
| Grim Charge | It works as the hoglin Ram |
| Grave Leap | It works as the zombie Lunger |
| Dodge Rot | It works as the pillager Evasion |
| Rot Rush | It works as the pillager Formation, with speed |
| Plague Breath | It works as the witch Bog Breath, with Hunger |
| Rot Cloud | It works as the zombie Bloat, with Wither |
| Grave Stench | It works as the vindicator Howl, with Nausea |
| Corpse Feeder | It works as the vindicator Bloodlust |
| Skull Crest | "Its health is under half" is the `wounded` state |
| Holy Bane | Smite does more damage to undead mobs. This gene halves that extra damage |
| Spine Spikes | It works as the bogged Thornwrap |
| Maddened, Player Hate | A vanilla zoglin targets the nearest mob. With these genes, it targets a player in the radius first |
| Mad Squeal | It works as the vindicator Alarm Bell |
| Berserk Rot | "Its health is under half" is the `wounded` state |
| Creeper Hunter | A vanilla zoglin does not attack creepers |

## Equipment

- The zoglin holds nothing. It drops rotten flesh, as in vanilla.

## Conversion

- A hoglin in the Overworld turns into a zoglin. The zoglin gets new genes
  from its own pool. It keeps none of the genes of the hoglin.
- The zoglin does not convert to a different species.

## Babies and jockeys

- A baby zoglin comes only from a baby hoglin that turns into one. Zoglins do
  not breed. They do not get a gene for babies.
- A zoglin does not ride a mob, and no mob rides it.

## Body

- The zoglin has the hoglin model, with a zoglin texture. Its texture is
  128 x 64.
- It uses the hoglin region map, `textures/region/hoglin.png`.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Rushing Gorers | Gorer | Stampeder | A gore at the end of a rush |
| Rotting Gorers | Gorer | Rotter | A gore that rots |
| Bony Gorers | Gorer | Boneback | A gore from a bone body |
| Mad Gorers | Gorer | Maddened | A gore with no control |
| Tusk Stampeders | Stampeder | Gorer | A rush with a gore |
| Rot Stampeders | Stampeder | Rotter | A rush that rots |
| Bone Stampeders | Stampeder | Boneback | A heavy rush |
| Mad Stampeders | Stampeder | Maddened | A rush with no control |
| Tusk Rotters | Rotter | Gorer | Rot with deep tusks |
| Rushing Rotters | Rotter | Stampeder | Rot that runs |
| Bone Rotters | Rotter | Boneback | Rot in bones |
| Mad Rotters | Rotter | Maddened | Rot with no control |
| Tusk Bonebacks | Boneback | Gorer | Bones with tusks |
| Rushing Bonebacks | Boneback | Stampeder | Bones that run |
| Rotting Bonebacks | Boneback | Rotter | Bones that rot |
| Mad Bonebacks | Boneback | Maddened | Bones with no control |
| Tusk Maniacs | Maddened | Gorer | Madness with tusks |
| Rushing Maniacs | Maddened | Stampeder | Madness that runs |
| Rotting Maniacs | Maddened | Rotter | Madness that rots |
| Bone Maniacs | Maddened | Boneback | Madness in bones |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Gorers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Death Charges | Gorer and Stampeder | A rush and a gore |
| Plague Tusks | Gorer and Rotter | A gore that rots |
| Bone Tusks | Boneback and Gorer | A gore from bones |
| Mad Tusks | Gorer and Maddened | A gore with no control |
| Plague Herds | Rotter and Stampeder | A rush of rot |
| Bone Herds | Boneback and Stampeder | A rush of bones |
| Mad Herds | Maddened and Stampeder | A rush with no control |
| Grave Boars | Boneback and Rotter | Bones and rot |
| Plague Maniacs | Maddened and Rotter | Rot with no control |
| Bone Fiends | Boneback and Maddened | Bones with no control |

## Neutral

A neutral zoglin has no genes, or no family points.
It is a plain zoglin, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet. The
hoglin work makes most of the throw and run parts.

| Need | Used by |
|---|---|
| The gene textures and gene models on the zoglin, with the hoglin region map | All visuals |
| The hoglin throw, run, and shield parts on the zoglin | High Toss, Grim Charge, Hook Tusk |
| A player first in the target choice of the zoglin, in a radius | Maddened, Player Hate |
| An attack on creepers, and creepers that run from it | Creeper Hunter |
| Less extra damage from Smite | Holy Bane |
| A check that the reused effects work on a zoglin: effect on hit, aura, death cloud, damage to, leap, sidestep, heal on hit, thorns, call on hurt, attack interval, attribute when, damage when, speed when, and the damage taken effects | Many genes |
