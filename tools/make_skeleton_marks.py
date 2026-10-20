"""Make the marks and the gene models of the skeleton genes.

Usage: python3 tools/make_skeleton_marks.py

The skeleton texture is 64x32. Its head and body have the humanoid layout,
but its arms and legs are 2x12x2, so each side face of a limb is 2 pixels
wide. The left limbs mirror the right limbs. Each visual names the skeleton
region map (textures/region/skeleton.png), from make_skeleton_region_map.py.
This script uses the helpers of make_gene_marks.py and make_husk_marks.py.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- gene_visuals/<gene>.json, with one pattern layer for each pattern.
- The gene models in MODELS, with their textures.

The script prints the pixels that two skeleton genes share.

Texture plan of the skeleton:
- The face is x 8 to 15, y 8 to 15. The eye sockets are x 9, 10, 13, and 14
  of y 12. The jaw is y 14.
- The body: the front is x 20 to 27, the back x 32 to 39, the sides x 16 to
  19 and x 28 to 31, from y 20 to 31. The ribs are y 22 and y 24 on all
  faces, the shoulders y 20, and the pelvis y 30 and 31. The spine is x 35
  and 36 of the back.
- An arm is at uv (40, 16), a leg at uv (0, 16). The side faces are, from
  left to right: outer, front, inner, back, 2 pixels each, from y 18 to 29.

Pixel plan for the Marksmen.
- Eagle Eye has the eye sockets, and the pixels over and under them.
- Deadeye has a crack over the right eye: face x 9 to 11, y 8 to 10, and
  the top of the head over it.
- Longdraw: a leather bracer on the left forearm, the gene model
  longdraw_bracer.
- Quickdraw has the fingertips: arm rows 10 and 11, and the bottom of the
  arm.
- Shieldbreaker has a notch in each rib, at y 22 and y 24.
- High Ground: claws of bone on the feet, the gene model high_ground_claws.

Pixel plan for the Rattlers.
- Skitter has cracks on the legs, rows 1 to 8, and a light shade on the
  outer and the inner faces of the legs.
- Backpedal: a bent spine that bows out of the back, the gene model
  backpedal_spine. The real spine is darker (order 0, the spine region).
- Bonedash has a crack on the pelvis: x 24 to 29 and x 20 and 21, y 30
  and 31.
- Hollow Bones has holes on the arms (rows 1 to 4, 7, and 8) and on the
  back of the head.
- Lightfoot has rags round the feet: leg rows 9 to 11, and the bottom of
  the leg.
- Fleetbone: a loose, broken jaw, the gene model fleetbone_jaw, and the
  dark mouth, y 14.

Pixel plan for the Ossified.
- Thickbone: thicker arms and legs with a knob at each elbow and knee, the
  gene model thickbone_bones. It uses the texture of the mob.
- Calcified has a chalk crust on all the bone, under the marks (order -1).
- Marrowmend has the joints: limb row 0 and the top of each limb, and the
  front and the back faces of row 5.
- Reassemble: loops of wire under the elbows, under the knees, and round
  the neck, the gene model reassemble_wire.
- Bonewall: a second rib cage over the chest, the gene model bonewall_ribs.
- Holy Ward has runes on the brow: x 12 to 15, y 8 to 10, and x 8, y 9
  and 10.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import ASSETS, MARK_DIR, VISUAL_DIR, Mark, noise100, touched, write
from make_husk_marks import paint_box, save_model

REGION_MAP = "originofspawns:textures/region/skeleton.png"

# The side faces of a limb, as the x offset from the start of the limb UV.
OUTER, FRONT, INNER, BACK = 0, 2, 4, 6
SIDES = (OUTER, FRONT, INNER, BACK)
ARM, LEG = (40, 16), (0, 16)

BONE = ("#E8E4D8", "#C8C2B0", "#8E887A")


def arm(face, col, row):
    """Return the texture pixel of a pixel on a side face of the arm."""
    return (ARM[0] + face + col, ARM[1] + 2 + row)


def leg(face, col, row):
    return (LEG[0] + face + col, LEG[1] + 2 + row)


def eagle_eye():
    # A bright ring in each eye socket: the socket glows amber, and a thin
    # pale gold ring shows over and under it.
    ring = Mark("#FFD860")
    core = Mark("#B07A20")
    halo = Mark("#F2D890", alpha=150)
    for outer, inner in ((9, 10), (14, 13)):
        ring.put((outer, 12))
        core.put((inner, 12))
        halo.put((outer, 11)).put((inner, 11)).put((outer, 13)).put((inner, 13))
    return [ring, core, halo]


def deadeye():
    # A crack over the right eye (x 9 and 10 of the face): a dark, jagged
    # line from the top of the head down to the brow, with a pale chipped
    # edge. It stops at y 10, over the Eagle Eye ring.
    crack = Mark("#3A3834")
    chip = Mark("#F6F2E8")
    for p in ((10, 7), (10, 8), (10, 9), (9, 10)):
        crack.put(p)
    crack.put((11, 6), 220)
    chip.put((11, 8), 230).put((9, 9), 230).put((10, 10), 210)
    return [crack, chip]


LEATHER = ("#7A4E2C", "#5E3A20", "#B08A5A")


def longdraw():
    # The bracer is the gene model longdraw_bracer.
    return []


def longdraw_model():
    # A leather bracer round the left forearm, arm rows 6 to 9, with two
    # straps and a light stitch. On an arm bone, the arm is from x -1 to 1
    # and from y -2, so row 6 is at y 4.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def bracer(face, x, y):
        if face in ("top", "bottom"):
            return LEATHER[1]
        if y in (0, 3):
            return LEATHER[1]
        return LEATHER[2] if (x + y) % 3 == 0 else LEATHER[0]

    # The bracer: 2x4x2 at uv (0, 0).
    paint_box(im, 0, 0, 2, 4, 2, bracer)
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [-1, 4, -1], "size": [2, 4, 2], "inflate": 0.3}]
    save_model("longdraw_bracer", im, parts)


def quickdraw():
    # Worn, white fingertips: the tips are a clean white, with a gray line
    # of wear above them.
    tip = Mark("#FFFFFF")
    wear = Mark("#5A564C")
    for face in SIDES:
        for col in (0, 1):
            tip.put(arm(face, col, 11)).put(arm(face, col, 10), 240)
            wear.put(arm(face, col, 9), 220 if (face + col) % 2 else 255)
    for x in (44, 45):
        for y in (16, 17):
            tip.put((x, y))
    return [tip, wear]


def shieldbreaker():
    # A notch cut in each rib: a dark V in the ribs at y 22 and y 24, on the
    # front, the sides, and the back (but the spine), with a light cut edge.
    notch = Mark("#2E2C28")
    cut = Mark("#F4F0E6")
    for x in (21, 26, 17, 30, 33, 38):
        notch.put((x, 22))
    for x in (20, 27, 18, 29, 34, 37):
        notch.put((x, 24))
    for x in (22, 25, 16, 31):
        cut.put((x, 22), 230)
    return [notch, cut]


CLAW = ("#E2DAC2", "#B8AE94", "#5A5446")


def high_ground():
    # The claws are the gene model high_ground_claws.
    return []


def high_ground_model():
    # Two claws of bone on the front of each foot, that curve down to the
    # ground, with dark tips. On a leg bone, the leg is from x -1 to 1 and
    # from z -1 to 1, and the foot ends at y 12.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def claw(face, x, y):
        if face == "front":
            return CLAW[2]
        return CLAW[0] if face == "top" else CLAW[1]

    # A claw: 1x1x3 at uv (0, 0).
    paint_box(im, 0, 0, 1, 1, 3, claw)
    parts = []
    for bone in ("right_leg", "left_leg"):
        for x in (-1, 0):
            parts.append({"bone": bone, "uv": [0, 0], "from": [x, 11, -3.2], "size": [1, 1, 3],
                          "pivot": [x + 0.5, 11.5, -1], "rotation": [-20, 0, 0]})
    save_model("high_ground_claws", im, parts)


def skitter():
    # Thin, cracked leg bones: dark cracks that run down the legs at a
    # slant, with pale chips beside them, on leg rows 1 to 8. The outer and
    # the inner faces of the legs are a little darker, so the legs look
    # thinner from the front.
    crack = Mark("#3A3834")
    chip = Mark("#F4F0E6")
    shade = Mark("#5A564C", alpha=90)
    for face in SIDES:
        for col, row in ((0, 1), (1, 2), (1, 3), (0, 6), (1, 7)):
            if (face // 2 + row) % 2 == 0 or face == FRONT:
                crack.put(leg(face, col, row))
        chip.put(leg(face, 1, 1), 230).put(leg(face, 0, 7), 230)
    for face in (OUTER, INNER):
        for col in (0, 1):
            for row in (4, 5, 8):
                shade.put(leg(face, col, row))
    return [crack, chip, shade]


def backpedal():
    # The bent back is the gene model backpedal_spine. The real spine is
    # darker, so the bent spine stands out in front of it.
    shade = Mark("#4A4840", alpha=170, regions=["spine"])
    shade.fill()
    return [shade]


def backpedal_model():
    # A bent, curved spine that bows out of the back: a column of vertebrae
    # down the middle of the back. The top and the bottom vertebrae lie on
    # the back, and the middle ones stand out further, so the back looks
    # hunched. The vertebrae overlap, so the column has no gaps. On the
    # body bone, the back is at z 2, and the middle 2 columns of the back
    # are at x -1 to 1.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def vertebra(face, x, y):
        # A dark line between the vertebrae, on the lowest row.
        if face in ("right", "left", "back") and y == 1:
            return BONE[2]
        return BONE[0] if face in ("top", "back") else BONE[1]

    # A vertebra: 2x2x1 at uv (0, 0).
    paint_box(im, 0, 0, 2, 2, 1, vertebra)
    parts = []
    for y, z in ((0, 2.0), (2, 2.4), (4, 2.8), (6, 2.9), (8, 2.5), (10, 2.0)):
        parts.append({"bone": "body", "uv": [0, 0], "from": [-1, y, z], "size": [2, 2, 1]})
    save_model("backpedal_spine", im, parts)


def bonedash():
    # A cracked hip: a dark crack across the pelvis on the left hip, from
    # the front to the side, with pale chips, and a chip gone from the
    # right hip.
    crack = Mark("#2E2C28")
    chip = Mark("#F4F0E6")
    for p in ((26, 30), (27, 31), (25, 31), (28, 30), (29, 31)):
        crack.put(p)
    chip.put((27, 30), 230).put((24, 30), 210)
    crack.put((20, 31), 220)
    chip.put((21, 31), 220)
    return [crack, chip]


def hollow_bones():
    # Small holes in the bones, as in bird bones: dark round holes with a
    # pale rim under each, on the arms (rows 1 to 4 and 7 and 8) and on the
    # back of the skull.
    hole = Mark("#2A2824")
    rim = Mark("#F0ECE2")
    for face in SIDES:
        for col, row in ((0, 1), (1, 3), (0, 7)):
            c = (col + face // 2) % 2
            hole.put(arm(face, c, row))
            rim.put(arm(face, c, row + 1), 225)
    for x, y in ((25, 9), (29, 10), (27, 12), (30, 13), (25, 14)):
        hole.put((x, y))
        rim.put((x, y + 1) if y < 15 else (x + 1, y), 225)
    return [hole, rim]


RAG = ("#9A8A6E", "#7E6E54", "#C2B494")


def lightfoot():
    # Feet wrapped in rags: strips of gray-brown cloth round the feet (leg
    # rows 9 to 11) and under them, with dark lines between the turns and a
    # loose end at the front.
    rag = Mark(RAG[0])
    light = Mark(RAG[2])
    line = Mark("#4A3E2E")
    for face in SIDES:
        for col in (0, 1):
            for row in (9, 10, 11):
                if (row + col + face // 2) % 3 == 0:
                    line.put(leg(face, col, row), 230)
                elif (col + row) % 2:
                    light.put(leg(face, col, row))
                else:
                    rag.put(leg(face, col, row))
    for x in (4, 5):
        for y in (16, 17):
            rag.put((x, y), 235)
    return [rag, light, line]


def fleetbone():
    # The loose jaw is the gene model fleetbone_jaw. The mouth on the face
    # is dark, as the jaw hangs open under it.
    gap = Mark("#1E1C1A")
    for x in range(9, 15):
        gap.put((x, 14))
    return [gap]


def fleetbone_model():
    # A broken jaw that hangs loose under the face: it hangs open, and one
    # side hangs lower than the other, with a row of teeth on its top. On
    # the head bone, the face is at z -4 and the bottom of the head at y 0.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def jaw(face, x, y):
        if face == "top":
            return "#F6F2E8" if x % 2 == 0 else "#2E2C28"
        return BONE[1] if face != "front" else BONE[0]

    # The jaw: 6x1x2 at uv (0, 0).
    paint_box(im, 0, 0, 6, 1, 2, jaw)
    parts = [{"bone": "head", "uv": [0, 0], "from": [-3, -0.5, -4.3], "size": [6, 1, 2],
              "pivot": [2.5, -0.5, -3.3], "rotation": [-25, 0, -15]}]
    save_model("fleetbone_jaw", im, parts)


def thickbone():
    # The thick bones are the gene model thickbone_bones.
    return []


def thickbone_model():
    # Thick, knobbed bones: each arm and leg is thicker (a shell 0.35 pixel
    # out, with the texture of the mob, so the marks of other genes show on
    # it), with a round knob of bone at each elbow and each knee. The knobs
    # also use the texture of the mob: their box UV at (16, 8) takes the
    # bone of the left side and the back of the head, where each pixel is
    # drawn. On an arm bone, the elbow (rows 5 and 6) is at y 3 to 5. On a
    # leg bone, the knee is at y 5 to 7.
    model = {"mob_texture": True, "parts": []}
    parts = model["parts"]
    for bone, mirror in (("right_arm", False), ("left_arm", True)):
        part = {"bone": bone, "uv": [40, 16], "from": [-1, -2, -1], "size": [2, 12, 2], "inflate": 0.35}
        knob = {"bone": bone, "uv": [16, 8], "from": [-1.5, 3, -1.5], "size": [3, 2, 3]}
        if mirror:
            part["mirror"] = True
        parts += [part, knob]
    for bone, mirror in (("right_leg", False), ("left_leg", True)):
        part = {"bone": bone, "uv": [0, 16], "from": [-1, 0, -1], "size": [2, 12, 2], "inflate": 0.35}
        knob = {"bone": bone, "uv": [16, 8], "from": [-1.5, 5, -1.5], "size": [3, 2, 3]}
        if mirror:
            part["mirror"] = True
        parts += [part, knob]
    path = os.path.join(ASSETS, "gene_models", "thickbone_bones.json")
    with open(path, "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


def calcified():
    # A crust of chalk-white mineral on all the bone: rough white patches
    # with light gray specks. They go under the marks of other genes
    # (order -1).
    crust = Mark("#FAF8F0", alpha=210, order=-1, materials=["bone"], size=16)
    speck = Mark("#B8B4A8", alpha=180, order=-1, materials=["bone"], size=16)
    for y in range(16):
        for x in range(16):
            n = noise100(x // 2, y // 2, 141)
            if n < 30:
                crust.put((x, y), 255 if noise100(x, y, 142) < 70 else 230)
            elif n < 36:
                speck.put((x, y))
    return [crust, speck]


def marrowmend():
    # Red marrow at each joint: dark red marrow and bright red drops at the
    # shoulders and the hips (row 0 and the top of each limb) and at the
    # elbows and the knees (row 5), on the front and the back faces.
    marrow = Mark("#8A1E1E")
    bright = Mark("#D2443A")
    for at in (arm, leg):
        for face in SIDES:
            marrow.put(at(face, 0, 0)).put(at(face, 1, 0), 230)
        for face in (FRONT, BACK):
            marrow.put(at(face, 0, 5))
            bright.put(at(face, 1, 5))
    for u, v in (ARM, LEG):
        bright.put((u + 2, v)).put((u + 3, v + 1))
        marrow.put((u + 3, v)).put((u + 2, v + 1))
    return [marrow, bright]


WIRE = ("#9AA0A8", "#6A7078", "#D8DEE4")


def reassemble():
    # The wire is the gene model reassemble_wire.
    return []


def reassemble_model():
    # Wire that ties the joints together: twisted loops of wire round each
    # arm under the elbow (row 7) and round each leg under the knee (row 7),
    # and round the neck, on the top of the body. The loops are outside the
    # Thickbone shell (0.35 pixel), and under its knobs.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def wire(face, x, y):
        return WIRE[2] if (x + y) % 3 == 0 else WIRE[(x + y) % 2]

    # A limb loop: 2x1x2 at uv (0, 0). The neck loop: 8x1x4 at uv (0, 4).
    paint_box(im, 0, 0, 2, 1, 2, wire)
    paint_box(im, 0, 4, 8, 1, 4, wire)
    parts = [{"bone": "body", "uv": [0, 4], "from": [-4, 0, -2], "size": [8, 1, 4], "inflate": 0.3}]
    for bone in ("right_arm", "left_arm"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1, 5, -1], "size": [2, 1, 2], "inflate": 0.45})
    for bone in ("right_leg", "left_leg"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1, 7, -1], "size": [2, 1, 2], "inflate": 0.45})
    save_model("reassemble_wire", im, parts)


def bonewall():
    # The rib cage is the gene model bonewall_ribs.
    return []


def bonewall_model():
    # A second rib cage that grows over the chest: four thick bars of bone
    # across the front of the body, that bend round to the sides, and a
    # breastbone down the middle. It lies on the front of the body. On the
    # body bone, the chest is at z -2, and the body is from x -4 to 4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def bar(face, x, y):
        return BONE[0] if face in ("top", "front") else BONE[2] if face == "bottom" else BONE[1]

    # A bar: 9x1x1 at uv (0, 0). A side of a bar: 1x1x3 at uv (0, 4). The
    # breastbone: 2x8x1 at uv (16, 4).
    paint_box(im, 0, 0, 9, 1, 1, bar)
    paint_box(im, 0, 4, 1, 1, 3, bar)
    paint_box(im, 16, 4, 2, 8, 1, lambda face, x, y: BONE[2] if y % 2 else BONE[0])
    parts = [{"bone": "body", "uv": [16, 4], "from": [-1, 1, -3.3], "size": [2, 8, 1]}]
    for y in (2, 4, 6, 8):
        parts.append({"bone": "body", "uv": [0, 0], "from": [-4.5, y, -3], "size": [9, 1, 1]})
        for x in (-4.5, 3.5):
            parts.append({"bone": "body", "uv": [0, 4], "from": [x, y, -2], "size": [1, 1, 3]})
    save_model("bonewall_ribs", im, parts)


def holy_ward():
    # Carved runes on the brow: two glyphs of dark carved lines on the left
    # side of the forehead (an L at x 12 and 13, and a hook at x 14 and 15,
    # y 8 to 10), and a short line at the right temple (x 8). Bone shows
    # between the lines, so each glyph reads. The cuts keep away from the
    # Deadeye crack (x 9 to 11) and the Eagle Eye ring (y 11).
    cut = Mark("#3E3A32")
    for p in ((12, 8), (12, 9), (12, 10), (13, 10), (15, 8), (14, 9), (15, 10), (8, 9), (8, 10)):
        cut.put(p)
    return [cut]


GENES = {
    "eagle_eye": eagle_eye,
    "deadeye": deadeye,
    "longdraw": longdraw,
    "quickdraw": quickdraw,
    "shieldbreaker": shieldbreaker,
    "high_ground": high_ground,
    "skitter": skitter,
    "backpedal": backpedal,
    "bonedash": bonedash,
    "hollow_bones": hollow_bones,
    "lightfoot": lightfoot,
    "fleetbone": fleetbone,
    "thickbone": thickbone,
    "calcified": calcified,
    "marrowmend": marrowmend,
    "reassemble": reassemble,
    "bonewall": bonewall,
    "holy_ward": holy_ward,
}

MODELS = {
    "longdraw": "originofspawns:longdraw_bracer",
    "high_ground": "originofspawns:high_ground_claws",
    "backpedal": "originofspawns:backpedal_spine",
    "fleetbone": "originofspawns:fleetbone_jaw",
    "thickbone": "originofspawns:thickbone_bones",
    "reassemble": "originofspawns:reassemble_wire",
    "bonewall": "originofspawns:bonewall_ribs",
}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    longdraw_model()
    high_ground_model()
    backpedal_model()
    fleetbone_model()
    thickbone_model()
    reassemble_model()
    bonewall_model()
    for gene, make in GENES.items():
        marks = make()
        write(gene, marks, MODELS, REGION_MAP)
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
    print("pixels that two or more skeleton genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
