package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.ElectricShockEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpellsAbility extends SelectableAbility {
    private final Set<UUID> isCastingWind = new HashSet<>();
    private final Set<UUID> isCastingFog = new HashSet<>();

    public SpellsAbility(String id) {
        super(id, 1.25f);
        autoClear = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 15;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.spells.wind", "ability.lotmcraft.spells.electric_shock", "ability.lotmcraft.spells.freeze", "ability.lotmcraft.spells.flash", "ability.lotmcraft.spells.fog", "ability.lotmcraft.spells.tumble", "ability.lotmcraft.spells.burning"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> wind(level, entity);
            case 1 -> electricShock(level, entity);
            case 2 -> freeze(level, entity);
            case 3 -> flash(level, entity);
            case 4 -> fog(level, entity);
            case 5 -> tumble(level, entity);
            case 6 -> burning(level, entity);
        }

        if(abilityIndex != 0)
            clearArtifactScaling(entity);
    }

    private void burning(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, entity.getEyePosition().subtract(0, .25, 0), 30, .4, .25);
        entity.getPersistentData().putBoolean("lotm_trickmaster_burning", true);
    }

    private void tumble(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 12*(int) multiplier(entity), 1);

        int maxLoopDepth = 10;
        while(level.getBlockState(BlockPos.containing(targetLoc).below()).getCollisionShape(level, BlockPos.containing(targetLoc).below()).isEmpty()) {
            targetLoc = targetLoc.subtract(0, 1, 0);
            maxLoopDepth--;

            if(maxLoopDepth <= 0)
                break;
        }

        ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.CLOUD, targetLoc, 6, 40);

        Vec3 finalTargetLoc = targetLoc;
        ServerScheduler.scheduleForDuration(0, 5, 110, () -> {
            AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, finalTargetLoc, 6).forEach(e -> {
                if(e.isShiftKeyDown()) return;
                if(e.getDeltaMovement().length() < .1) return;

                if(random.nextInt(3) != 0) return;

                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 12, false, false, false));
                e.setDeltaMovement(new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble()).normalize().multiply(.4f, .4f, .4f));
                e.hurtMarked = true;
                BeyonderData.addModifierWithTimeLimit(e, "tumble", .7, 2000);
            });
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void fog(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(isCastingFog.contains(entity.getUUID()))
            return;

        isCastingFog.add(entity.getUUID());

        Vec3 pos = entity.getEyePosition();

        ServerScheduler.scheduleForDuration(0, 2, 20 * 8, () -> {
            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.FOG_OF_WAR.get(), pos, 30, 6.5, 3, 6.5, 0);

            AbilityUtil.addPotionEffectToNearbyEntities(
                    (ServerLevel) level,
                    entity,
                    7.5,
                    pos,
                    new MobEffectInstance(MobEffects.BLINDNESS, 20, 4, false, false),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false)
            );

        }, () -> isCastingFog.remove(entity.getUUID()), (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void freeze(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 4*(int) multiplier(entity), 2);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, Blocks.ICE.getSoundType(Blocks.ICE.defaultBlockState(), level, BlockPos.containing(targetPos.x, targetPos.y, targetPos.z), null).getBreakSound(), entity.getSoundSource(), 1.0f, 1.0f);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNOWFLAKE, targetPos, 120, .5, .175);
        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5, DamageLookup.lookupDamage(8, .8) * (float) multiplier(entity), targetPos, true, false);
        AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 2.5, targetPos, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 2, false, false, false));
    }

    private void flash(Level level, LivingEntity entity) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 12*(int) multiplier(entity), 1);

        if (!level.isClientSide) {
            BlockState lightBlock = Blocks.LIGHT.defaultBlockState();
            level.setBlock(BlockPos.containing(targetLoc.x, targetLoc.y, targetLoc.z), lightBlock, 3);

            ParticleUtil.spawnParticlesForDuration((ServerLevel) level, ParticleTypes.FLASH, targetLoc, 10, 2, 2, 0);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 5, targetLoc, new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 50, false, false, false), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 5, false, false, false));

            ServerScheduler.scheduleDelayed(20, () -> {
                if (level.getBlockState(BlockPos.containing(targetLoc.x, targetLoc.y, targetLoc.z)).is(Blocks.LIGHT)) {
                    level.setBlock(BlockPos.containing(targetLoc.x, targetLoc.y, targetLoc.z), Blocks.AIR.defaultBlockState(), 3);
                }
            }, (ServerLevel) level);
        }
    }

    private void electricShock(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 start = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 15*(int) multiplier(entity), 1.4f).subtract(start).normalize();

        level.playSound(null, start.x, start.y, start.z, Blocks.ICE.getSoundType(Blocks.COPPER_GRATE.defaultBlockState(), level, BlockPos.containing(start.x, start.y, start.z), null).getStepSound(), entity.getSoundSource(), 5.0f, 1.0f);

        ElectricShockEntity shock = new ElectricShockEntity(level, entity, start, direction, 30, DamageLookup.lookupDamage(8, .7) *(int) multiplier(entity));
        level.addFreshEntity(shock);
    }

    private final Random random = new Random();

    private void wind(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(isCastingWind.contains(entity.getUUID()))
            return;

        isCastingWind.add(entity.getUUID());

        ServerScheduler.scheduleForDuration(0, 1, 20 * 6*(int) multiplier(entity), () -> {
            Vec3 dir = entity.getLookAngle().normalize().scale(.5);
            AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 10).forEach(e -> {
                if(AbilityUtil.getSequenceDifference(AbilityUtil.getSeqWithArt(entity, this), BeyonderData.getSequence(e)) >= 0) {
                    e.setDeltaMovement(dir);
                    e.hurtMarked = true;
                }
            });

            if(random.nextBoolean())
                level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.BLOCKS, .8f, 1);

            for(int i = 0; i < 10; i++) {
                Vec3 pos = VectorUtil.getRelativePosition(entity.getEyePosition(), dir, random.nextDouble(-2, 2.5), random.nextDouble(-7, 7), random.nextDouble(-3, 3));

                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, pos, 0, dir.x, dir.y, dir.z, 1);
            }

        }, () -> {
            isCastingWind.remove(entity.getUUID());
            clearArtifactScaling(entity);
            }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(entity.position(), level)));
    }

    @SubscribeEvent
    public static void onLivingDamageLiving(LivingDamageEvent.Post event) {
        if(
                !(event.getSource().getEntity() instanceof LivingEntity living) ||
                !living.getPersistentData().contains("lotm_trickmaster_burning") ||
                !living.getPersistentData().getBoolean("lotm_trickmaster_burning")
        ) return;

        living.getPersistentData().remove("lotm_trickmaster_burning");
        event.getEntity().setRemainingFireTicks(event.getEntity().getRemainingFireTicks() + 80);
    }

    @SubscribeEvent
    public static void onPlayerHitBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getEntity().getPersistentData().contains("lotm_trickmaster_burning") || !event.getEntity().getPersistentData().getBoolean("lotm_trickmaster_burning"))
            return;

        Level level = event.getEntity().level();
        if(level.isClientSide)
            return;

        event.getEntity().getPersistentData().remove("lotm_trickmaster_burning");
        BlockPos pos = event.getPos();

        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, pos.above().getBottomCenter(), 100, .5, .5, .5, 0.05);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, pos.above().getBottomCenter(), 50, .5, .5, .5, 0.05);

        System.out.println(pos.above() + " " + level.getBlockState(pos.above()).canBeReplaced() + " " + level.getBlockState(pos.above()).isEmpty() + " " + level.getBlockState(pos.above()).getBlock());

        if(level.getBlockState(pos.above()).canBeReplaced() || level.getBlockState(pos.above()).isEmpty()) {
            level.setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState());
        }
    }

    @SubscribeEvent
    public static void onPlayerHitAir(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().getPersistentData().contains("lotm_trickmaster_burning") || !event.getEntity().getPersistentData().getBoolean("lotm_trickmaster_burning"))
            return;

        Level level = event.getEntity().level();
        if(level.isClientSide)
            return;

        event.getEntity().getPersistentData().remove("lotm_trickmaster_burning");
        BlockPos pos = event.getPos();

        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, pos.above().getBottomCenter(), 100, .5, .5, .5, 0.05);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, pos.above().getBottomCenter(), 50, .5, .5, .5, 0.05);

        System.out.println(pos.above() + " " + level.getBlockState(pos.above()).canBeReplaced() + " " + level.getBlockState(pos.above()).isEmpty() + " " + level.getBlockState(pos.above()).getBlock());

        if(level.getBlockState(pos.above()).canBeReplaced() || level.getBlockState(pos.above()).isEmpty()) {
            level.setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState());
        }
    }
}
