package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class FactionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faction")
                .then(create())
                .then(get())
                .then(claim())
                .then(here())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("create")
                .then(Commands.literal("nation")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var name = StringArgumentType.getString(context, "name");

                            if (!BeyonderData.factionStorage.isNameUnique(name)) {
                                source.sendFailure(Component.literal("Name is already taken!"));
                                return 0;
                            }

                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(Component.literal("Player must use this command!"));
                                return 0;
                            }

                            if(BeyonderData.factionStorage.hasAnyByType(player.getName().getString(), 1)){
                                source.sendFailure(Component.literal("You are already part of such type of faction!"));
                                return 0;
                            }

                            BeyonderData.factionStorage.createFaction(player.getName().getString(), name, 1);

                            return 1;
                        })))
                .then(Commands.literal("church")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var name = StringArgumentType.getString(context, "name");

                                    if (!BeyonderData.factionStorage.isNameUnique(name)) {
                                        source.sendFailure(Component.literal("Name is already taken!"));
                                        return 0;
                                    }

                                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                                        source.sendFailure(Component.literal("Player must use this command!"));
                                        return 0;
                                    }

                                    if(BeyonderData.factionStorage.hasAnyByType(player.getName().getString(), 2)){
                                        source.sendFailure(Component.literal("You are already part of such type of faction!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.createFaction(player.getName().getString(), name, 2);

                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("get")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("Must be a player!"));
                        return 0;
                    }

                    var result = BeyonderData.factionStorage.getPartOfFaction(player.getName().getString());

                    if (result.isEmpty()) {
                        source.sendFailure(Component.literal("You are not in any faction!"));
                        return 0;
                    }

                    StringBuilder builder = new StringBuilder("You are part of this factions:\n");

                    for (var faction : result) {
                        builder.append(faction.getAllInfo());
                    }

                    source.sendSystemMessage(Component.literal(
                            builder.toString() + "\n"));

                    return 1;
                })
                .then(Commands.literal("all")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();

                                    source.sendSystemMessage(Component.literal(BeyonderData.factionStorage.getAllShortInfo() + "\n"));

                                    return 1;
                                }
                        ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> claim() {
        return Commands.literal("claim")
                .then(Commands.literal("nation")
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 9))
                .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var player = source.getPlayer();
                            if(player == null){
                                source.sendFailure(Component.literal("Must be a player!"));
                                return 0;
                            }

                            int level = IntegerArgumentType.getInteger(context, "level");
                            var pos = source.getLevel().getChunk(player.blockPosition()).getPos();

                            if(BeyonderData.factionStorage.isClaimedType(pos, 1)){
                                source.sendFailure(Component.literal("Chunk is already claimed by another nation!"));
                                return 0;
                            }

                            String name = player.getName().getString();
                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                            if(faction == null){
                                source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                return 0;
                            }

                            int playerLevel = faction.getPlayerLevel(name);

                            if(playerLevel == 1 || playerLevel < level){
                                source.sendFailure(Component.literal("You don`t have permission to claim this chunk!"));
                                return 0;
                            }

                            BeyonderData.factionStorage.claim(faction.getId(), pos, level);

                            source.sendSystemMessage(Component.literal("Successfully claimed chunk at " + pos.toString() + " with level " + level + '\n').withStyle(ChatFormatting.GREEN));

                            return 1;
                        }
                )));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> here() {
        return Commands.literal("here")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("Must be a player!"));
                        return 0;
                    }

                    var pos = source.getLevel().getChunk(player.blockPosition()).getPos();
                    var result = BeyonderData.factionStorage.getFaction(pos);

                    if (result.isEmpty()) {
                        source.sendFailure(Component.literal("Not claimed area!"));
                        return 1;
                    }

                    StringBuilder builder = new StringBuilder("Claimed by:\n");

                    for (var faction : result) {
                        builder.append(faction.getShortInfo());
                    }

                    source.sendSystemMessage(Component.literal(
                            builder.toString() + "\n"));

                    return 1;
                });
    }
}
