# Wolf

This document defines the families and the genes of the wolf.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The wolf is a neutral mob of the forests, the taigas, and the savannas.
It lives in packs. A wild wolf has 8 health, and a tame wolf has 40 health.
Its bite does 4 damage on Normal. A wild wolf hunts sheep, rabbits, foxes,
and skeletons. When a player hurts it, it and the wolves of its pack near it
get angry at that player. A player tames it with bones. It has 9 vanilla
variants, one for each biome group.
Its genes fit the pack, the chase, the howl, the coat, and the bite. It
shares no genes and no families with the other species.

The wolf is a neutral mob that fights the player.
[DESIGN.md](../DESIGN.md) gives the rules for these mobs: each death counts,
babies spawn as in vanilla, and a tame wolf keeps its genes but gets no new
ones.

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

### Denmates

Denmates fight hard next to their pack. Their bodies are small.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage with 2 or more wolves in 8 blocks | 10% less maximum health |
| Tier 2 | 20% more damage with 2 or more wolves in 8 blocks | 15% less maximum health |
| Tier 1 | 15% more damage with 2 or more wolves in 8 blocks | 20% less maximum health |

### Coursers

Coursers run down their prey. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 20% more speed while it chases a player | 10% more damage taken |
| Tier 2 | 15% more speed while it chases a player | 15% more damage taken |
| Tier 1 | 10% more speed while it chases a player | 20% more damage taken |

### Bayers

Bayers spread the anger of the pack far. Their bites are soft.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When something hurts it, the anger reaches two times as far | 10% less damage |
| Tier 2 | When something hurts it, the anger reaches 1.5 times as far | 15% less damage |
| Tier 1 | When something hurts it, the anger reaches 1.25 times as far | 20% less damage |

### Thickcoats

Thickcoats have a dense coat that stops arrows. The coat is heavy.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% less damage from projectiles | 10% less speed |
| Tier 2 | 20% less damage from projectiles | 15% less speed |
| Tier 1 | 15% less damage from projectiles | 20% less speed |

### Biters

Biters slow their prey with each bite. Each bite takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits give Slowness for 3 seconds | It attacks 10% slower |
| Tier 2 | Its hits give Slowness for 2 seconds | It attacks 15% slower |
| Tier 1 | Its hits give Slowness for 1 second | It attacks 20% slower |

## Pressures

The wolf uses `projectile`, `melee`, `explosion`, `fall`, `fire`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `player_death`,
`cold`, `snow`, `night`, and `shield`. An iron golem does not attack a
wolf, so the wolf does not use `golem`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Denmate genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Pack Bond | Denmate | `crowd` | 5% more damage for each wolf in 8 blocks, up to 20% | 10% less maximum health | Head: torn tips on both ears |
| Shared Wounds | Denmate | `critical` | A quarter of the damage that it takes goes to the wolves in 6 blocks | 10% less damage | Body: matching scars on the back |
| Huddle | Denmate | `projectile` | 30% less damage from projectiles with 2 or more wolves in 8 blocks | 10% less speed | Body: dense fur on the low flanks and the back of the mane |
| Grooming | Denmate | `melee` | It heals 1 health each 4 seconds with another wolf in 8 blocks | 10% less damage | Head: a clean, pale muzzle |
| Alpha Mark | Denmate | `player_death` | When it hits a player, the wolves in 12 blocks get Strength for 5 seconds | 10% less maximum health | Head: a dark mark on the brow |
| Pack Sprint | Denmate, sub Courser | `unreached` | 20% more speed with 2 or more wolves in 8 blocks | 10% less speed alone | Legs: dark socks on the paws |

### Courser genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Lope | Courser | `unreached` | 20% more speed | 10% less maximum health | Legs: long, pale stripes |
| Wet Nose | Courser | `alone` | Two times the follow range | 10% less speed | Head: a long, dark nose |
| Spring Leap | Courser | `fall` | It leaps at its target from 3 to 6 blocks away | 10% less maximum health | Legs: dark bands on all four legs |
| Night Prowl | Courser | `night` | 20% more speed at night | 10% less damage | Body: a dark stripe down the back |
| Weave | Courser | `projectile` | It steps aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Body: pale zigzag marks on the sides |
| Snow Paws | Courser, sub Thickcoat | `snow` | It walks on powder snow, and has 20% more speed on snow | 10% less damage | Legs: white, furry paws |

