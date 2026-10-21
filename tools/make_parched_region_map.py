"""Make the parched region map.

Usage: python3 tools/make_parched_region_map.py (from the root of the repository)

The parched (the desert skeleton) has a 64x64 texture
(textures/entity/skeleton/parched.png), with the bones of the skeleton in the
top half and wraps of cloth over them in the bottom half. Its model is
SkeletonModel.createSingleModelDualBodyLayer:
- The head is at uv (0, 0), and its wrap (8x8x8) at uv (0, 32).
- The body is at uv (16, 16), a box for the pelvis (8x1x4) at uv (28, 0),
  and the wrap of the body (8x12x4) at uv (16, 48).
- The right arm is at uv (40, 16), and its sleeve (3x12x3) at uv (42, 33).
- The left arm has its own bones at uv (56, 16), and its sleeve at uv
  (40, 48).
- The legs are at uv (0, 16), and their wraps (3x12x3) at uv (0, 49) and
  (4, 49).

The map uses the region ids and the rules of the skeleton map (see
make_skeleton_region_map.py). The bones have the material bone, and the
wraps have the material cloth. Red is the region id. Green is the material
id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box
from make_skeleton_region_map import EYE_SOCKETS, EYES, BONE, PELVIS, head, body, limb, PARTS

OUT = "src/main/resources/assets/originofspawns/textures/region/parched.png"
CLOTH = 2

BONES = [
    ((0, 0), (8, 8, 8), head),
    ((16, 16), (8, 12, 4), body),
    ((40, 16), (2, 12, 2), PARTS["arm"][2]),
    ((56, 16), (2, 12, 2), PARTS["arm"][2]),
    ((0, 16), (2, 12, 2), PARTS["leg"][2]),
]

# The wraps of cloth. Each wrap has the regions of the part under it: the
# rules of that part get the pixel moved to the uv of the bones (the last
# pair).
WRAPS = [
    ((0, 32), (8, 8, 8), head, (0, 0)),
    ((16, 48), (8, 12, 4), body, (16, 16)),
    ((42, 33), (3, 12, 3), PARTS["arm"][2], (40, 16)),
    ((40, 48), (3, 12, 3), PARTS["arm"][2], (40, 16)),
    ((0, 49), (3, 12, 3), PARTS["leg"][2], (0, 16)),
    ((4, 49), (3, 12, 3), PARTS["leg"][2], (0, 16)),
]


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for (u, v), (w, h, d), region, (bu, bv) in WRAPS:
        for x, y, face, row in box(u, v, w, h, d):
            r = region(x - u + bu, y - v + bv, face, row)
            # The wrap of the head has no eye sockets. It is face there.
            im.putpixel((x, y), (1 if r == EYE_SOCKETS else r, CLOTH, 0, 255))
    for (u, v), (w, h, d), region in BONES:
        for x, y, face, row in box(u, v, w, h, d):
            r = region(x, y, face, row)
            im.putpixel((x, y), (r, EYES if r == EYE_SOCKETS else BONE, 0, 255))
    # The box of the pelvis.
    for x, y, face, row in box(28, 0, 8, 1, 4):
        im.putpixel((x, y), (PELVIS, BONE, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
