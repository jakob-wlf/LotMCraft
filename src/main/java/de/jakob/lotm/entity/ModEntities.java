package de.jakob.lotm.entity;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.*;
import de.jakob.lotm.entity.custom.ability_entities.*;
import de.jakob.lotm.entity.custom.ability_entities.darkness_pathway.ConcealedDomainEntity;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.DeathDivineKingdomEntity;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.UnderworldGateEntity;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.*;
import de.jakob.lotm.entity.custom.ability_entities.mother_pathway.BloomingAreaEntity;
import de.jakob.lotm.entity.custom.ability_entities.mother_pathway.CoffinEntity;
import de.jakob.lotm.entity.custom.ability_entities.mother_pathway.DesolateAreaEntity;
import de.jakob.lotm.entity.custom.projectiles.*;
import de.jakob.lotm.entity.custom.ability_entities.red_priest_pathway.WarBannerEntity;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.AncientCourtEntity;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.JudgmentSwordEntity;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.JusticeSwordEntity;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.SunEntity;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.SunKingdomEntity;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.*;
import de.jakob.lotm.entity.custom.ability_entities.wheel_of_fortune_pathway.CycleOfFateEntity;
import de.jakob.lotm.entity.custom.ability_entities.wheel_of_fortune_pathway.MisfortuneWordsEntity;
import de.jakob.lotm.entity.custom.spirits.*;
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

    // Projectiles

    public static final Supplier<EntityType<FlamingSpearProjectileEntity>> FLAMING_SPEAR =
            ENTITY_TYPES.register("flaming_spear", () -> EntityType.Builder.<FlamingSpearProjectileEntity>of(FlamingSpearProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).noSave().build("flaming_spear"));

    public static final Supplier<EntityType<UnshadowedSpearProjectileEntity>> UNSHADOWED_SPEAR =
            ENTITY_TYPES.register("unshadowed_spear", () -> EntityType.Builder.<UnshadowedSpearProjectileEntity>of(UnshadowedSpearProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).noSave().build("unshadowed_spear"));

    public static final Supplier<EntityType<SpearOfLightProjectileEntity>> SPEAR_OF_LIGHT =
            ENTITY_TYPES.register("spear_of_light", () -> EntityType.Builder.<SpearOfLightProjectileEntity>of(SpearOfLightProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).noSave().build("spear_of_light"));

    public static final Supplier<EntityType<SpearOfDestructionProjectileEntity>> SPEAR_OF_DESTRUCTION =
            ENTITY_TYPES.register("spear_of_destruction", () -> EntityType.Builder.<SpearOfDestructionProjectileEntity>of(SpearOfDestructionProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).noSave().build("spear_of_destruction"));

    public static final Supplier<EntityType<FrostSpearProjectileEntity>> FROST_SPEAR =
            ENTITY_TYPES.register("frost_spear", () -> EntityType.Builder.<FrostSpearProjectileEntity>of(FrostSpearProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).noSave().build("frost_spear"));

    public static final Supplier<EntityType<PaperDaggerProjectileEntity>> PAPER_DAGGER =
            ENTITY_TYPES.register("paper_dagger", () -> EntityType.Builder.<PaperDaggerProjectileEntity>of(PaperDaggerProjectileEntity::new, MobCategory.MISC)
                    .sized(.35f, .35f).noSave().build("paper_dagger"));

    public static final Supplier<EntityType<FireballEntity>> FIREBALL =
            ENTITY_TYPES.register("fireball", () -> EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC)
                    .sized(.55f, .55f).noSave().build("fireball"));

    public static final Supplier<EntityType<SpiritBallEntity>> SPIRIT_BALL =
            ENTITY_TYPES.register("spirit_ball", () -> EntityType.Builder.<SpiritBallEntity>of(SpiritBallEntity::new, MobCategory.MISC)
                    .sized(.55f, .55f).noSave().build("spirit_ball"));

    public static final Supplier<EntityType<SpiritBlockProjectileEntity>> SPIRIT_BLOCK_PROJECTILE =
            ENTITY_TYPES.register("spirit_block_projectile", () -> EntityType.Builder.<SpiritBlockProjectileEntity>of(SpiritBlockProjectileEntity::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f).noSave().build("spirit_block_projectile"));

    public static final Supplier<EntityType<WindBladeEntity>> WIND_BLADE =
            ENTITY_TYPES.register("wind_blade", () -> EntityType.Builder.<WindBladeEntity>of(WindBladeEntity::new, MobCategory.MISC)
                    .sized(.75f, 2f).noSave().build("wind_blade"));

    // Ability Entities - Death Pathway

    public static final Supplier<EntityType<DeathDivineKingdomEntity>> DEATH_DIVINE_KINGDOM =
            ENTITY_TYPES.register("death_divine_kingdom", () -> EntityType.Builder.<DeathDivineKingdomEntity>of(DeathDivineKingdomEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(64).updateInterval(1).noSummon().noSave().fireImmune().build("death_divine_kingdom"));

    // Ability Entities - Darkness Pathway

    public static final Supplier<EntityType<ConcealedDomainEntity>> CONCEALED_DOMAIN =
            ENTITY_TYPES.register("concealed_domain", () -> EntityType.Builder.<ConcealedDomainEntity>of(ConcealedDomainEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(64).updateInterval(1).noSummon().noSave().fireImmune().build("concealed_domain"));
    
    // Ability Entities - Door Pathway

    public static final Supplier<EntityType<ApprenticeDoorEntity>> APPRENTICE_DOOR =
            ENTITY_TYPES.register("apprentice_door", () -> EntityType.Builder.<ApprenticeDoorEntity>of(ApprenticeDoorEntity::new, MobCategory.MISC)
                    .sized(.005f, 2f).build("apprentice_door"));

    public static final Supplier<EntityType<TravelersDoorEntity>> TRAVELERS_DOOR =
            ENTITY_TYPES.register("travelers_door", () -> EntityType.Builder.<TravelersDoorEntity>of(TravelersDoorEntity::new, MobCategory.MISC)
                    .sized(1.5f, 2.5f).noSave().build("travelers_door"));

    public static final Supplier<EntityType<ExileDoorsEntity>> EXILE_DOORS =
            ENTITY_TYPES.register("exile_doors", () -> EntityType.Builder.<ExileDoorsEntity>of(ExileDoorsEntity::new, MobCategory.MISC)
                    .sized(7.5f, 8f).build("exile_doors"));

    public static final Supplier<EntityType<ApprenticeBookEntity>> APPRENTICE_BOOK =
            ENTITY_TYPES.register("apprentice_book", () -> EntityType.Builder.<ApprenticeBookEntity>of(ApprenticeBookEntity::new, MobCategory.MISC)
                    .sized(.8f, .2f).build("apprentice_book"));

    // Ability Entities - Sun Pathway

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

    public static final DeferredHolder<EntityType<?>, EntityType<JudgmentSwordEntity>> JUDGMENT_SWORD =
            ENTITY_TYPES.register("judgment_sword", () -> EntityType.Builder.of(
                            (EntityType<JudgmentSwordEntity> type, Level level) ->
                                    new JudgmentSwordEntity(type, level),
                            MobCategory.MISC)
                    .sized(2f, 2f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("judgment_sword"));

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

    public static final Supplier<EntityType<SunKingdomEntity>> SUN_KINGDOM =
            ENTITY_TYPES.register("sun_kingdom", () -> EntityType.Builder.<SunKingdomEntity>of(SunKingdomEntity::new, MobCategory.MISC)
                    .sized(1.5f, 4f).build("sun_kingdom"));

    // Ability Entities - Justiciar Pathway

    public static final Supplier<EntityType<AncientCourtEntity>> ANCIENT_COURT =
            ENTITY_TYPES.register("ancient_court", () -> EntityType.Builder.<AncientCourtEntity>of(AncientCourtEntity::new, MobCategory.MISC)
                    .sized(1.5f, 4f).clientTrackingRange(256).updateInterval(1).noSummon().noSave().build("ancient_court"));

    // Ability Entities - Mother Pathway

    public static final Supplier<EntityType<CoffinEntity>> COFFIN =
            ENTITY_TYPES.register("coffin", () -> EntityType.Builder.<CoffinEntity>of(CoffinEntity::new, MobCategory.MISC)
                    .sized(5f, 2f).build("coffin"));

    public static final Supplier<EntityType<BloomingAreaEntity>> BLOOMING_AREA =
            ENTITY_TYPES.register("blooming_area", () -> EntityType.Builder.<BloomingAreaEntity>of(BloomingAreaEntity::new, MobCategory.MISC)
                    .sized(20f, 2f).build("blooming_area"));

    public static final Supplier<EntityType<DesolateAreaEntity>> DESOLATE_AREA =
            ENTITY_TYPES.register("desolate_area", () -> EntityType.Builder.<DesolateAreaEntity>of(DesolateAreaEntity::new, MobCategory.MISC)
                    .sized(20f, 2f).build("desolate_area"));

    // Ability Entities - Tyrant Pathway

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

    public static final Supplier<EntityType<TsunamiEntity>> TSUNAMI =
            ENTITY_TYPES.register("tsunami", () -> EntityType.Builder.<TsunamiEntity>of(TsunamiEntity::new, MobCategory.MISC)
                    .sized(4, 3)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("tsunami"));

    // Ability Entities - Wheel of Fortune Pathway

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

    public static final Supplier<EntityType<MisfortuneWordsEntity>> MISFORTUNE_WORDS =
            ENTITY_TYPES.register("misfortune_words", () -> EntityType.Builder.<MisfortuneWordsEntity>of(MisfortuneWordsEntity::new, MobCategory.MISC)
                    .sized(20f, 2f).build("misfortune_words"));

    // Ability Entities - Red Priest Pathway

    public static final Supplier<EntityType<WarBannerEntity>> WAR_BANNER =
            ENTITY_TYPES.register("war_banner", () -> EntityType.Builder.<WarBannerEntity>of(WarBannerEntity::new, MobCategory.MISC)
                    .sized(1.5f, 4f).build("war_banner"));

    // Ability Entities - General

    public static final DeferredHolder<EntityType<?>, EntityType<BigMoonEntity>> BIG_MOON =
            ENTITY_TYPES.register("big_moon", () -> EntityType.Builder.<BigMoonEntity>of(
                            BigMoonEntity::new, MobCategory.MISC)
                    .sized(12.0F, 12.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("big_moon"));

    public static final Supplier<EntityType<DistortionFieldEntity>> DISTORTION_FIELD =
            ENTITY_TYPES.register("distortion_field", () -> EntityType.Builder.<DistortionFieldEntity>of(DistortionFieldEntity::new, MobCategory.MISC)
                    .sized(1.5f, 4f).build("distortion_field"));

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

    // Lightning

    public static final Supplier<EntityType<ElectricShockEntity>> ELECTRIC_SHOCK =
            ENTITY_TYPES.register("electric_shock", () -> EntityType.Builder.<ElectricShockEntity>of(ElectricShockEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("electric_shock"));

    public static final Supplier<EntityType<LightningBranchEntity>> LIGHTNING_BRANCH =
            ENTITY_TYPES.register("lightning_branch", () -> EntityType.Builder.<LightningBranchEntity>of(LightningBranchEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("lightning_branch"));

    public static final Supplier<EntityType<LightningEntity>> LIGHTNING =
            ENTITY_TYPES.register("lightning", () -> EntityType.Builder.<LightningEntity>of(LightningEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("lightning"));

    public static final Supplier<EntityType<GiantLightningEntity>> GIANT_LIGHTNING =
            ENTITY_TYPES.register("giant_lightning", () -> EntityType.Builder.<GiantLightningEntity>of(GiantLightningEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("giant_lightning"));

    // Spirits
    public static final Supplier<EntityType<SpiritDervishEntity>> SPIRIT_DERVISH_ENTITY =
            ENTITY_TYPES.register("spirit_dervish", () -> EntityType.Builder.<SpiritDervishEntity>of(SpiritDervishEntity::new, MobCategory.MONSTER)
                    .sized(1, 1).build("spirit_dervish"));

    public static final Supplier<EntityType<SpiritBubblesEntity>> SPIRIT_BUBBLES_ENTITY =
            ENTITY_TYPES.register("spirit_bubbles", () -> EntityType.Builder.<SpiritBubblesEntity>of(SpiritBubblesEntity::new, MobCategory.MONSTER)
                    .sized(.6f, .6f).build("spirit_bubbles"));

    public static final Supplier<EntityType<SpiritBlueWizardEntity>> SPIRIT_BLUE_WIZARD =
            ENTITY_TYPES.register("spirit_blue_wizard", () -> EntityType.Builder.of(SpiritBlueWizardEntity::new, MobCategory.MONSTER)
                    .sized(2, 2).build("spirit_blue_wizard"));

    public static final Supplier<EntityType<SpiritTranslucentWizardEntity>> SPIRIT_TRANSLUCENT_WIZARD =
            ENTITY_TYPES.register("spirit_translucent_wizard", () -> EntityType.Builder.of(SpiritTranslucentWizardEntity::new, MobCategory.MONSTER)
                    .sized(1.3f, 1.3f).build("spirit_translucent_wizard"));

    public static final Supplier<EntityType<SpiritGhostEntity>> SPIRIT_GHOST =
            ENTITY_TYPES.register("spirit_ghost", () -> EntityType.Builder.of(SpiritGhostEntity::new, MobCategory.MONSTER)
                    .sized(1.3f, 1.3f).build("spirit_ghost"));

    public static final Supplier<EntityType<SpiritBizarroBaneEntity>> SPIRIT_BIZARRO_BANE =
            ENTITY_TYPES.register("spirit_bizarro_bane", () -> EntityType.Builder.of(SpiritBizarroBaneEntity::new, MobCategory.MONSTER)
                    .sized(2.6f, 4f).build("spirit_bizarro_bane"));

    public static final Supplier<EntityType<SpiritBaneEntity>> SPIRIT_BANE =
            ENTITY_TYPES.register("spirit_bane", () -> EntityType.Builder.of(SpiritBaneEntity::new, MobCategory.MONSTER)
                    .sized(1.6f, 3.85f).build("spirit_bane"));

    public static final Supplier<EntityType<SpiritMalmouthEntity>> SPIRIT_MALMOUTH =
            ENTITY_TYPES.register("spirit_malmouth", () -> EntityType.Builder.of(SpiritMalmouthEntity::new, MobCategory.MONSTER)
                    .sized(1.3f, 1.3f).build("spirit_malmouth"));


    // NPCs and Living Entities

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

    public static final Supplier<EntityType<OriginalBodyEntity>> ORIGINAL_BODY =
            ENTITY_TYPES.register("original_body", () -> EntityType.Builder.<OriginalBodyEntity>of(OriginalBodyEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .build("original_body"));

    // Utility

    public static final Supplier<EntityType<UnderworldGateEntity>> UNDERWORLD_GATE =
            ENTITY_TYPES.register("underworld_gate", () -> EntityType.Builder.<UnderworldGateEntity>of(UnderworldGateEntity::new, MobCategory.MISC)
                    .sized(1.5f, 2.5f).build("underworld_gate"));

    public static final Supplier<EntityType<PlanetEntity>> PLANET =
            ENTITY_TYPES.register("planet", () -> EntityType.Builder.of(PlanetEntity::new, MobCategory.MISC)
                    .sized(2.5f, 2.5f).build("planet"));

    public static final DeferredHolder<EntityType<?>, EntityType<ReturnPortalEntity>> RETURN_PORTAL =
            ENTITY_TYPES.register("return_portal", () ->
                    EntityType.Builder.<ReturnPortalEntity>of(ReturnPortalEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build("return_portal")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<ReturnPortalEntity>> NATURE_RETURN_PORTAL =
            ENTITY_TYPES.register("nature_return_portal", () ->
                    EntityType.Builder.<ReturnPortalEntity>of(ReturnPortalEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build("return_portal")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TimeChangeEntity>> TIME_CHANGE =
            ENTITY_TYPES.register("time_change", () ->
                    EntityType.Builder.<TimeChangeEntity>of(TimeChangeEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .noSummon()
                            .noSave()
                            .build("time_change")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<LocationGraftingEntity>> GRAFTING_LOCATION_ENTITY =
            ENTITY_TYPES.register("grafting_location", () ->
                    EntityType.Builder.<LocationGraftingEntity>of(LocationGraftingEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .noSummon()
                            .build("grafting_location")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<DamageTrackerEntity>> DAMAGE_TRACKER =
            ENTITY_TYPES.register("damage_tracker", () -> EntityType.Builder.<DamageTrackerEntity>of(DamageTrackerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .build("damage_tracker"));

    public static final DeferredHolder<EntityType<?>, EntityType<de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity>> UNIQUENESS_ENTITY =
            ENTITY_TYPES.register("uniqueness_entity", () ->
                    EntityType.Builder.<de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity>of(
                                    de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("uniqueness_entity")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
