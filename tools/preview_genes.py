"""Make preview images of the zombie gene visuals.

Usage: python3 tools/preview_genes.py

The script reads the vanilla zombie texture from the Minecraft jar in the
Gradle cache of Loom. It reads the visual of each gene from
assets/originofspawns/gene_visuals/, applies the layers with the region map,
and adds the cubes of the gene model from assets/originofspawns/gene_models/.
It writes PNG files on a white background. Git ignores build/:
- build/previews/genes/zombie_<gene>.png: the front and the back of one gene.
- build/previews/families/<family>.png: a plain zombie, each gene of the
  family, and one zombie with all the genes of the family. The front views
  are on top, and the back views are under them.
Each preview uses the vanilla texture. Do not commit a preview.
This script needs Pillow.

The effects here copy TextureBaker in the mod, so that the previews show the
same pixels as the game. If TextureBaker changes, change this script too.
The views are flat: each cube shows only its front face or its back face.
"""
import glob
import io
import json
import os
import zipfile

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESOURCES = os.path.join(ROOT, "src/main/resources")
ASSETS = os.path.join(RESOURCES, "assets")
VISUAL_DIR = os.path.join(ASSETS, "originofspawns/gene_visuals")
MODEL_DIR = os.path.join(ASSETS, "originofspawns/gene_models")
GENE_OUT = os.path.join(ROOT, "build/previews/genes")
FAMILY_OUT = os.path.join(ROOT, "build/previews/families")
ZOMBIE_PATH = "assets/minecraft/textures/entity/zombie/zombie.png"
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

# More zombies for a family sheet: a label and the genes before the family.
EXTRA = {
    "predators": [("Cinder + all 6", ["cinder"])],
}

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

# The area that a view shows, in model pixels: x from -VIEW_X, y from -VIEW_Y.
VIEW_X, VIEW_Y, VIEW_W, VIEW_H = 11, 11, 22, 37


def find_zombie_texture():
    """Return the vanilla zombie texture from a Minecraft jar of Loom."""
    caches = os.path.expanduser("~/.gradle/caches/fabric-loom")
    jars = sorted(glob.glob(os.path.join(caches, "**", "*.jar"), recursive=True))
    # The client jar is the most likely place. Look at it first.
    jars.sort(key=lambda p: "client" not in os.path.basename(p))
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as z:
                if ZOMBIE_PATH in z.namelist():
                    print("vanilla texture from", jar)
                    return Image.open(io.BytesIO(z.read(ZOMBIE_PATH))).convert("RGBA")
        except zipfile.BadZipFile:
            continue
    raise SystemExit("No Minecraft jar with the zombie texture. Run ./gradlew build first.")


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


def desaturate(color, amount, darken):
    r, g, b, a = color
    gray = (r * 299 + g * 587 + b * 114) // 1000
    keep = 1.0 - darken
    return tuple(int((c + (gray - c) * amount) * keep) for c in (r, g, b)) + (a,)


def apply(effect, color, x, y, seed):
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


