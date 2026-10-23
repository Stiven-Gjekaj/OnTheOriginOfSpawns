"""Make close-up previews of the gene models and of some marks.

Usage: python3 tools/preview_closeups.py [zombie] [husk] [drowned] [skeleton]

With no species, the script makes the close-ups of all the species.

Each close-up shows a few mobs from several sides, on a white
background. A view "at night" has a dark background and low light, so that
the parts that glow show. The script writes PNG files to
previews/closeups/, which Git ignores. The folder is not in build/, so that
"gradlew clean" does not delete it.
Each preview uses the vanilla texture. Do not commit a preview.
This script needs Pillow.
"""
import os
import sys

from PIL import Image, ImageDraw

import preview_genes as pg

OUT_DIR = os.path.join(pg.ROOT, "previews/closeups")
NIGHT = (18, 22, 38, 255)

FRONT = ("front", 0, 0, 1.0)
BACK = ("back", 180, 0, 1.0)
SIDE = ("right side", -90, 0, 1.0)
THREE_QUARTER = ("3/4", -35, 20, 1.0)
BEHIND = ("3/4 from behind", -145, 15, 1.0)
NIGHT_VIEW = ("3/4 at night", -35, 20, 0.18)

# Each close-up: a title, the rows (a label, the genes, True for a baby),
# and the views.
CLOSEUPS = {
    "death_throes_fuse": ("Death Throes fuse",
                          [("Death Throes", ["death_throes"], False),
                           ("All 6 Predators", pg.FAMILIES["predators"], False)],
                          [FRONT, BACK, SIDE, THREE_QUARTER, NIGHT_VIEW]),
    "lurker_hood": ("Lurker hood",
                    [("Lurker", ["lurker"], False),
                     ("Lurker, Nightborn, Hush, Cinder", ["lurker", "nightborn", "hush", "cinder"], False)],
                    [FRONT, THREE_QUARTER, SIDE, BEHIND]),
    "thickskull": ("Thickskull",
                   [("Thickskull", ["thickskull"], False),
                    ("Thickskull, Lurker, Runt", ["thickskull", "lurker", "runt"], False),
                    ("Thickskull, Rabid, Nightborn", ["thickskull", "rabid", "nightborn"], False)],
                   [FRONT, BACK, THREE_QUARTER, BEHIND]),
    "runt": ("Runt",
             [("Runt", ["runt"], False), ("Runt baby", ["runt"], True), ("Plain baby", [], True)],
             [FRONT, THREE_QUARTER, ("3/4 from above", -35, 35, 1.0)]),
    "siege": ("Siege",
              [("Siege", ["siege"], False),
               ("Siege, Cinder, Grave Call", ["siege", "cinder", "grave_call"], False),
               ("Siege, Rally, Blastproof", ["siege", "rally", "blastproof"], False)],
              [FRONT, THREE_QUARTER]),
    "leech_miasma": ("Leech and Miasma",
                     [("Leech", ["leech"], False), ("Miasma", ["miasma"], False),
                      ("Leech, Miasma, Contagion, Rally, Mender",
                       ["leech", "miasma", "contagion", "rally", "mender"], False)],
                     [FRONT, THREE_QUARTER, SIDE]),
    "nightborn_shade": ("Nightborn and Shade at night",
                        [("Nightborn", ["nightborn"], False), ("Shade", ["shade"], False)],
                        [FRONT, NIGHT_VIEW]),
}

LEFT_SIDE = ("left side", 90, 0, 1.0)
ABOVE = ("from above", -150, 60, 1.0)
NIGHT_BEHIND = ("3/4 behind, at night", -145, 15, 0.18)
ALL_SIDES = [FRONT, BACK, LEFT_SIDE, SIDE, THREE_QUARTER, BEHIND, ABOVE]
HUSK = pg.HUSK_FAMILIES
DROWNED = pg.DROWNED_FAMILIES


def gene_rows(genes, others):
    """Return the rows of a close-up of one change: the genes alone, the genes
    with other genes, and the genes on a baby."""
    return [(name(genes), genes, False), (name(genes + others), genes + others, False),
            (name(genes) + " baby", genes, True)]


def name(genes):
    return ", ".join(gene.replace("_", " ").title() for gene in genes)


# The close-ups of the husk.
HUSK_CLOSEUPS = {
    "husk_undying": ("Undying",
                     [("Undying", ["undying"], False),
                      ("Undying + Wrapped + Solar Flare", ["undying", "wrapped", "solar_flare"], False)],
                     [FRONT, BACK, THREE_QUARTER, NIGHT_VIEW]),
    "husk_signal_fire": ("Signal Fire", gene_rows(["signal_fire"], ["packmule", "nomad"]),
                         [FRONT, BACK, LEFT_SIDE, SIDE, ABOVE, BEHIND, NIGHT_BEHIND]),
    "husk_nomad": ("Nomad from all sides", gene_rows(["nomad"], ["tomb_guard", "packmule"]),
                   [FRONT, BACK, LEFT_SIDE, SIDE, ("from above", -30, 70, 1.0),
                    ("from below", -30, -60, 1.0)]),
    "husk_caravans": ("Caravan models",
                      [("Packmule + Signal Fire", ["packmule", "signal_fire"], False),
                       ("All 6 Caravans", HUSK["caravans"], False)],
                      [FRONT, BACK, SIDE, THREE_QUARTER, BEHIND]),
    "husk_parchers": ("Parcher faces and Salt Crust",
                      [("Thirstbite + Dehydrator", ["thirstbite", "dehydrator"], False),
                       ("Desiccate", ["desiccate"], False), ("Salt Crust", ["salt_crust"], False),
                       ("Salt Crust + Packmule", ["salt_crust", "packmule", "signal_fire"], False)],
                      [FRONT, BACK, LEFT_SIDE, THREE_QUARTER, BEHIND, ("from above", -30, 60, 1.0)]),
}

