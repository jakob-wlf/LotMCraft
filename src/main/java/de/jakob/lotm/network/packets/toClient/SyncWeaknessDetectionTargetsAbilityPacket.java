package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncWeaknessDetectionTargetsAbilityPacket(
        boolean active,
        Map<Integer, Integer> targets
) implements CustomPacketPayload {

    public static final Type<SyncWeaknessDetectionTargetsAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    LOTMCraft.MOD_ID,
                    "sync_weakness_detection_targets"
            ));

    public static final StreamCodec<ByteBuf, SyncWeaknessDetectionTargetsAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncWeaknessDetectionTargetsAbilityPacket::active,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, ByteBufCodecs.VAR_INT, 256),
                    SyncWeaknessDetectionTargetsAbilityPacket::targets,
                    SyncWeaknessDetectionTargetsAbilityPacket::new
            );

    public static void handle(SyncWeaknessDetectionTargetsAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleSyncWeaknessDetectionPacket(packet));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}