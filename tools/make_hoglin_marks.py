"""Make the gene visuals of the hoglin, from design/species/hoglin.md.

Usage: python3 tools/make_hoglin_marks.py (from the root of the repository)

The hoglin and the zoglin use the hoglin region map and the 128x64 layout of
HoglinModel. This script also holds the parts of that layout, which
make_zoglin_marks.py uses:
- the body, 16x14x26 at uv (1, 1). Its top (27, 1) is the back: row 0 is at
  the tail, and row 25 is at the neck. On its sides, the right side (1, 27)
  has the tail in column 0, and the left side (43, 27) has the neck in
  column 0.
- the mane, a flat plane on the back. Its two sides are at (90, 52) and
  (109, 52), 19x10 each.
- the head, 14x6x19 at uv (61, 1). It tips down, so its top (80, 1) is the
  face: rows 0 to 6 are the brow, the eyes are on rows 7 and 8 in columns 0
  and 1 and columns 12 and 13, and rows 9 to 18 are the snout. The front of
  the head (80, 20) is the end of the snout.
- the ears, 6x1x4 at uv (1, 1) and (1, 6). The tusks, 2x11x2 at uv (10, 13)
  and (1, 13). Row 0 of a tusk is its tip.
- the front legs, 6x14x6 at uv (66, 42) and (41, 42). The hind legs, 5x11x5
  at uv (21, 45) and (0, 45).
The gene models use the bones body, head, right_horn, and left_horn.

Pixel plan for the Tuskers.
- Long Tusks: long, curved tusks, the gene model long_tusks_tusks.
- Sky Toss has an upturned snout: nostrils that face up, and a pale, curled
  lip.
- Gore has blood-red tusk tips.
- Shield Hook: hooked tusks, the gene model shield_hook_hooks.
- Golem Toss has chipped, cracked tusks.
- Ram has a flat, hard brow: a dark plate on the brow, with a ridge.

Pixel plan for the Chargers.
- Stampede has dust on the legs.
- Long Run has long, thin legs: dark edges and a pale middle line.
- Hoof Leap has strong back legs: bright haunches and hind legs.
- Sidestep Hoof has a low, lean body: a dark belly line and lean flanks.
- Wild Run has a stripe of wild bristles down the back.
- Dust Kick has dirty hooves.

Pixel plan for the Thickhides.
- Rock Hide has gray, stony patches on the body.
- Bristle Mat: thick, long bristles, the gene model bristle_mat_bristles.
- Blast Hide has a scorched hide.
- Root Feet has thick, dark hooves, split down the middle, with a pale
  rim.
- Scar Hide has old scars on the body.
- Barbed Hide: sharp bristles on the back, the gene model barbed_hide_barbs.

Pixel plan for the Herdkin.
- Herd Call has a wide, open snout.
- Close Ranks has a thick mane: a dark mane, and dark hair beside it.
- Big Litter has a round belly: a pale belly low on the sides.
- Mourning Herd has dark, wet eyes, with tear marks.
- Herd Rally has red ear tags.
- Fungus Guard has red crimson moss on the back.

Pixel plan for the Fearless.
- Warp Eater has blue fungus stains on the snout.
- Portal Walker has purple sparks on the hide.
- Anchor Brave has a glowing purple eye.
- Steady Nerves has a calm, steady look: heavy lids over the eyes.
- Overworld Hide has green moss and grass patches: a few round clumps of
  fresh grass green on the back and the flanks.
- Bold Charge has blue stripes round the legs.
This script needs Pillow.
"""
from make_gene_marks import Mark as TextureMark
from make_gene_marks import noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/hoglin.png"


def Mark(tint):
    """Return a mark for the 128x64 texture of the hoglin."""
    return TextureMark(tint, size=128, height=64)

BODY = box_faces(1, 1, 16, 14, 26)
MANE = {"right": (90, 52, 19, 10), "left": (109, 52, 19, 10)}
HEAD = box_faces(61, 1, 14, 6, 19)
RIGHT_EAR = box_faces(1, 1, 6, 1, 4)
LEFT_EAR = box_faces(1, 6, 6, 1, 4)
RIGHT_TUSK = box_faces(10, 13, 2, 11, 2)
LEFT_TUSK = box_faces(1, 13, 2, 11, 2)
RIGHT_FRONT_LEG = box_faces(66, 42, 6, 14, 6)
LEFT_FRONT_LEG = box_faces(41, 42, 6, 14, 6)
RIGHT_HIND_LEG = box_faces(21, 45, 5, 11, 5)
LEFT_HIND_LEG = box_faces(0, 45, 5, 11, 5)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
EARS = (RIGHT_EAR, LEFT_EAR)
TUSKS = (RIGHT_TUSK, LEFT_TUSK)
FRONT_LEGS = (RIGHT_FRONT_LEG, LEFT_FRONT_LEG)
HIND_LEGS = (RIGHT_HIND_LEG, LEFT_HIND_LEG)
LEGS = FRONT_LEGS + HIND_LEGS
FLANKS = ("right", "left")
# The eyes on the face, the top of the head: (col, row) of the top left
# pixel of each 2x2 eye.
EYES = ((0, 7), (12, 7))


