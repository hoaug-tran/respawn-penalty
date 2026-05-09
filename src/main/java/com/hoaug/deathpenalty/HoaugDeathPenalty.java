package com.hoaug.deathpenalty;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HoaugDeathPenalty implements ModInitializer {
    public static final String MOD_ID = "hoaug_death_penalty";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerPlayerEvents.AFTER_RESPAWN.register(DeathPenaltyManager::onRespawn);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> DeathPenaltyManager.onJoin(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(DeathPenaltyManager::tick);
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                DeathPenaltyManager.onSleep(player);
            }
        });

        LOGGER.info("Hoaug Death Penalty initialized.");
    }
}
