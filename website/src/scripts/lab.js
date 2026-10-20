// The Gene Lab: a zombie with the genes that the visitor selects.

import { genes } from "../data/site.json";
import { familyOf, kindName, famName, recordFound } from "../lib/site.js";
import * as Z from "./zombie.js";

const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
const byId = Object.fromEntries(genes.map((g) => [g.id, g]));
const $ = (id) => document.getElementById(id);

const start = (new URLSearchParams(location.search).get("genes") || "cinder,ironhide,bonerunner").split(",").filter((id) => byId[id]);
const chosen = new Set(start.slice(0, 5));
let spin = !reduced;
let walk = false;
let baby = false;
let night = false;
let turn = -0.5;
let model = null;

const canvas = $("lab-canvas");
const dpr = Math.min(2, window.devicePixelRatio || 1);
canvas.width = 300 * dpr;
canvas.height = 400 * dpr;
const ctx = canvas.getContext("2d");

const press = (el, on) => el.setAttribute("aria-pressed", String(on));

function update() {
  document.querySelectorAll("[data-gene]").forEach((b) => press(b, chosen.has(b.dataset.gene)));
  $("lab-count").textContent = `${chosen.size} of 5`;
  const list = [...chosen].map((id) => byId[id]);
  const r = familyOf(list);
  if (list.length) recordFound(r);
  $("lab-result").innerHTML = `
    <div class="mob-info"><div class="n" style="color:${r.color}">${r.name}</div>
    <div class="k">${r.kind === "neutral" ? "No family effects" : kindName(r.kind)}</div></div>
    ${r.style ? `<div class="muted small">${r.style}</div>` : ""}
    <div class="row" style="justify-content:center">${r.points.map((p) => `<span class="badge fam" style="--fam:var(--${p.id})">${famName(p.id)} ${p.points}</span>`).join("")}</div>`;
  Z.bake(list.filter((g) => g.visual), baby).then((m) => { model = m; });
  const u = new URL(location.href);
  u.searchParams.set("genes", [...chosen].join(","));
  history.replaceState(null, "", u);
}

function syncAngle() {
  let deg = Math.round((turn * 180) / Math.PI) % 360;
  if (deg > 180) deg -= 360;
  if (deg < -180) deg += 360;
  $("lab-angle").value = String(deg);
}
const stopSpin = () => { spin = false; press($("lab-spin"), false); };

$("lab-spin").addEventListener("click", () => { spin = !spin; press($("lab-spin"), spin); });
$("lab-walk").addEventListener("click", () => { walk = !walk; press($("lab-walk"), walk); });
$("lab-baby").addEventListener("click", () => { baby = !baby; press($("lab-baby"), baby); update(); });
$("lab-night").addEventListener("click", () => { night = !night; press($("lab-night"), night); $("lab-stage").classList.toggle("night", night); });
$("lab-clear").addEventListener("click", () => { chosen.clear(); $("lab-msg").textContent = ""; update(); });
$("lab-angle").addEventListener("input", (e) => { turn = (Number(e.target.value) * Math.PI) / 180; stopSpin(); });

const stage = $("lab-stage");
let drag = null;
stage.addEventListener("pointerdown", (e) => { drag = { x: e.clientX, t: turn }; stage.setPointerCapture(e.pointerId); stopSpin(); });
stage.addEventListener("pointermove", (e) => { if (drag) { turn = drag.t + (e.clientX - drag.x) * 0.012; syncAngle(); } });
stage.addEventListener("pointerup", () => { drag = null; });

document.querySelectorAll("[data-gene]").forEach((b) => b.addEventListener("click", () => {
  const id = b.dataset.gene;
  if (chosen.has(id)) { chosen.delete(id); $("lab-msg").textContent = ""; }
  else if (chosen.size >= 5) { $("lab-msg").textContent = "A zombie carries a maximum of 5 genes. Remove a gene first."; return; }
  else { chosen.add(id); $("lab-msg").textContent = ""; }
  update();
}));

let last = performance.now();
function frame(now) {
  const dt = Math.min(0.05, (now - last) / 1000);
  last = now;
  if (!document.hidden) {
    if (spin) { turn += dt * 0.7; syncAngle(); }
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (model) {
      const list = [...chosen].map((id) => byId[id]);
      Z.draw(ctx, model, {
        scale: 9 * dpr, cx: canvas.width / 2, cy: canvas.height * 0.33, turn, tilt: -0.12,
        pose: Z.pose(now / 1000, walk), light: night ? 0.42 : 1, opacity: night ? Z.opacityAt(list, 0) : 1,
      });
    }
  }
  requestAnimationFrame(frame);
}
update();
requestAnimationFrame(frame);