def height(part):
    return part["front"][3]


def pixels(part, faces=ALL, cols=None, rows=None):
    """Yield (x, y, face, col, row) for the pixels of the faces of a part."""
    for face in faces:
        if face not in part:
            continue
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                if (cols is None or col in cols) and (rows is None or row in rows):
                    yield x0 + col, y0 + row, face, col, row


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    return x0 + col, y0 + row


def face(col, row):
    """Return a pixel of the face, the top of the head."""
    return at(HEAD, "top", col, row)


def cheek(eye, col, row):
    """Return a pixel of the side of the head beside an eye.

    The face and the sides of the head meet at the edge where the eyes are.
    Column 0 of a side is at the neck, as row 0 of the face, and row 0 of a
    side is at that edge. eye 0 is the right eye, and 1 is the left eye.
    """
    return at(HEAD, "right" if eye == 0 else "left", col if eye == 0 else 18 - col, row)


def put(mark, part, faces=ALL, cols=None, rows=None):
    mark.put([(x, y) for x, y, *_ in pixels(part, faces, cols, rows)])
    return mark


def speckle(marks, part, faces, share, seed, cols=None, rows=None, grain=1):
    """Put the pixels of a part on the marks, at random. share is the part of the pixels, in percent.

    With two marks, the second mark takes a third of the pixels. A grain
    above 1 makes patches of that size.
    """
    for x, y, f, col, row in pixels(part, faces, cols, rows):
        n = noise100(x // grain, y // grain, seed)
        if n < share:
            (marks[1] if len(marks) > 1 and n < share / 3 else marks[0]).put((x, y))


def eye_block(mark, centre, ring=None, size=3):
    """Put a block round each eye on the face. The block is size x size, on the 2x2 eye."""
    lo = -(size - 2) // 2
    for c0, r0 in EYES:
        for dc in range(lo, lo + size):
            for dr in range(lo, lo + size):
                c, r = c0 + dc, r0 + dr
                if 0 <= c < 14 and 0 <= r < 19:
                    (centre if dc in (0, 1) and dr in (0, 1) else mark).put(face(c, r))
        if ring:
            for dc in range(lo - 1, lo + size + 1):
                for dr in (lo - 1, lo + size):
                    c, r = c0 + dc, r0 + dr
                    if 0 <= c < 14 and 0 <= r < 19:
                        ring.put(face(c, r))


BONE = ("#F0E8D0", "#C8BCA0")


# The Tuskers.

def long_tusks():
    # The tusks are the gene model long_tusks_tusks.
    return []


def long_tusks_model():
    # Long, curved tusks: each tusk grows longer from its tip, and its end
    # curves back. On the bones right_horn and left_horn, the tusk is from
    # y -11 to 0, x -1 to 1, and z -1 to 1.
    im = model_image()
    paint_box(im, 0, 0, 2, 5, 2, lambda f, x, y: BONE[1] if y == 4 else BONE[0])
    paint_box(im, 0, 10, 2, 2, 2, lambda f, x, y: "#FFFFF0")
    parts = []
    for bone in ("right_horn", "left_horn"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1, -16, -1], "size": [2, 5, 2], "inflate": -0.1})
        parts.append({"bone": bone, "uv": [0, 10], "from": [-1, -17, 0.5], "size": [2, 2, 2], "inflate": -0.25})
    save_model("long_tusks_tusks", im, parts)


def sky_toss():
    # An upturned snout: on the end of the snout, the nostrils face up, dark,
    # on its high rows, and a pale, curled lip goes along the low end of the
    # face.
    dark = Mark("#2A0E06")
    lip = Mark("#F8C8A0")
    dark.put([at(HEAD, "front", c, r) for c in (2, 3, 4, 9, 10, 11) for r in (0, 1)])
    lip.put([face(c, r) for c in range(14) for r in (16, 17, 18)])
    lip.put([at(HEAD, "front", c, r) for c in range(14) for r in (4, 5)])
    return [dark, lip]


