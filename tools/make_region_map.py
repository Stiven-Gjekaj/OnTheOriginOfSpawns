"""Make the humanoid region map.

Usage: python3 tools/make_region_map.py [--check <dir with vanilla zombie textures>]

The map uses the 64x64 UV layout of the vanilla zombie texture.
Red is the region id. Green is the material id. Blue is 0.
A pixel with alpha 0 is not used.

The rules below come from the vanilla zombie texture. The script does not
read that texture to make the map. With --check, it compares the rules with
the vanilla zombie, husk, and drowned textures and prints each difference.
This script needs Pillow.
"""
import colorsys
import os
import sys

from PIL import Image

OUT = "src/main/resources/assets/originofspawns/textures/region/humanoid.png"

FACE, SCALP, TORSO, ARMS, HANDS, LEGS, LOWER_LEGS, FEET = range(1, 9)
SKIN, CLOTH, EYES = 1, 2, 3

# The eye pixels on the front of the head.
EYE_PIXELS = {(9, 12), (10, 12), (13, 12), (14, 12)}


def box(u, v, w, h, d):
    """Yield (x, y, face, row) for each pixel of a box in box UV layout.

    The row counts from the top of the side faces. It is None on the top and
    the bottom faces.
    """
    faces = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }
    for face, (x0, y0, fw, fh) in faces.items():
        for y in range(y0, y0 + fh):
            for x in range(x0, x0 + fw):
                row = None if face in ("top", "bottom") else y - y0
                yield x, y, face, row


def head(face, row):
    if face == "front":
        return FACE
    # The bottom of the head is under the jaw. The map calls it scalp.
    return SCALP


def arm(face, row):
    if face == "bottom":
        return HANDS
    if face == "top":
        return ARMS
    return HANDS if row >= 8 else ARMS


def leg(face, row):
    if face == "bottom":
        return FEET
    if face == "top":
        return LEGS
    if row >= 10:
        return FEET
    return LOWER_LEGS if row >= 6 else LEGS


def head_material(x, y, face, row):
    return EYES if (x, y) in EYE_PIXELS else SKIN


def torso_material(x, y, face, row):
    # The neck shows on the top of the body and in a V at the collar.
    if face == "top" and 21 <= x <= 26:
        return SKIN
    if face == "front" and row == 0 and 22 <= x <= 25:
        return SKIN
    if face == "front" and row == 1 and 23 <= x <= 24:
        return SKIN
    return CLOTH


def arm_material(x, y, face, row):
    # The sleeve covers the shoulder and the first 4 rows.
    if face == "top":
        return CLOTH
    if face == "bottom":
        return SKIN
    return CLOTH if row < 4 else SKIN


def leg_material(x, y, face, row):
    return CLOTH


# Each part: box UV origin, size, region rule, material rule.
# The inner layer comes first. The vanilla zombie mirrors the right limbs for
# the left limbs. The drowned has its own left limbs at (32, 48) and (16, 48).
PARTS = {
    "head": ((0, 0), (8, 8, 8), head, head_material),
    "body": ((16, 16), (8, 12, 4), lambda f, r: TORSO, torso_material),
    "right_arm": ((40, 16), (4, 12, 4), arm, arm_material),
    "left_arm": ((32, 48), (4, 12, 4), arm, arm_material),
    "right_leg": ((0, 16), (4, 12, 4), leg, leg_material),
    "left_leg": ((16, 48), (4, 12, 4), leg, leg_material),
}

# Outer layer: the inner part that it covers, and its own UV origin.
OUTER = {
    "hat": ("head", (32, 0)),
    "jacket": ("body", (16, 32)),
    "right_sleeve": ("right_arm", (40, 32)),
    "left_sleeve": ("left_arm", (48, 48)),
    "right_pants": ("right_leg", (0, 32)),
    "left_pants": ("left_leg", (0, 48)),
}


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    labels = {}
    for name, ((u, v), (w, h, d), region, material) in PARTS.items():
        for x, y, face, row in box(u, v, w, h, d):
            labels[(x, y)] = (region(face, row), material(x, y, face, row))
    for name, (inner, (u, v)) in OUTER.items():
        (iu, iv), (w, h, d), region, material = PARTS[inner]
        for x, y, face, row in box(u, v, w, h, d):
            # The outer layer takes the label of the inner pixel under it.
            # An outer layer has no eyes, so it is skin there.
            r, m = labels[(x - u + iu, y - v + iv)]
            labels[(x, y)] = (r, SKIN if m == EYES else m)
    for (x, y), (r, m) in labels.items():
        im.putpixel((x, y), (r, m, 0, 255))
    return im


def guess_material(p):
    """Guess the material of a vanilla zombie pixel from its color."""
    r, g, b, a = p
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    return SKIN if 0.2 < h < 0.36 and s > 0.2 else CLOTH


def check(im, vanilla_dir):
    names = {SKIN: "skin", CLOTH: "cloth", EYES: "eyes"}
    zombie = Image.open(os.path.join(vanilla_dir, "zombie.png")).convert("RGBA")
    used = {(x, y) for x in range(64) for y in range(64) if im.getpixel((x, y))[3]}
    for n in ("zombie", "husk", "drowned", "drowned_outer_layer"):
        tex = Image.open(os.path.join(vanilla_dir, n + ".png")).convert("RGBA")
        drawn = {(x, y) for x in range(64) for y in range(64) if tex.getpixel((x, y))[3]}
        print(f"{n}: {len(drawn)} drawn pixels, {len(drawn - used)} not in the map")
    diffs = []
    for x, y in sorted(used):
        p = zombie.getpixel((x, y))
        if not p[3]:
            continue
        want = EYES if (x, y) in EYE_PIXELS else guess_material(p)
        got = im.getpixel((x, y))[1]
        if want != got:
            diffs.append((x, y, names[got], names[want]))
    print(f"zombie material differences: {len(diffs)}")
    for d in diffs:
        print("  pixel (%d, %d): map %s, color says %s" % d)


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
    if len(sys.argv) == 3 and sys.argv[1] == "--check":
        check(im, sys.argv[2])
