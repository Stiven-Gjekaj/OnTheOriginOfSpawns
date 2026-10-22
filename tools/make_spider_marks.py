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

Pixel plan for the Fangs. Venom is green.
- Venom Bite has green tips on the fangs: pale green fangs with bright green
  tips, and green drops under them.
- Necrotic has black, rotten fangs: black fangs, and dark rot round the
  mouth, with sick green spots.
- Paralyze has yellow venom glands: yellow sacs on the face and the sides of
  the head, at the sides of the fangs.
- Hunger Venom has a red hourglass mark on the back, in the front half of
  the top of the abdomen, and on the bottom of the abdomen.
- Venom Blood has green veins on the body: a vein that branches on each
  face of the body.
- Spit has a swollen jaw: a pale, swollen band on the bottom rows of the
  face and the sides of the head, and green venom between the fangs.

Pixel plan for the Swarms.
- Clutch: three small spiderlings on the back, the gene model
  clutch_spiderlings.
- Nest Call has a red ring round the eyes: the pixels on the outer edge of
  the group of eyes turn red.
- Hive Mind has a pale ring pattern on the body: a pale ring on each face
  of the body, with a pale dot in the middle.
- Mother: a swollen egg sac under the rear of the abdomen, the gene model
  mother_egg_sac, and pale, stretched skin on the low rows of the sides of
  the abdomen.
- Pack Hunter has banded legs: three pale bands round each leg.
- Silk Signal has stiff hairs on the back: short pale hairs with white tips
  on the top of the abdomen, and hair tips on the top edge of its sides.

Pixel plan for the Trapdoors.
- Digger has dirt in the hairs: clumps of brown dirt on the body and on the
  low rows of the sides of the abdomen.
- Lurking Fangs has a dark hood of hair: the top of the head, and the top
  rows of its sides, its back, and the face, are black hair with a ragged
  lower edge.
- Day Hunter has yellow eyes: the eye pixels are yellow.
- Shadow Skin has black hairs on the body: the body is black with dark gray
  hair, and black hair reaches onto the front of the abdomen and the roots
  of the legs.
- Ceiling Drop has dark chevrons on the back: two black chevrons that point
  to the head, on the rear half of the top of the abdomen, each with a pale
  edge behind it.
- Silent Steps has soft, furred feet: the feet are pale gray fur, paler at
  the tip, with a ring of fur tufts above them.
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

VENOM = ("#5CD040", "#2E8A1E", "#B8F0A0")


def venom_bite():
    # Green tips on the fangs: the upper half of each fang is pale green, the
    # tip is bright green, and a green drop hangs under each fang on the
    # bottom of the head.
    pale = Mark(VENOM[2])
    tip = Mark(VENOM[0])
    pale.put([(41, 18), (46, 18)])
    tip.put([(41, 19), (46, 19), (49, 4), (54, 4)])
    return [pale, tip]


def necrotic():
    # Black, rotten fangs: the fangs are black, the mouth round them is dark
    # with rot, and sick green spots show in the rot.
    black = Mark("#141010")
    rot = Mark("#5A3A5A")
    spot = Mark("#9AB040")
    black.put(sorted(FANGS))
    rot.put([(42, 18), (43, 18), (44, 18), (45, 18), (42, 19), (43, 19), (44, 19), (45, 19), (40, 19), (47, 19),
             (42, 17), (45, 17)])
    spot.put([(43, 17), (44, 19), (40, 18)])
    return [black, rot, spot]


def paralyze():
    # Yellow venom glands: a yellow sac at each side of the fangs, on the
    # face and on the side of the head next to the face, with a pale shine
    # and a dark rim.
    gland = Mark("#E8C830")
    shine = Mark("#F8F0A0")
    rim = Mark("#8A6A18")
    gland.put([(40, 18), (47, 18), (38, 18), (39, 18), (48, 18), (49, 18), (39, 17), (48, 17)])
    shine.put([(40, 17), (47, 17), (38, 17), (49, 17)])
    rim.put([(37, 17), (37, 18), (50, 17), (50, 18), (40, 16), (47, 16)])
    return [gland, shine, rim]


