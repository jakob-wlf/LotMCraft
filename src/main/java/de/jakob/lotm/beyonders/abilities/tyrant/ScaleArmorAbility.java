package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ScaleArmorAbility extends ToggleAbility {
    public ScaleArmorAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return 2;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 4));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        PhysicalEnhancementsAbility.addEnhancementBoost(entity, PhysicalEnhancementsAbility.EnhancementType.RESISTANCE, "Scale_Armor", 10);
    }

    private final DustParticleOptions blueDust = new DustParticleOptions(new Vector3f(20 / 255f, 50 / 255f, 120 / 255f), 1.75f);

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;
        ParticleUtil.spawnParticles((ServerLevel) level, blueDust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 12, .4, entity.getEyeHeight() / 2, .4, 0);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        PhysicalEnhancementsAbility.removeEnhancementBoost(entity, "Scale_Armor");
    }
}