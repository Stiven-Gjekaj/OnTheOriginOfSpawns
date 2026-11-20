"""Make the zombie horse region map.

Usage: python3 tools/make_zombie_horse_region_map.py (from the root of the repository)

The zombie horse texture (textures/entity/horse/horse_zombie.png) is 64x64.
UndeadHorseRenderer uses HorseModel, with the mesh of AbstractEquineModel,
not scaled. The baby uses BabyHorseModel and its own texture,
horse_zombie_baby.png. This map is for the adult. AbstractEquineModel has
these parts:
- the body: 10x10x22 at uv (0, 32). The tail, a child of the body: 3x14x4 at
  uv (42, 36).
- head_parts, the neck: 4x12x7 at uv (0, 35). Its children have no offset:
  the head, 6x5x7 at uv (0, 13); the mane, 2x16x2 at uv (56, 36); and the
  upper mouth (the snout), 4x5x5 at uv (0, 25). The ears are children of the
  head: 2x3x1, both at uv (19, 16).
- the four legs: 4x11x4, all at uv (48, 21). The left legs are mirrored.
The eyes are on the sides of the head, in row 1: x 3 and 4, and 15 and 16,
at y 21.

The design uses the regions head, mane, body, legs, and tail. The map uses
these region ids of the humanoid map:
- face (1): the top of the head and the snout. scalp (2): the other faces of
  the head, and the ears.
- torso (3): the body and the neck. spine (10): the mane. pelvis (11): the
  tail.
- legs (6), lower legs (7), and feet (8): rows 0 to 5 of the legs, rows 6 to
  8, and the hooves, rows 9 and 10 and the bottom.
The material is MATERIAL (skin, 1), and eyes (3) on the eyes. Red is the
region id. Green is the material id. Blue is 0. A pixel with alpha 0 is not
used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import FACE, FEET, LEGS, LOWER_LEGS, SCALP, TORSO, box

OUT = "src/main/resources/assets/originofspawns/textures/region/zombie_horse.png"

SPINE, PELVIS = 10, 11
SKIN, EYES = 1, 3
MATERIAL = SKIN

EYE_PIXELS = {(3, 21), (4, 21), (15, 21), (16, 21)}


def leg(face, row):
    if face == "bottom" or face != "top" and row >= 9:
        return FEET
    if face == "top":
        return LEGS
    return LOWER_LEGS if row >= 6 else LEGS


# Each part: box UV origin, size, region rule.
PARTS = {
    "body": ((0, 32), (10, 10, 22), lambda f, r: TORSO),
    "tail": ((42, 36), (3, 14, 4), lambda f, r: PELVIS),
    "neck": ((0, 35), (4, 12, 7), lambda f, r: TORSO),
    "head": ((0, 13), (6, 5, 7), lambda f, r: FACE if f == "top" else SCALP),
    "mane": ((56, 36), (2, 16, 2), lambda f, r: SPINE),
    "upper_mouth": ((0, 25), (4, 5, 5), lambda f, r: FACE),
    "ear": ((19, 16), (2, 3, 1), lambda f, r: SCALP),
    "leg": ((48, 21), (4, 11, 4), leg),
}


def make(material=MATERIAL, eyes=EYE_PIXELS):
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for name, ((u, v), (w, h, d), region) in PARTS.items():
        for x, y, face, row in box(u, v, w, h, d):
            m = EYES if (x, y) in eyes else material
            im.putpixel((x, y), (region(face, row), m, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
