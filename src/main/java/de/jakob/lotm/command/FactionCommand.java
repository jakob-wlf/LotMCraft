package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FactionCommand {

    private static Map<UUID, Integer> inviteMap = new HashMap<>();

    private static final MutableComponent accept = Component.literal("[ACCEPT]")
            .withStyle(style -> style
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/faction invite accept"
                    )));

    private static final MutableComponent decline = Component.literal("[DECLINE]")
            .withStyle(style -> style
                    .withColor(ChatFormatting.RED)
                    .withClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/faction invite decline"
                    )));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faction")
                .then(create())
                .then(get())
                .then(claim())
                .then(here())
                .then(permission())
                .then(unclaim())
                .then(map())
                .then(disband())
                .then(invite())
                .then(leave())
                .then(kick())
                .then(promote())
                .then(passLeadership())
                .then(bank())
                .then(levelUp())
                .then(war())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("create")
                .then(Commands.literal("nation")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var name = StringArgumentType.getString(context, "name");

                                    if (!BeyonderData.factionStorage.isNameUnique(name)) {
                                        source.sendFailure(Component.literal("Name is already taken!"));
                                        return 0;
                                    }

                                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                                        source.sendFailure(Component.literal("Player must use this command!"));
                                        return 0;
                                    }

                                    if (BeyonderData.factionStorage.hasAnyByType(player.getName().getString(), 1)) {
                                        source.sendFailure(Component.literal("You are already part of such type of faction!"));
                                        return 0;
                                    }

                                    if (BeyonderData.getSequence(player) > FactionCore.getMinSeqNation()) {
                                        source.sendFailure(Component.literal("You have to be at least " + FactionCore.getMinSeqNation() + " sequence!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.createFaction(player.getName().getString(), name, 1);

                                    source.sendSystemMessage(Component.literal("Successfully created faction named: \"" + name + "\"" + '\n').withStyle(ChatFormatting.GREEN));

                                    return 1;
                                })))
                .then(Commands.literal("church")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var name = StringArgumentType.getString(context, "name");

                                    if (!BeyonderData.factionStorage.isNameUnique(name)) {
                                        source.sendFailure(Component.literal("Name is already taken!"));
                                        return 0;
                                    }

                                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                                        source.sendFailure(Component.literal("Player must use this command!"));
                                        return 0;
                                    }

                                    if (BeyonderData.factionStorage.hasAnyByType(player.getName().getString(), 2)) {
                                        source.sendFailure(Component.literal("You are already part of such type of faction!"));
                                        return 0;
                                    }

                                    if (BeyonderData.getSequence(player) > FactionCore.getMinSeqChurch()) {
                                        source.sendFailure(Component.literal("You have to be at least " + FactionCore.getMinSeqChurch() + " sequence!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.createFaction(player.getName().getString(), name, 2);

                                    source.sendSystemMessage(Component.literal("Successfully created faction named: \"" + name + "\"" + '\n').withStyle(ChatFormatting.GREEN));

                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("get")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("Must be a player!"));
                        return 0;
                    }

                    var result = BeyonderData.factionStorage.getPartOfFaction(player.getName().getString());

                    if (result.isEmpty()) {
                        source.sendFailure(Component.literal("You are not in any faction!"));
                        return 0;
                    }

                    StringBuilder builder = new StringBuilder("You are part of this factions:\n");

                    for (var faction : result) {
                        builder.append(faction.getShortInfo()).append("\n---------\n");
                    }

                    source.sendSystemMessage(Component.literal(
                            builder.toString() + "\n"));

                    return 1;
                })
                .then(Commands.literal("nation")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(Component.literal("Must be a player!"));
                                return 0;
                            }

                            var result = BeyonderData.factionStorage.getPartOfFactionType(player.getName().getString(), 1);

                            if (result == null) {
                                source.sendFailure(Component.literal("You are not in any faction!"));
                                return 0;
                            }

                            source.sendSystemMessage(Component.literal(
                                    "You are part of this nation:\n" + result.getAllInfo() + "\n"));

                            return 1;
                        })
                )
                .then(Commands.literal("church")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(Component.literal("Must be a player!"));
                                return 0;
                            }

                            var result = BeyonderData.factionStorage.getPartOfFactionType(player.getName().getString(), 2);

                            if (result == null) {
                                source.sendFailure(Component.literal("You are not in any faction!"));
                                return 0;
                            }

                            source.sendSystemMessage(Component.literal(
                                    "You are part of this church:\n" + result.getAllInfo() + "\n"));

                            return 1;
                        })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> claim() {
        return Commands.literal("claim")
                .then(Commands.literal("nation")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 9))
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }
                                            ServerLevel serverLevel = source.getLevel();
                                            if (serverLevel.dimension() != Level.OVERWORLD) {
                                                source.sendFailure(Component.literal("Any claim must be in the overworld!"));
                                                return 0;
                                            }

                                            int level = IntegerArgumentType.getInteger(context, "level");
                                            var pos = source.getLevel().getChunk(player.blockPosition()).getPos();

                                            if (BeyonderData.factionStorage.isClaimedType(pos, 1)) {
                                                source.sendFailure(Component.literal("Chunk is already claimed!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();
                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                return 0;
                                            }

                                            int playerLevel = faction.getPlayerLevel(name);

                                            if (playerLevel == 1 || playerLevel < level) {
                                                source.sendFailure(Component.literal("You don`t have permission to claim this chunk!"));
                                                return 0;
                                            }

                                            if (!BeyonderData.factionStorage.canClaimNation(faction.getId(), pos)) {
                                                source.sendFailure(Component.literal("Your faction must have claimed chunks nearby!"));
                                                return 0;
                                            }

                                            if (faction.getClaimed().size() + 1 > FactionCore.getClaimsPerLevelNation(faction.getLevel())) {
                                                source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                return 0;
                                            }

                                            BeyonderData.factionStorage.claim(faction.getId(), pos, level);

                                            source.sendSystemMessage(Component.literal("Successfully claimed chunk at " + pos.toString() + " with level " + level + '\n').withStyle(ChatFormatting.GREEN));

                                            return 1;
                                        }
                                )))
                .then(Commands.literal("church")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 9))
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }
                                            ServerLevel serverLevel = source.getLevel();
                                            if (serverLevel.dimension() != Level.OVERWORLD) {
                                                source.sendFailure(Component.literal("Any claim must be in the overworld!"));
                                                return 0;
                                            }

                                            int level = IntegerArgumentType.getInteger(context, "level");
                                            var pos = source.getLevel().getChunk(player.blockPosition()).getPos();

                                            if (BeyonderData.factionStorage.isClaimedType(pos, 2)) {
                                                source.sendFailure(Component.literal("Chunk is already claimed!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();
                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                return 0;
                                            }

                                            var factions = BeyonderData.factionStorage.getFaction(pos);
                                            if (factions.isEmpty()) {
                                                source.sendFailure(Component.literal("Church can claim chunks only in nation!"));
                                                return 0;
                                            }

                                            var nation = factions.stream().filter(obj -> obj.getType() == 1).findFirst().get();

                                            if (!faction.hasPermission(nation.getId())) {
                                                source.sendFailure(Component.literal("Your church does not have permission to be in this nation"));
                                                return 0;
                                            }

                                            int playerLevel = faction.getPlayerLevel(name);

                                            if (playerLevel == 1 || playerLevel < level || nation.getClaimLevel(pos) >= 2) {
                                                source.sendFailure(Component.literal("You don`t have permission to claim this chunk!"));
                                                return 0;
                                            }

                                            if (faction.getClaimed().size() + 1 > FactionCore.getClaimsPerLevelChurch(faction.getLevel())) {
                                                source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                return 0;
                                            }

                                            BeyonderData.factionStorage.claim(faction.getId(), pos, level);

                                            source.sendSystemMessage(Component.literal("Successfully claimed chunk at " + pos.toString() + " with level " + level + '\n').withStyle(ChatFormatting.GREEN));

                                            return 1;
                                        }
                                )));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> here() {
        return Commands.literal("here")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("Must be a player!"));
                        return 0;
                    }

                    var pos = source.getLevel().getChunk(player.blockPosition()).getPos();
                    var result = BeyonderData.factionStorage.getFaction(pos);

                    if (result.isEmpty()) {
                        source.sendFailure(Component.literal("Not claimed area!"));
                        return 1;
                    }

                    StringBuilder builder = new StringBuilder("Claimed by:\n");

                    for (var faction : result) {
                        builder.append(faction.getShortInfo()).append("\n--------\n");
                    }

                    source.sendSystemMessage(Component.literal(
                            builder.toString() + "\n"));

                    return 1;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> permission() {
        return Commands.literal("permission")
                .then(Commands.literal("add")
                        .then(Commands.argument("id", IntegerArgumentType.integer())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    int id = IntegerArgumentType.getInteger(context, "id");
                                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();
                                    var nation = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                    if (nation == null) {
                                        source.sendFailure(Component.literal("You must be part of nation!"));
                                        return 0;
                                    }

                                    if (nation.getPlayerLevel(name) < 8) {
                                        source.sendFailure(Component.literal("You have insufficient permission level!"));
                                        return 0;
                                    }

                                    if (!BeyonderData.factionStorage.contains(id)) {
                                        source.sendFailure(Component.literal("Incorrect id!"));
                                        return 0;
                                    }

                                    var church = BeyonderData.factionStorage.getFaction(id);
                                    if (church.getType() == 1) {
                                        source.sendFailure(Component.literal("You can grant permissions only to church type factions!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.addPermission(church.getId(), nation.getId());

                                    source.sendSystemMessage(Component.literal("Successfully granted permission for \"" + church.getName() + "\""));

                                    return 1;
                                })))
                .then(Commands.literal("remove")
                        .then(Commands.argument("id", IntegerArgumentType.integer())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    int id = IntegerArgumentType.getInteger(context, "id");
                                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();
                                    var nation = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                    if (nation == null) {
                                        source.sendFailure(Component.literal("You must be part of nation!"));
                                        return 0;
                                    }

                                    if (nation.getPlayerLevel(name) < 8) {
                                        source.sendFailure(Component.literal("You have insufficient permission level!"));
                                        return 0;
                                    }

                                    if (!BeyonderData.factionStorage.contains(id)) {
                                        source.sendFailure(Component.literal("Incorrect id!"));
                                        return 0;
                                    }

                                    var church = BeyonderData.factionStorage.getFaction(id);
                                    BeyonderData.factionStorage.removePermission(church.getId(), nation.getId());

                                    source.sendSystemMessage(Component.literal("Successfully removed permission from \"" + church.getName() + "\""));

                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> unclaim() {
        return Commands.literal("unclaim")
                .then(Commands.literal("nation")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    var pos = source.getLevel().getChunk(player.blockPosition()).getPos();

                                    var factions = BeyonderData.factionStorage.getFaction(pos);

                                    String name = player.getName().getString();
                                    boolean partOf = false;
                                    for (var obj : factions) {
                                        if (obj.isPartOfFaction(name) && obj.getType() == 1)
                                            partOf = true;
                                    }

                                    if (!partOf) {
                                        source.sendFailure(Component.literal("You are not part of this factions OR not claimed!"));
                                        return 0;
                                    }

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                        return 0;
                                    }

                                    int playerLevel = faction.getPlayerLevel(name);

                                    if (playerLevel == 1 || playerLevel < faction.getClaimLevel(pos)) {
                                        source.sendFailure(Component.literal("You don`t have permission to claim this chunk!"));
                                        return 0;
                                    }

                                    if (faction.getCore().equals(pos)) {
                                        source.sendFailure(Component.literal("You can't unclaim the core chunk!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.unclaim(faction.getId(), pos);

                                    source.sendSystemMessage(Component.literal("Successfully unclaimed chunk at " + pos.toString() + '\n').withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ))
                .then(Commands.literal("church")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    var pos = source.getLevel().getChunk(player.blockPosition()).getPos();

                                    var factions = BeyonderData.factionStorage.getFaction(pos);

                                    String name = player.getName().getString();
                                    boolean partOf = false;
                                    for (var obj : factions) {
                                        if (obj.isPartOfFaction(name) && obj.getType() == 2)
                                            partOf = true;
                                    }

                                    if (!partOf) {
                                        source.sendFailure(Component.literal("You are not part of this factions OR not claimed!"));
                                        return 0;
                                    }

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                        return 0;
                                    }

                                    var nation = factions.stream().filter(obj -> obj.getType() == 1).findFirst().get();

                                    if (!faction.hasPermission(nation.getId())) {
                                        source.sendFailure(Component.literal("Your church does not have permission to be in this nation"));
                                        return 0;
                                    }

                                    int playerLevel = faction.getPlayerLevel(name);

                                    if (playerLevel == 1 || playerLevel < faction.getClaimLevel(pos)) {
                                        source.sendFailure(Component.literal("You don`t have permission to claim this chunk!"));
                                        return 0;
                                    }

                                    if (faction.getCore().equals(pos)) {
                                        source.sendFailure(Component.literal("You can't unclaim the core chunk!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.unclaim(faction.getId(), pos);

                                    source.sendSystemMessage(Component.literal("Successfully unclaimed chunk at " + pos.toString() + '\n').withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> map() {
        return Commands.literal("map")
                .then(Commands.literal("nation")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(Component.literal("Must be a player!"));
                                return 0;
                            }

                            ChunkPos center = player.chunkPosition();

                            int width = 25;
                            int height = 15;

                            int startX = center.x - width / 2;
                            int startZ = center.z - height / 2;

                            player.sendSystemMessage(Component.literal("\nNation Mode\n \"-\" - unclaimed, \"N\" - level of claim, \"P\" - player, \"C\" - core\n"
                                    + "----------------------------------------------").withStyle(ChatFormatting.GREEN));

                            for (int z = startZ; z < startZ + height; z++) {
                                MutableComponent line = Component.empty();

                                for (int x = startX; x < startX + width; x++) {
                                    ChunkPos pos = new ChunkPos(x, z);

                                    boolean claimed = BeyonderData.factionStorage.isClaimed(pos, 1);

                                    String symbol = "-";
                                    ChatFormatting format = ChatFormatting.DARK_GRAY;

                                    if (BeyonderData.factionStorage.isCore(pos, 1)) {
                                        symbol = "C";
                                    } else if (claimed) {
                                        symbol = ("" + BeyonderData.factionStorage.getClaimLevel(pos, 1));
                                    } else if (pos.equals(center))
                                        symbol = "P";

                                    if (claimed) {
                                        format = ChatFormatting.GREEN;
                                    }
                                    if (pos.equals(center)) {
                                        format = ChatFormatting.GOLD;
                                    }

                                    line.append(Component.literal(symbol).withStyle(format));
                                }

                                player.sendSystemMessage(line);
                            }

                            player.sendSystemMessage(Component.literal("\n"));

                            return 1;
                        }))
                .then(Commands.literal("church")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(Component.literal("Must be a player!"));
                                return 0;
                            }

                            ChunkPos center = player.chunkPosition();

                            int width = 25;
                            int height = 15;

                            int startX = center.x - width / 2;
                            int startZ = center.z - height / 2;

                            player.sendSystemMessage(Component.literal("\nChurch Mode\n \"-\" - unclaimed, \"N\" - level of claim, \"P\" - player, \"C\" - core\n"
                                    + "----------------------------------------------").withStyle(ChatFormatting.GREEN));

                            for (int z = startZ; z < startZ + height; z++) {
                                MutableComponent line = Component.empty();

                                for (int x = startX; x < startX + width; x++) {
                                    ChunkPos pos = new ChunkPos(x, z);

                                    boolean claimed = BeyonderData.factionStorage.isClaimed(pos, 2);

                                    String symbol = "-";
                                    ChatFormatting format = ChatFormatting.DARK_GRAY;

                                    if (BeyonderData.factionStorage.isCore(pos, 2)) {
                                        symbol = "C";
                                    } else if (claimed) {
                                        symbol = ("" + BeyonderData.factionStorage.getClaimLevel(pos, 2));
                                    } else if (pos.equals(center))
                                        symbol = "P";

                                    if (claimed) {
                                        format = ChatFormatting.BLUE;
                                    }
                                    if (pos.equals(center)) {
                                        format = ChatFormatting.GOLD;
                                    }

                                    line.append(Component.literal(symbol).withStyle(format));
                                }

                                player.sendSystemMessage(line);
                            }

                            player.sendSystemMessage(Component.literal("\n"));

                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> disband() {
        return Commands.literal("disband")
                .then(Commands.literal("nation")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                        return 0;
                                    }

                                    if (!faction.getLeader().equals(name)) {
                                        source.sendFailure(Component.literal("You must be faction leader!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.disband(faction.getId());

                                    source.sendSystemMessage(Component.literal("Successfully disbanded \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ))
                .then(Commands.literal("church")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                        return 0;
                                    }

                                    if (!faction.getLeader().equals(name)) {
                                        source.sendFailure(Component.literal("You must be faction leader!"));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.disband(faction.getId());

                                    source.sendSystemMessage(Component.literal("Successfully disbanded \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> invite() {
        return Commands.literal("invite")
                .then(Commands.literal("nation")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();

                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You must be part of faction!"));
                                                return 0;
                                            }

                                            if (faction.getPlayerLevel(name) <= 1) {
                                                source.sendFailure(Component.literal("You don't have permission to invite people!"));
                                                return 0;
                                            }

                                            var target = EntityArgument.getEntity(context, "target");
                                            if (!(target instanceof ServerPlayer)) {
                                                source.sendFailure(Component.literal("Target must be a player!"));
                                                return 0;
                                            }

                                            if (faction.isPartOfFaction(target.getName().getString())) {
                                                source.sendFailure(Component.literal("Target is part of faction!"));
                                                return 0;
                                            }

                                            if (target.equals(player)) {
                                                source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                return 0;
                                            }

                                            if (faction.getAllCitizens().size() + 1 > FactionCore.getCitizensAmountPerLevel(faction.getLevel())) {
                                                source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                return 0;
                                            }

                                            inviteMap.put(target.getUUID(), faction.getId());

                                            source.sendSystemMessage(Component.literal("Successfully invited " + target.getName().getString() +
                                                    " into \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                            target.sendSystemMessage(
                                                    Component.literal(name + " invited you to \"" + faction.getName() + "\"\n")
                                                            .append(accept)
                                                            .append(Component.literal(" | "))
                                                            .append(decline)
                                            );

                                            return 1;
                                        }
                                ))
                )
                .then(Commands.literal("church")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();

                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You must be part of faction!"));
                                                return 0;
                                            }

                                            if (faction.getPlayerLevel(name) <= 1) {
                                                source.sendFailure(Component.literal("You don't have permission to invite people!"));
                                                return 0;
                                            }

                                            var target = EntityArgument.getEntity(context, "target");
                                            if (!(target instanceof ServerPlayer)) {
                                                source.sendFailure(Component.literal("Target must be a player!"));
                                                return 0;
                                            }

                                            if (faction.isPartOfFaction(target.getName().getString())) {
                                                source.sendFailure(Component.literal("Target is part of faction!"));
                                                return 0;
                                            }

                                            if (target.equals(player)) {
                                                source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                return 0;
                                            }

                                            if (faction.getAllCitizens().size() + 1 > FactionCore.getCitizensAmountPerLevel(faction.getLevel())) {
                                                source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                return 0;
                                            }

                                            inviteMap.put(target.getUUID(), faction.getId());

                                            source.sendSystemMessage(Component.literal("Successfully invited " + target.getName().getString() +
                                                    " into \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                            target.sendSystemMessage(
                                                    Component.literal(name + " invited you to \"" + faction.getName() + "\"\n")
                                                            .append(accept)
                                                            .append(Component.literal(" | "))
                                                            .append(decline)
                                            );

                                            return 1;
                                        }
                                ))
                )
                .then(Commands.literal("accept")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var player = source.getPlayer();
                            if (player == null) {
                                source.sendFailure(Component.literal("Must be a player!"));
                                return 0;
                            }

                            String name = player.getName().getString();

                            if (!inviteMap.containsKey(player.getUUID())) {
                                source.sendFailure(Component.literal("You don't have any pending invites!"));
                                return 0;
                            }

                            var faction = BeyonderData.factionStorage.getFaction(inviteMap.get(player.getUUID()));
                            if (faction == null) {
                                source.sendFailure(Component.literal("Faction cannot be found!"));
                                inviteMap.remove(player.getUUID());
                                return 0;
                            }

                            var partOf = BeyonderData.factionStorage.getPartOfFactionType(name, faction.getType());
                            if (partOf != null) {
                                source.sendFailure(Component.literal("You must leave your current faction to join another!"));
                                return 0;
                            }

                            var citizens = faction.getAllCitizens();
                            if (citizens.size() + 1 > FactionCore.getCitizensAmountPerLevel(faction.getLevel())) {
                                source.sendFailure(Component.literal("Faction is out of slots"));
                                return 0;
                            }

                            var allPlayers = faction.getAllPlayers();

                            BeyonderData.factionStorage.addCitizen(faction.getId(), name);

                            inviteMap.remove(player.getUUID());

                            for (var obj : allPlayers) {
                                var target = source.getLevel().getPlayerByUUID(Objects.requireNonNull(BeyonderData.playerMap.getKeyByName(obj)));
                                if (target == null) continue;

                                target.sendSystemMessage(Component.literal(name + " joined \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                            }

                            source.sendSystemMessage(Component.literal("Successfully joined into \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                            return 1;
                        })
                )
                .then(Commands.literal("decline")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var player = source.getPlayer();
                            if (player == null) {
                                source.sendFailure(Component.literal("Must be a player!"));
                                return 0;
                            }

                            String name = player.getName().getString();

                            if (!inviteMap.containsKey(player.getUUID())) {
                                source.sendFailure(Component.literal("You don't have any pending invites!"));
                                return 0;
                            }

                            inviteMap.remove(player.getUUID());

                            source.sendSystemMessage(Component.literal("Successfully declined\n").withStyle(ChatFormatting.GREEN));

                            return 1;
                        })
                )
                ;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leave() {
        return Commands.literal("leave")
                .then(Commands.literal("nation")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                        return 0;
                                    }

                                    if (faction.getLeader().equals(name)) {
                                        BeyonderData.factionStorage.disband(faction.getId());
                                        source.sendSystemMessage(Component.literal("Successfully disbanded \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.leave(faction.getId(), name);

                                    source.sendSystemMessage(Component.literal("Successfully left \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ))
                .then(Commands.literal("church")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                        return 0;
                                    }

                                    if (faction.getLeader().equals(name)) {
                                        BeyonderData.factionStorage.disband(faction.getId());
                                        source.sendSystemMessage(Component.literal("Successfully disbanded \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                                        return 0;
                                    }
                                    BeyonderData.factionStorage.leave(faction.getId(), name);

                                    source.sendSystemMessage(Component.literal("Successfully left \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> kick() {
        return Commands.literal("kick")
                .then(Commands.literal("nation")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();

                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You must be part of faction!"));
                                                return 0;
                                            }

                                            int level = faction.getPlayerLevel(name);
                                            if (level <= 1) {
                                                source.sendFailure(Component.literal("You don't have permission to kick people!"));
                                                return 0;
                                            }

                                            var target = EntityArgument.getEntity(context, "target");
                                            if (!(target instanceof ServerPlayer)) {
                                                source.sendFailure(Component.literal("Target must be a player!"));
                                                return 0;
                                            }

                                            if (!faction.isPartOfFaction(target.getName().getString())) {
                                                source.sendFailure(Component.literal("Target must be part of faction!"));
                                                return 0;
                                            }

                                            int targetLevel = faction.getPlayerLevel(target.getName().getString());
                                            if (targetLevel == 9 || targetLevel >= level) {
                                                source.sendFailure(Component.literal("You can't kick target!"));
                                                return 0;
                                            }

                                            if (target.equals(player)) {
                                                source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                return 0;
                                            }

                                            BeyonderData.factionStorage.leave(faction.getId(), target.getName().getString());

                                            source.sendSystemMessage(Component.literal("Successfully kicked " + target.getName().getString() + "\n").withStyle(ChatFormatting.GREEN));

                                            target.sendSystemMessage(Component.literal("You were kicked from \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.RED));

                                            return 1;
                                        }
                                ))
                )
                .then(Commands.literal("church")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();

                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You must be part of faction!"));
                                                return 0;
                                            }

                                            int level = faction.getPlayerLevel(name);
                                            if (level <= 1) {
                                                source.sendFailure(Component.literal("You don't have permission to kick people!"));
                                                return 0;
                                            }

                                            var target = EntityArgument.getEntity(context, "target");
                                            if (!(target instanceof ServerPlayer)) {
                                                source.sendFailure(Component.literal("Target must be a player!"));
                                                return 0;
                                            }

                                            if (!faction.isPartOfFaction(target.getName().getString())) {
                                                source.sendFailure(Component.literal("Target must be part of faction!"));
                                                return 0;
                                            }

                                            int targetLevel = faction.getPlayerLevel(target.getName().getString());
                                            if (targetLevel == 9 || targetLevel >= level) {
                                                source.sendFailure(Component.literal("You can't kick target!"));
                                                return 0;
                                            }

                                            if (target.equals(player)) {
                                                source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                return 0;
                                            }

                                            BeyonderData.factionStorage.leave(faction.getId(), target.getName().getString());

                                            source.sendSystemMessage(Component.literal("Successfully kicked " + target.getName().getString() + "\n").withStyle(ChatFormatting.GREEN));

                                            target.sendSystemMessage(Component.literal("You were kicked from \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.RED));


                                            return 1;
                                        }
                                ))
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> promote() {
        return Commands.literal("promote")
                .then(Commands.literal("nation")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 8))
                                        .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var player = source.getPlayer();
                                                    if (player == null) {
                                                        source.sendFailure(Component.literal("Must be a player!"));
                                                        return 0;
                                                    }

                                                    String name = player.getName().getString();

                                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                                    if (faction == null) {
                                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                                        return 0;
                                                    }

                                                    int level = faction.getPlayerLevel(name);
                                                    int promoteLevel = IntegerArgumentType.getInteger(context, "level");

                                                    if (level <= 4) {
                                                        source.sendFailure(Component.literal("You don't have permission to promote people!"));
                                                        return 0;
                                                    }

                                                    var target = EntityArgument.getEntity(context, "target");
                                                    if (!(target instanceof ServerPlayer)) {
                                                        source.sendFailure(Component.literal("Target must be a player!"));
                                                        return 0;
                                                    }

                                                    String targetName = target.getName().getString();

                                                    if (!faction.isPartOfFaction(targetName)) {
                                                        source.sendFailure(Component.literal("Target must be part of faction!"));
                                                        return 0;
                                                    }

                                                    int targetLevel = faction.getPlayerLevel(target.getName().getString());
                                                    if (targetLevel == 9 || targetLevel >= level) {
                                                        source.sendFailure(Component.literal("You can't promote target!"));
                                                        return 0;
                                                    }

                                                    if (promoteLevel >= level) {
                                                        source.sendFailure(Component.literal("You can't promote target to such level!"));
                                                        return 0;
                                                    }

                                                    if (target.getUUID().equals(player.getUUID())) {
                                                        source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                        return 0;
                                                    }

                                                    if (promoteLevel == 1) {
                                                        if (faction.getAllCitizens().size() + 1 > FactionCore.getCitizensAmountPerLevel(faction.getLevel())) {
                                                            source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                            return 0;
                                                        }
                                                    } else if (promoteLevel > 1 && promoteLevel < 8) {
                                                        if (faction.getAllNobles().size() + 1 > FactionCore.getNoblesAmountPerLevel(faction.getLevel())) {
                                                            source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                            return 0;
                                                        }
                                                    } else if (promoteLevel == 8) {
                                                        if (faction.getAllCoLeaders().size() + 1 > FactionCore.getCoLeadersAmountPerLevel(faction.getLevel())) {
                                                            source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                            return 0;
                                                        }
                                                    }

                                                    BeyonderData.factionStorage.promote(faction.getId(), target.getName().getString(), promoteLevel);

                                                    source.sendSystemMessage(Component.literal("Successfully promoted " + target.getName().getString() + " to " + promoteLevel + "\n").withStyle(ChatFormatting.GREEN));

                                                    target.sendSystemMessage(Component.literal("You were promoted in \"" + faction.getName() + "\" to " + promoteLevel + "\n").withStyle(ChatFormatting.GREEN));

                                                    return 1;
                                                }
                                        )))
                )
                .then(Commands.literal("church")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 8))
                                        .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var player = source.getPlayer();
                                                    if (player == null) {
                                                        source.sendFailure(Component.literal("Must be a player!"));
                                                        return 0;
                                                    }

                                                    String name = player.getName().getString();

                                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                                    if (faction == null) {
                                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                                        return 0;
                                                    }

                                                    int level = faction.getPlayerLevel(name);
                                                    int promoteLevel = IntegerArgumentType.getInteger(context, "level");

                                                    if (level <= 4) {
                                                        source.sendFailure(Component.literal("You don't have permission to promote people!"));
                                                        return 0;
                                                    }

                                                    var target = EntityArgument.getEntity(context, "target");
                                                    if (!(target instanceof ServerPlayer)) {
                                                        source.sendFailure(Component.literal("Target must be a player!"));
                                                        return 0;
                                                    }

                                                    if (target.getUUID().equals(player.getUUID())) {
                                                        source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                        return 0;
                                                    }

                                                    String targetName = target.getName().getString();

                                                    if (!faction.isPartOfFaction(targetName)) {
                                                        source.sendFailure(Component.literal("Target must be part of faction!"));
                                                        return 0;
                                                    }

                                                    int targetLevel = faction.getPlayerLevel(target.getName().getString());
                                                    if (targetLevel == 9 || targetLevel >= level) {
                                                        source.sendFailure(Component.literal("You can't promote target!"));
                                                        return 0;
                                                    }

                                                    if (promoteLevel >= level) {
                                                        source.sendFailure(Component.literal("You can't promote target to such level!"));
                                                        return 0;
                                                    }

                                                    if (target.equals(player)) {
                                                        source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                        return 0;
                                                    }

                                                    if (promoteLevel == 1) {
                                                        if (faction.getAllCitizens().size() + 1 > FactionCore.getCitizensAmountPerLevel(faction.getLevel())) {
                                                            source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                            return 0;
                                                        }
                                                    } else if (promoteLevel > 1 && promoteLevel < 8) {
                                                        if (faction.getAllNobles().size() + 1 > FactionCore.getNoblesAmountPerLevel(faction.getLevel())) {
                                                            source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                            return 0;
                                                        }
                                                    } else if (promoteLevel == 8) {
                                                        if (faction.getAllCoLeaders().size() + 1 > FactionCore.getCoLeadersAmountPerLevel(faction.getLevel())) {
                                                            source.sendFailure(Component.literal("Your faction is out of slots for this operation!"));
                                                            return 0;
                                                        }
                                                    }

                                                    BeyonderData.factionStorage.promote(faction.getId(), target.getName().getString(), promoteLevel);

                                                    source.sendSystemMessage(Component.literal("Successfully promoted " + target.getName().getString() + " to " + promoteLevel + "\n").withStyle(ChatFormatting.GREEN));

                                                    target.sendSystemMessage(Component.literal("You were promoted in \"" + faction.getName() + "\" to " + promoteLevel + "\n").withStyle(ChatFormatting.GREEN));

                                                    return 1;
                                                }
                                        )))
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> passLeadership() {
        return Commands.literal("pass_leadership")
                .then(Commands.literal("nation")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();

                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You must be part of faction!"));
                                                return 0;
                                            }

                                            if (!faction.getLeader().equals(name)) {
                                                source.sendFailure(Component.literal("You are not the faction leader!"));
                                                return 0;
                                            }

                                            var target = EntityArgument.getEntity(context, "target");
                                            if (!(target instanceof ServerPlayer)) {
                                                source.sendFailure(Component.literal("Target must be a player!"));
                                                return 0;
                                            }

                                            if (!faction.isPartOfFaction(target.getName().getString())) {
                                                source.sendFailure(Component.literal("Target must be part of faction!"));
                                                return 0;
                                            }

                                            if (target.equals(player)) {
                                                source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                return 0;
                                            }

                                            BeyonderData.factionStorage.promote(faction.getId(), name, 1);
                                            BeyonderData.factionStorage.promote(faction.getId(), target.getName().getString(), 9);

                                            source.sendSystemMessage(Component.literal("Successfully passed leadership to " + target.getName().getString() + "\n").withStyle(ChatFormatting.GREEN));

                                            target.sendSystemMessage(Component.literal("You are now a leader of \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                            return 1;
                                        }
                                ))
                )
                .then(Commands.literal("church")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var player = source.getPlayer();
                                            if (player == null) {
                                                source.sendFailure(Component.literal("Must be a player!"));
                                                return 0;
                                            }

                                            String name = player.getName().getString();

                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                            if (faction == null) {
                                                source.sendFailure(Component.literal("You must be part of faction!"));
                                                return 0;
                                            }

                                            if (!faction.getLeader().equals(name)) {
                                                source.sendFailure(Component.literal("You are not the faction leader!"));
                                                return 0;
                                            }

                                            var target = EntityArgument.getEntity(context, "target");
                                            if (!(target instanceof ServerPlayer)) {
                                                source.sendFailure(Component.literal("Target must be a player!"));
                                                return 0;
                                            }

                                            if (!faction.isPartOfFaction(target.getName().getString())) {
                                                source.sendFailure(Component.literal("Target must be part of faction!"));
                                                return 0;
                                            }

                                            if (target.equals(player)) {
                                                source.sendFailure(Component.literal("You can't perform this operation on yourself!"));
                                                return 0;
                                            }

                                            BeyonderData.factionStorage.promote(faction.getId(), name, 1);
                                            BeyonderData.factionStorage.promote(faction.getId(), target.getName().getString(), 9);

                                            source.sendSystemMessage(Component.literal("Successfully passed leadership to " + target.getName().getString() + "\n").withStyle(ChatFormatting.GREEN));

                                            target.sendSystemMessage(Component.literal("You are now leader of \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                            return 1;
                                        }
                                ))
                );
    }


    private static LiteralArgumentBuilder<CommandSourceStack> bank() {
        return Commands.literal("bank")
                .then(Commands.literal("nation")
                        .then(Commands.literal("deposit")
                                .then(Commands.argument("soli", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("pound", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            var player = source.getPlayer();
                                                            if (player == null) {
                                                                source.sendFailure(Component.literal("Must be a player!"));
                                                                return 0;
                                                            }

                                                            int soli = IntegerArgumentType.getInteger(context, "soli");
                                                            int pounds = IntegerArgumentType.getInteger(context, "pound");

                                                            String name = player.getName().getString();
                                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                                            if (faction == null) {
                                                                source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                                return 0;
                                                            }

                                                            int removedSoli = 0;
                                                            int removedPound = 0;

                                                            if (soli > 0) {
                                                                removedSoli = player.getInventory().clearOrCountMatchingItems(
                                                                        stack -> stack.is(ModItems.ONE_SOLI),
                                                                        soli,
                                                                        player.inventoryMenu.getCraftSlots()
                                                                );
                                                            }

                                                            if (pounds > 0) {
                                                                removedPound = player.getInventory().clearOrCountMatchingItems(
                                                                        stack -> stack.is(ModItems.ONE_POUND),
                                                                        pounds,
                                                                        player.inventoryMenu.getCraftSlots()
                                                                );
                                                            }

                                                            player.containerMenu.broadcastChanges();

                                                            faction.setSoli(faction.getSoli() + removedSoli);
                                                            faction.setPound(faction.getPound() + removedPound);

                                                            BeyonderData.factionStorage.setFaction(faction.getId(), faction);

                                                            source.sendSystemMessage(Component.literal("Successfully deposit " + removedSoli + " soli and " + removedPound + " pounds" + '\n').withStyle(ChatFormatting.GREEN));

                                                            return 1;
                                                        }
                                                ))))
                        .then(Commands.literal("withdrew")
                                .then(Commands.argument("soli", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("pound", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            var player = source.getPlayer();
                                                            if (player == null) {
                                                                source.sendFailure(Component.literal("Must be a player!"));
                                                                return 0;
                                                            }

                                                            int soli = IntegerArgumentType.getInteger(context, "soli");
                                                            int pounds = IntegerArgumentType.getInteger(context, "pound");

                                                            String name = player.getName().getString();
                                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                                            if (faction == null) {
                                                                source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                                return 0;
                                                            }

                                                            if (faction.getPlayerLevel(name) <= 7) {
                                                                source.sendFailure(Component.literal("You don't have permission to withdrew!"));
                                                                return 0;
                                                            }

                                                            int soliR = Math.min(soli, faction.getSoli());
                                                            int poundsR = Math.min(pounds, faction.getPound());

                                                            faction.setSoli(faction.getSoli() - soliR);
                                                            faction.setPound(faction.getPound() - poundsR);

                                                            BeyonderData.factionStorage.setFaction(faction.getId(), faction);

                                                            int remainingSoli = soliR;
                                                            while (remainingSoli > 0) {
                                                                int give = Math.min(remainingSoli, ModItems.ONE_SOLI.get().getDefaultMaxStackSize());
                                                                ItemHandlerHelper.giveItemToPlayer(
                                                                        player,
                                                                        new ItemStack(ModItems.ONE_SOLI.get(), give)
                                                                );
                                                                remainingSoli -= give;
                                                            }

                                                            int remainingPounds = poundsR;
                                                            while (remainingPounds > 0) {
                                                                int give = Math.min(remainingPounds, ModItems.ONE_POUND.get().getDefaultMaxStackSize());
                                                                ItemHandlerHelper.giveItemToPlayer(
                                                                        player,
                                                                        new ItemStack(ModItems.ONE_POUND.get(), give)
                                                                );
                                                                remainingPounds -= give;
                                                            }

                                                            source.sendSystemMessage(Component.literal("Successfully withdrew " + soliR + " soli and " + poundsR + " pounds" + '\n').withStyle(ChatFormatting.GREEN));

                                                            return 1;
                                                        }
                                                ))
                                )))
                .then(Commands.literal("church")
                        .then(Commands.literal("deposit")
                                .then(Commands.argument("soli", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("pound", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            var player = source.getPlayer();
                                                            if (player == null) {
                                                                source.sendFailure(Component.literal("Must be a player!"));
                                                                return 0;
                                                            }

                                                            int soli = IntegerArgumentType.getInteger(context, "soli");
                                                            int pounds = IntegerArgumentType.getInteger(context, "pound");

                                                            String name = player.getName().getString();
                                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                                            if (faction == null) {
                                                                source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                                return 0;
                                                            }

                                                            int removedSoli = 0;
                                                            int removedPound = 0;

                                                            if (soli > 0) {
                                                                removedSoli = player.getInventory().clearOrCountMatchingItems(
                                                                        stack -> stack.is(ModItems.ONE_SOLI),
                                                                        soli,
                                                                        player.inventoryMenu.getCraftSlots()
                                                                );
                                                            }

                                                            if (pounds > 0) {
                                                                removedPound = player.getInventory().clearOrCountMatchingItems(
                                                                        stack -> stack.is(ModItems.ONE_POUND),
                                                                        pounds,
                                                                        player.inventoryMenu.getCraftSlots()
                                                                );
                                                            }

                                                            player.containerMenu.broadcastChanges();

                                                            faction.setSoli(faction.getSoli() + removedSoli);
                                                            faction.setPound(faction.getPound() + removedPound);

                                                            BeyonderData.factionStorage.setFaction(faction.getId(), faction);

                                                            source.sendSystemMessage(Component.literal("Successfully deposit " + removedSoli + " soli and " + removedPound + " pounds" + '\n').withStyle(ChatFormatting.GREEN));

                                                            return 1;
                                                        }
                                                ))))
                        .then(Commands.literal("withdrew")
                                .then(Commands.argument("soli", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("pound", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            var player = source.getPlayer();
                                                            if (player == null) {
                                                                source.sendFailure(Component.literal("Must be a player!"));
                                                                return 0;
                                                            }

                                                            int soli = IntegerArgumentType.getInteger(context, "soli");
                                                            int pounds = IntegerArgumentType.getInteger(context, "pound");

                                                            String name = player.getName().getString();
                                                            var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                                            if (faction == null) {
                                                                source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                                return 0;
                                                            }

                                                            if (faction.getPlayerLevel(name) <= 7) {
                                                                source.sendFailure(Component.literal("You don't have permission to withdrew!"));
                                                                return 0;
                                                            }

                                                            int soliR = Math.min(soli, faction.getSoli());
                                                            int poundsR = Math.min(pounds, faction.getPound());

                                                            faction.setSoli(faction.getSoli() - soliR);
                                                            faction.setPound(faction.getPound() - poundsR);

                                                            BeyonderData.factionStorage.setFaction(faction.getId(), faction);

                                                            int remainingSoli = soliR;
                                                            while (remainingSoli > 0) {
                                                                int give = Math.min(remainingSoli, ModItems.ONE_SOLI.get().getDefaultMaxStackSize());
                                                                ItemHandlerHelper.giveItemToPlayer(
                                                                        player,
                                                                        new ItemStack(ModItems.ONE_SOLI.get(), give)
                                                                );
                                                                remainingSoli -= give;
                                                            }

                                                            int remainingPounds = poundsR;
                                                            while (remainingPounds > 0) {
                                                                int give = Math.min(remainingPounds, ModItems.ONE_POUND.get().getDefaultMaxStackSize());
                                                                ItemHandlerHelper.giveItemToPlayer(
                                                                        player,
                                                                        new ItemStack(ModItems.ONE_POUND.get(), give)
                                                                );
                                                                remainingPounds -= give;
                                                            }

                                                            source.sendSystemMessage(Component.literal("Successfully withdrew " + soliR + " soli and " + poundsR + " pounds" + '\n').withStyle(ChatFormatting.GREEN));

                                                            return 1;
                                                        }
                                                ))
                                )));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> levelUp() {
        return Commands.literal("levelup")
                .then(Commands.literal("nation")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                        return 0;
                                    }

                                    if (faction.getPlayerLevel(name) < 8) {
                                        source.sendFailure(Component.literal("You don't have permission to execute this command!"));
                                        return 0;
                                    }

                                    if (FactionCore.getMaxLevel() <= faction.getLevel()) {
                                        source.sendFailure(Component.literal("Your faction has the max level!"));
                                        return 0;
                                    }

                                    boolean failed = false;
                                    if (faction.getAllPlayers().size() < FactionCore.getLevelUpPeople(faction.getLevel())) {
                                        failed = true;
                                    } else if (faction.getClaimed().size() < FactionCore.getLevelUpChunks(faction.getLevel())) {
                                        failed = true;
                                    } else if (faction.getSoli() < FactionCore.getLevelUpSoli(faction.getLevel())
                                            || faction.getPound() < FactionCore.getLevelUpPounds(faction.getLevel())) {
                                        failed = true;
                                    }

                                    if (failed) {
                                        source.sendFailure(Component.literal("Your faction has not met the requirements to level up!"
                                                + "\nChunks: " + faction.getClaimed().size() + "/" + FactionCore.getLevelUpChunks(faction.getLevel())
                                                + "\nPeople: " + faction.getAllPlayers().size() + "/" + FactionCore.getLevelUpPeople(faction.getLevel())
                                                + "\nPounds: " + faction.getPound() + "/" + FactionCore.getLevelUpPounds(faction.getLevel())
                                                + " -- Soli: " + faction.getSoli() + "/" + FactionCore.getLevelUpSoli(faction.getLevel()
                                        )));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.levelUp(faction.getId());

                                    source.sendSystemMessage(Component.literal("Successfully leveled up \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ))
                .then(Commands.literal("church")
                        .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Must be a player!"));
                                        return 0;
                                    }

                                    String name = player.getName().getString();

                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                    if (faction == null) {
                                        source.sendFailure(Component.literal("You must be part of faction!"));
                                        return 0;
                                    }

                                    if (faction.getPlayerLevel(name) < 8) {
                                        source.sendFailure(Component.literal("You don't have permission to execute this command!"));
                                        return 0;
                                    }

                                    if (FactionCore.getMaxLevel() <= faction.getLevel()) {
                                        source.sendFailure(Component.literal("Your faction has the max level!"));
                                        return 0;
                                    }

                                    boolean failed = false;
                                    if (faction.getAllPlayers().size() < FactionCore.getLevelUpPeople(faction.getLevel())) {
                                        failed = true;
                                    } else if (faction.getClaimed().size() < FactionCore.getLevelUpChunks(faction.getLevel())) {
                                        failed = true;
                                    } else if (faction.getSoli() < FactionCore.getLevelUpSoli(faction.getLevel())
                                            || faction.getPound() < FactionCore.getLevelUpPounds(faction.getLevel())) {
                                        failed = true;
                                    }

                                    if (failed) {
                                        source.sendFailure(Component.literal("Your faction has not met the requirements to level up!"
                                                + "\nChunks: " + faction.getClaimed().size() + "/" + FactionCore.getLevelUpChunks(faction.getLevel())
                                                + "\nPeople: " + faction.getAllPlayers().size() + "/" + FactionCore.getLevelUpPeople(faction.getLevel())
                                                + "\nPounds: " + faction.getPound() + "/" + FactionCore.getLevelUpPounds(faction.getLevel())
                                                + " -- Soli: " + faction.getSoli() + "/" + FactionCore.getLevelUpSoli(faction.getLevel()
                                        )));
                                        return 0;
                                    }

                                    BeyonderData.factionStorage.levelUp(faction.getId());

                                    source.sendSystemMessage(Component.literal("Successfully leveled up \"" + faction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));

                                    return 1;
                                }
                        ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> war() {
        return Commands.literal("war")
                .then(Commands.literal("nation")
                        .then(Commands.literal("declare")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var player = source.getPlayer();
                                                    if (player == null) {
                                                        source.sendFailure(Component.literal("Must be a player!"));
                                                        return 0;
                                                    }

                                                    int id = IntegerArgumentType.getInteger(context, "id");

                                                    String name = player.getName().getString();
                                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                                    if (faction == null) {
                                                        source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                        return 0;
                                                    }

                                                    int playerLevel = faction.getPlayerLevel(name);

                                                    if (playerLevel < 8) {
                                                        source.sendFailure(Component.literal("You don`t have permission to declare the war!"));
                                                        return 0;
                                                    }

                                                    var targetFaction = BeyonderData.factionStorage.getFaction(id);
                                                    if (targetFaction == null) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id!"));
                                                        return 0;
                                                    }

                                                    if (faction.getId() == id) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id. You can't declare war to yourself!"));
                                                        return 0;
                                                    }

                                                    if (faction.getLevel() == 0) {
                                                        source.sendFailure(Component.literal("Your faction must be at least level 1!"));
                                                        return 0;
                                                    }

                                                    BeyonderData.factionStorage.declareWar(faction.getId(), targetFaction.getId());

                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), faction.getId(), Component.literal("Declared war to \"" + targetFaction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), targetFaction.getId(), Component.literal("\"" + faction.getName() + "\" declared war\n").withStyle(ChatFormatting.RED));

                                                    return 1;
                                                }
                                        ))
                        )
                        .then(Commands.literal("stop")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var player = source.getPlayer();
                                                    if (player == null) {
                                                        source.sendFailure(Component.literal("Must be a player!"));
                                                        return 0;
                                                    }

                                                    int id = IntegerArgumentType.getInteger(context, "id");

                                                    String name = player.getName().getString();
                                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
                                                    if (faction == null) {
                                                        source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                        return 0;
                                                    }

                                                    int playerLevel = faction.getPlayerLevel(name);

                                                    if (playerLevel < 8) {
                                                        source.sendFailure(Component.literal("You don`t have permission to stop the war!"));
                                                        return 0;
                                                    }

                                                    var targetFaction = BeyonderData.factionStorage.getFaction(id);
                                                    if (targetFaction == null) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id!"));
                                                        return 0;
                                                    }

                                                    if (faction.getId() == id) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id. You can't stop war against yourself!"));
                                                        return 0;
                                                    }

                                                    if (!faction.isAggressor(targetFaction.getId())) {
                                                        source.sendFailure(Component.literal("Your faction must be aggressor of the war!"));
                                                        return 0;
                                                    }

                                                    BeyonderData.factionStorage.stopWar(faction.getId(), targetFaction.getId());

                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), faction.getId(), Component.literal("Stopped war against \"" + targetFaction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), targetFaction.getId(), Component.literal("\"" + faction.getName() + "\" stopped war\n").withStyle(ChatFormatting.GREEN));

                                                    return 1;
                                                }
                                        ))
                        )

                ) //nation part end
                .then(Commands.literal("church")
                        .then(Commands.literal("declare")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var player = source.getPlayer();
                                                    if (player == null) {
                                                        source.sendFailure(Component.literal("Must be a player!"));
                                                        return 0;
                                                    }

                                                    int id = IntegerArgumentType.getInteger(context, "id");

                                                    String name = player.getName().getString();
                                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                                    if (faction == null) {
                                                        source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                        return 0;
                                                    }

                                                    int playerLevel = faction.getPlayerLevel(name);

                                                    if (playerLevel < 8) {
                                                        source.sendFailure(Component.literal("You don`t have permission to declare the war!"));
                                                        return 0;
                                                    }

                                                    var targetFaction = BeyonderData.factionStorage.getFaction(id);
                                                    if (targetFaction == null) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id!"));
                                                        return 0;
                                                    }

                                                    if (faction.getId() == id) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id. You can't declare war to yourself!"));
                                                        return 0;
                                                    }

                                                    if (faction.getLevel() == 0) {
                                                        source.sendFailure(Component.literal("Your faction must be at least level 1!"));
                                                        return 0;
                                                    }

                                                    BeyonderData.factionStorage.declareWar(faction.getId(), targetFaction.getId());

                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), faction.getId(), Component.literal("Declared war to \"" + targetFaction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), targetFaction.getId(), Component.literal("\"" + faction.getName() + "\" declared war\n").withStyle(ChatFormatting.RED));

                                                    return 1;
                                                }
                                        ))
                        )
                        .then(Commands.literal("stop")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var player = source.getPlayer();
                                                    if (player == null) {
                                                        source.sendFailure(Component.literal("Must be a player!"));
                                                        return 0;
                                                    }

                                                    int id = IntegerArgumentType.getInteger(context, "id");

                                                    String name = player.getName().getString();
                                                    var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
                                                    if (faction == null) {
                                                        source.sendFailure(Component.literal("You are not part of faction with such type!"));
                                                        return 0;
                                                    }

                                                    int playerLevel = faction.getPlayerLevel(name);

                                                    if (playerLevel < 8) {
                                                        source.sendFailure(Component.literal("You don`t have permission to stop the war!"));
                                                        return 0;
                                                    }

                                                    var targetFaction = BeyonderData.factionStorage.getFaction(id);
                                                    if (targetFaction == null) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id!"));
                                                        return 0;
                                                    }

                                                    if (faction.getId() == id) {
                                                        source.sendFailure(Component.literal("Incorrect target faction id. You can't stop war against yourself!"));
                                                        return 0;
                                                    }

                                                    if (!faction.isAggressor(targetFaction.getId())) {
                                                        source.sendFailure(Component.literal("Your faction must be aggressor of the war!"));
                                                        return 0;
                                                    }

                                                    BeyonderData.factionStorage.stopWar(faction.getId(), targetFaction.getId());

                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), faction.getId(), Component.literal("Stopped war against \"" + targetFaction.getName() + "\"\n").withStyle(ChatFormatting.GREEN));
                                                    BeyonderData.factionStorage.messageEveryoneInFaction(source.getLevel(), targetFaction.getId(), Component.literal("\"" + faction.getName() + "\" stopped war\n").withStyle(ChatFormatting.GREEN));

                                                    return 1;
                                                }
                                        ))
                        )

                );
    }

}
