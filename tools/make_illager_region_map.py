"""Make the illager region map.

Usage: python3 tools/make_illager_region_map.py (from the root of the repository)

The illager textures (textures/entity/illager/*.png) are 64x64. The illager
model (IllagerModel) has these parts:
- head: 8x10x8 at uv (0, 0), with a nose of 2x4x2 at uv (24, 0) and a hat of
  8x12x8 at uv (32, 0). The vanilla hat does not show.
- body: 8x12x6 at uv (16, 20), with a coat of 8x20x6 at uv (0, 38) over it.
- arms (crossed): 4x8x4 at uv (44, 22) and 8x4x4 at uv (40, 38).
- right_arm and left_arm: 4x12x4 at uv (40, 46). The left arm mirrors it.
- right_leg and left_leg: 4x12x4 at uv (0, 22). The left leg mirrors it.

The map uses the region ids of the humanoid map, so that no new id is
needed:
- face (1): the front of the head, and the nose.
- scalp (2): the rest of the head, and the hat.
- torso (3): the front, the sides, the top, and the bottom of the body.
- arms (4): rows 0 to 9 of the arms, the top of the arms, and the crossed
  arms.
- hands (5): rows 10 and 11 of the arms, the bottom of the arms, and the
  hands in the middle of the bar of the crossed arms (HAND_PIXELS). The
  vindicator shows its hands there.
- legs (6): rows 0 to 5 of the legs, and the top of the legs.
- feet (8): rows 6 to 11 of the legs, and the bottom of the legs. On the
  pillager, these are the boots.
- spine (10): the back face of the body.
- pelvis (11): the coat.
- eye_sockets (13): the eyes on the face.
The material is skin (1) on the head and the hands, cloth (2) on the hat,
the body, the coat, the arms, and the legs, and eyes (3) on the eyes. Red
is the region id. Green is the material id. Blue is 0. A pixel with alpha 0
is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/illager.png"

FACE, SCALP, TORSO, ARMS, HANDS, LEGS, FEET, SPINE, PELVIS, EYE_SOCKETS = 1, 2, 3, 4, 5, 6, 8, 10, 11, 13
SKIN, CLOTH, EYES = 1, 2, 3

# The white and the green pixels of the eyes on the face.
EYE_PIXELS = {(9, 14), (10, 14), (13, 14), (14, 14)}

# The hands on the bar of the crossed arms (8x4x4 at uv (40, 38)): the middle
# of its top, its front, its bottom, and its back. The vindicator draws its
# hands there.
HAND_PIXELS = ({(x, y) for x in range(46, 51) for y in range(38, 42)}
               | {(x, y) for x in range(46, 51) for y in range(42, 46)}
               | {(x, y) for x in range(54, 59) for y in range(38, 42)}
               | {(x, y) for x in range(57, 62) for y in range(42, 46)})


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


def coat(x, y, face, row):
    return PELVIS, CLOTH


def crossed_arms(x, y, face, row):
    if (x, y) in HAND_PIXELS:
        return HANDS, SKIN
    return ARMS, CLOTH


def arm(x, y, face, row):
    if face == "bottom" or row is not None and row >= 10:
        return HANDS, SKIN
    return ARMS, CLOTH


def leg(x, y, face, row):
    if face == "bottom" or row is not None and row >= 6:
        return FEET, CLOTH
    return LEGS, CLOTH


PARTS = [((0, 0), (8, 10, 8), head), ((24, 0), (2, 4, 2), nose), ((32, 0), (8, 12, 8), hat),
         ((16, 20), (8, 12, 6), body), ((0, 38), (8, 20, 6), coat), ((44, 22), (4, 8, 4), crossed_arms),
         ((40, 38), (8, 4, 4), crossed_arms), ((40, 46), (4, 12, 4), arm), ((0, 22), (4, 12, 4), leg)]


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
