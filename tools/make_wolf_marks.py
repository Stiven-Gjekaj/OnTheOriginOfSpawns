"""Make the gene visuals of the wolf, from design/species/wolf.md.

Usage: python3 tools/make_wolf_marks.py (from the root of the repository)

The wolf texture is 64x32, and it uses the wolf region map. The 9 variants
and their wild, tame, and angry textures use the same layout, so one set of
marks fits all of them. The parts, from AdultWolfModel:
- the head, 6x6x4 at uv (0, 0). The face is its front. The eyes are in row 2
  of the face, in columns 0 and 1, and 4 and 5. The bottom of the head and
  its back do not show.
- the ears, 2x2x1 at uv (16, 14). The two ears use the same pixels.
- the snout, 3x3x4 at uv (0, 10). Row 0 of its front has the nose. Row 2 of
  its front and its sides is the mouth line.
- the mane (upper_body), 8x6x7 at uv (21, 0), and the back half of the body
  (body), 6x9x6 at uv (18, 14). The two turn by 90 degrees round x. So the
  back face of each cube is the back of the wolf, the front face is the
  belly, row 0 of the sides is at the front end, and the bottom face of the
  body is the rump. The top face of the mane is the chest. The head hides
  the chest, and the collar of a tame wolf covers its edges and row 0 of the
  sides of the mane.
- the legs, 2x8x2 at uv (0, 18). All four legs use the same pixels. The right
  legs are mirrored, so the left face is the outer side of each leg. Row 0
  is in the body.
- the tail, 2x8x2 at uv (9, 18).
On a side face of the head and the snout, the script counts the columns from
the back end of the part. On a side face of the mane and the body, it counts
the columns from the back of the wolf (the spine) to the belly. The helper
side() puts the same column on the right side and on the left side.

The head shows in about a fifth of the render, which is too small for 14
genes. So three head genes go to other parts: Moon Howl and Rend to the
neck, and Hamstring to the legs. Each gene keeps to its own zone, so that
the marks stack:
- The face. Hot Temper has the eyes and the brows over them, and Alpha Mark
  the middle of the brow. Throat Bite has a dark mask round the eyes: the
  corners of the brow and the front column of the sides of the head. Snarl
  has the lips beside the snout. Strong Jaw has the low row of the face at
  the two sides. Grooming has the bridge between the eyes and the face round
  the snout.
- The sides of the head. Old Grudge has the two back columns over the jaw:
  a scar across the line of the eye. Strong Jaw has the two low rows, and
  Snarl the corner of the mouth.
- The snout. Wet Nose has the nose, the top of the snout, and the front of
  row 0 of the sides. Bone Crush has the teeth: rows 1 and 2 of the front,
  and row 1 of the front corners. Grooming has the back of rows 0 and 1 of
  the sides, and Snarl row 2 (the lips) to the front corners.
- The ears. Alarm Yelp has the inside (the front) and the inner side. Pack
  Bond tears the tip of the ear (a mask erases it) and has the back, the
  top, and the outer side.
- The neck (the sides of the mane). Last Howl has a ring round the neck,
  rows 1 and 2, behind the collar. Moon Howl has the throat, the low part of
  rows 1 and 2. Rend has red stains under the throat, the low part of rows 3
  to 5. Bitten Hide has the shoulders, the middle of rows 3 to 5, and
  Bristle the top column of rows 3 to 5 and the outer columns of the back of
  the mane. Fear Howl is a gene model: raised hackles on the top of the neck.
- The back. Night Prowl has a dark stripe down the middle of the back of the
  mane, the back of the body, and the top of the tail. Shared Wounds has
  matching scars on the two sides of the back, and Bristle the columns
  between them and the stripe. Shaggy Fur is a gene model: long, shaggy fur
  on the back.
- The flanks (the sides of the body), from the spine: Shared Wounds, Weave
  (front rows) and Singed Pelt (back rows), Huddle, and Mud Coat at the
  belly. Mud Coat also has the belly. Frost Coat has the rump.
- The legs, from the top: Spring Leap has dark bands in rows 1 to 3, and Lope
  a long, pale stripe down the outer side, rows 1 to 4. Hamstring has white
  fang marks in row 4. Pack Sprint has dark socks in rows 5 and 6, and Snow
  Paws white paws in row 7 and the bottom.
The gene models use the bones upper_body (Fear Howl) and body (Shaggy Fur).
This script needs Pillow.
"""
import os

