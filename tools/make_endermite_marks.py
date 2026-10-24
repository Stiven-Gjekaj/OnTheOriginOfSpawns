"""Make the gene visuals of the endermite, from design/species/endermite.md.

Usage: python3 tools/make_endermite_marks.py (from the root of the repository)

The endermite texture is 64x32, and it uses the endermite region map. The
parts, from EndermiteModel, from the front to the back:
- segment0, the head: 4x3x2 at uv (0, 0). Its front has the eye at (3, 2) and
  (4, 2). The jaws are the lower row of the front, and the front row of the
  bottom.
- segment1, the body: 6x4x5 at uv (0, 5). The top is the back of the shell.
  On the top, row 0 is at the back and row 4 is next to the head.
- segment2, the tail: 3x3x1 at uv (0, 14).
- segment3, the tip of the tail: 1x2x1 at uv (0, 18).
The gene models use the bones segment0 to segment3.

Pixel plan for the Swarmers.
- Brood Split has a swollen back segment: the back half of the body is pale
  and swollen, with stretched lines.
- Mite Call: two long feelers on the head, the gene model mite_call_feelers.
- Pack Bite has bright purple spots on the body.
- Swarm Shell has shiny shell plates: bands of shell with bright edges on the
  top and the sides of the body.
- Pearl Brood: an ender pearl on the back, the gene model pearl_brood_pearl.
- Hive Bite has red jaws.

Pixel plan for the Tunnelers.
- Stone Hide has gray plates on the body, with dark seams.
- Pebble Skin has small pebbles on the back: gray and brown pebbles on the top
  of the body and of the tail.
- Ambush Bite: two wide jaws, the gene model ambush_bite_jaws.
- Tunnel Sense has pale feelers: two pale feelers that lie back over the top
  of the head.
- Rubble Burst has dust on the shell: tan and gray dust on the top and the
  sides of the body and the tail.
- Warp Burrow has a purple glow on the tail: bright violet tail and tip.

Pixel plan for the Leeches.
- Blood Bite has dark red jaws.
- Hunger Bite has a green belly: the bottom of the body and the low row of its
  sides.
- Xp Leech has green glow spots: bright yellow green spots on the body.
- Weak Bite has gray jaws.
- Life Drain has a long, thin tail: a pale line down the middle of the tail,
  with dark sides, so that it looks thin and long.
- Toxic Bite: two green fangs, the gene model toxic_bite_fangs.

Pixel plan for the Elders.
- Long Life has faded, pale segments: all the segments are pale gray violet.
- Ageless has a white, old shell: the body is bone white, with gray cracks.
- Hard Shell: a thick shell over the back, the gene model hard_shell_shell.
- Old Kin has small eggs on the tail: pale eggs on the tail and the tip.
- Calm Mind has closed, calm eyes: the eye is a closed lid with a dark line.
- Thick Shell has a dark, thick shell: dark bands with ridges on the body.

Pixel plan for the Warpers.
- Mite Blink has purple sparks on the back: magenta sparks on the top of the
  body.
- Warp Bite has purple jaws.
- Kin Mask has a black shell, as the skin of an enderman: black, with dark
  gray pattern, on the body, the tail, and the tip.
- Pearl Leap: an ender pearl at the tip of the tail, the gene model
  pearl_leap_pearl.
- Void Skin has a dark purple shell on the body.
- Ender Scent has a purple mist on the shell: soft violet patches on the body
  and the tail.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/endermite.png"

HEAD = {"top": (2, 0, 4, 2), "bottom": (6, 0, 4, 2), "right": (0, 2, 2, 3), "front": (2, 2, 4, 3),
        "left": (6, 2, 2, 3), "back": (8, 2, 4, 3)}
BODY = {"top": (5, 5, 6, 5), "bottom": (11, 5, 6, 5), "right": (0, 10, 5, 4), "front": (5, 10, 6, 4),
        "left": (11, 10, 5, 4), "back": (16, 10, 6, 4)}
TAIL = {"top": (1, 14, 3, 1), "bottom": (4, 14, 3, 1), "right": (0, 15, 1, 3), "front": (1, 15, 3, 3),
        "left": (4, 15, 1, 3), "back": (5, 15, 3, 3)}
TIP = {"top": (1, 18, 1, 1), "bottom": (2, 18, 1, 1), "right": (0, 19, 1, 2), "front": (1, 19, 1, 2),
       "left": (2, 19, 1, 2), "back": (3, 19, 1, 2)}
SIDES = ("right", "front", "left", "back")
SHELL = ("top", "right", "left", "back")
ALL = ("top", "bottom") + SIDES

VIOLET = ("#B040F0", "#E8A0FF", "#5A2A7A")
PEARL = ("#40C8A0", "#1E6A5A", "#8AF0D0")


def pixels(part, faces):
    """Return each pixel (x, y, face, col, row) of the faces of a part."""
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                yield x0 + col, y0 + row, face, col, row


def jaw_pixels():
    """Return the jaw pixels: the lower row of the front of the head, and the front row of its bottom."""
    out = [(x, 4) for x in range(2, 6)]
    out += [(x, 0) for x in range(6, 10)]
    return out


def jaws(color, dark):
    """Return the marks of jaws of a color, with dark tips at their outer ends."""
    jaw = Mark(color)
    tip = Mark(dark)
    for x, y in jaw_pixels():
        (tip if x in (2, 5) else jaw).put((x, y))
    return [jaw, tip]


def pearl_texture():
    """Return a texture of 16x16 with an ender pearl of 2x2x2 at uv (0, 0)."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 2, 2, 2, lambda face, x, y: PEARL[2] if (x, y) == (0, 0) else
              PEARL[0] if (x + y) % 2 else PEARL[1])
    return im


