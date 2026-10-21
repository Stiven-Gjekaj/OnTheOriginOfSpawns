# Wither Skeleton

This document defines the families and the genes of the wither skeleton.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The wither skeleton lives in Nether fortresses. It is tall, it fights with a
stone sword, and each hit gives Wither for 10 seconds. Fire and Wither do not
hurt it. Its genes fit swordplay, the fire of the Nether, the Wither curse,
the guards of the fortress, and the souls of the soul sand valleys.
It shares no genes and no families with the skeleton, the stray, or the
bogged.

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

### Blademasters

Blademasters are masters of the sword. They do not guard against arrows.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more melee damage, and 20% faster attacks | 10% more damage taken from projectiles |
| Tier 2 | 20% more melee damage | 20% more damage taken from projectiles |
| Tier 1 | 10% more melee damage | 25% more damage taken from projectiles |

### Ashborn

Ashborn carry the fire of the Nether. Cold and water hurt them, as they hurt a blaze.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each hit sets the target on fire for 5 seconds | Snowballs do 1 damage. Splash water hurts it, and freezing does 25% more damage |
| Tier 2 | Each hit has a 50% chance to set the target on fire for 4 seconds | Snowballs do 2 damage. Splash water hurts it, and freezing does 50% more damage |
| Tier 1 | Each hit has a 25% chance to set the target on fire for 3 seconds | Snowballs do 3 damage. Splash water hurts it, and freezing does 75% more damage |

### Witherlords

Witherlords carry a stronger curse. The curse makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | The Wither from its hits lasts two times longer and is one level stronger | 10% less speed |
| Tier 2 | The Wither from its hits lasts two times longer | 15% less speed |
| Tier 1 | The Wither from its hits lasts 50% longer | 20% less speed |

### Ramparts

Ramparts guard the fortress. Their heavy bones make them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 6 more armor, and full knockback resistance | 10% less speed |
| Tier 2 | 4 more armor, and full knockback resistance | 15% less speed |
| Tier 1 | 2 more armor | 20% less speed |

### Soul Reapers

Soul Reapers feed on souls. Their bodies are thin.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Each melee hit heals it 2 health | 10% less maximum health |
| Tier 2 | Each melee hit heals it 1.5 health | 20% less maximum health |
| Tier 1 | Each melee hit heals it 1 health | 25% less maximum health |

## Pressures

The wither skeleton uses the zombie pressures, and `wolf` and `shield`. It
also uses this pressure.

| Pressure | A wither skeleton death counts when | State |
|---|---|---|
| `smite` | The killer hit it with a weapon with the Smite enchantment | New |

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Blademaster genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Duelist | Blademaster | `alone` | 30% more melee damage with no other wither skeleton in 8 blocks | 2 less armor | Arms: a red cloth tied around the sword arm. This changes the model |
| Parry | Blademaster | `melee` | It takes no damage from a melee hit, one time each 6 seconds | 10% less maximum health | Arms: a bone guard on the forearm. This changes the model |
| Cleave | Blademaster | `crowd` | Its hits also do half damage to other players in 2 blocks of the target | It attacks 15% slower | Body: a wide belt of bone plates |
| Lunge | Blademaster, sub Soul Reaper | `unreached` | It leaps at a player 3 to 6 blocks away | After a missed leap, it cannot move for 1 second | Legs: long, thin shins |
| Bladestorm | Blademaster | `projectile` | It attacks 30% faster | 15% less melee damage | Arms: notches cut in the arm bones |
| Executioner | Blademaster | `critical` | 50% more melee damage to a player under half health | 10% less speed | Head: a black hood over the skull. This changes the model |

### Ashborn genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Cinderblade | Ashborn | `melee` | Each hit sets the target on fire for 4 seconds | Snowballs do 2 damage to it | Hands: glowing cracks of ember. This changes the model |
| Pyre | Ashborn | `critical` | When it dies, fire burns where it died | 4 less maximum health | Ribs: coals that glow in the rib cage. This changes the model |
| Lavawalker | Ashborn | `fall` | 50% more speed in lava | 10% less speed out of lava | Legs: dried lava on the shins |
| Cauterize | Ashborn | `fire` | It heals 1 health each 2 seconds while it stands in fire or lava | 25% more damage from projectiles | Bones: orange seams of heat |
| Smoke Screen | Ashborn, sub Rampart | `spotted` | When it is hurt, the attacker gets Blindness for 2 seconds, each 15 seconds | 10% less melee damage | Body: smoke that rises from the shoulders |
| Ember Eyes | Ashborn | `golem` | Its hits set iron golems on fire for 8 seconds, and do 50% more damage to them | 10% less speed | Eyes: orange flames that glow. This changes the model |

