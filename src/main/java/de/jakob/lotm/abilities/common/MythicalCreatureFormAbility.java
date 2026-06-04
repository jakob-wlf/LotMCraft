package de.jakob.lotm.abilities.common;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.LightningEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MythicalCreatureFormAbility extends ToggleAbility {

    private static final HashMap<UUID, Double> previousScale = new HashMap<>();

    public MythicalCreatureFormAbility(String id) {
        super(id);

        this.canBeCopied = false;
        this.cannotBeStolen = true;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = false;
        this.canAlwaysBeUsed = true;
        this.doesNotIncreaseDigestion = true;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int seq = BeyonderData.getSequence(entity);
        if(seq > 2){
            var sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());
            sanity.setSanityAndSync(Math.max(0.0f, sanity.getSanity() - (seq == 4 ? 0.01f : 0.005f)), entity);
        }

        int range = 100;

        // Make all entities lower than you loose control when seeing you
        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), range).forEach(e -> {
                    if (AbilityUtil.getTargetEntity(e, range, 5f, true) != entity) {
                        return;
                    }

                    if (!entity.getData(ModAttachments.ALLY_COMPONENT.get()).isAlly(e.getUUID())) {
                        int entitySeq = BeyonderData.getSequence(entity);

                        e.getData(ModAttachments.SANITY_COMPONENT.get()).decreaseSanityWithSequenceDifference(
                                getAmount(entitySeq), e,
                                BeyonderData.getSequence(e), entitySeq);

                        doPathRelatedEffect(BeyonderData.getPathway(entity), level, entity, e);
                    }
        });

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
            scaleAttribute.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mythical_creature_form"), 1.9, AttributeModifier.Operation.ADD_VALUE));
        }

        BeyonderData.addModifier(entity, "mythical_creature_form", getAmplifier(BeyonderData.getSequence(entity)));

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.MYTHICAL_CREATURE, entity);
        String additionalData = BeyonderData.getPathway(entity);
        if(additionalData.equals("door") && BeyonderData.getSequence(entity) <= 2) {
            additionalData = "door_high";
        }
        transformationComponent.setAdditionalDataAndSync(additionalData, entity);

        if(additionalData.equals("visionary")){
            if(entity instanceof Player player) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.getAbilities().setFlyingSpeed(.5f);
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AttributeInstance scaleAttribute = entity.getAttribute(Attributes.SCALE);
        if(scaleAttribute != null) {
            scaleAttribute.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mythical_creature_form"));
        }

        BeyonderData.removeModifier(entity, "mythical_creature_form");

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.MYTHICAL_CREATURE.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

        if(BeyonderData.getPathway(entity).equals("visionary")){
            if(entity instanceof Player player) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(.05f);
                player.onUpdateAbilities();
            }
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
                    LightningEntity lightning = new LightningEntity(level, entity, e.position(), 50, 6, DamageLookup.lookupDamage(4, .7) * (int) Math.max(multiplier(entity)/4,1), false, 4, 200, 0x11A8DD);
                    level.addFreshEntity(lightning);
                }
                break;

            case "visionary":


                break;

            default:
                break;
        }
    }

    private float getAmount(int seq){
        return switch (seq){
          case 4 -> 0.04168f;
          case 3 -> 0.05168f;
          case 2 -> 0.07168f;
          case 1 -> 0.08168f;
          case 0 -> 0.092f;
          default -> 0f;
        };
    }

    private float getAmplifier(int seq){
        return switch (seq){
            case 4 -> 1.1f;
            case 3 -> 1.15f;
            case 2 -> 1.2f;
            case 1 -> 1.25f;
            case 0 -> 1.35f;
            default -> 1f;
        };
    }
}
