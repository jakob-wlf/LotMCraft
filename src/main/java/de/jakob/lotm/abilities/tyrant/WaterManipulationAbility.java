package de.jakob.lotm.abilities.tyrant;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.A;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WaterManipulationAbility extends SelectableAbility {
    private final HashSet<UUID> castingCorrosiveRain = new HashSet<>();
    private final HashSet<UUID> gettingSuffocated = new HashSet<>();

    public WaterManipulationAbility(String id) {
        super(id, 1.25f, "water");
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(30 / 255f, 120 / 255f, 255 / 255f),
            1.5f
    );
    private final DustParticleOptions dustOptions2 = new DustParticleOptions(
            new Vector3f(30 / 255f, 153 / 255f, 255 / 255f),
            2.5f
    );


    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "tyrant", 7
        ));
    }

    @Override
    public float getSpiritualityCost() {
        return 28;
    }

    @Override
    public String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.water_manipulation.water_bolt",
                "ability.lotmcraft.water_manipulation.aqueous_light",
                "ability.lotmcraft.water_manipulation.suffocate",
                "ability.lotmcraft.water_manipulation.disperse_water",
                "ability.lotmcraft.water_manipulation.water_surge",
                "ability.lotmcraft.water_manipulation.corrosive_rain",
        };
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player) && (abilityIndex == 1 || abilityIndex == 3))
            abilityIndex = 0;

        switch(abilityIndex) {
            case 0 -> waterBolt(level, entity);
            case 1 -> aqueousLight(level, entity);
            case 2 -> suffocate(level, entity);
            case 3 -> disperseWater(level, entity);
            case 4 -> waterSurge(level, entity);
            case 5 -> corrosiveRain(level, entity);
        }
    }

    private void disperseWater(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_SPLASH, entity.getSoundSource(), 1.0f, 1.0f);

        ItemStack handItem = entity.getMainHandItem();
        boolean wasOffHand = false;
        if(handItem.getItem() != Items.BUCKET && handItem.getItem() != Items.GLASS_BOTTLE) {
            handItem = entity.getOffhandItem();
            wasOffHand = true;
        }

        if(handItem.getItem() != Items.BUCKET && handItem.getItem() != Items.GLASS_BOTTLE) {
            BlockPos targetBlock = AbilityUtil.getTargetBlock(entity, 3, true);

            if(level.getBlockState(targetBlock).getCollisionShape(level, targetBlock).isEmpty()) {
                level.setBlockAndUpdate(targetBlock, Blocks.WATER.defaultBlockState());
            }
            return;
        }

        ItemStack newItem;
        if(handItem.getItem() == Items.BUCKET) {
            newItem = new ItemStack(Items.WATER_BUCKET);
        }
        else {
            newItem = new ItemStack(Items.POTION);
        }
        handItem.shrink(1);
        if(handItem.isEmpty()) {
            if(wasOffHand) {
                entity.setItemInHand(InteractionHand.OFF_HAND, newItem);
            }
            else {
                entity.setItemInHand(InteractionHand.MAIN_HAND, newItem);
            }
        }
        else if(entity instanceof Player player) {
            player.addItem(newItem);
        }
    }

    private void suffocate(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 10, 1.5f);
        if(target == null)
            return;

        if(gettingSuffocated.contains(target.getUUID()))
            return;

        gettingSuffocated.add(target.getUUID());

        AtomicBoolean wasCancelled = new AtomicBoolean(false);
        AtomicInteger suffocateTime = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, 20 * 8, () -> {
            if(wasCancelled.get()) {
                return;
            }

            if(InteractionHandler.isInteractionPossible(new Location(target.getEyePosition(), level), "burning", 7, false) || !target.isAlive()) {
                wasCancelled.set(true);
                return;
            }

            if(suffocateTime.get() % 30 == 0) {
                target.setAirSupply(-20);
                target.hurt(target.damageSources().drown(), 2.5F);
                suffocateTime.set(0);
            }

            suffocateTime.getAndIncrement();

            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.BUBBLE, target.getEyePosition(), 15, 0.2, 0.1);
            ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, target.getEyePosition(), 4, 0.2, 0.1);

        }, () -> gettingSuffocated.remove(target.getUUID()), (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void waterSurge(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.getEyePosition().add(0, .5, 0);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, entity.getSoundSource(), 1.0f, 1.0f);

        ServerScheduler.scheduleDelayed(18, () -> AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 5.5, DamageLookup.lookupDamage(7, .875) * (int) Math.max(multiplier(entity)/4,1), entity.position().add(0, .2, 0), true, false, true, 0));

        AtomicDouble i = new AtomicDouble(0.6);
        ServerScheduler.scheduleForDuration(0, 1, 24, () -> {
            double ySubtraction = 2 * ((1/((10 * i.get()) - 9)) - 1);
            Vec3 currentPos = startPos.add(0, ySubtraction, 0);
            double radius = i.get() < .71 ? i.get() : i.get() * 2;
            ParticleUtil.spawnCircleParticles((ServerLevel) level, dustOptions, currentPos, radius, (int) (radius * 16));
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.FALLING_WATER, currentPos, radius, (int) (radius * 16));
            i.set(i.get() + .1);
        }, (ServerLevel) level);
    }

    private void corrosiveRain(Level level, LivingEntity entity) {
        if (level.isClientSide)
            return;

        if (castingCorrosiveRain.contains(entity.getUUID()))
            return;

        castingCorrosiveRain.add(entity.getUUID());

        Vec3 startPos = AbilityUtil.getTargetLocation(entity, 15, 2);
        Vec3 cloudPos = startPos.add(0, 8, 0);
        Vec3 rainPos = startPos.add(0, 3, 0);
        ServerScheduler.scheduleForDuration(0, 4, 20 * 15, () -> {
            level.playSound(null, rainPos.x, rainPos.y, rainPos.z, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 2, 1);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, cloudPos, 120, 5, .4, 5, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.RAIN, rainPos, 180, 4, 4, 4, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, rainPos, 35, 4, 4, 4, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNEEZE, rainPos, 45, 4, 4, 4, 0);
        }, () -> castingCorrosiveRain.remove(entity.getUUID()), (ServerLevel) level);

        double multiplier = multiplier(entity);
        ServerScheduler.scheduleForDuration(0, 10, 20 * 15, () -> {
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 5, DamageLookup.lookupDps(7, .775, 10, 20* (int) Math.max(multiplier(entity)/6,1)) * (int) Math.max(multiplier(entity)/4,1), startPos, true, false, true, 0);
        }, (ServerLevel) level);
    }

    private void waterBolt(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(-.65, .65), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 10, 1.4f).subtract(startPos).normalize();

        AtomicReference<Vec3> currentPos = new AtomicReference<>(startPos);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.PLAYER_SPLASH, entity.getSoundSource(), 2.0f, 1.0f);

        AtomicBoolean hasHit = new AtomicBoolean(false);
        AtomicBoolean frozen = new AtomicBoolean(false);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if (hasHit.get()) {
                return;
            }

            if(InteractionHandler.isInteractionPossible(new Location(currentPos.get(), level), "freezing")) {
                frozen.set(true);
            }

            Vec3 pos = currentPos.get();

            if(frozen.get()) {
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNOWFLAKE, pos, 12, 0.24, 0.02);
                return;
            }

            if(AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5f, DamageLookup.lookupDamage(7, .825) * multiplier(entity), pos, true, false, true, 0)) {
                hasHit.set(true);
                return;
            }

            if(!level.getBlockState(BlockPos.containing(pos.x, pos.y, pos.z)).isAir()) {
                if(BeyonderData.isGriefingEnabled(entity)) {
                    pos = pos.subtract(direction);
                    level.setBlockAndUpdate(BlockPos.containing(pos.x, pos.y, pos.z), Blocks.WATER.defaultBlockState());
                }
                hasHit.set(true);
                return;
            }

            ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, pos, 23, 0.24, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.BUBBLE, pos, 12, 0.24, 0.02);

            currentPos.set(pos.add(direction));
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    final Vec3 eastFacing = new Vec3(1, 0, 0);
    final Vec3 southFacing = new Vec3(0, 0, 1);

    private void aqueousLight(Level level, LivingEntity entity) {
        BlockPos targetBlock = AbilityUtil.getTargetBlock(entity, 8, true);

        if (!level.isClientSide) {
            BlockState lightBlock = Blocks.LIGHT.defaultBlockState();
            level.setBlock(targetBlock, lightBlock, 3);

            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), .75, 20 * 20, 15, 7);
            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), southFacing, .75, 20 * 20, 15, 7);
            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), eastFacing, .75, 20 * 20, 15, 7);
            ParticleUtil.spawnParticlesForDuration((ServerLevel) level, dustOptions2, targetBlock.getCenter(), 20 * 20, 10, 3, .9);

            ServerScheduler.scheduleForDuration(0, 10, 20 * 20 * (int) Math.max(multiplier(entity)/4,1), () -> {
                AbilityUtil.getNearbyEntities(null, (ServerLevel) level, targetBlock.getCenter(), 3).forEach(e -> {
                    e.heal(.5f * multiplier(entity));
                });
            }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

            ServerScheduler.scheduleDelayed(20 * 20 * (int) Math.max(multiplier(entity)/4,1), () -> {
                if (level.getBlockState(targetBlock).is(Blocks.LIGHT)) {
                    level.setBlock(targetBlock, Blocks.AIR.defaultBlockState(), 3);
                }
            }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

        }
    }

    private void waterWhip(Level level, LivingEntity entity) {
    }
}
