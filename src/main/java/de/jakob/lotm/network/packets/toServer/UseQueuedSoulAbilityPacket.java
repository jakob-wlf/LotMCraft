package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.death.InternalUnderworldAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseQueuedSoulAbilityPacket() implements CustomPacketPayload {
    public static final Type<UseQueuedSoulAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "use_queued_soul_ability"));

    public static final StreamCodec<ByteBuf, UseQueuedSoulAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new UseQueuedSoulAbilityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseQueuedSoulAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                InternalUnderworldAbility.consumeQueuedSoulAbility(serverPlayer);
            }
        });
    }
}
