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

## Beagle Notebook

- The Beagle Notebook is an item.
- The notebook has one page for each species.
- A page shows gene frequencies, a chart of change over time, mutations, and
  a family tree.
- A page opens when the player first observes or kills a mob of that species.
- Notebook progress is the source of Insight.

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
