"""Make the Lurker hood: a gene model and its texture.

Usage: python3 tools/make_lurker_hood.py

A ragged hood of dark cloth on the head bone. It covers the top, the sides,
and the back of the head, and hangs 2 pixels below the head over the back
of the neck, with a torn lower edge. The front is open under the forehead, so
that the marks of other genes on the face stay visible: the cloth hangs
only over the top 2 rows of the face, which no other gene uses. No part
juts out in front of the face: the hood lies flat around the head.
The hood is thin (inflate 0.4), so that the head models of other genes can
sit on top of it.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")
TEXTURE = "textures/entity/lurker_hood.png"
MODEL = "gene_models/lurker_hood.json"

CLOTH = (44, 40, 34, 255)
FOLD = (31, 28, 24, 255)
WORN = (60, 55, 46, 255)
EDGE = (24, 22, 19, 255)

# The hood: 8x10x8 at uv (0, 0).
HOOD_W, HOOD_H, HOOD_D = 8, 10, 8
INFLATE = 0.4


def noise(x, y):
    return (x * 73856093 ^ y * 19349663) * 2654435761 % 4294967296


def cloth(x, y):
    """Return the cloth color at a texture pixel: folds, worn spots, and cloth."""
    n = noise(x, y) % 100
    if x % 3 == 0 and n < 70:
        return FOLD
    if n < 10:
        return WORN
    return CLOTH


def texture():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    w, h, d = HOOD_W, HOOD_H, HOOD_D
    # The top of the hood.
    for y in range(d):
        for x in range(d, d + w):
            im.putpixel((x, y), cloth(x, y))
    # The right side, the left side, and the back. The lower 2 rows hang
    # below the head, and the lowest rows are torn.
    for x0 in (0, d + w, 2 * d + w):
        width = w if x0 == 2 * d + w else d
        for x in range(x0, x0 + width):
            for row in range(h):
                n = noise(x, row) % 100
                if row == h - 1 and n < 45 or row == h - 2 and n < 12:
                    continue
                torn_edge = row == h - 1 or (row == h - 2 and noise(x, row + 1) % 100 < 45)
                im.putpixel((x, d + row), EDGE if torn_edge else cloth(x, d + row))
    # The front is open under the forehead. The cloth hangs over the top 2
    # rows of the face, with a ragged edge in the third row.
    for x in range(d, d + w):
        im.putpixel((x, d), cloth(x, d))
        im.putpixel((x, d + 1), cloth(x, d + 1))
        if noise(x, d + 2) % 100 < 40:
            im.putpixel((x, d + 2), EDGE)
    return im


def model():
    i = INFLATE
    return {
        "texture": "originofspawns:" + TEXTURE,
        "texture_width": 64,
        "texture_height": 32,
        "parts": [
            {"bone": "head", "uv": [0, 0], "from": [-4, -8, -4], "size": [HOOD_W, HOOD_H, HOOD_D],
             "inflate": i},
        ],
    }


if __name__ == "__main__":
    texture().save(os.path.join(ASSETS, TEXTURE))
    with open(os.path.join(ASSETS, MODEL), "w") as fh:
        json.dump(model(), fh, indent="\t")
        fh.write("\n")
    print(os.path.join(ASSETS, TEXTURE))
    print(os.path.join(ASSETS, MODEL))
