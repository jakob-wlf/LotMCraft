package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncTelepathyAbilityS2CPacket(boolean active, int entityId, List<String> goalNames) implements CustomPacketPayload {


    public static final Type<SyncTelepathyAbilityS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_telepathy_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncTelepathyAbilityS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncTelepathyAbilityS2CPacket::active,
                    ByteBufCodecs.INT, SyncTelepathyAbilityS2CPacket::entityId,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), SyncTelepathyAbilityS2CPacket::goalNames,
                    SyncTelepathyAbilityS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTelepathyAbilityS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncTelepathyAbility(packet, context.player());
            }
        });
    }
}