from PIL import Image

from make_gene_marks import MARK_DIR, Mark as TextureMark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/wolf.png"

HEAD = box_faces(0, 0, 6, 6, 4)
EAR = box_faces(16, 14, 2, 2, 1)
SNOUT = box_faces(0, 10, 3, 3, 4)
MANE = box_faces(21, 0, 8, 6, 7)
BODY = box_faces(18, 14, 6, 9, 6)
LEG = box_faces(0, 18, 2, 8, 2)
TAIL = box_faces(9, 18, 2, 8, 2)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES


def Mark(tint, **kw):
    return TextureMark(tint, height=32, **kw)


def pixels(part, faces=ALL, cols=None, rows=None):
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                if (cols is None or col in cols) and (rows is None or row in rows):
                    yield x0 + col, y0 + row, face, col, row


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    return x0 + col, y0 + row


def put(mark, part, faces=ALL, cols=None, rows=None):
    mark.put([(x, y) for x, y, *_ in pixels(part, faces, cols, rows)])
    return mark


def side(part, col, row):
    """Return a pixel of the right side and the same pixel of the left side.

    The column counts from the back end of the head and the snout, and from
    the spine on the mane and the body. Both are column 0 of the right face.
    """
    x0, y0, w, h = part["right"]
    if not (0 <= col < w and 0 <= row < h):
        return []
    lx, ly, lw, lh = part["left"]
    return [(x0 + col, y0 + row), (lx + lw - 1 - col, ly + row)]


def put_side(mark, part, cells):
    for c, r in cells:
        mark.put(side(part, c, r))
    return mark


def face(col, row):
    """Return the pixel of the face at a column and a row."""
    return at(HEAD, "front", col, row)


def cells(cols, rows):
    return [(c, r) for c in cols for r in rows]


# The face and the head.

def hot_temper():
    # Red eyes: each eye glows red with a bright middle, and a dark red brow
    # slants over it. An angry wolf has brows at the same pixels, so it
    # keeps its angry look.
    red = Mark("#C81010")
    hot = Mark("#FF4A2A")
    brow = Mark("#5A0A0A")
    red.put([face(0, 2), face(5, 2)])
    hot.put([face(1, 2), face(4, 2)])
    brow.put([face(0, 1), face(1, 1), face(4, 1), face(5, 1)])
    return [red, hot, brow]


def alpha_mark():
    # A dark mark on the brow: a dark brown blaze in the middle of the brow
    # points down between the eyes. It goes on to the front of the top of
    # the head, with a pale edge behind it.
    dark = Mark("#2A1C14")
    edge = Mark("#A08E7C")
    dark.put([face(1, 0), face(2, 0), face(3, 0), face(4, 0), face(2, 1), face(3, 1)])
    for x, y, f, col, row in pixels(HEAD, ("top",), cols=(2, 3), rows=(2, 3)):
        (dark if row == 3 else edge).put((x, y))
    return [dark, edge]


def throat_bite():
    # A dark mask round the eyes: black fur at the outer corners of the
    # brow, on the front column of the sides of the head, and on the cheeks
    # under the eyes, as a band from eye to eye round the head.
    mask = Mark("#0E0C0C")
    rim = Mark("#3A3030")
    mask.put([face(0, 0), face(5, 0)])
    for r in range(4):
        (rim if r == 3 else mask).put(side(HEAD, 3, r))
    return [mask, rim]


def old_grudge():
    # A scar across the eye: a pale, bare scar runs at a slant from the top
    # of the head down across the line of the eye to the jaw, with pink,
    # puckered skin at its two sides.
    scar = Mark("#F0E0D8")
    skin = Mark("#B0606A")
    line = {(2, 0), (2, 1), (1, 2), (1, 3)}
    for c, r in cells((1, 2), range(4)):
        (scar if (c, r) in line else skin).put(side(HEAD, c, r))
    for x, y, f, col, row in pixels(HEAD, ("top",), cols=(0, 5), rows=(2, 3)):
        (scar if row == 3 else skin).put((x, y))
    return [scar, skin]


def strong_jaw():
    # A wide, dark jaw: the low two rows of the sides of the head are dark
    # and heavy, and the dark goes on round the corners of the face.
    dark = Mark("#22180F")
    edge = Mark("#4A3A2A")
    for c, r in cells((1, 2), (4, 5)):
        (edge if r == 4 and c == 1 else dark).put(side(HEAD, c, r))
    dark.put(side(HEAD, 3, 5))
    dark.put([face(0, 5), face(5, 5)])
    return [dark, edge]


