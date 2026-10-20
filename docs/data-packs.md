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
| `in_water` | The mob died in water |
| `on_land` | The mob died out of water |
| `cold` | The mob died in a cold biome, or freezing killed it |
| `night` | The mob died while it was dark outside |
| `boat` | The killer was in a boat |
| `wolf` | A wolf killed the mob |
| `shield` | The killer blocked a hit with a shield in the 10 seconds before the death, or holds its shield up |

The mod has these pressures: `fire`, `projectile`, `melee`, `explosion`,
`fall`, `unreached`, `sunlight`, `spotted`, `alone`, `crowd`, `critical`,
`golem`, `player_death`, `in_water`, `on_land`, `cold`, `night`, `boat`,
`contact`, `trident`, `wolf`, and `shield`.
The `contact` pressure uses the tag `#originofspawns:contact`: cacti and sweet
berry bushes. The `trident` pressure uses the tag `#originofspawns:trident`.
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
| `damage_taken_when` | `when`, `multiplier`, `radius` (`8`) | Multiplies the damage that the mob takes while the state is true. The states `alone` and `grouped` use the radius |
| `damage_dealt_when` | `when`, `multiplier`, `players_only` (`false`), `hits` (`any`) | Multiplies the damage that the mob deals while the state is true. With `players_only`, only the damage to players. `hits` is `melee`, `projectile`, or `any` |
| `damage_from` | `attackers`, `multiplier` | Multiplies the damage from these entity types, for example `minecraft:iron_golem` |
| `sunburn` | `multiplier` | Multiplies the fire damage while the sun burns the mob. Other fire does normal damage |
| `critical_immunity` | | A critical hit of a player does no extra damage to the mob |
| `speed_when` | `when`, `multiplier` | Multiplies the speed of the mob while the state is true |
| `attribute_when` | `when`, `attribute`, `amount`, `operation` | Changes an attribute of the mob while the state is true |
| `dodge_projectiles` | `chance`, `when` (`always`) | Each projectile has this chance to miss the mob. A missed projectile goes through the mob |
| `undying` | `health` (`4`) | One time in its life, a lethal hit leaves the mob with this health |
| `effect_immunity` | `effects` | These status effects do not affect the mob |
| `heal_multiplier` | `multiplier` | Multiplies each heal of the mob |
| `attack_interval` | `multiplier` | Multiplies the time between two melee attacks. `0.7` attacks faster |
| `water_damage` | `amount`, `interval` (`20`) | Damages the mob each `interval` ticks in water or rain |
| `air_loss` | `extra` | The mob loses this much more air each tick under water. Mobs that breathe under water, such as zombies, do not drown |
| `regenerate` | `amount`, `interval_seconds` (`3`), `idle_seconds` (`5`), `when` (`always`) | Heals the mob after it was out of combat for `idle_seconds`, while the state is true |
| `harm_when` | `when`, `amount`, `interval_seconds` (`4`) | Hurts the mob after each interval while the state is true. The damage is drying out, so fire resistance does not stop it |
| `damage_from_behind` | `multiplier` | Multiplies the damage from an attacker behind the mob |
| `damage_from_front` | `multiplier`, `hits` (`any`) | Multiplies the damage from an attacker in front of the mob |
| `damage_from_enchantment` | `enchantment`, `multiplier` | Multiplies the damage from a weapon with the enchantment, for example `minecraft:smite` |
| `reassemble` | `seconds` (`3`), `health` (`0.5`) | One time in its life, the mob falls apart in place of its death, and stands up again after `seconds` with this part of its health. While it is down, only a weapon with Smite hurts it, and a Smite hit stops the return |
| `thorns` | `damage` | An attacker that hits the mob in melee takes this damage |
| `heal_once` | `threshold` (`0.5`) | One time in its life, the mob heals to full health when its health drops under this part of its maximum |
| `swim_vertical` | `down` (`1`), `up` (`1`) | In water, the mob sinks faster with a `down` above 1, and swims up slower with an `up` under 1 |

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
| `on_sand` | The mob stands on a block in the tag `#minecraft:sand` |
| `off_sand` | The mob does not stand on sand |
| `grouped` | Another mob of the same species is in 8 blocks |
| `on_land` | The mob is not in water |
| `deep` | The mob is 10 or more blocks under the sea level of the world |
| `above_sea_level` | The mob is at the sea level of the world or above it |
| `storm` | A thunderstorm is in the world |
| `sunlit` | The mob is out of water and rain, in the daylight, under the open sky |
| `target_below` | The target of the mob is 2 or more blocks lower than the mob |
| `target_above` | The target of the mob is 2 or more blocks higher than the mob |
| `wounded` | The health of the mob is under half of its maximum |

