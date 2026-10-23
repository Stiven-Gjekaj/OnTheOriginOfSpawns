# Evoker

This document defines the families and the genes of the evoker.
[DESIGN.md](../DESIGN.md) gives the rules for families and genes.
All values are first drafts. The beta stage balances them.

The evoker is an illager that casts spells. It has 24 health and no weapon.
It lives in woodland mansions and comes in raids. Each 5 seconds, it calls
up evoker fangs from the ground, which do 6 damage. Each 17 seconds, it
summons 3 vexes. It turns blue sheep red. It runs from players in 8 blocks.
When it dies, it drops a totem of undying.
Its genes fit the fangs, the vexes, the curses, the life magic of the totem,
and the tricks of a mage that keeps away from its enemy. It shares no genes
and no families with the other species.

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

### Fangcallers

Fangcallers put all their power into the fangs. Each spell takes longer.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | 30% more fang damage | 10% more time between spells |
| Tier 2 | 20% more fang damage | 15% more time between spells |
| Tier 1 | 15% more fang damage | 20% more time between spells |

### Conjurers

Conjurers make stronger vexes. They give their own life to them.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its vexes have 50% more health and do 20% more damage | 10% less maximum health |
| Tier 2 | Its vexes have 30% more health and do 15% more damage | 15% less maximum health |
| Tier 1 | Its vexes have 20% more health and do 10% more damage | 20% less maximum health |

### Cursers

Cursers curse with each spell. Their fangs are weaker.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | Its fangs give Weakness for 6 seconds | 10% less fang damage |
| Tier 2 | Its fangs give Weakness for 4 seconds | 15% less fang damage |
| Tier 1 | Its fangs give Weakness for 2 seconds | 20% less fang damage |

### Lifebinders

Lifebinders use the magic of the totem to heal. The magic makes them slow.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It heals 1 health each 2 seconds out of combat | 10% less speed |
| Tier 2 | It heals 1 health each 3 seconds out of combat | 15% less speed |
| Tier 1 | It heals 1 health each 4 seconds out of combat | 20% less speed |

### Tricksters

Tricksters step away from arrows and bolts. They cannot take a hit.

| Tier | Buff | Debuff |
|---|---|---|
| Tier 3 | It steps aside from an aimed shot, one time each 6 seconds | 10% more damage taken |
| Tier 2 | It steps aside from an aimed shot, one time each 9 seconds | 15% more damage taken |
| Tier 1 | It steps aside from an aimed shot, one time each 12 seconds | 20% more damage taken |

## Pressures

The evoker uses `fire`, `projectile`, `melee`, `explosion`, `unreached`,
`spotted`, `alone`, `crowd`, `critical`, `golem`, `player_death`,
`in_water`, `on_land`, `night`, `day`, `wolf`, `shield`, and `raid`. The sun
does not burn it, so it does not use `sunlight`. It needs no new pressure.

## Genes

Each gene has a main tag. Five genes also have a sub tag.
The visual column gives the region and the mark.

### Fangcaller genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Fang Line | Fangcaller | `unreached` | Its line of fangs is 24 blocks long, not 16 | 10% less maximum health | Body: a row of teeth on the robe |
| Fang Ring | Fangcaller | `melee` | It casts the ring of fangs at a target in 5 blocks, not 3 | 10% more time between spells | Arms: rings of teeth on the sleeves |
| Barbed Fangs | Fangcaller, sub Curser | `spotted` | Its fangs give Slowness II for 2 seconds | 10% less fang damage | Hands: hooked claws. This changes the model |
| Swift Cast | Fangcaller | `crowd` | 30% less time between spells | 10% less maximum health | Head: bright, glowing eyes |
| Golem Jaws | Fangcaller | `golem` | Its fangs do two times the damage to golems | 10% less speed | Body: an iron jaw on the belt. This changes the model |
| Upheaval | Fangcaller | `shield` | Its fangs throw the target 1 block up | 10% more damage taken | Legs: cracked earth on the boots |

### Conjurer genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Vex Swarm | Conjurer | `crowd` | It summons 4 vexes, not 3 | 10% less maximum health | Body: vex wings on the back. This changes the model |
| Iron Vexes | Conjurer | `melee` | Its vexes have two times the health | 10% more time between spells | Arms: iron bands |
| Long Bond | Conjurer | `alone` | Its vexes live two times as long | 10% less speed | Hands: a pale thread at each wrist |
| Sharp Vexes | Conjurer, sub Fangcaller | `spotted` | Its vexes do 30% more damage | 10% less fang damage | Head: a vex skull on the hood. This changes the model |
| Vengeful Vexes | Conjurer | `critical` | When it dies, 3 vexes come out and attack the killer | 4 less maximum health | Body: a cage of bone on the chest |
| Venom Vexes | Conjurer | `unreached` | The hits of its vexes give Poison for 3 seconds | 10% less maximum health | Arms: green runes |

