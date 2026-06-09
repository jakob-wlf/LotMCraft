package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SirenSongAbility extends SelectableAbility {
    public SirenSongAbility(String id) {
        super(id, 45);
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 180;
    }

    @Override
    public String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.siren_song.death_melody",
                "ability.lotmcraft.siren_song.strengthening_melody",
                "ability.lotmcraft.siren_song.dazing_song"};
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
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
        ParticleUtil.createExpandingParticleSpirals((ParticleOptions) ParticleTypes.NOTE, supplier, 1.0, 10.0, 2.0, .5, 5.0, (int) (20 * 20* multiplier(entity)), 20, 5);

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.DAZING_SONG.get(), SoundSource.BLOCKS, 1, 1);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        final UUID[] posTrackerHolder = new UUID[1];
        posTrackerHolder[0] = ServerScheduler.scheduleForDuration(0,  2, (int) (20 * 20* multiplier(entity)), () -> {
            if(entity.level().isClientSide)
                return;
            supplier.setPosition(entity.position());
            supplier.setLevel(entity.level());
        }, level);
        final UUID[] effectHolder = new UUID[1];
        effectHolder[0] = ServerScheduler.scheduleForDuration(0,  18, (int) (20 * 20* multiplier(entity)), () -> {
            if(entity.level().isClientSide)
                return;

            if(InteractionHandler.isInteractionPossible(new Location(entity.position(), entity.level()), "explosion", entitySeq)) {
                if(posTrackerHolder[0] != null) ServerScheduler.cancel(posTrackerHolder[0]);
                if(effectHolder[0] != null) ServerScheduler.cancel(effectHolder[0]);
                return;
            }

            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 25, entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3, false, false, false), new MobEffectInstance(MobEffects.WEAKNESS, 20, 3, false, false, false), new MobEffectInstance(MobEffects.BLINDNESS, 20, 3, false, false, false));
        }, level);
    }

    private void buffSong(ServerLevel level, LivingEntity entity) {
        Location loc = new Location(entity.getEyePosition().add(0, .1, 0), level);
        ParticleUtil.createParticleSpirals((ParticleOptions) ModParticles.GOLDEN_NOTE.get(), loc, 3.0, 3, 4, .35, 5, 20 * 20* (int) multiplier(entity), 15, 8);

        BeyonderData.addModifier(entity, "buff_song", 1.5);

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.SONG_OF_COURAGE.get(), SoundSource.BLOCKS, 1, 1);

        MobEffectInstance strength = entity.getEffect(MobEffects.DAMAGE_BOOST);
        MobEffectInstance speed = entity.getEffect(MobEffects.MOVEMENT_SPEED);

        int strengthLevel = strength == null ? 1 : strength.getAmplifier() + 1;
        int speedLevel = speed == null ? 1 : speed.getAmplifier() + 1;

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) (20 * 20* multiplier(entity)), strengthLevel, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) (20 * 20* multiplier(entity)), speedLevel, false, false, false));

        ServerScheduler.scheduleForDuration(0,  2, (int) (20 * 20* multiplier(entity)), () -> {
            if(entity.level().isClientSide)
                return;
            loc.setPosition(entity.position());
            loc.setLevel(entity.level());
        }, level);
        ServerScheduler.scheduleDelayed((int) (20 * 20* multiplier(entity)), () -> BeyonderData.removeModifier(entity, "buff_song"));
    }

    private void deathMelody(ServerLevel level, LivingEntity entity) {
        Location supplier = new Location(entity.position(), level);
        ParticleUtil.createExpandingParticleSpirals((ParticleOptions) ModParticles.BLACK_NOTE.get(), supplier, 1, 11, 4, .35, 5, 20 * 20* (int) multiplier(entity), 40, 10);

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.DEATH_MELODY.get(), SoundSource.BLOCKS, 1, 1);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        final UUID[] posTrackerHolder = new UUID[1];
        posTrackerHolder[0] = ServerScheduler.scheduleForDuration(0,  2, (int) (20 * 20* multiplier(entity)), () -> {
            if(entity.level().isClientSide)
                return;
            supplier.setPosition(entity.position());
            supplier.setLevel(entity.level());
        }, level);
        final UUID[] effectHolder = new UUID[1];
        effectHolder[0] = ServerScheduler.scheduleForDuration(0,  18, (int) (20 * 20* multiplier(entity)), () -> {
            if(entity.level().isClientSide)
                return;

            if(InteractionHandler.isInteractionPossible(new Location(entity.position(), entity.level()), "explosion", entitySeq)) {
                if(posTrackerHolder[0] != null) ServerScheduler.cancel(posTrackerHolder[0]);
                if(effectHolder[0] != null) ServerScheduler.cancel(effectHolder[0]);
                return;
            }

            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 25, DamageLookup.lookupDps(5,  .65, 18, 20) * multiplier(entity), entity.position(), true, false, true, 0);
        }, level);
    }
}
