# Magma Cube

This document defines the families and the genes of the magma cube.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The magma cube is the cube of the Nether. It spawns at size 1, 2, or 4, as
the slime does, and it has the same health. It has 3 armor for each size. It
does 2 more damage than a slime of the same size, and a magma cube of size 1
does damage. It hops 4 times less often than a slime, but higher. Fire and
lava do not hurt it. When it dies, it splits as a slime does.
Its genes fit the fire, the crust, the burst on death, the lava, and the
high jump. It shares no genes and no families with the other species.

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

### Infernos

Infernos burn what they touch. Water puts out their fire.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits set the target on fire for 4 seconds | Water and rain do 1 damage each 3 seconds |
| Tier 2 | Its hits set the target on fire for 3 seconds | Water and rain do 1 damage each 2 seconds |
| Tier 1 | Its hits set the target on fire for 2 seconds | Water and rain do 1 damage each second |

### Ironclads

Ironclads grow a thick crust. The weight makes them hop less often.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor | It hops 10% less often |
| Tier 2 | 4 more armor | It hops 15% less often |
| Tier 1 | 2 more armor | It hops 20% less often |

### Erupters

Erupters burst when they die. Their crust is thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When it dies, it bursts with 4 damage in 3 blocks, and sets the players there on fire | 10% less maximum health |
| Tier 2 | When it dies, it bursts with 3 damage in 2.5 blocks, and sets the players there on fire | 15% less maximum health |
| Tier 1 | When it dies, it bursts with 2 damage in 2 blocks, and sets the players there on fire | 20% less maximum health |

### Lavaborn

Lavaborn heal in lava. Out of lava, they are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It heals 1 health each second in lava | 10% less speed out of lava |
| Tier 2 | It heals 1 health each 2 seconds in lava | 15% less speed out of lava |
| Tier 1 | It heals 1 health each 3 seconds in lava | 20% less speed out of lava |

### Springers

Springers jump high and far. They land with less force.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It hops 30% farther and higher | 10% less damage |
| Tier 2 | It hops 20% farther and higher | 15% less damage |
| Tier 1 | It hops 15% farther and higher | 20% less damage |

## Pressures

