"""Make the gene visuals of the guardian, from design/species/guardian.md.

Usage: python3 tools/make_guardian_marks.py (from the root of the repository)

The guardian texture is 64x64, and it uses the guardian region map. The parts:
- the core of the body, 12x12x16 at uv (0, 0). Its front (16, 16) is the
  face. The white of the eye is in columns 3 to 8 of rows 5 to 7 of the face.
  The pupil sits in front of columns 5 and 6 of rows 5 and 6.
- the side plates, 2x12x12 at uv (0, 28), and the top and the bottom plates,
  12x2x12 at uv (16, 40). The plates cover most of the core.
- the eye, the pupil, 2x2x1 at uv (8, 0). Its front is at (9, 1).
- the spikes, 2x9x2 at uv (0, 0). All twelve use the same pixels. Row 0 of a
  spike side, and the top of the spike, are at its tip.
- the tail: tail0 (40, 0), tail1 (0, 54), tail2 (41, 32), and the fin (25,
  19), 1x9x9. Its right side (25, 28) is the face of the fin.
The eye genes keep apart by the place of their mark: the pupil, the white of
the eye, a lid over it, a ring round it, or lines on the face round it.
The gene models use the bones head, spike0 to spike11, and tail2.

Pixel plan for the Beamers.
- Deep Beam has a white, hot pupil, the white of the eye glows orange, and
  hot rays shine out from the eye.
- Tiring Gaze has a gray ring round the pupil, in the white of the eye.
- Hull Cutter has a wide, dark red band round the middle of the body.
- Split Beam has two pupils: a dark red pupil at each end of the white of
  the eye, and the middle pupil turns white.
- Dying Glare has red veins that run out from the eye over the face.
- Prism Flash has a ring of many colors: the white of the eye is a rainbow,
  with sparks of the same colors round it.

Pixel plan for the Focusers.
- Quick Focus has a narrow, slit pupil: a gold iris with a black slit.
- Steady Lock has a thick, gold ring round the eye.
- Close Snap has a bright tail tip: the end of the tail and the fin are a
  bright yellow.
- Wide Eye: a large, wide eye, the gene model wide_eye_eye.
- Kill Focus has a half-closed lid: a flat, dark lid over the high half of
  the eye.
- Pulse Beam has glowing, cyan lines on the body.

Pixel plan for the Spikebacks.
- Long Spikes: long spikes, the gene model long_spikes_tips.
- Trident Plate has thick, steel blue plates of uneven sizes that overlap.
- Coral Shell has pink coral plates.
- Blast Plate has dark, sooty plates.
- Venom Spikes has green tips that drip.
- Spike Burst has red tips with orange cracks.

Pixel plan for the Darters.
- Fast Fin: a wide tail fin, the gene model fast_fin_fin.
- Dodge Fin has torn fin edges: wide, dark notches with pale edges in the
  fin and the end of the tail.
- Shore Hop has wet, dark patches on the body.
- Slick Hide has smooth, pale scales on the body.
- Deep Flight has a dark tail: the tail is a deep navy.
- Ram Fin has a hard, gray brow on the body: a gray plate over the eye.

Pixel plan for the Shoalers.
- School Call has rows of small, pale spots on the body.
- Shoal Ward has thin, blue bands round the body.
- Pack Gaze has a thin, blue rim round the white of the eye.
- Alarm Pulse has ripple lines round the tail.
- Kin Grieving has a sad, drooping lid: a lid that droops at both ends,
  with a blue tear under the eye.
- Guide Beam has blue, glowing tips on the spikes.
This script needs Pillow.
"""
from make_gene_marks import Mark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/guardian.png"

