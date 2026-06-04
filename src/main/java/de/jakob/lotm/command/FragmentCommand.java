package de.jakob.lotm.command;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.item.custom.MysteriousTabletFragmentItem;
import de.jakob.lotm.item.custom.MysteriousTabletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FragmentCommand {

    private record HolderResult(String name, boolean online) {}

    private static final List<String> FRAGMENT_TYPES = List.of("upper", "right", "left", "lower", "tablet");

    private static final SuggestionProvider<CommandSourceStack> TYPE_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(FRAGMENT_TYPES, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fragment")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("check")
                        .executes(context -> checkFragments(context.getSource()))
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(TYPE_SUGGESTIONS)
                                .executes(context -> removeFragment(context.getSource(),
                                        StringArgumentType.getString(context, "type")))
                        )
                )
                .then(Commands.literal("locate")
                        .executes(context -> locateFragmentChests(context.getSource()))
                )
        );
    }

    // ========================= /fragment check =========================

    private static int checkFragments(CommandSourceStack source) {
        MinecraftServer server = source.getServer();

        source.sendSuccess(() -> Component.literal("=== Mysterious Tablet Status ===").withStyle(ChatFormatting.GOLD), false);

        boolean castleClaimed = de.jakob.lotm.attachments.SefirotData.get(server).isSefirotClaimed("sefirah_castle");
        if (castleClaimed) {
            source.sendSuccess(() -> Component.literal("  Sefirah Castle: ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("CLAIMED").withStyle(ChatFormatting.RED))
                    .append(Component.literal(" — tablets & fragments cannot exist").withStyle(ChatFormatting.DARK_GRAY)),
                    false);
        } else {
            source.sendSuccess(() -> Component.literal("  Sefirah Castle: ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("Unclaimed").withStyle(ChatFormatting.GREEN)),
                    false);
        }

        for (MysteriousTabletData.FragmentType type : MysteriousTabletData.FragmentType.values()) {
            String label = capitalize(type.getId()) + " Fragment";
            HolderResult holder = findFragmentHolder(server, type);
            source.sendSuccess(() -> buildLine(label, holder), false);
        }

        HolderResult tabletHolder = findTabletHolder(server);
        source.sendSuccess(() -> buildLine("Assembled Tablet", tabletHolder), false);

        return 1;
    }

    private static HolderResult findFragmentHolder(MinecraftServer server, MysteriousTabletData.FragmentType type) {
        // Check online players first (in-memory is always up-to-date)
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof MysteriousTabletFragmentItem frag
                        && frag.getFragmentType() == type) {
                    return new HolderResult(player.getName().getString(), true);
                }
            }
        }
        // Check offline player .dat files
        String targetId = LOTMCraft.MOD_ID + ":" + type.getId() + "_fragment_of_a_mysterious_tablet";
        for (UUID uuid : getAllPlayerUUIDs(server)) {
            if (server.getPlayerList().getPlayer(uuid) != null) continue; // already checked online
            CompoundTag root = readPlayerNbt(server, uuid);
            if (root != null && nbtInventoryContains(root, targetId)) {
                return new HolderResult(getPlayerName(server, uuid), false);
            }
        }
        return null;
    }

    private static HolderResult findTabletHolder(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof MysteriousTabletItem) {
                    return new HolderResult(player.getName().getString(), true);
                }
            }
        }
        String targetId = LOTMCraft.MOD_ID + ":mysterious_tablet";
        for (UUID uuid : getAllPlayerUUIDs(server)) {
            if (server.getPlayerList().getPlayer(uuid) != null) continue;
            CompoundTag root = readPlayerNbt(server, uuid);
            if (root != null && nbtInventoryContains(root, targetId)) {
                return new HolderResult(getPlayerName(server, uuid), false);
            }
        }
        return null;
    }

    // ========================= /fragment locate =========================

    private static int locateFragmentChests(CommandSourceStack source) {
        MysteriousTabletData data = MysteriousTabletData.get(source.getServer());

        source.sendSuccess(() -> Component.literal("=== Fragment Chest Locations ===").withStyle(ChatFormatting.GOLD), false);

        Set<BlockPos> spiritPositions = data.getSpiritChestPositions();
        source.sendSuccess(() -> {
            MutableComponent line = Component.literal("  Left Fragment (Spirit World): ").withStyle(ChatFormatting.YELLOW);
            if (spiritPositions.isEmpty()) {
                line.append(Component.literal("Not yet generated").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                boolean first = true;
                for (BlockPos pos : spiritPositions) {
                    if (!first) line.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                    line.append(buildCoordsLink(pos, "spirit_world"));
                    first = false;
                }
            }
            return line;
        }, false);

        Set<BlockPos> ancientPositions = data.getAncientCityChestPositions();
        // If no positions known, find nearest ancient city while still on main thread
        BlockPos nearestAncient = ancientPositions.isEmpty() ? findNearestAncientCity(source) : null;
        source.sendSuccess(() -> {
            MutableComponent line = Component.literal("  Lower Fragment (Ancient City / Overworld): ").withStyle(ChatFormatting.YELLOW);
            if (!ancientPositions.isEmpty()) {
                boolean first = true;
                for (BlockPos pos : ancientPositions) {
                    if (!first) line.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                    line.append(buildCoordsLink(pos, "overworld"));
                    first = false;
                }
            } else if (nearestAncient != null) {
                line.append(Component.literal("Chest not yet assigned — nearest ancient city: ").withStyle(ChatFormatting.DARK_GRAY));
                line.append(buildCoordsLink(nearestAncient, "overworld"));
                line.append(Component.literal(" (open chests there to spawn it)").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                line.append(Component.literal("Not yet generated").withStyle(ChatFormatting.DARK_GRAY));
            }
            return line;
        }, false);

        return 1;
    }

    private static BlockPos findNearestAncientCity(CommandSourceStack source) {
        try {
            ServerLevel overworld = source.getServer().overworld();
            BlockPos searchPos = BlockPos.containing(source.getPosition());
            ResourceKey<Structure> cityKey = ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.withDefaultNamespace("ancient_city"));
            Holder<Structure> holder = overworld.registryAccess()
                    .registry(Registries.STRUCTURE)
                    .flatMap(reg -> reg.getHolder(cityKey))
                    .orElse(null);
            if (holder == null) return null;
            Pair<BlockPos, Holder<Structure>> result = overworld.getChunkSource().getGenerator()
                    .findNearestMapStructure(overworld, HolderSet.direct(holder), searchPos, 100, false);
            return result != null ? result.getFirst() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static MutableComponent buildCoordsLink(BlockPos pos, String dimensionLabel) {
        String coordText = "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
        String tpCommand = "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        return Component.literal(coordText)
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCommand))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to fill /tp command (" + dimensionLabel + ")")
                                        .withStyle(ChatFormatting.GRAY))));
    }

    // ========================= /fragment remove =========================

    private static int removeFragment(CommandSourceStack source, String type) {
        boolean isTablet = type.equalsIgnoreCase("tablet");
        MysteriousTabletData.FragmentType fragmentType = isTablet ? null
                : MysteriousTabletData.FragmentType.fromId(type.toLowerCase());

        if (!isTablet && fragmentType == null) {
            source.sendFailure(Component.literal("Unknown type. Use: upper, right, left, lower, tablet"));
            return 0;
        }

        MinecraftServer server = source.getServer();
        int removed = 0;

        // Remove from online player inventories
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && matchesType(stack, isTablet, fragmentType)) {
                    inv.setItem(i, ItemStack.EMPTY);
                    removed++;
                }
            }
        }

        // Remove from offline player .dat files
        String targetId = isTablet
                ? LOTMCraft.MOD_ID + ":mysterious_tablet"
                : LOTMCraft.MOD_ID + ":" + fragmentType.getId() + "_fragment_of_a_mysterious_tablet";

        for (UUID uuid : getAllPlayerUUIDs(server)) {
            if (server.getPlayerList().getPlayer(uuid) != null) continue; // already handled
            CompoundTag root = readPlayerNbt(server, uuid);
            if (root == null) continue;
            int count = nbtInventoryRemove(root, targetId);
            if (count > 0) {
                writePlayerNbt(server, uuid, root);
                removed += count;
            }
        }

        // Remove from loaded block entities across all dimensions
        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (LevelChunk chunk : getRelevantChunks(serverLevel)) {
                for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
                    if (!(be instanceof Container container)) continue;
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack stack = container.getItem(i);
                        if (!stack.isEmpty() && matchesType(stack, isTablet, fragmentType)) {
                            container.setItem(i, ItemStack.EMPTY);
                            be.setChanged();
                            removed++;
                        }
                    }
                }
            }
        }

        final int count = removed;
        final String label = isTablet ? "Assembled Tablet" : capitalize(type) + " Fragment";
        if (removed > 0) {
            source.sendSuccess(() -> Component.literal("Removed " + count + "x " + label + ".").withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendSuccess(() -> Component.literal("No " + label + " found anywhere.").withStyle(ChatFormatting.YELLOW), false);
        }
        return removed;
    }

    // ========================= Offline player helpers =========================

    private static List<UUID> getAllPlayerUUIDs(MinecraftServer server) {
        Path dir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".dat"))
                    .map(p -> {
                        String name = p.getFileName().toString();
                        try {
                            return UUID.fromString(name.substring(0, name.length() - 4));
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private static CompoundTag readPlayerNbt(MinecraftServer server, UUID uuid) {
        Path file = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid + ".dat");
        if (!Files.exists(file)) return null;
        try {
            return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            LOTMCraft.LOGGER.warn("Failed to read playerdata for {}", uuid, e);
            return null;
        }
    }

    private static void writePlayerNbt(MinecraftServer server, UUID uuid, CompoundTag root) {
        Path file = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid + ".dat");
        try {
            NbtIo.writeCompressed(root, file);
        } catch (IOException e) {
            LOTMCraft.LOGGER.warn("Failed to write playerdata for {}", uuid, e);
        }
    }

    private static boolean nbtInventoryContains(CompoundTag root, String itemId) {
        ListTag inv = root.getList("Inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < inv.size(); i++) {
            if (itemId.equals(inv.getCompound(i).getString("id"))) return true;
        }
        return false;
    }

    private static int nbtInventoryRemove(CompoundTag root, String itemId) {
        ListTag inv = root.getList("Inventory", Tag.TAG_COMPOUND);
        int removed = 0;
        for (int i = inv.size() - 1; i >= 0; i--) {
            if (itemId.equals(inv.getCompound(i).getString("id"))) {
                inv.remove(i);
                removed++;
            }
        }
        return removed;
    }

    private static String getPlayerName(MinecraftServer server, UUID uuid) {
        return server.getProfileCache()
                .get(uuid)
                .map(GameProfile::getName)
                .orElse(uuid.toString().substring(0, 8) + "...");
    }

    // ========================= Shared helpers =========================

    private static MutableComponent buildLine(String label, HolderResult result) {
        MutableComponent msg = Component.literal("  " + label + ": ").withStyle(ChatFormatting.YELLOW);
        if (result != null) {
            String suffix = result.online() ? " (online)" : " (offline)";
            msg.append(Component.literal(result.name()).withStyle(result.online() ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            msg.append(Component.literal(suffix).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            msg.append(Component.literal("Not found").withStyle(ChatFormatting.RED));
        }
        return msg;
    }

    private static boolean matchesType(ItemStack stack, boolean isTablet, MysteriousTabletData.FragmentType fragmentType) {
        if (isTablet) return stack.getItem() instanceof MysteriousTabletItem;
        return stack.getItem() instanceof MysteriousTabletFragmentItem frag && frag.getFragmentType() == fragmentType;
    }

    private static List<LevelChunk> getRelevantChunks(ServerLevel level) {
        List<LevelChunk> result = new ArrayList<>();
        Set<ChunkPos> positions = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            ChunkPos center = new ChunkPos(player.blockPosition());
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    positions.add(new ChunkPos(center.x + dx, center.z + dz));
                }
            }
        }
        if (positions.isEmpty() && level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            net.minecraft.core.BlockPos spawn = level.getSharedSpawnPos();
            ChunkPos sc = new ChunkPos(spawn);
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    positions.add(new ChunkPos(sc.x + dx, sc.z + dz));
                }
            }
        }
        for (ChunkPos pos : positions) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk != null) result.add(chunk);
        }
        return result;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}