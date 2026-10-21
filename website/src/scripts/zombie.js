// The zombie renderer of the wiki.
//
// The renderer makes a zombie texture from the region map of the mod, then
// applies the visual recipes of the genes, as TextureBaker in the mod does.
// tools/preview_genes.py does the same steps in Python.
// The base texture is not the vanilla texture. The renderer paints each
// region of the region map with a flat color of its own.
// Then it draws the cubes of the zombie and of the gene models on a canvas.


// The ids of the region maps. The skeleton map adds the bone regions.
const REGIONS = {
  face: 1, scalp: 2, torso: 3, arms: 4, hands: 5, legs: 6, lower_legs: 7, feet: 8,
  ribs: 9, spine: 10, pelvis: 11, joints: 12, eye_sockets: 13,
};
const MATERIALS = { skin: 1, cloth: 2, eyes: 3, bone: 4 };
const DEFAULT_REGION_MAP = "originofspawns:textures/region/humanoid.png";

// The colors of the base texture: region id, material id -> color.
// The flat colors of each species: skin and cloth by region id, and the eyes.
// They are drawings of the wiki, not the colors of the vanilla textures.
const PALETTES = {
  zombie: {
    skin: { 1: [94, 140, 70], 2: [70, 110, 54], 4: [90, 134, 66], 5: [80, 122, 58], 6: [88, 130, 64], 7: [88, 130, 64], 8: [70, 104, 50], 3: [96, 142, 72] },
    cloth: { 3: [36, 140, 150], 4: [36, 140, 150], 6: [64, 66, 160], 7: [64, 66, 160], 8: [48, 46, 86] },
    eyes: [22, 26, 20],
  },
  husk: {
    skin: { 1: [170, 146, 104], 2: [126, 104, 70], 4: [160, 136, 96], 5: [146, 122, 84], 6: [156, 132, 92], 7: [156, 132, 92], 8: [120, 98, 66], 3: [172, 148, 106] },
    cloth: { 3: [112, 92, 62], 4: [112, 92, 62], 6: [84, 72, 54], 7: [84, 72, 54], 8: [60, 50, 38] },
    eyes: [40, 24, 12],
  },
  drowned: {
    skin: { 1: [96, 160, 150], 2: [62, 118, 112], 4: [90, 152, 142], 5: [80, 140, 130], 6: [86, 148, 138], 7: [86, 148, 138], 8: [66, 120, 112], 3: [98, 162, 152] },
    cloth: { 3: [92, 126, 82], 4: [92, 126, 82], 6: [60, 92, 110], 7: [60, 92, 110], 8: [44, 66, 80] },
    eyes: [150, 230, 240],
  },
  // The skeleton has the material bone. A bone pixel takes the skin color of
  // its region.
  skeleton: {
    skin: {
      1: [214, 210, 198], 2: [200, 196, 184], 3: [206, 202, 190], 4: [204, 200, 188], 5: [186, 182, 170],
      6: [204, 200, 188], 7: [196, 192, 180], 8: [180, 176, 164], 9: [212, 208, 196], 10: [170, 166, 154],
      11: [190, 186, 174], 12: [160, 156, 146], 13: [40, 40, 40],
    },
    cloth: {},
    eyes: [36, 36, 36],
    // Every second row of the ribs is dark, so the rib cage shows.
    stripes: { 9: [128, 124, 116] },
  },
};

// The stray: bone with a cold tint.
PALETTES.stray = {
  skin: Object.fromEntries(Object.entries(PALETTES.skeleton.skin).map(([k, c]) => [k, Number(k) === 13 ? c : [c[0] - 18, c[1] - 6, c[2] + 8]])),
  cloth: {},
  eyes: [30, 34, 40],
  stripes: { 9: [112, 118, 126] },
};

