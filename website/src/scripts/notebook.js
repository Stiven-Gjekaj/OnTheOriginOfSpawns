// The notebook page: the gene page and the family page of each species, and
// the mob info panel. The pages read the state that the simulator keeps in the
// browser.

import { genes, families, minors, hybrids } from "../data/site.json";
import { familyOf, kindName, pct, t, loadState, url, speciesById, an } from "../lib/site.js";
import { getSpecies } from "./species.js";

const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
const $ = (id) => document.getElementById(id);
const state = loadState();
let category = "temperate";
let species = getSpecies();
const mine = (list) => list.filter((x) => x.species === species);

// The gene page lists all the genes of the species, by family, with their
// frequency in the pool of the simulator. A pool that nobody changed is 0%.
function renderGenes() {
  const p = state.pools[`${species}:${category}`] || {};
  const list = mine(genes);
  const groups = mine(families).map((f) => `
    <div class="freq-group" style="--fam:var(--${f.id})">${f.name}</div>` +
    list.filter((g) => g.family === f.id).map((g) => `
    <div class="freq" style="--fam:var(--${g.family})"><span class="nm" data-tip-ref="tip-gene-${g.id}">${g.name}</span>
    <span class="bar"><span style="width:${(p[g.id] || 0) * 100}%"></span></span><span class="v">${pct(p[g.id] || 0)}</span></div>`).join(""));
  $("book-freq").innerHTML = groups.join("");
  const known = list.filter((g) => (p[g.id] || 0) > 0).sort((a, b) => p[b.id] - p[a.id]);
  // The field sketch: the picture of the strongest gene, or of a plain mob.
  const shot = known[0]?.shot || speciesById[species]?.shot;
  const img = $("sketch");
  if (shot) img.src = url(shot.front); else img.removeAttribute("src");
  img.alt = known[0] ? `A field sketch of ${an(species)} ${species.replace(/_/g, " ")} with ${known[0].name}` : `A field sketch of a plain ${species}`;
}

function renderFamilies() {
  const k = (x) => `${species}:${x}`;
  const all = [
    ...mine(families).map((f) => ({ key: k(f.id), name: f.name, kind: "pure" })),
    ...mine(minors).map((m) => ({ key: k(`${m.major}>${m.sub}`), name: m.name, kind: "minor" })),
    ...mine(hybrids).map((h) => ({ key: k([h.a, h.b].sort().join("+")), name: h.name, kind: "hybrid" })),
    { key: k("neutral"), name: kindName("neutral"), kind: "neutral" },
  ];
  // The family page shows all the family results of the species from the start.
  $("found-count").textContent = `${all.length} family results`;
  const link = url(`families/?species=${species}`);
  document.querySelectorAll(".found[data-kind]").forEach((box) => {
    box.innerHTML = all.filter((x) => x.kind === box.dataset.kind)
      .map((x) => `<a class="f" href="${link}${x.kind === "pure" ? `#${x.key.split(":")[1]}` : ""}">${x.name}</a>`).join("");
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
  // The wiki has a picture of each gene alone, so the mob has one gene.
  zombie = shuffled.slice(0, 1);
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
  const shot = zombie[0]?.shot || speciesById[species]?.shot;
  const img = $("scene-shot");
  if (shot) img.src = url(shot.front); else img.removeAttribute("src");
  img.alt = zombie[0] ? `${an(species)} ${species.replace(/_/g, " ")} with the gene ${zombie[0].name}` : `A plain ${species}`;
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

// On a phone the ribbons scroll. Keep the ribbon of the species in view.
function showRibbon() {
  const b = document.querySelector(".ribbon[aria-pressed='true']");
  const row = b?.parentElement;
  if (row && row.scrollWidth > row.clientWidth) row.scrollLeft = b.offsetLeft - (row.clientWidth - b.offsetWidth) / 2;
}
document.addEventListener("species", showRibbon);
showRibbon();
