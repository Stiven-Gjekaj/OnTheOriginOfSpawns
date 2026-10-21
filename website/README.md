# Wiki website

This folder holds the wiki of On the Origin of Spawns.
It is a static site that Astro makes.
GitHub Pages serves it at https://stiven-gjekaj.github.io/OnTheOriginOfSpawns/.

## Commands

You need Node 22.12 or later.
Run these commands in `website/`:

| Command | Result |
|---|---|
| `npm ci` | Installs the exact versions of `package-lock.json` |
| `npm run dev` | Makes the data, then starts a server at http://localhost:4321/OnTheOriginOfSpawns/ |
| `npm run build` | Makes the data, then writes the site to `dist/` |
| `npm run preview` | Serves `dist/` at http://localhost:4321/OnTheOriginOfSpawns/ |
| `npm run data` | Makes the data only |

The `pages` workflow runs `npm ci` and `npm run build`, then publishes `dist/`.

## Data

`build.mjs` makes the data of the pages from the files of the mod.
`npm run dev` and `npm run build` run it first.
It reads these files:

- `src/main/resources/data/originofspawns/originofspawns/gene/*.json`
- `src/main/resources/data/originofspawns/originofspawns/family/<species>/*.json`
- `src/main/resources/data/originofspawns/originofspawns/pressure/*.json`
- `src/main/resources/assets/originofspawns/lang/en_us.json`
- `design/DESIGN.md`, `design/ROADMAP.md`, and `design/species/<species>.md`
- `docs/data-packs.md`, `README.md`, and `gradle.properties`

It writes `src/data/site.json`, and copies the notebook item into
`public/img/`.
Git ignores these files, because the build makes them again each time.

Each folder in `family/` is a species. The species needs a design file with
the same name in `design/species/`, or the build stops.
The pictures of the mobs are screenshots of the game, in
`public/shots/<species>/<kind>/<name>-<view>.png`.
The kind is `plain` (a mob with no genes), `gene` (a mob with only that gene),
or `family` (a mob with the 6 genes of that family).
The view is `front` or `back`.
The build lists the pictures that are there, and a page shows no picture
where a file is missing.

The state of each feature comes from the roadmap.
A milestone with "(done)" in `design/ROADMAP.md` is part of the release.
A species is in the release when the first milestone that names it is done.

## Dependencies

Each dependency has an exact version in `package.json`.
`package-lock.json` holds the exact version of each package below them.
All versions were published before 2026-10-06.
To add a package, use the same rule:

    npm install --save-exact --before=<a date two weeks ago> <package>@<version>

## Files

| Path | Content |
|---|---|
| `astro.config.mjs` | The configuration. The base path is `/OnTheOriginOfSpawns` |
| `src/pages/` | One folder for each page |
| `src/layouts/Layout.astro` | The head, the header, and the footer of each page |
| `src/components/` | The parts that the pages share |
| `src/styles/global.css` | The design system: the type scale, the spacing, the colors, and the Minecraft elements |
| `src/lib/` | Helpers for the pages and the browser scripts |
| `src/scripts/` | The browser scripts: the hero, the species switch, the simulator, the notebook, and the tooltip |
| `public/img/` | The mod icon and the banner |
| `public/shots/` | The screenshots of the mobs |
| `LICENSES.md` | The licences of the fonts and the images |
