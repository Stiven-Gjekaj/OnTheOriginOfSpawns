"""Make the ghast region map.

Usage: python3 tools/make_ghast_region_map.py (from the root of the repository)

The ghast has two textures, ghast.png and ghast_shooting.png. Both are
128x64, and they are the same except the face: the shooting face has open
eyes and an open mouth. GhastModel is 64x32 in uv units, so each uv unit is
2x2 pixels of the texture. The model has these parts:
- the body, a cube of 16x16x16 at uv (0, 0), so its faces are 32x32 pixels.
  Its front, at (32, 32), is the face.
- nine tentacles, tentacle0 to tentacle8, each 2xLx2 at uv (0, 0), with L
  from 8 to 13. All the tentacles use the same pixels, from (0, 0) to (16,
  4 + 2L).
On the face, the eyes are from x 38 to 45 and 50 to 57, on y 42 and 43 when
they are closed and on y 40 to 43 when they shoot. The mouth is from x 44 to
51, on y 56 and 57 when it is closed and on y 52 to 61 when it shoots.

The design uses the regions body, face, and tentacles. The map uses these
region ids of the humanoid map:
- face (1): the front of the body.
- torso (3): the other faces of the body.
- legs (6): the tentacles.
The material is skin (1), and eyes (3) on the eyes of both faces. Red is the
region id. Green is the material id. Blue is 0. A pixel with alpha 0 is not
used. The map is 128x64, as the textures.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import FACE, LEGS, TORSO

OUT = "src/main/resources/assets/originofspawns/textures/region/ghast.png"

SKIN, EYES = 1, 3
LONGEST = 13


def eye(x, y):
    return (38 <= x <= 45 or 50 <= x <= 57) and 40 <= y <= 43


def make():
    im = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    # The body: in the box layout of a 16 cube, with 2 pixels for each uv
    # unit, the top and the bottom are on y 0 to 31, and the sides on y 32
    # to 63.
    for x in range(32, 96):
        for y in range(32):
            im.putpixel((x, y), (TORSO, SKIN, 0, 255))
    for x in range(128):
        for y in range(32, 64):
            region = FACE if 32 <= x < 64 else TORSO
            im.putpixel((x, y), (region, EYES if eye(x, y) else SKIN, 0, 255))
    # The tentacles: a 2xLx2 box at uv (0, 0). The top and the bottom are on
    # y 0 to 3, and the sides on y 4 to 4 + 2L.
    for x in range(4, 12):
        for y in range(4):
            im.putpixel((x, y), (LEGS, SKIN, 0, 255))
    for x in range(16):
        for y in range(4, 4 + 2 * LONGEST):
            im.putpixel((x, y), (LEGS, SKIN, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
