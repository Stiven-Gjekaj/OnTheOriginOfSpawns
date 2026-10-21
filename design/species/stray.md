# Stray

This document defines the families and the genes of the stray.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The stray lives in snowy and frozen biomes. It shoots arrows that give
Slowness, the sun burns it, and powder snow does not freeze it. Its genes fit
ice, snowstorms, travel over snow, frostbite, and the cold dead that wander
in the snow.
It shares no genes and no families with the skeleton, the bogged, or the
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

### Glaciers

Glaciers grow a shell of ice. Fire melts it.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor | 25% more fire damage |
| Tier 2 | 4 more armor | 50% more fire damage |
| Tier 1 | 2 more armor | 75% more fire damage |

### Blizzards

Blizzards are strong in a snowstorm. Under a clear sky, they are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more arrow damage and 15% more speed while it snows | 10% less arrow damage when it does not snow |
| Tier 2 | 20% more arrow damage while it snows | 20% less arrow damage when it does not snow |
| Tier 1 | 10% more arrow damage while it snows | 25% less arrow damage when it does not snow |

### Drifters

Drifters wander over the snow. Away from snow and ice, they are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed on snow and ice | 10% less speed on other blocks |
| Tier 2 | 20% more speed on snow and ice | 20% less speed on other blocks |
| Tier 1 | 10% more speed on snow and ice | 25% less speed on other blocks |

### Frostbiters

Frostbiters shoot arrows that freeze. Their arrows are brittle.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each arrow hit has a 60% chance to freeze the target for 3 seconds | 10% less arrow damage |
| Tier 2 | Each arrow hit has a 40% chance to freeze the target for 3 seconds | 20% less arrow damage |
| Tier 1 | Each arrow hit has a 25% chance to freeze the target for 3 seconds | 25% less arrow damage |

### Wailers

Wailers are the pale, lost dead of the snow. Their bodies are thin and weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It is almost invisible in a snowstorm and at night, and 10% more speed | 10% less maximum health |
| Tier 2 | It is almost invisible in a snowstorm and at night | 20% less maximum health |
| Tier 1 | It is almost invisible in a snowstorm | 25% less maximum health |

## Pressures

The stray uses the zombie pressures, and `night`, `cold`, `wolf`, and
`shield`. It also uses these pressures.

| Pressure | A stray death counts when | State |
|---|---|---|
| `snow` | The stray dies while it snows on it | New |
| `warm` | The stray dies in a biome that is not cold | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Glacier genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Iceplate | Glacier | `projectile` | 4 more armor | 15% less speed | Shoulders: thick plates of blue ice. This changes the model |
| Permafrost | Glacier | `critical` | It heals 1 health each 2 seconds while it stands on snow or ice | 50% more fire damage | Ribs: frost that fills the gaps |
| Shatterhide | Glacier | `melee` | When it dies, ice shards do 4 damage to players in 3 blocks | 4 less maximum health | Bones: sharp ice spikes on the back. This changes the model |
| Rimeshell | Glacier, sub Frostbiter | `melee` | A player that hits it in melee freezes for 2 seconds | 10% less speed | Arms: a crust of rime on the forearms |
| Glacial Wall | Glacier | `projectile` | 40% less damage from projectiles | It shoots 15% less often | Chest: a slab of ice over the ribs. This changes the model |
| Icebound | Glacier | `explosion` | Full knockback resistance | 20% less speed when it is not on snow or ice | Feet: frozen into blocks of ice. This changes the model |

### Blizzard genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Squall | Blizzard | `unreached` | 50% more bow range while it snows | It shoots 15% less often when it does not snow | Cloth: torn into long strips that blow back |
| Snowdrift | Blizzard, sub Wailer | `spotted` | While it snows, players more than 8 blocks away cannot see it | 10% less maximum health | Body: snow in the folds of the cloth |
| Hailshot | Blizzard | `crowd` | While it snows, each 10 seconds it shoots 3 arrows in a fan | Its arrows do 15% less damage | Back: a quiver packed with ice. This changes the model |
| Gale | Blizzard | `melee` | Its arrows knock the target back two times as far | 10% less speed | Cloth: a hood blown back from the skull. This changes the model |
| Frostwind | Blizzard | `snow` | While it snows, players in 6 blocks start to freeze | Players hear it from two times farther | Mouth: frost breath around the jaw |
| Stormborn | Blizzard | `snow` | It heals 1 health each 3 seconds while it snows | 25% more fire damage | Skull: a cap of packed snow |

