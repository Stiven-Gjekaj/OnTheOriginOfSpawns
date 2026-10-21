"""Make the wiki pictures of the mobs from the raw game pictures.

Run it after the client test WikiShotsClientTest, with the environment
variable ORIGINOFSPAWNS_WIKI_SHOTS set:

    uv run --with pillow --with numpy python3 tools/matte_wiki_shots.py

The test takes each view two times: in a black room and in a white room. It
also takes the two empty rooms. A pixel of the mob has the same color in both
rooms. A pixel of the room changes from black to white. The difference gives
the transparency of each pixel:

    alpha = 1 - (white - black) / (white room - black room)
    color = (black - (1 - alpha) * black room) / alpha

The tool cuts each species to one frame, so that all its pictures have the
same size and the mob stands at the same place. It writes the pictures to
website/public/shots/<species>/<kind>/<name>-<view>.png. The kind is plain,
gene, or family.
"""

import glob
import os
import sys

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RAW = os.path.join(ROOT, "build", "wiki_shots")
OUT = os.path.join(ROOT, "website", "public", "shots")
HEIGHT = 480
MARGIN = 8


def load(name):
    return np.asarray(Image.open(os.path.join(RAW, name + ".png")).convert("RGB"), dtype=np.float32)


def matte(black, white, black_room, white_room):
    """Return an RGBA image of the mob, with the rooms taken away."""
    room = np.maximum(white_room - black_room, 1.0)
    alpha = 1.0 - ((white - black) / room).mean(axis=2)
    alpha = np.clip(alpha, 0.0, 1.0)
    # Small values come from the noise of the concrete texture, not from the mob.
    alpha[alpha < 0.08] = 0.0
    safe = np.maximum(alpha, 1e-3)[..., None]
    color = (black - (1.0 - alpha)[..., None] * black_room) / safe
    color = np.clip(color, 0.0, 255.0)
    rgba = np.dstack([color, alpha * 255.0]).astype(np.uint8)
    return rgba


def main():
    if not os.path.isdir(RAW):
        sys.exit("No raw pictures in " + RAW + ". Run WikiShotsClientTest first.")
    black_room = load("background__black")
    white_room = load("background__white")
    shots = {}
    for path in sorted(glob.glob(os.path.join(RAW, "*__black.png"))):
        name = os.path.basename(path)[: -len("__black.png")]
        if name == "background":
            continue
        species, kind, gene, view = name.split("__")
        rgba = matte(load(name + "__black"), load(name + "__white"), black_room, white_room)
        shots.setdefault(species, []).append((kind, gene, view, rgba))
    for species, items in shots.items():
        # One frame for the species: the box around the mob in all its pictures.
        boxes = [Image.fromarray(rgba).getbbox() for _, _, _, rgba in items]
        boxes = [box for box in boxes if box]
        left = max(0, min(box[0] for box in boxes) - MARGIN)
        top = max(0, min(box[1] for box in boxes) - MARGIN)
        right = max(box[2] for box in boxes) + MARGIN
        bottom = max(box[3] for box in boxes) + MARGIN
        for kind, gene, view, rgba in items:
            image = Image.fromarray(rgba).crop((left, top, right, bottom))
            width = round(image.width * HEIGHT / image.height)
            image = image.resize((width, HEIGHT), Image.LANCZOS)
            folder = os.path.join(OUT, species, kind)
            os.makedirs(folder, exist_ok=True)
            image.save(os.path.join(folder, gene + "-" + view + ".png"), optimize=True)
        print(species, len(items), "pictures,", right - left, "x", bottom - top, "before the resize")


if __name__ == "__main__":
    main()
