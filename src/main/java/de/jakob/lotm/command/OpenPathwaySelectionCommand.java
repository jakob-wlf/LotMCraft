package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.NewPlayerComponent;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.gui.custom.pathway_selection.PathwaySelectionMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class OpenPathwaySelectionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("open_pathway_selection")
                .executes(context -> {
                    // Execute on a target entity
                    CommandSourceStack source = context.getSource();
                    ServerPlayer livingEntity = source.getEntity() instanceof ServerPlayer entity ? entity : null;
                    if(livingEntity == null) {
                        source.sendFailure(Component.literal("This command can only be executed by a player on the server."));
                        return 0;
                    }

                    boolean spawnWithStartingCharacteristic = livingEntity.level().getGameRules().getBoolean(ModGameRules.SPAWN_WITH_STARTING_CHARACTERISTIC);
                    if(!spawnWithStartingCharacteristic) {
                        source.sendFailure(Component.literal("The game rule 'spawn_with_starting_characteristic' is disabled."));
                        return 0;
                    }

                    NewPlayerComponent component = livingEntity.getData(ModAttachments.BOOK_COMPONENT);
                    if(component.isHasReceivedNewPlayerPerks()) {
                        source.sendFailure(Component.literal("This player has already selected a Pathway."));
                        return 0;
                    }


                    return executeCommand(source, livingEntity);
                })
        );
    }
    
    private static int executeCommand(CommandSourceStack source, ServerPlayer target) {
        try {
            PathwaySelectionMenu.openWithAllPathways(target);
            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to open the Pathway Selection GUI: " + e.getMessage()));
            return 0;
        }
    }
}