package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class CorruptionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("corruption")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("target", EntityArgument.entity())
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 1))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var targetEntity = EntityArgument.getEntity(context, "target");

                            if (!(targetEntity instanceof LivingEntity livingEntity)) {
                                source.sendFailure(Component.literal("Target must be a living entity!"));
                                return 0;
                            }

                            float amount = FloatArgumentType.getFloat(context, "amount");

                            return executeCorruptionCommand(source, livingEntity, amount);
                        })
                    )
                )
        );
    }

    private static int executeCorruptionCommand(CommandSourceStack source, LivingEntity target, float amount) {
        try {
            CorruptionComponent component = target.getData(ModAttachments.CORRUPTION_COMPONENT.get());
            component.setCorruptionAndSync(amount, target);
            source.sendSuccess(() -> Component.literal("Set corruption of " + target.getName().getString() + " to " + amount), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set corruption: " + e.getMessage()));
            return 0;
        }
    }
}
