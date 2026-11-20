"""Make the gene visuals of the ravager, from design/species/ravager.md.

Usage: python3 tools/make_ravager_marks.py (from the root of the repository)

The ravager texture is 128x128, and it uses the ravager region map. The
parts, from RavagerModel:
- the head, 16x20x16 at uv (0, 0). On the front (the face), the eyes are in
  row 13, in columns 2 to 5 and 10 to 13. Rows 11 and 12 are a dark brow.
  The snout, 4x8x4 at uv (0, 0), stands in front of columns 6 to 9 of rows
  14 to 19. On a side of the head, the script counts the columns from the
  back (column 0) to the front (column 15).
- the horns, 2x14x4 at uv (74, 55). Row 0 of a side is at the tip.
- the mouth, 16x3x16 at uv (0, 36), under the front of the head.
- the neck, 10x10x18 at uv (68, 73). At rest, the head and the body cover
  it, except a strip of 1.5 pixels. So the neck genes put their marks where
  the neck meets the head and the body, or have a gene model.
- the body, 14x16x20 at uv (0, 55) and 12x13x18 at uv (0, 91). It turns by
  90 degrees round x. On the back face of each cube (the back of the
  animal) and on its sides (the flanks), row 0 is at the front. On a flank,
  column 0 is at the top. The saddle is in rows 3 to 13 of the back of the
  first cube. The bottom face of the second cube is the rump.
- the legs, 8x37x8 at uv (64, 0) (front) and (96, 0) (hind). The legs reach
  up past the back, so their tops are part of the back of the animal. Row 0
  of a side is at the top. Rows 33 to 36 are the hoof.

Each gene keeps to its own zone, so that the marks stack:
- The horns. Horn Gore has rows 0 to 6 and the tip. Twin Strike has bands
  in rows 8, 9, 11, and 12, and rings round the roots of the horns on the
  head. Sweep Horns (long horns) and Shield Crush (notched edges) are gene
  models.
- The face. Ram Rush has a stripe down the middle of the top of the head (rows
  0 to 11) and of the forehead (columns 6 to 9, rows 0 to 7). Rider Heal has green marks
  on the sides of the forehead (rows 0 to 7) and on the front of the top.
  Golem Crusher has the brow (rows 8 to 11) and the brow on the sides.
  Victory Roar has gold rings round the eyes (rows 12 to 15), which wrap
  round to the sides. Gore Rage has red veins on the cheeks (rows 16 to 18)
  and on the sides under the eyes. Fence Breaker has scars on the snout, on the
  bridge between the eyes, and on the front of the top of the head. Raid
  Scent has wide, dark nostrils on the front of the snout and beside it,
  and a gene model of flared rims.
- The sides of the head, back half. Rage Roar has orange marks in rows 0 to
  9, and Lone Roar has gray scars in rows 10 to 19. These are the marks of
  the neck, where the neck goes into the head.
- The mouth. Loud Roar has the front, the back, and the high rows of the
  sides of the mouth, and the low row of the face at the corners (the
  lips), pale. War Roar has the low row of the sides of the mouth, red, and
  a gene model: a red tongue and gums out of the front. Fear Roar (long
  teeth) is a gene model.
- The neck. Escort Rider (a gray collar) and Lone Charge (a dark mane) are
  gene models on the neck, where it shows between the head and the body.
- The body. Saddle Guard has the saddle, rows 3 to 13 of the back. Blood
  Run has red marks on the flanks of the first cube. Arrow Hide has dark
  patches on the flanks of the second cube. Horde Hide has a band in
  illager colors across the back end: the back of the second cube and the
  low ring of the first cube. Blast Pelt
  has the rump. Plate Hide has iron plates on the tops of the four legs,
  which stand up beside the back. Rider Rush (a strap round the body) and
  Spiked Hide (short spikes on the back) are gene models.
- The legs. Wide Stride has pale stripes in rows 4 to 12. Ember Pelt has
  dark red in rows 13 to 26. Sure Foot has the hooves. Steady Stance (wide,
  pale feet) is a gene model.
The gene models use the bones head, right_horn, left_horn, mouth, neck,
body, and the four legs.
This script needs Pillow.
"""
from make_gene_marks import Mark as TextureMark, noise100
from make_husk_marks import paint_box, save_model
from make_zombified_piglin_marks import box_faces, model_image, write_all

