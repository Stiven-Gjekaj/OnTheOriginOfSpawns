"""Make the marks and the gene models of the parched genes.

Usage: python3 tools/make_parched_marks.py

The parched (the desert skeleton) has a 64x64 texture and its own region
map (textures/region/parched.png, from make_parched_region_map.py). The top
half of the texture has the bones of the skeleton, with the right arm at
uv (40, 16) and the left arm on its own uv (56, 16). The bottom half has
wraps of cloth over the bones: the head wrap at uv (0, 32), the body wrap
at uv (16, 48), the right sleeve (3x12x3) at uv (42, 33), the left sleeve at
uv (40, 48), and the leg wraps (3x12x3) at uv (0, 49) and (4, 49). The arms
hang from x -5.5 and x 5.5. The parched holds a bow in the right hand.

What the wraps leave in view: most of the face (the wrap covers the brow),
the middle of the chest and of the back, the forearms and the hands (arm
rows 8 to 11), and the shins and the feet (leg rows 8 to 11). A mark that
must read goes on the bones and on the wrap over them.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- textures/gene_marks/<gene>_<name>.png, the masks of the erase layers.
- gene_visuals/<gene>.json, with one layer for each pattern and mask.
- The gene models in MODELS, with their textures.

The script prints the pixels that two parched genes share.

Pixel plan for the Enfeeblers.
- Sap has gray, drained streaks on the bow arm (the right arm): every other
  column of the bones and of the sleeve, rows 1 to 8. The hands stay free.
- Fumble has long, crooked fingers: pale finger lines with dark gaps on arm
  rows 9 to 11 of both arms, and on the bottom of the arms.
- Exploit has a thin, pale ring round each eye socket (y 11 and 13, and
  beside the sockets), with dark sockets.
- Curse Cloud has gray dust in the rib cage: specks on the front ribs (y 21
  to 27) and on the wrap of the chest.
- Unnerving Rattle has loose teeth: crooked pale teeth with dark gaps on the
  mouth (y 14), and one tooth that has dropped (y 15).
- Withering Glare has a dull gray light in the eye sockets.

Pixel plan for the Windcarvers.
- Long Wind has a band of pale linen round the head wrap over the brow (y
  40 to 42), and two strips of it blow back from the back of the skull,
  the gene model long_wind_wrap.
- Gust Shot has pale cloth strips wound round the bow arm (the right arm):
  on the sleeve, rows 1 to 8, and on the bones of the forearm.
- Tailwind has sand-scoured shins: polished, pale bone with fine scratches
  on leg rows 7 to 11, and on the lower edge of the leg wraps.
- Wind Dodge has a faded teal sash across the body, from the right shoulder
  to the left hip, on the body wrap and on the ribs, with a loose end at
  the left hip.
- Whirlwind Volley: a quiver of woven reed with feathered arrows on the
  back, the gene model whirlwind_volley_quiver.
- Quickwind has worn, polished finger tips: arm rows 10 and 11 and the
  bottom of both arms, over the Fumble fingers (order 1).

Pixel plan for the Vultures.
- Carrion Eye has red rings round the eye sockets (y 11 and 13, and
  beside them), with dark sockets.
- Circler: a ruff of dark feathers round the neck, on the shoulders, the
  gene model circler_ruff.
- Gorge: a long, hooked jaw of bone like a beak, the gene model
  gorge_beak.
- Flock has black feathers with gray edges down the spine: on the spine (x
  35 and 36, y 20 to 29) and down the middle of the body wrap of the back
  (x 34 to 37).
- Glide: wings of bone with dark feathers under the arms, the gene model
  glide_wings.
- Watcher has a bald, wrinkled crown: the head wrap is gone on the top of
  the head and over the brow (y 32 to 41 of the wrap), and the bone of the
  crown and the brow has dark wrinkles.

Pixel plan for the Scorpions.
- Stinger: a curled scorpion tail on the back, the gene model
  stinger_tail.
- Pincers: claws like pincers on the hands, the gene model pincers_claws.
- Carapace: a dark shell of plates over the ribs, the gene model
  carapace_shell.
- Sand Hide has sand-colored patches on the bones (order -1).
- Venom Sac has a swollen yellow sac at the front of the pelvis: on the
  pelvis (x 21 to 26, y 29 to 31), on the box of the pelvis, and on the
  lower edge of the body wrap.
- Tail Whip has pale spikes down the lower back: on the spine (y 25 to 31)
  and on the body wrap of the back (y 56 to 63).

Pixel plan for the Fossils.
- Petrified has a gray stone texture on all the bone (order -1).
- Amber Heart: a stone of amber in the rib cage, the gene model
  amber_heart_stone, which glows.
- Bedrock Bones has dark stone at the joints of the legs: the knees (leg
  rows 5 and 6, on the bones and on the leg wraps) and the ankles (row 10).
- Stoneskin has short cracks filled with stone on the skull: on the cheeks
  (face rows 10 to 13, at the sides), on the head wrap over them, and on
  the top of the head.
- Sediment has layers of packed sand on the feet: bands of sand colors on
  leg rows 8 to 11 and on the bottom of the legs.
- Last Stand has one broken rib: a gap in the front rib (x 23, y 22), with
  pale, jagged ends and a piece that has dropped (y 24). A tear in the body
  wrap over it (x 21 to 25, y 54 to 56) shows the break.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_skeleton_marks import BACK, FRONT, INNER, OUTER, SIDES, leg
from make_stray_marks import changed_pixels, erase

REGION_MAP = "originofspawns:textures/region/parched.png"

BONE = ("#E8E0C8", "#B8AE98", "#6E6656")
SAND = ("#D8C090", "#B89A68", "#8A7048")
DARK = "#2A2218"

# The x offset of each side face of a 3x12x3 wrap from its uv.
WRAP_SIDES = {OUTER: 0, FRONT: 3, INNER: 6, BACK: 9}


def arm(face, col, row, left=False):
    """Return the texture pixel of a pixel on a side face of an arm bone.

    The right arm is at uv (40, 16), the left arm at uv (56, 16).
    """
    return ((56 if left else 40) + face + col, 18 + row)


def sleeve(face, col, row, left=False):
    """Return a pixel of a side face of a sleeve (3 pixels wide).

    The right sleeve is at uv (42, 33), the left sleeve at uv (40, 48). On
    the left sleeve, the outer face is the left face of the box.
    """
    if left:
        face = {OUTER: INNER, INNER: OUTER}.get(face, face)
        return (40 + WRAP_SIDES[face] + col, 51 + row)
    return (42 + WRAP_SIDES[face] + col, 36 + row)


def leg_wrap(face, col, row):
    """Return the pixels of a side face of the leg wraps, of both legs."""
    return [(0 + WRAP_SIDES[face] + col, 52 + row), (4 + WRAP_SIDES[face] + col, 52 + row)]


def both_arms(face, col, row):
    return [arm(face, col, row), arm(face, col, row, left=True)]


def both_sleeves(face, col, row):
    return [sleeve(face, col, row), sleeve(face, col, row, left=True)]


def sap():
    # Gray, drained streaks on the bow arm, the right arm: every other
    # column of the bones and of the sleeve is a dull gray, rows 1 to 8,
    # as if the life ran out of the arm. The hand stays free for Fumble.
    gray = Mark("#9A9894")
    dull = Mark("#6E6C68")
    for face in SIDES:
        for row in range(1, 9):
            gray.put(arm(face, 0, row))
            if row > 3:
                dull.put(arm(face, 1, row), 200)
        for col in (0, 2):
            for row in range(1, 9):
                gray.put(sleeve(face, col, row))
    return [gray, dull]


def fumble():
    # Long, crooked fingers: on each face of the hands of both arms, one
    # long pale finger bone runs down from the knuckle (row 9) to the tip
    # (row 11), and it bends to the side at the tip. A dark gap is beside
    # it. The bottom of the arm has the finger tips.
    finger = Mark(BONE[0])
    gap = Mark(DARK)
    for face in SIDES:
        col = face // 2 % 2
        for row, c in ((9, col), (10, col), (11, 1 - col)):
            for p in both_arms(face, c, row):
                finger.put(p)
            for p in both_arms(face, 1 - c, row):
                gap.put(p) if row > 9 else None
    for u in (40, 56):
        finger.put((u + 4, 16)).put((u + 5, 17))
        gap.put((u + 5, 16)).put((u + 4, 17))
    return [finger, gap]


def exploit():
    # A thin, pale ring round each eye socket: pale bone pixels over and
    # under each socket (y 11 and 13) and beside it, and the sockets
    # themselves go dark, so the ring reads as a cold, watching eye.
    ring = Mark("#F4F0E4")
    dark = Mark("#141008")
    for outer, inner, side in ((9, 10, 8), (14, 13, 15)):
        dark.put((outer, 12)).put((inner, 12))
        for x in (outer, inner):
            ring.put((x, 11)).put((x, 13))
        ring.put((side, 12))
    return [ring, dark]


def curse_cloud():
    # Gray dust in the rib cage: specks of gray dust on the front ribs (y
    # 21 to 27) and on the wrap of the chest, thicker low down, as if the
    # dust settles in the cage.
    dust = Mark("#8A8884")
    pale = Mark("#B8B6B0")
    for x in range(20, 28):
        for y in range(21, 28):
            n = noise100(x, y, 311)
            if n < 20 + 5 * (y - 21):
                (pale if n % 3 == 0 else dust).put((x, y))
        for y in range(52, 60):
            if noise100(x, y, 312) < 30:
                dust.put((x, y))
    return [dust, pale]


def unnerving_rattle():
    # Loose teeth that rattle: the mouth (y 14) has crooked, pale teeth
    # with dark gaps between them, set at different heights, and one tooth
    # has dropped down (y 15).
    tooth = Mark(BONE[0])
    gap = Mark(DARK)
    for x in range(9, 15):
        (tooth if x % 2 else gap).put((x, 14))
    tooth.put((10, 13)).put((12, 15))
    gap.put((13, 13))
    return [tooth, gap]


def withering_glare():
    # A dull gray light in the eye sockets: the sockets have a flat, dull
    # gray in place of the pale yellow eyes, with a darker gray on their
    # inner side.
    dull = Mark("#8A8A86")
    dark = Mark("#5A5A56")
    for outer, inner in ((9, 10), (14, 13)):
        dull.put((outer, 12))
        dark.put((inner, 12))
    return [dull, dark]



def long_wind():
    # A band of pale linen round the head wrap, over the brow (y 40 and 41
    # of the head wrap, on all sides), with a dark edge under it, so that
    # the wrap shows from the front. The strips that blow back from it are
    # the gene model long_wind_wrap.
    band = Mark("#F2E6C2")
    edge = Mark("#8A7048")
    for x in range(0, 32):
        band.put((x, 40)).put((x, 41))
        edge.put((x, 42))
    return [band, edge]


def long_wind_model():
    # Two strips of cloth that blow back from the skull: a knot of cloth on
    # the back of the head wrap, and two strips that stream back from it in
    # the wind, a little apart and a little down, with torn ends. On the
    # head bone, the back of the head wrap is at z 4.2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def strip(face, x, y):
        if face in ("front", "back"):
            return SAND[1]
        if face in ("right", "left") and x == 5 and y == 0:
            return None
        return SAND[0] if x % 3 else SAND[2]

    # The knot: 2x2x1 at uv (0, 0). A strip: 2x1x6 at uv (8, 0).
    paint_box(im, 0, 0, 2, 2, 1, lambda face, x, y: SAND[1])
    paint_box(im, 8, 0, 2, 1, 6, strip)
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-1, -6, 4], "size": [2, 2, 1]},
        {"bone": "head", "uv": [8, 0], "from": [-2, -6, 4.6], "size": [2, 1, 6],
         "pivot": [-1, -5.5, 4.6], "rotation": [-10, -12, 0]},
        {"bone": "head", "uv": [8, 0], "from": [0, -5, 4.6], "size": [2, 1, 6],
         "pivot": [1, -4.5, 4.6], "rotation": [-18, 10, 0]},
    ]
    save_model("long_wind_wrap", im, parts)


def gust_shot():
    # Pale cloth strips wound round the bow arm, the right arm: bands of
    # pale cloth at a slant on the sleeve (rows 1 to 8), each with a dark
    # edge under it, and the same bands on the bones of the forearm, where
    # the sleeve ends.
    band = Mark("#F2E6C2")
    edge = Mark("#6E5A3A")
    for face in SIDES:
        shift = face // 2
        for col in (0, 1, 2):
            for row in (1, 4, 7):
                band.put(sleeve(face, col, row + (col + shift) % 2))
                edge.put(sleeve(face, col, row + 1 + (col + shift) % 2))
        for col in (0, 1):
            band.put(arm(face, col, 8 + (col + shift) % 2))
    return [band, edge]


def tailwind():
    # Sand-scoured shins: the bones of the shins are worn to a smooth, pale
    # polish (leg rows 7 to 11) with fine scratches across them, and the
    # lower edge of the leg wraps is worn pale too.
    polish = Mark("#F2ECDC")
    scratch = Mark("#A09480")
    for face in SIDES:
        for col in (0, 1):
            for row in range(7, 12):
                (scratch if (row + col) % 3 == 0 else polish).put(leg(face, col, row))
        for col in (0, 1, 2):
            for p in leg_wrap(face, col, 6) + leg_wrap(face, col, 7):
                polish.put(p, 230)
    return [polish, scratch]


SASH = ("#3E8A8E", "#6AB0B0", "#2A5E62")


def wind_dodge():
    # A faded teal sash across the body, from the right shoulder down to the
    # left hip, 2 pixels wide, with a pale edge: on the wrap of the body, on
    # the ribs under it (where the wrap is open), and on the back. A loose
    # end hangs from the knot at the left hip, on the side of the wrap.
    sash = Mark(SASH[0])
    pale = Mark(SASH[1])
    for top in (20, 52):
        for y in range(top, top + 12):
            step = (y - top) * 7 // 11
            for x0, sign in ((20, 1), (39, -1)):
                x = x0 + sign * step
                sash.put((x, y)).put((x + sign, y))
                if 16 <= x - sign < 40:
                    pale.put((x - sign, y))
    end = Mark(SASH[2])
    for y in range(58, 64):
        end.put((28, y)).put((29, y))
    return [sash, pale, end]


def whirlwind_volley():
    # The quiver is the gene model whirlwind_volley_quiver.
    return []


def whirlwind_volley_model():
    # A quiver of woven reed on the back, over the right shoulder, full of
    # arrows with long feathers of black and white bands. Its front lies on
    # the body wrap of the back (z 2.05). On the body bone, the back is at
    # z 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def reed(face, x, y):
        if face == "top":
            return "#4A3820"
        return "#C8A868" if (x + y) % 2 else "#9A7A48"

    def feather(face, x, y):
        return "#F2F0EA" if y % 2 == 0 else "#202020"

    # The quiver: 3x8x2 at uv (0, 0). A feathered arrow: 1x3x1 at uv (12, 0).
    paint_box(im, 0, 0, 3, 8, 2, reed)
    paint_box(im, 12, 0, 1, 3, 1, feather)
    lean = {"pivot": [-2.5, 5, 3], "rotation": [0, 0, -25]}
    parts = [dict({"bone": "body", "uv": [0, 0], "from": [-4, 1, 2.05], "size": [3, 8, 2]}, **lean)]
    for x, z, y in ((-4, 2.05, -1.5), (-3, 3.05, -2.4), (-2, 2.05, -1.9), (-3.5, 2.6, -2.8), (-2.5, 3.05, -1.2)):
        parts.append(dict({"bone": "body", "uv": [12, 0], "from": [x, y, z], "size": [1, 3, 1]}, **lean))
    save_model("whirlwind_volley_quiver", im, parts)


def quickwind():
    # Worn, polished finger tips: the fingers (arm rows 10 and 11) and the
    # bottom of both arms are worn smooth and bright, with a shine. They go
    # over the Fumble fingers (order 1).
    tip = Mark("#FFF8E8", order=1)
    shine = Mark("#FFFFFF", order=1)
    for face in SIDES:
        for col in (0, 1):
            for p in both_arms(face, col, 11) + both_arms(face, col, 10):
                tip.put(p)
    for u in (40, 56):
        shine.put((u + 4, 16)).put((u + 5, 17))
        tip.put((u + 5, 16)).put((u + 4, 17))
    return [tip, shine]


FEATHER = ("#2A2220", "#4A3A30", "#6A5A48", "#1A1614")


def carrion_eye():
    # Red rings round the eye sockets, as the eyes of a carrion bird: red
    # pixels over and under each socket (y 11 and 13) and beside it, and
    # dark sockets with a small red core.
    ring = Mark("#B82020")
    dark = Mark("#140A08")
    core = Mark("#E04030")
    for outer, inner, side in ((9, 10, 8), (14, 13, 15)):
        dark.put((outer, 12))
        core.put((inner, 12))
        for x in (outer, inner):
            ring.put((x, 11)).put((x, 13))
        ring.put((side, 12))
    return [ring, dark, core]


def circler():
    # The ruff is the gene model circler_ruff.
    return []


def circler_model():
    # A ruff of dark feathers round the neck, as a vulture has: a ring of
    # feathers that sits on the shoulders, round the bottom of the head,
    # with a ragged lower edge and paler tips. The middle of its top is open
    # for the neck. On the body bone, the top of the body is at y 0, and the
    # head is from x -4 to 4 and from z -4 to 4.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def ruff(face, x, y):
        if face == "top":
            return None if 1 <= x <= 8 and 1 <= y <= 8 else FEATHER[1]
        if face == "bottom":
            return FEATHER[3]
        if y == 1 and x % 2:
            return None
        return FEATHER[2] if y == 0 and x % 3 == 0 else FEATHER[0] if (x + y) % 2 else FEATHER[1]

    # The ruff: 10x2x10 at uv (0, 0).
    paint_box(im, 0, 0, 10, 2, 10, ruff)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-5, -0.5, -5], "size": [10, 2, 10]}]
    save_model("circler_ruff", im, parts)


def gorge():
    # The beak is the gene model gorge_beak.
    return []


def gorge_model():
    # A long, hooked jaw of bone like a beak: a jaw that juts forward from
    # the mouth, and a dark hook at its tip that bends down, with a dark
    # edge where the jaw closes. On the head bone, the face is at z -4, and the
    # mouth is at y -2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def jaw(face, x, y):
        if face == "bottom":
            return BONE[2]
        return BONE[0] if face == "top" else DARK if y == 1 else BONE[1]

    def hook(face, x, y):
        return DARK if y == 1 else BONE[2]

    # The jaw: 4x2x2 at uv (0, 0). The hook: 2x2x1 at uv (12, 0).
    paint_box(im, 0, 0, 4, 2, 2, jaw)
    paint_box(im, 12, 0, 2, 2, 1, hook)
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-2, -3, -6], "size": [4, 2, 2]},
        {"bone": "head", "uv": [12, 0], "from": [-1, -2.5, -7], "size": [2, 2, 1]},
    ]
    save_model("gorge_beak", im, parts)


def flock():
    # Black feathers down the spine: feathers in a row of chevrons, black
    # with a gray edge, on the spine (x 35 and 36, y 20 to 29) and down the
    # middle of the body wrap of the back (x 34 to 37, y 48 to 63).
    black = Mark(FEATHER[3])
    edge = Mark("#7A7268")
    for top, bottom in ((20, 30), (52, 64)):
        for y in range(top, bottom):
            black.put((35, y)).put((36, y))
            if y % 2 == 0:
                edge.put((34, y)).put((37, y))
    for x in range(34, 38):
        for y in range(48, 52):
            (black if x in (35, 36) else edge).put((x, y))
    return [black, edge]


def glide():
    # The wings are the gene model glide_wings.
    return []


def glide_model():
    # Wings of bone with dark feathers under the arms: on each arm, a strut
    # of bone along the back of the arm, and a sheet of dark feathers that
    # hangs from it and spreads back and out, with ragged ends. On an arm
    # bone, the arm is from z -1 to 1, and from y -2 to 10.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def sheet(face, x, y):
        if face not in ("right", "left"):
            return None
        if y > 6 + x % 3:
            return None
        if y == 0:
            return BONE[0]
        return FEATHER[2] if (x + y) % 4 == 0 else FEATHER[0] if x % 2 else FEATHER[1]

    # The sheet: 0x9x4 at uv (0, 0).
    paint_box(im, 0, 0, 0, 9, 4, sheet)
    parts = [
        {"bone": "right_arm", "uv": [0, 0], "from": [0, -1, 1], "size": [0, 9, 4],
         "pivot": [0, -1, 1], "rotation": [0, -25, 0]},
        {"bone": "left_arm", "uv": [0, 0], "from": [0, -1, 1], "size": [0, 9, 4],
         "pivot": [0, -1, 1], "rotation": [0, 25, 0]},
    ]
    save_model("glide_wings", im, parts)


def watcher():
    # A bald, wrinkled crown, as the head of a vulture: the head wrap is
    # gone from the top of the head and from over the brow (y 32 to 41 of
    # the wrap), and the bone of the crown (the top of the head) and of the
    # brow (face rows 8 and 9) has dark wrinkles across it.
    bald = [(x, y) for x in range(8, 16) for y in range(32, 40)]
    bald += [(x, y) for x in range(0, 32) for y in (40, 41)]
    wrinkle = Mark("#5A4E3E")
    skin = Mark("#C8B8A0")
    for x in range(8, 16):
        for y in range(0, 8):
            if y % 2 == 1 and (x + y // 2) % 4 != 0:
                wrinkle.put((x, y))
            else:
                skin.put((x, y), 230)
        wrinkle.put((x, 8 + x % 2))
    return [erase("watcher", "bald", bald), skin, wrinkle]


CHITIN = ("#8A5A20", "#6A4218", "#C08A40", "#1A1008")


def chitin(face, x, y):
    """Return the color of a pixel of the shell of a scorpion."""
    if face == "top":
        return CHITIN[2] if (x + y) % 3 == 0 else CHITIN[0]
    return CHITIN[1] if face == "bottom" or (x + y) % 2 else CHITIN[0]


def stinger():
    # The tail is the gene model stinger_tail.
    return []


def stinger_model():
    # A curled scorpion tail on the back: a chain of shell segments that
    # starts on the lower back, rises up behind the head, and curls forward
    # over the top of the head, with a black sting at its tip that points
    # down to the front. Each segment overlaps the one before it, so the
    # tail holds together. The segments behind the head stay out of it (z
    # 4.3 and more), and the curl is over the head (y -10 and less). On the
    # body bone, the back is at z 2, and the body is from y 0 to 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def sting(face, x, y):
        return CHITIN[3] if y else CHITIN[2]

    # A segment: 2x2x2 at uv (0, 0). The sting: 1x2x1 at uv (8, 0).
    paint_box(im, 0, 0, 2, 2, 2, chitin)
    paint_box(im, 8, 0, 1, 2, 1, sting)
    parts = []
    for y, z in ((8, 1.8), (6, 2.8), (4, 3.6), (2, 4.3), (0, 4.4), (-2, 4.4), (-4, 4.4), (-6, 4.4),
                 (-8, 4.3), (-10, 3.4), (-10.8, 1.8), (-10.6, 0.2)):
        parts.append({"bone": "body", "uv": [0, 0], "from": [-1, y, z], "size": [2, 2, 2]})
    parts.append({"bone": "body", "uv": [8, 0], "from": [-0.5, -9, -0.6], "size": [1, 2, 1],
                  "pivot": [0, -9, 0], "rotation": [-35, 0, 0]})
    save_model("stinger_tail", im, parts)


def pincers():
    # The claws are the gene model pincers_claws.
    return []


def pincers_model():
    # Claws like pincers on the hands: on each hand, a shell base round the
    # wrist and two thick prongs under it that open a little, as the claw
    # of a scorpion. On an arm bone, the hand ends at y 10.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def prong(face, x, y):
        return CHITIN[3] if y == 2 else chitin(face, x, y)

    # The base: 2x1x2 at uv (0, 0). A prong: 1x3x1 at uv (8, 0).
    paint_box(im, 0, 0, 2, 1, 2, chitin)
    paint_box(im, 8, 0, 1, 3, 1, prong)
    parts = []
    for bone in ("right_arm", "left_arm"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1, 8, -1], "size": [2, 1, 2], "inflate": 0.3})
        parts.append({"bone": bone, "uv": [8, 0], "from": [-0.5, 9, -1.3], "size": [1, 3, 1],
                      "pivot": [0, 9, -0.8], "rotation": [-15, 0, 0]})
        parts.append({"bone": bone, "uv": [8, 0], "from": [-0.5, 9, 0.3], "size": [1, 3, 1],
                      "pivot": [0, 9, 0.8], "rotation": [15, 0, 0]})
    save_model("pincers_claws", im, parts)


def carapace():
    # The shell is the gene model carapace_shell.
    return []


def carapace_model():
    # A dark shell over the ribs: three plates of dark shell, one over the
    # other, each a little narrower, as the back of a scorpion, with a
    # glossy upper edge. They lie on the body wrap of the front (z -2.05).
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    dark = ("#4A3420", "#3A2818", "#8A6A40")

    def plate(face, x, y):
        if face == "front":
            return dark[2] if y == 0 else dark[0] if x % 3 else dark[1]
        return dark[1]

    # A wide plate: 7x3x1 at uv (0, 0). A narrow plate: 5x2x1 at uv (0, 6).
    paint_box(im, 0, 0, 7, 3, 1, plate)
    paint_box(im, 0, 6, 5, 2, 1, plate)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-3.5, 1, -3.05], "size": [7, 3, 1]},
        {"bone": "body", "uv": [0, 0], "from": [-3.5, 3.6, -3.15], "size": [7, 3, 1]},
        {"bone": "body", "uv": [0, 6], "from": [-2.5, 6.2, -3.05], "size": [5, 2, 1]},
    ]
    save_model("carapace_shell", im, parts)


def sand_hide():
    # Sand-colored patches on the bones, as the skin of a desert beast: wide
    # patches of sand color with darker sand edges on all the bone, under
    # the marks of other genes (order -1).
    patch = Mark(SAND[0], order=-1, materials=["bone"], size=16)
    edge = Mark(SAND[2], order=-1, materials=["bone"], size=16)
    for x in range(16):
        for y in range(16):
            n = noise100(x // 2, y // 2, 321)
            if n < 40:
                patch.put((x, y))
            elif n < 48:
                edge.put((x, y))
    return [patch, edge]


def venom_sac():
    # A swollen yellow sac at the front of the pelvis: a round, bright
    # yellow sac with a pale shine and a dark rim, on the pelvis (x 21 to
    # 26, y 29 to 31), on the front of the box of the pelvis, and on the
    # lower edge of the body wrap over it.
    sac = Mark("#D8C030")
    shine = Mark("#F8F0A0")
    rim = Mark("#6A5410")
    for x in range(21, 27):
        for y in (30, 31):
            sac.put((x, y))
        (rim if x in (21, 26) else sac).put((x, 29))
    shine.put((23, 30))
    for x in range(32, 40):
        (sac if 33 <= x <= 38 else rim).put((x, 4))
    shine.put((34, 4))
    for x in range(21, 27):
        for y in (61, 62, 63):
            (rim if x in (21, 26) and y == 61 else sac).put((x, y))
    shine.put((23, 62))
    return [sac, shine, rim]


def tail_whip():
    # Pale spikes down the lower back: sharp spikes of bone, each a pale
    # point on a dark base, on the spine (y 25 to 31) and down the middle
    # of the body wrap of the back (y 56 to 63). They go over the Flock
    # feathers (order 1).
    spike = Mark(BONE[0], order=1)
    base = Mark(DARK, order=1)
    for y in range(25, 32, 2):
        spike.put((35, y)).put((36, y))
        base.put((35, y + 1)).put((36, y + 1)) if y < 31 else None
    for y in range(56, 64, 2):
        spike.put((35, y)).put((36, y))
        base.put((34, y + 1)).put((37, y + 1)) if y < 63 else None
    return [spike, base]


STONE = ("#9A9A96", "#6A6A66", "#C0C0BC", "#2E2C2C")


def petrified():
    # A gray stone texture on all the bone, as a fossil: a cool gray with
    # dark and pale specks of stone, under the marks of other genes (order
    # -1).
    stone = Mark(STONE[0], alpha=230, order=-1, materials=["bone"], size=16).fill()
    dark = Mark(STONE[1], order=-1, materials=["bone"], size=16)
    pale = Mark(STONE[2], order=-1, materials=["bone"], size=16)
    for x in range(16):
        for y in range(16):
            n = noise100(x, y, 331)
            if n < 18:
                dark.put((x, y))
            elif n > 88:
                pale.put((x, y))
    return [stone, dark, pale]


AMBER = ("#E8A030", "#FFD070", "#A0601A")


def amber_heart():
    # The stone is the gene model amber_heart_stone.
    return []


def amber_heart_model():
    # A stone of amber in the rib cage: a clear, warm stone set in the
    # middle of the chest, where the body wrap is open, with a bright core
    # and darker facets. It sits in the front of the ribs and stands out of
    # them a little, so it shows from the front. It glows. On the body
    # bone, the front of the body is at z -2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def stone(face, x, y):
        if face == "front" and x == 1 and y == 1:
            return AMBER[1]
        return AMBER[2] if (x + y) % 3 == 0 else AMBER[0]

    # The stone: 3x3x2 at uv (0, 0).
    paint_box(im, 0, 0, 3, 3, 2, stone)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-1.5, 2, -2.6], "size": [3, 3, 2], "glow": True}]
    save_model("amber_heart_stone", im, parts)


def bedrock_bones():
    # Dark stone at the joints of the legs, as bedrock: the knees (leg rows
    # 5 and 6) on the bones and on the leg wraps over them, and the ankles
    # (row 10), dark gray with pale specks.
    rock = Mark(STONE[3])
    speck = Mark(STONE[1])
    for face in SIDES:
        for col in (0, 1):
            for row in (5, 6, 10):
                (speck if (col + row + face) % 3 == 0 else rock).put(leg(face, col, row))
        for col in (0, 1, 2):
            for row in (5, 6):
                for p in leg_wrap(face, col, row):
                    (speck if (col + row) % 3 == 0 else rock).put(p)
    return [rock, speck]


def stoneskin():
    # Short cracks filled with stone on the skull: small cracks on the
    # cheeks (at the sides of the face, rows 10 to 13), each a dark line
    # with gray stone in it, the same cracks on the head wrap over them, and
    # two on the top of the head. The cracks are short, so the face does
    # not get long streaks.
    crack = Mark("#3A3630")
    stone = Mark(STONE[2])
    cracks = (((8, 10), (8, 11), (9, 11)), ((15, 12), (15, 13), (14, 13)))
    for top in (0, 32):
        for line in cracks:
            for x, y in line:
                crack.put((x, y + top))
            x, y = line[1]
            stone.put((x + (1 if x == 8 else -1), y + top))
    for x, y in ((10, 2), (11, 3), (13, 5), (12, 6)):
        crack.put((x, y)).put((x, y + 32))
    stone.put((11, 2)).put((12, 5))
    return [crack, stone]


def sediment():
    # Layers of packed sand on the feet: bands of sand colors, one over the
    # other, on leg rows 8 to 11 of all the faces, and packed sand on the
    # bottom of the legs.
    bands = {8: "#C8A060", 9: "#E0C890", 10: "#9A7A50", 11: "#E8D8B0"}
    marks = {row: Mark(color) for row, color in bands.items()}
    for face in SIDES:
        for col in (0, 1):
            for row, mark in marks.items():
                mark.put(leg(face, col, row))
    for x in (4, 5):
        for y in (16, 17):
            marks[10].put((x, y))
    return list(marks.values())


def last_stand():
    # One broken rib: the front rib (y 22) has a gap at x 23, with pale,
    # jagged ends on each side of it (x 22 and 24), and a piece of the rib
    # has dropped below the gap (y 24). A tear in the body wrap over it (x
    # 21 to 25, y 54 to 56) lets the break show.
    end = Mark("#F4EEDC")
    piece = Mark(BONE[1])
    dark = Mark(DARK)
    end.put((22, 22)).put((24, 22))
    dark.put((23, 23))
    piece.put((23, 24))
    tear = [(x, y) for x in range(21, 26) for y in range(54, 57)]
    return [erase("last_stand", "gap", [(23, 22)] + tear), end, dark, piece]


GENES = {
    "sap": sap,
    "fumble": fumble,
    "exploit": exploit,
    "curse_cloud": curse_cloud,
    "unnerving_rattle": unnerving_rattle,
    "withering_glare": withering_glare,
    "long_wind": long_wind,
    "gust_shot": gust_shot,
    "tailwind": tailwind,
    "wind_dodge": wind_dodge,
    "whirlwind_volley": whirlwind_volley,
    "quickwind": quickwind,
    "carrion_eye": carrion_eye,
    "circler": circler,
    "gorge": gorge,
    "flock": flock,
    "glide": glide,
    "watcher": watcher,
    "stinger": stinger,
    "pincers": pincers,
    "carapace": carapace,
    "sand_hide": sand_hide,
    "venom_sac": venom_sac,
    "tail_whip": tail_whip,
    "petrified": petrified,
    "amber_heart": amber_heart,
    "bedrock_bones": bedrock_bones,
    "stoneskin": stoneskin,
    "sediment": sediment,
    "last_stand": last_stand,
}

MODELS = {
    "long_wind": "originofspawns:long_wind_wrap",
    "whirlwind_volley": "originofspawns:whirlwind_volley_quiver",
    "circler": "originofspawns:circler_ruff",
    "gorge": "originofspawns:gorge_beak",
    "glide": "originofspawns:glide_wings",
    "stinger": "originofspawns:stinger_tail",
    "pincers": "originofspawns:pincers_claws",
    "carapace": "originofspawns:carapace_shell",
    "amber_heart": "originofspawns:amber_heart_stone",
}

MODEL_MAKERS = [long_wind_model, whirlwind_volley_model, circler_model, gorge_model, glide_model,
                stinger_model, pincers_model, carapace_model, amber_heart_model]


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
    print("pixels that two or more parched genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
