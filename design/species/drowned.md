# Drowned

This document defines the families and the genes of the drowned.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The drowned lives in oceans and rivers. It swims, it can throw tridents, and
the sun burns it on land. Its genes fit currents, the deep, wrecks, coral, and
the songs of the sea. It shares no genes and no families with the zombie.

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

### Riptides

Riptides are fast in the water. They are slow on land.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 60% more speed in water | 5% less speed on land |
| Tier 2 | 40% more speed in water | 10% less speed on land |
| Tier 1 | 20% more speed in water | 20% less speed on land |

### Harpooners

Harpooners fight with tridents. Their bare hands are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 60% chance to spawn with a trident, and it throws 30% more often | 15% less melee damage |
| Tier 2 | 35% chance to spawn with a trident | 25% less melee damage |
| Tier 1 | 15% chance to spawn with a trident | 30% less melee damage |

### Abyssals

Abyssals come from the deep and the dark. Light hurts them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage at light level 6 or less, and it finds invisible players in 8 blocks | 10% more damage taken at light level 12 or more |
| Tier 2 | 20% more damage at light level 6 or less | 20% more damage taken at light level 12 or more |
| Tier 1 | 10% more damage at light level 6 or less | 25% more damage taken at light level 12 or more |

### Barnacled

Barnacled drowned grow shells and coral. They are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor, and full knockback resistance | 10% less speed |
| Tier 2 | 4 more armor, and full knockback resistance | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Sirens

Sirens sing to pull players into the water. Alone, they are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Players in water in 16 blocks move slowly toward it | 10% less damage with no drowned in 8 blocks |
| Tier 2 | Players in water in 10 blocks move slowly toward it | 20% less damage with no drowned in 8 blocks |
| Tier 1 | Players in water in 6 blocks move slowly toward it | 25% less damage with no drowned in 8 blocks |

## Pressures

The drowned uses the zombie pressures. It also uses these new pressures.

| Pressure | A drowned death counts when | State |
|---|---|---|
| `in_water` | The drowned dies in water | New |
| `on_land` | The drowned dies out of water | New |
| `boat` | The killer is in a boat | New |
| `trident` | A trident kills the drowned | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Riptide genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Undertow | Riptide | `boat` | Each hit on a player in water pulls the player 2 blocks down | 2 less armor | Head: thick strands of kelp around the head and down the back. This changes the model |
| Torrent | Riptide | `unreached` | 40% more speed in water | 15% less maximum health | Lower legs: large fins that stick out. This changes the model |
| Surge | Riptide, sub Harpooner | `unreached` | It dashes through water at a target 4 to 8 blocks away | It cannot move for 1 second after a missed dash | Arms: pale streaks, like foam |
| Capsizer | Riptide | `boat` | Its hits break boats and throw the riders into the water | 20% less speed on land | Hands: webbed fingers |
| Breacher | Riptide | `on_land` | It jumps out of the water at players on the shore, docks, or boats | 50% more fall damage | Back: a tall fin, like the fin of a shark. This changes the model |
| Gills | Riptide | `on_land` | It heals 1 health each 3 seconds in water | Out of water in the sun, it takes 1 damage each 4 seconds | Neck: open gills |

### Harpooner genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Tridentborn | Harpooner | `melee` | 25% chance to spawn with a trident | 2 less melee damage | It holds a trident |
| Barbed | Harpooner | `projectile` | Its trident hits give Slowness for 3 seconds | It throws 20% less often | Shoulders: a round cluster of spikes on each shoulder, like a sea urchin. This changes the model |
| Volley | Harpooner | `crowd` | It throws two times as often | 4 less maximum health | Arms: bands of rope |
| Scavenger | Harpooner, sub Riptide | `trident` | It picks up tridents that players throw and keeps them | It drops no trident when it dies | Belt: a bag of junk |
| Spearfisher | Harpooner | `unreached` | 30% more throw range at players in water or in boats | 30% less throw range at players on land | Body: a torn fishing net |
| Stormcaller | Harpooner | `golem` | In a thunderstorm, its trident hits call lightning | It takes 50% more damage from lightning, and it glows in storms | Skin: green copper rust |

