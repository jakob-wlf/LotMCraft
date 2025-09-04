package de.jakob.lotm.entity;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, LOTMCraft.MOD_ID);

    public static final Supplier<EntityType<FlamingSpearProjectileEntity>> FLAMING_SPEAR =
            ENTITY_TYPES.register("flaming_spear", () -> EntityType.Builder.<FlamingSpearProjectileEntity>of(FlamingSpearProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("flaming_spear"));

    public static final Supplier<EntityType<UnshadowedSpearProjectileEntity>> UNSHADOWED_SPEAR =
            ENTITY_TYPES.register("unshadowed_spear", () -> EntityType.Builder.<UnshadowedSpearProjectileEntity>of(UnshadowedSpearProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("unshadowed_spear"));


    public static final Supplier<EntityType<FireballEntity>> FIREBALL =
            ENTITY_TYPES.register("fireball", () -> EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC)
                    .sized(.55f, .55f).build("fireball"));

    public static final Supplier<EntityType<WindBladeEntity>> WIND_BLADE =
            ENTITY_TYPES.register("wind_blade", () -> EntityType.Builder.<WindBladeEntity>of(WindBladeEntity::new, MobCategory.MISC)
                    .sized(.75f, 2f).build("wind_blade"));

    public static final Supplier<EntityType<ApprenticeDoorEntity>> APPRENTICE_DOOR =
            ENTITY_TYPES.register("apprentice_door", () -> EntityType.Builder.<ApprenticeDoorEntity>of(ApprenticeDoorEntity::new, MobCategory.MISC)
                    .sized(.005f, 2f).build("apprentice_door"));

    public static final Supplier<EntityType<TravelersDoorEntity>> TRAVELERS_DOOR =
            ENTITY_TYPES.register("travelers_door", () -> EntityType.Builder.<TravelersDoorEntity>of(TravelersDoorEntity::new, MobCategory.MISC)
                    .sized(1.5f, 2.5f).build("travelers_door"));

    public static final Supplier<EntityType<ApprenticeBookEntity>> APPRENTICE_BOOK =
            ENTITY_TYPES.register("apprentice_book", () -> EntityType.Builder.<ApprenticeBookEntity>of(ApprenticeBookEntity::new, MobCategory.MISC)
                    .sized(.8f, .2f).build("apprentice_book"));

    public static final Supplier<EntityType<ElectricShockEntity>> ELECTRIC_SHOCK =
            ENTITY_TYPES.register("electric_shock", () -> EntityType.Builder.<ElectricShockEntity>of(ElectricShockEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f) // Small hitbox
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("electric_shock"));

    public static final Supplier<EntityType<LightningEntity>> LIGHTNING =
            ENTITY_TYPES.register("lightning", () -> EntityType.Builder.<LightningEntity>of(LightningEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f) // Small hitbox
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("lightning"));

    public static final Supplier<EntityType<FrostSpearProjectileEntity>> FROST_SPEAR =
            ENTITY_TYPES.register("frost_spear", () -> EntityType.Builder.<FrostSpearProjectileEntity>of(FrostSpearProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("frost_spear"));

    public static final Supplier<EntityType<PaperDaggerProjectileEntity>> PAPER_DAGGER =
            ENTITY_TYPES.register("paper_dagger", () -> EntityType.Builder.<PaperDaggerProjectileEntity>of(PaperDaggerProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("paper_dagger"));

    public static final Supplier<EntityType<FireRavenEntity>> FIRE_RAVEN =
            ENTITY_TYPES.register("fire_raven", () -> EntityType.Builder.<FireRavenEntity>of(FireRavenEntity::new, MobCategory.CREATURE)
                    .sized(.6f, .8f).build("fire_raven"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
