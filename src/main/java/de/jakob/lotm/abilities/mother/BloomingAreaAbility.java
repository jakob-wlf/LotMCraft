package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.custom.BloomingAreaEntity;
import de.jakob.lotm.entity.custom.MisfortuneWordsEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class BloomingAreaAbility extends AbilityItem {
    public BloomingAreaAbility(Properties properties) {
        super(properties, 5);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1400;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        BloomingAreaEntity previousEntity = AbilityUtil.getAllNearbyEntities(entity, (ServerLevel) level, entity.position(), 20)
                .stream()
                .filter(e -> e instanceof BloomingAreaEntity)
                .map(e -> (BloomingAreaEntity) e).findFirst().orElse(null);

        if(previousEntity != null) {
            previousEntity.discard();
            return;
        }

        BloomingAreaEntity wordsEntity = new BloomingAreaEntity(level, entity.position().add(0, 1, 0));
        level.addFreshEntity(wordsEntity);
    }
}
