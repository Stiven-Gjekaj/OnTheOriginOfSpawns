# Silverfish

This document defines the families and the genes of the silverfish.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The silverfish is a small bug that hides in stone. It has 8 health and does
1 melee damage. It hides in stone, cobblestone, deepslate, and stone bricks,
and the block becomes an infested block. When a player breaks an infested
block, a silverfish comes out. When something hurts a silverfish, it wakes the
hidden silverfish in 10 blocks across and 5 blocks up and down. It spawns from
infested blocks, from the spawner in a stronghold, and from the Infested
effect. Its genes fit the call to the nest, the bite, the fast scuttle, the
hide in stone, and the eggs. It shares no genes and no families with the other
species.

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

### Wakers

Wakers call the nest. Alone, they lose their prey.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When something hurts it, it wakes the hidden silverfish in 20 blocks across | 10% less follow range |
| Tier 2 | When something hurts it, it wakes the hidden silverfish in 14 blocks across | 15% less follow range |
| Tier 1 | When something hurts it, it wakes the hidden silverfish in 12 blocks across | 20% less follow range |

### Gnawers

Gnawers bite hard. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 2 more damage | 2 less maximum health |
| Tier 2 | 1 more damage | 3 less maximum health |
| Tier 1 | 1 more damage | 4 less maximum health |

### Skitterers

Skitterers run fast. Their shells are soft.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed | 10% more damage taken |
| Tier 2 | 20% more speed | 15% more damage taken |
| Tier 1 | 15% more speed | 20% more damage taken |

### Lodgers

Lodgers go back into the stone. They are slow in the open.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When a hit takes it under half health, it hides in a stone block next to it, one time each 20 seconds | 10% less speed |
| Tier 2 | When a hit takes it under half health, it hides in a stone block next to it, one time each 30 seconds | 15% less speed |
| Tier 1 | When a hit takes it under half health, it hides in a stone block next to it, one time each 40 seconds | 20% less speed |

### Infesters

Infesters put eggs in their prey. Their bite is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits give Infested for 12 seconds | 10% less damage |
| Tier 2 | Its hits give Infested for 6 seconds | 15% less damage |
| Tier 1 | Its hits give Infested for 4 seconds | 20% less damage |

## Pressures

The silverfish uses `fire`, `projectile`, `melee`, `explosion`, `unreached`,
`spotted`, `alone`, `crowd`, `critical`, `bane`, and `player_death`. It lives
in stone, so it does not use `sunlight`, `night`, `day`, or the biome
pressures. A golem seldom fights it, so it does not use `golem`. It needs no
new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Waker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Nest Alarm | Waker | `crowd` | When something hurts it, it wakes the hidden silverfish in 2 times the vanilla range | 10% less damage | Body: pale rings round the segments |
| Stone Alarm | Waker | `spotted` | When it sees a player, it wakes the hidden silverfish in 8 blocks | Players hear it from two times farther | Head: long feelers. This changes the model |
| Swarm Rally | Waker | `alone` | When it sees a player, it calls the silverfish in 16 blocks to the player | 10% less speed | Tail: a bright tail tip |
| Blood Call | Waker | `player_death` | When it hurts a player, the silverfish in 8 blocks target that player | 10% less maximum health | Body: red bands on the front segments |
| Death Cry | Waker | `critical` | When it dies, it wakes the hidden silverfish in 10 blocks | 10% less maximum health | Back: a white stripe across the back |
| Wake Bite | Waker, sub Infester | `melee` | Its hits give Infested for 3 seconds | 10% less damage | Back: pale ivory bristles on the front layer |

### Gnawer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Flint Jaw | Gnawer | `melee` | 1 more damage | 10% less speed | Head: a dark gray jaw line |
| Rust Bite | Gnawer | `critical` | Its hits damage the armor of a player two times as much | 10% less maximum health | Back: orange bristles round the body |
| Gnaw Frenzy | Gnawer | `crowd` | 20% more damage with 2 or more silverfish near it | 20% less damage alone | Body: dark red spots |
| Tunnel Gnaw | Gnawer | `unreached` | When it cannot reach its target, it gnaws through a stone block in its way, one block each 2 seconds | 10% less maximum health | Head: long front teeth. This changes the model |
| Feeding Bite | Gnawer | `player_death` | Each hit heals it 1 health | 10% less damage | Tail: a dark red tail |
| Leap Bite | Gnawer, sub Skitterer | `projectile` | It leaps at its target from 4 blocks, one time each 5 seconds | 10% less maximum health | Back: a pale stripe down the back |

