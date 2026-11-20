"""Make the gene visuals of the zombie horse, from design/species/zombie_horse.md.

Usage: python3 tools/make_zombie_horse_marks.py (from the root of the repository)

The zombie horse texture is 64x64, and it uses the zombie horse region map.
The parts, from AbstractEquineModel:
- the body, 10x10x22 at uv (0, 32). On its top, row 0 is at the tail, and
  row 21 is at the neck. Its front (22, 54) is the chest, and its back
  (54, 54) is the rump.
- the neck, 4x12x7 at uv (0, 35). The head, 6x5x7 at uv (0, 13). The snout,
  4x5x5 at uv (0, 25). The ears, 2x3x1 at uv (19, 16). The mane, 2x16x2 at
  uv (56, 36). The neck and the head lean forward by 30 degrees.
- the tail, 3x14x4 at uv (42, 36).
- the legs, 4x11x4 at uv (48, 21). All four legs use the same pixels, so a
  mark on a leg is on each leg.
The eye is in row 1 of each side of the head, in columns 3 and 4 from the
back of the head.
On a side face, the script counts the columns from the back end of the part
(the tail end). The helper side() puts the same column on the right side and
on the left side.

Each gene keeps to its own zone, so that the marks stack:
- The head. Grave Bond is a gene model: a thin strap round the muzzle, with
  a small charm. Shield Vault has an armored brow at the two sides of the
  top and over the eyes. Holy Scorn has the low rows of the sides of the
  head, the sides and the bottom of the snout, and teeth on the front. Shy
  Canter has the ears, and tall ears, a gene model.
- The mane. Last Charge has red ribbons, with loose ends as a gene model.
  Lance Run has dark streaks on the sides of the neck, and long strands, a
  gene model. Rear Up (a raised mane) is a gene model.
- The body. Rotten Barding has the back end of the back and the high sides,
  Ash Barding has the front end, and Battle Saddle (a gene model) sits
  between them. Sun Hide has the neck round the streaks of Lance Run, and the
  high sides under the saddle. On the low sides, from the tail: Rotten Hide, Undying Flesh,
  and Bone Frame. Grave Barding has the high rows of the chest, and Bone
  Shatter the low rows and the front of the belly. Charred Flesh has the
  rump, the back of the belly, and the throat. Ghost Gallop fades soft,
  uneven patches of the flanks and the neck, under all the other marks.
- The legs, from the top: High Vault rows 0 to 2, Spur Leap rows 3 and 5
  (and spurs, a gene model), Rooted Rot row 4 (and a swollen hock, a gene
  model), Golem Stomp rows 6 and 7 on the front and the sides, Mud Kick rows
  6 to 8 on the back, and the hooves: Hard Hooves on the front and the back
  of rows 9 and 10 and the bottom, Leap Strike on the sides of rows 8 to 10
  and the front of row 8. Soft Landing is a gene model: a wide, pale rim
  round the low row of the hoof. Long Stride is a gene model: long, lean
  thighs.
- The tail. Panic Bolt tears and thins the vanilla tail. Dodge Leap (a short,
  high tail) and Trample Run (a long, ragged tail) are gene models.
The gene models use the bones body, head, left_ear, right_ear, mane, tail,
and the four legs.
This script needs Pillow.
"""
import os

from PIL import Image

from make_gene_marks import MARK_DIR, Mark as TextureMark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/zombie_horse.png"

BODY = box_faces(0, 32, 10, 10, 22)
NECK = box_faces(0, 35, 4, 12, 7)
HEAD = box_faces(0, 13, 6, 5, 7)
SNOUT = box_faces(0, 25, 4, 5, 5)
EAR = box_faces(19, 16, 2, 3, 1)
MANE = box_faces(56, 36, 2, 16, 2)
TAIL = box_faces(42, 36, 3, 14, 4)
LEG = box_faces(48, 21, 4, 11, 4)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
LEG_BONES = ("left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg")


def Mark(tint, **kw):
    return TextureMark(tint, **kw)


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

    The column counts from the back end of the part (the tail end).
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


