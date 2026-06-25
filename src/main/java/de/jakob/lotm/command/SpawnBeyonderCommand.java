package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class SpawnBeyonderCommand {
    
    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
        (context, builder) -> SharedSuggestionProvider.suggest(BeyonderData.implementedPathways, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn_beyonder")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .then(Commands.argument("pathway", StringArgumentType.string())
                .suggests(PATHWAY_SUGGESTIONS)
                .then(Commands.argument("sequence", IntegerArgumentType.integer(0, 9))
                    .executes(context -> {
                        // Execute on the command source (self)
                        CommandSourceStack source = context.getSource();
                        if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                            source.sendFailure(Component.literal("Only living entities can use this command!"));
                            return 0;
                        }
                        
                        String pathway = StringArgumentType.getString(context, "pathway");
                        int sequence = IntegerArgumentType.getInteger(context, "sequence");
                        boolean hasQuest = false;
                        boolean hasTrades = false;
                        
                        return executeBeyonderCommand(source, livingEntity, pathway, sequence, hasQuest, hasTrades);
                    })
                    .then(Commands.argument("hasQuest", BoolArgumentType.bool())
                        .executes(context -> {
                            // Execute on a target entity
                            CommandSourceStack source = context.getSource();

                            if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                                source.sendFailure(Component.literal("Only living entities can use this command!"));
                                return 0;
                            }
                            
                            String pathway = StringArgumentType.getString(context, "pathway");
                            int sequence = IntegerArgumentType.getInteger(context, "sequence");
                            boolean hasQuest = BoolArgumentType.getBool(context, "hasQuest");
                            boolean hasTrades = false;
                            
                            return executeBeyonderCommand(source, livingEntity, pathway, sequence, hasQuest, hasTrades);
                        })
                        .then(Commands.argument("hasTrades", BoolArgumentType.bool())
                            .executes(context -> {
                                // Execute on a target entity
                                CommandSourceStack source = context.getSource();

                                if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                                    source.sendFailure(Component.literal("Only living entities can use this command!"));
                                    return 0;
                                }

                                String pathway = StringArgumentType.getString(context, "pathway");
                                int sequence = IntegerArgumentType.getInteger(context, "sequence");
                                boolean hasQuest = BoolArgumentType.getBool(context, "hasQuest");
                                boolean hasTrades = BoolArgumentType.getBool(context, "hasTrades");

                                if(hasQuest && hasTrades) {
                                    source.sendFailure(Component.literal("Cannot have both quests and trades!"));
                                    return 0;
                                }

                                return executeBeyonderCommand(source, livingEntity, pathway, sequence, hasQuest, hasTrades);
                             })
                        )
                    )
                )
            )
        );
    }
    
    private static int executeBeyonderCommand(CommandSourceStack source, LivingEntity target, String pathway, int sequence, boolean hasQuest, boolean hasTrades) {
        try {
            if (!BeyonderData.pathways.contains(pathway)) {
                source.sendFailure(Component.literal("Invalid pathway: " + pathway));
                return 0;
            }

            BeyonderNPCEntity beyonder = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false, BeyonderNPCEntity.getRandomBeyonderSkin(), pathway, sequence, hasQuest, hasTrades);
            target.level().addFreshEntity(beyonder);
            beyonder.setPos(target.getX(), target.getY(), target.getZ());

            source.sendSuccess(() -> Component.literal("Spawned Beyonder with pathway: " + pathway + " and sequence: " + sequence), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to spawn Beyonder: " + e.getMessage()));
            return 0;
        }
    }
}