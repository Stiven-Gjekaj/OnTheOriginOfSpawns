# Zombie horse

This document defines the families and the genes of the zombie horse.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The zombie horse is an undead horse. It has 25 health. It spawns in the dark
in some biomes, as a monster does, and a zombie with an iron spear rides it.
The zombie horse does not attack. Its rider fights. Its speed and its jump
are random at spawn, a little under those of a horse. A player can tame it
when no mob rides it. Its genes fit its gallop, the guard of its rider, its
undead flesh, its jump, and its kick. It shares no genes and no families with
the other species.

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

### Gallopers

Gallopers run fast. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed | 10% more damage taken |
| Tier 2 | 20% more speed | 15% more damage taken |
| Tier 1 | 15% more speed | 20% more damage taken |

### Destriers

Destriers guard their riders. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its riders take 30% less damage | 10% less maximum health |
| Tier 2 | Its riders take 20% less damage | 15% less maximum health |
| Tier 1 | Its riders take 15% less damage | 20% less maximum health |

### Revenants

Revenants mend their rotten flesh. They run slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It heals 1 health each 2 seconds out of combat | 10% less speed |
| Tier 2 | It heals 1 health each 3 seconds out of combat | 15% less speed |
| Tier 1 | It heals 1 health each 4 seconds out of combat | 20% less speed |

### Vaulters

Vaulters jump high. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It jumps 30% higher | 10% less maximum health |
| Tier 2 | It jumps 20% higher | 15% less maximum health |
| Tier 1 | It jumps 15% higher | 20% less maximum health |

### Stompers

Stompers kick the attackers that hurt them. They run slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When a melee hit hurts it, it kicks the attacker for 4 damage | 10% less speed |
| Tier 2 | When a melee hit hurts it, it kicks the attacker for 3 damage | 15% less speed |
| Tier 1 | When a melee hit hurts it, it kicks the attacker for 2 damage | 20% less speed |

## Pressures

The zombie horse uses `melee`, `projectile`, `explosion`, `fire`, `fall`,
`shield`, `smite`, `unreached`, `spotted`, `crowd`, `critical`,
`golem`, `sunlight`, and `player_death`. The sun burns it, as it burns a
zombie. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Galloper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Long Stride | Galloper | `unreached` | 20% more speed | 10% less maximum health | Legs: long, lean legs. This changes the model |
| Sun Hide | Galloper | `sunlight` | The sun burns it only one time each 4 seconds, not each second | 10% less maximum health | Body: a dark, leathery hide |
| Shy Canter | Galloper | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Head: tall, laid back ears |
| Lance Run | Galloper | `melee` | While it runs, the hits of its rider do 20% more damage | 10% less maximum health | Mane: a long, streaming mane. This changes the model |
| Ghost Gallop | Galloper | `spotted` | It is near invisible in the dark | 10% less maximum health | Body: pale, faded hide |
| Panic Bolt | Galloper, sub Revenant | `critical` | At under 30% health, it runs away from its target fast for 4 seconds | 10% less maximum health | Tail: a torn, thin tail |

### Destrier genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Rotten Barding | Destrier | `projectile` | Its riders take 30% less damage from projectiles | 10% less maximum health | Body: a ragged cloth over the back |
| Battle Saddle | Destrier | `melee` | Its riders take 20% less melee damage | 10% less speed | Body: a high, dark saddle. This changes the model |
| Grave Bond | Destrier | `critical` | When a rider is under half health, the rider heals 4 health at once, one time each 30 seconds | 10% less maximum health | Head: a green blaze on the face |
| Ash Barding | Destrier | `fire` | Its riders take half the damage from fire | 10% less speed | Body: a gray, scorched cloth |
| Last Charge | Destrier | `player_death` | When it dies, its riders get Strength for 10 seconds | 10% less maximum health | Mane: red ribbons in the mane |
| Grave Barding | Destrier, sub Vaulter | `smite` | Smite does half the extra damage to its riders | 10% less speed | Body: bone plates on the chest |

