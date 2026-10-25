"""Make the gene visuals of the zombified piglin, from design/species/zombified_piglin.md.

Usage: python3 tools/make_zombified_piglin_marks.py (from the root of the repository)

The zombified piglin, the piglin, and the piglin brute use the piglin region
map and the 64x64 player layout. This script also holds the parts of that
layout, which make_piglin_marks.py and make_piglin_brute_marks.py use:
- the head, 10x8x8 at uv (0, 0). The face is 10 wide and 8 high. The eyes
  are on row 3, in columns 2 and 7. The snout covers columns 3 to 6 of rows
  4 to 7, and the mouth is under it.
- the snout, 4x4x1 at uv (31, 1), and the tusks, 1x2x1 at uv (2, 4) (right)
  and (2, 0) (left).
- the ears, 1x5x4 at uv (39, 6) (right) and (51, 6) (left).
- the body (16, 16), the arms (40, 16) and (32, 48), and the legs (0, 16) and
  (16, 48). Row 0 of an arm is the shoulder, and rows 8 to 11 are the hand.
The zombified piglin and the piglin hold the sword in the right hand, so the
right arm is the sword arm.
The gene models use the bones head, body, right_arm, and left_arm.

Pixel plan for the Vengeful.
- Blood Feud has red, angry eyes under low, dark brows.
- Horde Call has a wide, open snout: dark, wide nostrils and an open mouth.
- Long Memory has a pale scar across the left eye of the face.
- War Squeal has torn ears: dark, ragged tips and notches. The zombified
  piglin has only its left ear, so a torn, red stub is where the right ear
  was.
- Wrath has open, red wounds on the body.
- Avenge: red cloth on the sword arm, the gene model avenge_cloth.

Pixel plan for the Gilded.
- Gold Flesh has gold flecks in the flesh of the body and the arms.
- Gold Plating: gold plates on the chest, the gene model gold_plating_plates.
- Gilded Skull: a gold plate on the skull, the gene model gilded_skull_plate.
- Glint has a shining gold hilt in the sword hand, with white glints.
- Gold Rush has gold stripes down the legs.
- Tarnish has green, tarnished gold on the arms.

Pixel plan for the Fester.
- Rot Bite has rotten, green tusks and rot round the mouth.
- Nether Rot has black rot spots on the body.
- Stench: green fumes, the gene model stench_fumes, and green stains.
- Corpse Gas has a swollen belly: a pale, round belly with a dark edge.
- Golem Rot has rot on the hands.
- Rot Sprint has rotten, thin legs: dark edges and rot spots.

Pixel plan for the Bladesman.
- Keen Edge has a notched sword arm: pale notches down the arm.
- Sweep has wide, strong shoulders: bright shoulders with a dark line under.
- Riposte: a gold guard on the left arm, the gene model riposte_guard.
- Gold Hilt has a gold ring on the wrist of each arm.
- Sword Slap has a flat sword scar: a wide, pale scar across the left arm.
- Gilded Blade has gold dust on the sword arm.

Pixel plan for the Chaser.
- Sprint has long, thin legs: dark edges and a pale middle line.
- Scent Tracker has a large snout: the face round the snout has the color
  of the snout, and the nostrils are large.
- Vault has strong, bent legs: a bright thigh and a dark knee.
- Shrunken has small, round ears: the ears are dark, and a small, round,
  pale ear is high on each side of the head.
- Duck has a low, bent back: a dark hunch with a ridge on the back.
- Pack Run has torn cloth at the knees.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/piglin.png"


def box_faces(u, v, w, h, d):
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


HEAD = box_faces(0, 0, 10, 8, 8)
SNOUT = box_faces(31, 1, 4, 4, 1)
RIGHT_TUSK = box_faces(2, 4, 1, 2, 1)
LEFT_TUSK = box_faces(2, 0, 1, 2, 1)
RIGHT_EAR = box_faces(39, 6, 1, 5, 4)
LEFT_EAR = box_faces(51, 6, 1, 5, 4)
BODY = box_faces(16, 16, 8, 12, 4)
RIGHT_ARM = box_faces(40, 16, 4, 12, 4)
LEFT_ARM = box_faces(32, 48, 4, 12, 4)
RIGHT_LEG = box_faces(0, 16, 4, 12, 4)
LEFT_LEG = box_faces(16, 48, 4, 12, 4)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
EARS = (RIGHT_EAR, LEFT_EAR)
ARMS = (RIGHT_ARM, LEFT_ARM)
LEGS = (RIGHT_LEG, LEFT_LEG)
TUSKS = (RIGHT_TUSK, LEFT_TUSK)
EYES = ((2, 3), (7, 3))


def pixels(part, faces=ALL, cols=None, rows=None):
    """Yield (x, y, face, col, row) for the pixels of the faces of a part."""
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                if (cols is None or col in cols) and (rows is None or row in rows):
                    yield x0 + col, y0 + row, face, col, row


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    return x0 + col, y0 + row


def face(col, row):
    """Return the pixel of the face of the head at a column and a row."""
    return at(HEAD, "front", col, row)


def put(mark, part, faces=ALL, cols=None, rows=None):
    mark.put([(x, y) for x, y, *_ in pixels(part, faces, cols, rows)])
    return mark


def speckle(marks, part, faces, share, seed, cols=None, rows=None):
    """Put the pixels of a part on the marks, at random. share is the part of the pixels, in percent.

    With two marks, the second mark takes a third of the pixels.
    """
    for x, y, f, col, row in pixels(part, faces, cols, rows):
        n = noise100(x, y, seed)
        if n < share:
            (marks[1] if len(marks) > 1 and n < share / 3 else marks[0]).put((x, y))


def write_all(genes, model_genes, region_map=REGION_MAP, label="genes"):
    os.makedirs(MARK_DIR, exist_ok=True)
    for make, _ in model_genes.values():
        make()
    models = {gene: "originofspawns:" + name for gene, (_, name) in model_genes.items()}
    for gene, make in genes.items():
        write(gene, make(), models, region_map)
    owners = {}
    for gene in genes:
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in changed_pixels(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = sum(1 for genes_at in owners.values() if len(genes_at) > 1)
    print(f"pixels that two or more {label} share:", shared)


def model_image(w=32, h=32):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


GOLD = ("#F6D03D", "#C8961E", "#FFF4A0")
RED = ("#B01818", "#5A0808")
ROT = ("#4A6A1E", "#2A3A10")


# The Vengeful.

def blood_feud():
    # Red, angry eyes: each eye is a red block of 2x2 with a bright middle,
    # and dark brows slant down toward the snout.
    red = Mark("#D01010")
    hot = Mark("#FF5030")
    brow = Mark("#3A0A0A")
    for col, row in EYES:
        side = 1 if col < 5 else -1
        hot.put(face(col, row))
        red.put([face(col + side, row), face(col, row + 1), face(col + side, row + 1)])
    brow.put([face(1, 2), face(2, 2), face(3, 2), face(4, 3), face(8, 2), face(7, 2), face(6, 2), face(5, 3)])
    return [red, hot, brow]


def horde_call():
    # A wide, open snout: two large, dark nostrils on the front of the snout,
    # and a dark, open mouth under it, from the low edge of the snout out to
    # the corners of the mouth on the face.
    dark = Mark("#1A0606")
    inner = Mark("#7A1A20")
    dark.put([at(SNOUT, "front", c, r) for c in (0, 3) for r in (1, 2)])
    inner.put([at(SNOUT, "front", c, 3) for c in (1, 2)])
    dark.put([at(SNOUT, "front", c, 3) for c in (0, 3)])
    put(dark, SNOUT, ("bottom",))
    dark.put([face(c, r) for c in (1, 2, 7, 8) for r in (6, 7)])
    inner.put([face(2, 7), face(7, 7)])
    return [dark, inner]


def long_memory():
    # A scar across the eye: a dark-red slash two pixels wide goes down
    # across the brow and the left eye of the face, with a pale middle line.
    slash = Mark("#7A1010")
    pale = Mark("#E8A8A0")
    for row, col in ((0, 5), (1, 5), (2, 6), (3, 7), (4, 7), (5, 8), (6, 9)):
        pale.put(face(col, row))
        slash.put([face(c, row) for c in (col - 1, col + 1) if 0 <= c < 10])
    slash.put([face(4, 0), face(8, 7), face(9, 7)])
    return [slash, pale]


def war_squeal():
    # Torn ears: the low ends of the ears are dark and ragged, with notches.
    # The zombified piglin has only its left ear. Where the right ear was,
    # a torn, red stub is on the top and the side of the head.
    dark = Mark("#3A1010")
    red = Mark("#A02020")
    for ear in EARS:
        for x, y, f, col, row in pixels(ear, SIDES):
            if row >= 3 or row == 2 and col % 2 == 0:
                dark.put((x, y))
            elif row == 1 and col % 3 == 1:
                red.put((x, y))
    # The stub: a ragged, red wound on the right of the top of the head and
    # on the high rows of the right side, with a dark edge.
    wound = Mark("#C02828")
    for col, row in ((0, 2), (1, 2), (0, 3), (1, 3), (2, 3), (0, 4), (1, 4)):
        wound.put(at(HEAD, "top", col, row))
    dark.put([at(HEAD, "top", c, r) for c, r in ((2, 2), (2, 4), (0, 1), (1, 5))])
    wound.put([at(HEAD, "right", c, r) for c, r in ((3, 0), (4, 0), (5, 0), (3, 1), (4, 1), (3, 2), (3, 3), (6, 1))])
    dark.put([at(HEAD, "right", c, r) for c, r in ((2, 0), (6, 0), (2, 1), (5, 1), (4, 2))])
    return [dark, red, wound]


def wrath():
    # Open, red wounds on the body: wounds of 2x2 with a dark middle, on the
    # front and the back.
    red = Mark(RED[0])
    dark = Mark(RED[1])
    for f, spots in (("front", ((1, 2), (5, 5), (2, 8))), ("back", ((4, 1), (1, 6), (5, 9)))):
        for c, r in spots:
            red.put([at(BODY, f, c, r), at(BODY, f, c + 1, r + 1), at(BODY, f, c, r + 1)])
            dark.put(at(BODY, f, c + 1, r))
    for f in ("right", "left"):
        red.put([at(BODY, f, 1, 4), at(BODY, f, 2, 4), at(BODY, f, 2, 5)])
    return [red, dark]


def avenge():
    # The cloth is the gene model avenge_cloth.
    return []


def avenge_model():
    # Red cloth on the sword arm: a red band round the upper arm, and a strip
    # of cloth that hangs from it. On the bone right_arm, the arm is from x -3
    # to 1, y -2 to 10, and z -2 to 2.
    im = model_image()
    paint_box(im, 0, 0, 4, 2, 4, lambda f, x, y: "#C01A1A" if (x + y) % 3 else "#8A0E0E")
    paint_box(im, 0, 8, 1, 4, 1, lambda f, x, y: "#A01414" if y < 3 else "#6A0A0A")
    parts = [{"bone": "right_arm", "uv": [0, 0], "from": [-3, 1, -2], "size": [4, 2, 4], "inflate": 0.3},
             {"bone": "right_arm", "uv": [0, 8], "from": [-3.6, 2.5, 0], "size": [1, 4, 1], "inflate": 0.05}]
    save_model("avenge_cloth", im, parts)


# The Gilded.

def gold_flesh():
    gold = Mark(GOLD[0])
    shine = Mark(GOLD[2])
    speckle([gold, shine], BODY, SIDES, 16, 1101)
    for arm in ARMS:
        speckle([gold, shine], arm, SIDES, 14, 1102)
    return [gold, shine]


def gold_plating():
    # The plates are the gene model gold_plating_plates.
    return []


def gold_plating_model():
    # Gold plates on the chest: two plates side by side, with a dark rim, and
    # a plate across the belly. On the bone body, the body is from x -4 to 4,
    # y 0 to 12, and z -2 to 2.
    im = model_image()
    paint_box(im, 0, 0, 3, 4, 1, lambda f, x, y: GOLD[1] if f == "front" and (x in (0, 2) or y in (0, 3))
              else GOLD[0])
    paint_box(im, 0, 8, 6, 2, 1, lambda f, x, y: GOLD[2] if f == "front" and y == 0 else GOLD[1])
    parts = [{"bone": "body", "uv": [0, 0], "from": [-3.5, 1, -2.6], "size": [3, 4, 1], "inflate": 0.0},
             {"bone": "body", "uv": [0, 0], "from": [0.5, 1, -2.6], "size": [3, 4, 1], "inflate": 0.0},
             {"bone": "body", "uv": [0, 8], "from": [-3, 6, -2.4], "size": [6, 2, 1], "inflate": 0.0}]
    save_model("gold_plating_plates", im, parts)


def gilded_skull():
    # The plate is the gene model gilded_skull_plate.
    return []


def gilded_skull_model():
    # A gold plate on the skull: a gold plate on the top of the head, with a
    # ridge down its middle and rivets at its corners. On the bone head, the
    # head is from x -5 to 5, y -8 to 0, and z -4 to 4.
    im = model_image(64, 32)
    paint_box(im, 0, 0, 8, 1, 7, lambda f, x, y: "#FFF4A0" if f == "top" and x in (0, 7) and y in (0, 6)
              else GOLD[0] if f == "top" else GOLD[1])
    paint_box(im, 0, 16, 2, 1, 7, lambda f, x, y: GOLD[2] if f == "top" else GOLD[1])
    parts = [{"bone": "head", "uv": [0, 0], "from": [-4, -8.6, -3.5], "size": [8, 1, 7], "inflate": 0.0},
             {"bone": "head", "uv": [0, 16], "from": [-1, -9.2, -3.5], "size": [2, 1, 7], "inflate": -0.1}]
    save_model("gilded_skull_plate", im, parts)


def glint():
    # A shining gold hilt: the sword hand is gold, with white glints.
    gold = Mark(GOLD[0])
    glint = Mark("#FFFFFF")
    for x, y, f, col, row in pixels(RIGHT_ARM, SIDES + ("bottom",), rows=range(8, 12)):
        (glint if (col + row) % 4 == 0 and f != "bottom" else gold).put((x, y))
    return [gold, glint]


def gold_rush():
    # Gold stripes down the legs: two stripes on the front and the back, and
    # one on each side.
    gold = Mark(GOLD[0])
    for leg in LEGS:
        put(gold, leg, ("front", "back"), cols=(0, 3))
        put(gold, leg, ("right", "left"), cols=(1, 2), rows=range(0, 12, 2))
    return [gold]


def tarnish():
    # Green, tarnished gold on the arms: patches of green and dull gold.
    green = Mark("#6A8A3A")
    dull = Mark("#A09A40")
    for arm in ARMS:
        for x, y, f, col, row in pixels(arm, SIDES):
            n = noise100(x // 2, y // 2, 1201)
            if n < 40:
                green.put((x, y))
            elif n < 60:
                dull.put((x, y))
    return [green, dull]


# The Fester.

def rot_bite():
    # Rotten tusks: the tusks are green and brown, and rot goes round the
    # mouth and the low edge of the snout.
    rot = Mark(ROT[0])
    dark = Mark(ROT[1])
    for tusk in TUSKS:
        put(rot, tusk, SIDES, rows=(0,))
        put(dark, tusk, SIDES, rows=(1,))
    dark.put([face(c, 7) for c in (2, 3, 6, 7)] + [face(2, 6), face(7, 6)])
    rot.put([at(SNOUT, "front", c, 3) for c in range(4)] + [face(1, 6), face(8, 6), face(1, 7), face(8, 7)])
    return [rot, dark]


def nether_rot():
    # Black rot spots on the body: spots of black and dark purple.
    black = Mark("#0E0A0E")
    purple = Mark("#3A1A3A")
    for x, y, f, col, row in pixels(BODY, SIDES):
        n = noise100(x // 2, y // 2, 1301)
        if n < 22:
            (purple if noise100(x, y, 1302) < 30 else black).put((x, y))
    return [black, purple]


def stench():
    # Green stains on the body. The fumes are the gene model stench_fumes.
    stain = Mark("#7AA040")
    for x, y, f, col, row in pixels(BODY, SIDES):
        if noise100(x // 2, y // 3, 1401) < 20:
            stain.put((x, y))
    return [stain]


def stench_model():
    # Green fumes: puffs of pale green gas that rise from the shoulders and
    # the back, each with a small puff on it. On the bone body, the top of
    # the body is at y 0, and the back is at z 2.
    im = model_image()
    paint_box(im, 0, 0, 2, 2, 2, lambda f, x, y: "#B8E070" if f == "top" else "#8AB850")
    paint_box(im, 0, 8, 1, 1, 1, lambda f, x, y: "#D8F0A0")
    parts = []
    # Two puffs sit on the shoulders, on the tops of the arms at y -2, and
    # one on the back of the body.
    for x, y, z in ((-7, -4, -1), (5, -4, -1), (-1, 1, 2)):
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, y, z], "size": [2, 2, 2], "inflate": 0.0})
        top = (x + 0.5, y - 1, z + 0.5) if y < 0 else (x + 0.5, y + 0.5, z + 2)
        parts.append({"bone": "body", "uv": [0, 8], "from": list(top), "size": [1, 1, 1], "inflate": 0.0})
    save_model("stench_fumes", im, parts)


def corpse_gas():
    # A swollen belly: a pale, round belly on the front of the body, with a
    # dark edge and a bright point.
    pale = Mark("#E8B0A8")
    edge = Mark("#8A4A40")
    shine = Mark("#FFE0D8")
    for x, y, f, col, row in pixels(BODY, ("front",), rows=range(4, 11)):
        dx, dy = col - 3.5, row - 7
        r = dx * dx / 12 + dy * dy / 9
        if r < 0.6:
            (shine if (col, row) == (2, 5) else pale).put((x, y))
        elif r < 1.1:
            edge.put((x, y))
    put(edge, BODY, ("right", "left"), cols=(0,), rows=range(5, 10))
    return [pale, edge, shine]


def golem_rot():
    rot = Mark(ROT[0])
    dark = Mark(ROT[1])
    for arm in ARMS:
        for x, y, f, col, row in pixels(arm, SIDES + ("bottom",), rows=range(7, 12)):
            (dark if noise100(x, y, 1501) < 35 else rot).put((x, y))
    return [rot, dark]


def rot_sprint():
    # Rotten, thin legs: the outer columns of the legs are dark, so the legs
    # look thin, and green rot spots are on them.
    dark = Mark("#2A1A14")
    rot = Mark(ROT[0])
    for leg in LEGS:
        for x, y, f, col, row in pixels(leg, SIDES):
            if col in (0, 3):
                dark.put((x, y))
            elif noise100(x, y, 1601) < 30:
                rot.put((x, y))
    return [dark, rot]


# The Bladesman.

def keen_edge():
    # A notched sword arm: pale notches, in a zigzag, down the front and the
    # outer side of the sword arm.
    notch = Mark("#F4E0D0")
    dark = Mark("#6A2A20")
    for f in ("front", "right"):
        for row in range(1, 12):
            col = (0, 1, 2, 1)[row % 4]
            notch.put(at(RIGHT_ARM, f, col, row))
            if row % 2:
                dark.put(at(RIGHT_ARM, f, col + 1, row))
    return [notch, dark]


def sweep():
    # Wide, strong shoulders: the tops of the arms and the first two rows are
    # bright, with a dark line under them.
    bright = Mark("#F0B8A8")
    dark = Mark("#5A2018")
    for arm in ARMS:
        put(bright, arm, ("top",))
        put(bright, arm, SIDES, rows=(0, 1))
        put(dark, arm, SIDES, rows=(2,))
    put(bright, BODY, ("top",), cols=(0, 1, 6, 7))
    return [bright, dark]


def riposte():
    # The guard is the gene model riposte_guard.
    return []


def riposte_model():
    # A gold guard on the arm: a gold bracer round the left forearm, with a
    # boss on its outer side. On the bone left_arm, the arm is from x -1 to
    # 3, y -2 to 10, and z -2 to 2.
    im = model_image()
    paint_box(im, 0, 0, 4, 3, 4, lambda f, x, y: GOLD[2] if y == 0 else GOLD[1] if y == 2 else GOLD[0])
    paint_box(im, 0, 8, 1, 2, 2, lambda f, x, y: GOLD[2])
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [-1, 4, -2], "size": [4, 3, 4], "inflate": 0.35},
             {"bone": "left_arm", "uv": [0, 8], "from": [3.2, 4.5, -1], "size": [1, 2, 2], "inflate": 0.0}]
    save_model("riposte_guard", im, parts)


def gold_hilt():
    # A gold ring on the wrist: a gold ring round row 7 of each arm, with a
    # bright line on its top.
    gold = Mark(GOLD[0])
    shine = Mark(GOLD[2])
    for arm in ARMS:
        put(shine, arm, SIDES, rows=(6,))
        put(gold, arm, SIDES, rows=(7,))
    return [gold, shine]


def sword_slap():
    # A flat sword scar: a wide, pale scar that slants across the left arm.
    scar = Mark("#F4D0C8")
    edge = Mark("#8A3A30")
    for f in ("front", "left", "back"):
        for col in range(4):
            row = 3 + col
            scar.put([at(LEFT_ARM, f, col, row), at(LEFT_ARM, f, col, row + 1)])
            edge.put(at(LEFT_ARM, f, col, row + 2))
    return [scar, edge]


def gilded_blade():
    gold = Mark(GOLD[0])
    shine = Mark(GOLD[2])
    speckle([gold, shine], RIGHT_ARM, SIDES, 30, 1701)
    return [gold, shine]


# The Chaser.

def sprint():
    # Long, thin legs: dark outer columns and a pale line down the middle.
    dark = Mark("#2A1410")
    pale = Mark("#F0B0A0")
    for leg in LEGS:
        put(dark, leg, SIDES, cols=(0, 3))
        put(pale, leg, ("front", "back"), cols=(1,), rows=range(2, 12))
    return [dark, pale]


def scent_tracker():
    # A large snout: the face round the snout has the color of the snout, so
    # that the snout looks larger, and the nostrils are large and dark.
    snout = Mark("#E8848A")
    dark = Mark("#3A0E10")
    snout.put([face(c, r) for c in (2, 7) for r in range(4, 8)] + [face(c, 3) for c in range(3, 7)])
    dark.put([at(SNOUT, "front", c, r) for c in (0, 3) for r in (1, 2)])
    snout.put([at(SNOUT, "front", c, r) for c in (1, 2) for r in range(4)] +
              [at(SNOUT, "front", c, r) for c in (0, 3) for r in (0, 3)])
    return [snout, dark]


def vault():
    # Strong, bent legs: a bright thigh and a dark knee on each leg.
    bright = Mark("#F2A890")
    dark = Mark("#4A1A14")
    for leg in LEGS:
        put(bright, leg, SIDES, rows=(1, 2, 3))
        put(dark, leg, SIDES, rows=(6, 7))
    return [bright, dark]


def shrunken():
    # Small, round ears: the ears are dark, as in shade, and a small, round,
    # pale ear sits high on each side of the head, with a dark ring.
    dark = Mark("#4A1E1E")
    pale = Mark("#F4B8B0")
    for ear in EARS:
        put(dark, ear, ALL)
    for f in ("right", "left"):
        for x, y, ff, col, row in pixels(HEAD, (f,), cols=range(2, 6), rows=range(0, 4)):
            if col in (3, 4) and row in (1, 2):
                pale.put((x, y))
            elif (col in (2, 5)) != (row in (0, 3)):
                dark.put((x, y))
    return [dark, pale]


def duck():
    # A low, bent back: dark hunch lines go across the high back and over
    # the shoulders, a darker stripe goes down the spine, and the top of the
    # body and the high rows of its sides are dark, so the back looks bent
    # from the front and the side too.
    dark = Mark("#4A1C18")
    spine = Mark("#2A0C0A")
    for x, y, f, col, row in pixels(BODY, ("back",)):
        if col in (3, 4):
            spine.put((x, y))
        elif row in (1, 3, 5):
            dark.put((x, y))
    put(dark, BODY, ("top",))
    put(dark, BODY, ("right", "left"), rows=(0, 1, 3))
    put(dark, BODY, ("front",), rows=(0,))
    for arm in ARMS:
        put(dark, arm, ("top",))
        put(dark, arm, ("back",), rows=(0, 2))
    return [dark, spine]


def pack_run():
    # Torn cloth at the knees: ragged brown cloth with pale torn edges round
    # rows 5 to 7 of the legs.
    cloth = Mark("#5A3A20")
    edge = Mark("#C8A880")
    for leg in LEGS:
        for x, y, f, col, row in pixels(leg, SIDES, rows=(5, 6, 7)):
            if row == 7 and (col + len(f)) % 2:
                edge.put((x, y))
            elif row < 7:
                cloth.put((x, y))
    return [cloth, edge]


GENES = {
    "blood_feud": blood_feud, "horde_call": horde_call, "long_memory": long_memory, "war_squeal": war_squeal,
    "wrath": wrath, "avenge": avenge,
    "gold_flesh": gold_flesh, "gold_plating": gold_plating, "gilded_skull": gilded_skull, "glint": glint,
    "gold_rush": gold_rush, "tarnish": tarnish,
    "rot_bite": rot_bite, "nether_rot": nether_rot, "stench": stench, "corpse_gas": corpse_gas,
    "golem_rot": golem_rot, "rot_sprint": rot_sprint,
    "keen_edge": keen_edge, "sweep": sweep, "riposte": riposte, "gold_hilt": gold_hilt, "sword_slap": sword_slap,
    "gilded_blade": gilded_blade,
    "sprint": sprint, "scent_tracker": scent_tracker, "vault": vault, "shrunken": shrunken, "duck": duck,
    "pack_run": pack_run,
}

# Each gene with a model: the maker of the model and its name.
MODEL_GENES = {
    "avenge": (avenge_model, "avenge_cloth"),
    "gold_plating": (gold_plating_model, "gold_plating_plates"),
    "gilded_skull": (gilded_skull_model, "gilded_skull_plate"),
    "stench": (stench_model, "stench_fumes"),
    "riposte": (riposte_model, "riposte_guard"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, label="zombified piglin genes")
