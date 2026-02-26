package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.HonorificName;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedList;

/**
 * Sent by the client when the player sets their honorific name via the in-menu UI.
 * The server validates the lines (same rules as /honorificname set) and saves the name.
 */
public record SetHonorificNamePacket(LinkedList<String> lines) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetHonorificNamePacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "set_honorific_name"));

    public static final StreamCodec<FriendlyByteBuf, SetHonorificNamePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeVarInt(pkt.lines().size());
                        for (String line : pkt.lines()) {
                            buf.writeUtf(line, HonorificName.MAX_LENGTH);
                        }
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        LinkedList<String> list = new LinkedList<>();
                        for (int i = 0; i < size; i++) {
                            list.add(buf.readUtf(HonorificName.MAX_LENGTH));
                        }
                        return new SetHonorificNamePacket(list);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetHonorificNamePacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;

            ServerPlayer player = (ServerPlayer) context.player();
            LinkedList<String> lines = pkt.lines();

            try {
                int sequence = BeyonderData.getSequence(player);

                if (sequence >= 4) {
                    player.sendSystemMessage(Component.literal(
                            "You must be sequence 3 or higher to utilize honorific name!")
                            .withStyle(ChatFormatting.RED));
                    return;
                }

                if (sequence == 3 && lines.size() != 5) {
                    player.sendSystemMessage(Component.literal(
                            "You must have 5 lines in honorific name as sequence 3")
                            .withStyle(ChatFormatting.RED));
                    return;
                }

                if (sequence == 2 && lines.size() < 4) {
                    player.sendSystemMessage(Component.literal(
                            "You must have 4 lines in honorific name as sequence 2")
                            .withStyle(ChatFormatting.RED));
                    return;
                }

                if (lines.size() < 3) {
                    player.sendSystemMessage(Component.literal(
                            "You must provide at least 3 lines.")
                            .withStyle(ChatFormatting.RED));
                    return;
                }

                if (lines.stream().distinct().count() != lines.size()) {
                    player.sendSystemMessage(Component.literal(
                            "Each line must be different!")
                            .withStyle(ChatFormatting.RED));
                    return;
                }

                for (String line : lines) {
                    if (line.length() >= HonorificName.MAX_LENGTH) {
                        player.sendSystemMessage(Component.literal(
                                "Maximum length of a line is " + HonorificName.MAX_LENGTH + "!")
                                .withStyle(ChatFormatting.RED));
                        return;
                    }
                }

                String pathway = BeyonderData.getPathway(player);
                if (!HonorificName.validate(pathway, lines)) {
                    player.sendSystemMessage(Component.literal(
                            "Honorific name must contain these words in each line:\n "
                                    + HonorificName.getMustHaveWords(pathway))
                            .withStyle(ChatFormatting.RED));
                    return;
                }

                BeyonderData.setHonorificName(player, new HonorificName(lines));
                player.sendSystemMessage(Component.literal(
                        "Honorific name set successfully!")
                        .withStyle(ChatFormatting.GREEN));

            } catch (Exception e) {
                player.sendSystemMessage(Component.literal(
                        "Failed to set honorific name: " + e.getMessage())
                        .withStyle(ChatFormatting.RED));
            }
        });
    }
}
