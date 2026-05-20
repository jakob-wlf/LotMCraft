package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenInternalUnderworldAbilityScreenPacket() implements CustomPacketPayload {
    public static final Type<OpenInternalUnderworldAbilityScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_internal_underworld_ability_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenInternalUnderworldAbilityScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenInternalUnderworldAbilityScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenInternalUnderworldAbilityScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                // Client-only: replace the vanilla chest screen with the custom underworld UI.
                ClientHandler.handleOpenInternalUnderworldAbilityScreenPacket();
            }
        });
    }
}