### Abyssal genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Lure | Abyssal | `spotted` | A light hangs in front of it. When a player comes within 6 blocks, the light goes out, and its first hit does double damage | Players see the light through water from far away | Head: an angler lure with a glowing tip. This changes the model |
| Pressure Hide | Abyssal | `explosion` | 40% less damage taken 10 or more blocks under sea level | 20% less speed above sea level | Skin: pale and thick |
| Blind Hunter | Abyssal | `critical` | It finds invisible players, and has 50% more follow range in water | It does not see a sneaking player more than 4 blocks away | Eyes: milky white |
| Ink | Abyssal, sub Siren | `melee` | When it is hurt, it sprays ink: the attacker gets Blindness for 3 seconds, each 15 seconds | 4 less maximum health | Mouth and chest: black stains |
| Deepcold | Abyssal | `fire` | Each hit gives Mining Fatigue for 5 seconds | 20% less speed on land | Skin: a frost-blue tint |
| Glowspot | Abyssal | `alone` | Each hit gives the player Glowing for 10 seconds, so other drowned find the player | Its own spots glow, so players see it in the dark | Skin: spots that glow. This changes the model |

### Barnacled genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Barnacle Crust | Barnacled | `projectile` | 4 more armor | 15% less speed | Shoulders and back: barnacles. This changes the model |
| Coral Growth | Barnacled | `melee` | Melee attackers take 1 damage when they hit it | 4 less maximum health | Arms: coral branches. This changes the model |
| Shellback | Barnacled | `critical` | 50% less damage from behind | 15% less attack speed | Back: a shell. This changes the model |
| Anchor | Barnacled | `boat` | Full knockback resistance, and it sinks fast | It swims up 50% slower | Waist: a rusty chain. This changes the model |
| Pearlheart | Barnacled | `player_death` | One time in its life, it heals to full health when it drops under half health | 10% less speed | Chest: a pearl that glows |
| Kelpwrapped | Barnacled, sub Abyssal | `spotted` | It is almost invisible in kelp and seagrass | The sun burns it two times faster | Body: wrapped in kelp |

### Siren genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Siren Song | Siren | `alone` | Players in water in 12 blocks get Slowness, renewed each 2 seconds | Players hear it from two times farther | Mouth: open, as if it sings |
| Drag Under | Siren | `boat` | When it hits a player in water, it holds on, and both sink for 2 seconds | 2 less attack damage | Arms: long, thin fingers |
| Fry | Siren | `projectile` | 20% chance to spawn as a baby drowned | Its babies have 50% less maximum health | The baby model, and head: small fins |
| Shoal | Siren | `crowd` | Its spawn groups have 1 more drowned | Each drowned of the group has 15% less maximum health | Skin: a pattern of scales |
| Chorus | Siren | `critical` | When it sees a player, the drowned in 24 blocks target that player. It can call again after 30 seconds | Players hear it from two times farther | Ear: a shell earring |
| Wreckcaller | Siren, sub Barnacled | `explosion` | When it dies, the drowned in 16 blocks get Resistance for 10 seconds | 4 less maximum health | Waist: a length of ship rope |

## Gene details

| Gene | Rules |
|---|---|
| Undertow | The pull works only on a player who is in water |
| Surge | It dashes only in water, with 5 seconds between dashes |
| Capsizer | It breaks a boat after 3 hits |
| Breacher | It jumps only when a player is in 6 blocks and less than 3 blocks above the water |
| Gills | The damage on land is not fire, so fire resistance does not stop it |
| Tridentborn | This adds to the vanilla chance to spawn with a trident |
| Scavenger | It picks up a trident only when it has no other item in its hand |
| Lure | The light shows again after 20 seconds with no player near |
| Pressure Hide | Sea level is the sea level of the world, 63 in the overworld |
| Kelpwrapped | It is drawn translucent, like the zombie Shade. Other mobs still see it |
| Pearlheart | It shows a burst of bubbles when it heals |
| Fry | Baby drowned spawn only through this gene |
| Drag Under | The player can break free with a hit |

