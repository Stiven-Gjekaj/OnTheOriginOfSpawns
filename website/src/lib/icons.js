// Pixel icons, drawn as SVG from small grids. They are original drawings.

export const ICONS = {
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
export function icon(name) {
  const ic = ICONS[name];
  if (!ic) return "";
  const rows = ic.g;
  let rects = "";
  rows.forEach((row, y) => [...row].forEach((ch, x) => {
    if (ch === ".") return;
    // The key "g" holds the grid, so the color of the letter g is in "gg".
    const color = typeof ic[ch] === "string" ? ic[ch] : ic[ch + ch];
    if (color) rects += `<rect x="${x}" y="${y}" width="1.02" height="1.02" fill="${color}"/>`;
  }));
  return `<svg viewBox="0 0 ${rows[0].length} ${rows.length}" shape-rendering="crispEdges" aria-hidden="true">${rects}</svg>`;
}
export const brandIcon = {
  modrinth: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="currentColor" d="M2 0h4v1h1v1h1v4H7v1H6v1H2V7H1V6H0V2h1V1h1zM3 2v1H2v2h1v1h2V5h1V3H5V2z"/></svg>`,
  curseforge: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="currentColor" d="M0 1h8v2H6v1H5v1h1v2H2V5h1V4H1V3H0z"/></svg>`,
  kofi: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="currentColor" d="M0 2h6v1h1v1H6v2H5v1H1V6H0zM2 3v1h1v1h1V4h1V3H4v1H3V3z"/></svg>`,
  github: `<svg viewBox="0 0 8 8" shape-rendering="crispEdges" aria-hidden="true"><path fill="currentColor" d="M2 0h4v1h1v1h1v4H7v1H5V6h1V3H5V2H3v1H2v3h1v1H1V6H0V2h1V1h1z"/></svg>`,
};

