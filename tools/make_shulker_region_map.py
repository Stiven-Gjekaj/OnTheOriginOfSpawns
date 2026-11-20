"""Make the shulker region map.

Usage: python3 tools/make_shulker_region_map.py (from the root of the repository)

The shulker textures (textures/entity/shulker/shulker.png and the 16
shulker_<color>.png) are 64x64, with one layout. One map serves all 17
colors. ShulkerModel has three parts:
- the lid: 16x12x16 at uv (0, 0). On its sides, the low 4 rows have gaps
  in the middle 8 columns. The bottom of the lid is clear, except a thin
  frame.
- the base: 16x8x16 at uv (0, 28). On its sides, the high 4 rows have only
  the middle 8 columns. These fill the gaps of the lid when it is shut. The
  top of the base is clear, except a few pixels. The bottom has a spiral.
- the head: 6x6x6 at uv (0, 52). It is pale yellow on all colors. Its front
  has two eyes, a white pixel and a black pixel each, at (6, 61), (7, 61),
  (10, 61), and (11, 61).

The design uses the regions lid, base, and head. The map uses these region
ids of the humanoid map:
- torso (3): the lid.
- legs (6): the base.
- face (1): the front of the head.
- scalp (2): the rest of the head.
- eye_sockets (13): the eyes.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/shulker.png"

FACE, SCALP, TORSO, LEGS, EYE_SOCKETS = 1, 2, 3, 6, 13
SKIN, EYES = 1, 3

EYE_PIXELS = {(6, 61), (7, 61), (10, 61), (11, 61)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    return (FACE if face == "front" else SCALP), SKIN


def lid(x, y, face, row):
    return TORSO, SKIN


def base(x, y, face, row):
    return LEGS, SKIN


PARTS = [((0, 0), (16, 12, 16), lid), ((0, 28), (16, 8, 16), base), ((0, 52), (6, 6, 6), head)]


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for (u, v), (w, h, d), region in PARTS:
        for x, y, face, row in box(u, v, w, h, d):
            r, material = region(x, y, face, row)
            im.putpixel((x, y), (r, material, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
