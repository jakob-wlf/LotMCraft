package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.Quest.QuestAcceptanceScreen;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record OpenQuestAcceptanceScreenPacket(String questId, List<ItemStack> rewards, 
                                               float digestionReward, int questSequence) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<OpenQuestAcceptanceScreenPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_quest_acceptance_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenQuestAcceptanceScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            OpenQuestAcceptanceScreenPacket::questId,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            OpenQuestAcceptanceScreenPacket::rewards,
            ByteBufCodecs.FLOAT,
            OpenQuestAcceptanceScreenPacket::digestionReward,
            ByteBufCodecs.INT,
            OpenQuestAcceptanceScreenPacket::questSequence,
            OpenQuestAcceptanceScreenPacket::new
    );
    
    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(OpenQuestAcceptanceScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Quest quest = QuestRegistry.getQuest(packet.questId());
            if (quest == null) {
                return;
            }
            
            Component questName = quest.getName();
            Component questDescription = quest.getDescription();
            
            Minecraft.getInstance().setScreen(new QuestAcceptanceScreen(
                    packet.questId(),
                    questName,
                    questDescription,
                    packet.rewards(),
                    packet.digestionReward(),
                    packet.questSequence()
            ));
        });
    }
}