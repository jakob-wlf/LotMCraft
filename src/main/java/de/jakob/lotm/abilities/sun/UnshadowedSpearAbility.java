package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.custom.FlamingSpearProjectileEntity;
import de.jakob.lotm.entity.custom.UnshadowedSpearProjectileEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class UnshadowedSpearAbility extends AbilityItem {
    public UnshadowedSpearAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 150;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 50, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1.0f, 1.0f);

        UnshadowedSpearProjectileEntity spear = new UnshadowedSpearProjectileEntity(level, entity, 39 * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        spear.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        spear.shoot(direction.x, direction.y, direction.z, 1.6f, 0);
        level.addFreshEntity(spear);
    }
}
