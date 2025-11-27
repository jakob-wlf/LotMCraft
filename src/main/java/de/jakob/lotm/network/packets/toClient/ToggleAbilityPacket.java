package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import de.jakob.lotm.LOTMCraft;

/**
 * Packet sent from server to client to synchronize toggle ability state
 */
public record ToggleAbilityPacket(int entityId, String abilityId, boolean active) implements CustomPacketPayload {

    public static final Type<ToggleAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "toggle_ability")
    );

    public static final StreamCodec<ByteBuf, ToggleAbilityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ToggleAbilityPacket::entityId,
            ByteBufCodecs.STRING_UTF8,
            ToggleAbilityPacket::abilityId,
            ByteBufCodecs.BOOL,
            ToggleAbilityPacket::active,
            ToggleAbilityPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ToggleAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleToggleAbilityPacket(packet, context);
        });
    }
}