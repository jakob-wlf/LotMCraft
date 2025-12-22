package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jakob.lotm.util.helper.AllyRequestManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.UUID;

/**
 * Client-side commands for accepting/denying ally requests
 * These are hidden commands triggered by clicking chat messages
 */
public class AllyRequestCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Accept ally request command
        dispatcher.register(Commands.literal("lotm_accept_ally")
                .then(Commands.argument("uuid", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                String uuidStr = StringArgumentType.getString(context, "uuid");
                                UUID requesterUUID = UUID.fromString(uuidStr);
                                AllyRequestManager.acceptRequest(requesterUUID);
                                return 1;
                            } catch (IllegalArgumentException e) {
                                return 0;
                            }
                        })
                )
        );
        
        // Deny ally request command
        dispatcher.register(Commands.literal("lotm_deny_ally")
                .then(Commands.argument("uuid", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                String uuidStr = StringArgumentType.getString(context, "uuid");
                                UUID requesterUUID = UUID.fromString(uuidStr);
                                AllyRequestManager.denyRequest(requesterUUID);
                                return 1;
                            } catch (IllegalArgumentException e) {
                                return 0;
                            }
                        })
                )
        );
    }
}