### Drifter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Snowshoe | Drifter | `unreached` | It walks on powder snow and does not sink | 10% less speed on other blocks | Feet: wide snowshoes of bone and hide. This changes the model |
| Iceskate | Drifter | `melee` | 40% more speed on ice | 50% more fall damage | Feet: bone blades under the feet. This changes the model |
| Pathfinder | Drifter | `alone` | 50% more follow range, and it chases longer | 2 less armor | Skull: a cloth wrapped over the eyes, with one eye hole |
| Avalanche | Drifter, sub Glacier | `fall` | When its target is 3 or more blocks lower, it jumps down at the target and takes no fall damage | 15% less maximum health | Legs: snow packed on the shins |
| Snowburrow | Drifter | `spotted` | It hides under snow and rises next to a player | It cannot move for 1 second after it rises | Body: snow on the shoulders and the skull |
| Driftline | Drifter | `crowd` | Strays in 8 blocks get 15% more speed | 10% less arrow damage | Back: a pole with a torn banner. This changes the model |

### Frostbiter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Frostbite | Frostbiter | `projectile` | Each arrow hit freezes the target for 4 seconds | 10% less arrow damage | Fingers: black, frostbitten tips |
| Icicle | Frostbiter | `melee` | 30% more arrow damage at a target that freezes | 2 less armor | Skull: icicles that hang from the jaw. This changes the model |
| Numbing Shot | Frostbiter | `golem` | Its arrows give Mining Fatigue for 5 seconds | 10% less speed | Arms: blue veins of frost |
| Coldsnap | Frostbiter | `shield` | The Slowness from its arrows lasts two times longer and is one level stronger | It shoots 15% less often | Bow arm: frost on the hand that holds the bow |
| Rimefall | Frostbiter, sub Drifter | `critical` | When it dies, powder snow fills the block where it died | 4 less maximum health | Pelvis: a lump of packed snow |
| Brittle Shot | Frostbiter | `shield` | Its arrows break the armor of a frozen player two times faster | 10% less arrow damage | Ribs: cracks filled with ice |

### Wailer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Pale | Wailer | `spotted` | At night, it is almost invisible in snowy biomes | The sun burns it two times faster | Bones: a pale white that fades at the edges |
| Wail | Wailer | `melee` | When it is hurt, players in 8 blocks get Weakness for 3 seconds, each 15 seconds | Players hear it from two times farther | Jaw: hangs open, as if it screams |
| Frozen Gaze | Wailer | `projectile` | A player that looks at it gets Slowness for 2 seconds | 10% less arrow damage | Eyes: cold white lights that glow. This changes the model |
| Hollow Cry | Wailer, sub Blizzard | `alone` | When it dies, the strays in 24 blocks target its killer | 10% less speed | Chest: a hole through the ribs |
| Mourner | Wailer | `crowd` | It heals 4 health when another stray dies in 12 blocks | 10% less speed | Cloth: a long, torn veil over the skull. This changes the model |
| Ghostshot | Wailer | `shield` | Its arrows are almost invisible in flight | 10% less arrow damage | Hands: faded and see-through |

## Gene details

| Gene | Rules |
|---|---|
| Blizzards, Squall, Snowdrift, Hailshot, Frostwind, Stormborn | "While it snows" means a storm in a biome where the storm is snow, with open sky above the stray |
| Drifters, Icebound, Permafrost | "Snow and ice" means the blocks in the tags `#minecraft:snow` and `#minecraft:ice`, and powder snow |
| Frostbiters, Rimeshell, Frostbite, Icicle | A freeze is the freeze of powder snow. The target shakes, and it takes freeze damage when the freeze is full. Leather armor stops it |
| Wailers, Pale | It is drawn translucent, like the zombie Shade. Other mobs still see it |
| Shatterhide | The 4 damage hits players only, as the zombie Death Throes does |
| Snowdrift | Players in 8 blocks see it as normal. It is not the invisibility effect |
| Hailshot | The 3 arrows fly at 15 degrees from each other. The normal shot does not change |
| Snowburrow | It works like the husk Sandswimmer, but only through snow blocks |
| Rimefall | It places powder snow only in air, and not where a player stands |
| Brittle Shot | It doubles the armor damage of the arrow hit |
| Frozen Gaze | It works as the look check of the enderman. It needs a clear line of sight, each 2 seconds |
| Ghostshot | The client draws the arrow at about 20% opacity. The arrow still has its sound |

