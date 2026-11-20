# Creaking

This document defines the families and the genes of the creaking.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The creaking is a tree mob of the pale garden. A creaking heart in a pale oak
log makes it at night when a player is in 32 blocks. It has 1 health, but no
damage hurts it while its heart lives. When a player hurts it, its heart
shows a trail of particles and grows resin. When its heart breaks, the
creaking crumbles. A player that looks at it from 12 blocks or less wakes it.
While a player looks at it, it does not move. It does 3 damage, one time each
2 seconds, and it stays in 32 blocks of its heart. Its genes fit the twitch
while watched, the thorns, the heart, the prowl, and the stare. It shares no
genes and no families with the other species.

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

### Twitchers

Twitchers move a little while a player looks at them. They attack slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | While a player looks at it, it takes one step each 2 seconds | It attacks 10% slower |
| Tier 2 | While a player looks at it, it takes one step each 4 seconds | It attacks 15% slower |
| Tier 1 | While a player looks at it, it takes one step each 5 seconds | It attacks 20% slower |

### Thornwoods

Thornwoods have sharp thorns. They are slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage | 10% less speed |
| Tier 2 | 20% more damage | 15% less speed |
| Tier 1 | 15% more damage | 20% less speed |

### Heartkeepers

Heartkeepers have a hard heart. They stay near it.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its heart takes 100% more time to break | It stays in 28 blocks of its heart, not 32 |
| Tier 2 | Its heart takes 50% more time to break | It stays in 24 blocks of its heart, not 32 |
| Tier 1 | Its heart takes 30% more time to break | It stays in 20 blocks of its heart, not 32 |

### Slinkers

Slinkers move fast in the dark. Their hits are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed | 10% less damage |
| Tier 2 | 20% more speed | 15% less damage |
| Tier 1 | 15% more speed | 20% less damage |

### Starers

Starers wake from far away. A look holds them for a short time.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | A player that looks at it from 24 blocks or less wakes it, not 12 | It stays still for 0.5 seconds after a player looks away |
| Tier 2 | A player that looks at it from 18 blocks or less wakes it, not 12 | It stays still for 1 second after a player looks away |
| Tier 1 | A player that looks at it from 16 blocks or less wakes it, not 12 | It stays still for 1.5 seconds after a player looks away |

## Pressures

A creaking dies when its heart breaks. The death counts for the pressures of
the heart break: the player that breaks the heart is the killer, and a blast
that breaks the heart counts for `explosion`. The creaking uses `explosion`,
`unreached`, `spotted`, `alone`, `shield`, and `player_death`.
No damage hurts it, so it does not use `fire`, `projectile`, `melee`,
`fall`, `critical`, or `golem`. It comes only at night, so `night` is true for
each death, and the creaking does not use it.

It needs two new pressures:

| Pressure | Condition | True when |
|---|---|---|
| `heart_far` | `heart_far` | The creaking was more than 16 blocks from its heart when the heart broke |
| `heart_trail` | `heart_trail` | A player hurt the creaking, and its heart showed the trail, in the 30 seconds before the heart broke |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Twitcher genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Jitter Step | Twitcher | `spotted` | While a player looks at it, it takes one step each 3 seconds | It attacks 10% slower | Head: a crooked crack on the face |
| Blink Strike | Twitcher | `unreached` | When a player looks away from it in 2 blocks, its next hit comes at once | It attacks 10% slower | Arms: long splinter fingers |
| Shadow Creep | Twitcher | `alone` | In the dark, a look from more than 6 blocks does not stop it | It attacks 10% slower | Body: dark bark on the chest |
| Fear Twitch | Twitcher | `player_death` | After it hits a player, a look from that player does not stop it for 2 seconds | It attacks 10% slower | Head: white lines round the eyes |
| Trail Twitch | Twitcher | `heart_trail` | When a player hurts it, it moves for 1 second, also while a player looks at it | It attacks 10% slower | Legs: twisted legs |
| Slip Step | Twitcher, sub Slinker | `shield` | When a player looks away, it gets 50% more speed for 2 seconds | It attacks 10% slower | Legs: pale moss on the legs |

### Thornwood genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Thorn Fist | Thornwood | `spotted` | Its hits do 2 more damage | 10% less speed | Arms: thorns on the arms |
| Shield Splinter | Thornwood | `shield` | Its hits disable a shield for 3 seconds, as an axe does | 10% less speed | Arms: dark, heavy hands |
| Splinter Burst | Thornwood | `heart_far` | When its heart breaks, splinters fly out of it and do 4 damage to the players in 3 blocks | 10% less speed | Body: splinters on the chest |
| Sap Bite | Thornwood | `alone` | Its hits give Poison for 3 seconds | 10% less speed | Arms: amber sap on the arms |
| Long Limbs | Thornwood | `unreached` | It hits from 1 block farther | 10% less speed | Arms: long arms. This changes the model |
| Thorn Heart | Thornwood, sub Heartkeeper | `heart_trail` | A player that breaks its heart takes 4 damage | 10% less speed | Body: a red knot on the chest |

