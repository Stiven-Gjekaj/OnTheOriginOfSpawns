"""Make the gene visuals of the skeleton horse, from design/species/skeleton_horse.md.

Usage: python3 tools/make_skeleton_horse_marks.py (from the root of the repository)

The skeleton horse texture is 64x64, and it uses the skeleton horse region
map. It has the parts and the UV layout of the zombie horse (see
tools/make_zombie_horse_marks.py). Most of its pixels are clear, between the
bones:
- the body: on each side, the pelvis (columns 0 to 2), the spine (rows 0
  and 1), five ribs (columns 6 to 15, rows 2 to 9), and the shoulder
  (columns 16 to 21). On the top, the spine down the middle, with cross
  bars. The chest is a frame, and the belly is the breast bone.
- the neck: the back of the neck, and two columns of each side.
- the head and the snout: nearly all pixels.
- the legs: most of the outer side, two columns of the front, and the hoof.
- the mane: 8 pixels on each side. The tail: no pixels at all.
On a side face, the script counts the columns from the back end of the part
(the tail end). The helper side() puts the same column on the right side and
on the left side. A mark on a clear pixel stays clear, so each mark keeps to
the bones.

Each gene keeps to its own zone, so that the marks stack:
- The ribs. Rib Gaps erases the second and the fourth rib. Trident Ribs has
  rows 4 and 7 of the ribs, a steel ring across each. Charged Riders has the
  first and the middle rib, and two cross bars of the spine on the top, out
  of the place of a saddle. Blast Ribs has the last rib and the shoulder. Rattle Flight is a gene model: loose ribs that
  hang under the body.
- The rest of the body. Bone Barding has the spine on the sides. Coral Bones
  has the pelvis and the rump. Bone Mend has the chest and the breast bone.
  Wraith Veil is a fade under all the other marks. Storm Cloak (a cloth on
  the back end of the back) and Squire Saddle (a saddle) are gene models.
- The head. Helm Bond has an iron band round the high row of the head.
  Faithful Bones has a blue mark on the top of the head. Wide Trap has dark
  rims round the eye sockets, and glowing sockets, a gene model. Blessed
  Skull has the sides and the top of the snout. Long Wait has the front and
  the bottom of the snout, the bottom of the head, and the back of the head.
- The mane. Storm Kin has white streaks on the back of the neck and the
  mane. Storm Herald (blue, sparking strands), Kelp Mane (kelp strands),
  Last Ride (torn ribbons), and Bone Dodge (short, stiff spikes) are gene
  models, each in its own place along the neck.
- The legs, from the top: Shore Bound rows 0, 2, 3, and 5, Grounded Bones
  rows 1 and 4, Golem Brace rows 6 to 8, and the hooves, rows 9 and 10: Deep
  Stride on the sides, Weightless on the front and the back and the bottom.
  Heavy Bones (a thick copy of the leg) and Ghost Stride (long, thin struts
  up the flank) are gene models.
- The tail. The vanilla tail has no pixels, so Cold Current (a long, wet
  tail) and Night Run (pale wisps) are gene models.
The gene models use the bones body, head, mane, tail, and the four legs.
This script needs Pillow.
"""
import os

from PIL import Image

from make_gene_marks import MARK_DIR, Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombie_horse_marks import (ALL, BODY, HEAD, LEG, LEG_BONES, MANE, NECK, SIDES, SNOUT, at, pixels, put,
                                     put_side, save_mob_model, side)
from make_zombified_piglin_marks import model_image, write_all

REGION_MAP = "originofspawns:textures/region/skeleton_horse.png"

# The columns of the five ribs on a side of the body, from the back.
RIBS = ((6, 7), (8, 9), (10, 11), (12, 13), (14, 15))
RIB_ROWS = range(2, 10)
RING_ROWS = (4, 7)


def rib_cells(ribs, rows=RIB_ROWS):
    """Yield (col, row) of the ribs in the rows, but not in the rows of the steel rings."""
    for rib in ribs:
        for c in RIBS[rib]:
            for r in rows:
                if r not in RING_ROWS:
                    yield c, r


def leg_put(mark, faces, rows, test=None):
    for x, y, f, col, row in pixels(LEG, faces, rows=rows):
        if test is None or test(x, y, f, col, row):
            mark.put((x, y))


# The ribs.

