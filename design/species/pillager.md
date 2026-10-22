# Pillager

This document defines the families and the genes of the pillager.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The pillager is an illager with a crossbow. It has 24 health. It spawns in
patrols and at outposts, and it comes in raids. A patrol has a captain with
an ominous banner, and a player that kills a captain gets Bad Omen. In a raid,
its crossbow can have Quick Charge, Multishot, or Piercing. Its genes fit the
aim of the crossbow, the fire of the rocket, the command of the captain, the
speed of the scout, and the greed of the looter. It shares no genes and no
families with the other species.

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

### Crossbowmen

Crossbowmen hit hard with the crossbow. They load it slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more bolt damage | It shoots 10% less often |
| Tier 2 | 20% more bolt damage | It shoots 15% less often |
| Tier 1 | 15% more bolt damage | It shoots 20% less often |

### Bombardiers

Bombardiers shoot firework rockets. Blasts hurt them too.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 15% chance that a shot is a firework rocket | 25% more explosion damage |
| Tier 2 | 10% chance that a shot is a firework rocket | 50% more explosion damage |
| Tier 1 | 5% chance that a shot is a firework rocket | 75% more explosion damage |

### Captains

Captains lead the patrol. They keep themselves back, so their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Pillagers in 12 blocks get Resistance I | 10% less maximum health |
| Tier 2 | Pillagers in 8 blocks get Resistance I | 20% less maximum health |
| Tier 1 | Pillagers in 4 blocks get Resistance I | 25% less maximum health |

### Scouts

Scouts run ahead of the patrol. They carry light bolts.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 25% more speed | 10% less bolt damage |
| Tier 2 | 20% more speed | 15% less bolt damage |
| Tier 1 | 15% more speed | 20% less bolt damage |

### Looters

Looters take what they can from the player. They run from a fair fight.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit takes 2 food points from a player | 10% less damage while its health is over half |
| Tier 2 | Each hit takes 1 food point from a player | 15% less damage while its health is over half |
| Tier 1 | Each hit has a 50% chance to take 1 food point from a player | 20% less damage while its health is over half |

## Pressures

The pillager uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `night`, `day`, `wolf`, and `shield`.
The sun does not burn it, so it does not use `sunlight`. It also uses this
pressure.

| Pressure | A pillager death counts when | State |
|---|---|---|
| `raid` | The pillager dies as a part of a raid | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Crossbowman genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Piercer | Crossbowman | `shield` | Its bolts go through shields | 10% less maximum health | Arms: a bracer of steel on the crossbow arm. This changes the model |
| Long Bolt | Crossbowman | `unreached` | 50% more crossbow range | 10% less speed | Back: a quiver of long bolts. This changes the model |
| Heavy Bolt | Crossbowman, sub Looter | `melee` | Its bolts knock the target back two times as far | It shoots 10% less often | Arms: thick leather wraps |
| Overdraw | Crossbowman | `critical` | 25% more bolt damage | It shoots 15% less often | Head: a scar over one eye |
| Quick Charge | Crossbowman | `spotted` | It loads its crossbow 30% faster | Its bolts spread two times as much | Hands: worn, dark gloves |
| Splinter | Crossbowman | `projectile` | Its bolts give Slowness for 2 seconds | 10% less bolt damage | Body: a belt of jagged bolts |

### Bombardier genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rocketeer | Bombardier | `crowd` | Each 10 seconds, it shoots a firework rocket | 25% more explosion damage | Back: a bundle of rockets. This changes the model |
| Flak | Bombardier, sub Captain | `explosion` | Its firework rockets have 50% larger blasts | It shoots 10% less often | Body: a red sash with stars |
| Incendiary | Bombardier | `fire` | Its bolts set the target on fire for 4 seconds | 25% more fire damage | Arms: scorched sleeves |
| Smoke Shell | Bombardier | `spotted` | Its bolts give Blindness for 2 seconds | 10% less maximum health | Head: soot on the face |
| Powder Keg | Bombardier | `critical` | When it dies, it does 4 damage to the players in 3 blocks | 4 less maximum health | Body: a small keg on the belt. This changes the model |
| Blast Proof | Bombardier | `golem` | 50% less explosion damage | 10% less speed | Body: a padded vest |