#### Hits

| Type | Fields | Result |
|---|---|---|
| `ignite_target` | `chance`, `seconds`, `hits` (`melee`) | Each hit has this chance to set the target on fire |
| `pierce_shields` | | The projectiles of the mob go through shields, with their full damage |
| `effect_on_hit` | `effect`, `seconds`, `amplifier` (`0`), `chance` (`1`), `when` (`always`), `hits` (`melee`) | Each hit gives the target a status effect, while the state is true. `hits` is `melee`, `projectile`, or `any`. A thrown trident is a projectile |
| `pull_down_on_hit` | `blocks` | Each hit on a player in water pulls the player down by up to this many blocks. A block stops the pull |
| `drag_under` | `seconds`, `sink_speed` (`0.12`) | When the mob hits a player in water, it holds on, and both sink. The player breaks free with a hit on the mob, or when the player leaves the water |
| `break_boats` | `hits` (`3`) | The hits on a boat or on its rider break the boat after this many hits. The riders fall out |
| `lightning_on_hit` | `storm_only` (`true`) | A projectile hit calls lightning on a target under the open sky. With `storm_only`, only in a thunderstorm |
| `drain_food` | `amount`, `heal` (`0`) | Each hit on a player takes food points, and heals the mob |
| `leech_xp` | `amount` | Each hit takes XP points from a player. The mob drops the XP that it took when it dies, and no other XP |
| `rally` | `per_ally`, `max`, `radius` (`6`) | Adds damage for each mob of the same species in the radius, up to `max`. `0.1` is 10% |
| `convert_villagers` | | A villager that the mob kills always becomes a zombie villager |

#### Groups

| Type | Fields | Result |
|---|---|---|
| `effect_on_hurt` | `effect`, `seconds`, `amplifier` (`0`), `cooldown_seconds` (`0`), `ink` (`false`), `ally_radius` (`0`) | When an attacker hurts the mob, the attacker gets a status effect, after each cooldown. `ink` also sprays a cloud of ink. With an `ally_radius` above 0, the mobs of its species in the radius get the effect, and the attacker does not |
| `barrage` | `group` (`3`), `radius` (`16`) | When the mob shoots, and this many mobs of its species in the radius target the same entity, they all shoot at the same time |
| `call_on_hurt` | `radius`, `smoke` (`false`), `cooldown_seconds` (`0`) | When the mob is hurt, the mobs of its species in the radius attack the attacker. `smoke` also makes a column of smoke. The call waits for its cooldown |
| `share_damage` | `fraction`, `radius` (`8`) | A part of the damage that the mob takes goes to the mobs of its species in the radius |
| `share_healing` | `radius` (`6`) | When the mob heals, the mobs of its species in the radius heal by the same amount |
| `call_on_sight` | `radius`, `cooldown_seconds` (`30`) | When the mob first targets a player, the mobs of its species in the radius target that player |
| `grave_call` | `chance`, `max_calls` (`2`), `radius` (`8`) | When the mob is hurt, it has a chance to call a new mob of its species, with no genes |
| `jockey` | `chance`, `mount` | When the mob spawns in a natural way, it has this chance to ride a new mob of the mount type |
| `brood` | `extra` | When the mob spawns in a natural way, this many more mobs with the same genes spawn near it |
| `runt` | `chance`, `baby_health_multiplier` (`0.5`) | The mob has a chance to spawn as a baby. Baby zombies spawn only through this effect |

