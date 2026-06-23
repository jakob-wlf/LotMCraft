package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BlessingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ApplyBlessingPacket(UUID targetUUID, String blessingId) implements CustomPacketPayload {
    public static final Type<ApplyBlessingPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "apply_blessing"));

    public static final StreamCodec<FriendlyByteBuf, ApplyBlessingPacket> STREAM_CODEC =
            StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC),
                    ApplyBlessingPacket::targetUUID,
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8,
                    ApplyBlessingPacket::blessingId,
                    ApplyBlessingPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ApplyBlessingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            ServerPlayer target = sender.getServer().getPlayerList().getPlayer(packet.targetUUID());
            if (target != null) {
                // In a real scenario, we might want to check if the target is actually an anchor of the sender
                BlessingManager.applyBlessing(packet.blessingId(), sender, target);
            }
        });
    }
}