// The cloth of the stray: a drawing of the wiki in the zombie layout. It
// covers the body, the upper arms, and the legs, with torn holes that are
// more common near the hem.
function strayCloth(humanoid) {
  const out = new ImageData(humanoid.width, humanoid.height);
  const d = humanoid.data;
  for (let y = 0; y < out.height; y++) {
    for (let x = 0; x < out.width; x++) {
      const i = (y * out.width + x) * 4;
      if (!d[i + 3]) continue;
      const region = d[i];
      // The cloth: torso (3), arms (4), legs (6), and lower legs (7).
      if (![3, 4, 6, 7].includes(region)) continue;
      const h = spotHash(x, y, 4242) & 0xff;
      const hole = region === 7 ? 0.45 : region === 4 ? 0.2 : 0.1;
      if (h / 255 < hole) continue;
      const n = ((spotHash(x, y, 77) & 0xff) / 255 - 0.5) * 0.16;
      const c = region === 3 ? [150, 168, 176] : [134, 152, 162];
      out.data[i] = c[0] * (1 + n);
      out.data[i + 1] = c[1] * (1 + n);
      out.data[i + 2] = c[2] * (1 + n);
      out.data[i + 3] = 255;
    }
  }
  return out;
}

// The body of each species: the region map, the cubes, and the pivots, as in
// tools/preview_genes.py. The drowned has its own UV for the left limbs. The
// skeleton has thin arms and legs, and its map is 64 x 32.
const BODIES = {
  skeleton: {
    map: "originofspawns:textures/region/skeleton.png",
    pivots: {
      head: [0, 0, 0], body: [0, 0, 0], right_arm: [-5, 2, 0], left_arm: [5, 2, 0],
      right_leg: [-2, 12, 0], left_leg: [2, 12, 0],
    },
    cubes: [
      ["head", [0, 0], [-4, -8, -4], [8, 8, 8], false],
      ["body", [16, 16], [-4, 0, -2], [8, 12, 4], false],
      ["right_arm", [40, 16], [-1, -2, -1], [2, 12, 2], false],
      ["left_arm", [40, 16], [-1, -2, -1], [2, 12, 2], true],
      ["right_leg", [0, 16], [-1, 0, -1], [2, 12, 2], false],
      ["left_leg", [0, 16], [-1, 0, -1], [2, 12, 2], true],
    ],
  },
  stray: {
    map: "originofspawns:textures/region/skeleton.png",
    // The cloth layer uses the zombie cubes, 0.25 pixel larger.
    outer: true,
  },
  drowned: {
    split: true,
    cubes: [
      ["head", [0, 0], [-4, -8, -4], [8, 8, 8], false],
      ["body", [16, 16], [-4, 0, -2], [8, 12, 4], false],
      ["right_arm", [40, 16], [-3, -2, -2], [4, 12, 4], false],
      ["left_arm", [32, 48], [-1, -2, -2], [4, 12, 4], false],
      ["right_leg", [0, 16], [-2, 0, -2], [4, 12, 4], false],
      ["left_leg", [16, 48], [-2, 0, -2], [4, 12, 4], false],
    ],
  },
};
// The stray has the body of the skeleton.
BODIES.stray.pivots = BODIES.skeleton.pivots;
BODIES.stray.cubes = BODIES.skeleton.cubes;


// The bones and the cubes of the zombie, as in tools/preview_genes.py.
const PIVOTS = {
  head: [0, 0, 0], body: [0, 0, 0], right_arm: [-5, 2, 0], left_arm: [5, 2, 0],
  right_leg: [-1.9, 12, 0], left_leg: [1.9, 12, 0],
};
const ZOMBIE_CUBES = [
  ["head", [0, 0], [-4, -8, -4], [8, 8, 8], false],
  ["body", [16, 16], [-4, 0, -2], [8, 12, 4], false],
  ["right_arm", [40, 16], [-3, -2, -2], [4, 12, 4], false],
  ["left_arm", [40, 16], [-1, -2, -2], [4, 12, 4], true],
  ["right_leg", [0, 16], [-2, 0, -2], [4, 12, 4], false],
  ["left_leg", [0, 16], [-2, 0, -2], [4, 12, 4], true],
];

