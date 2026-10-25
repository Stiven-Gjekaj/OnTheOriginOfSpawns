"""Make the gene visuals of the ghast, from design/species/ghast.md.

Usage: python3 tools/make_ghast_marks.py (from the root of the repository)

The ghast has two textures, ghast.png and ghast_shooting.png, 128x64 each,
with 2x2 pixels for each uv unit. They use the ghast region map, and a mark
goes on both of them, so each face mark covers the eyes and the mouth of both
faces. The parts:
- the body: each face is 32x32. The front (32, 32) is the face. On the face,
  the eyes are in columns 6 to 13 and 18 to 25, on rows 10 and 11 when they
  are closed and rows 8 to 11 when they shoot. The mouth is in columns 12 to
  19, on rows 24 and 25 when it is closed and rows 20 to 29 when it shoots.
- the tentacles: all nine use the same pixels. Each side is 4 wide, from
  row 4 down. A tentacle of length L ends on row 4 + 2L - 1, from row 19 (L
  8) to row 29 (L 13), and the bottom (8, 0) is the end of each tentacle.
The gene models use the bones body and tentacle0 to tentacle8.

Pixel plan for the Bombers.
- Great Blast has a wide, black mouth, wider than both mouths.
- Fire Rain has orange, burnt tentacle tips.
- Blast Shell has gray soot on the body: dark streaks of soot.
- Shield Burst has red cracks round the mouth.
- Golem Bomb has uneven iron gray patches with rough edges, some with
  rivets.
- Twin Shot: two small, red mouths, the gene model twin_shot_mouths.

Pixel plan for the Barragers.
- Quick Fire has narrow, red eyes: white lids over the high half of the eyes.
- Far Sight has large, white eyes with a wide, low, dark pupil and a pale
  lid.
- Spread Fire has three red rings on the tentacles.
- Hard Fireball has hard, dark plates of uneven sizes and shapes on the
  body, as a crust.
- Steady Aim has a red mark between the eyes.
- Ember Spit has red tear streaks.

Pixel plan for the Weepers.
- Healing Tears has blue tear streaks.
- Soul Tears has pale blue eyes with a soft glow.
- Tear Rain: drops at the tentacle tips, the gene model tear_rain_drops.
- Sad Shell has a thick, gray shell: an even gray with plate seams.
- Kin Grief has sad, drooping eyes: brows that slant down.
- Grief Cry has a wide, crying mouth that turns down at its corners.

Pixel plan for the Driftwings.
- Swift Drift: long, thin tentacles, the gene model swift_drift_tentacles.
- Dodge Drift has light, thin skin: a pale blue white with faint veins.
- High Float has short, curled tentacles: spiral stripes and dark ends.
- Haze Skin has gray, smoky patches: soft, light gray clouds.
- Return Ward has a pale blue ward rune on the back and the sides.
- Ghost Glide has closed, quiet eyes: curved, blue gray lids.

Pixel plan for the Moaners.
- Dread Moan has a long, open moan: a tall, black mouth.
- Weak Wail has gray, sunken eyes: round, gray sockets round the eyes.
- Echo Call has ripple lines on the tentacles.
- Dark Cry has black, empty eyes: round, black holes.
- Shrill Cry has a small, round mouth.
- Blast Cry has small, red rings of different sizes on the body.
This script needs Pillow.
"""
import math

from make_gene_marks import Mark as TextureMark
from make_gene_marks import noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import model_image, write_all

REGION_MAP = "originofspawns:textures/region/ghast.png"

BODY = {"top": (32, 0, 32, 32), "bottom": (64, 0, 32, 32), "right": (0, 32, 32, 32), "front": (32, 32, 32, 32),
        "left": (64, 32, 32, 32), "back": (96, 32, 32, 32)}
TENTACLE = {"top": (4, 0, 4, 4), "bottom": (8, 0, 4, 4), "right": (0, 4, 4, 26), "front": (4, 4, 4, 26),
            "left": (8, 4, 4, 26), "back": (12, 4, 4, 26)}
SHELL = ("top", "bottom", "right", "left", "back")
SIDES = ("right", "front", "left", "back")
EYE_COLS = (range(6, 14), range(18, 26))
LENGTHS = (8, 13, 9, 11, 11, 10, 12, 9, 12)


def Mark(tint):
    """Return a mark for the 128x64 texture of the ghast."""
    return TextureMark(tint, size=128, height=64)