### Skitterer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Fast Scuttle | Skitterer | `unreached` | 20% more speed | 10% less maximum health | Tail: a forked tail. This changes the model |
| Dodge Scuttle | Skitterer | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Body: zigzag lines |
| Flat Body | Skitterer | `critical` | A critical hit does no extra damage to it | 10% less speed | Back: flat, wide back plates |
| Shell Slip | Skitterer | `melee` | Each melee hit has a 20% chance to miss it | 10% less damage | Body: a shiny, smooth shell |
| Wall Climb | Skitterer | `spotted` | It climbs walls, as a spider does | 10% less maximum health | Tail: a pale hook on the tail tip. This changes the model |
| Alarm Dash | Skitterer, sub Waker | `crowd` | When something hurts it, it and the silverfish near it get 20% more speed for 4 seconds | 10% less damage | Body: pale side spots |

### Lodger genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Cobble Skin | Lodger | `projectile` | 30% less damage from projectiles | 10% less speed | Body: gray stone plates |
| Deep Lodge | Lodger | `critical` | When a hit takes it under half health, it hides in a stone block next to it, one time each 30 seconds | 10% less speed | Body: cracks where the back meets the sides |
| Ember Shell | Lodger | `fire` | Half the damage from fire and lava | 10% less maximum health | Back: dark red back plates and red bristles |
| Stone Ambush | Lodger | `unreached` | Its first hit after it comes out of a block does 2 more damage | 10% less maximum health | Head: long brown jaws out of the sides of the head. This changes the model |
| Bane Plate | Lodger | `bane` | Bane of Arthropods does no extra damage to it and gives it no Slowness | 10% less speed | Back: near black plates and dark bristles |
| Egg Lodge | Lodger, sub Infester | `alone` | When it hides in a block, 2 silverfish come out of that block, not 1 | 10% less maximum health | Tail: small cream eggs on the tail. This changes the model |

### Infester genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Egg Bite | Infester | `melee` | Its hits give Infested for 6 seconds | 10% less damage | Head: pale yellow jaws |
| Brood Burst | Infester | `explosion` | When it dies, 2 silverfish come out | 10% less maximum health | Body: a swollen middle segment. This changes the model |
| Stone Seed | Infester | `spotted` | When it hides in a stone block, it also infests 2 stone blocks next to it | 10% less maximum health | Back: small cream eggs on the bristles |
| Egg Sac | Infester | `projectile` | When a projectile hurts it, a silverfish comes out, one time each 10 seconds | 10% less maximum health | Body: pale, swollen sides |
| Egg Spit | Infester | `unreached` | It spits an egg at its target from 6 blocks, one time each 6 seconds. The egg gives Infested for 4 seconds | 10% less damage | Head: a green mouth |
| Gut Bite | Infester, sub Gnawer | `player_death` | Its hits give Hunger for 4 seconds | 10% less maximum health | Body: a green belly |

## Gene details

| Gene | Rules |
|---|---|
| Wakers, Nest Alarm | The vanilla range is 10 blocks across and 5 blocks up and down. The new range keeps the same shape |
| Stone Alarm | It wakes the hidden silverfish one time each 10 seconds |
| Swarm Rally | It works as the pillager War Horn |
| Blood Call | Only the silverfish that have no target change their target |
| Death Cry | It also works when a projectile or a blast kills it |
| Infesters, Wake Bite, Egg Bite, Egg Spit | Infested is the vanilla effect. When something hurts the target, a silverfish can come out of it |
| Rust Bite | The silverfish does its normal damage. Only the loss of armor durability is two times as much |
| Gnaw Frenzy | It works as the pillager Formation |
| Tunnel Gnaw | It gnaws only the blocks that a silverfish can infest. The block drops nothing. It needs the mobGriefing game rule |
| Feeding Bite | It works as the bogged heal on hit |
| Leap Bite | The leap is the leap of a spider. It does the normal damage |
| Dodge Scuttle | It works as the pillager Evasion |
| Flat Body | The critical hit does the damage of a normal hit |
| Shell Slip | A missed hit does no damage and no knockback |
| Wall Climb | It climbs as a spider does |
| Lodgers, Deep Lodge | The block becomes an infested block. A silverfish that comes out of it has full health and the genes of the first one |
| Stone Ambush | The first hit must come in 10 seconds after it comes out |
| Bane Plate | Bane of Arthropods does the damage of the weapon without the enchantment |
| Egg Lodge | The second silverfish has the genes of the pool, not the genes of the first one |
| Brood Burst | It works as the evoker Vengeful Vexes, with silverfish. The new silverfish have the genes of the pool |
| Stone Seed | The blocks must be blocks that a silverfish can infest. It needs the mobGriefing game rule |
| Egg Sac | The new silverfish has the genes of the pool |
| Egg Spit | The egg is a slow projectile, as a llama spit |

## Equipment

- The silverfish holds nothing and drops nothing, as in vanilla.

## Conversion

- The silverfish does not convert to a different species, and no species
  converts to the silverfish.

## Babies and jockeys

- The silverfish has no babies. It does not get a gene for them.
- A silverfish does not ride a mob. It does not get a gene for this.