def hunger_venom():
    # A red hourglass mark: two red triangles that meet at their points, in
    # the front half of the top of the abdomen (rows 6 to 11), with a dark
    # red edge. A smaller one is on the bottom of the abdomen.
    red = Mark("#D81818")
    edge = Mark("#8A0E0E")
    shape = {6: (3, 6), 7: (3, 6), 8: (4, 5), 9: (4, 5), 10: (3, 6), 11: (3, 6)}
    for row, (a, b) in shape.items():
        for col in range(a, b + 1):
            (edge if col in (a, b) and a == 3 else red).put((12 + col, 12 + row))
    for row in (4, 5, 6, 7):
        for col in ((4, 5) if row in (5, 6) else (3, 4, 5, 6)):
            red.put((22 + col, 12 + row))
    return [red, edge]


def venom_blood():
    # Green veins on the body: on each face of the body, a vein that comes in
    # at one edge and branches in two, bright green with pale green where it
    # branches.
    vein = Mark(VENOM[0])
    pale = Mark(VENOM[2])
    path = ((0, 1), (1, 1), (2, 2), (3, 2), (4, 1), (5, 0), (3, 3), (3, 4), (4, 5), (2, 5))
    for face in ("top", "right", "front", "left", "back"):
        x0, y0, w, h = BODY[face]
        for col, row in path:
            (pale if (col, row) == (3, 2) else vein).put((x0 + col, y0 + row))
    return [vein, pale]


def spit():
    # A swollen jaw: the bottom three rows of the face and of the sides of
    # the head, next to the face, are a pale, swollen band, brighter on the
    # top row. Green venom is between the fangs.
    jaw = Mark("#7A6A58")
    bright = Mark("#A09078")
    venom = Mark(VENOM[0])
    for x in range(40, 48):
        if (x, 18) not in FANGS:
            jaw.put([(x, 18), (x, 19)])
        bright.put((x, 17))
    jaw.put([(36, 18), (37, 18), (38, 18), (39, 18), (48, 18), (49, 18), (50, 18), (51, 18)])
    jaw.put([(37, 19), (38, 19), (39, 19), (48, 19), (49, 19), (50, 19)])
    bright.put([(37, 17), (38, 17), (49, 17), (50, 17)])
    venom.put([(43, 19), (44, 19)])
    return [jaw, bright, venom]


def clutch():
    # The spiderlings are the gene model clutch_spiderlings.
    return []


def clutch_model():
    # Three small spiderlings that ride on the back: each has a body of 2x1x2
    # with a head of 1x1x1 in front, two thin bars of legs across it, and a
    # red eye. They sit on the top of the abdomen and look in different
    # directions. On the abdomen bone, the top is at y -4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def body(face, x, y):
        return "#9A8468" if face == "top" else "#6A5A46"

    def head(face, x, y):
        return "#C81818" if face == "front" else "#5A4A3A"

    # The body: 2x1x2 at uv (0, 0). The head: 1x1x1 at uv (8, 0). The legs:
    # 4x1x1 at uv (0, 4).
    paint_box(im, 0, 0, 2, 1, 2, body)
    paint_box(im, 8, 0, 1, 1, 1, head)
    paint_box(im, 0, 4, 4, 1, 1, lambda face, x, y: "#3A3028")
    parts = []
    for x, z, turn in ((-2.5, -2, 20), (2, 1, -30), (-1, 4, 160)):
        pivot = [x, -4.5, z]
        for uv, f, size, inflate in (([0, 0], [x - 1, -5, z - 1], [2, 1, 2], 0),
                                     ([8, 0], [x - 0.5, -5, z - 2], [1, 1, 1], 0),
                                     ([0, 4], [x - 2, -4.8, z - 0.9], [4, 1, 1], -0.3),
                                     ([0, 4], [x - 2, -4.8, z - 0.1], [4, 1, 1], -0.3)):
            parts.append({"bone": "abdomen", "uv": uv, "from": f, "size": size, "inflate": inflate,
                          "pivot": pivot, "rotation": [0, turn, 0]})
    save_model("clutch_spiderlings", im, parts)


