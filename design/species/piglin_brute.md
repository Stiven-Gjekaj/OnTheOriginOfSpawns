# Piglin Brute

This document defines the families and the genes of the piglin brute.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The piglin brute guards the bastions of the Nether. It has 50 health and
does 7 melee damage with a gold axe. It is always hostile. Gold does not
calm it, and it does not barter. It spawns only with a bastion, and it does
not spawn again. In the Overworld, it turns into a zombified piglin after 15
seconds. It has no babies.
Its genes fit the axe, the heavy body, the rage, the guard of the bastion,
and the break of shields and armor. It shares no genes and no families with
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

### Axemen

Axemen swing the gold axe with force. Each swing takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more melee damage | It attacks 10% slower |
| Tier 2 | 20% more melee damage | It attacks 15% slower |
| Tier 1 | 15% more melee damage | It attacks 20% slower |

### Colossi

Colossi do not move from a hit. Their weight makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Full knockback resistance and 4 more armor | 10% less speed |
| Tier 2 | Full knockback resistance and 3 more armor | 15% less speed |
| Tier 1 | Full knockback resistance and 2 more armor | 20% less speed |

### Maulers

Maulers fight harder when they bleed. They do not guard themselves.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 40% more damage while its health is under half | 10% more damage taken |
| Tier 2 | 30% more damage while its health is under half | 15% more damage taken |
| Tier 1 | 20% more damage while its health is under half | 20% more damage taken |

### Gatekeepers

Gatekeepers guard their place. Far from it, they are weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It heals 1 health each 2 seconds in 16 blocks of the place where it spawned | 10% less damage farther than 32 blocks from that place |
| Tier 2 | It heals 1 health each 3 seconds in 16 blocks of the place where it spawned | 15% less damage farther than 32 blocks from that place |
| Tier 1 | It heals 1 health each 4 seconds in 16 blocks of the place where it spawned | 20% less damage farther than 32 blocks from that place |

### Guardbreakers

Guardbreakers break guards and armor. Their axe is light.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its hits stop a shield for 5 seconds | 10% less damage |
| Tier 2 | Its hits stop a shield for 4 seconds | 15% less damage |
| Tier 1 | Its hits stop a shield for 3 seconds | 20% less damage |

## Pressures

The piglin brute uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `wolf`, and `shield`. It lives in
the Nether, so it does not use `sunlight`, `night`, or `day`. It needs no
new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Axeman genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Great Swing | Axeman | `melee` | 20% more melee damage | It attacks 15% slower | Arms: huge, scarred forearms |
| Head Splitter | Axeman | `critical` | 30% more damage to a player under half health | 10% less damage to a player over half health | Head: a bone helm crest. This changes the model |
| Wide Arc | Axeman | `crowd` | Each hit also hurts the other players in 2 blocks with half the damage | 10% less maximum health | Arms: wide shoulders with gold bands |
| Hurl Axe | Axeman | `unreached` | Each 10 seconds, it throws a small axe at a player 4 to 10 blocks away. It gives Slowness for 3 seconds | 10% less maximum health | Body: a row of small axes on the belt. This changes the model |
| Bone Breaker | Axeman | `fall` | Its hits knock the target back two times as far | It attacks 10% slower | Arms: bone rings on the wrist |
| Golden Edge | Axeman, sub Guardbreaker | `shield` | Its hits damage armor two times as much | 10% less speed | Arms: a gold edge on the arm |

### Colossus genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Iron Hide | Colossus | `melee` | 4 more armor | 10% less speed | Body: dark iron plates |
| Blast Wall | Colossus | `explosion` | Half the damage from explosions | 10% less speed | Body: a thick, scorched belt |
| Arrow Wall | Colossus | `projectile` | 30% less damage from projectiles | 10% less speed | Body: arrows stuck in the back. This changes the model |
| Steady Feet | Colossus | `golem` | Full knockback resistance | 10% less speed | Legs: heavy, gold greaves |
| Thick Neck | Colossus | `critical` | 6 more armor while its health is under half | 10% less maximum health | Head: a thick, wide neck |
| Rage Plate | Colossus, sub Mauler | `crowd` | 20% less damage taken with 2 or more piglin brutes near it | 20% more damage taken alone | Body: red marks on the plates |

