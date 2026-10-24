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
| `snow` | It snowed on the mob when it died |
| `warm` | The mob died in a biome that is not cold |
| `dry` | The mob died in a biome that is not wet |
| `shears` | Someone sheared the mob before its death, for example the mushrooms of a bogged |
| `smite` | The killer hit the mob with a weapon with the Smite enchantment |
| `day` | The mob died while it was bright outside |
| `fuse` | The mob is a creeper, and it died while its fuse burned |
| `bane` | The killer hit the mob with a weapon with the Bane of Arthropods enchantment |
| `raid` | The mob died as a part of a raid |

The mod has these pressures: `fire`, `projectile`, `melee`, `explosion`,
`fall`, `unreached`, `sunlight`, `spotted`, `alone`, `crowd`, `critical`,
`golem`, `player_death`, `in_water`, `on_land`, `cold`, `night`, `boat`,
`contact`, `trident`, `wolf`, `shield`, `snow`, `warm`, `dry`, `shears`, `smite`, `day`,
`fuse`, `bane`, and `raid`.
A creeper that explodes does not die, so its blast does not count for a
pressure. A blast that kills a player counts for `player_death`.
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
| `attribute` | `attribute`, `amount`, `operation` | Changes an attribute of the mob. `operation` is `add_value`, `add_multiplied_base`, or `add_multiplied_total`. The `scale` attribute changes the size of the mob from the next tick |
| `damage_taken` | `damage_types`, `multiplier` | Multiplies the damage from these damage types. `0` makes the mob immune |
| `damage_taken_when` | `when`, `multiplier`, `radius` (`8`), `damage_types` (all) | Multiplies the damage that the mob takes while the state is true. The states `alone` and `grouped` use the radius. With `damage_types`, only the damage of this tag counts |
| `damage_dealt_when` | `when`, `multiplier`, `players_only` (`false`), `hits` (`any`) | Multiplies the damage that the mob deals while the state is true. With `players_only`, only the damage to players. `hits` is `melee`, `projectile`, or `any` |
| `damage_from` | `attackers`, `multiplier` | Multiplies the damage from these entity types, for example `minecraft:iron_golem` |
| `damage_from_item` | `items`, `multiplier` | Multiplies the melee damage from an attacker that holds an item of this tag, for example `#minecraft:pickaxes` |
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
| `on_fire` | The mob burns |
| `target_far` | The target of the mob is farther than 11 blocks |
| `falling` | The mob is in the air, out of water, and it fell more than 2 blocks |
| `sees_player` | The target of the mob is a player, and the mob sees the player |
| `in_rain` | Rain falls on the mob: the world has rain, the biome has rain and not snow, and nothing is above the mob. `storm` is different: it is true in a thunderstorm, also under a roof |
| `target_unreachable` | The mob has a target, and it finds no path that reaches it |
| `wet` | The mob is in water, or rain falls on it |
| `carrying_block` | The mob is an enderman that carries a block |
| `sunlit` | The mob is out of water and rain, in the daylight, under the open sky |
| `target_below` | The target of the mob is 2 or more blocks lower than the mob |
| `target_above` | The target of the mob is 2 or more blocks higher than the mob |
| `wounded` | The health of the mob is under half of its maximum |
| `snowing` | It snows on the mob: a storm in a biome where the storm is snow, with open sky above the mob |
| `not_snowing` | It does not snow on the mob |
| `on_snow` | The mob stands on snow or ice, or in a snow layer or powder snow |
| `off_snow` | The mob does not stand on snow or ice |
| `on_ice` | The mob stands on ice |
| `cold_biome` | The mob is in a biome that is cold enough for snow |
| `night` | It is dark outside |
| `target_frozen` | The target of the mob freezes, as in powder snow |
| `wet_ground` | The mob is in water, or stands on a block in the tag `#originofspawns:mud` |
| `dry_ground` | The mob is not in water and does not stand on mud |
| `on_rot_ground` | The mob stands on a block in the tag `#originofspawns:rot_ground`: mud, moss, or mycelium |
| `target_in_water` | The target of the mob is in water |
| `in_lava` | The feet of the mob are in lava |
| `out_of_lava` | The feet of the mob are not in lava |
| `in_fire` | The feet of the mob are in fire, soul fire, or lava |
| `on_soul_ground` | The mob stands on soul sand or soul soil, the blocks in the tag `#minecraft:soul_speed_blocks` |
| `off_soul_ground` | The mob does not stand on soul sand or soul soil |
| `day` | It is bright outside |
| `healthy` | The health of the mob is half of its maximum or more |
| `hunting_healthy` | The mob targets a player with half health or more |
| `target_weakened` | The target of the mob has the Weakness effect |
| `charged` | The mob is a charged creeper |
| `unseen` | The mob is behind the player, or the player did not look at it in the last 3 seconds. In `damage_dealt_when`, it asks this of each player that the mob damages. In other effects, it asks it of the target |
| `near_spawner` | A monster spawner is in 16 blocks of the mob |
| `far_from_spawner` | No monster spawner is in 16 blocks of the mob |

