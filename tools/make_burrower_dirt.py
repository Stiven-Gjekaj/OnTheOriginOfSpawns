"""Make the Burrower dirt: a gene model and its texture.

Usage: python3 tools/make_burrower_dirt.py

A thin shell of dirt covers the lower legs and the feet. It is one cube on
each leg bone, the size of the lower half of the leg, a little larger
("inflate"), so that it sits over the normal legs and over the bone legs of
Bonerunner. The texture is a cutout: clumps of dirt with gaps, thickest at
the bottom. The legs show through the gaps.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")
TEXTURE = "textures/entity/burrower_dirt.png"
MODEL = "gene_models/burrower_dirt.json"

DIRT = ((106, 74, 44, 255), (84, 58, 34, 255), (64, 44, 26, 255))
DIRT_LIGHT = (128, 94, 60, 255)

# The cube: 4x6x4, the lower 6 rows of the 4x12x4 leg, at uv (0, 0).
W, H, D = 4, 6, 4
INFLATE = 0.3
# The part of each side row that has dirt, in percent, from the top row
# (the shin) to the bottom row (the shoe).
SIDE_COVER = (20, 30, 45, 60, 75, 90)
TOP_COVER = 15
BOTTOM_COVER = 90


def noise(x, y):
    return (x * 73856093 ^ y * 19349663) * 2654435761 % 4294967296


def texture():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def fill(x0, y0, w, h, cover):
        for y in range(h):
            for x in range(w):
                n = noise(x0 + x, y0 + y)
                if n % 100 < cover(y):
                    color = DIRT[(n >> 8) % len(DIRT)]
                    im.putpixel((x0 + x, y0 + y), color)
    # The top and the bottom faces.
    fill(D, 0, W, D, lambda y: TOP_COVER)
    fill(D + W, 0, W, D, lambda y: BOTTOM_COVER)
    # The four side faces.
    fill(0, D, 2 * (D + W), H, lambda y: SIDE_COVER[y])
    # A light pixel on the top edge of each clump on the sides.
    for x in range(2 * (D + W)):
        for y in range(D + 1, D + H):
            if im.getpixel((x, y))[3] and not im.getpixel((x, y - 1))[3]:
                im.putpixel((x, y), DIRT_LIGHT)
    return im


def model():
    parts = []
    for bone, mirror in (("right_leg", False), ("left_leg", True)):
        part = {"bone": bone, "uv": [0, 0], "from": [-2, 6, -2], "size": [W, H, D], "inflate": INFLATE}
        if mirror:
            part["mirror"] = True
        parts.append(part)
    return {
        "texture": "originofspawns:" + TEXTURE,
        "texture_width": 16,
        "texture_height": 16,
        "parts": parts,
    }


if __name__ == "__main__":
    texture().save(os.path.join(ASSETS, TEXTURE))
    with open(os.path.join(ASSETS, MODEL), "w") as fh:
        json.dump(model(), fh, indent="\t")
        fh.write("\n")
    print(os.path.join(ASSETS, TEXTURE))
    print(os.path.join(ASSETS, MODEL))
