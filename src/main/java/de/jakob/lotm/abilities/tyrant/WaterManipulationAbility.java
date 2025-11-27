package de.jakob.lotm.abilities.tyrant;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WaterManipulationAbility extends SelectableAbilityItem {
    private final HashSet<UUID> castingCorrosiveRain = new HashSet<>();

    public WaterManipulationAbility(Properties properties) {
        super(properties, .75f);
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
    protected float getSpiritualityCost() {
        return 28;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.water_manipulation.water_bolt",
                "ability.lotmcraft.water_manipulation.aqueous_light",
                "ability.lotmcraft.water_manipulation.water_surge",
                "ability.lotmcraft.water_manipulation.corrosive_rain",
        };
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player) && abilityIndex == 1)
            abilityIndex = 0;

        switch(abilityIndex) {
            case 0 -> waterBolt(level, entity);
            case 1 -> aqueousLight(level, entity);
            case 2 -> waterSurge(level, entity);
            case 3 -> corrosiveRain(level, entity);
        }
    }

    private void waterSurge(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.getEyePosition().add(0, .5, 0);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, entity.getSoundSource(), 1.0f, 1.0f);

        ServerScheduler.scheduleDelayed(18, () -> AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 5.5, DamageLookup.lookupDamage(7, .875) * multiplier(entity), entity.position().add(0, .2, 0), true, false, true, 0));

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

        ServerScheduler.scheduleForDuration(0, 10, 20 * 15, () -> {
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 5, DamageLookup.lookupDps(7, .775, 10, 20) * multiplier(entity), startPos, true, false, true, 0);
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

        ServerScheduler.scheduleForDuration(0, 1, 20 * 40, () -> {
            if (hasHit.get()) {
                return;
            }

            Vec3 pos = currentPos.get();

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
        }, (ServerLevel) level);
    }

    final Vec3 eastFacing = new Vec3(1, 0, 0);
    final Vec3 southFacing = new Vec3(0, 0, 1);

    private void aqueousLight(Level level, LivingEntity entity) {
        BlockPos targetBlock = AbilityUtil.getTargetBlock(entity, 8);

        if (!level.isClientSide) {
            BlockState lightBlock = Blocks.LIGHT.defaultBlockState();
            level.setBlock(targetBlock, lightBlock, 3);

            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), .75, 20 * 20, 15, 7);
            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), southFacing, .75, 20 * 20, 15, 7);
            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), eastFacing, .75, 20 * 20, 15, 7);
            ParticleUtil.spawnParticlesForDuration((ServerLevel) level, dustOptions2, targetBlock.getCenter(), 20 * 20, 10, 3, .9);

            ServerScheduler.scheduleDelayed(20 * 20, () -> {
                if (level.getBlockState(targetBlock).is(Blocks.LIGHT)) {
                    level.setBlock(targetBlock, Blocks.AIR.defaultBlockState(), 3);
                }
            }, (ServerLevel) level);

        }
    }

    private void waterWhip(Level level, LivingEntity entity) {
    }

    @Override
    public void nextAbility(LivingEntity entity) {
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility++;
        if(selectedAbility >= (ClientBeyonderCache.getSequence(entity.getUUID()) <= 5 ? getAbilityNames().length : getAbilityNames().length - 1)) {
            selectedAbility = 0;
        }
        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(this, selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity) {
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = ClientBeyonderCache.getSequence(entity.getUUID()) <= 5 ? getAbilityNames().length - 1 : getAbilityNames().length - 2;
        }
        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(this, selectedAbility));
    }
}
