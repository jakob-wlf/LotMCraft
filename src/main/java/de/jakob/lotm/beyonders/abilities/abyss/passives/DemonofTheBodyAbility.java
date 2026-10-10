package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DemonofTheBodyAbility extends PassiveAbility {

    public DemonofTheBodyAbility(String id) {super(id);}
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 4));}

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide)
            return;
        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.RESISTANCE, "demon_of_the_body", 5);
    }
}