REGION_MAP = "originofspawns:textures/region/ravager.png"


def Mark(tint, **kw):
    # The ravager texture is 128x128.
    return TextureMark(tint, size=128, height=128, **kw)


HEAD = box_faces(0, 0, 16, 20, 16)
SNOUT = box_faces(0, 0, 4, 8, 4)
HORN = box_faces(74, 55, 2, 14, 4)
MOUTH = box_faces(0, 36, 16, 3, 16)
BODY1 = box_faces(0, 55, 14, 16, 20)
BODY2 = box_faces(0, 91, 12, 13, 18)
LEGS = (box_faces(64, 0, 8, 37, 8), box_faces(96, 0, 8, 37, 8))
SIDES = ("right", "front", "left", "back")


def at(part, face, col, row):
    x0, y0, w, h = part[face]
    assert 0 <= col < w and 0 <= row < h, (face, col, row)
    return x0 + col, y0 + row


def cells(part, face, cols, rows):
    return [at(part, face, c, r) for c in cols for r in rows]


def side(part, col, row):
    """Return a pixel of the right side and the same pixel of the left side.

    On the head, the column counts from the back to the front. On the body,
    it counts from the top to the belly.
    """
    x0, y0, w, h = part["right"]
    lx, ly, lw, lh = part["left"]
    assert 0 <= col < w and 0 <= row < h, (col, row)
    return [(x0 + col, y0 + row), (lx + lw - 1 - col, ly + row)]


def sides(part, cols, rows):
    return [p for c in cols for r in rows for p in side(part, c, r)]


def leg_cells(faces, cols, rows):
    return [p for leg in LEGS for f in faces for p in cells(leg, f, cols, rows)]


def pattern(marks, pixels, seed, shares):
    """Put each pixel on one of the marks, at random, by the shares in percent.

    A pixel past the sum of the shares gets no mark.
    """
    for x, y in pixels:
        n = noise100(x, y, seed)
        total = 0
        for mark, share in zip(marks, shares):
            total += share
            if n < total:
                mark.put((x, y))
                break


# The horns.

def horn_gore():
    # Dark horn tips: rows 0 to 6 of the horns and the tip are near black,
    # with a dark red stain at the low edge.
    dark = Mark("#1A1614")
    stain = Mark("#5A1A12")
    dark.put(cells(HORN, "top", range(2), range(4)))
    for f in SIDES:
        x0, y0, w, h = HORN[f]
        dark.put(cells(HORN, f, range(w), range(6)))
        stain.put(cells(HORN, f, range(w), (6,)))
    return [dark, stain]


def twin_strike():
    # Banded horns: bands of pale bone and dark horn round each horn, rows 7
    # to 13, and a pale ring, 2 pixels wide, round the root of each horn on
    # the head.
    pale = Mark("#E8E0C8")
    dark = Mark("#2A2420")
    for f in SIDES:
        x0, y0, w, h = HORN[f]
        pale.put(cells(HORN, f, range(w), (7, 8, 11, 12)))
        dark.put(cells(HORN, f, range(w), (9, 10, 13)))
    # The roots of the horns are on the sides of the head, near the top and
    # the back. The ring is round columns 6 to 9 of rows 2 to 4.
    for c in range(4, 12):
        for r in range(0, 7):
            if c in (4, 5, 10, 11) or r in (0, 1, 5, 6):
                (dark if (c + r) % 3 == 0 else pale).put(side(HEAD, c, r))
    return [pale, dark]


def sweep_horns():
    # The long horns are the gene model sweep_horns_horns.
    return []


