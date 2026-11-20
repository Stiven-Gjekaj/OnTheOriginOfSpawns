"""Make the silverfish region map.

Usage: python3 tools/make_silverfish_region_map.py (from the root of the repository)

The silverfish texture (textures/entity/silverfish/silverfish.png) is 64x32.
SilverfishModel has seven body segments in a row, from the front to the
back, and three back layers. Each segment stands on the ground.
- segment0, the head: 3x2x2 at uv (0, 0). Its front has two black eyes at
  (2, 2) and (4, 2).
- segment1: 4x3x2 at uv (0, 4).
- segment2: 6x4x3 at uv (0, 9).
- segment3: 3x3x3 at uv (0, 16).
- segment4: 2x2x3 at uv (0, 22).
- segment5: 2x1x2 at uv (11, 0).
- segment6: 1x1x2 at uv (13, 4).
- layer0: 10x8x3 at uv (20, 0), round segment2.
- layer1: 6x4x3 at uv (20, 11), round segment4.
- layer2: 6x5x2 at uv (20, 18), round segment1.
The layers are larger than the segments in them. Most pixels of a layer are
clear. The few opaque pixels are bristles that stand round the body.

The design uses the regions head, body, back, and tail. The head is segment0.
The body is segment1 to segment4. The tail is segment5 and segment6. The back
is the three layers. The map uses these region ids of the humanoid map:
- face (1): the front of the head.
- scalp (2): the rest of the head.
- torso (3): the faces of the body segments, except the tops.
- spine (10): the tops of the body segments, the back of the bug, and the
  bristles of the three layers.
- pelvis (11): the tail.
- eye_sockets (13): the two eyes.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/silverfish.png"

FACE, SCALP, TORSO, SPINE, PELVIS, EYE_SOCKETS = 1, 2, 3, 10, 11, 13
SKIN, EYES = 1, 3

EYE_PIXELS = {(2, 2), (4, 2)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    return (FACE if face == "front" else SCALP), SKIN


def body(x, y, face, row):
    return (SPINE if face == "top" else TORSO), SKIN


def tail(x, y, face, row):
    return PELVIS, SKIN


def back(x, y, face, row):
    return SPINE, SKIN


PARTS = [
    ((0, 0), (3, 2, 2), head),
    ((0, 4), (4, 3, 2), body),
    ((0, 9), (6, 4, 3), body),
    ((0, 16), (3, 3, 3), body),
    ((0, 22), (2, 2, 3), body),
    ((11, 0), (2, 1, 2), tail),
    ((13, 4), (1, 1, 2), tail),
    ((20, 0), (10, 8, 3), back),
    ((20, 11), (6, 4, 3), back),
    ((20, 18), (6, 5, 2), back),
]


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
