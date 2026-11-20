"""Make the gene visuals of the silverfish, from design/species/silverfish.md.

Usage: python3 tools/make_silverfish_marks.py (from the root of the repository)

The silverfish texture is 64x32, and it uses the silverfish region map. The
parts, from SilverfishModel, from the front to the back:
- segment0, the head: 3x2x2 at uv (0, 0). The eyes are at (2, 2) and (4, 2)
  on the front. The low row of the front and of the sides is the jaw line.
- segment1: 4x3x2 at uv (0, 4). The head covers the low rows of its front.
- segment2: 6x4x3 at uv (0, 9). segment1 covers the low rows of the middle
  of its front, and segment3 covers the low rows of the middle of its back.
- segment3: 3x3x3 at uv (0, 16), segment4: 2x2x3 at uv (0, 22). The next
  segment covers the low rows of the back of each.
- segment5: 2x1x2 at uv (11, 0), segment6: 1x1x2 at uv (13, 4). These are
  the tail.
- layer0: 10x8x3 at uv (20, 0), layer1: 6x4x3 at uv (20, 11), and layer2:
  6x5x2 at uv (20, 18). Most of their pixels are clear. The opaque pixels
  are bristles, on the front and the back faces of the layers only. The
  bristles stand round the body, and the player sees them well from above.
On a side face, the script counts the columns from the back of the bug to
the front. The helper side() puts the same column on the right side and on
the left side. On a top face, row 0 is at the back.

Each gene keeps to its own zone, so that the marks stack:
- The head. Egg Spit has the middle column of the front, the green mouth,
  and the bottom. Flint Jaw has the low corners of the front and the low
  row of the sides, the jaw line. Egg Bite has the high row of the sides and
  the front corners of the top, where the jaws join the head. Stone Alarm
  (long feelers), Tunnel Gnaw (long front teeth), and Stone Ambush (wide
  jaws) are gene models.
- The back. Leap Bite has a stripe down the middle of the tops. Death Cry
  has a white band across the back end of segment2: the back row of its top
  and the high row of its back face. Flat Body has the outer columns of the
  top of segment2 and the high row of its front face. Bane Plate has the
  outer columns of the top of segment3 and the bristles of the back of
  layer0. Ember Shell has the outer columns of the top of segment1 and the
  bristles of layer2. Deep Lodge has cracks in the high row of the sides of
  segment2 and segment3. Wake Bite has the high bristles of the front of
  layer0. Stone Seed has the bristles of layer1.
- The body. Blood Call has the high row of segment1. Gnaw Frenzy has the
  middle row of segment1. Gut Bite has the low row of segment1, segment2,
  and segment3. Nest Alarm has the back column of the sides of segment2 and
  the back faces of segment3 and segment4. Egg Sac has the middle of the
  sides of segment2. Cobble Skin has the outer columns of the front face of
  segment2. Dodge Scuttle has a zigzag on the back face of segment2, and
  Alarm Dash has spots between the zigzag and on the middle row of the sides
  of segment3. Shell Slip has the sides of segment4. Rust Bite has the low
  bristles of the front of layer0. Brood Burst is a gene model: a swollen
  copy of segment2 with the texture of the mob.
- The tail. Feeding Bite has the top and the sides of segment5. Swarm Rally
  has the back half of segment6. Fast Scuttle (a forked tail), Wall Climb (a
  hooked tail tip), and Egg Lodge (small eggs on the tail) are gene models.
The gene models use the bones segment0, segment2, segment5, and segment6.
In the space of a segment bone, the segment is from y 0 (its top) to its
height, and from z -d/2 (its front) to d/2 (its back).
This script needs Pillow.
"""
import json
import os

from make_gene_marks import Mark as TextureMark
from make_husk_marks import MODEL_DIR, paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/silverfish.png"


def Mark(tint, **kw):
    # The silverfish texture is 64x32.
    return TextureMark(tint, size=64, height=32, **kw)