### Mauler genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Blood Rage | Mauler | `melee` | Each hit heals it 1 health | 2 less armor | Head: red war paint |
| Charge | Mauler | `unreached` | 30% more speed while its health is under half | 10% less maximum health | Legs: wide, red stripes |
| Battle Scars | Mauler | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Body: many old scars |
| Roar | Mauler | `spotted` | Players in 6 blocks get Weakness for 3 seconds, each 15 seconds | Players hear it from two times farther | Head: a wide, roaring mouth |
| Frenzy Axe | Mauler | `crowd` | It attacks 20% faster | 10% less maximum health | Arms: red veins |
| Hot Head | Mauler, sub Gatekeeper | `fire` | It heals 1 health each 3 seconds out of combat | 25% more fire damage | Head: a red, hot snout |

### Gatekeeper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Home Guard | Gatekeeper | `alone` | 20% more damage in 16 blocks of the place where it spawned | 10% less speed | Body: a bastion banner cloth. This changes the model |
| Alarm Horn | Gatekeeper | `spotted` | When it sees a player, it calls the piglins and piglin brutes in 24 blocks | Players hear it from two times farther | Body: a gold horn on a cord. This changes the model |
| Gold Guard | Gatekeeper | `player_death` | It attacks a player that breaks a gold block or opens a chest in 16 blocks | 10% less maximum health | Head: gold rings on the snout |
| Watch Post | Gatekeeper | `projectile` | Two times the follow range | 10% less speed | Head: a narrow, watching eye |
| Lava Moat | Gatekeeper | `in_water` | Lava and fire do half the damage | 10% less maximum health | Legs: burnt, black boots |
| Golem Wrecker | Gatekeeper, sub Guardbreaker | `golem` | Two times the damage to golems | 10% less speed | Arms: iron studs on the knuckles |

### Guardbreaker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Guard Crusher | Guardbreaker | `shield` | Its hits stop a shield for 5 seconds | 10% less damage | Arms: a heavy, gold fist |
| Armor Splitter | Guardbreaker | `melee` | Its hits damage armor two times as much | 10% less maximum health | Arms: cracked gold bracers |
| Sunder Strike | Guardbreaker | `critical` | Its hits on a player with no shield up do 20% more damage | 10% less speed | Head: a cruel grin |
| Rust Axe | Guardbreaker | `golem` | Its hits give iron golems Weakness for 5 seconds | 10% less damage | Arms: rust on the hands |
| Shatter Blow | Guardbreaker | `explosion` | Its hits break the armor of a target with less than 25% armor durability | 10% less maximum health | Body: shards of armor on the belt |
| Brute Force | Guardbreaker, sub Axeman | `spotted` | Its first hit on a player does 30% more damage | 2 less armor | Arms: thick, short fingers |

## Gene details

| Gene | Rules |
|---|---|
| Axemen, Great Swing, Bone Breaker, Frenzy Axe | "Attacks slower" or "faster" changes the time between two melee attacks |
| Maulers, Charge, Thick Neck | "Its health is under half" is the `wounded` state |
| Gatekeepers, Home Guard | "The place where it spawned" is saved with the mob. A brute that a player moves keeps the old place |
| Head Splitter | It works as the vindicator Decapitator |
| Wide Arc | It works as the wither skeleton Cleave |
| Hurl Axe | It works as the vindicator Hatchet |
| Bone Breaker | It works as the vindicator Chop |
| Golden Edge, Armor Splitter | They work as the cave spider Corrosive |
| Rage Plate | It works as the pillager Formation |
| Blood Rage | It works as the vindicator Bloodlust |
| Battle Scars | It works as the vindicator Death Wish |
| Roar | It works as the vindicator Howl, with Weakness |
| Hot Head | It works as the vindicator Thick Blood |
| Alarm Horn | It works as the vindicator Hunting Call, with piglins and piglin brutes |
| Gold Guard | It works as the vanilla anger of the piglins at a gold block or a chest |
| Golem Wrecker | "Golems" are the iron golem and the snow golem |
| Guard Crusher, Guardbreakers | They work as the slime Shield Melt |
| Brute Force | It works as the spider Trapdoors |
| Shatter Blow | "Breaks" means the armor piece loses the rest of its durability |
| Sunder Strike | "A shield up" is a player that holds a shield up |

