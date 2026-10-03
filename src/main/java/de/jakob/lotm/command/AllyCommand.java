package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.authlib.GameProfile;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AllyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ally")
                .requires(source -> source.getPlayer() != null)
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                    Entity target = EntityArgument.getEntity(context, "target");
                                    return executeRemove(context.getSource(), sender, target.getUUID(), target.getDisplayName());
                                })
                        )
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                    Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "player");
                                    GameProfile profile = profiles.iterator().next();
                                    return executeRemove(context.getSource(), sender, profile.getId(), Component.literal(profile.getName()));
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            return executeList(context.getSource(), sender);
                        })
                )
        );
    }

    private static int executeList(CommandSourceStack source, ServerPlayer sender) {
        AllyComponent comp = sender.getData(ModAttachments.ALLY_COMPONENT.get());

        if (!comp.hasAllies()) {
            source.sendSuccess(() -> Component.translatable("lotm.ally.list_empty").withColor(0x9E9E9E), false);
            return 1;
        }

        List<Component> playerEntries = new java.util.ArrayList<>();
        for (AllyComponent.AllyInfo allyInfo : comp.allies()) {
            ServerPlayer online = source.getServer().getPlayerList().getPlayer(allyInfo.uuid());
            if (online != null) {
                playerEntries.add(online.getName().copy().withColor(0x4CAF50));
            } else {
                String name = (allyInfo.playerName() != null && !allyInfo.playerName().isBlank())
                        ? allyInfo.playerName()
                        : "Unknown";
                playerEntries.add(Component.literal(name).withColor(0x9E9E9E));
            }
        }

        if (playerEntries.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("lotm.ally.list_empty").withColor(0x9E9E9E), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("lotm.ally.list_header", playerEntries.size()).withColor(0x2196F3), false);
        for (Component entry : playerEntries) {
            source.sendSuccess(() -> Component.literal("  - ").append(entry), false);
        }

        return 1;
    }

    private static int executeRemove(CommandSourceStack source, ServerPlayer sender, UUID targetUuid, Component targetName) {
        if (sender.getUUID().equals(targetUuid)) {
            source.sendFailure(Component.translatable("lotm.ally.remove_self"));
            return 0;
        }

        if (!AllyUtil.isAlly(sender, targetUuid)) {
            source.sendFailure(Component.translatable("lotm.ally.not_allies", targetName));
            return 0;
        }

        ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(targetUuid);
        if (onlineTarget != null) {
            AllyUtil.removeAllies(sender, onlineTarget);
        } else {
            AllyUtil.removeAllyOneWay(sender, targetUuid);
            sender.sendSystemMessage(Component.translatable("lotm.ally.removed",
                    targetName).withColor(0xFF9800));
        }

        return 1;
    }
}