The magma cube uses `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `wolf`, and `shield`. Fire does not
hurt it, so it does not use `fire`. It does not use `sunlight`, `night`, or
`day`, because it lives in the Nether. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Inferno genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Flame Touch | Inferno | `melee` | Its hits set the target on fire for 5 seconds | Water and rain do 1 damage each 2 seconds | Body: bright orange cracks |
| Ember Trail | Inferno | `unreached` | It leaves fire behind it now and then | 10% less maximum health | Body: glowing embers at the bottom. This changes the model |
| Heat Wave | Inferno | `crowd` | Each 5 seconds, it sets the players in 2 blocks on fire for 2 seconds | 10% less maximum health | Core: a white-hot core. Body: white-hot lines between the layers |
| Spit Fire | Inferno | `projectile` | Each 10 seconds, it shoots a small fireball at a target 6 to 16 blocks away | 10% less maximum health | Face: a glowing mouth |
| Soul Burn | Inferno | `golem` | Two times the damage to golems, and it sets them on fire for 8 seconds | 10% less speed | Body: blue soul fire cracks |
| Magma Blood | Inferno, sub Erupter | `critical` | When a player hurts it in melee, the player burns for 3 seconds | 10% less maximum health | Core: a core that drips lava. Body: lava drips down the sides |

### Ironclad genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Basalt Skin | Ironclad | `melee` | 4 more armor | 10% less speed | Body: gray basalt plates |
| Obsidian Core | Ironclad | `explosion` | Half the damage from explosions | 10% less speed | Core: a black, purple core. Body: black obsidian flecks with purple glints |
| Iron Crust | Ironclad | `projectile` | 30% less damage from projectiles | It hops 10% less often | Body: dark iron flecks |
| Heavy Cube | Ironclad | `golem` | Full knockback resistance | 10% less speed | Body: a thick, dark bottom layer |
| Crust | Ironclad | `critical` | 6 more armor while its health is under half | 10% less maximum health | Body: a cracked black crust |
| Shield Slam | Ironclad, sub Springer | `shield` | When it lands on a player, it stops the shield of the player for 5 seconds | It hops 10% less often | Face: a hard brow. This changes the model |

### Erupter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Blast Core | Erupter | `crowd` | When it dies, it bursts with 4 damage in 3 blocks, and sets the players there on fire | 10% less maximum health | Core: a pulsing, bright core. Body: bright yellow rings around the layers |
| Lava Pool | Erupter | `alone` | When it dies, it leaves lava for 3 seconds | 10% less maximum health | Body: lava at the bottom |
| Ash Cloud | Erupter | `spotted` | When it dies, it leaves a cloud of Blindness | 10% less maximum health | Body: gray ash on the top |
| Split Burst | Erupter | `melee` | When it splits, the players in 2 blocks burn for 3 seconds | 10% less maximum health | Body: many small cracks |
| Scatter Shot | Erupter | `unreached` | When it splits, its pieces jump 4 blocks toward its killer | 10% less speed | Face: wide eyes |
| Molten Core | Erupter, sub Lavaborn | `explosion` | Explosions heal it, in place of damage, for half the damage | 10% less maximum health | Core: a molten, orange core. Body: orange molten seams. This changes the model |

### Lavaborn genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Lava Swim | Lavaborn | `on_land` | 40% more speed in lava | 10% less speed out of lava | Body: smooth, wet layers |
| Lava Heal | Lavaborn | `alone` | It heals 1 health each 2 seconds in lava | 10% less maximum health | Core: a deep red core. Body: deep red veins on the layers |
| Steam Body | Lavaborn | `in_water` | Water does not hurt it, and 20% more speed in water | 10% less damage | Body: white steam marks. This changes the model |
| Lava Leap | Lavaborn | `unreached` | Out of lava, it leaps at a target 4 to 10 blocks away | 10% less maximum health | Face: a long, open mouth |
| Fire Drink | Lavaborn | `critical` | It heals 1 health each second in fire | 10% less maximum health | Core: a flame inside the core. Face: small flames above the eyes |
| Blaze Heart | Lavaborn, sub Inferno | `spotted` | After it leaves lava, its hits set the target on fire for 8 seconds, for 10 seconds | 10% less speed | Core: a gold, burning heart. Body: gold flecks on the layers |

### Springer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sky Jump | Springer | `fall` | It hops two times as high | 10% less maximum health | Body: bright yellow streaks |
| Far Jump | Springer | `unreached` | It hops 50% farther | 10% less maximum health | Body: long, thin cracks |
| Ground Pound | Springer | `melee` | When it lands, it does 3 damage to the players in 2 blocks | It hops 10% less often | Body: a heavy, black bottom |
| Quick Jump | Springer | `spotted` | It hops 30% more often | 10% less damage | Face: narrow, bright eyes |
| Rebound | Springer | `crowd` | After it hits, it hops 3 blocks back | 10% less damage | Body: a spring-like coil inside, and gray coil bands on the outer layers. This changes the model |
| Iron Pounce | Springer, sub Ironclad | `projectile` | Half the damage from projectiles while it is in the air | 10% less speed | Body: iron bands |

## Gene details

| Gene | Rules |
|---|---|
| All | The pieces of a split have the genes of the cube that split, as for the slime. A gene acts at each size, unless its rules say different |
| Infernos, Flame Touch | The magma cube does not take water damage in vanilla. Water damage stops while it has Fire Resistance from a potion |
| Ironclads | The armor adds to the vanilla 3 armor for each size. It is the same at each size |
| Ironclads, Iron Crust, Shield Slam, Ground Pound | "Hops less often" changes the wait between two hops |
| Erupters, Blast Core | It works as the bombardier Powder Keg, with fire. It breaks no blocks |
| Lava Pool | It obeys the `mobGriefing` gamerule. The lava goes away after 3 seconds and does not flow |
| Ash Cloud | It works as the zombie Bloat, with Blindness |
| Lavaborn, Lava Heal, Fire Drink | They work as a regeneration that also acts in combat |
| Steam Body | It stops the water damage of Infernos and Flame Touch |
| Lava Leap | It works as the zombie Lunger |
| Spit Fire | It works as the blaze small fireball. One fireball each time |
| Soul Burn | It works as the wither skeleton Ember Eyes, with all golems |
| Magma Blood | It works as the bogged Rotting Touch, with fire |
| Molten Core | It also stops the damage of a creeper and of its own Blast Core |
| Heat Wave, Magma Blood, Obsidian Core, Blast Core, Molten Core, Lava Heal, Fire Drink, Blaze Heart, Rebound | The layers cover the core, except while the cube hops. So each of these genes also has a mark on the layers or on the face |
| Ember Trail | It obeys the `mobGriefing` gamerule. The fire does not spread |

## Equipment

- The magma cube holds nothing. A magma cube of size 2 or more drops magma
  cream, as in vanilla.

## Conversion

- The magma cube does not convert to a different species, and no species
  converts to the magma cube. A split is not a conversion. The pieces keep
  the genes.

## Babies and jockeys

- Magma cubes have no babies. The small cubes of a split are not babies.
- A magma cube does not ride a mob. It does not get a gene for this.

## Body

- The magma cube has the magma cube model: 8 thin layers and an inner core,
  with eyes. The layers spread apart when it hops. Its texture is 64 x 32.
  The model scales with its size.
- It needs a new region map, `textures/region/magma_cube.png`. The design
  uses the regions body (the layers), core (the inner core), and face (the
  eyes).
- The gene models use the magma cube bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Armored Flames | Inferno | Ironclad | Fire behind a crust |
| Fire Bombs | Inferno | Erupter | Fire that bursts |
| Lava Flames | Inferno | Lavaborn | Fire from the lava |
| Fire Jumpers | Inferno | Springer | Fire from above |
| Hot Crusts | Ironclad | Inferno | A crust that burns |
| Iron Bombs | Ironclad | Erupter | A crust that bursts |
| Lava Shells | Ironclad | Lavaborn | A crust from the lava |
| Rock Jumpers | Ironclad | Springer | A heavy jump |
| Fire Erupters | Erupter | Inferno | A burst of fire |
| Shell Erupters | Erupter | Ironclad | A slow burst |
| Lava Erupters | Erupter | Lavaborn | A burst of lava |
| Sky Erupters | Erupter | Springer | A burst from above |
| Fire Swimmers | Lavaborn | Inferno | Fire in the lava |
| Crust Swimmers | Lavaborn | Ironclad | A crust in the lava |
| Pool Bursters | Lavaborn | Erupter | A burst near the lava |
| Lava Jumpers | Lavaborn | Springer | Jumps out of the lava |
| Flame Springers | Springer | Inferno | Burning jumps |
| Stone Springers | Springer | Ironclad | Heavy jumps |
| Burst Springers | Springer | Erupter | Jumps that burst |
| Lava Springers | Springer | Lavaborn | Jumps from the lava |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Infernos".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Furnaces | Ironclad and Inferno | A burning wall |
| Volcanoes | Erupter and Inferno | A burst of fire |
| Lava Lords | Inferno and Lavaborn | The fire of the lava sea |
| Meteors | Inferno and Springer | Fire from the sky |
| Bomb Shells | Ironclad and Erupter | A slow bomb |
| Lava Fortresses | Ironclad and Lavaborn | A crust in the lava |
| Boulders | Ironclad and Springer | A rock that jumps |
| Lava Geysers | Erupter and Lavaborn | A burst from the lava |
| Fire Rockets | Erupter and Springer | A jump that bursts |
| Lava Springs | Lavaborn and Springer | A jump out of the lava |

## Neutral

A neutral magma cube has no genes, or no family points.
It is a plain magma cube, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet. The
slime work makes most of the cube parts.

| Need | Used by |
|---|---|
| A magma cube region map, and the gene textures and gene models on the magma cube model, at each size | All visuals |
| The slime cube parts on the magma cube: split genes, the hop, landing effects, and split effects | Ironclads, Springers, Shield Slam, Ground Pound, Split Burst, Scatter Shot, Sky Jump, Far Jump, Quick Jump |
| A fire trail | Ember Trail |
| A fire aura on players | Heat Wave |
| A small fireball shot | Spit Fire |
| Fire on an attacker in melee | Magma Blood |
| A death burst with fire | Erupters, Blast Core |
| Lava for a short time at its death | Lava Pool |
| Explosions that heal it | Molten Core |
| A regeneration in lava and in fire, also in combat | Lavaborn, Lava Heal, Fire Drink |
| A fire time on hits after it leaves lava | Blaze Heart |
| A hop back after a hit | Rebound |
| Less projectile damage in the air | Iron Pounce |
| A check that the reused effects work on a magma cube: ignite target, water damage, damage to, attributes, attribute when, death cloud, speed when, leap, and the damage taken effects, at each size | Many genes |
