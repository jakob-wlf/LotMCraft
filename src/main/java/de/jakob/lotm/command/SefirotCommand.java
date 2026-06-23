package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

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

    private static LiteralArgumentBuilder<CommandSourceStack> clear() {
        return Commands.literal("clear")
                        .then(Commands.argument("sefirot", StringArgumentType.word())
                                .suggests(SUGGESTIONS)
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var sefirot = StringArgumentType.getString(context, "sefirot");

                                    SefirahHandler.clearAll(sefirot, source.getServer());

                                    return 1;
                                }));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sefirot")
                .requires(source -> source.hasPermission(2))
                .then(check())
                .then(set())
                .then(clear())
                .then(imprint())
        );
    }

    // ── /sefirot imprint ──────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> imprint() {
        return Commands.literal("imprint")
                // /sefirot imprint check <sefirot>
                .then(Commands.literal("check")
                        .then(Commands.argument("sefirot", StringArgumentType.word())
                                .suggests(SUGGESTIONS)
                                .executes(ctx -> {
                                    String sefirot = StringArgumentType.getString(ctx, "sefirot");
                                    SefirotData data = SefirotData.get(ctx.getSource().getServer());
                                    int pct = data.getMentalImprint(sefirot);
                                    UUID firstOwner = data.getFirstOwner(sefirot);
                                    UUID holder = data.getHolderOf(sefirot);
                                    String ownerStr = firstOwner != null ? firstOwner.toString() : "none";
                                    String holderStr = holder != null ? holder.toString() : "unclaimed";
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "[Sefirot Imprint] " + sefirot
                                            + "\n  First owner: " + ownerStr
                                            + "\n  Current holder: " + holderStr
                                            + "\n  Imprint: " + pct + "%"), false);
                                    return pct;
                                })
                        )
                )
                // /sefirot imprint set <sefirot> <0-100>
                .then(Commands.literal("set")
                        .then(Commands.argument("sefirot", StringArgumentType.word())
                                .suggests(SUGGESTIONS)
                                .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> {
                                            String sefirot = StringArgumentType.getString(ctx, "sefirot");
                                            int pct = IntegerArgumentType.getInteger(ctx, "percent");
                                            SefirotData data = SefirotData.get(ctx.getSource().getServer());
                                            data.setMentalImprintDirect(sefirot, pct);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "Set mental imprint for " + sefirot + " to " + pct + "%"), true);
                                            return 1;
                                        })
                                )
                        )
                )
                // /sefirot imprint clear <sefirot>
                .then(Commands.literal("clear")
                        .then(Commands.argument("sefirot", StringArgumentType.word())
                                .suggests(SUGGESTIONS)
                                .executes(ctx -> {
                                    String sefirot = StringArgumentType.getString(ctx, "sefirot");
                                    SefirotData data = SefirotData.get(ctx.getSource().getServer());
                                    data.clearMentalImprint(sefirot);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "Cleared mental imprint data for " + sefirot), true);
                                    return 1;
                                })
                        )
                );
    }
}
