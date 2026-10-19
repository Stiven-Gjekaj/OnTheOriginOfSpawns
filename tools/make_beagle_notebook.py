"""Make the Beagle Notebook item texture.

Usage: python3 tools/make_beagle_notebook.py

The texture is 16x16 in the vanilla item style.
The notebook lies at a slant, in the same pose as the vanilla book items.
The script draws the cover as a parallelogram. One edge goes 2 pixels right
for each pixel up. The other edge goes 1 pixel right for each pixel down.
Under the cover are 2 rows of pages and then the back cover.

The notebook is different from the vanilla books: it has worn brown leather,
a dark elastic band that goes around the cover and the pages, a paper label,
and yellow page edges. It has no ribbon, because the enchanted book has one.
This script needs Pillow.
"""
import os

from PIL import Image

OUT = "src/main/resources/assets/originofspawns/textures/item/beagle_notebook.png"

OUTLINE = (44, 27, 17, 255)
COVER = (136, 78, 44, 255)
COVER_LIGHT = (160, 98, 58, 255)
COVER_DARK = (108, 60, 34, 255)
WORN = (178, 122, 78, 255)
SPINE = (82, 50, 29, 255)
SPINE_RIB = (112, 73, 43, 255)
BACK = (76, 46, 27, 255)
PAGE = (224, 206, 150, 255)
PAGE_SHADE = (196, 174, 116, 255)
BAND = (40, 30, 26, 255)
BAND_LIGHT = (72, 56, 48, 255)
LABEL = (222, 208, 170, 255)
LABEL_TEXT = (112, 86, 62, 255)

# The left corner of the cover, and the length of each edge in steps.
LEFT = (1.0, 5.5)
U_LEN = 4.5
V_LEN = 5.2
PAGE_ROWS = 2

# The band goes across the cover between these values of u.
BAND_U = (1.05, 1.6)


def cover_coords(x, y, drop):
    """Return (u, v) of a pixel on a cover that is moved down by drop.

    u goes along the upper left edge, toward the top corner.
    v goes along the upper right edge, toward the right corner.
    """
    dx = x + 0.5 - LEFT[0]
    dy = y + 0.5 - LEFT[1] - drop
    return (dx - dy) / 3, (dx + 2 * dy) / 3


def inside(u, v):
    return 0 <= u <= U_LEN and 0 <= v <= V_LEN


def in_band(u):
    return BAND_U[0] <= u <= BAND_U[1]


def cover_pixel(u, v):
    if in_band(u):
        return BAND_LIGHT if v < 0.9 else BAND
    if 2.2 <= u <= 3.9 and 1.4 <= v <= 4.1:
        # One line of writing on the label.
        if 2.85 <= u <= 3.25 and 1.9 <= v <= 3.7:
            return LABEL_TEXT
        return LABEL
    # The spine is the upper left edge. It has ribs.
    if v < 0.8:
        return SPINE_RIB if int(u * 2) % 3 == 0 else SPINE
    corner = (u > U_LEN - 0.8 and v < 1.4) or (u > U_LEN - 0.8 and v > V_LEN - 0.9)
    if corner:
        return WORN
    if u > U_LEN - 0.7:
        return COVER_LIGHT
    if v > V_LEN - 0.8 or u < 0.6:
        return COVER_DARK
    return COVER


def make():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    filled = set()
    for y in range(16):
        for x in range(16):
            u, v = cover_coords(x, y, 0)
            if inside(u, v):
                im.putpixel((x, y), cover_pixel(u, v))
                filled.add((x, y))
                continue
            for drop in range(1, PAGE_ROWS + 1):
                u, v = cover_coords(x, y, drop)
                if inside(u, v):
                    color = PAGE if y % 2 else PAGE_SHADE
                    im.putpixel((x, y), BAND if in_band(u) else color)
                    filled.add((x, y))
                    break
            else:
                u, v = cover_coords(x, y, PAGE_ROWS + 1)
                if inside(u, v):
                    im.putpixel((x, y), BAND if in_band(u) else BACK)
                    filled.add((x, y))
    # A dark outline goes around the shape.
    for y in range(16):
        for x in range(16):
            if (x, y) in filled:
                continue
            near = ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))
            if any(n in filled for n in near):
                im.putpixel((x, y), OUTLINE)
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
