package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.beyonders.acting.ActingCapHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ResetCapCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("resetcap")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                if (!(source.getEntity() instanceof Player player)) {
                    source.sendFailure(Component.literal("Must be run by a player."));
                    return 0;
                }
                return executeReset(source, (ServerPlayer) player);
            })
            .then(Commands.argument("target", EntityArgument.player())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                    return executeReset(source, target);
                })
            )
        );
    }

    private static int executeReset(CommandSourceStack source, ServerPlayer target) {
        target.getPersistentData().putFloat(ActingCapHelper.CAP_REDUCTION_KEY, 0f);
        target.getPersistentData().remove(ActingCapHelper.MISSED_ACTING_KEY);
        ActingCapHelper.syncToClient(target);
        source.sendSuccess(() -> Component.literal("Reset acting cap for " + target.getGameProfile().getName()), true);
        return 1;
    }
}
