package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FactionAdminCommand {

    public static Set<UUID> opState = new HashSet<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faction_admin")
                .requires(source -> source.hasPermission(2))
                .then(get())
                .then(level())
                .then(delete())
                .then(op())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("get")
                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            int id = IntegerArgumentType.getInteger(context, "id");

                            var faction = BeyonderData.factionStorage.getFaction(id);
                            if (faction == null) {
                                source.sendFailure(Component.literal("Invalid id!"));
                                return 0;
                            }

                            source.sendSystemMessage(Component.literal(faction.getAllInfo() + "\n"));

                            return 0;
                        }))
                .then(Commands.literal("all")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            source.sendSystemMessage(Component.literal(BeyonderData.factionStorage.getAllShortInfo() + "\n"));

                            return 1;
                        }))
                ;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> level() {
        return Commands.literal("level")
                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, FactionCore.getMaxLevel()))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    int id = IntegerArgumentType.getInteger(context, "id");
                                    int level = IntegerArgumentType.getInteger(context, "level");

                                    var faction = BeyonderData.factionStorage.getFaction(id);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("Invalid id!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.setLevel(id, level);

                                    source.sendSystemMessage(Component.literal("Level of \"" + faction.getName() + "\" was set to " + level + "\n").withStyle(ChatFormatting.GREEN));
                                    return 0;
                                }))
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> delete() {
        return Commands.literal("delete")
                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            int id = IntegerArgumentType.getInteger(context, "id");

                            var faction = BeyonderData.factionStorage.getFaction(id);
                            if (faction == null) {
                                source.sendFailure(Component.literal("Invalid id!"));
                                return 0;
                            }

                            BeyonderData.factionStorage.disband(id);

                            source.sendSystemMessage(Component.literal("Deleted \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                            return 0;
                        })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> op() {
        return Commands.literal("op")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayer();
                    if(player == null){
                        source.sendFailure(Component.literal("Must be player!"));
                        return 0;
                    }

                    boolean isOp = opState.contains(player.getUUID());

                    if(isOp){
                        opState.remove(player.getUUID());
                    }
                    else {
                        opState.add(player.getUUID());
                    }

                    source.sendSystemMessage(Component.literal("Set OP state to " + isOp + "\n")
                            .withStyle(ChatFormatting.GREEN));
                    return 0;
                });
    }

}