def sweep_horns_model():
    # Long horns: a long, curved horn piece goes on from the tip of each horn
    # and sweeps out to the side, in the gray of the horn, with a pale edge.
    # On a horn bone, the horn is from x 0 to 2, y -14 to 0, and z -2 to 2.
    im = model_image(32, 32)
    paint_box(im, 0, 0, 2, 10, 3, lambda f, x, y: "#B8BEB0" if x == 0 or f == "top" else "#7C8478")
    parts = [{"bone": bone, "uv": [0, 0], "from": [0, -23, -1.5], "size": [2, 10, 3], "inflate": -0.1,
              "pivot": [1, -13, 0], "rotation": [0, 0, turn]}
             for bone, turn in (("right_horn", 30), ("left_horn", -30))]
    save_model("sweep_horns_horns", im, parts)


def shield_crush():
    # The notched edges are the gene model shield_crush_notches.
    return []


def shield_crush_model():
    # Notched horn edges: a row of short, pale, sharp notches stands out of
    # the edge of each horn that faces up and back, with dark roots. The
    # other edge faces the head.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 3, 2, 3, lambda f, x, y: "#2A2420" if f == "front" or y == 1 else "#E4E0D0")
    parts = [{"bone": bone, "uv": [0, 0], "from": [-0.5, y, 1.5], "size": [3, 2, 3]}
             for bone in ("right_horn", "left_horn") for y in (-12.5, -8.5, -4.5)]
    save_model("shield_crush_notches", im, parts)


# The face and the head.

def ram_rush():
    # A pale stripe down the face: a pale stripe runs down the middle of the
    # top of the head and down the forehead.
    pale = Mark("#D8D0C0")
    edge = Mark("#A89E8C")
    pale.put(cells(HEAD, "top", (7, 8), range(12)))
    edge.put(cells(HEAD, "top", (6, 9), range(12)))
    pale.put(cells(HEAD, "front", (7, 8), range(8)))
    edge.put(cells(HEAD, "front", (6, 9), range(8)))
    return [pale, edge]


def rider_heal():
    # Green marks on the brow: green patches on the sides of the forehead,
    # and on the front part of the top of the head.
    green = Mark("#4CB040")
    dark = Mark("#2A7024")
    forehead = cells(HEAD, "front", list(range(0, 6)) + list(range(10, 16)), range(8))
    top = cells(HEAD, "top", list(range(0, 6)) + list(range(10, 16)), range(10, 16))
    pattern([green, dark], forehead + top, 2201, (45, 25))
    return [green, dark]


def golem_crusher():
    # An iron gray brow: the brow, rows 8 to 11 of the face, is iron gray with
    # rivets, and it goes on round the front of the sides.
    iron = Mark("#A0A4A8")
    rivet = Mark("#4A4E52")
    for c in range(16):
        for r in range(8, 12):
            (rivet if r == 9 and c % 3 == 1 else iron).put(at(HEAD, "front", c, r))
    for c in range(10, 16):
        for r in range(8, 12):
            (rivet if r == 9 and c % 3 == 1 else iron).put(side(HEAD, c, r))
    return [iron, rivet]


def victory_roar():
    # Gold rings round the eyes: rows 12 to 15 round each eye are gold, and
    # the rings go on round the front corners of the head.
    gold = Mark("#F0C030")
    deep = Mark("#A87810")
    eyes = {(c, 13) for c in (2, 3, 4, 5, 10, 11, 12, 13)}
    for c in list(range(0, 6)) + list(range(10, 16)):
        for r in range(12, 16):
            if (c, r) in eyes:
                continue
            (deep if r == 15 or c in (0, 15) else gold).put(at(HEAD, "front", c, r))
    for c in range(12, 16):
        for r in range(12, 16):
            (deep if r == 15 else gold).put(side(HEAD, c, r))
    return [gold, deep]


def gore_rage():
    # Red veins round the eyes: a web of red veins on the cheeks, rows 16 to
    # 18 of the face, and on the sides under and behind the eyes.
    vein = Mark("#C01818")
    dark = Mark("#6A0808")
    cheeks = cells(HEAD, "front", list(range(0, 4)) + list(range(12, 16)), range(16, 19))
    under = [p for c in range(8, 16) for r in range(16, 20) for p in side(HEAD, c, r)]
    behind = [p for c in range(8, 12) for r in range(12, 16) for p in side(HEAD, c, r)]
    pattern([vein, dark], cheeks + under + behind, 2203, (60, 40))
    return [vein, dark]


