"""Make the skeleton region map.

Usage: python3 tools/make_skeleton_region_map.py [--check <dir with vanilla skeleton textures>]

The map uses the 64x32 UV layout of the vanilla skeleton texture
(textures/entity/skeleton/skeleton.png). The skeleton has the humanoid head
and body, but thin arms and legs of 2x12x2. Red is the region id. Green is
the material id. Blue is 0. A pixel with alpha 0 is not used.

The map uses the region ids of the humanoid map, so that a gene visual can
name the same regions, and it adds the regions of the bones:
- ribs (9): the rib cage, rows 1 to 6 of the body, but the spine.
- spine (10): the middle 2 columns of the back of the body, and the gap in
  the front of the body under the ribs.
- pelvis (11): the lowest 2 rows of the body, and the bottom of the body.
- joints (12): the shoulders and the hips (row 0 and the top of each
  limb), the elbows, and the knees (rows 5 and 6 of each limb).
- eye_sockets (13): the 4 eye pixels on the face.
The other regions: face (1) is the front of the head, scalp (2) is the rest
of the head, torso (3) is the shoulder row and the top of the body, arms (4),
hands (5, rows 10 and 11 and the bottom of the arm), legs (6), lower legs
(7, rows 7 to 9 of the leg), and feet (8, rows 10 and 11 and the bottom of
the leg).
The material is bone (4), and eyes (3) in the eye sockets.

The rules below come from the vanilla skeleton texture. The script does not
read that texture to make the map. With --check, it prints the drawn pixels
of the vanilla skeleton textures that are not in the map.
This script needs Pillow.
"""
import os
import sys

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/skeleton.png"

FACE, SCALP, TORSO, ARMS, HANDS, LEGS, LOWER_LEGS, FEET = range(1, 9)
RIBS, SPINE, PELVIS, JOINTS, EYE_SOCKETS = range(9, 14)
EYES, BONE = 3, 4

# The eye sockets on the front of the head.
EYE_PIXELS = {(9, 12), (10, 12), (13, 12), (14, 12)}


def head(x, y, face, row):
    if (x, y) in EYE_PIXELS:
        return EYE_SOCKETS
    return FACE if face == "front" else SCALP


def body(x, y, face, row):
    if face == "top":
        return TORSO
    if face == "bottom":
        return PELVIS
    if row >= 10:
        return PELVIS
    # The back face is from x 32 to 39. Its middle 2 columns are the spine.
    if face == "back" and x in (35, 36):
        return SPINE
    if row == 0:
        return TORSO
    if row <= 6:
        return RIBS
    # Rows 7 to 9: the spine shows under the ribs.
    return SPINE


def limb(upper, middle, lower, end):
    def region(x, y, face, row):
        if face == "top":
            return JOINTS
        if face == "bottom":
            return end
        if row == 0 or row in (5, 6):
            return JOINTS
        if row >= 10:
            return end
        return lower if row >= 7 else upper
    return region


PARTS = {
    "head": ((0, 0), (8, 8, 8), head),
    "body": ((16, 16), (8, 12, 4), body),
    "arm": ((40, 16), (2, 12, 2), limb(ARMS, JOINTS, ARMS, HANDS)),
    "leg": ((0, 16), (2, 12, 2), limb(LEGS, JOINTS, LOWER_LEGS, FEET)),
}

# The hat layer covers the head, with the same labels.
HAT = (32, 0)


def make():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for name, ((u, v), (w, h, d), region) in PARTS.items():
        for x, y, face, row in box(u, v, w, h, d):
            r = region(x, y, face, row)
            im.putpixel((x, y), (r, EYES if r == EYE_SOCKETS else BONE, 0, 255))
    (u, v), (w, h, d), region = PARTS["head"]
    for x, y, face, row in box(HAT[0], HAT[1], w, h, d):
        r = region(x - HAT[0] + u, y - HAT[1] + v, face, row)
        im.putpixel((x, y), (SCALP if r == EYE_SOCKETS else r, BONE, 0, 255))
    return im


def check(im, vanilla_dir):
    used = {(x, y) for x in range(im.width) for y in range(im.height) if im.getpixel((x, y))[3]}
    for name in sorted(os.listdir(vanilla_dir)):
        if not name.endswith(".png"):
            continue
        tex = Image.open(os.path.join(vanilla_dir, name)).convert("RGBA")
        if tex.size != im.size:
            print(f"{name}: size {tex.size}, not {im.size}")
            continue
        drawn = {(x, y) for x in range(tex.width) for y in range(tex.height) if tex.getpixel((x, y))[3]}
        print(f"{name}: {len(drawn)} drawn pixels, {len(drawn - used)} not in the map")


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
    if len(sys.argv) == 3 and sys.argv[1] == "--check":
        check(im, sys.argv[2])
