// Shared helpers for the pages and the browser scripts.
// The data comes from src/data/site.json, which build.mjs makes from the mod files.

import { families, minors, hybrids, genes, notebook, version, species } from "../data/site.json";

const BASE = import.meta.env.BASE_URL.replace(/\/$/, "");

// Return a link inside the site, with the base path of GitHub Pages.
export const url = (p = "") => `${BASE}/${String(p).replace(/^\//, "")}`;

export const speciesById = Object.fromEntries(species.map((s) => [s.id, s]));
export const familiesOf = (sp) => families.filter((f) => f.species === sp);
export const minorsOf = (sp) => minors.filter((m) => m.species === sp);
export const hybridsOf = (sp) => hybrids.filter((h) => h.species === sp);
export const genesOf = (sp) => genes.filter((g) => g.species === sp);
export const famById = Object.fromEntries(families.map((f) => [f.id, f]));
export const geneById = Object.fromEntries(genes.map((g) => [g.id, g]));
export const famName = (id) => famById[id]?.name ?? id;
export const famVar = (id) => `var(--${id || "neutral"})`;
export const singular = (id) => id.charAt(0).toUpperCase() + id.slice(1);
export const label = (id) => {
  const s = String(id).replace(/_/g, " ");
  return s.charAt(0).toUpperCase() + s.slice(1);
};
export const pct = (x) => (x * 100).toFixed(x >= 0.1 || x === 0 ? 0 : 1) + "%";

const lang = notebook.lang;
export const t = (key, fallback) => lang[key] ?? fallback ?? key;
export const kindName = (kind) => (kind === "neutral" ? t("family.originofspawns.neutral", "Neutral") : t(`mob_info.originofspawns.kind.${kind}`, kind));
export const fmt = (key, ...args) => {
  let i = 0;
  return t(key).replace(/%s/g, () => args[i++]);
};

export const statusText = (s) => (s === "released" ? `In ${version}` : s === "development" ? "In development" : "Planned");
// The state of a species. A species that no milestone names is "In development".
export const speciesStatus = (sp) =>
  sp.status === "released" ? undefined : sp.milestone ? `${sp.milestone}, in development` : "In development";
export const statusClass = (s) => (s === "released" ? "ok" : s === "development" ? "warn" : "plan");

// The family of a set of genes of one species, with the rules of DESIGN.md
// and docs/data-packs.md. A gene can have a freq field, which breaks a tie.
export function familyOf(list, sp = list[0]?.species ?? "zombie") {
  const points = {};
  const best = {};
  for (const g of list) {
    points[g.family] = (points[g.family] || 0) + 2;
    if (g.sub) points[g.sub] = (points[g.sub] || 0) + 1;
    const f = g.freq ?? 0;
    best[g.family] = Math.max(best[g.family] || 0, f);
    if (g.sub) best[g.sub] = Math.max(best[g.sub] || 0, f);
  }
  const order = Object.keys(points).sort(
    (a, b) => points[b] - points[a] || (best[b] || 0) - (best[a] || 0) || (famById[a]?.slot ?? 0) - (famById[b]?.slot ?? 0),
  );
  const [a, b] = order;
  const pa = points[a] || 0;
  const pb = points[b] || 0;
  let r;
  if (pa < 4) r = { kind: "neutral", name: kindName("neutral") };
  else if (pb === pa) {
    const h = hybrids.find((x) => (x.a === a && x.b === b) || (x.a === b && x.b === a));
    r = { kind: "hybrid", name: h.name, a, b, style: h.style };
  } else if (pa >= 6 && pb === 0) r = { kind: "pure", name: famName(a), major: a };
  else if (pb >= 2) {
    const m = minors.find((x) => x.major === a && x.sub === b);
    r = { kind: "minor", name: m.name, major: a, sub: b, style: m.style };
  } else r = { kind: "major", name: famName(a), major: a };
  r.species = sp;
  r.points = order.map((id) => ({ id, points: points[id] }));
  r.color = famVar(r.major || r.a);
  return r;
}

// The key of a family result in the notebook. A major family alone uses the
// key of its family, as a pure family does.
export const resultKey = (r) =>
  `${r.species}:` +
  (r.kind === "neutral" ? "neutral" : r.kind === "hybrid" ? [r.a, r.b].sort().join("+") : r.kind === "minor" ? `${r.major}>${r.sub}` : r.major);

// A short line for an effect of a data file.
export function effectLine(e) {
  const parts = Object.entries(e)
    .filter(([k]) => k !== "type")
    .map(([k, v]) => {
      const val = typeof v === "string" ? v.replace(/^#?(minecraft|originofspawns):/, "")
        : Array.isArray(v) ? v.map((x) => (typeof x === "object" ? JSON.stringify(x) : x)).join(", ")
        : v && typeof v === "object" ? JSON.stringify(v) : v;
      return `${k.replace(/_/g, " ")} ${val}`;
    });
  return `${e.type}${parts.length ? ": " + parts.join(", ") : ""}`;
}

// The state that the visitor makes on one page and reads on another: the
// pools of the simulator and the families that the visitor found.
const KEY = "oots-wiki-v2";
export function loadState() {
  try {
    return Object.assign({ pools: {}, deaths: {}, found: [] }, JSON.parse(localStorage.getItem(KEY) || "{}"));
  } catch {
    return { pools: {}, deaths: {}, found: [] };
  }
}
export function saveState(state) {
  try {
    localStorage.setItem(KEY, JSON.stringify(state));
  } catch {
    // The browser does not keep the state. The page still works.
  }
}
export function recordFound(r) {
  const s = loadState();
  const key = resultKey(r);
  if (!s.found.includes(key)) {
    s.found.push(key);
    saveState(s);
  }
}
