# Respawn Penalty

Server-side Fabric 1.21.8 mod that discourages repeated deaths without relying on datapacks or thirst APIs.

## Behavior

When a non-creative, non-spectator player dies and respawns:

| Death streak | Max health | Food | Effects | Sleep recovery lock |
|---:|---:|---:|---|---:|
| 1 | 15 | 9 | Weakness 30s, Slowness 10s, Darkness 3s | none |
| 2 | 14 | 8 | Weakness 45s, Slowness 15s, Darkness 4s | none |
| 3 | 12 | 7 | Weakness II 60s, Slowness 20s, Mining Fatigue 20s, Darkness 5s | none |
| 4-5 | 10 | 6 | Weakness II 75s, Slowness 25s, Mining Fatigue 30s, Darkness 6s | 5 minutes |
| 6+ | 10 | 6 | Weakness II 90s, Slowness II 30s, Mining Fatigue 40s, Darkness 8s | 10 minutes |

Death streak increases only when deaths happen within 10 minutes. Penalties cap at 10 max health and 6 food to avoid permanent death spirals.

## Recovery

Player recovers when either condition happens:

- Sleeps successfully while sleep recovery is not locked.
- Survives 20 minutes while online.

Repeated intentional deaths lock sleep recovery:

- Streak 4-5: sleep recovery locked for 5 minutes.
- Streak 6+: sleep recovery locked for 10 minutes.

Survival recovery still works during lock. This prevents `die -> sleep -> reset -> die` abuse without trapping players forever.

Recovery restores the max-health base value captured before the active penalty started and clears penalty effects.

## Anti-Exploit Guarantees

- Respawn cannot refill player to full normal health/food.
- Logout/relog keeps active penalty.
- Server restart keeps active penalty, death streak, and recovery lock through world `PersistentState`.
- Multiplayer state is keyed by UUID, so players do not affect each other.
- Creative and spectator players are ignored for admin/testing safety.
- Tough As Nails/thirst is intentionally not used to avoid optional API crash risk on 1.21.8.

## Compatibility Note

The public mod name and jar basename are now `Respawn Penalty`. Internal mod id remains `hoaug_death_penalty` for save compatibility with existing worlds.

## Build

```powershell
gradle clean build
```

Jar output:

```text
build/libs/respawn-penalty-1.0.0.jar
```

## Install

1. Install Fabric Loader for Minecraft 1.21.8.
2. Install Fabric API.
3. Copy `respawn-penalty-1.0.0.jar` into server `mods/`.
4. Restart server.

## Tuning

Edit constants in:

```text
src/main/java/com/hoaug/deathpenalty/DeathPenaltyConfig.java
```

Important constants:

- `STREAK_WINDOW_TICKS`
- `RECOVERY_TICKS`
- `MODERATE_RECOVERY_LOCK_TICKS`
- `CRITICAL_RECOVERY_LOCK_TICKS`
- `IGNORE_CREATIVE_AND_SPECTATOR`
- `tierForStreak(...)`
