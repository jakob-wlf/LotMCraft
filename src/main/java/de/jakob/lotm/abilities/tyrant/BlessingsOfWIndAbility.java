package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.WindBladeEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlessingsOfWIndAbility extends SelectableAbility {
    public BlessingsOfWIndAbility(String id) {
        super(id, 8);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 100;
    }

    @Override
    public String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.blessings_of_wind.speed_of_wind",
                "ability.lotmcraft.blessings_of_wind.penetrative_wind",
                "ability.lotmcraft.blessings_of_wind.air_cushion",
                "ability.lotmcraft.blessings_of_wind.glide"
        };
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide) return;
        if(!(entity instanceof Player) && abilityIndex == 3) {
            abilityIndex = 0;
        }

        level.playSound(null, entity.blockPosition(), SoundEvents.BREEZE_SHOOT, entity.getSoundSource(), 1, 1);

        switch (abilityIndex) {
            case 0 -> speedOfWind(level, entity);
            case 1 -> penetariveWind(level, entity);
            case 2 -> airCushion(level, entity);
            case 3 -> glide(level, entity);
        }
    }

    private void glide(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 8, 1, false, false, false));

        ServerScheduler.scheduleForDuration(0, 1, 20 * 10, () -> {
            if (!entity.onGround()) {
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, entity.position().add(0, .5, 0), 5, .5, .2);
            }
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void airCushion(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.RESISTANCE, "blessings_of_wind_resistance", 2);
        UUID entityId = entity.getUUID();

        ServerScheduler.scheduleForDuration(0, 1, 20 * 8, () -> {
            LivingEntity target = (LivingEntity) ((ServerLevel) level).getEntity(entityId);
            if (target == null) return;

            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.CLOUD, entity.position().add(0, .7, 0), .65f, 10);
        }, () -> {
            LivingEntity target = (LivingEntity) ((ServerLevel) level).getEntity(entityId);
            if (target == null) return;

            PhysicalEnhancementsAbility.removeEnhancementBoost(target, "blessings_of_wind_resistance");
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void penetariveWind(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        BeyonderData.addModifier(entity, "blessings_of_wind_strength", 1.3f);
        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.STRENGTH, "blessings_of_wind", 2);
        UUID entityId = entity.getUUID();

        ServerScheduler.scheduleForDuration(0, 1, 20 * 8, () -> {
            LivingEntity target = (LivingEntity) ((ServerLevel) level).getEntity(entityId);
            if (target == null) return;

            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, entity.getEyePosition().subtract(0, .5, 0), 5, .5, .13);
        }, () -> {
            LivingEntity target = (LivingEntity) ((ServerLevel) level).getEntity(entityId);
            if (target == null) return;

            PhysicalEnhancementsAbility.removeEnhancementBoost(target, "blessings_of_wind_strength");
            BeyonderData.removeModifier(target, "blessings_of_wind_strength");
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void speedOfWind(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.SPEED, "blessings_of_wind", 6);
        UUID entityId = entity.getUUID();

        ServerScheduler.scheduleForDuration(0, 1, 20 * 8, () -> {
            LivingEntity target = (LivingEntity) ((ServerLevel) level).getEntity(entityId);
            if (target == null) return;

            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, entity.position(), 5, .5, .2);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.GUST, entity.position(), 1, .2, .2);
        }, () -> {
            LivingEntity target = (LivingEntity) ((ServerLevel) level).getEntity(entityId);
            if (target == null) return;

            PhysicalEnhancementsAbility.removeEnhancementBoost(target, "blessings_of_wind");
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));


    }
}
