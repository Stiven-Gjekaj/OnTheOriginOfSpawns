"""Make the endermite region map.

Usage: python3 tools/make_endermite_region_map.py (from the root of the repository)

The endermite texture (textures/entity/endermite/endermite.png) is 64x32.
The endermite model (EndermiteModel) has four segments, from the front to
the back:
- segment0, the head: 4x3x2 at uv (0, 0). Its front has a dark red eye at
  (3, 2) and (4, 2).
- segment1, the body: 6x4x5 at uv (0, 5).
- segment2, the tail: 3x3x1 at uv (0, 14).
- segment3, the tip of the tail: 1x2x1 at uv (0, 18).

The map uses the region ids of the humanoid map:
- face (1): the front of the head.
- scalp (2): the rest of the head.
- torso (3): the sides and the bottom of the body.
- spine (10): the top of the body, the back of the shell.
- pelvis (11): the tail and the tip of the tail.
- eye_sockets (13): the eye on the front of the head.
The material is skin (1), and eyes (3) on the eye. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/endermite.png"

FACE, SCALP, TORSO, SPINE, PELVIS, EYE_SOCKETS = 1, 2, 3, 10, 11, 13
SKIN, EYES = 1, 3

EYE_PIXELS = {(3, 2), (4, 2)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    return (FACE if face == "front" else SCALP), SKIN


def body(x, y, face, row):
    return (SPINE if face == "top" else TORSO), SKIN


def tail(x, y, face, row):
    return PELVIS, SKIN


PARTS = [((0, 0), (4, 3, 2), head), ((0, 5), (6, 4, 5), body), ((0, 14), (3, 3, 1), tail),
         ((0, 18), (1, 2, 1), tail)]


def make():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
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
