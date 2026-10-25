"""Make the magma cube region map.

Usage: python3 tools/make_magma_cube_region_map.py (from the root of the repository)

The magma cube texture (textures/entity/slime/magmacube.png) is 64x64.
MagmaCubeModel has these parts:
- eight thin layers, cube0 (the top) to cube7 (the bottom). Each layer is
  8x1x8, at y 16 + i. Layers 0 to 3 are at uv (0, 9i). Layers 4 to 7 are at
  uv (32, 9i - 36). When the cube hops, the layers move apart.
- the core, inside_cube: 4x4x4 at uv (24, 40), from y 18 to 22.
The eyes are on the front of layers 2 and 3, in columns 1, 2, 5, and 6.

The design uses the regions body (the layers), core (the inside cube), and
face. The face is the front of layers 1 to 5: the brow, the eyes, and the
mouth under them. The map uses these region ids of the humanoid map:
- torso (3): the body, the layers.
- ribs (9): the core, the inside cube.
- face (1): the front of layers 1 to 5.
The material is skin (1), and eyes (3) on the eyes. Red is the region id.
Green is the material id. Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from PIL import Image

from make_region_map import box

OUT = "src/main/resources/assets/originofspawns/textures/region/magma_cube.png"

FACE, TORSO, RIBS = 1, 3, 9
SKIN, EYES = 1, 3

FACE_LAYERS = (1, 2, 3, 4, 5)
EYE_LAYERS = (2, 3)
EYE_COLUMNS = (1, 2, 5, 6)


def layer_uv(i):
    """Return the uv of layer i, as MagmaCubeModel gives it."""
    return (0, 9 * i) if i < 4 else (32, 9 * i - 36)


def make():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for i in range(8):
        u, v = layer_uv(i)
        for x, y, face, row in box(u, v, 8, 1, 8):
            region, material = TORSO, SKIN
            if face == "front" and i in FACE_LAYERS:
                region = FACE
                if i in EYE_LAYERS and x - (u + 8) in EYE_COLUMNS:
                    material = EYES
            im.putpixel((x, y), (region, material, 0, 255))
    for x, y, face, row in box(24, 40, 4, 4, 4):
        im.putpixel((x, y), (RIBS, SKIN, 0, 255))
    return im


if __name__ == "__main__":
    im = make()
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
