package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sun.jdi.connect.Connector;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.BeyonderMap;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
6) edit <target> (or <target_nickname>) <json data> - will edit needed info
7) delete all - will delete entire database
"""));

                    return 1;
                });
    }

    private static int showAll(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        var data = BeyonderData.beyonderMap.entrySet().stream()
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

                    BeyonderData.beyonderMap.put(livingEntity);

                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> delete() {
        return Commands.literal("delete")
                .then(Commands.argument("target", EntityArgument.entity())
                .executes( context -> {
                    CommandSourceStack source = context.getSource();
                    var targetEntity = EntityArgument.getEntity(context, "target");

                    if (!(targetEntity instanceof LivingEntity livingEntity)
                            || !(BeyonderData.isBeyonder(livingEntity))) {
                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                        return 0;
                    }

                    if(!BeyonderData.beyonderMap.contains(livingEntity)){
                        source.sendFailure(Component.literal("BeyonderMap doesn't contain passed target!"));
                        return 0;
                    }

                    BeyonderData.beyonderMap.remove(livingEntity);

                    return 1;
                }))
                .then(Commands.argument("nickname", StringArgumentType.string())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var targetNick = StringArgumentType.getString(context, "nickname");

                            var UUID = BeyonderData.beyonderMap.getKeyByName(targetNick);

                            if(UUID == null){
                                source.sendFailure(Component.literal("BeyonderMap doesn't contain passed target!"));
                                return 0;
                            }

                            BeyonderData.beyonderMap.remove(UUID);

                            return 1;
                        })
                )
                .then(Commands.literal("all")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            if(BeyonderData.beyonderMap.isEmpty()) {
                                source.sendFailure(Component.literal("BeyonderMap must contain any target!"));
                                return 0;
                            }

                            BeyonderData.beyonderMap.clear();

                            return 1;
                        })
                )
                ;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("get")
                .then(Commands.argument("target", EntityArgument.entity())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    var targetEntity = EntityArgument.getEntity(context, "target");

                    if (!(targetEntity instanceof LivingEntity livingEntity)
                            || !(BeyonderData.isBeyonder(livingEntity))) {
                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                        return 0;
                    }

                    if(!BeyonderData.beyonderMap.contains(livingEntity)){
                        source.sendFailure(Component.literal("BeyonderMap doesn't contain passed target!"));
                        return 0;
                    }

                    source.sendSystemMessage(Component.literal(BeyonderData.
                            beyonderMap.get(livingEntity).get().getAllInfo()));

                    return 1;
                }))
                .then(Commands.argument("nickname", StringArgumentType.string())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var targetNick = StringArgumentType.getString(context, "nickname");

                            var UUID = BeyonderData.beyonderMap.getKeyByName(targetNick);

                            if(UUID == null){
                                source.sendFailure(Component.literal("BeyonderMap doesn't contain passed target!"));
                                return 0;
                            }

                            source.sendSystemMessage(Component.literal(BeyonderData.
                                    beyonderMap.get(UUID).get().getAllInfo()));

                            return 1;
                        }));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beyondermap")
                .requires(source -> source.hasPermission(2))
                .then(help())
                .then(all())
                .then(add())
                .then(delete())
                .then(get())

        );
    }
}
