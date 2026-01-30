package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.custom.DesolateAreaEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AreaDesolationAbility extends Ability {
    public AreaDesolationAbility(String id) {
        super(id, 5);
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

        if(!BeyonderData.isGriefingEnabled(entity)) {
            if(entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("lotm.griefing_required").withColor(0xed716b), false);
            }
            return;
        }

        DesolateAreaEntity previousEntity = AbilityUtil.getAllNearbyEntities(entity, (ServerLevel) level, entity.position(), 30)
                .stream()
                .filter(e -> e instanceof DesolateAreaEntity)
                .map(e -> (DesolateAreaEntity) e).findFirst().orElse(null);

        if(previousEntity != null) {
            previousEntity.discard();
            return;
        }

        DesolateAreaEntity wordsEntity = new DesolateAreaEntity(level, entity.position().add(0, 1, 0));
        level.addFreshEntity(wordsEntity);
    }
}
