package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PassiveLuckAbility extends PassiveAbility {


    public PassiveLuckAbility(String id) {
        super(id);
    }
    public static int getNormalLuckForEntity(LivingEntity livingEntity) {
        if(!(PassiveAbilityHandler.getById("passive_luck_ability") instanceof PassiveLuckAbility instance)) {
            return 0;
        }

        if(!instance.shouldApplyTo(livingEntity)) {
            return 0;
        }

        int sequence = BeyonderData.getSequence(livingEntity);
        if (sequence < 0 || sequence > 9) {
            return 0;
        }

        return instance.getLuckLevelForSequence(sequence);
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "wheel_of_fortune", 7
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

}