HEAD = box_faces(0, 0, 3, 2, 2)
S1 = box_faces(0, 4, 4, 3, 2)
S2 = box_faces(0, 9, 6, 4, 3)
S3 = box_faces(0, 16, 3, 3, 3)
S4 = box_faces(0, 22, 2, 2, 3)
S5 = box_faces(11, 0, 2, 1, 2)
S6 = box_faces(13, 4, 1, 1, 2)
LAYER0 = box_faces(20, 0, 10, 8, 3)
LAYER1 = box_faces(20, 11, 6, 4, 3)
LAYER2 = box_faces(20, 18, 6, 5, 2)

# The bristles: the opaque pixels of the layers, as (column, row) on a face.
BRISTLES = {
    ("layer0", "front"): [(3, 1), (5, 1), (1, 2), (3, 2), (5, 2), (7, 2), (0, 3), (2, 3), (4, 3), (5, 3), (6, 3),
                        (8, 3), (1, 4), (9, 4), (8, 5), (0, 6), (1, 7), (8, 7)],
    ("layer0", "back"): [(2, 2), (5, 2), (3, 3), (5, 3), (8, 3), (1, 4), (1, 6), (8, 6), (9, 6)],
    ("layer1", "front"): [(3, 0), (0, 1), (3, 1), (1, 2), (4, 3)],
    ("layer1", "back"): [(3, 0), (2, 1), (5, 1), (0, 2), (1, 2), (4, 2), (4, 3)],
    ("layer2", "front"): [(0, 0), (3, 0), (5, 0), (1, 1), (3, 1), (5, 1), (0, 2), (5, 3), (0, 4)],
}


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    assert 0 <= col < w and 0 <= row < h, (face, col, row)
    return x0 + col, y0 + row


def side(part, col, row):
    """Return a pixel of the right side and the same pixel of the left side.

    The column counts from the back of the part to the front.
    """
    x0, y0, w, h = part["right"]
    lx, ly, lw, lh = part["left"]
    assert 0 <= col < w and 0 <= row < h, (col, row)
    return [(x0 + col, y0 + row), (lx + lw - 1 - col, ly + row)]


def sides(part, cols, rows):
    return [p for c in cols for r in rows for p in side(part, c, r)]


def face_px(part, face, cols, rows):
    return [at(part, face, c, r) for c in cols for r in rows]


LAYERS = {"layer0": LAYER0, "layer1": LAYER1, "layer2": LAYER2}


def bristles(layer, face, test=lambda col, row: True):
    return [at(LAYERS[layer], face, c, r) for c, r in BRISTLES[(layer, face)] if test(c, r)]


# The head.

def flint_jaw():
    # Gray, stone-colored jaws: the jaw line, the low corners of the front
    # and the low row of the sides, is dark flint gray, with darker flecks.
    flint = Mark("#56565C")
    fleck = Mark("#2C2C32")
    flint.put([at(HEAD, "front", 0, 1), at(HEAD, "front", 2, 1)])
    flint.put(side(HEAD, 1, 1))
    fleck.put(side(HEAD, 0, 1))
    return [flint, fleck]


def egg_bite():
    # Pale yellow jaws: the jaws join the head in the high row of the sides
    # and at the front corners of the top. These pixels are pale yellow.
    pale = Mark("#F2E27A")
    hinge = Mark("#C8B048")
    pale.put(side(HEAD, 1, 0))
    hinge.put(side(HEAD, 0, 0))
    pale.put([at(HEAD, "top", 0, 1), at(HEAD, "top", 2, 1)])
    return [pale, hinge]


def egg_spit():
    # A green mouth: the middle column of the front, from the snout down to
    # the mouth, and the bottom of the head are green.
    green = Mark("#3CC83C")
    dark = Mark("#1E7A1E")
    green.put([at(HEAD, "front", 1, 1), at(HEAD, "top", 1, 1)])
    dark.put(at(HEAD, "front", 1, 0))
    green.put(face_px(HEAD, "bottom", range(3), range(2)))
    return [green, dark]


