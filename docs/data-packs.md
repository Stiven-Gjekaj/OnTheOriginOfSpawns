# Data packs

Data packs define the pressures, the genes, and the perks of the mod.
Each entry is a JSON file at this path:

    data/<namespace>/originofspawns/<registry>/<name>.json

The registries are `pressure`, `gene`, and `perk`.
The world reads these files when it loads.
A file with an error stops the world from loading, and the log names the file.

## Pressure

A pressure is a cause of death.
A death counts for a pressure when its damage type is in the tag.

    { "damage_types": "#minecraft:is_fire" }

| Field | Type | Description |
|---|---|---|
| `damage_types` | damage type tag, with `#` | The damage types that count for this pressure |

The mod has these pressures: `fire`, `projectile`, `melee`, `explosion`, and
`fall`.
The list is not final.
The `melee` pressure uses the tag `#originofspawns:melee`.

## Gene

A gene is a trait that a species can carry.

    {
        "species": ["minecraft:zombie", "minecraft:husk"],
        "counters": "originofspawns:fire",
        "cost": 2,
        "mutation_rate": 0.01,
        "decay_rate": 0.001
    }

| Field | Type | Default | Description |
|---|---|---|---|
| `species` | entity type, list, or tag | | The mobs that can carry the gene |
| `counters` | pressure id | | The pressure that makes the gene stronger |
| `cost` | integer, 0 or more | | The gene points that the gene uses |
| `mutation_rate` | number, 0 to 1 | `0.01` | The random change in each generation |
| `decay_rate` | number, 0 to 1 | `0.001` | The loss in each generation when the pressure stops |

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
