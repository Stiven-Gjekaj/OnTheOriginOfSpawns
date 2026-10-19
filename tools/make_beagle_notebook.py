"""Make the Beagle Notebook item texture.

Usage: python3 tools/make_beagle_notebook.py

The texture is 16x16 in the vanilla item style.
The notebook stands up and faces the player, so it does not look like the
vanilla book, which lies flat. It is tall, so it does not look like a chest.
It has a worn brown leather cover, spine ribs, an elastic band, a paper
label, yellow page edges on the right side, and a red ribbon bookmark.
This script needs Pillow.
"""
import os

from PIL import Image

OUT = "src/main/resources/assets/originofspawns/textures/item/beagle_notebook.png"

COLORS = {
    ".": (0, 0, 0, 0),
    "O": (44, 27, 17, 255),     # outline
    "S": (78, 48, 28, 255),     # spine
    "s": (110, 72, 42, 255),    # spine rib
    "C": (120, 78, 45, 255),    # cover
    "L": (142, 96, 58, 255),    # cover, light
    "D": (96, 60, 34, 255),     # cover, dark
    "W": (160, 116, 72, 255),   # worn corner
    "e": (110, 84, 60, 255),    # writing on the label
    "E": (222, 208, 170, 255),  # paper label
    "P": (222, 204, 150, 255),  # page edge
    "p": (194, 172, 116, 255),  # page edge, shade
    "T": (40, 30, 26, 255),     # band
    "t": (74, 58, 50, 255),     # band, light
    "R": (168, 40, 36, 255),    # ribbon
    "r": (120, 26, 24, 255),    # ribbon, dark
}

ROWS = [
    "..OOOOOOOOOO....",
    "..OsWLLLLLTDOO..",
    "..OSLCCCCCTDPPO.",
    "..OsLCCCCCTDppO.",
    "..OSCCCCCCTDPPO.",
    "..OSCEEEECtDppO.",
    "..OSCEeeECTDPPO.",
    "..OSCEEEECTDppO.",
    "..OSCEeEECTDPPO.",
    "..OSCEEEECtDppO.",
    "..OSCCCCDCTDPPO.",
    "..OsCDCCCCTDppO.",
    "..OSDCCCCCTDPPO.",
    "..OsWDDDDDTWOO..",
    "..OOOOOOOOOORO..",
    "............Rr..",
]


def make():
    assert len(ROWS) == 16 and all(len(r) == 16 for r in ROWS)
    im = Image.new("RGBA", (16, 16))
    for y, row in enumerate(ROWS):
        for x, key in enumerate(row):
            im.putpixel((x, y), COLORS[key])
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
