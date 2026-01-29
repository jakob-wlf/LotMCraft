package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class HorrorAuraAbility extends Ability {
    public HorrorAuraAbility(String id) {
        super(id, 30);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 1000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Location loc = new Location(entity.position(), serverLevel);
        UUID effectID = MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.HORROR_AURA, loc, 20 * 25, false, serverLevel);

        AtomicInteger ticks = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 30, () -> {
            loc.setPosition(entity.position());
            loc.setLevel(serverLevel);
            MovableEffectManager.updateEffectPosition(effectID, loc, serverLevel);

            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 20, entity.position(), new MobEffectInstance(MobEffects.DARKNESS, 30, 5, false, false, false),
                    new MobEffectInstance(MobEffects.BLINDNESS, 20, 4, false, false, false),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));

            AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20).forEach(e -> {
                BeyonderData.addModifier(e, "horror_aura", .4);
                if(AbilityUtil.isTargetSignificantlyWeaker(entity, e) && ticks.get() % 10 == 0) {
                    e.hurt(e.damageSources().mobAttack(entity), (float) (DamageLookup.lookupDps(3, .95, 10, 20) * multiplier(entity)));
                }

                SanityComponent sanityComponent = e.getData(ModAttachments.SANITY_COMPONENT);
                sanityComponent.increaseSanityAndSync(-.0033f, e);
            });
            ticks.getAndIncrement();
        });
    }
}
