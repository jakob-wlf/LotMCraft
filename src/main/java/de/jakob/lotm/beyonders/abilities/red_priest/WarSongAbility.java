package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WarSongAbility extends Ability {
    public WarSongAbility(String id) {
        super(id, 70, "morale_boost");
        interactionRadius = 20;
        interactionCacheTicks = 20 * 30;
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(30, 40, 45, 60));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(3000f, 1100f, 800f, 570f));
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

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.SONG_OF_COURAGE.get(), SoundSource.BLOCKS, 1, 1);

        MobEffectInstance speed = entity.getEffect(MobEffects.MOVEMENT_SPEED);

        int speedLevel = speed == null ? 1 : speed.getAmplifier() + 2;
        BeyonderData.addModifierWithTimeLimit(entity, "war_song", 1.1, 20 * 30);

        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) (20 * 30), speedLevel, false, false, false));

        ServerScheduler.scheduleForDuration(0,  2, 20 * 30, () -> {
            if(entity.level().isClientSide)
                return;
            loc.setPosition(entity.position());
            loc.setLevel(entity.level());
        }, (ServerLevel) level);
    }
}