def fence_breaker():
    # Scars on the snout: pale scars across the top and the sides of the
    # snout, the high rows of its front, and the bridge between the eyes.
    scar = Mark("#D8B8A8")
    dark = Mark("#3A2420")
    for f in ("right", "left"):
        for c in range(4):
            for r in range(8):
                (scar if (c + r) % 3 == 0 else dark).put(at(SNOUT, f, c, r))
    scar.put(cells(SNOUT, "top", range(4), range(4)))
    for c in range(4):
        for r in range(3):
            (scar if (c + r) % 2 == 0 else dark).put(at(SNOUT, "front", c, r))
    for c in range(6, 10):
        for r in (12, 13):
            (scar if (c + r) % 2 == 0 else dark).put(at(HEAD, "front", c, r))
    for c in range(6, 10):
        for r in range(12, 16):
            (scar if (c + r) % 2 == 0 else dark).put(at(HEAD, "top", c, r))
    return [scar, dark]


def raid_scent_model():
    # Wide nostrils: two dark, flared rims stand out of the front of the
    # snout, round the nostrils. On the bone head, the snout is from x -2 to
    # 2, y -6 to 2, and z -18 to -14.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 4, 4, 2, lambda f, x, y: "#0A0808" if f == "front" and 1 <= x <= 2 and 1 <= y <= 2
              else "#3A2A26")
    parts = [{"bone": "head", "uv": [0, 0], "from": [x, -4, -19.5], "size": [4, 4, 2]} for x in (-4.5, 0.5)]
    save_model("raid_scent_nostrils", im, parts)


def raid_scent():
    # Wide, dark nostrils: two wide, black nostrils on the front of the
    # snout, with flared, dark rims that spread on to the face beside the
    # snout.
    black = Mark("#0A0808")
    rim = Mark("#3A2A26")
    black.put(cells(SNOUT, "front", (0, 1, 2, 3), (4, 5)))
    rim.put(cells(SNOUT, "front", (0, 1, 2, 3), (3, 6, 7)))
    rim.put(cells(HEAD, "front", (4, 5, 10, 11), range(16, 20)))
    return [black, rim]


def rage_roar():
    # Orange marks on the neck: orange and dark orange stripes cover the back
    # half of the sides of the head, rows 0 to 9, where the neck goes into
    # the head. The ring of Twin Strike stays clear.
    orange = Mark("#F07820")
    dark = Mark("#8A3A10")
    for c in range(8):
        for r in range(10):
            if c >= 4 and r <= 6 and (c in (4, 5) or r in (0, 1, 5, 6)):
                continue
            (orange if (c + 2 * r) % 4 < 2 else dark).put(side(HEAD, c, r))
    return [orange, dark]


def lone_roar():
    # Gray scars on the neck: long, pale gray scars, with dark edges, cover
    # the back half of the sides of the head, rows 10 to 19, where the neck
    # goes into the head.
    scar = Mark("#C4C4C0")
    dark = Mark("#3A3A38")
    for c in range(8):
        for r in range(10, 20):
            (scar if (c - r) % 3 == 0 else dark).put(side(HEAD, c, r))
    return [scar, dark]


# The mouth.

def war_roar():
    # A red mouth: the red tongue and gums are the gene model war_roar_tongue.
    # The low row of the sides of the mouth is red too.
    red = Mark("#B01818")
    red.put(cells(MOUTH, "right", range(16), (2,)) + cells(MOUTH, "left", range(16), (2,)))
    return [red]


def war_roar_model():
    # A red mouth: a long, red tongue hangs out of the middle of the front of
    # the mouth, between the fangs of Fear Roar, with dark red gums over it. On the bone mouth, the mouth is
    # from x -8 to 8, y 0 to 3, and z -16 to 0.
    im = model_image(64, 32)
    paint_box(im, 0, 0, 5, 1, 10, lambda f, x, y: "#E03030" if f == "top" and x % 3 else "#8A1010")
    paint_box(im, 0, 12, 5, 3, 1, lambda f, x, y: "#5A0808" if y else "#C01818")
    parts = [{"bone": "mouth", "uv": [0, 0], "from": [-2.5, 1.5, -24.5], "size": [5, 1, 10], "pivot": [0, 2, -16],
              "rotation": [25, 0, 0]},
             {"bone": "mouth", "uv": [0, 12], "from": [-2.5, -1, -16.8], "size": [5, 3, 1]}]
    save_model("war_roar_tongue", im, parts)


