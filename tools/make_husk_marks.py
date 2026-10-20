"""Make the marks of the husk genes.

Usage: python3 tools/make_husk_marks.py

The husk texture has the same UV layout and the same region map as the
zombie texture, so this script uses the helpers of make_gene_marks.py. Read
that script for how a mark works.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- gene_visuals/<gene>.json, with one pattern layer for each pattern.

The script prints the pixels that two husk genes share. The husk shares no
genes with the zombie, so the check looks only at the husk genes.

Husk skin is a dark gray brown. The shirt and the sleeves are a light tan
cloth. The top 2 rows of the face are tan.

Pixel plan for the Scorchers.
- Skin color: Kilnskin makes all the skin brick red (order -4). It is the
  only Scorcher with a color for all the skin.
- Face, eyes: Mirage has the 4 eye pixels, and the outer pixels over the
  eyes (x 9 and 14, y 11). The Sunstroke lids go over them (order 1).
- Face: Sunstroke has the flush on the cheeks and the nose (y 12 and 13),
  and the lids (y 11, and the outer eye pixels). Sunbaked has the forehead
  (y 10), the pixel between the eyes (x 12, y 11), x 8 of y 11, and the
  chin (y 14 and 15).
- Head sides, the neckline, and the hands (arm rows 8 to 11): Sunbaked has
  the cracks and the burns (order 1).
- Forearms and the top of the head: Kilnskin has the glaze shine (order -4).
- Chest: Solar Flare has the sun, a gene model that glows, over the chest
  columns 1 to 6, rows 2 to 7. It burns the cloth around the sun.
- Shirt back, shirt sides, chest rows 9 to 11, sleeves, and the top of the
  legs: Heatshimmer has the bleached patches.

Pixel plan for the Dunecrawlers.
- Sandswimmer has the creases of the pants (leg rows 2 to 4), and the hem
  of the shirt (row 11, chest and back columns 2 to 5, side columns 1 and
  2).
- Scuttler has the elbows (arm rows 5 and 6) and the knees (leg rows 5 and
  6).
- Dune Runner has the lower legs, rows 8 to 11, and the bottom of the feet.
- Sinkhole has the palms, the bottom face of the arms. The sand that pours
  is the gene model sinkhole_sand.
- Sandblast has the mouth (x 11 to 13, y 14) and 2 pixels of the chin (x 10
  and 14, y 15).
- Duststorm has a coat of dust over all the husk (order -2).
This script needs Pillow.
"""
import json
import os

from make_gene_marks import (ASSETS, BACK, CHEST, FRONT, INNER, LEFT_SIDE, MARK_DIR, OUTER, RIGHT_SIDE,
                             SHIRT_BACK, VISUAL_DIR, Mark, arm, body, head_side, leg, noise100,
                             touched, write)

TEXTURE_DIR = os.path.join(ASSETS, "textures/entity")
MODEL_DIR = os.path.join(ASSETS, "gene_models")


def sunbaked():
    # Sunburn and dry cracks on the skin that the sun hits most: the face,
    # the neck, and the hands. The gene has no skin color of its own, so it
    # reads as sunburn on normal skin and as fired cracks on Kilnskin. Its
    # marks are at order 1, over the Kilnskin color (order -4).
    crack = Mark("#22170E", order=1)
    burn = Mark("#6E2416", order=1)
    peel = Mark("#D8C09A", order=1)
    # The face: the forehead, between the eyes, and the chin.
    crack.put((10, 10)).put((11, 10), 220).put((12, 11)).put((10, 14), 220).put((9, 15))
    burn.put((13, 10)).put((14, 10), 220).put((8, 11), 230).put((12, 15)).put((13, 15), 220)
    peel.put((15, 10), 230).put((11, 15), 230)
    # The sides of the head, next to the face.
    for side in (0, 1):
        crack.put(head_side(side, 0, 3)).put(head_side(side, 1, 4)).put(head_side(side, 1, 5), 220)
        burn.put(head_side(side, 0, 6), 230).put(head_side(side, 1, 6))
        peel.put(head_side(side, 2, 4), 230)
    # The neck: the skin in the neckline of the shirt.
    crack.put((22, 20)).put((24, 20), 220)
    burn.put((25, 20)).put((24, 21), 230)
    # The hands: rows 8 to 11 of the arms.
    for face in (OUTER, FRONT, BACK):
        crack.put(arm(face, 1, 8)).put(arm(face, 2, 9), 220).put(arm(face, 2, 10))
        burn.put(arm(face, 0, 10), 230).put(arm(face, 3, 11)).put(arm(face, 0, 11), 220)
        peel.put(arm(face, 3, 9), 230)
    crack.put(arm(INNER, 2, 9)).put(arm(INNER, 1, 10), 220)
    return [crack, burn, peel]


