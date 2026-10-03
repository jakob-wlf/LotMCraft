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

public record SyncTelepathyAbilityPacket(boolean active, int entityId, List<String> goalNames) implements CustomPacketPayload {


    public static final Type<SyncTelepathyAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_telepathy_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncTelepathyAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncTelepathyAbilityPacket::active,
                    ByteBufCodecs.INT, SyncTelepathyAbilityPacket::entityId,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), SyncTelepathyAbilityPacket::goalNames,
                    SyncTelepathyAbilityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTelepathyAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncTelepathyAbility(packet, context.player());
            }
        });
    }
}