// The baby zombie model, as BabyLayout.java in the mod and
// tools/preview_genes.py give it. The feet are at y 24, as for the adult.
const BABY_PIVOTS = {
  head: [0, 15.25, 0], body: [0, 17.5, 0], right_arm: [-3, 15.5, 0], left_arm: [3, 15.5, 0],
  right_leg: [-1, 20, 0], left_leg: [1, 20, 0],
};
// Each pair: bone (null for the hat layer), the adult cube, the baby cube,
// and true if the adult cube mirrors its texture. A cube is
// [u, v, width, height, depth, x, y, z].
const BABY_PAIRS = [
  ["head", [0, 0, 8, 8, 8, -4, -8, -4], [3, 3, 6, 6, 6, -3, -6.25, -3], false],
  [null, [32, 0, 8, 8, 8, -4, -8, -4], [35, 3, 6, 6, 6, -3, -6.15, -3], false],
  ["body", [16, 16, 8, 12, 4, -4, 0, -2], [16, 16, 4, 5, 2, -2, -2.5, -1], false],
  ["right_arm", [40, 16, 4, 12, 4, -3, -2, -2], [36, 16, 2, 5, 2, -1, -0.5, -1], false],
  ["left_arm", [40, 16, 4, 12, 4, -1, -2, -2], [28, 16, 2, 5, 2, -1, -0.5, -1], true],
  ["right_leg", [0, 16, 4, 12, 4, -2, 0, -2], [8, 16, 2, 4, 2, -1, 0, -1], false],
  ["left_leg", [0, 16, 4, 12, 4, -2, 0, -2], [0, 16, 2, 4, 2, -1, 0, -1], true],
];
// The faces that change places in a mirrored cube: side 1 and side 2.
const MIRRORED_FACE = [0, 1, 4, 3, 2, 5];

// The faces of a box UV: top, bottom, side 1, front, side 2, back, as
// [x, y, width, height].
function uvFaces([u, v, w, h, d]) {
  return [[u + d, v, w, d], [u + d + w, v, w, d], [u, v + d, d, h], [u + d, v + d, w, h], [u + d + w, v + d, d, h], [u + d * 2 + w, v + d, w, h]];
}

// Copy one face to a face of a different size, with the nearest pixel.
function copyFace(src, from, out, to, scale, flip) {
  const tw = to[2] * scale, th = to[3] * scale, fw = from[2] * scale, fh = from[3] * scale;
  for (let y = 0; y < th; y++) {
    for (let x = 0; x < tw; x++) {
      const fx = flip ? tw - 1 - x : x;
      const sx = from[0] * scale + Math.trunc(((fx + 0.5) * fw) / tw);
      const sy = from[1] * scale + Math.trunc(((y + 0.5) * fh) / th);
      const tx = to[0] * scale + x, ty = to[1] * scale + y;
      if (sx >= src.width || sy >= src.height || tx >= out.width || ty >= out.height) continue;
      const si = (sy * src.width + sx) * 4, ti = (ty * out.width + tx) * 4;
      for (let k = 0; k < 4; k++) out.data[ti + k] = src.data[si + k];
    }
  }
}

// Move an image in the adult layout to the baby layout, like BabyLayout.toBaby.
function toBaby(adult) {
  const scale = Math.max(Math.floor(adult.width / 64), 1);
  const out = new ImageData(adult.width, adult.height);
  for (const [, a, b, mirror] of BABY_PAIRS) {
    const bf = uvFaces(b), af = uvFaces(a);
    for (let f = 0; f < 6; f++) copyFace(adult, af[mirror ? MIRRORED_FACE[f] : f], out, bf[f], scale, mirror);
  }
  return out;
}

// Move an image in the baby layout to the adult layout, like BabyLayout.toAdult.
function toAdult(baby) {
  const scale = Math.max(Math.floor(baby.width / 64), 1);
  const out = new ImageData(baby.width, baby.height);
  for (const [, a, b, mirror] of BABY_PAIRS) {
    if (mirror) continue;
    const bf = uvFaces(b), af = uvFaces(a);
    for (let f = 0; f < 6; f++) copyFace(baby, bf[f], out, af[f], scale, false);
  }
  return out;
}

