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
Cinder keeps the hands and the feet. Bonerunner erases the lower legs and
the feet, so no other mark goes there.
- Face, eyes: Nightborn has the outer eye pixels. Miasma has the inner eye
  pixels.
- Face, around the eyes: Rabid has the row over the eyes, the pixels under
  the outer eye corners, and the cheek edges (x 8 and 15, y 12 and 13).
- Face, nose: Bloodscent has the nose bridge (y 12) and the nose (y 13).
- Face, mouth: Rabid has the mouth corners (x 9 and 14) and the jaw edges
  (x 8 and 15, y 15). Hush has the lips (y 14, x 10 to 13). Packcaller has
  the jaw (y 15, x 10 to 13). Leech has no face pixels yet.
- Low head sides, next to the face: Death Throes.
- Chest: Death Throes has rows 0 and 1, and the neck. Ironhide has the
  breastplate, x 21 to 26 and y 22 to 27. Frenzy has the belly, x 20 to 23
  and y 28 and 29. Bloodscent has the right edge under the breastplate, and
  the belly, x 24 and 25.
- Back of the shirt: Death Throes has rows 0 and 1. Ironhide has the
  breastplate, x 33 to 38 and y 22 to 27. Bloodscent has x 38 and 39 in
  rows 1 and 2. Frenzy has rows 8 and 9.
- Sides of the shirt: Frenzy has the right side, rows 2 to 4.
- Sleeves: Ironhide has the arm top and rows 0 and 1. Frenzy has rows 2
  and 3.
- Arm skin, rows 4 to 6: Frenzy has the front face. Death Throes has the
  outer face.
- Legs: Lunger has the knees, rows 2 to 5 of all four faces.
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


def head_side(side, col, row):
    """Return a pixel of a side of the head. Column 0 is next to the face."""
    return (7 - col, 8 + row) if side == 0 else (16 + col, 8 + row)


# The faces of the body: the top left pixel of each face.
CHEST, SHIRT_BACK, RIGHT_SIDE, LEFT_SIDE = (20, 20), (32, 20), (16, 20), (28, 20)


def body(face, col, row):
    return (face[0] + col, face[1] + row)


# The color of zombie skin, for the skin that shows through a tear.
SKIN = "#71955B"


def lunger():
    # Strained, torn knees: the cloth is torn all around the knee, with holes,
    # red scrapes, and frayed threads from row 2 to row 5.
    hole = Mark("#16121E")
    scrape = Mark("#9A2A22")
    thread = Mark("#B8B2C8")
    hole.put(leg(FRONT, 1, 4)).put(leg(FRONT, 2, 5))
    scrape.put(leg(FRONT, 2, 4)).put(leg(FRONT, 1, 5), 200)
    thread.put(leg(FRONT, 0, 4)).put(leg(FRONT, 3, 5), 210).put(leg(FRONT, 1, 3), 190)
    thread.put(leg(FRONT, 2, 2), 170)
    hole.put(leg(OUTER, 1, 3)).put(leg(OUTER, 1, 4)).put(leg(OUTER, 2, 5))
    thread.put(leg(OUTER, 2, 4), 200).put(leg(OUTER, 0, 5), 190).put(leg(OUTER, 1, 2), 170)
    hole.put(leg(BACK, 1, 4)).put(leg(BACK, 2, 4))
    scrape.put(leg(BACK, 2, 5), 200)
    thread.put(leg(BACK, 0, 3), 190).put(leg(BACK, 3, 5), 170)
    hole.put(leg(INNER, 2, 3))
    thread.put(leg(INNER, 1, 4), 190).put(leg(INNER, 2, 2), 170)
    return [hole, scrape, thread]


def frenzy():
    # Ripped sleeves with jagged edges, claw scratches on the forearms, and
    # small tears in the shirt where the skin shows.
    tear = Mark("#121818")
    fray = Mark("#9AA6A6")
    for face in SIDES:
        tear.put(arm(face, 1, 2)).put(arm(face, 0, 3)).put(arm(face, 2, 3))
        fray.put(arm(face, 3, 3), 220)
    scratch = Mark("#8E1A1A")
    for col, row in ((0, 4), (1, 5), (2, 6), (2, 4), (3, 5)):
        scratch.put(arm(FRONT, col, row), 255 if row < 6 else 190)
    skin = Mark(SKIN)
    # A tear on the belly, on the right side, and on the back.
    tear.put(body(CHEST, 0, 8)).put(body(CHEST, 3, 9), 200)
    skin.put(body(CHEST, 1, 8)).put(body(CHEST, 2, 8), 225).put(body(CHEST, 2, 9))
    tear.put(body(RIGHT_SIDE, 1, 2)).put(body(RIGHT_SIDE, 3, 4))
    skin.put(body(RIGHT_SIDE, 2, 3)).put(body(RIGHT_SIDE, 1, 3), 225).put(body(RIGHT_SIDE, 2, 4))
    tear.put(body(SHIRT_BACK, 2, 8)).put(body(SHIRT_BACK, 5, 9), 200)
    skin.put(body(SHIRT_BACK, 3, 8)).put(body(SHIRT_BACK, 4, 9), 225).put(body(SHIRT_BACK, 3, 9))
    return [tear, fray, scratch, skin]


