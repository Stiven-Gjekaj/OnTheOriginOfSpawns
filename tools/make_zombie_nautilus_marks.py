"""Make the gene visuals of the zombie nautilus, from design/species/zombie_nautilus.md.

Usage: python3 tools/make_zombie_nautilus_marks.py (from the root of the repository)

The two textures of the zombie nautilus are 128x128, with the same layout, so
each mark goes on both. The marks use the zombie nautilus region map. The
parts, from NautilusModel:
- the shell: the high cube 14x10x16 at uv (0, 0), and the low cube 14x8x20
  at uv (0, 26). The low cube is 4 pixels longer, at the +z end. Its top and
  its +z end are clear.
- the body, 10x8x14 at uv (0, 54). The eyes are in row 2 of its sides, in
  column 8 from the +z end.
- the mouth: upper_mouth (54, 54), lower_mouth (54, 62), and inner_mouth
  (54, 70). Their texture is a ring of short tentacles.
The mouth is at the +z end, and the coil of the shell is at the -z end. The
coil end is the front of the zombie nautilus as it swims.

On a side face, the script counts the columns from the +z end, the end of
the mouth. The helper side() puts the same column on the right side and on
the left side. Column c of the sides of the high cube is at the same z as
column c + 4 of the sides of the low cube. On the top, row r is at the same z
as column r of the high sides.

Each gene keeps to its own zone, so that the marks stack:
- The shell. Trident Shell has two steel bands round the shell, Trident
  Guard has one silver ring in the middle. The other shell marks leave the
  bands clear. Sun Shell has the top, and Sealed Shell has a scar across the
  top. Coral Crust has the high rows of the high sides, and Layered Shell the
  low rows. Barnacle Hide has the high rows of the low sides, and Tide Shield
  the low rows. Hull Ram has the rim of the opening at the +z end. Shield
  Ram has the +z end of the high cube. Spiral Rest has the coil end of the
  high cube, and Ink Veil the coil end of the low cube and the bottom. Deep
  Ram (a sharp prow) and Shell Wall (a high rim) are gene models.
- The body. Quick Ram has a slit in the eye row, and Thick Ink a ring of ink
  round it. Dodge Jet has large eyes, a gene model. Shore Crawl has the low
  rows of the sides near the mouth, and Ink Screen the low rows near the
  shell. Sour Ink has the bottom, and Kin Current a mark in the middle of the
  bottom and on the flat face.
- The tentacles. The vanilla tentacles have few pixels, so each tentacle
  gene but Deep Escape is a gene model: a pair of tentacles in its own place
  round the mouth. Deep Escape tears the vanilla tentacles.
The gene models use the bones shell, body, upper_mouth, inner_mouth, and
lower_mouth.
This script needs Pillow.
"""
import os

from PIL import Image

from make_gene_marks import MARK_DIR, Mark as TextureMark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/zombie_nautilus.png"


def Mark(tint, **kw):
    # The textures of the zombie nautilus are 128x128.
    return TextureMark(tint, size=128, height=128, **kw)


HIGH = box_faces(0, 0, 14, 10, 16)
LOW = box_faces(0, 26, 14, 8, 20)
BODY = box_faces(0, 54, 10, 8, 14)
FACE = box_faces(0, 76, 10, 8, 0)
UPPER_MOUTH = box_faces(54, 54, 10, 4, 4)
LOWER_MOUTH = box_faces(54, 62, 10, 4, 4)
INNER_MOUTH = box_faces(54, 70, 6, 4, 4)
MOUTHS = (UPPER_MOUTH, LOWER_MOUTH, INNER_MOUTH)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES

# The columns of the high sides under the bands of Trident Shell and the
# ring of Trident Guard. The columns of the low sides are 4 more.
STEEL_COLS = (3, 4, 11, 12)
SILVER_COLS = (7, 8)
BAND_COLS = STEEL_COLS + SILVER_COLS


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

    The column counts from the +z end of the part (the end of the mouth).
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


