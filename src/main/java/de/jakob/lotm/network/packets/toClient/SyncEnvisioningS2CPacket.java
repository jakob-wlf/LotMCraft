package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.MindWorldAuthorityEnvisioningAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncEnvisioningS2CPacket(boolean canEnvision) implements CustomPacketPayload {
        public static final Type<SyncEnvisioningS2CPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_envisioning"));

        public static final StreamCodec<FriendlyByteBuf, SyncEnvisioningS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SyncEnvisioningS2CPacket::canEnvision,
                    SyncEnvisioningS2CPacket::new
            );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SyncEnvisioningS2CPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                MindWorldAuthorityEnvisioningAbility.canEnvisionClient = packet.canEnvision();
            });
        }

}
