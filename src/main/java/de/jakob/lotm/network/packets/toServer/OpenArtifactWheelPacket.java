package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.ArtifactWheel.ArtifactWheelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenArtifactWheelPacket(List<String> abilities) implements CustomPacketPayload {
    public static final Type<OpenArtifactWheelPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_artifact_wheel"));

    public static final StreamCodec<FriendlyByteBuf, OpenArtifactWheelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenArtifactWheelPacket::abilities,
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
                        (id, inventory, p) -> new ArtifactWheelMenu(id, inventory, packet.abilities()),
                        Component.translatable("lotm.ability_wheel.title")
                ), buffer -> {
                    buffer.writeCollection(packet.abilities(), FriendlyByteBuf::writeUtf);
                });
            }
        });
    }
}