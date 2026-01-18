package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.quests.PlayerQuestData;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
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
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<AllyComponent>> ALLY_COMPONENT = ATTACHMENT_TYPES.register(
            "ally_component",
            () -> AttachmentType.builder(() -> new AllyComponent())
                    .serialize(AllyComponent.CODEC)
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

    public static final Supplier<AttachmentType<PlayerQuestData>> PLAYER_QUEST_DATA = ATTACHMENT_TYPES.register(
            "player_quest_data",
            () -> AttachmentType.builder(() -> new PlayerQuestData())
                    .serialize(new IAttachmentSerializer<CompoundTag, PlayerQuestData>() {
                        @Override
                        public PlayerQuestData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            PlayerQuestData data = new PlayerQuestData();
                            data.loadFromNBT(tag);
                            return data;
                        }

                        @Override
                        public CompoundTag write(PlayerQuestData attachment, HolderLookup.Provider provider) {
                            return attachment.saveToNBT();
                        }
                    })
                    .build()
    );

    public static final Supplier<AttachmentType<AbilityHotbarManager>> ABILITY_HOTBAR =
            ATTACHMENT_TYPES.register("ability_hotbar", () ->
                    AttachmentType.builder(AbilityHotbarManager::new)
                            .serialize(new IAttachmentSerializer<CompoundTag, AbilityHotbarManager>() {
                                @Override
                                public AbilityHotbarManager read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                                    AbilityHotbarManager manager = new AbilityHotbarManager();
                                    manager.deserializeNBT(provider, tag);
                                    return manager;
                                }

                                @Override
                                public CompoundTag write(AbilityHotbarManager attachment, HolderLookup.Provider provider) {
                                    CompoundTag result = attachment.serializeNBT(provider);
                                    return result;
                                }
                            })
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

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}