def gore():
    red = Mark("#C01010")
    dark = Mark("#6A0606")
    for tusk in TUSKS:
        put(red, tusk, ALL, rows=range(0, 4))
        put(dark, tusk, SIDES, rows=(4,))
    return [red, dark]


def shield_hook():
    # The hooks are the gene model shield_hook_hooks.
    return []


def shield_hook_model():
    # Hooked tusks: a hook at the tip of each tusk bends forward and down.
    # On the bones right_horn and left_horn, the tip of the tusk is at y -11,
    # and the front is at z -1.
    im = model_image()
    paint_box(im, 0, 0, 2, 2, 3, lambda f, x, y: BONE[0])
    paint_box(im, 0, 8, 2, 3, 1, lambda f, x, y: BONE[1] if y == 2 else BONE[0])
    parts = []
    for bone in ("right_horn", "left_horn"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1, -13, -3], "size": [2, 2, 3], "inflate": -0.1})
        parts.append({"bone": bone, "uv": [0, 8], "from": [-1, -12, -4], "size": [2, 3, 1], "inflate": -0.1})
    save_model("shield_hook_hooks", im, parts)


def golem_toss():
    # Chipped, cracked tusks: gray chips and dark cracks down the tusks.
    crack = Mark("#3A3428")
    chip = Mark("#9A9488")
    for tusk in TUSKS:
        for x, y, f, col, row in pixels(tusk, ALL):
            if (row + col) % 3 == 0:
                crack.put((x, y))
            elif row % 4 == 1:
                chip.put((x, y))
    return [crack, chip]


def ram():
    # A flat, hard brow: rows 0 to 5 of the face are a dark, hard plate, with
    # a pale ridge on row 5 and dark lines across it.
    plate = Mark("#6A4A38")
    ridge = Mark("#D8B898")
    line = Mark("#3A2418")
    for x, y, f, col, row in pixels(HEAD, ("top",), rows=range(0, 6)):
        (ridge if row == 5 else line if row == 2 else plate).put((x, y))
    put(plate, HEAD, ("back",))
    return [plate, ridge, line]


# The Chargers.

