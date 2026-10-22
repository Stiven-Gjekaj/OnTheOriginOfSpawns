# Spider

This document defines the families and the genes of the spider.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The spider climbs walls and leaps at the player. It has 16 health. In bright
light it does not attack, unless something hurts it first. Cobwebs do not slow
it, and Poison does not affect it. On Hard, it can spawn with Speed,
Strength, Regeneration, or Invisibility. Its genes fit the silk of the web,
the leap of the jumping spider, the venom of the fangs, the swarm of the nest,
and the trapdoor spider that waits under the ground. It shares no genes and
no families with the cave spider or the other species.

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

### Weavers

Weavers spin silk to trap the player. Silk burns.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit has a 30% chance to put a cobweb at the target | 10% more fire damage |
| Tier 2 | Each hit has a 20% chance to put a cobweb at the target | 20% more fire damage |
| Tier 1 | Each hit has a 10% chance to put a cobweb at the target | 25% more fire damage |

### Jumpers

Jumpers leap far and hit hard after the leap. Their bodies are light.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its first hit after a leap does 40% more damage | 10% less maximum health |
| Tier 2 | Its first hit after a leap does 30% more damage | 20% less maximum health |
| Tier 1 | Its first hit after a leap does 20% more damage | 25% less maximum health |

### Fangs

Fangs carry venom in their bites. The venom makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit gives Poison for 5 seconds | 10% less speed |
| Tier 2 | Each hit gives Poison for 4 seconds | 15% less speed |
| Tier 1 | Each hit gives Poison for 3 seconds | 20% less speed |

### Swarms

Swarms hunt together. Alone, they are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 20% more damage while another spider is in 8 blocks | 10% less damage while no other spider is in 8 blocks |
| Tier 2 | 15% more damage while another spider is in 8 blocks | 15% less damage while no other spider is in 8 blocks |
| Tier 1 | 10% more damage while another spider is in 8 blocks | 20% less damage while no other spider is in 8 blocks |

### Trapdoors

Trapdoors wait in hiding and strike first. Bright light makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its first hit on a player does 40% more damage | 10% less speed in bright light |
| Tier 2 | Its first hit on a player does 30% more damage | 15% less speed in bright light |
| Tier 1 | Its first hit on a player does 20% more damage | 20% less speed in bright light |

## Pressures

The spider uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `night`, `day`, and `shield`.
The sun does not burn it, and wolves do not attack it, so it does not use
`sunlight` or `wolf`. It also uses this pressure.

| Pressure | A spider death counts when | State |
|---|---|---|
| `bane` | The killer hit the spider with a weapon with the Bane of Arthropods enchantment | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Weaver genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Web Shot | Weaver | `projectile` | Each 8 seconds, it spits silk at a player 4 to 12 blocks away. The silk makes a cobweb where it lands | 10% less maximum health | Abdomen: a white spinneret at the back. This changes the model |
| Tripwire | Weaver | `spotted` | It leaves a cobweb behind it each 10 seconds while it walks. The cobweb goes away after 30 seconds | 10% less speed | Legs: silk threads on the legs |
| Silk Armor | Weaver | `melee` | 4 more armor | 25% more fire damage | Body: a wrap of silk |
| Cocoon | Weaver, sub Swarm | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Abdomen: silk threads that wrap the abdomen |
| Snare Line | Weaver | `unreached` | Its hits give Slowness II for 3 seconds | 10% less damage | Head: silk threads from the fangs |
| Web Walker | Weaver | `fall` | No fall damage | 10% less maximum health | Back: a silk line from the abdomen. This changes the model |

### Jumper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Pounce | Jumper | `melee` | It leaps at a player 3 to 7 blocks away | 10% less maximum health | Legs: long, thick back legs. This changes the model |
| Springlegs | Jumper | `fall` | No fall damage, and it jumps 50% higher | 50% more explosion damage | Legs: striped joints |
| Big Eyes | Jumper | `spotted` | 50% more follow range | 10% less maximum health | Head: two large front eyes |
| Dart | Jumper, sub Trapdoor | `projectile` | It steps to the side when a player aims at it from far away | 10% less damage | Body: pale stripes |
| Quickbite | Jumper | `crowd` | It attacks 25% faster | 10% less damage | Head: long, curved fangs. This changes the model |
| Bound | Jumper | `fire` | 20% more speed | 10% less maximum health | Legs: thin, long legs |