## Equipment

- A stray keeps its bow. The bow is its natural weapon.
- Its arrows give Slowness, as in vanilla. Coldsnap makes this effect
  stronger.
- Mobs with genes do not spawn with random armor. A stray with genes has no
  armor and no enchanted bow from its spawn.

## Conversion

- A skeleton that freezes in powder snow converts to a stray. The stray gets
  new genes from the stray pool. It keeps none of its skeleton genes.
- A stray does not convert to a different species.

## Babies

- Strays have no babies in vanilla. They do not get a gene for babies.

## Body

- The stray has the body of the skeleton, and an outer layer of torn cloth.
- It uses the skeleton region map.
- The gene marks go under the outer layer, as on the drowned. Marks on the
  cloth go on the outer layer.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Icestorms | Glacier | Blizzard | Shells of ice that grow in the storm |
| Floes | Glacier | Drifter | Slow ice that moves fast on snow |
| Rime Guards | Glacier | Frostbiter | Ice walls that freeze all that they touch |
| Frost Tombs | Glacier | Wailer | Dead frozen in ice, half seen |
| Whiteouts | Blizzard | Glacier | Storms that harden the dead |
| Snow Raiders | Blizzard | Drifter | Raiders that come with the snow |
| Sleet Bows | Blizzard | Frostbiter | Freezing arrows in the storm |
| Howlers | Blizzard | Wailer | Cries in the snowstorm |
| Ice Walkers | Drifter | Glacier | Heavy travelers of the ice |
| Storm Chasers | Drifter | Blizzard | Wanderers that follow the storm |
| Trappers | Drifter | Frostbiter | Hunters that leave the snow as a trap |
| Lost Wanderers | Drifter | Wailer | Pale shapes on a long road |
| Hoarfrosts | Frostbiter | Glacier | Ice and frost in one body |
| Hailers | Frostbiter | Blizzard | Hail of freezing arrows |
| Frostrunners | Frostbiter | Drifter | Fast archers that freeze from far away |
| Chillbanes | Frostbiter | Wailer | Unseen cold that freezes the player |
| Ice Wraiths | Wailer | Glacier | Ghosts in shells of ice |
| Banshees | Wailer | Blizzard | Screams in the white storm |
| Snow Ghosts | Wailer | Drifter | Ghosts that drift over the snow |
| Cold Hands | Wailer | Frostbiter | The touch of the frozen dead |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Glaciers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Icefalls | Glacier and Blizzard | Ice that the storm makes and moves |
| Glacier Treks | Glacier and Drifter | Heavy ice that wanders far |
| Deep Freeze | Glacier and Frostbiter | Ice that freezes all that comes near |
| Frozen Graves | Glacier and Wailer | The dead under the ice |
| Snow Hunts | Blizzard and Drifter | Hunts that ride the storm |
| Hailstorms | Blizzard and Frostbiter | Storms of freezing arrows |
| White Wails | Blizzard and Wailer | Cries that come out of the snow |
| Frost Trails | Drifter and Frostbiter | Long roads of frozen victims |
| Lost Souls | Drifter and Wailer | The dead that wander and never stop |
| Winter Shades | Frostbiter and Wailer | Unseen cold that kills slowly |

## Neutral

A neutral stray has no genes, or no family points.
It is a plain stray, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A state for snow on the mob in a storm | Blizzards, Squall, Snowdrift, Hailshot, Frostwind, Stormborn |
| A state for snow and ice under the mob | Drifters, Icebound, Permafrost, Iceskate |
| A freeze on hit, with the freeze of powder snow | Frostbiters, Rimeshell, Frostbite, Frostwind |
| A state for a target that freezes | Icicle, Brittle Shot |
| The pressures `snow` and `warm` | Frostwind, Stormborn |
| A walk on powder snow | Snowshoe |
| A shot of 3 arrows in a fan | Hailshot |
| Arrow knockback | Gale |
| A stronger and longer Slowness on the arrows of the stray | Coldsnap |
| Armor damage from arrows | Brittle Shot |
| Powder snow where the mob dies | Rimefall |
| A burrow through snow blocks | Snowburrow |
| A state for a player that looks at the mob | Frozen Gaze |
| A call to the killer on death, and a heal when an ally dies | Hollow Cry, Mourner |
| Translucent arrows, and translucent mobs in a storm | Ghostshot, Wailers, Snowdrift |
| A hidden mob for players far away | Snowdrift |
| The outer cloth layer in the gene textures | All visuals |
