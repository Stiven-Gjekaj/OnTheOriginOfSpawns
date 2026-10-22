# Cave spider

This document defines the families and the genes of the cave spider.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The cave spider is a small, blue spider of the mineshafts. It has 12
health, and it comes from spawners. Its bite gives Poison for 7 seconds on
Normal and for 15 seconds on Hard. It fits through gaps of one block. It
climbs walls, cobwebs do not slow it, and Poison does not affect it. Its
genes fit stronger venom, the dark of the caves, fast runs through the
tunnels, the nest at the spawner, and crystals from the stone. It shares no
genes and no families with the spider or the other species.

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

### Toxins

Toxins carry a stronger venom. Their bites are weaker.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | The Poison of its bite lasts 50% longer and is one level stronger | 10% less damage |
| Tier 2 | The Poison of its bite lasts 50% longer | 15% less damage |
| Tier 1 | The Poison of its bite lasts 25% longer | 20% less damage |

### Gloomers

Gloomers bring the dark of the caves with them. Bright light hurts them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit gives Darkness for 4 seconds | 10% more damage in bright light |
| Tier 2 | Each hit gives Darkness for 3 seconds | 20% more damage in bright light |
| Tier 1 | Each hit gives Darkness for 2 seconds | 25% more damage in bright light |

### Scurriers

Scurriers run fast through the tunnels. Their bodies are small and weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 25% more speed | 10% less maximum health |
| Tier 2 | 20% more speed | 20% less maximum health |
| Tier 1 | 15% more speed | 25% less maximum health |

### Broodkeepers

Broodkeepers guard the spawner that they came from. Far from it, they are
slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage in 16 blocks of a spawner | 10% less speed farther than 16 blocks from a spawner |
| Tier 2 | 20% more damage in 16 blocks of a spawner | 15% less speed farther than 16 blocks from a spawner |
| Tier 1 | 15% more damage in 16 blocks of a spawner | 20% less speed farther than 16 blocks from a spawner |

### Geodes

Geodes grow crystals out of the stone. A pickaxe breaks them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor | 25% more damage from pickaxes |
| Tier 2 | 4 more armor | 50% more damage from pickaxes |
| Tier 1 | 2 more armor | 75% more damage from pickaxes |

## Pressures

The cave spider uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `shield`, and `bane`. It lives in the
dark under the ground, so it does not use `night`, `day`, or `sunlight`.
Wolves do not attack it, so it does not use `wolf`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Toxin genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Neurotoxin | Toxin | `melee` | Its hits give Slowness II for 3 seconds | 10% less damage | Head: blue tips on the fangs |
| Hemotoxin | Toxin | `critical` | Its hits give Wither for 2 seconds | 4 less maximum health | Head: dark red fangs |
| Toxic Cloud | Toxin, sub Broodkeeper | `crowd` | When it dies, it leaves a cloud of Poison for 4 seconds | 4 less maximum health | Abdomen: green spots |
| Venom Spray | Toxin | `unreached` | A player that hits it in melee gets Poison for 2 seconds | 10% less speed | Body: green pores |
| Lingering Venom | Toxin | `projectile` | Its hits give Nausea for 4 seconds | 10% less speed | Head: green drool from the fangs |
| Corrosive | Toxin, sub Geode | `shield` | Its hits damage the armor of a player two times as much | 10% less damage | Head: yellow fangs |

### Gloomer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Lightsnuffer | Gloomer | `spotted` | Its hits give Blindness for 2 seconds | 10% less maximum health | Head: black around the eyes |
| Shade Skin | Gloomer | `projectile` | It is almost invisible at a light level of 7 or less | 10% less maximum health | Body: dark blue patches |
| Echo Sense | Gloomer | `alone` | It sees invisible players in 16 blocks | 10% less speed | Head: pale, blind eyes |
| Night Eyes | Gloomer, sub Scurrier | `melee` | 50% more follow range in the dark | 10% less maximum health | Eyes: green eyes |
| Dread | Gloomer | `crowd` | Players in 6 blocks get Darkness for 3 seconds, each 15 seconds | Players hear it from two times farther | Back: a dark mist pattern |
| Cave Ghost | Gloomer | `critical` | In the dark, players farther than 10 blocks do not see it | 10% less damage | Legs: faded, pale legs |

