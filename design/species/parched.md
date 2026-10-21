# Parched

This document defines the families and the genes of the parched.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The parched is the skeleton of the desert. It spawns in deserts, it has 16
health, and it shoots slower than the skeleton. Its arrows give Weakness for
30 seconds, and Weakness does not affect it. The sun does not burn it. Its
genes fit the curse of weakness, the desert wind, the vultures that circle
the dead, the scorpions under the sand, and the fossils of old bones.
It shares no genes and no families with the skeleton, the stray, the bogged,
the wither skeleton, or the husk.

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

### Enfeeblers

Enfeeblers carry a stronger curse of weakness. Their arrows are weaker.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | The Weakness from its arrows lasts two times longer and is one level stronger | 10% less arrow damage |
| Tier 2 | The Weakness from its arrows lasts two times longer | 20% less arrow damage |
| Tier 1 | The Weakness from its arrows lasts 50% longer | 25% less arrow damage |

### Windcarvers

Windcarvers shoot on the desert wind. Their bodies are light.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more bow range, and its arrows knock the target back two times as far | 10% less maximum health |
| Tier 2 | 20% more bow range, and its arrows knock the target back 50% farther | 20% less maximum health |
| Tier 1 | 10% more bow range | 25% less maximum health |

### Vultures

Vultures hunt the weak and the wounded. A healthy target is hard for them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 40% more damage while its target player is under half health | 10% less damage while its target player is over half health |
| Tier 2 | 30% more damage while its target player is under half health | 15% less damage while its target player is over half health |
| Tier 1 | 20% more damage while its target player is under half health | 20% less damage while its target player is over half health |

### Scorpions

Scorpions have a poison sting. The heat of the day makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each arrow hit has a 60% chance to give Poison for 4 seconds | 10% less speed in the day |
| Tier 2 | Each arrow hit has a 40% chance to give Poison for 4 seconds | 15% less speed in the day |
| Tier 1 | Each arrow hit has a 25% chance to give Poison for 4 seconds | 20% less speed in the day |

### Fossils

Fossils are bones that turned to stone. Stone breaks in a blast.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor | 25% more explosion damage |
| Tier 2 | 4 more armor | 50% more explosion damage |
| Tier 1 | 2 more armor | 75% more explosion damage |

## Pressures

The parched uses the zombie pressures, and `night`, `wolf`, and `shield`.
The sun does not burn it, so it does not use `sunlight`. It also uses this
pressure.

| Pressure | A parched death counts when | State |
|---|---|---|
| `day` | The parched dies while it is day | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Enfeebler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sap | Enfeebler | `projectile` | The Weakness from its arrows is one level stronger | It shoots 15% less often | Arms: gray, drained streaks on the bow arm |
| Fumble | Enfeebler | `melee` | Each arrow hit has a 15% chance to make the player drop the item in the main hand | 10% less arrow damage | Fingers: long, crooked fingers |
| Exploit | Enfeebler, sub Vulture | `critical` | 30% more arrow damage to a player with Weakness | 2 less armor | Eyes: a thin pale ring in each socket |
| Curse Cloud | Enfeebler | `crowd` | When it dies, it leaves a cloud of Weakness for 5 seconds | 4 less maximum health | Ribs: gray dust in the rib cage |
| Unnerving Rattle | Enfeebler | `alone` | Players in 6 blocks get Weakness for 3 seconds, each 10 seconds | Players hear it from two times farther | Jaw: loose teeth that rattle |
| Withering Glare | Enfeebler | `spotted` | A player that looks at it gets Weakness for 3 seconds | 10% less speed | Eyes: dull gray light in the sockets |

