"""Make the phantom region map.

Usage: python3 tools/make_phantom_region_map.py (from the root of the repository)

The phantom texture (textures/entity/phantom/phantom.png) is 64x64.
PhantomModel has these parts. All of them are children of the body:
- the body: 5x3x9 at uv (0, 8). Its top is the back.
- the head: 7x3x5 at uv (0, 0). The eyes are on row 1 of its front, in
  columns 0 and 1 and columns 5 and 6. The glowing eyes (phantom_eyes.png)
  go over them.
- the tail: tail_base, 3x2x6 at uv (3, 20), and its child tail_tip, 1x1x6 at
  uv (4, 29).
- the wings: left_wing_base, 6x2x9 at uv (23, 12), and its child
  left_wing_tip, 13x1x9 at uv (16, 24). The right wing uses the same uv,
  mirrored, so a mark on a wing goes on both wings.

The design uses the regions head, body, wings, and tail. The map uses these
region ids of the humanoid map:
- face (1): the front of the head. scalp (2): the other faces of the head.
- torso (3): the body.
- arms (4): the wing bases. hands (5): the wing tips.
- legs (6): the tail.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import ARMS, FACE, HANDS, LEGS, SCALP, TORSO, box

OUT = "src/main/resources/assets/originofspawns/textures/region/phantom.png"

SKIN, EYES = 1, 3
EYE_PIXELS = {(5, 6), (6, 6), (10, 6), (11, 6)}

PARTS = [
    ((0, 0), (7, 3, 5), lambda f: FACE if f == "front" else SCALP),
    ((0, 8), (5, 3, 9), lambda f: TORSO),
    ((3, 20), (3, 2, 6), lambda f: LEGS),
    ((4, 29), (1, 1, 6), lambda f: LEGS),
    ((23, 12), (6, 2, 9), lambda f: ARMS),
    ((16, 24), (13, 1, 9), lambda f: HANDS),
]


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for (u, v), (w, h, d), region in PARTS:
        for x, y, face, row in box(u, v, w, h, d):
            im.putpixel((x, y), (region(face), EYES if (x, y) in EYE_PIXELS else SKIN, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
