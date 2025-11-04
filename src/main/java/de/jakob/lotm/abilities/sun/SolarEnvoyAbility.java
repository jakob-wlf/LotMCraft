package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SolarEnvoyAbility extends ToggleAbilityItem {
    public SolarEnvoyAbility(Properties properties) {
        super(properties);
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
    protected void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToThirdPerson();
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.SOLAR_ENVOY, entity);


    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToThirdPerson();
            return;
        }


        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.SOLAR_ENVOY.getIndex()) {
            cancel(level, entity);
            return;
        }

    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.SOLAR_ENVOY.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }
}
