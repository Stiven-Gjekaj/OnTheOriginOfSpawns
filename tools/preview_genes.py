"""Make preview images of the gene visuals of the zombie or the husk.

Usage: python3 tools/preview_genes.py [zombie | husk | drowned]

The script reads the vanilla texture of the species from the Minecraft jar in
the Gradle cache of Loom. The default species is the zombie. It reads the visual of each gene from
assets/originofspawns/gene_visuals/, applies the layers with the region map,
and adds the cubes of the gene model from assets/originofspawns/gene_models/.
It writes PNG files on a white background. Git ignores build/:
- build/previews/genes/<species>_<gene>.png: the front and the back of one gene.
- build/previews/families/<family>.png: a plain zombie, each gene of the
  family, and one zombie with all the genes of the family. The front views
  are on top, and the back views are under them.
Each preview uses the vanilla texture. Do not commit a preview.
This script needs Pillow.

The effects here copy TextureBaker in the mod, so that the previews show the
same pixels as the game. If TextureBaker changes, change this script too.
tools/render3d.py draws the views.
"""
import glob
import io
import json
import os
import sys
import zipfile

from PIL import Image, ImageDraw, ImageFont

import render3d

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESOURCES = os.path.join(ROOT, "src/main/resources")
ASSETS = os.path.join(RESOURCES, "assets")
VISUAL_DIR = os.path.join(ASSETS, "originofspawns/gene_visuals")
MODEL_DIR = os.path.join(ASSETS, "originofspawns/gene_models")
GENE_OUT = os.path.join(ROOT, "build/previews/genes")
FAMILY_OUT = os.path.join(ROOT, "build/previews/families")
ZOMBIE_PATH = "assets/minecraft/textures/entity/zombie/zombie.png"
BABY_PATH = "assets/minecraft/textures/entity/zombie/zombie_baby.png"
HUSK_PATH = "assets/minecraft/textures/entity/zombie/husk.png"
HUSK_BABY_PATH = "assets/minecraft/textures/entity/zombie/husk_baby.png"
DROWNED_PATH = "assets/minecraft/textures/entity/zombie/drowned.png"
DROWNED_BABY_PATH = "assets/minecraft/textures/entity/zombie/drowned_baby.png"
DROWNED_OUTER_PATH = "assets/minecraft/textures/entity/zombie/drowned_outer_layer.png"
DROWNED_OUTER_BABY_PATH = "assets/minecraft/textures/entity/zombie/drowned_outer_layer_baby.png"
DEFAULT_REGION_MAP = "originofspawns:textures/region/humanoid.png"

REGIONS = {"face": 1, "scalp": 2, "torso": 3, "arms": 4, "hands": 5, "legs": 6,
           "lower_legs": 7, "feet": 8}
MATERIALS = {"skin": 1, "cloth": 2, "eyes": 3}

# The families of the zombie, from design/species/zombie.md.
FAMILIES = {
    "predators": ["bonerunner", "lunger", "frenzy", "bloodscent", "rabid", "death_throes"],
    "stalkers": ["cinder", "hush", "shade", "burrower", "lurker", "nightborn"],
    "tanks": ["ironhide", "thickskull", "bloat", "stoneblood", "blastproof", "mender"],
    "hordes": ["packcaller", "brood", "rally", "runt", "grave_call", "siege"],
    "blights": ["rotgut", "contagion", "sporebearer", "leech", "miasma", "plague_carrier"],
}

