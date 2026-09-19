package de.jakob.lotm.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.BlasphemySlateData;
import de.jakob.lotm.item.custom.BlasphemyCardItem;
import de.jakob.lotm.item.custom.BlasphemySlateHalfItem;
import de.jakob.lotm.item.custom.BlasphemySlateItem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlasphemyCommand {

    private record HolderResult(String name, boolean online) {}

    private static final List<String> PATHWAYS = List.of(
            "fool", "door", "error", "sun", "tyrant", "visionary",
            "darkness", "death", "twilight_giant", "demoness",
            "red_priest", "mother", "abyss", "wheel_of_fortune",
            "black_emperor", "justiciar",
            "left_half", "right_half", "slate"
    );

    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(PATHWAYS, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("blasphemy")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("check")
                        .executes(ctx -> checkBlasphemy(ctx.getSource()))
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(PATHWAY_SUGGESTIONS)
                                .executes(ctx -> removeBlasphemy(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "type")))
                        )
                )
        );
    }

    // ========================= /blasphemy check =========================

    private static int checkBlasphemy(CommandSourceStack source) {
        MinecraftServer server = source.getServer();

        source.sendSuccess(() -> Component.literal("=== Blasphemy Items Status ===").withStyle(ChatFormatting.GOLD), false);

        boolean chaosClaimed = de.jakob.lotm.attachments.SefirotData.get(server).isSefirotClaimed("chaos_sea");
        source.sendSuccess(() -> Component.literal("  Chaos Sea: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(chaosClaimed
                        ? Component.literal("CLAIMED").withStyle(ChatFormatting.RED)
                        : Component.literal("Unclaimed").withStyle(ChatFormatting.GREEN)),
                false);

        source.sendSuccess(() -> Component.literal("  — Cards —").withStyle(ChatFormatting.AQUA), false);
        for (String pathway : List.of("fool","door","error","sun","tyrant","visionary",
                "darkness","death","twilight_giant","demoness",
                "red_priest","mother","abyss","wheel_of_fortune","black_emperor","justiciar")) {
            HolderResult holder = findCardHolder(server, pathway);
            String label = capitalize(pathway) + " Blasphemy Card";
            source.sendSuccess(() -> buildLine(label, holder), false);
        }

        source.sendSuccess(() -> Component.literal("  — Slates —").withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> buildLine("Left Half", findItemHolder(server, LOTMCraft.MOD_ID + ":blasphemy_slate_left_half", BlasphemySlateHalfItem.class)), false);
        source.sendSuccess(() -> buildLine("Right Half", findItemHolder(server, LOTMCraft.MOD_ID + ":blasphemy_slate_right_half", BlasphemySlateHalfItem.class)), false);
        source.sendSuccess(() -> buildLine("Blasphemy Slate", findItemHolder(server, LOTMCraft.MOD_ID + ":blasphemy_slate", BlasphemySlateItem.class)), false);

        return 1;
    }

    private static HolderResult findCardHolder(MinecraftServer server, String pathway) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlasphemyCardItem card
                        && pathway.equals(card.getPathway())) {
                    return new HolderResult(player.getName().getString(), true);
                }
            }
        }
        String targetId = LOTMCraft.MOD_ID + ":" + pathway + "_blasphemy_card";
        for (UUID uuid : getAllPlayerUUIDs(server)) {
            if (server.getPlayerList().getPlayer(uuid) != null) continue;
            CompoundTag root = readPlayerNbt(server, uuid);
            if (root != null && nbtInventoryContains(root, targetId)) {
                return new HolderResult(getPlayerName(server, uuid), false);
            }
        }
        return null;
    }

    private static HolderResult findItemHolder(MinecraftServer server, String itemId, Class<?> itemClass) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                    return new HolderResult(player.getName().getString(), true);
                }
            }
        }
        for (UUID uuid : getAllPlayerUUIDs(server)) {
            if (server.getPlayerList().getPlayer(uuid) != null) continue;
            CompoundTag root = readPlayerNbt(server, uuid);
            if (root != null && nbtInventoryContains(root, itemId)) {
                return new HolderResult(getPlayerName(server, uuid), false);
            }
        }
        return null;
    }

    // ========================= /blasphemy remove =========================

    private static int removeBlasphemy(CommandSourceStack source, String type) {
        MinecraftServer server = source.getServer();

        boolean isLeftHalf  = type.equalsIgnoreCase("left_half");
        boolean isRightHalf = type.equalsIgnoreCase("right_half");
        boolean isSlate     = type.equalsIgnoreCase("slate");
        boolean isCard      = !isLeftHalf && !isRightHalf && !isSlate;

        String targetId;
        String label;
        if (isLeftHalf) {
            targetId = LOTMCraft.MOD_ID + ":blasphemy_slate_left_half";
            label = "Left Half";
        } else if (isRightHalf) {
            targetId = LOTMCraft.MOD_ID + ":blasphemy_slate_right_half";
            label = "Right Half";
        } else if (isSlate) {
            targetId = LOTMCraft.MOD_ID + ":blasphemy_slate";
            label = "Blasphemy Slate";
        } else {
            // Validate pathway
            if (!PATHWAYS.contains(type.toLowerCase())) {
                source.sendFailure(Component.literal("Unknown type. Use a pathway name, left_half, right_half, or slate."));
                return 0;
            }
            targetId = LOTMCraft.MOD_ID + ":" + type.toLowerCase() + "_blasphemy_card";
            label = capitalize(type) + " Blasphemy Card";
        }

        int removed = 0;

        // Online players
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && matchesRemoveType(stack, isCard, isLeftHalf, isRightHalf, isSlate, type)) {
                    inv.setItem(i, ItemStack.EMPTY);
                    removed++;
                }
            }
        }

        // Offline players
        for (UUID uuid : getAllPlayerUUIDs(server)) {
            if (server.getPlayerList().getPlayer(uuid) != null) continue;
            CompoundTag root = readPlayerNbt(server, uuid);
            if (root == null) continue;
            int count = nbtInventoryRemove(root, targetId);
            if (count > 0) {
                writePlayerNbt(server, uuid, root);
                removed += count;
            }
        }

        // Loaded block entities
        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (LevelChunk chunk : getRelevantChunks(serverLevel)) {
                for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
                    if (!(be instanceof Container container)) continue;
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack stack = container.getItem(i);
                        if (!stack.isEmpty() && matchesRemoveType(stack, isCard, isLeftHalf, isRightHalf, isSlate, type)) {
                            container.setItem(i, ItemStack.EMPTY);
                            be.setChanged();
                            removed++;
                        }
                    }
                }
            }
        }

        // Also clear uniqueness tracking in BlasphemySlateData
        BlasphemySlateData bsd = BlasphemySlateData.get(server);
        if (isCard) {
            bsd.clearCard(type.toLowerCase());
        } else if (isLeftHalf) {
            bsd.clearLeftHalf();
        } else if (isRightHalf) {
            bsd.clearRightHalf();
        } else if (isSlate) {
            bsd.clearSlate();
        }

        final int count = removed;
        final String finalLabel = label;
        if (removed > 0) {
            source.sendSuccess(() -> Component.literal("Removed " + count + "x " + finalLabel + ".").withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendSuccess(() -> Component.literal("No " + finalLabel + " found anywhere.").withStyle(ChatFormatting.YELLOW), false);
        }
        return removed;
    }

    // ========================= Helpers =========================

    private static boolean matchesRemoveType(ItemStack stack, boolean isCard, boolean isLeftHalf,
                                              boolean isRightHalf, boolean isSlate, String pathway) {
        if (isCard) return stack.getItem() instanceof BlasphemyCardItem card && pathway.toLowerCase().equals(card.getPathway());
        if (isLeftHalf) return stack.getItem() instanceof BlasphemySlateHalfItem half && half.getHalfType() == BlasphemySlateHalfItem.HalfType.LEFT;
        if (isRightHalf) return stack.getItem() instanceof BlasphemySlateHalfItem half && half.getHalfType() == BlasphemySlateHalfItem.HalfType.RIGHT;
        if (isSlate) return stack.getItem() instanceof BlasphemySlateItem;
        return false;
    }

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

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Arrays.stream(s.split("_"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static List<UUID> getAllPlayerUUIDs(MinecraftServer server) {
        Path dir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".dat"))
                    .map(p -> {
                        String name = p.getFileName().toString();
                        try { return UUID.fromString(name.substring(0, name.length() - 4)); }
                        catch (IllegalArgumentException e) { return null; }
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
        try { return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()); }
        catch (IOException e) { LOTMCraft.LOGGER.warn("Failed to read playerdata for {}", uuid, e); return null; }
    }

    private static void writePlayerNbt(MinecraftServer server, UUID uuid, CompoundTag root) {
        Path file = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid + ".dat");
        try { NbtIo.writeCompressed(root, file); }
        catch (IOException e) { LOTMCraft.LOGGER.warn("Failed to write playerdata for {}", uuid, e); }
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
            if (itemId.equals(inv.getCompound(i).getString("id"))) { inv.remove(i); removed++; }
        }
        return removed;
    }

    private static String getPlayerName(MinecraftServer server, UUID uuid) {
        return server.getProfileCache()
                .get(uuid)
                .map(GameProfile::getName)
                .orElse(uuid.toString().substring(0, 8) + "...");
    }

    private static List<LevelChunk> getRelevantChunks(ServerLevel level) {
        List<LevelChunk> result = new ArrayList<>();
        Set<net.minecraft.world.level.ChunkPos> positions = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            net.minecraft.world.level.ChunkPos center = new net.minecraft.world.level.ChunkPos(player.blockPosition());
            for (int dx = -8; dx <= 8; dx++)
                for (int dz = -8; dz <= 8; dz++)
                    positions.add(new net.minecraft.world.level.ChunkPos(center.x + dx, center.z + dz));
        }
        for (net.minecraft.world.level.ChunkPos pos : positions) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk != null) result.add(chunk);
        }
        return result;
    }
}
