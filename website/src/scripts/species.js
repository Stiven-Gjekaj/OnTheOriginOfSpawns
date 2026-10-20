// The selected species of a page. Buttons with data-species-btn select it.
// Elements with data-species-panel show only for their species. The page
// gets a "species" event when the species changes.

import { species } from "../data/site.json";

const ids = species.map((s) => s.id);
let current = null;

export function getSpecies() {
  return current;
}

export function setSpecies(id, { push = true } = {}) {
  const btn = document.querySelector(`[data-species-btn="${id}"]`);
  if (!ids.includes(id) || btn?.disabled) id = ids[0];
  current = id;
  document.querySelectorAll("[data-species-btn]").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.speciesBtn === id)));
  document.querySelectorAll("[data-species-panel]").forEach((el) => { el.hidden = el.dataset.speciesPanel !== id; });
  if (push) {
    const u = new URL(location.href);
    if (id === ids[0]) u.searchParams.delete("species"); else u.searchParams.set("species", id);
    history.replaceState(null, "", u);
  }
  document.dispatchEvent(new CustomEvent("species", { detail: id }));
}

document.addEventListener("click", (e) => {
  const b = e.target.closest("[data-species-btn]");
  if (b && !b.disabled) setSpecies(b.dataset.speciesBtn);
});

setSpecies(new URLSearchParams(location.search).get("species") || ids[0], { push: false });