def snarl():
    # Dark, curled lips: black lips curl up beside the snout to the corner
    # of the mouth, and run along the mouth line on the sides of the snout
    # to its front corners.
    lip = Mark("#100A0A")
    pink = Mark("#9A4A50")
    lip.put([face(0, 4), face(5, 4)])
    pink.put([face(0, 3), face(5, 3)])
    lip.put(side(HEAD, 3, 4))
    for c in (1, 2, 3):
        lip.put(side(SNOUT, c, 2))
    return [lip, pink]


def grooming():
    # A clean, pale muzzle: the fur round the snout, on the bridge between
    # the eyes, and on the upper sides of the snout is clean and pale cream, with a darker cream edge. The cream
    # shows on the white snout of the ashen and the snowy wolf too.
    pale = Mark("#FFF2DC")
    cream = Mark("#EAD6B8")
    pale.put([face(1, 3), face(1, 4), face(4, 3), face(4, 4)])
    cream.put([face(c, 5) for c in (1, 2, 3, 4)])
    for c, r in cells((1, 2), (0, 1)):
        if (c, r) != (2, 0):
            (cream if c == 1 and r == 1 else pale).put(side(SNOUT, c, r))
    pale.put([face(2, 2), face(3, 2)])
    for x, y, f, col, row in pixels(HEAD, ("top",), cols=(1, 4), rows=(2, 3)):
        cream.put((x, y))
    return [pale, cream]


# The snout.

def wet_nose():
    # A long, dark nose: the black nose covers the front of the top row of
    # the snout and the front of its top edge, and runs back along the top
    # of the snout. Wet glints shine on
    # it.
    dark = Mark("#0C0C10")
    glint = Mark("#C8D8E8")
    # The vanilla nose is black, so the glints sit on it, and the dark goes
    # round it.
    for x, y, f, col, row in pixels(SNOUT, ("front",), rows=(0,)):
        (glint if col == 1 else dark).put((x, y))
    for x, y, f, col, row in pixels(SNOUT, ("top",)):
        (glint if col == 1 and row == 3 else dark).put((x, y))
    dark.put(side(SNOUT, 3, 0))
    dark.put(side(SNOUT, 2, 0))
    return [dark, glint]


def bone_crush():
    # Chipped, yellow teeth: the teeth on the front of the snout and at its
    # front corners are yellow, with dark chips and brown roots.
    tooth = Mark("#E8C850")
    chip = Mark("#3A2A10")
    root = Mark("#8A6A28")
    for x, y, f, col, row in pixels(SNOUT, ("front",), rows=(1, 2)):
        if row == 2 and col == 1:
            chip.put((x, y))
        elif row == 1:
            (root if col == 1 else tooth).put((x, y))
        else:
            tooth.put((x, y))
    tooth.put(side(SNOUT, 3, 1))
    return [tooth, chip, root]


# The ears.

def ear_tip(f, col, row):
    """Return True for a pixel of the tip of the ear that Pack Bond tears away."""
    return row == 0 and (f == "top" and col == 0 or f in ("front", "back") and col == 0 or f == "right")


def pack_bond():
    # A torn ear: the outer tip of each ear is torn away (a mask erases the
    # top of the outer side and the corner next to it), and the torn edge
    # and the back of the ear are dark with old scabs.
    mask = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    scab = Mark("#3A1A14")
    raw = Mark("#9A4038")
    torn = {("right", 0, 0), ("top", 0, 0), ("front", 0, 0), ("back", 1, 0)}
    for x, y, f, col, row in pixels(EAR, ("top", "right", "front", "back", "left")):
        if (f, col, row) in torn:
            mask.putpixel((x, y), (255, 255, 255, 255))
        elif f == "front":
            continue
        elif f == "left":
            continue
        elif f in ("right", "back") and row == 0 or f == "top":
            raw.put((x, y))
        else:
            scab.put((x, y))
    name = "pack_bond_mask"
    mask.save(os.path.join(MARK_DIR, name + ".png"))
    erase = {"regions": ["scalp"], "effect": {"type": "erase", "texture": f"originofspawns:textures/gene_marks/{name}.png"}}
    return [scab, raw, erase]


