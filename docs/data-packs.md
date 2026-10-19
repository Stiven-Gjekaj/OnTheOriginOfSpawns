# Data packs

Data packs define the pressures, the genes, the families, and the perks of the
mod.
Each entry is a JSON file at this path:

    data/<namespace>/originofspawns/<registry>/<name>.json

The registries are `pressure`, `gene`, `family`, and `perk`.
The world reads these files when it loads.
A file with an error stops the world from loading, and the log names the file.

## Pressure

A pressure is a cause of death.
A death counts for a pressure when it matches all the parts of the pressure.

    { "damage_types": "#minecraft:is_fire" }
    { "conditions": ["unreached"] }

| Field | Type | Default | Description |
|---|---|---|---|
| `damage_types` | damage type tag, with `#` | none | The damage types that count for this pressure |
| `conditions` | list of conditions | `[]` | The conditions of the death that must all be true |

A pressure needs `damage_types`, `conditions`, or both.

| Condition | True when |
|---|---|
| `unreached` | The mob targeted a player, but did not damage a player before it died |
| `sunlight` | The sun set the mob on fire, and the fire killed it |
| `spotted` | The killer saw the mob from more than 8 blocks, for 2 seconds or more before the death |
| `alone` | No other mob of the same species was in 8 blocks |
| `crowd` | 3 or more mobs of the same species died in 8 blocks in 5 seconds. The condition is true for each of them |
| `critical` | A critical hit of a player killed the mob |
| `golem` | An iron golem killed the mob |
| `player_death` | The mob killed a player. This is not a death of the mob. Each gene of the killer gets stronger |

The mod has these pressures: `fire`, `projectile`, `melee`, `explosion`,
`fall`, `unreached`, `sunlight`, `spotted`, `alone`, `crowd`, `critical`,
`golem`, and `player_death`.
The `melee` pressure uses the tag `#originofspawns:melee`.

## Gene

A gene is a trait that a species can carry.
It has gains and downsides, like a player perk.

    {
        "species": "minecraft:zombie",
        "counters": "originofspawns:fire",
        "family": "originofspawns:zombie/stalker",
        "sub_family": "originofspawns:zombie/predator",
        "gains": [
            { "type": "damage_taken", "damage_types": "#minecraft:is_fire", "multiplier": 0 }
        ],
        "downsides": [
            { "type": "attribute", "attribute": "minecraft:max_health", "amount": -4, "operation": "add_value" }
        ]
    }

| Field | Type | Default | Description |
|---|---|---|---|
| `species` | entity type, list, or tag | | The mobs that can carry the gene |
| `counters` | pressure id | | The pressure that makes the gene stronger |
| `family` | family id | | The main family tag. It gives 2 points |
| `sub_family` | family id | none | The sub family tag. It gives 1 point. Only a few genes have one |
| `gains` | list of effects | `[]` | The effects that help the mob |
| `downsides` | list of effects | `[]` | The effects that hurt the mob |
| `selection_rate` | number, 0 to 1 | `0.02` | The rise for each death, as a part of the distance to 100% |
| `mutation_rate` | number, 0 to 1 | `0.003` | The smallest chance that a new mob gets the gene |
| `decay_rate` | number, 0 to 1 | `0.01` | The loss for each Minecraft day, as a part of the frequency |

### Effects

Each effect has a `type` field.
A field with a value in parentheses is optional, and the value is its default.
Times are in seconds, and distances are in blocks.

#### Body and damage

| Type | Fields | Result |
|---|---|---|
| `attribute` | `attribute`, `amount`, `operation` | Changes an attribute of the mob. `operation` is `add_value`, `add_multiplied_base`, or `add_multiplied_total` |
| `damage_taken` | `damage_types`, `multiplier` | Multiplies the damage from these damage types. `0` makes the mob immune |
| `damage_taken_when` | `when`, `multiplier` | Multiplies the damage that the mob takes while the state is true |
| `damage_dealt_when` | `when`, `multiplier` | Multiplies the damage that the mob deals while the state is true |
| `damage_from` | `attackers`, `multiplier` | Multiplies the damage from these entity types, for example `minecraft:iron_golem` |
| `critical_immunity` | | A critical hit of a player does no extra damage to the mob |
| `speed_when` | `when`, `multiplier` | Multiplies the speed of the mob while the state is true |
| `attack_interval` | `multiplier` | Multiplies the time between two melee attacks. `0.7` attacks faster |
| `water_damage` | `amount`, `interval` (`20`) | Damages the mob each `interval` ticks in water or rain |
| `air_loss` | `extra` | The mob loses this much more air each tick under water. Mobs that breathe under water, such as zombies, do not drown |
| `regenerate` | `amount`, `interval_seconds` (`3`), `idle_seconds` (`5`) | Heals the mob after it was out of combat for `idle_seconds` |

The states for `when` are:

