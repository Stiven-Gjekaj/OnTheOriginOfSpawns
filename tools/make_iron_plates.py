"""Make the Ironhide plate pattern.

Usage: python3 tools/make_iron_plates.py

The pattern is 16x16 and tiles in both directions. It is gray with alpha.
The file is RGBA with the same value in red, green, and blue.
The game tints it and stamps it on the torso cloth.
There are two rows of plates. The second row moves 4 pixels to the right.
Each plate has a light top edge, a dark seam, and two rivets.
This script needs Pillow.
"""
import os

from PIL import Image

OUT = "src/main/resources/assets/originofspawns/textures/pattern/iron_plates.png"

SEAM = (72, 235)
PLATE = (150, 205)
EDGE_LIGHT = (188, 215)
EDGE_SIDE = (172, 210)
EDGE_SHADE = (122, 210)
WEAR = (136, 205)
RIVET = (226, 255)
RIVET_SHADOW = (92, 240)

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


def make():
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        shift = 0 if y < 8 else 4
        for x in range(16):
            gray, alpha = plate_pixel((x - shift) % 8, y % 8)
            im.putpixel((x, y), (gray, gray, gray, alpha))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
