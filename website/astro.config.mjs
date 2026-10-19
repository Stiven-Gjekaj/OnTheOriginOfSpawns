// The Astro configuration of the wiki.
// GitHub Pages serves the site under /OnTheOriginOfSpawns/, so all links use the base path.
import { defineConfig } from "astro/config";

export default defineConfig({
  site: "https://stiven-gjekaj.github.io",
  base: "/OnTheOriginOfSpawns",
  trailingSlash: "ignore",
  build: { format: "directory" },
  devToolbar: { enabled: false },
});