def brood_split():
    # A swollen back segment: the back half of the body (rows 0 to 2 of the
    # top, the back, and the back columns of the sides) is pale and swollen,
    # with stretched lines.
    pale = Mark("#7A5A9A")
    line = Mark("#A888C8")
    for x, y, face, col, row in pixels(BODY, SHELL):
        back = (face == "top" and row <= 2 or face == "back" or face == "right" and col <= 1
                or face == "left" and col >= 3)
        if back:
            (line if (col + row) % 3 == 0 else pale).put((x, y))
    return [pale, line]


def mite_call():
    # The feelers are the gene model mite_call_feelers.
    return []


def mite_call_model():
    # Long feelers: two thin, dark feelers that rise from the top of the head
    # and lean forward and out, with pale tips. On the head bone (segment0),
    # the head is from y 0 to 3 and from z -1 to 1, and its top is at y 0.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 5, 1, lambda face, x, y: "#C8A8E8" if y == 0 else "#2A1A3A")
    parts = [{"bone": "segment0", "uv": [0, 0], "from": [x, -5, -0.5], "size": [1, 5, 1], "inflate": -0.3,
              "pivot": [x + 0.5, 0, 0], "rotation": [-35, 0, turn]} for x, turn in ((-1.5, -20), (0.5, 20))]
    save_model("mite_call_feelers", im, parts)


def pack_bite():
    # Bright purple spots on the body: spots of bright violet, each with a
    # pale middle, on the top and the sides of the body.
    spot = Mark(VIOLET[0])
    pale = Mark(VIOLET[1])
    for x, y in ((6, 6), (9, 7), (7, 9), (1, 11), (3, 12), (12, 11), (14, 12), (17, 11), (20, 12)):
        spot.put([(x, y), (x + 1, y)])
        pale.put((x, y))
    return [spot, pale]


def swarm_shell():
    # Shiny shell plates: bands of shell across the top of the body, with
    # bright edges between them, and a bright edge with a band under it on the
    # top rows of the sides.
    plate = Mark("#5A3A7A")
    shine = Mark("#C8A8F0")
    for x, y, face, col, row in pixels(BODY, SHELL):
        if face == "top":
            (shine if row % 2 == 0 else plate).put((x, y))
        elif row == 0:
            shine.put((x, y))
        elif row == 1:
            plate.put((x, y))
    return [plate, shine]


def pearl_brood():
    # The pearl is the gene model pearl_brood_pearl.
    return []


def pearl_brood_model():
    # A pearl on the back: an ender pearl that lies on the middle of the top of
    # the body. On the body bone (segment1), the top is at y 0.
    parts = [{"bone": "segment1", "uv": [0, 0], "from": [-1, -1.6, -1], "size": [2, 2, 2]}]
    save_model("pearl_brood_pearl", pearl_texture(), parts)


