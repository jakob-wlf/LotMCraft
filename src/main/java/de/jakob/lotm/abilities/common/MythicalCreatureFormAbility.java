package de.jakob.lotm.abilities.common;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MythicalCreatureFormAbility extends ToggleAbility {
    private static final HashMap<UUID, Double> previousScale = new HashMap<>();

    public MythicalCreatureFormAbility(String id) {
        super(id);

        this.canBeCopied = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AttributeInstance scaleAttribute = entity.getAttribute(Attributes.SCALE);
        if(scaleAttribute != null) {
            scaleAttribute.setBaseValue(2.75);
        }

        int seq = BeyonderData.getSequence(entity);
        if(seq > 2){
            var sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());
            sanity.setSanityAndSync(sanity.getSanity() - (seq == 4 ? 0.01f : 0.005f), entity);
        }

        // Buff user
        BeyonderData.addModifier(entity, "mythical_creature_form", (seq > 2 ? 1.25 : 1.75));

        int amplifier = (seq > 2 ? 4 : 6);

        // Make all entities lower than you loose control when seeing you
        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 30).forEach(e -> {
            if(!AbilityUtil.isTargetSignificantlyWeaker(entity, e)) {
                return;
            }

            if(AbilityUtil.getTargetEntity(e, 30, 5f) != entity) {
                return;
            }

            if(!e.hasEffect(ModEffects.LOOSING_CONTROL)) { // Only apply effect when effect wasn't applied already, otherwise they would never actually die
                e.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 4, amplifier));
            }

            doPathRelatedEffect(BeyonderData.getPathway(entity), level, entity, e);
        });

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.MYTHICAL_CREATURE.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AttributeInstance scaleAttribute = entity.getAttribute(Attributes.SCALE);
        if(scaleAttribute != null) {
            previousScale.put(entity.getUUID(), scaleAttribute.getValue());
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.MYTHICAL_CREATURE, entity);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AttributeInstance scaleAttribute = entity.getAttribute(Attributes.SCALE);
        if(scaleAttribute != null && previousScale.containsKey(entity.getUUID())) {
            scaleAttribute.setBaseValue(previousScale.get(entity.getUUID()));
            previousScale.remove(entity.getUUID());
        }

        // Remove buff
        BeyonderData.removeModifier(entity, "mythical_creature_form");

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.MYTHICAL_CREATURE.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap();

        for(String pathway : BeyonderData.pathways) {
            reqs.put(pathway, 4);
        }
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    private void doPathRelatedEffect(String pathway, Level level, LivingEntity entity, LivingEntity e){
        switch (pathway){
            case "tyrant":
                if(random.nextInt(6) == 0) {
                    LightningEntity lightning = new LightningEntity(level, entity, e.position(), 50, 6, DamageLookup.lookupDamage(4, .7) * multiplier(entity), false, 4, 200, 0x11A8DD);
                    level.addFreshEntity(lightning);
                }
                break;

            default:
                break;
        }
    }
}
