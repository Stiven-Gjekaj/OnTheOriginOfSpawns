// The pages of the wiki. All the content comes from window.SITE_DATA, which
// website/build.mjs makes from the files of the mod.

(function () {
  "use strict";

  const D = window.SITE_DATA;
  const Z = window.Zombie;
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];
  const esc = (s) => String(s ?? "").replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" })[c]);
  const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;

  const FAMILY_COLORS = { predator: "var(--predator)", stalker: "var(--stalker)", tank: "var(--tank)", horde: "var(--horde)", blight: "var(--blight)" };
  const FAMILY_HEX = { predator: "#e2574c", stalker: "#9a7cf0", tank: "#b8bcc4", horde: "#f0a83a", blight: "#7ed23a" };
  const famById = Object.fromEntries(D.families.map((f) => [f.id, f]));
  const famName = (id) => famById[id]?.name ?? id;
  const singular = (id) => id.charAt(0).toUpperCase() + id.slice(1);
  const geneById = Object.fromEntries(D.genes.map((g) => [g.id, g]));
  const label = (id) => { const s = id.replace(/_/g, " "); return s.charAt(0).toUpperCase() + s.slice(1); };
  const lang = D.notebook.lang;
  const kindName = (kind) => kind === "neutral" ? "Neutral" : lang[`mob_info.originofspawns.kind.${kind}`] || kind;
  const fmt = (key, ...args) => { let i = 0; return (lang[key] || key).replace(/%s/g, () => args[i++]); };
  const pct = (x) => (x * 100).toFixed(x >= 0.1 || x === 0 ? 0 : 1) + "%";

  // ---------------------------------------------------------------------
  // Pixel icons, drawn as SVG from small grids.

  const ICONS = {
    hot: { y: "#f4c542", o: "#ff8a1f", g: ["y..y..y", ".y.y.y.", "..ooo..", "yyoooyy", "..ooo..", ".y.y.y.", "y..y..y"] },
    cold: { c: "#bfefff", g: ["c..c..c", ".c.c.c.", "..ccc..", "ccccccc", "..ccc..", ".c.c.c.", "c..c..c"] },
    wet: { b: "#3a8fe0", l: "#9fd4ff", g: ["...b...", "...b...", "..bbb..", ".bblbb.", ".blbbb.", ".bbbbb.", "..bbb.."] },
    temperate: { g: ["..GGG..", ".GGgGG.", "GGgGGgG", ".GGGgG.", "..GgG..", "...w...", "..www.."], G: "#7ed23a", gg: "#4f9a1c", w: "#8c6640" },
    nether: { r: "#c0392b", o: "#ff8a1f", y: "#f4c542", g: ["...r...", "..rr...", "..ror..", ".roor.r", ".royor.", "rooyoor", ".rrrrr."] },
    end: { p: "#2a6b5a", d: "#5fbf9a", k: "#111111", E: "#c8f7b0", g: [".ppppp.", "pEEEEEp", "pEdkdEp", "pEkkkEp", "pEdkdEp", "pEEEEEp", ".ppppp."] },
    predator: { r: "#e2574c", g: ["r...r..", ".r...r.", "..r...r", "r..r...", ".r..r..", "..r..r.", "...r..r"] },
    stalker: { s: "#9a7cf0", g: ["..sss..", ".ss....", "ss.....", "ss.....", "ss.....", ".ss....", "..sss.."] },
    tank: { t: "#b8bcc4", d: "#6b7076", g: ["ttttttt", "tdddddt", "tdtttdt", "tdtttdt", ".tdddt.", "..tdt..", "...t..."] },
    horde: { h: "#f0a83a", g: ["..hhh..", "..hhh..", ".......", "hhh.hhh", "hhh.hhh", "hhh.hhh", "......."] },
    blight: { B: "#7ed23a", k: "#1e420a", g: ["..BBB..", ".BBBBB.", "BBkBkBB", "BBBBBBB", ".BkBkB.", "..B.B..", ".B...B."] },
    neutral: { n: "#8a9a86", k: "#2a3a26", g: [".nnnnn.", "nnnnnnn", "nkknkkn", "nnnnnnn", "nnkkknn", "nnnnnnn", ".nnnnn."] },
    check: { g: ["......g", ".....gg", "g...gg.", "gg.gg..", ".ggg...", "..g....", "......."], gg: "#80ff20" },
    clock: { a: "#f4c542", k: "#3a2a1a", g: [".aaaaa.", "aaakaaa", "aaakaaa", "aaakkka", "aaaaaaa", "aaaaaaa", ".aaaaa."] },
    lock: { l: "#9aa0a6", d: "#4b5257", g: ["..lll..", ".l...l.", ".l...l.", "ddddddd", "dddlddd", "dddlddd", "ddddddd"] },
    sun: { y: "#f4c542", g: ["...y...", ".y.y.y.", "..yyy..", "yyyyyyy", "..yyy..", ".y.y.y.", "...y..."] },
    moon: { m: "#dfe6ff", g: ["..mmm..", ".mm....", "mm.....", "mm.....", "mm.....", ".mm....", "..mmm.."] },
    menu: { w: "#ffffff", g: [".......", "wwwwwww", ".......", "wwwwwww", ".......", "wwwwwww", "......."] },
  };
  function icon(name) {
    const ic = ICONS[name];
    if (!ic) return "";
    const rows = ic.g;
    let rects = "";
    rows.forEach((row, y) => [...row].forEach((ch, x) => {
      if (ch === ".") return;
      const color = ic[ch] || ic[ch + ch];
      if (color) rects += `<rect x="${x}" y="${y}" width="1.02" height="1.02" fill="${color}"/>`;
    }));
    return `<svg viewBox="0 0 ${rows[0].length} ${rows.length}" shape-rendering="crispEdges" aria-hidden="true">${rects}</svg>`;
  }
  const brandIcon = {
    modrinth: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="#fff" d="M2 0h4v1h1v1h1v4H7v1H6v1H2V7H1V6H0V2h1V1h1zM3 2v1H2v2h1v1h2V5h1V3H5V2z"/></svg>`,
    curseforge: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="#fff" d="M0 1h8v2H6v1H5v1h1v2H2V5h1V4H1V3H0z"/></svg>`,
    kofi: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="#fff" d="M0 2h6v1h1v1H6v2H5v1H1V6H0zM2 3v1h1v1h1V4h1V3H4v1H3V3z"/></svg>`,
    github: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="#fff" d="M2 0h4v1h1v1h1v4H7v1H5V6h1V3H5V2H3v1H2v3h1v1H1V6H0V2h1V1h1z"/></svg>`,
  };

  // ---------------------------------------------------------------------
  // Tooltip: an item tooltip with the purple border.

  const tip = document.createElement("div");
  tip.className = "tooltip";
  tip.setAttribute("role", "tooltip");
  document.body.appendChild(tip);
  let tipTarget = null;
  function placeTip(x, y) {
    const pad = 14;
    const r = tip.getBoundingClientRect();
    let left = x + pad, top = y + pad;
    if (left + r.width > innerWidth - 8) left = Math.max(8, x - r.width - pad);
    if (top + r.height > innerHeight - 8) top = Math.max(8, y - r.height - pad);
    tip.style.left = left + "px";
    tip.style.top = top + "px";
  }
  function showTip(el, x, y) {
    const html = el.dataset.tipHtml ? TIP_HTML[el.dataset.tipHtml] : `<div class="t-name">${esc(el.dataset.tip)}</div>`;
    if (!html) return;
    tip.innerHTML = typeof html === "function" ? html() : html;
    tipTarget = el;
    placeTip(x, y);
    requestAnimationFrame(() => tip.classList.add("show"));
  }
  function hideTip() { tip.classList.remove("show"); tipTarget = null; }
  const TIP_HTML = {};
  let tipSeq = 0;
  const tipAttr = (html) => { const k = "t" + tipSeq++; TIP_HTML[k] = html; return `data-tip-html="${k}"`; };
  document.addEventListener("pointerover", (e) => {
    if (e.pointerType === "touch") return;
    const el = e.target.closest("[data-tip],[data-tip-html]");
    if (el && el !== tipTarget) showTip(el, e.clientX, e.clientY);
    else if (!el && tipTarget) hideTip();
  });
  document.addEventListener("pointermove", (e) => { if (tipTarget) placeTip(e.clientX, e.clientY); });
  document.addEventListener("focusin", (e) => {
    const el = e.target.closest("[data-tip],[data-tip-html]");
    if (el) { const r = el.getBoundingClientRect(); showTip(el, r.left + 10, r.bottom); } else hideTip();
  });
  document.addEventListener("scroll", hideTip, { passive: true });

  // A readable line for an effect of a gene or a family.
  function effectLine(e) {
    const parts = Object.entries(e).filter(([k]) => k !== "type").map(([k, v]) => {
      const val = typeof v === "string" ? v.replace(/^minecraft:|^#minecraft:|^originofspawns:/, "") : Array.isArray(v) ? v.join(", ") : v;
      return `${k.replace(/_/g, " ")} ${val}`;
    });
    return `${e.type}${parts.length ? ": " + parts.join(", ") : ""}`;
  }

  function geneTip(g) {
    const fam = `<div class="t-blue">${esc(famName(g.family))}${g.sub ? ` <span class="t-line">(sub: ${esc(famName(g.sub))})</span>` : ""}</div>`;
    return `<div class="t-name" style="color:${FAMILY_HEX[g.family]}">${esc(g.name)}</div>${fam}
      <div class="t-line">Pressure: ${esc(label(g.counters))}</div>
      ${g.effects.gains.map((e) => `<div class="t-gain">+ ${esc(effectLine(e))}</div>`).join("")}
      ${g.effects.downsides.map((e) => `<div class="t-loss">- ${esc(effectLine(e))}</div>`).join("")}
      <div class="t-gray">${esc(g.look)}</div>
      <div class="t-id">originofspawns:${esc(g.id)}</div>`;
  }

  const statusBadge = (s) => s === "released"
    ? `<span class="badge released" data-tip="This is in the ${esc(D.version)} release">In ${esc(D.version)}</span>`
    : s === "development"
      ? `<span class="badge development" data-tip="Milestone 2a adds this. It is in development">Milestone 2a</span>`
      : `<span class="badge planned" data-tip="This is on the roadmap. It is not in the mod yet">Planned</span>`;

  // ---------------------------------------------------------------------
  // Families: the rules of DESIGN.md and docs/data-packs.md.

  function familyOf(genes) {
    const points = {};
    const best = {};
    for (const g of genes) {
      points[g.family] = (points[g.family] || 0) + 2;
      if (g.sub) points[g.sub] = (points[g.sub] || 0) + 1;
      const f = g.freq ?? 0;
      best[g.family] = Math.max(best[g.family] || 0, f);
      if (g.sub) best[g.sub] = Math.max(best[g.sub] || 0, f);
    }
    // When two families have the same points, the family of the gene with
    // the highest frequency comes first.
    const order = Object.keys(points).sort((a, b) => points[b] - points[a] || (best[b] || 0) - (best[a] || 0) || D.families.findIndex((f) => f.id === a) - D.families.findIndex((f) => f.id === b));
    const [a, b] = order;
    const pa = points[a] || 0, pb = points[b] || 0;
    let result;
    if (pa < 4) result = { kind: "neutral", name: "Neutral" };
    else if (pb === pa) {
      const h = D.hybrids.find((x) => (x.a === a && x.b === b) || (x.a === b && x.b === a));
      result = { kind: "hybrid", name: h.name, a, b, style: h.style };
    } else if (pa >= 6 && pb === 0) result = { kind: "pure", name: famName(a), major: a };
    else if (pb >= 2) {
      const m = D.minors.find((x) => x.major === a && x.sub === b);
      result = { kind: "minor", name: m.name, major: a, sub: b, style: m.style };
    } else result = { kind: "major", name: famName(a), major: a };
    result.points = order.map((id) => ({ id, points: points[id] }));
    result.color = result.kind === "neutral" ? "#9fb3ad" : FAMILY_HEX[result.major || result.a];
    return result;
  }
  const resultKey = (r) => r.kind === "neutral" ? "neutral" : r.kind === "hybrid" ? [r.a, r.b].sort().join("+") : r.kind === "minor" ? `${r.major}>${r.sub}` : r.major;
  const found = new Set();
  function recordFound(r) {
    // The notebook keeps the families that the visitor finds.
    const key = r.kind === "major" ? r.major : resultKey(r);
    const before = found.size;
    found.add(key);
    if (found.size !== before) renderNotebook();
  }

  // ---------------------------------------------------------------------
  // Shell: header, navigation, theme, and the page change.

  const PAGES = [
    ["home", "Home"],
    ["evolution", "Evolution"],
    ["families", "Families"],
    ["genes", "Genes"],
    ["lab", "Gene Lab"],
    ["notebook", "Notebook"],
    ["datapacks", "Data packs"],
    ["roadmap", "Roadmap"],
    ["download", "Download"],
  ];
  const nav = $("#nav");
  nav.innerHTML = PAGES.map(([id, name]) => `<a class="btn" href="#${id}" data-page="${id}">${esc(name)}</a>`).join("");
  $("#menu-btn").innerHTML = icon("menu") + "<span>Menu</span>";
  $("#menu-btn").addEventListener("click", () => {
    const open = nav.classList.toggle("open");
    $("#menu-btn").setAttribute("aria-expanded", open);
  });

  const root = document.documentElement;
  function setTheme(theme) {
    if (theme) root.dataset.theme = theme;
    const dark = (root.dataset.theme || (matchMedia("(prefers-color-scheme: light)").matches ? "light" : "dark")) === "dark";
    const btn = $("#theme-btn");
    btn.innerHTML = icon(dark ? "moon" : "sun");
    btn.setAttribute("aria-label", dark ? "Use the day colors" : "Use the night colors");
    btn.dataset.tip = dark ? "Night colors. Click for day colors" : "Day colors. Click for night colors";
  }
  try { const saved = localStorage.getItem("oots-theme"); if (saved) root.dataset.theme = saved; } catch (e) { /* no storage */ }
  setTheme();
  $("#theme-btn").addEventListener("click", () => {
    const dark = (root.dataset.theme || (matchMedia("(prefers-color-scheme: light)").matches ? "light" : "dark")) === "dark";
    const next = dark ? "light" : "dark";
    setTheme(next);
    try { localStorage.setItem("oots-theme", next); } catch (e) { /* no storage */ }
  });

  let current = null;
  const pageIndex = (id) => PAGES.findIndex((p) => p[0] === id);
  function route() {
    const hash = location.hash.slice(1);
    const [id, anchor] = hash.split("/");
    const page = PAGES.some((p) => p[0] === id) ? id : "home";
    const el = $(`#page-${page}`);
    if (current !== page) {
      const back = current && pageIndex(page) < pageIndex(current);
      const old = current && $(`#page-${current}`);
      if (old) {
        old.classList.remove("active", "back");
        if (!reduced) {
          old.classList.add("leaving");
          old.classList.toggle("back", !!back);
          setTimeout(() => old.classList.remove("leaving", "back"), 260);
        }
      }
      el.classList.toggle("back", !!back);
      el.classList.add("active");
      current = page;
      window.scrollTo({ top: 0, behavior: "instant" });
      $$("#nav a").forEach((a) => { if (a.dataset.page === page) a.setAttribute("aria-current", "page"); else a.removeAttribute("aria-current"); });
      document.title = (page === "home" ? "" : PAGES[pageIndex(page)][1] + " | ") + "On the Origin of Spawns Wiki";
      if (page === "lab") drawLab();
      if (page === "notebook") renderNotebook();
    }
    if (anchor) {
      const target = document.getElementById(anchor);
      if (target) setTimeout(() => target.scrollIntoView({ behavior: reduced ? "auto" : "smooth" }), 60);
    }
    nav.classList.remove("open");
    hideTip();
  }
  // In-page links of the data pack guide keep the page in the hash.
  document.addEventListener("click", (e) => {
    const a = e.target.closest("a[href^='#dp-']");
    if (a) { e.preventDefault(); location.hash = "datapacks/" + a.getAttribute("href").slice(1); }
  });
  // Buttons get the press effect on touch too.
  document.addEventListener("pointerdown", (e) => {
    const b = e.target.closest(".btn");
    if (b) { b.classList.add("pressed"); const up = () => { b.classList.remove("pressed"); removeEventListener("pointerup", up); }; addEventListener("pointerup", up); }
  });

  // ---------------------------------------------------------------------
  // Canvas helpers

  function setupCanvas(canvas, w, h) {
    const dpr = Math.min(2, window.devicePixelRatio || 1);
    canvas.width = Math.round(w * dpr);
    canvas.height = Math.round(h * dpr);
    const ctx = canvas.getContext("2d");
    ctx.imageSmoothingEnabled = false;
    return { ctx, dpr, w, h };
  }
  async function drawStill(canvas, genes, opts = {}) {
    const w = opts.w || 120, h = opts.h || 160;
    const { ctx, dpr } = setupCanvas(canvas, w, h);
    const model = await Z.bake(genes);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    Z.draw(ctx, model, Object.assign({ scale: (opts.scale || 4) * dpr, cx: canvas.width / 2, cy: canvas.height * (opts.cy || 0.36), turn: opts.turn ?? -0.5, tilt: -0.1, pose: Z.pose(0.4, false) }, opts.draw || {}));
  }

  // ---------------------------------------------------------------------
  // Home

  const visualGenes = D.genes.filter((g) => g.visual);
  const counts = {
    genes: D.genes.length,
    families: D.families.length,
    minors: D.minors.length,
    hybrids: D.hybrids.length,
    pressures: D.pressures.length,
    categories: D.evolution.categories.length,
  };
  const v1 = D.roadmap.filter((r) => !r.after1 && r.items.length);
  const v1Done = v1.filter((r) => r.done).length;

  function renderHome() {
    const el = $("#page-home");
    el.innerHTML = `
      <section class="hero">
        <canvas class="hero-particles" id="particles" aria-hidden="true"></canvas>
        <div class="hero-inner">
          <div>
            <div class="eyebrow">A Fabric mod for Minecraft ${esc(D.minecraft)}</div>
            <h1>On the Origin <span class="green">of Spawns</span></h1>
            <div class="tagline">${esc(D.tagline)}.</div>
            <p>${esc(D.overview[0] || "")}</p>
            <div class="hero-actions">
              <a class="btn modrinth" href="${esc(D.links.modrinth)}" target="_blank" rel="noopener">${brandIcon.modrinth}Modrinth</a>
              <a class="btn curseforge" href="${esc(D.links.curseforge)}" target="_blank" rel="noopener">${brandIcon.curseforge}CurseForge</a>
              <a class="btn" href="#evolution">How it works</a>
            </div>
            <div class="hero-meta">
              <span class="badge released">${esc(D.version)}</span>
              <span class="badge">Minecraft ${esc(D.minecraft)}</span>
              <span class="badge">Fabric Loader ${esc(D.fabric)}</span>
            </div>
          </div>
          <div class="hero-stage">
            <div class="hero-pedestal"></div>
            <canvas id="hero-zombie" aria-label="A zombie that turns slowly. Its genes change every few seconds." role="img"></canvas>
            <div class="mob-info hero-label" id="hero-label" aria-live="polite"></div>
          </div>
        </div>
      </section>

      <div class="stats">
        ${[["genes", "Zombie genes", "blight"], ["families", "Major families", "predator"], ["minors", "Minor families", "stalker"], ["hybrids", "Hybrids", "horde"], ["pressures", "Pressures", "tank"], ["categories", "Biome categories", "temperate"]]
          .map(([k, t, ic]) => `<div class="panel stat"><span class="slot">${icon(ic)}</span><div><b>${counts[k]}</b><span>${t}</span></div></div>`).join("")}
      </div>

      <section class="section">
        <div class="eyebrow">The loop</div>
        <h2>Each kill changes the next generation</h2>
        <p class="lead">${esc(D.overview[1] || "")}</p>
        <div class="loop" style="margin-top:26px">
          <div class="panel stone"><h3>You kill a zombie</h3><p>The mod records the cause of the death. Fire, arrows, melee, and falls are some of the ${counts.pressures} pressures.</p></div>
          <div class="panel stone"><h3>The pool selects</h3><p>The gene that counters the cause gets stronger in the pool of that biome category.</p></div>
          <div class="panel stone"><h3>New zombies spawn</h3><p>Each new zombie gets genes from its pool. A zombie carries a maximum of 5 genes.</p></div>
          <div class="panel stone"><h3>You read the signs</h3><p>Each gene has a mark on the body. Your Beagle Notebook records the species.</p></div>
        </div>
      </section>

      <section class="section grid two">
        <div class="panel">
          <h2>Features</h2>
          <p class="muted">These features are for version 1.0.0. The roadmap shows the state of each one.</p>
          <ul class="feature-list">${D.features.map((f) => `<li>${esc(f)}</li>`).join("")}</ul>
        </div>
        <div class="grid" style="align-content:start">
          <div class="panel wood">
            <h3>Progress to version 1.0.0</h3>
            <div class="xp-row"><div class="xp" id="home-xp"><span></span></div><div class="xp-level">${v1Done}/${v1.length}</div></div>
            <p style="margin-top:10px">${v1Done} of ${v1.length} milestones are done. Milestone 2a, the complete zombie, comes next.</p>
            <a class="btn small" href="#roadmap">See the roadmap</a>
          </div>
          <div class="panel">
            <h3>Players adapt too</h3>
            <p>You choose small adaptations, each with a trade-off. Then the mobs adapt to counter you. ${statusBadge("planned")}</p>
            <a class="btn small" href="#notebook/perks">See the perks</a>
          </div>
          <div class="panel">
            <div class="head" style="display:flex;gap:12px;align-items:center">
              <span class="slot"><img src="img/beagle_notebook.png" alt="" width="36" height="36"></span>
              <h3 style="margin:0">The Beagle Notebook</h3>
            </div>
            <p style="margin-top:10px">The notebook records each species, its genes, and the families that you find.</p>
            <a class="btn small" href="#notebook">Open the notebook</a>
          </div>
        </div>
      </section>`;
    requestAnimationFrame(() => { $("#home-xp span").style.width = (v1Done / v1.length) * 100 + "%"; });
    startHero();
  }

  function startHero() {
    const canvas = $("#hero-zombie");
    const particles = $("#particles");
    const label = $("#hero-label");
    const W = 300, H = 400;
    const { ctx, dpr } = setupCanvas(canvas, W, H);
    // Gene sets for the hero: one family at a time, then a mix.
    const sets = [];
    for (const f of D.families) {
      const fam = visualGenes.filter((g) => g.family === f.id).slice(0, 3);
      if (fam.length >= 2) sets.push(fam);
    }
    sets.push(["cinder", "bonerunner", "frenzy"].map((id) => geneById[id]).filter((g) => g && g.visual));
    sets.push(["ironhide", "lurker", "nightborn"].map((id) => geneById[id]).filter((g) => g && g.visual));
    sets.push([]);
    let setIndex = 0;
    let model = null;
    let modelTime = 0;
    function loadSet() {
      const genes = sets[setIndex % sets.length];
      Z.bake(genes).then((m) => {
        model = m;
        modelTime = performance.now();
        const r = familyOf(genes);
        label.classList.add("fade");
        setTimeout(() => {
          label.innerHTML = `<div class="n" style="color:${r.color}">${esc(r.name)}</div>${r.kind === "neutral" ? "" : `<div class="k">${esc(kindName(r.kind))}</div>`}<div class="g">${genes.length ? genes.map((g) => esc(g.name)).join(", ") : esc(lang["mob_info.originofspawns.no_genes"] || "No genes")}</div>`;
          label.classList.remove("fade");
        }, 250);
      });
    }
    loadSet();
    const timer = setInterval(() => { if (current === "home" && !document.hidden) { setIndex++; loadSet(); } }, 4200);

    const { ctx: pctx } = setupCanvas(particles, particles.clientWidth || 800, particles.clientHeight || 600);
    let pw = particles.width, ph = particles.height;
    const dots = Array.from({ length: 46 }, () => ({ x: Math.random(), y: Math.random(), s: 2 + Math.random() * 4, v: 0.02 + Math.random() * 0.05, c: Math.random() < 0.55 ? "#7ed23a" : "#3ad6c8", p: Math.random() * 6.28 }));
    const resize = () => { const r = setupCanvas(particles, particles.clientWidth, particles.clientHeight); pw = particles.width; ph = particles.height; };
    addEventListener("resize", resize);

    let last = performance.now();
    function frame(now) {
      const dt = Math.min(0.05, (now - last) / 1000);
      last = now;
      if (current === "home" && !document.hidden) {
        const t = now / 1000;
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        if (model) {
          const fade = Math.min(1, (now - modelTime) / 300);
          Z.draw(ctx, model, { scale: 9 * dpr, cx: canvas.width / 2, cy: canvas.height * 0.33, turn: reduced ? -0.5 : Math.sin(t * 0.35) * 1.1 - 0.15, tilt: -0.14, pose: Z.pose(t, false), opacity: fade });
        }
        pctx.clearRect(0, 0, pw, ph);
        for (const d of dots) {
          if (!reduced) { d.y -= d.v * dt; d.p += dt; }
          if (d.y < -0.05) { d.y = 1.05; d.x = Math.random(); }
          const x = (d.x + Math.sin(d.p) * 0.01) * pw, y = d.y * ph;
          pctx.globalAlpha = 0.25 + 0.35 * Math.abs(Math.sin(d.p));
          pctx.fillStyle = d.c;
          pctx.fillRect(Math.round(x), Math.round(y), d.s * 1.5, d.s * 1.5);
        }
        pctx.globalAlpha = 1;
      }
      requestAnimationFrame(frame);
    }
    requestAnimationFrame(frame);
    window.addEventListener("beforeunload", () => clearInterval(timer));
  }

  // ---------------------------------------------------------------------
  // Evolution

  const pools = {};
  const deaths = {};
  let simCategory = "temperate";
  let simZombie = [];
  function pool(cat) {
    if (!pools[cat]) pools[cat] = Object.fromEntries(D.genes.map((g) => [g.id, 0]));
    return pools[cat];
  }

  function renderEvolution() {
    const ev = D.evolution;
    const m2d = D.roadmap.find((r) => r.key === "Milestone 2d");
    const el = $("#page-evolution");
    const pick = (list, re) => list.filter((x) => re.test(x));
    el.innerHTML = `
      <header class="page-head">
        <div class="eyebrow">How evolution works</div>
        <h1>Natural selection, one kill at a time</h1>
        <p>Each species has a gene pool for each biome category. The causes of death change the pool. New mobs take their genes from the pool.</p>
      </header>

      <section>
        <h2>Gene pools</h2>
        <p class="lead">A species is one mob type. Zombie, husk, and drowned are different species. Each species keeps one pool for each of these ${ev.categories.length} biome categories:</p>
        <div class="categories">${ev.categories.map((c) => `<div class="panel category"><span class="slot">${icon(c.id)}</span><b>${esc(c.name)}</b></div>`).join("")}</div>
        <div class="planned-box"><h4>${statusBadge("planned")} Gene flow</h4><p style="margin:0">${esc(ev.pools.find((x) => /flows/.test(x)) || "")} Milestone 2d adds this.</p></div>
      </section>

      <section class="section">
        <h2>Pressures</h2>
        <p class="lead">A pressure is a cause of death, or a condition of the death. Each gene counters one pressure. When a pressure is common, the gene that counters it gets stronger.</p>
        <div class="table-wrap"><table>
          <thead><tr><th>Pressure</th><th>A zombie death counts when</th><th>Genes that it selects</th><th>State</th></tr></thead>
          <tbody>${D.pressures.map((p) => {
            const gs = D.genes.filter((g) => g.counters === p.id);
            return `<tr><td><span class="badge pressure">${esc(label(p.id))}</span></td><td>${esc(p.when)}</td><td>${gs.map((g) => `<a href="#genes/gene-${g.id}" ${tipAttr(() => geneTip(g))}>${esc(g.name)}</a>`).join(", ") || "None"}</td><td>${statusBadge(p.status)}</td></tr>`;
          }).join("")}</tbody>
        </table></div>
      </section>

      <section class="section">
        <h2>Selection, mutation, and decay</h2>
        <div class="steps">
          <div class="panel"><h3>Selection</h3><p>Each death raises the gene that counters its pressure by 2% of the distance to 100%.</p><div class="formula">f = f + 0.02 x (1 - f)</div><p class="muted">About 50 fire deaths take Cinder from 0% to about 60%.</p></div>
          <div class="panel"><h3>Mutation</h3><p>Each gene has a small chance to go to a new mob, also at 0%. The default chance is ${pct(0.003)}.</p><div class="formula">chance = max(f, mutation rate)</div></div>
          <div class="panel"><h3>Decay</h3><p>Each Minecraft day, decay lowers each frequency by 1% of its value. When players stop a cause of death, its gene fades slowly.</p><div class="formula">f = f x (1 - 0.01)</div></div>
          <div class="panel"><h3>The 5-gene limit</h3><p>Each mob carries a maximum of 5 genes. The genes with the highest frequency stay. Thus, a species cannot have all defenses at the same time.</p></div>
        </div>
        <p class="muted" style="margin-top:12px">The mod weights each death by its cause, not by its player. Thus, one player cannot control a pool alone. Data packs can change the rates of each gene.</p>
      </section>

      <section class="section">
        <div class="eyebrow">Try it</div>
        <h2>Pool simulator</h2>
        <p class="lead">Kill zombies in a biome category, and watch the pool change. Then spawn a zombie from the pool. The simulator uses the rates of the gene files.</p>
        <div class="sim">
          <div class="panel">
            <div class="sim-controls" id="sim-cats">${ev.categories.map((c) => `<button class="btn small" data-cat="${c.id}" aria-pressed="${c.id === simCategory}">${esc(c.name)}</button>`).join("")}</div>
            <h4 style="margin:6px 0">Kill 5 zombies by:</h4>
            <div class="sim-controls" id="sim-pressures">${D.pressures.map((p) => `<button class="btn small" data-pressure="${p.id}" ${tipAttr(() => `<div class="t-name">${esc(label(p.id))}</div><div class="t-line">${esc(p.when)}</div>${D.genes.filter((g) => g.counters === p.id).map((g) => `<div class="t-gain">+ ${esc(g.name)}</div>`).join("")}`)}>${esc(label(p.id))}</button>`).join("")}</div>
            <div class="sim-controls">
              <button class="btn small dark" id="sim-day">${icon("clock")} A day passes</button>
              <button class="btn small dark" id="sim-reset">Reset the pool</button>
            </div>
            <div class="pool" id="sim-pool"></div>
          </div>
          <div class="panel stone sim-zombie">
            <h3 style="margin:0">Spawn from the pool</h3>
            <canvas id="sim-canvas" role="img" aria-label="The zombie that spawned from the pool"></canvas>
            <div id="sim-result" class="lab-result"></div>
            <div class="sim-genes" id="sim-genes"></div>
            <button class="btn green" id="sim-spawn">Spawn a zombie</button>
            <div class="sim-log muted" id="sim-log">Deaths in this pool: 0</div>
          </div>
        </div>
      </section>

      <section class="section grid two">
        <div class="panel">
          <h3>Genes on a mob</h3>
          <ul>${[
            "A mob gets its genes one time, when it first joins a world.",
            "Each gene has a chance equal to its frequency in the pool of the biome category, or its mutation rate if that is higher.",
            "A mob carries a maximum of 5 genes. The genes with the highest frequency stay.",
            ...pick(ev.babies, /Runt|start/),
          ].map((x) => `<li>${esc(x)}</li>`).join("")}</ul>
        </div>
        <div class="panel">
          <h3>Gene marks</h3>
          <ul>${pick(ev.visuals, /^Each gene adds|^The marks of different|^The mod does not have a texture|^Data packs define/).map((x) => `<li>${esc(x)}</li>`).join("")}</ul>
          <a class="btn small" href="#lab">Open the Gene Lab</a>
        </div>
      </section>

      <section class="section">
        <h2>Coming later</h2>
        <div class="grid three">
          <div class="planned-box"><h4>${statusBadge("planned")} Extinction</h4><p style="margin:0">${esc(D.evolution.extinction[0] || "")} ${esc(D.evolution.extinction[1] || "")}</p></div>
          <div class="planned-box"><h4>${statusBadge("planned")} Speed of evolution</h4><p style="margin:0">${esc(pick(ev.balance, /difficulty|Peaceful|Hard|gamerule/).join(" "))}</p></div>
          <div class="planned-box"><h4>${statusBadge("planned")} Shared pools</h4><p style="margin:0">${esc(ev.multiplayer.join(" "))}</p></div>
        </div>
        ${m2d ? `<p class="muted">Milestone 2d and milestone 5 on the <a href="#roadmap">roadmap</a> add these.</p>` : ""}
      </section>`;

    $("#sim-cats").addEventListener("click", (e) => {
      const b = e.target.closest("[data-cat]");
      if (!b) return;
      simCategory = b.dataset.cat;
      $$("#sim-cats .btn").forEach((x) => x.setAttribute("aria-pressed", x === b));
      renderPool();
    });
    $("#sim-pressures").addEventListener("click", (e) => {
      const b = e.target.closest("[data-pressure]");
      if (!b) return;
      const p = pool(simCategory);
      const hit = [];
      for (let i = 0; i < 5; i++) {
        for (const g of D.genes) if (g.counters === b.dataset.pressure) { p[g.id] += g.rates.selection * (1 - p[g.id]); hit.push(g.id); }
      }
      deaths[simCategory] = (deaths[simCategory] || 0) + 5;
      renderPool(new Set(hit));
    });
    $("#sim-day").addEventListener("click", () => {
      const p = pool(simCategory);
      for (const g of D.genes) p[g.id] *= 1 - g.rates.decay;
      renderPool();
    });
    $("#sim-reset").addEventListener("click", () => { pools[simCategory] = null; deaths[simCategory] = 0; renderPool(); });
    $("#sim-spawn").addEventListener("click", spawnSim);
    renderPool();
    drawSim();
  }

  function renderPool(flash) {
    const p = pool(simCategory);
    const top = new Set(Object.entries(p).filter(([, f]) => f > 0).sort((a, b) => b[1] - a[1]).slice(0, 5).map(([id]) => id));
    const rows = [...D.genes].sort((a, b) => p[b.id] - p[a.id] || 0);
    $("#sim-pool").innerHTML = rows.map((g) => `
      <div class="pool-row${flash && flash.has(g.id) ? " flash" : ""}${top.has(g.id) ? " top" : ""}">
        <span class="name" style="color:${FAMILY_HEX[g.family]}" ${tipAttr(() => geneTip(g))}>${esc(g.name)}</span>
        <div class="xp"><span style="width:${Math.max(p[g.id] * 100, 0)}%"></span></div>
        <span class="val">${pct(p[g.id])}</span>
      </div>`).join("");
    $("#sim-log").textContent = `Deaths in this pool: ${deaths[simCategory] || 0}. A star marks the genes with the highest frequency.`;
    renderNotebookPool();
  }

  function spawnSim() {
    const p = pool(simCategory);
    let got = D.genes.filter((g) => Math.random() < Math.max(p[g.id], g.rates.mutation)).map((g) => Object.assign({}, g, { freq: p[g.id] }));
    const dropped = Math.max(0, got.length - 5);
    got.sort((a, b) => b.freq - a.freq);
    got = got.slice(0, 5);
    simZombie = got;
    const r = familyOf(got);
    recordFound(r);
    $("#sim-result").innerHTML = `<div class="fam-name" style="color:${r.color}">${esc(r.name)}</div>${r.kind === "neutral" ? "" : `<div class="muted">${esc(kindName(r.kind))}</div>`}`;
    $("#sim-genes").innerHTML = got.length ? got.map((g) => `<span class="badge fam" style="--fam:${FAMILY_HEX[g.family]}" ${tipAttr(() => geneTip(g))}>${esc(g.name)}</span>`).join("") : `<span class="muted">${esc(lang["mob_info.originofspawns.no_genes"] || "No genes")}</span>`;
    $("#sim-log").textContent = dropped ? `The zombie rolled ${got.length + dropped} genes. It keeps the 5 with the highest frequency.` : `Deaths in this pool: ${deaths[simCategory] || 0}.`;
    drawSim();
  }
  function drawSim() {
    drawStill($("#sim-canvas"), simZombie.filter((g) => g.visual), { w: 180, h: 240, scale: 5.4, cy: 0.33, turn: -0.55 });
  }

  // ---------------------------------------------------------------------
  // Families

  function renderFamilies() {
    const el = $("#page-families");
    const rules = [
      ["Neutral", "neutral", "No family has 4 points.", "No family effects. At the start of a world, all mobs are neutral."],
      ["Major", "major", "One family has 4 points or more, and no other family has 2.", "Tier 2 effects of the major."],
      ["Pure", "pure", "One family has 6 points or more, and no other family has points.", "Tier 3 effects of the family."],
      ["Minor", "minor", "The major has 4 points or more, and a sub family has 2 or more.", "Tier 2 effects of the major, and tier 1 effects of the sub."],
      ["Hybrid", "hybrid", "Two families have the same points, 4 or more.", "Tier 2 effects of each of the two."],
    ];
    el.innerHTML = `
      <header class="page-head">
        <div class="eyebrow">Families</div>
        <h1>The genes of a mob select its family</h1>
        <p>Each zombie gene has a main family tag. Some genes also have a sub family tag. The points of the genes select the family of each zombie. ${statusBadge("development")}</p>
      </header>

      <section>
        <h2>The 5 major families</h2>
        <p class="lead">Each major family has one big buff and one big debuff, at 3 tiers. A higher tier makes the buff stronger and the debuff weaker.</p>
        <div class="grid three">${D.families.map((f) => `
          <article class="panel stone family-card" style="--fam:${FAMILY_COLORS[f.id]}" id="fam-${f.id}">
            <div class="head"><span class="slot">${icon(f.id)}</span><div><h3>${esc(f.name)}</h3><div class="muted" style="font-size:.92rem">${esc(f.summary)}</div></div></div>
            <div class="tier-tabs" role="group" aria-label="Tier">${[1, 2, 3].map((n) => `<button class="btn small" data-fam="${f.id}" data-tier="${n}" aria-pressed="${n === 3}">Tier ${n}</button>`).join("")}</div>
            <div class="tier-body" id="tier-${f.id}"></div>
            <a class="btn small" href="#genes/f-${f.id}" style="margin-top:10px">${esc(singular(f.id))} genes</a>
          </article>`).join("")}
        </div>
      </section>

      <section class="section">
        <h2>Family points and results</h2>
        <p class="lead">A main family tag gives 2 points. A sub family tag gives 1 point. The family with the most points is the major family. The family with the second most points is the sub family.</p>
        <div class="rules">${rules.map(([name, kind, rule, fx]) => `<div class="panel rule"><span class="name">${esc(name)}</span><div><div>${esc(rule)}</div><div class="muted">${esc(fx)}</div></div></div>`).join("")}</div>
        <div class="panel" style="margin-top:16px">
          <h4>The order of power</h4>
          <div class="power">${D.powerOrder.map((p, i) => `${i ? '<span class="arrow">&gt;</span>' : ""}<span class="badge">${esc(p)}</span>`).join("")}</div>
          <p style="margin:12px 0 0">A mob with 0 or 1 gene is neutral. A mob with genes of 3 different families, one gene each, is also neutral. If two families have the same points for the sub family, the family of the gene with the highest frequency wins.</p>
        </div>
      </section>

      <section class="section">
        <h2>The 20 minor families</h2>
        <p class="lead">A minor family has a major family and a sub family. Each one has its own name. The rows show the major family, and the columns show the sub family. The diagonal shows the pure families.</p>
        <div class="matrix" role="table" aria-label="Minor families">
          <div class="mh" role="columnheader">Major / Sub</div>
          ${D.families.map((f) => `<div class="mh" style="--fam:${FAMILY_COLORS[f.id]}" role="columnheader">${esc(f.name)}</div>`).join("")}
          ${D.families.map((row) => `
            <div class="mh" style="--fam:${FAMILY_COLORS[row.id]}" role="rowheader">${esc(row.name)}</div>
            ${D.families.map((col) => {
              if (row.id === col.id) return `<div class="cell pure" style="--fam:${FAMILY_COLORS[row.id]}" tabindex="0" role="cell" ${tipAttr(() => `<div class="t-name" style="color:${FAMILY_HEX[row.id]}">${esc(row.name)}</div><div class="t-blue">${esc(kindName("pure"))}</div><div class="t-line">Tier 3 effects of the ${esc(row.name)}</div>`)}>${esc(row.name)}</div>`;
              const m = D.minors.find((x) => x.major === row.id && x.sub === col.id);
              return `<div class="cell" style="--fam:${FAMILY_COLORS[row.id]}" tabindex="0" role="cell" ${tipAttr(() => `<div class="t-name" style="color:${FAMILY_HEX[row.id]}">${esc(m.name)}</div><div class="t-blue">${esc(kindName("minor"))}</div><div class="t-line">Major: ${esc(row.name)}, tier 2</div><div class="t-line">Sub: ${esc(col.name)}, tier 1</div><div class="t-gray">${esc(m.style)}</div>`)}>${esc(m.name)}</div>`;
            }).join("")}`).join("")}
        </div>
        <div class="table-wrap" style="margin-top:18px"><table>
          <thead><tr><th>Minor family</th><th>Major</th><th>Sub</th><th>Play style</th></tr></thead>
          <tbody>${D.minors.map((m) => `<tr><td><b class="pixel">${esc(m.name)}</b></td><td style="color:${FAMILY_HEX[m.major]}">${esc(famName(m.major))}</td><td>${esc(famName(m.sub))}</td><td>${esc(m.style)}</td></tr>`).join("")}</tbody>
        </table></div>
      </section>

      <section class="section">
        <h2>The 10 hybrids</h2>
        <p class="lead">A hybrid has the same points in two families. It gets the tier 2 effects of each of the two families. Each hybrid has its own name.</p>
        <div class="hybrids">${D.hybrids.map((h) => `
          <div class="panel hybrid" tabindex="0" ${tipAttr(() => `<div class="t-name">${esc(h.name)}</div><div class="t-blue">${esc(kindName("hybrid"))}</div><div class="t-line">${esc(famName(h.a))} and ${esc(famName(h.b))}, tier 2 each</div><div class="t-gray">${esc(h.style)}</div>`)}>
            <span class="pair"><span class="slot">${icon(h.a)}</span><span class="slot">${icon(h.b)}</span></span>
            <div><b>${esc(h.name)}</b><span class="muted">${esc(h.style)}</span></div>
          </div>`).join("")}
        </div>
      </section>`;

    const showTier = (id, n) => {
      const f = famById[id];
      const t = f.tiers[n - 1];
      const body = $(`#tier-${id}`);
      body.classList.remove("swap");
      void body.offsetWidth;
      body.classList.add("swap");
      body.innerHTML = `<div class="line buff"><b>Buff</b><span>${esc(t.buff)}</span></div><div class="line debuff"><b>Debuff</b><span>${esc(t.debuff)}</span></div>
        <div class="muted" style="font-size:.85rem">${n === 3 ? "A pure family gets tier 3." : n === 2 ? "A major family alone, a hybrid, or the major of a minor family gets tier 2." : "The sub of a minor family gets tier 1."}</div>`;
    };
    D.families.forEach((f) => showTier(f.id, 3));
    el.addEventListener("click", (e) => {
      const b = e.target.closest("[data-tier]");
      if (!b) return;
      $$(`[data-fam="${b.dataset.fam}"]`, el).forEach((x) => x.setAttribute("aria-pressed", x === b));
      showTier(b.dataset.fam, +b.dataset.tier);
    });
  }

  // ---------------------------------------------------------------------
  // Genes

  let geneFilter = "all";
  function renderGenes() {
    const el = $("#page-genes");
    el.innerHTML = `
      <header class="page-head">
        <div class="eyebrow">Genes</div>
        <h1>The ${D.genes.length} zombie genes</h1>
        <p>Each gene has gains, downsides, and a mark on the body. Its main tag gives 2 points to a family. Point at a card to see the effects of its data file.</p>
      </header>
      <div class="filters" id="gene-filters">
        <button class="btn small" data-filter="all" aria-pressed="true">All</button>
        ${D.families.map((f) => `<button class="btn small" data-filter="${f.id}" aria-pressed="false" style="--fam:${FAMILY_COLORS[f.id]}"><span class="dot"></span>${esc(f.name)}</button>`).join("")}
        <button class="btn small" data-filter="released" aria-pressed="false">In ${esc(D.version)}</button>
        <input class="search" id="gene-search" type="search" placeholder="Search genes, effects, or pressures" aria-label="Search the genes">
        <span class="result-count" id="gene-count" aria-live="polite"></span>
      </div>
      <div class="legend"><span class="badge fam" style="--fam:#bbb">Main tag</span> 2 points <span class="badge sub" style="--fam:#ddd">Sub tag</span> 1 point <span class="badge pressure">Pressure</span> makes the gene stronger</div>
      <div class="genes" id="gene-list">${D.genes.map((g) => `
        <article class="panel gene" id="gene-${g.id}" style="--fam:${FAMILY_COLORS[g.family]}" data-family="${g.family}" data-sub="${g.sub || ""}" data-status="${g.status}"
          data-text="${esc([g.name, g.gains, g.downsides, g.look, g.counters, g.details, famName(g.family), g.sub ? famName(g.sub) : ""].join(" ").toLowerCase())}">
          <div class="top">
            <span class="slot big" ${tipAttr(() => geneTip(g))}>${g.visual ? `<canvas data-thumb="${g.id}" aria-label="The look of ${esc(g.name)}"></canvas>` : icon(g.family)}</span>
            <div>
              <h3>${esc(g.name)}</h3>
              <div class="tags">
                <span class="badge fam" style="--fam:${FAMILY_COLORS[g.family]}">${esc(famName(g.family))}</span>
                ${g.sub ? `<span class="badge sub" style="--fam:${FAMILY_COLORS[g.sub]}">sub: ${esc(famName(g.sub))}</span>` : ""}
                <span class="badge pressure" data-tip="${esc((D.pressures.find((p) => p.id === g.counters) || {}).when || "")}">${esc(label(g.counters))}</span>
                ${statusBadge(g.status)}
              </div>
            </div>
          </div>
          <dl>
            <div class="gain"><dt>Gains</dt><dd>${esc(g.gains)}</dd></div>
            <div class="loss"><dt>Downsides</dt><dd>${esc(g.downsides)}</dd></div>
            <div class="look"><dt>Look</dt><dd>${esc(g.look)}</dd></div>
          </dl>
          ${g.details ? `<details><summary>Details</summary><p>${esc(g.details)}</p></details>` : ""}
          <div class="actions">${g.visual ? `<a class="btn small" href="#lab" data-lab="${g.id}">Try it in the Gene Lab</a>` : `<span class="muted" style="font-size:.85rem">The look of this gene is not in the mod yet.</span>`}</div>
        </article>`).join("")}
      </div>`;

    const apply = () => {
      const q = $("#gene-search").value.trim().toLowerCase();
      let n = 0;
      $$(".gene", el).forEach((card) => {
        const okF = geneFilter === "all" || (geneFilter === "released" ? card.dataset.status === "released" : card.dataset.family === geneFilter || card.dataset.sub === geneFilter);
        const okQ = !q || card.dataset.text.includes(q);
        card.classList.toggle("hide", !(okF && okQ));
        if (okF && okQ) n++;
      });
      $("#gene-count").textContent = `${n} of ${D.genes.length}`;
    };
    $("#gene-filters").addEventListener("click", (e) => {
      const b = e.target.closest("[data-filter]");
      if (!b) return;
      geneFilter = b.dataset.filter;
      $$("#gene-filters [data-filter]").forEach((x) => x.setAttribute("aria-pressed", x === b));
      // Run the card animation again.
      $$(".gene", el).forEach((c) => { c.style.animation = "none"; void c.offsetWidth; c.style.animation = ""; });
      apply();
    });
    $("#gene-search").addEventListener("input", apply);
    el.addEventListener("click", (e) => {
      const a = e.target.closest("[data-lab]");
      if (a) { labGenes = new Set([a.dataset.lab]); renderLabToggles(); }
    });
    apply();
    $$("canvas[data-thumb]", el).forEach((c) => {
      drawStill(c, [geneById[c.dataset.thumb]], { w: 64, h: 64, scale: 1.85, cy: 0.38, turn: c.dataset.thumb === "shade" || c.dataset.thumb === "lurker" ? 2.6 : -0.55 });
    });
  }
  // A hash like #genes/f-tank selects a family filter.
  addEventListener("hashchange", () => {
    const m = /^#genes\/f-(\w+)/.exec(location.hash);
    if (m) { const b = $(`#gene-filters [data-filter="${m[1]}"]`); if (b) b.click(); }
  });

  // ---------------------------------------------------------------------
  // Gene Lab

  let labGenes = new Set(["cinder", "ironhide", "bonerunner"].filter((id) => geneById[id]));
  let labNight = false;
  let labTurn = -0.5;
  let labSpin = !reduced;
  let labWalk = false;
  let labBaby = false;
  let labModel = null;
  let labMsg = "";

  function renderLab() {
    const el = $("#page-lab");
    el.innerHTML = `
      <header class="page-head">
        <div class="eyebrow">Gene Lab</div>
        <h1>Put genes on a zombie</h1>
        <p>The lab uses the real gene marks and gene models of the mod. It applies them in the same order as the game. The base skin is a flat drawing of the wiki, not the texture of the game.</p>
      </header>
      <div class="lab">
        <div class="lab-stage panel">
          <div class="lab-canvas-wrap" id="lab-wrap"><canvas id="lab-canvas" role="img" aria-label="A zombie with the selected genes"></canvas></div>
          <div class="lab-row">
            <button class="btn small" id="lab-spin" aria-pressed="${labSpin}">Turn</button>
            <button class="btn small" id="lab-walk" aria-pressed="${labWalk}">Walk</button>
            <button class="btn small" id="lab-baby" aria-pressed="${labBaby}" data-tip="Only the Runt gene makes baby zombies">Baby</button>
            <button class="btn small" id="lab-night" aria-pressed="${labNight}">${icon("moon")} Night</button>
            <button class="btn small dark" id="lab-clear">Remove all</button>
          </div>
          <label class="lab-row" style="width:100%"><span class="pixel">Angle</span><input class="range" id="lab-angle" type="range" min="-180" max="180" value="${Math.round((labTurn * 180) / Math.PI)}"></label>
          <div class="lab-result" id="lab-result"></div>
        </div>
        <div>
          <div class="panel">
            <h3 style="margin-top:0">Genes <span class="muted" id="lab-count"></span></h3>
            <p class="muted" style="margin-top:-6px">A zombie carries a maximum of 5 genes. The family result uses the rules of the mod. The Baby button shows the marks on a baby zombie, as the game fits them. Only the Runt gene makes baby zombies.</p>
            <div id="lab-msg" class="sim-log" aria-live="polite"></div>
            <div id="lab-toggles"></div>
          </div>
        </div>
      </div>`;

    const wrap = $("#lab-wrap");
    $("#lab-spin").addEventListener("click", (e) => { labSpin = !labSpin; e.currentTarget.setAttribute("aria-pressed", labSpin); });
    $("#lab-walk").addEventListener("click", (e) => { labWalk = !labWalk; e.currentTarget.setAttribute("aria-pressed", labWalk); });
    $("#lab-night").addEventListener("click", (e) => { labNight = !labNight; e.currentTarget.setAttribute("aria-pressed", labNight); wrap.classList.toggle("night", labNight); });
    $("#lab-baby").addEventListener("click", (e) => { labBaby = !labBaby; e.currentTarget.setAttribute("aria-pressed", labBaby); renderLabToggles(); });
    $("#lab-clear").addEventListener("click", () => { labGenes.clear(); labMsg = ""; renderLabToggles(); });
    $("#lab-angle").addEventListener("input", (e) => { labTurn = (e.target.value * Math.PI) / 180; labSpin = false; $("#lab-spin").setAttribute("aria-pressed", false); });
    let drag = null;
    wrap.addEventListener("pointerdown", (e) => { drag = { x: e.clientX, t: labTurn }; wrap.setPointerCapture(e.pointerId); labSpin = false; $("#lab-spin").setAttribute("aria-pressed", false); });
    wrap.addEventListener("pointermove", (e) => { if (drag) { labTurn = drag.t + (e.clientX - drag.x) * 0.012; syncAngle(); } });
    wrap.addEventListener("pointerup", () => { drag = null; });
    $("#lab-toggles").addEventListener("click", (e) => {
      const b = e.target.closest("[data-gene]");
      if (!b) return;
      const id = b.dataset.gene;
      if (labGenes.has(id)) { labGenes.delete(id); labMsg = ""; }
      else if (labGenes.size >= 5) { labMsg = "A zombie carries a maximum of 5 genes. Remove a gene first."; }
      else { labGenes.add(id); labMsg = ""; }
      renderLabToggles();
    });
    renderLabToggles();
    startLabLoop();
  }
  function syncAngle() {
    let deg = Math.round((labTurn * 180) / Math.PI) % 360;
    if (deg > 180) deg -= 360;
    if (deg < -180) deg += 360;
    const r = $("#lab-angle");
    if (r) r.value = deg;
  }

  function renderLabToggles() {
    const box = $("#lab-toggles");
    if (!box) return;
    box.innerHTML = D.families.map((f) => `
      <div class="lab-family" style="--fam:${FAMILY_COLORS[f.id]}">
        <h4>${esc(f.name)}</h4>
        <div class="toggles">${D.genes.filter((g) => g.family === f.id).map((g) => `
          <button class="btn small toggle" style="--fam:${FAMILY_COLORS[g.family]}" data-gene="${g.id}" aria-pressed="${labGenes.has(g.id)}" ${tipAttr(() => geneTip(g))}>
            <span class="chk"></span><span>${esc(g.name)}${g.visual ? "" : ' <span class="muted" style="font-size:.75rem">(no look yet)</span>'}</span>
          </button>`).join("")}
        </div>
      </div>`).join("");
    $("#lab-count").textContent = `${labGenes.size} of 5`;
    $("#lab-msg").textContent = labMsg;
    const genes = [...labGenes].map((id) => geneById[id]);
    const r = familyOf(genes);
    if (genes.length) recordFound(r);
    $("#lab-result").innerHTML = `<div class="fam-name" style="color:${r.color}">${esc(r.name)}</div><div class="muted">${r.kind === "neutral" ? "No family effects" : esc(kindName(r.kind))}${r.style ? ": " + esc(r.style) : ""}</div>
      <div class="points">${r.points.map((p) => `<span class="badge fam" style="--fam:${FAMILY_HEX[p.id]}">${esc(famName(p.id))} ${p.points}</span>`).join("")}</div>`;
    Z.bake(genes.filter((g) => g.visual), labBaby).then((m) => { labModel = m; });
  }

  let labLoop = false;
  function startLabLoop() {
    if (labLoop) return;
    labLoop = true;
    let last = performance.now();
    const frame = (now) => {
      const dt = Math.min(0.05, (now - last) / 1000);
      last = now;
      if (current === "lab" && !document.hidden) {
        if (labSpin) { labTurn += dt * 0.7; syncAngle(); }
        drawLab(now / 1000);
      }
      requestAnimationFrame(frame);
    };
    requestAnimationFrame(frame);
  }
  let labCtx = null;
  function drawLab(t = 0) {
    const canvas = $("#lab-canvas");
    if (!canvas) return;
    if (!labCtx || labCtx.canvas !== canvas) labCtx = Object.assign(setupCanvas(canvas, 300, 400), { canvas });
    const { ctx, dpr } = labCtx;
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (!labModel) return;
    const genes = [...labGenes].map((id) => geneById[id]);
    const opacity = labNight ? Z.opacityAt(genes, 0) : 1;
    Z.draw(ctx, labModel, { scale: 9 * dpr, cx: canvas.width / 2, cy: canvas.height * 0.33, turn: labTurn, tilt: -0.12, pose: Z.pose(t, labWalk), light: labNight ? 0.42 : 1, opacity });
  }

  // ---------------------------------------------------------------------
  // Notebook

  let bookTab = "genes";
  let bookCategory = "temperate";
  let bookZombie = null;
  function renderNotebookPool() { if (current === "notebook") renderSheets(); }

  function renderNotebook() {
    const el = $("#page-notebook");
    if (!el.dataset.ready) {
      el.dataset.ready = "1";
      const m4 = D.notebook.m4;
      const m3 = D.roadmap.find((r) => r.key === "Milestone 3");
      el.innerHTML = `
        <header class="page-head">
          <div class="eyebrow">${esc(D.notebook.itemName)}</div>
          <h1>Record each species</h1>
          <p>The Beagle Notebook is an item. It has a tab for each species. Each species tab has a gene page and a family page. ${statusBadge("development")}</p>
        </header>
        <div class="book">
          <div class="ribbons" role="group" aria-label="Species">
            <button class="ribbon" aria-pressed="true" style="--rib:#3f7f1c">Zombie</button>
            <button class="ribbon" disabled style="--rib:#a46a0c" data-tip="The husk comes in milestone 2b">Husk</button>
            <button class="ribbon" disabled style="--rib:#1f6f8a" data-tip="The drowned comes in milestone 2b">Drowned</button>
            <button class="ribbon" disabled style="--rib:#6b6b6b" data-tip="The other hostile mobs come in milestone 2c">More</button>
          </div>
          <div class="spread" id="spread">
            <div class="sheet left" id="sheet-left"></div>
            <div class="sheet right" id="sheet-right"></div>
            <div class="edge-tabs" role="group" aria-label="Notebook pages">
              <button class="edge-tab" data-tab="genes" aria-pressed="true">${esc(lang["screen.originofspawns.notebook.section.genes"] || "Genes")}</button>
              <button class="edge-tab" data-tab="families" aria-pressed="false">${esc(lang["screen.originofspawns.notebook.section.families"] || "Families")}</button>
            </div>
          </div>
        </div>
        <p class="muted" style="text-align:center;margin-top:14px">This notebook reads the pools of the simulator on the Evolution page, and the families that you find there and in the Gene Lab.</p>

        <section class="section grid two">
          <div>
            <h2>The mob info panel</h2>
            <p>When you look at a mob, a small panel shows at the top of the screen. It shows the family of the mob and its dominant genes. Mods such as AppleSkin and Jade show information in a similar way. ${statusBadge("development")}</p>
            <div class="lab-row" style="justify-content:flex-start;margin-bottom:12px">
              <button class="btn small green" id="info-next">Look at a different zombie</button>
              <button class="btn small" id="info-night" aria-pressed="false">${icon("moon")} Night</button>
            </div>
          </div>
          <div class="scene" id="scene">
            <div class="mob-info" id="mob-info"></div>
            <canvas id="scene-zombie" role="img" aria-label="A zombie in front of the player"></canvas>
            <div class="crosshair"></div>
          </div>
        </section>

        <section class="section" id="perks">
          <h2>Knowledge and perks</h2>
          <p class="lead">The notebook shows a knowledge percentage. It is the part of all notebook entries that you filled. The knowledge percentage sets the number of perk slots. ${statusBadge("planned")}</p>
          <div class="grid two">
            <div class="panel">
              <h3>Perk slots</h3>
              ${D.notebook.knowledge.map((k) => `<div class="xp-row" style="margin:8px 0"><div class="xp"><span data-w="${parseFloat(k.knowledge)}"></span></div><div class="xp-level" style="min-width:110px;text-align:right">${esc(k.slots)} ${k.slots === "1" ? "slot" : "slots"}</div></div><div class="muted" style="font-size:.85rem;margin-top:-4px">At ${esc(k.knowledge)} knowledge</div>`).join("")}
            </div>
            <div class="panel">
              <h3>The ${D.perks.length} perks</h3>
              <p class="muted">Each perk has a gain and a cost. You pay XP levels to take a perk. Point at a slot to read it.</p>
              <div style="display:flex;flex-wrap:wrap;gap:4px">${D.perks.map((p) => `<span class="slot" tabindex="0" ${tipAttr(() => `<div class="t-name t-gold">${esc(p.name)}</div><div class="t-gain">+ ${esc(p.gain)}</div><div class="t-loss">- ${esc(p.cost)}</div><div class="t-gray">Milestone 3</div>`)} style="font-family:var(--font-pixel);font-size:.8rem;color:#fff;text-align:center;line-height:1">${esc(p.name.split(" ").map((w) => w[0]).join("").toUpperCase())}</span>`).join("")}</div>
            </div>
          </div>
          ${m3 ? `<p class="muted">Milestone 3 adds the perks and the knowledge percentage.</p>` : ""}
        </section>

        <section class="section">
          <h2>The full notebook</h2>
          <div class="planned-box"><h4>${statusBadge("planned")} Milestone 4</h4><ul style="margin:0">${(m4 ? m4.items : []).map((x) => `<li>${esc(x)}</li>`).join("")}</ul></div>
        </section>`;

      $(".edge-tabs", el).addEventListener("click", (e) => {
        const b = e.target.closest("[data-tab]");
        if (!b || b.dataset.tab === bookTab) return;
        const back = b.dataset.tab === "genes";
        bookTab = b.dataset.tab;
        $$(".edge-tab", el).forEach((x) => x.setAttribute("aria-pressed", x === b));
        turnPage(back);
      });
      $("#info-next").addEventListener("click", nextInfo);
      $("#info-night").addEventListener("click", (e) => {
        const on = e.currentTarget.getAttribute("aria-pressed") !== "true";
        e.currentTarget.setAttribute("aria-pressed", on);
        $("#scene").classList.toggle("night", on);
        drawInfo();
      });
      nextInfo();
      requestAnimationFrame(() => $$("[data-w]", el).forEach((s) => { s.style.width = Math.max(4, +s.dataset.w) + "%"; }));
    }
    renderSheets();
  }

  function turnPage(back) {
    const spread = $("#spread");
    if (reduced) { renderSheets(); return; }
    const leaf = document.createElement("div");
    leaf.className = "turn" + (back ? " reverse" : "");
    spread.appendChild(leaf);
    setTimeout(renderSheets, 300);
    setTimeout(() => leaf.remove(), 640);
  }

  function renderSheets() {
    const left = $("#sheet-left"), right = $("#sheet-right");
    if (!left) return;
    const cats = D.evolution.categories;
    if (bookTab === "genes") {
      const p = pool(bookCategory);
      const known = D.genes.filter((g) => p[g.id] > 0).sort((a, b) => p[b.id] - p[a.id]);
      left.innerHTML = `
        <canvas class="sketch" id="sketch" aria-label="A field sketch of a zombie"></canvas>
        <h3>Zombie</h3>
        <p class="ink-note">The common zombie. It changes in each biome category. Its marks tell its history.</p>
        <h4>Biome category</h4>
        <div class="lab-row" style="justify-content:flex-start;gap:6px" id="book-cats">${cats.map((c) => `<button class="btn small" data-bcat="${c.id}" aria-pressed="${c.id === bookCategory}" style="padding:3px 8px 5px;min-height:30px">${icon(c.id)}${esc(c.name)}</button>`).join("")}</div>
        <span class="folio">1</span>`;
      right.innerHTML = `
        <h3>${esc(lang["screen.originofspawns.notebook.section.genes"] || "Genes")}: ${esc(cats.find((c) => c.id === bookCategory).name)}</h3>
        ${known.length ? known.slice(0, 12).map((g) => `<div class="freq" style="--fam:${FAMILY_HEX[g.family]}"><span class="pixel">${esc(g.name)}</span><span class="bar"><span style="width:${p[g.id] * 100}%"></span></span><span class="pixel">${pct(p[g.id])}</span></div>`).join("")
          : `<p class="ink-note">${esc(lang["screen.originofspawns.notebook.no_genes"] || "No genes are known for this species.")}</p><p class="ink-note">Kill zombies in the <a href="#evolution">pool simulator</a> to fill this page.</p>`}
        <span class="folio">2</span>`;
      $("#book-cats").addEventListener("click", (e) => {
        const b = e.target.closest("[data-bcat]");
        if (b) { bookCategory = b.dataset.bcat; renderSheets(); }
      });
      const top = known.slice(0, 5).filter((g) => g.visual);
      drawStill($("#sketch"), top, { w: 120, h: 160, scale: 3.6, cy: 0.33, turn: -0.6, draw: { light: 0.95 } });
    } else {
      const all = [
        ...D.families.map((f) => ({ key: f.id, name: f.name, kind: "pure" })),
        ...D.minors.map((m) => ({ key: `${m.major}>${m.sub}`, name: m.name, kind: "minor" })),
        ...D.hybrids.map((h) => ({ key: [h.a, h.b].sort().join("+"), name: h.name, kind: "hybrid" })),
        { key: "neutral", name: "Neutral", kind: "neutral" },
      ];
      const n = all.filter((x) => found.has(x.key)).length;
      const list = (kind) => `<div class="found">${all.filter((x) => x.kind === kind).map((x) => `<span class="f${found.has(x.key) ? "" : " no"}">${found.has(x.key) ? esc(x.name) : "?"}</span>`).join("")}</div>`;
      left.innerHTML = `
        <h3>${esc(lang["screen.originofspawns.notebook.section.families"] || "Families")}</h3>
        <p class="pixel">${esc(fmt("screen.originofspawns.notebook.found", n, all.length))}</p>
        <h4>${esc(lang["screen.originofspawns.notebook.kind.pure"] || "Pure families")}</h4>${list("pure")}
        <h4 style="margin-top:12px">${esc(lang["screen.originofspawns.notebook.kind.hybrid"] || "Hybrids")}</h4>${list("hybrid")}
        <h4 style="margin-top:12px">${esc(lang["screen.originofspawns.notebook.kind.neutral"] || "Neutral")}</h4>${list("neutral")}
        <span class="folio">3</span>`;
      right.innerHTML = `
        <h4>${esc(lang["screen.originofspawns.notebook.kind.minor"] || "Minor families")}</h4>${list("minor")}
        <p class="ink-note" style="margin-top:14px">A family name shows when you find a zombie of that family. Spawn zombies in the simulator, or mix genes in the Gene Lab.</p>
        <span class="folio">4</span>`;
    }
  }

  function nextInfo() {
    const pick = [];
    const pool = [...D.genes].sort(() => Math.random() - 0.5);
    const n = 1 + Math.floor(Math.random() * 4);
    for (const g of pool) { if (pick.length >= n) break; pick.push(g); }
    bookZombie = pick;
    drawInfo();
  }
  function drawInfo() {
    const r = familyOf(bookZombie);
    const box = $("#mob-info");
    box.style.animation = "none"; void box.offsetWidth; box.style.animation = "";
    box.innerHTML = `<div class="n" style="color:${r.color}">${esc(r.name)}</div>${r.kind === "neutral" ? "" : `<div class="k">${esc(kindName(r.kind))}</div>`}<div class="g">${bookZombie.length ? bookZombie.map((g) => esc(g.name)).join(", ") : esc(lang["mob_info.originofspawns.no_genes"])}</div>`;
    const night = $("#scene").classList.contains("night");
    const vis = bookZombie.filter((g) => g.visual);
    drawStill($("#scene-zombie"), vis, { w: 150, h: 200, scale: 4.6, cy: 0.33, turn: -0.25, draw: { light: night ? 0.5 : 1, opacity: night ? Z.opacityAt(bookZombie, 0) : 1 } });
  }

  // ---------------------------------------------------------------------
  // Data packs

  function renderDataPacks() {
    const el = $("#page-datapacks");
    el.innerHTML = `
      <header class="page-head">
        <div class="eyebrow">For pack makers</div>
        <h1>Data packs</h1>
        <p>Data packs define the pressures, the genes, the families, and the perks of the mod. This page shows the guide <a href="${esc(D.repo)}/blob/main/docs/data-packs.md" target="_blank" rel="noopener">docs/data-packs.md</a> of the repository.</p>
      </header>
      <div class="docs">
        <nav class="panel toc" aria-label="Contents"><h4>Contents</h4>${D.dataPacks.toc.map((t) => `<a class="l${t.level}" href="#${t.id}">${esc(t.title)}</a>`).join("")}</nav>
        <article class="doc">${D.dataPacks.html}</article>
      </div>`;
  }

  // ---------------------------------------------------------------------
  // Roadmap

  function renderRoadmap() {
    const el = $("#page-roadmap");
    const parts = D.roadmap.filter((r) => r.items.length);
    const next = parts.find((r) => !r.done);
    const group = (after) => parts.filter((r) => r.after1 === after).map((r) => `
      <div class="panel milestone${r.done ? " done" : r === next ? " next" : ""}">
        <span class="slot">${icon(r.done ? "check" : r === next ? "clock" : "lock")}</span>
        <div>
          <div class="eyebrow" style="font-size:.8rem">${esc(r.key)}</div>
          <h3>${esc(r.title.charAt(0).toUpperCase() + r.title.slice(1))} ${r.done ? '<span class="badge released">Done</span>' : r === next ? '<span class="badge development">In development</span>' : '<span class="badge planned">Planned</span>'}</h3>
          <details${r === next ? " open" : ""}><summary>${r.items.length} items</summary><ul>${r.items.map((x) => `<li>${esc(x)}</li>`).join("")}</ul></details>
        </div>
      </div>`).join("");
    el.innerHTML = `
      <header class="page-head">
        <div class="eyebrow">Roadmap</div>
        <h1>The road to version 1.0.0</h1>
        <p>Each milestone needs the milestones before it. Each finished milestone makes an alpha release. This page reads <a href="${esc(D.repo)}/blob/main/design/ROADMAP.md" target="_blank" rel="noopener">design/ROADMAP.md</a>.</p>
      </header>
      <div class="panel wood" style="margin-bottom:22px">
        <div class="xp-row"><div class="xp" id="road-xp"><span></span></div><div class="xp-level">${v1Done}/${v1.length}</div></div>
        <p style="margin:10px 0 0">${v1Done} of ${v1.length} parts to version 1.0.0 are done.</p>
      </div>
      <h2>Version 1.0.0</h2>
      <div class="milestones">${group(false)}</div>
      <h2 class="section">After version 1.0.0</h2>
      <div class="milestones">${group(true)}</div>`;
    requestAnimationFrame(() => { $("#road-xp span").style.width = (v1Done / v1.length) * 100 + "%"; });
  }

  // ---------------------------------------------------------------------
  // Download

  function renderDownload() {
    const el = $("#page-download");
    el.innerHTML = `
      <header class="page-head">
        <div class="eyebrow">Download</div>
        <h1>Get the mod</h1>
        <p>The newest release is ${esc(D.version)}. It is an alpha release. An alpha release has the systems, but not all of the content.</p>
      </header>
      <img class="banner" src="img/banner.jpg" alt="On the Origin of Spawns: an open notebook in front of a strand of DNA" width="1400" height="788" loading="lazy">
      <div class="downloads">
        <div class="panel stone download"><span class="slot big">${brandIcon.modrinth.replace('fill="#fff"', 'fill="#00af5c"')}</span><h3>Modrinth</h3><a class="btn modrinth" href="${esc(D.links.modrinth)}" target="_blank" rel="noopener">${brandIcon.modrinth}Download</a></div>
        <div class="panel stone download"><span class="slot big">${brandIcon.curseforge.replace('fill="#fff"', 'fill="#f16436"')}</span><h3>CurseForge</h3><a class="btn curseforge" href="${esc(D.links.curseforge)}" target="_blank" rel="noopener">${brandIcon.curseforge}Download</a></div>
        <div class="panel stone download"><span class="slot big">${brandIcon.github}</span><h3>GitHub</h3><a class="btn dark" href="${esc(D.links.releases)}" target="_blank" rel="noopener">${brandIcon.github}Releases</a></div>
      </div>
      <section class="section">
        <h2>What you need</h2>
        <div class="req">
          <div class="panel"><b>${esc(D.minecraft)}</b>Minecraft</div>
          <div class="panel"><b>${esc(D.fabric)}+</b>Fabric Loader</div>
          <div class="panel"><b>${esc(D.version)}</b>Newest release</div>
        </div>
      </section>
      <section class="section grid two">
        <div class="panel wood">
          <h2 style="margin-top:0">Support the project</h2>
          <p>If you like On the Origin of Spawns, you can support its development.</p>
          <a class="btn kofi" href="${esc(D.links.kofi)}" target="_blank" rel="noopener">${brandIcon.kofi}Support on Ko-fi</a>
        </div>
        <div class="panel">
          <h2 style="margin-top:0">Licence</h2>
          <p>All rights reserved. The <a href="${esc(D.repo)}/blob/main/LICENSE" target="_blank" rel="noopener">LICENSE</a> file tells what you can do with this work.</p>
          <p class="muted">This is not an official Minecraft product. Mojang and Microsoft do not approve it, and it has no connection with them.</p>
        </div>
      </section>`;
  }

  // ---------------------------------------------------------------------
  // Footer and start

  $("#footer").innerHTML = `
    <div class="inner">
      <div class="footer-links">
        <a href="${esc(D.links.modrinth)}" target="_blank" rel="noopener">Modrinth</a>
        <a href="${esc(D.links.curseforge)}" target="_blank" rel="noopener">CurseForge</a>
        <a href="${esc(D.links.github)}" target="_blank" rel="noopener">GitHub</a>
        <a href="${esc(D.links.kofi)}" target="_blank" rel="noopener">Ko-fi</a>
      </div>
      <div>On the Origin of Spawns by Stiven Gjekaj. All rights reserved. This is not an official Minecraft product. Mojang and Microsoft do not approve it, and it has no connection with them.</div>
      <div style="opacity:.75">The wiki data comes from the mod files. Last build: ${esc(D.generated)}.</div>
    </div>`;

  renderHome();
  renderEvolution();
  renderFamilies();
  renderGenes();
  renderLab();
  renderDataPacks();
  renderRoadmap();
  renderDownload();
  addEventListener("hashchange", route);
  route();
  if (/^#genes\/f-/.test(location.hash)) dispatchEvent(new HashChangeEvent("hashchange"));
})();
