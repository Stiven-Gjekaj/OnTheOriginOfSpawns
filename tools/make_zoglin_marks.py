"""Make the gene visuals of the zoglin, from design/species/zoglin.md.

Usage: python3 tools/make_zoglin_marks.py (from the root of the repository)

The zoglin uses the hoglin region map, and the marks go on the vanilla zoglin
texture. make_hoglin_marks.py gives the parts of the layout. The zoglin
texture has no mane, so no mark goes on the mane.
The gene models use the bones body, head, right_horn, and left_horn.

Pixel plan for the Gorers.
- Deep Gore: long, broken tusks, the gene model deep_gore_tusks.
- High Toss has tusks that point up: dark bases and bright, long tips.
- Bleed Out has red, wet tusks with white glints.
- Hook Tusk: hooked, bone tusks, the gene model hook_tusk_hooks.
- Golem Gore has cracked tusks: gray tusks with black cracks.
- Grim Charge has a cracked, flat brow: a bone plate on the brow, with
  cracks.

Pixel plan for the Stampeders.
- Dead Run has thin, rotten legs: dark edges and green rot.
- Hunt Sense has a wide, rotten snout: a rotten, green snout with dark,
  wide nostrils.
- Grave Leap has bent back legs with bones: bones through the hind legs.
- Dodge Rot has a twisted, low body: dark bands that twist along the flanks.
- Rot Rush has torn hide at the knees: red flesh with pale, torn edges.
- Mire Trample has mud and rot on the hooves.

Pixel plan for the Rotters.
- Wither Tusk has black tusk tips.
- Plague Breath: green fumes at the snout, the gene model
  plague_breath_fumes.
- Rot Cloud has a swollen, green belly.
- Grave Stench has green, rotten patches on the body.
- Corpse Feeder has a torn, open jaw: a red, open mouth with teeth along the
  low edges of the head.
- Bone Rot has rotten bones in the hide: pale bones with brown rot round
  them.

Pixel plan for the Bonebacks.
- Bone Plates: white bone plates on the back, the gene model
  bone_plates_plates.
- Rib Cage has ribs through the hide of the flanks.
- Bone Brace has thick bone on the legs.
- Skull Crest: a bone crest on the head, the gene model skull_crest_crest.
- Holy Bane has dark runes on the bones of the flanks and the tusks.
- Spine Spikes: bone spikes on the back, the gene model spine_spikes_spikes.

Pixel plan for the Maddened.
- Player Hate has white, mad eyes with small red points.
- Blind Fury has red, bulging eyes with a dark ring.
- Mad Squeal has white foam at a wide-open snout: a black opening with a
  ring of foam.
- Berserk Rot has red, open wounds on the body.
- Creeper Hunter has scorched, black ears.
- Rabid Bite has a foaming mouth: white foam on the snout and the jaw.
This script needs Pillow.
"""
from make_gene_marks import noise100
from make_hoglin_marks import (ALL, BODY, BONE, EARS, EYES, FLANKS, FRONT_LEGS, HEAD, HIND_LEGS, LEGS, REGION_MAP,
                               SIDES, TUSKS, Mark, at, cheek, eye_block, face, height, pixels, put, speckle)
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import model_image, write_all

ROT = ("#5A7A2A", "#2A3A14")
RED = ("#B01818", "#5A0808")


# The Gorers.

def deep_gore():
    # The tusks are the gene model deep_gore_tusks.
    return []


def deep_gore_model():
    # Long, broken tusks: each tusk grows longer from its tip, and ends in a
    # broken, jagged end with a dark crack. On the bones right_horn and
    # left_horn, the tusk is from y -11 to 0, x -1 to 1, and z -1 to 1.
    im = model_image()
    paint_box(im, 0, 0, 2, 5, 2, lambda f, x, y: "#4A4438" if y == 2 and x == 0 else BONE[0])
    paint_box(im, 0, 10, 1, 2, 1, lambda f, x, y: BONE[1])
    parts = []
    for bone in ("right_horn", "left_horn"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1, -16, -1], "size": [2, 5, 2], "inflate": -0.1})
        parts.append({"bone": bone, "uv": [0, 10], "from": [-1, -18, -1], "size": [1, 2, 1], "inflate": -0.1})
    save_model("deep_gore_tusks", im, parts)


def high_toss():
    # Tusks that point up: the low part of each tusk is dark, and its long,
    # high part is a bright, pale bone, so the tusks look tall.
    dark = Mark("#5A5040")
    bright = Mark("#FFFFF4")
    for tusk in TUSKS:
        put(bright, tusk, ALL, rows=range(0, 7))
        put(dark, tusk, SIDES, rows=range(8, 11))
    return [dark, bright]


