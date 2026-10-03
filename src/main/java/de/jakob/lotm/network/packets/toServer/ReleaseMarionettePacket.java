package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReleaseMarionettePacket(int entityId) implements CustomPacketPayload {

    public static final Type<ReleaseMarionettePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "release_marionette"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseMarionettePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ReleaseMarionettePacket::entityId,
                    ReleaseMarionettePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReleaseMarionettePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            ServerLevel level = serverPlayer.serverLevel();

            if (level.getEntity(packet.entityId()) instanceof LivingEntity living) {
                living.hurt(living.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
            }
        });
    }
}