// BabyLayout.SPLIT_LIMBS in the mod: the right limb of the zombie layout, and
// the left limb of a model with split limbs, such as the drowned.
const SPLIT_PAIRS = [
  [[40, 16, 4, 12, 4], [32, 48, 4, 12, 4]],
  [[0, 16, 4, 12, 4], [16, 48, 4, 12, 4]],
];

// Copy the right limbs, mirrored, to the left limbs, like BabyLayout.toSplitLimbs.
function toSplit(image) {
  const scale = Math.max(Math.floor(image.width / 64), 1);
  const out = new ImageData(new Uint8ClampedArray(image.data), image.width, image.height);
  for (const [a, b] of SPLIT_PAIRS) {
    const from = uvFaces(a), to = uvFaces(b);
    for (let f = 0; f < 6; f++) copyFace(image, from[MIRRORED_FACE[f]], out, to[f], scale, true);
  }
  return out;
}

// The change from an adult bone to a baby bone, like BabyLayout.boneChange.
// A point p of the adult bone goes to move + (p - adult) * scale.
function boneChange(bone) {
  const pair = BABY_PAIRS.find((p) => p[0] === bone);
  const a = pair[1], b = pair[2];
  return { move: [b[5], b[6], b[7]], scale: [b[2] / a[2], b[3] / a[3], b[4] / a[4]], adult: [a[5], a[6], a[7]] };
}

const assetUrl = (id) => {
  const [ns, p] = id.split(":");
  return `${import.meta.env.BASE_URL.replace(/\/$/, "")}/mod/${ns}/${p}`;
};

// ---------------------------------------------------------------------
// Images

const images = new Map();
function loadImage(id) {
  if (!images.has(id)) {
    images.set(id, new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => {
        const c = document.createElement("canvas");
        c.width = img.width;
        c.height = img.height;
        const g = c.getContext("2d");
        g.drawImage(img, 0, 0);
        resolve(g.getImageData(0, 0, img.width, img.height));
      };
      img.onerror = () => reject(new Error("cannot load " + id));
      img.src = assetUrl(id);
    }));
  }
  return images.get(id);
}

// ---------------------------------------------------------------------
// Hash functions with Java int math, as in TextureBaker.

function javaStringHash(s) {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (Math.imul(31, h) + s.charCodeAt(i)) | 0;
  return h;
}
function identifierHash(id) {
  const [ns, p] = id.split(":");
  return (Math.imul(31, javaStringHash(ns)) + javaStringHash(p)) | 0;
}
function spotHash(x, y, seed) {
  let h = seed;
  h = (Math.imul(h, 31) + x) | 0;
  h = (Math.imul(h, 31) + y) | 0;
  h ^= h >>> 16;
  h = Math.imul(h, 0x85ebca6b | 0);
  h ^= h >>> 13;
  h = Math.imul(h, 0xc2b2ae35 | 0);
  h ^= h >>> 16;
  return h;
}
const rgb = (hex) => [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16));

// ---------------------------------------------------------------------
// Textures

function baseTexture(map, species = "zombie") {
  const { skin: SKIN, cloth: CLOTH, eyes: EYES, stripes = {} } = PALETTES[species] || PALETTES.zombie;
  const out = new ImageData(map.width, map.height);
  const d = map.data;
  for (let y = 0; y < map.height; y++) {
    for (let x = 0; x < map.width; x++) {
      const i = (y * map.width + x) * 4;
      if (!d[i + 3]) continue;
      const region = d[i];
      const material = d[i + 1];
      let c = material === MATERIALS.eyes ? EYES : material === MATERIALS.cloth ? CLOTH[region] || [90, 90, 90] : SKIN[region] || [90, 130, 60];
      if (stripes[region] && y % 2 === 0) c = stripes[region];
      // A small noise, so that the flat colors look like a texture.
      const n = ((spotHash(x, y, 1234) & 0xff) / 255 - 0.5) * 0.22;
      out.data[i] = Math.max(0, Math.min(255, c[0] * (1 + n)));
      out.data[i + 1] = Math.max(0, Math.min(255, c[1] * (1 + n)));
      out.data[i + 2] = Math.max(0, Math.min(255, c[2] * (1 + n)));
      out.data[i + 3] = 255;
    }
  }
  return out;
}

