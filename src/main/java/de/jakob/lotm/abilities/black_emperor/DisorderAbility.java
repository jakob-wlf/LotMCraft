package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;


import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DisorderAbility extends SelectableAbility {

    // NBT keys — public so DistortionAbility.breakBonds can clear them
    public static final String DISORDERED_ACTION_KEY      = "lotm_disordered_action";
    public static final String DISORDERED_PERCEPTION_KEY  = "lotm_disordered_perception";
    public static final String DISORDERED_PERCEPTION_CHARGES_KEY = "lotm_disordered_perception_charges";
    public static final String DISTANCE_WARP_KEY          = "lotm_distance_warp";


    // Casters currently protected by the Defensive Veil
    private static final Set<UUID> DEFENSIVE_VEIL = new HashSet<>();

    private static final ResourceLocation DISTANCE_WARP_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "distance_warp_speed");

    private static final ResourceLocation DISTANCE_WARP_STEP_ID =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "distance_warp_step");

    public DisorderAbility(String id) {
        super(id, 7.0f);
        canBeCopied      = false;
        canBeReplicated  = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 65;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.disorder.disordered_actions",
                "ability.lotmcraft.disorder.disordered_perception",
                "ability.lotmcraft.disorder.defensive_veil",
                "ability.lotmcraft.disorder.break_bonds",
                "ability.lotmcraft.disorder.distance_warp",
                "ability.lotmcraft.disorder.frenzy",
                "ability.lotmcraft.disorder.entropy"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // S-veil: no target needed.
        if (abilityIndex == 2) {
            castDefensiveVeil(serverLevel, entity);
            return;
        }

        // S-warp: self-cast.
        if (abilityIndex == 4) {
            distanceWarp(serverLevel, entity);
            return;
        }

        // S-frenzy: AOE cast, no target required.
        if (abilityIndex == 5) {
            FrenzySubAbility.cast(serverLevel, entity);
            return;
        }

        // S-entropy: AOE decay mark, no target required.
        if (abilityIndex == 6) {
            EntropySubAbility.cast(serverLevel, entity);
            return;
        }

        // S0/S1/S3: normal target-based abilities.
        LivingEntity target;
        if (abilityIndex == 3) {
            target = AbilityUtil.getTargetEntity(entity, 14, 1.5f);
            if (target == null) target = entity;
        } else {
            target = AbilityUtil.getTargetEntity(entity, 14, 1.5f);
            if (target == null) {
                AbilityUtil.sendActionBar(entity,
                        Component.literal("No target in range.").withColor(0xFF5555));
                return;
            }
        }

        if (!canDisorder(entity, target)) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("Target resists Disorder.").withColor(0xFF5555));
            return;
        }

        switch (abilityIndex) {
            case 0 -> disorderActions(serverLevel, entity, target);
            case 1 -> disorderPerception(serverLevel, entity, target);
            case 3 -> breakBonds(serverLevel, entity, target);
        }
    }

    // -------------------------------------------------------------------------
    // Sub-abilities
    // -------------------------------------------------------------------------

    /**
     * Disorder Actions: the target's next outgoing hit is redirected to a random
     * nearby entity (including their own allies) as their action "misfires".
     */
    private void disorderActions(ServerLevel level, LivingEntity caster, LivingEntity target) {
        target.getPersistentData().putBoolean(DISORDERED_ACTION_KEY, true);

        // Tight lightning spiral rising off the target — chaotic mental energy
        ParticleUtil.createParticleSpirals(level, ModParticles.LIGHTNING.get(),
                target.position().add(0, 0.5, 0),
                0.3, 1.1, 1.8, 0.09, 10, 35, 2, 3);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Actions disordered.").withColor(0x9933CC));

        // Auto-expire after 6 s if the tag was not consumed by a hit
        ServerScheduler.scheduleDelayed(20 * 6, () ->
                target.getPersistentData().remove(DISORDERED_ACTION_KEY), level);
    }

    /**
     * Disorder Perception: the target's next 3 ability uses are replaced with a random
     * ability from their own pathway, excluding DisorderAbility itself.
     */
    private void disorderPerception(ServerLevel level, LivingEntity caster, LivingEntity target) {
        target.getPersistentData().putBoolean(DISORDERED_PERCEPTION_KEY, true);
        target.getPersistentData().putInt(DISORDERED_PERCEPTION_CHARGES_KEY, 3);
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 7, 0, false, false, true));

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.0, 16);
        RingEffectManager.createRingForAll(target.position(), 2.5f, 18,
                0.12f, 0.0f, 0.20f, 0.85f, 0.14f, 0.6f, level);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Perception disordered.").withColor(0x9933CC));

        ServerScheduler.scheduleDelayed(20 * 7, () -> {
            target.getPersistentData().remove(DISORDERED_PERCEPTION_KEY);
            target.getPersistentData().remove(DISORDERED_PERCEPTION_CHARGES_KEY);
        }, level);
    }

    /**
     * Defensive Veil: for 8 seconds the caster gains a 35 % chance to negate any
     * incoming hit as attackers' perception of them becomes disordered.
     */
    private void castDefensiveVeil(ServerLevel level, LivingEntity caster) {
        UUID id = caster.getUUID();
        DEFENSIVE_VEIL.add(id);

        // Outward ring at the caster's feet as the veil snaps into place
        RingEffectManager.createRingForAll(caster.position(), 2.8f, 22,
                0.12f, 0.0f, 0.22f, 1.0f, 0.18f, 1.4f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 1.8, 28);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Disorder veil active.").withColor(0x9933CC));

        ServerScheduler.scheduleDelayed(20 * 8, () -> {
            DEFENSIVE_VEIL.remove(id);
            if (caster.isAlive()) {
                AbilityUtil.sendActionBar(caster,
                        Component.literal("Disorder veil faded.").withColor(0x660099));
            }
        }, level);
    }

    /**
     * Break Bonds: strips all active control effects from the target.
     * Can be used on self, an ally (to cleanse), or an enemy (to remove their active CC).
     */

    private void breakBonds(ServerLevel level, LivingEntity caster, LivingEntity target) {
        // Cleanse fire and harmful effects
        target.setRemainingFireTicks(0);

        target.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL)
                .toList()
                .forEach(target::removeEffect);

        // sanity fix its like placate
        target.removeEffect(ModEffects.LOOSING_CONTROL);
        target.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync(.15f, target);

        // Cleanse support.
        if (target instanceof Player player) {
            player.getFoodData().setSaturation(20);
            player.getFoodData().setFoodLevel(20);
        }

        // Simple cleanse visuals
        RingEffectManager.createRingForAll(target.position().add(0, 1, 0), 2.0f, 60,
                122 / 255f, 235 / 255f, 124 / 255f, 1.0f, 0.5f, 0.75f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.2, 18);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Control bonds shattered.").withColor(0x9933CC));
    }

    // -------------------------------------------------------------------------