### Curser genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Wither Hex | Curser | `spotted` | Each 15 seconds, it gives its target Wither for 4 seconds, from up to 16 blocks | 10% less maximum health | Head: black runes on the face |
| Blind Hex | Curser | `unreached` | Each 15 seconds, it gives its target Blindness for 3 seconds, from up to 16 blocks | 10% less speed | Head: a cloth over one eye. This changes the model |
| Weak Hex | Curser | `melee` | Players in 4 blocks get Weakness | 10% less fang damage | Hands: gray, withered hands |
| Hunger Hex | Curser | `alone` | Each 15 seconds, it gives its target Hunger for 10 seconds, from up to 16 blocks | 10% less maximum health | Body: a belt of dry bones |
| Drain Hex | Curser, sub Lifebinder | `critical` | Each fang hit heals it 2 health | 2 less armor | Body: red runes on the robe |
| Float Hex | Curser | `shield` | Each 15 seconds, it gives its target Levitation for 2 seconds, from up to 16 blocks | 10% less speed | Arms: feathers on the sleeves |

### Lifebinder genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Totem Bearer | Lifebinder | `critical` | One time in its life, it heals to full health at under 25% health | 4 less maximum health | Body: a gold totem on the chest. This changes the model |
| Soul Mend | Lifebinder | `alone` | It heals 1 health each 3 seconds out of combat | 10% less fang damage | Head: green, glowing eyes |
| Life Siphon | Lifebinder | `crowd` | Each hit of its vexes heals it 1 health | 10% less speed | Hands: a green glow |
| Arrow Ward | Lifebinder | `projectile` | 30% less damage from projectiles | 15% less speed | Arms: gold bracers |
| Fire Ward | Lifebinder | `fire` | Half the damage from fire | 10% less maximum health | Body: a gold sash |
| Blast Ward | Lifebinder, sub Trickster | `explosion` | Half the damage from explosions | 10% less speed | Legs: gold greaves |

### Trickster genes

| Gene | Tags | Pressure | Gains | Downsides | Visual |
|---|---|---|---|---|---|
| Blink | Trickster | `projectile` | It steps aside from an aimed shot, one time each 8 seconds | 10% less maximum health | Head: a purple glint in the eyes |
| Smoke Veil | Trickster | `melee` | When something hurts it, the players in 4 blocks get Blindness for 2 seconds, one time each 20 seconds | 10% less fang damage | Body: a gray smoke on the robe |
| Fleet | Trickster | `spotted` | 20% more speed | 10% less maximum health | Legs: light, soft boots |
| Shimmer | Trickster | `unreached` | It is near invisible in the dark | 10% less maximum health | Body: a shimmer on the robe |
| Decoy | Trickster, sub Conjurer | `crowd` | When something hurts it, it summons 1 vex, one time each 20 seconds | 10% less speed | Hands: a vex charm. This changes the model |
| Mind Trick | Trickster | `golem` | Iron golems do not target it | 10% less fang damage | Head: a purple mask. This changes the model |

## Gene details

| Gene | Rules |
|---|---|
| All | "Fangs" are the evoker fangs of its attack spell. "Golems" are the iron golem and the snow golem |
| Fangcallers, Fang Ring, Iron Vexes, Swift Cast | "Time between spells" is the cast interval of the fang spell and the vex spell |
| Conjurers, Iron Vexes, Long Bond, Sharp Vexes, Venom Vexes | They change only the vexes that the evoker summons. A vex has no genes |
| Lifebinders, Soul Mend | "Out of combat" means 5 seconds with no hit |
| Tricksters, Blink | They work as the pillager Evasion |
| Wither Hex, Blind Hex, Hunger Hex, Float Hex | A hex needs a line of sight to the target. It throws no item. It is a spell, with the spell particles of the evoker |
| Weak Hex | It works as the pillager Banner Bearer, on players |
| Totem Bearer | It works as the vindicator Death Wish. The evoker still drops its totem |
| Shimmer | It works as the creeper Gloom, at light level 7 or less |
| Vengeful Vexes | The vexes live 30 seconds |
| Mind Trick | It works as the spider Holy Ward, with iron golems in place of wolves. A golem that it hurts still attacks it |

## Equipment

- The evoker holds no weapon. A gene gives it no weapon.
- The evoker drops its totem of undying, as in vanilla.
- An evoker cannot be the leader of a raid wave. Only a pillager with a
  Captain gene can be, as the pillager design says.
