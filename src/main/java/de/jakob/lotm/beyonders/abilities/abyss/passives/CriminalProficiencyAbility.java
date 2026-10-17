package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class CriminalProficiencyAbility extends PassiveAbility {
    public CriminalProficiencyAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 9));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        ItemStack weapon = entity.getMainHandItem();
        Item item = weapon.getItem();
        if (item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof BowItem || item instanceof CrossbowItem || item instanceof MaceItem) {
            PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.STRENGTH, "criminal_proficiency", 1);
        }
    }
}
