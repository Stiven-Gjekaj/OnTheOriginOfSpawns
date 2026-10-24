# Endermite

This document defines the families and the genes of the endermite.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The endermite is a small bug of the End. It has 8 health and does 2 melee
damage. A thrown ender pearl makes one with a 5% chance. It dies of age after
2 minutes, unless it has a name or is persistent. Endermen attack it.
Its genes fit the swarm, the hide in the ground, the bite that feeds, the
long life, and the warp of the End. It shares no genes and no families with
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

### Swarmers

Swarmers come in groups. Each one is small.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When it spawns, 2 more endermites spawn with it | 2 less maximum health |
| Tier 2 | When it spawns, 1 more endermite spawns with it | 3 less maximum health |
| Tier 1 | When it spawns, 1 more endermite spawns with it, with a 50% chance | 4 less maximum health |

### Tunnelers

Tunnelers hide in the ground and wait. They are slow on the surface.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It hides under the ground and comes out when a player is in 6 blocks | 10% less speed |
| Tier 2 | It hides under the ground and comes out when a player is in 4 blocks | 15% less speed |
| Tier 1 | It hides under the ground and comes out when a player is in 3 blocks | 20% less speed |

### Leeches

Leeches feed on their prey. Their bite is small.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit heals it 2 health | 10% less damage |
| Tier 2 | Each hit heals it 1 health | 15% less damage |
| Tier 1 | Each hit heals it 1 health, with a 50% chance | 20% less damage |

### Elders

Elders live longer. Age makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It lives 6 minutes, not 2 | 10% less speed |
| Tier 2 | It lives 4 minutes, not 2 | 15% less speed |
| Tier 1 | It lives 3 minutes, not 2 | 20% less speed |

### Warpers

Warpers teleport as an enderman does. The warp makes them weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It teleports next to its target, one time each 8 seconds | 10% less maximum health |
| Tier 2 | It teleports next to its target, one time each 12 seconds | 15% less maximum health |
| Tier 1 | It teleports next to its target, one time each 16 seconds | 20% less maximum health |

## Pressures

The endermite uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `night`, `day`, `wolf`, and `shield`.
The sun does not burn it, so it does not use `sunlight`. It needs no new
pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Swarmer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Brood Split | Swarmer | `critical` | When it dies, 2 endermites come out | 2 less maximum health | Body: a swollen back segment |
| Mite Call | Swarmer | `spotted` | When it sees a player, it calls the endermites in 16 blocks | Players hear it from two times farther | Head: long feelers. This changes the model |
| Pack Bite | Swarmer | `crowd` | 20% more damage with 2 or more endermites near it | 20% less damage alone | Body: bright purple spots |
| Swarm Shell | Swarmer | `projectile` | 20% less damage taken with 2 or more endermites near it | 20% more damage taken alone | Body: shiny shell plates |
| Pearl Brood | Swarmer | `alone` | A thrown ender pearl makes it with a 10% chance, not 5% | 2 less maximum health | Body: a pearl on the back. This changes the model |
| Hive Bite | Swarmer, sub Leech | `melee` | Its hits give Hunger for 3 seconds | 10% less maximum health | Head: red jaws |

### Tunneler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Stone Hide | Tunneler | `spotted` | It can hide in stone, deepslate, and end stone | 10% less speed | Body: gray, stone-colored plates |
| Pebble Skin | Tunneler | `projectile` | 30% less damage from projectiles | 10% less speed | Body: small pebbles on the back |
| Ambush Bite | Tunneler | `melee` | Its first hit on a player does 50% more damage | 2 less armor | Head: wide jaws. This changes the model |
| Tunnel Sense | Tunneler | `alone` | It sees invisible players in 8 blocks | 10% less speed | Head: pale feelers |
| Rubble Burst | Tunneler | `explosion` | 50% less damage from explosions | 10% less maximum health | Body: dust on the shell |
| Warp Burrow | Tunneler, sub Warper | `unreached` | When it comes out of the ground, it comes out next to the player | 10% less maximum health | Tail: a purple glow on the tail |

### Leech genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Blood Bite | Leech | `melee` | Each hit heals it 1 health | 10% less speed | Head: dark red jaws |
| Hunger Bite | Leech | `crowd` | Its hits give Hunger for 5 seconds | 10% less damage | Body: a green belly |
| Xp Leech | Leech | `critical` | Each hit takes 1 experience point from a player | 10% less maximum health | Body: green glow spots |
| Weak Bite | Leech | `spotted` | Its hits give Weakness for 3 seconds | 10% less maximum health | Head: gray jaws |
| Life Drain | Leech, sub Elder | `alone` | Each hit adds 10 seconds to its life | 10% less damage | Tail: a long, thin tail |
| Toxic Bite | Leech | `shield` | Its hits give Poison for 2 seconds | 10% less maximum health | Head: green fangs. This changes the model |

### Elder genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Life | Elder | `alone` | It lives 4 minutes, not 2 | 10% less damage | Body: faded, pale segments |
| Ageless | Elder | `critical` | It does not die of age | 4 less maximum health | Body: white, old shell |
| Hard Shell | Elder | `melee` | 2 more armor | 10% less speed | Body: thick shell plates. This changes the model |
| Old Kin | Elder, sub Swarmer | `crowd` | When it dies of age, 2 endermites come out | 10% less maximum health | Tail: small eggs on the tail |
| Calm Mind | Elder | `golem` | Iron golems do not target it | 10% less damage | Head: closed, calm eyes |
| Thick Shell | Elder | `explosion` | 30% less damage from explosions | 10% less speed | Body: dark, thick shell |