### Captain genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Banner Bearer | Captain | `alone` | Pillagers in 8 blocks get Speed | 10% less maximum health | Back: a small ominous banner. This changes the model |
| War Horn | Captain | `spotted` | When it sees a player, it calls the pillagers in 32 blocks | Players hear it from two times farther | Body: a horn on a cord. This changes the model |
| Rally Cry | Captain | `crowd` | 5% more damage for each pillager in 8 blocks, up to 20% | 2 less armor | Head: a red plume |
| Patrol Leader | Captain | `alone` | Its patrols have 1 more pillager | Each pillager of the patrol has 15% less maximum health | Head: a dark hood |
| Grudge | Captain, sub Scout | `critical` | When it dies, pillagers in 16 blocks get Strength for 10 seconds | 4 less maximum health | Head: a black band over the brow |
| Formation | Captain | `melee` | 20% less damage taken while another pillager is in 8 blocks | 20% more damage taken while no other pillager is in 8 blocks | Body: a gray tabard |

### Scout genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Fleet Foot | Scout | `projectile` | 20% more speed | 10% less maximum health | Legs: light boots |
| Spyglass | Scout | `unreached` | 50% more follow range | 10% less maximum health | Body: a spyglass at the belt. This changes the model |
| Pathseeker | Scout | `spotted` | It sees invisible players in 16 blocks | 10% less speed | Head: pale, sharp eyes |
| Evasion | Scout, sub Looter | `projectile` | It steps to the side when a player aims at it from far away | 10% less bolt damage | Legs: patched trousers |
| Camouflage | Scout | `night` | It is almost invisible while it stands in leaves or tall grass | 10% less maximum health | Body: a cloak of leaves |
| Signal Smoke | Scout | `melee` | When something hurts it, it sends up smoke and calls the pillagers in 24 blocks | 10% less bolt damage | Back: a smoke pot on the belt |

### Looter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Pickpocket | Looter | `melee` | Each bolt hit has a 10% chance to make the player drop the item in the main hand | 10% less bolt damage | Hands: long, quick fingers |
| Plunder | Looter | `alone` | It picks up items | 10% less speed | Back: a full sack. This changes the model |
| Hunger Raid | Looter | `unreached` | Its bolts give Hunger for 10 seconds | 10% less maximum health | Body: a belt of empty food bags |
| Saboteur | Looter, sub Bombardier | `shield` | Its bolts damage the armor of a player two times as much | 10% less bolt damage | Hands: a file and pliers on the belt |
| Greed | Looter | `critical` | Each hit takes 3 XP points from a player, and it drops them when it dies | 10% less maximum health | Head: a gold tooth |
| Escape Artist | Looter | `golem` | 30% more speed while its health is under half | 10% less speed while its health is over half | Legs: torn trousers |

## Gene details

| Gene | Rules |
|---|---|
| All | "A bolt" is the arrow from the crossbow of the pillager. The arrow genes of the skeletons work on the crossbow of the pillager in the same way |
| Bombardiers, Rocketeer | The rocket is a firework rocket with one red star. It explodes at the target, as a rocket from the crossbow of a player does. It does not break blocks |
| Captains, Banner Bearer, Rally Cry, Patrol Leader, Grudge, Formation | "Pillagers" are the pillagers only. The other illagers do not count |
| Looters | "Its health is over half" is the `healthy` state |
| Piercer | It works as the skeleton Shieldbreaker |
| Long Bolt | A longer range also needs more follow range, as for the skeleton Eagle Eye |
| Quick Charge | It is not the Quick Charge enchantment. It also works with the enchantment |
| Patrol Leader | A patrol of pillagers gets one more pillager when it spawns. A raid does not change |
| Camouflage | It works as the creeper Leafskin |
| Signal Smoke | It works as the zombie Signal Fire |
| Pickpocket | It works as the parched Fumble |
| Greed | It works as the zombie Leech |
| Powder Keg | It uses the `death_burst` effect. It does not break blocks |

