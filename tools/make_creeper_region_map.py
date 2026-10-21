"""Make the creeper region map.

Usage: python3 tools/make_creeper_region_map.py (from the root of the repository)

The creeper texture (textures/entity/creeper/creeper.png) is 64x32. The
creeper model (CreeperModel) has a head (8x8x8) at uv (0, 0), a body
(8x12x4) at uv (16, 16), and four legs (4x6x4) that all use uv (0, 16).

The map uses the region ids of the humanoid map, so that no new id is
needed:
- face (1): the front of the head.
- scalp (2): the rest of the head.
- torso (3): the front, the sides, the top, and the bottom of the body.
- spine (10): the back face of the body.
- legs (6): rows 0 to 3 of the legs, and the top of the legs.
- feet (8): rows 4 and 5 of the legs, and the bottom of the legs.
- eye_sockets (13): the black pixels of the eyes and the mouth on the face.
The material is skin (1), and eyes (3) on the eye_sockets pixels. Red is
the region id. Green is the material id. Blue is 0. A pixel with alpha 0 is
not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/creeper.png"

FACE, SCALP, TORSO, LEGS, FEET, SPINE, EYE_SOCKETS = 1, 2, 3, 6, 8, 10, 13
SKIN, EYES = 1, 3

# The black pixels of the eyes and the mouth on the face of the creeper.
EYE_PIXELS = {(9, 10), (10, 10), (9, 11), (10, 11), (13, 10), (14, 10), (13, 11), (14, 11),
              (11, 12), (12, 12), (10, 13), (11, 13), (12, 13), (13, 13), (10, 14), (11, 14),
              (12, 14), (13, 14), (10, 15), (13, 15)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS
    return FACE if face == "front" else SCALP


def body(x, y, face, row):
    return SPINE if face == "back" else TORSO


def leg(x, y, face, row):
    if face == "top":
        return LEGS
    if face == "bottom":
        return FEET
    return FEET if row >= 4 else LEGS


PARTS = [((0, 0), (8, 8, 8), head), ((16, 16), (8, 12, 4), body), ((0, 16), (4, 6, 4), leg)]


def make():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for (u, v), (w, h, d), region in PARTS:
        for x, y, face, row in box(u, v, w, h, d):
            r = region(x, y, face, row)
            im.putpixel((x, y), (r, EYES if r == EYE_SOCKETS else SKIN, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
