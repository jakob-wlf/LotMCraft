package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.CycleOfFateEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.CycleOfFateHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class CycleOfFateAbility extends SelectableAbility {
    public CycleOfFateAbility(String id) {
        super(id, 1);
        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.cycle_of_fate.create_cycle", "ability.lotmcraft.cycle_of_fate.trigger_cycle"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        switch (selectedAbility) {
            case 0 -> CycleOfFateHelper.spawnCycleOfFateAtOwner(entity);
            case 1 -> {
                CycleOfFateEntity cycle = CycleOfFateHelper.findCycleOfFateForOwner(serverLevel, entity);
                if (cycle != null) {
                    cycle.trigger(entity);
                }
                else {
                    AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.cycle_of_fate.no_cycle").withColor(0xccebed));
                }
            }
        }
    }
}
