package de.jakob.lotm.damage;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ModDamageTypes {

    //3 base types of damage
    public static final TagKey<DamageType> MIND = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("lotmcraft", "mind")
    );
    public static final TagKey<DamageType> PHYSICAL = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("lotmcraft", "physical")
    );
    public static final TagKey<DamageType> SOUL = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("lotmcraft", "soul")
    );
    public static final TagKey<DamageType> HOLY = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("lotmcraft", "holy")
    );
    public static final TagKey<DamageType> EVIL = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("lotmcraft", "evil")
    );
    public static final TagKey<DamageType> NATURE = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("lotmcraft", "nature")
    );

    public static final ResourceKey<DamageType> MIND_BASED = key("mind_based");
    public static final ResourceKey<DamageType> PHYSICAL_BASED = key("physical_based");
    public static final ResourceKey<DamageType> SOUL_BASED = key("soul_based");
    public static final ResourceKey<DamageType> HOLY_BASED = key("holy_based");
    public static final ResourceKey<DamageType> EVIL_BASED = key("evil_based");
    public static final ResourceKey<DamageType> NATURE_BASED = key("nature_based");

    public static final ResourceKey<DamageType> LOOSING_CONTROL = key("loosing_control");
    ////fallback (remove when rework is finished or just ignore it)
    public static final ResourceKey<DamageType> BEYONDER_GENERIC = key("beyonder_generic");

    public static final ResourceKey<DamageType> PURIFICATION = key("purification");
    public static final ResourceKey<DamageType> LIGHT = key("light");
    public static final ResourceKey<DamageType> FAITH = key("faith");
    public static final ResourceKey<DamageType> ORDER = key("order");

    public static final ResourceKey<DamageType> IMAGINATION = key("imagination"); //for illusions
    public static final ResourceKey<DamageType> AWE = key("awe");

    public static final ResourceKey<DamageType> LIGHTNING = key("lightning");
    public static final ResourceKey<DamageType> INFORMATION_DESTRUCTION = key("information_destruction");
    public static final ResourceKey<DamageType> WATER = key("water");
    public static final ResourceKey<DamageType> WIND = key("wind");
    public static final ResourceKey<DamageType> FIRE = key("fire");
    public static final ResourceKey<DamageType> IMPACT = key("impact");
    public static final ResourceKey<DamageType> SPACE_DESTRUCTION = key("space_destruction");

    public static final ResourceKey<DamageType> PLAGUE = key("plague");

    public static final ResourceKey<DamageType> UNLUCK = key("unluck");
    public static final ResourceKey<DamageType> SPIRITUAL = key("spiritual");

    public static final ResourceKey<DamageType> PROVOCATION = key("provocation");
    public static final ResourceKey<DamageType> SOUL_FIRE = key("soul_fire");

    public static final ResourceKey<DamageType> NATURE_WRATH = key("nature_wrath");
    public static final ResourceKey<DamageType> MUTATION = key("mutation");
    public static final ResourceKey<DamageType> TRIAL_OF_DEATH = key("trial_of_death");
    public static final ResourceKey<DamageType> TRIAL_OF_MADNESS = key("trial_of_madness");
    public static final ResourceKey<DamageType> LIFE_DEPRIVATION = key("life_deprivation");
    public static final ResourceKey<DamageType> RETURN_TO_EARTH = key("return_to_earth");



    /** Used by ticking/AoE Sun abilities — treated as indirect for digestion drain purposes. */
    public static final ResourceKey<DamageType> PURIFICATION_INDIRECT = key("purification_indirect");
    public static final ResourceKey<DamageType> HUNTER_FIRE = key("hunter_fire");
    public static final ResourceKey<DamageType> SAILOR_LIGHTNING = key("sailor_lightning");

    public static final ResourceKey<DamageType> MOTHER_GENERIC = key("mother_generic");
    public static final ResourceKey<DamageType> DOOR_SPACE = key("door_space");
    public static final ResourceKey<DamageType> DARKNESS_GENERIC = key("darkness_generic");
    public static final ResourceKey<DamageType> DEMONESS_GENERIC = key("demoness_generic");

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
    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity attacker) {
        if(attacker == null)
            return source(level, key);

        return new DamageSource(holder(level, key), attacker);
    }

    public static boolean isModDamage(DamageSource source){
        return source.is(ModDamageTypes.MIND)
                || source.is(ModDamageTypes.PHYSICAL)
                || source.is(ModDamageTypes.SOUL);
    }

    public static boolean isModDamage(Holder<DamageType> source){
        return source.is(ModDamageTypes.MIND)
                || source.is(ModDamageTypes.PHYSICAL)
                ||source.is(ModDamageTypes.SOUL);
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