def pixels(part, faces, cols=None, rows=None):
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                if (cols is None or col in cols) and (rows is None or row in rows):
                    yield x0 + col, y0 + row, face, col, row


def face(col, row):
    """Return a pixel of the face, the front of the body."""
    return 32 + col, 32 + row


def put_face(mark, cells):
    mark.put([face(c, r) for c, r in cells if 0 <= c < 32 and 0 <= r < 32])


def tip_rows(start=16):
    """Return the tentacle side rows from start to the end of the longest tentacle."""
    return range(start, 30)


# The Bombers.

def great_blast():
    # A wide, black mouth: a wide mouth with round corners, wider than the
    # mouth of both faces, black with a dark red inside.
    black = Mark("#0A0606")
    inner = Mark("#4A0A0A")
    for c in range(7, 25):
        for r in range(19, 31):
            corner = (c in (7, 24)) and r in (19, 30)
            if not corner:
                (inner if 10 <= c <= 21 and 23 <= r <= 27 else black).put(face(c, r))
    return [black, inner]


def fire_rain():
    # Orange, burnt tips: the low part of the tentacles goes from orange to
    # a black, burnt end.
    orange = Mark("#FF7A20")
    hot = Mark("#FFC040")
    burnt = Mark("#2A1A10")
    for x, y, f, col, row in pixels(TENTACLE, SIDES, rows=tip_rows(14)):
        (hot if row < 18 else orange if row < 24 else burnt).put((x, y))
    for x, y, f, col, row in pixels(TENTACLE, ("bottom",)):
        burnt.put((x, y))
    return [orange, hot, burnt]


