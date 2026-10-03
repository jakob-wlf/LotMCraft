package de.jakob.lotm.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Comparator;

public class BeyonderMapCommand {

    private static LiteralArgumentBuilder<CommandSourceStack> help() {
        return Commands.literal("help")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();

                    source.sendSystemMessage(Component.literal("""
BeyonderMap is module designed to store information about players on server side
This command is for debug or server maintenance purposes only

Available commands:
1) help - will give general information about beyonderMap module and possible commands
2) all - will give list of all stored players and general information about them.
3) add <target> - will try to add target into beyonderMap
4) delete <target> (or <target_nickname>, if target is offline) - will delete target from beyonderMap
5) get <target> (or <target_nickname>) - will give all information about target
6) edit <json data> - will edit needed info
    json format - {"name":"nickname","path":"needed_path","seq":<needed_seq_as_number>}
7) delete all - will delete entire database
"""));

                    return 1;
                });
    }

    private static int showAll(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        var data = BeyonderData.playerMap.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().sequence()))
                .toList();

        if (data.isEmpty()) {
            source.sendFailure(Component.literal("No entries found."));
            return 0;
        }

        source.sendSystemMessage(Component.literal(
                        "---- Players: " + data.size() + " ----" ));

        for (var obj : data){
            source.sendSystemMessage(Component.literal(obj.getValue().getShortInfo()));
        }

        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> all() {
        return Commands.literal("all")
                .executes(BeyonderMapCommand::showAll);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> add() {
        return Commands.literal("add")
                .then(Commands.argument("target", EntityArgument.entity())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    var targetEntity = EntityArgument.getEntity(context, "target");

                    if (!(targetEntity instanceof LivingEntity livingEntity)
                            || !(BeyonderData.isBeyonder(livingEntity))) {
                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                        return 0;
                    }

                    BeyonderData.playerMap.put(livingEntity);

                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> delete() {
        return Commands.literal("delete")
                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var targets = GameProfileArgument.getGameProfiles(context, "targets");

                            for(var obj : targets){
                                BeyonderData.playerMap.remove(obj.getId());
                            }

                    return 1;
                }))
                .then(Commands.literal("all")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            if(BeyonderData.playerMap.isEmpty()) {
                                source.sendFailure(Component.literal("BeyonderMap must contain any target!"));
                                return 0;
                            }

                            BeyonderData.playerMap.clear();

                            return 1;
                        })
                )
                ;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("get")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("Must be a player!"));
                        return 0;
                    }
                    if (!BeyonderData.playerMap.contains(player.getUUID())) {
                        source.sendFailure(Component.literal("You are not in the BeyonderMap!"));
                        return 0;
                    }
                    source.sendSystemMessage(Component.literal(
                            BeyonderData.playerMap.get(player.getUUID()).get().getSelfInfo() + "\n"));
                    return 1;
                })
                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    var targets = GameProfileArgument.getGameProfiles(context, "targets");

                    for (var obj : targets) {
                        if (!BeyonderData.playerMap.contains(obj.getId())) {
                            source.sendFailure(Component.literal("BeyonderMap doesn't contain this player!"));
                            continue;
                        }
                        source.sendSystemMessage(Component.literal(
                                BeyonderData.playerMap.get(obj.getId()).get().getAllInfo() + "\n"));
                    }

                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> edit() {
        return Commands.literal("edit")
                .then(Commands.argument("json", StringArgumentType.greedyString())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            String rawJson = StringArgumentType.getString(context, "json");

                            try {
                                JsonObject obj = JsonParser.parseString(rawJson).getAsJsonObject();

                                String name = obj.get("name").getAsString();
                                String path = obj.get("path").getAsString();
                                int seq = obj.get("seq").getAsInt();

                                var id = BeyonderData.playerMap.getKeyByName(name);

                                if(id == null){
                                    source.sendFailure(Component.literal("BeyonderMap doesn't contain passed target!"));
                                    return 0;
                                }

                                if(!BeyonderData.implementedPathways.contains(path)){
                                    source.sendFailure(Component.literal("Unimplemented or unknown pathway!"));
                                    return 0;
                                }

                                if(BeyonderData.getHighestImplementedSequence(path) > seq || seq >= LOTMCraft.NON_BEYONDER_SEQ) {
                                    source.sendFailure(Component.literal("Unimplemented or unknown sequence!"));
                                    return 0;
                                }

                                StoredData data = BeyonderData.playerMap.get(id).get();

                                BeyonderData.playerMap.put(id, StoredData.builder
                                        .copyFrom(data)
                                        .pathway(path)
                                        .sequence(seq)
                                        .modified(true)
                                        .build());

                                return 1;
                            } catch (Exception e) {
                                source.sendFailure(Component.literal("Invalid json format!"));
                                return 0;
                            }
                        })
                );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beyondermap")
                .then(get())
                .then(help().requires(source -> source.hasPermission(2)))
                .then(all().requires(source -> source.hasPermission(2)))
                .then(add().requires(source -> source.hasPermission(2)))
                .then(delete().requires(source -> source.hasPermission(2)))
                .then(edit().requires(source -> source.hasPermission(2)))
        );
    }
}
