package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityWheelS2CPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestAbilityWheelC2SPacket() implements CustomPacketPayload {

    public static final Type<RequestAbilityWheelC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_ability_wheel"));

    public static final StreamCodec<ByteBuf, RequestAbilityWheelC2SPacket> STREAM_CODEC = StreamCodec.unit(new RequestAbilityWheelC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestAbilityWheelC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                AbilityWheelComponent component = serverPlayer.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
                PacketHandler.sendToPlayer(
                        serverPlayer,
                        new SyncAbilityWheelS2CPacket(component.getAbilities(), component.getSelectedAbility())
                );
            }
        });
    }
}