def kilnskin():
    # Skin like fired clay: brick red with a glaze. The color goes under the
    # marks of other genes (order -4). A few light pixels show the shine of
    # the glaze on the forearms and the top of the head.
    brick = Mark("#9C3A22", alpha=215, order=-4, materials=["skin"], size=16)
    for y in range(16):
        for x in range(16):
            brick.put((x, y), 255 if noise100(x, y, 21) < 60 else 215)
    shine = Mark("#F4B088", alpha=230, order=-4, materials=["skin"])
    for col, row in ((1, 5), (1, 6), (2, 9)):
        shine.put(arm(FRONT, col, row), 255 if row != 9 else 210)
    for col, row in ((2, 5), (2, 6), (1, 9)):
        shine.put(arm(OUTER, col, row), 230)
    shine.put((10, 2), 230).put((11, 2)).put((13, 4), 210)
    shine.put(head_side(0, 1, 2), 230).put(head_side(1, 1, 2), 230)
    return [brick, shine]


def sunstroke():
    # Heat sickness: a red flush across the cheeks and the nose, and heavy,
    # half-closed lids over the eyes. The flush is smooth, with no spots.
    # The lids are at order 1, so that they go over the Mirage eyes.
    flush = Mark("#C83A28", alpha=140)
    for p in ((8, 13), (9, 13), (14, 13), (15, 13), (11, 13), (12, 13)):
        flush.put(p)
    soft = Mark("#C83A28", alpha=75)
    for p in ((8, 12), (15, 12), (11, 12), (12, 12), (10, 13), (13, 13), (8, 14), (9, 14),
              (14, 14), (15, 14)):
        soft.put(p)
    lid = Mark("#3A2C20", order=1)
    for x in (9, 10, 13, 14):
        lid.put((x, 11))
    droop = Mark("#4E3E2C", alpha=230, order=1)
    droop.put((9, 12)).put((14, 12))
    return [flush, soft, lid, droop]


def mirage():
    # Pale, shimmering eyes: the eyes are a pale blue white, and a faint
    # shimmer rises over the outer corners.
    eye = Mark("#D8F0F4", alpha=240)
    eye.put((9, 12)).put((14, 12)).put((10, 12), 215).put((13, 12), 215)
    haze = Mark("#D8F0F4", alpha=110)
    haze.put((9, 11)).put((14, 11))
    return [eye, haze]


def heatshimmer():
    # Pale, bleached patches on the cloth of the body: the shirt back, the
    # sides of the shirt, the sleeves, and the top of the legs.
    pale = Mark("#F2EAD6", alpha=200)
    for p in ((33, 21), (34, 21), (34, 22), (37, 24), (38, 24), (38, 25), (37, 25),
              (33, 28), (34, 28), (34, 29), (36, 27)):
        pale.put(body(SHIRT_BACK, p[0] - 32, p[1] - 20), 255 if sum(p) % 2 else 225)
    for face, cells in ((RIGHT_SIDE, ((1, 2), (2, 2), (1, 3), (2, 7))),
                        (LEFT_SIDE, ((1, 4), (2, 4), (2, 5), (1, 8)))):
        for col, row in cells:
            pale.put(body(face, col, row), 240)
    for face, cells in ((OUTER, ((1, 1), (2, 1), (2, 2))), (BACK, ((0, 2), (1, 2))),
                        (FRONT, ((3, 0), (3, 1)))):
        for col, row in cells:
            pale.put(arm(face, col, row), 235)
    for col, row in ((0, 9), (1, 10), (0, 10), (7, 10), (6, 11)):
        pale.put(body(CHEST, col, row), 240)
    for face, cells in ((OUTER, ((1, 0), (2, 0), (1, 1))), (FRONT, ((0, 1),)),
                        (BACK, ((1, 0),))):
        for col, row in cells:
            pale.put(leg(face, col, row), 235)
    return [pale]


