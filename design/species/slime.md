# Slime

This document defines the families and the genes of the slime.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The slime is a green cube that hops. It spawns at size 1, 2, or 4. Its
health is its size times its size: 1, 4, or 16. Its damage is its size, and
a slime of size 1 does no damage. When a slime of size 2 or more dies, it
splits into 2 to 4 slimes of half its size. It spawns in swamps at night and
deep in slime chunks.
Its genes fit its size, the split, the glue of its body, the hop, and the
acid of its gel. It shares no genes and no families with the other species.

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

### Giants

Giants grow big. A big body hops less often.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It spawns at size 4 with a 50% chance | It hops 10% less often |
| Tier 2 | It spawns at size 4 with a 45% chance | It hops 15% less often |
| Tier 1 | It spawns at size 4 with a 40% chance | It hops 20% less often |

### Splitters

Splitters break into many pieces. Each piece is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When it splits, it makes 2 more slimes | 10% less maximum health |
| Tier 2 | When it splits, it makes 1 more slime | 15% less maximum health |
| Tier 1 | When it splits, it makes 1 more slime | 20% less maximum health |

### Gluers

Gluers stick to their prey. Their hits are soft.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits give Slowness for 4 seconds | 10% less damage |
| Tier 2 | Its hits give Slowness for 3 seconds | 15% less damage |
| Tier 1 | Its hits give Slowness for 2 seconds | 20% less damage |

### Bouncers

Bouncers hop far. Their gel is thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It hops 30% farther and higher | 10% less maximum health |
| Tier 2 | It hops 20% farther and higher | 15% less maximum health |
| Tier 1 | It hops 15% farther and higher | 20% less maximum health |

### Acids

Acids eat through armor. Fire boils their gel.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits damage armor two times as much | 25% more fire damage |
| Tier 2 | Its hits damage armor 1.75 times as much | 50% more fire damage |
| Tier 1 | Its hits damage armor 1.5 times as much | 75% more fire damage |

## Pressures

The slime uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `night`, `day`, `wolf`, and `shield`.
The sun does not burn it, so it does not use `sunlight`. It needs no new
pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Giant genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Great Blob | Giant | `crowd` | It spawns at size 4 with a 50% chance | It hops 10% less often | Body: a darker, thick gel |
| Heavy Body | Giant | `explosion` | Full knockback resistance | 10% less speed | Core: a large, dark core |
| Thick Gel | Giant | `melee` | 2 more armor | 10% less speed | Body: thick, cloudy layers in the gel |
| Swell | Giant | `alone` | After 60 seconds out of combat, it grows one size, up to size 4 | 10% less maximum health | Core: a swollen core |
| Mother Gel | Giant, sub Splitter | `critical` | When it splits, its pieces get Resistance for 10 seconds | It hops 10% less often | Body: small cubes in the gel. This changes the model |
| Wide Reach | Giant | `unreached` | It hits from 1 block farther | 10% less damage | Face: a wide, open mouth |

### Splitter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Many Pieces | Splitter | `melee` | When it splits, it always makes 4 slimes | 10% less maximum health | Body: cracks in the gel |
| Tiny Teeth | Splitter | `spotted` | Its slimes of size 1 do 1 damage | 10% less maximum health | Face: small teeth |
| Quick Split | Splitter | `projectile` | When it splits, its pieces get Speed for 5 seconds | 10% less damage | Body: light green streaks |
| Scatter | Splitter | `explosion` | When it splits, its pieces jump 3 blocks apart | 10% less speed | Core: a cracked core |
| Gel Trap | Splitter, sub Gluer | `crowd` | When it splits, the players in 3 blocks get Slowness for 3 seconds | 10% less maximum health | Body: gel strings on the surface. This changes the model |
| Bud | Splitter | `alone` | Each 30 seconds out of combat, a slime of size 1 buds off it | 10% less maximum health | Body: small buds on the sides. This changes the model |

