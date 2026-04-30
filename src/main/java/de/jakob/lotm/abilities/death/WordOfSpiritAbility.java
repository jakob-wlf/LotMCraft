package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class WordOfSpiritAbility extends Ability {

    private static final int DURATION_TICKS = 20 * 10; // 10 seconds

    public WordOfSpiritAbility(String id) {
        super(id, 45f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), level), "purification", BeyonderData.getSequence(entity), -1)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25*(int) Math.max(multiplier(entity)/4,1), 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.word_of_spirit.no_target").withColor(0xFF334f23));
            return;
        }

        int casterSeq = BeyonderData.getSequence(entity);
        int targetSeq = BeyonderData.getSequence(target);
        if (targetSeq - casterSeq <= -1) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.word_of_spirit.too_strong").withColor(0xFF334f23));
            return;
        }

        target.addEffect(new MobEffectInstance(ModEffects.SPIRIT_CALLED, DURATION_TICKS, 0, false, true, true));
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SOUL, target.getEyePosition(), 60, 0.5, 0.5, 0.5, 0.1);
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.word_of_spirit.applied").withColor(0xFF334f23));
    }
}
