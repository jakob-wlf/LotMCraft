package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.BlasphemySlateData;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.item.custom.BlasphemyCardItem;
import de.jakob.lotm.item.custom.BlasphemySlateHalfItem;
import de.jakob.lotm.item.custom.BlasphemySlateItem;
import de.jakob.lotm.item.custom.MysteriousTabletFragmentItem;
import de.jakob.lotm.item.custom.MysteriousTabletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class SefrotConvergenceHandler {
    private static final long TICKS_PER_MINUTE = 20L * 60L;
    private static final long MIN_INTERVAL_TICKS = 5L * TICKS_PER_MINUTE;
    private static final long MAX_INTERVAL_TICKS = 60L * TICKS_PER_MINUTE;
    private static final double DISTANCE_SCALE = 4_000.0;

    private static final Map<UUID, Long> NEXT_CHECK_TICKS = new HashMap<>();

    private SefrotConvergenceHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long now = server.overworld().getGameTime();
        if (now % 20 != 0) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            List<HeldSefrot> heldSefrots = getEligibleSefrots(player);
            if (heldSefrots.isEmpty()) {
                NEXT_CHECK_TICKS.remove(player.getUUID());
                continue;
            }

            long nextCheck = NEXT_CHECK_TICKS.getOrDefault(player.getUUID(), 0L);
            if (nextCheck == 0L) {
                NEXT_CHECK_TICKS.put(player.getUUID(), now + randomInterval(player, heldSefrots));
                continue;
            }
            if (now < nextCheck) continue;

            attemptConvergence(player, heldSefrots, false);
            NEXT_CHECK_TICKS.put(player.getUUID(), now + randomInterval(player, heldSefrots));
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NEXT_CHECK_TICKS.remove(event.getEntity().getUUID());
    }

    public static ConvergenceResult triggerManualConvergence(ServerPlayer player) {
        List<HeldSefrot> heldSefrots = getEligibleSefrots(player);
        if (heldSefrots.isEmpty()) return ConvergenceResult.NO_PIECES;
        return attemptConvergence(player, heldSefrots, true);
    }

    private static ConvergenceResult attemptConvergence(ServerPlayer player, List<HeldSefrot> heldSefrots,
                                                        boolean guaranteed) {
        List<SenseTarget> targets = findTargets(player, heldSefrots);
        if (targets.isEmpty()) return ConvergenceResult.NO_TARGET;

        SenseTarget target = targets.getFirst();
        double heldRatio = (double) target.source().pieces().size() / target.source().type().totalPieces();
        double distanceFactor = distanceFactor(target.distance());
        double chance = Mth.clamp(0.10 + 0.90 * heldRatio * distanceFactor, 0.10, 0.95);
        if (!guaranteed && player.getRandom().nextDouble() > chance) return ConvergenceResult.CHANCE_FAILED;

        String direction = getDirection(player.getX(), player.getZ(), target.player().getX(), target.player().getZ());
        int estimatedDistance = estimateDistance(target.distance());
        Component message = Component.literal("{ ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal("CONVERGENCE").withStyle(ChatFormatting.BOLD)
                .withColor(target.source().type().textColor()))
                .append(Component.literal(" } ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal("You sense the power of ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(target.source().type().displayName())
                .withColor(target.source().type().textColor()))
                .append(Component.literal(" to the " + direction + ", about ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(estimatedDistance + " blocks").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" away.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(message);

            Component warning = Component.literal("{ ").withStyle(ChatFormatting.DARK_GRAY)
        .append(Component.literal("CONVERGENCE").withStyle(ChatFormatting.BOLD)
            .withColor(target.source().type().textColor()))
                .append(Component.literal(" } ").withStyle(ChatFormatting.DARK_GRAY))
        .append(Component.literal("Someone senses the power of ").withStyle(ChatFormatting.RED))
        .append(Component.literal(target.source().type().displayName())
            .withColor(target.source().type().textColor()))
        .append(Component.literal(" that you carry. Be wary.").withStyle(ChatFormatting.RED));
        target.player().sendSystemMessage(warning);
        return ConvergenceResult.SUCCESS;
    }

    private static long randomInterval(ServerPlayer player, List<HeldSefrot> heldSefrots) {
        List<SenseTarget> targets = findTargets(player, heldSefrots);
        double strongestRatio = heldSefrots.stream()
                .mapToDouble(held -> (double) held.pieces().size() / held.type().totalPieces())
                .max().orElse(0.0);
        double proximity = targets.isEmpty() ? 0.0 : distanceFactor(targets.getFirst().distance());
        double convergenceStrength = Mth.clamp(strongestRatio * 0.65 + proximity * 0.35, 0.0, 1.0);
        long variableRange = Math.round((MAX_INTERVAL_TICKS - MIN_INTERVAL_TICKS) * (1.0 - convergenceStrength));
        return MIN_INTERVAL_TICKS + Math.round(player.getRandom().nextDouble() * variableRange);
    }

    private static List<SenseTarget> findTargets(ServerPlayer sourcePlayer, List<HeldSefrot> heldSefrots) {
        List<SenseTarget> targets = new ArrayList<>();
        for (HeldSefrot held : heldSefrots) {
            if (held.pieces().size() >= held.type().totalPieces()) continue;

            for (ServerPlayer candidate : sourcePlayer.server.getPlayerList().getPlayers()) {
                if (candidate == sourcePlayer
                        || !candidate.level().dimension().equals(sourcePlayer.level().dimension())) continue;

                Set<String> candidatePieces = getPieces(candidate, held.type());
                candidatePieces.removeAll(held.pieces());
                if (candidatePieces.isEmpty()) continue;

                targets.add(new SenseTarget(held, candidate, candidatePieces.size(), sourcePlayer.distanceTo(candidate)));
            }
        }

        targets.sort(Comparator.comparingInt(SenseTarget::missingPiecesHeld).reversed()
                .thenComparingDouble(SenseTarget::distance));
        return targets;
    }

    private static List<HeldSefrot> getEligibleSefrots(ServerPlayer player) {
        List<HeldSefrot> result = new ArrayList<>();
        for (SefrotItemSet type : SefrotItemSet.values()) {
            if (!SefirotAuthorityManager.NEIGHBORING_PATHS
                    .getOrDefault(type.sefrotId(), List.of())
                    .contains(de.jakob.lotm.util.BeyonderData.getPathway(player))) {
                continue;
            }
            Set<String> pieces = getPieces(player, type);
            if (!pieces.isEmpty()) {
                result.add(new HeldSefrot(type, pieces));
            }
        }
        return result;
    }

    private static Set<String> getPieces(ServerPlayer player, SefrotItemSet type) {
        Set<String> pieces = new HashSet<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            addRepresentedPieces(player.getInventory().getItem(slot), type, pieces);
        }
        return pieces;
    }

    private static void addRepresentedPieces(ItemStack stack, SefrotItemSet type, Set<String> pieces) {
        if (stack.isEmpty()) return;

        if (type == SefrotItemSet.SEFIRAH_CASTLE) {
            if (stack.getItem() instanceof MysteriousTabletFragmentItem fragment) {
                pieces.add(fragment.getFragmentType().name().toLowerCase());
            } else if (stack.getItem() instanceof MysteriousTabletItem) {
                for (MysteriousTabletData.FragmentType fragmentType : MysteriousTabletData.FragmentType.values()) {
                    pieces.add(fragmentType.name().toLowerCase());
                }
            }
            return;
        }

        if (stack.getItem() instanceof BlasphemyCardItem card) {
            pieces.add(card.getPathway());
        } else if (stack.getItem() instanceof BlasphemySlateHalfItem half) {
            pieces.addAll(half.getHalfType() == BlasphemySlateHalfItem.HalfType.LEFT
                    ? BlasphemySlateData.LEFT_HALF_PATHWAYS
                    : BlasphemySlateData.RIGHT_HALF_PATHWAYS);
        } else if (stack.getItem() instanceof BlasphemySlateItem) {
            pieces.addAll(BlasphemySlateData.LEFT_HALF_PATHWAYS);
            pieces.addAll(BlasphemySlateData.RIGHT_HALF_PATHWAYS);
        }
    }

    private static double distanceFactor(double distance) {
        return Mth.clamp(1.0 / (1.0 + distance / DISTANCE_SCALE), 0.15, 1.0);
    }

    private static int estimateDistance(double distance) {
        int step = distance < 100 ? 25 : distance < 500 ? 50 : distance < 2_000 ? 100 : 500;
        return Math.max(step, (int) Math.round(distance / step) * step);
    }

    private static String getDirection(double fromX, double fromZ, double toX, double toZ) {
        double degrees = Math.toDegrees(Math.atan2(toZ - fromZ, toX - fromX));
        String[] directions = {"east", "south-east", "south", "south-west", "west", "north-west", "north", "north-east"};
        int index = Math.floorMod((int) Math.round(degrees / 45.0), directions.length);
        return directions[index];
    }

    private enum SefrotItemSet {
        SEFIRAH_CASTLE("sefirah_castle", "Sefirah Castle", 4, 0x8FA9B8),
        CHAOS_SEA("chaos_sea", "the Chaos Sea", 16, 0xFFAD33);

        private final String sefrotId;
        private final String displayName;
        private final int totalPieces;
        private final int textColor;

        SefrotItemSet(String sefrotId, String displayName, int totalPieces, int textColor) {
            this.sefrotId = sefrotId;
            this.displayName = displayName;
            this.totalPieces = totalPieces;
            this.textColor = textColor;
        }

        private String sefrotId() {
            return sefrotId;
        }

        private String displayName() {
            return displayName;
        }

        private int totalPieces() {
            return totalPieces;
        }

        private int textColor() {
            return textColor;
        }
    }

    private record HeldSefrot(SefrotItemSet type, Set<String> pieces) {
    }

    private record SenseTarget(HeldSefrot source, ServerPlayer player, int missingPiecesHeld, double distance) {
    }

    public enum ConvergenceResult {
        SUCCESS,
        NO_PIECES,
        NO_TARGET,
        CHANCE_FAILED
    }
}