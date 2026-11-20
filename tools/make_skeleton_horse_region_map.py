"""Make the skeleton horse region map.

Usage: python3 tools/make_skeleton_horse_region_map.py (from the root of the repository)

The skeleton horse texture (textures/entity/horse/horse_skeleton.png) is
64x64. UndeadHorseRenderer uses HorseModel for it, with the same mesh as the
zombie horse, not scaled. So the skeleton horse has the same parts and the
same UV layout as the zombie horse. tools/make_zombie_horse_region_map.py
gives the parts and the region ids. The baby uses BabyHorseModel and its own
texture, horse_skeleton_baby.png. This map is for the adult.

Many pixels of the skeleton horse texture are clear, between the bones. The
map still gives each pixel of each part a region, so a mark on a clear pixel
stays clear.

The material is bone (4), and eyes (3) on the dark eye sockets, at x 3 and
4, and 15 and 16, at y 21. Red is the region id. Green is the material id.
Blue is 0. A pixel with alpha 0 is not used.
This script needs Pillow.
"""
import os

from make_zombie_horse_region_map import EYE_PIXELS, make

OUT = "src/main/resources/assets/originofspawns/textures/region/skeleton_horse.png"

BONE = 4


if __name__ == "__main__":
    im = make(material=BONE, eyes=EYE_PIXELS)
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print(OUT)
