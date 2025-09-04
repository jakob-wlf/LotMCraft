package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.data.LocationSupplier;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SirenSongAbility extends SelectableAbilityItem {
    public SirenSongAbility(Properties properties) {
        super(properties, 45);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 60;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.siren_song.death_melody", "ability.lotmcraft.siren_song.strengthening_melody", "ability.lotmcraft.siren_song.dazing_song"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;

        switch (abilityIndex) {
            case 0 -> deathMelody((ServerLevel) level, entity);
            case 1 -> buffSong((ServerLevel) level, entity);
            case 2 -> dazingSong((ServerLevel) level, entity);
        }
    }

    private void dazingSong(ServerLevel level, LivingEntity entity) {
        Location supplier = new Location(entity.getEyePosition().add(0, .1, 0), level);
        ParticleUtil.createExpandingParticleSpirals(ParticleTypes.NOTE, supplier, 1, 10, 2, .5, 5, 20 * 30, 20, 5);

        //level.playSound(null, BlockPos.containing(entity.position()), ModSounds.DEATH_MELODY.get(), SoundSource.BLOCKS, 1, 1);

        ServerScheduler.scheduleForDuration(0,  2, 20 * 30, () -> {
            if(entity.level().isClientSide)
                return;
            supplier.setPosition(entity.position());
            supplier.setLevel(entity.level());
        }, level);
        ServerScheduler.scheduleForDuration(0,  18, 20 * 30, () -> {
            if(entity.level().isClientSide)
                return;
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 25, entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3, false, false, false), new MobEffectInstance(MobEffects.WEAKNESS, 20, 3, false, false, false), new MobEffectInstance(MobEffects.BLINDNESS, 20, 3, false, false, false));
        }, level);
    }

    private void buffSong(ServerLevel level, LivingEntity entity) {
        Location loc = new Location(entity.getEyePosition().add(0, .1, 0), level);
        ParticleUtil.createParticleSpirals(ModParticles.GOLDEN_NOTE.get(), loc, 3, 3, 4, .35, 5, 20 * 30, 15, 8);

        BeyonderData.addModifier(entity, "buff_song", 1.5);

        //level.playSound(null, BlockPos.containing(entity.position()), ModSounds.DEATH_MELODY.get(), SoundSource.BLOCKS, 1, 1);

        MobEffectInstance strength = entity.getEffect(MobEffects.DAMAGE_BOOST);
        MobEffectInstance speed = entity.getEffect(MobEffects.MOVEMENT_SPEED);

        int strengthLevel = strength == null ? 1 : strength.getAmplifier() + 1;
        int speedLevel = speed == null ? 1 : speed.getAmplifier() + 1;

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 30, strengthLevel, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, speedLevel, false, false, false));

        ServerScheduler.scheduleForDuration(0,  2, 20 * 30, () -> {
            if(entity.level().isClientSide)
                return;
            loc.setPosition(entity.position());
            loc.setLevel(entity.level());
        }, level);
        ServerScheduler.scheduleDelayed(20 * 30, () -> BeyonderData.removeModifier(entity, "buff_song"));
    }

    private void deathMelody(ServerLevel level, LivingEntity entity) {
        Location supplier = new Location(entity.position(), level);
        ParticleUtil.createExpandingParticleSpirals(ModParticles.BLACK_NOTE.get(), supplier, 1, 11, 4, .35, 5, 20 * 30, 40, 10);

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.DEATH_MELODY.get(), SoundSource.BLOCKS, 1, 1);

        ServerScheduler.scheduleForDuration(0,  2, 20 * 30, () -> {
            if(entity.level().isClientSide)
                return;
            supplier.setPosition(entity.position());
            supplier.setLevel(entity.level());
        }, level);
        ServerScheduler.scheduleForDuration(0,  18, 20 * 30, () -> {
            if(entity.level().isClientSide)
                return;
            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 25, 2.15 * multiplier(entity), entity.position(), true, false, true, 0);
        }, level);
    }
}