CORE = box_faces(0, 0, 12, 12, 16)
SIDE_PLATE = box_faces(0, 28, 2, 12, 12)
FLAT_PLATE = box_faces(16, 40, 12, 2, 12)
PUPIL = box_faces(8, 0, 2, 2, 1)
SPIKE = box_faces(0, 0, 2, 9, 2)
TAIL0 = box_faces(40, 0, 4, 4, 8)
TAIL1 = box_faces(0, 54, 3, 3, 7)
TAIL2 = box_faces(41, 32, 2, 2, 6)
FIN = box_faces(25, 19, 1, 9, 9)
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES
TAIL = (TAIL0, TAIL1, TAIL2)
PLATES = (SIDE_PLATE, FLAT_PLATE)
EYE_WHITE = [(c, r) for c in range(3, 9) for r in range(5, 8)]


def pixels(part, faces=ALL, cols=None, rows=None):
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                if (cols is None or col in cols) and (rows is None or row in rows):
                    yield x0 + col, y0 + row, face, col, row


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    return x0 + col, y0 + row


def face(col, row):
    return at(CORE, "front", col, row)


def put(mark, part, faces=ALL, cols=None, rows=None):
    mark.put([(x, y) for x, y, *_ in pixels(part, faces, cols, rows)])
    return mark


def put_face(mark, cells):
    mark.put([face(c, r) for c, r in cells if 0 <= c < 12 and 0 <= r < 12])


def body(faces=ALL):
    """Yield the pixels of the outer body: the plates, and the core round them, but not the white of the eye."""
    for part in PLATES:
        yield from pixels(part, faces)
    for x, y, f, col, row in pixels(CORE, ("front", "back")):
        # The white of the eye stays clear of the body marks.
        if f == "front" and (col, row) in EYE_WHITE:
            continue
        yield x, y, f, col, row


def ring(c0, r0, c1, r1):
    """Return the cells of a rectangle outline on the face."""
    return [(c, r) for c in range(c0, c1 + 1) for r in range(r0, r1 + 1)
            if c in (c0, c1) or r in (r0, r1)]


def spike_tips(mark, rows=(0, 1, 2)):
    put(mark, SPIKE, SIDES, rows=rows)
    put(mark, SPIKE, ("top",))


# The Beamers.

def deep_beam():
    # A white, hot pupil: the pupil is white, the white of the eye glows
    # orange, and hot, yellow rays shine out from the eye over the face.
    white = Mark("#FFFFF0")
    hot = Mark("#FF9A20")
    ray = Mark("#FFE070")
    put(white, PUPIL, ALL)
    put_face(hot, EYE_WHITE)
    put_face(ray, [(2, 4), (1, 3), (0, 2), (9, 4), (10, 3), (11, 2), (2, 8), (1, 9), (0, 10), (9, 8), (10, 9),
                   (11, 10), (5, 4), (5, 3), (6, 3), (6, 2), (5, 8), (5, 9), (6, 9), (6, 10), (2, 6), (1, 6), (9, 6),
                   (10, 6)])
    return [white, hot, ray]


def tiring_gaze():
    # A gray ring round the pupil: the white of the eye next to the pupil is
    # a dark gray ring, and wide, gray, tired shadows lie over and under the
    # eye.
    gray = Mark("#6A6A70")
    shade = Mark("#9A9AA4")
    put_face(gray, [(c, r) for c in range(4, 8) for r in range(5, 8)])
    put_face(shade, [(c, r) for c in range(2, 10) for r in (3, 4, 8, 9)])
    return [gray, shade]


def hull_cutter():
    red = Mark("#7A1010")
    edge = Mark("#3A0606")
    for x, y, f, col, row in pixels(SIDE_PLATE, SIDES):
        if 4 <= col <= 7 and f in ("right", "left") or f in ("front", "back") and 4 <= row <= 7:
            (edge if col in (4, 7) and f in ("right", "left") else red).put((x, y))
    for x, y, f, col, row in pixels(FLAT_PLATE, ("top", "bottom")):
        if 4 <= row <= 7:
            (edge if row in (4, 7) else red).put((x, y))
    for f in ("back",):
        put(red, CORE, (f,), rows=(5, 6))
    put(red, FLAT_PLATE, ("right", "left"), cols=range(4, 8))
    return [red, edge]