## Equipment

- A pillager keeps its crossbow. The crossbow is its natural weapon.
- Mobs with genes do not spawn with random armor. A pillager with genes has
  no random enchantment on its crossbow from its spawn.
- In a raid, the vanilla raid enchantments stay: Quick Charge, Multishot, and
  Piercing.
- A captain keeps its ominous banner, and Bad Omen works as in vanilla.

## Conversion

- The pillager does not convert to a different species, and no species
  converts to the pillager.

## Babies and jockeys

- Pillagers have no babies in vanilla. They do not get a gene for them.
- A pillager can ride a ravager in a raid, as in vanilla. It does not get a
  gene for this.

## Body

- The pillager has the illager model: a head with a nose and a hat, a body,
  arms, and legs. Its texture is 64 x 64.
- It needs its own region map, `textures/region/illager.png`, with regions for
  the face, the nose, the scalp, the body, the coat, the arms, the hands, and
  the legs. The vindicator and the evoker can use the same map later.
- The gene models need the illager bones: `head`, `body`, `right_arm`,
  `left_arm`, `right_leg`, and `left_leg`.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Fire Archers | Crossbowman | Bombardier | Strong bolts and rockets |
| Bolt Sergeants | Crossbowman | Captain | Strong shots that lead |
| Rangers | Crossbowman | Scout | Strong shots on the move |
| Bounty Hunters | Crossbowman | Looter | Strong shots that rob |
| Rocket Gunners | Bombardier | Crossbowman | Rockets that hit hard |
| Siege Masters | Bombardier | Captain | Rockets that lead |
| Fire Runners | Bombardier | Scout | Fast rockets |
| Arsonists | Bombardier | Looter | Rockets that rob |
| Marshals | Captain | Crossbowman | Leaders with strong shots |
| Warlords | Captain | Bombardier | Leaders with rockets |
| Vanguards | Captain | Scout | Fast leaders |
| Bandit Chiefs | Captain | Looter | Leaders that rob |
| Skirmishers | Scout | Crossbowman | Fast, strong shots |
| Fire Scouts | Scout | Bombardier | Fast rockets |
| Forerunners | Scout | Captain | Fast leaders of the patrol |
| Prowlers | Scout | Looter | Fast thieves |
| Highwaymen | Looter | Crossbowman | Thieves with strong shots |
| Firebugs | Looter | Bombardier | Thieves with rockets |
| Gang Bosses | Looter | Captain | Thieves that lead |
| Cutpurses | Looter | Scout | Fast thieves |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Crossbowmen".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Artillery | Bombardier and Crossbowman | Bolts and rockets |
| Officers | Captain and Crossbowman | Leaders with strong shots |
| Hunting Parties | Scout and Crossbowman | Fast, strong shots |
| Brigands | Looter and Crossbowman | Thieves with strong shots |
| Fire Brigades | Bombardier and Captain | Leaders with rockets |
| Raiding Parties | Bombardier and Scout | Fast rockets |
| Pyromaniacs | Bombardier and Looter | Rockets that rob |
| Patrol Riders | Captain and Scout | A fast patrol |
| Robber Barons | Captain and Looter | Leaders that rob |
| Raider Gangs | Looter and Scout | Fast thieves |

## Neutral

A neutral pillager has no genes, or no family points.
It is a plain pillager, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the pillager, with the illager bones | All visuals |
| A region map for the illager | All visuals |
| The arrow and bow genes on the crossbow: damage, rate, range, load time, spread, knockback, shield pierce, status effects, and fire | Crossbowmen, Scouts, and most bolt genes |
| A chance that a crossbow shot is a firework rocket, and a timed rocket shot | Bombardiers, Rocketeer |
| A larger blast for the rockets of the mob | Flak |
| An aura that reaches only mobs of the same species | Captains, Banner Bearer (check the `allies` option of `aura`) |
| One more pillager in a patrol | Patrol Leader |
| A chance for `drain_food` | Looters, tier 1 |
| The pressure `raid` | The deaths of pillagers in raids |