# The close-ups of the drowned.
DROWNED_CLOSEUPS = {
    "drowned_riptides": ("Riptide models",
                         [("Undertow", ["undertow"], False), ("Torrent", ["torrent"], False),
                          ("Breacher", ["breacher"], False), ("All 6", DROWNED["riptides"], False),
                          ("All 6 baby", DROWNED["riptides"], True)], ALL_SIDES),
    "drowned_harpooners": ("Harpooner models",
                           [("Barbed + Volley", ["barbed", "volley"], False), ("Scavenger", ["scavenger"], False),
                            ("Spearfisher + Stormcaller", ["spearfisher", "stormcaller"], False),
                            ("All 6", DROWNED["harpooners"], False)],
                           [FRONT, BACK, LEFT_SIDE, SIDE, THREE_QUARTER, ("from above", -30, 60, 1.0)]),
    "drowned_abyssals": ("Abyssal models",
                         [("Lure", ["lure"], False), ("Glowspot", ["glowspot"], False),
                          ("Blind Hunter + Ink + Pressure Hide", ["blind_hunter", "ink", "pressure_hide"], False),
                          ("All 6", DROWNED["abyssals"], False), ("All 6 baby", DROWNED["abyssals"], True)],
                         [FRONT, LEFT_SIDE, SIDE, THREE_QUARTER, ("front at night", 0, 0, 0.18),
                          ("left side at night", 90, 0, 0.18), NIGHT_VIEW]),
    "drowned_barnacled": ("Barnacled models",
                          [("Barnacle Crust", ["barnacle_crust"], False), ("Coral Growth", ["coral_growth"], False),
                           ("Shellback", ["shellback"], False), ("Anchor + Pearlheart", ["anchor", "pearlheart"], False),
                           ("Kelpwrapped", ["kelpwrapped"], False), ("All 6", DROWNED["barnacled"], False)],
                          ALL_SIDES),
    "drowned_sirens": ("Siren models",
                       [("Fry baby", ["fry"], True), ("Fry, adult carrier", ["fry"], False),
                        ("Drag Under + Chorus", ["drag_under", "chorus"], False),
                        ("Siren Song + Shoal + Wreckcaller", ["siren_song", "shoal", "wreckcaller"], False),
                        ("All 6", DROWNED["sirens"], False), ("All 6 baby", DROWNED["sirens"], True)],
                       ALL_SIDES),
    "drowned_torrent": ("Torrent", gene_rows(["torrent"], ["undertow", "breacher", "surge"]), ALL_SIDES),
    "drowned_undertow": ("Undertow", gene_rows(["undertow"], ["gills", "glowspot", "lure", "pressure_hide"]),
                         ALL_SIDES),
    "drowned_breacher": ("Breacher", gene_rows(["breacher"], ["undertow", "torrent", "surge"]), ALL_SIDES),
    "drowned_barbed": ("Barbed", gene_rows(["barbed"], ["volley", "undertow", "breacher"]), ALL_SIDES),
    "drowned_anchor": ("Anchor", gene_rows(["anchor"], ["torrent", "kelpwrapped"]), ALL_SIDES),
    "drowned_wreckcaller": ("Wreckcaller", gene_rows(["wreckcaller"], ["spearfisher", "scavenger"]), ALL_SIDES),
    "drowned_pearlheart": ("Pearlheart", gene_rows(["pearlheart"], ["wreckcaller", "spearfisher", "ink"]),
                           ALL_SIDES[:-1] + [NIGHT_VIEW]),
    "drowned_drag_under": ("Drag Under", gene_rows(["drag_under"], ["surge", "capsizer"]), ALL_SIDES),
    "drowned_chorus": ("Chorus", gene_rows(["chorus"], ["breacher", "barnacle_crust", "shellback"]), ALL_SIDES),
}

