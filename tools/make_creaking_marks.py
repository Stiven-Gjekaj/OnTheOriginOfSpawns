"""Make the gene visuals of the creaking, from design/species/creaking.md.

Usage: python3 tools/make_creaking_marks.py (from the root of the repository)

The creaking texture is 64x64, and it uses the creaking region map. The
glow layer (creaking_eyes.png) draws the three orange eyes over the head,
so the marks keep off the eye pixels, except the marks of the eye genes.
The parts, from CreakingModel:
- the head block, 6x10x6 at uv (0, 0). On its front (the face), the eyes
  are in row 2 (columns 0 to 2), row 4 (columns 3 to 5), and row 7
  (columns 1 to 3). The crown, 6x3x6 at uv (28, 31), is on top of the
  block. Two flat branches, at uv (12, 40) and (34, 12), stand out at the
  sides of the head.
- the body: 6x13x5 at uv (0, 16) (the low chest) and 6x7x5 at uv (24, 0)
  (the high chest, at the side of the head).
- the right arm, 3x21x3 at uv (22, 13), with the hand, 3x4x3 at uv (46, 0).
- the left arm, 3x16x3 at uv (30, 40), with the shoulder, 3x4x3 at uv
  (52, 12), and the hand, 3x4x3 at uv (52, 19).
- the right leg, 3x19x3 at uv (0, 34), with the hip, 3x3x3 at uv (12, 34),
  and the left leg, 3x16x3 at uv (42, 40). The feet are flat planes, 5x9,
  at uv (45, 46) and (45, 55).
On a side face of the head, the script counts the columns from the back
(column 0) to the front.

Each gene keeps to its own zone, so that the marks stack:
- The face. Heart Sense has orange rims under and beside the eyes. Dusk
  Bark has dark shades over the eyes, and a gene model: bark ledges over
  the eyes. Jitter Step has a crooked crack from the top right of the face
  down to the left. Fear Twitch has white lines at the top left of the face
  and round the low eye, and on the sides of the head. Hidden Heart has the
  low rows (the mouth), closed with bark, round the head. Long Stare (wide,
  bulging eyes) and Stare Back (a third eye on the brow) are gene models.
  Kill Watch has a red glint in each eye and red glints on the branches.
- The head, sides and back. Fear Twitch has white lines in the eye rows of
  the sides, and Heart Sense has an orange line in row 3. Jitter Step has
  its crack in rows 0, 1, 5, and 6 of the sides.
  Hunt Lust has a red stripe down the back of the head and over the crown.
- The body. Shadow Creep has dark bark on rows 0 to 3 of the front of the
  low chest. Dark Heart has a dark core in rows 4 to 8. Hard Heart has a
  thick bark plate in rows 9 to 12. Thorn Heart has a red knot in the
  middle of the high chest, front and back. Splinter Burst has pale
  splinters round the knot. Blast Sense has gray ash on the high rows of the
  back of the low chest, and Lone Watch has pale knots like eyes on its low
  rows.
- The arms. Shield Splinter has the hands. Root Grip has bands of roots, and
  Flank Walk has bands of bark, at other rows. Shield Stare has eye knots,
  and Sap Bite has amber sap, on the arms between the bands. Long Limbs (long
  arms), Blink Strike (long splinter fingers), and Thorn Fist (thorns) are
  gene models.
- The legs, from the top: Quick Bark has pale stripes, Trail Twitch has a
  twist, Heart Twitch has bright cracks, Slip Step has pale moss, and Silent
  Roots has moss on the low rows and the feet. Close Guard (root feet) is a
  gene model.
The gene models use the bones head, right_arm, left_arm, right_leg, and
left_leg. On the bone head, the head block is from x -3 to 3, y -10 to 0,
and z -3 to 3. Row r of the face is from y r - 10 to r - 9, and column c
is from x c - 3 to c - 2.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/creaking.png"

HEAD = box_faces(0, 0, 6, 10, 6)
CROWN = box_faces(28, 31, 6, 3, 6)
BRANCHES = ((12, 40, 9, 14), (21, 40, 9, 14), (34, 12, 9, 14), (43, 12, 9, 14))
LOW_CHEST = box_faces(0, 16, 6, 13, 5)
HIGH_CHEST = box_faces(24, 0, 6, 7, 5)
RIGHT_ARM = box_faces(22, 13, 3, 21, 3)
RIGHT_HAND = box_faces(46, 0, 3, 4, 3)
LEFT_ARM = box_faces(30, 40, 3, 16, 3)
LEFT_SHOULDER = box_faces(52, 12, 3, 4, 3)
LEFT_HAND = box_faces(52, 19, 3, 4, 3)
RIGHT_LEG = box_faces(0, 34, 3, 19, 3)
RIGHT_HIP = box_faces(12, 34, 3, 3, 3)
LEFT_LEG = box_faces(42, 40, 3, 16, 3)
FEET = ((54, 46, 5, 9), (59, 46, 5, 9), (54, 55, 5, 9), (59, 55, 5, 9))
SIDES = ("right", "front", "left", "back")

# The eyes on the face, as (column, row).
EYES = [(0, 2), (1, 2), (2, 2), (3, 4), (4, 4), (5, 4), (1, 7), (2, 7), (3, 7)]


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    assert 0 <= col < w and 0 <= row < h, (face, col, row)
    return x0 + col, y0 + row


def cells(part, face, cols, rows):
    return [at(part, face, c, r) for c in cols for r in rows]


def face(cells_):
    return [at(HEAD, "front", c, r) for c, r in cells_]


def side(col, row):
    """Return a pixel of the right side of the head and the same pixel of the left side."""
    x0, y0, w, h = HEAD["right"]
    lx, ly, lw, lh = HEAD["left"]
    return [(x0 + col, y0 + row), (lx + lw - 1 - col, ly + row)]


def all_sides(part, cols, rows):
    return [p for f in SIDES for p in cells(part, f, cols, rows)]


def plane(rect):
    x0, y0, w, h = rect
    return [(x0 + c, y0 + r) for c in range(w) for r in range(h)]


def opaque_branches():
    """Return the opaque pixels of the branches. They are thin twigs."""
    from preview_genes import CREAKING_PATH, find_zombie_texture
    tex = find_zombie_texture(CREAKING_PATH)
    return [p for rect in BRANCHES for p in plane(rect) if tex.getpixel(p)[3]]


# The face.

def heart_sense():
    # Orange eye rims: an orange rim under each eye and at its ends, and a
    # line of the rim round the sides of the head, under the high eye.
    rim = Mark("#FF8A20")
    deep = Mark("#B04A08")
    rim.put(face([(0, 3), (1, 3), (2, 3), (3, 5), (4, 5), (5, 5), (1, 8), (2, 8), (3, 8)]))
    deep.put(face([(3, 2), (2, 4), (0, 7), (4, 7)]))
    # The rim of the high eye goes on round the sides of the head, in row 3.
    for c in range(6):
        (rim if c % 2 else deep).put(side(c, 3))
    return [rim, deep]


def dusk_bark():
    # Dark shades over the eyes: the row over each eye is near black. The
    # bark ledges are the gene model dusk_bark_ledges.
    shade = Mark("#100C0A")
    shade.put(face([(0, 1), (1, 1), (2, 1), (3, 3), (4, 3), (5, 3), (1, 6), (2, 6), (3, 6)]))
    return [shade]


def dusk_bark_model():
    # Bark ledges: a short, dark ledge of bark stands out 2 pixels over each
    # eye, as the brim of a hood. The front of the face is at z -3.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 3, 1, 2, lambda f, x, y: "#2A201A" if f in ("top", "front") else "#141010")
    parts = [{"bone": "head", "uv": [0, 0], "from": [x, y, -5], "size": [3, 1, 2], "inflate": -0.05}
             for x, y in ((-3, -9.5), (0, -7.5), (-2, -4.5))]
    save_model("dusk_bark_ledges", im, parts)


def jitter_step():
    # A crooked crack on the face: a dark crack with pale edges runs from the
    # top right of the face down to the left, and goes on in a zigzag on the
    # right side of the head.
    crack = Mark("#0C0A08")
    edge = Mark("#B8A890")
    crack.put(face([(4, 0), (4, 1), (5, 2), (0, 4), (1, 5), (0, 6)]))
    edge.put(face([(3, 0), (5, 0), (3, 1), (5, 1), (4, 2), (1, 4), (0, 5), (2, 5)]))
    for c, r in ((0, 0), (1, 1), (2, 0), (3, 1), (4, 0), (5, 1), (0, 5), (1, 6), (2, 5), (3, 6), (4, 5), (5, 6)):
        crack.put(side(c, r))
    for c, r in ((0, 1), (1, 0), (2, 1), (3, 0), (4, 1), (5, 0), (0, 6), (1, 5), (2, 6), (3, 5), (4, 6), (5, 5)):
        edge.put(side(c, r))
    return [crack, edge]


def fear_twitch():
    # White lines round the eyes: white lines at the top left of the face,
    # beside the low eye, and back along the sides of the head in the rows
    # of the eyes.
    white = Mark("#F4F4F0")
    gray = Mark("#8A8680")
    white.put(face([(0, 0), (1, 0), (2, 0), (5, 7), (4, 6), (5, 6)]))
    for c in range(6):
        for r in (2, 4, 7):
            (white if (c + r) % 2 == 0 else gray).put(side(c, r))
    return [white, gray]


def hidden_heart():
    # Closed bark over the mouth: the low rows of the head, rows 8 and 9, are
    # closed with rough, dark bark plates round the front, the sides, and the
    # back.
    bark = Mark("#3A2C22")
    ridge = Mark("#6A5442")
    mouth = face([(0, 8), (4, 8), (5, 8)] + [(c, 9) for c in range(6)])
    for x, y in mouth:
        (ridge if (x + y) % 3 == 0 else bark).put((x, y))
    for c in range(6):
        for r in (8, 9):
            (ridge if (c + r) % 3 == 0 else bark).put(side(c, r))
            (ridge if (c + r) % 3 == 0 else bark).put(at(HEAD, "back", c, r))
    return [bark, ridge]


def long_stare():
    # The wide eyes are the gene model long_stare_eyes.
    return []


def long_stare_model():
    # Wide eyes: each eye bulges out of the face, wider than the slit, as a
    # glowing, pale orange eye with a white middle.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 4, 1, 1, lambda f, x, y: "#FFF4D0" if f == "front" and x in (1, 2) else "#FFB040")
    parts = [{"bone": "head", "uv": [0, 0], "from": [x, y, -3.8], "size": [4, 1, 1], "inflate": 0.1, "glow": True}
             for x, y in ((-3.5, -8), (-0.5, -6), (-2.5, -3))]
    save_model("long_stare_eyes", im, parts)


def stare_back():
    # The third eye is the gene model stare_back_eye. A pale ring round it
    # is on the front of the crown.
    ring = Mark("#D8D0B8")
    dark = Mark("#2A201A")
    ring.put(cells(CROWN, "front", (1, 4), range(3)))
    ring.put(cells(CROWN, "front", (2, 3), (0, 2)))
    dark.put(cells(CROWN, "front", (0, 5), range(3)))
    return [ring, dark]


def stare_back_model():
    # A third eye on the brow: a round knot of bark, 4 wide and 3 high,
    # stands out of the front of the crown, with a glowing orange eye in it.
    # On the bone head, the crown is from x -3 to 3, y -13 to -10, and z -3
    # to 3.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 4, 3, 2, lambda f, x, y: ("#FFF0C0" if (x, y) in ((1, 1), (2, 1)) else "#FFA020")
              if f == "front" and 0 < x < 3 else "#4A3A2E")
    parts = [{"bone": "head", "uv": [0, 0], "from": [-2, -13, -4.5], "size": [4, 3, 2], "glow": True}]
    save_model("stare_back_eye", im, parts)


def kill_watch():
    # Red eye glints: a red glint in the middle of each eye, and red glints,
    # as small red eyes, on the twigs of the branches.
    red = Mark("#FF2020")
    dark = Mark("#8A0808")
    red.put(face([(1, 2), (4, 4), (2, 7)]))
    for x, y in opaque_branches():
        n = noise100(x, y, 2301)
        if n < 45:
            red.put((x, y))
        elif n < 75:
            dark.put((x, y))
    return [red, dark]


def hunt_lust():
    # A red stripe on the head: a red stripe runs down the middle of the back
    # of the head, from the crown to the bark of the mouth, and over the back
    # and the top of the crown.
    red = Mark("#C81818")
    dark = Mark("#6A0808")
    red.put(cells(HEAD, "back", (2, 3), range(8)))
    dark.put(cells(HEAD, "back", (1, 4), range(8)))
    red.put(cells(CROWN, "back", (2, 3), range(3)))
    red.put(cells(CROWN, "top", (2, 3), range(6)))
    return [red, dark]


# The body.

def shadow_creep():
    # Dark bark on the chest: rows 0 to 3 of the front of the low chest are
    # near black bark.
    dark = Mark("#141010")
    ridge = Mark("#2E2620")
    for c in range(6):
        for r in range(4):
            (ridge if (c + r) % 3 == 0 else dark).put(at(LOW_CHEST, "front", c, r))
    return [dark, ridge]


def dark_heart():
    # A dark core on the chest: a black core in rows 4 to 8 of the front of
    # the low chest, with a dark rim.
    core = Mark("#050404")
    rim = Mark("#2A1E18")
    core.put(cells(LOW_CHEST, "front", range(1, 5), range(5, 8)))
    rim.put(cells(LOW_CHEST, "front", range(1, 5), (4, 8)))
    rim.put(cells(LOW_CHEST, "front", (0, 5), range(4, 9)))
    return [core, rim]


def hard_heart():
    # A thick bark plate on the chest: rows 9 to 12 of the front of the low
    # chest are a thick, pale bark plate with a dark rim.
    plate = Mark("#8A7058")
    rim = Mark("#2A1E14")
    for c in range(6):
        for r in range(9, 13):
            (rim if c in (0, 5) or r in (9, 12) else plate).put(at(LOW_CHEST, "front", c, r))
    return [plate, rim]


def thorn_heart():
    # A red knot on the chest: a red knot of wood in the middle of the high
    # chest, on the front and the back.
    red = Mark("#B81818")
    dark = Mark("#5A0808")
    for f in ("front", "back"):
        for c in range(1, 5):
            for r in range(2, 6):
                (dark if c in (1, 4) or r in (2, 5) else red).put(at(HIGH_CHEST, f, c, r))
    return [red, dark]


def splinter_burst():
    # Splinters on the chest: pale, sharp splinters stand round the red knot
    # on the front of the high chest.
    pale = Mark("#E8DCC0")
    dark = Mark("#4A3A2C")
    knot = {(c, r) for c in range(1, 5) for r in range(2, 6)}
    for c in range(6):
        for r in range(7):
            if (c, r) in knot:
                continue
            (pale if (c + 2 * r) % 3 else dark).put(at(HIGH_CHEST, "front", c, r))
    return [pale, dark]


def blast_sense():
    # Gray ash on the chest: gray and pale ash in rows 0 to 6 of the back of
    # the low chest, and on the sides of the high chest.
    gray = Mark("#8A8884")
    pale = Mark("#C8C4BC")
    ash = cells(LOW_CHEST, "back", range(6), range(7)) + cells(HIGH_CHEST, "right", range(5), range(7))
    for x, y in ash:
        (pale if noise100(x, y, 2303) < 40 else gray).put((x, y))
    return [gray, pale]


def lone_watch():
    # Pale knots like eyes: three pale knots, each with a dark middle, in rows
    # 7 to 12 of the back of the low chest.
    pale = Mark("#E0D4B8")
    dark = Mark("#1A1410")
    for c0, r0 in ((0, 7), (3, 7), (1, 10)):
        for dc in range(3):
            for dr in range(3):
                if c0 + dc < 6 and r0 + dr < 13:
                    (dark if (dc, dr) == (1, 1) else pale).put(at(LOW_CHEST, "back", c0 + dc, r0 + dr))
    return [pale, dark]


# The arms.

def shield_splinter():
    # Dark, heavy hands: the two hands are near black, with dark gray knuckles.
    dark = Mark("#141210")
    knuckle = Mark("#4A4440")
    for hand in (RIGHT_HAND, LEFT_HAND):
        for f in ("top", "bottom") + SIDES:
            x0, y0, w, h = hand[f]
            for c in range(w):
                for r in range(h):
                    (knuckle if (c + r) % 3 == 0 else dark).put(at(hand, f, c, r))
    return [dark, knuckle]


def root_grip():
    # Root bands round the arms: pale, twisted roots wrap round each arm, in
    # rows 2 and 3 and rows 12 and 13 of the right arm, and rows 2 and 3 and
    # rows 10 and 11 of the left arm.
    root = Mark("#C8AA80")
    dark = Mark("#5A4430")
    for arm, rows in ((RIGHT_ARM, (2, 3, 12, 13)), (LEFT_ARM, (2, 3, 10, 11))):
        for f in SIDES:
            for c in range(3):
                for r in rows:
                    (dark if (c + r) % 3 == 0 else root).put(at(arm, f, c, r))
    return [root, dark]


def flank_walk():
    # Bark bands on the arms: bands of thick, dark bark with pale ridges round
    # each arm, in rows 7 and 8 of the right arm and rows 6 and 7 of the left
    # arm, and round the left shoulder.
    bark = Mark("#201814")
    ridge = Mark("#7A6450")
    for arm, rows in ((RIGHT_ARM, (7, 8)), (LEFT_ARM, (6, 7))):
        for f in SIDES:
            for c in range(3):
                for r in rows:
                    (ridge if r == rows[0] and c == 1 else bark).put(at(arm, f, c, r))
    for f in SIDES:
        for c in range(3):
            for r in range(4):
                (ridge if r == 0 else bark).put(at(LEFT_SHOULDER, f, c, r))
    return [bark, ridge]


def shield_stare():
    # Eye knots on the arms: pale knots with a dark middle, as eyes, on the
    # front and the back of each arm, in rows 4 to 6 of the right arm and in
    # rows 13 to 15 of the left arm.
    pale = Mark("#E8DCC0")
    dark = Mark("#0C0808")
    for arm, r0 in ((RIGHT_ARM, 4), (LEFT_ARM, 13)):
        for f in SIDES:
            for c in range(3):
                for r in range(r0, r0 + 3):
                    (dark if (c, r - r0) == (1, 1) else pale).put(at(arm, f, c, r))
    return [pale, dark]


def sap_bite():
    # Amber sap on the arms: drips of amber sap run down the arms, in rows 15
    # to 20 of the right arm and rows 0 and 1 and 8, 9, and 12 of the left
    # arm.
    amber = Mark("#F0A020")
    deep = Mark("#A05808")
    for arm, rows in ((RIGHT_ARM, range(15, 21)), (LEFT_ARM, (0, 1, 8, 9, 12))):
        for f in SIDES:
            for c in range(3):
                for r in rows:
                    n = noise100(*at(arm, f, c, r), 2305)
                    if n < 55:
                        (deep if n < 20 else amber).put(at(arm, f, c, r))
    return [amber, deep]


def long_limbs():
    # The long arms are the gene model long_limbs_arms.
    return []


def long_limbs_model():
    # Long arms: each arm goes on below the hand, as a long, thin limb of
    # bark, 6 pixels long. The right hand is from y 19.5 to 23.5 on the bone
    # right_arm, and the left hand from y 15 to 19 on the bone left_arm.
    im = model_image(32, 32)
    paint_box(im, 0, 0, 2, 6, 2, lambda f, x, y: "#4A3C32" if (x + y) % 3 else "#2A221C")
    parts = [{"bone": "right_arm", "uv": [0, 0], "from": [-1.5, 23.5, -1], "size": [2, 6, 2]},
             {"bone": "left_arm", "uv": [0, 0], "from": [0.5, 19, -1], "size": [2, 6, 2]}]
    save_model("long_limbs_arms", im, parts)


def blink_strike():
    # The splinter fingers are the gene model blink_strike_fingers.
    return []


def blink_strike_model():
    # Long splinter fingers: three long, thin, pale splinters grow out of the
    # front of each hand and point forward and down.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 7, lambda f, x, y: "#E4D8BC" if f != "front" else "#6A5A48")
    parts = []
    for bone, x0, y in (("right_arm", -2, 21.5), ("left_arm", 0, 17)):
        for k in range(3):
            parts.append({"bone": bone, "uv": [0, 0], "from": [x0 + k, y, -8], "size": [1, 1, 7], "inflate": -0.3,
                          "pivot": [x0 + k + 0.5, y + 0.5, -1.5], "rotation": [-30, 0, 0]})
    save_model("blink_strike_fingers", im, parts)


def thorn_fist():
    # The thorns are the gene model thorn_fist_thorns.
    return []


def thorn_fist_model():
    # Thorns on the arms: short, dark thorns with pale points stand out of the
    # outer sides of the arms.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 2, 1, 1, lambda f, x, y: "#E0D4B8" if f in ("left", "right") else "#1E1814")
    parts = []
    for y in (1, 6, 10, 15):
        parts.append({"bone": "right_arm", "uv": [0, 0], "from": [-3.6, y, -0.5], "size": [2, 1, 1]})
    for y in (2, 9, 13):
        parts.append({"bone": "left_arm", "uv": [0, 0], "from": [2.6, y, -0.5], "size": [2, 1, 1]})
    save_model("thorn_fist_thorns", im, parts)


# The legs.

def leg_rows(rows_right, rows_left):
    return ([(RIGHT_LEG, f, c, r) for f in SIDES for c in range(3) for r in rows_right]
            + [(LEFT_LEG, f, c, r) for f in SIDES for c in range(3) for r in rows_left])


def quick_bark():
    # Pale stripes on the legs: pale vertical stripes on the high rows of each
    # leg and on the hip.
    pale = Mark("#E0D4BC")
    dark = Mark("#3A2E26")
    for part, f, c, r in leg_rows(range(0, 4), range(0, 3)):
        (pale if c == 1 else dark).put(at(part, f, c, r))
    for f in SIDES:
        for c in range(3):
            for r in range(3):
                (pale if c == 1 else dark).put(at(RIGHT_HIP, f, c, r))
    return [pale, dark]


def trail_twitch():
    # Twisted legs: dark and pale bands wind round each leg, as if the wood
    # is twisted, in the rows under the stripes.
    pale = Mark("#9A8670")
    dark = Mark("#1C1612")
    order = {"right": 0, "front": 3, "left": 6, "back": 9}
    for part, f, c, r in leg_rows(range(4, 8), range(3, 6)):
        k = (order[f] + c + r) % 4
        (dark if k < 2 else pale).put(at(part, f, c, r))
    return [pale, dark]


def heart_twitch():
    # Bright cracks on the legs: cracks that glow orange, as the heart of a
    # creaking, in the middle rows of each leg.
    glow = Mark("#FFA030")
    dark = Mark("#3A1A08")
    for part, f, c, r in leg_rows(range(8, 11), range(6, 9)):
        (glow if (c + r) % 2 == 0 else dark).put(at(part, f, c, r))
    return [glow, dark]


def slip_step():
    # Pale moss on the legs: patches of pale, gray green moss in the rows
    # under the cracks.
    moss = Mark("#A8C0A0")
    shade = Mark("#6A8A66")
    for part, f, c, r in leg_rows(range(11, 14), range(9, 12)):
        x, y = at(part, f, c, r)
        (shade if noise100(x, y, 2307) < 35 else moss).put((x, y))
    return [moss, shade]


def silent_roots():
    # Moss on the feet: green moss on the low rows of each leg and on the
    # flat feet.
    moss = Mark("#4A8A30")
    dark = Mark("#2A5A1A")
    for part, f, c, r in leg_rows(range(14, 19), range(12, 16)):
        x, y = at(part, f, c, r)
        (dark if noise100(x, y, 2309) < 35 else moss).put((x, y))
    for x, y in [p for rect in FEET for p in plane(rect)]:
        (dark if noise100(x, y, 2311) < 35 else moss).put((x, y))
    return [moss, dark]


def close_guard():
    # The root feet are the gene model close_guard_roots.
    return []


def close_guard_model():
    # Root feet: thick roots spread out from the foot of each leg over the
    # ground, to the front, the sides, and the back. On the bone right_leg,
    # the foot is at y 17.2. On the bone left_leg, it is at y 15.7.
    im = model_image(32, 32)
    paint_box(im, 0, 0, 1, 1, 4, lambda f, x, y: "#6A5240" if f == "top" else "#3A2C20")
    parts = []
    for bone, cx, y in (("right_leg", -1.5, 17.2), ("left_leg", 0, 15.7)):
        for turn in (0, 90, 180, 270):
            parts.append({"bone": bone, "uv": [0, 0], "from": [cx - 0.5, y - 0.8, -5.5], "size": [1, 1, 4],
                          "pivot": [cx, y, 0], "rotation": [0, turn, 0]})
    save_model("close_guard_roots", im, parts)


GENES = {
    "jitter_step": jitter_step, "blink_strike": blink_strike, "shadow_creep": shadow_creep,
    "fear_twitch": fear_twitch, "trail_twitch": trail_twitch, "slip_step": slip_step,
    "thorn_fist": thorn_fist, "shield_splinter": shield_splinter, "splinter_burst": splinter_burst,
    "sap_bite": sap_bite, "long_limbs": long_limbs, "thorn_heart": thorn_heart,
    "hard_heart": hard_heart, "hidden_heart": hidden_heart, "close_guard": close_guard,
    "root_grip": root_grip, "dark_heart": dark_heart, "heart_sense": heart_sense,
    "quick_bark": quick_bark, "silent_roots": silent_roots, "flank_walk": flank_walk,
    "hunt_lust": hunt_lust, "dusk_bark": dusk_bark, "blast_sense": blast_sense,
    "long_stare": long_stare, "stare_back": stare_back, "lone_watch": lone_watch,
    "shield_stare": shield_stare, "kill_watch": kill_watch, "heart_twitch": heart_twitch,
}

MODEL_GENES = {
    "dusk_bark": (dusk_bark_model, "dusk_bark_ledges"),
    "long_stare": (long_stare_model, "long_stare_eyes"),
    "stare_back": (stare_back_model, "stare_back_eye"),
    "long_limbs": (long_limbs_model, "long_limbs_arms"),
    "blink_strike": (blink_strike_model, "blink_strike_fingers"),
    "thorn_fist": (thorn_fist_model, "thorn_fist_thorns"),
    "close_guard": (close_guard_model, "close_guard_roots"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="creaking genes")
