// The pool simulator of the evolution page. It uses the rates of the gene
// files and keeps the pools in the browser, so that the notebook can read them.

import { genes } from "../data/site.json";
import { familyOf, kindName, pct, t, loadState, saveState, recordFound } from "../lib/site.js";
import { drawStill } from "./zombie-canvas.js";
import { getSpecies } from "./species.js";

const state = loadState();
let category = "temperate";
let species = getSpecies();
let zombie = [];

// The pools have the key <species>:<category>.
const mine = () => genes.filter((g) => g.species === species);
const key = () => `${species}:${category}`;
const pool = () => (state.pools[key()] ||= Object.fromEntries(mine().map((g) => [g.id, 0])));
const $ = (id) => document.getElementById(id);

function render(hit = new Set()) {
  const p = pool();
  const genes = mine();
  for (const g of genes) p[g.id] ??= 0;
  const top = new Set(Object.entries(p).filter(([, f]) => f > 0).sort((a, b) => b[1] - a[1]).slice(0, 5).map(([id]) => id));
  const rows = [...genes].sort((a, b) => p[b.id] - p[a.id]);
  $("sim-pool").innerHTML = rows.map((g) => `
    <div class="pool-row${hit.has(g.id) ? " hit" : ""}${top.has(g.id) ? " top" : ""}">
      <span class="name" style="color:var(--${g.family})" data-tip-ref="tip-gene-${g.id}">${g.name}</span>
      <div class="xp"><span style="--w:${p[g.id] * 100}%"></span></div>
      <span class="val">${pct(p[g.id])}</span>
    </div>`).join("");
  $("sim-deaths").textContent = `Deaths in this pool: ${state.deaths[key()] || 0}`;
  $("sim-art").hidden = genes.some((g) => g.visual);
  saveState(state);
}

function spawn() {
  const p = pool();
  let got = mine()
    .filter((g) => Math.random() < Math.max(p[g.id], g.rates.mutation))
    .map((g) => ({ ...g, freq: p[g.id] }))
    .sort((a, b) => b.freq - a.freq);
  const dropped = Math.max(0, got.length - 5);
  got = got.slice(0, 5);
  zombie = got;
  const r = familyOf(got, species);
  recordFound(r);
  $("sim-result").innerHTML = `<div class="n" style="color:${r.color}">${r.name}</div>` +
    (r.kind === "neutral" ? "" : `<div class="k">${kindName(r.kind)}</div>`) +
    `<div class="g">${got.length ? got.map((g) => g.name).join(", ") : t("mob_info.originofspawns.no_genes", "No genes")}</div>`;
  $("sim-log").textContent = dropped
    ? `The mob rolled ${got.length + dropped} genes. It keeps the 5 with the highest frequency.`
    : "Each gene had a chance equal to its frequency.";
  draw();
}

const draw = () => drawStill($("sim-canvas"), zombie, { w: 180, h: 240, scale: 5.2, cy: 0.32, turn: -0.55, species });

$("sim-cats").addEventListener("click", (e) => {
  const b = e.target.closest("[data-cat]");
  if (!b) return;
  category = b.dataset.cat;
  document.querySelectorAll("#sim-cats [data-cat]").forEach((x) => x.setAttribute("aria-pressed", String(x === b)));
  render();
});
document.addEventListener("click", (e) => {
  const b = e.target.closest(".sim-pressures [data-pressure]");
  if (!b) return;
  const p = pool();
  const hit = new Set();
  for (let i = 0; i < 5; i++) {
    for (const g of mine()) {
      if (g.counters !== b.dataset.pressure) continue;
      p[g.id] += g.rates.selection * (1 - p[g.id]);
      hit.add(g.id);
    }
  }
  state.deaths[key()] = (state.deaths[key()] || 0) + 5;
  render(hit);
});
$("sim-day").addEventListener("click", () => {
  const p = pool();
  for (const g of mine()) p[g.id] *= 1 - g.rates.decay;
  render();
});
$("sim-reset").addEventListener("click", () => {
  delete state.pools[key()];
  state.deaths[key()] = 0;
  render();
});
$("sim-spawn").addEventListener("click", spawn);
document.addEventListener("species", (e) => {
  species = e.detail;
  zombie = [];
  $("sim-result").innerHTML = `<div class="n">No mob yet</div><div class="g">Press the button</div>`;
  $("sim-log").textContent = "";
  render();
  draw();
});

render();
draw();
