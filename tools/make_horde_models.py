"""Make the gene model of the Horde gene Packcaller.

Usage: python3 tools/make_horde_models.py

Packcaller: a lower jaw that hangs open under the front of the face. The
open mouth shows over the chin, with two fangs.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")

SKIN = ((95, 128, 70, 255), (84, 115, 61, 255))
SKIN_DARK = (66, 92, 48, 255)
GUM = (70, 26, 24, 255)
MOUTH = (26, 15, 12, 255)
TOOTH = (226, 218, 190, 255)

HAIR = ((58, 48, 38, 255), (78, 66, 52, 255))



def save(name, texture, model):
    path = os.path.join(ASSETS, "textures/entity", name + ".png")
    texture.save(path)
    with open(os.path.join(ASSETS, "gene_models", name + ".json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")
    print(path)


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
            c = color(x - x0, y - y0) if callable(color) else color
            if c:
                im.putpixel((x, y), c)


def packcaller():
    # The jaw: 4x2x2 at uv (0, 0), under the front of the head (the head
    # bottom is at y 0, and the face is at z -4). Its front is 0.4 pixel in
    # front of the face. The upper row of its front is the open mouth, with
    # two fangs. The lower row is the chin.
    w, h, d = 4, 2, 2
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    f = box_faces(0, 0, w, h, d)
    fill(im, f["front"], lambda x, y: (TOOTH if x in (0, 3) else MOUTH) if y == 0 else SKIN[x % 2])
    fill(im, f["right"], SKIN_DARK)
    fill(im, f["left"], SKIN_DARK)
    fill(im, f["bottom"], SKIN_DARK)
    fill(im, f["back"], MOUTH)
    # The top: fangs at the corners at the front, and gums behind them.
    fill(im, f["top"], lambda x, y: TOOTH if y == 1 and x in (0, 3) else GUM if y == 1 else MOUTH)
    model = {
        "texture": "originofspawns:textures/entity/packcaller_jaw.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": [{"bone": "head", "uv": [0, 0], "from": [-2, 0, -4.4], "size": [w, h, d]}],
    }
    save("packcaller_jaw", im, model)


if __name__ == "__main__":
    packcaller()
