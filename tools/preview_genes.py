"""Make preview images of the zombie gene visuals.

Usage: python3 tools/preview_genes.py

The script reads the vanilla zombie texture from the Minecraft jar in the
Gradle cache of Loom. It applies the visual recipes with the region map, and
writes PNG files to build/previews/. Git ignores build/.
Each preview uses the vanilla texture. Do not commit a preview.
This script needs Pillow.

The recipes here are a copy for review. The data packs of the mod hold the
real recipes. The game applies them when it loads.
"""
import colorsys
import glob
import io
import os
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/originofspawns/textures")
REGION_MAP = os.path.join(ASSETS, "region/humanoid.png")
IRON_PLATES = os.path.join(ASSETS, "pattern/iron_plates.png")
OUT_DIR = os.path.join(ROOT, "build/previews")
ZOMBIE_PATH = "assets/minecraft/textures/entity/zombie/zombie.png"

FACE, SCALP, TORSO, ARMS, HANDS, LEGS, LOWER_LEGS, FEET = range(1, 9)
SKIN, CLOTH, EYES = 1, 2, 3

SCALE = 12


def find_zombie_texture():
    """Return the vanilla zombie texture from a Minecraft jar of Loom."""
    caches = os.path.expanduser("~/.gradle/caches/fabric-loom")
    jars = sorted(glob.glob(os.path.join(caches, "**", "*.jar"), recursive=True))
    # The client jar is the most likely place. Look at it first.
    jars.sort(key=lambda p: "client" not in os.path.basename(p))
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as z:
                if ZOMBIE_PATH in z.namelist():
                    print("vanilla texture from", jar)
                    return Image.open(io.BytesIO(z.read(ZOMBIE_PATH))).convert("RGBA")
        except zipfile.BadZipFile:
            continue
    raise SystemExit("No Minecraft jar with the zombie texture. Run ./gradlew build first.")


def noise(x, y, seed):
    """Return a fixed number from 0 to 99 for a pixel."""
    n = (x * 374761393 + y * 668265263 + seed * 982451653) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return (n ^ (n >> 16)) % 100


def pixels(regions, region_ids=None, material_ids=None):
    for y in range(64):
        for x in range(64):
            r, m, _, a = regions.getpixel((x, y))
            if not a:
                continue
            if region_ids and r not in region_ids:
                continue
            if material_ids and m not in material_ids:
                continue
            yield x, y


def cinder(tex, regions):
    """Skin: more gray. Hands and feet: dark and red spots."""
    out = tex.copy()
    for x, y in pixels(regions, material_ids={SKIN}):
        r, g, b, a = out.getpixel((x, y))
        if not a:
            continue
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        r, g, b = colorsys.hsv_to_rgb(h, s * 0.6, v * 0.88)
        out.putpixel((x, y), (round(r * 255), round(g * 255), round(b * 255), a))
    for x, y in pixels(regions, region_ids={HANDS, FEET}):
        a = out.getpixel((x, y))[3]
        if not a:
            continue
        n = noise(x, y, 11)
        if n < 18:
            out.putpixel((x, y), (30, 26, 24, a))
        elif n < 30:
            out.putpixel((x, y), (112, 40, 32, a))
    return out


def ironhide(tex, regions, plates):
    """Torso cloth: iron plates, tinted iron gray."""
    tint = (216, 218, 224)
    out = tex.copy()
    for x, y in pixels(regions, region_ids={TORSO}, material_ids={CLOTH}):
        base = out.getpixel((x, y))
        if not base[3]:
            continue
        gray, _, _, alpha = plates.getpixel((x % 16, y % 16))
        k = alpha / 255
        color = [round(base[i] * (1 - k) + tint[i] * gray / 255 * k) for i in range(3)]
        out.putpixel((x, y), (*color, base[3]))
    return out


def face(tex, x0, y0, w, h, flip=False):
    img = tex.crop((x0, y0, x0 + w, y0 + h))
    return img.transpose(Image.FLIP_LEFT_RIGHT) if flip else img


def render(tex):
    """Draw the front and the back of the standing zombie, side by side.

    The vanilla zombie uses the right limbs for the left limbs, with a mirror.
    """
    view = Image.new("RGBA", (16 * 2 + 4, 32), (0, 0, 0, 0))
    # Front. The right side of the zombie is on the left of the image.
    view.paste(face(tex, 8, 8, 8, 8), (4, 0))
    view.paste(face(tex, 20, 20, 8, 12), (4, 8))
    view.paste(face(tex, 44, 20, 4, 12), (0, 8))
    view.paste(face(tex, 44, 20, 4, 12, flip=True), (12, 8))
    view.paste(face(tex, 4, 20, 4, 12), (4, 20))
    view.paste(face(tex, 4, 20, 4, 12, flip=True), (8, 20))
    # Back. The right side of the zombie is on the right of the image.
    ox = 20
    view.paste(face(tex, 24, 8, 8, 8), (ox + 4, 0))
    view.paste(face(tex, 32, 20, 8, 12), (ox + 4, 8))
    view.paste(face(tex, 52, 20, 4, 12, flip=True), (ox, 8))
    view.paste(face(tex, 52, 20, 4, 12), (ox + 12, 8))
    view.paste(face(tex, 12, 20, 4, 12, flip=True), (ox + 4, 20))
    view.paste(face(tex, 12, 20, 4, 12), (ox + 8, 20))
    bg = Image.new("RGBA", view.size, (200, 210, 220, 255))
    bg.alpha_composite(view)
    return bg.resize((view.width * SCALE, view.height * SCALE), Image.NEAREST)


def save(name, tex):
    tex.resize((64 * 8, 64 * 8), Image.NEAREST).save(os.path.join(OUT_DIR, name + "_texture.png"))
    path = os.path.join(OUT_DIR, name + "_front_back.png")
    render(tex).save(path)
    print(path)


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    tex = find_zombie_texture()
    regions = Image.open(REGION_MAP).convert("RGBA")
    plates = Image.open(IRON_PLATES).convert("RGBA")
    save("zombie_plain", tex)
    save("zombie_cinder", cinder(tex, regions))
    save("zombie_ironhide", ironhide(tex, regions, plates))
    save("zombie_combined", ironhide(cinder(tex, regions), regions, plates))


if __name__ == "__main__":
    main()
