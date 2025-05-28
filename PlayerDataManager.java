package com.englearn;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(Englearning.MOD_ID);
    private static MinecraftServer server;
    private static final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public static void initialize(MinecraftServer serverInstance) {
        server = serverInstance;
        loadAllPlayerData();
        LOGGER.info("PlayerDataManager initialized with server instance.");
    }

    private static void loadAllPlayerData() {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world != null) {
            for (UUID uuid : playerDataMap.keySet()) {
                PlayerData data = world.getPersistentStateManager()
                        .getOrCreate(
                                nbt -> PlayerData.fromNbt(nbt),
                                () -> new PlayerData(uuid),
                                "englearning_player_" + uuid.toString()
                        );
                playerDataMap.put(uuid, data);
                LOGGER.info("Loaded PlayerData for player {}", uuid);
            }
        }
    }

    public static void saveAllPlayerData() {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world != null) {
            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
                UUID uuid = entry.getKey();
                PlayerData data = entry.getValue();
                world.getPersistentStateManager().set("englearning_player_" + uuid.toString(), data);
                LOGGER.info("Saved PlayerData for player {}", uuid);
            }
            world.getPersistentStateManager().save();
            LOGGER.info("Saved data for {} players.", playerDataMap.size());
        }
    }

    public static PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, uuid -> {
            ServerWorld world = server.getWorld(World.OVERWORLD);
            PlayerData data = world.getPersistentStateManager()
                    .getOrCreate(
                            nbt -> PlayerData.fromNbt(nbt),
                            () -> new PlayerData(uuid),
                            "englearning_player_" + uuid.toString()
                    );
            LOGGER.info("Created or loaded PlayerData for {}", uuid);
            return data;
        });
    }

    public static List<Word> getWrongWords(UUID playerId) {
        return getPlayerData(playerId).getWrongWords();
    }

    public static void addWrongWord(UUID playerId, Word word) {
        getPlayerData(playerId).addWrongWord(word);
    }

    public static void removeWrongWord(UUID playerId, Word word) {
        getPlayerData(playerId).removeWrongWord(word);
    }

    public static int getPlayerReviewInterval(UUID playerId) {
        return getPlayerData(playerId).getReviewInterval();
    }

    public static void setPlayerReviewInterval(UUID playerId, int interval) {
        getPlayerData(playerId).setReviewInterval(interval);
    }

    public static int getConsecutiveCorrects(UUID playerId) {
        return getPlayerData(playerId).getConsecutiveCorrects();
    }

    public static void setConsecutiveCorrects(UUID playerId, int count) {
        getPlayerData(playerId).setConsecutiveCorrects(count);
    }

    public static int getDamageStreak(UUID playerId) {
        return getPlayerData(playerId).getDamageStreak();
    }

    public static void setDamageStreak(UUID playerId, int count) {
        getPlayerData(playerId).setDamageStreak(count);
    }

    public static int getConsecutiveWrongs(UUID playerId) {
        return getPlayerData(playerId).getConsecutiveWrongs();
    }

    public static void setConsecutiveWrongs(UUID playerId, int count) {
        getPlayerData(playerId).setConsecutiveWrongs(count);
    }

    public static Set<Integer> getRewardMilestones(UUID playerId) {
        return getPlayerData(playerId).getRewardMilestones();
    }

    public static void setRewardMilestones(UUID playerId, Set<Integer> milestones) {
        getPlayerData(playerId).setRewardMilestones(milestones);
    }

    public static String getCurrentWordList(UUID playerId) {
        return getPlayerData(playerId).getCurrentWordList();
    }

    public static void setCurrentWordList(UUID playerId, String wordList) {
        getPlayerData(playerId).setCurrentWordList(wordList);
    }
}