async function applyLayer(out, gene, layer, maps, patternOf) {
  const map = maps[gene.visual.regionMap || DEFAULT_REGION_MAP];
  const outer = layer.target === "outer";
  const seed = identifierHash("originofspawns:" + gene.id);
  const regions = new Set((layer.regions || []).map((r) => REGIONS[r]));
  const materials = new Set((layer.materials || []).map((m) => MATERIALS[m]));
  const e = layer.effect;
  // A pattern, and an erase with a mask texture, read a tiled texture.
  const pattern = e.texture && (e.type === "pattern" || e.type === "erase") ? await patternOf(e.texture) : null;
  const tint = e.tint ? rgb(e.tint) : [255, 255, 255];
  const colors = e.colors ? e.colors.map(rgb) : [];
  const o = out.data;
  for (let y = 0; y < out.height; y++) {
    for (let x = 0; x < out.width; x++) {
      // A layer with the target "outer" goes on the whole cloth texture of
      // the stray. It does not use the region map.
      if (!outer) {
        const mi = (Math.floor((y * map.height) / out.height) * map.width + Math.floor((x * map.width) / out.width)) * 4;
        if (!map.data[mi + 3]) continue;
        if (regions.size && !regions.has(map.data[mi])) continue;
        if (materials.size && !materials.has(map.data[mi + 1])) continue;
      }
      const i = (y * out.width + x) * 4;
      if (e.type === "desaturate") {
        const gray = Math.floor((o[i] * 299 + o[i + 1] * 587 + o[i + 2] * 114) / 1000);
        const keep = 1 - (e.darken || 0);
        for (let k = 0; k < 3; k++) o[i + k] = Math.trunc((o[i + k] + (gray - o[i + k]) * e.amount) * keep);
      } else if (e.type === "spots") {
        const h = spotHash(x, y, seed);
        if ((h & 0xffff) >= e.density * 0x10000) continue;
        const c = colors[((h >>> 16) & 0x7fff) % colors.length];
        o[i] = c[0]; o[i + 1] = c[1]; o[i + 2] = c[2];
      } else if (e.type === "pattern") {
        const pi = ((y % pattern.height) * pattern.width + (x % pattern.width)) * 4;
        const strength = pattern.data[pi + 3] / 255;
        const shade = pattern.data[pi] / 255;
        for (let k = 0; k < 3; k++) o[i + k] = Math.trunc(o[i + k] + (tint[k] * shade - o[i + k]) * strength);
      } else if (e.type === "erase") {
        // With a mask, erase only where the mask has alpha, like TextureBaker.masked.
        if (pattern && !pattern.data[((y % pattern.height) * pattern.width + (x % pattern.width)) * 4 + 3]) continue;
        o[i] = o[i + 1] = o[i + 2] = o[i + 3] = 0;
      }
    }
  }
}