def bleed_out():
    red = Mark(RED[0])
    dark = Mark(RED[1])
    glint = Mark("#FFE8E8")
    for tusk in TUSKS:
        for x, y, f, col, row in pixels(tusk, ALL):
            (glint if (col + row) % 5 == 0 else dark if row > 7 else red).put((x, y))
    return [red, dark, glint]


def hook_tusk():
    # The hooks are the gene model hook_tusk_hooks.
    return []


def hook_tusk_model():
    # Hooked, bone tusks: a pale bone hook at the tip of each tusk bends
    # forward and down. On the bones right_horn and left_horn, the tip of the
    # tusk is at y -11, and the front is at z -1.
    im = model_image()
    paint_box(im, 0, 0, 2, 2, 3, lambda f, x, y: "#FFFFF4" if f == "top" else BONE[0])
    paint_box(im, 0, 8, 2, 3, 1, lambda f, x, y: BONE[1])
    parts = []
    for bone in ("right_horn", "left_horn"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [-1, -13, -3], "size": [2, 2, 3], "inflate": -0.1})
        parts.append({"bone": bone, "uv": [0, 8], "from": [-1, -12, -4], "size": [2, 3, 1], "inflate": -0.1})
    save_model("hook_tusk_hooks", im, parts)


def golem_gore():
    gray = Mark("#A09A90")
    crack = Mark("#141210")
    for tusk in TUSKS:
        for x, y, f, col, row in pixels(tusk, ALL):
            (crack if (row + 2 * col) % 4 == 0 else gray).put((x, y))
    return [gray, crack]


def grim_charge():
    # A cracked, flat brow: rows 0 to 5 of the face are a pale bone plate,
    # with dark cracks across it.
    bone = Mark(BONE[0])
    crack = Mark("#3A3428")
    for x, y, f, col, row in pixels(HEAD, ("top",), rows=range(0, 6)):
        (crack if col == (row * 2 + 3) % 14 or col == (13 - row * 2) % 14 or row == 5 else bone).put((x, y))
    put(bone, HEAD, ("back",))
    return [bone, crack]


# The Stampeders.

def dead_run():
    dark = Mark("#3A2018")
    rot = Mark(ROT[0])
    for leg in LEGS:
        w = leg["front"][2]
        for x, y, f, col, row in pixels(leg, SIDES):
            if col in (0, w - 1):
                dark.put((x, y))
            elif noise100(x, y, 7101) < 35:
                rot.put((x, y))
    return [dark, rot]


def hunt_sense():
    # A wide, rotten snout: the snout end of the face and the end of the
    # snout are a rotten green, with dark, wide nostrils.
    rot = Mark(ROT[0])
    dark = Mark("#140A06")
    speckle([rot, Mark(ROT[1])], HEAD, ("top",), 70, 7201, rows=range(12, 19))
    for x, y, f, col, row in pixels(HEAD, ("front",)):
        (dark if row in (1, 2) and col in (2, 3, 4, 9, 10, 11) else rot).put((x, y))
    return [rot, dark]


def grave_leap():
    # Bent back legs with bones: pale bones show through the hind legs, with
    # dark flesh round them, and the haunch is bent and dark.
    bone = Mark(BONE[0])
    flesh = Mark("#6A2020")
    for leg in HIND_LEGS:
        for x, y, f, col, row in pixels(leg, SIDES):
            if col == 2 and 1 <= row <= 9:
                bone.put((x, y))
            elif col in (1, 3) and row in (1, 5, 9):
                flesh.put((x, y))
    for f, cols in (("right", range(1, 7)), ("left", range(19, 25))):
        put(flesh, BODY, (f,), cols=cols, rows=(12, 13))
    return [bone, flesh]


def dodge_rot():
    # A twisted, low body: dark bands slant along the flanks, as twists in
    # the body, and the low rows are dark.
    dark = Mark("#5A2A20")
    for x, y, f, col, row in pixels(BODY, FLANKS):
        if (col + row) % 7 in (0, 1) or row >= 12:
            dark.put((x, y))
    return [dark]


def rot_rush():
    flesh = Mark("#9A1A1A")
    edge = Mark("#F0D8C8")
    for leg in LEGS:
        h = height(leg)
        knee = h // 2
        for x, y, f, col, row in pixels(leg, SIDES, rows=(knee - 1, knee, knee + 1)):
            (edge if row == knee - 1 and col % 2 == 0 or row == knee + 1 and col % 2 else flesh).put((x, y))
    return [flesh, edge]


