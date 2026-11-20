"""Make the zombie nautilus region map.

Usage: python3 tools/make_zombie_nautilus_region_map.py (from the root of the repository)

The zombie nautilus has two textures, temperate (zombie_nautilus.png) and warm
(zombie_nautilus_coral.png). Both are 128x128. The temperate variant uses
NautilusModel, and the warm variant uses ZombieNautilusCoralModel, which is
NautilusModel with flat corals on the shell. The two textures have the same
layout for the parts of NautilusModel, so this one map serves both. The
corals of the warm variant (rows 85 to 121) are not a region.

NautilusModel has these parts, under the bone root:
- the shell: a high cube 14x10x16 at uv (0, 0), a low cube 14x8x20 at uv
  (0, 26), and a flat wall 14x8x0 at uv (48, 26). The top of the low cube and
  its +z end are clear. The bottom of the low cube has a round hole.
- the body: 10x8x14 at uv (0, 54), and a flat face 10x8x0 at uv (0, 76). The
  front and the back of the body are clear. The eyes are on the sides, at
  (8, 70) and (29, 70).
- the mouth, three children of the body: upper_mouth 10x4x4 at uv (54, 54),
  lower_mouth 10x4x4 at uv (54, 62), and inner_mouth 6x4x4 at uv (54, 70).
  Their texture is a ring of short tentacles. Most of their pixels are clear.
The mouth is at the +z end of the model, and the coil of the shell is at the
-z end.

The design uses the regions shell, body, and tentacles. The map uses these
region ids of the humanoid map:
- torso (3): the shell.
- face (1): the body and its flat face.
- arms (4): the tentacles, the three parts of the mouth.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import ARMS, FACE, TORSO, box

OUT = "src/main/resources/assets/originofspawns/textures/region/zombie_nautilus.png"

SKIN, EYES = 1, 3

EYE_PIXELS = {(8, 70), (29, 70)}

# Each part: box UV origin, size, region.
PARTS = {
    "shell_high": ((0, 0), (14, 10, 16), TORSO),
    "shell_low": ((0, 26), (14, 8, 20), TORSO),
    "shell_wall": ((48, 26), (14, 8, 0), TORSO),
    "body": ((0, 54), (10, 8, 14), FACE),
    "face": ((0, 76), (10, 8, 0), FACE),
    "upper_mouth": ((54, 54), (10, 4, 4), ARMS),
    "lower_mouth": ((54, 62), (10, 4, 4), ARMS),
    "inner_mouth": ((54, 70), (6, 4, 4), ARMS),
}


def make():
    im = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    for name, ((u, v), (w, h, d), region) in PARTS.items():
        for x, y, face, row in box(u, v, w, h, d):
            m = EYES if (x, y) in EYE_PIXELS else SKIN
            im.putpixel((x, y), (region, m, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
