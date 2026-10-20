# Skeleton

This document defines the families and the genes of the skeleton.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The skeleton lives in most biomes. It shoots arrows with a bow, the sun burns
it, and it runs from wolves. Its genes fit archery, bones, speed, ranks of the
dead, and cursed arrows.
It shares no genes and no families with the zombie, the husk, or the drowned.
The stray, the bogged, and the wither skeleton are different species. Each one
gets its own design file.

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

### Marksmen

Marksmen shoot hard and far. Close up, they are easy to hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more arrow damage, and 25% more bow range | 10% more damage taken from melee |
| Tier 2 | 20% more arrow damage | 20% more damage taken from melee |
| Tier 1 | 10% more arrow damage | 25% more damage taken from melee |

### Rattlers

Rattlers are fast and hard to catch. Their bones are light and break easily.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 20% more speed, and it keeps 8 blocks from its target while it shoots | 10% less maximum health |
| Tier 2 | 15% more speed | 15% less maximum health |
| Tier 1 | 10% more speed | 20% less maximum health |

### Ossified

Ossified skeletons have thick, heavy bones. They are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor, and full knockback resistance | 10% less speed |
| Tier 2 | 4 more armor, and full knockback resistance | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Legions

Legions fight in ranks. Alone, they are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage with another skeleton in 8 blocks | 10% less damage with no skeleton in 8 blocks |
| Tier 2 | 20% more damage with another skeleton in 8 blocks | 20% less damage with no skeleton in 8 blocks |
| Tier 1 | 10% more damage with another skeleton in 8 blocks | 25% less damage with no skeleton in 8 blocks |

### Hexers

Hexers shoot cursed arrows. The sun burns their dark bones faster.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each arrow hit has a 60% chance to give Weakness for 4 seconds | The sun burns it 10% faster |
| Tier 2 | Each arrow hit has a 40% chance to give Weakness for 4 seconds | The sun burns it 25% faster |
| Tier 1 | Each arrow hit has a 25% chance to give Weakness for 4 seconds | The sun burns it 50% faster |

## Pressures

The skeleton uses the zombie pressures. It also uses these pressures.

| Pressure | A skeleton death counts when | State |
|---|---|---|
| `night` | The skeleton dies between dusk and dawn | From the husk |
| `cold` | The skeleton dies in a cold biome, or freezing kills it | From the husk |
| `wolf` | A wolf kills the skeleton | New |
| `shield` | The killer held a shield up in the 10 seconds before the death | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Marksman genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Eagle Eye | Marksman | `unreached` | 50% more bow range | 10% less speed | Eyes: a bright ring in each socket |
| Deadeye | Marksman | `spotted` | Its arrows fly straight, with no spread | It draws its bow 25% slower | Skull: a crack over one eye |
| Longdraw | Marksman | `projectile` | Its arrows do 30% more damage | It shoots 20% less often | Arms: a leather bracer on the left forearm. This changes the model |
| Quickdraw | Marksman, sub Rattler | `crowd` | It shoots 50% more often | Its arrows do 25% less damage | Fingers: worn, white tips |
| Shieldbreaker | Marksman | `shield` | Its arrows go through shields | 4 less maximum health | Ribs: a notch cut in each rib |
| High Ground | Marksman | `fall` | 30% more arrow damage at a target lower than it | 20% less arrow damage at a target higher than it | Feet: claws of bone |

### Rattler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Skitter | Rattler | `melee` | 25% more speed | 15% less maximum health | Legs: thin, cracked bones |
| Backpedal | Rattler | `melee` | It walks back to keep 12 blocks from its target while it shoots | It shoots 15% less often | Spine: a bent, curved back |
| Bonedash | Rattler | `projectile` | When a player draws a bow at it, it moves 3 blocks to the side, each 8 seconds | 2 less armor | Pelvis: a cracked hip |
| Hollow Bones | Rattler | `fall` | No fall damage, and it jumps 50% higher | 50% more damage from explosions | Bones: small holes, as in bird bones |
| Lightfoot | Rattler | `spotted` | No step sounds and no idle sounds | 10% less arrow damage | Feet: wrapped in rags |
| Fleetbone | Rattler, sub Legion | `critical` | 40% more speed when its health is under half | 20% less damage when its health is under half | Skull: a broken jaw that hangs loose |

