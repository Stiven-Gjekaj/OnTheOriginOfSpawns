# Phantom

This document defines the families and the genes of the phantom.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The phantom is an undead flyer of the night sky. It has 20 health. Its bite
does 6 damage, and more at a larger size. It spawns in groups at night near
a player that has not slept for three days or more. It circles high in the
sky and swoops down to bite. The sun burns it. Cats scare it away.
Its genes fit the swoop, the glide, the night, the shriek that keeps the
player awake, and the flock. It shares no genes and no families with the
other species.

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

### Swoopers

Swoopers dive hard. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage | 10% less maximum health |
| Tier 2 | 20% more damage | 15% less maximum health |
| Tier 1 | 15% more damage | 20% less maximum health |

### Gliders

Gliders fly fast. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more flying speed | 10% more damage taken |
| Tier 2 | 20% more flying speed | 15% more damage taken |
| Tier 1 | 15% more flying speed | 20% more damage taken |

### Nightwings

Nightwings resist the sun. Their bite is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | The sun burns it only one time each 4 seconds, not each second | 10% less damage |
| Tier 2 | The sun burns it only one time each 3 seconds, not each second | 15% less damage |
| Tier 1 | The sun burns it only one time each 2 seconds, not each second | 20% less damage |

### Shriekers

Shriekers weaken their prey. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its bites give Weakness for 4 seconds | 10% less maximum health |
| Tier 2 | Its bites give Weakness for 3 seconds | 15% less maximum health |
| Tier 1 | Its bites give Weakness for 2 seconds | 20% less maximum health |

### Swarmwings

Swarmwings come in larger groups. Each phantom is small.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When it spawns, 2 more phantoms spawn with it | 10% less maximum health |
| Tier 2 | When it spawns, 1 more phantom spawns with it | 15% less maximum health |
| Tier 1 | When it spawns, 1 more phantom spawns with it | 20% less maximum health |

## Pressures

The phantom uses `sunlight`, `fire`, `projectile`, `melee`, `explosion`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `smite`, and `shield`. It flies, so it does not use `fall`,
`in_water`, or `on_land`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Swooper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Talon Dive | Swooper | `melee` | 20% more damage | 10% less maximum health | Head: long, hooked fangs. This changes the model |
| Rending Bite | Swooper | `critical` | 30% more damage to a player under half health | 10% less damage to a player over half health | Head: a red mouth |
| Wing Slam | Swooper | `shield` | Its bites stop a shield for 4 seconds | 10% less flying speed | Wings: heavy, dark wing edges |
| Golem Diver | Swooper | `golem` | Two times the damage to golems | 10% less flying speed | Body: gray scars on the back |
| Steep Dive | Swooper | `explosion` | Its bites knock the target back two times as far | 10% less maximum health | Tail: a long, sharp tail. This changes the model |
| First Dive | Swooper, sub Glider | `spotted` | Its first bite on a player does 30% more damage | 10% less maximum health | Head: narrow, bright eyes |

### Glider genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Swift Wing | Glider | `unreached` | 20% more flying speed | 10% less maximum health | Wings: long, thin wings. This changes the model |
| Wide Circle | Glider | `alone` | Two times the follow range | 10% less flying speed | Body: a pale stripe down the back |
| Feint | Glider | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Wings: light, torn wing tips |
| Silent Wing | Glider | `spotted` | It makes no sound | 10% less damage | Wings: soft, gray wings |
| Wind Rider | Glider | `explosion` | Half the damage from explosions | 10% less flying speed | Tail: a split tail. This changes the model |
| Night Glide | Glider, sub Nightwing | `crowd` | It is near invisible in the dark | 10% less maximum health | Body: a dark, blue body |

