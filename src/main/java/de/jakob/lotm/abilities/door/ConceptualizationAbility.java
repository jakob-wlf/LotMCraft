package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ConceptualizationAbility extends ToggleAbility {
    public ConceptualizationAbility(String id) {
        super(id);
    }

    @Override
    public float getSpiritualityCost() {
        return 2;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.CONCEPTUALIZATION, entity);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        // Allow Flying
        if(entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(.225f);
            player.onUpdateAbilities();
        }

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.CONCEPTUALIZATION.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.ENCHANT, entity.position(), 200, .8, .8, .8, .1);
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.STAR.get(), entity.position(), 5, .4, .4, .4, .1);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
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
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.CONCEPTUALIZATION.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

        ServerScheduler.scheduleForDuration(0, 1, 50, () -> {
            entity.resetFallDistance();
            entity.fallDistance = 0;
        });
    }
}
