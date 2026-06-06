package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LOTMCraft.MOD_ID);

    public static final Supplier<AttachmentType<MarionetteComponent>> MARIONETTE_COMPONENT =
            ATTACHMENT_TYPES.register("marionette_component", () ->
                    AttachmentType.builder(MarionetteComponent::new)
                            .serialize(MarionetteComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<ParasitationComponent>> PARASITE_COMPONENT =
            ATTACHMENT_TYPES.register("parasite_component", () ->
                    AttachmentType.builder(ParasitationComponent::new)
                            .serialize(ParasitationComponent.SERIALIZER)
                            .build()
            );


    public static final Supplier<AttachmentType<DisabledFlightComponent>> FLIGHT_DISABLE_COMPONENT =
            ATTACHMENT_TYPES.register("disabled_flight_component", () ->
                    AttachmentType.builder(DisabledFlightComponent::new)
                            .serialize(DisabledFlightComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<AbilityCooldownComponent>> COOLDOWN_COMPONENT =
            ATTACHMENT_TYPES.register("cooldown_component", () ->
                    AttachmentType.serializable(AbilityCooldownComponent::new).build()
            );

    public static final Supplier<AttachmentType<AbilityWheelComponent>> ABILITY_WHEEL_COMPONENT =
            ATTACHMENT_TYPES.register("ability_wheel_component", () ->
                    AttachmentType.serializable(AbilityWheelComponent::new).copyOnDeath().build()
            );

    public static final Supplier<AttachmentType<AbilityBarComponent>> ABILITY_BAR_COMPONENT =
            ATTACHMENT_TYPES.register("ability_bar_component", () ->
                    AttachmentType.serializable(AbilityBarComponent::new).copyOnDeath().build()
            );

    public static final Supplier<AttachmentType<LuckComponent>> LUCK_COMPONENT =
            ATTACHMENT_TYPES.register("luck_component", () ->
                    AttachmentType.serializable(LuckComponent::new).copyOnDeath().build()
            );

    public static final Supplier<AttachmentType<DisabledAbilitiesComponent>> DISABLED_ABILITIES_COMPONENT =
            ATTACHMENT_TYPES.register("disabled_abilities_component", () ->
                    AttachmentType.serializable(DisabledAbilitiesComponent::new).build()
            );

    public static final Supplier<AttachmentType<ApotheosisComponent>> APOTHEOSIS_COMPONENT =
            ATTACHMENT_TYPES.register("apotheosis_component", () ->
                    AttachmentType.serializable(ApotheosisComponent::new).build()
            );

    public static final Supplier<AttachmentType<MultiplierModifierComponent>> MULTIPLIER_MODIFIER_COMPONENT =
            ATTACHMENT_TYPES.register("multiplier_modifier_component", () ->
                    AttachmentType.serializable(MultiplierModifierComponent::new).build()
            );

    public static final Supplier<AttachmentType<AllyComponent>> ALLY_COMPONENT = ATTACHMENT_TYPES.register(
            "ally_component",
            () -> AttachmentType.builder(() -> new AllyComponent())
                    .serialize(AllyComponent.CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<QuestComponent>> QUEST_COMPONENT = ATTACHMENT_TYPES.register(
            "quest_component",
            () -> AttachmentType.builder(QuestComponent::new)
                    .serialize(QuestComponent.SERIALIZER)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<NewPlayerComponent>> BOOK_COMPONENT =
            ATTACHMENT_TYPES.register("book_component", () ->
                    AttachmentType.builder(NewPlayerComponent::new)
                            .serialize(NewPlayerComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<SanityComponent>> SANITY_COMPONENT =
            ATTACHMENT_TYPES.register("sanity_component", () ->
                    AttachmentType.builder(SanityComponent::new)
                            .serialize(SanityComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<SubordinateComponent>> SUBORDINATE_COMPONENT =
            ATTACHMENT_TYPES.register("subordinate_component", () ->
                    AttachmentType.builder(SubordinateComponent::new)
                            .serialize(SubordinateComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<LuckAccumulationComponent>> LUCK_ACCUMULATION_COMPONENT =
            ATTACHMENT_TYPES.register("luck_accumulation_component", () ->
                    AttachmentType.builder(LuckAccumulationComponent::new)
                            .serialize(LuckAccumulationComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<MirrorWorldTraversalComponent>> MIRROR_WORLD_COMPONENT =
            ATTACHMENT_TYPES.register("mirror_world_component", () ->
                    AttachmentType.builder(MirrorWorldTraversalComponent::new)
                            .serialize(MirrorWorldTraversalComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<TransformationComponent>> TRANSFORMATION_COMPONENT =
            ATTACHMENT_TYPES.register("transformation_component", () ->
                    AttachmentType.builder(TransformationComponent::new)
                            .serialize(TransformationComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<ActiveShaderComponent>> SHADER_COMPONENT =
            ATTACHMENT_TYPES.register("shader_component", () ->
                    AttachmentType.builder(ActiveShaderComponent::new)
                            .serialize(ActiveShaderComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<FogComponent>> FOG_COMPONENT =
            ATTACHMENT_TYPES.register("fog_component", () ->
                    AttachmentType.builder(FogComponent::new)
                            .serialize(FogComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<ControllingDataComponent>> CONTROLLING_DATA =
            ATTACHMENT_TYPES.register("controlling_data", () ->
                    AttachmentType.builder(ControllingDataComponent::new)
                            .serialize(ControllingDataComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<DiscernmentComponent>> DISCERNMENT_DATA =
            ATTACHMENT_TYPES.register("discernment_data", () ->
                    AttachmentType.builder(DiscernmentComponent::new)
                            .serialize(DiscernmentComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<CopiedInventoryComponent>> COPIED_INVENTORY =
            ATTACHMENT_TYPES.register("copied_inventory", () ->
                    AttachmentType.builder(CopiedInventoryComponent::new)
                            .serialize(CopiedInventoryComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<WaypointComponent>> WAYPOINT_COMPONENT = ATTACHMENT_TYPES.register(
            "waypoint_component",
            () -> AttachmentType.builder(WaypointComponent::new)
                    .serialize(new IAttachmentSerializer<CompoundTag, WaypointComponent>() {
                        @Override
                        public WaypointComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            WaypointComponent data = new WaypointComponent();
                            data.loadFromNBT(tag, holder);
                            return data;
                        }

                        @Override
                        public CompoundTag write(WaypointComponent attachment, HolderLookup.Provider provider) {
                            return attachment.saveToNBT();
                        }
                    })
                    .build()
    );

    public static final Supplier<AttachmentType<MemorisedEntities>> MEMORISED_ENTITIES =
            ATTACHMENT_TYPES.register("memorised_entities", () ->
                    AttachmentType.builder(() -> new MemorisedEntities())
                            .serialize(MemorisedEntities.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<CopiedAbilityComponent>> COPIED_ABILITY_COMPONENT =
            ATTACHMENT_TYPES.register("copied_ability_component", () ->
                    AttachmentType.serializable(CopiedAbilityComponent::new).copyOnDeath().build()
            );

    public static final Supplier<AttachmentType<TeamComponent>> TEAM_COMPONENT =
            ATTACHMENT_TYPES.register("team_component", () ->
                    AttachmentType.builder(TeamComponent::new)
                            .serialize(TeamComponent.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<SharedAbilitiesComponent>> SHARED_ABILITIES_COMPONENT =
            ATTACHMENT_TYPES.register("shared_abilities_component", () ->
                    AttachmentType.builder(SharedAbilitiesComponent::new)
                            .serialize(SharedAbilitiesComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<BeyonderComponent>> BEYONDER_COMPONENT =
            ATTACHMENT_TYPES.register("beyonder_component", () ->
                    AttachmentType.serializable(BeyonderComponent::new).copyOnDeath().build()
            );

    public static final Supplier<AttachmentType<SkillScalingComponent>> SKILL_SCALING_COMPONENT =
            ATTACHMENT_TYPES.register("skill_scaling_component", () ->
                    AttachmentType.builder(SkillScalingComponent::new)
                            .serialize(SkillScalingComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<KillCountComponent>> KILL_COUNT_COMPONENT =
            ATTACHMENT_TYPES.register("kill_count_component", () ->
                    AttachmentType.builder(KillCountComponent::new)
                            .serialize(KillCountComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<SacrificeRevertComponent>> SACRIFICE_REVERT_COMPONENT =
            ATTACHMENT_TYPES.register("sacrifice_revert_component", () ->
                    AttachmentType.builder(SacrificeRevertComponent::new)
                            .serialize(SacrificeRevertComponent.SERIALIZER)
                            .build()
            );

    public static final Supplier<AttachmentType<FoolingComponent>> FOOLING_COMPONENT =
            ATTACHMENT_TYPES.register("fooling_component", () ->
                    AttachmentType.serializable(FoolingComponent::new).build());
                                      
    public static final Supplier<AttachmentType<UniquenessComponent>> UNIQUENESS_COMPONENT =
            ATTACHMENT_TYPES.register("uniqueness_component", () ->
                    AttachmentType.builder(UniquenessComponent::new)
                            .serialize(UniquenessComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<HistoricalVoidComponent>> HISTORICAL_VOID_COMPONENT =
            ATTACHMENT_TYPES.register("historical_void_component", () ->
                    AttachmentType.serializable(HistoricalVoidComponent::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<MiracleOfResurrectionComponent>> MIRACLE_OF_RESURRECTION =
            ATTACHMENT_TYPES.register("miracle_of_resurrection", () ->
                    AttachmentType.builder(MiracleOfResurrectionComponent::new)
                            .serialize(MiracleOfResurrectionComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<ShapeShiftComponent>> SHAPE_SHIFT =
            ATTACHMENT_TYPES.register("shape_shift", () ->
                    AttachmentType.builder(ShapeShiftComponent::new)
                            .serialize(ShapeShiftComponent.SERIALIZER)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<SefirotUnlockedAbilitiesComponent>> SEFIROT_UNLOCKED_ABILITIES =
            ATTACHMENT_TYPES.register("sefirot_unlocked_abilities", () ->
                    AttachmentType.serializable(SefirotUnlockedAbilitiesComponent::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<DailySpinComponent>> DAILY_SPIN_COMPONENT =
            ATTACHMENT_TYPES.register("daily_spin_component", () ->
                    AttachmentType.builder(DailySpinComponent::new)
                            .serialize(DailySpinComponent.SERIALIZER)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}