def stone_alarm():
    # The long feelers are the gene model stone_alarm_feelers.
    return []


def stone_alarm_model():
    # Long feelers: two thin, dark feelers rise from the front corners of
    # the top of the head and reach forward and out, with pale tips. On the
    # bone segment0, the head is from x -1.5 to 1.5, y 0 to 2, and z -1 to 1.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 6, lambda f, x, y: "#D8D8C8" if f == "front" or (f in ("top", "right", "left") and
                                                                              (x == 0 if f != "left" else x == 5))
              else "#2A2A2E")
    parts = [{"bone": "segment0", "uv": [0, 0], "from": [x, -0.5, -6.6], "size": [1, 1, 6], "inflate": -0.3,
              "pivot": [x + 0.5, 0, -0.6], "rotation": [-30, turn, 0]} for x, turn in ((-1.6, 20), (0.6, -20))]
    save_model("stone_alarm_feelers", im, parts)


def tunnel_gnaw():
    # The long front teeth are the gene model tunnel_gnaw_teeth.
    return []


def tunnel_gnaw_model():
    # Long front teeth: two long, white teeth stick out forward from the
    # middle of the low row of the front, as the teeth of a rat, with a
    # yellow tint at the root.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 2, lambda f, x, y: "#E8DCA0" if f == "back" else "#FAFAF2")
    parts = [{"bone": "segment0", "uv": [0, 0], "from": [x, 1.05, -2.6], "size": [1, 1, 2], "inflate": -0.2}
             for x in (-1.0, 0.0)]
    save_model("tunnel_gnaw_teeth", im, parts)


def stone_ambush():
    # The wide jaws are the gene model stone_ambush_jaws.
    return []


def stone_ambush_model():
    # Wide jaws: two long, curved, dark brown jaws grow out of the sides of
    # the head, at the jaw line, and reach wide to the sides and forward,
    # with pale points.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 3, lambda f, x, y: "#E0D8C0" if f == "front" or
              (f == "top" and y == 0) else "#4A3424")
    parts = [{"bone": "segment0", "uv": [0, 0], "from": [x, 1.0, -3.4], "size": [1, 1, 3], "inflate": -0.1,
              "pivot": [x + 0.5, 1.5, -0.4], "rotation": [0, turn, 0]} for x, turn in ((-2.0, 40), (1.0, -40))]
    save_model("stone_ambush_jaws", im, parts)


# The back.

def leap_bite():
    # A pale stripe down the back: the middle columns of the tops of
    # segment1, segment2 (not its back row), and segment3, and all of the top
    # of segment4, are pale sand.
    pale = Mark("#E8DCB4")
    edge = Mark("#C4B488")
    pale.put(face_px(S1, "top", (1, 2), range(2)))
    pale.put(face_px(S2, "top", (2, 3), (1, 2)))
    pale.put(face_px(S3, "top", (1,), range(3)))
    pale.put(face_px(S4, "top", range(2), (1, 2)))
    edge.put(face_px(S4, "top", range(2), (0,)))
    return [pale, edge]


def death_cry():
    # A white stripe on the back: a band of white across the back end of
    # segment2, the back row of its top and the high row of its back face.
    white = Mark("#FFFFFF")
    shade = Mark("#D4D4DC")
    white.put(face_px(S2, "top", range(6), (0,)))
    shade.put(face_px(S2, "back", range(6), (0,)))
    return [white, shade]


def flat_body():
    # Flat, wide back plates: the outer columns of the top of segment2 are
    # flat, dark slate plates, with a pale rim along the front edge of the
    # back, the high row of the front face of segment2.
    plate = Mark("#3E4A58")
    rim = Mark("#B8C8D8")
    plate.put(face_px(S2, "top", (0, 1, 4, 5), (1, 2)))
    rim.put(face_px(S2, "front", range(6), (0,)))
    return [plate, rim]