const baked = new Map();
// Return the texture of a zombie with the genes, like TextureBaker.bakeAll.
// With baby, the region maps and the marks move to the baby layout, and the
// gene models move onto the baby bones, as GeneTextures and GeneModelLayer
// do in the game.
async function bake(genes, baby = false, species = genes[0]?.species ?? "zombie") {
  const key = `${species}|` + genes.map((g) => g.id).sort().join(",") + (baby ? "|baby" : "");
  if (baked.has(key)) return baked.get(key);
  const job = (async () => {
    const maps = {};
    const bodyOf = BODIES[species] || {};
    const baseMap = bodyOf.map || DEFAULT_REGION_MAP;
    const mapIds = new Set([baseMap]);
    for (const g of genes) if (g.visual && g.visual.regionMap) mapIds.add(g.visual.regionMap);
    // A body with split limbs, such as the drowned, moves the marks of the
    // right limbs to the left limbs too.
    const split = bodyOf.split && !baby;
    const layout = (img) => (baby ? toBaby(img) : split ? toSplit(img) : img);
    for (const id of mapIds) maps[id] = layout(await loadImage(id));
    const base = baseTexture(maps[baseMap], species);
    // A pattern with the size of a whole mob texture is a mark in the adult
    // layout, so it moves to the baby layout. A smaller pattern is a tile.
    const patternOf = async (id) => {
      const img = await loadImage(id);
      return img.width >= 64 && img.height >= 64 ? layout(img) : img;
    };
    const steps = [];
    [...genes].sort((a, b) => (a.id < b.id ? -1 : 1)).forEach((g) => {
      if (!g.visual) return;
      for (const layer of g.visual.layers) steps.push({ order: layer.order || 0, gene: g, layer });
    });
    steps.sort((a, b) => a.order - b.order);
    for (const s of steps) if (s.layer.target !== "outer") await applyLayer(base, s.gene, s.layer, maps, patternOf);
    const body = baby
      ? BABY_PAIRS.filter((p) => p[0]).map(([bone, , b]) => [bone, [b[0], b[1]], [b[5], b[6], b[7]], [b[2], b[3], b[4]], false])
      : bodyOf.cubes || ZOMBIE_CUBES;
    const cubes = body.map(([bone, uv, from, size, mirror]) => ({
      bone, uv, from, size, mirror, inflate: 0, tex: base, scale: 1, pivot: [0, 0, 0], rotation: [0, 0, 0], glow: false,
    }));
    // The cloth layer of the stray, with the layers that have the target "outer".
    if (bodyOf.outer && !baby) {
      const cloth = strayCloth(await loadImage(DEFAULT_REGION_MAP));
      for (const s of steps) if (s.layer.target === "outer") await applyLayer(cloth, s.gene, s.layer, maps, patternOf);
      for (const [bone, uv, from, size, mirror] of ZOMBIE_CUBES) {
        cubes.push({ bone, uv, from, size, mirror, inflate: 0.25, tex: cloth, scale: 1, pivot: [0, 0, 0], rotation: [0, 0, 0], glow: false });
      }
    }
    // Gene model cubes have adult UVs, so a model with mob_texture reads the
    // baby texture in the adult layout.
    const mobTex = baby ? toAdult(base) : base;
    for (const g of genes) {
      const m = g.visual && g.visual.model;
      if (!m) continue;
      // A model with mob_texture uses the baked texture of the zombie, with
      // the marks of all its genes, as GeneModel does in the game.
      const tex = m.mob_texture ? mobTex : await loadImage(m.texture);
      const scale = tex.width / (m.texture_width || 64);
      for (const p of m.parts) {
        cubes.push({
          bone: p.bone, uv: p.uv, from: p.from, size: p.size, mirror: !!p.mirror, inflate: p.inflate || 0,
          tex, scale, pivot: p.pivot || [0, 0, 0], rotation: p.rotation || [0, 0, 0], glow: !!p.glow, gene: g.id,
          change: baby ? boneChange(p.bone) : null,
        });
      }
    }
    return { texture: base, cubes, faces: buildFaces(cubes), pivots: baby ? BABY_PIVOTS : bodyOf.pivots || PIVOTS };
  })();
  baked.set(key, job);
  return job;
}

// ---------------------------------------------------------------------
// Geometry

const rad = (d) => (d * Math.PI) / 180;
function rotX(p, a) { const c = Math.cos(a), s = Math.sin(a); return [p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c]; }
function rotY(p, a) { const c = Math.cos(a), s = Math.sin(a); return [p[0] * c + p[2] * s, p[1], -p[0] * s + p[2] * c]; }
function rotZ(p, a) { const c = Math.cos(a), s = Math.sin(a); return [p[0] * c - p[1] * s, p[0] * s + p[1] * c, p[2]]; }
const add = (a, b) => [a[0] + b[0], a[1] + b[1], a[2] + b[2]];
const sub = (a, b) => [a[0] - b[0], a[1] - b[1], a[2] - b[2]];
const mul = (a, k) => [a[0] * k, a[1] * k, a[2] * k];