# The close-ups of the skeleton. The skeleton has no babies.
SKELETON = pg.SKELETON_FAMILIES
SKELETON_CLOSEUPS = {
    "skeleton_marksmen": ("Marksman marks and models",
                          [("Eagle Eye + Deadeye", ["eagle_eye", "deadeye"], False),
                           ("Longdraw + Quickdraw", ["longdraw", "quickdraw"], False),
                           ("Shieldbreaker + High Ground", ["shieldbreaker", "high_ground"], False),
                           ("All 6", SKELETON["marksmen"], False)], ALL_SIDES),
    "skeleton_rattlers": ("Rattler marks and models",
                          [("Skitter + Lightfoot", ["skitter", "lightfoot"], False),
                           ("Backpedal + Bonedash", ["backpedal", "bonedash"], False),
                           ("Hollow Bones + Fleetbone", ["hollow_bones", "fleetbone"], False),
                           ("All 6", SKELETON["rattlers"], False)], ALL_SIDES),
    "skeleton_ossified": ("Ossified marks and models",
                          [("Thickbone + Reassemble", ["thickbone", "reassemble"], False),
                           ("Bonewall + Holy Ward", ["bonewall", "holy_ward"], False),
                           ("Calcified + Marrowmend", ["calcified", "marrowmend"], False),
                           ("All 6", SKELETON["ossified"], False)], ALL_SIDES),
    "skeleton_legions": ("Legion marks and models",
                         [("Barrage + Phalanx", ["barrage", "phalanx"], False),
                          ("Bugler + Muster", ["bugler", "muster"], False),
                          ("Warcry + Spider Rider", ["warcry", "spider_rider"], False),
                          ("All 6", SKELETON["legions"], False)], ALL_SIDES),
    "skeleton_hexers": ("Hexer marks and models",
                        [("Venomtip + Witherhex", ["venomtip", "witherhex"], False),
                         ("Frostnock", ["frostnock"], False),
                         ("Blindshot + Hexmark", ["blindshot", "hexmark"], False),
                         ("Soulfire + Hexmark", ["soulfire", "hexmark"], False),
                         ("All 6", SKELETON["hexers"], False)],
                        [FRONT, SIDE, BACK, THREE_QUARTER, ("front at night", 0, 0, 0.18), NIGHT_BEHIND]),
    "skeleton_redesigns": ("Bonedash, Fleetbone, High Ground, and Quickdraw",
                           [("Bonedash", ["bonedash"], False), ("Fleetbone", ["fleetbone"], False),
                            ("High Ground", ["high_ground"], False), ("Quickdraw", ["quickdraw"], False)],
                           [FRONT, SIDE, BACK, THREE_QUARTER]),
}

# The close-ups of the stray. The stray has no babies.
STRAY = pg.STRAY_FAMILIES
STRAY_CLOSEUPS = {
    "stray_glaciers": ("Glacier marks and models",
                       [("Iceplate + Icebound", ["iceplate", "icebound"], False),
                        ("Permafrost + Rimeshell", ["permafrost", "rimeshell"], False),
                        ("Shatterhide", ["shatterhide"], False),
                        ("Glacial Wall", ["glacial_wall"], False),
                        ("All 6", STRAY["glaciers"], False)], ALL_SIDES),
    "stray_blizzards": ("Blizzard marks and models",
                        [("Squall + Frostwind", ["squall", "frostwind"], False),
                         ("Snowdrift + Stormborn", ["snowdrift", "stormborn"], False),
                         ("Hailshot", ["hailshot"], False),
                         ("Gale", ["gale"], False),
                         ("All 6", STRAY["blizzards"], False)], ALL_SIDES),
    "stray_drifters": ("Drifter marks and models",
                       [("Snowshoe + Pathfinder", ["snowshoe", "pathfinder"], False),
                        ("Iceskate + Avalanche", ["iceskate", "avalanche"], False),
                        ("Snowburrow", ["snowburrow"], False),
                        ("Driftline", ["driftline"], False),
                        ("All 6", STRAY["drifters"], False)], ALL_SIDES),
    "stray_frostbiters": ("Frostbiter marks and models",
                          [("Frostbite + Numbing Shot", ["frostbite", "numbing_shot"], False),
                           ("Icicle + Coldsnap", ["icicle", "coldsnap"], False),
                           ("Rimefall", ["rimefall"], False),
                           ("Brittle Shot", ["brittle_shot"], False),
                           ("All 6", STRAY["frostbiters"], False)], ALL_SIDES),
    "stray_wailers": ("Wailer marks and models",
                      [("Pale", ["pale"], False), ("Wail + Frozen Gaze", ["wail", "frozen_gaze"], False),
                       ("Hollow Cry + Ghostshot", ["hollow_cry", "ghostshot"], False),
                       ("Mourner", ["mourner"], False),
                       ("All 6", STRAY["wailers"], False)],
                      [FRONT, SIDE, BACK, THREE_QUARTER, ("front at night", 0, 0, 0.18), NIGHT_BEHIND]),
}

# The close-ups of the bogged. The bogged has no babies.
BOGGED = pg.BOGGED_FAMILIES
BOGGED_CLOSEUPS = {
    "bogged_sporecaps": ("Sporecap marks and models",
                         [("Puffball + Regrowth", ["puffball", "regrowth"], False),
                          ("Shelfback + Rot Mend", ["shelfback", "rot_mend"], False),
                          ("Mushroom Shot + Spore Lung", ["mushroom_shot", "spore_lung"], False),
                          ("All 6", BOGGED["sporecaps"], False)], ALL_SIDES),
    "bogged_mirefolk": ("Mirefolk marks and models",
                        [("Wader + Mire Grip", ["wader", "mire_grip"], False),
                         ("Mudcake + Swamp Gas", ["mudcake", "swamp_gas"], False),
                         ("Mudburrow + Leechbone", ["mudburrow", "leechbone"], False),
                         ("All 6", BOGGED["mirefolk"], False)], ALL_SIDES),
    "bogged_venomers": ("Venomer marks and models",
                        [("Venom Gland + Toxic Tip", ["venom_gland", "toxic_tip"], False),
                         ("Bile Shot + Rotting Touch", ["bile_shot", "rotting_touch"], False),
                         ("Antivenom + Blightburst", ["antivenom", "blightburst"], False),
                         ("All 6", BOGGED["venomers"], False)],
                        [FRONT, SIDE, BACK, THREE_QUARTER, ("front at night", 0, 0, 0.18), NIGHT_BEHIND]),
    "bogged_vinebound": ("Vinebound marks and models",
                         [("Rootshot + Thornwrap", ["rootshot", "thornwrap"], False),
                          ("Creeper Vines + Snare", ["creeper_vines", "snare"], False),
                          ("Mossbed + Vine Swing", ["mossbed", "vine_swing"], False),
                          ("All 6", BOGGED["vinebound"], False)], ALL_SIDES),
    "bogged_mosshides": ("Mosshide marks and models",
                         [("Lily Pad + Ambush Shot", ["lily_pad", "ambush_shot"], False),
                          ("Swamp Shroud + Silent Rot", ["swamp_shroud", "silent_rot"], False),
                          ("Bog Mist + Barkskin", ["bog_mist", "barkskin"], False),
                          ("All 6", BOGGED["mosshides"], False)], ALL_SIDES),
    "bogged_changes": ("Bogged genes after the review",
                       [("Snare", ["snare"], False), ("Lily Pad", ["lily_pad"], False),
                        ("Wader", ["wader"], False), ("Mire Grip", ["mire_grip"], False),
                        ("Swamp Gas", ["swamp_gas"], False), ("Bile Shot", ["bile_shot"], False),
                        ("Bog Mist", ["bog_mist"], False)],
                       [FRONT, SIDE, BACK, THREE_QUARTER]),
}

