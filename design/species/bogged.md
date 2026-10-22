# Bogged

This document defines the families and the genes of the bogged.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The bogged lives in swamps, in mangrove swamps, and in trial chambers. It
shoots arrows that give Poison, it shoots slower than the skeleton, and it has
less health. Mushrooms grow on it, and a player can shear them off. Its genes
fit fungus, mud, poison, vines, and the moss that hides it.
It shares no genes and no families with the skeleton, the stray, or the
wither skeleton.

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

### Sporecaps

Sporecaps grow thick fungus. The fungus burns easily.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When it is hurt, players in 4 blocks get Nausea for 4 seconds, each 10 seconds | 25% more fire damage |
| Tier 2 | When it is hurt, players in 3 blocks get Nausea for 3 seconds, each 15 seconds | 50% more fire damage |
| Tier 1 | When it is hurt, players in 2 blocks get Nausea for 2 seconds, each 20 seconds | 75% more fire damage |

### Mirefolk

Mirefolk are at home in water and mud. On dry ground, they are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed and 2 more armor in water or on mud | 10% less speed on dry ground |
| Tier 2 | 20% more speed in water or on mud | 20% less speed on dry ground |
| Tier 1 | 10% more speed in water or on mud | 25% less speed on dry ground |

### Venomers

Venomers shoot stronger poison. Their arrows are weaker.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | The Poison from its arrows lasts two times longer and is one level stronger | 10% less arrow damage |
| Tier 2 | The Poison from its arrows lasts two times longer | 20% less arrow damage |
| Tier 1 | The Poison from its arrows lasts 50% longer | 25% less arrow damage |

### Vinebound

Vinebound bogged are wrapped in vines that hold the player. The vines make
them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each arrow hit has a 50% chance to root the target for 1 second | 10% less speed |
| Tier 2 | Each arrow hit has a 35% chance to root the target for 1 second | 15% less speed |
| Tier 1 | Each arrow hit has a 20% chance to root the target for 1 second | 20% less speed |

### Mosshides

Mosshides hide in the plants of the swamp. Their bodies are soft.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It is almost invisible in leaves, vines, and tall plants, and in water | 10% less maximum health |
| Tier 2 | It is almost invisible in leaves, vines, and tall plants, and in water | 20% less maximum health |
| Tier 1 | It is almost invisible in leaves, vines, and tall plants | 25% less maximum health |

## Pressures

The bogged uses the zombie pressures, and `night`, `wolf`, and `shield`. It
also uses these pressures.

| Pressure | A bogged death counts when | State |
|---|---|---|
| `dry` | The bogged dies in a biome that is not wet | New |
| `shears` | Someone sheared the bogged before its death | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Sporecap genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Puffball | Sporecap | `melee` | When it dies, it leaves a cloud of Nausea for 5 seconds | 4 less maximum health | Head: a large puffball mushroom on the skull. This changes the model |
| Shelfback | Sporecap | `projectile` | 4 more armor | 10% less speed | Back: shelf fungus in steps down the spine. This changes the model |
| Mushroom Shot | Sporecap, sub Venomer | `spotted` | Its arrows give Nausea for 4 seconds | 10% less arrow damage | Arms: red mushroom caps on the bow arm. This changes the model |
| Regrowth | Sporecap | `shears` | After a shear, its mushrooms grow back after 30 seconds, and it heals 6 health | 25% more fire damage | Skull: small mushrooms in the eye sockets |
| Rot Mend | Sporecap | `critical` | It heals 1 health each 2 seconds while it stands on mud, moss, or mycelium | 4 less maximum health | Ribs: white threads of fungus between the ribs |
| Spore Lung | Sporecap | `crowd` | Players in 3 blocks get Nausea each 10 seconds | Players hear it from two times farther | Mouth: spores that drift from the jaw |

### Mirefolk genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Wader | Mirefolk | `in_water` | 40% more speed in water | 10% less speed on dry ground | Legs: mud up to the knees |
| Mudcake | Mirefolk | `projectile` | 30% less damage from projectiles in water or on mud | 15% less speed | Body: a cracked crust of dried mud |
| Mudburrow | Mirefolk, sub Mosshide | `unreached` | It hides under mud and rises next to a player | It cannot move for 1 second after it rises | Head: mud that drips from the skull |
| Mire Grip | Mirefolk | `boat` | Its arrows give Slowness II for 3 seconds to a target in water | 10% less arrow damage | Hands: webbed fingers of rotten skin |
| Swamp Gas | Mirefolk | `fire` | When it dies, it leaves a cloud of Poison for 4 seconds | 50% more explosion damage | Pelvis: bubbles of gas under the cloth |
| Leechbone | Mirefolk | `alone` | Each arrow hit heals it 1 health while it is in water or on mud | 2 less armor | Arms: black leeches on the forearms. This changes the model |