### Nightwing genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sun Shade | Nightwing | `sunlight` | The sun burns it only one time each 4 seconds, not each second | 10% less damage | Wings: dark, thick wings |
| Ember Hide | Nightwing | `fire` | Half the fire damage | 10% less flying speed | Body: a scorched, black hide |
| Pale Bone | Nightwing | `smite` | Smite does half the extra damage | 10% less maximum health | Body: white ribs on the body |
| Dusk Mend | Nightwing | `alone` | It heals 1 health each 3 seconds at night | 10% less damage | Head: green, glowing eyes |
| Moon Eye | Nightwing | `player_death` | It sees invisible players in 16 blocks | 10% less flying speed | Head: a large, pale eye |
| Night Terror | Nightwing, sub Shrieker | `unreached` | Each 15 seconds, the players in 8 blocks get Darkness for 3 seconds | 10% less maximum health | Head: black, empty eyes |

### Shrieker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Shriek | Shrieker | `spotted` | Its bites give Weakness for 3 seconds | 10% less maximum health | Head: a wide, open mouth |
| Blinding Cry | Shrieker | `crowd` | Its bites give Blindness for 2 seconds | 10% less damage | Head: white rings round the eyes |
| Gnaw | Shrieker | `melee` | Its bites give Hunger for 5 seconds | 10% less damage | Head: long, thin teeth |
| Sleepless Bite | Shrieker | `player_death` | Its bites give Slowness II for 2 seconds | 10% less maximum health | Body: purple marks on the back |
| Echo Cry | Shrieker | `alone` | When something hurts it, it calls the phantoms in 32 blocks | Players hear it from two times farther | Wings: ripple lines on the wings |
| Flock Cry | Shrieker, sub Swarmwing | `critical` | When it sees a player, it calls the phantoms in 32 blocks | Players hear it from two times farther | Head: a tall crest. This changes the model |

### Swarmwing genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Big Flock | Swarmwing | `crowd` | When it spawns, 1 more phantom spawns with it | 10% less maximum health | Body: small spots on the back |
| Pack Wing | Swarmwing | `melee` | 20% more damage with 2 or more phantoms near it | 20% less damage alone | Wings: blue bands on the wings |
| Flock Shield | Swarmwing | `projectile` | 20% less damage taken with 2 or more phantoms near it | 20% more damage taken alone | Body: thick, dark scales |
| Last Flight | Swarmwing | `critical` | When it dies, the phantoms in 16 blocks get Strength for 10 seconds | 10% less maximum health | Body: a red mark on the chest |
| Flock Rally | Swarmwing | `spotted` | 5% more damage for each phantom in 8 blocks, up to 20% | 10% less maximum health | Wings: white wing tips |
| Swarm Dive | Swarmwing, sub Swooper | `unreached` | When it swoops, the phantoms in 16 blocks swoop at the same target | 10% less maximum health | Tail: a forked, red tail. This changes the model |

## Gene details

| Gene | Rules |
|---|---|
| Gliders, Swift Wing | "Flying speed" is the speed of the circle and the swoop |
| Nightwings, Sun Shade | The phantom still burns in the sun, but less often. A helmet does not apply |
| Swarmwings, Big Flock | The extra phantoms have genes from the pool, as new mobs |
| Rending Bite | It works as the vindicator Decapitator |
| Wing Slam | It works as the slime Shield Melt |
| Golem Diver | "Golems" are the iron golem and the snow golem |
| Steep Dive | It works as the vindicator Chop |
| First Dive | It works as the spider Trapdoors |
| Feint | It works as the pillager Evasion, in the air |
| Silent Wing | It works as the creeper Silent Fuse, for all its sounds |
| Night Glide | It works as the creeper Gloom |
| Pale Bone | It works as the zoglin Holy Bane |
| Dusk Mend | It works as a regeneration at night, also in combat |
| Moon Eye | It works as the pillager Pathseeker |
| Night Terror | It works as the vindicator Howl, with Darkness |
| Echo Cry | It works as the vindicator Alarm Bell |
| Flock Cry | It works as the pillager War Horn |
| Pack Wing, Flock Shield | They work as the pillager Formation |
| Last Flight | It works as the pillager Grudge |
| Flock Rally | It works as the pillager Rally Cry |
| Swarm Dive | Each phantom swoops one time for each call, after its own wait |

