package de.jakob.lotm.util.helper;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.events.custom.StartAdvanceSequencePathwayEvent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.ChangePlayerPerspectivePacket;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.jakob.lotm.util.BeyonderData.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AdvancementUtil {

    private static final HashMap<UUID, BeyonderPotion> activeAdvancements = new HashMap<>();

    private static final List<HashSet<String>> PATHWAY_DOMAINS = List.of(
            new HashSet<>(Set.of("fool", "error", "door")),
            new HashSet<>(Set.of("red_priest", "demoness")),
            new HashSet<>(Set.of("sun", "tyrant", "visionary")),
            new HashSet<>(Set.of("darkness", "death"))
    );


    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!activeAdvancements.containsKey(player.getUUID())) return;

        BeyonderPotion potion = activeAdvancements.get(player.getUUID());
        if (!player.getInventory().add(potion.getDefaultInstance()))
            player.drop(potion.getDefaultInstance(), false);
        activeAdvancements.remove(player.getUUID());

        int index = player.getInventory().findSlotMatchingItem(PotionItemHandler.EMPTY_BOTTLE.get().getDefaultInstance());
        if (index != -1) player.getInventory().removeItem(index, 1);
    }

    public static void advance(LivingEntity entity, String pathway, int sequence) {
        if(playerMap == null) return;

        ControllingDataComponent data = entity.getData(ModAttachments.CONTROLLING_DATA);
        if (data.isControlling()) {
            entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
            return;
        }

        if (entity instanceof Player player && player.isCreative()) {
            setBeyonder(entity, pathway, sequence);
            return;
        }

        if (entity instanceof Player player) {
            activeAdvancements.put(player.getUUID(), PotionItemHandler.selectPotionOfPathwayAndSequence(null, pathway, sequence));
        }

        float sanity = entity.getData(ModAttachments.SANITY_COMPONENT).getSanity();

        if (!isBeyonder(entity)) {
            advanceFirstTime(entity, pathway, sequence, sanity);
            return;
        }

        String prevPathway = getPathway(entity);
        int prevSequence = getSequence(entity);

        if (!prevPathway.equals(pathway)) {
            advancePathwaySwitch(entity, pathway, sequence, prevPathway, prevSequence);
            return;
        }

        if (prevSequence < sequence) return;

        float digestionProgress = entity instanceof Player p ? BeyonderData.getDigestionProgress(p) : 0f;
        int difference = Math.abs(prevSequence - sequence);
        double failureChance = calculateFailureChance(difference, digestionProgress, sanity);
        if (BeyonderData.hasSwitchedPathway(entity)) failureChance = Math.min(1.0, failureChance + 0.1);

        if(prevSequence == sequence) {
            advanceSameSequence(entity, pathway, sequence, failureChance);
            return;
        }

        executeAdvancement(entity, pathway, sequence, failureChance, null);
    }

    private static void advanceFirstTime(LivingEntity entity, String pathway, int sequence, float sanity) {
        double failureChance = calculateFailureChanceForFirstTime(sequence, sanity);
        executeAdvancement(entity, pathway, sequence, failureChance, null);
    }

    private static void advancePathwaySwitch(LivingEntity entity, String pathway, int sequence,
                                             String prevPathway, int prevSequence) {
        boolean isSameDomainSwitch = prevSequence <= 5 && sequence == (prevSequence - 1) && sameDomain(prevPathway, pathway);
        double failureChance = isSameDomainSwitch && !hasSwitchedPathway(entity) ? 0.0 : 1.0;

        Runnable onSuccess = isSameDomainSwitch
                ? () -> playerMap.recordPathwaySwitch(entity, prevSequence, prevPathway)
                : null;

        executeAdvancement(entity, pathway, sequence, failureChance, onSuccess);
    }

    private static void advanceSameSequence(LivingEntity entity, String pathway, int sequence, double failureChance) {
        int duration = calculateAdvancementDuration(sequence);
        StartAdvanceSequencePathwayEvent event = postAdvancementEvent(entity, sequence, pathway, failureChance, duration);

        String finalPathway = event.getPathway();
        int finalSequence = event.getSequence();
        double finalFailureChance = event.getFailureChance();
        int finalDuration = event.getDuration();

        scheduleAdvancementEffects(entity, finalPathway, finalDuration, finalSequence);

        if (finalFailureChance >= 1.0 || Math.random() < finalFailureChance) {
            scheduleFailure(entity, finalDuration);
            return;
        }

        ServerScheduler.scheduleDelayed(finalDuration, () -> {
            if (!activeAdvancements.containsKey(entity.getUUID())) return;
            activeAdvancements.remove(entity.getUUID());
            BeyonderData.addCharStack(entity, sequence);
            sendThirdPersonPacket(entity);
        });
    }

    // Fires the event, schedules effects, then schedules failure-death or success-setBeyonder.
    // onSuccessPreSet runs before setBeyonder if non-null.
    private static void executeAdvancement(LivingEntity entity, String pathway, int sequence,
                                           double failureChance, Runnable onSuccessPreSet) {
        int duration = calculateAdvancementDuration(sequence);
        StartAdvanceSequencePathwayEvent event = postAdvancementEvent(entity, sequence, pathway, failureChance, duration);

        String finalPathway = event.getPathway();
        int finalSequence = event.getSequence();
        double finalFailureChance = event.getFailureChance();
        int finalDuration = event.getDuration();

        scheduleAdvancementEffects(entity, finalPathway, finalDuration, finalSequence);

        if (finalFailureChance >= 1.0 || Math.random() < finalFailureChance) {
            scheduleFailure(entity, finalDuration);
            return;
        }

        ServerScheduler.scheduleDelayed(finalDuration, () -> {
            if (!activeAdvancements.containsKey(entity.getUUID())) return;
            activeAdvancements.remove(entity.getUUID());
            if (onSuccessPreSet != null) onSuccessPreSet.run();
            setBeyonder(entity, finalPathway, finalSequence);
            sendThirdPersonPacket(entity);
        });
    }

    private static StartAdvanceSequencePathwayEvent postAdvancementEvent(LivingEntity entity, int sequence,
                                                                         String pathway, double failureChance, int duration) {
        StartAdvanceSequencePathwayEvent event = new StartAdvanceSequencePathwayEvent(entity, sequence, pathway, failureChance, duration);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    private static void scheduleAdvancementEffects(LivingEntity entity, String pathway, int duration, int sequence) {
        scheduleAdvancementParticles(entity, pathway, duration);
        scheduleFloating(entity, duration);
        scheduleThirdPerson(entity, duration);
        scheduleFog(entity, duration, pathway);
        scheduleRandomDamage(entity, duration, sequence);
    }

    private static void scheduleFailure(LivingEntity entity, int duration) {
        int deathTime = (int) (Math.random() * duration);
        ServerScheduler.scheduleDelayed(deathTime, () -> {
            if (!activeAdvancements.containsKey(entity.getUUID())) return;
            activeAdvancements.remove(entity.getUUID());
            if (!entity.isDeadOrDying())
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
        });
    }

    private static void sendThirdPersonPacket(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(serverPlayer, new ChangePlayerPerspectivePacket(
                    entity.getId(), ChangePlayerPerspectivePacket.PERSPECTIVE.THIRD.getValue()));
        }
    }

    private static boolean sameDomain(String pathway1, String pathway2) {
        for (HashSet<String> domain : PATHWAY_DOMAINS) {
            if (domain.contains(pathway1) && domain.contains(pathway2)) return true;
        }
        return false;
    }

    private static void scheduleFog(LivingEntity entity, int duration, String pathway) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        int colorInt = BeyonderData.pathwayInfos.get(pathway).color();
        float baseRed   = ((colorInt >> 16) & 0xFF) / 255.0f;
        float baseGreen = ((colorInt >>  8) & 0xFF) / 255.0f;
        float baseBlue  = ( colorInt        & 0xFF) / 255.0f;

        AtomicInteger tickCounter = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
            int tick = tickCounter.getAndIncrement();
            float hueShift = (float) Math.sin(tick * 0.05f) * 0.3f;

            float red   = Math.max(0f, Math.min(1f, baseRed   + hueShift));
            float green = Math.max(0f, Math.min(1f, baseGreen + hueShift * 0.8f));
            float blue  = Math.max(0f, Math.min(1f, baseBlue  + hueShift * 0.6f));

            FogComponent fogComponent = serverPlayer.getData(ModAttachments.FOG_COMPONENT);
            fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.ADVANCING, entity);
            fogComponent.setActiveAndSync(true, entity);
            fogComponent.setFogColorAndSync(new Vec3f(red, green, blue), entity);
        });
    }

    private static void scheduleRandomDamage(LivingEntity entity, int duration, int sequence) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        int baseDamageInterval = 15;
        int damageEventCount = duration / baseDamageInterval;

        for (int i = 0; i < damageEventCount; i++) {
            int randomOffset = (int) (Math.random() * (baseDamageInterval / 2));
            int damageTime = (i * baseDamageInterval) + randomOffset;

            ServerScheduler.scheduleDelayed(damageTime, () -> {
                if (entity.isDeadOrDying()) return;

                float baseDamage = 2.0f + (9 - sequence) * 0.5f;
                float damage = baseDamage + (float) (Math.random() * 2.0f);
                float safeMaxDamage = entity.getHealth() - 4.0f;

                if (safeMaxDamage > 0) {
                    DamageSource damageSource = new DamageSource(
                            entity.level().registryAccess()
                                    .registryOrThrow(Registries.DAMAGE_TYPE)
                                    .getHolderOrThrow(ModDamageTypes.LOOSING_CONTROL)
                    );
                    entity.hurt(damageSource, Math.min(damage, safeMaxDamage));
                }
            }, serverLevel);
        }
    }

    private static void scheduleThirdPerson(LivingEntity entity, int duration) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
            if (entity.isDeadOrDying()) return;
            PacketHandler.sendToPlayer(serverPlayer, new ChangePlayerPerspectivePacket(
                    entity.getId(), ChangePlayerPerspectivePacket.PERSPECTIVE.THIRD.getValue()));
        }, () -> {
            PacketHandler.sendToPlayer(serverPlayer, new ChangePlayerPerspectivePacket(
                    entity.getId(), ChangePlayerPerspectivePacket.PERSPECTIVE.FIRST.getValue()));
        }, serverPlayer.serverLevel());
    }

    private static void scheduleFloating(LivingEntity entity, int duration) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        Vec3 position = entity.position().add(0, 1.5, 0);
        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if (entity.isDeadOrDying()) return;
            entity.teleportTo(position.x, position.y, position.z);
        }, serverLevel);
    }

    private static void scheduleAdvancementParticles(LivingEntity entity, String pathway, int duration) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        int colorInt = BeyonderData.pathwayInfos.get(pathway).color();
        float red   = ((colorInt >> 16) & 0xFF) / 255.0f;
        float green = ((colorInt >>  8) & 0xFF) / 255.0f;
        float blue  = ( colorInt        & 0xFF) / 255.0f;

        DustParticleOptions dustParticle = new DustParticleOptions(new Vector3f(red, green, blue), 2.5f);
        Vec3 center = entity.position().add(0, entity.getBbHeight() / 2 + 1.5, 0);

        int convergingDuration = (int) (duration * 0.6);
        int sphereDuration     = (int) (duration * 0.3);
        int sphereStart        = convergingDuration;
        int fadeStart          = sphereStart + sphereDuration;
        int fadeDuration       = duration - fadeStart;

        scheduleConvergingParticles(serverLevel, entity, convergingDuration);
        scheduleSphereParticles(serverLevel, entity, dustParticle, sphereStart, sphereDuration);
        scheduleFadeParticles(serverLevel, entity, dustParticle, fadeStart, fadeDuration);

        ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.ENCHANT, center,
                2, 2, entity.getBbHeight(), .75, 2, duration, 8, 2);
    }

    private static void scheduleConvergingParticles(ServerLevel level, LivingEntity entity, int duration) {
        AtomicInteger currentTick = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if (entity.isDeadOrDying()) return;

            int tick = currentTick.getAndIncrement();
            Vec3 center = entity.position().add(0, entity.getBbHeight() / 2, 0);
            int particleCount = 8 + (tick * 15) / duration;
            double speed = 0.15 + ((double) tick / duration) * 0.45;

            for (int i = 0; i < particleCount; i++) {
                double radius = 2 + Math.random() * 2.0;
                double theta  = Math.random() * 2 * Math.PI;
                double phi    = Math.random() * Math.PI;

                double startX = center.x + radius * Math.sin(phi) * Math.cos(theta);
                double startY = center.y + radius * Math.cos(phi);
                double startZ = center.z + radius * Math.sin(phi) * Math.sin(theta);

                Vec3 direction = center.subtract(new Vec3(startX, startY, startZ)).normalize();

                level.sendParticles(ParticleTypes.SMOKE,
                        startX, startY, startZ, 0,
                        direction.x * speed, direction.y * speed, direction.z * speed, 1.0);
            }
        }, level);
    }

    private static void scheduleSphereParticles(ServerLevel level, LivingEntity entity,
                                                DustParticleOptions particle, int startDelay, int duration) {
        ServerScheduler.scheduleDelayed(startDelay, () -> {
            if (entity.isDeadOrDying()) return;
            ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
                if (entity.isDeadOrDying()) return;
                Vec3 currentCenter = entity.position().add(0, entity.getBbHeight() / 2, 0);
                ParticleUtil.spawnSphereParticles(level, particle, currentCenter, 1.5, 60);
            }, level);
        });
    }

    private static void scheduleFadeParticles(ServerLevel level, LivingEntity entity,
                                              DustParticleOptions particle, int startDelay, int duration) {
        ServerScheduler.scheduleDelayed(startDelay, () -> {
            if (entity.isDeadOrDying()) return;
            AtomicInteger currentTick = new AtomicInteger(0);
            ServerScheduler.scheduleForDuration(0, 3, duration, () -> {
                if (entity.isDeadOrDying()) return;
                int tick = currentTick.getAndIncrement();
                double progressRatio = (double) tick / (duration / 3);
                int particleCount = (int) (60 * (1.0 - progressRatio));
                if (particleCount > 0) {
                    Vec3 center = entity.position().add(0, entity.getBbHeight() / 2, 0);
                    ParticleUtil.spawnSphereParticles(level, particle, center, 1.5 + progressRatio * 0.5, particleCount);
                }
            }, level);
        });
    }

    private static int calculateAdvancementDuration(int sequence) {
        int baseSeconds = 5 + (9 - sequence) * 3;
        return 20 * baseSeconds;
    }

    private static double calculateFailureChanceForFirstTime(int sequence, float sanity) {
        if (sanity < 0.2f) return 1.0;

        double baseChance = sequence >= 9 ? 0.0 : sequence >= 7 ? 0.85 : 1.0;
        double sanityPenalty = sanity < 0.8f ? (0.8f - sanity) * 0.4 : 0;

        return Math.min(1.0, Math.max(0.0, baseChance + sanityPenalty));
    }

    private static double calculateFailureChance(int sequenceDifference, float digestion, float sanity) {
        if (sanity < 0.2f || sequenceDifference > 2) return 1.0;

        double baseChance;
        if (sequenceDifference <= 1) {
            boolean goodDigestion = digestion >= 0.95f;
            boolean goodSanity    = sanity    >= 0.8f;
            if      (goodDigestion && goodSanity) baseChance = 0.0;
            else if (goodDigestion)               baseChance = 0.65;
            else if (goodSanity)                  baseChance = 0.6;
            else                                  baseChance = 0.3;
        } else {
            baseChance = 0.9;
        }

        double digestionPenalty = digestion < 0.95f ? Math.min(0.5, (0.95f - digestion) * 0.4) : 0;
        double sanityPenalty    = sanity    < 0.8f  ? (0.8f - sanity) * 0.5               : 0;

        return Math.min(1.0, Math.max(0.0, baseChance + digestionPenalty + sanityPenalty));
    }
}