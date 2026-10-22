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

Pixel plan for the Captains.
- Banner Bearer: a small ominous banner on a pole on the back, the gene
  model banner_bearer_banner.
- War Horn: a horn on a cord at the left hip, the gene model war_horn_horn,
  and the cord across the chest and the back.
- Rally Cry has a red plume: a red crest along the middle of the top of the
  head, from the brow to the back.
- Patrol Leader has a dark hood: the top, the sides, and the back of the
  head are a dark hood, with a frame round the face.
- Grudge has a black band over the brow: a black band round the head, over
  the brows, with a knot and two tails at the back.
- Formation has a gray tabard: a gray tabard with dark edges on the front
  and the back of the body, under the belt, and on the coat.

Pixel plan for the Scouts.
- Fleet Foot has light boots: the boots are pale tan leather, with laces on
  the front and dark soles.
- Spyglass: a brass spyglass at the right hip, the gene model spyglass_glass.
- Pathseeker has pale, sharp eyes: pale, icy eyes, with a pale point at the
  outer corner of each eye.
- Evasion has patched trousers: square patches of other cloth, with pale
  stitches, on the rows of the trousers under the coat.
- Camouflage has a cloak of leaves: green leaves on the body, the coat, and
  the shoulders.
- Signal Smoke has a smoke pot on the belt: a dark pot at the back of the
  belt, with puffs of gray smoke above it on the back.

Pixel plan for the Looters. The coat hides rows 9 to 11 of the body, so the
things on the belt are on body rows 4 to 8 and on the coat.
- Pickpocket has long, quick fingers: pale, long fingers with dark gaps on
  the hands, and on the end of the arm.
- Plunder: a full sack on the back, the gene model plunder_sack.
- Hunger Raid has a belt of empty food bags: small, limp bags of pale cloth
  that hang from the belt over the coat.
- Saboteur has a file and pliers on the belt: a steel file at the right hip,
  and pliers with red handles at the left hip.
- Greed has a gold tooth at the corner of the mouth, beside the nose.
- Escape Artist has torn trousers: holes and frayed threads on the rows of
  the trousers under the coat.
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

def banner_bearer():
    # The banner is the gene model banner_bearer_banner.
    return []


def banner_bearer_model():
    # A small ominous banner on the back: a dark pole that rises behind the
    # head from a holder on the back, a crossbar at its top, and a small
    # banner that hangs from the crossbar, white with a cyan diamond, a gray
    # border, and black bands, as the ominous banner. On the body bone, the
    # back is at z 3, the coat is 0.5 pixel larger, and the back of the head
    # is at z 4.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def pole(face, x, y):
        return "#4A3A2A" if face in ("right", "back") else "#6A5038"

    def cloth(face, x, y):
        if face not in ("front", "back"):
            return "#A8A8A0"
        if y in (0, 7):
            return "#202020"
        if x in (0, 4):
            return "#8A8A8A"
        if (x, y) in ((2, 2), (1, 3), (3, 3), (2, 4), (2, 3)):
            return "#3A9A9A"
        return "#202020" if y == 5 else "#E8E8E0"

    # The pole: 1x20x1 at uv (0, 0). The crossbar: 6x1x1 at uv (8, 0). The
    # banner: 5x8x1 at uv (8, 4). The holder: 2x2x2 at uv (24, 0).
    paint_box(im, 0, 0, 1, 20, 1, pole)
    paint_box(im, 8, 0, 6, 1, 1, pole)
    paint_box(im, 8, 4, 5, 8, 1, cloth)
    paint_box(im, 24, 0, 2, 2, 2, lambda face, x, y: LEATHER[1])
    parts = [
        {"bone": "body", "uv": [24, 0], "from": [-1, 5, 3.4], "size": [2, 2, 2], "inflate": 0.1},
        {"bone": "body", "uv": [0, 0], "from": [-0.5, -12, 4.4], "size": [1, 20, 1]},
        {"bone": "body", "uv": [8, 0], "from": [-3, -12, 4.4], "size": [6, 1, 1]},
        {"bone": "body", "uv": [8, 4], "from": [-2.5, -11, 5.4], "size": [5, 8, 1], "inflate": -0.1},
    ]
    save_model("banner_bearer_banner", im, parts)


