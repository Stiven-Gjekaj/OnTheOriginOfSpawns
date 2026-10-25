# Zombified Piglin

This document defines the families and the genes of the zombified piglin.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The zombified piglin is an undead piglin of the Nether. It has 20 health and
does 5 melee damage with a gold sword. Fire and lava do not hurt it. It is
neutral until a player hurts it. Then it and the zombified piglins near it
get angry at that player. A piglin or a piglin brute in the Overworld turns
into one, and so does a pig that lightning hits. Some spawn as babies, and
some babies ride chickens.
Its genes fit the shared anger, the gold of its body, the rot, the sword,
and the chase. It shares no genes and no families with the other species,
also not with the zombie.

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

### Vengefuls

Vengefuls spread their anger far. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When something hurts it, the anger reaches two times as far | 10% less maximum health |
| Tier 2 | When something hurts it, the anger reaches 1.5 times as far | 15% less maximum health |
| Tier 1 | When something hurts it, the anger reaches 1.25 times as far | 20% less maximum health |

### Gilded

Gilded have gold in their flesh. The gold is heavy.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 4 more armor | 10% less speed |
| Tier 2 | 3 more armor | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Festers

Festers rot what they hit. Their hits are soft.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits give Hunger for 6 seconds | 10% less damage |
| Tier 2 | Its hits give Hunger for 4 seconds | 15% less damage |
| Tier 1 | Its hits give Hunger for 2 seconds | 20% less damage |

### Bladesmen

Bladesmen swing their sword with force. Each swing takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more melee damage | It attacks 10% slower |
| Tier 2 | 20% more melee damage | It attacks 15% slower |
| Tier 1 | 15% more melee damage | It attacks 20% slower |

### Chasers

Chasers run down their prey. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 20% more speed | 10% more damage taken |
| Tier 2 | 15% more speed | 15% more damage taken |
| Tier 1 | 10% more speed | 20% more damage taken |

## Pressures