### Witherlord genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Deep Wither | Witherlord | `melee` | The Wither from its hits is one level stronger | It attacks 15% slower | Bones: black veins that spread from the joints |
| Wither Aura | Witherlord | `crowd` | Players in 4 blocks get Wither for 2 seconds, each 5 seconds | Players hear it from two times farther | Body: black dust that falls from the ribs |
| Decay Touch | Witherlord | `projectile` | Its hits give Weakness for 4 seconds | 10% less melee damage | Hands: black, rotten fingers |
| Skull Shot | Witherlord, sub Ashborn | `unreached` | Each 10 seconds, it shoots a wither skull at a player 6 to 16 blocks away | 10% less maximum health | Head: a second small skull on the shoulder. This changes the model |
| Black Rot | Witherlord | `shield` | Its hits damage the armor of a player two times as much | 2 less armor | Ribs: black rot in the bone |
| Grave Chill | Witherlord | `fall` | Its hits give Slowness for 3 seconds | 10% less melee damage | Skull: frost in the eye sockets |

### Rampart genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Brickplate | Rampart | `projectile` | 6 more armor | 15% less speed | Chest: a plate of nether brick. This changes the model |
| Holdfast | Rampart | `explosion` | Full knockback resistance, and 40% less explosion damage | 10% less speed | Feet: wide feet of blackstone. This changes the model |
| Sentinel | Rampart | `spotted` | When it sees a player, the wither skeletons in 24 blocks target that player. It can call again after 30 seconds | Players hear it from two times farther | Head: a tall crest of bone. This changes the model |
| Brickshield | Rampart | `projectile` | 50% less damage from projectiles that hit it from the front | It attacks 15% slower | Arms: a nether brick shield on the left arm. This changes the model |
| Garrison | Rampart, sub Witherlord | `alone` | Its spawn groups have 1 more wither skeleton | Each wither skeleton of the group has 15% less maximum health | Body: a red sash of the fortress |
| Smite Ward | Rampart | `smite` | 50% less damage from Smite weapons | 4 less maximum health | Skull: gold runes on the brow |

### Soul Reaper genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Soulstride | Soul Reaper | `unreached` | 40% more speed on soul sand and soul soil | 10% less speed on other blocks | Feet: blue soul fire at the feet that glows. This changes the model |
| Soul Link | Soul Reaper | `crowd` | It heals 4 health when another wither skeleton dies in 12 blocks | 10% less speed | Chest: a blue soul flame in the ribs that glows. This changes the model |
| Vengeful Spirit | Soul Reaper, sub Witherlord | `critical` | When it dies, a vex comes out and attacks its killer | 4 less maximum health | Shoulder: a pale blue ghost that curls up from the back over the shoulder and glows. This changes the model |
| Soul Hunger | Soul Reaper | `alone` | 20% more melee damage for each player that it hit in the last 10 seconds, up to 60% | 10% less maximum health | Skull: blue soul light in the eyes and the mouth, with thin blue cracks |
| Ghost Step | Soul Reaper | `projectile` | 25% chance to dodge a projectile | 10% less melee damage | Legs: faded and see-through |
| Soul Drain | Soul Reaper | `melee` | Its hits take 1 food from a player | 2 less armor | Hands: blue light between the fingers |

## Gene details

| Gene | Rules |
|---|---|
| Witherlords, Deep Wither | They change the Wither that the wither skeleton gives with each hit in vanilla, 10 seconds of Wither I. "One level stronger" is Wither II |
| Ashborn, Cinderblade | Water cannot be placed in the Nether, and it does not rain there. So the counters are the ones that work there: snowballs, splash water bottles, and freezing, such as powder snow and the arrows of the stray Frostbiters. "Splash water hurts it" uses the vanilla damage of splash water to a blaze |
| Parry | It blocks the whole hit. A sound of a sword block plays |
| Cleave | The half damage hits players only. It does not cleave again |
| Lunge | It works like the zombie Lunger, with a range of 3 to 6 blocks |
| Pyre | It places fire only in air, on a block that can hold fire, and not where a player stands |
| Lavawalker | "In lava" means that the feet of the wither skeleton are in lava |
| Cauterize | "In fire" means that the feet of the wither skeleton are in fire or soul fire |
| Skull Shot | It needs a clear line of sight. The skull is the black skull of the Wither boss. It gives Wither for 10 seconds |
| Black Rot | It works as the stray Brittle Shot, but for melee hits, and on each player |
| Smite Ward | It works as the skeleton Holy Ward, with no effect on wolves |
| Vengeful Spirit | The vex lives 30 seconds. It counts as a mob of the vex, with no genes |
| Soul Hunger | A player counts one time, also with many hits |
| Soulstride | "Soul sand and soul soil" means the blocks in the tag `#minecraft:soul_speed_blocks` |

