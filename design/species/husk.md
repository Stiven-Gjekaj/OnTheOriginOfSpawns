# Husk

This document defines the families and the genes of the husk.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The husk lives in hot, dry biomes. The sun does not burn it, and its hits
give Hunger. Its genes fit sand, heat, thirst, and the dead of the desert.
It shares no genes and no families with the zombie.

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

### Scorchers

Scorchers take power from the sun. They are weak in the dark.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage and 20% more speed at light level 12 or more | 10% less damage at light level 6 or less |
| Tier 2 | 20% more damage at light level 12 or more | 20% less damage at light level 6 or less |
| Tier 1 | 10% more damage at light level 12 or more | 25% less damage at light level 6 or less |

### Dunecrawlers

Dunecrawlers move fast on sand. They are slow on other ground.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 50% more speed on sand, and full knockback resistance on sand | 5% less speed off sand |
| Tier 2 | 30% more speed on sand | 10% less speed off sand |
| Tier 1 | 15% more speed on sand | 15% less speed off sand |

### Mummies

Mummies are wrapped, old, and hard to kill. Fire hurts them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 60% more maximum health, and each hit gives Slowness for 3 seconds | 25% more fire damage |
| Tier 2 | 40% more maximum health | 50% more fire damage |
| Tier 1 | 20% more maximum health | 75% more fire damage |

### Caravans

Caravans travel in lines and carry each other. A husk of a caravan shares
the damage that it takes with the husks near it.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 50% of the damage that it takes goes to the husks in 8 blocks, split between them | 10% more damage taken with no husk in 8 blocks |
| Tier 2 | 30% of the damage goes to the husks in 8 blocks | 20% more damage taken with no husk in 8 blocks |
| Tier 1 | 15% of the damage goes to the husks in 8 blocks | 25% more damage taken with no husk in 8 blocks |

### Parchers

Parchers spread thirst. Their hits drain food. Water hurts them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit gives Hunger II for 10 seconds, and takes 2 food points | 1 damage each 4 seconds in water or rain |
| Tier 2 | Each hit gives Hunger II for 10 seconds | 1 damage each 3 seconds in water or rain |
| Tier 1 | Each hit gives Hunger for 10 seconds | 1 damage each 2 seconds in water or rain |

## Pressures

The husk uses the zombie pressures, except `sunlight`, because the sun does
not burn a husk. It also uses these new pressures.

| Pressure | A husk death counts when | State |
|---|---|---|
| `in_water` | The husk dies in water | New |
| `cold` | The husk dies in a cold biome, or freezing kills it | New |
| `night` | The husk dies between dusk and dawn | New |
| `contact` | A cactus or a sweet berry bush kills it | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Scorcher genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sunbaked | Scorcher | `night` | Heals 1 health each 2 seconds in full sun | 50% more damage taken at light level 6 or less | Skin: cracked and sunburnt |
| Heatshimmer | Scorcher, sub Dunecrawler | `projectile` | At light level 12 or more, each projectile has a 30% chance to miss it | 15% less maximum health | Body: pale, bleached patches |
| Kilnskin | Scorcher | `fire` | Fire and lava do no damage to it | Water and rain do 2 times the damage of a Parcher | Skin: glazed, brick red |
| Sunstroke | Scorcher | `melee` | At light level 12 or more, each hit gives Nausea and Slowness for 3 seconds | 2 less attack damage | Face: blisters |
| Mirage | Scorcher | `spotted` | When a player looks at it from more than 12 blocks, it moves 4 blocks to the side, each 10 seconds | 10% less speed | Eyes: a pale shimmer |
| Solar Flare | Scorcher | `crowd` | When it dies in daylight, players in 4 blocks get Blindness for 3 seconds | 4 less maximum health | Chest: a sun mark that glows. This changes the model |

### Dunecrawler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sandswimmer | Dunecrawler | `unreached` | When it spawns near a player on sand, it moves under the sand and rises next to the player | It cannot move for 1 second after it rises | Body: sand in the folds of the cloth |
| Dune Runner | Dunecrawler | `unreached` | 25% more speed on sand | 2 less armor | Feet: wrapped in cloth |
| Sinkhole | Dunecrawler | `golem` | Each hit on a target on sand gives Slowness IV for 1 second | 20% less attack damage | Hands: sand that pours from them |
| Scuttler | Dunecrawler | `fall` | No fall damage, and it climbs walls like a spider | 15% less maximum health | Arms and legs: bent, with sand on the joints |
| Sandblast | Dunecrawler | `projectile` | It throws sand at a player in 8 blocks, which gives Blindness for 2 seconds, each 8 seconds | 15% less attack speed | Mouth: full of sand |
| Duststorm | Dunecrawler, sub Caravan | `alone` | Players in 5 blocks get Darkness for 2 seconds, renewed each 6 seconds | Players see it from far away, because of the dust | Body: a cloud of dust particles |

