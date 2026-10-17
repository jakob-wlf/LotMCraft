package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.darkness.SpiritCommandingAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseSpiritPowerPacket(int index) implements CustomPacketPayload {
    public static final Type<UseSpiritPowerPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "use_spirit_power"));
    public static final StreamCodec<ByteBuf, UseSpiritPowerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UseSpiritPowerPacket::index, UseSpiritPowerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseSpiritPowerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SpiritCommandingAbility.castPower(player, packet.index());
            }
        });
    }
}
