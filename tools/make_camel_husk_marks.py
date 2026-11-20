"""Make the gene visuals of the camel husk, from design/species/camel_husk.md.

Usage: python3 tools/make_camel_husk_marks.py (from the root of the repository)

The camel husk texture is 128x128, and it uses the camel husk region map. The
parts, from AdultCamelModel:
- the body, 15x12x27 at uv (0, 25). On its top, row 0 is at the tail and
  row 26 is at the neck. Column 0 of the top is on the right side.
- the hump, 9x5x11 at uv (74, 0). It sits on rows 8 to 18 and columns 3 to
  11 of the top of the body.
- the low neck, 7x8x19 at uv (60, 24), the high neck, 7x14x7 at uv (21, 0),
  and the snout, 5x5x6 at uv (50, 0). The snout covers columns 1 to 5 of
  rows 0 to 4 of the front of the high neck. Rows 0 to 5 of the high neck
  are the head. The eye is in row 1, at the front end of each side.
- the ears, 3x1x2 at uv (45, 0) and (67, 0).
- the four legs, 5x21x5 at uv (0, 0), (0, 26), (58, 16), and (94, 16).
On a side face, the script counts the columns from the back of the animal
(the tail) to the front. The helper side() puts the same column on the right
side and on the left side, so each mark is the same on both sides.

Each gene keeps to its own zone, so that the marks stack:
- The head. Ram Dash has the top of the head. Shield Kick has the brow, row
  0 of the sides and the front edge of the top. Quick Dash has the eye row
  and two streaks behind the eye. Night Trek has a glowing eye, a gene
  model, and a halo under the eye. Rider Bond has the top of the snout.
  Sand Spit has the mouth, the low rows and the bottom of the snout, and
  the throat under it. Shy Step has the ears, and tall ears, a gene model.
- The body. Fire Cloth has the back end of the back, rows 0 to 5 of the
  top. Saddle Wall has the middle of the back round the hump, rows 6 to 20
  of the top, and a drape on the high sides. Iron Studs has a ring of
  studs round the base of the hump, and the front and the back of the hump.
  Last Carry has tassels under the drape. Spear Charge has a band round the
  body in front of the hump, columns 21 to 23 of the sides. Thick Hide has
  the front end of the body, the chest, and the low neck. Dried Flesh has
  the back end of the low sides, the rump, and the belly. Deep Reserve has
  the sides of the hump. Bone Hump has the top of the hump. Sand Veil is a
  faded, sandy color under all the other marks. Water Hump (a swollen hump)
  and High Seat (a saddle frame) are gene models.
- The legs, from the top: Sand Shove rows 0 to 2, Escape Run rows 3 to 5,
  Stubborn Hooves rows 6 to 8 (the knees), Golem Kick rows 9 to 11 (the
  shins), Sand Leap rows 12 and 13 (the ankles), Dust Dash rows 14 and 15,
  Sand Spray rows 16 and 17 on the front and the outer side, Hard Kick rows
  18 to 20 on the front and the outer side and the bottom, and Swift Stride
  rows 16 to 20 on the back and the inner side. Sure Feet is a gene model:
  a hard, wide sole round row 20. Long Dash is a gene model: long, lean
  thighs that go up the flank. Sand Shove also has a gene model: a thick
  copy of each leg with the texture of the mob, so the leg marks show on it.
The gene models use the bones body, head, left_ear, right_ear, and the four
legs. The hump is a child of the body that does not move, so the hump
models use the bone body.
This script needs Pillow.
"""
from make_gene_marks import Mark as TextureMark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/camel_husk.png"


def Mark(tint, **kw):
    # The camel husk texture is 128x128.
    return TextureMark(tint, size=128, height=128, **kw)


BODY = box_faces(0, 25, 15, 12, 27)
HUMP = box_faces(74, 0, 9, 5, 11)
LOW_NECK = box_faces(60, 24, 7, 8, 19)
HIGH_NECK = box_faces(21, 0, 7, 14, 7)
SNOUT = box_faces(50, 0, 5, 5, 6)
LEFT_EAR = box_faces(45, 0, 3, 1, 2)
RIGHT_EAR = box_faces(67, 0, 3, 1, 2)
TAIL = box_faces(122, 0, 3, 14, 0)
# The legs, with the side of the animal where each leg is.
LEGS = [(box_faces(58, 16, 5, 21, 5), "left"), (box_faces(94, 16, 5, 21, 5), "right"),
        (box_faces(0, 0, 5, 21, 5), "left"), (box_faces(0, 26, 5, 21, 5), "right")]
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES


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