#### Hits

| Type | Fields | Result |
|---|---|---|
| `ignite_target` | `chance`, `seconds`, `hits` (`melee`) | Each hit has this chance to set the target on fire |
| `pierce_shields` | `blasts` (`false`) | The projectiles of the mob go through shields, with their full damage. With `blasts`, the blast of a creeper also goes through shields, and the shield takes no damage |
| `effect_on_hit` | `effect`, `seconds`, `amplifier` (`0`), `chance` (`1`), `when` (`always`), `hits` (`melee`) | Each hit gives the target a status effect, while the state is true. `hits` is `melee`, `projectile`, or `any`. A thrown trident is a projectile. A projectile that a shield blocks gives no effect |
| `pull_down_on_hit` | `blocks` | Each hit on a player in water pulls the player down by up to this many blocks. A block stops the pull |
| `drag_under` | `seconds`, `sink_speed` (`0.12`) | When the mob hits a player in water, it holds on, and both sink. The player breaks free with a hit on the mob, or when the player leaves the water |
| `break_boats` | `hits` (`3`) | The hits on a boat or on its rider break the boat after this many hits. The riders fall out |
| `lightning_on_hit` | `storm_only` (`true`) | A projectile hit calls lightning on a target under the open sky. With `storm_only`, only in a thunderstorm |
| `drain_food` | `amount`, `heal` (`0`), `chance` (`1`), `hits` (`melee`) | Each hit of the kind in `hits` has the chance to take food points from a player, and heals the mob |
| `leech_xp` | `amount`, `hits` (`melee`) | Each hit of the kind in `hits` takes XP points from a player. The mob drops the XP that it took when it dies, and no other XP |
| `rally` | `per_ally`, `max`, `radius` (`6`) | Adds damage for each mob of the same species in the radius, up to `max`. `0.1` is 10% |
| `convert_villagers` | | A villager that the mob kills always becomes a zombie villager |

#### Groups

| Type | Fields | Result |
|---|---|---|
| `effect_on_hurt` | `effect`, `seconds`, `amplifier` (`0`), `cooldown_seconds` (`0`), `ink` (`false`), `ally_radius` (`0`), `player_radius` (`0`), `hits` (`any`) | When an attacker hurts the mob with a hit of the kind in `hits`, the attacker gets a status effect, after each cooldown. `ink` also sprays a cloud of ink. With an `ally_radius` above 0, the mobs of its species in the radius get the effect, and the attacker does not. With a `player_radius` above 0, the players in the radius get the effect, and the attacker does not |
| `barrage` | `group` (`3`), `radius` (`16`) | When the mob shoots at a player, and this many skeletons of its species in the radius target that player and see the player, they get the same wait to the next shot. Then they all shoot at the same time |
| `call_on_hurt` | `radius`, `smoke` (`false`), `cooldown_seconds` (`0`) | When the mob is hurt, the mobs of its species in the radius attack the attacker. `smoke` also makes a column of smoke. The call waits for its cooldown |
| `share_damage` | `fraction`, `radius` (`8`) | A part of the damage that the mob takes goes to the mobs of its species in the radius |
| `share_healing` | `radius` (`6`) | When the mob heals, the mobs of its species in the radius heal by the same amount |
| `call_on_hit` | `radius`, `hits` (`melee`) | When the mob hits a player, the mobs of its species in the radius target that player |
| `call_on_sight` | `radius`, `cooldown_seconds` (`30`) | When the mob first targets a player, the mobs of its species in the radius target that player |
| `grave_call` | `chance`, `max_calls` (`2`), `radius` (`8`) | When the mob is hurt, it has a chance to call a new mob of its species, with no genes |
| `jockey` | `chance`, `mount` | When the mob spawns in a natural way, it has this chance to ride a new mob of the mount type |
| `brood` | `extra` | When the mob spawns in a natural way, this many more mobs with the same genes spawn near it |
| `runt` | `chance`, `baby_health_multiplier` (`0.5`) | The mob has a chance to spawn as a baby. Baby zombies spawn only through this effect |

#### Behavior