# More zombies for a family sheet: a label, the genes, True to add all the
# genes of the family, and the light (1 is day). A night column has a dark
# background. "opacity" draws the zombie translucent, as Shade at night.
EXTRA = {
    "predators": [("Cinder + all 6", ["cinder"], True, {}),
                  ("Ironhide + D. Throes", ["ironhide", "death_throes"], False, {}),
                  ("Ironhide + all 6", ["ironhide"], True, {})],
    "stalkers": [("All 6 at night", [], True, {"light": 0.18}),
                 ("Shade at 10%", ["shade"], False, {"opacity": 0.1}),
                 ("Burrower + Bonerunner", ["burrower", "bonerunner"], False, {}),
                 ("Lurker + Nightborn", ["lurker", "nightborn"], False, {}),
                 ("Ironhide + all 6", ["ironhide"], True, {})],
    "tanks": [("Stoneblood + Bonerunner", ["stoneblood", "bonerunner"], False, {}),
              ("Thickskull + Lurker", ["thickskull", "lurker"], False, {}),
              ("Tanks + Burrower", ["burrower"], True, {}),
              ("Bloat + others", ["bloat", "ironhide", "frenzy", "bloodscent", "death_throes",
                                  "thickskull"], False, {}),
              ("Thickskull + face", ["thickskull", "rabid", "nightborn"], False, {})],
    "blights": [("Blights + Lurker", ["lurker", "thickskull"], True, {}),
                ("Blights + Nightborn", ["nightborn", "cinder", "siege"], True, {}),
                ("Blights + Ironhide", ["ironhide", "rabid", "hush"], True, {}),
                ("Blights baby", [], True, {"baby": True})],
    "hordes": [("Runt baby", ["runt"], False, {"baby": True}),
               ("Plain baby", [], False, {"baby": True}),
               ("Hordes + Lurker", ["lurker", "thickskull"], True, {}),
               ("Hordes + Cinder", ["cinder", "bloat"], True, {}),
               ("Hordes + Hush", ["hush", "rabid"], True, {})],
}
# The families of the husk, from design/species/husk.md.
HUSK_FAMILIES = {
    "scorchers": ["sunbaked", "heatshimmer", "kilnskin", "sunstroke", "mirage", "solar_flare"],
    "dunecrawlers": ["sandswimmer", "dune_runner", "sinkhole", "scuttler", "sandblast", "duststorm"],
    "mummies": ["wrapped", "cursetouch", "undying", "embalmed", "tomb_guard", "grave_dust"],
    "caravans": ["packmule", "linked", "signal_fire", "sandling", "nomad", "waterskin"],
    "parchers": ["dehydrator", "thirstbite", "desiccate", "salt_crust", "mirage_water", "dust_lung"],
}

HUSK_EXTRA = {
    "scorchers": [("Sunbaked + Kilnskin", ["sunbaked", "kilnskin"], False, {}),
                  ("Sunstroke + Mirage", ["sunstroke", "mirage"], False, {}),
                  ("All 6 at night", [], True, {"light": 0.18}),
                  ("All 6 baby", [], True, {"baby": True})],
    "dunecrawlers": [("All 6 + Heatshimmer", ["heatshimmer"], True, {}),
                     ("All 6 + Sunbaked, Sunstroke", ["sunbaked", "sunstroke"], True, {}),
                     ("All 6 + Kilnskin", ["kilnskin"], True, {}),
                     ("All 6 baby", [], True, {"baby": True})],
    "mummies": [("Wrapped + Embalmed", ["wrapped", "embalmed"], False, {}),
                ("Kilnskin + Embalmed", ["kilnskin", "embalmed"], False, {}),
                ("All 6 + Sunbaked", ["sunbaked"], True, {}),
                ("All 6 + Solar Flare", ["solar_flare", "kilnskin"], True, {}),
                ("All 6 + Duststorm", ["duststorm", "scuttler"], True, {}),
                ("All 6 baby", [], True, {"baby": True})],
    "caravans": [("Sandling baby", ["sandling"], False, {"baby": True}),
                 ("All 6 baby", [], True, {"baby": True}),
                 ("All 6 + Tomb Guard", ["tomb_guard", "wrapped"], True, {}),
                 ("All 6 + Solar Flare", ["solar_flare", "sinkhole"], True, {}),
                 ("All 6 at night", [], True, {"light": 0.18})],
    "parchers": [("Dehydrator + Sandblast", ["dehydrator", "sandblast", "thirstbite"], False, {}),
                 ("Mirage Water + Dune Runner", ["mirage_water", "dune_runner"], False, {}),
                 ("All 6 + Wrapped", ["wrapped", "embalmed"], True, {}),
                 ("All 6 + Solar Flare", ["solar_flare", "packmule", "nomad"], True, {}),
                 ("All 6 baby", [], True, {"baby": True})],
}
# The families of the drowned, from design/species/drowned.md.
DROWNED_FAMILIES = {
    "riptides": ["undertow", "torrent", "surge", "capsizer", "breacher", "gills"],
    "harpooners": ["tridentborn", "barbed", "volley", "scavenger", "spearfisher", "stormcaller"],
    "abyssals": ["lure", "pressure_hide", "blind_hunter", "ink", "deepcold", "glowspot"],
    "barnacled": ["barnacle_crust", "coral_growth", "shellback", "anchor", "pearlheart", "kelpwrapped"],
    "sirens": ["siren_song", "drag_under", "fry", "shoal", "chorus", "wreckcaller"],
}

