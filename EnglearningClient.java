package com.englearn;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class EnglearningClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerClientNetworking();
    }

    private void registerClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(Englearning.WORD_SELECTION_PACKET, (client, handler, buf, responseSender) -> {
            String word = buf.readString();
            String definition = buf.readString();
            int optionCount = buf.readInt();
            String[] options = new String[optionCount];
            for (int i = 0; i < optionCount; i++) {
                options[i] = buf.readString();
            }
            int correctIndex = buf.readInt();
            boolean isReviewWord = buf.readBoolean(); // 读取是否是复习单词的信息

            client.execute(() -> {
                // 将 isReviewWord 传递给 WordSelectionScreen
                WordSelectionScreen screen = new WordSelectionScreen(word, definition, options, correctIndex, isReviewWord);
                client.setScreen(screen);
            });
        });
    }
}