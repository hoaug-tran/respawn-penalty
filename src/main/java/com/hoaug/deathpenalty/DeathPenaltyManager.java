package com.hoaug.deathpenalty;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class DeathPenaltyManager {
    private DeathPenaltyManager() {
    }

    public static void onRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (alive || shouldIgnore(newPlayer)) {
            reapplyActivePenalty(newPlayer);
            return;
        }

        DeathPenaltyState state = readState(newPlayer);
        long now = newPlayer.getWorld().getTime();
        boolean recentDeath = state.lastDeathGameTime > 0L
                && now - state.lastDeathGameTime <= DeathPenaltyConfig.STREAK_WINDOW_TICKS;

        if (!state.penaltyActive) {
            state.originalMaxHealth = safeMaxHealth(getCurrentMaxHealth(newPlayer));
        }

        state.deathStreak = recentDeath ? Math.max(1, state.deathStreak + 1) : 1;
        state.penaltyActive = true;
        state.aliveTicks = 0;
        state.recoveryLockTicks = Math.max(state.recoveryLockTicks,
                DeathPenaltyConfig.recoveryLockForStreak(state.deathStreak));
        state.lastDeathGameTime = now;

        writeState(newPlayer, state);
        applyPenalty(newPlayer, state, true);
    }

    public static void onJoin(ServerPlayerEntity player) {
        reapplyActivePenalty(player);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (shouldIgnore(player)) {
                continue;
            }

            DeathPenaltyState state = readState(player);
            if (!state.penaltyActive) {
                continue;
            }

            state.aliveTicks++;
            if (state.recoveryLockTicks > 0) {
                state.recoveryLockTicks--;
            }

            if (state.aliveTicks >= DeathPenaltyConfig.RECOVERY_TICKS) {
                recover(player, state, "Bạn đã hồi phục sau death penalty.");
                continue;
            }

            writeState(player, state);
        }
    }

    public static void onSleep(ServerPlayerEntity player) {
        if (shouldIgnore(player)) {
            return;
        }

        DeathPenaltyState state = readState(player);
        if (!state.penaltyActive) {
            return;
        }

        if (state.recoveryLockTicks > 0) {
            player.sendMessage(Text.literal("You have died too many times. Sleep recovery is now locked. "
                    + formatTicks(state.recoveryLockTicks) + ".")
                    .formatted(Formatting.RED), true);
            return;
        }
        recover(player, state, "You have recovered after sleeping.");
    }

    private static void reapplyActivePenalty(ServerPlayerEntity player) {
        if (shouldIgnore(player)) {
            return;
        }

        DeathPenaltyState state = readState(player);
        if (state.penaltyActive) {
            applyPenalty(player, state, false);
        }
    }

    private static void applyPenalty(ServerPlayerEntity player, DeathPenaltyState state, boolean showMessage) {
        DeathPenaltyConfig.PenaltyTier tier = DeathPenaltyConfig.tierForStreak(state.deathStreak);

        setMaxHealth(player, tier.maxHealth());
        player.setHealth(Math.min(player.getHealth(), (float) tier.maxHealth()));
        if (player.getHealth() <= 0.0F) {
            player.setHealth(1.0F);
        }

        player.getHungerManager().setFoodLevel(tier.foodLevel());
        player.getHungerManager().setSaturationLevel(0.0F);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, tier.weaknessTicks(),
                tier.weaknessAmplifier(), false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, tier.slownessTicks(),
                tier.slownessAmplifier(), false, false, true));
        if (tier.hasMiningFatigue()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, tier.miningFatigueTicks(),
                    tier.miningFatigueAmplifier(), false, false, true));
        }
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, tier.darknessTicks(),
                tier.darknessAmplifier(), false, false, true));

        if (showMessage) {
            Text message = Text.literal(penaltyMessage(state))
                    .formatted(Formatting.DARK_RED, Formatting.BOLD);
            player.sendMessage(message, true);
        }
    }

    private static void recover(ServerPlayerEntity player, DeathPenaltyState state, String message) {
        double restoredMaxHealth = safeMaxHealth(state.originalMaxHealth);
        setMaxHealth(player, restoredMaxHealth);
        player.setHealth((float) Math.min(restoredMaxHealth, Math.max(player.getHealth(), 1.0F)));
        clearPenaltyEffects(player);

        state.penaltyActive = false;
        state.aliveTicks = 0;
        state.recoveryLockTicks = 0;
        state.deathStreak = 0;
        state.originalMaxHealth = restoredMaxHealth;
        writeState(player, state);

        player.sendMessage(Text.literal(message).formatted(Formatting.GREEN), true);
    }

    private static void clearPenaltyEffects(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.WEAKNESS);
        player.removeStatusEffect(StatusEffects.SLOWNESS);
        player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        player.removeStatusEffect(StatusEffects.DARKNESS);
    }

    private static boolean shouldIgnore(ServerPlayerEntity player) {
        return DeathPenaltyConfig.IGNORE_CREATIVE_AND_SPECTATOR
                && (player.isCreative() || player.isSpectator());
    }

    private static void setMaxHealth(ServerPlayerEntity player, double value) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(value);
        }
    }

    private static double getCurrentMaxHealth(ServerPlayerEntity player) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        return attribute == null ? 20.0D : attribute.getBaseValue();
    }

    private static String penaltyMessage(DeathPenaltyState state) {
        String message = "You are weakened after dying. Death streak: " + state.deathStreak;
        if (state.recoveryLockTicks > 0) {
            message += " | Sleep recovery locked: " + formatTicks(state.recoveryLockTicks);
        }
        return message;
    }

    private static String formatTicks(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes <= 0) {
            return seconds + "s";
        }
        return minutes + "m " + seconds + "s";
    }

    private static double safeMaxHealth(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 1.0D) {
            return 20.0D;
        }
        return value;
    }

    private static DeathPenaltyState readState(ServerPlayerEntity player) {
        return DeathPenaltyStateStore.get(player.getServer(), player.getUuid());
    }

    private static void writeState(ServerPlayerEntity player, DeathPenaltyState state) {
        DeathPenaltyStateStore.put(player.getServer(), player.getUuid(), state);
    }
}
