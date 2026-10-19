"""Make the Ironhide plate patterns.

Usage: python3 tools/make_iron_plates.py

Each pattern is 16x16 and tiles in both directions. It is gray with alpha.
The file is RGBA with the same value in red, green, and blue.
The game tints a pattern and stamps it at the same position in the mob
texture, so pattern pixel (x % 16, y % 16) goes on texture pixel (x, y).
Each plate pixel has alpha 255, so that the shirt color does not show
through, and the plates have the same iron gray on each shirt.

iron_plates.png is for the shirt. There are two rows of 8x8 plates. The
second row moves 4 pixels to the right. Each plate has a light top edge, a
dark seam, and two rivets.

iron_plates_sleeve.png is for the sleeves. Each face of an arm is 4 pixels
wide, and each face starts at an x that is a multiple of 4. Thus the sleeve
pattern repeats each 4 pixels across. Rows 0 to 3 go on the top of the arm.
Rows 4 to 7 go on the sleeve, which is the first 4 rows of each arm side.
The pattern repeats each 8 rows, so that it also fits the left arm of the
drowned at y 48. The sleeve plates use the same grays as the shirt plates.
This script needs Pillow.
"""
import os

from PIL import Image

OUT_DIR = "src/main/resources/assets/originofspawns/textures/pattern"

SEAM = (72, 255)
PLATE = (150, 255)
EDGE_LIGHT = (188, 255)
EDGE_SIDE = (172, 255)
EDGE_SHADE = (122, 255)
WEAR = (136, 255)
RIVET = (226, 255)
RIVET_SHADOW = (92, 255)

# Pixels in plate space that show wear. Each plate uses the same set.
WEAR_PIXELS = {(3, 3), (4, 4), (2, 5)}


def plate_pixel(px, py):
    """Return (gray, alpha) for a pixel of an 8x8 plate."""
    if px == 7 or py == 7:
        return SEAM
    if (px, py) in ((1, 1), (5, 1)):
        return RIVET
    if (px, py) in ((1, 2), (5, 2)):
        return RIVET_SHADOW
    if py == 0:
        return EDGE_LIGHT
    if py == 6:
        return EDGE_SHADE
    if px == 0:
        return EDGE_SIDE
    if (px, py) in WEAR_PIXELS:
        return WEAR
    return PLATE


def sleeve_pixel(px, py):
    """Return (gray, alpha) for a pixel of a 4x8 sleeve cell."""
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
    # The plate on the sleeve. The seam at the bottom is the hem.
    if py == 7:
        return SEAM
    if py == 4:
        return EDGE_LIGHT
    if (px, py) == (1, 5):
        return RIVET
    if (px, py) == (1, 6):
        return RIVET_SHADOW
    if py == 6:
        return EDGE_SHADE
    return PLATE


def make_shirt():
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        shift = 0 if y < 8 else 4
        for x in range(16):
            gray, alpha = plate_pixel((x - shift) % 8, y % 8)
            im.putpixel((x, y), (gray, gray, gray, alpha))
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
