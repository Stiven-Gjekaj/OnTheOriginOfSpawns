# Wiki website

This folder holds the wiki of On the Origin of Spawns.
It is a static site. It has no backend.

## Serve the wiki

The Gene Lab reads the mod textures, so the browser must load the site from a
server, not from a file.
Run this command from the root of the repository:

    python3 -m http.server 8000 --directory website

Then open http://localhost:8000 in a browser.

## Update the data

The pages read `js/data.js`.
`build.mjs` makes this file from the files of the mod.
Run it after a change to the genes, the families, the pressures, the language
file, the gene visuals, the design files, or the data pack guide:

    node website/build.mjs

The script needs Node 18 or later, and no packages.
It reads these files:

- `src/main/resources/data/originofspawns/originofspawns/gene/*.json`
- `src/main/resources/data/originofspawns/originofspawns/family/zombie/*.json`
- `src/main/resources/data/originofspawns/originofspawns/pressure/*.json`
- `src/main/resources/assets/originofspawns/lang/en_us.json`
- `src/main/resources/assets/originofspawns/gene_visuals/*.json` and `gene_models/*.json`
- `design/DESIGN.md`, `design/ROADMAP.md`, and `design/species/zombie.md`
- `docs/data-packs.md`, `README.md`, and `gradle.properties`

It also copies the textures that the gene visuals use into `mod/`.
Do not edit `js/data.js` or `mod/`. The script writes them again.

The state of each feature comes from the roadmap.
A gene that a finished milestone names shows as part of the release.
The other genes show as milestone 2a.
When milestone 2a gets "(done)" in `design/ROADMAP.md`, run the script again.

## Files

| File | Content |
|---|---|
| `index.html` | The page shell |
| `css/site.css` | The styles: panels, slots, buttons, tooltips, and the XP bar |
| `js/app.js` | The pages |
| `js/zombie.js` | The zombie renderer of the hero, the Gene Lab, and the gene cards |
| `js/data.js` | The data from `build.mjs` |
| `img/` | The mod icon, the banner, and the notebook item |
| `mod/` | The mod textures from `build.mjs` |
| `LICENSES.md` | The licences of the font and the images |