## Baby drowned

- Baby drowned do not spawn in vanilla ways.
- Only the Fry gene makes baby drowned.
- A baby zombie that converts to a drowned stays a baby, and it gets new
  genes from the drowned pool.

## Conversion

- A zombie that converts to a drowned in water gets new genes from the
  drowned pool. It keeps none of its zombie genes.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Spearrunners | Riptide | Harpooner | Fast swimmers that throw from the current |
| Rip Currents | Riptide | Abyssal | Fast hunters that come up from the dark |
| Reefrunners | Riptide | Barnacled | Swift swimmers with a hard shell |
| Wavecallers | Riptide | Siren | Fast singers that pull the player under |
| Whalers | Harpooner | Riptide | Trident throwers that never stay still |
| Gloomspears | Harpooner | Abyssal | Throwers that strike from the deep |
| Ironharpoons | Harpooner | Barnacled | Tough throwers that hold their place |
| Choir of Spears | Harpooner | Siren | Groups that sing and throw |
| Darkcurrents | Abyssal | Riptide | Deep hunters that move fast |
| Trench Spears | Abyssal | Harpooner | Throwers from the dark under the waves |
| Deepshells | Abyssal | Barnacled | Armored shapes from the deep |
| Lantern Choir | Abyssal | Siren | Lights and songs in the dark water |
| Tideshells | Barnacled | Riptide | Shelled drowned that still swim fast |
| Breakwaters | Barnacled | Harpooner | Walls of shell that throw tridents |
| Wreckhulks | Barnacled | Abyssal | Heavy shapes from deep wrecks |
| Reefguard | Barnacled | Siren | Coral walls that call the others |
| Seafoam | Siren | Riptide | Fast singers on the surf |
| Spearsong | Siren | Harpooner | Singers that end the song with a trident |
| Deepsong | Siren | Abyssal | Songs from the dark that pull you down |
| Coral Choir | Siren | Barnacled | Tough groups that sing on the reef |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Riptides".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Stormlancers | Riptide and Harpooner | Fast throwers that ride the waves |
| Undercurrents | Riptide and Abyssal | Swift shapes from the deep |
| Reefsharks | Riptide and Barnacled | Hard and fast, made for the reef |
| Tidesingers | Riptide and Siren | Songs that move with the tide |
| Leviathans | Harpooner and Abyssal | Throwers from the dark that hit hard |
| Shipbreakers | Harpooner and Barnacled | Tough throwers that sink boats |
| Spear Chorus | Harpooner and Siren | A song that ends in tridents |
| Wreckborn | Abyssal and Barnacled | Shelled things from the deep wrecks |
| Drowning Choir | Abyssal and Siren | Dark songs that pull you into the deep |
| Reefsong | Barnacled and Siren | A living reef that sings |

## Neutral

A neutral drowned has no genes, or no family points.
It is a plain drowned, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| States for "in water", "on land", and "under sea level" | Riptides, Gills, Pressure Hide, and others |
| A pull on players in water | Sirens, Undertow, Drag Under |
| A trident spawn chance and a throw rate | Harpooners, Tridentborn, Volley, Barbed |
| A dash in water | Surge |
| Boat breaking | Capsizer |
| A jump out of the water | Breacher |
| Trident pickup | Scavenger |
| Lightning on trident hits | Stormcaller |
| A lure light that goes out | Lure |
| Damage from behind | Shellback |
| Melee thorns | Coral Growth |
| Heal one time at half health | Pearlheart |
| Stop baby drowned in vanilla ways | Fry |
| Gene marks under the outer layer, and the baby drowned model | All visuals |
| The pressures `in_water`, `on_land`, `boat`, and `trident` | Many genes |
