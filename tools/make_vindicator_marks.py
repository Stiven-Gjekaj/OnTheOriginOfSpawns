"""Make the gene visuals of the vindicator, from design/species/vindicator.md.

Usage: python3 tools/make_vindicator_marks.py (from the root of the repository)

The vindicator texture is 64x64, with the illager layout, and it uses the
illager region map. make_pillager_marks.py gives the faces of the head, the
body, the coat, the arms, and the legs. On the vindicator:
- The coat covers the whole body. Rows 0 to 12 of its sides are drawn, with a
  V neck at the top of the front. So the marks on the body go on the coat.
- The trousers are rows 0 to 8 of the legs, and the boots are rows 9 to 11.
- When it does not attack, it crosses its arms: the part "arms" shows, with a
  box of 4x8x4 at uv (44, 22) for each upper arm and a bar of 8x4x4 at uv
  (40, 38) for the forearms and the hands. The arms right_arm and left_arm
  (uv (40, 46)) show only when it attacks. So each mark on the arms or the
  hands goes on both.
The gene models use the bones head, body, right_arm, left_arm, right_leg, and
left_leg. A model on an arm shows only while the arms are apart.

Pixel plan for the Butchers.
- Heavy Swing has thick, scarred forearms: bare gray forearms with pale red
  scars, on the lower arms and on the crossed forearms.
- Sunder has iron knuckles: steel plates with dark rivets on the knuckles.
- Gash: a bloody apron on the front of the body, the gene model gash_apron.
- Hatchet: a row of three hatchets on the belt at the back, the gene model
  hatchet_hatchets.
- Chop has rolled-up sleeves: a pale roll of cloth at the elbow, and bare
  gray forearms under it.
- Decapitator: a black hood with eye holes over the head, the gene model
  decapitator_hood.

Pixel plan for the Berserkers.
- Bloodlust has red war paint: red bands on the cheeks and a red band on the
  brow.
- Rage has bared teeth: white teeth with dark gaps at the sides of the mouth,
  beside the nose.
- Death Wish has many old scars: pale scars with stitches across the coat.
- Howl has a wide, open mouth: a dark, wide mouth with a red tongue.
- Fury has red veins: red veins that branch on the sleeves.
- Thick Blood has dark red cloth: the coat is dark red, with darker seams.

Pixel plan for the Sappers.
- Door Splitter has wood splinters on the sleeves: short splinters of pale
  wood that stick in the sleeves.
- Shatterer has heavy gloves: thick leather gloves with pale stitches, and a
  cuff on the wrist.
- Torchbearer: a lit torch on the back, the gene model torchbearer_torch.
- Climber has climbing hooks: steel hooks on the knuckles, and dark straps
  that hold them.
- Battering has a padded gambeson: the coat is quilted padding, with dark
  seams.
- Netter: a coil of rope at the right hip, the gene model netter_rope.

Pixel plan for the Ransackers.
- Village Bane: a torn villager hat of straw on the head, the gene model
  village_bane_hat.
- Golem Smasher has iron-gray fist wraps: gray wraps with dark lines round
  the hands and the wrists.
- Looting: an open sack of loot on the back, with gold and emeralds at its
  top, the gene model looting_sack.
- Ruthless has a cold, narrow stare: a heavy black brow over the eyes, gray
  eye whites, pale icy pupils, and dark lower lids.
- Hunting Call has a horn on a cord: a pale cord across the coat, and a horn
  at the left hip of the coat.
- Pillage Run has muddy boots: brown mud on the boots, with splashes up the
  trousers.

Pixel plan for the Guardsmen.
- Deflect: a steel bracer on the left arm, the gene model deflect_bracer. It
  shows only while the arms are apart.
- Bodyguard has a gray tabard: a gray tabard with dark edges on the front and
  the back of the coat, under the V neck.
- Watchman: a lantern at the belt on the left hip, with glowing panes, the
  gene model watchman_lantern.
- Stand Ground has heavy greaves: steel plates on the shins, with a pale
  upper edge and a dark strap.
- Bracer has thick leather bracers: brown leather on the forearms, with pale
  laces on the front.
- Alarm Bell has a small bell on the belt: a small gold bell with a dark
  mouth on the left hip of the coat.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_illager_region_map import HAND_PIXELS
from make_pillager_marks import ALL, ARM, BODY, COAT, HEAD, LEG, SIDES, STEEL, pixels
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/illager.png"

# The crossed arms: a box for each upper arm, and a bar for the forearms and
# the hands.
CROSSED_ARM = {"top": (48, 22, 4, 4), "bottom": (52, 22, 4, 4), "right": (44, 26, 4, 8), "front": (48, 26, 4, 8),
               "left": (52, 26, 4, 8), "back": (56, 26, 4, 8)}
CROSSED_BAR = {"top": (44, 38, 8, 4), "bottom": (52, 38, 8, 4), "right": (40, 42, 4, 4), "front": (44, 42, 8, 4),
               "left": (52, 42, 4, 4), "back": (56, 42, 8, 4)}
# The rows of the coat that the vindicator draws, and the clear V neck at the
# top of its front.
COAT_ROWS = range(0, 13)
V_NECK = {(1, 0), (2, 0), (3, 0), (4, 0), (5, 0), (6, 0), (2, 1), (3, 1), (4, 1), (5, 1), (3, 2), (4, 2)}

SKIN = ("#959B9B", "#7A8080")
SCAR = ("#C88A8A", "#8A5A5A")


def coat_pixels(faces=SIDES):
    """Return each drawn pixel (x, y, face, col, row) of the sides of the coat."""
    for x, y, face, col, row in pixels(COAT, faces):
        if row in COAT_ROWS and not (face == "front" and (col, row) in V_NECK):
            yield x, y, face, col, row


def sleeve_pixels(first=0, last=9):
    """Return the sleeve pixels (x, y, face, col, row) of the arms from row first to row last. The row of a pixel of
    the crossed arms is the row of the same place on an arm that hangs: the upper arm box is rows 0 to 7 (its own
    rows), and the bar is the forearm, rows 8 and 9."""
    for x, y, face, col, row in pixels(ARM, SIDES):
        if first <= row <= last:
            yield x, y, face, col, row
    for x, y, face, col, row in pixels(CROSSED_ARM, SIDES):
        if first <= row <= last:
            yield x, y, face, col, row
    if last >= 8:
        for x, y, face, col, row in pixels(CROSSED_BAR, ALL):
            if (x, y) not in HAND_PIXELS:
                yield x, y, face, col, 8 + row % 2


def hand_pixels():
    """Return the hand pixels (x, y, face, col, row) of the arms that hang (rows 10 and 11, and the bottom) and of the
    crossed arms."""
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row >= 10:
            yield x, y, face, col, row
    for x, y, face, col, row in pixels(ARM, ("bottom",)):
        yield x, y, face, col, 11
    for x, y, face, col, row in pixels(CROSSED_BAR, ALL):
        if (x, y) in HAND_PIXELS:
            yield x, y, face, col, 10 + row % 2


def heavy_swing():
    # Thick, scarred forearms: the forearms (rows 5 to 9, and the crossed
    # forearms) are bare gray skin, with short pale red scars across them.
    skin = Mark(SKIN[0])
    scar = Mark(SCAR[0])
    for x, y, face, col, row in sleeve_pixels(5, 9):
        (scar if (col + 2 * row) % 5 == 0 else skin).put((x, y))
    return [skin, scar]


def sunder():
    # Iron knuckles: on the knuckles (row 10 of the hands, and the top row of
    # the crossed hands), steel plates with a dark rivet on every other pixel.
    steel = Mark(STEEL[0])
    rivet = Mark(STEEL[2])
    for x, y, face, col, row in hand_pixels():
        if row == 10:
            (rivet if col % 2 else steel).put((x, y))
    return [steel, rivet]


def gash():
    # The apron is the gene model gash_apron.
    return []


def gash_model():
    # A bloody apron on the front of the body: an apron of pale canvas over
    # the coat, from the chest to the knees, with dark red stains and drips of
    # blood, and two straps up to the shoulders. On the body bone, the front
    # of the coat is at z -3.5.
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def apron(face, x, y):
        if face != "front":
            return "#B8B0A0"
        stain = noise100(x // 2, y // 2, 1013) < 30 or (x, y) in ((2, 9), (2, 10), (5, 8), (5, 9))
        if stain:
            return "#7A1010" if (x + y) % 3 else "#A82020"
        return "#D8D0C0" if (x + y) % 4 else "#C8C0B0"

    def strap(face, x, y):
        return "#8A7A60"

    # The apron: 6x10x1 at uv (0, 0). A strap: 1x3x1 at uv (16, 0).
    paint_box(im, 0, 0, 6, 10, 1, apron)
    paint_box(im, 16, 0, 1, 3, 1, strap)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-3, 3, -4.2], "size": [6, 10, 1]},
        {"bone": "body", "uv": [16, 0], "from": [-3, 0, -4.2], "size": [1, 3, 1], "inflate": -0.2},
        {"bone": "body", "uv": [16, 0], "from": [2, 0, -4.2], "size": [1, 3, 1], "inflate": -0.2},
    ]
    save_model("gash_apron", im, parts)


def hatchet():
    # The hatchets are the gene model hatchet_hatchets.
    return []


def hatchet_model():
    # A row of hatchets on the belt at the back: three small hatchets that
    # hang in a row on the back of the coat, each a wooden handle with an
    # iron head at its top, and a leather belt that holds them. On the body
    # bone, the back of the coat is at z 3.5.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def handle(face, x, y):
        return "#8A5A30" if face in ("back", "left") else "#6A4220"

    def head(face, x, y):
        if face == "back" and x == 1:
            return STEEL[0]
        return STEEL[1] if face in ("back", "top") else STEEL[2]

    # A handle: 1x5x1 at uv (0, 0). A head: 2x2x1 at uv (4, 0). The belt:
    # 8x1x1 at uv (12, 0).
    paint_box(im, 0, 0, 1, 5, 1, handle)
    paint_box(im, 4, 0, 2, 2, 1, head)
    paint_box(im, 12, 0, 8, 1, 1, lambda face, x, y: "#3A2A1A")
    parts = [{"bone": "body", "uv": [12, 0], "from": [-4, 7, 3.4], "size": [8, 1, 1], "inflate": 0.15}]
    for x in (-3, -0.5, 2):
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, 6, 3.7], "size": [1, 5, 1]})
        parts.append({"bone": "body", "uv": [4, 0], "from": [x - 0.5, 5.5, 4.2], "size": [2, 2, 1]})
    save_model("hatchet_hatchets", im, parts)


def chop():
    # Rolled-up sleeves: a pale roll of cloth at the elbow (rows 4 and 5, and
    # the ends of the crossed upper arms), and bare gray forearms under it.
    roll = Mark("#6A645E")
    shade = Mark("#4A4440")
    skin = Mark(SKIN[0])
    for x, y, face, col, row in sleeve_pixels(4, 9):
        if row == 4:
            roll.put((x, y))
        elif row == 5:
            shade.put((x, y))
        else:
            skin.put((x, y))
    return [roll, shade, skin]


def decapitator():
    # The hood is the gene model decapitator_hood.
    return []


def decapitator_model():
    # A black hood with eye holes: a hood of black cloth over the whole head,
    # a little larger than the head, with a point on its top. Two holes show
    # the eyes, and the hood is open under the chin. The nose sticks out
    # through it. On the head bone, the head is from y -10 to 0.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def hood(face, x, y):
        if face == "bottom":
            return None
        if face == "front" and y == 6 and x in (1, 2, 5, 6):
            return None
        if face == "front" and y >= 8 and 2 <= x <= 5:
            return None
        return "#1A1818" if (x + 2 * y) % 5 else "#2A2626"

    # The hood: 8x10x8 at uv (0, 0). The point: 2x2x2 at uv (32, 0).
    paint_box(im, 0, 0, 8, 10, 8, hood)
    paint_box(im, 32, 0, 2, 2, 2, lambda face, x, y: "#1A1818")
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-4, -10, -4], "size": [8, 10, 8], "inflate": 0.55},
        {"bone": "head", "uv": [32, 0], "from": [-1, -12, -1], "size": [2, 2, 2]},
    ]
    save_model("decapitator_hood", im, parts)


def bloodlust():
    # Red war paint: two short red bands on each cheek, under the eyes, and a
    # red band across the brow above the eyebrows.
    red = Mark("#B81818")
    red.put([(8, 15), (9, 15), (8, 16), (14, 15), (15, 15), (15, 16)])
    red.put([(x, 11) for x in range(9, 15)])
    return [red]


def rage():
    # Bared teeth: the mouth is wider, with white teeth and dark gaps at the
    # sides of the mouth (x 9, 10, 13, and 14). The nose hides the middle of
    # the mouth.
    tooth = Mark("#E8E8E0")
    gap = Mark("#201818")
    tooth.put([(9, 16), (10, 17), (13, 17), (14, 16)])
    gap.put([(10, 16), (9, 17), (13, 16), (14, 17)])
    return [tooth, gap]


def death_wish():
    # Many old scars: short, pale scars across the coat, each a diagonal line
    # of 3 pixels with a dark stitch across its middle.
    scar = Mark(SCAR[0])
    stitch = Mark(SCAR[1])
    starts = {"front": ((1, 4), (5, 7), (2, 10)), "back": ((1, 2), (4, 5), (2, 9), (5, 10)),
              "right": ((1, 3), (2, 8)), "left": ((2, 4), (1, 9))}
    for face, spots in starts.items():
        x0, y0 = COAT[face][:2]
        for c, r in spots:
            scar.put([(x0 + c, y0 + r), (x0 + c + 2, y0 + r + 2)])
            stitch.put((x0 + c + 1, y0 + r + 1))
    return [scar, stitch]


def howl():
    # A wide, open mouth: a dark mouth on rows 15 to 17, wider than the
    # vanilla mouth, with a red tongue at its bottom. The nose hides the
    # middle of the mouth, so the mouth shows at its sides.
    dark = Mark("#140A0A")
    tongue = Mark("#A83030")
    dark.put([(x, y) for x in range(9, 15) for y in (15, 16)])
    dark.put([(9, 17), (14, 17)])
    tongue.put([(10, 17), (13, 17)])
    return [dark, tongue]


def fury():
    # Red veins: on each face of the sleeves, a red vein that runs down the
    # arm and branches twice.
    vein = Mark("#C02020")
    for x, y, face, col, row in sleeve_pixels(0, 9):
        main = col == (1 if row < 4 else 2)
        branch = (row, col) in ((3, 0), (6, 3), (7, 3), (8, 0))
        if main or branch:
            vein.put((x, y))
    return [vein]


def thick_blood():
    # Dark red cloth: the coat is dark red, with darker seams on its edges and
    # down the middle of its front.
    red = Mark("#5A1414")
    seam = Mark("#3A0C0C")
    for x, y, face, col, row in coat_pixels():
        w = COAT[face][2]
        edge = col in (0, w - 1) or face == "front" and col in (3, 4)
        (seam if edge else red).put((x, y))
    return [red, seam]

WOOD = ("#C8A060", "#8A6430", "#5A3A1A")
LEATHER = ("#5A3A20", "#8A6A40")
ROPE = ("#C8B080", "#9A8458", "#5A4A30")


def door_splitter():
    # Wood splinters on the sleeves: short splinters of pale wood, each 2
    # pixels on a slant, that stick in the sleeves, with a dark wood root.
    pale = Mark(WOOD[0])
    dark = Mark(WOOD[1])
    for x, y, face, col, row in sleeve_pixels(0, 9):
        n = noise100(x, y, 1103)
        if n < 10:
            pale.put((x, y))
        elif n < 18:
            dark.put((x, y))
    return [pale, dark]


def shatterer():
    # Heavy gloves: the hands are thick brown leather, with pale stitches on
    # every other pixel of the knuckles, and a leather cuff on the wrist (row 9
    # of the arm).
    glove = Mark(LEATHER[0])
    stitch = Mark(LEATHER[1])
    for x, y, face, col, row in hand_pixels():
        (stitch if row == 10 and col % 2 == 0 else glove).put((x, y))
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row == 9:
            stitch.put((x, y))
    return [glove, stitch]


def torchbearer():
    # The torch is the gene model torchbearer_torch.
    return []


def torchbearer_model():
    # A torch on the back: a lit torch that hangs at a slant across the back,
    # with a wooden stick, a glowing head of fire at its top, and a leather
    # strap that holds it to the back. The fire has full light. On the body
    # bone, the back of the coat is at z 3.5, and the back of the head is at
    # z 4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def stick(face, x, y):
        return WOOD[1] if face in ("back", "left") else WOOD[2]

    def fire(face, x, y):
        if face == "top":
            return "#FFF0A0"
        return "#FFC030" if (x + y) % 2 else "#F08020"

    # The stick: 1x9x1 at uv (0, 0). The fire: 2x2x2 at uv (4, 0). The strap:
    # 8x1x2 at uv (12, 0).
    paint_box(im, 0, 0, 1, 9, 1, stick)
    paint_box(im, 4, 0, 2, 2, 2, fire)
    paint_box(im, 12, 0, 8, 1, 2, lambda face, x, y: "#3A2A1A")
    pivot = [0, 5, 4.7]
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-0.5, -2, 4.2], "size": [1, 9, 1], "pivot": pivot,
         "rotation": [0, 0, 30]},
        {"bone": "body", "uv": [4, 0], "from": [-1, -4, 3.7], "size": [2, 2, 2], "pivot": pivot,
         "rotation": [0, 0, 30], "glow": True},
        {"bone": "body", "uv": [12, 0], "from": [-4, 5, 3.5], "size": [8, 1, 2], "inflate": 0.15},
    ]
    save_model("torchbearer_torch", im, parts)


def climber():
    # Climbing hooks: a steel hook on every other pixel of the bottom row of
    # the hands, and a dark strap across the row above them.
    hook = Mark(STEEL[0])
    strap = Mark("#2A2420")
    for x, y, face, col, row in hand_pixels():
        if row == 11 and col % 2 == 0:
            hook.put((x, y))
        elif row == 10:
            strap.put((x, y))
    return [hook, strap]


def battering():
    # A padded gambeson: the coat is quilted padding of pale gray brown, with
    # dark seams on every third row and column.
    pad = Mark("#8A7E66")
    seam = Mark("#5A5040")
    for x, y, face, col, row in coat_pixels():
        (seam if row % 3 == 2 or col % 3 == 2 else pad).put((x, y))
    return [pad, seam]


def netter():
    # The rope is the gene model netter_rope.
    return []


def netter_model():
    # A coil of rope at the right hip: a round coil of pale rope, with dark
    # lines between the loops and a dark hole in its middle, that hangs on the
    # side of the coat, and a short end of rope that hangs under it. On the
    # body bone, the right side of the coat is at x -4.5.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def coil(face, x, y):
        if face in ("right", "left") and 1 <= x <= 2 and 1 <= y <= 2:
            return ROPE[2]
        if face in ("right", "left") and (x in (0, 3) or y in (0, 3)):
            return ROPE[0] if (x + y) % 2 else ROPE[1]
        return ROPE[1] if (x + y) % 2 else ROPE[0]

    # The coil: 2x4x4 at uv (0, 0). The end: 1x3x1 at uv (16, 0).
    paint_box(im, 0, 0, 2, 4, 4, coil)
    paint_box(im, 16, 0, 1, 3, 1, lambda face, x, y: ROPE[1])
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-6.5, 5.5, -2], "size": [2, 4, 4]},
        {"bone": "body", "uv": [16, 0], "from": [-6, 9.5, 0.5], "size": [1, 3, 1], "inflate": -0.2},
    ]
    save_model("netter_rope", im, parts)


def village_bane():
    # The hat is the gene model village_bane_hat.
    return []


def village_bane_model():
    # A torn villager hat: a straw hat with a wide brim, taken from a farmer,
    # on the head. Bites are torn out of the edge of the brim, and the crown
    # has a hole. On the head bone, the top of the head is at y -10.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def brim(face, x, y):
        if face == "top" and ((x, y) in ((0, 3), (0, 4), (1, 4), (9, 7), (8, 9), (9, 8), (4, 0), (5, 0))):
            return None
        if face in ("right", "front", "left", "back") and x in (2, 7):
            return None
        return "#D8B860" if (x + y) % 3 else "#A88830"

    def crown(face, x, y):
        if face == "top" and (x, y) in ((4, 3), (5, 3), (4, 4)):
            return None
        if y == 2 and face != "top":
            return "#8A3A20"
        return "#D8B860" if (x + 2 * y) % 3 else "#B89840"

    # The brim: 12x1x12 at uv (0, 0). The crown: 8x3x8 at uv (0, 16).
    paint_box(im, 0, 0, 12, 1, 12, brim)
    paint_box(im, 0, 16, 8, 3, 8, crown)
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-6, -10.4, -6], "size": [12, 1, 12]},
        {"bone": "head", "uv": [0, 16], "from": [-4, -13, -4], "size": [8, 3, 8], "inflate": 0.3},
    ]
    save_model("village_bane_hat", im, parts)


def golem_smasher():
    # Iron-gray fist wraps: the hands and the wrist (row 9 of the arm) are
    # wrapped in iron-gray cloth, with dark lines between the wraps.
    wrap = Mark("#8A8E92")
    line = Mark("#4A4E52")
    for x, y, face, col, row in hand_pixels():
        (line if (row + col) % 3 == 0 else wrap).put((x, y))
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row == 9:
            (line if col % 3 == 0 else wrap).put((x, y))
    return [wrap, line]


def looting():
    # The sack is the gene model looting_sack.
    return []


def looting_model():
    # A sack of loot on the back: an open sack of dark burlap, with a pale
    # rim, full of loot: a gold ingot and an emerald stick out of its top. On
    # the body bone, the back of the coat is at z 3.5.
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def sack(face, x, y):
        if y == 0 and face != "bottom":
            return "#A08458"
        if (x + y) % 3 == 0:
            return "#5A4028"
        return "#7A5A38" if face in ("back", "top", "left") else "#6A4C30"

    # The sack: 5x6x4 at uv (0, 0). The gold: 2x1x1 at uv (0, 12). The
    # emerald: 1x2x1 at uv (8, 12).
    paint_box(im, 0, 0, 5, 6, 4, sack)
    paint_box(im, 0, 12, 2, 1, 1, lambda face, x, y: "#F8D840" if face == "top" else "#D0A020")
    paint_box(im, 8, 12, 1, 2, 1, lambda face, x, y: "#60E080" if face in ("top", "front") else "#20A050")
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-2.5, 2, 3.6], "size": [5, 6, 4], "inflate": 0.2},
        {"bone": "body", "uv": [0, 12], "from": [-1.5, 1.2, 5], "size": [2, 1, 1], "pivot": [-0.5, 1.7, 5.5],
         "rotation": [0, 20, 15]},
        {"bone": "body", "uv": [8, 12], "from": [0.8, 0.3, 4.6], "size": [1, 2, 1]},
    ]
    save_model("looting_sack", im, parts)


def ruthless():
    # A cold, narrow stare: a heavy black brow across the face over the eyes
    # (row 13), gray eye whites, pale icy pupils, and dark lower lids under
    # the eyes.
    brow = Mark("#141414")
    white = Mark("#7A8484")
    ice = Mark("#C8E8F0")
    lid = Mark("#3A3E3E")
    brow.put([(x, 13) for x in range(8, 16)])
    white.put([(9, 14), (14, 14)])
    ice.put([(10, 14), (13, 14)])
    lid.put([(9, 15), (10, 15), (13, 15), (14, 15)])
    return [brow, white, ice, lid]


def hunting_call():
    # A horn on a cord: a pale cord from the right shoulder to the left hip on
    # the front of the coat, and back up on the back, and a horn of pale bone
    # with a brass band and a dark mouth on the left side of the coat.
    cord = Mark("#D8C8A0")
    bone = Mark("#E8DCB8")
    brass = Mark("#C8A040")
    mouth = Mark("#2A2018")
    for face, flip in (("front", False), ("back", True)):
        x0, y0, w, h = COAT[face]
        for row in range(3, 11):
            col = row - 3 if not flip else 10 - row
            cord.put((x0 + col, y0 + row))
    x0, y0 = COAT["left"][:2]
    bone.put([(x0 + 1, y0 + 8), (x0 + 2, y0 + 8), (x0 + 2, y0 + 9), (x0 + 3, y0 + 9)])
    brass.put((x0 + 3, y0 + 8))
    mouth.put([(x0 + 4, y0 + 8), (x0 + 4, y0 + 9)])
    return [cord, bone, brass, mouth]


def pillage_run():
    # Muddy boots: the boots (rows 9 to 11 of the legs) and their soles are
    # brown mud, and splashes of mud reach up the trousers to row 6.
    mud = Mark("#4A3220")
    splash = Mark("#6A4A2A")
    for x, y, face, col, row in pixels(LEG, SIDES):
        n = noise100(x, y, 1201)
        if row >= 10 or row == 9 and n < 70:
            mud.put((x, y))
        elif row >= 6 and n < 30 - 5 * (8 - row):
            splash.put((x, y))
    for x, y, face, col, row in pixels(LEG, ("bottom",)):
        mud.put((x, y))
    return [mud, splash]


def deflect():
    # The bracer is the gene model deflect_bracer.
    return []


def deflect_model():
    # A steel bracer on the left arm: a steel cuff round the forearm, with a
    # pale upper edge and a row of dark rivets. It shows only while the arms
    # are apart. On the left arm bone, the arm is from y -2 to 10, and from x
    # -1 to 3.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def steel(face, x, y):
        if face in ("top", "bottom"):
            return STEEL[1]
        if y == 0:
            return STEEL[0]
        return STEEL[2] if y == 2 and x % 2 == 0 else STEEL[1]

    # The bracer: 4x4x4 at uv (0, 0).
    paint_box(im, 0, 0, 4, 4, 4, steel)
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [-1, 4, -2], "size": [4, 4, 4], "inflate": 0.35}]
    save_model("deflect_bracer", im, parts)


def bodyguard():
    # A gray tabard: columns 1 to 6 of the front and the back of the coat are
    # gray, with dark edges. On the front, the tabard starts under the V neck.
    gray = Mark("#80868A")
    edge = Mark("#55585C")
    for x, y, face, col, row in coat_pixels(("front", "back")):
        if 1 <= col <= 6 and (face == "back" or row >= 3):
            (edge if col in (1, 6) else gray).put((x, y))
    return [gray, edge]


def watchman():
    # The lantern is the gene model watchman_lantern.
    return []


def watchman_model():
    # A lantern at the belt: an iron lantern at the left hip, with open panes
    # that show a glowing light inside, and a ring on its top. The light has
    # full light. On the body bone, the left side of the coat is at x 4.5.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def frame(face, x, y):
        if face in ("top", "bottom"):
            return "#3A3A3E"
        if 1 <= x <= 1 and 1 <= y <= 2:
            return None
        return "#3A3A3E" if (x + y) % 2 else "#4E4E54"

    def light(face, x, y):
        return "#FFE070" if (x + y) % 2 else "#FFC040"

    # The frame: 3x4x3 at uv (0, 0). The light: 2x3x2 at uv (16, 0). The ring:
    # 1x1x1 at uv (0, 8).
    paint_box(im, 0, 0, 3, 4, 3, frame)
    paint_box(im, 16, 0, 2, 3, 2, light)
    paint_box(im, 0, 8, 1, 1, 1, lambda face, x, y: "#3A3A3E")
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [4.6, 6.5, -1.5], "size": [3, 4, 3]},
        {"bone": "body", "uv": [16, 0], "from": [5.1, 7, -1], "size": [2, 3, 2], "glow": True},
        {"bone": "body", "uv": [0, 8], "from": [5.6, 5.6, -0.5], "size": [1, 1, 1]},
    ]
    save_model("watchman_lantern", im, parts)


def stand_ground():
    # Heavy greaves: steel plates on the front and the sides of the shins
    # (rows 2 to 8 of the legs), with a pale upper edge, and a dark strap on
    # row 5 that goes round the back.
    steel = Mark("#9AA0A4")
    bright = Mark("#C8CED2")
    strap = Mark("#2A2A2A")
    for x, y, face, col, row in pixels(LEG, SIDES):
        if row == 5:
            strap.put((x, y))
        elif 2 <= row <= 8 and face != "back":
            (bright if row == 2 else steel).put((x, y))
    return [steel, bright, strap]


def bracer():
    # Thick leather bracers: the forearms (rows 5 to 9, and the crossed
    # forearms) are brown leather, with pale laces that cross on the front.
    leather = Mark("#6A4428")
    lace = Mark("#C8A878")
    for x, y, face, col, row in sleeve_pixels(5, 9):
        crossed = face == "front" and (col + row) % 3 == 0
        (lace if crossed else leather).put((x, y))
    return [leather, lace]


def alarm_bell():
    # A small bell on the belt: a gold bell with a pale top, a dark mouth, and
    # a clapper, on the left side of the coat at the hip, and its top on the
    # edge of the front.
    gold = Mark("#E0B030")
    pale = Mark("#F8E080")
    dark = Mark("#6A4A10")
    x0, y0 = COAT["left"][:2]
    pale.put((x0 + 2, y0 + 6))
    gold.put([(x0 + 1, y0 + 7), (x0 + 2, y0 + 7), (x0 + 3, y0 + 7), (x0 + 1, y0 + 8), (x0 + 3, y0 + 8)])
    dark.put([(x0 + 2, y0 + 8), (x0 + 2, y0 + 9)])
    fx, fy = COAT["front"][:2]
    gold.put([(fx + 7, fy + 7), (fx + 7, fy + 8)])
    return [gold, pale, dark]



GENES = {
    "heavy_swing": heavy_swing,
    "sunder": sunder,
    "gash": gash,
    "hatchet": hatchet,
    "chop": chop,
    "decapitator": decapitator,
    "bloodlust": bloodlust,
    "rage": rage,
    "death_wish": death_wish,
    "howl": howl,
    "fury": fury,
    "thick_blood": thick_blood,
    "door_splitter": door_splitter,
    "shatterer": shatterer,
    "torchbearer": torchbearer,
    "climber": climber,
    "battering": battering,
    "netter": netter,
    "village_bane": village_bane,
    "golem_smasher": golem_smasher,
    "looting": looting,
    "ruthless": ruthless,
    "hunting_call": hunting_call,
    "pillage_run": pillage_run,
    "deflect": deflect,
    "bodyguard": bodyguard,
    "watchman": watchman,
    "stand_ground": stand_ground,
    "bracer": bracer,
    "alarm_bell": alarm_bell,
}

MODELS = {
    "gash": "originofspawns:gash_apron",
    "hatchet": "originofspawns:hatchet_hatchets",
    "decapitator": "originofspawns:decapitator_hood",
    "torchbearer": "originofspawns:torchbearer_torch",
    "netter": "originofspawns:netter_rope",
    "village_bane": "originofspawns:village_bane_hat",
    "looting": "originofspawns:looting_sack",
    "deflect": "originofspawns:deflect_bracer",
    "watchman": "originofspawns:watchman_lantern",
}

MODEL_MAKERS = [gash_model, hatchet_model, decapitator_model, torchbearer_model, netter_model, village_bane_model,
                looting_model, deflect_model, watchman_model]


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
    print("pixels that two or more vindicator genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