### Mummy genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Wrapped | Mummy | `projectile` | 25% less damage from projectiles and melee | 50% more fire damage | Body: bandages |
| Cursetouch | Mummy | `melee` | Each hit gives Weakness for 5 seconds | 15% less attack speed | Hands: dark wraps |
| Undying | Mummy | `critical` | One time in its life, a hit that kills it leaves it with 4 health | 20% less speed | Chest: a sealed jar mark |
| Embalmed | Mummy, sub Parcher | `player_death` | Poison, Wither, and Hunger do not affect it, and it heals out of combat | 4 less maximum health | Skin: dark resin |
| Tomb Guard | Mummy | `explosion` | Full knockback resistance, and 40% less explosion damage | 20% less speed | Neck and wrists: gold trinkets |
| Grave Dust | Mummy | `crowd` | When it dies, it leaves a cloud of Slowness, 3 blocks wide | 4 less maximum health | Wraps: torn and dusty |

### Caravan genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Packmule | Caravan | `alone` | It picks up items and wears armor more often | It drops all of its items when it dies | Back: a pack. This changes the model |
| Linked | Caravan | `alone` | 30% less damage taken with another husk in 4 blocks | 30% more damage taken with no husk in 4 blocks | Wrists: a length of rope |
| Signal Fire | Caravan | `spotted` | When it is hurt, it makes a column of smoke, and the husks in 32 blocks come to it | Players see the smoke from far away | Back: a small torch. This changes the model |
| Sandling | Caravan | `projectile` | 20% chance to spawn as a baby husk | Its babies have 50% less maximum health | The baby model, and head: a small wrap |
| Nomad | Caravan, sub Dunecrawler | `crowd` | Its spawn groups have 1 more husk, and the group follows the first husk in a line | Each husk of the group has 15% less maximum health | Neck: a long scarf |
| Waterskin | Caravan | `in_water` | When it heals, the husks in 6 blocks heal by the same amount | It heals 25% slower | Hip: a leather water bag. This changes the model |

### Parcher genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Dehydrator | Parcher | `melee` | Each hit gives Hunger II for 10 seconds | 2 less attack damage | Lips: cracked and dry |
| Thirstbite | Parcher | `in_water` | Each hit takes 1 food point from the player and heals the husk by 1 | Water and rain do 1 damage each 3 seconds | Teeth: long and yellow |
| Desiccate | Parcher | `alone` | Players in 4 blocks get Hunger, renewed each 2 seconds | It glows, so players see it through walls | Skin: flakes that fall as particles |
| Salt Crust | Parcher, sub Mummy | `in_water` | It does not convert to a zombie in water | 50% less speed in water | Body: white salt crystals |
| Mirage Water | Parcher | `contact` | Players in 12 blocks see a false pool of water at its feet, and the husk is 50% harder to see | 10% less maximum health | Feet: a wet shimmer |
| Dust Lung | Parcher | `cold` | When it dies, it leaves a cloud of Hunger, 3 blocks wide | 4 less maximum health | Chest: a sunken, dusty ribcage |

## Gene details

| Gene | Rules |
|---|---|
| Heatshimmer | The miss is a full miss: the projectile goes through the husk |
| Mirage | It moves only to a safe block. It does not move into walls or water |
| Sandswimmer | It works like the zombie Burrower, but only through blocks in the tag `#minecraft:sand` |
| Scuttler | It climbs like a spider. It cannot climb glass |
| Undying | It shows a burst of sand particles when it survives the hit |
| Packmule | It can pick up items, as some vanilla zombies can |
| Signal Fire | It can make smoke again after 30 seconds |
| Sandling | Baby husks spawn only through this gene |
| Salt Crust | It stops the vanilla conversion of a husk to a zombie in water |
| Mirage Water | The false water is a client effect. It is not a block |

### Limits of the first version

