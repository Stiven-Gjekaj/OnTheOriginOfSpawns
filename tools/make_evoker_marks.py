"""Make the gene visuals of the evoker, from design/species/evoker.md.

Usage: python3 tools/make_evoker_marks.py (from the root of the repository)

The evoker texture is 64x64, with the illager layout, and it uses the illager
region map. make_pillager_marks.py gives the faces of the parts, and
make_vindicator_marks.py gives the pixels of the arms, which the evoker also
crosses. On the evoker:
- The robe (the coat) covers the body and reaches to the ankles: rows 0 to 19
  of its sides are drawn, with a V neck at the top of the front and a gold
  trim down the middle of the front. So the marks on the body go on the robe.
- Only rows 8 to 11 of the legs show under the robe: the boots.
- When it does not cast, it crosses its arms, and its hands stay in the
  sleeves. When it casts, it raises its arms, and its hands show.
The gene models use the bones head, body, right_arm, left_arm, right_leg, and
left_leg. A model on an arm shows only while the arms are apart.

Pixel plan for the Fangcallers.
- Fang Line has a row of teeth on the robe: a dark red gum line round the
  lower robe, with pale teeth that hang from it.
- Fang Ring has rings of teeth on the sleeves: two rings of pale teeth round
  each sleeve.
- Barbed Fangs: hooked claws at the tips of the hands, the gene model
  barbed_fangs_claws. They show while the arms are raised.
- Swift Cast has bright, glowing eyes: the gene model swift_cast_eyes, two
  white plates with full light on the eyes.
- Golem Jaws: the iron jaw of a golem on the belt at the right hip, the gene
  model golem_jaws_jaw.
- Upheaval has cracked earth on the boots: brown earth with dark cracks and
  gray stones.

Pixel plan for the Conjurers.
- Vex Swarm: two pale vex wings on the back, the gene model vex_swarm_wings.
- Iron Vexes has iron bands on the sleeves: three bands of iron with a pale
  upper edge.
- Long Bond has a pale thread at each wrist: a pale thread round each wrist,
  with a loose end, and two threads where the crossed forearms meet.
- Sharp Vexes: a small vex skull on the top of the head, the gene model
  sharp_vexes_skull.
- Vengeful Vexes has a cage of bone on the chest: pale ribs across the front
  of the robe, with a breastbone down the middle.
- Venom Vexes has green runes on the sleeves: small green signs on each face
  of the sleeves.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_pillager_marks import ARM, COAT, HEAD, LEG, SIDES, STEEL, pixels
from make_stray_marks import changed_pixels
from make_vindicator_marks import CROSSED_BAR, V_NECK, hand_pixels, sleeve_pixels

REGION_MAP = "originofspawns:textures/region/illager.png"

# The rows of the robe that the evoker draws.
ROBE_ROWS = range(0, 20)

BONE = ("#E8E4D8", "#B8B0A0")
GOLD = ("#E0B030", "#F8E080", "#8A6A10")


def robe_pixels(faces=SIDES):
    """Return each pixel (x, y, face, col, row) of the sides of the robe, without the V neck."""
    for x, y, face, col, row in pixels(COAT, faces):
        if row in ROBE_ROWS and not (face == "front" and (col, row) in V_NECK):
            yield x, y, face, col, row


def boot_pixels(faces=SIDES):
    """Return each pixel (x, y, face, col, row) of the boots, rows 8 to 11 of the legs, which show under the robe."""
    for x, y, face, col, row in pixels(LEG, faces):
        if row >= 8:
            yield x, y, face, col, row


def eye_plates(name, color):
    """Save a gene model of two thin plates with full light on the eyes of the face. On the head bone, the face is at
    z -4, and the eyes are on row -4 of the head, from x -3 to -1 and from x 1 to 3."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 2, 1, 1, lambda face, x, y: color)
    parts = [{"bone": "head", "uv": [0, 0], "from": [x, -4, -4.5], "size": [2, 1, 1], "inflate": -0.45, "glow": True}
             for x in (-3, 1)]
    save_model(name, im, parts)


def fang_line():
    # A row of teeth on the robe: a dark red gum line on row 13 round the
    # robe, and pale teeth that hang from it, on all of row 14 and on every
    # other pixel of row 15.
    gum = Mark("#5A1A1A")
    tooth = Mark(BONE[0])
    for x, y, face, col, row in robe_pixels():
        if row == 13:
            gum.put((x, y))
        elif row == 14 or row == 15 and col % 2 == 0:
            tooth.put((x, y))
    return [gum, tooth]


