"""Make the gene visuals of the vindicator, from design/species/vindicator.md.

Usage: python3 tools/make_vindicator_marks.py (from the root of the repository)

The vindicator texture is 64x64, with the illager layout, and it uses the
illager region map. make_pillager_marks.py gives the faces of the head, the
body, the coat, the arms, and the legs. On the vindicator:
- The coat covers the whole body. Rows 0 to 12 of its sides are drawn, with a
  V neck at the top of the front. So the marks on the body go on the coat.
- The trousers are rows 0 to 8 of the legs, and the boots are rows 9 to 11.
- When it does not attack, it crosses its arms: the part "arms" shows, with a
  box of 4x8x4 at uv (44, 22) for each upper arm and a bar of 8x4x4 at uv
  (40, 38) for the forearms and the hands. The arms right_arm and left_arm
  (uv (40, 46)) show only when it attacks. So each mark on the arms or the
  hands goes on both.
The gene models use the bones head, body, right_arm, left_arm, right_leg, and
left_leg. A model on an arm shows only while the arms are apart.

Pixel plan for the Butchers.
- Heavy Swing has thick, scarred forearms: bare gray forearms with pale red
  scars, on the lower arms and on the crossed forearms.
- Sunder has iron knuckles: steel plates with dark rivets on the knuckles.
- Gash: a bloody apron on the front of the body, the gene model gash_apron.
- Hatchet: a row of three hatchets on the belt at the back, the gene model
  hatchet_hatchets.
- Chop has rolled-up sleeves: a pale roll of cloth at the elbow, and bare
  gray forearms under it.
- Decapitator: a black hood with eye holes over the head, the gene model
  decapitator_hood.

Pixel plan for the Berserkers.
- Bloodlust has red war paint: red bands on the cheeks and a red band on the
  brow.
- Rage has bared teeth: white teeth with dark gaps at the sides of the mouth,
  beside the nose.
- Death Wish has many old scars: pale scars with stitches across the coat.
- Howl has a wide, open mouth: a dark, wide mouth with a red tongue.
- Fury has red veins: red veins that branch on the sleeves.
- Thick Blood has dark red cloth: the coat is dark red, with darker seams.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_illager_region_map import HAND_PIXELS
from make_pillager_marks import ALL, ARM, BODY, COAT, HEAD, LEG, SIDES, STEEL, pixels
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/illager.png"

# The crossed arms: a box for each upper arm, and a bar for the forearms and
# the hands.
CROSSED_ARM = {"top": (48, 22, 4, 4), "bottom": (52, 22, 4, 4), "right": (44, 26, 4, 8), "front": (48, 26, 4, 8),
               "left": (52, 26, 4, 8), "back": (56, 26, 4, 8)}
CROSSED_BAR = {"top": (44, 38, 8, 4), "bottom": (52, 38, 8, 4), "right": (40, 42, 4, 4), "front": (44, 42, 8, 4),
               "left": (52, 42, 4, 4), "back": (56, 42, 8, 4)}
# The rows of the coat that the vindicator draws, and the clear V neck at the
# top of its front.
COAT_ROWS = range(0, 13)
V_NECK = {(1, 0), (2, 0), (3, 0), (4, 0), (5, 0), (6, 0), (2, 1), (3, 1), (4, 1), (5, 1), (3, 2), (4, 2)}

SKIN = ("#959B9B", "#7A8080")
SCAR = ("#C88A8A", "#8A5A5A")


def coat_pixels(faces=SIDES):
    """Return each drawn pixel (x, y, face, col, row) of the sides of the coat."""
    for x, y, face, col, row in pixels(COAT, faces):
        if row in COAT_ROWS and not (face == "front" and (col, row) in V_NECK):
            yield x, y, face, col, row


def sleeve_pixels(first=0, last=9):
    """Return the sleeve pixels (x, y, face, col, row) of the arms from row first to row last. The row of a pixel of
    the crossed arms is the row of the same place on an arm that hangs: the upper arm box is rows 0 to 7 (its own
    rows), and the bar is the forearm, rows 8 and 9."""
    for x, y, face, col, row in pixels(ARM, SIDES):
        if first <= row <= last:
            yield x, y, face, col, row
    for x, y, face, col, row in pixels(CROSSED_ARM, SIDES):
        if first <= row <= last:
            yield x, y, face, col, row
    if last >= 8:
        for x, y, face, col, row in pixels(CROSSED_BAR, ALL):
            if (x, y) not in HAND_PIXELS:
                yield x, y, face, col, 8 + row % 2


def hand_pixels():
    """Return the hand pixels (x, y, face, col, row) of the arms that hang (rows 10 and 11, and the bottom) and of the
    crossed arms."""
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row >= 10:
            yield x, y, face, col, row
    for x, y, face, col, row in pixels(ARM, ("bottom",)):
        yield x, y, face, col, 11
    for x, y, face, col, row in pixels(CROSSED_BAR, ALL):
        if (x, y) in HAND_PIXELS:
            yield x, y, face, col, 10 + row % 2


def heavy_swing():
    # Thick, scarred forearms: the forearms (rows 5 to 9, and the crossed
    # forearms) are bare gray skin, with short pale red scars across them.
    skin = Mark(SKIN[0])
    scar = Mark(SCAR[0])
    for x, y, face, col, row in sleeve_pixels(5, 9):
        (scar if (col + 2 * row) % 5 == 0 else skin).put((x, y))
    return [skin, scar]


def sunder():
    # Iron knuckles: on the knuckles (row 10 of the hands, and the top row of
    # the crossed hands), steel plates with a dark rivet on every other pixel.
    steel = Mark(STEEL[0])
    rivet = Mark(STEEL[2])
    for x, y, face, col, row in hand_pixels():
        if row == 10:
            (rivet if col % 2 else steel).put((x, y))
    return [steel, rivet]


def gash():
    # The apron is the gene model gash_apron.
    return []


def gash_model():
    # A bloody apron on the front of the body: an apron of pale canvas over
    # the coat, from the chest to the knees, with dark red stains and drips of
    # blood, and two straps up to the shoulders. On the body bone, the front
    # of the coat is at z -3.5.
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def apron(face, x, y):
        if face != "front":
            return "#B8B0A0"
        stain = noise100(x // 2, y // 2, 1013) < 30 or (x, y) in ((2, 9), (2, 10), (5, 8), (5, 9))
        if stain:
            return "#7A1010" if (x + y) % 3 else "#A82020"
        return "#D8D0C0" if (x + y) % 4 else "#C8C0B0"

    def strap(face, x, y):
        return "#8A7A60"

    # The apron: 6x10x1 at uv (0, 0). A strap: 1x3x1 at uv (16, 0).
    paint_box(im, 0, 0, 6, 10, 1, apron)
    paint_box(im, 16, 0, 1, 3, 1, strap)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-3, 3, -4.2], "size": [6, 10, 1]},
        {"bone": "body", "uv": [16, 0], "from": [-3, 0, -4.2], "size": [1, 3, 1], "inflate": -0.2},
        {"bone": "body", "uv": [16, 0], "from": [2, 0, -4.2], "size": [1, 3, 1], "inflate": -0.2},
    ]
    save_model("gash_apron", im, parts)


def hatchet():
    # The hatchets are the gene model hatchet_hatchets.
    return []


def hatchet_model():
    # A row of hatchets on the belt at the back: three small hatchets that
    # hang in a row on the back of the coat, each a wooden handle with an
    # iron head at its top, and a leather belt that holds them. On the body
    # bone, the back of the coat is at z 3.5.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def handle(face, x, y):
        return "#8A5A30" if face in ("back", "left") else "#6A4220"

    def head(face, x, y):
        if face == "back" and x == 1:
            return STEEL[0]
        return STEEL[1] if face in ("back", "top") else STEEL[2]

    # A handle: 1x5x1 at uv (0, 0). A head: 2x2x1 at uv (4, 0). The belt:
    # 8x1x1 at uv (12, 0).
    paint_box(im, 0, 0, 1, 5, 1, handle)
    paint_box(im, 4, 0, 2, 2, 1, head)
    paint_box(im, 12, 0, 8, 1, 1, lambda face, x, y: "#3A2A1A")
    parts = [{"bone": "body", "uv": [12, 0], "from": [-4, 7, 3.4], "size": [8, 1, 1], "inflate": 0.15}]
    for x in (-3, -0.5, 2):
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, 6, 3.7], "size": [1, 5, 1]})
        parts.append({"bone": "body", "uv": [4, 0], "from": [x - 0.5, 5.5, 4.2], "size": [2, 2, 1]})
    save_model("hatchet_hatchets", im, parts)


def chop():
    # Rolled-up sleeves: a pale roll of cloth at the elbow (rows 4 and 5, and
    # the ends of the crossed upper arms), and bare gray forearms under it.
    roll = Mark("#6A645E")
    shade = Mark("#4A4440")
    skin = Mark(SKIN[0])
    for x, y, face, col, row in sleeve_pixels(4, 9):
        if row == 4:
            roll.put((x, y))
        elif row == 5:
            shade.put((x, y))
        else:
            skin.put((x, y))
    return [roll, shade, skin]


def decapitator():
    # The hood is the gene model decapitator_hood.
    return []


def decapitator_model():
    # A black hood with eye holes: a hood of black cloth over the whole head,
    # a little larger than the head, with a point on its top. Two holes show
    # the eyes, and the hood is open under the chin. The nose sticks out
    # through it. On the head bone, the head is from y -10 to 0.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def hood(face, x, y):
        if face == "bottom":
            return None
        if face == "front" and y == 6 and x in (1, 2, 5, 6):
            return None
        if face == "front" and y >= 8 and 2 <= x <= 5:
            return None
        return "#1A1818" if (x + 2 * y) % 5 else "#2A2626"

    # The hood: 8x10x8 at uv (0, 0). The point: 2x2x2 at uv (32, 0).
    paint_box(im, 0, 0, 8, 10, 8, hood)
    paint_box(im, 32, 0, 2, 2, 2, lambda face, x, y: "#1A1818")
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-4, -10, -4], "size": [8, 10, 8], "inflate": 0.55},
        {"bone": "head", "uv": [32, 0], "from": [-1, -12, -1], "size": [2, 2, 2]},
    ]
    save_model("decapitator_hood", im, parts)


def bloodlust():
    # Red war paint: two short red bands on each cheek, under the eyes, and a
    # red band across the brow above the eyebrows.
    red = Mark("#B81818")
    red.put([(8, 15), (9, 15), (8, 16), (14, 15), (15, 15), (15, 16)])
    red.put([(x, 11) for x in range(9, 15)])
    return [red]


def rage():
    # Bared teeth: the mouth is wider, with white teeth and dark gaps at the
    # sides of the mouth (x 9, 10, 13, and 14). The nose hides the middle of
    # the mouth.
    tooth = Mark("#E8E8E0")
    gap = Mark("#201818")
    tooth.put([(9, 16), (10, 17), (13, 17), (14, 16)])
    gap.put([(10, 16), (9, 17), (13, 16), (14, 17)])
    return [tooth, gap]


def death_wish():
    # Many old scars: short, pale scars across the coat, each a diagonal line
    # of 3 pixels with a dark stitch across its middle.
    scar = Mark(SCAR[0])
    stitch = Mark(SCAR[1])
    starts = {"front": ((1, 4), (5, 7), (2, 10)), "back": ((1, 2), (4, 5), (2, 9), (5, 10)),
              "right": ((1, 3), (2, 8)), "left": ((2, 4), (1, 9))}
    for face, spots in starts.items():
        x0, y0 = COAT[face][:2]
        for c, r in spots:
            scar.put([(x0 + c, y0 + r), (x0 + c + 2, y0 + r + 2)])
            stitch.put((x0 + c + 1, y0 + r + 1))
    return [scar, stitch]


def howl():
    # A wide, open mouth: a dark mouth on rows 15 to 17, wider than the
    # vanilla mouth, with a red tongue at its bottom. The nose hides the
    # middle of the mouth, so the mouth shows at its sides.
    dark = Mark("#140A0A")
    tongue = Mark("#A83030")
    dark.put([(x, y) for x in range(9, 15) for y in (15, 16)])
    dark.put([(9, 17), (14, 17)])
    tongue.put([(10, 17), (13, 17)])
    return [dark, tongue]


def fury():
    # Red veins: on each face of the sleeves, a red vein that runs down the
    # arm and branches twice.
    vein = Mark("#C02020")
    for x, y, face, col, row in sleeve_pixels(0, 9):
        main = col == (1 if row < 4 else 2)
        branch = (row, col) in ((3, 0), (6, 3), (7, 3), (8, 0))
        if main or branch:
            vein.put((x, y))
    return [vein]


def thick_blood():
    # Dark red cloth: the coat is dark red, with darker seams on its edges and
    # down the middle of its front.
    red = Mark("#5A1414")
    seam = Mark("#3A0C0C")
    for x, y, face, col, row in coat_pixels():
        w = COAT[face][2]
        edge = col in (0, w - 1) or face == "front" and col in (3, 4)
        (seam if edge else red).put((x, y))
    return [red, seam]


GENES = {
    "heavy_swing": heavy_swing,
    "sunder": sunder,
    "gash": gash,
    "hatchet": hatchet,
    "chop": chop,
    "decapitator": decapitator,
    "bloodlust": bloodlust,
    "rage": rage,
    "death_wish": death_wish,
    "howl": howl,
    "fury": fury,
    "thick_blood": thick_blood,
}

MODELS = {
    "gash": "originofspawns:gash_apron",
    "hatchet": "originofspawns:hatchet_hatchets",
    "decapitator": "originofspawns:decapitator_hood",
}

MODEL_MAKERS = [gash_model, hatchet_model, decapitator_model]


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
    print("pixels that two or more vindicator genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