### Revenant genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Undying Flesh | Revenant | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Body: stitched wounds on the flank |
| Rotten Hide | Revenant | `projectile` | 30% less damage from projectiles | 10% less speed | Body: thick, rotten patches |
| Charred Flesh | Revenant | `fire` | Half the damage from fire | 10% less speed | Body: black, charred patches |
| Bone Frame | Revenant | `explosion` | Half the damage from explosions | 10% less speed | Body: bare ribs on the flank |
| Holy Scorn | Revenant | `smite` | Smite does half the extra damage | 10% less maximum health | Head: a pale skull face |
| Rooted Rot | Revenant, sub Stomper | `melee` | Hits do not push it back | 10% less speed | Legs: thick, swollen hocks |

### Vaulter genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| High Vault | Vaulter | `unreached` | It jumps 50% higher | 10% less maximum health | Legs: long, springy hocks |
| Soft Landing | Vaulter | `fall` | No fall damage | 10% less maximum health | Legs: wide, pale hooves |
| Leap Strike | Vaulter | `golem` | When it lands from a jump, the mobs and players in 2 blocks take 3 damage | 10% less maximum health | Legs: iron gray hooves |
| Dodge Leap | Vaulter | `projectile` | It jumps aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Tail: a short, high tail |
| Shield Vault | Vaulter | `shield` | When it lands from a jump, the shields of the players in 2 blocks stop for 3 seconds | 10% less speed | Head: a dark, armored brow |
| Spur Leap | Vaulter, sub Galloper | `spotted` | After a jump, it gets Speed for 3 seconds | 10% less maximum health | Legs: bright bands round the hocks |

### Stomper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Hard Hooves | Stomper | `melee` | Its kick does 2 more damage | 10% less speed | Legs: dark, cracked hooves |
| Mud Kick | Stomper | `spotted` | Its kick gives Slowness for 3 seconds | 10% less maximum health | Legs: mud on the lower legs |
| Golem Stomp | Stomper | `golem` | Its kick does two times the damage to golems | 10% less speed | Legs: iron gray shins |
| Bone Shatter | Stomper | `critical` | Its kick gives Weakness for 4 seconds | 10% less maximum health | Body: cracked bones on the chest |
| Rear Up | Stomper | `crowd` | Its kick also hits the other mobs and players in 2 blocks | 10% less speed | Mane: a wild, raised mane |
| Trample Run | Stomper, sub Galloper | `player_death` | Its kick pushes the attacker back two times as far | 10% less maximum health | Tail: a long, ragged tail |

## Gene details

| Gene | Rules |
|---|---|
| Gallopers, Long Stride | "Speed" is the movement speed of the zombie horse, also when a player rides it |
| Sun Hide | It works as the phantom Sun Shade |
| Shy Canter | It works as the pillager Evasion |
| Lance Run | "While it runs" is while it moves at more than half its speed |
| Ghost Gallop | It works as the creeper Gloom |
| Panic Bolt | It works as the blaze Smoke Dash. Its riders stay on it |
| Destriers | "Its riders" are the mobs that ride it. A player that rides it does not count |
| Grave Bond | It checks each rider. The riders are undead, and the undead ignore Regeneration, so it heals at once |
| Last Charge | It works as the pillager Grudge, on its riders only |
| Grave Barding | It works as the zoglin Holy Bane, on its riders |
| Revenants | They work as the vindicator Thick Blood |
| Undying Flesh | It works as the vindicator Death Wish |
| Holy Scorn | It works as the zoglin Holy Bane |
| Rooted Rot | It works as the hoglin Root Feet |
| Vaulters, High Vault | "Jumps higher" changes the jump strength. A mob rider makes it jump only to climb, as in vanilla |
| Leap Strike | It works as the breeze Crash Down |
| Dodge Leap | It works as the pillager Evasion, with a jump |
| Shield Vault | It works as the magma cube Shield Slam |
| Stompers | The kick is a hit of the zombie horse. It also acts when no mob rides it |
| Trample Run | It works as the push of the hoglin Tuskers |

## Equipment

- The zombie horse holds nothing. It drops its vanilla items.
- A zombie that rides a zombie horse keeps its iron spear, as DESIGN.md says
  for the husk on a camel husk.

## Conversion