### Warper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Mite Blink | Warper | `projectile` | When a projectile hurts it, it teleports away, one time each 8 seconds | 10% less maximum health | Body: purple sparks on the back |
| Warp Bite | Warper | `melee` | After it hits, it teleports behind its target, one time each 10 seconds | 10% less damage | Head: purple jaws |
| Kin Mask | Warper | `wolf` | Endermen do not target it | 10% less speed | Body: black, enderman-colored shell |
| Pearl Leap | Warper | `unreached` | When it cannot reach its target, it teleports next to it, one time each 10 seconds | 10% less maximum health | Tail: a pearl at the tail. This changes the model |
| Void Skin | Warper | `fall` | Half the fall damage | 10% less maximum health | Body: a dark purple shell |
| Ender Scent | Warper, sub Tunneler | `spotted` | The endermen in 16 blocks get angry at the player that it attacks | 10% less maximum health | Body: a purple mist on the shell |

## Gene details

| Gene | Rules |
|---|---|
| Swarmers | The extra endermites have the genes of the pool, not the genes of the first one |
| Brood Split | It works as the evoker Vengeful Vexes, with endermites. The new endermites live 2 minutes |
| Mite Call | It works as the pillager War Horn |
| Pack Bite, Swarm Shell | They work as the pillager Formation |
| Tunnelers, Stone Hide, Warp Burrow | They work as the zombie Burrower. The endermite rises only through the blocks in its tag |
| Ambush Bite | It works as the spider Trapdoors |
| Tunnel Sense | It works as the pillager Pathseeker |
| Leeches, Blood Bite | They work as the bogged heal on hit |
| Xp Leech | It works as the pillager Greed |
| Elders, Long Life, Life Drain | The life of an endermite is 2 minutes in vanilla. An endermite with a name or a persistent one does not die of age, as in vanilla |
| Calm Mind | It works as the evoker Mind Trick |
| Warpers, Pearl Leap, Warp Bite, Mite Blink | They use the same teleports as the enderman Phasers, Far Step, and Shadow Step |
| Kin Mask | It works as the evoker Mind Trick, with endermen |
| Ender Scent | Only the endermen that are not angry at another target change their target |

## Equipment

- The endermite holds nothing and drops nothing, as in vanilla.

## Conversion

- The endermite does not convert to a different species, and no species
  converts to the endermite.

## Babies and jockeys

- Endermites have no babies in vanilla. They do not get a gene for them.
- An endermite does not ride a mob. It does not get a gene for this.

## Body

- The endermite has the endermite model: a head and three body segments.
  Its texture is 64 x 32.
- It needs a new region map, `textures/region/endermite.png`. The design
  uses the regions head, body, and tail.
- The gene models use the endermite bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Hidden Swarms | Swarmer | Tunneler | A swarm from the ground |
| Hungry Swarms | Swarmer | Leech | A swarm that feeds |
| Old Swarms | Swarmer | Elder | A swarm that lasts |
| Warp Swarms | Swarmer | Warper | A swarm that warps |
| Nest Tunnelers | Tunneler | Swarmer | A nest in the ground |
| Trap Tunnelers | Tunneler | Leech | A bite from the ground |
| Deep Tunnelers | Tunneler | Elder | A long wait in the ground |
| Rift Tunnelers | Tunneler | Warper | A warp out of the ground |
| Swarm Leeches | Leech | Swarmer | Many small bites |
| Dirt Leeches | Leech | Tunneler | Bites from the ground |
| Old Leeches | Leech | Elder | Bites that keep it alive |
| Warp Leeches | Leech | Warper | Bites from nowhere |
| Brood Elders | Elder | Swarmer | An old brood |
| Stone Elders | Elder | Tunneler | An old mite in the stone |
| Blood Elders | Elder | Leech | An old mite that feeds |
| Void Elders | Elder | Warper | An old mite of the void |
| Swarm Warpers | Warper | Swarmer | A warping swarm |
| Dig Warpers | Warper | Tunneler | Warps into the ground |
| Bite Warpers | Warper | Leech | Warps and bites |
| Old Warpers | Warper | Elder | Warps for a long time |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Swarmers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Mite Nests | Tunneler and Swarmer | A nest that waits in the ground |
| Feeding Swarms | Leech and Swarmer | A swarm that feeds |
| Ancient Broods | Elder and Swarmer | A brood that lasts |
| Rift Swarms | Swarmer and Warper | A swarm from the void |
| Pit Leeches | Tunneler and Leech | Bites from a pit |
| Deep Sleepers | Tunneler and Elder | A long sleep in the stone |
| Void Diggers | Tunneler and Warper | Warps through the ground |
| Elder Leeches | Elder and Leech | A bite that keeps it alive |
| Void Leeches | Leech and Warper | Bites from the void |
| Void Ancients | Elder and Warper | Old mites of the void |

## Neutral

A neutral endermite has no genes, or no family points.
It is a plain endermite, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| An endermite region map, and the gene textures and gene models on the endermite model | All visuals |
| More endermites at its spawn | Swarmers |
| The chance that an ender pearl makes an endermite | Pearl Brood |
| The life of an endermite, a life with no end, and more life on each hit | Elders, Long Life, Ageless, Life Drain |
| Endermites that come out when it dies of age | Old Kin |
| The enderman teleports on the endermite | Warpers, Mite Blink, Warp Bite, Pearl Leap |
| A call to the endermen to attack the target of the endermite | Ender Scent |
| A check that the reused effects work on an endermite: burrow, death spawn, call on sight, damage when grouped and alone, first strike, see invisible, heal on hit, effect on hit, leech xp, ignored by, and the damage taken effects | Many genes |
