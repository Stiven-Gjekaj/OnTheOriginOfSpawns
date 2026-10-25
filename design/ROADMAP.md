# Roadmap

This document puts the features in DESIGN.md in order.
Each milestone needs the milestones before it.
Milestones 0 to 5 and the beta stage make version 1.0.0.

## Releases

- Each finished milestone from 1 to 5 makes an alpha release.
  Each part of milestone 2 also makes an alpha release.
  An alpha release has the systems, but not all of the content.
- After milestone 5, the beta stage starts.
  Beta releases add the content, the balance, and the fixes.
- Version 1.0.0 comes when each vanilla hostile species has:
  - 30 genes.
  - 5 major families, each with a name, a buff, and a debuff.
  - 20 minor families, each with a name.
- Version 1.0.0 is not close.
  The alpha and beta releases build toward it.

## Version 1.0.0

### Milestone 0: Setup (done)

- Make a Fabric project for Minecraft 26.3.
- Make the build run in GitHub Actions.
- Read gene, perk, and pressure definitions from data packs.
- Save the gene pools with the world.
- Add debug commands that show and set a pool.

### Milestone 1: Core loop with zombies (done)

- Record the cause of each zombie death.
- Keep one zombie pool for each biome category.
- Add pressures that are conditions of the death, such as "unreached".
- Add three genes:
  - Cinder: fire resistance, and sets the player on fire. It takes damage from
    rain and water, and has less health.
  - Ironhide: resists projectiles and knockback. It is slower, and takes more
    explosion damage.
  - Bonerunner: faster, and chases longer. It has less armor, and takes more
    fall damage.
- Give each new zombie genes from its pool.
- Add mutation, pressure decay, and the limit of 5 genes for each mob.
- Give each gene its gains, its downsides, and a main family tag.
- Add the humanoid region map.
- Make the gene textures at load time from visual recipes.
- Add the bone legs model for Bonerunner.
- Add the Beagle Notebook with one zombie page.
  The page shows the gene frequencies of each biome category.
  The full notebook comes in milestone 4.

### Milestone 2: All hostile and neutral mobs

Milestone 2 has four parts. Each part makes its own alpha release.

#### Milestone 2a: Complete zombie (done)

- Add the family system: family points, the major family, the sub family,
  hybrids, pure families, and neutral mobs.
- Add the tier 1, tier 2, and tier 3 effects of the 5 zombie families.
- Add the names of the 20 minor families and the 10 hybrids.
- Add the effect types that the other 27 zombie genes need, for example
  leaps, sounds, auras, and clouds on death.
- Add the new pressures: sunlight, spotted, alone, crowd, critical, golem,
  and player death.
- Count the deaths of the player as data.
- Add the other 27 zombie genes, with their visuals.
- Make Runt the only source of baby zombies.
- Add the notebook tabs: a tab for each species, with family tabs and gene
  tabs.
- Add the mob info panel that shows when the player looks at a mob.
- Add the model changes of Bloat, Thickskull, and Sporebearer.

#### Milestone 2b: Husk and drowned (done)

The husk and the drowned come out together in one alpha release.

- Write the design files of the husk and the drowned.
- Give each one 5 new families, with their own names and themes.
  They do not copy the 5 zombie families.
- Add their 30 genes, their families, and their visuals.
- Give each one its own gene for babies, like Runt.
  Baby husks and baby drowned spawn only through that gene.
- When a mob converts to a different species, it gets new genes from the
  pool of the new species. It keeps none of its old genes.
- Draw the gene marks under the outer layer of the drowned, and fit them to
  the baby drowned model.
- Stop the random weapons, the random armor, and the item pickup of mobs
  with genes. Keep natural weapons, such as the trident of the drowned, and
  the items that genes give.
- Let a long-lived mob change one gene toward its pool, only where no player
  sees it. A mob with a name tag keeps its genes.

#### Milestone 2c: The other hostile and neutral mobs

- The order is: skeletons (skeleton, stray, bogged, wither skeleton,
  parched), the creeper, spiders (spider, cave spider), illagers (pillager,
  vindicator, evoker, witch), endermen (enderman, endermite), slimes (slime,
  magma cube), the Nether mobs (zombified piglin, piglin, piglin
  brute, hoglin, zoglin), the flyers (phantom, vex, ghast, blaze, breeze), the
  guardian, the undead mounts (camel husk, zombie nautilus, zombie horse,
  skeleton horse), and the others (silverfish, shulker, ravager, creaking).