| State | True when |
|---|---|
| `always` | Always |
| `alone` | No other mob of the same species is in 8 blocks |
| `ambush` | The mob was not in combat for 10 seconds |
| `dark` | The light level at the mob is 6 or less |
| `bright` | The light level at the mob is 12 or more |
| `hunting_wounded` | The mob targets a player under half health |
| `in_water` | The mob is in water |

#### Hits

| Type | Fields | Result |
|---|---|---|
| `ignite_target` | `chance`, `seconds` | Each hit has this chance to set the target on fire |
| `effect_on_hit` | `effect`, `seconds`, `amplifier` (`0`), `chance` (`1`) | Each hit gives the target a status effect |
| `leech_xp` | `amount` | Each hit takes XP points from a player. The mob drops the XP that it took when it dies, and no other XP |
| `rally` | `per_ally`, `max`, `radius` (`6`) | Adds damage for each mob of the same species in the radius, up to `max`. `0.1` is 10% |
| `convert_villagers` | | A villager that the mob kills always becomes a zombie villager |

#### Groups

| Type | Fields | Result |
|---|---|---|
| `call_on_hurt` | `radius` | When the mob is hurt, the mobs of its species in the radius attack the attacker |
| `call_on_sight` | `radius`, `cooldown_seconds` (`30`) | When the mob first targets a player, the mobs of its species in the radius target that player |
| `grave_call` | `chance`, `max_calls` (`2`), `radius` (`8`) | When the mob is hurt, it has a chance to call a new mob of its species, with no genes |
| `brood` | `extra` | When the mob spawns in a natural way, this many more mobs with the same genes spawn near it |
| `runt` | `chance`, `baby_health_multiplier` (`0.5`) | The mob has a chance to spawn as a baby. Baby zombies spawn only through this effect |

#### Behavior

| Type | Fields | Result |
|---|---|---|
| `leap` | `min_distance` (`3`), `max_distance` (`5`), `cooldown_seconds` (`5`), `miss_stun_seconds` (`1`) | The mob leaps at its target. After a missed leap, it cannot move for a short time |
| `hunt` | `invisible_radius` (`0`), `wounded_range_multiplier` (`1`) | The mob finds invisible players in the radius, and players under half health farther away |
| `frenzied_targeting` | `chance`, `interval_seconds` (`10`), `radius` (`8`) | Now and then, the mob targets the nearest mob of a different species |
| `lurk` | `radius` (`6`) | The mob does not move while its target player faces it, until the player is in the radius |
| `burrow` | `player_radius` (`24`), `rise_seconds` (`2`) | At night, the mob can spawn under the ground near a player and rise. It rises only through the blocks in the tag `#originofspawns:burrowable` |
| `siege` | `break_seconds` (`10`) | The mob breaks doors on each difficulty, and breaks the blocks in the tag `#originofspawns:siege_breakable`. It obeys the `mobGriefing` gamerule |
| `raise_victim` | | A player that the mob kills rises as a mob of its species, with its genes |

#### Senses

| Type | Fields | Result |
|---|---|---|
| `silence` | `idle` (`false`), `hurt` (`false`), `steps` (`false`), `death` (`false`) | Stops these sounds of the mob |
| `loud` | `multiplier` | Multiplies the volume of the sounds of the mob |
| `translucent` | `max_light` (`7`), `opacity` (`0.1`) | The client draws the mob translucent at this light level or lower. Other mobs still see it |
| `glowing` | | The mob glows all the time, so that players see it through walls |
| `aura` | `effect`, `amplifier` (`0`), `radius`, `interval_seconds` (`2`), `particles` (`false`) | Gives a status effect to the players near the mob |

#### Death

| Type | Fields | Result |
|---|---|---|
| `death_burst` | `damage`, `radius` | When the mob dies, it damages the players in the radius |
| `death_cloud` | `effect`, `seconds`, `amplifier` (`0`), `radius`, `cloud_seconds` (`5`) | When the mob dies, it leaves a cloud that gives a status effect |

### Gene visuals

The look of a gene is in the resource packs, not in the data packs.
The file `assets/<namespace>/gene_visuals/<name>.json` is the visual of the
gene `<namespace>:<name>`.
A resource pack can change the look of any gene.

    {
        "region_map": "originofspawns:textures/region/humanoid.png",
        "layers": [
            { "materials": ["skin"], "effect": { "type": "desaturate", "amount": 0.4, "darken": 0.15 } },
            { "regions": ["hands", "feet"], "effect": { "type": "spots", "colors": ["#1A1A1A", "#8B2E1F"], "density": 0.35 } }
        ],
        "model": "originofspawns:bone_legs"
    }

| Field | Default | Description |
|---|---|---|
| `region_map` | `originofspawns:textures/region/humanoid.png` | The map that labels the pixels of the mob texture |
| `layers` | | The changes to the texture, in order. A layer with no `regions` or no `materials` covers all of them. A layer can have an `order`, an integer with the default `0` |
| `model` | none | A gene model that adds cubes to the mob, for example `originofspawns:bone_legs` |

