package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
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
                            .build()
            );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}