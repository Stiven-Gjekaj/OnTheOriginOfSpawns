# Camel husk

This document defines the families and the genes of the camel husk.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The camel husk is an undead camel of the desert. It has 32 health. When a husk
spawns in a natural way in the desert, it rides a camel husk one time in 10,
if there is room. That husk holds an iron spear, and a parched sits behind it.
The camel husk does not attack. Its riders fight. It walks slowly and it can
dash far. It cannot breed, and it has no babies. It goes away when no player
is near, as a monster does. Its genes fit its dash, its endurance, its stride,
the guard of its riders, and its kick. It shares no genes and no families with
the other species.

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

### Dashers

Dashers dash at the target of their rider. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each 8 seconds, it dashes at the target of its rider | 10% less maximum health |
| Tier 2 | Each 10 seconds, it dashes at the target of its rider | 15% less maximum health |
| Tier 1 | Each 12 seconds, it dashes at the target of its rider | 20% less maximum health |

### Humpbacks

Humpbacks heal from the water in their humps. They walk slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It heals 1 health each 2 seconds out of combat | 10% less speed |
| Tier 2 | It heals 1 health each 3 seconds out of combat | 15% less speed |
| Tier 1 | It heals 1 health each 4 seconds out of combat | 20% less speed |

### Dunewalkers

Dunewalkers walk fast. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed | 10% more damage taken |
| Tier 2 | 20% more speed | 15% more damage taken |
| Tier 1 | 15% more speed | 20% more damage taken |

### Bearers

Bearers guard their riders. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its riders take 30% less damage | 10% less maximum health |
| Tier 2 | Its riders take 20% less damage | 15% less maximum health |
| Tier 1 | Its riders take 15% less damage | 20% less maximum health |

### Sandkickers

Sandkickers kick the attackers that hurt them. They walk slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When a melee hit hurts it, it kicks the attacker for 4 damage | 10% less speed |
| Tier 2 | When a melee hit hurts it, it kicks the attacker for 3 damage | 15% less speed |
| Tier 1 | When a melee hit hurts it, it kicks the attacker for 2 damage | 20% less speed |

## Pressures

The camel husk uses `melee`, `projectile`, `explosion`, `fire`, `fall`,
`shield`, `unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`, and
`player_death`. A camel husk without a rider still counts. It lives in the
desert and the sun does not hurt it, so it does not use `sunlight`, `night`,
or `day`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Dasher genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Dash | Dasher | `unreached` | Its dash goes two times as far | 10% less maximum health | Legs: raised, lean muscles on the hips and the shoulders. This changes the model |
| Ram Dash | Dasher | `melee` | Its dash hits the mobs and players in its path for 4 damage | 10% less maximum health | Head: a hard, scarred brow |
| Quick Dash | Dasher | `spotted` | It dashes 50% more often | 10% less maximum health | Head: swept-back wind lines down the neck |
| Spear Charge | Dasher | `golem` | After a dash, the next hit of its rider does 50% more damage | 10% less maximum health | Body: a red band round the body |
| Dust Dash | Dasher | `crowd` | When it dashes, the mobs and players in 3 blocks get Blindness for 2 seconds | 10% less speed | Legs: dusty, brown feet |
| Sand Leap | Dasher, sub Dunewalker | `fall` | Its dash goes 2 blocks higher than a vanilla dash, and it takes no fall damage | 10% less maximum health | Legs: pale wraps round the ankles |

### Humpback genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Water Hump | Humpback | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Body: a large, swollen hump. This changes the model |
| Thick Hide | Humpback | `projectile` | 30% less damage from projectiles | 10% less speed | Body: thick, cracked hide |
| Dried Flesh | Humpback | `fire` | Half the damage from fire | 10% less speed | Body: dark, dried patches |
| Deep Reserve | Humpback | `alone` | It heals 1 health each 3 seconds out of combat | 10% less speed | Body: soft blue-gray veins on the hump |
| Bone Hump | Humpback | `explosion` | Half the damage from explosions | 10% less speed | Body: bone ridges on the hump |
| Stubborn Hooves | Humpback, sub Bearer | `melee` | Hits do not push it back | 10% less speed | Legs: thick, knotted knees |

