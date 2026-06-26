package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.WaypointComponent;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record OpenWaypointSelectionScreenPacket(List<WaypointComponent.ClientWaypoint> waypoints, String use) implements CustomPacketPayload {

    public static final Type<OpenWaypointSelectionScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_waypoint_selection_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWaypointSelectionScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, WaypointComponent.ClientWaypoint.STREAM_CODEC),
                    OpenWaypointSelectionScreenPacket::waypoints,
                    ByteBufCodecs.STRING_UTF8,
                    OpenWaypointSelectionScreenPacket::use,
                    OpenWaypointSelectionScreenPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenWaypointSelectionScreenPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleWaypointSelectionScreenPacket(payload));
    }
}