// The faces of a box in box UV layout. Each face has an origin, a vector
// for one texel in u and in v, the texel rectangle, and a normal.
function buildFaces(cubes) {
  const faces = [];
  for (const c of cubes) {
    const [w, h, d] = c.size;
    const f = c.from;
    const k = c.inflate;
    const x0 = f[0] - k, y0 = f[1] - k, z0 = f[2] - k;
    const x1 = f[0] + w + k, y1 = f[1] + h + k, z1 = f[2] + d + k;
    const [u, v] = c.uv;
    const s = c.scale;
    const tw = Math.round(w * s), th = Math.round(h * s), td = Math.round(d * s);
    const X = x1 - x0, Y = y1 - y0, Z = z1 - z0;
    let list = [
      { n: [0, 0, -1], o: [x0, y0, z0], ue: [X, 0, 0], ve: [0, Y, 0], r: [u + d, v + d, w, h], t: [tw, th] },
      { n: [0, 0, 1], o: [x1, y0, z1], ue: [-X, 0, 0], ve: [0, Y, 0], r: [u + 2 * d + w, v + d, w, h], t: [tw, th] },
      { n: [-1, 0, 0], o: [x0, y0, z1], ue: [0, 0, -Z], ve: [0, Y, 0], r: [u, v + d, d, h], t: [td, th] },
      { n: [1, 0, 0], o: [x1, y0, z0], ue: [0, 0, Z], ve: [0, Y, 0], r: [u + d + w, v + d, d, h], t: [td, th] },
      { n: [0, -1, 0], o: [x0, y0, z1], ue: [X, 0, 0], ve: [0, 0, -Z], r: [u + d, v, w, d], t: [tw, td] },
      { n: [0, 1, 0], o: [x0, y1, z0], ue: [X, 0, 0], ve: [0, 0, Z], r: [u + d + w, v, w, d], t: [tw, td] },
    ];
    if (c.mirror) {
      // Swap the side textures and flip u on each face.
      const r2 = list[2].r;
      list[2].r = list[3].r;
      list[3].r = r2;
      for (const fc of list) {
        fc.o = add(fc.o, fc.ue);
        fc.ue = mul(fc.ue, -1);
      }
    }
    for (const fc of list) {
      if (!fc.t[0] || !fc.t[1]) continue;
      // The texel colors of the face, read one time.
      const cols = [];
      const [ru, rv] = fc.r;
      for (let j = 0; j < fc.t[1]; j++) {
        for (let i = 0; i < fc.t[0]; i++) {
          const px = Math.floor(ru * s) + i, py = Math.floor(rv * s) + j;
          if (px < 0 || py < 0 || px >= c.tex.width || py >= c.tex.height) { cols.push(null); continue; }
          const idx = (py * c.tex.width + px) * 4;
          const a = c.tex.data[idx + 3];
          cols.push(a < 8 ? null : [c.tex.data[idx], c.tex.data[idx + 1], c.tex.data[idx + 2], a / 255]);
        }
      }
      faces.push({ cube: c, n: fc.n, o: fc.o, ue: mul(fc.ue, 1 / fc.t[0]), ve: mul(fc.ve, 1 / fc.t[1]), tw: fc.t[0], th: fc.t[1], cols });
    }
  }
  return faces;
}

// Return a function that moves a point of a cube into the view.
function transformer(cube, pose, view, pivots) {
  const piv = cube.pivot, rot = cube.rotation.map(rad);
  const bonePivot = pivots[cube.bone] || [0, 0, 0];
  const ch = cube.change;
  const boneRot = (pose[cube.bone] || [0, 0, 0]);
  const point = (p, isNormal) => {
    let q = isNormal ? p : sub(p, piv);
    q = rotZ(rotY(rotX(q, rot[0]), rot[1]), rot[2]);
    if (!isNormal) q = add(q, piv);
    // Move and scale an adult part onto the baby bone.
    if (ch) {
      q = isNormal
        ? (() => { const n = [q[0] / ch.scale[0], q[1] / ch.scale[1], q[2] / ch.scale[2]]; const l = Math.hypot(...n) || 1; return n.map((x) => x / l); })()
        : [0, 1, 2].map((k) => ch.move[k] + (q[k] - ch.adult[k]) * ch.scale[k]);
    }
    q = rotZ(rotY(rotX(q, boneRot[0]), boneRot[1]), boneRot[2]);
    if (!isNormal) q = add(q, bonePivot);
    q = rotX(rotY(q, view.turn), view.tilt);
    return q;
  };
  return point;
}