### Gluer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Sticky Hit | Gluer | `melee` | Its hits give Slowness II for 2 seconds | 10% less damage | Body: a shiny, wet gel |
| Gel Trail | Gluer | `unreached` | It leaves a cobweb behind it now and then | 10% less speed | Body: drips at the bottom. This changes the model |
| Gum Skin | Gluer | `projectile` | 30% less damage from projectiles | 10% less speed | Body: a yellow-green gum gel |
| Glue Grip | Gluer | `spotted` | Its hits pull a target in the air down | 10% less damage | Face: a long tongue. This changes the model |
| Sticky Pounce | Gluer, sub Bouncer | `crowd` | When it lands next to a player, the player gets Slowness for 3 seconds | It hops 10% less often | Body: a sticky bottom |
| Cling | Gluer | `alone` | It climbs walls | 10% less maximum health | Body: gel feet on the sides |

### Bouncer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| High Hop | Bouncer | `fall` | It hops two times as high | 10% less maximum health | Body: a bright, light gel |
| Long Hop | Bouncer | `unreached` | It hops 50% farther | 10% less maximum health | Body: long, thin streaks |
| Quick Hop | Bouncer | `spotted` | It hops 30% more often | 10% less damage | Face: wide eyes |
| Slam | Bouncer | `melee` | When it lands, it does 2 damage to the players in 2 blocks | It hops 10% less often | Core: a heavy core at the bottom |
| Water Hop | Bouncer | `in_water` | 30% more speed in water | 10% less maximum health | Body: blue tint in the gel |
| Acid Landing | Bouncer, sub Acid | `shield` | When it lands, it damages the armor of the players in 2 blocks | 10% less maximum health | Body: a yellow bottom |

### Acid genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Acid Touch | Acid | `melee` | Its hits damage armor two times as much | 10% less maximum health | Body: a yellow-green gel |
| Shield Melt | Acid | `shield` | Its hits stop a shield for 5 seconds | 10% less speed | Face: a yellow mouth |
| Acid Blood | Acid | `critical` | When a player hurts it in melee, the player gets Poison for 2 seconds | 25% more fire damage | Core: a yellow core |
| Burning Gel | Acid | `fire` | Half the damage from fire | 10% less maximum health | Body: orange spots in the gel |
| Acid Pool | Acid | `crowd` | When it dies, it leaves a cloud of Poison | 10% less maximum health | Body: bubbles in the gel. This changes the model |
| Dissolve | Acid, sub Giant | `golem` | Two times the damage to golems | 10% less speed | Face: green, sunken eyes |

## Gene details

| Gene | Rules |
|---|---|
| All | The slimes of a split have the genes of the slime that split. They are not new mobs, so they do not get genes from the pool. A gene of a slime acts at each size, unless its rules say different |
| Giants, Great Blob | Vanilla gives size 1, 2, and 4 the same chance. The other sizes share the rest of the chance |
| Giants, Mother Gel, Sticky Pounce, Slam | "Hops less often" changes the wait between two hops |
| Splitters, Many Pieces | Vanilla makes 2 to 4 slimes |
| Tiny Teeth | A slime of size 1 does no damage in vanilla |
| Swell | It grows one size at a time: 1 to 2, then 2 to 4. It heals to the health of its new size |
| Bud | The new slime has the genes of the slime that made it. It does not make buds |
| Wide Reach | It works on the touch attack of the slime |
| Gel Trail | It works as the spider Tripwire |
| Glue Grip | It works as the drowned Undertow |
| Cling | It works as the vindicator Climber |
| Acid Touch, Acids | They work as the cave spider Corrosive |
| Acid Blood | It works as the bogged Rotting Touch |
| Acid Pool | It works as the zombie Bloat |
| Dissolve | "Golems" are the iron golem and the snow golem |

## Equipment

- The slime holds nothing. A slime of size 1 drops slimeballs, as in vanilla.

## Conversion

- The slime does not convert to a different species, and no species
  converts to the slime. A split is not a conversion. The pieces keep the
  genes.

