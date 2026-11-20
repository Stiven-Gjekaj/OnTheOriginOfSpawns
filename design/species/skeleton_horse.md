# Skeleton horse

This document defines the families and the genes of the skeleton horse.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The skeleton horse is an undead horse of bones. It has 15 health. In a
thunderstorm, lightning can leave a skeleton horse that is a trap. When a
player comes in 10 blocks, the trap opens: lightning strikes, three more
skeleton horses appear, and a skeleton with an iron helmet and an enchanted
bow rides each of the four. A trap that no player finds goes away after 15
minutes. The skeleton horse does not attack. Its riders fight. Water does not
slow it much, so it swims well. A player can tame it when no mob rides it. Its
genes fit the trap, its swim, its bones, the guard of its riders, and its
ghostly run. It shares no genes and no families with the other species.

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

### Thunderhooves

Thunderhooves bring more horsemen. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When its trap opens, 2 more horsemen appear | 10% less maximum health |
| Tier 2 | When its trap opens, 1 more horseman appears | 15% less maximum health |
| Tier 1 | When its trap opens, 1 more horseman appears | 20% less maximum health |

### Tidestriders

Tidestriders swim fast. They walk slowly on land.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed in water | 10% less speed on land |
| Tier 2 | 20% more speed in water | 15% less speed on land |
| Tier 1 | 15% more speed in water | 20% less speed on land |

### Ossifiers

Ossifiers have hard bones. They run slowly.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 4 more armor | 10% less speed |
| Tier 2 | 3 more armor | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Squires

Squires guard their riders. Their bodies are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its riders take 30% less damage | 10% less maximum health |
| Tier 2 | Its riders take 20% less damage | 15% less maximum health |
| Tier 1 | Its riders take 15% less damage | 20% less maximum health |

### Wraithrunners

Wraithrunners run fast. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more speed | 10% more damage taken |
| Tier 2 | 20% more speed | 15% more damage taken |
| Tier 1 | 15% more speed | 20% more damage taken |

## Pressures

The skeleton horse uses `melee`, `projectile`, `explosion`, `fire`, `fall`,
`trident`, `smite`, `in_water`, `on_land`, `unreached`, `spotted`, `alone`,
`crowd`, `critical`, `golem`, and `player_death`. The sun does not hurt it,
so it does not use `sunlight`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Thunderhoof genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Storm Herald | Thunderhoof | `spotted` | When its trap opens, lightning also strikes 3 blocks from the player | 10% less maximum health | Mane: a blue, sparking mane. This changes the model |
| Grounded Bones | Thunderhoof | `fire` | No damage and no fire from lightning, and half the damage from other fire | 10% less speed | Legs: copper bands on the legs |
| Long Wait | Thunderhoof | `alone` | Its trap waits two times as long before it goes away | 10% less maximum health | Head: a dark, empty skull |
| Wide Trap | Thunderhoof | `unreached` | Its trap opens when a player comes in 20 blocks | 10% less maximum health | Head: wide, glowing eye sockets. This changes the model |
| Charged Riders | Thunderhoof | `crowd` | When its trap opens, the horses of the trap get Speed for 10 seconds | 10% less maximum health | Body: blue lines on the ribs |
| Storm Kin | Thunderhoof, sub Squire | `player_death` | When a rider dies, the arrows of the other horsemen in 16 blocks do 30% more damage for 10 seconds | 10% less maximum health | Mane: a dark storm gray mane with pale blue streaks |

### Tidestrider genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Deep Stride | Tidestrider | `in_water` | 20% more speed in water | 10% less speed on land | Legs: deep blue hooves |
| Kelp Mane | Tidestrider | `spotted` | It is near invisible in water | 10% less maximum health | Mane: wavy kelp strands in the mane. This changes the model |
| Coral Bones | Tidestrider | `projectile` | 30% less damage from projectiles | 10% less speed | Body: pink coral on the bones |
| Cold Current | Tidestrider | `melee` | When a melee hit hurts it in water, the attacker gets Slowness for 3 seconds | 10% less maximum health | Tail: a long, wet tail. This changes the model |
| Shore Bound | Tidestrider | `on_land` | On land, it hops to the nearest water in 8 blocks | 10% less maximum health | Legs: wet, dark legs |
| Trident Ribs | Tidestrider, sub Ossifier | `trident` | Half the damage from tridents | 10% less speed | Body: wide steel bands with rivets across the ribs. This changes the model |