def war_horn():
    # The cord of the horn of the gene model war_horn_horn: a pale cord from
    # the right shoulder to the left hip on the front of the body, and from
    # the left hip back up to the right shoulder on the back.
    cord = Mark("#D8C8A0")
    for face, flip in (("front", False), ("back", True)):
        x0, y0, w, h = BODY[face]
        for row in range(8):
            col = row if not flip else 7 - row
            cord.put((x0 + col, y0 + row))
    return [cord]


def war_horn_model():
    # A horn on a cord: a curved horn of pale bone at the left hip, with a
    # brass band, a wide bell that points out to the side, and a thin
    # mouthpiece. On the body bone, the front is at z -3, the left side is at
    # x 4, and the coat is 0.5 pixel larger.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def horn(face, x, y):
        if x == 1:
            return "#C8A040"
        return "#E8DCB8" if face in ("top", "front") else "#C8B890"

    def bell(face, x, y):
        return "#2A2018" if face == "left" else "#E8DCB8" if face in ("top", "front") else "#C8B890"

    # The horn: 4x2x2 at uv (0, 0). The bell: 2x3x3 at uv (16, 0). The
    # mouthpiece: 1x1x1 at uv (0, 8).
    paint_box(im, 0, 0, 4, 2, 2, horn)
    paint_box(im, 16, 0, 2, 3, 3, bell)
    paint_box(im, 0, 8, 1, 1, 1, lambda face, x, y: "#C8A040")
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [0.5, 8, -5.5], "size": [4, 2, 2]},
        {"bone": "body", "uv": [16, 0], "from": [4.3, 7.5, -6], "size": [2, 3, 3]},
        {"bone": "body", "uv": [0, 8], "from": [-0.5, 8.5, -5], "size": [1, 1, 1]},
    ]
    save_model("war_horn_horn", im, parts)


def rally_cry():
    # A red plume: a red crest, two pixels wide, along the middle of the top
    # of the head from the brow to the back, that runs down the top two rows
    # of the back of the head, with bright red on alternate pixels.
    red = Mark("#C81818")
    bright = Mark("#F05050")
    for row in range(8):
        for x in (11, 12):
            (bright if (row + x) % 2 else red).put((x, row))
    for y in (8, 9):
        for x in (27, 28):
            red.put((x, y))
    red.put([(11, 8), (12, 8)])
    return [red, bright]


def patrol_leader():
    # A dark hood: the top of the head, rows 0 to 7 of the sides and the
    # back, and a frame round the face (the top two rows and the outer
    # columns down to row 6) are a dark hood, with paler folds.
    hood = Mark("#1E1A24")
    fold = Mark("#34303C")
    for x, y, face, col, row in pixels(HEAD, ("top", "right", "left", "back")):
        if face == "top" or row <= 7:
            (fold if (col + 2 * row) % 7 == 0 else hood).put((x, y))
    for x, y, face, col, row in pixels(HEAD, ("front",)):
        if row <= 1 or col in (0, 7) and row <= 6:
            hood.put((x, y))
    return [hood, fold]


def grudge():
    # A black band over the brow: a black band on rows 2 and 3 of all the
    # sides of the head, over the brows, and a knot with two short tails in
    # the middle of the back.
    band = Mark("#121212")
    for x, y, face, col, row in pixels(HEAD, SIDES):
        if row in (2, 3):
            band.put((x, y))
        elif face == "back" and row in (4, 5) and col in (3, 4):
            band.put((x, y))
    return [band]


