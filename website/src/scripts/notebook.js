// The notebook page: the gene page and the family page of the zombie, and the
// mob info panel. The pages read the state that the simulator and the Gene
// Lab keep in the browser.

import { genes, families, minors, hybrids } from "../data/site.json";
import { familyOf, kindName, pct, fmt, t, loadState } from "../lib/site.js";
import { getSpecies } from "./species.js";
import { drawStill } from "./zombie-canvas.js";
import * as Z from "./zombie.js";

const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
const $ = (id) => document.getElementById(id);
const state = loadState();
let category = "temperate";
let species = getSpecies();
const mine = (list) => list.filter((x) => x.species === species);

function renderGenes() {
  const p = state.pools[`${species}:${category}`] || {};
  const known = mine(genes).filter((g) => (p[g.id] || 0) > 0).sort((a, b) => p[b.id] - p[a.id]);
  $("book-freq").innerHTML = known.slice(0, 12).map((g) => `
    <div class="freq" style="--fam:var(--${g.family})"><span class="nm">${g.name}</span>
    <span class="bar"><span style="width:${p[g.id] * 100}%"></span></span><span class="v">${pct(p[g.id])}</span></div>`).join("");
  $("book-empty").hidden = known.length > 0;
  drawStill($("sketch"), known.slice(0, 5), { w: 120, h: 160, scale: 3.6, cy: 0.33, turn: -0.6, species });
}

function renderFamilies() {
  const k = (x) => `${species}:${x}`;
  const all = [
    ...mine(families).map((f) => ({ key: k(f.id), name: f.name, kind: "pure" })),
    ...mine(minors).map((m) => ({ key: k(`${m.major}>${m.sub}`), name: m.name, kind: "minor" })),
    ...mine(hybrids).map((h) => ({ key: k([h.a, h.b].sort().join("+")), name: h.name, kind: "hybrid" })),
    { key: k("neutral"), name: kindName("neutral"), kind: "neutral" },
  ];
  const found = new Set(loadState().found);
  // The game counts known families and seen families. The wiki records a
  // family when the visitor spawns it, so each found family is known.
  const n = all.filter((x) => found.has(x.key)).length;
  const slots = (t("screen.originofspawns.notebook.found").match(/%s/g) || []).length;
  $("found-count").textContent = slots >= 3
    ? fmt("screen.originofspawns.notebook.found", n, 0, all.length)
    : fmt("screen.originofspawns.notebook.found", n, all.length);
  document.querySelectorAll(".found[data-kind]").forEach((box) => {
    box.innerHTML = all.filter((x) => x.kind === box.dataset.kind)
      .map((x) => `<span class="f${found.has(x.key) ? "" : " no"}">${found.has(x.key) ? x.name : "?"}</span>`).join("");
  });
}

function showTab(tab) {
  const genesTab = tab === "genes";
  ["sheet-genes-left", "sheet-genes-right"].forEach((id) => { $(id).hidden = !genesTab; });
  ["sheet-fam-left", "sheet-fam-right"].forEach((id) => { $(id).hidden = genesTab; });
  if (!genesTab) renderFamilies();
}

document.querySelector(".edge-tabs").addEventListener("click", (e) => {
  const b = e.target.closest("[data-tab]");
  if (!b || b.getAttribute("aria-pressed") === "true") return;
  document.querySelectorAll(".edge-tab").forEach((x) => x.setAttribute("aria-pressed", String(x === b)));
  if (reduced) return showTab(b.dataset.tab);
  const leaf = document.createElement("div");
  leaf.className = "page-turn" + (b.dataset.tab === "genes" ? " back" : "");
  $("spread").appendChild(leaf);
  setTimeout(() => showTab(b.dataset.tab), 300);
  setTimeout(() => leaf.remove(), 620);
});

$("book-cats").addEventListener("click", (e) => {
  const b = e.target.closest("[data-cat]");
  if (!b) return;
  category = b.dataset.cat;
  document.querySelectorAll("#book-cats [data-cat]").forEach((x) => x.setAttribute("aria-pressed", String(x === b)));
  $("book-cat-name").textContent = b.textContent.trim();
  renderGenes();
});

// The mob info panel on a scene.
let zombie = [];
function nextZombie() {
  const shuffled = mine(genes).sort(() => Math.random() - 0.5);
  zombie = shuffled.slice(0, 1 + Math.floor(Math.random() * 4));
  drawInfo();
}
function drawInfo() {
  const r = familyOf(zombie, species);
  const box = $("mob-info");
  box.classList.add("fade");
  setTimeout(() => {
    box.innerHTML = `<div class="n" style="color:${r.color}">${r.name}</div>` +
      (r.kind === "neutral" ? "" : `<div class="k">${kindName(r.kind)}</div>`) +
      `<div class="g">${zombie.length ? zombie.map((g) => g.name).join(", ") : t("mob_info.originofspawns.no_genes", "No genes")}</div>`;
    box.classList.remove("fade");
  }, 160);
  const night = $("scene").classList.contains("night");
  drawStill($("scene-zombie"), zombie, { w: 150, h: 200, scale: 4.6, cy: 0.33, turn: -0.25, species, light: night ? 0.5 : 1, opacity: night ? Z.opacityAt(zombie, 0) : 1 });
}
$("info-next").addEventListener("click", nextZombie);
$("info-night").addEventListener("click", (e) => {
  const on = e.currentTarget.getAttribute("aria-pressed") !== "true";
  e.currentTarget.setAttribute("aria-pressed", String(on));
  $("scene").classList.toggle("night", on);
  drawInfo();
});

document.addEventListener("species", (e) => {
  species = e.detail;
  renderGenes();
  renderFamilies();
  nextZombie();
});

renderGenes();
renderFamilies();
nextZombie();
