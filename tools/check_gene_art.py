"""Measure the gene art of a species in the preview renders.

Usage: python3 tools/check_gene_art.py <species> (from the root of the repository)

The script draws the mob as tools/preview_genes.py does, in the views of the
sheets and from the front and the back. It prints:
- for each gene, the part of the pixels of the mob that the gene changes, in
  percent. The script compares the gene with the plain mob, and divides by
  the pixels that show the mob in one of the two images. It then gives the
  part of the texture pixels of the mob that the marks change. A gene model
  does not change the texture.
- for each pair of genes where one gene has a gene model, or the two genes
  are in the same family: the part of the changed pixels of the first gene
  that still show when the mob has the two genes. A gene model can cover
  the marks of another gene, so the script checks each pair with a model.
The pairs with less than 80 percent come first.
This script needs Pillow.
"""
import itertools
import sys

import preview_genes as pg

VIEWS = None


def render(tex, genes):
    images = []
    for turn, tilt in VIEWS:
        im = pg.render3d.render(pg.cubes_of(tex, genes), pg.pivots(), turn, tilt, 4,
                                (pg.VIEW_W, pg.VIEW_H), center=(0, pg.VIEW_H / 2 - pg.VIEW_Y))
        images.append(im.load())
        images[-1] = (images[-1], im.size)
    return images


def differ(a, b):
    if a[3] == 0 and b[3] == 0:
        return False
    if (a[3] == 0) != (b[3] == 0):
        return True
    return sum(abs(a[i] - b[i]) for i in range(3)) > 12


def changed(one, two):
    """Return the set of (view, x, y) where two images differ, and the count of mob pixels."""
    out, mob = set(), 0
    for v, ((pa, size), (pb, _)) in enumerate(zip(one, two)):
        for y in range(size[1]):
            for x in range(size[0]):
                a, b = pa[x, y], pb[x, y]
                if a[3] or b[3]:
                    mob += 1
                if differ(a, b):
                    out.add((v, x, y))
    return out, mob


def pixel(images, p):
    v, x, y = p
    return images[v][0][x, y]


def main(species):
    global VIEWS
    adult_path, _, families, _, _ = pg.SPECIES[species]
    pg.set_mob(species)
    VIEWS = list(pg.MOB["sheet_views"]) + [(0, 0), (180, 0)]
    tex = pg.find_zombie_texture(adult_path)
    family_of = {g: f for f, genes in families.items() for g in genes}
    genes = list(family_of)
    models = {g for g in genes if "model" in (pg.visual(g) or {})}
    plain = render(tex, [])
    alone, marks = {}, {}
    print("gene, changed pixels in percent: of the render, and of the texture")
    opaque = [(x, y) for y in range(tex.height) for x in range(tex.width) if tex.getpixel((x, y))[3]]
    for g in genes:
        baked = pg.zombie(tex, [g])
        alone[g] = render(baked, [g])
        marks[g], mob = changed(plain, alone[g])
        texels = sum(1 for p in opaque if differ(tex.getpixel(p), baked.getpixel(p)))
        print(f"  {g}: {100 * len(marks[g]) / mob:.1f}  {100 * texels / len(opaque):.1f}")
    rows = []
    for a, b in itertools.permutations(genes, 2):
        if not (a in models or b in models or family_of[a] == family_of[b]):
            continue
        both = render(pg.zombie(tex, [a, b]), [a, b])
        kept = sum(1 for p in marks[a] if differ(pixel(both, p), pixel(alone[b], p)))
        rows.append((100 * kept / max(1, len(marks[a])), a, b))
    rows.sort()
    print("pairs, the part of the first gene that shows with the second gene, in percent")
    for share, a, b in rows[:25]:
        print(f"  {a} with {b}: {share:.0f}")


if __name__ == "__main__":
    main(sys.argv[1])