These genes work in a simpler way than the table says, for now.

| Gene | In the mod now |
|---|---|
| Sinkhole | The Slowness comes when the husk stands on sand, not when the target does |
| Duststorm | Its downside is the Glowing effect, so players see it through walls |
| Signal Fire | The husks in 32 blocks attack the attacker. They do not walk to the smoke first |
| Nomad | The group spawns, but it does not walk in a line yet |
| Mirage Water | It is 50% translucent at all times. The false pool of water is not in the mod yet |
| Kilnskin | Water and rain do 1 damage each second |

## Baby husks

- Baby husks do not spawn in vanilla ways.
- Only the Sandling gene makes baby husks.
- At the start of a world, no baby husks spawn.

## Conversion

- A husk that converts to a zombie in water gets new genes from the zombie
  pool. It keeps none of its husk genes.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Sandglare | Scorcher | Dunecrawler | Quick hunters that strike from the bright dunes |
| Sunkings | Scorcher | Mummy | Old, tough dead that grow strong at noon |
| Noonmarchers | Scorcher | Caravan | Lines of husks that march in the full sun |
| Sunbleached | Scorcher | Parcher | Hot, dry biters that burn and starve the player |
| Glasswalkers | Dunecrawler | Scorcher | Fast sand runners that hide in the heat haze |
| Tombdiggers | Dunecrawler | Mummy | Tough husks that come up out of the sand |
| Dune Raiders | Dunecrawler | Caravan | Groups that cross the sand fast |
| Dust Devils | Dunecrawler | Parcher | Swift husks that leave thirst behind them |
| Gilded Dead | Mummy | Scorcher | Wrapped kings that wake in the sun |
| Sandbound | Mummy | Dunecrawler | Wrapped dead that walk the dunes with ease |
| Funeral Train | Mummy | Caravan | Slow, tough processions that share their wounds |
| Saltwraps | Mummy | Parcher | Dry, salted dead that drain the player |
| Sunwalkers | Caravan | Scorcher | Travelers that march strong through the day |
| Drifters | Caravan | Dunecrawler | Wanderers that follow the dunes |
| Processions | Caravan | Mummy | Long lines of tough, wrapped husks |
| Dry Caravans | Caravan | Parcher | Thirsty groups that strip the player of food |
| Sunstricken | Parcher | Scorcher | Thirst made worse by the heat |
| Siltmouths | Parcher | Dunecrawler | Fast husks with mouths full of sand |
| Cracked Ones | Parcher | Mummy | Dry, tough husks that will not rot |
| Famished Host | Parcher | Caravan | Hungry crowds that share what little they have |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Scorchers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Mirage Kin | Scorcher and Dunecrawler | Shapes in the heat haze that move too fast |
| Sunlords | Scorcher and Mummy | Ancient dead that rule the day |
| Noon Caravan | Scorcher and Caravan | Travelers that do not rest from the sun |
| Kiln-Dried | Scorcher and Parcher | Husks baked hard and dry |
| Barrow Sands | Dunecrawler and Mummy | The dead under the dunes |
| Duneherd | Dunecrawler and Caravan | Herds that move across the sand as one |
| Sandsuckers | Dunecrawler and Parcher | Fast husks that dry out all they touch |
| Cortege | Mummy and Caravan | A funeral line that will not stop |
| Natron Dead | Mummy and Parcher | Salted, preserved, and always thirsty |
| Last Oasis | Caravan and Parcher | Desperate groups that hunt for water |

## Neutral

A neutral husk has no genes, or no family points.
It is a plain husk, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A state for "on sand" and "off sand" | Dunecrawlers, Dune Runner, Sinkhole |
| Shared damage between mobs near each other | Caravans |
| A chance for projectiles to miss | Heatshimmer |
| A short move to the side when a far player looks at the mob | Mirage |
| Wall climbing | Scuttler |
| A ranged throw that gives an effect | Sandblast |
| Survive one lethal hit | Undying |
| Immunity to some status effects | Embalmed |
| Item pickup | Packmule |
| A smoke signal that calls mobs to a place | Signal Fire |
| Shared healing | Waterskin |
| Food point drain on hit | Thirstbite, Parchers |
| Stop the conversion to a zombie | Salt Crust |
| A false water effect on the client | Mirage Water |
| The pressures `in_water`, `cold`, `night`, and `contact` | Many genes |
