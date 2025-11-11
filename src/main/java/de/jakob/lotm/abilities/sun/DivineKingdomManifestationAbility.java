package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.SunKingdomEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DivineKingdomManifestationAbility extends AbilityItem {
    public DivineKingdomManifestationAbility(Properties properties) {
        super(properties, 20 * 60 * 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2900;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SunKingdomEntity sunKingdomEntity = new SunKingdomEntity(ModEntities.SUN_KINGDOM.get(), level, 20 * 60 * 2, entity.getUUID(), BeyonderData.isGriefingEnabled(entity));
        sunKingdomEntity.setPos(entity.getX(), entity.getY() + .5, entity.getZ());
        serverLevel.addFreshEntity(sunKingdomEntity);
    }
}
