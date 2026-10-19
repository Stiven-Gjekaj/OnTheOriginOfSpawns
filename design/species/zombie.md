# Zombie

This document defines the families and the genes of the zombie.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

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

| Result | Effects |
|---|---|
| Pure family | Tier 3 from the major |
| Minor family | Tier 2 from the major, and tier 1 from the sub |
| Hybrid | Tier 2 from each of the two families |

A higher tier makes the buff stronger and the debuff weaker.
The order of power is: pure families, hybrids, minor families, neutral.

### Predators

Predators chase and hit hard. They are fast and fragile.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 40% more speed, and 4 more attack damage | 10% less maximum health |
| Tier 2 | 20% more speed, and 2 more attack damage | 15% less maximum health |
| Tier 1 | 10% more speed | 20% less maximum health |

### Stalkers

Stalkers hide and attack first. They are weak in the light.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | No sounds at all, and the first hit on a player does triple damage | 10% more damage taken at light level 12 or more |
| Tier 2 | No step sounds, and the first hit on a player does double damage | 20% more damage taken at light level 12 or more |
| Tier 1 | Quieter steps, and the first hit does 50% more damage | 25% more damage taken at light level 12 or more |

### Tanks

Tanks take hits and hold ground. They are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 100% more maximum health, full knockback resistance, and 2 more armor | 10% less speed |
| Tier 2 | 50% more maximum health, and full knockback resistance | 15% less speed |
| Tier 1 | 25% more maximum health | 20% less speed |

### Hordes

Hordes fight in groups. A zombie of a horde is weak alone.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When the zombie is hit, all zombies in 32 blocks attack the attacker | 10% less damage with no zombie in 8 blocks |
| Tier 2 | When the zombie is hit, all zombies in 16 blocks attack the attacker | 20% less damage with no zombie in 8 blocks |
| Tier 1 | When the zombie is hit, all zombies in 8 blocks attack the attacker | 25% less damage with no zombie in 8 blocks |

### Blights

Blights carry disease. Their hits are weak, but their effects last.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit gives Hunger for 15 seconds and Weakness for 5 seconds, and a killed villager always becomes a zombie villager | 15% less attack damage |
| Tier 2 | Each hit gives Hunger for 10 seconds, and a killed villager always becomes a zombie villager | 25% less attack damage |
| Tier 1 | Each hit gives Hunger for 5 seconds | 30% less attack damage |

## Pressures

The genes use these pressures.
The pressures with "new" are not in the mod yet.
Each one is a condition of the death, like "unreached".

| Pressure | A zombie death counts when | State |
|---|---|---|
| `fire` | Fire or lava kills the zombie | In the mod |
| `projectile` | A projectile kills the zombie | In the mod |
| `melee` | A melee attack kills the zombie | In the mod |
| `explosion` | An explosion kills the zombie | In the mod |
| `fall` | A fall kills the zombie | In the mod |
| `unreached` | The zombie targeted a player and did not damage a player before it died | New |
| `sunlight` | The sun burns the zombie to death | New |
| `spotted` | The zombie was in the view of the killer, at more than 8 blocks, for 2 seconds or more before the death | New |
| `alone` | The zombie dies with no other zombie in 8 blocks | New |
| `crowd` | 3 or more zombies die in 8 blocks in 5 seconds. The death counts for each of them | New |
| `critical` | A critical hit kills the zombie | New |
| `golem` | An iron golem kills the zombie | New |
| `player_death` | The zombie kills a player. Each gene of the killer rises by its selection rate, and this pressure counts one time | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Predator genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Bonerunner | Predator | `unreached` | 20% more speed, and 16 more blocks of follow range | 2 less armor, and 50% more fall damage | Lower legs: bare bone. This changes the model |
| Lunger | Predator | `unreached` | Leaps at a player in 4 blocks | After a missed leap, it cannot move for 1 second | Hands: long dark nails |
| Frenzy | Predator | `melee` | 30% more attack speed | 4 less maximum health | Shirt: torn sleeves |
| Bloodscent | Predator, sub Stalker | `unreached` | Double follow range to a player under half health, and it sees invisible players in 8 blocks | 25% more damage taken while it chases a hurt player | Face: red stains on the mouth |
| Rabid | Predator | `projectile` | Each hit gives Nausea for 3 seconds | It sometimes attacks other mobs | Face: foam on the mouth |
| Death Throes | Predator | `melee` | When it dies, it does 4 damage to players in 2 blocks | 4 less maximum health | Hands and feet: swollen, dark veins |

