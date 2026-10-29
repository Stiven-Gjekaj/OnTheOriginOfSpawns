"""Make the guardian region map.

Usage: python3 tools/make_guardian_region_map.py (from the root of the repository)

The guardian texture (textures/entity/guardian/guardian.png) is 64x64.
GuardianModel has these parts. The head is the root bone, and all the other
parts are its children:
- the head: a core of 12x12x16 at uv (0, 0), two side plates of 2x12x12 at
  uv (0, 28), and a top and a bottom plate of 12x2x12 at uv (16, 40). The
  plates cover most of the core, so most of the core texture is clear. The
  front of the core (16, 16) is the face. The white of the eye is on it, in
  columns 3 to 8 of rows 5 to 7.
- the eye: 2x2x1 at uv (8, 0), the dark red pupil, just in front of the face.
- twelve spikes, spike0 to spike11, 2x9x2 at uv (0, 0). All the spikes use the
  same pixels.
- the tail: tail0, 4x4x8 at uv (40, 0), holds tail1, 3x3x7 at uv (0, 54),
  which holds tail2, 2x2x6 at uv (41, 32), with a fin of 1x9x9 at uv (25, 19).

The design uses the regions body, eye, tail, and spikes. The map uses these
region ids of the humanoid map:
- torso (3): the body, the core and the plates.
- face (1): the eye, the pupil and the white of the eye.
- legs (6): the tail. lower legs (7): the fin of the tail.
- arms (4): the spikes.
The material is skin (1), and eyes (3) on the eye. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
Where two parts use the same pixel, the part later in PARTS wins.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import ARMS, FACE, LEGS, LOWER_LEGS, TORSO, box

OUT = "src/main/resources/assets/originofspawns/textures/region/guardian.png"

SKIN, EYES = 1, 3

PARTS = [
    ((0, 0), (12, 12, 16), TORSO),
    ((0, 28), (2, 12, 12), TORSO),
    ((16, 40), (12, 2, 12), TORSO),
    ((40, 0), (4, 4, 8), LEGS),
    ((0, 54), (3, 3, 7), LEGS),
    ((41, 32), (2, 2, 6), LEGS),
    ((25, 19), (1, 9, 9), LOWER_LEGS),
    ((0, 0), (2, 9, 2), ARMS),
    ((8, 0), (2, 2, 1), FACE),
]

# The white of the eye, on the face.
EYE_WHITE = {(16 + c, 16 + r) for c in range(3, 9) for r in range(5, 8)}


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for (u, v), (w, h, d), region in PARTS:
        for x, y, face, row in box(u, v, w, h, d):
            im.putpixel((x, y), (region, EYES if region == FACE else SKIN, 0, 255))
    for x, y in EYE_WHITE:
        im.putpixel((x, y), (FACE, EYES, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
