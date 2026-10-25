"""Make the gene visuals of the blaze, from design/species/blaze.md.

Usage: python3 tools/make_blaze_marks.py (from the root of the repository)

The blaze texture is 64x32, and it uses the blaze region map. The parts:
- the head, 8x8x8 at uv (0, 0). Its front (8, 8) is the face. The eyes are on
  row 3, in columns 1 and 2 and columns 5 and 6. Each eye is a white pixel
  and a dark pupil.
- twelve rods, part0 to part11, 2x8x2 at uv (0, 16). All the rods use the
  same pixels, so a rod mark goes on all twelve rods. Row 0 of a rod side is
  its top.
The texture is bright yellow and orange, so the marks use dark, pale, or
cold colors that read on it.
The gene models use the bones part0 to part11.

Pixel plan for the Searers.
- White Heat has white, hot eyes with an orange glow.
- Blast Ember has red, cracked rods: dark red rods with black cracks.
- Shield Scorch has a red, angry brow: a scorched, dark red band with black
  spots across the brow.
- Golem Melt has orange, molten tips: the low ends of the rods melt, with
  drips.
- Sear Touch has a burning, open mouth: dark, with a bright fire inside.
- Swift Shot has narrow, yellow eyes under dark lids.

Pixel plan for the Volleyers.
- Long Burst: twelve thin rods, the gene model long_burst_rods: a thin rod
  beside each rod.
- Quick Burst has three thin, yellow rings on each rod, with dark gaps.
- Fan Shot: spread, wide rods, the gene model fan_shot_fins: a second rod
  that leans out from each rod.
- Seeker Spark has a gold mark on the brow.
- Spark Ring has one thick, gold band round the middle of each rod.
- Soot Cloud has black ash on the face.

Pixel plan for the Smolderers.
- Deep Burn has dark red eyes in dark sockets.
- Fire Trail has black, sooty rods.
- Burn Pain has a deep, cruel frown.
- Fire Feed has rods that glow at the base: the high ends glow white.
- Rod Wall: thick, short rods, the gene model rod_wall_collars.
- Heat Haze has pale, faded eyes on a faded face.

Pixel plan for the Hoverers.
- Rising Heat has rods that point up: pale chevrons up the rods.
- Air Dodge has light, wide eyes: large, pale blue eyes.
- Quick Wings: thin, long rods, the gene model quick_wings_rods: a thin end
  under each rod.
- Smoke Dash has gray smoke round the head.
- Sky Guard has pale, ash gray rods.
- Diving Flame has a sharp, red brow: a thin, red V.

Pixel plan for the Steamhides.
- Steam Shell has wet, blue gray rods with a shine.
- Snow Shrug has blue, cold eyes with frost.
- Boil Off has white steam rings on the rods.
- Hot Core has a dark core that glows: a dark head with glowing cracks.
- Cinder Heart has a bright heart on the bottom of the head, the chest of
  the blaze, and a glow round the low edge of the head.
- Steam Burst has rods with open vent slits that let out steam at the top.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/blaze.png"

HEAD = box_faces(0, 0, 8, 8, 8)
ROD = box_faces(0, 16, 2, 8, 2)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
SHELL = ("top", "bottom", "right", "left", "back")
EYES = ((1, 2), (5, 6))
EYE_ROW = 3
RODS = [f"part{i}" for i in range(12)]


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


def rod_rows(mark, rows, faces=SIDES):
    put(mark, ROD, faces, rows=rows)


def rod_parts(name, w, h, d, color, frm, inflate=0.0):
    """Save a gene model of one part on each rod, from the same place on each."""
    im = model_image()
    paint_box(im, 0, 0, w, h, d, color)
    parts = [{"bone": bone, "uv": [0, 0], "from": list(frm), "size": [w, h, d], "inflate": inflate}
             for bone in RODS]
    save_model(name, im, parts)


# The Searers.

def white_heat():
    white = Mark("#FFFFFF")
    glow = Mark("#FF7A10")
    for cols in EYES:
        put_face(white, [(c, r) for c in cols for r in (3, 4)])
        put_face(glow, [(c, 2) for c in range(cols[0] - 1, cols[1] + 2)] +
                 [(cols[0] - 1, r) for r in (3, 4)] + [(cols[1] + 1, r) for r in (3, 4)] +
                 [(c, 5) for c in cols])
    return [white, glow]


def blast_ember():
    red = Mark("#8A1008")
    crack = Mark("#1A0404")
    hot = Mark("#FF4020")
    for x, y, f, col, row in pixels(ROD, ALL):
        if f in SIDES and (row + col + SIDES.index(f)) % 3 == 0:
            (hot if row % 2 else crack).put((x, y))
        else:
            red.put((x, y))
    return [red, crack, hot]


def shield_scorch():
    # A red, angry brow: a scorched, dark red band across rows 1 and 2 of the
    # face, with black scorch spots, and onto the top of the head.
    red = Mark("#8A1010")
    black = Mark("#1A0806")
    for c in range(8):
        for r in (1, 2):
            (black if (c + r) % 3 == 0 else red).put(face(c, r))
    put(red, HEAD, ("top",), rows=(7,))
    return [red, black]


def golem_melt():
    # Orange, molten tips: rows 5 to 7 of the rods and their bottom are a
    # bright, molten orange with pale yellow drips.
    molten = Mark("#FF6A00")
    drip = Mark("#FFE090")
    for x, y, f, col, row in pixels(ROD, SIDES, rows=(4, 5, 6, 7)):
        (drip if (row == 7 or (row == 6 and col == 0)) else molten).put((x, y))
    put(drip, ROD, ("bottom",))
    return [molten, drip]


def sear_touch():
    # A burning, open mouth: a dark mouth on rows 5 and 6, with a bright
    # yellow and white fire inside, and dark corners.
    dark = Mark("#2A0804")
    fire = Mark("#FFF0A0")
    put_face(dark, [(c, r) for c in range(1, 7) for r in (5, 6, 7)])
    put_face(fire, [(c, 6) for c in (2, 3, 4, 5)] + [(3, 5), (4, 5)])
    return [dark, fire]


def swift_shot():
    lid = Mark("#3A1404")
    yellow = Mark("#FFFF60")
    for cols in EYES:
        put_face(lid, [(c, 2) for c in range(cols[0] - 1, cols[1] + 2)])
        put_face(yellow, [(c, 3) for c in range(cols[0] - 1, cols[1] + 2)])
        put_face(lid, [(c, 4) for c in cols])
    return [lid, yellow]


# The Volleyers.

def long_burst():
    # The thin rods are the gene model long_burst_rods.
    return []


def long_burst_model():
    # Twelve thin rods: a thin, dark orange rod stands beside each rod, a
    # little higher. On each rod bone, the rod is from x 0 to 2, y 0 to 8,
    # and z 0 to 2.
    rod_parts("long_burst_rods", 1, 9, 1, lambda f, x, y: "#FFE060" if y < 2 else "#C04A08", (2.6, -2, 0.5))


def quick_burst():
    yellow = Mark("#FFFF80")
    dark = Mark("#4A1404")
    rod_rows(yellow, (1, 4, 7))
    rod_rows(dark, (2, 5))
    return [yellow, dark]


def fan_shot():
    # The fins are the gene model fan_shot_fins.
    return []


def fan_shot_model():
    # Spread, wide rods: a second, red rod grows from the top of each rod and
    # leans out by 30 degrees, so each rod spreads wide.
    im = model_image()
    paint_box(im, 0, 0, 2, 7, 2, lambda f, x, y: "#FFD040" if y < 2 else "#C83A08" if y < 5 else "#7A1A04")
    parts = [{"bone": bone, "uv": [0, 0], "from": [0, 0.5, 0], "size": [2, 7, 2], "inflate": -0.2,
              "pivot": [1, 0.5, 1], "rotation": [0, 0, -30]} for bone in RODS]
    save_model("fan_shot_fins", im, parts)


def seeker_spark():
    # A gold mark on the brow: a gold diamond with a white point between the
    # eyes, and a gold line up onto the top of the head.
    gold = Mark("#C08010")
    point = Mark("#FFFFFF")
    put_face(gold, [(3, 0), (4, 0), (2, 1), (3, 1), (4, 1), (5, 1), (3, 2), (4, 2)])
    put_face(point, [(3, 1), (4, 1)])
    put(gold, HEAD, ("top",), cols=(3, 4), rows=(5, 6, 7))
    return [gold, point]


def spark_ring():
    gold = Mark("#C88A10")
    rim = Mark("#4A2004")
    spark = Mark("#FFFFFF")
    rod_rows(rim, (2, 5))
    rod_rows(gold, (3, 4))
    for x, y, f, col, row in pixels(ROD, ("front", "back"), rows=(3,)):
        if col == 0:
            spark.put((x, y))
    return [gold, rim, spark]


def soot_cloud():
    black = Mark("#141010")
    gray = Mark("#4A4444")
    for x, y, f, col, row in pixels(HEAD, ("front",)):
        if row == EYE_ROW and (col in EYES[0] or col in EYES[1]):
            continue
        n = noise100(x, y, 3101)
        if n < 45:
            (gray if n < 15 else black).put((x, y))
    return [black, gray]


# The Smolderers.

def deep_burn():
    red = Mark("#7A0808")
    socket = Mark("#2A0604")
    for cols in EYES:
        put_face(red, [(c, 3) for c in cols])
        put_face(socket, [(c, r) for c in range(cols[0] - 1, cols[1] + 2) for r in (2, 4)] +
                 [(cols[0] - 1, 3), (cols[1] + 1, 3)])
    return [red, socket]


def fire_trail():
    black = Mark("#141010")
    soot = Mark("#5A5450")
    for x, y, f, col, row in pixels(ROD, ALL):
        (soot if noise100(x, y, 3201) < 25 else black).put((x, y))
    return [black, soot]


def burn_pain():
    # A deep, cruel frown: a dark mouth that turns down at its corners, and
    # dark brows that slant down to the middle.
    dark = Mark("#2A0604")
    put_face(dark, [(2, 5), (3, 5), (4, 5), (5, 5), (1, 6), (6, 6), (1, 7), (6, 7)])
    put_face(dark, [(0, 1), (1, 1), (2, 2), (3, 2), (7, 1), (6, 1), (5, 2), (4, 2)])
    return [dark]


def fire_feed():
    # Rods that glow at the base: the high ends of the rods glow white and
    # pale yellow, and the rest of each rod is a darker red.
    white = Mark("#FFFFF0")
    pale = Mark("#FFF0A0")
    red = Mark("#A02808")
    put(white, ROD, ("top",))
    rod_rows(white, (0,))
    rod_rows(pale, (1, 2))
    rod_rows(red, (4, 5, 6, 7))
    return [white, pale, red]


def rod_wall():
    # The collars are the gene model rod_wall_collars.
    return []


def rod_wall_model():
    # Thick, short rods: a thick, dark collar round the high half of each
    # rod, larger than the rod, so the rods look thick and short.
    rod_parts("rod_wall_collars", 2, 4, 2, lambda f, x, y: "#5A2006" if y % 2 else "#8A3A0A", (0, 0, 0),
              inflate=0.6)


def heat_haze():
    # Pale, faded eyes on a faded face: the face is a pale, faded cream, and
    # the eyes are a pale, faded gray.
    cream = Mark("#F4E8C8")
    pale = Mark("#B8B0A8")
    for x, y, f, col, row in pixels(HEAD, ("front",)):
        if row == EYE_ROW and (col in EYES[0] or col in EYES[1]):
            pale.put((x, y))
        elif noise100(x, y, 3301) < 70:
            cream.put((x, y))
    return [cream, pale]


# The Hoverers.

def rising_heat():
    # Rods that point up: pale chevrons point up on the sides of the rods,
    # and the tops of the rods are white.
    pale = Mark("#FFFFE0")
    for x, y, f, col, row in pixels(ROD, SIDES):
        if (row + col) % 3 == 0 and row % 3 != 2:
            pale.put((x, y))
    put(pale, ROD, ("top",))
    return [pale]


def air_dodge():
    pale = Mark("#C8F0FF")
    rim = Mark("#2A3A50")
    for cols in EYES:
        put_face(pale, [(c, r) for c in cols for r in (2, 3, 4)])
        put_face(rim, [(cols[0] - 1, r) for r in (2, 3, 4)] + [(cols[1] + 1, r) for r in (2, 3, 4)])
    return [pale, rim]


def quick_wings():
    # The thin ends are the gene model quick_wings_rods.
    return []


def quick_wings_model():
    # Thin, long rods: a thin, pale end grows down from the bottom of each
    # rod, so the rods look long and thin.
    rod_parts("quick_wings_rods", 1, 5, 1, lambda f, x, y: "#FFF4C0" if y < 3 else "#E0C890", (0.5, 8, 0.5))


def smoke_dash():
    smoke = Mark("#8A8480")
    light = Mark("#B8B2AC")
    for x, y, f, col, row in pixels(HEAD, SHELL):
        n = noise100(x // 2, y // 2, 3401)
        if n < 45:
            (light if noise100(x, y, 3402) < 35 else smoke).put((x, y))
    for c in (0, 7):
        put_face(smoke, [(c, r) for r in range(8) if noise100(c, r, 3403) < 60])
    return [smoke, light]


def sky_guard():
    ash = Mark("#B8B4B0")
    dark = Mark("#8A8682")
    for x, y, f, col, row in pixels(ROD, ALL):
        (dark if noise100(x, y, 3501) < 25 else ash).put((x, y))
    return [ash, dark]


def diving_flame():
    # A sharp, red brow: a thin, red V that points down between the eyes,
    # with a red flame point up on the brow.
    red = Mark("#E01010")
    put_face(red, [(0, 0), (1, 1), (2, 1), (3, 2), (4, 2), (5, 1), (6, 1), (7, 0)])
    put_face(red, [(3, 0), (4, 0)])
    return [red]


# The Steamhides.

def steam_shell():
    gray = Mark("#5A6878")
    shine = Mark("#B8D0E8")
    for x, y, f, col, row in pixels(ROD, ALL):
        (shine if f in SIDES and col == 0 and row % 3 == 1 else gray).put((x, y))
    return [gray, shine]


def snow_shrug():
    blue = Mark("#40A0FF")
    frost = Mark("#E8F8FF")
    for cols in EYES:
        put_face(blue, [(c, 3) for c in cols])
        put_face(frost, [(c, 2) for c in range(cols[0] - 1, cols[1] + 2)] + [(c, 4) for c in cols])
    return [blue, frost]


def boil_off():
    white = Mark("#FFFFFF")
    gray = Mark("#C8C8C8")
    rod_rows(white, (1, 4, 7))
    rod_rows(gray, (2,))
    return [white, gray]


def hot_core():
    # A dark core that glows: the head, except the face, is a dark charcoal,
    # with glowing orange cracks.
    dark = Mark("#2A1A14")
    glow = Mark("#FF8A20")
    for x, y, f, col, row in pixels(HEAD, SHELL):
        (glow if (col + 2 * row) % 7 == 0 or (col == 4 and row % 2) else dark).put((x, y))
    return [dark, glow]


def cinder_heart():
    # A bright mark on the chest: a bright, white and orange heart on the
    # bottom of the head, which is the chest of the blaze, and its glow goes
    # round the low edge of the head on all four sides.
    white = Mark("#FFFFF0")
    orange = Mark("#FF6010")
    heart = {(2, 2), (5, 2), (1, 3), (2, 3), (3, 3), (4, 3), (5, 3), (6, 3), (2, 4), (3, 4), (4, 4), (5, 4),
             (3, 5), (4, 5), (1, 2), (6, 2)}
    for x, y, f, col, row in pixels(HEAD, ("bottom",)):
        if (col, row) in heart:
            (white if 2 <= col <= 5 and 3 <= row <= 4 else orange).put((x, y))
        else:
            orange.put((x, y))
    for x, y, f, col, row in pixels(HEAD, SIDES, rows=(6, 7)):
        (white if row == 7 and 2 <= col <= 5 else orange).put((x, y))
    return [white, orange]


def steam_burst():
    # Rods with open vents: a dark, open vent slit runs down the middle of
    # each side of the rods, and white steam comes out at its top.
    vent = Mark("#1A0804")
    steam = Mark("#FFFFFF")
    for x, y, f, col, row in pixels(ROD, SIDES):
        if col == 1 and 2 <= row <= 6:
            vent.put((x, y))
        elif row in (0, 1) and col == 1:
            steam.put((x, y))
    put(steam, ROD, ("top",))
    return [vent, steam]


GENES = {
    "white_heat": white_heat, "blast_ember": blast_ember, "shield_scorch": shield_scorch,
    "golem_melt": golem_melt, "sear_touch": sear_touch, "swift_shot": swift_shot,
    "long_burst": long_burst, "quick_burst": quick_burst, "fan_shot": fan_shot, "seeker_spark": seeker_spark,
    "spark_ring": spark_ring, "soot_cloud": soot_cloud,
    "deep_burn": deep_burn, "fire_trail": fire_trail, "burn_pain": burn_pain, "fire_feed": fire_feed,
    "rod_wall": rod_wall, "heat_haze": heat_haze,
    "rising_heat": rising_heat, "air_dodge": air_dodge, "quick_wings": quick_wings, "smoke_dash": smoke_dash,
    "sky_guard": sky_guard, "diving_flame": diving_flame,
    "steam_shell": steam_shell, "snow_shrug": snow_shrug, "boil_off": boil_off, "hot_core": hot_core,
    "cinder_heart": cinder_heart, "steam_burst": steam_burst,
}

MODEL_GENES = {
    "long_burst": (long_burst_model, "long_burst_rods"),
    "fan_shot": (fan_shot_model, "fan_shot_fins"),
    "rod_wall": (rod_wall_model, "rod_wall_collars"),
    "quick_wings": (quick_wings_model, "quick_wings_rods"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="blaze genes")
