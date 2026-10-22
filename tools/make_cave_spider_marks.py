"""Make the gene visuals of the cave spider, from design/species/cave_spider.md.

Usage: python3 tools/make_cave_spider_marks.py (from the root of the repository)

The cave spider has the model and the texture layout of the spider, and it
uses the spider region map. make_spider_marks.py gives the faces of the
parts. The skin of the cave spider is dark teal. The glowing eyes draw over
the marks, so a gene that changes the eyes puts a rim round them.

Pixel plan for the Toxins.
- Neurotoxin has blue tips on the fangs: pale blue fangs with bright blue
  tips, and blue drops under them.
- Hemotoxin has dark red fangs, with a dark red stain at the root of each
  fang.
- Toxic Cloud has green spots on the abdomen: round green spots with a dark
  green rim on the top and the sides.
- Venom Spray has green pores on the body: dark pores with a green rim on
  each face of the body.
- Lingering Venom has green drool from the fangs: green fang tips, green
  drool along the bottom row of the face, and drops under the head.
- Corrosive has yellow fangs, with pits of dull yellow beside them.

Pixel plan for the Gloomers.
- Lightsnuffer has black around the eyes: the pixels next to the eyes are
  black.
- Shade Skin has dark blue patches on the body, and on the front of the
  abdomen next to it.
- Echo Sense has pale, blind eyes: a milky, pale rim round the eyes.
- Night Eyes has green eyes: a bright green rim round the eyes.
- Dread has a dark mist pattern on the back: waves of gray violet mist on
  the top of the abdomen.
- Cave Ghost has faded, pale legs: the legs are pale gray teal, palest at
  the tip.
This script needs Pillow.
"""
import json
import os

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_spider_marks import ABDOMEN, ALL, BODY, EYES, FANGS, HEAD, leg_pixels, pixels
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/spider.png"


