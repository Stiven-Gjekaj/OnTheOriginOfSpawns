# Design

This document records the design decisions for On the Origin of Spawns.
Change this document when a decision changes.

## Platform

- The mod uses Fabric.
- The mod targets Minecraft 26.3.
  This is the newest stable version that Fabric supports on 2026-10-19.
- Reason: 1.21.1 and 1.21.11 have more Fabric mods now.
  But the 26.x versions ship without obfuscation, and the mod does not need
  a port to 26.x later.
- The mod uses Fabric Loader 0.19.5 or a later version.
- NeoForge support is not in scope.

## Gene pools

- Each species has a gene pool.
- Each species has one pool for each biome category.
  The categories are hot, cold, wet, temperate, nether, and end.
- A small part of each pool flows to the pools of neighbor categories.
  Thus, genes move slowly across the world.
- A configuration option sets the pool scope to "global" or "per biome".
  This option comes after the first release.

## Selection

- Each pool counts the causes of death of its members.
  Examples of causes are fire, drowning, arrows, melee, explosions, and falls.
- A common cause of death makes the gene that counters it stronger.
- The mod weights each death by its cause, not by its player.
  Thus, one player cannot control a pool alone.
- Each new mob gets its genes from its pool.
- Random mutation makes small changes to the pool.
- Each gene has a cost.
  For example, fire resistance lowers water resistance.
- Each gene changes how the mob looks.
  The player can see the threat before the fight.

## Kinds of genes

- Resistance genes decrease damage from one cause.
  Examples are fire, water, and arrows.
- Behavior genes change the AI of the mob.
  - Mobs that die in the open start to attack from cover.
  - Mobs that die alone start to move in groups.
  - Creepers that die before they explode get a shorter fuse.
- Sense genes change how the mob finds the player.
  - Mobs that die in the dark get night vision.
  - Mobs get good hearing, so the player cannot sneak past them.
  - Mobs that are easy to see in daylight get camouflage.
- Life-history genes change the number of spawns.
  A pool can make a small number of strong mobs or a large number of weak
  mobs.
- Mimicry is a rare zombie mutation.
  The zombie looks like a villager until the player comes near.

## Balance

- Pressure decays.
  If players stop a cause of death, the gene that counters it fades slowly.
- Each mob has a fixed number of gene points.
  Thus, a species cannot have all defenses at the same time.
- The difficulty sets the speed of evolution.
  Peaceful has no evolution.
  Hard has the fastest evolution.
- A gamerule overrides the speed.

## Extinction and speciation

- If players kill a species too much in a category, the species goes extinct
  in that category.
  An extinct species does not spawn there.
- If a pool moves far from its start, it becomes a new named variant.

## Player adaptations

- The player earns Insight when they study a species.
  To study a species, the player observes it, kills it in different ways,
  and finds mutations.
- Each Insight milestone opens a perk slot.
- To take a perk, the player pays XP levels.
- Each perk has a trade-off.
  For example, "thick hide" decreases damage taken by 10% and decreases speed
  by 10%.
- The player can change a perk.
  A change has a small cost.

These are the first perks:

| Perk | Gain | Cost |
|---|---|---|
| Thick hide | 10% less damage taken | 10% less speed |
| Night eyes | The player sees in the dark | The sun blinds the player |
| Cold blood | Hunger decreases more slowly | The player is weak in snow biomes |
| Gills | The player breathes longer under water | The player loses air slowly on land |
| Lean frame | More sprint speed | 2 fewer hearts |
| Scent mask | Mobs notice the player later | Animals run from the player |

## Breeding

- Passive mobs carry genes.
  Examples are cows, sheep, wolves, and horses.
- The player selects the parents.
  This is artificial selection.
- The player breeds for traits.
  Examples are wool color, wool density, milk yield, wolf loyalty, and horse
  stamina.
- Each bred line gets a pedigree page in the notebook.

## Beagle Notebook

- The Beagle Notebook is an item.
- The notebook has one page for each species.
- A page shows gene frequencies, a chart of change over time, mutations, and
  a family tree.
- A page opens when the player first observes or kills a mob of that species.
- Notebook progress is the source of Insight.
- A phylogenetic tree shows when each variant split from its species.
- Field sketches show each mob with its current genes.
  The sketch changes when the genes change.
- The notebook gives predictions.
  For example: "Zombies in hot biomes move toward fire immunity."

## Darwin content

- Finches are a new small bird.
  The shape of the beak changes with the biome.
  Finches are a collectible in the notebook.
- The Galapagos is a rare island structure.
  Its gene pools are isolated, and they evolve quickly.
- The HMS Beagle is a shipwreck variant.
  Its loot holds the first Beagle Notebook.
- Advancement names come from the chapters of Darwin's book.
  Examples are "Struggle for Existence", "Variation Under Domestication", and
  "Extinction".

## World events

- A mass extinction resets the pools of one category to their base genes.
  It is a rare event.
  A boss kill can also cause it.
- Adaptive radiation follows a mass extinction.
  The next species in that category evolves two times faster for a period.

## Multiplayer

- All players in a world share one set of gene pools.
- Each player has a different notebook and different perks.

## Royal Society

- The Royal Society lets players share notebook knowledge.
- A block does the share.
  The player puts two or more notebooks in the block.
  Each notebook gets the pages that the others have.
- A share costs XP levels.
  The cost increases with the number of new pages.
- The block is the job site of a new villager, the Naturalist.
- A new village building holds the block.
  Thus, a player can find the block before they can craft it.

## First milestone

- The first milestone is a small test of the full loop.
- It has zombies only, in one biome category.
- It has three genes: fire resistance, water resistance, and arrow resistance.
- Each gene changes how the zombie looks.
- It has one notebook page for zombies.
