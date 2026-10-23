# Witch

This document defines the families and the genes of the witch.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The witch is a raider that throws splash potions. It has 26 health. It lives
in swamp huts, spawns in the dark, and comes in raids. Each 3 seconds, it
throws a splash potion at a target in 10 blocks: Slowness at 8 blocks or
more, Poison at a target with 8 health or more, Weakness at 3 blocks or less,
and Harming in the other cases. It drinks potions of Water Breathing, Fire
Resistance, Healing, and Swiftness. It takes 85% less damage from magic. In a
raid, it heals the other raiders.
Its genes fit the brew, the throw, the potions that it drinks, the coven of
witches, and the swamp. It shares no genes and no families with the other
species.

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

### Brewers

Brewers make strong potions. The work makes them weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | The effects of its thrown potions last 50% longer | 10% less maximum health |
| Tier 2 | The effects of its thrown potions last 30% longer | 15% less maximum health |
| Tier 1 | The effects of its thrown potions last 20% longer | 20% less maximum health |

### Slingers

Slingers throw fast. Their potions are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It throws 30% more often | The effects of its thrown potions last 10% shorter |
| Tier 2 | It throws 20% more often | The effects of its thrown potions last 15% shorter |
| Tier 1 | It throws 15% more often | The effects of its thrown potions last 20% shorter |

### Alchemists

Alchemists drink to keep alive. Each drink makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | At under half health, it drinks a potion of Healing, one time each 20 seconds | 10% less speed |
| Tier 2 | At under half health, it drinks a potion of Healing, one time each 30 seconds | 15% less speed |
| Tier 1 | At under half health, it drinks a potion of Healing, one time each 40 seconds | 20% less speed |

### Covens

Covens keep the other monsters alive. Their own potions are weaker.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It throws a potion of Healing at a hurt monster in 8 blocks, one time each 10 seconds | 10% less damage |
| Tier 2 | It throws a potion of Healing at a hurt monster in 8 blocks, one time each 15 seconds | 15% less damage |
| Tier 1 | It throws a potion of Healing at a hurt monster in 8 blocks, one time each 20 seconds | 20% less damage |

### Bog Hags

Bog Hags live in the swamp water. Fire hurts them more.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 40% more speed in water | 25% more fire damage |
| Tier 2 | 30% more speed in water | 50% more fire damage |
| Tier 1 | 20% more speed in water | 75% more fire damage |

## Pressures

The witch uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `night`, `day`, `dry`, `wolf`,
`shield`, and `raid`. The sun does not burn it, so it does not use
`sunlight`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Brewer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Strong Brew | Brewer | `critical` | Its Poison, Slowness, and Weakness potions are one level stronger | 10% less maximum health | Hands: stained, purple fingers |
| Long Brew | Brewer | `projectile` | The effects of its thrown potions last two times as long | It throws 10% less often | Body: a long row of vials on the belt. This changes the model |
| Lingering Brew | Brewer | `crowd` | Its potions are lingering potions | 10% less maximum health | Body: a green cloud stain on the robe |
| Blinding Brew | Brewer | `unreached` | At 8 blocks or more, it throws Blindness in place of Slowness | 10% less speed | Head: a black veil on the hat. This changes the model |
| Wither Brew | Brewer, sub Slinger | `shield` | Its Harming potions also give Wither for 3 seconds | 10% less maximum health | Hat: a black band with a skull |
| Rust Brew | Brewer | `golem` | Its potions do two times the damage to golems | 10% less speed | Arms: rust-red stains on the sleeves |

### Slinger genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Arm | Slinger | `unreached` | It throws at a target in 16 blocks, not 10 | 10% less maximum health | Arms: a leather sling on the arm. This changes the model |
| Quick Hand | Slinger | `spotted` | It throws 30% more often | 10% less maximum health | Hands: quick, thin fingers |
| Double Throw | Slinger | `crowd` | It throws two potions at once, a little apart | It throws 10% less often | Body: two bottles in the crossed arms. This changes the model |
| Sure Aim | Slinger | `projectile` | Its potions fly straight | 10% less speed | Head: a monocle. This changes the model |
| Wide Splash | Slinger, sub Brewer | `melee` | The splash of its potions is 50% wider | 10% less maximum health | Body: splash stains on the robe |
| Glass Shards | Slinger | `shield` | Its potions do 2 damage when they break on a target | 10% less speed | Arms: glass shards in the sleeves |