### Heartkeeper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Hard Heart | Heartkeeper | `explosion` | Its heart takes 50% more time to break, and explosions do not break it | It stays in 4 fewer blocks of its heart | Body: a thick bark plate on the chest |
| Hidden Heart | Heartkeeper | `heart_trail` | When a player hurts it, its heart shows no trail and grows no resin | It stays in 4 fewer blocks of its heart | Head: closed bark over the mouth |
| Close Guard | Heartkeeper | `heart_far` | When a player comes in 8 blocks of its heart, it goes back to the heart with 50% more speed | It stays in 4 fewer blocks of its heart | Legs: root feet |
| Root Grip | Heartkeeper | `unreached` | Its hits give Slowness II for 2 seconds | It stays in 4 fewer blocks of its heart | Arms: root bands round the arms |
| Dark Heart | Heartkeeper | `spotted` | Its heart does not glow | It stays in 4 fewer blocks of its heart | Body: a dark core on the chest |
| Heart Sense | Heartkeeper, sub Starer | `alone` | When a player comes in 16 blocks of its heart, it wakes and targets that player, also with no look | It stays in 4 fewer blocks of its heart | Head: orange eye rims |

### Slinker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Quick Bark | Slinker | `unreached` | 20% more speed | 10% less damage | Legs: pale stripes on the legs |
| Silent Roots | Slinker | `alone` | Players hear its steps only from 4 blocks | 10% less damage | Legs: moss on the feet |
| Flank Walk | Slinker | `shield` | It walks round to the side of a player that holds a shield up | 10% less damage | Arms: bark bands on the arms |
| Hunt Lust | Slinker | `player_death` | After it hits a player, 30% more speed for 5 seconds | 10% less damage | Head: a red stripe on the head |
| Dusk Bark | Slinker | `spotted` | Its eyes do not glow while no player looks at it | 10% less damage | Head: dark shades over the eyes |
| Blast Sense | Slinker, sub Starer | `explosion` | When TNT is lit in 16 blocks of its heart, it wakes and targets the nearest player, also with no look | 10% less damage | Body: gray ash on the chest |

### Starer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Stare | Starer | `unreached` | A player that looks at it from 24 blocks or less wakes it | It stays still for 0.5 seconds after a player looks away | Head: wide eyes |
| Stare Back | Starer | `spotted` | When a player looks at it from 8 blocks or less, the player gets Darkness for 3 seconds, one time each 10 seconds | It stays still for 0.5 seconds after a player looks away | Head: a third eye on the brow |
| Lone Watch | Starer | `alone` | When only one player is in 16 blocks, it hears that player and targets it, also with no look | It stays still for 0.5 seconds after a player looks away | Body: pale knots like eyes |
| Shield Stare | Starer | `shield` | A look from a player that holds a shield up does not stop it | It stays still for 0.5 seconds after a player looks away | Arms: eye knots on the arms |
| Kill Watch | Starer | `player_death` | After it kills a player, it targets the nearest other player in 24 blocks | It stays still for 0.5 seconds after a player looks away | Head: red eye glints |
| Heart Twitch | Starer, sub Twitcher | `heart_far` | When a player comes in 8 blocks of its heart, it moves for 3 seconds, also while a player looks at it | It stays still for 0.5 seconds after a player looks away | Legs: bright cracks on the legs |

## Gene details

| Gene | Rules |
|---|---|
| Twitchers, Jitter Step | A step is one block toward its target |
| Blink Strike | In vanilla, it hits one time each 2 seconds |
| Shadow Creep | "The dark" is a block light of 4 or less at the creaking |
| Trail Twitch, Heart Twitch | It works one time each 10 seconds |
| Thornwoods, Thorn Fist | The vanilla hit does 3 damage |
| Shield Splinter | It works as the vindicator axe on a shield |
| Splinter Burst | It does no damage to other creakings |
| Long Limbs | The vanilla reach of a mob hit is about 1 block past its body |
| Thorn Heart | The damage comes when the heart breaks, and it does not stop the break |
| Heartkeepers, Hard Heart | The break time is the time for a player with an axe |
| Hidden Heart | In vanilla, the heart shows a trail and grows 2 or 3 resin clumps |
| Close Guard | It works one time each 10 seconds |
| Dark Heart | The heart looks as a dormant heart while it is awake |
| Heart Sense, Blast Sense, Lone Watch | It wakes as a vanilla creaking wakes when a player looks at it |
| Silent Roots | Its other sounds do not change |
| Flank Walk | It walks to a place at the side of the player, and then it attacks |
| Starers, Long Stare | In vanilla, a look from 12 blocks or less wakes it. When it is awake, a look from any distance stops it |
| Stare Back | Darkness is the vanilla effect of the warden |
| Shield Stare | The look of a player with no shield up still stops it |

## Equipment

- The creaking holds nothing and drops nothing, as in vanilla. Its heart drops
  its vanilla items.

## Conversion

