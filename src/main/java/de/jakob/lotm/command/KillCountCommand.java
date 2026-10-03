package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.jakob.lotm.attachments.KillCountComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncKillCountPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KillCountCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("killcount")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        ServerPlayer target = EntityArgument.getPlayer(context, "target");
                        int amount = IntegerArgumentType.getInteger(context, "amount");

                        KillCountComponent killCount = target.getData(ModAttachments.KILL_COUNT_COMPONENT);
                        killCount.setKillCount(amount);
                        PacketHandler.sendToPlayer(target, new SyncKillCountPacket(amount));

                        context.getSource().sendSuccess(() -> Component.literal(
                                "Set kill count of " + target.getName().getString() + " to " + amount), true);
                        return 1;
                    })
                )
            )
        );
    }
}