def mire_trample():
    mud = Mark("#4A3A24")
    rot = Mark(ROT[0])
    for leg in LEGS:
        h = height(leg)
        for x, y, f, col, row in pixels(leg, SIDES, rows=range(h - 4, h)):
            (rot if noise100(x, y, 7301) < 35 else mud).put((x, y))
        put(mud, leg, ("bottom",))
    return [mud, rot]


# The Rotters.

def wither_tusk():
    black = Mark("#141010")
    gray = Mark("#4A4444")
    for tusk in TUSKS:
        put(black, tusk, ALL, rows=range(0, 4))
        put(gray, tusk, SIDES, rows=(4,))
    return [black, gray]


def plague_breath():
    # Green stains on the snout. The fumes are the gene model
    # plague_breath_fumes.
    stain = Mark("#7AA040")
    speckle([stain], HEAD, ("front",), 50, 7401)
    speckle([stain], HEAD, ("top",), 40, 7402, rows=range(15, 19))
    return [stain]


def plague_breath_model():
    # Green fumes at the snout: puffs of pale green gas in front of the end
    # of the snout, each on the one under it. On the bone head, the end of
    # the snout is at z -19, from x -7 to 7 and y -3 to 3.
    im = model_image()
    paint_box(im, 0, 0, 3, 3, 2, lambda f, x, y: "#B8E070" if f == "front" else "#8AB850")
    paint_box(im, 0, 8, 2, 2, 2, lambda f, x, y: "#D8F0A0")
    parts = [{"bone": "head", "uv": [0, 0], "from": [-6, -1, -21], "size": [3, 3, 2], "inflate": 0.0},
             {"bone": "head", "uv": [0, 0], "from": [3, -1, -21], "size": [3, 3, 2], "inflate": 0.0},
             {"bone": "head", "uv": [0, 8], "from": [-1, 0, -21], "size": [2, 2, 2], "inflate": 0.0},
             {"bone": "head", "uv": [0, 8], "from": [-5.5, -1, -23], "size": [2, 2, 2], "inflate": 0.0},
             {"bone": "head", "uv": [0, 8], "from": [3.5, -1, -23], "size": [2, 2, 2], "inflate": 0.0}]
    save_model("plague_breath_fumes", im, parts)


def rot_cloud():
    green = Mark("#8AB060")
    edge = Mark("#4A6A2A")
    for x, y, f, col, row in pixels(BODY, FLANKS):
        dx, dy = (col - 12.5) / 9, (row - 13) / 6
        r = dx * dx + dy * dy
        if r < 0.8:
            green.put((x, y))
        elif r < 1.1:
            edge.put((x, y))
    put(green, BODY, ("bottom",))
    return [green, edge]


def grave_stench():
    green = Mark(ROT[0])
    dark = Mark(ROT[1])
    speckle([green, dark], BODY, ("top",) + FLANKS + ("front", "back"), 30, 7501, grain=2)
    return [green, dark]


def corpse_feeder():
    # A torn, open jaw: along the low edge of each side of the head, a red,
    # open mouth with white teeth, and the end of the snout is torn and red.
    red = Mark(RED[0])
    dark = Mark(RED[1])
    teeth = Mark("#F8F4E8")
    for eye in (0, 1):
        dark.put([cheek(eye, c, 5) for c in range(6, 19)])
        red.put([cheek(eye, c, 4) for c in range(8, 19)])
        teeth.put([cheek(eye, c, 3) for c in range(9, 19, 2)])
    for x, y, f, col, row in pixels(HEAD, ("front",)):
        (dark if row >= 4 else red if row >= 2 and col % 3 else None) and \
            (dark if row >= 4 else red).put((x, y))
    put(red, HEAD, ("bottom",), rows=range(12, 19))
    return [red, dark, teeth]


