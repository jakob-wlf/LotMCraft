package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MaliceSeedAbility extends Ability {

    // target UUID -> growth level (0-10)
    private static final HashMap<UUID, Integer> seedGrowth = new HashMap<>();
    // caster UUID -> target UUID
    private static final HashMap<UUID, UUID> casterTarget = new HashMap<>();

    private static final int MAX_GROWTH = 10;

    private static final DustParticleOptions maliceDust = new DustParticleOptions(new Vector3f(0.4f, 0f, 0.1f), 2f);

    public MaliceSeedAbility(String id) {
        super(id, 15);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 150;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2.5f);
        if (target == null || seedGrowth.containsKey(target.getUUID())) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.malice_seed.no_target").withColor(0xFFff124d));
            return;
        }

        if (AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 3));
            entity.hurt(entity.damageSources().generic(), 10);
            return;
        }

        // Replace previous seed if the caster already had one planted
        if (casterTarget.containsKey(entity.getUUID())) {
            UUID oldTargetUUID = casterTarget.get(entity.getUUID());
            seedGrowth.remove(oldTargetUUID);
        }

        seedGrowth.put(target.getUUID(), 0);
        casterTarget.put(entity.getUUID(), target.getUUID());

        ParticleUtil.spawnParticles(serverLevel, maliceDust, target.getEyePosition(), 40, .75, 0);
        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1, 1);

        target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 60 * 5, 0, false, false, true));

        AtomicInteger growth = new AtomicInteger(0);

        // Grow one stage every 30 seconds for 5 minutes (10 stages total)
        ServerScheduler.scheduleForDuration(20 * 30, 20 * 30, 20 * 30 * MAX_GROWTH, () -> {
            if (!seedGrowth.containsKey(target.getUUID())) return;
            int newGrowth = Math.min(growth.incrementAndGet(), MAX_GROWTH);
            seedGrowth.put(target.getUUID(), newGrowth);
            growth.set(newGrowth);
            // Pulse intensity increases with growth stage
            ParticleUtil.spawnParticles(serverLevel, maliceDust, target.getEyePosition(), 5 + newGrowth * 3, 1.0 + newGrowth * 0.1, 0);
        }, serverLevel);
    }

    /** Called by other systems (e.g. cleanse abilities) to purge the seed from a target. */
    public static void purgeSeedFromTarget(UUID targetUUID, LivingEntity target) {
        if (!seedGrowth.containsKey(targetUUID)) return;
        seedGrowth.remove(targetUUID);
        // Remove the reverse mapping
        casterTarget.values().remove(targetUUID);
        if (target != null) {
            target.removeEffect(ModEffects.LOOSING_CONTROL);
        }
    }

    public static boolean hasSeed(UUID targetUUID) {
        return seedGrowth.containsKey(targetUUID);
    }

    public static int getGrowth(UUID targetUUID) {
        return seedGrowth.getOrDefault(targetUUID, 0);
    }

    /**
     * When the caster dies while a Malice Seed is planted, cancel their death and
     * respawn them at the target's location. Damage is dealt to the target based on
     * the seed's growth level.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCasterDeath(LivingDeathEvent event) {
        LivingEntity caster = event.getEntity();
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        UUID casterUUID = caster.getUUID();
        if (!casterTarget.containsKey(casterUUID)) return;

        UUID targetUUID = casterTarget.get(casterUUID);
        if (!seedGrowth.containsKey(targetUUID)) {
            casterTarget.remove(casterUUID);
            return;
        }

        Entity targetEntity = serverLevel.getEntity(targetUUID);
        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {
            casterTarget.remove(casterUUID);
            seedGrowth.remove(targetUUID);
            return;
        }

        int growth = seedGrowth.get(targetUUID);

        event.setCanceled(true);
        caster.setHealth(caster.getMaxHealth() * 0.5f);
        caster.teleportTo(target.getX(), target.getY(), target.getZ());

        // 5 base damage + 3 per growth stage (5–35 damage)
        float damage = 5 + (growth * 3f);
        target.hurt(serverLevel.damageSources().magic(), damage);

        ParticleUtil.spawnParticles(serverLevel, new DustParticleOptions(new Vector3f(0.4f, 0f, 0.1f), 2.5f),
                target.getEyePosition(), 60, 2.0, 0);

        // Consume the seed on use
        casterTarget.remove(casterUUID);
        seedGrowth.remove(targetUUID);
        target.removeEffect(ModEffects.LOOSING_CONTROL);
    }

    /** Clean up if the seed target dies before the caster. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTargetDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        UUID targetUUID = event.getEntity().getUUID();
        if (seedGrowth.containsKey(targetUUID)) {
            seedGrowth.remove(targetUUID);
            casterTarget.values().remove(targetUUID);
        }
    }
}
