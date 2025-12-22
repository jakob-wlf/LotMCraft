package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.rendering.effectRendering.impl.HorrorAuraEffect;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HorrorAuraAbility extends AbilityItem {
    public HorrorAuraAbility(Properties properties) {
        super(properties, 30);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Location loc = new Location(entity.position(), serverLevel);
        UUID effectID = MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.HORROR_AURA, loc, 20 * 20, false, serverLevel);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 30, () -> {
            loc.setPosition(entity.position());
            loc.setLevel(serverLevel);
            MovableEffectManager.updateEffectPosition(effectID, loc, serverLevel);
        });
    }
}
