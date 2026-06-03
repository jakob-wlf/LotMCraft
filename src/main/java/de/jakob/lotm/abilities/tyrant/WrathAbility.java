package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WrathAbility extends Ability {
    public WrathAbility(String id) {
        super(id, 12);
        canBeUsedByNPC = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            if(entity instanceof Player player)
                player.playSound(SoundEvents.RAVAGER_ROAR, 1, 1);
            return;
        }

        entity.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(.2f, entity, BeyonderData.getSequence(entity), BeyonderData.getSequence(entity));
        BeyonderData.addModifier(entity, "folk_of_rage_wrath", 1.75f);
        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.STRENGTH, "folk_of_rage_wrath_strength", 3);
        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.SPEED, "folk_of_rage_wrath_speed", 2);
        UUID entityId = entity.getUUID();
        ServerScheduler.scheduleDelayed(20 * 10, () -> {
            LivingEntity target = (LivingEntity) ((ServerLevel) level).getEntity(entityId);
            if (target == null) return;

            PhysicalEnhancementsAbility.removeEnhancementBoost(target, "folk_of_rage_wrath_strength");
            PhysicalEnhancementsAbility.removeEnhancementBoost(target, "folk_of_rage_wrath_speed");
            BeyonderData.removeModifier(target, "folk_of_rage_wrath");
        }, (ServerLevel) level);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 90;
    }
}
