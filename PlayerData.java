package com.englearn;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.PersistentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

public class PlayerData extends PersistentState {
    private static final Logger LOGGER = LoggerFactory.getLogger(Englearning.MOD_ID);
    private final UUID playerUuid;
    private final List<Word> wrongWords;
    private int reviewInterval;
    private int consecutiveCorrects;
    private int damageStreak;
    private int consecutiveWrongs;
    private Set<Integer> rewardMilestones;
    private String currentWordList; // 新增：当前词库名称

    public PlayerData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.wrongWords = new ArrayList<>();
        this.reviewInterval = Englearning.DEFAULT_REPEAT_INTERVAL;
        this.consecutiveCorrects = 0;
        this.damageStreak = 0;
        this.consecutiveWrongs = 0;
        this.rewardMilestones = new HashSet<>();
        this.currentWordList = "kaoyan"; // 默认词库
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public List<Word> getWrongWords() {
        return wrongWords;
    }

    public void addWrongWord(Word word) {
        if (!wrongWords.contains(word)) {
            this.wrongWords.add(word);
            LOGGER.info("Player {} added wrong word: {}", playerUuid, word.getWord());
            markDirty();
        }
    }

    public void removeWrongWord(Word word) {
        if (this.wrongWords.remove(word)) {
            LOGGER.info("Player {} removed wrong word: {}", playerUuid, word.getWord());
            markDirty();
        }
    }

    public int getReviewInterval() {
        return reviewInterval;
    }

    public void setReviewInterval(int reviewInterval) {
        this.reviewInterval = reviewInterval;
        markDirty();
    }

    public int getConsecutiveCorrects() {
        return consecutiveCorrects;
    }

    public void setConsecutiveCorrects(int consecutiveCorrects) {
        this.consecutiveCorrects = consecutiveCorrects;
        markDirty();
    }

    public int getDamageStreak() {
        return damageStreak;
    }

    public void setDamageStreak(int damageStreak) {
        this.damageStreak = damageStreak;
        markDirty();
    }

    public int getConsecutiveWrongs() {
        return consecutiveWrongs;
    }

    public void setConsecutiveWrongs(int consecutiveWrongs) {
        this.consecutiveWrongs = consecutiveWrongs;
        markDirty();
    }

    public Set<Integer> getRewardMilestones() {
        return rewardMilestones;
    }

    public void setRewardMilestones(Set<Integer> milestones) {
        this.rewardMilestones = milestones;
        markDirty();
    }

    public String getCurrentWordList() {
        return currentWordList;
    }

    public void setCurrentWordList(String wordList) {
        this.currentWordList = wordList;
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList wrongWordsNbt = new NbtList();
        for (Word word : wrongWords) {
            wrongWordsNbt.add(NbtString.of(word.getWord()));
        }
        nbt.put("WrongWords", wrongWordsNbt);
        nbt.putInt("ReviewInterval", reviewInterval);
        nbt.putInt("ConsecutiveCorrects", consecutiveCorrects);
        nbt.putInt("DamageStreak", damageStreak);
        nbt.putInt("ConsecutiveWrongs", consecutiveWrongs);
        nbt.putString("PlayerUuid", playerUuid.toString());
        nbt.putString("CurrentWordList", currentWordList); // 保存词库

        NbtList milestonesNbt = new NbtList();
        for (Integer milestone : rewardMilestones) {
            NbtCompound milestoneNbt = new NbtCompound();
            milestoneNbt.putInt("value", milestone);
            milestonesNbt.add(milestoneNbt);
        }
        nbt.put("RewardMilestones", milestonesNbt);

        return nbt;
    }

    public static PlayerData fromNbt(NbtCompound nbt) {
        UUID playerUuid = UUID.fromString(nbt.getString("PlayerUuid"));
        PlayerData data = new PlayerData(playerUuid);

        NbtList wrongWordsNbt = nbt.getList("WrongWords", NbtElement.STRING_TYPE);
        String wordListName = nbt.getString("CurrentWordList").isEmpty() ? "kaoyan" : nbt.getString("CurrentWordList");
        for (int i = 0; i < wrongWordsNbt.size(); i++) {
            String wordStr = wrongWordsNbt.getString(i);
            Word word = Englearning.getWordByString(wordStr, wordListName);
            if (word != null) {
                data.addWrongWord(word);
            } else {
                LOGGER.warn("Skipping unknown word: {} in word list {}", wordStr, wordListName);
            }
        }
        data.reviewInterval = nbt.getInt("ReviewInterval");
        data.consecutiveCorrects = nbt.getInt("ConsecutiveCorrects");
        data.damageStreak = nbt.getInt("DamageStreak");
        data.consecutiveWrongs = nbt.getInt("ConsecutiveWrongs");
        data.currentWordList = wordListName;

        NbtList milestonesNbt = nbt.getList("RewardMilestones", NbtElement.COMPOUND_TYPE);
        Set<Integer> milestones = new HashSet<>();
        for (int i = 0; i < milestonesNbt.size(); i++) {
            NbtCompound milestoneNbt = milestonesNbt.getCompound(i);
            milestones.add(milestoneNbt.getInt("value"));
        }
        data.setRewardMilestones(milestones);

        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return playerUuid.equals(that.playerUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUuid);
    }
}