"""Make the creaking region map.

Usage: python3 tools/make_creaking_region_map.py (from the root of the repository)

The creaking texture (textures/entity/creaking/creaking.png) is 64x64. Its
glow layer (creaking_eyes.png) has the three orange eyes of the face, at
(6, 8) to (8, 8), (9, 10) to (11, 10), and (7, 13) to (9, 13). CreakingModel
has these parts:
- the head: a block, 6x10x6 at uv (0, 0), a crown, 6x3x6 at uv (28, 31),
  and two flat branches, 9x14 at uv (12, 40) and (34, 12).
- the body: 6x13x5 at uv (0, 16) and 6x7x5 at uv (24, 0).
- the right arm: 3x21x3 at uv (22, 13), and the hand, 3x4x3 at uv (46, 0).
- the left arm: 3x16x3 at uv (30, 40), the shoulder, 3x4x3 at uv (52, 12),
  and the hand, 3x4x3 at uv (52, 19).
- the right leg: 3x19x3 at uv (0, 34), the hip, 3x3x3 at uv (12, 34), and
  a flat foot, 5x9 at uv (45, 46).
- the left leg: 3x16x3 at uv (42, 40), and a flat foot, 5x9 at uv (45, 55).

The design uses the regions head, body, arms, and legs. The map uses these
region ids of the humanoid map:
- face (1): the front of the head block.
- scalp (2): the rest of the head block, the crown, and the branches.
- eye_sockets (13): the three eyes.
- torso (3): the body.
- arms (4): the arms and the shoulder. hands (5): the two hands.
- legs (6): the legs and the hip. feet (8): the two feet.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/creaking.png"

FACE, SCALP, TORSO, ARMS, HANDS, LEGS, FEET, EYE_SOCKETS = 1, 2, 3, 4, 5, 6, 8, 13
SKIN, EYES = 1, 3

EYE_PIXELS = {(6, 8), (7, 8), (8, 8), (9, 10), (10, 10), (11, 10), (7, 13), (8, 13), (9, 13)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    return (FACE if face == "front" else SCALP), SKIN


def region(rid):
    return lambda x, y, face, row: (rid, SKIN)


PARTS = [
    ((0, 0), (6, 10, 6), head),
    ((28, 31), (6, 3, 6), region(SCALP)),
    ((12, 40), (9, 14, 0), region(SCALP)),
    ((34, 12), (9, 14, 0), region(SCALP)),
    ((0, 16), (6, 13, 5), region(TORSO)),
    ((24, 0), (6, 7, 5), region(TORSO)),
    ((22, 13), (3, 21, 3), region(ARMS)),
    ((46, 0), (3, 4, 3), region(HANDS)),
    ((30, 40), (3, 16, 3), region(ARMS)),
    ((52, 12), (3, 4, 3), region(ARMS)),
    ((52, 19), (3, 4, 3), region(HANDS)),
    ((0, 34), (3, 19, 3), region(LEGS)),
    ((12, 34), (3, 3, 3), region(LEGS)),
    ((45, 46), (5, 0, 9), region(FEET)),
    ((42, 40), (3, 16, 3), region(LEGS)),
    ((45, 55), (5, 0, 9), region(FEET)),
]


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for (u, v), (w, h, d), rule in PARTS:
        for x, y, face, row in box(u, v, w, h, d):
            r, material = rule(x, y, face, row)
            im.putpixel((x, y), (r, material, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
