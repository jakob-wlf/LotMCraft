package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSpiritVisionAbilityPacket(boolean active, int entityId) implements CustomPacketPayload {


    public static final Type<SyncSpiritVisionAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_spirit_vision_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncSpiritVisionAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncSpiritVisionAbilityPacket::active,
                    ByteBufCodecs.INT, SyncSpiritVisionAbilityPacket::entityId,
                    SyncSpiritVisionAbilityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSpiritVisionAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncSpiritVisionAbility(packet, context.player());
            }
        });
    }
}