### Alchemist genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Healing Draught | Alchemist | `critical` | At under half health, it drinks a potion of Healing, one time each 30 seconds | 10% less speed | Body: a red vial on the belt |
| Fire Draught | Alchemist | `fire` | When it burns, it always drinks Fire Resistance | 10% less maximum health | Hat: an orange band |
| Swift Draught | Alchemist | `unreached` | When its target is farther than 11 blocks, it always drinks Swiftness II | 10% less maximum health | Legs: light blue stockings |
| Shade Draught | Alchemist | `spotted` | When it first sees a player, it drinks Invisibility for 10 seconds, one time each 60 seconds | 10% less maximum health | Head: a gray, pale face |
| Shared Draught | Alchemist, sub Coven | `crowd` | When it drinks a potion, the monsters in 4 blocks get the same effect | 10% less speed | Body: a bowl at the belt. This changes the model |
| Feather Draught | Alchemist | `fall` | When it falls, it drinks Slow Falling | 10% less speed | Hat: a feather on the hat. This changes the model |

### Coven genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Mending Hand | Coven | `crowd` | It throws a potion of Healing at a hurt monster in 8 blocks, one time each 15 seconds | 10% less damage | Hands: a soft green glow |
| Coven Call | Coven | `spotted` | When it sees a player, it calls the witches in 24 blocks | Players hear it from two times farther | Head: a long, crooked nose |
| Coven Ward | Coven | `projectile` | The witches in 8 blocks get Resistance | 10% less speed | Body: a silver charm on the chest |
| Blood Pact | Coven | `critical` | It shares the damage that it takes with the witches in 8 blocks | 10% less speed | Hands: a red cord on the wrist |
| Bog Circle | Coven, sub Bog Hag | `in_water` | The witches in 8 blocks get Dolphin's Grace in water | 10% less maximum health | Legs: wet, green hems |
| Last Rites | Coven | `alone` | When a witch dies in 16 blocks, it heals 6 health | 10% less damage | Hat: a ring of candles on the hat. This changes the model |

### Bog Hag genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Swamp Stride | Bog Hag | `in_water` | 30% more speed in water | 25% more fire damage | Legs: muddy boots |
| Rain Dance | Bog Hag | `dry` | It heals 1 health each 3 seconds in the rain | 10% less speed | Hat: drops of rain on the brim |
| Mudskin | Bog Hag | `explosion` | 30% less damage from explosions | 10% less speed | Head: dried mud on the face |
| Toad Leap | Bog Hag | `fall` | It jumps two times as high, and it takes half the fall damage | 10% less maximum health | Legs: green, toad-like skin |
| Bog Breath | Bog Hag, sub Brewer | `melee` | Players in 4 blocks get Nausea | 10% less maximum health | Head: a green mist at the mouth |
| Leech Jar | Bog Hag | `wolf` | Each potion hit heals it 1 health | 2 less armor | Body: a jar of leeches on the belt. This changes the model |

## Gene details

| Gene | Rules |
|---|---|
| All | "Thrown potions" are the splash potions that the witch throws at a target. "Monsters" are the hostile mobs, not players. "Golems" are the iron golem and the snow golem |
| Brewers, Slingers, Long Brew | The duration changes only the effects with a time, not Harming or Healing |
| Slingers, Quick Hand, Long Brew, Double Throw | "Throws more often" or "less often" changes the time between two throws |
| Alchemists, Healing Draught | A drink takes the vanilla drink time, and the witch is slow while it drinks |
| Covens, Mending Hand | It throws only at a monster that is not its target. In a raid, it still heals the raiders as in vanilla |
| Strong Brew | It does not change Harming |
| Lingering Brew | A lingering potion leaves a cloud, as the lingering potion of a player does |
| Long Arm | It works as the pillager Long Bolt |
| Sure Aim | It works as the skeleton arrow spread at `0` |
| Coven Call | It works as the pillager War Horn |
| Coven Ward | It works as the pillager Banner Bearer, with Resistance |
| Blood Pact | It works as the vindicator Bodyguard |
| Bog Circle | Only the witches in water get the effect |
| Last Rites | It works as the stray heal on the death of an ally |
| Rain Dance | It works as a regeneration while it rains, also out of combat |
| Bog Breath | It works as the evoker Weak Hex, with Nausea |
| Leech Jar | A potion hit is a thrown potion that splashes on a target |

## Equipment

- The witch holds a potion in its hand while it drinks, as in vanilla. A gene
  gives it no weapon.
