package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.LightningEntity;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MythicalCreatureFormTyrantAbility extends ToggleAbility {
    public MythicalCreatureFormTyrantAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 3;
    }

    // Does not need to get persisted as the stop gets called before a player logs out
    private static final HashMap<UUID, Double> previousScale = new HashMap<>();

    @Override
    public void start(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.changeToThirdPerson();
            return;
        }

        // Get the previous value of the Scale Attribute and save it
        AttributeInstance scaleAttribute = entity.getAttribute(Attributes.SCALE);
        if(scaleAttribute != null) {
            previousScale.put(entity.getUUID(), scaleAttribute.getValue());
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.TYRANT_MYTHICAL_CREATURE, entity);

    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.changeToThirdPerson();
            return;
        }

        // Constantly set it to be bigger so the camera is positioned better
        AttributeInstance scaleAttribute = entity.getAttribute(Attributes.SCALE);
        if(scaleAttribute != null) {
            scaleAttribute.setBaseValue(2.75);
        }

        // Buff user
        BeyonderData.addModifier(entity, "mythical_creature_form", 1.5);

        // Make all entities lower than you loose control when seeing you
        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 30).forEach(e -> {
            if(!AbilityUtil.isTargetSignificantlyWeaker(entity, e)) {
                return;
            }

            if(AbilityUtil.getTargetEntity(e, 30, 5f) != entity) {
                return;
            }

            if(!e.hasEffect(ModEffects.LOOSING_CONTROL)) { // Only apply effect when effect wasn't applied already, otherwise they would never actually die
                e.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 4, random.nextInt(4, 6)));
            }

            if(random.nextInt(6) == 0) {
                LightningEntity lightning = new LightningEntity(level, entity, e.position(), 50, 6, DamageLookup.lookupDamage(4, .7) * multiplier(entity), false, 4, 200, 0x11A8DD);
                level.addFreshEntity(lightning);
            }
        });

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.TYRANT_MYTHICAL_CREATURE.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }

    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.changeToFirstPerson();
            return;
        }

        // Reset the scale
        AttributeInstance scaleAttribute = entity.getAttribute(Attributes.SCALE);
        if(scaleAttribute != null && previousScale.containsKey(entity.getUUID())) {
            scaleAttribute.setBaseValue(previousScale.get(entity.getUUID()));
            previousScale.remove(entity.getUUID());
        }

        // Remove buff
        BeyonderData.removeModifier(entity, "mythical_creature_form");

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.TYRANT_MYTHICAL_CREATURE.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }
}
