package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CommandingPresenceAbility extends ToggleAbility {

    // S0: active casters
    private static final Set<UUID> ACTIVE_CASTERS = new HashSet<>();

    // S1: aura tracking
    private static final Map<UUID, UUID> UNDER_PRESENCE = new HashMap<>();
    private static final Map<UUID, Long> LAST_MSG_TICK = new HashMap<>();

    // S2: retaliation
    private static final Map<UUID, UUID> RETALIATING_MOBS = new HashMap<>();
    private static final Map<UUID, Long> RETALIATING_MOBS_UNTIL = new HashMap<>();

    // Damage reflection debuff: attacker deals 50% less damage for a short window
    private static final Map<UUID, Long> REFLECTED_UNTIL = new HashMap<>();
    private static final long REFLECT_DURATION_TICKS = 60L;

    // S3: drain window
    private static final Map<UUID, UUID> PRESENCE_SPIRITUALITY_DRAIN = new HashMap<>();
    private static final Map<UUID, Long> PRESENCE_SPIRITUALITY_DRAIN_UNTIL = new HashMap<>();

    // S4: restore caster movement
    private static final String SHARED_SCALE_BACKUP_KEY = "lotm_shared_scale_backup";
    private static final String COMMANDING_STEP_BACKUP_KEY = "lotm_commanding_presence_step_backup";

    // Seq 2: step over a full block and keep the boost alive.
    private static final float DUKE_STEP_HEIGHT = 2.0F;

    private static final String[] REVERENCE_MESSAGES = {
            "An overwhelming sense of authority washes over you...",
            "You feel an inexplicable urge to lower your head.",
            "The weight of majesty presses down upon your shoulders.",
            "Every instinct within you screams to submit."
    };

    public CommandingPresenceAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 4.0f;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ACTIVE_CASTERS.add(entity.getUUID());
        entity.getPersistentData().putBoolean("lotm_commanding_presence_active", true);

        boolean dukeTier = isDukeTier(entity);

        // S2: Duke tier gets the bigger body.
        AttributeInstance scaleAttr = entity.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            if (!entity.getPersistentData().contains(SHARED_SCALE_BACKUP_KEY)) {
                entity.getPersistentData().putDouble(SHARED_SCALE_BACKUP_KEY, scaleAttr.getBaseValue());
            }
            scaleAttr.setBaseValue(dukeTier ? 2.0D : 1.5D);
            entity.refreshDimensions();
        }

        // S2: step height is enabled once, then restored on stop.
        if (dukeTier) {
            saveAndForceStepHeight(entity, DUKE_STEP_HEIGHT);
            entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false, false));
        }

        // S1/S2: opening burst scales with authority.
        boolean presenceAuraTier = isPresenceAuraTier(entity);
        double presenceScale = getPresenceScale(entity);

        RingEffectManager.createPulsingRingForAll(
                entity.position(),
                dukeTier ? 44f : (presenceAuraTier ? (float) (22f * presenceScale) : 18f),
                dukeTier ? 4 : (presenceAuraTier ? 3 : 2),
                dukeTier ? 32 : (presenceAuraTier ? 28 : 24),
                dukeTier ? 10 : (presenceAuraTier ? 12 : 10),
                0.15f, 0.0f, 0.25f, 1.0f, 0.20f, 1.2f, serverLevel
        );

        ParticleUtil.spawnSphereParticles(
                serverLevel,
                ModParticles.BLACK.get(),
                entity.position().add(0, 1, 0),
                dukeTier ? 6.0 : (presenceAuraTier ? 5.0 * presenceScale : 4.2),
                dukeTier ? 96 : (presenceAuraTier ? (int) (68 * presenceScale) : 54)
        );

        ParticleUtil.createParticleSpirals(
                serverLevel,
                ModParticles.LIGHTNING.get(),
                entity.position().add(0, 0.5, 0),
                dukeTier ? 0.55 : (presenceAuraTier ? 0.42 * presenceScale : 0.35),
                dukeTier ? 1.70 : (presenceAuraTier ? 1.35 * presenceScale : 1.2),
                dukeTier ? 2.80 : (presenceAuraTier ? 2.3 * presenceScale : 2.0),
                0.08,
                dukeTier ? 24 : (presenceAuraTier ? (int) (18 * presenceScale) : 14),
                dukeTier ? 56 : (presenceAuraTier ? (int) (42 * presenceScale) : 36),
                2, 3
        );

        entity.sendSystemMessage(Component.literal("§5Commanding Presence: ON"));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        int selfSeq = BeyonderData.getSequence(entity);
        boolean presenceAuraTier = selfSeq <= 3;
        boolean dukeTier = selfSeq <= 2;
        boolean seq5LegacyTier = selfSeq == 5;

        double presenceScale = getPresenceScale(entity);
        double auraRange = dukeTier ? 36.0D : 18.0D;

        long gameTick = level.getGameTime();
        UUID casterId = entity.getUUID();


        AttributeInstance scaleAttr = entity.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            double wantedScale = dukeTier ? 2.0D : 1.5D;
            if (scaleAttr.getBaseValue() != wantedScale) {
                scaleAttr.setBaseValue(wantedScale);
                entity.refreshDimensions();
            }
        }

        // S2: keep jump boost alive while active.
        // Step height is not forced here anymore; it is only enabled in start() and restored in stop().
        if (dukeTier && gameTick % 20 == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false, false));
            entity.fallDistance = Math.max(0.0F, entity.fallDistance - 2.0F);
        }

        Set<UUID> seenSubjects = new HashSet<>();

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, entity.getBoundingBox().inflate(auraRange))) {

            if (target == entity) continue;

            boolean isBeyonder = BeyonderData.isBeyonder(target);
            int targetSeq = isBeyonder ? BeyonderData.getSequence(target) : Integer.MAX_VALUE;

            if (isBeyonder && targetSeq < selfSeq && !(target instanceof Player)) {
                continue;
            }


            seenSubjects.add(target.getUUID());
            UNDER_PRESENCE.put(target.getUUID(), casterId);

            boolean headDownTarget = target instanceof Player || isHeavilyPressured(target, selfSeq);

            if (target instanceof Mob mob) {
                UUID mobId = mob.getUUID();
                UUID retaliateCaster = RETALIATING_MOBS.get(mobId);
                Long retaliateUntil = RETALIATING_MOBS_UNTIL.get(mobId);

                boolean canRetaliate =
                        retaliateCaster != null
                                && retaliateCaster.equals(casterId)
                                && retaliateUntil != null
                                && retaliateUntil >= gameTick;

                if (canRetaliate) {
                    mob.setTarget(entity);
                } else {
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                }

                if (presenceAuraTier && headDownTarget) {
                    lockHeadDown(target, entity, selfSeq);
                }

                if (seq5LegacyTier && gameTick % 40 == 0) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));
                }
            }

            if (target instanceof Player player) {
                if (seq5LegacyTier && gameTick % 40 == 0) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, 50, 0, false, false, false));

                    if (targetSeq >= selfSeq) {
                        player.addEffect(new MobEffectInstance(
                                MobEffects.WEAKNESS, 50, 0, false, false, false));
                    }
                }

                if (presenceAuraTier && headDownTarget) {
                    lockHeadDown(target, entity, selfSeq);

                    if (gameTick % 40 == 0) {
                        player.addEffect(new MobEffectInstance(
                                MobEffects.CONFUSION, 30, 0, false, false, false));
                    }
                }

                if (dukeTier && gameTick % 4 == 0) {
                    player.setDeltaMovement(player.getDeltaMovement().add(
                            (serverLevel.random.nextDouble() - 0.5D) * 0.06D,
                            0.0D,
                            (serverLevel.random.nextDouble() - 0.5D) * 0.06D
                    ));
                    player.hurtMarked = true;
                }

                long lastMsg = LAST_MSG_TICK.getOrDefault(player.getUUID(), 0L);
                if (gameTick - lastMsg >= 100) {
                    LAST_MSG_TICK.put(player.getUUID(), gameTick);
                    String msg = REVERENCE_MESSAGES[(int) ((gameTick / 100) % REVERENCE_MESSAGES.length)];
                    AbilityUtil.sendActionBar(player, Component.literal(msg).withColor(0x8800CC));
                }
            } else {
                if (presenceAuraTier && headDownTarget && gameTick % 40 == 0) {
                    target.addEffect(new MobEffectInstance(
                            MobEffects.CONFUSION, 30, 0, false, false, false));
                }
            }

            if (dukeTier && isBeyonder && targetSeq >= selfSeq && gameTick % 20 == 0) {
                float drainAmount = 0.75f + Math.max(0, targetSeq - selfSeq) * 0.35f;
                drainSpirituality(target, drainAmount);
            }

            UUID drainCaster = PRESENCE_SPIRITUALITY_DRAIN.get(target.getUUID());
            Long drainUntil = PRESENCE_SPIRITUALITY_DRAIN_UNTIL.get(target.getUUID());

            if (seq5LegacyTier
                    && isBeyonder
                    && drainCaster != null
                    && drainCaster.equals(casterId)
                    && drainUntil != null
                    && drainUntil >= gameTick
                    && targetSeq <= selfSeq
                    && gameTick % 20 == 0) {

                float drainAmount = 1.0f + Math.max(0, selfSeq - targetSeq) * 0.25f;
                drainSpirituality(target, drainAmount);
            }
        }

        if (presenceAuraTier && gameTick % 6 == 0) {
            RingEffectManager.createRingForAll(
                    entity.position(),
                    (float) (19f * presenceScale), 20,
                    0.62f, 0.12f, 0.82f, 0.95f, 0.18f, 1.05f, serverLevel
            );

            ParticleUtil.spawnSphereParticles(
                    serverLevel,
                    ModParticles.BLACK.get(),
                    entity.position().add(0, 1, 0),
                    3.0 * presenceScale,
                    Math.max(24, (int) (40 * presenceScale))
            );

            ParticleUtil.createParticleSpirals(
                    serverLevel,
                    ModParticles.LIGHTNING.get(),
                    entity.position().add(0, 0.5, 0),
                    0.24 * presenceScale,
                    1.0 * presenceScale,
                    1.6 * presenceScale,
                    0.07,
                    10,
                    24,
                    2, 2
            );
        }

        if (dukeTier && gameTick % 4 == 0) {
            RingEffectManager.createRingForAll(
                    entity.position(),
                    38f, 24,
                    0.72f, 0.16f, 0.92f, 1.0f, 0.20f, 1.15f, serverLevel
            );

            ParticleUtil.spawnSphereParticles(
                    serverLevel,
                    ModParticles.BLACK.get(),
                    entity.position().add(0, 1, 0),
                    6.0,
                    72
            );
        }

        if (seq5LegacyTier && gameTick % 18 == 0) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5D) * 2.5D;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5D) * 2.5D;

            RingEffectManager.createRingForAll(
                    entity.position().add(offsetX, 0.0D, offsetZ),
                    16f, 18,
                    0.55f, 0.12f, 0.72f, 0.75f, 0.16f, 0.85f, serverLevel
            );

            RingEffectManager.createRingForAll(
                    entity.position().add(-offsetX, 0.0D, -offsetZ),
                    12f, 16,
                    0.40f, 0.08f, 0.60f, 0.55f, 0.14f, 0.65f, serverLevel
            );
        }

        UNDER_PRESENCE.entrySet().removeIf(e -> e.getValue().equals(casterId) && !seenSubjects.contains(e.getKey()));
        LAST_MSG_TICK.keySet().removeIf(id -> !seenSubjects.contains(id));
        RETALIATING_MOBS.keySet().removeIf(id -> !seenSubjects.contains(id));
        RETALIATING_MOBS_UNTIL.keySet().removeIf(id -> !RETALIATING_MOBS.containsKey(id));
        PRESENCE_SPIRITUALITY_DRAIN.keySet().removeIf(id -> !seenSubjects.contains(id));
        PRESENCE_SPIRITUALITY_DRAIN_UNTIL.keySet().removeIf(id -> !PRESENCE_SPIRITUALITY_DRAIN.containsKey(id));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        UUID casterId = entity.getUUID();
        ACTIVE_CASTERS.remove(casterId);
        entity.getPersistentData().remove("lotm_commanding_presence_active");

        // S4: restore size
        AttributeInstance scaleAttr = entity.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            if (entity.getPersistentData().contains(SHARED_SCALE_BACKUP_KEY)) {
                scaleAttr.setBaseValue(entity.getPersistentData().getDouble(SHARED_SCALE_BACKUP_KEY));
                entity.getPersistentData().remove(SHARED_SCALE_BACKUP_KEY);
            }
            entity.refreshDimensions();
        }

        // S4: restore step height
        restoreStepHeight(entity);

        RingEffectManager.createPulsingRingForAll(entity.position(), 18, 2, 24, 10,
                0.15f, 0.0f, 0.25f, 1.0f, 0.20f, 1.2f, serverLevel);
        ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                entity.position().add(0, 1, 0), 3.5, 34);

        ParticleUtil.createParticleSpirals(serverLevel, ModParticles.LIGHTNING.get(),
                entity.position().add(0, 0.5, 0),
                0.35, 1.2, 2.0, 0.08, 14, 36, 2, 3);

        Set<UUID> subjects = new HashSet<>();
        UNDER_PRESENCE.entrySet().stream()
                .filter(e -> e.getValue().equals(casterId))
                .map(Map.Entry::getKey)
                .forEach(subjects::add);

        subjects.forEach(id -> {
            UNDER_PRESENCE.remove(id);
            LAST_MSG_TICK.remove(id);
            RETALIATING_MOBS.remove(id);
            RETALIATING_MOBS_UNTIL.remove(id);
            PRESENCE_SPIRITUALITY_DRAIN.remove(id);
            PRESENCE_SPIRITUALITY_DRAIN_UNTIL.remove(id);
        });

        entity.sendSystemMessage(Component.literal("§cCommanding Presence: OFF"));
    }

    @SubscribeEvent
    public static void onPresenceChallenged(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null) return;

        if (ACTIVE_CASTERS.contains(attacker.getUUID()) && victim instanceof Monster) {
            RETALIATING_MOBS.put(victim.getUUID(), attacker.getUUID());
            RETALIATING_MOBS_UNTIL.put(victim.getUUID(), victim.level().getGameTime() + 20 * 8);
        }

        if (!ACTIVE_CASTERS.contains(victim.getUUID())) return;

        UUID presenceCaster = UNDER_PRESENCE.get(attacker.getUUID());
        if (presenceCaster == null || !presenceCaster.equals(victim.getUUID())) return;

        if (BeyonderData.isBeyonder(attacker)) {
            int attackerSeq = BeyonderData.getSequence(attacker);
            int casterSeq = BeyonderData.getSequence(victim);

            if (casterSeq == 5 && attackerSeq <= casterSeq) {
                PRESENCE_SPIRITUALITY_DRAIN.put(attacker.getUUID(), victim.getUUID());
                PRESENCE_SPIRITUALITY_DRAIN_UNTIL.put(attacker.getUUID(), victim.level().getGameTime() + 20 * 8);

                drainSpirituality(attacker, 1.0f + Math.max(0, casterSeq - attackerSeq) * 0.25f);
            }
        }

        // Debuff attacker's outgoing damage by 50% for 3 seconds
        REFLECTED_UNTIL.put(attacker.getUUID(), attacker.level().getGameTime() + REFLECT_DURATION_TICKS);

        var pushDir = attacker.position().subtract(victim.position()).normalize();
        attacker.setDeltaMovement(pushDir.x * 0.45, 0.28, pushDir.z * 0.45);
        attacker.hasImpulse = true;

        if (attacker instanceof Player player) {
            AbilityUtil.sendActionBar(player,
                    Component.literal("Fighting the presence exhausts you.")
                            .withColor(0x8800CC));
        }
    }

    @SubscribeEvent
    public static void onReflectedDamage(LivingDamageEvent.Pre event) {
        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null) return;

        Long until = REFLECTED_UNTIL.get(attacker.getUUID());
        if (until == null) return;

        if (attacker.level().getGameTime() > until) {
            REFLECTED_UNTIL.remove(attacker.getUUID());
            return;
        }

        event.setNewDamage(event.getNewDamage() * 0.5f);
    }

    private static LivingEntity resolveAttacker(DamageSource source) {
        var direct = source.getDirectEntity();
        if (direct instanceof LivingEntity le) return le;
        if (direct instanceof Projectile p && p.getOwner() instanceof LivingEntity le) return le;

        var owner = source.getEntity();
        if (owner instanceof LivingEntity le) return le;

        return null;
    }

    private static void drainSpirituality(LivingEntity entity, float amount) {
        float current = BeyonderData.getSpirituality(entity);
        float next = Math.max(0.0f, current - amount);
        trySetSpirituality(entity, next);
    }

    private static void trySetSpirituality(LivingEntity entity, float value) {
        String[] names = {
                "setSpirituality",
                "setCurrentSpirituality",
                "setSpiritualityValue",
                "setSpirit"
        };

        for (String methodName : names) {
            try {
                Method method = BeyonderData.class.getDeclaredMethod(methodName, LivingEntity.class, float.class);
                method.setAccessible(true);
                method.invoke(null, entity, value);
                return;
            } catch (ReflectiveOperationException ignored) {
            }

            try {
                Method method = BeyonderData.class.getDeclaredMethod(methodName, LivingEntity.class, double.class);
                method.setAccessible(true);
                method.invoke(null, entity, (double) value);
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private static boolean isHeavilyPressured(LivingEntity target, int casterSeq) {
        if (!BeyonderData.isBeyonder(target)) return true;
        return BeyonderData.getSequence(target) >= casterSeq + 2;
    }

    private static void lockHeadDown(LivingEntity target, LivingEntity caster, int casterSeq) {
        float forcedPitch;

        if (!BeyonderData.isBeyonder(target)) {
            forcedPitch = Math.max(target.getXRot(), 40.0f);
        } else {
            int targetSeq = BeyonderData.getSequence(target);
            if (targetSeq < casterSeq + 2 && !(target instanceof Player)) {
                return;
            }
            forcedPitch = 36.0f + Math.min(18.0f, Math.max(0, targetSeq - casterSeq) * 4.0f);
        }

        target.setXRot(forcedPitch);
        target.xRotO = forcedPitch;
        target.setYRot(target.getYRot());
        target.yRotO = target.getYRot();
        target.yBodyRot = target.getYRot();
        target.yBodyRotO = target.getYRot();
        target.yHeadRot = target.getYRot();
        target.yHeadRotO = target.getYRot();
        target.setDeltaMovement(target.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));

        if (target instanceof ServerPlayer player) {
            player.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), forcedPitch);
            player.hurtMarked = true;
        }
    }

    public static boolean isActive(UUID uuid) {
        return ACTIVE_CASTERS.contains(uuid);
    }

    private static boolean isPresenceAuraTier(LivingEntity entity) {
        return BeyonderData.getSequence(entity) <= 3;
    }

    private static boolean isDukeTier(LivingEntity entity) {
        return BeyonderData.getSequence(entity) <= 2;
    }

    private static double getPresenceScale(LivingEntity entity) {
        return isDukeTier(entity) ? 2.0D : 1.0D + Math.max(0, 3 - BeyonderData.getSequence(entity)) * 0.20D;
    }

    private static void saveAndForceStepHeight(LivingEntity entity, float value) {
        if (!entity.getPersistentData().contains(COMMANDING_STEP_BACKUP_KEY)) {
            entity.getPersistentData().putFloat(COMMANDING_STEP_BACKUP_KEY, readStepHeight(entity));
        }
        setStepHeight(entity, value);
    }

    private static void restoreStepHeight(LivingEntity entity) {
        if (!entity.getPersistentData().contains(COMMANDING_STEP_BACKUP_KEY)) return;

        setStepHeight(entity, entity.getPersistentData().getFloat(COMMANDING_STEP_BACKUP_KEY));
        entity.getPersistentData().remove(COMMANDING_STEP_BACKUP_KEY);
    }

    private static float readStepHeight(LivingEntity entity) {
        try {
            Field field = net.minecraft.world.entity.Entity.class.getDeclaredField("maxUpStep");
            field.setAccessible(true);
            return field.getFloat(entity);
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method getter = entity.getClass().getMethod("getMaxUpStep");
            return ((Number) getter.invoke(entity)).floatValue();
        } catch (ReflectiveOperationException ignored) {
        }

        return 0.6F;
    }

    private static void setStepHeight(LivingEntity entity, float value) {
        try {
            Field field = net.minecraft.world.entity.Entity.class.getDeclaredField("maxUpStep");
            field.setAccessible(true);
            field.setFloat(entity, value);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method setter = entity.getClass().getMethod("setMaxUpStep", float.class);
            setter.invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}