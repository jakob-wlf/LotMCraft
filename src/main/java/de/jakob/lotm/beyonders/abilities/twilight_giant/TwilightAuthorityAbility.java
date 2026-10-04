package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.death.DoorToTheUnderworldAbility;
import de.jakob.lotm.beyonders.abilities.death.InternalUnderworldAbility;
import de.jakob.lotm.beyonders.abilities.door.BlinkAbility;
import de.jakob.lotm.beyonders.abilities.door.ExileAbility;
import de.jakob.lotm.beyonders.abilities.door.PlayerTeleportationAbility;
import de.jakob.lotm.beyonders.abilities.door.TeleportationAuthorityAbility;
import de.jakob.lotm.beyonders.abilities.door.TravelersDoorAbility;
import de.jakob.lotm.beyonders.abilities.error.TimeManipulationAbility;
import de.jakob.lotm.beyonders.abilities.visionary.EnvisionPositionAbility;
import de.jakob.lotm.beyonders.abilities.red_priest.FogOfWarAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import de.jakob.lotm.entity.custom.ability_entities.VolcanoEntity;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.ElectromagneticTornadoEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class TwilightAuthorityAbility extends SelectableAbility {

    private static final double AURA_RADIUS = 16.0D;
    private static final double DOMAIN_RADIUS = 40.0D;
    private static final double PULSE_RADIUS = 16.0D;
    private static final int PULSE_LIFE = 80;
    private static final int DOOR_DELAY = 80;
    private static final int GRACE_TICKS = 240;
    private static final int SWORD_TIME = 100;
    private static final float SLOW_STEP = 0.2f;
    private static final float HIT_YEARS = 10f;
    private static final float AURA_YEARS = 3f;
    private static final float DOMAIN_YEARS = 5f;
    private static final float LAND_YEARS = 5f;
    private static final float AURA_UPKEEP = 40f;
    private static final float DOMAIN_UPKEEP = 15f;
    private static final float LAND_UPKEEP = 10f;
    private static final float CAST_COST = 5000f;
    private static final DustParticleOptions AIR_DUST = new DustParticleOptions(new Vector3f(1f, 0.45f, 0.15f), 0.7f);

    private static final Set<UUID> auras = new HashSet<>();
    private static final Map<UUID, Domain> domains = new HashMap<>();
    private static final List<Pulse> pulses = new ArrayList<>();
    private static final Set<UUID> doorReady = new HashSet<>();
    private static final Map<UUID, Long> grace = new HashMap<>();
    private static final Map<UUID, Long> swordStopUntil = new HashMap<>();
    private static final Map<UUID, Vec3> swordAnchor = new HashMap<>();
    private static final Map<UUID, Long> slowUntil = new HashMap<>();
    private static final Map<UUID, Vec3> landAnchors = new HashMap<>();
    private static final Map<UUID, Float> tickPart = new HashMap<>();

    public TwilightAuthorityAbility(String id) {
        super(id, 1);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return CAST_COST;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.twilight_authority.aura",
                "ability.lotmcraft.twilight_authority.domain",
                "ability.lotmcraft.twilight_authority.land",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(entity instanceof ServerPlayer player)) return;
        switch (selectedAbility) {
            case 0 -> toggleAura(player);
            case 1 -> toggleDomain(player);
            default -> toggleLand(player);
        }
    }

    private static void toggleAura(ServerPlayer player) {
        if (auras.remove(player.getUUID())) {
            BeyonderData.incrementSpirituality(player, CAST_COST);
            bar(player, "ability.lotmcraft.twilight_authority.aura_stopped");
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, player.getSoundSource(), 1f, 0.6f);
            return;
        }
        auras.add(player.getUUID());
        bar(player, "ability.lotmcraft.twilight_authority.aura_started");
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, player.getSoundSource(), 1.4f, 0.5f);
    }

    private static void toggleDomain(ServerPlayer player) {
        if (domains.remove(player.getUUID()) != null) {
            bar(player, "ability.lotmcraft.twilight_authority.domain_stopped");
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, player.getSoundSource(), 1.2f, 0.4f);
            return;
        }
        domains.put(player.getUUID(), new Domain(player.level().dimension(), player.position()));
        bar(player, "ability.lotmcraft.twilight_authority.domain_started");
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 1.6f, 0.4f);
    }

    private static void toggleLand(ServerPlayer player) {
        Domain domain = domains.get(player.getUUID());
        if (domain == null) {
            BeyonderData.incrementSpirituality(player, CAST_COST);
            bar(player, "ability.lotmcraft.twilight_authority.land_needs_domain");
            return;
        }
        domain.land = !domain.land;
        if (!domain.land) BeyonderData.incrementSpirituality(player, CAST_COST);
        bar(player, domain.land
                ? "ability.lotmcraft.twilight_authority.land_started"
                : "ability.lotmcraft.twilight_authority.land_stopped");
        player.level().playSound(null, player.blockPosition(), domain.land ? SoundEvents.WARDEN_HEARTBEAT : SoundEvents.BEACON_DEACTIVATE, player.getSoundSource(), 1.4f, 0.5f);
    }

    public static void onSwordLeftClick(ServerPlayer player, LivingEntity target) {
        if (!domains.containsKey(player.getUUID()) || !AbilityUtil.mayDamage(player, target)) return;
        if (AbilityUtil.isTargetSignificantlyStronger(player, target)) return;
        long until = player.level().getGameTime() + SWORD_TIME;
        if (AbilityUtil.isTargetSignificantlyWeaker(player, target)) {
            swordStopUntil.put(target.getUUID(), until);
            swordAnchor.put(target.getUUID(), target.position());
            return;
        }
        slowUntil.put(target.getUUID(), until);
    }

    public static boolean dissipates(ServerLevel level, Vec3 pos, LivingEntity owner) {
        for (UUID id : auras) {
            ServerPlayer aura = online(level, id);
            if (aura == null || aura.level() != level || aura.position().distanceTo(pos) > AURA_RADIUS) continue;
            if (owner != null && AbilityUtil.isTargetSignificantlyStronger(aura, owner)) continue;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        applyPlayerHold(player);
        UUID id = player.getUUID();
        if (!player.isAlive()) {
            auras.remove(id);
            domains.remove(id);
            return;
        }
        ServerLevel level = player.serverLevel();
        if (auras.contains(id)) tickAura(level, player);
        Domain domain = domains.get(id);
        if (domain != null) tickDomain(level, player, domain);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        auras.remove(id);
        domains.remove(id);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof LivingEntity living) || living instanceof Player) return;
        if (stopped(living)) {
            Vec3 anchor = anchorOf(living);
            living.setDeltaMovement(Vec3.ZERO);
            living.teleportTo(anchor.x, anchor.y, anchor.z);
            event.setCanceled(true);
            return;
        }
        landAnchors.remove(living.getUUID());
        if (swordStopUntil.getOrDefault(living.getUUID(), 0L) <= living.level().getGameTime()) swordAnchor.remove(living.getUUID());
        if (!slowed(living)) {
            tickPart.remove(living.getUUID());
            return;
        }
        float acc = tickPart.getOrDefault(living.getUUID(), 0f) + SLOW_STEP;
        if (acc >= 1f) tickPart.put(living.getUUID(), acc - 1f);
        else {
            tickPart.put(living.getUUID(), acc);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living) || spared(living) || !stopped(living)) return;
        if (event.getTarget().distanceToSqr(anchorOf(living)) < 1.0) return;
        spare(living);
    }

    @SubscribeEvent
    public static void onUse(AbilityUseEvent event) {
        LivingEntity caster = event.getEntity();
        if (!(caster.level() instanceof ServerLevel level)) return;
        Ability ability = event.getAbility();
        UUID id = caster.getUUID();
        if (isDoor(ability) && doorReady.remove(id)) return;
        if (isLight(ability) && pressured(caster, false)) {
            event.setCanceled(true);
            if (caster instanceof Player player) bar(player, "ability.lotmcraft.twilight_authority.light_stopped");
            return;
        }
        if (stopped(caster) && isEscape(caster, ability)) {
            spare(caster);
            return;
        }
        if (isDoor(ability) && pressured(caster, true)) {
            event.setCanceled(true);
            if (caster instanceof Player player) bar(player, "ability.lotmcraft.twilight_authority.door_delayed");
            ServerScheduler.scheduleDelayed(DOOR_DELAY, () -> {
                if (!caster.isAlive() || !(caster.level() instanceof ServerLevel current)) return;
                doorReady.add(id);
                ability.useAbility(current, caster);
            }, level);
            return;
        }
        if (ability.getId().contains("twilight")) remember(level, caster);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || event.getSource().getDirectEntity() != attacker) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;
        if (BeyonderData.getSequence(attacker) > 0 || !"twilight_giant".equals(BeyonderData.getPathway(attacker))) return;
        LivingEntity target = event.getEntity();
        if (!AbilityUtil.mayDamage(attacker, target)) return;
        ServerScheduler.scheduleDelayed(1, () -> TwilightAging.age(level, target, attacker, HIT_YEARS), level);
    }

    private static void tickAura(ServerLevel level, ServerPlayer player) {
        if (player.tickCount % 5 == 0) {
            level.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX(), player.getY(1.0), player.getZ(), 8, 0.6, 0.8, 0.6, 0.02);
            paintDecay(level, player);
        }
        if (player.tickCount % 20 != 0) return;
        if (!pay(player, AURA_UPKEEP)) {
            auras.remove(player.getUUID());
            bar(player, "ability.lotmcraft.twilight_authority.aura_stopped");
            return;
        }
        for (LivingEntity target : AbilityUtil.getNearbyEntities(player, level, player.position(), AURA_RADIUS)) {
            if (AbilityUtil.mayDamage(player, target)) TwilightAging.age(level, target, player, AURA_YEARS);
        }
        fadeAoe(level, player);
    }

    private static void tickDomain(ServerLevel level, ServerPlayer player, Domain domain) {
        if (player.tickCount % 10 == 0 && player.level().dimension().equals(domain.dimension)) drawRing(level, domain.center, DOMAIN_RADIUS);
        if (player.tickCount % 20 != 0) return;
        if (!pay(player, DOMAIN_UPKEEP)) {
            domains.remove(player.getUUID());
            bar(player, "ability.lotmcraft.twilight_authority.domain_stopped");
            return;
        }
        if (domain.land && !pay(player, LAND_UPKEEP)) {
            domain.land = false;
            bar(player, "ability.lotmcraft.twilight_authority.land_stopped");
        }
        if (!player.level().dimension().equals(domain.dimension)) return;
        float years = domain.land ? LAND_YEARS : DOMAIN_YEARS;
        for (LivingEntity target : AbilityUtil.getNearbyEntities(player, level, domain.center, DOMAIN_RADIUS)) {
            if (AbilityUtil.mayDamage(player, target)) TwilightAging.age(level, target, player, years);
        }
    }

    private static void paintDecay(ServerLevel level, ServerPlayer player) {
        for (int i = 0; i < 8; i++) {
            double dx = (player.getRandom().nextDouble() - 0.5) * AURA_RADIUS * 2;
            double dy = player.getRandom().nextDouble() * 5;
            double dz = (player.getRandom().nextDouble() - 0.5) * AURA_RADIUS * 2;
            BlockPos pos = BlockPos.containing(player.getX() + dx, player.getY() + dy, player.getZ() + dz);
            if (!level.getBlockState(pos).isAir()) continue;
            level.sendParticles(AIR_DUST, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.15, 0.15, 0.15, 0.01);
        }
    }

    private static void drawRing(ServerLevel level, Vec3 center, double radius) {
        int points = 64;
        for (int i = 0; i < points; i++) {
            double angle = i * Math.PI * 2 / points;
            Vec3 point = center.add(Math.cos(angle) * radius, 1.2, Math.sin(angle) * radius);
            level.sendParticles(TwilightAging.TWILIGHT_DUST, point.x, point.y, point.z, 1, 0.1, 0.4, 0.1, 0);
        }
    }

    private static void fadeAoe(ServerLevel level, ServerPlayer player) {
        AABB box = new AABB(player.position(), player.position()).inflate(AURA_RADIUS);
        Ability fogAbility = LOTMCraft.abilityHandler.getById("fog_of_war_ability");
        if (fogAbility instanceof FogOfWarAbility fog) {
            for (ServerPlayer other : level.players()) {
                if (!fog.isActiveForEntity(other) || !dissipates(level, other.position(), other)) continue;
                fog.onAbilityUse(level, other);
            }
        }
        for (VolcanoEntity volcano : level.getEntitiesOfClass(VolcanoEntity.class, box)) {
            if (dissipates(level, volcano.position(), volcano.getOwner(level))) volcano.discard();
        }
        for (TornadoEntity tornado : level.getEntitiesOfClass(TornadoEntity.class, box)) {
            if (dissipates(level, tornado.position(), living(level, tornado.getCasterUUID()))) tornado.discard();
        }
        for (ElectromagneticTornadoEntity tornado : level.getEntitiesOfClass(ElectromagneticTornadoEntity.class, box)) {
            if (dissipates(level, tornado.position(), living(level, tornado.getCasterUUID()))) tornado.discard();
        }
    }

    private static void applyPlayerHold(ServerPlayer player) {
        if (!stopped(player)) {
            landAnchors.remove(player.getUUID());
            if (swordStopUntil.getOrDefault(player.getUUID(), 0L) <= player.level().getGameTime()) swordAnchor.remove(player.getUUID());
            if (slowed(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 4, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10, 4, false, false, false));
            }
            return;
        }
        Vec3 anchor = anchorOf(player);
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(anchor.x, anchor.y, anchor.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.resetFallDistance();
    }

    private static boolean stopped(LivingEntity entity) {
        if (spared(entity)) return false;
        if (swordStopUntil.getOrDefault(entity.getUUID(), 0L) > entity.level().getGameTime()) return true;
        return fieldAt(entity, true) != null;
    }

    private static boolean slowed(LivingEntity entity) {
        if (stopped(entity) || spared(entity)) return false;
        if (slowUntil.getOrDefault(entity.getUUID(), 0L) > entity.level().getGameTime()) return true;
        return fieldAt(entity, false) != null;
    }

    private static Domain fieldAt(LivingEntity entity, boolean land) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        for (Map.Entry<UUID, Domain> entry : domains.entrySet()) {
            Domain domain = entry.getValue();
            if (domain.land != land) continue;
            if (!entity.level().dimension().equals(domain.dimension)) continue;
            if (entity.position().distanceTo(domain.center) > DOMAIN_RADIUS) continue;
            ServerPlayer owner = online(level, entry.getKey());
            if (owner == null || !AbilityUtil.mayDamage(owner, entity)) continue;
            return domain;
        }
        return null;
    }

    private static Vec3 anchorOf(LivingEntity entity) {
        long now = entity.level().getGameTime();
        if (swordStopUntil.getOrDefault(entity.getUUID(), 0L) > now) {
            return swordAnchor.computeIfAbsent(entity.getUUID(), id -> entity.position());
        }
        return landAnchors.computeIfAbsent(entity.getUUID(), id -> entity.position());
    }

    private static boolean spared(LivingEntity entity) {
        Long until = grace.get(entity.getUUID());
        if (until == null) return false;
        if (entity.level().getGameTime() >= until) {
            grace.remove(entity.getUUID());
            return false;
        }
        return true;
    }

    private static void spare(LivingEntity entity) {
        grace.put(entity.getUUID(), entity.level().getGameTime() + GRACE_TICKS);
        landAnchors.remove(entity.getUUID());
        swordAnchor.remove(entity.getUUID());
        swordStopUntil.remove(entity.getUUID());
    }

    private static boolean pressured(LivingEntity caster, boolean significant) {
        if (!(caster.level() instanceof ServerLevel level)) return false;
        long now = level.getGameTime();
        for (UUID id : auras) {
            ServerPlayer owner = online(level, id);
            if (owner == null || owner.level() != caster.level() || owner.distanceTo(caster) > AURA_RADIUS) continue;
            if (matches(owner, caster, significant)) return true;
        }
        for (Map.Entry<UUID, Domain> entry : domains.entrySet()) {
            Domain domain = entry.getValue();
            if (!caster.level().dimension().equals(domain.dimension) || caster.position().distanceTo(domain.center) > DOMAIN_RADIUS) continue;
            ServerPlayer owner = online(level, entry.getKey());
            if (owner != null && matches(owner, caster, significant)) return true;
        }
        Iterator<Pulse> iterator = pulses.iterator();
        while (iterator.hasNext()) {
            Pulse pulse = iterator.next();
            if (pulse.until < now) {
                iterator.remove();
                continue;
            }
            if (!caster.level().dimension().equals(pulse.dimension) || caster.position().distanceTo(pulse.pos) > PULSE_RADIUS) continue;
            ServerPlayer owner = online(level, pulse.owner);
            if (owner != null && matches(owner, caster, significant)) return true;
        }
        return false;
    }

    private static boolean matches(LivingEntity owner, LivingEntity caster, boolean significant) {
        if (significant) return !AbilityUtil.isTargetSignificantlyStronger(owner, caster);
        return BeyonderData.getSequence(owner) <= BeyonderData.getSequence(caster);
    }

    private static void remember(ServerLevel level, LivingEntity caster) {
        long now = level.getGameTime();
        pulses.removeIf(pulse -> pulse.until < now);
        pulses.add(new Pulse(level.dimension(), caster.position(), caster.getUUID(), now + PULSE_LIFE));
    }

    private static boolean isLight(Ability ability) {
        if (ability.getId().contains("holiness")) return true;
        for (String flag : ability.getInteractionFlags()) {
            if (flag.equals("purification") || flag.equals("purification_holy") || flag.equals("light_source") || flag.equals("light_strong") || flag.equals("light_weak")) return true;
        }
        Integer sun = ability.getRequirements().get("sun");
        return sun != null && sun <= 2;
    }

    private static boolean isDoor(Ability ability) {
        return ability instanceof ExileAbility
                || ability instanceof PlayerTeleportationAbility
                || ability instanceof TravelersDoorAbility
                || ability instanceof TeleportationAuthorityAbility
                || ability instanceof DoorToTheUnderworldAbility
                || ability instanceof InternalUnderworldAbility;
    }

    private static boolean isEscape(LivingEntity caster, Ability ability) {
        if (ability instanceof ExileAbility || ability instanceof BlinkAbility || ability instanceof PlayerTeleportationAbility || ability instanceof TravelersDoorAbility || ability instanceof TeleportationAuthorityAbility || ability instanceof EnvisionPositionAbility) return true;
        if (ability instanceof TimeManipulationAbility time && time.getSelectedAbilityIndex(caster.getUUID()) == 1) return true;
        for (String flag : ability.getInteractionFlags()) {
            if (flag.equals("blink_escape") || flag.equals("escape")) return true;
        }
        return false;
    }

    private static boolean pay(ServerPlayer player, float cost) {
        if (player.hasInfiniteMaterials()) return true;
        if (BeyonderData.getSpirituality(player) < cost) return false;
        BeyonderData.reduceSpirituality(player, cost);
        return true;
    }

    private static ServerPlayer online(ServerLevel level, UUID id) {
        Player player = level.getServer().getPlayerList().getPlayer(id);
        return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    private static LivingEntity living(ServerLevel level, UUID id) {
        if (id == null) return null;
        Entity entity = level.getEntity(id);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private static void bar(Player player, String key) {
        AbilityUtil.sendActionBar(player, Component.translatable(key).withColor(TwilightAging.TWILIGHT_TEXT));
    }

    private static final class Domain {
        private final ResourceKey<Level> dimension;
        private Vec3 center;
        private boolean land;

        private Domain(ResourceKey<Level> dimension, Vec3 center) {
            this.dimension = dimension;
            this.center = center;
        }
    }

    private static final class Pulse {
        private final ResourceKey<Level> dimension;
        private final Vec3 pos;
        private final UUID owner;
        private final long until;

        private Pulse(ResourceKey<Level> dimension, Vec3 pos, UUID owner, long until) {
            this.dimension = dimension;
            this.pos = pos;
            this.owner = owner;
            this.until = until;
        }
    }
}
