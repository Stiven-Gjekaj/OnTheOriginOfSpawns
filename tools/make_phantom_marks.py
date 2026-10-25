"""Make the gene visuals of the phantom, from design/species/phantom.md.

Usage: python3 tools/make_phantom_marks.py (from the root of the repository)

The phantom texture is 64x64, and it uses the phantom region map. The parts:
- the head, 7x3x5 at uv (0, 0). Its front (5, 5) is 7x3, with the eyes on
  row 1 in columns 0 and 1 and columns 5 and 6. Row 0 of its bottom (12, 0)
  is at the front, so the mouth goes there.
- the body, 5x3x9 at uv (0, 8). Its top (9, 8) is the back, with row 8 at the
  neck. Row 0 of its bottom (14, 8) is at the chest.
- the tail: tail_base, 3x2x6 at uv (3, 20), and tail_tip, 1x1x6 at uv (4, 29).
- the wings: the wing base, 6x2x9 at uv (23, 12), and the wing tip, 13x1x9 at
  uv (16, 24). Column 0 of a top or a bottom is at the body. Row 8 of a top
  is at the front edge, and row 0 of a bottom is at the front edge. The two
  wings use the same uv, so a wing mark goes on both wings.
The game draws the glowing eyes (phantom_eyes.png) over the eye pixels, so a
mark on the eyes does not show. As for the eye genes of the enderman, each
eye gene of the phantom has a gene model of flat plates with full light over
the eyes.
The gene models use the bones head, tail_tip, left_wing_tip, and
right_wing_tip.

Pixel plan for the Swoopers.
- Talon Dive: long, hooked fangs, the gene model talon_dive_fangs.
- Rending Bite has a red mouth, with red drops on the front of the head.
- Wing Slam has heavy, dark wing edges: the front edges and the tips.
- Golem Diver has gray scars on the back.
- Steep Dive: a long, sharp tail, the gene model steep_dive_tail.
- First Dive has narrow, bright eyes: the plates first_dive_eyes, and dark
  brows.

Pixel plan for the Gliders.
- Swift Wing: long, thin wings, the gene model swift_wing_wings.
- Wide Circle has a pale stripe down the back, from the head to the tail.
- Feint has light, torn wing tips: pale, ragged tips with dark tears.
- Silent Wing has soft, gray wings.
- Wind Rider: a split tail, the gene model wind_rider_tail.
- Night Glide has a dark, blue body.

Pixel plan for the Nightwings.
- Sun Shade has dark, thick wings, almost black.
- Ember Hide has a scorched, black hide with orange embers.
- Pale Bone has white ribs on the body.
- Dusk Mend has green, glowing eyes: the plates dusk_mend_eyes, and a green
  glow round them.
- Moon Eye has one large, pale eye: the plate moon_eye_eye, and a pale ring.
- Night Terror has black, empty eyes: the plates night_terror_eyes, and dark
  sockets.

Pixel plan for the Shriekers.
- Shriek has a wide, open mouth: black, with a red inside.
- Blinding Cry has white rings round the eyes.
- Gnaw has long, thin teeth on the mouth.
- Sleepless Bite has purple marks on the back.
- Echo Cry has ripple lines on the wings.
- Flock Cry: a tall crest, the gene model flock_cry_crest.

Pixel plan for the Swarmwings.
- Big Flock has small spots on the back.
- Pack Wing has blue bands across the wings.
- Flock Shield has thick, dark scales on the body.
- Last Flight has a red mark on the chest.
- Flock Rally has clean, white wing tips, with a dark line on their inner
  edge.
- Swarm Dive: a forked, red tail, the gene model swarm_dive_tail.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/phantom.png"

HEAD = box_faces(0, 0, 7, 3, 5)
BODY = box_faces(0, 8, 5, 3, 9)
TAIL_BASE = box_faces(3, 20, 3, 2, 6)
TAIL_TIP = box_faces(4, 29, 1, 1, 6)
WING_BASE = box_faces(23, 12, 6, 2, 9)
WING_TIP = box_faces(16, 24, 13, 1, 9)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
WINGS = (WING_BASE, WING_TIP)
TAIL = (TAIL_BASE, TAIL_TIP)
FLAT = ("top", "bottom")
# The eyes on the front of the head: the columns of each eye, on row 1.
EYES = ((0, 1), (5, 6))
EYE_ROW = 1


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


def front(col, row):
    return at(HEAD, "front", col, row)


def front_edge(part, face, col, row):
    """Return True for the pixels of a wing face at its front edge.

    Row 8 of a top, and row 0 of a bottom, are at the front edge.
    """
    return (face == "top" and row >= 7) or (face == "bottom" and row <= 1) or face == "front"


# The depth of the eye plates comes from the place of the gene here, so that
# two plates are never in one plane.
EYE_ORDER = ("first_dive", "dusk_mend", "moon_eye", "night_terror")


def eye_plates(name, gene, color, eyes=(0, 1), cols=2, rows=1):
    """Save a gene model of flat plates with full light over the eyes.

    On the head bone, the front of the head is at z -5, and the eye row is
    from y -1 to 0. The right eye is from x -4 to -2, and the left eye from x
    1 to 3. A plate can be wider or higher than the eye.
    """
    im = model_image(16, 16)
    paint_box(im, 0, 0, cols, rows, 0, lambda face, x, y: color)
    z = round(-5.03 - 0.01 * EYE_ORDER.index(gene), 2)
    lefts = (-4, 1)
    parts = [{"bone": "head", "uv": [0, 0], "from": [lefts[e] - (cols - 2) / 2, -1 - (rows - 1) / 2, z],
              "size": [cols, rows, 0], "glow": True} for e in eyes]
    save_model(name, im, parts)


# The Swoopers.

def talon_dive():
    # The fangs are the gene model talon_dive_fangs.
    return []


def talon_dive_model():
    # Long, hooked fangs: two long, pale fangs hang down from the front of
    # the mouth, and their ends hook forward. On the bone head, the head is
    # from x -4 to 3, y -2 to 1, and z -5 to 0.
    im = model_image()
    paint_box(im, 0, 0, 1, 3, 1, lambda f, x, y: "#F0E8D0" if y < 2 else "#C8BCA0")
    paint_box(im, 0, 8, 1, 1, 1, lambda f, x, y: "#FFFFF0")
    parts = []
    for x in (-3.5, 1.5):
        parts.append({"bone": "head", "uv": [0, 0], "from": [x, 1, -4.8], "size": [1, 3, 1], "inflate": -0.1})
        parts.append({"bone": "head", "uv": [0, 8], "from": [x, 3.4, -5.6], "size": [1, 1, 1], "inflate": -0.15})
    save_model("talon_dive_fangs", im, parts)


def rending_bite():
    # A red mouth: the front of the bottom of the head is a red mouth with a
    # dark middle, and red drops run from it on the low row of the front.
    red = Mark("#C01818")
    dark = Mark("#5A0606")
    for x, y, f, col, row in pixels(HEAD, ("bottom",), rows=(0, 1, 2)):
        (dark if row == 1 and 1 <= col <= 5 else red).put((x, y))
    red.put([front(c, 2) for c in range(7)])
    dark.put([front(c, 2) for c in (1, 3, 5)])
    return [red, dark]


def wing_slam():
    # Heavy, dark wing edges: the front edges of the wings, and the outer
    # end of the wing tips, are a heavy, dark band.
    dark = Mark("#12121C")
    edge = Mark("#2A2A40")
    for wing in WINGS:
        for x, y, f, col, row in pixels(wing, FLAT + ("front",)):
            if front_edge(wing, f, col, row):
                dark.put((x, y))
    for x, y, f, col, row in pixels(WING_TIP, FLAT, cols=(11, 12)):
        edge.put((x, y))
    put(dark, WING_TIP, ("left",))
    return [dark, edge]


def golem_diver():
    # Gray scars on the back: pale gray scars that slant across the back and
    # the wing bases.
    scar = Mark("#A8A8B0")
    dark = Mark("#4A4A58")
    for s in (0, 3, 6):
        for n in range(4):
            col, row = n + (s % 2), s + n
            if col < 5 and row < 9:
                scar.put(at(BODY, "top", col, row))
                if row + 1 < 9:
                    dark.put(at(BODY, "top", col, row + 1))
    for s in (1, 5):
        for n in range(5):
            scar.put(at(WING_BASE, "top", n + 1, s + (n % 2)))
    return [scar, dark]


def steep_dive():
    # The tail is the gene model steep_dive_tail.
    return []


def steep_dive_model():
    # A long, sharp tail: the tail grows longer behind the tail tip, and ends
    # in a sharp, pale spike. On the bone tail_tip, the tip is from x -1 to
    # 0, y 0 to 1, and z 0 to 6.
    im = model_image()
    paint_box(im, 0, 0, 1, 1, 5, lambda f, x, y: "#2A3466")
    paint_box(im, 0, 8, 1, 1, 3, lambda f, x, y: "#E8E0C8")
    parts = [{"bone": "tail_tip", "uv": [0, 0], "from": [-1, 0, 6], "size": [1, 1, 5], "inflate": -0.05},
             {"bone": "tail_tip", "uv": [0, 8], "from": [-1, 0, 11], "size": [1, 1, 3], "inflate": -0.2}]
    save_model("steep_dive_tail", im, parts)


def first_dive():
    # Narrow, bright eyes: the eyes are the plates first_dive_eyes. Dark,
    # low brows go over the eyes, on the high row of the front and the front
    # row of the top.
    brow = Mark("#0A0A14")
    for cols in EYES:
        brow.put([front(c, 0) for c in (cols[0] - 1, cols[0], cols[1], cols[1] + 1) if 0 <= c < 7])
    put(brow, HEAD, ("top",), rows=(4,))
    return [brow]


def first_dive_model():
    eye_plates("first_dive_eyes", "first_dive", "#FFF8A0", cols=2, rows=1)


# The Gliders.

def swift_wing():
    # The wings are the gene model swift_wing_wings.
    return []


def swift_wing_model():
    # Long, thin wings: each wing tip grows a long, thin end, at its front
    # edge. On the bones left_wing_tip and right_wing_tip, the wing tip is 13
    # long, from x 0 to 13 (left) or -13 to 0 (right), and from z 0 to 9,
    # with the front edge at z 0.
    im = model_image()
    paint_box(im, 0, 0, 7, 1, 3, lambda f, x, y: "#C8B898" if f == "front" or y == 0 and f == "top"
              else "#3A4A88")
    parts = [{"bone": "left_wing_tip", "uv": [0, 0], "from": [13, 0, 0], "size": [7, 1, 3], "inflate": 0.0},
             {"bone": "right_wing_tip", "uv": [0, 0], "from": [-20, 0, 0], "size": [7, 1, 3], "inflate": 0.0,
              "mirror": True}]
    save_model("swift_wing_wings", im, parts)


def wide_circle():
    # A pale stripe down the back: a pale stripe down the middle of the head,
    # the back, and the tail.
    pale = Mark("#E8E0C8")
    put(pale, BODY, ("top",), cols=(2,))
    put(pale, HEAD, ("top",), cols=(3,))
    put(pale, TAIL_BASE, ("top",), cols=(1,))
    put(pale, TAIL_TIP, ("top",))
    put(pale, BODY, ("front", "back"), cols=(2,))
    return [pale]


def feint():
    # Light, torn wing tips: the outer five columns of the wing tips are a
    # pale tan, with dark tears that go in from the ends.
    pale = Mark("#D8C8A8")
    tear = Mark("#18182A")
    for x, y, f, col, row in pixels(WING_TIP, FLAT, cols=range(8, 13)):
        if col >= 10 and row % 3 == 1:
            tear.put((x, y))
        elif col >= 11 and row % 3 == 2:
            tear.put((x, y))
        else:
            pale.put((x, y))
    return [pale, tear]


def silent_wing():
    gray = Mark("#8A8C98")
    soft = Mark("#A8AAB4")
    for wing in WINGS:
        for x, y, f, col, row in pixels(wing, ALL):
            (soft if noise100(x, y, 2101) < 35 else gray).put((x, y))
    return [gray, soft]


def wind_rider():
    # The tail is the gene model wind_rider_tail.
    return []


def wind_rider_model():
    # A split tail: two long, thin prongs grow from the end of the tail tip
    # and part to the left and the right. On the bone tail_tip, the end of
    # the tip is at z 6.
    im = model_image()
    paint_box(im, 0, 0, 1, 1, 6, lambda f, x, y: "#5A6AA8" if f == "top" else "#3A4A88")
    parts = [{"bone": "tail_tip", "uv": [0, 0], "from": [-1, 0, 5.5], "size": [1, 1, 6], "inflate": -0.15,
              "pivot": [-0.5, 0.5, 6], "rotation": [0, a, 0]} for a in (-28, 28)]
    save_model("wind_rider_tail", im, parts)


def night_glide():
    blue = Mark("#1A2258")
    light = Mark("#2A3A80")
    for x, y, f, col, row in pixels(BODY, ALL):
        (light if noise100(x, y, 2201) < 25 else blue).put((x, y))
    for part in TAIL:
        put(blue, part, ALL)
    return [blue, light]


# The Nightwings.

def sun_shade():
    dark = Mark("#0E0C16")
    thick = Mark("#22182E")
    for wing in WINGS:
        for x, y, f, col, row in pixels(wing, ALL):
            (thick if (col + row) % 5 == 0 else dark).put((x, y))
    return [dark, thick]


def ember_hide():
    black = Mark("#141010")
    ember = Mark("#E8601A")
    for part in (BODY, HEAD) + TAIL:
        for x, y, f, col, row in pixels(part, ALL):
            if part is HEAD and f == "front":
                continue
            (ember if noise100(x, y, 2301) < 14 else black).put((x, y))
    return [black, ember]


def pale_bone():
    # White ribs on the body: white ribs across the back, the belly, and the
    # sides of the body, on every second row.
    rib = Mark("#F4F0E4")
    for x, y, f, col, row in pixels(BODY, FLAT):
        if row % 2 == 1:
            rib.put((x, y))
    for x, y, f, col, row in pixels(BODY, ("right", "left")):
        if col % 2 == 1:
            rib.put((x, y))
    return [rib]


def dusk_mend():
    # Green, glowing eyes: the eyes are the plates dusk_mend_eyes, two rows
    # high, and a soft green glow goes round them on the front of the head.
    glow = Mark("#3AA040")
    for cols in EYES:
        for c in range(cols[0] - 1, cols[1] + 2):
            if 0 <= c < 7:
                glow.put([front(c, 0), front(c, 2)])
    return [glow]


def dusk_mend_model():
    eye_plates("dusk_mend_eyes", "dusk_mend", "#40FF50", cols=2, rows=2)


def moon_eye():
    # A large, pale eye: the right eye is the large plate moon_eye_eye, and a
    # pale ring goes round it on the front and the side of the head.
    ring = Mark("#D8D8C8")
    ring.put([front(c, r) for c in (0, 1, 2) for r in (0, 2)] + [front(2, 1)])
    put(ring, HEAD, ("right",), cols=(3, 4), rows=(0, 1, 2))
    return [ring]


def moon_eye_model():
    eye_plates("moon_eye_eye", "moon_eye", "#F4F4E0", eyes=(0,), cols=3, rows=2)


def night_terror():
    # Black, empty eyes: the eyes are the black plates night_terror_eyes, and
    # dark sockets go round them.
    socket = Mark("#1A1424")
    for cols in EYES:
        for c in range(cols[0] - 1, cols[1] + 2):
            if 0 <= c < 7:
                socket.put([front(c, 0), front(c, 2)])
        socket.put([front(cols[0] - 1, 1)] if cols[0] else [])
        socket.put([front(cols[1] + 1, 1)] if cols[1] < 6 else [])
    return [socket]


def night_terror_model():
    eye_plates("night_terror_eyes", "night_terror", "#000000", cols=2, rows=1)


# The Shriekers.

def shriek():
    # A wide, open mouth: the bottom of the head is a wide, black mouth, red
    # inside, and the low row of the front is black.
    black = Mark("#0A0606")
    red = Mark("#8A1A1A")
    for x, y, f, col, row in pixels(HEAD, ("bottom",)):
        (red if 1 <= col <= 5 and 1 <= row <= 3 else black).put((x, y))
    black.put([front(c, 2) for c in range(7)])
    return [black, red]


def blinding_cry():
    # White rings round the eyes: white rings on the front of the head, and
    # round the front edge onto the sides and the top.
    white = Mark("#FFFFFF")
    for cols in EYES:
        for c in range(cols[0] - 1, cols[1] + 2):
            if 0 <= c < 7:
                white.put([front(c, 0), front(c, 2)])
    white.put([front(2, 1), front(4, 1)])
    put(white, HEAD, ("right",), cols=(4,))
    put(white, HEAD, ("left",), cols=(0,))
    put(white, HEAD, ("top",), rows=(4,), cols=(0, 1, 5, 6))
    return [white]


def gnaw():
    # Long, thin teeth: thin, white teeth with dark gaps along the front of
    # the mouth, on the bottom of the head and the low row of its front.
    tooth = Mark("#F8F4E8")
    gap = Mark("#1A1010")
    for x, y, f, col, row in pixels(HEAD, ("bottom",), rows=(0, 1, 2)):
        if col % 2 == 0:
            tooth.put((x, y))
        elif row < 2:
            gap.put((x, y))
    for c in range(7):
        (tooth if c % 2 == 0 else gap).put(front(c, 2))
    return [tooth, gap]


def sleepless_bite():
    purple = Mark("#8A30C8")
    dark = Mark("#4A1870")
    for part, seed in ((BODY, 2401), (WING_BASE, 2402)):
        for x, y, f, col, row in pixels(part, ("top",)):
            n = noise100(x // 2, y // 2, seed)
            if n < 35:
                (dark if noise100(x, y, seed + 9) < 30 else purple).put((x, y))
    return [purple, dark]


def echo_cry():
    # Ripple lines on the wings: pale blue lines in rings round the place
    # where the wing meets the body, on the tops and the bottoms.
    line = Mark("#9AC8E8")
    for wing, offset in ((WING_BASE, 0), (WING_TIP, 6)):
        for x, y, f, col, row in pixels(wing, FLAT):
            d = ((col + offset) ** 2 + (row - 4) ** 2) ** 0.5
            if int(d) % 4 == 2:
                line.put((x, y))
    return [line]


def flock_cry():
    # The crest is the gene model flock_cry_crest.
    return []


def flock_cry_model():
    # A tall crest: a tall, thin crest rises from the top of the head and
    # leans back, dark blue with a pale edge. On the bone head, the top of
    # the head is at y -2.
    im = model_image()
    paint_box(im, 0, 0, 1, 4, 4, lambda f, x, y: "#D8C8A8" if f == "top" or y == 0 else "#2A3A80")
    paint_box(im, 0, 12, 1, 3, 2, lambda f, x, y: "#D8C8A8" if y == 0 else "#3A4A98")
    parts = [{"bone": "head", "uv": [0, 0], "from": [-1, -6, -4], "size": [1, 4, 4], "inflate": 0.0},
             {"bone": "head", "uv": [0, 12], "from": [-1, -8.5, -2], "size": [1, 3, 2], "inflate": 0.0}]
    save_model("flock_cry_crest", im, parts)


# The Swarmwings.

def big_flock():
    spot = Mark("#E8E0C8")
    for part, seed in ((BODY, 2501), (WING_BASE, 2502), (HEAD, 2503)):
        for x, y, f, col, row in pixels(part, ("top",)):
            if noise100(x, y, seed) < 22:
                spot.put((x, y))
    return [spot]


def pack_wing():
    blue = Mark("#2A78E8")
    for x, y, f, col, row in pixels(WING_TIP, FLAT + ("front", "back"), cols=(3, 4, 8, 9)):
        blue.put((x, y))
    put(blue, WING_BASE, FLAT + ("front", "back"), cols=(3,))
    return [blue]


def flock_shield():
    # Thick, dark scales: dark scales with pale edges on the body and the
    # top of the head.
    scale = Mark("#1E1E28")
    edge = Mark("#6A6A80")
    for part in (BODY, HEAD):
        for x, y, f, col, row in pixels(part, ALL):
            if part is HEAD and f != "top":
                continue
            (edge if (row % 2 == 0 and (col + row // 2) % 2 == 0) else scale).put((x, y))
    return [scale, edge]


def last_flight():
    # A red mark on the chest: a red diamond on the front of the belly, and
    # a red band on the front of the body.
    red = Mark("#D01818")
    for row in range(0, 6):
        half = 2 - abs(row - 2.5) // 1
        for col in range(5):
            if abs(col - 2) <= half:
                red.put(at(BODY, "bottom", col, row))
    put(red, BODY, ("front",))
    return [red]


def flock_rally():
    # White wing tips: the outer three columns of the wing tips are a clean,
    # bright white, with a dark line on their inner edge, so the white reads
    # on a light sky too. The tips are whole, not torn.
    white = Mark("#FAFAFF")
    line = Mark("#14141E")
    put(white, WING_TIP, FLAT + ("front", "back"), cols=(10, 11, 12))
    put(white, WING_TIP, ("left",))
    put(line, WING_TIP, FLAT + ("front", "back"), cols=(9,))
    return [white, line]


def swarm_dive():
    # The tail is the gene model swarm_dive_tail.
    return []


def swarm_dive_model():
    # A forked, red tail: the tail tip is red, and ends in a short fork of
    # two red barbs that point out to the sides. On the bone tail_tip, the
    # tip is from x -1 to 0 and z 0 to 6.
    im = model_image()
    paint_box(im, 0, 0, 1, 1, 6, lambda f, x, y: "#C01818" if f != "top" else "#E03030")
    paint_box(im, 0, 8, 3, 1, 1, lambda f, x, y: "#E03030")
    paint_box(im, 0, 12, 1, 1, 2, lambda f, x, y: "#8A0A0A")
    parts = [{"bone": "tail_tip", "uv": [0, 0], "from": [-1, 0, 0], "size": [1, 1, 6], "inflate": 0.05},
             {"bone": "tail_tip", "uv": [0, 8], "from": [-2, 0, 6], "size": [3, 1, 1], "inflate": 0.0},
             {"bone": "tail_tip", "uv": [0, 12], "from": [-2.5, 0, 6.5], "size": [1, 1, 2], "inflate": -0.1},
             {"bone": "tail_tip", "uv": [0, 12], "from": [0.5, 0, 6.5], "size": [1, 1, 2], "inflate": -0.1}]
    save_model("swarm_dive_tail", im, parts)


GENES = {
    "talon_dive": talon_dive, "rending_bite": rending_bite, "wing_slam": wing_slam, "golem_diver": golem_diver,
    "steep_dive": steep_dive, "first_dive": first_dive,
    "swift_wing": swift_wing, "wide_circle": wide_circle, "feint": feint, "silent_wing": silent_wing,
    "wind_rider": wind_rider, "night_glide": night_glide,
    "sun_shade": sun_shade, "ember_hide": ember_hide, "pale_bone": pale_bone, "dusk_mend": dusk_mend,
    "moon_eye": moon_eye, "night_terror": night_terror,
    "shriek": shriek, "blinding_cry": blinding_cry, "gnaw": gnaw, "sleepless_bite": sleepless_bite,
    "echo_cry": echo_cry, "flock_cry": flock_cry,
    "big_flock": big_flock, "pack_wing": pack_wing, "flock_shield": flock_shield, "last_flight": last_flight,
    "flock_rally": flock_rally, "swarm_dive": swarm_dive,
}

MODEL_GENES = {
    "talon_dive": (talon_dive_model, "talon_dive_fangs"),
    "steep_dive": (steep_dive_model, "steep_dive_tail"),
    "first_dive": (first_dive_model, "first_dive_eyes"),
    "swift_wing": (swift_wing_model, "swift_wing_wings"),
    "wind_rider": (wind_rider_model, "wind_rider_tail"),
    "dusk_mend": (dusk_mend_model, "dusk_mend_eyes"),
    "moon_eye": (moon_eye_model, "moon_eye_eye"),
    "night_terror": (night_terror_model, "night_terror_eyes"),
    "flock_cry": (flock_cry_model, "flock_cry_crest"),
    "swarm_dive": (swarm_dive_model, "swarm_dive_tail"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="phantom genes")
