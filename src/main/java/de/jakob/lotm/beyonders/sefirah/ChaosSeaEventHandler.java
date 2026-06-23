package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.BlasphemySlateItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSefirotAccommodationPacket;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.data.PathwayInfos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.*;

/**
 * Handles the Chaos Sea sefirah accommodation ritual.
 *
 * Trigger     : Right-click a Blasphemy Slate.
 * Duration    : 5 minutes (6 000 ticks, same as other sefirah rituals).
 * Allowed     : tyrant, sun, visionary, white_tower, hanged_man.
 * Particles   : pathway colours of sun, tyrant, and visionary.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ChaosSeaEventHandler {

    private static final String SEFIROT_ID    = "chaos_sea";
    private static final int    REQUIRED_TICKS = 20 * 60 * 5;

    private static final Set<String> ALLOWED_PATHWAYS = Set.of(
            "tyrant", "sun", "visionary", "white_tower", "hanged_man");

    /** Pathways whose colours are used for the particle cloud. */
    private static final String[] PARTICLE_PATHWAYS = {"sun", "tyrant", "visionary"};

    private static final Map<UUID, Integer> ritualTicks         = new HashMap<>();
    private static final Map<UUID, UUID>    ritualSlateIds      = new HashMap<>();
    private static final Map<UUID, UUID>    ritualBeamEffectIds = new HashMap<>();
    private static final Set<UUID>          ritualAnnounced     = new HashSet<>();

    // ─── Dimension lock ───────────────────────────────────────────────────────

    /**
     * 1. While a ritual is in progress, lock the Spirit World (same as other sefirot).
     * 2. Block entry into the Chaos Sea unless the player is the current owner.
     * 3. Block travel TO the Chaos Sea from the Nature dimension (compass exploit fix).
     */
    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceKey<Level> target  = event.getDimension();
        ResourceKey<Level> current = event.getEntity().level().dimension();

        // 1. Spirit World lock during ritual
        if (!ritualTicks.isEmpty()) {
            boolean goingIn  = target  == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY;
            boolean goingOut = current == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY;
            if (goingIn || goingOut) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("lotm.sefirot.sefirah_castle_spirit_world_locked"));
                return;
            }
        }

        // 2 & 3. Block unauthorised entry into the Chaos Sea
        if (!target.equals(ModDimensions.CHAOS_SEA_DIMENSION_KEY)) return;

        // The owner may always enter their own dimension
        if (SEFIROT_ID.equals(SefirahHandler.getClaimedSefirot(player))) return;

        // Gathering members (blessed by the owner) may freely enter and exit
        UUID ownerUUID = SefirotData.get(player.server).getHolderOf(SEFIROT_ID);
        if (ownerUUID != null && GatheringData.get(player.server).isMember(ownerUUID, player.getUUID())) return;

        // Nobody else — including players coming from the Nature dimension — may enter
        event.setCanceled(true);
        boolean fromNature = current.location().equals(ModDimensions.WORLD_CREATION_LEVEL_KEY.location());
        if (fromNature) {
            player.sendSystemMessage(Component.literal(
                    "The Chaos Sea cannot be reached from within the Nature dimension.")
                    .withStyle(net.minecraft.ChatFormatting.RED));
        } else {
            player.sendSystemMessage(Component.literal(
                    "You do not have authority over the Chaos Sea.")
                    .withStyle(net.minecraft.ChatFormatting.RED));
        }
    }

    // ─── Right-click trigger ─────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof BlasphemySlateItem slateItem)) return;

        if (!canStartRitual(player)) {
            Component msg = getFailureMessage(player);
            if (msg != null) player.sendSystemMessage(msg);
            event.setCanceled(true);
            return;
        }

        if (!slateItem.syncSlate(serverLevel, stack)) {
            event.setCanceled(true);
            return;
        }

        UUID slateId = BlasphemySlateItem.getSlateId(stack);
        if (slateId == null) {
            slateId = UUID.randomUUID();
            BlasphemySlateItem.setSlateId(stack, slateId);
        }

        startRitual(player, slateId);
        event.setCanceled(true);
    }

    // ─── Tick ────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!ritualTicks.containsKey(player.getUUID())) return;

        if (player.tickCount % 5 == 0) {
            UUID beamId = ritualBeamEffectIds.get(player.getUUID());
            if (beamId != null) {
                MovableEffectManager.updateEffectPosition(beamId, new EntityLocation(player), serverLevel);
            }
        }

        if (player.tickCount % 20 != 0) return;
        tickRitual(player, serverLevel);
    }

    // ─── Logout / death interruption ─────────────────────────────────────────

    @SubscribeEvent    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Clear any stale accommodation bar left over from a previous session
        if (!isAccommodating(player)) {
            PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, 0));
        }
    }

    @SubscribeEvent    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isAccommodating(player)) return;
        resetRitual(player, true);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isAccommodating(player)) return;
        resetRitual(player, true);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private static void startRitual(ServerPlayer player, UUID slateId) {
        UUID playerId = player.getUUID();
        if (ritualTicks.containsKey(playerId)) return;

        // Consume the slate immediately
        int slot = findSlateSlot(player, slateId);
        if (slot >= 0) player.getInventory().removeItem(slot, 1);

        ritualTicks.put(playerId, 0);
        ritualSlateIds.put(playerId, slateId);
        ritualAnnounced.remove(playerId);

        if (player.level() instanceof ServerLevel sl) {
            UUID beamId = MovableEffectManager.playEffect(
                    MovableEffectManager.MovableEffect.SKY_BEAM,
                    new EntityLocation(player),
                    0, true, sl, player);
            ritualBeamEffectIds.put(playerId, beamId);
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
                    Component.literal(player.getName().getString()
                            + " has begun accommodating the Chaos Sea!"),
                    false);
            ritualAnnounced.add(playerId);
        }

        int ticks = ritualTicks.getOrDefault(playerId, 0) + 20;
        ritualTicks.put(playerId, ticks);

        applyAccommodationEffects(player, serverLevel);

        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(ticks, REQUIRED_TICKS));

        if (ticks < REQUIRED_TICKS) return;

        // Finish — claim sefirah
        UUID finishBeamId = ritualBeamEffectIds.remove(playerId);
        if (finishBeamId != null) {
            MovableEffectManager.removeEffect(finishBeamId, serverLevel);
        }

        boolean claimed = SefirahHandler.claimSefirot(player, SEFIROT_ID, true);
        if (claimed) {
            player.sendSystemMessage(Component.literal(
                    "The Chaos Sea has accepted you. Its endless disorder flows through your veins.")
                    .withStyle(net.minecraft.ChatFormatting.AQUA));
            serverLevel.getServer().execute(() -> SefirahHandler.teleportToSefirot(player, true));
        } else {
            player.sendSystemMessage(Component.literal(
                    "The Chaos Sea is already claimed by another.")
                    .withStyle(net.minecraft.ChatFormatting.RED));
            dropSlate(player);
        }

        resetRitual(player, false);
    }

    private static void applyAccommodationEffects(ServerPlayer player, ServerLevel serverLevel) {
        Vec3 center = player.position().add(0, player.getEyeHeight() * 0.5, 0);

        for (String pathway : PARTICLE_PATHWAYS) {
            DustParticleOptions dust = dustForPathway(pathway);
            ParticleUtil.spawnParticles(serverLevel, dust, center, 150, 2.5, 2.5, 2.5, 0.05);
            ParticleUtil.spawnParticles(serverLevel, dust, center,  80, 0.6, 0.6, 0.6, 0.02);
        }
    }

    private static DustParticleOptions dustForPathway(String pathway) {
        PathwayInfos info = BeyonderData.pathwayInfos.get(pathway);
        int color = info != null ? info.color() : 0xFFFFFF;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = ( color        & 0xFF) / 255f;
        return new DustParticleOptions(new Vector3f(r, g, b), 1.0f);
    }

    private static boolean canStartRitual(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) return false;
        if (SefirotData.get(player.server).isSefirotClaimed(SEFIROT_ID)) return false;
        if (SefirahHandler.hasSefirot(player)) return false;
        return ALLOWED_PATHWAYS.contains(BeyonderData.getPathway(player));
    }

    private static boolean canContinueRitual(ServerPlayer player) {
        return canStartRitual(player) && ritualSlateIds.containsKey(player.getUUID());
    }

    private static Component getFailureMessage(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player))
            return Component.literal("You are not a beyonder.").withStyle(net.minecraft.ChatFormatting.RED);
        if (SefirotData.get(player.server).isSefirotClaimed(SEFIROT_ID))
            return Component.literal("The Chaos Sea is already claimed.").withStyle(net.minecraft.ChatFormatting.RED);
        if (SefirahHandler.hasSefirot(player))
            return Component.translatable("lotm.sefirot.already_has_sefirot");
        if (!ALLOWED_PATHWAYS.contains(BeyonderData.getPathway(player)))
            return Component.translatable("lotm.sefirot.wrong_pathway");
        return null;
    }

    private static int findSlateSlot(ServerPlayer player, UUID slateId) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!(s.getItem() instanceof BlasphemySlateItem)) continue;
            UUID sid = BlasphemySlateItem.getSlateId(s);
            if (slateId.equals(sid)) return i;
        }
        return -1;
    }

    private static void dropSlate(ServerPlayer player) {
        ItemStack drop = new ItemStack(ModItems.BLASPHEMY_SLATE.get());
        player.drop(drop, true);
    }

    private static void resetRitual(ServerPlayer player, boolean dropSlate) {
        if (dropSlate) dropSlate(player);
        UUID playerId = player.getUUID();
        UUID beamId   = ritualBeamEffectIds.remove(playerId);
        if (beamId != null && player.level() instanceof ServerLevel sl) {
            MovableEffectManager.removeEffect(beamId, sl);
        }
        ritualTicks.remove(playerId);
        ritualSlateIds.remove(playerId);
        ritualAnnounced.remove(playerId);
        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, 0));
    }

    public static boolean isAccommodating(ServerPlayer player) {
        return ritualTicks.containsKey(player.getUUID());
    }
}
