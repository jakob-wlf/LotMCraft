package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static de.jakob.lotm.LOTMCraft.MOD_ID;

public record DiscardQuestPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DiscardQuestPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "discard_quest"));

    public static final StreamCodec<ByteBuf, DiscardQuestPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buffer, DiscardQuestPacket packet) {
            // No data to encode
        }

        @Override
        public DiscardQuestPacket decode(ByteBuf buffer) {
            return new DiscardQuestPacket();
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DiscardQuestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                QuestComponent component = serverPlayer.getData(ModAttachments.QUEST_COMPONENT);
                
                if (!component.getQuestProgress().isEmpty()) {
                    // Clear the active quest
                    component.getQuestProgress().clear();
                    serverPlayer.sendSystemMessage(Component.translatable("lotm.quest.discarded").withColor(0xFF5722));
                }
            }
        });
    }
}