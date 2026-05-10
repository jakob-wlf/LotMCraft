package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.error.ParasitationAbility;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.ControllingUtil;
import de.jakob.lotm.util.DiscernmentUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StopDiscernmentPacket() implements CustomPacketPayload {
    public static final Type<StopDiscernmentPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "stop_discernment"));

    public static final StreamCodec<ByteBuf, StopDiscernmentPacket> STREAM_CODEC = StreamCodec.unit(new StopDiscernmentPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StopDiscernmentPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                var component = serverPlayer.getData(ModAttachments.DISCERNMENT_DATA.get());

                if(component.isDiscerning()){
                    DiscernmentUtil.stopDiscernment(serverPlayer);
                }
            }
        });
    }
}
