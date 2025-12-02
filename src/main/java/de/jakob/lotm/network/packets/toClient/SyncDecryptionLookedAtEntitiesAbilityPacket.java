package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDecryptionLookedAtEntitiesAbilityPacket(boolean active, int entityId) implements CustomPacketPayload {


    public static final Type<SyncDecryptionLookedAtEntitiesAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_decryption_entities"));

    public static final StreamCodec<FriendlyByteBuf, SyncDecryptionLookedAtEntitiesAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncDecryptionLookedAtEntitiesAbilityPacket::active,
                    ByteBufCodecs.INT, SyncDecryptionLookedAtEntitiesAbilityPacket::entityId,
                    SyncDecryptionLookedAtEntitiesAbilityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDecryptionLookedAtEntitiesAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncDecryptionAbility(packet, context.player());
            }
        });
    }
}