def hive_bite():
    # Red jaws.
    return jaws("#D82020", "#7A0A0A")


def stone_hide():
    # Gray plates on the body: tiles of gray stone with dark seams on the top
    # and the sides of the body.
    plate = Mark("#7A7A80")
    seam = Mark("#3A3A40")
    for x, y, face, col, row in pixels(BODY, SHELL):
        (seam if col % 3 == 2 or row % 2 == 1 and face != "top" or face == "top" and row == 2 else plate).put((x, y))
    return [plate, seam]


def pebble_skin():
    # Small pebbles on the back: gray and brown pebbles on the top of the body
    # and of the tail.
    gray = Mark("#9A9A9A")
    brown = Mark("#8A6A48")
    for x, y, face, col, row in list(pixels(BODY, ("top",))) + list(pixels(TAIL, ("top",))):
        n = noise100(x, y, 1801)
        if n < 18:
            gray.put((x, y))
        elif n < 32:
            brown.put((x, y))
    return [gray, brown]


def ambush_bite():
    # The jaws are the gene model ambush_bite_jaws.
    return []


def ambush_bite_model():
    # Wide jaws: two long, curved jaws that stick out wide from the front of
    # the head and turn in at their tips, pale with dark points. On the head
    # bone (segment0), the front is at z -1, and the head is from x -2 to 2
    # and from y 0 to 3.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 1, 3, lambda face, x, y: "#2A1A2A" if face == "front" else "#C8B8D8")
    parts = [{"bone": "segment0", "uv": [0, 0], "from": [x, 2, -3.5], "size": [1, 1, 3], "pivot": [x + 0.5, 2.5, -1],
              "rotation": [0, turn, 0]} for x, turn in ((-2.5, -25), (1.5, 25))]
    save_model("ambush_bite_jaws", im, parts)


def tunnel_sense():
    # Pale feelers: two pale feelers that lie back over the top of the head,
    # and their roots on the top row of the front.
    feeler = Mark("#D8C8E8")
    feeler.put([(3, 0), (3, 1), (4, 0), (4, 1), (2, 2), (5, 2)])
    return [feeler]


def rubble_burst():
    # Dust on the shell: tan and gray dust on the top and the sides of the body
    # and of the tail.
    tan = Mark("#B8A078")
    gray = Mark("#8A8480")
    for x, y, face, col, row in list(pixels(BODY, SHELL)) + list(pixels(TAIL, SHELL)):
        n = noise100(x, y, 1803)
        if n < 14:
            tan.put((x, y))
        elif n < 24:
            gray.put((x, y))
    return [tan, gray]


def warp_burrow():
    # A purple glow on the tail: the tail and the tip are bright violet, with
    # pale violet on the tip.
    glow = Mark(VIOLET[0])
    pale = Mark(VIOLET[1])
    for x, y, face, col, row in pixels(TAIL, ALL):
        glow.put((x, y))
    for x, y, face, col, row in pixels(TIP, ALL):
        pale.put((x, y))
    return [glow, pale]


def blood_bite():
    # Dark red jaws.
    return jaws("#8A1010", "#3A0404")


def hunger_bite():
    # A green belly: the bottom of the body is green, and the low row of its
    # sides is a darker green.
    green = Mark("#5AA040")
    dark = Mark("#3A7028")
    for x, y, face, col, row in pixels(BODY, ("bottom",)):
        green.put((x, y))
    for x, y, face, col, row in pixels(BODY, SIDES):
        if row == 3:
            dark.put((x, y))
    return [green, dark]


def xp_leech():
    # Green glow spots: bright yellow green spots, as orbs of experience, on
    # the top and the sides of the body, each with a pale middle.
    spot = Mark("#90E030")
    pale = Mark("#E8FFA0")
    for x, y in ((7, 6), (9, 8), (6, 8), (2, 11), (13, 12), (18, 11), (20, 12)):
        spot.put([(x, y), (x + 1, y), (x, y + 1)])
        pale.put((x, y))
    return [spot, pale]


def weak_bite():
    # Gray jaws.
    return jaws("#8A8A8E", "#4A4A4E")