| Type | Fields | Result |
|---|---|---|
| `leap` | `min_distance` (`3`), `max_distance` (`5`), `cooldown_seconds` (`5`), `miss_stun_seconds` (`1`), `medium` (`ground`), `when` (`always`) | The mob leaps at its target while the state is true. A leap that reaches the target does a normal hit. After a missed leap, it cannot move for a short time. `medium` is `ground`, `water` for a dash through water at a target in water, or `breach` for a jump out of the water at a target on land up to 3 blocks higher |
| `water_pull` | `radius`, `strength` (`0.01`) | Pulls the players in water in the radius slowly toward the mob. Players in creative or spectator mode do not move |
| `trident_chance` | `chance` | The mob has this chance to spawn with a trident in an empty hand or in place of a fishing rod. This adds to the vanilla chance |
| `throw_rate` | `multiplier`, `when` (`always`) | While the state is true, multiplies how often the mob throws or shoots with a ranged attack, also with a bow. `2` throws two times as often. With a bow, it changes the draw and the wait together |
| `draw_time` | `multiplier` | Multiplies the time that the mob draws its bow, or loads its crossbow. `1.25` draws 25% slower |
| `throw_range` | `multiplier`, `target`, `when` (`always`) | While the state is true, multiplies the throw range at a target. A range past the follow range of the mob needs more follow range too. `target` is `wet`, for a target in water or in a boat, `dry`, or `any` |
| `keep_distance` | `blocks` | While the mob shoots with a bow, it walks back from a target nearer than this |
| `arrow_spread` | `multiplier` | Multiplies the spread of the arrows of the mob. `0` makes each arrow fly straight |
| `scavenge_tridents` | `radius` (`2`) | The mob takes a trident that a player threw near it into its empty hand, and keeps it |
| `no_weapon_drop` | | The item in the hand of the mob does not drop when the mob dies |
| `hunt` | `invisible_radius` (`0`), `wounded_range_multiplier` (`1`) | The mob finds invisible players in the radius, and players under half health farther away |
| `frenzied_targeting` | `chance`, `interval_seconds` (`10`), `radius` (`8`) | Now and then, the mob targets the nearest mob of a different species. A zombie, husk, or drowned does not target its kin |
| `lurk` | `radius` (`6`), `ignite` (`false`) | The mob does not move while its target player faces it, until the player is in the radius. With `ignite`, a creeper lights its fuse when its target is in the radius and in sight |
| `burrow` | `player_radius` (`24`), `rise_seconds` (`2`), `blocks` (`#originofspawns:burrowable`), `night_only` (`true`), `stun_seconds` (`0`), `near_player` (`false`) | The mob can spawn 2 blocks under the ground near a player and rise. It rises only through the blocks in the tag. After it rises, it cannot move for `stun_seconds`. With `near_player`, it rises next to the nearest player |
| `sidestep` | `range` (`12`), `distance` (`4`), `cooldown_seconds` (`10`), `aimed` (`false`) | When a player looks at the mob from farther than the range, the mob moves to the side. With `aimed`, it moves when a player in the range draws a bow, a crossbow, or a trident and looks at it |
| `climb` | | The mob climbs walls, like a spider. It cannot climb glass |
| `throw_effect` | `effect`, `seconds`, `amplifier` (`0`), `min_range` (`0`), `range` (`8`), `cooldown_seconds` (`8`) | The mob throws at its target, which gives a status effect. The target must be between `min_range` and `range` |
| `pick_up_items` | | The mob picks up items. A raider with no inventory of its own, such as a pillager or a vindicator, keeps them in a sack of 5 slots, and drops them when it dies. A vindicator keeps its axe |
| `no_conversion` | | The mob does not convert to a different species in water |
| `siege` | `break_seconds` (`10`), `door_seconds` (`12`), `also_breaks` (none) | The mob breaks doors on each difficulty, and breaks the blocks in the tag `#originofspawns:siege_breakable`. A block takes `break_seconds`, and a door takes `door_seconds`. The vanilla door time is 12 seconds. `also_breaks` is a block tag that the mob also breaks, for example `#originofspawns:shatterable`. It obeys the `mobGriefing` gamerule |
| `raise_victim` | | A player that the mob kills rises as a mob of its species, with its genes |
| `attract_golems` | `radius` (`16`) | Iron golems in the radius attack the mob before other targets |

#### Senses

