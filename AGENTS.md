# Agent instructions (Team Sneakymouse / Lords of Minecraft)

This repo is a **Paper plugin template** (Kotlin, Paper 26.2, Gradle). All coding agents (Cursor, Claude Code, Codex, Antigravity, Hermes, etc.) should follow this file.

<!-- BEGIN TEAM-POLICY MANAGED BOOTSTRAP -->
This project is enrolled in Team-Sneakymouse development policy via a sibling checkout (see `.team-policy/pointer.json`).

Resolve the policy root: try each `local_candidates` path in the pointer relative to this project root. If none exist, `git clone` the pointer `remote` into the first candidate using the developer's existing Git credentials. If a checkout exists but is behind `origin/<track_branch>`, `git fetch` and fast-forward that branch when safe; if local edits block the update, report and ask.

Before implementing, or when authorization or risk is unclear, read `<policy-root>/policy/core.md`. Read `<policy-root>/policy/scope-and-testing.md` when classifying risk or changing behavior. Read `<policy-root>/workflows/development.md` for nontrivial multi-step work. Read `<policy-root>/policy/sneakyutils.md` when adding or sharing Paper util/manager code. Load optional skills from `<policy-root>/skills/` only when relevant.

Do not substitute remembered rules, a web mirror, or an unlisted path. Follow local build/architecture facts and explicit exceptions below. When host instructions and team policy repeat the same norm, follow the host wording. If they require incompatible actions, surface the conflict before expanding work. Distinguish running checks from implementing tests; a source edit does not automatically require a new test.
<!-- END TEAM-POLICY MANAGED BOOTSTRAP -->

## Project facts

| | |
|--|--|
| Project | SneakyRacing (racing rings Paper plugin) |
| Shared policy | sibling checkout via `.team-policy/pointer.json` → `team-development-policy` |
| Runtime targets | Paper API 26.2, Java/Kotlin JVM 25 |
| Shared Paper utils | SneakyUtils `compileOnly` + runtime `depend` (see team policy `sneakyutils.md`) |
| Deployment | push to `main` triggers publish via `.github/workflows/publish.yml` |
| Build output | `~/.cache/SneakyRacing-gradle-build` (keeps Gradle off FUSE/NTFS checkouts; see README) |

Custom item look, CraftEngine/pack model id, server resource pack, or ItemDisplay place/orient → read `<policy-root>/policy/paper-items.md`.

### Local exceptions

- **Stricter testing (template):** if you change behavior under `src/main/`, add or update tests under `src/test/` in the same change, and run `./gradlew test` (or `check`) before finishing. Rationale: this repo is the team template; every behavior change should demonstrate the test harness. Approver: pilot enrollment 2026-10-18. This is stricter than shared proportional defaults and wins for this repository.

## Commands

| Task | Command |
|------|---------|
| Build + unit tests | `./gradlew check` (includes `publishPreflight`) |
| Publish preflight only | `./gradlew publishPreflight` or `bash scripts/publish-preflight.sh` |
| Unit tests only | `./gradlew test` |
| Local Paper server | `./gradlew runServer` |
| Publish (CI) | `./gradlew publishMavenPublicationToSneakyrpRepository` (requires Maven credentials) |

Builds require a **git checkout** with at least one commit (version is derived from `HEAD`).

## Architecture (keep plugins testable)

Put code where it can be tested without a live server when possible:

```text
src/main/kotlin/<package>/
  SneakyRacing.kt      # Bootstrap only: config, register commands/listeners
  service/             # Rules, calculations, domain logic — no org.bukkit imports where possible
  commands/            # Parse args, call services, send messages
  listener/            # Events → services
  util/                # Plugin-specific helpers (do not copy TextUtility here)
src/test/kotlin/<package>/   # Mirror packages; *Test.kt next to the unit under test
```

Shared text helpers (`com.sneakyutils.util.TextUtility`) come from the **SneakyUtils**
`compileOnly` dependency — keep the `paper-plugin.yml` server dependency on `SneakyUtils`.

Guidelines:

- **Business logic** belongs in `service/` or `util/`, not in listeners or command `execute` bodies.
- **Commands and listeners** are thin adapters over services.
- Avoid new uses of **`SneakyRacing.instance`**; prefer constructor parameters (`JavaPlugin`, services) so tests stay simple.
- Classes loaded by MockBukkit (plugin, commands) must be **`open`** in Kotlin.

## Testing (local exception — required)

1. If you change behavior under `src/main/`, **add or update** tests under `src/test/` in the same change.
2. Run **`./gradlew test`** (or `./gradlew check`) and fix failures before finishing the task.
3. Do not rely on `./gradlew runServer` as the only verification for logic changes.

### What to test how

| Kind of code | Approach |
|--------------|----------|
| `util/`, `service/` (no Bukkit) | Plain **JUnit 5** (see `TextUtilityTest` — uses SneakyUtils) |
| Permissions, command wiring, enable/disable | **MockBukkit** (`mockbukkit-v26.2`, see `SneakyRacingTest`, `CommandBaseTest`) |
| Full gameplay / integration | Manual **`runServer`** smoke tests |

MockBukkit lifecycle in every MockBukkit test class:

- `@BeforeEach`: `MockBukkit.mock()`
- `@AfterEach`: `MockBukkit.unmock()`

Skipped tests from unimplemented MockBukkit APIs are acceptable; failing tests are not.

## Code style

- Match existing Kotlin style and package layout.
- Keep diffs minimal; do not refactor unrelated code.
- `CommandBase` sets permission to `{identifier}.command.{name}` using `SneakyRacing.IDENTIFIER`.

## Definition of done

- [ ] Behavior covered or updated in `src/test/`
- [ ] `./gradlew check` passes locally
- [ ] No secrets committed (Maven credentials via env / Infisical only)
- [ ] `paper-plugin.yml` and POM metadata updated if plugin identity changed

## Human docs

- `README.md` — template usage, publish flow, rename checklist
