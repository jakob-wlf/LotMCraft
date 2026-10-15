package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HolyOathAbility extends ToggleAbility {
    public HolyOathAbility(String id) {
        super(id, "morale_boost");

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(40f, 35f, 30f, 15f, 11.5f, 8.5f, 6.5f, 5f));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
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

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if (BeyonderData.getSpirituality(entity) <= 0) {
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.literal("Your spirituality is exhausted.").withColor(0xFF422a2a)
                ));
            }

            stop(level, entity);

            return;
        }

        ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, entity.getEyePosition().subtract(0, entity.getEyeHeight() / 2, 0), 3, .3, .6, .3, 0);
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 6, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 1, false, false, false));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }
}
