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
texture: outer, front, inner, back. Rows count from the top of a side face.
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
- Shirt and sleeves: Ironhide covers all the cloth at order -5, so the
  marks of other genes go on top of its plates.
- Shirt: Frenzy tears the cloth away at the bottom, with a deep rip in the
  middle of the chest (x 23 and 24). Bloodscent has the cloth over the rip
  (x 23 and 24, y 22 to 24) and x 38 and 39 in rows 1 and 2 of the back.
- Sleeves: Frenzy has rows 2 and 3.
- Arm skin, rows 4 to 6: Frenzy has the front face.
- Legs: Lunger has the knees, rows 2 to 5 of all four faces.
- Death Throes has no marks. Its look is the fuse, a gene model.
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
# The first side face in the texture is the outer side of the limb, as in
# the vanilla box UV: the right side (-x) of the right limb.
OUTER, FRONT, INNER, BACK = 0, 4, 8, 12
SIDES = (OUTER, FRONT, INNER, BACK)

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
    """One color of a mark: a tint and the gray of each pixel.

    The order puts the layer before (lower) or after (higher) the layers of
    other genes. The regions and the materials limit the layer, as in a gene
    visual. A mark of size 16 is a tile that covers the whole texture.
    """

    def __init__(self, tint, alpha=255, order=0, regions=None, materials=None, size=64):
        self.tint = tint
        self.alpha = alpha
        self.order = order
        self.regions = regions
        self.materials = materials
        self.size = size
        self.pixels = {}

    def put(self, pixels, gray=255):
        for p in pixels if isinstance(pixels, list) else [pixels]:
            self.pixels[p] = gray
        return self

    def fill(self, gray=255):
        for y in range(self.size):
            for x in range(self.size):
                self.pixels[(x, y)] = gray
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


def ruin(tear, skin, face, depths):
    """Tear away the bottom of a shirt face, like the shirt of the drowned.

    depths gives, for each column, how many rows of cloth are gone at the
    bottom of the shirt (row 9). The skin shows there. The row over the gap
    gets a dark torn edge, if the depth is 1 to 5.
    """
    for col, depth in enumerate(depths):
        for d in range(depth):
            skin.put(body(face, col, 9 - d), 255 if (col + d) % 2 else 225)
        if depth and depth < 6 and depth not in NO_EDGE.get((face, col), ()):
            tear.put(body(face, col, 9 - depth), 220)


# Shirt pixels where a torn edge would go on an Ironhide plate. The plate
# seam is the edge there.
NO_EDGE = {(CHEST, c): (2,) for c in (0, 1, 2, 5, 6, 7)}
NO_EDGE.update({(SHIRT_BACK, c): (2,) for c in range(1, 7)})


def frenzy():
    # A ruined shirt, like the shirt of the drowned: the cloth is torn away
    # at the bottom of the shirt and at the sleeves, so the skin shows, with
    # a deep rip in the middle of the chest. Claw scratches on the forearms.
    tear = Mark("#121818")
    skin = Mark(SKIN)
    for face in SIDES:
        tear.put(arm(face, 0, 2)).put(arm(face, 3, 2)).put(arm(face, 2, 3), 200)
        skin.put(arm(face, 0, 3)).put(arm(face, 1, 2), 225).put(arm(face, 1, 3)).put(arm(face, 3, 3), 225)
    ruin(tear, skin, CHEST, (1, 2, 1, 5, 4, 2, 1, 2))
    ruin(tear, skin, SHIRT_BACK, (3, 1, 2, 1, 2, 1, 2, 3))
    ruin(tear, skin, RIGHT_SIDE, (2, 3, 1, 2))
    ruin(tear, skin, LEFT_SIDE, (1, 2, 3, 2))
    scratch = Mark("#8E1A1A")
    for col, row in ((0, 4), (1, 5), (2, 6), (2, 4), (3, 5)):
        scratch.put(arm(FRONT, col, row), 255 if row < 6 else 190)
    return [tear, skin, scratch]


def bloodscent():
    # A flared, sniffing snout: wrinkles on the nose bridge, wide nostrils,
    # and dark red streaks in the nostrils.
    snout = Mark("#3E4A26")
    snout.put((11, 12), 230).put((12, 12), 230).put((10, 13), 255).put((13, 13), 255)
    streak = Mark("#7A1414")
    streak.put((11, 13), 255).put((12, 13), 200)
    stain = Mark("#5A0E0E")
    # Blood that dripped from the snout onto the cloth in the middle of the
    # chest, over the Frenzy rip, and a stain high on the back.
    stain.put(body(CHEST, 3, 2)).put(body(CHEST, 4, 2), 200).put(body(CHEST, 4, 3))
    stain.put(body(CHEST, 4, 4), 180)
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


def hush():
    # The mouth is sewn shut: pale stitches across the dark line of the lips.
    thread = Mark("#A88B5C")
    thread.put((10, 14)).put((12, 14), 225)
    seam = Mark("#1A100A")
    seam.put((11, 14)).put((13, 14))
    return [thread, seam]


# The shapes of the Shade patches, as (col, row) offsets.
PATCH_SHAPES = (((0, 0), (1, 0), (0, 1)), ((0, 0), (1, 0), (1, 1), (0, 1)),
                ((0, 0), (0, 1), (1, 1)), ((0, 0), (1, 0)), ((1, 0), (0, 1), (1, 1), (2, 1)))