def rib_gaps():
    # Wide gaps between the ribs: the second and the fourth rib are gone
    # (erased with a mask), so the gaps between the ribs are wide. The rows
    # of the rings of Trident Ribs stay. The bone in front of the first rib
    # is dark.
    mask = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    dark = Mark("#3A3A36")
    for c in RIBS[1] + RIBS[3]:
        for r in RIB_ROWS:
            if r in RING_ROWS:
                continue
            for p in side(BODY, c, r):
                mask.putpixel(p, (255, 255, 255, 255))
    for c in (5,):
        for r in RIB_ROWS:
            if r not in RING_ROWS:
                dark.put(side(BODY, c, r))
    name = "rib_gaps_mask"
    mask.save(os.path.join(MARK_DIR, name + ".png"))
    gone = {"regions": ["torso"], "effect": {"type": "erase", "texture": f"originofspawns:textures/gene_marks/{name}.png"}}
    return [dark, gone]


def trident_ribs():
    # Steel rings round the ribs: two steel rings go across the ribs, with
    # bright rivets where they cross a rib.
    steel = Mark("#5A646C")
    rivet = Mark("#D8E0E4")
    for c in range(5, 16):
        for r in RING_ROWS:
            (rivet if c % 2 == 0 else steel).put(side(BODY, c, r))
    return [steel, rivet]


def charged_riders():
    # Blue lines on the ribs: bright blue lines of charge run down the first
    # and the middle rib, and across the front and the back cross bar of the
    # spine on the top, out of the place of a saddle.
    blue = Mark("#40B0FF")
    pale = Mark("#C8F0FF")
    for c, r in rib_cells((0, 2)):
        (pale if (c + r) % 3 == 0 else blue).put(side(BODY, c, r))
    for x, y, f, col, row in pixels(BODY, ("top",), rows=(8, 16)):
        (pale if col in (4, 5) else blue).put((x, y))
    return [blue, pale]


def blast_ribs():
    # Dark, cracked ribs: the last rib and the shoulder are dark, burnt bone
    # with pale cracks.
    dark = Mark("#3A342C")
    crack = Mark("#C8C0B0")
    cells = list(rib_cells((4,)))
    cells += [(c, r) for c in range(16, 22) for r in RIB_ROWS if r not in RING_ROWS]
    for c, r in cells:
        (crack if (c * 2 + r) % 5 == 0 else dark).put(side(BODY, c, r))
    return [dark, crack]


def rattle_flight():
    # The loose, rattling ribs are the gene model rattle_flight_ribs.
    return []


def rattle_flight_model():
    # Loose, rattling ribs: four loose rib bones hang at a slant under the
    # body, two on each side. On the bone body, the body is from x -5 to 5,
    # y -8 to 2, and z -17 to 5. Its low edge is at y 2.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 5, 1, lambda f, x, y: "#E8E4D8" if y < 4 else "#9A968A")
    parts = []
    for x, z, turn in ((-5, -10, 25), (-5, -6, -20), (4, -8, -25), (4, -4, 20)):
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, 1, z], "size": [1, 5, 1],
                      "pivot": [x + 0.5, 1, z + 0.5], "rotation": [turn, 0, 0]})
    save_model("rattle_flight_ribs", im, parts)


# The rest of the body.

def bone_barding():
    # Bone plates on the flanks: a row of thick bone plates lies along the
    # spine on each flank, each plate with a dark edge.
    plate = Mark("#F4F0E4")
    edge = Mark("#7A746A")
    for c in range(3, 22):
        for r in (0, 1):
            (edge if c % 3 == 0 or r == 1 and c % 3 == 2 else plate).put(side(BODY, c, r))
    return [plate, edge]


def coral_bones():
    # Pink coral on the bones: pink coral with pale tips grows on the pelvis
    # and on the rump.
    pink = Mark("#F070A0")
    pale = Mark("#FFC8DC")
    cells = []
    for c in range(0, 3):
        for r in range(2, 10):
            cells += side(BODY, c, r)
    cells += [(x, y) for x, y, f, col, row in pixels(BODY, ("back",))]
    for x, y in cells:
        n = noise100(x, y, 7701)
        if n < 70:
            (pale if n < 20 else pink).put((x, y))
    return [pink, pale]


