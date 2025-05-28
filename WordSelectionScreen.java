package com.englearn; // 请再次确认您的WordSelectionScreen是否真的在这个包下，如果是在com.englearn.client.gui等子包，请修改此处

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class WordSelectionScreen extends Screen {
    private final String word;
    private final String definition;
    private final String[] options;
    private final int correctIndex;
    private final boolean isReviewWord;

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 25;

    public WordSelectionScreen(String word, String definition, String[] options, int correctIndex, boolean isReviewWord) {
        super(Text.literal("选择翻译"));
        this.word = word;
        this.definition = definition != null ? definition : "";
        this.options = options;
        this.correctIndex = correctIndex;
        this.isReviewWord = isReviewWord;
        System.out.println("WordSelectionScreen initialized with word: " + word + ", Review: " + isReviewWord);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int optionsStartY = this.height / 2 + (isReviewWord ? 30 : 10);

        for (int i = 0; i < options.length; i++) {
            final int selectedIndex = i;
            ButtonWidget button = new ButtonWidget(
                    centerX - BUTTON_WIDTH / 2,
                    optionsStartY + i * BUTTON_SPACING,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    Text.literal(options[i]),
                    btn -> selectOption(selectedIndex)
            );
            this.addDrawableChild(button);
        }

        // 修改这里：将取消按钮移动到屏幕左上角
        ButtonWidget cancelButton = new ButtonWidget(
                10, // X 坐标，距离左边缘 10 像素
                10, // Y 坐标，距离上边缘 10 像素
                80, // 宽度，可以根据需要调整
                20, // 高度
                Text.literal("取消"), // 简化按钮文本，因为 ESC 提示已经很明确
                btn -> sendCancelPacketAndClose()
        );
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // 渲染单词
        Text wordText = Text.literal(word).formatted(Formatting.GOLD, Formatting.BOLD);
        int wordWidth = this.textRenderer.getWidth(wordText);
        this.textRenderer.draw(matrices, wordText, (this.width - wordWidth) / 2f, 50, 0xFFFFFF);

        // 渲染复习单词提示
        if (isReviewWord) {
            Text reviewHintText = Text.literal("§c(曾错过的单词！)");
            int reviewHintWidth = this.textRenderer.getWidth(reviewHintText);
            this.textRenderer.draw(matrices, reviewHintText, (this.width - reviewHintWidth) / 2f, 75, 0xFF0000);
        }

        // 渲染通用提示文本
        int hintTextY = isReviewWord ? 115 : 95;
        Text hintText = Text.literal("选择 '" + word + "' 的正确中文翻译：").formatted(Formatting.WHITE);
        int hintWidth = this.textRenderer.getWidth(hintText);
        this.textRenderer.draw(matrices, hintText, (this.width - hintWidth) / 2f, hintTextY, 0xFFFFFF);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void selectOption(int selectedIndex) {
        System.out.println("Selected option index: " + selectedIndex);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(selectedIndex);
        ClientPlayNetworking.send(Englearning.WORD_ANSWER_PACKET, buf);

        this.close();
    }

    private void sendCancelPacketAndClose() {
        ClientPlayNetworking.send(Englearning.CANCEL_CHALLENGE_PACKET, PacketByteBufs.empty());
        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            sendCancelPacketAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}