## Equipment

- A wither skeleton keeps its stone sword. The sword is its natural weapon.
- Mobs with genes do not spawn with random armor. A wither skeleton with
  genes has no armor and no enchanted sword from its spawn.

## Conversion

- The wither skeleton does not convert to a different species, and no species
  converts to the wither skeleton.

## Babies and jockeys

- Wither skeletons have no babies and no jockeys in vanilla. They do not get a
  gene for them.

## Body

- The wither skeleton has the body of the skeleton, 1.2 times larger.
- It uses the skeleton region map. The gene models scale with the body.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Flamberges | Blademaster | Ashborn | Burning blades |
| Cursed Blades | Blademaster | Witherlord | Swords that rot all that they cut |
| Fortress Knights | Blademaster | Rampart | Armored swordsmen |
| Soul Blades | Blademaster | Soul Reaper | Swords that drink life |
| Hellblades | Ashborn | Blademaster | Fire that fights with a sword |
| Black Flames | Ashborn | Witherlord | Fire and curse in one body |
| Brazier Guards | Ashborn | Rampart | Burning guards of the gate |
| Will-o-Wisps | Ashborn | Soul Reaper | Blue fire that feeds on souls |
| Grim Blades | Witherlord | Blademaster | The curse with a fast sword |
| Pyre Lords | Witherlord | Ashborn | Lords of black fire |
| Crypt Wardens | Witherlord | Rampart | Heavy keepers of the curse |
| Soul Blights | Witherlord | Soul Reaper | Curse and hunger together |
| Wall Breakers | Rampart | Blademaster | Guards that hit hard |
| Ember Walls | Rampart | Ashborn | Walls of hot stone |
| Black Bastions | Rampart | Witherlord | Fortresses of the curse |
| Soul Wardens | Rampart | Soul Reaper | Guards that never fall |
| Reaper Blades | Soul Reaper | Blademaster | Hunters of souls with a sword |
| Soul Pyres | Soul Reaper | Ashborn | Souls that burn blue |
| Hollow Kings | Soul Reaper | Witherlord | Old kings that drain all life |
| Soul Keepers | Soul Reaper | Rampart | Guards of the soul valleys |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Blademasters".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Hellknights | Blademaster and Ashborn | Burning knights of the Nether |
| Deathblades | Blademaster and Witherlord | Fast swords with a deep curse |
| Blackguards | Blademaster and Rampart | Swordsmen in heavy bone |
| Soul Duelists | Blademaster and Soul Reaper | Duelists that drink life |
| Infernals | Ashborn and Witherlord | Fire and curse together |
| Kiln Guards | Ashborn and Rampart | Guards of the burning gates |
| Blue Infernos | Ashborn and Soul Reaper | Blue fire and hungry souls |
| Dread Walls | Witherlord and Rampart | Walls that curse |
| Lich Lords | Witherlord and Soul Reaper | Masters of death |
| Soul Bastions | Rampart and Soul Reaper | Fortresses of the soul valleys |

## Neutral

A neutral wither skeleton has no genes, or no family points.
It is a plain wither skeleton, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The change of the Wither on the hits of the wither skeleton | Witherlords, Deep Wither |
| A block of one melee hit, with a cooldown | Parry |
| A hit that also hurts other players near the target | Cleave |
| States for lava and for fire at the feet, and for soul sand | Lavawalker, Cauterize, Soulstride |
| A wither skull that the mob shoots | Skull Shot |
| Armor break on melee hits | Black Rot |
| More damage for each player that the mob hit in a time | Soul Hunger |
| A vex that comes out on death and attacks the killer | Vengeful Spirit |
| More damage and fire on iron golems | Ember Eyes |
| The pressure `smite` | Smite Ward |
| Damage from snowballs, and damage from splash water | Ashborn, Cinderblade |
| The gene textures and the gene models on the larger body | All visuals |
