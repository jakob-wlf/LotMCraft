package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConsumeCharacteristicPacket(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<ConsumeCharacteristicPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "consume_characteristic"));

    public static final StreamCodec<FriendlyByteBuf, ConsumeCharacteristicPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of((buf, val) -> buf.writeEnum(val), buf -> buf.readEnum(InteractionHand.class)),
                    ConsumeCharacteristicPacket::hand,
                    ConsumeCharacteristicPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConsumeCharacteristicPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            InteractionHand hand = packet.hand();
            ItemStack stack = player.getItemInHand(hand);

            if (stack.getItem() instanceof BeyonderCharacteristicItem beChar) {
                int seq = beChar.getSequence();
                String path = beChar.getPathway();

                int playerSeq = BeyonderData.getSequence(player);
                
                if (seq >= playerSeq) {
                    //if ((seq >= 1 || playerSeq == 0) && (BeyonderData.getDigestionProgress(player) >= 1.0 || playerSeq == 0)) {
                        if (player.level() instanceof ServerLevel serverLevel
                                && !BeyonderData.hasSequenceSlotAvailableWithAdjustment(serverLevel, path, seq, seq, 0)) {
                            player.sendSystemMessage(Component.literal("No sequence slots available for that characteristic")
                                    .withStyle(ChatFormatting.RED));
                            return;
                        }

                        float missed = 0;
                        if(playerSeq > 0){
                            missed = 1-BeyonderData.getDigestionProgress(player);
                        }

                        BeyonderData.addCharacteristic(player, seq, path);
                        BeyonderData.setDigestionProgress(player, 0.5f);
                        BeyonderData.increaseCorruption(player, missed);
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            player.setItemInHand(hand, ItemStack.EMPTY);
                        }
                    //}
                }
            }
        });
    }
}