// Draw the zombie. opts: turn and tilt in radians, scale, light from 0 to
// 1, opacity, the pose of the bones, and the center of the view.
function draw(ctx, model, opts) {
  const o = Object.assign({ turn: 0, tilt: -0.12, scale: 8, light: 1, opacity: 1, pose: {}, cx: ctx.canvas.width / 2, cy: ctx.canvas.height / 2 }, opts);
  const view = { turn: o.turn, tilt: o.tilt };
  const lightDir = (() => { const v = [-0.45, -0.75, -0.5]; const l = Math.hypot(...v); return v.map((x) => x / l); })();
  const list = [];
  for (const f of model.faces) {
    const tf = transformer(f.cube, o.pose, view, model.pivots || PIVOTS);
    const n = tf(f.n, true);
    if (n[2] >= -1e-4) continue;
    const O = tf(f.o);
    const U = sub(tf(add(f.o, f.ue)), O);
    const V = sub(tf(add(f.o, f.ve)), O);
    const center = add(O, add(mul(U, f.tw / 2), mul(V, f.th / 2)));
    const dot = -(n[0] * lightDir[0] + n[1] * lightDir[1] + n[2] * lightDir[2]);
    const shade = f.cube.glow ? 1 : (0.6 + 0.4 * Math.max(0, -dot)) * o.light;
    list.push({ f, O, U, V, z: center[2], shade });
  }
  list.sort((a, b) => b.z - a.z);
  ctx.save();
  for (const it of list) {
    const s = o.scale;
    ctx.setTransform(it.U[0] * s, it.U[1] * s, it.V[0] * s, it.V[1] * s, o.cx + it.O[0] * s, o.cy + it.O[1] * s);
    const { tw, th, cols } = it.f;
    const glow = it.f.cube.glow;
    for (let j = 0; j < th; j++) {
      for (let i = 0; i < tw; i++) {
        const c = cols[j * tw + i];
        if (!c) continue;
        const k = it.shade;
        ctx.globalAlpha = c[3] * (glow ? 1 : o.opacity);
        ctx.fillStyle = `rgb(${(c[0] * k) | 0},${(c[1] * k) | 0},${(c[2] * k) | 0})`;
        // A small overlap hides the seams between texels.
        ctx.fillRect(i - 0.02, j - 0.02, 1.04, 1.04);
      }
    }
  }
  ctx.restore();
}

// The zombie pose: arms to the front, with a small sway.
function pose(t, walk) {
  const sway = Math.sin(t * 1.6) * 0.06;
  const step = walk ? Math.sin(t * 3.2) * 0.45 : Math.sin(t * 1.2) * 0.05;
  return {
    head: [Math.sin(t * 0.9) * 0.05, Math.sin(t * 0.5) * 0.12, 0],
    right_arm: [-Math.PI / 2 + sway - (walk ? step * 0.2 : 0), 0.04, 0.02],
    left_arm: [-Math.PI / 2 - sway + (walk ? step * 0.2 : 0), -0.04, -0.02],
    right_leg: [step, 0, 0],
    left_leg: [-step, 0, 0],
  };
}

// The opacity of a zombie with the genes, from the translucent effect.
function opacityAt(genes, lightLevel) {
  let op = 1;
  for (const g of genes) {
    for (const e of g.effects.gains.concat(g.effects.downsides)) {
      if (e.type === "translucent" && lightLevel <= (e.max_light ?? 7)) op = Math.min(op, e.opacity ?? 0.1);
    }
  }
  return op;
}

export { bake, draw, pose, opacityAt, loadImage, toBaby, toAdult };
