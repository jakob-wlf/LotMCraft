package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.addons.rituals.RitualDescriptionHelper;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RitualCommand {

    public static Set<UUID> opState = new HashSet<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rituals")
                .requires(source -> source.hasPermission(2))
                .then(info())
                .then(setComplete())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> info() {
        return Commands.literal("info")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayer();
                    if (player == null) {
                        source.sendFailure(Component.literal("Must be player!"));
                        return 0;
                    }

                    if(!BeyonderData.isBeyonder(player)){
                        source.sendFailure(Component.literal("You must be a beyonder!"));
                        return 0;
                    }

                    String desc = RitualDescriptionHelper.getRitualDescription(
                            BeyonderData.getPathway(player), BeyonderData.getSequence(player)-1
                    );

                    Component msg = Component.literal(desc.isEmpty() ? "You don`t need any ritual for your next sequence\n"
                            : "Your next ritual: " + desc).withStyle(ChatFormatting.GREEN);

                    source.sendSystemMessage(msg);
                    return 0;
                })
                ;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setComplete() {
        return Commands.literal("set_complete")
                .then(Commands.argument("player", EntityArgument.entity())
                        .then(Commands.argument("complete", BoolArgumentType.bool())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    boolean value = BoolArgumentType.getBool(context, "complete");
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                    if (!BeyonderData.isBeyonder(player) || !BeyonderData.hasRitual(player)) {
                                        source.sendFailure(Component.literal("Target must be beyonder and have pending ritual!"));
                                        return 0;
                                    }

                                    if(value)
                                        player.getData(ModAttachments.RITUALS.get()).setCompleted(value);
                                    else
                                        RitualEffectHandlerEvent.removeRitualWithMessage(player);

                                    source.sendSystemMessage(Component.literal("Set completion status to " + value + "\n").withStyle(ChatFormatting.GREEN));
                                    return 0;
                                })
                        ));
    }

}
