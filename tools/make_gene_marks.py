"""Make the marks of the zombie genes.

Usage: python3 tools/make_gene_marks.py

A mark is a 64x64 gray pattern with alpha. The pattern effect stamps pattern
pixel (x % 64, y % 64) on texture pixel (x, y), so a 64x64 pattern puts each
mark on exact pixels of the zombie texture. The tint gives the color, and the
gray gives the shade: gray 255 is the tint, and a lower gray is darker.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- gene_visuals/<gene>.json, with one pattern layer for each pattern.
The script does not write the visuals of the genes that are not in GENES.

The script prints the pixels that two genes share. Each gene has its own
pixels, so that a zombie can carry 5 genes and each mark stays visible.

Pixel plan. The UV is the vanilla zombie UV. The arms and the legs are
4 pixels on each side. Their side faces are, from left to right in the
texture: inner, front, outer, back. Rows count from the top of a side face.
- Face, mouth: Bloodscent has the mouth corners and the jaw corners.
  Rabid has two drips under the lip and the neck. Hush has the lips (row
  y 14). Packcaller has the jaw (row y 15). Leech has the row under the
  nose (row y 13).
- Face, eyes: Nightborn has the outer eye pixels. Miasma has the inner eye
  pixels.
- Sleeves: Ironhide has the arm top and rows 0 and 1. Frenzy has rows 2
  and 3.
- Hands: Lunger has rows 10 and 11. Death Throes has rows 8 and 9 on the
  front and the back. The other hand genes use rows 8 and 9 on the inner
  and the outer faces, and the hand end.
- Feet: Death Throes has the front of the shoe.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")
MARK_DIR = os.path.join(ASSETS, "textures/gene_marks")
VISUAL_DIR = os.path.join(ASSETS, "gene_visuals")

# The side faces of a limb, as the x offset from the start of the limb UV.
INNER, FRONT, OUTER, BACK = 0, 4, 8, 12
SIDES = (INNER, FRONT, OUTER, BACK)

# The UV of the limbs. The vanilla zombie uses the right limbs for both
# sides. The drowned has its own left limbs. A mark on a limb goes on both.
ARMS = ((40, 16), (32, 48))
LEGS = ((0, 16), (16, 48))


def arm(face, col, row):
    """Return the texture pixels of a pixel on a side face of each arm."""
    return [(u + face + col, v + 4 + row) for u, v in ARMS]


def leg(face, col, row):
    return [(u + face + col, v + 4 + row) for u, v in LEGS]


def hand_end(col, row):
    """Return the pixels of the bottom face of each arm, which is the hand end."""
    return [(u + 8 + col, v + row) for u, v in ARMS]


class Mark:
    """One color of a mark: a tint and the gray of each pixel."""

    def __init__(self, tint, alpha=255):
        self.tint = tint
        self.alpha = alpha
        self.pixels = {}

    def put(self, pixels, gray=255):
        for p in pixels if isinstance(pixels, list) else [pixels]:
            self.pixels[p] = gray
        return self


def lunger():
    # Long dark nails: two claws on each side of the hand, at the finger tips.
    nail = Mark("#3A302A")
    for face in SIDES:
        for col in (0, 2):
            nail.put(arm(face, col, 10), 255)
            nail.put(arm(face, col, 11), 150)
    for col in (0, 2):
        for row in (0, 2):
            nail.put(hand_end(col, row), 150)
    return [nail]


def frenzy():
    # Torn sleeves: dark tears in the hem of each sleeve.
    tear = Mark("#1C2424", alpha=230)
    for face in SIDES:
        tear.put(arm(face, 1, 2))
        for col in (0, 2, 3):
            tear.put(arm(face, col, 3))
    return [tear]


def bloodscent():
    # Red stains around the mouth: the corners and the jaw.
    blood = Mark("#A41E1E")
    blood.put((9, 14), 255).put((14, 14), 210).put((8, 15), 170).put((15, 15), 255)
    blood.put((8, 14), 210)
    return [blood]


def rabid():
    # White foam that drips from the lip and runs down the neck.
    foam = Mark("#ECEFE2")
    foam.put((9, 15), 255).put((14, 15), 230)
    foam.put((22, 20), 230).put((24, 20), 255).put((24, 21), 215)
    return [foam]


def death_throes():
    # Dark veins in a zigzag on the hands and on the front of the feet.
    vein = Mark("#4C2244")
    for face in (FRONT, BACK):
        for col in (0, 2):
            vein.put(arm(face, col, 8))
            vein.put(arm(face, col + 1, 9), 200)
    for col in (0, 2):
        vein.put(leg(FRONT, col, 10))
        vein.put(leg(FRONT, col + 1, 11), 200)
    return [vein]


GENES = {
    "lunger": lunger,
    "frenzy": frenzy,
    "bloodscent": bloodscent,
    "rabid": rabid,
    "death_throes": death_throes,
}


def write(gene, marks):
    layers = []
    for n, mark in enumerate(marks):
        im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        for (x, y), gray in mark.pixels.items():
            im.putpixel((x, y), (gray, gray, gray, mark.alpha))
        name = f"{gene}_{n}"
        im.save(os.path.join(MARK_DIR, name + ".png"))
        layers.append({"effect": {"type": "pattern",
                                  "texture": f"originofspawns:textures/gene_marks/{name}.png",
                                  "tint": mark.tint}})
    with open(os.path.join(VISUAL_DIR, gene + ".json"), "w") as fh:
        json.dump({"layers": layers}, fh, indent="\t")
        fh.write("\n")


REGIONS = {"face": 1, "scalp": 2, "torso": 3, "arms": 4, "hands": 5, "legs": 6,
           "lower_legs": 7, "feet": 8}
MATERIALS = {"skin": 1, "cloth": 2, "eyes": 3}


def touched(visual):
    """Return the pixels that the marks of a gene visual change.

    A desaturate layer is a color change of a whole part, not a mark, so the
    check does not count it. A spots layer can change each pixel that it covers.
    """
    regions = Image.open(os.path.join(ASSETS, "textures/region/humanoid.png")).convert("RGBA")
    pixels = set()
    for layer in visual.get("layers", []):
        effect = layer["effect"]
        if effect["type"] == "desaturate":
            continue
        pattern = None
        if effect["type"] == "pattern":
            namespace, path = effect["texture"].split(":")
            pattern = Image.open(os.path.join(ROOT, "src/main/resources/assets", namespace, path)).convert("RGBA")
        want_r = {REGIONS[r] for r in layer.get("regions", [])}
        want_m = {MATERIALS[m] for m in layer.get("materials", [])}
        for y in range(64):
            for x in range(64):
                r, m, _, a = regions.getpixel((x, y))
                if not a or (want_r and r not in want_r) or (want_m and m not in want_m):
                    continue
                if pattern and not pattern.getpixel((x % pattern.width, y % pattern.height))[3]:
                    continue
                pixels.add((x, y))
    return pixels


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for gene, make in GENES.items():
        marks = make()
        write(gene, marks)
        print(gene, sum(len(m.pixels) for m in marks), "pixels")
    owners = {}
    for name in sorted(os.listdir(VISUAL_DIR)):
        with open(os.path.join(VISUAL_DIR, name)) as fh:
            for p in touched(json.load(fh)):
                owners.setdefault(p, set()).add(name[:-5])
    shared = {}
    for p, genes in owners.items():
        if len(genes) > 1:
            key = " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