def bone_mend():
    # A white, healed bone on the chest: the frame of the chest and the
    # breast bone are bright, white, new bone, with a pale seam where they
    # healed.
    white = Mark("#FFFFFF")
    seam = Mark("#B8D0C8")
    for x, y, f, col, row in pixels(BODY, ("front",)):
        (seam if row in (3, 6) else white).put((x, y))
    for x, y, f, col, row in pixels(BODY, ("bottom",), rows=range(11, 22)):
        (seam if row % 4 == 0 else white).put((x, y))
    return [white, seam]


def wraith_veil():
    # Pale, faded bones: the bones fade to a cold, pale gray blue. The layers
    # go under the marks of the other genes.
    pale = Mark("#C8D4E0", alpha=140, order=-1, regions=["torso", "face", "scalp"])
    for part in (BODY, NECK, HEAD, SNOUT):
        for x, y, f, col, row in pixels(part):
            pale.put((x, y), 220 + noise100(x, y, 7801) // 4)
    return [{"regions": ["torso", "face", "scalp"], "effect": {"type": "desaturate", "amount": 0.7}, "order": -2},
            pale]


def storm_cloak():
    # The gray, singed cloth is the gene model storm_cloak_cloth.
    return []


def storm_cloak_model():
    # A gray, singed cloth: a gray cloth lies over the back end of the back
    # and hangs down a little on each side, with black, burnt edges. On the
    # bone body, the back is at y -8, and the back end of the body is at z
    # 5.
    im = model_image(64, 32)

    def cloth(f, x, y):
        if f == "top":
            return "#1A1816" if x in (0, 9) or y in (0, 7) else "#7A7A78" if (x + y) % 4 else "#6A6A68"
        return "#1A1816" if y == 2 or (x + y) % 5 == 0 else "#7A7A78"
    paint_box(im, 0, 0, 10, 0, 8, cloth)
    paint_box(im, 0, 12, 0, 3, 8, cloth)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-5, -8.3, -3], "size": [10, 0, 8], "inflate": 0.2},
             {"bone": "body", "uv": [0, 12], "from": [-5.4, -8.3, -3], "size": [0, 3, 8]},
             {"bone": "body", "uv": [0, 12], "from": [5.4, -8.3, -3], "size": [0, 3, 8]}]
    save_model("storm_cloak_cloth", im, parts)


def squire_saddle():
    # The old, dark saddle is the gene model squire_saddle_saddle.
    return []


def squire_saddle_model():
    # An old, dark saddle: a cracked seat of dark, old leather on the spine,
    # in front of the cloth of Storm Cloak, with a worn front and back, and
    # short flaps. The saddle is from z -10 to -4 on the bone body.
    im = model_image(64, 32)

    def leather(f, x, y):
        return "#3A2A1C" if (x + 2 * y) % 5 else "#1E140C"

    def high(f, x, y):
        return "#5A4228" if y == 0 else "#2A1C10"
    paint_box(im, 0, 0, 8, 1, 6, leather)
    paint_box(im, 0, 8, 6, 2, 1, high)
    paint_box(im, 20, 8, 1, 4, 4, leather)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-4, -9, -10], "size": [8, 1, 6]},
             {"bone": "body", "uv": [0, 8], "from": [-3, -11, -10.5], "size": [6, 2, 1]},
             {"bone": "body", "uv": [0, 8], "from": [-3, -10, -4.5], "size": [6, 2, 1]},
             {"bone": "body", "uv": [20, 8], "from": [-5.3, -8, -9], "size": [1, 4, 4]},
             {"bone": "body", "uv": [20, 8], "from": [4.3, -8, -9], "size": [1, 4, 4], "mirror": True}]
    save_model("squire_saddle_saddle", im, parts)


# The head.

def helm_bond():
    # An iron band round the skull: a band of dark iron goes round the high
    # row of the head, with bright rivets.
    iron = Mark("#4A4E54")
    rivet = Mark("#C8CED4")
    for x, y, f, col, row in pixels(HEAD, SIDES, rows=(0,)):
        (rivet if col % 3 == 1 else iron).put((x, y))
    return [iron, rivet]


