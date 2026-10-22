"""Make the gene visuals of the spider, from design/species/spider.md.

Usage: python3 tools/make_spider_marks.py (from the root of the repository)

The spider texture is 64x32. The parts, from SpiderModel:
- head: 8x8x8 at uv (32, 4). The face is x 40 to 47, y 12 to 19. The eyes
  are where spider_eyes.png draws. The fangs are (41, 18), (41, 19),
  (46, 18), and (46, 19).
- body (body0): 6x6x6 at uv (0, 0).
- abdomen (body1): 10x8x12 at uv (0, 12). On the top, row 0 is the rear end
  and row 11 is next to the body. Column 0 is the right side.
- legs: 16x2x2 at uv (18, 0), all eight on the same pixels. The tip is
  column 0 of the front, the top, and the bottom, and column 15 of the back.
The gene models use the bones head, body (body0), abdomen (body1), and the
eight legs.

Pixel plan for the Weavers. Silk is white, with pale gray shade.
- Web Shot: a white spinneret at the rear end of the abdomen, the gene model
  web_shot_spinneret, and a white patch round it on the rear end.
- Tripwire has silk threads on the legs: a white thread along the top of
  each leg, and two wraps of thread round the leg.
- Silk Armor has a wrap of silk on the body: crossed bands of white and
  pale gray on all the faces of the body.
- Cocoon has silk threads that wrap the abdomen: three bands of silk round
  the abdomen, with loose threads between them.
- Snare Line has silk threads from the fangs: white fangs, a thread between
  them, threads along the jaw on the sides of the head, and threads under
  the front of the head.
- Web Walker: a silk line from the abdomen to the ground behind the spider,
  the gene model web_walker_dragline, and a white line along the middle of
  the back to it.

Pixel plan for the Jumpers.
- Pounce: long, thick back legs, the gene model pounce_legs. It is a larger
  copy of the two hind legs, with the texture of the mob.
- Springlegs has striped joints: yellow and black stripes on the joints of
  the legs.
- Big Eyes has two large front eyes: two black eyes of 3x3 pixels on the
  face, with a gap between them, each with a white glint.
- Dart has pale stripes on the body: two pale stripes along the top and the
  sides of the body.
- Quickbite: long, curved fangs, the gene model quickbite_fangs.
- Bound has thin, long legs: a pale line along the top of each leg from the
  tip to the body, and a dark line along the bottom, so that the legs look
  thinner and longer.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import MODEL_DIR, paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/spider.png"

# The top left pixel and the size of each face of each part.
HEAD = {"top": (40, 4, 8, 8), "bottom": (48, 4, 8, 8), "right": (32, 12, 8, 8), "front": (40, 12, 8, 8),
        "left": (48, 12, 8, 8), "back": (56, 12, 8, 8)}
BODY = {"top": (6, 0, 6, 6), "bottom": (12, 0, 6, 6), "right": (0, 6, 6, 6), "front": (6, 6, 6, 6),
        "left": (12, 6, 6, 6), "back": (18, 6, 6, 6)}
ABDOMEN = {"top": (12, 12, 10, 12), "bottom": (22, 12, 10, 12), "right": (0, 24, 12, 8),
           "front": (12, 24, 10, 8), "left": (22, 24, 12, 8), "back": (34, 24, 10, 8)}
LEG = {"top": (20, 0, 16, 2), "bottom": (36, 0, 16, 2), "right": (18, 2, 2, 2), "front": (20, 2, 16, 2),
       "left": (36, 2, 2, 2), "back": (38, 2, 16, 2)}
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES

EYES = {(39, 14), (39, 15), (40, 14), (40, 15), (41, 12), (42, 13), (42, 15), (42, 16), (43, 15), (43, 16),
        (44, 15), (44, 16), (45, 13), (45, 15), (45, 16), (46, 12), (47, 14), (47, 15), (48, 14), (48, 15)}
FANGS = {(41, 18), (41, 19), (46, 18), (46, 19)}

SILK = ("#F0F0E8", "#C8C8C0", "#9A9A90")


def pixels(part, faces):
    """Return each pixel (x, y, face, col, row) of the faces of a part."""
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                yield x0 + col, y0 + row, face, col, row


def leg_pixels(faces=ALL):
    """Return each pixel (x, y, face, t, row) of the legs. t is the distance
    from the tip, from 0 to 15."""
    for x, y, face, col, row in pixels(LEG, faces):
        t = {"right": 0, "left": 15, "back": 15 - col}.get(face, col)
        yield x, y, face, t, row


def ring(z):
    """Return the pixels of a band round the abdomen, z pixels from the rear
    end."""
    out = []
    for col in range(10):
        out.append((12 + col, 12 + z))
        out.append((22 + col, 23 - z))
    for row in range(8):
        out.append((z, 24 + row))
        out.append((33 - z, 24 + row))
    return out


def web_shot():
    # A white patch round the spinneret on the rear end of the abdomen, and
    # pale threads that run from it onto the top of the rear end.
    patch = Mark(SILK[0])
    shade = Mark(SILK[1])
    patch.put([(38, 27), (39, 27), (38, 28), (39, 28)])
    shade.put([(37, 27), (40, 27), (37, 28), (40, 28), (38, 26), (39, 26), (38, 29), (39, 29)])
    shade.put([(16, 12), (17, 12)])
    return [patch, shade]


def web_shot_model():
    # A white spinneret at the rear end of the abdomen: a wide base on the
    # rear end and a short nozzle behind it, with a gray tip. On the abdomen
    # bone, the rear end is at z 6, and the abdomen is from y -4 to 4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def silk(face, x, y):
        return SILK[1] if face in ("bottom", "right") else SILK[0]

    # The base: 4x3x1 at uv (0, 0). The nozzle: 2x2x2 at uv (12, 0).
    paint_box(im, 0, 0, 4, 3, 1, silk)
    paint_box(im, 12, 0, 2, 2, 2, lambda face, x, y: SILK[2] if face == "back" else silk(face, x, y))
    parts = [
        {"bone": "abdomen", "uv": [0, 0], "from": [-2, -1, 5.5], "size": [4, 3, 1]},
        {"bone": "abdomen", "uv": [12, 0], "from": [-1, -0.5, 6.5], "size": [2, 2, 2],
         "pivot": [0, 0.5, 6.5], "rotation": [-15, 0, 0]},
    ]
    save_model("web_shot_spinneret", im, parts)


def tripwire():
    # Silk threads on the legs: a white thread along the top edge of the
    # front of each leg, with gaps, and two wraps of thread round the leg, 4
    # and 9 pixels from the tip.
    thread = Mark(SILK[0])
    shade = Mark(SILK[1])
    for x, y, face, t, row in leg_pixels():
        if t in (4, 9) and face not in ("right", "left"):
            thread.put((x, y))
        elif face == "front" and row == 0 and 2 <= t <= 13 and t % 4 != 3:
            thread.put((x, y))
        elif face == "top" and row == 1 and 2 <= t <= 13 and t % 4 == 1:
            shade.put((x, y))
    return [thread, shade]


def silk_armor():
    # A wrap of silk on the body: bands of silk that cross on all the faces
    # of the body, white one way and pale gray the other way.
    band = Mark(SILK[0])
    cross = Mark(SILK[1])
    for x, y, face, col, row in pixels(BODY, ALL):
        if (col + row) % 3 == 0:
            band.put((x, y))
        elif (col - row) % 3 == 0:
            cross.put((x, y))
    return [band, cross]


def cocoon():
    # Silk threads that wrap the abdomen: three bands of silk round the
    # abdomen, 2, 6, and 9 pixels from the rear end, and loose threads that
    # run between the bands on the top and the sides.
    band = Mark(SILK[0])
    loose = Mark(SILK[1])
    for z in (2, 6, 9):
        band.put(ring(z))
    for z in (3, 4, 5, 7, 8):
        loose.put([(12 + (z * 3) % 10, 12 + z), (12 + (z * 7 + 4) % 10, 12 + z)])
        loose.put([(z, 24 + (z * 3) % 8), (33 - z, 24 + (z * 5) % 8)])
    return [band, loose]


def snare_line():
    # Silk threads from the fangs: white fangs, a thread between them on the
    # bottom row of the face, threads that run back from the fangs along the
    # bottom row of the sides of the head, and threads under the front of the
    # head.
    thread = Mark(SILK[0])
    shade = Mark(SILK[1])
    thread.put(sorted(FANGS))
    thread.put([(40, 19), (42, 19), (45, 19), (47, 19)])
    shade.put([(43, 19), (44, 19)])
    # The sides of the head: column 7 of the right side and column 0 of the
    # left side are next to the face.
    thread.put([(39, 19), (38, 19), (36, 19), (48, 19), (49, 19), (51, 19)])
    shade.put([(37, 19), (35, 18), (50, 19), (52, 18)])
    # The bottom of the head: row 0 is the front. Columns 1 and 6 are under
    # the fangs.
    thread.put([(49, 4), (54, 4), (49, 5), (54, 5)])
    shade.put([(50, 6), (53, 6), (51, 7), (52, 7)])
    return [thread, shade]


def web_walker():
    # A white line along the middle of the back, from the middle of the back
    # to the rear end, where the silk line of the gene model web_walker_dragline
    # starts.
    line = Mark(SILK[0])
    shade = Mark(SILK[1])
    for row in range(0, 7):
        line.put((16 + row % 2, 12 + row))
    line.put([(38, 24), (39, 24), (38, 25)])
    shade.put([(17, 12 + r) for r in range(0, 7, 2)] + [(16, 13 + r) for r in range(0, 6, 2)])
    return [line, shade]


def web_walker_model():
    # A silk line from the rear end of the abdomen to the ground behind the
    # spider, as the drag line of a spider that drops from a wall. The line
    # starts at the top of the rear end and goes back and down. On the
    # abdomen bone, the rear end is at z 6, and the ground is at y 9.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def silk(face, x, y):
        return SILK[0] if (x + y) % 4 else SILK[1]

    # The line: 1x1x16 at uv (0, 0). It turns 50 degrees down, so that its
    # end is on the ground.
    paint_box(im, 0, 0, 1, 1, 16, silk)
    parts = [
        {"bone": "abdomen", "uv": [0, 0], "from": [-0.5, -3.5, 5], "size": [1, 1, 16], "inflate": -0.3,
         "pivot": [0, -3, 5.5], "rotation": [-50, 0, 0]},
    ]
    save_model("web_walker_dragline", im, parts)


def pounce():
    # The legs are the gene model pounce_legs.
    return []


def pounce_model():
    # Long, thick back legs: a copy of each hind leg, with the texture of the
    # mob, that is larger on each side. It keeps the marks of the other
    # genes. On a leg bone, the right leg is from x -15 to 1, and the left
    # leg, which mirrors it, is from x -1 to 15.
    parts = [
        {"bone": "right_hind_leg", "uv": [18, 0], "from": [-16.5, -1, -1], "size": [16, 2, 2], "inflate": 0.7},
        {"bone": "left_hind_leg", "uv": [18, 0], "from": [0.5, -1, -1], "size": [16, 2, 2], "inflate": 0.7,
         "mirror": True},
    ]
    model = {"mob_texture": True, "texture_width": 64, "texture_height": 32, "parts": parts}
    with open(os.path.join(MODEL_DIR, "pounce_legs.json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


def springlegs():
    # Striped joints: on each joint, 5 and 10 pixels from the tip, a yellow
    # stripe between two black stripes.
    yellow = Mark("#E8C838")
    black = Mark("#141010")
    for x, y, face, t, row in leg_pixels():
        if t in (5, 10):
            yellow.put((x, y))
        elif t in (4, 6, 9, 11):
            black.put((x, y))
    return [yellow, black]


def big_eyes():
    # Two large front eyes: two black eyes of 3x3 pixels on the face, from y
    # 12 to 14, with a gap between them, and a white glint in the middle of
    # each. The red eye pixels of the vanilla eyes still glow on them.
    eye = Mark("#0C0808")
    glint = Mark("#F8F8F8")
    for x0 in (40, 45):
        for dx in range(3):
            for dy in range(3):
                eye.put((x0 + dx, 12 + dy))
    glint.put([(41, 13), (46, 13)])
    return [eye, glint]


def dart():
    # Pale stripes on the body: two pale stripes along the top of the body,
    # and one along each side, from the head to the abdomen.
    stripe = Mark("#C8B898")
    for x, y, face, col, row in pixels(BODY, ("top",)):
        if col in (1, 4):
            stripe.put((x, y))
    for x, y, face, col, row in pixels(BODY, ("right", "left")):
        if row == 2:
            stripe.put((x, y))
    return [stripe]


def quickbite():
    # The fangs are the gene model quickbite_fangs. The vanilla fangs turn
    # pale, as the root of the long fangs.
    root = Mark("#D8D0C0")
    root.put(sorted(FANGS))
    return [root]


def quickbite_model():
    # Long, curved fangs: two fangs under the front of the head, each a pale
    # upper part that hangs down and a lower part that curves in, with a dark
    # tip. On the head bone, the face is at z -8, and the bottom of the head
    # is at y 4. The fangs are under the vanilla fangs, at x -3 and x 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def upper(face, x, y):
        return "#E8E0D0" if face in ("front", "left", "top") else "#B8B0A0"

    def lower(face, x, y):
        return "#3A2A20" if y == 2 or face == "bottom" else upper(face, x, y)

    # The upper parts: 1x3x1 at uv (0, 0). The lower parts: 1x3x1 at uv (4, 0).
    paint_box(im, 0, 0, 1, 3, 1, upper)
    paint_box(im, 4, 0, 1, 3, 1, lower)
    parts = []
    for x, turn, mirror in ((-3, -35, False), (2, 35, True)):
        parts.append({"bone": "head", "uv": [0, 0], "from": [x, 3, -8.5], "size": [1, 3, 1], "mirror": mirror})
        parts.append({"bone": "head", "uv": [4, 0], "from": [x, 5.5, -8.5], "size": [1, 3, 1], "mirror": mirror,
                      "pivot": [x + 0.5, 5.8, -8], "rotation": [-15, 0, turn]})
    save_model("quickbite_fangs", im, parts)


def bound():
    # Thin, long legs: a pale line along the top of each leg from the tip to
    # the body, and a dark line along the bottom row of the front and the
    # back, so that the legs look thinner and longer.
    pale = Mark("#9A8C78")
    dark = Mark("#1A1612")
    for x, y, face, t, row in leg_pixels(("front", "back", "top")):
        if face == "top" and row == 1 or face != "top" and row == 0:
            pale.put((x, y))
        elif face != "top" and row == 1:
            dark.put((x, y))
    return [pale, dark]


GENES = {
    "web_shot": web_shot,
    "tripwire": tripwire,
    "silk_armor": silk_armor,
    "cocoon": cocoon,
    "snare_line": snare_line,
    "web_walker": web_walker,
    "pounce": pounce,
    "springlegs": springlegs,
    "big_eyes": big_eyes,
    "dart": dart,
    "quickbite": quickbite,
    "bound": bound,
}

MODELS = {
    "web_shot": "originofspawns:web_shot_spinneret",
    "web_walker": "originofspawns:web_walker_dragline",
    "pounce": "originofspawns:pounce_legs",
    "quickbite": "originofspawns:quickbite_fangs",
}

MODEL_MAKERS = [web_shot_model, web_walker_model, pounce_model, quickbite_model]


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
    print("pixels that two or more spider genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
