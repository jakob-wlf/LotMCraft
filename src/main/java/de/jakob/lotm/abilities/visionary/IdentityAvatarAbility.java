package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class IdentityAvatarAbility extends Ability {
    public IdentityAvatarAbility(String id) {
        super(id, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 1700;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) {
            return;
        }

        if(!BeyonderData.isBeyonder(entity)) {
            return;
        }

        int sequence = BeyonderData.getSequence(entity);
        AvatarEntity avatar = new AvatarEntity(ModEntities.ERROR_AVATAR.get(), level, entity.getUUID(), "visionary", sequence);
        avatar.setPos(entity.getX(), entity.getY(), entity.getZ());
        level.addFreshEntity(avatar);
    }
}