def bloodscent():
    # A flared, sniffing snout: wrinkles on the nose bridge, wide nostrils,
    # and dark red streaks in the nostrils. Blood stains on the shirt, low on
    # the belly and on the right edge, away from the Frenzy tears.
    snout = Mark("#3E4A26")
    snout.put((11, 12), 230).put((12, 12), 230).put((10, 13), 255).put((13, 13), 255)
    streak = Mark("#7A1414")
    streak.put((11, 13), 255).put((12, 13), 200)
    stain = Mark("#6E1212")
    stain.put(body(CHEST, 7, 5)).put(body(CHEST, 7, 6), 200).put(body(CHEST, 7, 8))
    stain.put(body(CHEST, 4, 8), 220).put(body(CHEST, 5, 8)).put(body(CHEST, 5, 9), 190)
    stain.put(body(SHIRT_BACK, 6, 1)).put(body(SHIRT_BACK, 7, 2), 200)
    return [snout, streak, stain]


def rabid():
    # A feverish look: red rims around the eyes and flushed cheeks, and foam
    # at the corners of the mouth that runs along the jaw.
    rim = Mark("#B42828")
    for x in (9, 10, 13, 14):
        rim.put((x, 11), 210)
    rim.put((9, 13), 255).put((14, 13), 255)
    fever = Mark("#C2554A")
    for x in (8, 15):
        fever.put((x, 12), 230).put((x, 13), 255)
    foam = Mark("#ECEFE2", alpha=235)
    foam.put((9, 15), 255).put((14, 15), 255).put((8, 15), 225).put((15, 15), 225)
    # Thin foam over the mouth corners, so that the foam is not a hard block.
    bubbles = Mark("#ECEFE2", alpha=140)
    bubbles.put((9, 14)).put((14, 14))
    return [rim, fever, foam, bubbles]


def death_throes():
    # Gunpowder: gray-black speckles with a few dim orange cracks, on the
    # upper chest, the neck, the upper back, and the outer side of the arms.
    # Two cracks go up the neck to the jaw, on the sides of the head.
    powder = Mark("#2E2C2A")
    crack = Mark("#A65A24")
    for col, gray in ((0, 255), (1, 190), (6, 220), (7, 255)):
        powder.put(body(CHEST, col, 0), gray)
    powder.put(body(CHEST, 2, 0), 200).put(body(CHEST, 0, 1), 220).put(body(CHEST, 7, 1), 190)
    powder.put(body(CHEST, 5, 0), 230).put(body(CHEST, 4, 1), 210)
    crack.put(body(CHEST, 3, 1), 230).put(body(CHEST, 3, 0), 200).put(body(CHEST, 6, 1), 200)
    for col, row, gray in ((0, 0, 230), (2, 1, 255), (3, 0, 200), (5, 1, 240), (7, 0, 220)):
        powder.put(body(SHIRT_BACK, col, row), gray)
    crack.put(body(SHIRT_BACK, 4, 0), 210)
    for col, row, gray in ((0, 4, 255), (2, 4, 210), (1, 5, 230), (3, 6, 240), (0, 6, 200)):
        powder.put(arm(OUTER, col, row), gray)
    crack.put(arm(OUTER, 2, 5), 220).put(arm(OUTER, 1, 6), 190)
    for side in (0, 1):
        crack.put(head_side(side, 1, 7)).put(head_side(side, 1, 6), 220).put(head_side(side, 0, 5), 200)
        powder.put(head_side(side, 2, 7), 240).put(head_side(side, 0, 7), 210).put(head_side(side, 2, 5), 200)
    return [powder, crack]


GENES = {
    "lunger": lunger,
    "frenzy": frenzy,
    "bloodscent": bloodscent,
    "rabid": rabid,
    "death_throes": death_throes,
}


def write(gene, marks):
    # Remove the old patterns of the gene, so that no unused file stays.
    for old in os.listdir(MARK_DIR):
        if old.startswith(gene + "_") and old[len(gene) + 1:-4].isdigit():
            os.remove(os.path.join(MARK_DIR, old))
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
