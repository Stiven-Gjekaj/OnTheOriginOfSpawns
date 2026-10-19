"""Make preview images of the zombie gene visuals.

Usage: python3 tools/preview_genes.py

The script reads the vanilla zombie texture from the Minecraft jar in the
Gradle cache of Loom. It reads the visual layers of each gene from the data
pack of the mod, and applies them with the region map. A gene with the model
originofspawns:bone_legs also gets the bone legs, as BoneLegsLayer draws them.
The script writes PNG files to build/previews/genes/. Git ignores build/:
- one image for each gene, a plain zombie, and a zombie with all genes. Each
  image has a front view and a back view on a white background.
- zombie_genes_sheet.png, with all of them in a row. The front views are on
  top, and the back views are under them.
Each preview uses the vanilla texture. Do not commit a preview.
This script needs Pillow.

The effects here copy TextureBaker in the mod, so that the previews show the
same pixels as the game. If TextureBaker changes, change this script too.
"""
import glob
import io
import json
import os
import zipfile

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESOURCES = os.path.join(ROOT, "src/main/resources")
GENE_DIR = os.path.join(RESOURCES, "data/originofspawns/originofspawns/gene")
REGION_MAP = os.path.join(RESOURCES, "assets/originofspawns/textures/region/humanoid.png")
OUT_DIR = os.path.join(ROOT, "build/previews/genes")
BONE_LEG_FILE = os.path.join(RESOURCES, "assets/originofspawns/textures/entity/bone_leg.png")
BONE_LEGS = "originofspawns:bone_legs"
WHITE = (255, 255, 255, 255)
# The genes come in this order. Other genes come after them, by name.
ORDER = ["cinder", "ironhide", "bonerunner"]
ZOMBIE_PATH = "assets/minecraft/textures/entity/zombie/zombie.png"

REGIONS = {"face": 1, "scalp": 2, "torso": 3, "arms": 4, "hands": 5, "legs": 6,
           "lower_legs": 7, "feet": 8}
MATERIALS = {"skin": 1, "cloth": 2, "eyes": 3}

SCALE = 8
BONE_LEG = Image.open(BONE_LEG_FILE).convert("RGBA")


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


def texture_file(identifier):
    namespace, path = identifier.split(":")
    return os.path.join(RESOURCES, "assets", namespace, path)


def desaturate(color, amount, darken):
    r, g, b, a = color
    gray = (r * 299 + g * 587 + b * 114) // 1000
    keep = 1.0 - darken
    return tuple(int((c + (gray - c) * amount) * keep) for c in (r, g, b)) + (a,)


def apply(effect, color, x, y, seed, patterns):
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
        pattern = patterns[effect["texture"]]
        tint = rgb(effect.get("tint", "#FFFFFF"))
        stamp = pattern.getpixel((x % pattern.width, y % pattern.height))
        strength = stamp[3] / 255
        shade = stamp[0] / 255
        return tuple(int(color[i] + (tint[i] * shade - color[i]) * strength) for i in range(3)) + (color[3],)
    if kind == "erase":
        return (0, 0, 0, 0)
    raise ValueError("unknown effect " + kind)


def bake(tex, regions, gene):
    """Apply the visual layers of one gene, like TextureBaker.bake."""
    with open(os.path.join(GENE_DIR, gene + ".json")) as fh:
        layers = json.load(fh)["visual"]["layers"]
    patterns = {}
    for layer in layers:
        effect = layer["effect"]
        if effect["type"] == "pattern":
            patterns[effect["texture"]] = Image.open(texture_file(effect["texture"])).convert("RGBA")
    seed = identifier_hash("originofspawns:" + gene)
    out = tex.copy()
    for y in range(64):
        for x in range(64):
            region, material, _, alpha = regions.getpixel((x, y))
            if not alpha:
                continue
            for layer in layers:
                wanted_regions = {REGIONS[r] for r in layer.get("regions", [])}
                wanted_materials = {MATERIALS[m] for m in layer.get("materials", [])}
                if wanted_regions and region not in wanted_regions:
                    continue
                if wanted_materials and material not in wanted_materials:
                    continue
                out.putpixel((x, y), apply(layer["effect"], out.getpixel((x, y)), x, y, seed, patterns))
    return out


def models(gene):
    """Return the model changes of a gene."""
    with open(os.path.join(GENE_DIR, gene + ".json")) as fh:
        model = json.load(fh)["visual"].get("model")
    return {model} if model else set()


def face(tex, x0, y0, w, h, flip=False):
    img = tex.crop((x0, y0, x0 + w, y0 + h))
    img = img.transpose(Image.FLIP_LEFT_RIGHT) if flip else img
    return img.resize((w * SCALE, h * SCALE), Image.NEAREST)


