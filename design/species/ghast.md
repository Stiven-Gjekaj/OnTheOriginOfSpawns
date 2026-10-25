# Ghast

This document defines the families and the genes of the ghast.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The ghast is a large, white flyer of the Nether. It has 10 health. It sees a
player from far away, and it shoots fireballs that explode where they hit.
A player can hit a fireball back, and a fireball that comes back kills a
ghast in one hit. Fire does not hurt it. It cries, and it drops ghast tears.
Its genes fit the blast, the rate of fire, the tears that heal, the speed of
its flight, and its cries. It shares no genes and no families with the other
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

### Bombers

Bombers make large blasts. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its fireballs make a 50% larger blast | 10% less maximum health |
| Tier 2 | Its fireballs make a 30% larger blast | 15% less maximum health |
| Tier 1 | Its fireballs make a 20% larger blast | 20% less maximum health |

### Barragers

Barragers shoot often. Each blast is small.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It shoots 30% more often | Its fireballs make a 10% smaller blast |
| Tier 2 | It shoots 20% more often | Its fireballs make a 15% smaller blast |
| Tier 1 | It shoots 15% more often | Its fireballs make a 20% smaller blast |

### Weepers

Weepers heal with their tears. The tears make them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It heals 1 health each 2 seconds out of combat | 10% less flying speed |
| Tier 2 | It heals 1 health each 3 seconds out of combat | 15% less flying speed |
| Tier 1 | It heals 1 health each 4 seconds out of combat | 20% less flying speed |

### Driftwings

Driftwings fly fast. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more flying speed | 10% more damage taken |
| Tier 2 | 20% more flying speed | 15% more damage taken |
| Tier 1 | 15% more flying speed | 20% more damage taken |

### Moaners

Moaners cry with a sound that hurts the mind. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each 15 seconds, the players in 16 blocks get Nausea for 4 seconds | 10% less maximum health |
| Tier 2 | Each 20 seconds, the players in 16 blocks get Nausea for 4 seconds | 15% less maximum health |
| Tier 1 | Each 25 seconds, the players in 16 blocks get Nausea for 4 seconds | 20% less maximum health |

## Pressures

The ghast uses `projectile`, `melee`, `explosion`, `unreached`, `spotted`,
`alone`, `crowd`, `critical`, `golem`, `player_death`, and `shield`. Fire
does not hurt it, so it does not use `fire`. It flies in the Nether, so it
does not use `sunlight`, `night`, `day`, `fall`, `in_water`, or `on_land`.
It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Bomber genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Great Blast | Bomber | `crowd` | Its fireballs make a two times larger blast | 10% less maximum health | Face: a wide, black mouth |
| Fire Rain | Bomber | `unreached` | Its blasts leave more fire | 10% less maximum health | Tentacles: orange, burnt tips |
| Blast Shell | Bomber | `explosion` | Half the damage from explosions | 10% less flying speed | Body: gray soot on the body |
| Shield Burst | Bomber | `shield` | Its blasts stop the shields in the blast for 5 seconds | 10% less maximum health | Face: red cracks round the mouth |
| Golem Bomb | Bomber | `golem` | Its blasts do two times the damage to golems | 10% less flying speed | Body: iron gray patches |
| Twin Shot | Bomber, sub Barrager | `spotted` | It shoots two fireballs at once, a little apart | It shoots 10% less often | Face: two small, red mouths. This changes the model |

### Barrager genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Quick Fire | Barrager | `spotted` | It shoots 30% more often | 10% less maximum health | Face: narrow, red eyes |
| Far Sight | Barrager | `alone` | Two times the follow range | 10% less flying speed | Face: large, white eyes |
| Spread Fire | Barrager | `crowd` | It shoots three small fireballs in a fan with each big one | It shoots 10% less often | Tentacles: three red rings |
| Hard Fireball | Barrager | `melee` | A player cannot hit its fireballs back | 10% less maximum health | Body: dark, hard spots |
| Steady Aim | Barrager | `unreached` | Its fireballs fly straight at the target, with no spread | 10% less flying speed | Face: a red mark between the eyes |
| Ember Spit | Barrager, sub Weeper | `projectile` | Its fireballs set the target on fire for 4 more seconds | 10% less maximum health | Face: red tear streaks |

