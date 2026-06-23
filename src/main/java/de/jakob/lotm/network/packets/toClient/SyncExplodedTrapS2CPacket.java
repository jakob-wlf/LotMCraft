package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.red_priest.TrapAbility;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SyncExplodedTrapS2CPacket(UUID entityId) implements CustomPacketPayload {
    public static final Type<SyncExplodedTrapS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "stop_trap_particles"));

    public static final StreamCodec<FriendlyByteBuf, SyncExplodedTrapS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncExplodedTrapS2CPacket::entityId,
                    SyncExplodedTrapS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncExplodedTrapS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Store the explosion state for this entity
            TrapAbility.ClientTrapManager.setTrapExploded(packet.entityId());
        });
    }
}