### Ossifier genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Heavy Bones | Ossifier | `melee` | Hits do not push it back | 10% less speed | Legs: thick, heavy bones. This changes the model |
| Blast Ribs | Ossifier | `explosion` | Half the damage from explosions | 10% less speed | Body: dark, cracked ribs |
| Rib Gaps | Ossifier | `projectile` | Each projectile has a 25% chance to pass through its ribs with no damage | 10% less maximum health | Body: broken stubs where two ribs are gone |
| Blessed Skull | Ossifier | `smite` | Smite does half the extra damage | 10% less maximum health | Head: a pale snout with a faint gold line |
| Bone Mend | Ossifier | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Body: a white, healed bone on the chest |
| Golem Brace | Ossifier, sub Squire | `golem` | 30% less damage from golems | 10% less speed | Legs: dark iron bands with rivets |

### Squire genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Bone Barding | Squire | `projectile` | Its riders take 30% less damage from projectiles | 10% less maximum health | Body: bone plates on the flanks |
| Squire Saddle | Squire | `melee` | Its riders take 20% less melee damage | 10% less speed | Body: an old, dark saddle. This changes the model |
| Faithful Bones | Squire | `critical` | When a rider is under half health, the rider heals 4 health at once, one time each 30 seconds | 10% less maximum health | Head: a blue mark on the skull |
| Storm Cloak | Squire | `fire` | Its riders take half the damage from fire | 10% less speed | Body: a thin, torn, singed cloth over the back. This changes the model |
| Last Ride | Squire | `alone` | When it dies, its riders get Resistance for 10 seconds | 10% less maximum health | Mane: torn ribbons in the mane. This changes the model |
| Helm Bond | Squire, sub Wraithrunner | `smite` | Smite does half the extra damage to its riders | 10% less speed | Head: a dark iron band round the skull |

### Wraithrunner genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Ghost Stride | Wraithrunner | `unreached` | 20% more speed | 10% less maximum health | Legs: thin struts up the flank. This changes the model |
| Wraith Veil | Wraithrunner | `spotted` | It is near invisible in the dark | 10% less maximum health | Body: pale, faded bones |
| Night Run | Wraithrunner | `alone` | 20% more speed at night | 10% less maximum health | Tail: a pale, wispy tail. This changes the model |
| Bone Dodge | Wraithrunner | `projectile` | It moves aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Mane: a short, stiff mane. This changes the model |
| Weightless | Wraithrunner | `fall` | No fall damage | 10% less maximum health | Legs: light, thin hooves |
| Rattle Flight | Wraithrunner, sub Tidestrider | `critical` | At under 30% health, it runs away from its target fast for 4 seconds | 10% less maximum health | Body: loose, rattling ribs. This changes the model |

## Gene details

| Gene | Rules |
|---|---|
| Thunderhooves | The extra horsemen are skeleton horses with skeleton riders, as in vanilla. They have genes from the pools, as new mobs |
| Thunderhoof genes | They act only on a skeleton horse that is a trap. On a tame or a plain skeleton horse, they do nothing but their downside |
| Storm Herald | The lightning does the vanilla damage, and it does not hit the horsemen |
| Grounded Bones | It works as the creeper Grounded, with half fire damage |
| Long Wait | The vanilla trap goes away after 15 minutes |
| Wide Trap | The vanilla trap opens at 10 blocks |
| Storm Kin | The horsemen shoot bows, and Strength does not change arrow damage, so it changes the arrow damage |
| Charged Riders | Speed on a skeleton rider does not change the speed of its horse, so the horses get the Speed |
| Tidestriders | "Speed in water" and "speed on land" use the in water and on land states |
| Kelp Mane | It works as the creeper Gloom, in water at each light level |
| Cold Current | It works as the evoker Smoke Veil, on the attacker, in water only |
| Shore Bound | It works as the guardian Shore Hop |
| Heavy Bones | It works as the hoglin Root Feet |
| Rib Gaps | It works as the guardian Slick Hide |
| Blessed Skull | It works as the zoglin Holy Bane |
| Bone Mend | It works as the vindicator Death Wish |
| Squires | "Its riders" are the mobs that ride it. A player that rides it does not count |
| Faithful Bones | It checks each rider. The riders are undead, and the undead ignore Regeneration, so it heals at once |
| Last Ride | It works as the pillager Grudge, on its riders only |
| Helm Bond | It works as the zoglin Holy Bane, on its riders |
| Wraith Veil | It works as the creeper Gloom |
| Night Run | It uses the night state |
| Bone Dodge | It works as the pillager Evasion |
| Rattle Flight | It works as the blaze Smoke Dash. Its riders stay on it |

## Equipment

- The skeleton horse holds nothing. It drops its vanilla items.
- A skeleton of a trap keeps its iron helmet and its enchanted bow, because
  the trap and its horsemen are one vanilla mob design.

## Conversion

- The skeleton horse does not convert to a different species, and no species
  converts to the skeleton horse.

## Babies and jockeys

- A skeleton horse can be a baby only from a spawn egg. It does not get a
  gene for babies.
