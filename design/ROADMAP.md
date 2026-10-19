# Roadmap

This document puts the features in DESIGN.md in order.
Each milestone needs the milestones before it.
Milestones 0 to 5 and the beta stage make version 1.0.0.

## Releases

- Each finished milestone from 1 to 5 makes an alpha release.
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

### Milestone 2: All hostile mobs

- Add the 5 major families of each species, with tier 1 and tier 2 effects.
- Add minor families from the major family and the sub family.
- Add hybrids and neutral mobs.
- Add pools for all vanilla hostile mobs.
- Add behavior genes, sense genes, and life-history genes.
- Let genes flow between neighbor categories.
- Count the deaths of the player as data.
- Add convergent evolution.
- Add extinction in a category.
- Set the speed of evolution from the difficulty and a gamerule.

### Milestone 3: Player perks and classes

- Calculate the knowledge percentage.
- Open perk slots at 0%, 20%, 40%, 60%, and 80%.
- Add the 30 perks with their XP cost, the change cost, and the exclusive
  pairs.
- Add the Lamarck and Darwin schools.
- Add epigenetics.
- Record the activity categories with more weight for recent activity.
- Add the evolution classes with tier 1 and tier 2 effects.
- Make the mobs counter the classes of the players.

### Milestone 4: Full notebook

- Add the charts of change over time.
- Add the family tree and the phylogenetic tree.
- Add field sketches that change with the genes.
- Add predictions.
- Add the advancements with names from Darwin's book.

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

### Milestone 6: Breeding

- Give genes to cows, sheep, wolves, and horses.
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
