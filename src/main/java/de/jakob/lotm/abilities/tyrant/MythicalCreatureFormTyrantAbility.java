package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class MythicalCreatureFormTyrantAbility extends ToggleAbilityItem {
    public MythicalCreatureFormTyrantAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3;
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.changeToThirdPerson();
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.TYRANT_MYTHICAL_CREATURE, entity);

    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.changeToThirdPerson();
            return;
        }

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.TYRANT_MYTHICAL_CREATURE.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }

    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.changeToFirstPerson();
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.TYRANT_MYTHICAL_CREATURE.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }
}