def patches(faces, rows, count, seed):
    """Yield (face, col, row, gray) for small patches on the faces of a limb.

    Each face gets count patches of 2 to 4 pixels, in the given rows.
    """
    first, last = rows[0], rows[-1]
    for f, face in enumerate(faces):
        for n in range(count):
            h = (seed * 31 + f * 7 + n * 13) * 2654435761 % 4294967296
            shape = PATCH_SHAPES[h % len(PATCH_SHAPES)]
            col = (h >> 8) % 3
            row = first + (h >> 12) % (last - first)
            for dc, dr in shape:
                if col + dc < 4 and row + dr <= last:
                    yield face, col + dc, row + dr, 255 if (h >> 20) % 3 else 215


def shade():
    # Dark, mottled patches on the arms and the legs. They are near black and
    # opaque, so that they keep the most contrast when the zombie is
    # translucent at night. They go under the marks of other genes (order -2).
    dark = Mark("#0E1014", order=-2)
    for face, col, row, gray in patches(SIDES, range(4, 8), 2, 3):
        dark.put(arm(face, col, row), gray)
    for face, col, row, gray in patches(SIDES, range(0, 10), 4, 11):
        dark.put(leg(face, col, row), gray)
    return [dark]


def burrower():
    # Dirt from the ground, caked on the shins and the shoes. It is thickest
    # at the bottom. It goes under the marks of other genes (order -1).
    dirt = Mark("#6A4A2C", alpha=235, order=-1)
    chance = {6: 15, 7: 30, 8: 50, 9: 70, 10: 65, 11: 80}
    for face in SIDES:
        for row, percent in chance.items():
            for col in range(4):
                h = (face * 17 + row * 31 + col * 7) % 100
                if h < percent:
                    dirt.put(leg(face, col, row), (255, 200, 160)[h % 3])
    return [dirt]


def lurker():
    # A dark shadow over the scalp, like a hood, with a brim over the
    # forehead. It goes under the marks of other genes (order -1).
    hood = Mark("#15180F", alpha=205, order=-1)
    soft = Mark("#15180F", alpha=120, order=-1)
    for y in range(0, 8):
        for x in range(8, 16):
            hood.put((x, y))
    for y in range(8, 16):
        for x in range(24, 32):
            hood.put((x, y))
        for col in range(8):
            for side in (0, 1):
                (hood if y < 14 else soft).put(head_side(side, col, y - 8))
    for x in range(8, 16):
        hood.put((x, 8))
    soft.put((8, 9)).put((15, 9))
    return [hood, soft]


def nightborn():
    # A pale face, under the marks of other genes (order -1). The bright eyes
    # are in the gene model nightborn_eyes, which glows in the dark.
    pale = Mark("#D2DCC0", alpha=110, order=-1, regions=["face"], materials=["skin"], size=16).fill()
    return [pale]


GENES = {
    "lunger": lunger,
    "frenzy": frenzy,
    "bloodscent": bloodscent,
    "rabid": rabid,
    "hush": hush,
    "shade": shade,
    "burrower": burrower,
    "lurker": lurker,
    "nightborn": nightborn,
}

# The gene models of the genes in GENES.
MODELS = {
    "nightborn": "originofspawns:nightborn_eyes",
}


def write(gene, marks):
    # Remove the old patterns of the gene, so that no unused file stays.
    for old in os.listdir(MARK_DIR):
        if old.startswith(gene + "_") and old[len(gene) + 1:-4].isdigit():
            os.remove(os.path.join(MARK_DIR, old))
    layers = []
    n = 0
    for mark in marks:
        if isinstance(mark, dict):
            # A layer that is not a pattern, for example a desaturate layer.
            layers.append(mark)
            continue
        im = Image.new("RGBA", (mark.size, mark.size), (0, 0, 0, 0))
        for (x, y), gray in mark.pixels.items():
            im.putpixel((x, y), (gray, gray, gray, mark.alpha))
        name = f"{gene}_{n}"
        n += 1
        im.save(os.path.join(MARK_DIR, name + ".png"))
        layer = {}
        if mark.regions:
            layer["regions"] = mark.regions
        if mark.materials:
            layer["materials"] = mark.materials
        layer["effect"] = {"type": "pattern",
                           "texture": f"originofspawns:textures/gene_marks/{name}.png",
                           "tint": mark.tint}
        if mark.order:
            layer["order"] = mark.order
        layers.append(layer)
    visual = {"layers": layers}
    if gene in MODELS:
        visual["model"] = MODELS[gene]
    with open(os.path.join(VISUAL_DIR, gene + ".json"), "w") as fh:
        json.dump(visual, fh, indent="\t")
        fh.write("\n")


REGIONS = {"face": 1, "scalp": 2, "torso": 3, "arms": 4, "hands": 5, "legs": 6,
           "lower_legs": 7, "feet": 8}
MATERIALS = {"skin": 1, "cloth": 2, "eyes": 3}


def touched(visual):
    """Return the pixels that the marks of a gene visual change.

    A desaturate layer, or a layer with an order below 0, is a color change
    of a whole part that goes under the marks. The check does not count it.
    A spots layer can change each pixel that it covers.
    """
    regions = Image.open(os.path.join(ASSETS, "textures/region/humanoid.png")).convert("RGBA")
    pixels = set()
    for layer in visual.get("layers", []):
        effect = layer["effect"]
        if effect["type"] == "desaturate" or layer.get("order", 0) < 0:
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
        marked = [m for m in marks if not isinstance(m, dict) and m.order >= 0]
        print(gene, sum(len(m.pixels) for m in marked), "mark pixels")
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
