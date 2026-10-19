"""Make the gene model of the Blight gene Sporebearer.

Usage: python3 tools/make_blight_models.py

Sporebearer: small fungus on the head: two mushrooms on the top, and a
bracket fungus on the right side. Each sits outside the Lurker hood
(inflate 0.4), so the fungus shows on top of the hood. The bracket is high
on the side, away from the cheekbones of Thickskull, and the top mushrooms
are away from the middle. The skull patch of Thickskull is on the back of
the head, where no fungus is.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")

CAP = ((184, 155, 94, 255), (166, 138, 80, 255))
SPOT = (110, 127, 74, 255)
GILLS = (90, 74, 48, 255)
STEM = (207, 197, 168, 255)


def box_faces(u, v, w, h, d):
    """Return the box UV areas: top, bottom, right, front, left, back."""
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }


def fill(im, area, color):
    x0, y0, w, h = area
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            im.putpixel((x, y), color(x - x0, y - y0) if callable(color) else color)


def sporebearer():
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    # The cap: 3x1x3 at uv (0, 0). The stem: 1x1x1 at uv (0, 6).
    # The bracket: 2x1x3 at uv (12, 0): 2 pixels out from the side, 3 long.
    cap = box_faces(0, 0, 3, 1, 3)
    for name, area in cap.items():
        if name == "bottom":
            fill(im, area, GILLS)
        elif name == "top":
            fill(im, area, lambda x, y: SPOT if (x, y) in ((0, 0), (2, 1), (1, 2)) else CAP[(x + y) % 2])
        else:
            fill(im, area, lambda x, y: CAP[(x + y) % 2])
    for area in box_faces(0, 6, 1, 1, 1).values():
        fill(im, area, STEM)
    for name, area in box_faces(12, 0, 2, 1, 3).items():
        if name == "bottom":
            fill(im, area, GILLS)
        else:
            fill(im, area, lambda x, y: SPOT if (x + 2 * y) % 4 == 0 else CAP[(x + y) % 2])
    # Head coordinates: the top is at y -8, the hood top at y -8.4. The sides
    # are at x -4 and 4, the hood sides at x -4.4 and 4.4. The stems stand on
    # the scalp, from y -8 to -9, and the caps sit on the stems.
    parts = [
        {"bone": "head", "uv": [0, 6], "from": [-3, -9, 1.5], "size": [1, 1, 1]},
        {"bone": "head", "uv": [0, 0], "from": [-4, -10, 0.5], "size": [3, 1, 3]},
        {"bone": "head", "uv": [0, 6], "from": [2, -9, -1.5], "size": [1, 1, 1]},
        {"bone": "head", "uv": [0, 0], "from": [1, -10, -2.5], "size": [3, 1, 3],
         "pivot": [2.5, -9.5, -1], "rotation": [0, 0, 12]},
        {"bone": "head", "uv": [12, 0], "from": [-6.4, -7, 0], "size": [2, 1, 3],
         "pivot": [-4.4, -6.5, 1.5], "rotation": [0, 0, -10]},
    ]
    model = {
        "texture": "originofspawns:textures/entity/sporebearer_fungus.png",
        "texture_width": 32,
        "texture_height": 16,
        "parts": parts,
    }
    path = os.path.join(ASSETS, "textures/entity/sporebearer_fungus.png")
    im.save(path)
    with open(os.path.join(ASSETS, "gene_models/sporebearer_fungus.json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")
    print(path)


if __name__ == "__main__":
    sporebearer()
