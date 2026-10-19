// Make the data of the wiki from the files of the mod.
//
// Usage: node website/build.mjs
//
// The script reads the gene, family, and pressure JSON files, the language
// file, the gene visuals and models, the design files, and the data pack
// guide. It writes website/js/data.js and copies the mod textures that the
// gene viewer uses into website/mod/. Run it again when the data changes.
// The script needs Node 18 or later, and no packages.

import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const SITE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.dirname(SITE);
const RES = path.join(ROOT, "src/main/resources");
const DATA = path.join(RES, "data/originofspawns/originofspawns");
const ASSETS = path.join(RES, "assets/originofspawns");
const NS = "originofspawns";

const read = (p) => fs.readFileSync(path.join(ROOT, p), "utf8");
const readJson = (file) => JSON.parse(fs.readFileSync(file, "utf8"));
const jsonDir = (dir) =>
  fs.existsSync(dir)
    ? fs.readdirSync(dir).filter((f) => f.endsWith(".json")).sort()
        .map((f) => ({ id: f.slice(0, -5), json: readJson(path.join(dir, f)) }))
    : [];

// ---------------------------------------------------------------------------
// Markdown helpers

// Split a Markdown file into sections. Each section has the heading level,
// the title, and its lines up to the next heading.
function sections(md) {
  const out = [{ level: 0, title: "", lines: [] }];
  let fence = false;
  for (const line of md.split("\n")) {
    if (/^\s*```/.test(line)) fence = !fence;
    const m = !fence && /^(#{1,6})\s+(.*)$/.exec(line);
    if (m) out.push({ level: m[1].length, title: m[2].trim(), lines: [] });
    else out[out.length - 1].lines.push(line);
  }
  return out;
}

function splitRow(line) {
  return line.trim().replace(/^\|/, "").replace(/\|$/, "").split("|").map((c) => c.trim());
}

// Return the tables in a list of lines. Each table is a list of objects with
// the header cells as keys.
function tables(lines) {
  const out = [];
  for (let i = 0; i < lines.length - 1; i++) {
    if (/^\s*\|/.test(lines[i]) && /^\s*\|[\s:|-]+\|\s*$/.test(lines[i + 1])) {
      const head = splitRow(lines[i]);
      const rows = [];
      let j = i + 2;
      for (; j < lines.length && /^\s*\|/.test(lines[j]); j++) {
        const cells = splitRow(lines[j]);
        rows.push(Object.fromEntries(head.map((h, k) => [h, cells[k] ?? ""])));
      }
      rows.head = head;
      out.push(rows);
      i = j;
    }
  }
  return out;
}

function paragraphs(lines) {
  return lines.join("\n").split(/\n\s*\n/).map((p) => p.trim())
    .filter((p) => p && !p.startsWith("|") && !p.startsWith("-") && !p.startsWith("```"))
    .map((p) => p.replace(/\s*\n\s*/g, " "));
}

function bullets(lines) {
  const out = [];
  for (const line of lines) {
    const m = /^(\s*)- (.*)$/.exec(line);
    if (m) out.push({ depth: m[1].length >= 2 ? 1 : 0, text: m[2].trim() });
    else if (out.length && /^\s+\S/.test(line)) out[out.length - 1].text += " " + line.trim();
  }
  return out;
}

// Plain text from inline Markdown: no bold, no code marks, and link text only.
const plain = (s) => s.replace(/\*\*([^*]+)\*\*/g, "$1").replace(/`([^`]*)`/g, "$1")
  .replace(/\[([^\]]+)\]\([^)]+\)/g, "$1").replace(/(^|\W)_([^_]+)_(?=\W|$)/g, "$1$2");
const plainDeep = (v) => typeof v === "string" ? plain(v)
  : Array.isArray(v) ? v.map(plainDeep)
  : v && typeof v === "object" ? Object.fromEntries(Object.entries(v).map(([k, x]) => [k, plainDeep(x)])) : v;

const esc = (s) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

// Inline Markdown: code, bold, italic, and links. Links to other files of
// the repository go to GitHub.
function inline(s) {
  const parts = s.split(/(`[^`]*`)/);
  return parts.map((p) => {
    if (p.startsWith("`") && p.endsWith("`") && p.length > 1) return `<code>${esc(p.slice(1, -1))}</code>`;
    return esc(p)
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_, t, href) => {
        const url = /^https?:/.test(href) ? href : `${REPO}/blob/main/${href.replace(/^\.\.\//, "design/")}`;
        return `<a href="${url}" target="_blank" rel="noopener">${t}</a>`;
      })
      .replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>")
      .replace(/(^|\W)_([^_]+)_(?=\W|$)/g, "$1<em>$2</em>");
  }).join("");
}

