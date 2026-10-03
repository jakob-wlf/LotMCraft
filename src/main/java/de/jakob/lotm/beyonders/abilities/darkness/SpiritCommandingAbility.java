package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SpiritCommandingAbility extends Ability {
    public SpiritCommandingAbility(String id) {
        super(id, 1);

        this.canBeUsedInArtifact = false; //as long as it is not implemented
        this.cannotBeStolen = true; //as long as it is not implemented
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 400;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        AbilityUtil.sendActionBar(entity, Component.translatable("lotm.not_implemented_yet").withColor(0xff0000));
    }
}
