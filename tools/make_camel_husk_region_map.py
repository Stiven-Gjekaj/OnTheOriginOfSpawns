"""Make the camel husk region map.

Usage: python3 tools/make_camel_husk_region_map.py (from the root of the repository)

The camel husk texture (textures/entity/camel/camel_husk.png) is 128x128. Only
the high 64 rows hold pixels. CamelHuskRenderer uses AdultCamelModel, the
model of the adult camel. AdultCamelModel has these parts:
- the body: 15x12x27 at uv (0, 25). Its top is the back of the animal.
- the hump, a child of the body: 9x5x11 at uv (74, 0).
- the tail, a child of the body: a flat plane 3x14x0 at uv (122, 0).
- the head, a child of the body, with three cubes: the low neck, 7x8x19 at
  uv (60, 24), that goes forward from the body; the high neck, 7x14x7 at
  uv (21, 0), that stands up; and the snout, 5x5x6 at uv (50, 0), at the
  front of the top of the high neck.
- the ears, left_ear and right_ear, children of the head: 3x1x2 at uv
  (45, 0) and (67, 0).
- the legs, left_hind_leg (58, 16), right_hind_leg (94, 16), left_front_leg
  (0, 0), and right_front_leg (0, 26): 5x21x5.
The eyes are on the high neck, where its two sides meet its front, in row 1:
x 27 and 28, and 34 and 35, at y 8. The camel texture has dark eyes there.
The camel husk texture is dark on all of row 1, so its eyes do not show.

The design uses the regions head, body, and legs. The map uses these region
ids of the humanoid map:
- face (1): the snout, and the top and rows 0 to 5 of the high neck.
- scalp (2): the other pixels of the head bone, and the ears.
- torso (3): the body and the tail. spine (10): the hump.
- legs (6), lower legs (7), and feet (8): rows 0 to 9 of the legs, rows 10 to
  18, and the hooves, rows 19 and 20 and the bottom.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
The saddle is not a region. It is a layer of equipment with its own texture.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import FACE, FEET, LEGS, LOWER_LEGS, SCALP, TORSO, box

OUT = "src/main/resources/assets/originofspawns/textures/region/camel_husk.png"

SPINE = 10
SKIN, EYES = 1, 3

EYE_PIXELS = {(27, 8), (28, 8), (34, 8), (35, 8)}


def leg(face, row):
    if face == "bottom" or face != "top" and row >= 19:
        return FEET
    if face == "top":
        return LEGS
    return LOWER_LEGS if row >= 10 else LEGS


def high_neck(face, row):
    if face == "top" or face not in ("bottom",) and row <= 5:
        return FACE
    return SCALP


# Each part: box UV origin, size, region rule.
PARTS = {
    "body": ((0, 25), (15, 12, 27), lambda f, r: TORSO),
    "hump": ((74, 0), (9, 5, 11), lambda f, r: SPINE),
    "tail": ((122, 0), (3, 14, 0), lambda f, r: TORSO),
    "low_neck": ((60, 24), (7, 8, 19), lambda f, r: SCALP),
    "high_neck": ((21, 0), (7, 14, 7), high_neck),
    "snout": ((50, 0), (5, 5, 6), lambda f, r: FACE),
    "left_ear": ((45, 0), (3, 1, 2), lambda f, r: SCALP),
    "right_ear": ((67, 0), (3, 1, 2), lambda f, r: SCALP),
    "left_hind_leg": ((58, 16), (5, 21, 5), leg),
    "right_hind_leg": ((94, 16), (5, 21, 5), leg),
    "left_front_leg": ((0, 0), (5, 21, 5), leg),
    "right_front_leg": ((0, 26), (5, 21, 5), leg),
}


def make():
    im = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    for name, ((u, v), (w, h, d), region) in PARTS.items():
        for x, y, face, row in box(u, v, w, h, d):
            m = EYES if (x, y) in EYE_PIXELS else SKIN
            im.putpixel((x, y), (region(face, row), m, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
