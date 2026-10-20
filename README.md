# SneakyRacing

Paper plugin for placeable **racing rings** — glowing circular checkpoints that racers pass through while wearing racing goggles.

## Features

- Configurable ring types (tint, scale, respawn timer, commands)
- Place by right-clicking with a ring item (consumes the item)
- Rings are persistent `ItemDisplay` entities (move / rotate / scale / delete with existing display tools)
- Visible only while wearing racing goggles (helmet with plugin PDC tag)
- Per-player hide on cross + configurable respawn timer
- Crossing runs console commands with `{player}`, `{world}`, `{player_x|y|z}`, `{ring_x|y|z}` placeholders

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/sneakyracing reload` | `sneakyracing.command.reload` | Reload config and refresh ring tint/scale from type ids |
| `/sneakyracing give ring <type> [player] [amount]` | `sneakyracing.command.give` | Give placeable ring items |
| `/sneakyracing give goggles [player] [amount]` | `sneakyracing.command.give` | Give racing goggles |

Temporary rings (default) are removed on `/sneakyracing reload` and on server stop. Players with `sneakyracing.ring.permanent` place rings that persist.

## Config

See `src/main/resources/config.yml` for `corrections` (diameter / yaw) and `rings` type definitions.

## Build / test

```bash
./gradlew check          # tests + publish preflight
./gradlew runServer      # local Paper 26.2 (+ pinned SneakyUtils into run/plugins)
```

Requires a git checkout with at least one commit (version is derived from `HEAD`).

Local `run/` should be a symlink to a native filesystem path (e.g. `~/.cache/sneakyracing-run`). Paper 26.2 world COW/file locks break on NTFS/FUSE mounts such as `/mnt/files`.

`./gradlew runServer` downloads the SneakyUtils version pinned in `build.gradle.kts` from `maven.sneakyrp.com` and stages it under `run/plugins` (stale hand-copied jars are removed first).

## Runtime dependency

Requires **SneakyUtils** on the server (`paper-plugin.yml` load `BEFORE`).

## Item model

Rings use CraftEngine / resourcepack id `lom:racing_ring` with CMD tint color index 0.