## Equipment

- The phantom holds nothing. It drops its vanilla items.

## Conversion

- The phantom does not convert to a different species, and no species
  converts to the phantom.

## Babies and jockeys

- Phantoms have no babies. The size of a phantom is not a baby.
- A phantom does not ride a mob. It does not get a gene for this.

## Body

- The phantom has the phantom model: a body, a head, two wings in two parts,
  and a tail in two parts. Its eyes glow. Its texture is 64 x 64. The model
  scales with its size.
- It needs a new region map, `textures/region/phantom.png`. The design uses
  the regions head, body, wings, and tail.
- The gene models use the phantom bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Fast Divers | Swooper | Glider | A fast, hard dive |
| Moon Divers | Swooper | Nightwing | A hard dive at dawn |
| Crying Divers | Swooper | Shrieker | A dive that weakens |
| Flock Divers | Swooper | Swarmwing | Hard dives in a group |
| Diving Gliders | Glider | Swooper | Fast wings that bite hard |
| Moon Gliders | Glider | Nightwing | Fast wings at dawn |
| Crying Gliders | Glider | Shrieker | Fast wings that weaken |
| Flock Gliders | Glider | Swarmwing | Fast wings in a group |
| Diving Shades | Nightwing | Swooper | A sun shade that bites hard |
| Swift Shades | Nightwing | Glider | A fast sun shade |
| Crying Shades | Nightwing | Shrieker | A sun shade that weakens |
| Flock Shades | Nightwing | Swarmwing | Sun shades in a group |
| Diving Criers | Shrieker | Swooper | A cry and a hard dive |
| Swift Criers | Shrieker | Glider | A cry on fast wings |
| Moon Criers | Shrieker | Nightwing | A cry at dawn |
| Flock Criers | Shrieker | Swarmwing | A cry in a group |
| Diving Swarms | Swarmwing | Swooper | A group that dives hard |
| Swift Swarms | Swarmwing | Glider | A fast group |
| Moon Swarms | Swarmwing | Nightwing | A group at dawn |
| Crying Swarms | Swarmwing | Shrieker | A group that weakens |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Swoopers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Sky Reapers | Glider and Swooper | Fast, hard dives |
| Dawn Raiders | Nightwing and Swooper | Hard dives in the sun |
| Night Hags | Shrieker and Swooper | Dives that weaken |
| Death Flocks | Swarmwing and Swooper | Groups that dive hard |
| Dusk Riders | Glider and Nightwing | Fast wings in the sun |
| Wind Wailers | Glider and Shrieker | Fast wings that weaken |
| Storm Flocks | Swarmwing and Glider | Fast groups |
| Grave Criers | Nightwing and Shrieker | Cries in the sun |
| Dawn Flocks | Swarmwing and Nightwing | Groups in the sun |
| Nightmares | Swarmwing and Shrieker | Groups that weaken |

## Neutral

A neutral phantom has no genes, or no family points.
It is a plain phantom, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A phantom region map, and the gene textures and gene models on the phantom model, at each size, with the glowing eyes | All visuals |
| The flying speed of the circle and the swoop | Gliders, Swift Wing, and the downsides |
| A slower sunburn | Nightwings, Sun Shade |
| A brood for phantoms, with genes from the pool | Swarmwings, Big Flock |
| A sidestep in the air | Feint |
| No sounds | Silent Wing |
| A regeneration at night | Dusk Mend |
| A swoop call to the phantoms near it | Swarm Dive |
| A check that the reused effects work on a phantom: the hit effects on its bite, shield break, damage to, knockback, first strike, translucent, smite resist, see invisible, aura, calls, damage when grouped and alone, death effect, rally, and the damage taken effects | Many genes |
