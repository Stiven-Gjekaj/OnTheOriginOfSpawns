# Creeper

This document defines the families and the genes of the creeper.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The creeper walks up to a player without a sound and explodes. It has 20
health and no weapon. Its fuse burns for 1.5 seconds, and its blast has a
radius of 3. A charged creeper has a blast with a radius of 6. Cats and
ocelots scare it. Its genes fit bigger blasts, faster rushes, ambushes from
cover, clouds of gas, and the charge of the storm. It shares no genes and no
families with the other species.

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

### Blasters

Blasters carry more powder. Their fuses burn longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its blast is 40% larger | Its fuse burns 10% longer |
| Tier 2 | Its blast is 30% larger | Its fuse burns 20% longer |
| Tier 1 | Its blast is 20% larger | Its fuse burns 25% longer |

### Rushers

Rushers run at the player. They carry less powder.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 25% more speed | Its blast is 10% smaller |
| Tier 2 | 20% more speed | Its blast is 20% smaller |
| Tier 1 | 15% more speed | Its blast is 25% smaller |

### Skulkers

Skulkers wait where the player cannot see them. They are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Players farther than 10 blocks do not see it | 10% less speed |
| Tier 2 | Players farther than 14 blocks do not see it | 15% less speed |
| Tier 1 | Players farther than 20 blocks do not see it | 20% less speed |

### Fumers

Fumers fill their blasts with gas. Their blasts hit softer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its blast leaves a cloud of Poison for 6 seconds | Its blast does 10% less damage |
| Tier 2 | Its blast leaves a cloud of Poison for 4 seconds | Its blast does 20% less damage |
| Tier 1 | Its blast leaves a cloud of Poison for 3 seconds | Its blast does 25% less damage |

### Thunderheads

Thunderheads carry the charge of the storm. Water and rain hurt them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 10% chance to spawn charged | Water and rain do 1 damage each 4 seconds |
| Tier 2 | 6% chance to spawn charged | Water and rain do 1 damage each 3 seconds |
| Tier 1 | 3% chance to spawn charged | Water and rain do 1 damage each 2 seconds |

## Pressures

A creeper that explodes does not die. Its blast removes it from the world.
So a blast does not change the pool, and only a creeper that dies before its
blast counts. A creeper whose blast kills a player counts for
`player_death`, and its genes get stronger.

The creeper uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`spotted`, `alone`, `crowd`, `critical`, `golem`, `player_death`,
`in_water`, `on_land`, `night`, `day`, and `shield`.
It does not use `unreached`, because nearly every creeper that dies has not
damaged a player. That pressure would win each time. The sun does not burn
it, and wolves do not attack it, so it does not use `sunlight` or `wolf`.
It also uses this pressure.

| Pressure | A creeper death counts when | State |
|---|---|---|
| `fuse` | The creeper dies while its fuse burns | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Blaster genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Big Bang | Blaster | `melee` | Its blast is 30% larger | Its fuse burns 15% longer | Body: a swollen belly with stretched skin. This changes the model |
| Shrapnel | Blaster, sub Rusher | `shield` | A shield does not block its blast | 10% less maximum health | Body: shards of iron in the skin |
| Golem Breaker | Blaster | `golem` | Its blast does two times the damage to iron golems | 10% less speed | Head: dark iron-gray spots |
| Chain Reaction | Blaster | `crowd` | When it explodes, the creepers in 5 blocks light their fuses | 25% more damage from explosions | Body: bright orange cracks |
| Aftershock | Blaster | `critical` | 2 seconds after its blast, a second blast half as large goes off at the same place | 4 less maximum health | Feet: cracked, ash-gray feet |
| Concussion | Blaster | `projectile` | Its blast gives Nausea and Slowness for 5 seconds | Its blast is 10% smaller | Face: wide eyes with rings |

### Rusher genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sprinter | Rusher | `projectile` | 25% more speed | Its blast is 15% smaller | Legs: pale stripes down the legs |
| Short Fuse | Rusher, sub Blaster | `fuse` | Its fuse burns 30% shorter | Its blast is 15% smaller | Head: a fuse that smokes on top of the head. This changes the model |
| Leaper | Rusher | `fall` | It leaps at a player 3 to 6 blocks away | 10% less maximum health | Legs: bent, springy back legs. This changes the model |
| Dodger | Rusher | `spotted` | It steps to the side when a player aims at it from far away | Its blast is 10% smaller | Feet: wide, flat feet |
| Tracker | Rusher | `alone` | 50% more follow range | 10% less maximum health | Face: a wide, open mouth |
| Wallcrawler | Rusher | `melee` | It climbs walls | 10% less maximum health | Feet: hooked claws. This changes the model |

