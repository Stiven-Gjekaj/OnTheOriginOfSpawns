"""Make the enderman region map.

Usage: python3 tools/make_enderman_region_map.py (from the root of the repository)

The enderman texture (textures/entity/enderman/enderman.png) is 64x32. The
enderman model (EndermanModel) is a HumanoidModel with these parts:
- head: 8x8x8 at uv (0, 0), and a jaw of 8x8x8 at uv (0, 16), 0.5 pixel
  smaller, that is the part "hat" of the head.
- body: 8x12x4 at uv (32, 16).
- right_arm, left_arm, right_leg, and left_leg: 2x30x2, all at uv (56, 0).
  The left limbs mirror the right limbs. So the arms and the legs use the same
  pixels, and a mark on them shows on all four limbs.
The eyes layer (enderman_eyes.png) draws the glowing eyes over the texture.

The map uses the region ids of the humanoid map:
- face (1): the front of the head and of the jaw.
- scalp (2): the rest of the head and of the jaw.
- torso (3): the front, the sides, the top, and the bottom of the body.
- legs (6): rows 0 to 25 of the limbs, and the top of the limbs.
- feet (8): rows 26 to 29 of the limbs, and the bottom of the limbs. On the
  arms, these are the hands.
- spine (10): the back face of the body.
- eye_sockets (13): the pixels of the glowing eyes.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/enderman.png"

FACE, SCALP, TORSO, LEGS, FEET, SPINE, EYE_SOCKETS = 1, 2, 3, 6, 8, 10, 13
SKIN, EYES = 1, 3

# The pixels that enderman_eyes.png draws.
EYE_PIXELS = {(8, 12), (9, 12), (10, 12), (13, 12), (14, 12), (15, 12)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    return (FACE if face == "front" else SCALP), SKIN


def body(x, y, face, row):
    return (SPINE if face == "back" else TORSO), SKIN


def limb(x, y, face, row):
    if face == "bottom" or row is not None and row >= 26:
        return FEET, SKIN
    return LEGS, SKIN


PARTS = [((0, 0), (8, 8, 8), head), ((0, 16), (8, 8, 8), head), ((32, 16), (8, 12, 4), body),
         ((56, 0), (2, 30, 2), limb)]


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
