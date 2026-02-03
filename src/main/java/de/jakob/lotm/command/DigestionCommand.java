package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class DigestionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("digest")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("target", EntityArgument.entity())
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 1))
                        .executes(context -> {
                            // Execute on a target entity
                            CommandSourceStack source = context.getSource();
                            var targetEntity = EntityArgument.getEntity(context, "target");

                            if (!(targetEntity instanceof Player player)) {
                                source.sendFailure(Component.literal("Target must be a player!"));
                                return 0;
                            }

                            float amount = FloatArgumentType.getFloat(context, "amount");

                            return executeSanityCommand(source, player, amount);
                        })
                    )
                )
        );
    }
    
    private static int executeSanityCommand(CommandSourceStack source, Player target, float amount) {
        try {
            BeyonderData.digest(target, amount);
            source.sendSuccess(() -> Component.literal("Digested potion " + target.getName().getString() + " with " + amount), true);
            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set digest: " + e.getMessage()));
            return 0;
        }
    }
}