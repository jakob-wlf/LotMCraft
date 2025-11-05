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
                            .build()
            );

    public static final Supplier<AttachmentType<SubordinateComponent>> SUBORDINATE_COMPONENT =
            ATTACHMENT_TYPES.register("subordinate_component", () ->
                    AttachmentType.builder(SubordinateComponent::new)
                            .serialize(SubordinateComponent.SERIALIZER)
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

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}