def alarm_yelp():
    # Wide, pale ears: the inside of each ear and its inner side are pale
    # cream, with a pink middle, so that the ears look wide open.
    pale = Mark("#F4ECDC")
    pink = Mark("#E0A0A0")
    for x, y, f, col, row in pixels(EAR, ("front",)):
        if (col, row) == (0, 0):
            continue
        (pink if (col, row) == (1, 1) else pale).put((x, y))
    put(pale, EAR, ("left",))
    return [pale, pink]


# The neck: the sides of the mane.

def last_howl():
    # A pale ring of fur round the neck: rows 1 and 2 of the mane, just behind
    # the collar, from the spine down to the throat, and over the back.
    pale = Mark("#F8F4EC")
    shade = Mark("#C8C0B4")
    for c, r in cells(range(4), (1, 2)):
        (shade if r == 2 and c % 2 else pale).put(side(MANE, c, r))
    for x, y, f, col, row in pixels(MANE, ("back",), rows=(1, 2)):
        (shade if row == 2 and col % 2 else pale).put((x, y))
    return [pale, shade]


def moon_howl():
    # A gray throat: the throat under the head, the low part of rows 1 and
    # 2 of the mane, is ash gray, with darker streaks.
    gray = Mark("#7A7A80")
    dark = Mark("#4A4A52")
    for c, r in cells((4, 5, 6), (1, 2)):
        (dark if (c + r) % 3 == 0 else gray).put(side(MANE, c, r))
    put(gray, MANE, ("front",), rows=(1, 2))
    return [gray, dark]


def rend():
    # Red stains: the blood of its prey runs from the muzzle down the throat
    # and dries in drips on the low part of rows 3 to 5 of the mane.
    red = Mark("#A81212")
    dark = Mark("#5A0606")
    drips = {(6, 3): red, (5, 3): red, (4, 3): dark, (6, 4): dark, (5, 4): red, (6, 5): red, (5, 5): dark}
    for (c, r), m in drips.items():
        m.put(side(MANE, c, r))
    put(dark, MANE, ("front",), rows=(3, 4))
    return [red, dark]


def bitten_hide():
    # Old bite scars on the shoulders: pale, bare scars in pairs of fang
    # holes, with dark, healed edges, on the middle of rows 3 to 5 of the
    # mane.
    scar = Mark("#E8D8D0")
    edge = Mark("#5A3A34")
    holes = {(1, 3), (3, 3), (2, 5), (4, 5)}
    for c, r in cells((1, 2, 3), (3, 4, 5)):
        (scar if (c, r) in holes else edge).put(side(MANE, c, r))
    return [scar, edge]


def bristle():
    # Stiff, dark bristles on the back: dark, stiff hairs stand on the top
    # column of rows 3 to 5 of the mane, on the outer columns of its back,
    # and on the back of the body between the scars and the stripe.
    dark = Mark("#141010")
    tip = Mark("#6A5A50")
    for r in (3, 4, 5):
        (tip if r == 4 else dark).put(side(MANE, 0, r))
    for x, y, f, col, row in pixels(MANE, ("back",), cols=(0, 1, 6, 7), rows=(3, 4, 5)):
        (tip if (col + row) % 2 else dark).put((x, y))
    for x, y, f, col, row in pixels(BODY, ("back",), cols=(1, 4), rows=range(1, 9)):
        (tip if row % 2 else dark).put((x, y))
    return [dark, tip]


def fear_howl():
    # Raised hackles on the neck: the gene model fear_howl_hackles. The marks
    # are only on the model.
    return []


def fear_howl_model():
    # Raised hackles: two rows of stiff tufts of fur stand up on the top of
    # the neck and lean back. The tufts use the mob texture, so they have
    # the coat of each variant. Their uv is on the back of the head, which
    # does not show and has no marks. The bone upper_body turns by 90
    # degrees round x. Each tuft turns back by -90 degrees round x at its
    # pivot, so the box is upright: in the frame of the pivot, -y is up and
    # -z is the front. The pivot is on the top of the mane (z 4 of the bone).
    # Each tuft is 2 wide, 1 deep, and 2 or 3 high, and leans back by 20
    # degrees.
    parts = []
    for y, high in ((-2.5, 3), (-1, 2)):
        for x, tall in ((-2, high - 1), (0, high), (2, high - 1)):
            parts.append({"bone": "upper_body", "uv": [14, 4], "from": [x, y - tall, 3.5], "size": [2, tall, 1],
                          "pivot": [x + 1, y, 4], "rotation": [-70, 0, 0]})
    save_mob_model("fear_howl_hackles", parts)


