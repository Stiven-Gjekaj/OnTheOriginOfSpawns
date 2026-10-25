"""Draw box UV cubes from any side, for the preview scripts.

The view is orthographic. The model coordinates are in pixels, with the y
axis down, as in the vanilla models. The front of the mob looks to -z.
A turn of 0 shows the front. A turn of -90 shows the right side of the mob.
A tilt above 0 looks down on the mob.

Each cube is a dict with: "bone", "uv", "from", "size", "inflate",
"mirror", "texture" (a Pillow image), and "scale" (texture pixels for each
UV unit). It can also have "pivot" and "rotation" (degrees), as the parts
of a gene model, "glow": True to keep its full light in the dark, and
"bone_change": (move, scale, adult) to fit an adult part to a baby bone. A
point p then goes to move + (p - adult) * scale, after the part turns. The faces use the box UV layout of the vanilla ModelPart: in the
side row, the faces are the right side (-x), the front, the left side (+x),
and the back.
A cube with "translucent": True, as the clear gel of the slime, is drawn
after all the other cubes, on a layer of its own that keeps the alpha of its
texture, and the layer goes over the image.
A bone pivot is (x, y, z), or (x, y, z, rx, ry, rz) for a bone that turns,
as the legs of the spider. The angles are in degrees.
This script needs Pillow.
"""
import math

from PIL import Image, ImageDraw


def part_rotation(angles):
    """Return a function that turns a vector like ModelPart: Z, then Y, then X.

    The vector turns around X first, then around Y, then around Z.
    """
    ax, ay, az = (math.radians(a) for a in angles)

    def turn(v):
        x, y, z = v
        y, z = y * math.cos(ax) - z * math.sin(ax), y * math.sin(ax) + z * math.cos(ax)
        x, z = x * math.cos(ay) + z * math.sin(ay), -x * math.sin(ay) + z * math.cos(ay)
        x, y = x * math.cos(az) - y * math.sin(az), x * math.sin(az) + y * math.cos(az)
        return (x, y, z)
    return turn


def faces(cube, bone_pivot):
    """Return the visible parts of a cube: (origin, u vector, v vector, normal, rect)."""
    part_pivot = cube.get("pivot", (0, 0, 0))
    turn = part_rotation(cube.get("rotation", (0, 0, 0)))
    w, h, d = (int(s) for s in cube["size"])
    u, v = cube["uv"]
    i = cube["inflate"]
    x0, y0, z0 = (cube["from"][n] - i for n in range(3))
    x1, y1, z1 = (cube["from"][n] + cube["size"][n] + i for n in range(3))
    sx, sy, sz = x1 - x0, y1 - y0, z1 - z0
    rects = {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }
    if cube["mirror"]:
        rects["right"], rects["left"] = rects["left"], rects["right"]

    change = cube.get("bone_change")
    bone_turn = part_rotation(bone_pivot[3:]) if len(bone_pivot) > 3 else None

    def place(origin, du, dv, normal, rect):
        local = [origin[n] - part_pivot[n] for n in range(3)]
        moved = turn(local)
        origin = [part_pivot[n] + moved[n] for n in range(3)]
        du, dv, normal = turn(du), turn(dv), turn(normal)
        if change:
            move, scale, adult = change
            origin = [move[n] + (origin[n] - adult[n]) * scale[n] for n in range(3)]
            du = tuple(du[n] * scale[n] for n in range(3))
            dv = tuple(dv[n] * scale[n] for n in range(3))
        if bone_turn:
            origin, du, dv, normal = bone_turn(origin), bone_turn(du), bone_turn(dv), bone_turn(normal)
        origin = tuple(bone_pivot[n] + origin[n] for n in range(3))
        return origin, du, dv, normal, rect

    return [place(*f) for f in [
        ((x0, y0, z0), (sx, 0, 0), (0, sy, 0), (0, 0, -1), rects["front"]),
        ((x1, y0, z1), (-sx, 0, 0), (0, sy, 0), (0, 0, 1), rects["back"]),
        ((x0, y0, z1), (0, 0, -sz), (0, sy, 0), (-1, 0, 0), rects["right"]),
        ((x1, y0, z0), (0, 0, sz), (0, sy, 0), (1, 0, 0), rects["left"]),
        ((x0, y0, z1), (sx, 0, 0), (0, 0, -sz), (0, -1, 0), rects["top"]),
        ((x0, y1, z0), (sx, 0, 0), (0, 0, sz), (0, 1, 0), rects["bottom"]),
    ]]


def rotation(turn, tilt):
    t, p = math.radians(turn), math.radians(tilt)

    def rotate(x, y, z):
        x, z = x * math.cos(t) + z * math.sin(t), -x * math.sin(t) + z * math.cos(t)
        y, z = y * math.cos(p) - z * math.sin(p), y * math.sin(p) + z * math.cos(p)
        return x, y, z
    return rotate


def render(cubes, pivots, turn=0, tilt=0, scale=8, size=(24, 40), center=(0, 6), light=1.0,
           opacity=1.0):
    """Return an image of the cubes. size and center are in model pixels.

    light from 0 to 1 makes the cubes darker, as at night. A cube with
    "glow" keeps its full light. opacity from 0 to 1 fades the whole image,
    as a translucent mob. The far faces do not show through.
    """
    rotate = rotation(turn, tilt)
    width, height = size
    im = Image.new("RGBA", (width * scale, height * scale), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    parts = []
    for cube in cubes:
        for origin, du, dv, normal, rect in faces(cube, pivots[cube["bone"]]):
            if rotate(*normal)[2] >= -1e-6:
                continue
            middle = [origin[k] + du[k] / 2 + dv[k] / 2 for k in range(3)]
            parts.append((rotate(*middle)[2], cube, origin, du, dv, rect))
    cx, cy = center

    def screen(p):
        x, y, _ = rotate(*p)
        return ((x - cx + width / 2) * scale, (y - cy + height / 2) * scale)

    # Sort each texel by its own depth, and draw the far texels first. A
    # sort of whole faces fails where a large face is near a small one.
    quads = []
    for _, cube, origin, du, dv, (ru, rv, rw, rh) in parts:
        k = cube["scale"]
        cols, rows = max(1, round(rw * k)), max(1, round(rh * k))
        for j in range(rows):
            for i in range(cols):
                tx = round(ru * k) + (cols - 1 - i if cube["mirror"] else i)
                color = cube["texture"].getpixel((tx, round(rv * k) + j))
                if color[3] == 0:
                    continue
                if light < 1 and not cube.get("glow"):
                    color = tuple(int(c * light) for c in color[:3]) + (color[3],)
                points = []
                for a, b in ((i, j), (i + 1, j), (i + 1, j + 1), (i, j + 1)):
                    points.append([origin[n] + du[n] * a / cols + dv[n] * b / rows for n in range(3)])
                middle = [sum(p[n] for p in points) / 4 for n in range(3)]
                quads.append((rotate(*middle)[2], [screen(p) for p in points], color, cube.get("translucent")))
    clear = Image.new("RGBA", im.size, (0, 0, 0, 0))
    clear_draw = ImageDraw.Draw(clear)
    for _, corners, color, translucent in sorted(quads, key=lambda q: -q[0]):
        if translucent:
            clear_draw.polygon(corners, fill=color)
        else:
            draw.polygon(corners, fill=color, outline=color)
    im.alpha_composite(clear)
    if opacity < 1:
        alpha = im.getchannel("A").point(lambda a: int(a * opacity))
        im.putalpha(alpha)
    return im
