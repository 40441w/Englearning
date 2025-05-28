package com.englearn;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordDatabase {
    private static final List<Word> words = new ArrayList<>();
    private static final Random random = Random.create();
    private static String nextWord = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(Englearning.MOD_ID);

    static {
        // 加载 JSON 文件
        try {
            Reader reader = new InputStreamReader(
                    WordDatabase.class.getResourceAsStream("/data/englearn/words.json"),
                    StandardCharsets.UTF_8
            );
            if (reader == null) {
                LOGGER.error("words.json not found in resources");
            } else {
                Gson gson = new Gson();
                List<Word> loadedWords = gson.fromJson(reader, new TypeToken<List<Word>>(){}.getType());
                if (loadedWords != null) {
                    words.addAll(loadedWords);
                    LOGGER.info("Loaded {} words from words.json", loadedWords.size());
                } else {
                    LOGGER.warn("No words loaded from words.json, file may be empty");
                }
                reader.close();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load words.json", e);
        }

        // 添加默认单词（备用）
        if (words.isEmpty()) {
            LOGGER.warn("No words loaded, initializing with default words");
            List<Word.Translation> appleTranslations = new ArrayList<>();
            appleTranslations.add(new Word.Translation("苹果", "n."));
            List<Word.Phrase> applePhrases = new ArrayList<>();
            applePhrases.add(new Word.Phrase("an apple a day", "一天一苹果"));
            words.add(new Word("Apple", appleTranslations, applePhrases));

            List<Word.Translation> bookTranslations = new ArrayList<>();
            bookTranslations.add(new Word.Translation("书", "n."));
            List<Word.Phrase> bookPhrases = new ArrayList<>();
            bookPhrases.add(new Word.Phrase("read a book", "读一本书"));
            words.add(new Word("Book", bookTranslations, bookPhrases));
        }
    }

    public static Word getNextWord() {
        if (nextWord != null) {
            Word entry = getCurrentWord(nextWord);
            LOGGER.info("Using forced next word: {}", nextWord);
            nextWord = null;
            return entry != null ? entry : words.get(random.nextInt(words.size()));
        }
        if (words.isEmpty()) {
            LOGGER.error("Word list is empty");
            return null;
        }
        Word word = words.get(random.nextInt(words.size()));
        LOGGER.info("Selected random word: {}", word.getWord());
        return word;
    }

    public static void setNextWord(String word) {
        nextWord = word;
        LOGGER.info("Set next word to: {}", word);
    }

    public static Word getCurrentWord(String word) {
        for (Word entry : words) {
            if (entry.getWord().equals(word)) {
                LOGGER.info("Found current word: {}", word);
                return entry;
            }
        }
        LOGGER.warn("Current word not found: {}", word);
        return null;
    }

    public static String[] generateOptions(Word word) {
        if (word == null) {
            LOGGER.error("Cannot generate options for null word");
            return new String[0];
        }
        List<String> options = new ArrayList<>();
        options.add(word.getPrimaryTranslation()); // 正确答案
        // 添加随机错误选项
        while (options.size() < 4) {
            Word randomWord = words.get(random.nextInt(words.size()));
            String randomTranslation = randomWord.getPrimaryTranslation();
            if (!options.contains(randomTranslation) && !randomWord.getWord().equals(word.getWord())) {
                options.add(randomTranslation);
            }
        }
        // 打乱选项
        for (int i = options.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = options.get(i);
            options.set(i, options.get(j));
            options.set(j, temp);
        }
        LOGGER.info("Generated options for word {}: {}", word.getWord(), String.join(", ", options));
        return options.toArray(new String[0]);
    }

    // 新增方法：通过单词字符串查找 Word 对象
    public static Word getWordByString(String wordStr) {
        for (Word word : words) {
            if (word.getWord().equalsIgnoreCase(wordStr)) {
                LOGGER.info("Found word in database: {}", wordStr);
                return word;
            }
        }
        LOGGER.warn("Word not found in database: {}", wordStr);
        return null;
    }
}