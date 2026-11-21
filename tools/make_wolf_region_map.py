"""Make the wolf region map.

Usage: python3 tools/make_wolf_region_map.py (from the root of the repository)

The wolf textures (textures/entity/wolf/wolf*.png) are 64x32. The 9 variants
and their tame and angry textures use the same layout. WolfRenderer uses
AdultWolfModel for an adult. The baby uses BabyWolfModel and its own 32x32
textures. This map is for the adult. AdultWolfModel has these parts:
- head, an empty bone. Its child real_head has the head, 6x6x4 at uv (0, 0),
  the two ears, 2x2x1, both at uv (16, 14), and the snout, 3x3x4 at
  uv (0, 10).
- body, the back half of the body: 6x9x6 at uv (18, 14). It turns by 90
  degrees round x, so its back face is the back of the wolf, its front face
  is the belly, and its bottom face is the rump.
- upper_body, the mane: 8x6x7 at uv (21, 0). It also turns by 90 degrees round
  x. Its top face is the chest, and row 0 of its sides is at the neck.
- the four legs: 2x8x2, all at uv (0, 18). The right legs are mirrored.
- tail, an empty bone. Its child real_tail is 2x8x2 at uv (9, 18).
The eyes are on the face, in row 2: x 4 and 5, and 8 and 9, at y 6.
The collar of a tame wolf is a separate texture. It covers the edge of the
chest and row 0 of the sides of the mane.

The design uses the regions head, body, and legs. The map uses these region
ids of the humanoid map:
- face (1): the face of the head and the snout. scalp (2): the other faces of
  the head, and the ears.
- torso (3): the back half of the body. spine (10): the mane. pelvis (11): the
  tail.
- legs (6), lower legs (7), and feet (8): rows 0 to 3 of the legs and the top,
  rows 4 to 6, and the paws, row 7 and the bottom.
The material is MATERIAL (skin, 1), and eyes (3) on the eyes. Red is the
region id. Green is the material id. Blue is 0. A pixel with alpha 0 is not
used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import FACE, FEET, LEGS, LOWER_LEGS, SCALP, TORSO, box

OUT = "src/main/resources/assets/originofspawns/textures/region/wolf.png"

SPINE, PELVIS = 10, 11
SKIN, EYES = 1, 3
MATERIAL = SKIN

EYE_PIXELS = {(4, 6), (5, 6), (8, 6), (9, 6)}


def leg(face, row):
    if face == "bottom" or face != "top" and row >= 7:
        return FEET
    if face == "top":
        return LEGS
    return LOWER_LEGS if row >= 4 else LEGS


# Each part: box UV origin, size, region rule.
PARTS = {
    "head": ((0, 0), (6, 6, 4), lambda f, r: FACE if f == "front" else SCALP),
    "ear": ((16, 14), (2, 2, 1), lambda f, r: SCALP),
    "snout": ((0, 10), (3, 3, 4), lambda f, r: FACE),
    "body": ((18, 14), (6, 9, 6), lambda f, r: TORSO),
    "mane": ((21, 0), (8, 6, 7), lambda f, r: SPINE),
    "leg": ((0, 18), (2, 8, 2), leg),
    "tail": ((9, 18), (2, 8, 2), lambda f, r: PELVIS),
}


def make(material=MATERIAL, eyes=EYE_PIXELS):
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
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
