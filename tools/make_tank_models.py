"""Make the gene models of the Tank genes: Thickskull, Stoneblood, and Bloat.

Usage: python3 tools/make_tank_models.py

Thickskull: a crown of thick bone on the top of the head, raised above the
skin, and a thin plate flat on the forehead. Both sit outside the Lurker
hood (inflate 0.4), so the two can be on one zombie. No part juts out in
front of the face.

Stoneblood: a thin shell of stone chips over the lower legs and the feet,
with gaps. It shows on the normal legs and on the bone legs of Bonerunner.
It is thinner than the Burrower dirt (inflate 0.3), so the dirt sits on top.

Bloat: a swollen body. It is one body cube, larger than the body, with the
texture of the mob ("mob_texture"), so that the marks of all the genes on
the shirt show on it too. It is a model of its own, because a model uses
one texture for all its parts.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")

BONE = ((206, 196, 168, 255), (188, 178, 148, 255), (170, 160, 130, 255))
SUTURE = (110, 98, 74, 255)
BONE_EDGE = (140, 128, 100, 255)

STONE = ((128, 128, 128, 255), (112, 112, 112, 255), (96, 96, 98, 255))
STONE_LIGHT = (150, 150, 150, 255)
STONE_DARK = (70, 70, 72, 255)


def noise(x, y, seed=0):
    return (x * 73856093 ^ y * 19349663 ^ seed * 83492791) * 2654435761 % 4294967296


def save(name, texture, model):
    path = os.path.join(ASSETS, "textures/entity", name + ".png")
    texture.save(path)
    with open(os.path.join(ASSETS, "gene_models", name + ".json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")
    print(path)


def bone_box(im, u, v, w, h, d, seed):
    """Fill the box UV area of a w x h x d box with bone."""
    for y in range(v, v + d + h):
        for x in range(u, u + 2 * (d + w)):
            if y < v + d and not u + d <= x < u + d + 2 * w:
                continue
            im.putpixel((x, y), BONE[noise(x, y, seed) % 3])


def thickskull():
    # The crown: 6x2x6 at uv (0, 0), on the top of the head, from y -10 to
    # -8. The forehead plate: 6x2x1 at uv (0, 8), flat on the forehead, over
    # face rows y 8 and 9. Its back half is inside the head, so its front is
    # only 0.6 pixel in front of the face. No part juts out. The sizes are
    # whole pixels, so that the box UV has no half pixels.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    bone_box(im, 0, 0, 6, 2, 6, 1)
    bone_box(im, 0, 8, 6, 2, 1, 7)
    # Sutures on the top of the crown: a line along the middle and a crack.
    for y in range(6):
        im.putpixel((9, y), SUTURE)
    for x, y in ((7, 1), (8, 2), (10, 4), (11, 3)):
        im.putpixel((x, y), SUTURE)
    # The thick edge of the crown and a crack in the forehead plate.
    for x in range(24):
        for y in (6, 7):
            if noise(x, y, 2) % 3 == 0:
                im.putpixel((x, y), BONE_EDGE)
    for x, y in ((3, 9), (4, 10)):
        im.putpixel((x, y), SUTURE)
    model = {
        "texture": "originofspawns:textures/entity/thickskull_plate.png",
        "texture_width": 32,
        "texture_height": 16,
        "parts": [
            {"bone": "head", "uv": [0, 0], "from": [-3, -10, -3], "size": [6, 2, 6]},
            {"bone": "head", "uv": [0, 8], "from": [-3, -8, -4.6], "size": [6, 2, 1]},
        ],
    }
    save("thickskull_plate", im, model)


def stoneblood():
    # The shell: 4x6x4 at uv (0, 0) on each leg, the lower half of the leg.
    w, h, d = 4, 6, 4
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    cover = (35, 45, 55, 65, 75, 80)
    for y in range(h):
        for x in range(2 * (d + w)):
            if noise(x, y, 3) % 100 < cover[y]:
                im.putpixel((x, d + y), STONE[noise(x, y, 4) % 3])
    for y in range(d):
        for x in range(d, d + 2 * w):
            if noise(x, y, 5) % 100 < 70:
                im.putpixel((x, y), STONE[noise(x, y, 6) % 3])
    # Light top edges and dark lower edges on the chips.
    for x in range(2 * (d + w)):
        for y in range(d, d + h):
            if not im.getpixel((x, y))[3]:
                continue
            if y == d or not im.getpixel((x, y - 1))[3]:
                im.putpixel((x, y), STONE_LIGHT)
            elif y == d + h - 1 or not im.getpixel((x, y + 1))[3]:
                im.putpixel((x, y), STONE_DARK)
    parts = []
    for bone, mirror in (("right_leg", False), ("left_leg", True)):
        part = {"bone": bone, "uv": [0, 0], "from": [-2, 6, -2], "size": [w, h, d], "inflate": 0.15}
        if mirror:
            part["mirror"] = True
        parts.append(part)
    model = {
        "texture": "originofspawns:textures/entity/stoneblood_shell.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": parts,
    }
    save("stoneblood_shell", im, model)


def bloat():
    model = {
        "mob_texture": True,
        "parts": [{"bone": "body", "uv": [16, 16], "from": [-4, 0, -2], "size": [8, 12, 4],
                   "inflate": 0.8}],
    }
    path = os.path.join(ASSETS, "gene_models", "bloat_body.json")
    with open(path, "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")
    print(path)


if __name__ == "__main__":
    thickskull()
    stoneblood()
    bloat()
