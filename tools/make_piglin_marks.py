"""Make the gene visuals of the piglin, from design/species/piglin.md.

Usage: python3 tools/make_piglin_marks.py (from the root of the repository)

The piglin uses the piglin region map. make_zombified_piglin_marks.py gives
the parts of the layout. The piglin holds a crossbow or a gold sword in the
right hand, so the right arm is the crossbow arm. The belt goes round rows 9
and 10 of the body.
The gene models use the bones body, left_arm, right_ear, and left_ear.

Pixel plan for the Sharpshots.
- Quick Load has quick, thin fingers: thin, dark lines down the hands.
- Long Shot has a narrow, squinting eye: the right eye is shut, and the left
  eye is a thin line under a low lid.
- Shield Piercer: a bolt quiver on the left arm, the gene model
  shield_piercer_quiver.
- Fire Bolt has soot on the hands.
- Bolt Storm: a belt of bolts across the chest, the gene model
  bolt_storm_belt.
- Greedy Bolt: gold rings in the ears, the gene model greedy_bolt_rings.

Pixel plan for the Hoarders.
- Gold Wise has narrow, suspicious eyes: dark lids over and under the eyes,
  and one high brow.
- Snatch has long, grabbing fingers: long, pale fingers with dark tips.
- Glitter Eye has gold-colored eyes.
- Gilded Hide has gold studs on the belt.
- Gold Lust: a gold chain on the chest, the gene model gold_lust_chain.
- Treasure Hunter has a gold tooth: gold tusks, and a gold tooth in the
  mouth.

Pixel plan for the Huntsmen.
- Keen Nose has a wide, wet snout: a dark, wet snout with white glints.
- Wound Hunter has a hunter's scar: three claw scars across the face.
- Spear Dash has strong, bent legs: a bright thigh and a dark knee.
- Hobble has rope on the wrist: a twisted rope round the left wrist.
- Shade Cloak: a dark cloak, the gene model shade_cloak_cloak.
- Tribe Hunter has tribal paint: white and red stripes on the face.

Pixel plan for the Packmates.
- Battle Cry has an open, loud snout: a wide, open mouth with teeth.
- Shoulder Wall has thick shoulder pads of leather, with stitches.
- Piglet has small, pink ears.
- Blood Kin has a kin mark on the chest: a red diamond.
- Kin Rally has a band of kin colors on each arm: red and gold.
- Ember Kin has ember-colored stripes across the body.

Pixel plan for the Firewalkers.
- Fire Skin has a dark, scorched skin on the body and the arms.
- Overworld Lungs has wide nostrils.
- Ash Lung has gray ash on the chest.
- Hot Blade has glowing hands.
- Soul Brave has blue, calm eyes.
- Blaze Bolt has a glowing crossbow arm: bright veins down the right arm.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import (ALL, ARMS, BODY, EARS, EYES, GOLD, HEAD, LEFT_ARM, LEGS, RIGHT_ARM,
                                         SIDES, SNOUT, TUSKS, at, face, model_image, pixels, put, speckle,
                                         write_all)

SOOT = ("#2A2622", "#4A4440")


# The Sharpshots.

def quick_load():
    # Quick, thin fingers: thin, dark lines down the hands, between pale
    # fingers, on all the sides of the hands.
    line = Mark("#4A2418")
    finger = Mark("#F4C0A0")
    for arm in ARMS:
        for x, y, f, col, row in pixels(arm, SIDES, rows=range(8, 12)):
            (line if col % 2 else finger).put((x, y))
    return [line, finger]


def long_shot():
    # A narrow, squinting eye: the right eye is shut, a dark line, and the
    # left eye is a thin, bright line under a low, dark lid. A dark ring of a
    # squint goes round the left eye.
    dark = Mark("#3A1408")
    bright = Mark("#FFFFFF")
    dark.put([face(1, 3), face(2, 3), face(3, 3)])
    dark.put([face(c, 2) for c in (5, 6, 7, 8, 9)] + [face(c, 4) for c in (6, 7, 8)] + [face(5, 3), face(9, 3)])
    bright.put([face(6, 3), face(7, 3), face(8, 3)])
    return [dark, bright]


def shield_piercer():
    # The quiver is the gene model shield_piercer_quiver.
    return []


def shield_piercer_model():
    # A bolt quiver on the arm: a brown quiver on the outer side of the left
    # arm, with three bolts in it. On the bone left_arm, the arm is from x -1
    # to 3, y -2 to 10, and z -2 to 2.
    im = model_image()
    paint_box(im, 0, 0, 2, 6, 3, lambda f, x, y: "#3A2414" if y in (0, 5) else "#6A4428")
    paint_box(im, 0, 12, 1, 2, 1, lambda f, x, y: "#A0A0A8" if y == 0 else "#8A6A40")
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [3, 0, -1.5], "size": [2, 6, 3], "inflate": 0.0}]
    for x, z in ((3.2, -1), (3.8, 0.2), (4.2, -0.6)):
        parts.append({"bone": "left_arm", "uv": [0, 12], "from": [x, -2, z], "size": [1, 2, 1], "inflate": -0.2})
    save_model("shield_piercer_quiver", im, parts)


def fire_bolt():
    soot = Mark(SOOT[0])
    gray = Mark(SOOT[1])
    for arm in ARMS:
        for x, y, f, col, row in pixels(arm, SIDES + ("bottom",), rows=range(7, 12)):
            n = noise100(x, y, 2101)
            if n < 55 or row >= 10:
                (gray if n < 20 else soot).put((x, y))
    return [soot, gray]


def bolt_storm():
    # The belt is the gene model bolt_storm_belt.
    return []


def bolt_storm_model():
    # A belt of bolts: a leather belt that goes from the right shoulder down
    # to the left hip across the chest, with bolts in it. On the bone body,
    # the body is from x -4 to 4, y 0 to 12, and z -2 to 2.
    im = model_image()
    paint_box(im, 0, 0, 2, 2, 1, lambda f, x, y: "#5A3A20" if y == 1 else "#7A5030")
    paint_box(im, 0, 8, 1, 2, 1, lambda f, x, y: "#B0B0B8" if y == 0 else "#9A7040")
    parts = []
    for n in range(6):
        x, y = -4 + 1.4 * n, 0.5 + 1.9 * n
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, y, -2.5], "size": [2, 2, 1], "inflate": 0.0})
        if n % 2 == 0:
            parts.append({"bone": "body", "uv": [0, 8], "from": [x + 0.5, y - 1, -2.7], "size": [1, 2, 1],
                          "inflate": -0.15})
    save_model("bolt_storm_belt", im, parts)


def greedy_bolt():
    # The rings are the gene model greedy_bolt_rings.
    return []


def greedy_bolt_model():
    # Gold rings in the ears: a gold ring that hangs from the low end of each
    # ear. On the bones right_ear and left_ear, the ear is 1x5x4, from y 0 to
    # 5 and z -2 to 2.
    im = model_image()
    paint_box(im, 0, 0, 1, 2, 2, lambda f, x, y: GOLD[2] if y == 0 else GOLD[0])
    parts = [{"bone": "right_ear", "uv": [0, 0], "from": [-1, 4, -1], "size": [1, 2, 2], "inflate": 0.25},
             {"bone": "left_ear", "uv": [0, 0], "from": [0, 4, -1], "size": [1, 2, 2], "inflate": 0.25}]
    save_model("greedy_bolt_rings", im, parts)


# The Hoarders.

def gold_wise():
    # Narrow, suspicious eyes: dark lids over and under each eye, so the eyes
    # look narrow, and a high brow over the left eye.
    lid = Mark("#4A1E10")
    brow = Mark("#2A0E06")
    for col, row in EYES:
        side = 1 if col < 5 else -1
        lid.put([face(col, row - 1), face(col + side, row - 1), face(col, row + 1), face(col + side, row + 1),
                 face(col - side, row)])
    brow.put([face(1, 2), face(2, 2), face(3, 2), face(6, 1), face(7, 0), face(8, 1)])
    return [lid, brow]


def snatch():
    # Long, grabbing fingers: long, pale fingers down the hands, with dark
    # tips, as claws.
    finger = Mark("#F8C8A8")
    tip = Mark("#2A1008")
    for arm in ARMS:
        for x, y, f, col, row in pixels(arm, SIDES, rows=range(7, 12)):
            if col % 2 == 0:
                (tip if row == 11 else finger).put((x, y))
        put(tip, arm, ("bottom",))
    return [finger, tip]


def glitter_eye():
    # Gold-colored eyes: each eye is a gold block of 3x2 with a white point,
    # and gold glints sit under it, on the cheek.
    gold = Mark(GOLD[0])
    dark = Mark(GOLD[1])
    shine = Mark("#FFFFFF")
    for col, row in EYES:
        side = 1 if col < 5 else -1
        shine.put(face(col, row))
        gold.put([face(col + side, row), face(col - side, row), face(col, row + 1), face(col + side, row + 1),
                  face(col - side, row + 1)])
        dark.put([face(col - side, row - 1), face(col, row - 1), face(col + side, row - 1)])
        gold.put(face(col - side, row + 2))
    return [gold, dark, shine]


def gilded_hide():
    # Gold studs on the belt: gold studs on every second pixel of rows 9 and
    # 10 of the body, on all the sides.
    gold = Mark(GOLD[0])
    shine = Mark(GOLD[2])
    for x, y, f, col, row in pixels(BODY, SIDES, rows=(9, 10)):
        if (col + row) % 2 == 0:
            (shine if row == 9 else gold).put((x, y))
    return [gold, shine]


def gold_lust():
    # The chain is the gene model gold_lust_chain.
    return []


def gold_lust_model():
    # A gold chain on the chest: links that hang from the shoulders down to a
    # gold pendant on the chest. On the bone body, the front of the body is
    # at z -2.
    im = model_image()
    paint_box(im, 0, 0, 1, 1, 1, lambda f, x, y: GOLD[0])
    paint_box(im, 0, 8, 2, 2, 1, lambda f, x, y: GOLD[2] if (x, y) == (0, 0) else GOLD[1])
    links = ((-3.5, 0), (-3, 1.2), (-2.3, 2.3), (-1.5, 3.2), (3.5, 0), (3, 1.2), (2.3, 2.3), (1.5, 3.2))
    parts = [{"bone": "body", "uv": [0, 0], "from": [x - 0.5, y, -2.4], "size": [1, 1, 1], "inflate": -0.1}
             for x, y in links]
    parts.append({"bone": "body", "uv": [0, 8], "from": [-1, 3.6, -2.6], "size": [2, 2, 1], "inflate": 0.0})
    save_model("gold_lust_chain", im, parts)


def treasure_hunter():
    # A gold tooth: the snout covers the mouth, so a dark grin goes along the
    # low edge of the snout and out to the corners of the mouth beside it,
    # with a large gold tooth in its middle. The tusks are gold too.
    gold = Mark(GOLD[0])
    shine = Mark(GOLD[2])
    mouth = Mark("#3A0E08")
    for tusk in TUSKS:
        put(gold, tusk, ALL)
        put(shine, tusk, ("front",), rows=(0,))
    mouth.put([at(SNOUT, "front", c, 3) for c in (0, 3)] + [face(c, r) for c in (1, 2, 7, 8) for r in (6, 7)])
    shine.put([at(SNOUT, "front", 1, 3)])
    gold.put([at(SNOUT, "front", 2, 3), at(SNOUT, "front", 1, 2), at(SNOUT, "front", 2, 2)])
    put(gold, SNOUT, ("bottom",))
    return [gold, shine, mouth]


# The Huntsmen.

def keen_nose():
    # A wide, wet snout: the snout is a dark, wet pink, with white glints,
    # and the face beside it is wet too.
    wet = Mark("#C05A60")
    glint = Mark("#FFFFFF")
    for x, y, f, col, row in pixels(SNOUT, ALL):
        (glint if (col + row) % 3 == 0 and f == "front" else wet).put((x, y))
    wet.put([face(c, r) for c in (2, 7) for r in (4, 5, 6)])
    glint.put([face(2, 4), face(7, 4)])
    return [wet, glint]


def wound_hunter():
    # A hunter's scar: three claw scars that slant down across the right of
    # the face, pale with dark edges.
    scar = Mark("#F8D0C0")
    edge = Mark("#6A2A1A")
    for start in (0, 2, 4):
        for row in range(0, 7):
            col = start + row // 2
            if col < 10 and (col, row) not in EYES:
                scar.put(face(col, row))
                if col + 1 < 10:
                    edge.put(face(col + 1, row))
    return [scar, edge]


def spear_dash():
    bright = Mark("#F8C090")
    dark = Mark("#4A2010")
    for leg in LEGS:
        put(bright, leg, SIDES, rows=(0, 1, 2, 3))
        put(dark, leg, SIDES, rows=(6,))
        put(dark, leg, ("front",), rows=(7,))
    return [bright, dark]


def hobble():
    # Rope on the wrist: a twisted, pale rope round rows 6 to 8 of the left
    # arm, with an end that hangs down the outer side.
    rope = Mark("#C8A870")
    dark = Mark("#7A5A30")
    for x, y, f, col, row in pixels(LEFT_ARM, SIDES, rows=(6, 7, 8)):
        ((dark if (col + row) % 3 == 0 else rope)).put((x, y))
    put(rope, LEFT_ARM, ("left",), cols=(1,), rows=(9, 10))
    put(dark, LEFT_ARM, ("left",), cols=(1,), rows=(11,))
    return [rope, dark]


def shade_cloak():
    # The cloak is the gene model shade_cloak_cloak.
    return []


def shade_cloak_model():
    # A dark cloak: a long, dark cloak on the back, from the shoulders to
    # the knees, and a short cape over the shoulders. On the bone body, the
    # body is from x -4 to 4, y 0 to 12, and z -2 to 2.
    im = model_image(64, 32)
    paint_box(im, 0, 0, 9, 16, 1, lambda f, x, y: "#1E1A24" if y % 5 else "#2E2836")
    paint_box(im, 0, 20, 10, 1, 5, lambda f, x, y: "#2E2836" if f == "top" else "#1E1A24")
    parts = [{"bone": "body", "uv": [0, 0], "from": [-4.5, 0, 2.1], "size": [9, 16, 1], "inflate": 0.0},
             {"bone": "body", "uv": [0, 20], "from": [-5, -0.4, -2.5], "size": [10, 1, 5], "inflate": 0.0}]
    save_model("shade_cloak_cloak", im, parts)


def tribe_hunter():
    # Tribal paint: a white stripe down the face under each eye, a red stripe
    # beside it, and a red chevron on the brow.
    white = Mark("#F4F0E8")
    red = Mark("#C02818")
    for col, row in EYES:
        side = -1 if col < 5 else 1
        white.put([face(col, r) for r in (4, 5, 6)])
        red.put([face(col + side, r) for r in (4, 5)])
    red.put([face(3, 1), face(4, 2), face(5, 2), face(6, 1), face(2, 0), face(7, 0)])
    return [white, red]


# The Packmates.

def battle_cry():
    # An open, loud snout: a wide, open mouth under the snout, dark red
    # inside, with white teeth at its top and the tusks, and open nostrils.
    mouth = Mark("#5A0A0A")
    teeth = Mark("#F8F4E8")
    dark = Mark("#1A0404")
    mouth.put([face(c, r) for c in range(1, 9) for r in (6, 7)])
    teeth.put([face(c, 6) for c in (2, 7)] + [face(1, 6), face(8, 6)])
    dark.put([at(SNOUT, "front", c, r) for c in (0, 1, 2, 3) for r in (1, 2)])
    return [mouth, teeth, dark]


def shoulder_wall():
    # Thick shoulder pads: brown leather on the tops of the arms and on rows
    # 0 to 3, with pale stitches on row 3.
    leather = Mark("#6A4024")
    stitch = Mark("#E8D0A0")
    for arm in ARMS:
        put(leather, arm, ("top",))
        for x, y, f, col, row in pixels(arm, SIDES, rows=(0, 1, 2, 3)):
            (stitch if row == 3 and col % 2 == 0 else leather).put((x, y))
    put(leather, BODY, ("top",), cols=(0, 1, 6, 7))
    return [leather, stitch]


def piglet():
    pink = Mark("#FFA8B8")
    inner = Mark("#F07890")
    for ear in EARS:
        for x, y, f, col, row in pixels(ear, ALL):
            (inner if f == "front" or f in ("right", "left") and 0 < col < 3 and 0 < row < 4 else pink).put((x, y))
    return [pink, inner]


def blood_kin():
    # A kin mark on the chest: a red diamond with a dark middle, on the front
    # of the body.
    red = Mark("#C01818")
    dark = Mark("#4A0606")
    for row in range(1, 8):
        half = 3 - abs(row - 4)
        for col in range(4 - half, 4 + half):
            (dark if half > 1 and abs(col - 3.5) < half - 1 else red).put(at(BODY, "front", col, row))
    return [red, dark]


def kin_rally():
    # A band of kin colors: a band of red and gold on rows 3 and 4 of each
    # arm.
    red = Mark("#C01818")
    gold = Mark(GOLD[0])
    for arm in ARMS:
        put(red, arm, SIDES, rows=(3,))
        put(gold, arm, SIDES, rows=(4,))
        put(red, arm, SIDES, rows=(5,))
    return [red, gold]


def ember_kin():
    orange = Mark("#FF7A20")
    hot = Mark("#FFC040")
    for x, y, f, col, row in pixels(BODY, SIDES):
        if row in (2, 5, 8):
            (hot if (col + row) % 4 == 0 else orange).put((x, y))
    return [orange, hot]


# The Firewalkers.

def fire_skin():
    dark = Mark("#3A1C10")
    char = Mark("#1A0C06")
    for part in (BODY,) + ARMS:
        for x, y, f, col, row in pixels(part, SIDES):
            n = noise100(x, y, 2501)
            if n < 70:
                (char if n < 25 else dark).put((x, y))
    return [dark, char]


def overworld_lungs():
    # Wide nostrils: two large, dark nostrils on the snout, with a pink rim,
    # and the sides of the snout flare out on the face.
    dark = Mark("#1A0606")
    rim = Mark("#F08080")
    dark.put([at(SNOUT, "front", c, r) for c in (0, 1, 2, 3) for r in (1, 2) if c != 1 and c != 2 or r == 2])
    rim.put([at(SNOUT, "front", c, r) for c in range(4) for r in (0, 3)])
    rim.put([face(2, 5), face(2, 6), face(7, 5), face(7, 6)])
    return [dark, rim]


def ash_lung():
    ash = Mark("#A8A4A0")
    dark = Mark("#6E6A68")
    for x, y, f, col, row in pixels(BODY, ("front", "top"), rows=range(0, 8)):
        n = noise100(x, y, 2601)
        if n < 60:
            (dark if n < 20 else ash).put((x, y))
    return [ash, dark]


def hot_blade():
    hot = Mark("#FF9A20")
    white = Mark("#FFF0A0")
    for arm in ARMS:
        for x, y, f, col, row in pixels(arm, SIDES + ("bottom",), rows=range(8, 12)):
            (white if row >= 10 or f == "bottom" else hot).put((x, y))
    return [hot, white]


def soul_brave():
    # Blue, calm eyes: each eye is a blue block of 3x2 with a pale point,
    # and a soft, blue glow goes round it.
    blue = Mark("#30C0E8")
    pale = Mark("#B8F4FF")
    glow = Mark("#7AB8D0")
    for col, row in EYES:
        side = 1 if col < 5 else -1
        pale.put(face(col, row))
        blue.put([face(col + side, row), face(col - side, row), face(col, row + 1), face(col + side, row + 1),
                  face(col - side, row + 1)])
        glow.put([face(col - side, row - 1), face(col, row - 1), face(col + side, row - 1),
                  face(col, row + 2)])
    return [blue, pale, glow]


def blaze_bolt():
    # A glowing crossbow arm: bright orange veins that zigzag down the
    # crossbow arm, with yellow points.
    vein = Mark("#FF8A20")
    hot = Mark("#FFE050")
    for f in SIDES:
        for row in range(12):
            col = (0, 1, 2, 3, 2, 1)[row % 6]
            (hot if row % 3 == 0 else vein).put(at(RIGHT_ARM, f, col, row))
    return [vein, hot]


GENES = {
    "quick_load": quick_load, "long_shot": long_shot, "shield_piercer": shield_piercer, "fire_bolt": fire_bolt,
    "bolt_storm": bolt_storm, "greedy_bolt": greedy_bolt,
    "gold_wise": gold_wise, "snatch": snatch, "glitter_eye": glitter_eye, "gilded_hide": gilded_hide,
    "gold_lust": gold_lust, "treasure_hunter": treasure_hunter,
    "keen_nose": keen_nose, "wound_hunter": wound_hunter, "spear_dash": spear_dash, "hobble": hobble,
    "shade_cloak": shade_cloak, "tribe_hunter": tribe_hunter,
    "battle_cry": battle_cry, "shoulder_wall": shoulder_wall, "piglet": piglet, "blood_kin": blood_kin,
    "kin_rally": kin_rally, "ember_kin": ember_kin,
    "fire_skin": fire_skin, "overworld_lungs": overworld_lungs, "ash_lung": ash_lung, "hot_blade": hot_blade,
    "soul_brave": soul_brave, "blaze_bolt": blaze_bolt,
}

MODEL_GENES = {
    "shield_piercer": (shield_piercer_model, "shield_piercer_quiver"),
    "bolt_storm": (bolt_storm_model, "bolt_storm_belt"),
    "greedy_bolt": (greedy_bolt_model, "greedy_bolt_rings"),
    "gold_lust": (gold_lust_model, "gold_lust_chain"),
    "shade_cloak": (shade_cloak_model, "shade_cloak_cloak"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, label="piglin genes")
