package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.util.ClientQuestData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

import static de.jakob.lotm.LOTMCraft.MOD_ID;

public record SyncQuestDataPacket(
        Set<String> completedQuests,
        String activeQuestId,
        float activeQuestProgress,
        String activeQuestName,
        String activeQuestDescription,
        List<ItemStack> activeQuestRewards,
        int activeQuestDigestionReward
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncQuestDataPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync_quest_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncQuestDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, SyncQuestDataPacket packet) {

            // Write completed quests
            buf.writeInt(packet.completedQuests.size());
            for (String questId : packet.completedQuests) {
                buf.writeUtf(questId);
            }

            // Write active quest data
            buf.writeBoolean(packet.activeQuestId != null);
            if (packet.activeQuestId != null) {
                buf.writeUtf(packet.activeQuestId);
                buf.writeFloat(packet.activeQuestProgress);
                buf.writeUtf(packet.activeQuestName);
                buf.writeUtf(packet.activeQuestDescription);
                buf.writeInt(packet.activeQuestDigestionReward);

                // Write rewards
                buf.writeInt(packet.activeQuestRewards.size());
                for (ItemStack reward : packet.activeQuestRewards) {
                    ItemStack.STREAM_CODEC.encode(buf, reward);
                }
            }
        }

        @Override
        public SyncQuestDataPacket decode(RegistryFriendlyByteBuf buf) {

            // Read completed quests
            int completedSize = buf.readInt();
            Set<String> completedQuests = new HashSet<>();
            for (int i = 0; i < completedSize; i++) {
                completedQuests.add(buf.readUtf());
            }

            // Read active quest data
            String activeQuestId = null;
            float activeQuestProgress = 0f;
            String activeQuestName = "";
            String activeQuestDescription = "";
            int activeQuestDigestionReward = 0;
            List<ItemStack> activeQuestRewards = new ArrayList<>();

            boolean hasActiveQuest = buf.readBoolean();
            if (hasActiveQuest) {
                activeQuestId = buf.readUtf();
                activeQuestProgress = buf.readFloat();
                activeQuestName = buf.readUtf();
                activeQuestDescription = buf.readUtf();
                activeQuestDigestionReward = buf.readInt();

                // Read rewards
                int rewardSize = buf.readInt();
                for (int i = 0; i < rewardSize; i++) {
                    activeQuestRewards.add(ItemStack.STREAM_CODEC.decode(buf));
                }
            }

            return new SyncQuestDataPacket(completedQuests, activeQuestId, activeQuestProgress,
                    activeQuestName, activeQuestDescription, activeQuestRewards, activeQuestDigestionReward);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncQuestDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientQuestData.setCompletedQuests(packet.completedQuests);

            if (packet.activeQuestId != null) {
                ClientQuestData.setActiveQuest(
                        packet.activeQuestId,
                        packet.activeQuestProgress,
                        packet.activeQuestName,
                        packet.activeQuestDescription,
                        packet.activeQuestRewards,
                        packet.activeQuestDigestionReward
                );
            } else {
                ClientQuestData.clearActiveQuest();
            }
        });
    }
}