def faithful_bones():
    # A blue mark on the skull: a blue mark in the shape of a horseshoe on
    # the top of the head, with a pale middle.
    blue = Mark("#2A70E0")
    pale = Mark("#A8D0FF")
    shoe = {(1, 1), (1, 2), (1, 3), (1, 4), (2, 5), (3, 5), (4, 4), (4, 3), (4, 2), (4, 1)}
    for x, y, f, col, row in pixels(HEAD, ("top",)):
        if (col, row) in shoe:
            blue.put((x, y))
        elif 2 <= col <= 3 and 1 <= row <= 4:
            pale.put((x, y))
    return [blue, pale]


def wide_trap():
    # Wide, glowing eye sockets: the glow is the gene model wide_trap_eyes.
    # A dark rim goes round each socket, wider than the socket.
    rim = Mark("#1A1A1E")
    put_side(rim, HEAD, [(c, r) for c in range(1, 7) for r in (1, 2, 3) if (c, r) not in ((3, 1), (4, 1))])
    return [rim]


def wide_trap_model():
    # Wide, glowing eye sockets: a glowing, pale blue square of 2x2 in each
    # eye socket. On the bone head, the head is from x -3 to 3, y -11 to -6,
    # and z -2 to 5. The eye is in row 1 of the side, at y -10 to -9, and
    # in columns 3 and 4 from the back, at z 1 to 3.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 0, 2, 2, lambda f, x, y: "#E0F8FF" if (x + y) % 2 else "#80D8FF")
    parts = [{"bone": "head", "uv": [0, 0], "from": [3.03, -10, 1], "size": [0, 2, 2], "glow": True},
             {"bone": "head", "uv": [0, 0], "from": [-3.03, -10, 1], "size": [0, 2, 2], "glow": True}]
    save_model("wide_trap_eyes", im, parts)


def blessed_skull():
    # A pale, clean skull: the sides and the top of the snout are clean,
    # smooth, pale bone, with a thin gold edge round them.
    clean = Mark("#F2EEE2")
    gold = Mark("#D8B860")
    for x, y, f, col, row in pixels(SNOUT, ("right", "left", "top")):
        w, h = SNOUT[f][2], SNOUT[f][3]
        edge = col in (0, w - 1) or row in (0, h - 1)
        (gold if edge else clean).put((x, y))
    return [clean, gold]


def long_wait():
    # A dark, empty skull: the front and the bottom of the snout, the bottom
    # of the head, and the back of the head under the band of Helm Bond are
    # dark, as if the skull is hollow and empty.
    dark = Mark("#26241F")
    shade = Mark("#4A4740")
    for part, faces in ((SNOUT, ("front", "bottom")), (HEAD, ("bottom", "back"))):
        for x, y, f, col, row in pixels(part, faces):
            if part is HEAD and f == "back" and row == 0:
                continue
            (shade if (col + row) % 3 == 0 else dark).put((x, y))
    return [dark, shade]


# The mane.

def storm_kin():
    # White streaks in the mane: bright, white streaks run down the back of
    # the neck and the mane, as lightning.
    white = Mark("#FFFFFF")
    pale = Mark("#C8E0FF")
    for x, y, f, col, row in pixels(NECK, ("back",)):
        k = (col + row) % 4
        if k == 0:
            white.put((x, y))
        elif k == 1 and row % 2 == 0:
            pale.put((x, y))
    put(white, MANE, SIDES)
    return [white, pale]


def mane_strands(name, color_of, spots, size=(1, 6, 1), turn=-30, glow=False):
    """Save a gene model of strands on the mane. spots is a list of (x, y, z) on the bone mane."""
    im = model_image(16, 16)
    paint_box(im, 0, 0, size[0], size[1], size[2], color_of)
    parts = [{"bone": "mane", "uv": [0, 0], "from": [x, y, z], "size": list(size),
              "pivot": [x + size[0] / 2, y, z], "rotation": [turn, 0, 0]} for x, y, z in spots]
    if glow:
        for p in parts:
            p["glow"] = True
    save_model(name, im, parts)


def storm_herald():
    return []


def storm_herald_model():
    # A blue, sparking mane: three blue strands glow along the back of the
    # high neck, with white sparks.
    mane_strands("storm_herald_mane", lambda f, x, y: "#F0FAFF" if y % 3 == 0 else "#40A0FF",
                 [(-0.5, -11, 7), (-0.5, -8, 7), (-0.5, -5, 7)], size=(1, 5, 1), turn=-45, glow=True)


def kelp_mane():
    return []


