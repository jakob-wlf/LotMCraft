package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AvatarOfDesireAbility extends ToggleAbility {
    public AvatarOfDesireAbility(String id) {
        super(id);
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 5));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToThirdPerson(entity);
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.DESIRE_AVATAR, entity);

        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(.3f);
        }

    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToThirdPerson(entity);
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 7, false, false, false));

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.DESIRE_AVATAR.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }

        Random random = new Random();

        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 3.75, false).forEach(e -> {
            if(AbilityUtil.isTargetSignificantlyWeaker(entity, e)) {
                e.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 7));
                return;
            }
            else if(AbilityUtil.isTargetSignificantlyStronger(entity, e)) {
                entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 3));
                entity.hurt(entity.damageSources().generic(), 10);
                cancel((ServerLevel) level, entity);
                return;
            }

            e.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, random.nextInt(2, 5)));
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToFirstPerson(entity);
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.DESIRE_AVATAR.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(1f);
        }
    }
}
