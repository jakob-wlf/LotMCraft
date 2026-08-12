package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.red_priest_pathway.WarBannerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EssenceOfWarAbility extends Ability {
    public EssenceOfWarAbility(String id) {
        super(id, 180);
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(15, 20, 25, 40));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(6000f, 2600f, 1600f, 1000f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        WarBannerEntity banner = new WarBannerEntity(ModEntities.WAR_BANNER.get(), level, 20 * 30, entity.getUUID());
        banner.setPos(entity.getX(), entity.getY() + .75, entity.getZ());
        level.addFreshEntity(banner);

        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);
    }
}
