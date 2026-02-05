package de.jakob.lotm.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.MessageType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class AddMessageCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("addmessage")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("title", StringArgumentType.string())
                        .then(Commands.argument("desc", StringArgumentType.string())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            var target = EntityArgument.getEntity(context, "target");

                            if (!(target instanceof LivingEntity livingEntity)) {
                                source.sendFailure(Component.literal("Target must be a living entity!"));
                                return 0;
                            }

                            String title = StringArgumentType.getString(context, "title");
                            String desc = StringArgumentType.getString(context, "desc");

                            return executeAddMessageCommand(livingEntity, title, desc);
                        }
                        )
                        )
                        )
                )
        );
    }

    public static int executeAddMessageCommand(LivingEntity target, String title, String desc){
        BeyonderData.beyonderMap.addMessage(target, new MessageType(
                null,
                MessageType.createTimestamp(),
                title,
                desc,
                false));

        return 1;
    }
}
