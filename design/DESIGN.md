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

- A species is one mob type, for example zombie, skeleton, or creeper.
- Vanilla variants are different species.
  For example, zombie, husk, and drowned do not share pools or families.
  A husk evolves genes that fit sand and dry biomes.
- Each species has a gene pool.
- Each species has one pool for each biome category.
  The categories are hot, cold, wet, temperate, nether, and end.
- A small part of each pool flows to the pools of neighbor categories.
  Thus, genes move slowly across the world.
- A configuration option sets the pool scope to "global" or "per biome".
  This option comes after version 1.0.0.

## Selection

- Each pool counts the causes of death of its members.
  Examples of causes are fire, arrows, melee, explosions, and falls.
- A pressure can also be a condition of the death.
  For example, a zombie that dies before it hits the player counts for the
  "unreached" pressure.
  This happens when the player runs away or wins the fight quickly.
- A death counts for "unreached" when the mob targeted a player and did not
  damage a player before it died.
- The "unreached" pressure does not select for attack speed.
  Attack speed comes from the "melee" pressure.
- The list of pressures is not final.
  The mod adds pressures for new genes.
- A common cause of death makes the gene that counters it stronger.
- The mod weights each death by its cause, not by its player.
  Thus, one player cannot control a pool alone.
- Each new mob gets its genes from its pool.
- A mob can also change its genes after it spawns, but only where no player
  sees it:
  - The mob must be alive for one Minecraft day or more.
  - No player is in 32 blocks, and no player can see the mob.
  - At a check about once each minute, a small chance swaps one gene.
    The mob drops its weakest gene for a gene that its pool now favors.
    Then its family is found again.
  - A mob with a name tag never changes its genes.
  - This changes only the mob. It does not change the pool.
- Random mutation makes small changes to the pool.
- Each death raises the gene that counters its pressure by 2% of the
  distance to 100%.
  For example, about 50 fire deaths take Cinder from 0% to about 60%.
- Each Minecraft day, decay lowers each frequency by 1% of its value.
- Mutation gives each gene a small chance to go to a new mob, also at 0%.
- These values are first drafts.
- Each gene has a cost.
  For example, fire resistance lowers the maximum health.
- Each gene changes how the mob looks.
  The player can see the threat before the fight.

## Kinds of genes

- Genes work like player perks.
  Each gene has a name, one or more gains, and one or more downsides.
  The gains and the downsides have one theme.
- For example, the "Cinder" gene:

| Gains | Downsides |
|---|---|
| Fire resistance | Rain and water damage the mob |
| The mob sometimes sets the player on fire | Less health, because the mob is burnt |

- The kinds below are the parts that a gene can use.
- Each species has 30 genes at version 1.0.0.
  Some genes are for one species only, and some are for more species.
- Resistance genes decrease damage from one cause.
  Examples are fire and arrows.
- Speed genes make the mob faster.
  The "unreached" pressure selects for speed.
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

## Gene visuals

- Each gene adds a small but distinct mark to the mob.
  The mob stays easy to recognize.
  A player who knows the genes can read the history of a mob.
- The marks of different genes go on different body parts.
  Thus, all genes can combine on one mob, and no mark hides another.
- The mod does not have a texture for each mix of genes.
  The game makes the textures when it loads, from the texture of the mob.
- Each body type has a region map.
  A region map is an image that labels each pixel of the mob texture.
  The regions are skin, shirt, pants, hands, feet, and head.
- One region map serves all mobs with the same body type.
  For example, the humanoid map serves zombies, husks, drowned, and
  piglins.
- Each gene has a visual recipe.
  The recipe applies an effect to a region.
  Examples of effects are a color change, spots, and a pattern.
- A gene can also change the model.
  This is rare.
  It is only for a gene that a texture cannot show.
- Milestone 1 has one model change: Bonerunner.
- Milestone 2a adds model changes for Bloat, Thickskull, and Sporebearer.
- The marks must not clutter the mob.
  First, the modeller makes the marks small and puts them on different
  pixels.
  If marks still collide, the region map gets smaller regions, for example
  mouth, eyes, brow, chest, belly, back, and sleeves.