def formation():
    # A gray tabard: a gray tabard with dark edges on columns 1 to 6 of the
    # front and the back of the body, on rows 0 to 7 and 10 and 11 (the belt
    # stays on rows 8 and 9), and on the drawn rows of the coat.
    gray = Mark("#80868A")
    edge = Mark("#55585C")
    for part, faces, rows in ((BODY, ("front", "back"), (0, 1, 2, 3, 4, 5, 6, 7, 10, 11)),
                              (COAT, ("front", "back"), COAT_ROWS)):
        for x, y, face, col, row in pixels(part, faces):
            if 1 <= col <= 6 and row in rows:
                (edge if col in (1, 6) else gray).put((x, y))
    return [gray, edge]


def fleet_foot():
    # Light boots: the boots (rows 6 to 11 of the legs) are pale tan leather,
    # with crossed laces on the front and dark soles.
    tan = Mark("#C8A878")
    lace = Mark("#7A5A38")
    sole = Mark("#4A3A28")
    for x, y, face, col, row in pixels(LEG, SIDES):
        if row == 11:
            sole.put((x, y))
        elif row >= 6:
            lace_here = face == "front" and row in (7, 9) and col in (1, 2)
            (lace if lace_here else tan).put((x, y))
    for x, y, face, col, row in pixels(LEG, ("bottom",)):
        sole.put((x, y))
    return [tan, lace, sole]


def spyglass():
    # The spyglass is the gene model spyglass_glass.
    return []


def spyglass_model():
    # A spyglass at the belt: a brass tube that hangs at the right hip, with
    # dark bands, a wide end, and a pale blue lens at the bottom. On the body
    # bone, the right side is at x -4, the coat is 0.5 pixel larger, and the
    # belt is at y 8.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def tube(face, x, y):
        if face == "bottom":
            return "#A8D8E8"
        if y in (1, 3):
            return "#8A5A28"
        return "#D8A048" if face in ("front", "right") else "#B07830"

    # The tube: 2x5x2 at uv (0, 0).
    paint_box(im, 0, 0, 2, 5, 2, tube)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-6.3, 6.5, -1], "size": [2, 5, 2],
              "pivot": [-5.3, 6.5, 0], "rotation": [0, 0, -10]}]
    save_model("spyglass_glass", im, parts)


def pathseeker():
    # Pale, sharp eyes: the pupils are pale and icy, and a pale point at the
    # outer corner of each eye makes the eyes look long and sharp.
    ice = Mark("#D8FAFF")
    corner = Mark("#B8E0E8")
    ice.put([(10, 14), (13, 14)])
    corner.put([(8, 14), (15, 14)])
    return [ice, corner]


def evasion():
    # Patched trousers: square patches of 2x2 pixels of other cloth, green and
    # brown, with pale stitches at their corners. The coat hides rows 0 to 3
    # of the trousers, so the patches are on rows 4 and 5, which show.
    green = Mark("#5A8A5A")
    brown = Mark("#9A6A3A")
    stitch = Mark("#E8E0C0")
    spots = {"front": ((0, 4, green), (2, 4, brown)), "right": ((1, 4, brown),), "left": ((1, 4, green),),
             "back": ((0, 4, brown), (2, 4, green))}
    for face, patches in spots.items():
        x0, y0, w, h = LEG[face]
        for col, row, mark in patches:
            mark.put([(x0 + col, y0 + row), (x0 + col + 1, y0 + row), (x0 + col, y0 + row + 1),
                      (x0 + col + 1, y0 + row + 1)])
            stitch.put([(x0 + col, y0 + row), (x0 + col + 1, y0 + row + 1)])
    return [green, brown, stitch]


def camouflage():
    # A cloak of leaves: green leaves of three shades on all the faces of the
    # body, on the drawn rows of the coat, and on the top two rows of the
    # arms, as a cloak over the shoulders.
    dark = Mark("#2A4A1A")
    mid = Mark("#3A6A2A")
    light = Mark("#5A8A3A")
    spots = list(pixels(BODY, ALL)) + [p for p in pixels(COAT, SIDES) if p[4] in COAT_ROWS]
    spots += [p for p in pixels(ARM, ("top",) + SIDES) if p[2] == "top" or p[4] <= 1]
    for x, y, face, col, row in spots:
        n = noise100(x, y, 907)
        (light if n < 25 else mid if n < 65 else dark).put((x, y))
    return [dark, mid, light]