| Type | Fields | Result |
|---|---|---|
| `silence` | `idle` (`false`), `hurt` (`false`), `steps` (`false`), `death` (`false`) | Stops these sounds of the mob |
| `loud` | `multiplier` | Multiplies the volume of the sounds of the mob, also its steps and the fuse of a creeper |
| `translucent` | `max_light` (`7`), `opacity` (`0.1`), `blocks` (none), `night_only` (`false`), `when` (`always`), `also` (`always`) | The client draws the mob translucent at this light level or lower. With `blocks`, only while its feet or its eyes are in a block of this tag. With `night_only` (`false`), only while it is dark outside. With `when` (`always`) and `also` (`always`), only while both states are true. Other mobs still see it |
| `glowing` | `when` (`always`) | The mob glows while the state is true, so that players see it through walls |
| `aura` | `effect`, `amplifier` (`0`), `radius`, `interval_seconds` (`2`), `particles` (`false`), `in_water_only` (`false`), `seconds` (none), `allies` (`false`), `when` (`always`) | While the state is true, gives a status effect to the players near the mob. The effect lasts `seconds`, or until the next renewal when there is no `seconds`. With `in_water_only`, only the players in water get it. With `allies`, the mobs of its species get it instead of the players |
| `ignored_by` | `mobs` | Mobs of these types do not target the mob |
| `see_invisible` | `radius` | The mob sees invisible entities in the radius as if they were visible |
| `sneak_blind` | `radius` | The mob does not see a sneaking entity farther than the radius |
| `lure` | `radius` (`6`), `reset_seconds` (`20`), `multiplier` (`2`) | A light hangs in front of the mob. When a player comes into the radius, the light goes out, and the next hit does more damage. The light shows again after `reset_seconds` with no player near |

#### Death

| Type | Fields | Result |
|---|---|---|
| `death_burst` | `damage`, `radius` | When the mob dies, it damages the players in the radius |
| `death_cloud` | `effect`, `seconds`, `amplifier` (`0`), `radius`, `cloud_seconds` (`5`) | When the mob dies, it leaves a cloud that gives a status effect |
| `freeze_on_hit` | `seconds`, `chance` (`1`), `hits` (`melee`) | Each hit freezes the target as powder snow does. The freeze stays full for `seconds`. Leather armor stops it |
| `freeze_attacker` | `seconds` | An entity that hits the mob in melee freezes for `seconds` |
| `freeze_aura` | `radius`, `rate` (`60`), `when` (`always`) | While the state is true, the players in the radius start to freeze. The freeze grows each second by `rate` ticks. The game takes away 40 ticks each second, so a `rate` above 40 makes it grow. It stops 1 second past full |
| `fan_shot` | `arrows` (`3`), `angle` (`15`), `cooldown_seconds` (`10`), `when` (`always`) | When the mob shoots its bow while the state is true, it also shoots more arrows in a fan, one time in each cooldown. `arrows` includes the normal shot |
| `arrow_knockback` | `multiplier` | The projectiles of the mob push the target back more. `2` pushes two times as far |
| `walk_on_powder_snow` | | The mob walks on powder snow and does not sink, as with leather boots |
| `arrow_effect` | `effect`, `seconds`, `amplifier` (`0`) | The arrows of the mob carry a status effect, as a tipped arrow does. A stronger effect of the same kind replaces a weaker one |
| `death_block` | `block` | When the mob dies, the block goes where it died. It goes only in air, and not where a player stands |
| `armor_break` | `multiplier`, `frozen_only` (`false`), `hits` (`projectile`) | The hits of the mob of the kind in `hits` damage the armor of a player more. `2` damages it two times as much. With `frozen_only`, only on a player that freezes |
| `gaze_effect` | `effect`, `seconds`, `amplifier` (`0`), `range` (`16`), `interval_seconds` (`2`) | A player in the range that looks at the mob gets the status effect, as the enderman checks a look, with a clear line of sight |
| `call_on_death` | `radius` | When the mob dies, the mobs of its species in the radius target the player that killed it |
| `heal_on_ally_death` | `amount`, `radius` | The mob heals by the amount when another mob of its species dies in the radius |
| `hidden_from_afar` | `blocks`, `when` (`always`) | While the state is true, players farther than `blocks` do not see the mob. It is not the invisibility effect |
| `ghost_arrows` | `blocks` | Players farther than `blocks` do not see the arrows of the mob while they fly |
| `heal_on_hit` | `amount`, `hits` (`melee`), `when` (`always`) | Each hit of the mob heals it by the amount, while the state is true |
| `regrow` | `seconds`, `heal` (`0`) | After a shear, the mushrooms of a bogged grow back after the seconds, and it heals by `heal` |
| `parry` | `cooldown_seconds` (`6`) | The mob takes no damage from one melee hit, one time in each cooldown. A shield sound plays |
| `cleave` | `part` (`0.5`), `radius` (`2`) | Each melee hit also hurts the other players in the radius of the target, with this part of the attack damage of the mob. A cleave does not cleave again |
| `skull_shot` | `cooldown_seconds` (`10`), `min_range` (`6`), `max_range` (`16`) | One time in each cooldown, the mob shoots a wither skull at its target player in the range, if it can see the player |
| `death_spawn` | `entity`, `count` (`1`), `life_seconds` (`30`) | When the mob dies, this many mobs of this type come out and attack the killer. A vex lives for `life_seconds` |
| `soul_hunger` | `per_player`, `max`, `seconds` (`10`) | The mob does more melee damage for each player that it hit in melee in the last `seconds`, up to `max`. `0.2` is 20% for each player |
| `damage_to` | `targets`, `multiplier` (`1`), `fire_seconds` (`0`) | The hits of the mob do more damage to entities of the tag in `targets`, and set them on fire for `fire_seconds` |
| `knockback` | `multiplier`, `hits` (`melee`) | The hits of the mob push the target back more. `2` pushes two times as far |
| `snowball_damage` | `damage` | Snowballs do this damage to the mob, as they do to a blaze |
| `water_sensitive` | | Water hurts the mob, as it hurts a blaze or an enderman: splash water, rain, and water |
| `disarm` | `chance`, `cooldown_seconds` (`10`), `hits` (`projectile`) | Each hit has the chance to make a player drop the item in the main hand. A player loses an item one time in each cooldown at most. An item with Curse of Binding does not drop |
| `death_effect` | `effect`, `seconds`, `amplifier` (`0`), `radius`, `when` (`always`), `allies` (`false`) | When the mob dies while the state is true, the players in the radius get a status effect. With `allies`, the mobs of its species get it instead |