- The user reviews the visuals with one preview sheet for each family: 6
  genes on white, front and back.
- Data packs define the visual recipes.
- The modeller makes the region maps and the patterns with code.
  The mod does not use Blockbench.

These are the visuals of the first zombie genes:

| Gene | Region | Visual |
|---|---|---|
| Cinder | Skin | The skin is more gray |
| Cinder | Hands and feet | Dark spots and red spots |
| Ironhide | Shirt | Plates that look like iron |
| Bonerunner | Lower legs | Bare bone, thicker than the legs of a skeleton |

The Bonerunner gene changes the model.
The bone legs are narrower than the zombie legs, but wider than the legs of
a skeleton.
Thus, the bones look worn, not undead.

## Balance

- Pressure decays.
  If players stop a cause of death, the gene that counters it fades slowly.
- Each mob has a maximum of 5 genes.
  The genes under the most pressure come first.
  Thus, a species cannot have all defenses at the same time.
- Each gene has a gain and a downside.
- Mobs of a species with genes do not get weapons or armor that the vanilla
  game gives at random, and they do not pick up items.
  For example, a zombie does not spawn with a sword, a shovel, or armor.
- A mob keeps the weapons and armor that are natural to it.
  For example, a drowned keeps its trident, and a skeleton keeps its bow.
  A husk that rides a camel husk keeps its iron spear, because the rider and
  its spear are one vanilla mob design.
- A gene can give a weapon, armor, or item pickup when that is its effect.
  For example, Tridentborn gives a trident, and Packmule picks up items and
  wears armor.
- The difficulty sets the speed of evolution.
  Peaceful has no evolution.
  Hard has the fastest evolution.
- A gamerule overrides the speed.

## Extinction

- If players kill a species too much in a category, the species goes extinct
  in that category.
  An extinct species does not spawn there.
- Families replace named variants.
  Refer to "Families".

## Families

- Each species has a file in [species/](species/) with its families and genes.
  For example, [species/zombie.md](species/zombie.md).
- Each species has 5 major families.
  Each species has its own families, with its own names.
  For example, a zombie family can be "Predators".
- Each major family has one big buff and one big debuff, at 3 tiers.
- A higher tier makes the buff stronger and the debuff weaker.
  Thus, a mob gets stronger at a higher tier.
- The tier 3 buff is about two times the tier 2 buff.
  Thus, a pure family is stronger than a hybrid.
- Each gene has a main family tag.
  The main tag gives 2 points to its family.
- A small number of genes also have a sub family tag.
  The sub tag gives 1 point to a second family.
  Only a gene that truly belongs to two families gets a sub tag.
- Families are different for each species.
  Thus, a gene that more than one species can carry has different tags for
  each species.
- For example, Cinder has Stalker as its main tag for zombies.
- Each mob gets its own genes.
  Each gene in the pool has a chance to go to a new mob, equal to its
  frequency.
  Thus, two mobs from the same pool can be different.
- The family points of the genes of a mob select its families.
  For example, a zombie with speed, fire resistance, faster attacks, less
  health, and more damage on death is a Predator.
- The family with the most points is the major family.
- The family with the second most points is the sub family.
- The major family and the sub family make a minor family.
  For example, a Predator with a Tank sub family is a different minor family
  from a Predator with a Stalker sub family.
- Each minor family has its own name.
  The name is not only the names of its major and sub family.
- Each hybrid has its own name.
- A pure family has the name of its major family.

| Points of the genes | Result |
|---|---|
| One family has 6 or more points (3 genes), and no other family has points | Pure family: tier 3 effects from the major |
| The major family has 4 or more points (2 genes), and a second family has 2 or more | Minor family: tier 2 effects from the major, tier 1 effects from the sub |
| Two families have the same points, 4 or more each | Hybrid: tier 2 effects from each of the two |
| The major family has 4 or more points, and no second family has 2 or more | Major family alone: tier 2 effects from the major |
| The major family has fewer than 4 points | Neutral: no family effects |

