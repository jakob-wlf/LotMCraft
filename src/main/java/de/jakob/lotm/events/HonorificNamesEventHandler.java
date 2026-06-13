package de.jakob.lotm.events;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.attachments.AnchorComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.sefirah.RiverBlessingManager;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.PendingPrayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HonorificNamesEventHandler {
    public static HashMap<UUID, LinkedList<String>> input = new HashMap<>();
    public static HashMap<UUID, Long> timeout = new HashMap<>();
    public static HashMap<UUID, UUID> isInTransferring = new HashMap<>();

    public static LinkedList<Pair<UUID, UUID>> answerState = new LinkedList<>();

    public static void addPendingPrayer(UUID targetUUID, PendingPrayer prayer) {
        pendingPrayers.computeIfAbsent(targetUUID, k -> new LinkedList<>()).add(prayer);
    }

    /** Pending prayers per target player UUID: target → list of prayers addressed to them. */
    private static final HashMap<UUID, LinkedList<PendingPrayer>> pendingPrayers = new HashMap<>();

    /** Returns a copy of the pending prayers for the given target player, or an empty list. */
    public static LinkedList<PendingPrayer> getPendingPrayers(UUID targetUUID) {
        return new LinkedList<>(pendingPrayers.getOrDefault(targetUUID, new LinkedList<>()));
    }

    /** Removes a specific pending prayer (identified by sender UUID) for a target player. */
    public static void removePendingPrayer(UUID targetUUID, UUID senderUUID) {
        LinkedList<PendingPrayer> list = pendingPrayers.get(targetUUID);
        if (list != null) {
            list.removeIf(p -> p.senderUUID().equals(senderUUID));
            if (list.isEmpty()) pendingPrayers.remove(targetUUID);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onChatMessageSent(ServerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUUID();

        String DEBUG_nickname = event.getPlayer().getName().getString();

        if(timeout.containsKey(playerUUID)
                && timeout.get(playerUUID) <= System.currentTimeMillis() - 60000) {

            timeout.remove(playerUUID);
            input.remove(playerUUID);

            event.getPlayer().sendSystemMessage(Component.translatable("lotmcraft.honorific_timeout")
                    .withStyle(ChatFormatting.DARK_RED));

            return;
        }

        String rawMessage = event.getRawText();

        if(isInTransferring.containsKey(playerUUID)){
            var target = event.getPlayer().server.getPlayerList().
                    getPlayer(isInTransferring.get(playerUUID));

            if(target != null){
                target.sendSystemMessage(Component.translatable("lotmcraft.honorific_praying_message", rawMessage)
                        .withStyle(ChatFormatting.DARK_GREEN));
            }

            isInTransferring.remove(playerUUID);

            return;
        }

        if(!input.containsKey(playerUUID) && isHonorificNameFirstLine(rawMessage)){
            input.put(playerUUID, new LinkedList<>(List.of(rawMessage)));
            timeout.put(playerUUID, System.currentTimeMillis());

            return;
        }
        else if (input.containsKey(playerUUID) && isHonorificNamePart(rawMessage)){
            var list = input.get(playerUUID);
            list.add(rawMessage);

        } else if (!isHonorificNamePart(rawMessage)) {
            return;
        }

        if(input.containsKey(playerUUID)
                && input.get(playerUUID).size() >= 3
                && isHonorificNameLastLine(rawMessage)){

            var targetUUID = BeyonderData.playerMap.findCandidate(input.get(playerUUID));

            if(targetUUID == null){
                input.remove(playerUUID);
                timeout.remove(playerUUID);

                return;
            }

            var target = event.getPlayer().server.getPlayerList().getPlayer(targetUUID);

            if(target == null){
                input.remove(playerUUID);
                timeout.remove(playerUUID);

                return;
            }

            // ── Gathering member / River-blessed sefirot access ───────────────
            // If the sender is a gathering member of the target (castle owner)
            // OR is blessed by the target (River owner), teleport them to the
            // owner's sefirot instead of sending a prayer notification.
            {
                ServerPlayer sender = event.getPlayer();
                MinecraftServer server = sender.getServer();
                if (server != null) {
                    GatheringData gd = GatheringData.get(server);
                    boolean isMember  = gd.isMember(targetUUID, playerUUID);
                    boolean isBlessed = RiverBlessingManager.isBlessed(playerUUID)
                            && targetUUID.equals(RiverBlessingManager.getOwner(playerUUID));
                    if (isMember || isBlessed) {
                        input.remove(playerUUID);
                        timeout.remove(playerUUID);
                        handleSefirotAccess(sender, targetUUID, server);
                        return;
                    }
                }
            }
            // ─────────────────────────────────────────────────────────────────

            if(targetUUID.equals(playerUUID)){
                target.sendSystemMessage(Component.translatable("lotmcraft.own_praying")
                        .withStyle(ChatFormatting.GREEN));
                return;
            }

            if(BeyonderData.getSequence(target) == 3 && target.distanceTo(event.getPlayer()) >= 4000.0f){
                target.sendSystemMessage(Component.translatable("lotmcraft.far_away_praying")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            isInTransferring.put(playerUUID, targetUUID);

            target.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync(.01f, target);

            // Decrease corruption on prayer
            int decreaseVal = target.level().getGameRules().getInt(de.jakob.lotm.gamerule.ModGameRules.PRAYER_CORRUPTION_DECREASE);
            if (decreaseVal > 0) {
                target.getData(ModAttachments.CORRUPTION_COMPONENT).decreaseCorruptionAndSync(decreaseVal / 1000f, target);
            }

            // Add/Update anchor
            AnchorComponent anchorComp = target.getData(ModAttachments.ANCHOR_COMPONENT);
            anchorComp.addOrUpdateAnchor(playerUUID, 1.0f);

            storePendingPrayer(event.getPlayer(), target);

            target.sendSystemMessage(formNotification(event.getPlayer()));
        }
    }

    /**
     * Stores the incoming prayer so the target can respond via the Honorific Names menu.
     */
    public static void storePendingPrayer(LivingEntity sender, LivingEntity target) {
        answerState.add(new Pair<>(target.getUUID(), sender.getUUID()));

        PendingPrayer prayer = new PendingPrayer(
                sender.getUUID(),
                sender.getName().getString(),
                BeyonderData.getPathway(sender),
                BeyonderData.getSequence(sender),
                sender.getX(), sender.getY(), sender.getZ()
        );

        pendingPrayers.computeIfAbsent(target.getUUID(), k -> new LinkedList<>()).add(prayer);
    }

    /**
     * Sends a simple notification chat message to the target player, pointing them to the menu.
     */
    public static Component formNotification(LivingEntity sender) {
        return Component.empty()
                .append(Component.translatable("lotmcraft.honorific_praying",
                        sender.getName().getString(),
                        BeyonderData.getPathway(sender),
                        BeyonderData.getSequence(sender),
                                (int) sender.getX(), (int) sender.getY(), (int) sender.getZ())
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("\n→ Open your ")
                        .withStyle(ChatFormatting.DARK_GREEN))
                .append(Component.literal("Honorific Names menu")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.GOLD)
                                .withBold(true)))
                .append(Component.literal(" in the Introspect screen to respond.")
                        .withStyle(ChatFormatting.DARK_GREEN));
    }

    /** @deprecated Use the Honorific Names menu instead. Kept for command-based fallback. */
    @Deprecated
    public static Component formMessage(LivingEntity player, LivingEntity target) {
        return formNotification(player);
    }

    public static boolean isHonorificNameFirstLine(String str) {
        return BeyonderData.playerMap.containsHonorificNameWithFirstLine(str);
    }

    public static boolean isHonorificNameLastLine(String str) {
        return BeyonderData.playerMap.containsHonorificNameWithLastLine(str);
    }

    public static boolean isHonorificNamePart(String str) {
        return BeyonderData.playerMap.containsHonorificNameWithLine(str);
    }

    /**
     * Teleports a gathering member or River-blessed player into their owner's sefirot,
     * or returns them to their previous location if they are already inside.
     */
    private static void handleSefirotAccess(ServerPlayer member, UUID ownerUUID, MinecraftServer server) {
        SefirotData sefirotData = SefirotData.get(server);
        String ownerSefirot = sefirotData.getClaimedSefirot(ownerUUID);
        if (ownerSefirot == null || ownerSefirot.isEmpty()) {
            member.sendSystemMessage(Component.literal("§cThe owner has no sefirot."));
            return;
        }

        ResourceLocation sefirotDimLoc = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, ownerSefirot);
        boolean inSefirot = member.level().dimension().location().equals(sefirotDimLoc);

        GatheringData gd = GatheringData.get(server);

        if (inSefirot) {
            // Return to previous location
            GatheringData.returnPlayer(member, server);
            member.sendSystemMessage(Component.literal("§bYou have left the sefirot.").withStyle(ChatFormatting.AQUA));
        } else {
            // Save return location and teleport in
            gd.saveReturnLocation(member);
            ResourceKey<net.minecraft.world.level.Level> dimKey = ResourceKey.create(Registries.DIMENSION, sefirotDimLoc);
            ServerLevel sefirotLevel = server.getLevel(dimKey);
            if (sefirotLevel == null) {
                member.sendSystemMessage(Component.literal("§cSefirot dimension not loaded."));
                return;
            }
            // Use a guest chair position (first slot)
            double[] pos = GatheringData.CHAIR_POSITIONS[0];
            member.teleportTo(sefirotLevel, pos[0], pos[1], pos[2], member.getYRot(), member.getXRot());
            GatheringData.markGathered(member.getUUID());
            member.sendSystemMessage(Component.literal("§bYou have entered the sefirot.").withStyle(ChatFormatting.AQUA));
        }
    }
}
