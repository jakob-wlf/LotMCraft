package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.quest.QuestRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class QuestCommand {

    private static final SuggestionProvider<CommandSourceStack> QUEST_ID_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(QuestRegistry.getQuests().keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("quest_id", StringArgumentType.word())
                                        .suggests(QUEST_ID_SUGGESTIONS)
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                            String questId = StringArgumentType.getString(context, "quest_id");

                                            if (QuestRegistry.getQuest(questId) == null) {
                                                source.sendFailure(Component.literal("Unknown quest id: " + questId));
                                                return 0;
                                            }

                                            boolean accepted = QuestManager.acceptQuestInternal(target, questId);
                                            if (!accepted) {
                                                source.sendFailure(Component.literal("Failed to assign quest to player."));
                                                return 0;
                                            }

                                            source.sendSuccess(() -> Component.literal("Assigned quest '" + questId + "' to " + target.getName().getString()), true);
                                            return 1;
                                        })))));
        ;
    }
}