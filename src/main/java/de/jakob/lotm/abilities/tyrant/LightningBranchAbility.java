package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.custom.LightningBranchEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class LightningBranchAbility extends AbilityItem {
    public LightningBranchAbility(Properties properties) {
        super(properties, 1.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 900;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 dir = entity.getLookAngle().normalize();
        Vec3 startPos = entity.position().add(dir).add(0, 1.5, 0);

        LightningBranchEntity branch = new LightningBranchEntity(level, entity, startPos, dir, 30, 40 * multiplier(entity));
        level.addFreshEntity(branch);
    }
}