def split_beam():
    # Two pupils: a large, dark red pupil at each end of the white of the
    # eye, and the middle pupil turns white, so the eye shows two pupils.
    red = Mark("#8A0A10")
    white = Mark("#F4F0E8")
    put_face(red, [(c, r) for c in (2, 3, 4, 7, 8, 9) for r in (5, 6, 7)])
    put(white, PUPIL, ALL)
    put_face(white, [(5, 5), (6, 5), (5, 6), (6, 6), (5, 7), (6, 7)])
    return [red, white]


def dying_glare():
    # Red veins run out from the eye over the face, and the white of the eye
    # is pink.
    vein = Mark("#C01818")
    pink = Mark("#F0B0B0")
    put_face(pink, EYE_WHITE)
    put_face(vein, [(2, 4), (1, 3), (0, 3), (2, 8), (1, 9), (1, 10), (9, 4), (10, 3), (11, 2), (9, 8), (10, 9),
                    (11, 9), (5, 4), (5, 3), (6, 2), (6, 8), (6, 9), (7, 10), (2, 6), (1, 6), (9, 6), (10, 6)])
    return [vein, pink]


def prism_flash():
    # A ring of many colors: the white of the eye is a rainbow, a band of a
    # color in each column, and sparks of the same colors lie round the eye.
    colors = ("#FF3030", "#FF9A20", "#FFE830", "#40D040", "#3080FF", "#A040E0")
    marks = [Mark(c) for c in colors]
    for c, mark in zip(range(3, 9), marks):
        put_face(mark, [(c, r) for r in range(5, 8)])
    sparks = ((1, 3), (3, 2), (6, 2), (9, 3), (10, 5), (10, 8), (8, 10), (5, 10), (2, 9), (1, 6), (0, 4), (11, 7))
    for n, cell in enumerate(sparks):
        put_face(marks[n % 6], [cell])
    return marks


# The Focusers.

def quick_focus():
    gold = Mark("#E0A820")
    black = Mark("#000000")
    put_face(gold, [(c, r) for c, r in EYE_WHITE if c not in (5, 6)])
    put_face(black, [(c, r) for c in (5, 6) for r in range(3, 10)])
    put(gold, PUPIL, ("front",))
    black.put([at(PUPIL, "front", 0, 0), at(PUPIL, "front", 0, 1)])
    return [gold, black]


def steady_lock():
    gold = Mark("#E0A020")
    dark = Mark("#6A4A08")
    put_face(gold, ring(1, 3, 10, 9) + ring(2, 4, 9, 8))
    put_face(dark, [(1, 3), (10, 3), (1, 9), (10, 9)])
    return [gold, dark]


def close_snap():
    yellow = Mark("#FFE840")
    put(yellow, TAIL2, ALL)
    put(yellow, FIN, ALL)
    return [yellow]


def wide_eye():
    # The large eye is the gene model wide_eye_eye.
    return []


def wide_eye_model():
    # A large, wide eye: a large, white eye of 10x5 on the face, with a
    # large, dark red pupil of 4x3 in its middle. On the bone head, the face
    # is at z -8, from x -6 to 6 and y 10 to 22.
    im = model_image()

    def eye(f, x, y):
        if f != "front":
            return "#C8C0B0"
        return "#7A0A10" if 3 <= x <= 6 and 1 <= y <= 3 else "#F4F0E8"
    paint_box(im, 0, 0, 10, 5, 1, eye)
    parts = [{"bone": "head", "uv": [0, 0], "from": [-5, 13, -8.6], "size": [10, 5, 1], "inflate": -0.1}]
    save_model("wide_eye_eye", im, parts)