# The cells of the high sides under the scar of Sealed Shell: a line, 2
# pixels wide, from the top at the +z end down to the low edge at the coil
# end.
SIDE_SCAR = {(c, r) for r in range(10) for c in (r * 14 // 9 + 1, r * 14 // 9 + 2) if c not in BAND_COLS}


def high_side_cells(rows):
    """Yield (col, row) of the high sides in the rows, but not under a band or the scar."""
    for c in range(16):
        if c in BAND_COLS:
            continue
        for r in rows:
            if (c, r) not in SIDE_SCAR:
                yield c, r


def low_side_cells(rows, first=3):
    """Yield (col, row) of the low sides in the rows, from the column first, but not under a band."""
    for c in range(first, 20):
        if c - 4 in BAND_COLS:
            continue
        for r in rows:
            yield c, r


def top_cells():
    """Yield (x, y, col, row) of the top of the shell, but not under a band."""
    for x, y, f, col, row in pixels(HIGH, ("top",)):
        if row not in BAND_COLS:
            yield x, y, col, row


# The shell.

def band(mark, edge, cols, light=None):
    """Put a band round the shell: the columns of the high sides, the same z on the low sides and the top."""
    for c in cols:
        first, last = c == cols[0], c == cols[-1]
        for r in range(10):
            (edge if r == 0 else mark).put(side(HIGH, c, r))
        for r in range(8):
            (edge if r == 7 else mark).put(side(LOW, c + 4, r))
        for x, y, f, col, row in pixels(HIGH, ("top",), rows=(c,)):
            (light if light and (first or last) and col % 3 == 1 else mark).put((x, y))


def trident_shell():
    # Steel bands round the shell: two dark steel bands go round the shell,
    # with a pale edge and bright rivets on the top.
    steel = Mark("#5A646C")
    edge = Mark("#B8C4CC")
    rivet = Mark("#E8F0F4")
    band(steel, edge, (3, 4), rivet)
    band(steel, edge, (11, 12), rivet)
    return [steel, edge, rivet]


def trident_guard():
    # A silver ring: one bright silver ring goes round the middle of the
    # shell, with a blue gem on the top and on each side.
    silver = Mark("#E0E8EE")
    shade = Mark("#98A4AC")
    gem = Mark("#2A70E0")
    band(silver, shade, (7, 8))
    gem.put([at(HIGH, "top", c, r) for c in (6, 7) for r in (7, 8)])
    put_side(gem, HIGH, [(7, 4), (8, 4), (7, 5), (8, 5)])
    return [silver, shade, gem]


def sun_shell():
    # A dark, sun-baked shell: the top of the shell is dark brown, baked by
    # the sun, with pale cracks.
    baked = Mark("#4A3220")
    crack = Mark("#C8A880")
    scar = scar_cells()
    for x, y, col, row in top_cells():
        if (col, row) in scar:
            continue
        if (col * 2 + row) % 7 == 0 and noise100(col, row, 9101) < 70:
            crack.put((x, y))
        else:
            baked.put((x, y), 210 + noise100(x, y, 9102) // 3)
    return [baked, crack]


def scar_cells():
    """Return the (col, row) cells of the top under the scar of Sealed Shell."""
    cells = set()
    for row in range(16):
        col = 1 + row * 12 // 15
        for c in (col, col + 1):
            if row not in BAND_COLS:
                cells.add((c, row))
    return cells


def sealed_shell():
    # A bright scar across the shell: a bright, pale gold scar goes across
    # the top from corner to corner, and down each high side, with stitches
    # of dark thread.
    scar = Mark("#FFE8A0")
    stitch = Mark("#3A2A18")
    for c, r in scar_cells():
        (stitch if r % 3 == 0 and c == 1 + r * 12 // 15 else scar).put(at(HIGH, "top", c, r))
    for c, r in SIDE_SCAR:
        (stitch if r % 3 == 1 and c == r * 14 // 9 + 1 else scar).put(side(HIGH, c, r))
    return [scar, stitch]


def coral_crust():
    # Pink coral patches: round patches of pink coral, with pale tips, on the
    # high rows of the high sides.
    pink = Mark("#F070A0")
    pale = Mark("#FFC8DC")
    for c, r in high_side_cells(range(0, 5)):
        n = noise100(c // 2, r // 2, 9301)
        if n < 55:
            (pale if noise100(c, r, 9302) < 25 else pink).put(side(HIGH, c, r))
    return [pink, pale]


def layered_shell():
    # Thick, layered ridges: thick ridges go along the low rows of the high
    # sides, each with a light top and a dark shadow.
    light = Mark("#F0E8D0")
    dark = Mark("#6A5A40")
    for c, r in high_side_cells(range(5, 10)):
        if r in (5, 8):
            light.put(side(HIGH, c, r))
        elif r in (6, 9):
            dark.put(side(HIGH, c, r))
    return [light, dark]


def barnacle_hide():
    # Rows of barnacles: two rows of gray barnacles, each a pale ring with a
    # dark middle, on the high rows of the low sides.
    shell = Mark("#C8C4B8")
    hole = Mark("#2A2A28")
    for c, r in low_side_cells(range(0, 4)):
        # Each barnacle is 2x2: a pale ring with a dark hole at its low
        # corner.
        bc, br = c % 3, r % 2
        if bc == 2:
            continue
        (hole if (bc, br) == (1, 1) else shell).put(side(LOW, c, r))
    return [shell, hole]


def tide_shield():
    # A broad, flat shell edge: the low edge of the shell is a broad, flat,
    # pale band, with fine lines along it.
    flat = Mark("#E8E0C8")
    line = Mark("#9A9078")
    for c, r in low_side_cells(range(4, 8)):
        (line if r in (5, 7) else flat).put(side(LOW, c, r))
    return [flat, line]


def hull_ram():
    # A dark red rim: the rim of the opening, at the mouth end of the low
    # cube, is dark red, and a dark red line goes along the low edge of the
    # +z end of the high cube.
    red = Mark("#7A1010")
    dark = Mark("#3A0606")
    for c in range(0, 3):
        for r in range(8):
            (dark if c == 2 else red).put(side(LOW, c, r))
    put(red, HIGH, ("back",), rows=(8,))
    put(dark, HIGH, ("back",), rows=(9,))
    return [red, dark]


def shield_ram():
    # Gray scrape marks: three long, gray scrapes cross the +z end of the
    # high cube at a slant, each with a pale line along its top.
    scrape = Mark("#6A6A68")
    pale = Mark("#E0E0D8")
    for start in (1, 6, 11):
        for col in range(14):
            row = start + col // 3 - 2
            if 0 <= row < 8 and 1 <= col <= 12:
                pale.put(at(HIGH, "back", col, row))
                if row + 1 < 8:
                    scrape.put(at(HIGH, "back", col, row + 1))
    return [scrape, pale]


def spiral_rest():
    # A pale spiral line: a pale line winds out from the middle of the coil
    # end of the high cube, round three times, with a gap of 1 pixel between
    # the turns, and a soft shadow under the line.
    pale = Mark("#FFF8E0")
    shade = Mark("#A89878")
    # The coil end is 14 wide and 10 high. The spiral starts at (6, 5) and
    # goes right, down, left, and up, each leg 2 pixels longer than the last
    # two.
    c, r = 6, 5
    path = [(c, r)]
    steps = ((1, 0), (0, 1), (-1, 0), (0, -1))
    length, turn = 1, 0
    while len(path) < 80:
        for _ in range(2):
            dc, dr = steps[turn % 4]
            for _ in range(length * 2):
                c, r = c + dc, r + dr
                if 0 <= c < 14 and 0 <= r < 10:
                    path.append((c, r))
            turn += 1
        length += 1
        if length > 7:
            break
    cells = set(path)
    for c, r in cells:
        pale.put(at(HIGH, "front", c, r))
    for c, r in cells:
        if (c, r + 1) not in cells and r + 1 < 10:
            shade.put(at(HIGH, "front", c, r + 1))
    return [pale, shade]


def ink_veil():
    # Black ink stains: large stains of black ink on the coil end of the low
    # cube, with drips that run down, and on the bottom of the shell.
    ink = Mark("#0A0A10")
    sheen = Mark("#3A3A58")
    for x, y, f, col, row in pixels(LOW, ("front",)):
        n = noise100(col // 2, row // 2, 9601)
        if n < 55 or (col % 3 == 0 and row >= 4):
            (sheen if noise100(x, y, 9602) < 15 else ink).put((x, y))
    for x, y, f, col, row in pixels(LOW, ("bottom",)):
        if noise100(col // 2, row // 2, 9603) < 45:
            ink.put((x, y))
    return [ink, sheen]


def deep_ram():
    # The sharp front edge is the gene model deep_ram_prow.
    return []


def deep_ram_model():
    # A sharp, pointed front edge: a prow of pale bone grows from the high
    # part of the coil end, and narrows to a point that juts forward. On the
    # bone shell, the high cube is from x -7 to 7, y -10 to 0, and z -7 to
    # 9. The coil end is at z -7.
    im = model_image(64, 32)

    def bone(f, x, y):
        return "#F4ECD8" if f == "top" or y == 0 else "#B8AC90" if (x + y) % 3 else "#8A7E66"
    paint_box(im, 0, 0, 10, 4, 2, bone)
    paint_box(im, 0, 8, 6, 3, 3, bone)
    paint_box(im, 0, 16, 2, 2, 3, bone)
    parts = [{"bone": "shell", "uv": [0, 0], "from": [-5, -11, -9], "size": [10, 4, 2]},
             {"bone": "shell", "uv": [0, 8], "from": [-3, -10.5, -12], "size": [6, 3, 3]},
             {"bone": "shell", "uv": [0, 16], "from": [-1, -10, -15], "size": [2, 2, 3]}]
    save_model("deep_ram_prow", im, parts)


def shell_wall():
    # The high shell rim is the gene model shell_wall_rim.
    return []


def shell_wall_model():
    # A high shell rim: a wall of shell stands along the two long edges of
    # the top of the shell, just out of the top, so that the top stays clear.
    # It is 2 pixels high at the +z end and the coil end, and 1 pixel high in
    # the middle. It is pale, with dark ridges. On the bone shell, the top is
    # at y -10, from x -7 to 7 and z -7 to 9.
    im = model_image(64, 32)

    def rim(f, x, y):
        return "#E8DCC0" if (x + y) % 4 else "#9A8A6A"
    paint_box(im, 0, 0, 1, 2, 4, rim)
    paint_box(im, 0, 10, 1, 1, 8, rim)
    parts = []
    for x, mirror in ((-8, False), (7, True)):
        parts += [{"bone": "shell", "uv": [0, 0], "from": [x, -12, -7], "size": [1, 2, 4], "mirror": mirror},
                  {"bone": "shell", "uv": [0, 10], "from": [x, -11, -3], "size": [1, 1, 8], "mirror": mirror},
                  {"bone": "shell", "uv": [0, 0], "from": [x, -12, 5], "size": [1, 2, 4], "mirror": mirror}]
    save_model("shell_wall_rim", im, parts)


# The body.

EYE_COL, EYE_ROW = 8, 2


def quick_ram():
    # Narrow, pale eyes: each eye is white, in a long, narrow, pale slit
    # that goes along the body, with a fork at the end near the mouth, and
    # a pale line along the top edge of the body near the mouth.
    pale = Mark("#E8F0E0")
    white = Mark("#FFFFFF")
    put_side(pale, BODY, [(c, EYE_ROW) for c in range(3, 13) if c not in (5, EYE_COL)]
             + [(3, 1), (4, 1), (3, 3), (4, 3), (12, 1), (12, 3)] + [(c, 0) for c in range(3, 8)])
    put_side(white, BODY, [(EYE_COL, EYE_ROW), (5, EYE_ROW)])
    return [pale, white]


def thick_ink():
    # Black ink round the eyes: thick bands of black ink over and under each
    # eye, and along the top edge of the body near the shell.
    ink = Mark("#06060A")
    put_side(ink, BODY, [(c, r) for c in range(5, 12) for r in (1, 3)])
    put_side(ink, BODY, [(c, 0) for c in range(8, 13)])
    return [ink]


def dodge_jet():
    # The wide, alert eyes are the gene model dodge_jet_eyes.
    return []


def dodge_jet_model():
    # Wide, alert eyes: a large eye of 3x3 bulges 1 pixel out of each
    # side of the body, white with a black pupil, over the vanilla eye. On the bone body, the
    # sides are at x -5 and 5, and the eye is at y -2 and z 2.5.
    im = model_image(16, 16)

    def eye(f, x, y):
        if f in ("right", "left") and (x, y) == (1, 1):
            return "#000000"
        return "#F4FFFF" if f in ("right", "left") else "#B8D0D0"
    paint_box(im, 0, 0, 1, 3, 3, eye)
    parts = [{"bone": "body", "uv": [0, 0], "from": [5, -3.5, 1], "size": [1, 3, 3]},
             {"bone": "body", "uv": [0, 0], "from": [-6, -3.5, 1], "size": [1, 3, 3], "mirror": True}]
    save_model("dodge_jet_eyes", im, parts)


def shore_crawl():
    # Wet, dark skin: the low rows of the body near the mouth are dark and
    # wet, with bright, wet shines.
    wet = Mark("#0E1A1E")
    shine = Mark("#90C8D0")
    for c in range(3, 8):
        for r in range(4, 8):
            (shine if (c + r) % 4 == 0 else wet).put(side(BODY, c, r))
    return [wet, shine]


def ink_screen():
    # Dark, smoky patches: soft, dark gray patches of smoke on the low rows
    # of the body near the shell, on the top of the body behind the green
    # mark, and on the flat face round its middle.
    smoke = Mark("#2A2A30")
    pale = Mark("#7A7A88")
    for c in range(8, 13):
        for r in range(4, 8):
            (pale if noise100(c, r, 9901) < 30 else smoke).put(side(BODY, c, r))
    for x, y, f, col, row in pixels(BODY, ("top",), cols=range(3, 7), rows=range(9, 12)):
        (pale if noise100(x, y, 9903) < 30 else smoke).put((x, y))
    for x, y, f, col, row in pixels(FACE, ("front",)):
        if not (3 <= col <= 6 and 2 <= row <= 5) and noise100(x, y, 9902) < 60:
            smoke.put((x, y))
    return [smoke, pale]


def sour_ink():
    # Purple spots: round purple spots, each with a pale middle, on the top
    # of the body at the sides of the green mark, and on the bottom.
    purple = Mark("#8A2AC8")
    pale = Mark("#E0A8FF")
    for x, y, f, col, row in pixels(BODY, ("bottom",)):
        if 3 <= col <= 6 and 4 <= row <= 9:
            continue
        k = (col + 2 * row) % 5
        if k == 0:
            pale.put((x, y))
        elif k in (1, 4) and row % 2 == 0:
            purple.put((x, y))
    for x, y, f, col, row in pixels(BODY, ("top",), rows=range(3, 12)):
        if 3 <= col <= 6:
            continue
        k = (col + 2 * row) % 4
        if k == 0:
            pale.put((x, y))
        elif k in (1, 3):
            purple.put((x, y))
    return [purple, pale]


def kin_current():
    # A green mark between the eyes: a green mark in the shape of a wave on
    # the top of the body, between the eyes, where the shell does not cover
    # it, and in the middle of the bottom and of the flat face.
    green = Mark("#30D060")
    pale = Mark("#C8FFD8")
    wave = {(3, 3), (4, 2), (5, 3), (6, 2), (3, 4), (6, 4), (4, 5), (5, 5)}
    for x, y, f, col, row in pixels(FACE, ("front",), cols=range(3, 7), rows=range(2, 6)):
        (green if (col, row) in wave else pale).put((x, y))
    for x, y, f, col, row in pixels(BODY, ("bottom",), cols=range(3, 7), rows=range(4, 10)):
        (green if (col + row) % 2 == 0 else pale).put((x, y))
    top_wave = {(3, 4), (4, 3), (5, 4), (6, 3), (3, 5), (6, 5), (4, 6), (5, 6), (4, 7), (5, 7)}
    for x, y, f, col, row in pixels(BODY, ("top",), cols=range(3, 7), rows=range(3, 9)):
        (green if (col, row) in top_wave else pale).put((x, y))
    return [green, pale]


# The tentacles.

def deep_escape():
    # Torn tentacles: the ends of the vanilla tentacles are torn away (erased
    # with a mask), and the torn ends that are left are pale.
    mask = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    pale = Mark("#D8C8B0")
    for part in MOUTHS:
        for x, y, f, col, row in pixels(part):
            n = noise100(x, y, 9701)
            if n < 35:
                mask.putpixel((x, y), (255, 255, 255, 255))
            elif n < 65:
                pale.put((x, y))
    name = "deep_escape_mask"
    mask.save(os.path.join(MARK_DIR, name + ".png"))
    torn = {"regions": ["arms"], "effect": {"type": "erase", "texture": f"originofspawns:textures/gene_marks/{name}.png"}}
    return [pale, torn]


TENTACLE_BASE = ("#2A3A24", "#1E2A1A")


def tentacle_pair(name, bone, x, y, length, tip, ring=None, width=1, stripes=None, droop=75, splay=10):
    """Save a gene model: two tentacles at x and -x, at the height y of the bone, from the front of the mouth.

    tip is (color, length of the tip). ring is (color, step). stripes is a color for lines along the tentacle.
    Each tentacle starts 2 pixels in the mouth. It turns down from the line of the body by 90 - droop
    degrees, and out to its side by splay degrees.
    """
    im = model_image(64, 32)

    def color(f, cx, cy):
        # cy counts along the tentacle on the side faces, from the mouth.
        along = cy if f not in ("top", "bottom") else cy
        if along >= length - tip[1]:
            return tip[0]
        if ring and along % ring[1] == ring[1] - 1:
            return ring[0]
        if stripes and cx % 2 == 0:
            return stripes
        return TENTACLE_BASE[(cx + along) % 2]
    paint_box(im, 0, 0, width, length, 1, lambda f, cx, cy: color(f, cx, cy))
    # The tentacle is a box of width x length x 1. Its "length" is its height
    # in the box, so each tentacle turns by about 90 degrees round x to
    # point back, along +z.
    parts = [{"bone": bone, "uv": [0, 0], "from": [sx, y, 2], "size": [width, length, 1], "pivot": [sx, y, 2],
              "rotation": [droop, splay if sx > 0 else -splay, 0]} for sx in (x, -x - width)]
    save_model(name, im, parts)


def tide_charge():
    # The long, thin tentacles are the gene model tide_charge_tentacles.
    return []


def tide_charge_model():
    # Long, thin tentacles: two long, thin, pale tentacles at the high
    # corners of the mouth.
    tentacle_pair("tide_charge_tentacles", "upper_mouth", 3.5, -1.5, 11, ("#C8D8B8", 3), droop=80, splay=15)


def jet_fin():
    # The wide, flat tentacles are the gene model jet_fin_fins.
    return []


def jet_fin_model():
    # Wide, flat tentacles: two wide, flat tentacles, as fins, at the sides
    # of the mouth, light at the edges.
    im = model_image(64, 32)

    def fin(f, cx, cy):
        return "#6A8A5A" if cx in (0, 3) or cy == 0 else "#3A5A30" if (cx + cy) % 3 else "#4A6A3A"
    paint_box(im, 0, 0, 4, 1, 7, fin)
    parts = [{"bone": "upper_mouth", "uv": [0, 0], "from": [5, 1.5, 3], "size": [4, 1, 7]},
             {"bone": "upper_mouth", "uv": [0, 0], "from": [-9, 1.5, 3], "size": [4, 1, 7], "mirror": True}]
    save_model("jet_fin_fins", im, parts)


def breath_ram():
    return []


def breath_ram_model():
    # Blue tips: two tentacles at the low corners of the mouth, with long,
    # bright blue tips.
    tentacle_pair("breath_ram_tentacles", "lower_mouth", 3.5, 1, 7, ("#30A0FF", 3), width=2, droop=60)


def last_current():
    return []


def last_current_model():
    # Pale tips: two tentacles in the middle of the top of the mouth, with
    # pale, white tips.
    tentacle_pair("last_current_tentacles", "upper_mouth", 1, -1.5, 6, ("#F0F4F0", 2), width=2, droop=95)


def wide_ink():
    return []


def wide_ink_model():
    # Black tips: two tentacles in the middle of the bottom of the mouth,
    # with black tips.
    tentacle_pair("wide_ink_tentacles", "lower_mouth", 1, 1.5, 6, ("#050508", 2), width=2, droop=55, splay=0)


def quick_ink():
    return []


def quick_ink_model():
    # Purple rings: two tentacles at the sides of the low part of the mouth,
    # with purple rings along them.
    tentacle_pair("quick_ink_tentacles", "lower_mouth", 4, -1, 6, ("#8A2AC8", 1), ring=("#A040E0", 2), width=2,
                  splay=30)


def wake_jet():
    return []


def wake_jet_model():
    # White wake marks: two tentacles at the sides of the high part of the
    # mouth, with white lines along them, as a wake.
    tentacle_pair("wake_jet_tentacles", "upper_mouth", 4, 0, 7, ("#FFFFFF", 1), width=2, stripes="#F0F8FF", droop=85,
                  splay=25)


def ink_rush():
    return []


def ink_rush_model():
    # Streaked tentacles: two tentacles in the middle of the mouth, with dark
    # streaks along them.
    tentacle_pair("ink_rush_tentacles", "inner_mouth", 1, 0, 6, ("#1A1A22", 1), width=2, stripes="#101018", droop=70,
                  splay=5)


GENES = {
    "deep_ram": deep_ram, "quick_ram": quick_ram, "hull_ram": hull_ram, "shield_ram": shield_ram,
    "breath_ram": breath_ram, "tide_charge": tide_charge,
    "layered_shell": layered_shell, "trident_shell": trident_shell, "coral_crust": coral_crust,
    "barnacle_hide": barnacle_hide, "sealed_shell": sealed_shell, "spiral_rest": spiral_rest,
    "jet_fin": jet_fin, "dodge_jet": dodge_jet, "shore_crawl": shore_crawl, "deep_escape": deep_escape,
    "sun_shell": sun_shell, "wake_jet": wake_jet,
    "shell_wall": shell_wall, "tide_shield": tide_shield, "trident_guard": trident_guard,
    "kin_current": kin_current, "last_current": last_current, "ink_veil": ink_veil,
    "thick_ink": thick_ink, "wide_ink": wide_ink, "sour_ink": sour_ink, "ink_screen": ink_screen,
    "quick_ink": quick_ink, "ink_rush": ink_rush,
}

MODEL_GENES = {
    "deep_ram": (deep_ram_model, "deep_ram_prow"),
    "shell_wall": (shell_wall_model, "shell_wall_rim"),
    "dodge_jet": (dodge_jet_model, "dodge_jet_eyes"),
    "tide_charge": (tide_charge_model, "tide_charge_tentacles"),
    "jet_fin": (jet_fin_model, "jet_fin_fins"),
    "breath_ram": (breath_ram_model, "breath_ram_tentacles"),
    "last_current": (last_current_model, "last_current_tentacles"),
    "wide_ink": (wide_ink_model, "wide_ink_tentacles"),
    "quick_ink": (quick_ink_model, "quick_ink_tentacles"),
    "wake_jet": (wake_jet_model, "wake_jet_tentacles"),
    "ink_rush": (ink_rush_model, "ink_rush_tentacles"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="zombie nautilus genes")
