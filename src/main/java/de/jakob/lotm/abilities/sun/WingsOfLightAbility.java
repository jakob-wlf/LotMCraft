package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class WingsOfLightAbility extends ToggleAbilityItem {

    public WingsOfLightAbility(Properties properties) {
        super(properties);

        canBeUsedByNPC = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return 5;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 2));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.WINGS_OF_LIGHT, entity);

    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        // Allow Flying
        if(entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(.15f);
            player.onUpdateAbilities();
        }

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.WINGS_OF_LIGHT.getIndex()) {
            cancel(level, entity);
            return;
        }

    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        // Disable Flying
        if(entity instanceof Player player) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(.05f);
            player.onUpdateAbilities();
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.WINGS_OF_LIGHT.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }
}
