package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpatialCageAbility extends SelectableAbility {

    private static final HashMap<UUID, HashSet<SpatialCage>> activeCages = new HashMap<>();

    public SpatialCageAbility(String id) {
        super(id, 20);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1400;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.spatial_cage.surround", "ability.lotmcraft.spatial_cage.open_front"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide) return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 35, 2);
        Direction frontDirection = entity.getDirection().getOpposite();
        createSpatialCage(entity, level, targetLoc, selectedAbility != 0, frontDirection);
    }

    private void createSpatialCage(LivingEntity entity, Level level, Vec3 targetLoc, boolean openFront, Direction frontDirection) {
        SpatialCage cage = new SpatialCage((ServerLevel) level, targetLoc, openFront, 5, new ArrayList<>(), frontDirection, entity.getUUID());
        cage.createCage();
        activeCages.computeIfAbsent(entity.getUUID(), k -> new HashSet<>()).add(cage);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 60 * 4, () -> {
            cage.updateCage();
            cage.addParticles();
            cage.addSlownessToEntities();
        }, cage::removeCage, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(targetLoc, level)));
    }

    private record SpatialCage(ServerLevel level, Vec3 position, boolean isFrontOpen, int radius, List<BlockPos> barrierBlocks, Direction frontDirection, UUID owner) {
        void createCage() {
            List<BlockPos> barrierBlocks = new ArrayList<>();

            BlockPos center = BlockPos.containing(position);

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        double distSq = x * x + y * y + z * z;
                        double outerRadius = radius;
                        double innerRadius = radius - 1;

                        if (distSq <= outerRadius * outerRadius && distSq > innerRadius * innerRadius) {
                            BlockPos blockPos = center.offset(x, y, z);

                            if (isFrontOpen) {
                                Vec3 blockVec = new Vec3(x, y, z).normalize();
                                Vec3 frontVec = Vec3.atLowerCornerOf(frontDirection.getNormal());
                                double dot = blockVec.dot(frontVec);

                                if (dot > 0.5) {
                                    continue;
                                }
                            }

                            barrierBlocks.add(blockPos);}
                    }
                }
            }

            this.barrierBlocks.addAll(barrierBlocks);
        }

        void removeCage() {
            for (BlockPos pos : barrierBlocks) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            activeCages.getOrDefault(owner, new HashSet<>()).remove(this);
        }

        void updateCage() {
            for (BlockPos pos : barrierBlocks) {
                if(!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                    continue;
                }
                level.setBlockAndUpdate(pos, Blocks.BARRIER.defaultBlockState());
            }
        }

        void addParticles() {
            for (BlockPos pos : barrierBlocks) {
                if(level.getRandom().nextBoolean()) ParticleUtil.spawnParticles(level, ModParticles.STAR.get(), pos.getCenter(), 1, .55, 0);
                if(level.getRandom().nextBoolean()) ParticleUtil.spawnParticles(level, new DustParticleOptions(new Vector3f(0, 0, 0), 3.5f), pos.getCenter(), 1, .55, 0);
            }
        }

        void addSlownessToEntities() {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(position.x - radius, position.y - radius, position.z - radius, position.x + radius, position.y + radius, position.z + radius))) {
                if (entity.position().distanceTo(position) <= radius && !entity.getUUID().equals(owner)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 9, false, false, false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();
        if (activeCages.containsKey(playerUUID)) {
            for (SpatialCage cage : activeCages.get(playerUUID)) {
                if(cage != null)
                    cage.removeCage();
            }
            activeCages.remove(playerUUID);
        }
    }
}
