# Blaze

This document defines the families and the genes of the blaze.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The blaze is a burning flyer of the Nether fortress. It has 20 health. It
hovers, and it shoots bursts of three small fireballs. Each fireball does 5
damage and sets the target on fire for 5 seconds. Its touch does 6 damage.
Fire does not hurt it, but water, rain, and snowballs hurt it. A spawner in
the fortress makes it. It drops blaze rods. Its genes fit its fireballs, the
size of its bursts, its fire, its hover, and its fight with water. It shares
no genes and no families with the other species.

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

### Searers

Searers shoot hot fireballs. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its fireballs do 30% more damage | 10% less maximum health |
| Tier 2 | Its fireballs do 20% more damage | 15% less maximum health |
| Tier 1 | Its fireballs do 15% more damage | 20% less maximum health |

### Volleyers

Volleyers shoot more fireballs in a burst. Each fireball is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 2 more fireballs in each burst | Its fireballs do 10% less damage |
| Tier 2 | 1 more fireball in each burst | Its fireballs do 15% less damage |
| Tier 1 | 1 more fireball in each burst | Its fireballs do 20% less damage |

### Smolderers

Smolderers burn for a long time. They are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its fire burns the target for 5 more seconds | 10% less flying speed |
| Tier 2 | Its fire burns the target for 4 more seconds | 15% less flying speed |
| Tier 1 | Its fire burns the target for 3 more seconds | 20% less flying speed |

### Hoverers

Hoverers fly fast and high. Their touch is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more flying speed | 10% less melee damage |
| Tier 2 | 20% more flying speed | 15% less melee damage |
| Tier 1 | 15% more flying speed | 20% less melee damage |

### Steamhides

Steamhides resist water. Their fire is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 70% less damage from water, rain, and snowballs | Its fire burns the target for 1 second less |
| Tier 2 | 50% less damage from water, rain, and snowballs | Its fire burns the target for 2 seconds less |
| Tier 1 | 40% less damage from water, rain, and snowballs | Its fire burns the target for 3 seconds less |

## Pressures

The blaze uses `projectile`, `melee`, `explosion`, `unreached`, `spotted`,
`alone`, `crowd`, `critical`, `golem`, `player_death`, `shield`, and
`in_water`. Fire does not hurt it, so it does not use `fire`. It lives in the
Nether, so it does not use `sunlight`, `night`, or `day`. It needs no new
pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Searer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| White Heat | Searer | `unreached` | Its fireballs do 40% more damage | 10% less maximum health | Head: white, hot eyes |
| Blast Ember | Searer | `crowd` | Its fireballs make a small blast where they hit, with no block damage | 10% less maximum health | Rods: red, cracked rods |
| Shield Scorch | Searer | `shield` | Its fireballs stop a shield for 3 seconds | 10% less flying speed | Head: a red, angry brow |
| Golem Melt | Searer | `golem` | Its fireballs do two times the damage to golems | 10% less maximum health | Rods: orange, molten tips |
| Sear Touch | Searer | `melee` | Its touch sets the target on fire for 4 seconds | 10% less flying speed | Head: a burning, open mouth |
| Swift Shot | Searer, sub Volleyer | `spotted` | Its fireballs fly 50% faster | 10% less maximum health | Head: narrow, yellow eyes |

### Volleyer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Burst | Volleyer | `unreached` | 2 more fireballs in each burst | 10% less maximum health | Rods: twelve thin rods. This changes the model |
| Quick Burst | Volleyer | `spotted` | 30% less time between two bursts | Its fireballs do 10% less damage | Rods: yellow rings on the rods |
| Fan Shot | Volleyer | `crowd` | It shoots each burst in a fan | 10% less maximum health | Rods: spread, wide rods. This changes the model |
| Seeker Spark | Volleyer | `projectile` | Its fireballs turn a little to the target | It shoots 10% less often | Head: a gold mark on the brow |
| Spark Ring | Volleyer | `alone` | Each 10 seconds, it shoots 8 fireballs in a ring | 10% less maximum health | Rods: a gold band round the middle |
| Soot Cloud | Volleyer, sub Smolderer | `explosion` | Its fireballs leave a cloud of smoke that gives Blindness for 2 seconds | 10% less flying speed | Head: black ash on the face |