### Weeper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Healing Tears | Weeper | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Face: blue tear streaks |
| Soul Tears | Weeper | `alone` | It heals 1 health each 3 seconds out of combat | 10% less flying speed | Face: pale blue eyes |
| Tear Rain | Weeper | `crowd` | The ghasts in 16 blocks get Regeneration | 10% less maximum health | Tentacles: drops at the tips. This changes the model |
| Sad Shell | Weeper | `projectile` | 30% less damage from projectiles | 10% less flying speed | Body: a thick, gray shell |
| Kin Grief | Weeper | `player_death` | When a ghast dies in 32 blocks, it heals 5 health | 10% less maximum health | Face: sad, drooping eyes |
| Grief Cry | Weeper, sub Moaner | `spotted` | When something hurts it, the players in 16 blocks get Slowness for 3 seconds | 10% less maximum health | Face: a wide, crying mouth |

### Driftwing genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Swift Drift | Driftwing | `unreached` | 20% more flying speed | 10% less maximum health | Tentacles: long, thin tentacles. This changes the model |
| Dodge Drift | Driftwing | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Body: light, thin skin |
| High Float | Driftwing | `melee` | It keeps 4 more blocks of height above its target | 10% less damage | Tentacles: short, curled tentacles |
| Haze Skin | Driftwing | `explosion` | It is near invisible in the dark | 10% less maximum health | Body: gray, smoky patches |
| Return Ward | Driftwing | `golem` | A fireball that comes back does 50% less damage to it | 10% less flying speed | Body: a pale blue ward rune |
| Ghost Glide | Driftwing, sub Bomber | `crowd` | It makes no sound before it shoots | 10% less maximum health | Face: closed, quiet eyes |

### Moaner genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Dread Moan | Moaner | `spotted` | Each 15 seconds, the players in 16 blocks get Nausea for 4 seconds | 10% less maximum health | Face: a long, open moan |
| Weak Wail | Moaner | `crowd` | Each 15 seconds, the players in 16 blocks get Weakness for 4 seconds | 10% less maximum health | Face: gray, sunken eyes |
| Echo Call | Moaner | `alone` | When something hurts it, it calls the ghasts in 48 blocks | Players hear it from two times farther | Tentacles: ripple lines |
| Dark Cry | Moaner | `unreached` | Each 20 seconds, the players in 16 blocks get Darkness for 3 seconds | 10% less maximum health | Face: black, empty eyes |
| Shrill Cry | Moaner | `player_death` | Each 20 seconds, the players in 16 blocks get Mining Fatigue for 6 seconds | 10% less flying speed | Face: a small, round mouth |
| Blast Cry | Moaner, sub Driftwing | `explosion` | When it shoots, the players in 8 blocks of the blast get Slowness for 3 seconds | 10% less maximum health | Body: red rings on the body |

## Gene details

| Gene | Rules |
|---|---|
| Bombers, Great Blast | The vanilla blast has a power of 1. The blast breaks blocks only if the `mobGriefing` gamerule is on, as in vanilla |
| Barragers, Quick Fire | "Shoots more often" changes the time between two fireballs |
| Driftwings, Swift Drift | "Flying speed" is the speed of the flight of the ghast |
| Fire Rain | The blast sets more blocks on fire, as a larger fire, if the `mobGriefing` gamerule is on |
| Shield Burst | It works as the slime Shield Melt, for each player in the blast |
| Golem Bomb | "Golems" are the iron golem and the snow golem |
| Twin Shot, Spread Fire | The extra fireballs follow the rules of the main one |
| Hard Fireball | The player can hit the fireball, but it does not turn back |
| Steady Aim | It works as the skeleton arrow spread at `0` |
| Healing Tears | It works as the vindicator Death Wish |
| Soul Tears, Weepers | They work as the vindicator Thick Blood |
| Tear Rain | It works as the witch Coven Ward, with Regeneration |
| Kin Grief | It works as the stray Mourner |
| Grief Cry | It works as the evoker Smoke Veil, with Slowness |
| Dodge Drift | It works as the pillager Evasion, in the air |
| High Float | The ghast keeps this much more height when it shoots |
| Haze Skin | It works as the creeper Gloom |
| Return Ward | "Comes back" is a fireball that a player hits back |
| Ghost Glide | The vanilla ghast makes a warning sound before it shoots |
| Moaners, Dread Moan, Weak Wail, Dark Cry, Shrill Cry | They work as the vindicator Howl, with their effects |
| Echo Call | It works as the vindicator Alarm Bell |

