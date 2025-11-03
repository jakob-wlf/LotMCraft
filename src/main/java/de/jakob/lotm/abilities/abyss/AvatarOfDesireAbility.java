package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AvatarOfDesireAbility extends ToggleAbilityItem {
    public AvatarOfDesireAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 5));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToThirdPerson();
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
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.changeToThirdPerson();
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 7, false, false, false));

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.DESIRE_AVATAR.getIndex()) {
            cancel(level, entity);
            return;
        }

        Random random = new Random();

        AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 3.5, entity.position(), new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 6, random.nextInt(5)));
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

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
