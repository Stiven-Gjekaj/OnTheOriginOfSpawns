"""Make the gene visuals of the enderman, from design/species/enderman.md.

Usage: python3 tools/make_enderman_marks.py (from the root of the repository)

The enderman texture is 64x32, and it uses the enderman region map. The
parts, from EndermanModel:
- head: 8x8x8 at uv (0, 0). The face is x 8 to 15, y 8 to 15. The eyes layer
  draws the glowing eyes on row 4 of the face, at x 8 to 10 and 13 to 15.
- body: 8x12x4 at uv (32, 16).
- the four limbs: 2x30x2, all at uv (56, 0). The arms and the legs use the
  same pixels.
Because the limbs share their pixels, a gene that changes only the arms or
only the legs is a thin overlay model on those bones (limb_overlay). Because
the eyes layer draws over the eye pixels, a gene that changes the eyes is a
thin plate with full light in front of the eyes (eye_plates).

Pixel plan for the Phasers.
- Far Step has purple sparks at the feet: glowing purple sparks on the lower
  legs, an overlay on the legs.
- Shadow Step has a dark shadow on the back: wisps of dark violet on the back
  of the body.
- Pearl Heart: an ender pearl that glows in the chest, the gene model
  pearl_heart_pearl.
- Blast Step has burn marks on the arms: brown scorch with orange embers, an
  overlay on the arms.
- Sudden Step: large, bright purple eyes with full light, the gene model
  sudden_step_eyes.
- Rain Step has wet, dark legs: dark blue legs with pale blue shine, an
  overlay on the legs.

Pixel plan for the Gazers. Each eye gene is a plate with full light.
- Dread Gaze: wide, white eyes, two rows high.
- Chill Gaze: pale blue eyes.
- Mind Gaze: violet eyes, and violet spiral marks round the eyes on the face.
- Far Gaze has a third eye on the forehead: a purple eye with a dark lid on
  rows 1 and 2 of the face.
- Weak Gaze: dim gray eyes, and heavy gray lids over them on the face.
- Hollow Eyes: empty, black eyes.

Pixel plan for the Haulers.
- Block Throw has strong, thick hands: thick, gray violet hands with pale
  knuckles, a model on the arms.
- Heavy Block has stone dust on the arms: gray dust, an overlay on the arms.
- Stone Grip has gray stone on the hands: stone on the lower arms, an overlay
  on the arms.
- Shield Breaker has cracks on the chest: violet cracks with pale edges on
  the front of the body.
- Burden has a strap across the chest: a leather strap with a buckle, from
  the shoulder to the hip, on the front and the back of the body.
- Dust Throw has sand on the hands: sand on the lower arms, an overlay on the
  arms.

Pixel plan for the Bruisers.
- Heavy Fist: large, dark fists at the ends of the arms, the gene model
  heavy_fist_fists.
- Void Punch has purple veins on the arms, an overlay on the arms.
- Ender Rage has an open mouth: a dark purple, open mouth with a magenta
  inside and pale teeth on the lower face.
- Stone Skin has gray plates on the body, with dark seams.
- Wide Swing has long, thin arms with scars: pale scars across the arms, an
  overlay on the arms.
- Long Reach has bent, strong legs: dark violet knee caps and muscle lines,
  an overlay on the legs.

Pixel plan for the Rainwalkers.
- Dry Skin has a dry, cracked skin on the body: pale gray cracks.
- Storm Stride has blue stripes on the legs, an overlay on the legs.
- Splash Proof has blue drops on the head: drops with bright points on the
  top and the sides of the head.
- Wet Fury has blue lines on the arms, an overlay on the arms.
- Ice Veins has pale blue veins on the body.
- Mud Hands has mud on the hands: brown mud on the lower arms, an overlay on
  the arms.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/enderman.png"

HEAD = {"top": (8, 0, 8, 8), "bottom": (16, 0, 8, 8), "right": (0, 8, 8, 8), "front": (8, 8, 8, 8),
        "left": (16, 8, 8, 8), "back": (24, 8, 8, 8)}
BODY = {"top": (36, 16, 8, 4), "bottom": (44, 16, 8, 4), "right": (32, 20, 4, 12), "front": (36, 20, 8, 12),
        "left": (44, 20, 4, 12), "back": (48, 20, 8, 12)}
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
EYES = {(8, 12), (9, 12), (10, 12), (13, 12), (14, 12), (15, 12)}

ARMS = ("right_arm", "left_arm")
LEGS = ("right_leg", "left_leg")
VIOLET = ("#B040F0", "#E8A0FF", "#5A2A7A")
BLUE = ("#5090E0", "#B0D8FF", "#2A4A80")


def pixels(part, faces):
    """Return each pixel (x, y, face, col, row) of the faces of a part."""
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                yield x0 + col, y0 + row, face, col, row


def limb_overlay(name, bones, color, glow=False):
    """Save a gene model of a thin skin over each limb of the bones.

    color(face, col, row) gives the color of a pixel of the overlay, or None
    for a clear pixel. Row 0 is the top of the limb (the shoulder or the hip)
    and row 29 is its end (the hand or the foot). On the top and the bottom
    faces, the row is -1 and 30. The overlay is 0.08 pixel larger than the
    limb, so that it covers it.
    """
    im = Image.new("RGBA", (16, 32), (0, 0, 0, 0))

    def paint(face, x, y):
        row = -1 if face == "top" else 30 if face == "bottom" else y
        return color(face, x, row)

    paint_box(im, 0, 0, 2, 30, 2, paint)
    parts = []
    for bone in bones:
        top = -2 if bone in ARMS else 0
        part = {"bone": bone, "uv": [0, 0], "from": [-1, top, -1], "size": [2, 30, 2], "inflate": 0.08}
        if bone.startswith("left"):
            part["mirror"] = True
        if glow:
            part["glow"] = True
        parts.append(part)
    save_model(name, im, parts)


def eye_plates(name, color, rows=1):
    """Save a gene model of two flat plates with full light over the eyes. On the head bone, the face is at z -4,
    and the eyes are on row -4 of the head, from x -4 to -1 and from x 1 to 4. With rows 2, the plates are two
    rows high, from y -5 to -3."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 3, rows, 0, lambda face, x, y: color)
    parts = [{"bone": "head", "uv": [0, 0], "from": [x, -3 - rows, -4.03], "size": [3, rows, 0], "glow": True}
             for x in (-4, 1)]
    save_model(name, im, parts)