DROWNED_EXTRA = {
    "riptides": [("All 6 + Cinder", ["cinder"], True, {}),
                 ("All 6 + Ironhide", ["ironhide"], True, {}),
                 ("All 6 baby", [], True, {"baby": True})],
}
NIGHT = (18, 22, 38, 255)

# The babies of BabyGenesClientTest, for a sheet to compare with the game.
BABIES = [
    ("Plain baby", [], True),
    ("Runt, Cinder, Ironhide", ["runt", "cinder", "ironhide"], True),
    ("Lurker, Thickskull", ["lurker", "thickskull"], True),
    ("Bloat, Frenzy", ["bloat", "frenzy"], True),
    ("D. Throes, Bonerunner, Burrower", ["death_throes", "bonerunner", "burrower"], True),
    ("Adult: Runt, Cinder, Ironhide", ["runt", "cinder", "ironhide"], False),
]

SCALE = 8
WHITE = (255, 255, 255, 255)

# The bones of the standing zombie: the pivot of each bone, in model pixels.
# The y axis goes down, as in the vanilla models.
PIVOTS = {
    "head": (0, 0, 0), "body": (0, 0, 0), "right_arm": (-5, 2, 0), "left_arm": (5, 2, 0),
    "right_leg": (-1.9, 12, 0), "left_leg": (1.9, 12, 0),
}

# The cubes of the vanilla zombie: bone, uv, from, size, mirror.
ZOMBIE_CUBES = [
    ("head", (0, 0), (-4, -8, -4), (8, 8, 8), False),
    ("body", (16, 16), (-4, 0, -2), (8, 12, 4), False),
    ("right_arm", (40, 16), (-3, -2, -2), (4, 12, 4), False),
    ("left_arm", (40, 16), (-1, -2, -2), (4, 12, 4), True),
    ("right_leg", (0, 16), (-2, 0, -2), (4, 12, 4), False),
    ("left_leg", (0, 16), (-2, 0, -2), (4, 12, 4), True),
]

# The cubes of the drowned: the left limbs have their own UV.
DROWNED_CUBES = ZOMBIE_CUBES[:2] + [
    ("right_arm", (40, 16), (-3, -2, -2), (4, 12, 4), False),
    ("left_arm", (32, 48), (-1, -2, -2), (4, 12, 4), False),
    ("right_leg", (0, 16), (-2, 0, -2), (4, 12, 4), False),
    ("left_leg", (16, 48), (-2, 0, -2), (4, 12, 4), False),
]

# The baby zombie has its own model (BabyZombieModel) and its own texture
# layout. The feet are at y 24, as for the adult.
BABY_PIVOTS = {
    "head": (0, 15.25, 0), "body": (0, 17.5, 0), "right_arm": (-3, 15.5, 0), "left_arm": (3, 15.5, 0),
    "right_leg": (-1, 20, 0), "left_leg": (1, 20, 0),
}
BABY_CUBES = [
    ("head", (3, 3), (-3, -6.25, -3), (6, 6, 6), False),
    ("body", (16, 16), (-2, -2.5, -1), (4, 5, 2), False),
    ("right_arm", (36, 16), (-1, -0.5, -1), (2, 5, 2), False),
    ("left_arm", (28, 16), (-1, -0.5, -1), (2, 5, 2), False),
    ("right_leg", (8, 16), (-1, 0, -1), (2, 4, 2), False),
    ("left_leg", (0, 16), (-1, 0, -1), (2, 4, 2), False),
]

