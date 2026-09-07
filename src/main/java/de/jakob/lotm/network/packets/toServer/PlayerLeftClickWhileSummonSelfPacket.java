package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.fool.HistoricalVoidSummonSelfAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityWheelPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayerLeftClickWhileSummonSelfPacket() implements CustomPacketPayload {

    public static final Type<PlayerLeftClickWhileSummonSelfPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "player_left_click_while_summon_self"));

    public static final StreamCodec<ByteBuf, PlayerLeftClickWhileSummonSelfPacket> STREAM_CODEC = StreamCodec.unit(new PlayerLeftClickWhileSummonSelfPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerLeftClickWhileSummonSelfPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                HistoricalVoidSummonSelfAbility.onPlayerLeftClickServer(serverPlayer);
            }
        });
    }
}