# The sun of Solar Flare: 6x6 pixels. W is the white core, C the yellow
# disc, and R the orange rays.
SUN = (".R..R.",
       "R.CC.R",
       ".CWWC.",
       ".CWWC.",
       "R.CC.R",
       ".R..R.")
SUN_COLORS = {"W": (255, 246, 196, 255), "C": (255, 200, 74, 255), "R": (255, 132, 40, 255)}


def solar_flare():
    # The cloth around the sun is burnt. The sun itself is the gene model
    # solar_flare_sun.
    burn = Mark("#3A2414")
    for col, row in ((0, 3), (7, 4), (3, 1), (4, 8), (0, 5), (7, 2)):
        burn.put(body(CHEST, col, row), 255 if col in (0, 7) else 215)
    return [burn]


def solar_flare_model():
    # A flat 6x6 plane just in front of the chest, over chest columns 1 to 6
    # and rows 2 to 7. On the body bone, the chest is at z -2, and chest
    # column 0 is x -4. The plane glows.
    from PIL import Image
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, line in enumerate(SUN):
        for x, ch in enumerate(line):
            if ch in SUN_COLORS:
                im.putpixel((x, y), SUN_COLORS[ch])
                im.putpixel((6 + x, y), SUN_COLORS[ch])
    model = {
        "texture": "originofspawns:textures/entity/solar_flare_sun.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": [{"bone": "body", "uv": [0, 0], "from": [-3, 2, -2.05], "size": [6, 6, 0], "glow": True}],
    }
    im.save(os.path.join(TEXTURE_DIR, "solar_flare_sun.png"))
    with open(os.path.join(MODEL_DIR, "solar_flare_sun.json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


# The colors of sand: light, middle, and dark.
SAND = ("#E0C88E", "#C4A66C", "#A08552")


def sandswimmer():
    # Sand caught in the folds of the cloth: streaks of sand in the creases
    # of the pants, rows 2 to 4, and along the hem of the shirt, row 11.
    light, mid = Mark(SAND[0]), Mark(SAND[1])
    # The creases are thin lines down the sides of the front face, and on the
    # outer and the back faces.
    for face, cells in ((OUTER, ((2, 2), (2, 3), (1, 4))), (FRONT, ((0, 2), (0, 3), (0, 4), (3, 3))),
                        (INNER, ((1, 2), (1, 3))), (BACK, ((1, 2), (1, 3), (2, 4)))):
        for col, row in cells:
            (light if (col + row) % 2 else mid).put(leg(face, col, row), 255 if row != 4 else 225)
    for col in (2, 3, 4, 5):
        (light if col % 2 else mid).put(body(CHEST, col, 11), 240)
        (mid if col % 2 else light).put(body(SHIRT_BACK, col, 11), 240)
    for face in (RIGHT_SIDE, LEFT_SIDE):
        mid.put(body(face, 1, 11)).put(body(face, 2, 11), 220)
    return [light, mid]


def dune_runner():
    # Feet wrapped in cloth: pale strips of cloth around the lower legs, rows
    # 8 to 11, with dark lines between the turns, and wraps under the feet.
    wrap = Mark("#D6CBB0")
    line = Mark("#5E5038")
    for face in (OUTER, FRONT, INNER, BACK):
        for col in range(4):
            for row in (8, 9, 10, 11):
                # The strips go round the leg at a slant.
                if (row + col // 2 + face // 4) % 3 == 0:
                    line.put(leg(face, col, row), 230)
                else:
                    wrap.put(leg(face, col, row), 255 if (col + row) % 2 else 225)
    for x in range(8, 12):
        for y in range(16, 20):
            wrap.put([(x, y), (x + 16, y + 32)], 235 if (x + y) % 2 else 210)
    return [wrap, line]


def sinkhole():
    # Sand caked on the palms, under the hands. The sand that pours from the
    # hands is the gene model sinkhole_sand.
    caked = Mark(SAND[1])
    for x in range(48, 52):
        for y in range(16, 20):
            caked.put([(x, y), (x - 8, y + 32)], 255 if (x + y) % 2 else 220)
    return [caked]


def sinkhole_model():
    # A thin stream of sand under each hand, and two grains that fall below
    # it. On an arm bone, the hand is at y 10. The right hand is from x -3 to
    # 1, and the left hand from x -1 to 3.
    from PIL import Image
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    colors = [tuple(int(c[i:i + 2], 16) for i in (1, 3, 5)) + (255,) for c in SAND]
    # The stream: 1x4x1 at uv (0, 0). A grain: 1x1x1 at uv (0, 8).
    for y in range(6):
        for x in range(4):
            im.putpixel((x, y), colors[(x + 2 * y) % 3])
    for y in range(8, 10):
        for x in range(4):
            im.putpixel((x, y), colors[(x + y) % 2])
    parts = []
    for bone, x in (("right_arm", -1.5), ("left_arm", 0.5)):
        parts.append({"bone": bone, "uv": [0, 0], "from": [x, 10, -0.5], "size": [1, 4, 1]})
        parts.append({"bone": bone, "uv": [0, 8], "from": [x, 15, -0.5], "size": [1, 1, 1]})
        parts.append({"bone": bone, "uv": [0, 8], "from": [x + (1 if x < 0 else -1), 17, 0.5],
                      "size": [1, 1, 1]})
    model = {
        "texture": "originofspawns:textures/entity/sinkhole_sand.png",
        "texture_width": 16,
        "texture_height": 16,
        "parts": parts,
    }
    im.save(os.path.join(TEXTURE_DIR, "sinkhole_sand.png"))
    with open(os.path.join(MODEL_DIR, "sinkhole_sand.json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


def scuttler():
    # Sand packed into the joints: uneven clumps on the elbows (arm rows 5
    # and 6) and on the knees (leg rows 5 and 6).
    light, dark = Mark(SAND[0]), Mark(SAND[2])
    for at in (arm, leg):
        light.put(at(FRONT, 1, 5)).put(at(FRONT, 2, 5), 225).put(at(FRONT, 2, 6), 240)
        dark.put(at(FRONT, 1, 6), 230)
        light.put(at(OUTER, 2, 5), 240).put(at(OUTER, 1, 6))
        dark.put(at(OUTER, 2, 6), 230)
        light.put(at(BACK, 1, 5), 235).put(at(BACK, 2, 6))
        dark.put(at(BACK, 2, 5), 230)
        light.put(at(INNER, 2, 6), 225)
    return [light, dark]


def sandblast():
    # A mouth full of sand: sand fills the open mouth, and a few grains
    # spill on the chin.
    sand = Mark(SAND[0])
    sand.put((11, 14)).put((12, 14), 235)
    shade = Mark(SAND[2])
    shade.put((13, 14))
    spill = Mark(SAND[1], alpha=200)
    spill.put((10, 15)).put((14, 15), 220)
    return [sand, shade, spill]


def duststorm():
    # A coat of fine dust over the whole husk: a pale haze and small specks.
    # They go under the marks of other genes (order -2). The dust cloud
    # around the husk needs particles, which a gene visual cannot make.
    haze = Mark("#D8C8A0", alpha=70, order=-2, size=16).fill()
    speck = Mark("#EADCB8", alpha=170, order=-2, size=16)
    for y in range(16):
        for x in range(16):
            if noise100(x, y, 33) < 9:
                speck.put((x, y), 255 if noise100(x, y, 34) < 50 else 215)
    return [haze, speck]


GENES = {
    "sunbaked": sunbaked,
    "heatshimmer": heatshimmer,
    "kilnskin": kilnskin,
    "sunstroke": sunstroke,
    "mirage": mirage,
    "solar_flare": solar_flare,
    "sandswimmer": sandswimmer,
    "dune_runner": dune_runner,
    "sinkhole": sinkhole,
    "scuttler": scuttler,
    "sandblast": sandblast,
    "duststorm": duststorm,
}

MODELS = {
    "solar_flare": "originofspawns:solar_flare_sun",
    "sinkhole": "originofspawns:sinkhole_sand",
}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    solar_flare_model()
    sinkhole_model()
    for gene, make in GENES.items():
        marks = make()
        write(gene, marks, MODELS)
        marked = [m for m in marks if not isinstance(m, dict) and m.order >= 0]
        print(gene, sum(len(m.pixels) for m in marked), "mark pixels")
    owners = {}
    for gene in GENES:
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in touched(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = {}
    for p, genes in owners.items():
        if len(genes) > 1:
            key = " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more husk genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