def blast_shell():
    # Gray soot on the body: dark gray streaks of soot that run down the
    # body, thicker on the low half.
    soot = Mark("#4A4648")
    dark = Mark("#2A2828")
    for x, y, f, col, row in pixels(BODY, SHELL):
        streak = noise100(col // 3, 0, 1101 + len(f)) < 45
        if streak and noise100(x, y // 3, 1102) < 30 + (row if f != "top" else 0):
            (dark if noise100(x, y, 1103) < 30 else soot).put((x, y))
    return [soot, dark]


def shield_burst():
    # Red cracks round the mouth: thick, red cracks that run out from the
    # mouth in seven directions, with dark ends.
    red = Mark("#D01818")
    dark = Mark("#6A0808")
    cx, cy = 15.5, 24.5
    for angle in (200, 240, 290, 340, 20, 110, 160):
        a = math.radians(angle)
        for n in range(5, 18):
            c = int(cx + math.cos(a) * n + (n % 3 - 1) * 0.6)
            r = int(cy + math.sin(a) * n * 0.8)
            for dc in (0, 1):
                cc = c + dc
                if 0 <= cc < 32 and 0 <= r < 32 and not (12 <= cc <= 19 and 20 <= r <= 29):
                    (dark if n > 14 else red).put(face(cc, r))
    return [red, dark]


def golem_bomb():
    # Iron gray patches: uneven patches of iron with rough edges on the body,
    # each a round blob of its own size, with a dark rough rim. Some of them
    # have pale rivets.
    iron = Mark("#9AA0A8")
    edge = Mark("#4A4E54")
    rivet = Mark("#E0E4E8")
    for f in SHELL:
        for n in range(5):
            cx = 3 + noise100(n, len(f), 1201) % 26
            cy = 3 + noise100(n, len(f), 1202) % 26
            rx = 3 + noise100(n, len(f), 1203) % 4
            ry = 2 + noise100(n, len(f), 1204) % 4
            studded = noise100(n, len(f), 1205) < 50
            for x, y, ff, col, row in pixels(BODY, (f,)):
                d = ((col - cx) / rx) ** 2 + ((row - cy) / ry) ** 2 + noise100(x, y, 1206) / 400
                if d < 0.75:
                    is_rivet = studded and (col - cx) % 3 == 0 and (row - cy) % 3 == 0 and d < 0.45
                    (rivet if is_rivet else iron).put((x, y))
                elif d < 1.05:
                    edge.put((x, y))
    return [iron, edge, rivet]


def twin_shot():
    # The mouths are the gene model twin_shot_mouths.
    return []


def twin_shot_model():
    # Two small, red mouths: two small, round mouths on the face, under the
    # eyes, each a red rim round a black hole. On the bone body, the face is
    # at z -8, from x -8 to 8 and y -8 to 8.
    im = model_image()

    def mouth(f, x, y):
        if f != "front":
            return "#A01010"
        return "#0A0404" if 0 < x < 2 and 0 < y < 2 else "#E02020"
    paint_box(im, 0, 0, 3, 3, 1, mouth)
    parts = [{"bone": "body", "uv": [0, 0], "from": [x, 2, -8.6], "size": [3, 3, 1], "inflate": -0.1}
             for x in (-6, 3)]
    save_model("twin_shot_mouths", im, parts)


# The Barragers.

def quick_fire():
    # Narrow, red eyes: white lids cover the high half of the eyes of both
    # faces. The low half is a long, red slit that goes past the eye at both
    # ends, under a dark, angry brow that slants down to the middle.
    lid = Mark("#F2F0F0")
    red = Mark("#E01010")
    brow = Mark("#3A2A2E")
    for cols, inner in ((EYE_COLS[0], 1), (EYE_COLS[1], -1)):
        put_face(lid, [(c, r) for c in cols for r in (8, 9)])
        put_face(red, [(c, r) for c in range(cols[0] - 2, cols[-1] + 3) for r in (10, 11)])
        for n, c in enumerate(range(cols[0] - 1, cols[-1] + 2)):
            k = n if inner == 1 else 9 - n
            put_face(brow, [(c, 5 + k // 3), (c, 6 + k // 3)])
    return [lid, red, brow]


def far_sight():
    # Large, white eyes: each eye is a large, white oval, wider than the eyes
    # of both faces, with a wide, dark pupil low in it, a pale gray lid over
    # its top, and a soft, pale rim. No outline goes round it.
    white = Mark("#FFFFFF")
    pupil = Mark("#141010")
    lid = Mark("#B8B4B8")
    for cx in (9.5, 21.5):
        for c in range(int(cx) - 6, int(cx) + 8):
            for r in range(4, 18):
                d = ((c - cx) / 6) ** 2 + ((r - 10.5) / 5.5) ** 2
                if d >= 1.0:
                    continue
                if r <= 6:
                    lid.put(face(c, r))
                elif abs(c - cx) < 2.5 and 10 <= r <= 13:
                    pupil.put(face(c, r))
                else:
                    white.put(face(c, r))
    return [white, pupil, lid]


def spread_fire():
    red = Mark("#D01818")
    for x, y, f, col, row in pixels(TENTACLE, SIDES, rows=(6, 7, 11, 12, 16, 17)):
        red.put((x, y))
    return [red]


def hard_fireball():
    # Dark, hard spots: hard, dark plates of uneven sizes, as a crust. Each
    # plate is a solid blob of its own size and shape, with a darker low
    # edge.
    plate = Mark("#3A3434")
    dark = Mark("#1A1616")
    for f in SHELL:
        for n in range(9):
            cx = 2 + noise100(n, len(f), 1301) % 28
            cy = 2 + noise100(n, len(f), 1302) % 28
            rx = 2 + noise100(n, len(f), 1303) % 4
            ry = 1.5 + noise100(n, len(f), 1304) % 3
            for x, y, ff, col, row in pixels(BODY, (f,)):
                d = abs(col - cx) / rx + abs(row - cy) / ry + noise100(x, y, 1305) / 500
                if d < 1.0:
                    (dark if row > cy + ry / 2 else plate).put((x, y))
    return [plate, dark]


def steady_aim():
    # A red mark between the eyes: a large, red diamond between the eyes,
    # with a dark middle, and a red line down the brow above it.
    red = Mark("#E01818")
    dark = Mark("#6A0808")
    for r in range(1, 20):
        half = max(0, 4 - abs(r - 11) // 2) if r >= 4 else 1
        for c in range(16 - half, 16 + half):
            (dark if half >= 3 and abs(c - 15.5) < half - 1.5 else red).put(face(c, r))
    return [red, dark]


def tear_streaks(color, dark_color):
    tear = Mark(color)
    dark = Mark(dark_color)
    for cols in ((8, 9), (22, 23)):
        for r in range(12, 26):
            put_face(tear, [(c, r) for c in cols])
            if r % 4 == 1:
                put_face(dark, [(cols[0] - 1, r), (cols[1] + 1, r)])
        put_face(dark, [(cols[0], 26), (cols[1], 26)])
    return [tear, dark]


def ember_spit():
    # Red tear streaks: red streaks run down the face from both eyes, with
    # orange drops at their ends.
    return tear_streaks("#E02010", "#FF8A20")


# The Weepers.

def healing_tears():
    # Blue tear streaks: blue streaks run down the face from both eyes, with
    # pale blue drops at their ends.
    return tear_streaks("#2A6ADA", "#9AC8FF")


def soul_tears():
    # Pale blue eyes: the eyes of both faces are a pale blue, with a soft
    # blue glow round them.
    pale = Mark("#A8E0FF")
    glow = Mark("#D0ECFA")
    for cols in EYE_COLS:
        put_face(pale, [(c, r) for c in cols for r in range(8, 12)])
        put_face(glow, [(c, r) for c in range(cols[0] - 1, cols[-1] + 2) for r in (6, 7, 12, 13)])
        put_face(glow, [(cols[0] - 1, r) for r in range(8, 12)] + [(cols[-1] + 1, r) for r in range(8, 12)])
    return [pale, glow]


def tear_rain():
    # The drops are the gene model tear_rain_drops.
    return []


def tear_rain_model():
    # Drops at the tips: a blue drop hangs under the end of each tentacle.
    # On each tentacle bone, the tentacle is from y 0 to its length L.
    im = model_image()
    paint_box(im, 0, 0, 1, 2, 1, lambda f, x, y: "#C8E8FF" if y == 0 else "#3A8AE8")
    parts = [{"bone": f"tentacle{i}", "uv": [0, 0], "from": [-0.5, LENGTHS[i], -0.5], "size": [1, 2, 1],
              "inflate": -0.1} for i in range(9)]
    save_model("tear_rain_drops", im, parts)


def sad_shell():
    # A thick, gray shell: the body, except the face, is an even, dull gray,
    # with a few uneven, dark cracks that wander across it.
    gray = Mark("#A4A2A8")
    crack = Mark("#5A5860")
    cracks = set()
    for f in SHELL:
        for n in range(3):
            col = 4 + noise100(n, len(f), 1601) % 24
            for row in range(32):
                cracks.add((f, col, row))
                col = max(0, min(31, col + (noise100(row, n + len(f), 1602) % 3 - 1)))
    for x, y, f, col, row in pixels(BODY, SHELL):
        (crack if (f, col, row) in cracks else gray).put((x, y))
    return [gray, crack]


def kin_grief():
    # Sad, drooping eyes: thick, dark brows slant down to the outer corners,
    # the outer corners of the eyes droop down, and dark, tired bags sit
    # under the eyes.
    brow = Mark("#3A3440")
    droop = Mark("#6A6470")
    bag = Mark("#B8B0BC")
    for cols, outer in ((EYE_COLS[0], -1), (EYE_COLS[1], 1)):
        for n, c in enumerate(range(cols[0] - 1, cols[-1] + 2)):
            k = n if outer == 1 else 9 - n
            put_face(brow, [(c, 3 + k // 2), (c, 4 + k // 2), (c, 5 + k // 2)])
        end = cols[-1] if outer == 1 else cols[0]
        for n in range(4):
            put_face(droop, [(end + outer * n, 12 + n), (end + outer * n, 13 + n)])
        put_face(bag, [(c, r) for c in cols for r in (13, 14)])
    return [brow, droop, bag]


def grief_cry():
    # A wide, crying mouth: a wide, dark blue gray mouth with corners that
    # turn down, and a pale line of teeth on its top.
    dark = Mark("#1E2430")
    teeth = Mark("#E8E8F0")
    for c in range(9, 23):
        top = 21 if 11 <= c <= 20 else 22
        for r in range(top, 28):
            dark.put(face(c, r))
    put_face(dark, [(c, r) for c in (7, 8, 23, 24) for r in (26, 27, 28, 29)])
    put_face(teeth, [(c, 21) for c in range(12, 20, 2)])
    return [dark, teeth]


# The Driftwings.

def swift_drift():
    # The tentacles are the gene model swift_drift_tentacles.
    return []


def swift_drift_model():
    # Long, thin tentacles: a long, thin end grows from the end of each
    # tentacle. On each tentacle bone, the tentacle is from y 0 to its
    # length L, and from x and z -1 to 1.
    im = model_image()
    paint_box(im, 0, 0, 1, 6, 1, lambda f, x, y: "#C8A0B4" if y < 4 else "#8A5A74")
    parts = [{"bone": f"tentacle{i}", "uv": [0, 0], "from": [-0.5, LENGTHS[i], -0.5], "size": [1, 6, 1],
              "inflate": 0.0} for i in range(9)]
    save_model("swift_drift_tentacles", im, parts)


def dodge_drift():
    # Light, thin skin: the body is a pale blue white, with faint, blue veins
    # through it.
    pale = Mark("#E4ECF8")
    vein = Mark("#A8C0E0")
    for x, y, f, col, row in pixels(BODY, SHELL):
        wave = int(4 * math.sin((row + 3 * len(f)) / 3.0))
        (vein if (col + wave) % 9 == 0 else pale).put((x, y))
    return [pale, vein]


def high_float():
    # Short, curled tentacles: pink and gray stripes curl round the
    # tentacles, and the low part of each tentacle is dark, so the tentacles
    # look short.
    curl = Mark("#C88AA0")
    gray = Mark("#8A8090")
    dark = Mark("#4A4050")
    for x, y, f, col, row in pixels(TENTACLE, SIDES):
        ring = (row + col + 4 * SIDES.index(f)) % 8
        if row >= 18:
            dark.put((x, y))
        elif ring in (0, 1):
            curl.put((x, y))
        elif ring == 4:
            gray.put((x, y))
    for x, y, f, col, row in pixels(TENTACLE, ("bottom",)):
        dark.put((x, y))
    return [curl, gray, dark]


def haze_skin():
    # Gray, smoky patches: soft, light gray clouds on the body, with a paler
    # edge.
    smoke = Mark("#B8B4B8")
    soft = Mark("#D4D0D4")
    for x, y, f, col, row in pixels(BODY, SHELL):
        n = noise100(x // 4, y // 4, 1401)
        if n < 30:
            smoke.put((x, y))
        elif n < 45 and noise100(x, y, 1402) < 60:
            soft.put((x, y))
    return [smoke, soft]


def return_ward():
    # A pale blue ward rune: a large ring with a cross in it, and a dot at
    # each end of the cross, on the back and on both sides.
    rune = Mark("#7AB8F0")
    pale = Mark("#C8E4FF")
    for f in ("back", "right", "left"):
        for x, y, ff, col, row in pixels(BODY, (f,)):
            d = math.hypot(col - 15.5, row - 15.5)
            if 10 <= d < 12 or (abs(col - 15.5) < 1 and d < 10) or (abs(row - 15.5) < 1 and d < 10):
                rune.put((x, y))
            elif 12 <= d < 13:
                pale.put((x, y))
    return [rune, pale]


def ghost_glide():
    # Closed, quiet eyes: the eyes of both faces are closed under a white
    # lid, with a thick, curved, blue gray line of a closed eye, short lashes
    # under it, and a soft blue shade under the eye.
    lid = Mark("#F2F0F0")
    line = Mark("#5A6A8A")
    shade = Mark("#C8D8EC")
    for cols in EYE_COLS:
        put_face(lid, [(c, r) for c in cols for r in range(7, 12)])
        for n, c in enumerate(range(cols[0] - 1, cols[-1] + 2)):
            r = 9 if n in (0, 9) else 10 if n in (1, 8) else 11
            put_face(line, [(c, r), (c, r + 1)])
            if n % 2 == 1 and 1 < n < 8:
                put_face(line, [(c, 13)])
        put_face(shade, [(c, r) for c in cols for r in (14, 15)])
    return [lid, line, shade]


# The Moaners.

def dread_moan():
    # A long, open moan: a tall, black oval mouth from the eyes nearly to the
    # bottom of the face.
    black = Mark("#0A0606")
    edge = Mark("#3A3034")
    for c in range(11, 21):
        for r in range(15, 32):
            d = ((c - 15.5) / 4.5) ** 2 + ((r - 23.5) / 8.5) ** 2
            if d < 0.8:
                black.put(face(c, r))
            elif d < 1.0:
                edge.put(face(c, r))
    return [black, edge]


def weak_wail():
    # Gray, sunken eyes: a round, gray socket round each eye, darker in its
    # middle, with a soft gray shade under it.
    gray = Mark("#8A8890")
    dark = Mark("#4A4850")
    shade = Mark("#C0BEC4")
    for cols in EYE_COLS:
        cx = (cols[0] + cols[-1]) / 2
        for c in range(cols[0] - 3, cols[-1] + 4):
            for r in range(4, 18):
                d = ((c - cx) / 6) ** 2 + ((r - 10) / 4.5) ** 2
                if d < 0.35:
                    dark.put(face(c, r))
                elif d < 0.8:
                    gray.put(face(c, r))
                elif d < 1.0 and r > 10:
                    shade.put(face(c, r))
    return [gray, dark, shade]


def echo_call():
    # Ripple lines: wavy, blue gray rings round the tentacles, every four
    # rows.
    line = Mark("#7A90B8")
    for x, y, f, col, row in pixels(TENTACLE, SIDES):
        wave = 1 if (col + SIDES.index(f)) % 4 in (1, 2) else 0
        if (row + wave) % 4 == 0:
            line.put((x, y))
    return [line]


def dark_cry():
    # Black, empty eyes: each eye is a round, black hole, larger than the
    # eye of both faces, with a soft, dark gray edge.
    black = Mark("#050505")
    edge = Mark("#5A5458")
    for cols in EYE_COLS:
        cx = (cols[0] + cols[-1]) / 2
        for c in range(cols[0] - 2, cols[-1] + 3):
            for r in range(5, 16):
                d = ((c - cx) / 5) ** 2 + ((r - 10) / 4) ** 2
                if d < 0.75:
                    black.put(face(c, r))
                elif d < 1.0:
                    edge.put(face(c, r))
    return [black, edge]


def shrill_cry():
    # A small, round mouth: a small, dark ring of a mouth with a red inside,
    # pursed lips round it, and white skin round them covers the large mouth
    # of the shooting face. Thin strain lines go out from the lips.
    skin = Mark("#F2F0F0")
    ring = Mark("#1A1418")
    inner = Mark("#7A2A2A")
    lip = Mark("#B89098")
    for c in range(10, 22):
        for r in range(18, 31):
            d = math.hypot(c - 15.5, (r - 24.5) * 0.9)
            if d < 2.0:
                inner.put(face(c, r))
            elif d < 3.4:
                ring.put(face(c, r))
            elif d < 4.6:
                lip.put(face(c, r))
            elif 12 <= c <= 19 and 20 <= r <= 29:
                skin.put(face(c, r))
    for n in range(4):
        put_face(lip, [(9 - n, 22 - n), (22 + n, 22 - n), (9 - n, 27 + n), (22 + n, 27 + n)])
    return [skin, ring, inner, lip]


def blast_cry():
    # Red rings on the body: small, red rings of different sizes, spread over
    # the back, the sides, and the top, as the marks of blasts.
    red = Mark("#D02020")
    dark = Mark("#7A1010")
    for f in ("back", "right", "left", "top"):
        for n in range(4):
            cx = 5 + noise100(n, len(f), 1501) % 22
            cy = 5 + noise100(n, len(f), 1502) % 22
            size = 2.5 + noise100(n, len(f), 1503) % 3
            for x, y, ff, col, row in pixels(BODY, (f,)):
                d = math.hypot(col - cx, row - cy)
                if size - 1 <= d < size:
                    red.put((x, y))
                elif size <= d < size + 0.8:
                    dark.put((x, y))
    return [red, dark]


GENES = {
    "great_blast": great_blast, "fire_rain": fire_rain, "blast_shell": blast_shell, "shield_burst": shield_burst,
    "golem_bomb": golem_bomb, "twin_shot": twin_shot,
    "quick_fire": quick_fire, "far_sight": far_sight, "spread_fire": spread_fire, "hard_fireball": hard_fireball,
    "steady_aim": steady_aim, "ember_spit": ember_spit,
    "healing_tears": healing_tears, "soul_tears": soul_tears, "tear_rain": tear_rain, "sad_shell": sad_shell,
    "kin_grief": kin_grief, "grief_cry": grief_cry,
    "swift_drift": swift_drift, "dodge_drift": dodge_drift, "high_float": high_float, "haze_skin": haze_skin,
    "return_ward": return_ward, "ghost_glide": ghost_glide,
    "dread_moan": dread_moan, "weak_wail": weak_wail, "echo_call": echo_call, "dark_cry": dark_cry,
    "shrill_cry": shrill_cry, "blast_cry": blast_cry,
}

MODEL_GENES = {
    "twin_shot": (twin_shot_model, "twin_shot_mouths"),
    "tear_rain": (tear_rain_model, "tear_rain_drops"),
    "swift_drift": (swift_drift_model, "swift_drift_tentacles"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="ghast genes")
