"""Make the gene visuals of the slime, from design/species/slime.md.

Usage: python3 tools/make_slime_marks.py (from the root of the repository)

The slime texture is 64x32, and it uses the slime region map. The parts:
- the outer gel: 8x8x8 at uv (0, 0), in SlimeOuterLayer. Its pixels have alpha
  180, and a mark keeps that alpha, so a mark on the gel shows as a tint.
- the core: 6x6x6 at uv (0, 16), the part "cube" of SlimeModel.
- the eyes: 2x2x2 at uv (32, 0) and (32, 4). The mouth: 1x1x1 at uv (32, 8).
The eyes and the mouth are small cubes inside the gel. On the front of the
gel, the eyes are behind columns 1 and 2 and columns 5 and 6 of rows 2 and 3,
and the mouth is behind column 4 of row 5. So a face gene can also paint the
gel in front of the face.
The gene models use the bones cube (the core, which has the same origin as
the gel) and mouth.

Pixel plan for the Giants.
- Great Blob has a darker, thick gel: all the gel is a deep green.
- Heavy Body has a large, dark core: the core is dark green, darkest at its
  edges.
- Thick Gel has thick, cloudy layers in the gel: pale cloudy bands across the
  gel.
- Swell has a swollen core: a pale core with stretched lines.
- Mother Gel: small cubes in the gel, the gene model mother_gel_cubes.
- Wide Reach has a wide, open mouth: a wide, dark mouth on the front of the
  gel, and a dark mouth.

Pixel plan for the Splitters.
- Many Pieces has cracks in the gel: dark lines that split the gel.
- Tiny Teeth has small teeth: pale teeth round the mouth on the front of the
  gel.
- Quick Split has light green streaks across the gel.
- Scatter has a cracked core: dark cracks with pale edges on the core.
- Gel Trap: strings of gel that hang from the sides, the gene model
  gel_trap_strings.
- Bud: small buds of gel on the sides, the gene model bud_buds.

Pixel plan for the Gluers.
- Sticky Hit has a shiny, wet gel: white shine on the gel.
- Gel Trail: drips of gel under the bottom, the gene model gel_trail_drips.
- Gum Skin has a yellow-green gum gel.
- Glue Grip: a long, pink tongue from the mouth, the gene model
  glue_grip_tongue.
- Sticky Pounce has a sticky bottom: the bottom and the low row of the gel are
  a dark, sticky green.
- Cling has gel feet on the sides: pale lumps at the low corners of the gel.

Pixel plan for the Bouncers.
- High Hop has a bright, light gel: all the gel is a pale, bright green.
- Long Hop has long, thin streaks: pale lines up the sides of the gel.
- Quick Hop has wide eyes: white eyes with black pupils, and white rims round
  them on the front of the gel.
- Slam has a heavy core at the bottom: the low rows of the core are dark and
  heavy.
- Water Hop has a blue tint in the gel.
- Acid Landing has a yellow bottom: the bottom and the low row of the gel.

Pixel plan for the Acids.
- Acid Touch has a yellow-green gel.
- Shield Melt has a yellow mouth: a yellow mouth, and yellow on the gel in
  front of it.
- Acid Blood has a yellow core.
- Burning Gel has orange spots in the gel.
- Acid Pool: bubbles in the gel, the gene model acid_pool_bubbles.
- Dissolve has green, sunken eyes: dark green eyes, and dark rings round them
  on the front of the gel.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/slime.png"

GEL = {"top": (8, 0, 8, 8), "bottom": (16, 0, 8, 8), "right": (0, 8, 8, 8), "front": (8, 8, 8, 8),
       "left": (16, 8, 8, 8), "back": (24, 8, 8, 8)}
CORE = {"top": (6, 16, 6, 6), "bottom": (12, 16, 6, 6), "right": (0, 22, 6, 6), "front": (6, 22, 6, 6),
        "left": (12, 22, 6, 6), "back": (18, 22, 6, 6)}
RIGHT_EYE = {"top": (34, 0, 2, 2), "bottom": (36, 0, 2, 2), "right": (32, 2, 2, 2), "front": (34, 2, 2, 2),
             "left": (36, 2, 2, 2), "back": (38, 2, 2, 2)}
LEFT_EYE = {"top": (34, 4, 2, 2), "bottom": (36, 4, 2, 2), "right": (32, 6, 2, 2), "front": (34, 6, 2, 2),
            "left": (36, 6, 2, 2), "back": (38, 6, 2, 2)}
MOUTH = {"top": (33, 8, 1, 1), "bottom": (34, 8, 1, 1), "right": (32, 9, 1, 1), "front": (33, 9, 1, 1),
         "left": (34, 9, 1, 1), "back": (35, 9, 1, 1)}
SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom") + SIDES

# The places on the front of the gel in front of the eyes and the mouth.
GEL_EYES = [(9, 10), (10, 10), (9, 11), (10, 11), (13, 10), (14, 10), (13, 11), (14, 11)]
GEL_MOUTH = (12, 13)

ACID = ("#C8E040", "#E8F070")


def pixels(part, faces):
    """Return each pixel (x, y, face, col, row) of the faces of a part."""
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                yield x0 + col, y0 + row, face, col, row


def tint(part, color, faces=ALL):
    """Return a mark that gives all the faces of a part one color."""
    mark = Mark(color)
    mark.put([(x, y) for x, y, *_ in pixels(part, faces)])
    return mark


def great_blob():
    # A darker, thick gel: all the gel is a deep green, a little paler on the
    # top.
    deep = tint(GEL, "#3A8A30", ("bottom",) + SIDES)
    top = tint(GEL, "#4AA040", ("top",))
    return [deep, top]


def heavy_body():
    # A large, dark core: the core is dark green, darkest at its edges.
    dark = Mark("#2A6A20")
    edge = Mark("#1A4A14")
    for x, y, face, col, row in pixels(CORE, ALL):
        (edge if col in (0, 5) or row in (0, 5) else dark).put((x, y))
    return [dark, edge]


def thick_gel():
    # Thick, cloudy layers in the gel: pale, cloudy bands across the sides of
    # the gel, on rows 1, 2, 5, and 6, with soft edges.
    cloud = Mark("#D8F0D0")
    soft = Mark("#B8E0B0")
    for x, y, face, col, row in pixels(GEL, SIDES):
        n = noise100(x, y, 1901)
        if row in (1, 5):
            (cloud if n < 60 else soft).put((x, y))
        elif row in (2, 6) and n < 40:
            soft.put((x, y))
    return [cloud, soft]


def swell():
    # A swollen core: the core is a bright, pale lime, with stretched pale
    # lines across its faces.
    pale = Mark("#D8FF70")
    line = Mark("#FFFFE8")
    for x, y, face, col, row in pixels(CORE, ALL):
        (line if (col + row) % 3 == 0 else pale).put((x, y), 255)
    return [pale, line]


def mother_gel():
    # The cubes are the gene model mother_gel_cubes.
    return []


def mother_gel_model():
    # Small cubes in the gel: four small slime cubes, each with a tiny dark
    # eye, that float in the gel between the core and the gel round it, at its
    # corners. On the bone cube, the gel is from y 16 to 24 and from x and z
    # -4 to 4, and the core is from -3 to 3.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 2, 2, 2, lambda face, x, y: "#10300C" if face == "front" and y == 0 and x == 1
              else "#2E7A22")
    parts = [{"bone": "cube", "uv": [0, 0], "from": [x, y, z], "size": [2, 2, 2], "inflate": -0.25}
             for x, y, z in ((-3.9, 16.2, -3.9), (1.9, 17.2, -3.9), (-3.9, 21.9, 1.9), (1.9, 21.9, 1.9))]
    save_model("mother_gel_cubes", im, parts)


def wide_reach():
    # A wide, open mouth: a wide, dark mouth on the front of the gel, five
    # pixels wide on rows 5 and 6, and the mouth itself dark.
    dark = Mark("#102010")
    dark.put([(x, y) for x in range(10, 15) for y in (13, 14)])
    dark.put([(x, y) for x, y, *_ in pixels(MOUTH, ALL)])
    return [dark]


def many_pieces():
    # Cracks in the gel: dark green lines that split each face of the gel into
    # pieces.
    crack = Mark("#1E5A18")
    for x, y, face, col, row in pixels(GEL, ALL):
        if col == (2 if row < 4 else 5) or row == (3 if col < 4 else 5):
            crack.put((x, y))
    return [crack]


def tiny_teeth():
    # Small teeth: a row of pale teeth on the front of the gel, round the
    # mouth, with dark gaps between them.
    tooth = Mark("#F4F4E8")
    gap = Mark("#1A3A14")
    tooth.put([(10, 13), (11, 12), (13, 12), (14, 13), (11, 14), (13, 14)])
    gap.put([(11, 13), (13, 13), (12, 14)])
    return [tooth, gap]


def quick_split():
    # Light green streaks across the gel: short, pale green streaks on a
    # slant on the sides and the top of the gel.
    streak = Mark("#C8F8A8")
    for x, y, face, col, row in pixels(GEL, ("top",) + SIDES):
        if (col + row) % 5 == 0 and col % 3 != 2:
            streak.put((x, y))
    return [streak]


def scatter():
    # A cracked core: dark cracks with pale edges on each face of the core.
    crack = Mark("#1A4A14")
    edge = Mark("#A8E890")
    for x, y, face, col, row in pixels(CORE, ALL):
        if (col, row) in ((1, 0), (2, 1), (2, 2), (3, 3), (4, 4), (4, 5), (1, 3), (0, 4)):
            crack.put((x, y))
        elif (col, row) in ((3, 1), (3, 2), (4, 3), (2, 3)):
            edge.put((x, y))
    return [crack, edge]


def gel_trap():
    # The strings are the gene model gel_trap_strings.
    return []


def gel_trap_model():
    # Gel strings on the surface: thin, pale strings of gel that hang from the
    # low edges of the sides, each with a drop at its end. On the bone cube,
    # the gel is from y 16 to 24 and from x and z -4 to 4.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 4, 1, lambda face, x, y: "#F4FFE8" if y == 3 else "#4E9A3A")
    parts = []
    for x, z in ((-4.6, -2), (-4.6, 1.5), (3.6, -1), (3.6, 2), (-1.5, -4.6), (1.8, -4.6), (0, 3.6)):
        parts.append({"bone": "cube", "uv": [0, 0], "from": [x, 20, z], "size": [1, 4, 1], "inflate": -0.2})
    save_model("gel_trap_strings", im, parts)


def bud():
    # The buds are the gene model bud_buds.
    return []


def bud_model():
    # Small buds on the sides: small cubes of gel, as young slimes, that grow
    # out of the sides of the gel, each with a dark eye on its front. On the
    # bone cube, the gel is from x and z -4 to 4.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 2, 2, 2, lambda face, x, y: "#2A5A20" if face == "front" and (x, y) == (0, 0)
              else "#8AD070")
    parts = [{"bone": "cube", "uv": [0, 0], "from": [x, y, z], "size": [2, 2, 2]}
             for x, y, z in ((-5.5, 19, -1), (3.5, 17.5, 0.5), (-1, 20, 3.5))]
    save_model("bud_buds", im, parts)


def sticky_hit():
    # A shiny, wet gel: white shine on the upper corners of the sides and on
    # the top of the gel.
    shine = Mark("#FFFFFF")
    for x, y, face, col, row in pixels(GEL, ("top",) + SIDES):
        if face == "top" and (col + row) % 4 == 0 or face != "top" and (row == 0 and col in (1, 2) or
                                                                         row == 1 and col == 1):
            shine.put((x, y))
    return [shine]


def gel_trail():
    # The drips are the gene model gel_trail_drips.
    return []


def gel_trail_model():
    # Drips at the bottom: drops of gel that hang under the bottom of the gel,
    # long and short. On the bone cube, the bottom of the gel is at y 24.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 2, 1, lambda face, x, y: "#C8F0B8" if y == 1 else "#8AD070")
    parts = [{"bone": "cube", "uv": [0, 0], "from": [x, 23.5, z], "size": [1, 2, 1], "inflate": -0.1}
             for x, z in ((-3, -3), (1, -2.5), (2.5, 1.5), (-2, 2.5), (-0.5, 0))]
    save_model("gel_trail_drips", im, parts)


def gum_skin():
    # A yellow-green gum gel: all the gel is a yellow green.
    return [tint(GEL, "#A8C840")]


def glue_grip():
    # The tongue is the gene model glue_grip_tongue.
    return []


def glue_grip_model():
    # A long tongue: a long, pink tongue that comes out of the mouth, through
    # the front of the gel, and curls down at its tip. On the bone mouth, the
    # mouth is at x 0, y 21, z -3.5.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 1, 4, lambda face, x, y: "#E07090" if face == "top" else "#C05070")
    paint_box(im, 0, 8, 1, 2, 1, lambda face, x, y: "#E07090")
    parts = [
        {"bone": "mouth", "uv": [0, 0], "from": [0, 21.5, -7.5], "size": [1, 1, 4], "inflate": -0.15},
        {"bone": "mouth", "uv": [0, 8], "from": [0, 22.3, -7.8], "size": [1, 2, 1], "inflate": -0.15},
    ]
    save_model("glue_grip_tongue", im, parts)


def sticky_pounce():
    # A sticky bottom: the bottom of the gel and the low row of its sides are a
    # dark, sticky green, with pale strings on the row above.
    sticky = Mark("#2A5A1A")
    string = Mark("#A8D890")
    for x, y, face, col, row in pixels(GEL, ("bottom",) + SIDES):
        if face == "bottom" or row == 7:
            sticky.put((x, y))
        elif row == 6 and col % 3 == 1:
            string.put((x, y))
    return [sticky, string]


def cling():
    # Gel feet on the sides: pale lumps of gel at the low corners of each side
    # of the gel.
    foot = Mark("#C8F0B0")
    for x, y, face, col, row in pixels(GEL, SIDES):
        if row >= 6 and (col <= 1 or col >= 6):
            foot.put((x, y))
    return [foot]


def high_hop():
    # A bright, light gel: all the gel is a pale, bright green.
    return [tint(GEL, "#C8FFB0")]


def long_hop():
    # Long, thin streaks: pale lines up the sides of the gel, on columns 1 and
    # 5, with gaps.
    streak = Mark("#E0FFD0")
    for x, y, face, col, row in pixels(GEL, SIDES):
        if col in (1, 5) and row != 4:
            streak.put((x, y))
    return [streak]


def quick_hop():
    # Wide eyes: the fronts of the eyes are white with black pupils, and white
    # rims go round the eyes on the front of the gel.
    white = Mark("#FFFFFF")
    black = Mark("#0A0A0A")
    for part in (RIGHT_EYE, LEFT_EYE):
        for x, y, face, col, row in pixels(part, ("front",)):
            (black if (col, row) == (1, 1) else white).put((x, y))
    white.put([(8, 10), (11, 10), (12, 10), (15, 10), (9, 9), (10, 9), (13, 9), (14, 9)])
    return [white, black]


def slam():
    # A heavy core at the bottom: the low two rows of the sides of the core and
    # its bottom are dark and heavy.
    heavy = Mark("#1A3A14")
    for x, y, face, col, row in pixels(CORE, ("bottom",) + SIDES):
        if face == "bottom" or row >= 4:
            heavy.put((x, y))
    return [heavy]


def water_hop():
    # A blue tint in the gel.
    return [tint(GEL, "#70C8E0")]


def acid_landing():
    # A yellow bottom: the bottom of the gel and the low row of its sides are
    # yellow.
    yellow = Mark("#E8D030")
    for x, y, face, col, row in pixels(GEL, ("bottom",) + SIDES):
        if face == "bottom" or row == 7:
            yellow.put((x, y))
    return [yellow]


def acid_touch():
    # A yellow-green gel: all the gel is an acid yellow green.
    return [tint(GEL, ACID[0])]


def shield_melt():
    # A yellow mouth: the mouth is yellow, and a wide band of yellow on the
    # gel goes round it, with a drip of acid under it.
    yellow = Mark("#F0D020")
    yellow.put([(x, y) for x, y, *_ in pixels(MOUTH, ALL)])
    yellow.put([(x, 13) for x in range(10, 15)] + [(11, 14), (12, 14), (13, 14), (12, 15)])
    return [yellow]


def acid_blood():
    # A yellow core.
    return [tint(CORE, "#D8C030")]


def burning_gel():
    # Orange spots in the gel: spots of orange, each with a pale middle, on the
    # top and the sides of the gel.
    spot = Mark("#F08030")
    pale = Mark("#FFC070")
    for x, y, face, col, row in pixels(GEL, ("top",) + SIDES):
        n = noise100(x // 2, y // 2, 1903)
        if n < 10:
            pale.put((x, y))
        elif n < 22:
            spot.put((x, y))
    return [spot, pale]


def acid_pool():
    # The bubbles are the gene model acid_pool_bubbles.
    return []


def acid_pool_model():
    # Bubbles in the gel: small, pale yellow-green bubbles that float in the
    # gel round the core, each with a bright point. On the bone cube, the gel is
    # from y 16 to 24 and from x and z -4 to 4, and the core is from -3 to 3.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 1, 1, lambda face, x, y: ACID[1] if face == "top" else ACID[0])
    spots = ((-3.6, 17, -3.6), (2.9, 18.5, -3.7), (-1, 16.4, -3.7), (3, 22, 1), (-3.7, 21, 2.2), (1.2, 23, -3.7),
             (-2.5, 19.5, 3.0))
    parts = [{"bone": "cube", "uv": [0, 0], "from": [x, y, z], "size": [1, 1, 1], "inflate": -0.2} for x, y, z in spots]
    save_model("acid_pool_bubbles", im, parts)


def dissolve():
    # Green, sunken eyes: the eyes are dark green, and dark rings go round
    # them on the front of the gel, so that they look sunk in.
    eye = Mark("#1A4A10")
    ring = Mark("#2A5A20")
    for part in (RIGHT_EYE, LEFT_EYE):
        eye.put([(x, y) for x, y, *_ in pixels(part, ALL)])
    ring.put(GEL_EYES)
    ring.put([(9, 12), (10, 12), (13, 12), (14, 12)])
    return [eye, ring]


GENES = {
    "great_blob": great_blob,
    "heavy_body": heavy_body,
    "thick_gel": thick_gel,
    "swell": swell,
    "mother_gel": mother_gel,
    "wide_reach": wide_reach,
    "many_pieces": many_pieces,
    "tiny_teeth": tiny_teeth,
    "quick_split": quick_split,
    "scatter": scatter,
    "gel_trap": gel_trap,
    "bud": bud,
    "sticky_hit": sticky_hit,
    "gel_trail": gel_trail,
    "gum_skin": gum_skin,
    "glue_grip": glue_grip,
    "sticky_pounce": sticky_pounce,
    "cling": cling,
    "high_hop": high_hop,
    "long_hop": long_hop,
    "quick_hop": quick_hop,
    "slam": slam,
    "water_hop": water_hop,
    "acid_landing": acid_landing,
    "acid_touch": acid_touch,
    "shield_melt": shield_melt,
    "acid_blood": acid_blood,
    "burning_gel": burning_gel,
    "acid_pool": acid_pool,
    "dissolve": dissolve,
}

# Each gene with a model: the maker of the model and its name.
MODEL_GENES = {
    "mother_gel": (mother_gel_model, "mother_gel_cubes"),
    "gel_trap": (gel_trap_model, "gel_trap_strings"),
    "bud": (bud_model, "bud_buds"),
    "gel_trail": (gel_trail_model, "gel_trail_drips"),
    "glue_grip": (glue_grip_model, "glue_grip_tongue"),
    "acid_pool": (acid_pool_model, "acid_pool_bubbles"),
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
    print("pixels that two or more slime genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
