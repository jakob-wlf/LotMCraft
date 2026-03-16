package de.jakob.lotm.damage;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> LOOSING_CONTROL = key("loosing_control");
    public static final ResourceKey<DamageType> PURIFICATION = key("purification");
    public static final ResourceKey<DamageType> HUNTER_FIRE = key("hunter_fire");
    public static final ResourceKey<DamageType> SAILOR_LIGHTNING = key("sailor_lightning");
    public static final ResourceKey<DamageType> UNLUCK = key("unluck");
    public static final ResourceKey<DamageType> MOTHER_GENERIC = key("mother_generic");
    public static final ResourceKey<DamageType> DOOR_SPACE = key("door_space");
    public static final ResourceKey<DamageType> DARKNESS_GENERIC = key("darkness_generic");
    public static final ResourceKey<DamageType> BEYONDER_GENERIC = key("beyonder_generic");

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
}