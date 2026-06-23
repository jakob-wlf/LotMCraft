package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.quest.QuestManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record QuestAcceptanceResponseC2SPacket(String questId, boolean accepted, int npcId) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<QuestAcceptanceResponseC2SPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "quest_acceptance_response"));
    
    public static final StreamCodec<ByteBuf, QuestAcceptanceResponseC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            QuestAcceptanceResponseC2SPacket::questId,
            ByteBufCodecs.BOOL,
            QuestAcceptanceResponseC2SPacket::accepted,
            ByteBufCodecs.INT,
            QuestAcceptanceResponseC2SPacket::npcId,
            QuestAcceptanceResponseC2SPacket::new
    );
    
    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(QuestAcceptanceResponseC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (packet.accepted()) {
                    QuestManager.acceptQuestInternal(serverPlayer, packet.questId());

                    Entity npc = serverPlayer.level().getEntity(packet.npcId());
                    if(!(npc instanceof BeyonderNPCEntity beyonderNPC))
                        return;

                    beyonderNPC.setQuestId("");
                }
            }
        });
    }
}