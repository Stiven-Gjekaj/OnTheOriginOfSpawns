"""Make the gene models of the Horde genes: Packcaller and Siege.

Usage: python3 tools/make_horde_models.py

Packcaller: a lower jaw that hangs open under the front of the face. The
open mouth shows over the chin, with two fangs.

Siege: big splinters of wood stuck in each arm: one in the shoulder, one
in the outer side of the forearm, and one in the front of the forearm, each
at its own angle. Two small shards of glass are stuck in the outer side of
each forearm.
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

WOOD = ((176, 136, 80, 255), (150, 112, 62, 255))
WOOD_TIP = (94, 68, 38, 255)


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


GLASS = (200, 236, 244, 255)
GLASS_GLINT = (250, 254, 255, 255)


def mirror_part(part):
    """Return the part for the left arm: the same part, mirrored in x."""
    left = dict(part, bone="left_arm", mirror=True)
    left["from"] = [-(part["from"][0] + part["size"][0])] + part["from"][1:]
    if "pivot" in part:
        left["pivot"] = [-part["pivot"][0]] + part["pivot"][1:]
    if "rotation" in part:
        x, y, z = part["rotation"]
        left["rotation"] = [x, -y, -z]
    return left


def siege():
    # Each splinter is 1x1x4 (or 4x1x1, or 1x4x1), with a dark tip at its
    # outer end. Glass shards are flat 1x1 squares. All on the right arm
    # first, in arm coordinates: the arm box is from x -3 to 1, y -2 to 10,
    # and z -2 to 2. The outer side is at x -3.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    for area in box_faces(0, 0, 1, 4, 1).values():
        fill(im, area, lambda x, y: WOOD[(x + y) % 2])
    for area in box_faces(8, 0, 4, 1, 1).values():
        fill(im, area, lambda x, y: WOOD[(x + y) % 2])
    for area in box_faces(0, 8, 1, 1, 4).values():
        fill(im, area, lambda x, y: WOOD[(x + y) % 2])
    # The tips: the top of the shoulder splinter, the outer end of the
    # sideways one, and the front end of the forward one.
    im.putpixel((1, 0), WOOD_TIP)
    im.putpixel((8, 1), WOOD_TIP)
    for p in ((5, 12), (4, 12)):
        im.putpixel(p, WOOD_TIP)
    fill(im, box_faces(20, 0, 1, 1, 0)["front"], GLASS_GLINT)
    fill(im, box_faces(20, 0, 1, 1, 0)["back"], GLASS)
    right = [
        {"bone": "right_arm", "uv": [0, 0], "from": [-2.5, -6, -0.5], "size": [1, 4, 1],
         "pivot": [-2, -2, 0], "rotation": [10, 0, -35]},
        {"bone": "right_arm", "uv": [8, 0], "from": [-7, 5, -0.5], "size": [4, 1, 1],
         "pivot": [-3, 5.5, 0], "rotation": [0, 15, 20]},
        {"bone": "right_arm", "uv": [0, 8], "from": [-1.5, 6.5, -6], "size": [1, 1, 4],
         "pivot": [-1, 7, -2], "rotation": [25, -15, 0]},
        {"bone": "right_arm", "uv": [20, 0], "from": [-4, 2, 0.5], "size": [1, 1, 0],
         "pivot": [-3, 2.5, 0.5], "rotation": [0, 0, 25]},
        {"bone": "right_arm", "uv": [20, 0], "from": [-4, 3.5, -1], "size": [1, 1, 0],
         "pivot": [-3, 4, -1], "rotation": [0, 30, -20]},
    ]
    model = {
        "texture": "originofspawns:textures/entity/siege_splinters.png",
        "texture_width": 32,
        "texture_height": 16,
        "parts": right + [mirror_part(p) for p in right],
    }
    save("siege_splinters", im, model)


if __name__ == "__main__":
    packcaller()
    siege()
