// The item tooltip of the game. An element with data-tip shows its text. An
// element with data-tip-ref shows the content of the template with that id.

const tip = document.getElementById("tooltip");
let target = null;

function place(x, y) {
  const pad = 14;
  const r = tip.getBoundingClientRect();
  let left = x + pad;
  let top = y + pad;
  if (left + r.width > innerWidth - 8) left = Math.max(8, x - r.width - pad);
  if (top + r.height > innerHeight - 8) top = Math.max(8, y - r.height - pad);
  tip.style.left = `${left}px`;
  tip.style.top = `${top}px`;
}

function show(el, x, y) {
  if (el.dataset.tipRef) {
    const tpl = document.getElementById(el.dataset.tipRef);
    if (!tpl) return;
    tip.replaceChildren(tpl.content.cloneNode(true));
  } else {
    tip.textContent = el.dataset.tip;
  }
  target = el;
  place(x, y);
  requestAnimationFrame(() => tip.classList.add("show"));
}

function hide() {
  tip.classList.remove("show");
  target = null;
}

const find = (el) => el?.closest?.("[data-tip],[data-tip-ref]");

document.addEventListener("pointerover", (e) => {
  if (e.pointerType === "touch") return;
  const el = find(e.target);
  if (el && el !== target) show(el, e.clientX, e.clientY);
  else if (!el && target) hide();
});
document.addEventListener("pointermove", (e) => {
  if (target) place(e.clientX, e.clientY);
});
document.addEventListener("focusin", (e) => {
  const el = find(e.target);
  if (el) {
    const r = el.getBoundingClientRect();
    show(el, r.left + 8, r.bottom);
  } else hide();
});
document.addEventListener("scroll", hide, { passive: true });
