// Draw each still zombie of a page. A canvas with data-zombie lists its genes
// in data-genes. Other data fields set the view.

import { genes } from "../data/site.json";
import * as Z from "./zombie.js";

const byId = Object.fromEntries(genes.map((g) => [g.id, g]));

export async function drawStill(canvas, list, o = {}) {
  const w = o.w ?? (Number(canvas.dataset.w) || 120);
  const h = o.h ?? (Number(canvas.dataset.h) || 160);
  const dpr = Math.min(2, window.devicePixelRatio || 1);
  canvas.width = Math.round(w * dpr);
  canvas.height = Math.round(h * dpr);
  const ctx = canvas.getContext("2d");
  ctx.imageSmoothingEnabled = false;
  const species = o.species ?? canvas.dataset.species ?? list[0]?.species ?? "zombie";
  const model = await Z.bake(list.filter((g) => g.visual), o.baby ?? canvas.dataset.baby === "true", species);
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  Z.draw(ctx, model, {
    scale: (o.scale ?? (Number(canvas.dataset.scale) || 4)) * dpr,
    cx: canvas.width / 2,
    cy: canvas.height * (o.cy ?? (Number(canvas.dataset.cy) || 0.36)),
    turn: o.turn ?? (canvas.dataset.turn ? Number(canvas.dataset.turn) : -0.5),
    tilt: -0.1,
    pose: Z.pose(0.4, false),
    light: o.light ?? 1,
    opacity: o.opacity ?? 1,
  });
}

function drawAll() {
  const list = document.querySelectorAll("canvas[data-zombie]");
  const io = new IntersectionObserver((entries) => {
    for (const e of entries) {
      if (!e.isIntersecting) continue;
      io.unobserve(e.target);
      const ids = (e.target.dataset.genes || "").split(",").filter(Boolean);
      drawStill(e.target, ids.map((id) => byId[id]).filter(Boolean));
    }
  }, { rootMargin: "200px" });
  list.forEach((c) => io.observe(c));
}
drawAll();
