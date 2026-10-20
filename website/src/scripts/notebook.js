// The notebook page: the gene page and the family page of the zombie, and the
// mob info panel. The pages read the state that the simulator and the Gene
// Lab keep in the browser.

import { genes, families, minors, hybrids } from "../data/site.json";
import { familyOf, kindName, pct, fmt, t, loadState } from "../lib/site.js";
import { drawStill } from "./zombie-canvas.js";
import * as Z from "./zombie.js";

const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
const $ = (id) => document.getElementById(id);
const state = loadState();
let category = "temperate";

function renderGenes() {
  const p = state.pools[category] || {};
  const known = genes.filter((g) => (p[g.id] || 0) > 0).sort((a, b) => p[b.id] - p[a.id]);
  $("book-freq").innerHTML = known.slice(0, 12).map((g) => `
    <div class="freq" style="--fam:var(--${g.family})"><span class="nm">${g.name}</span>
    <span class="bar"><span style="width:${p[g.id] * 100}%"></span></span><span class="v">${pct(p[g.id])}</span></div>`).join("");
  $("book-empty").hidden = known.length > 0;
  drawStill($("sketch"), known.slice(0, 5), { w: 120, h: 160, scale: 3.6, cy: 0.33, turn: -0.6 });
}

function renderFamilies() {
  const all = [
    ...families.map((f) => ({ key: f.id, name: f.name, kind: "pure" })),
    ...minors.map((m) => ({ key: `${m.major}>${m.sub}`, name: m.name, kind: "minor" })),
    ...hybrids.map((h) => ({ key: [h.a, h.b].sort().join("+"), name: h.name, kind: "hybrid" })),
    { key: "neutral", name: kindName("neutral"), kind: "neutral" },
  ];
  const found = new Set(loadState().found);
  $("found-count").textContent = fmt("screen.originofspawns.notebook.found", all.filter((x) => found.has(x.key)).length, all.length);
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
  const shuffled = [...genes].sort(() => Math.random() - 0.5);
  zombie = shuffled.slice(0, 1 + Math.floor(Math.random() * 4));
  drawInfo();
}
function drawInfo() {
  const r = familyOf(zombie);
  const box = $("mob-info");
  box.classList.add("fade");
  setTimeout(() => {
    box.innerHTML = `<div class="n" style="color:${r.color}">${r.name}</div>` +
      (r.kind === "neutral" ? "" : `<div class="k">${kindName(r.kind)}</div>`) +
      `<div class="g">${zombie.length ? zombie.map((g) => g.name).join(", ") : t("mob_info.originofspawns.no_genes", "No genes")}</div>`;
    box.classList.remove("fade");
  }, 160);
  const night = $("scene").classList.contains("night");
  drawStill($("scene-zombie"), zombie, { w: 150, h: 200, scale: 4.6, cy: 0.33, turn: -0.25, light: night ? 0.5 : 1, opacity: night ? Z.opacityAt(zombie, 0) : 1 });
}
$("info-next").addEventListener("click", nextZombie);
$("info-night").addEventListener("click", (e) => {
  const on = e.currentTarget.getAttribute("aria-pressed") !== "true";
  e.currentTarget.setAttribute("aria-pressed", String(on));
  $("scene").classList.toggle("night", on);
  drawInfo();
});

renderGenes();
renderFamilies();
nextZombie();
