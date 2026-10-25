"""Make the breeze region map.

Usage: python3 tools/make_breeze_region_map.py (from the root of the repository)

The breeze texture (textures/entity/breeze/breeze.png) is 32x32. The body
model of BreezeModel has these parts:
- the head: 8x8x8 at uv (0, 0), on the bone head. The eyes are on row 6 of
  its front, in columns 0 and 7, and they go round the corners onto the
  sides. The glowing eyes (breeze_eyes.png) go over them.
- a band of wind round the face, 10x3x4 at uv (4, 24), on the bone head. Its
  white wisps show under the eyes.
- three rods, rod_1, rod_2, and rod_3, 2x8x2 at uv (0, 17), on the bone rods.
  All three rods use the same pixels.
The wind is a second model with its own texture, breeze_wind.png (128x128).
A wind mark has the target "wind" and goes on that texture, so this map does
not cover the wind.

The design uses the regions head, rods, and wind. The map uses these region
ids of the humanoid map:
- face (1): the front of the head, and the band of wind.
- scalp (2): the other faces of the head.
- arms (4): rows 0 to 3 of the rods. hands (5): rows 4 to 7 and the bottom.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import ARMS, FACE, HANDS, SCALP, box

OUT = "src/main/resources/assets/originofspawns/textures/region/breeze.png"

SKIN, EYES = 1, 3
EYE_PIXELS = {(7, 14), (8, 14), (15, 14), (16, 14)}


def make():
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    for x, y, face, row in box(0, 0, 8, 8, 8):
        region = FACE if face == "front" else SCALP
        im.putpixel((x, y), (region, EYES if (x, y) in EYE_PIXELS else SKIN, 0, 255))
    for x, y, face, row in box(4, 24, 10, 3, 4):
        im.putpixel((x, y), (FACE, SKIN, 0, 255))
    for x, y, face, row in box(0, 17, 2, 8, 2):
        region = HANDS if face == "bottom" or (row is not None and row >= 4) else ARMS
        im.putpixel((x, y), (region, SKIN, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
