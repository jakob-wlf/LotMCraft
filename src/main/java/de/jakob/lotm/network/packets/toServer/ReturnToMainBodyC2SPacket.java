package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.error.ParasitationAbility;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.ControllingUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReturnToMainBodyC2SPacket() implements CustomPacketPayload {

    public static final Type<ReturnToMainBodyC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "return_to_main_body"));

    public static final StreamCodec<ByteBuf, ReturnToMainBodyC2SPacket> STREAM_CODEC = StreamCodec.unit(new ReturnToMainBodyC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReturnToMainBodyC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ControllingDataComponent data = serverPlayer.getData(ModAttachments.CONTROLLING_DATA);
                if (data.isControlling()) {
                    if (ParasitationAbility.isControlling(serverPlayer.getUUID())) {
                        ParasitationAbility.exitControl(serverPlayer.serverLevel(), serverPlayer);
                    } else {
                        ControllingUtil.reset(serverPlayer, serverPlayer.serverLevel(), true);
                    }
                }
            }
        });
    }
}