#### Pillagers

The bow and arrow effects of the skeletons also act on the crossbow of a
pillager: `draw_time`, `throw_rate`, `throw_range`, `arrow_spread`,
`arrow_effect`, `arrow_knockback`, `ghost_arrows`, and `pierce_shields`. A bolt
that hits counts as a `projectile` hit. `throw_rate` divides both the load and
the wait after the load.

| Type | Fields | Result |
|---|---|---|
| `rocket_chance` | `chance` | Each crossbow shot has this chance to be a firework rocket with one red star. The rocket breaks no blocks |
| `rocket_shot` | `cooldown_seconds` (`10`), `range` (`16`) | After each cooldown, the mob shoots a firework rocket at a target in the range |
| `rocket_blast` | `multiplier` | Multiplies the blast radius of the rockets of the mob. The vanilla radius is 5 |
| `patrol_size` | `extra` (`1`) | A patrol that this mob spawns in gets this many more pillagers. A raid does not change |

#### Evokers

The fang effects change the evoker fangs of the attack spell. The vex
effects change the vexes that the mob summons with its vex spell or with
`decoy`. The vexes of `death_spawn` have no owner, so the vex effects do not
change them. A vex has no genes of its own.

| Type | Fields | Result |
|---|---|---|
| `fang_damage` | `multiplier`, `targets` (none) | Multiplies the damage of the fangs of the mob. The fangs do 6 damage in vanilla. With `targets`, only the damage to the entities of this tag changes |
| `fang_effect` | `effect`, `seconds`, `amplifier` (`0`), `chance` (`1`) | Gives the status effect to each target that the fangs hurt |
| `fang_launch` | `blocks` (`1`) | Each fang hit throws the target this many blocks up |
| `fang_heal` | `amount` | Each fang hit heals the mob this much health |
| `spell_interval` | `multiplier` | Multiplies the time between the fang spell (vanilla 5 seconds) and the vex spell (vanilla 17 seconds). `0.7` is 30% less time. The sheep spell does not change. More than one effect multiplies together |
| `fang_line` | `fangs` | The number of fangs in the line. Vanilla has 16 fangs, one each 1.25 blocks. With more than one effect, the largest number counts |
| `fang_ring` | `range` | The mob casts the ring of fangs in place of the line when its target is in this range. Vanilla is 3 blocks. With more than one effect, the largest range counts |
| `vex_count` | `count` | The number of vexes of the vex spell. Vanilla is 3. With more than one effect, the largest number counts |
| `vex_health` | `multiplier` | Multiplies the maximum health of the vexes of the mob. A vex has 14 health |
| `vex_damage` | `multiplier` | Multiplies the attack damage of the vexes of the mob |
| `vex_life` | `multiplier` | Multiplies the life of the vexes of the mob. A vex lives 30 to 119 seconds in vanilla |
| `vex_effect` | `effect`, `seconds`, `amplifier` (`0`), `chance` (`1`) | Gives the status effect to each target that a vex of the mob hits |
| `vex_heal` | `amount` | Each hit of a vex of the mob heals the mob this much health |
| `hex` | `effect`, `seconds`, `amplifier` (`0`), `range` (`16`), `cooldown_seconds` (`15`) | One time in each cooldown, the mob gives its target the effect, if the target is in the range and the mob sees it. It throws no item. The mob and the target show the spell particles of the evoker, in the color of the effect. Each hex has its own cooldown |
| `decoy` | `count` (`1`), `cooldown_seconds` (`20`) | When an attacker hurts the mob, the mob summons this many vexes that attack the attacker, one time in each cooldown |

