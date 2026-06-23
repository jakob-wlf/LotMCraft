package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class UniquenessCommand {

    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(BeyonderData.implementedPathways, builder);
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("uniqueness")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("check")
                        .then(Commands.argument("pathway", StringArgumentType.string())
                                .suggests(PATHWAY_SUGGESTIONS)
                                .executes(context -> {
                                    String pathway = StringArgumentType.getString(context, "pathway");
                                    return checkHolder(context.getSource(), pathway);
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("pathway", StringArgumentType.string())
                                .suggests(PATHWAY_SUGGESTIONS)
                                .executes(context -> {
                                    String pathway = StringArgumentType.getString(context, "pathway");
                                    return removeUniqueness(context.getSource(), pathway);
                                })
                        )
                )
                .then(Commands.literal("spawn")
                        .then(Commands.argument("pathway", StringArgumentType.string())
                                .suggests(PATHWAY_SUGGESTIONS)
                                .executes(context -> {
                                    String pathway = StringArgumentType.getString(context, "pathway");

                                    if (UniquenessEntity.existsInWorld(context.getSource().getLevel(), pathway)) return 0;

                                    if (UniquenessEntity.anyPlayerHoldsUniqueness(context.getSource().getLevel(), pathway)) return 0;

                                    int seq0Count = BeyonderData.countTotalSequence(context.getSource().getLevel(), pathway, 0);
                                    if (context.getSource().getEntity() instanceof LivingEntity living
                                            && BeyonderData.isBeyonder(living)
                                            && BeyonderData.getSequence(living) == 0
                                            && pathway.equalsIgnoreCase(BeyonderData.getPathway(living))) {
                                        seq0Count = Math.max(0, seq0Count - 1);
                                    }
                                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                        int stored = countStoredSeq0Souls(player, pathway);
                                        seq0Count = Math.max(0, seq0Count - stored);
                                    }
                                    if (seq0Count > 0) return 0;

                                    UniquenessEntity.trySpawn(context.getSource().getLevel(), context.getSource().getPosition().add(0, -2, 0), pathway);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("spawn_empty")
                            .executes(context -> {
                                String pathway = "";

                                UniquenessEntity uniqueness = new UniquenessEntity(context.getSource().getLevel(), context.getSource().getPosition(), pathway);
                                uniqueness.setPos(context.getSource().getPosition());
                                context.getSource().getLevel().addFreshEntity(uniqueness);
                                return 1;
                            })
                )
        );
    }

    private static int checkHolder(CommandSourceStack source, String pathway) {
        if (!BeyonderData.implementedPathways.contains(pathway)) {
            source.sendFailure(Component.literal("Unknown or unimplemented pathway: " + pathway));
            return 0;
        }

        ServerLevel level = source.getServer().overworld();

        // Check if an entity exists in the world
        boolean entityExists = UniquenessEntity.existsInWorld(level, pathway);

        // Check which player holds it
        List<ServerPlayer> holders = source.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> {
                    UniquenessComponent comp = p.getData(ModAttachments.UNIQUENESS_COMPONENT);
                    return comp.hasUniqueness() && pathway.equalsIgnoreCase(comp.getUniquenessPathway());
                })
                .toList();

        if (holders.isEmpty()) {
            if (entityExists) {
                source.sendSuccess(() -> Component.literal("The " + pathway + " uniqueness is currently in the world (not held by any player)."), false);
            } else {
                source.sendSuccess(() -> Component.literal("The " + pathway + " uniqueness is not currently in the world and not held by any player."), false);
            }
        } else {
            for (ServerPlayer holder : holders) {
                int killCount = holder.getData(ModAttachments.UNIQUENESS_COMPONENT).getKillCount();
                source.sendSuccess(() -> Component.literal(
                        holder.getName().getString() + " holds the " + pathway + " uniqueness. Kill count: " + killCount
                ), false);
            }
        }

        return 1;
    }

    private static int removeUniqueness(CommandSourceStack source, String pathway) {
        if (!BeyonderData.implementedPathways.contains(pathway)) {
            source.sendFailure(Component.literal("Unknown or unimplemented pathway: " + pathway));
            return 0;
        }

        List<ServerPlayer> holders = source.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> {
                    UniquenessComponent comp = p.getData(ModAttachments.UNIQUENESS_COMPONENT);
                    return comp.hasUniqueness() && pathway.equalsIgnoreCase(comp.getUniquenessPathway());
                })
                .toList();

        if (holders.isEmpty()) {
            source.sendFailure(Component.literal("No online player currently holds the " + pathway + " uniqueness."));
            return 0;
        }

        for (ServerPlayer holder : holders) {
            UniquenessComponent comp = holder.getData(ModAttachments.UNIQUENESS_COMPONENT);
            comp.setHasUniqueness(false);
            comp.setUniquenessPathway("");
            BeyonderData.playerMap.setUniqueness(holder, "none");
            PacketHandler.syncUniquenessToPlayer(holder);
            source.sendSuccess(() -> Component.literal("Removed uniqueness from " + holder.getName().getString()), false);
        }

        return 1;
    }

    private static int countStoredSeq0Souls(ServerPlayer player, String pathway) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains("InternalUnderworldSouls", Tag.TAG_LIST)) {
            return 0;
        }
        ListTag list = data.getList("InternalUnderworldSouls", Tag.TAG_COMPOUND);
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag soul = list.getCompound(i);
            if (!soul.contains("Sequence", Tag.TAG_INT)) continue;
            if (soul.getInt("Sequence") != 0) continue;
            if (!pathway.equalsIgnoreCase(soul.getString("Pathway"))) continue;
            count++;
        }
        return count;
    }
}
