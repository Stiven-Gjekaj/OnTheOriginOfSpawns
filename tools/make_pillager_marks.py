"""Make the gene visuals of the pillager, from design/species/pillager.md.

Usage: python3 tools/make_pillager_marks.py (from the root of the repository)

The pillager texture is 64x64, with the illager layout. The parts, from
IllagerModel:
- head: 8x10x8 at uv (0, 0). The face is x 8 to 15, y 8 to 17. The eyes are
  (9, 14), (10, 14), (13, 14), and (14, 14). The mouth is x 10 to 13, y 16
  and 17.
- body: 8x12x6 at uv (16, 20). The belt is on rows 8 and 9.
- coat: 8x20x6 at uv (0, 38). Only rows 9 to 16 of its sides are drawn: the
  hem over the legs. A mark on a clear pixel does not show.
- arms: 4x12x4 at uv (40, 46). Rows 10 and 11 are the hands.
- legs: 4x12x4 at uv (0, 22). Rows 0 to 5 are the trousers, and rows 6 to 11
  are the boots.
The pillager holds a crossbow, so both arms point forward. The gene models
use the bones head, body, right_arm, left_arm, right_leg, and left_leg.

Pixel plan for the Crossbowmen.
- Piercer: a bracer of steel on the right arm, which holds the crossbow, the
  gene model piercer_bracer.
- Long Bolt: a quiver of long bolts on the back, the gene model
  long_bolt_quiver.
- Heavy Bolt has thick leather wraps on the arms: three wraps of brown
  leather round each arm.
- Overdraw has a scar over one eye: a short, pale red scar across the right
  eye, from the brow to the cheek.
- Quick Charge has worn, dark gloves: the hands are dark leather, with worn,
  pale spots.
- Splinter has a belt of jagged bolts: short bolts that stand in the belt,
  with jagged steel tips, round the body.

Pixel plan for the Bombardiers.
- Rocketeer: a bundle of rockets on the back, the gene model
  rocketeer_rockets.
- Flak has a red sash with stars: a red sash from the left shoulder to the
  right hip, on the front and the back, with yellow stars on it.
- Incendiary has scorched sleeves: the lower sleeves are black with burns,
  with orange embers and gray ash.
- Smoke Shell has soot on the face: dark smudges of soot on the cheeks, the
  chin, and the sides of the head.
- Powder Keg: a small keg on the belt, the gene model powder_keg_keg.
- Blast Proof has a padded vest: the body is quilted tan padding, with dark
  seams.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/illager.png"

# The top left pixel and the size of each face of each part.
HEAD = {"top": (8, 0, 8, 8), "bottom": (16, 0, 8, 8), "right": (0, 8, 8, 10), "front": (8, 8, 8, 10),
        "left": (16, 8, 8, 10), "back": (24, 8, 8, 10)}
BODY = {"top": (22, 20, 8, 6), "bottom": (30, 20, 8, 6), "right": (16, 26, 6, 12), "front": (22, 26, 8, 12),
        "left": (30, 26, 6, 12), "back": (36, 26, 8, 12)}
COAT = {"top": (6, 38, 8, 6), "bottom": (14, 38, 8, 6), "right": (0, 44, 6, 20), "front": (6, 44, 8, 20),
        "left": (14, 44, 6, 20), "back": (20, 44, 8, 20)}
ARM = {"top": (44, 46, 4, 4), "bottom": (48, 46, 4, 4), "right": (40, 50, 4, 12), "front": (44, 50, 4, 12),
       "left": (48, 50, 4, 12), "back": (52, 50, 4, 12)}
LEG = {"top": (4, 22, 4, 4), "bottom": (8, 22, 4, 4), "right": (0, 26, 4, 12), "front": (4, 26, 4, 12),
       "left": (8, 26, 4, 12), "back": (12, 26, 4, 12)}
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES

EYES = {(9, 14), (10, 14), (13, 14), (14, 14)}
# The rows of the coat that the pillager draws.
COAT_ROWS = range(9, 17)

STEEL = ("#B8C0C4", "#7A8288", "#4A5056")
LEATHER = ("#7A4A2A", "#5A3418", "#9A6A40")


def pixels(part, faces):
    """Return each pixel (x, y, face, col, row) of the faces of a part."""
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                yield x0 + col, y0 + row, face, col, row


def piercer():
    # The bracer is the gene model piercer_bracer.
    return []


def piercer_model():
    # A bracer of steel on the right arm, which holds the crossbow: a steel
    # cuff round the forearm, with a pale upper edge and a row of dark rivets.
    # On the arm bone, the arm is from y -2 to 10, and from x -3 to 1.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def steel(face, x, y):
        if face in ("top", "bottom"):
            return STEEL[1]
        if y == 0:
            return STEEL[0]
        return STEEL[2] if y == 2 and x % 2 == 0 else STEEL[1]

    # The bracer: 4x4x4 at uv (0, 0).
    paint_box(im, 0, 0, 4, 4, 4, steel)
    parts = [{"bone": "right_arm", "uv": [0, 0], "from": [-3, 4, -2], "size": [4, 4, 4], "inflate": 0.35}]
    save_model("piercer_bracer", im, parts)


def long_bolt():
    # The quiver is the gene model long_bolt_quiver.
    return []


def long_bolt_model():
    # A quiver of long bolts on the back: a leather quiver that hangs at a
    # slant across the back, and three long bolts that stand out of its top,
    # with pale wood shafts and white fletching. On the body bone, the back
    # is at z 3, and the coat is 0.5 pixel larger.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def quiver(face, x, y):
        if y == 0 or y == 7:
            return LEATHER[2]
        return LEATHER[1] if face in ("right", "back") else LEATHER[0]

    def bolt(face, x, y):
        return "#F0F0E8" if y == 0 else "#B89060"

    # The quiver: 3x8x2 at uv (0, 0). A bolt: 1x5x1 at uv (12, 0).
    paint_box(im, 0, 0, 3, 8, 2, quiver)
    paint_box(im, 12, 0, 1, 5, 1, bolt)
    pivot = [0, 6, 4.5]
    parts = [{"bone": "body", "uv": [0, 0], "from": [-1.5, 2, 3.5], "size": [3, 8, 2], "pivot": pivot,
              "rotation": [0, 0, 25]}]
    for x in (-1.2, 0, 1.2):
        parts.append({"bone": "body", "uv": [12, 0], "from": [x - 0.5, -2, 4], "size": [1, 5, 1],
                      "inflate": -0.15, "pivot": pivot, "rotation": [0, 0, 25]})
    save_model("long_bolt_quiver", im, parts)


def heavy_bolt():
    # Thick leather wraps on the arms: three wraps of brown leather round
    # each arm, each two rows wide with a dark lower edge, on rows 1 to 9.
    band = Mark(LEATHER[0])
    edge = Mark(LEATHER[1])
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row in (1, 4, 7):
            band.put((x, y))
        elif row in (2, 5, 8):
            edge.put((x, y))
    return [band, edge]


def overdraw():
    # A scar over one eye: a short, pale red scar across the right eye of
    # the face (x 9 and 10), from the brow to the cheek, with a dark edge. It
    # crosses the eye, but it does not change the eye pixels.
    scar = Mark("#D88A8A")
    edge = Mark("#8A4A4A")
    scar.put([(11, 11), (11, 12), (10, 13), (9, 15), (9, 16)])
    edge.put([(12, 11), (11, 13), (10, 15)])
    return [scar, edge]


def quick_charge():
    # Worn, dark gloves: the hands are dark leather, with pale, worn spots on
    # the knuckles.
    glove = Mark("#2A2420")
    worn = Mark("#5A4A3A")
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row >= 10:
            (worn if row == 10 and col % 2 == 1 else glove).put((x, y))
    for x, y, face, col, row in pixels(ARM, ("bottom",)):
        glove.put((x, y))
    return [glove, worn]


def splinter():
    # A belt of jagged bolts: short bolts that stand in the belt round the
    # body, each a pale wood shaft on rows 6 and 7 with a jagged steel tip on
    # row 5 or 6.
    shaft = Mark("#B89060")
    tip = Mark(STEEL[0])
    for x, y, face, col, row in pixels(BODY, SIDES):
        if col % 2 == 0:
            high = (col // 2) % 2 == 0
            if row == (5 if high else 6):
                tip.put((x, y))
            elif row in ((6, 7) if high else (7,)):
                shaft.put((x, y))
    return [shaft, tip]


def rocketeer():
    # The rockets are the gene model rocketeer_rockets.
    return []


def rocketeer_model():
    # A bundle of rockets on the back: three firework rockets of red paper,
    # side by side, with gray tips and a leather strap round them. They lie on
    # the back under the head, and lean out a little to the sides. On the body
    # bone, the back is at z 3, and the coat is 0.5 pixel larger.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def rocket(face, x, y):
        if y == 0:
            return "#A0A0A0"
        return "#E8E0D0" if y == 5 else "#C82828" if face in ("front", "left") else "#981818"

    # A rocket: 2x7x2 at uv (0, 0). The strap: 7x1x2 at uv (8, 0).
    paint_box(im, 0, 0, 2, 7, 2, rocket)
    paint_box(im, 8, 0, 7, 1, 2, lambda face, x, y: LEATHER[1] if face != "top" else LEATHER[0])
    parts = []
    for x, turn in ((-3.5, 10), (-1, 0), (1.5, -10)):
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, 0.5, 3.6], "size": [2, 7, 2],
                      "pivot": [x + 1, 6, 4.6], "rotation": [0, 0, turn]})
    parts.append({"bone": "body", "uv": [8, 0], "from": [-3.5, 4, 3.5], "size": [7, 1, 2], "inflate": 0.3})
    save_model("rocketeer_rockets", im, parts)


def flak():
    # A red sash with stars: a red sash, 2 pixels wide, from the left
    # shoulder to the right hip on the front of the body, and from the right
    # shoulder to the left hip on the back, with yellow stars on it.
    sash = Mark("#B82020")
    star = Mark("#F0D040")
    for face, flip in (("front", False), ("back", True)):
        x0, y0, w, h = BODY[face]
        for row in range(h):
            c = 7 - (row * 7) // 11
            if flip:
                c = 7 - c
            for col in (c, c - 1 if not flip else c + 1):
                if 0 <= col < w:
                    (star if row in (2, 6, 10) and col == c else sash).put((x0 + col, y0 + row))
    return [sash, star]


def incendiary():
    # Scorched sleeves: rows 4 to 9 of each arm are black with burns, with
    # orange embers and gray ash, and a ragged upper edge.
    black = Mark("#1A1210")
    ember = Mark("#E86020")
    ash = Mark("#6A6460")
    for x, y, face, col, row in pixels(ARM, SIDES):
        if 4 <= row <= 9 and not (row == 4 and (col + len(face)) % 2):
            n = noise100(x, y, 811)
            (ember if n < 12 else ash if n < 26 else black).put((x, y))
    return [black, ember, ash]


def smoke_shell():
    # Soot on the face: dark smudges of soot on the cheeks and the chin, and
    # on the sides of the head next to the face. They keep off the eyes.
    soot = Mark("#2A2828")
    pale = Mark("#4A4646")
    for x, y in ((8, 15), (9, 16), (8, 16), (14, 16), (15, 15), (15, 16), (10, 17), (13, 17), (9, 12), (14, 11)):
        soot.put((x, y))
    for x, y in ((11, 15), (12, 17), (8, 17), (15, 17), (6, 15), (7, 16), (16, 16), (17, 15)):
        pale.put((x, y))
    return [soot, pale]


def powder_keg():
    # The keg is the gene model powder_keg_keg.
    return []


def powder_keg_model():
    # A small keg on the belt: a wooden keg at the left hip, with dark iron
    # hoops and a short fuse on its top. On the body bone, the left side is
    # at x 4, the coat is 0.5 pixel larger, and the belt is at y 8.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def keg(face, x, y):
        if face in ("top", "bottom"):
            return "#8A5A30"
        if y in (0, 4):
            return "#3A3A3A"
        return "#9A6A3A" if x % 2 else "#7A4E28"

    # The keg: 4x5x4 at uv (0, 0). The fuse: 1x2x1 at uv (16, 0).
    paint_box(im, 0, 0, 4, 5, 4, keg)
    paint_box(im, 16, 0, 1, 2, 1, lambda face, x, y: "#F0A030" if y == 0 else "#E8E0C8")
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [4.3, 6.5, -2], "size": [4, 5, 4]},
        {"bone": "body", "uv": [16, 0], "from": [5.8, 5, -0.5], "size": [1, 2, 1], "inflate": -0.2},
    ]
    save_model("powder_keg_keg", im, parts)


def blast_proof():
    # A padded vest: the body is quilted tan padding on rows 0 to 7, with
    # dark seams on every third row and column.
    pad = Mark("#9A8060")
    seam = Mark("#6A5440")
    for x, y, face, col, row in pixels(BODY, ("top",) + SIDES):
        if face == "top" or row <= 7:
            (seam if row % 3 == 2 or col % 3 == 2 else pad).put((x, y))
    return [pad, seam]


GENES = {
    "piercer": piercer,
    "long_bolt": long_bolt,
    "heavy_bolt": heavy_bolt,
    "overdraw": overdraw,
    "quick_charge": quick_charge,
    "splinter": splinter,
    "rocketeer": rocketeer,
    "flak": flak,
    "incendiary": incendiary,
    "smoke_shell": smoke_shell,
    "powder_keg": powder_keg,
    "blast_proof": blast_proof,
}

MODELS = {
    "piercer": "originofspawns:piercer_bracer",
    "long_bolt": "originofspawns:long_bolt_quiver",
    "rocketeer": "originofspawns:rocketeer_rockets",
    "powder_keg": "originofspawns:powder_keg_keg",
}

MODEL_MAKERS = [piercer_model, long_bolt_model, rocketeer_model, powder_keg_model]


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for make in MODEL_MAKERS:
        make()
    for gene, make in GENES.items():
        write(gene, make(), MODELS, REGION_MAP)
    owners = {}
    for gene in GENES:
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in changed_pixels(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = {}
    for (where, _), genes in owners.items():
        if len(genes) > 1:
            key = where + ": " + " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more pillager genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