#### Behavior

| Type | Fields | Result |
|---|---|---|
| `leap` | `min_distance` (`3`), `max_distance` (`5`), `cooldown_seconds` (`5`), `miss_stun_seconds` (`1`), `medium` (`ground`) | The mob leaps at its target. A leap that reaches the target does a normal hit. After a missed leap, it cannot move for a short time. `medium` is `ground`, `water` for a dash through water at a target in water, or `breach` for a jump out of the water at a target on land up to 3 blocks higher |
| `water_pull` | `radius`, `strength` (`0.01`) | Pulls the players in water in the radius slowly toward the mob. Players in creative or spectator mode do not move |
| `trident_chance` | `chance` | The mob has this chance to spawn with a trident in an empty hand or in place of a fishing rod. This adds to the vanilla chance |
| `throw_rate` | `multiplier` | Multiplies how often the mob throws or shoots with a ranged attack, also with a bow. `2` throws two times as often |
| `throw_range` | `multiplier`, `target` | Multiplies the throw range at a target. `target` is `wet`, for a target in water or in a boat, `dry`, or `any` |
| `keep_distance` | `blocks` | While the mob shoots with a bow, it walks back from a target nearer than this |
| `arrow_spread` | `multiplier` | Multiplies the spread of the arrows of the mob. `0` makes each arrow fly straight |
| `scavenge_tridents` | `radius` (`2`) | The mob takes a trident that a player threw near it into its empty hand, and keeps it |
| `no_weapon_drop` | | The item in the hand of the mob does not drop when the mob dies |
| `hunt` | `invisible_radius` (`0`), `wounded_range_multiplier` (`1`) | The mob finds invisible players in the radius, and players under half health farther away |
| `frenzied_targeting` | `chance`, `interval_seconds` (`10`), `radius` (`8`) | Now and then, the mob targets the nearest mob of a different species. A zombie, husk, or drowned does not target its kin |
| `lurk` | `radius` (`6`) | The mob does not move while its target player faces it, until the player is in the radius |
| `burrow` | `player_radius` (`24`), `rise_seconds` (`2`), `blocks` (`#originofspawns:burrowable`), `night_only` (`true`), `stun_seconds` (`0`), `near_player` (`false`) | The mob can spawn 2 blocks under the ground near a player and rise. It rises only through the blocks in the tag. After it rises, it cannot move for `stun_seconds`. With `near_player`, it rises next to the nearest player |
| `sidestep` | `range` (`12`), `distance` (`4`), `cooldown_seconds` (`10`), `aimed` (`false`) | When a player looks at the mob from farther than the range, the mob moves to the side. With `aimed`, it moves when a player in the range draws a bow, a crossbow, or a trident and looks at it |
| `climb` | | The mob climbs walls, like a spider. It cannot climb glass |
| `throw_effect` | `effect`, `seconds`, `amplifier` (`0`), `range` (`8`), `cooldown_seconds` (`8`) | The mob throws at its target, which gives a status effect |
| `pick_up_items` | | The mob picks up items |
| `no_conversion` | | The mob does not convert to a different species in water |
| `siege` | `break_seconds` (`10`) | The mob breaks doors on each difficulty, and breaks the blocks in the tag `#originofspawns:siege_breakable`. It obeys the `mobGriefing` gamerule |
| `raise_victim` | | A player that the mob kills rises as a mob of its species, with its genes |
| `attract_golems` | `radius` (`16`) | Iron golems in the radius attack the mob before other targets |

#### Senses