### Skulker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Silent Fuse | Skulker | `melee` | Its fuse makes no sound | 10% less speed | Face: a mouth that is sewn shut |
| Leafskin | Skulker | `spotted` | It is almost invisible while it stands in leaves or tall grass | 10% less maximum health | Body: a pattern of leaves on the skin |
| Gloom | Skulker | `night` | It is almost invisible at a light level of 7 or less | Its blast is 15% smaller | Body: dark gray patches |
| Veiled | Skulker, sub Fumer | `projectile` | Players farther than 12 blocks do not see it | 10% less speed | Head: a faded, pale face |
| Patient | Skulker | `alone` | It waits without a move until a player comes in 4 blocks, then it lights its fuse | Its blast is 10% smaller | Feet: roots that grow around the feet. This changes the model |
| Surprise | Skulker | `critical` | Its blast does 30% more damage to a player that did not see it | 10% less maximum health | Face: a wider grin |

### Fumer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Toxic Blast | Fumer | `melee` | Its blast leaves a cloud of Poison for 5 seconds | Its blast is 10% smaller | Body: green-yellow blisters |
| Smoke Bomb | Fumer | `projectile` | Its blast leaves a cloud of Blindness for 4 seconds | 10% less maximum health | Head: gray smoke from the eyes |
| Stink | Fumer | `alone` | Players in 4 blocks get Nausea for 3 seconds, each 10 seconds | Players hear it from two times farther | Body: dark green stains |
| Wither Gas | Fumer, sub Thunderhead | `critical` | Its blast leaves a cloud of Wither for 3 seconds | 4 less maximum health | Face: black drool from the mouth |
| Gas Leak | Fumer | `fire` | When it dies before its blast, it leaves a cloud of Poison for 4 seconds | 25% more fire damage | Back: holes that leak gas |
| Spore Blast | Fumer | `crowd` | Its blast leaves a cloud of Slowness for 6 seconds | 10% less speed | Head: small brown mushrooms. This changes the model |

### Thunderhead genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Lightning Rod | Thunderhead | `night` | It becomes charged in a thunderstorm | Water and rain do 1 damage each 3 seconds | Head: a copper spike. This changes the model |
| Static | Thunderhead | `melee` | A player that hits it in melee takes 2 damage | 10% less speed | Body: blue sparks on the skin |
| Overcharge | Thunderhead, sub Blaster | `explosion` | While it is charged, its blast is 25% larger | Its fuse burns 15% longer | Body: bright blue veins |
| Conductor | Thunderhead | `projectile` | When it explodes, lightning strikes the place of the blast | 4 less maximum health | Head: forked blue marks |
| Grounded | Thunderhead | `fire` | No damage from lightning, and no fire from lightning | 10% less maximum health | Legs: copper bands |
| Thunderstruck | Thunderhead | `in_water` | 30% more speed in a thunderstorm | 10% less speed out of a thunderstorm | Feet: a blue glow |

## Gene details

| Gene | Rules |
|---|---|
| All | "Its blast is larger" multiplies the radius of the blast. It also multiplies the blast of a charged creeper. "Its blast does more damage" changes only the damage, not the radius |
| All | "Its fuse burns longer" multiplies the 1.5 seconds of the vanilla fuse |
| Fumers, Toxic Blast, Smoke Bomb, Wither Gas, Spore Blast | The cloud has the radius of the blast and lasts the given time. It is added to the vanilla cloud that a creeper with status effects leaves |
| Thunderheads | "Charged" is the vanilla charged creeper. A creeper that spawns charged has the blue aura |
| Thunderheads, Lightning Rod | Water and rain damage it as they damage an enderman, but at the given rate |
| Skulkers, Veiled | It works as the bogged Swamp Shroud |
| Shrapnel | The blast still does its normal damage to a player that blocks with a shield. A shield takes no damage from it |
| Chain Reaction | It lights only creepers that are not already lit. A lit creeper does not light more creepers |
| Aftershock | The second blast does not light more aftershocks |
| Dodger | It works as the zombie sidestep, with the aim check |
| Leaper | It works as the zombie Lunger. A leap does not light its fuse |
| Wallcrawler | It works as the husk Scuttler. It cannot climb glass |
| Leafskin | It works as the Mosshides of the bogged. "Leaves or tall grass" are the blocks in a new tag, `#originofspawns:leaf_cover`: leaves, short and tall grass, and ferns |
| Gloom | It is drawn translucent, like the zombie Shade |
| Patient | It works as the zombie Lurker |
| Surprise | "Did not see it" means that the creeper was behind the player, or that the player did not look at it in the 3 seconds before the blast |
| Gas Leak | It works as the parched Curse Cloud, but with Poison |
| Conductor | The lightning does not charge other creepers |