### Windcarver genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Wind | Windcarver | `unreached` | 50% more bow range | 10% less speed | Head: a cloth wrap that blows back from the skull. This changes the model |
| Gust Shot | Windcarver, sub Vulture | `melee` | Its arrows knock the target back two times as far | It shoots 10% less often | Arms: cloth strips on the bow arm |
| Tailwind | Windcarver | `fall` | 25% more speed on sand | 10% less speed on other blocks | Legs: sand-scoured shins |
| Wind Dodge | Windcarver, sub Fossil | `projectile` | 25% chance to dodge a projectile | 10% less arrow damage | Body: a sash that flaps in the wind |
| Whirlwind Volley | Windcarver | `crowd` | Each 10 seconds, it shoots 3 arrows in a fan | Its arrows do 15% less damage | Back: a quiver of feathered arrows. This changes the model |
| Quickwind | Windcarver | `spotted` | It shoots 30% more often | Its arrows spread two times as much | Fingers: worn, polished tips |

### Vulture genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Carrion Eye | Vulture | `critical` | 30% more arrow damage to a player under half health | 10% less speed | Eyes: red rings in the sockets |
| Circler | Vulture | `melee` | It walks back to keep 10 blocks from its target while it shoots | It shoots 10% less often | Shoulders: a ruff of dark feathers. This changes the model |
| Gorge | Vulture | `alone` | Each arrow hit heals it 1 health | 2 less armor | Jaw: a long, hooked jaw like a beak. This changes the model |
| Flock | Vulture | `alone` | Its spawn groups have 1 more parched | Each parched of the group has 15% less maximum health | Back: black feathers down the spine |
| Glide | Vulture, sub Windcarver | `fall` | No fall damage, and it jumps 50% higher | 50% more explosion damage | Arms: bone wings with feathers under the arms. This changes the model |
| Watcher | Vulture | `spotted` | It sees invisible players | 10% less maximum health | Head: a bald, wrinkled crown |

### Scorpion genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Stinger | Scorpion | `projectile` | Its arrows give Poison for 4 seconds | 10% less arrow damage | Back: a curled scorpion tail. This changes the model |
| Pincers | Scorpion | `melee` | Its arrows give Slowness for 2 seconds | 10% less speed | Hands: claws like pincers. This changes the model |
| Carapace | Scorpion | `explosion` | 4 more armor | 10% less speed | Chest: a dark shell over the ribs. This changes the model |
| Sand Hide | Scorpion | `spotted` | It is almost invisible while it stands on sand in the day | 10% less maximum health | Body: sand-colored patches |
| Venom Sac | Scorpion, sub Fossil | `critical` | When it dies, it leaves a cloud of Poison for 4 seconds | 4 less maximum health | Pelvis: a swollen yellow sac |
| Tail Whip | Scorpion | `melee` | A player that hits it in melee takes 2 damage | 10% less arrow damage | Spine: spikes down the back |

### Fossil genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Petrified | Fossil | `projectile` | 30% less damage from projectiles | 15% less speed | Bones: gray stone texture |
| Amber Heart | Fossil | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Chest: a glowing amber stone in the ribs. This changes the model |
| Bedrock Bones | Fossil | `melee` | Full knockback resistance | 10% less speed | Legs: dark stone at the joints |
| Stoneskin | Fossil | `projectile` | 4 more armor | 15% less speed | Skull: cracks filled with stone |
| Sediment | Fossil, sub Scorpion | `fire` | It heals 1 health each 3 seconds while it stands on sand | 25% more fire damage | Feet: layers of packed sand |
| Last Stand | Fossil | `critical` | 30% more damage while its health is under half | 20% less damage while its health is over half | Ribs: one rib that is broken |

## Gene details

| Gene | Rules |
|---|---|
| Enfeeblers, Sap | They change the Weakness that the parched puts on its arrows in vanilla, 30 seconds of Weakness I. "One level stronger" is Weakness II |
| Vultures | The buff and the debuff count only while the target of the parched is a player |
| Scorpions | "In the day" means while it is bright outside |
| Fumble | The item drops on the ground in front of the player. An item with Curse of Binding does not drop. A player cannot lose a held item more than one time each 10 seconds |
| Exploit | It counts the Weakness from any source |
| Sand Hide | It is drawn translucent, like the zombie Shade. Other mobs still see it |
| Amber Heart | It works as the drowned Pearlheart |
| Tailwind, Sediment | "Sand" means the blocks in the tag `#minecraft:sand` |

