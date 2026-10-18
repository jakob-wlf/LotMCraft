package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.door.SpatialCageAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.ConfinementAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.ImprisonAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.CorrodedEffect;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class TheCorroderAbility extends ToggleAbility {

    private static final int BARRIER_RADIUS = 8;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int BARRIER_COOLDOWN_TICKS = 20 * 8; // time between barriers being eaten through
    private static final int CORRODED_TICKS = 20 * 10;
    private static final int HIT_GEAR_DAMAGE = 3;
    private static final int HARD_SLOW_AMPLIFIER = 3; // slowness at or above this is treated as a seal/cage and refused

    private static final Set<UUID> active = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> barrierCooldown = new ConcurrentHashMap<>();

    public TheCorroderAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        cannotBeStolen = true;
        canBeShared = false;
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 1));}
    @Override
    protected float getSpiritualityCost() {return 100;}

    public static boolean isCorroding(UUID uuid) {return active.contains(uuid);}

    // Toggle
    @Override
    public void start(Level level, LivingEntity entity) {if (!level.isClientSide) active.add(entity.getUUID());}
    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        active.remove(entity.getUUID());
        barrierCooldown.remove(entity.getUUID());
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        active.add(entity.getUUID());
        breakFree(entity);
        if (entity.tickCount % SCAN_INTERVAL_TICKS != 0) return;
        destroyBarriers(serverLevel, entity);
    }

    // Seals, imprisonment and cages
    /** Runs every tick, so anything that lands on the corroder is shaken off immediately. Only the corroder is freed. */
    private static void breakFree(LivingEntity entity) {
        if (ImprisonAbility.IMPRISONED.contains(entity.getUUID())) ImprisonAbility.releaseTarget(entity.getUUID());
        if (BeyonderData.isBeyonder(entity)) entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).enableAbilityUsage("sealed");
        BeyonderData.removeModifier(entity, "sealed");
        MobEffectInstance slow = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (slow != null && slow.getAmplifier() >= HARD_SLOW_AMPLIFIER) entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }
    /** One barrier per pulse, then a cooldown, so it reads as corroding through them. */
    private static void destroyBarriers(ServerLevel level, LivingEntity entity) {
        long now = level.getGameTime();
        Long next = barrierCooldown.get(entity.getUUID());
        if (next != null && now < next) return;
        Vec3 pos = entity.position();
        boolean destroyed = false;
        for (ConfinementAbility.ConfinementZone zone : ConfinementAbility.ACTIVE_ZONES) {
            if (!zone.level.equals(level) || !zone.isActive()) continue;
            if (zone.center.distanceTo(pos) <= zone.radius + BARRIER_RADIUS) {
                ConfinementAbility.removeZone(zone, level);
                destroyed = true;
                break;
            }
        }
        if (!destroyed) destroyed = SpatialCageAbility.corrodeCages(level, pos, BARRIER_RADIUS);
        if (destroyed) barrierCooldown.put(entity.getUUID(), now + BARRIER_COOLDOWN_TICKS);
    }
    /** Hard slowness (Sealing, Imprison, Spatial Cage) can't be applied to a corroder. */
    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!active.contains(event.getEntity().getUUID())) return;
        MobEffectInstance inst = event.getEffectInstance();
        if (inst.getEffect().equals(MobEffects.MOVEMENT_SLOWDOWN) && inst.getAmplifier() >= HARD_SLOW_AMPLIFIER) event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    // Corrosion on hit
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0) return;
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        boolean corrosionDamage = event.getSource().is(ModDamageTypes.CORROSION);
        boolean corroderHit = event.getSource().getEntity() instanceof LivingEntity attacker && active.contains(attacker.getUUID());
        if (!corrosionDamage && !corroderHit) return;
        victim.addEffect(new MobEffectInstance(ModEffects.CORRODED, CORRODED_TICKS, 0));
        if (corroderHit && victim instanceof Player) CorrodedEffect.corrodeGear(victim, HIT_GEAR_DAMAGE);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        active.remove(event.getEntity().getUUID());
        barrierCooldown.remove(event.getEntity().getUUID());
    }
}