def view(tex, back, bone_legs):
    """Draw the front or the back of the standing zombie, 16x32 pixels at SCALE.

    The vanilla zombie uses the right limbs for the left limbs, with a mirror.
    From the front, the right side of the zombie is on the left of the image.
    """
    im = Image.new("RGBA", (16 * SCALE, 32 * SCALE), (0, 0, 0, 0))
    s = SCALE
    if not back:
        parts = [((8, 8, 8, 8), False, (4, 0)), ((20, 20, 8, 12), False, (4, 8)),
                 ((44, 20, 4, 12), False, (0, 8)), ((44, 20, 4, 12), True, (12, 8)),
                 ((4, 20, 4, 12), False, (4, 20)), ((4, 20, 4, 12), True, (8, 20))]
    else:
        parts = [((24, 8, 8, 8), False, (4, 0)), ((32, 20, 8, 12), False, (4, 8)),
                 ((52, 20, 4, 12), True, (0, 8)), ((52, 20, 4, 12), False, (12, 8)),
                 ((12, 20, 4, 12), True, (4, 20)), ((12, 20, 4, 12), False, (8, 20))]
    for (x0, y0, w, h), flip, (px, py) in parts:
        im.alpha_composite(face(tex, x0, y0, w, h, flip), (px * s, py * s))
    if bone_legs:
        # Copy BoneLegsLayer: a 3x6x3 bone in the middle of the lower half of
        # each leg. The bone is half a pixel in from each side of the leg.
        bone = face(BONE_LEG, 9 if back else 3, 3, 3, 6)
        for leg_x in (4, 8):
            im.alpha_composite(bone, (leg_x * s + s // 2, 26 * s))
    return im


def text(draw, xy, label, size):
    font = ImageFont.load_default(size=size)
    w = draw.textlength(label, font=font)
    draw.text((xy[0] - w / 2, xy[1]), label, fill=(0, 0, 0, 255), font=font)


def gene_image(label, tex, bone_legs):
    """Return one image with the front and the back view, and a label."""
    gap, top, foot = 4 * SCALE, 6 * SCALE, 4 * SCALE
    w, h = 16 * SCALE, 32 * SCALE
    im = Image.new("RGBA", (2 * w + 3 * gap, h + top + foot), WHITE)
    draw = ImageDraw.Draw(im)
    text(draw, (im.width / 2, SCALE), label, 3 * SCALE)
    for i, back in enumerate((False, True)):
        x = gap + i * (w + gap)
        im.alpha_composite(view(tex, back, bone_legs), (x, top))
        text(draw, (x + w / 2, top + h + SCALE), "back" if back else "front", 2 * SCALE)
    return im


def sheet(zombies):
    """Return all zombies in a row: the front views on top, the back views under them."""
    gap, top = 4 * SCALE, 6 * SCALE
    w, h = 16 * SCALE, 32 * SCALE
    im = Image.new("RGBA", (len(zombies) * (w + gap) + gap, top + 2 * h + 3 * gap), WHITE)
    draw = ImageDraw.Draw(im)
    for i, (label, tex, bone_legs) in enumerate(zombies):
        x = gap + i * (w + gap)
        text(draw, (x + w / 2, SCALE), label, 3 * SCALE)
        im.alpha_composite(view(tex, False, bone_legs), (x, top))
        im.alpha_composite(view(tex, True, bone_legs), (x, top + h + 2 * gap))
    return im


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    tex = find_zombie_texture()
    regions = Image.open(REGION_MAP).convert("RGBA")
    found = sorted(f[:-5] for f in os.listdir(GENE_DIR) if f.endswith(".json"))
    genes = [g for g in ORDER if g in found] + [g for g in found if g not in ORDER]
    zombies = [("plain", "Plain zombie", tex, False)]
    for gene in genes:
        zombies.append((gene, gene.capitalize(), bake(tex, regions, gene), BONE_LEGS in models(gene)))
    combined = tex
    for gene in genes:
        combined = bake(combined, regions, gene)
    all_models = set().union(*(models(g) for g in genes))
    zombies.append(("all_genes", "All genes", combined, BONE_LEGS in all_models))
    for key, label, baked, bone_legs in zombies:
        path = os.path.join(OUT_DIR, "zombie_" + key + ".png")
        gene_image(label, baked, bone_legs).save(path)
        print(path)
    path = os.path.join(OUT_DIR, "zombie_genes_sheet.png")
    sheet([z[1:] for z in zombies]).save(path)
    print(path)


if __name__ == "__main__":
    main()
