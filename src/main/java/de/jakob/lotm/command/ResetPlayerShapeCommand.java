package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ShapeShiftComponent;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ResetPlayerShapeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reset_shape_shifting")
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

                        if (targetEntity instanceof ServerPlayer serverPlayer){
                            ShapeShiftingUtil.resetShape(serverPlayer);
                        }


                        return 1;
                    })
                )
        );
    }
}