def bane_plate():
    # Thick, dark plates: the outer columns of the top of segment3 are near
    # black, with a gray rim, and the bristles of the back of layer0 are dark
    # spikes of the plates.
    dark = Mark("#18181C")
    rim = Mark("#4C4C56")
    dark.put(face_px(S3, "top", (0, 2), (1, 2)))
    rim.put(face_px(S3, "top", (0, 2), (0,)))
    dark.put(bristles("layer0", "back"))
    return [dark, rim]


def ember_shell():
    # Dark red back plates: the outer columns of the top of segment1 are dark
    # red, and the bristles of layer2 glow red as embers.
    plate = Mark("#5A0C0C")
    ember = Mark("#E03810")
    plate.put(face_px(S1, "top", (0, 3), range(2)))
    ember.put(bristles("layer2", "front"))
    return [plate, ember]


def deep_lodge():
    # Cracks on the back: dark cracks run along the high row of the sides of
    # segment2 and segment3, where the back meets the sides.
    crack = Mark("#141414")
    edge = Mark("#5A5A5A")
    for part, cols in ((S2, range(3)), (S3, range(3))):
        for c in cols:
            (crack if c % 2 == 0 else edge).put(side(part, c, 0))
    return [crack, edge]


def wake_bite():
    # A white fringe on the back: the high bristles of the front of layer0
    # are a very light warm gray. Pure white looks like holes on a white
    # background, and the cream of Stone Seed and Egg Lodge is for eggs.
    pale = Mark("#D8D2CA")
    pale.put(bristles("layer0", "front", lambda c, r: r <= 3))
    return [pale]


def stone_seed():
    # Small white eggs on the back: the bristles of layer1 are small, cream
    # eggs.
    egg = Mark("#F4ECD0")
    shade = Mark("#D8CCA4")
    egg.put(bristles("layer1", "back"))
    shade.put(bristles("layer1", "front"))
    return [egg, shade]


# The body.

def blood_call():
    # Red bands on the front segments: the high row of the sides and of the
    # front of segment1 is red.
    red = Mark("#C81818")
    red.put(sides(S1, range(2), (0,)))
    red.put(face_px(S1, "front", range(4), (0,)))
    return [red]


def gnaw_frenzy():
    # Dark red spots: the middle row of the sides of segment1, and the outer
    # columns of the low rows of its front, have dark red spots.
    spot = Mark("#6A0808")
    spot.put(sides(S1, range(2), (1,)))
    spot.put(face_px(S1, "front", (0, 3), (1, 2)))
    return [spot]


def gut_bite():
    # A green belly: the low row of the sides of segment1, segment2, and
    # segment3, and the bottoms of segment1 to segment4, are green.
    green = Mark("#5AAA36")
    dark = Mark("#2E6E1C")
    green.put(sides(S1, range(2), (2,)))
    green.put(sides(S2, range(3), (3,)))
    green.put(sides(S3, range(3), (2,)))
    for part in (S1, S2, S3, S4):
        x0, y0, w, h = part["bottom"]
        dark.put(face_px(part, "bottom", range(w), range(h)))
    return [green, dark]


def nest_alarm():
    # Pale rings round the segments: the back column of the sides of
    # segment2, and the back faces of segment3 and segment4, are pale.
    pale = Mark("#E4ECF4")
    pale.put(sides(S2, (0,), (1, 2)))
    pale.put(face_px(S3, "back", range(3), range(3)))
    pale.put(face_px(S4, "back", range(2), range(2)))
    return [pale]


def egg_sac():
    # Pale, swollen sides: the middle of the sides of segment2 is pale, with a
    # bright shine, so that the sides look swollen.
    pale = Mark("#E8E0D0")
    shine = Mark("#FFFFFF")
    pale.put(sides(S2, (1, 2), (2,)))
    pale.put(side(S2, 1, 1))
    shine.put(side(S2, 2, 1))
    return [pale, shine]


