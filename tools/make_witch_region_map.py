"""Make the witch region map.

Usage: python3 tools/make_witch_region_map.py (from the root of the repository)

The witch texture (textures/entity/witch/witch.png) is 64x128. The witch
model (WitchModel) is the villager body, which has the parts and the uv of
the illager body, with these changes:
- head: 8x10x8 at uv (0, 0), with a nose of 2x4x2 at uv (24, 0), a mole of
  1x1x1 at uv (0, 0) on the nose, and a hat in four parts: a brim of 10x2x10
  at uv (0, 64), then 7x4x7 at uv (0, 76), 4x4x4 at uv (0, 87), and a tip of
  1x2x1 at uv (0, 95).
- body: 8x12x6 at uv (16, 20), with a robe of 8x20x6 at uv (0, 38) over it.
- arms: always crossed, 4x8x4 at uv (44, 22) and 8x4x4 at uv (40, 38).
- right_leg and left_leg: 4x12x4 at uv (0, 22).

The map uses the region ids of the humanoid map, as the illager map does:
- face (1): the front of the head, the nose, and the mole.
- scalp (2): the rest of the head, with the material skin, and the hat, with
  the material cloth.
- torso (3): the front, the sides, the top, and the bottom of the body.
- arms (4): the crossed arms.
- hands (5): the middle of the bar of the crossed arms, where the hands are
  (HAND_PIXELS of the illager map). The witch keeps its hands in its sleeves
  there.
- legs (6): rows 0 to 9 of the legs, and the top of the legs.
- feet (8): rows 10 and 11 of the legs, and the bottom of the legs: the shoes.
- spine (10): the back face of the body.
- pelvis (11): the robe.
- eye_sockets (13): the eyes on the face.
The material is skin (1) on the head and the hands, cloth (2) on the hat,
the body, the robe, the arms, and the legs, and eyes (3) on the eyes. Red is
the region id. Green is the material id. Blue is 0. A pixel with alpha 0 is
not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_illager_region_map import (ARMS, CLOTH, EYE_PIXELS, EYE_SOCKETS, EYES, FACE, FEET, HAND_PIXELS, HANDS,
                                     LEGS, PELVIS, SCALP, SKIN, SPINE, TORSO)
from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/witch.png"


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    return (FACE if face == "front" else SCALP), SKIN


def nose(x, y, face, row):
    return FACE, SKIN


def hat(x, y, face, row):
    return SCALP, CLOTH


def body(x, y, face, row):
    return (SPINE if face == "back" else TORSO), CLOTH


def robe(x, y, face, row):
    return PELVIS, CLOTH


def crossed_arms(x, y, face, row):
    if (x, y) in HAND_PIXELS:
        return HANDS, SKIN
    return ARMS, CLOTH


def leg(x, y, face, row):
    if face == "bottom" or row is not None and row >= 10:
        return FEET, CLOTH
    return LEGS, CLOTH


PARTS = [((0, 0), (8, 10, 8), head), ((24, 0), (2, 4, 2), nose), ((0, 0), (1, 1, 1), nose),
         ((0, 64), (10, 2, 10), hat), ((0, 76), (7, 4, 7), hat), ((0, 87), (4, 4, 4), hat), ((0, 95), (1, 2, 1), hat),
         ((16, 20), (8, 12, 6), body), ((0, 38), (8, 20, 6), robe), ((44, 22), (4, 8, 4), crossed_arms),
         ((40, 38), (8, 4, 4), crossed_arms), ((0, 22), (4, 12, 4), leg)]


def make():
    im = Image.new("RGBA", (64, 128), (0, 0, 0, 0))
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
