package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import de.jakob.lotm.attachments.CorruptedPlayerComponent;
import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.events.CorruptionEventHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;

public class CorruptionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("corruption")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("target", EntityArgument.entity())
                    .then(Commands.literal("set")
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
                    .then(Commands.literal("reset")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var targetEntity = EntityArgument.getEntity(context, "target");

                            if (targetEntity instanceof ServerPlayer player) {
                                CorruptionEventHandler.revertFullCorruption(player);
                                CorruptedPlayerComponent corruptedComp = player.getData(ModAttachments.CORRUPTED_PLAYER_COMPONENT);
                                corruptedComp.setFullyCorrupted(false);
                                corruptedComp.setNpcUUID(null);
                                player.setGameMode(GameType.SURVIVAL);
                                
                                CorruptionComponent corruptionComp = player.getData(ModAttachments.CORRUPTION_COMPONENT);
                                corruptionComp.setCorruptionAndSync(0, player);
                                
                                source.sendSuccess(() -> Component.literal("Reset corruption and state for " + player.getName().getString()), true);
                                return 1;
                            } else if (targetEntity instanceof LivingEntity living) {
                                CorruptionComponent corruptionComp = living.getData(ModAttachments.CORRUPTION_COMPONENT);
                                corruptionComp.setCorruptionAndSync(0, living);
                                source.sendSuccess(() -> Component.literal("Reset corruption for " + living.getName().getString()), true);
                                return 1;
                            }
                            return 0;
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
