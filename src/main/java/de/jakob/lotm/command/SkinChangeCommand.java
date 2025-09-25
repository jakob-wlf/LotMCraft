package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jakob.lotm.util.helper.skin.SkinManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SkinChangeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("changeskin")
                .then(Commands.argument("target", StringArgumentType.word())
                    .then(Commands.argument("newskin", StringArgumentType.word())
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "target");
                            String newSkin = StringArgumentType.getString(context, "newskin");
                            
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                SkinManager.changeSkin(player, target, newSkin);
                            }
                            
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("restore")
                    .then(Commands.argument("target", StringArgumentType.word())
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "target");
                            ServerPlayer targetPlayer = context.getSource().getServer()
                                .getPlayerList().getPlayerByName(target);
                            
                            if (targetPlayer != null) {
                                SkinManager.restoreOriginalSkin(targetPlayer);
                            } else {
                                context.getSource().sendFailure(Component.literal("Player not found: " + target));
                            }
                            
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("status")
                    .executes(context -> {
                        if (context.getSource().getEntity() instanceof ServerPlayer player) {
                            if (SkinManager.hasSkinOverride(player)) {
                                String override = SkinManager.getCurrentSkinOverride(player);
                                context.getSource().sendSuccess(() -> 
                                    Component.literal("Current skin override: " + override), false);
                            } else {
                                context.getSource().sendSuccess(() -> 
                                    Component.literal("No active skin override"), false);
                            }
                        }
                        return 1;
                    })
                )
        );
    }
}