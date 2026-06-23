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

public record SyncSkillScalingS2CPacket(Boolean scaleToSkill, int seq, String path, int entityId) implements CustomPacketPayload {
    public static final Type<SyncSkillScalingS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_skill_scaling"));

    public static final StreamCodec<ByteBuf, SyncSkillScalingS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SyncSkillScalingS2CPacket::scaleToSkill,
            ByteBufCodecs.INT,
            SyncSkillScalingS2CPacket::seq,
            ByteBufCodecs.STRING_UTF8,
            SyncSkillScalingS2CPacket::path,
            ByteBufCodecs.INT,
            SyncSkillScalingS2CPacket::entityId,
            SyncSkillScalingS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSkillScalingS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleSkillScalingPacket(packet);
            }
        });
    }
}