# The back and the flanks.

def night_prowl():
    # A dark stripe down the back: a dark band runs down the middle of the
    # back of the mane and the body, and over the top of the tail.
    dark = Mark("#141418")
    edge = Mark("#3A3A44")
    put(dark, MANE, ("back",), cols=(3, 4), rows=(3, 4, 5))
    for x, y, f, col, row in pixels(BODY, ("back",), cols=(2, 3)):
        dark.put((x, y))
    for x, y, f, col, row in pixels(TAIL, ("back",)):
        (edge if row == 7 else dark).put((x, y))
    put(edge, TAIL, ("top",))
    return [dark, edge]


def shared_wounds():
    # Matching scars on the back: three pale scars with pink edges cross the
    # top column of each flank and the outer columns of the back, at the same
    # rows, so that the left and the right side match.
    scar = Mark("#F4E8E0")
    pink = Mark("#C07078")
    rows = {2: scar, 3: pink, 5: scar, 6: pink, 8: scar}
    for r, m in rows.items():
        m.put(side(BODY, 0, r))
    for x, y, f, col, row in pixels(BODY, ("back",), cols=(0, 5), rows=tuple(rows)):
        rows[row].put((x, y))
    return [scar, pink]


def weave():
    # Pale zigzag marks on the sides: a pale zigzag line runs along the front
    # half of each flank, with a dark shade under it.
    pale = Mark("#FCFAF2")
    dark = Mark("#5A5048")
    line = {1: 1, 2: 2, 3: 3, 4: 2}
    for r, c in line.items():
        pale.put(side(BODY, c, r))
        if c + 1 <= 3:
            dark.put(side(BODY, c + 1, r))
    pale.put(side(BODY, 1, 5))
    return [pale, dark]


def singed_pelt():
    # Singed, brown fur on the flanks: a ragged, singed brown patch on the
    # back half of each flank, with black, burnt tips at its middle.
    brown = Mark("#8A5226")
    burnt = Mark("#2A1808")
    for c, r in cells((1, 2, 3), (5, 6, 7, 8)):
        edge = r in (5, 8) or c == 1
        n = noise100(c, r, 7311)
        if edge and n < 40:
            continue
        (burnt if not edge and n < 55 else brown).put(side(BODY, c, r))
    return [brown, burnt]


def huddle():
    # Dense fur on the flanks: a band of thick, dense fur, darker at the
    # roots, covers the low column of each flank and the back of the mane.
    tuft = Mark("#A89880")
    root = Mark("#5E5246")
    for r in range(9):
        (root if noise100(4, r, 5521) < 30 else tuft).put(side(BODY, 4, r))
    for x, y, f, col, row in pixels(MANE, ("bottom",)):
        (root if noise100(x, y, 5522) < 30 else tuft).put((x, y))
    return [tuft, root]


def mud_coat():
    # Dried mud on the belly: cracked, dried mud covers the belly and the
    # low edge of each flank.
    mud = Mark("#6A5232")
    crack = Mark("#3A2C18")
    for r in range(9):
        (crack if r % 3 == 1 else mud).put(side(BODY, 5, r))
    for x, y, f, col, row in pixels(BODY, ("front",)):
        (crack if (col + 2 * row) % 5 == 0 else mud).put((x, y))
    return [mud, crack]


def frost_coat():
    # White tips on the fur: the fur of the rump has white, frosty tips, with
    # pale blue ice between them.
    white = Mark("#FFFFFF")
    ice = Mark("#B8D8F0")
    for x, y, f, col, row in pixels(BODY, ("bottom",)):
        (ice if noise100(x, y, 6631) < 35 else white).put((x, y))
    return [white, ice]


def shaggy_fur():
    # Long, shaggy fur on the back: the gene model shaggy_fur_coat. The marks
    # are only on the model.
    return []