def side(part, col, row):
    """Return a pixel of the right side and the same pixel of the left side.

    The column counts from the back of the part (the tail end) to the front.
    """
    x0, y0, w, h = part["right"]
    if not (0 <= col < w and 0 <= row < h):
        return []
    lx, ly, lw, lh = part["left"]
    return [(x0 + col, y0 + row), (lx + lw - 1 - col, ly + row)]


def put_side(mark, part, cells):
    for c, r in cells:
        mark.put(side(part, c, r))
    return mark


def top(part, col, row):
    x0, y0, w, h = part["top"]
    return (x0 + col, y0 + row) if 0 <= col < w and 0 <= row < h else None


def put_top(mark, part, cells):
    mark.put([p for p in (top(part, c, r) for c, r in cells) if p])
    return mark


def leg_faces(leg, where):
    """Return the names of the outer and the inner faces of a leg."""
    return ("left", "right") if where == "left" else ("right", "left")


def leg_put(mark, faces_of, rows, test=None):
    """Put leg pixels on a mark. faces_of(where) gives the faces of a leg."""
    for n, (leg, where) in enumerate(LEGS):
        for x, y, f, col, row in pixels(leg, faces_of(where), rows=rows):
            if test is None or test(x, y, f, col, row, n):
                mark.put((x, y))


def all_sides(where):
    return SIDES


def front_outer(where):
    return ("front", leg_faces(None, where)[0])


def back_inner(where):
    return ("back", leg_faces(None, where)[1])


# The head.

def ram_dash():
    # A hard, scarred brow: a pale plate of bone on the top of the head and
    # down the back of the head, with dark scars across it.
    bone = Mark("#C8B898")
    edge = Mark("#8A7A60")
    scar = Mark("#5A2A1E")
    scars = {(0, 1), (1, 2), (2, 3), (3, 4), (4, 5), (4, 0), (5, 1), (6, 2), (2, 0), (3, 1)}
    for x, y, f, col, row in pixels(HIGH_NECK, ("top",), rows=range(6)):
        if (col, row) in scars:
            scar.put((x, y))
        elif col in (0, 6) or row == 0:
            edge.put((x, y))
        else:
            bone.put((x, y))
    for x, y, f, col, row in pixels(HIGH_NECK, ("back",), rows=range(3)):
        (scar if (col + row) % 4 == 0 else edge if row == 2 else bone).put((x, y))
    return [bone, edge, scar]


def quick_dash():
    # Narrow, bright eyes: a long, narrow slit of bright amber in the eye
    # row, hot white at the front, and two amber streaks that run from the
    # eye down the back of the neck, as lines of speed.
    amber = Mark("#FFB020")
    hot = Mark("#FFF4B0")
    put_side(amber, HIGH_NECK, [(3, 1), (4, 1), (5, 1)])
    put_side(hot, HIGH_NECK, [(6, 1)])
    hot.put([at(HIGH_NECK, "front", 0, 1), at(HIGH_NECK, "front", 6, 1)])
    streaks = [(4, 3), (3, 3), (3, 4), (2, 5), (2, 6), (1, 7), (1, 8), (0, 9), (0, 10),
               (4, 6), (4, 7), (3, 8), (3, 9), (2, 10), (2, 11), (1, 12), (1, 13)]
    put_side(amber, HIGH_NECK, streaks)
    put_side(hot, HIGH_NECK, [(c + 1, r) for c, r in streaks if c < 4 and (r in (3, 6, 7) or r % 2 == 0)])
    return [amber, hot]


def night_trek():
    # Pale, glowing eyes: the glow is the gene model night_trek_eyes. A pale
    # blue halo lies under each eye, and pale blue tracks of light run from
    # the eyes down the front edge of the neck.
    halo = Mark("#A8E0FF")
    pale = Mark("#E0F6FF")
    put_side(halo, HIGH_NECK, [(4, 2), (5, 2), (6, 2), (5, 3), (6, 3)])
    halo.put([at(HIGH_NECK, "front", c, r) for c in (0, 6) for r in (2, 3)])
    put_side(pale, HIGH_NECK, [(6, r) for r in range(4, 14)] + [(5, r) for r in range(4, 14)])
    return [halo, pale]


