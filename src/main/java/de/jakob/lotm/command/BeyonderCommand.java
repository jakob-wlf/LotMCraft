package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BeyonderCommand {
    
    // Suggestion provider for pathways
    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
        (context, builder) -> SharedSuggestionProvider.suggest(BeyonderData.implementedPathways, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beyonder")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .then(Commands.argument("pathway", StringArgumentType.string())
                .suggests(PATHWAY_SUGGESTIONS)
                .then(Commands.argument("sequence", IntegerArgumentType.integer(1, 9))
                    .executes(context -> {
                        // Execute on the command source (self)
                        CommandSourceStack source = context.getSource();
                        if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                            source.sendFailure(Component.literal("Only living entities can use this command!"));
                            return 0;
                        }
                        
                        String pathway = StringArgumentType.getString(context, "pathway");
                        int sequence = IntegerArgumentType.getInteger(context, "sequence");
                        
                        return executeBeyonderCommand(source, livingEntity, pathway, sequence);
                    })
                    .then(Commands.argument("target", EntityArgument.entity())
                        .executes(context -> {
                            // Execute on a target entity
                            CommandSourceStack source = context.getSource();
                            var targetEntity = EntityArgument.getEntity(context, "target");
                            
                            if (!(targetEntity instanceof LivingEntity livingEntity)) {
                                source.sendFailure(Component.literal("Target must be a living entity!"));
                                return 0;
                            }
                            
                            String pathway = StringArgumentType.getString(context, "pathway");
                            int sequence = IntegerArgumentType.getInteger(context, "sequence");
                            
                            return executeBeyonderCommand(source, livingEntity, pathway, sequence);
                        })
                    )
                )
            )
        );
    }
    
    private static int executeBeyonderCommand(CommandSourceStack source, LivingEntity target, String pathway, int sequence) {
        try {
            // Validate pathway exists in the list
            if (!BeyonderData.pathways.contains(pathway)) {
                source.sendFailure(Component.literal("Invalid pathway: " + pathway));
                return 0;
            }
            
            // Call the setBeyonder method
            BeyonderData.setBeyonder(target, pathway, sequence);

            if(target instanceof Player player) {
                var optional = BeyonderData.beyonderMap.get(player);

                if(optional.isPresent()) {
                    StoredData data = optional.get();

                    if (data.sequence() != sequence || (!data.pathway().equals(pathway)
                            && !data.pathway().equals("none"))) {
                        source.sendFailure(Component.literal(
                                "Failed to advance because of insufficient amount of characteristics"));
                        return 0;
                    }
                }
            }

            // Send success message
            String targetName = target instanceof Player player ? player.getGameProfile().getName() : target.getDisplayName().getString();
            source.sendSuccess(() -> Component.literal("Set " + targetName + " to " + pathway + " sequence " + sequence), true);
            
            return 1; // Success
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set beyonder data: " + e.getMessage()));
            return 0;
        }
    }
}