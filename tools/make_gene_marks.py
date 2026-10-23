"""Make the marks of the zombie genes.

Usage: python3 tools/make_gene_marks.py

A mark is a 64x64 gray pattern with alpha. The pattern effect stamps pattern
pixel (x % 64, y % 64) on texture pixel (x, y), so a 64x64 pattern puts each
mark on exact pixels of the zombie texture. The tint gives the color, and the
gray gives the shade: gray 255 is the tint, and a lower gray is darker.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- gene_visuals/<gene>.json, with one pattern layer for each pattern.
The script does not write the visuals of the genes that are not in GENES.

The script prints the pixels that two genes share. Each gene has its own
pixels, so that a zombie can carry 5 genes and each mark stays visible.

Pixel plan. The UV is the vanilla zombie UV. The arms and the legs are
4 pixels on each side. Their side faces are, from left to right in the
texture: outer, front, inner, back. Rows count from the top of a side face.
Cinder keeps the hands and the feet. Bonerunner erases the lower legs and
the feet, so no other mark goes there.
- Face, eyes: Nightborn has the outer eye pixels. Miasma has the inner eye
  pixels.
- Face, around the eyes: Rabid has the row over the eyes, the pixels under
  the outer eye corners, and the cheek edges (x 8 and 15, y 12 and 13).
- Face, nose: Bloodscent has the nose bridge (y 12) and the nose (y 13).
- Face, mouth: Rabid has the mouth corners (x 9 and 14) and the jaw edges
  (x 8 and 15, y 15). Hush has the lips (y 14, x 10 to 13). Packcaller has
  the jaw (y 15, x 10 to 13). Leech has no face pixels yet.
- Shirt and sleeves: Ironhide covers all the cloth at order -5, so the
  marks of other genes go on top of its plates.
- Shirt: Frenzy tears the cloth away at the bottom, with a deep rip in the
  middle of the chest (x 23 and 24). Bloodscent has the cloth over the rip
  (x 23 and 24, y 22 to 24) and x 38 and 39 in rows 1 and 2 of the back.
- Sleeves: Frenzy has rows 2 and 3.
- Arm skin, rows 4 to 6: Frenzy has the front face.
- Legs: Lunger has the knees, rows 2 to 5 of all four faces.
- Death Throes has no marks. Its look is the fuse, a gene model.
This script needs Pillow.
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns")
MARK_DIR = os.path.join(ASSETS, "textures/gene_marks")
VISUAL_DIR = os.path.join(ASSETS, "gene_visuals")

# The side faces of a limb, as the x offset from the start of the limb UV.
# The first side face in the texture is the outer side of the limb, as in
# the vanilla box UV: the right side (-x) of the right limb.
OUTER, FRONT, INNER, BACK = 0, 4, 8, 12
SIDES = (OUTER, FRONT, INNER, BACK)

# The UV of the limbs. The vanilla zombie uses the right limbs for both
# sides. The drowned has its own left limbs. A mark on a limb goes on both.
ARMS = ((40, 16), (32, 48))
LEGS = ((0, 16), (16, 48))


def arm(face, col, row):
    """Return the texture pixels of a pixel on a side face of each arm."""
    return [(u + face + col, v + 4 + row) for u, v in ARMS]


def leg(face, col, row):
    return [(u + face + col, v + 4 + row) for u, v in LEGS]


class Mark:
    """One color of a mark: a tint and the gray of each pixel.

    The order puts the layer before (lower) or after (higher) the layers of
    other genes. The regions and the materials limit the layer, as in a gene
    visual. A mark of size 16 is a tile that covers the whole texture. A
    mark with a height is that many pixels high, for a texture that is not
    square. A mark with the target "outer" goes on the outer layer of the
    stray.
    """

    def __init__(self, tint, alpha=255, order=0, regions=None, materials=None, size=64, target=None, height=None):
        self.tint = tint
        self.target = target
        self.alpha = alpha
        self.order = order
        self.regions = regions
        self.materials = materials
        self.size = size
        # A mark is square, unless it has a height. The witch texture is
        # 64x128, and a pattern repeats over the texture, so its marks are
        # 64 wide and 128 high.
        self.height = height or size
        self.pixels = {}

    def put(self, pixels, gray=255):
        for p in pixels if isinstance(pixels, list) else [pixels]:
            self.pixels[p] = gray
        return self

    def fill(self, gray=255):
        for y in range(self.height):
            for x in range(self.size):
                self.pixels[(x, y)] = gray
        return self


def head_side(side, col, row):
    """Return a pixel of a side of the head. Column 0 is next to the face."""
    return (7 - col, 8 + row) if side == 0 else (16 + col, 8 + row)


# The faces of the body: the top left pixel of each face.
CHEST, SHIRT_BACK, RIGHT_SIDE, LEFT_SIDE = (20, 20), (32, 20), (16, 20), (28, 20)


def body(face, col, row):
    return (face[0] + col, face[1] + row)


# The color of zombie skin, for the skin that shows through a tear.
SKIN = "#71955B"


def lunger():
    # Strained, torn knees: the cloth is torn all around the knee, with holes,
    # red scrapes, and frayed threads from row 2 to row 5.
    hole = Mark("#16121E")
    scrape = Mark("#9A2A22")
    thread = Mark("#B8B2C8")
    hole.put(leg(FRONT, 1, 4)).put(leg(FRONT, 2, 5))
    scrape.put(leg(FRONT, 2, 4)).put(leg(FRONT, 1, 5), 200)
    thread.put(leg(FRONT, 0, 4)).put(leg(FRONT, 3, 5), 210).put(leg(FRONT, 1, 3), 190)
    thread.put(leg(FRONT, 2, 2), 170)
    hole.put(leg(OUTER, 1, 3)).put(leg(OUTER, 1, 4)).put(leg(OUTER, 2, 5))
    thread.put(leg(OUTER, 2, 4), 200).put(leg(OUTER, 0, 5), 190).put(leg(OUTER, 1, 2), 170)
    hole.put(leg(BACK, 1, 4)).put(leg(BACK, 2, 4))
    scrape.put(leg(BACK, 2, 5), 200)
    thread.put(leg(BACK, 0, 3), 190).put(leg(BACK, 3, 5), 170)
    hole.put(leg(INNER, 2, 3))
    thread.put(leg(INNER, 1, 4), 190).put(leg(INNER, 2, 2), 170)
    return [hole, scrape, thread]


def ruin(tear, skin, face, depths):
    """Tear away the bottom of a shirt face, like the shirt of the drowned.

    depths gives, for each column, how many rows of cloth are gone at the
    bottom of the shirt (row 9). The skin shows there. The row over the gap
    gets a dark torn edge, if the depth is 1 to 5.
    """
    for col, depth in enumerate(depths):
        for d in range(depth):
            skin.put(body(face, col, 9 - d), 255 if (col + d) % 2 else 225)
        if depth and depth < 6 and depth not in NO_EDGE.get((face, col), ()):
            tear.put(body(face, col, 9 - depth), 220)


# Shirt pixels where a torn edge would go on an Ironhide plate. The plate
# seam is the edge there.
NO_EDGE = {(CHEST, c): (2,) for c in (0, 1, 2, 5, 6, 7)}
NO_EDGE.update({(SHIRT_BACK, c): (2,) for c in range(1, 7)})


def frenzy():
    # A ruined shirt, like the shirt of the drowned: the cloth is torn away
    # at the bottom of the shirt and at the sleeves, so the skin shows, with
    # a deep rip in the middle of the chest. Claw scratches on the forearms.
    tear = Mark("#121818")
    skin = Mark(SKIN)
    for face in SIDES:
        tear.put(arm(face, 0, 2)).put(arm(face, 3, 2)).put(arm(face, 2, 3), 200)
        skin.put(arm(face, 0, 3)).put(arm(face, 1, 2), 225).put(arm(face, 1, 3)).put(arm(face, 3, 3), 225)
    ruin(tear, skin, CHEST, (1, 2, 1, 5, 4, 2, 1, 2))
    ruin(tear, skin, SHIRT_BACK, (3, 1, 2, 1, 2, 1, 2, 3))
    ruin(tear, skin, RIGHT_SIDE, (2, 3, 1, 2))
    ruin(tear, skin, LEFT_SIDE, (1, 2, 3, 2))
    scratch = Mark("#8E1A1A")
    for col, row in ((0, 4), (1, 5), (2, 6), (2, 4), (3, 5)):
        scratch.put(arm(FRONT, col, row), 255 if row < 6 else 190)
    return [tear, skin, scratch]


def bloodscent():
    # A flared, sniffing snout: wrinkles on the nose bridge, wide nostrils,
    # and dark red streaks in the nostrils.
    snout = Mark("#3E4A26")
    snout.put((11, 12), 230).put((12, 12), 230).put((10, 13), 255).put((13, 13), 255)
    streak = Mark("#7A1414")
    streak.put((11, 13), 255).put((12, 13), 200)
    stain = Mark("#5A0E0E")
    # Blood that dripped from the snout onto the cloth in the middle of the
    # chest, over the Frenzy rip, and a stain high on the back.
    stain.put(body(CHEST, 3, 2)).put(body(CHEST, 4, 2), 200).put(body(CHEST, 4, 3))
    stain.put(body(CHEST, 4, 4), 180)
    stain.put(body(SHIRT_BACK, 6, 1)).put(body(SHIRT_BACK, 7, 2), 200)
    return [snout, streak, stain]


def rabid():
    # A feverish look: red rims around the eyes and flushed cheeks, and foam
    # at the corners of the mouth that runs along the jaw.
    rim = Mark("#B42828")
    for x in (9, 10, 13, 14):
        rim.put((x, 11), 210)
    rim.put((9, 13), 255).put((14, 13), 255)
    fever = Mark("#C2554A")
    for x in (8, 15):
        fever.put((x, 12), 230).put((x, 13), 255)
    foam = Mark("#ECEFE2", alpha=235)
    foam.put((9, 15), 255).put((14, 15), 255).put((8, 15), 225).put((15, 15), 225)
    # Thin foam over the mouth corners, so that the foam is not a hard block.
    bubbles = Mark("#ECEFE2", alpha=140)
    bubbles.put((9, 14)).put((14, 14))
    return [rim, fever, foam, bubbles]


def hush():
    # The mouth is sewn shut: pale stitches across the dark line of the lips.
    thread = Mark("#A88B5C")
    thread.put((10, 14)).put((12, 14), 225)
    seam = Mark("#1A100A")
    seam.put((11, 14)).put((13, 14))
    return [thread, seam]


# The shapes of the Shade patches, as (col, row) offsets.
PATCH_SHAPES = (((0, 0), (1, 0), (0, 1)), ((0, 0), (1, 0), (1, 1), (0, 1)),
                ((0, 0), (0, 1), (1, 1)), ((0, 0), (1, 0)), ((1, 0), (0, 1), (1, 1), (2, 1)))


def patches(faces, rows, count, seed):
    """Yield (face, col, row, gray) for small patches on the faces of a limb.

    Each face gets count patches of 2 to 4 pixels, in the given rows.
    """
    first, last = rows[0], rows[-1]
    for f, face in enumerate(faces):
        for n in range(count):
            h = (seed * 31 + f * 7 + n * 13) * 2654435761 % 4294967296
            shape = PATCH_SHAPES[h % len(PATCH_SHAPES)]
            col = (h >> 8) % 3
            row = first + (h >> 12) % (last - first)
            for dc, dr in shape:
                if col + dc < 4 and row + dr <= last:
                    yield face, col + dc, row + dr, 255 if (h >> 20) % 3 else 215


def shade():
    # Dark, mottled patches on the arms and the legs. They are near black and
    # opaque, so that they keep the most contrast when the zombie is
    # translucent at night. They go under the marks of other genes (order -2).
    dark = Mark("#0E1014", order=-2)
    for face, col, row, gray in patches(SIDES, range(4, 8), 2, 3):
        dark.put(arm(face, col, row), gray)
    for face, col, row, gray in patches(SIDES, range(0, 10), 4, 11):
        dark.put(leg(face, col, row), gray)
    return [dark]


def burrower():
    # Burrower has no marks on the texture. Its dirt is the gene model
    # burrower_dirt, a shell over the lower legs, so that it also covers the
    # bone legs of Bonerunner.
    return []


def lurker():
    # The shade under the edge of the hood, on the upper half of the face. The
    # hood itself is the gene model lurker_hood. The shade goes under the
    # marks of other genes (order -1), and it leaves the eyes as they are.
    shade = Mark("#0E100B", alpha=170, order=-1)
    soft = Mark("#0E100B", alpha=90, order=-1)
    for x in range(8, 16):
        for y in (8, 9, 10):
            shade.put((x, y))
        soft.put((x, 11))
    return [shade, soft]


def nightborn():
    # A pale face, under the marks of other genes (order -1). The bright eyes
    # are in the gene model nightborn_eyes, which glows in the dark.
    pale = Mark("#D2DCC0", alpha=110, order=-1, regions=["face"], materials=["skin"], size=16).fill()
    return [pale]


def thickskull():
    # The cheekbones show as bare bone, with a raw, dark edge of skin around
    # them: on each outer cheek and on the next side of the head. The
    # forehead plate is the gene model thickskull_plate. The marks
    # go at order -1, so that the Rabid cheeks go on top of them, and the raw
    # edge goes on top of the Lurker shade.
    bone = Mark("#CEC4A8", order=-1)
    raw = Mark("#7A2E24", order=-1)
    for face_x, side in ((8, 0), (15, 1)):
        bone.put((face_x, 12)).put((face_x, 13), 225)
        bone.put(head_side(side, 0, 4), 235).put(head_side(side, 0, 5)).put(head_side(side, 1, 5), 215)
        raw.put((face_x, 11), 230).put((face_x, 14))
        raw.put(head_side(side, 0, 3), 230).put(head_side(side, 1, 4)).put(head_side(side, 0, 6), 215)
        raw.put(head_side(side, 2, 5), 200).put(head_side(side, 1, 6), 230)
    # A patch on the back of the head where the skin is gone and the skull
    # shows, with a suture line and a crack, and a raw edge around it. Only
    # the Runt hair uses the back of the head, in its top 2 rows.
    skull = Mark("#CEC4A8", order=-1)
    suture = Mark("#6E6248", order=-1)
    for x, y in ((26, 11), (27, 11), (28, 11), (26, 12), (27, 12), (28, 12), (29, 12),
                 (26, 13), (27, 13), (28, 13), (27, 14)):
        skull.put((x, y), 255 if (x + y) % 2 else 225)
    suture.put((27, 11)).put((27, 12)).put((28, 13), 230)
    skull.pixels.pop((27, 11)), skull.pixels.pop((27, 12)), skull.pixels.pop((28, 13))
    for p in ((26, 10), (27, 10), (28, 10), (25, 11), (29, 11), (25, 12), (30, 12),
              (25, 13), (29, 13), (26, 14), (28, 14), (27, 15)):
        raw.put(p, 230 if sum(p) % 2 else 200)
    return [bone, raw, skull, suture]


def blob(core, rim, center, ring):
    """Put a swollen blotch: a light core pixel and a darker ring around it."""
    core.put(center)
    for p in ring:
        rim.put(p, 230)


def bloat():
    # Swollen green blotches on the shirt, on pixels that no other gene marks.
    # The swollen body is the gene model bloat_body. It uses the mob texture,
    # so these blotches and the marks of other genes show on it.
    core = Mark("#9DB64C")
    rim = Mark("#6A852A")
    blob(core, rim, (21, 24), [(21, 23), (22, 23), (22, 24), (21, 25)])
    blob(core, rim, (26, 25), [(25, 25), (26, 26), (25, 26)])
    blob(core, rim, (35, 24), [(34, 24), (35, 25), (36, 24)])
    blob(core, rim, (37, 26), [(37, 25), (38, 26)])
    blob(core, rim, (17, 23), [(18, 23), (17, 24)])
    blob(core, rim, (29, 24), [(30, 24), (29, 25)])
    return [core, rim]


def stoneblood():
    # The lower legs and the feet turn to gray stone, with light and dark
    # specks. This goes under the marks of other genes (order -3). The gene
    # model stoneblood_shell adds chips of stone that also cover the bone
    # legs of Bonerunner.
    legs = {"regions": ["lower_legs", "feet"]}
    gray = dict(legs, effect={"type": "desaturate", "amount": 1.0, "darken": 0.1}, order=-3)
    speck = Mark("#9A9A9A", alpha=110, order=-3, size=16, **legs)
    for y in range(16):
        for x in range(16):
            n = (x * 73856093 ^ y * 19349663) * 2654435761 % 4294967296 % 100
            speck.put((x, y), 255 if n < 30 else 120 if n < 55 else 190)
    return [gray, speck]


def blastproof():
    # Blast scars. The cloth of the shirt, the sleeves, and the pants loses
    # its color (order -7), and a heavy scorch darkens it (order -6). Both go
    # under the Ironhide plates (order -5),
    # so that the plates stay readable. Charred holes with burnt edges are on
    # the edges of the shirt, on the belt, and on the top of the pants. Soot
    # covers the hands, under the Grave Call earth and the Cinder spots
    # (order -2).
    cloth = {"regions": ["torso", "arms", "legs", "lower_legs"], "materials": ["cloth"]}
    gray = dict(cloth, effect={"type": "desaturate", "amount": 0.75, "darken": 0.2}, order=-7)
    scorch = Mark("#2A1E16", alpha=170, order=-6, size=16, **cloth)
    for y in range(16):
        for x in range(16):
            scorch.put((x, y), (255, 200, 150, 230)[(x * 7 + y * 11) % 4])
    hole = Mark("#120E0C")
    burnt = Mark("#4A3020")
    ember = Mark("#A04A1E")

    def char(center, edges, glow=None):
        hole.put(center)
        for p in edges:
            burnt.put(p, 230)
        if glow:
            ember.put(glow, 210)
    # The edges of the shirt, front and back.
    char((20, 22), [(20, 21), (20, 23)])
    char((27, 25), [(27, 24), (27, 26)], (27, 27))
    char((32, 21), [(32, 20), (33, 21)])
    char((39, 24), [(39, 23), (39, 25)], (38, 24))
    # The belt, front and back.
    char((22, 30), [(21, 30), (23, 30), (22, 31)])
    char((25, 31), [(26, 31)], (24, 31))
    char((34, 31), [(33, 31), (35, 31)])
    char((38, 30), [(38, 31)], (37, 31))
    # The top of the pants.
    for face, col in ((FRONT, 1), (OUTER, 2), (BACK, 1)):
        hole.put(leg(face, col, 0))
        burnt.put(leg(face, col + 1, 0), 230).put(leg(face, col, 1), 230)
    ember.put(leg(FRONT, 0, 1), 200)
    soot = Mark("#1E1A17", alpha=200, order=-2, regions=["hands"])
    for face in SIDES:
        for col in range(4):
            for row in range(8, 12):
                if (face + col * 3 + row * 5) % 4:
                    soot.put(arm(face, col, row), (255, 210, 170)[(col + row) % 3])
    return [gray, scorch, hole, burnt, ember, soot]


def mender():
    # Bold, stitched scars: a thick dark seam with pale cross stitches and a
    # strip of pink new skin, on the outer and the inner side of each arm,
    # and one across the neck. The arm scars stop over row 7, where the
    # Rally band is. A bandage is wrapped around the shins.
    seam = Mark("#3A1E16")
    thread = Mark("#E0D2A8")
    pink = Mark("#C98A84")
    for face in (OUTER, INNER):
        for row in range(4, 7):
            seam.put(arm(face, 1, row))
            pink.put(arm(face, 2, row), 255 if row % 2 else 225)
            thread.put(arm(face, 0 if row % 2 == 0 else 3, row))
    seam.put((23, 20)).put((24, 20))
    pink.put((22, 20)).put((25, 20), 225)
    thread.put((23, 21)).put((24, 21), 225)
    # A bandage wrapped around the shins, with a spot of old blood. The legs
    # share one texture, so both shins have it.
    bandage = Mark("#DCD5C2")
    blood = Mark("#8A2A22")
    for face in SIDES:
        for col in range(4):
            bandage.put(leg(face, col, 7), 255 if (col + face) % 3 else 215)
            bandage.put(leg(face, col, 8), 230 if col % 2 else 200)
    blood.put(leg(FRONT, 2, 8))
    bandage.pixels.pop(leg(FRONT, 2, 8)[0], None)
    bandage.pixels.pop(leg(FRONT, 2, 8)[1], None)
    return [seam, thread, pink, bandage, blood]


def packcaller():
    # The jaw hangs open: the jaw row of the face is the dark inside of the
    # mouth. The lower jaw is the gene model packcaller_jaw.
    mouth = Mark("#1A0F0C")
    for x in range(10, 14):
        mouth.put((x, 15), 255 if x in (11, 12) else 200)
    return [mouth]


def brood():
    # The ribs show through: a spine and two ribs on the upper back, and one
    # rib on each side of the shirt, with dark gaps between the bones.
    bone = Mark("#D8D0B8")
    gap = Mark("#2A2420")
    for x in (35, 36):
        bone.put((x, 20)).put((x, 22))
    for x in (34, 37):
        gap.put((x, 22))
    for x in range(34, 38):
        bone.put((x, 21), 235).put((x, 23), 215)
    for face in (RIGHT_SIDE, LEFT_SIDE):
        for col in range(4):
            gap.put(body(face, col, 1))
            bone.put(body(face, col, 2), 235 if col % 2 else 215)
    return [bone, gap]


def rally():
    # A red band of cloth around each forearm, in the last row of the arm,
    # with two loose ends on the back. It stops where the Mender seam is.
    band = Mark("#9B2D23")
    for face in SIDES:
        for col in range(4):
            if (face, col) != (OUTER, 1):
                band.put(arm(face, col, 7), 255 if col % 2 else 200)
    band.put(arm(BACK, 1, 6), 220).put(arm(BACK, 2, 6), 180)
    return [band]


def runt():
    # Thin tufts of hair that lie flat on the scalp: short strands on the top
    # and on the upper rows of the sides and the back of the head. Most
    # strands are in x 9 to 14 and y 3 to 7: there the top of the adult head
    # and the top of the baby head (BabyZombieModel) share their pixels, so
    # the hair shows on Runt babies too.
    hair = Mark("#3E3128")
    light = Mark("#5A4838")
    strands = [(9, 3), (11, 4), (13, 3), (10, 6), (12, 6), (14, 5), (9, 1), (13, 0), (15, 2)]
    for x, y in strands:
        hair.put((x, y))
        light.put((x + 1 if x < 15 else x - 1, y + 1 if y < 7 else y - 1), 230)
    for x in (1, 4, 6, 17, 19, 22, 25, 28, 30):
        hair.put((x, 8))
        if x % 3:
            light.put((x, 9), 220)
    return [hair, light]


def grave_call():
    # Dark grave earth caked on the hands. It goes under the Cinder spots
    # (order -1), so both show on one zombie.
    earth = Mark("#3B2A1C", alpha=230, order=-1)
    for face in SIDES:
        for col in range(4):
            for row in range(8, 12):
                n = (face * 13 + col * 7 + row * 29) % 10
                if n < 3 + (row - 8) * 2:
                    earth.put(arm(face, col, row), (255, 200, 160)[n % 3])
    for col in range(4):
        for row in range(4):
            if (col + row) % 3:
                for u, v in ARMS:
                    earth.put((u + 8 + col, v + row), 220)
    return [earth]


def siege():
    # Battered fists: a dark bruise over the hands (order -3, under the
    # Blastproof soot, the Grave Call earth, and the Cinder spots), split,
    # raw knuckles on the fist, and a few short splinters stuck flat in the
    # hands. The knuckles and the splinters go on top of the other hand
    # marks (order 1). All of it is in the hand rows.
    bruise = Mark("#3A2433", alpha=170, order=-3, regions=["hands"])
    for face in SIDES:
        for col in range(4):
            for row in range(8, 12):
                bruise.put(arm(face, col, row), (255, 215, 235)[(col + row + face) % 3])
    for col in range(4):
        for row in range(4):
            for u, v in ARMS:
                bruise.put((u + 8 + col, v + row), 235)
    split = Mark("#8E1E1A", order=1)
    raw = Mark("#C76A60", order=1)
    for u, v in ARMS:
        # The fist: the hand end. Two rows of split knuckles.
        for col in range(4):
            split.put((u + 8 + col, v + 1), 255 if col % 2 else 220)
        raw.put((u + 8, v + 2)).put((u + 11, v + 2), 225).put((u + 9, v), 220)
    for face in (FRONT, OUTER):
        split.put(arm(face, 1, 11)).put(arm(face, 3, 11), 220)
    wood = Mark("#B08850", order=1)
    tip = Mark("#5E4426", order=1)
    wood.put(arm(FRONT, 0, 8)).put(arm(FRONT, 1, 9), 225)
    tip.put(arm(FRONT, 2, 10))
    wood.put(arm(OUTER, 2, 8), 225).put(arm(OUTER, 1, 9))
    tip.put(arm(OUTER, 0, 10))
    return [bruise, split, raw, wood, tip]


def noise100(x, y, seed):
    return (x * 73856093 ^ y * 19349663 ^ seed * 83492791) * 2654435761 % 4294967296 % 100


def rotgut():
    # Green and black rot on the shirt and the sleeves. It goes with the
    # Blastproof scorch (order -6), under the Ironhide plates (order -5), so
    # the plates stay readable and the marks of other genes go on top.
    cloth = {"regions": ["torso", "arms"], "materials": ["cloth"]}
    green = Mark("#4E6A22", alpha=220, order=-6, **cloth)
    black = Mark("#16180E", alpha=225, order=-6, **cloth)
    for y in range(16, 48):
        for x in range(16, 56):
            n = noise100(x // 2, y // 2, 9)
            if n < 18:
                green.put((x, y), 255 if noise100(x, y, 10) % 2 else 215)
            elif n < 26:
                black.put((x, y))
    return [green, black]


def contagion():
    # Green, wet sores on the skin of the arms: a dark rim, a green middle,
    # and a bright wet spot. They go under the Frenzy scratches, the Mender
    # scars, and the Rally band (order -1), and over the Shade patches.
    rim = Mark("#2E3A12", order=-1)
    sore = Mark("#7FA83A", order=-1)
    wet = Mark("#D2EE78", order=-1)
    sores = ((OUTER, 2, 5), (FRONT, 1, 4), (INNER, 2, 5), (BACK, 1, 4), (FRONT, 3, 6))
    for face, col, row in sores:
        wet.put(arm(face, col, row))
        for dc, dr in ((-1, 0), (0, 1), (1, 0), (0, -1)):
            c, r = col + dc, row + dr
            if 0 <= c < 4 and 4 <= r <= 7:
                (sore if (dc + dr) % 2 else rim).put(arm(face, c, r), 230)
    return [rim, sore, wet]


def sporebearer():
    # Sporebearer has no marks on the texture. Its fungus is the gene model
    # sporebearer_fungus.
    return []


def leech():
    # Dark tendrils, like veins, that start at the hand and climb up the arm
    # in curves: on the front, the outer side, and the back of each arm. They
    # go under the Rally band, the Mender scars, and the Frenzy scratches
    # (order -1), as veins under the skin, and they keep away from the
    # Contagion sores, which have the same order.
    vein = Mark("#4A1630", order=-1)
    light = Mark("#7A2850", order=-1)
    paths = {
        FRONT: ((0, 11), (0, 10), (1, 9), (1, 8), (0, 7), (0, 6), (0, 5)),
        OUTER: ((3, 11), (3, 10), (2, 9), (2, 8), (1, 7), (1, 6), (0, 5), (0, 4)),
        BACK: ((1, 11), (1, 10), (2, 9), (2, 8), (3, 7), (3, 6), (3, 5), (3, 4)),
    }
    for face, path in paths.items():
        for n, (col, row) in enumerate(path):
            (light if n % 3 == 2 else vein).put(arm(face, col, row))
    return [vein, light]


def miasma():
    # Lime green eyes, on the inner eye pixels (Nightborn has the outer
    # ones), and thin gloves of slime: a translucent lime layer over the
    # hands that fades into the arm. The gloves go over the other hand and
    # arm marks (order 2), but they are thin, so those marks show through.
    eyes = Mark("#A6FF3C")
    eyes.put((10, 12)).put((13, 12))
    slime = Mark("#8FE03A", alpha=95, order=2, regions=["hands"], size=16).fill()
    shine = Mark("#D8FF9A", alpha=150, order=2)
    for face in (FRONT, OUTER):
        shine.put(arm(face, 1, 8)).put(arm(face, 2, 10), 220)
    # Above the glove, the slime fades into the arm over 3 rows: each row
    # has fewer slime pixels, and they are fainter.
    fade = []
    for row, alpha, keep in ((7, 75, (0, 1, 2)), (6, 55, (0, 2)), (5, 35, (1,))):
        mark = Mark("#8FE03A", alpha=alpha, order=2)
        for face in SIDES:
            for col in range(4):
                if (col + row + face // 4) % 4 in keep:
                    mark.put(arm(face, col, row))
        fade.append(mark)
    return [eyes, slime, shine] + fade


def plague_carrier():
    # Gray-green blotches on the skin: a light gray-green middle with a dark
    # rim, so that they show on green skin. They go under all other marks
    # (order -2), so they never hide one.
    skin = {"materials": ["skin"]}
    core = Mark("#A3AC93", alpha=230, order=-2, **skin)
    rim = Mark("#4C5644", alpha=220, order=-2, **skin)
    for y in range(64):
        for x in range(64):
            n = noise100(x // 2, y // 2, 21)
            if n < 20:
                inner = noise100(x // 2, y // 2, 24) < 60 and noise100(x, y, 22) < 75
                (core if inner else rim).put((x, y), 255 if noise100(x, y, 23) % 3 else 215)
    return [core, rim]


GENES = {
    "lunger": lunger,
    "frenzy": frenzy,
    "bloodscent": bloodscent,
    "rabid": rabid,
    "hush": hush,
    "shade": shade,
    "burrower": burrower,
    "lurker": lurker,
    "nightborn": nightborn,
    "thickskull": thickskull,
    "bloat": bloat,
    "stoneblood": stoneblood,
    "blastproof": blastproof,
    "mender": mender,
    "packcaller": packcaller,
    "brood": brood,
    "rally": rally,
    "runt": runt,
    "grave_call": grave_call,
    "siege": siege,
    "rotgut": rotgut,
    "contagion": contagion,
    "sporebearer": sporebearer,
    "leech": leech,
    "miasma": miasma,
    "plague_carrier": plague_carrier,
}

# The zombie genes with a visual that is written by hand, not by this script.
# The overlap check looks at them too. The visuals of other species are not
# in the check.
HAND_WRITTEN = ("cinder", "ironhide", "bonerunner")

# The gene models of the genes in GENES.
MODELS = {
    "burrower": "originofspawns:burrower_dirt",
    "lurker": "originofspawns:lurker_hood",
    "nightborn": "originofspawns:nightborn_eyes",
    "thickskull": "originofspawns:thickskull_plate",
    "bloat": "originofspawns:bloat_body",
    "packcaller": "originofspawns:packcaller_jaw",
    "sporebearer": "originofspawns:sporebearer_fungus",
    "stoneblood": "originofspawns:stoneblood_shell",
}


def write(gene, marks, models=None, region_map=None):
    """Write the patterns and the visual of a gene.

    models gives the gene model of each gene. The default is MODELS.
    region_map is the region map of the species, if it is not the humanoid
    map.
    """
    models = MODELS if models is None else models
    # Two species must not use the same gene id. A visual of the same name
    # with a different region map belongs to a gene of another species, so
    # stop before the write replaces it.
    path = os.path.join(VISUAL_DIR, gene + ".json")
    if os.path.exists(path):
        with open(path) as fh:
            old_map = json.load(fh).get("region_map")
        if old_map != region_map:
            raise SystemExit(f"{gene}: the visual belongs to another species ({old_map}). Use another id.")
    # Remove the old patterns of the gene, so that no unused file stays.
    for old in os.listdir(MARK_DIR):
        if old.startswith(gene + "_") and old[len(gene) + 1:-4].isdigit():
            os.remove(os.path.join(MARK_DIR, old))
    layers = []
    n = 0
    for mark in marks:
        if isinstance(mark, dict):
            # A layer that is not a pattern, for example a desaturate layer.
            layers.append(mark)
            continue
        im = Image.new("RGBA", (mark.size, mark.height), (0, 0, 0, 0))
        for (x, y), gray in mark.pixels.items():
            im.putpixel((x, y), (gray, gray, gray, mark.alpha))
        name = f"{gene}_{n}"
        n += 1
        im.save(os.path.join(MARK_DIR, name + ".png"))
        layer = {}
        if mark.regions:
            layer["regions"] = mark.regions
        if mark.materials:
            layer["materials"] = mark.materials
        layer["effect"] = {"type": "pattern",
                           "texture": f"originofspawns:textures/gene_marks/{name}.png",
                           "tint": mark.tint}
        if mark.order:
            layer["order"] = mark.order
        if mark.target:
            layer["target"] = mark.target
        layers.append(layer)
    visual = {"layers": layers}
    if region_map:
        visual = {"region_map": region_map, "layers": layers}
    if gene in models:
        visual["model"] = models[gene]
    with open(os.path.join(VISUAL_DIR, gene + ".json"), "w") as fh:
        json.dump(visual, fh, indent="\t")
        fh.write("\n")


REGIONS = {"face": 1, "scalp": 2, "torso": 3, "arms": 4, "hands": 5, "legs": 6,
           "lower_legs": 7, "feet": 8, "ribs": 9, "spine": 10, "pelvis": 11, "joints": 12, "eye_sockets": 13}
MATERIALS = {"skin": 1, "cloth": 2, "eyes": 3, "bone": 4}


def touched(visual):
    """Return the pixels that the marks of a gene visual change.

    A desaturate layer, or a layer with an order below 0, is a color change
    of a whole part that goes under the marks. The check does not count it.
    A spots layer can change each pixel that it covers.
    """
    namespace, path = visual.get("region_map", "originofspawns:textures/region/humanoid.png").split(":")
    regions = Image.open(os.path.join(ROOT, "src/main/resources/assets", namespace, path)).convert("RGBA")
    pixels = set()
    for layer in visual.get("layers", []):
        effect = layer["effect"]
        if effect["type"] == "desaturate" or layer.get("order", 0) < 0:
            continue
        pattern = None
        if effect["type"] == "pattern" or effect["type"] == "erase" and "texture" in effect:
            namespace, path = effect["texture"].split(":")
            pattern = Image.open(os.path.join(ROOT, "src/main/resources/assets", namespace, path)).convert("RGBA")
        want_r = {REGIONS[r] for r in layer.get("regions", [])}
        want_m = {MATERIALS[m] for m in layer.get("materials", [])}
        for y in range(regions.height):
            for x in range(regions.width):
                r, m, _, a = regions.getpixel((x, y))
                if not a or (want_r and r not in want_r) or (want_m and m not in want_m):
                    continue
                if pattern and not pattern.getpixel((x % pattern.width, y % pattern.height))[3]:
                    continue
                pixels.add((x, y))
    return pixels


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for gene, make in GENES.items():
        marks = make()
        write(gene, marks)
        marked = [m for m in marks if not isinstance(m, dict) and m.order >= 0]
        print(gene, sum(len(m.pixels) for m in marked), "mark pixels")
    owners = {}
    for gene in sorted(list(GENES) + list(HAND_WRITTEN)):
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in touched(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = {}
    for p, genes in owners.items():
        if len(genes) > 1:
            key = " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