#### Witches

"Thrown potions" are the potions that a witch throws at a target. The
potions that heal a raider in a raid do not change. `throw_rate` changes the
time between two throws (vanilla 3 seconds), `throw_range` multiplies the
range of the throw (vanilla 10 blocks), and `arrow_spread` multiplies the
spread of the thrown potions.

| Type | Fields | Result |
|---|---|---|
| `potion_duration` | `multiplier` | Multiplies the time of the effects of the thrown potions. An effect with no time, such as Harming or Healing, does not change. More than one effect multiplies together. It also changes the effect of `potion_extra` |
| `potion_level` | `levels` (`1`) | Makes the effects of the thrown potions this many levels stronger. An effect with no time, such as Harming, does not change |
| `potion_swap` | `from`, `to`, `seconds` (none) | The effect `to` takes the place of the effect `from`. It keeps the time and the level, or it lasts `seconds` |
| `potion_extra` | `with`, `effect`, `seconds`, `amplifier` (`0`) | A thrown potion with the effect `with` also gives `effect` |
| `potion_lingering` | | The thrown potions are lingering potions. They leave a cloud, as the lingering potion of a player does |
| `potion_count` | `count` (`2`), `spread_degrees` (`8`) | The mob throws this many potions in one throw, this many degrees apart. With more than one effect, the largest number counts |
| `potion_splash` | `multiplier` | Multiplies the radius of the splash (vanilla 4 blocks) and the start radius of a lingering cloud (vanilla 3 blocks) |
| `potion_damage` | `multiplier`, `targets` (none) | Multiplies the damage of the potions of the mob, for example Harming. With `targets`, only the damage to the entities of this tag changes |
| `potion_shards` | `damage` | A thrown potion that breaks on a living entity does this much damage to it |
| `potion_heal` | `amount` | A thrown potion that breaks on a living entity heals the mob this much health |
| `drink` | `potion`, `when`, `cooldown_seconds` (`0`), `seconds` (none) | The mob drinks the potion while the state is true, one time in each cooldown. It does not drink when it already has each lasting effect of the potion. A drink takes the vanilla time, and the mob is slow while it drinks. With `seconds`, the effects last this long. `potion` is a potion id, such as `minecraft:strong_swiftness` |
| `shared_drink` | `radius` (`4`) | When the mob ends a drink, the monsters in the radius get the same effects. Each drink counts, also a vanilla drink. A monster is a hostile mob |
| `heal_throw` | `radius` (`8`), `cooldown_seconds` (`15`) | One time in each cooldown, the mob throws a splash potion of Healing at the nearest hurt monster in the radius that it sees. It does not throw at its own target. An undead monster gets Harming, which heals it. The vanilla raid heal stays |

#### Endermen

A teleport goes to a free place: the mob fits there, on a block that it can
stand on, out of liquid. If a teleport finds no free place, it does not
teleport, and its cooldown does not start. The teleports work on each mob.

