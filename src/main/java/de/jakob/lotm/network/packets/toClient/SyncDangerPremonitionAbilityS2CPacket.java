package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.DangerPremonitionOverlayRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDangerPremonitionAbilityS2CPacket(boolean active) implements CustomPacketPayload {


    public static final Type<SyncDangerPremonitionAbilityS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_danger_premonition_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncDangerPremonitionAbilityS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncDangerPremonitionAbilityS2CPacket::active,
                    SyncDangerPremonitionAbilityS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDangerPremonitionAbilityS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                if(packet.active()) {
                    DangerPremonitionOverlayRenderer.playersWithDangerPremonitionActivated.add(context.player().getUUID());
                } else {
                    DangerPremonitionOverlayRenderer.playersWithDangerPremonitionActivated.remove(context.player().getUUID());
                }
            }
        });
    }
}