# The close-ups of the wither skeleton. It has no babies.
WITHER = pg.WITHER_SKELETON_FAMILIES
WITHER_SKELETON_CLOSEUPS = {
    "wither_blademasters": ("Blademaster marks and models",
                            [("Duelist + Parry", ["duelist", "parry"], False),
                             ("Cleave + Lunge", ["cleave", "lunge"], False),
                             ("Bladestorm + Executioner", ["bladestorm", "executioner"], False),
                             ("All 6", WITHER["blademasters"], False)], ALL_SIDES),
    "wither_ashborn": ("Ashborn marks and models",
                       [("Cinderblade + Pyre", ["cinderblade", "pyre"], False),
                        ("Lavawalker + Cauterize", ["lavawalker", "cauterize"], False),
                        ("Smoke Screen + Ember Eyes", ["smoke_screen", "ember_eyes"], False),
                        ("All 6", WITHER["ashborn"], False)],
                       [FRONT, SIDE, BACK, THREE_QUARTER, ("front at night", 0, 0, 0.18), NIGHT_BEHIND]),
    "wither_witherlords": ("Witherlord marks and models",
                           [("Deep Wither + Decay Touch", ["deep_wither", "decay_touch"], False),
                            ("Wither Aura + Black Rot", ["wither_aura", "black_rot"], False),
                            ("Skull Shot + Grave Chill", ["skull_shot", "grave_chill"], False),
                            ("All 6", WITHER["witherlords"], False)], ALL_SIDES),
    "wither_ramparts": ("Rampart marks and models",
                        [("Brickplate + Smite Ward", ["brickplate", "smite_ward"], False),
                         ("Holdfast + Sentinel", ["holdfast", "sentinel"], False),
                         ("Brickshield + Garrison", ["brickshield", "garrison"], False),
                         ("All 6", WITHER["ramparts"], False)], ALL_SIDES),
    "wither_soul_reapers": ("Soul Reaper marks and models",
                            [("Soulstride + Ghost Step", ["soulstride", "ghost_step"], False),
                             ("Soul Link + Soul Drain", ["soul_link", "soul_drain"], False),
                             ("Vengeful Spirit + Soul Hunger", ["vengeful_spirit", "soul_hunger"], False),
                             ("All 6", WITHER["soul_reapers"], False)],
                            [FRONT, SIDE, BACK, THREE_QUARTER, ("front at night", 0, 0, 0.18), NIGHT_BEHIND]),
    "wither_changes": ("Wither skeleton genes after the review",
                       [("Soul Hunger", ["soul_hunger"], False), ("Vengeful Spirit", ["vengeful_spirit"], False)],
                       [FRONT, SIDE, BACK, THREE_QUARTER]),
    "wither_soul_hunger": ("Soul Hunger", [("Soul Hunger", ["soul_hunger"], False)], ALL_SIDES),
}