| Type | Fields | Result |
|---|---|---|
| `teleport_to_target` | `cooldown_seconds` (`6`), `when` (`always`), `min_distance` (`4`) | One time in each cooldown, while the state is true, the mob teleports next to its target, 1.5 or 2.5 blocks from it. Only when the target is farther than `min_distance` |
| `teleport_behind` | `cooldown_seconds` (`10`) | After a melee hit, the mob teleports behind its target, one time in each cooldown |
| `escape_teleport` | `threshold` (`0.25`), `heal` (`10`), `range` (`16`) | One time in the life of the mob, under this part of its maximum health, it teleports to a random place in the range and heals |
| `blast_teleport` | `cooldown_seconds` (`20`), `range` (`16`) | When an explosion hurts the mob, it teleports to a random place in the range and takes no damage, one time in each cooldown |
| `aimed_teleport` | `when` (`always`) | While the state is true, each random teleport of an enderman goes next to its target |
| `projectile_teleport` | `cooldown_seconds` (`8`), `range` (`16`) | When a projectile hurts the mob, it teleports to a random place in the range, one time in each cooldown |
| `teleport_strike` | `multiplier`, `seconds` (`5`) | The first melee hit in `seconds` after a teleport does more damage. The vanilla teleports of an enderman also count |
| `teleport_target_on_hit` | `chance`, `range` (`8`) | Each melee hit has this chance to teleport the target to a random place in the range, as a chorus fruit does |
| `stare_effect` | `effect`, `seconds`, `amplifier` (`0`) | A player that stares at the face of the mob gets the effect. The stare is the vanilla stare test of the enderman. A carved pumpkin stops it |
| `stare_range` | `multiplier` | Multiplies the stare range, which is the follow range of the mob, 64 blocks for an enderman. It changes the anger and `stare_effect` |
| `stare_through_pumpkin` | | A carved pumpkin does not stop the stare |
| `block_throw` | `damage` (`4`), `min_range` (`4`), `range` (`12`), `cooldown_seconds` (`8`) | One time in each cooldown, an enderman throws its carried block at its target in the range, if it sees it. The block hurts the first entity that it hits. Where it lands, it puts the block down if `mobGriefing` is on, else it drops nothing. `throw_rate` changes the cooldown |
| `thrown_block_damage` | `multiplier` | Multiplies the damage of the thrown block |
| `thrown_block_effect` | `effect`, `seconds`, `amplifier` (`0`), `when` (`always`) | The thrown block gives the effect to the entity that it hits, if the state of the thrower is true at the hit |
| `thrown_block_shield` | `seconds` | A shield that stops the thrown block is useless for this many seconds |
| `pickup_blocks` | `blocks` | An enderman can also pick up the blocks of this tag. It obeys `mobGriefing` |
| `water_damage_taken` | `multiplier`, `splash_only` (`false`) | Multiplies the damage that water does to a mob that water hurts: water, rain, and a splash potion of water. With `splash_only`, only the splash potion. `water_damage` is different: it makes water hurt a mob |
| `pearl_chance` | `chance` | A thrown ender pearl makes an endermite with this gene with this chance, not 5% |
| `lifespan` | `seconds` (none), `multiplier` (`1`), `endless` (`false`) | Sets the life of an endermite. Vanilla is 2 minutes. With `seconds`, it lives this long, else the multiplier changes the vanilla life. With `endless`, it does not die of age. The longest life counts |
| `life_on_hit` | `seconds` | Each melee hit of an endermite adds this many seconds to its life |
| `age_spawn` | `count` (`2`) | When an endermite dies of age, this many endermites come out, with genes from the pool |
| `call_others` | `mobs`, `radius` (`16`) | When the mob hits a player, the mobs of this list or tag in the radius that have no target attack the player |

`brood` has the option `pool_genes` (`false`). With it, the extra mobs get
genes from the pool, as new mobs. A brood and a burrow also start at an
event, such as an endermite from an ender pearl.

#### Spiders

These effects suit a spider, but other mobs can also use them. A gene cobweb
does not replace a block. It goes away after its time and drops no string,
also after a restart of the server.

| Type | Fields | Result |
|---|---|---|
| `web_on_hit` | `chance`, `seconds` (`30`), `hits` (`melee`) | Each hit has this chance to put a cobweb at the target |
| `web_trail` | `interval_seconds` (`10`), `seconds` (`30`) | While the mob walks, it leaves a cobweb behind it after each interval |
| `web_shot` | `min_range` (`4`), `range` (`12`), `cooldown_seconds` (`8`), `seconds` (`30`) | The mob spits silk at a player between the two ranges. The silk makes a cobweb where it lands |
| `leap_strike` | `multiplier`, `seconds` (`2`) | The first hit in this time after a leap does more damage. A leap is the vanilla leap of a spider or a wolf, or a gene leap |
| `first_strike` | `multiplier`, `reset_seconds` (`10`) | The first hit of the mob on a player does more damage. It counts again after this time with no hit on that player |
| `hunt_in_light` | | A spider attacks players in bright light too |
| `bite_poison` | `duration_multiplier` (`1`), `extra_levels` (`0`) | Changes the Poison of the vanilla bite of a cave spider: a longer time, and more levels. On Easy the bite gives no Poison, so this does nothing |

#### Creepers

These effects act only on a creeper. The vanilla blast has a radius of 3, or
6 for a charged creeper. The vanilla fuse burns for 30 ticks. A gene fuse
burns for 3 ticks or more.

The creeper causes its own blast. So `damage_dealt_when` and `damage_to`
change the blast damage, and the `hits` field counts a blast as `melee`.