// A small Markdown to HTML converter for the data pack guide. It knows
// headings, paragraphs, lists, tables, and indented code blocks.
function mdToHtml(md, { skipFirstHeading = true } = {}) {
  const lines = md.split("\n");
  const html = [];
  const toc = [];
  let i = 0;
  let firstHeading = true;
  const slug = (t) => t.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
  while (i < lines.length) {
    const line = lines[i];
    if (!line.trim()) { i++; continue; }
    const h = /^(#{1,6})\s+(.*)$/.exec(line);
    if (h) {
      if (firstHeading && skipFirstHeading) { firstHeading = false; i++; continue; }
      const level = Math.min(h[1].length + 1, 6);
      const id = "dp-" + slug(h[2]);
      if (level <= 4) toc.push({ level, id, title: h[2] });
      html.push(`<h${level} id="${id}">${inline(h[2])}</h${level}>`);
      i++;
      continue;
    }
    if (/^ {4}\S/.test(line)) {
      const code = [];
      while (i < lines.length && (/^ {4}/.test(lines[i]) || !lines[i].trim())) {
        code.push(lines[i].slice(4));
        i++;
      }
      while (code.length && !code[code.length - 1].trim()) code.pop();
      html.push(`<pre class="code"><code>${esc(code.join("\n"))}</code></pre>`);
      continue;
    }
    if (/^\s*\|/.test(line)) {
      const block = [];
      while (i < lines.length && /^\s*\|/.test(lines[i])) block.push(lines[i++]);
      const [t] = tables(block);
      if (t) {
        const head = t.head.map((c) => `<th>${inline(c)}</th>`).join("");
        const body = t.map((r) => `<tr>${t.head.map((c) => `<td>${inline(r[c])}</td>`).join("")}</tr>`).join("");
        html.push(`<div class="table-wrap"><table><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table></div>`);
      }
      continue;
    }
    if (/^\s*- /.test(line)) {
      const items = [];
      while (i < lines.length && (/^\s*- /.test(lines[i]) || /^\s{2,}\S/.test(lines[i]))) {
        if (/^\s*- /.test(lines[i])) items.push(lines[i].replace(/^\s*- /, ""));
        else items[items.length - 1] += " " + lines[i].trim();
        i++;
      }
      html.push(`<ul>${items.map((t) => `<li>${inline(t)}</li>`).join("")}</ul>`);
      continue;
    }
    const para = [];
    while (i < lines.length && lines[i].trim() && !/^(#|\s*\||\s*- | {4}\S)/.test(lines[i])) para.push(lines[i++].trim());
    html.push(`<p>${inline(para.join(" "))}</p>`);
  }
  return { html: html.join("\n"), toc };
}

// ---------------------------------------------------------------------------
// Sources

const REPO = "https://github.com/Stiven-Gjekaj/OnTheOriginOfSpawns";
const lang = readJson(path.join(ASSETS, "lang/en_us.json"));
const t = (key, fallback) => lang[key] ?? fallback ?? key;
const short = (id) => id.split(/[:/]/).pop();

const zombieMd = sections(read("design/species/zombie.md"));
const designMd = sections(read("design/DESIGN.md"));
const roadmapMd = sections(read("design/ROADMAP.md"));
const readme = read("README.md");
const section = (secs, title) => secs.find((s) => s.title === title);

// Links: the download badges of the README.
function badgeLink(name) {
  const m = new RegExp(`\\]\\((https://[^)]*${name}[^)]*)\\)`, "i").exec(readme);
  return m ? m[1] : null;
}
const links = {
  modrinth: badgeLink("modrinth.com"),
  curseforge: badgeLink("curseforge.com"),
  kofi: badgeLink("ko-fi.com/"),
  github: REPO,
  releases: badgeLink("releases"),
};
const tagline = (/^### (.*)$/m.exec(readme) || [])[1] || "";
const subtitle = (/^_(.*)_$/m.exec(readme) || [])[1] || "";
const overview = paragraphs(section(sections(readme), "Overview").lines);
const features = bullets(section(sections(readme), "Features").lines).map((b) => b.text);
const minecraft = (/badge\/minecraft-([\d.]+)-/.exec(readme) || [])[1];
const fabric = (/badge\/fabric-([\d.]+)-/.exec(readme) || [])[1];
const modVersion = (/^mod_version\s*=\s*(.*)$/m.exec(read("gradle.properties")) || [])[1]?.trim();

// Roadmap: each milestone, its state, and its items.
const roadmap = [];
for (const s of roadmapMd) {
  const m = /^(Milestone [\w]+|Beta stage):\s*(.*)$/.exec(s.title);
  if (!m) continue;
  const done = /\(done\)/.test(m[2]);
  const after = roadmapMd.indexOf(s) > roadmapMd.findIndex((x) => x.title === "After version 1.0.0");
  roadmap.push({
    key: m[1],
    title: m[2].replace(/\s*\(done\)\s*/, ""),
    done,
    after1: after,
    items: bullets(s.lines).filter((b) => b.depth === 0).map((b) => b.text),
    text: s.lines.join(" "),
  });
}
const milestoneOf = (key) => roadmap.find((r) => r.key === key);

// Families: the JSON files, the names, and the text of zombie.md.
const familyOrder = ["predator", "stalker", "tank", "horde", "blight"];
const familyJson = Object.fromEntries(jsonDir(path.join(DATA, "family/zombie")).map((f) => [f.id, f.json]));
const families = familyOrder.filter((id) => familyJson[id]).map((id) => {
  const name = t(`family.${NS}.zombie.${id}`);
  const sec = section(zombieMd, name);
  const tierRows = sec ? tables(sec.lines)[0] : [];
  return {
    id,
    name,
    summary: sec ? paragraphs(sec.lines)[0] : "",
    tiers: [1, 2, 3].map((n) => {
      const row = tierRows.find((r) => r.Tier === `Tier ${n}`) || {};
      const data = familyJson[id].tiers[n - 1];
      return { buff: row.Buff || "", debuff: row.Debuff || "", buffs: data.buffs, debuffs: data.debuffs };
    }),
  };
});
for (const id of Object.keys(familyJson)) {
  if (!familyOrder.includes(id)) throw new Error(`family ${id} is not in the order list of build.mjs`);
}

const styleTable = (title) => tables(section(zombieMd, title).lines)[0];
const minorStyles = styleTable("Minor families");
const minors = [];
for (const major of familyOrder) {
  for (const sub of familyOrder) {
    if (major === sub) continue;
    const name = t(`family.${NS}.zombie.${major}.${sub}`);
    const row = minorStyles.find((r) => r["Minor family"] === name) || {};
    minors.push({ major, sub, name, style: row["Play style"] || "" });
  }
}
const hybridStyles = styleTable("Hybrids");
const hybrids = [];
for (let i = 0; i < familyOrder.length; i++) {
  for (let j = i + 1; j < familyOrder.length; j++) {
    // The language key puts the first family by id first.
    const [a, b] = [familyOrder[i], familyOrder[j]].sort();
    const name = t(`family.${NS}.zombie.${a}.and.${b}`);
    const row = hybridStyles.find((r) => r.Hybrid === name) || {};
    hybrids.push({ a: familyOrder[i], b: familyOrder[j], name, style: row["Play style"] || "" });
  }
}
const resultRules = tables(section(designMd, "Families").lines)
  .find((tb) => tb.head[0] === "Points of the genes")
  .map((r) => ({ points: r["Points of the genes"], result: r.Result }));
const powerOrder = (bullets(section(designMd, "Families").lines)
  .map((b) => b.text).find((x) => /order of power/.test(x)) || "")
  .split(/\s*\d+\.\s+/).slice(1).map((x) => x.replace(/\.$/, ""));

// Pressures: the JSON files and the table in zombie.md.
const pressureRows = tables(section(zombieMd, "Pressures").lines)[0];
const pressures = jsonDir(path.join(DATA, "pressure")).map(({ id, json }) => {
  const row = pressureRows.find((r) => r.Pressure === "`" + id + "`") || {};
  return {
    id,
    damageTypes: json.damage_types || null,
    conditions: json.conditions || [],
    when: row["A zombie death counts when"] || "",
    status: row.State === "In the mod" ? "released" : "development",
  };
});

// Genes: the JSON files, the names, the text of zombie.md, and the visuals.
const geneRows = {};
for (const s of zombieMd) {
  if (!/ genes$/.test(s.title)) continue;
  for (const tb of tables(s.lines)) for (const r of tb) geneRows[r.Gene] = { ...r, index: Object.keys(geneRows).length };
}
const detailRows = Object.fromEntries(
  tables(section(zombieMd, "Gene details").lines)[0].map((r) => [r.Gene, r.Rules]));
const doneText = roadmap.filter((r) => r.done).map((r) => r.text).join(" ");
const m2a = milestoneOf("Milestone 2a");
const textures = new Set();

function visualOf(id) {
  const file = path.join(ASSETS, "gene_visuals", id + ".json");
  if (!fs.existsSync(file)) return null;
  const vis = readJson(file);
  for (const layer of vis.layers || []) if (layer.effect.texture) textures.add(layer.effect.texture);
  textures.add(vis.region_map || `${NS}:textures/region/humanoid.png`);
  let model = null;
  if (vis.model) {
    const [ns, name] = vis.model.split(":");
    model = readJson(path.join(RES, "assets", ns, "gene_models", name + ".json"));
    // A model with mob_texture uses the baked texture of the mob, as in the game.
    if (model.texture) textures.add(model.texture);
    else if (!model.mob_texture) {
      throw new Error(`the gene model ${vis.model} needs a texture, or mob_texture set to true`);
    }
  }
  return { layers: vis.layers || [], regionMap: vis.region_map || null, model };
}

const genes = jsonDir(path.join(DATA, "gene")).map(({ id, json }) => {
  const name = t(`gene.${NS}.${id}`);
  const row = geneRows[name] || {};
  if (!geneRows[name]) console.warn(`warning: zombie.md has no row for the gene ${name}`);
  const released = new RegExp(`\\b${name}\\b`).test(doneText);
  return {
    id,
    name,
    species: [].concat(json.species),
    family: short(json.family),
    sub: json.sub_family ? short(json.sub_family) : null,
    counters: short(json.counters),
    gains: row.Gains || "",
    downsides: row.Downsides || "",
    look: row.Visual || "",
    details: detailRows[name] || "",
    effects: { gains: json.gains || [], downsides: json.downsides || [] },
    rates: {
      selection: json.selection_rate ?? 0.02,
      mutation: json.mutation_rate ?? 0.003,
      decay: json.decay_rate ?? 0.01,
    },
    status: released ? "released" : m2a && !m2a.done ? "development" : "released",
    visual: visualOf(id),
  };
});
genes.sort((a, b) => (geneRows[a.name]?.index ?? 99) - (geneRows[b.name]?.index ?? 99));

// A pressure that a released gene counters is in the release too.
for (const p of pressures) {
  if (genes.some((g) => g.status === "released" && g.counters === p.id)) p.status = "released";
}

// The text of DESIGN.md for the evolution page.
const designBullets = (title) => bullets(section(designMd, title).lines).map((b) => b.text);
const evolution = {
  pools: designBullets("Gene pools"),
  selection: designBullets("Selection"),
  balance: designBullets("Balance"),
  visuals: designBullets("Gene visuals"),
  extinction: designBullets("Extinction"),
  multiplayer: designBullets("Multiplayer"),
  categories: ["hot", "cold", "wet", "temperate", "nether", "end"].map((id) => ({
    id, name: t(`biome_category.${NS}.${id}`),
  })),
  babies: bullets(section(zombieMd, "Baby zombies").lines).map((b) => b.text),
};
const notebook = {
  design: designBullets("Beagle Notebook"),
  mobInfo: designBullets("Mob info on hover"),
  knowledge: tables(section(designMd, "Player adaptations").lines)[0]
    .map((r) => ({ knowledge: r.Knowledge, slots: r["Perk slots"] })),
  m4: milestoneOf("Milestone 4"),
  lang: Object.fromEntries(Object.entries(lang).filter(([k]) => /^(screen|mob_info)\./.test(k))),
  itemName: t(`item.${NS}.beagle_notebook`),
};
const perks = tables(section(designMd, "Player adaptations").lines)[1]
  .map((r) => ({ name: r.Perk, gain: r.Gain, cost: r.Cost }));
const dataPacks = mdToHtml(read("docs/data-packs.md"));

// ---------------------------------------------------------------------------
// Output

const out = {
  generated: new Date().toISOString().slice(0, 10),
  repo: REPO,
  version: modVersion,
  minecraft,
  fabric,
  links,
  tagline,
  subtitle,
  overview: plainDeep(overview),
  features: plainDeep(features),
  roadmap: plainDeep(roadmap.map(({ text, ...r }) => r)),
  families,
  minors,
  hybrids,
  resultRules,
  powerOrder,
  pressures: plainDeep(pressures),
  genes: genes.map((g) => ({ ...g, gains: plain(g.gains), downsides: plain(g.downsides), look: plain(g.look), details: plain(g.details) })),
  evolution: plainDeep(evolution),
  notebook: { ...plainDeep(notebook), lang: notebook.lang },
  perks: plainDeep(perks),
  dataPacks,
};

fs.mkdirSync(path.join(SITE, "js"), { recursive: true });
fs.writeFileSync(
  path.join(SITE, "js/data.js"),
  "// This file is made by website/build.mjs. Do not edit it.\n" +
    "window.SITE_DATA = " + JSON.stringify(out, null, 1) + ";\n",
);

// Copy the textures of the gene viewer. Remove old copies first, so that a
// texture that the mod removes also goes from the site.
const modDir = path.join(SITE, "mod");
fs.rmSync(modDir, { recursive: true, force: true });
for (const id of textures) {
  const [ns, p] = id.split(":");
  const src = path.join(RES, "assets", ns, p);
  const dst = path.join(modDir, ns, p);
  fs.mkdirSync(path.dirname(dst), { recursive: true });
  fs.copyFileSync(src, dst);
}
const img = path.join(SITE, "img");
fs.mkdirSync(img, { recursive: true });
fs.copyFileSync(path.join(ASSETS, "textures/item/beagle_notebook.png"), path.join(img, "beagle_notebook.png"));

const withVisual = genes.filter((g) => g.visual).length;
console.log(`genes: ${genes.length} (${withVisual} with a visual), families: ${families.length}, ` +
  `minor families: ${minors.length}, hybrids: ${hybrids.length}, pressures: ${pressures.length}, ` +
  `textures: ${textures.size}, milestones: ${roadmap.length}`);