### Venomer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Venom Gland | Venomer | `projectile` | The Poison from its arrows lasts two times longer | It shoots 15% less often | Jaw: a green sac under the jaw. This changes the model |
| Toxic Tip | Venomer | `melee` | The Poison from its arrows is one level stronger | 10% less arrow damage | Fingers: green tips |
| Bile Shot | Venomer | `golem` | Its arrows give Hunger for 6 seconds | 10% less speed | Ribs: yellow stains |
| Rotting Touch | Venomer | `melee` | A player that hits it in melee gets Poison for 3 seconds | 2 less armor | Bones: green rot in the joints |
| Antivenom | Venomer | `wolf` | Poison and Wither do not affect it | 25% more fire damage | Eyes: green light in the sockets that glows. This changes the model |
| Blightburst | Venomer, sub Sporecap | `critical` | When it dies, players in 3 blocks get Poison for 5 seconds | 4 less maximum health | Chest: green pustules on the cloth |

### Vinebound genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rootshot | Vinebound | `unreached` | Each arrow hit roots the target for 1 second | 10% less arrow damage | Arms: roots that coil around the bow arm. This changes the model |
| Creeper Vines | Vinebound | `fall` | It climbs walls | 2 less armor | Legs: vines that hang from the legs. This changes the model |
| Thornwrap | Vinebound | `melee` | A player that hits it in melee takes 2 damage | 10% less speed | Body: thorny vines around the ribs |
| Snare | Vinebound, sub Mirefolk | `spotted` | A player in 3 blocks is rooted for 1 second, each 10 seconds | 10% less maximum health | Feet: roots that grow from the feet. This changes the model |
| Mossbed | Vinebound | `fire` | It heals 1 health each 3 seconds out of combat | 50% more fire damage | Shoulders: thick moss. This changes the model |
| Vine Swing | Vinebound | `fall` | No fall damage | 10% less maximum health | Arms: vines that trail from the hands. This changes the model |

### Mosshide genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Lily Pad | Mosshide | `spotted` | It is almost invisible in water | 10% less speed on land | Head: a lily pad on the skull. This changes the model |
| Ambush Shot | Mosshide | `unreached` | Its first arrow after 10 seconds out of combat does 50% more damage | It shoots 10% less often | Eyes: moss over one eye socket |
| Silent Rot | Mosshide | `spotted` | No step sounds and no idle sounds | 10% less arrow damage | Feet: wrapped in moss |
| Swamp Shroud | Mosshide, sub Vinebound | `alone` | At night, players more than 10 blocks away cannot see it | The sun burns it two times faster | Cloth: a hood of hanging moss. This changes the model |
| Bog Mist | Mosshide | `melee` | When it is hurt, the attacker gets Blindness for 1 second, each 15 seconds | 10% less maximum health | Body: a gray haze of mist on the cloth |
| Barkskin | Mosshide | `explosion` | 4 more armor | 15% less speed | Arms and legs: plates of wet bark |

## Gene details

| Gene | Rules |
|---|---|
| Mirefolk, Mudcake, Rot Mend, Leechbone | "Mud" means the blocks mud, muddy mangrove roots, and mangrove roots. "Moss" means moss blocks and moss carpet |
| Sporecaps, Spore Lung | The Nausea goes to players only |
| Venomers, Venom Gland, Toxic Tip | They change the Poison that the bogged puts on its arrows in vanilla, 5 seconds of Poison I. "One level stronger" is Poison II |
| Vinebound, Rootshot, Snare | A root is Slowness V for 1 second, so the target almost cannot move |
| Mosshides, Lily Pad | It is drawn translucent, like the zombie Shade. Other mobs still see it |
| Mosshides | "Tall plants" means tall grass, ferns, large ferns, and sugar cane. The feet or the eyes of the bogged must be in the block |
| Regrowth | Each shear counts, by a player or by a dispenser. A bogged with no mushrooms has no shear |
| Mudburrow | It works like the husk Sandswimmer, but only through mud |
| Ambush Shot | "Out of combat" means no hit to it and no hit by it for 10 seconds |