def save_mob_model(name, parts):
    import json
    from make_husk_marks import MODEL_DIR
    model = {"mob_texture": True, "texture_width": 64, "texture_height": 64, "parts": parts}
    with open(os.path.join(MODEL_DIR, name + ".json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


# The head.

def grave_bond():
    # The strap and the charm are the gene model grave_bond_strap.
    return []


def grave_bond_model():
    # A thin, dark leather strap goes round the muzzle, with thin cheek
    # straps up the sides of the head, and one small, dull green charm hangs
    # under the muzzle. On the bone head, the snout is from x -2 to 2, y -11
    # to -6, and z -7 to -2. The head is from x -3 to 3 and z -2 to 5.
    im = model_image(32, 16)

    def leather(f, x, y):
        return "#4A3422" if (x + y) % 4 == 0 else "#2A1C12"
    paint_box(im, 0, 0, 4, 5, 1, leather)
    paint_box(im, 12, 0, 0, 5, 1, leather)
    paint_box(im, 20, 0, 1, 1, 1, lambda f, x, y: "#5A7A4A")
    parts = [{"bone": "head", "uv": [0, 0], "from": [-2, -11, -5], "size": [4, 5, 1], "inflate": 0.2},
             {"bone": "head", "uv": [12, 0], "from": [3.05, -11, -1.5], "size": [0, 5, 1]},
             {"bone": "head", "uv": [12, 0], "from": [-3.05, -11, -1.5], "size": [0, 5, 1]},
             {"bone": "head", "uv": [20, 0], "from": [-0.5, -5.6, -5], "size": [1, 1, 1]}]
    save_model("grave_bond_strap", im, parts)


def shield_vault():
    # A dark, armored brow: dark iron plates cover the two sides of the top
    # of the head, and a band of iron with rivets goes over each eye.
    iron = Mark("#2A2E34")
    rivet = Mark("#9AA2AA")
    for x, y, f, col, row in pixels(HEAD, ("top",), cols=(0, 1, 4, 5)):
        (rivet if row % 3 == 1 and col in (0, 5) else iron).put((x, y))
    put_side(iron, HEAD, [(c, 0) for c in range(1, 7)])
    put_side(rivet, HEAD, [(0, 0)])
    return [iron, rivet]


def holy_scorn():
    # A pale skull face: the face is pale bone, with dark sockets round the
    # eyes, and bare teeth at the front of the snout.
    bone = Mark("#E8E0C8")
    dark = Mark("#1A1612")
    socket = [(2, 1), (5, 1), (2, 2), (3, 2), (4, 2), (5, 2)]
    put_side(dark, HEAD, socket)
    put_side(bone, HEAD, [(c, r) for c in range(7) for r in (3, 4)] + [(c, r) for c in (0, 1, 6) for r in (1, 2)])
    put(bone, SNOUT, ("right", "left", "bottom"))
    for x, y, f, col, row in pixels(SNOUT, ("front",), rows=(3, 4)):
        (dark if col % 2 == 1 and row == 4 else bone).put((x, y))
    return [bone, dark]


def shy_canter():
    # Tall, laid back ears: the tall ears are the gene model shy_canter_ears.
    # The vanilla ears turn pale inside.
    pale = Mark("#C8B8A0")
    put(pale, EAR, ("front",))
    return [pale]


def shy_canter_model():
    # Tall, laid back ears: a tall ear stands on each ear bone, 2 wide, 6
    # high, and 1 deep, and leans back by 60 degrees. Each ear is dark with
    # a pale inside and a dark tip. The vanilla ear is at y -13 to -10.
    im = model_image(16, 16)

    def ear(f, x, y):
        if y == 0 or f == "top":
            return "#1A2412"
        if f == "front":
            return "#A89880" if 1 <= y <= 4 else "#3A4A2A"
        return "#3A4A2A" if (x + y) % 3 else "#2A3A1E"
    paint_box(im, 0, 0, 2, 6, 1, ear)
    parts = [{"bone": "left_ear", "uv": [0, 0], "from": [0.55, -18, 4], "size": [2, 6, 1],
              "pivot": [1.55, -12, 4.5], "rotation": [-60, 0, 0]},
             {"bone": "right_ear", "uv": [0, 0], "from": [-2.55, -18, 4], "size": [2, 6, 1],
              "pivot": [-1.55, -12, 4.5], "rotation": [-60, 0, 0]}]
    save_model("shy_canter_ears", im, parts)


# The mane.

def last_charge():
    # Red ribbons in the mane: three red ribbons are tied round the mane,
    # each with a dark knot. The loose ends of the ribbons are the gene model
    # last_charge_ribbons.
    red = Mark("#C81818")
    knot = Mark("#5A0808")
    for r0 in (2, 7, 12):
        put(red, MANE, SIDES, rows=(r0, r0 + 1))
        put(knot, MANE, ("back",), rows=(r0 + 2,))
    return [red, knot]


def last_charge_model():
    # The loose ends of the ribbons: from each knot, a red ribbon end hangs
    # down the back of the mane, 1 pixel wide and thick. On the bone mane,
    # the mane is from x -1 to 1, y -11 to 5, and z 5 to 7.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 5, 1, lambda f, x, y: "#E02020" if y < 4 else "#7A0A0A")
    parts = []
    for r0 in (2, 7, 12):
        parts.append({"bone": "mane", "uv": [0, 0], "from": [-0.5, -11 + r0, 7], "size": [1, 5, 1],
                      "pivot": [0, -11 + r0, 7], "rotation": [-25, 0, 0]})
    save_model("last_charge_ribbons", im, parts)


def lance_run():
    # A long, streaming mane: dark streaks sweep back along the sides of the
    # neck, as hair that the wind combs back. The long strands are the gene
    # model lance_run_mane.
    streak = Mark("#141C0E")
    light = Mark("#4A5A34")
    for x, y, f, col, row in pixels(NECK, ("right", "left")):
        k = lance_streak(f, col, row)
        if k == 1:
            streak.put((x, y))
        elif k == 2:
            light.put((x, y))
    return [streak, light]


def lance_streak(f, col, row):
    """Return 1 for a dark streak of Lance Run on a side of the neck, 2 for its light edge, or 0."""
    # Column 0 of the right side is the back of the neck, under the mane.
    back = col if f == "right" else 6 - col
    k = (row + back) % 4
    if k == 0:
        return 1
    return 2 if k == 1 and back < 4 else 0


def lance_run_model():
    # A long, streaming mane: four long, thin strands hang from the sides of
    # the mane, on the left and the right in turn, and stream back. The
    # tufts of Rear Up stand in the middle, so the two stay apart. On the bone mane, the mane is from x -1 to 1, y
    # -11 to 5, and z 5 to 7.
    im = model_image(32, 32)

    def strand(f, x, y):
        return "#1A2412" if (x + y) % 3 else "#3A4A2A"
    paint_box(im, 0, 0, 1, 9, 1, strand)
    parts = [{"bone": "mane", "uv": [0, 0], "from": [x, y, 6], "size": [1, 9, 1],
              "pivot": [x + 0.5, y, 6], "rotation": [-35 - n * 5, 0, 0]}
             for n, (x, y) in enumerate(((-2, -10), (1, -6), (-2, -2), (1, 2)))]
    save_model("lance_run_mane", im, parts)


def rear_up():
    # The wild, raised mane is the gene model rear_up_mane.
    return []


def rear_up_model():
    # A wild, raised mane: spiky tufts stand up out of the mane along the
    # neck, each one dark at the base and pale at the tip.
    im = model_image(32, 32)

    def tuft(f, x, y):
        return "#C8C0A0" if y == 0 or f == "top" else "#2A3A1E"
    paint_box(im, 0, 0, 2, 3, 2, tuft)
    parts = [{"bone": "mane", "uv": [0, 0], "from": [-1, y - 3, 5], "size": [2, 3, 2],
              "pivot": [0, y, 5], "rotation": [-110, 0, 0]} for y in (-10, -6, -2, 2)]
    save_model("rear_up_mane", im, parts)


# The tail.

def panic_bolt():
    # A torn, thin tail: the outer columns and the end of the tail are torn
    # away (erased with a mask), and the torn edges are pale.
    mask = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    pale = Mark("#C8C0A8")
    for x, y, f, col, row in pixels(TAIL, SIDES):
        w = TAIL[f][2]
        edge = col in (0, w - 1)
        if (edge and noise100(x, y, 6101) < 70) or row >= 11:
            mask.putpixel((x, y), (255, 255, 255, 255))
        elif edge or row == 10:
            pale.put((x, y))
    name = "panic_bolt_mask"
    mask.save(os.path.join(MARK_DIR, name + ".png"))
    torn = {"regions": ["pelvis"], "effect": {"type": "erase", "texture": f"originofspawns:textures/gene_marks/{name}.png"}}
    return [pale, torn]


def dodge_leap():
    # The short, high tail is the gene model dodge_leap_tail.
    return []


def dodge_leap_model():
    # A short, high tail: a short, thick tuft stands up and back from the
    # base of the tail. On the bone tail, the tail is from x -1.5 to 1.5, y 0
    # to 14, and z 0 to 4, and it hangs down at 30 degrees.
    im = model_image(32, 32)

    def tuft(f, x, y):
        return "#A8B890" if y == 0 else "#2A3A1E" if (x + y) % 3 else "#3A4A2A"
    paint_box(im, 0, 0, 3, 5, 3, tuft)
    parts = [{"bone": "tail", "uv": [0, 0], "from": [-1.5, -5, 0.5], "size": [3, 5, 3],
              "pivot": [0, 0, 2], "rotation": [-60, 0, 0]}]
    save_model("dodge_leap_tail", im, parts)


def trample_run():
    # The long, ragged tail is the gene model trample_run_tail.
    return []


def trample_run_model():
    # A long, ragged tail: three long, ragged strands hang on from the end of
    # the tail, each of a different length, with pale, torn ends.
    im = model_image(32, 32)

    def strand(length):
        def color(f, x, y):
            return "#B8B098" if y >= length - 1 else "#1E2A16" if (x + y) % 2 else "#2A3A1E"
        return color
    for n, length in enumerate((8, 6, 9)):
        paint_box(im, n * 6, 0, 1, length, 1, strand(length))
    parts = [{"bone": "tail", "uv": [n * 6, 0], "from": [x, 13, z], "size": [1, length, 1]}
             for n, (x, z, length) in enumerate(((-1.5, 1, 8), (-0.5, 2.5, 6), (0.5, 1.5, 9)))]
    save_model("trample_run_tail", im, parts)


# The body.

def body_top(mark_of, rows):
    for x, y, f, col, row in pixels(BODY, ("top",), rows=rows):
        m = mark_of(col, row)
        if m:
            m.put((x, y))


def rotten_barding():
    # A ragged cloth over the back: a rotten, brown cloth lies over the back
    # end of the back and hangs down the high sides, with a ragged, torn hem
    # and holes.
    cloth = Mark("#5A4A30")
    dark = Mark("#2E2618")
    body_top(lambda c, r: dark if noise100(c, r, 6201) < 15 else cloth, range(0, 8))
    for c in range(0, 8):
        hem = 2 + noise100(c, 0, 6202) % 2
        for r in range(hem + 1):
            (dark if r == hem or noise100(c, r, 6203) < 12 else cloth).put(side(BODY, c, r))
    for x, y, f, col, row in pixels(BODY, ("back",), rows=(0, 1)):
        cloth.put((x, y))
    return [cloth, dark]


def ash_barding():
    # A gray, scorched cloth: a gray cloth lies over the front end of the
    # back and hangs down the high sides, with black, burnt edges and orange
    # embers.
    gray = Mark("#8A8A88")
    burnt = Mark("#1A1816")
    ember = Mark("#FF7A20")
    body_top(lambda c, r: burnt if c in (0, 9) else ember if noise100(c, r, 6301) < 6 else gray, range(14, 22))
    for c in range(14, 22):
        for r in range(4):
            m = burnt if r == 3 else ember if noise100(c, r, 6302) < 8 else gray
            m.put(side(BODY, c, r))
    return [gray, burnt, ember]


def battle_saddle():
    # The high, dark saddle is the gene model battle_saddle_saddle.
    return []


def battle_saddle_model():
    # A high, dark saddle: a seat of dark leather on the middle of the back,
    # with a high front and a high back, iron edges, and flaps down the
    # sides. On the bone body, the back is at y -8, from x -5 to 5, and the
    # body is from z -17 to 5. The saddle is from z -9 to -3.
    im = model_image(64, 32)

    def leather(f, x, y):
        if f == "top" and (x == 0 or y == 0):
            return "#8A8E94"
        return "#2A1A10" if (x + y) % 4 else "#3E2818"

    def high(f, x, y):
        return "#8A8E94" if y == 0 else "#1E120A"
    paint_box(im, 0, 0, 10, 1, 6, leather)
    paint_box(im, 0, 8, 6, 3, 1, high)
    paint_box(im, 20, 8, 1, 5, 4, leather)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-5, -9, -9], "size": [10, 1, 6], "inflate": 0.05},
             {"bone": "body", "uv": [0, 8], "from": [-3, -12, -9.5], "size": [6, 3, 1]},
             {"bone": "body", "uv": [0, 8], "from": [-3, -11, -3.5], "size": [6, 3, 1]},
             {"bone": "body", "uv": [20, 8], "from": [-5.6, -8, -8], "size": [1, 5, 4]},
             {"bone": "body", "uv": [20, 8], "from": [4.6, -8, -8], "size": [1, 5, 4], "mirror": True}]
    save_model("battle_saddle_saddle", im, parts)