def fang_ring():
    # Rings of teeth on the sleeves: on rows 2 and 6 of the sleeves, a pale
    # ring, with teeth on every other pixel of the row under it.
    tooth = Mark(BONE[0])
    shade = Mark(BONE[1])
    for x, y, face, col, row in sleeve_pixels(0, 9):
        if row in (2, 6):
            tooth.put((x, y))
        elif row in (3, 7) and col % 2 == 0:
            shade.put((x, y))
    return [tooth, shade]


def barbed_fangs():
    # The claws are the gene model barbed_fangs_claws.
    return []


def barbed_fangs_model():
    # Hooked claws: three pale, hooked claws at the tip of each hand, each a
    # short claw that bends in, with a dark point. They show while the arms
    # are raised. On an arm bone, the hand ends at y 10. The right arm is from
    # x -3 to 1, and the left arm from x -1 to 3.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def claw(face, x, y):
        return "#3A3028" if y == 1 else BONE[0] if face in ("front", "left") else BONE[1]

    # A claw: 1x2x1 at uv (0, 0).
    paint_box(im, 0, 0, 1, 2, 1, claw)
    parts = []
    for bone, x0 in (("right_arm", -3), ("left_arm", -1)):
        for i, z in enumerate((-1.5, -0.5, 0.5)):
            x = x0 + 0.5 + i * 1.2
            parts.append({"bone": bone, "uv": [0, 0], "from": [x, 9.5, z], "size": [1, 2, 1], "inflate": -0.1,
                          "pivot": [x + 0.5, 10, z + 0.5], "rotation": [25, 0, 0]})
    save_model("barbed_fangs_claws", im, parts)


def swift_cast():
    # Bright, glowing eyes: the eye pixels are bright white, and the gene
    # model swift_cast_eyes puts plates with full light on them.
    bright = Mark("#F0FFFF")
    bright.put([(9, 14), (10, 14), (13, 14), (14, 14)])
    return [bright]


def swift_cast_model():
    eye_plates("swift_cast_eyes", "#E8FFFF")


def golem_jaws():
    # The jaw is the gene model golem_jaws_jaw.
    return []


def golem_jaws_model():
    # An iron jaw on the belt: the pale iron jaw of an iron golem, with a row
    # of dark teeth and a green vine, that hangs at the right hip of the
    # robe. On the body bone, the right side of the robe is at x -4.5.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def jaw(face, x, y):
        if face in ("right", "front", "left", "back") and y == 0:
            return "#3A3A3A" if x % 2 else "#C8C8C0"
        if face == "right" and (x, y) in ((1, 1), (2, 2)):
            return "#4A8A3A"
        return "#C8C8C0" if face in ("top", "right", "front") else "#9A9A94"

    # The jaw: 2x3x4 at uv (0, 0).
    paint_box(im, 0, 0, 2, 3, 4, jaw)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-6.5, 7, -2], "size": [2, 3, 4]}]
    save_model("golem_jaws_jaw", im, parts)


def upheaval():
    # Cracked earth on the boots: the boots are brown earth, with dark cracks
    # and gray stones.
    earth = Mark("#6A4A2A")
    crack = Mark("#2A1A10")
    stone = Mark("#8A8A84")
    for x, y, face, col, row in boot_pixels():
        n = noise100(x, y, 1301)
        (crack if (col + row) % 4 == 0 else stone if n < 15 else earth).put((x, y))
    for x, y, face, col, row in pixels(LEG, ("bottom",)):
        earth.put((x, y))
    return [earth, crack, stone]


def vex_swarm():
    # The wings are the gene model vex_swarm_wings.
    return []


def vex_swarm_model():
    # Vex wings on the back: two pale, thin wings, as the wings of a vex, that
    # spread out from the top of the back, each with dark veins. On the body
    # bone, the back of the robe is at z 3.5.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def wing(face, x, y):
        if face in ("front", "back"):
            if y == 0 or (x + y) % 3 == 0:
                return "#8A9AB0"
            return "#D8E4F0"
        return "#B8C4D4"

    # A wing: 5x6x1 at uv (0, 0).
    paint_box(im, 0, 0, 5, 6, 1, wing)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-5.5, 0.5, 3.6], "size": [5, 6, 1], "pivot": [-0.5, 1, 4],
         "rotation": [0, 30, -15]},
        {"bone": "body", "uv": [0, 0], "from": [0.5, 0.5, 3.6], "size": [5, 6, 1], "mirror": True,
         "pivot": [0.5, 1, 4], "rotation": [0, -30, 15]},
    ]
    save_model("vex_swarm_wings", im, parts)


