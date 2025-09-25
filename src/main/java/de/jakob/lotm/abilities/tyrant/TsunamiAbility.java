package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.custom.TsunamiEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class TsunamiAbility extends AbilityItem {
    public TsunamiAbility(Properties properties) {
        super(properties, 6);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        Vec3 position = VectorUtil.getRelativePosition(entity.position(), entity.getLookAngle().normalize(), 10, random.nextDouble(-11, 11), 0);
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 40, 2);
        Vec3 direction = targetPos.subtract(position).normalize();

        level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_SPLASH, entity.getSoundSource(), 5, 1.0f);

        TsunamiEntity tsunami = new TsunamiEntity(level, position, direction, (float) (40f * multiplier(entity)), BeyonderData.isGriefingEnabled(entity), entity);
        level.addFreshEntity(tsunami);
    }
}
