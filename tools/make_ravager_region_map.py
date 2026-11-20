"""Make the ravager region map.

Usage: python3 tools/make_ravager_region_map.py (from the root of the repository)

The ravager texture (textures/entity/illager/ravager.png) is 128x128.
RavagerModel has these parts:
- the neck: 10x10x18 at uv (68, 73). The head is a child of the neck. At
  rest, the head and the body cover almost all of the neck.
- the head: 16x20x16 at uv (0, 0), and the snout, 4x8x4 at uv (0, 0), in
  front of the low middle of the face. The eyes are in row 13 of the front
  of the head, at (18, 29) to (21, 29) and (26, 29) to (29, 29).
- the horns: 2x14x4 at uv (74, 55), children of the head. Both horns use
  the same pixels.
- the mouth: 16x3x16 at uv (0, 36), a child of the head. Its top is clear.
- the body: 14x16x20 at uv (0, 55) and 12x13x18 at uv (0, 91). The body
  turns by 90 degrees round x, so the back face of each cube is the back of
  the animal, and the front face is the belly. The saddle is on the back of
  the first cube.
- the legs: 8x37x8 at uv (64, 0) for the front legs and at uv (96, 0) for
  the hind legs. The two legs of a pair use the same pixels.

The design uses the regions head, horns, mouth, neck, body, and legs. The
map uses these region ids of the humanoid map:
- face (1): the front of the head and the snout.
- scalp (2): the rest of the head.
- eye_sockets (13): the eyes.
- arms (4): the horns.
- joints (12): the mouth.
- spine (10): the neck.
- torso (3): the body.
- legs (6): the legs.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/ravager.png"

FACE, SCALP, TORSO, ARMS, LEGS, SPINE, JOINTS, EYE_SOCKETS = 1, 2, 3, 4, 6, 10, 12, 13
SKIN, EYES = 1, 3

EYE_PIXELS = {(x, 29) for x in (18, 19, 20, 21, 26, 27, 28, 29)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS, EYES
    return (FACE if face == "front" else SCALP), SKIN


def snout(x, y, face, row):
    return FACE, SKIN


def region(rid):
    return lambda x, y, face, row: (rid, SKIN)


PARTS = [
    ((68, 73), (10, 10, 18), region(SPINE)),
    ((0, 0), (16, 20, 16), head),
    ((0, 0), (4, 8, 4), snout),
    ((74, 55), (2, 14, 4), region(ARMS)),
    ((0, 36), (16, 3, 16), region(JOINTS)),
    ((0, 55), (14, 16, 20), region(TORSO)),
    ((0, 91), (12, 13, 18), region(TORSO)),
    ((96, 0), (8, 37, 8), region(LEGS)),
    ((64, 0), (8, 37, 8), region(LEGS)),
]


def make():
    im = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    for (u, v), (w, h, d), rule in PARTS:
        for x, y, face, row in box(u, v, w, h, d):
            r, material = rule(x, y, face, row)
            im.putpixel((x, y), (r, material, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
