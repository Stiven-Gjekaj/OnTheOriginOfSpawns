"""Make the slime region map.

Usage: python3 tools/make_slime_region_map.py (from the root of the repository)

The slime texture (textures/entity/slime/slime.png) is 64x32. The slime has
two models:
- SlimeModel, the main model: an inner cube of 6x6x6 at uv (0, 16) (the part
  "cube"), two eyes of 2x2x2 at uv (32, 0) and (32, 4), and a mouth of 1x1x1
  at uv (32, 8).
- the outer gel, in SlimeOuterLayer: a cube of 8x8x8 at uv (0, 0). Its
  pixels have alpha 180, so it is clear.

The design uses the regions body (the outer gel), core (the inner cube), and
face (the eyes and the mouth). The map uses these region ids of the
humanoid map:
- torso (3): the body, the outer gel.
- ribs (9): the core, the inner cube.
- face (1): the eyes and the mouth.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/slime.png"

FACE, TORSO, RIBS = 1, 3, 9
SKIN, EYES = 1, 3

PARTS = [((0, 0), (8, 8, 8), (TORSO, SKIN)), ((0, 16), (6, 6, 6), (RIBS, SKIN)),
         ((32, 0), (2, 2, 2), (FACE, EYES)), ((32, 4), (2, 2, 2), (FACE, EYES)), ((32, 8), (1, 1, 1), (FACE, SKIN))]


def make():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for (u, v), (w, h, d), (region, material) in PARTS:
        for x, y, face, row in box(u, v, w, h, d):
            im.putpixel((x, y), (region, material, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