### Smolderer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Deep Burn | Smolderer | `critical` | Its fire burns the target for 6 more seconds | 10% less flying speed | Head: dark red eyes |
| Fire Trail | Smolderer | `unreached` | Its fireballs set the blocks where they hit on fire | 10% less maximum health | Rods: black, sooty rods |
| Burn Pain | Smolderer | `player_death` | A target that burns takes 20% more damage | 10% less maximum health | Head: a deep, cruel frown |
| Fire Feed | Smolderer | `alone` | It heals 1 health each second that a target near it burns | 10% less flying speed | Rods: rods that glow at the base |
| Rod Wall | Smolderer | `projectile` | 30% less damage from projectiles | 10% less flying speed | Rods: thick, short rods. This changes the model |
| Heat Haze | Smolderer, sub Hoverer | `spotted` | It is near invisible when it does not shoot | 10% less maximum health | Head: pale, faded eyes |

### Hoverer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rising Heat | Hoverer | `melee` | It keeps 4 more blocks of height above its target | 10% less melee damage | Rods: rods that point up |
| Air Dodge | Hoverer | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Head: light, wide eyes |
| Quick Wings | Hoverer | `unreached` | 20% more flying speed | 10% less maximum health | Rods: thin, long rods. This changes the model |
| Smoke Dash | Hoverer | `critical` | At under 30% health, it flies away fast for 4 seconds | 10% less melee damage | Head: gray smoke round the head |
| Sky Guard | Hoverer | `golem` | It stays out of the reach of golems | 10% less flying speed | Rods: pale, ash gray rods |
| Diving Flame | Hoverer, sub Searer | `alone` | It dives at the target, and its touch does 4 more damage | 10% less maximum health | Head: a sharp, red brow |

### Steamhide genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Steam Shell | Steamhide | `in_water` | No damage from water and rain | 10% less flying speed | Rods: wet, blue gray rods |
| Snow Shrug | Steamhide | `projectile` | No damage from snowballs | 10% less maximum health | Head: blue, cold eyes |
| Boil Off | Steamhide | `in_water` | Water near it turns to steam, in 2 blocks | 10% less maximum health | Rods: white steam rings |
| Hot Core | Steamhide | `explosion` | Half the damage from explosions | 10% less flying speed | Head: a dark core that glows |
| Cinder Heart | Steamhide | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Head: a bright mark on the chest |
| Steam Burst | Steamhide, sub Volleyer | `crowd` | When something hurts it, it pushes the mobs and players in 3 blocks away | 10% less maximum health | Rods: rods with open vents |

## Gene details

| Gene | Rules |
|---|---|
| Searers, White Heat | The damage is the direct damage of the small fireball. The fire time does not change |
| Volleyers, Long Burst | The vanilla blaze shoots 3 fireballs in each burst |
| Smolderers, Deep Burn | The fire time adds to the 5 seconds of the vanilla fireball |
| Steamhides, Steam Shell, Snow Shrug | The vanilla blaze takes damage in water and in rain, and 3 damage from a snowball |
| Blast Ember | The blast has a power of 0.5, and it does no block damage |
| Shield Scorch | It works as the slime Shield Melt |
| Golem Melt | "Golems" are the iron golem and the snow golem |
| Sear Touch | The vanilla blaze touch does not set the target on fire |
| Swift Shot | The speed is the speed of the flight of each fireball |
| Quick Burst | "Time between two bursts" is the rest after each burst |
| Fan Shot | The fireballs go out in a fan of 30 degrees |
| Seeker Spark | Each fireball turns at most 5 degrees each tick, so a player can step aside |
| Spark Ring | The fireballs go out level, and each one does the normal damage |
| Soot Cloud | The cloud stays for 3 seconds and has a radius of 2 blocks |
| Fire Trail | It sets the blocks on fire only if the `mobGriefing` gamerule is on, as in vanilla |
| Burn Pain | The extra damage comes from all sources |
| Fire Feed | "Near" is 8 blocks |
| Heat Haze | It works as the creeper Gloom. It becomes visible for 2 seconds after it shoots |
| Rising Heat | The blaze keeps this much more height when it shoots |
| Air Dodge | It works as the pillager Evasion, in the air |
| Smoke Dash | It flies away from the target with 50% more flying speed |
| Sky Guard | It keeps 4 blocks of height above a golem |
| Diving Flame | It dives one time each 10 seconds |
| Boil Off | It removes water source blocks only if the `mobGriefing` gamerule is on |
| Cinder Heart | It works as the vindicator Death Wish |
| Steam Burst | The push is as strong as a vanilla wind charge |

## Equipment

- The blaze holds nothing. It drops its vanilla items.

## Conversion

