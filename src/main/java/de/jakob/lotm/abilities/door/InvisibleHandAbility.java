package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class InvisibleHandAbility extends SelectableAbility {
    public InvisibleHandAbility(String id) {
        super(id, 1.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 170;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.invisible_hand.push", "ability.lotmcraft.invisible_hand.pull"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide)
            return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15*multiplier(entity)), 3);
        if(target == null) {
            Vec3 failureParticleLoc = AbilityUtil.getTargetLocation(entity, (int) (12*multiplier(entity)), 3);
            spawnFailureParticles((ServerLevel) level, failureParticleLoc);
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.invisible_hand.no_target").withColor(BeyonderData.pathwayInfos.get("door").color()));
            return;
        }

        float directionMultiplier = selectedAbility == 0 ? 2.25f : -2.25f;
        Vec3 direction = target.position().subtract(entity.position()).normalize().multiply(directionMultiplier, directionMultiplier, directionMultiplier);
        target.setDeltaMovement(direction);
        target.hurtMarked = true;
    }

    private void spawnFailureParticles(ServerLevel level, Vec3 pos) {
        ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD, pos, 35, .4, .1);
        ParticleUtil.spawnParticles(level, new DustParticleOptions(
                new Vector3f(99 / 255f, 255 / 255f, 250 / 255f),
                1
        ), pos, 35, .4, .1);
    }
}