## Spawning

- A silverfish that comes out of an infested block, a spawner, or the
  Infested effect gets genes as a natural spawn.
- A silverfish that hides in a block keeps its genes. The silverfish that
  comes out of that block has the same genes.

## Body

- The silverfish has the silverfish model: seven body segments and three
  back layers. Its texture is 64 x 32.
- It needs a new region map, `textures/region/silverfish.png`. The design
  uses the regions head, body, back, and tail.
- The head is the first segment. The tail is the last two segments. The
  back is the tops of the middle segments and the bristles of the three back
  layers. Most pixels of the back layers are clear, so a mark on a layer
  colors its bristles.
- Seven genes change the model: Stone Alarm, Tunnel Gnaw, Stone Ambush, Fast
  Scuttle, Wall Climb, Egg Lodge, and Brood Burst. The tail is small, so
  three tail genes are models.
- The gene models use the silverfish bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Gnawing Wakers | Waker | Gnawer | A loud nest and a hard bite |
| Skittering Wakers | Waker | Skitterer | A loud nest and a fast scuttle |
| Lodged Wakers | Waker | Lodger | A loud nest and a hide in stone |
| Brooding Wakers | Waker | Infester | A loud nest and eggs in the prey |
| Waking Gnawers | Gnawer | Waker | A hard bite and a loud nest |
| Skittering Gnawers | Gnawer | Skitterer | A hard bite and a fast scuttle |
| Lodged Gnawers | Gnawer | Lodger | A hard bite and a hide in stone |
| Brooding Gnawers | Gnawer | Infester | A hard bite and eggs in the prey |
| Waking Skitterers | Skitterer | Waker | A fast scuttle and a loud nest |
| Gnawing Skitterers | Skitterer | Gnawer | A fast scuttle and a hard bite |
| Lodged Skitterers | Skitterer | Lodger | A fast scuttle and a hide in stone |
| Brooding Skitterers | Skitterer | Infester | A fast scuttle and eggs in the prey |
| Waking Lodgers | Lodger | Waker | A hide in stone and a loud nest |
| Gnawing Lodgers | Lodger | Gnawer | A hide in stone and a hard bite |
| Skittering Lodgers | Lodger | Skitterer | A hide in stone and a fast scuttle |
| Brooding Lodgers | Lodger | Infester | A hide in stone and eggs in the prey |
| Waking Infesters | Infester | Waker | Eggs in the prey and a loud nest |
| Gnawing Infesters | Infester | Gnawer | Eggs in the prey and a hard bite |
| Skittering Infesters | Infester | Skitterer | Eggs in the prey and a fast scuttle |
| Lodged Infesters | Infester | Lodger | Eggs in the prey and a hide in stone |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Wakers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Biting Nests | Waker and Gnawer | A loud nest and a hard bite |
| Alarm Runners | Waker and Skitterer | A loud nest and a fast scuttle |
| Stone Sleepers | Waker and Lodger | A loud nest and a hide in stone |
| Brood Callers | Waker and Infester | A loud nest and eggs in the prey |
| Darting Jaws | Gnawer and Skitterer | A hard bite and a fast scuttle |
| Rock Biters | Gnawer and Lodger | A hard bite and a hide in stone |
| Grub Jaws | Gnawer and Infester | A hard bite and eggs in the prey |
| Crack Runners | Skitterer and Lodger | A fast scuttle and a hide in stone |
| Egg Runners | Skitterer and Infester | A fast scuttle and eggs in the prey |
| Nest Stones | Lodger and Infester | A hide in stone and eggs in the prey |

## Neutral

A neutral silverfish has no genes, or no family points.
It is a plain silverfish, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A silverfish region map, and the gene textures and gene models on the silverfish model | All visuals |
| The range of the wake call, a wake call when it sees a player, and a wake call at death | Wakers, Nest Alarm, Stone Alarm, Death Cry |
| The Infested effect from a hit and from a spit | Infesters, Wake Bite, Egg Bite, Egg Spit |
| A hide in stone when a hit takes it under half health | Lodgers, Deep Lodge |
| More silverfish from one infested block, and an infest of the blocks next to it | Egg Lodge, Stone Seed |
| A gnaw through stone | Tunnel Gnaw |
| More loss of armor durability from a hit | Rust Bite |
| No extra damage from a critical hit, and a chance that a melee hit misses | Flat Body, Shell Slip |
| No extra damage and no Slowness from Bane of Arthropods | Bane Plate |
| A climb on walls | Wall Climb |
| A silverfish that comes out when a projectile hurts it | Egg Sac |
| A first hit with more damage after it comes out of a block | Stone Ambush |
| A check that the reused effects work on a silverfish: calls, groups, leaps, sidesteps, heal on hit, the death brood, and the damage taken effects | Many genes |