def life_drain():
    # A long, thin tail: a pale line down the middle of the tail and the tip,
    # with dark sides, so that the tail looks thin and long.
    line = Mark("#B898D8")
    dark = Mark("#1E1028")
    for x, y, face, col, row in pixels(TAIL, ALL):
        if face in ("right", "left") or face in ("front", "back", "top", "bottom") and col in (0, 2):
            dark.put((x, y))
        else:
            line.put((x, y))
    for x, y, face, col, row in pixels(TIP, ALL):
        line.put((x, y))
    return [line, dark]


def toxic_bite():
    # The fangs are the gene model toxic_bite_fangs.
    return []


def toxic_bite_model():
    # Green fangs: two green fangs that hang from the front of the head, each
    # with a pale green tip. On the head bone (segment0), the front is at z -1,
    # and the bottom of the head is at y 3.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 2, 1, lambda face, x, y: "#B8F0A0" if y == 1 else "#40B030")
    parts = [{"bone": "segment0", "uv": [0, 0], "from": [x, 2.5, -1.6], "size": [1, 2, 1], "inflate": -0.15}
             for x in (-1.5, 0.5)]
    save_model("toxic_bite_fangs", im, parts)


def long_life():
    # Faded, pale segments: all the segments are pale gray violet, a little
    # paler on their tops.
    pale = Mark("#8A7E98")
    top = Mark("#A89CB4")
    for part in (HEAD, BODY, TAIL, TIP):
        for x, y, face, col, row in pixels(part, ALL):
            if (x, y) in ((3, 2), (4, 2)):
                continue
            (top if face == "top" else pale).put((x, y))
    return [pale, top]


def ageless():
    # A white, old shell: the top and the sides of the body are bone white,
    # with gray cracks.
    white = Mark("#E8E4DC")
    crack = Mark("#8A8680")
    for x, y, face, col, row in pixels(BODY, SHELL):
        (crack if (col * 2 + row) % 5 == 0 else white).put((x, y))
    return [white, crack]


def hard_shell():
    # The shell is the gene model hard_shell_shell.
    return []


def hard_shell_model():
    # Thick shell plates: a thick shell of dark purple plates over the top and
    # the upper sides of the body, a little larger than the body, with pale
    # seams between the plates. On the body bone (segment1), the body is from
    # x -3 to 3, from y 0 to 4, and from z -2.5 to 2.5.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def shell(face, x, y):
        if face == "bottom":
            return None
        if face == "top":
            return "#8A6AAA" if y in (1, 3) else "#3A2050"
        return "#8A6AAA" if x % 2 == 0 and face in ("right", "left") else "#3A2050"

    # The shell: 7x2x6 at uv (0, 0).
    paint_box(im, 0, 0, 7, 2, 6, shell)
    parts = [{"bone": "segment1", "uv": [0, 0], "from": [-3.5, -0.8, -3], "size": [7, 2, 6]}]
    save_model("hard_shell_shell", im, parts)


def old_kin():
    # Small eggs on the tail: pale eggs on the top and the sides of the tail and
    # on the tip.
    egg = Mark("#E8E0D0")
    shade = Mark("#B8B0A0")
    egg.put([(1, 14), (3, 14), (2, 16), (0, 15), (4, 16), (6, 15), (1, 18), (1, 19)])
    shade.put([(2, 14), (1, 17), (5, 17), (3, 20)])
    return [egg, shade]


def calm_mind():
    # Closed, calm eyes: the eye is a closed lid of the skin color, with a dark
    # line of the shut eye under it.
    lid = Mark("#5A4070")
    line = Mark("#1A0E24")
    lid.put([(3, 2), (4, 2)])
    line.put([(2, 3), (3, 3), (4, 3), (5, 3)])
    return [lid, line]


def thick_shell():
    # A dark, thick shell: the top and the sides of the body are dark, with
    # ridges across the top and down the sides.
    dark = Mark("#1E1028")
    ridge = Mark("#4A3060")
    for x, y, face, col, row in pixels(BODY, SHELL):
        ridge_here = face == "top" and row % 2 == 1 or face != "top" and col % 2 == 1
        (ridge if ridge_here else dark).put((x, y))
    return [dark, ridge]


