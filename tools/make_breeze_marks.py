"""Make the gene visuals of the breeze, from design/species/breeze.md.

Usage: python3 tools/make_breeze_marks.py (from the root of the repository)

The breeze texture is 32x32, and it uses the breeze region map. The parts:
- the head, 8x8x8 at uv (0, 0). Its front (8, 8) is the face. The eyes are on
  row 6, in columns 0 and 7. The game draws the glowing eyes
  (breeze_eyes.png) over them, so a mark on the eye pixels does not show.
  Row 7 is under the eyes. Row 0 of the bottom of the head (16, 0) is at the
  front, so a mouth goes on row 7 and on the front rows of the bottom.
- three rods, 2x8x2 at uv (0, 17). All of them use the same pixels.
- the wind, a second texture of 128x128, breeze_wind.png. A wind mark has the
  target "wind" and goes on that texture. The wind is see-through: only its
  thin streaks have pixels, and a mark keeps their alpha, so a wind mark
  tints the streaks. The streaks of each shell of the wind are in these
  places: the top shells on rows 18 to 25, the core of the top on rows 62 to
  69 at x 105 to 124, the middle shells on rows 40 to 45 at x 74 to 121, the
  core of the middle on rows 76 to 81 at x 49 to 68, and the bottom on rows
  88 to 94 at x 1 to 20.
The gene models use the bones head, rod_1, rod_2, rod_3, and body.

Pixel plan for the Gusters.
- Hard Gust has a wide, open mouth: dark, with a pale inside.
- Lift Gust: pale, curled rods, the gene model lift_gust_curls: a pale curl
  winds round each rod near the head. The rods keep their color.
- Shield Gust has a sharp, blue brow.
- Golem Gust has gray, heavy wind: a warm iron gray with rust spots.
- Disarm Gust has narrow, white eyes: white slits beside the eyes.
- Wide Gust: wide, spread wind, the gene model wide_gust_wind.

Pixel plan for the Deflectors.
- Wind Mirror has silver, bright rods.
- Gale Guard has a thick ring of wind: the top shells are a strong teal.
- Blast Calm has a calm, closed mouth: a thin, soft line.
- Fire Shear has orange, burnt tips on the rods.
- Steel Core has a gray, iron brow with rivets.
- Last Draft has rods that point down: dark chevrons that point down.

Pixel plan for the Bounders.
- Sky Hop: long, thin rods, the gene model sky_hop_rods.
- Fast Spring has wide, bright eyes: bright yellow round the eyes.
- Crash Down has a ring of dust at the base of the wind.
- Side Hop has rods that lean out: a bright outer side and a dark inner side.
- Swim Hop has blue, wet wind.
- Wind Ride has a mark of three lines on the brow.

Pixel plan for the Whirlers.
- Rapid Wind has fast, thin streaks: thin, mint lines in the wind.
- Twin Charge: two small mouths, the gene model twin_charge_mouths.
- Spin Up has rods with a dark blue spiral.
- True Aim has a thin, blue line down to the place between the eyes.
- Fast Charge has a pointed tip of wind: the bottom of the wind is gold.
- Eye of Storm has a dark ring round the eyes.

Pixel plan for the Tempests.
- Cut Wind has sharp, white edges: the streaks of the wind are cut into
  sharp dashes of white and deep navy.
- Chill Wind has pale blue wind.
- Dizzy Wind has spiral eyes.
- Choke Wind has a gray, tight mouth.
- Thunder Call has yellow, charged tips on the rods.
- Storm Gale has a dark, storm gray wind with flashes.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/breeze.png"

HEAD = box_faces(0, 0, 8, 8, 8)
ROD = box_faces(0, 17, 2, 8, 2)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
RODS = ("rod_1", "rod_2", "rod_3")

# The streaks of the wind: (x, y, width, height) of each shell.
WIND = {"top": [(0, 18, 72, 8)], "top_core": [(105, 62, 20, 8)], "mid": [(74, 40, 48, 6)],
        "mid_core": [(49, 76, 20, 6)], "bottom": [(1, 88, 20, 7)]}
WIND_ALL = ("top", "top_core", "mid", "mid_core", "bottom")


def wind_mark(tint):
    return Mark(tint, size=128, height=128, target="wind")


def wind_pixels(parts=WIND_ALL):
    """Yield (x, y, row) for the pixels of the wind shells. row counts from the top of each shell."""
    for part in parts:
        for x0, y0, w, h in WIND[part]:
            for y in range(y0, y0 + h):
                for x in range(x0, x0 + w):
                    yield x, y, y - y0


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


def face(col, row):
    return at(HEAD, "front", col, row)


def put(mark, part, faces=ALL, cols=None, rows=None):
    mark.put([(x, y) for x, y, *_ in pixels(part, faces, cols, rows)])
    return mark


def put_face(mark, cells):
    mark.put([face(c, r) for c, r in cells if 0 <= c < 8 and 0 <= r < 8])


def mouth(mark, cols, bottom_rows=(0, 1)):
    """Put a mouth on row 7 of the face and on the front rows of the bottom of the head."""
    put_face(mark, [(c, 7) for c in cols])
    mark.put([at(HEAD, "bottom", c, r) for c in cols for r in bottom_rows])


def eye_sides(mark, rows=(6,), cols=(6, 7)):
    """Put the parts of the eyes on the sides of the head, at the front corners."""
    mark.put([at(HEAD, "right", c, r) for c in cols for r in rows])
    mark.put([at(HEAD, "left", 7 - c, r) for c in cols for r in rows])


def rod_tips(mark, rows=(5, 6, 7)):
    put(mark, ROD, SIDES, rows=rows)
    put(mark, ROD, ("bottom",))


# The Gusters.

def hard_gust():
    # A wide, open mouth: the low face is dark navy, so the open mouth has a
    # white rim round a black inside. It is wide and tall, on rows 4 to 7 of
    # the face, on the front of the bottom of the head, and its corners go
    # round onto the sides.
    rim = Mark("#F4F4FF")
    dark = Mark("#06060E")
    for c in range(1, 7):
        for r in (4, 5, 6, 7):
            (dark if 2 <= c <= 5 and r >= 5 else rim).put(face(c, r))
    dark.put([at(HEAD, "bottom", c, r) for c in range(2, 6) for r in (0, 1, 2, 3)])
    rim.put([at(HEAD, "bottom", c, 4) for c in range(1, 7)] + [at(HEAD, "bottom", c, r) for c in (1, 6)
                                                                for r in (0, 1, 2, 3)])
    eye_sides(rim, rows=(7,), cols=(5, 6, 7))
    return [rim, dark]


def lift_gust():
    # The rods keep their own color. The curl is the gene model
    # lift_gust_curls.
    return []


def lift_gust_model():
    # Pale, curled rods: a pale curl of wind winds round each rod near the
    # head, in four short steps that go round the rod, with a darker lavender
    # edge. On each rod bone, the rod is from x -1 to 1, y 0 to 8, and z -3
    # to -1, and y 0 is the end near the head.
    im = model_image()
    paint_box(im, 0, 0, 3, 1, 1, lambda f, x, y: "#F4F0FF" if f in ("top", "front") else "#7A6AC8")
    steps = ((-1.5, 0.5, -3.8), (1.2, 1.5, -3.5), (-1.5, 2.5, -1.2), (-2.2, 3.5, -3.5))
    parts = []
    for n, (x, y, z) in enumerate(steps):
        turn = 90 if n % 2 else 0
        for bone in RODS:
            parts.append({"bone": bone, "uv": [0, 0], "from": [x, y, z], "size": [3, 1, 1], "inflate": -0.1,
                          "pivot": [0, y + 0.5, -2], "rotation": [0, turn, 0]})
    save_model("lift_gust_curls", im, parts)


def shield_gust():
    # A sharp, blue brow: a bright blue brow that slants down to a point
    # between the eyes, on rows 1 to 4.
    blue = Mark("#2A6AFF")
    put_face(blue, [(0, 1), (1, 1), (1, 2), (2, 2), (2, 3), (3, 3), (3, 4), (4, 4), (4, 3), (5, 3), (5, 2),
                    (6, 2), (6, 1), (7, 1)])
    return [blue]


def golem_gust():
    # Gray, heavy wind: a warm iron gray wind, with rust spots.
    iron = wind_mark("#8A7A6A")
    rust = wind_mark("#A8501E")
    for x, y, row in wind_pixels():
        (rust if noise100(x, y, 4401) < 18 else iron).put((x, y))
    return [iron, rust]


def disarm_gust():
    # Narrow, white eyes: a thin, white slit goes out from each eye on row 6,
    # on the face and round the corner onto the side, under a dark lid on
    # row 5.
    white = Mark("#FFFFFF")
    lid = Mark("#141428")
    put_face(white, [(1, 6), (2, 6), (5, 6), (6, 6)])
    put_face(lid, [(c, 5) for c in (0, 1, 2, 5, 6, 7)])
    eye_sides(white, rows=(6,), cols=(4, 5, 6))
    eye_sides(lid, rows=(5,), cols=(4, 5, 6, 7))
    return [white, lid]


def wide_gust():
    # The spread wind is the gene model wide_gust_wind.
    return []


def wide_gust_model():
    # Wide, spread wind: four thin streaks of wind spread out from the low
    # body, at the height of the rods, as arms of a gust. Each arm has gaps.
    # On the bone body, the rods are at y 5.
    im = model_image()

    def streak(f, x, y):
        if (x * 3 + y) % 5 == 0:
            return None
        return "#C8D8FF" if y == 0 else "#6A80D0"
    paint_box(im, 0, 0, 10, 1, 1, streak)
    parts = []
    for angle in (45, 135, 225, 315):
        parts.append({"bone": "body", "uv": [0, 0], "from": [4, 10, -0.5], "size": [10, 1, 1], "inflate": -0.2,
                      "pivot": [0, 10, 0], "rotation": [0, angle, -8]})
    save_model("wide_gust_wind", im, parts)


# The Deflectors.

def wind_mirror():
    # Silver, bright rods: the rods are a bright silver, with dark steel
    # edges and a white shine down the middle.
    silver = Mark("#C8D0E0")
    edge = Mark("#3A4058")
    shine = Mark("#FFFFFF")
    for x, y, f, col, row in pixels(ROD, ALL):
        if f in ("right", "left"):
            edge.put((x, y))
        elif f in SIDES and col == 0:
            shine.put((x, y))
        else:
            silver.put((x, y))
    return [silver, edge, shine]


def gale_guard():
    teal = wind_mark("#30A0B0")
    teal.put([(x, y) for x, y, row in wind_pixels(("top",))])
    return [teal]


def blast_calm():
    # A calm, closed mouth: pale pink lips across the low face, on rows 5 and
    # 7, with a deep red seam between them on row 6. The lips go on across
    # the front of the bottom of the head and round the low corners onto the
    # sides.
    lip = Mark("#F4C8DC")
    seam = Mark("#8A2040")
    put_face(lip, [(c, 5) for c in range(1, 7)] + [(c, 7) for c in range(1, 7)])
    put_face(seam, [(c, 6) for c in range(1, 7)])
    lip.put([at(HEAD, "bottom", c, r) for c in range(1, 7) for r in (0, 2)])
    seam.put([at(HEAD, "bottom", c, 1) for c in range(1, 7)])
    eye_sides(lip, rows=(7,), cols=(4, 5, 6, 7))
    return [lip, seam]


def fire_shear():
    orange = Mark("#FF7A20")
    black = Mark("#1A1010")
    rod_tips(orange, (4, 5))
    rod_tips(black, (6, 7))
    return [orange, black]


def steel_core():
    # A gray, iron brow: a gray iron plate across rows 2 to 4 of the face,
    # with pale rivets, and onto the top of the head.
    iron = Mark("#6A6E78")
    rivet = Mark("#D8DCE4")
    for c in range(8):
        for r in (2, 3, 4):
            ((rivet if r == 3 and c in (1, 6) else iron)).put(face(c, r))
    put(iron, HEAD, ("top",), rows=(7,))
    return [iron, rivet]


def last_draft():
    dark = Mark("#1E2450")
    for x, y, f, col, row in pixels(ROD, SIDES):
        if (row - col) % 3 == 0 or (row + col) % 3 == 1 and row % 3 == 1:
            dark.put((x, y))
    return [dark]


# The Bounders.

def sky_hop():
    # The long rods are the gene model sky_hop_rods.
    return []


def sky_hop_model():
    # Long, thin rods: a long, thin, bright end grows from the end of each
    # rod, cyan white with a strong blue tip. On each rod bone, the rod is
    # from x -1 to 1, y 0 to 8, and z -3 to -1.
    im = model_image()
    paint_box(im, 0, 0, 1, 9, 1, lambda f, x, y: "#E0FFFF" if y < 5 else "#2A60FF")
    parts = [{"bone": bone, "uv": [0, 0], "from": [-0.5, 8, -2.5], "size": [1, 9, 1], "inflate": 0.1}
             for bone in RODS]
    save_model("sky_hop_rods", im, parts)


def fast_spring():
    # Wide, bright eyes: a bright yellow frame round each eye, from row 4 to
    # row 7, on the face and on the sides, so the eyes look wide.
    yellow = Mark("#FFE840")
    put_face(yellow, [(c, r) for c in (0, 1, 2, 5, 6, 7) for r in (4, 5, 7)] + [(1, 6), (2, 6), (5, 6), (6, 6)])
    eye_sides(yellow, rows=(4, 5, 7), cols=(5, 6, 7))
    return [yellow]


def crash_down():
    dust = wind_mark("#A08060")
    dark = wind_mark("#6A5040")
    for x, y, row in wind_pixels(("bottom", "mid_core")):
        (dark if noise100(x, y, 4101) < 30 else dust).put((x, y))
    return [dust, dark]


def side_hop():
    bright = Mark("#FFFFFF")
    dark = Mark("#2A2A5A")
    put(bright, ROD, ("right",))
    put(dark, ROD, ("left",))
    put(bright, ROD, ("front", "back"), cols=(0,))
    put(dark, ROD, ("front", "back"), cols=(1,))
    return [bright, dark]


def swim_hop():
    blue = wind_mark("#2060E0")
    drop = wind_mark("#C8E8FF")
    for x, y, row in wind_pixels():
        (drop if noise100(x, y, 4201) < 12 else blue).put((x, y))
    return [blue, drop]


def wind_ride():
    # A mark of three lines: three cyan lines across the brow, each a little
    # shorter, and the first goes onto the top of the head.
    cyan = Mark("#30E0F0")
    put_face(cyan, [(c, 0) for c in range(8)] + [(c, 2) for c in range(1, 7)] + [(c, 4) for c in range(2, 6)])
    put(cyan, HEAD, ("top",), rows=(6,))
    return [cyan]


# The Whirlers.

def rapid_wind():
    mint = wind_mark("#90FFD0")
    mint.put([(x, y) for x, y, row in wind_pixels() if row % 3 == 0])
    return [mint]


def twin_charge():
    # The mouths are the gene model twin_charge_mouths.
    return []


def twin_charge_model():
    # Two small mouths: two small mouths, apart, between the eyes on
    # the low face, that stick out a little. Each has a dark edge round a
    # cyan front with a black hole in its low middle. On the bone head, the
    # face is at z -4, the low row is from y -1 to 0, and the eyes are at x
    # -4 and 3.
    im = model_image()

    def mouth(f, x, y):
        if f != "front":
            return "#0A0A1A"
        if (x, y) == (1, 1):
            return "#000000"
        return "#60E0FF"
    paint_box(im, 0, 0, 2, 2, 2, mouth)
    parts = [{"bone": "head", "uv": [0, 0], "from": [x, -2, -5.5], "size": [2, 2, 2], "inflate": -0.1}
             for x in (-3, 1)]
    save_model("twin_charge_mouths", im, parts)


def spin_up():
    blue = Mark("#1E2A80")
    for x, y, f, col, row in pixels(ROD, SIDES):
        if (row + col + 2 * SIDES.index(f)) % 4 == 0:
            blue.put((x, y))
    return [blue]


def true_aim():
    # A blue mark between the eyes: a thin, bright blue line, one pixel wide,
    # runs from the back of the top of the head, over the front edge, and
    # down the middle of the face to the low row between the eyes, with a
    # small, pale blue point on each side at its low end.
    blue = Mark("#2A8AFF")
    pale = Mark("#B8E0FF")
    put_face(blue, [(4, r) for r in range(0, 8)])
    put_face(pale, [(3, 6), (5, 6)])
    put(blue, HEAD, ("top",), cols=(4,))
    put(blue, HEAD, ("back",), cols=(3,), rows=(0, 1, 2))
    return [blue, pale]


def fast_charge():
    # A pointed tip of wind: the bottom of the wind, its tip, is a bright
    # gold, and the core of the middle over it is a paler gold.
    gold = wind_mark("#FFC820")
    pale = wind_mark("#FFE890")
    gold.put([(x, y) for x, y, row in wind_pixels(("bottom",))])
    pale.put([(x, y) for x, y, row in wind_pixels(("mid_core",))])
    return [gold, pale]


def eye_of_storm():
    # A dark ring round the eyes: a black ring round each eye, with a pale
    # outer edge so it shows on the dark low face, on the front and round the
    # corners onto the sides.
    ring = Mark("#06060E")
    edge = Mark("#D8D4F8")
    put_face(ring, [(c, r) for c in (0, 1, 2) for r in (5, 7)] + [(2, 6)] +
             [(c, r) for c in (5, 6, 7) for r in (5, 7)] + [(5, 6)])
    put_face(edge, [(c, 4) for c in (0, 1, 2, 3, 4, 5, 6, 7)] + [(3, 5), (3, 6), (3, 7), (4, 5), (4, 6), (4, 7)])
    eye_sides(ring, rows=(5, 6, 7), cols=(5, 6, 7))
    eye_sides(edge, rows=(4,), cols=(4, 5, 6, 7))
    return [ring, edge]


# The Tempests.

def cut_wind():
    # Sharp, white edges: each streak of the wind is cut into short, sharp
    # dashes of white and deep navy, as blades of wind.
    white = wind_mark("#FFFFFF")
    navy = wind_mark("#1E2A60")
    for x, y, row in wind_pixels():
        (white if (x // 2 + row) % 2 == 0 else navy).put((x, y))
    return [white, navy]


def chill_wind():
    pale = wind_mark("#A8DCFF")
    pale.put([(x, y) for x, y, row in wind_pixels()])
    return [pale]


def dizzy_wind():
    # Spiral eyes: a purple spiral round each eye, on the face and round the
    # corners onto the sides.
    purple = Mark("#A040E0")
    pale = Mark("#E8C8FF")
    put_face(purple, [(0, 5), (1, 5), (2, 5), (2, 6), (2, 7), (1, 7), (5, 5), (6, 5), (7, 5), (5, 6), (5, 7),
                      (6, 7)])
    put_face(pale, [(1, 6), (6, 6), (0, 7), (7, 7)])
    eye_sides(purple, rows=(5,), cols=(5, 6, 7))
    eye_sides(pale, rows=(7,), cols=(6, 7))
    return [purple, pale]


def choke_wind():
    # A gray, tight mouth: a large, gray mouth across rows 5 to 7, with a
    # pale gray rim over it and black pinched corners, a gray jaw on the
    # front of the bottom of the head, and gray at the low corners of the
    # sides.
    gray = Mark("#6A6A78")
    rim = Mark("#B8B8C4")
    pinch = Mark("#08080C")
    put_face(rim, [(c, 4) for c in range(1, 7)])
    put_face(gray, [(c, r) for c in range(2, 6) for r in (5, 6, 7)])
    put_face(pinch, [(1, 5), (6, 5), (1, 6), (6, 6), (1, 7), (6, 7)])
    gray.put([at(HEAD, "bottom", c, r) for c in range(1, 7) for r in (0, 1, 2, 3)])
    eye_sides(gray, rows=(7,), cols=(3, 4, 5, 6, 7))
    eye_sides(rim, rows=(6,), cols=(3, 4, 5))
    return [gray, rim, pinch]


def thunder_call():
    yellow = Mark("#FFE820")
    white = Mark("#FFFFF0")
    rod_tips(yellow, (5, 6, 7))
    for x, y, f, col, row in pixels(ROD, SIDES, rows=(1, 2, 3, 4)):
        if (row + col) % 3 == 0:
            yellow.put((x, y))
    put(white, ROD, ("bottom",))
    return [yellow, white]


def storm_gale():
    dark = wind_mark("#2A3040")
    flash = wind_mark("#FFF8A0")
    for x, y, row in wind_pixels():
        (flash if noise100(x, y, 4301) < 6 else dark).put((x, y))
    return [dark, flash]


GENES = {
    "hard_gust": hard_gust, "lift_gust": lift_gust, "shield_gust": shield_gust, "golem_gust": golem_gust,
    "disarm_gust": disarm_gust, "wide_gust": wide_gust,
    "wind_mirror": wind_mirror, "gale_guard": gale_guard, "blast_calm": blast_calm, "fire_shear": fire_shear,
    "steel_core": steel_core, "last_draft": last_draft,
    "sky_hop": sky_hop, "fast_spring": fast_spring, "crash_down": crash_down, "side_hop": side_hop,
    "swim_hop": swim_hop, "wind_ride": wind_ride,
    "rapid_wind": rapid_wind, "twin_charge": twin_charge, "spin_up": spin_up, "true_aim": true_aim,
    "fast_charge": fast_charge, "eye_of_storm": eye_of_storm,
    "cut_wind": cut_wind, "chill_wind": chill_wind, "dizzy_wind": dizzy_wind, "choke_wind": choke_wind,
    "thunder_call": thunder_call, "storm_gale": storm_gale,
}

MODEL_GENES = {
    "wide_gust": (wide_gust_model, "wide_gust_wind"),
    "sky_hop": (sky_hop_model, "sky_hop_rods"),
    "lift_gust": (lift_gust_model, "lift_gust_curls"),
    "twin_charge": (twin_charge_model, "twin_charge_mouths"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="breeze genes")