def kill_focus():
    # Kill marks: a red ring round the eye, with notches cut in it, and a row
    # of short, red tally marks over it, one for each kill.
    red = Mark("#C81818")
    tally = Mark("#7A0808")
    put_face(red, [(c, r) for c, r in ring(1, 3, 10, 9) if (c + r) % 3 != 0])
    put_face(tally, [(c, r) for c in (2, 4, 6, 8) for r in (0, 1)] + [(c, 1) for c in (3, 5, 7)])
    return [red, tally]


def pulse_beam():
    cyan = Mark("#40F0FF")
    for x, y, f, col, row in body():
        if (col + row) % 6 == 0:
            cyan.put((x, y))
    return [cyan]


# The Spikebacks.

def long_spikes():
    # The long tips are the gene model long_spikes_tips.
    return []


def long_spikes_model():
    # Long spikes: a thin, orange tip grows from the end of each spike. On
    # each spike bone, the spike is from y -4.5 to 4.5, and its tip is at y
    # -4.5.
    im = model_image()
    paint_box(im, 0, 0, 1, 5, 1, lambda f, x, y: "#FFC060" if y < 2 else "#E86A1A")
    parts = [{"bone": f"spike{i}", "uv": [0, 0], "from": [-0.5, -9.5, -0.5], "size": [1, 5, 1], "inflate": 0.0}
             for i in range(12)]
    save_model("long_spikes_tips", im, parts)


def plates(colors, seed):
    main, edge = Mark(colors[0]), Mark(colors[1])
    for x, y, f, col, row in body():
        if col % 4 == 0 or row % 4 == 0:
            edge.put((x, y))
        elif noise100(x, y, seed) < 85:
            main.put((x, y))
    return [main, edge]


