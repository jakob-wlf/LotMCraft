package de.jakob.lotm.abilities;

import net.minecraft.world.effect.MobEffects;

import java.util.List;

public abstract class PhysicalEnhancementsAbility extends PassiveAbilityItem{
    public PhysicalEnhancementsAbility(Properties properties) {
        super(properties);
    }

    public abstract List<PhysicalEnhancement> getEnhancements();

    public enum PhysicalEnhancement {
    }
}