def kelp_mane_model():
    # Green kelp in the mane: long, green strands of kelp hang from the low
    # part of the mane, on both sides of the neck.
    mane_strands("kelp_mane_kelp", lambda f, x, y: "#2E8A2A" if (x + y) % 3 else "#5AB840",
                 [(2.1, -2, 4), (-3.1, -2, 4), (2.1, 2, 4), (-3.1, 2, 4)], size=(1, 7, 1), turn=-10)


def last_ride():
    return []


def last_ride_model():
    # Torn ribbons in the mane: torn, faded red ribbons hang from the high
    # part of the mane, on both sides of the neck.
    mane_strands("last_ride_ribbons", lambda f, x, y: "#C8B8A0" if y >= 3 else "#A02020",
                 [(2.1, -9, 4), (-3.1, -9, 4), (2.1, -6, 4), (-3.1, -6, 4)], size=(1, 4, 1), turn=-15)


def bone_dodge():
    return []


def bone_dodge_model():
    # A short, stiff mane: a row of short, stiff spikes of bone stand up
    # along the front of the mane.
    mane_strands("bone_dodge_spikes", lambda f, x, y: "#F4F0E4" if y == 0 else "#B8B0A0",
                 [(-1, y, 5) for y in (-10, -7, -4, -1, 2, 5)], size=(2, 3, 1), turn=-120)


# The tail.

def cold_current():
    return []


def cold_current_model():
    # A long, wet tail: three long strands of dark, wet hair hang from the
    # base of the tail, each of a different length, with a wet shine. The
    # bone tail leans back by 30 degrees, so the strands turn back by 25
    # degrees to hang down.
    im = model_image(32, 32)

    def strand(f, x, y):
        return "#9AC8D8" if (x + y) % 5 == 0 else "#2A3A44" if y % 2 else "#344A56"
    paint_box(im, 0, 0, 1, 13, 1, strand)
    parts = [{"bone": "tail", "uv": [0, 0], "from": [x, 0, 1.5], "size": [1, length, 1],
              "pivot": [x + 0.5, 0, 2], "rotation": [-25, 0, 0]}
             for x, length in ((-1.5, 11), (-0.5, 13), (0.5, 10))]
    save_model("cold_current_tail", im, parts)


def night_run():
    return []


def night_run_model():
    # A pale, wispy tail: thin, pale wisps fan out from the base of the tail
    # to the sides, each one fading at its end.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 7, 1, lambda f, x, y: "#E8F0FF" if y < 4 else "#A8B8D0")
    parts = [{"bone": "tail", "uv": [0, 0], "from": [x, 0, 2], "size": [1, 7, 1],
              "pivot": [x + 0.5, 0, 2.5], "rotation": [0, 0, turn]} for x, turn in ((-1.5, 35), (0.5, -35), (-0.5, 0))]
    save_model("night_run_tail", im, parts)


# The legs.

def shore_bound():
    # Wet, dark legs: the high and the middle of each leg are dark and wet,
    # with bright, wet shines.
    wet = Mark("#3A4A50")
    shine = Mark("#C8E8F0")
    leg_put(wet, SIDES, (0, 2, 3, 5), lambda x, y, f, c, r: (c + r) % 4 != 0)
    leg_put(shine, SIDES, (0, 2, 3, 5), lambda x, y, f, c, r: (c + r) % 4 == 0)
    return [wet, shine]


def grounded_bones():
    # Copper bands on the legs: two copper bands go round each leg, with a
    # green patina on them.
    copper = Mark("#D07840")
    patina = Mark("#40B898")
    leg_put(copper, SIDES, (1, 4), lambda x, y, f, c, r: c != 2)
    leg_put(patina, SIDES, (1, 4), lambda x, y, f, c, r: c == 2)
    return [copper, patina]


def golem_brace():
    # Iron gray bands: a wide brace of iron gray goes round each shin, with
    # a pale top edge and dark rivets.
    iron = Mark("#8A9098")
    light = Mark("#D0D6DC")
    rivet = Mark("#2A2E34")
    leg_put(light, SIDES, (6,))
    leg_put(iron, SIDES, (7, 8), lambda x, y, f, c, r: not (c == 1 and r == 7))
    leg_put(rivet, SIDES, (7,), lambda x, y, f, c, r: c == 1)
    return [iron, light, rivet]