def bone_rot():
    bone = Mark(BONE[0])
    rot = Mark("#5A4020")
    for x, y, f, col, row in pixels(BODY, FLANKS + ("top",)):
        n = noise100(x // 2, y // 2, 7601)
        if n < 12:
            bone.put((x, y))
        elif n < 22:
            rot.put((x, y))
    return [bone, rot]


# The Bonebacks.

def bone_plates():
    # The plates are the gene model bone_plates_plates.
    return []


def bone_plates_model():
    # White bone plates: small bone plates along the back, in two rows that
    # do not line up, as scales, each with a pale ridge. On the bone body,
    # the top of the body is at y -7, from x -8 to 8 and z -13 to 13.
    im = model_image()
    paint_box(im, 0, 0, 5, 1, 3, lambda f, x, y: "#FFFFF4" if f == "top" and x == 2 else BONE[0]
              if f == "top" else BONE[1])
    parts = []
    for n, z in enumerate(range(-12, 11, 3)):
        x = -5.5 if n % 2 == 0 else 0.5
        parts.append({"bone": "body", "uv": [0, 0], "from": [x, -7.8, z], "size": [5, 1, 3], "inflate": 0.0})
    save_model("bone_plates_plates", im, parts)


def rib_cage():
    # Ribs through the hide: pale ribs go down the middle of each flank, with
    # dark gaps between them.
    rib = Mark(BONE[0])
    gap = Mark("#4A1A14")
    for x, y, f, col, row in pixels(BODY, FLANKS, cols=range(8, 20), rows=range(2, 11)):
        (rib if col % 3 == 0 else gap if col % 3 == 1 else None) and (rib if col % 3 == 0 else gap).put((x, y))
    return [rib, gap]


def bone_brace():
    bone = Mark(BONE[0])
    dark = Mark(BONE[1])
    for leg in LEGS:
        for x, y, f, col, row in pixels(leg, ("front", "right", "left"), rows=range(2, 9)):
            (dark if row in (2, 8) else bone).put((x, y))
    return [bone, dark]


def skull_crest():
    # The crest is the gene model skull_crest_crest.
    return []


def skull_crest_model():
    # A bone crest on the head: a ridge of bone along the middle of the brow
    # and the snout, with spikes on its high end. On the bone head, the face
    # is the top of the head, at y -3, from z -19 to 0.
    im = model_image(64, 32)
    paint_box(im, 0, 0, 2, 2, 12, lambda f, x, y: "#FFFFF4" if f == "top" else BONE[0])
    paint_box(im, 0, 16, 1, 2, 1, lambda f, x, y: BONE[1] if y else "#FFFFF4")
    parts = [{"bone": "head", "uv": [0, 0], "from": [-1, -5, -13], "size": [2, 2, 12], "inflate": 0.0}]
    for z in (-4, -2):
        parts.append({"bone": "head", "uv": [0, 16], "from": [-0.5, -7, z], "size": [1, 2, 1], "inflate": 0.0})
    save_model("skull_crest_crest", im, parts)


def holy_bane():
    # Dark runes on the bones: dark rune signs of 3x3 in a row along the
    # flanks, and dark rings round the tusks.
    rune = Mark("#2A1440")
    shapes = (((0, 0), (1, 1), (2, 2), (0, 2), (2, 0)), ((1, 0), (1, 1), (1, 2), (0, 1), (2, 1)),
              ((0, 0), (1, 0), (2, 0), (1, 1), (1, 2)))
    for f in FLANKS:
        for n, c0 in enumerate(range(2, 24, 5)):
            for dc, dr in shapes[n % 3]:
                rune.put(at(BODY, f, c0 + dc, 4 + dr))
    for tusk in TUSKS:
        put(rune, tusk, SIDES, rows=(3, 6, 9))
    return [rune]


def spine_spikes():
    # The spikes are the gene model spine_spikes_spikes.
    return []


def spine_spikes_model():
    # Bone spikes on the back: a row of bone spikes down the middle of the
    # back, tall at the neck and short at the tail. On the bone body, the top
    # of the body is at y -7.
    im = model_image()
    paint_box(im, 0, 0, 2, 4, 2, lambda f, x, y: "#FFFFF4" if y == 0 else BONE[0])
    parts = []
    for n, z in enumerate(range(-12, 12, 3)):
        h = 4 if n < 3 else 3 if n < 6 else 2
        parts.append({"bone": "body", "uv": [0, 0], "from": [-1, -7 - h, z], "size": [2, h, 2], "inflate": -0.2})
    save_model("spine_spikes_spikes", im, parts)


# The Maddened.

def player_hate():
    white = Mark("#FFFFFF")
    red = Mark("#E01010")
    eye_block(white, white, size=4)
    for n, (c0, r0) in enumerate(EYES):
        red.put(face(c0 + (1 if n == 0 else 0), r0 + 1))
        white.put([cheek(n, c, r) for c in (6, 7, 8, 9) for r in (0, 1)])
    return [white, red]


def blind_fury():
    red = Mark("#E01010")
    bright = Mark("#FF6040")
    ring = Mark("#3A0606")
    eye_block(red, bright, ring=ring, size=4)
    for n in (0, 1):
        red.put([cheek(n, c, r) for c in (6, 7, 8, 9) for r in (0, 1)])
        ring.put([cheek(n, c, 2) for c in (6, 7, 8, 9)])
    return [red, bright, ring]


def mad_squeal():
    # White foam at a wide-open snout: the end of the snout is wide open,
    # black inside, with a thick ring of white foam round the opening and
    # foam on the end of the face. No foam goes along the sides of the head.
    black = Mark("#0E0808")
    foam = Mark("#FFFFFF")
    bubble = Mark("#C8DCDC")
    for x, y, f, col, row in pixels(HEAD, ("front",)):
        if 2 <= col <= 11 and 1 <= row <= 4:
            black.put((x, y))
        else:
            (bubble if (col + row) % 3 == 0 else foam).put((x, y))
    for x, y, f, col, row in pixels(HEAD, ("top",), rows=(16, 17, 18)):
        (bubble if (col * 2 + row) % 5 == 0 else foam).put((x, y))
    return [black, foam, bubble]


def berserk_rot():
    red = Mark(RED[0])
    dark = Mark(RED[1])
    for f in FLANKS + ("top",):
        x0, y0, w, h = BODY[f]
        for n in range(6):
            c, r = (n * 7 + 3) % (w - 2), (n * 5 + 2) % (h - 2)
            red.put([at(BODY, f, c, r), at(BODY, f, c + 1, r), at(BODY, f, c, r + 1), at(BODY, f, c + 1, r + 1)])
            dark.put(at(BODY, f, c + 1, r))
    return [red, dark]


def creeper_hunter():
    black = Mark("#141010")
    ash = Mark("#6A6460")
    for ear in EARS:
        for x, y, f, col, row in pixels(ear, ALL):
            (ash if (col + row) % 4 == 0 else black).put((x, y))
    return [black, ash]


def rabid_bite():
    # A foaming mouth: white foam on the end of the snout, on the lip at the
    # end of the face, along the low edge of each side of the head, and in
    # drops under the jaw.
    foam = Mark("#FFFFFF")
    bubble = Mark("#C8DCDC")
    for x, y, f, col, row in pixels(HEAD, ("front",)):
        if row >= 2 or noise100(x, y, 7701) < 40:
            (bubble if noise100(x, y, 7702) < 30 else foam).put((x, y))
    for x, y, f, col, row in pixels(HEAD, ("top",), rows=(17, 18)):
        (bubble if col % 3 == 0 else foam).put((x, y))
    for eye in (0, 1):
        for c in range(8, 19):
            foam.put(cheek(eye, c, 5))
            if c % 2:
                bubble.put(cheek(eye, c, 4))
            if c % 3 == 0:
                foam.put(cheek(eye, c, 3))
    speckle([foam, bubble], HEAD, ("bottom",), 50, 7703, rows=range(10, 19))
    return [foam, bubble]


GENES = {
    "deep_gore": deep_gore, "high_toss": high_toss, "bleed_out": bleed_out, "hook_tusk": hook_tusk,
    "golem_gore": golem_gore, "grim_charge": grim_charge,
    "dead_run": dead_run, "hunt_sense": hunt_sense, "grave_leap": grave_leap, "dodge_rot": dodge_rot,
    "rot_rush": rot_rush, "mire_trample": mire_trample,
    "wither_tusk": wither_tusk, "plague_breath": plague_breath, "rot_cloud": rot_cloud,
    "grave_stench": grave_stench, "corpse_feeder": corpse_feeder, "bone_rot": bone_rot,
    "bone_plates": bone_plates, "rib_cage": rib_cage, "bone_brace": bone_brace, "skull_crest": skull_crest,
    "holy_bane": holy_bane, "spine_spikes": spine_spikes,
    "player_hate": player_hate, "blind_fury": blind_fury, "mad_squeal": mad_squeal, "berserk_rot": berserk_rot,
    "creeper_hunter": creeper_hunter, "rabid_bite": rabid_bite,
}

MODEL_GENES = {
    "deep_gore": (deep_gore_model, "deep_gore_tusks"),
    "hook_tusk": (hook_tusk_model, "hook_tusk_hooks"),
    "plague_breath": (plague_breath_model, "plague_breath_fumes"),
    "bone_plates": (bone_plates_model, "bone_plates_plates"),
    "skull_crest": (skull_crest_model, "skull_crest_crest"),
    "spine_spikes": (spine_spikes_model, "spine_spikes_spikes"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="zoglin genes")