def iron_vexes():
    # Iron bands on the sleeves: three bands of iron round each sleeve, on
    # rows 1, 4, and 7, with a pale upper row on rows 0, 3, and 6.
    iron = Mark(STEEL[1])
    bright = Mark(STEEL[0])
    for x, y, face, col, row in sleeve_pixels(0, 9):
        if row in (1, 4, 7):
            iron.put((x, y))
        elif row in (0, 3, 6):
            bright.put((x, y))
    return [iron, bright]


def long_bond():
    # A pale thread at each wrist: a pale thread round the wrist (row 9 of
    # the arms), with a loose end that hangs onto the hand, and two threads
    # across the front of the crossed forearms, where the wrists are.
    thread = Mark("#E8E8F0")
    for x, y, face, col, row in pixels(ARM, SIDES):
        if row == 9 or row == 10 and face == "front" and col == 1:
            thread.put((x, y))
    x0, y0 = CROSSED_BAR["front"][:2]
    thread.put([(x0 + c, y0 + r) for c in (2, 5) for r in range(4)])
    return [thread]


def sharp_vexes():
    # The skull is the gene model sharp_vexes_skull.
    return []


def sharp_vexes_model():
    # A vex skull on the hood: a small, pale skull of a vex on the top of the
    # head, with dark eye holes on its front and two short horns. On the head
    # bone, the top of the head is at y -10.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def skull(face, x, y):
        if face == "front" and y == 1 and x in (0, 2):
            return "#201818"
        return BONE[0] if face in ("top", "front", "left") else BONE[1]

    # The skull: 3x3x3 at uv (0, 0). A horn: 1x2x1 at uv (16, 0).
    paint_box(im, 0, 0, 3, 3, 3, skull)
    paint_box(im, 16, 0, 1, 2, 1, lambda face, x, y: BONE[1])
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-1.5, -12.8, -2.5], "size": [3, 3, 3]},
        {"bone": "head", "uv": [16, 0], "from": [-2, -14, -1.5], "size": [1, 2, 1], "pivot": [-1.5, -12.5, -1],
         "rotation": [0, 0, -25]},
        {"bone": "head", "uv": [16, 0], "from": [1, -14, -1.5], "size": [1, 2, 1], "pivot": [1.5, -12.5, -1],
         "rotation": [0, 0, 25]},
    ]
    save_model("sharp_vexes_skull", im, parts)


def vengeful_vexes():
    # A cage of bone on the chest: on the front of the robe, pale ribs across
    # rows 3, 5, 7, and 9, and a breastbone down the middle (columns 3 and 4)
    # from row 3 to row 9. The sides of the robe show the ends of the ribs.
    rib = Mark(BONE[0])
    shade = Mark(BONE[1])
    for x, y, face, col, row in robe_pixels(("front", "right", "left")):
        if face == "front":
            if row in (3, 5, 7, 9) and 1 <= col <= 6:
                rib.put((x, y))
            elif 3 <= row <= 9 and col in (3, 4):
                shade.put((x, y))
        elif row in (3, 5, 7, 9) and col in ((0, 1) if face == "left" else (4, 5)):
            shade.put((x, y))
    return [rib, shade]


def venom_vexes():
    # Green runes on the sleeves: on each face of the sleeves, two small
    # green signs, each an L of 3 pixels, on rows 2 and 3 and on rows 6 and 7.
    rune = Mark("#5CD040")
    for x, y, face, col, row in sleeve_pixels(0, 9):
        if (row, col) in ((2, 1), (3, 1), (3, 2), (6, 2), (7, 2), (7, 1)):
            rune.put((x, y))
    return [rune]


GENES = {
    "fang_line": fang_line,
    "fang_ring": fang_ring,
    "barbed_fangs": barbed_fangs,
    "swift_cast": swift_cast,
    "golem_jaws": golem_jaws,
    "upheaval": upheaval,
    "vex_swarm": vex_swarm,
    "iron_vexes": iron_vexes,
    "long_bond": long_bond,
    "sharp_vexes": sharp_vexes,
    "vengeful_vexes": vengeful_vexes,
    "venom_vexes": venom_vexes,
}

MODELS = {
    "barbed_fangs": "originofspawns:barbed_fangs_claws",
    "swift_cast": "originofspawns:swift_cast_eyes",
    "golem_jaws": "originofspawns:golem_jaws_jaw",
    "vex_swarm": "originofspawns:vex_swarm_wings",
    "sharp_vexes": "originofspawns:sharp_vexes_skull",
}

MODEL_MAKERS = [barbed_fangs_model, swift_cast_model, golem_jaws_model, vex_swarm_model, sharp_vexes_model]


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
    print("pixels that two or more evoker genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