### Scurrier genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Dash | Scurrier | `projectile` | 20% more speed | 10% less maximum health | Legs: thin blue stripes |
| Juke | Scurrier | `spotted` | It steps to the side when a player aims at it from far away | 10% less damage | Body: a zigzag pattern |
| Gap Runner | Scurrier | `melee` | It is 20% smaller | 10% less maximum health | Body: a smaller, flat body |
| Nimble | Scurrier, sub Gloomer | `projectile` | 25% chance to dodge a projectile | 10% less damage | Legs: pale joints |
| Frenzy Bite | Scurrier | `crowd` | It attacks 25% faster | 10% less damage | Head: chipped fangs |
| Ceiling Crawler | Scurrier | `fall` | No fall damage | 10% less maximum health | Feet: hooked claws. This changes the model |

### Broodkeeper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Brood Call | Broodkeeper | `melee` | When something hurts it, it calls the cave spiders in 16 blocks | 10% less damage | Back: red hairs |
| Nest Weaver | Broodkeeper | `unreached` | Each hit has a 20% chance to put a cobweb at the target | 25% more fire damage | Abdomen: a web pattern |
| Egg Layer | Broodkeeper | `alone` | Its spawn groups have 1 more cave spider | Each cave spider of the group has 15% less maximum health | Abdomen: white eggs on the back. This changes the model |
| Nest Mend | Broodkeeper | `critical` | It heals 1 health each 3 seconds in 16 blocks of a spawner | 4 less maximum health | Body: a pale silk band |
| Swarm Mind | Broodkeeper | `crowd` | It shares the damage that it takes with the cave spiders in 8 blocks | 10% less speed | Head: a pale crown of hairs |
| Spawner Bond | Broodkeeper, sub Geode | `golem` | 4 more armor in 16 blocks of a spawner | 10% less speed | Legs: iron-gray bands |

### Geode genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Amethyst Shell | Geode | `projectile` | 4 more armor | 10% less speed | Abdomen: purple crystals. This changes the model |
| Shatter | Geode | `critical` | When it dies, shards of crystal do 3 damage to the players in 3 blocks | 4 less maximum health | Back: sharp crystal shards. This changes the model |
| Chime | Geode | `melee` | A player that hits it in melee gets Nausea for 3 seconds | 10% less damage | Body: thin crystal lines |
| Ore Skin | Geode | `explosion` | Full knockback resistance | 10% less speed | Body: flecks of iron ore |
| Stone Jaws | Geode | `golem` | 20% more damage | 10% less speed | Head: gray stone fangs |
| Deepslate Plates | Geode | `projectile` | 30% less damage from projectiles | 15% less speed | Body: dark deepslate plates |

## Gene details

| Gene | Rules |
|---|---|
| Toxins | They change the Poison that the cave spider gives with each bite in vanilla. On Easy, the bite gives no Poison, so the buff does nothing there. "One level stronger" is Poison II |
| Gloomers, Night Eyes, Cave Ghost | "Bright light" is a light level of 12 or more. "The dark" is a light level of 6 or less, as for the other species |
| Broodkeepers, Nest Mend, Spawner Bond | "A spawner" is any monster spawner block in the radius, not only the spawner that the cave spider came from |
| Geodes | "A pickaxe" is an item in the tag `#minecraft:pickaxes` in the hand of the attacker |
| Venom Spray, Chime | They work as the bogged Rotting Touch, for melee hits only |
| Juke | It works as the zombie sidestep, with the aim check |
| Gap Runner | It uses the vanilla `scale` attribute. A smaller cave spider fits through smaller gaps and is harder to hit |
| Nimble | It works as the parched Wind Dodge |
| Cave Ghost | It works as the bogged Swamp Shroud, in the dark |
| Nest Weaver | It works as the spider Weavers, with the same cobwebs |
| Egg Layer | It works as the spider Clutch |
| Swarm Mind | It works as the spider Hive Mind |
| Shatter | It uses the `death_burst` effect |

