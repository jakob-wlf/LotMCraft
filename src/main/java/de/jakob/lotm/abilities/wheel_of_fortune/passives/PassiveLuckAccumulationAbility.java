package de.jakob.lotm.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.attachments.LuckAccumulationComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PassiveLuckAccumulationAbility extends PassiveAbilityItem {


    public PassiveLuckAccumulationAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 5));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        LuckAccumulationComponent component = entity.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT.get());
        component.setTicksAccumulated(component.getTicksAccumulated() + 5);

        System.out.println(component.getTicksAccumulated());
    }
}