def sun_hide():
    # A dark, leathery hide: the neck, round the streaks of Lance Run, and
    # the high sides under the saddle are dark, tough leather, with fine,
    # pale creases that run across it.
    leather = Mark("#2E2A1A")
    crease = Mark("#6A5A3A")

    def mark(col, row):
        return crease if row % 3 == 0 and (col + row // 3) % 4 != 0 else leather
    for x, y, f, col, row in pixels(NECK, ("top",)):
        mark(col, row).put((x, y))
    for x, y, f, col, row in pixels(NECK, ("right", "left")):
        if not lance_streak(f, col, row):
            mark(col, row).put((x, y))
    for c in range(8, 14):
        for r in range(4):
            mark(c, r).put(side(BODY, c, r))
    return [leather, crease]


def rotten_hide():
    # Thick, rotten patches: thick patches of rotten, green black flesh, with
    # pale, sick edges, on the back end of the low sides.
    rot = Mark("#1E2A10")
    sick = Mark("#A8B860")
    for c in range(0, 7):
        for r in range(4, 10):
            n = noise100(c // 2, r // 2, 6401)
            if n < 55:
                rot.put(side(BODY, c, r))
            elif n < 75:
                sick.put(side(BODY, c, r))
    return [rot, sick]


def undying_flesh():
    # Stitched wounds on the flank: two long wounds on the middle of the
    # flank, dark red, closed with pale stitches across them.
    wound = Mark("#5A0E0E")
    stitch = Mark("#E0D8C0")
    for c in range(7, 14):
        for r0 in (5, 8):
            r = r0 + (c % 3 == 0)
            wound.put(side(BODY, c, r))
            if c % 2 == 1:
                stitch.put(side(BODY, c, r - 1))
                stitch.put(side(BODY, c, r + 1))
    return [wound, stitch]


def bone_frame():
    # Bare ribs on the flank: pale ribs that curve down show through the
    # dark, torn flesh on the front end of the low sides. The ribs are thin
    # at their ends, and the flesh between them is a deep shadow.
    bone = Mark("#E0D8C0")
    shade = Mark("#B0A890")
    gap = Mark("#18200E")
    for c in range(15, 22, 2):
        for r in range(4, 10):
            col = c - (1 if r >= 8 else 0)
            (shade if r in (4, 9) else bone).put(side(BODY, col, r))
            if r in (5, 6, 7):
                gap.put(side(BODY, col + 1, r))
    return [bone, shade, gap]


def grave_barding():
    # Bone plates on the chest: three rows of pale bone plates on the high
    # part of the chest, each with a dark edge.
    bone = Mark("#E0D8C0")
    edge = Mark("#5A5444")
    for x, y, f, col, row in pixels(BODY, ("front",), rows=range(0, 6)):
        (edge if row % 2 == 1 or col % 3 == 0 and row % 4 == 0 else bone).put((x, y))
    return [bone, edge]


def bone_shatter():
    # Cracked bones on the chest: a pale breast bone on the low part of the
    # chest and the front of the belly, broken by dark cracks.
    bone = Mark("#D8D0B8")
    crack = Mark("#1A1612")
    for x, y, f, col, row in pixels(BODY, ("front",), rows=range(6, 10)):
        (crack if (col + row) % 4 == 0 else bone).put((x, y))
    for x, y, f, col, row in pixels(BODY, ("bottom",), cols=range(2, 8), rows=range(14, 22)):
        (crack if (col * 2 + row) % 5 == 0 else bone).put((x, y))
    return [bone, crack]


def charred_flesh():
    # Black, charred patches: the rump, the back of the belly, and the throat
    # have large, black, charred patches, with glowing, orange cracks.
    char = Mark("#0E0A08")
    glow = Mark("#E06A18")
    cells = [(x, y) for x, y, f, col, row in pixels(BODY, ("back",), rows=range(2, 10))]
    cells += [(x, y) for x, y, f, col, row in pixels(BODY, ("bottom",), rows=range(0, 12))]
    cells += [(x, y) for x, y, f, col, row in pixels(NECK, ("front",))]
    for x, y in cells:
        n = noise100(x // 2, y // 2, 6501)
        if n < 50:
            (glow if noise100(x, y, 6502) < 12 else char).put((x, y))
    return [char, glow]


def ghost_gallop():
    # Pale, faded hide: soft, uneven patches of the hide fade to a pale,
    # ghostly gray over the flanks and the neck, and the green hide shows
    # between them. The patches go under the marks of the other genes.
    pale = Mark("#C8D0C0", alpha=150, order=-1, regions=["torso"])
    faint = Mark("#A8B4A0", alpha=110, order=-1, regions=["torso"])
    cells = []
    for c in range(22):
        for r in range(10):
            cells += side(BODY, c, r)
    cells += [(x, y) for x, y, f, col, row in pixels(NECK, ("right", "left"))]
    for x, y in cells:
        n = noise100(x // 3, y // 2, 6601)
        if n < 40:
            pale.put((x, y), 220 + noise100(x, y, 6602) // 3)
        elif n < 55:
            faint.put((x, y))
    return [pale, faint]


# The legs.

def leg_put(mark, faces, rows, test=None):
    for x, y, f, col, row in pixels(LEG, faces, rows=rows):
        if test is None or test(x, y, f, col, row):
            mark.put((x, y))


def high_vault():
    # Long, springy hocks: pale, springy tendons go down the high legs in a
    # zigzag, over dark sinew.
    tendon = Mark("#C8D0A8")
    sinew = Mark("#1E2A14")
    leg_put(tendon, SIDES, (0, 1, 2), lambda x, y, f, c, r: c == (r % 2) + 1)
    leg_put(sinew, SIDES, (0, 1, 2), lambda x, y, f, c, r: c != (r % 2) + 1 and c in (0, 3))
    return [tendon, sinew]


def spur_leap():
    # Pale, worn bands with spurs: two pale, worn bands go round each hock,
    # and small metal spurs (the gene model spur_leap_spurs) stand out at the
    # back of the hocks.
    band = Mark("#B8B0A0")
    worn = Mark("#8A8478")
    leg_put(band, SIDES, (3, 5), lambda x, y, f, c, r: (c + r) % 3 != 0)
    leg_put(worn, SIDES, (3, 5), lambda x, y, f, c, r: (c + r) % 3 == 0)
    return [band, worn]


def spur_leap_model():
    # Small metal spurs: a short, dull steel spur stands out back from the
    # back of each hock, between the two bands. A leg is from y -1 to 10 on
    # its bone, so row 4 is at y 3. The back of a hind leg is at z 3, and
    # the back of a front leg at z 2.1.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 2, lambda f, x, y: "#8A8E90" if (x + y) % 2 else "#5A5E60")
    parts = []
    for bone in LEG_BONES:
        left = bone.startswith("left")
        z = 3 if "hind" in bone else 2.1
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1.5 if left else 0.5, 3, z], "size": [1, 1, 2]})
    save_model("spur_leap_spurs", im, parts)


def rooted_rot():
    # Thick, swollen hocks: a dark, knotted band at each hock, and the hock
    # is a swollen copy of the leg (the gene model rooted_rot_hocks).
    knot = Mark("#1A140C")
    lump = Mark("#6A7A3A")
    leg_put(knot, SIDES, (4,), lambda x, y, f, c, r: c % 2 == 0)
    leg_put(lump, SIDES, (4,), lambda x, y, f, c, r: c % 2 == 1)
    return [knot, lump]


def rooted_rot_model():
    # A swollen hock: a copy of rows 2 to 4 of each leg, with the texture of
    # the mob and its marks, 0.5 pixel larger on each side. A leg is from y
    # -1 to 10 on its bone, so rows 2 to 4 are from y 1 to 4.
    parts = []
    for bone in LEG_BONES:
        left = bone.startswith("left")
        z = -1.9 if "front" in bone else -1
        parts.append({"bone": bone, "uv": [48, 23], "from": [-3 if left else -1, 1, z], "size": [4, 3, 4],
                      "inflate": 0.5, "mirror": left})
    save_mob_model("rooted_rot_hocks", parts)


def golem_stomp():
    # Iron gray shins: an iron plate on the front and the sides of each shin,
    # with a pale top edge.
    iron = Mark("#8A9098")
    light = Mark("#D0D6DC")
    leg_put(light, ("front", "right", "left"), (6,))
    leg_put(iron, ("front", "right", "left"), (7,))
    return [iron, light]


def mud_kick():
    # Mud on the lower legs: wet, brown mud cakes the back of the shins, from
    # the shin down to the hoof.
    mud = Mark("#4A3018")
    wet = Mark("#7A5A30")
    leg_put(mud, ("back",), (6, 7, 8), lambda x, y, f, c, r: noise100(x, y, 6701) < 75)
    leg_put(wet, ("back",), (6, 7, 8), lambda x, y, f, c, r: noise100(x, y, 6701) >= 75)
    return [mud, wet]


def hard_hooves():
    # Dark, cracked hooves: the front and the back of each hoof are black,
    # with pale cracks.
    black = Mark("#0E0C0A")
    crack = Mark("#8A8478")
    leg_put(black, ("front", "back"), (9, 10), lambda x, y, f, c, r: (c + r) % 3 != 0)
    leg_put(crack, ("front", "back"), (9, 10), lambda x, y, f, c, r: (c + r) % 3 == 0)
    put(black, LEG, ("bottom",))
    return [black, crack]


def leap_strike():
    # Iron gray hooves: the sides of each hoof are iron gray, with a bright
    # shine, and an iron cap goes round the front of the top of the hoof.
    iron = Mark("#7A8088")
    shine = Mark("#D8DEE4")
    leg_put(iron, ("right", "left"), (8, 9, 10), lambda x, y, f, c, r: not (r == 9 and c == 1))
    leg_put(shine, ("right", "left"), (9,), lambda x, y, f, c, r: c == 1)
    leg_put(shine, ("front",), (8,))
    return [iron, shine]


def soft_landing():
    # The wide, pale hooves are the gene model soft_landing_hooves.
    return []


def soft_landing_model():
    # Wide, pale hooves: a wide, pale rim of soft hoof, 6 wide and 6 deep,
    # round the low row of each hoof. On each leg bone, the leg is from y
    # -1 to 10.
    im = model_image(32, 16)

    def rim(f, x, y):
        if f == "top":
            return "#E8DCC0" if x in (0, 5) or y in (0, 5) else None
        return "#E8DCC0" if (x + y) % 3 else "#B8AC90"
    paint_box(im, 0, 0, 6, 1, 6, rim)
    parts = []
    for bone in LEG_BONES:
        left = bone.startswith("left")
        z = -1.9 if "front" in bone else -1
        parts.append({"bone": bone, "uv": [0, 0], "from": [(-3 if left else -1) - 1, 9.5, z - 1],
                      "size": [6, 1, 6]})
    save_model("soft_landing_hooves", im, parts)


def long_stride():
    # The long, lean legs are the gene model long_stride_legs.
    return []


def long_stride_model():
    # Long, lean legs: a lean thigh of 2x8x1 goes up from the top of each
    # leg along the flank, so that each leg looks long. It is dark, with a
    # pale tendon on its outer side. The body side is at x -5 and 5, and the
    # legs are at x -4 and 4. The thigh stands 1 pixel out from the side.
    im = model_image(32, 32)

    def thigh(f, x, y):
        if f in ("right", "left") and x == 1:
            return "#A8B890"
        return "#1E2A14" if (x + y) % 5 else "#2E3E20"
    paint_box(im, 0, 0, 2, 8, 1, thigh)
    parts = []
    for bone in LEG_BONES:
        left = bone.startswith("left")
        z = 2 if "hind" in bone else -1.9
        parts.append({"bone": bone, "uv": [0, 0], "from": [0 if left else -2, -9, z], "size": [2, 8, 1]})
    save_model("long_stride_legs", im, parts)


GENES = {
    "long_stride": long_stride, "sun_hide": sun_hide, "shy_canter": shy_canter, "lance_run": lance_run,
    "ghost_gallop": ghost_gallop, "panic_bolt": panic_bolt,
    "rotten_barding": rotten_barding, "battle_saddle": battle_saddle, "grave_bond": grave_bond,
    "ash_barding": ash_barding, "last_charge": last_charge, "grave_barding": grave_barding,
    "undying_flesh": undying_flesh, "rotten_hide": rotten_hide, "charred_flesh": charred_flesh,
    "bone_frame": bone_frame, "holy_scorn": holy_scorn, "rooted_rot": rooted_rot,
    "high_vault": high_vault, "soft_landing": soft_landing, "leap_strike": leap_strike,
    "dodge_leap": dodge_leap, "shield_vault": shield_vault, "spur_leap": spur_leap,
    "hard_hooves": hard_hooves, "mud_kick": mud_kick, "golem_stomp": golem_stomp,
    "bone_shatter": bone_shatter, "rear_up": rear_up, "trample_run": trample_run,
}

MODEL_GENES = {
    "long_stride": (long_stride_model, "long_stride_legs"),
    "shy_canter": (shy_canter_model, "shy_canter_ears"),
    "lance_run": (lance_run_model, "lance_run_mane"),
    "battle_saddle": (battle_saddle_model, "battle_saddle_saddle"),
    "rooted_rot": (rooted_rot_model, "rooted_rot_hocks"),
    "soft_landing": (soft_landing_model, "soft_landing_hooves"),
    "dodge_leap": (dodge_leap_model, "dodge_leap_tail"),
    "rear_up": (rear_up_model, "rear_up_mane"),
    "trample_run": (trample_run_model, "trample_run_tail"),
    "last_charge": (last_charge_model, "last_charge_ribbons"),
    "grave_bond": (grave_bond_model, "grave_bond_strap"),
    "spur_leap": (spur_leap_model, "spur_leap_spurs"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="zombie horse genes")
