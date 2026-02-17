package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.SelectionGui.PlayerSelectionGui;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.data.PlayerInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OpenPlayerDivinationScreenPacket(List<PlayerInfo> players) implements CustomPacketPayload {
    public static final Type<OpenPlayerDivinationScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_player_divination_screen"));

    private static final StreamCodec<RegistryFriendlyByteBuf, PlayerInfo> PLAYER_INFO_CODEC =
            StreamCodec.of(
                    (buf, playerInfo) -> {
                        buf.writeUUID(playerInfo.uuid());
                        buf.writeUtf(playerInfo.name());
                    },
                    buf -> {
                        UUID uuid = buf.readUUID();
                        String name = buf.readUtf(32767);
                        return new PlayerInfo(uuid, name);
                    }
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPlayerDivinationScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, PLAYER_INFO_CODEC),
                    OpenPlayerDivinationScreenPacket::players,
                    OpenPlayerDivinationScreenPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenPlayerDivinationScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.handlePlayerDivinationScreenPacket(packet);
            }
        });
    }

}