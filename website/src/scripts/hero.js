// The hero of the home page: the game pictures of the families, one after
// the other, and pixel particles that rise.

import { an } from "../lib/site.js";

const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
const box = document.getElementById("hero-shots");
const label = document.getElementById("hero-label");
const particles = document.getElementById("hero-particles");
const shots = JSON.parse(document.getElementById("hero-data")?.textContent || "[]");

if (box && shots.length > 1 && !reduced) {
  const [a, b] = box.querySelectorAll("img");
  let front = a, next = b, index = 0;
  const show = () => {
    index = (index + 1) % shots.length;
    const s = shots[index];
    next.onload = () => {
      next.alt = `${an(s.species)} ${s.species.toLowerCase()} of the ${s.name}`;
      front.alt = "";
      next.classList.add("show");
      front.classList.remove("show");
      [front, next] = [next, front];
      label.classList.add("fade");
      setTimeout(() => {
        label.innerHTML = `<div class="n" style="color:${s.color}">${s.name}</div><div class="k">${s.species}</div><div class="g">${s.genes.join(", ")}</div>`;
        label.classList.remove("fade");
      }, 240);
    };
    next.src = s.src;
  };
  setInterval(() => { if (!document.hidden) show(); }, 3800);
}

if (particles) {
  const dpr = Math.min(2, window.devicePixelRatio || 1);
  const ctx = particles.getContext("2d");
  let w = 0, h = 0;
  const resize = () => {
    w = particles.width = particles.clientWidth * dpr;
    h = particles.height = particles.clientHeight * dpr;
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
      ctx.clearRect(0, 0, w, h);
      for (const d of dots) {
        if (!reduced) { d.y -= d.v * dt; d.p += dt; }
        if (d.y < -0.05) { d.y = 1.05; d.x = Math.random(); }
        ctx.globalAlpha = 0.18 + 0.3 * Math.abs(Math.sin(d.p));
        ctx.fillStyle = d.c;
        ctx.fillRect(Math.round((d.x + Math.sin(d.p) * 0.01) * w), Math.round(d.y * h), d.s, d.s);
      }
      ctx.globalAlpha = 1;
    }
    requestAnimationFrame(frame);
  };
  requestAnimationFrame(frame);
}
