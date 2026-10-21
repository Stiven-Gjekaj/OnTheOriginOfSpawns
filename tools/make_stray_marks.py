"""Make the marks and the gene models of the stray genes.

Usage: python3 tools/make_stray_marks.py

The stray has the skeleton body and the skeleton region map, so its bone
marks use the same pixels as the skeleton marks (see
make_skeleton_marks.py). Over the bones, it has an outer layer of torn
cloth: the vanilla stray overlay. The outer layer has the zombie layout,
with 4x12x4 arms and legs, 0.25 pixel larger than the body. A mark with
the target "outer" goes on the cloth only, and it keeps the holes of the
cloth. A mark with no target goes on the bones only.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- textures/gene_marks/<gene>_<name>.png, the masks of the erase layers.
- gene_visuals/<gene>.json, with one layer for each pattern and mask.
- The gene models in MODELS, with their textures.

The script prints the pixels that two stray genes share, on the bones and
on the cloth.

What the cloth covers:
- The head: all of the head but the face under row 10 (y 11 to 15). The
  face shows the eye sockets (y 12) and the mouth (y 14).
- The body: rows 0 to 9 of the front and the back (y 20 to 29), and the
  sides. The pelvis (y 30 and 31) shows under the cloth.
- The arms: the sleeves cover arm rows 0 to 3. The forearms and the hands
  (rows 4 to 11) show.
- The legs: the cloth covers leg rows 0 to 7 on the outer and the front
  faces. The shins and the feet (rows 8 to 11) show.

Pixel plan for the Glaciers.
- Iceplate: thick plates of blue ice on the shoulders, the gene model
  iceplate_plates.
- Permafrost: a hole in the cloth over the front of the ribs (x 20 to 27,
  y 21 to 26). The ribs show in it, and ice fills the gaps between them.
- Shatterhide: sharp ice spikes out of the back, the gene model
  shatterhide_spikes.
- Rimeshell has a crust of rime on the forearms: arm rows 5 to 9 of the
  outer, the inner, and the back faces.
- Glacial Wall: a slab of ice over the chest, the gene model
  glacial_wall_slab.
- Icebound: a block of ice round each foot, the gene model icebound_blocks.

Pixel plan for the Blizzards.
- Squall cuts the lower cloth into long, slanted strips: it erases slits
  in the cloth of the body (rows 4 to 9), the sleeves, and the legs.
- Snowdrift has snow in the folds of the cloth: the fronts, the backs,
  and the sides of the body, the sleeves, and the leg cloth, more at the
  hems.
- Hailshot: a quiver packed with ice on the back, the gene model
  hailshot_quiver.
- Gale erases the hood of the cloth, so the skull is bare, and the gene
  model gale_hood hangs the hood back from the neck.
- Frostwind has frost breath round the jaw: face rows 13 to 15, and the
  front of the bottom of the head.
- Stormborn has a cap of packed snow on the cloth of the head: the top,
  and row 8 of each side, with drips on row 9.

Pixel plan for the Drifters.
- Snowshoe: a wide snowshoe of bone and hide under each foot, the gene
  model snowshoe_shoes.
- Iceskate: a bone blade under each foot, the gene model iceskate_blades.
- Pathfinder has a band of pale rag over the eyes: face rows 11 to 13, with a
  hole at the left eye (x 13 and 14), and the same band on the cloth of
  the sides and the back of the head, with a knot at the back.
- Avalanche has snow packed on the shins: leg rows 8 to 11, and the lower
  edge of the leg cloth (y 25 to 27).
- Snowburrow has snow on the cloth of the shoulders (the top of the body,
  the tops of the sleeves, and their top row) and patches of snow on the
  top of the hood.
- Driftline: a pole with a torn banner on the back, the gene model
  driftline_banner.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_skeleton_marks import BACK, FRONT, INNER, OUTER, REGION_MAP, SIDES, arm, leg

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

ICE = ("#DDF4FF", "#A8DCF4", "#74B8E0", "#4A88B8")
RIME = ("#F4FBFF", "#C8E6F4")
SNOW = ("#F6F8FA", "#D4DEE6", "#AEBCC8")

# The pixels of the front of the body where the stray texture has no bone:
# the gaps between the lower ribs, and the belly under the ribs.
FRONT_GAPS = {(20, 25), (22, 25), (25, 25), (27, 25), (20, 26), (23, 26), (24, 26), (27, 26),
              (20, 27), (21, 27), (22, 27), (23, 27), (24, 27), (25, 27), (26, 27)}


def cloth_arm(face, col, row):
    """Return the pixel of the cloth on a side face of a 4x12x4 arm.

    The faces are OUTER, FRONT, INNER, and BACK, as on the bone, but each
    face is 4 pixels wide.
    """
    return (40 + 2 * face + col, 20 + row)


def cloth_leg(face, col, row):
    return (2 * face + col, 20 + row)


def erase(gene, name, pixels, target=None, order=0):
    """Return an erase layer that clears the pixels, with its mask."""
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for p in pixels:
        im.putpixel(p, (255, 255, 255, 255))
    file_name = f"{gene}_{name}"
    im.save(os.path.join(MARK_DIR, file_name + ".png"))
    layer = {"effect": {"type": "erase", "texture": f"originofspawns:textures/gene_marks/{file_name}.png"}}
    if order:
        layer["order"] = order
    if target:
        layer["target"] = target
    return layer


def iceplate():
    # The plates are the gene model iceplate_plates.
    return []


def iceplate_model():
    # A thick plate of blue ice on each shoulder, on top of the sleeve, and
    # a smaller lump of ice on top of the plate. On an arm bone, the sleeve
    # top is at y -2.25. The plates keep clear of the head (x -4 to 4): on
    # the right arm, the inner edge is at x 0.5, that is x -4.5 on the body.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def plate(face, x, y):
        if face == "top":
            return ICE[0] if (x + y) % 4 else ICE[1]
        if face == "bottom":
            return ICE[3]
        return ICE[1] if y == 0 else ICE[2] if (x + y) % 3 else ICE[0]

    def lump(face, x, y):
        return ICE[0] if face == "top" else ICE[1]

    # The plate: 4x2x5 at uv (0, 0). The lump: 3x1x3 at uv (0, 8).
    paint_box(im, 0, 0, 4, 2, 5, plate)
    paint_box(im, 0, 8, 3, 1, 3, lump)
    parts = [
        {"bone": "right_arm", "uv": [0, 0], "from": [-3.5, -4, -2.5], "size": [4, 2, 5]},
        {"bone": "right_arm", "uv": [0, 8], "from": [-3, -5, -1.5], "size": [3, 1, 3]},
        {"bone": "left_arm", "uv": [0, 0], "from": [-0.5, -4, -2.5], "size": [4, 2, 5], "mirror": True},
        {"bone": "left_arm", "uv": [0, 8], "from": [0, -5, -1.5], "size": [3, 1, 3], "mirror": True},
    ]
    save_model("iceplate_plates", im, parts)


def permafrost():
    # Frost in the rib cage: the cloth over the front of the ribs is gone,
    # the ribs show, and ice fills the gaps between the lower ribs. The ice
    # is on the cloth layer, so it can fill the gaps where the stray texture
    # has no bone. A thin rime edge rims the hole in the cloth.
    window = {(x, y) for x in range(20, 28) for y in range(21, 27)}
    ice = Mark(ICE[1], target="outer")
    clear = Mark(ICE[0], target="outer")
    rim = Mark(RIME[0], target="outer")
    for x, y in window & FRONT_GAPS:
        (clear if (x + y) % 3 == 0 else ice).put((x, y))
    for x in range(20, 28):
        rim.put((x, 27), 235)
    frost = Mark(RIME[1], alpha=200)
    for x in range(20, 28):
        frost.put((x, 22)).put((x, 24), 230)
    hole = erase("permafrost", "hole", window - FRONT_GAPS, target="outer")
    return [hole, ice, clear, rim, frost]


def shatterhide():
    # The spikes are the gene model shatterhide_spikes.
    return []


def shatterhide_model():
    # Sharp ice spikes out of the back: each spike has a thick base on the
    # cloth of the back (z 2.25) and a thin tip, and it leans up. On the
    # body bone, the back is at z 2, and the body is from x -4 to 4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def base(face, x, y):
        return ICE[2] if face in ("right", "bottom") else ICE[1]

    def tip(face, x, y):
        return ICE[0] if face != "bottom" else ICE[1]

    # A base: 2x2x2 at uv (0, 0). A tip: 1x1x3 at uv (12, 0).
    paint_box(im, 0, 0, 2, 2, 2, base)
    paint_box(im, 12, 0, 1, 1, 3, tip)
    parts = []
    for x, y, angle in ((-3, 1.5, 35), (0.5, 0.5, 45), (-1.5, 4.5, 40), (1.5, 6, 30), (-3, 7.5, 35)):
        lean = {"pivot": [x + 1, y + 1, 2], "rotation": [angle, 0, 0]}
        parts.append(dict({"bone": "body", "uv": [0, 0], "from": [x, y, 1.6], "size": [2, 2, 2]}, **lean))
        parts.append(dict({"bone": "body", "uv": [12, 0], "from": [x + 0.5, y + 0.5, 3.6], "size": [1, 1, 3]},
                          **lean))
    save_model("shatterhide_spikes", im, parts)


def rimeshell():
    # A crust of rime on the forearms: a rough crust of pale blue ice with
    # white specks on arm rows 6 to 9 of the outer, the inner, and the back
    # faces, with a deep blue edge under the sleeve (row 5). The front face
    # stays free for the Numbing Shot veins, and the hands (rows 10 and 11)
    # stay free for the Frostbite fingers.
    crust = Mark(ICE[1])
    white = Mark(RIME[0])
    edge = Mark(ICE[3])
    for face in (OUTER, INNER, BACK):
        for col in (0, 1):
            for row in range(6, 10):
                (white if noise100(face + col, row, 211) < 35 else crust).put(arm(face, col, row))
            edge.put(arm(face, col, 5), 230)
    return [crust, white, edge]


def glacial_wall():
    # The slab is the gene model glacial_wall_slab.
    return []


def glacial_wall_model():
    # A slab of ice over the chest: a thick, flat block on the cloth of the
    # front (z -2.25), with chipped corners and a bright streak across it.
    # On the body bone, the front is at z -2, and the body is from x -4 to 4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def slab(face, x, y):
        if face == "front":
            if (x, y) in ((0, 0), (5, 0), (0, 7), (5, 7)):
                return None
            if x + y in (4, 5) or x + y == 9:
                return ICE[0]
            return ICE[1] if (x * 3 + y) % 5 else ICE[2]
        return ICE[2] if face in ("bottom", "left", "right") else ICE[1]

    # The slab: 6x8x1 at uv (0, 0). A chip on its edge: 1x2x1 at uv (16, 0).
    paint_box(im, 0, 0, 6, 8, 1, slab)
    paint_box(im, 16, 0, 1, 2, 1, lambda face, x, y: ICE[0])
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-3, 1, -3.2], "size": [6, 8, 1]},
        {"bone": "body", "uv": [16, 0], "from": [-3.5, 3, -3], "size": [1, 2, 1]},
        {"bone": "body", "uv": [16, 0], "from": [2.5, 6, -3], "size": [1, 2, 1]},
    ]
    save_model("glacial_wall_slab", im, parts)


def icebound():
    # The ice is the gene model icebound_blocks.
    return []


def icebound_model():
    # The feet are frozen into blocks of ice: a block round the bottom of
    # each leg, from the ankle to the ground, with a pale frame on its edges
    # so that it reads as clear ice, and a lump of ice on top. On a leg
    # bone, the leg is from y 0 to 12, and from x -1 to 1.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def block(face, x, y):
        edge = face in ("top", "bottom") and (x in (0, 3) or y in (0, 3)) or \
            face not in ("top", "bottom") and (x in (0, 3) or y == 0)
        if edge:
            return ICE[0]
        return ICE[1] if (x + 2 * y) % 5 else ICE[2]

    # The block: 4x3x4 at uv (0, 0). The lump: 2x1x2 at uv (16, 0).
    paint_box(im, 0, 0, 4, 3, 4, block)
    paint_box(im, 16, 0, 2, 1, 2, lambda face, x, y: ICE[0] if face == "top" else ICE[1])
    parts = []
    for bone in ("right_leg", "left_leg"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-2, 9, -2], "size": [4, 3, 4]})
        parts.append({"bone": bone, "uv": [16, 0], "from": [-1.5, 8, -1], "size": [2, 1, 2]})
    save_model("icebound_blocks", im, parts)



CLOTH = ("#617677", "#576C6D", "#445C5D", "#394E4F")


def cloth_pixels(rows=range(20, 30)):
    """Return the cloth pixels of the body, the sleeves, and the legs.

    The body is x 16 to 39, the arms x 40 to 55, and the legs x 0 to 15,
    on the rows. The holes of the cloth stay clear, as the marks keep the
    alpha of the cloth.
    """
    return [(x, y) for y in rows for x in range(0, 56)]


def squall():
    # The cloth is torn into long strips that blow back: long slits that
    # lean a little cut the lower cloth of the body (rows 23 to 29), the
    # sleeves, and the leg cloth (rows 22 to 29), each fourth column. A pale
    # streak on one edge of each strip shows the wind.
    slits = []
    streak = Mark("#8FA4A6", alpha=170, target="outer")
    for x, y in cloth_pixels(range(22, 30)):
        if 16 <= x < 40 and y < 23:
            continue
        cut = (x + (y - 20) // 4) % 4
        if cut == 0:
            slits.append((x, y))
        elif cut == 1:
            streak.put((x, y))
    return [erase("squall", "slits", slits, target="outer"), streak]


def snowdrift():
    # Snow in the folds of the cloth: wide clumps of snow (2 pixels) on the
    # cloth of the body, the sleeves, and the legs. There is no snow on the
    # top rows, and more snow on each lower row, where the snow collects in
    # the hems. A gray shade marks the top of each clump.
    snow = Mark(SNOW[0], target="outer")
    shade = Mark(SNOW[1], target="outer")
    for x, y in cloth_pixels(range(22, 32)):
        if noise100(x // 2, y, 231) < 6 * (y - 21):
            if noise100(x // 2, y - 1, 231) < 6 * (y - 22):
                snow.put((x, y))
            else:
                shade.put((x, y))
    return [snow, shade]


def hailshot():
    # The quiver is the gene model hailshot_quiver.
    return []


def hailshot_model():
    # A quiver of pale hide on the back, over the right shoulder, packed
    # with lumps of ice that stick out of its top in place of arrows. It
    # leans the other way to the Barrage quiver of the skeleton. On the body
    # bone, the cloth of the back is at z 2.25.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def quiver(face, x, y):
        if face == "top":
            return "#2E3A40"
        if y in (1, 5):
            return "#46565E"
        return "#7A8C94" if (x + y) % 3 else "#66787F"

    def lump(face, x, y):
        return ICE[0] if face == "top" else ICE[1] if (x + y) % 2 else ICE[2]

    # The quiver: 3x7x2 at uv (0, 0). A lump: 2x2x2 at uv (12, 0). A small
    # lump: 1x2x1 at uv (20, 0).
    paint_box(im, 0, 0, 3, 7, 2, quiver)
    paint_box(im, 12, 0, 2, 2, 2, lump)
    paint_box(im, 20, 0, 1, 2, 1, lump)
    lean = {"pivot": [-2.5, 5, 3], "rotation": [0, 0, -25]}
    parts = [dict({"bone": "body", "uv": [0, 0], "from": [-4, 1, 2.3], "size": [3, 7, 2]}, **lean),
             dict({"bone": "body", "uv": [12, 0], "from": [-4, -0.5, 2.3], "size": [2, 2, 2]}, **lean),
             dict({"bone": "body", "uv": [20, 0], "from": [-2, -1.5, 2.8], "size": [1, 2, 1]}, **lean),
             dict({"bone": "body", "uv": [20, 0], "from": [-3.5, -1, 3.3], "size": [1, 2, 1]}, **lean)]
    save_model("hailshot_quiver", im, parts)


def gale():
    # The wind blows the hood back: the cloth of the head is gone, so the
    # skull is bare, and the gene model gale_hood hangs the hood back from
    # the neck.
    head = [(x, y) for x in range(0, 32) for y in range(0, 16)]
    return [erase("gale", "hood", head, target="outer")]


def gale_model():
    # The hood, blown back off the skull: a bunch of cloth at the back of
    # the neck, and a torn tail that hangs from it and lies on the cloth of
    # the back. On the head bone, the back of the head is at z 4, the
    # neck is at y 0, and the cloth of the back is at z 2.25.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def bunch(face, x, y):
        return CLOTH[0] if face == "top" else CLOTH[1] if (x + y) % 3 else CLOTH[2]

    def tail(face, x, y):
        if face in ("front", "back"):
            # The lower end of the tail is torn.
            if y >= 5 and (x + y) % 3 == 0 or y == 6 and x % 2:
                return None
            return CLOTH[1] if x % 3 else CLOTH[2]
        return CLOTH[3]

    # The bunch: 8x3x3 at uv (0, 0). The tail: 6x7x1 at uv (0, 8).
    paint_box(im, 0, 0, 8, 3, 3, bunch)
    paint_box(im, 0, 8, 6, 7, 1, tail)
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-4, -3, 3], "size": [8, 3, 3]},
        {"bone": "head", "uv": [0, 8], "from": [-3, 0, 2.3], "size": [6, 7, 1]},
    ]
    save_model("gale_hood", im, parts)


def frostwind():
    # Frost breath round the jaw: a pale mist over the mouth and the chin
    # (face rows 13 to 15), and on the front of the bottom of the head, so
    # that it shows from below too. The mouth stays dark in the middle.
    mist = Mark("#E8F6FF", alpha=190)
    frost = Mark(RIME[0])
    for x in range(8, 16):
        frost.put((x, 15)) if x % 3 else mist.put((x, 15))
    for x in (8, 9, 14, 15):
        mist.put((x, 14))
    for x in (8, 15):
        mist.put((x, 13), 220)
    for x in range(16, 24):
        (frost if x % 2 else mist).put((x, 7))
        mist.put((x, 6), 200)
    return [mist, frost]


def stormborn():
    # A cap of packed snow on the cloth of the head: the top of the hood is
    # white, row 8 of each side is snow, and drips hang on row 9. A pale
    # blue shade marks the edge of the cap.
    snow = Mark(SNOW[0], target="outer")
    shade = Mark(SNOW[2], target="outer")
    for x in range(8, 16):
        for y in range(0, 8):
            (shade if noise100(x, y, 241) < 15 else snow).put((x, y))
    for x in range(0, 32):
        snow.put((x, 8))
        if x % 3 == 1:
            shade.put((x, 9))
    return [snow, shade]


HIDE = ("#8A7458", "#6E5A42")


def snowshoe():
    # The snowshoes are the gene model snowshoe_shoes.
    return []


def snowshoe_model():
    # A wide snowshoe under each foot: a frame of bone with round corners
    # and a web of hide in it, and a strap of hide over the foot. On a leg
    # bone, the foot is from x -1 to 1 and from z -1 to 1, and the ground
    # is at y 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def shoe(face, x, y):
        if face in ("top", "bottom"):
            if (x in (0, 3)) and (y in (0, 5)):
                return None
            if x in (0, 3) or y in (0, 5):
                return "#E2E4DC"
            return HIDE[0] if (x + y) % 2 else HIDE[1]
        return "#C8CCC2"

    # The shoe: 4x1x6 at uv (0, 0). The strap: 2x1x2 at uv (0, 10).
    paint_box(im, 0, 0, 4, 1, 6, shoe)
    paint_box(im, 0, 10, 2, 1, 2, lambda face, x, y: HIDE[1])
    parts = []
    for bone in ("right_leg", "left_leg"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-2, 11, -3], "size": [4, 1, 6]})
        parts.append({"bone": bone, "uv": [0, 10], "from": [-1, 10, -1], "size": [2, 1, 2], "inflate": 0.3})
    save_model("snowshoe_shoes", im, parts)


def iceskate():
    # The blades are the gene model iceskate_blades.
    return []


def iceskate_model():
    # A thin blade of bone under each foot, longer than the foot, with a
    # curl at its front tip, and a strut that holds it to the foot. On a
    # leg bone, the foot is from z -1 to 1, and the ground is at y 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def blade(face, x, y):
        return "#E8E4D8" if face == "top" else "#A8A496" if face != "bottom" else "#7E7A6E"

    # The blade: 1x1x7 at uv (0, 0). The curl: 1x2x1 at uv (18, 0). The
    # strut: 1x1x2 at uv (0, 8).
    paint_box(im, 0, 0, 1, 1, 7, blade)
    paint_box(im, 18, 0, 1, 2, 1, lambda face, x, y: "#A8A496")
    paint_box(im, 0, 8, 1, 1, 2, lambda face, x, y: "#6E6452")
    parts = []
    for bone in ("right_leg", "left_leg"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-0.5, 11, -3.5], "size": [1, 1, 7], "inflate": 0.05})
        parts.append({"bone": bone, "uv": [18, 0], "from": [-0.5, 9.5, -4.4], "size": [1, 2, 1]})
        parts.append({"bone": bone, "uv": [0, 8], "from": [-0.5, 10, -1], "size": [1, 1, 2], "inflate": 0.3})
    save_model("iceskate_blades", im, parts)


def pathfinder():
    # A band of pale rag over the eyes: face rows 11 to 13, with one hole at
    # the left eye (x 13 and 14 of row 12), so that one eye looks out. The
    # band goes on round the cloth of the head (rows 11 to 13 of the sides
    # and the back), with a knot and two loose ends at the back.
    band = Mark("#B8AE98")
    edge = Mark("#6E6452")
    for x in range(8, 16):
        edge.put((x, 11))
        band.put((x, 13))
        if x not in (13, 14):
            band.put((x, 12))
    wrap = Mark("#B8AE98", target="outer")
    light = Mark("#6E6452", target="outer")
    for x in list(range(0, 8)) + list(range(16, 32)):
        light.put((x, 11))
        wrap.put((x, 12)).put((x, 13))
    knot = Mark("#8E846E", target="outer")
    knot.put((27, 12)).put((28, 12)).put((27, 13)).put((28, 13))
    knot.put((26, 14)).put((29, 14)).put((26, 15)).put((29, 15), 220)
    return [band, edge, wrap, light, knot]


def avalanche():
    # Snow packed on the shins: a thick band of snow on leg rows 8 to 11 of
    # all the faces, with a gray shade on its top row. The snow goes on up
    # the lower edge of the leg cloth (rows 5 to 7), so that it reads from
    # far away.
    snow = Mark(SNOW[0])
    shade = Mark(SNOW[1])
    for face in SIDES:
        for col in (0, 1):
            shade.put(leg(face, col, 8))
            for row in (9, 10, 11):
                snow.put(leg(face, col, row))
    cloth_snow = Mark(SNOW[0], target="outer")
    cloth_shade = Mark(SNOW[1], target="outer")
    for x in range(0, 16):
        cloth_shade.put((x, 25))
        cloth_snow.put((x, 26)).put((x, 27))
    return [snow, shade, cloth_snow, cloth_shade]


def snowburrow():
    # Snow on the shoulders and the skull, as if it just rose out of the
    # snow: snow on the top of the body, on the tops of the sleeves and
    # their top row, and patches of snow on the top of the hood.
    snow = Mark(SNOW[0], target="outer")
    shade = Mark(SNOW[1], target="outer")
    for x in range(20, 28):
        for y in range(16, 20):
            snow.put((x, y))
    for x in range(44, 52):
        for y in range(16, 20):
            snow.put((x, y))
    for x in range(40, 56):
        (shade if x % 3 == 0 else snow).put((x, 20))
    for x in range(16, 40):
        if x % 4 == 1:
            shade.put((x, 20))
    for x in range(8, 16):
        for y in range(0, 8):
            if noise100(x // 2, y // 2, 251) < 45:
                snow.put((x, y))
    return [snow, shade]


def driftline():
    # The banner is the gene model driftline_banner.
    return []


def driftline_model():
    # A tall pole on the back, with a torn banner high over the head. The
    # pole lies on the cloth of the back (z 2.25) from the waist up, and a
    # bar at its top holds the banner. The banner is pale blue cloth with a
    # dark stripe, and its lower edge is torn. On the body bone, the top of
    # the head is at y -8.
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def pole(face, x, y):
        return "#7A6A52" if face in ("front", "back") and y % 5 else "#5E5040"

    def banner(face, x, y):
        if face not in ("front", "back"):
            return None
        if y == 4 and x % 2 == 0 or y == 3 and x == 1:
            return None
        return "#3E5A6E" if y == 1 else "#B4C8D4" if (x + y) % 4 else "#9CB2C0"

    # The pole: 1x23x1 at uv (0, 0). The bar: 5x1x1 at uv (4, 0). The
    # banner: 4x5x0 at uv (4, 4).
    paint_box(im, 0, 0, 1, 23, 1, pole)
    paint_box(im, 4, 0, 5, 1, 1, lambda face, x, y: "#5E5040")
    paint_box(im, 4, 4, 4, 5, 0, banner)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [2, -14, 2.3], "size": [1, 23, 1]},
        {"bone": "body", "uv": [4, 0], "from": [-2, -14, 2.3], "size": [5, 1, 1]},
        {"bone": "body", "uv": [4, 4], "from": [-2, -13, 2.8], "size": [4, 5, 0]},
    ]
    save_model("driftline_banner", im, parts)

GENES = {
    "iceplate": iceplate,
    "permafrost": permafrost,
    "shatterhide": shatterhide,
    "rimeshell": rimeshell,
    "glacial_wall": glacial_wall,
    "icebound": icebound,
    "squall": squall,
    "snowdrift": snowdrift,
    "hailshot": hailshot,
    "gale": gale,
    "frostwind": frostwind,
    "stormborn": stormborn,
    "snowshoe": snowshoe,
    "iceskate": iceskate,
    "pathfinder": pathfinder,
    "avalanche": avalanche,
    "snowburrow": snowburrow,
    "driftline": driftline,
}

MODELS = {
    "iceplate": "originofspawns:iceplate_plates",
    "shatterhide": "originofspawns:shatterhide_spikes",
    "glacial_wall": "originofspawns:glacial_wall_slab",
    "icebound": "originofspawns:icebound_blocks",
    "hailshot": "originofspawns:hailshot_quiver",
    "gale": "originofspawns:gale_hood",
    "snowshoe": "originofspawns:snowshoe_shoes",
    "iceskate": "originofspawns:iceskate_blades",
    "driftline": "originofspawns:driftline_banner",
}

MODEL_MAKERS = [iceplate_model, shatterhide_model, glacial_wall_model, icebound_model, hailshot_model,
                gale_model, snowshoe_model, iceskate_model, driftline_model]


def changed_pixels(visual):
    """Return the (layer, pixel) pairs that the marks of a gene visual change.

    The layer is "outer" for the cloth and "bone" for the bones. A layer
    with an order below 0 is a color change under the marks, and the check
    does not count it.
    """
    pixels = set()
    for layer in visual["layers"]:
        if layer.get("order", 0) < 0 or "texture" not in layer["effect"]:
            continue
        _, path = layer["effect"]["texture"].split(":")
        im = Image.open(os.path.join(ROOT, "src/main/resources/assets/originofspawns", path)).convert("RGBA")
        where = layer.get("target", "bone")
        for y in range(32):
            for x in range(64):
                if im.getpixel((x, y))[3]:
                    pixels.add((where, (x, y)))
    return pixels


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
    print("pixels that two or more stray genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
