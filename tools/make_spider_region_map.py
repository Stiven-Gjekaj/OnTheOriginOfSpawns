"""Make the spider region map.

Usage: python3 tools/make_spider_region_map.py (from the root of the repository)

The spider texture (textures/entity/spider/spider.png) is 64x32. The spider
model (SpiderModel) has a head (8x8x8) at uv (32, 4), a body (body0, 6x6x6)
at uv (0, 0), an abdomen (body1, 10x8x12) at uv (0, 12), and eight legs
(16x2x2) that all use uv (18, 0). The left legs mirror the right legs.

The map uses the region ids of the humanoid map, so that no new id is
needed:
- face (1): the front of the head. The fangs are on the face, with the
  material bone (4).
- scalp (2): the rest of the head.
- torso (3): the body.
- legs (6): the legs, from the body to the joints.
- feet (8): the 3 pixels at the tip of each leg, and the end of the tip.
- spine (10): the back. This is the top of the abdomen.
- pelvis (11): the abdomen. This is the sides, the bottom, and the two ends
  of the abdomen.
- joints (12): the two joints of each leg, 5 and 10 pixels from the tip.
- eye_sockets (13): the eyes, where spider_eyes.png draws. The material is
  eyes (3).
The material is skin (1), bone (4) on the fangs, and eyes (3) on the eyes.
Red is the region id. Green is the material id. Blue is 0. A pixel with
alpha 0 is not used.

On the legs, the tip is column 0 of the front, the top, and the bottom, and
column 15 of the back. The right face is the end of the tip. The mirror of
the left legs keeps this.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/spider.png"

FACE, SCALP, TORSO, LEGS, FEET, SPINE, PELVIS, JOINTS, EYE_SOCKETS = 1, 2, 3, 6, 8, 10, 11, 12, 13
SKIN, EYES, BONE = 1, 3, 4

# The pixels that spider_eyes.png draws: the eyes on the front and the sides
# of the head.
EYE_PIXELS = {(39, 14), (39, 15), (40, 14), (40, 15), (41, 12), (42, 13), (42, 15), (42, 16), (43, 15),
              (43, 16), (44, 15), (44, 16), (45, 13), (45, 15), (45, 16), (46, 12), (47, 14), (47, 15),
              (48, 14), (48, 15)}
# The two pale fangs at the bottom of the face.
FANG_PIXELS = {(41, 18), (41, 19), (46, 18), (46, 19)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    if (x, y) in FANG_PIXELS:
        return FACE, BONE
    return (FACE if face == "front" else SCALP), SKIN


def body(x, y, face, row):
    return TORSO, SKIN


def abdomen(x, y, face, row):
    return (SPINE if face == "top" else PELVIS), SKIN


def from_tip(x, face):
    """Return how far the leg pixel is from the tip, from 0 to 15."""
    if face == "right":
        return 0
    if face == "left":
        return 15
    if face == "back":
        return 53 - x
    return x - (36 if face == "bottom" else 20)


def leg(x, y, face, row):
    t = from_tip(x, face)
    if t <= 2:
        return FEET, SKIN
    return (JOINTS if t in (5, 10) else LEGS), SKIN


PARTS = [((32, 4), (8, 8, 8), head), ((0, 0), (6, 6, 6), body), ((0, 12), (10, 8, 12), abdomen),
         ((18, 0), (16, 2, 2), leg)]


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
