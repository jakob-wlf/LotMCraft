package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SolarEnvoyAbility extends ToggleAbility {
    private final HashMap<UUID, Vec3> locations = new HashMap<>();

    public SolarEnvoyAbility(String id) {
        super(id);
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 2));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.changeToThirdPerson();
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.SOLAR_ENVOY, entity);

        locations.put(entity.getUUID(), entity.position().add(0, 5, 0));

        Random random = new Random();

        // Destroy blocks
        if (BeyonderData.isGriefingEnabled(entity)) {
            // Get all blocks once
            List<BlockPos> sphereBlocks = AbilityUtil.getBlocksInSphereRadius(
                    serverLevel, entity.position(), 25, true, true, false
            );

            for (BlockPos pos : sphereBlocks) {
                BlockState state = serverLevel.getBlockState(pos);

                // Remove all non-air blocks
                if (!state.isAir()) {
                    serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }

                // Roughly 1/3 chance to place fire nearby (only if the spot is now empty)
                if (random.nextInt(3) == 0 && serverLevel.isEmptyBlock(pos)) {
                    serverLevel.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                }
            }
        }

    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToThirdPerson();
            return;
        }


        // Stop movement and keep position
        entity.setDeltaMovement(0, 0, 0);
        entity.setNoGravity(true);
        if(locations.containsKey(entity.getUUID())) {
            entity.teleportTo(locations.get(entity.getUUID()).x, locations.get(entity.getUUID()).y, locations.get(entity.getUUID()).z);
        }

        // Damage entities
        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 37, DamageLookup.lookupDps(2, .9, 5, 20) * multiplier(entity), entity.position(), true, true, 20 * 5);

        // Particles
        ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, entity.position(), 2.6, 60);
        ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.FLAME, entity.position(), 2.6, 60);

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.SOLAR_ENVOY.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }

    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToFirstPerson();
            return;
        }

        entity.setNoGravity(false);

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.SOLAR_ENVOY.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }
}
