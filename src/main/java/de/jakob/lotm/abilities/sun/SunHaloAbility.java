package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class SunHaloAbility extends ToggleAbility {
    public SunHaloAbility(String id) {
        super(id, "morale_boost");
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return 7;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 7));
    }

    @Override
    public void start(Level level, LivingEntity entity) {

    }

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            2f
    );

    DustParticleOptions dustOptions2 = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            1f
    );

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ParticleUtil.spawnCircleParticles((ServerLevel) level, dustOptions2, entity.getEyePosition().add(0, .4, 0), .75,  20);
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0, false, false, false));

        AbilityUtil.getNearbyEntities(null, (ServerLevel) level, entity.getEyePosition(), 20).forEach(e -> {
            if(AllyUtil.areAllies(entity, e) && entity != e) {
                e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0, false, false, false));
                ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, e.getEyePosition().subtract(0, .5, 0), 10, 1);
            }
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }
}
