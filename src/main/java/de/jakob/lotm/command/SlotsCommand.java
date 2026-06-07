package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.network.packets.toServer.CharSlotRollResultPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /slots <player>
 *
 * Forces the characteristic slot-roll wheel to open for the specified player,
 * resetting their new-player-perks flag so they can accept a new characteristic.
 * Requires OP level 2.
 */
public class SlotsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("slots")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("player", EntityArgument.player())
                .executes(context -> {
                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                    return executeSlots(context.getSource(), target);
                })
            )
        );
    }

    private static int executeSlots(CommandSourceStack source, ServerPlayer target) {
        CharSlotRollResultPacket.forceRollForPlayer(target);
        source.sendSuccess(() -> Component.literal(
                "Opened the characteristic wheel for " + target.getGameProfile().getName()), true);
        return 1;
    }
}