- The zombie villager comes in milestone 6, with the villagers.
- The sulfur cube comes in milestone 6, with the mobs that the player breeds.
  It is passive. It breeds, and it swallows items that change its body.
- The neutral mobs that fight the player come after the hostile mobs: polar
  bear, bee, wolf, goat, dolphin, panda, llama, and iron golem.

- For each species, one at a time:
  - Write its design file.
  - Make the region map of its body type, if it is a new body type.
  - Add its genes, its families, and its visuals.

#### Milestone 2d: Systems between pools

- Let genes flow between neighbor categories.
- Add convergent evolution.
- Add extinction in a category.
- Set the speed of evolution from the difficulty and a gamerule.

### Milestone 3: Player perks and classes

- Add the first start pages in the notebook: a quote of Darwin, the
  inheritance page, and the pick of an inherited gene from 5 player families.
- Add the inherited genes. Each one is permanent, takes slot 1, and grows
  from tier 1 to tier 3 with use.
- Calculate the knowledge percentage.
- Open perk slots 2 to 8 at 0%, 15%, 30%, 45%, 60%, 75%, and 90%.
- Add the 30 perks with their XP cost, the change cost, and the exclusive
  pairs.
- Add the Lamarck and Darwin schools.
- Add epigenetics.
- Record the activity categories with more weight for recent activity.
- Make the 5 evolution classes the 5 player families. The inherited gene sets
  the main family, at tier 2. The play style sets the second family, at
  tier 1.
- Make the mobs counter the classes and the inherited families of the
  players.
- If a perk adds an element to the weapons of the player, such as water, add
  the other elements as a set at the same time: fire, water, frost, and the
  others that the mob families use. One element alone is not fair to the
  families that it counters.

### Milestone 4: Full notebook

- Add the charts of change over time.
- Add the family tree and the phylogenetic tree.
- Add field sketches that change with the genes.
- Add predictions.
- Add the advancements with names from Darwin's book.
- Draw the notebook as an open book: two facing pages with a leather edge.
- Use bookmark ribbons as the species tabs.
  Put the tabs for the gene page and the family page on the edge of the page.
- Show a small sketch of the mob on its page, with its real gene textures.
- Play a page-turn sound when the player changes a tab.

### Milestone 5: Multiplayer and the Royal Society

- Test shared pools and separate perks on a server.
- Add the Royal Society block and the XP cost of a share.
- Add the Naturalist villager.
- Add the village building that holds the block.

### Beta stage: content and balance

- Add the genes, the families, and the minor family names for each vanilla
  hostile species.
- Balance the genes, the families, the perks, and the classes.
- Fix the problems that players report.

## After version 1.0.0

### Milestone 6: Breeding, habitats, villages, and bosses

- Give genes to each mob that the player can breed, and add artificial
  selection.
- Give genes to the sulfur cube. Its babies, its food, and the items that it
  swallows fit artificial selection.
- Give genes to the wild mobs, and add habitat selection.
- Give genes to the villagers and the wandering trader, and add social
  selection.
- Give genes to the zombie villager from the same work as the villager. The
  profession of a villager that turns sets the family of its genes. A zombie
  villager that spawns naturally has a random profession, as in vanilla. A
  cured zombie villager keeps its genes.
- Give genes to the bosses, and add lineage.
- Add selected traits and pedigree pages.
- Add hybrids in border biomes.

### Milestone 7: Population genetics

- Add bottleneck, founder effect, invasive species, and genetic drift.
- Add mass extinction and adaptive radiation.

### Milestone 8: Species against species

- Add arms races between mobs.
- Add kin selection, sexual selection, vestigial traits, and mimicry.
- Add parasites.

### Milestone 9: Darwin content

- Add finches.
- Add the Galapagos island structure.
- Add the HMS Beagle shipwreck.

### Milestone 10: Fossils

- Add fossils in suspicious sand and suspicious gravel.
- Add the museum building.
- Add the sequencer block and the lab block.

### Milestone 11: End game

- Let the Wither and the Ender Dragon get genes from the pools.
- Add the last perk tier when the notebook records all families of one
  species.
- Add the Red Queen meter.
- Add lineage mode.

### Milestone 12: Sharing and compatibility

- Export the evolution of a world as an image or a web page.
- Add pressure from seasonal mods.
- Add the "global" pool scope option.
