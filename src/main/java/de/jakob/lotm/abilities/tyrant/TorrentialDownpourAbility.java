package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3f;

import java.util.*;

public class TorrentialDownpourAbility extends Ability {
    public TorrentialDownpourAbility(String id) {
        super(id, 40);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(30 / 255f, 120 / 255f, 255 / 255f),
            10f
    );

    private final DustParticleOptions dustOptions2 = new DustParticleOptions(
            new Vector3f(1, 1, 1),
            10f
    );

    private final DustParticleOptions iceDust = new DustParticleOptions(
            new Vector3f(211 / 255f, 232 / 255f, 230 / 255f),
            10f
    );

    private static final HashSet<TorrentialDownpourData> activeDownpours = new HashSet<>();

    @Override
    public float getSpiritualityCost() {
        return 900;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ServerLevel serverLevel = (ServerLevel) level;
        serverLevel.setWeatherParameters(
                0,          // clearDuration (0 = start immediately)
                20 * 30,       // rainDuration (ticks → 2400 = 2 minutes)
                true,       // raining
                true        // thundering
        );

        Vec3 startPos = AbilityUtil.getTargetLocation(entity, 25, 2);
        Vec3 cloudPos = startPos.add(0, 12, 0);
        Vec3 rainPos = startPos.add(0, 7, 0);

        List<BlockPos> blocks = new ArrayList<>(AbilityUtil.getBlocksInCircle((ServerLevel) level, startPos.add(0, -2, 0), 27));
        for(int i = -12; i < 13; i++) {
            blocks.addAll(AbilityUtil.getBlocksInCircle((ServerLevel) level, startPos.add(0, i, 0), 27));
        }

        List<BlockPos> validBlocks = blocks.stream().filter(b -> !level.getBlockState(b).getCollisionShape(level, b).isEmpty() && level.getBlockState(b.above()).getCollisionShape(level, b).isEmpty() && !level.getBlockState(b).is(Blocks.WATER)).toList();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        UUID downpourId = UUID.randomUUID();
        TorrentialDownpourData data = new TorrentialDownpourData(new Location(startPos, level), downpourId, false, BeyonderData.getSequence(entity));
        activeDownpours.add(data);

        // Scheduler for Animations
        ServerScheduler.scheduleForDuration(0, 4, 20 * 30, () -> {
            boolean isFrozen = isFrozen(downpourId);

            level.playSound(null, rainPos.x, rainPos.y, rainPos.z, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 2, 1);

            if(!isFrozen) {
                ParticleUtil.spawnParticles((ServerLevel) level, dustOptions2, cloudPos, 700, 20, .4, 20, 0);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.RAIN, rainPos, 300, 20, 10, 20, 0);
                ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, rainPos, 100, 20, 10, 20, 0);
            } else {
                ParticleUtil.spawnParticles((ServerLevel) level, dustOptions2, cloudPos, 700, 20, .4, 20, 0);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.RAIN, rainPos, 300, 20, 10, 20, 0);
                ParticleUtil.spawnParticles((ServerLevel) level, iceDust, rainPos, 100, 20, 10, 20, 0);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNOWFLAKE, rainPos, 500, 20, 10, 20, 0);
            }

            if(griefing) {
                for (int i = 0; i < 10; i++) {
                    BlockPos pos = validBlocks.get(random.nextInt(validBlocks.size()));
                    BlockState state = level.getBlockState(pos);
                    if(state.getCollisionShape(level, pos).isEmpty() || state.is(Blocks.WATER))
                        continue;
                    if(!isFrozen) level.setBlockAndUpdate(pos, random.nextBoolean() ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState());
                    else level.setBlockAndUpdate(pos, random.nextBoolean() ? Blocks.AIR.defaultBlockState() : Blocks.ICE.defaultBlockState());
                }
            }
        }, () -> activeDownpours.remove(data), (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(startPos, level)));

        // Scheduler for Damage
        ServerScheduler.scheduleForDuration(0, 10, 20 * 30, () -> {
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 25, DamageLookup.lookupDps(3, .75, 10, 20) * multiplier(entity), startPos, true, false, true, 0);
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(startPos, level)));
    }

    private static boolean isFrozen(UUID downpourId) {
        return activeDownpours.stream().filter(d -> d.downpourId.equals(downpourId)).map(d -> d.frozen).findFirst().orElse(false);
    }

    public static HashSet<TorrentialDownpourData> getActiveDownpours() {
        return new HashSet<>(activeDownpours);
    }

    public static void freezeDownpour(UUID downpourId) {
        activeDownpours.stream().filter(d -> d.downpourId.equals(downpourId)).findFirst().ifPresent(d -> {
            activeDownpours.remove(d);
            activeDownpours.add(new TorrentialDownpourData(d.loc, d.downpourId, true, d.sequence));
        });
    }

    public static void cancelDownpour(UUID downpourId) {
        activeDownpours.removeIf(d -> d.downpourId.equals(downpourId));
    }

    public record TorrentialDownpourData(Location loc, UUID downpourId, boolean frozen, int sequence) {}
}
