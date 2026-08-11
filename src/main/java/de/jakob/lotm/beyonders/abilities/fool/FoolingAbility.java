package de.jakob.lotm.beyonders.abilities.fool;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.FoolingComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;


public class FoolingAbility extends Ability {

    public FoolingAbility(String id) {
        super(id, 5f);
        hasOptimalDistance = false;
        canBeShared = false;

    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 0));
    }

    @Override
    public float getSpiritualityCost() {
        return 52500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        double radius = 50;
        int duration = 20 * 5;
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        serverLevel.playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.AMETHYST_CLUSTER_BREAK,
                entity.getSoundSource(), 1.5f, 0.6f);

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(
                entity,
                serverLevel,
                entity.position(),
                radius
        );

        for (LivingEntity target : nearby) {
            FoolingComponent component = target.getData(ModAttachments.FOOLING_COMPONENT);

            component.setTicksRemaining(duration);
        }

        if(entity instanceof ServerPlayer player)
            EffectManager.playEffect(EffectManager.Effect.FOOLING, entity.getX(), entity.getY(), entity.getZ(), player);


        int particleCount = 80;
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI / particleCount) * i;
            double px = entity.getX() + radius * Math.cos(angle);
            double pz = entity.getZ() + radius * Math.sin(angle);
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    px, entity.getY() + 1.0, pz,
                    3, 0, 0.5, 0, 0.05
            );
        }
    }
}
