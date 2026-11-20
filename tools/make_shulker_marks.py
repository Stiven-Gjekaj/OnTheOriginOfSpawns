"""Make the gene visuals of the shulker, from design/species/shulker.md.

Usage: python3 tools/make_shulker_marks.py (from the root of the repository)

The shulker textures are 64x64, one layout for 17 colors, and they use the
shulker region map. The parts, from ShulkerModel:
- the lid, 16x12x16 at uv (0, 0). The top is 16x16. On the four sides, rows
  0 to 7 are full. In rows 8 to 11, only columns 0 to 3 and 12 to 15 are
  opaque: these are the corners of the lid, which reach down over the base.
- the base, 16x8x16 at uv (0, 28). On the four sides, rows 4 to 7 are
  full. In rows 0 to 3, only columns 4 to 11 are opaque: these fill the
  gaps of the lid when it is shut.
- the head, 6x6x6 at uv (0, 52). It is pale yellow on all colors. The eyes
  are in row 3 of the front, in columns 0 and 1 and in columns 4 and 5. Row
  2 is a dark band round the head. When the lid is open, rows 0 to 3 show
  over the base, and the player sees rows 4 and 5 through the open top of
  the base.

A shulker has one of 17 colors, from white to black. So each mark on the
lid and the base has a light part and a dark part: the light part shows on
the dark colors, and the dark part shows on the light colors.

Each gene keeps to its own zone, so that the marks stack:
- The lid top. Thick Lid has the rim, the outer 2 rows and columns. Heir
  Shell has a cross through the middle. Double Shot has two rings in the
  front right and the back left quarters. High Lift has spots in the two
  other quarters.
- The lid sides. High Lift has spots in rows 1 and 2. Blink Shut has a seam
  in rows 4 and 5. Snap Lid has teeth on the corners of the lid, rows 7 to
  11.
- The base. Quick Blink has sparks on the high part, rows 0 to 3. Steep
  Lift has a band in rows 4 and 5. Blast Lid has sooty patches at the ends
  of rows 6 and 7, and Split Reach has buds in the middle of rows 6 and 7.
- The head. Hot Shot has the back half of the top and the high rows of the
  back. Heavy Air has the low rows of the back and the back half of the
  sides. Scatter Bud has the front half of the top and columns 3 and 4 of
  the sides. Sight Blink has rows 0 to 2 of the front. Seeker Shot has the
  eye row and the rows under it, the front column of the sides, and the
  gene model seeker_shot_eyes: two glowing lenses on the eyes.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/shulker.png"

LID = box_faces(0, 0, 16, 12, 16)
BASE = box_faces(0, 28, 16, 8, 16)
HEAD = box_faces(0, 52, 6, 6, 6)
SIDES = ("right", "front", "left", "back")


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    assert 0 <= col < w and 0 <= row < h, (face, col, row)
    return x0 + col, y0 + row


def cells(part, face, cols, rows):
    return [at(part, face, c, r) for c in cols for r in rows]


def all_sides(part, cols, rows):
    return [p for f in SIDES for p in cells(part, f, cols, rows)]


def lid_opaque(col, row):
    return row < 8 or col < 4 or col > 11


def base_opaque(col, row):
    return row >= 4 or 4 <= col <= 11


# The lid top.

def thick_lid():
    # A thick, dark rim on the lid: the outer row and column of the top are
    # near black, and the next row and column are a light gray edge.
    dark = Mark("#18141C")
    edge = Mark("#A8A0B0")
    for c in range(16):
        for r in range(16):
            ring = min(c, r, 15 - c, 15 - r)
            if ring == 0:
                dark.put(at(LID, "top", c, r))
            elif ring == 1:
                edge.put(at(LID, "top", c, r))
    return [dark, edge]


def heir_shell():
    # A pale cross on the lid: two arms, 2 pixels wide, cross in the middle of
    # the top. A dark line runs along each side of each arm.
    pale = Mark("#FFF8E4")
    dark = Mark("#3A3040")
    inner = range(2, 14)
    for k in inner:
        for m in (7, 8):
            pale.put([at(LID, "top", k, m), at(LID, "top", m, k)])
    for k in inner:
        for m in (6, 9):
            if k not in (6, 7, 8, 9):
                dark.put([at(LID, "top", k, m), at(LID, "top", m, k)])
    return [pale, dark]


# The four quarters of the top, between the rim and the cross: columns and
# rows 2 to 5, and 10 to 13.
QUARTERS = {"front right": (2, 10), "back left": (10, 2), "front left": (10, 10), "back right": (2, 2)}


def double_shot():
    # Two dark rings on the lid: a dark violet ring of 4x4, with a light ring
    # inside it, in the front right and the back left quarters of the top.
    # The violet ring shows on a black shulker too.
    dark = Mark("#30084C")
    light = Mark("#E0D0F0")
    for name in ("front right", "back left"):
        c0, r0 = QUARTERS[name]
        for c in range(4):
            for r in range(4):
                edge = c in (0, 3) or r in (0, 3)
                (dark if edge else light).put(at(LID, "top", c0 + c, r0 + r))
    return [dark, light]


def high_lift():
    # White spots on the lid: spots with a white middle and gray corners, in
    # the two other quarters of the top, and in rows 1 and 2 of the sides.
    white = Mark("#FFFFFF")
    rim = Mark("#5A5468")
    for name in ("front left", "back right"):
        c0, r0 = QUARTERS[name]
        for dc, dr in ((0, 0), (2, 2)):
            white.put([at(LID, "top", c0 + dc, r0 + dr), at(LID, "top", c0 + dc + 1, r0 + dr + 1)])
            rim.put([at(LID, "top", c0 + dc + 1, r0 + dr), at(LID, "top", c0 + dc, r0 + dr + 1)])
    for f in SIDES:
        for c in (3, 11):
            white.put([at(LID, f, c, 1), at(LID, f, c + 1, 2)])
            rim.put([at(LID, f, c + 1, 1), at(LID, f, c, 2)])
    return [white, rim]


# The lid sides.

def blink_shut():
    # A purple seam on the lid: a bright violet seam runs round the sides of
    # the lid in row 4, with a dark line under it in row 5.
    seam = Mark("#E868FF")
    dark = Mark("#2A0C3A")
    seam.put(all_sides(LID, range(16), (4,)))
    dark.put(all_sides(LID, range(16), (5,)))
    return [seam, dark]


def snap_lid():
    # Teeth on the edge of the lid: the corners of the lid, rows 8 to 11,
    # are rows of white teeth with dark gaps, and row 7 over them is a dark
    # gum line.
    tooth = Mark("#F4F0DC")
    gap = Mark("#1C1414")
    for f in SIDES:
        for c in list(range(4)) + list(range(12, 16)):
            gap.put(at(LID, f, c, 7))
            for r in range(8, 12):
                (gap if c in (1, 14) or (c in (2, 13) and r == 11) else tooth).put(at(LID, f, c, r))
    return [tooth, gap]


# The base.

def quick_blink():
    # Purple sparks on the base: sparks of violet, each a cross with a white
    # middle, on the high part of the base.
    spark = Mark("#B030FF")
    core = Mark("#FFFFFF")
    for f in SIDES:
        for c, r in ((5, 1), (9, 2)):
            core.put(at(BASE, f, c, r))
            spark.put([at(BASE, f, c + dc, r + dr) for dc, dr in ((-1, 0), (1, 0), (0, -1), (0, 1))
                       if base_opaque(c + dc, r + dr)])
    return [spark, core]


def steep_lift():
    # Light blue bands on the base: a light blue band in row 5, with a dark
    # blue line over it in row 4.
    blue = Mark("#9CE4FF")
    line = Mark("#10304C")
    blue.put(all_sides(BASE, range(16), (5,)))
    line.put(all_sides(BASE, range(16), (4,)))
    return [blue, line]


def blast_lid():
    # Sooty patches on the base: patches of soot and pale ash at the two ends
    # of rows 6 and 7 of each side.
    soot = Mark("#1C1A18")
    ash = Mark("#B8B0A8")
    for f in SIDES:
        for c in list(range(0, 5)) + list(range(11, 16)):
            for r in (6, 7):
                x, y = at(BASE, f, c, r)
                (ash if noise100(x, y, 2101) < 45 else soot).put((x, y))
    return [soot, ash]


def split_reach():
    # Small buds on the base: two small, pale buds in the middle of rows 6
    # and 7 of each side. Each bud has a dark shade at its low right.
    bud = Mark("#F8E8F4")
    shade = Mark("#4A2A44")
    for f in SIDES:
        for c in (5, 9):
            bud.put([at(BASE, f, c, 6), at(BASE, f, c + 1, 6), at(BASE, f, c, 7)])
            shade.put(at(BASE, f, c + 1, 7))
        shade.put([at(BASE, f, 7, 7), at(BASE, f, 8, 7)])
    return [bud, shade]


# The head. On a side face of the head, column 0 is at the back and column 5
# at the front. The helper side() gives the pixel of the right side and the
# same pixel of the left side.

def side(col, row):
    x0, y0, w, h = HEAD["right"]
    lx, ly, lw, lh = HEAD["left"]
    return [(x0 + col, y0 + row), (lx + lw - 1 - col, ly + row)]


def hot_shot():
    # An orange head: the back half of the top and the high rows of the back
    # are orange, with a darker orange edge.
    orange = Mark("#FF8A1C")
    dark = Mark("#C85A10")
    orange.put(cells(HEAD, "top", range(6), (0, 1)))
    dark.put(cells(HEAD, "top", range(6), (2,)))
    orange.put(cells(HEAD, "back", range(6), (0,)))
    dark.put(cells(HEAD, "back", range(6), (1,)))
    return [orange, dark]


def heavy_air():
    # A gray head: the low rows of the back and the back half of the sides
    # are gray.
    gray = Mark("#8A8A88")
    dark = Mark("#5E5E5C")
    gray.put(cells(HEAD, "back", range(6), (2, 3, 4, 5)))
    for c in range(3):
        for r in range(6):
            (dark if r == 2 else gray).put(side(c, r))
    return [gray, dark]


def scatter_bud():
    # A pink head: the front half of the top, and columns 3 and 4 of the
    # sides, are pink.
    pink = Mark("#F49AC0")
    dark = Mark("#C8608C")
    pink.put(cells(HEAD, "top", range(6), (3, 4, 5)))
    for c in range(3, 5):
        for r in range(6):
            (dark if r == 2 else pink).put(side(c, r))
    return [pink, dark]


def sight_blink():
    # Wide, pale eyes: the eyes open up into rows 1 and 2 over them, with pale
    # whites and a gray rim, and row 0 is a pale brow.
    white = Mark("#FFFFFF")
    rim = Mark("#8A8478")
    brow = Mark("#F8F8E8")
    for c0 in (0, 4):
        white.put(cells(HEAD, "front", (c0, c0 + 1), (1, 2)))
    rim.put(cells(HEAD, "front", (2, 3), (1, 2)))
    brow.put(cells(HEAD, "front", range(6), (0,)))
    return [white, rim, brow]


def seeker_shot():
    # Pale yellow eyes: the eyes are pale yellow, and a yellow glow runs down
    # under them and round the front corners of the head, in the front
    # column of the sides. The glowing lenses are the gene model
    # seeker_shot_eyes.
    eye = Mark("#FFF27A")
    glow = Mark("#E8C030")
    eye.put(cells(HEAD, "front", (0, 1, 4, 5), (3,)))
    glow.put(cells(HEAD, "front", (2, 3), (3,)))
    glow.put(cells(HEAD, "front", range(6), (4, 5)))
    for r in range(6):
        (eye if r == 3 else glow).put(side(5, r))
    return [eye, glow]


def seeker_shot_model():
    # Two glowing, pale yellow lenses stand out 1 pixel over the eyes. On
    # the bone head, the head is from x -3 to 3, y 0 to 6, and z -3 to 3. The
    # eye row is y 3 to 4.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 2, 1, 2, lambda f, x, y: "#FFFFE0" if f == "front" and x == 1 else "#FFF27A")
    parts = [{"bone": "head", "uv": [0, 0], "from": [x, 3, -4], "size": [2, 1, 2], "inflate": -0.05, "glow": True}
             for x in (-3, 1)]
    save_model("seeker_shot_eyes", im, parts)


GENES = {
    "hot_shot": hot_shot, "double_shot": double_shot, "seeker_shot": seeker_shot,
    "high_lift": high_lift, "heavy_air": heavy_air, "steep_lift": steep_lift,
    "thick_lid": thick_lid, "blast_lid": blast_lid, "snap_lid": snap_lid,
    "quick_blink": quick_blink, "sight_blink": sight_blink, "blink_shut": blink_shut,
    "split_reach": split_reach, "heir_shell": heir_shell, "scatter_bud": scatter_bud,
}

MODEL_GENES = {
    "seeker_shot": (seeker_shot_model, "seeker_shot_eyes"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="shulker genes")
