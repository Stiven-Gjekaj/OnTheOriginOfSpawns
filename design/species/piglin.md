# Piglin

This document defines the families and the genes of the piglin.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The piglin lives in the Nether. It has 16 health and does 5 melee damage. It
holds a gold sword or a crossbow. It attacks a player that wears no gold
armor. It takes gold items, looks at them, and barters for them. It hunts
hoglins, and it runs from zombified piglins and soul fire. In the Overworld,
it turns into a zombified piglin after 15 seconds. 20% spawn as babies.
Its genes fit the crossbow, the greed for gold, the hunt, the pack, and the
heat of the Nether. It shares no genes and no families with the other
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

### Sharpshots

Sharpshots hit hard with the crossbow. Each load takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more crossbow damage | It loads 10% slower |
| Tier 2 | 20% more crossbow damage | It loads 15% slower |
| Tier 1 | 15% more crossbow damage | It loads 20% slower |

### Hoarders

Hoarders do not trust gold armor. Their greed makes them weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It attacks a player in gold armor with a 30% chance | 10% less maximum health |
| Tier 2 | It attacks a player in gold armor with a 20% chance | 15% less maximum health |
| Tier 1 | It attacks a player in gold armor with a 15% chance | 20% less maximum health |

### Huntsmen

Huntsmen finish weak prey. They do not guard themselves.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more damage to a player under half health | 10% more damage taken |
| Tier 2 | 20% more damage to a player under half health | 15% more damage taken |
| Tier 1 | 15% more damage to a player under half health | 20% more damage taken |

### Packmates

Packmates fight together. Alone, each hit is weak.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | When it attacks a player, the piglins in 16 blocks attack too | 10% less damage |
| Tier 2 | When it attacks a player, the piglins in 12 blocks attack too | 15% less damage |
| Tier 1 | When it attacks a player, the piglins in 8 blocks attack too | 20% less damage |

### Firewalkers

Firewalkers resist the heat. Their thick skin makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 75% less fire damage | 10% less speed |
| Tier 2 | 50% less fire damage | 15% less speed |
| Tier 1 | 25% less fire damage | 20% less speed |

## Pressures

The piglin uses `fire`, `projectile`, `melee`, `explosion`, `fall`,
`unreached`, `spotted`, `alone`, `crowd`, `critical`, `golem`,
`player_death`, `in_water`, `on_land`, `wolf`, and `shield`. It lives in
the Nether, so it does not use `sunlight`, `night`, or `day`. It needs no
new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Sharpshot genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Quick Load | Sharpshot | `spotted` | It loads 30% faster | Its bolts spread two times as much | Arms: quick, thin fingers |
| Long Shot | Sharpshot | `unreached` | It shoots from 1.5 times as far | 10% less maximum health | Head: a narrow, squinting eye |
| Shield Piercer | Sharpshot | `shield` | Its bolts go through shields | 10% less maximum health | Arms: a bolt quiver on the arm. This changes the model |
| Fire Bolt | Sharpshot | `golem` | Its bolts set the target on fire for 4 seconds | 25% more fire damage | Arms: soot on the hands |
| Bolt Storm | Sharpshot | `crowd` | One time each 10 seconds, it shoots 3 bolts in a fan | It loads 10% slower | Body: a belt of bolts. This changes the model |
| Greedy Bolt | Sharpshot, sub Hoarder | `critical` | Each bolt hit takes 2 experience points from a player | 10% less maximum health | Head: gold rings in the ears. This changes the model |

### Hoarder genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Gold Wise | Hoarder | `spotted` | It attacks a player in gold armor with a 30% chance | 10% less maximum health | Head: narrow, suspicious eyes |
| Snatch | Hoarder | `melee` | Each melee hit has a 10% chance to make a player drop the item in the main hand | 10% less damage | Hands: long, gold-stained claws at the finger tips |
| Glitter Eye | Hoarder | `alone` | It sees invisible players in 16 blocks | 10% less speed | Head: gold-colored eyes |
| Gilded Hide | Hoarder | `projectile` | 30% less damage from projectiles | 10% less speed | Body: gold studs on the belt |
| Gold Lust | Hoarder | `crowd` | 30% more damage to a player in gold armor | 10% less maximum health | Body: a gold chain on the chest. This changes the model |
| Treasure Hunter | Hoarder, sub Huntsman | `critical` | Its first hit on a player does 30% more damage | 2 less armor | Head: a gold tooth |

