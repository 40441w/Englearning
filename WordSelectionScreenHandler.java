package com.englearn;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class WordSelectionScreenHandler extends ScreenHandler {
    // 注册 ScreenHandlerType，使用简化后的构造函数
    public static final ScreenHandlerType<WordSelectionScreenHandler> TYPE = Registry.register(
            Registry.SCREEN_HANDLER,
            new Identifier("englearning", "word_selection_handler"), // 最好使用一个更明确的名称
            new ScreenHandlerType<>((syncId, inventory) -> new WordSelectionScreenHandler(syncId, inventory))
    );

    // 简化构造函数，仅用于ScreenHandler的基础功能
    public WordSelectionScreenHandler(int syncId, PlayerInventory inventory) {
        super(TYPE, syncId);
        // 如果您的UI需要展示玩家物品，可以在这里添加物品槽位，例如：
        // for (int i = 0; i < 3; i++) {
        //     for (int j = 0; j < 9; j++) {
        //         this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        //     }
        // }
        // for (int i = 0; i < 9; i++) {
        //     this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        // }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        // 如果没有物品槽位，直接返回空 ItemStack
        return ItemStack.EMPTY;
    }

    // Factory 类可以被简化，因为它现在只需要创建一个基础的 WordSelectionScreenHandler 实例
    public static class Factory implements net.minecraft.screen.NamedScreenHandlerFactory {
        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
            // 返回一个简单的 WordSelectionScreenHandler 实例
            return new WordSelectionScreenHandler(syncId, inventory);
        }

        @Override
        public Text getDisplayName() {
            return Text.of("Choose the Correct Word");
        }
    }
}