### Fang genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Venom Bite | Fang | `melee` | Its hits give Poison II for 4 seconds | 10% less speed | Head: green tips on the fangs |
| Necrotic | Fang | `critical` | Its hits give Wither for 3 seconds | 4 less maximum health | Head: black, rotten fangs |
| Paralyze | Fang | `unreached` | Its hits give Slowness IV for 1 second | 10% less damage | Head: yellow venom glands |
| Hunger Venom | Fang, sub Swarm | `alone` | Its hits give Hunger for 10 seconds | 10% less maximum health | Abdomen: a red hourglass mark |
| Venom Blood | Fang | `crowd` | A player that hits it in melee gets Poison for 3 seconds | 10% less speed | Body: green veins |
| Spit | Fang | `projectile` | Each 6 seconds, it spits venom at a player 3 to 10 blocks away. The venom gives Poison for 4 seconds | 10% less damage | Head: a swollen jaw |

### Swarm genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Clutch | Swarm | `alone` | Its spawn groups have 1 more spider | Each spider of the group has 15% less maximum health | Abdomen: small spiderlings on the back. This changes the model |
| Nest Call | Swarm | `spotted` | When it sees a player, it calls the spiders in 24 blocks | Players hear it from two times farther | Head: a red ring around the eyes |
| Hive Mind | Swarm | `crowd` | It shares the damage that it takes with the spiders in 8 blocks | 10% less speed | Body: a pale ring pattern |
| Mother | Swarm, sub Fang | `critical` | When it dies, 2 cave spiders come out of it | 4 less maximum health | Abdomen: a swollen egg sac. This changes the model |
| Pack Hunter | Swarm | `golem` | 5% more damage for each spider in 8 blocks, up to 20% | 2 less armor | Legs: banded legs |
| Silk Signal | Swarm | `melee` | When something hurts it, it calls the spiders in 16 blocks | 10% less damage | Back: stiff hairs on the back |

### Trapdoor genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Digger | Trapdoor | `spotted` | It waits under dirt or sand, then it comes up next to a player | It cannot move for 1 second after it comes up | Body: dirt in the hairs |
| Lurking Fangs | Trapdoor | `alone` | It does not move while a player faces it, until the player is in 4 blocks | 25% less follow range | Head: a dark hood of hair |
| Day Hunter | Trapdoor | `day` | It attacks players in bright light too | 10% less speed in bright light | Eyes: yellow eyes |
| Shadow Skin | Trapdoor | `night` | It is almost invisible at a light level of 7 or less | 10% less maximum health | Body: black hairs |
| Ceiling Drop | Trapdoor, sub Jumper | `fall` | No fall damage, and 30% more damage while it is above its target | 2 less armor | Back: dark chevrons |
| Silent Steps | Trapdoor | `melee` | It makes no sound | 10% less maximum health | Legs: soft, furred feet |

## Gene details

| Gene | Rules |
|---|---|
| Weavers, Web Shot, Tripwire | A cobweb from a gene does not replace a block. It goes away after 30 seconds and drops no string |
| Jumpers | "A leap" is the vanilla leap of the spider or a gene leap. The bonus counts for the first hit in 2 seconds after the leap |
| Trapdoors | "Its first hit on a player" counts again after 10 seconds with no hit on that player |
| Trapdoors, Day Hunter | "Bright light" is a light level of 12 or more |
| Fangs, Venom Bite | Poison does not affect spiders, so the Poison of a spider never hurts another spider |
| Pounce | It works as the zombie Lunger |
| Dart | It works as the zombie sidestep, with the aim check |
| Cocoon | It works as the drowned Pearlheart |
| Hive Mind | It uses the `share_damage` effect, which no gene uses yet |
| Mother | The cave spiders have no genes from the spider. They roll their own genes from the cave spider pool |
| Digger | It works as the zombie Burrower, in the blocks of the tag `#originofspawns:burrowable`: dirt, grass, sand, and gravel |
| Lurking Fangs | It works as the zombie Lurker |
| Day Hunter | It removes the vanilla rule that a spider in bright light does not attack |
| Shadow Skin | It is drawn translucent, like the zombie Shade |
| Silent Steps | It works as the zombie Hush |
| Spit | It works as the husk Sandblast, with Poison in place of Blindness |