def night_trek_model():
    # A glowing, pale eye on each side of the head, over the eye: a flat
    # square of 2x2. On the bone head, the high neck is from x -3.5 to 3.5,
    # y -21 to -7, and z -15 to -8. The eye is at y -20 to -18, z -15 to -13.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 0, 2, 2, lambda f, x, y: "#F0FFFF" if (x + y) % 2 else "#B8F0FF")
    parts = [{"bone": "head", "uv": [0, 0], "from": [3.55, -20, -15], "size": [0, 2, 2], "glow": True},
             {"bone": "head", "uv": [0, 0], "from": [-3.55, -20, -15], "size": [0, 2, 2], "glow": True}]
    save_model("night_trek_eyes", im, parts)


def shy_step():
    # Tall, alert ears: the tall ears are the gene model shy_step_ears. The
    # flat ears of the camel turn pale inside.
    pale = Mark("#E8D0B0")
    for ear in (LEFT_EAR, RIGHT_EAR):
        put(pale, ear, ("top", "bottom", "front"))
    return [pale]


def shy_step_model():
    # A tall ear stands up on each ear bone: 2 wide, 5 high, and 1 deep,
    # dark at the back and pale pink inside, with a dark tip.
    im = model_image(16, 16)

    def ear(f, x, y):
        if f in ("top",) or y == 0:
            return "#2A2218"
        if f == "front":
            return "#D8A890" if 0 < y < 4 else "#6A5844"
        return "#6A5844"
    paint_box(im, 0, 0, 2, 5, 1, ear)
    parts = [{"bone": "left_ear", "uv": [0, 0], "from": [0, -4.5, -0.5], "size": [2, 5, 1]},
             {"bone": "right_ear", "uv": [0, 0], "from": [-2, -4.5, -0.5], "size": [2, 5, 1]}]
    save_model("shy_step_ears", im, parts)


def rider_bond():
    # A green mark on the brow: a green diamond with a pale middle on the
    # top of the snout, and a green band with dark knots round the high part
    # of the snout, as a sign of a bond. The mark is also on the back of the
    # head.
    green = Mark("#30C850")
    pale = Mark("#C8FFC8")
    dark = Mark("#145A24")
    diamond = {(2, 0), (1, 1), (3, 1), (0, 2), (4, 2), (0, 3), (4, 3), (1, 4), (3, 4), (2, 5)}
    for x, y, f, col, row in pixels(SNOUT, ("top",)):
        if (col, row) in diamond:
            green.put((x, y))
        elif (col, row) in ((2, 2), (2, 3)):
            pale.put((x, y))
        elif (col, row) in ((1, 2), (3, 2), (1, 3), (3, 3), (2, 1), (2, 4)):
            dark.put((x, y))
    for x, y, f, col, row in pixels(SNOUT, ("right", "left", "front"), rows=(0, 1)):
        (dark if (col + row) % 3 == 0 else green).put((x, y))
    # The same mark on the back of the head, where the rider sees it.
    rune = {(3, 6), (2, 7), (4, 7), (1, 8), (5, 8), (2, 9), (4, 9), (3, 10), (3, 11), (2, 12), (4, 12), (3, 13)}
    for x, y, f, col, row in pixels(HIGH_NECK, ("back",), rows=range(6, 14)):
        if (col, row) in rune:
            green.put((x, y))
        elif (col, row) in ((3, 8), (3, 9)):
            pale.put((x, y))
    return [green, pale, dark]


def shield_kick():
    # A grim, low brow: a heavy, dark ridge over the eyes that goes round the
    # back of the head, with a pale top edge and deep, dark creases on the
    # cheeks behind the eyes.
    ridge = Mark("#1E1812")
    light = Mark("#B49C78")
    put_side(ridge, HIGH_NECK, [(c, 0) for c in range(7)] + [(0, 1), (1, 1), (2, 1), (0, 2), (1, 2),
                                                            (0, 3), (1, 4), (0, 5)])
    put_top(light, HIGH_NECK, [(c, 6) for c in range(7)])
    ridge.put([at(HIGH_NECK, "front", c, 0) for c in (0, 6)])
    put(ridge, HIGH_NECK, ("back",), rows=(3, 4))
    put(light, HIGH_NECK, ("back",), rows=(5,))
    return [ridge, light]