## Equipment

- A parched keeps its bow. The bow is its natural weapon.
- Its arrows give Weakness, as in vanilla. The Enfeebler genes make this
  effect stronger.
- Mobs with genes do not spawn with random armor. A parched with genes has
  no armor and no enchanted bow from its spawn.

## Conversion

- The parched does not convert to a different species, and no species
  converts to the parched.

## Babies and jockeys

- Parched have no babies and no jockeys in vanilla. They do not get a gene
  for them.

## Body

- The parched has the shape of the skeleton, with cloth wraps on the head, the
  body, the arms, and the legs. Its texture is 64 x 64, and its left arm has
  its own texture area.
- It has its own region map, `textures/region/parched.png`, with the region
  and material ids of the skeleton map. The wraps have the cloth material.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Ill Winds | Enfeebler | Windcarver | A curse that rides the wind |
| Carrion Curses | Enfeebler | Vulture | Archers that weaken, then pick the bones |
| Blight Stings | Enfeebler | Scorpion | Weakness and poison together |
| Cursed Relics | Enfeebler | Fossil | Old stone that saps all strength |
| Sirocco Bows | Windcarver | Enfeebler | Hot winds that weaken |
| Sky Hunters | Windcarver | Vulture | Archers that circle high |
| Sand Stingers | Windcarver | Scorpion | Fast stings from far away |
| Dune Sentries | Windcarver | Fossil | Watchers of stone on the dunes |
| Wake Watchers | Vulture | Enfeebler | They wait for the weak |
| Thermal Riders | Vulture | Windcarver | Vultures on the rising heat |
| Carrion Stings | Vulture | Scorpion | Poison for the dying |
| Bone Pickers | Vulture | Fossil | Old hunters of old bones |
| Plague Tails | Scorpion | Enfeebler | Stings that drain the body |
| Dust Stingers | Scorpion | Windcarver | Stings carried on the wind |
| Death Stalkers | Scorpion | Vulture | Poison that hunts the wounded |
| Rock Scorpions | Scorpion | Fossil | Armored stings |
| Grave Stones | Fossil | Enfeebler | Stone that curses |
| Wind Worn | Fossil | Windcarver | Stone shaped by the wind |
| Old Hunters | Fossil | Vulture | Patient stone that waits |
| Amber Stings | Fossil | Scorpion | Stings kept in amber |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Enfeeblers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Hot Breaths | Enfeebler and Windcarver | A weakening wind |
| Grave Watchers | Enfeebler and Vulture | Death that waits and weakens |
| Black Stings | Enfeebler and Scorpion | Poison and weakness |
| Cursed Fossils | Enfeebler and Fossil | Old bones with an old curse |
| Sky Stalkers | Windcarver and Vulture | Hunters of the open sky |
| Sand Squalls | Windcarver and Scorpion | Stings in the storm |
| Wind Stones | Windcarver and Fossil | Stone that the wind carved |
| Carrion Stingers | Vulture and Scorpion | Poison for the wounded |
| Bone Yards | Vulture and Fossil | The old dead of the desert |
| Amber Tails | Scorpion and Fossil | Stone scorpions |

## Neutral

A neutral parched has no genes, or no family points.
It is a plain parched, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the parched | All visuals |
| The change of the Weakness on the arrows of the parched | Enfeeblers, Sap |
| A state for a target with Weakness | Exploit |
| States for a target player under and over half health | Vultures, Carrion Eye |
| A state for the day | Scorpions, Sand Hide |
| A hit that makes the player drop the held item | Fumble |
| The pressure `day` | The play of the parched in the day |
