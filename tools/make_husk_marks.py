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
- Skin color: Kilnskin makes all the skin brick red (order -4). Sunbaked
  puts a red flush on all the skin (order -3). Both go under the marks.
- Face, eyes: Mirage has the 4 eye pixels, and the outer pixels over the
  eyes (x 9 and 14, y 11).
- Face: Sunstroke has the blisters on the forehead (y 10), on the cheeks
  (x 8 and 15, y 13 and 14), and at the chin (y 15).
- Head sides and the back of the head: Sunbaked has the cracks.
- Forearms: Sunbaked has the cracks on the outer and the back faces, rows 5
  to 10, and in columns 0 and 3 of the front face. Kilnskin has the glaze
  shine (order -4).
- Chest: Solar Flare has the sun, a gene model that glows, over the chest
  columns 1 to 6, rows 2 to 7. It burns the cloth around the sun.
- Shirt back, shirt sides, chest rows 9 to 11, sleeves, and the top of the
  legs: Heatshimmer has the bleached patches.
This script needs Pillow.
"""
import json
import os

from make_gene_marks import (ASSETS, BACK, CHEST, FRONT, LEFT_SIDE, MARK_DIR, OUTER, RIGHT_SIDE,
                             SHIRT_BACK, VISUAL_DIR, Mark, arm, body, head_side, leg, noise100,
                             touched, write)

TEXTURE_DIR = os.path.join(ASSETS, "textures/entity")
MODEL_DIR = os.path.join(ASSETS, "gene_models")


def sunbaked():
    # Sunburnt skin: a red flush on all the skin, under the marks. Dry cracks
    # with a pale, dry edge on the forearms, on the sides of the head, and on
    # the back of the head.
    flush = Mark("#D2604A", alpha=70, order=-3, materials=["skin"], size=16).fill()
    crack = Mark("#22170E")
    dry = Mark("#C9AE80")
    for face in (OUTER, BACK):
        for col, row in ((1, 5), (2, 6), (2, 7), (1, 8), (3, 9)):
            crack.put(arm(face, col, row), 255 if row % 2 else 220)
        for col, row in ((2, 5), (3, 6), (1, 7), (0, 8)):
            dry.put(arm(face, col, row), 230)
    for col, row in ((3, 6), (3, 7), (0, 9), (0, 10)):
        crack.put(arm(FRONT, col, row))
    dry.put(arm(FRONT, 2, 7), 230).put(arm(FRONT, 1, 10), 230)
    for side in (0, 1):
        for col, row in ((2, 3), (3, 4), (3, 5), (4, 6), (5, 6)):
            crack.put(head_side(side, col, row))
        for col, row in ((3, 3), (4, 5), (5, 7)):
            dry.put(head_side(side, col, row), 230)
    for p in ((25, 10), (26, 11), (26, 12), (29, 13), (30, 13), (30, 14)):
        crack.put(p)
    for p in ((25, 11), (29, 14)):
        dry.put(p, 230)
    return [flush, crack, dry]


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
    # Heat blisters on the face: a pale, swollen core with a red rim.
    core = Mark("#EED9A0")
    rim = Mark("#B23A26")
    core.put((11, 10)).put((8, 13)).put((15, 14)).put((12, 15), 230)
    for p in ((10, 10), (12, 10), (8, 14), (15, 13), (11, 15), (13, 15)):
        rim.put(p, 220)
    return [core, rim]


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


GENES = {
    "sunbaked": sunbaked,
    "heatshimmer": heatshimmer,
    "kilnskin": kilnskin,
    "sunstroke": sunstroke,
    "mirage": mirage,
    "solar_flare": solar_flare,
}

MODELS = {
    "solar_flare": "originofspawns:solar_flare_sun",
}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    solar_flare_model()
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
