package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SunSpellsAbility extends SelectableAbility {

    private final DustParticleOptions goldenDust = new DustParticleOptions(
            new Vector3f(1f, 0.8f, 0f),
            1.5f
    );

    public SunSpellsAbility(String id) {
        super(id, 4, "light_source", "purification", "light_weak");
        postsUsedAbilityEventManually = true;

        interactionCacheTicks = 20 * 20;
        interactionRadius = 5;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 80;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.sun_spells.sunshine", "ability.lotmcraft.sun_spells.blessing", "ability.lotmcraft.sun_spells.night_vision", "ability.lotmcraft.sun_spells.illuminate"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(!(entity instanceof Player)) selectedAbility = 0;
        if(level.isClientSide) return;
        switch (selectedAbility) {
            case 0 -> sunshine(level, entity);
            case 1 -> blessing(level, entity);
            case 2 -> nightVision(level, entity);
            case 3 -> illuminate(level, entity);
        }
    }

    final int radius = 12;
    final int duration = 20 * 25;

    final Vec3 eastFacing = new Vec3(1, 0, 0);
    final Vec3 southFacing = new Vec3(0, 0, 1);

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            5f
    );

    private void illuminate(Level level, LivingEntity entity) {
        BlockPos targetBlock = AbilityUtil.getTargetBlock(entity, radius, true);

        BlockState lightBlock = Blocks.LIGHT.defaultBlockState();
        level.setBlock(targetBlock, lightBlock, 3);

        ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), 1, duration, 10, 7);
        ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), southFacing, 1, duration, 10, 7);
        ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), eastFacing, 1, duration, 10, 7);
        ParticleUtil.spawnParticlesForDuration((ServerLevel) level, dustOptions, targetBlock.getCenter(), duration, 10, random.nextInt(1, 6), .9);

        ServerScheduler.scheduleDelayed(duration, () -> {
            if (level.getBlockState(targetBlock).is(Blocks.LIGHT)) {
                level.setBlock(targetBlock, Blocks.AIR.defaultBlockState(), 3);
            }
        }, (ServerLevel) level);
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetBlock.getCenter(), entity, this, interactionFlags, interactionRadius, interactionCacheTicks));
    }

    private void sunshine(Level level, LivingEntity entity) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 8, 2).add(0, 2, 0);

        if(level.getBlockState(BlockPos.containing(targetLoc)).getCollisionShape(level, BlockPos.containing(targetLoc)).isEmpty()) {
            level.setBlockAndUpdate(BlockPos.containing(targetLoc), Blocks.LIGHT.defaultBlockState());
        }

        level.playSound(null, BlockPos.containing(targetLoc), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1, 1);
        ServerScheduler.scheduleForDuration(0, 5, 20 * 12, () -> {
            ParticleUtil.spawnSphereParticles((ServerLevel) level, goldenDust, targetLoc, 1.4f, 100);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc, 1.4f, 50);

            AbilityUtil.getNearbyEntities(null, (ServerLevel) level, targetLoc, 10).forEach(e -> {
                if(!AbilityUtil.isUndead(e)) return;

                e.hurt(ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, entity),  (float) DamageLookup.lookupDps(8, .9, 5, 30) * multiplier(entity));
            });
        }, () -> {
            if(level.getBlockState(BlockPos.containing(targetLoc)).is(Blocks.LIGHT)) {
                level.setBlockAndUpdate(BlockPos.containing(targetLoc), Blocks.AIR.defaultBlockState());
            }
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(targetLoc, level)));

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetLoc, entity, this, interactionFlags, 10, 20 * 12));
    }

    private void blessing(Level level, LivingEntity entity) {
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 6, 2, false, true);
        if(targetEntity == null) {
            targetEntity = entity;
        }

        level.playSound(null, BlockPos.containing(targetEntity.position()), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1, 1);
        targetEntity.getTags().add("light_supplicant_blessing");
        ParticleUtil.spawnCircleParticles((ServerLevel) level, goldenDust, targetEntity.getEyePosition(), new Vec3(0, 1, 0), 1.5, 40);
        ParticleUtil.spawnCircleParticles((ServerLevel) level, goldenDust, targetEntity.getEyePosition(), new Vec3(1, 0, 0), 1.5, 40);
        ParticleUtil.spawnCircleParticles((ServerLevel) level, goldenDust, targetEntity.getEyePosition(), new Vec3(0, 0, 1), 1.5, 40);

        LivingEntity finalTargetEntity = targetEntity;
        ServerScheduler.scheduleDelayed(20 * 30, () -> {
            if(!finalTargetEntity.isAlive()) return;
            ParticleUtil.spawnCircleParticles((ServerLevel) level, goldenDust, finalTargetEntity.getEyePosition(), new Vec3(0, 1, 0), 1, 40);
            ParticleUtil.spawnCircleParticles((ServerLevel) level, goldenDust, finalTargetEntity.getEyePosition(), new Vec3(1, 0, 0), 1, 40);
            ParticleUtil.spawnCircleParticles((ServerLevel) level, goldenDust, finalTargetEntity.getEyePosition(), new Vec3(0, 0, 1), 1, 40);
            finalTargetEntity.getTags().remove("light_supplicant_blessing");
        });
    }

    private void nightVision(Level level, LivingEntity entity) {
        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1, 1);

        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60, 0, false, false, false));
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, entity.getEyePosition(), 20, .3, .3, .3, .075);
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if(event.getSource().getEntity() == null) return;
        if(!(event.getEntity().level() instanceof ServerLevel level)) return;

        LivingEntity entity = event.getEntity();
        Entity source = event.getSource().getEntity();

        if(!(source instanceof LivingEntity)) return;

        if(source.getTags().contains("light_supplicant_blessing") && AbilityUtil.isUndead(entity)) {
            event.setAmount(event.getAmount() * 1.5f);
            ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD, entity.getEyePosition(), 10, .3, .3, .3, .075);
        }

        if(entity.getTags().contains("light_supplicant_blessing") && AbilityUtil.isUndead((LivingEntity) source)) {
            event.setAmount(event.getAmount() * 0.75f);
            ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD, entity.getEyePosition(), 10, .3, .3, .3, .075);
        }
    }
}
