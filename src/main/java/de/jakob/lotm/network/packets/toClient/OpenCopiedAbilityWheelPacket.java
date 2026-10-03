package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-bound packet that signals the client to prepare for the copied ability wheel.
 * The actual menu opening is handled by the server via player.openMenu() in CopiedAbilityHelper.
 * This packet ensures the copied ability data is ready before the screen opens.
 */
public record OpenCopiedAbilityWheelPacket() implements CustomPacketPayload {

    public static final Type<OpenCopiedAbilityWheelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_copied_ability_wheel"));

    public static final StreamCodec<ByteBuf, OpenCopiedAbilityWheelPacket> STREAM_CODEC = StreamCodec.unit(new OpenCopiedAbilityWheelPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCopiedAbilityWheelPacket packet, IPayloadContext context) {
        // The server-side player.openMenu() handles the actual screen opening.
        // This packet is available for future extensibility.
    }
}
