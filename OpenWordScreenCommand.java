package com.englearn;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class OpenWordScreenCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("learnword")
                    .requires(source -> source.hasPermissionLevel(0))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        Word entry = WordDatabase.getNextWord();
                        String[] options = WordDatabase.generateOptions(entry);
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeString(entry.getWord());
                        buf.writeString(entry.getAllTranslations());
                        buf.writeInt(options.length);
                        for (String option : options) {
                            buf.writeString(option);
                        }
                        buf.writeInt(0); // 假设正确答案索引为0
                        ServerPlayNetworking.send(player, Englearning.WORD_SELECTION_PACKET, buf);
                        return 1;
                    }));
        });
    }
}