# The area that a view shows, in model pixels: x from -VIEW_X, y from -VIEW_Y.
VIEW_X, VIEW_Y, VIEW_W, VIEW_H = 11, 11, 22, 37


def find_zombie_texture(path=ZOMBIE_PATH):
    """Return a vanilla zombie texture from a Minecraft jar of Loom."""
    caches = os.path.expanduser("~/.gradle/caches/fabric-loom")
    jars = sorted(glob.glob(os.path.join(caches, "**", "*.jar"), recursive=True))
    # The client jar is the most likely place. Look at it first.
    jars.sort(key=lambda p: "client" not in os.path.basename(p))
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as z:
                if path in z.namelist():
                    print("vanilla texture from", jar)
                    return Image.open(io.BytesIO(z.read(path))).convert("RGBA")
        except zipfile.BadZipFile:
            continue
    raise SystemExit("No Minecraft jar with the texture. Run ./gradlew build first.")


def int32(n):
    n &= 0xFFFFFFFF
    return n - (1 << 32) if n & 0x80000000 else n


def java_string_hash(s):
    h = 0
    for c in s:
        h = int32(31 * h + ord(c))
    return h


def identifier_hash(identifier):
    """Return the Java hashCode of an Identifier, which the game uses as the seed."""
    namespace, path = identifier.split(":")
    return int32(31 * java_string_hash(namespace) + java_string_hash(path))


def spot_hash(x, y, seed):
    """Copy TextureBaker.hash, with Java int overflow."""
    h = seed
    h = int32(h * 31 + x)
    h = int32(h * 31 + y)
    h = int32(h ^ ((h & 0xFFFFFFFF) >> 16))
    h = int32(h * int32(0x85EBCA6B))
    h = int32(h ^ ((h & 0xFFFFFFFF) >> 13))
    h = int32(h * int32(0xC2B2AE35))
    h = int32(h ^ ((h & 0xFFFFFFFF) >> 16))
    return h


def rgb(hex_color):
    return tuple(int(hex_color[i:i + 2], 16) for i in (1, 3, 5))


def asset_file(identifier):
    namespace, path = identifier.split(":")
    return os.path.join(ASSETS, namespace, path)


IMAGES = {}


def image(identifier):
    if identifier not in IMAGES:
        IMAGES[identifier] = Image.open(asset_file(identifier)).convert("RGBA")
    return IMAGES[identifier]


# BabyLayout in the mod: each cube of the adult model, and the same cube of
# the baby model. Each is (u, v, width, height, depth, x, y, z): the box UV
# corner, the size, and the corner of the cube in its bone. The last value is
# True where the adult cube mirrors its texture and the baby cube does not.
BABY_PAIRS = [
    ("head", (0, 0, 8, 8, 8, -4, -8, -4), (3, 3, 6, 6, 6, -3, -6.25, -3), False),
    (None, (32, 0, 8, 8, 8, -4, -8, -4), (35, 3, 6, 6, 6, -3, -6.15, -3), False),
    ("body", (16, 16, 8, 12, 4, -4, 0, -2), (16, 16, 4, 5, 2, -2, -2.5, -1), False),
    ("right_arm", (40, 16, 4, 12, 4, -3, -2, -2), (36, 16, 2, 5, 2, -1, -0.5, -1), False),
    ("left_arm", (40, 16, 4, 12, 4, -1, -2, -2), (28, 16, 2, 5, 2, -1, -0.5, -1), True),
    ("right_leg", (0, 16, 4, 12, 4, -2, 0, -2), (8, 16, 2, 4, 2, -1, 0, -1), False),
    ("left_leg", (0, 16, 4, 12, 4, -2, 0, -2), (0, 16, 2, 4, 2, -1, 0, -1), True),
]
# The faces that change places in a mirrored cube: side 1 and side 2.
MIRRORED_FACE = (0, 1, 4, 3, 2, 5)