def loud_roar():
    # A pale mouth: the front and the sides of the mouth are pale, as old
    # bone, with gray shade in the low row.
    pale = Mark("#E8E0D4")
    shade = Mark("#B0A89C")
    for f in ("front", "right", "left"):
        x0, y0, w, h = MOUTH[f]
        pale.put(cells(MOUTH, f, range(w), (0, 1)))
        if f == "front":
            shade.put(cells(MOUTH, f, range(w), (2,)))
    # The back of the mouth, under the head, is pale too.
    shade.put(cells(MOUTH, "back", range(16), range(3)))
    # Pale lips: the low row of the face, beside the cheeks.
    pale.put(cells(HEAD, "front", list(range(0, 4)) + list(range(12, 16)), (19,)))
    return [pale, shade]


def fear_roar():
    # The long teeth are the gene model fear_roar_teeth.
    return []


def fear_roar_model():
    # Long teeth: four long, pale fangs hang from the front edge of the
    # mouth, to 6 pixels below it. On the bone mouth, the mouth is from x -8
    # to 8, y 0 to 3, and z -16 to 0.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 2, 9, 2, lambda f, x, y: "#C8B890" if y >= 7 else "#F4EEDC")
    parts = [{"bone": "mouth", "uv": [0, 0], "from": [x, -1, -17], "size": [2, 9, 2], "inflate": -0.4}
             for x in (-7.5, -4.5, 2.5, 5.5)]
    save_model("fear_roar_teeth", im, parts)


# The neck.

def escort_rider():
    # The gray collar is the gene model escort_rider_collar.
    return []


def escort_rider_model():
    # A gray collar: a thick ring of gray iron round the neck, where the
    # neck shows between the head and the body. It is larger than the head,
    # so that its rim shows round the back of the head, with dark rivets. On
    # the bone neck, the neck is from x -5 to 5, y -1 to 9, and z -18 to 0.
    # The back of the head is at z -15.
    im = model_image(64, 64)

    def iron(f, x, y):
        if f in ("right", "left", "top", "bottom"):
            return "#3E4246" if (x + y) % 4 == 0 else "#9CA2A8"
        return "#9CA2A8" if (x + y) % 6 else "#3E4246"
    paint_box(im, 0, 0, 18, 22, 3, iron)
    parts = [{"bone": "neck", "uv": [0, 0], "from": [-9, -5, -15.5], "size": [18, 22, 3]}]
    save_model("escort_rider_collar", im, parts)


def lone_charge():
    # The dark mane is the gene model lone_charge_mane.
    return []


def lone_charge_model():
    # A dark mane: a crest of dark, rough hair stands up on the neck,
    # between the head and the body, and over the front of the back. On the
    # bone neck, the neck is from x -5 to 5, y -1 to 9, and z -18 to 0. The
    # neck shows between z -15 and -13.5.
    im = model_image(32, 32)

    def hair(f, x, y):
        if f == "top":
            return "#4A3A30" if (x + y) % 2 else "#1A1410"
        return "#4A3A30" if y == 0 or (x + y) % 3 == 0 else "#1A1410"
    paint_box(im, 0, 0, 4, 9, 5, hair)
    parts = [{"bone": "neck", "uv": [0, 0], "from": [-2, -9, -16], "size": [4, 9, 5]}]
    save_model("lone_charge_mane", im, parts)


# The body.

def saddle_guard():
    # A dark saddle cloth: the saddle, rows 3 to 13 of the back, is a dark
    # blue gray cloth with a pale stitched edge.
    cloth = Mark("#2A3040")
    stitch = Mark("#B8B0A0")
    for c in range(14):
        for r in range(3, 14):
            edge = r in (3, 13) or c in (0, 13)
            (stitch if edge and (c + r) % 2 == 0 else cloth).put(at(BODY1, "back", c, r))
    return [cloth, stitch]


