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
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class TheCorroderAbility extends ToggleAbility {

    private static final int BARRIER_RADIUS = 16;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int BLOCKS_PER_PULSE = 2;      // blocks eaten per corroder per pulse
    private static final int PULSE_INTERVAL_TICKS = 4;  // a pulse every 4 ticks = 10 blocks per second
    private static final int SOUND_COOLDOWN_TICKS = 12; // at most one sound per corroder per 0.6s
    private static final Map<UUID, Long> lastSound = new ConcurrentHashMap<>();
    private static final DustParticleOptions CORRODE_DUST = new DustParticleOptions(new Vector3f(0.15f, 0.45f, 0.1f), 1.2f);
    private static final int CORRODED_TICKS = 20 * 10;
    private static final int HIT_GEAR_DAMAGE = 3;
    private static final int HARD_SLOW_AMPLIFIER = 11; // slowness at or above this is treated as a seal/cage and refused

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
        lastSound.remove(entity.getUUID());
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        active.add(entity.getUUID());
        breakFree(entity);
    }

    // Seals, imprisonment and cages
    private static void breakFree(LivingEntity entity) {
        if (BeyonderData.isBeyonder(entity)) entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).enableAbilityUsage("sealed");
        BeyonderData.removeModifier(entity, "sealed");
        MobEffectInstance slow = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (slow != null && slow.getAmplifier() >= HARD_SLOW_AMPLIFIER) entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }
    // hard slowness cant effect thecorroder
    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!active.contains(event.getEntity().getUUID())) return;
        MobEffectInstance inst = event.getEffectInstance();
        if (inst.getEffect().equals(MobEffects.MOVEMENT_SLOWDOWN) && inst.getAmplifier() >= HARD_SLOW_AMPLIFIER) event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }
    public static List<BlockPos> corrodeBlocks(ServerLevel level, List<BlockPos> blocks) {
        List<BlockPos> eaten = new ArrayList<>();
        if (blocks.isEmpty() || active.isEmpty()) return eaten;
        long now = level.getGameTime();
        if (now % PULSE_INTERVAL_TICKS != 0) return eaten; // every caller shares the same pulse tick
        for (UUID id : active) {
            if (!(level.getEntity(id) instanceof LivingEntity corroder) || !corroder.isAlive()) continue;
            Vec3 pos = corroder.position();
            List<BlockPos> candidates = blocks.stream()
                    .filter(p -> level.getBlockState(p).is(Blocks.BARRIER) && p.getCenter().distanceTo(pos) <= BARRIER_RADIUS)
                    .sorted(Comparator.comparingDouble(p -> p.getCenter().distanceToSqr(pos)))
                    .limit(BLOCKS_PER_PULSE).toList();
            for (BlockPos p : candidates) {
                level.removeBlock(p, false);
                blocks.remove(p);
                eaten.add(p);
                ParticleUtil.spawnParticles(level, CORRODE_DUST, p.getCenter(), 6, 0.4, 0);
            }
            if (candidates.isEmpty()) continue;
            Long last = lastSound.get(id);
            if (last == null || now - last >= SOUND_COOLDOWN_TICKS) {
                lastSound.put(id, now);
                level.playSound(null, corroder.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15f, 1.6f);
            }
        }
        return eaten;
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
        lastSound.remove(event.getEntity().getUUID());
    }

}