- The zombie horse does not convert to a different species, and no species
  converts to the zombie horse.

## Babies and jockeys

- A zombie horse can be a baby only from a spawn egg. It does not get a gene
  for babies.
- The zombie on its back keeps its own genes. The zombie horse does not change
  them, except through the Destrier genes.

## Spawning

- A zombie horse that spawns in a natural way gets genes as a natural spawn.
- A tame zombie horse keeps its genes.

## Body

- The zombie horse has the horse model. Check the texture size and the bones
  against the vanilla model, as for the ghast.
- It needs a new region map, `textures/region/zombie_horse.png`. The design
  uses the regions head, mane, body, legs, and tail.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Guarding Gallopers | Galloper | Destrier | Speed that guards the rider |
| Undying Gallopers | Galloper | Revenant | Speed that lasts |
| Leaping Gallopers | Galloper | Vaulter | Speed and jumps |
| Stomping Gallopers | Galloper | Stomper | Speed and kicks |
| Swift Destriers | Destrier | Galloper | A guard at speed |
| Undying Destriers | Destrier | Revenant | A guard that lasts |
| Leaping Destriers | Destrier | Vaulter | A guard and jumps |
| Stomping Destriers | Destrier | Stomper | A guard and kicks |
| Swift Revenants | Revenant | Galloper | Rotten flesh at speed |
| Guarding Revenants | Revenant | Destrier | Rotten flesh that guards the rider |
| Leaping Revenants | Revenant | Vaulter | Rotten flesh and jumps |
| Stomping Revenants | Revenant | Stomper | Rotten flesh and kicks |
| Swift Vaulters | Vaulter | Galloper | Jumps at speed |
| Guarding Vaulters | Vaulter | Destrier | Jumps that guard the rider |
| Undying Vaulters | Vaulter | Revenant | Jumps that last |
| Stomping Vaulters | Vaulter | Stomper | Jumps and kicks |
| Swift Stompers | Stomper | Galloper | Kicks at speed |
| Guarding Stompers | Stomper | Destrier | Kicks that guard the rider |
| Undying Stompers | Stomper | Revenant | Kicks that last |
| Leaping Stompers | Stomper | Vaulter | Kicks and jumps |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Gallopers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Dread Cavalry | Destrier and Galloper | Speed that guards the rider |
| Pale Riders | Galloper and Revenant | Speed that lasts |
| Dusk Coursers | Galloper and Vaulter | Speed and jumps |
| Rot Chargers | Galloper and Stomper | Speed and kicks |
| Grave Knights | Destrier and Revenant | A guard that lasts |
| Siege Leapers | Destrier and Vaulter | A guard and jumps |
| Crypt Sentries | Destrier and Stomper | A guard and kicks |
| Rot Springers | Revenant and Vaulter | Jumps that last |
| Dead Hooves | Revenant and Stomper | Kicks that last |
| Grave Kickers | Stomper and Vaulter | Kicks and jumps |

## Neutral

A neutral zombie horse has no genes, or no family points.
It is a plain zombie horse, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet. The
camel husk and the zombie nautilus need some of the same parts, so build each
part one time for all mounts.

| Need | Used by |
|---|---|
| A zombie horse region map, and the gene textures and gene models on the horse model | All visuals |
| Less damage to the riders, also by damage type, and less Smite damage to the riders | Destriers, Rotten Barding, Battle Saddle, Ash Barding, Grave Barding |
| An effect on a rider at low health, and an effect on the riders at death | Grave Bond, Last Charge |
| More damage on the hits of a rider while the mount runs | Lance Run |
| The jump strength, a hit and a shield stop when it lands, and a jump aside | Vaulters, High Vault, Leap Strike, Shield Vault, Dodge Leap |
| A kick at the attacker after a melee hit, with damage, effects, golem damage, a hit round it, and a push | Stompers, Hard Hooves, Mud Kick, Golem Stomp, Bone Shatter, Rear Up, Trample Run |
| A check that the reused effects work on a zombie horse: speed, sun shade, sidestep, translucent, flee, regenerate, heal once, smite resist, knockback, no fall damage, a self effect after a jump, and the damage taken effects | Many genes |