### Dunewalker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Swift Stride | Dunewalker | `unreached` | 20% more speed | 10% less maximum health | Legs: wide, pale foot pads |
| Sand Veil | Dunewalker | `spotted` | It is near invisible in the dark | 10% less maximum health | Body: sandy, faded skin |
| Sure Feet | Dunewalker | `fall` | No fall damage | 10% less maximum health | Legs: hard, wide feet. This changes the model |
| Night Trek | Dunewalker | `alone` | 20% more speed at night | 10% less maximum health | Head: pale, glowing eyes, and pale hoof prints on the legs. This changes the model |
| Shy Step | Dunewalker | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Head: tall, alert ears. This changes the model |
| Escape Run | Dunewalker, sub Dasher | `critical` | At under 30% health, it runs away from its target fast for 4 seconds | 10% less maximum health | Legs: torn wraps on the legs |

### Bearer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Saddle Wall | Bearer | `projectile` | Its riders take 30% less damage from projectiles | 10% less maximum health | Body: a thick saddle blanket |
| High Seat | Bearer | `melee` | Its riders take 20% less melee damage | 10% less speed | Body: a tall wooden saddle frame. This changes the model |
| Rider Bond | Bearer | `critical` | When a rider is under half health, the rider heals 4 health at once, one time each 30 seconds | 10% less maximum health | Head: a woven cord with a green charm round the neck |
| Fire Cloth | Bearer | `fire` | Its riders take half the damage from fire | 10% less speed | Body: a red cloth over the back |
| Last Carry | Bearer | `player_death` | When it dies, its riders get Resistance for 10 seconds | 10% less maximum health | Body: long, torn tassels |
| Iron Studs | Bearer, sub Sandkicker | `golem` | Its riders do two times the damage to golems | 10% less speed | Body: iron studs on the saddle |

### Sandkicker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Hard Kick | Sandkicker | `melee` | Its kick does 2 more damage | 10% less speed | Legs: dark, hard hooves |
| Sand Spray | Sandkicker | `spotted` | Its kick gives Blindness for 2 seconds | 10% less maximum health | Legs: sand caked on the feet |
| Golem Kick | Sandkicker | `golem` | Its kick does two times the damage to golems | 10% less speed | Legs: iron gray shins |
| Shield Kick | Sandkicker | `shield` | Its kick stops a shield for 4 seconds | 10% less speed | Head: a grim, low brow |
| Sand Spit | Sandkicker | `unreached` | Each 6 seconds, it spits at its target for 2 damage and Slowness for 2 seconds | 10% less maximum health | Head: a wide, wet mouth |
| Sand Shove | Sandkicker, sub Dunewalker | `crowd` | Its kick pushes the attacker back two times as far | 10% less maximum health | Legs: thick, dusty legs. This changes the model |

## Gene details

| Gene | Rules |
|---|---|
| Dashers | A vanilla camel husk dashes only when a player rides it. A Dasher dashes while a mob rides it and the rider has a target in 16 blocks |
| Long Dash, Quick Dash | They change the dash of the Dashers. Without a Dasher family, the camel husk does not dash, and these genes do nothing |
| Ram Dash, Spear Charge, Dust Dash, Sand Leap | They act on each dash, also on a dash of a player that rides it. Dust Dash does not blind the camel husk or its riders. Spear Charge acts on the melee hits of mob riders only |
| Humpbacks, Deep Reserve | They work as the vindicator Thick Blood |
| Water Hump | It works as the vindicator Death Wish |
| Stubborn Hooves | It works as the hoglin Root Feet |
| Sand Veil | It works as the creeper Gloom |
| Night Trek | It uses the night state |
| Shy Step | It works as the pillager Evasion |
| Escape Run | It works as the blaze Smoke Dash. Its riders stay on it |
| Bearers | "Its riders" are the mobs that ride it. A player that rides it does not count |
| Rider Bond | It checks each rider. The riders are undead, and the undead ignore Regeneration, so it heals at once |
| Last Carry | It works as the pillager Grudge, on its riders only |
| Sandkickers | The kick is a hit of the camel husk. It also acts when no mob rides it |
| Sand Spit | It spits only while a mob rides it and the rider has a target. It works as a llama spit |
| Sand Shove | It works as the push of the hoglin Tuskers |

## Equipment

- The camel husk holds nothing. It drops its vanilla items.
- A husk that rides a camel husk keeps its iron spear, as DESIGN.md says.

## Conversion

- The camel husk does not convert to a different species, and no species
  converts to the camel husk.

