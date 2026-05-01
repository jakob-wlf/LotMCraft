package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class OrderProxyAbility extends SelectableAbility {

    public static final List<PermanentProhibitionZone> PERMANENT_PROHIBITION_ZONES = new CopyOnWriteArrayList<>();
    public static final List<PermanentLawZone> PERMANENT_LAW_ZONES = new CopyOnWriteArrayList<>();

    public static final Set<UUID> IMMUNE_ENTITIES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final Set<UUID> NO_REVIVAL_ENTITIES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static final double PROHIBITION_RADIUS = 100.0;
    public static final double LAW_RADIUS = 60.0;

    public OrderProxyAbility(String id) {
        super(id, 20f, "order_proxy");
        interactionRadius = 50;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
        canBeShared = false;
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.order_proxy.prohibition_resurrecting",
                "ability.lotmcraft.order_proxy.prohibition_dying",
                "ability.lotmcraft.order_proxy.prohibition_demigods",
                "ability.lotmcraft.order_proxy.law_combat",
                "ability.lotmcraft.order_proxy.law_losing_control",
                "ability.lotmcraft.order_proxy.order_sacrifice",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        switch (abilityIndex) {
            case 0 -> orderProhibition(serverLevel, entity, OrderProhibitionType.RESURRECTING);
            case 1 -> orderProhibition(serverLevel, entity, OrderProhibitionType.DYING);
            case 2 -> orderProhibition(serverLevel, entity, OrderProhibitionType.DEMIGODS);
            case 3 -> orderLaw(serverLevel, entity, LawType.COMBAT);
            case 4 -> orderLaw(serverLevel, entity, LawType.LOSING_CONTROL);
            case 5 -> orderSacrifice(serverLevel, entity);
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, 20 * 2));
    }

    private void orderSacrifice(ServerLevel serverLevel, LivingEntity entity) {
        UUID uuid = entity.getUUID();
        Location casterLocation = new Location(entity.position(), serverLevel);

        IMMUNE_ENTITIES.add(uuid);
        BeyonderData.addModifierWithTimeLimit(entity, "order_sacrifice_boost", 3.0, 15000L);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 0.45f);

        RingEffectManager.createRingForAll(entity.position(), 5f, 70,
                1.0f, 0.75f, 0.2f, 1.0f, 3f, 7f, serverLevel);
        RingEffectManager.createRingForAll(entity.position(), 8f, 60,
                1.0f, 0.92f, 0.6f, 0.6f, 2f, 5f, serverLevel);

        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, entity.position().add(0, 1, 0), 3.5, 60);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, entity.position().add(0, 1, 0), 4.0, 50);

        ParticleUtil.createParticleSpirals(
                serverLevel, ParticleTypes.END_ROD, entity.position().add(0, 0.1, 0),
                1.5, 1.5, 3.0, 1.2, 1.5, 300, 3, 40
        );

        ServerScheduler.scheduleForDuration(0, 10, 300, () -> {
            Vec3 pos = entity.position().add(0, 1.0, 0);
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.END_ROD, pos, 1.8, 12);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.DUST_PLUME, pos.add(0, 0.5, 0), 3, 0.4, 0.0);
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, casterLocation));

        broadcastToNearby(serverLevel, entity,
                Component.translatable("ability.lotmcraft.order_proxy.sacrifice_level_prefix")
                        .withStyle(ChatFormatting.DARK_RED)
                        .append(Component.literal(entity.getDisplayName().getString()).withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable("ability.lotmcraft.order_proxy.sacrifice_level_suffix").withStyle(ChatFormatting.DARK_RED)));

        if (entity instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.order_proxy.sacrifice_notification")
                    .withStyle(ChatFormatting.GOLD));
        }

        ServerScheduler.scheduleDelayed(300, () -> {
            IMMUNE_ENTITIES.remove(uuid);
            LivingEntity target = (LivingEntity) serverLevel.getEntity(uuid);
            if (target == null || !target.isAlive()) return;

            serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5f, 0.4f);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, target.position().add(0, 1, 0), 4.0, 80);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, target.position().add(0, 1, 0), 5.0, 60);
            RingEffectManager.createRingForAll(target.position(), 6f, 70,
                    1.0f, 0.75f, 0.2f, 1.0f, 3f, 8f, serverLevel);

            target.kill();
        });
    }

    private void orderProhibition(ServerLevel serverLevel, LivingEntity entity, OrderProhibitionType type) {
        Vec3 pos = entity.position();
        Location casterLocation = new Location(pos, serverLevel);

        PermanentProhibitionZone existing = PERMANENT_PROHIBITION_ZONES.stream()
                .filter(z -> z.type() == type && z.isInZone(pos, serverLevel))
                .findFirst().orElse(null);

        if (existing != null) {
            PERMANENT_PROHIBITION_ZONES.remove(existing);

            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.7f);
            RingEffectManager.createRingForAll(pos, (float) PROHIBITION_RADIUS, 60,
                    1.0f, 0.92f, 0.6f, 0.5f, 2f, 5f, serverLevel);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos.add(0, 1, 0), 3.0, 30);

            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.order_proxy.prohibition_lifted", type.displayName)
                        .withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        int MAX_PROHIBITION_ZONES = 3 * (int) Math.max(multiplier(entity) / 4, 1);
        if (PERMANENT_PROHIBITION_ZONES.size() >= MAX_PROHIBITION_ZONES) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.order_proxy.prohibition_limit")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        PermanentProhibitionZone zone = new PermanentProhibitionZone(entity.getUUID(), type, pos, serverLevel);
        PERMANENT_PROHIBITION_ZONES.add(zone);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 0.5f);

        RingEffectManager.createRingForAll(pos, (float) PROHIBITION_RADIUS, 80,
                1.0f, 0.92f, 0.6f, 1.0f, 3f, 8f, serverLevel);
        RingEffectManager.createRingForAll(pos, (float) (PROHIBITION_RADIUS * 0.5), 60,
                1.0f, 0.85f, 0.45f, 0.7f, 2f, 6f, serverLevel);

        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, pos.add(0, 1, 0), 4.0, 60);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos.add(0, 1, 0), 5.0, 50);

        ServerScheduler.scheduleForDuration(0, 4, 80, () -> {
            double angle = (serverLevel.getGameTime() % 360) * Math.PI / 180.0;
            for (int i = 0; i < 8; i++) {
                double a = angle + (2 * Math.PI * i) / 8;
                double x = pos.x + PROHIBITION_RADIUS * Math.cos(a);
                double z = pos.z + PROHIBITION_RADIUS * Math.sin(a);
                Vec3 edgePos = new Vec3(x, pos.y + 0.1, z);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, edgePos, 2, 0.5, 0.0);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.DUST_PLUME, edgePos, 3, 0.7, 0.0);
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, casterLocation));

        EffectManager.playEffect(EffectManager.Effect.PROHIBITION, pos.x, pos.y, pos.z, serverLevel);

        broadcastToNearby(serverLevel, entity,
                Component.translatable("ability.lotmcraft.order_proxy.prohibition_permanent", type.displayName)
                        .withStyle(ChatFormatting.GOLD));
    }

    private void orderLaw(ServerLevel serverLevel, LivingEntity entity, LawType type) {
        Vec3 pos = entity.position();
        Location casterLocation = new Location(pos, serverLevel);

        PermanentLawZone existing = PERMANENT_LAW_ZONES.stream()
                .filter(z -> z.type() == type && z.isInZone(pos, serverLevel))
                .findFirst().orElse(null);

        if (existing != null) {
            PERMANENT_LAW_ZONES.remove(existing);

            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.75f);
            RingEffectManager.createRingForAll(pos, (float) LAW_RADIUS, 55,
                    1.0f, 0.92f, 0.6f, 0.5f, 2f, 5f, serverLevel);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos.add(0, 1, 0), 2.5, 25);

            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.order_proxy.law_lifted", type.displayName)
                        .withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        int MAX_LAW_ZONES = 2 * (int) Math.max(multiplier(entity) / 4, 1);
        if (PERMANENT_LAW_ZONES.size() >= MAX_LAW_ZONES) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.order_proxy.law_limit")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        PermanentLawZone zone = new PermanentLawZone(entity.getUUID(), type, pos, serverLevel);
        PERMANENT_LAW_ZONES.add(zone);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2f, 0.6f);

        RingEffectManager.createRingForAll(pos, (float) LAW_RADIUS, 65,
                1.0f, 0.92f, 0.6f, 1.0f, 2f, 6f, serverLevel);
        RingEffectManager.createRingForAll(pos, (float) (LAW_RADIUS * 0.5), 50,
                1.0f, 0.85f, 0.45f, 0.7f, 1.5f, 5f, serverLevel);

        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, pos.add(0, 1, 0), 3.0, 45);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos.add(0, 1, 0), 3.5, 35);

        ServerScheduler.scheduleForDuration(0, 4, 60, () -> {
            double angle = (serverLevel.getGameTime() % 360) * Math.PI / 180.0;
            for (int i = 0; i < 6; i++) {
                double a = angle + (2 * Math.PI * i) / 6;
                double x = pos.x + LAW_RADIUS * Math.cos(a);
                double z = pos.z + LAW_RADIUS * Math.sin(a);
                Vec3 edgePos = new Vec3(x, pos.y + 0.1, z);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, edgePos, 2, 0.4, 0.0);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.DUST_PLUME, edgePos, 2, 0.6, 0.0);
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, casterLocation));

        broadcastToNearby(serverLevel, entity,
                Component.translatable("ability.lotmcraft.order_proxy.law_active", type.displayName)
                        .withStyle(ChatFormatting.GOLD));
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (IMMUNE_ENTITIES.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!IMMUNE_ENTITIES.contains(event.getEntity().getUUID())) return;
        if (event.getEffectInstance().getEffect().is(ModEffects.LOOSING_CONTROL)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    private static void broadcastToNearby(ServerLevel level, LivingEntity source, Component msg) {
        level.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(level) && p.distanceTo(source) <= PROHIBITION_RADIUS) {
                p.sendSystemMessage(msg);
            }
        });
    }

    public record PermanentProhibitionZone(UUID ownerId, OrderProhibitionType type, Vec3 center, ServerLevel level) {
        public boolean isInZone(Vec3 pos, ServerLevel lvl) {
            return lvl.equals(level) && pos.distanceTo(center) <= PROHIBITION_RADIUS;
        }
    }

    public record PermanentLawZone(UUID ownerId, LawType type, Vec3 center, ServerLevel level) {
        public boolean isInZone(Vec3 pos, ServerLevel lvl) {
            return lvl.equals(level) && pos.distanceTo(center) <= LAW_RADIUS;
        }
    }

    public enum OrderProhibitionType {
        RESURRECTING("Resurrecting"),
        DYING("Dying"),
        DEMIGODS("Demigods");

        public final String displayName;

        OrderProhibitionType(String name) {
            this.displayName = name;
        }
    }

    public enum LawType {
        COMBAT("Combat"),
        LOSING_CONTROL("Losing Control");

        public final String displayName;

        LawType(String name) {
            this.displayName = name;
        }
    }
}