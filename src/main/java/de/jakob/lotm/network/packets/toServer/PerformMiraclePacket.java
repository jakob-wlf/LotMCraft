package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.door.TravelersDoorAbility;
import de.jakob.lotm.abilities.fool.miracle_creation.MiracleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PerformMiraclePacket(String miracle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PerformMiraclePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "perform_miracle"));

    public static final StreamCodec<FriendlyByteBuf, PerformMiraclePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PerformMiraclePacket::miracle,
                    PerformMiraclePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PerformMiraclePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            MiracleHandler.performMiracle(packet.miracle(), serverPlayer.serverLevel(), context.player());
        });
    }
}