## Equipment

- The ghast holds nothing. It drops its vanilla items.

## Conversion

- The ghast does not convert to a different species, and no species
  converts to the ghast. A happy ghast is a different species, and it does
  not get genes.

## Babies and jockeys

- The hostile ghast has no babies. It does not get a gene for them.
- A ghast does not ride a mob. It does not get a gene for this.

## Body

- The ghast has the ghast model: a large body cube, and nine tentacles. Its
  face shows a closed or an open, shooting mouth. Its texture is 64 x 32.
- It needs a new region map, `textures/region/ghast.png`. The design uses
  the regions body, face, and tentacles. The shooting face is a second
  texture, so a face mark goes on both.
- The gene models use the ghast bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Rapid Bombers | Bomber | Barrager | Large blasts, shot fast |
| Weeping Bombers | Bomber | Weeper | Large blasts that heal it |
| Drifting Bombers | Bomber | Driftwing | Large blasts from fast wings |
| Moaning Bombers | Bomber | Moaner | Large blasts and cries |
| Blast Barragers | Barrager | Bomber | Fast fire with large blasts |
| Weeping Barragers | Barrager | Weeper | Fast fire that heals it |
| Drifting Barragers | Barrager | Driftwing | Fast fire from fast wings |
| Moaning Barragers | Barrager | Moaner | Fast fire and cries |
| Blast Weepers | Weeper | Bomber | Tears and large blasts |
| Rapid Weepers | Weeper | Barrager | Tears and fast fire |
| Drifting Weepers | Weeper | Driftwing | Tears on fast wings |
| Moaning Weepers | Weeper | Moaner | Tears and cries |
| Blast Drifters | Driftwing | Bomber | Fast wings and large blasts |
| Rapid Drifters | Driftwing | Barrager | Fast wings and fast fire |
| Weeping Drifters | Driftwing | Weeper | Fast wings that heal |
| Moaning Drifters | Driftwing | Moaner | Fast wings and cries |
| Blast Moaners | Moaner | Bomber | Cries and large blasts |
| Rapid Moaners | Moaner | Barrager | Cries and fast fire |
| Weeping Moaners | Moaner | Weeper | Cries and tears |
| Drifting Moaners | Moaner | Driftwing | Cries on fast wings |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Bombers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Firestorms | Barrager and Bomber | Large blasts, shot fast |
| Weeping Bombs | Bomber and Weeper | Large blasts that heal |
| Sky Bombs | Bomber and Driftwing | Large blasts from fast wings |
| Howling Bombs | Bomber and Moaner | Large blasts and cries |
| Ember Tears | Barrager and Weeper | Fast fire that heals |
| Strafing Wings | Barrager and Driftwing | Fast fire from fast wings |
| Wailing Volleys | Barrager and Moaner | Fast fire and cries |
| Drifting Tears | Driftwing and Weeper | Tears on fast wings |
| Mourning Choirs | Moaner and Weeper | Tears and cries |
| Drift Wailers | Driftwing and Moaner | Cries on fast wings |

## Neutral

A neutral ghast has no genes, or no family points.
It is a plain ghast, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A ghast region map, and the gene textures and gene models on the ghast model, with both faces | All visuals |
| The blast size of its fireballs | Bombers, Barragers, Great Blast |
| More fire from the blast | Fire Rain |
| A shield stop and more golem damage in the blast | Shield Burst, Golem Bomb |
| The time between its fireballs | Barragers, Quick Fire, and the downsides |
| Two fireballs, and small fireballs in a fan | Twin Shot, Spread Fire |
| A fireball that a player cannot hit back | Hard Fireball |
| No spread of its fireballs | Steady Aim |
| More fire time from its fireballs | Ember Spit |
| The flying speed of the ghast | Driftwings, Swift Drift, and the downsides |
| A sidestep in the air | Dodge Drift |
| More height when it shoots | High Float |
| Less damage from a fireball that comes back | Return Ward |
| No warning sound before it shoots | Ghost Glide |
| An effect on the players near the blast | Blast Cry |
| A check that the reused effects work on a ghast: regenerate, heal once, aura, heal on ally death, effect on hurt, translucent, calls, and the damage taken effects | Many genes |