### Bayer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Moon Howl | Bayer | `night` | When it sees a player at night, it calls the wolves in 24 blocks | Players hear it from two times farther | Body: a gray throat on the low sides of the mane, under the head |
| Alarm Yelp | Bayer | `spotted` | When something hurts it, it calls the wolves in 32 blocks | Players hear it from two times farther | Head: wide, pale ears |
| Old Grudge | Bayer | `player_death` | After a player dies, it stays angry at that player for 5 minutes | 10% less speed | Head: a scar on the side of the head, behind the eye |
| Hot Temper | Bayer | `melee` | Its anger lasts two times as long | 10% less maximum health | Head: red eyes and dark red brows |
| Fear Howl | Bayer | `crowd` | Players in 4 blocks get Weakness | 10% less maximum health | Body: two rows of raised hackles on the top of the neck, behind the ears. This changes the model |
| Last Howl | Bayer, sub Denmate | `alone` | When it dies, the wolves in 24 blocks target its killer | 10% less maximum health | Body: a pale ring of fur round the neck |

### Thickcoat genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Shaggy Fur | Thickcoat | `melee` | 2 more armor | 10% less speed | Body: a ridge of long, shaggy tufts along the back. This changes the model |
| Frost Coat | Thickcoat | `cold` | It takes no damage from freezing | 10% less speed | Body: white tips on the fur of the rump |
| Bristle | Thickcoat | `critical` | A player that hits it in melee takes 1 damage | 10% less speed | Body: stiff, dark bristles on the back |
| Singed Pelt | Thickcoat | `fire` | Half the damage from fire | 10% less maximum health | Body: singed, brown fur on the flanks |
| Mud Coat | Thickcoat | `explosion` | Half the damage from explosions | 10% less speed | Body: dried mud on the belly and the low flanks |
| Bitten Hide | Thickcoat, sub Biter | `shield` | When something hurts it, it gets Strength for 3 seconds | 10% less speed | Body: old bite scars on the shoulders |

### Biter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Strong Jaw | Biter | `melee` | 20% more damage | It attacks 15% slower | Head: a wide, dark jaw |
| Hamstring | Biter | `unreached` | Its hits give Slowness for 4 seconds | 10% less damage | Legs: white fang holes with red edges |
| Rend | Biter | `critical` | Its hits give Weakness for 4 seconds | 10% less maximum health | Body: red drips down the throat, on the low sides of the mane |
| Throat Bite | Biter | `shield` | Its first hit on a player does 30% more damage | 10% less maximum health | Head: a dark mask round the eyes |
| Bone Crush | Biter | `crowd` | Its hits damage armor two times as much | 10% less speed | Head: chipped, yellow teeth |
| Snarl | Biter, sub Bayer | `spotted` | When it hits a player, the wolves in 16 blocks target that player | 10% less damage | Head: dark, curled lips |

## Gene details

| Gene | Rules |
|---|---|
| Denmates, Huddle, Pack Sprint | "Wolves" are wild wolves and tame wolves. "2 or more wolves in 8 blocks" is the `grouped` state |
| Coursers | It works as a speed while its target is a player |
| Bayers | The anger area of the vanilla wolf grows by this multiplier, as for the zombified piglin Vengefuls |
| Old Grudge, Hot Temper | They change the anger time of the wolf, as the zombified piglin Long Memory and Blood Feud do |
| Pack Bond | It works as the pillager Rally Cry, with wolves |
| Shared Wounds | It works as the witch shared damage |
| Grooming | It works as a regeneration in the `grouped` state |
| Alpha Mark | It works as a rally on a hit |
| Spring Leap | It works as the zombie Lunger |
| Weave | It works as the pillager Evasion |
| Snow Paws | It works as the stray walk on powder snow |
| Moon Howl, Alarm Yelp | They work as the vindicator Alarm Bell. Moon Howl calls on sight |
| Fear Howl | It works as an aura |
| Last Howl | It works as a call on death |
| Bristle | It works as the thorns effect |
| Bitten Hide | The effect goes on the wolf itself |
| Throat Bite | It works as the first strike effect |
| Bone Crush | It works as the cave spider Corrosive |
| Snarl | It works as a call on a hit |
| All genes | A gain that acts on players works while the wolf is angry. A wild wolf that is not angry attacks only its prey, as in vanilla |

## Tame wolves

- A tame wolf keeps the genes that it had in the wild.
- It gets no new genes, and its death does not count in the pool.
- Its genes act against the targets of its owner, as its vanilla bite does.
- A gain that calls wolves calls only wolves with the same owner.
- Its maximum health is 40 before the genes change it.
- Wolf armor covers the body. The gene marks show under the armor.

