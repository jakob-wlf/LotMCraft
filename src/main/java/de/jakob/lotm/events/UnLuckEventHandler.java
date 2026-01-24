package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class UnLuckEventHandler {

    private static final HashMap<UUID, Long> lastSpawnTime = new HashMap<>();
    private static final HashMap<UUID, Long> lastSlipTime = new HashMap<>();
    private static final HashMap<UUID, Long> lastMultiplierReductionTime = new HashMap<>();
    private static final HashMap<UUID, Long> lastAbilityDisableTime = new HashMap<>();
    private static final HashMap<UUID, Long> lastTripTime = new HashMap<>();

    // Brown/orange particles for unluck
    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(161 / 255f, 114 / 255f, 58 / 255f),
            1.5f
    );

    // Randomly destroy items when mining blocks
    @SubscribeEvent
    public static void onBreakBlocks(BlockDropsEvent event) {
        if(!(event.getBreaker() instanceof LivingEntity entity)) {
            return;
        }

        if(!entity.hasEffect(ModEffects.UNLUCK) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = entity.getEffect(ModEffects.UNLUCK).getAmplifier();
        double destroyChance = getItemDestroyChance(amplifier);

        if (Math.random() < destroyChance) {
            // Destroy all drops
            event.getDrops().clear();

            ParticleUtil.spawnParticles(level, dust, event.getPos().getCenter(), 20, .6, .6, .6, 0);
            level.playSound(null, event.getPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 0.8f);
        }

        // Chance to damage tool extra
        if(Math.random() < getToolDamageChance(amplifier)) {
            ItemStack heldItem = entity.getMainHandItem();
            if(!heldItem.isEmpty() && heldItem.isDamageableItem()) {
                heldItem.hurtAndBreak(5 * (amplifier + 1), entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
            }
        }
    }

    // Take extra damage instead of dodging
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if(!event.getEntity().hasEffect(ModEffects.UNLUCK) || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = event.getEntity().getEffect(ModEffects.UNLUCK).getAmplifier();
        double extraDamageChance = getExtraDamageChance(amplifier);

        if (Math.random() < extraDamageChance) {
            float originalDamage = event.getAmount();
            float increasedDamage = originalDamage * (1.5f + 0.25f * amplifier);
            event.setAmount(increasedDamage);

            Entity entity = event.getEntity();
            ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 55, .4, entity.getEyeHeight() / 2, .4, 0);
        }
    }

    // Deal less damage (reverse of critical hits)
    @SubscribeEvent
    public static void onLivingDamageLiving(LivingIncomingDamageEvent event) {
        Entity damager = event.getSource().getEntity();

        if(!(damager instanceof LivingEntity entity)) {
            return;
        }

        if(!entity.hasEffect(ModEffects.UNLUCK)) {
            return;
        }

        if(!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = entity.getEffect(ModEffects.UNLUCK).getAmplifier();
        double weakHitChance = getWeakHitChance(amplifier);

        if (Math.random() < weakHitChance) {
            float originalDamage = event.getAmount();
            float weakDamage = originalDamage * 0.4f; // 60% damage reduction
            event.setAmount(weakDamage);

            ParticleUtil.spawnParticles(level, dust, event.getEntity().position().add(0, event.getEntity().getEyeHeight() / 2, 0), 30, .4, event.getEntity().getEyeHeight() / 2, .4, 0);
        }
    }

    // Various unluck effects on entity tick
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if(!entity.hasEffect(ModEffects.UNLUCK) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = entity.getEffect(ModEffects.UNLUCK).getAmplifier();

        // Apply harmful effects periodically
        if(Math.random() < getHarmfulEffectChance(amplifier)) {
            applyRandomHarmfulEffect(entity, level);
        }

        // Random trip and take damage
        if(Math.random() < getTripChance(amplifier)) {
            tripAndTakeDamage(entity, level, amplifier);
        }

        // Bad Omen effect at higher levels
        if(amplifier > 1) {
            entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 100, Math.min(amplifier - 1, 5), false, false, true));
        }

        // Spawn hostile mobs nearby
        if(Math.random() < getMobSpawnChance(amplifier)) {
            spawnHostileMob(entity, level, amplifier);
        }

        // Random item drops from inventory
        if(Math.random() < getItemDropChance(amplifier)) {
            dropRandomInventoryItem(entity, level);
        }

        // Make player slip/stumble
        if(Math.random() < getSlipChance(amplifier)) {
            makeEntitySlip(entity, level);
        }

        // Temporarily reduce damage multiplier for Beyonders
        if(BeyonderData.isBeyonder(entity) && Math.random() < getMultiplierReductionChance(amplifier)) {
            reduceMultiplierTemporarily(entity, level, amplifier);
        }

        // Temporarily disable abilities for Beyonders
        if(BeyonderData.isBeyonder(entity) && Math.random() < getAbilityDisableChance(amplifier)) {
            disableAbilitiesTemporarily(entity, level, amplifier);
        }
    }

    private static void reduceMultiplierTemporarily(LivingEntity entity, ServerLevel level, int amplifier) {
        UUID uuid = entity.getUUID();
        long currentTime = System.currentTimeMillis();

        // Cooldown check to prevent spam
        if(lastMultiplierReductionTime.containsKey(uuid) && currentTime - lastMultiplierReductionTime.get(uuid) < 8000) {
            return;
        }

        lastMultiplierReductionTime.put(uuid, currentTime);

        // Reduce multiplier by 30-60% based on amplifier
        double reductionFactor = 0.4 + (amplifier * 0.03);
        reductionFactor = Math.min(reductionFactor, 0.7); // Max 70% reduction

        int duration = 3000 + (amplifier * 200); // 3-6 seconds based on amplifier

        BeyonderData.addModifierWithTimeLimit(entity, "unluck_multiplier_reduction", reductionFactor, duration);

        ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 40, .4, entity.getEyeHeight() / 2, .4, 0);
    }

    private static void disableAbilitiesTemporarily(LivingEntity entity, ServerLevel level, int amplifier) {
        UUID uuid = entity.getUUID();
        long currentTime = System.currentTimeMillis();

        // Cooldown check to prevent spam - longer cooldown for ability disabling
        if(lastAbilityDisableTime.containsKey(uuid) && currentTime - lastAbilityDisableTime.get(uuid) < 15000) {
            return;
        }

        lastAbilityDisableTime.put(uuid, currentTime);

        int duration = 2000 + (amplifier * 150); // 2-5 seconds based on amplifier

        BeyonderData.disableAbilityUseWithTimeLimit(entity, "unluck_ability_disable", duration);

        ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 50, .5, entity.getEyeHeight() / 2, .5, 0);
        level.playSound(null, entity.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5f, 0.5f);
    }

    private static void makeEntitySlip(LivingEntity entity, ServerLevel level) {
        UUID uuid = entity.getUUID();
        long currentTime = System.currentTimeMillis();

        if(lastSlipTime.containsKey(uuid) && currentTime - lastSlipTime.get(uuid) < 3000) {
            return;
        }

        lastSlipTime.put(uuid, currentTime);

        Random random = new Random();
        Vec3 currentVelocity = entity.getDeltaMovement();
        entity.setDeltaMovement(
                currentVelocity.x * 1.5 + random.nextDouble(-0.3, 0.3),
                -0.2,
                currentVelocity.z * 1.5 + random.nextDouble(-0.3, 0.3)
        );
        entity.hurtMarked = true;

        ParticleUtil.spawnParticles(level, dust, entity.position(), 30, .5, .1, .5, 0);
    }

    private static void dropRandomInventoryItem(LivingEntity entity, ServerLevel level) {
        if(!(entity instanceof ServerPlayer player)) {
            return;
        }

        List<ItemStack> validItems = new ArrayList<>();
        for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if(!stack.isEmpty() && !stack.is(Items.AIR)) {
                validItems.add(stack);
            }
        }

        if(validItems.isEmpty()) {
            return;
        }

        ItemStack itemToDrop = validItems.get(level.getRandom().nextInt(validItems.size()));
        ItemStack droppedStack = itemToDrop.split(1);

        ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), droppedStack);
        level.addFreshEntity(itemEntity);

        ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 20, .3, .3, .3, 0);
    }

    private static void tripAndTakeDamage(LivingEntity entity, ServerLevel level, int amplifier) {
        UUID uuid = entity.getUUID();
        long currentTime = System.currentTimeMillis();

        // Cooldown check to prevent spam
        if(lastTripTime.containsKey(uuid) && currentTime - lastTripTime.get(uuid) < 2000) {
            return;
        }

        lastTripTime.put(uuid, currentTime);

        // Calculate damage based on amplifier (at amp 12: ~32.5 damage)
        float baseDamage = 10.0f;
        float damagePerLevel = 1.875f;
        float damage = baseDamage + (amplifier * damagePerLevel);

        // Apply damage
        entity.hurt(level.damageSources().magic(), damage);

        // Apply a small knockdown effect
        Random random = new Random();
        entity.setDeltaMovement(
                random.nextDouble(-0.2, 0.2),
                0.1,
                random.nextDouble(-0.2, 0.2)
        );
        entity.hurtMarked = true;

        // Visual and sound effects
        ParticleUtil.spawnParticles(level, dust, entity.position(), 40, .5, .2, .5, 0);
        level.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.7f, 0.8f);

        // Optional: Display action bar message for players
        if(entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.literal("You tripped!").withStyle(style -> style.withColor(0xA17234))
            ));
        }
    }

    private static double getTripChance(int amplifier) {
        // At amplifier 12: ~0.006 (with 20 ticks/sec = every 8.3 seconds on average)
        // Adjusted to hit every 3-5 seconds at amp 12
        return lerpClamped(amplifier, 0, 19, 0.0005, 0.015);
    }

    private static void spawnHostileMob(LivingEntity entity, ServerLevel level, int amplifier) {
        UUID uuid = entity.getUUID();
        long currentTime = System.currentTimeMillis();

        if(lastSpawnTime.containsKey(uuid) && currentTime - lastSpawnTime.get(uuid) < 15000) {
            return;
        }

        lastSpawnTime.put(uuid, currentTime);

        EntityType<?>[] hostileMobs = {
                EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.SPIDER,
                EntityType.CREEPER
        };

        Random random = new Random();
        EntityType<?> mobType = hostileMobs[random.nextInt(hostileMobs.length)];

        Vec3 spawnPos = entity.position().add(
                random.nextDouble(-5, 5),
                0,
                random.nextDouble(-5, 5)
        );

        BlockPos blockPos = BlockPos.containing(spawnPos);
        Entity mob = mobType.create(level);

        if(mob != null) {
            mob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, random.nextFloat() * 360, 0);
            level.addFreshEntity(mob);

            ParticleUtil.spawnParticles(level, dust, spawnPos, 40, .5, 1, .5, 0);
            level.playSound(null, blockPos, SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 0.5f, 0.5f);
        }
    }

    private static void applyRandomHarmfulEffect(LivingEntity entity, ServerLevel level) {
        Holder<MobEffect>[] harmfulEffects = new Holder[] {
                MobEffects.WEAKNESS,
                MobEffects.HUNGER,
                MobEffects.POISON,
                MobEffects.WITHER,
                MobEffects.BLINDNESS,
                MobEffects.MOVEMENT_SLOWDOWN,
                MobEffects.DIG_SLOWDOWN
        };

        Random random = new Random();
        Holder<MobEffect> effect = harmfulEffects[random.nextInt(harmfulEffects.length)];

        entity.addEffect(new MobEffectInstance(effect, 100, 0, false, true, true));

        ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 30, .4, entity.getEyeHeight() / 2, .4, 0);

    }

    // Probability calculation methods
    private static double getExtraDamageChance(int amplifier) {
        return Math.max(Math.min(0.06 * (amplifier + 1), 0.85), 0.06);
    }

    private static double getWeakHitChance(int amplifier) {
        return Math.max(Math.min(0.07 * (amplifier + 1), 0.90), 0.07);
    }

    private static double getItemDestroyChance(int amplifier) {
        return Math.max(Math.min(0.03 + 0.02 * amplifier, 0.40), 0.03);
    }

    private static double getToolDamageChance(int amplifier) {
        return Math.max(Math.min(0.08 + 0.04 * amplifier, 0.60), 0.08);
    }

    private static double getHarmfulEffectChance(int amplifier) {
        return amplifier >= 19 ? 0.04 : 0.001 + (0.04 - 0.001) / 19 * Math.max(amplifier, 0);
    }

    private static double getMobSpawnChance(int amplifier) {
        return lerpClamped(amplifier, 0, 19, 0.0005, 0.008);
    }

    private static double getItemDropChance(int amplifier) {
        return lerpClamped(amplifier, 0, 19, 0.0008, 0.012);
    }

    private static double getSlipChance(int amplifier) {
        return lerpClamped(amplifier, 0, 19, 0.002, 0.025);
    }

    private static double getMultiplierReductionChance(int amplifier) {
        return lerpClamped(amplifier, 0, 19, 0.0008, 0.010);
    }

    private static double getAbilityDisableChance(int amplifier) {
        return lerpClamped(amplifier, 0, 19, 0.0003, 0.005);
    }

    private static double lerpClamped(double amplifier, double minAmplifier, double maxAmplifier, double minValue, double maxValue) {
        double t = (amplifier - minAmplifier) / (maxAmplifier - minAmplifier);
        t = Math.max(0.0, Math.min(1.0, t));
        return minValue + t * (maxValue - minValue);
    }
}