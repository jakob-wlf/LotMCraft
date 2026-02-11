package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncQuestDataPacket;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

import static de.jakob.lotm.LOTMCraft.MOD_ID;

public record RequestQuestDataPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestQuestDataPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "request_quest_data"));

    public static final StreamCodec<ByteBuf, RequestQuestDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buffer, RequestQuestDataPacket packet) {
            // No data to encode
        }

        @Override
        public RequestQuestDataPacket decode(ByteBuf buffer) {
            return new RequestQuestDataPacket();
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestQuestDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                QuestComponent component = serverPlayer.getData(ModAttachments.QUEST_COMPONENT);
                
                Set<String> completedQuests = component.getCompletedQuests();
                String activeQuestId = null;
                float activeQuestProgress = 0f;
                String activeQuestName = "";
                String activeQuestDescription = "";
                List<ItemStack> activeQuestRewards = new ArrayList<>();
                int activeQuestDigestionReward = 0;

                // Get active quest data
                if (!component.getQuestProgress().isEmpty()) {
                    Map.Entry<String, Float> activeQuestEntry = component.getQuestProgress().entrySet().iterator().next();
                    activeQuestId = activeQuestEntry.getKey();
                    activeQuestProgress = activeQuestEntry.getValue();
                    
                    Quest quest = QuestRegistry.getQuest(activeQuestId);
                    if (quest != null) {
                        activeQuestName = quest.getName().getString();
                        activeQuestDescription = quest.getDescription().getString();
                        activeQuestRewards = quest.getRewards();
                        activeQuestDigestionReward = (int) quest.getDigestionReward();
                    }
                }

                // Send quest data to client
                PacketHandler.sendToPlayer(serverPlayer, new SyncQuestDataPacket(
                        completedQuests,
                        activeQuestId,
                        activeQuestProgress,
                        activeQuestName,
                        activeQuestDescription,
                        activeQuestRewards,
                        activeQuestDigestionReward
                ));
            }
        });
    }
}