def sand_spit():
    # A wide, wet mouth: the mouth line of the snout is a wide, dark gap
    # with a wet lip, and long, wet drips hang down the throat under it.
    mouth = Mark("#1A0E0A")
    wet = Mark("#7A8C88")
    shine = Mark("#C8D8D4")
    put(mouth, SNOUT, ("right", "left", "front"), rows=(3,))
    put(mouth, SNOUT, ("right", "left", "front"), rows=(4,), cols=(1, 2, 3))
    put(wet, SNOUT, ("right", "left", "front"), rows=(2,), cols=(1, 2, 3, 4))
    for x, y, f, col, row in pixels(SNOUT, ("bottom",)):
        (wet if (col + row) % 3 == 0 else mouth).put((x, y))
    for c, length in ((0, 7), (1, 4), (2, 8), (3, 3), (4, 6), (5, 9), (6, 5)):
        for r in range(5, 5 + length):
            (shine if r == 4 + length else wet).put(at(HIGH_NECK, "front", c, r))
    return [mouth, wet, shine]


# The body.

HUMP_TOP = {(c, r) for c in range(3, 12) for r in range(8, 19)}
STUD_RING = {(c, r) for c in range(2, 13) for r in range(7, 20)
             if (c in (2, 12) or r in (7, 19))}


def fire_cloth():
    # A red cloth over the back: a red cloth lies over the back end of the
    # back and hangs down the rump and the high sides, with a hem of orange
    # and yellow flames.
    red = Mark("#B42018")
    orange = Mark("#FF7A18")
    yellow = Mark("#FFD040")
    for x, y, f, col, row in pixels(BODY, ("top",), rows=range(6)):
        (orange if row == 5 and col % 3 == 0 else red).put((x, y))

    def hem(col, row, depth):
        flame = (col % 3 == 1) and row == depth
        if row < depth or flame:
            return red if not flame else orange
        if row == depth + 1 and col % 3 == 1:
            return yellow
        if row == depth and col % 3 != 1:
            return orange
        return None
    for x, y, f, col, row in pixels(BODY, ("back",)):
        m = hem(col, row, 4)
        if m:
            m.put((x, y))
    for c in range(6):
        for r in range(7):
            m = hem(c, r, 4)
            if m:
                m.put(side(BODY, c, r))
    return [red, orange, yellow]


def saddle_wall():
    # A thick saddle blanket: a woven blanket of indigo and cream stripes
    # lies on the back round the hump, and hangs down the high sides, with a
    # thick, dark hem.
    indigo = Mark("#2E3A78")
    cream = Mark("#E8DCC0")
    rust = Mark("#A04A28")
    hem = Mark("#1A1E3C")

    def weave(n):
        return (indigo, indigo, cream, rust, cream, indigo)[n % 6]
    for x, y, f, col, row in pixels(BODY, ("top",), rows=range(6, 20)):
        if (col, row) in HUMP_TOP or (col, row) in STUD_RING:
            continue
        weave(row).put((x, y))
    for c in range(6, 20):
        for r in range(4):
            (hem if r == 3 else weave(r + 2)).put(side(BODY, c, r))
    return [indigo, cream, rust, hem]


def iron_studs():
    # Iron studs on the saddle: a ring of dark leather round the base of the
    # hump with an iron stud on every second pixel, and iron plates with
    # studs on the low part of the front and the back of the hump.
    leather = Mark("#3A2416")
    stud = Mark("#C8D0D8")
    iron = Mark("#6A727A")
    for c, r in STUD_RING:
        (stud if (c + r) % 2 == 0 else leather).put(top(BODY, c, r))
    for x, y, f, col, row in pixels(HUMP, ("front", "back"), rows=(2, 3, 4)):
        (stud if col % 2 == 0 and row == 3 else iron).put((x, y))
    return [leather, stud, iron]


def last_carry():
    # Long, torn tassels: long cream and red tassels hang down the sides
    # under the blanket, each of a different length, with torn ends.
    cream = Mark("#E0D4B4")
    red = Mark("#A82820")
    torn = Mark("#5A3A24")
    for c in range(6, 20, 2):
        length = 3 + noise100(c, 0, 7101) % 3
        for r in range(4, 4 + length):
            (red if c % 4 == 0 else cream).put(side(BODY, c, r))
        torn.put(side(BODY, c, 4 + length))
    return [cream, red, torn]


