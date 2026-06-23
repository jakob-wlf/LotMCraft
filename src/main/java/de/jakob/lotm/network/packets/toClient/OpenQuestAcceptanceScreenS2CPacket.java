package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.Quest.QuestAcceptanceScreen;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
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

public record OpenQuestAcceptanceScreenS2CPacket(String questId, List<ItemStack> rewards, 
                                               float digestionReward, int questSequence, int npcId) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<OpenQuestAcceptanceScreenS2CPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_quest_acceptance_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenQuestAcceptanceScreenS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            OpenQuestAcceptanceScreenS2CPacket::questId,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            OpenQuestAcceptanceScreenS2CPacket::rewards,
            ByteBufCodecs.FLOAT,
            OpenQuestAcceptanceScreenS2CPacket::digestionReward,
            ByteBufCodecs.INT,
            OpenQuestAcceptanceScreenS2CPacket::questSequence,
            ByteBufCodecs.INT,
            OpenQuestAcceptanceScreenS2CPacket::npcId,
            OpenQuestAcceptanceScreenS2CPacket::new
    );
    
    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(OpenQuestAcceptanceScreenS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleQuestScreenPacket(packet);
        });
    }
}