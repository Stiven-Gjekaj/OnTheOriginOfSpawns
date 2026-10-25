"""Make the gene visuals of the piglin brute, from design/species/piglin_brute.md.

Usage: python3 tools/make_piglin_brute_marks.py (from the root of the repository)

The piglin brute uses the piglin region map. make_zombified_piglin_marks.py
gives the parts of the layout. The brute also has an outer layer: a black
and gold belt on the jacket (16, 32), pants (0, 32) and (0, 48), and a left
sleeve (48, 48). Where the outer layer has pixels, it covers the body under
it, so a mark on the body, the legs, or the left arm goes on both layers.
The brute holds its gold axe in the right hand.
The gene models use the bones head and body.

Pixel plan for the Axemen.
- Great Swing has huge, scarred forearms: bright muscles with pale scars.
- Head Splitter: a bone helm crest, the gene model head_splitter_crest.
- Wide Arc has wide shoulders with gold bands.
- Hurl Axe: a row of small axes on the belt, the gene model hurl_axe_axes.
- Bone Breaker has bone rings on the wrists.
- Golden Edge has a gold edge down the outer side of the axe arm.

Pixel plan for the Colossi.
- Iron Hide has dark iron plates on the body, with rivets.
- Blast Wall has a thick, scorched belt, black with embers.
- Arrow Wall: arrows stuck in the back, the gene model arrow_wall_arrows.
- Steady Feet has heavy, gold greaves on the low legs.
- Thick Neck has a thick, wide neck: folds of the neck at the low head.
- Rage Plate has red marks on the plates: red slashes on the body.

Pixel plan for the Maulers.
- Blood Rage has red war paint across the eyes and down the face.
- Charge has wide, red stripes round the legs.
- Battle Scars has many old scars on the body and the arms.
- Roar has a wide, roaring mouth: an open snout and dark mouth corners.
- Frenzy Axe has red veins on the arms.
- Hot Head has a red, hot snout.

Pixel plan for the Gatekeepers.
- Home Guard: a bastion banner cloth on the back, the gene model
  home_guard_banner.
- Alarm Horn: a gold horn on a cord, the gene model alarm_horn_horn.
- Gold Guard has gold rings on the snout.
- Watch Post has a narrow, watching eye: a dark band across the eyes, with
  one bright slit.
- Lava Moat has burnt, black boots with glowing cracks.
- Golem Wrecker has iron studs on the knuckles.

Pixel plan for the Guardbreakers.
- Guard Crusher has a heavy, gold fist on the axe arm.
- Armor Splitter has cracked, gold bracers on the forearms.
- Sunder Strike has a cruel grin.
- Rust Axe has rust on the hands.
- Shatter Blow has shards of armor on the belt.
- Brute Force has thick, short fingers.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import (ALL, ARMS, BODY, EYES, GOLD, HEAD, LEFT_ARM, LEFT_LEG, RIGHT_ARM,
                                         RIGHT_LEG, SIDES, SNOUT, TUSKS, at, box_faces, face, model_image, pixels,
                                         put, speckle, write_all)

JACKET = box_faces(16, 32, 8, 12, 4)
RIGHT_PANTS = box_faces(0, 32, 4, 12, 4)
LEFT_PANTS = box_faces(0, 48, 4, 12, 4)
LEFT_SLEEVE = box_faces(48, 48, 4, 12, 4)
RIGHT_SLEEVE = box_faces(40, 32, 4, 12, 4)
OUTER = {id(BODY): JACKET, id(RIGHT_LEG): RIGHT_PANTS, id(LEFT_LEG): LEFT_PANTS, id(LEFT_ARM): LEFT_SLEEVE,
         id(RIGHT_ARM): RIGHT_SLEEVE}
LEGS = (RIGHT_LEG, LEFT_LEG)


def both(part, faces=ALL, cols=None, rows=None):
    """Yield the pixels of a part and of the outer layer over it."""
    yield from pixels(part, faces, cols, rows)
    yield from pixels(OUTER[id(part)], faces, cols, rows)


def put2(mark, part, faces=ALL, cols=None, rows=None):
    mark.put([(x, y) for x, y, *_ in both(part, faces, cols, rows)])
    return mark


# The Axemen.

def great_swing():
    # Huge, scarred forearms: rows 4 to 9 of the arms are bright, as big
    # muscles, with pale scars that slant across them.
    muscle = Mark("#F8B890")
    scar = Mark("#FFF0E0")
    for arm in ARMS:
        for x, y, f, col, row in both(arm, SIDES, rows=range(4, 10)):
            ((scar if (col + row) % 4 == 0 else muscle)).put((x, y))
    return [muscle, scar]


def head_splitter():
    # The crest is the gene model head_splitter_crest.
    return []


def head_splitter_model():
    # A bone helm crest: a ridge of bone from the front to the back of the
    # top of the head, with three spikes on it. On the bone head, the top of
    # the head is at y -8.
    im = model_image(64, 32)
    paint_box(im, 0, 0, 2, 2, 9, lambda f, x, y: "#F0E8D0" if f == "top" else "#C8BCA0")
    paint_box(im, 0, 16, 1, 2, 1, lambda f, x, y: "#FFFFF0" if y == 0 else "#D8CCB0")
    parts = [{"bone": "head", "uv": [0, 0], "from": [-1, -10, -4.5], "size": [2, 2, 9], "inflate": 0.0}]
    for z in (-3.5, -0.5, 2.5):
        parts.append({"bone": "head", "uv": [0, 16], "from": [-0.5, -12, z], "size": [1, 2, 1], "inflate": 0.0})
    save_model("head_splitter_crest", im, parts)


def wide_arc():
    # Wide shoulders with gold bands: bright shoulders and a gold band on
    # rows 2 and 3 of each arm.
    bright = Mark("#F8C098")
    gold = Mark(GOLD[0])
    for arm in ARMS:
        put2(bright, arm, ("top",))
        put2(bright, arm, SIDES, rows=(0, 1))
        put2(gold, arm, SIDES, rows=(2, 3))
    return [bright, gold]


def hurl_axe():
    # The axes are the gene model hurl_axe_axes.
    return []


def hurl_axe_model():
    # A row of small axes on the belt: three small axes that hang from the
    # belt, on the front and the sides, each with a wood handle and a gold
    # head. On the bone body, the belt is at y 8 to 11, and the body is from
    # x -4 to 4 and z -2 to 2.
    im = model_image()
    paint_box(im, 0, 0, 1, 5, 1, lambda f, x, y: "#8A6A3A")
    paint_box(im, 0, 8, 1, 3, 3, lambda f, x, y: GOLD[2] if y == 0 else GOLD[0])
    parts = []
    for x, z in ((-3, -2.6), (2, -2.6), (4.1, 0)):
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, 8, z], "size": [1, 5, 1], "inflate": -0.05})
        parts.append({"bone": "body", "uv": [0, 8], "from": [x, 7.5, z - 2 if z < 0 else z - 1], "size": [1, 3, 3],
                      "inflate": 0.0})
    save_model("hurl_axe_axes", im, parts)


def bone_breaker():
    bone = Mark("#F0E8D0")
    groove = Mark("#8A7A60")
    for arm in ARMS:
        put2(bone, arm, SIDES, rows=(7, 9))
        for x, y, f, col, row in both(arm, SIDES, rows=(8,)):
            (groove if col % 2 else bone).put((x, y))
    return [bone, groove]


def golden_edge():
    # A gold edge on the arm: a gold line down the outer side of the axe
    # arm, and along the edges of its front and back.
    gold = Mark(GOLD[0])
    shine = Mark(GOLD[2])
    put(gold, RIGHT_ARM, ("right",))
    put(shine, RIGHT_ARM, ("front", "back"), cols=(0,))
    put(gold, RIGHT_ARM, ("front",), cols=(1,), rows=range(0, 12, 3))
    return [gold, shine]


# The Colossi.

def iron_hide():
    # Dark iron plates on the body: plates of 4x3 with dark edges and pale
    # rivets, on all the sides of the body.
    plate = Mark("#4A4C52")
    edge = Mark("#2A2C30")
    rivet = Mark("#B0B4BA")
    for x, y, f, col, row in both(BODY, SIDES):
        if row % 3 == 2 or col % 4 == 3:
            edge.put((x, y))
        elif row % 3 == 0 and col % 4 == 0:
            rivet.put((x, y))
        else:
            plate.put((x, y))
    return [plate, edge, rivet]


def blast_wall():
    # A thick, scorched belt: rows 7 to 11 of the body are a black, burnt
    # belt with orange embers.
    black = Mark("#141008")
    ember = Mark("#E06018")
    for x, y, f, col, row in both(BODY, SIDES, rows=range(7, 12)):
        (ember if noise100(x, y, 3101) < 18 else black).put((x, y))
    return [black, ember]


def arrow_wall():
    # The arrows are the gene model arrow_wall_arrows.
    return []


def arrow_wall_model():
    # Arrows stuck in the back: seven arrows with white feathers that stick
    # out of the back of the body, at small angles. On the bone body, the
    # back is at z 2.
    im = model_image()
    paint_box(im, 0, 0, 1, 1, 5, lambda f, x, y: "#7A5A34")
    paint_box(im, 0, 8, 2, 2, 1, lambda f, x, y: "#F4F4F4" if (x + y) % 2 == 0 else "#C8C8C8")
    parts = []
    for x, y, rx, ry in ((-2.5, 1.5, -15, 10), (1.5, 2.5, 10, -15), (-0.5, 5, -5, 20), (2.5, 7.5, 15, 5),
                         (-3, 8.5, -10, -10), (0.5, 10, 5, -20), (-1.5, 3, 20, 0)):
        pivot = [x + 0.5, y + 0.5, 2]
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, y, 1.5], "size": [1, 1, 5], "inflate": -0.2,
                      "pivot": pivot, "rotation": [rx, ry, 0]})
        parts.append({"bone": "body", "uv": [0, 8], "from": [x - 0.5, y - 0.5, 5.8], "size": [2, 2, 1],
                      "inflate": -0.2, "pivot": pivot, "rotation": [rx, ry, 0]})
    save_model("arrow_wall_arrows", im, parts)


def steady_feet():
    # Heavy, gold greaves: rows 6 to 11 of the legs are gold, with a dark rim
    # on row 6 and a bright line on row 9.
    gold = Mark(GOLD[0])
    dark = Mark(GOLD[1])
    shine = Mark(GOLD[2])
    for leg in LEGS:
        put2(dark, leg, SIDES, rows=(6,))
        put2(gold, leg, SIDES, rows=(7, 8, 10, 11))
        put2(shine, leg, SIDES, rows=(9,))
    return [gold, dark, shine]


def thick_neck():
    # A thick, wide neck: the low rows of the sides and the back of the head
    # are a darker skin with folds, and the top of the body round the neck
    # is thick too.
    neck = Mark("#B86A4A")
    fold = Mark("#7A3A24")
    for x, y, f, col, row in pixels(HEAD, ("right", "left", "back"), rows=(5, 6, 7)):
        (fold if row == 6 else neck).put((x, y))
    put(neck, HEAD, ("bottom",))
    put(neck, BODY, ("top",))
    put(fold, BODY, ("front", "back"), cols=(2, 3, 4, 5), rows=(0,))
    return [neck, fold]


def rage_plate():
    # Red marks on the plates: red slashes across the front and the back of
    # the body, three on each.
    red = Mark("#D01818")
    dark = Mark("#6A0808")
    for f in ("front", "back"):
        for start in (0, 3, 6):
            for n in range(4):
                col, row = (start + n) % 8, 1 + n + start
                for part in (BODY, JACKET):
                    red.put(at(part, f, col, row))
                    if n == 3:
                        dark.put(at(part, f, col, min(11, row + 1)))
    return [red, dark]


# The Maulers.

def blood_rage():
    # Red war paint: a red band across the eyes, and red stripes down the
    # cheeks and the chin.
    red = Mark("#C81010")
    for c in range(10):
        if (c, 3) not in EYES:
            red.put(face(c, 3))
        red.put(face(c, 2))
    red.put([face(c, r) for c in (1, 8) for r in (4, 5, 6, 7)])
    return [red]


def charge():
    # Wide, red stripes: two stripes of two rows round each leg.
    red = Mark("#C81818")
    for leg in LEGS:
        put2(red, leg, SIDES, rows=(2, 3, 7, 8))
    return [red]


def battle_scars():
    scar = Mark("#F8D8C8")
    edge = Mark("#8A4A30")
    for part in (BODY,) + ARMS:
        for x, y, f, col, row in both(part, SIDES):
            n = noise100(x // 2, y, 3301)
            if n < 12:
                scar.put((x, y))
            elif n < 16:
                edge.put((x, y))
    return [scar, edge]


def roar():
    # A wide, roaring mouth: the snout is open, dark with a red inside, and
    # the dark corners of the mouth go out under the eyes, with white teeth.
    dark = Mark("#1A0404")
    red = Mark("#8A1010")
    teeth = Mark("#F8F4E8")
    dark.put([at(SNOUT, "front", c, r) for c in range(4) for r in range(4) if c in (0, 3) or r == 3])
    red.put([at(SNOUT, "front", c, r) for c in (1, 2) for r in (0, 1, 2)])
    dark.put([face(c, r) for c in (1, 2, 7, 8) for r in (5, 6, 7)])
    teeth.put([face(2, 5), face(7, 5), face(1, 7), face(8, 7)])
    return [dark, red, teeth]


def frenzy_axe():
    vein = Mark("#C01818")
    dark = Mark("#6A0808")
    for arm in ARMS:
        for f in SIDES:
            for row in range(12):
                col = (0, 1, 2, 1)[row % 4] + (1 if f in ("front", "back") else 0)
                vein.put(at(arm, f, col, row))
                if row % 4 == 2:
                    dark.put(at(arm, f, (col + 2) % 4, row))
    return [vein, dark]


def hot_head():
    # A red, hot snout: the snout is red, with an orange glow at its middle,
    # and the face round it is red too.
    red = Mark("#D02010")
    hot = Mark("#FF8020")
    put(red, SNOUT, ALL)
    hot.put([at(SNOUT, "front", c, r) for c in (1, 2) for r in (1, 2)])
    red.put([face(c, r) for c in (2, 7) for r in (4, 5, 6)] + [face(c, 3) for c in (3, 4, 5, 6)])
    return [red, hot]


# The Gatekeepers.

def home_guard():
    # The banner is the gene model home_guard_banner.
    return []


def home_guard_model():
    # A bastion banner cloth: a black cloth that hangs on the back from a bar
    # over the shoulders, with a gold pattern of a snout on it. On the bone
    # body, the back is at z 2.
    im = model_image(64, 32)

    def cloth(f, x, y):
        if f != "back":
            return "#1A1A1E"
        if y in (2, 3) and 1 <= x <= 5 or y in (5, 6, 7) and x in (2, 4) or y == 9 and 1 <= x <= 5:
            return GOLD[0]
        return "#1A1A1E" if (x + y) % 5 else "#2A2A30"
    paint_box(im, 0, 0, 7, 12, 1, cloth)
    paint_box(im, 0, 20, 9, 1, 1, lambda f, x, y: "#6A4A2A")
    parts = [{"bone": "body", "uv": [0, 0], "from": [-3.5, 0.5, 2.1], "size": [7, 12, 1], "inflate": 0.0},
             {"bone": "body", "uv": [0, 20], "from": [-4.5, -0.5, 2.1], "size": [9, 1, 1], "inflate": 0.0}]
    save_model("home_guard_banner", im, parts)


def alarm_horn():
    # The horn is the gene model alarm_horn_horn.
    return []


def alarm_horn_model():
    # A gold horn on a cord: a pale cord from the left shoulder across the
    # chest, and a large, curved gold horn that hangs from it on the front of
    # the belly, with its bell to the right. On the bone body, the front is
    # at z -2.
    im = model_image()
    paint_box(im, 0, 0, 1, 1, 1, lambda f, x, y: "#C8A870")
    paint_box(im, 0, 8, 3, 3, 2, lambda f, x, y: "#3A2A10" if f == "right" and 0 < x < 2 and 0 < y < 2
              else GOLD[2] if f == "top" else GOLD[0])
    paint_box(im, 0, 16, 3, 2, 2, lambda f, x, y: GOLD[1] if y == 1 else GOLD[0])
    paint_box(im, 0, 24, 2, 1, 1, lambda f, x, y: GOLD[1])
    parts = [{"bone": "body", "uv": [0, 0], "from": [3 - 0.75 * n, 0.4 + 0.9 * n, -2.4], "size": [1, 1, 1],
              "inflate": -0.15} for n in range(8)]
    parts.append({"bone": "body", "uv": [0, 8], "from": [-4, 6.5, -4.2], "size": [3, 3, 2], "inflate": 0.0})
    parts.append({"bone": "body", "uv": [0, 16], "from": [-1, 7, -4], "size": [3, 2, 2], "inflate": -0.1})
    parts.append({"bone": "body", "uv": [0, 24], "from": [2, 7.5, -3.5], "size": [2, 1, 1], "inflate": 0.0})
    save_model("alarm_horn_horn", im, parts)


def gold_guard():
    # Gold rings on the snout: a gold ring through the middle of the snout,
    # and a gold band round the snout.
    gold = Mark(GOLD[0])
    shine = Mark(GOLD[2])
    gold.put([at(SNOUT, "front", c, r) for c in (1, 2) for r in (2, 3)])
    shine.put(at(SNOUT, "front", 1, 2))
    for f in ("top", "bottom", "right", "left"):
        put(gold, SNOUT, (f,))
    put(gold, SNOUT, ("front",), rows=(0,))
    gold.put([face(2, 5), face(7, 5)])
    return [gold, shine]


def watch_post():
    # A narrow, watching eye: a dark band across the eyes, as a shade, with
    # one bright, narrow slit at the right eye.
    dark = Mark("#2A0E06")
    slit = Mark("#FFF0A0")
    for c in range(10):
        dark.put([face(c, 2), face(c, 4)])
        if c not in (1, 2, 3):
            dark.put(face(c, 3))
    slit.put([face(1, 3), face(2, 3), face(3, 3)])
    return [dark, slit]


def lava_moat():
    black = Mark("#120A06")
    glow = Mark("#FF6A18")
    for leg in LEGS:
        for x, y, f, col, row in both(leg, SIDES + ("bottom",), rows=range(7, 12)):
            (glow if f != "bottom" and (col + 2 * row) % 7 == 0 else black).put((x, y))
    return [black, glow]


def golem_wrecker():
    # Iron studs on the knuckles: gray studs on the front, the sides, and the
    # bottom of each hand, on rows 9 and 11.
    iron = Mark("#A0A4AA")
    dark = Mark("#4A4C52")
    for arm in ARMS:
        for x, y, f, col, row in both(arm, SIDES, rows=(9, 10, 11)):
            if row != 10 and col % 2 == 0:
                iron.put((x, y))
            elif row == 10:
                dark.put((x, y))
        put(iron, arm, ("bottom",))
    return [iron, dark]


# The Guardbreakers.

def guard_crusher():
    gold = Mark(GOLD[0])
    dark = Mark(GOLD[1])
    shine = Mark(GOLD[2])
    for x, y, f, col, row in both(RIGHT_ARM, SIDES + ("bottom",), rows=range(7, 12)):
        (dark if row == 7 else shine if row == 8 and col % 2 == 0 else gold).put((x, y))
    return [gold, dark, shine]


def armor_splitter():
    # Cracked, gold bracers: rows 4 to 8 of the arms are gold, with dark
    # cracks.
    gold = Mark(GOLD[0])
    crack = Mark("#3A2A10")
    for arm in ARMS:
        for x, y, f, col, row in both(arm, SIDES, rows=range(4, 9)):
            (crack if (col + row) % 5 == 0 or row == 6 and col == 2 else gold).put((x, y))
    return [gold, crack]


def sunder_strike():
    # A cruel grin: the corners of the mouth turn up beside the snout, dark,
    # with white, sharp tusks and a row of teeth on the low edge of the
    # snout.
    dark = Mark("#1A0604")
    teeth = Mark("#FFFFF0")
    dark.put([face(1, 4), face(1, 5), face(2, 6), face(2, 7), face(8, 4), face(8, 5), face(7, 6), face(7, 7)])
    dark.put([at(SNOUT, "front", c, 3) for c in range(4)])
    teeth.put([at(SNOUT, "front", c, 2) for c in (0, 3)] + [face(2, 5), face(7, 5)])
    for tusk in TUSKS:
        put(teeth, tusk, ALL)
    return [dark, teeth]


def rust_axe():
    rust = Mark("#A0501A")
    dark = Mark("#5A2A10")
    for arm in ARMS:
        for x, y, f, col, row in both(arm, SIDES + ("bottom",), rows=range(6, 12)):
            n = noise100(x, y, 3501)
            if n < 65:
                (dark if n < 22 else rust).put((x, y))
    return [rust, dark]


def shatter_blow():
    # Shards of armor on the belt: sharp, gray shards of iron and gold on
    # rows 8 to 11 of the body.
    iron = Mark("#B0B4BA")
    dark = Mark("#4A4C52")
    gold = Mark(GOLD[0])
    for x, y, f, col, row in both(BODY, SIDES, rows=range(8, 12)):
        n = (col * 3 + row * 5 + len(f)) % 9
        if n < 3:
            iron.put((x, y))
        elif n < 5:
            dark.put((x, y))
        elif n == 5:
            gold.put((x, y))
    return [iron, dark, gold]


def brute_force():
    # Thick, short fingers: on rows 9 to 11 of the hands, wide, pale fingers
    # of two pixels with dark gaps between them, and pale knuckles.
    finger = Mark("#F8C8A0")
    gap = Mark("#5A2A18")
    for arm in ARMS:
        for x, y, f, col, row in both(arm, SIDES, rows=(9, 10, 11)):
            (gap if col == 2 and row > 9 else finger).put((x, y))
        put(finger, arm, ("bottom",))
    return [finger, gap]


GENES = {
    "great_swing": great_swing, "head_splitter": head_splitter, "wide_arc": wide_arc, "hurl_axe": hurl_axe,
    "bone_breaker": bone_breaker, "golden_edge": golden_edge,
    "iron_hide": iron_hide, "blast_wall": blast_wall, "arrow_wall": arrow_wall, "steady_feet": steady_feet,
    "thick_neck": thick_neck, "rage_plate": rage_plate,
    "blood_rage": blood_rage, "charge": charge, "battle_scars": battle_scars, "roar": roar,
    "frenzy_axe": frenzy_axe, "hot_head": hot_head,
    "home_guard": home_guard, "alarm_horn": alarm_horn, "gold_guard": gold_guard, "watch_post": watch_post,
    "lava_moat": lava_moat, "golem_wrecker": golem_wrecker,
    "guard_crusher": guard_crusher, "armor_splitter": armor_splitter, "sunder_strike": sunder_strike,
    "rust_axe": rust_axe, "shatter_blow": shatter_blow, "brute_force": brute_force,
}

MODEL_GENES = {
    "head_splitter": (head_splitter_model, "head_splitter_crest"),
    "hurl_axe": (hurl_axe_model, "hurl_axe_axes"),
    "arrow_wall": (arrow_wall_model, "arrow_wall_arrows"),
    "home_guard": (home_guard_model, "home_guard_banner"),
    "alarm_horn": (alarm_horn_model, "alarm_horn_horn"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, label="piglin brute genes")