def spear_charge():
    # A red band round the body: a wide, red band round the body in front
    # of the hump, with dark edges and a pale spear point on each side.
    red = Mark("#C01818")
    edge = Mark("#4A0808")
    point = Mark("#F0E8D8")
    tip = {(21, 4), (22, 4), (21, 5), (22, 5), (21, 6), (21, 7)}
    for c in range(20, 24):
        for r in range(12):
            m = point if (c, r) in tip else edge if c in (20, 23) else red
            m.put(side(BODY, c, r))
    for x, y, f, col, row in pixels(BODY, ("top", "bottom"), rows=range(20, 24)):
        (edge if row in (20, 23) else red).put((x, y))
    return [red, edge, point]


def cracks(marks, cells, seed):
    """Put thick, cracked hide on the cells: a pale, thick tint over the hide, with short, dark cracks that wander
    and branch, and a light edge on one side of each crack. marks is (crack, mid, light)."""
    crack, mid, light = marks
    inside = {(x, y) for x, y, col, row in cells}
    lines = set()
    for x, y in sorted(inside):
        if noise100(x, y, seed) >= 6:
            continue
        cx, cy = x, y
        for step in range(3 + noise100(x, y, seed + 1) % 4):
            if (cx, cy) not in inside:
                break
            lines.add((cx, cy))
            turn = noise100(cx, cy, seed + 2 + step)
            cx += -1 if turn < 35 else 1 if turn >= 65 else 0
            cy += 1
    for x, y in inside:
        if (x, y) in lines:
            crack.put((x, y))
        elif (x - 1, y) in lines:
            light.put((x, y))
        else:
            mid.put((x, y), 200 + noise100(x, y, seed + 9) // 2)


def thick_hide():
    # Thick, cracked hide: the front end of the body, the chest, and the low
    # neck are thick plates of pale hide with dark cracks between them.
    crack = Mark("#1A120A")
    mid = Mark("#7A6448", alpha=130)
    light = Mark("#A89070")
    cells = []
    for c in range(24, 27):
        for r in range(12):
            for p in side(BODY, c, r):
                cells.append((p[0], p[1], c, r))
    cells += [(x, y, col, row) for x, y, f, col, row in pixels(BODY, ("front",))]
    cells += [(x, y, col, row) for x, y, f, col, row in pixels(BODY, ("top", "bottom"), rows=range(24, 27))]
    cells += [(x, y, col, row) for x, y, f, col, row in pixels(LOW_NECK)]
    cracks((crack, mid, light), cells, 7201)
    return [crack, mid, light]


def dried_flesh():
    # Dark, dried patches: large, dark patches of dried flesh, with red
    # brown edges, on the back end of the low sides, the rump, and the belly.
    dark = Mark("#140A06")
    edge = Mark("#8A3A22")
    crust = Mark("#C0A888")
    cells = []
    for c in range(0, 20):
        for r in range(6 if c < 6 else 10, 12):
            for p in side(BODY, c, r):
                cells.append(p)
    cells += [(x, y) for x, y, f, col, row in pixels(BODY, ("back",), rows=range(6, 12))]
    cells += [(x, y) for x, y, f, col, row in pixels(BODY, ("bottom",), rows=range(0, 20))]
    for x, y in cells:
        n = noise100(x // 2, y // 2, 7301)
        if n < 45:
            dark.put((x, y))
        elif n < 62:
            (crust if noise100(x, y, 7302) < 35 else edge).put((x, y))
    return [dark, edge, crust]


def deep_reserve():
    # Blue gray veins on the hump: a thick net of branching veins of blue
    # gray on the sides of the hump, dark where they branch.
    vein = Mark("#7A90B0")
    deep = Mark("#3A4A68")
    for c in range(11):
        for r in range(5):
            trunk = r == (c * 2 // 3 + 1) % 5 or r == (12 - c) * 2 // 3 % 5
            if trunk and (c + r) % 4 == 0:
                deep.put(side(HUMP, c, r))
            elif trunk or (c % 3 == 1 and r >= 1) or (r == 4 and c % 2 == 0):
                vein.put(side(HUMP, c, r))
    return [vein, deep]


def bone_hump():
    # Bone ridges on the hump: four pale ridges of bone go across the top of
    # the hump, each with knobs and a dark shadow behind it. A ridge also
    # goes along the top edge of the front and the back of the hump.
    bone = Mark("#E8DCC4")
    knob = Mark("#FFF8E8")
    shade = Mark("#3A2E22")
    for x, y, f, col, row in pixels(HUMP, ("top",)):
        if row in (1, 4, 7, 10):
            (knob if col % 3 == 1 else bone).put((x, y))
        elif row in (2, 5, 8):
            shade.put((x, y))
    for x, y, f, col, row in pixels(HUMP, ("front", "back"), rows=(0, 1)):
        (knob if col % 3 == 1 and row == 0 else bone if row == 0 else shade).put((x, y))
    return [bone, knob, shade]


def sand_veil():
    # Sandy, faded skin: the body, the hump, and the necks fade and turn a
    # pale sand color. The layers go under the marks of the other genes.
    sand = Mark("#E0C898", alpha=105, order=-1, regions=["torso", "spine", "scalp"])
    for part in (BODY, HUMP, LOW_NECK, HIGH_NECK):
        for x, y, f, col, row in pixels(part):
            sand.put((x, y), 200 + noise100(x, y, 7401) // 2)
    return [{"regions": ["torso", "spine", "scalp"], "effect": {"type": "desaturate", "amount": 0.45},
             "order": -2}, sand]


def water_hump():
    # The swollen hump is the gene model water_hump_hump.
    return []


def water_hump_model():
    # A large, swollen hump: a copy of the hump, with the texture of the mob
    # and its marks, 2 pixels larger on each side and 1.5 pixels higher. On
    # the bone body, the hump is from x -4.5 to 4.5, y -17 to -12, and z
    # -15.5 to -4.5.
    parts = [{"bone": "body", "uv": [74, 0], "from": [-4.5, -18.5, -15.5], "size": [9, 5, 11], "inflate": 2.0}]
    save_mob_model("water_hump_hump", parts)


def high_seat():
    # The saddle frame is the gene model high_seat_frame.
    return []


def high_seat_model():
    # A tall saddle frame over the hump: a tall front post and a back post
    # of dark wood with brass caps, and two rails high between them, out of
    # the way of the hump. The hump top is at y -17 on the bone body, from
    # x -4.5 to 4.5 and z -15.5 to -4.5.
    im = model_image(64, 32)

    def wood(f, x, y):
        if y == 0 or f == "top":
            return "#E0B040"
        return "#6A3E1E" if (x + y) % 4 else "#4A2A12"

    def rail(f, x, y):
        return "#8A5228" if (x + y) % 3 else "#5A3216"
    paint_box(im, 0, 0, 7, 10, 1, wood)
    paint_box(im, 0, 12, 7, 8, 1, wood)
    paint_box(im, 20, 0, 1, 1, 11, rail)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-3.5, -27, -16.5], "size": [7, 10, 1]},
             {"bone": "body", "uv": [0, 12], "from": [-3.5, -25, -4.5], "size": [7, 8, 1]},
             {"bone": "body", "uv": [20, 0], "from": [-5.5, -24, -16], "size": [1, 1, 11]},
             {"bone": "body", "uv": [20, 0], "from": [4.5, -24, -16], "size": [1, 1, 11]}]
    save_model("high_seat_frame", im, parts)


# The legs.

def sand_shove():
    # Thick, dusty legs: the thick legs are the gene model sand_shove_legs.
    # Pale dust and dark lines of muscle lie on the high legs.
    dust = Mark("#D8C098")
    muscle = Mark("#2E2218")
    leg_put(muscle, all_sides, range(0, 3), lambda x, y, f, c, r, n: c in (0, 4))
    leg_put(dust, all_sides, range(0, 3), lambda x, y, f, c, r, n: c not in (0, 4))
    return [dust, muscle]


def sand_shove_model():
    # Thick legs: a copy of each leg, with the texture of the mob and its
    # marks, 0.6 pixel larger on each side.
    parts = [{"bone": leg, "uv": list(uv), "from": [-2.5, 2, -2.5], "size": [5, 21, 5], "inflate": 0.6}
             for leg, uv in (("left_hind_leg", (58, 16)), ("right_hind_leg", (94, 16)),
                             ("left_front_leg", (0, 0)), ("right_front_leg", (0, 26)))]
    save_mob_model("sand_shove_legs", parts)


def escape_run():
    # Torn wraps on the legs: a band of gray cloth round each high leg, with
    # torn, ragged ends and holes.
    cloth = Mark("#B8B0A0")
    dark = Mark("#5A5448")
    leg_put(dark, all_sides, (3,))
    leg_put(cloth, all_sides, (4,), lambda x, y, f, c, r, n: noise100(x, y, 8201) < 85)
    leg_put(cloth, all_sides, (5,), lambda x, y, f, c, r, n: c % 2 == n % 2)
    leg_put(dark, all_sides, (5,), lambda x, y, f, c, r, n: c % 2 != n % 2 and noise100(x, y, 8202) < 40)
    return [cloth, dark]


def stubborn_hooves():
    # Thick, knotted knees: each knee is a dark, thick knot with pale lumps.
    knot = Mark("#2A1C12")
    lump = Mark("#B89C78")
    leg_put(knot, all_sides, (6, 8))
    leg_put(lump, all_sides, (7,), lambda x, y, f, c, r, n: c % 2 == 0)
    leg_put(knot, all_sides, (7,), lambda x, y, f, c, r, n: c % 2 == 1)
    return [knot, lump]


def golem_kick():
    # Iron gray shins: a plate of iron gray on each shin, with a pale top
    # edge and dark rivets.
    iron = Mark("#8A9098")
    light = Mark("#D0D6DC")
    rivet = Mark("#2A2E34")
    leg_put(light, all_sides, (9,))
    leg_put(rivet, all_sides, (10, 11), lambda x, y, f, c, r, n: c == 2 and r == 10)
    leg_put(iron, all_sides, (10, 11), lambda x, y, f, c, r, n: not (c == 2 and r == 10))
    return [iron, light, rivet]


def sand_leap():
    # Pale wraps round the ankles: two bands of pale linen cross round each
    # ankle.
    linen = Mark("#F0E8D4")
    fold = Mark("#B8AC90")
    leg_put(linen, all_sides, (12, 13), lambda x, y, f, c, r, n: (c + r) % 3 != 0)
    leg_put(fold, all_sides, (12, 13), lambda x, y, f, c, r, n: (c + r) % 3 == 0)
    return [linen, fold]


def dust_dash():
    # Dusty, brown feet: a cloud of brown dust over the feet, thick at the
    # bottom and thin at the top.
    brown = Mark("#8A5A2E")
    light = Mark("#C08A50")
    leg_put(brown, all_sides, (15,), lambda x, y, f, c, r, n: noise100(x, y, 8601) < 85)
    leg_put(light, all_sides, (14,), lambda x, y, f, c, r, n: noise100(x, y, 8602) < 70)
    leg_put(brown, all_sides, (14,), lambda x, y, f, c, r, n: noise100(x, y, 8602) >= 85)
    return [brown, light]


def sand_spray():
    # Sand caked on the feet: thick clumps of yellow sand on the front and
    # the outer side of each foot.
    sand = Mark("#E8C860")
    clump = Mark("#B89430")
    leg_put(sand, front_outer, (16, 17), lambda x, y, f, c, r, n: noise100(x, y, 8701) < 70)
    leg_put(clump, front_outer, (16, 17), lambda x, y, f, c, r, n: noise100(x, y, 8701) >= 70)
    return [sand, clump]


def hard_kick():
    # Dark, hard hooves: the hooves are black, with a gray shine on the front
    # and a pale split between the two toes.
    black = Mark("#0E0A08")
    shine = Mark("#9A9CA8")
    split = Mark("#C8B8A0")
    leg_put(black, front_outer, (18, 19, 20), lambda x, y, f, c, r, n: not (f == "front" and (c == 2 or
                                                                                         (c in (1, 3) and r == 18))
                                                                    or f != "front" and c in (1, 2) and r - 17 == c))
    leg_put(split, front_outer, (18, 19, 20), lambda x, y, f, c, r, n: f == "front" and c == 2)
    leg_put(shine, front_outer, (18, 19), lambda x, y, f, c, r, n: f != "front" and c in (1, 2) and r - 17 == c)
    leg_put(shine, front_outer, (18,), lambda x, y, f, c, r, n: f == "front" and c in (1, 3))
    for leg, where in LEGS:
        put(black, leg, ("bottom",))
    return [black, shine, split]


def swift_stride():
    # Wide, pale foot pads: the back and the inner side of each foot are a
    # wide, pale pad, with fine folds.
    pad = Mark("#F0DEC0")
    fold = Mark("#B89E80")
    leg_put(pad, back_inner, range(16, 21), lambda x, y, f, c, r, n: (r - c) % 3 != 0 or r == 16)
    leg_put(fold, back_inner, range(17, 21), lambda x, y, f, c, r, n: (r - c) % 3 == 0)
    return [pad, fold]


def sure_feet():
    # The hard, wide feet are the gene model sure_feet_soles.
    return []


def sure_feet_model():
    # Hard, wide feet: a thin, hard sole of gray horn, 7 wide and 7 deep,
    # round the low half of the low row of each foot, with dark cracks. On
    # each leg bone, the leg is from x -2.5 to 2.5, y 2 to 23, and z -2.5 to
    # 2.5. The sole goes 0.5 pixel under the foot, into the ground.
    im = model_image(32, 16)

    def horn(f, x, y):
        if f == "top":
            return "#8A8478" if x in (0, 6) or y in (0, 6) else None
        return "#3A3630" if (x * 3 + y) % 5 == 0 else "#7A7468"
    paint_box(im, 0, 0, 7, 1, 7, horn)
    parts = [{"bone": leg, "uv": [0, 0], "from": [-3.5, 22.5, -3.5], "size": [7, 1, 7]}
             for leg in ("left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg")]
    save_model("sure_feet_soles", im, parts)


def long_dash():
    # The long, lean legs are the gene model long_dash_legs.
    return []


def long_dash_model():
    # Long, lean legs: a lean thigh of 2x11x2 goes up from the top of each
    # leg along the flank, so that each leg looks long. The thigh is dark,
    # with a pale tendon down its outer side. It stands 1.5 pixels out from
    # the side of the body. On each leg bone, the leg is from x -2.5 to 2.5
    # and from y 2 to 23. The body side is at x 7.5 on the model, and the
    # legs are at x 4.9 and -4.9. The thighs of the front legs sit at the
    # front edge of the leg, and the thighs of the hind legs at the back
    # edge, so that the band of Spear Charge stays clear.
    im = model_image(32, 32)

    def thigh(f, x, y):
        if f in ("top", "bottom"):
            return "#4A3A2A"
        if f in ("right", "left") and x == 1:
            return "#C8B494"
        return "#3A2C1E" if (x + y) % 5 else "#5A4836"
    paint_box(im, 0, 0, 2, 11, 2, thigh)
    parts = []
    for leg, sign, z in (("left_hind_leg", 1, 0.5), ("right_hind_leg", -1, 0.5), ("left_front_leg", 1, -2.5),
                         ("right_front_leg", -1, -2.5)):
        parts.append({"bone": leg, "uv": [0, 0], "from": [2.1 if sign > 0 else -4.1, -9, z],
                      "size": [2, 11, 2]})
    save_model("long_dash_legs", im, parts)


def save_mob_model(name, parts):
    import json
    import os
    from make_husk_marks import MODEL_DIR
    model = {"mob_texture": True, "texture_width": 128, "texture_height": 128, "parts": parts}
    with open(os.path.join(MODEL_DIR, name + ".json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


GENES = {
    "long_dash": long_dash, "ram_dash": ram_dash, "quick_dash": quick_dash, "spear_charge": spear_charge,
    "dust_dash": dust_dash, "sand_leap": sand_leap,
    "water_hump": water_hump, "thick_hide": thick_hide, "dried_flesh": dried_flesh,
    "deep_reserve": deep_reserve, "bone_hump": bone_hump, "stubborn_hooves": stubborn_hooves,
    "swift_stride": swift_stride, "sand_veil": sand_veil, "sure_feet": sure_feet, "night_trek": night_trek,
    "shy_step": shy_step, "escape_run": escape_run,
    "saddle_wall": saddle_wall, "high_seat": high_seat, "rider_bond": rider_bond, "fire_cloth": fire_cloth,
    "last_carry": last_carry, "iron_studs": iron_studs,
    "hard_kick": hard_kick, "sand_spray": sand_spray, "golem_kick": golem_kick, "shield_kick": shield_kick,
    "sand_spit": sand_spit, "sand_shove": sand_shove,
}

MODEL_GENES = {
    "long_dash": (long_dash_model, "long_dash_legs"),
    "water_hump": (water_hump_model, "water_hump_hump"),
    "high_seat": (high_seat_model, "high_seat_frame"),
    "sure_feet": (sure_feet_model, "sure_feet_soles"),
    "night_trek": (night_trek_model, "night_trek_eyes"),
    "shy_step": (shy_step_model, "shy_step_ears"),
    "sand_shove": (sand_shove_model, "sand_shove_legs"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="camel husk genes")
