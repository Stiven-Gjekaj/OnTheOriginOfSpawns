"""Make the blaze region map.

Usage: python3 tools/make_blaze_region_map.py (from the root of the repository)

The blaze texture (textures/entity/blaze/blaze.png) is 64x32. BlazeModel has
these parts:
- the head: 8x8x8 at uv (0, 0). The eyes are on row 3 of its front, at x 9
  to 10 and 13 to 14.
- twelve rods, part0 to part11, in three rings of four round the body. Each
  rod is 2x8x2, and all of them use uv (0, 16), so a mark on a rod goes on
  all twelve rods.

The design uses the regions head and rods. The map uses these region ids of
the humanoid map:
- face (1): the front of the head. scalp (2): the other faces of the head.
- arms (4): the rods, rows 0 to 3 (the high half). hands (5): rows 4 to 7
  and the bottom (the low half).
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import ARMS, FACE, HANDS, SCALP, box

OUT = "src/main/resources/assets/originofspawns/textures/region/blaze.png"

SKIN, EYES = 1, 3
EYE_PIXELS = {(9, 11), (10, 11), (13, 11), (14, 11)}


def make():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for x, y, face, row in box(0, 0, 8, 8, 8):
        region = FACE if face == "front" else SCALP
        im.putpixel((x, y), (region, EYES if (x, y) in EYE_PIXELS else SKIN, 0, 255))
    for x, y, face, row in box(0, 16, 2, 8, 2):
        region = HANDS if face == "bottom" or (row is not None and row >= 4) else ARMS
        im.putpixel((x, y), (region, SKIN, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
