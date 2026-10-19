"""Make preview images of the zombie gene visuals.

Usage: python3 tools/preview_genes.py

The script reads the vanilla zombie texture from the Minecraft jar in the
Gradle cache of Loom. It reads the visual layers of each gene from the data
pack of the mod, applies them with the region map, and writes PNG files to
build/previews/. Git ignores build/.
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

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESOURCES = os.path.join(ROOT, "src/main/resources")
GENE_DIR = os.path.join(RESOURCES, "data/originofspawns/originofspawns/gene")
REGION_MAP = os.path.join(RESOURCES, "assets/originofspawns/textures/region/humanoid.png")
OUT_DIR = os.path.join(ROOT, "build/previews")
ZOMBIE_PATH = "assets/minecraft/textures/entity/zombie/zombie.png"

REGIONS = {"face": 1, "scalp": 2, "torso": 3, "arms": 4, "hands": 5, "legs": 6,
           "lower_legs": 7, "feet": 8}
MATERIALS = {"skin": 1, "cloth": 2, "eyes": 3}

SCALE = 12


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


def face(tex, x0, y0, w, h, flip=False):
    img = tex.crop((x0, y0, x0 + w, y0 + h))
    return img.transpose(Image.FLIP_LEFT_RIGHT) if flip else img


def render(tex):
    """Draw the front and the back of the standing zombie, side by side.

    The vanilla zombie uses the right limbs for the left limbs, with a mirror.
    """
    view = Image.new("RGBA", (16 * 2 + 4, 32), (0, 0, 0, 0))
    # Front. The right side of the zombie is on the left of the image.
    view.paste(face(tex, 8, 8, 8, 8), (4, 0))
    view.paste(face(tex, 20, 20, 8, 12), (4, 8))
    view.paste(face(tex, 44, 20, 4, 12), (0, 8))
    view.paste(face(tex, 44, 20, 4, 12, flip=True), (12, 8))
    view.paste(face(tex, 4, 20, 4, 12), (4, 20))
    view.paste(face(tex, 4, 20, 4, 12, flip=True), (8, 20))
    # Back. The right side of the zombie is on the right of the image.
    ox = 20
    view.paste(face(tex, 24, 8, 8, 8), (ox + 4, 0))
    view.paste(face(tex, 32, 20, 8, 12), (ox + 4, 8))
    view.paste(face(tex, 52, 20, 4, 12, flip=True), (ox, 8))
    view.paste(face(tex, 52, 20, 4, 12), (ox + 12, 8))
    view.paste(face(tex, 12, 20, 4, 12, flip=True), (ox + 4, 20))
    view.paste(face(tex, 12, 20, 4, 12), (ox + 8, 20))
    bg = Image.new("RGBA", view.size, (200, 210, 220, 255))
    bg.alpha_composite(view)
    return bg.resize((view.width * SCALE, view.height * SCALE), Image.NEAREST)


def save(name, tex):
    tex.resize((64 * 8, 64 * 8), Image.NEAREST).save(os.path.join(OUT_DIR, name + "_texture.png"))
    path = os.path.join(OUT_DIR, name + "_front_back.png")
    render(tex).save(path)
    print(path)


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    tex = find_zombie_texture()
    regions = Image.open(REGION_MAP).convert("RGBA")
    save("zombie_plain", tex)
    save("zombie_cinder", bake(tex, regions, "cinder"))
    save("zombie_ironhide", bake(tex, regions, "ironhide"))
    save("zombie_combined", bake(bake(tex, regions, "cinder"), regions, "ironhide"))


if __name__ == "__main__":
    main()
