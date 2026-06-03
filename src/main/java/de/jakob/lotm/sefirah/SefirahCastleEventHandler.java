package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.item.custom.MysteriousTabletItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSefirotAccommodationPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.pathways.PathwayInfos;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.data.EntityLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SefirahCastleEventHandler {

    private static final String SEFIROT_ID = "sefirah_castle";
    private static final int REQUIRED_TICKS = 20 * 60 * 5;
    private static final int COMMAND_PERMISSION_LEVEL = 2;

    private static final Set<String> ALLOWED_PATHWAYS = Set.of("fool", "door", "error");

    private static final Map<UUID, Integer> ritualTicks = new HashMap<>();
    private static final Map<UUID, UUID> ritualTabletIds = new HashMap<>();
    private static final Map<UUID, UUID> ritualBeamEffectIds = new HashMap<>();
    private static final Set<UUID> ritualAnnounced = new HashSet<>();

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof MysteriousTabletItem tabletItem)) {
            return;
        }

        if (!canStartRitual(player)) {
            Component failMessage = getStartFailureMessage(player);
            if (failMessage != null) {
                player.sendSystemMessage(failMessage);
            }
            event.setCanceled(true);
            return;
        }

        if (!tabletItem.syncTablet(serverLevel, stack)) {
            event.setCanceled(true);
            return;
        }

        UUID tabletId = MysteriousTabletItem.getTabletId(stack);
        if (tabletId == null) {
            tabletId = UUID.randomUUID();
            MysteriousTabletItem.setTabletId(stack, tabletId);
        }

        startRitual(player, tabletId);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!ritualTicks.containsKey(player.getUUID())) {
            return;
        }

        // Update beam position every 5 ticks for smooth tracking even at long range
        // (within render distance the client uses the entity directly; outside it relies on these packets)
        if (player.tickCount % 5 == 0) {
            UUID beamId = ritualBeamEffectIds.get(player.getUUID());
            if (beamId != null) {
                MovableEffectManager.updateEffectPosition(beamId, new EntityLocation(player), serverLevel);
            }
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        tickRitual(player, serverLevel);
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.hasPermissions(COMMAND_PERMISSION_LEVEL)) {
            return;
        }
        if (!isAccommodating(player)) {
            return;
        }

        event.setCanceled(true);
        player.sendSystemMessage(Component.translatable("lotm.sefirot.command_blocked"));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isAccommodating(player)) {
            return;
        }
        resetRitual(player, true);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isAccommodating(player)) {
            return;
        }
        resetRitual(player, true);
    }

    /**
     * Seal the Spirit World for ALL dimension travellers while any
     * Sefirah Castle ritual is in progress.
     */
    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (ritualTicks.isEmpty()) return;

        ResourceKey<net.minecraft.world.level.Level> target  = event.getDimension();
        ResourceKey<net.minecraft.world.level.Level> current = event.getEntity().level().dimension();

        boolean goingIn  = target  == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY;
        boolean goingOut = current == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY;

        if (!goingIn && !goingOut) return;

        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("lotm.sefirot.sefirah_castle_spirit_world_locked"));
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.level().dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY)) {
            return;
        }

        // Disable griefing
        if (entity instanceof Player player) {
            BeyonderData.setGriefingEnabled(player, false);
        }

        // Disable ability use
        if (!(entity instanceof ServerPlayer player) || !SefirahHandler.getClaimedSefirot(player).equalsIgnoreCase(SEFIROT_ID)) {
            DisabledAbilitiesComponent component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            component.disableAbilityUsageForTime(SEFIROT_ID, 20 * 20, entity);
        }
    }

    private static void startRitual(ServerPlayer player, UUID tabletId) {
        UUID playerId = player.getUUID();
        if (ritualTicks.containsKey(playerId)) {
            return;
        }
        // Consume the tablet immediately on start
        int slot = findTabletSlot(player, tabletId);
        if (slot >= 0) {
            player.getInventory().removeItem(slot, 1);
        }
        ritualTicks.put(playerId, 0);
        ritualTabletIds.put(playerId, tabletId);
        ritualAnnounced.remove(playerId);
        // Start the geometry sky beam, visible to all players in the level except the accommodating
        // player themselves (it's extremely distracting in first-person)
        if (player.level() instanceof ServerLevel sl) {
            UUID beamId = MovableEffectManager.playEffect(
                    MovableEffectManager.MovableEffect.SKY_BEAM,
                    new EntityLocation(player),
                    0, true, sl, player);
            ritualBeamEffectIds.put(playerId, beamId);
            // Immediately cancel it on the accommodating player's own client
            MovableEffectManager.removeEffect(beamId, player);
        }
        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, REQUIRED_TICKS));
    }

    private static void tickRitual(ServerPlayer player, ServerLevel serverLevel) {
        if (!canContinueRitual(player)) {
            resetRitual(player, true);
            return;
        }

        UUID playerId = player.getUUID();
        if (!ritualAnnounced.contains(playerId)) {
            player.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("lotm.sefirot.sefirah_castle_started", player.getName().getString()),
                    false);
            ritualAnnounced.add(playerId);
        }

        int ticks = ritualTicks.getOrDefault(playerId, 0) + 20;
        ritualTicks.put(playerId, ticks);

        applyAccommodationEffects(player, serverLevel);

        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(ticks, REQUIRED_TICKS));

        if (ticks < REQUIRED_TICKS) {
            return;
        }

        // Remove the beam BEFORE teleporting — after teleport player.level() changes dimension,
        // so the RemoveMovableEffect packet would be sent to the wrong set of players.
        UUID finishBeamId = ritualBeamEffectIds.remove(playerId);
        if (finishBeamId != null) {
            MovableEffectManager.removeEffect(finishBeamId, serverLevel);
        }

        boolean claimed = SefirahHandler.claimSefirot(player, SEFIROT_ID, true);
        if (claimed) {
            player.sendSystemMessage(Component.translatable("lotm.sefirot.sefirah_castle_claimed"));
            SefirahHandler.teleportToSefirot(player, true);
        } else {
            player.sendSystemMessage(Component.translatable("lotm.sefirot.sefirah_castle_already_occupied"));
            dropTablet(player);
        }

        resetRitual(player, false);
    }

    private static void applyAccommodationEffects(ServerPlayer player, ServerLevel serverLevel) {
        DisabledAbilitiesComponent component = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsageForTime("sefirah_castle_accommodation", 40, player);

        // Heavy particle cloud around the player
        Vec3 center = player.position().add(0, player.getEyeHeight() * 0.5, 0);
        ParticleUtil.spawnParticles(serverLevel, dustForPathway("fool"),  center, 150, 2.5, 2.5, 2.5, 0.05);
        ParticleUtil.spawnParticles(serverLevel, dustForPathway("error"), center, 150, 2.5, 2.5, 2.5, 0.05);
        ParticleUtil.spawnParticles(serverLevel, dustForPathway("door"),  center, 150, 2.5, 2.5, 2.5, 0.05);
        // Extra dense inner burst at eye level
        ParticleUtil.spawnParticles(serverLevel, dustForPathway("fool"),  center, 80, 0.6, 0.6, 0.6, 0.02);
        ParticleUtil.spawnParticles(serverLevel, dustForPathway("error"), center, 80, 0.6, 0.6, 0.6, 0.02);
        ParticleUtil.spawnParticles(serverLevel, dustForPathway("door"),  center, 80, 0.6, 0.6, 0.6, 0.02);
    }

    private static DustParticleOptions dustForPathway(String pathway) {
        PathwayInfos info = BeyonderData.pathwayInfos.get(pathway);
        int color = info != null ? info.color() : 0xFFFFFF;

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        return new DustParticleOptions(new Vector3f(r, g, b), 1.0f);
    }

    private static boolean canStartRitual(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) {
            return false;
        }

        if (SefirotData.get(player.server).isSefirotClaimed(SEFIROT_ID)) {
            return false;
        }

        if (SefirahHandler.hasSefirot(player)) {
            return false;
        }

        String pathway = BeyonderData.getPathway(player);
        return ALLOWED_PATHWAYS.contains(pathway);
    }

    private static boolean canContinueRitual(ServerPlayer player) {
        if (!canStartRitual(player)) {
            return false;
        }
        return ritualTabletIds.containsKey(player.getUUID());
    }

    private static Component getStartFailureMessage(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) {
            return Component.translatable("lotm.sefirot.wrong_pathway");
        }

        if (SefirotData.get(player.server).isSefirotClaimed(SEFIROT_ID)) {
            return Component.translatable("lotm.sefirot.sefirah_castle_already_occupied");
        }

        if (SefirahHandler.hasSefirot(player)) {
            return Component.translatable("lotm.sefirot.already_has_sefirot");
        }

        String pathway = BeyonderData.getPathway(player);
        if (!ALLOWED_PATHWAYS.contains(pathway)) {
            return Component.translatable("lotm.sefirot.wrong_pathway");
        }

        return null;
    }

    private static void consumeTablet(ServerPlayer player) {
        UUID tabletId = ritualTabletIds.get(player.getUUID());
        if (tabletId == null) {
            return;
        }
        int slot = findTabletSlot(player, tabletId);
        if (slot < 0) {
            return;
        }
        player.getInventory().removeItem(slot, 1);
    }

    private static void dropTablet(ServerPlayer player) {
        ItemStack toDrop = new ItemStack(de.jakob.lotm.item.ModItems.MYSTERIOUS_TABLET.get());
        player.drop(toDrop, true);
    }

    private static int findTabletSlot(ServerPlayer player, UUID tabletId) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof MysteriousTabletItem)) {
                continue;
            }
            UUID stackId = MysteriousTabletItem.getTabletId(stack);
            if (tabletId.equals(stackId)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isAccommodating(ServerPlayer player) {
        return ritualTicks.containsKey(player.getUUID());
    }

    private static void resetRitual(ServerPlayer player, boolean dropTablet) {
        if (dropTablet) {
            dropTablet(player);
        }
        UUID playerId = player.getUUID();
        // Stop the geometry sky beam
        UUID beamId = ritualBeamEffectIds.remove(playerId);
        if (beamId != null && player.level() instanceof ServerLevel sl) {
            MovableEffectManager.removeEffect(beamId, sl);
        }
        ritualTicks.remove(playerId);
        ritualTabletIds.remove(playerId);
        ritualAnnounced.remove(playerId);
        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, 0));
    }
}
