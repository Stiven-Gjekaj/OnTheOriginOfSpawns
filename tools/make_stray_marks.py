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
- Rimeshell has a crust of rime on the forearms: arm rows 5 to 11 of the
  outer, the inner, and the back faces.
- Glacial Wall: a slab of ice over the chest, the gene model
  glacial_wall_slab.
- Icebound: a block of ice round each foot, the gene model icebound_blocks.
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
    # white specks on arm rows 6 to 10 of the outer, the inner, and the back
    # faces, with a deep blue edge under the sleeve (row 5) and at the wrist
    # (row 11). The
    # front face stays free for the Numbing Shot veins.
    crust = Mark(ICE[1])
    white = Mark(RIME[0])
    edge = Mark(ICE[3])
    for face in (OUTER, INNER, BACK):
        for col in (0, 1):
            for row in range(6, 11):
                (white if noise100(face + col, row, 211) < 35 else crust).put(arm(face, col, row))
            edge.put(arm(face, col, 5 if (col + face // 2) % 2 else 11), 230)
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


GENES = {
    "iceplate": iceplate,
    "permafrost": permafrost,
    "shatterhide": shatterhide,
    "rimeshell": rimeshell,
    "glacial_wall": glacial_wall,
    "icebound": icebound,
}

MODELS = {
    "iceplate": "originofspawns:iceplate_plates",
    "shatterhide": "originofspawns:shatterhide_spikes",
    "glacial_wall": "originofspawns:glacial_wall_slab",
    "icebound": "originofspawns:icebound_blocks",
}

MODEL_MAKERS = [iceplate_model, shatterhide_model, glacial_wall_model, icebound_model]


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