def nest_call():
    # A red ring round the eyes: each pixel of the head next to an eye, which
    # is not an eye or a fang, turns red. Only the outer edge of the group of
    # eyes gets the ring: the top and the bottom rows, the edges of the face,
    # and the sides of the head. The pixels between the eyes stay dark, so
    # that the eyes stand out.
    ring = Mark("#E83030")
    for x, y, face, col, row in pixels(HEAD, ("right", "front", "left")):
        if (x, y) in EYES or (x, y) in FANGS:
            continue
        outer = y <= 12 or y >= 17 or x <= 40 or x >= 47
        if outer and any((x + dx, y + dy) in EYES for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            ring.put((x, y))
    return [ring]


def hive_mind():
    # A pale ring pattern on the body: on each face of the body, a pale ring
    # of 4x4 pixels, with a pale dot in the middle.
    ring = Mark("#D8C8A8")
    for x, y, face, col, row in pixels(BODY, ("top", "right", "front", "left", "back")):
        if 1 <= col <= 4 and 1 <= row <= 4 and (col in (1, 4) or row in (1, 4)):
            if (col, row) not in ((1, 1), (4, 1), (1, 4), (4, 4)):
                ring.put((x, y))
        elif (col, row) in ((2, 2), (3, 3)):
            ring.put((x, y))
    return [ring]


def mother():
    # Pale, stretched skin: the two low rows of the sides of the abdomen are
    # pale, with paler lines where the egg sac pulls the skin.
    skin = Mark("#7A6A5A")
    line = Mark("#9A8A76")
    for x, y, face, col, row in pixels(ABDOMEN, ("right", "left", "back")):
        if row == 7:
            skin.put((x, y))
        elif row == 6 and col % 3 == 1:
            line.put((x, y))
    return [skin, line]


def mother_model():
    # A swollen egg sac under the rear of the abdomen: a round sac of pale
    # silk, full of eggs that show as pale yellow bumps, with a smaller sac
    # behind it. It hangs from the bottom of the abdomen. On the abdomen
    # bone, the bottom is at y 4, and the rear end is at z 6.
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def sac(face, x, y):
        if (x * 3 + y * 2 + len(face)) % 5 == 0:
            return "#D8C88A"
        return "#C8C0A8" if face in ("bottom", "back", "left") else "#E8E2CE"

    # The sac: 6x5x6 at uv (0, 0). The small sac: 4x3x3 at uv (0, 11).
    paint_box(im, 0, 0, 6, 5, 6, sac)
    paint_box(im, 0, 11, 4, 3, 3, sac)
    parts = [
        {"bone": "abdomen", "uv": [0, 0], "from": [-3, 2.5, -0.5], "size": [6, 5, 6], "inflate": 0.3},
        {"bone": "abdomen", "uv": [0, 11], "from": [-2, 3, 5], "size": [4, 3, 3]},
    ]
    save_model("mother_egg_sac", im, parts)


def pack_hunter():
    # Banded legs: three pale bands round each leg, 2 and 3, 7 and 8, and 12
    # and 13 pixels from the tip. They are between the joints.
    band = Mark("#C8A878")
    for x, y, face, t, row in leg_pixels():
        if t in (2, 3, 7, 8, 12, 13):
            band.put((x, y))
    return [band]


def silk_signal():
    # Stiff hairs on the back: short hairs of 2 pixels on the top of the
    # abdomen, pale with a white tip, in rows across the back, and hair tips
    # on the top row of the sides of the abdomen.
    hair = Mark("#A89880")
    tip = Mark("#E8E0D0")
    for x, y, face, col, row in pixels(ABDOMEN, ("top",)):
        if row % 2 == 0 and row < 11 and (col + 3 * (row // 2)) % 4 == 0:
            tip.put((x, y))
            hair.put((x, y + 1))
    for x, y, face, col, row in pixels(ABDOMEN, ("right", "left", "back")):
        if row == 0 and col % 3 == 1:
            tip.put((x, y))
    return [hair, tip]


def digger():
    # Dirt in the hairs: clumps of brown dirt on all the faces of the body,
    # and on the low rows of the sides and the rear end of the abdomen, where
    # the soil sticks when it digs.
    dirt = Mark("#6A4A2A")
    clod = Mark("#9A7448")
    for x, y, face, col, row in pixels(BODY, ALL):
        n = noise100(x, y, 517)
        if n < 22:
            clod.put((x, y))
        elif n < 50:
            dirt.put((x, y))
    for x, y, face, col, row in pixels(ABDOMEN, ("right", "left", "back")):
        n = noise100(x, y, 523)
        if row >= 5 and n < 55 or row == 4 and n < 20:
            (clod if n < 18 else dirt).put((x, y))
    return [dirt, clod]


def lurking_fangs():
    # A dark hood of hair: the top of the head is black hair, and the hood
    # comes down over the sides and the back of the head, with a ragged
    # lower edge, and over the top row of the face, but not the eyes.
    hood = Mark("#1A1612")
    hair = Mark("#2E2620")
    for x, y, face, col, row in pixels(HEAD, ("top",)):
        (hair if (col + 2 * row) % 5 == 0 else hood).put((x, y))
    for x, y, face, col, row in pixels(HEAD, ("right", "left", "back")):
        if row < 2 or row == 2 and col % 2 == 0 or row == 3 and col % 4 == 1:
            if (x, y) not in EYES:
                (hair if (col + row) % 4 == 0 else hood).put((x, y))
    hood.put([(x, 12) for x in range(40, 48) if (x, 12) not in EYES])
    return [hood, hair]


def day_hunter():
    # Yellow eyes: the eye pixels are yellow, and pale yellow on the two
    # middle pairs of the front.
    yellow = Mark("#F0D020")
    pale = Mark("#F8F0A0")
    for p in sorted(EYES):
        (pale if p in ((43, 15), (44, 15)) else yellow).put(p)
    return [yellow, pale]


def shadow_skin():
    # Black hairs on the body: the body is black, with strokes of dark gray
    # hair. Black hair also reaches onto the front of the abdomen and the
    # first rows of its top and sides, and onto the roots of the legs.
    black = Mark("#121010")
    hair = Mark("#2A2622")
    for x, y, face, col, row in pixels(BODY, ALL):
        (hair if (col + row) % 3 == 0 else black).put((x, y))
    for x, y, face, col, row in pixels(ABDOMEN, ("front",)):
        black.put((x, y))
    for x, y, face, col, row in pixels(ABDOMEN, ("top",)):
        if row == 11 or row == 10 and col % 2 == 0:
            black.put((x, y))
    for x, y, face, col, row in pixels(ABDOMEN, ("right", "left")):
        near = col if face == "left" else 11 - col
        if near == 0 or near == 1 and row % 2 == 0:
            black.put((x, y))
    for x, y, face, t, row in leg_pixels():
        if t >= 14:
            black.put((x, y))
    return [black, hair]


def ceiling_drop():
    # Dark chevrons on the back: two black chevrons that point to the head,
    # on the rear half of the top of the abdomen, from row 0 to row 5. A pale
    # edge runs behind each chevron, so that it shows on the dark back.
    dark = Mark("#141010")
    edge = Mark("#B8AA96")
    for base in (1, 3):
        for k, cols in enumerate(((1, 8), (2, 7), (3, 6), (4, 5))):
            row = base - 1 + k
            dark.put([(12 + c, 12 + row) for c in cols])
            if row >= 1:
                edge.put([(12 + c, 11 + row) for c in cols])
    return [edge, dark]


def silent_steps():
    # Soft, furred feet: the 3 pixels at the tip of each leg are pale gray
    # fur, paler at the tip and on its end, and a ring of fur tufts sits
    # above them, 3 pixels from the tip.
    fur = Mark("#B0A496")
    tip = Mark("#D8D0C4")
    tuft = Mark("#8A7E70")
    for x, y, face, t, row in leg_pixels():
        if t == 0:
            tip.put((x, y))
        elif t <= 2:
            fur.put((x, y))
        elif t == 3 and (row + len(face)) % 2 == 0:
            tuft.put((x, y))
    return [fur, tip, tuft]



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
    "venom_bite": venom_bite,
    "necrotic": necrotic,
    "paralyze": paralyze,
    "hunger_venom": hunger_venom,
    "venom_blood": venom_blood,
    "spit": spit,
    "clutch": clutch,
    "nest_call": nest_call,
    "hive_mind": hive_mind,
    "mother": mother,
    "pack_hunter": pack_hunter,
    "silk_signal": silk_signal,
    "digger": digger,
    "lurking_fangs": lurking_fangs,
    "day_hunter": day_hunter,
    "shadow_skin": shadow_skin,
    "ceiling_drop": ceiling_drop,
    "silent_steps": silent_steps,
}

MODELS = {
    "web_shot": "originofspawns:web_shot_spinneret",
    "web_walker": "originofspawns:web_walker_dragline",
    "pounce": "originofspawns:pounce_legs",
    "quickbite": "originofspawns:quickbite_fangs",
    "clutch": "originofspawns:clutch_spiderlings",
    "mother": "originofspawns:mother_egg_sac",
}

MODEL_MAKERS = [web_shot_model, web_walker_model, pounce_model, quickbite_model, clutch_model, mother_model]


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
