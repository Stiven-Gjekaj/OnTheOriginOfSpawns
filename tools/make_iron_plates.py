"""Make the Ironhide plate patterns.

Usage: python3 tools/make_iron_plates.py

Each pattern is gray with alpha. The file is RGBA with the same value in
red, green, and blue. The game tints a pattern and stamps it at the same
position in the mob texture, so pattern pixel (x % w, y % h) goes on
texture pixel (x, y).

iron_plates.png is for the shirt. It is 64x64, so each plate is on exact
pixels of the zombie texture. The plates are 3x3 pixels, each with a rivet
and a dark seam. The chest has two columns of 2 plates, at x 20 to 22 and
x 25 to 27, y 22 to 27. The back has a breastplate of 2x2 plates, at x 33
to 38, y 22 to 27. The rest of the shirt stays clear for the marks of
other genes.

iron_plates_sleeve.png is for the sleeves. Each face of an arm is 4 pixels
wide, and each face starts at an x that is a multiple of 4. Thus the sleeve
pattern repeats each 4 pixels across. Rows 0 to 3 go on the top of the arm.
Rows 4 and 5 go on the first 2 rows of each arm side. Rows 6 and 7 are clear.
The pattern repeats each 8 rows, so that it also fits the left arm of the
drowned at y 48. The sleeve plates use the same grays as the shirt plates.
This script needs Pillow.
"""
import os

from PIL import Image

OUT_DIR = "src/main/resources/assets/originofspawns/textures/pattern"

SEAM = (72, 235)
PLATE = (150, 205)
EDGE_LIGHT = (188, 215)
EDGE_SIDE = (172, 210)
RIVET = (226, 255)
RIVET_SHADOW = (92, 240)
CLEAR = (0, 0)

def sleeve_pixel(px, py):
    """Return (gray, alpha) for a pixel of a 4x8 sleeve cell."""
    if py >= 6:
        return CLEAR
    if px == 3:
        return SEAM
    if py < 4:
        # The cap on the top of the arm.
        if py == 0:
            return EDGE_LIGHT
        if (px, py) == (1, 1):
            return RIVET
        if (px, py) == (1, 2):
            return RIVET_SHADOW
        return PLATE
    # The plate on the sleeve is 2 rows: a row with a rivet, and the hem.
    # Rows 2 and 3 of the sleeve stay clear for the marks of other genes.
    if py == 4:
        return RIVET if px == 1 else EDGE_LIGHT
    if py == 5:
        return SEAM
    return CLEAR


# The plates on the shirt: x, y, width, and height. On the chest, two
# columns of plates leave the middle clear for the Frenzy tear. The back has
# one breastplate.
BREASTPLATES = ((20, 22, 3, 6), (25, 22, 3, 6), (33, 22, 6, 6))


def breastplate_pixel(px, py):
    """Return (gray, alpha) for a pixel of a breastplate of 3x3 plates."""
    cx, cy = px % 3, py % 3
    if cx == 2 or cy == 2:
        return SEAM
    if (cx, cy) == (0, 0):
        return RIVET
    if cy == 0:
        return EDGE_LIGHT
    if cx == 0:
        return EDGE_SIDE
    return PLATE


def make_shirt():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for x0, y0, w, h in BREASTPLATES:
        for py in range(h):
            for px in range(w):
                gray, alpha = breastplate_pixel(px, py)
                im.putpixel((x0 + px, y0 + py), (gray, gray, gray, alpha))
    return im


def make_sleeve():
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            gray, alpha = sleeve_pixel(x % 4, y % 8)
            im.putpixel((x, y), (gray, gray, gray, alpha))
    return im


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, im in (("iron_plates", make_shirt()), ("iron_plates_sleeve", make_sleeve())):
        path = os.path.join(OUT_DIR, name + ".png")
        im.save(path)
        print(path)
