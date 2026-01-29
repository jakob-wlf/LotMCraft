package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.entity.custom.CoffinEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class MaternalEmbraceAbility extends Ability {
    public MaternalEmbraceAbility(String id) {
        super(id, 20);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 1600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(targetEntity == null) {
            CoffinEntity coffinEntity = new CoffinEntity(serverLevel, AbilityUtil.getTargetLocation(entity, 20, 2).add(0, 1, 0));
            serverLevel.addFreshEntity(coffinEntity);
            return;
        }

        CoffinEntity coffinEntity = new CoffinEntity(serverLevel, targetEntity.position().add(0, 1, 0));
        serverLevel.addFreshEntity(coffinEntity);

        if(AbilityUtil.isTargetSignificantlyStronger(entity, targetEntity) || (
                BeyonderData.isBeyonder(entity) && BeyonderData.isBeyonder(targetEntity) && BeyonderData.getSequence(targetEntity) <= BeyonderData.getSequence(entity))) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.maternal_embrace.too_strong").withColor(0x8abd93));
            coffinEntity.discard();
            return;
        }

        TransformationComponent component = targetEntity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        component.setTransformedAndSync(true, targetEntity);
        component.setTransformationIndexAndSync(TransformationComponent.TransformationType.COFFIN, targetEntity);

        BeyonderData.disableAbilityUse(targetEntity, "maternal_embrace");
        targetEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 30, 20, false, false, false));
        targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 30, 20, false, false, false));
        targetEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 30, 20, false, false, false));

        ServerScheduler.scheduleForDuration(0, 1, 20 * 30, () -> {
            if(!targetEntity.isAlive()) {
                return;
            }
            targetEntity.setPos(coffinEntity.getX(), coffinEntity.getY(), coffinEntity.getZ());
        });

        ServerScheduler.scheduleDelayed(20 * 30, () -> {
            component.setTransformedAndSync(false, targetEntity);
            component.setTransformationIndexAndSync(0, targetEntity);
            coffinEntity.discard();
        });
    }
}
