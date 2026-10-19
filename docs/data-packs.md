# Data packs

Data packs define the pressures, the genes, and the perks of the mod.
Each entry is a JSON file at this path:

    data/<namespace>/originofspawns/<registry>/<name>.json

The registries are `pressure`, `gene`, and `perk`.
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

The mod has these pressures: `fire`, `projectile`, `melee`, `explosion`,
`fall`, and `unreached`.
The list is not final.
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

| Type | Fields | Result |
|---|---|---|
| `attribute` | `attribute`, `amount`, `operation` | Changes an attribute of the mob. `operation` is `add_value`, `add_multiplied_base`, or `add_multiplied_total` |
| `damage_taken` | `damage_types`, `multiplier` | Multiplies the damage from these damage types. `0` makes the mob immune |
| `ignite_target` | `chance`, `seconds` | Each hit has this chance to set the target on fire |
| `water_damage` | `amount`, `interval` (default `20`) | Damages the mob each `interval` ticks in water or rain |
| `air_loss` | `extra` | The mob loses this much more air each tick under water. Mobs that breathe under water, such as zombies, do not drown |

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
| `layers` | | The changes to the texture, in order. A layer with no `regions` or no `materials` covers all of them |
| `model` | none | A model change. `originofspawns:bone_legs` is the only one now |

The regions are `face`, `scalp`, `torso`, `arms`, `hands`, `legs`,
`lower_legs`, and `feet`. The materials are `skin`, `cloth`, and `eyes`.

| Effect | Fields | Result |
|---|---|---|
| `desaturate` | `amount`, `darken` | Moves the color toward gray, and makes it darker |
| `spots` | `colors`, `density` | Puts spots of the colors on a part of the pixels |
| `pattern` | `texture`, `tint` | Stamps a tiled grayscale texture in the tint color |
| `erase` | | Makes the pixels transparent |

### Genes on mobs

- A mob gets its genes one time, when it first joins a world.
- Each gene has a chance equal to its frequency in the pool of the biome
  category, or its mutation rate if that is higher.
- A mob carries a maximum of 5 genes. The genes with the highest frequency
  stay.

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