# The close-ups of the parched. It has no babies.
PARCHED = pg.PARCHED_FAMILIES
PARCHED_CLOSEUPS = {
    "parched_enfeeblers": ("Enfeebler marks",
                           [("Sap + Fumble", ["sap", "fumble"], False),
                            ("Exploit + Unnerving Rattle", ["exploit", "unnerving_rattle"], False),
                            ("Curse Cloud + Withering Glare", ["curse_cloud", "withering_glare"], False),
                            ("All 6", PARCHED["enfeeblers"], False)], ALL_SIDES),
    "parched_windcarvers": ("Windcarver marks and models",
                            [("Long Wind + Quickwind", ["long_wind", "quickwind"], False),
                             ("Gust Shot + Tailwind", ["gust_shot", "tailwind"], False),
                             ("Wind Dodge + Whirlwind Volley", ["wind_dodge", "whirlwind_volley"], False),
                             ("All 6", PARCHED["windcarvers"], False)], ALL_SIDES),
    "parched_vultures": ("Vulture marks and models",
                         [("Carrion Eye + Gorge", ["carrion_eye", "gorge"], False),
                          ("Circler + Flock", ["circler", "flock"], False),
                          ("Glide + Watcher", ["glide", "watcher"], False),
                          ("All 6", PARCHED["vultures"], False)], ALL_SIDES),
    "parched_scorpions": ("Scorpion marks and models",
                          [("Stinger + Tail Whip", ["stinger", "tail_whip"], False),
                           ("Pincers + Sand Hide", ["pincers", "sand_hide"], False),
                           ("Carapace + Venom Sac", ["carapace", "venom_sac"], False),
                           ("All 6", PARCHED["scorpions"], False)], ALL_SIDES),
    "parched_fossils": ("Fossil marks and models",
                        [("Petrified + Stoneskin", ["petrified", "stoneskin"], False),
                         ("Amber Heart + Last Stand", ["amber_heart", "last_stand"], False),
                         ("Bedrock Bones + Sediment", ["bedrock_bones", "sediment"], False),
                         ("All 6", PARCHED["fossils"], False)],
                        [FRONT, SIDE, BACK, THREE_QUARTER, ("front at night", 0, 0, 0.18), NIGHT_BEHIND]),
}

# The close-ups of the creeper. It has no babies.
CREEPER = pg.CREEPER_FAMILIES
NIGHT_VIEWS = [FRONT, SIDE, BACK, THREE_QUARTER, ("front at night", 0, 0, 0.18), NIGHT_BEHIND]
CREEPER_CLOSEUPS = {
    "creeper_blasters": ("Blaster marks and models",
                         [("Big Bang + Concussion", ["big_bang", "concussion"], False),
                          ("Shrapnel + Aftershock", ["shrapnel", "aftershock"], False),
                          ("Golem Breaker + Chain Reaction", ["golem_breaker", "chain_reaction"], False),
                          ("All 6", CREEPER["blasters"], False)], ALL_SIDES),
    "creeper_rushers": ("Rusher marks and models",
                        [("Sprinter + Short Fuse", ["sprinter", "short_fuse"], False),
                         ("Leaper + Dodger", ["leaper", "dodger"], False),
                         ("Tracker + Wallcrawler", ["tracker", "wallcrawler"], False),
                         ("All 6", CREEPER["rushers"], False)], ALL_SIDES),
    "creeper_skulkers": ("Skulker marks and models",
                          [("Silent Fuse + Surprise", ["silent_fuse", "surprise"], False),
                           ("Leafskin + Patient", ["leafskin", "patient"], False),
                           ("Gloom + Veiled", ["gloom", "veiled"], False),
                           ("All 6", CREEPER["skulkers"], False)], NIGHT_VIEWS),
    "creeper_fumers": ("Fumer marks and models",
                       [("Toxic Blast + Stink", ["toxic_blast", "stink"], False),
                        ("Smoke Bomb + Gas Leak", ["smoke_bomb", "gas_leak"], False),
                        ("Wither Gas + Spore Blast", ["wither_gas", "spore_blast"], False),
                        ("All 6", CREEPER["fumers"], False)], ALL_SIDES),
    "creeper_thunderheads": ("Thunderhead marks and models",
                             [("Lightning Rod + Conductor", ["lightning_rod", "conductor"], False),
                              ("Static + Overcharge", ["static", "overcharge"], False),
                              ("Grounded + Thunderstruck", ["grounded", "thunderstruck"], False),
                              ("All 6", CREEPER["thunderheads"], False)], NIGHT_VIEWS),
}

# Each species: its texture, its baby texture, and its close-ups.
# The close-ups of the spider. It has no babies. The views look down on it,
# so that its back shows.
SPIDER = pg.SPIDER_FAMILIES
SPIDER_VIEWS = [("front", 0, 20, 1.0), ("back", 180, 30, 1.0), ("right side", -90, 10, 1.0),
                ("3/4", -35, 30, 1.0), ("3/4 from behind", -145, 30, 1.0), ("from above", -150, 60, 1.0)]
SPIDER_NIGHT_VIEWS = SPIDER_VIEWS[:5] + [("3/4 at night", -35, 30, 0.18)]
SPIDER_CLOSEUPS = {
    "spider_weavers": ("Weaver marks and models",
                       [("Web Shot + Web Walker", ["web_shot", "web_walker"], False),
                        ("Tripwire + Silk Armor", ["tripwire", "silk_armor"], False),
                        ("Cocoon + Snare Line", ["cocoon", "snare_line"], False),
                        ("All 6", SPIDER["weavers"], False)], SPIDER_VIEWS),
    "spider_jumpers": ("Jumper marks and models",
                       [("Pounce + Springlegs", ["pounce", "springlegs"], False),
                        ("Big Eyes + Quickbite", ["big_eyes", "quickbite"], False),
                        ("Dart + Bound", ["dart", "bound"], False),
                        ("All 6", SPIDER["jumpers"], False)], SPIDER_VIEWS),
    "spider_fangs": ("Fang marks",
                     [("Venom Bite + Venom Blood", ["venom_bite", "venom_blood"], False),
                      ("Necrotic + Hunger Venom", ["necrotic", "hunger_venom"], False),
                      ("Paralyze + Spit", ["paralyze", "spit"], False),
                      ("All 6", SPIDER["fangs"], False)], SPIDER_VIEWS),
    "spider_swarms": ("Swarm marks and models",
                      [("Clutch + Nest Call", ["clutch", "nest_call"], False),
                       ("Hive Mind + Pack Hunter", ["hive_mind", "pack_hunter"], False),
                       ("Mother + Silk Signal", ["mother", "silk_signal"], False),
                       ("All 6", SPIDER["swarms"], False)], SPIDER_VIEWS),
    "spider_trapdoors": ("Trapdoor marks",
                         [("Digger + Silent Steps", ["digger", "silent_steps"], False),
                          ("Lurking Fangs + Day Hunter", ["lurking_fangs", "day_hunter"], False),
                          ("Shadow Skin + Ceiling Drop", ["shadow_skin", "ceiling_drop"], False),
                          ("All 6", SPIDER["trapdoors"], False)], SPIDER_NIGHT_VIEWS),
}