### Huntsman genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Keen Nose | Huntsman | `alone` | Two times the follow range | 10% less speed | Head: a wide, wet snout |
| Wound Hunter | Huntsman | `critical` | 30% more damage to a player under half health | 10% less damage to a player over half health | Head: a hunter's scar |
| Spear Dash | Huntsman | `unreached` | It leaps at its target from 3 to 6 blocks away | 10% less maximum health | Legs: strong, bent legs |
| Hobble | Huntsman | `melee` | Its hits give Slowness for 2 seconds | 10% less damage | Arms: rope on the wrist |
| Shade Cloak | Huntsman | `spotted` | It is near invisible in the dark | 10% less maximum health | Body: a dark cloak. This changes the model |
| Tribe Hunter | Huntsman, sub Packmate | `crowd` | 20% more damage with 2 or more piglins near it | 20% less damage alone | Head: tribal paint |

### Packmate genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Battle Cry | Packmate | `spotted` | When it sees a player, it calls the piglins in 16 blocks | Players hear it from two times farther | Head: an open, loud snout |
| Shoulder Wall | Packmate | `projectile` | 20% less damage taken with 2 or more piglins near it | 20% more damage taken alone | Body: thick shoulder pads |
| Piglet | Packmate | `alone` | It spawns as a baby with a 25% chance | 10% less maximum health | Head: small, pink ears |
| Blood Kin | Packmate | `critical` | When it dies, the piglins in 16 blocks get Strength for 10 seconds | 4 less maximum health | Body: a kin mark on the chest |
| Kin Rally | Packmate | `melee` | 5% more damage for each piglin in 8 blocks, up to 20% | 2 less armor | Arms: a band of kin colors |
| Ember Kin | Packmate, sub Firewalker | `fire` | The piglins in 8 blocks get Fire Resistance | 10% less maximum health | Body: ember-colored stripes |

### Firewalker genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Fire Skin | Firewalker | `fire` | Half the fire damage | 10% less speed | Body: a dark, scorched skin |
| Overworld Lungs | Firewalker | `on_land` | In the Overworld, it turns into a zombified piglin after 60 seconds, not 15 | 10% less maximum health | Head: wide, flared nostrils, and a pale chest |
| Ash Lung | Firewalker | `explosion` | 30% less damage from explosions | 10% less speed | Body: gray ash on the chest |
| Hot Blade | Firewalker | `melee` | Its melee hits set the target on fire for 3 seconds | 10% less maximum health | Arms: glowing hands |
| Soul Brave | Firewalker | `shield` | It does not run from soul fire or from zombified piglins | 10% less maximum health | Head: blue, calm eyes |
| Blaze Bolt | Firewalker, sub Sharpshot | `projectile` | Its bolts set the target on fire for 4 seconds | 10% less speed | Arms: a glowing crossbow arm |

## Gene details

| Gene | Rules |
|---|---|
| Sharpshots, Quick Load, Bolt Storm | The bow genes of the skeleton work on the crossbow of the piglin, as for the pillager. They do nothing on a piglin with a sword |
| Hoarders, Gold Wise | A player in gold armor is safe in vanilla. With this gene, the piglin checks the chance one time for each player that it sees |
| Huntsmen, Wound Hunter | They count only while its target is a player |
| Packmates | It works as the pillager War Horn, when it attacks |
| Snatch | It works as the pillager Pickpocket, with melee hits |
| Greedy Bolt | It works as the pillager Greed, with bolts |
| Gold Lust | "Gold armor" is a player with at least one piece of gold armor |
| Treasure Hunter | It works as the spider Trapdoors |
| Spear Dash | It works as the zombie Lunger |
| Shade Cloak | It works as the creeper Gloom |
| Tribe Hunter, Shoulder Wall | They work as the pillager Formation |
| Battle Cry | It works as the pillager War Horn |
| Piglet | Piglins spawn as babies only through this gene, as zombies do with Runt |
| Blood Kin | It works as the pillager Grudge |
| Kin Rally | It works as the pillager Rally Cry |
| Ember Kin | It works as the witch Coven Ward, with Fire Resistance |
| Overworld Lungs | It changes the vanilla time of 15 seconds |
| Soul Brave | It stops the vanilla fear of soul fire, soul torches, soul lanterns, soul campfires, and zombified piglins |

