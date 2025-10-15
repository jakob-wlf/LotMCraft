package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SteelMasteryAbility extends SelectableAbilityItem {
    private final HashSet<UUID> castingSteelSkin = new HashSet<>();

    public SteelMasteryAbility(Properties properties) {
        super(properties, 2);
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
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;
        switch (abilityIndex) {
            case 0 -> steelSkin((ServerLevel) level, entity);
            case 1 -> steelChains((ServerLevel) level, entity);
        }
    }

    private void steelChains(ServerLevel level, LivingEntity entity) {

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