def trident_plate():
    # Thick, steel blue plates: large, steel blue plates of uneven sizes that
    # overlap, each with a dark low edge and a pale high edge, not in a grid.
    steel = Mark("#4A6A98")
    dark = Mark("#22344E")
    pale = Mark("#8AA8D0")
    for x, y, f, col, row in body():
        cell = (col + noise100(row // 4, len(f), 3101) % 5) // 5, (row + noise100(col // 5, len(f), 3102) % 4) // 4
        inner_r = (row + noise100(col // 5, len(f), 3102) % 4) % 4
        if inner_r == 3:
            dark.put((x, y))
        elif inner_r == 0 and noise100(cell[0], cell[1], 3103) < 60:
            pale.put((x, y))
        else:
            steel.put((x, y))
    return [steel, dark, pale]


def coral_shell():
    # Pink coral plates: pink, round blobs of coral with pale tips on the
    # plates, not in a grid.
    pink = Mark("#F070A0")
    pale = Mark("#FFC8DC")
    for x, y, f, col, row in body():
        n = noise100(x // 2, y // 2, 3201)
        if n < 45:
            (pale if noise100(x, y, 3202) < 25 else pink).put((x, y))
    return [pink, pale]


def blast_plate():
    soot = Mark("#2A2624")
    gray = Mark("#5A5450")
    for x, y, f, col, row in body():
        n = noise100(x, y // 2, 3301)
        if n < 70:
            (gray if n < 20 else soot).put((x, y))
    return [soot, gray]


def venom_spikes():
    green = Mark("#40D030")
    drip = Mark("#1A6A10")
    spike_tips(green, (0, 1, 2))
    put(drip, SPIKE, SIDES, rows=(3,), cols=(0,))
    return [green, drip]


def spike_burst():
    red = Mark("#D01010")
    crack = Mark("#FFA040")
    spike_tips(red, (0, 1, 2, 3))
    put(crack, SPIKE, SIDES, rows=(1, 3), cols=(1,))
    return [red, crack]


# The Darters.

def fast_fin():
    # The wide fin is the gene model fast_fin_fin.
    return []


def fast_fin_model():
    # A wide tail fin: a wide, flat, orange fin grows out to both sides of
    # the end of the tail, as the tail of a fish. On the bone tail2, the end
    # of the tail is from x 0 to 2, y 14 to 16, and z 0 to 6.
    im = model_image(64, 32)

    def fin(f, x, y):
        return "#FFB040" if x in (0, 13) or y == 0 else "#E87A20" if (x + y) % 3 else "#B04A10"
    paint_box(im, 0, 0, 14, 1, 6, fin)
    parts = [{"bone": "tail2", "uv": [0, 0], "from": [-6, 14.5, 4], "size": [14, 1, 6], "inflate": -0.1}]
    save_model("fast_fin_fin", im, parts)


def dodge_fin():
    # Torn fins: dark, ragged notches go along the full length of the tail,
    # with pale, torn gaps between them. The fin and the end of the tail
    # have torn edges too.
    dark = Mark("#1A1410")
    pale = Mark("#F0D8B0")
    for n, part in enumerate(TAIL):
        for x, y, f, col, row in pixels(part):
            w, h = part[f][2], part[f][3]
            # The torn edges go along the long edges of each face.
            if (row in (0, h - 1)) if w > h else (col in (0, w - 1)):
                k = noise100(x, y, 4301 + n)
                if k < 35:
                    dark.put((x, y))
                elif k < 65:
                    pale.put((x, y))
    for x, y, f, col, row in pixels(FIN, ("right", "left")):
        edge = row in (0, 1, 7, 8) or col in (0, 1, 7, 8)
        if edge and ((row // 2) + (col // 2)) % 2 == 0:
            dark.put((x, y))
        elif edge:
            pale.put((x, y))
    put(dark, TAIL2, ("back",))
    return [dark, pale]


def shore_hop():
    wet = Mark("#2A4A48")
    shine = Mark("#9AD0D0")
    for x, y, f, col, row in body():
        n = noise100(x // 3, y // 3, 4101)
        if n < 35:
            (shine if noise100(x, y, 4102) < 12 else wet).put((x, y))
    return [wet, shine]


def slick_hide():
    # Smooth, pale scales: a smooth, pale hide, with soft, light shine in
    # places and a few faint scale arcs.
    pale = Mark("#B8E0D0")
    soft = Mark("#9AD0C0")
    shine = Mark("#E8FFF8")
    for x, y, f, col, row in body():
        n = noise100(x // 3, y // 3, 4201)
        if n < 12 and noise100(x, y, 4202) < 50:
            shine.put((x, y))
        elif row % 4 == 3 and (col + (row // 4) * 2) % 6 in (1, 2) and noise100(col // 6, row // 4, 4203) < 50:
            soft.put((x, y))
        else:
            pale.put((x, y))
    return [pale, soft, shine]


def deep_flight():
    navy = Mark("#141E3A")
    blue = Mark("#2A3A6A")
    for part in TAIL + (FIN,):
        for x, y, f, col, row in pixels(part, ALL):
            (blue if (col + row) % 4 == 0 else navy).put((x, y))
    return [navy, blue]


def ram_fin():
    # A hard, gray brow on the body: a thick, gray plate across the high
    # face over the eye, with dark rivets, and on the front of the top
    # plate.
    gray = Mark("#8A8E94")
    rivet = Mark("#3A3E44")
    for c in range(12):
        for r in (1, 2, 3):
            (rivet if r == 2 and c in (1, 5, 10) else gray).put(face(c, r))
    put(gray, FLAT_PLATE, ("front",))
    put(gray, FLAT_PLATE, ("top",), rows=(10, 11))
    return [gray, rivet]


# The Shoalers.

def school_call():
    # Rows of small spots: small, pale spots in rows, each row moved half a
    # step from the one over it, as a shoal of small fish.
    spot = Mark("#E8F4E0")
    for x, y, f, col, row in body():
        shift = 2 if (row // 3) % 2 else 0
        if row % 3 == 1 and (col + shift) % 4 == 1 and noise100(x, y, 5101) < 85:
            spot.put((x, y))
    return [spot]


def shoal_ward():
    blue = Mark("#2A70E0")
    for x, y, f, col, row in pixels(SIDE_PLATE, ("right", "left")):
        if col in (2, 5, 8):
            blue.put((x, y))
    for x, y, f, col, row in pixels(FLAT_PLATE, ("top", "bottom")):
        if row in (2, 5, 8):
            blue.put((x, y))
    put(blue, SIDE_PLATE, ("front", "back"), rows=(2, 5, 8))
    put(blue, CORE, ("back",), rows=(2, 5, 8))
    return [blue]


def pack_gaze():
    # A blue rim round the eye: a blue rim round the white of the eye, and
    # the ends of the white are a pale blue, so the eye looks blue.
    blue = Mark("#2A8AFF")
    pale = Mark("#A8D8FF")
    put_face(blue, ring(2, 4, 9, 8))
    put_face(pale, [(c, r) for c in (3, 4, 7, 8) for r in range(5, 8)])
    return [blue, pale]


def alarm_pulse():
    # Ripple lines: pale rings go round the tail, every third row, as
    # ripples that run back along it.
    ripple = Mark("#F0F4FF")
    for part in TAIL:
        for x, y, f, col, row in pixels(part, SIDES):
            if (col if f in ("right", "left") else row) % 3 == 0:
                ripple.put((x, y))
    return [ripple]


def kin_grieving():
    # A sad, drooping lid: a dark lid that covers the ends of the white of
    # the eye and droops down at both ends, sad brows that slant down to the
    # sides over it, and a long, blue tear under the eye.
    lid = Mark("#2A4A40")
    tear = Mark("#3A8AF0")
    put_face(lid, [(3, 5), (4, 5), (7, 5), (8, 5), (3, 6), (8, 6), (2, 4), (3, 4), (8, 4), (9, 4), (2, 5), (9, 5),
                   (2, 6), (9, 6), (1, 5), (10, 5)])
    put_face(lid, [(4, 1), (5, 1), (6, 1), (7, 1), (3, 2), (8, 2), (2, 3), (9, 3), (4, 2), (7, 2)])
    put_face(tear, [(c, r) for c in (3, 4) for r in range(8, 12)])
    return [lid, tear]


def guide_beam():
    blue = Mark("#40B0FF")
    pale = Mark("#C8ECFF")
    spike_tips(blue, (1, 2))
    put(pale, SPIKE, SIDES, rows=(0,))
    put(pale, SPIKE, ("top",))
    return [blue, pale]


GENES = {
    "deep_beam": deep_beam, "tiring_gaze": tiring_gaze, "hull_cutter": hull_cutter, "split_beam": split_beam,
    "dying_glare": dying_glare, "prism_flash": prism_flash,
    "quick_focus": quick_focus, "steady_lock": steady_lock, "close_snap": close_snap, "wide_eye": wide_eye,
    "kill_focus": kill_focus, "pulse_beam": pulse_beam,
    "long_spikes": long_spikes, "trident_plate": trident_plate, "coral_shell": coral_shell,
    "blast_plate": blast_plate, "venom_spikes": venom_spikes, "spike_burst": spike_burst,
    "fast_fin": fast_fin, "dodge_fin": dodge_fin, "shore_hop": shore_hop, "slick_hide": slick_hide,
    "deep_flight": deep_flight, "ram_fin": ram_fin,
    "school_call": school_call, "shoal_ward": shoal_ward, "pack_gaze": pack_gaze, "alarm_pulse": alarm_pulse,
    "kin_grieving": kin_grieving, "guide_beam": guide_beam,
}

MODEL_GENES = {
    "wide_eye": (wide_eye_model, "wide_eye_eye"),
    "long_spikes": (long_spikes_model, "long_spikes_tips"),
    "fast_fin": (fast_fin_model, "fast_fin_fin"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="guardian genes")