// Distance Warp
// -------------------------------------------------------------------------
// This is now a forward teleport dash instead of a movement-speed buff.
// It scales with sequence, tries to stop at a safe position, and uses your
// existing particle/ring framework so it looks like space is warping around
// the caster.
    private void distanceWarp(ServerLevel level, LivingEntity caster) {
        int sequence = BeyonderData.getSequence(caster);

        // Stronger casters warp farther.
        double maxDistance = Math.min(34.0D, 8.0D + Math.max(0, 8 - sequence) * 3.0D);

        Vec3 start = caster.position();
        Vec3 look = caster.getLookAngle().normalize();

        // Find a safe end point using a block-only clip, not entity collision.
        Vec3 destination = findWarpDestination(level, caster, start, look, maxDistance);

        if (destination == null || destination.distanceToSqr(start) < 1.0D) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("No space to warp into.").withColor(0xFF5555));
            return;
        }

        // Start effect
        RingEffectManager.createRingForAll(start, 2.8f, 22,
                0.12f, 0.0f, 0.22f, 1.0f, 0.18f, 1.4f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                start.add(0, 1, 0), 1.2, 24);

        spawnWarpTrail(level, start, destination);

        // Teleport the caster.
        caster.stopRiding();
        caster.fallDistance = 0;
        caster.teleportTo(destination.x, destination.y, destination.z);
        caster.setDeltaMovement(Vec3.ZERO);

        // End effect
        RingEffectManager.createRingForAll(destination, 2.8f, 22,
                0.12f, 0.0f, 0.22f, 1.0f, 0.18f, 1.4f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                destination.add(0, 1, 0), 1.2, 24);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Distance warped.").withColor(0x9933CC));
    }
    // -------------------------------------------------------------------------
    // Sequence resistance check
    // -------------------------------------------------------------------------

    private boolean canDisorder(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;
        int cs = BeyonderData.getSequence(caster);
        int ts = BeyonderData.getSequence(target);
        if (ts >= cs) return true;
        if (ts == 0) return false;
        if (cs - ts == 1) return random.nextFloat() < 0.30f;
        return false;
    }

    // -------------------------------------------------------------------------
    // Event hooks
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentData().getBoolean(DISORDERED_PERCEPTION_KEY)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        // Don't intercept DisorderAbility itself to avoid recursion
        if (event.getAbility() instanceof DisorderAbility) return;

        int charges = entity.getPersistentData().getInt(DISORDERED_PERCEPTION_CHARGES_KEY);
        if (charges <= 0) {
            entity.getPersistentData().remove(DISORDERED_PERCEPTION_KEY);
            entity.getPersistentData().remove(DISORDERED_PERCEPTION_CHARGES_KEY);
            return;
        }

        // Cancel intended ability and fire a random one instead
        event.setCanceled(true);

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);
        var randomAbility = LOTMCraft.abilityHandler.getRandomAbility(
                pathway, sequence, new java.util.Random(), false,
                List.of(event.getAbility(), LOTMCraft.abilityHandler.getById("disorder_ability"))
        );

        if (randomAbility != null) {
            randomAbility.useAbility(serverLevel, entity, true, true, true);
        }

        int remaining = charges - 1;
        if (remaining <= 0) {
            entity.getPersistentData().remove(DISORDERED_PERCEPTION_KEY);
            entity.getPersistentData().remove(DISORDERED_PERCEPTION_CHARGES_KEY);
        } else {
            entity.getPersistentData().putInt(DISORDERED_PERCEPTION_CHARGES_KEY, remaining);
        }
    }

    @SubscribeEvent
    public static void onDisorderEvents(LivingDamageEvent.Pre event) {
        LivingEntity victim   = event.getEntity();
        LivingEntity attacker = resolveAttacker(event);

        // --- Defensive Veil: 35 % miss chance against protected casters ---
        if (DEFENSIVE_VEIL.contains(victim.getUUID())) {
            if (victim.level().random.nextFloat() < 0.35f) {
                event.setNewDamage(0);
                return; // attack fully negated, no further processing needed
            }
        }

        if (attacker == null) return;
        float dmg = event.getNewDamage();

        // --- Disordered Actions: redirect this hit to a random nearby entity ---
        if (attacker.getPersistentData().getBoolean(DISORDERED_ACTION_KEY)) {
            attacker.getPersistentData().remove(DISORDERED_ACTION_KEY);

            if (dmg > 0 && attacker.level() instanceof ServerLevel sLevel) {
                event.setNewDamage(0);

                // Use the built-in entity query instead of the missing helper.
                List<LivingEntity> nearby = sLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        attacker.getBoundingBox().inflate(8),
                        e -> e != attacker && e != victim
                );

                if (!nearby.isEmpty()) {
                    LivingEntity redirectedTarget = nearby.get(sLevel.random.nextInt(nearby.size()));

                    // Apply the redirected hit through the normal damage pipeline.
                    // This keeps it compatible with your damage hooks and other effects.
                    redirectedTarget.hurt(
                            attacker.damageSources().mobAttack(attacker),
                            (float) dmg
                    );
                }
            }

            return; // action was redirected — skip perception check below
        }
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Vec3 findWarpDestination(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 direction, double maxDistance) {
        Vec3 lastSafe = start;

        // Step forward through space and stop at the last safe block position.
        for (double traveled = 1.0D; traveled <= maxDistance; traveled += 1.0D) {
            Vec3 candidate = start.add(direction.scale(traveled));

            if (isSafeWarpPosition(level, caster, candidate)) {
                lastSafe = candidate;
            } else {
                break;
            }
        }

        return lastSafe;
    }

    private boolean isSafeWarpPosition(ServerLevel level, LivingEntity caster, Vec3 position) {
        Vec3 delta = position.subtract(caster.position());
        AABB movedBox = caster.getBoundingBox().move(delta);

        // Only check whether the destination space itself is free.
        // This does not care about the path between start and end.
        return level.noCollision(caster, movedBox) && !level.containsAnyLiquid(movedBox);
    }

    private void spawnWarpTrail(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);

        for (int i = 1; i < 6; i++) {
            double t = i / 6.0D;
            Vec3 point = start.add(delta.scale(t));

            ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                    point.add(0, 1, 0), 0.75, 14);
        }
    }


    private static LivingEntity resolveAttacker(LivingDamageEvent.Pre event) {
        var direct = event.getSource().getDirectEntity();
        if (direct instanceof LivingEntity le) return le;
        if (direct instanceof Projectile p && p.getOwner() instanceof LivingEntity le) return le;
        return null;
    }

    public static boolean isVeilActive(UUID uuid) {
        return DEFENSIVE_VEIL.contains(uuid);
    }
}