## Equipment

- A piglin keeps its gold sword or its crossbow. It is its natural weapon.
- Mobs with genes do not spawn with random armor. A piglin with genes does
  not get its vanilla gold armor, and no random enchantment on its weapon.
- The barter of a piglin does not change. Its genes do not change what it
  gives for gold.

## Conversion

- In the Overworld, a piglin turns into a zombified piglin after 15
  seconds. The zombified piglin gets new genes from its own pool. It keeps
  none of the genes of the piglin.

## Babies and jockeys

- Piglins spawn as babies only through the Piglet gene.
- A baby piglin can ride a hoglin, as in vanilla. It does not get a gene for
  this.

## Body

- The piglin has the piglin model, with ears and a snout. Its texture is
  64 x 64, with an outer layer.
- It uses the piglin region map, `textures/region/piglin.png`, as the
  zombified piglin does.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Greedy Shots | Sharpshot | Hoarder | A crossbow for gold |
| Hunting Shots | Sharpshot | Huntsman | A crossbow that finishes prey |
| Volley Shots | Sharpshot | Packmate | A crossbow in a pack |
| Ember Shots | Sharpshot | Firewalker | A crossbow from the heat |
| Gold Snipers | Hoarder | Sharpshot | Greed with a crossbow |
| Gold Stalkers | Hoarder | Huntsman | Greed that hunts |
| Gold Gangs | Hoarder | Packmate | Greed in a pack |
| Gold Smelters | Hoarder | Firewalker | Greed in the heat |
| Bolt Hunters | Huntsman | Sharpshot | A hunt with bolts |
| Gold Trackers | Huntsman | Hoarder | A hunt for gold |
| Pack Trackers | Huntsman | Packmate | A hunt in a pack |
| Heat Trackers | Huntsman | Firewalker | A hunt in the heat |
| Volley Kin | Packmate | Sharpshot | A pack with crossbows |
| Gold Kin | Packmate | Hoarder | A pack for gold |
| Hunting Kin | Packmate | Huntsman | A pack that hunts |
| Fire Kin | Packmate | Firewalker | A pack in the heat |
| Ember Snipers | Firewalker | Sharpshot | Heat and bolts |
| Ember Hoarders | Firewalker | Hoarder | Heat and gold |
| Ember Trackers | Firewalker | Huntsman | Heat and a hunt |
| Ember Packs | Firewalker | Packmate | Heat and a pack |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Sharpshots".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Gold Gunners | Hoarder and Sharpshot | Bolts for gold |
| Bastion Snipers | Huntsman and Sharpshot | Bolts that finish prey |
| Bolt Packs | Packmate and Sharpshot | A pack of crossbows |
| Fire Gunners | Firewalker and Sharpshot | Burning bolts |
| Gold Hunts | Hoarder and Huntsman | A hunt for gold |
| Gold Tribes | Hoarder and Packmate | A tribe for gold |
| Gold Furnaces | Firewalker and Hoarder | Gold in the heat |
| Hunting Tribes | Huntsman and Packmate | A tribe that hunts |
| Ash Hunters | Firewalker and Huntsman | A hunt in the heat |
| Ember Tribes | Firewalker and Packmate | A tribe in the heat |

## Neutral

A neutral piglin has no genes, or no family points.
It is a plain piglin, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and gene models on the piglin, with the piglin region map of the zombified piglin | All visuals |
| The bow genes on the crossbow of a piglin | Sharpshots, Quick Load, Long Shot, Shield Piercer, Fire Bolt, Bolt Storm, Greedy Bolt, Blaze Bolt |
| A chance to attack a player in gold armor | Hoarders, Gold Wise |
| More damage to a player in gold armor | Gold Lust |
| A call when it attacks | Packmates |
| A baby gene for the piglin, and the tag `no_vanilla_babies` | Piglet |
| The time of the Overworld conversion | Overworld Lungs |
| No fear of soul fire and zombified piglins | Soul Brave |
| Disarm on melee hits | Snatch |
| A check that the reused effects work on a piglin: leap, effect on hit, translucent, see invisible, damage when grouped and alone, call on sight, death effect, rally, aura, ignite target, and the damage taken effects | Many genes |