def deep_stride():
    # Blue gray hooves: the sides of each hoof are blue gray, with a pale
    # shine.
    blue = Mark("#5A6A80")
    shine = Mark("#B8C8DC")
    leg_put(blue, ("right", "left"), (9, 10), lambda x, y, f, c, r: not (r == 9 and c == 1))
    leg_put(shine, ("right", "left"), (9,), lambda x, y, f, c, r: c == 1)
    return [blue, shine]


def weightless():
    # Light, thin hooves: the front, the back, and the bottom of each hoof
    # are light and pale, as thin as glass.
    pale = Mark("#E8F0F4")
    light = Mark("#FFFFFF")
    leg_put(pale, ("front", "back"), (9, 10), lambda x, y, f, c, r: (c + r) % 2 == 0)
    leg_put(light, ("front", "back"), (9, 10), lambda x, y, f, c, r: (c + r) % 2 == 1)
    put(pale, LEG, ("bottom",))
    return [pale, light]


def heavy_bones():
    # The thick, heavy bones are the gene model heavy_bones_legs.
    return []


def heavy_bones_model():
    # Thick, heavy bones: a copy of each leg, with the texture of the mob and
    # its marks, 0.6 pixel larger on each side.
    parts = []
    for bone in LEG_BONES:
        left = bone.startswith("left")
        z = -1.9 if "front" in bone else -1
        parts.append({"bone": bone, "uv": [48, 21], "from": [-3 if left else -1, -1.01, z], "size": [4, 11, 4],
                      "inflate": 0.6, "mirror": left})
    save_mob_model("heavy_bones_legs", parts)


def ghost_stride():
    # The long, thin legs are the gene model ghost_stride_struts.
    return []


def ghost_stride_model():
    # Long, thin legs: a thin strut of pale bone goes up from the top of each
    # leg along the flank, so that each leg looks long. It stands 1 pixel out
    # from the side of the body.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 9, 1, lambda f, x, y: "#F0ECE0" if y % 3 else "#B8B4A8")
    parts = []
    for bone in LEG_BONES:
        left = bone.startswith("left")
        z = 2 if "hind" in bone else -1.9
        parts.append({"bone": bone, "uv": [0, 0], "from": [1 if left else -2, -10, z], "size": [1, 9, 1]})
    save_model("ghost_stride_struts", im, parts)


GENES = {
    "storm_herald": storm_herald, "grounded_bones": grounded_bones, "long_wait": long_wait,
    "wide_trap": wide_trap, "charged_riders": charged_riders, "storm_kin": storm_kin,
    "deep_stride": deep_stride, "kelp_mane": kelp_mane, "coral_bones": coral_bones,
    "cold_current": cold_current, "shore_bound": shore_bound, "trident_ribs": trident_ribs,
    "heavy_bones": heavy_bones, "blast_ribs": blast_ribs, "rib_gaps": rib_gaps,
    "blessed_skull": blessed_skull, "bone_mend": bone_mend, "golem_brace": golem_brace,
    "bone_barding": bone_barding, "squire_saddle": squire_saddle, "faithful_bones": faithful_bones,
    "storm_cloak": storm_cloak, "last_ride": last_ride, "helm_bond": helm_bond,
    "ghost_stride": ghost_stride, "wraith_veil": wraith_veil, "night_run": night_run,
    "bone_dodge": bone_dodge, "weightless": weightless, "rattle_flight": rattle_flight,
}

MODEL_GENES = {
    "rattle_flight": (rattle_flight_model, "rattle_flight_ribs"),
    "storm_cloak": (storm_cloak_model, "storm_cloak_cloth"),
    "squire_saddle": (squire_saddle_model, "squire_saddle_saddle"),
    "wide_trap": (wide_trap_model, "wide_trap_eyes"),
    "storm_herald": (storm_herald_model, "storm_herald_mane"),
    "kelp_mane": (kelp_mane_model, "kelp_mane_kelp"),
    "last_ride": (last_ride_model, "last_ride_ribbons"),
    "bone_dodge": (bone_dodge_model, "bone_dodge_spikes"),
    "cold_current": (cold_current_model, "cold_current_tail"),
    "night_run": (night_run_model, "night_run_tail"),
    "heavy_bones": (heavy_bones_model, "heavy_bones_legs"),
    "ghost_stride": (ghost_stride_model, "ghost_stride_struts"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="skeleton horse genes")