- The blaze does not convert to a different species, and no species
  converts to the blaze.

## Babies and jockeys

- The blaze has no babies. It does not get a gene for them.
- A blaze does not ride a mob. It does not get a gene for this.

## Spawners

- A blaze from a spawner gets genes as a natural spawn.

## Body

- The blaze has the blaze model: a head, and twelve rods in three rings
  round its body. Its texture is 64 x 32.
- It needs a new region map, `textures/region/blaze.png`. The design uses
  the regions head and rods.
- The gene models use the blaze bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Volley Searers | Searer | Volleyer | Hot fireballs in long bursts |
| Smoldering Searers | Searer | Smolderer | Hot fireballs and long fire |
| Hovering Searers | Searer | Hoverer | Hot fireballs from fast flight |
| Steaming Searers | Searer | Steamhide | Hot fireballs and a wet guard |
| Searing Volleyers | Volleyer | Searer | Long bursts of hot fireballs |
| Smoldering Volleyers | Volleyer | Smolderer | Long bursts and long fire |
| Hovering Volleyers | Volleyer | Hoverer | Long bursts from fast flight |
| Steaming Volleyers | Volleyer | Steamhide | Long bursts and a wet guard |
| Searing Smolderers | Smolderer | Searer | Long fire and hot fireballs |
| Volley Smolderers | Smolderer | Volleyer | Long fire in long bursts |
| Hovering Smolderers | Smolderer | Hoverer | Long fire from fast flight |
| Steaming Smolderers | Smolderer | Steamhide | Long fire and a wet guard |
| Searing Hoverers | Hoverer | Searer | Fast flight and hot fireballs |
| Volley Hoverers | Hoverer | Volleyer | Fast flight and long bursts |
| Smoldering Hoverers | Hoverer | Smolderer | Fast flight and long fire |
| Steaming Hoverers | Hoverer | Steamhide | Fast flight and a wet guard |
| Searing Steamhides | Steamhide | Searer | A wet guard and hot fireballs |
| Volley Steamhides | Steamhide | Volleyer | A wet guard and long bursts |
| Smoldering Steamhides | Steamhide | Smolderer | A wet guard and long fire |
| Hovering Steamhides | Steamhide | Hoverer | A wet guard and fast flight |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Searers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Inferno Volleys | Searer and Volleyer | Long bursts of hot fireballs |
| Hellfires | Searer and Smolderer | Hot fireballs and long fire |
| Sun Divers | Hoverer and Searer | Hot fireballs from fast flight |
| Steam Searers | Searer and Steamhide | Hot fireballs and a wet guard |
| Ember Storms | Smolderer and Volleyer | Long bursts and long fire |
| Spark Swarms | Hoverer and Volleyer | Long bursts from fast flight |
| Geyser Volleys | Steamhide and Volleyer | Long bursts and a wet guard |
| Smoke Wraiths | Hoverer and Smolderer | Long fire from fast flight |
| Kiln Hearts | Smolderer and Steamhide | Long fire and a wet guard |
| Cloud Kilns | Hoverer and Steamhide | Fast flight and a wet guard |

## Neutral

A neutral blaze has no genes, or no family points.
It is a plain blaze, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A blaze region map, and the gene textures and gene models on the blaze model | All visuals |
| The damage, the speed, and the fire time of its small fireballs | Searers, Smolderers, Steamhides, White Heat, Swift Shot, Deep Burn |
| The number of fireballs in a burst, and the time between bursts | Volleyers, Long Burst, Quick Burst |
| A fan of fireballs, and a ring of fireballs | Fan Shot, Spark Ring |
| A small blast, a smoke cloud, and fire on the blocks where a fireball hits | Blast Ember, Soot Cloud, Fire Trail |
| A shield stop and more golem damage from its fireballs | Shield Scorch, Golem Melt |
| Fire from its touch | Sear Touch |
| Fireballs that turn to the target | Seeker Spark |
| More damage to a target that burns, and heal while a target burns | Burn Pain, Fire Feed |
| The flying speed of the blaze, and more height when it shoots | Hoverers, Quick Wings, Rising Heat, Sky Guard, and the downsides |
| A sidestep in the air, and a dive | Air Dodge, Diving Flame |
| Less damage from water, rain, and snowballs | Steamhides, Steam Shell, Snow Shrug |
| Water near it turns to steam | Boil Off |
| A check that the reused effects work on a blaze: translucent, flee, heal once, push on hurt, and the damage taken effects | Many genes |
