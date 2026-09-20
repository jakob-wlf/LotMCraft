package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lotmtest")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("kill").executes(context ->
                                {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            player.kill();
                            return 0;
                                }
                        )
                )
        );
    }

}