## Babies and jockeys

- Slimes have no babies. The small slimes of a split are not babies.
- A slime does not ride a mob. It does not get a gene for this.

## Body

- The slime has the slime model: an outer cube of clear gel, an inner cube,
  two eyes, and a mouth. Its texture is 64 x 32. The model scales with its
  size.
- It needs a new region map, `textures/region/slime.png`. The design uses
  the regions body (the outer gel), core (the inner cube), and face (the eyes
  and the mouth).
- The outer gel is clear. A mark on it shows as a tint. Check the gene
  models against the vanilla model, as for the witch.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Broken Giants | Giant | Splitter | A big slime that breaks into many |
| Glue Giants | Giant | Gluer | A big slime that holds on |
| Jumping Giants | Giant | Bouncer | A big slime that hops far |
| Acid Giants | Giant | Acid | A big slime that melts armor |
| Swollen Splitters | Splitter | Giant | Big splits |
| Glue Splitters | Splitter | Gluer | Sticky pieces |
| Hopping Splitters | Splitter | Bouncer | Pieces that hop far |
| Acid Splitters | Splitter | Acid | Acid pieces |
| Big Gluers | Gluer | Giant | A big, sticky body |
| Gum Splitters | Gluer | Splitter | Sticky pieces everywhere |
| Spring Gluers | Gluer | Bouncer | A sticky hop |
| Acid Gluers | Gluer | Acid | A sticky acid |
| Heavy Bouncers | Bouncer | Giant | A big hop |
| Split Bouncers | Bouncer | Splitter | Hops that break apart |
| Sticky Bouncers | Bouncer | Gluer | Hops that hold |
| Acid Bouncers | Bouncer | Acid | Hops that melt |
| Great Acids | Acid | Giant | A big pool of acid |
| Split Acids | Acid | Splitter | Acid in pieces |
| Glue Acids | Acid | Gluer | A sticky acid |
| Hop Acids | Acid | Bouncer | Acid that hops |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Giants".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Slime Kings | Giant and Splitter | A big slime that breaks into a swarm |
| Gel Walls | Giant and Gluer | A big wall of glue |
| Hill Hoppers | Bouncer and Giant | A big slime that hops high |
| Acid Kings | Acid and Giant | A big slime of acid |
| Gum Swarms | Gluer and Splitter | A swarm of sticky pieces |
| Rubber Swarms | Bouncer and Splitter | A swarm that hops |
| Acid Swarms | Acid and Splitter | A swarm of acid |
| Glue Springs | Bouncer and Gluer | A hop that holds |
| Tar Pits | Acid and Gluer | A sticky pit of acid |
| Acid Springs | Acid and Bouncer | A hop of acid |

## Neutral

A neutral slime has no genes, or no family points.
It is a plain slime, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet. The magma
cube needs most of them too.

| Need | Used by |
|---|---|
| A slime region map, and the gene textures and gene models on the slime model, at each size | All visuals |
| The pieces of a split keep the genes of the slime that split | All genes |
| The chance of the spawn size | Giants, Great Blob |
| The hop: how often, how far, and how high | Giants, Bouncers, High Hop, Long Hop, Quick Hop, Mother Gel, Sticky Pounce, Slam |
| Effects when it lands: damage, an effect, and armor damage near it | Slam, Sticky Pounce, Acid Landing |
| The split: more pieces, a fixed count, effects on the pieces, a jump of the pieces, and an effect on the players near it | Splitters, Many Pieces, Quick Split, Scatter, Mother Gel, Gel Trap |
| Damage from a slime of size 1 | Tiny Teeth |
| Growth out of combat | Swell |
| A bud: a new size 1 slime out of combat | Bud |
| More reach of the touch attack | Wide Reach |
| A hit that stops a shield | Shield Melt |
| A check that the reused effects work on a slime: effect on hit, web trail, pull down, climb, armor break, effect on hurt, death cloud, damage to, speed when, and the damage taken effects, at each size | Many genes |
