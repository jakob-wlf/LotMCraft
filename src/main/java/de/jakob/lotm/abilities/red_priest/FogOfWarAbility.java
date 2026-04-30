package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FogOfWarAbility extends ToggleAbility {
    public FogOfWarAbility(String id) {
        super(id, "fog");
        canBeCopied = false;
        interactionRadius = 20;
        canBeShared = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return 80;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 3));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;
        BeyonderData.reduceSpirituality(entity, 15);

        if (BeyonderData.getSpirituality(entity) <= 0) {
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetActionBarTextPacket(Component.literal("Your spirituality is exhausted.").withColor(0xFF422a2a)));
            }

            stop(level, entity);


            return;
        }

        // Fog of War is weakened by light_source interactions
        Location fogLoc = new Location(entity.getEyePosition(), level);
        int seq = AbilityUtil.getSeqWithArt(entity, this);
        boolean lightNearby = InteractionHandler.isInteractionPossible(fogLoc, "light_source", seq);

        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.FOG_OF_WAR.get(), entity.getEyePosition(), 20, 15*(int) Math.max(multiplier(entity)/4,1), 5*(int) Math.max(multiplier(entity)/4,1), 15*(int) Math.max(multiplier(entity)/4,1), 0);
        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), 20*(int) Math.max(multiplier(entity)/4,1)).forEach(e -> {
            if (!AllyUtil.isAlly(entity, e.getUUID())) {
                if (!lightNearby) {
                    e.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0, false, false, true));
                }
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, lightNearby ? 0 : 2, false, false, true));
                BeyonderData.addModifier(e, "fog_of_war", lightNearby ? .85 : .7);
                ServerScheduler.scheduleDelayed(20, () -> {
                    if (e.distanceTo(entity) > 20) {
                        BeyonderData.removeModifier(e, "fog_of_war");
                    }
                });
            }
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
    }
}