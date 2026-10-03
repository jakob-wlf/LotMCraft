package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.beyonders.quest.Quest;
import de.jakob.lotm.beyonders.quest.QuestRegistry;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncQuestDataPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.jakob.lotm.LOTMCraft.MOD_ID;

public record AcceptSefirotInvitePacket() implements CustomPacketPayload {

    public static final Type<AcceptSefirotInvitePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "accept_sefirot_invite"));

    public static final StreamCodec<ByteBuf, AcceptSefirotInvitePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buffer, AcceptSefirotInvitePacket packet) {
            // No data to encode
        }

        @Override
        public AcceptSefirotInvitePacket decode(ByteBuf buffer) {
            return new AcceptSefirotInvitePacket();
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AcceptSefirotInvitePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SefirahHandler.acceptInvite(serverPlayer);
            }
        });
    }
}