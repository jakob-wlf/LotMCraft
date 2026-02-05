package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.UpdateAbilityBarPacket;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestAbilityBarPacket() implements CustomPacketPayload {
    public static final Type<RequestAbilityBarPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_ability_bar"));

    public static final StreamCodec<ByteBuf, RequestAbilityBarPacket> STREAM_CODEC = StreamCodec.unit(new RequestAbilityBarPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestAbilityBarPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PacketHandler.sendToPlayer(player, new UpdateAbilityBarPacket(AbilityBarHelper.getAbilities(player)));
        });
    }
}