def cobble_skin():
    # Gray stone plates: the outer columns of the front face of segment2 are
    # tiles of stone with dark seams.
    stone = Mark("#9A9488")
    seam = Mark("#3C3832")
    for c in (0, 5):
        stone.put([at(S2, "front", c, 1), at(S2, "front", c, 3)])
        seam.put(at(S2, "front", c, 2))
    return [stone, seam]


# The back face of segment2, where segment3 does not cover it: the outer two
# columns of rows 1 to 3.
ZIGZAG = [(0, 1), (1, 2), (0, 3), (5, 1), (4, 2), (5, 3)]
BETWEEN = [(1, 1), (0, 2), (1, 3), (4, 1), (5, 2), (4, 3)]


def dodge_scuttle():
    # Zigzag lines: a dark zigzag runs down each side of the back face of
    # segment2.
    dark = Mark("#141418")
    dark.put([at(S2, "back", c, r) for c, r in ZIGZAG])
    return [dark]


def alarm_dash():
    # Pale side spots: pale spots in the middle row of the sides of segment3,
    # and between the lines of the back face of segment2.
    pale = Mark("#F8F4D8")
    pale.put(sides(S3, (0, 2), (1,)))
    pale.put([at(S2, "back", c, r) for c, r in BETWEEN])
    return [pale]


def shell_slip():
    # A shiny, smooth shell: the sides of segment4 are smooth and pale blue,
    # with a white shine.
    shell = Mark("#A8C8E0")
    shine = Mark("#FFFFFF")
    shell.put(sides(S4, range(3), (1,)))
    shell.put(sides(S4, (0, 2), (0,)))
    shine.put(side(S4, 1, 0))
    return [shell, shine]


def rust_bite():
    # Orange flecks: the low bristles of the front of layer0 are orange.
    fleck = Mark("#E87020")
    fleck.put(bristles("layer0", "front", lambda c, r: r >= 4))
    return [fleck]


def brood_burst():
    # The swollen middle segment is the gene model brood_burst_segment.
    return []


def brood_burst_model():
    # A swollen middle segment: a copy of segment2, 0.5 pixel larger on each
    # side, with the texture of the mob. The marks of segment2 show on it.
    # The copy moves up 0.5 pixel, so that it stands on the ground.
    parts = [{"bone": "segment2", "uv": [0, 9], "from": [-3, -0.5, -1.5], "size": [6, 4, 3], "inflate": 0.5}]
    save_mob_model("brood_burst_segment", parts)


# The tail.

def feeding_bite():
    # A dark red tail: the top and the sides of segment5 are dark red.
    red = Mark("#7A1010")
    dark = Mark("#4A0606")
    red.put(face_px(S5, "top", range(2), range(2)))
    dark.put(sides(S5, range(2), (0,)))
    return [red, dark]


def swarm_rally():
    # A bright tail tip: the back half of segment6 is bright yellow, white at
    # the very end.
    bright = Mark("#FFE040")
    white = Mark("#FFFFF0")
    bright.put(at(S6, "top", 0, 0))
    bright.put(side(S6, 0, 0))
    white.put(at(S6, "back", 0, 0))
    return [bright, white]


def fast_scuttle():
    # The forked tail is the gene model fast_scuttle_fork.
    return []


def fast_scuttle_model():
    # A forked tail: two thin prongs grow from the back of the tail tip and
    # spread out to the sides, dark, with pale points. On the bone segment6,
    # the tip is from x -0.5 to 0.5, y 0 to 1, and z -1 to 1.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 3, lambda f, x, y: "#C8C8C0" if f == "back" or (f == "top" and y == 2)
              else "#303034")
    parts = [{"bone": "segment6", "uv": [0, 0], "from": [x, 0.1, 0.2], "size": [1, 1, 3], "inflate": -0.25,
              "pivot": [x + 0.5, 0.5, 0.4], "rotation": [0, turn, 0]} for x, turn in ((-1.2, -50), (0.2, 50))]
    save_model("fast_scuttle_fork", im, parts)


