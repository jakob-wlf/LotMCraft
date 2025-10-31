package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.WarBannerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class EssenceOfWarAbility extends AbilityItem {
    public EssenceOfWarAbility(Properties properties) {
        super(properties, 180);
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
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        WarBannerEntity banner = new WarBannerEntity(ModEntities.WAR_BANNER.get(), level, 20 * 90, entity.getUUID());
        banner.setPos(entity.getX(), entity.getY() + .75, entity.getZ());
        level.addFreshEntity(banner);

        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);
    }
}