def signal_smoke():
    # A smoke pot on the belt: a dark pot at the middle of the back, on the
    # belt (rows 8 to 11 of the back of the body), with a pale rim, and three
    # puffs of gray smoke above it on rows 1 to 6.
    pot = Mark("#2E2E2E")
    rim = Mark("#6A6A6A")
    smoke = Mark("#C8C8C8")
    shade = Mark("#9A9A9A")
    x0, y0 = BODY["back"][:2]
    for row in range(8, 12):
        pot.put([(x0 + c, y0 + row) for c in range(2, 6)])
    rim.put([(x0 + c, y0 + 7) for c in range(2, 6)])
    # Three round puffs of 2x2 pixels that rise and drift, each with a shade
    # on its lower right pixel.
    for c, r in ((2, 5), (4, 3), (3, 1)):
        smoke.put([(x0 + c, y0 + r), (x0 + c + 1, y0 + r), (x0 + c, y0 + r + 1)])
        shade.put((x0 + c + 1, y0 + r + 1))
    return [pot, rim, smoke, shade]


def pickpocket():
    # Long, quick fingers: on the hands (rows 10 and 11 of the arm), and up
    # onto row 9, pale fingers with dark gaps between them. The end of the
    # arm, which points forward, shows four fingers.
    finger = Mark("#B8C0C0")
    gap = Mark("#3A3E3E")
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row >= 9:
            (finger if col % 2 == 0 else gap if row >= 10 else finger).put((x, y))
    for x, y, face, col, row in pixels(ARM, ("bottom",)):
        (finger if col % 2 == 0 else gap).put((x, y))
    return [finger, gap]


def plunder():
    # The sack is the gene model plunder_sack.
    return []


def plunder_model():
    # A full sack on the back: a round sack of burlap, full and heavy, with a
    # dark weave, a patch, and a tied neck at its top. It hangs on the back
    # under the head. On the body bone, the back is at z 3, and the coat is
    # 0.5 pixel larger.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def sack(face, x, y):
        if face == "back" and 1 <= x <= 2 and 2 <= y <= 3:
            return "#6A7A5A"
        if (x + y) % 3 == 0:
            return "#806038"
        return "#A08050" if face in ("top", "back", "left") else "#8A6A40"

    def neck(face, x, y):
        return "#5A4028" if y == 0 else "#A08050"

    # The sack: 6x7x4 at uv (0, 0). The neck: 2x2x2 at uv (24, 0).
    paint_box(im, 0, 0, 6, 7, 4, sack)
    paint_box(im, 24, 0, 2, 2, 2, neck)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-3, 1.5, 3.6], "size": [6, 7, 4], "inflate": 0.3},
        {"bone": "body", "uv": [24, 0], "from": [-1, -0.2, 4.6], "size": [2, 2, 2]},
    ]
    save_model("plunder_sack", im, parts)


def hunger_raid():
    # A belt of empty food bags: small, limp bags of pale cloth, 2 pixels
    # wide, that hang from the belt over the top rows of the coat (rows 9 to
    # 11), each with a dark tie at its top and a fold down its middle.
    bag = Mark("#C8A870")
    fold = Mark("#9A7A48")
    tie = Mark("#3A2A1A")
    for face in ("front", "right", "left", "back"):
        x0, y0, w, h = COAT[face]
        for c0 in ((1, 5) if w == 8 else (2,)):
            tie.put([(x0 + c0, y0 + 9), (x0 + c0 + 1, y0 + 9)])
            bag.put([(x0 + c0, y0 + 10), (x0 + c0, y0 + 11), (x0 + c0 + 1, y0 + 10)])
            fold.put((x0 + c0 + 1, y0 + 11))
    return [bag, fold, tie]