## Equipment

- A bogged keeps its bow. The bow is its natural weapon.
- Its arrows give Poison, as in vanilla. The Venomer genes make this effect
  stronger.
- Mobs with genes do not spawn with random armor. A bogged with genes has no
  armor and no enchanted bow from its spawn.

## Shearing

- A player can shear the mushrooms off a bogged, as in vanilla. Its genes stay.
- The `shears` pressure counts the death of a bogged that was sheared before
  its death. It favors Regrowth.

## Conversion

- The bogged does not convert to a different species, and no species converts
  to the bogged.

## Babies and jockeys

- Bogged have no babies and no jockeys in vanilla. They do not get a gene for
  them.

## Body

- The bogged has the body of the skeleton, and an outer layer with mushrooms
  and vines.
- It uses the skeleton region map.
- Marks on bones go under the outer layer, as on the stray. Marks on the
  outer layer use the target `outer`.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Rot Mounds | Sporecap | Mirefolk | Fungus that grows in the mud |
| Toadstools | Sporecap | Venomer | Poisonous caps that make the player sick |
| Fungal Knots | Sporecap | Vinebound | Fungus and roots that hold the player |
| Moldering | Sporecap | Mosshide | Soft rot that hides in the green |
| Bog Bodies | Mirefolk | Sporecap | The swamp dead, full of spores |
| Mire Snakes | Mirefolk | Venomer | Poison that comes out of the water |
| Quagmires | Mirefolk | Vinebound | Mud and roots that pull the player down |
| Fen Lurkers | Mirefolk | Mosshide | Shapes under the water and the moss |
| Blightcaps | Venomer | Sporecap | Poison arrows and sick clouds |
| Marsh Fangs | Venomer | Mirefolk | Poison archers that wade fast |
| Thorn Fangs | Venomer | Vinebound | Poison that holds the player still |
| Hidden Fangs | Venomer | Mosshide | Poison shots from where nobody looks |
| Root Lords | Vinebound | Sporecap | Roots that grow fungus |
| Mangrove Kin | Vinebound | Mirefolk | Roots in the water |
| Stranglers | Vinebound | Venomer | Vines with poison thorns |
| Green Snares | Vinebound | Mosshide | Unseen roots that wait |
| Mushroom Rings | Mosshide | Sporecap | Hidden fungus that makes the player sick |
| Reed Ghosts | Mosshide | Mirefolk | Shapes in the reeds and the water |
| Moss Vipers | Mosshide | Venomer | Unseen archers with poison |
| Overgrowths | Mosshide | Vinebound | The swamp that comes alive |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Sporecaps".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Mire Molds | Sporecap and Mirefolk | Fungus of the deep mud |
| Deathcaps | Sporecap and Venomer | Spores and poison together |
| Fungal Vines | Sporecap and Vinebound | Roots that carry spores |
| Spore Shades | Sporecap and Mosshide | Unseen clouds of sickness |
| Swamp Adders | Mirefolk and Venomer | Poison from the water |
| Mangrove Hands | Mirefolk and Vinebound | Roots that hold the player in the mud |
| Drowned Moss | Mirefolk and Mosshide | Hidden in water and moss |
| Thornvenoms | Venomer and Vinebound | Poison thorns that do not let go |
| Silent Venoms | Venomer and Mosshide | Poison from the dark green |
| Green Wardens | Vinebound and Mosshide | The swamp itself, standing up |

## Neutral

A neutral bogged has no genes, or no family points.
It is a plain bogged, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the bogged, and on its outer layer | All visuals |
| A state for water or mud under the mob, and for dry ground | Mirefolk, Wader, Mudcake, Leechbone |
| A state for mud, moss, or mycelium under the mob | Rot Mend |
| The change of the Poison on the arrows of the bogged | Venomers, Venom Gland, Toxic Tip |
| A heal on each arrow hit | Leechbone |
| The regrowth of sheared mushrooms, with a heal | Regrowth |
| A first arrow after a time out of combat that does more damage | Ambush Shot |
| The pressures `dry` and `shears` | Regrowth, and the wet and dry play of the swamp |
| A burrow through mud | Mudburrow |