# The close-ups of the cave spider. It has no babies. It uses the views of
# the spider.
CAVE_SPIDER = pg.CAVE_SPIDER_FAMILIES
CAVE_SPIDER_CLOSEUPS = {
    "cave_spider_toxins": ("Toxin marks",
                           [("Neurotoxin + Toxic Cloud", ["neurotoxin", "toxic_cloud"], False),
                            ("Hemotoxin + Venom Spray", ["hemotoxin", "venom_spray"], False),
                            ("Lingering Venom + Corrosive", ["lingering_venom", "corrosive"], False),
                            ("All 6", CAVE_SPIDER["toxins"], False)], SPIDER_VIEWS),
    "cave_spider_gloomers": ("Gloomer marks",
                             [("Lightsnuffer + Dread", ["lightsnuffer", "dread"], False),
                              ("Echo Sense + Shade Skin", ["echo_sense", "shade_skin"], False),
                              ("Night Eyes + Cave Ghost", ["night_eyes", "cave_ghost"], False),
                              ("All 6", CAVE_SPIDER["gloomers"], False)], SPIDER_NIGHT_VIEWS),
    "cave_spider_scurriers": ("Scurrier marks and models",
                              [("Dash + Ceiling Crawler", ["dash", "ceiling_crawler"], False),
                               ("Juke + Gap Runner", ["juke", "gap_runner"], False),
                               ("Nimble + Frenzy Bite", ["nimble", "frenzy_bite"], False),
                               ("All 6", CAVE_SPIDER["scurriers"], False)], SPIDER_VIEWS),
    "cave_spider_broodkeepers": ("Broodkeeper marks and models",
                                 [("Egg Layer + Brood Call", ["egg_layer", "brood_call"], False),
                                  ("Nest Weaver + Nest Mend", ["nest_weaver", "nest_mend"], False),
                                  ("Swarm Mind + Spawner Bond", ["swarm_mind", "spawner_bond"], False),
                                  ("All 6", CAVE_SPIDER["broodkeepers"], False)], SPIDER_VIEWS),
    "cave_spider_geodes": ("Geode marks and models",
                           [("Amethyst Shell + Ore Skin", ["amethyst_shell", "ore_skin"], False),
                            ("Shatter + Chime", ["shatter", "chime"], False),
                            ("Stone Jaws + Deepslate Plates", ["stone_jaws", "deepslate_plates"], False),
                            ("All 6", CAVE_SPIDER["geodes"], False)], SPIDER_NIGHT_VIEWS),
}

# The close-ups of the pillager. It has no babies.
PILLAGER = pg.PILLAGER_FAMILIES
PILLAGER_NIGHT_VIEWS = [FRONT, BACK, SIDE, THREE_QUARTER, BEHIND, NIGHT_BEHIND]
PILLAGER_CLOSEUPS = {
    "pillager_crossbowmen": ("Crossbowman marks and models",
                             [("Piercer + Overdraw", ["piercer", "overdraw"], False),
                              ("Long Bolt + Splinter", ["long_bolt", "splinter"], False),
                              ("Heavy Bolt + Quick Charge", ["heavy_bolt", "quick_charge"], False),
                              ("All 6", PILLAGER["crossbowmen"], False)], ALL_SIDES),
    "pillager_bombardiers": ("Bombardier marks and models",
                             [("Rocketeer + Flak", ["rocketeer", "flak"], False),
                              ("Powder Keg + Smoke Shell", ["powder_keg", "smoke_shell"], False),
                              ("Incendiary + Blast Proof", ["incendiary", "blast_proof"], False),
                              ("All 6", PILLAGER["bombardiers"], False)], ALL_SIDES),
    "pillager_captains": ("Captain marks and models",
                          [("Banner Bearer + Rally Cry", ["banner_bearer", "rally_cry"], False),
                           ("War Horn + Formation", ["war_horn", "formation"], False),
                           ("Patrol Leader + Grudge", ["patrol_leader", "grudge"], False),
                           ("All 6", PILLAGER["captains"], False)], ALL_SIDES),
    "pillager_scouts": ("Scout marks and models",
                        [("Spyglass + Pathseeker", ["spyglass", "pathseeker"], False),
                         ("Fleet Foot + Evasion", ["fleet_foot", "evasion"], False),
                         ("Camouflage + Signal Smoke", ["camouflage", "signal_smoke"], False),
                         ("All 6", PILLAGER["scouts"], False)], PILLAGER_NIGHT_VIEWS),
    "pillager_looters": ("Looter marks and models",
                         [("Plunder + Greed", ["plunder", "greed"], False),
                          ("Pickpocket + Saboteur", ["pickpocket", "saboteur"], False),
                          ("Hunger Raid + Escape Artist", ["hunger_raid", "escape_artist"], False),
                          ("All 6", PILLAGER["looters"], False)], ALL_SIDES),
}

