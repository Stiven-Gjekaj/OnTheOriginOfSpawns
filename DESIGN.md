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
- Data packs define the genes, the perks, and the pressures in JSON.
  Thus, pack makers can add their own.
  The code must read these definitions from the start.

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

## Species against species

- Mobs evolve against other mobs.
  For example, wolves hunt skeletons, so skeletons evolve to run.
  Then wolves evolve more speed.
  The world evolves when the player is not near.
- Mobs that kill the player get more of the genes that did the kill.
  The deaths of the player are data too.
- Convergent evolution: two species under the same pressure can grow the same
  trait.
  For example, burned spiders and burned zombies both get char plates.
  The notebook shows the match.

## Population genetics

- Bottleneck: after a large cull, the survivors have little variety.
  The pool locks onto a small number of traits.
  These traits are strong, but the pool is fragile.
- Founder effect: mobs that go through a Nether portal or in a boat start a
  new pool.
  The new pool has only their genes.
- Invasive species: a player can lead a mob into a different biome.
  The mob competes with the local pool, and it can replace it.
- Genetic drift: small, isolated pools change at random with no pressure.

## Other biology

- Hybrids: related species can breed in border biomes and mix their traits.
  Examples are zombie with husk, and skeleton with stray.
- Vestigial traits: a lost gene leaves a visual mark, such as faded char.
  The gene comes back faster under the same pressure.
- Kin selection: related mobs defend each other.
  If the player hits one mob, its siblings attack.
- Sexual selection: a bright trait makes a mob spawn more.
  The same trait makes the mob easier to see.
- Parasites: a parasite moves through dense groups of mobs.
  Thus, the player has a reason to keep mixed spawners.

## Player adaptations

- The notebook shows a knowledge percentage.
  It is the part of all notebook entries that the player has filled.
  Entries include species pages, observed genes, variants, mutations, and
  fossils.
- The knowledge percentage sets the number of perk slots.

| Knowledge | Perk slots |
|---|---|
| 0% | 1 |
| 20% | 2 |
| 40% | 3 |
| 60% | 4 |
| 80% | 5 |

- The maximum is 5 slots.
- Shared pages from the Royal Society also increase the knowledge percentage.
- To take a perk, the player pays XP levels.
- Each perk has a trade-off.
- The player can change a perk.
  A change has a small cost.
- Some perks cannot be used together.
  For example, the player cannot have Ruminant and Carnivore at the same time.

The first release has these 30 perks:

| Perk | Gain | Cost |
|---|---|---|
| Thick hide | 10% less damage taken | 10% less speed |
| Night eyes | The player sees in the dark | The sun blinds the player |
| Cold blood | Hunger decreases more slowly | The player is weak in snow biomes |
| Gills | The player breathes longer under water | The player loses air slowly on land |
| Lean frame | More sprint speed | 2 fewer hearts |
| Scent mask | Mobs notice the player later | Animals run from the player |
| Hollow bones | Less fall damage | More knockback taken |
| Hard shell | 2 more armor points | The player sinks in water |
| Heat sink | Less fire and lava damage | Powder snow freezes the player faster |
| Ruminant | The player can eat grass and leaves | Meat gives less food |
| Carnivore | Raw meat is safe and gives full food | Plant food gives half food |
| Fat stores | Saturation lasts longer | Slower swim speed |
| Adrenaline | Speed and strength at low health | Slowness and hunger after the burst |
| Axolotl blood | Faster natural healing | Healing uses more hunger |
| Venom glands | Melee attacks poison the target | Healing potions heal less |
| Echolocation | Sneaking shows mobs through walls | Mobs hear the player from farther |
| Keen nose | The player sees the trails of near mobs | Rotten flesh and zombies cause nausea |
| Long arms | 1 more block of reach | Slower attack speed |
| Claws | An empty hand digs dirt and sand fast | Tools lose durability faster |
| Gecko pads | The player climbs walls while sneaking | More fall damage |
| Spring legs | Higher jumps | Jumps use more hunger |
| Camouflage skin | Invisible when still in grass or leaves | Works only with no chestplate |
| Nocturnal | More damage at night | Less damage in the day |
| Hibernation | Sleep restores all hunger and health | Slowness for one minute after sleep |
| Pack instinct | More damage near tame wolves or players | Less damage when alone |
| Symbiosis | Near tame animals heal the player slowly | The player takes part of their damage |
| Immune memory | Each bad effect is shorter after the player survives it | Good effects are also shorter |
| Tough stomach | No bad effects from rotten flesh, spider eyes, or pufferfish | Golden apples give less |
| Bioluminescence | The player gives light | Mobs see the player from farther at night |
| Mimic scent | Zombies ignore the player until the player attacks | Villagers give worse trades, and iron golems do not trust the player |

## Player evolution modes

- Lineage mode is for hardcore play.
  When the player dies, the player continues as their own offspring.
  The offspring gets some of the perks, with a small mutation.
  The notebook keeps a family tree of the past players.
- The player selects a school at the start of the world.
  - Lamarck: perks grow from use.
    This is slow, but it costs nothing.
  - Darwin: the player selects perks at milestones.
    This is fast, but the offers are random.
- Epigenetics: the place where the player lives gives a short buff.
  For example, a week in the desert gives heat tolerance.
  The buff fades when the player leaves.

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
- Notebook progress sets the knowledge percentage.
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

## Fossils

- Suspicious sand and suspicious gravel can hold fossils.
  The fossils come from extinct variants in the history of the same world.
- A museum building displays fossils and specimens.
  A full set gives a reward.
- A sequencer block reads the genes of a captured mob or of a mob drop.
  The notebook then shows the exact values.
- A lab block uses fossils to bring back an extinct variant.
  It can also add old genes to a pool.
  This is late game content.

## End game

- The Wither and the Ender Dragon read the gene pools when they spawn.
  They get the strongest genes in the world.
- The player completes the notebook when it records a full speciation.
  This opens the last perk tier.
- The Red Queen meter shows who leads the arms race: the players or the mobs.
  It is one value for the world.

## Sharing and compatibility

- The player can export the evolution of the world as an image or a web page.
- Seasons from seasonal mods add pressure.
  For example, winter selects for cold traits.

## First milestone

- The first milestone is a small test of the full loop.
- It has zombies only, in one biome category.
- It has three genes: fire resistance, water resistance, and arrow resistance.
- Each gene changes how the zombie looks.
- It has one notebook page for zombies.