- The creaking does not convert to a different species, and no species
  converts to the creaking.

## Babies and jockeys

- The creaking has no babies. It does not get a gene for them.
- A creaking does not ride a mob. It does not get a gene for this.

## Spawning

- A creaking that a creaking heart makes gets genes as a natural spawn.
- When its heart makes it again on a new night, it gets new genes from the
  pool.
- A creaking from a spawn egg has no heart. It has 1 health, and the damage
  that kills it counts for the pressures as for other mobs. The heart genes do
  nothing on it.

## Body

- The creaking has the creaking model: a head, a body, two arms, and two
  legs. Its texture is 64 x 64, and it has a glow layer for the eyes.
- It needs a new region map, `textures/region/creaking.png`. The design uses
  the regions head, body, arms, and legs.
- The gene models use the creaking bones. Check them against the vanilla
  model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Thorned Twitchers | Twitcher | Thornwood | A move while watched and a hard hit |
| Rooted Twitchers | Twitcher | Heartkeeper | A move while watched and a hard heart |
| Slinking Twitchers | Twitcher | Slinker | A move while watched and a fast slink |
| Staring Twitchers | Twitcher | Starer | A move while watched and a far stare |
| Twitching Thornwoods | Thornwood | Twitcher | A hard hit and a move while watched |
| Rooted Thornwoods | Thornwood | Heartkeeper | A hard hit and a hard heart |
| Slinking Thornwoods | Thornwood | Slinker | A hard hit and a fast slink |
| Staring Thornwoods | Thornwood | Starer | A hard hit and a far stare |
| Twitching Heartkeepers | Heartkeeper | Twitcher | A hard heart and a move while watched |
| Thorned Heartkeepers | Heartkeeper | Thornwood | A hard heart and a hard hit |
| Slinking Heartkeepers | Heartkeeper | Slinker | A hard heart and a fast slink |
| Staring Heartkeepers | Heartkeeper | Starer | A hard heart and a far stare |
| Twitching Slinkers | Slinker | Twitcher | A fast slink and a move while watched |
| Thorned Slinkers | Slinker | Thornwood | A fast slink and a hard hit |
| Rooted Slinkers | Slinker | Heartkeeper | A fast slink and a hard heart |
| Staring Slinkers | Slinker | Starer | A fast slink and a far stare |
| Twitching Starers | Starer | Twitcher | A far stare and a move while watched |
| Thorned Starers | Starer | Thornwood | A far stare and a hard hit |
| Rooted Starers | Starer | Heartkeeper | A far stare and a hard heart |
| Slinking Starers | Starer | Slinker | A far stare and a fast slink |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Twitchers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Snap Thorns | Twitcher and Thornwood | A move while watched and a hard hit |
| Root Jitters | Twitcher and Heartkeeper | A move while watched and a hard heart |
| Shade Steps | Twitcher and Slinker | A move while watched and a fast slink |
| Blink Eyes | Twitcher and Starer | A move while watched and a far stare |
| Bramble Hearts | Thornwood and Heartkeeper | A hard hit and a hard heart |
| Briar Hunters | Thornwood and Slinker | A hard hit and a fast slink |
| Thorn Eyes | Thornwood and Starer | A hard hit and a far stare |
| Grove Watchmen | Heartkeeper and Slinker | A hard heart and a fast slink |
| Pale Sentinels | Heartkeeper and Starer | A hard heart and a far stare |
| Pale Lurkers | Slinker and Starer | A fast slink and a far stare |

## Neutral

A neutral creaking has no genes, or no family points.
It is a plain creaking, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A creaking region map, and the gene textures and gene models on the creaking model | All visuals |
| A creaking death from a broken heart that counts for the pressures of the heart break | All genes |
| The `heart_far` and `heart_trail` pressures | The genes that use them |
| A step while a player looks at it, a move for a short time while watched, and a look that does not stop it | Twitchers, Jitter Step, Shadow Creep, Fear Twitch, Trail Twitch, Heart Twitch, Shield Stare |
| A hit at once when a player looks away, and speed when a player looks away | Blink Strike, Slip Step |
| The break time of the heart, a heart that explosions do not break, damage to the player that breaks the heart, and splinters when the heart breaks | Heartkeepers, Hard Heart, Thorn Heart, Splinter Burst |
| The roam range from the heart, and a return to the heart | Heartkeepers, Close Guard, and the downsides |
| No trail and no resin from the heart, and a heart that does not glow | Hidden Heart, Dark Heart |
| A wake with no look, from a player near the heart, from lit TNT, and from one player alone | Heart Sense, Blast Sense, Lone Watch |
| The wake range, and a still time after a player looks away | Starers, Long Stare, and the downsides |
| Darkness on a player that looks at it | Stare Back |
| Eyes that do not glow while no player looks at it | Dusk Bark |
| The time between two hits, and the reach of a hit | Twitchers, Long Limbs, and the downsides |
| A check that the reused effects work on a creaking: speed, damage, shield disable, Poison, Slowness, quiet steps, flanks, and kill targets | Many genes |