def stampede():
    dust = Mark("#C8B490")
    dark = Mark("#8A7A60")
    for leg in LEGS:
        h = height(leg)
        speckle([dust, dark], leg, SIDES, 55, 4101, rows=range(h // 2, h))
    return [dust, dark]


def long_run():
    dark = Mark("#4A2A1A")
    pale = Mark("#F8C8A0")
    for leg in LEGS:
        w = leg["front"][2]
        put(dark, leg, SIDES, cols=(0, w - 1))
        put(pale, leg, ("front", "back"), cols=(w // 2,))
    return [dark, pale]


def hoof_leap():
    # Strong back legs: a round haunch at the tail end of each flank, bright
    # at its middle and less bright at its edge, with a dark line on its
    # front edge, and bright high hind legs.
    bright = Mark("#F8BC8C")
    mid = Mark("#EEA474")
    dark = Mark("#8A4A2A")
    for f, centre, front in (("right", 4.5, 1), ("left", 20.5, -1)):
        for x, y, ff, col, row in pixels(BODY, (f,)):
            dx, dy = (col - centre) / 5, (row - 8.5) / 5
            r = dx * dx + dy * dy
            if r < 0.35:
                bright.put((x, y))
            elif r < 0.75:
                mid.put((x, y))
            elif r < 1.0 and dx * front > 0.3:
                dark.put((x, y))
    for leg in HIND_LEGS:
        put(bright, leg, SIDES, rows=range(0, 3))
        put(mid, leg, SIDES, rows=(3, 4))
    return [bright, mid, dark]


def sidestep_hoof():
    # A low, lean body: a dark belly line along the low rows of the flanks,
    # and lean, dark lines between the ribs.
    dark = Mark("#5A2E1C")
    lean = Mark("#9A5A3A")
    for x, y, f, col, row in pixels(BODY, FLANKS):
        if row >= 11:
            dark.put((x, y))
        elif row >= 5 and col % 4 == 0:
            lean.put((x, y))
    put(dark, BODY, ("bottom",))
    return [dark, lean]


def wild_run():
    # A stripe of wild bristles: a dark, ragged stripe down the middle of the
    # back, from the neck to the tail.
    dark = Mark("#2A1A12")
    tip = Mark("#6A4A34")
    for x, y, f, col, row in pixels(BODY, ("top",)):
        width = 2 + (noise100(row, 0, 4201) % 3)
        if abs(col - 7.5) < width:
            (tip if noise100(x, y, 4202) < 30 else dark).put((x, y))
    put(dark, BODY, ("front", "back"), cols=(6, 7, 8, 9), rows=(0, 1))
    return [dark, tip]


def dust_kick():
    mud = Mark("#5A4430")
    dirt = Mark("#8A7058")
    for leg in LEGS:
        h = height(leg)
        for x, y, f, col, row in pixels(leg, SIDES, rows=range(h - 4, h)):
            (mud if row >= h - 2 or noise100(x, y, 4301) < 40 else dirt).put((x, y))
        put(mud, leg, ("bottom",))
    return [mud, dirt]


# The Thickhides.

def rock_hide():
    stone = Mark("#8A8680")
    dark = Mark("#5A5652")
    speckle([stone, dark], BODY, ("top",) + FLANKS + ("front", "back"), 30, 5101, grain=3)
    return [stone, dark]


def bristle_mat():
    # The bristles are the gene model bristle_mat_bristles.
    return []


def bristle_mat_model():
    # Thick, long bristles: rows of dark, long bristles along the back, from
    # the neck to the hips, on both sides of the mane. On the bone body, the
    # top of the body is at y -7, from x -8 to 8 and z -13 to 13.
    im = model_image()
    paint_box(im, 0, 0, 1, 3, 1, lambda f, x, y: "#2A1A12" if y else "#5A4030")
    parts = []
    for z in range(-12, 10, 2):
        for x in (-5.5, -3, 2, 4.5):
            if noise100(int(x * 2), z, 5201) < 70:
                parts.append({"bone": "body", "uv": [0, 0], "from": [x, -9.5, z], "size": [1, 3, 1],
                              "inflate": 0.0})
    save_model("bristle_mat_bristles", im, parts)


def blast_hide():
    burnt = Mark("#3A2014")
    black = Mark("#160C08")
    for x, y, f, col, row in pixels(BODY, ("top",) + FLANKS + ("front", "back")):
        n = noise100(x // 2, y // 2, 5301)
        if n < 55:
            (black if n < 20 else burnt).put((x, y))
    return [burnt, black]


def root_feet():
    # Thick, dark hooves: the low three rows and the bottom of each leg are
    # a thick, dark hoof, split down its middle, with a pale rim on top.
    hoof = Mark("#1E1814")
    split = Mark("#5A4A40")
    rim = Mark("#C8A890")
    for leg in LEGS:
        h = height(leg)
        w = leg["front"][2]
        put(rim, leg, SIDES, rows=(h - 4,))
        for x, y, f, col, row in pixels(leg, SIDES, rows=range(h - 3, h)):
            (split if f in ("front", "back") and col == w // 2 else hoof).put((x, y))
        put(hoof, leg, ("bottom",))
    return [hoof, split, rim]


def scar_hide():
    scar = Mark("#F8D8C0")
    edge = Mark("#8A4A30")
    for f, starts in (("right", (2, 9, 16)), ("left", (4, 12, 19)), ("top", (1, 7, 13))):
        for s in starts:
            for n in range(6):
                col, row = s + n, 2 + n + (s % 3)
                x0, y0, w, h = BODY[f]
                if col < w and row < h:
                    scar.put(at(BODY, f, col, row))
                    if row + 1 < h:
                        edge.put(at(BODY, f, col, row + 1))
    return [scar, edge]


def barbed_hide():
    # The barbs are the gene model barbed_hide_barbs.
    return []


def barbed_hide_model():
    # Sharp bristles on the back: short, gray barbs that lean out from the
    # high edges of the back, with pale tips. On the bone body, the top of the
    # body is at y -7, and its edges are at x -8 and 8.
    im = model_image()
    paint_box(im, 0, 0, 1, 2, 1, lambda f, x, y: "#E0DCD0" if y == 0 else "#4A4440")
    parts = []
    for z in range(-11, 12, 3):
        for x, rz in ((-7.5, -30), (6.5, 30)):
            parts.append({"bone": "body", "uv": [0, 0], "from": [x, -9, z], "size": [1, 2, 1], "inflate": 0.0,
                          "pivot": [x + 0.5, -7, z + 0.5], "rotation": [0, 0, rz]})
    save_model("barbed_hide_barbs", im, parts)


# The Herdkin.

def herd_call():
    # A wide, open snout: the end of the snout is a wide, open mouth, dark
    # with a red inside, large nostrils, and the open mouth goes back along
    # the low edge of each side of the head.
    dark = Mark("#1A0604")
    red = Mark("#8A1A1A")
    dark.put([at(HEAD, "front", c, r) for c in range(14) for r in range(6) if r in (0, 5) or c in (0, 13)])
    red.put([at(HEAD, "front", c, r) for c in range(1, 13) for r in range(1, 5)])
    dark.put([face(c, r) for c in (3, 4, 9, 10) for r in (16, 17)])
    for eye in (0, 1):
        dark.put([cheek(eye, c, 5) for c in range(9, 19)])
        red.put([cheek(eye, c, 4) for c in range(12, 19)])
    return [dark, red]


def close_ranks():
    # A thick mane: the mane is a dark, thick brown, and long, dark strands
    # of hair go down the middle of the back beside it.
    dark = Mark("#3A2214")
    hair = Mark("#5A3A24")
    put(dark, MANE, ("right", "left"))
    for x, y, f, col, row in pixels(BODY, ("top",), cols=range(3, 13)):
        strand = (col * 7 + row // 3) % 4
        if 6 <= col <= 9 or strand == 0:
            dark.put((x, y))
        elif strand == 1:
            hair.put((x, y))
    return [dark, hair]


def big_litter():
    # A round belly: a pale, round belly on the low rows of the flanks, and a
    # pale bottom.
    pale = Mark("#F8C8B0")
    edge = Mark("#B07A60")
    for x, y, f, col, row in pixels(BODY, FLANKS):
        dx, dy = (col - 12.5) / 9, (row - 13) / 6
        r = dx * dx + dy * dy
        if r < 0.8:
            pale.put((x, y))
        elif r < 1.1:
            edge.put((x, y))
    put(pale, BODY, ("bottom",))
    return [pale, edge]


def mourning_herd():
    # Dark, wet eyes: each eye is dark, with a wet, white glint, and dark,
    # wet tear streaks two pixels wide run down from it, on the face down to
    # the snout and on the side of the head beside it.
    dark = Mark("#140A08")
    glint = Mark("#E8F4FF")
    tear = Mark("#3A2018")
    wet = Mark("#7A5A50")
    eye_block(dark, dark, size=3)
    for n, (c0, r0) in enumerate(EYES):
        glint.put(face(c0, r0))
        for r in range(r0 + 2, r0 + 11):
            tear.put([face(c0, r), face(c0 + 1, r)])
            if r % 3 == 0:
                wet.put(face(c0 + (2 if n == 0 else -1), r))
        dark.put([cheek(n, c, 0) for c in (6, 7, 8, 9)] + [cheek(n, c, 1) for c in (7, 8)])
        for r in range(2, 6):
            tear.put([cheek(n, 7 + r // 2, r), cheek(n, 8 + r // 2, r)])
    return [dark, glint, tear, wet]


def herd_rally():
    # Red ear tags: each ear is red, with a pale tag ring on its top and its
    # bottom.
    red = Mark("#D01818")
    ring = Mark("#F0E0D0")
    for ear in EARS:
        for x, y, f, col, row in pixels(ear, ALL):
            if f in ("top", "bottom") and (col, row) in ((2, 1), (3, 1), (2, 2), (3, 2)):
                ring.put((x, y))
            else:
                red.put((x, y))
    return [red, ring]


def fungus_guard():
    moss = Mark("#9A1A24")
    bright = Mark("#D03040")
    speckle([moss, bright], BODY, ("top",), 60, 5501, grain=2)
    speckle([moss, bright], BODY, FLANKS, 30, 5502, rows=range(0, 4), grain=2)
    return [moss, bright]


# The Fearless.

def warp_eater():
    blue = Mark("#20A0A8")
    pale = Mark("#70E0D8")
    speckle([blue, pale], HEAD, ("top",), 55, 6101, rows=range(10, 19), grain=2)
    speckle([blue, pale], HEAD, ("front",), 60, 6102, grain=2)
    return [blue, pale]


def portal_walker():
    spark = Mark("#C040FF")
    bright = Mark("#F0B0FF")
    speckle([spark, bright], BODY, ("top",) + FLANKS + ("front", "back"), 9, 6201)
    return [spark, bright]


def anchor_brave():
    # A glowing purple eye: the right eye is a bright purple block of 3x3,
    # with a purple glow round it, on the face and on the side of the head.
    purple = Mark("#A030F0")
    bright = Mark("#F0C0FF")
    glow = Mark("#6A2A9A")
    c0, r0 = EYES[0]
    for c in range(c0, c0 + 3):
        for r in range(r0 - 1, r0 + 2):
            (bright if c < c0 + 2 and r >= r0 else purple).put(face(c, r))
    glow.put([face(c, r) for c in range(c0, c0 + 4) for r in (r0 - 2, r0 + 2)] + [face(c0 + 3, r)
                                                                                    for r in range(r0 - 1, r0 + 2)])
    purple.put([cheek(0, c, 0) for c in (7, 8)] + [cheek(0, c, 1) for c in (7, 8)])
    glow.put([cheek(0, c, r) for c in (5, 6, 9, 10) for r in (0, 1)] + [cheek(0, c, 2) for c in range(6, 10)])
    return [purple, bright, glow]


def steady_nerves():
    # A calm, steady look: heavy, dark lids over the high half of each eye,
    # on the face and round the edge onto the side of the head, and a calm,
    # dark line under each eye.
    lid = Mark("#4A2A1A")
    calm = Mark("#2A1A12")
    for n, (c0, r0) in enumerate(EYES):
        lid.put([face(c, r) for c in range(max(0, c0 - 1), min(14, c0 + 3)) for r in (r0 - 2, r0 - 1, r0)])
        calm.put([face(c, r0 + 2) for c in range(max(0, c0 - 1), min(14, c0 + 3))])
        lid.put([cheek(n, c, r) for c in range(5, 9) for r in (0, 1)])
        calm.put([cheek(n, c, 2) for c in range(6, 10)])
    return [lid, calm]


def overworld_hide():
    # Green moss and grass patches: a few round clumps of fresh grass green
    # on the back and on the flanks, each with dark moss at its edge and
    # bright tufts of grass in it.
    moss = Mark("#3A7A24")
    grass = Mark("#5AAE34")
    tuft = Mark("#9AE05A")
    clumps = (("top", 4, 5, 3.5, 3), ("top", 11, 14, 3, 4), ("top", 5, 21, 3, 3),
              ("right", 6, 4, 4, 2.5), ("right", 18, 3, 3.5, 2.5), ("right", 12, 10, 3, 2.5),
              ("left", 8, 3, 3.5, 2.5), ("left", 20, 5, 4, 2.5), ("left", 14, 11, 3, 2.5))
    for f, cc, cr, rc, rr in clumps:
        for x, y, ff, col, row in pixels(BODY, (f,)):
            r = ((col - cc) / rc) ** 2 + ((row - cr) / rr) ** 2
            if r < 0.55:
                (tuft if (col + 2 * row) % 4 == 0 else grass).put((x, y))
            elif r < 1.0 and noise100(x, y, 6301) < 80:
                moss.put((x, y))
    return [moss, grass, tuft]


def bold_charge():
    blue = Mark("#2050C8")
    for leg in LEGS:
        h = height(leg)
        put(blue, leg, SIDES, rows=(2, 3, h - 6, h - 5))
    return [blue]


GENES = {
    "long_tusks": long_tusks, "sky_toss": sky_toss, "gore": gore, "shield_hook": shield_hook,
    "golem_toss": golem_toss, "ram": ram,
    "stampede": stampede, "long_run": long_run, "hoof_leap": hoof_leap, "sidestep_hoof": sidestep_hoof,
    "wild_run": wild_run, "dust_kick": dust_kick,
    "rock_hide": rock_hide, "bristle_mat": bristle_mat, "blast_hide": blast_hide, "root_feet": root_feet,
    "scar_hide": scar_hide, "barbed_hide": barbed_hide,
    "herd_call": herd_call, "close_ranks": close_ranks, "big_litter": big_litter,
    "mourning_herd": mourning_herd, "herd_rally": herd_rally, "fungus_guard": fungus_guard,
    "warp_eater": warp_eater, "portal_walker": portal_walker, "anchor_brave": anchor_brave,
    "steady_nerves": steady_nerves, "overworld_hide": overworld_hide, "bold_charge": bold_charge,
}

MODEL_GENES = {
    "long_tusks": (long_tusks_model, "long_tusks_tusks"),
    "shield_hook": (shield_hook_model, "shield_hook_hooks"),
    "bristle_mat": (bristle_mat_model, "bristle_mat_bristles"),
    "barbed_hide": (barbed_hide_model, "barbed_hide_barbs"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="hoglin genes")