## Equipment

- A cave spider has no weapon and no armor in vanilla. It does not get them
  from its genes.

## Conversion

- The cave spider does not convert to a different species, and no species
  converts to the cave spider.

## Babies and jockeys

- Cave spiders have no babies and no jockeys in vanilla. They do not get a
  gene for them.
- A cave spider from the spider Mother has no genes from the spider. It rolls
  its own genes from the cave spider pool.

## Body

- The cave spider has the model of the spider, at 0.7 times the size. Its
  texture is 64 x 32, with the same layout as the spider texture.
- It uses the spider region map, `textures/region/spider.png`.
- The glowing eyes of the cave spider draw over the gene marks.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Blind Biters | Toxin | Gloomer | Venom in the dark |
| Quick Venoms | Toxin | Scurrier | Fast venom |
| Nest Venoms | Toxin | Broodkeeper | Venom that guards the nest |
| Crystal Fangs | Toxin | Geode | Venom with a crystal shell |
| Dark Venoms | Gloomer | Toxin | Darkness and venom |
| Shadow Darts | Gloomer | Scurrier | Fast in the dark |
| Nest Shadows | Gloomer | Broodkeeper | Dark guards of the nest |
| Dark Geodes | Gloomer | Geode | Crystals in the dark |
| Venom Runners | Scurrier | Toxin | Fast bites with venom |
| Tunnel Shades | Scurrier | Gloomer | Fast and hidden |
| Nest Runners | Scurrier | Broodkeeper | Fast guards of the nest |
| Crystal Darts | Scurrier | Geode | Fast and armored |
| Venom Keepers | Broodkeeper | Toxin | Guards with venom |
| Dark Keepers | Broodkeeper | Gloomer | Guards in the dark |
| Swift Keepers | Broodkeeper | Scurrier | Fast guards |
| Stone Keepers | Broodkeeper | Geode | Armored guards |
| Toxic Crystals | Geode | Toxin | Crystals with venom |
| Black Geodes | Geode | Gloomer | Dark crystals |
| Quartz Runners | Geode | Scurrier | Fast crystals |
| Spawner Stones | Geode | Broodkeeper | Crystals at the nest |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Toxins".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Deep Stingers | Gloomer and Toxin | Venom from the dark |
| Mineshaft Plagues | Scurrier and Toxin | Fast venom in the tunnels |
| Poison Nests | Broodkeeper and Toxin | A nest full of venom |
| Crystal Venoms | Geode and Toxin | Venom in a shell |
| Dark Runners | Gloomer and Scurrier | Fast and dark |
| Shadow Nests | Broodkeeper and Gloomer | A dark nest |
| Night Geodes | Geode and Gloomer | Crystals in the dark |
| Tunnel Swarms | Broodkeeper and Scurrier | A fast nest |
| Living Geodes | Geode and Scurrier | Fast crystals |
| Crystal Nests | Broodkeeper and Geode | An armored nest |

## Neutral

A neutral cave spider has no genes, or no family points.
It is a plain cave spider, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the cave spider | All visuals |
| The change of the Poison of the bite of the cave spider | Toxins |
| A state for a spawner in 16 blocks, and its opposite | Broodkeepers, Nest Mend, Spawner Bond |
| More damage from items in a tag, such as pickaxes | Geodes |
| A check that the spider effects work on a cave spider | Nest Weaver, Egg Layer, Swarm Mind, Juke |
