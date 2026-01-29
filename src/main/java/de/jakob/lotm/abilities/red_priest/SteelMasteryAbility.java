package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SteelMasteryAbility extends SelectableAbility {
    private final HashSet<UUID> castingSteelSkin = new HashSet<>();

    public SteelMasteryAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.steel_mastery.steel_skin", "ability.lotmcraft.steel_mastery.steel_chains"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;
        switch (abilityIndex) {
            case 0 -> steelSkin((ServerLevel) level, entity);
            case 1 -> steelChains((ServerLevel) level, entity);
        }
    }

    private void steelChains(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                Component actionBar = Component.translatable("ability.lotmcraft.steel_mastery.no_target").withColor(0xFF422a2a);
                sendActionBar(player, actionBar);
            }
            return;
        }

        ServerScheduler.scheduleForDuration(0, 5, 20 * 8, () -> {
            if(entity.isDeadOrDying())
                return;

            target.setDeltaMovement(new Vec3(0, 0, 0));
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 20, false, false, false));

            Vec3 entityPos = target.position().add(0, 1.25, 0);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(0, -1.5, 3), .35, 1);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(0, -1.5, -3), .35, 1);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(3, -1.5, 0), .35, 1);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(-3, -1.5, 0), .35, 1);

        });
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }


    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.3f, 0.3f, 0.3f), 2.25f);

    private void steelSkin(ServerLevel level, LivingEntity entity) {
        if(castingSteelSkin.contains(entity.getUUID())) {
            castingSteelSkin.remove(entity.getUUID());
            return;
        }

        castingSteelSkin.add(entity.getUUID());
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil(level, () -> {
            if(BeyonderData.getSpirituality(entity) <= 4) {
                castingSteelSkin.remove(entity.getUUID());
            }
            if(!castingSteelSkin.contains(entity.getUUID())) {
                shouldStop.set(true);
                return;
            }

            BeyonderData.reduceSpirituality(entity, 4);

            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 3, false, false, false));
            ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 10, .4, entity.getEyeHeight() / 2, .4, 0);
        }, 2, () -> castingSteelSkin.remove(entity.getUUID()), shouldStop);
    }

}