def far_step():
    # The sparks are the overlay far_step_sparks.
    return []


def far_step_model():
    # Purple sparks at the feet: glowing purple sparks on rows 21 to 29 of the
    # legs and on their bottom, more of them near the feet.
    def spark(face, col, row):
        n = noise100(col + 3 * len(face), row, 1701)
        if row >= 21 and n < (row - 18) * 6:
            return VIOLET[1] if n % 3 == 0 else VIOLET[0]
        return None
    limb_overlay("far_step_sparks", LEGS, spark, glow=True)


def shadow_step():
    # A dark shadow on the back: soft wisps of dark violet on the back of the
    # body, thicker at its middle.
    dark = Mark("#3E2456")
    mid = Mark("#62387E")
    for x, y, face, col, row in pixels(BODY, ("back",)):
        n = noise100(x // 2 + row % 2, y, 1703)
        if n < 30 + 20 * (2 <= col <= 5):
            (mid if n < 15 else dark).put((x, y))
    return [dark, mid]


def pearl_heart():
    # The pearl is the gene model pearl_heart_pearl.
    return []


def pearl_heart_model():
    # A pearl in the chest: an ender pearl of dark teal with a green middle, set
    # in the front of the chest, with full light. On the body bone, the front
    # is at z -2, and the body is from y 0 to 12.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def pearl(face, x, y):
        if face == "front":
            return ("#40C8A0", "#1E6A5A", "#1E6A5A", "#8AF0D0")[x + 2 * y]
        return "#1E6A5A"

    # The pearl: 2x2x1 at uv (0, 0).
    paint_box(im, 0, 0, 2, 2, 1, pearl)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-1, 3, -2.6], "size": [2, 2, 1], "glow": True}]
    save_model("pearl_heart_pearl", im, parts)