def blood_run():
    # Red marks on the flanks: red claw marks and smears on the flanks of the
    # first cube, under the saddle.
    red = Mark("#C01818")
    dark = Mark("#6A0A0A")
    for c in range(0, 20):
        for r in range(3, 14):
            k = (c + r) % 4
            if k == 0:
                red.put(side(BODY1, c, r))
            elif k == 1:
                dark.put(side(BODY1, c, r))
    return [red, dark]


def arrow_hide():
    # Thick, dark patches: thick, near black patches of hide on the flanks
    # of the second cube.
    dark = Mark("#18120E")
    edge = Mark("#3A2E26")
    flanks = sides(BODY2, range(18), range(0, 13))
    pattern([dark, edge], flanks, 2205, (60, 40))
    return [dark, edge]


def horde_hide():
    # A band of illager colors: bands of dark gray blue, dark green, and pale
    # beige, as the coat of an illager, across the back end of the back: the
    # back of the second cube, and the low ring of the first cube.
    blue = Mark("#3A4250")
    green = Mark("#2E4A2A")
    beige = Mark("#D8C8A0")
    colors = (blue, green, beige)
    for c in range(12):
        for r in range(13):
            colors[(r // 2) % 3].put(at(BODY2, "back", c, r))
    x0, y0, w, h = BODY1["bottom"]
    for c in range(w):
        for r in range(h):
            if c in (0, w - 1) or r in (0, h - 1):
                colors[(c + r) % 3].put(at(BODY1, "bottom", c, r))
    return [blue, green, beige]


def blast_pelt():
    # Sooty patches: patches of soot and pale ash on the rump.
    soot = Mark("#1A1816")
    ash = Mark("#A8A098")
    x0, y0, w, h = BODY2["bottom"]
    pattern([soot, ash], cells(BODY2, "bottom", range(w), range(h)), 2207, (50, 20))
    return [soot, ash]


def plate_hide():
    # Iron plates on the back: the tops of the four legs, which stand up
    # beside the back, are riveted iron plates.
    iron = Mark("#8A9096")
    rivet = Mark("#2E3236")
    for leg in LEGS:
        for c in range(8):
            for r in range(8):
                edge = c in (0, 7) or r in (0, 7)
                (rivet if edge and (c + r) % 3 == 0 else iron).put(at(leg, "top", c, r))
    return [iron, rivet]


def spiked_hide():
    # The short spikes are the gene model spiked_hide_spikes.
    return []


def spiked_hide_model():
    # Short spikes on the back: two rows of short, dark spikes with pale
    # points stand along the two top edges of the back, just outside the
    # saddle cloth. On the bone body, before
    # the turn of 90 degrees, the first cube is from x -7 to 7, y -10 to 6,
    # and z -7 to 13. The back of the animal is at z 13, and y goes from the
    # front to the back.
    im = model_image(16, 16)
    paint_box(im, 0, 0, 1, 1, 3, lambda f, x, y: "#E0D8C8" if f == "front" else "#2A2420")
    parts = [{"bone": "body", "uv": [0, 0], "from": [x, y, 12.5], "size": [1, 1, 3], "inflate": 0.1}
             for x in (-7.6, 6.6) for y in (-8.5, -5, -1.5, 2, 5)]
    save_model("spiked_hide_spikes", im, parts)


def rider_rush():
    # The strap is the gene model rider_rush_strap.
    return []


def rider_rush_model():
    # A brown strap round the body: a leather strap, 2 pixels wide, goes
    # round the back end of the first cube (rows 14 and 15), between the
    # front legs and the hind legs, with a brass buckle on each flank. The
    # first cube is from x -7 to 7, y -10 to 6, and z -7 to 13, before the
    # turn.
    im = model_image(128, 64)

    def strap(f, x, y):
        if f in ("right", "left") and 7 <= x <= 10:
            return "#D8A830" if x in (7, 10) else "#7A5A20"
        return "#6A4224" if (x + y) % 5 else "#4A2C16"
    paint_box(im, 0, 0, 15, 2, 21, strap)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-7.5, 4, -7.5], "size": [15, 2, 21]}]
    save_model("rider_rush_strap", im, parts)


