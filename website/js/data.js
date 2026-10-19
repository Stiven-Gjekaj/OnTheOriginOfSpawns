// This file is made by website/build.mjs. Do not edit it.
window.SITE_DATA = {
 "generated": "2026-10-19",
 "repo": "https://github.com/Stiven-Gjekaj/OnTheOriginOfSpawns",
 "version": "0.1.0-alpha",
 "minecraft": "26.3",
 "fabric": "0.19.5",
 "links": {
  "modrinth": "https://modrinth.com/mod/tvKIdALe",
  "curseforge": "https://www.curseforge.com/projects/1754344",
  "kofi": "https://ko-fi.com/stivengjekaj",
  "github": "https://github.com/Stiven-Gjekaj/OnTheOriginOfSpawns",
  "releases": "https://github.com/Stiven-Gjekaj/OnTheOriginOfSpawns/releases/latest"
 },
 "tagline": "Adapt or go extinct",
 "subtitle": "A Fabric mod where the mobs evolve from how you kill them",
 "overview": [
  "On the Origin of Spawns makes the mobs in your world evolve. Each mob that you kill changes the next generation. Burn many zombies, and their offspring start to resist fire. Outrun them, and their offspring get faster.",
  "You evolve too, but you choose how. Select small adaptations, each with a trade-off. Then watch the mobs adapt to counter you. Record each species in your Beagle Notebook while natural selection and artificial selection race each other."
 ],
 "features": [
  "A gene pool for each species in each biome category: hot, cold, wet, temperate, nether, and end",
  "The cause of each death changes the pool: fire, arrows, melee, explosions, falls, sunlight, critical hits, iron golems, mobs that die alone or in a crowd, and mobs that die before they reach you",
  "Genes that change how mobs look, fight, sense, and spawn",
  "A downside for each gene, and a maximum of 5 genes for each mob",
  "5 major families and 20 minor families for each species, with hybrids",
  "Extinction of a species in a biome category",
  "30 player perks, each with a gain and a cost",
  "1 to 5 perk slots, from the knowledge percentage of your notebook",
  "A panel that shows the family and the genes of the mob that you look at",
  "The Beagle Notebook, with a tab for each species, a page of genes, a page of the families that you found, and later charts, family trees, and predictions",
  "The Royal Society block and the Naturalist villager, to share notebook pages for XP",
  "Gene pools that all players in a world share",
  "Genes, families, perks, and pressures in data packs"
 ],
 "roadmap": [
  {
   "key": "Milestone 0",
   "title": "Setup",
   "done": true,
   "after1": false,
   "items": [
    "Make a Fabric project for Minecraft 26.3.",
    "Make the build run in GitHub Actions.",
    "Read gene, perk, and pressure definitions from data packs.",
    "Save the gene pools with the world.",
    "Add debug commands that show and set a pool."
   ]
  },
  {
   "key": "Milestone 1",
   "title": "Core loop with zombies",
   "done": true,
   "after1": false,
   "items": [
    "Record the cause of each zombie death.",
    "Keep one zombie pool for each biome category.",
    "Add pressures that are conditions of the death, such as \"unreached\".",
    "Add three genes:",
    "Give each new zombie genes from its pool.",
    "Add mutation, pressure decay, and the limit of 5 genes for each mob.",
    "Give each gene its gains, its downsides, and a main family tag.",
    "Add the humanoid region map.",
    "Make the gene textures at load time from visual recipes.",
    "Add the bone legs model for Bonerunner.",
    "Add the Beagle Notebook with one zombie page. The page shows the gene frequencies of each biome category. The full notebook comes in milestone 4."
   ]
  },
  {
   "key": "Milestone 2",
   "title": "All hostile mobs",
   "done": false,
   "after1": false,
   "items": []
  },
  {
   "key": "Milestone 2a",
   "title": "Complete zombie",
   "done": false,
   "after1": false,
   "items": [
    "Add the family system: family points, the major family, the sub family, hybrids, pure families, and neutral mobs.",
    "Add the tier 1, tier 2, and tier 3 effects of the 5 zombie families.",
    "Add the names of the 20 minor families and the 10 hybrids.",
    "Add the effect types that the other 27 zombie genes need, for example leaps, sounds, auras, and clouds on death.",
    "Add the new pressures: sunlight, spotted, alone, crowd, critical, golem, and player death.",
    "Count the deaths of the player as data.",
    "Add the other 27 zombie genes, with their visuals.",
    "Make Runt the only source of baby zombies.",
    "Add the notebook tabs: a tab for each species, with family tabs and gene tabs.",
    "Add the mob info panel that shows when the player looks at a mob.",
    "Add the model changes of Bloat, Thickskull, and Sporebearer."
   ]
  },
  {
   "key": "Milestone 2b",
   "title": "Husk and drowned",
   "done": false,
   "after1": false,
   "items": [
    "Write the design files of the husk and the drowned.",
    "Add their 30 genes, their families, and their visuals."
   ]
  },
  {
   "key": "Milestone 2c",
   "title": "The other hostile mobs",
   "done": false,
   "after1": false,
   "items": [
    "For each species, one at a time:"
   ]
  },
  {
   "key": "Milestone 2d",
   "title": "Systems between pools",
   "done": false,
   "after1": false,
   "items": [
    "Let genes flow between neighbor categories.",
    "Add convergent evolution.",
    "Add extinction in a category.",
    "Set the speed of evolution from the difficulty and a gamerule."
   ]
  },
  {
   "key": "Milestone 3",
   "title": "Player perks and classes",
   "done": false,
   "after1": false,
   "items": [
    "Calculate the knowledge percentage.",
    "Open perk slots at 0%, 20%, 40%, 60%, and 80%.",
    "Add the 30 perks with their XP cost, the change cost, and the exclusive pairs.",
    "Add the Lamarck and Darwin schools.",
    "Add epigenetics.",
    "Record the activity categories with more weight for recent activity.",
    "Add the evolution classes with tier 1 and tier 2 effects.",
    "Make the mobs counter the classes of the players."
   ]
  },
  {
   "key": "Milestone 4",
   "title": "Full notebook",
   "done": false,
   "after1": false,
   "items": [
    "Add the charts of change over time.",
    "Add the family tree and the phylogenetic tree.",
    "Add field sketches that change with the genes.",
    "Add predictions.",
    "Add the advancements with names from Darwin's book.",
    "Draw the notebook as an open book: two facing pages with a leather edge.",
    "Use bookmark ribbons as the species tabs. Put the tabs for the gene page and the family page on the edge of the page.",
    "Show a small sketch of the mob on its page, with its real gene textures.",
    "Play a page-turn sound when the player changes a tab."
   ]
  },
  {
   "key": "Milestone 5",
   "title": "Multiplayer and the Royal Society",
   "done": false,
   "after1": false,
   "items": [
    "Test shared pools and separate perks on a server.",
    "Add the Royal Society block and the XP cost of a share.",
    "Add the Naturalist villager.",
    "Add the village building that holds the block."
   ]
  },
  {
   "key": "Beta stage",
   "title": "content and balance",
   "done": false,
   "after1": false,
   "items": [
    "Add the genes, the families, and the minor family names for each vanilla hostile species.",
    "Balance the genes, the families, the perks, and the classes.",
    "Fix the problems that players report."
   ]
  },
  {
   "key": "Milestone 6",
   "title": "Breeding",
   "done": false,
   "after1": true,
   "items": [
    "Give genes to cows, sheep, wolves, and horses.",
    "Add selected traits and pedigree pages.",
    "Add hybrids in border biomes."
   ]
  },
  {
   "key": "Milestone 7",
   "title": "Population genetics",
   "done": false,
   "after1": true,
   "items": [
    "Add bottleneck, founder effect, invasive species, and genetic drift.",
    "Add mass extinction and adaptive radiation."
   ]
  },
  {
   "key": "Milestone 8",
   "title": "Species against species",
   "done": false,
   "after1": true,
   "items": [
    "Add arms races between mobs.",
    "Add kin selection, sexual selection, vestigial traits, and mimicry.",
    "Add parasites."
   ]
  },
  {
   "key": "Milestone 9",
   "title": "Darwin content",
   "done": false,
   "after1": true,
   "items": [
    "Add finches.",
    "Add the Galapagos island structure.",
    "Add the HMS Beagle shipwreck."
   ]
  },
  {
   "key": "Milestone 10",
   "title": "Fossils",
   "done": false,
   "after1": true,
   "items": [
    "Add fossils in suspicious sand and suspicious gravel.",
    "Add the museum building.",
    "Add the sequencer block and the lab block."
   ]
  },
  {
   "key": "Milestone 11",
   "title": "End game",
   "done": false,
   "after1": true,
   "items": [
    "Let the Wither and the Ender Dragon get genes from the pools.",
    "Add the last perk tier when the notebook records all families of one species.",
    "Add the Red Queen meter.",
    "Add lineage mode."
   ]
  },
  {
   "key": "Milestone 12",
   "title": "Sharing and compatibility",
   "done": false,
   "after1": true,
   "items": [
    "Export the evolution of a world as an image or a web page.",
    "Add pressure from seasonal mods.",
    "Add the \"global\" pool scope option."
   ]
  }
 ],
 "families": [
  {
   "id": "predator",
   "name": "Predators",
   "summary": "Predators chase and hit hard. They are fast and fragile.",
   "tiers": [
    {
     "buff": "10% more speed",
     "debuff": "20% less maximum health",
     "buffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:movement_speed",
       "amount": 0.1,
       "operation": "add_multiplied_total"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:max_health",
       "amount": -0.2,
       "operation": "add_multiplied_total"
      }
     ]
    },
    {
     "buff": "20% more speed, and 2 more attack damage",
     "debuff": "15% less maximum health",
     "buffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:movement_speed",
       "amount": 0.2,
       "operation": "add_multiplied_total"
      },
      {
       "type": "attribute",
       "attribute": "minecraft:attack_damage",
       "amount": 2,
       "operation": "add_value"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:max_health",
       "amount": -0.15,
       "operation": "add_multiplied_total"
      }
     ]
    },
    {
     "buff": "40% more speed, and 4 more attack damage",
     "debuff": "10% less maximum health",
     "buffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:movement_speed",
       "amount": 0.4,
       "operation": "add_multiplied_total"
      },
      {
       "type": "attribute",
       "attribute": "minecraft:attack_damage",
       "amount": 4,
       "operation": "add_value"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:max_health",
       "amount": -0.1,
       "operation": "add_multiplied_total"
      }
     ]
    }
   ]
  },
  {
   "id": "stalker",
   "name": "Stalkers",
   "summary": "Stalkers hide and attack first. They are weak in the light.",
   "tiers": [
    {
     "buff": "Quieter steps, and the first hit does 50% more damage",
     "debuff": "25% more damage taken at light level 12 or more",
     "buffs": [
      {
       "type": "loud",
       "multiplier": 0.5
      },
      {
       "type": "damage_dealt_when",
       "when": "ambush",
       "multiplier": 1.5
      }
     ],
     "debuffs": [
      {
       "type": "damage_taken_when",
       "when": "bright",
       "multiplier": 1.25
      }
     ]
    },
    {
     "buff": "No step sounds, and the first hit on a player does double damage",
     "debuff": "20% more damage taken at light level 12 or more",
     "buffs": [
      {
       "type": "silence",
       "steps": true
      },
      {
       "type": "damage_dealt_when",
       "when": "ambush",
       "multiplier": 2
      }
     ],
     "debuffs": [
      {
       "type": "damage_taken_when",
       "when": "bright",
       "multiplier": 1.2
      }
     ]
    },
    {
     "buff": "No sounds at all, and the first hit on a player does triple damage",
     "debuff": "10% more damage taken at light level 12 or more",
     "buffs": [
      {
       "type": "silence",
       "idle": true,
       "hurt": true,
       "steps": true
      },
      {
       "type": "damage_dealt_when",
       "when": "ambush",
       "multiplier": 3
      }
     ],
     "debuffs": [
      {
       "type": "damage_taken_when",
       "when": "bright",
       "multiplier": 1.1
      }
     ]
    }
   ]
  },
  {
   "id": "tank",
   "name": "Tanks",
   "summary": "Tanks take hits and hold ground. They are slow.",
   "tiers": [
    {
     "buff": "25% more maximum health",
     "debuff": "20% less speed",
     "buffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:max_health",
       "amount": 0.25,
       "operation": "add_multiplied_total"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:movement_speed",
       "amount": -0.2,
       "operation": "add_multiplied_total"
      }
     ]
    },
    {
     "buff": "50% more maximum health, and full knockback resistance",
     "debuff": "15% less speed",
     "buffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:max_health",
       "amount": 0.5,
       "operation": "add_multiplied_total"
      },
      {
       "type": "attribute",
       "attribute": "minecraft:knockback_resistance",
       "amount": 1,
       "operation": "add_value"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:movement_speed",
       "amount": -0.15,
       "operation": "add_multiplied_total"
      }
     ]
    },
    {
     "buff": "100% more maximum health, full knockback resistance, and 2 more armor",
     "debuff": "10% less speed",
     "buffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:max_health",
       "amount": 1,
       "operation": "add_multiplied_total"
      },
      {
       "type": "attribute",
       "attribute": "minecraft:knockback_resistance",
       "amount": 1,
       "operation": "add_value"
      },
      {
       "type": "attribute",
       "attribute": "minecraft:armor",
       "amount": 2,
       "operation": "add_value"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:movement_speed",
       "amount": -0.1,
       "operation": "add_multiplied_total"
      }
     ]
    }
   ]
  },
  {
   "id": "horde",
   "name": "Hordes",
   "summary": "Hordes fight in groups. A zombie of a horde is weak alone.",
   "tiers": [
    {
     "buff": "When the zombie is hit, all zombies in 8 blocks attack the attacker",
     "debuff": "25% less damage with no zombie in 8 blocks",
     "buffs": [
      {
       "type": "call_on_hurt",
       "radius": 8
      }
     ],
     "debuffs": [
      {
       "type": "damage_dealt_when",
       "when": "alone",
       "multiplier": 0.75
      }
     ]
    },
    {
     "buff": "When the zombie is hit, all zombies in 16 blocks attack the attacker",
     "debuff": "20% less damage with no zombie in 8 blocks",
     "buffs": [
      {
       "type": "call_on_hurt",
       "radius": 16
      }
     ],
     "debuffs": [
      {
       "type": "damage_dealt_when",
       "when": "alone",
       "multiplier": 0.8
      }
     ]
    },
    {
     "buff": "When the zombie is hit, all zombies in 32 blocks attack the attacker",
     "debuff": "10% less damage with no zombie in 8 blocks",
     "buffs": [
      {
       "type": "call_on_hurt",
       "radius": 32
      }
     ],
     "debuffs": [
      {
       "type": "damage_dealt_when",
       "when": "alone",
       "multiplier": 0.9
      }
     ]
    }
   ]
  },
  {
   "id": "blight",
   "name": "Blights",
   "summary": "Blights carry disease. Their hits are weak, but their effects last.",
   "tiers": [
    {
     "buff": "Each hit gives Hunger for 5 seconds",
     "debuff": "30% less attack damage",
     "buffs": [
      {
       "type": "effect_on_hit",
       "effect": "minecraft:hunger",
       "seconds": 5
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:attack_damage",
       "amount": -0.3,
       "operation": "add_multiplied_total"
      }
     ]
    },
    {
     "buff": "Each hit gives Hunger for 10 seconds, and a killed villager always becomes a zombie villager",
     "debuff": "25% less attack damage",
     "buffs": [
      {
       "type": "effect_on_hit",
       "effect": "minecraft:hunger",
       "seconds": 10
      },
      {
       "type": "convert_villagers"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:attack_damage",
       "amount": -0.25,
       "operation": "add_multiplied_total"
      }
     ]
    },
    {
     "buff": "Each hit gives Hunger for 15 seconds and Weakness for 5 seconds, and a killed villager always becomes a zombie villager",
     "debuff": "15% less attack damage",
     "buffs": [
      {
       "type": "effect_on_hit",
       "effect": "minecraft:hunger",
       "seconds": 15
      },
      {
       "type": "effect_on_hit",
       "effect": "minecraft:weakness",
       "seconds": 5
      },
      {
       "type": "convert_villagers"
      }
     ],
     "debuffs": [
      {
       "type": "attribute",
       "attribute": "minecraft:attack_damage",
       "amount": -0.15,
       "operation": "add_multiplied_total"
      }
     ]
    }
   ]
  }
 ],
 "minors": [
  {
   "major": "predator",
   "sub": "stalker",
   "name": "Ambushers",
   "style": "Fast hunters that hit first from the dark"
  },
  {
   "major": "predator",
   "sub": "tank",
   "name": "Brutes",
   "style": "Fast and hard to stop"
  },
  {
   "major": "predator",
   "sub": "horde",
   "name": "Packhunters",
   "style": "Fast groups that surround the player"
  },
  {
   "major": "predator",
   "sub": "blight",
   "name": "Fevermaws",
   "style": "Fast biters that spread disease"
  },
  {
   "major": "stalker",
   "sub": "predator",
   "name": "Shadowfangs",
   "style": "Hidden hunters that strike, then chase"
  },
  {
   "major": "stalker",
   "sub": "tank",
   "name": "Barrowkin",
   "style": "Quiet and tough, they wait under the ground"
  },
  {
   "major": "stalker",
   "sub": "horde",
   "name": "Whisperers",
   "style": "Silent groups that close in at night"
  },
  {
   "major": "stalker",
   "sub": "blight",
   "name": "Gloomrot",
   "style": "Hidden carriers of disease"
  },
  {
   "major": "tank",
   "sub": "predator",
   "name": "Juggernauts",
   "style": "Slow walls that hit hard"
  },
  {
   "major": "tank",
   "sub": "stalker",
   "name": "Stonewatchers",
   "style": "Tough guards that wait in the dark"
  },
  {
   "major": "tank",
   "sub": "horde",
   "name": "Phalanx",
   "style": "Tough lines that move as one"
  },
  {
   "major": "tank",
   "sub": "blight",
   "name": "Bloatbacks",
   "style": "Swollen and slow, they poison when they die"
  },
  {
   "major": "horde",
   "sub": "predator",
   "name": "Ravagers",
   "style": "Fast swarms that run down the player"
  },
  {
   "major": "horde",
   "sub": "stalker",
   "name": "Night Swarm",
   "style": "Swarms that come from the dark with no sound"
  },
  {
   "major": "horde",
   "sub": "tank",
   "name": "Siegeborn",
   "style": "Groups that break into bases"
  },
  {
   "major": "horde",
   "sub": "blight",
   "name": "Plaguehost",
   "style": "Groups that spread disease to each other and the player"
  },
  {
   "major": "blight",
   "sub": "predator",
   "name": "Carrion",
   "style": "Diseased hunters that follow the hurt"
  },
  {
   "major": "blight",
   "sub": "stalker",
   "name": "Hushrot",
   "style": "Silent carriers that infect from cover"
  },
  {
   "major": "blight",
   "sub": "tank",
   "name": "Rotbulks",
   "style": "Slow, tough carriers of rot"
  },
  {
   "major": "blight",
   "sub": "horde",
   "name": "Pestilence",
   "style": "A plague that grows with each kill"
  }
 ],
 "hybrids": [
  {
   "a": "predator",
   "b": "stalker",
   "name": "Gloamhunters",
   "style": "Hunters of the dusk, quick and hard to see"
  },
  {
   "a": "predator",
   "b": "tank",
   "name": "Rendhulks",
   "style": "Heavy bodies that still close the distance"
  },
  {
   "a": "predator",
   "b": "horde",
   "name": "Feral Host",
   "style": "Wild groups that chase as one"
  },
  {
   "a": "predator",
   "b": "blight",
   "name": "Fevered",
   "style": "Restless biters that burn with disease"
  },
  {
   "a": "stalker",
   "b": "tank",
   "name": "Cairnwalkers",
   "style": "Quiet, heavy shapes that rise from old graves"
  },
  {
   "a": "stalker",
   "b": "horde",
   "name": "Hollow Choir",
   "style": "Silent crowds that move together in the dark"
  },
  {
   "a": "stalker",
   "b": "blight",
   "name": "Mirelings",
   "style": "Damp, hidden carriers from swamps and caves"
  },
  {
   "a": "tank",
   "b": "horde",
   "name": "Bulwark",
   "style": "Tough groups that hold a line"
  },
  {
   "a": "tank",
   "b": "blight",
   "name": "Moldhides",
   "style": "Thick skins covered in mold and rot"
  },
  {
   "a": "horde",
   "b": "blight",
   "name": "Plaguetide",
   "style": "A rising wave of sick zombies"
  }
 ],
 "resultRules": [
  {
   "points": "One family has 6 or more points (3 genes), and no other family has points",
   "result": "Pure family: tier 3 effects from the major"
  },
  {
   "points": "The major family has 4 or more points (2 genes), and a second family has 2 or more",
   "result": "Minor family: tier 2 effects from the major, tier 1 effects from the sub"
  },
  {
   "points": "Two families have the same points, 4 or more each",
   "result": "Hybrid: tier 2 effects from each of the two"
  },
  {
   "points": "The major family has 4 or more points, and no second family has 2 or more",
   "result": "Major family alone: tier 2 effects from the major"
  },
  {
   "points": "The major family has fewer than 4 points",
   "result": "Neutral: no family effects"
  }
 ],
 "powerOrder": [
  "Pure families",
  "Hybrids",
  "Minor families",
  "Neutral mobs"
 ],
 "pressures": [
  {
   "id": "alone",
   "damageTypes": null,
   "conditions": [
    "alone"
   ],
   "when": "The zombie dies with no other zombie in 8 blocks",
   "status": "development"
  },
  {
   "id": "critical",
   "damageTypes": null,
   "conditions": [
    "critical"
   ],
   "when": "A critical hit kills the zombie",
   "status": "development"
  },
  {
   "id": "crowd",
   "damageTypes": null,
   "conditions": [
    "crowd"
   ],
   "when": "3 or more zombies die in 8 blocks in 5 seconds. The death counts for each of them",
   "status": "development"
  },
  {
   "id": "explosion",
   "damageTypes": "#minecraft:is_explosion",
   "conditions": [],
   "when": "An explosion kills the zombie",
   "status": "released"
  },
  {
   "id": "fall",
   "damageTypes": "#minecraft:is_fall",
   "conditions": [],
   "when": "A fall kills the zombie",
   "status": "released"
  },
  {
   "id": "fire",
   "damageTypes": "#minecraft:is_fire",
   "conditions": [],
   "when": "Fire or lava kills the zombie",
   "status": "released"
  },
  {
   "id": "golem",
   "damageTypes": null,
   "conditions": [
    "golem"
   ],
   "when": "An iron golem kills the zombie",
   "status": "development"
  },
  {
   "id": "melee",
   "damageTypes": "#originofspawns:melee",
   "conditions": [],
   "when": "A melee attack kills the zombie",
   "status": "released"
  },
  {
   "id": "player_death",
   "damageTypes": null,
   "conditions": [
    "player_death"
   ],
   "when": "The zombie kills a player. Each gene of the killer rises by its selection rate, and this pressure counts one time",
   "status": "development"
  },
  {
   "id": "projectile",
   "damageTypes": "#minecraft:is_projectile",
   "conditions": [],
   "when": "A projectile kills the zombie",
   "status": "released"
  },
  {
   "id": "spotted",
   "damageTypes": null,
   "conditions": [
    "spotted"
   ],
   "when": "The zombie was in the view of the killer, at more than 8 blocks, for 2 seconds or more before the death",
   "status": "development"
  },
  {
   "id": "sunlight",
   "damageTypes": null,
   "conditions": [
    "sunlight"
   ],
   "when": "The sun burns the zombie to death",
   "status": "development"
  },
  {
   "id": "unreached",
   "damageTypes": null,
   "conditions": [
    "unreached"
   ],
   "when": "The zombie targeted a player and did not damage a player before it died",
   "status": "released"
  }
 ],
 "genes": [
  {
   "id": "bonerunner",
   "name": "Bonerunner",
   "species": [
    "minecraft:zombie"
   ],
   "family": "predator",
   "sub": null,
   "counters": "unreached",
   "gains": "20% more speed, and 16 more blocks of follow range",
   "downsides": "2 less armor, and 50% more fall damage",
   "look": "Lower legs: bare bone. This changes the model",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "attribute",
      "attribute": "minecraft:movement_speed",
      "amount": 0.2,
      "operation": "add_multiplied_base"
     },
     {
      "type": "attribute",
      "attribute": "minecraft:follow_range",
      "amount": 16,
      "operation": "add_value"
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:armor",
      "amount": -2,
      "operation": "add_value"
     },
     {
      "type": "attribute",
      "attribute": "minecraft:fall_damage_multiplier",
      "amount": 0.5,
      "operation": "add_multiplied_base"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "released",
   "visual": {
    "layers": [
     {
      "regions": [
       "lower_legs",
       "feet"
      ],
      "effect": {
       "type": "erase"
      }
     }
    ],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/bone_leg.png",
     "texture_width": 16,
     "texture_height": 16,
     "parts": [
      {
       "bone": "right_leg",
       "uv": [
        0,
        0
       ],
       "from": [
        -1.5,
        6,
        -1.5
       ],
       "size": [
        3,
        6,
        3
       ]
      },
      {
       "bone": "left_leg",
       "uv": [
        0,
        0
       ],
       "from": [
        -1.5,
        6,
        -1.5
       ],
       "size": [
        3,
        6,
        3
       ]
      }
     ]
    }
   }
  },
  {
   "id": "lunger",
   "name": "Lunger",
   "species": [
    "minecraft:zombie"
   ],
   "family": "predator",
   "sub": null,
   "counters": "unreached",
   "gains": "Leaps at a player in 4 blocks",
   "downsides": "After a missed leap, it cannot move for 1 second",
   "look": "Legs: a torn knee with scrapes, around the leg",
   "details": "It leaps when the player is 3 to 5 blocks away, with 5 seconds between leaps. A leap that touches the player does a normal hit. After a missed leap, it cannot move for 1 second",
   "effects": {
    "gains": [
     {
      "type": "leap",
      "min_distance": 3,
      "max_distance": 5,
      "cooldown_seconds": 5,
      "miss_stun_seconds": 1
     }
    ],
    "downsides": []
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/lunger_0.png",
       "tint": "#16121E"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/lunger_1.png",
       "tint": "#9A2A22"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/lunger_2.png",
       "tint": "#B8B2C8"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "frenzy",
   "name": "Frenzy",
   "species": [
    "minecraft:zombie"
   ],
   "family": "predator",
   "sub": null,
   "counters": "melee",
   "gains": "30% more attack speed",
   "downsides": "4 less maximum health",
   "look": "Shirt: ruined, like the shirt of the drowned",
   "details": "A zombie attacks on a timer of 1 second. Frenzy makes the timer 30% shorter",
   "effects": {
    "gains": [
     {
      "type": "attack_interval",
      "multiplier": 0.7
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:max_health",
      "amount": -4,
      "operation": "add_value"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/frenzy_0.png",
       "tint": "#121818"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/frenzy_1.png",
       "tint": "#71955B"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/frenzy_2.png",
       "tint": "#8E1A1A"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "bloodscent",
   "name": "Bloodscent",
   "species": [
    "minecraft:zombie"
   ],
   "family": "predator",
   "sub": "stalker",
   "counters": "unreached",
   "gains": "Double follow range to a player under half health, and it sees invisible players in 8 blocks",
   "downsides": "25% more damage taken while it chases a hurt player",
   "look": "Shirt: blood stains",
   "details": "It can target an invisible player in 8 blocks. The double follow range is only for players under half health",
   "effects": {
    "gains": [
     {
      "type": "hunt",
      "invisible_radius": 8,
      "wounded_range_multiplier": 2
     }
    ],
    "downsides": [
     {
      "type": "damage_taken_when",
      "when": "hunting_wounded",
      "multiplier": 1.25
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/bloodscent_0.png",
       "tint": "#3E4A26"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/bloodscent_1.png",
       "tint": "#7A1414"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/bloodscent_2.png",
       "tint": "#5A0E0E"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "rabid",
   "name": "Rabid",
   "species": [
    "minecraft:zombie"
   ],
   "family": "predator",
   "sub": null,
   "counters": "projectile",
   "gains": "Each hit gives Nausea for 3 seconds",
   "downsides": "It sometimes attacks other mobs",
   "look": "Face: foam on the mouth",
   "details": "Each 10 seconds, it has a 10% chance to target the nearest mob in 8 blocks that is not a zombie",
   "effects": {
    "gains": [
     {
      "type": "effect_on_hit",
      "effect": "minecraft:nausea",
      "seconds": 3
     }
    ],
    "downsides": [
     {
      "type": "frenzied_targeting",
      "chance": 0.1,
      "interval_seconds": 10,
      "radius": 8
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/rabid_0.png",
       "tint": "#B42828"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/rabid_1.png",
       "tint": "#C2554A"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/rabid_2.png",
       "tint": "#ECEFE2"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/rabid_3.png",
       "tint": "#ECEFE2"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "death_throes",
   "name": "Death Throes",
   "species": [
    "minecraft:zombie"
   ],
   "family": "predator",
   "sub": null,
   "counters": "melee",
   "gains": "When it dies, it does 4 damage to players in 2 blocks",
   "downsides": "4 less maximum health",
   "look": "A fuse that comes out of the neck, with a glowing tip. This changes the model",
   "details": "The 4 damage hits players only, not mobs or blocks. A small burst of particles shows it",
   "effects": {
    "gains": [
     {
      "type": "death_burst",
      "damage": 4,
      "radius": 2
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:max_health",
      "amount": -4,
      "operation": "add_value"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/death_throes_fuse.png",
     "texture_width": 16,
     "texture_height": 16,
     "parts": [
      {
       "bone": "body",
       "uv": [
        0,
        0
       ],
       "from": [
        -3,
        -5,
        -1.5
       ],
       "size": [
        1,
        7,
        1
       ],
       "pivot": [
        -2.5,
        2,
        -1
       ],
       "rotation": [
        -15,
        0,
        -45
       ]
      },
      {
       "bone": "body",
       "uv": [
        0,
        8
       ],
       "from": [
        -3,
        -6,
        -1.5
       ],
       "size": [
        1,
        1,
        1
       ],
       "pivot": [
        -2.5,
        2,
        -1
       ],
       "rotation": [
        -15,
        0,
        -45
       ]
      },
      {
       "bone": "body",
       "uv": [
        0,
        12
       ],
       "from": [
        -3,
        -7,
        -1.5
       ],
       "size": [
        1,
        1,
        1
       ],
       "pivot": [
        -2.5,
        2,
        -1
       ],
       "rotation": [
        -15,
        0,
        -45
       ],
       "glow": true
      }
     ]
    }
   }
  },
  {
   "id": "cinder",
   "name": "Cinder",
   "species": [
    "minecraft:zombie"
   ],
   "family": "stalker",
   "sub": "predator",
   "counters": "fire",
   "gains": "Fire immunity, and each hit has a 20% chance to set the player on fire",
   "downsides": "Rain and water damage it, and 4 less maximum health",
   "look": "Skin: more gray. Hands and feet: dark and red spots",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "damage_taken",
      "damage_types": "#minecraft:is_fire",
      "multiplier": 0
     },
     {
      "type": "ignite_target",
      "chance": 0.2,
      "seconds": 4
     }
    ],
    "downsides": [
     {
      "type": "water_damage",
      "amount": 1,
      "interval": 20
     },
     {
      "type": "attribute",
      "attribute": "minecraft:max_health",
      "amount": -4,
      "operation": "add_value"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "released",
   "visual": {
    "layers": [
     {
      "materials": [
       "skin"
      ],
      "effect": {
       "type": "desaturate",
       "amount": 0.4,
       "darken": 0.15
      }
     },
     {
      "regions": [
       "hands",
       "feet"
      ],
      "effect": {
       "type": "spots",
       "colors": [
        "#1A1A1A",
        "#2B2B2B",
        "#8B2E1F",
        "#6E1F14"
       ],
       "density": 0.35
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "hush",
   "name": "Hush",
   "species": [
    "minecraft:zombie"
   ],
   "family": "stalker",
   "sub": null,
   "counters": "spotted",
   "gains": "No idle sounds and no hurt sounds",
   "downsides": "2 less attack damage",
   "look": "Face: mouth sewn shut",
   "details": "It keeps its death sound",
   "effects": {
    "gains": [
     {
      "type": "silence",
      "idle": true,
      "hurt": true
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:attack_damage",
      "amount": -2,
      "operation": "add_value"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/hush_0.png",
       "tint": "#A88B5C"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/hush_1.png",
       "tint": "#1A100A"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "shade",
   "name": "Shade",
   "species": [
    "minecraft:zombie"
   ],
   "family": "stalker",
   "sub": null,
   "counters": "spotted",
   "gains": "At night, it is almost invisible: translucent, at about 10% opacity",
   "downsides": "The sun burns it two times faster",
   "look": "Arms and legs: dark, mottled patches",
   "details": "The client draws it translucent at night. It is not the invisibility effect, so other mobs still see it",
   "effects": {
    "gains": [
     {
      "type": "translucent",
      "max_light": 7,
      "opacity": 0.1
     }
    ],
    "downsides": [
     {
      "type": "sunburn",
      "multiplier": 2
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/shade_0.png",
       "tint": "#0E1014"
      },
      "order": -2
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "burrower",
   "name": "Burrower",
   "species": [
    "minecraft:zombie"
   ],
   "family": "stalker",
   "sub": null,
   "counters": "unreached",
   "gains": "At night, it can come up from the ground near a player",
   "downsides": "It cannot move for 2 seconds after it comes up",
   "look": "Feet and pants: dirt",
   "details": "When it spawns at night in 24 blocks of a player, it starts 2 blocks under the surface, and rises in 2 seconds with dirt particles. It rises only through dirt, grass, sand, and gravel. It breaks no blocks",
   "effects": {
    "gains": [
     {
      "type": "burrow",
      "player_radius": 24,
      "rise_seconds": 2
     }
    ],
    "downsides": []
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/burrower_dirt.png",
     "texture_width": 16,
     "texture_height": 16,
     "parts": [
      {
       "bone": "right_leg",
       "uv": [
        0,
        0
       ],
       "from": [
        -2,
        6,
        -2
       ],
       "size": [
        4,
        6,
        4
       ],
       "inflate": 0.3
      },
      {
       "bone": "left_leg",
       "uv": [
        0,
        0
       ],
       "from": [
        -2,
        6,
        -2
       ],
       "size": [
        4,
        6,
        4
       ],
       "inflate": 0.3,
       "mirror": true
      }
     ]
    }
   }
  },
  {
   "id": "lurker",
   "name": "Lurker",
   "species": [
    "minecraft:zombie"
   ],
   "family": "stalker",
   "sub": null,
   "counters": "spotted",
   "gains": "It stays out of the view of players. It can come close from behind. In 6 blocks, it attacks as a normal zombie",
   "downsides": "25% less follow range",
   "look": "Scalp: a dark shadow, like a hood",
   "details": "It moves only where the player does not face. The camera mode does not change this",
   "effects": {
    "gains": [
     {
      "type": "lurk",
      "radius": 6
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:follow_range",
      "amount": -0.25,
      "operation": "add_multiplied_base"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/lurker_0.png",
       "tint": "#0E100B"
      },
      "order": -1
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/lurker_1.png",
       "tint": "#0E100B"
      },
      "order": -1
     }
    ],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/lurker_hood.png",
     "texture_width": 64,
     "texture_height": 32,
     "parts": [
      {
       "bone": "head",
       "uv": [
        0,
        0
       ],
       "from": [
        -4,
        -8,
        -4
       ],
       "size": [
        8,
        10,
        8
       ],
       "inflate": 0.4
      }
     ]
    }
   }
  },
  {
   "id": "nightborn",
   "name": "Nightborn",
   "species": [
    "minecraft:zombie"
   ],
   "family": "stalker",
   "sub": null,
   "counters": "sunlight",
   "gains": "20% more speed and damage at light level 6 or less",
   "downsides": "20% less damage at light level 12 or more",
   "look": "Face: pale, bright eyes",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "speed_when",
      "when": "dark",
      "multiplier": 1.2
     },
     {
      "type": "damage_dealt_when",
      "when": "dark",
      "multiplier": 1.2
     }
    ],
    "downsides": [
     {
      "type": "damage_dealt_when",
      "when": "bright",
      "multiplier": 0.8
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "regions": [
       "face"
      ],
      "materials": [
       "skin"
      ],
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/nightborn_0.png",
       "tint": "#D2DCC0"
      },
      "order": -1
     }
    ],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/nightborn_eyes.png",
     "texture_width": 16,
     "texture_height": 16,
     "parts": [
      {
       "bone": "head",
       "uv": [
        0,
        0
       ],
       "from": [
        -3,
        -4,
        -4.05
       ],
       "size": [
        1,
        1,
        0
       ],
       "glow": true
      },
      {
       "bone": "head",
       "uv": [
        0,
        0
       ],
       "from": [
        2,
        -4,
        -4.05
       ],
       "size": [
        1,
        1,
        0
       ],
       "glow": true
      }
     ]
    }
   }
  },
  {
   "id": "ironhide",
   "name": "Ironhide",
   "species": [
    "minecraft:zombie"
   ],
   "family": "tank",
   "sub": null,
   "counters": "projectile",
   "gains": "50% less projectile damage, and 50% knockback resistance",
   "downsides": "10% less speed, and 25% more explosion damage",
   "look": "Shirt: plates that look like iron",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "damage_taken",
      "damage_types": "#minecraft:is_projectile",
      "multiplier": 0.5
     },
     {
      "type": "attribute",
      "attribute": "minecraft:knockback_resistance",
      "amount": 0.5,
      "operation": "add_value"
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:movement_speed",
      "amount": -0.1,
      "operation": "add_multiplied_base"
     },
     {
      "type": "damage_taken",
      "damage_types": "#minecraft:is_explosion",
      "multiplier": 1.25
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "released",
   "visual": {
    "layers": [
     {
      "regions": [
       "torso"
      ],
      "materials": [
       "cloth"
      ],
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/pattern/iron_plates.png",
       "tint": "#A1998F"
      },
      "order": -5
     },
     {
      "regions": [
       "arms"
      ],
      "materials": [
       "cloth"
      ],
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/pattern/iron_plates_sleeve.png",
       "tint": "#A1998F"
      },
      "order": -5
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "thickskull",
   "name": "Thickskull",
   "species": [
    "minecraft:zombie"
   ],
   "family": "tank",
   "sub": null,
   "counters": "critical",
   "gains": "Critical hits do no extra damage to it",
   "downsides": "15% less attack speed",
   "look": "Scalp: a bone plate",
   "details": "The attack timer is 15% longer",
   "effects": {
    "gains": [
     {
      "type": "critical_immunity"
     }
    ],
    "downsides": [
     {
      "type": "attack_interval",
      "multiplier": 1.15
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/thickskull_0.png",
       "tint": "#CEC4A8"
      },
      "order": -1
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/thickskull_1.png",
       "tint": "#7A2E24"
      },
      "order": -1
     }
    ],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/thickskull_plate.png",
     "texture_width": 32,
     "texture_height": 16,
     "parts": [
      {
       "bone": "head",
       "uv": [
        0,
        0
       ],
       "from": [
        -3,
        -10,
        -3
       ],
       "size": [
        6,
        2,
        6
       ]
      },
      {
       "bone": "head",
       "uv": [
        0,
        8
       ],
       "from": [
        -3,
        -8,
        -4.6
       ],
       "size": [
        6,
        2,
        1
       ]
      }
     ]
    }
   }
  },
  {
   "id": "bloat",
   "name": "Bloat",
   "species": [
    "minecraft:zombie"
   ],
   "family": "tank",
   "sub": "blight",
   "counters": "melee",
   "gains": "10 more maximum health, and a small cloud of Poison when it dies",
   "downsides": "25% less speed",
   "look": "Shirt: swollen green blotches",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "attribute",
      "attribute": "minecraft:max_health",
      "amount": 10,
      "operation": "add_value"
     },
     {
      "type": "death_cloud",
      "effect": "minecraft:poison",
      "seconds": 4,
      "radius": 1.5
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:movement_speed",
      "amount": -0.25,
      "operation": "add_multiplied_base"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/bloat_0.png",
       "tint": "#9DB64C"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/bloat_1.png",
       "tint": "#6A852A"
      }
     }
    ],
    "regionMap": null,
    "model": {
     "mob_texture": true,
     "parts": [
      {
       "bone": "body",
       "uv": [
        16,
        16
       ],
       "from": [
        -4,
        0,
        -2
       ],
       "size": [
        8,
        12,
        4
       ],
       "inflate": 0.8
      }
     ]
    }
   }
  },
  {
   "id": "stoneblood",
   "name": "Stoneblood",
   "species": [
    "minecraft:zombie"
   ],
   "family": "tank",
   "sub": null,
   "counters": "fall",
   "gains": "No fall damage, and full knockback resistance",
   "downsides": "50% slower in water",
   "look": "Feet and lower legs: gray, like stone",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "damage_taken",
      "damage_types": "#minecraft:is_fall",
      "multiplier": 0
     },
     {
      "type": "attribute",
      "attribute": "minecraft:knockback_resistance",
      "amount": 1,
      "operation": "add_value"
     }
    ],
    "downsides": [
     {
      "type": "speed_when",
      "when": "in_water",
      "multiplier": 0.5
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "regions": [
       "lower_legs",
       "feet"
      ],
      "effect": {
       "type": "desaturate",
       "amount": 1,
       "darken": 0.1
      },
      "order": -3
     },
     {
      "regions": [
       "lower_legs",
       "feet"
      ],
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/stoneblood_0.png",
       "tint": "#9A9A9A"
      },
      "order": -3
     }
    ],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/stoneblood_shell.png",
     "texture_width": 16,
     "texture_height": 16,
     "parts": [
      {
       "bone": "right_leg",
       "uv": [
        0,
        0
       ],
       "from": [
        -2,
        6,
        -2
       ],
       "size": [
        4,
        6,
        4
       ],
       "inflate": 0.15
      },
      {
       "bone": "left_leg",
       "uv": [
        0,
        0
       ],
       "from": [
        -2,
        6,
        -2
       ],
       "size": [
        4,
        6,
        4
       ],
       "inflate": 0.15,
       "mirror": true
      }
     ]
    }
   }
  },
  {
   "id": "blastproof",
   "name": "Blastproof",
   "species": [
    "minecraft:zombie"
   ],
   "family": "tank",
   "sub": null,
   "counters": "explosion",
   "gains": "60% less explosion damage",
   "downsides": "50% more fire damage",
   "look": "Shirt: soot on the edges",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "damage_taken",
      "damage_types": "#minecraft:is_explosion",
      "multiplier": 0.4
     }
    ],
    "downsides": [
     {
      "type": "damage_taken",
      "damage_types": "#minecraft:is_fire",
      "multiplier": 1.5
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "regions": [
       "torso",
       "arms",
       "legs",
       "lower_legs"
      ],
      "materials": [
       "cloth"
      ],
      "effect": {
       "type": "desaturate",
       "amount": 0.75,
       "darken": 0.2
      },
      "order": -7
     },
     {
      "regions": [
       "torso",
       "arms",
       "legs",
       "lower_legs"
      ],
      "materials": [
       "cloth"
      ],
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/blastproof_0.png",
       "tint": "#2A1E16"
      },
      "order": -6
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/blastproof_1.png",
       "tint": "#120E0C"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/blastproof_2.png",
       "tint": "#4A3020"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/blastproof_3.png",
       "tint": "#A04A1E"
      }
     },
     {
      "regions": [
       "hands"
      ],
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/blastproof_4.png",
       "tint": "#1E1A17"
      },
      "order": -2
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "mender",
   "name": "Mender",
   "species": [
    "minecraft:zombie"
   ],
   "family": "tank",
   "sub": null,
   "counters": "melee",
   "gains": "It heals 1 health each 3 seconds when nothing hits it for 5 seconds",
   "downsides": "25% less attack damage",
   "look": "Arms: stitched seams",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "regenerate",
      "amount": 1,
      "interval_seconds": 3,
      "idle_seconds": 5
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:attack_damage",
      "amount": -0.25,
      "operation": "add_multiplied_total"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/mender_0.png",
       "tint": "#3A1E16"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/mender_1.png",
       "tint": "#E0D2A8"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/mender_2.png",
       "tint": "#C98A84"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/mender_3.png",
       "tint": "#DCD5C2"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/mender_4.png",
       "tint": "#8A2A22"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "packcaller",
   "name": "Packcaller",
   "species": [
    "minecraft:zombie"
   ],
   "family": "horde",
   "sub": null,
   "counters": "alone",
   "gains": "When it sees a player, it calls the zombies in 24 blocks",
   "downsides": "Players hear its groans from two times farther",
   "look": "Face: open jaw",
   "details": "When it first sees a player, all zombies in 24 blocks target that player. It can call again after 30 seconds",
   "effects": {
    "gains": [
     {
      "type": "call_on_sight",
      "radius": 24,
      "cooldown_seconds": 30
     }
    ],
    "downsides": [
     {
      "type": "loud",
      "multiplier": 2
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/packcaller_0.png",
       "tint": "#1A0F0C"
      }
     }
    ],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/packcaller_jaw.png",
     "texture_width": 16,
     "texture_height": 16,
     "parts": [
      {
       "bone": "head",
       "uv": [
        0,
        0
       ],
       "from": [
        -2,
        0,
        -4.4
       ],
       "size": [
        4,
        2,
        2
       ]
      }
     ]
    }
   }
  },
  {
   "id": "brood",
   "name": "Brood",
   "species": [
    "minecraft:zombie"
   ],
   "family": "horde",
   "sub": null,
   "counters": "crowd",
   "gains": "Its spawn groups have 1 more zombie",
   "downsides": "Each zombie of the group has 15% less maximum health",
   "look": "Shirt: ribs show through",
   "details": "When it spawns in a natural way, 1 more zombie with the same genes spawns near it",
   "effects": {
    "gains": [
     {
      "type": "brood",
      "extra": 1
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:max_health",
      "amount": -0.15,
      "operation": "add_multiplied_total"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/brood_0.png",
       "tint": "#D8D0B8"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/brood_1.png",
       "tint": "#2A2420"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "rally",
   "name": "Rally",
   "species": [
    "minecraft:zombie"
   ],
   "family": "horde",
   "sub": null,
   "counters": "alone",
   "gains": "10% more damage for each zombie in 6 blocks, to a maximum of 30%",
   "downsides": "20% less damage when alone",
   "look": "Arms: bands of cloth",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "rally",
      "per_ally": 0.1,
      "max": 0.3,
      "radius": 6
     }
    ],
    "downsides": [
     {
      "type": "damage_dealt_when",
      "when": "alone",
      "multiplier": 0.8
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/rally_0.png",
       "tint": "#9B2D23"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "runt",
   "name": "Runt",
   "species": [
    "minecraft:zombie"
   ],
   "family": "horde",
   "sub": null,
   "counters": "projectile",
   "gains": "20% chance to spawn as a baby zombie",
   "downsides": "Its baby zombies have 50% less maximum health",
   "look": "The baby model, and scalp: thin tufts of hair",
   "details": "Runt babies can ride chickens at the vanilla chance",
   "effects": {
    "gains": [
     {
      "type": "runt",
      "chance": 0.2,
      "baby_health_multiplier": 0.5
     }
    ],
    "downsides": []
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/runt_0.png",
       "tint": "#3E3128"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/runt_1.png",
       "tint": "#5A4838"
      }
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "grave_call",
   "name": "Grave Call",
   "species": [
    "minecraft:zombie"
   ],
   "family": "horde",
   "sub": null,
   "counters": "melee",
   "gains": "When hit, it has a 10% chance to call a new zombie",
   "downsides": "2 less armor",
   "look": "Hands: grave dirt",
   "details": "The new zombie has no genes. Each Grave Call zombie calls a maximum of 2 zombies",
   "effects": {
    "gains": [
     {
      "type": "grave_call",
      "chance": 0.1,
      "max_calls": 2,
      "radius": 8
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:armor",
      "amount": -2,
      "operation": "add_value"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/grave_call_0.png",
       "tint": "#3B2A1C"
      },
      "order": -1
     }
    ],
    "regionMap": null,
    "model": null
   }
  },
  {
   "id": "siege",
   "name": "Siege",
   "species": [
    "minecraft:zombie"
   ],
   "family": "horde",
   "sub": "tank",
   "counters": "unreached",
   "gains": "It breaks doors on all difficulties, and it breaks glass slowly",
   "downsides": "20% less speed",
   "look": "Hands: wood splinters",
   "details": "It breaks glass blocks and glass panes only, in 10 seconds for each block. It obeys the gamerule that stops mobs from breaking blocks",
   "effects": {
    "gains": [
     {
      "type": "siege",
      "break_seconds": 10
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:movement_speed",
      "amount": -0.2,
      "operation": "add_multiplied_base"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": {
    "layers": [
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/siege_0.png",
       "tint": "#D8C090"
      }
     },
     {
      "effect": {
       "type": "pattern",
       "texture": "originofspawns:textures/gene_marks/siege_1.png",
       "tint": "#B0453A"
      },
      "order": 1
     }
    ],
    "regionMap": null,
    "model": {
     "texture": "originofspawns:textures/entity/siege_splinters.png",
     "texture_width": 32,
     "texture_height": 16,
     "parts": [
      {
       "bone": "right_arm",
       "uv": [
        0,
        0
       ],
       "from": [
        -2.5,
        -6,
        -0.5
       ],
       "size": [
        1,
        4,
        1
       ],
       "pivot": [
        -2,
        -2,
        0
       ],
       "rotation": [
        10,
        0,
        -35
       ]
      },
      {
       "bone": "right_arm",
       "uv": [
        8,
        0
       ],
       "from": [
        -7,
        5,
        -0.5
       ],
       "size": [
        4,
        1,
        1
       ],
       "pivot": [
        -3,
        5.5,
        0
       ],
       "rotation": [
        0,
        15,
        20
       ]
      },
      {
       "bone": "right_arm",
       "uv": [
        0,
        8
       ],
       "from": [
        -1.5,
        6.5,
        -6
       ],
       "size": [
        1,
        1,
        4
       ],
       "pivot": [
        -1,
        7,
        -2
       ],
       "rotation": [
        25,
        -15,
        0
       ]
      },
      {
       "bone": "right_arm",
       "uv": [
        20,
        0
       ],
       "from": [
        -4,
        2,
        0.5
       ],
       "size": [
        1,
        1,
        0
       ],
       "pivot": [
        -3,
        2.5,
        0.5
       ],
       "rotation": [
        0,
        0,
        25
       ]
      },
      {
       "bone": "right_arm",
       "uv": [
        20,
        0
       ],
       "from": [
        -4,
        3.5,
        -1
       ],
       "size": [
        1,
        1,
        0
       ],
       "pivot": [
        -3,
        4,
        -1
       ],
       "rotation": [
        0,
        30,
        -20
       ]
      },
      {
       "bone": "left_arm",
       "uv": [
        0,
        0
       ],
       "from": [
        1.5,
        -6,
        -0.5
       ],
       "size": [
        1,
        4,
        1
       ],
       "pivot": [
        2,
        -2,
        0
       ],
       "rotation": [
        10,
        0,
        35
       ],
       "mirror": true
      },
      {
       "bone": "left_arm",
       "uv": [
        8,
        0
       ],
       "from": [
        3,
        5,
        -0.5
       ],
       "size": [
        4,
        1,
        1
       ],
       "pivot": [
        3,
        5.5,
        0
       ],
       "rotation": [
        0,
        -15,
        -20
       ],
       "mirror": true
      },
      {
       "bone": "left_arm",
       "uv": [
        0,
        8
       ],
       "from": [
        0.5,
        6.5,
        -6
       ],
       "size": [
        1,
        1,
        4
       ],
       "pivot": [
        1,
        7,
        -2
       ],
       "rotation": [
        25,
        15,
        0
       ],
       "mirror": true
      },
      {
       "bone": "left_arm",
       "uv": [
        20,
        0
       ],
       "from": [
        3,
        2,
        0.5
       ],
       "size": [
        1,
        1,
        0
       ],
       "pivot": [
        3,
        2.5,
        0.5
       ],
       "rotation": [
        0,
        0,
        -25
       ],
       "mirror": true
      },
      {
       "bone": "left_arm",
       "uv": [
        20,
        0
       ],
       "from": [
        3,
        3.5,
        -1
       ],
       "size": [
        1,
        1,
        0
       ],
       "pivot": [
        3,
        4,
        -1
       ],
       "rotation": [
        0,
        -30,
        20
       ],
       "mirror": true
      }
     ]
    }
   }
  },
  {
   "id": "rotgut",
   "name": "Rotgut",
   "species": [
    "minecraft:zombie"
   ],
   "family": "blight",
   "sub": null,
   "counters": "melee",
   "gains": "Each hit gives Hunger II for 10 seconds",
   "downsides": "2 less attack damage",
   "look": "Shirt: green and black rot",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "effect_on_hit",
      "effect": "minecraft:hunger",
      "seconds": 10,
      "amplifier": 1
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:attack_damage",
      "amount": -2,
      "operation": "add_value"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": null
  },
  {
   "id": "contagion",
   "name": "Contagion",
   "species": [
    "minecraft:zombie"
   ],
   "family": "blight",
   "sub": null,
   "counters": "golem",
   "gains": "A villager that it kills always becomes a zombie villager",
   "downsides": "Iron golems attack it first, and do 50% more damage to it",
   "look": "Arms: green, wet sores",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "convert_villagers"
     }
    ],
    "downsides": [
     {
      "type": "attract_golems",
      "radius": 16
     },
     {
      "type": "damage_from",
      "attackers": "minecraft:iron_golem",
      "multiplier": 1.5
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": null
  },
  {
   "id": "sporebearer",
   "name": "Sporebearer",
   "species": [
    "minecraft:zombie"
   ],
   "family": "blight",
   "sub": null,
   "counters": "crowd",
   "gains": "When it dies, it leaves a cloud of Weakness, 3 blocks wide",
   "downsides": "4 less maximum health",
   "look": "Scalp: small fungus",
   "details": "",
   "effects": {
    "gains": [
     {
      "type": "death_cloud",
      "effect": "minecraft:weakness",
      "seconds": 10,
      "radius": 1.5
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:max_health",
      "amount": -4,
      "operation": "add_value"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": null
  },
  {
   "id": "leech",
   "name": "Leech",
   "species": [
    "minecraft:zombie"
   ],
   "family": "blight",
   "sub": null,
   "counters": "melee",
   "gains": "Each hit takes some XP from the player",
   "downsides": "It drops no XP",
   "look": "Face: a round mouth with teeth",
   "details": "Each hit takes 3 XP points. When it dies, it drops the XP that it took, and no other XP",
   "effects": {
    "gains": [
     {
      "type": "leech_xp",
      "amount": 3
     }
    ],
    "downsides": []
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": null
  },
  {
   "id": "miasma",
   "name": "Miasma",
   "species": [
    "minecraft:zombie"
   ],
   "family": "blight",
   "sub": null,
   "counters": "projectile",
   "gains": "Players in 3 blocks get Weakness",
   "downsides": "It glows, so players see it through walls",
   "look": "Hands: thin gloves, like slime. Eyes: lime green",
   "details": "It has the Glowing effect all the time. Its aura gives Weakness I to players in 3 blocks, renewed each 2 seconds",
   "effects": {
    "gains": [
     {
      "type": "aura",
      "effect": "minecraft:weakness",
      "amplifier": 0,
      "radius": 3,
      "interval_seconds": 2,
      "particles": true
     }
    ],
    "downsides": [
     {
      "type": "glowing"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": null
  },
  {
   "id": "plague_carrier",
   "name": "Plague Carrier",
   "species": [
    "minecraft:zombie"
   ],
   "family": "blight",
   "sub": "horde",
   "counters": "player_death",
   "gains": "A player that it kills rises as a zombie with its genes",
   "downsides": "10% less maximum health",
   "look": "Skin: gray-green blotches",
   "details": "A zombie with the genes of the killer spawns where the player died, and wears the head of the player. The items of the player drop on the ground as in vanilla",
   "effects": {
    "gains": [
     {
      "type": "raise_victim"
     }
    ],
    "downsides": [
     {
      "type": "attribute",
      "attribute": "minecraft:max_health",
      "amount": -0.1,
      "operation": "add_multiplied_total"
     }
    ]
   },
   "rates": {
    "selection": 0.02,
    "mutation": 0.003,
    "decay": 0.01
   },
   "status": "development",
   "visual": null
  }
 ],
 "evolution": {
  "pools": [
   "A species is one mob type, for example zombie, skeleton, or creeper.",
   "Vanilla variants are different species. For example, zombie, husk, and drowned do not share pools or families. A husk evolves genes that fit sand and dry biomes.",
   "Each species has a gene pool.",
   "Each species has one pool for each biome category. The categories are hot, cold, wet, temperate, nether, and end.",
   "A small part of each pool flows to the pools of neighbor categories. Thus, genes move slowly across the world.",
   "A configuration option sets the pool scope to \"global\" or \"per biome\". This option comes after version 1.0.0."
  ],
  "selection": [
   "Each pool counts the causes of death of its members. Examples of causes are fire, arrows, melee, explosions, and falls.",
   "A pressure can also be a condition of the death. For example, a zombie that dies before it hits the player counts for the \"unreached\" pressure. This happens when the player runs away or wins the fight quickly.",
   "A death counts for \"unreached\" when the mob targeted a player and did not damage a player before it died.",
   "The \"unreached\" pressure does not select for attack speed. Attack speed comes from the \"melee\" pressure.",
   "The list of pressures is not final. The mod adds pressures for new genes.",
   "A common cause of death makes the gene that counters it stronger.",
   "The mod weights each death by its cause, not by its player. Thus, one player cannot control a pool alone.",
   "Each new mob gets its genes from its pool.",
   "Random mutation makes small changes to the pool.",
   "Each death raises the gene that counters its pressure by 2% of the distance to 100%. For example, about 50 fire deaths take Cinder from 0% to about 60%.",
   "Each Minecraft day, decay lowers each frequency by 1% of its value.",
   "Mutation gives each gene a small chance to go to a new mob, also at 0%.",
   "These values are first drafts.",
   "Each gene has a cost. For example, fire resistance lowers the maximum health.",
   "Each gene changes how the mob looks. The player can see the threat before the fight."
  ],
  "balance": [
   "Pressure decays. If players stop a cause of death, the gene that counters it fades slowly.",
   "Each mob has a maximum of 5 genes. The genes under the most pressure come first. Thus, a species cannot have all defenses at the same time.",
   "Each gene has a gain and a downside.",
   "The difficulty sets the speed of evolution. Peaceful has no evolution. Hard has the fastest evolution.",
   "A gamerule overrides the speed."
  ],
  "visuals": [
   "Each gene adds a small but distinct mark to the mob. The mob stays easy to recognize. A player who knows the genes can read the history of a mob.",
   "The marks of different genes go on different body parts. Thus, all genes can combine on one mob, and no mark hides another.",
   "The mod does not have a texture for each mix of genes. The game makes the textures when it loads, from the texture of the mob.",
   "Each body type has a region map. A region map is an image that labels each pixel of the mob texture. The regions are skin, shirt, pants, hands, feet, and head.",
   "One region map serves all mobs with the same body type. For example, the humanoid map serves zombies, husks, drowned, and piglins.",
   "Each gene has a visual recipe. The recipe applies an effect to a region. Examples of effects are a color change, spots, and a pattern.",
   "A gene can also change the model. This is rare. It is only for a gene that a texture cannot show.",
   "Milestone 1 has one model change: Bonerunner.",
   "Milestone 2a adds model changes for Bloat, Thickskull, and Sporebearer.",
   "The marks must not clutter the mob. First, the modeller makes the marks small and puts them on different pixels. If marks still collide, the region map gets smaller regions, for example mouth, eyes, brow, chest, belly, back, and sleeves.",
   "The user reviews the visuals with one preview sheet for each family: 6 genes on white, front and back.",
   "Data packs define the visual recipes.",
   "The modeller makes the region maps and the patterns with code. The mod does not use Blockbench."
  ],
  "extinction": [
   "If players kill a species too much in a category, the species goes extinct in that category. An extinct species does not spawn there.",
   "Families replace named variants. Refer to \"Families\"."
  ],
  "multiplayer": [
   "All players in a world share one set of gene pools.",
   "Each player has a different notebook and different perks."
  ],
  "categories": [
   {
    "id": "hot",
    "name": "Hot"
   },
   {
    "id": "cold",
    "name": "Cold"
   },
   {
    "id": "wet",
    "name": "Wet"
   },
   {
    "id": "temperate",
    "name": "Temperate"
   },
   {
    "id": "nether",
    "name": "Nether"
   },
   {
    "id": "end",
    "name": "End"
   }
  ],
  "babies": [
   "Baby zombies do not spawn in vanilla ways.",
   "Only the Runt gene makes baby zombies. Thus, a baby zombie shows that its pool evolved Runt.",
   "At the start of a world, no baby zombies spawn."
  ]
 },
 "notebook": {
  "design": [
   "The notebook has a tab for each species. Each species tab has family tabs and gene tabs.",
   "The Beagle Notebook is an item.",
   "The notebook has one page for each species.",
   "A page shows gene frequencies, a chart of change over time, mutations, and a family tree.",
   "A page opens when the player first observes or kills a mob of that species.",
   "Notebook progress sets the knowledge percentage.",
   "A phylogenetic tree shows when each variant split from its species.",
   "Field sketches show each mob with its current genes. The sketch changes when the genes change.",
   "The notebook gives predictions. For example: \"Zombies in hot biomes move toward fire immunity.\""
  ],
  "mobInfo": [
   "When the player looks at a mob, a small panel shows at the top of the screen. Mods such as AppleSkin and Jade show information in a similar way.",
   "The panel shows short information: the family of the mob and its dominant genes."
  ],
  "knowledge": [
   {
    "knowledge": "0%",
    "slots": "1"
   },
   {
    "knowledge": "20%",
    "slots": "2"
   },
   {
    "knowledge": "40%",
    "slots": "3"
   },
   {
    "knowledge": "60%",
    "slots": "4"
   },
   {
    "knowledge": "80%",
    "slots": "5"
   }
  ],
  "m4": {
   "key": "Milestone 4",
   "title": "Full notebook",
   "done": false,
   "after1": false,
   "items": [
    "Add the charts of change over time.",
    "Add the family tree and the phylogenetic tree.",
    "Add field sketches that change with the genes.",
    "Add predictions.",
    "Add the advancements with names from Darwin's book.",
    "Draw the notebook as an open book: two facing pages with a leather edge.",
    "Use bookmark ribbons as the species tabs. Put the tabs for the gene page and the family page on the edge of the page.",
    "Show a small sketch of the mob on its page, with its real gene textures.",
    "Play a page-turn sound when the player changes a tab."
   ],
   "text": " - Add the charts of change over time. - Add the family tree and the phylogenetic tree. - Add field sketches that change with the genes. - Add predictions. - Add the advancements with names from Darwin's book. - Draw the notebook as an open book: two facing pages with a leather edge. - Use bookmark ribbons as the species tabs.   Put the tabs for the gene page and the family page on the edge of the page. - Show a small sketch of the mob on its page, with its real gene textures. - Play a page-turn sound when the player changes a tab. "
  },
  "lang": {
   "screen.originofspawns.notebook.no_genes": "No genes are known for this species.",
   "mob_info.originofspawns.kind.major": "Major family",
   "mob_info.originofspawns.kind.pure": "Pure family",
   "mob_info.originofspawns.kind.minor": "Minor family",
   "mob_info.originofspawns.kind.hybrid": "Hybrid",
   "mob_info.originofspawns.no_genes": "No genes",
   "screen.originofspawns.notebook.no_species": "No species are known.",
   "screen.originofspawns.notebook.found": "Found %s of %s",
   "screen.originofspawns.notebook.section.genes": "Genes",
   "screen.originofspawns.notebook.section.families": "Families",
   "screen.originofspawns.notebook.kind.pure": "Pure families",
   "screen.originofspawns.notebook.kind.minor": "Minor families",
   "screen.originofspawns.notebook.kind.hybrid": "Hybrids",
   "screen.originofspawns.notebook.kind.neutral": "Neutral"
  },
  "itemName": "Beagle Notebook"
 },
 "perks": [
  {
   "name": "Thick hide",
   "gain": "10% less damage taken",
   "cost": "10% less speed"
  },
  {
   "name": "Night eyes",
   "gain": "The player sees in the dark",
   "cost": "The sun blinds the player"
  },
  {
   "name": "Cold blood",
   "gain": "Hunger decreases more slowly",
   "cost": "The player is weak in snow biomes"
  },
  {
   "name": "Gills",
   "gain": "The player breathes longer under water",
   "cost": "The player loses air slowly on land"
  },
  {
   "name": "Lean frame",
   "gain": "More sprint speed",
   "cost": "2 fewer hearts"
  },
  {
   "name": "Scent mask",
   "gain": "Mobs notice the player later",
   "cost": "Animals run from the player"
  },
  {
   "name": "Hollow bones",
   "gain": "Less fall damage",
   "cost": "More knockback taken"
  },
  {
   "name": "Hard shell",
   "gain": "2 more armor points",
   "cost": "The player sinks in water"
  },
  {
   "name": "Heat sink",
   "gain": "Less fire and lava damage",
   "cost": "Powder snow freezes the player faster"
  },
  {
   "name": "Ruminant",
   "gain": "The player can eat grass and leaves",
   "cost": "Meat gives less food"
  },
  {
   "name": "Carnivore",
   "gain": "Raw meat is safe and gives full food",
   "cost": "Plant food gives half food"
  },
  {
   "name": "Fat stores",
   "gain": "Saturation lasts longer",
   "cost": "Slower swim speed"
  },
  {
   "name": "Adrenaline",
   "gain": "Speed and strength at low health",
   "cost": "Slowness and hunger after the burst"
  },
  {
   "name": "Axolotl blood",
   "gain": "Faster natural healing",
   "cost": "Healing uses more hunger"
  },
  {
   "name": "Venom glands",
   "gain": "Melee attacks poison the target",
   "cost": "Healing potions heal less"
  },
  {
   "name": "Echolocation",
   "gain": "Sneaking shows mobs through walls",
   "cost": "Mobs hear the player from farther"
  },
  {
   "name": "Keen nose",
   "gain": "The player sees the trails of near mobs",
   "cost": "Rotten flesh and zombies cause nausea"
  },
  {
   "name": "Long arms",
   "gain": "1 more block of reach",
   "cost": "Slower attack speed"
  },
  {
   "name": "Claws",
   "gain": "An empty hand digs dirt and sand fast",
   "cost": "Tools lose durability faster"
  },
  {
   "name": "Gecko pads",
   "gain": "The player climbs walls while sneaking",
   "cost": "More fall damage"
  },
  {
   "name": "Spring legs",
   "gain": "Higher jumps",
   "cost": "Jumps use more hunger"
  },
  {
   "name": "Camouflage skin",
   "gain": "Invisible when still in grass or leaves",
   "cost": "Works only with no chestplate"
  },
  {
   "name": "Nocturnal",
   "gain": "More damage at night",
   "cost": "Less damage in the day"
  },
  {
   "name": "Hibernation",
   "gain": "Sleep restores all hunger and health",
   "cost": "Slowness for one minute after sleep"
  },
  {
   "name": "Pack instinct",
   "gain": "More damage near tame wolves or players",
   "cost": "Less damage when alone"
  },
  {
   "name": "Symbiosis",
   "gain": "Near tame animals heal the player slowly",
   "cost": "The player takes part of their damage"
  },
  {
   "name": "Immune memory",
   "gain": "Each bad effect is shorter after the player survives it",
   "cost": "Good effects are also shorter"
  },
  {
   "name": "Tough stomach",
   "gain": "No bad effects from rotten flesh, spider eyes, or pufferfish",
   "cost": "Golden apples give less"
  },
  {
   "name": "Bioluminescence",
   "gain": "The player gives light",
   "cost": "Mobs see the player from farther at night"
  },
  {
   "name": "Mimic scent",
   "gain": "Zombies ignore the player until the player attacks",
   "cost": "Villagers give worse trades, and iron golems do not trust the player"
  }
 ],
 "dataPacks": {
  "html": "<p>Data packs define the pressures, the genes, the families, and the perks of the mod. Each entry is a JSON file at this path:</p>\n<pre class=\"code\"><code>data/&lt;namespace&gt;/originofspawns/&lt;registry&gt;/&lt;name&gt;.json</code></pre>\n<p>The registries are <code>pressure</code>, <code>gene</code>, <code>family</code>, and <code>perk</code>. The world reads these files when it loads. A file with an error stops the world from loading, and the log names the file.</p>\n<h3 id=\"dp-pressure\">Pressure</h3>\n<p>A pressure is a cause of death. A death counts for a pressure when it matches all the parts of the pressure.</p>\n<pre class=\"code\"><code>{ \"damage_types\": \"#minecraft:is_fire\" }\n{ \"conditions\": [\"unreached\"] }</code></pre>\n<div class=\"table-wrap\"><table><thead><tr><th>Field</th><th>Type</th><th>Default</th><th>Description</th></tr></thead><tbody><tr><td><code>damage_types</code></td><td>damage type tag, with <code>#</code></td><td>none</td><td>The damage types that count for this pressure</td></tr><tr><td><code>conditions</code></td><td>list of conditions</td><td><code>[]</code></td><td>The conditions of the death that must all be true</td></tr></tbody></table></div>\n<p>A pressure needs <code>damage_types</code>, <code>conditions</code>, or both.</p>\n<div class=\"table-wrap\"><table><thead><tr><th>Condition</th><th>True when</th></tr></thead><tbody><tr><td><code>unreached</code></td><td>The mob targeted a player, but did not damage a player before it died</td></tr><tr><td><code>sunlight</code></td><td>The sun set the mob on fire, and the fire killed it</td></tr><tr><td><code>spotted</code></td><td>The killer saw the mob from more than 8 blocks, for 2 seconds or more before the death</td></tr><tr><td><code>alone</code></td><td>No other mob of the same species was in 8 blocks</td></tr><tr><td><code>crowd</code></td><td>3 or more mobs of the same species died in 8 blocks in 5 seconds. The condition is true for each of them</td></tr><tr><td><code>critical</code></td><td>A critical hit of a player killed the mob</td></tr><tr><td><code>golem</code></td><td>An iron golem killed the mob</td></tr><tr><td><code>player_death</code></td><td>The mob killed a player. This is not a death of the mob. Each gene of the killer gets stronger</td></tr></tbody></table></div>\n<p>The mod has these pressures: <code>fire</code>, <code>projectile</code>, <code>melee</code>, <code>explosion</code>, <code>fall</code>, <code>unreached</code>, <code>sunlight</code>, <code>spotted</code>, <code>alone</code>, <code>crowd</code>, <code>critical</code>, <code>golem</code>, and <code>player_death</code>. The <code>melee</code> pressure uses the tag <code>#originofspawns:melee</code>.</p>\n<h3 id=\"dp-gene\">Gene</h3>\n<p>A gene is a trait that a species can carry. It has gains and downsides, like a player perk.</p>\n<pre class=\"code\"><code>{\n    \"species\": \"minecraft:zombie\",\n    \"counters\": \"originofspawns:fire\",\n    \"family\": \"originofspawns:zombie/stalker\",\n    \"sub_family\": \"originofspawns:zombie/predator\",\n    \"gains\": [\n        { \"type\": \"damage_taken\", \"damage_types\": \"#minecraft:is_fire\", \"multiplier\": 0 }\n    ],\n    \"downsides\": [\n        { \"type\": \"attribute\", \"attribute\": \"minecraft:max_health\", \"amount\": -4, \"operation\": \"add_value\" }\n    ]\n}</code></pre>\n<div class=\"table-wrap\"><table><thead><tr><th>Field</th><th>Type</th><th>Default</th><th>Description</th></tr></thead><tbody><tr><td><code>species</code></td><td>entity type, list, or tag</td><td></td><td>The mobs that can carry the gene</td></tr><tr><td><code>counters</code></td><td>pressure id</td><td></td><td>The pressure that makes the gene stronger</td></tr><tr><td><code>family</code></td><td>family id</td><td></td><td>The main family tag. It gives 2 points</td></tr><tr><td><code>sub_family</code></td><td>family id</td><td>none</td><td>The sub family tag. It gives 1 point. Only a few genes have one</td></tr><tr><td><code>gains</code></td><td>list of effects</td><td><code>[]</code></td><td>The effects that help the mob</td></tr><tr><td><code>downsides</code></td><td>list of effects</td><td><code>[]</code></td><td>The effects that hurt the mob</td></tr><tr><td><code>selection_rate</code></td><td>number, 0 to 1</td><td><code>0.02</code></td><td>The rise for each death, as a part of the distance to 100%</td></tr><tr><td><code>mutation_rate</code></td><td>number, 0 to 1</td><td><code>0.003</code></td><td>The smallest chance that a new mob gets the gene</td></tr><tr><td><code>decay_rate</code></td><td>number, 0 to 1</td><td><code>0.01</code></td><td>The loss for each Minecraft day, as a part of the frequency</td></tr></tbody></table></div>\n<h4 id=\"dp-effects\">Effects</h4>\n<p>Each effect has a <code>type</code> field. A field with a value in parentheses is optional, and the value is its default. Times are in seconds, and distances are in blocks.</p>\n<h5 id=\"dp-body-and-damage\">Body and damage</h5>\n<div class=\"table-wrap\"><table><thead><tr><th>Type</th><th>Fields</th><th>Result</th></tr></thead><tbody><tr><td><code>attribute</code></td><td><code>attribute</code>, <code>amount</code>, <code>operation</code></td><td>Changes an attribute of the mob. <code>operation</code> is <code>add_value</code>, <code>add_multiplied_base</code>, or <code>add_multiplied_total</code></td></tr><tr><td><code>damage_taken</code></td><td><code>damage_types</code>, <code>multiplier</code></td><td>Multiplies the damage from these damage types. <code>0</code> makes the mob immune</td></tr><tr><td><code>damage_taken_when</code></td><td><code>when</code>, <code>multiplier</code></td><td>Multiplies the damage that the mob takes while the state is true</td></tr><tr><td><code>damage_dealt_when</code></td><td><code>when</code>, <code>multiplier</code></td><td>Multiplies the damage that the mob deals while the state is true</td></tr><tr><td><code>damage_from</code></td><td><code>attackers</code>, <code>multiplier</code></td><td>Multiplies the damage from these entity types, for example <code>minecraft:iron_golem</code></td></tr><tr><td><code>sunburn</code></td><td><code>multiplier</code></td><td>Multiplies the fire damage while the sun burns the mob. Other fire does normal damage</td></tr><tr><td><code>critical_immunity</code></td><td></td><td>A critical hit of a player does no extra damage to the mob</td></tr><tr><td><code>speed_when</code></td><td><code>when</code>, <code>multiplier</code></td><td>Multiplies the speed of the mob while the state is true</td></tr><tr><td><code>attack_interval</code></td><td><code>multiplier</code></td><td>Multiplies the time between two melee attacks. <code>0.7</code> attacks faster</td></tr><tr><td><code>water_damage</code></td><td><code>amount</code>, <code>interval</code> (<code>20</code>)</td><td>Damages the mob each <code>interval</code> ticks in water or rain</td></tr><tr><td><code>air_loss</code></td><td><code>extra</code></td><td>The mob loses this much more air each tick under water. Mobs that breathe under water, such as zombies, do not drown</td></tr><tr><td><code>regenerate</code></td><td><code>amount</code>, <code>interval_seconds</code> (<code>3</code>), <code>idle_seconds</code> (<code>5</code>)</td><td>Heals the mob after it was out of combat for <code>idle_seconds</code></td></tr></tbody></table></div>\n<p>The states for <code>when</code> are:</p>\n<div class=\"table-wrap\"><table><thead><tr><th>State</th><th>True when</th></tr></thead><tbody><tr><td><code>always</code></td><td>Always</td></tr><tr><td><code>alone</code></td><td>No other mob of the same species is in 8 blocks</td></tr><tr><td><code>ambush</code></td><td>The mob was not in combat for 10 seconds</td></tr><tr><td><code>dark</code></td><td>The light level at the mob is 6 or less</td></tr><tr><td><code>bright</code></td><td>The light level at the mob is 12 or more</td></tr><tr><td><code>hunting_wounded</code></td><td>The mob targets a player under half health</td></tr><tr><td><code>in_water</code></td><td>The mob is in water</td></tr></tbody></table></div>\n<h5 id=\"dp-hits\">Hits</h5>\n<div class=\"table-wrap\"><table><thead><tr><th>Type</th><th>Fields</th><th>Result</th></tr></thead><tbody><tr><td><code>ignite_target</code></td><td><code>chance</code>, <code>seconds</code></td><td>Each hit has this chance to set the target on fire</td></tr><tr><td><code>effect_on_hit</code></td><td><code>effect</code>, <code>seconds</code>, <code>amplifier</code> (<code>0</code>), <code>chance</code> (<code>1</code>)</td><td>Each hit gives the target a status effect</td></tr><tr><td><code>leech_xp</code></td><td><code>amount</code></td><td>Each hit takes XP points from a player. The mob drops the XP that it took when it dies, and no other XP</td></tr><tr><td><code>rally</code></td><td><code>per_ally</code>, <code>max</code>, <code>radius</code> (<code>6</code>)</td><td>Adds damage for each mob of the same species in the radius, up to <code>max</code>. <code>0.1</code> is 10%</td></tr><tr><td><code>convert_villagers</code></td><td></td><td>A villager that the mob kills always becomes a zombie villager</td></tr></tbody></table></div>\n<h5 id=\"dp-groups\">Groups</h5>\n<div class=\"table-wrap\"><table><thead><tr><th>Type</th><th>Fields</th><th>Result</th></tr></thead><tbody><tr><td><code>call_on_hurt</code></td><td><code>radius</code></td><td>When the mob is hurt, the mobs of its species in the radius attack the attacker</td></tr><tr><td><code>call_on_sight</code></td><td><code>radius</code>, <code>cooldown_seconds</code> (<code>30</code>)</td><td>When the mob first targets a player, the mobs of its species in the radius target that player</td></tr><tr><td><code>grave_call</code></td><td><code>chance</code>, <code>max_calls</code> (<code>2</code>), <code>radius</code> (<code>8</code>)</td><td>When the mob is hurt, it has a chance to call a new mob of its species, with no genes</td></tr><tr><td><code>brood</code></td><td><code>extra</code></td><td>When the mob spawns in a natural way, this many more mobs with the same genes spawn near it</td></tr><tr><td><code>runt</code></td><td><code>chance</code>, <code>baby_health_multiplier</code> (<code>0.5</code>)</td><td>The mob has a chance to spawn as a baby. Baby zombies spawn only through this effect</td></tr></tbody></table></div>\n<h5 id=\"dp-behavior\">Behavior</h5>\n<div class=\"table-wrap\"><table><thead><tr><th>Type</th><th>Fields</th><th>Result</th></tr></thead><tbody><tr><td><code>leap</code></td><td><code>min_distance</code> (<code>3</code>), <code>max_distance</code> (<code>5</code>), <code>cooldown_seconds</code> (<code>5</code>), <code>miss_stun_seconds</code> (<code>1</code>)</td><td>The mob leaps at its target. After a missed leap, it cannot move for a short time</td></tr><tr><td><code>hunt</code></td><td><code>invisible_radius</code> (<code>0</code>), <code>wounded_range_multiplier</code> (<code>1</code>)</td><td>The mob finds invisible players in the radius, and players under half health farther away</td></tr><tr><td><code>frenzied_targeting</code></td><td><code>chance</code>, <code>interval_seconds</code> (<code>10</code>), <code>radius</code> (<code>8</code>)</td><td>Now and then, the mob targets the nearest mob of a different species</td></tr><tr><td><code>lurk</code></td><td><code>radius</code> (<code>6</code>)</td><td>The mob does not move while its target player faces it, until the player is in the radius</td></tr><tr><td><code>burrow</code></td><td><code>player_radius</code> (<code>24</code>), <code>rise_seconds</code> (<code>2</code>)</td><td>At night, the mob can spawn under the ground near a player and rise. It rises only through the blocks in the tag <code>#originofspawns:burrowable</code></td></tr><tr><td><code>siege</code></td><td><code>break_seconds</code> (<code>10</code>)</td><td>The mob breaks doors on each difficulty, and breaks the blocks in the tag <code>#originofspawns:siege_breakable</code>. It obeys the <code>mobGriefing</code> gamerule</td></tr><tr><td><code>raise_victim</code></td><td></td><td>A player that the mob kills rises as a mob of its species, with its genes</td></tr><tr><td><code>attract_golems</code></td><td><code>radius</code> (<code>16</code>)</td><td>Iron golems in the radius attack the mob before other targets</td></tr></tbody></table></div>\n<h5 id=\"dp-senses\">Senses</h5>\n<div class=\"table-wrap\"><table><thead><tr><th>Type</th><th>Fields</th><th>Result</th></tr></thead><tbody><tr><td><code>silence</code></td><td><code>idle</code> (<code>false</code>), <code>hurt</code> (<code>false</code>), <code>steps</code> (<code>false</code>), <code>death</code> (<code>false</code>)</td><td>Stops these sounds of the mob</td></tr><tr><td><code>loud</code></td><td><code>multiplier</code></td><td>Multiplies the volume of the sounds of the mob</td></tr><tr><td><code>translucent</code></td><td><code>max_light</code> (<code>7</code>), <code>opacity</code> (<code>0.1</code>)</td><td>The client draws the mob translucent at this light level or lower. Other mobs still see it</td></tr><tr><td><code>glowing</code></td><td></td><td>The mob glows all the time, so that players see it through walls</td></tr><tr><td><code>aura</code></td><td><code>effect</code>, <code>amplifier</code> (<code>0</code>), <code>radius</code>, <code>interval_seconds</code> (<code>2</code>), <code>particles</code> (<code>false</code>)</td><td>Gives a status effect to the players near the mob</td></tr></tbody></table></div>\n<h5 id=\"dp-death\">Death</h5>\n<div class=\"table-wrap\"><table><thead><tr><th>Type</th><th>Fields</th><th>Result</th></tr></thead><tbody><tr><td><code>death_burst</code></td><td><code>damage</code>, <code>radius</code></td><td>When the mob dies, it damages the players in the radius</td></tr><tr><td><code>death_cloud</code></td><td><code>effect</code>, <code>seconds</code>, <code>amplifier</code> (<code>0</code>), <code>radius</code>, <code>cloud_seconds</code> (<code>5</code>)</td><td>When the mob dies, it leaves a cloud that gives a status effect</td></tr></tbody></table></div>\n<h4 id=\"dp-gene-visuals\">Gene visuals</h4>\n<p>The look of a gene is in the resource packs, not in the data packs. The file <code>assets/&lt;namespace&gt;/gene_visuals/&lt;name&gt;.json</code> is the visual of the gene <code>&lt;namespace&gt;:&lt;name&gt;</code>. A resource pack can change the look of any gene.</p>\n<pre class=\"code\"><code>{\n    \"region_map\": \"originofspawns:textures/region/humanoid.png\",\n    \"layers\": [\n        { \"materials\": [\"skin\"], \"effect\": { \"type\": \"desaturate\", \"amount\": 0.4, \"darken\": 0.15 } },\n        { \"regions\": [\"hands\", \"feet\"], \"effect\": { \"type\": \"spots\", \"colors\": [\"#1A1A1A\", \"#8B2E1F\"], \"density\": 0.35 } }\n    ],\n    \"model\": \"originofspawns:bone_legs\"\n}</code></pre>\n<div class=\"table-wrap\"><table><thead><tr><th>Field</th><th>Default</th><th>Description</th></tr></thead><tbody><tr><td><code>region_map</code></td><td><code>originofspawns:textures/region/humanoid.png</code></td><td>The map that labels the pixels of the mob texture</td></tr><tr><td><code>layers</code></td><td></td><td>The changes to the texture, in order. A layer with no <code>regions</code> or no <code>materials</code> covers all of them. A layer can have an <code>order</code>, an integer with the default <code>0</code></td></tr><tr><td><code>model</code></td><td>none</td><td>A gene model that adds cubes to the mob, for example <code>originofspawns:bone_legs</code></td></tr></tbody></table></div>\n<p>The layers of all the genes of a mob go on in sequence of their <code>order</code>. A layer with a lower order goes on first, so a higher order puts a mark on top of the marks of other genes. Layers with the same order keep the order of the gene ids, and the order in their recipe.</p>\n<p>The regions are <code>face</code>, <code>scalp</code>, <code>torso</code>, <code>arms</code>, <code>hands</code>, <code>legs</code>, <code>lower_legs</code>, and <code>feet</code>. The materials are <code>skin</code>, <code>cloth</code>, and <code>eyes</code>.</p>\n<div class=\"table-wrap\"><table><thead><tr><th>Effect</th><th>Fields</th><th>Result</th></tr></thead><tbody><tr><td><code>desaturate</code></td><td><code>amount</code>, <code>darken</code></td><td>Moves the color toward gray, and makes it darker</td></tr><tr><td><code>spots</code></td><td><code>colors</code>, <code>density</code></td><td>Puts spots of the colors on a part of the pixels</td></tr><tr><td><code>pattern</code></td><td><code>texture</code>, <code>tint</code></td><td>Stamps a tiled grayscale texture in the tint color</td></tr><tr><td><code>erase</code></td><td></td><td>Makes the pixels transparent</td></tr></tbody></table></div>\n<h4 id=\"dp-gene-models\">Gene models</h4>\n<p>A gene model adds cubes to the model of a mob. The file <code>assets/&lt;namespace&gt;/gene_models/&lt;name&gt;.json</code> is the model <code>&lt;namespace&gt;:&lt;name&gt;</code>. Each cube moves with one bone: <code>head</code>, <code>body</code>, <code>right_arm</code>, <code>left_arm</code>, <code>right_leg</code>, or <code>left_leg</code>. The coordinates are in pixels, in the coordinates of the bone, as in the vanilla models.</p>\n<pre class=\"code\"><code>{\n    \"texture\": \"originofspawns:textures/entity/bone_leg.png\",\n    \"texture_width\": 16,\n    \"texture_height\": 16,\n    \"parts\": [\n        { \"bone\": \"right_leg\", \"uv\": [0, 0], \"from\": [-1.5, 6, -1.5], \"size\": [3, 6, 3] }\n    ]\n}</code></pre>\n<div class=\"table-wrap\"><table><thead><tr><th>Field</th><th>Default</th><th>Description</th></tr></thead><tbody><tr><td><code>texture</code></td><td></td><td>The texture of the cubes. It is optional when <code>mob_texture</code> is <code>true</code></td></tr><tr><td><code>mob_texture</code></td><td><code>false</code></td><td>The cubes use the texture of the mob, with the marks of all its genes. A swollen copy of a body part keeps the marks of the other genes this way</td></tr><tr><td><code>texture_width</code>, <code>texture_height</code></td><td><code>64</code></td><td>The size of the texture</td></tr><tr><td><code>parts</code></td><td></td><td>The cubes</td></tr></tbody></table></div>\n<div class=\"table-wrap\"><table><thead><tr><th>Part field</th><th>Default</th><th>Description</th></tr></thead><tbody><tr><td><code>bone</code></td><td></td><td>The bone that the cube moves with</td></tr><tr><td><code>uv</code></td><td></td><td>The top left corner of the box UV in the texture</td></tr><tr><td><code>from</code></td><td></td><td>The corner of the cube with the smallest coordinates</td></tr><tr><td><code>size</code></td><td></td><td>The size of the cube</td></tr><tr><td><code>inflate</code></td><td><code>0</code></td><td>Makes the cube larger on each side, with no change to its UV</td></tr><tr><td><code>mirror</code></td><td><code>false</code></td><td>Mirrors the UV</td></tr><tr><td><code>pivot</code></td><td><code>[0, 0, 0]</code></td><td>The point that the cube turns around, in the coordinates of the bone</td></tr><tr><td><code>rotation</code></td><td><code>[0, 0, 0]</code></td><td>The angles around the X, Y, and Z axes of the pivot, in degrees</td></tr><tr><td><code>glow</code></td><td><code>false</code></td><td>Gives the cube full light, so that it is bright in the dark</td></tr></tbody></table></div>\n<h4 id=\"dp-baby-zombies\">Baby zombies</h4>\n<p>Baby zombies use a smaller model with another texture layout. Draw the marks and the models for the adult only. The game moves each adult face of the region map and of the marks to the same baby face, and it moves and scales the cubes of gene models from the adult bones to the baby bones. A pattern smaller than 64 x 64 is a tile, and it stays as it is. Thin marks can lose detail, because an adult limb of 12 rows becomes 5 rows.</p>\n<h4 id=\"dp-genes-on-mobs\">Genes on mobs</h4>\n<ul><li>A mob gets its genes one time, when it first joins a world.</li><li>Each gene has a chance equal to its frequency in the pool of the biome category, or its mutation rate if that is higher.</li><li>A mob carries a maximum of 5 genes. The genes with the highest frequency stay.</li></ul>\n<h3 id=\"dp-family\">Family</h3>\n<p>A family is a major family of a species. The genes of a mob give points to families, and the points decide the family of the mob.</p>\n<pre class=\"code\"><code>{\n    \"species\": \"minecraft:zombie\",\n    \"tiers\": [\n        { \"buffs\": [...], \"debuffs\": [...] },\n        { \"buffs\": [...], \"debuffs\": [...] },\n        { \"buffs\": [...], \"debuffs\": [...] }\n    ]\n}</code></pre>\n<div class=\"table-wrap\"><table><thead><tr><th>Field</th><th>Type</th><th>Description</th></tr></thead><tbody><tr><td><code>species</code></td><td>entity type, list, or tag</td><td>The mobs that the family is for</td></tr><tr><td><code>tiers</code></td><td>list of 3 tiers</td><td>The effects of tier 1, tier 2, and tier 3. Each tier has <code>buffs</code> and <code>debuffs</code>, which are lists of effects</td></tr></tbody></table></div>\n<p>A main family tag gives 2 points, and a sub family tag gives 1 point.</p>\n<div class=\"table-wrap\"><table><thead><tr><th>Result</th><th>Rule</th><th>Effects</th></tr></thead><tbody><tr><td>Neutral</td><td>No family has 4 points</td><td>None</td></tr><tr><td>Major</td><td>One family has 4 points or more, and no other family has 2</td><td>Tier 2 of the major</td></tr><tr><td>Pure</td><td>One family has 6 points or more, and no other family has points</td><td>Tier 3 of the family</td></tr><tr><td>Minor</td><td>The major has 4 points or more, and a sub family has 2 or more</td><td>Tier 2 of the major, and tier 1 of the sub</td></tr><tr><td>Hybrid</td><td>Two families have the same points, 4 or more</td><td>Tier 2 of each</td></tr></tbody></table></div>\n<p>The names of the families are in the language files:</p>\n<div class=\"table-wrap\"><table><thead><tr><th>Result</th><th>Key</th></tr></thead><tbody><tr><td>Pure or major</td><td><code>family.&lt;namespace&gt;.&lt;path&gt;</code>, for example <code>family.originofspawns.zombie.predator</code></td></tr><tr><td>Minor</td><td>The key of the major, then the last part of the sub, for example <code>family.originofspawns.zombie.predator.stalker</code></td></tr><tr><td>Hybrid</td><td>The key of the first family by id, then <code>and</code>, then the last part of the second, for example <code>family.originofspawns.zombie.predator.and.stalker</code></td></tr><tr><td>Neutral</td><td><code>family.originofspawns.neutral</code></td></tr></tbody></table></div>\n<h3 id=\"dp-perk\">Perk</h3>\n<p>A perk is a player adaptation.</p>\n<pre class=\"code\"><code>{ \"xp_cost\": 5, \"incompatible_with\": [\"originofspawns:carnivore\"] }</code></pre>\n<div class=\"table-wrap\"><table><thead><tr><th>Field</th><th>Type</th><th>Default</th><th>Description</th></tr></thead><tbody><tr><td><code>xp_cost</code></td><td>integer, 0 or more</td><td></td><td>The XP levels that the player pays</td></tr><tr><td><code>incompatible_with</code></td><td>list of perk ids</td><td><code>[]</code></td><td>The perks that the player cannot have at the same time</td></tr></tbody></table></div>\n<h3 id=\"dp-debug-commands\">Debug commands</h3>\n<p>The commands need permission level 2.</p>\n<div class=\"table-wrap\"><table><thead><tr><th>Command</th><th>Result</th></tr></thead><tbody><tr><td><code>/originofspawns pool get &lt;species&gt; &lt;category&gt;</code></td><td>Shows the gene frequencies of a pool</td></tr><tr><td><code>/originofspawns pool set &lt;species&gt; &lt;category&gt; &lt;gene&gt; &lt;frequency&gt;</code></td><td>Sets one gene frequency, from 0 to 1</td></tr></tbody></table></div>\n<p>The categories are <code>hot</code>, <code>cold</code>, <code>wet</code>, <code>temperate</code>, <code>nether</code>, and <code>end</code>. The world saves the pools in <code>data/originofspawns/gene_pools.dat</code>.</p>",
  "toc": [
   {
    "level": 3,
    "id": "dp-pressure",
    "title": "Pressure"
   },
   {
    "level": 3,
    "id": "dp-gene",
    "title": "Gene"
   },
   {
    "level": 4,
    "id": "dp-effects",
    "title": "Effects"
   },
   {
    "level": 4,
    "id": "dp-gene-visuals",
    "title": "Gene visuals"
   },
   {
    "level": 4,
    "id": "dp-gene-models",
    "title": "Gene models"
   },
   {
    "level": 4,
    "id": "dp-baby-zombies",
    "title": "Baby zombies"
   },
   {
    "level": 4,
    "id": "dp-genes-on-mobs",
    "title": "Genes on mobs"
   },
   {
    "level": 3,
    "id": "dp-family",
    "title": "Family"
   },
   {
    "level": 3,
    "id": "dp-perk",
    "title": "Perk"
   },
   {
    "level": 3,
    "id": "dp-debug-commands",
    "title": "Debug commands"
   }
  ]
 }
};