- The skeletons of a trap keep their own genes. The skeleton horse does not
  change them, except through the Thunderhoof and Squire genes.

## Spawning

- A skeleton horse of a trap gets genes as a natural spawn. The three horses
  that appear when the trap opens also get genes as natural spawns.
- A tame skeleton horse keeps its genes.

## Body

- The skeleton horse has the horse model. Check the texture size and the
  bones against the vanilla model, as for the ghast.
- It needs a new region map, `textures/region/skeleton_horse.png`. The design
  uses the regions head, mane, body, legs, and tail. If it shares the bones
  of the zombie horse, the gene models can share their bone names.
- The texture is 64 x 64, with the layout of the zombie horse. The vanilla
  tail has no pixels, and the mane has few, so most mane and tail genes are
  gene models.
- A baby has another texture layout, so a baby gets no gene marks and no
  gene models.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Tidal Thunderhooves | Thunderhoof | Tidestrider | A storm trap near water |
| Boned Thunderhooves | Thunderhoof | Ossifier | A storm trap of hard bones |
| Squired Thunderhooves | Thunderhoof | Squire | A storm trap that guards the riders |
| Wraith Thunderhooves | Thunderhoof | Wraithrunner | A fast storm trap |
| Stormy Tidestriders | Tidestrider | Thunderhoof | A fast swim and a storm trap |
| Boned Tidestriders | Tidestrider | Ossifier | A fast swim on hard bones |
| Squired Tidestriders | Tidestrider | Squire | A fast swim that guards the riders |
| Wraith Tidestriders | Tidestrider | Wraithrunner | A fast swim and a fast run |
| Stormy Ossifiers | Ossifier | Thunderhoof | Hard bones and a storm trap |
| Tidal Ossifiers | Ossifier | Tidestrider | Hard bones that swim |
| Squired Ossifiers | Ossifier | Squire | Hard bones that guard the riders |
| Wraith Ossifiers | Ossifier | Wraithrunner | Hard bones and a fast run |
| Stormy Squires | Squire | Thunderhoof | A guard and a storm trap |
| Tidal Squires | Squire | Tidestrider | A guard that swims |
| Boned Squires | Squire | Ossifier | A guard of hard bones |
| Wraith Squires | Squire | Wraithrunner | A guard and a fast run |
| Stormy Wraithrunners | Wraithrunner | Thunderhoof | A fast run and a storm trap |
| Tidal Wraithrunners | Wraithrunner | Tidestrider | A fast run that swims |
| Boned Wraithrunners | Wraithrunner | Ossifier | A fast run on hard bones |
| Squired Wraithrunners | Wraithrunner | Squire | A fast run that guards the riders |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Thunderhooves".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Drowned Storms | Thunderhoof and Tidestrider | A storm trap near water |
| Iron Storms | Ossifier and Thunderhoof | A storm trap of hard bones |
| Thunder Escorts | Squire and Thunderhoof | A storm trap that guards the riders |
| Ghost Riders | Thunderhoof and Wraithrunner | A fast storm trap |
| Reef Bones | Ossifier and Tidestrider | Hard bones that swim |
| Sea Squires | Squire and Tidestrider | A guard that swims |
| Phantom Tides | Tidestrider and Wraithrunner | A fast swim and a fast run |
| Bone Bastions | Ossifier and Squire | A guard of hard bones |
| Rattling Wraiths | Ossifier and Wraithrunner | Hard bones and a fast run |
| Spectral Escorts | Squire and Wraithrunner | A fast run that guards the riders |

## Neutral

A neutral skeleton horse has no genes, or no family points.
It is a plain skeleton horse, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet. The
other mounts need many of the same parts, so build each part one time for all
mounts.

| Need | Used by |
|---|---|
| A skeleton horse region map, and the gene textures and gene models on the horse model | All visuals |
| More horsemen, extra lightning, a longer wait, a wider reach, and an effect on the horsemen when the trap opens | Thunderhooves, Storm Herald, Long Wait, Wide Trap, Charged Riders |
| More arrow damage for the horsemen of a trap when one of them dies | Storm Kin |
| No damage and no fire from lightning on a mount | Grounded Bones |
| Speed in water and on land | Tidestriders, Deep Stride, and the downsides |
| Translucent in water at each light level | Kelp Mane |
| Less damage to the riders, also by damage type, and less Smite damage to the riders | Squires, Bone Barding, Squire Saddle, Storm Cloak, Helm Bond |
| An effect on a rider at low health, and an effect on the riders at death | Faithful Bones, Last Ride |
| A check that the reused effects work on a skeleton horse: armor, effect on hurt, a hop to water, knockback, dodge projectiles, smite resist, heal once, damage from golems, speed, translucent, speed at night, sidestep, no fall damage, flee, and the damage taken effects | Many genes |