- Thus, a mob with 0 or 1 gene is neutral.
  A mob with genes of 3 different families, one gene each, is also neutral.

- For example, a pure Predator gets the tier 3 Predator effects.
  A Predator with a Tank sub family gets the tier 2 Predator effects and the
  tier 1 Tank effects.
- This is the order of power, from the strongest to the weakest:
  1. Pure families.
  2. Hybrids.
  3. Minor families.
  4. Neutral mobs.
- If two families have the same number of points for the sub family, the
  family of the gene with the highest frequency wins.
- At the start of a world, all mobs are neutral.
  Evolution can also make a mob neutral again by chance.
- Each species has these family results:

| Result | Number |
|---|---|
| Minor families: 5 majors, each with one of the other 4 as sub | 20 |
| Pure major families | 5 |
| Hybrids: one for each pair of families | 10 |
| Neutral | 1 |
| Total | 36 |

- Mobs in the same family still differ by their genes.
- The notebook records each family that the player finds.

## Species against species

- Mobs evolve against other mobs.
  For example, wolves hunt skeletons, so skeletons evolve to run.
  Then wolves evolve more speed.
  The world evolves when the player is not near.
- Mobs that kill the player get more of the genes that did the kill.
  Each gene of the killer rises by its selection rate.
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
- The player gets full knowledge only from a fight that ends in a death:
  - When the player kills a mob, the player learns about that mob.
  - When a mob kills the player, the player learns about that mob.
  - A mob that dies near the player in a different way teaches nothing.
- The player gets partial knowledge from a mob that is near:
  - The mob is in 16 blocks, and the player has a clear line of sight to it.
  - After 8 seconds near the mob, in total, the player sees its family.
  - A seen family shows its name, but not its genes.
- The mob info panel shows only what the player knows:
  - An unknown family shows "???", and its genes show "???".
  - A seen family shows its name. Its genes show "???".
  - A known family shows its name and its genes.
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

Version 1.0.0 has these 30 perks:

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

## Evolution classes

- Each player has passive evolution classes.
  The player does not select a class.
  The play style of the player selects it.
- The mod records the activity of the player in categories.
  These are the first categories:

| Class | Activity |
|---|---|
| Builder | Blocks placed |
| Fighter | Kills and damage dealt |
| Explorer | Distance traveled, new biomes, and new structures |
| Miner | Blocks mined under the surface |
| Farmer | Crops harvested and animals bred |

- More categories can come later.
- Each category has a percentage of the total activity of the player.
- Recent activity has more weight than old activity.
  The weight of each activity point decreases with time.
  Thus, the classes change when the play style changes.
- Each class has two tiers of effects.
  Each tier has a gain and a cost.
  The cost is smaller than the cost of a perk.
- The classes come from the two highest categories:

| Condition | Highest category | Second category |
|---|---|---|
| The highest category is 10 or more points above the second | Main class: tier 2 effects | Tier 1 effects |
| The highest category is less than 10 points above the second | Tier 1 effects | Tier 1 effects |

- The other categories give no effects.
- The mobs counter the classes of the players.
  For example, a world of Fighters selects for tougher mobs.
  A world of Explorers selects for faster mobs.
- The notebook shows the percentage of each category and the current classes.
- Data packs define the classes, their activities, and their effects.

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

## Mob info on hover

- When the player looks at a mob, a small panel shows at the top of the
  screen.
  Mods such as AppleSkin and Jade show information in a similar way.
- The panel shows short information: the family of the mob and its dominant
  genes.

## Beagle Notebook

- The notebook has a tab for each species.
  Each species tab has family tabs and gene tabs.

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
- The player completes the notebook when it records all families of one
  species.
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
- It has three genes: Cinder, Ironhide, and Bonerunner.
- Each gene changes how the zombie looks.
- It has one notebook page for zombies.