### Stalker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Cinder | Stalker, sub Predator | `fire` | Fire immunity, and each hit has a 20% chance to set the player on fire | Rain and water damage it, and 4 less maximum health | Skin: more gray. Hands and feet: dark and red spots |
| Hush | Stalker | `spotted` | No idle sounds and no hurt sounds | 2 less attack damage | Face: mouth sewn shut |
| Shade | Stalker | `spotted` | At night, it is almost invisible: translucent, at about 10% opacity | The sun burns it two times faster | Arms and legs: dark, mottled patches |
| Burrower | Stalker | `unreached` | At night, it can come up from the ground near a player | It cannot move for 2 seconds after it comes up | Feet and pants: dirt |
| Lurker | Stalker | `spotted` | It stays out of the view of players. It can come close from behind. In 6 blocks, it attacks as a normal zombie | 25% less follow range | Scalp: a dark shadow, like a hood |
| Nightborn | Stalker | `sunlight` | 20% more speed and damage at light level 6 or less | 20% less damage at light level 12 or more | Face: pale, bright eyes |

### Tank genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Ironhide | Tank | `projectile` | 50% less projectile damage, and 50% knockback resistance | 10% less speed, and 25% more explosion damage | Shirt: plates that look like iron |
| Thickskull | Tank | `critical` | Critical hits do no extra damage to it | 15% less attack speed | Scalp: a bone plate |
| Bloat | Tank, sub Blight | `melee` | 10 more maximum health, and a small cloud of Poison when it dies | 25% less speed | Shirt: swollen green blotches |
| Stoneblood | Tank | `fall` | No fall damage, and full knockback resistance | 50% slower in water | Feet and lower legs: gray, like stone |
| Blastproof | Tank | `explosion` | 60% less explosion damage | 50% more fire damage | Shirt: soot on the edges |
| Mender | Tank | `melee` | It heals 1 health each 3 seconds when nothing hits it for 5 seconds | 25% less attack damage | Arms: stitched seams |

### Horde genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Packcaller | Horde | `alone` | When it sees a player, it calls the zombies in 24 blocks | Players hear its groans from two times farther | Face: open jaw |
| Brood | Horde | `crowd` | Its spawn groups have 1 more zombie | Each zombie of the group has 15% less maximum health | Shirt: ribs show through |
| Rally | Horde | `alone` | 10% more damage for each zombie in 6 blocks, to a maximum of 30% | 20% less damage when alone | Arms: bands of cloth |
| Runt | Horde | `projectile` | 20% chance to spawn as a baby zombie | Its baby zombies have 50% less maximum health | The baby model, and scalp: thin tufts of hair |
| Grave Call | Horde | `melee` | When hit, it has a 10% chance to call a new zombie | 2 less armor | Hands: grave dirt |
| Siege | Horde, sub Tank | `unreached` | It breaks doors on all difficulties, and it breaks glass slowly | 20% less speed | Hands: wood splinters |

### Blight genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rotgut | Blight | `melee` | Each hit gives Hunger II for 10 seconds | 2 less attack damage | Shirt: green and black rot |
| Contagion | Blight | `golem` | A villager that it kills always becomes a zombie villager | Iron golems attack it first, and do 50% more damage to it | Arms: green, wet sores |
| Sporebearer | Blight | `crowd` | When it dies, it leaves a cloud of Weakness, 3 blocks wide | 4 less maximum health | Scalp: small fungus |
| Leech | Blight | `melee` | Each hit takes some XP from the player | It drops no XP | Face: a round mouth with teeth |
| Miasma | Blight | `projectile` | Players in 3 blocks get Weakness | It glows, so players see it through walls | Hands: thin gloves, like slime. Eyes: lime green |
| Plague Carrier | Blight, sub Horde | `player_death` | A player that it kills rises as a zombie with its genes | 10% less maximum health | Skin: gray-green blotches |

## Gene details

These rules make the gene table exact.

