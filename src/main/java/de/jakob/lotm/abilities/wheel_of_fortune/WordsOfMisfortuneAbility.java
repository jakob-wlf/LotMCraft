package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.custom.MisfortuneWordsEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class WordsOfMisfortuneAbility extends Ability {
    public WordsOfMisfortuneAbility(String id) {
        super(id, 4);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        MisfortuneWordsEntity previousWordsEntity = AbilityUtil.getAllNearbyEntities(entity, (ServerLevel) level, entity.position(), 3)
                .stream()
                .filter(e -> e instanceof MisfortuneWordsEntity)
                .map(e -> (MisfortuneWordsEntity) e).findFirst().orElse(null);

        if(previousWordsEntity != null) {
            previousWordsEntity.discard();
            return;
        }

        MisfortuneWordsEntity wordsEntity = new MisfortuneWordsEntity(level, entity.position().add(0, 1, 0));
        level.addFreshEntity(wordsEntity);
    }
}
