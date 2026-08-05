package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.jakob.lotm.attachments.DeathImprintData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ImprintCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("imprint")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.literal("set")
                    .then(Commands.argument("tier", IntegerArgumentType.integer(0, 3))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                            int tier = IntegerArgumentType.getInteger(context, "tier");

                            DeathImprintData data = DeathImprintData.get(source.getServer());
                            data.setImprintCount(target.getUUID(), tier);
                            if (tier > 0) {
                                data.scheduleDecay(target.getUUID());
                                data.saveSnapshot(
                                        target.getUUID(),
                                        target.getGameProfile().getName(),
                                        de.jakob.lotm.util.BeyonderData.getPathway(target),
                                        de.jakob.lotm.util.BeyonderData.getSequence(target)
                                );
                            }

                            int finalTier = tier;
                            source.sendSuccess(() -> Component.literal(
                                    "Set death imprint tier of §e" + target.getName().getString() + "§r to §d" + finalTier + "§r."), true);
                            target.sendSystemMessage(Component.literal(
                                    "§8[River of Eternal Darkness] §5Your death imprint tier has been set to §d" + finalTier + "§5."));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("remove")
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        ServerPlayer target = EntityArgument.getPlayer(context, "target");

                        DeathImprintData data = DeathImprintData.get(source.getServer());
                        data.setImprintCount(target.getUUID(), 0);

                        source.sendSuccess(() -> Component.literal(
                                "Removed all death imprints from §e" + target.getName().getString() + "§r."), true);
                        target.sendSystemMessage(Component.literal(
                                "§8[River of Eternal Darkness] §5Your death imprint has been cleared."));
                        return 1;
                    })
                )
            )
        );
    }
}
