package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityCooldownPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ResetCooldownsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reset_cooldowns")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("target", EntityArgument.entity())
                    .executes(context -> {
                        // Execute on a target entity
                        CommandSourceStack source = context.getSource();
                        var targetEntity = EntityArgument.getEntity(context, "target");

                        if (!(targetEntity instanceof Player player)) {
                            source.sendFailure(Component.literal("Target must be a player!"));
                            return 0;
                        }

                        player.getData(ModAttachments.COOLDOWN_COMPONENT).removeAllCooldowns();
                        if (player instanceof ServerPlayer serverPlayer) {
                            PacketHandler.sendToPlayer(serverPlayer, SyncAbilityCooldownPacket.clearAll());
                        }
                        source.sendSuccess(() -> Component.literal("Reset cooldowns for " + player.getName().getString()), true);
                        return 1;
                    })
                )
        );
    }
}