## Equipment

- A creeper has no weapon and no armor in vanilla. It does not get them from
  its genes.

## Conversion

- Lightning makes a creeper a charged creeper. It is the same species, and
  it keeps its genes.
- The creeper does not convert to a different species, and no species
  converts to the creeper.

## Babies and jockeys

- Creepers have no babies and no jockeys in vanilla. They do not get a gene
  for them.

## Body

- The creeper has a head, a body, and four legs. Its texture is 64 x 32.
- It needs its own region map, `textures/region/creeper.png`, with regions
  for the head, the face, the body, the back, the legs, and the feet.
- The charged aura draws over the gene marks.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Cannonballs | Blaster | Rusher | Big blasts that run at you |
| Landmines | Blaster | Skulker | Big blasts that wait in cover |
| Dirty Bombs | Blaster | Fumer | Big blasts full of gas |
| Thunderclaps | Blaster | Thunderhead | Big blasts with the charge of the storm |
| Comets | Rusher | Blaster | Fast and still strong |
| Pouncers | Rusher | Skulker | They wait, then run |
| Gas Runners | Rusher | Fumer | Fast clouds of gas |
| Sparks | Rusher | Thunderhead | Fast in the storm |
| Traps | Skulker | Blaster | Hidden and strong |
| Jumpscares | Skulker | Rusher | Hidden, then fast |
| Silent Fogs | Skulker | Fumer | Hidden clouds of gas |
| Dark Clouds | Skulker | Thunderhead | Hidden in the storm |
| Gas Bombs | Fumer | Blaster | Large clouds of gas |
| Stink Runners | Fumer | Rusher | Gas that runs at you |
| Creeping Mists | Fumer | Skulker | Gas that waits |
| Acid Rains | Fumer | Thunderhead | Gas in the storm |
| Thunderbolts | Thunderhead | Blaster | Strong charged blasts |
| Ball Lightning | Thunderhead | Rusher | Fast charged blasts |
| Heat Lightning | Thunderhead | Skulker | Hidden charge |
| Ozone Clouds | Thunderhead | Fumer | Charged gas |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Blasters".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Hidden Charges | Skulker and Blaster | Strong blasts that wait in cover |
| Silent Gas | Skulker and Fumer | Gas that you do not see |
| Shadow Runners | Skulker and Rusher | Fast and hidden |
| Night Storms | Skulker and Thunderhead | Hidden in the dark storm |
| Gas Mines | Blaster and Fumer | Strong blasts of gas |
| Wrecking Balls | Blaster and Rusher | Strong and fast |
| Supercells | Blaster and Thunderhead | The strongest charged blasts |
| Smoke Trails | Fumer and Rusher | Fast clouds of gas |
| Toxic Storms | Fumer and Thunderhead | Gas and lightning |
| Lightning Strikes | Rusher and Thunderhead | Fast charge |

## Neutral

A neutral creeper has no genes, or no family points.
It is a plain creeper, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the creeper, which is not a humanoid | All visuals |
| A region map for the creeper | All visuals |
| A change of the blast radius and the blast damage | Blasters, Rushers, Fumers, Big Bang, Sprinter, Short Fuse, Gloom, Patient, Concussion, Dodger, Toxic Blast, Overcharge |
| A change of the fuse time | Blasters, Big Bang, Short Fuse, Overcharge |
| A cloud of a status effect from the blast | Fumers, Toxic Blast, Smoke Bomb, Wither Gas, Spore Blast |
| Status effects on the players that the blast hits | Concussion |
| A blast that a shield does not block | Shrapnel |
| More blast damage to a tag of entities | Golem Breaker |
| A blast that lights the fuses of creepers near it | Chain Reaction |
| A second blast after the first | Aftershock |
| A fuse with no sound | Silent Fuse |
| A chance to spawn charged, and a charge in a thunderstorm | Thunderheads, Lightning Rod |
| A state for a charged creeper | Overcharge |
| Lightning at the place of the blast | Conductor |
| No damage and no fire from lightning | Grounded |
| A state for a player that did not see the mob | Surprise |
| The block tag `#originofspawns:leaf_cover` | Leafskin |
| The pressure `fuse` | The deaths of creepers while their fuses burn |
| `player_death` for a blast that kills a player, after the creeper is gone | The selection of the creepers whose blasts kill |
