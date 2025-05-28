package com.englearn;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Word {
    @SerializedName("word")
    private String word;

    @SerializedName("translations")
    private List<Translation> translations;

    @SerializedName("phrases")
    private List<Phrase> phrases;

    public Word() {}

    public Word(String word, List<Translation> translations, List<Phrase> phrases) {
        this.word = word;
        this.translations = translations;
        this.phrases = phrases;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    public List<Phrase> getPhrases() {
        return phrases;
    }

    public void setPhrases(List<Phrase> phrases) {
        this.phrases = phrases;
    }

    public String getPrimaryTranslation() {
        if (translations != null && !translations.isEmpty()) {
            return translations.get(0).getTranslation();
        }
        return "";
    }

    public String getFullTranslation() {
        if (translations != null && !translations.isEmpty()) {
            Translation trans = translations.get(0);
            String result = trans.getTranslation();
            if (trans.getType() != null && !trans.getType().isEmpty()) {
                result += " (" + trans.getType() + ")";
            }
            return result;
        }
        return "";
    }

    public String getAllTranslations() {
        if (translations == null || translations.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < translations.size(); i++) {
            if (i > 0) sb.append("；");
            Translation trans = translations.get(i);
            sb.append(trans.getTranslation());
            if (trans.getType() != null && !trans.getType().isEmpty()) {
                sb.append(" (").append(trans.getType()).append(")");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Word{" +
                "word='" + word + '\'' +
                ", translations=" + translations +
                ", phrases=" + phrases +
                '}';
    }

    public static class Translation {
        @SerializedName("translation")
        private String translation;

        @SerializedName("type")
        private String type;

        public Translation() {}

        public Translation(String translation, String type) {
            this.translation = translation;
            this.type = type;
        }

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Translation{" +
                    "translation='" + translation + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public static class Phrase {
        @SerializedName("phrase")
        private String phrase;

        @SerializedName("translation")
        private String translation;

        public Phrase() {}

        public Phrase(String phrase, String translation) {
            this.phrase = phrase;
            this.translation = translation;
        }

        public String getPhrase() {
            return phrase;
        }

        public void setPhrase(String phrase) {
            this.phrase = phrase;
        }

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        @Override
        public String toString() {
            return "Phrase{" +
                    "phrase='" + phrase + '\'' +
                    ", translation='" + translation + '\'' +
                    '}';
        }
    }
}