# The close-ups of the vindicator. It has no babies. The close-ups of
# "vindicator_attacking" show it with its arms apart, as when it attacks, so
# that the gene models on the arms show.
VINDICATOR = pg.VINDICATOR_FAMILIES
VINDICATOR_NIGHT_VIEWS = [FRONT, BACK, SIDE, THREE_QUARTER, BEHIND, NIGHT_BEHIND]
VINDICATOR_CLOSEUPS = {
    "vindicator_butchers": ("Butcher marks and models",
                            [("Gash + Decapitator", ["gash", "decapitator"], False),
                             ("Hatchet + Sunder", ["hatchet", "sunder"], False),
                             ("Heavy Swing + Chop", ["heavy_swing", "chop"], False),
                             ("All 6", VINDICATOR["butchers"], False)], ALL_SIDES),
    "vindicator_berserkers": ("Berserker marks",
                              [("Bloodlust + Death Wish", ["bloodlust", "death_wish"], False),
                               ("Rage + Fury", ["rage", "fury"], False),
                               ("Howl + Thick Blood", ["howl", "thick_blood"], False),
                               ("All 6", VINDICATOR["berserkers"], False)], ALL_SIDES),
    "vindicator_sappers": ("Sapper marks and models",
                           [("Torchbearer + Climber", ["torchbearer", "climber"], False),
                            ("Netter + Battering", ["netter", "battering"], False),
                            ("Door Splitter + Shatterer", ["door_splitter", "shatterer"], False),
                            ("All 6", VINDICATOR["sappers"], False)], VINDICATOR_NIGHT_VIEWS),
    "vindicator_ransackers": ("Ransacker marks and models",
                              [("Village Bane + Ruthless", ["village_bane", "ruthless"], False),
                               ("Looting + Hunting Call", ["looting", "hunting_call"], False),
                               ("Golem Smasher + Pillage Run", ["golem_smasher", "pillage_run"], False),
                               ("All 6", VINDICATOR["ransackers"], False)], ALL_SIDES),
    "vindicator_guardsmen": ("Guardsman marks and models",
                             [("Watchman + Alarm Bell", ["watchman", "alarm_bell"], False),
                              ("Bodyguard + Stand Ground", ["bodyguard", "stand_ground"], False),
                              ("Deflect + Bracer", ["deflect", "bracer"], False),
                              ("All 6", VINDICATOR["guardsmen"], False)], VINDICATOR_NIGHT_VIEWS),
}
VINDICATOR_ATTACK_CLOSEUPS = {
    "vindicator_arms_apart": ("Arm and hand marks with the arms apart, when it attacks",
                              [("Deflect + Bracer", ["deflect", "bracer"], False),
                               ("Heavy Swing + Sunder", ["heavy_swing", "sunder"], False),
                               ("Chop + Fury", ["chop", "fury"], False),
                               ("Door Splitter + Shatterer", ["door_splitter", "shatterer"], False),
                               ("Climber + Golem Smasher", ["climber", "golem_smasher"], False)], ALL_SIDES),
}

# The close-ups of the evoker. It has no babies. The close-ups of
# "evoker_casting" show it with its arms raised, as when it casts, so that
# the gene models on the arms show.
EVOKER = pg.EVOKER_FAMILIES
EVOKER_NIGHT_VIEWS = [FRONT, BACK, SIDE, THREE_QUARTER, BEHIND, NIGHT_BEHIND]
EVOKER_CLOSEUPS = {
    "evoker_fangcallers": ("Fangcaller marks and models",
                           [("Swift Cast + Fang Line", ["swift_cast", "fang_line"], False),
                            ("Golem Jaws + Upheaval", ["golem_jaws", "upheaval"], False),
                            ("Fang Ring + Barbed Fangs", ["fang_ring", "barbed_fangs"], False),
                            ("All 6", EVOKER["fangcallers"], False)], EVOKER_NIGHT_VIEWS),
    "evoker_conjurers": ("Conjurer marks and models",
                         [("Vex Swarm + Vengeful Vexes", ["vex_swarm", "vengeful_vexes"], False),
                          ("Sharp Vexes + Long Bond", ["sharp_vexes", "long_bond"], False),
                          ("Iron Vexes + Venom Vexes", ["iron_vexes", "venom_vexes"], False),
                          ("All 6", EVOKER["conjurers"], False)], ALL_SIDES),
    "evoker_cursers": ("Curser marks and models",
                       [("Blind Hex + Wither Hex", ["blind_hex", "wither_hex"], False),
                        ("Hunger Hex + Drain Hex", ["hunger_hex", "drain_hex"], False),
                        ("Weak Hex + Float Hex", ["weak_hex", "float_hex"], False),
                        ("All 6", EVOKER["cursers"], False)], ALL_SIDES),
    "evoker_lifebinders": ("Lifebinder marks and models",
                           [("Totem Bearer + Soul Mend", ["totem_bearer", "soul_mend"], False),
                            ("Fire Ward + Blast Ward", ["fire_ward", "blast_ward"], False),
                            ("Life Siphon + Arrow Ward", ["life_siphon", "arrow_ward"], False),
                            ("All 6", EVOKER["lifebinders"], False)], EVOKER_NIGHT_VIEWS),
    "evoker_tricksters": ("Trickster marks and models",
                          [("Mind Trick + Blink", ["mind_trick", "blink"], False),
                           ("Smoke Veil + Fleet", ["smoke_veil", "fleet"], False),
                           ("Shimmer + Decoy", ["shimmer", "decoy"], False),
                           ("All 6", EVOKER["tricksters"], False)], ALL_SIDES),
}
EVOKER_CAST_CLOSEUPS = {
    "evoker_arms_raised": ("Arm and hand marks with the arms raised, when it casts",
                           [("Barbed Fangs + Fang Ring", ["barbed_fangs", "fang_ring"], False),
                            ("Decoy + Long Bond", ["decoy", "long_bond"], False),
                            ("Weak Hex + Float Hex", ["weak_hex", "float_hex"], False),
                            ("Life Siphon + Arrow Ward", ["life_siphon", "arrow_ward"], False),
                            ("Iron Vexes + Venom Vexes", ["iron_vexes", "venom_vexes"], False)], ALL_SIDES),
}

