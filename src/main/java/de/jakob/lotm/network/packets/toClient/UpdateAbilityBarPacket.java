package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.Introspect.IntrospectScreen;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record UpdateAbilityBarPacket(ArrayList<String> abilities) implements CustomPacketPayload {
    public static final Type<UpdateAbilityBarPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "update_ability_bar"));

    public static final StreamCodec<ByteBuf, UpdateAbilityBarPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            UpdateAbilityBarPacket::abilities,
            UpdateAbilityBarPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateAbilityBarPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleUpdateAbilityBarPacket(packet.abilities);
        });
    }
}