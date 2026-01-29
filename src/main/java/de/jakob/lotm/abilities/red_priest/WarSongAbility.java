package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class WarSongAbility extends Ability {
    public WarSongAbility(String id) {
        super(id, 40);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Location loc = new Location(entity.getEyePosition().add(0, .1, 0), level);
        ParticleUtil.createParticleSpirals(ModParticles.BLACK_NOTE.get(), loc, 3, 3, 4, .35, 5, 20 * 30, 15, 8);

        BeyonderData.addModifier(entity, "buff_song", 1.5);

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.SONG_OF_COURAGE.get(), SoundSource.BLOCKS, 1, 1);

        MobEffectInstance strength = entity.getEffect(MobEffects.DAMAGE_BOOST);
        MobEffectInstance speed = entity.getEffect(MobEffects.MOVEMENT_SPEED);

        int strengthLevel = strength == null ? 1 : strength.getAmplifier() + 2;
        int speedLevel = speed == null ? 1 : speed.getAmplifier() + 2;
        BeyonderData.addModifier(entity, "war_song", 1.5f);

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 30, strengthLevel, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, speedLevel, false, false, false));

        ServerScheduler.scheduleForDuration(0,  2, 20 * 30, () -> {
            if(entity.level().isClientSide)
                return;
            loc.setPosition(entity.position());
            loc.setLevel(entity.level());
        }, (ServerLevel) level);
        ServerScheduler.scheduleDelayed(20 * 30, () -> BeyonderData.removeModifier(entity, "war_song"));
    }
}