def saboteur():
    # A file and pliers on the belt: a steel file with a wooden handle on the
    # right side of the body, and pliers with steel jaws and red handles on
    # the left side, on rows 4 to 8 and on the top rows of the coat.
    steel = Mark(STEEL[1])
    bright = Mark(STEEL[0])
    wood = Mark("#7A5030")
    red = Mark("#B82020")
    rx, ry = BODY["right"][:2]
    wood.put([(rx + 2, ry + 4), (rx + 2, ry + 5)])
    steel.put([(rx + 2, ry + 6), (rx + 2, ry + 7), (rx + 2, ry + 8)])
    bright.put((rx + 3, ry + 7))
    cx, cy = COAT["right"][:2]
    steel.put([(cx + 2, cy + 9), (cx + 2, cy + 10)])
    lx, ly = BODY["left"][:2]
    steel.put([(lx + 2, ly + 4), (lx + 3, ly + 4), (lx + 2, ly + 5), (lx + 3, ly + 5)])
    bright.put((lx + 2, ly + 4))
    red.put([(lx + 1, ly + 6), (lx + 1, ly + 7), (lx + 4, ly + 6), (lx + 4, ly + 7), (lx + 1, ly + 8),
             (lx + 4, ly + 8)])
    cx, cy = COAT["left"][:2]
    red.put([(cx + 1, cy + 9), (cx + 4, cy + 9)])
    return [steel, bright, wood, red]


def greed():
    # A gold tooth: a tooth at the left corner of the mouth is bright gold,
    # with a darker gold root under it. The nose hides the middle of the
    # mouth (x 11 and 12), so the tooth is at x 13.
    gold = Mark("#F0C830")
    root = Mark("#B08A20")
    gold.put((13, 16))
    root.put((13, 17))
    return [gold, root]


def escape_artist():
    # Torn trousers: on the rows of the trousers under the coat (rows 4 and
    # 5 of the legs), holes that show the gray skin, and dark frayed threads
    # that hang down onto the top row of the boots.
    hole = Mark("#959B9B")
    fray = Mark("#0E1414")
    for x, y, face, col, row in pixels(LEG, SIDES):
        k = (col + len(face)) % 4
        if row == 5 and k in (0, 1) or row == 4 and k == 1:
            hole.put((x, y))
        elif row == 6 and k == 2:
            fray.put((x, y))
        elif row == 5 and k == 3:
            fray.put((x, y))
    return [hole, fray]



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
    "banner_bearer": banner_bearer,
    "war_horn": war_horn,
    "rally_cry": rally_cry,
    "patrol_leader": patrol_leader,
    "grudge": grudge,
    "formation": formation,
    "fleet_foot": fleet_foot,
    "spyglass": spyglass,
    "pathseeker": pathseeker,
    "evasion": evasion,
    "camouflage": camouflage,
    "signal_smoke": signal_smoke,
    "pickpocket": pickpocket,
    "plunder": plunder,
    "hunger_raid": hunger_raid,
    "saboteur": saboteur,
    "greed": greed,
    "escape_artist": escape_artist,
}

MODELS = {
    "piercer": "originofspawns:piercer_bracer",
    "long_bolt": "originofspawns:long_bolt_quiver",
    "rocketeer": "originofspawns:rocketeer_rockets",
    "powder_keg": "originofspawns:powder_keg_keg",
    "banner_bearer": "originofspawns:banner_bearer_banner",
    "war_horn": "originofspawns:war_horn_horn",
    "spyglass": "originofspawns:spyglass_glass",
    "plunder": "originofspawns:plunder_sack",
}

MODEL_MAKERS = [piercer_model, long_bolt_model, rocketeer_model, powder_keg_model, banner_bearer_model,
                war_horn_model, spyglass_model, plunder_model]


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
