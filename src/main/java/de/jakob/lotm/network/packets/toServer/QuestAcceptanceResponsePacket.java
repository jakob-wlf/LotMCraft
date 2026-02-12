package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.quest.QuestManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record QuestAcceptanceResponsePacket(String questId, boolean accepted) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<QuestAcceptanceResponsePacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "quest_acceptance_response"));
    
    public static final StreamCodec<ByteBuf, QuestAcceptanceResponsePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            QuestAcceptanceResponsePacket::questId,
            ByteBufCodecs.BOOL,
            QuestAcceptanceResponsePacket::accepted,
            QuestAcceptanceResponsePacket::new
    );
    
    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(QuestAcceptanceResponsePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (packet.accepted()) {
                    QuestManager.acceptQuestInternal(serverPlayer, packet.questId());
                }
                // If declined, do nothing - the quest remains with the NPC
            }
        });
    }
}