package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.UpdateAbilityBarS2CPacket;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestAbilityBarC2SPacket() implements CustomPacketPayload {
    public static final Type<RequestAbilityBarC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_ability_bar"));

    public static final StreamCodec<ByteBuf, RequestAbilityBarC2SPacket> STREAM_CODEC = StreamCodec.unit(new RequestAbilityBarC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestAbilityBarC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PacketHandler.sendToPlayer(player, new UpdateAbilityBarS2CPacket(AbilityBarHelper.getAbilities(player)));
        });
    }
}