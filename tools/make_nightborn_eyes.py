"""Make the Nightborn eyes: a gene model and its texture.

Usage: python3 tools/make_nightborn_eyes.py

Two flat squares of 1x1 pixel sit just in front of the outer pixel of each
eye. They glow, so the eyes are bright in the dark. The inner eye pixels
stay free for the marks of other genes.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")
TEXTURE = "textures/entity/nightborn_eyes.png"
MODEL = "gene_models/nightborn_eyes.json"

EYE = (255, 242, 176, 255)

# The outer eye pixels are x 9 and 14, y 12 of the head texture. On the head
# bone, the face is at z -4, and texture x 8 to 16 is x -4 to 4.
EYES_X = (-3.0, 2.0)
EYE_Y = -4.0
FACE_Z = -4.05


def texture():
    # One 1x1x0 box at uv (0, 0): its front is pixel (0, 0), its back (1, 0).
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    im.putpixel((0, 0), EYE)
    im.putpixel((1, 0), EYE)
    return im


def model():
    return {
        "texture": "originofspawns:" + TEXTURE,
        "texture_width": 16,
        "texture_height": 16,
        "parts": [
            {"bone": "head", "uv": [0, 0], "from": [x, EYE_Y, FACE_Z], "size": [1, 1, 0], "glow": True}
            for x in EYES_X
        ],
    }


if __name__ == "__main__":
    texture().save(os.path.join(ASSETS, TEXTURE))
    with open(os.path.join(ASSETS, MODEL), "w") as fh:
        json.dump(model(), fh, indent="\t")
        fh.write("\n")
    print(os.path.join(ASSETS, TEXTURE))
    print(os.path.join(ASSETS, MODEL))
