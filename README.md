# Respawn Penalty

Server-side Fabric mod for Minecraft 1.21.8 that discourages repeated deaths without relying on datapacks or external thirst APIs.

---

## Features

- Persistent death streak tracking
- Automatic death penalties after respawn
- Sleep recovery locking against abuse
- Passive survival-based recovery
- Multiplayer-safe UUID-based persistence
- Server restart persistence using `PersistentState`
- No Tough As Nails or thirst API dependency
- Dedicated-server friendly design

---

## Requirements

- Minecraft `1.21.8`
- Fabric Loader `>= 0.16.14`
- Fabric API
- Java `21`

---

## Environment

This is a **server-side only** mod.

Clients do not need to install the mod when joining a dedicated server.

> [!IMPORTANT]
> This mod intentionally avoids external thirst/survival APIs to reduce compatibility and crash risks on Minecraft 1.21.8.

---

## Penalty System

When a non-creative, non-spectator player dies and respawns:

| Death streak | Max health | Food | Effects | Sleep recovery lock |
|---:|---:|---:|---|---:|
| 1 | 15 | 9 | Weakness 30s, Slowness 10s, Darkness 3s | none |
| 2 | 14 | 8 | Weakness 45s, Slowness 15s, Darkness 4s | none |
| 3 | 12 | 7 | Weakness II 60s, Slowness 20s, Mining Fatigue 20s, Darkness 5s | none |
| 4-5 | 10 | 6 | Weakness II 75s, Slowness 25s, Mining Fatigue 30s, Darkness 6s | 5 minutes |
| 6+ | 10 | 6 | Weakness II 90s, Slowness II 30s, Mining Fatigue 40s, Darkness 8s | 10 minutes |

Death streak increases only when deaths happen within 10 minutes.

Penalties cap at 10 max health and 6 food to avoid permanent death spirals.

---

## Recovery

A player recovers when either condition happens:

- Successfully sleeps while sleep recovery is not locked
- Survives online for 20 minutes

Repeated intentional deaths temporarily lock sleep recovery:

| Streak | Lock duration |
|---|---|
| 4-5 | 5 minutes |
| 6+ | 10 minutes |

Survival recovery still works during the lock period.

This prevents:

```text
die -> sleep -> reset -> die
````

abuse loops without permanently trapping players.

Recovery restores:

* Original max-health base value
* Normal recovery state
* Cleared penalty effects

---

## Anti-Exploit Guarantees

* Respawn cannot fully restore normal health/food
* Logout/relog keeps active penalty state
* Server restart preserves:

  * death streak
  * active penalties
  * recovery lock
* Multiplayer state is isolated per UUID
* Creative and spectator players are ignored
* Penalty persistence survives crashes and restarts through world `PersistentState`

> [!NOTE]
> Death streak only increases if deaths happen within the configured streak window.

---

## Download

Download the latest release from the GitHub Releases page.

---

## Build

Windows PowerShell:

```powershell
.\gradlew clean build
```

Linux/macOS:

```bash
./gradlew clean build
```

Generated jar:

```text
build/libs/respawn-penalty-<version>.jar
```

---

## Install

1. Install Fabric Loader for Minecraft `1.21.8`
2. Install Fabric API
3. Copy the generated `.jar` file into the server `mods/` directory
4. Restart the server

---

## Configuration

Edit constants in:

```text
src/main/java/com/hoaug/deathpenalty/DeathPenaltyConfig.java
```

Important constants:

* `STREAK_WINDOW_TICKS`
* `RECOVERY_TICKS`
* `MODERATE_RECOVERY_LOCK_TICKS`
* `CRITICAL_RECOVERY_LOCK_TICKS`
* `IGNORE_CREATIVE_AND_SPECTATOR`
* `tierForStreak(...)`

---

## Compatibility

| Component        | Status        |
| ---------------- | ------------- |
| Dedicated Server | Supported     |
| Singleplayer     | Supported     |
| Fabric API       | Required      |
| NeoForge         | Not supported |
| Forge            | Not supported |

---

## License

MIT License