def wall_climb():
    # The hooked tail tip is the gene model wall_climb_hook.
    return []


def wall_climb_model():
    # A hooked tail tip: a pale, curved hook rises from the front half of the
    # top of the tail tip and bends forward, with a dark point.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 2, 1, lambda f, x, y: "#D8D0BC")
    paint_box(im, 0, 4, 1, 1, 2, lambda f, x, y: "#2A2620" if f == "front" else "#D8D0BC")
    parts = [{"bone": "segment6", "uv": [0, 0], "from": [-0.5, -2, -0.9], "size": [1, 2, 1], "inflate": -0.2},
             {"bone": "segment6", "uv": [0, 4], "from": [-0.5, -2.2, -2.5], "size": [1, 1, 2], "inflate": -0.2}]
    save_model("wall_climb_hook", im, parts)


def egg_lodge():
    # The small eggs are the gene model egg_lodge_eggs.
    return []


def egg_lodge_model():
    # Small eggs on the tail: two small, cream eggs stick to the low edge of
    # each side of segment5. On the bone segment5, the segment is from x -1 to 1, y 0 to
    # 1, and z -1 to 1.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 1, lambda f, x, y: "#C8BC98" if f == "bottom" else "#F6EED6")
    parts = [{"bone": "segment5", "uv": [0, 0], "from": [x, 0.3, z], "size": [1, 1, 1], "inflate": -0.1}
             for x in (-1.7, 0.7) for z in (-1.0, 0.1)]
    save_model("egg_lodge_eggs", im, parts)


def save_mob_model(name, parts):
    model = {"mob_texture": True, "texture_width": 64, "texture_height": 32, "parts": parts}
    with open(os.path.join(MODEL_DIR, name + ".json"), "w") as fh:
        json.dump(model, fh, indent="\t")
        fh.write("\n")


GENES = {
    "nest_alarm": nest_alarm, "stone_alarm": stone_alarm, "swarm_rally": swarm_rally, "blood_call": blood_call,
    "death_cry": death_cry, "wake_bite": wake_bite,
    "flint_jaw": flint_jaw, "rust_bite": rust_bite, "gnaw_frenzy": gnaw_frenzy, "tunnel_gnaw": tunnel_gnaw,
    "feeding_bite": feeding_bite, "leap_bite": leap_bite,
    "fast_scuttle": fast_scuttle, "dodge_scuttle": dodge_scuttle, "flat_body": flat_body,
    "shell_slip": shell_slip, "wall_climb": wall_climb, "alarm_dash": alarm_dash,
    "cobble_skin": cobble_skin, "deep_lodge": deep_lodge, "ember_shell": ember_shell,
    "stone_ambush": stone_ambush, "bane_plate": bane_plate, "egg_lodge": egg_lodge,
    "egg_bite": egg_bite, "brood_burst": brood_burst, "stone_seed": stone_seed, "egg_sac": egg_sac,
    "egg_spit": egg_spit, "gut_bite": gut_bite,
}

MODEL_GENES = {
    "stone_alarm": (stone_alarm_model, "stone_alarm_feelers"),
    "tunnel_gnaw": (tunnel_gnaw_model, "tunnel_gnaw_teeth"),
    "stone_ambush": (stone_ambush_model, "stone_ambush_jaws"),
    "brood_burst": (brood_burst_model, "brood_burst_segment"),
    "fast_scuttle": (fast_scuttle_model, "fast_scuttle_fork"),
    "wall_climb": (wall_climb_model, "wall_climb_hook"),
    "egg_lodge": (egg_lodge_model, "egg_lodge_eggs"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="silverfish genes")