# The close-ups of the witch. It has no babies, and its arms are always
# crossed.
WITCH = pg.WITCH_FAMILIES
WITCH_CLOSEUPS = {}

SPECIES = {
    "zombie": (pg.ZOMBIE_PATH, pg.BABY_PATH, CLOSEUPS),
    "husk": (pg.HUSK_PATH, pg.HUSK_BABY_PATH, HUSK_CLOSEUPS),
    "drowned": (pg.DROWNED_PATH, pg.DROWNED_BABY_PATH, DROWNED_CLOSEUPS),
    "skeleton": (pg.SKELETON_PATH, None, SKELETON_CLOSEUPS),
    "stray": (pg.STRAY_PATH, None, STRAY_CLOSEUPS),
    "bogged": (pg.BOGGED_PATH, None, BOGGED_CLOSEUPS),
    "wither_skeleton": (pg.WITHER_SKELETON_PATH, None, WITHER_SKELETON_CLOSEUPS),
    "parched": (pg.PARCHED_PATH, None, PARCHED_CLOSEUPS),
    "creeper": (pg.CREEPER_PATH, None, CREEPER_CLOSEUPS),
    "spider": (pg.SPIDER_PATH, None, SPIDER_CLOSEUPS),
    "cave_spider": (pg.CAVE_SPIDER_PATH, None, CAVE_SPIDER_CLOSEUPS),
    "pillager": (pg.PILLAGER_PATH, None, PILLAGER_CLOSEUPS),
    "vindicator": (pg.VINDICATOR_PATH, None, VINDICATOR_CLOSEUPS),
    "vindicator_attacking": (pg.VINDICATOR_PATH, None, VINDICATOR_ATTACK_CLOSEUPS),
    "evoker": (pg.EVOKER_PATH, None, EVOKER_CLOSEUPS),
    "evoker_casting": (pg.EVOKER_PATH, None, EVOKER_CAST_CLOSEUPS),
    "witch": (pg.WITCH_PATH, None, WITCH_CLOSEUPS),
}


def closeup(title, rows, views, adult_tex, baby_tex):
    scale, (size, center) = pg.SCALE, pg.MOB["closeup_view"]
    cell_w, cell_h = size[0] * scale, size[1] * scale
    pad, top, label_h = 16, 70, 40
    out = Image.new("RGBA", (pad + len(views) * (cell_w + pad), top + len(rows) * (cell_h + label_h + pad)),
                    pg.WHITE)
    draw = ImageDraw.Draw(out)
    pg.text(draw, (out.width / 2, 12), title, 32)
    for r, (name, genes, baby) in enumerate(rows):
        y = top + r * (cell_h + label_h + pad)
        draw.text((pad, y), name, fill=(0, 0, 0, 255), font=pg.ImageFont.load_default(size=20))
        tex = pg.zombie(baby_tex if baby else adult_tex, genes, baby)
        cubes = pg.cubes_of(tex, genes, baby)
        pivots = pg.pivots(baby)
        for c, (view, turn, tilt, light) in enumerate(views):
            x = pad + c * (cell_w + pad)
            if light < 1:
                draw.rectangle((x, y + label_h, x + cell_w, y + label_h + cell_h), fill=NIGHT)
            im = pg.render3d.render(cubes, pivots, turn, tilt, scale, size, center=center, light=light)
            out.alpha_composite(im, (x, y + label_h))
            color = (255, 255, 255, 255) if light < 1 else (0, 0, 0, 255)
            font = pg.ImageFont.load_default(size=18)
            w = draw.textlength(view, font=font)
            draw.text((x + cell_w / 2 - w / 2, y + label_h + cell_h - 26), view, fill=color, font=font)
    return out


def main(*species):
    os.makedirs(OUT_DIR, exist_ok=True)
    for kind in species or SPECIES:
        adult_path, baby_path, closeups = SPECIES[kind]
        pg.set_mob(kind)
        adult = pg.find_zombie_texture(adult_path)
        baby = pg.find_zombie_texture(baby_path) if baby_path else None
        for file_name, (title, rows, views) in closeups.items():
            path = os.path.join(OUT_DIR, file_name + ".png")
            closeup(title, rows, views, adult, baby).save(path)
            print(path)


if __name__ == "__main__":
    main(*sys.argv[1:])
