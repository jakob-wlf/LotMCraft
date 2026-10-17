package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KnowledgeAbility extends ToggleAbility {

    public KnowledgeAbility(String id) {
        super(id);
        tickRate = 20;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(80f, 60f, 40f, 30f, 20f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.BOOK_PAGE_TURN, entity.getSoundSource(), 1f, 0.8f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.BOOK_PUT, entity.getSoundSource(), 1f, 0.8f);
    }

    public static boolean isActive(LivingEntity entity) {
        return ToggleAbility.getActiveAbilitiesForEntity(entity).stream().anyMatch(ability -> ability instanceof KnowledgeAbility);
    }
}
