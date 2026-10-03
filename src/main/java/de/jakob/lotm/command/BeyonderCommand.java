package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.beyonders.abilities.death.passives.ReincarnationAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.acting.ActingCapHelper;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.SetBeyonderAuditLog;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BeyonderCommand {
    
    // Suggestion provider for pathways
    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
        (context, builder) -> SharedSuggestionProvider.suggest(BeyonderData.implementedPathways, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beyonder")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .then(Commands.literal("none")
                .then(Commands.literal("all")
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        int count = 0;
                        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                            count += clearBeyonderStatus(source, player);
                        }
                        final int total = count;
                        source.sendSuccess(() -> Component.literal("Removed Beyonder status from " + total + " player(s)"), true);
                        return count;
                    })
                )
            )
            .then(Commands.argument("pathway", StringArgumentType.string())
                .suggests(PATHWAY_SUGGESTIONS)
                .then(Commands.argument("sequence", IntegerArgumentType.integer(0, 9))
                    .executes(context -> {
                        // Execute on the command source (self)
                        CommandSourceStack source = context.getSource();
                        if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                            source.sendFailure(Component.literal("Only living entities can use this command!"));
                            return 0;
                        }
                        
                        String pathway = StringArgumentType.getString(context, "pathway");
                        int sequence = IntegerArgumentType.getInteger(context, "sequence");
                        
                        return executeBeyonderCommand(source, livingEntity, pathway, sequence, true);
                    })
                    .then(Commands.argument("target", EntityArgument.entity())
                        .executes(context -> {
                            // Execute on a target entity
                            CommandSourceStack source = context.getSource();
                            var targetEntity = EntityArgument.getEntity(context, "target");
                            
                            if (!(targetEntity instanceof LivingEntity livingEntity)) {
                                source.sendFailure(Component.literal("Target must be a living entity!"));
                                return 0;
                            }
                            
                            String pathway = StringArgumentType.getString(context, "pathway");
                            int sequence = IntegerArgumentType.getInteger(context, "sequence");
                            
                            return executeBeyonderCommand(source, livingEntity, pathway, sequence, true);
                        })
                    )
                )
            )
        );
    }

    // Mirrors the regression-on-death path (BeyonderEventHandler.onPlayerDrops) for a Seq 9 -> non-Beyonder drop
    private static int clearBeyonderStatus(CommandSourceStack source, ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) return 0;

        try {
            // Toggled abilities aren't tied to pathway/sequence checks while active, so a stale
            // toggle would otherwise keep ticking (and stay re-toggleable) after the reset below.
            ToggleAbility.cleanUp(player.serverLevel(), player);

            ActingCapHelper.skipNextCapApplication = true;
            try {
                BeyonderData.setBeyonder(player, "none", LOTMCraft.NON_BEYONDER_SEQ, true, false, false, false);
            } finally {
                ActingCapHelper.skipNextCapApplication = false;
            }

            ActingCapHelper.clearCap(player);

            // setBeyonder/clearBeyonderData only reset the core BeyonderComponent fields;
            // custom pathway attachments (luck, fooling status, uniqueness) need clearing separately,
            // same as the explicit resets done on the death-regression path.
            player.getData(ModAttachments.LUCK_COMPONENT).setLuck(0);
            player.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT).setTicksAccumulated(0);
            player.getData(ModAttachments.FOOLING_COMPONENT).clear();

            UniquenessComponent uniquenessComponent = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
            uniquenessComponent.setHasUniqueness(false);
            uniquenessComponent.setUniquenessPathway("");
            uniquenessComponent.resetKillCount();

            player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).enableAllAbilities();
            ReincarnationAbility.clearCooldown(player);

            String playerName = player.getGameProfile().getName();
            SetBeyonderAuditLog.get(source.getLevel()).addEntry(source.getTextName(), playerName,
                    "none", LOTMCraft.NON_BEYONDER_SEQ, "beyonder none all");

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to clear beyonder data for "
                    + player.getGameProfile().getName() + ": " + e.getMessage()));
            return 0;
        }
    }

    private static int executeBeyonderCommand(CommandSourceStack source, LivingEntity target, String pathway, int sequence, boolean announce) {
        try {
            // Validate pathway exists in the list
            if (!BeyonderData.pathways.contains(pathway)) {
                source.sendFailure(Component.literal("Invalid pathway: " + pathway));
                return 0;
            }
            
            // Call the setBeyonder method. Skip acting cap so admin commands don't penalise players
            ActingCapHelper.skipNextCapApplication = true;
            try {
                BeyonderData.setBeyonder(target, pathway, sequence, false, true, true, true);
            } finally {
                ActingCapHelper.skipNextCapApplication = false;
            }

            if(target instanceof Player player) {
                var optional = BeyonderData.playerMap.get(player);

                if(optional.isPresent()) {
                    StoredData data = optional.get();

                    if (data.sequence() != sequence || (!data.pathway().equals(pathway)
                            && !data.pathway().equals("none"))) {
                        source.sendFailure(Component.literal(
                                "Failed to advance due to insufficient amount of characteristics"));
                        return 0;
                    }
                }

                // Command-set sequences start with a fresh slate, same as /resetcap
                ActingCapHelper.clearCap(player);
            }

            // Send success message
            String targetName = target instanceof Player player ? player.getGameProfile().getName() : target.getDisplayName().getString();
            if (announce) {
                source.sendSuccess(() -> Component.literal("Set " + targetName + " to " + pathway + " sequence " + sequence), true);
            }

            // Audit log
            String executorName = source.getTextName();
            String fullCommand = "beyonder " + pathway + " " + sequence
                    + (target instanceof Player p && !p.getGameProfile().getName().equals(executorName)
                       ? " " + targetName : "");
            ServerLevel level = source.getLevel();
            SetBeyonderAuditLog.get(level).addEntry(executorName, targetName, pathway, sequence, fullCommand);

            return 1; // Success
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set beyonder data: " + e.getMessage()));
            return 0;
        }
    }
}