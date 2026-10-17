package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.CancelPhotonEntityEffectPacket;
import de.jakob.lotm.network.packets.toClient.PlayPhotonEntityEffectPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SunHaloAbility extends ToggleAbility {
    private static final HashMap<UUID, UUID> activeEffects = new HashMap<>(); // first ios the entity uuid, second the effect uuids

    public SunHaloAbility(String id) {
        super(id, "morale_boost");
        canBeCopied = false;
        canBeReplicated = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(40f, 35f, 30f, 25f, 20f, 15f, 10f, 5f));
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
        if(level.isClientSide)
            return;

        UUID effectUUID = UUID.randomUUID();
        activeEffects.put(entity.getUUID(), effectUUID);
        PacketHandler.sendToAllPlayersInSameLevel(
                new PlayPhotonEntityEffectPacket("sun_halo", entity.getId(), 0, .3, 0, .5, null,true, false, effectUUID, false),
                (ServerLevel) level
        );
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false, false));

        AbilityUtil.getNearbyEntities(null, (ServerLevel) level, entity.getEyePosition(), 20).forEach(e -> {
            if(AllyUtil.areAllies(entity, e) && entity != e) {
                e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false, false));
            }
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        UUID effectUUID = activeEffects.remove(entity.getUUID());
        PacketHandler.sendToAllPlayersInSameLevel(
                new CancelPhotonEntityEffectPacket(effectUUID),
                (ServerLevel) level
        );
    }
}