def mite_blink():
    # Purple sparks on the back: magenta sparks, each a pale point with bright
    # pixels beside it, on the top of the body.
    spark = Mark("#F040FF")
    pale = Mark("#FFD0FF")
    for x, y in ((6, 6), (9, 7), (7, 9)):
        pale.put((x, y))
        spark.put([(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)])
    return [spark, pale]


def warp_bite():
    # Purple jaws.
    return jaws("#B040F0", "#5A1A7A")


def kin_mask():
    # A black shell, as the skin of an enderman: the body, the tail, and the tip
    # are black, with a dark gray pattern.
    black = Mark("#0A0A0A")
    gray = Mark("#1E1E1E")
    for part in (BODY, TAIL, TIP):
        for x, y, face, col, row in pixels(part, ALL):
            (gray if noise100(x, y, 1805) < 30 else black).put((x, y))
    return [black, gray]


def pearl_leap():
    # The pearl is the gene model pearl_leap_pearl.
    return []


def pearl_leap_model():
    # A pearl at the tail: an ender pearl at the tip of the tail, behind it. On
    # the bone of the tip (segment3), the tip is from y 0 to 2 and from z -0.5
    # to 0.5.
    parts = [{"bone": "segment3", "uv": [0, 0], "from": [-1, 0, 0.4], "size": [2, 2, 2]}]
    save_model("pearl_leap_pearl", pearl_texture(), parts)


def void_skin():
    # A dark purple shell: the top and the sides of the body are dark purple,
    # with a darker spot here and there.
    purple = Mark("#2E1248")
    dark = Mark("#1A0A2A")
    for x, y, face, col, row in pixels(BODY, SHELL):
        (dark if noise100(x, y, 1807) < 25 else purple).put((x, y))
    return [purple, dark]


def ender_scent():
    # A purple mist on the shell: soft violet patches on the top and the sides
    # of the body and of the tail.
    mist = Mark("#8A5AB8")
    pale = Mark("#B890E0")
    for x, y, face, col, row in list(pixels(BODY, SHELL)) + list(pixels(TAIL, SHELL)):
        n = noise100(x // 2, y, 1809)
        if n < 18:
            pale.put((x, y))
        elif n < 40:
            mist.put((x, y))
    return [mist, pale]


GENES = {
    "brood_split": brood_split,
    "mite_call": mite_call,
    "pack_bite": pack_bite,
    "swarm_shell": swarm_shell,
    "pearl_brood": pearl_brood,
    "hive_bite": hive_bite,
    "stone_hide": stone_hide,
    "pebble_skin": pebble_skin,
    "ambush_bite": ambush_bite,
    "tunnel_sense": tunnel_sense,
    "rubble_burst": rubble_burst,
    "warp_burrow": warp_burrow,
    "blood_bite": blood_bite,
    "hunger_bite": hunger_bite,
    "xp_leech": xp_leech,
    "weak_bite": weak_bite,
    "life_drain": life_drain,
    "toxic_bite": toxic_bite,
    "long_life": long_life,
    "ageless": ageless,
    "hard_shell": hard_shell,
    "old_kin": old_kin,
    "calm_mind": calm_mind,
    "thick_shell": thick_shell,
    "mite_blink": mite_blink,
    "warp_bite": warp_bite,
    "kin_mask": kin_mask,
    "pearl_leap": pearl_leap,
    "void_skin": void_skin,
    "ender_scent": ender_scent,
}

# Each gene with a model: the maker of the model and its name.
MODEL_GENES = {
    "mite_call": (mite_call_model, "mite_call_feelers"),
    "pearl_brood": (pearl_brood_model, "pearl_brood_pearl"),
    "ambush_bite": (ambush_bite_model, "ambush_bite_jaws"),
    "toxic_bite": (toxic_bite_model, "toxic_bite_fangs"),
    "hard_shell": (hard_shell_model, "hard_shell_shell"),
    "pearl_leap": (pearl_leap_model, "pearl_leap_pearl"),
}

MODELS = {gene: "originofspawns:" + name for gene, (_, name) in MODEL_GENES.items()}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for make, _ in MODEL_GENES.values():
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
    print("pixels that two or more endermite genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
