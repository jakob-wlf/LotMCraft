package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class DisableAbilityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("disableability")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .then(Commands.argument("ability", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(LOTMCraft.abilityHandler.getAbilities().stream().map(Ability::getId), builder))
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        String abilityId = StringArgumentType.getString(context, "ability");

                        AbilityHandler abilityHandler = LOTMCraft.abilityHandler;
                        Ability ability = abilityHandler.getById(abilityId);

                        if (ability == null) {
                            source.sendFailure(Component.literal("Ability not found: " + abilityId));
                            return 0;
                        }

                        if(abilityHandler.isDisabled(ability)) {
                            source.sendFailure(Component.literal("Ability is already disabled: " + abilityId));
                            return 0;
                        }

                        abilityHandler.disableAbility(ability);

                        source.sendSuccess(() -> Component.literal("Disabled ability: " + abilityId), true);
                        return 1; // Success
                    })
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
                                "Failed to advance due to insufficient amount of characteristics"));
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