// The hero of the home page: a zombie that turns slowly, with genes that
// change every few seconds, and pixel particles that rise.

import { genes, families } from "../data/site.json";
import { familyOf, kindName, t } from "../lib/site.js";
import * as Z from "./zombie.js";

const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
const byId = Object.fromEntries(genes.map((g) => [g.id, g]));
const canvas = document.getElementById("hero-zombie");
const particles = document.getElementById("hero-particles");
const label = document.getElementById("hero-label");

if (canvas) {
  const dpr = Math.min(2, window.devicePixelRatio || 1);
  const W = 300, H = 380;
  canvas.width = W * dpr;
  canvas.height = H * dpr;
  const ctx = canvas.getContext("2d");

  // One family at a time, then some mixes, then a plain zombie. A set has
  // the genes of one species only.
  const sets = families.map((f) => genes.filter((g) => g.family === f.id && g.visual).slice(0, 3));
  sets.push(["cinder", "bonerunner", "frenzy"].map((id) => byId[id]));
  sets.push(["ironhide", "lurker", "nightborn"].map((id) => byId[id]));
  sets.push(["bloat", "sporebearer", "thickskull"].map((id) => byId[id]));
  // A family with no gene art yet has no set. The last set is a plain zombie.
  const clean = sets.map((s) => s.filter((g) => g && g.visual)).filter((s) => s.length);
  clean.push([]);

  let index = 0;
  let model = null;
  let since = 0;
  const load = () => {
    const list = clean[index % clean.length];
    Z.bake(list, false, list[0]?.species ?? "zombie").then((m) => {
      model = m;
      since = performance.now();
      const r = familyOf(list, list[0]?.species ?? "zombie");
      label.classList.add("fade");
      setTimeout(() => {
        label.innerHTML = `<div class="n" style="color:${r.color}">${r.name}</div>` +
          (r.kind === "neutral" ? "" : `<div class="k">${kindName(r.kind)}</div>`) +
          `<div class="g">${list.length ? list.map((g) => g.name).join(", ") : t("mob_info.originofspawns.no_genes", "No genes")}</div>`;
        label.classList.remove("fade");
      }, 240);
    });
  };
  load();
  setInterval(() => {
    if (!document.hidden) { index++; load(); }
  }, 4200);

  const pctx = particles.getContext("2d");
  let pw = 0, ph = 0;
  const resize = () => {
    pw = particles.width = particles.clientWidth * dpr;
    ph = particles.height = particles.clientHeight * dpr;
  };
  resize();
  addEventListener("resize", resize);
  const dots = Array.from({ length: 40 }, () => ({
    x: Math.random(), y: Math.random(), s: (2 + Math.random() * 3) * dpr, v: 0.015 + Math.random() * 0.04,
    c: Math.random() < 0.6 ? "#8bd346" : "#45d3c5", p: Math.random() * 6.28,
  }));

  let last = performance.now();
  const frame = (now) => {
    const dt = Math.min(0.05, (now - last) / 1000);
    last = now;
    if (!document.hidden) {
      const time = now / 1000;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      if (model) {
        Z.draw(ctx, model, {
          scale: 9 * dpr, cx: canvas.width / 2, cy: canvas.height * 0.3,
          turn: reduced ? -0.5 : Math.sin(time * 0.35) * 1.1 - 0.15, tilt: -0.14,
          pose: Z.pose(time, false), opacity: Math.min(1, (now - since) / 300),
        });
      }
      pctx.clearRect(0, 0, pw, ph);
      for (const d of dots) {
        if (!reduced) { d.y -= d.v * dt; d.p += dt; }
        if (d.y < -0.05) { d.y = 1.05; d.x = Math.random(); }
        pctx.globalAlpha = 0.18 + 0.3 * Math.abs(Math.sin(d.p));
        pctx.fillStyle = d.c;
        pctx.fillRect(Math.round((d.x + Math.sin(d.p) * 0.01) * pw), Math.round(d.y * ph), d.s, d.s);
      }
      pctx.globalAlpha = 1;
    }
    requestAnimationFrame(frame);
  };
  requestAnimationFrame(frame);
}
