package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSkillScalingPacket(Boolean scaleToSkill, int seq, String path, int entityId) implements CustomPacketPayload {
    public static final Type<SyncSkillScalingPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_skill_scaling"));

    public static final StreamCodec<ByteBuf, SyncSkillScalingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SyncSkillScalingPacket::scaleToSkill,
            ByteBufCodecs.INT,
            SyncSkillScalingPacket::seq,
            ByteBufCodecs.STRING_UTF8,
            SyncSkillScalingPacket::path,
            ByteBufCodecs.INT,
            SyncSkillScalingPacket::entityId,
            SyncSkillScalingPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSkillScalingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleSkillScalingPacket(packet);
            }
        });
    }
}
