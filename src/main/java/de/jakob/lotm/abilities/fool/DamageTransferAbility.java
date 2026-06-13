package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class DamageTransferAbility extends SelectableAbility {
    public DamageTransferAbility(String id) {
        super(id, 90);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 120;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.damage_transfer.self", "ability.lotmcraft.damage_transfer.others"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide) return;

        LivingEntity target = selectedAbility == 0 ? entity : AbilityUtil.getTargetEntity(entity, 3, 2, true, true);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("lotmcraft.no_target").withColor(getColorForPathway("fool")));
            return;
        }

        float healthLost = target.getMaxHealth() - target.getHealth();
        if(healthLost <= 0) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.damage_transfer.no_damage").withColor(getColorForPathway("fool")));
            return;
        }

        target.heal(healthLost / 2f);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.ASH, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()), 120, .4, target.getBbWidth() / 2, .4, .1);
        level.playSound(null, target.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, net.minecraft.sounds.SoundSource.PLAYERS, .6f, .1f);
    }
}
