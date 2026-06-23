package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSpiritVisionAbilityS2CPacket(boolean active, int entityId) implements CustomPacketPayload {


    public static final Type<SyncSpiritVisionAbilityS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_spirit_vision_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncSpiritVisionAbilityS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncSpiritVisionAbilityS2CPacket::active,
                    ByteBufCodecs.INT, SyncSpiritVisionAbilityS2CPacket::entityId,
                    SyncSpiritVisionAbilityS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSpiritVisionAbilityS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncSpiritVisionAbility(packet, context.player());
            }
        });
    }
}
