package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.fool.passives.DangerPremonitionAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncDemonHuntingGazePacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class EyeOfDemonHuntingAbility extends ToggleAbility {


    private static final float CRIT_CHANCE = 0.25f;
    private static final float CRIT_MULTIPLIER = 1.5f;
    private static final int SANITY_RANGE = 32;
    private static final int GAZE_RANGE = 40;
    private static final int GAZE_INTERVAL = 20;
    private static final int MAX_PULSES = 6;
    private static final int FOOTPRINT_RANGE = 24;
    private static final int FOOTPRINT_LIFETIME = 24000;
    private static final int FOOTPRINT_INTERVAL = 10;
    private static final double FOOTPRINT_SPACING = 1.2D;
    private static final int MAX_FOOTPRINTS_SHOWN = 400;
    private static final double SANITY_BALL_RADIUS = 0.2D;
    private static final int SANITY_BALL_POINTS = 26;

    private static final DustParticleOptions FOOTPRINT_DUST = new DustParticleOptions(new Vector3f(0.1f, 0.55f, 0.2f), 0.7f);
    private static final DustParticleOptions EMPTY_SANITY_DUST = new DustParticleOptions(new Vector3f(0.15f, 0.15f, 0.15f), 0.45f);
    private static final Vector3f SANITY_FULL = new Vector3f(0xe8 / 255f, 0xbb / 255f, 0x68 / 255f);
    private static final Vector3f SANITY_LOW = new Vector3f(0xf5 / 255f, 0xad / 255f, 0x2a / 255f);

    private static final Map<UUID, Deque<Footprint>> footprints = new HashMap<>();

    public EyeOfDemonHuntingAbility(String id) {
        super(id);
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(60f, 45f, 30f, 20f, 15f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 15;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.ENDER_EYE_DEATH, entity.getSoundSource(), 1f, 0.7f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        showSanity(serverLevel, player);
        showFootprints(serverLevel, player);
        if (player.tickCount % GAZE_INTERVAL < tickRate) warnGazes(serverLevel, player);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.ENDER_EYE_DEATH, entity.getSoundSource(), 1f, 0.4f);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.isCriticalHit() || !isActive(player)) return;
        if (player.getRandom().nextFloat() >= CRIT_CHANCE) return;
        event.setCriticalHit(true);
        event.setDamageMultiplier(Math.max(event.getDamageMultiplier(), CRIT_MULTIPLIER));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.isSpectator() || player.tickCount % FOOTPRINT_INTERVAL != 0 || !player.onGround()) return;
        Deque<Footprint> trail = footprints.computeIfAbsent(player.getUUID(), uuid -> new ArrayDeque<>());
        long now = player.level().getGameTime();
        while (!trail.isEmpty() && now - trail.peekFirst().time() > FOOTPRINT_LIFETIME) trail.pollFirst();
        Footprint last = trail.peekLast();
        Vec3 position = player.position();
        ResourceKey<Level> dimension = player.level().dimension();
        if (last != null && last.dimension().equals(dimension) && last.position().distanceToSqr(position) < FOOTPRINT_SPACING * FOOTPRINT_SPACING) return;
        trail.addLast(new Footprint(position, now, dimension));
    }

    public static boolean isActive(LivingEntity entity) {
        return ToggleAbility.getActiveAbilitiesForEntity(entity).stream().anyMatch(ability -> ability instanceof EyeOfDemonHuntingAbility);
    }

    private static void showSanity(ServerLevel level, ServerPlayer viewer) {
        for (LivingEntity target : AbilityUtil.getNearbyEntities(viewer, level, viewer.position(), SANITY_RANGE)) {
            if (VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(viewer), target)) continue;
            if (!(target instanceof Player) && !BeyonderData.isBeyonder(target)) continue;
            float sanity = Mth.clamp(target.getData(ModAttachments.SANITY_COMPONENT).getSanity(), 0f, 1f);
            Vector3f color = new Vector3f(SANITY_LOW).lerp(SANITY_FULL, sanity);
            DustParticleOptions filled = new DustParticleOptions(color, 0.45f);
            Vec3 center = target.position().add(0, target.getBbHeight() + 0.55, 0);
            for (int i = 0; i < SANITY_BALL_POINTS; i++) {
                double y = 1 - 2 * (i + 0.5) / SANITY_BALL_POINTS;
                double ring = Math.sqrt(1 - y * y);
                double angle = i * Math.PI * (3 - Math.sqrt(5));
                Vec3 point = center.add(Math.cos(angle) * ring * SANITY_BALL_RADIUS, y * SANITY_BALL_RADIUS, Math.sin(angle) * ring * SANITY_BALL_RADIUS);
                DustParticleOptions dust = (y + 1) / 2 <= sanity ? filled : EMPTY_SANITY_DUST;
                level.sendParticles(viewer, dust, true, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void showFootprints(ServerLevel level, ServerPlayer viewer) {
        int shown = 0;
        double rangeSqr = FOOTPRINT_RANGE * FOOTPRINT_RANGE;
        ResourceKey<Level> dimension = level.dimension();
        long now = level.getGameTime();
        footprints.values().removeIf(trail -> {
            while (!trail.isEmpty() && now - trail.peekFirst().time() > FOOTPRINT_LIFETIME) trail.pollFirst();
            return trail.isEmpty();
        });
        for (Map.Entry<UUID, Deque<Footprint>> entry : footprints.entrySet()) {
            if (entry.getKey().equals(viewer.getUUID())) continue;
            Player owner = level.getPlayerByUUID(entry.getKey());
            if (owner != null && VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(viewer), owner)) continue;
            for (Footprint footprint : entry.getValue()) {
                if (!footprint.dimension().equals(dimension) || footprint.position().distanceToSqr(viewer.position()) > rangeSqr) continue;
                Vec3 pos = footprint.position();
                level.sendParticles(viewer, FOOTPRINT_DUST, false, pos.x, pos.y + 0.05, pos.z, 1, 0.1, 0, 0.1, 0);
                if (++shown >= MAX_FOOTPRINTS_SHOWN) return;
            }
        }
    }

    private static void warnGazes(ServerLevel level, ServerPlayer viewer) {
        int viewerSequence = BeyonderData.getSequence(viewer);
        for (LivingEntity threat : AbilityUtil.getNearbyEntities(viewer, level, viewer.position(), GAZE_RANGE)) {
            if (AllyUtil.areAllies(viewer, threat) || MindConcealmentAbility.isHiddenFrom(viewer, threat) || VisionaryHandler.shouldStayInvisible(viewerSequence, threat)) continue;
            if (!DangerPremonitionAbility.isThreatLookingAtUser(viewer, threat, GAZE_RANGE)) continue;
            int threatSequence = BeyonderData.getSequence(threat);
            int pulses = Mth.clamp(1 + (viewerSequence - threatSequence) + 2, 1, MAX_PULSES);
            PacketHandler.sendToPlayer(viewer, new SyncDemonHuntingGazePacket(threat.getId(), pulses));
        }
    }

    private record Footprint(Vec3 position, long time, ResourceKey<Level> dimension) {
    }
}