| Type | Fields | Result |
|---|---|---|
| `blast_size` | `multiplier`, `when` (`always`) | Multiplies the radius of the blast while the state is true. It also multiplies the radius of a charged creeper |
| `fuse_time` | `multiplier` | Multiplies the time that the fuse burns |
| `blast_cloud` | `effect`, `seconds`, `amplifier` (`0`), `effect_seconds` (the value of `seconds`) | The blast leaves a cloud of the status effect, with the radius of the blast. The cloud stays for `seconds`. It comes in addition to the vanilla cloud of a creeper with status effects |
| `blast_effect` | `effect`, `seconds`, `amplifier` (`0`) | Each living entity that the blast damages gets the status effect. A shield that stops the blast also stops the effect. Use one entry for each effect |
| `chain_reaction` | `radius` (`5`) | The blast lights the fuses of the creepers in the radius whose fuses do not burn yet. A creeper that a chain lit does not light more creepers |
| `aftershock` | `seconds` (`2`), `fraction` (`0.5`) | After the time, a second blast goes off at the same place, with this part of the radius. It makes no more aftershocks, clouds, chains, or lightning. The damage genes and `blast_effect` of the creeper still act on it. A save and quit before the second blast loses it |
| `silent_fuse` | | The fuse makes no sound, also when flint and steel or a fire charge lights it. Sculk sensors still detect it |
| `charged_spawn` | `chance` | The creeper has this chance to spawn charged |
| `charge_when` | `when` (`storm`) | The creeper becomes charged when the state is true, and it stays charged |
| `blast_lightning` | | Lightning strikes the place of the blast. This lightning does not charge creepers |
| `lightning_immunity` | | A lightning strike does no damage to the mob and sets no fire on it. A creeper still becomes charged. A fire block from the lightning can still burn it |

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
| `layers` | | The changes to the texture, in order. A layer with no `regions` or no `materials` covers all of them. A layer can have an `order`, an integer with the default `0`. A layer can have a `target`: `body` (the default) or `outer`. An `outer` layer goes on the outer layer of the mob, such as the cloth of the stray, and covers all of its pixels |
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
| `erase` | `texture` (none) | Makes the pixels transparent. With a texture, it erases only the pixels where the tiled texture has an alpha above 0. A 64 x 64 texture moves to the baby and split layouts, as a pattern does |

### Gene models

A gene model adds cubes to the model of a mob.
The file `assets/<namespace>/gene_models/<name>.json` is the model
`<namespace>:<name>`.
Each cube moves with one bone: `head`, `body`, `right_arm`, `left_arm`,
`right_leg`, or `left_leg`. A creeper has `head`, `body`, `right_hind_leg`,
`left_hind_leg`, `right_front_leg`, and `left_front_leg`. A spider has `head`,
`body` (the front part), `abdomen`, and the eight legs: `right_hind_leg`,
`left_hind_leg`, `right_middle_hind_leg`, `left_middle_hind_leg`,
`right_middle_front_leg`, `left_middle_front_leg`, `right_front_leg`, and
`left_front_leg`. A cave spider has the same bones as the spider.
A pillager, a vindicator, and an evoker have the humanoid bones. The crossed
arms of a vindicator or an evoker are a different part, so a model on an arm
shows only while the arms are apart, when it attacks or casts a spell.
A witch has `head`, `body`, `arms`, `right_leg`, `left_leg`, `hat`, and
`nose`. The arms are one part, and they are always crossed. The `hat` is the
flat brim of the hat. It is a part of the head, so a cube on it moves with
the head. The cube is in the coordinates of the brim, which start at the
corner of the brim, 5 pixels left, 10 pixels up, and 5 pixels to the front
of the head pivot. The higher parts of the hat do not have a bone. The
`nose` is also a part of the head. The gene marks of a witch are 64 x 128,
the size of the witch texture.
An enderman has the humanoid bones. The vanilla eyes layer draws over its
eyes, so a gene that changes the eyes uses a gene model with `glow`. An
endermite has `segment0` (the head), `segment1` (the body), `segment2` (the
tail), and `segment3` (the tip of the tail).
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

These tags are for the fields of effects, such as the `targets` of
`damage_to`.

| Tag | Contents |
|---|---|
| `#originofspawns:golems` | The iron golem |
| `#originofspawns:all_golems` | The iron golem and the snow golem |
| `#originofspawns:villagers` | The villager and the wandering trader |
| `#originofspawns:siege_breakable` (block) | Glass blocks and glass panes, the weak blocks that `siege` breaks |
| `#originofspawns:shatterable` (block) | Fences, fence gates, glass blocks, and glass panes |

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
