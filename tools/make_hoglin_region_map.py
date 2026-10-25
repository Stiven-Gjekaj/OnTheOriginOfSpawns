"""Make the hoglin region map.

Usage: python3 tools/make_hoglin_region_map.py (from the root of the repository)

The hoglin and the zoglin use this map. Their textures are 128x64. HoglinModel
has these parts:
- the body: 16x14x26 at uv (1, 1). Its top is the back of the animal.
- the mane, a child of the body: a flat plane 0x10x19 at uv (90, 33). Only
  its two sides show, at (90, 52) and (109, 52). The zoglin texture has no
  mane, so its mane pixels are clear.
- the head: 14x6x19 at uv (61, 1). The head tips down by 50 degrees, so its
  top (80, 1) is the face, with the eyes, and its front (80, 20) is the end of
  the snout.
- the ears, right_ear and left_ear, children of the head: 6x1x4 at uv (1, 1)
  and (1, 6).
- the tusks, right_horn and left_horn, children of the head: 2x11x2 at uv
  (10, 13) and (1, 13).
- the front legs, right_front_leg and left_front_leg: 6x14x6 at uv (66, 42)
  and (41, 42). The hind legs, right_hind_leg and left_hind_leg: 5x11x5 at uv
  (21, 45) and (0, 45).
The eyes are on the face, at x 80 to 81 and 92 to 93, y 8 to 9.

The map uses the region ids of the humanoid map:
- face (1): the top and the front of the head, and the tusks.
- scalp (2): the other faces of the head, and the ears.
- torso (3): the body. spine (10): the mane.
- legs (6), lower legs (7), and feet (8): the high half of the legs, the low
  half, and the hooves, the low two rows and the bottom.
The material is skin (1), cloth (2) on the mane, eyes (3) on the eyes, and
bone (4) on the tusks. Red is the region id. Green is the material id. Blue
is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import FACE, FEET, LEGS, LOWER_LEGS, SCALP, TORSO, box

OUT = "src/main/resources/assets/originofspawns/textures/region/hoglin.png"

SPINE = 10
SKIN, CLOTH, EYES, BONE = 1, 2, 3, 4

EYE_PIXELS = {(80, 8), (81, 8), (80, 9), (81, 9), (92, 8), (93, 8), (92, 9), (93, 9)}


def leg_rule(h):
    def region(face, row):
        if face == "bottom" or face != "top" and row >= h - 2:
            return FEET
        if face == "top":
            return LEGS
        return LOWER_LEGS if row >= h // 2 else LEGS
    return region


# Each part: box UV origin, size, region rule, material.
PARTS = {
    "body": ((1, 1), (16, 14, 26), lambda f, r: TORSO, SKIN),
    "mane": ((90, 33), (0, 10, 19), lambda f, r: SPINE, CLOTH),
    "head": ((61, 1), (14, 6, 19), lambda f, r: FACE if f in ("top", "front") else SCALP, SKIN),
    "right_ear": ((1, 1), (6, 1, 4), lambda f, r: SCALP, SKIN),
    "left_ear": ((1, 6), (6, 1, 4), lambda f, r: SCALP, SKIN),
    "right_horn": ((10, 13), (2, 11, 2), lambda f, r: FACE, BONE),
    "left_horn": ((1, 13), (2, 11, 2), lambda f, r: FACE, BONE),
    "right_front_leg": ((66, 42), (6, 14, 6), leg_rule(14), SKIN),
    "left_front_leg": ((41, 42), (6, 14, 6), leg_rule(14), SKIN),
    "right_hind_leg": ((21, 45), (5, 11, 5), leg_rule(11), SKIN),
    "left_hind_leg": ((0, 45), (5, 11, 5), leg_rule(11), SKIN),
}


def make():
    im = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    for name, ((u, v), (w, h, d), region, material) in PARTS.items():
        for x, y, face, row in box(u, v, w, h, d):
            m = EYES if (x, y) in EYE_PIXELS else material
            im.putpixel((x, y), (region(face, row), m, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
