package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HistoricalVoidHidingAbility extends ToggleAbilityItem {
    public HistoricalVoidHidingAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 5;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 3));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.FOG_OF_HISTORY, entity);
    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        FogComponent fogComponent = entity.getData(ModAttachments.FOG_COMPONENT);
        fogComponent.setActiveAndSync(true, entity);
        fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.FOG_OF_HISTORY, entity);

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.FOG_OF_HISTORY.getIndex()) {
            cancel(level, entity);
            return;
        }

    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.FOG_OF_HISTORY.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }
}