def blast_step():
    # The burns are the overlay blast_step_burns.
    return []


def blast_step_model():
    # Burn marks on the arms: patches of brown scorch on the arms, with orange
    # embers in them.
    def burn(face, col, row):
        n = noise100(col + 3 * len(face), row // 2, 1705)
        if 0 <= row <= 29 and n < 30:
            return "#E86020" if (row + col) % 5 == 0 else "#5A3018"
        return None
    limb_overlay("blast_step_burns", ARMS, burn)


def sudden_step():
    # The eyes are the gene model sudden_step_eyes.
    return []


def sudden_step_model():
    eye_plates("sudden_step_eyes", "#F060FF")


def rain_step():
    # The wet legs are the overlay rain_step_wet.
    return []


def rain_step_model():
    # Wet, dark legs: rows 6 to 29 of the legs are dark blue, with pale blue
    # shine and drops on them.
    def wet(face, col, row):
        if 6 <= row <= 30:
            n = noise100(col + 3 * len(face), row, 1707)
            return BLUE[1] if n < 10 else "#1A2238"
        return None
    limb_overlay("rain_step_wet", LEGS, wet)


def dread_gaze():
    # The eyes are the gene model dread_gaze_eyes.
    return []


def dread_gaze_model():
    eye_plates("dread_gaze_eyes", "#FFFFFF", rows=2)


def chill_gaze():
    # The eyes are the gene model chill_gaze_eyes.
    return []


def chill_gaze_model():
    eye_plates("chill_gaze_eyes", "#A8E0FF")


def mind_gaze():
    # Spiral marks round the eyes: violet curls on the face above, under, and
    # beside the eyes. The eyes are the gene model mind_gaze_eyes.
    curl = Mark(VIOLET[0])
    curl.put([(8, 11), (9, 10), (10, 10), (11, 11), (11, 13), (10, 13), (13, 11), (12, 10)])
    curl.put([(15, 11), (14, 10), (13, 10), (12, 13), (13, 13), (8, 13)])
    return [curl]


def mind_gaze_model():
    eye_plates("mind_gaze_eyes", "#D080FF")


def far_gaze():
    # A third eye on the forehead: a purple eye on row 2 of the face (x 11 and
    # 12), with a dark lid over it on row 1 and a pale point in its middle.
    eye = Mark("#C050F0")
    point = Mark("#F8D0FF")
    lid = Mark("#0A060E")
    lid.put([(10, 9), (11, 9), (12, 9), (13, 9)])
    eye.put([(11, 10), (12, 10), (10, 10), (13, 10)])
    point.put((12, 10))
    return [eye, point, lid]


def weak_gaze():
    # Heavy gray lids over the eyes, on row 3 of the face. The eyes are the
    # gene model weak_gaze_eyes.
    lid = Mark("#4A4A50")
    lid.put([(8, 11), (9, 11), (10, 11), (13, 11), (14, 11), (15, 11)])
    return [lid]


def weak_gaze_model():
    eye_plates("weak_gaze_eyes", "#8A8A96")


def hollow_eyes():
    # The empty eyes are the gene model hollow_eyes_eyes.
    return []


def hollow_eyes_model():
    eye_plates("hollow_eyes_eyes", "#000000")


def block_throw():
    # The hands are the gene model block_throw_hands.
    return []


def block_throw_model():
    # Strong, thick hands: a thick hand of 3x4x3 at the end of each arm, gray
    # violet with pale knuckles on its front. On an arm bone, the arm ends at y 28.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def hand(face, x, y):
        if face == "front" and y == 0:
            return "#8A7AA0"
        return "#3A3046" if (x + y) % 2 else "#4A3E58"

    # A hand: 3x4x3 at uv (0, 0).
    paint_box(im, 0, 0, 3, 4, 3, hand)
    parts = [{"bone": bone, "uv": [0, 0], "from": [-1.5, 24.5, -1.5], "size": [3, 4, 3]} for bone in ARMS]
    save_model("block_throw_hands", im, parts)


def heavy_block():
    # The dust is the overlay heavy_block_dust.
    return []


def heavy_block_model():
    # Stone dust on the arms: light and dark gray dust on all of the arms.
    def dust(face, col, row):
        n = noise100(col + 3 * len(face), row, 1709)
        if n < 14:
            return "#9A9A9A" if n < 7 else "#6A6A6A"
        return None
    limb_overlay("heavy_block_dust", ARMS, dust)


def stone_grip():
    # The stone is the overlay stone_grip_stone.
    return []


def stone_grip_model():
    # Gray stone on the hands: rows 23 to 29 and the end of the arms are gray
    # stone, with dark cracks.
    def stone(face, col, row):
        if row >= 23:
            return "#3A3A3A" if (col + row) % 4 == 0 else "#7A7A7A"
        return None
    limb_overlay("stone_grip_stone", ARMS, stone)


def shield_breaker():
    # Cracks on the chest: violet cracks that run from the middle of the front
    # of the body, with pale violet edges.
    crack = Mark(VIOLET[2])
    edge = Mark(VIOLET[1])
    path = ((3, 1), (4, 2), (4, 3), (3, 4), (2, 5), (1, 6), (5, 4), (6, 5), (6, 6), (4, 5), (4, 6), (3, 7),
            (3, 8), (4, 9))
    x0, y0 = BODY["front"][:2]
    for c, r in path:
        crack.put((x0 + c, y0 + r))
    edge.put([(x0 + c + 1, y0 + r) for c, r in path[::3] if c < 7])
    return [crack, edge]


def burden():
    # A strap across the chest: a brown leather strap from the left shoulder
    # to the right hip on the front of the body, and back up on the back, with
    # a brass buckle on the front.
    strap = Mark("#6A4428")
    buckle = Mark("#C8A040")
    for face, flip in (("front", False), ("back", True)):
        x0, y0, w, h = BODY[face]
        for row in range(h):
            c = 7 - (row * 7) // 11
            if flip:
                c = 7 - c
            ((buckle if face == "front" and row == 6 else strap)).put((x0 + c, y0 + row))
    x0, y0 = BODY["top"][:2]
    strap.put([(x0 + 7, y0 + r) for r in range(4)])
    return [strap, buckle]


def dust_throw():
    # The sand is the overlay dust_throw_sand.
    return []


def dust_throw_model():
    # Sand on the hands: tan sand on rows 22 to 29 and the end of the arms,
    # thicker at the end.
    def sand(face, col, row):
        n = noise100(col + 3 * len(face), row, 1711)
        if row >= 22 and n < (row - 19) * 9:
            return "#E0C890" if n % 2 else "#C8A868"
        return None
    limb_overlay("dust_throw_sand", ARMS, sand)


def heavy_fist():
    # The fists are the gene model heavy_fist_fists.
    return []


def heavy_fist_model():
    # Large, dark fists: a large fist of 4x5x4 at the end of each arm, black
    # with dark violet knuckles on its front. On an arm bone, the arm ends at
    # y 28.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def fist(face, x, y):
        if face == "front" and y in (0, 1) and x % 2 == 0:
            return VIOLET[2]
        return "#0E0C12" if (x + y) % 3 else "#1A1620"

    # A fist: 4x5x4 at uv (0, 0).
    paint_box(im, 0, 0, 4, 5, 4, fist)
    parts = [{"bone": bone, "uv": [0, 0], "from": [-2, 24, -2], "size": [4, 5, 4]} for bone in ARMS]
    save_model("heavy_fist_fists", im, parts)


def void_punch():
    # The veins are the overlay void_punch_veins.
    return []


def void_punch_model():
    # Purple veins on the arms: a purple vein that runs down each face of the
    # arms and branches to the other column, with pale violet points.
    def vein(face, col, row):
        if not 0 <= row <= 29:
            return None
        main = col == (row // 5) % 2
        if main:
            return VIOLET[1] if row % 9 == 0 else VIOLET[0]
        return None
    limb_overlay("void_punch_veins", ARMS, vein)


def ender_rage():
    # An open mouth: a wide mouth, dark purple round a bright magenta inside,
    # with pale teeth at its top corners. Rows 6 and 7 of the head face are
    # clear, and the front of the jaw (x 8 to 15, y 30 and 31) shows there, so
    # the mouth is on the jaw. The face is black, so the mouth is purple, not
    # black, to show on it.
    mouth = Mark("#4A1060")
    inside = Mark("#C030E0")
    tooth = Mark("#F0E8F8")
    mouth.put([(x, 30) for x in range(9, 15)] + [(9, 31), (14, 31)])
    inside.put([(10, 31), (11, 31), (12, 31), (13, 31), (11, 30), (12, 30)])
    tooth.put([(9, 30), (14, 30)])
    return [mouth, inside, tooth]


def stone_skin():
    # Gray plates on the body: tiles of gray stone with dark seams on all the
    # faces of the body.
    plate = Mark("#5A5A60")
    seam = Mark("#1E1E22")
    for x, y, face, col, row in pixels(BODY, ALL):
        (seam if col % 3 == 2 or row % 3 == 2 else plate).put((x, y))
    return [plate, seam]


def wide_swing():
    # The scars are the overlay wide_swing_scars.
    return []


def wide_swing_model():
    # Long, thin arms with scars: short, pale scars across the arms, each a
    # line on a slant across both columns of a face.
    def scar(face, col, row):
        if 0 <= row <= 29 and (row + col) % 7 == 0 and row % 14 < 9:
            return "#8A7A9A"
        return None
    limb_overlay("wide_swing_scars", ARMS, scar)


def long_reach():
    # The knees are the overlay long_reach_knees.
    return []


def long_reach_model():
    # Bent, strong legs: dark violet knee caps on rows 13 to 15 of the legs,
    # and muscle lines on the thighs and the shins.
    def knee(face, col, row):
        if 13 <= row <= 15 and face in ("front", "right", "left"):
            return VIOLET[2] if row != 14 else "#7A3AA0"
        if face == "front" and col == 0 and (4 <= row <= 11 or 18 <= row <= 26):
            return "#2A1A36"
        return None
    limb_overlay("long_reach_knees", LEGS, knee)


def dry_skin():
    # A dry, cracked skin: pale gray cracks in a net over all the faces of the
    # body.
    crack = Mark("#6A6470")
    for x, y, face, col, row in pixels(BODY, ALL):
        if (col * 3 + row * 2) % 7 == 0 or (col + row * 3) % 8 == 0:
            crack.put((x, y))
    return [crack]


def storm_stride():
    # The stripes are the overlay storm_stride_stripes.
    return []


def storm_stride_model():
    # Blue stripes on the legs: bands of bright blue on every sixth row of the
    # legs, with a pale upper edge.
    def stripe(face, col, row):
        if 0 <= row <= 29 and row % 6 in (3, 4):
            return BLUE[1] if row % 6 == 3 else BLUE[0]
        return None
    limb_overlay("storm_stride_stripes", LEGS, stripe)


def splash_proof():
    # Blue drops on the head: drops of blue, each with a bright point, on the
    # top and the sides of the head.
    drop = Mark(BLUE[0])
    bright = Mark(BLUE[1])
    for x, y, face, col, row in pixels(HEAD, ("top", "right", "left", "back")):
        n = noise100(x, y, 1713)
        if n < 6:
            bright.put((x, y))
        elif n < 16:
            drop.put((x, y))
    return [drop, bright]


def wet_fury():
    # The lines are the overlay wet_fury_lines.
    return []


def wet_fury_model():
    # Blue lines on the arms: a blue line down the outer column of each face
    # of the arms, broken every few rows.
    def line(face, col, row):
        if 0 <= row <= 29 and col == 1 and row % 5 != 4:
            return BLUE[0]
        return None
    limb_overlay("wet_fury_lines", ARMS, line)


def ice_veins():
    # Pale blue veins on the body: on each face of the body, a pale blue vein
    # that runs down and branches.
    vein = Mark(BLUE[1])
    for face in ("front", "back"):
        x0, y0 = BODY[face][:2]
        for c, r in ((3, 0), (3, 1), (4, 2), (4, 3), (3, 4), (2, 5), (2, 6), (5, 4), (6, 5), (6, 6), (3, 7),
                     (3, 8), (4, 9), (4, 10), (2, 7), (1, 8)):
            vein.put((x0 + c, y0 + r))
    for face in ("right", "left"):
        x0, y0 = BODY[face][:2]
        vein.put([(x0 + (1 + r // 4) % 4, y0 + r) for r in range(1, 11)])
    return [vein]


def mud_hands():
    # The mud is the overlay mud_hands_mud.
    return []


def mud_hands_model():
    # Mud on the hands: brown mud on rows 23 to 29 and the end of the arms,
    # with drips up to row 20.
    def mud(face, col, row):
        if row >= 23 or row in (20, 21, 22) and (col + len(face) + row) % 3 == 0:
            return "#4A3220" if (col + row) % 3 else "#6A4A2A"
        return None
    limb_overlay("mud_hands_mud", ARMS, mud)


GENES = {
    "far_step": far_step,
    "shadow_step": shadow_step,
    "pearl_heart": pearl_heart,
    "blast_step": blast_step,
    "sudden_step": sudden_step,
    "rain_step": rain_step,
    "dread_gaze": dread_gaze,
    "chill_gaze": chill_gaze,
    "mind_gaze": mind_gaze,
    "far_gaze": far_gaze,
    "weak_gaze": weak_gaze,
    "hollow_eyes": hollow_eyes,
    "block_throw": block_throw,
    "heavy_block": heavy_block,
    "stone_grip": stone_grip,
    "shield_breaker": shield_breaker,
    "burden": burden,
    "dust_throw": dust_throw,
    "heavy_fist": heavy_fist,
    "void_punch": void_punch,
    "ender_rage": ender_rage,
    "stone_skin": stone_skin,
    "wide_swing": wide_swing,
    "long_reach": long_reach,
    "dry_skin": dry_skin,
    "storm_stride": storm_stride,
    "splash_proof": splash_proof,
    "wet_fury": wet_fury,
    "ice_veins": ice_veins,
    "mud_hands": mud_hands,
}

# Each gene with a model: the maker of the model and its name.
MODEL_GENES = {
    "far_step": (far_step_model, "far_step_sparks"),
    "pearl_heart": (pearl_heart_model, "pearl_heart_pearl"),
    "blast_step": (blast_step_model, "blast_step_burns"),
    "sudden_step": (sudden_step_model, "sudden_step_eyes"),
    "rain_step": (rain_step_model, "rain_step_wet"),
    "dread_gaze": (dread_gaze_model, "dread_gaze_eyes"),
    "chill_gaze": (chill_gaze_model, "chill_gaze_eyes"),
    "mind_gaze": (mind_gaze_model, "mind_gaze_eyes"),
    "weak_gaze": (weak_gaze_model, "weak_gaze_eyes"),
    "hollow_eyes": (hollow_eyes_model, "hollow_eyes_eyes"),
    "block_throw": (block_throw_model, "block_throw_hands"),
    "heavy_block": (heavy_block_model, "heavy_block_dust"),
    "stone_grip": (stone_grip_model, "stone_grip_stone"),
    "dust_throw": (dust_throw_model, "dust_throw_sand"),
    "heavy_fist": (heavy_fist_model, "heavy_fist_fists"),
    "void_punch": (void_punch_model, "void_punch_veins"),
    "wide_swing": (wide_swing_model, "wide_swing_scars"),
    "long_reach": (long_reach_model, "long_reach_knees"),
    "storm_stride": (storm_stride_model, "storm_stride_stripes"),
    "wet_fury": (wet_fury_model, "wet_fury_lines"),
    "mud_hands": (mud_hands_model, "mud_hands_mud"),
}

MODELS = {gene: "originofspawns:" + name for gene, (_, name) in MODEL_GENES.items()}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for make, _ in MODEL_GENES.values():
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
    print("pixels that two or more enderman genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
