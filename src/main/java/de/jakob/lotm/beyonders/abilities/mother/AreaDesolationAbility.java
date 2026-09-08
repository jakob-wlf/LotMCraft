package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.custom.ability_entities.mother_pathway.BloomingAreaEntity;
import de.jakob.lotm.entity.custom.ability_entities.mother_pathway.DesolateAreaEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AreaDesolationAbility extends Ability {
    public AreaDesolationAbility(String id) {
        super(id, 5);
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(4, 8, 14));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(7500f, 4000f, 3400f));

        baseDamage = 1;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1400;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        DesolateAreaEntity previousEntity = AbilityUtil.getAllNearbyEntities(entity, (ServerLevel) level, entity.position(), 30)
                .stream()
                .filter(e -> e instanceof DesolateAreaEntity)
                .map(e -> (DesolateAreaEntity) e).findFirst().orElse(null);

        BloomingAreaEntity previousEntityBlooming = AbilityUtil.getAllNearbyEntities(entity, (ServerLevel) level, entity.position(), 30)
                .stream()
                .filter(e -> e instanceof BloomingAreaEntity)
                .map(e -> (BloomingAreaEntity) e).findFirst().orElse(null);

        if(previousEntity != null) {
            previousEntity.discard();
            return;
        }

        if(previousEntityBlooming != null){
            previousEntityBlooming.discard();
            return;
        }

        DesolateAreaEntity wordsEntity = new DesolateAreaEntity(
                level,
                entity.position().add(0, 1, 0),
                baseDamage,
                entity
        );
        level.addFreshEntity(wordsEntity);
    }
}
