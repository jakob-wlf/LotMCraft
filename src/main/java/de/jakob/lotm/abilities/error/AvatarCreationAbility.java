package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AvatarCreationAbility extends AbilityItem {
    public AvatarCreationAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) {
            return;
        }

        if(!BeyonderData.isBeyonder(entity)) {
            return;
        }

        int sequence = BeyonderData.getSequence(entity) + 1;
        AvatarEntity avatar = new AvatarEntity(ModEntities.ERROR_AVATAR.get(), level, entity.getUUID(), "error", sequence);
        avatar.setPos(entity.getX(), entity.getY(), entity.getZ());
        level.addFreshEntity(avatar);
    }
}
