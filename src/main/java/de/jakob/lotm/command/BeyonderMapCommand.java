package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Supplier;

public class BeyonderMapCommand {

    public static int PAGE_SIZE = 10;

    private static LiteralArgumentBuilder<CommandSourceStack> help() {
        return Commands.literal("help")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();

                    source.sendSuccess(
                            () -> Component.literal(
                """
                BeyonderMap is module designed to store information about players on server side
                This command is for debug or server maintenance purposes only
                
                Available commands:
                1) help - will give general information about beyonderMap module and possible commands
                2) all <page> - will give list of all stored players and general information about them.
                    By default page is set to 1
                3) add <target> - will try to add target into beyonderMap
                4) delete <target> - will delete target from beyonderMap
                5) get <target> - will give all information about target
                6) edit <target> <json data> - will edit needed info
                """),
                            false
                    );

                    return 1;
                });
    }

    private static int showAll(CommandContext<CommandSourceStack> ctx, int page) {
        CommandSourceStack source = ctx.getSource();

        var data = BeyonderData.beyonderMap.map.entrySet();

        int totalPages = (int) Math.ceil((double) data.size() / PAGE_SIZE);

        if (data.isEmpty()) {
            source.sendFailure(Component.literal("No entries found."));
            return 0;
        }

        if (page > totalPages) {
            source.sendFailure(Component.literal("Page " + page + " does not exist."));
            return 0;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, data.size());

        source.sendSuccess(
                () -> Component.literal(
                        "---- Page " + page + "/" + totalPages + " ----"
                ),
                false
        );

        for (var obj : data){
            source.sendSuccess(
                    () -> Component.literal(obj.getValue().getShortInfo()),
                    false
            );
        }

        sendPageControls(source, page, totalPages);

        return 1;
    }

    private static void sendPageControls(CommandSourceStack source, int page, int totalPages) {
        MutableComponent controls = Component.empty();

        if(totalPages == 1) return;

        if (page > 1) {
            controls =  controls.append(
                    Component.literal("« Prev ")
                            .withStyle(style -> style
                                    .withClickEvent(
                                            new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/beyondermap all " + (page - 1)
                                            )
                                    )
                                    .withHoverEvent(
                                            new HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT,
                                                    Component.literal("Go to page " + (page - 1))
                                            )
                                    )
                            )
            );
        }

        controls = controls.append(
                Component.literal(" | Page " + page + " of " + totalPages + " | ")
                        .withStyle(ChatFormatting.GRAY)
        );

        if (page < totalPages) {
            controls = controls.append(
                    Component.literal(" Next »")
                            .withStyle(style -> style
                                    .withClickEvent(
                                            new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/beyondermap all " + (page + 1)
                                            )
                                    )
                                    .withHoverEvent(
                                            new HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT,
                                                    Component.literal("Go to page " + (page + 1))
                                            )
                                    )
                            )
            );
        }


        source.sendSuccess((Supplier<Component>) controls, false);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> all() {
        return Commands.literal("all")
                .executes(ctx -> showAll(ctx, 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx ->
                                showAll(ctx, IntegerArgumentType.getInteger(ctx, "page"))
                        )
                );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beyondermap")
                .requires(source -> source.hasPermission(2))
                .then(help())
                .then(all())

        );
    }
}
