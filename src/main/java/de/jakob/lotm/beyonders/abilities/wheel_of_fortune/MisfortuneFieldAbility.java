package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class MisfortuneFieldAbility extends Ability {
    public MisfortuneFieldAbility(String id) {
        super(id, 30);
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.MISFORTUNE_FIELD, entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        Vec3 startPos = entity.position();
        float multiplier = multiplier(entity);
        int amplifier = Math.min(Math.round(multiplier * 6.25f ) * 80, 6500);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 20, () -> {
            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 20*multiplier).forEach(e -> {
                LuckComponent luckComponent = e.getData(ModAttachments.LUCK_COMPONENT.get());
                luckComponent.addLuckWithMax(amplifier, -amplifier);
            });
        });
    }
}
