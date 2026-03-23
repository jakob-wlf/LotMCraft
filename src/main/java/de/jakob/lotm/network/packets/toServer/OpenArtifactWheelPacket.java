package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.ArtifactWheel.ArtifactWheelMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenArtifactWheelPacket(ItemStack stack) implements CustomPacketPayload {
    public static final Type<OpenArtifactWheelPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_artifact_wheel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArtifactWheelPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, OpenArtifactWheelPacket::stack,
            OpenArtifactWheelPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenArtifactWheelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();
                player.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> new ArtifactWheelMenu(id, inventory, packet.stack()),
                        Component.translatable("lotm.ability_wheel.title")
                ), buffer -> {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, packet.stack());
                });
            }
        });
    }
}