The zombified piglin uses `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `smite`, `wolf`, and `shield`. Fire
does not hurt it, so it does not use `fire`. The sun does not burn it, so it
does not use `sunlight`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Vengeful genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Blood Feud | Vengeful | `melee` | Its anger lasts two times as long | 10% less maximum health | Head: red, angry eyes |
| Horde Call | Vengeful | `spotted` | When something hurts it, it calls the zombified piglins in 32 blocks | Players hear it from two times farther | Head: a wide, open snout |
| Long Memory | Vengeful | `player_death` | After a player dies, it stays angry at that player for 5 minutes | 10% less speed | Head: a scar across the eye |
| War Squeal | Vengeful | `crowd` | 5% more damage for each angry zombified piglin in 8 blocks, up to 20% | 2 less armor | Head: torn ears |
| Wrath | Vengeful | `critical` | 30% more damage while its health is under half | 10% less maximum health | Body: open, red wounds |
| Avenge | Vengeful, sub Bladesman | `projectile` | When a projectile hurts it, it gets Strength for 5 seconds | 10% less maximum health | Arms: red cloth on the sword arm. This changes the model |

### Gilded genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Gold Flesh | Gilded | `melee` | 2 more armor | 10% less speed | Body: gold flecks in the flesh |
| Gold Plating | Gilded | `projectile` | 30% less damage from projectiles | 10% less speed | Body: gold plates on the chest. This changes the model |
| Gilded Skull | Gilded | `explosion` | Half the damage from explosions | 10% less speed | Head: a gold plate on the skull. This changes the model |
| Glint | Gilded | `alone` | Its gold sword has Sharpness I | 10% less maximum health | Arms: a shining gold hilt |
| Gold Rush | Gilded | `unreached` | 20% more speed while it chases a player | 10% less maximum health | Legs: gold stripes |
| Tarnish | Gilded, sub Fester | `shield` | Its hits damage armor two times as much | 10% less speed | Arms: green, tarnished gold |

### Fester genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rot Bite | Fester | `melee` | Its hits give Hunger for 5 seconds | 10% less damage | Head: rotten tusks |
| Nether Rot | Fester | `critical` | Its hits give Wither for 2 seconds | 10% less maximum health | Body: black rot spots |
| Stench | Fester | `crowd` | Players in 3 blocks get Nausea | 10% less speed | Body: green fumes. This changes the model |
| Corpse Gas | Fester | `explosion` | When it dies, it leaves a cloud of Poison | 10% less maximum health | Body: a swollen belly |
| Golem Rot | Fester | `golem` | Two times the damage to golems | 10% less speed | Arms: rot on the hands |
| Rot Sprint | Fester, sub Chaser | `unreached` | After it hits, it gets Speed for 3 seconds | 10% less damage | Legs: green rot patches and torn cloth on the legs |

### Bladesman genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Keen Edge | Bladesman | `melee` | 20% more melee damage | It attacks 15% slower | Arms: a notched sword arm |
| Sweep | Bladesman | `crowd` | Each hit also hurts the other players in 2 blocks with half the damage | 10% less maximum health | Arms: wide, strong shoulders |
| Riposte | Bladesman | `shield` | It blocks one melee hit each 8 seconds | 10% less damage | Arms: a gold guard on the arm. This changes the model |
| Gold Hilt | Bladesman | `critical` | Its first hit on a player does 30% more damage | 2 less armor | Body: a gold sword pommel on the belt |
| Sword Slap | Bladesman | `spotted` | Each hit has a 10% chance to make a player drop the item in the main hand | 10% less damage | Arms: a flat sword scar |
| Gilded Blade | Bladesman, sub Gilded | `golem` | Two times the damage to iron golems with its sword | 10% less speed | Arms: gold dust on the arm |

### Chaser genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sprint | Chaser | `unreached` | 20% more speed | 10% less maximum health | Legs: long, thin legs |
| Scent Tracker | Chaser | `alone` | Two times the follow range | 10% less speed | Head: a large snout |
| Vault | Chaser | `fall` | It leaps at its target from 3 to 6 blocks away | 10% less maximum health | Legs: strong, bent legs |
| Shrunken | Chaser | `crowd` | It spawns as a baby with a 25% chance | 10% less maximum health | Head: small, round ears |
| Duck | Chaser | `projectile` | It steps aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Body: a low, bent back |
| Pack Run | Chaser, sub Vengeful | `melee` | 20% more speed with 2 or more zombified piglins near it | 10% less speed alone | Legs: torn cloth at the knees |

## Gene details

| Gene | Rules |
|---|---|
| Vengefuls | The anger area of the vanilla zombified piglin grows by this multiplier |
| Blood Feud, Long Memory | They change the anger time of the zombified piglin |
| Horde Call | It works as the vindicator Alarm Bell |
| War Squeal | It works as the pillager Rally Cry, with angry zombified piglins |
| Wrath | "Its health is under half" is the `wounded` state |
| Glint | The enchantment is on the sword at the spawn. The sword drops as in vanilla |
| Gold Rush | It works as a speed while its target is a player |
| Tarnish | It works as the cave spider Corrosive |
| Stench | It works as the witch Bog Breath |
| Corpse Gas | It works as the zombie Bloat |
| Golem Rot, Gilded Blade | "Golems" are the iron golem and the snow golem. Gilded Blade counts only for the iron golem |
| Bladesmen, Keen Edge | "Attacks slower" changes the time between two melee attacks |
| Sweep | It works as the wither skeleton Cleave |
| Riposte | It works as the wither skeleton Parry |
| Sword Slap | It works as the pillager Pickpocket, with melee hits |
| Vault | It works as the zombie Lunger |
| Shrunken | Zombified piglins spawn as babies only through this gene, as zombies do with Runt. A baby with genes keeps them |
| Duck | It works as the pillager Evasion |
| Pack Run | It works as the pillager Formation, with speed |
| Avenge, Rot Sprint | The effect goes on the zombified piglin itself |

## Equipment

- A zombified piglin keeps its gold sword. It is its natural weapon.
- Mobs with genes do not spawn with random armor. A zombified piglin with
  genes has no random enchantment on its sword from its spawn, except
  Glint.
- A baby can ride a chicken, as in vanilla. It does not get a gene for this.

## Conversion

- A piglin or a piglin brute that turns into a zombified piglin gets new
  genes from the pool of the zombified piglin. It keeps none of its old
  genes, as for the other species.
- A pig that lightning hits turns into a zombified piglin, and it gets genes
  as a new mob.
- The zombified piglin does not convert to a different species.

## Babies and jockeys

- Zombified piglins spawn as babies only through the Shrunken gene.
- A baby zombified piglin can ride a chicken, as in vanilla.

## Body

- The zombified piglin has the piglin model: a wide head with ears and a
  snout, and the humanoid body. Its texture is 64 x 64, with an outer layer.
- It needs a new region map, `textures/region/piglin.png`. The piglin and the
  piglin brute use the same map.
- The gene models use the piglin bones. Check them against the vanilla
  model, as for the witch, also the ears.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Gilded Mobs | Vengeful | Gilded | An angry mob in gold |
| Rot Mobs | Vengeful | Fester | An angry mob that rots |
| Blade Mobs | Vengeful | Bladesman | An angry mob with swords |
| Running Mobs | Vengeful | Chaser | An angry mob that runs |
| Gold Avengers | Gilded | Vengeful | A gold wall that remembers |
| Gold Rotters | Gilded | Fester | A gold wall that rots |
| Gold Duelists | Gilded | Bladesman | A gold wall with a sword |
| Gold Runners | Gilded | Chaser | Gold that runs |
| Spiteful Rotters | Fester | Vengeful | Rot and spite |
| Gilded Rotters | Fester | Gilded | Rot in gold |
| Rot Blades | Fester | Bladesman | A rotten sword |
| Rot Runners | Fester | Chaser | Rot that runs |
| Grudge Blades | Bladesman | Vengeful | A sword with a grudge |
| Gold Blades | Bladesman | Gilded | A sword behind gold |
| Plague Blades | Bladesman | Fester | A sword that rots |
| Swift Blades | Bladesman | Chaser | A fast sword |
| Feud Runners | Chaser | Vengeful | A chase that does not end |
| Gilded Runners | Chaser | Gilded | A chase in gold |
| Plague Runners | Chaser | Fester | A chase that rots |
| Blade Runners | Chaser | Bladesman | A chase with a sword |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Vengefuls".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Gold Legions | Gilded and Vengeful | An angry legion in gold |
| Rot Legions | Fester and Vengeful | An angry legion that rots |
| Blade Legions | Bladesman and Vengeful | An angry legion with swords |
| Wild Hunts | Chaser and Vengeful | An angry hunt |
| Gold Plagues | Fester and Gilded | Rot in gold |
| Gold Knights | Bladesman and Gilded | Swords in gold |
| Gold Hounds | Chaser and Gilded | A fast chase in gold |
| Plague Knights | Bladesman and Fester | Rotten swords |
| Plague Hounds | Chaser and Fester | A rotten chase |
| Blade Hounds | Bladesman and Chaser | A fast sword |

## Neutral

A neutral zombified piglin has no genes, or no family points.
It is a plain zombified piglin, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A piglin region map, and the gene textures and gene models on the piglin model, with the outer layer | All visuals |
| The anger area and the anger time of the zombified piglin, and anger after the death of a player | Vengefuls, Blood Feud, Long Memory |
| A rally with angry allies only | War Squeal |
| An effect on the mob itself when something hurts it, and after it hits | Avenge, Rot Sprint |
| A spawn enchantment on the natural weapon | Glint |
| Speed while its target is a player | Gold Rush |
| A baby gene for the zombified piglin, and the tag `no_vanilla_babies` | Shrunken |
| Disarm on melee hits | Sword Slap |
| A check that the zombie code of the zombified piglin does not take the zombie genes or the zombie families | All genes |
| A check that the reused effects work on a zombified piglin: armor break, aura, death cloud, damage to, cleave, parry, first strike, leap, sidestep, call on hurt, speed when, and the damage taken effects | Many genes |