- The witch drops its vanilla items.
- A witch cannot be the leader of a raid wave. Only a pillager with a Captain
  gene can be, as the pillager design says.

## Conversion

- A villager that lightning hits becomes a witch, as in vanilla. The new
  witch gets genes as a new mob.
- The witch does not convert to a different species.

## Babies and jockeys

- Witches have no babies in vanilla. They do not get a gene for them.
- A witch does not ride a mob in vanilla. It does not get a gene for this.

## Body

- The witch has the witch model: the villager body with a hat, a nose, and
  arms that are always crossed. Its texture is 64 x 128.
- It needs a new region map, `textures/region/witch.png`.
- The gene models use the witch bones: `head`, `hat`, `nose`, `body`, `arms`,
  `right_leg`, and `left_leg`. The arms are one part, and they are always
  crossed.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Quick Brewers | Brewer | Slinger | Strong potions, thrown fast |
| Tonic Brewers | Brewer | Alchemist | Strong potions, and drinks to live |
| Coven Brewers | Brewer | Coven | Strong potions for the coven |
| Swamp Brewers | Brewer | Bog Hag | Strong potions from the swamp |
| Potion Slingers | Slinger | Brewer | Fast throws of strong potions |
| Flask Slingers | Slinger | Alchemist | Fast throws, and drinks to live |
| Coven Slingers | Slinger | Coven | Fast throws for the coven |
| Bog Slingers | Slinger | Bog Hag | Fast throws from the water |
| Poison Tasters | Alchemist | Brewer | Drinks, with strong potions |
| Flask Throwers | Alchemist | Slinger | Drinks, with fast throws |
| Coven Healers | Alchemist | Coven | Drinks that the coven shares |
| Swamp Healers | Alchemist | Bog Hag | Drinks in the swamp |
| High Witches | Coven | Brewer | A coven with strong potions |
| Coven Raiders | Coven | Slinger | A coven with fast throws |
| Elder Witches | Coven | Alchemist | A coven that does not fall |
| Marsh Covens | Coven | Bog Hag | A coven of the swamp |
| Fen Brewers | Bog Hag | Brewer | Swamp potions |
| Fen Slingers | Bog Hag | Slinger | Fast throws from the swamp |
| Fen Healers | Bog Hag | Alchemist | A swamp witch that heals |
| Fen Covens | Bog Hag | Coven | The swamp coven |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Brewers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Potion Masters | Brewer and Slinger | Strong potions, thrown fast |
| Apothecaries | Alchemist and Brewer | Strong potions for others and for itself |
| Grand Covens | Brewer and Coven | Strong potions for the coven |
| Swamp Witches | Bog Hag and Brewer | Strong potions from the swamp |
| Flask Jugglers | Alchemist and Slinger | Fast throws and drinks |
| Coven Slingshots | Coven and Slinger | Fast throws for the coven |
| Marsh Slingers | Bog Hag and Slinger | Fast throws from the water |
| Wise Women | Alchemist and Coven | Heals for itself and for the coven |
| Bog Healers | Alchemist and Bog Hag | Drinks in the swamp |
| Swamp Covens | Bog Hag and Coven | The coven of the swamp |

## Neutral

A neutral witch has no genes, or no family points.
It is a plain witch, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A witch region map, and the gene textures and gene models on the witch model | All visuals |
| The duration of the thrown potions | Brewers, Slingers, Long Brew |
| A stronger level of the thrown potions | Strong Brew |
| Lingering potions in place of splash potions | Lingering Brew |
| A change of the thrown potion: Blindness in place of Slowness, and Wither with Harming | Blinding Brew, Wither Brew |
| More potion damage to a tag of mobs | Rust Brew |
| A check that `throw_rate`, `throw_range`, and `arrow_spread` work on the potions of a witch | Slingers, Long Arm, Quick Hand, Sure Aim |
| Two potions in one throw | Double Throw |
| A wider splash | Wide Splash |
| Damage when a potion breaks on a target | Glass Shards |
| A drink: a potion that the witch drinks in a state, with a cooldown | Alchemists, Healing Draught, Fire Draught, Swift Draught, Shade Draught, Feather Draught |
| The effect of a drink, shared with the monsters near it | Shared Draught |
| A potion of Healing thrown at a hurt monster | Covens, Mending Hand |
| A heal on each potion hit | Leech Jar |
| A check that the reused effects work on a witch: call on sight, aura, share damage, heal on ally death, regenerate in the rain, speed in water, attributes, and the damage taken effects | Many genes |
