"""Make the Death Throes fuse: its gene model and its texture.

Usage: python3 tools/make_fuse.py

The fuse comes out of the upper back of the zombie, near its right
shoulder. It leans back and away from the head, so that from the front it
stands out next to the head and does not look like an earring.
The three parts share one pivot at the base of the fuse, on the back, and
one rotation:
- the wick, 1x5x1, charcoal brown,
- a singed ring, 1x1x1, ash brown, under the tip,
- the tip, 1x1x1, bright orange. It glows, so it is bright in the dark.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")
TEXTURE = "textures/entity/death_throes_fuse.png"
MODEL = "gene_models/death_throes_fuse.json"

WICK = (43, 33, 24, 255)
WICK_LIGHT = (58, 45, 33, 255)
SINGED = (96, 84, 72, 255)
SINGED_DARK = (70, 60, 50, 255)
TIP = (255, 138, 31, 255)
TIP_HOT = (255, 196, 92, 255)

# The base of the fuse, on the back of the body, near the right shoulder.
PIVOT = [-3.5, 1.0, 2.0]
# Lean back (x) and away from the head (z), in degrees.
ROTATION = [-25.0, 0.0, -35.0]
WICK_LENGTH = 5


def texture():
    """Return the 16x16 texture. Each part has its own box UV area."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # The wick, 1x5x1, at uv (0, 0): rows 0 to 5.
    for x in range(4):
        for y in range(1 + WICK_LENGTH):
            im.putpixel((x, y), WICK_LIGHT if x in (0, 1) and y > 0 and y % 2 else WICK)
    # The ring, 1x1x1, at uv (0, 8).
    for x in range(4):
        for y in (8, 9):
            im.putpixel((x, y), SINGED if x in (0, 1) else SINGED_DARK)
    # The tip, 1x1x1, at uv (0, 12). Its top is the hottest pixel.
    for x in range(4):
        for y in (12, 13):
            im.putpixel((x, y), TIP)
    im.putpixel((1, 12), TIP_HOT)
    return im


def part(uv, y, length, glow=False):
    """Return a part of the fuse: from y to y + length, above the pivot."""
    p = {
        "bone": "body", "uv": uv,
        "from": [PIVOT[0] - 0.5, y, PIVOT[2] - 0.5], "size": [1, length, 1],
        "pivot": PIVOT, "rotation": ROTATION,
    }
    if glow:
        p["glow"] = True
    return p


def model():
    base = PIVOT[1]
    top = base - WICK_LENGTH
    return {
        "texture": "originofspawns:" + TEXTURE,
        "texture_width": 16,
        "texture_height": 16,
        "parts": [
            part([0, 0], top, WICK_LENGTH),
            part([0, 8], top - 1, 1),
            part([0, 12], top - 2, 1, glow=True),
        ],
    }


if __name__ == "__main__":
    texture().save(os.path.join(ASSETS, TEXTURE))
    with open(os.path.join(ASSETS, MODEL), "w") as fh:
        json.dump(model(), fh, indent="\t")
        fh.write("\n")
    print(os.path.join(ASSETS, TEXTURE))
    print(os.path.join(ASSETS, MODEL))
