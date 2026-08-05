package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveLuckAccumulationAbility;
import de.jakob.lotm.entity.custom.ability_entities.wheel_of_fortune_pathway.MisfortuneWordsEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class WordsOfMisfortuneAbility extends Ability {
    private static final int luckCost = 300;

    public WordsOfMisfortuneAbility(String id) {
        super(id, 4);

        canBeUsedByNPC = false;
        canBeShared = false;
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
    public int luckCost() {
        return luckCost;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        MisfortuneWordsEntity previousWordsEntity = AbilityUtil.getAllNearbyEntities(entity, (ServerLevel) level, entity.position(), 15)
                .stream()
                .filter(e -> e instanceof MisfortuneWordsEntity)
                .map(e -> (MisfortuneWordsEntity) e).findFirst().orElse(null);

        if(previousWordsEntity != null) {
            previousWordsEntity.discard();
            return;
        }

        if (!PassiveLuckAccumulationAbility.consumeStoredLuck(entity, luckCost)) {
            AbilityUtil.sendActionBar(entity, Component.literal("\u00A7cWords of Misfortune requires more luck."));
            return;
        }

        MisfortuneWordsEntity wordsEntity = new MisfortuneWordsEntity(level, entity.position().add(0, 1, 0));
        wordsEntity.setCasterUUID(entity.getUUID());
        level.addFreshEntity(wordsEntity);
    }
}
