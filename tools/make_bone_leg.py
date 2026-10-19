"""Make the Bonerunner bone leg texture.

Usage: python3 tools/make_bone_leg.py

The texture is 16x16. It holds the box UV of one cube of 3x6x3 pixels at UV
origin (0, 0). The top and the bottom faces are in the first 3 rows. The four
sides are in the next 6 rows. The other pixels are clear.
The bone is a little darker and more yellow than skeleton bone, so it looks
worn. The skeleton leg is gray from 142 to 188.
This script needs Pillow.
"""
import os

from PIL import Image

OUT = "src/main/resources/assets/originofspawns/textures/entity/bone_leg.png"

LIGHT = (194, 182, 142, 255)
BASE = (174, 162, 124, 255)
SHADE = (150, 138, 104, 255)
DARK = (122, 110, 82, 255)
CRACK = (98, 88, 64, 255)

# Each side face is 3 columns and 6 rows. Row 0 is the knee. Row 5 is the
# ankle. The knee and the ankle are knobs, so they are lighter than the shaft.
FRONT = [
    [LIGHT, LIGHT, BASE],
    [SHADE, LIGHT, SHADE],
    [BASE, LIGHT, SHADE],
    [BASE, BASE, CRACK],
    [SHADE, LIGHT, SHADE],
    [LIGHT, LIGHT, BASE],
]
SIDE = [
    [LIGHT, BASE, BASE],
    [SHADE, BASE, SHADE],
    [SHADE, DARK, SHADE],
    [SHADE, BASE, SHADE],
    [DARK, BASE, SHADE],
    [BASE, LIGHT, BASE],
]
BACK = [
    [BASE, BASE, SHADE],
    [DARK, SHADE, DARK],
    [SHADE, BASE, DARK],
    [DARK, SHADE, DARK],
    [SHADE, SHADE, DARK],
    [BASE, BASE, SHADE],
]
# The top joins the zombie knee. The bottom is the end of the bone.
TOP = [
    [BASE, BASE, SHADE],
    [BASE, DARK, SHADE],
    [SHADE, SHADE, SHADE],
]
BOTTOM = [
    [SHADE, BASE, SHADE],
    [BASE, DARK, BASE],
    [SHADE, BASE, SHADE],
]


def put(im, x0, y0, rows):
    for dy, row in enumerate(rows):
        for dx, color in enumerate(row):
            im.putpixel((x0 + dx, y0 + dy), color)


def mirror(rows):
    return [list(reversed(r)) for r in rows]


def make():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    w, h, d = 3, 6, 3
    put(im, d, 0, TOP)
    put(im, d + w, 0, BOTTOM)
    put(im, 0, d, SIDE)
    put(im, d, d, FRONT)
    put(im, d + w, d, mirror(SIDE))
    put(im, 2 * d + w, d, BACK)
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