def cube_faces(c):
    """Return the box UV faces of a cube: top, bottom, side 1, front, side 2, back."""
    u, v, w, h, d = c[:5]
    return [(u + d, v, w, d), (u + d + w, v, w, d), (u, v + d, d, h), (u + d, v + d, w, h),
            (u + d + w, v + d, d, h), (u + 2 * d + w, v + d, w, h)]


def copy_face(source, src, target, dst, scale, flip):
    """Copy one face to a face of another size, with the nearest pixel, like BabyLayout.copyFace."""
    to_w, to_h = dst[2] * scale, dst[3] * scale
    from_w, from_h = src[2] * scale, src[3] * scale
    for y in range(to_h):
        for x in range(to_w):
            fx = to_w - 1 - x if flip else x
            sx = src[0] * scale + int((fx + 0.5) * from_w / to_w)
            sy = src[1] * scale + int((y + 0.5) * from_h / to_h)
            tx, ty = dst[0] * scale + x, dst[1] * scale + y
            if sx < source.width and sy < source.height and tx < target.width and ty < target.height:
                target.putpixel((tx, ty), source.getpixel((sx, sy)))


def to_baby(adult):
    """Move an image in the adult layout to the baby layout, like BabyLayout.toBaby."""
    scale = max(adult.width // 64, 1)
    out = Image.new("RGBA", adult.size, (0, 0, 0, 0))
    for _, a, b, mirror in BABY_PAIRS:
        adult_faces, baby_faces = cube_faces(a), cube_faces(b)
        for face in range(6):
            src = adult_faces[MIRRORED_FACE[face] if mirror else face]
            copy_face(adult, src, out, baby_faces[face], scale, mirror)
    return out


def to_adult(baby):
    """Move an image in the baby layout to the adult layout, like BabyLayout.toAdult."""
    scale = max(baby.width // 64, 1)
    out = Image.new("RGBA", baby.size, (0, 0, 0, 0))
    for _, a, b, mirror in BABY_PAIRS:
        if mirror:
            continue
        for src, dst in zip(cube_faces(b), cube_faces(a)):
            copy_face(baby, src, out, dst, scale, False)
    return out


# BabyLayout.SPLIT_LIMBS in the mod: the right limb of the zombie layout, and
# the left limb of a model with split limbs, such as the drowned.
SPLIT_PAIRS = [
    ((40, 16, 4, 12, 4), (32, 48, 4, 12, 4)),
    ((0, 16, 4, 12, 4), (16, 48, 4, 12, 4)),
]


def to_split_limbs(image_in):
    """Copy the right limbs, mirrored, to the left limbs, like BabyLayout.toSplitLimbs."""
    scale = max(image_in.width // 64, 1)
    out = image_in.copy()
    for a, b in SPLIT_PAIRS:
        from_faces, to_faces = cube_faces(a), cube_faces(b)
        for face in range(6):
            copy_face(image_in, from_faces[MIRRORED_FACE[face]], out, to_faces[face], scale, True)
    return out


SPLIT_IMAGES = {}


def split_image(identifier):
    if identifier not in SPLIT_IMAGES:
        SPLIT_IMAGES[identifier] = to_split_limbs(image(identifier))
    return SPLIT_IMAGES[identifier]


# The look of the current species: True for split limbs, and the outer layer
# textures (adult, baby) of the drowned, or None. main() sets it.
MOB = {"split": False, "outer": None}

BABY_IMAGES = {}


def baby_image(identifier):
    if identifier not in BABY_IMAGES:
        BABY_IMAGES[identifier] = to_baby(image(identifier))
    return BABY_IMAGES[identifier]


def bone_change(bone):
    """Return (move, scale, adult) for a bone, like BabyLayout.boneChange."""
    for name, a, b, _ in BABY_PAIRS:
        if name == bone:
            return b[5:8], (b[2] / a[2], b[3] / a[3], b[4] / a[4]), a[5:8]
    raise ValueError(bone)


def desaturate(color, amount, darken):
    r, g, b, a = color
    gray = (r * 299 + g * 587 + b * 114) // 1000
    keep = 1.0 - darken
    return tuple(int((c + (gray - c) * amount) * keep) for c in (r, g, b)) + (a,)


def apply(effect, color, x, y, seed, baby=False):
    kind = effect["type"]
    if kind == "desaturate":
        return desaturate(color, effect["amount"], effect.get("darken", 0.0))
    if kind == "spots":
        h = spot_hash(x, y, seed)
        if (h & 0xFFFF) >= effect["density"] * 0x10000:
            return color
        spot = rgb(effect["colors"][((h & 0xFFFFFFFF) >> 16 & 0x7FFF) % len(effect["colors"])])
        return (*spot, color[3])
    if kind == "pattern":
        pattern = image(effect["texture"])
        if baby and pattern.width >= 64 and pattern.height >= 64:
            # A whole-texture mark moves to the baby layout. A tile stays.
            pattern = baby_image(effect["texture"])
        elif MOB["split"] and pattern.width >= 64 and pattern.height >= 64:
            pattern = split_image(effect["texture"])
        tint = rgb(effect.get("tint", "#FFFFFF"))
        stamp = pattern.getpixel((x % pattern.width, y % pattern.height))
        strength = stamp[3] / 255
        shade = stamp[0] / 255
        return tuple(int(color[i] + (tint[i] * shade - color[i]) * strength) for i in range(3)) + (color[3],)
    if kind == "erase":
        return (0, 0, 0, 0)
    raise ValueError("unknown effect " + kind)


def visual(gene):
    path = os.path.join(VISUAL_DIR, gene + ".json")
    if not os.path.exists(path):
        return None
    with open(path) as fh:
        return json.load(fh)


def bake_layer(out, gene, layer, baby=False):
    """Apply one visual layer of a gene to the texture, in place, like TextureBaker.bake.

    For a baby, the region map and the whole-texture marks move to the baby layout.
    """
    vis = visual(gene)
    region_map = vis.get("region_map", DEFAULT_REGION_MAP)
    regions = (baby_image(region_map) if baby else split_image(region_map) if MOB["split"]
               else image(region_map))
    seed = identifier_hash("originofspawns:" + gene)
    wanted_regions = {REGIONS[r] for r in layer.get("regions", [])}
    wanted_materials = {MATERIALS[m] for m in layer.get("materials", [])}
    for y in range(out.height):
        for x in range(out.width):
            region, material, _, alpha = regions.getpixel((x * regions.width // out.width,
                                                           y * regions.height // out.height))
            if not alpha:
                continue
            if wanted_regions and region not in wanted_regions:
                continue
            if wanted_materials and material not in wanted_materials:
                continue
            out.putpixel((x, y), apply(layer["effect"], out.getpixel((x, y)), x, y, seed, baby))


def gene_cubes(gene, mob_tex=None, baby=False):
    """Return the cubes of the gene model of a gene, or an empty list.

    A model with "mob_texture" uses mob_tex, the baked texture of the mob. On a
    baby, it gets that texture moved back to the adult layout, and each part
    moves from the adult bone to the baby bone, like GeneModelLayer.
    """
    vis = visual(gene)
    if vis is None or "model" not in vis:
        return []
    namespace, name = vis["model"].split(":")
    with open(os.path.join(ASSETS, namespace, "gene_models", name + ".json")) as fh:
        model = json.load(fh)
    if model.get("mob_texture"):
        tex = to_adult(mob_tex) if baby else mob_tex
        scale = 1.0
    else:
        tex = image(model["texture"])
        scale = tex.width / model.get("texture_width", 64)
    cubes = []
    for part in model["parts"]:
        cubes.append({
            "bone": part["bone"], "uv": part["uv"], "from": part["from"], "size": part["size"],
            "inflate": part.get("inflate", 0.0), "mirror": part.get("mirror", False),
            "texture": tex, "scale": scale, "pivot": part.get("pivot", (0, 0, 0)),
            "rotation": part.get("rotation", (0, 0, 0)), "glow": part.get("glow", False),
            "bone_change": bone_change(part["bone"]) if baby else None,
        })
    return cubes


def cubes_of(tex, genes, baby=False):
    """Return the cubes of the zombie and of the gene models of the genes.

    The drowned has split limbs, and an outer layer: the same cubes, 0.25
    pixel larger, with the outer layer texture. The genes put their marks on
    the outer layer too, as DrownedOuterLayerMixin does.
    """
    body = BABY_CUBES if baby else DROWNED_CUBES if MOB["split"] else ZOMBIE_CUBES
    cubes = [{"bone": b, "uv": uv, "from": f, "size": s, "inflate": 0.0, "mirror": m,
              "texture": tex, "scale": 1.0} for b, uv, f, s, m in body]
    if MOB["outer"]:
        outer = zombie(MOB["outer"][1 if baby else 0], genes, baby)
        cubes += [{"bone": b, "uv": uv, "from": f, "size": s, "inflate": 0.25, "mirror": m,
                   "texture": outer, "scale": 1.0} for b, uv, f, s, m in body]
    for gene in genes:
        cubes += gene_cubes(gene, tex, baby)
    return cubes


def view(tex, genes, back, turn=None, tilt=0, light=1.0, opacity=1.0, baby=False):
    """Draw the zombie with the gene models: the front, the back, or any turn.

    With baby, it draws the baby model. The gene models then sit on the bones
    of the baby, as in the game.
    """
    if turn is None:
        turn = 180 if back else 0
    return render3d.render(cubes_of(tex, genes, baby), BABY_PIVOTS if baby else PIVOTS, turn, tilt,
                           SCALE, (VIEW_W, VIEW_H), center=(0, VIEW_H / 2 - VIEW_Y), light=light,
                           opacity=opacity)


def text(draw, xy, label, size, max_width=None):
    """Draw a label centered on xy. With max_width, a long label gets a smaller font."""
    font = ImageFont.load_default(size=size)
    while max_width and size > 8 and draw.textlength(label, font=font) > max_width:
        size -= 1
        font = ImageFont.load_default(size=size)
    w = draw.textlength(label, font=font)
    draw.text((xy[0] - w / 2, xy[1]), label, fill=(0, 0, 0, 255), font=font)


def label(gene):
    return gene.replace("_", " ").title()


def zombie(tex, genes, baby=False):
    """Return the baked texture of a zombie with the genes, like TextureBaker.bakeAll.

    The layers of all the genes go on in the order of their "order" field. The
    sort is stable: layers with the same order keep the order of the genes, by
    id, and the order in their recipe.
    """
    steps = []
    for gene in sorted(genes):
        vis = visual(gene)
        if vis is not None:
            steps += [(layer.get("order", 0), gene, layer) for layer in vis.get("layers", [])]
    steps.sort(key=lambda step: step[0])
    out = tex.copy()
    for _, gene, layer in steps:
        bake_layer(out, gene, layer, baby)
    return out


def gene_image(name, tex, genes):
    """Return one image with the front and the back view, and a label."""
    gap, top, foot = 2 * SCALE, 6 * SCALE, 4 * SCALE
    w, h = VIEW_W * SCALE, VIEW_H * SCALE
    im = Image.new("RGBA", (2 * w + 3 * gap, h + top + foot), WHITE)
    draw = ImageDraw.Draw(im)
    text(draw, (im.width / 2, SCALE), name, 3 * SCALE)
    for i, back in enumerate((False, True)):
        x = gap + i * (w + gap)
        im.alpha_composite(view(tex, genes, back), (x, top))
        text(draw, (x + w / 2, top + h + SCALE), "back" if back else "front", 2 * SCALE)
    return im


def sheet(title, columns):
    """Return a row of zombies: the front views on top, the back views under
    them, and a 3/4 view from the front right at the bottom."""
    gap, top = 2 * SCALE, 11 * SCALE
    w, h = VIEW_W * SCALE, VIEW_H * SCALE
    im = Image.new("RGBA", (len(columns) * (w + gap) + gap, top + 3 * h + 4 * gap), WHITE)
    draw = ImageDraw.Draw(im)
    text(draw, (im.width / 2, SCALE), title, 4 * SCALE)
    for i, (name, tex, genes, options) in enumerate(columns):
        x = gap + i * (w + gap)
        text(draw, (x + w / 2, 6 * SCALE), name, 2 * SCALE + 4, max_width=w)
        if options.get("light", 1.0) < 1:
            for y in (top, top + h + gap, top + 2 * (h + gap)):
                draw.rectangle((x, y, x + w - 1, y + h - 1), fill=NIGHT)
        im.alpha_composite(view(tex, genes, False, **options), (x, top))
        im.alpha_composite(view(tex, genes, True, **options), (x, top + h + gap))
        im.alpha_composite(view(tex, genes, False, turn=-35, tilt=15, **options),
                           (x, top + 2 * (h + gap)))
    return im


# Each species: the texture, the baby texture, the families, the extra
# columns, and the babies sheet.
SPECIES = {
    "zombie": (ZOMBIE_PATH, BABY_PATH, FAMILIES, EXTRA, BABIES),
    "husk": (HUSK_PATH, HUSK_BABY_PATH, HUSK_FAMILIES, HUSK_EXTRA, []),
    "drowned": (DROWNED_PATH, DROWNED_BABY_PATH, DROWNED_FAMILIES, DROWNED_EXTRA, []),
}


def set_mob(species):
    """Set the look of the species for the bake and the cubes."""
    MOB["split"] = species == "drowned"
    MOB["outer"] = ((find_zombie_texture(DROWNED_OUTER_PATH), find_zombie_texture(DROWNED_OUTER_BABY_PATH))
                    if species == "drowned" else None)


def main(species="zombie"):
    os.makedirs(GENE_OUT, exist_ok=True)
    os.makedirs(FAMILY_OUT, exist_ok=True)
    adult_path, baby_path, families, extras, babies = SPECIES[species]
    set_mob(species)
    tex = find_zombie_texture(adult_path)
    baby_tex = find_zombie_texture(baby_path)
    for family, genes in families.items():
        ready = [g for g in genes if visual(g) is not None]
        missing = [g for g in genes if g not in ready]
        if missing:
            print(family + ": no visual yet for " + ", ".join(missing))
        if not ready:
            continue
        columns = [("Plain", tex, [], {})]
        for gene in ready:
            baked = zombie(tex, [gene])
            columns.append((label(gene), baked, [gene], {}))
            path = os.path.join(GENE_OUT, species + "_" + gene + ".png")
            gene_image(label(gene), baked, [gene]).save(path)
        if len(ready) > 1:
            columns.append(("All " + str(len(ready)), zombie(tex, ready), ready, {}))
        for name, extra, with_family, options in extras.get(family, []):
            genes_here = extra + (ready if with_family else [])
            baby = options.get("baby", False)
            columns.append((name, zombie(baby_tex if baby else tex, genes_here, baby), genes_here, options))
        path = os.path.join(FAMILY_OUT, family + ".png")
        sheet(family.title(), columns).save(path)
        print(path)
    if not babies:
        return
    columns = [(name, zombie(baby_tex if baby else tex, genes, baby), genes, {"baby": baby})
               for name, genes, baby in babies]
    path = os.path.join(FAMILY_OUT, "babies.png")
    sheet("Babies", columns).save(path)
    print(path)


if __name__ == "__main__":
    main(*sys.argv[1:2])