## Babies

- Wolves spawn as babies as in vanilla. No gene is the only source of
  babies.
- A natural baby and a bred baby take their genes from the pool.
- In milestone 6, a bred baby takes its genes from its parents.

## Body

- The wolf has the wolf model: a head with ears and a snout, a body, a
  mane, four legs, and a tail. Its texture is 64 x 32.
- It needs a new region map, `textures/region/wolf.png`.
- The wolf has 9 variants. Each variant has a wild, a tame, and an angry
  texture. The gene marks go over each of these textures.
- The collar of a tame wolf stays on top of the gene marks.
- The gene models use the wolf bones. Check them against the vanilla model,
  also the mane and the tail.
- Two genes change the model: Fear Howl and Shaggy Fur.
  No gene model is in the mouth.
- The gene models use the wolf texture of each variant, so they have the
  coat color of the wolf.
- The head is small, so three head marks are on the neck or the legs:
  Moon Howl, Rend, and Hamstring.
- The four legs share their texture, so each leg mark shows on all four
  legs.
- The baby wolf has its own model and texture, with no mane. A baby keeps
  its genes, but it shows no gene marks and no gene models until it grows
  up.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Hunting Packs | Denmate | Courser | A pack that runs down its prey |
| Howling Packs | Denmate | Bayer | A pack that calls more wolves |
| Shaggy Packs | Denmate | Thickcoat | A pack in thick coats |
| Biting Packs | Denmate | Biter | A pack that slows its prey |
| Pack Coursers | Courser | Denmate | A chase in a group |
| Loud Coursers | Courser | Bayer | A chase that calls the pack |
| Snow Coursers | Courser | Thickcoat | A chase in a heavy coat |
| Fang Coursers | Courser | Biter | A chase that ends in a bite |
| Pack Callers | Bayer | Denmate | A call that the pack answers |
| Chase Callers | Bayer | Courser | A call and a fast chase |
| Shaggy Callers | Bayer | Thickcoat | A call from behind a thick coat |
| Snarling Callers | Bayer | Biter | A call and a hard bite |
| Pack Coats | Thickcoat | Denmate | A thick coat in a pack |
| Running Coats | Thickcoat | Courser | A thick coat that runs |
| Loud Coats | Thickcoat | Bayer | A thick coat that calls |
| Scarred Coats | Thickcoat | Biter | A thick coat with a bite |
| Pack Jaws | Biter | Denmate | Bites in a pack |
| Running Jaws | Biter | Courser | Bites on the run |
| Snarling Jaws | Biter | Bayer | Bites that call the pack |
| Iron Jaws | Biter | Thickcoat | Bites behind a thick coat |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Denmates".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Hunting Bands | Denmate and Courser | A pack that runs |
| Moon Choirs | Bayer and Denmate | A pack that howls |
| Winter Packs | Denmate and Thickcoat | A pack in winter coats |
| Fang Packs | Biter and Denmate | A pack of hard bites |
| Night Chases | Bayer and Courser | A loud chase |
| Snow Trails | Courser and Thickcoat | A chase through the snow |
| Running Fangs | Biter and Courser | A fast bite |
| Storm Coats | Bayer and Thickcoat | A loud, thick coat |
| Snarling Fangs | Biter and Bayer | A loud bite |
| Bitten Coats | Biter and Thickcoat | A bite behind a thick coat |

## Neutral

A neutral wolf has no genes, or no family points.
It is a plain wolf, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| A wolf region map, and the gene textures over the 9 variants and the wild, tame, and angry textures | All visuals |
| Gene models on the wolf model, under the collar and the wolf armor | Fear Howl, Shaggy Fur |
| The anger area and the anger time of the wolf, and anger after the death of a player | Bayers, Old Grudge, Hot Temper |
| A tame wolf keeps its genes, gets no new genes, and its death does not count in the pool | All genes |
| A call that reaches only wolves with the same owner, for a tame wolf | Moon Howl, Alarm Yelp, Last Howl, Snarl |
| Gene effects of a tame wolf against the targets of its owner | All genes |
| Natural and bred babies take genes from the pool | All genes |
| A check that the reused effects work on a wolf: rally, share damage, regenerate, leap, sidestep, walk on powder snow, call on hurt, call on sight, call on death, call on hit, aura, thorns, first strike, armor break, and the damage taken effects | Many genes |