| Type | Fields | Result |
|---|---|---|
| `silence` | `idle` (`false`), `hurt` (`false`), `steps` (`false`), `death` (`false`) | Stops these sounds of the mob |
| `loud` | `multiplier` | Multiplies the volume of the sounds of the mob, also its steps |
| `translucent` | `max_light` (`7`), `opacity` (`0.1`), `blocks` (none) | The client draws the mob translucent at this light level or lower. With `blocks`, only while its feet or its eyes are in a block of this tag. With `night_only` (`false`), only while it is dark outside. Other mobs still see it |
| `glowing` | `when` (`always`) | The mob glows while the state is true, so that players see it through walls |
| `aura` | `effect`, `amplifier` (`0`), `radius`, `interval_seconds` (`2`), `particles` (`false`), `in_water_only` (`false`), `seconds` (none) | Gives a status effect to the players near the mob. The effect lasts `seconds`, or until the next renewal when there is no `seconds`. With `in_water_only`, only the players in water get it |
| `ignored_by` | `mobs` | Mobs of these types do not target the mob |
| `see_invisible` | `radius` | The mob sees invisible entities in the radius as if they were visible |
| `sneak_blind` | `radius` | The mob does not see a sneaking entity farther than the radius |
| `lure` | `radius` (`6`), `reset_seconds` (`20`), `multiplier` (`2`) | A light hangs in front of the mob. When a player comes into the radius, the light goes out, and the next hit does more damage. The light shows again after `reset_seconds` with no player near |

#### Death

| Type | Fields | Result |
|---|---|---|
| `death_burst` | `damage`, `radius` | When the mob dies, it damages the players in the radius |
| `death_cloud` | `effect`, `seconds`, `amplifier` (`0`), `radius`, `cloud_seconds` (`5`) | When the mob dies, it leaves a cloud that gives a status effect |
| `death_effect` | `effect`, `seconds`, `amplifier` (`0`), `radius`, `when` (`always`), `allies` (`false`) | When the mob dies while the state is true, the players in the radius get a status effect. With `allies`, the mobs of its species get it instead |

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
The skeleton map, `originofspawns:textures/region/skeleton.png`, also has the
regions `ribs`, `spine`, `pelvis`, `joints`, and `eye_sockets`, and the
material `bone`.

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
| `texture` | | The texture of the cubes. It is optional when `mob_texture` is `true` |
| `mob_texture` | `false` | The cubes use the texture of the mob, with the marks of all its genes. A swollen copy of a body part keeps the marks of the other genes this way |
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

### Baby zombies

Baby zombies use a smaller model with another texture layout.
Draw the marks and the models for the adult only.
The game moves each adult face of the region map and of the marks to the same
baby face, and it moves and scales the cubes of gene models from the adult
bones to the baby bones.
A pattern smaller than 64 x 64 is a tile, and it stays as it is.
Thin marks can lose detail, because an adult limb of 12 rows becomes 5 rows.

### Genes on mobs

- A mob gets its genes one time, when it first joins a world.
- Each gene has a chance equal to its frequency in the pool of the biome
  category, or its mutation rate if that is higher.
- A mob carries a maximum of 5 genes. The genes with the highest frequency
  stay.

### Entity tags

These tags of entity types change vanilla spawns. A data pack can add a
species to them.

| Tag | Effect |
|---|---|
| `#originofspawns:no_random_equipment` | The species spawns with no random weapon, no random armor, and no random enchantments. A natural weapon, such as the bow of a skeleton, stays |
| `#originofspawns:no_item_pickup` | The species does not pick up items, unless a gene gives it pickup |
| `#originofspawns:no_vanilla_babies` | The species spawns no babies in vanilla ways. Its babies come only from a gene |
| `#originofspawns:no_vanilla_jockeys` | The species spawns with no rider in vanilla ways, for example the spider that carries a skeleton. A rider comes only from a gene |

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
