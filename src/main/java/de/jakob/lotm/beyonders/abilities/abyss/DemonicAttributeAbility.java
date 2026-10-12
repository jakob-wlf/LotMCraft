package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Corroder.BloodlineMindRotterAbility.getPoisonDamageMultiplier;


public class DemonicAttributeAbility extends SelectableAbility {

    // index 0 is always unlocked (guaranteed). Indices 1-4 are the random pool:
    // 1 = Toxic Smoke, 2 = Poisonous Flame, 3 = Curse, 4 = Sink Into Ground
    private static final int[][] POOL_PAIRS = {
            {1, 2}, {1, 3}, {1, 4}, {2, 3}, {2, 4}, {3, 4}
    };
    private final Map<UUID, int[]> unlockedAbilities = new HashMap<>();
    private static final DustParticleOptions SMOKE_DUST = new DustParticleOptions(new Vector3f(35 / 255f, 168 / 255f, 102 / 255f), 3f);
    private static final DustParticleOptions FLAME_DUST = new DustParticleOptions(new Vector3f(35 / 255f, 168 / 255f, 102 / 255f), 1f);
    public DemonicAttributeAbility(String id) {
        super(id, 7);
        postsUsedAbilityEventManually = true;
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 17;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.demonic_attribute.slow",
                "ability.lotmcraft.demonic_attribute.toxic_smoke",
                "ability.lotmcraft.demonic_attribute.poisonous_flame",
                "ability.lotmcraft.demonic_attribute.curse",
                "ability.lotmcraft.demonic_attribute.sink_into_ground"};
    }
    private int[] getUnlockedPair(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        long combinedSeed = uuid.getMostSignificantBits() ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 32);
        Random random = new Random(combinedSeed);
        return POOL_PAIRS[random.nextInt(POOL_PAIRS.length)];
    }
    public boolean isSubAbilityUnlocked(LivingEntity entity, int index) {
        if (index == 0) return true;
        if (index < 0 || index >= getAbilityNames().length) return false;

        int[] pair = getUnlockedPair(entity);
        return index == pair[0] || index == pair[1];
    }
    @Override
    public void nextAbility(LivingEntity entity) {
        cycleToUnlocked(entity, 1);
    }
    @Override
    public void previousAbility(LivingEntity entity) {
        cycleToUnlocked(entity, -1);
    }
    private void cycleToUnlocked(LivingEntity entity, int step) {
        int length = getAbilityNames().length;
        if (length == 0) return;
        int selected = selectedAbilities.getOrDefault(entity.getUUID(), 0);
        for (int i = 0; i < length; i++) {
            selected = Math.floorMod(selected + step, length);
            if (isSubAbilityUnlocked(entity, selected)) break;
        }
        selectedAbilities.put(entity.getUUID(), selected);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selected));
    }
    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {return;}
        if (level.isClientSide) return;
        if (!isSubAbilityUnlocked(entity, abilityIndex)) return;
        switch (abilityIndex) {
            case 0 -> Slow(level, entity,serverLevel);
            case 1 -> toxicSmoke(level, entity);
            case 2 -> poisonousFlame(level, entity);
            case 3 -> curse(level, entity);
            case 4 -> sinkIntoGround(level, entity);
        }
    }
    // 0: slow
    private void Slow(Level ignoredLevel, LivingEntity entity, ServerLevel serverLevel) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2.5f);
        int duration = 4 * 20;
        if (target == null) return;
        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            target.setDeltaMovement(0, 0, 0);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false, false));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
    //1: Toxic Smoke
    private void toxicSmoke(Level level, LivingEntity entity) {
        Vec3 pos = entity.getEyePosition();
        final UUID[] taskIdHolder = new UUID[1];
        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 6, (int) (20 * 5 * multiplier(entity)), () -> {
            Location smokeLoc = new Location(pos, level);
            int seq = AbilityUtil.getSeqWithArt(entity, this);
            if (InteractionHandler.isInteractionPossible(smokeLoc, "burning", seq)) {
                AbilityUtil.damageNearbyEntities((ServerLevel) level, null, 9, DamageLookup.lookupDamage(8, 1.2) * multiplier(entity), pos, true, false, true, 0, ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity));
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.EXPLOSION_EMITTER, pos, 50, 7, .02);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.LARGE_SMOKE, pos, 175, 7, .02);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, pos, 250, 7, .02);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1, 1);
                if (taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }
            if (InteractionHandler.isInteractionPossible(smokeLoc, "purification", seq)) {
                if (taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 6.5 * multiplier(entity), DamageLookup.lookupDps(8, .8, 6, 20) * multiplier(entity)*getPoisonDamageMultiplier(entity), pos, true, false, true, 0, ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity));
            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.TOXIC_SMOKE.get(), pos, 25, 3, .01);
            ParticleUtil.spawnParticles((ServerLevel) level, SMOKE_DUST, pos, 25, 3, .01);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, pos, 25, 3, .01);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 5, pos, new MobEffectInstance(MobEffects.POISON, 30, 1), new MobEffectInstance(MobEffects.BLINDNESS, 10, 0));}, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(pos, level)));

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, pos, entity, this, new String[]{"poison"}, 1.4, 10));
    }

    //2: Poisonous Flame

    private void poisonousFlame(Level level, LivingEntity entity) {
        Vec3 startPos = entity.getEyePosition().subtract(0, .2, 0).add(entity.getLookAngle().normalize());
        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 2.0f, .5f);
        if (InteractionHandler.isInteractionPossible(new Location(startPos, level), "water", AbilityUtil.getSeqWithArt(entity, this))) {
            level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.FIRE_EXTINGUISH, entity.getSoundSource(), 2.0f, .5f);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.LARGE_SMOKE, startPos, 10, .2, .1);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FALLING_WATER, startPos, 50, .2, .1);
            return;
        }
        ParticleUtil.drawParticleLine((ServerLevel) level, ParticleTypes.SMOKE, startPos, entity.getLookAngle().normalize(), 2, .5, 23, .4);
        ParticleUtil.drawParticleLine((ServerLevel) level, ModParticles.GREEN_FLAME.get(), startPos, entity.getLookAngle().normalize(), 2, .5, 35, .4);
        ParticleUtil.drawParticleLine((ServerLevel) level, FLAME_DUST, startPos, entity.getLookAngle().normalize(), 2, .5, 23, .4);
        Location flameLoc = new Location(startPos, level);
        int seq = AbilityUtil.getSeqWithArt(entity, this);
        boolean purified = InteractionHandler.isInteractionPossible(flameLoc, "purification", seq);
        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.75 * multiplier(entity), DamageLookup.lookupDamage(8, .9) * multiplier(entity)*getPoisonDamageMultiplier(entity), startPos, true, false, true, 0, 20 * 3, ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity));
        if (!purified) {
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 3, startPos, new MobEffectInstance(MobEffects.POISON, 20 * 8 * (int) Math.max(multiplier(entity) / 2, 1), 2, false, true));
        }
        BlockState block = level.getBlockState(BlockPos.containing(startPos));
        if (block.isAir()) {
            level.setBlockAndUpdate(BlockPos.containing(startPos), Blocks.LIGHT.defaultBlockState());
        }
        ServerScheduler.scheduleDelayed(25, () -> level.setBlockAndUpdate(BlockPos.containing(startPos), Blocks.AIR.defaultBlockState()));
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, startPos, entity, this, new String[]{"burning", "poison"}, 2.4f, 10));
    }

    // ── 3: Curse
    private void curse(Level ignoredLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2.5f);
        if (target == null) return;
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 1, false, false,false));
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 2, 1, false, false,false));
        target.hurt(target.damageSources().magic(), 4f);
    }
    //4: Sink Into Ground
    private void sinkIntoGround(Level level, LivingEntity entity) {
        ServerLevel serverLevel = (ServerLevel) level;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        boolean hasSingleTarget = target != null;
        Set<LivingEntity> targets = hasSingleTarget ? Set.of(target) : new HashSet<>(AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20, false, true));
        int drainDuration = 20 * 10;
        double dps = DamageLookup.lookupDps(3, hasSingleTarget ? .85 : .6, 10, 30) * multiplier(entity);
        serverLevel.playSound(null, BlockPos.containing(entity.position()), SoundEvents.WITHER_SPAWN, entity.getSoundSource(), 1f, 1f);
        for (LivingEntity e : targets) BeyonderData.addModifierWithTimeLimit(e, "sink_into_ground", .8, drainDuration * 500);
        AtomicInteger tickCounter = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, drainDuration, () -> {
            for (LivingEntity e : targets) {
                if (tickCounter.get() % 10 == 0) {
                    e.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), (float) dps);
                    serverLevel.playSound(null, BlockPos.containing(e.position()), SoundEvents.WITHER_SHOOT, e.getSoundSource(), .5f, .5f);
                    if (tickCounter.get() % 20 == 0) {
                        // Original pulled -.001 per 20 ticks (10 pulls, ~negligible). This pulls -.1 per 20 ticks
                        // (10 pulls over the full duration) so the total comes to exactly 1 block, per spec.
                        e.teleportRelative(0, -.1, 0);
                    }
                }
                ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.SOUL, e.getEyePosition(), 2, 25);
                ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.SOUL, e.getEyePosition().subtract(0, .75, 0), 2, 25);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.LARGE_SMOKE, e.position().add(0, e.getEyeHeight() / 2, 0), 40, .3, .8, .3, 0);
            }
            tickCounter.incrementAndGet();
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
}