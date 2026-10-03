package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.beyonders.abilities.death.NationOfTheDeadAbility;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.DeathDivineKingdomEntity;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.BlackHoleEntity;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.SunKingdomEntity;
import de.jakob.lotm.entity.custom.ability_entities.wheel_of_fortune_pathway.MisfortuneWordsEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

// Cancels the standing activation (domain/entity) of abilities whose effect
// persists beyond the initial use: Sun's Divine Kingdom, Words of Misfortune,
// Nation of the Dead, Death's Divine Kingdom, and Blackhole.
public class CancelActivationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cancel_activation")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                    source.sendFailure(Component.literal("Only living entities can use this command!"));
                    return 0;
                }
                return cancelActivations(source, livingEntity);
            })
            .then(Commands.literal("all")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    int total = 0;
                    for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                        total += cancelActivations(source, player, false);
                    }

                    final int finalTotal = total;
                    source.sendSuccess(() -> Component.literal("Cancelled " + finalTotal + " active ability activation(s) across all players"), true);
                    return total;
                })
            )
            .then(Commands.argument("target", EntityArgument.entity())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    var targetEntity = EntityArgument.getEntity(context, "target");

                    if (!(targetEntity instanceof LivingEntity livingEntity)) {
                        source.sendFailure(Component.literal("Target must be a living entity!"));
                        return 0;
                    }

                    return cancelActivations(source, livingEntity);
                })
            )
        );
    }

    private static int cancelActivations(CommandSourceStack source, LivingEntity target) {
        return cancelActivations(source, target, true);
    }

    private static int cancelActivations(CommandSourceStack source, LivingEntity target, boolean announce) {
        UUID targetUUID = target.getUUID();
        int cancelled = 0;

        if (NationOfTheDeadAbility.cancelDomain(targetUUID)) {
            cancelled++;
        }

        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                boolean owned = switch (entity) {
                    case SunKingdomEntity e -> targetUUID.equals(e.getCasterUUID());
                    case DeathDivineKingdomEntity e -> targetUUID.equals(e.getOwnerUUID());
                    case BlackHoleEntity e -> targetUUID.equals(e.getOwnerUUID());
                    case MisfortuneWordsEntity e -> targetUUID.equals(e.getCasterUUID());
                    default -> false;
                };

                if (owned) {
                    entity.discard();
                    cancelled++;
                }
            }
        }

        final int total = cancelled;
        if (announce) {
            String targetName = target.getDisplayName().getString();
            if (total > 0) {
                source.sendSuccess(() -> Component.literal("Cancelled " + total + " active ability activation(s) for " + targetName), true);
            } else {
                source.sendFailure(Component.literal(targetName + " has no cancellable active ability activations"));
            }
        }

        return total;
    }
}
