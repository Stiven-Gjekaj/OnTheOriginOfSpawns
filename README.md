<p align="center">
  <img src="docs/images/banner.png" alt="On the Origin of Spawns" width="100%">
</p>

<p align="center">
  <a href="https://github.com/Stiven-Gjekaj/OnTheOriginOfSpawns/actions/workflows/build.yml"><img src="https://github.com/Stiven-Gjekaj/OnTheOriginOfSpawns/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.3-62B47A" alt="Minecraft 26.3">
  <img src="https://img.shields.io/badge/loader-Fabric-DBD0B4" alt="Fabric">
  <a href="https://ko-fi.com/stivengjekaj"><img src="https://img.shields.io/badge/Ko--fi-support-FF5E5B?logo=ko-fi&logoColor=white" alt="Ko-fi"></a>
</p>

**Adapt or go extinct.**
The mobs in your world evolve.
Each mob that you kill changes the next generation.
Burn many zombies, and their offspring start to resist fire.
Drown them, and they learn to stay away from water.

You evolve too, but you choose how.
Select small adaptations, each with a trade-off.
Then watch the mobs adapt to counter you.
Record each species in your Beagle Notebook while natural selection and
artificial selection race each other.

## Features

- Each species has gene pools for each biome category.
- The cause of each death changes the pool.
- Genes change how mobs look, fight, sense, and spawn.
- 30 player perks, each with a gain and a cost.
- Perk slots open as the knowledge in your notebook increases.
- The Royal Society lets players share notebook pages for XP.

Some features are not complete.
Refer to [ROADMAP.md](ROADMAP.md) for the plan and to [DESIGN.md](DESIGN.md)
for the full design.

## Status

The mod is in development.
There is no release yet.

## Build

You need Java 21 or later to start Gradle.
Gradle runs the build on Java 25.

```bash
./gradlew build
```

The mod file is in `build/libs/`.

## Release

1. Set `mod_version` in `gradle.properties`.
2. Commit the change.
3. Publish a GitHub release with the tag `v` and the version, for example
   `v0.1.0`.
   The text of the release becomes the changelog.

The `release` workflow then builds the mod.
It uploads the mod file to Modrinth, CurseForge, and the GitHub release.
The workflow uses the `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN` repository
secrets.

## Support

If you like the project, you can support it on
[Ko-fi](https://ko-fi.com/stivengjekaj).

## License

All rights reserved.
Refer to [LICENSE](LICENSE) for what you can do with this work.
