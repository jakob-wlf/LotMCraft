package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class CopiedAbilitiesCommand {

    private static final SuggestionProvider<CommandSourceStack> ABILITY_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    LOTMCraft.abilityHandler.getRegisteredAbilityIds(), builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("copied_abilities")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("ability_id", StringArgumentType.string())
                                .suggests(ABILITY_SUGGESTIONS)
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                                        source.sendFailure(Component.literal("Only living entities can use this command!"));
                                        return 0;
                                    }
                                    String abilityId = StringArgumentType.getString(context, "ability_id");
                                    return executeAddAbility(source, livingEntity, abilityId);
                                })
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var targetEntity = EntityArgument.getEntity(context, "target");

                                            if (!(targetEntity instanceof LivingEntity livingEntity)) {
                                                source.sendFailure(Component.literal("Target must be a living entity!"));
                                                return 0;
                                            }

                                            String abilityId = StringArgumentType.getString(context, "ability_id");
                                            return executeAddAbility(source, livingEntity, abilityId);
                                        })
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("ability_id", StringArgumentType.string())
                                .suggests(ABILITY_SUGGESTIONS)
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                                        source.sendFailure(Component.literal("Only living entities can use this command!"));
                                        return 0;
                                    }
                                    String abilityId = StringArgumentType.getString(context, "ability_id");
                                    return executeRemoveAbility(source, livingEntity, abilityId);
                                })
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var targetEntity = EntityArgument.getEntity(context, "target");

                                            if (!(targetEntity instanceof LivingEntity livingEntity)) {
                                                source.sendFailure(Component.literal("Target must be a living entity!"));
                                                return 0;
                                            }

                                            String abilityId = StringArgumentType.getString(context, "ability_id");
                                            return executeRemoveAbility(source, livingEntity, abilityId);
                                        })
                                )
                        )
                )
        );
    }

    private static int executeAddAbility(CommandSourceStack source, LivingEntity target, String abilityId) {
        try {
            if (LOTMCraft.abilityHandler.getById(abilityId) == null) {
                source.sendFailure(Component.literal("Unknown ability ID: " + abilityId));
                return 0;
            }

            CopiedAbilityComponent.CopiedAbilityData data = new CopiedAbilityComponent.CopiedAbilityData(
                    abilityId,
                    "replicated",
                    -1,
                    null
            );

            CopiedAbilityHelper.addAbility(target, data);

            String targetName = target instanceof ServerPlayer player ? player.getGameProfile().getName() : target.getDisplayName().getString();
            source.sendSuccess(() -> Component.literal("Added copied ability '" + abilityId + "' to " + targetName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to add ability: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeRemoveAbility(CommandSourceStack source, LivingEntity target, String abilityId) {
        try {
            CopiedAbilityHelper.removeAbilityID(target, abilityId);
            String targetName = target instanceof ServerPlayer player ? player.getGameProfile().getName() : target.getDisplayName().getString();

            source.sendSuccess(() -> Component.literal("Removed copied ability '" + abilityId + "' from " + targetName), true);
            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to remove ability: " + e.getMessage()));
            return 0;
        }
    }
}