def eye_rim():
    """Return the pixels of the head next to an eye, which are not an eye or
    a fang."""
    out = []
    for x, y, face, col, row in pixels(HEAD, ("right", "front", "left")):
        if (x, y) in EYES or (x, y) in FANGS:
            continue
        if any((x + dx, y + dy) in EYES for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            out.append((x, y))
    return out


def neurotoxin():
    # Blue tips on the fangs: the upper half of each fang is pale blue, the
    # tip is bright blue, and a blue drop hangs under each fang on the bottom
    # of the head.
    pale = Mark("#A8E0FF")
    tip = Mark("#40A0FF")
    pale.put([(41, 18), (46, 18)])
    tip.put([(41, 19), (46, 19), (49, 4), (54, 4)])
    return [pale, tip]


def hemotoxin():
    # Dark red fangs: the fangs are dark red, darker at the tip, with a dark
    # red stain at the root of each fang.
    red = Mark("#C82020")
    tip = Mark("#7A0A0A")
    red.put([(41, 18), (46, 18)])
    tip.put([(41, 19), (46, 19)])
    red.put([(40, 18), (47, 18)], 170)
    return [red, tip]


def toxic_cloud():
    # Green spots on the abdomen: round spots of 2x2 pixels, bright green,
    # with a dark green rim, on the top and the sides of the abdomen.
    spot = Mark("#7AE050")
    rim = Mark("#3A8A2A")
    spots = [(12 + c, 12 + r) for c, r in ((1, 1), (6, 2), (3, 5), (7, 7), (1, 9))]
    spots += [(x, 24 + r) for x, r in ((2, 2), (7, 4), (24, 3), (29, 1), (37, 3))]
    for x, y in spots:
        spot.put([(x, y), (x + 1, y), (x, y + 1), (x + 1, y + 1)])
        rim.put([(x - 1, y), (x - 1, y + 1), (x + 2, y), (x + 2, y + 1), (x, y - 1), (x + 1, y - 1),
                 (x, y + 2), (x + 1, y + 2)])
    return [rim, spot]


def venom_spray():
    # Green pores on the body: on each face of the body, three dark pores,
    # each with a bright green rim.
    pore = Mark("#0A140A")
    rim = Mark("#5CD040")
    for face in ("top", "right", "front", "left", "back"):
        x0, y0, w, h = BODY[face]
        for col, row in ((1, 1), (4, 2), (2, 4)):
            pore.put((x0 + col, y0 + row))
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                c, r = col + dx, row + dy
                if 0 <= c < w and 0 <= r < h and (c, r) not in ((1, 1), (4, 2), (2, 4)):
                    rim.put((x0 + c, y0 + r))
    return [rim, pore]


def lingering_venom():
    # Green drool from the fangs: green fang tips, green drool along the
    # bottom row of the face between and beside the fangs, and drops under
    # the front of the head. The drool keeps to the bottom row, so that it
    # does not look like tears.
    drool = Mark("#5CD040")
    pale = Mark("#B8F0A0")
    drool.put([(41, 19), (46, 19), (42, 19), (43, 19), (44, 19), (45, 19), (40, 19), (47, 19)])
    pale.put([(41, 18), (46, 18)])
    drool.put([(49, 4), (54, 4), (50, 4), (53, 4), (49, 5), (54, 5)])
    pale.put([(51, 5), (52, 6)])
    return [drool, pale]


def corrosive():
    # Yellow fangs: the fangs are yellow, darker at the tip, with pits of
    # dull yellow on the face beside them.
    yellow = Mark("#E8D040")
    tip = Mark("#B89A20")
    pit = Mark("#7A6A20")
    yellow.put([(41, 18), (46, 18)])
    tip.put([(41, 19), (46, 19)])
    pit.put([(40, 19), (47, 19), (42, 17), (45, 17)])
    return [yellow, tip, pit]


def lightsnuffer():
    # Black around the eyes: each pixel of the head next to an eye is black,
    # so that the red eyes glow in a black mask.
    black = Mark("#050505")
    black.put(eye_rim())
    return [black]


def shade_skin():
    # Dark blue patches on the body: patches of deep blue, with paler blue
    # edges, on all the faces of the body, and on the front of the abdomen
    # and the first rows of its top next to the body.
    deep = Mark("#1A2A70")
    edge = Mark("#30449A")
    for x, y, face, col, row in pixels(BODY, ALL):
        n = noise100(x // 2, y // 2, 611)
        if n < 45:
            deep.put((x, y))
        elif n < 60:
            edge.put((x, y))
    for x, y, face, col, row in pixels(ABDOMEN, ("front", "top")):
        if face == "top" and row < 9:
            continue
        n = noise100(x // 2, y // 2, 613)
        if n < 40:
            deep.put((x, y))
        elif n < 55:
            edge.put((x, y))
    return [deep, edge]


def echo_sense():
    # Pale, blind eyes: a milky, pale rim round the eyes, as a film over
    # them. The red eyes glow dim in the pale rim.
    milk = Mark("#8A9C9C")
    milk.put(eye_rim())
    return [milk]


def night_eyes():
    # Green eyes: a bright green rim round the eyes, pale green under the
    # middle eyes.
    green = Mark("#40E060")
    pale = Mark("#B0F8B0")
    for x, y in eye_rim():
        (pale if y == 17 else green).put((x, y))
    return [green, pale]


def dread():
    # A dark mist pattern on the back: waves of gray violet mist that run
    # across the top of the abdomen, with a paler crest on each wave.
    mist = Mark("#3A3448")
    crest = Mark("#6A6280")
    for x, y, face, col, row in pixels(ABDOMEN, ("top",)):
        wave = (row + (0, 1, 1, 0, -1, -1, 0, 1, 1, 0)[col]) % 4
        if wave == 0:
            crest.put((x, y))
        elif wave == 1:
            mist.put((x, y))
    return [mist, crest]


def cave_ghost():
    # Faded, pale legs: the legs are pale gray teal, and paler to the tip,
    # as if they fade into the dark.
    root = Mark("#7A9490")
    middle = Mark("#9AB4B0")
    tip = Mark("#C0D4D0")
    for x, y, face, t, row in leg_pixels():
        (tip if t <= 5 else middle if t <= 10 else root).put((x, y))
    return [root, middle, tip]


GENES = {
    "neurotoxin": neurotoxin,
    "hemotoxin": hemotoxin,
    "toxic_cloud": toxic_cloud,
    "venom_spray": venom_spray,
    "lingering_venom": lingering_venom,
    "corrosive": corrosive,
    "lightsnuffer": lightsnuffer,
    "shade_skin": shade_skin,
    "echo_sense": echo_sense,
    "night_eyes": night_eyes,
    "dread": dread,
    "cave_ghost": cave_ghost,
}

MODELS = {}

MODEL_MAKERS = []


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for make in MODEL_MAKERS:
        make()
    for gene, make in GENES.items():
        write(gene, make(), MODELS, REGION_MAP)
    owners = {}
    for gene in GENES:
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in changed_pixels(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = {}
    for (where, _), genes in owners.items():
        if len(genes) > 1:
            key = where + ": " + " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more cave spider genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
