package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.AngelAuthorityAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record CutAnchorPacket(String anchorId) implements CustomPacketPayload {
    public static final Type<CutAnchorPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "cut_anchor"));

    public static final StreamCodec<ByteBuf, CutAnchorPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CutAnchorPacket::anchorId,
            CutAnchorPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CutAnchorPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()
                    || !(context.player() instanceof ServerPlayer player)) return;
            try {
                if (AngelAuthorityAbility.cutAnchor(player, UUID.fromString(packet.anchorId))) {
                    AngelAuthorityAbility.openAnchorCuttingScreen(player);
                }
            } catch (IllegalArgumentException ignored) {
            }
        });
    }
}