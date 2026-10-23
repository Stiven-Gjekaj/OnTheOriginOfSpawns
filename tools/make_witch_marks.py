"""Make the gene visuals of the witch, from design/species/witch.md.

Usage: python3 tools/make_witch_marks.py (from the root of the repository)

The witch texture is 64x128, so each mark is 64 wide and 128 high. The witch
has the villager body, which has the parts and the uv of the illager body
(make_pillager_marks.py), and it uses the witch region map. On the witch:
- The robe (the coat) covers the body and reaches to the ankles: rows 0 to 19
  of its sides are drawn, with a V neck at the top of the front. So the marks
  on the body go on the robe.
- Only rows 8 to 11 of the legs show under the robe. Rows 10 and 11 are the
  shoes.
- The arms are always crossed (make_vindicator_marks.py gives their faces).
  The hands stay in the sleeves, in the middle of the bar of the crossed arms.
- The nose is 2x4x2 at uv (24, 0). The hat is a brim of 10x2x10 at uv (0, 64),
  a part of 7x4x7 at uv (0, 76), a part of 4x4x4 at uv (0, 87), and a tip.
The gene models use the bones head, hat (the brim), body, and arms (the
crossed arms). The hat and the nose are children of the head in WitchModel.

Pixel plan for the Brewers.
- Strong Brew has stained, purple fingers: purple stains on the hands.
- Long Brew: a long row of vials of many colors on a belt, the gene model
  long_brew_vials.
- Lingering Brew has a green cloud stain on the robe: soft patches of green on
  the lower robe.
- Blinding Brew: a black veil of net that hangs from the front of the brim,
  the gene model blinding_brew_veil.
- Wither Brew has a black band with a skull on the hat: a black band round the
  middle of the hat, with a pale skull on its front.
- Rust Brew has rust-red stains on the sleeves.

Pixel plan for the Slingers.
- Long Arm: a leather sling on the right upper arm, with a pouch, the gene
  model long_arm_sling.
- Quick Hand has quick, thin fingers: pale, thin fingers with dark gaps on the
  hands.
- Double Throw: two bottles in the crossed arms, the gene model
  double_throw_bottles.
- Sure Aim: a gold monocle on the right eye, with a chain, the gene model
  sure_aim_monocle.
- Wide Splash has splash stains on the robe: splashes of purple, green, and
  blue, each a small star.
- Glass Shards has glass shards in the sleeves: pale blue shards.

Pixel plan for the Alchemists.
- Healing Draught has a red vial on the belt: a small red vial with a glass
  rim and a cork at the right hip of the robe.
- Fire Draught has an orange band on the hat: an orange band, with a bright
  upper edge, round the middle part of the hat.
- Swift Draught has light blue stockings: light blue legs with a white
  stripe, above the shoes.
- Shade Draught has a gray, pale face: the skin of the head and the nose is
  pale gray.
- Shared Draught: a wooden bowl of green brew at the left hip, the gene model
  shared_draught_bowl.
- Feather Draught: a white feather on the hat, the gene model
  feather_draught_feather.

Pixel plan for the Coven.
- Mending Hand has a soft green glow on the hands: pale green hands with
  bright points.
- Coven Call has a long, crooked nose: a dark line that bends down the nose,
  and a green wart.
- Coven Ward has a silver charm on the chest: a silver charm on a chain on
  the front of the robe, under the crossed arms.
- Blood Pact has a red cord on the wrist: red cords where the wrists cross on
  the bar of the arms.
- Bog Circle has wet, green hems: a dark green hem on the robe, with drips
  and a wet shine.
- Last Rites: a ring of lit candles on the brim of the hat, the gene model
  last_rites_candles.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark as SquareMark, noise100, write
from make_husk_marks import paint_box, save_model
from make_illager_region_map import HAND_PIXELS
from make_pillager_marks import ALL, BODY, COAT, HEAD, LEG, SIDES, pixels
from make_stray_marks import changed_pixels
from make_vindicator_marks import CROSSED_ARM, CROSSED_BAR, V_NECK

REGION_MAP = "originofspawns:textures/region/witch.png"

HAT_BRIM = {"top": (10, 64, 10, 10), "bottom": (20, 64, 10, 10), "right": (0, 74, 10, 2), "front": (10, 74, 10, 2),
            "left": (20, 74, 10, 2), "back": (30, 74, 10, 2)}
HAT_MIDDLE = {"top": (7, 76, 7, 7), "bottom": (14, 76, 7, 7), "right": (0, 83, 7, 4), "front": (7, 83, 7, 4),
              "left": (14, 83, 7, 4), "back": (21, 83, 7, 4)}
NOSE = {"top": (26, 0, 2, 2), "bottom": (28, 0, 2, 2), "right": (24, 2, 2, 4), "front": (26, 2, 2, 4),
        "left": (28, 2, 2, 4), "back": (30, 2, 2, 4)}
ROBE_ROWS = range(0, 20)
EYES = {(9, 14), (10, 14), (13, 14), (14, 14)}

SKIN = ("#A39483", "#80725E")
GLASS = ("#C8E8F0", "#88B8C8")
POTIONS = ("#D02828", "#3A60E0", "#40B040", "#8A3AC0", "#E0C030")
CORK = "#9A6A3A"
LEATHER = ("#6A4428", "#4A2E18")


def Mark(tint, **options):
    """Return a mark of 64x128, for the witch texture."""
    return SquareMark(tint, height=128, **options)


def robe_pixels(faces=SIDES):
    """Return each pixel (x, y, face, col, row) of the sides of the robe, without the V neck."""
    for x, y, face, col, row in pixels(COAT, faces):
        if row in ROBE_ROWS and not (face == "front" and (col, row) in V_NECK):
            yield x, y, face, col, row


def sleeve_pixels():
    """Return each sleeve pixel (x, y, face, col, row) of the crossed arms. The bar has the rows 8 and 9."""
    for x, y, face, col, row in pixels(CROSSED_ARM, SIDES):
        yield x, y, face, col, row
    for x, y, face, col, row in pixels(CROSSED_BAR, ALL):
        if (x, y) not in HAND_PIXELS:
            yield x, y, face, col, 8 + row % 2


def hand_pixels():
    """Return each hand pixel (x, y, face, col, row) in the middle of the bar of the crossed arms."""
    for x, y, face, col, row in pixels(CROSSED_BAR, ALL):
        if (x, y) in HAND_PIXELS:
            yield x, y, face, col, row


def leg_pixels(first=8):
    """Return each pixel (x, y, face, col, row) of the sides of the legs from row first, and the bottom."""
    for x, y, face, col, row in pixels(LEG, SIDES):
        if row >= first:
            yield x, y, face, col, row
    for x, y, face, col, row in pixels(LEG, ("bottom",)):
        yield x, y, face, col, 11


def strong_brew():
    # Stained, purple fingers: the hands are skin with purple stains, darker
    # on the tips of the fingers.
    skin = Mark(SKIN[0])
    stain = Mark("#8A3AA0")
    dark = Mark("#5A1A70")
    for x, y, face, col, row in hand_pixels():
        if row == 3 or face == "bottom":
            dark.put((x, y))
        elif (col + row) % 2 == 0:
            stain.put((x, y))
        else:
            skin.put((x, y))
    return [skin, stain, dark]


def long_brew():
    # The vials are the gene model long_brew_vials.
    return []


def long_brew_model():
    # A long row of vials on the belt: a leather belt round the robe under the
    # crossed arms, and six small glass vials of many colors that hang from
    # it, each with a cork. On the body bone, the robe is 0.5 pixel larger
    # than the body (x -4 to 4, z -3 to 3).
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def belt(face, x, y):
        return LEATHER[0] if (x + y) % 3 else LEATHER[1]

    # The belt: 9x1x7 at uv (0, 0). A vial: 1x2x1 at uv (32 + 4 * i, 0).
    paint_box(im, 0, 0, 9, 1, 7, belt)
    for i, color in enumerate(POTIONS + ("#E070C0",)):
        paint_box(im, 32 + 4 * i, 0, 1, 2, 1, lambda face, x, y, c=color: CORK if y == 0 else c)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-4.5, 9.5, -3.5], "size": [9, 1, 7], "inflate": 0.05}]
    spots = ((-3.5, -4.6), (-1.5, -4.6), (0.5, -4.6), (2.5, -4.6), (-5.6, -1), (4.6, -1))
    for i, (x, z) in enumerate(spots):
        parts.append({"bone": "body", "uv": [32 + 4 * i, 0], "from": [x, 10.3, z], "size": [1, 2, 1]})
    save_model("long_brew_vials", im, parts)


def lingering_brew():
    # A green cloud stain on the robe: soft patches of green, with paler green
    # at their edges, on rows 8 to 16 of the robe.
    green = Mark("#4A8A3A")
    pale = Mark("#7AB868")
    for x, y, face, col, row in robe_pixels():
        if 8 <= row <= 16:
            n = noise100(x // 2, y // 2, 1501)
            if n < 22:
                green.put((x, y))
            elif n < 34:
                pale.put((x, y))
    return [green, pale]


def blinding_brew():
    # The veil is the gene model blinding_brew_veil.
    return []


def blinding_brew_model():
    # A black veil on the hat: a veil of black net that hangs from the front
    # of the brim over the eyes. Every other pixel of the net is open. On the
    # hat bone, the brim is from x 0 to 10, y 0 to 2, and z 0 to 10. The face
    # is at z 1 of the hat.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def veil(face, x, y):
        if face in ("front", "back"):
            return None if (x + y) % 2 else "#141414"
        return "#141414"

    # The veil: 9x6x1 at uv (0, 0).
    paint_box(im, 0, 0, 9, 6, 1, veil)
    parts = [{"bone": "hat", "uv": [0, 0], "from": [0.5, 2, -0.2], "size": [9, 6, 1], "inflate": -0.3}]
    save_model("blinding_brew_veil", im, parts)


def wither_brew():
    # A black band with a skull on the hat: a black band on rows 2 and 3 of the
    # middle part of the hat, and a pale skull with dark eyes on its front.
    band = Mark("#0C0C0C")
    bone = Mark("#E8E4D8")
    eye = Mark("#201818")
    for x, y, face, col, row in pixels(HAT_MIDDLE, SIDES):
        if row in (2, 3):
            band.put((x, y))
    # The skull is 5 pixels wide: a round top, two dark eyes with bone round
    # them, and a jaw with a dark gap in its middle.
    x0, y0 = HAT_MIDDLE["front"][:2]
    bone.put([(x0 + c, y0 + r) for c, r in ((2, 1), (3, 1), (4, 1), (1, 2), (3, 2), (5, 2), (2, 3), (4, 3))])
    eye.put([(x0 + 2, y0 + 2), (x0 + 4, y0 + 2), (x0 + 3, y0 + 3)])
    return [band, bone, eye]


def rust_brew():
    # Rust-red stains on the sleeves: patches of rust red, with darker rust in
    # their middle, on the crossed arms.
    rust = Mark("#B0502A")
    dark = Mark("#7A3018")
    for x, y, face, col, row in sleeve_pixels():
        n = noise100(x // 2, y // 2, 1503)
        if n < 12:
            dark.put((x, y))
        elif n < 30:
            rust.put((x, y))
    return [rust, dark]


def long_arm():
    # The sling is the gene model long_arm_sling.
    return []


def long_arm_model():
    # A leather sling on the arm: a leather band round the right upper arm,
    # and the pouch of the sling that hangs from it on two cords. On the arms
    # bone, the right upper arm is from x -8 to -4, y -2 to 6, and z -2 to 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def band(face, x, y):
        return LEATHER[1] if y == 0 else LEATHER[0]

    def pouch(face, x, y):
        return "#8A5A30" if face in ("front", "left") else LEATHER[0]

    # The band: 4x2x4 at uv (0, 0). The pouch: 2x2x1 at uv (16, 0). A cord:
    # 1x2x1 at uv (24, 0).
    paint_box(im, 0, 0, 4, 2, 4, band)
    paint_box(im, 16, 0, 2, 2, 1, pouch)
    paint_box(im, 24, 0, 1, 2, 1, lambda face, x, y: "#3A2A1A")
    parts = [
        {"bone": "arms", "uv": [0, 0], "from": [-8, 1, -2], "size": [4, 2, 4], "inflate": 0.3},
        {"bone": "arms", "uv": [24, 0], "from": [-8.4, 3, -0.5], "size": [1, 2, 1], "inflate": -0.3},
        {"bone": "arms", "uv": [16, 0], "from": [-9, 5, -1], "size": [2, 2, 1]},
    ]
    save_model("long_arm_sling", im, parts)


def quick_hand():
    # Quick, thin fingers: the hands are pale skin, with dark gaps between
    # thin fingers.
    finger = Mark("#C8B8A4")
    gap = Mark("#3A2E24")
    for x, y, face, col, row in hand_pixels():
        (gap if col % 2 else finger).put((x, y))
    return [finger, gap]


def double_throw():
    # The bottles are the gene model double_throw_bottles.
    return []


def double_throw_model():
    # Two bottles in the crossed arms: two potion bottles, one red and one
    # blue, that the arms hold in front of the bar, each with a glass top and
    # a cork. On the arms bone, the bar is from x -4 to 4, y 2 to 6, and z -2
    # to 2, and its front is at z -2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    parts = []
    for i, (x, color) in enumerate(((-2.8, POTIONS[0]), (0.8, POTIONS[1]))):
        u = 16 * i
        paint_box(im, u, 0, 2, 3, 2, lambda face, px, py, c=color: GLASS[0] if face == "top" or py == 0 else c)
        paint_box(im, u + 8, 0, 1, 1, 1, lambda face, px, py: CORK)
        parts.append({"bone": "arms", "uv": [u, 0], "from": [x, 0, -3.8], "size": [2, 3, 2]})
        parts.append({"bone": "arms", "uv": [u + 8, 0], "from": [x + 0.5, -1, -3.3], "size": [1, 1, 1]})
    save_model("double_throw_bottles", im, parts)


def sure_aim():
    # The monocle is the gene model sure_aim_monocle.
    return []


def sure_aim_model():
    # A monocle: a gold frame with a pale glass on the right eye, and a thin
    # gold chain that hangs from it down the cheek. On the head bone, the face
    # is at z -4, and the right eye is from x -3 to -1 on row -4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def frame(face, x, y):
        if face == "front" and (x, y) == (1, 1):
            return GLASS[0]
        return "#E0B030"

    # The frame: 3x3x1 at uv (0, 0). The chain: 1x3x1 at uv (12, 0).
    paint_box(im, 0, 0, 3, 3, 1, frame)
    paint_box(im, 12, 0, 1, 3, 1, lambda face, x, y: "#C89A20")
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-3.5, -5, -4.6], "size": [3, 3, 1], "inflate": -0.1},
        {"bone": "head", "uv": [12, 0], "from": [-3.5, -2.2, -4.5], "size": [1, 3, 1], "inflate": -0.35},
    ]
    save_model("sure_aim_monocle", im, parts)


def wide_splash():
    # Splash stains on the robe: splashes of purple, green, and blue, each a
    # small star of 5 pixels, on each face of the robe.
    marks = [Mark("#A050D0"), Mark("#50C050"), Mark("#5080E0")]
    spots = {"front": ((1, 11), (6, 14), (2, 17)), "back": ((2, 4), (5, 9), (2, 13), (5, 16)),
             "right": ((2, 6), (3, 13)), "left": ((2, 8), (3, 15))}
    k = 0
    for face, places in spots.items():
        x0, y0 = COAT[face][:2]
        for c, r in places:
            star = [(c, r), (c - 1, r), (c + 1, r), (c, r - 1), (c, r + 1)]
            marks[k % 3].put([(x0 + a, y0 + b) for a, b in star if 0 <= a < COAT[face][2]])
            k += 1
    return marks


def glass_shards():
    # Glass shards in the sleeves: small, pale blue shards of glass, each with
    # a bright point, in the crossed arms.
    shard = Mark(GLASS[1])
    bright = Mark("#F0FCFF")
    for x, y, face, col, row in sleeve_pixels():
        n = noise100(x, y, 1505)
        if n < 7:
            bright.put((x, y))
        elif n < 18:
            shard.put((x, y))
    return [shard, bright]

def healing_draught():
    # A red vial on the belt: a small red vial at the right hip of the front
    # of the robe (columns 1 and 2, rows 9 to 12), with a pale glass rim, a
    # cork, and a bright point.
    red = Mark("#D02020")
    glass = Mark(GLASS[0])
    cork = Mark(CORK)
    x0, y0 = COAT["front"][:2]
    cork.put([(x0 + 1, y0 + 9), (x0 + 2, y0 + 9)])
    glass.put([(x0 + 1, y0 + 10), (x0 + 2, y0 + 11)])
    red.put([(x0 + 2, y0 + 10), (x0 + 1, y0 + 11), (x0 + 1, y0 + 12), (x0 + 2, y0 + 12)])
    return [red, glass, cork]


def fire_draught():
    # An orange band on the hat: an orange band on rows 2 and 3 of the middle
    # part of the hat, with a bright upper row.
    orange = Mark("#F08020")
    bright = Mark("#FFC040")
    for x, y, face, col, row in pixels(HAT_MIDDLE, SIDES):
        if row == 2:
            bright.put((x, y))
        elif row == 3:
            orange.put((x, y))
    return [orange, bright]


def swift_draught():
    # Light blue stockings: rows 4 to 9 of the legs are light blue, with a
    # white stripe on row 8. Rows 8 and 9 show under the robe.
    blue = Mark("#80C0F0")
    white = Mark("#F0F8FF")
    for x, y, face, col, row in pixels(LEG, SIDES):
        if 4 <= row <= 9:
            (white if row == 8 else blue).put((x, y))
    return [blue, white]


def shade_draught():
    # A gray, pale face: the skin of the head and of the nose is pale gray,
    # with darker gray at the edges of the face. The eyes, the brows (row 13),
    # and the mouth stay.
    pale = Mark("#B0B0AC")
    shade = Mark("#8A8A86")
    for x, y, face, col, row in pixels(HEAD, SIDES + ("top",)):
        if (x, y) in EYES:
            continue
        if face == "front" and (row == 5 or row == 8 and col in (3, 4)):
            continue
        (shade if face == "front" and col in (0, 7) else pale).put((x, y))
    for x, y, face, col, row in pixels(NOSE, ALL):
        shade.put((x, y))
    return [pale, shade]


def shared_draught():
    # The bowl is the gene model shared_draught_bowl.
    return []


def shared_draught_model():
    # A bowl at the belt: a wooden bowl of green brew at the left hip, with a
    # wooden rim round the green brew on its top. On the body bone, the left
    # side of the robe is at x 4.5.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def bowl(face, x, y):
        if face == "top":
            return "#5AC050" if (x, y) == (1, 1) else "#7A5030"
        return "#8A5A30" if face in ("front", "left") else "#6A4220"

    # The bowl: 3x2x3 at uv (0, 0).
    paint_box(im, 0, 0, 3, 2, 3, bowl)
    parts = [{"bone": "body", "uv": [0, 0], "from": [4.6, 9.5, -1.5], "size": [3, 2, 3]}]
    save_model("shared_draught_bowl", im, parts)


def feather_draught():
    # The feather is the gene model feather_draught_feather.
    return []


def feather_draught_model():
    # A feather on the hat: a long white feather with a gray quill, stuck in
    # the hat at the right of its middle part, that leans back. On the hat
    # bone, the top of the brim is at y 0, and the middle part of the hat is
    # from x 1.75 to 8.75 and from z 2 to 9.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def feather(face, x, y):
        if face in ("front", "back") and x == 0:
            return "#9A9A9A"
        return "#F4F4F0" if y % 2 else "#DCDCD8"

    # The feather: 1x7x2 at uv (0, 0).
    paint_box(im, 0, 0, 1, 7, 2, feather)
    parts = [{"bone": "hat", "uv": [0, 0], "from": [8.4, -7, 4.5], "size": [1, 7, 2], "pivot": [8.9, 0, 5.5],
              "rotation": [-20, 0, -10]}]
    save_model("feather_draught_feather", im, parts)


def mending_hand():
    # A soft green glow on the hands: the hands are pale green, with bright
    # points.
    glow = Mark("#80E8A0")
    bright = Mark("#D8FFE4")
    for x, y, face, col, row in hand_pixels():
        (bright if (col + row) % 3 == 0 else glow).put((x, y))
    return [glow, bright]


def coven_call():
    # A long, crooked nose: a dark line that bends down the front and the
    # sides of the nose, so that the nose looks bent, and a green wart near
    # its tip.
    line = Mark("#5A3A2A")
    wart = Mark("#5A7A3A")
    x0, y0 = NOSE["front"][:2]
    line.put([(x0, y0), (x0, y0 + 1), (x0 + 1, y0 + 2), (x0 + 1, y0 + 3)])
    wart.put((x0, y0 + 3))
    x0, y0 = NOSE["right"][:2]
    line.put([(x0 + 1, y0 + 1), (x0, y0 + 2)])
    return [line, wart]


def coven_ward():
    # A silver charm on the chest: a silver charm in the shape of a diamond,
    # with a bright middle, that hangs on a thin chain on the front of the
    # robe. The nose and the crossed arms hide the top of the chest, so the
    # charm hangs under the arms, on rows 10 to 12, and the chain shows on
    # rows 8 and 9.
    silver = Mark("#C8CCD0")
    bright = Mark("#F4F8FC")
    chain = Mark("#8A8E92")
    x0, y0 = COAT["front"][:2]
    chain.put([(x0 + c, y0 + r) for c in (2, 5) for r in (8, 9)])
    silver.put([(x0 + c, y0 + r) for c, r in ((3, 10), (4, 10), (2, 11), (5, 11), (3, 12), (4, 12))])
    bright.put([(x0 + 3, y0 + 11), (x0 + 4, y0 + 11)])
    return [silver, bright, chain]


def blood_pact():
    # A red cord on the wrist: on the bar of the crossed arms, red cords on
    # columns 2 and 5 of the front and the top, where the wrists are.
    cord = Mark("#B01818")
    for face in ("front", "top"):
        x0, y0, w, h = CROSSED_BAR[face]
        cord.put([(x0 + c, y0 + r) for c in (2, 5) for r in range(h)])
    return [cord]


def bog_circle():
    # Wet, green hems: rows 17 to 19 of the robe are dark green with a wet
    # shine, and green drips hang from the hem up to row 15.
    green = Mark("#3A5A2A")
    shine = Mark("#6A9A5A")
    for x, y, face, col, row in robe_pixels():
        if row >= 17:
            (shine if row == 17 and col % 3 == 0 else green).put((x, y))
        elif row == 16 and col % 2 == 0 or row == 15 and col % 4 == 0:
            green.put((x, y))
    return [green, shine]


def last_rites():
    # The candles are the gene model last_rites_candles.
    return []


def last_rites_model():
    # A ring of candles on the hat: seven white candles that stand on the brim
    # round the middle part of the hat, each with a lit flame with full light.
    # On the hat bone, the top of the brim is at y 0, the brim is from x 0 to
    # 10 and from z 0 to 10, and the middle part of the hat is from x 1.75 to
    # 8.75 and from z 2 to 9.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def candle(face, x, y):
        return "#F0ECD8" if face in ("front", "left", "top") else "#C8C4B0"

    def flame(face, x, y):
        return "#FFE070" if face == "top" else "#FFA030"

    # A candle: 1x2x1 at uv (0, 0). A flame: 1x1x1 at uv (8, 0).
    paint_box(im, 0, 0, 1, 2, 1, candle)
    paint_box(im, 8, 0, 1, 1, 1, flame)
    parts = []
    for x, z in ((0.3, 0.3), (4.5, 0.4), (8.9, 0.3), (0.3, 4.5), (9.0, 4.5), (0.3, 8.9), (9.0, 8.9)):
        parts.append({"bone": "hat", "uv": [0, 0], "from": [x, -2, z], "size": [1, 2, 1], "inflate": -0.1})
        parts.append({"bone": "hat", "uv": [8, 0], "from": [x, -3, z], "size": [1, 1, 1], "inflate": -0.3,
                      "glow": True})
    save_model("last_rites_candles", im, parts)



GENES = {
    "strong_brew": strong_brew,
    "long_brew": long_brew,
    "lingering_brew": lingering_brew,
    "blinding_brew": blinding_brew,
    "wither_brew": wither_brew,
    "rust_brew": rust_brew,
    "long_arm": long_arm,
    "quick_hand": quick_hand,
    "double_throw": double_throw,
    "sure_aim": sure_aim,
    "wide_splash": wide_splash,
    "glass_shards": glass_shards,
    "healing_draught": healing_draught,
    "fire_draught": fire_draught,
    "swift_draught": swift_draught,
    "shade_draught": shade_draught,
    "shared_draught": shared_draught,
    "feather_draught": feather_draught,
    "mending_hand": mending_hand,
    "coven_call": coven_call,
    "coven_ward": coven_ward,
    "blood_pact": blood_pact,
    "bog_circle": bog_circle,
    "last_rites": last_rites,
}

MODELS = {
    "long_brew": "originofspawns:long_brew_vials",
    "blinding_brew": "originofspawns:blinding_brew_veil",
    "long_arm": "originofspawns:long_arm_sling",
    "double_throw": "originofspawns:double_throw_bottles",
    "sure_aim": "originofspawns:sure_aim_monocle",
    "shared_draught": "originofspawns:shared_draught_bowl",
    "feather_draught": "originofspawns:feather_draught_feather",
    "last_rites": "originofspawns:last_rites_candles",
}

MODEL_MAKERS = [long_brew_model, blinding_brew_model, long_arm_model, double_throw_model, sure_aim_model,
                shared_draught_model, feather_draught_model, last_rites_model]


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for make in MODEL_MAKERS:
        make()
    for gene, make in GENES.items():
        write(gene, make(), MODELS, REGION_MAP)
    owners = {}
    for gene in GENES:
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in changed_pixels(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = {}
    for (where, _), genes in owners.items():
        if len(genes) > 1:
            key = where + ": " + " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more witch genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
