package com.hoaug.deathpenalty;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DeathPenaltyPersistentState extends PersistentState {
    private static final String PLAYERS_KEY = "Players";

    public static final Codec<DeathPenaltyPersistentState> CODEC = NbtCompound.CODEC.xmap(
            DeathPenaltyPersistentState::fromNbt,
            DeathPenaltyPersistentState::toNbt
    );

    public static final PersistentStateType<DeathPenaltyPersistentState> TYPE = new PersistentStateType<>(
            HoaugDeathPenalty.MOD_ID,
            DeathPenaltyPersistentState::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, DeathPenaltyState> players = new HashMap<>();

    public DeathPenaltyState get(UUID uuid) {
        return players.computeIfAbsent(uuid, ignored -> new DeathPenaltyState());
    }

    public void put(UUID uuid, DeathPenaltyState state) {
        players.put(uuid, state);
        markDirty();
    }

    public void remove(UUID uuid) {
        if (players.remove(uuid) != null) {
            markDirty();
        }
    }

    private static DeathPenaltyPersistentState fromNbt(NbtCompound root) {
        DeathPenaltyPersistentState persistentState = new DeathPenaltyPersistentState();
        NbtCompound playersTag = root.getCompoundOrEmpty(PLAYERS_KEY);
        for (String key : playersTag.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                NbtElement element = playersTag.get(key);
                if (element instanceof NbtCompound playerTag) {
                    persistentState.players.put(uuid, DeathPenaltyState.fromNbt(playerTag));
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore corrupt/legacy player keys instead of failing world load.
            }
        }
        return persistentState;
    }

    private NbtCompound toNbt() {
        NbtCompound root = new NbtCompound();
        NbtCompound playersTag = new NbtCompound();
        for (Map.Entry<UUID, DeathPenaltyState> entry : players.entrySet()) {
            playersTag.put(entry.getKey().toString(), entry.getValue().toNbt());
        }
        root.put(PLAYERS_KEY, playersTag);
        return root;
    }
}
