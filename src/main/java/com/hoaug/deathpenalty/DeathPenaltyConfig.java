package com.hoaug.deathpenalty;

public final class DeathPenaltyConfig {
    public static final boolean IGNORE_CREATIVE_AND_SPECTATOR = true;

    public static final int STREAK_WINDOW_TICKS = 20 * 60 * 10;
    public static final int RECOVERY_TICKS = 20 * 60 * 20;
    public static final int MODERATE_RECOVERY_LOCK_TICKS = 20 * 60 * 5;
    public static final int CRITICAL_RECOVERY_LOCK_TICKS = 20 * 60 * 10;

    private DeathPenaltyConfig() {
    }

    public static int recoveryLockForStreak(int deathStreak) {
        if (deathStreak >= 6) {
            return CRITICAL_RECOVERY_LOCK_TICKS;
        }
        if (deathStreak >= 4) {
            return MODERATE_RECOVERY_LOCK_TICKS;
        }
        return 0;
    }

    public static PenaltyTier tierForStreak(int deathStreak) {
        if (deathStreak <= 1) {
            return new PenaltyTier(15.0D, 9, 20 * 30, 0, 20 * 10, 0, 0, -1, 20 * 3, 0);
        }
        if (deathStreak == 2) {
            return new PenaltyTier(14.0D, 8, 20 * 45, 0, 20 * 15, 0, 0, -1, 20 * 4, 0);
        }
        if (deathStreak == 3) {
            return new PenaltyTier(12.0D, 7, 20 * 60, 1, 20 * 20, 0, 20 * 20, 0, 20 * 5, 0);
        }
        if (deathStreak <= 5) {
            return new PenaltyTier(10.0D, 6, 20 * 75, 1, 20 * 25, 0, 20 * 30, 0, 20 * 6, 0);
        }
        return new PenaltyTier(10.0D, 6, 20 * 90, 1, 20 * 30, 1, 20 * 40, 0, 20 * 8, 0);
    }

    public record PenaltyTier(
            double maxHealth,
            int foodLevel,
            int weaknessTicks,
            int weaknessAmplifier,
            int slownessTicks,
            int slownessAmplifier,
            int miningFatigueTicks,
            int miningFatigueAmplifier,
            int darknessTicks,
            int darknessAmplifier
    ) {
        public boolean hasMiningFatigue() {
            return miningFatigueTicks > 0;
        }
    }
}
