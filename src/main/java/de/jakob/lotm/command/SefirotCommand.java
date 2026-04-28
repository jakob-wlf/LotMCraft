package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SefirotCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(SefirahHandler.implementedSefirah, builder);


    private static LiteralArgumentBuilder<CommandSourceStack> check() {
        return Commands.literal("check")
                .then(Commands.argument("target", StringArgumentType.string())
                        .suggests(SUGGESTIONS)
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var target = StringArgumentType.getString(context, "target");

                            var id = BeyonderData.playerMap.findBySefirot(target);
                            if (id == null) {
                                source.sendFailure(Component.literal("Selected sefirot is not claimed"));
                                return 0;
                            }

                            source.sendSystemMessage(Component.literal(
                                    BeyonderData.playerMap.get(id).get().getAllInfo() + "\n"));

                            return 1;
                        })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> set() {
        return Commands.literal("set")
                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                .then(Commands.argument("sefirot", StringArgumentType.word())
                        .suggests(SUGGESTIONS)
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var sefirot = StringArgumentType.getString(context, "sefirot");
                            var targets = GameProfileArgument.getGameProfiles(context, "targets");

                            for (var obj : targets) {
                                if (!BeyonderData.playerMap.contains(obj.getId())) {
                                    source.sendFailure(Component.literal("BeyonderMap doesn't contain this player!"));
                                    continue;
                                }

                                var target = source.getLevel().getPlayerByUUID(obj.getId());
                                if (target == null) {
                                    StoredData data = BeyonderData.playerMap.get(obj.getId()).get();

                                    BeyonderData.playerMap.put(obj.getId(), StoredData.builder
                                            .copyFrom(data)
                                            .sefirot(sefirot)
                                            .modified(true)
                                            .build());
                                } else {
                                    SefirahHandler.claimSefirot((ServerPlayer) target, sefirot);
                                }
                            }

                            return 1;
                        })));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sefirot")
                .requires(source -> source.hasPermission(2))
                .then(check())
                .then(set())
        );
    }
}
