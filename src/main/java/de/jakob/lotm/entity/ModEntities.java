package de.jakob.lotm.entity;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
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

    public static final Supplier<EntityType<SpearOfLightProjectileEntity>> SPEAR_OF_LIGHT =
            ENTITY_TYPES.register("spear_of_light", () -> EntityType.Builder.<SpearOfLightProjectileEntity>of(SpearOfLightProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("spear_of_light"));

    public static final DeferredHolder<EntityType<?>, EntityType<ReturnPortalEntity>> RETURN_PORTAL =
            ENTITY_TYPES.register("return_portal", () ->
                    EntityType.Builder.<ReturnPortalEntity>of(ReturnPortalEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build("return_portal")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<CycleOfFateEntity>> CYCLE_OF_FATE =
            ENTITY_TYPES.register("cycle_of_fate", () -> EntityType.Builder.of(
                            CycleOfFateEntity::new,
                            MobCategory.MISC
                    )
                    .sized(0.5F, 0.5F) // Small invisible entity
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .noSummon() // Cannot be summoned via commands
                    .fireImmune()
                    .build("cycle_of_fate"));

    public static final Supplier<EntityType<SpearOfDestructionProjectileEntity>> SPEAR_OF_DESTRUCTION =
            ENTITY_TYPES.register("spear_of_destruction", () -> EntityType.Builder.<SpearOfDestructionProjectileEntity>of(SpearOfDestructionProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("spear_of_destruction"));

    public static final Supplier<EntityType<FireballEntity>> FIREBALL =
            ENTITY_TYPES.register("fireball", () -> EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC)
                    .sized(.55f, .55f).build("fireball"));

    public static final Supplier<EntityType<WindBladeEntity>> WIND_BLADE =
            ENTITY_TYPES.register("wind_blade", () -> EntityType.Builder.<WindBladeEntity>of(WindBladeEntity::new, MobCategory.MISC)
                    .sized(.75f, 2f).build("wind_blade"));

    public static final Supplier<EntityType<CoffinEntity>> COFFIN =
            ENTITY_TYPES.register("coffin", () -> EntityType.Builder.<CoffinEntity>of(CoffinEntity::new, MobCategory.MISC)
                    .sized(5f, 2f).build("coffin"));

    public static final Supplier<EntityType<MisfortuneWordsEntity>> MISFORTUNE_WORDS =
            ENTITY_TYPES.register("misfortune_words", () -> EntityType.Builder.<MisfortuneWordsEntity>of(MisfortuneWordsEntity::new, MobCategory.MISC)
                    .sized(20f, 2f).build("misfortune_words"));

    public static final Supplier<EntityType<BloomingAreaEntity>> BLOOMING_AREA =
            ENTITY_TYPES.register("blooming_area", () -> EntityType.Builder.<BloomingAreaEntity>of(BloomingAreaEntity::new, MobCategory.MISC)
                    .sized(20f, 2f).build("blooming_area"));

    public static final Supplier<EntityType<DesolateAreaEntity>> DESOLATE_AREA =
            ENTITY_TYPES.register("desolate_area", () -> EntityType.Builder.<DesolateAreaEntity>of(DesolateAreaEntity::new, MobCategory.MISC)
                    .sized(20f, 2f).build("desolate_area"));

    public static final DeferredHolder<EntityType<?>, EntityType<TornadoEntity>> TORNADO =
            ENTITY_TYPES.register("tornado", () -> EntityType.Builder.of(
                            (EntityType<TornadoEntity> type, net.minecraft.world.level.Level level) ->
                                    new TornadoEntity(type, level),
                            MobCategory.MISC)
                    .sized(8.0f, 16.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("tornado"));

    public static final DeferredHolder<EntityType<?>, EntityType<ElectromagneticTornadoEntity>> ELECTROMAGNETIC_TORNADO =
            ENTITY_TYPES.register("electromagnetic_tornado", () -> EntityType.Builder.of(
                            (EntityType<ElectromagneticTornadoEntity> type, net.minecraft.world.level.Level level) ->
                                    new ElectromagneticTornadoEntity(type, level),
                            MobCategory.MISC)
                    .sized(8.0f, 16.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("electromagnetic_tornado"));

    public static final DeferredHolder<EntityType<?>, EntityType<MeteorEntity>> Meteor =
            ENTITY_TYPES.register("meteor", () -> EntityType.Builder.of(
                            (EntityType<MeteorEntity> type, net.minecraft.world.level.Level level) ->
                                    new MeteorEntity(type, level),
                            MobCategory.MISC)
                    .sized(5.0f, 5.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("meteor"));

    public static final DeferredHolder<EntityType<?>, EntityType<JusticeSwordEntity>> JUSTICE_SWORD =
            ENTITY_TYPES.register("justice_sword", () -> EntityType.Builder.of(
                            (EntityType<JusticeSwordEntity> type, Level level) ->
                                    new JusticeSwordEntity(type, level),
                            MobCategory.MISC)
                    .sized(2f, 2f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("justice_sword"));

    public static final DeferredHolder<EntityType<?>, EntityType<VolcanoEntity>> VOLCANO =
            ENTITY_TYPES.register("volcano", () -> EntityType.Builder.of(
                            (EntityType<VolcanoEntity> type, Level level) ->
                                    new VolcanoEntity(type, level),
                            MobCategory.MISC)
                    .sized(2f, 2f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("volcano"));

    public static final Supplier<EntityType<ApprenticeDoorEntity>> APPRENTICE_DOOR =
            ENTITY_TYPES.register("apprentice_door", () -> EntityType.Builder.<ApprenticeDoorEntity>of(ApprenticeDoorEntity::new, MobCategory.MISC)
                    .sized(.005f, 2f).build("apprentice_door"));

    public static final Supplier<EntityType<TravelersDoorEntity>> TRAVELERS_DOOR =
            ENTITY_TYPES.register("travelers_door", () -> EntityType.Builder.<TravelersDoorEntity>of(TravelersDoorEntity::new, MobCategory.MISC)
                    .sized(1.5f, 2.5f).build("travelers_door"));

    public static final Supplier<EntityType<WarBannerEntity>> WAR_BANNER =
            ENTITY_TYPES.register("war_banner", () -> EntityType.Builder.<WarBannerEntity>of(WarBannerEntity::new, MobCategory.MISC)
                    .sized(1.5f, 4f).build("war_banner"));

    public static final Supplier<EntityType<SunKingdomEntity>> SUN_KINGDOM =
            ENTITY_TYPES.register("sun_kingdom", () -> EntityType.Builder.<SunKingdomEntity>of(SunKingdomEntity::new, MobCategory.MISC)
                    .sized(1.5f, 4f).build("sun_kingdom"));

    public static final Supplier<EntityType<DistortionFieldEntity>> DISTORTION_FIELD =
            ENTITY_TYPES.register("distortion_field", () -> EntityType.Builder.<DistortionFieldEntity>of(DistortionFieldEntity::new, MobCategory.MISC)
                    .sized(1.5f, 4f).build("distortion_field"));

    public static final Supplier<EntityType<ApprenticeBookEntity>> APPRENTICE_BOOK =
            ENTITY_TYPES.register("apprentice_book", () -> EntityType.Builder.<ApprenticeBookEntity>of(ApprenticeBookEntity::new, MobCategory.MISC)
                    .sized(.8f, .2f).build("apprentice_book"));

    public static final Supplier<EntityType<ExileDoorsEntity>> EXILE_DOORS =
            ENTITY_TYPES.register("exile_doors", () -> EntityType.Builder.<ExileDoorsEntity>of(ExileDoorsEntity::new, MobCategory.MISC)
                    .sized(7.5f, 8f).build("exile_doors"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpaceCollapseEntityLegacy>> SPACE_COLLAPSE_LEGACY =
            ENTITY_TYPES.register("space_collapse_legacy", () -> EntityType.Builder.<SpaceCollapseEntityLegacy>of(
                            SpaceCollapseEntityLegacy::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .noSave()
                    .build("space_collapse_legacy"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpaceCollapseEntity>> SPACE_COLLAPSE =
            ENTITY_TYPES.register("space_collapse", () -> EntityType.Builder.<SpaceCollapseEntity>of(
                            SpaceCollapseEntity::new,
                            MobCategory.MISC
                    )
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("space_collapse"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlackHoleEntity>> BLACK_HOLE =
            ENTITY_TYPES.register("black_hole", () -> EntityType.Builder.<BlackHoleEntity>of(
                            BlackHoleEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("black_hole"));

    public static final DeferredHolder<EntityType<?>, EntityType<SunEntity>> SUN =
            ENTITY_TYPES.register("sun", () -> EntityType.Builder.<SunEntity>of(
                            SunEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("sun"));

    public static final DeferredHolder<EntityType<?>, EntityType<BigSunEntity>> BIG_SUN =
            ENTITY_TYPES.register("big_sun", () -> EntityType.Builder.<BigSunEntity>of(
                            BigSunEntity::new, MobCategory.MISC)
                    .sized(12.0F, 12.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("big_sun"));

    public static final DeferredHolder<EntityType<?>, EntityType<BigMoonEntity>> BIG_MOON =
            ENTITY_TYPES.register("big_moon", () -> EntityType.Builder.<BigMoonEntity>of(
                            BigMoonEntity::new, MobCategory.MISC)
                    .sized(12.0F, 12.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("big_moon"));

    public static final Supplier<EntityType<SpaceRiftEntity>> SPACE_RIFT =
            ENTITY_TYPES.register("space_rift", () -> EntityType.Builder.<SpaceRiftEntity>of(SpaceRiftEntity::new, MobCategory.MISC)
                    .sized(10f, 10f).build("space_rift"));

    public static final Supplier<EntityType<ElectricShockEntity>> ELECTRIC_SHOCK =
            ENTITY_TYPES.register("electric_shock", () -> EntityType.Builder.<ElectricShockEntity>of(ElectricShockEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f) // Small hitbox
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("electric_shock"));

    public static final Supplier<EntityType<LightningBranchEntity>> LIGHTNING_BRANCH =
            ENTITY_TYPES.register("lightning_branch", () -> EntityType.Builder.<LightningBranchEntity>of(LightningBranchEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("lightning_branch"));

    public static final Supplier<EntityType<TsunamiEntity>> TSUNAMI =
            ENTITY_TYPES.register("tsunami", () -> EntityType.Builder.<TsunamiEntity>of(TsunamiEntity::new, MobCategory.MISC)
                    .sized(4, 3)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("tsunami"));

    public static final Supplier<EntityType<LightningEntity>> LIGHTNING =
            ENTITY_TYPES.register("lightning", () -> EntityType.Builder.<LightningEntity>of(LightningEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f) // Small hitbox
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("lightning"));

    public static final Supplier<EntityType<GiantLightningEntity>> GIANT_LIGHTNING =
            ENTITY_TYPES.register("giant_lightning", () -> EntityType.Builder.<GiantLightningEntity>of(GiantLightningEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f) // Small hitbox
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("giant_lightning"));

    public static final Supplier<EntityType<FrostSpearProjectileEntity>> FROST_SPEAR =
            ENTITY_TYPES.register("frost_spear", () -> EntityType.Builder.<FrostSpearProjectileEntity>of(FrostSpearProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("frost_spear"));

    public static final Supplier<EntityType<PaperDaggerProjectileEntity>> PAPER_DAGGER =
            ENTITY_TYPES.register("paper_dagger", () -> EntityType.Builder.<PaperDaggerProjectileEntity>of(PaperDaggerProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).build("paper_dagger"));

    public static final Supplier<EntityType<FireRavenEntity>> FIRE_RAVEN =
            ENTITY_TYPES.register("fire_raven", () -> EntityType.Builder.<FireRavenEntity>of(FireRavenEntity::new, MobCategory.CREATURE)
                    .sized(.6f, .8f).build("fire_raven"));

    public static final DeferredHolder<EntityType<?>, EntityType<BeyonderNPCEntity>> BEYONDER_NPC =
            ENTITY_TYPES.register("beyonder_npc", () -> EntityType.Builder.<BeyonderNPCEntity>of(BeyonderNPCEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(32)
                    .updateInterval(2)
                    .build("beyonder_npc"));

    public static final DeferredHolder<EntityType<?>, EntityType<AvatarEntity>> ERROR_AVATAR =
            ENTITY_TYPES.register("error_avatar", () -> EntityType.Builder.<AvatarEntity>of(AvatarEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(32)
                    .updateInterval(2)
                    .noSummon()
                    .build("error_avatar"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
