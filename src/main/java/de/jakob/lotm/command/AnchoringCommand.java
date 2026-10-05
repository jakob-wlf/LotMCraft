package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AnchoringCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anchoring")
                .then(info())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> info() {
        return Commands.literal("info")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayer();
                    if(player == null){
                        source.sendFailure(Component.literal("Must be player!"));
                        return 0;
                    }

                    var anchoring = BeyonderData.anchoringStorage.getAnchoring(player.getName().getString());
                    Component msg = Component.literal(anchoring
                                    .getInfo(BeyonderData.getSequence(player)))
                                    .withStyle(ChatFormatting.GREEN);

                    source.sendSystemMessage(msg);
                    return 0;
                })
                ;
    }

}