## Equipment

- A piglin brute keeps its gold axe. It is its natural weapon.
- A piglin brute with genes has no random enchantment on its axe from its
  spawn.
- A piglin brute cannot be the leader of a raid. It does not take part in a
  raid.

## Conversion

- In the Overworld, a piglin brute turns into a zombified piglin after 15
  seconds. The zombified piglin gets new genes from its own pool. It keeps
  none of the genes of the piglin brute.

## Babies and jockeys

- Piglin brutes have no babies. They do not get a gene for them.
- A piglin brute does not ride a mob. It does not get a gene for this.

## Body

- The piglin brute has the piglin model, with ears and a snout. Its texture
  is 64 x 64, with an outer layer.
- It uses the piglin region map, `textures/region/piglin.png`, as the
  zombified piglin does.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Iron Axemen | Axeman | Colossus | Heavy blows from a heavy body |
| Blood Axemen | Axeman | Mauler | Heavy blows in a rage |
| Gate Axemen | Axeman | Gatekeeper | Heavy blows at the gate |
| Crushing Axemen | Axeman | Guardbreaker | Heavy blows through a shield |
| Axe Towers | Colossus | Axeman | A tower with an axe |
| Raging Towers | Colossus | Mauler | A tower in a rage |
| Gate Towers | Colossus | Gatekeeper | A tower at the gate |
| Siege Towers | Colossus | Guardbreaker | A tower that breaks guards |
| Axe Maulers | Mauler | Axeman | A rage with an axe |
| Iron Maulers | Mauler | Colossus | A rage in iron |
| Gate Maulers | Mauler | Gatekeeper | A rage at the gate |
| Guard Maulers | Mauler | Guardbreaker | A rage that breaks guards |
| Axe Wardens | Gatekeeper | Axeman | A guard with an axe |
| Iron Wardens | Gatekeeper | Colossus | A guard in iron |
| Raging Wardens | Gatekeeper | Mauler | A guard in a rage |
| Breaker Wardens | Gatekeeper | Guardbreaker | A guard that breaks shields |
| Axe Breakers | Guardbreaker | Axeman | A broken shield, then the axe |
| Iron Breakers | Guardbreaker | Colossus | A broken shield, then the wall |
| Rage Breakers | Guardbreaker | Mauler | A broken shield, then the rage |
| Gate Breakers | Guardbreaker | Gatekeeper | A broken shield at the gate |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Axemen".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| War Towers | Axeman and Colossus | An axe on a wall |
| Butcher Kings | Axeman and Mauler | An axe in a rage |
| Bastion Lords | Axeman and Gatekeeper | An axe at the gate |
| Doom Axes | Axeman and Guardbreaker | An axe that breaks all |
| Rage Titans | Colossus and Mauler | A wall in a rage |
| Gold Walls | Gatekeeper and Colossus | A wall at the gate |
| Ram Titans | Colossus and Guardbreaker | A wall that breaks guards |
| Blood Wardens | Gatekeeper and Mauler | A rage at the gate |
| Wreck Brutes | Mauler and Guardbreaker | A rage that breaks guards |
| Vault Keepers | Gatekeeper and Guardbreaker | The guards of the treasure |

## Neutral

A neutral piglin brute has no genes, or no family points.
It is a plain piglin brute, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and gene models on the piglin brute, with the piglin region map | All visuals |
| The place where it spawned, saved with the mob, and states near it and far from it | Gatekeepers, Home Guard |
| Anger at a player that breaks a gold block or opens a chest near it | Gold Guard |
| More damage to a player with no shield up | Sunder Strike |
| A hit that breaks a worn armor piece at low durability | Shatter Blow |
| An effect on hit only on iron golems | Rust Axe |
| A call to piglins and piglin brutes | Alarm Horn |
| A check that the reused effects work on a piglin brute: shield break, armor break, cleave, throw effect, knockback, heal on hit, heal once, aura, attack interval, regenerate, first strike, damage to, damage when grouped and alone, and the damage taken effects | Many genes |
