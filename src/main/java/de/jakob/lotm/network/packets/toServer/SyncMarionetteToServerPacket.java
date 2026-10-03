package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncMarionetteToServerPacket(int entityId, int modeOrdinal, boolean shouldAttack) implements CustomPacketPayload {

    public static final Type<SyncMarionetteToServerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_marionette_to_server"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMarionetteToServerPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncMarionetteToServerPacket::entityId,
                    ByteBufCodecs.INT, SyncMarionetteToServerPacket::modeOrdinal,
                    ByteBufCodecs.BOOL, SyncMarionetteToServerPacket::shouldAttack,
                    SyncMarionetteToServerPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncMarionetteToServerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            ServerLevel level = serverPlayer.serverLevel();

            if (level.getEntity(packet.entityId()) instanceof LivingEntity living) {
                MarionetteComponent comp = living.getData(ModAttachments.MARIONETTE_COMPONENT);

                MarionetteComponent.MarionetteMode[] values = MarionetteComponent.MarionetteMode.values();
                int ordinal = Math.clamp(packet.modeOrdinal(), 0, values.length - 1);

                comp.setCurrentMode(values[ordinal]);
                comp.setShouldAttack(packet.shouldAttack());
            }
        });
    }
}