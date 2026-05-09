package com.hoaug.deathpenalty;

import net.minecraft.nbt.NbtCompound;

public final class DeathPenaltyState {
    public int deathStreak;
    public boolean penaltyActive;
    public int aliveTicks;
    public int recoveryLockTicks;
    public long lastDeathGameTime;
    public double originalMaxHealth = 20.0D;

    public static DeathPenaltyState fromNbt(NbtCompound tag) {
        DeathPenaltyState state = new DeathPenaltyState();
        state.deathStreak = tag.getInt("DeathStreak", 0);
        state.penaltyActive = tag.getBoolean("PenaltyActive", false);
        state.aliveTicks = tag.getInt("AliveTicks", 0);
        state.recoveryLockTicks = tag.getInt("RecoveryLockTicks", 0);
        state.lastDeathGameTime = tag.getLong("LastDeathGameTime", 0L);
        state.originalMaxHealth = tag.getDouble("OriginalMaxHealth", 20.0D);
        return state;
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("DeathStreak", deathStreak);
        tag.putBoolean("PenaltyActive", penaltyActive);
        tag.putInt("AliveTicks", aliveTicks);
        tag.putInt("RecoveryLockTicks", recoveryLockTicks);
        tag.putLong("LastDeathGameTime", lastDeathGameTime);
        tag.putDouble("OriginalMaxHealth", originalMaxHealth);
        return tag;
    }
}
