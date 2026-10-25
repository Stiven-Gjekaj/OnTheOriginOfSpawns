"""Make the piglin region map.

Usage: python3 tools/make_piglin_region_map.py (from the root of the repository)

The zombified piglin, the piglin, and the piglin brute use this map. Their
textures are 64x64, in the player layout. The models (AbstractPiglinModel
and PlayerModel) have these parts:
- the head: 10x8x8 at uv (0, 0). The head also holds the snout (4x4x1 at uv
  (31, 1)) and two tusks (1x2x1 at uv (2, 4) and (2, 0)). They are cubes of
  the head, not bones of their own.
- the ears, the bones right_ear and left_ear: 1x5x4 at uv (39, 6) and (51, 6).
  They are children of the head.
- the body (16, 16), the right arm (40, 16), the left arm (32, 48), the right
  leg (0, 16), and the left leg (16, 48), as in the player model.
- the outer layer: the jacket (16, 32), the sleeves (40, 32) and (48, 48),
  and the pants (0, 32) and (0, 48). The hat is empty on a piglin.
The eyes are on row 3 of the face, at x 10 and 15.

The map uses the region ids of the humanoid map:
- face (1): the front of the head, the snout, and the tusks.
- scalp (2): the other faces of the head, and the ears.
- torso (3), arms (4), hands (5), legs (6), lower legs (7), feet (8), as on
  the humanoid map.
The material is skin (1), cloth (2) on the legs and on the outer layer of the
body and the legs, eyes (3) on the eyes, and bone (4) on the tusks. Red is
the region id. Green is the material id. Blue is 0. A pixel with alpha 0 is
not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import ARMS, FACE, HANDS, SCALP, TORSO, arm, box, leg

OUT = "src/main/resources/assets/originofspawns/textures/region/piglin.png"

SKIN, CLOTH, EYES, BONE = 1, 2, 3, 4

EYE_PIXELS = {(10, 11), (15, 11)}

# Each part: box UV origin, size, region rule, material.
PARTS = {
    "head": ((0, 0), (10, 8, 8), lambda f, r: FACE if f == "front" else SCALP, SKIN),
    "snout": ((31, 1), (4, 4, 1), lambda f, r: FACE, SKIN),
    "right_tusk": ((2, 4), (1, 2, 1), lambda f, r: FACE, BONE),
    "left_tusk": ((2, 0), (1, 2, 1), lambda f, r: FACE, BONE),
    "right_ear": ((39, 6), (1, 5, 4), lambda f, r: SCALP, SKIN),
    "left_ear": ((51, 6), (1, 5, 4), lambda f, r: SCALP, SKIN),
    "body": ((16, 16), (8, 12, 4), lambda f, r: TORSO, SKIN),
    "right_arm": ((40, 16), (4, 12, 4), arm, SKIN),
    "left_arm": ((32, 48), (4, 12, 4), arm, SKIN),
    "right_leg": ((0, 16), (4, 12, 4), leg, CLOTH),
    "left_leg": ((16, 48), (4, 12, 4), leg, CLOTH),
}

# Outer layer: the inner part that it covers, its own UV origin, and its
# material.
OUTER = {
    "jacket": ("body", (16, 32), CLOTH),
    "right_sleeve": ("right_arm", (40, 32), SKIN),
    "left_sleeve": ("left_arm", (48, 48), SKIN),
    "right_pants": ("right_leg", (0, 32), CLOTH),
    "left_pants": ("left_leg", (0, 48), CLOTH),
}


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    labels = {}
    for name, ((u, v), (w, h, d), region, material) in PARTS.items():
        for x, y, face, row in box(u, v, w, h, d):
            labels[(x, y)] = (region(face, row), EYES if (x, y) in EYE_PIXELS else material)
    for name, (inner, (u, v), material) in OUTER.items():
        (iu, iv), (w, h, d), region, _ = PARTS[inner]
        for x, y, face, row in box(u, v, w, h, d):
            labels[(x, y)] = (region(face, row), material)
    for (x, y), (r, m) in labels.items():
        im.putpixel((x, y), (r, m, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