def bake(tex, gene):
    """Apply the visual layers of one gene, like TextureBaker.bake."""
    vis = visual(gene)
    if vis is None:
        return tex
    regions = image(vis.get("region_map", DEFAULT_REGION_MAP))
    layers = vis.get("layers", [])
    seed = identifier_hash("originofspawns:" + gene)
    out = tex.copy()
    for y in range(out.height):
        for x in range(out.width):
            region, material, _, alpha = regions.getpixel((x * regions.width // out.width,
                                                           y * regions.height // out.height))
            if not alpha:
                continue
            for layer in layers:
                wanted_regions = {REGIONS[r] for r in layer.get("regions", [])}
                wanted_materials = {MATERIALS[m] for m in layer.get("materials", [])}
                if wanted_regions and region not in wanted_regions:
                    continue
                if wanted_materials and material not in wanted_materials:
                    continue
                out.putpixel((x, y), apply(layer["effect"], out.getpixel((x, y)), x, y, seed))
    return out


def gene_cubes(gene):
    """Return the cubes of the gene model of a gene, or an empty list."""
    vis = visual(gene)
    if vis is None or "model" not in vis:
        return []
    namespace, name = vis["model"].split(":")
    with open(os.path.join(ASSETS, namespace, "gene_models", name + ".json")) as fh:
        model = json.load(fh)
    tex = image(model["texture"])
    scale = tex.width / model.get("texture_width", 64)
    cubes = []
    for part in model["parts"]:
        cubes.append({
            "bone": part["bone"], "uv": part["uv"], "from": part["from"], "size": part["size"],
            "inflate": part.get("inflate", 0.0), "mirror": part.get("mirror", False),
            "texture": tex, "scale": scale,
        })
    return cubes


def face_image(cube, back):
    """Return the front or the back face of a box UV cube, at SCALE."""
    w, h, d = (int(s) for s in cube["size"])
    u, v = cube["uv"]
    k = cube["scale"]
    x0 = u + 2 * d + w if back else u + d
    y0 = v + d
    face = cube["texture"].crop((round(x0 * k), round(y0 * k), round((x0 + w) * k), round((y0 + h) * k)))
    if cube["mirror"]:
        face = face.transpose(Image.FLIP_LEFT_RIGHT)
    i = cube["inflate"]
    size = (round((cube["size"][0] + 2 * i) * SCALE), round((cube["size"][1] + 2 * i) * SCALE))
    return face.resize(size, Image.NEAREST)


def view(tex, genes, back):
    """Draw the front or the back of the standing zombie with the gene models."""
    cubes = [{"bone": b, "uv": uv, "from": f, "size": s, "inflate": 0.0, "mirror": m,
              "texture": tex, "scale": 1.0} for b, uv, f, s, m in ZOMBIE_CUBES]
    for gene in genes:
        cubes += gene_cubes(gene)
    placed = []
    for cube in cubes:
        px, py, pz = PIVOTS[cube["bone"]]
        i = cube["inflate"]
        x0 = px + cube["from"][0] - i
        x1 = px + cube["from"][0] + cube["size"][0] + i
        y0 = py + cube["from"][1] - i
        z0 = pz + cube["from"][2] - i
        z1 = pz + cube["from"][2] + cube["size"][2] + i
        # From the front, the right side of the zombie is on the left.
        left = 8 - x1 if back else x0 + 8
        depth = z1 if back else -z0
        placed.append((depth, left, y0 + 8, cube))
    im = Image.new("RGBA", (VIEW_W * SCALE, VIEW_H * SCALE), (0, 0, 0, 0))
    # Draw the far faces first.
    for depth, left, top, cube in sorted(placed, key=lambda p: p[0]):
        im.alpha_composite(face_image(cube, back),
                           (round((left + VIEW_X - 8) * SCALE), round((top + VIEW_Y - 8) * SCALE)))
    return im


def text(draw, xy, label, size):
    font = ImageFont.load_default(size=size)
    w = draw.textlength(label, font=font)
    draw.text((xy[0] - w / 2, xy[1]), label, fill=(0, 0, 0, 255), font=font)


def label(gene):
    return gene.replace("_", " ").title()


def zombie(tex, genes):
    """Return the baked texture of a zombie with the genes, in order."""
    for gene in genes:
        tex = bake(tex, gene)
    return tex


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
    """Return a row of zombies: the front views on top, the back views under them."""
    gap, top = 2 * SCALE, 11 * SCALE
    w, h = VIEW_W * SCALE, VIEW_H * SCALE
    im = Image.new("RGBA", (len(columns) * (w + gap) + gap, top + 2 * h + 3 * gap), WHITE)
    draw = ImageDraw.Draw(im)
    text(draw, (im.width / 2, SCALE), title, 4 * SCALE)
    for i, (name, tex, genes) in enumerate(columns):
        x = gap + i * (w + gap)
        text(draw, (x + w / 2, 6 * SCALE), name, 2 * SCALE + 4)
        im.alpha_composite(view(tex, genes, False), (x, top))
        im.alpha_composite(view(tex, genes, True), (x, top + h + gap))
    return im


def main():
    os.makedirs(GENE_OUT, exist_ok=True)
    os.makedirs(FAMILY_OUT, exist_ok=True)
    tex = find_zombie_texture()
    for family, genes in FAMILIES.items():
        ready = [g for g in genes if visual(g) is not None]
        missing = [g for g in genes if g not in ready]
        if missing:
            print(family + ": no visual yet for " + ", ".join(missing))
        if not ready:
            continue
        columns = [("Plain", tex, [])]
        for gene in ready:
            baked = zombie(tex, [gene])
            columns.append((label(gene), baked, [gene]))
            path = os.path.join(GENE_OUT, "zombie_" + gene + ".png")
            gene_image(label(gene), baked, [gene]).save(path)
        if len(ready) > 1:
            columns.append(("All " + str(len(ready)), zombie(tex, ready), ready))
        for name, before in EXTRA.get(family, []):
            genes_here = before + ready
            columns.append((name, zombie(tex, genes_here), genes_here))
        path = os.path.join(FAMILY_OUT, family + ".png")
        sheet(family.title(), columns).save(path)
        print(path)


if __name__ == "__main__":
    main()
