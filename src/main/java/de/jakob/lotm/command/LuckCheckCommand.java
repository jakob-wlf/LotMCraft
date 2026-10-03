package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class LuckCheckCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("luck")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("target", EntityArgument.entity())
                    .executes(context -> {
                        // Execute on a target entity
                        CommandSourceStack source = context.getSource();
                        var targetEntity = EntityArgument.getEntity(context, "target");

                        if (!(targetEntity instanceof LivingEntity livingEntity)) {
                            source.sendFailure(Component.literal("Target must be a living entity!"));
                            return 0;
                        }

                        return executeCommand(source, livingEntity);
                    })
                )
        );
    }
    
    private static int executeCommand(CommandSourceStack source, LivingEntity target) {
        try {
            LuckComponent component = target.getData(ModAttachments.LUCK_COMPONENT);
            source.sendSuccess(() -> Component.literal("Luck of " + target.getName().getString() + " is " + component.getLuck()), true);
            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to get luck: " + e.getMessage()));
            return 0;
        }
    }
}