| Gene | Rules |
|---|---|
| Frenzy | A zombie attacks on a timer of 1 second. Frenzy makes the timer 30% shorter |
| Thickskull | The attack timer is 15% longer |
| Lunger | It leaps when the player is 3 to 5 blocks away, with 5 seconds between leaps. A leap that touches the player does a normal hit. After a missed leap, it cannot move for 1 second |
| Bloodscent | It can target an invisible player in 8 blocks. The double follow range is only for players under half health |
| Rabid | Each 10 seconds, it has a 10% chance to target the nearest mob in 8 blocks that is not a zombie |
| Death Throes | The 4 damage hits players only, not mobs or blocks. A small burst of particles shows it |
| Hush | It keeps its death sound |
| Shade | The client draws it translucent at night. It is not the invisibility effect, so other mobs still see it |
| Burrower | When it spawns at night in 24 blocks of a player, it starts 2 blocks under the surface, and rises in 2 seconds with dirt particles. It rises only through dirt, grass, sand, and gravel. It breaks no blocks |
| Lurker | It moves only where the player does not face. The camera mode does not change this |
| Packcaller | When it first sees a player, all zombies in 24 blocks target that player. It can call again after 30 seconds |
| Brood | When it spawns in a natural way, 1 more zombie with the same genes spawns near it |
| Grave Call | The new zombie has no genes. Each Grave Call zombie calls a maximum of 2 zombies |
| Siege | It breaks glass blocks and glass panes only, in 10 seconds for each block. It obeys the gamerule that stops mobs from breaking blocks |
| Runt | Runt babies can ride chickens at the vanilla chance |
| Leech | Each hit takes 3 XP points. When it dies, it drops the XP that it took, and no other XP |
| Miasma | It has the Glowing effect all the time. Its aura gives Weakness I to players in 3 blocks, renewed each 2 seconds |
| Plague Carrier | A zombie with the genes of the killer spawns where the player died, and wears the head of the player. The items of the player drop on the ground as in vanilla |

## Family details

- The Stalker "first hit" is the first hit after 10 seconds with no combat.
- The Horde call makes all zombies in range attack the attacker, of any
  family.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Ambushers | Predator | Stalker | Fast hunters that hit first from the dark |
| Brutes | Predator | Tank | Fast and hard to stop |
| Packhunters | Predator | Horde | Fast groups that surround the player |
| Fevermaws | Predator | Blight | Fast biters that spread disease |
| Shadowfangs | Stalker | Predator | Hidden hunters that strike, then chase |
| Barrowkin | Stalker | Tank | Quiet and tough, they wait under the ground |
| Whisperers | Stalker | Horde | Silent groups that close in at night |
| Gloomrot | Stalker | Blight | Hidden carriers of disease |
| Juggernauts | Tank | Predator | Slow walls that hit hard |
| Stonewatchers | Tank | Stalker | Tough guards that wait in the dark |
| Phalanx | Tank | Horde | Tough lines that move as one |
| Bloatbacks | Tank | Blight | Swollen and slow, they poison when they die |
| Ravagers | Horde | Predator | Fast swarms that run down the player |
| Night Swarm | Horde | Stalker | Swarms that come from the dark with no sound |
| Siegeborn | Horde | Tank | Groups that break into bases |
| Plaguehost | Horde | Blight | Groups that spread disease to each other and the player |
| Carrion | Blight | Predator | Diseased hunters that follow the hurt |
| Hushrot | Blight | Stalker | Silent carriers that infect from cover |
| Rotbulks | Blight | Tank | Slow, tough carriers of rot |
| Pestilence | Blight | Horde | A plague that grows with each kill |

## Baby zombies

- Baby zombies do not spawn in vanilla ways.
- Only the Runt gene makes baby zombies.
  Thus, a baby zombie shows that its pool evolved Runt.
- At the start of a world, no baby zombies spawn.

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Predators".
It gives the tier 3 effects of that family.

## Hybrids

A hybrid has the same points in two families.
It gives the tier 2 effects of each of the two families.
Each hybrid has its own name.

| Hybrid | Families | Play style |
|---|---|---|
| Gloamhunters | Predator and Stalker | Hunters of the dusk, quick and hard to see |
| Rendhulks | Predator and Tank | Heavy bodies that still close the distance |
| Feral Host | Predator and Horde | Wild groups that chase as one |
| Fevered | Predator and Blight | Restless biters that burn with disease |
| Cairnwalkers | Stalker and Tank | Quiet, heavy shapes that rise from old graves |
| Hollow Choir | Stalker and Horde | Silent crowds that move together in the dark |
| Mirelings | Stalker and Blight | Damp, hidden carriers from swamps and caves |
| Bulwark | Tank and Horde | Tough groups that hold a line |
| Moldhides | Tank and Blight | Thick skins covered in mold and rot |
| Plaguetide | Horde and Blight | A rising wave of sick zombies |

## Neutral

A neutral zombie has no genes, or no family points.
It is a plain zombie, with no family effects.