# The legs.

def wide_stride():
    # Pale stripes on the legs: pale vertical stripes in rows 4 to 12 of the
    # four sides of each leg.
    pale = Mark("#E4DCCC")
    dark = Mark("#3A302A")
    pale.put(leg_cells(SIDES, (1, 2, 5, 6), range(4, 13)))
    dark.put(leg_cells(SIDES, (3, 4), range(4, 13)))
    return [pale, dark]


def ember_pelt():
    # Dark red legs: rows 13 to 26 of each leg are dark red, with darker red
    # streaks. The metal band of the front legs, rows 20 to 22, stays.
    red = Mark("#7A1A12")
    dark = Mark("#4A0C08")
    for leg in LEGS:
        for f in SIDES:
            for c in range(8):
                for r in range(13, 27):
                    if leg is LEGS[0] and 20 <= r <= 22:
                        continue
                    (dark if (c * 3 + r) % 7 == 0 else red).put(at(leg, f, c, r))
    return [red, dark]


def sure_foot():
    # Dark hooves: the hooves, rows 33 to 36, are near black, with a gray
    # edge on the top row.
    dark = Mark("#141210")
    edge = Mark("#5A5650")
    dark.put(leg_cells(SIDES, range(8), (34, 35, 36)))
    edge.put(leg_cells(SIDES, range(8), (33,)))
    return [dark, edge]


def steady_stance():
    # The wide feet are the gene model steady_stance_feet.
    return []


def steady_stance_model():
    # Wide, pale feet: a wide, flat, pale pad round the foot of each leg, 1
    # pixel wider than the leg on each side and 1 pixel high. On a leg bone,
    # the leg is from x -4 to 4, y 0 to 37, and z -4 to 4.
    im = model_image(64, 32)
    paint_box(im, 0, 0, 10, 1, 10, lambda f, x, y: "#E8E0CC" if f != "bottom" else "#B0A890")
    parts = [{"bone": leg, "uv": [0, 0], "from": [-5, 36, -5], "size": [10, 1, 10]}
             for leg in ("right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg")]
    save_model("steady_stance_feet", im, parts)


GENES = {
    "horn_gore": horn_gore, "shield_crush": shield_crush, "golem_crusher": golem_crusher,
    "gore_rage": gore_rage, "sweep_horns": sweep_horns, "ram_rush": ram_rush,
    "wide_stride": wide_stride, "fence_breaker": fence_breaker, "sure_foot": sure_foot,
    "blood_run": blood_run, "lone_charge": lone_charge, "rider_rush": rider_rush,
    "loud_roar": loud_roar, "fear_roar": fear_roar, "rage_roar": rage_roar,
    "victory_roar": victory_roar, "lone_roar": lone_roar, "war_roar": war_roar,
    "plate_hide": plate_hide, "arrow_hide": arrow_hide, "blast_pelt": blast_pelt,
    "ember_pelt": ember_pelt, "steady_stance": steady_stance, "spiked_hide": spiked_hide,
    "saddle_guard": saddle_guard, "escort_rider": escort_rider, "rider_heal": rider_heal,
    "horde_hide": horde_hide, "raid_scent": raid_scent, "twin_strike": twin_strike,
}

MODEL_GENES = {
    "sweep_horns": (sweep_horns_model, "sweep_horns_horns"),
    "shield_crush": (shield_crush_model, "shield_crush_notches"),
    "fear_roar": (fear_roar_model, "fear_roar_teeth"),
    "war_roar": (war_roar_model, "war_roar_tongue"),
    "escort_rider": (escort_rider_model, "escort_rider_collar"),
    "raid_scent": (raid_scent_model, "raid_scent_nostrils"),
    "lone_charge": (lone_charge_model, "lone_charge_mane"),
    "spiked_hide": (spiked_hide_model, "spiked_hide_spikes"),
    "rider_rush": (rider_rush_model, "rider_rush_strap"),
    "steady_stance": (steady_stance_model, "steady_stance_feet"),
}


if __name__ == "__main__":
    write_all(GENES, MODEL_GENES, REGION_MAP, label="ravager genes")
