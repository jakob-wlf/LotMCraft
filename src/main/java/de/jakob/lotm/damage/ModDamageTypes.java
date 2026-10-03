package de.jakob.lotm.damage;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> LOOSING_CONTROL = key("loosing_control");
    public static final ResourceKey<DamageType> PURIFICATION = key("purification");
    /** Used by ticking/AoE Sun abilities — treated as indirect for digestion drain purposes. */
    public static final ResourceKey<DamageType> PURIFICATION_INDIRECT = key("purification_indirect");
    public static final ResourceKey<DamageType> HUNTER_FIRE = key("hunter_fire");
    public static final ResourceKey<DamageType> SAILOR_LIGHTNING = key("sailor_lightning");
    public static final ResourceKey<DamageType> UNLUCK = key("unluck");
    public static final ResourceKey<DamageType> MOTHER_GENERIC = key("mother_generic");
    public static final ResourceKey<DamageType> DOOR_SPACE = key("door_space");
    public static final ResourceKey<DamageType> DARKNESS_GENERIC = key("darkness_generic");
    public static final ResourceKey<DamageType> DEMONESS_GENERIC = key("demoness_generic");
    public static final ResourceKey<DamageType> BEYONDER_GENERIC = key("beyonder_generic");
    public static final ResourceKey<DamageType> SPIRIT_CALLED = key("spirit_called");

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /** Shorthand for creating a ResourceKey for a damage type under this mod's namespace. */
    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, name)
        );
    }

    /** Resolves a ResourceKey to its Holder via the level's registry access. */
    private static Holder<DamageType> holder(Level level, ResourceKey<DamageType> key) {
        return level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);
    }

    /** Damage with no attacker — uses base death message key. */
    public static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(holder(level, key));
    }

    /** Damage with a direct attacker — uses .player death message key if attacker is a player or named entity. */
    public static DamageSource source(Level level, ResourceKey<DamageType> key, Entity attacker) {
        return new DamageSource(holder(level, key), attacker);
    }

    /**
     * Deals true damage that bypasses armor and resistance by directly reducing health.
     * Kills the target if damage >= current health. Still triggers death if health reaches 0.
     */
    public static void trueDamage(LivingEntity target, float amount) {
        float newHealth = target.getHealth() - amount;
        if (newHealth <= 0) {
            target.setHealth(0);
            target.die(target.level() instanceof Level l ? source(l, BEYONDER_GENERIC) : null);
        } else {
            target.setHealth(newHealth);
        }
    }

    /**
     * Deals true damage with an attacker for death message attribution.
     */
    public static void trueDamage(LivingEntity target, float amount, Level level, Entity attacker) {
        float newHealth = target.getHealth() - amount;
        if (newHealth <= 0) {
            target.setHealth(0);
            target.die(source(level, BEYONDER_GENERIC, attacker));
        } else {
            target.setHealth(newHealth);
        }
    }
}