The layers of all the genes of a mob go on in sequence of their `order`.
A layer with a lower order goes on first, so a higher order puts a mark on top
of the marks of other genes.
Layers with the same order keep the order of the gene ids, and the order in
their recipe.

The regions are `face`, `scalp`, `torso`, `arms`, `hands`, `legs`,
`lower_legs`, and `feet`. The materials are `skin`, `cloth`, and `eyes`.

| Effect | Fields | Result |
|---|---|---|
| `desaturate` | `amount`, `darken` | Moves the color toward gray, and makes it darker |
| `spots` | `colors`, `density` | Puts spots of the colors on a part of the pixels |
| `pattern` | `texture`, `tint` | Stamps a tiled grayscale texture in the tint color |
| `erase` | | Makes the pixels transparent |

### Gene models

A gene model adds cubes to the model of a mob.
The file `assets/<namespace>/gene_models/<name>.json` is the model
`<namespace>:<name>`.
Each cube moves with one bone: `head`, `body`, `right_arm`, `left_arm`,
`right_leg`, or `left_leg`.
The coordinates are in pixels, in the coordinates of the bone, as in the
vanilla models.

    {
        "texture": "originofspawns:textures/entity/bone_leg.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": [
            { "bone": "right_leg", "uv": [0, 0], "from": [-1.5, 6, -1.5], "size": [3, 6, 3] }
        ]
    }

| Field | Default | Description |
|---|---|---|
| `texture` | | The texture of the cubes |
| `texture_width`, `texture_height` | `64` | The size of the texture |
| `parts` | | The cubes |

| Part field | Default | Description |
|---|---|---|
| `bone` | | The bone that the cube moves with |
| `uv` | | The top left corner of the box UV in the texture |
| `from` | | The corner of the cube with the smallest coordinates |
| `size` | | The size of the cube |
| `inflate` | `0` | Makes the cube larger on each side, with no change to its UV |
| `mirror` | `false` | Mirrors the UV |
| `pivot` | `[0, 0, 0]` | The point that the cube turns around, in the coordinates of the bone |
| `rotation` | `[0, 0, 0]` | The angles around the X, Y, and Z axes of the pivot, in degrees |
| `glow` | `false` | Gives the cube full light, so that it is bright in the dark |

### Genes on mobs

- A mob gets its genes one time, when it first joins a world.
- Each gene has a chance equal to its frequency in the pool of the biome
  category, or its mutation rate if that is higher.
- A mob carries a maximum of 5 genes. The genes with the highest frequency
  stay.

## Family

A family is a major family of a species.
The genes of a mob give points to families, and the points decide the family
of the mob.

    {
        "species": "minecraft:zombie",
        "tiers": [
            { "buffs": [...], "debuffs": [...] },
            { "buffs": [...], "debuffs": [...] },
            { "buffs": [...], "debuffs": [...] }
        ]
    }

| Field | Type | Description |
|---|---|---|
| `species` | entity type, list, or tag | The mobs that the family is for |
| `tiers` | list of 3 tiers | The effects of tier 1, tier 2, and tier 3. Each tier has `buffs` and `debuffs`, which are lists of effects |

A main family tag gives 2 points, and a sub family tag gives 1 point.

| Result | Rule | Effects |
|---|---|---|
| Neutral | No family has 4 points | None |
| Major | One family has 4 points or more, and no other family has 2 | Tier 2 of the major |
| Pure | One family has 6 points or more, and no other family has points | Tier 3 of the family |
| Minor | The major has 4 points or more, and a sub family has 2 or more | Tier 2 of the major, and tier 1 of the sub |
| Hybrid | Two families have the same points, 4 or more | Tier 2 of each |

The names of the families are in the language files:

| Result | Key |
|---|---|
| Pure or major | `family.<namespace>.<path>`, for example `family.originofspawns.zombie.predator` |
| Minor | The key of the major, then the last part of the sub, for example `family.originofspawns.zombie.predator.stalker` |
| Hybrid | The key of the first family by id, then `and`, then the last part of the second, for example `family.originofspawns.zombie.predator.and.stalker` |
| Neutral | `family.originofspawns.neutral` |

## Perk

A perk is a player adaptation.

    { "xp_cost": 5, "incompatible_with": ["originofspawns:carnivore"] }

| Field | Type | Default | Description |
|---|---|---|---|
| `xp_cost` | integer, 0 or more | | The XP levels that the player pays |
| `incompatible_with` | list of perk ids | `[]` | The perks that the player cannot have at the same time |

## Debug commands

The commands need permission level 2.

| Command | Result |
|---|---|
| `/originofspawns pool get <species> <category>` | Shows the gene frequencies of a pool |
| `/originofspawns pool set <species> <category> <gene> <frequency>` | Sets one gene frequency, from 0 to 1 |

The categories are `hot`, `cold`, `wet`, `temperate`, `nether`, and `end`.
The world saves the pools in `data/originofspawns/gene_pools.dat`.