- The evoker still turns blue sheep red.

## Conversion

- The evoker does not convert to a different species, and no species
  converts to the evoker.

## Babies and jockeys

- Evokers have no babies in vanilla. They do not get a gene for them.
- An evoker can ride a ravager in a raid, as in vanilla. It does not get a
  gene for this.

## Body

- The evoker has the illager model, with crossed arms when it does not cast.
  When it casts, it raises its arms. Its texture is 64 x 64.
- It uses the illager region map, `textures/region/illager.png`.
- The gene models use the illager bones. A model on an arm shows only while
  the arms are apart, as for the vindicator.

## Minor families

The major family comes first, and the sub family comes second.

| Minor family | Major | Sub | Play style |
|---|---|---|---|
| Biting Swarms | Fangcaller | Conjurer | Fangs and vexes together |
| Cursed Fangs | Fangcaller | Curser | Fangs that curse |
| Feeding Fangs | Fangcaller | Lifebinder | Fangs that heal |
| Trap Mages | Fangcaller | Trickster | Fangs from a mage that steps away |
| Vex Callers | Conjurer | Fangcaller | Vexes behind a wall of fangs |
| Hex Binders | Conjurer | Curser | Vexes and curses |
| Life Keepers | Conjurer | Lifebinder | Vexes that keep it alive |
| Illusionists | Conjurer | Trickster | Vexes as a mask |
| Fang Witches | Curser | Fangcaller | Curses with strong fangs |
| Vex Witches | Curser | Conjurer | Curses with strong vexes |
| Blood Cursers | Curser | Lifebinder | Curses that heal |
| Shadow Cursers | Curser | Trickster | Curses from the dark |
| Bone Menders | Lifebinder | Fangcaller | Heals behind the fangs |
| Spirit Menders | Lifebinder | Conjurer | Heals with vexes |
| Dark Menders | Lifebinder | Curser | Heals and curses |
| Warded Mages | Lifebinder | Trickster | Wards and tricks |
| Fang Dancers | Trickster | Fangcaller | Tricks with fangs |
| Vex Dancers | Trickster | Conjurer | Tricks with vexes |
| Jinxers | Trickster | Curser | Tricks and curses |
| Runaways | Trickster | Lifebinder | Tricks that heal |

## Pure families

A pure family has all its points in one major family.
It has the name of its major family, for example "Fangcallers".
It gives the tier 3 effects of that family.

## Hybrids

| Hybrid | Families | Play style |
|---|---|---|
| Archmages | Conjurer and Fangcaller | Fangs and vexes |
| Doom Casters | Fangcaller and Curser | Fangs that curse |
| Life Eaters | Fangcaller and Lifebinder | Fangs that feed it |
| Sly Fangs | Fangcaller and Trickster | Fangs from a mage that is hard to hit |
| Necromancers | Conjurer and Curser | Vexes and curses |
| Spirit Guardians | Conjurer and Lifebinder | Vexes that guard its life |
| Phantasmists | Conjurer and Trickster | Vexes and tricks |
| Witch Doctors | Curser and Lifebinder | Curses and heals |
| Hex Tricksters | Curser and Trickster | Curses and tricks |
| Sly Healers | Lifebinder and Trickster | Heals and tricks |

## Neutral

A neutral evoker has no genes, or no family points.
It is a plain evoker, with no family effects.

## New engine work

These genes and families need parts that the mod does not have yet.

| Need | Used by |
|---|---|
| The gene textures and the gene models on the evoker. The arms show apart while it casts | All visuals |
| Fang damage, and an effect that the fangs give on a hit | Fangcallers, Cursers, Barbed Fangs, Sharp Vexes, Weak Hex, Soul Mend, Smoke Veil, Mind Trick |
| The time between spells | Fangcallers, Fang Ring, Iron Vexes, Swift Cast |
| The length of the fang line, and the range of the fang ring | Fang Line, Fang Ring |
| More fang damage to a tag of mobs | Golem Jaws |
| Fangs that throw the target up | Upheaval |
| Fang hits that heal the evoker | Drain Hex |
| The vex count, health, damage, life, and an effect on a vex hit | Conjurers, Vex Swarm, Iron Vexes, Long Bond, Sharp Vexes, Venom Vexes |
| A vex hit that heals the evoker | Life Siphon |
| A hex: an effect on the target at range, with no item | Wither Hex, Blind Hex, Hunger Hex, Float Hex |
| A vex summon when something hurts the evoker | Decoy |
| A check that the reused effects work on an evoker: aura, sidestep, effect on hurt, translucent, ignored by, death spawn, heal once, regenerate, and the damage taken effects | Many genes |
