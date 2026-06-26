package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.MindWorldAuthorityEnvisioningAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncEnvisioningPacket(boolean canEnvision) implements CustomPacketPayload {
        public static final Type<SyncEnvisioningPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_envisioning"));

        public static final StreamCodec<FriendlyByteBuf, SyncEnvisioningPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SyncEnvisioningPacket::canEnvision,
                    SyncEnvisioningPacket::new
            );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SyncEnvisioningPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                MindWorldAuthorityEnvisioningAbility.canEnvisionClient = packet.canEnvision();
            });
        }

}