### Ossified genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Thickbone | Ossified | `projectile` | 4 more armor | 10% less speed | Bones: thick and knobbed. This changes the model |
| Calcified | Ossified | `explosion` | Full knockback resistance, and 40% less explosion damage | 15% less speed | Bones: chalk white crust |
| Marrowmend | Ossified | `fire` | It heals 1 health each 3 seconds out of combat | 25% more fire damage | Joints: red marrow at each joint |
| Reassemble | Ossified, sub Hexer | `critical` | One time in its life, it falls into a pile of bones when it dies, and stands up again after 3 seconds with half its health | 4 less maximum health | Bones: wire that ties the joints together |
| Bonewall | Ossified | `melee` | 50% less damage from projectiles that hit it from the front | It shoots 20% less often | Ribs: a rib cage that grows over the chest. This changes the model |
| Holy Ward | Ossified | `wolf` | 50% less damage from Smite weapons, and wolves do not attack it | 25% more fire damage | Skull: carved runes on the brow |

### Legion genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Barrage | Legion | `crowd` | When 3 or more skeletons in 16 blocks target the same player, they shoot at the same time | It shoots 10% less often | Back: a quiver of many arrows. This changes the model |
| Bugler | Legion | `spotted` | When it sees a player, the skeletons in 24 blocks target that player. It can call again after 30 seconds | Players hear it from two times farther | Mouth: a bone horn at the jaw. This changes the model |
| Phalanx | Legion | `alone` | 30% less damage taken with another skeleton in 4 blocks | 30% more damage taken with no skeleton in 4 blocks | Arms: a shield strap on the left arm |
| Muster | Legion | `alone` | Its spawn groups have 1 more skeleton | Each skeleton of the group has 15% less maximum health | Chest: a painted rank mark |
| Warcry | Legion, sub Ossified | `melee` | When it is hurt, the skeletons in 12 blocks get Speed for 4 seconds, each 15 seconds | 2 less armor | Skull: a wide, open jaw |
| Spider Rider | Legion | `unreached` | 10% chance to spawn on a spider | 10% less maximum health | Legs: spider silk around the knees |

### Hexer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Venomtip | Hexer | `melee` | Its arrows give Poison for 4 seconds | 10% less arrow damage | Fingers: green stains |
| Frostnock | Hexer | `fire` | Its arrows give Slowness for 3 seconds | 25% more fire damage | Bones: a frost-blue tint |
| Witherhex | Hexer | `golem` | Its arrows give Wither for 3 seconds | 4 less maximum health | Bones: black at the tips |
| Blindshot | Hexer | `spotted` | Each arrow hit has a 25% chance to give Blindness for 2 seconds | 10% less speed | Eyes: dark smoke in the sockets |
| Soulfire | Hexer, sub Marksman | `in_water` | Its arrows set the target on fire | The sun burns it two times faster | Eyes: blue soul flames that glow. This changes the model |
| Hexmark | Hexer | `alone` | Each arrow hit gives the player Glowing for 10 seconds, so other skeletons find the player | Its runes glow, so players see it in the dark | Bones: runes that glow |

## Gene details

| Gene | Rules |
|---|---|
| Deadeye | The spread of its arrows is 0 on each difficulty |
| Shieldbreaker | A shield does not stop its arrow. The arrow does its full damage |
| High Ground | The target must be 2 or more blocks lower or higher than the skeleton |
| Backpedal | It walks back only on safe blocks. It does not walk into lava, water, or a drop of more than 3 blocks |
| Bonedash | It works like the husk Mirage, but it starts when a player draws a bow and looks at it |
| Reassemble | The pile of bones takes no damage. A player can stop it with a hit from a Smite weapon. Then it does not stand up |
| Bonewall | The front is the half of the space in front of the skeleton |
| Holy Ward | Wolves also do not run at it. It still runs from wolves |
| Barrage | The volley needs a clear line of sight from each skeleton |
| Spider Rider | Skeleton jockeys come only from this gene. A spider does not spawn with a skeleton in vanilla ways |
| Soulfire | It works only with its bow. Its arrows do not set blocks on fire |

## Equipment

