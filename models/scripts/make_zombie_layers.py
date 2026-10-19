import sys, os
# Make the draft gene layer textures for the zombie.
# Usage: make_zombie_layers.py <output dir> <vanilla zombie.png> <check image>
# The script also prints the overlap of the opaque marks of each gene.
# The vanilla texture is only for the check image. Do not put it in the repository.
from PIL import Image

OUT = sys.argv[1]
VANILLA = sys.argv[2]

# Box UV faces for a box at (u, v) with size (w, h, d).
def faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }

HEAD = faces(0, 0, 8, 8, 8)
BODY = faces(16, 16, 8, 12, 4)
ARM = faces(40, 16, 4, 12, 4)
LEG = faces(0, 16, 4, 12, 4)
PARTS = [HEAD, BODY, ARM, LEG]
SIDES = ["right", "front", "left", "back"]

def h(x, y, s):
    n = (x * 374761393 + y * 668265263 + s * 2147483647) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return (n ^ (n >> 16)) % 100

def new():
    return Image.new("RGBA", (64, 64), (0, 0, 0, 0))

def rect(face, rows=None, cols=None):
    u, v, w, hh = face
    r0, r1 = rows if rows else (0, hh)
    c0, c1 = cols if cols else (0, w)
    for y in range(v + r0, v + r1):
        for x in range(u + c0, u + c1):
            yield x, y

# Water resistance: a flat wet tint on all faces, and gills on the neck.
WATER_TINT = (34, 150, 140, 110)
GILL_FLESH = (214, 104, 112, 255)
GILL_SLIT = (110, 26, 44, 255)

def water():
    im = new()
    for part in PARTS:
        for f in part.values():
            for p in rect(f):
                im.putpixel(p, WATER_TINT)
    for side in ("right", "left"):
        u, v, w, hh = HEAD[side]
        # Three gill slits on the low part of each head side.
        for i, row in enumerate((3, 5, 7)):
            c0 = 2 if side == "right" else 1
            c1 = 7 if side == "right" else 6
            if i == 0:
                c0, c1 = c0 + 1, c1 - 1
            for x in range(u + c0, u + c1):
                im.putpixel((x, v + row - 1), GILL_FLESH)
                im.putpixel((x, v + row), GILL_SLIT)
    return im

# Fire resistance: charred skin and dark char plates with embers.
CHAR = [(24, 22, 22, 255), (38, 34, 32, 255), (52, 46, 42, 255)]
CHAR_EDGE = (74, 66, 60, 255)
EMBER = (214, 96, 28, 255)
EMBER_HOT = (250, 176, 60, 255)

def char_pixel(x, y, s):
    # Plates are 3x3 tiles. The tile border is a dark crack.
    if x % 3 == 0 or y % 3 == 0:
        r = h(x, y, s)
        if r < 4:
            return EMBER_HOT
        if r < 12:
            return EMBER
        return CHAR[0]
    r = h(x, y, s + 1)
    if (x % 3, y % 3) == (1, 1):
        return CHAR_EDGE
    return CHAR[1] if r < 60 else CHAR[2]

def fire():
    im = new()
    areas = []
    areas += list(rect(HEAD["back"]))
    areas += list(rect(HEAD["right"], rows=(0, 2)))
    areas += list(rect(HEAD["left"], rows=(0, 2)))
    areas += list(rect(BODY["back"]))
    for side in SIDES:
        areas += list(rect(ARM[side], rows=(6, 12)))
        areas += list(rect(LEG[side], rows=(5, 10)))
    areas += list(rect(ARM["bottom"]))
    for x, y in areas:
        im.putpixel((x, y), char_pixel(x, y, 7))
    return im

# Arrow resistance: small bone plates on the chest, the shoulders and the head.
PLATE = (204, 192, 156, 255)
PLATE_LIGHT = (232, 224, 196, 255)
PLATE_SHADE = (150, 136, 104, 255)
PLATE_LINE = (78, 68, 52, 255)

def plate(im, x0, y0, w, hh):
    for y in range(y0, y0 + hh):
        for x in range(x0, x0 + w):
            if y == y0 + hh - 1 or x == x0 + w - 1:
                c = PLATE_LINE
            elif y == y0:
                c = PLATE_LIGHT
            elif y == y0 + hh - 2 and hh > 2:
                c = PLATE_SHADE
            else:
                c = PLATE
            im.putpixel((x, y), c)

def arrow():
    im = new()
    u, v, w, hh = HEAD["front"]
    # A brow plate over the eyes, in two parts.
    plate(im, u, v, 4, 3)
    plate(im, u + 4, v, 4, 3)
    # A cap of four plates on the top of the head.
    u, v, w, hh = HEAD["top"]
    for dx in (0, 4):
        for dy in (0, 4):
            plate(im, u + dx, v + dy, 4, 4)
    u, v, w, hh = BODY["front"]
    plate(im, u, v, 4, 4)
    plate(im, u + 4, v, 4, 4)
    plate(im, u + 1, v + 4, 6, 3)
    for side in ("right", "left"):
        u, v, w, hh = BODY[side]
        plate(im, u, v, w, 4)
    for p in rect(ARM["top"]):
        im.putpixel(p, PLATE)
    for side in SIDES:
        u, v, w, hh = ARM[side]
        plate(im, u, v, w, 3)
    # The zombie holds its arms forward. Thus the front of the arm points up.
    u, v, w, hh = ARM["front"]
    plate(im, u, v + 3, w, 3)
    return im

os.makedirs(OUT, exist_ok=True)
layers = {"zombie_fire_resistance": fire(), "zombie_water_resistance": water(),
          "zombie_arrow_resistance": arrow()}
for name, im in layers.items():
    im.save(os.path.join(OUT, name + ".png"))

def opaque(im):
    return {(x, y) for x in range(64) for y in range(64) if im.getpixel((x, y))[3] == 255}

# The tint is not opaque. The opaque marks of each gene must not overlap.
f, w_, a = (opaque(layers[k]) for k in ("zombie_fire_resistance", "zombie_water_resistance", "zombie_arrow_resistance"))
print("opaque pixels: fire", len(f), "water", len(w_), "arrow", len(a))
print("overlap fire/water", len(f & w_), "fire/arrow", len(f & a), "water/arrow", len(w_ & a))

base = Image.open(VANILLA).convert("RGBA")
combo = base.copy()
for k in ("zombie_water_resistance", "zombie_fire_resistance", "zombie_arrow_resistance"):
    combo.alpha_composite(layers[k])
combo.resize((512, 512), Image.NEAREST).save(sys.argv[3])