## Babies and jockeys

- The camel husk has no babies. It does not get a gene for them.
- The husk and the parched on its back keep their own genes. The camel husk
  does not change them, except through the Bearer genes.

## Spawning

- A camel husk that spawns under a husk gets genes as a natural spawn.

## Body

- The camel husk has the camel model. Check the texture size and the bones
  against the vanilla model, as for the ghast.
- It needs a new region map, `textures/region/camel_husk.png`. The design uses
  the regions head, body, and legs. The saddle is not a region.
- The texture is 128 x 128, and only the high 64 rows are used. The bones are
  `body`, `head`, the two ears, and the four legs. The hump does not move,
  so the hump models use `body`.
- A natural camel husk has no saddle, so the saddle genes are gene art.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Hardy Dashers | Dasher | Humpback | Dashes that last |
| Striding Dashers | Dasher | Dunewalker | Fast dashes |
| Guarding Dashers | Dasher | Bearer | Dashes that guard the riders |
| Kicking Dashers | Dasher | Sandkicker | Dashes and kicks |
| Darting Humpbacks | Humpback | Dasher | Endurance and dashes |
| Striding Humpbacks | Humpback | Dunewalker | Endurance and a fast stride |
| Guarding Humpbacks | Humpback | Bearer | Endurance that guards the riders |
| Kicking Humpbacks | Humpback | Sandkicker | Endurance and kicks |
| Darting Dunewalkers | Dunewalker | Dasher | A fast stride and dashes |
| Hardy Dunewalkers | Dunewalker | Humpback | A fast stride that lasts |
| Guarding Dunewalkers | Dunewalker | Bearer | A fast stride that guards the riders |
| Kicking Dunewalkers | Dunewalker | Sandkicker | A fast stride and kicks |
| Darting Bearers | Bearer | Dasher | A guard and dashes |
| Hardy Bearers | Bearer | Humpback | A guard that lasts |
| Striding Bearers | Bearer | Dunewalker | A guard and a fast stride |
| Kicking Bearers | Bearer | Sandkicker | A guard and kicks |
| Darting Sandkickers | Sandkicker | Dasher | Kicks and dashes |
| Hardy Sandkickers | Sandkicker | Humpback | Kicks that last |
| Striding Sandkickers | Sandkicker | Dunewalker | Kicks and a fast stride |
| Guarding Sandkickers | Sandkicker | Bearer | Kicks that guard the riders |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Dashers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Oasis Runners | Dasher and Humpback | Dashes that last |
| Dune Lancers | Dasher and Dunewalker | Fast dashes |
| War Caravans | Bearer and Dasher | Dashes that guard the riders |
| Sand Rams | Dasher and Sandkicker | Dashes and kicks |
| Long Haulers | Dunewalker and Humpback | Endurance and a fast stride |
| Pack Humps | Bearer and Humpback | Endurance that guards the riders |
| Mule Kickers | Humpback and Sandkicker | Endurance and kicks |
| Swift Caravans | Bearer and Dunewalker | A fast stride that guards the riders |
| Dust Kickers | Dunewalker and Sandkicker | A fast stride and kicks |
| Spiteful Mounts | Bearer and Sandkicker | Kicks that guard the riders |

## Neutral

A neutral camel husk has no genes, or no family points.
It is a plain camel husk, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A camel husk region map, and the gene textures and gene models on the camel model | All visuals |
| A dash at the target of a mob rider, with a time between dashes | Dashers, Quick Dash |
| The length and the height of a dash, a hit in the path of a dash, a dust cloud, and no fall damage after a dash | Long Dash, Ram Dash, Dust Dash, Sand Leap |
| More damage on the next hit of a rider after a dash | Spear Charge |
| Less damage to the riders, also by damage type and by target | Bearers, Saddle Wall, High Seat, Fire Cloth, Iron Studs |
| An effect on a rider at low health, and an effect on the riders at death | Rider Bond, Last Carry |
| A kick at the attacker after a melee hit, with damage, an effect, a shield stop, golem damage, and a push | Sandkickers, Hard Kick, Sand Spray, Shield Kick, Golem Kick, Sand Shove |
| A spit at the target of its rider | Sand Spit |
| A check that the reused effects work on a camel husk: regenerate, heal once, knockback, translucent, speed at night, sidestep, flee, and the damage taken effects | Many genes |
