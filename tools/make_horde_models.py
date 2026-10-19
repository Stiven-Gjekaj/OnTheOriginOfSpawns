"""Make the gene models of the Horde genes: Packcaller, Runt, and Siege.

Usage: python3 tools/make_horde_models.py

Packcaller: a lower jaw that hangs open under the front of the face. The
open mouth shows over the chin, with two fangs.

Runt: thin tufts of hair at the corners of the scalp. They stand higher
than the Lurker hood, and away from the middle, where the Thickskull crown
is.

Siege: splinters of wood that stick out of the hands: one from the outer
side and one from the front of each hand.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")

SKIN = ((95, 128, 70, 255), (84, 115, 61, 255))
SKIN_DARK = (66, 92, 48, 255)
GUM = (70, 26, 24, 255)
MOUTH = (26, 15, 12, 255)
TOOTH = (226, 218, 190, 255)

HAIR = ((58, 48, 38, 255), (78, 66, 52, 255))

WOOD = ((176, 136, 80, 255), (150, 112, 62, 255))
WOOD_TIP = (94, 68, 38, 255)


def save(name, texture, model):
    path = os.path.join(ASSETS, "textures/entity", name + ".png")
    texture.save(path)
    with open(os.path.join(ASSETS, "gene_models", name + ".json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")
    print(path)


def box_faces(u, v, w, h, d):
    """Return the box UV areas: top, bottom, right, front, left, back."""
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }


def fill(im, area, color):
    x0, y0, w, h = area
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            c = color(x - x0, y - y0) if callable(color) else color
            if c:
                im.putpixel((x, y), c)


def packcaller():
    # The jaw: 4x2x2 at uv (0, 0), under the front of the head (the head
    # bottom is at y 0, and the face is at z -4). Its front is 0.4 pixel in
    # front of the face. The upper row of its front is the open mouth, with
    # two fangs. The lower row is the chin.
    w, h, d = 4, 2, 2
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    f = box_faces(0, 0, w, h, d)
    fill(im, f["front"], lambda x, y: (TOOTH if x in (0, 3) else MOUTH) if y == 0 else SKIN[x % 2])
    fill(im, f["right"], SKIN_DARK)
    fill(im, f["left"], SKIN_DARK)
    fill(im, f["bottom"], SKIN_DARK)
    fill(im, f["back"], MOUTH)
    # The top: fangs at the corners at the front, and gums behind them.
    fill(im, f["top"], lambda x, y: TOOTH if y == 1 and x in (0, 3) else GUM if y == 1 else MOUTH)
    model = {
        "texture": "originofspawns:textures/entity/packcaller_jaw.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": [{"bone": "head", "uv": [0, 0], "from": [-2, 0, -4.4], "size": [w, h, d]}],
    }
    save("packcaller_jaw", im, model)


def runt():
    # Each tuft: a flat plane 1 pixel wide (1xHx0) at uv (0, 0), on the head
    # bone, on the scalp. Many short tufts, each with its own lean, read as
    # thin hair. They stand away from the middle of the scalp, where the
    # Thickskull crown is, and start above the Lurker hood (y -8.4).
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    f = box_faces(0, 0, 1, 2, 0)
    fill(im, f["front"], lambda x, y: HAIR[y])
    fill(im, f["back"], lambda x, y: HAIR[y])
    # x, z, height, lean
    tufts = [(-3.6, -3.0, 2, 30), (-3.6, 0.5, 1, -25), (-3.4, 3.0, 2, 20),
             (2.6, -2.5, 1, -30), (2.6, 1.0, 2, 25), (2.4, 3.2, 1, -20),
             (-1.0, 3.4, 2, 15), (0.5, -3.5, 1, -15)]
    parts = []
    for x, z, height, lean in tufts:
        y = -8.5 - height
        parts.append({"bone": "head", "uv": [0, 0], "from": [x, y, z], "size": [1, height, 0],
                      "pivot": [x + 0.5, -8.5, z], "rotation": [0, 0, lean]})
    model = {
        "texture": "originofspawns:textures/entity/runt_tufts.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": parts,
    }
    save("runt_tufts", im, model)


def siege():
    # A sideways splinter (2x1x1 at uv (0, 0)) and a forward splinter (1x1x2
    # at uv (0, 4)) on each hand. The arm box is 4x12x4, from y -2 to 10.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for area in box_faces(0, 0, 2, 1, 1).values():
        fill(im, area, lambda x, y: WOOD[(x + y) % 2])
    for area in box_faces(0, 4, 1, 1, 2).values():
        fill(im, area, lambda x, y: WOOD[(x + y) % 2])
    # The tips are dark: the outer end of each splinter.
    for p in ((0, 1), (0, 5), (1, 4)):
        im.putpixel(p, WOOD_TIP)
    parts = []
    for bone, side in (("right_arm", -1), ("left_arm", 1)):
        outer = -3 if side < 0 else 3
        parts.append({"bone": bone, "uv": [0, 0], "size": [2, 1, 1],
                      "from": [outer - 2 if side < 0 else outer, 7, -0.5],
                      "mirror": side > 0})
        parts.append({"bone": bone, "uv": [0, 4], "size": [1, 1, 2],
                      "from": [-1.5 if side < 0 else 0.5, 8.5, -4], "mirror": side > 0})
    model = {
        "texture": "originofspawns:textures/entity/siege_splinters.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": parts,
    }
    save("siege_splinters", im, model)


if __name__ == "__main__":
    packcaller()
    runt()
    siege()
