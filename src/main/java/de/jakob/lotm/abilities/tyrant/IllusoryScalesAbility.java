package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.AbilityHandler;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.tyrant.passives.PhysicalEnhancementsTyrantAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class IllusoryScalesAbility extends ToggleAbility {
    public IllusoryScalesAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return 1;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 9));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.RESISTANCE, "illusory_scales", 5);
    }

    private final DustParticleOptions blueDust = new DustParticleOptions(new Vector3f(87 / 255f, 212 / 255f, 183 / 255f), 1.75f);

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ParticleUtil.spawnParticles((ServerLevel) level, blueDust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 12, .4, entity.getEyeHeight() / 2, .4, 0);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        PhysicalEnhancementsAbility.removeEnhancementBoost(entity, "illusory_scales");
    }
}