## Equipment

- A spider has no weapon and no armor in vanilla. It does not get them from
  its genes.
- The random effects that a spider gets on Hard stay, as in vanilla.

## Conversion

- The spider does not convert to a different species, and no species
  converts to the spider.

## Babies and jockeys

- Spiders have no babies in vanilla. They do not get a gene for them.
- The skeleton Spider Rider already gives the skeleton a spider to ride. The
  spider does not get a gene for jockeys.

## Body

- The spider has a head, a body, and eight legs. Its texture is 64 x 32.
- It needs its own region map, `textures/region/spider.png`, with regions
  for the head, the fangs, the eyes, the body, the abdomen, the back, and the
  legs.
- The glowing eyes of the spider draw over the gene marks.
- The gene models need bones for the eight legs: `right_hind_leg`,
  `left_hind_leg`, `right_middle_hind_leg`, `left_middle_hind_leg`,
  `right_middle_front_leg`, `left_middle_front_leg`, `right_front_leg`, and
  `left_front_leg`.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Orb Weavers | Weaver | Jumper | Webs and leaps |
| Funnel Webs | Weaver | Fang | Webs with venom |
| Silk Nests | Weaver | Swarm | Many webs from many spiders |
| Tangle Traps | Weaver | Trapdoor | Hidden webs |
| Web Leapers | Jumper | Weaver | Leaps that trap |
| Bite Jumpers | Jumper | Fang | Leaps with venom |
| Hopping Packs | Jumper | Swarm | Leaps in groups |
| Bushwhackers | Jumper | Trapdoor | Leaps from hiding |
| Silk Fangs | Fang | Weaver | Venom and webs |
| Strike Fangs | Fang | Jumper | Fast venom |
| Widow Packs | Fang | Swarm | Venom in groups |
| Buried Fangs | Fang | Trapdoor | Venom from under the ground |
| Web Colonies | Swarm | Weaver | A nest of webs |
| Leaping Swarms | Swarm | Jumper | A swarm that leaps |
| Venom Nests | Swarm | Fang | A swarm with venom |
| Burrow Colonies | Swarm | Trapdoor | A swarm under the ground |
| Silk Doors | Trapdoor | Weaver | Trapdoors lined with silk |
| Spring Traps | Trapdoor | Jumper | Trapdoors that leap |
| Venom Pits | Trapdoor | Fang | Trapdoors with venom |
| Hidden Nests | Trapdoor | Swarm | Many trapdoors |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Weavers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Wolf Weavers | Jumper and Weaver | Webs and leaps |
| Black Widows | Fang and Weaver | Webs and venom |
| Silk Cities | Swarm and Weaver | Many webs |
| Web Pits | Trapdoor and Weaver | Webs in the dark |
| Death Jumpers | Fang and Jumper | Fast venom |
| Leaping Hordes | Jumper and Swarm | Leaps in groups |
| Ambush Leapers | Jumper and Trapdoor | Leaps from hiding |
| Venom Swarms | Fang and Swarm | Venom in groups |
| Pit Vipers | Fang and Trapdoor | Venom from hiding |
| Underground Nests | Swarm and Trapdoor | A swarm in the dark |

## Neutral

A neutral spider has no genes, or no family points.
It is a plain spider, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the spider, with bones for the eight legs | All visuals |
| A region map for the spider | All visuals |
| A cobweb at the target of a hit, which goes away after a time | Weavers |
| A silk shot that makes a cobweb where it lands | Web Shot |
| A trail of cobwebs behind the mob | Tripwire |
| A bonus to the first hit after a leap | Jumpers |
| A bonus to the first hit on a player | Trapdoors |
| A state for bright light at a level of 12 or more | Trapdoors, Day Hunter |
| An attack in bright light | Day Hunter |
| The pressure `bane` | The deaths of spiders to Bane of Arthropods |
| A check that leap, sidestep, burrow, lurk, silence, share damage, death spawn, and the thrown effect work on a spider | Pounce, Dart, Digger, Lurking Fangs, Silent Steps, Hive Mind, Mother, Spit |
