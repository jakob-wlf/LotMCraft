package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.FoolingComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * State holder and event handler for all Fool pathway "Fooling" abilities.
 * The individual cast behaviours have been separated into their own Ability classes.
 * This class retains only the shared static state and the game-event listeners that
 * service those abilities at runtime.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FoolingAbility {

    static final int HISTORY_DURATION_TICKS = 20 * 30;
    static final int FATE_DURATION_TICKS = 20 * 60;
    static final int BLIND_STUPIDITY_TICKS = 20 * 20;
    static final int REALM_RADIUS = 24;
    static final int HISTORY_SPIRIT_PER_SECOND = 40;
    static final int REALM_SPIRIT_PER_SECOND = 85;

    static final Map<UUID, ArrayDeque<TemporalSnapshot>> POSITION_HISTORY = new HashMap<>();
    static final Map<UUID, HistoryMask> HISTORY_MASKS = new HashMap<>();
    static final Map<UUID, FateProtection> FATE_PROTECTIONS = new HashMap<>();
    static final Map<UUID, Long> SPIRIT_WORLD_SUMMONS = new HashMap<>();
    static final Set<UUID> ACTIVE_REALMS = new HashSet<>();
    static final Map<UUID, RealmDomain> REALM_DOMAINS = new HashMap<>();

    // Private constructor – not instantiated
    private FoolingAbility() {}

    // =========================================================================
    // Public API used by BeyonderData
    // =========================================================================

    public static Integer getFooledSequenceOverride(LivingEntity entity) {
        HistoryMask mask = HISTORY_MASKS.get(entity.getUUID());
        if (mask == null) {
            return null;
        }
        if (entity.level().getGameTime() > mask.expiresAt) {
            HISTORY_MASKS.remove(entity.getUUID());
            return null;
        }
        return mask.fooledSequence;
    }

    public static float getRealmPowerMultiplier(LivingEntity entity, Ability ability) {
        return isRealmEmpowered(entity, ability) ? 1.65f : 1.0f;
    }

    public static float getRealmSpiritualityCostMultiplier(LivingEntity entity, Ability ability) {
        return isRealmEmpowered(entity, ability) ? 0.6f : 1.0f;
    }

    public static float getRealmCooldownMultiplier(LivingEntity entity, Ability ability) {
        return isRealmEmpowered(entity, ability) ? 0.55f : 1.0f;
    }

    // =========================================================================
    // Package-private helpers used by individual Fool ability classes
    // =========================================================================

    static void activateRealm(Player caster, ServerLevel level, Vec3 center, int radius, int durationTicks) {
        ACTIVE_REALMS.add(caster.getUUID());
        REALM_DOMAINS.put(caster.getUUID(),
                new RealmDomain(caster.getUUID(), center, radius, level.getGameTime() + durationTicks, level.dimension()));
    }

    static void deactivateRealm(UUID casterId) {
        ACTIVE_REALMS.remove(casterId);
        REALM_DOMAINS.remove(casterId);
    }

    static int getUnmaskedSequence(LivingEntity entity) {
        return entity.getData(ModAttachments.BEYONDER_COMPONENT).getSequence();
    }

    static TemporalSnapshot getOldestSnapshot(LivingEntity entity) {
        ArrayDeque<TemporalSnapshot> history = POSITION_HISTORY.get(entity.getUUID());
        if (history == null || history.isEmpty()) {
            return null;
        }
        return history.peekFirst();
    }

    static Vec3 findRandomFuturePosition(ServerLevel level, net.minecraft.core.BlockPos origin) {
        int x = origin.getX() + level.random.nextInt(-32, 33);
        int z = origin.getZ() + level.random.nextInt(-32, 33);
        net.minecraft.core.BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new net.minecraft.core.BlockPos(x, origin.getY(), z));
        return new Vec3(top.getX() + 0.5, top.getY() + 1.0, top.getZ() + 0.5);
    }

    static void playPulse(ServerLevel level, Vec3 center, net.minecraft.core.particles.ParticleOptions particle, int count, double radius) {
        level.sendParticles(particle, center.x, center.y + 1.0, center.z, count, radius, 0.7, radius, 0.02);
    }

    static void restoreBattleReadyState(LivingEntity target) {
        target.setHealth(target.getMaxHealth());
        target.setRemainingFireTicks(0);
        target.getData(ModAttachments.FOOLING_COMPONENT).setTicksRemaining(0);
        target.removeEffect(ModEffects.LOOSING_CONTROL);
        target.removeEffect(ModEffects.FOOLING);
        target.removeEffect(MobEffects.BLINDNESS);
        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        target.removeEffect(MobEffects.WEAKNESS);
        target.removeEffect(MobEffects.CONFUSION);
        target.removeEffect(MobEffects.POISON);
        target.removeEffect(MobEffects.WITHER);
        target.removeEffect(MobEffects.HUNGER);

        AbilityCooldownComponent cooldowns = target.getData(ModAttachments.COOLDOWN_COMPONENT);
        cooldowns.removeAllCooldowns();

        if (target instanceof Player) {
            target.getData(ModAttachments.BEYONDER_COMPONENT).setSpirituality(BeyonderData.getMaxSpirituality(BeyonderData.getPathway(target), getUnmaskedSequence(target)));
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || !(living.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (living.tickCount % 10 == 0) {
            POSITION_HISTORY.computeIfAbsent(living.getUUID(), ignored -> new ArrayDeque<>());
            ArrayDeque<TemporalSnapshot> history = POSITION_HISTORY.get(living.getUUID());
            history.addLast(new TemporalSnapshot(living.position(), living.getHealth(), BeyonderData.getSpirituality(living)));
            while (history.size() > 24) {
                history.removeFirst();
            }
        }

        HistoryMask mask = HISTORY_MASKS.get(living.getUUID());
        if (mask != null && serverLevel.getGameTime() > mask.expiresAt) {
            HISTORY_MASKS.remove(living.getUUID());
        }

        FateProtection fateProtection = FATE_PROTECTIONS.get(living.getUUID());
        if (fateProtection != null && serverLevel.getGameTime() > fateProtection.expiresAt) {
            FATE_PROTECTIONS.remove(living.getUUID());
        }

        Long summonExpiry = SPIRIT_WORLD_SUMMONS.get(living.getUUID());
        if (summonExpiry != null && serverLevel.getGameTime() > summonExpiry) {
            SPIRIT_WORLD_SUMMONS.remove(living.getUUID());
            living.discard();
        }

        if (ACTIVE_REALMS.contains(living.getUUID()) && living.tickCount % 10 == 0) {
            RealmDomain realm = REALM_DOMAINS.get(living.getUUID());
            if (realm != null && realm.dimension.equals(serverLevel.dimension())) {
                playPulse(serverLevel, realm.center, ParticleTypes.DRAGON_BREATH, 45, 4.2);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        RealmDomain ownedRealm = REALM_DOMAINS.get(player.getUUID());
        if (ownedRealm != null && serverLevel.getGameTime() > ownedRealm.expiresAt) {
            deactivateRealm(player.getUUID());
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.fooling.realm_disabled").withColor(0xAA7CFF));
        }

        if (player.tickCount % 10 == 0) {
            FoolingComponent fooling = player.getData(ModAttachments.FOOLING_COMPONENT);
            if (fooling.isFooled()) {
                shuffleInventory(player);
                player.getInventory().selected = serverLevel.random.nextInt(9);
            }
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        int activeHistoryCount = 0;
        for (HistoryMask mask : HISTORY_MASKS.values()) {
            if (mask.casterId.equals(player.getUUID())) {
                activeHistoryCount++;
            }
        }

        if (activeHistoryCount > 0) {
            float required = activeHistoryCount * HISTORY_SPIRIT_PER_SECOND;
            if (BeyonderData.getSpirituality(player) <= required) {
                HISTORY_MASKS.entrySet().removeIf(entry -> entry.getValue().casterId.equals(player.getUUID()));
            } else {
                BeyonderData.reduceSpirituality(player, required);
            }
        }

        if (ACTIVE_REALMS.contains(player.getUUID())) {
            RealmDomain realm = REALM_DOMAINS.get(player.getUUID());
            if (realm == null || !realm.dimension.equals(serverLevel.dimension())) {
                deactivateRealm(player.getUUID());
                return;
            }

            if (BeyonderData.getSpirituality(player) <= REALM_SPIRIT_PER_SECOND) {
                deactivateRealm(player.getUUID());
                AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.fooling.realm_disabled").withColor(0xAA7CFF));
                return;
            }

            BeyonderData.reduceSpirituality(player, REALM_SPIRIT_PER_SECOND);
            applyRealmAura(serverLevel, player, realm);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLosingControlDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(ModDamageTypes.LOOSING_CONTROL)) {
            return;
        }

        FateProtection protection = FATE_PROTECTIONS.remove(event.getEntity().getUUID());
        if (protection == null) {
            return;
        }

        event.setCanceled(true);
        event.getEntity().removeEffect(ModEffects.LOOSING_CONTROL);
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            playPulse(serverLevel, event.getEntity().position(), ParticleTypes.TOTEM_OF_UNDYING, 35, 0.9);
            serverLevel.playSound(null, event.getEntity().blockPosition(), SoundEvents.TOTEM_USE, event.getEntity().getSoundSource(), 0.8f, 1.2f);
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!event.getEffectInstance().getEffect().is(ModEffects.LOOSING_CONTROL)) {
            return;
        }

        FateProtection protection = FATE_PROTECTIONS.remove(event.getEntity().getUUID());
        if (protection == null) {
            return;
        }

        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            playPulse(serverLevel, event.getEntity().position(), ParticleTypes.TOTEM_OF_UNDYING, 25, 0.8);
        }
    }

    static void shuffleInventory(Player player) {
        List<ItemStack> items = new ArrayList<>(player.getInventory().getContainerSize());
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            items.add(player.getInventory().getItem(i).copy());
        }

        for (int i = items.size() - 1; i > 0; i--) {
            int swapIndex = player.level().random.nextInt(i + 1);
            ItemStack temp = items.get(i);
            items.set(i, items.get(swapIndex));
            items.set(swapIndex, temp);
        }

        for (int i = 0; i < items.size(); i++) {
            player.getInventory().setItem(i, items.get(i));
        }
        player.containerMenu.broadcastChanges();
    }

    static void applyRealmAura(ServerLevel level, Player caster, RealmDomain realm) {
        AABB bounds = new AABB(realm.center, realm.center).inflate(realm.radius + 2.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, bounds);
        double radiusSqr = realm.radius * realm.radius;
        for (LivingEntity entity : entities) {
            if (entity.position().distanceToSqr(realm.center) > radiusSqr) {
                continue;
            }

            boolean allied = entity == caster || AllyUtil.areAllies(caster, entity);
            if (entity != caster) {
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, allied ? 0 : 1, false, false, false));
            }

            if (entity == caster) {
                entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 40, 3, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
            } else if (allied) {
                entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 40, 2, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, false));
            } else if (AbilityUtil.mayDamage(caster, entity)) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));
            }
        }
    }

    private static boolean isRealmEmpowered(LivingEntity entity, Ability ability) {
        if (entity == null || ability == null) {
            return false;
        }

        if (!"fool".equals(BeyonderData.getPathway(entity)) || !ability.getRequirements().containsKey("fool")) {
            return false;
        }

        RealmDomain realm = REALM_DOMAINS.get(entity.getUUID());
        return realm != null && realm.contains(entity);
    }

    static class TemporalSnapshot {
        final Vec3 position;
        final float health;
        final float spirituality;

        TemporalSnapshot(Vec3 position, float health, float spirituality) {
            this.position = position;
            this.health = health;
            this.spirituality = spirituality;
        }
    }

    static class HistoryMask {
        final int actualSequence;
        final int fooledSequence;
        final long expiresAt;
        final UUID casterId;

        HistoryMask(int actualSequence, int fooledSequence, long expiresAt, UUID casterId) {
            this.actualSequence = actualSequence;
            this.fooledSequence = fooledSequence;
            this.expiresAt = expiresAt;
            this.casterId = casterId;
        }
    }

    static class FateProtection {
        final UUID casterId;
        final long expiresAt;

        FateProtection(UUID casterId, long expiresAt) {
            this.casterId = casterId;
            this.expiresAt = expiresAt;
        }
    }

    static class RealmDomain {
        final UUID casterId;
        final Vec3 center;
        final int radius;
        final long expiresAt;
        final ResourceKey<Level> dimension;

        RealmDomain(UUID casterId, Vec3 center, int radius, long expiresAt, ResourceKey<Level> dimension) {
            this.casterId = casterId;
            this.center = center;
            this.radius = radius;
            this.expiresAt = expiresAt;
            this.dimension = dimension;
        }

        boolean contains(LivingEntity entity) {
            if (!dimension.equals(entity.level().dimension())) {
                return false;
            }
            return entity.position().distanceToSqr(center) <= radius * radius;
        }
    }
}
