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
                         ("Antivenom + Plague Carrier", ["antivenom", "plague_carrier"], False),
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
}

# Each species: its texture, its baby texture, and its close-ups.
SPECIES = {
    "zombie": (pg.ZOMBIE_PATH, pg.BABY_PATH, CLOSEUPS),
    "husk": (pg.HUSK_PATH, pg.HUSK_BABY_PATH, HUSK_CLOSEUPS),
    "drowned": (pg.DROWNED_PATH, pg.DROWNED_BABY_PATH, DROWNED_CLOSEUPS),
    "skeleton": (pg.SKELETON_PATH, None, SKELETON_CLOSEUPS),
    "stray": (pg.STRAY_PATH, None, STRAY_CLOSEUPS),
    "bogged": (pg.BOGGED_PATH, None, BOGGED_CLOSEUPS),
}


def closeup(title, rows, views, adult_tex, baby_tex):
    scale, size = pg.SCALE, (26, 40)
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
            im = pg.render3d.render(cubes, pivots, turn, tilt, scale, size, center=(0, 5), light=light)
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