- A skeleton keeps its bow. The bow is its natural weapon.
- Mobs with genes do not spawn with random armor. A skeleton with genes has
  no armor and no enchanted bow from its spawn.
- Genes can give items, for example the quiver of Barrage and the shield
  strap of Phalanx, as model parts.

## Conversion

- A skeleton that freezes in powder snow converts to a stray. The stray gets
  new genes from the stray pool. It keeps none of its skeleton genes.

## Spider jockeys

- Spider jockeys do not spawn in vanilla ways.
- Only the Spider Rider gene makes a skeleton ride a spider.
- At the start of a world, no spider jockeys spawn.

## Babies

- Skeletons have no babies in vanilla. They do not get a gene for babies.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Sharpshooters | Marksman | Rattler | Archers that shoot, then run |
| Ballistae | Marksman | Ossified | Heavy archers that hold their ground |
| Firing Lines | Marksman | Legion | Ranks of archers that shoot as one |
| Cursed Eyes | Marksman | Hexer | Sure shots with cursed heads |
| Harriers | Rattler | Marksman | Quick hunters that shoot on the run |
| Rattleguard | Rattler | Ossified | Fast skeletons that are hard to break |
| Outriders | Rattler | Legion | Scouts that call the ranks |
| Wisp Bones | Rattler | Hexer | Fast shapes that curse from the dark |
| Bastions | Ossified | Marksman | Walls of bone that shoot back |
| Boneplates | Ossified | Rattler | Heavy skeletons that still move fast |
| Shieldwalls | Ossified | Legion | Rows of thick bone that do not break |
| Cairn Guards | Ossified | Hexer | Old guards of the grave, hard and cursed |
| Centuries | Legion | Marksman | Disciplined ranks of archers |
| Skirmish Lines | Legion | Rattler | Groups that strike and fall back |
| Bone Cohorts | Legion | Ossified | Armored ranks that push forward |
| Death Choirs | Legion | Hexer | Groups that curse the player together |
| Bonehexes | Hexer | Marksman | Cursed arrows from far away |
| Gravewisps | Hexer | Rattler | Fast skeletons with poisoned arrows |
| Barrow Wights | Hexer | Ossified | Tough dead that curse all that they hit |
| Hex Legions | Hexer | Legion | Ranks that cover the player in curses |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Marksmen".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Hawkbones | Marksman and Rattler | Fast archers that never stand still |
| Siegebones | Marksman and Ossified | Heavy archers that do not fall |
| Arrowstorms | Marksman and Legion | Rains of arrows from the ranks |
| Doomshots | Marksman and Hexer | Sure shots that curse |
| Ironmarrow | Rattler and Ossified | Fast and hard to break |
| War Bands | Rattler and Legion | Raiding groups that come and go |
| Night Rattlers | Rattler and Hexer | Quick shapes with cursed arrows |
| Ossuary | Ossified and Legion | A wall of bones that moves as one |
| Grave Lords | Ossified and Hexer | Old, cursed, and hard to kill |
| Black Legion | Legion and Hexer | Ranks of the cursed dead |

## Neutral

A neutral skeleton has no genes, or no family points.
It is a plain skeleton, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The region map of the skeleton body, with its thin limbs | All visuals |
| Throw rate and throw range for the bow goal of the skeleton | Marksmen, Eagle Eye, Longdraw, Quickdraw, Backpedal, Bonewall, Barrage |
| Arrow damage, arrow spread, and the draw time of the bow | Marksmen, Deadeye, Longdraw, Quickdraw, Lightfoot |
| Arrows that go through shields | Shieldbreaker |
| A state for a target that is lower or higher than the mob | High Ground |
| A walk back that keeps a distance from the target | Rattlers, Backpedal |
| A sidestep when a player draws a bow | Bonedash |
| A state for health under half | Fleetbone |
| A revival after death, with a pile of bones | Reassemble |
| Damage taken from the front, and damage from Smite weapons | Bonewall, Holy Ward |
| Wolves that do not attack the mob | Holy Ward |
| A shared volley of a group | Barrage |
| A spawn on a spider, and no spider jockeys in vanilla ways | Spider Rider |
| Arrows that set the target on fire | Soulfire |
| The pressures `wolf` and `shield` | Holy Ward, Shieldbreaker |
