package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DistortionAbility extends SelectableAbility {

    // NBT keys
    public static final String DISTORT_ACTION_KEY = "lotm_distort_action";   // next hit negated
    public static final String DISTORT_ACTION_STRONG_KEY = "lotm_distort_action_strong"; // seq5+ backlash
    public static final String DISTORT_TRAJ_KEY = "lotm_distort_traj";       // next projectile bent
    public static final String DISTORT_TRAJ_CHARGES_KEY = "lotm_distort_traj_charges";

    // Seq 5+ concept distortion: both entities share damage 50/50 for a short time.
    public static final String DISTORT_CONCEPT_KEY = "lotm_distort_concept";

    // Seq 4+ wound distortion: active cast that heals missing HP.
    public static final String DISTORT_WOUND_KEY = "lotm_distort_wound";

    // Seq 4+ wound distortion tuning.
    private static final float WOUND_DISTORT_HEAL_SEQ4 = 0.35f;

    // Tracks UUID -> caster UUID for intent distortion redirection.
    private static final Map<UUID, UUID> INTENT_DISTORTED = new HashMap<>();
    private static final Map<UUID, Long> INTENT_DISTORTED_UNTIL = new HashMap<>();

    // Seq 5+ concept-link state
    private static final Map<UUID, UUID> CONCEPT_LINKS = new HashMap<>();
    private static final Set<UUID> CONCEPT_LOCK = new HashSet<>();

    public DistortionAbility(String id) {
        super(id, 6.0f);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 55;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.distortion.distort_action",
                "ability.lotmcraft.distortion.distort_intent",
                "ability.lotmcraft.distortion.distort_trajectory",
                "ability.lotmcraft.distortion.distort_concept",
                "ability.lotmcraft.distortion.distort_wound"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (abilityIndex == 4) {
            if (!isSeq4Plus(entity)) {
                AbilityUtil.sendActionBar(entity,
                        Component.literal("Wound Distortion awakens at Seq 4.").withColor(0xFF5555));
                return;
            }
            distortWound(serverLevel, entity);
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("No target in range.").withColor(0xFF5555));
            return;
        }

        if (!canDistort(entity, target)) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("Target resists Distortion.").withColor(0xFF5555));
            return;
        }

        switch (abilityIndex) {
            case 0 -> distortAction(serverLevel, entity, target);
            case 1 -> distortIntent(serverLevel, entity, target);
            case 2 -> distortTrajectory(serverLevel, entity, target);
            case 3 -> {
                if (!isSeq5Plus(entity)) {
                    AbilityUtil.sendActionBar(entity,
                            Component.literal("Concept Distortion awakens at Seq 5.").withColor(0xFF5555));
                    return;
                }
                distortConcept(serverLevel, entity, target);
            }
            default -> {
            }
        }
    }

    // ----- Sub-abilities -----

    private void distortAction(ServerLevel level, LivingEntity caster, LivingEntity target) {
        boolean strong = isSeq5Plus(caster);

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), strong ? 1.0 : 0.7, strong ? 12 : 10);

        target.getPersistentData().putBoolean(DISTORT_ACTION_KEY, true);
        if (strong) {
            target.getPersistentData().putBoolean(DISTORT_ACTION_STRONG_KEY, true);
        }

        AbilityUtil.sendActionBar(caster,
                Component.literal("Action distorted.").withColor(0xAA77FF));

        playDistortionCastFX(level, caster, target, strong);

        ServerScheduler.scheduleDelayed(strong ? 20 * 8 : 20 * 6, () -> {
            target.getPersistentData().remove(DISTORT_ACTION_KEY);
            target.getPersistentData().remove(DISTORT_ACTION_STRONG_KEY);
        }, level);
    }

    private void distortIntent(ServerLevel level, LivingEntity caster, LivingEntity target) {
        boolean strong = isSeq5Plus(caster);

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), strong ? 1.0 : 0.7, strong ? 12 : 10);

        // Mark the target: their next hit on the caster gets bounced.
        INTENT_DISTORTED.put(target.getUUID(), caster.getUUID());
        INTENT_DISTORTED_UNTIL.put(target.getUUID(), level.getGameTime() + (strong ? 20 * 6 : 20 * 5));

        AbilityUtil.sendActionBar(caster,
                Component.literal("Intent distorted.").withColor(0xAA77FF));

        playDistortionCastFX(level, caster, target, strong);

        ServerScheduler.scheduleDelayed(strong ? 20 * 6 : 20 * 5, () -> {
            INTENT_DISTORTED.remove(target.getUUID());
            INTENT_DISTORTED_UNTIL.remove(target.getUUID());
        }, level);
    }

    private void distortTrajectory(ServerLevel level, LivingEntity caster, LivingEntity target) {
        boolean strong = isSeq5Plus(caster);

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), strong ? 1.0 : 0.8, strong ? 12 : 10);

        target.getPersistentData().putBoolean(DISTORT_TRAJ_KEY, true);
        target.getPersistentData().putInt(DISTORT_TRAJ_CHARGES_KEY, strong ? 2 : 1);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Trajectory distorted.").withColor(0xAA77FF));

        playDistortionCastFX(level, caster, target, strong);

        ServerScheduler.scheduleDelayed(strong ? 20 * 10 : 20 * 8, () -> {
            target.getPersistentData().remove(DISTORT_TRAJ_KEY);
            target.getPersistentData().remove(DISTORT_TRAJ_CHARGES_KEY);
        }, level);
    }

    private void distortConcept(ServerLevel level, LivingEntity caster, LivingEntity target) {
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 1.0, 14);

        UUID casterId = caster.getUUID();
        UUID targetId = target.getUUID();

        CONCEPT_LINKS.put(casterId, targetId);
        CONCEPT_LINKS.put(targetId, casterId);

        target.getPersistentData().putBoolean(DISTORT_CONCEPT_KEY, true);
        caster.getPersistentData().putBoolean(DISTORT_CONCEPT_KEY, true);

        RingEffectManager.createRingForAll(target.position(), 2.8f, 22,
                0.72f, 0.12f, 0.90f, 1.0f, 0.18f, 1.1f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.2, 18);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Concept distorted.").withColor(0xAA77FF));

        playDistortionCastFX(level, caster, target, true);

        ServerScheduler.scheduleDelayed(20 * 8, () -> {
            CONCEPT_LINKS.remove(casterId);
            CONCEPT_LINKS.remove(targetId);
            caster.getPersistentData().remove(DISTORT_CONCEPT_KEY);
            target.getPersistentData().remove(DISTORT_CONCEPT_KEY);
        }, level);
    }

    private void distortWound(ServerLevel level, LivingEntity caster) {
        playDistortionCastFX(level, caster, caster, true);

        float missingHp = Math.max(0.0f, caster.getMaxHealth() - caster.getHealth());
        float healAmount = missingHp * WOUND_DISTORT_HEAL_SEQ4;
        if (healAmount > 0.0f) {
            caster.heal(healAmount);
        }

        RingEffectManager.createRingForAll(caster.position(), 2.8f, 22,
                0.72f, 0.12f, 0.90f, 1.0f, 0.18f, 1.1f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 1.2, 18);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Wound distorted.").withColor(0xAA77FF));
    }

    private static void playDistortionCastFX(ServerLevel level, LivingEntity caster, LivingEntity target, boolean strong) {
        double casterRingSize = strong ? 3.2f : 2.6f;
        double targetRingSize = strong ? 2.2f : 1.6f;

        RingEffectManager.createRingForAll(caster.position(), (float) casterRingSize, strong ? 24 : 16,
                0.62f, 0.12f, 0.88f, 1.0f, 0.18f, strong ? 1.1f : 0.75f, level);

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), strong ? 1.6 : 1.0, strong ? 24 : 14);

        RingEffectManager.createRingForAll(target.position(), (float) targetRingSize, strong ? 16 : 12,
                0.56f, 0.18f, 0.92f, 0.90f, 0.14f, strong ? 0.7f : 0.45f, level);

        ParticleUtil.createParticleSpirals(level, ModParticles.LIGHTNING.get(),
                target.position().add(0, 0.5, 0),
                strong ? 0.22 : 0.15,
                strong ? 0.95 : 0.70,
                strong ? 1.5 : 1.0,
                0.06,
                strong ? 10 : 6,
                strong ? 26 : 14,
                2,
                2);
    }

    private boolean canDistort(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        if (targetSeq >= casterSeq) return true;
        if (targetSeq == 0 && casterSeq != 0) return false;

        int diff = casterSeq - targetSeq;
        if (diff == 1) return random.nextFloat() < 0.35f;
        return false;
    }

    private boolean isSeq5Plus(LivingEntity entity) {
        return BeyonderData.isBeyonder(entity) && BeyonderData.getSequence(entity) <= 5;
    }

    private boolean isSeq4Plus(LivingEntity entity) {
        return BeyonderData.isBeyonder(entity)
                && "black_emperor".equals(BeyonderData.getPathway(entity))
                && BeyonderData.getSequence(entity) <= 4;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        var source = event.getSource();
        var directEntity = source.getDirectEntity();

        LivingEntity attacker = null;
        if (directEntity instanceof LivingEntity le) {
            attacker = le;
        } else if (directEntity instanceof Projectile p
                && p.getOwner() instanceof LivingEntity le) {
            attacker = le;
        }

        LivingEntity victim = event.getEntity();

        // Intent distortion: bounce the hit, then move harmful effects too.
        if (attacker != null) {
            UUID distortedCasterId = INTENT_DISTORTED.get(attacker.getUUID());
            Long distortedUntil = INTENT_DISTORTED_UNTIL.get(attacker.getUUID());

            if (distortedCasterId != null
                    && distortedUntil != null
                    && distortedUntil >= victim.level().getGameTime()
                    && victim.getUUID().equals(distortedCasterId)
                    && victim.level() instanceof ServerLevel serverLevel) {

                float dmg = event.getNewDamage();
                if (dmg > 0.0f) {
                    LivingEntity redirectTarget = findNearestLivingToCaster(serverLevel, victim, attacker, 16.0D);

                    event.setNewDamage(0.0f);

                    // Consume the mark on first successful redirect.
                    INTENT_DISTORTED.remove(attacker.getUUID());
                    INTENT_DISTORTED_UNTIL.remove(attacker.getUUID());

                    // Move harmful active effects off the victim and onto the redirection target.
                    if (redirectTarget != null) {
                        transferHarmfulEffects(victim, redirectTarget);
                        redirectTarget.hurt(source, dmg);

                        ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                                redirectTarget.position().add(0, 1, 0), 1.0, 14);
                    } else {
                        clearHarmfulEffects(victim);
                    }

                    return;
                }
            }
        }

        if (attacker == null) {
            applyConceptSplitIfLinked(victim, event);
            return;
        }

        if (attacker.getPersistentData().getBoolean(DISTORT_ACTION_KEY)) {
            attacker.getPersistentData().remove(DISTORT_ACTION_KEY);
            event.setNewDamage(0);

            if (attacker.getPersistentData().getBoolean(DISTORT_ACTION_STRONG_KEY)) {
                attacker.getPersistentData().remove(DISTORT_ACTION_STRONG_KEY);
                attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, false));
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));
            }

            return;
        }

        applyConceptSplitIfLinked(victim, event);
    }

    @SubscribeEvent
    public static void onProjectileFired(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (!(projectile.getOwner() instanceof LivingEntity owner)) return;

        if (!owner.getPersistentData().getBoolean(DISTORT_TRAJ_KEY)) return;

        int charges = owner.getPersistentData().getInt(DISTORT_TRAJ_CHARGES_KEY);
        if (charges <= 0) charges = 1;

        charges--;
        if (charges <= 0) {
            owner.getPersistentData().remove(DISTORT_TRAJ_KEY);
            owner.getPersistentData().remove(DISTORT_TRAJ_CHARGES_KEY);
        } else {
            owner.getPersistentData().putInt(DISTORT_TRAJ_CHARGES_KEY, charges);
        }

        var vel = projectile.getDeltaMovement();
        double angle = Math.toRadians(60 + owner.level().random.nextInt(60));
        double newX = vel.x * Math.cos(angle) - vel.z * Math.sin(angle);
        double newZ = vel.x * Math.sin(angle) + vel.z * Math.cos(angle);
        projectile.setDeltaMovement(newX, vel.y, newZ);
        projectile.hasImpulse = true;
    }

    private static void transferHarmfulEffects(LivingEntity source, LivingEntity destination) {
        if (destination == null || source == destination) return;

        // Move harmful potion-style effects only.
        List<MobEffectInstance> harmful = source.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL)
                .map(MobEffectInstance::new)
                .toList();

        for (MobEffectInstance effect : harmful) {
            source.removeEffect(effect.getEffect());
            destination.addEffect(effect);
        }
    }

    private static void clearHarmfulEffects(LivingEntity source) {
        source.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL)
                .toList()
                .forEach(source::removeEffect);
    }

    private static void applyConceptSplitIfLinked(LivingEntity victim, LivingDamageEvent.Pre event) {
        UUID partnerId = CONCEPT_LINKS.get(victim.getUUID());
        if (partnerId == null) return;
        if (!(victim.level() instanceof ServerLevel serverLevel)) return;

        LivingEntity partner = findLivingByUuid(serverLevel, victim, partnerId);
        if (partner == null || !partner.isAlive() || partner.getUUID().equals(victim.getUUID())) return;

        float dmg = event.getNewDamage();
        if (dmg <= 0.0f) return;

        event.setNewDamage(dmg * 0.5f);

        if (CONCEPT_LOCK.contains(victim.getUUID()) || CONCEPT_LOCK.contains(partner.getUUID())) {
            return;
        }

        CONCEPT_LOCK.add(victim.getUUID());
        CONCEPT_LOCK.add(partner.getUUID());

        try {
            partner.hurt(victim.damageSources().mobAttack(victim), dmg * 0.5f);
        } finally {
            CONCEPT_LOCK.remove(victim.getUUID());
            CONCEPT_LOCK.remove(partner.getUUID());
        }
    }

    private static LivingEntity findNearestLivingToCaster(ServerLevel level, LivingEntity caster, LivingEntity attacker, double radius) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e != caster && e != attacker && !e.isAlliedTo(caster)
        );

        LivingEntity closest = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity candidate : nearby) {
            double dist = candidate.distanceToSqr(caster);
            if (dist < bestDist) {
                bestDist = dist;
                closest = candidate;
            }
        }

        return closest;
    }

    private static LivingEntity findLivingByUuid(ServerLevel level, LivingEntity source, UUID uuid) {
        return level.getEntitiesOfClass(
                LivingEntity.class,
                source.getBoundingBox().inflate(128.0D),
                e -> e.getUUID().equals(uuid)
        ).stream().findFirst().orElse(null);
    }
}