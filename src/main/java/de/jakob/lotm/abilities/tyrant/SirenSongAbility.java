package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.data.LocationSupplier;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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

    }

    private void buffSong(ServerLevel level, LivingEntity entity) {

    }

    private void deathMelody(ServerLevel level, LivingEntity entity) {
        LocationSupplier supplier = new LocationSupplier(entity.position());
        ParticleUtil.createExpandingParticleSpirals(level, ModParticles.BLACK_NOTE.get(), supplier, 1, 11, 4, .35, 5, 20 * 30, 40, 10);

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.DEATH_MELODY.get(), SoundSource.BLOCKS, 1, 1);

        ServerScheduler.scheduleForDuration(0,  2, 20 * 30, () -> supplier.setPos(entity.position()), level);
        ServerScheduler.scheduleForDuration(0,  18, 20 * 30, () -> AbilityUtil.damageNearbyEntities(level, entity, 25, 2.5 * multiplier(entity), entity.position(), true, false, true, 0), level);
    }
}