def shaggy_fur_model():
    # Long, shaggy fur: a ridge of long, ragged tufts of fur lies along the
    # middle of the back of the body and leans back. The tufts use the mob
    # texture, so they have the coat of each variant. Their uv is on the back
    # of the head, which does not show and has no marks. The bone body turns
    # by 90 degrees round x. Each tuft turns back by -90 degrees round x at
    # its pivot on the top of the body (z 3 of the bone), so the box is
    # upright. Each tuft is 2 wide, 1 deep, and 2 to 4 high, and leans back by
    # 50 degrees, so that the fur lies flat and long.
    parts = []
    for n, y in enumerate((-1.5, 0.5, 2.5, 4.5, 6.5)):
        for k, x in enumerate((-2, 0)):
            tall = 2 + (n + k) % 3
            parts.append({"bone": "body", "uv": [14, 4], "from": [x, y - tall, 2.5], "size": [2, tall, 1],
                          "pivot": [x + 1, y, 3], "rotation": [-40, 0, 0]})
    save_mob_model("shaggy_fur_coat", parts)


def save_mob_model(name, parts):
    """Write a gene model that uses the baked texture of the mob."""
    import json
    from make_husk_marks import MODEL_DIR
    model = {"mob_texture": True, "texture_width": 64, "texture_height": 32, "parts": parts}
    with open(os.path.join(MODEL_DIR, name + ".json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


# The legs.

def leg_put(mark, faces, rows, test=None):
    for x, y, f, col, row in pixels(LEG, faces, rows=rows):
        if test is None or test(f, col, row):
            mark.put((x, y))
    return mark


def lope():
    # Long, pale stripes: a long, pale stripe runs down the outer side of each
    # leg, rows 1 to 4.
    pale = Mark("#FCFAF4")
    leg_put(pale, ("left",), range(1, 5), lambda f, c, r: c == 1)
    return [pale]


def spring_leap():
    # Strong legs with dark bands: two dark bands round the high part of each
    # leg, rows 1 to 3, with a pale gap between them. The four legs use the
    # same pixels, so the bands are on each leg.
    dark = Mark("#1A1612")
    gap = Mark("#8A7E70")
    not_stripe = lambda f, c, r: not (f == "left" and c == 1)
    leg_put(dark, SIDES, (1, 3), not_stripe)
    leg_put(gap, SIDES, (2,), not_stripe)
    return [dark, gap]


def hamstring():
    # White fang marks: pairs of white fang holes with red edges in row 4 of
    # each leg, where a wolf bites its prey. The stripe of Lope goes
    # through the row on the outer side.
    white = Mark("#FFFFFF")
    red = Mark("#A02020")
    leg_put(white, SIDES, (4,), lambda f, c, r: c == 0)
    leg_put(red, SIDES, (4,), lambda f, c, r: c == 1 and f != "left")
    return [white, red]


def pack_sprint():
    # Dark socks on the paws: rows 5 and 6 of each leg are dark.
    dark = Mark("#18140F")
    edge = Mark("#3A3028")
    leg_put(edge, SIDES, (5,))
    leg_put(dark, SIDES, (6,))
    return [dark, edge]


def snow_paws():
    # White, furry paws: the low row of each leg and the sole are snow
    # white, with pale blue shade between the tufts.
    white = Mark("#FFFFFF")
    shade = Mark("#C8DCEC")
    leg_put(white, SIDES, (7,), lambda f, c, r: (c + (f in ("front", "back"))) % 2 == 0)
    leg_put(shade, SIDES, (7,), lambda f, c, r: (c + (f in ("front", "back"))) % 2 == 1)
    put(white, LEG, ("bottom",))
    return [white, shade]


GENES = {
    "pack_bond": pack_bond, "shared_wounds": shared_wounds, "huddle": huddle, "grooming": grooming,
    "alpha_mark": alpha_mark, "pack_sprint": pack_sprint,
    "lope": lope, "wet_nose": wet_nose, "spring_leap": spring_leap, "night_prowl": night_prowl,
    "weave": weave, "snow_paws": snow_paws,
    "moon_howl": moon_howl, "alarm_yelp": alarm_yelp, "old_grudge": old_grudge, "hot_temper": hot_temper,
    "fear_howl": fear_howl, "last_howl": last_howl,
    "shaggy_fur": shaggy_fur, "frost_coat": frost_coat, "bristle": bristle, "singed_pelt": singed_pelt,
    "mud_coat": mud_coat, "bitten_hide": bitten_hide,
    "strong_jaw": strong_jaw, "hamstring": hamstring, "rend": rend, "throat_bite": throat_bite,
    "bone_crush": bone_crush, "snarl": snarl,
}

MODEL_GENES = {
    "fear_howl": (fear_howl_model, "fear_howl_hackles"),
    "shaggy_fur": (shaggy_fur_model, "shaggy_fur_coat"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="wolf genes")
