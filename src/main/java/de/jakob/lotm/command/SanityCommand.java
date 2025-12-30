package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SanityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sanity")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("target", EntityArgument.entity())
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 1))
                        .executes(context -> {
                            // Execute on a target entity
                            CommandSourceStack source = context.getSource();
                            var targetEntity = EntityArgument.getEntity(context, "target");

                            if (!(targetEntity instanceof LivingEntity livingEntity)) {
                                source.sendFailure(Component.literal("Target must be a living entity!"));
                                return 0;
                            }

                            float amount = FloatArgumentType.getFloat(context, "amount");

                            return executeSanityCommand(source, livingEntity, amount);
                        })
                    )
                )
        );
    }
    
    private static int executeSanityCommand(CommandSourceStack source, LivingEntity target, float amount) {
        try {
            SanityComponent component = target.getData(ModAttachments.SANITY_COMPONENT);
            component.setSanity(amount);
            source.sendSuccess(() -> Component.literal("Set sanity of " + target.getName().getString() + " to " + amount), true);
            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set sanity: " + e.getMessage()));
            return 0;
        }
    }
}