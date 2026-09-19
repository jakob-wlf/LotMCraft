package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.jakob.lotm.util.LuckManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LuckCheckCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("luck")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> {
                        return getLuck(context.getSource(), EntityArgument.getPlayer(context, "target"));
                    })
                    .then(Commands.argument("value", IntegerArgumentType.integer(LuckManager.minimumLuck, 25000))
                        .executes(context -> setLuck(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "target"),
                                IntegerArgumentType.getInteger(context, "value"))))
                )
        );
    }

    private static int getLuck(CommandSourceStack source, ServerPlayer target) {
        int luck = LuckManager.usesWheelLuckResource(target)
                ? LuckManager.getNetLuck(target)
                : LuckManager.getEffectiveLuck(target);
        source.sendSuccess(() -> Component.literal(
                "Luck of " + target.getName().getString() + " is " + luck), false);
        return 1;
    }

    private static int setLuck(CommandSourceStack source, ServerPlayer target, int requestedValue) {
        int actualValue = LuckManager.setLuck(target, requestedValue);
        source.sendSuccess(() -> Component.literal(
                "Set luck of " + target.getName().getString() + " to " + actualValue), true);
        return 1;
    }
}